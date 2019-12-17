package com.example.myfirstapplicaion;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.victor.loading.book.BookLoading;

import java.io.IOException;

public class SurfaceCamera extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final String downloadPath = Environment.getExternalStorageDirectory() + "/download";
    private static final String recordVideoFileName = "recordVideoFile.mp4";
    private static final String videoPath = downloadPath + "/video";
    private static final String recordVideoPath = videoPath+ '/' + recordVideoFileName;
//    private static final String urlServer = "http://192.168.43.172:5000/"; //lun cellular data


    private MediaRecorder mMediaRecorder;
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private View recordButton;
    private TextView infoText;
    private boolean mInitSuccesful;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
        }
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // we shall take the video in landscape orientation
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//        ProgressDialog progress = new ProgressDialog(this);
//        progress.setMessage("Wait while loading...");

//        RotateLoading rotateLoading = findViewById(R.id.rotateloading);
//        rotateLoading.start();
        BookLoading bookLoading = findViewById(R.id.bookloading);
//        bookLoading.start();
//        bookLoading.stop();
        bookLoading.setVisibility(View.GONE);

        recordButton = findViewById(R.id.recordButton);
        recordButton.setVisibility(View.GONE);
//        recordButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            // toggle video recording
//            public void onClick(View v) {
//                //                if (recordButton.isChecked()) {
//                recordButton.setVisibility(View.GONE);
//                //                try {
//                //                    Thread.sleep(1 * 5); // This will recode for 10 seconds, if you don't want then just remove it.
//                //
//                //                } catch (Exception e) {
//                //                    e.printStackTrace();
//                //                }
//                mMediaRecorder.start();
//            }
//        });
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        shutdown();
    }

    /* Init the MediaRecorder, the order the methods are called is vital to
     * its correct functioning */
    private void initRecorder(Surface surface) throws IOException {
        // It is very important to unlock the camera before doing setCamera
        // or it will results in a black preview
        if(mCamera == null) {
            boolean found = false;
            int i;
            for (i=0; i< Camera.getNumberOfCameras(); i++) {
                Camera.CameraInfo newInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(i, newInfo);
                if (newInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    found = true;
                    break;
                }
            }
            mCamera = Camera.open(i);
        }
//        mCamera.setDisplayOrientation();

//        mCamera.setDisplayOrientation(180);

        if(mMediaRecorder == null)  mMediaRecorder = new MediaRecorder();
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
        } else {
            // In portrait

        }
        mMediaRecorder.setOrientationHint(270);
        mCamera.setDisplayOrientation(0);
        mMediaRecorder.setPreviewDisplay(surface);
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        //       mMediaRecorder.setOutputFormat(8);
//        mMediaRecorder.setMaxDuration(5000);
        mMediaRecorder.setMaxDuration(1000);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setVideoEncodingBitRate(512 * 1000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(640, 480);
//        mMediaRecorder.setVideoSize(1920, 1080);

        mMediaRecorder.setOutputFile(recordVideoPath);
        mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
//                    mr.stop();
////                    Log.e("Maximumd", "MaximsetDataSourceum Duration Reached");
//                    mr.release();
//                    mCamera.release();
                    shutdown();
//                    setContentView(R.layout.main);
//                    infoText = findViewById(R.id.textView);
//                    infoText.setText("Sending video file to server ");
//                    MainActivity.stopXMSecForDisplay(3000);
//                    startActivity(new Intent(SurfaceCamera.this,SendFile.class));
                    Intent resultIntent = new Intent();
//                    resultIntent.putExtra("Test", "newValue");
                    setResult(100, resultIntent);
                    finish();
                }
            }
        });
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            // This is thrown if the previous calls are not called with the
            // proper order
            e.printStackTrace();
        }

        mInitSuccesful = true;
//        mMediaRecorder.start(); // no use button start style

//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                // run AsyncTask here.
//                mMediaRecorder.start();// no use button start style
//            }
//        }, 1500);
        mCamera.unlock();
        mMediaRecorder.start();// no use button start style

//        try {
//            Thread.sleep(2250);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if(!mInitSuccesful)
                initRecorder(mHolder.getSurface());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    private void shutdown() {
        // Release MediaRecorder and especially the Camera as it's a shared
        // object that can be used by other applications
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mCamera.release();

        // once the objects have been released they can't be reused
        mMediaRecorder = null;
        mCamera = null;
    }
}
