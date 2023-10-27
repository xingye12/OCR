package com.wt.ocr.utils;

import static com.wt.ocr.utils.CheckSumBuilder.generateNonce;

import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HttpUtil {
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build();

    public static String sendPostRequest(    String requestBody) {
        String appKey= "368b830f9a4339e6e193c2b62b2a969b";
        String url="https://api.netease.im/nimserver/user/create.action";
        RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), requestBody);
        long currentTimeMillis = System.currentTimeMillis() / 1000; // 转换为秒
        String curTime=String.valueOf(currentTimeMillis);
        String nonce = generateNonce(16);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("AppKey", appKey)
                .addHeader("Nonce", nonce)
                .addHeader("CurTime", curTime)
                .addHeader("CheckSum", CheckSumBuilder.getCheckSum(nonce,curTime))
                .post(body)
                .build();

        String response = null;
        try (Response httpResponse = client.newCall(request).execute()) {
            if (httpResponse.isSuccessful()) {
                response = httpResponse.body().string();
            } else {
                // 处理请求失败的情况
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}