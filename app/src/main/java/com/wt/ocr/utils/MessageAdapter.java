package com.wt.ocr.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wt.ocr.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> mMessages;
    private FileAdapter.OnItemLongClickListener onItemLongClickListener; // 自定义长按事件监听器接口
    public MessageAdapter(List<Message> messages) {
        mMessages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = mMessages.get(position);
        holder.senderTextView.setText(message.getSender());
        holder.contentTextView.setText(message.getContent());
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onItemLongClickListener != null) {
                    onItemLongClickListener.onItemLongClick(holder.getAdapterPosition()); // 通知监听器长按事件发生
                    return true; // 返回true表示事件已处理
                }
                return false; // 返回false表示事件未处理
            }
        });
    }
    public void setOnItemLongClickListener(FileAdapter.OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView senderTextView;
        TextView contentTextView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderTextView = itemView.findViewById(R.id.sender_textview);
            contentTextView = itemView.findViewById(R.id.content_textview);
        }
    }
}