package com.esu.halexkimm.datacaptureapp.controller;

import android.app.Activity;
import android.app.FragmentTransaction;


import com.esu.halexkimm.datacaptureapp.fragment.*;
import com.esu.halexkimm.datacaptureapp.R;

public class ActivityController {

    private static Activity _activity = null;

    private CameraFragment _cameraFragment;
    private GPSFragment _gpsFragment;

    public ActivityController(Activity activity){
        _activity = activity;
        //_gpsButton = (Button) _activity.findViewById(R.id.gpsButton);

        setUI();
    }

    private void setUI(){

        /*_gpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_locationManager == null){
                    //getLocation();
                }
            }
        });*/

        _cameraFragment = new CameraFragment();
        _gpsFragment = new GPSFragment();

        FragmentTransaction transaction = _activity.getFragmentManager().beginTransaction();

        transaction.add(R.id.camera_fragment, _cameraFragment);
        transaction.add(R.id.gps_fragment, _gpsFragment);

        transaction.commit();
    }

/*    Button startButton;
    TextView sampleText;
    SurfaceView sViewBack;
    SurfaceView sViewFront;
    Context context;
    SatelliteCamera cameraLauncherBack;
    SatelliteCamera cameraLauncherFront;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        startButton = (Button)findViewById(R.id.startButton);
        sampleText = (TextView)findViewById(R.id.sampleText);
        sViewBack = (SurfaceView)findViewById(R.id.surfaceViewBack);
        sViewFront = (SurfaceView)findViewById(R.id.surfaceViewFront);

        context = getApplicationContext();

        File s = Environment.getExternalStorageDirectory();
        //sampleText.setText(s.getPath());
        String a = Integer.toString(Camera.getNumberOfCameras());
        sampleText.setText(a);
        startButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v){
                try {
                    cameraLauncherBack = new SatelliteCamera("back", sViewBack, context);
                    //cameraLauncherBack.surfaceCreated(null);
                    cameraLauncherBack.captureImage();
                    //cameraLauncherBack.start();
                    //cameraLauncherFront = new CameraLauncher("front", sViewFront, context);
                    //cameraLauncherFront.surfaceCreated(null);
                    //cameraLauncherFront.captureImage(null);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraLauncherBack.surfaceDestroyed(null);
    }*/
}
