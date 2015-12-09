package com.esu.halexkimm.datacaptureapp.fragment;

import android.app.Fragment;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.esu.halexkimm.datacaptureapp.R;

/**
 * Created by halexkimm on 12/8/15.
 */
public class GPSFragment extends Fragment{

    private Button _gpsButton;

    private LocationManager _locationManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View _fragmentView = inflater.inflate(R.layout.gps_fragment, container, false);

        return _fragmentView;
    }

}
