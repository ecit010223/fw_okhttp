package com.year17.fw_okhttp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GETActivity extends AppCompatActivity implements View.OnClickListener {
    private Context mContext;
    /** 同步GET请求 **/
    private Button btnSync;
    /** 异步GET请求 **/
    private Button btnAsync;
    /** 网络请求，通过Request.Builder辅助类来构建 **/
    private Request mRequest;
    /** 网络请求返回 **/
    private Response mResponse;
    /**
     * 请求客户端
     * OkHttpClient实现了Call.Factory接口，是Call的工厂类，Call负责发送执行请求和读取响应。
     */
    private OkHttpClient mOkHttpClient;
    /** 当前点击按钮 **/
    private View mCurrentClickView;

    public static void entry(Context from){
        Intent intent = new Intent(from, GETActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get);
        mContext = this;
        initView();
    }

    private void initView(){
        btnSync = (Button)findViewById(R.id.btn_sync);
        btnSync.setOnClickListener(this);
        btnAsync = (Button)findViewById(R.id.btn_async);
        btnAsync.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(requestPermission()){
            switch (view.getId()) {
                case R.id.btn_sync:
                    syncGET();
                    break;
                case R.id.btn_async:
                    asyncGET();
                    break;
            }
        }else{
            mCurrentClickView = view;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case Constants.INTERNET_REQUEST_CODE:
                if(grantResults.length>0&&grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    onClick(mCurrentClickView);
                }
                break;
        }
    }

    /** 申请网络访问权限 **/
    private boolean requestPermission(){
        boolean hasPermission = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.INTERNET)== PackageManager.PERMISSION_GRANTED;
        if(!hasPermission){
            ActivityCompat.requestPermissions(scanForActivity(mContext),new String[]{Manifest.permission.INTERNET},
                    Constants.INTERNET_REQUEST_CODE);
            ActivityCompat.shouldShowRequestPermissionRationale(scanForActivity(mContext),Manifest.permission.INTERNET);
        }else{
            return true;
        }
        return false;
    }

    /**
     * 同步Get请求
     * 同步GET的意思是一直等待http请求，直到返回了响应，在这之间会阻塞进程，所以通过get不能在Android的主线程中执行，否则会报错。
     */
    private void syncGET(){
        mOkHttpClient = new OkHttpClient();
        mRequest = new Request.Builder().url(Constants.URL).build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mResponse = mOkHttpClient.newCall(mRequest).execute();
                    if(!mResponse.isSuccessful()){
                        Log.d(Constants.TAG,"is not successful");
                        throw new IOException("Unexpected code "+mResponse);
                    }
                    Headers responseHeaders = mResponse.headers();
                    for(int i=0;i<responseHeaders.size();i++){
                        Log.d(Constants.TAG,i+":"+responseHeaders.value(i));
                    }
                    /**
                     * response.body()是ResponseBody类，代表响应体，可以通过responseBody.string()获得字符串的表达形式，
                     * 或responseBody.bytes()获得字节数组的表达形式, 这两种形式都会把文档加入到内存。
                     * 也可以通过responseBody.charStream()和responseBody.byteStream()返回流来处理。
                     */
                    final String responseBody = mResponse.body().string();
                    Log.d(Constants.TAG,"body:"+responseBody);
                    //在主线程中显示提示信息
                    new Handler(getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext,responseBody,Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 异步GET是指在另外的工作线程中执行http请求, 请求时不会阻塞当前的线程, 所以可以在Android主线程中使用。
     * 当响应可读时回调Callback接口，当响应头准备好后，就会调用Callback接口，所以读取响应体时可能会阻塞，
     * OkHttp现阶段不提供异步api来接收响应体。
     */
    private void asyncGET(){
        mOkHttpClient = new OkHttpClient();
        mRequest = new Request.Builder().url(Constants.URL).build();
        mOkHttpClient.newCall(mRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(Constants.TAG,"onFailure："+e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(!response.isSuccessful())
                    throw new IOException("Unexpected code " + response);
                Headers responseHeaders = response.headers();
                for(int i=0;i<responseHeaders.size();i++){
                    Log.d(Constants.TAG,i+":"+responseHeaders.value(i));
                }
                final String responseBody = mResponse.body().string();
                Log.d(Constants.TAG,responseBody);
                //在主线程中显示提示信息
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext,responseBody,Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private Activity scanForActivity(Context context){
        if(context == null)
            return null;
        else if(context instanceof Activity)
            return (Activity)context;
        else if(context instanceof ContextWrapper)
            return scanForActivity(((ContextWrapper)context).getBaseContext());
        return null;
    }
}
