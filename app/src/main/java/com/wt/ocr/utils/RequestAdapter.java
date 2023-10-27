package com.wt.ocr.utils;

import static com.netease.nimlib.sdk.msg.constant.SystemMessageStatus.declined;
import static com.netease.nimlib.sdk.msg.constant.SystemMessageStatus.passed;
import static com.wt.ocr.InitActivity.flag;
import static com.wt.ocr.utils.JsonData.MYURL;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.SystemMessageService;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.netease.nimlib.sdk.team.TeamService;
import com.wt.ocr.ChatActivity;
import com.wt.ocr.CommunityActivity;
import com.wt.ocr.R;
import com.wt.ocr.RequestActivity;
import com.wt.ocr.pojo.User;

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

public class RequestAdapter extends ArrayAdapter<String> {
    private KqwSpeechSynthesizer mKqwSpeechSynthesizer;
    private Context mycontext;
    private int flag;
    private String username;
    private String nickname;
    private String phone;
    private String sex;
    private String address;
    private String idCard;
    private List<SystemMessage> messages;
    private String request;
    private int n;
    public RequestAdapter(Context context, List<String> data,User user,int flag) {

        super(context, 0, data);
        mKqwSpeechSynthesizer = new KqwSpeechSynthesizer(context);
        mycontext=context;
        username=user.getUsername();
        nickname=user.getUsername();
        phone=user.getPhone();
        sex=user.getSex();
        idCard=user.getIdCard();
        address=user.getAddress();
        this.n=flag;
    }
    public RequestAdapter(Context context, List<String> data, User user, int flag, List<SystemMessage> messages) {

        super(context, 0, data);
        mKqwSpeechSynthesizer = new KqwSpeechSynthesizer(context);
        mycontext=context;
        username=user.getUsername();
        nickname=user.getUsername();
        phone=user.getPhone();
        sex=user.getSex();
        idCard=user.getIdCard();
        address=user.getAddress();
        this.messages=messages;
        this.n=flag;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.request_item, parent, false);
        }

        // 获取当前行对应的数据
        String item = getItem(position);
        request=item;

        // 设置文本框内容
        EditText editText = convertView.findViewById(R.id.editText);
        editText.setText(item);

        // 设置按钮点击事件
        Button btnAgree = convertView.findViewById(R.id.btnAgree);
        Button btnReject = convertView.findViewById(R.id.btnReject);
        Button btnIgnore = convertView.findViewById(R.id.btnIgnore);

        btnAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击了同意按钮
                mKqwSpeechSynthesizer.start("这是同意按钮");
            }
        });
        btnAgree.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                flag=1;
                if(n==0)
                new Thread(allowRun).start();
                else if (n==1) {
                        String teamid=messages.get(position).getTargetId();
                        String inviter=messages.get(position).getFromAccount();
                        long msgid=messages.get(position).getMessageId();
                        NIMClient.getService(TeamService.class).acceptInvite(teamid,inviter).setCallback(new RequestCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                mKqwSpeechSynthesizer.start("加入群聊成功");
                                NIMClient.getService(SystemMessageService.class).setSystemMessageStatus(msgid,passed);
                            }

                            @Override
                            public void onFailed(int code) {
                                mKqwSpeechSynthesizer.start("加入群聊失败，错误码"+code);
                            }

                            @Override
                            public void onException(Throwable exception) {
                                mKqwSpeechSynthesizer.start("网络错误");
                            }
                        });

                    }

                return true;
            }
        });

        btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击了拒绝按钮

                mKqwSpeechSynthesizer.start("这是拒绝按钮");


            }
        });
        btnReject.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                flag=0;
                if(n==0)new Thread(allowRun).start();
                else if (n==1) {
                    String teamid=messages.get(position).getTargetId();
                    String inviter=messages.get(position).getFromAccount();
                    long msgid=messages.get(position).getMessageId();
                    NIMClient.getService(TeamService.class).declineInvite(teamid,inviter,"拒绝邀请").setCallback(new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {

                            mKqwSpeechSynthesizer.start("拒绝成功");
                            NIMClient.getService(SystemMessageService.class).setSystemMessageStatus(msgid,declined);
                        }

                        @Override
                        public void onFailed(int code) {
                            mKqwSpeechSynthesizer.start("拒绝失败，错误码"+code);
                        }

                        @Override
                        public void onException(Throwable exception) {
                            mKqwSpeechSynthesizer.start("网络错误");
                        }
                    });

                }

                return true;
            }
        });
        btnIgnore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击了忽略按钮

                mKqwSpeechSynthesizer.start("这是忽略按钮");
            }
        });
        btnIgnore.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                flag=0;
                new Thread(allowRun).start();
                return true;
            }
        });
        return convertView;
    }
    Runnable allowRun = new Runnable() {
        @Override
        public void run() {

                User userCredentials = new User();
                userCredentials.setNickname(username);
                userCredentials.setUsername(request);
                userCredentials.setAddress(""+flag);
                Gson gson = new Gson();
                String json = gson.toJson(userCredentials);

                OkHttpClient client = new OkHttpClient();

                RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json"));

                Request request = new Request.Builder()
                        .url(MYURL+"/allow")
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
                        bundle.putString("nickname",nickname);
                        bundle.putString("sex",sex);
                        bundle.putString("address",address);
                        bundle.putString("idCard",idCard);
                        bundle.putString("phone",phone);
                        bundle.putStringArrayList("requests",friends);
                        Intent intent=new Intent(mycontext, RequestActivity.class);
                        intent.putExtra("bundle",bundle);
                        mycontext.startActivity(intent);

                    }else{
                        mKqwSpeechSynthesizer.start("网络异常");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    };
}
