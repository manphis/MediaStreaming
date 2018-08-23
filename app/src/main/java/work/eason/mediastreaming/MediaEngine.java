package work.eason.mediastreaming;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.view.SurfaceHolder;

import work.eason.medialibrary.util.GlobalDefine;
import work.eason.medialibrary.video.BaseCamera;
import work.eason.medialibrary.video.CameraCallback;
import work.eason.medialibrary.video.CameraOnTexture;
import work.eason.medialibrary.video.EglWrapper;
import work.eason.medialibrary.video.EncoderCallback;
import work.eason.medialibrary.video.HardwareEncoder;
import work.eason.streaminglibrary.MediaParameters;
import work.eason.streaminglibrary.StreamEngine;

public class MediaEngine {
    private static final String TAG = GlobalDefine.TAG + "MediaEngine";

    public static final int MSG_CAMERA_RESOLUTION = 0;

    private static final int bitrate = 800000;
    private static final int fps = 15;

    private BaseCamera mCamera = null;
    private CameraCallback cameraCallback;
    private EglWrapper eglWrapper;
    private HardwareEncoder mEncoder = null;
    private StreamEngine streamEngine;
    private EncoderCallback encoderCallback = null;

    private Activity mActivity;
    private Handler activityHandler, backgroundHandler;
    private SurfaceTexture mSurfaceTexture;

    private int targetWidth, targetHeight;
    private int cameraWidth, cameraHeight;
    private int encoderWidth, encoderHeight;


    public MediaEngine(Activity activity, Handler handler) {
        this.mActivity = activity;
        this.activityHandler = handler;
    }

    public void start(SurfaceHolder surfaceHolder) {
        eglWrapper = new EglWrapper(surfaceHolder, activityHandler);
        mSurfaceTexture = eglWrapper.getCameraTexture();

        startCamera();
    }

    public void stop() {
        mCamera.stopCamera();
        if (null != streamEngine)
            streamEngine.stop();

        if (null != mEncoder)
            mEncoder.shutDown();
        eglWrapper.release();
    }

    public void setTargetResolution(int width, int height) {
        targetWidth = width;
        targetHeight = height;
    }

    public void setViewResolution(int width, int height) {
        eglWrapper.setViewResolution(width, height);
    }

    public void frameAvailToEncoder() {
        mEncoder.frameAvailableSoon();
    }

    public void startStreaming(String ip, int port) {
        streamEngine = new StreamEngine();
        MediaParameters params = new MediaParameters();
        params.videoPara.host_ipaddr = ip;
        params.videoPara.host_port = port;
        streamEngine.start(params);

        if (null != mEncoder) {
            encoderCallback = new EncoderCallback() {
                @Override
                public void onEncodeFrame(byte[] frame, int size) {
                    streamEngine.sendFrame(frame, size);
                }
            };
            mEncoder.setEncoderCallback(encoderCallback);
        }
    }

    public void startEncode(int degrees) {
        if (degrees == 0 || degrees == 180) {
            encoderWidth = cameraHeight;
            encoderHeight = cameraWidth;
        } else {
            encoderWidth = cameraWidth;
            encoderHeight = cameraHeight;
        }
        startEncoder();
    }




    private void startCamera() {
        cameraCallback = new CameraCallback() {
            @Override
            public void updateCameraResolution(int width, int height) {
                cameraWidth = width;
                cameraHeight = height;
                activityHandler.obtainMessage(MSG_CAMERA_RESOLUTION, width, height).sendToTarget();
            }
        };
        mCamera = new CameraOnTexture(mActivity, mSurfaceTexture, targetWidth, targetHeight, cameraCallback);
        mCamera.startCamera();
    }

    private void startEncoder() {
        mEncoder = new HardwareEncoder(encoderWidth, encoderHeight, bitrate, fps);
        eglWrapper.createEncoderSurface(mEncoder.getInputSurface(), encoderWidth, encoderHeight);
    }

}
