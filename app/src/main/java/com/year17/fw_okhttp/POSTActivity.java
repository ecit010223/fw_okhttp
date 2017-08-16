package com.year17.fw_okhttp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
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
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class POSTActivity extends AppCompatActivity implements View.OnClickListener {
    private Context mContext;
    /** POST提交Markdown格式文本 **/
    private Button btnPostMarkdown;
    /** POST提交json **/
    private Button btnPostJson;
    /** POST提交流 **/
    private Button btnPostStream;
    /** POST提交文件 **/
    private Button btnPostFile;
    /** POSt提交表单 **/
    private Button btnPostForm;
    /** POST提交分块请求 **/
    private Button btnPostBlocks;
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
        Intent intent = new Intent(from,POSTActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        mContext = this;
        initView();
    }

    private void initView(){
        btnPostMarkdown = (Button)findViewById(R.id.btn_post_markdown);
        btnPostMarkdown.setOnClickListener(this);
        btnPostJson = (Button)findViewById(R.id.btn_post_json);
        btnPostJson.setOnClickListener(this);
        btnPostStream = (Button)findViewById(R.id.btn_post_stream);
        btnPostStream.setOnClickListener(this);
        btnPostFile = (Button)findViewById(R.id.btn_post_file);
        btnPostFile.setOnClickListener(this);
        btnPostForm = (Button)findViewById(R.id.btn_post_form);
        btnPostForm.setOnClickListener(this);
        btnPostBlocks = (Button)findViewById(R.id.btn_post_blocks);
        btnPostBlocks.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(requestPermission()){
            switch (view.getId()){
                case R.id.btn_post_markdown:
                    postMarkdown();
                    break;
                case R.id.btn_post_json:
                    postJson();
                    break;
                case R.id.btn_post_stream:
                    postStream();
                    break;
                case R.id.btn_post_file:
                    postFile();
                    break;
                case R.id.btn_post_form:
                    postForm();
                    break;
                case R.id.btn_post_blocks:
                    postBlocks();
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
        }
        return hasPermission;
    }

    /**
     * 使用HTTP POST提交一个markdown文档到web服务,以HTML方式渲染markdown。
     * 因为整个请求体都在内存中,因此避免使用此api提交大文档（大于1MB）。
     */
    private void postMarkdown(){
        mOkHttpClient = new OkHttpClient();
        String postBody = ""
                + "Release\n"
                + "-------\n"
                + "\n"
                + " * _1.0_ May 6,2013\n"
                + " * _1.1_ June 15,2013\n"
                + " * _1.2_ August 11,2013\n";
        mRequest = new Request.Builder()
                .url(Constants.URL_MARKDOWN)
                .post(RequestBody.create(Constants.MEDIA_TYPE_MARKDOWN,postBody))
                .build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mResponse = mOkHttpClient.newCall(mRequest).execute();
                    if(!mResponse.isSuccessful()){
                        Log.d(Constants.TAG,"fail:"+mResponse);
                        throw new IOException("Unexpected code " + mResponse);
                    }
                    Log.d(Constants.TAG,mResponse.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /** post提交json **/
    private void postJson(){
        String jsonData = "haha";
        mOkHttpClient = new OkHttpClient();

        RequestBody requestBody = RequestBody.create(Constants.MEDIA_TYPE_JSON,jsonData);
        mRequest = new Request.Builder()
                .url(Constants.URL_POST_JSON)
                .post(requestBody)
                .build();
        mOkHttpClient.newCall(mRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(Constants.TAG,e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(Constants.TAG,response.body().string());
            }
        });
    }

    /**
     * 以流的方式POST提交请求体,请求体的内容由流写入产生,这个例子是流直接写入Okio的BufferedSink.
     * 你的程序可能会使用OutputStream,可以使用BufferedSink.outputStream()来获取.
     * OkHttp的底层对流和字节的操作都是基于Okio库,Okio库也是Square开发的另一个IO库,填补I/O和NIO的空缺,
     * 目的是提供简单便于使用的接口来操作IO.
     */
    private void postStream(){
        mOkHttpClient = new OkHttpClient();
        RequestBody requestBody = new RequestBody() {
            @Nullable
            @Override
            public MediaType contentType() {
                return Constants.MEDIA_TYPE_MARKDOWN;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                sink.writeUtf8("Numbers\n");
                sink.writeUtf8("-------\n");
                for(int i=2;i<997;i++){
                    sink.writeUtf8(String.format("* %s = %s\n",i,factor(i)));
                }
            }

            private String factor(int n){
                for(int i=2;i<n;i++){
                    int x = n/i;
                    if(x*i==n)
                        return factor(x) + "x" +i;
                }
                return Integer.toString(n);
            }
        };
        mRequest = new Request.Builder()
                .url(Constants.URL_MARKDOWN)
                .post(requestBody)
                .build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mResponse = mOkHttpClient.newCall(mRequest).execute();
                    if(!mResponse.isSuccessful()){
                        Log.d(Constants.TAG,"fail:"+mResponse);
                        throw  new IOException("Unexpected code " + mResponse);
                    }
                    Log.d(Constants.TAG,mResponse.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /** post提交文件 **/
    private void postFile(){
        mOkHttpClient = new OkHttpClient();
        File file = new File("README.md");
        mRequest = new Request.Builder()
                .url(Constants.URL_MARKDOWN)
                .post(RequestBody.create(Constants.MEDIA_TYPE_MARKDOWN,file))
                .build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mResponse = mOkHttpClient.newCall(mRequest).execute();
                    if(!mResponse.isSuccessful()){
                        Log.d(Constants.TAG,"fail:"+mResponse);
                        throw new IOException("Unexpected code " + mResponse);
                    }
                    Log.d(Constants.TAG,mResponse.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * post提交表单
     * 使用FormEncodingBuilder来构建和HTML<form>标签相同效果的请求体,键值对将使用一种HTML兼容形式的URL编码来进行编码.
     */
    private void postForm(){
        mOkHttpClient = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("search","Jurassic park")
                .build();
        mRequest = new Request.Builder()
                .url(Constants.URL_FORM)
                .post(formBody)
                .build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mResponse = mOkHttpClient.newCall(mRequest).execute();
                    if(!mResponse.isSuccessful()){
                        Log.d(Constants.TAG,"fail:"+mResponse);
                        throw new IOException("Unexpected code " + mResponse);
                    }
                    Log.d(Constants.TAG,mResponse.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * post提交分块请求
     * MultipartBody.Builder可以构建复杂的请求体,与HTML文件上传形式兼容.多块请求体中每块请求都是一个请求体,
     * 可以定义自己的请求头,这些请求头可以用来描述这块请求,例如它的Content-Disposition.如果Content-Length
     * 和Content-Type可用的话,他们会被自动添加到请求头中.
     */
    private void postBlocks(){
        mOkHttpClient = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("title","Square Logo")
                .addFormDataPart("image","logo-square.png",RequestBody.create(Constants.MEDIA_TYPE_PNG,
                        new File("website/static/logo-square.png")))
                .build();
        mRequest = new Request.Builder()
                .header("Authorization","Client-ID " + Constants.IMGUR_CLIENT_ID)
                .url(Constants.URL_BLOCKS)
                .post(requestBody)
                .build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mResponse = mOkHttpClient.newCall(mRequest).execute();
                    if(!mResponse.isSuccessful()){
                        Log.d(Constants.TAG,"fail:"+mResponse);
                        throw new IOException("Unexpected code " + mResponse);
                    }
                    Log.d(Constants.TAG,mResponse.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
