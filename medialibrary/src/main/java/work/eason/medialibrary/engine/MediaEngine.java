package work.eason.medialibrary.engine;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.view.SurfaceHolder;

import work.eason.medialibrary.util.GlobalDefine;
import work.eason.medialibrary.video.BaseCamera;
import work.eason.medialibrary.video.CameraCallback;
import work.eason.medialibrary.video.CameraOnTexture;
import work.eason.medialibrary.video.EglWrapper;

public class MediaEngine {
    private static final String TAG = GlobalDefine.TAG + "MediaEngine";

    public static final int MSG_CAMERA_RESOLUTION = 0;

    private BaseCamera mCamera = null;
    private CameraCallback cameraCallback;
    private EglWrapper eglWrapper;

    private Activity mActivity;
    private Handler activityHandler, backgroundHandler;
    private SurfaceTexture mSurfaceTexture;

    private int targetWidth, targetHeight;

    public MediaEngine(Activity activity, Handler handler) {
        this.mActivity = activity;
        this.activityHandler = handler;
    }

    public void start(SurfaceHolder surfaceHolder) {
        eglWrapper = new EglWrapper(surfaceHolder);
        mSurfaceTexture = eglWrapper.getCameraTexture();

        startCamera();
    }

    public void stop() {
        mCamera.stopCamera();
        eglWrapper.release();
    }

    public void setTargetResolution(int width, int height) {
        targetWidth = width;
        targetHeight = height;
    }

    public void setViewResolution(int width, int height) {
        eglWrapper.setViewResolution(width, height);
    }




    private void startCamera() {
        cameraCallback = new CameraCallback() {
            @Override
            public void updateCameraResolution(int width, int height) {
                activityHandler.obtainMessage(MSG_CAMERA_RESOLUTION, width, height).sendToTarget();
            }
        };
        mCamera = new CameraOnTexture(mActivity, mSurfaceTexture, targetWidth, targetHeight, cameraCallback);
        mCamera.startCamera();
    }
}
