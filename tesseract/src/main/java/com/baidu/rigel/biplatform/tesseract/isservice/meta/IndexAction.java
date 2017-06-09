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
package com.baidu.rigel.biplatform.tesseract.isservice.meta;

/**
 * 
 * 索引动作
 * @author lijin
 *
 */
public enum IndexAction {
    /**
     * INDEX_INIT
     */
    INDEX_INIT("INDEX_INIT",Boolean.TRUE),
    /**
     * 建索引
     */
    INDEX_INDEX("INDEX_INDEX"),
    /**
     * INDEX_NORMAL
     */
    INDEX_NORMAL("INDEX_NORMAL"),
    /**
     * INDEX_MERGE_NORMAL
     */
    INDEX_MERGE_NORMAL("INDEX_MERGE_NORMAL",Boolean.TRUE),
    /**
     * INDEX_INIT_LIMITED
     */
    INDEX_INIT_LIMITED("INDEX_INIT_LIMITED",Boolean.TRUE),
    /**
     * INDEX_MERGE
     */
    INDEX_MERGE("INDEX_MERGE",Boolean.TRUE),
    /**
     * INDEX_UPDATE
     */
    INDEX_UPDATE("INDEX_UPDATE"),
    
    /**
     * INDEX_MOD
     */
    INDEX_MOD("INDEX_MOD",Boolean.TRUE); 
    
    /**
     * 动作名称
     */
    private String actionName; 
    /**
     * 是否需要从0开始
     */
    private Boolean fromScratch=Boolean.FALSE;
    
    /**
     * Constructor of IndexAction with 2 params
     * @param actionName actionName
     * @param fromScratch fromScratch
     */
    private IndexAction(String actionName,Boolean fromScratch){
    	this.actionName=actionName;
    	this.fromScratch=fromScratch;
    }
    
    /**
     * Constructor of IndexAction with 1 params
     * @param actionName actionName
     */
    private IndexAction(String actionName){
    	this.actionName=actionName;
    }

	/**
	 * @return the actionName
	 */
	public String getActionName() {
		return actionName;
	}

	/**
	 * @param actionName the actionName to set
	 */
	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	/**
	 * @return the fromScratch
	 */
	public Boolean getFromScratch() {
		return fromScratch;
	}

	/**
	 * @param fromScratch the fromScratch to set
	 */
	public void setFromScratch(Boolean fromScratch) {
		this.fromScratch = fromScratch;
	}
    
    
}
