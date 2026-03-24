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
import org.springframework.stereotype.Service;

/**
 * OpenCV-based face verification (MVP).
 * Uses cosine similarity on normalized pixel vectors for MVP verification.
 */
@Service
public class OpenCvFaceService {
  private static final int IMAGE_SIZE = 200;
  private static final double COSINE_THRESHOLD = 0.75;

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

      double similarity = cosineSimilarity(avatarPrep, probePrep);
      boolean matched = similarity >= COSINE_THRESHOLD;
      double similarityPercent = similarity * 100.0;
      double thresholdPercent = COSINE_THRESHOLD * 100.0;
      double distancePercent = 100.0 - similarityPercent;
      return new VerificationResult(matched, similarityPercent, thresholdPercent, distancePercent);
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

  private double cosineSimilarity(Mat a, Mat b) {
    if (a.rows() != b.rows() || a.cols() != b.cols()) {
      throw new IllegalArgumentException("Face images must be the same size");
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

  public record VerificationResult(
      boolean matched, double similarity, double threshold, double distance) {}
}
