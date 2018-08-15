package work.eason.medialibrary.video;

public abstract class BaseCamera {
    protected int targetWidth, targetHeight;
    protected int cameraWidth, cameraHeight;

    protected CameraCallback cameraCallback = null;

    public abstract void startCamera();
    public abstract void stopCamera();
}
