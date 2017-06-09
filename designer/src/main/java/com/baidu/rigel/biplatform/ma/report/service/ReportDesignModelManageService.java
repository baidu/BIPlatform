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

import com.baidu.rigel.biplatform.ma.model.service.PositionType;
import com.baidu.rigel.biplatform.ma.report.exception.ReportModelOperationException;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;

/**
 * 
 * 报表定义模型管理服务： 报表定义模型的管理操作接口，包括扩展区的创建，扩展区的管理，扩展区的编辑(添加列、删除列，添加参数等)等管理工作。
 * 
 *
 * @author david.wang
 * @version 1.0.0.1
 */
public interface ReportDesignModelManageService {
    
    /**
     * 
     * @param ori
     *            原始信息
     * @param extendArea
     *            扩展区域信息
     * @return 修改后的报表模型信息
     * @throws ReportModelOperationException 
     */
    ReportDesignModel addExtendArea(ReportDesignModel ori, ExtendArea extendArea) throws ReportModelOperationException;
    
    /**
     * 
     * @param ori
     *            原始信息
     * @param extendArea
     *            要删除的扩展模型信息
     * @return 修改的模型信息
     * @throws ReportModelOperationException 
     */
    ReportDesignModel removeExtendArea(ReportDesignModel ori, String extendArea) throws ReportModelOperationException;
    
    /**
     * 添加或者修改指定区域指定位置上的条目
     * 
     * @param ori
     *            原始模型信息
     * @param areaId
     *            区域id
     * @param item
     *            条目信息
     * @param posittion
     *            位置类型信息
     * @return 修改后的模型信息
     * @throws ReportModelOperationException 
     */
    ReportDesignModel addOrUpdateItemIntoArea(ReportDesignModel ori, String areaId, Item item,
        PositionType posittion) throws ReportModelOperationException;
    
    /**
     * 从轴上删除制定条目
     * 
     * @param ori
     *            原始model
     * @param areaId
     *            扩展区标识
     * @param itemId
     *            条目id
     * @param position
     *            位置信息
     * @return 修改后的designModel
     * @throws ReportModelOperationException 
     */
    ReportDesignModel removeItem(ReportDesignModel ori, String areaId, String itemId,
        PositionType position) throws ReportModelOperationException;

    /**
     * 
     * @param model －－ 报表模型
     * @param areaId －－ 扩展区id
     * @param source －－ 要移动的item id
     * @param target －－ 目标位置之前的item id 如果是最前，为－1
     * @param type －－ 位置
     * @return 修改后的报表模型
     * @throws ReportModelOperationException
     */
    ReportDesignModel changeItemOrder(ReportDesignModel model, String areaId, String source,
            String target, PositionType type) throws ReportModelOperationException;
    
}
