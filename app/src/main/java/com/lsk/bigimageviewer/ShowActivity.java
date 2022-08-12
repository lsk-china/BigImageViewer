package com.lsk.bigimageviewer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

public class ShowActivity extends AppCompatActivity {

    private SubsamplingScaleImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        this.imageView = findViewById(R.id.imageView);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String filename = bundle.getString("filename");
        String storagePath = bundle.getString("storagePath");
        Log.i("BigImageViewer:show", filename+storagePath);
        imageView.setImage(ImageSource.uri(filename + storagePath));
    }
}