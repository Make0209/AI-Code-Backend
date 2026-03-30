package com.hbpu.aicodebackend.ai.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import com.hbpu.aicodebackend.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class FileReadTool extends BaseTool {

    @Tool("读取指定路径的文件内容")
    public String readFile(
            @P("文件的相对路径") String relativeFilePath,
            @ToolMemoryId Long appId
    ) {
        try {
            Path path = resolvePath(relativeFilePath, appId);
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                return "错误：文件不存在或不是普通文件 - " + relativeFilePath;
            }
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            String errorMessage = "读取文件失败: " + relativeFilePath + ", error: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    @Override
    public String getToolName() {
        return "readFile";
    }

    @Override
    public String getDisplayName() {
        return "读取文件";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments, String result) {
        String relativeFilePath = arguments == null ? null : arguments.getStr("relativeFilePath");
        String suffix = FileUtil.getSuffix(relativeFilePath);

        String title = String.format(
                "%s 读取文件：<code>%s</code>",
                resolveStatusIcon(result),
                normalize(relativeFilePath, "unknown")
        );
        return generateDetailsBlock("📖", title, generateCodeBlock(normalize(suffix, "text"), normalize(result, "")));
    }

    @Override
    public String generateToolHistoryEntry(JSONObject arguments, String result) {
        String relativeFilePath = arguments == null ? null : arguments.getStr("relativeFilePath");
        return String.format("\n[工具:%s] 已读取文件: %s\n", getDisplayName(), normalize(relativeFilePath, "unknown"));
    }

    private Path resolvePath(String relativeFilePath, Long appId) {
        Path path = Paths.get(relativeFilePath);
        if (path.isAbsolute()) {
            return path;
        }
        String projectDirName = "vue_project_" + appId;
        Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
        return projectRoot.resolve(relativeFilePath);
    }
}
