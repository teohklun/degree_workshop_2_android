package com.example.myfirstapplicaion;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.IOException;

public class CustomVideoCamera extends Activity implements SurfaceHolder.Callback{

    private static final String TAG = "CAMERA_TUTORIAL";

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private boolean previewRunning;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_surface);
        surfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open();
        if (camera != null){
            Camera.Parameters params = camera.getParameters();
            camera.setParameters(params);
        }
        else {
            Toast.makeText(getApplicationContext(), "Camera not available!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (previewRunning){
            camera.stopPreview();
        }
        Camera.Parameters p = camera.getParameters();
        p.setPreviewSize(width, height);
        p.setPreviewFormat(PixelFormat.JPEG);
        camera.setParameters(p);

        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            previewRunning = true;
        }
        catch (IOException e) {
            Log.e(TAG,e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        previewRunning = false;
        camera.release();
    }
}