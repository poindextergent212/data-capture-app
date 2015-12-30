package com.esu.halexkimm.datacaptureapp.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.esu.halexkimm.datacaptureapp.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by halexkimm on 12/29/15.
 */
@SuppressLint("NewApi")
public class CameraFragment2 extends Fragment implements View.OnClickListener {

    //ORIENTATIONS//
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    //TEXTURES//
    private TextureView dcTextureViewBack;
    private TextureView dcTextureViewFront;

    //CAMERA SET-UP//
    private Size dcPreviewSize;
    private String dcCameraId;
    private CameraDevice dcCameraDevice;
    private CameraDevice.StateCallback dcCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            dcCameraDevice = camera;
            createCameraPreviewSession();
            Toast.makeText(getActivity().getApplicationContext(), "camera opened", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            dcCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            dcCameraDevice = null;
        }
    };

    private final TextureView.SurfaceTextureListener dcSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            setUpCamera(width, height);
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };

    private CaptureRequest dcPreviewCaptureRequest;
    private CaptureRequest.Builder dcPreviewCaptureRequestBuilder;
    private CameraCaptureSession dcCameraCaptureSession;
    private CameraCaptureSession.CaptureCallback dcCamerCaptureSessionCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }
    };

    //Background THREADS//
    private HandlerThread dcBackgroundThread;
    private Handler dcBackgroundHandler;

    //FOCUSING//
    private int dcLockState = STATE_PREVIEW;

    private static final int STATE_PREVIEW = 0;

    /**
     * Camera state: Waiting for the focus to be locked.
     */
    private static final int STATE_WAITING_LOCK = 1;

    /**
     * Camera state: Waiting for the exposure to be precapture state.
     */
    private static final int STATE_WAITING_PRECAPTURE = 2;

    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.
     */
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;

    /**
     * Camera state: Picture was taken.
     */
    private static final int STATE_PICTURE_TAKEN = 4;


    private CameraCaptureSession.CaptureCallback mCaptureCallback= new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            switch (dcLockState) {
                case STATE_PREVIEW: {
                    // We have nothing to do when the camera preview is working normally.
                    break;
                }
                case STATE_WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            dcLockState = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE || aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        dcLockState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        dcLockState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            process(result);
        }
    };

    //Image Saving//
    private File dcImageFile;
    private ImageReader dcImageReader;

    private final ImageReader.OnImageAvailableListener dcOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            dcBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), dcImageFile));
        }

    };

    ////////////////////////////////////////////////////////////////////////
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    ///////////////////////////////////////////////////////////////////////

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dcImageFile = new File(getActivity().getExternalFilesDir(null), "pic.jpg");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //view.findViewById(R.id.info).setOnClickListener(this);
        View v = inflater.inflate(R.layout.camera_fragment, container, false);
        v.findViewById(R.id.cameraButton).setOnClickListener(this);

        dcTextureViewBack = (TextureView) v.findViewById(R.id.textureViewBack);
        dcTextureViewFront = (TextureView) v.findViewById(R.id.textureViewFront);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        openBackgroundThread();
        if (dcTextureViewBack.isAvailable()) {
            setUpCamera(dcTextureViewBack.getWidth(), dcTextureViewBack.getHeight());
            openCamera();
        } else {
            dcTextureViewBack.setSurfaceTextureListener(dcSurfaceTextureListener);
            //dcTextureViewFront.setSurfaceTextureListener(dcSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        closeBackgroundThread();
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        takePicture();
    }

    private static class ImageSaver implements Runnable {

        private final Image dcImage;
        private final File dcFile;

        public ImageSaver(Image image, File file) {
            dcImage = image;
            dcFile = file;
        }

        @Override
        public void run() {
            ByteBuffer buffer = dcImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(dcFile);
                output.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                dcImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    private void takePicture() {
        lockFocus();
    }

    private void lockFocus() {
            try {
                // This is how to tell the camera to lock focus.
                dcPreviewCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
                // Tell #mCaptureCallback to wait for the lock.
                dcLockState = STATE_WAITING_LOCK;
                dcCameraCaptureSession.capture(dcPreviewCaptureRequestBuilder.build(), mCaptureCallback, dcBackgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
    }

    private void setUpCamera(int width, int height) {
        CameraManager dcCameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);

        try {
            /*for(String cameraId : dcCameraManager.getCameraIdList()) {
                CameraCharacteristics dcCameraCharacteristics = dcCameraManager.getCameraCharacteristics(cameraId);

            }*/
            CameraCharacteristics dcCameraCharacteristics = dcCameraManager.getCameraCharacteristics("0");
            StreamConfigurationMap map = dcCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            Size largest = Collections.max(
                    Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                    new CompareSizesByArea());
            dcImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),ImageFormat.JPEG, /*maxImages*/2);
            dcImageReader.setOnImageAvailableListener(dcOnImageAvailableListener, dcBackgroundHandler);

            dcPreviewSize = getPreferredPreviewSize(map.getOutputSizes(SurfaceTexture.class), width, height);
            dcCameraId = "0";
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Size getPreferredPreviewSize(Size[] sizeArray, int width, int height){
        List<Size> collectorSizes = new ArrayList<Size>();
        for(Size option : sizeArray) {
            if(width > height) {
                if(option.getWidth() > width && option.getHeight() > height) {
                    collectorSizes.add(option);
                }
            } else if(option.getWidth() > width && option.getHeight() > height) {
                    collectorSizes.add(option);
            }
        }
        if(!collectorSizes.isEmpty()) {
            return Collections.min(collectorSizes, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight());
                }
            });
        }
        return sizeArray[0];
    }

    private void openCamera() {
        CameraManager dcCameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            dcCameraManager.openCamera(dcCameraId, dcCameraDeviceStateCallback, dcBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if(dcCameraCaptureSession != null) {
            dcCameraCaptureSession.close();
            dcCameraCaptureSession = null;
        }
        if(dcCameraDevice != null) {
            dcCameraDevice.close();
            dcCameraDevice = null;
        }
        if(dcImageReader != null) {
            dcImageReader.close();
            dcImageReader = null;
        }
    }

    private void createCameraPreviewSession() {
        try {
            SurfaceTexture surfaceTexture = dcTextureViewBack.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(dcPreviewSize.getWidth(), dcPreviewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            dcPreviewCaptureRequestBuilder = dcCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            dcPreviewCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            dcPreviewCaptureRequestBuilder.addTarget(previewSurface);
            dcCameraDevice.createCaptureSession(Arrays.asList(previewSurface, dcImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            if (dcCameraDevice != null) {
                                try {
                                    dcPreviewCaptureRequest = dcPreviewCaptureRequestBuilder.build();
                                    dcCameraCaptureSession = cameraCaptureSession;
                                    dcCameraCaptureSession.setRepeatingRequest(dcPreviewCaptureRequest, dcCamerCaptureSessionCallback, dcBackgroundHandler);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                            Toast.makeText(getActivity().getApplicationContext(), "create camera session failed", Toast.LENGTH_SHORT).show();
                        }
                    }, null);
        } catch(CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            dcPreviewCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            dcLockState = STATE_WAITING_PRECAPTURE;
            dcCameraCaptureSession.capture(dcPreviewCaptureRequestBuilder.build(), mCaptureCallback,
                    dcBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void captureStillPicture() {
        try {
            final Activity activity = getActivity();
            if (null == activity || null == dcCameraDevice) {
                return;
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder = dcCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(dcImageReader.getSurface());

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

            // Orientation
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    //showToast("Saved: " + mFile);
                    //Log.d(TAG, mFile.toString());
                    unlockFocus();
                }
            };

            dcCameraCaptureSession.stopRepeating();
            dcCameraCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            dcPreviewCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            dcPreviewCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            dcCameraCaptureSession.capture(dcPreviewCaptureRequestBuilder.build(), mCaptureCallback, dcBackgroundHandler);
            // After this, the camera will go back to the normal state of preview.
            dcLockState = STATE_PREVIEW;
            dcCameraCaptureSession.setRepeatingRequest(dcPreviewCaptureRequest, mCaptureCallback, dcBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openBackgroundThread() {
        dcBackgroundThread = new HandlerThread("CameraBackground");
        dcBackgroundThread.start();
        dcBackgroundHandler = new Handler(dcBackgroundThread.getLooper());
    }

    private void closeBackgroundThread() {
        dcBackgroundThread.quitSafely();
        try {
            dcBackgroundThread.join();
            dcBackgroundThread = null;
            dcBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
