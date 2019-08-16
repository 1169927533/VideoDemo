package com.hikvision.open.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_main);
        findViewById(R.id.preview_button).setOnClickListener(MainActivity.this);
        findViewById(R.id.playback_button).setOnClickListener(MainActivity.this);
        findViewById(R.id.voicetalk_button).setOnClickListener(MainActivity.this);
        requestPermissions();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.preview_button) {
            startActivity(new Intent(this, PreviewActivity.class));
        } else if (view.getId() == R.id.playback_button) {
            startActivity(new Intent(this, PlaybackActivity.class));
        } else if (view.getId() == R.id.voicetalk_button) {
            startActivity(new Intent(this, VoiceTalkActivity.class));
        }
    }


    /**
     * TODO：获取手机存储读写权限
     * <p>
     * TODO：语音对讲功能需要手机麦克风权限
     */
    private void requestPermissions() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, (new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}), 10);
        }
    }

}
