package com.hbpu.aicodebackend.ai.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

/**
 * Base class for file tools.
 */
public abstract class BaseTool {

    public abstract String getToolName();

    public abstract String getDisplayName();

    public String generateToolRequestResponse() {
        return "\n\n<div class=\"tool-status-loading\">\n" +
                "  <span class=\"spin-icon\">⚙️</span>\n" +
                "  <span class=\"status-text\">工具调用中...</span>\n" +
                "</div>\n\n";
    }

    public abstract String generateToolExecutedResult(JSONObject arguments, String result);

    public String generateToolHistoryEntry(JSONObject arguments, String result) {
        JSONObject structured = parseStructuredResult(result);
        String summary = structured == null
                ? normalize(result, "工具执行完成")
                : normalize(structured.getStr("summary"), normalize(result, "工具执行完成"));
        return String.format("\n[工具:%s] %s\n", getDisplayName(), summary);
    }

    protected String buildStructuredResult(
            String status,
            String path,
            boolean verified,
            String summary,
            JSONObject extra
    ) {
        JSONObject result = new JSONObject();
        result.set("status", status);
        result.set("path", path);
        result.set("verified", verified);
        result.set("summary", summary);
        if (extra != null) {
            extra.forEach(result::set);
        }
        return JSONUtil.toJsonStr(result);
    }

    protected JSONObject parseStructuredResult(String result) {
        if (StrUtil.isBlank(result) || !JSONUtil.isTypeJSON(result)) {
            return null;
        }
        try {
            return JSONUtil.parseObj(result);
        } catch (Exception ignored) {
            return null;
        }
    }

    protected String generateDetailsBlock(String toolIcon, String title, String body) {
        return String.format(
                "<details class=\"tool-call-block\">\n" +
                        "<summary>\n" +
                        "  <span class=\"chevron-icon\">▼</span>\n" +
                        "  <span class=\"tool-icon\">%s</span>\n" +
                        "  <span class=\"tool-title\">%s</span>\n" +
                        "</summary>\n\n" +
                        "%s\n\n" +
                        "</details>\n",
                toolIcon,
                title,
                body
        );
    }

    protected String generateCodeBlock(String language, String content) {
        return String.format("```%s\n%s\n```", normalize(language, "text"), normalize(content, ""));
    }

    protected String normalize(String value, String defaultValue) {
        return StrUtil.blankToDefault(value, defaultValue);
    }

    protected String resolveStatusIcon(String result) {
        JSONObject structured = parseStructuredResult(result);
        String status = structured == null ? null : structured.getStr("status");
        Boolean verified = structured == null ? null : structured.getBool("verified");
        if ("error".equals(status)) {
            return "❌";
        }
        if ("warning".equals(status)) {
            return "⚠️";
        }
        if (Boolean.FALSE.equals(verified)) {
            return "⚠️";
        }
        return "✅";
    }

    protected String extractSummary(String result, String fallback) {
        JSONObject structured = parseStructuredResult(result);
        return structured == null ? normalize(result, fallback) : normalize(structured.getStr("summary"), fallback);
    }

    protected String extractToolReturn(String result, String fallback) {
        JSONObject structured = parseStructuredResult(result);
        if (structured == null) {
            return normalize(result, fallback);
        }
        return normalize(structured.getStr("summary"), fallback);
    }

}
