package com.wt.ocr;

import static com.wt.ocr.utils.JsonData.MYURL;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.gson.Gson;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.Team;
import com.wt.ocr.pojo.User;
import com.wt.ocr.utils.CustomAdapter;
import com.wt.ocr.utils.JsonData;
import com.wt.ocr.utils.KqwSpeechSynthesizer;
import com.wt.ocr.utils.OptionAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TeamListActivity extends AppCompatActivity {
    private KqwSpeechSynthesizer mKqwSpeechSynthesizer;
    private TextView spinnerTextView;
    private ListView friendList;
    private CustomAdapter adapter;
    private String username;
    //    private Spinner spinner;
    private OptionAdapter optionAdapter;
    private PopupWindow popupWindow;
    private ArrayList<String> teams=new ArrayList<>();
    private ArrayList<String> ids=new ArrayList<>();
    private ArrayList<String> friends;
    private ListView listView;
    private Button searchBtn;
    private EditText searchText;
    private String[] spinnerItems = {"添加好友","好友申请","创建群聊"};
    private String nickname;
    private String address;
    private String phone;
    private String idCard;
    private String sex;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_list);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mKqwSpeechSynthesizer=new KqwSpeechSynthesizer(this);
        Intent intentExtra=getIntent();
        Bundle bundleExtra=intentExtra.getBundleExtra("bundle");
        username=bundleExtra.getString("username");
        String nickname= bundleExtra.getString("nickname");
        String sex=bundleExtra.getString("sex");
        String address= bundleExtra.getString("address");
        String idCard=bundleExtra.getString("idCard");
        String phone= bundleExtra.getString("phone");
        friends=bundleExtra.getStringArrayList("friends");
        spinnerTextView = findViewById(R.id.sousuo_textview);
        listView=findViewById(R.id.teamList);
        searchBtn=findViewById(R.id.teamSearchBtn);
        searchText=findViewById(R.id.teamSearchText);

        NIMClient.getService(TeamService.class).queryTeamList().setCallback(new RequestCallback<List<Team>>() {
            @Override
            public void onSuccess(List<Team> teamlist) {
                // 获取成功，teams为加入的所有群组
                if(teamlist==null){
                    mKqwSpeechSynthesizer.start("您尚未加入任何群组");
                    return;
                }
                for (Team team:teamlist){
                    teams.add(team.getName());
                    ids.add(team.getId());
                }
                adapter = new CustomAdapter(TeamListActivity.this, teams,200,username,0,ids);
                listView.setAdapter(adapter);

            }

            @Override
            public void onFailed(int i) {
                // 获取失败，具体错误码见i参数
                mKqwSpeechSynthesizer.start("加载失败");
            }

            @Override
            public void onException(Throwable throwable) {
                // 获取异常
                mKqwSpeechSynthesizer.start("网络错误");
            }
        });
        // 设置TextView的长按监听事件
        // 创建PopupWindow来显示下拉列表
        View popupView = LayoutInflater.from(this).inflate(R.layout.spinner_popup, null);
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        // 创建适配器并填充下拉列表的选项
        optionAdapter = new OptionAdapter(this, R.layout.item_option, spinnerItems,username,friends,idCard,sex,nickname,address,phone);

        // 获取ListView
        ListView listView = popupView.findViewById(R.id.spinner_popup_listview);

        // 设置ListView的适配器
        listView.setAdapter(optionAdapter);


        // 显示下拉列表
        spinnerTextView.setOnClickListener(v -> {
            mKqwSpeechSynthesizer.start("这里是下拉列表，长按后可以选择添加好友，处理好友申请或创建群聊");
        });
        spinnerTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 在这里处理TextView的长按事件
                mKqwSpeechSynthesizer.start("下拉列表已展开");
                popupWindow.showAsDropDown(spinnerTextView);
                return true;
            }
        });

    }
    protected void onResume() {
        super.onResume();
        mKqwSpeechSynthesizer.start("您正在群聊列表界面");
    }

}