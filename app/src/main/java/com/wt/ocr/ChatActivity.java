package com.wt.ocr;

import static com.iflytek.cloud.SpeechConstant.APPID;
import static com.wt.ocr.InitActivity.flag;
import static com.wt.ocr.utils.JsonData.MYURL;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

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
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.wt.ocr.fragment.ChatTopFragment;
import com.wt.ocr.pojo.ChatSession;
import com.wt.ocr.pojo.User;
import com.wt.ocr.utils.DictationResult;
import com.wt.ocr.utils.FileAdapter;
import com.wt.ocr.utils.IM.ChatMsgHandler;
import com.wt.ocr.utils.IM.Preferences;
import com.wt.ocr.utils.JsonData;
import com.wt.ocr.utils.KqwSpeechSynthesizer;
import com.wt.ocr.utils.Message;
import com.wt.ocr.utils.MessageAdapter;

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

public class ChatActivity extends AppCompatActivity implements View.OnClickListener, ChatMsgHandler.OnLoadMsgListener {
    private TextView friendnameText;
    //    private ChatMsgHandler mChatHandler;
    private ChatSession mChatSession;
    private String username;
    private String friend;
    private ImageButton listenBtn;
    private Button sendBtn;
    private EditText contentEt;
    private String dictationResultStr = "[";
    private KqwSpeechSynthesizer mKqwSpeechSynthesizer;
    private Fragment topFragment;
    private RecyclerView mRecyclerView;
    private List<Message> mMessages;
    private List<IMMessage> mMsgList;
    private MessageAdapter mAdapter;
    private List<NimUserInfo> users;
    private String friendAccount;
    private ArrayList<User> allUsers;

    //申请录音权限
    private static final int GET_RECODE_AUDIO = 1;
    private static final int ONE_QUERY_LIMIT = 20;
    private static String[] PERMISSION_AUDIO = {
            Manifest.permission.RECORD_AUDIO
    };
    /*
     * 申请录音权限*/
    public static void verifyAudioPermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.RECORD_AUDIO);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSION_AUDIO,
                    GET_RECODE_AUDIO);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_chat);
//        new Thread(getAllUserRun).start();

        Intent intentExtra=getIntent();
        Bundle bundleExtra=intentExtra.getBundleExtra("bundle");
        friend= bundleExtra.getString("friend");
        username= bundleExtra.getString("username");
        friendAccount=bundleExtra.getString("friendAccount");
        String nickname= bundleExtra.getString("nickname");

        String sex=bundleExtra.getString("sex");
        String address= bundleExtra.getString("address");
        String idCard=bundleExtra.getString("idCard");
        String phone= bundleExtra.getString("phone");
        mMessages = new ArrayList<>();
        mAdapter = new MessageAdapter(mMessages);
        mAdapter.setOnItemLongClickListener(new FileAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position) {
                mKqwSpeechSynthesizer.start(mMessages.get(position).getSender()+
                        "说"+mMessages.get(position).getContent());
            }
        });
//        topFragment=new ChatTopFragment();
//        topFragment.setArguments(bundleExtra);
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        fragmentManager.beginTransaction()
//                .replace(R.id.top_container,topFragment)
//                .addToBackStack(null)
//                .commit();
        mKqwSpeechSynthesizer = new KqwSpeechSynthesizer(this);
        mKqwSpeechSynthesizer.start("您现在在和"+friend+"聊天");
        friendnameText=findViewById(R.id.friendnameText);
        friendnameText.setText(friend);
        friendnameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKqwSpeechSynthesizer.start("长按此处可以查看好友信息");
            }
        });
        friendnameText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new Thread(getFriendRun).start();
                return true;
            }
        });
        listenBtn = findViewById(R.id.speakBtn);
        contentEt = findViewById(R.id.speakText);
        sendBtn=findViewById(R.id.sendBtn);
        mRecyclerView = findViewById(R.id.messageRecycleView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
        verifyAudioPermissions(this);
        listenBtn.setOnClickListener(this);
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
        NIMClient.getService(MsgServiceObserve.class)
                .observeReceiveMessage(incomingMessageObserver, true);
        NIMClient.getService(MsgServiceObserve.class).observeMsgStatus(statusObserver, true);
//       NIMClient.getService(MsgService.class).queryMessageListEx("zhangsan",10,false);
        showMessage(friendAccount);
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
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.speakBtn:
                dictationResultStr = "[";
                // 语音配置对象初始化
                SpeechUtility.createUtility(ChatActivity.this, APPID
                        + "=" + APPID);
                // 1.创建SpeechRecognizer对象，第2个参数：本地听写时传InitListener
                SpeechRecognizer mIat = SpeechRecognizer.createRecognizer(
                        ChatActivity.this, null);
                // 交互动画
                RecognizerDialog iatDialog = new RecognizerDialog(
                        ChatActivity.this, null);
                // 2.设置听写参数，详见《科大讯飞MSC API手册(Android)》SpeechConstant类
                mIat.setParameter(SpeechConstant.DOMAIN, "iat"); // domain:域名
                mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
                mIat.setParameter(SpeechConstant.ACCENT, "mandarin"); // mandarin:普通话

                //3.开始听写
                iatDialog.setListener(new RecognizerDialogListener() {


                    @Override
                    public void onResult(RecognizerResult results, boolean isLast) {
                        // TODO 自动生成的方法存根
                        if (!isLast) {
                            dictationResultStr += results.getResultString() + ",";
                        } else {
                            dictationResultStr += results.getResultString() + "]";
                        }
                        if (isLast) {
                            // 解析Json列表字符串
                            Gson gson = new Gson();
                            List<DictationResult> dictationResultList = gson
                                    .fromJson(dictationResultStr,
                                            new TypeToken<List<DictationResult>>() {
                                            }.getType());
                            String finalResult = "";
                            for (int i = 0; i < dictationResultList.size() - 1; i++) {
                                finalResult += dictationResultList.get(i)
                                        .toString();
                            }
                            contentEt.setText(finalResult);
                            mKqwSpeechSynthesizer.start("语音内容是："+finalResult);
                            //获取焦点
                            contentEt.requestFocus();
                            //将光标定位到文字最后，以便修改
                            contentEt.setSelection(finalResult.length());
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
                break;
            case R.id.sendBtn:
                mKqwSpeechSynthesizer.start("这是发送按钮，长按发送消息");

            default:
                break;
        }
    }
    private void sendMessage(String content) {
        //这里主要以发送文本消息为例
//        getIdByName(friend);
        String account =friendAccount;
        SessionTypeEnum sessionType = SessionTypeEnum.P2P; // 以单聊类型为例
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

    @Override
    protected void onResume() {
        super.onResume();
        mKqwSpeechSynthesizer.start("这里是与"+friend+"的聊天界面");
    }
    private Observer<IMMessage> statusObserver = new Observer<IMMessage>() {
        @Override
        public void onEvent(IMMessage msg) {
            // 1、根据sessionId判断是否是自己的消息
            // 2、更改内存中消息的状态
            // 3、刷新界面

            System.out.println(NIMClient.getCurrentAccount());
            if(msg.getSessionId().equals(NIMClient.getCurrentAccount())){

                String sender =msg.getSessionId();
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
    @Override
    public void loadSuccess(List<IMMessage> messages, IMMessage anchorMessage) {
//        mRecyclerView.hideHeadView();

        boolean scroll = false;
        // 如果原本没有，为第一次加载，需要在加载完成后移动到最后一项
        if (mMessages.isEmpty()) {
            scroll = true;
        }
        if (!messages.isEmpty()) {
//            mMsgList.addAll(0, mChatHandler.dealLoadMessage(messages, anchorMessage));
//            mMessages.addAll(0, messages);
//            mAdapter.notifyDataSetChanged();

        }
        if (scroll) {
//            mLayoutManager.scrollToPosition(mMsgList.size());
        }

    }


    @Override
    public void loadFail(String message) {
        System.out.println(message);

    }
    public void showMessage(String account){
//        if (mMessages.isEmpty()) {
            // 记录为空时，以当前时间为锚点
            IMMessage anchorMessage = MessageBuilder.createEmptyMessage(account,
                    SessionTypeEnum.P2P, System.currentTimeMillis());
            loadMessage(anchorMessage);

    }
    public void loadMessage(IMMessage anchorMessage) {
        NIMClient.getService(MsgService.class).pullMessageHistory(anchorMessage,20,true).setCallback(new RequestCallback<List<IMMessage>>() {
            @Override
            public void onSuccess(List<IMMessage> result) {
                System.out.println(1);
                for (IMMessage message:result){
                    String sender=message.getFromNick();
                    if(sender.equals(friend)) updateMessage(sender, message.getContent());
                    else updateMessage("我",message.getContent());
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
    public String getNameById(String id){
        List<String> a=new ArrayList<>();
        a.add(id);
        NIMClient.getService(UserService.class).fetchUserInfo(a).setCallback(new RequestCallback<List<NimUserInfo>>() {
            @Override
            public void onSuccess(List<NimUserInfo> result) {
                users=result;
            }

            @Override
            public void onFailed(int code) {
                mKqwSpeechSynthesizer.start("没有此用户");
            }

            @Override
            public void onException(Throwable exception) {
                mKqwSpeechSynthesizer.start("网络异常");
            }
        });
        for(UserInfo user:users){
            if(id.equals(user.getAccount())){
                return user.getName();
            }
        }
        return null;
    }
    public interface OnLoadMsgListener {
        void loadSuccess(List<IMMessage> messages, IMMessage anchorMessage);

        void loadFail(String message);
    }

    Runnable getAllUserRun = new Runnable() {
        @Override
        public void run() {

            User user1=new User();
            user1.setUsername(username);
            Gson gson = new Gson();
            String json = gson.toJson(user1);

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json"));

            Request request = new Request.Builder()
                    .url(MYURL+"/getAllUser")
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
                    ArrayList<User> users = new ArrayList<>();
                    if (o instanceof ArrayList) {
                        ArrayList<Object> arrayList = (ArrayList<Object>) o;
                        for (Object item : arrayList) {
                            if (item instanceof LinkedTreeMap) {
                                LinkedTreeMap<String, Object> linkedTreeMap = (LinkedTreeMap<String, Object>) item;
                                // 从LinkedTreeMap中提取值
                                String username = (String) linkedTreeMap.get("username");
                                String password = (String) linkedTreeMap.get("password");
                                String nickname = (String) linkedTreeMap.get("nickname");
                                String sex = (String) linkedTreeMap.get("sex");
                                String phone=(String)linkedTreeMap.get("phone");
                                String idcard=(String)linkedTreeMap.get("idCard");
                                String address=(String)linkedTreeMap.get("address");
                                User userresult=new User(username,password,idcard,sex,nickname,address,phone);
                                users.add(userresult);
                            }
                        }
                        allUsers=users;
                    }

                }else{
                    mKqwSpeechSynthesizer.start("获取失败");
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };
    Runnable getFriendRun = new Runnable() {
        @Override
        public void run() {

            User userCredentials = new User();
            userCredentials.setNickname(friend);
            Gson gson = new Gson();
            String json = gson.toJson(userCredentials);

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json"));

            Request request = new Request.Builder()
                    .url(MYURL + "/getFriendInfo")
                    .post(requestBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String jsonResponse = response.body().string();

                Gson backGson = new Gson();
                JsonData jsonData = backGson.fromJson(jsonResponse, JsonData.class);
                String code = jsonData.getCode();
                String msg = jsonData.getMsg();
                Object o = jsonData.getData();
                if (code.equals("1")) {
                    Map map = gson.fromJson(o.toString(), Map.class);
                    String account = map.get("username").toString();
                    String friendName = map.get("nickname").toString();
                    String sex = map.get("sex").toString();
                    String address = map.get("address").toString();
                    String phone = new BigDecimal(map.get("phone").toString()).toString();
                    Intent intent = new Intent(ChatActivity.this, FriendInfoActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("account", account);
                    bundle.putString("friendName", friendName);
                    bundle.putString("sex", sex);
                    bundle.putString("address", address);
                    bundle.putString("phone", phone);
                    intent.putExtra("bundle", bundle);
                    startActivity(intent);
                } else {
                    mKqwSpeechSynthesizer.start("好友信息获取失败");
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

}
