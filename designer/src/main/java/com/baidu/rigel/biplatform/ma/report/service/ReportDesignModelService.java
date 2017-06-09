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
package com.baidu.rigel.biplatform.ma.report.service;

import java.util.List;

import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceOperationException;
import com.baidu.rigel.biplatform.ma.report.exception.ReportModelOperationException;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;

/**
 * 
 * report design model manage service:
 * <p>
 *      design for {@link ReportDesignModel} model status. it provide API for query,
 *  modify, delete, check, publish and copy {@link ReportDesignModel} operations.
 * </p>
 * @see com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel
 * @since jdk1.8 or after
 * @version silkroad 1.0.1
 * @author david.wang
 *
 */
public interface ReportDesignModelService {
    
    /**
     * 获取ReportModel列表
     * @param released 
     * 
     * @return
     */
    ReportDesignModel[] queryAllModels(boolean released);
    
    /**
     * 依据id获取报表模版对象
     * 
     * @param id 报表id或者名称
     * @param isRelease 是否要获取发布的对象
     * @return 
     */
    ReportDesignModel getModelByIdOrName(String id, boolean isRelease);
    
    /**
     * 删除模版
     * 
     * @param id
     * @param removeFromDisk
     * @return
     * @throws ReportModelOperationException
     */
    boolean deleteModel(String id, boolean removeFromDisk) throws ReportModelOperationException;
    
    /**
     * 检测名称是否存在
     * 
     * @param name
     * @return
     */
    boolean isNameExist(String name);
    
    /**
     * 保存或者更新报表模型对象
     * 
     * @param model
     * @return
     * @throws ReportModelOperationException
     */
    ReportDesignModel saveOrUpdateModel(ReportDesignModel model)
            throws ReportModelOperationException;
    
    /**
     * 拷贝报表模型对象
     * 
     * @param src
     * @param targetName
     * @return
     * @throws ReportModelOperationException
     */
    ReportDesignModel copyModel(String src, String targetName) throws ReportModelOperationException;
    
    /**
     * 发布报表
     * @param model
     * @return
     * @throws ReportModelOperationException
     * @throws DataSourceOperationException 
     */
    boolean publishReport(ReportDesignModel model, String securityKey) 
                throws ReportModelOperationException, DataSourceOperationException;

    /**
     * 根据区域数据格式信息更新区域定义
     * @param area 区域定义
     * @param dataFormat 数据格式
     */
    void updateAreaWithDataFormat(ExtendArea area, String dataFormat);

    /**
     * 
     * @param area
     * @param toolTips
     */
    void updateAreaWithToolTips(ExtendArea area, String toolTips);

    /**
     * 
     * @param area
     * @param topSetting
     */
    void updateAreaWithTopSetting(ExtendArea area, String topSetting);

    /**
     * 
     * @param area
     * @param otherSetting
     */
    void updateAreaWithOtherSetting(ExtendArea area, String otherSetting);

    /**
     * 
     * @param id
     * @return List<String>
     */
    List<String> lsReportWithDsId(String id);

    /**
     * 更新报表名称
     * @param model
     * @param modelInCache
     */
    boolean updateReportModel(ReportDesignModel model, boolean modelInCache);

    /**
     * 
     * @param name
     * @param id
     * @return boolean
     */
    boolean isNameExist(String name, String id);

    /**
     * 
     * @param area
     * @param colorFormat
     */
    void updateAreaColorFormat(ExtendArea area, String colorFormat);

    /**
     * 
     * @param area
     * @param positions
     */
    void updateAreaPositionDef(ExtendArea area, String positions);
    
    /**
     * 更新区域文本对齐样式
     * @param area 区域
     * @param textAlignFormat 对齐样式
     */
    void updateAreaTextAlignFormat(ExtendArea area, String textAlignFormat);

    /**
     * 更新平面表区域的分页设置信息
     * @param area 区域
     * @param pageSettingStr 分页设置信息
     */
    void updatePageSetting4PlaneTable(ExtendArea area, String pageSettingStr);
}
