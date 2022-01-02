package com.crewkingstudio.mirrors;

import android.Manifest;
import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.crewkingstudio.mirrors.databinding.ActivityFullscreenBinding;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.flutter.embedding.android.FlutterFragment;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.FlutterEngineCache;
import io.flutter.embedding.engine.dart.DartExecutor;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, Runnable {

    private static final String TAG = "OCVSampleFaceDetect";
    private CameraBridgeViewBase cameraBridgeViewBase;

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully");
                cameraBridgeViewBase.enableView();

            } else {
                super.onManagerConnected(status);
            }
        }
    };

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 1000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 100;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (AUTO_HIDE) {
                        delayedHide(AUTO_HIDE_DELAY_MILLIS);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    view.performClick();
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private ActivityFullscreenBinding binding;

    private static final String TAG_FLUTTER_FRAGMENT = "flutter_fragment";

    private TextView infoFaces;

    FlutterFragment flutterFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFullscreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mVisible = true;
        mContentView = binding.fullscreenContent;

        hide();

        Log.d("d","dw");

        cameraBridgeViewBase = findViewById(R.id.main_surface);
        checkPermissions();

        infoFaces = findViewById(R.id.tv);


        FlutterEngine flutterEngine = new FlutterEngine(this);

        flutterEngine
                .getDartExecutor()
                .executeDartEntrypoint(
                        DartExecutor.DartEntrypoint.createDefault()
                );

        FlutterEngineCache
                .getInstance()
                .put(TAG_FLUTTER_FRAGMENT, flutterEngine);


        flutterFragment = FlutterFragment
                .withCachedEngine(TAG_FLUTTER_FRAGMENT)
                .build();

        /*FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction transaction = fragmentManager.beginTransaction().setCustomAnimations(R.anim.fade_out, R.anim.fade_in);

        transaction.replace(
                R.id.fullscreen_content,
                flutterFragment,
                TAG_FLUTTER_FRAGMENT
        );*/

        //transaction.commit();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    Boolean startFaces = false;
    Boolean firstTimeFaces = false;
    Net detector;
    Boolean Face_det = false;
    Boolean Mirror_Active = false;
    Boolean Face_TimeOut = false;
    long Face_time_start = 0, Face_time_current = 0;
    double Face_time_thresholder = 1.5;
    boolean running = false;

    private void checkPermissions() {
        if (isPermissionGranted()) {
            loadCameraBridge();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    private boolean isPermissionGranted() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkPermissions();
    }

    private void loadCameraBridge() {
        cameraBridgeViewBase.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        //cameraBridgeViewBase.setScaleX(0);
        //cameraBridgeViewBase.setScaleY(0);
        cameraBridgeViewBase.setCvCameraViewListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        disableCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isPermissionGranted()) return;
        resumeOCV();
    }

    private void resumeOCV() {
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);

            LoadFaceCNN();
        } else {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
        }

        if (running) return;
        new Thread(this).start();
    }

    public void onDestroy() {
        super.onDestroy();
        disableCamera();
    }

    private void disableCamera() {
        running = false;
        if (cameraBridgeViewBase != null)
            cameraBridgeViewBase.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        if (startFaces) {
            String protoPath = getPath("deploy.prototxt", this);
            String caffeWeights = getPath("res10_300x300_ssd_iter_140000.caffemodel", this);
            detector = Dnn.readNetFromCaffe(protoPath, caffeWeights);
        }
    }

    void LoadFaceCNN(){
        if (!startFaces) {
            startFaces = true;
            if (!firstTimeFaces) {
                firstTimeFaces = true;
                String protoPath = getPath("deploy.prototxt", this);
                String caffeWeights = getPath("res10_300x300_ssd_iter_140000.caffemodel", this);
                detector = Dnn.readNetFromCaffe(protoPath, caffeWeights);
            }
        } else {
            startFaces = false;
        }
    }

    private static String getPath(String file, Context context) {
        AssetManager assetManager = context.getAssets();
        BufferedInputStream inputStream = null;
        try {
            // Read data from assets.
            inputStream = new BufferedInputStream(assetManager.open(file));
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
            // Create copy file in storage.
            File outFile = new File(context.getFilesDir(), file);
            FileOutputStream os = new FileOutputStream(outFile);
            os.write(data);
            os.close();
            // Return a path to file which may be read in common way.
            return outFile.getAbsolutePath();
        } catch (IOException ex) {
            Log.i(TAG, "Failed to upload a file");
        }
        return "";
    }

    void FaceDetected(Mat matTmpProcessingFace){
        if (startFaces){
            Imgproc.cvtColor(matTmpProcessingFace, matTmpProcessingFace, Imgproc.COLOR_RGBA2RGB);
            Mat imageBlob = Dnn.blobFromImage(matTmpProcessingFace, 1.0, new Size(300, 300), new Scalar(104.0, 177.0, 123.0), true, false, CvType.CV_32F);
            detector.setInput(imageBlob); //set the input to network model
            Mat detections = detector.forward(); //feed forward the input to the netwrok to get the output
            int cols = matTmpProcessingFace.cols();
            int rows = matTmpProcessingFace.rows();
            double THRESHOLD = 0.55;
            detections = detections.reshape(1, (int)detections.total() / 7);
            Log.d("EXPERIMENT5:ROWS", detections.rows()+"");

            if(Face_det){
                Face_det = false;
                return;
            }

            for (int i = 0; i < detections.rows(); ++i) {
                double confidence = detections.get(i, 2)[0];
                Log.d("EXPERIMENT6", i+" "+confidence+" "+THRESHOLD +" "+Face_det);
                if (confidence > THRESHOLD) {
                    int left   = (int)(detections.get(i, 3)[0] * cols);
                    int top    = (int)(detections.get(i, 4)[0] * rows);
                    int right  = (int)(detections.get(i, 5)[0] * cols);
                    int bottom = (int)(detections.get(i, 6)[0] * rows);
                    if (left<0){
                        left=0;
                    }
                    if (top<0){
                        top=0;
                    }
                    if (right<0){
                        right=0;
                    }
                    if (bottom<0){
                        bottom=0;
                    }
                    Imgproc.rectangle(matTmpProcessingFace, new Point(left, top), new Point(right, bottom),new Scalar(255, 255, 0),2);
                    Face_det = true;
                    return;
                }
                else {
                    Face_det = false;
                    return;
                }
            }

        }
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        Mat matTmpProcessingFace = inputFrame.rgba();

        FaceDetected(matTmpProcessingFace);

        if(Face_det){
            Mirror_Active = true;
            Face_TimeOut = false;
            Face_time_start = 0;
        }
        else {
            if (!Face_TimeOut) {
                if (Face_time_start == 0) {
                    Face_time_start = Core.getTickCount();
                }
                Face_time_current = Core.getTickCount();
                double duration = (double) (Face_time_current - Face_time_start) / Core.getTickFrequency();
                Log.d("D", "Face duration " + duration);
                if (duration >= Face_time_thresholder) {
                    Face_TimeOut = true;
                    Face_time_start = 0;
                }
            }
        }
        if (Face_TimeOut) {
            Mirror_Active = false;
            Face_time_current = 0;
        }

        Log.d("D","Face Statue " + Face_det);
        Log.d("D","Face Timeout " + Face_TimeOut);
        Log.d("D","Mirror Statue " + Mirror_Active);

        return matTmpProcessingFace;
    }

    Boolean k = true;


    @Override
    public void run() {
        running = true;
        while (running) {
            try {

                if (Mirror_Active) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {


                            if (k) {


                                FragmentManager fragmentManager = getSupportFragmentManager();

                                FragmentTransaction transaction = fragmentManager.beginTransaction().setCustomAnimations(R.anim.fade_out, R.anim.fade_in);

                                transaction.replace(
                                        R.id.fullscreen_content,
                                        flutterFragment,
                                        TAG_FLUTTER_FRAGMENT
                                );

                                transaction.commit();

                                Log.d("D", "MirrorS On");

                                infoFaces.setText("Mirror Active");

                                k = false;
                            }
                        }
                    });
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (!k) {
                                FragmentManager fragmentManager = getSupportFragmentManager();

                                FragmentTransaction transaction = fragmentManager.beginTransaction().setCustomAnimations(R.anim.fade_out, R.anim.fade_in);


                                //transaction.remove(flutterFragment);

                                transaction.replace(
                                        R.id.fullscreen_content,
                                        new Fragment(),
                                        TAG_FLUTTER_FRAGMENT
                                );

                                transaction.commit();

                                Log.d("D", "MirrorS Off");
                                infoFaces.setText("Mirror Deactive");
                                k = true;
                            }
                        }
                    });
                }
                Thread.sleep(100);
            }
            catch(Throwable t) {
                try {
                    Thread.sleep(1000);
                } catch (Throwable tt) {
                }
            }
        }
    }
}