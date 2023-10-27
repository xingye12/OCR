package com.wt.ocr;

import static com.wt.ocr.utils.JsonData.MYURL;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.wt.ocr.pojo.Boke;
import com.wt.ocr.pojo.User;
import com.wt.ocr.utils.JsonData;
import com.wt.ocr.utils.KqwSpeechSynthesizer;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class detailActivity extends AppCompatActivity {
    private KqwSpeechSynthesizer mKqwSpeechSynthesizer;
    private String username;
    private String nickname;
    private TextView contentText;
    private TextView typeText;
    private TextView nameText;
    private TextView timeText;
    private TextView titleText;
    private Button guanzhuBtn;
    private Button backBtn;
    private Button addFriendBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_detail);
        Intent intentExtra=getIntent();
        Bundle bundleExtra=intentExtra.getBundleExtra("bundle");
        username= bundleExtra.getString("username");
        nickname= bundleExtra.getString("nickname");
        String time= bundleExtra.getString("time");
        String title= bundleExtra.getString("title");
        String content= bundleExtra.getString("content");
        String type= bundleExtra.getString("type");
        mKqwSpeechSynthesizer = new KqwSpeechSynthesizer(this);
        mKqwSpeechSynthesizer.start("欢迎查看"+nickname+"的动态,标题是"+title+",发布于"+time+",类别为"+type+",动态的内容是"+content);
        titleText=findViewById(R.id.detailTitleText);
        contentText=findViewById(R.id.detailContentText);
        typeText=findViewById(R.id.detailTypeText);
        timeText=findViewById(R.id.detailTimeText);
        nameText=findViewById(R.id.detailNameText);
        backBtn=findViewById(R.id.detailBackBtn);
        addFriendBtn=findViewById(R.id.detailAddFriendBtn);
        guanzhuBtn=findViewById(R.id.guanzhuBtn);
        contentText.setText(content);
        if(type!=null) typeText.setText(type);
        timeText.setText(time);
        nameText.setText(nickname);
        titleText.setText(title);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这里是返回按钮");
            }
        });
        backBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new Thread(enterBokeRun).start();
                return true;
            }
        });
        addFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这里是添加好友按钮,长按可向对方发出好友申请");
            }
        });
        addFriendBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new Thread(addFriendRun).start();
                return true;
            }
        });
        guanzhuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这里是关注按钮,长按可关注博主");
            }
        });
        guanzhuBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new Thread(guanzhuRun).start();
                return true;
            }
        });
    }
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
                    Intent intent=new Intent(detailActivity.this,BokeActivity.class);
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
    Runnable addFriendRun = new Runnable() {
        @Override
        public void run() {
            User user=new User();
            user.setUsername(username);
            user.setNickname(nickname);
            Gson gson = new Gson();
            String json = gson.toJson(user);

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json"));

            Request request = new Request.Builder()
                    .url(MYURL+"/addFriend")
                    .post(requestBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String jsonResponse = response.body().string();
                Gson backGson = new Gson();
                JsonData jsonData = backGson.fromJson(jsonResponse, JsonData.class);
                String code = jsonData.getCode();
                String msg = jsonData.getMsg();
                if(code.equals("1")){
                    mKqwSpeechSynthesizer.start("好友请求发送成功！");
                }else{
                    mKqwSpeechSynthesizer.start("对方已经是您的好友");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };
    Runnable guanzhuRun = new Runnable() {
        @Override
        public void run() {
            User user=new User();
            user.setUsername(username);
            user.setNickname(nickname);
            Gson gson = new Gson();
            String json = gson.toJson(user);

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json"));

            Request request = new Request.Builder()
                    .url(MYURL+"/guanzhu")
                    .post(requestBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String jsonResponse = response.body().string();
                Gson backGson = new Gson();
                JsonData jsonData = backGson.fromJson(jsonResponse, JsonData.class);
                String code = jsonData.getCode();
                String msg = jsonData.getMsg();
                if(code.equals("1")){
                    mKqwSpeechSynthesizer.start("关注成功！");
                }else{
                    mKqwSpeechSynthesizer.start("您已经关注过对方");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        mKqwSpeechSynthesizer.start("这里是"+nickname+"的博客详情界面");
    }
}