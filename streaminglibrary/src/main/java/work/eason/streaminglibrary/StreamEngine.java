package work.eason.streaminglibrary;

import work.eason.streaminglibrary.util.GlobalDefine;

public class StreamEngine {
    private static final String TAG = GlobalDefine.TAG + "StreamEngine";

    private int nativeHandle;

    public static void initialize() {

    }

    public void start(MediaParameters params) {
        nativeHandle = nativeStreamStart(params);

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
}
