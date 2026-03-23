package com.example.hr.service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.springframework.stereotype.Service;

/**
 * OpenCV-based face verification (MVP).
 * Uses LBPH model trained from the stored avatar and compares probe image.
 */
@Service
public class OpenCvFaceService {
  private static final int IMAGE_SIZE = 200;
  private static final double CONFIDENCE_THRESHOLD = 60.0;

  public OpenCvFaceService() {
    // Ensure native OpenCV libs are loaded.
    Loader.load(opencv_core.class);
    Loader.load(opencv_imgcodecs.class);
    Loader.load(opencv_imgproc.class);
  }

  public VerificationResult verify(InputStream probeStream, Path avatarPath) throws Exception {
    try {
      if (avatarPath == null || !Files.exists(avatarPath)) {
        throw new IllegalArgumentException("Avatar not found");
      }
      Mat avatar = readImage(Files.readAllBytes(avatarPath));
      Mat probe = readImage(probeStream.readAllBytes());

      Mat avatarPrep = preprocess(avatar);
      Mat probePrep = preprocess(probe);

      LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
      MatVector images = new MatVector(1);
      images.put(0, avatarPrep);
      Mat labels = new Mat(1, 1, opencv_core.CV_32SC1);
      labels.ptr(0).putInt(1);
      recognizer.train(images, labels);

      int[] label = new int[1];
      double[] confidence = new double[1];
      recognizer.predict(probePrep, label, confidence);
      boolean matched = label[0] == 1 && confidence[0] <= CONFIDENCE_THRESHOLD;
      return new VerificationResult(matched, confidence[0], CONFIDENCE_THRESHOLD);
    } catch (UnsatisfiedLinkError err) {
      throw new IllegalStateException("OpenCV native libraries not available", err);
    }
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

  public record VerificationResult(boolean matched, double confidence, double threshold) {}
}
