package com.wt.ocr;

import static com.wt.ocr.InitActivity.flag;
import static com.wt.ocr.utils.JsonData.MYURL;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.widget.Button;

import android.widget.EditText;
import android.widget.ImageButton;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.wt.ocr.pojo.Boke;
import com.wt.ocr.pojo.User;
import com.wt.ocr.utils.DictationResult;
import com.wt.ocr.utils.JsonData;
import com.wt.ocr.utils.KqwSpeechSynthesizer;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.inputmethod.InputMethodManager;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CommunityActivity extends AppCompatActivity implements View.OnLongClickListener, OnClickListener {
    private static String APPID = "3f2c3aef";
    private KqwSpeechSynthesizer mKqwSpeechSynthesizer;
    private String username;
    private String nickname;
    private String address;
    private String phone;
    private String idCard;
    private String sex;
    private String friend;
    private Button friendBtn;
    private Button bokeBtn;
    private Button yesBtn;
    private Button toInitBtn;
    private ImageButton listenBtn;
    private EditText contentEt;
    private int teamOrFriend;


    // 听写结果字符串（多个Json的列表字符串）
    private String dictationResultStr = "[";

    //申请录音权限
    private static final int GET_RECODE_AUDIO = 1;
    private static String[] PERMISSION_AUDIO = {
            Manifest.permission.RECORD_AUDIO
    };
    /*
     * 申请录音权限*/
    public static void verifyAudioPermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.RECORD_AUDIO);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSION_AUDIO,
                    GET_RECODE_AUDIO);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_community);
        mKqwSpeechSynthesizer = new KqwSpeechSynthesizer(this);
        mKqwSpeechSynthesizer.start("这里是明阅社区首页，从屏幕的上方到下方依次是好友列表按钮，明阅论坛按钮，确定执行语音输入按钮和语音输入按钮。" +
                "您可以通过点击语音屏幕最下方的语音输入按钮，说出自己想要做的事情，如我要和某某聊天，或者我要进入论坛" +
                "然后长按语音输入按钮上方的确定按钮，即可来到对应的界面");
        Intent intentExtra=getIntent();
        Bundle bundleExtra=intentExtra.getBundleExtra("bundle");
        username= bundleExtra.getString("username");
        nickname= bundleExtra.getString("nickname");
        sex=bundleExtra.getString("sex");
        address= bundleExtra.getString("address");
        idCard=bundleExtra.getString("idCard");
        phone= bundleExtra.getString("phone");
        System.out.println(sex);
        listenBtn = findViewById(R.id.speechBtn);
        contentEt = findViewById(R.id.speechText);
        friendBtn=findViewById(R.id.friendBtn);
        bokeBtn=findViewById(R.id.bokeBtn);
        yesBtn=findViewById(R.id.yesBtn);
        toInitBtn=findViewById(R.id.communityToInitBtn);
        verifyAudioPermissions(this);
        listenBtn.setOnClickListener(this);
        toInitBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这是回到首页的按钮");
            }
        });
        toInitBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent=new Intent(CommunityActivity.this,InitActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("username",username);
                bundle.putString("nickname",nickname);
                bundle.putString("sex",sex);
                bundle.putString("address",address);
                bundle.putString("idCard",idCard);
                bundle.putString("phone",phone);
                intent.putExtra("bundle",bundle);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            }
        });
        yesBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                    mKqwSpeechSynthesizer.start("这是群组列表按钮");

            }
        });
        yesBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                teamOrFriend=0;
                new Thread(getFriendRun).start();
                return true;
            }
        });
        friendBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这是好友列表按钮，长按进入好友列表界面");
            }
        });
        friendBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                teamOrFriend=1;
                new Thread(getFriendRun).start();
                return true;
            }
        });
        bokeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这是论坛入口按钮");
            }
        });
        bokeBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new Thread(enterBokeRun).start();
                return true;
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.speechBtn:
                dictationResultStr = "[";
                // 语音配置对象初始化
                SpeechUtility.createUtility(CommunityActivity.this, SpeechConstant.APPID
                        + "=" + APPID);
                // 1.创建SpeechRecognizer对象，第2个参数：本地听写时传InitListener
                SpeechRecognizer mIat = SpeechRecognizer.createRecognizer(
                        CommunityActivity.this, null);
                // 交互动画
                RecognizerDialog iatDialog = new RecognizerDialog(
                        CommunityActivity.this, null);
                // 2.设置听写参数，详见《科大讯飞MSC API手册(Android)》SpeechConstant类
                mIat.setParameter(SpeechConstant.DOMAIN, "iat"); // domain:域名
                mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
                mIat.setParameter(SpeechConstant.ACCENT, "mandarin"); // mandarin:普通话
                //3.开始听写
                iatDialog.setListener(new RecognizerDialogListener() {
                    @Override
                    public void onResult(RecognizerResult results, boolean isLast) {
                        // TODO 自动生成的方法存根
                        // Log.d("Result", results.getResultString());
                        // contentTv.setText(results.getResultString());
                        if (!isLast) {
                            dictationResultStr += results.getResultString() + ",";
                        } else {
                            dictationResultStr += results.getResultString() + "]";
                        }
                        if (isLast) {
                            // 解析Json列表字符串
                            Gson gson = new Gson();
                            List<DictationResult> dictationResultList = gson
                                    .fromJson(dictationResultStr,
                                            new TypeToken<List<DictationResult>>() {
                                            }.getType());
                            String finalResult = "";
                            for (int i = 0; i < dictationResultList.size() - 1; i++) {
                                finalResult += dictationResultList.get(i)
                                        .toString();
                            }
                            contentEt.setText(finalResult);
                            mKqwSpeechSynthesizer.start("语音内容是："+finalResult);
                            //获取焦点
                            contentEt.requestFocus();
                            //将光标定位到文字最后，以便修改
                            contentEt.setSelection(finalResult.length());
                            Log.d("From reall phone", finalResult);
                        }
                    }


                    @Override
                    public void onError(SpeechError error) {
                        // TODO 自动生成的方法存根
                        error.getPlainDescription(true);
                    }
                });


                // 开始听写
                iatDialog.show();


                break;
            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        return false;
    }
    Runnable ChatRun = new Runnable() {
        @Override
        public void run() {

            User userCredentials = new User();
            userCredentials.setNickname(friend);
            userCredentials.setUsername(username);
            Gson gson = new Gson();
            String json = gson.toJson(userCredentials);

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json"));

            Request request = new Request.Builder()
                    .url(MYURL+"/chatWithFriend")
                    .post(requestBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String jsonResponse = response.body().string();

                Gson backGson = new Gson();
                JsonData jsonData = backGson.fromJson(jsonResponse, JsonData.class);
                String code = jsonData.getCode();
                String msg = jsonData.getMsg();
                Object o = jsonData.getData();
                if(code.equals("1")){
                    Map map=gson.fromJson(o.toString(),Map.class);
                    String nickname=map.get("nickname").toString();
                    String sex=map.get("sex").toString();
                    String address=map.get("address").toString();
                    String idCard=map.get("idCard").toString();
                    String phone= new BigDecimal(map.get("phone").toString()).toString();
                    Intent intent=new Intent(CommunityActivity.this,ChatActivity.class);
                    Bundle bundle=new Bundle();
                    bundle.putString("friend",friend);
                    bundle.putString("username",username);
                    bundle.putString("nickname",nickname);
                    bundle.putString("sex",sex);
                    bundle.putString("address",address);
                    bundle.putString("idCard",idCard);
                    bundle.putString("phone",phone);
                    intent.putExtra("bundle",bundle);
                    flag=1;
                    startActivity(intent);
                }else{
                    mKqwSpeechSynthesizer.start("好友不存在或是语音格式错误");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };
    Runnable getFriendRun = new Runnable() {
        @Override
        public void run() {

            User userCredentials = new User();
//            userCredentials.setNickname(friend);
            userCredentials.setUsername(username);
            Gson gson = new Gson();
            String json = gson.toJson(userCredentials);
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url(MYURL+"/getFriendList")
                    .post(requestBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String jsonResponse = response.body().string();
                Gson backGson = new Gson();
                JsonData jsonData = backGson.fromJson(jsonResponse, JsonData.class);
                String code = jsonData.getCode();
                String msg = jsonData.getMsg();
                Object object = jsonData.getData();
                ArrayList<String> friends=new ArrayList<>();
                if (object instanceof ArrayList) {
                    ArrayList<Object> objectList = (ArrayList<Object>) object;
                    for (Object element : objectList) {
                        if (element instanceof String) {
                            String elementString = (String) element;
                            friends.add(elementString);
                        }
                    }
                }
                if(teamOrFriend==1){
                    if(code.equals("1")){
//                    Map map=gson.fromJson(o.toString(),Map.class);
                        Intent intent=new Intent(CommunityActivity.this,FriendListActivity.class);
                        Bundle bundle=new Bundle();
                        bundle.putStringArrayList("friends",friends);
                        bundle.putString("username",username);
                        bundle.putString("nickname",nickname);
                        bundle.putString("sex",sex);
                        bundle.putString("address",address);
                        bundle.putString("idCard",idCard);
                        bundle.putString("phone",phone);
                        intent.putExtra("bundle",bundle);
                        startActivity(intent);
                    }else{
                        mKqwSpeechSynthesizer.start("您还未添加好友");
                    }
                } else if (teamOrFriend==0) {
                    Intent intent=new Intent(CommunityActivity.this,TeamListActivity.class);
                    Bundle bundle=new Bundle();
                    bundle.putStringArrayList("friends",friends);
                    bundle.putString("username",username);
                    bundle.putString("nickname",nickname);
                    bundle.putString("sex",sex);
                    bundle.putString("address",address);
                    bundle.putString("idCard",idCard);
                    bundle.putString("phone",phone);
                    intent.putExtra("bundle",bundle);
                    startActivity(intent);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };
    Runnable enterBokeRun = new Runnable() {
        @Override
        public void run() {

            Boke boke=new Boke();
            boke.setType("a");
            Gson gson = new Gson();
            String json = gson.toJson(boke);

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json"));

            Request request = new Request.Builder()
                    .url(MYURL+"/getAllBoke")
                    .post(requestBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String jsonResponse = response.body().string();

                Gson backGson = new Gson();
                JsonData jsonData = backGson.fromJson(jsonResponse, JsonData.class);
                String code = jsonData.getCode();
                String msg = jsonData.getMsg();
                Object object = jsonData.getData();
                if(code.equals("1")){
                    Object o = jsonData.getData();
                    ArrayList<Boke> bokes = new ArrayList<>();
                    if (o instanceof ArrayList) {
                        ArrayList<Object> arrayList = (ArrayList<Object>) o;
                        System.out.println("是");
                        for (Object item : arrayList) {
                            if (item instanceof LinkedTreeMap) {
                                LinkedTreeMap<String, Object> linkedTreeMap = (LinkedTreeMap<String, Object>) item;
                                // 从LinkedTreeMap中提取值
                                String nickname = (String) linkedTreeMap.get("nickname");
                                String title = (String) linkedTreeMap.get("title");
                                String time = (String) linkedTreeMap.get("time");
                                String type = (String) linkedTreeMap.get("type");
                                String content=(String)linkedTreeMap.get("content");
                                // 创建Boke对象
                                Boke bokeResult = new Boke(nickname, time, title,content, type);
                                bokes.add(bokeResult);
                            }
                        }
                    }
                    Intent intent=new Intent(CommunityActivity.this,BokeActivity.class);
                    Bundle bundle=new Bundle();
                    bundle.putString("username",username);
                    intent.putExtra("bundle",bundle);
                    intent.putParcelableArrayListExtra("bokeList", bokes);
                    startActivity(intent);
                }else{
                    mKqwSpeechSynthesizer.start("获取失败");
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        mKqwSpeechSynthesizer.start("这里是社区首页");
    }
}