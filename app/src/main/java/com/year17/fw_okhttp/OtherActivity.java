package com.year17.fw_okhttp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;
import com.year17.fw_okhttp.interceptors.CacheInterceptor;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OtherActivity extends AppCompatActivity implements View.OnClickListener {
    private Context mContext;
    private Button btnHeader;
    private Button btnGSON;
    private Button btnCache;
    private Button btnCacheInterceptor;
    private Button btnCancelCall;
    private Button btnSetCall;
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
    /** 线程池 **/
    private ScheduledExecutorService mScheduledExecutorService;

    public static void entry(Context from){
        Intent intent = new Intent(from,OtherActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
        mContext = this;
        mScheduledExecutorService = Executors.newScheduledThreadPool(1);
        initView();
    }

    private void initView(){
        btnHeader = (Button)findViewById(R.id.btn_header);
        btnHeader.setOnClickListener(this);
        btnGSON = (Button)findViewById(R.id.btn_GSON);
        btnGSON.setOnClickListener(this);
        btnCache = (Button)findViewById(R.id.btn_cache);
        btnCache.setOnClickListener(this);
        btnCacheInterceptor = (Button)findViewById(R.id.btn_cache_interceptor);
        btnCacheInterceptor.setOnClickListener(this);
        btnCancelCall = (Button)findViewById(R.id.btn_cancel_call);
        btnCancelCall.setOnClickListener(this);
        btnSetCall = (Button)findViewById(R.id.btn_set_call);
        btnSetCall.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case Constants.INTERNET_REQUEST_CODE:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
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

    public Activity scanForActivity(Context context){
        if(context == null){
            return null;
        }else if(context instanceof Activity){
            return (Activity)context;
        }else if(context instanceof ContextWrapper){
            return scanForActivity(((ContextWrapper)context).getBaseContext());
        }
        return null;
    }

    @Override
    public void onClick(View view) {
        if(requestPermission()){
            switch (view.getId()){
                case R.id.btn_header:
                    processHeader();
                    break;
                case R.id.btn_GSON:
                    jsonParse();
                    break;
                case R.id.btn_cache:
                    processCache("myCache");
                    break;
                case R.id.btn_cache_interceptor:
                    cacheInterceptor();
                    break;
                case R.id.btn_cancel_call:
                    cancelCall();
                    break;
                case R.id.btn_set_call:
                    setCall();
                    break;
            }
        }else{
            mCurrentClickView = view;
        }
    }

    /**
     * 提取响应头
     * 当写请求头的时候，使用header(name,value)可以设置唯一的name、value。如果已经有值，旧的将被移除，然后添加新的，
     * 使用addHeader(name,value)可以添加多值（添加, 移除已有的）。
     * 当读取响应头时，使用header(name)返回最后出现的name、value。通常情况这也是唯一的name、value，如果没有值，
     * 那么header(name)将返回null，如果想读取字段对应的所有值，使用headers(name)会返回一个list。
     */
    private void processHeader(){
        Log.d(Constants.TAG,"processHeader()");
        mOkHttpClient = new OkHttpClient();
        mRequest = new Request.Builder()
                .url(Constants.URL_HEADE)
                .header("User-Agent", "OkHttp Headers.java")
                .addHeader("Accept", "application/json; q=0.5")
                .addHeader("Accept", "application/vnd.github.v3+json")
                .build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mResponse = mOkHttpClient.newCall(mRequest).execute();
                    if (!mResponse.isSuccessful())
                        throw new IOException("Unexpected code " + mResponse);
                    Log.d(Constants.TAG,"Server: " + mResponse.header("Server"));
                    Log.d(Constants.TAG,"Date: " + mResponse.header("Date"));
                    Log.d(Constants.TAG,"Vary: " + mResponse.headers("Vary"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 用Gson来解析Github API的JSON响应
     * ResponseBody.charStream()使用响应头Content-Type指定的字符集来解析响应体，默认是UTF-8。
     */
    private void jsonParse(){
        Log.d(Constants.TAG,"jsonParse()");
        mOkHttpClient = new OkHttpClient();
        final Gson gson = new Gson();

        mRequest = new Request.Builder()
                .url(Constants.URL_JSON)
                .build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mResponse = mOkHttpClient.newCall(mRequest).execute();
                    if (!mResponse.isSuccessful())
                        throw new IOException("Unexpected code " + mResponse);
                    Gist gist = gson.fromJson(mResponse.body().charStream(), Gist.class);
                    for (Map.Entry<String, GistFile> entry : gist.files.entrySet()) {
                        Log.d(Constants.TAG,"key:"+entry.getKey()+",value:"+entry.getValue().content);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 为了缓存响应，你需要一个你可以读写的缓存目录，和缓存大小的限制，这个缓存目录应该是私有的，不信任的程序应不能读取缓存内容。
     * 一个缓存目录同时拥有多个缓存访问是错误的，大多数程序只需要调用一次new OkHttp()，在第一次调用时配置好缓存，
     * 然后其他地方只需要调用这个实例就可以了，否则两个缓存示例互相干扰，破坏响应缓存，而且有可能会导致程序崩溃。
     * 响应缓存使用HTTP头作为配置，你可以在请求头中添加Cache-Control: max-stale=3600，OkHttp缓存会支持。
     * 你的服务通过响应头确定响应缓存多长时间，例如使用Cache-Control: max-age=9600。
     */
    private void processCache(String cacheDirectory){
        Log.d(Constants.TAG,"processCache()");
        //10MB
        int cacheSize = 10*1024*1024;
        Cache cache = new Cache(createDir(cacheDirectory),cacheSize);

        //此处使用Builder().build()方式创建OkHttpClient
        mOkHttpClient = new OkHttpClient.Builder().cache(cache).build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //强制使用网络
                    Request request1 = new Request.Builder()
                            .url(Constants.URL_CACHE)
                            .cacheControl(CacheControl.FORCE_NETWORK)
                            .build();
                    //如果服务器端强制使用缓存，则可通过将缓存时间修改为0的方式实现不缓存
//                    Request request1 = new Request.Builder()
//                            .url(Constants.URL_CACHE)
//                            .cacheControl(new CacheControl.Builder().maxAge(0, TimeUnit.SECONDS).build())
//                            .build();
                    Response response1 = mOkHttpClient.newCall(request1).execute();
                    if (!response1.isSuccessful())
                        throw new IOException("Unexpected code " + response1);
                    String response1Body = response1.body().string();
                    Log.d(Constants.TAG, "response1Body:" + response1Body);
                    Log.d(Constants.TAG, "response1:" + response1);
                    Log.d(Constants.TAG, "cache response1:" + response1.cacheResponse());
                    Log.d(Constants.TAG, "network response1:" + response1.networkResponse());
                    Log.d(Constants.TAG, "response1.cacheControl()"+response1.cacheControl().toString());

                    /**
                     * 强制使用缓存
                     * 如果你使用FORCE_CACHE,但是response要求使用网络(即服务器不支持缓存),OkHttp将会返回一个504 Unsatisfiable Request响应。
                     * 如果服务器不支持缓存，则需要使用Interceptor来重写Response的头部信息，从而让OkHttp支持缓存
                     */
                    Request request2 = new Request.Builder()
                            .url(Constants.URL_CACHE)
                            .cacheControl(CacheControl.FORCE_CACHE)
                            .build();
                    Response response2 = mOkHttpClient.newCall(request2).execute();
                    if(response2.code()!=504){
                        Log.d(Constants.TAG,"可以使用缓存");
                        String response2Body = response2.body().string();
                        Log.d(Constants.TAG, "response2Body:" + response2Body);
                        Log.d(Constants.TAG, "response2:" + response2);
                        Log.d(Constants.TAG, "cache response2:" + response2.cacheResponse());
                        Log.d(Constants.TAG, "network response2:" + response2.networkResponse());
                        Log.d(Constants.TAG, "response2.cacheControl()"+response2.cacheControl().toString());

                        Log.d(Constants.TAG,"Response 2 equals Response 1? " + response1Body.equals(response2Body));
                    }else{
                        Log.d(Constants.TAG,"不可以使用缓存");
                    }
                    if (!response2.isSuccessful())
                        throw new IOException("Unexpected code " + response2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 如果服务器不支持缓存就可能没有指定这个头部，或者指定的值是如no-store等，但是我们还想在本地使用缓存的话要怎么办呢？
     * 这种情况下我们就需要使用Interceptor来重写Respose的头部信息，从而让okhttp支持缓存。
     */
    private void cacheInterceptor(){
        Log.d(Constants.TAG,"processCache()");
        //10MB
        int cacheSize = 10*1024*1024;
        Cache cache = new Cache(createDir("cache_interceptor"),cacheSize);

        //加入拦截器
        mOkHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new CacheInterceptor())
                .cache(cache)
                .connectTimeout(20,TimeUnit.SECONDS)
                .readTimeout(20,TimeUnit.SECONDS)
                .build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //强制使用网络
                    Request request1 = new Request.Builder()
                            .url(Constants.URL_CACHE)
                            .cacheControl(CacheControl.FORCE_NETWORK)
                            .build();
                    //如果服务器端强制使用缓存，则可通过将缓存时间修改为0的方式实现不缓存
//                    Request request1 = new Request.Builder()
//                            .url(Constants.URL_CACHE)
//                            .cacheControl(new CacheControl.Builder().maxAge(0, TimeUnit.SECONDS).build())
//                            .build();
                    Response response1 = mOkHttpClient.newCall(request1).execute();
                    if (!response1.isSuccessful())
                        throw new IOException("Unexpected code " + response1);
                    String response1Body = response1.body().string();
                    Log.d(Constants.TAG, "response1Body:" + response1Body);
                    Log.d(Constants.TAG, "response1:" + response1);
                    Log.d(Constants.TAG, "cache response1:" + response1.cacheResponse());
                    Log.d(Constants.TAG, "network response1:" + response1.networkResponse());
                    Log.d(Constants.TAG, "response1.cacheControl()"+response1.cacheControl().toString());

                    /**
                     * 强制使用缓存
                     * 如果你使用FORCE_CACHE,但是response要求使用网络(即服务器不支持缓存),OkHttp将会返回一个504 Unsatisfiable Request响应。
                     * 如果服务器不支持缓存，则需要使用Interceptor来重写Response的头部信息，从而让OkHttp支持缓存
                     */
                    Request request2 = new Request.Builder()
                            .url(Constants.URL_CACHE)
                            .cacheControl(CacheControl.FORCE_CACHE)
                            .build();
                    Response response2 = mOkHttpClient.newCall(request2).execute();
                    if(response2.code()!=504){
                        Log.d(Constants.TAG,"可以使用缓存");
                        String response2Body = response2.body().string();
                        Log.d(Constants.TAG, "response2Body:" + response2Body);
                        Log.d(Constants.TAG, "response2:" + response2);
                        Log.d(Constants.TAG, "cache response2:" + response2.cacheResponse());
                        Log.d(Constants.TAG, "network response2:" + response2.networkResponse());
                        Log.d(Constants.TAG, "response2.cacheControl()"+response2.cacheControl().toString());

                        Log.d(Constants.TAG,"Response 2 equals Response 1? " + response1Body.equals(response2Body));
                    }else{
                        Log.d(Constants.TAG,"不可以使用缓存");
                    }
                    if (!response2.isSuccessful())
                        throw new IOException("Unexpected code " + response2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 使用Call.cancel()可以立即停止掉一个正在执行的call，如果一个线程正在写请求或者读响应，将会引发IOException，
     * 当call没有必要的时候，使用这个api可以节约网络资源，例如当用户离开一个应用时，不管同步还是异步的call都可以取消。
     * 你可以通过tags来同时取消多个请求，当你构建一请求时，使用RequestBuilder.tag(tag)来分配一个标签，
     * 之后你就可以用OkHttpClient.cancel(tag)来取消所有带有这个tag的call。
     */
    private void cancelCall(){
        mOkHttpClient = new OkHttpClient();
        new Thread(new Runnable() {
            @Override
            public void run() {
                mRequest = new Request.Builder()
                        .url(Constants.URL_DELAY_2)
                        .build();
                /**
                 * nanoTime()返回最准确的可用系统计时器的当前值，以毫微秒为单位。
                 * 此方法只能用于测量已过的时间，与系统或钟表时间的其他任何时间概念无关。
                 * 返回值表示从某一固定但任意的时间算起的毫微秒数（或许从以后算起，所以该值可能为负）
                 */
                final long startNanos = System.nanoTime();
                final Call call = mOkHttpClient.newCall(mRequest);
                // 第2个参数“1”表示延时1秒执行，表示1秒后取消网络请求
                mScheduledExecutorService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(Constants.TAG,(System.nanoTime()-startNanos)/1e9f + " canceling call.");
                        call.cancel();
                        Log.d(Constants.TAG,(System.nanoTime()-startNanos)/1e9f + " canceled call.");
                    }
                },1,TimeUnit.SECONDS);
                Log.d(Constants.TAG,(System.nanoTime()-startNanos)/1e9f + " executing call.");
                try {
                    mResponse = call.execute();
                    Log.d(Constants.TAG, (System.nanoTime() - startNanos)/1e9f+" call was expected to fail, but completed:"+mResponse);

                } catch (IOException e) {
//                    e.printStackTrace();
                    Log.d(Constants.TAG,(System.nanoTime() - startNanos)/1e9f+" call failed as expected: "+e);
                }
            }
        }).start();
    }

    /**
     * 使用OkHttpClient，所有的HTTP Client配置包括代理设置、超时设置、缓存设置，当你需要为单个call改变配置的时候，
     * 调用OkHttpClient.newBuilder()，这个api将会返回一个builder，这个builder和原始的client共享相同的连接池，分发器和配置。
     */
    private void setCall(){
        final OkHttpClient okHttpClient = new OkHttpClient();
        new Thread(new Runnable() {
            @Override
            public void run() {
                mRequest = new Request.Builder()
                        .url(Constants.URL_DELAY_1)
                        .build();
                OkHttpClient okHttpClient1 = okHttpClient.newBuilder()
                        .readTimeout(1000,TimeUnit.SECONDS)
                        .build();
                try {
                    Response response1 = okHttpClient1.newCall(mRequest).execute();
                    Log.d(Constants.TAG,"Response 1 succeeded: " + response1);
                } catch (IOException e) {
                    Log.d(Constants.TAG,"Response 1 failed: " + e);
                }

                OkHttpClient okHttpClient2 = okHttpClient.newBuilder()
                        .readTimeout(3000, TimeUnit.SECONDS)
                        .build();
                try {
                    Response response2 = okHttpClient2.newCall(mRequest).execute();
                    Log.d(Constants.TAG,"Response 2 succeeded: " + response2);
                } catch (IOException e) {
                    Log.d(Constants.TAG,"Response 2 failed: " + e);
                }
            }
        }).start();
    }

    /** 根据目录名（dirName）创建目录 */
    private File createDir(String dirName){
        File sd = mContext.getCacheDir();
        String path = sd.getPath()+"/"+dirName;
        File file = new File(path);
        if(!file.exists()){
            file.mkdir();
        }
        return file;
    }

    static class Gist{
        Map<String, GistFile> files;
    }

    static class GistFile {
        String content;
    }
}
