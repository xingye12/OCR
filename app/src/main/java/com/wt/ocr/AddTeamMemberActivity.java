package com.wt.ocr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.wt.ocr.pojo.User;
import com.wt.ocr.utils.KqwSpeechSynthesizer;

import java.util.ArrayList;
import java.util.List;

public class AddTeamMemberActivity extends AppCompatActivity {
    private ListView listView;
    private Button inviteBtn;
    private KqwSpeechSynthesizer mKqwSpeechSynthesizer;
    private ArrayAdapter<String> adapter;
    private String username;
    private ArrayList<String> friends;
    private ArrayList<String> selectedItems;
    private List<NimUserInfo> users;
    private ArrayList<String> teamMembers;
    private ArrayList<String> someFriends;
    private ArrayList<String> userlist=new ArrayList<>();
    private String teamid;
    private String teamname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_team_member);
        Intent intentExtra=getIntent();
        Bundle bundleExtra=intentExtra.getBundleExtra("bundle");
        username=bundleExtra.getString("username");
        friends=bundleExtra.getStringArrayList("friends");
        teamid=bundleExtra.getString("teamid");
        teamname=bundleExtra.getString("teamname");
        teamMembers=bundleExtra.getStringArrayList("teamMember");
        someFriends=new ArrayList<>();
        if(friends!=null){
            for(String friend:friends){
                int flag=0;
                for(int i=0;i<teamMembers.size();i++){
                    if(friend.equals(teamMembers.get(i))){
                        flag=1;
                        break;
                    }
                }
                if(flag==0) someFriends.add(friend);
            }
        }

        users=NIMClient.getService(UserService.class).getAllUserInfo();
        mKqwSpeechSynthesizer=new KqwSpeechSynthesizer(this);
        listView=findViewById(R.id.addMemberLstView);
        inviteBtn=findViewById(R.id.addMemberInviteBtn);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, someFriends);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(adapter);
        selectedItems=new ArrayList<>();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mKqwSpeechSynthesizer.start("这是"+friends.get(position));
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                selectedItems.add(friends.get(position));
                mKqwSpeechSynthesizer.start("您选择了"+friends.get(position));
                return true;
            }
        });
        inviteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKqwSpeechSynthesizer.start("这是邀请按钮，长按可邀请您选择的成员加入该群");
            }
        });
        inviteBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                for(NimUserInfo nimUserInfo:users){
                    for(int i=0;i<friends.size();i++){
                        if(friends.get(i).equals(nimUserInfo.getAccount())){
                            userlist.add(nimUserInfo.getAccount());
                            break;
                        }
                    }

                }
                NIMClient.getService(TeamService.class).addMembersEx(teamid,userlist,"","").setCallback(new RequestCallback<List<String>>() {
                    @Override
                    public void onSuccess(List<String> result) {

                        mKqwSpeechSynthesizer.start("邀请成功！邀请到了"+result.toString());
                    }

                    @Override
                    public void onFailed(int code) {
                        if(code==802){
                            mKqwSpeechSynthesizer.start("只有管理员有权限邀请其他成员入群");
                            return;
                        }

                        mKqwSpeechSynthesizer.start("邀请失败,错误码"+code);
                    }

                    @Override
                    public void onException(Throwable exception) {
                        mKqwSpeechSynthesizer.start("网络错误");
                    }
                });
                finish();
                return true;
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        mKqwSpeechSynthesizer.start("这里是"+teamname+"的邀请成员界面");
    }

}