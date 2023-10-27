package com.wt.ocr;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.wt.ocr.utils.KqwSpeechSynthesizer;
import com.wt.ocr.utils.OpenCVUtil;
//import com.wt.ocr.utils.Utils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.FileOutputStream;


public class TestActivity extends Activity {

    private EditText mEtText;
    private KqwSpeechSynthesizer mKqwSpeechSynthesizer;
    private  ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test2);

        mEtText = (EditText) findViewById(R.id.et_text);

        // 初始化语音合成对象
        mKqwSpeechSynthesizer = new KqwSpeechSynthesizer(this);
        imageView = findViewById(R.id.imagetest);

        // 输入图片路径
//        String srcPath = "D:\\TU\\x7.png";

        // 加载opencv动态库，必要
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // 输入图片
//        Mat src = Imgcodecs.imread(srcPath);
        Bitmap srcBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img);

        // 将Bitmap转换为Mat
        Mat src = new Mat(srcBitmap.getHeight(), srcBitmap.getWidth(), CvType.CV_8UC3);
        int width = srcBitmap.getWidth();
        int height = srcBitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.setRotate(-90, width / 2, height / 2);
        Bitmap bit = Bitmap.createBitmap(srcBitmap, 0, 0, width, height, matrix, true);
        Utils.bitmapToMat(bit, src);
        // 灰度化
        Mat gray = new Mat();
//        if (src.channels() == 3) {
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
            src = gray;
//        } else {
//            System.out.println("不是RGB图片!");
//        }


        // 边缘算法检测
        Mat cannyMat = src.clone();

        // 表示迟滞过程的第一个阈值
        double threshold1 = 60;

        // 表示迟滞过程的第二个阈值，通常把第一个阈值*2或*3
        double threshold2 = threshold1 * 3;
        Imgproc.Canny(src, cannyMat, threshold1, threshold2);
//        Utils.matToBitmap(cannyMat,srcBitmap);
        // 计算倾斜角度
        double angle = OpenCVUtil.getAngle(cannyMat);
        // 图片旋转
//        Bitmap srcBitmap = BitmapFactory.decodeFile(srcPath);
//        int width = srcBitmap.getWidth();
//        int height = srcBitmap.getHeight();
//
//        Matrix matrix = new Matrix();

//        matrix.setRotate(10, width / 2, height / 2);
        width= bit.getWidth();
        height= bit.getHeight();
        matrix.setRotate((float) angle, width / 2, height / 2);



        Bitmap rotatedBitmap = Bitmap.createBitmap(bit, 0, 0, width, height, matrix, true);

        // 保存旋转后的图片
//        String rotatePath = "D:\\TU\\rotate.png";
//        try {
//            FileOutputStream outputStream = new FileOutputStream(rotatePath);
//            rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
//            outputStream.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // 显示旋转后的图片

        imageView.setImageBitmap(srcBitmap);


    }

    /**
     * 开始合成
     *
     * @param view
     */
    public void start(View view) {
        System.out.println(mEtText.getText().toString());
        Toast.makeText(this, "开始合成 : " + mEtText.getText().toString().trim(), Toast.LENGTH_SHORT).show();
        mKqwSpeechSynthesizer.start(mEtText.getText().toString().trim());
    }

}
