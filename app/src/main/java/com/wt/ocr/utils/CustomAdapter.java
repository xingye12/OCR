package com.wt.ocr.utils;

import static com.wt.ocr.utils.JsonData.MYURL;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.wt.ocr.AddFriendActivity;
import com.wt.ocr.ChatActivity;
import com.wt.ocr.FriendListActivity;
import com.wt.ocr.R;
import com.wt.ocr.TeamChatActivity;
import com.wt.ocr.pojo.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CustomAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> mData;
    private int mRowHeight;
    private String username;
    private int friendOrTeam;
    private List<String> ids;
    private ArrayList<User> users;
    public CustomAdapter(Context context, List<String> data, int rowHeight,String name,int friendOrTeam) {
        mContext = context;
        mData = data;
        mRowHeight = rowHeight;
        username=name;
        this.friendOrTeam=friendOrTeam;
    }
    public CustomAdapter(Context context, List<String> data, int rowHeight,String name,int friendOrTeam,List<String> ids) {
        mContext = context;
        mData = data;
        mRowHeight = rowHeight;
        username=name;
        this.friendOrTeam=friendOrTeam;
        this.ids=ids;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.textView = convertView.findViewById(R.id.text_view);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        new Thread(getAllUserRun).start();
        // 设置每一行的高度
        ViewGroup.LayoutParams params = convertView.getLayoutParams();
        params.height = mRowHeight;
        convertView.setLayoutParams(params);

        holder.textView.setText(mData.get(position));
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理点击事件
                handleItemClick(position);
            }
        });
        return convertView;
    }
    private void handleItemClick(int position) {
        // 处理点击事件逻辑
        String item = mData.get(position);  // 获取点击行对应的数据项
        String friendAccount=null;
        if(friendOrTeam==1){
            for(User user:users){
                if(user.getNickname().equals(item))friendAccount=user.getUsername();
            }
            Intent intent=new Intent(mContext, ChatActivity.class);
            Bundle bundle=new Bundle();
            bundle.putString("friend",item);
            bundle.putString("username",username);
            bundle.putString("friendAccount",friendAccount);
            intent.putExtra("bundle",bundle);
            mContext.startActivity(intent);
        } else if (friendOrTeam==0) {
            String id=ids.get(position);
            Intent intent=new Intent(mContext, TeamChatActivity.class);
            Bundle bundle=new Bundle();
            bundle.putString("team",item);
            bundle.putString("username",username);
            bundle.putString("id",id);
            intent.putExtra("bundle",bundle);
            mContext.startActivity(intent);
        }

    }

    private static class ViewHolder {
        TextView textView;
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
                    ArrayList<User> userList = new ArrayList<>();
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
                                userList.add(userresult);
                            }
                        }
                        users=userList;
                    }

                }else{

                }


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };
}
