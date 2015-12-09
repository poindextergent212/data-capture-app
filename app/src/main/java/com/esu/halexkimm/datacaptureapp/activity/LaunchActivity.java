package com.esu.halexkimm.datacaptureapp.activity;

import com.esu.halexkimm.datacaptureapp.controller.ActivityController;
import com.esu.halexkimm.datacaptureapp.R;

import android.app.Activity;
import android.os.Bundle;

public class LaunchActivity extends Activity {

    private ActivityController _activityController = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher_1);

        _activityController = new ActivityController(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        //_activityController.stopLocation();
        super.onPause();
    }

    @Override
    protected void onStop(){
        //_activityController.stopLocation();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*if(_activityController != null){
            _activityController.startLocation();
        }*/
    }
}