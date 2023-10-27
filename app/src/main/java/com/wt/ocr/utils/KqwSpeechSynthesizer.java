package com.wt.ocr.utils;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.util.ResourceUtil;
import com.iflytek.cloud.util.ResourceUtil.RESOURCE_TYPE;
import com.wt.ocr.TakePhoteActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * 语音合成器
 *
 * @author kongqw
 */
public class KqwSpeechSynthesizer {

    // Log标签
    private static final String TAG = "KqwSpeechSynthesizer";
    private String speed;
    private Context mContext;
    private int flag;
    // 语音合成对象
    private SpeechSynthesizer mTts;

    public KqwSpeechSynthesizer(Context context) {
        mContext = context;
        flag=0;
        speed="60";

        // 初始化合成对象
        SpeechSynthesizer m= SpeechSynthesizer.createSynthesizer(context, new InitListener() {
            @Override
            public void onInit(int code) {
                Log.d(TAG, "InitListener init() code = " + code);
                if (code != ErrorCode.SUCCESS) {
                    Log.d(TAG, "初始化失败,错误码：" + code);
                    Toast.makeText(mContext, "初始化失败,错误码：" + code, Toast.LENGTH_SHORT).show();
                }else{
                    Log.d(TAG, "初始化成功");
                }
            }
        });
        mTts=m;
    }

    /**
     * 开始语音合成
     *
     * @param text
     */
    public void start(String text) {
        // 设置参数
        setParam();
        int code = mTts.startSpeaking(text, mTtsListener);
        if (code != ErrorCode.SUCCESS) {
            Toast.makeText(mContext, "语音合成失败,错误码: " + code, Toast.LENGTH_SHORT).show();
        }
    }
    public void pause(){
        mTts.pauseSpeaking();
    }
    public void bofang(){
        mTts.resumeSpeaking();
    }
    public void kuaijin(String result){
        speed="100";
        mTts.setParameter(SpeechConstant.SPEED, speed);
        mTts.startSpeaking(result,mTtsListener);
    }
    public void huifu(){
        speed="60";
        mTts.setParameter(SpeechConstant.SPEED, speed);
    }
    public void huitui(String result){
        speed="20";
        mTts.setParameter(SpeechConstant.SPEED, speed);
        mTts.startSpeaking(result,mTtsListener);
    }
    public void stop(){
        flag=0;
        mTts.stopSpeaking();
    }
    public void destroy(){
        mTts.destroy();
    }
    public void changeListener(){
        SynthesizerListener listener = new SynthesizerListener(){
            @Override
            public void onSpeakBegin() {

            }

            @Override
            public void onSpeakPaused() {

            }

            @Override
            public void onSpeakResumed() {

            }

            @Override
            public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
//            Log.i(TAG, "传冲进度 ：" + percent);
            }

            @Override
            public void onSpeakProgress(int percent, int beginPos, int endPos) {
//            Log.i(TAG, "合成进度 ： " + percent);
            }

            @Override
            public void onCompleted(SpeechError error) {

                if (error == null) {
                    Intent intent = new Intent(mContext, TakePhoteActivity.class);
                    mContext.startActivity(intent);
                } else if (error != null) {
                    Log.i(TAG, "error : " + error.toString()+flag);
                }

            }

            @Override
            public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
                // TODO Auto-generated method stub

            }
        };
        mTtsListener=listener;
    }
    public boolean isSpeak(){
        return mTts.isSpeaking();
    }
    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {
            flag=1;
            Log.i(TAG, "开始合成"+flag);
        }

        @Override
        public void onSpeakPaused() {
            flag=2;
            Log.i(TAG, "暂停合成"+flag);
        }

        @Override
        public void onSpeakResumed() {
            flag=1;
            Log.i(TAG, "继续合成"+flag);
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
//            Log.i(TAG, "传冲进度 ：" + percent);
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
//            Log.i(TAG, "合成进度 ： " + percent);
        }

        @Override
        public void onCompleted(SpeechError error) {
            flag=0;
            if (error == null) {
                Log.i(TAG, "合成完成"+flag);
            } else if (error != null) {
                Log.i(TAG, "error : " + error.toString()+flag);
            }

        }

        @Override
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
            // TODO Auto-generated method stub

        }
    };

    /**
     * 参数设置
     *
     * @return
     */
    private void setParam() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 设置使用本地引擎
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        // 设置发音人资源路径
        mTts.setParameter(ResourceUtil.TTS_RES_PATH, getResourcePath());
        // 设置发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");

        // 设置语速
        mTts.setParameter(SpeechConstant.SPEED, speed);

        // 设置音调
        mTts.setParameter(SpeechConstant.PITCH, "50");

        // 设置音量
        mTts.setParameter(SpeechConstant.VOLUME, "60");

        // 设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
    }

    // 获取发音人资源路径
    private String getResourcePath() {
        StringBuffer tempBuffer = new StringBuffer();
        // 合成通用资源
        tempBuffer.append(ResourceUtil.generateResourcePath(mContext, RESOURCE_TYPE.assets, "tts/common.jet"));
        tempBuffer.append(";");
        // 发音人资源
        tempBuffer.append(ResourceUtil.generateResourcePath(mContext, RESOURCE_TYPE.assets, "tts/xiaoyan.jet"));
        return tempBuffer.toString();
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
