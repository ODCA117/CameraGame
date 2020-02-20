package com.example.cameraapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

// =================== NOT USED AT THE MOMENT ================================= //
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void StartCameraActivity(View view) {
        Intent intent = new Intent(this, VideoActivity.class);
        this.startActivity(intent);

    }
}
