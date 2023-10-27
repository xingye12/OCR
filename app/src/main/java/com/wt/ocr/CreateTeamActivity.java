package com.wt.ocr;

import static com.netease.nimlib.sdk.team.constant.TeamBeInviteModeEnum.NoAuth;
import static com.netease.nimlib.sdk.team.constant.TeamMessageNotifyTypeEnum.All;
import static com.netease.nimlib.sdk.team.constant.VerifyTypeEnum.Free;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.SparseBooleanArray;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.CreateTeamResult;
import com.netease.nimlib.sdk.team.model.Team;
import com.wt.ocr.pojo.User;
import com.wt.ocr.utils.KqwSpeechSynthesizer;
import com.wt.ocr.utils.fileUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CreateTeamActivity extends AppCompatActivity {
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private SparseBooleanArray selectedItems;
    private Button selectButton;
    private ArrayList<String> friends;
    private ArrayList<User> users;
    private KqwSpeechSynthesizer mKqwSpeechSynthesizer;
    private String username;
    private int nameflag;
    private int introduceflag;
    private int yesflag1;
    private int yesflag2;
    private ArrayList<String> nicknameList=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_team);
        mKqwSpeechSynthesizer=new KqwSpeechSynthesizer(this);
        listView=findViewById(R.id.CreateTeamListView);
        Intent intentExtra=getIntent();
        users=intentExtra.getParcelableArrayListExtra("users");
        username=intentExtra.getBundleExtra("bundle").getString("username");
        friends=intentExtra.getBundleExtra("bundle").getStringArrayList("friends");
        selectButton = findViewById(R.id.selectButton);
        nameflag=0;introduceflag=0;yesflag1=0;yesflag2=0;
        // 假设这是您的数据源
//        String[] items = {"Item 1", "Item 2", "Item 3", "Item 4", "Item 5"};

        // 初始化适配器和选择状态数组
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, friends);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(adapter);
        selectedItems = new SparseBooleanArray();

        // 设置 ListView 项点击事件监听器
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 切换选择状态
                selectedItems.put(position, !selectedItems.get(position));
                mKqwSpeechSynthesizer.start("您选择了"+friends.get(position));
                nicknameList.add(friends.get(position));
            }
        });

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理选中的项目
               ArrayList<String> accounts=new ArrayList<>();
               accounts.add(username);
               for(User user:users){
                   for(int i=0;i< nicknameList.size();i++){
                       if(nicknameList.get(i).equals(user.getNickname())){
                           accounts.add(user.getUsername());
                           break;
                       }
                   }
               }
              writeTeamName(CreateTeamActivity.this,accounts);
    }
    private void writeTeamName(Context context, ArrayList<String> accounts) {
        mKqwSpeechSynthesizer.start("请填写群名称");
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.custom_dialog);
        Button customPositiveButton = dialog.findViewById(R.id.custom_positive_button);
        Button negativeBtn=findViewById(R.id.custom_negative_button);
        EditText teamNameText=dialog.findViewById(R.id.dialog_textview);
        customPositiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 根据条件决定是否关闭对话框
                    mKqwSpeechSynthesizer.start("这是确定按钮，长按确定您创建的群名称");
            }
        });
        customPositiveButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                    String teamName = teamNameText.getText().toString();
                    writeIntroduce(context, teamName, accounts);
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
// 显示自定义对话框
        dialog.show();
    }
    private void writeIntroduce(Context context,String teamName, ArrayList<String> accounts) {
                mKqwSpeechSynthesizer.start("请填写群简介,然后点击确定按钮");
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.custom_dialog);
        Button customPositiveButton = dialog.findViewById(R.id.custom_positive_button);
        Button negativeBtn=findViewById(R.id.custom_negative_button);
        EditText teamNameText=dialog.findViewById(R.id.dialog_textview);
        customPositiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 根据条件决定是否关闭对话框
                mKqwSpeechSynthesizer.start("这是确定按钮，长按确定您创建的群名称");
            }
        });
        customPositiveButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String introduce = teamNameText.getText().toString();
                    TeamTypeEnum type = TeamTypeEnum.Advanced;
// 创建时可以预设群组的一些相关属性。
// fields 中，key 为数据字段，value 对对应的值，该值类型必须和 field 中定义的 fieldType 一致
                    HashMap<TeamFieldEnum, Serializable> fields = new HashMap<TeamFieldEnum, Serializable>();
                    fields.put(TeamFieldEnum.Name, teamName);
                    fields.put(TeamFieldEnum.Introduce, introduce);
                    fields.put(TeamFieldEnum.VerifyType,Free);
                    fields.put(TeamFieldEnum.BeInviteMode,NoAuth);
                    fields.put(TeamFieldEnum.InviteMode,All);
                    yesflag2=0;
                    NIMClient.getService(TeamService.class).createTeam(fields, type, "", accounts)
                            .setCallback(new RequestCallback<CreateTeamResult>() {
                                @Override
                                public void onSuccess(CreateTeamResult result) {
                                    mKqwSpeechSynthesizer.start("群聊"+result.getTeam().getName()+"创建成功！");

                                }

                                @Override
                                public void onFailed(int code) {
                                    mKqwSpeechSynthesizer.start("群聊创建失败！");
                                }

                                @Override
                                public void onException(Throwable exception) {
                                    mKqwSpeechSynthesizer.start("网络异常！");
                                }
                            });
                    dialog.cancel(); // 取消对话框

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
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle("填写群聊简介");
//
//        // 创建一个EditText视图
//        final EditText input = new EditText(context);
//        input.setInputType(InputType.TYPE_CLASS_TEXT); // 设置输入类型为文本
//
//        builder.setView(input);
//
//        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                if(yesflag2==0){
//                    mKqwSpeechSynthesizer.start("这是确定按钮，再次点击将确认创建群聊");
//                    yesflag2=1;
//                } else if (yesflag2==1) {
//                    String introduce = input.getText().toString();
//                    TeamTypeEnum type = TeamTypeEnum.Advanced;
//// 创建时可以预设群组的一些相关属性。
//// fields 中，key 为数据字段，value 对对应的值，该值类型必须和 field 中定义的 fieldType 一致
//                    HashMap<TeamFieldEnum, Serializable> fields = new HashMap<TeamFieldEnum, Serializable>();
//                    fields.put(TeamFieldEnum.Name, teamName);
//                    fields.put(TeamFieldEnum.Introduce, introduce);
//                    fields.put(TeamFieldEnum.VerifyType,Free);
//                    fields.put(TeamFieldEnum.BeInviteMode,NoAuth);
//                    yesflag2=0;
//                    NIMClient.getService(TeamService.class).createTeam(fields, type, "", accounts)
//                            .setCallback(new RequestCallback<CreateTeamResult>() {
//                                @Override
//                                public void onSuccess(CreateTeamResult result) {
//                                    mKqwSpeechSynthesizer.start("群聊"+result.getTeam().getName()+"创建成功！");
//
//                                }
//
//                                @Override
//                                public void onFailed(int code) {
//                                    mKqwSpeechSynthesizer.start("群聊创建失败！");
//                                }
//
//                                @Override
//                                public void onException(Throwable exception) {
//                                    mKqwSpeechSynthesizer.start("网络异常！");
//                                }
//                            });
//                    dialog.cancel(); // 取消对话框
//                }
//
//            }
//        });
//        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//                if(introduceflag==0){
//                    mKqwSpeechSynthesizer.start("这是取消按钮,再次点击该按钮将取消群聊创建");
//                    introduceflag=1;
//                }
//                else if (introduceflag==1) {
//                    mKqwSpeechSynthesizer.start("您已取消群聊创建操作");
//                    dialog.cancel(); // 取消对话框
//                    introduceflag=0;
//                }
//            }
//        });
//
//        builder.show();
//                // 在这里处理用户输入的文本，例如将其显示在UI上或执行其他操作
//                // userInput 中包含了用户输入的文本
            }
        });

    }
}