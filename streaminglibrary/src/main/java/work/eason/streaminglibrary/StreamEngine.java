package work.eason.streaminglibrary;

import android.util.Log;

import work.eason.streaminglibrary.util.GlobalDefine;

public class StreamEngine {
    private static final String TAG = GlobalDefine.TAG + "StreamEngine";

    private int nativeHandle;

    public static void initialize() {

    }

    public void start(MediaParameters params) {
        nativeHandle = nativeStreamStart(params);
    }

    public int sendFrame(byte[] frame, int size) {
        Log.i(TAG, "sendFrame size = " + size);
        return nativeSendFrame(frame, size);
    }

    public void stop() {
        nativeStreamStop(nativeHandle);
    }

    static {
        System.loadLibrary("qstream_engine");
        nativeClassInit();
    }

    private static native boolean nativeClassInit();
    private native int nativeStreamStart(MediaParameters params);
    private native int nativeStreamStop(int handle);
    private native int nativeSendFrame(byte[] frame, int size);
}
