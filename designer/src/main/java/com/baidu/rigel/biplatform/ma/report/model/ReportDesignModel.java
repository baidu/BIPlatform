/**
 * Copyright (c) 2014 Baidu, Inc. All Rights Reserved.
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
package com.baidu.rigel.biplatform.ma.report.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.model.Schema;
import com.baidu.rigel.biplatform.ma.model.meta.StarModel;
import com.baidu.rigel.biplatform.ma.model.utils.UuidGeneratorUtils;
import com.baidu.rigel.biplatform.ma.regular.report.RegularReportTaskInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Maps;

/**
 * 
 * 报表设计态模型定义： 报表模型由多个扩展区域组成，每个扩展区域包含逻辑模型定义，参数定义 格式定义以及数据模型定义等
 * 
 * @author david.wang
 *
 */
@JsonIgnoreProperties
public class ReportDesignModel implements Serializable {
    
    /**
     * seralize id
     */
    private static final long serialVersionUID = 1043055016254402266L;
    
    /**
     * id 无业务含义
     */
    private String id;
    
    /**
     * runTimeId
     */
    private String runTimeId;
    
    /**
     * 引用的数据源id
     */
    private String dsId;
    
    /**
     * 报表名称
     */
    private String name;
    
    /**
     * 版本
     */
    private String version;
    
    /**
     * 创建者
     */
    private String creator;
    
    /**
     * 创建时间
     */
    private long createTime;
    
    /**
     * 修改者
     */
    private String lastModifyUser;
    
    /**
     * 修改时间
     */
    private long lastModifyTime;
    
    /**
     * 对应的schema定义
     */
    private Schema schema;
    
    /**
     * 模型持久化状态 创建、更新后，状态为false，明显save操作之后，状态为true
     */
    private boolean persStatus = false;
    
    /**
     * 扩展区域定义
     */
    private Map<String, ExtendArea> extendAreas = new HashMap<String, ExtendArea>();
    
    /**
     * 兼容旧报表，json文件定义
     */
    private String jsonContent;
    
    /**
     * 兼容旧报表 vm文件定义
     */
    private String vmContent;
    
    /**
     * 主题
     */
    private String theme;
    
    /**
     * 报表参数映射关系
     */
    private Map<String, ReportParam> params = Maps.newHashMap();
       
    /**
     * 平面表对应的条件
     */
    private Map<String, PlaneTableCondition> planeTableConditions = Maps.newHashMap();
    
    /**
     * 报表对应的固定报表任务
     */
    private Map<String, RegularReportTaskInfo> regularTasks = Maps.newHashMap();
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getDsId() {
        return dsId;
    }
    
    public void setDsId(String dsId) {
        this.dsId = dsId;
    }
        
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getCreator() {
        return creator;
    }
    
    public void setCreator(String creator) {
        this.creator = creator;
    }
    
    public long getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
    
    public String getLastModifyUser() {
        return lastModifyUser;
    }
    
    public void setLastModifyUser(String lastModifyUser) {
        this.lastModifyUser = lastModifyUser;
    }
    
    public long getLastModifyTime() {
        return lastModifyTime;
    }
    
    public void setLastModifyTime(long lastModifyTime) {
        this.lastModifyTime = lastModifyTime;
    }
    
    public Schema getSchema() {
        return schema;
    }
    
    public void setSchema(Schema schema) {
        this.schema = schema;
    }
    
    public Map<String, ExtendArea> getExtendAreas() {
        return Collections.unmodifiableMap(this.extendAreas);
    }
    
    public void setExtendAreas(Map<String, ExtendArea> extendAreas) {
        this.extendAreas = extendAreas;
    }
    
    /**
     * 获取扩展区域列表
     * 
     * @return
     */
    @JsonIgnore
    public ExtendArea[] getExtendAreaList() {
        return extendAreas.values().toArray(new ExtendArea[0]);
    }
    
    /**
     * 将报表所引用的schema中的逻辑模型抓换成星型模型
     * 
     * @return
     */
    public StarModel[] toStarModel() {
        return null;
    }
    
    public boolean isPersStatus() {
        return persStatus;
    }
    
    public void setPersStatus(boolean persStatus) {
        this.persStatus = persStatus;
    }
    
    public String getJsonContent() {
        return jsonContent;
    }
    
    public void setJsonContent(String jsonContent) {
        this.jsonContent = jsonContent;
    }
    
    public String getVmContent() {
        return vmContent;
    }
    
    public void setVmContent(String vmContent) {
        this.vmContent = vmContent;
    }
    
    
    /**
     * 添加扩展区域
     * 
     * @param extendArea
     */
    public void addExtendArea(ExtendArea extendArea) {
        if (StringUtils.isEmpty(extendArea.getId())) {
            extendArea.setId(UuidGeneratorUtils.generate());
        }
        this.extendAreas.put(extendArea.getId(), extendArea);
    }
    
    /**
     * 依据扩展区域id获取扩展区域定义
     * 
     * @param id
     *            扩展区域id
     * @return 扩展区域定义
     */
    public ExtendArea getExtendById(String id) {
        return this.extendAreas.get(id);
    }
    
    /**
     * 删除质地过区域
     * 
     * @param extendAreaId
     */
    public void deleteAreaById(String extendAreaId) {
        ExtendArea target = this.extendAreas.get(extendAreaId);
        if (target.getType() == ExtendAreaType.LITEOLAP) {
            String selectionId = ((LiteOlapExtendArea) target).getSelectionAreaId();
            String tableId = ((LiteOlapExtendArea) target).getTableAreaId();
            String chartId = ((LiteOlapExtendArea) target).getChartAreaId();
            if (StringUtils.hasText(tableId)) {
                deleteAreaById(tableId);
            }
            if (StringUtils.hasText(chartId)) {
                deleteAreaById(chartId);
            }
            if (StringUtils.hasText(selectionId)) {
                deleteAreaById(selectionId);
            }
        }
        this.extendAreas.remove(extendAreaId);
    }
    
    /**
     * get the runTimeId
     * 
     * @return the runTimeId
     */
    public String getRunTimeId() {
        return runTimeId;
    }
    
    /**
     * set the runTimeId
     * 
     * @param runTimeId
     *            the runTimeId to set
     */
    public void setRunTimeId(String runTimeId) {
        this.runTimeId = runTimeId;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.getName() + ": " + this.getVersion();
    }

    /**
     * @return the params
     */
    public Map<String, ReportParam> getParams() {
        if (this.params == null) {
            this.params = Maps.newHashMap();
        }
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(Map<String, ReportParam> params) {
        this.params = params;
    }

    /**
     * @return the theme
     */
    public String getTheme() {
        if (StringUtils.isEmpty(this.theme)) {
            this.theme = "di";
        }
        return theme;
    }

    /**
     * @param theme the theme to set
     */
    public void setTheme(String theme) {
        this.theme = theme;
    }
    
    /**
     * 获取平面表查询条件
     * @return
     */
	public Map<String, PlaneTableCondition> getPlaneTableConditions() {
	    if (this.planeTableConditions == null) {
	        this.planeTableConditions = Maps.newHashMap();
	    }
		return planeTableConditions;
	}
    
	/**
	 * 设置平面表查询条件
	 * @param planeTableConditions
	 */
	public void setPlaneTableConditions(Map<String, PlaneTableCondition> planeTableConditions) {
		this.planeTableConditions = planeTableConditions;
	}

	/**
	 * 获取固定报表任务列表
	 * @return
	 */
    public Map<String, RegularReportTaskInfo> getRegularTasks() {
        if (this.regularTasks == null) {
            this.regularTasks = Maps.newHashMap();
        }
        return regularTasks;
    }

    /**
     * 设置固定报表任务列表
     * @param regularTasks
     */
    public void setRegularTasks(Map<String, RegularReportTaskInfo> regularTasks) {
        this.regularTasks = regularTasks;
    }
    
    /**
     * 获取任务信息
     * @param taskId
     * @return
     */
    public RegularReportTaskInfo getRegularReportTaskInfo(String taskId) {
        if (this.regularTasks == null || this.regularTasks.size() == 0 || !this.regularTasks.containsKey(taskId)) {
            return null;
        }
        return this.regularTasks.get(taskId);
    }
}
