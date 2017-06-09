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

/**
 * 
 * 扩展区域类型定义
 * 
 * @author david.wang
 * @version 1.0.0.1
 */
public enum ExtendAreaType {
    
    /**
     * 表格
     */
    TABLE("多维表格"),
    
    /**
     * 图表
     */
    CHART("图表"),
    
    /**
     * 平面表
     */
    PLANE_TABLE("平面表"),
    
    /**
     * LITEOLAP中的图
     */
    LITEOLAP_CHART("透视表图"),
    
    /**
     * LITEOLAP中的表
     */
    LITEOLAP_TABLE("透视表"),
    
    /**
     * 时间控件
     */
    TIME_COMP("时间控件"),
    
    /**
     * 查询控件
     */
    QUERY_COMP("查询控件"),
    
    /**
     * 功能按钮控件
     */
    FUNC_COMP("功能按钮控件"),
    
    /**
     * 待选区域控件
     */
    SELECTION_AREA("待选区域控件"),
    
    /**
     * 下拉列表
     */
    SELECT("下拉列表"),
    
    /**
     * 多选下拉列表
     */
    MULTISELECT("多选下拉列表"),
    
    /**
     * 文本框
     */
    TEXT("文本框"),
    
    /**
     * 
     */
    H_BUTTON("按钮"),
    
    /**
     * 单选下拉树
     */
    SINGLE_DROP_DOWN_TREE("单选下拉树"),
    
    /**
     * LiteOlap组件
     */
    LITEOLAP("LiteOlap组件"),
    
    /**
     * 报表保存保存
     */
    REPORT_SAVE_COMP("报表保存保存"),
    
    /**
     * 联动部件
     */
    CASCADE_SELECT("联动部件");
    
    /**
     * 中文名称
     */
    private String name;

    /**
     * getName
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    private ExtendAreaType(String name) {
        this.name = name;
    }
}
