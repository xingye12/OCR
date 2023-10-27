package com.wt.ocr;

import android.annotation.SuppressLint;
import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import android.app.Application;
import android.text.TextUtils;
import android.widget.Toast;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.util.NIMUtil;
//import com.netease.yunxin.kit.conversationkit.ui.normal.page.ConversationFragment;
//import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.wt.ocr.utils.IM.Preferences;



public class App extends Application {
    private static GoogleAnalytics sAnalytics;
    private static Tracker sTracker;
    public static boolean hasLogined = false;
    @SuppressLint("VisibleForTests")
    @Override
    public void onCreate() {
        // 应用程序入口处调用,避免手机内存过小,杀死后台进程后通过历史intent进入Activity造成SpeechUtility对象为null
        // 如在Application中调用初始化，需要在Mainifest中注册该Applicaiton
        // 注意：此接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
        // 参数间使用“,”分隔。
        // 设置你申请的应用appid
        StringBuffer param = new StringBuffer();
        param.append("appid=3f2c3aef");
        param.append(",");
        param.append(SpeechConstant.ENGINE_MODE + "=" + SpeechConstant.MODE_MSC);
         param.append(",");
         param.append(SpeechConstant.FORCE_LOGIN + "=true");
        SpeechUtility.createUtility(App.this, param.toString());

        sAnalytics = GoogleAnalytics.getInstance(App.this);

        super.onCreate();

        Preferences.setContext(this);
        String account = Preferences.getUserAccount();
        String token = Preferences.getUserToken();
        LoginInfo loginInfo = null;

        if (!TextUtils.isEmpty(account)&&!TextUtils.isEmpty(token)){
            //之前已经登录过，可以走自动登录。
            hasLogined = true;
            loginInfo = new LoginInfo(account,token);
        }
        SDKOptions sdkOptions = new SDKOptions();
        sdkOptions.appKey="368b830f9a4339e6e193c2b62b2a969b";
//        IMKitClient.init(this,loginInfo,sdkOptions);

//        if(NIMUtil.isMainProcess(this)){
            NIMClient.init(this,loginInfo,sdkOptions);
//        }


    }

    synchronized public Tracker getDefaultTracker() {
        if (sTracker == null) {
            sTracker = sAnalytics.newTracker(R.xml.global_tracker);
        }
        return sTracker;
    }
}
