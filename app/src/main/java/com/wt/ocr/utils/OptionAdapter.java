package com.wt.ocr.utils;
import static com.wt.ocr.utils.JsonData.MYURL;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wt.ocr.AddFriendActivity;
import com.wt.ocr.CommunityActivity;
import com.wt.ocr.CreateTeamActivity;
import com.wt.ocr.FriendListActivity;
import com.wt.ocr.InitActivity;
import com.wt.ocr.RequestActivity;
import com.wt.ocr.pojo.User;


import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import com.wt.ocr.R;

import java.util.List;

public class OptionAdapter extends ArrayAdapter<String> {

    private Context context;
    private String[] items;
    private KqwSpeechSynthesizer mKqwSpeechSynthesizer;
    private String username;
    private String nickname;
    private String address;
    private String phone;
    private String idCard;
    private String sex;
    private ArrayList<String> friends;
    private int flag;
    public OptionAdapter(Context context, int resource, String[] items,String username,ArrayList<String> friends,String idCard, String sex, String nickname, String address, String phone) {
        super(context, resource, items);
        this.context = context;
        this.items = items;
        this.mKqwSpeechSynthesizer=new KqwSpeechSynthesizer(context);
        this.friends=friends;
        this.username=username;
        this.idCard = idCard;
        this.sex = sex;
        this.nickname = nickname;
        this.address = address;
        this.phone = phone;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_option, parent, false);
        }
        LinearLayout listItemLayout = convertView.findViewById(R.id.list_item_layout);
        listItemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKqwSpeechSynthesizer.start("您选择了"+items[position]+"选项。");
            }
        });
        listItemLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String selectedItem = items[position];
                switch (selectedItem){
                    case "添加好友":
                        flag=0;
                        new Thread(getAllUserRun).start();
                        break;
                    case "好友申请":
                        new Thread(getRequestRun).start();
                        break;
                    case "创建群聊":
                        flag=1;
                        new Thread(getAllUserRun).start();
                        break;
                }
                return true;
            }
        });
        // 获取列表项的数据
        String itemText = items[position];

        // 获取视图元素
        TextView itemTextView = convertView.findViewById(R.id.item_text);

        // 设置列表项视图的内容
        itemTextView.setText(itemText);

        return convertView;
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
                    }
//                    Intent intent=new Intent(context, AddFriendActivity.class);
                    Bundle bundle=new Bundle();
                    bundle.putString("username",username);
                    bundle.putStringArrayList("friends",friends);
//                    intent.putExtra("bundle",bundle);
//                    intent.putParcelableArrayListExtra("users", users);
//                    context.startActivity(intent);
                    if (flag == 0) {
                        Intent intent=new Intent(context, AddFriendActivity.class);
                        intent.putExtra("bundle",bundle);
                        intent.putParcelableArrayListExtra("users", users);
                        context.startActivity(intent);
                    } else if (flag==1) {
                        Intent intent=new Intent(context, CreateTeamActivity.class);
                        intent.putExtra("bundle",bundle);
                        intent.putParcelableArrayListExtra("users", users);
                        context.startActivity(intent);
                    }
                }else{
                    mKqwSpeechSynthesizer.start("获取失败");
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };
    Runnable getRequestRun = new Runnable() {
        @Override
        public void run() {
            User userCredentials = new User();
            userCredentials.setUsername(username);
            Gson gson = new Gson();
            String json = gson.toJson(userCredentials);
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = RequestBody.create(json, MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url(MYURL+"/getRequest")
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
                bundle.putString("nickname",nickname);
                bundle.putString("sex",sex);
                bundle.putString("address",address);
                bundle.putString("idCard",idCard);
                bundle.putString("phone",phone);
                bundle.putStringArrayList("requests",friends);

                    Intent intent=new Intent(context, RequestActivity.class);
                    intent.putExtra("bundle",bundle);
                    context.startActivity(intent);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
}

