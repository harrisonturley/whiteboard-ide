package com.project.harrisonturley.whiteboardide;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;

/**
 * Screen for taking picture of code
 */
public class PictureActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final String imageFileName = "/CapturedImage.jpg";

    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private TextureView.SurfaceTextureListener surfaceTextureListener;
    private CameraDevice.StateCallback stateCallback;

    private TextureView textureView;
    private FloatingActionButton floatingActionButton;
    private ImageView backButton;

    private CaptureRequest.Builder captureRequestBuilder;
    private CaptureRequest captureRequest;
    private CameraCaptureSession cameraCaptureSession;

    private int cameraFacing;
    private Size previewSize;
    private String cameraId;
    private String imagePath;

    /**
     * Sets up the required fields for the picture activity, and any listeners
     *
     * @param savedInstanceState activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET}, CAMERA_REQUEST_CODE);

        floatingActionButton = findViewById(R.id.capture_button);
        cameraManager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        cameraFacing = CameraCharacteristics.LENS_FACING_BACK;
        textureView = findViewById(R.id.texture_view);
        backButton = findViewById(R.id.camera_back_button);

        disableFab();
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PictureActivity.this, MainActivity.class);
                closeCamera();
                closeBackgroundThread();
                startActivity(intent);
            }
        });

        surfaceTextureListener = new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                setUpCamera();
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        };

        stateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(CameraDevice cameraDevice) {
                PictureActivity.this.cameraDevice = cameraDevice;
                createPreviewSession();
            }

            @Override
            public void onDisconnected(CameraDevice cameraDevice) {
                cameraDevice.close();
                PictureActivity.this.cameraDevice = null;
            }

            @Override
            public void onError(CameraDevice cameraDevice, int error) {
                cameraDevice.close();
                PictureActivity.this.cameraDevice = null;
            }
        };
    }

    /**
     * Sets up the camera and thread again when app resumes
     */
    @Override
    protected void onResume() {
        super.onResume();
        openBackgroundThread();
        if (textureView.isAvailable()) {
            setUpCamera();
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    /**
     * Stops the camera and background thread when the app is stopped
     */
    @Override
    protected void onStop() {
        super.onStop();
        closeCamera();
        closeBackgroundThread();
    }

    /**
     * Captures the current image, saves it, and moves to the SavePictureActivity
     *
     * @param v view for button selected
     */
    public void onClickCaptureImage(View v) {
        lock();
        FileOutputStream outputPhoto = null;
        try {
            outputPhoto = new FileOutputStream(createImageFile());
            textureView.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, outputPhoto);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            unlock();
            try {
                if (outputPhoto != null) {
                    outputPhoto.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(this, SavePictureActivity.class);
            intent.putExtra(getString(R.string.saved_image_path), imagePath);
            startActivity(intent);
        }
    }

    /**
     * Closes the camera session
     */
    private void closeCamera() {
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }

        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    /**
     * Closes the background thread
     */
    private void closeBackgroundThread() {
        if (backgroundHandler != null) {
            backgroundThread.quitSafely();
            backgroundThread = null;
            backgroundHandler = null;
        }
    }

    /**
     * Initializes a preview session of the camera
     */
    private void createPreviewSession() {
        try {
            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(previewSurface);

            cameraDevice.createCaptureSession(Collections.singletonList(previewSurface), new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            if (cameraDevice == null) {
                                return;
                            }

                            try {
                                captureRequest = captureRequestBuilder.build();
                                PictureActivity.this.cameraCaptureSession = cameraCaptureSession;
                                PictureActivity.this.cameraCaptureSession.setRepeatingRequest(captureRequest, null, backgroundHandler);
                                PictureActivity.this.enableFab();
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

                        }
                    }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets up the physical camera characteristics
     */
    private void setUpCamera() {
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == cameraFacing) {
                    StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    previewSize = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];
                    this.cameraId = cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the camera for use in preview and capture
     */
    private void openCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(cameraId, stateCallback, backgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens a background thread for the camera
     */
    private void openBackgroundThread() {
        backgroundThread = new HandlerThread("camera_background_thread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    /**
     * Takes an image
     */
    private void lock() {
        try {
            cameraCaptureSession.capture(captureRequestBuilder.build(),
                    null, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Resumes the preview session
     */
    private void unlock() {
        try {
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(),
                    null, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Enables the capture button
     */
    private void enableFab() {
        if (floatingActionButton == null) {
            return;
        }

        floatingActionButton.setEnabled(true);
        floatingActionButton.setAlpha(1.0f);
    }

    /**
     * Disables the capture button
     */
    private void disableFab() {
        if (floatingActionButton == null) {
            return;
        }

        floatingActionButton.setAlpha(0f);
        floatingActionButton.setEnabled(false);
    }

    /**
     * Saves the captured image as a file on the system
     *
     * @return file for the captured image
     * @throws IOException in case file creation/modification fails
     */
    private File createImageFile() throws IOException {
        File galleryFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), getResources().getString(R.string.app_name));
        if (!galleryFolder.exists()) {
            boolean makeGalleryFolder = galleryFolder.mkdirs();

            if (!makeGalleryFolder) {
                Log.e("CaptureFiles", "Failed to create a new directory");
            }
        }

        File imageFile = new File(galleryFolder.getPath(), imageFileName);
        imagePath = imageFile.getPath();

        if (imageFile.exists()) {
            imageFile.delete();
        }

        return imageFile;
    }
}
