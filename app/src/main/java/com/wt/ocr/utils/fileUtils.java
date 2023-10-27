package com.wt.ocr.utils;

import android.annotation.SuppressLint;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class fileUtils {
    public static int writefile(String content,String fileName){
        String text = content;
        int flag;
        Date currentDate = new Date();

        // 创建日期格式化对象
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 格式化日期对象
        String formattedDate = format.format(currentDate);
        String temp=formattedDate.replace("-","").trim();
        String date=temp.replace(":","");
        // 指定文件路径和名称
        @SuppressLint("SdCardPath")
        String folderPath = "/sdcard/braille";
        String filePath = folderPath + "/" + fileName+date + ".txt";

        // 检查文件夹是否存在，如果不存在则创建文件夹
        File folder = new File(folderPath);
        if (!folder.exists()) {
            boolean created = folder.mkdirs();
            if (!created) {
                System.out.println("创建文件夹失败");
                flag = 0;
                return flag;
            }
        }

        // 创建文件对象
        File file = new File(filePath);

        try {
            // 创建文件写入器
            FileWriter writer = new FileWriter(file);

            // 写入文本内容
            writer.write(text);

            // 关闭写入流
            writer.close();
            flag = 1;
        } catch (IOException e) {
            e.printStackTrace();
            flag = 0;
        }
        return flag;
    }


}
