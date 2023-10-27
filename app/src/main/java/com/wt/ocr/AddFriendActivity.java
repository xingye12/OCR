package com.wt.ocr;

import static com.wt.ocr.utils.JsonData.MYURL;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
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

public class AddFriendActivity extends AppCompatActivity {
    private String username;
    private String nickname;
    private EditText searchText;
    private Button searchBtn;
    private Button backBtn;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String > friends;
    private KqwSpeechSynthesizer mKqwSpeechSynthesizer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_add_friend);
        mKqwSpeechSynthesizer=new KqwSpeechSynthesizer(this);
        mKqwSpeechSynthesizer.start("这里是添加好友界面");
        friends=new ArrayList<>();
        Intent intentExtra=getIntent();
        Bundle bundleExtra=intentExtra.getBundleExtra("bundle");
        username=bundleExtra.getString("username");
        friends=bundleExtra.getStringArrayList("friends");
        ArrayList<User> users=intentExtra.getParcelableArrayListExtra("users");
        searchBtn=findViewById(R.id.addFriendSearchBtn);
        searchText=findViewById(R.id.addFriendSearchText);
        backBtn=findViewById(R.id.addFriendBackBtn);
        listView=findViewById(R.id.addFriendListView);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这是搜索好友按钮");
            }
        });
        searchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这是好友搜索框，请输入您想要添加的好友的昵称");
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这是返回按钮");
            }
        });
        searchBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(TextUtils.isEmpty(searchText.getText().toString())){
                    mKqwSpeechSynthesizer.start("请先输入想要添加为好友的人的昵称");
                    return true;
                }
                int count=0;
                ArrayList<String>names=new ArrayList<>();
                String content="";
                for(User user:users){
                    if(count<=5){
                        if(user.getNickname().contains(searchText.getText().toString())){
                            names.add(user.getNickname());
                            content+=user.getNickname()+",";
                            count++;
                        }
                    }
                    else break;

                }
                adapter=new ArrayAdapter<>(AddFriendActivity.this,android.R.layout.simple_list_item_1,names);
                listView.setAdapter(adapter);
                mKqwSpeechSynthesizer.start("搜索到"+count+"个类似的用户，分别是"+content);
                return true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mKqwSpeechSynthesizer.start("这是"+users.get(i).getNickname());
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                nickname=adapter.getItem(i);
                for(String friend:friends){
                    if (nickname.equals(friend)){
                        mKqwSpeechSynthesizer.start(nickname+"已经是您的好友，不可重复添加");
                        return true;
                    }
                }
                new Thread(addFriendRun).start();
                return true;
            }
        });
        backBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                finish();
                return true;
            }
        });
    }
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
    @Override
    protected void onResume() {
        super.onResume();
        mKqwSpeechSynthesizer.start("这里是添加好友界面");
    }
}