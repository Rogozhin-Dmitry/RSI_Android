package de.dmitryRogozhin;

import static android.graphics.ImageDecoder.decodeBitmap;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.TensorImage;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import de.dmitryRogozhin.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ActivityResultLauncher<PickVisualMediaRequest> pickVisualLauncher;
    private ImageCapture imageCapture;

    private Camera camera;

    private boolean lighton = true;


    private SignInfoActivity act;

    private Interpreter interpreter;

    private final String TAG = "MainActivity";


    private void analyzePic(Uri uri) {
        try {
            ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
            Bitmap bitmap = decodeBitmap(source);
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            int height = bitmap.getHeight();
            int weight = bitmap.getWidth();
            int delta;
            if (weight < height) {
                delta = (height - weight) / 2;
            } else {
                delta = (weight - height) / 2;
            }
            bitmap = Bitmap.createBitmap(bitmap, 0, delta, weight, height - delta);
            bitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, false);
            TensorImage image = TensorImage.fromBitmap(bitmap);
            float[][] numOf = new float[1][6];
            interpreter.run(image.getBuffer(), numOf);
            float m = numOf[0][1];
            int num = 0;
            for (int i = 0; i < numOf[0].length; i++) {
                if (m < numOf[0][i]) {
                    m = numOf[0][i];
                    num = i;
                }
            }
            this.onResult(num);
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        openCamera();
        registerActivityForPickImage();

        binding.flash.setOnClickListener(v -> this.turnLight());

        binding.takePicture.setOnClickListener(v -> {
            String name = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(System.currentTimeMillis());

            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CNN-Images");

            ImageCapture.OutputFileOptions outputOptions =
                    new ImageCapture.OutputFileOptions.Builder(getContentResolver(),
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).build();

            imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            analyzePic(outputFileResults.getSavedUri());
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            String text = "Error: " + exception.getMessage();
                            Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, text);
                        }
                    });
        });


        binding.galleryView.setOnClickListener(v -> pickVisualLauncher.launch(
                new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));
        this.act = new SignInfoActivity();

        ByteBuffer tFlitEModel = loadModelFile(getAssets());
        assert tFlitEModel != null;
        interpreter = new Interpreter(tFlitEModel);
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager) {
        try {
            AssetFileDescriptor fileDescriptor = assetManager.openFd("rsi.tflite");
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();

            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        } catch (Exception e) {
            Log.e("ImageAnalyze", "Error : " + e.getMessage());
        }
        return null;
    }

    private void turnLight() {
        PackageManager pm = getApplicationContext().getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            if (lighton) {
                camera.getCameraControl().enableTorch(true);
                lighton = false;
            } else {
                camera.getCameraControl().enableTorch(false);
                lighton = true;
            }
        } else {
            Toast.makeText(getBaseContext(), "На вашем устройстве нет фонарика", Toast.LENGTH_SHORT).show();
        }
    }


    private void registerActivityForPickImage() {
        pickVisualLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                this.analyzePic(uri);
            }
        });
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityResultLauncher<String[]> launcher = registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(), result -> result.forEach((permission, res) -> {
                        if (permission.equals(Manifest.permission.CAMERA)) {
                            openCamera();
                        }
                    }));
            launcher.launch(new String[]{Manifest.permission.CAMERA});
        } else {
            bindPreview();
        }
    }

    private void bindPreview() {
        Preview preview = new Preview.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        imageCapture = new ImageCapture.Builder().build();
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                preview.setSurfaceProvider(binding.cameraView.getSurfaceProvider());

                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    public void onResult(int signClass) {
        Intent i = new Intent(this, act.getClass());
        i.putExtra("signClass", signClass);
        startActivity(i);
    }
}