package com.wt.ocr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.wt.ocr.pojo.FileItem;
import com.wt.ocr.utils.FileAdapter;
import com.wt.ocr.utils.KqwSpeechSynthesizer;
import com.wt.ocr.utils.Message;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class MySouCangActivity extends AppCompatActivity {
    @SuppressLint("SdCardPath")
    private static String folderPath = "/sdcard/braille";
    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private KqwSpeechSynthesizer mKqwSpeechSynthesizer;
    private List<FileItem> fileList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_sou_cang);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mKqwSpeechSynthesizer=new KqwSpeechSynthesizer(this);
        mKqwSpeechSynthesizer.start("这里是我的收藏界面,您可以查看您收藏的所有文本");
        recyclerView = findViewById(R.id.recyclerView);

//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fileList=getFileList();
        fileAdapter = new FileAdapter(this,fileList);
        fileAdapter.setOnItemClickListener(new FileAdapter.OnItemClickListener() {
            @Override
            public void onItemLongClick(int position) {
                String fileName=fileList.get(position).getFileName().replace(".txt","");
                String fileInfo=convertString(fileName);
                mKqwSpeechSynthesizer.start(fileInfo);
            }
        });
        fileAdapter.setOnItemLongClickListener(new FileAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position) {
                String fileName=fileList.get(position).getFileName();
                String context=readTextFromFile(folderPath+"/"+fileName);
                mKqwSpeechSynthesizer.start(context);
            }
        });
        // 假设你已经从文件夹中获取了文件列表，然后将其添加到fileList中
        // fileList.add(new FileItem("file_name.txt", "/path/to/file"));
        recyclerView.setAdapter(fileAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

//        updateFileList(fileList);
//        fileAdapter.notifyDataSetChanged();
    }
    private void updateFileList(List<FileItem> files){
        fileAdapter.notifyItemInserted(files.size() - 1);
        recyclerView.scrollToPosition(files.size() - 1);
    }
    public List<FileItem> getFileList(){
        List<FileItem> fileItemList=new ArrayList<>();

        File folder = new File(folderPath);
        if (folder.exists()){
           try {
               File[] files = folder.listFiles();
               if (files != null) {
                   for (File file : files) {
                       if (file.isFile()) {
                           // 处理文件
                           String fileName = file.getName();
                           // 执行你想要的操作
                           FileItem fileItem = new FileItem(fileName, folderPath+"/"+fileName);
                           fileItemList.add(fileItem);
                       }
                   }
               }
           }catch (Exception exception){
               Log.e("getFileList", "getFileList: ", exception);
           }

        }

        return fileItemList;
    }
    public String readTextFromFile(String filePath) {
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append("\n"); // 如果需要保留换行符
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return text.toString();
    }
    public static String convertString(String input) {
        // 使用正则表达式来匹配日期时间部分，日期时间部分总是以数字开始
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(input);

        String dateTimePart = "";

        // 寻找匹配的日期时间部分
        if (matcher.find()) {
            dateTimePart = matcher.group();
        }

        // 剩余的部分是文件名
        String fileNamePart = input.replace(dateTimePart, "").trim().replaceAll("\\d", "");;

        // 将日期时间部分分割为年、月、日、小时、分钟和秒
        String year = dateTimePart.substring(0, 4);
        String month = dateTimePart.substring(4, 6);
        String day = dateTimePart.substring(6, 8);


        // 格式化日期时间部分
        String formattedDateTime = year + "年" + Integer.parseInt(month) + "月" + Integer.parseInt(day) + "日";

        // 构建最终的转换后的字符串
        String convertedString = "文件名：" + fileNamePart + "，收藏时间：" + formattedDateTime;

        return convertedString;
    }
    protected void onResume() {
        super.onResume();
        mKqwSpeechSynthesizer.start("您正在我的收藏界面");
    }
}