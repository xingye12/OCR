package com.wt.ocr;

import static org.opencv.android.Utils.bitmapToMat;
import static org.opencv.android.Utils.matToBitmap;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.edmodo.cropper.CropImageView;
import com.wt.ocr.camear.CameraPreview;
import com.wt.ocr.camear.FocusView;
import com.wt.ocr.utils.FaceRecognition;
import com.wt.ocr.utils.KqwSpeechSynthesizer;
import com.wt.ocr.utils.OpenCVUtil;
import com.wt.ocr.utils.Utils;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 拍照界面
 * Created by Administrator on 2016/12/8.
 */
public class TakePhoteActivity extends AppCompatActivity implements CameraPreview.OnCameraStatusListener, SensorEventListener {
    private CascadeClassifier mJavaDetector; // OpenCV的人脸检测器
    private KqwSpeechSynthesizer mKqwSpeechSynthesizer;
    private             Context context;
    //true:横屏   false:竖屏
    public static final boolean isTransverse = true;

    private static final String TAG       = "TakePhoteActivity";
    public static final  Uri    IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

    private String PATH;

    private CameraPreview  mCameraPreview;
    private CropImageView  mCropImageView;
    private RelativeLayout mTakePhotoLayout;
    private LinearLayout   mCropperLayout;
    private ImageView      btnClose;
    private ImageView      btnShutter;
    private Button         btnAlbum;
    private ImageView      btnStartCropper;
    private ImageView      btnCloseCropper;
    private FaceRecognition faceRecognition;


    /**
     * 旋转文字
     */
    private boolean isRotated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置横屏
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // 设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_take_phote);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mKqwSpeechSynthesizer = new KqwSpeechSynthesizer(this);
        mKqwSpeechSynthesizer.start("您已进入拍照界面。横屏后点击屏幕右侧开始拍照");
        context = this;
        PATH = getExternalCacheDir() + "/AndroidMedia/";

        btnClose = findViewById(R.id.btn_close);
        btnClose.setOnClickListener(onClickListener);
        btnClose.setOnLongClickListener(onLongClickListener);
        btnShutter = findViewById(R.id.btn_shutter);
        btnShutter.setOnClickListener(onClickListener);
        btnShutter.setOnLongClickListener(onLongClickListener);
        btnAlbum = findViewById(R.id.btn_album);
        btnAlbum.setOnClickListener(onClickListener);
        btnAlbum.setOnLongClickListener(onLongClickListener);

        btnStartCropper = findViewById(R.id.btn_startcropper);
        btnStartCropper.setOnClickListener(cropcper);
        btnCloseCropper = findViewById(R.id.btn_closecropper);
        btnCloseCropper.setOnClickListener(cropcper);

        mTakePhotoLayout = findViewById(R.id.take_photo_layout);
        mCameraPreview = findViewById(R.id.cameraPreview);
        FocusView focusView = findViewById(R.id.view_focus);

        mCropperLayout = findViewById(R.id.cropper_layout);
        mCropImageView = findViewById(R.id.CropImageView);
        mCropImageView.setGuidelines(2);

        mCameraPreview.setFocusView(focusView);
        mCameraPreview.setOnCameraStatusListener(this);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        if (isTransverse) {
            if (!isRotated) {
                TextView tvHint = findViewById(R.id.hint);
                ObjectAnimator animator = ObjectAnimator.ofFloat(tvHint, "rotation", 0f, 90f);
                animator.setStartDelay(800);
                animator.setDuration(500);
                animator.setInterpolator(new LinearInterpolator());
                animator.start();

                ImageView btnShutter = findViewById(R.id.btn_shutter);
                ObjectAnimator animator1 = ObjectAnimator.ofFloat(btnShutter, "rotation", 0f, 90f);
                animator1.setStartDelay(800);
                animator1.setDuration(500);
                animator1.setInterpolator(new LinearInterpolator());
                animator1.start();

                View view = findViewById(R.id.crop_hint);
                AnimatorSet animSet = new AnimatorSet();
                ObjectAnimator animator2 = ObjectAnimator.ofFloat(view, "rotation", 0f, 90f);
                ObjectAnimator moveIn = ObjectAnimator.ofFloat(view, "translationX", 0f, -50f);
                animSet.play(animator2).before(moveIn);
                animSet.setDuration(10);
                animSet.start();

                ObjectAnimator animator3 = ObjectAnimator.ofFloat(btnAlbum, "rotation", 0f, 90f);
                animator3.setStartDelay(800);
                animator3.setDuration(500);
                animator3.setInterpolator(new LinearInterpolator());
                animator3.start();
                isRotated = true;
            }
        } else {
            if (!isRotated) {
                View view = findViewById(R.id.crop_hint);
                AnimatorSet animSet = new AnimatorSet();
                ObjectAnimator animator2 = ObjectAnimator.ofFloat(view, "rotation", 0f, 90f);
                ObjectAnimator moveIn = ObjectAnimator.ofFloat(view, "translationX", 0f, -50f);
                animSet.play(animator2).before(moveIn);
                animSet.setDuration(10);
                animSet.start();
                isRotated = true;
            }
        }
        mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_UI);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.e(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    private View.OnLongClickListener onLongClickListener=new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            switch (view.getId()){

                    case R.id.btn_close: //关闭相机
//                        mKqwSpeechSynthesizer.start("这是关闭相机按钮，长按可关闭相机");
                        finish();
                        break;
                    case R.id.btn_shutter: //拍照
                        if (mCameraPreview != null) {
//                            mKqwSpeechSynthesizer.start("这是拍照按钮，长按可进行拍照");
                            mCameraPreview.takePicture();
                        }
                        break;
                    case R.id.btn_album: //相册
                        Intent intent = new Intent();
                        /* 开启Pictures画面Type设定为image */
//                        mKqwSpeechSynthesizer.start("这是相册，长按可进入相册选择图片");
                        intent.setType("image/*");
                        /* 使用Intent.ACTION_GET_CONTENT这个Action */
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        /* 取得相片后返回本画面 */
                        startActivityForResult(intent, 1);
                        break;

            }
            return true;
        }
    };

    /**
     * 拍照界面
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_close: //关闭相机
                    mKqwSpeechSynthesizer.start("这是关闭相机按钮，长按可关闭相机");
//                    finish();
                    break;
                case R.id.btn_shutter: //拍照
                    if (mCameraPreview != null) {
                        mKqwSpeechSynthesizer.start("这是拍照按钮，长按可进行拍照");
//                        mCameraPreview.takePicture();
                    }
                    break;
                case R.id.btn_album: //相册
                    Intent intent = new Intent();
                    /* 开启Pictures画面Type设定为image */
                    mKqwSpeechSynthesizer.start("这是相册，长按可进入相册选择图片");
//                    intent.setType("image/*");
//                    /* 使用Intent.ACTION_GET_CONTENT这个Action */
//                    intent.setAction(Intent.ACTION_GET_CONTENT);
//                    /* 取得相片后返回本画面 */
//                    startActivityForResult(intent, 1);
                    break;
            }
        }
    };

    /**
     * 截图界面
     */
    private View.OnClickListener cropcper = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_closecropper:
                    showTakePhotoLayout();
                    break;
                case R.id.btn_startcropper:
                    //获取截图并旋转90度
                    Bitmap cropperBitmap = mCropImageView.getCroppedImage();

                    Bitmap bitmap;
                    bitmap = Utils.rotate(cropperBitmap, -90);

                    // 系统时间
                    long dateTaken = System.currentTimeMillis();
                    // 图像名称
                    String filename = DateFormat.format("yyyy-MM-dd kk.mm.ss", dateTaken).toString() + ".jpg";
                    Uri uri = insertImage(getContentResolver(), filename, dateTaken, PATH, filename, bitmap, null);
//                    boolean a=true;
//                    a=isUriValid(uri);
                    Intent intent = new Intent(context, ShowCropperedActivity.class);
                    intent.setData(uri);
                    intent.putExtra("path", PATH + filename);
                    intent.putExtra("width", bitmap.getWidth());
                    intent.putExtra("height", bitmap.getHeight());
//                  intent.putExtra("cropperImage", bitmap);
                    startActivity(intent);
                    bitmap.recycle();
                    finish();
                    break;
            }
        }
    };

    /**
     * 拍照成功后回调
     * 存储图片并显示截图界面
     */
    @Override
    public void onCameraStopped(byte[] data) {
        Log.i("TAG", "==onCameraStopped==");
        // 创建图像
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        if (!isTransverse) {
            bitmap = Utils.rotate(bitmap, 90);
        }

        // 系统时间
        long dateTaken = System.currentTimeMillis();
        // 图像名称
        String filename = DateFormat.format("yyyy-MM-dd kk.mm.ss", dateTaken).toString() + ".jpg";

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        bitmap = Utils.rotate(bitmap, 90);

        // 将Bitmap转换为Mat
        Mat src = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC3);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(-90, width / 2, height / 2);
        Bitmap bit = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        bitmapToMat(bit, src);
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        src = gray;
        // 边缘算法检测
        Mat cannyMat = src.clone();
        double threshold1 = 60;
        double threshold2 = threshold1 * 3;
        Imgproc.Canny(src, cannyMat, threshold1, threshold2);
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
        System.out.println(width+":bit");
        height= bit.getHeight();
        System.out.println(height+":bit");
        matrix.setRotate((float) angle, width / 2, height / 2);

        bitmap = Utils.rotate(bitmap, (int)angle-90);


        Bitmap rotatedBitmap = Bitmap.createBitmap(bit, 0, 0, width, height, matrix, true);
//        Uri source = insertImage(getContentResolver(), filename, dateTaken, PATH, filename, bitmap, data);
//        isUriValid(source);
        Mat re= new Mat();
        bitmapToMat(rotatedBitmap,re);

        Bitmap finalBitMap=rotatedBitmap;
        Mat resultMat = faceRecognition.detectFaces(re);
        matToBitmap(resultMat,finalBitMap);
//        matrix.setRotate(0,width / 2, height / 2);

        if(clarity(re)>=0){
            Intent intent = new Intent(context, ShowCropperedActivity.class);
//            intent.setData(source);
            intent.putExtra("path", PATH + filename);
            intent.putExtra("width", rotatedBitmap.getWidth());
            intent.putExtra("height", rotatedBitmap.getHeight());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);
            finalBitMap.compress(Bitmap.CompressFormat.JPEG, 60, stream);
            byte[] bitmapBytes = stream.toByteArray();
            intent.putExtra("bitmap",bitmapBytes);
            startActivity(intent);
            bitmap.recycle();
            finish();
        }else{
//            try {
            mKqwSpeechSynthesizer.changeListener();
            mKqwSpeechSynthesizer.start("图片清晰度不足，请保持摄像头稳定，重新拍照");

                Intent intent = new Intent(context, TakePhoteActivity.class);
                startActivity(intent);
                bitmap.recycle();
                finish();


        }

//        showCropperLayout();
    }

    /*
     * 获取图片回调
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            Log.e("uri", uri.toString());
            ContentResolver cr = this.getContentResolver();
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                //与拍照保持一致方便处理
                bitmap = Utils.rotate(bitmap, 90);
                mCropImageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                Log.e("Exception", e.getMessage(), e);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
        showCropperLayout();
    }

    /**
     * 存储图像并将信息添加入媒体数据库
     */
    private Uri insertImage(ContentResolver cr, String name, long dateTaken,
                            String directory, String filename, Bitmap source, byte[] jpegData) {
        OutputStream outputStream = null;
        String filePath = directory + filename;
        try {
            File dir = new File(directory);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(directory, filename);
            if (file.createNewFile()) {
                outputStream = new FileOutputStream(file);
                if (source != null) {
                    source.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                } else {
                    outputStream.write(jpegData);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Throwable t) {
                }
            }
        }
        ContentValues values = new ContentValues(7);
        values.put(MediaStore.Images.Media.TITLE, name);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
        values.put(MediaStore.Images.Media.DATE_TAKEN, dateTaken);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATA, filePath);
        System.out.println(IMAGE_URI);
        return cr.insert(IMAGE_URI, values);
    }
    private boolean isUriValid(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                inputStream.close();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showTakePhotoLayout() {
        mTakePhotoLayout.setVisibility(View.VISIBLE);
        mCropperLayout.setVisibility(View.GONE);
    }

    private void showCropperLayout() {
        mTakePhotoLayout.setVisibility(View.GONE);
        mCropperLayout.setVisibility(View.VISIBLE);
        mCameraPreview.start();   //继续启动摄像头
    }


    private float         mLastX       = 0;
    private float         mLastY       = 0;
    private float         mLastZ       = 0;
    private boolean       mInitialized = false;
    private SensorManager mSensorManager;
    private Sensor        mAccel;


    /**
     * 位移 自动对焦
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        if (!mInitialized) {
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            mInitialized = true;
        }
        float deltaX = Math.abs(mLastX - x);
        float deltaY = Math.abs(mLastY - y);
        float deltaZ = Math.abs(mLastZ - z);

        if (deltaX > 0.8 || deltaY > 0.8 || deltaZ > 0.8) {
            mCameraPreview.setFocus();
        }
        mLastX = x;
        mLastY = y;
        mLastZ = z;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    private static double clarity(Mat grayImage) {
        Mat laplacianDstImage = new Mat();
        Imgproc.Laplacian(grayImage, laplacianDstImage, CvType.CV_64F);
        MatOfDouble median = new MatOfDouble();
        MatOfDouble std = new MatOfDouble();
        Core.meanStdDev(laplacianDstImage, median, std);
        double clarity = Math.pow(std.get(0, 0)[0], 2);

        //后续可根据业务设置阈值
        Log.e("ymc", "清晰度：" + clarity);
        laplacianDstImage.release();
        return clarity;
    }
    public Mat detectFace(Mat inputImage) {
        // 将输入图像转换为灰度图像
        Mat grayImage = new Mat();
        Imgproc.cvtColor(inputImage, grayImage, Imgproc.COLOR_BGR2GRAY);

        // 在灰度图像中检测人脸
        MatOfRect faces = new MatOfRect();
        mJavaDetector.detectMultiScale(grayImage, faces, 1.1, 3, 0, new Size(30, 30));

        // 在原始图像上绘制矩形框以标记人脸
        for (Rect rect : faces.toArray()) {
            Imgproc.rectangle(inputImage, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 0, 0), 2);
        }

        return inputImage;
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.d(TAG, "OpenCV loaded successfully");
                System.loadLibrary("detection_based_tracker");
                File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                File cascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                // 从应用程序资源加载级联文件
                try (InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                     FileOutputStream os = new FileOutputStream(cascadeFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // 根据级联文件创建OpenCV的人脸检测器
                mJavaDetector = new CascadeClassifier(cascadeFile.getAbsolutePath());
               faceRecognition = new FaceRecognition(cascadeFile.getAbsolutePath());
                if (mJavaDetector.empty()) {
                    Log.d(TAG, "Failed to load cascade classifier");
                    mJavaDetector = null;
                } else {
                    Log.d(TAG, "Loaded cascade classifier from " + cascadeFile.getAbsolutePath());
                }
                cascadeDir.delete();
            } else {
                super.onManagerConnected(status);
            }
        }
    };
}

