package com.year17.fw_okhttp;

import okhttp3.MediaType;

/**
 * 作者：张玉辉
 * 时间：2017/8/8.
 */

public class Constants {
    public static final String TAG = "tag";

    /** 网络请求资源地址 **/
    public static final String URL = "http://192.168.1.211:9000/rxjava";
    /** post请求发送Markdown文档的请求地址 **/
    public static final String URL_MARKDOWN = "https://api.github.com/markdown/raw";
    /** POST提交json地址 **/
    public static final String URL_POST_JSON = "http://write.blog.csdn.net/postlist/0/0/enabled/1";
    /** post请求提交form表单 **/
    public static final String URL_FORM = "https://en.wikipedia.org/w/index.php";
    /** post提交分块请求 **/
    public static final String URL_BLOCKS = "https://api.imgur.com/3/image";
    /** heade处理地址 **/
    public static final String URL_HEADE = "https://api.github.com/repos/square/okhttp/issues";
    /** 用GSON解析json **/
    public static final String URL_JSON = "https://api.github.com/gists/c2a7c39532239ff261be";
    /** 使用Cache的请求地址 **/
    public static final String URL_CACHE = "http://publicobject.com/helloworld.txt";
    /** 该地址有2秒的延时 **/
    public static final String URL_DELAY_2 = "http://httpbin.org/delay/2";
    /** 该地址有1秒的延时 **/
    public static final String URL_DELAY_1 = "http://httpbin.org/delay/2";
    public static final String IMGUR_CLIENT_ID = "...";
    /** 申请访问网络权限的Request Code **/
    public static final int INTERNET_REQUEST_CODE = 1;
    /** post请求的请求体内容为文本格式 **/
    public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown;charset=utf-8");
    /** post请求的请求体内容为图片格式 **/
    public static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    /** post请求的请求体内容为json格式 **/
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
}
