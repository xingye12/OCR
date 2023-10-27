package com.wt.ocr;

import static com.wt.ocr.ChatActivity.verifyAudioPermissions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.wt.ocr.utils.FileAdapter;
import com.wt.ocr.utils.KqwSpeechSynthesizer;
import com.wt.ocr.utils.Message;
import com.wt.ocr.utils.MessageAdapter;

import java.util.ArrayList;
import java.util.List;

public class TeamChatActivity extends AppCompatActivity {
    private String username;
    private String team;
    private String teamid;
    private ImageButton listenBtn;
    private Button sendBtn;
    private EditText contentEt;
    private Button teamInfoBtn;
    private KqwSpeechSynthesizer mKqwSpeechSynthesizer;
    private RecyclerView mRecyclerView;
    private List<Message> mMessages=new ArrayList<>();
    private MessageAdapter mAdapter;
    private List<NimUserInfo> users;
    private static final int GET_RECODE_AUDIO = 1;
    private static final int ONE_QUERY_LIMIT = 20;
    private static String[] PERMISSION_AUDIO = {
            Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_chat);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mKqwSpeechSynthesizer=new KqwSpeechSynthesizer(this);
        Intent intentExtra=getIntent();
        Bundle bundleExtra=intentExtra.getBundleExtra("bundle");
        username= bundleExtra.getString("username");
        team=bundleExtra.getString("team");
        teamid=bundleExtra.getString("id");
        mKqwSpeechSynthesizer.start("您正在群聊"+team+"中");
        listenBtn = findViewById(R.id.teamSpeakBtn);
        contentEt = findViewById(R.id.teamSpeakText);
        sendBtn=findViewById(R.id.teamSendBtn);
        teamInfoBtn=findViewById(R.id.teamInfoBtn);
        mAdapter = new MessageAdapter(mMessages);

        mAdapter.setOnItemLongClickListener(new FileAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position) {
                mKqwSpeechSynthesizer.start(mMessages.get(position).getSender()+
                        "说"+mMessages.get(position).getContent());
            }
        });
        mRecyclerView = findViewById(R.id.teamMessageRecycleView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

        verifyAudioPermissions(this);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这是发送按钮");
            }
        });
        sendBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(TextUtils.isEmpty(contentEt.getText().toString().trim())){
                    mKqwSpeechSynthesizer.start("请先输入或说出您想要发送的内容");
                }else{
                    sendMessage(contentEt.getText().toString());
                    contentEt.setText("");
                }
                return true;
            }
        });
        teamInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKqwSpeechSynthesizer.start("这是群信息按钮");
            }
        });
        teamInfoBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent=new Intent(TeamChatActivity.this, TeamInfoActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("username",username);
                bundle.putString("teamid",teamid);
                bundle.putString("team",team);
                intent.putExtra("bundle",bundle);
                startActivity(intent);
                return true;
            }
        });
        NIMClient.getService(MsgServiceObserve.class)
                .observeReceiveMessage(incomingMessageObserver, true);
        NIMClient.getService(MsgServiceObserve.class).observeMsgStatus(statusObserver, true);
//       NIMClient.getService(MsgService.class).queryMessageListEx("zhangsan",10,false);
        showMessage(teamid);
    }
    private Observer<List<IMMessage>> incomingMessageObserver =
            new Observer<List<IMMessage>>() {
                @Override
                public void onEvent(List<IMMessage> messages) {
                    // 处理新收到的消息，为了上传处理方便，SDK 保证参数 messages 全部来自同一个聊天对象。
                    for (IMMessage message : messages) {
                        if (message.getSessionId().equals(NIMClient.getCurrentAccount())) {
                            // 自己的消息
                            System.out.println("收到自己的消息：" + message.getContent());
                        } else {
                            // 对方的消息
                            System.out.println("收到对方的消息：" + message.getContent());
                        }
                    }

                }
            };
    private Observer<IMMessage> statusObserver = new Observer<IMMessage>() {
        @Override
        public void onEvent(IMMessage msg) {
            // 1、根据sessionId判断是否是自己的消息
            // 2、更改内存中消息的状态
            // 3、刷新界面
            if(msg.getSessionId().equals(NIMClient.getCurrentAccount())){
                String sender =msg.getFromNick();
                updateMessage(sender, msg.getContent());
            }else{
                String sender = "我:";  // 这里可以根据实际需求获取发送者的名称
                updateMessage(sender, msg.getContent());
            }
        }
    };
    private void updateMessage(String sender,String content){
        Message message = new Message(sender, content);
        mMessages.add(message);
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        mRecyclerView.scrollToPosition(mMessages.size() - 1);
    }
    private void sendMessage(String content) {
        //这里主要以发送文本消息为例
        String account = teamid;
        SessionTypeEnum sessionType = SessionTypeEnum.Team; // 以单聊类型为例
        String text = content;// 创建一个文本消息
        IMMessage textMessage = MessageBuilder.createTextMessage(account, sessionType, content);
        // 发送给对方
        NIMClient.getService(MsgService.class).sendMessage(textMessage, false).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                mKqwSpeechSynthesizer.start("发送成功！");
            }
            @Override
            public void onFailed(int code) {

                mKqwSpeechSynthesizer.start("发送失败！错误码："+code);
            }
            @Override
            public void onException(Throwable exception) {
                mKqwSpeechSynthesizer.start("网络异常！");
            }
        });
    }
    public void showMessage(String account){
        IMMessage anchorMessage = MessageBuilder.createEmptyMessage(account,
                SessionTypeEnum.Team, System.currentTimeMillis());
        loadMessage(anchorMessage);
    }
    public void loadMessage(IMMessage anchorMessage) {
        NIMClient.getService(MsgService.class).pullMessageHistory(anchorMessage, 20,true).setCallback(new RequestCallback<List<IMMessage>>() {
            @Override
            public void onSuccess(List<IMMessage> result) {
                System.out.println(1);
                for (IMMessage message:result){
                    String sender=message.getFromNick();
                    if(message.getSessionId().equals(NIMClient.getCurrentAccount())){
                        updateMessage("我", message.getContent());
                    }else
                     updateMessage(sender, message.getContent());
                }
            }

            @Override
            public void onFailed(int code) {
                System.out.println(1);
            }

            @Override
            public void onException(Throwable exception) {
                System.out.println(1);
            }
        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        mKqwSpeechSynthesizer.start("您正在群聊"+team+"中");
    }
}