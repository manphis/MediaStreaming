package work.eason.mediastreaming;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

import work.eason.medialibrary.video.HardwareEncoder;
import work.eason.streaminglibrary.StreamEngine;
import work.eason.util.GlobalDefine;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final String TAG = GlobalDefine.TAG + "MainActivity";

    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;

    private MainHandler mHandler;
    private MediaEngine mediaEngine;

    private int screenWidth, screenHeight, degrees;

    public static class MainHandler extends Handler {
        private WeakReference<MainActivity> mWeakActivity;

        public MainHandler(MainActivity activity) {
            mWeakActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mWeakActivity.get();
            if (activity == null) {
                Log.d(TAG, "Got message for dead activity");
                return;
            }
            Log.i(TAG, "recv msg = " + msg.what);

            switch (msg.what) {
                case MediaEngine.MSG_CAMERA_RESOLUTION:
                    int width = msg.arg1;
                    int height = msg.arg2;
                    if (activity.degrees == 0 || activity.degrees == 180)
                        activity.updateView(height, width, activity.screenWidth, activity.screenHeight, activity.mSurfaceView);
                    else activity.updateView(width, height, activity.screenWidth, activity.screenHeight, activity.mSurfaceView);

                    break;

                case HardwareEncoder.MSG_FRAME_AVAILABLE_SOON:
                    activity.frameAvailToEncoder();
                    break;

                default:
                    throw new RuntimeException("Unknown message " + msg.what);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        getScreenResolution();
        initComponent();
        initUI();

        StreamEngine.initialize();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mediaEngine.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mediaEngine.start(surfaceHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int width, int height) {
        mediaEngine.setViewResolution(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }





    private void getScreenResolution() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        int rotation = display.getRotation();
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        Log.i(TAG, "screen size = " + screenWidth + " x " + screenHeight + " degrees = " + degrees);
    }

    private void initUI() {
        mSurfaceView = (SurfaceView) findViewById(R.id.main_camera_surfaceView);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
    }

    private void initComponent() {
        mHandler = new MainHandler(this);
        mediaEngine = new MediaEngine(this, mHandler);
        mediaEngine.setTargetResolution(640, 480);
    }

    private void startCamera(SurfaceHolder holder, int width, int height) {
//        cameraCallback = new CameraCallback() {
//            @Override
//            public void updateCameraResolution(int width, int height) {
//                if (degrees == 0 || degrees == 180)
//                    updateView(height, width, screenWidth, screenHeight, cameraSurfaceView);
//                else updateView(width, height, screenWidth, screenHeight, cameraSurfaceView);
//            }
//        };
//        mCamera = new Camera1(this, holder, width, height, cameraCallback);
//        mCamera.startCamera();
    }

    private void updateView(int inWidth, int inHeight, int outWidth, int outHeight, View view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();

        float widthRatio = ((float)outWidth) / inWidth;
        float heightRatio = ((float)outHeight) / inHeight;
        float ratio = Math.min(widthRatio, heightRatio);

        params.width = (int) (inWidth * ratio);
        params.height = (int) (inHeight * ratio);

        Log.i(TAG, "width ratio = " + widthRatio + " height ratio = " + heightRatio + " ratio = " + ratio);

        view.setLayoutParams(params);

    }

    private void frameAvailToEncoder() {
        mediaEngine.frameAvailToEncoder();
    }


    public void startTest(View v) {
        mediaEngine.startEncode(degrees);
        mediaEngine.startStreaming("172.16.13.42", 8888);
    }
}
