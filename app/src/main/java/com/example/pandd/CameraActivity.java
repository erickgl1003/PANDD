package com.example.pandd;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    ImageCapture imageCapture;
    File outputDirectory;
    ExecutorService cameraExecutor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_fragment);

        // Check for permissions
        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            String permissionsArray[] = {Manifest.permission.CAMERA};
            ActivityCompat.requestPermissions(
                    this, permissionsArray, 10);
        }

        // set up button for image capture
        Button camera_capture_button = findViewById(R.id.camera_capture_button);
        camera_capture_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });

        // setting up the output directory
        outputDirectory = getOutputDirectory();
        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void takePhoto() {
        if(imageCapture == null){
            return;
        }

        // set up the photo file for storing the photo
        File photoFile = new File(outputDirectory, new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg");

        // save the image and wait for callback.
        ImageCapture.OutputFileOptions fileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();
        imageCapture.takePicture(fileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Toast.makeText(getBaseContext(), "Photo capture succeeded", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.putExtra("photo", photoFile.getPath());
                setResult(-1, intent);
                finish();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.i("Image Capture", exception.toString());
            }
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> processCameraProvider = ProcessCameraProvider.getInstance(this);

        processCameraProvider.addListener(new Runnable() {
            @Override
            public void run() {
                ProcessCameraProvider cameraProvider = null;
                try {
                    cameraProvider = processCameraProvider.get();
                } catch (Exception e) {
                    Toast.makeText(CameraActivity.this, e.getMessage(),Toast.LENGTH_LONG).show();
                }

                // Set up preview window
                Preview preview = new Preview.Builder().build();
                PreviewView viewFinder = findViewById(R.id.viewFinder);
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .build();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Tie the preview, camera selector and imageCapture together via the cameraProvider
                try{
                    cameraProvider.unbindAll();
                    cameraProvider.bindToLifecycle(CameraActivity.this, cameraSelector, preview, imageCapture);
                }catch (Exception e){
                    Toast.makeText(CameraActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    //Gets the directory in which the taken image will be stored in.
    private File getOutputDirectory() {
        File mediaDirs = getExternalMediaDirs()[0];
        File newFiles = null;

        if (mediaDirs != null) {
            newFiles = new File(mediaDirs, getResources().getString(R.string.app_name));
            newFiles.mkdirs();
        }
        if (newFiles != null && mediaDirs.exists()) {
            return mediaDirs;
        } else
            return getFilesDir();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10) {
            if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not grated by the user", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}