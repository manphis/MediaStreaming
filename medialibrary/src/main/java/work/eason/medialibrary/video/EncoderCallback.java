package work.eason.medialibrary.video;

public interface EncoderCallback {
    public void onEncodeFrame(byte[] frame, int size);
}
