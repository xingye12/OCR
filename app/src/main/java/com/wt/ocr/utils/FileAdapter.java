package com.wt.ocr.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.wt.ocr.R;
import com.wt.ocr.pojo.FileItem;



public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {
    private Context context;
    private List<FileItem> fileList;
    private OnItemLongClickListener onItemLongClickListener;
    private OnItemClickListener onItemClickListener;
    public FileAdapter(Context context, List<FileItem> dataList) {
        this.context = context;
        this.fileList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileItem item = fileList.get(position);
        holder.textView.setText(item.getFileName());
//        int adapterPosition = getAdapterPosition(); // 获取适配器位置
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
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener!=null){
                    onItemClickListener.onItemLongClick(holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    // 设置自定义长按事件监听器
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImageView;
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.fileIconImage);
            textView = itemView.findViewById(R.id.fileNameText);
        }
    }
    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }
    public interface OnItemClickListener{
        void onItemLongClick(int position);
    }

//    private List<FileItem> fileList;
//
//    public FileAdapter(List<FileItem> fileList) {
//        this.fileList = fileList;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        FileItem fileItem = fileList.get(position);
//        holder.fileNameTextView.setText(fileItem.getFileName());
//    }
//
//    @Override
//    public int getItemCount() {
//        return fileList.size();
//    }
//
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        TextView fileNameTextView;
//
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//            fileNameTextView = itemView.findViewById(R.id.fileNameTextView);
//        }
//    }
}

