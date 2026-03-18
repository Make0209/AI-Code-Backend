package com.hbpu.aicodebackend.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 项目下载服务
 */
public interface ProjectDownloadService {
    /**
     * 将项目打包成 zip 文件并下载
     *
     * @param projectPath      项目路径
     * @param downloadFileName 下载的文件名
     * @param response         HTTP 响应
     */
    void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);
}
