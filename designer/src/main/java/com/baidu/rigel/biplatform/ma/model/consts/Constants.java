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
/**
 * 
 */
package com.baidu.rigel.biplatform.ma.model.consts;

/**
 * @author zhongyi
 *
 */
public interface Constants {
    
    /**
     * FILE_NAME_SEPERATOR
     */
    String FILE_NAME_SEPERATOR = "^_^";
    
    /**
     * BIPLATFORM_PRODUCTLINE
     */
    String BIPLATFORM_PRODUCTLINE = "biplatform_productline";
    
    /**
     * sessionID
     */
    String SESSION_ID = "identity";
    
    /**
     *  产品线和sessionId的cookie path
     */
    String COOKIE_PATH = "/";
    
    /**
     *  token
     */
    String TOKEN = "token";
    
    /**
     *  TODO jpass url传参数临时方案
     */
    String ORG_NAME = "orgname";
    
    /**
     *  appname
     */
    String APP_NAME = "appname";
    
    /**
     * 过滤空白行设置
     */
    String FILTER_BLANK = "filterBlank";
    
    /**
     * TOD
     */
    String TOP = "top";
    
    /**
     * RANDOMCODEKEY
     */
    String RANDOMCODEKEY = "RANDOMVALIDATECODEKEY";
    
    /**
     * NEED_LIMITED
     */
    String NEED_LIMITED = "needLimited";
    
    /**
     * 级次关系
     */
    String LEVEL = "LEVEL";
    
    /**
     * SOCKET_TIME_OUT_KEY
     */
    String SOCKET_TIME_OUT_KEY = "timeOut";

    /**
     * 判断是否由设计器发起请求
     */
    String IN_EDITOR = "isEdit";
    
    /**
     * 统计图当前选中索引
     */
    String CHART_SELECTED_MEASURE = "chartSelectedMeasureIndex";
    
    String LEVEL_KEY = "level_class";
    
    String COLOR_FORMAT = "colorFormat";

    String POSITION = "position";
    
    /**
     * 文本对齐样式定义
     */
    String ALIGN_FORMAT = "textFormat";
    
    /**
     * 默认文本对齐样式key 
     */
    String DEFAULT_ALIGN_FORMAT_KEY = "defaultTextAlign";
    
    /**
     * 默认文本对齐样式 
     */
    String DEFALUT_ALIGN_FORMAT = "left";
        
    
    /**
     * 是否能够动态切换指标
     */
    String CAN_CHANGED_MEASURE = "canChangedMeasure";
    
    /**
     * 平面表分页设置
     */
    String PAGINATION_SETTING = "pagination";
    
    /**
     * 设置表格中数据显示0还是-
     */
    String IS_SHOW_ZERO = "isShowZero";
}
