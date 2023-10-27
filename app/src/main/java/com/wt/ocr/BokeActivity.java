package com.wt.ocr;

import static com.iflytek.cloud.SpeechConstant.APPID;
import static com.wt.ocr.InitActivity.flag;
import static com.wt.ocr.utils.JsonData.MYURL;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.wt.ocr.pojo.Boke;
import com.wt.ocr.pojo.User;
import com.wt.ocr.utils.BokeAdapter;
import com.wt.ocr.utils.DictationResult;
import com.wt.ocr.utils.JsonData;
import com.wt.ocr.utils.KqwSpeechSynthesizer;

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

public class BokeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Spinner typeSpinner;
    private String username;
    private ArrayAdapter<String> typeAdapter;
    private List<String> typeDataList;
    private EditText searchText;
    private Button searchBtn;
    private Button fabuBtn;
    private Button guanzhuListBtn;
    private Button lookAllBtn;
    private ImageButton speakBtn;
    private KqwSpeechSynthesizer mKqwSpeechSynthesizer;
    private String type;
    private ArrayList<Boke> bokeList;
    private ListView bokeListView;
    private BokeAdapter bokeAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_boke);
        Intent receivedIntent = getIntent();
        Bundle bundleExtra=receivedIntent.getBundleExtra("bundle");
        username=bundleExtra.getString("username");
        ArrayList<Boke> receivedBokeList = receivedIntent.getParcelableArrayListExtra("bokeList");
        bokeList=new ArrayList<>();
        if (receivedBokeList != null) {
            bokeList=receivedBokeList;
        }
        mKqwSpeechSynthesizer = new KqwSpeechSynthesizer(this);
        mKqwSpeechSynthesizer.start("这里是明阅社区论坛");
        typeDataList = new ArrayList<>();
        typeDataList.add("全部");
        typeDataList.add("生活日常");
        typeDataList.add("购物");
        typeDataList.add("教育");
        typeDataList.add("其它");
        bokeListView=findViewById(R.id.bokeListView);
        BokeAdapter bokeAdapter = new BokeAdapter(this, bokeList,username);
        bokeListView.setAdapter(bokeAdapter);
        speakBtn=findViewById(R.id.bokeSpeakBtn);
        searchBtn=findViewById(R.id.bokeSearchBtn);
        searchText=findViewById(R.id.bokeSearchText);
        fabuBtn=findViewById(R.id.fabuBtn);
        typeSpinner=findViewById(R.id.bokeSpinner);
        guanzhuListBtn=findViewById(R.id.guanzhuListBtn);
        lookAllBtn=findViewById(R.id.bokeLookAllBtn);
        typeAdapter=new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typeDataList);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);
        // 设置默认选中项
        typeSpinner.setSelection(0);
        lookAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这里是进入广场按钮，进入后可以看到所有人的动态");
            }
        });
        lookAllBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new Thread(enterBokeRun).start();
                return true;
            }
        });
        guanzhuListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这里是关注列表，长按可查看关注的人的动态");
            }
        });
        guanzhuListBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new Thread(searchGuanzhuRun).start();
                return true;
            }
        });
        speakBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] dictationResultStr = {"["};
                // 语音配置对象初始化
                SpeechUtility.createUtility(BokeActivity.this, APPID
                        + "=" + APPID);
                // 1.创建SpeechRecognizer对象，第2个参数：本地听写时传InitListener
                SpeechRecognizer mIat = SpeechRecognizer.createRecognizer(
                        BokeActivity.this, null);
                // 交互动画
                RecognizerDialog iatDialog = new RecognizerDialog(
                        BokeActivity.this, null);
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
                            dictationResultStr[0] += results.getResultString() + ",";
                        } else {
                            dictationResultStr[0] += results.getResultString() + "]";
                        }
                        if (isLast) {
                            // 解析Json列表字符串
                            Gson gson = new Gson();
                            List<DictationResult> dictationResultList = gson
                                    .fromJson(dictationResultStr[0],
                                            new TypeToken<List<DictationResult>>() {
                                            }.getType());
                            String finalResult = "";
                            for (int i = 0; i < dictationResultList.size() - 1; i++) {
                                finalResult += dictationResultList.get(i)
                                        .toString();
                            }
                            searchText.setText(finalResult);
                            mKqwSpeechSynthesizer.start("语音内容是："+finalResult);
                            //获取焦点
                            searchText.requestFocus();

                            //将光标定位到文字最后，以便修改
                            searchText.setSelection(finalResult.length());

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

            }
        });
        searchBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new Thread(searchRun).start();
                return true;
            }
        });
        fabuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这是发布博客按钮，长按可进入您准备发布内容的编辑界面");
            }
        });
        fabuBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent intent=new Intent(BokeActivity.this,FabuActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("username",username);
                intent.putExtra("bundle",bundle);
                startActivity(intent);
                return true;
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        type = typeDataList.get(i);
        System.out.println(type);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
    Runnable searchRun = new Runnable() {
        @Override
        public void run() {

            Boke boke=new Boke();
            boke.setTitle(searchText.getText().toString().trim());
            boke.setType(typeSpinner.getSelectedItem().toString());
            System.out.println(boke);
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
                    Intent intent=new Intent(BokeActivity.this,BokeActivity.class);
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
                    Intent intent=new Intent(BokeActivity.this,BokeActivity.class);
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
    Runnable searchGuanzhuRun = new Runnable() {
        @Override
        public void run() {
           User user=new User();
           user.setUsername(username);
            Gson gson = new Gson();
            String json = gson.toJson(user);
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url(MYURL+"/searchGuanzhuBoke")
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
                    Intent intent=new Intent(BokeActivity.this,BokeActivity.class);
                    intent.putParcelableArrayListExtra("bokeList", bokes);
                    Bundle bundle=new Bundle();
                    bundle.putString("username",username);
                    intent.putExtra("bundle",bundle);
                    startActivity(intent);
                }else{
                    mKqwSpeechSynthesizer.start("您还没有关注他人哦，请先关注您感兴趣的人叭");
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        mKqwSpeechSynthesizer.start("这里是博客界面");
    }
}