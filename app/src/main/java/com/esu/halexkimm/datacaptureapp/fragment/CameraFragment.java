package com.esu.halexkimm.datacaptureapp.fragment;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.widget.Button;


import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import android.os.Bundle;

import com.esu.halexkimm.datacaptureapp.R;
/**
 * Created by halexkimm on 12/8/15.
 */
@TargetApi(21)
public class CameraFragment extends Fragment {

    private Button _cameraButton;

    private CameraManager _cameraManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View _fragmentView = inflater.inflate(R.layout.camera_fragment, container, false);

        _cameraButton = (Button) _fragmentView.findViewById(R.id.cameraButton);
        _cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_cameraManager == null){
                    takePhoto();
                }
            }
        });

        return _fragmentView;
    }

    private void takePhoto() {
        _cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        String[] cameraArray = null;
        try {
            cameraArray = _cameraManager.getCameraIdList();

        } catch (CameraAccessException ex) {
            System.out.println("Could not access camera for some reason");
            ex.getStackTrace();
        }
    }
    /*private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }

    };*/
}
