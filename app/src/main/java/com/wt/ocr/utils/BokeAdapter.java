package com.wt.ocr.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wt.ocr.ChatActivity;
import com.wt.ocr.R;
import com.wt.ocr.detailActivity;
import com.wt.ocr.pojo.Boke;

import java.util.ArrayList;

public class BokeAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Boke> bokeList;
    private String username;
    private static final String sender="发布人:";
    private static final String time="时间:";
    private static final String type="类别:";
    public BokeAdapter(Context context, ArrayList<Boke> bokeList,String name) {
        this.context = context;
        this.bokeList = bokeList;
        this.username=name;
    }

    @Override
    public int getCount() {
        return bokeList.size();
    }

    @Override
    public Object getItem(int position) {
        return bokeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_boke, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.titleTextView = convertView.findViewById(R.id.titleTextView);
            viewHolder.nicknameTextView = convertView.findViewById(R.id.nicknameTextView);
            viewHolder.timeTextView = convertView.findViewById(R.id.timeTextView);
            viewHolder.typeTextView = convertView.findViewById(R.id.typeTextView);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Boke boke = bokeList.get(position);

        viewHolder.titleTextView.setText(boke.getTitle());
        viewHolder.nicknameTextView.setText(sender+boke.getNickname());
        viewHolder.timeTextView.setText(time+boke.getTime());
        viewHolder.typeTextView.setText(type+boke.getType());
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
        Boke item = bokeList.get(position);  // 获取点击行对应的数据项
        Intent intent=new Intent(context, detailActivity.class);
        Bundle bundle=new Bundle();
        bundle.putString("nickname", bokeList.get(position).getNickname());
        bundle.putString("time", bokeList.get(position).getTime());
        bundle.putString("title", bokeList.get(position).getTitle());
        bundle.putString("content", bokeList.get(position).getContent());
        bundle.putString("type", bokeList.get(position).getType());
        bundle.putString("username",username);
        System.out.println(bokeList);
        intent.putExtra("bundle",bundle);
        context.startActivity(intent);
    }

    static class ViewHolder {
        TextView titleTextView;
        TextView nicknameTextView;
        TextView timeTextView;
        TextView typeTextView;
    }
}
