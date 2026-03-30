package com.hbpu.aicodebackend.ai.tools;

import cn.hutool.json.JSONObject;
import com.hbpu.aicodebackend.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class FileDeleteTool extends BaseTool {

    @Tool("删除指定路径的文件")
    public String deleteFile(
            @P("文件的相对路径") String relativeFilePath,
            @ToolMemoryId Long appId
    ) {
        try {
            Path path = resolvePath(relativeFilePath, appId);
            if (!Files.exists(path)) {
                return buildStructuredResult("warning", relativeFilePath, false, "文件不存在，无需删除: " + relativeFilePath, null);
            }
            if (!Files.isRegularFile(path)) {
                return buildStructuredResult("error", relativeFilePath, false, "指定路径不是普通文件: " + relativeFilePath, null);
            }

            String fileName = path.getFileName().toString();
            if (isImportantFile(fileName)) {
                return buildStructuredResult("error", relativeFilePath, false, "不允许删除重要文件: " + fileName, null);
            }

            Files.delete(path);
            boolean verified = !Files.exists(path);
            String summary = verified
                    ? "文件删除并校验成功: " + relativeFilePath
                    : "文件已删除请求，但校验失败: " + relativeFilePath;
            return buildStructuredResult(verified ? "success" : "error", relativeFilePath, verified, summary, null);
        } catch (IOException e) {
            String errorMessage = "删除文件失败: " + relativeFilePath + ", error: " + e.getMessage();
            log.error(errorMessage, e);
            return buildStructuredResult("error", relativeFilePath, false, errorMessage, null);
        }
    }

    @Override
    public String getToolName() {
        return "deleteFile";
    }

    @Override
    public String getDisplayName() {
        return "删除文件";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments, String result) {
        String relativeFilePath = arguments == null ? null : arguments.getStr("relativeFilePath");
        String title = String.format(
                "%s 删除文件：<code>%s</code>",
                resolveStatusIcon(result),
                normalize(relativeFilePath, "unknown")
        );
        return generateDetailsBlock("🗑️", title, "> " + extractSummary(result, "文件删除完成"));
    }

    private boolean isImportantFile(String fileName) {
        String[] importantFiles = {
                "package.json", "package-lock.json", "yarn.lock", "pnpm-lock.yaml",
                "vite.config.js", "vite.config.ts", "vue.config.js",
                "tsconfig.json", "tsconfig.app.json", "tsconfig.node.json",
                "index.html", "main.js", "main.ts", "App.vue", ".gitignore", "README.md"
        };
        for (String important : importantFiles) {
            if (important.equalsIgnoreCase(fileName)) {
                return true;
            }
        }
        return false;
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
