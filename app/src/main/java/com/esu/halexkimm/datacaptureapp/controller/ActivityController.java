package com.esu.halexkimm.datacaptureapp.controller;

import android.app.Activity;
import android.app.FragmentTransaction;


import com.esu.halexkimm.datacaptureapp.fragment.*;
import com.esu.halexkimm.datacaptureapp.R;

public class ActivityController {

    private static Activity _activity = null;

    private CameraFragment2 _cameraFragment;
    private GPSFragment _gpsFragment;

    public ActivityController(Activity activity){
        _activity = activity;
        //_gpsButton = (Button) _activity.findViewById(R.id.gpsButton);

        setUI();
    }

    private void setUI(){
        _cameraFragment = new CameraFragment2();
        _gpsFragment = new GPSFragment();

        FragmentTransaction transaction = _activity.getFragmentManager().beginTransaction();

        transaction.add(R.id.camera_fragment, _cameraFragment);
        transaction.add(R.id.gps_fragment, _gpsFragment);

        transaction.commit();
    }
}
