package work.eason.medialibrary.video;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import work.eason.medialibrary.engine.MediaEngine;
import work.eason.medialibrary.gles.EglCore;
import work.eason.medialibrary.gles.FullFrameRect;
import work.eason.medialibrary.gles.Texture2dProgram;
import work.eason.medialibrary.gles.WindowSurface;
import work.eason.medialibrary.util.GlobalDefine;

public class EglWrapper implements SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = GlobalDefine.TAG + "EglWrapper";

    private Handler activityHandler;

    private EglCore mEglCore;
    private WindowSurface mDisplaySurface;
    private WindowSurface mEncoderSurface = null;
    private SurfaceTexture mCameraTexture;  // receives the output from the camera preview
    private FullFrameRect mFullFrameBlit;
    private final float[] mTmpMatrix = new float[16];
    private int mTextureId;
    private int mFrameNum;

    private int viewWidth, viewHeight;
    private int cameraWidth, cameraHeight;

    public EglWrapper(SurfaceHolder holder, Handler handler) {
        activityHandler = handler;
        mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
        mDisplaySurface = new WindowSurface(mEglCore, holder.getSurface(), false);
        mDisplaySurface.makeCurrent();

        mFullFrameBlit = new FullFrameRect(
                new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT));
        mTextureId = mFullFrameBlit.createTextureObject();
        mCameraTexture = new SurfaceTexture(mTextureId);
        mCameraTexture.setOnFrameAvailableListener(this);
    }

    public SurfaceTexture getCameraTexture() {
        return mCameraTexture;
    }

    public void setViewResolution(int width, int height) {
        Log.i(TAG, "setViewResolution: " + width + " x " + height);
        viewWidth = width;
        viewHeight = height;
    }

    public void createEncoderSurface(Surface surface, int width, int height) {
        cameraWidth = width;
        cameraHeight = height;
        mEncoderSurface = new WindowSurface(mEglCore, surface, true);
    }

    public void release() {
        if (mCameraTexture != null) {
            mCameraTexture.release();
            mCameraTexture = null;
        }
        if (mDisplaySurface != null) {
            mDisplaySurface.release();
            mDisplaySurface = null;
        }
        if (mFullFrameBlit != null) {
            mFullFrameBlit.release(false);
            mFullFrameBlit = null;
        }
        if (mEglCore != null) {
            mEglCore.release();
            mEglCore = null;
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if (mEglCore == null) {
            Log.d(TAG, "Skipping drawFrame after shutdown");
            return;
        }

        // Latch the next frame from the camera.
        mDisplaySurface.makeCurrent();
        mCameraTexture.updateTexImage();
        mCameraTexture.getTransformMatrix(mTmpMatrix);

        // Fill the SurfaceView with it.
        GLES20.glViewport(0, 0, viewWidth, viewHeight);
        mFullFrameBlit.drawFrame(mTextureId, mTmpMatrix);
//        drawExtra(mFrameNum, viewWidth, viewHeight);
        mDisplaySurface.swapBuffers();

        if (mEncoderSurface != null) {
            mEncoderSurface.makeCurrent();
            GLES20.glViewport(0, 0, cameraWidth, cameraHeight);
            mFullFrameBlit.drawFrame(mTextureId, mTmpMatrix);
//            drawExtra(mFrameNum, VIDEO_WIDTH, VIDEO_HEIGHT);
//            mCircEncoder.frameAvailableSoon();
            activityHandler.sendEmptyMessage(MediaEngine.MSG_FRAME_AVAIL_ENCODER);
            mEncoderSurface.setPresentationTime(mCameraTexture.getTimestamp());
            mEncoderSurface.swapBuffers();
        }
    }
}
