package com.wt.ocr;

import static com.wt.ocr.utils.JsonData.MYURL;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.wt.ocr.pojo.Boke;
import com.wt.ocr.utils.JsonData;
import com.wt.ocr.utils.KqwSpeechSynthesizer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FabuActivity extends AppCompatActivity {
    private EditText titleText;
    private EditText contentText;
    private Spinner fabuSpinner;
    private Button fabuBtn;
    private Button backBtn;
    private ArrayAdapter<String> adapter;
    private KqwSpeechSynthesizer mKqwSpeechSynthesizer;
    private String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_fabu);
        mKqwSpeechSynthesizer = new KqwSpeechSynthesizer(this);
        mKqwSpeechSynthesizer.start("这里是博客发布界面");
        Intent intentExtra=getIntent();
        Bundle bundleExtra=intentExtra.getBundleExtra("bundle");
        username=bundleExtra.getString("username");
        titleText=findViewById(R.id.fabuTitleText);
        contentText=findViewById(R.id.fabuContentText);
        fabuSpinner=findViewById(R.id.fabuSpinner);
        backBtn=findViewById(R.id.fabuBackBtn);
        fabuBtn=findViewById(R.id.fabuYesBtn);
        ArrayList<String> typeDataList=new ArrayList<>();
        typeDataList.add("生活日常");
        typeDataList.add("购物");
        typeDataList.add("教育");
        typeDataList.add("其它");
        adapter=new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typeDataList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fabuSpinner.setAdapter(adapter);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这是撤销发布按钮");
            }
        });
        backBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mKqwSpeechSynthesizer.start("回到明阅社区论坛首页");
                finish();
                return true;
            }
        });
        fabuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这是发布按钮");
            }
        });
        fabuBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (TextUtils.isEmpty(titleText.getText().toString())) {
                    mKqwSpeechSynthesizer.start("标题不可为空");
                    return true;
                } else if (TextUtils.isEmpty(contentText.getText().toString())) {
                    mKqwSpeechSynthesizer.start("内容不可为空");
                    return true;
                } else if (TextUtils.isEmpty(fabuSpinner.getSelectedItem().toString())) {
                    mKqwSpeechSynthesizer.start("请选择类别后再发布");
                    return true;
                } else {
                    new Thread(fabuRun).start();
                    return true;
                }
            }

        });
    }
    Runnable searchRun = new Runnable() {
        @Override
        public void run() {

            Boke boke=new Boke();
            boke.setTitle(titleText.getText().toString().trim());
            boke.setType(fabuSpinner.getSelectedItem().toString());
            boke.setContent(contentText.getText().toString());
            Date currentDate = new Date(); // 创建当前时间对象
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 创建日期格式化对象，指定要使用的格式
            String formattedDate = sdf.format(currentDate);
            boke.setTime(formattedDate);
            boke.setNickname(username);
            Gson gson = new Gson();
            String json = gson.toJson(boke);
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url(MYURL+"/searchBoke")
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
                    Object o = jsonData.getData();
                    ArrayList<Boke> bokes = new ArrayList<>();
                    if (o instanceof ArrayList) {
                        ArrayList<?> arrayList = (ArrayList<?>) o;
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
                    Intent intent=new Intent(FabuActivity.this,BokeActivity.class);
                    intent.putParcelableArrayListExtra("bokeList", bokes);
                    Bundle bundle=new Bundle();
                    bundle.putString("username",username);
                    intent.putExtra("bundle",bundle);
                    startActivity(intent);
                }else{
                    mKqwSpeechSynthesizer.start("搜索失败，没有符合条件的博客");
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };
    Runnable fabuRun = new Runnable() {
        @Override
        public void run() {

            Boke boke=new Boke();
            boke.setTitle(titleText.getText().toString().trim());
            boke.setType(fabuSpinner.getSelectedItem().toString());
            boke.setContent(contentText.getText().toString());
            Date currentDate = new Date(); // 创建当前时间对象
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 创建日期格式化对象，指定要使用的格式
            String formattedDate = sdf.format(currentDate);
            boke.setTime(formattedDate);
            boke.setNickname(username);
            Gson gson = new Gson();
            String json = gson.toJson(boke);
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url(MYURL+"/fabu")
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
                    Object o = jsonData.getData();
                    ArrayList<Boke> bokes = new ArrayList<>();
                    if (o instanceof ArrayList) {
                        ArrayList<?> arrayList = (ArrayList<?>) o;
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
                    Intent intent=new Intent(FabuActivity.this,BokeActivity.class);
                    intent.putParcelableArrayListExtra("bokeList", bokes);
                    Bundle bundle=new Bundle();
                    bundle.putString("username",username);
                    intent.putExtra("bundle",bundle);
                    startActivity(intent);
                }else{
                    mKqwSpeechSynthesizer.start("发布失败");
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        mKqwSpeechSynthesizer.start("这里是博客发布界面");
    }
}