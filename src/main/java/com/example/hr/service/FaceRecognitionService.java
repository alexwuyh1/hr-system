package com.example.hr.service;

/**
 * 人脸识别服务接口
 * 定义统一的人脸验证标准，支持不同算法实现
 */
public interface FaceRecognitionService {
    
    /**
     * 人脸验证结果
     */
    record FaceResult(
        boolean matched,
        double similarity,
        double threshold,
        String algorithm
    ) {}
    
    /**
     * 验证两张人脸是否匹配
     * @param probeImage 待验证的人脸图像字节数组
     * @param referenceImage 参考人脸图像字节数组
     * @return 验证结果
     */
    FaceResult verify(byte[] probeImage, byte[] referenceImage) throws Exception;
    
    /**
     * 获取算法名称
     * @return 算法名称
     */
    String getAlgorithmName();
}
