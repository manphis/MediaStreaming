package work.eason.medialibrary.util;

import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;

import java.util.List;

public class CameraUtil {
    private static final String TAG = GlobalDefine.TAG + "CameraUtil";

    private static int cameraId = -1;

    public static Camera getCameraInstance(){
        Camera camera = null;
        try {
            cameraId = findCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
            if (cameraId < 0)
                cameraId = findCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
//	    	camera = Camera.open(FRONT_CAMERA);
            Log.e(TAG, "Open Camera No. " + cameraId);
            camera = Camera.open(cameraId);
        } catch (Exception e){
            Log.e(TAG, "Camera is not available: " + e.getMessage());
        }
        return camera;
    }

    public static int getCameraInstanceId(){
        return cameraId;
    }

    public static Camera.Size selectPreviewSize(List<Camera.Size> previewSizes, int targetWidth, int targetHeight) {
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

    public static void setCameraDisplayOrientation(Activity activity,
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



    private static int findCameraId(int id) {
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
}
