

/**
 * Copyright (c) 2015 Baidu, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baidu.rigel.biplatform.ma.regular.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ma.report.utils.ContextManager;

/** 
 * 固定报表数据文件存储工具类 
 * @author yichao.jiang 
 * @version  2015年7月29日 
 * @since jdk 1.8 or after
 */
@Service
public class RegularReportDataFileUtils {
    
    /**
     * 日志对象
     */
    private static final Logger LOG = LoggerFactory.getLogger(RegularReportDataFileUtils.class);
    
    /**
     * 固定报表存储路径中的日期格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    
    /**
     * 固定报表存储根目录
     */
    private static String regularReportBaseDir;
    
    /**
     * 注入固定报表存放路径
     * @param RegularReportDir
     */
    @Value("${biplatform.ma.regular.report.base.dir}")
    public void setRegularReportBaseDir(String regularReportDir) {
        RegularReportDataFileUtils.regularReportBaseDir = regularReportDir;
    }
   
    /**
     * 获取基于产品线的固定报表存储根目录
     * @return 
     */
    private static String genFileBaseDir4RegularReport() {
        return ContextManager.getProductLine() + File.separator + regularReportBaseDir;
    }
    
    /**
     * TODO获取当前日期对应的字符串，临时定义到日粒度
     * @return
     */
    private static String genCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        return dateFormat.format(new Date());
    }
    
    /**
     * TODO 考虑是否修改
     * 获取固定报表存放的路径信息,形如产品线/固定报表/报表id/任务id/时间/权限（如果有岗位或者其他权限)
     * @param reportId 报表id
     * @param taskId 任务id
     * @param authority 权限
     * @return
     */
    public static String genDataFilePath4RegularReport(String reportId, String taskId, String authority, String time) {
        String filePath = genFilePath4RegularReportWithoutTime(reportId, taskId);
        // 添加日期
        if (!StringUtils.isEmpty(time)) {
            filePath = filePath + File.separator + time;
        } else {
            filePath = filePath + File.separator + genCurrentTime();
        }
        if (!StringUtils.isEmpty(authority)) {
            // 添加权限
            filePath = filePath + File.separator + authority;            
        }
        LOG.info("the current Regular report file path is " + filePath);
        return filePath;
    }
    
    /**
     * 获取固定报表基于时间的目录
     * @param reportId
     * @param taskId
     * @param time
     * @return
     */
    public static String genFilePath4RegularReportWithTime(String reportId, String taskId, String time) {
        String filePath = genFilePath4RegularReportWithoutTime(reportId, taskId);
        filePath = filePath + File.separator + time;
        LOG.info("the current Regular report file path is " + filePath);
        return filePath;
    }

    /**
     * 获取固定报表的根目录路径信息，产品线/reportId/taskId
     * @param reportId
     * @param taskId
     * @return
     */
    private static String genFilePath4RegularReportWithoutTime(String reportId, String taskId) {
        String filePath = "";
        String fileStoreBaseDir = genFileBaseDir4RegularReport();
        // 添加报表id
        filePath = fileStoreBaseDir + File.separator + reportId;
        // 添加任务id
        filePath = filePath + File.separator + taskId;
        return filePath;
    }
}

