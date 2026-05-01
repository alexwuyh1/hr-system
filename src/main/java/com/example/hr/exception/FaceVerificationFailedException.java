package com.example.hr.exception;

import org.springframework.http.HttpStatus;

public class FaceVerificationFailedException extends BusinessException {
    public FaceVerificationFailedException() {
        super("FACE_VERIFICATION_FAILED", HttpStatus.BAD_REQUEST, "人脸验证失败");
    }

    public FaceVerificationFailedException(double similarity, double threshold) {
        super("FACE_VERIFICATION_FAILED", HttpStatus.BAD_REQUEST, 
            String.format("人脸相似度 %.1f%% 低于阈值 %.0f%%", similarity, threshold));
    }
}
