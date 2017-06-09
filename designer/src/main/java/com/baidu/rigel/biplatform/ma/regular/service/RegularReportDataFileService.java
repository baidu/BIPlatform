

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

package com.baidu.rigel.biplatform.ma.regular.service;

import java.util.Map;

import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;

/** 
 * 固定报表数据文件服务
 * @author yichao.jiang 
 * @version  2015年7月28日 
 * @since jdk 1.8 or after
 */
public interface RegularReportDataFileService {
       
    /**
     * 将每个区域对应的dataModel，转为统一的JSON字符串
     * 其中JSON的Key为每个区域的areaId,Value为每个区域的数据模型（表格为PlaneTable，图形为DIReport)
     * @param reportModel
     * @param dataModels
     * @return
     */
    public String convertData2Json(ReportDesignModel reportModel, Map<String, DataModel> dataModels);
    
    /**
     * 存储固定报表的数据文件JSON
     * @param reportId 报表id
     * @param taskId 任务id
     * @param authority 权限
     * @param dataJson 数据JSON
     * @return
     */
    public boolean saveRegularReportDataFile(String reportId, String taskId, String authority, String dataJson);
    
    /**
     * 读取固定报表的数据文件JSON
     * @param reportId
     * @param taskId
     * @param authority
     * @return
     */
    public String readRegularReportDataFile(String reportId, String taskId, String authority); 
    
    /**
     * 读取固定报表的数据文件JSON
     * @param reportId 报表id
     * @param taskId 任务id
     * @param authority 权限
     * @param time 时间
     * @return
     */
    public String readRegularReportDataFile(String reportId, String taskId, String authority, String time);
}

