package com.example.textscanner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    TextView scan_data;
    Button capture_btn,copy_btn;
    private static final int REQUEST_CAMARA_CODE =100;
    Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scan_data = findViewById(R.id.scan_data);
        capture_btn = findViewById(R.id.capture_btn);
        copy_btn = findViewById(R.id.copy_btn);

        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.CAMERA) !=
        PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.CAMERA
            },REQUEST_CAMARA_CODE);
        }
        capture_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(MainActivity.this);

            }
        });
        copy_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("key", scan_data.getText().toString());
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(MainActivity.this, "Copied", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                Uri resultUri = result.getUri();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),resultUri);
                    getTextFromImage(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void getTextFromImage(Bitmap bitmap){
        TextRecognizer recognizer = new TextRecognizer.Builder(this).build();
        if(!recognizer.isOperational()){
            Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
        }else{
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> textBlockSparseArray = recognizer.detect(frame);
            StringBuffer stringBuffer = new StringBuffer();

            for(int i=0;i<textBlockSparseArray.size();i++){
                TextBlock textBlock = textBlockSparseArray.valueAt(i);
                stringBuffer.append(textBlock.getValue());
                stringBuffer.append("\n");
            }
            scan_data.setText(stringBuffer.toString());
            if(scan_data.getText().toString().isEmpty()){
                scan_data.setText("No Text Found");
            }
        }
    }
}