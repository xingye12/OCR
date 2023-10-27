package com.wt.ocr;

import static com.wt.ocr.utils.JsonData.MYURL;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.wt.ocr.pojo.User;
import com.wt.ocr.utils.CustomAdapter;
import com.wt.ocr.utils.JsonData;
import com.wt.ocr.utils.KqwSpeechSynthesizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TeamInfoActivity extends AppCompatActivity {
    private ListView listView;
    private TextView nameText;
    private TextView introduceText;
    private Button exitBtn;
    private Button addMemberBtn;
    private ArrayAdapter adapter;
    private ArrayList<String> members=new ArrayList<>();
    private String username;
    private String teamid;
    private String teamname;
    private KqwSpeechSynthesizer mKqwSpeechSynthesizer;

    private int flag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_info);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mKqwSpeechSynthesizer=new KqwSpeechSynthesizer(this);
        Intent intentExtra=getIntent();
        Bundle bundleExtra=intentExtra.getBundleExtra("bundle");
        teamid=bundleExtra.getString("teamid");
        username=bundleExtra.getString("username");
        teamname=bundleExtra.getString("team");
        nameText=findViewById(R.id.teamInfoNameText);
        introduceText=findViewById(R.id.teamInfoIntroText);
        exitBtn=findViewById(R.id.teamInfoExitBtn);
        listView=findViewById(R.id.teamMemberList);
        addMemberBtn=findViewById(R.id.teamInfoAddMemberBtn);
        nameText.setText(teamname);
        NIMClient.getService(TeamService.class).queryTeam(teamid).setCallback(new RequestCallback<Team>() {
            @Override
            public void onSuccess(Team result) {
                introduceText.setText(result.getIntroduce());
            }

            @Override
            public void onFailed(int code) {
                mKqwSpeechSynthesizer.start("该群不存在");
            }

            @Override
            public void onException(Throwable exception) {
                mKqwSpeechSynthesizer.start("网络错误");
            }
        });

        adapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,members);
        NIMClient.getService(TeamService.class).queryMemberList(teamid).setCallback(new RequestCallback<List<TeamMember>>() {
            @Override
            public void onSuccess(List<TeamMember> result) {
                for(TeamMember member:result){
                    String name=getNameById(member.getAccount());
                    members.add(name);
                }
                adapter=new ArrayAdapter(TeamInfoActivity.this,android.R.layout.simple_list_item_1,members);
                listView.setAdapter(adapter);

            }

            @Override
            public void onFailed(int code) {
                mKqwSpeechSynthesizer.start("失败"+code);
            }

            @Override
            public void onException(Throwable exception) {
                mKqwSpeechSynthesizer.start("网络错误");
            }
        });
        introduceText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKqwSpeechSynthesizer.start("这是群简介，长按可播放详细内容");
            }
        });
        introduceText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mKqwSpeechSynthesizer.start(introduceText.getText().toString());
                return true;
            }
        });
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKqwSpeechSynthesizer.start("这是退出群聊按钮，长安可退出群聊");
            }
        });
        exitBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                check();
                return true;
            }
        });
        addMemberBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKqwSpeechSynthesizer.start("这是添加群成员按钮，长按可从自己的好友中添加群成员");
            }
        });
        addMemberBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                flag=1;
                new Thread(getFriendRun).start();
                return true;
            }
        });
    }
    public String getNameById(String id){
        List<NimUserInfo> users=new ArrayList<>();
        users = NIMClient.getService(UserService.class).getAllUserInfo();
        for(NimUserInfo user:users){
            if(id.equals(user.getAccount())){
                return user.getName();
            }
        }
        return null;
    }
    private void check(){
//        mKqwSpeechSynthesizer.start("您确定要退出"+teamname+"群聊吗");
        Dialog dialog = new Dialog(TeamInfoActivity.this);
        dialog.setContentView(R.layout.custom_dialog);
        Button customPositiveButton = dialog.findViewById(R.id.custom_positive_button);
        Button negativeBtn=findViewById(R.id.custom_negative_button);
        EditText teamNameText=dialog.findViewById(R.id.dialog_textview);
        customPositiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 根据条件决定是否关闭对话框
                mKqwSpeechSynthesizer.start("这是确定按钮，长按将退出群聊"+teamname+"。您还可以输入退群理由");
            }
        });
        customPositiveButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String teamName = teamNameText.getText().toString();
                NIMClient.getService(TeamService.class).quitTeam(teamid).setCallback(new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {

                            mKqwSpeechSynthesizer.start("退群成功！");
                            flag=0;
                            new Thread(getFriendRun).start();
                        }

                        @Override
                        public void onFailed(int code) {
                            mKqwSpeechSynthesizer.start("退群失败！错误码"+code);
                        }

                        @Override
                        public void onException(Throwable exception) {
                            mKqwSpeechSynthesizer.start("网络错误");
                        }
                    });
                dialog.dismiss(); // 取消对话框
                return true;
            }
        });
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKqwSpeechSynthesizer.start("这是取消按钮，长按将取消群聊创建");
            }
        });
        negativeBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                dialog.dismiss();
                return true;
            }
        });
//        AlertDialog.Builder builder = new AlertDialog.Builder(TeamInfoActivity.this);
//        builder.setTitle("您确认要退出群聊吗？");
//
//        // 创建一个EditText视图
//        final EditText input = new EditText(TeamInfoActivity.this);
//        input.setInputType(InputType.TYPE_CLASS_TEXT); // 设置输入类型为文本
//
//        builder.setView(input);
//
//        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
////                String teamName = input.getText().toString();
//                // 在这里处理用户输入的文本，例如将其显示在UI上或执行其他操作
//                // userInput 中包含了用户输入的文本
//                Dialog dialog = new Dialog(TeamInfoActivity.this);
//                dialog.setContentView(R.layout.custom_dialog);
//                Button customPositiveButton = dialog.findViewById(R.id.custom_positive_button);
//                Button negativeBtn=findViewById(R.id.custom_negative_button);
//                EditText teamNameText=dialog.findViewById(R.id.dialog_textview);
//                customPositiveButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        // 根据条件决定是否关闭对话框
//                        mKqwSpeechSynthesizer.start("这是确定按钮，长按将退出群聊");
//                    }
//                });
//                customPositiveButton.setOnLongClickListener(new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View v) {
//                        String teamName = teamNameText.getText().toString();
//
//                        dialog.dismiss(); // 取消对话框
//                        return true;
//                    }
//                });
//                negativeBtn.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        mKqwSpeechSynthesizer.start("这是取消按钮，长按将取消群聊创建");
//                    }
//                });
//                negativeBtn.setOnLongClickListener(new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View v) {
//                        dialog.dismiss();
//                        return true;
//                    }
//                });
////                if(yseflag==0){
////                    mKqwSpeechSynthesizer.start("这是确定按钮,再次点击可确认退群");
////                    yseflag=1;
////                } else if (yseflag==1) {
////                    NIMClient.getService(TeamService.class).quitTeam(teamid).setCallback(new RequestCallback<Void>() {
////                        @Override
////                        public void onSuccess(Void result) {
////
////                            mKqwSpeechSynthesizer.start("退群成功！");
////                            flag=0;
////                            new Thread(getFriendRun).start();
////                        }
////
////                        @Override
////                        public void onFailed(int code) {
////                            mKqwSpeechSynthesizer.start("退群失败！错误码"+code);
////                        }
////
////                        @Override
////                        public void onException(Throwable exception) {
////                            mKqwSpeechSynthesizer.start("网络错误");
////                        }
////                    });
////                    yseflag=0;
////                }
//
//            }
//
//
//        });
//        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel(); // 取消对话框
//            }
//        });
//
//        builder.show();
    }
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
                Bundle bundle=new Bundle();
                bundle.putString("username",username);
                bundle.putStringArrayList("friends",friends);
                bundle.putString("teamid",teamid);
                bundle.putString("teamname",teamname);
                bundle.putStringArrayList("teamMember",members);
                Intent intent=new Intent(TeamInfoActivity.this,AddTeamMemberActivity.class);
                intent.putExtra("bundle",bundle);
                startActivity(intent);
                finish();



            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };
    protected void onResume() {
        super.onResume();
        mKqwSpeechSynthesizer.start("您正在"+teamname+"的群信息界面中");
    }
}