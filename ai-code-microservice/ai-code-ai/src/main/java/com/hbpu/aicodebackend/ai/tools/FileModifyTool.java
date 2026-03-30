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
public class FileModifyTool extends BaseTool {

    @Tool("修改文件内容，用新内容替换指定旧内容")
    public String modifyFile(
            @P("文件的相对路径") String relativeFilePath,
            @P("要替换的旧内容") String oldContent,
            @P("替换后的新内容") String newContent,
            @ToolMemoryId Long appId
    ) {
        try {
            Path path = resolvePath(relativeFilePath, appId);
            if (!Files.exists(path) || !Files.isRegularFile(path)) {
                return buildStructuredResult(
                        "error",
                        relativeFilePath,
                        false,
                        "文件不存在或不是普通文件: " + relativeFilePath,
                        null
                );
            }

            String originalContent = Files.readString(path, StandardCharsets.UTF_8);
            int matchCount = countMatches(originalContent, oldContent);

            if (matchCount == 0) {
                JSONObject extra = new JSONObject();
                extra.set("operation", "modify");
                extra.set("matchCount", 0);
                return buildStructuredResult(
                        "warning",
                        relativeFilePath,
                        false,
                        "未找到要替换的内容，文件未修改: " + relativeFilePath,
                        extra
                );
            }

            if (matchCount > 1) {
                JSONObject extra = new JSONObject();
                extra.set("operation", "modify");
                extra.set("matchCount", matchCount);
                return buildStructuredResult(
                        "warning",
                        relativeFilePath,
                        false,
                        "命中多个相同片段，已拒绝修改以避免误改: " + relativeFilePath,
                        extra
                );
            }

            String modifiedContent = originalContent.replaceFirst(
                    java.util.regex.Pattern.quote(StrUtil.nullToDefault(oldContent, "")),
                    java.util.regex.Matcher.quoteReplacement(StrUtil.nullToDefault(newContent, ""))
            );

            Files.writeString(
                    path,
                    modifiedContent,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            String actualContent = Files.readString(path, StandardCharsets.UTF_8);
            boolean verified = StrUtil.equals(actualContent, modifiedContent)
                    && !actualContent.contains(StrUtil.nullToDefault(oldContent, ""))
                    && actualContent.contains(StrUtil.nullToDefault(newContent, ""));

            JSONObject extra = new JSONObject();
            extra.set("operation", "modify");
            extra.set("matchCount", 1);
            extra.set("contentLength", StrUtil.length(StrUtil.nullToDefault(newContent, "")));

            String summary = verified
                    ? "文件修改并校验成功: " + relativeFilePath
                    : "文件已修改，但写后校验失败: " + relativeFilePath;
            return buildStructuredResult(verified ? "success" : "error", relativeFilePath, verified, summary, extra);
        } catch (IOException e) {
            String errorMessage = "修改文件失败: " + relativeFilePath + ", error: " + e.getMessage();
            log.error(errorMessage, e);
            return buildStructuredResult("error", relativeFilePath, false, errorMessage, null);
        }
    }

    @Override
    public String getToolName() {
        return "modifyFile";
    }

    @Override
    public String getDisplayName() {
        return "修改文件";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments, String result) {
        String relativeFilePath = arguments == null ? null : arguments.getStr("relativeFilePath");
        String newContent = arguments == null ? null : arguments.getStr("newContent");
        String suffix = FileUtil.getSuffix(relativeFilePath);
        String toolReturn = extractToolReturn(result, "文件修改完成");

        StringBuilder bodyBuilder = new StringBuilder();
        if (StrUtil.isNotBlank(newContent)) {
            bodyBuilder.append(generateCodeBlock(normalize(suffix, "text"), newContent)).append("\n\n");
        }
        bodyBuilder.append("工具返回：").append(toolReturn);

        String title = String.format(
                "%s 修改文件：<code>%s</code>",
                resolveStatusIcon(result),
                normalize(relativeFilePath, "unknown")
        );
        return generateDetailsBlock("🛠️", title, bodyBuilder.toString());
    }

    private int countMatches(String content, String target) {
        if (StrUtil.isEmpty(target)) {
            return 0;
        }
        int count = 0;
        int index = 0;
        while ((index = content.indexOf(target, index)) >= 0) {
            count++;
            index += target.length();
        }
        return count;
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
