package com.wt.ocr.utils;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class FaceRecognition {
    private CascadeClassifier faceCascade;

    public FaceRecognition(String cascadeFile) {
        // 加载人脸检测器级联分类器
        faceCascade = new CascadeClassifier();
        faceCascade.load(cascadeFile);
    }

    public Mat detectFaces(Mat inputImage) {
        // 将输入图像转换为灰度图像
        Mat grayImage = new Mat();
        Imgproc.cvtColor(inputImage, grayImage, Imgproc.COLOR_BGR2GRAY);
        // 在灰度图像中检测人脸
        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(grayImage, faces, 1.1, 3, 0, new Size(30, 30));
        // 在原始图像上绘制矩形框以标记人脸
        for (Rect rect : faces.toArray()) {
            Imgproc.rectangle(inputImage, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 0, 0), 2);
        }

        return inputImage;
    }
}

