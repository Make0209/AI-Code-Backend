package com.hbpu.aicodebackend.service;

/**
 * 截图服务接口，定义了生成并上传网页截图的抽象方法
 */
public interface ScreenshotService {
    /**
     * 生成并上传网页截图的抽象方法
     * @param webUrl 网页URL
     * @return 截图的访问URL
     */
    String generateAndUploadScreenshot(String webUrl);
}
