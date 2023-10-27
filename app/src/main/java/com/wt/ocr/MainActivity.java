package com.wt.ocr;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.wt.ocr.utils.KqwSpeechSynthesizer;
import com.wt.ocr.utils.fileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends BaseActivity implements View.OnLongClickListener {

    private KqwSpeechSynthesizer mKqwSpeechSynthesizer;
    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final int PERMISSIONS_REQUEST_CAMERA        = 454;
    private static final int PERMISSIONS_REQUEST_WRITE_STORAGE = 455;
    private static final int PERMISSIONS_REQUEST_READSTORAGE = 456;

    static final String  PERMISSION_CAMERA        = Manifest.permission.CAMERA;
    static final String  PERMISSION_WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    static final String PERMISSION_READ_STORAGE=Manifest.permission.READ_EXTERNAL_STORAGE;
    private      Context context;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        context = this;
        imageView = findViewById(R.id.btn_camera);
        imageView.setOnLongClickListener(this);

        mKqwSpeechSynthesizer = new KqwSpeechSynthesizer(this);
        mKqwSpeechSynthesizer.start("长按手机屏幕中央可以进入拍照页面");
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mKqwSpeechSynthesizer.start("这是拍照按钮，长按可进行拍照");
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                deepFile("tessdata");
            }
        }).start();

        sendScreenImageName();
    }

    public boolean onLongClick(View view) {
        if (view.getId() == R.id.btn_camera) {
            checkSelfPermission();
            //google分析
            getTracker().send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("拍照")
                    .build());
        }
        return true;
    }

    /**
     * 将assets中的文件复制出
     *
     * @param path
     */
    public void deepFile(String path) {
        String newPath = getExternalFilesDir(null) + "/";
        try {
            String str[] = getAssets().list(path);
            if (str.length > 0) {//如果是目录
                File file = new File(newPath + path);
                file.mkdirs();
                for (String string : str) {
                    path = path + "/" + string;
                    deepFile(path);
                    path = path.substring(0, path.lastIndexOf('/'));//回到原来的path
                }
            } else {//如果是文件
                InputStream is = getAssets().open(path);
                FileOutputStream fos = new FileOutputStream(new File(newPath + path));
                byte[] buffer = new byte[1024];
                while (true) {
                    int len = is.read(buffer);
                    if (len == -1) {
                        break;
                    }
                    fos.write(buffer, 0, len);
                }
                is.close();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //google分析
    private void sendScreenImageName() {
        getTracker().setScreenName("Activity-" + "首页");
        getTracker().send(new HitBuilders.ScreenViewBuilder().build());
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CAMERA || requestCode == PERMISSIONS_REQUEST_WRITE_STORAGE||requestCode==PERMISSIONS_REQUEST_READSTORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                int a=0;
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];
                    if (!(permission.equals(PERMISSION_CAMERA)||permission.equals(PERMISSION_WRITE_STORAGE)||permission.equals(PERMISSION_READ_STORAGE))){
                        a=1;
                        break;
                    }

                }
//                if (requestCode == PERMISSION_REQUEST_CODE) {
//                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                        // 用户授予了文件写入权限，执行文件写入操作
////                        mKqwSpeechSynthesizer.start("您已拒绝存储权限。只有同意权限才能正常将识别内容同步到盲文点显器");
//                        Intent intent = new Intent(context, TakePhoteActivity.class);
//                        startActivity(intent);
//                    } else {
//                        // 用户拒绝了文件写入权限，可以根据需要处理
//                        mKqwSpeechSynthesizer.start("您已拒绝存储权限。只有同意权限才能正常将识别内容同步到盲文点显器");
//                    }
//                }
                if(a==0){
                    Intent intent = new Intent(context, TakePhoteActivity.class);
                    startActivity(intent);
                }

            } else {
                mKqwSpeechSynthesizer.start("请授予权限");
                Toast.makeText(context, "请开启权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 检查权限
     */
    void checkSelfPermission() {
        if (ContextCompat.checkSelfPermission(this, PERMISSION_CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{PERMISSION_READ_STORAGE,PERMISSION_CAMERA,PERMISSION_WRITE_STORAGE}, PERMISSIONS_REQUEST_CAMERA);

        } else if (ContextCompat.checkSelfPermission(this, PERMISSION_WRITE_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{PERMISSION_WRITE_STORAGE}, PERMISSIONS_REQUEST_CAMERA);
        } else {
            Intent intent = new Intent(context, TakePhoteActivity.class);
            startActivity(intent);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        // 在onStart之后调用
        // 执行交互和数据刷新等操作
        mKqwSpeechSynthesizer.start("这里是盲文拍照页面");
    }
}
