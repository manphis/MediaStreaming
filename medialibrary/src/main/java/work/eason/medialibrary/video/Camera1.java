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
        mCamera = getCameraInstance();
        if (null == mCamera) {
            Log.e(TAG, "Open Camera NULL");
            return;
        }
        try {
            mCamera.stopPreview();
            mCamera.setPreviewDisplay(mHolder);
            setCameraDisplayOrientation(mActivity, mCameraIndex, mCamera);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Camera.Parameters params = mCamera.getParameters();
        List<int[]> fpsList = params.getSupportedPreviewFpsRange();
        for (int[] item : fpsList) {
            Log.i(TAG, "support FPS range = " + item[0] + "," + item[1]);
        }

        Camera.Size selectSize = selectPreviewSize(mCamera.getParameters().getSupportedPreviewSizes(), targetWidth, targetHeight);

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

    private void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private Camera.Size selectPreviewSize(List<Camera.Size> previewSizes, int targetWidth, int targetHeight) {
        Camera.Size selectSize = null;
        int tempWidth = 0;
        int tempHeight = 0;

        for (Camera.Size size : previewSizes) {
            Log.i(TAG, "support preview size = " + size.width + " x " + size.height);
        }

        for (Camera.Size size : previewSizes) {
            if (size.width == targetWidth && size.height == targetHeight) {
                selectSize = size;
                break;
            } else {
                int pixels = size.width * size.height;
                if (pixels <= targetWidth*targetHeight && pixels > tempWidth*tempHeight) {
                    tempWidth = size.width;
                    tempHeight = size.height;
                    selectSize = size;
                }
            }
        }

        return selectSize;
    }

    private Camera getCameraInstance(){
        Camera camera = null;
        try {
            mCameraIndex = findCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
            if (mCameraIndex < 0)
                mCameraIndex = findCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
//	    	camera = Camera.open(FRONT_CAMERA);
            Log.e(TAG, "Open Camera No. " + mCameraIndex);
            camera = Camera.open(mCameraIndex);
        } catch (Exception e){
            Log.e(TAG, "Camera is not available: " + e.getMessage());
        }
        return camera;
    }

    private int findCameraId(int id) {
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == id) {
                cameraId = i;
                break;
            }
        }

        return cameraId;
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        camera.addCallbackBuffer(bytes);
    }
}
