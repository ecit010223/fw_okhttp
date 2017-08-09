package com.year17.fw_okhttp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnGET;
    private Button btnPOST;
    private Button btnOther;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView(){
        btnGET = (Button)findViewById(R.id.btn_to_get);
        btnGET.setOnClickListener(this);
        btnPOST = (Button)findViewById(R.id.btn_to_post);
        btnPOST.setOnClickListener(this);
        btnOther = (Button)findViewById(R.id.btn_to_other);
        btnOther.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_to_get:
                GETActivity.entry(MainActivity.this);
                break;
            case R.id.btn_to_post:
                POSTActivity.entry(MainActivity.this);
                break;
            case R.id.btn_to_other:
                OtherActivity.entry(MainActivity.this);
                break;
        }
    }
}
