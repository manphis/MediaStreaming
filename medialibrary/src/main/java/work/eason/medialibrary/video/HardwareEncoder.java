package work.eason.medialibrary.video;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import work.eason.medialibrary.util.GlobalDefine;

public class HardwareEncoder {
    private static final String TAG = GlobalDefine.TAG + "HardwareEncoder";

    private static final boolean TEST_SAVE_FILE = true;
    private static final String TEST_FILE_NAME = "/sdcard/hw.h264";
    private static final String MIME_TYPE = "video/avc";
    private static final int IFRAME_INTERVAL = 5;

    private static final int MSG_FRAME_AVAILABLE_SOON = 1;

    private Surface mInputSurface;
    private MediaCodec mEncoder;
    private MediaCodec.BufferInfo mBufferInfo;
    private MediaFormat mEncodedFormat;
    HandlerThread handlerThread;
    private Handler mHandler;
    private BufferedOutputStream bos;

    private byte[] frame_info = null;


    public HardwareEncoder(int width, int height, int bitRate, int frameRate) {
        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, width, height);

        // Set some properties.  Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
        Log.i(TAG, "format: " + format);

        // Create a MediaCodec encoder, and configure it with our format.  Get a Surface
        // we can use for input and wrap it with a class that handles the EGL work.
        try {
            mEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
            mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mInputSurface = mEncoder.createInputSurface();
            mEncoder.start();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        mBufferInfo = new MediaCodec.BufferInfo();

        handlerThread = new HandlerThread("HandlerThread");
        handlerThread.start();

        mHandler = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message msg){
                super.handleMessage(msg);
//                Log.i(TAG, "recv msg = " + msg.what);

                switch(msg.what){
                    case MSG_FRAME_AVAILABLE_SOON:
                        drainEncoder();
                        break;
                }
            }
        };

        if (TEST_SAVE_FILE) {
            try {
                bos = new BufferedOutputStream(new FileOutputStream(new File(TEST_FILE_NAME)));
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            }

        }
    }

    public Surface getInputSurface() {
        return mInputSurface;
    }

    public void frameAvailableSoon() {
//        Log.i(TAG, "frameAvailableSoon");
        mHandler.sendEmptyMessage(MSG_FRAME_AVAILABLE_SOON);
    }

    public void shutDown() {
        if (null != handlerThread)
            handlerThread.quitSafely();

        if (TEST_SAVE_FILE) {
            try {
                bos.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

    }




    private void drainEncoder() {
        final int TIMEOUT_USEC = 0;     // no timeout -- check for buffers, bail if none

        ByteBuffer[] encoderOutputBuffers = mEncoder.getOutputBuffers();
        while (true) {
            int encoderStatus = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                break;
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = mEncoder.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // Should happen before receiving buffers, and should only happen once.
                // The MediaFormat contains the csd-0 and csd-1 keys, which we'll need
                // for MediaMuxer.  It's unclear what else MediaMuxer might want, so
                // rather than extract the codec-specific data and reconstruct a new
                // MediaFormat later, we just grab it here and keep it around.
                mEncodedFormat = mEncoder.getOutputFormat();
                Log.d(TAG, "encoder output format changed: " + mEncodedFormat);
            } else if (encoderStatus < 0) {
                Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                        encoderStatus);
                // let's ignore it
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // The codec config data was pulled out when we got the
                    // INFO_OUTPUT_FORMAT_CHANGED status.  The MediaMuxer won't accept
                    // a single big blob -- it wants separate csd-0/csd-1 chunks --
                    // so simply saving this off won't work.
                    Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
//                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0) {
                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);

//                    Log.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer, ts=" +
//                            mBufferInfo.presentationTimeUs);

                    byte[] b = new byte[encodedData.remaining()];
                    encodedData.get(b, 0, b.length);

                    byte[] h264_frame = CompleteH264Frame(b);

                    //TODO SAVE FILE
                    if (TEST_SAVE_FILE && null != h264_frame) {
                        try {
                            bos.write(h264_frame);
                            bos.flush();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }

                }

                mEncoder.releaseOutputBuffer(encoderStatus, false);

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.w(TAG, "reached end of stream unexpectedly");
                    break;      // out of while
                }
            }
        }
    }

    private byte[] CompleteH264Frame(byte[] input) {
        byte[] output = null;

        if (null == frame_info) {
            ByteBuffer spsPpsBuffer = ByteBuffer.wrap(input);
            Log.v(TAG, "swapYV12toI420:outData:"+input);
            Log.v(TAG, "swapYV12toI420:spsPpsBuffer:"+spsPpsBuffer);
//
            for(int i = 0; i < input.length; i++){
                Log.e(TAG, "run: get data rtpData[i]="+i+":"+input[i]);//输出SPS和PPS循环
            }

            if (spsPpsBuffer.getInt() == 0x00000001) {
                Log.i(TAG, "save spsPpsBuffer");
                frame_info = new byte[input.length];
                System.arraycopy(input, 0, frame_info, 0, input.length);
            } else {
                return null;
            }
        } else {
            if ((input[4] & 0x0f) == 5) { // key frame
                Log.i(TAG, "append spsPpsBuffer to key frame");
                output = new byte[frame_info.length + input.length];
                System.arraycopy(frame_info, 0,  output, 0, frame_info.length);
                System.arraycopy(input, 0,  output, frame_info.length, input.length);
            } else {
                output = new byte[input.length];
                System.arraycopy(input, 0,  output, 0, input.length);
            }
        }

        return output;
    }
}
