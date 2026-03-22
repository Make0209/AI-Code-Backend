package com.hbpu.aicodebackend.ai.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
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
import java.nio.file.StandardOpenOption;

@Slf4j
@Component
public class FileWriteTool extends BaseTool {

    @Tool("写入文件到指定路径")
    public String writeFile(
            @P("文件的相对路径") String relativeFilePath,
            @P("要写入文件的内容") String content,
            @ToolMemoryId Long appId
    ) {
        try {
            Path path = resolvePath(relativeFilePath, appId);
            Path parentDir = path.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }

            Files.writeString(
                    path,
                    StrUtil.nullToDefault(content, ""),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            String actualContent = Files.readString(path, StandardCharsets.UTF_8);
            boolean verified = StrUtil.equals(actualContent, StrUtil.nullToDefault(content, ""));
            String summary = verified
                    ? "文件写入并校验成功: " + relativeFilePath
                    : "文件已写入，但写后校验失败: " + relativeFilePath;

            JSONObject extra = new JSONObject();
            extra.set("operation", "write");
            extra.set("contentLength", StrUtil.length(StrUtil.nullToDefault(content, "")));
            return buildStructuredResult(verified ? "success" : "error", relativeFilePath, verified, summary, extra);
        } catch (IOException e) {
            String errorMessage = "文件写入失败: " + relativeFilePath + ", error: " + e.getMessage();
            log.error(errorMessage, e);
            return buildStructuredResult("error", relativeFilePath, false, errorMessage, null);
        }
    }

    @Override
    public String getToolName() {
        return "writeFile";
    }

    @Override
    public String getDisplayName() {
        return "写入文件";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments, String result) {
        String relativeFilePath = arguments == null ? null : arguments.getStr("relativeFilePath");
        String content = arguments == null ? null : arguments.getStr("content");
        String suffix = FileUtil.getSuffix(relativeFilePath);
        String toolReturn = extractToolReturn(result, "文件写入完成");

        StringBuilder bodyBuilder = new StringBuilder();
        if (StrUtil.isNotBlank(content)) {
            bodyBuilder.append(generateCodeBlock(normalize(suffix, "text"), content)).append("\n\n");
        }
        bodyBuilder.append("工具返回：").append(toolReturn);

        String title = String.format(
                "%s 写入文件：<code>%s</code>",
                resolveStatusIcon(result),
                normalize(relativeFilePath, "unknown")
        );
        return generateDetailsBlock("📝", title, bodyBuilder.toString());
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
