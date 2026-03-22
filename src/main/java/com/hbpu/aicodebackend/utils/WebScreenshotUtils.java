package com.hbpu.aicodebackend.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.hbpu.aicodebackend.exception.BusinessException;
import com.hbpu.aicodebackend.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

/**
 * 网页截图工具类，使用 Chrome 浏览器进行网页截图。
 */
@Slf4j
@Component
public class WebScreenshotUtils {

    private WebDriver webDriver;
    private final Object lock = new Object();

    // ✅ 懒加载，第一次用的时候才初始化，失败了不影响其他功能
    private WebDriver getDriver() {
        if (webDriver == null) {
            synchronized (lock) {
                if (webDriver == null) {
                    webDriver = initEdgeDriver(1600, 900);
                }
            }
        }
        return webDriver;
    }

    // ✅ 改成实例方法
    public String saveWebPageScreenshot(String webUrl) {
        if (StrUtil.isBlank(webUrl)) {
            log.error("网页URL不能为空");
            return null;
        }
        try {
            String rootPath = System.getProperty("user.dir") + File.separator + "tmp"
                    + File.separator + "screenshots"
                    + File.separator + UUID.randomUUID().toString().substring(0, 8);
            FileUtil.mkdir(rootPath);
            final String IMAGE_SUFFIX = ".png";
            String imageSavePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + IMAGE_SUFFIX;

            WebDriver driver = getDriver();  // ✅ 懒加载获取
            driver.get(webUrl);
            waitForPageLoad(driver);
            byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            saveImage(screenshotBytes, imageSavePath);
            log.info("原始截图保存成功: {}", imageSavePath);

            final String COMPRESSION_SUFFIX = "_compressed.jpg";
            String compressedImagePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + COMPRESSION_SUFFIX;
            compressImage(imageSavePath, compressedImagePath);
            log.info("压缩图片保存成功: {}", compressedImagePath);

            FileUtil.del(imageSavePath);
            return compressedImagePath;
        } catch (Exception e) {
            log.error("网页截图失败: {}", webUrl, e);
            // ✅ 截图失败就重置 driver，下次重新初始化
            resetDriver();
            return null;
        }
    }

    // ✅ @PreDestroy 现在真的有效了
    @PreDestroy
    public void destroy() {
        if (webDriver != null) {
            try {
                webDriver.quit();
                log.info("Chrome WebDriver 已关闭");
            } catch (Exception e) {
                log.warn("关闭 WebDriver 时出现异常", e);
            }
        }
    }

    private void resetDriver() {
        try {
            if (webDriver != null) webDriver.quit();
        } catch (Exception ignored) {}
        webDriver = null;
    }

    private WebDriver initEdgeDriver(int width, int height) {
        try {
            // ✅ Edge 驱动从微软服务器下载，国内直接能访问，不需要镜像
            WebDriverManager.edgedriver().setup();

            EdgeOptions options = new EdgeOptions();
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments(String.format("--window-size=%d,%d", width, height));
            options.addArguments("--disable-extensions");
            options.addArguments(
                    "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36 Edg/133.0.0.0"
            );

            WebDriver driver = new EdgeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            log.info("Edge WebDriver 初始化成功");
            return driver;
        } catch (Exception e) {
            log.error("初始化 Edge 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Edge 浏览器失败");
        }
    }

    /**
     * 保存图片到文件
     *
     * @param imageBytes 图片字节数组
     * @param imagePath  图片保存路径
     */
    private static void saveImage(byte[] imageBytes, String imagePath) {
        try {
            FileUtil.writeBytes(imageBytes, imagePath);
        } catch (Exception e) {
            log.error("保存图片失败: {}", imagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }

    /**
     * 压缩图片，使用 Hutool 工具类
     *
     * @param originalImagePath   原始图片路径
     * @param compressedImagePath 压缩后图片保存路径
     */
    private static void compressImage(String originalImagePath, String compressedImagePath) {
        // 压缩图片质量（0.1 = 10% 质量）
        final float COMPRESSION_QUALITY = 0.8f;
        try {
            ImgUtil.compress(
                    FileUtil.file(originalImagePath),
                    FileUtil.file(compressedImagePath),
                    COMPRESSION_QUALITY
            );
        } catch (Exception e) {
            log.error("压缩图片失败: {} -> {}", originalImagePath, compressedImagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }
    }

    /**
     * 等待页面加载完成
     *
     * @param driver WebDriver
     */
    private static void waitForPageLoad(WebDriver driver) {
        try {
            // 创建等待页面加载对象
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            // 等待 document.readyState 为complete
            wait.until(webDriver ->
                               Objects.equals(
                                       ((JavascriptExecutor) webDriver).executeScript("return document.readyState"),
                                       "complete"
                               )
            );
            // 额外等待一段时间，确保动态内容加载完成
            Thread.sleep(2000);
            log.info("页面加载完成");
        } catch (Exception e) {
            log.error("等待页面加载时出现异常，继续执行截图", e);
        }
    }

}
