

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

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.baidu.rigel.biplatform.ma.regular.report.RegularReportParam;
import com.baidu.rigel.biplatform.ma.regular.report.RegularReportTaskInfo;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;

/** 
 * 固定报表设置服务接口
 * @author yichao.jiang 
 * @version  2015年8月5日 
 * @since jdk 1.8 or after
 */
public interface RegularReportSettingService {
    
    /**
     * 保存固定报表设置信息
     * @param reportId 报表id
     * @param taskBo 封装的任务实体
     * @return
     */
    public ReportDesignModel saveRegularReportSetting(String reportId, RegularReportTaskInfo taskBo);
    
    /**
     * 获取某个固定报表的设置信息
     * @param reportId
     * @param taskId
     * @return
     */
    public RegularReportTaskInfo getRegularReportSetting(String reportId, String taskId);
    
    /**
     * 获取某个报表下的所有固定报表的设置信息
     * @param reportId
     * @return
     */
    public List<RegularReportTaskInfo> getRegularReportSetting(String reportId);
    
    /**
     * 获取某个报表的所有查询参数
     * 对于多维报表，取参数纬度P对应的参数；对于平面表，取条件轴上的参数
     * @param reportId 报表id
     * @return
     */
    public Set<RegularReportParam> getAllParamsOfReportModel(String reportId);
    
    /**
     * 获取某个固定报表的查询参数
     * @param reportId 报表id
     * @param taskId 任务id
     * @return
     */
    public Set<RegularReportParam> getAllParamsOfReguarTask(String reportId, String taskId);
    
    /**
     * 获取固定报表中的权限信息
     * @param reportId 报表id
     * @param taskId 任务id
     * @return
     */
    public List<String> getAuthoritysOfRegularReport(String reportId, String taskId);
    
    /**
     * 获取固定报表的参数树信息
     * @param reportId 报表id
     * @param taskId 固定报表id
     * @param paramId 参数id
     * @param id 节点id
     * @param name 节点名称
     * @return 该节点对应的子节点
     */
    public List<Map<String, Object>> getParamTreeOfRegularReport(String reportId, String taskId, 
            String paramId, String id, String name, Map<String, String> params);
    
    /**
     * 判断报表中的某个固定报表名称是否存在
     * @param reportId
     * @param taskName
     * @return
     */
    public boolean isTaskNameExists(String reportId, String taskName);
}

