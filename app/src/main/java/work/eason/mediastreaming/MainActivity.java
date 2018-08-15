package work.eason.mediastreaming;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import work.eason.medialibrary.video.Camera1;
import work.eason.medialibrary.video.BaseCamera;
import work.eason.medialibrary.video.CameraCallback;
import work.eason.medialibrary.video.CameraSurfaceView;
import work.eason.util.GlobalDefine;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = GlobalDefine.TAG + "MainActivity";

    private Handler backgroundHandler;

    private CameraSurfaceView cameraSurfaceView = null;
    private CameraSurfaceView.CameraSurfaceCallback cameraViewCallback = null;
    private BaseCamera mCamera = null;
    private CameraCallback cameraCallback;

    private int screenWidth, screenHeight, degrees;

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
        initUI();
        initComponent();
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
        cameraSurfaceView = (CameraSurfaceView) findViewById(R.id.main_camera_view);
        cameraViewCallback = new CameraSurfaceView.CameraSurfaceCallback() {
            @Override
            public void onSurfaceChanged(final SurfaceHolder surfaceHolder, final int format,
                                         final int width, final int height) {
                Log.i(TAG, "onSurfaceChanged: " + format + " " + width + " x " + height);
                if (null == mCamera) {
                    backgroundHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            startCamera(surfaceHolder, width, height);
                        }
                    });
                }
            }
        };
        cameraSurfaceView.setCameraViewCallback(cameraViewCallback);
    }

    private void initComponent() {
        HandlerThread thread = new HandlerThread("background_handler");
        thread.start();
        backgroundHandler = new Handler(thread.getLooper());
    }

    private void startCamera(SurfaceHolder holder, int width, int height) {
        cameraCallback = new CameraCallback() {
            @Override
            public void updateCameraResolution(int width, int height) {
                if (degrees == 0 || degrees == 180)
                    updateView(height, width, screenWidth, screenHeight, cameraSurfaceView);
                else updateView(width, height, screenWidth, screenHeight, cameraSurfaceView);
            }
        };
        mCamera = new Camera1(this, holder, width, height, cameraCallback);
        mCamera.startCamera();
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
}
