package com.year17.fw_okhttp;

import okhttp3.MediaType;

/**
 * 作者：张玉辉
 * 时间：2017/8/8.
 */

public class Constants {
    /** 网络请求资源地址 **/
    public static final String URL = "http://192.168.1.211:9000/rxjava";
    /** post请求发送Markdown文档的请求地址 **/
    public static final String URL_MARKDOWN = "https://api.github.com/markdown/raw";
    /** post请求提交form表单 **/
    public static final String URL_FORM = "https://en.wikipedia.org/w/index.php";
    /** post提交分块请求 **/
    public static final String URL_BLOCKS = "https://api.imgur.com/3/image";
    public static final String IMGUR_CLIENT_ID = "...";
    /** 申请访问网络权限的Request Code **/
    public static final int INTERNET_REQUEST_CODE = 1;
    public static final String TAG = "tag";
    /** post请求的请求体内容的媒体格式 **/
    public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown;charset=utf-8");
    public static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
}
