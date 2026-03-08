package com.hbpu.aicodebackend.ai;

import com.hbpu.aicodebackend.ai.model.HtmlCodeResult;
import com.hbpu.aicodebackend.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class AiCodeGeneratorServiceTest {
    @Resource
    private AiCodeGeneratorService service;

    @Test
    void generateHtmlCode() {
        HtmlCodeResult result = service.generateHtmlCode("写个登录页面，只需要20行代码");
        Assertions.assertNotNull(result);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult multiFileCode = service.generateMultiFileCode("写个简单的登录页面");
        Assertions.assertNotNull(multiFileCode);
    }

}