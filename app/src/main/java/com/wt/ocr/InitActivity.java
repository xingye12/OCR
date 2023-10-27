package com.wt.ocr;

import static com.wt.ocr.utils.JsonData.MYURL;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.wt.ocr.pojo.Boke;
import com.wt.ocr.pojo.User;
import com.wt.ocr.utils.IM.Preferences;
import com.wt.ocr.utils.JsonData;
import com.wt.ocr.utils.KqwSpeechSynthesizer;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InitActivity extends AppCompatActivity {
    private String username;
    private String phone;
    private String nickname;
    private String sex;
    private String address;
    private String idCard;
    public static int flag=0;
    private KqwSpeechSynthesizer mKqwSpeechSynthesizer;
    private TextView translateText;
    private TextView getWordText;
    private TextView myInfoText;
    private TextView communityText;
    private TextView testText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_init);
        if(!shouldJumpToLoginActivity()){
            username= Preferences.getUserAccount();
            if(TextUtils.isEmpty(nickname)){
                new Thread(getUserInfoRun).start();
            }
        }

        translateText=findViewById(R.id.textTranslate);
        getWordText=findViewById(R.id.textGetChineseAndEnglish);
        myInfoText=findViewById(R.id.textMyInfo);
        communityText=findViewById(R.id.textComunnity);
        testText=findViewById(R.id.textTest);
        testText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(InitActivity.this,TestActivity.class);
                startActivity(intent);
            }
        });
        mKqwSpeechSynthesizer = new KqwSpeechSynthesizer(this);
        if(flag==0){
            mKqwSpeechSynthesizer.start("欢迎进入明阅辅助阅读平台！长按手机屏幕可以使用不同的功能。从上到下依次是：盲文转录，书籍、药品语音播报，个人信息，明阅社区");
//            user=null;
        }else if(flag==1||flag==2){
            if(flag==1){
                mKqwSpeechSynthesizer.start("登陆成功！您现在在明阅辅助阅读平台首页");
                flag=2;
            }else if(flag==2){
                mKqwSpeechSynthesizer.start("您现在在明阅辅助阅读平台首页");
            }
            Intent loginIntent=getIntent();
            Bundle bundleExtra=loginIntent.getBundleExtra("bundle");
            username= bundleExtra.getString("username");
            nickname= bundleExtra.getString("nickname");
            sex=bundleExtra.getString("sex");
            address= bundleExtra.getString("address");
            idCard=bundleExtra.getString("idCard");
            phone= bundleExtra.getString("phone");
        }

        translateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这是进行中英文拍照识别按钮，长按可以开始对文字进行拍照");
            }
        });
        translateText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent=new Intent(InitActivity.this,MainActivity.class);
                intent.putExtra("flag",1);
                startActivity(intent);
                return true;
            }
        });
        getWordText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent=new Intent(InitActivity.this,MainActivity.class);
                intent.putExtra("flag",2);
                startActivity(intent);
                return true;
            }
        });
        getWordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这是盲文转录按钮，长按可以开始对文字进行拍照");
            }
        });
        myInfoText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这是个人信息按钮，如果您未登录，长按后会先进入登陆界面");
            }
        });
        myInfoText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
//                if(username==null||username.equals("")){
                if(shouldJumpToLoginActivity()){
                    Intent intent=new Intent(InitActivity.this,LoginActivity.class);
                    startActivity(intent);

                }else {

                   new Thread(getAllUserRun).start();

                }
                return true;
            }
        });
        communityText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这是明阅社区，如果您未登录，长按后会先进入登陆界面");
            }
        });
        communityText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
//                if(username==null||username.equals("")){
                if(shouldJumpToLoginActivity()){
                    Intent intent=new Intent(InitActivity.this,LoginActivity.class);
                    startActivity(intent);

                }else {

                    new Thread(getRequestRun).start();
                }
                return true;
            }

        });

    }
    Runnable getUserInfoRun = new Runnable() {
        @Override
        public void run() {

            User userCredentials = new User();
            userCredentials.setUsername(username);
            userCredentials.setPassword(Preferences.getUserToken());

            Gson gson = new Gson();
            String json = gson.toJson(userCredentials);

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json"));

            Request request = new Request.Builder()
                    .url(MYURL+"/login")
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
                    nickname=map.get("nickname").toString();
                    sex=map.get("sex").toString();
                    address=map.get("address").toString();
                    idCard=map.get("idCard").toString();
                    phone= new BigDecimal(map.get("phone").toString()).toString();

                }else{
//                    mKqwSpeechSynthesizer.start("登陆失败,账号或密码输入错误");
                    username=null;
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };
    Runnable getAllUserRun = new Runnable() {
        @Override
        public void run() {

            User user1=new User();
            user1.setUsername(NIMClient.getCurrentAccount());
            Gson gson = new Gson();
            String json = gson.toJson(user1);

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json"));

            Request request = new Request.Builder()
                    .url(MYURL+"/getAllUser")
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
                    ArrayList<User> users = new ArrayList<>();
                    if (o instanceof ArrayList) {
                        ArrayList<Object> arrayList = (ArrayList<Object>) o;
                        for (Object item : arrayList) {
                            if (item instanceof LinkedTreeMap) {
                                LinkedTreeMap<String, Object> linkedTreeMap = (LinkedTreeMap<String, Object>) item;
                                // 从LinkedTreeMap中提取值
                                String username = (String) linkedTreeMap.get("username");
                                String password = (String) linkedTreeMap.get("password");
                                String nickname = (String) linkedTreeMap.get("nickname");
                                String sex = (String) linkedTreeMap.get("sex");
                                String phone=(String)linkedTreeMap.get("phone");
                                String idcard=(String)linkedTreeMap.get("idCard");
                                String address=(String)linkedTreeMap.get("address");
                                User userresult=new User(username,password,idcard,sex,nickname,address,phone);
                                users.add(userresult);
                            }
                        }
                    }
                    Intent intent=new Intent(InitActivity.this,ModifyInfoActivity.class);
                    Bundle bundle=new Bundle();
                    bundle.putString("username",username);
                    bundle.putString("nickname",nickname);
                    bundle.putString("sex",sex);
                    bundle.putString("address",address);
                    bundle.putString("idCard",idCard);
                    bundle.putString("phone",phone);
                    intent.putExtra("bundle",bundle);
                    intent.putParcelableArrayListExtra("users", users);
                    startActivity(intent);
                }else{
                    mKqwSpeechSynthesizer.start("获取失败");
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };
    Runnable getRequestRun = new Runnable() {
        @Override
        public void run() {
            User userCredentials = new User();
            userCredentials.setUsername(username);
            Gson gson = new Gson();
            String json = gson.toJson(userCredentials);
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url(MYURL+"/getRequest")
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
                Bundle bundle=new Bundle();
                bundle.putString("username",username);
                bundle.putString("nickname",nickname);
                bundle.putString("sex",sex);
                bundle.putString("address",address);
                bundle.putString("idCard",idCard);
                bundle.putString("phone",phone);
                if(code.equals("1")){
                    bundle.putStringArrayList("requests",friends);
                    Intent intent=new Intent(InitActivity.this,RequestActivity.class);
                    intent.putExtra("bundle",bundle);
                    startActivity(intent);
                }else{
                    Intent intent=new Intent(InitActivity.this,CommunityActivity.class);
                    intent.putExtra("bundle",bundle);
                    startActivity(intent);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    public static boolean shouldJumpToLoginActivity(){

        if (!App.hasLogined){
            //如果尚未登录过，需要进入登录页面，可以根据业务需要选择返回值。
            return true;
        }
        //已经登录过的情况下，当前状态不会走自动登录，需要重新登陆。
        StatusCode statusCode = NIMClient.getStatus();
        return statusCode.wontAutoLogin()||statusCode == StatusCode.LOGOUT;
    }
    @Override
    protected void onResume() {
        super.onResume();
        mKqwSpeechSynthesizer.start("这里是明阅辅助阅读平台首页");
    }
}