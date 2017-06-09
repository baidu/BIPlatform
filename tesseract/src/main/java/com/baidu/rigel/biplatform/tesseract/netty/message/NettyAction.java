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
package com.baidu.rigel.biplatform.tesseract.netty.message;

import java.io.Serializable;

/**
 * NettyAction Netty操作类型枚举
 * 
 * @author lijin
 *
 */
public enum NettyAction implements Serializable {
    
    /**
     * NETTY_ACTION_INDEX 建索引
     */
    NETTY_ACTION_INDEX("NETTY_ACTION_INDEX"),
    /**
     * NETTY_ACTION_INITINDEX 初始化索引
     */
    NETTY_ACTION_INITINDEX("NETTY_ACTION_INITINDEX"),
    /**
     * NETTY_ACTION_UPDATE 索引更新
     */
    NETTY_ACTION_UPDATE("NETTY_ACTION_UPDATE"),
    /**
     * NETTY_ACTION_MOD 索引修订
     */
    NETTY_ACTION_MOD("NETTY_ACTION_MOD"),
    /**
     * NETTY_ACTION_INDEX_FEEDBACK 建索引结果反馈
     */
    NETTY_ACTION_INDEX_FEEDBACK("NETTY_ACTION_INDEX_FEEDBACK"),
    /**
     * NETTY_ACTION_START_COPYINDEX 启动索引拷贝
     */
    NETTY_ACTION_START_COPYINDEX("NETTY_ACTION_START_COPYINDEX"),
    /**
     * NETTY_ACTION_RETURN_COPYINDEX_FEEDBACK 索引拷贝情况反馈
     */
    NETTY_ACTION_RETURN_COPYINDEX_FEEDBACK("NETTY_ACTION_RETURN_COPYINDEX_FEEDBACK"),
    /**
     * NETTY_ACTION_COPYFILE 文件拷贝
     */
    NETTY_ACTION_COPYFILE("NETTY_ACTION_COPYFILE"),
    /**
     * NETTY_ACTION_COPYFILE_FEEDBACK 文件拷贝结果反馈
     */
    NETTY_ACTION_SERVER_FEEDBACK("NETTY_ACTION_SERVER_FEEDBACK"),
    /**
     * NETTY_ACTION_SEARCH 查询
     */
    NETTY_ACTION_SEARCH("NETTY_ACTION_SEARCH"),
    /**
     * 用于client的feedback(client不会再返回信息)
     */
    NETTY_ACTION_NULL("NETTY_ACTION_NULL"),
    /**
     * NETTY_ACTION_SEARCH_FEEDBACK 查询结果反馈
     */
    NETTY_ACTION_SEARCH_FEEDBACK("NETTY_ACTION_SEARCH_FEEDBACK");
    
    /**
     * 动作名称
     */
    private String actionName;
    
    /**
     * @param actionName
     */
    private NettyAction(String actionName) {
        this.actionName = actionName;
    }
    
    /**
     * getter method for property actionName
     * 
     * @return the actionName
     */
    public String getActionName() {
        return actionName;
    }
    
    /**
     * setter method for property actionName
     * 
     * @param actionName
     *            the actionName to set
     */
    public void setActionName(String actionName) {
        this.actionName = actionName;
    }
    
}
