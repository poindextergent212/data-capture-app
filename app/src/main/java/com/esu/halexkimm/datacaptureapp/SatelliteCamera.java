package com.esu.halexkimm.datacaptureapp;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class SatelliteCamera extends Thread implements SurfaceHolder.Callback {
    private Camera camera;
    private SurfaceView sView;
    private String cameraType;
    private Context cameraContext;
    private SurfaceHolder holder;
    private PictureCallback jpegCallback;

    public SatelliteCamera(String type, SurfaceView surface, Context context) {
        cameraType = type;
        sView = surface;
        cameraContext = context;
        holder = sView.getHolder();
        holder.addCallback(this);
        jpegCallback = new PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
                FileOutputStream outStream = null;
                try {
                    outStream = new FileOutputStream(String.format("/sdcard/%d.jpg", System.currentTimeMillis()));
                    outStream.write(data);
                    outStream.close();
                    Log.d("Log", "onPictureTaken - wrote bytes: " + data.length);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("Log", "onPictureTaken - wrote bytes: " + data.length);
                refreshCamera();
            }

        };
    }

    public void refreshCamera() {
        if (holder.getSurface() == null) {
            return;
        }
        try {
            camera.stopPreview();
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            System.out.println("Started Camera Again");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            if (this.cameraType != null) {

                if (this.cameraType.equals("back")) {
                    camera = Camera.open();
                }
                if (this.cameraType.equals("front")) {
                    camera = Camera.open(1);
                }
            }
            else {
                System.err.println("The camera type was not specified");
            }
        } catch (RuntimeException e) {
            System.err.println(e);
            System.err.println("sldigejrkgjwr");
            Toast.makeText(cameraContext, "Camera couldn't open", Toast.LENGTH_LONG).show();
            return;
        }
        Camera.Parameters param;
        param = camera.getParameters();
        camera.setParameters(param);
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            System.err.println(e);
            return;
        }
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        //camera.stopPreview();
        //camera.release();
        camera = null;
    }

    public void captureImage() throws IOException {
        surfaceCreated(holder);
        camera.takePicture(null, null, jpegCallback);
    }

    /**@Override
    public void run() {
    for(int i = 0; i < 5; i++) {
    try {
    surfaceCreated(holder);
    captureImage(null);
    System.out.println("HANKE");
    Thread.sleep(5000);
    } catch (Exception e) {
    e.printStackTrace();
    }
    }
    }**/
}