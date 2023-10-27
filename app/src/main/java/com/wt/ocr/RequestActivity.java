package com.wt.ocr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.SystemMessageService;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.netease.nimlib.sdk.team.model.Team;
import com.wt.ocr.pojo.User;
import com.wt.ocr.utils.KqwSpeechSynthesizer;
import com.wt.ocr.utils.RequestAdapter;

import java.util.ArrayList;
import java.util.List;

public class RequestActivity extends AppCompatActivity {
    private String username;
    private String phone;
    private String nickname;
    private String sex;
    private String address;
    private String idCard;
    private ListView requestListView;
    private KqwSpeechSynthesizer mKqwSpeechSynthesizer;
    private RequestAdapter adapter;
    private Button friendBtn;
    private Button teamBtn;
    private ArrayList<String> teamMessages=new ArrayList<>();
    private List<SystemMessage> teamMessagesId;
    private int flag;
//    private Button backBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_request);
        NIMClient.getService(SystemMessageService.class).querySystemMessages(0,20).setCallback(new RequestCallback<List<SystemMessage>>() {
            @Override
            public void onSuccess(List<SystemMessage> result) {
                System.out.println(1);
                for(SystemMessage msg:result){

                    String inviter=msg.getFromAccount();
                    String teamid=msg.getTargetId();
                    String type=msg.getType().toString();
                    String status=msg.getStatus().toString();
                    if(type.equals("TeamInvite")&&status.equals("init")){
                        String content=inviter+"邀请您加入"+teamid;
                        teamMessages.add(content);
                    }
                }
                teamMessagesId=result;
            }

            @Override
            public void onFailed(int code) {
                mKqwSpeechSynthesizer.start("错误");
            }

            @Override
            public void onException(Throwable exception) {
                mKqwSpeechSynthesizer.start("网络错误");
            }
        });
        mKqwSpeechSynthesizer = new KqwSpeechSynthesizer(this);
        mKqwSpeechSynthesizer.start("这里好友申请处理界面");
        Intent intentExtra=getIntent();
        Bundle bundleExtra=intentExtra.getBundleExtra("bundle");
        nickname= bundleExtra.getString("nickname");
        username= bundleExtra.getString("username");
        sex=bundleExtra.getString("sex");
        address= bundleExtra.getString("address");
        idCard=bundleExtra.getString("idCard");
        phone= bundleExtra.getString("phone");
        User user=new User(username,"123456",idCard,sex,nickname,address,phone);
        ArrayList<String> data=bundleExtra.getStringArrayList("requests");
        requestListView=findViewById(R.id.requestListView);
        friendBtn=findViewById(R.id.requestFriendBtn);
        teamBtn=findViewById(R.id.requestTeamBtn);
//        backBtn=findViewById(R.id.requestBackBtn);
        adapter=new RequestAdapter(this, data,user,0);
        requestListView.setAdapter(adapter);
        friendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKqwSpeechSynthesizer.start("这是进入好友申请列表的按钮");
            }
        });
        friendBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(flag==0)return true;
                flag=0;
                adapter=new RequestAdapter(RequestActivity.this,data,user,0);
                requestListView.setAdapter(adapter);
                return true;
            }
        });
        teamBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKqwSpeechSynthesizer.start("这是显示群相关消息列表的按钮");
            }
        });
        teamBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(flag==1)return true;
                flag=1;
                adapter=new RequestAdapter(RequestActivity.this,teamMessages,user,1,teamMessagesId);
                requestListView.setAdapter(adapter);
                return true;
            }
        });
//        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(flag==0)
        mKqwSpeechSynthesizer.start("这里是好友申请处理界面");
        else if (flag==1) {
            mKqwSpeechSynthesizer.start("这里是群相关信息申请处理界面");
        }
    }
}