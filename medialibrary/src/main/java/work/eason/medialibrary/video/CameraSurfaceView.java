package work.eason.medialibrary.video;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import work.eason.medialibrary.util.GlobalDefine;

/**
 * Created by yu-hsunchen on 2018/8/13.
 */

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = GlobalDefine.TAG + "CameraSurfaceView";

    private SurfaceHolder mHolder = null;

    private CameraSurfaceCallback cameraSurfaceCallback = null;

    public interface CameraSurfaceCallback {
        public void onSurfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.i(TAG, "surfaceCreated ");
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged " + width + " x " + height);
        if (null != cameraSurfaceCallback)
            cameraSurfaceCallback.onSurfaceChanged(surfaceHolder, format, width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    public void setCameraViewCallback(CameraSurfaceCallback callback) {
        cameraSurfaceCallback = callback;
    }
}
