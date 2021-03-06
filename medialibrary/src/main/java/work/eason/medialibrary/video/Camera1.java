package work.eason.medialibrary.video;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;

import work.eason.medialibrary.util.CameraUtil;
import work.eason.medialibrary.util.GlobalDefine;

public class Camera1 extends BaseCamera implements Camera.PreviewCallback {
    private static final String TAG = GlobalDefine.TAG + "Camera1";
    private static final int FIX_FRAMERATE = 30;
    private static final int PIXEL_FORMAT = ImageFormat.NV21;
    private static final int BUFFER_NUM = 3;

    private Camera mCamera = null;
    private SurfaceHolder mHolder = null;
    private Activity mActivity = null;
    private PixelFormat mPixelFormat = new PixelFormat();

    private int mCameraIndex;
    private int cameraFPS;


    public Camera1(Activity activity, SurfaceHolder holder, int width, int height, CameraCallback callback) {
        this.mActivity = activity;
        this.mHolder = holder;
        this.targetWidth = width;
        this.targetHeight = height;
        this.cameraCallback = callback;
    }

    @Override
    public void startCamera() {
        Log.i(TAG, "startCamera");
        mCamera = CameraUtil.getCameraInstance();
        if (null == mCamera) {
            Log.e(TAG, "Open Camera NULL");
            return;
        }
        try {
            mCamera.stopPreview();
            mCamera.setPreviewDisplay(mHolder);
            mCameraIndex = CameraUtil.getCameraInstanceId();
            CameraUtil.setCameraDisplayOrientation(mActivity, mCameraIndex, mCamera);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Camera.Parameters params = mCamera.getParameters();
        List<int[]> fpsList = params.getSupportedPreviewFpsRange();
        for (int[] item : fpsList) {
            Log.i(TAG, "support FPS range = " + item[0] + "," + item[1]);
        }

        Camera.Size selectSize = CameraUtil.selectPreviewSize(mCamera.getParameters().getSupportedPreviewSizes(), targetWidth, targetHeight);

        cameraFPS = FIX_FRAMERATE;
        params.setPreviewFormat(PIXEL_FORMAT);
        params.setPreviewSize(selectSize.width, selectSize.height);
        params.setPreviewFpsRange(cameraFPS*1000, cameraFPS*1000);
        params.setPreviewFrameRate(cameraFPS);

        mCamera.setParameters(params);

        cameraWidth = selectSize.width;
        cameraHeight = selectSize.height;

        Log.e(TAG, "startCamera target width " + targetWidth + " height " + targetHeight + " fps " + cameraFPS + " filterIndex = ");
        Log.e(TAG, "startCamera width " + cameraWidth + " height " + cameraHeight);
//        Log.e(TAG, "mRotationDegree = " + mRotationDegree);

        PixelFormat.getPixelFormatInfo(PIXEL_FORMAT, mPixelFormat);
        byte[] buffer = null;
        for (int i=0; i < BUFFER_NUM; i++) {
            buffer = new byte[cameraWidth * cameraHeight * mPixelFormat.bitsPerPixel/8];
            mCamera.addCallbackBuffer(buffer);
        }

        mCamera.setPreviewCallbackWithBuffer(this);
        mCamera.startPreview();

        cameraCallback.updateCameraResolution(cameraWidth, cameraHeight);
    }

    @Override
    public void stopCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }



    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        camera.addCallbackBuffer(bytes);
    }
}
