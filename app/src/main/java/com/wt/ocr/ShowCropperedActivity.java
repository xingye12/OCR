package com.wt.ocr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.wt.ocr.utils.KqwSpeechSynthesizer;
import com.wt.ocr.utils.OpenCVUtil;

import com.wt.ocr.utils.Utils;
import com.wt.ocr.utils.fileUtils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import org.opencv.imgproc.Imgproc;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * 显示截图结果
 * 并识别
 * Created by Administrator on 2016/12/10.
 */

public class ShowCropperedActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 999;
    private final int REQUEST_PERMISSIONS_CODE = 1;
    private              Context context;
    private Bitmap rotatedBitmap;
    //sd卡路径
    private static       String  LANGUAGE_PATH = "";
    //识别语言
    private static final String  LANGUAGE      = "chi_sim";//chi_sim | eng

    private static final String    TAG = "ShowCropperedActivity";
    private              ImageView imageView;
//    private              ImageView imageView2;
    private SelectWordTextView textView;
//    private              SelectWordTextView textView;
    private int    width;
    private int    height;
//    private Uri    uri;
    private String result;
    private KqwSpeechSynthesizer mKqwSpeechSynthesizer;
    private TessBaseAPI    baseApi = new TessBaseAPI();
    private Handler        handler = new Handler();
    private ProgressDialog dialog;
    private Button mySouCangBtn;
    private ImageButton huituiBtn;
    private ImageButton bofangBtn;
    private ImageButton kuaijinBtn;
    private ImageButton chongboBt;
    private ImageButton tingzhiBtn;
    private ImageButton shoucangBtn;
    private ColorMatrix colorMatrix;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_show_croppered);
        context = this;
        LANGUAGE_PATH = getExternalFilesDir("") + "/";
        Log.e("---------", LANGUAGE_PATH);
//        huituiBtn.setOnClickListener(onClickListener);
        bofangBtn=findViewById(R.id.zantingBtn);
        kuaijinBtn=findViewById(R.id.kuaijinBtn);
        huituiBtn=findViewById(R.id.huituiBtn);
        tingzhiBtn=findViewById(R.id.tingzhiBtn);
        chongboBt=findViewById(R.id.chongboBtn);
        shoucangBtn=findViewById(R.id.shoucangBtn);
        mySouCangBtn=findViewById(R.id.mySouCangBtn);
        bofangBtn.setOnClickListener(onClickListener);
//        kuaijinBtn.setOnClickListener(onClickListener);
        tingzhiBtn.setOnClickListener(onClickListener);
        chongboBt.setOnClickListener(onClickListener);
        shoucangBtn.setOnClickListener(onClickListener);
        huituiBtn.setOnTouchListener(onTouchListener);
        kuaijinBtn.setOnTouchListener(onTouchListener);
        width = getIntent().getIntExtra("width", 0);
        height = getIntent().getIntExtra("height", 0);
        byte[] bitmapBytes = getIntent().getByteArrayExtra("bitmap");
        rotatedBitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
//        uri = getIntent().getData();
        mKqwSpeechSynthesizer = new KqwSpeechSynthesizer(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(ShowCropperedActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // 如果权限未被授予，则请求权限
                mKqwSpeechSynthesizer.start("请授予文件存储权限，方便您正常使用app的功能");
                ActivityCompat.requestPermissions(ShowCropperedActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            }
        }
        initView();
        initTess();
    }

    @SuppressLint("WrongViewCast")
    private void initView() {
        imageView = findViewById(R.id.image);
//        imageView2 = findViewById(R.id.image2);
        textView = findViewById(R.id.textviewcon);
        dialog = new ProgressDialog(context);
        mKqwSpeechSynthesizer.start("图像正在识别中，请耐心等待");
        dialog.setMessage("正在识别...");
        dialog.setCancelable(false);
        dialog.show();

        if (width != 0 && height != 0) {
            int screenWidth = Utils.getWidthInPx(this);
            float scale = (float) screenWidth / (float) width;
            final ViewGroup.LayoutParams lp = imageView.getLayoutParams();
            int imgHeight = (int) (scale * height);
            lp.height = imgHeight;
            imageView.setLayoutParams(lp);
            Log.e(TAG, "imageView.getLayoutParams().width:" + imageView.getLayoutParams().width);
        }
//        imageView.setImageURI(uri);
        imageView.setImageBitmap(rotatedBitmap);
        mySouCangBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ShowCropperedActivity.this, MySouCangActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initTess() {
        //字典库
        baseApi.init(LANGUAGE_PATH, LANGUAGE);
        //设置设别模式
        baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
        Thread myThread = new Thread(runnable);
        myThread.start();
    }


    /**
     * uri转bitmap
     */
    private Bitmap getBitmapFromUri(Uri uri) {
        try {

            return MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

        } catch (Exception e) {
            Log.e("[Android]", e.getMessage());
            Log.e("[Android]", "目录为：" + uri);
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 灰度化处理
     */
    public Bitmap convertGray(Bitmap bitmap3) {
        colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);

        Paint paint = new Paint();
        paint.setColorFilter(filter);
        Bitmap result = Bitmap.createBitmap(bitmap3.getWidth(), bitmap3.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        canvas.drawBitmap(bitmap3, 0, 0, paint);
        return result;
    }

    /**
     * 二值化
     *
     * @param tmp 二值化阈值 默认100
     */
    private Bitmap binaryzation(Bitmap bitmap22, int tmp) {
        // 获取图片的宽和高
        int width = bitmap22.getWidth();
        int height = bitmap22.getHeight();
        // 创建二值化图像
        Bitmap bitmap;
        bitmap = bitmap22.copy(Bitmap.Config.ARGB_8888, true);
        // 遍历原始图像像素,并进行二值化处理
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                // 得到当前的像素值
                int pixel = bitmap.getPixel(i, j);
                // 得到Alpha通道的值
                int alpha = pixel & 0xFF000000;
                // 得到Red的值
                int red = (pixel & 0x00FF0000) >> 16;
                // 得到Green的值
                int green = (pixel & 0x0000FF00) >> 8;
                // 得到Blue的值
                int blue = pixel & 0x000000FF;

                if (red > tmp) {
                    red = 255;
                } else {
                    red = 0;
                }
                if (blue > tmp) {
                    blue = 255;
                } else {
                    blue = 0;
                }
                if (green > tmp) {
                    green = 255;
                } else {
                    green = 0;
                }

                // 通过加权平均算法,计算出最佳像素值
                int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                // 对图像设置黑白图
                if (gray <= 95) {
                    gray = 0;
                } else {
                    gray = 255;
                }
                // 得到新的像素值
                int newPiexl = alpha | (gray << 16) | (gray << 8) | gray;
                // 赋予新图像的像素
                bitmap.setPixel(i, j, newPiexl);
            }
        }
        return bitmap;
    }

    /**
     * 识别线程
     */
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
//            System.out.println(uri);
//            final Bitmap bitmap_1 = convertGray(getBitmapFromUri(uri));
            final Bitmap bitmap_1 = convertGray(rotatedBitmap);
            baseApi.setImage(bitmap_1);
            result = baseApi.getUTF8Text();
            baseApi.end();

            handler.post(new Runnable() {
                @Override
                public void run() {
//                    imageView2.setImageBitmap(bitmap_1);
                    textView.setText(result);
                    // 检查是否已经授予文件写入权限

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(ShowCropperedActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            // 如果权限未被授予，则请求权限
                            mKqwSpeechSynthesizer.start("请授予文件存储权限，方便您正常使用app的功能");
                            ActivityCompat.requestPermissions(ShowCropperedActivity.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    PERMISSION_REQUEST_CODE);
                        } else {
                            // 权限已经被授予，可以执行文件写入操作
//                            if(fileUtils.writefile(result)==1){
//                                mKqwSpeechSynthesizer.start("文件存储成功，识别结果是："+result);
//                            }else{
//                                mKqwSpeechSynthesizer.start("文件存储失败，识别结果是："+result);
//                            }
                            mKqwSpeechSynthesizer.start("识别结果是"+result);
                        }
                    } else {
                        // 在较旧的Android版本上，权限默认被授予，可以执行文件写入操作
//                        if(fileUtils.writefile(result)==1){
//                            mKqwSpeechSynthesizer.start("文件存储成功，识别结果是："+result);
//                        }else{
//                            mKqwSpeechSynthesizer.start("文件存储失败，识别结果是："+result);
//                        }
                    }

                    dialog.dismiss();
                }
            });
        }
    };
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户授予了文件写入权限，执行文件写入操作
//                if(fileUtils.writefile(result)==1){
//                    mKqwSpeechSynthesizer.start("您现在在识别结果播报界面。识别结果是："+result);
//                }else{
//                    mKqwSpeechSynthesizer.start("文件存储失败，识别结果是："+result);
//                }
                mKqwSpeechSynthesizer.start("您现在在文字识别结果播报界面。界面共有三行，前两行都有3个按钮，最后一行只有一个按钮。它们先从左到右，再从上到下分别是：慢放，暂停与播放，快进，重播，停止播放，收藏和我的收藏");
            } else {
                // 用户拒绝了文件写入权限，可以根据需要处理
                mKqwSpeechSynthesizer.start("您已拒绝存储权限。只有同意权限才能正常将识别内容同步到盲文点显器");
            }
        }
    }
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.huituiBtn:
                    mKqwSpeechSynthesizer.huitui(result);
//
                    break;
                case R.id.zantingBtn:
                    if(mKqwSpeechSynthesizer.getFlag()==1){
                        mKqwSpeechSynthesizer.pause();
                    } else if (mKqwSpeechSynthesizer.getFlag()==2) {
                        mKqwSpeechSynthesizer.bofang();
                    } else if (mKqwSpeechSynthesizer.getFlag()==0) {
                        mKqwSpeechSynthesizer.start(result);
                    }

                    break;
                case R.id.kuaijinBtn:
                    mKqwSpeechSynthesizer.kuaijin(result);
                    break;
                case R.id.chongboBtn:
                    mKqwSpeechSynthesizer.stop();
                    mKqwSpeechSynthesizer.start("开始重播"+result);
                    break;
                case R.id.tingzhiBtn:
                    mKqwSpeechSynthesizer.stop();
                    break;
                case R.id.shoucangBtn:
                    writeFileName(ShowCropperedActivity.this);
//

                    break;
                case R.id.mySouCangBtn:
                    Intent intent=new Intent(ShowCropperedActivity.this, MySouCangActivity.class);
                    startActivity(intent);
            }
        }
    };
    private View.OnLongClickListener onLongClickListener=new View.OnLongClickListener(){
        @Override
        public boolean onLongClick(View view) {
            switch (view.getId()) {
                case R.id.huituiBtn:
//                    mKqwSpeechSynthesizer.
                    break;
                case R.id.zantingBtn:

                    break;
                case R.id.kuaijinBtn:

                    break;
                case R.id.chongboBtn:

                    break;
                case R.id.tingzhiBtn:

                    break;
                case R.id.shoucangBtn:

                    break;
            }
            return true;
        }};
    private View.OnTouchListener onTouchListener=new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                mKqwSpeechSynthesizer.huifu();

            } else if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                switch (view.getId()) {
                    case R.id.huituiBtn:
                        mKqwSpeechSynthesizer.kuaijin(result);

                        break;
                    case R.id.kuaijinBtn:
                        mKqwSpeechSynthesizer.huitui(result);
                }

            }
            return true;
        }


    };
    private void writeFileName(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("命名收藏内容");
        mKqwSpeechSynthesizer.start("请为您的收藏内容命名");
        // 创建一个EditText视图
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT); // 设置输入类型为文本

        builder.setView(input);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String fileName = input.getText().toString();
                if(fileUtils.writefile(result,fileName)==1){
                    mKqwSpeechSynthesizer.start("收藏成功");
                }else{
                    mKqwSpeechSynthesizer.start("收藏失败");
                }
                // 在这里处理用户输入的文本，例如将其显示在UI上或执行其他操作
                // userInput 中包含了用户输入的文本
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel(); // 取消对话框
            }
        });

        builder.show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mKqwSpeechSynthesizer.start("这里是识别结果播报界面");
    }
}
