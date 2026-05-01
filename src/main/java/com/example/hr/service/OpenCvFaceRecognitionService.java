package com.example.hr.service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import org.springframework.stereotype.Service;

/**
 * 基于 OpenCV 的人脸识别服务实现
 * 使用余弦相似度算法进行人脸验证
 */
@Service("openCvFaceRecognitionService")
public class OpenCvFaceRecognitionService implements FaceRecognitionService {
    
    private static final int IMAGE_SIZE = 200;
    private static final double COSINE_THRESHOLD = 0.75;

    public OpenCvFaceRecognitionService() {
        // 确保加载本地 OpenCV 库
        Loader.load(opencv_core.class);
        Loader.load(opencv_imgcodecs.class);
        Loader.load(opencv_imgproc.class);
    }

    @Override
    public FaceResult verify(byte[] probeImage, byte[] referenceImage) throws Exception {
        try {
            Mat avatar = readImage(referenceImage);
            Mat probe = readImage(probeImage);

            Mat avatarPrep = preprocess(avatar);
            Mat probePrep = preprocess(probe);

            double similarity = cosineSimilarity(avatarPrep, probePrep);
            boolean matched = similarity >= COSINE_THRESHOLD;
            
            return new FaceResult(
                matched, 
                similarity * 100.0, 
                COSINE_THRESHOLD * 100.0, 
                "opencv-cosine"
            );
        } catch (UnsatisfiedLinkError err) {
            throw new IllegalStateException("OpenCV 本地库不可用", err);
        }
    }

    @Override
    public String getAlgorithmName() {
        return "OpenCV 余弦相似度";
    }

    private Mat readImage(byte[] bytes) {
        Mat buf = new Mat(1, bytes.length, opencv_core.CV_8U);
        buf.data().put(bytes);
        return opencv_imgcodecs.imdecode(buf, opencv_imgcodecs.IMREAD_COLOR);
    }

    private Mat preprocess(Mat img) {
        Mat gray = new Mat();
        opencv_imgproc.cvtColor(img, gray, opencv_imgproc.COLOR_BGR2GRAY);
        Mat resized = new Mat();
        opencv_imgproc.resize(gray, resized, new Size(IMAGE_SIZE, IMAGE_SIZE));
        return resized;
    }

    private double cosineSimilarity(Mat a, Mat b) {
        if (a.rows() != b.rows() || a.cols() != b.cols()) {
            throw new IllegalArgumentException("人脸图像必须相同大小");
        }
        int total = a.rows() * a.cols();
        byte[] dataA = new byte[total];
        byte[] dataB = new byte[total];
        a.data().get(dataA);
        b.data().get(dataB);
        
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < total; i++) {
            int va = dataA[i] & 0xFF;
            int vb = dataB[i] & 0xFF;
            dot += (double) va * vb;
            normA += (double) va * va;
            normB += (double) vb * vb;
        }
        
        double denom = Math.sqrt(normA) * Math.sqrt(normB);
        if (denom == 0.0) {
            return 0.0;
        }
        double sim = dot / denom;
        return Math.max(0.0, Math.min(1.0, sim));
    }
}
