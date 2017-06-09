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
 * 索引状态
 * 
 * @author lijin
 *
 */
public enum IndexState {
    /**
     * 索引可用
     */
    INDEX_AVAILABLE,
    /**
     * 索引不可用
     */
    INDEX_UNAVAILABLE,
    /**
     * 未进行初始化
     */
    INDEX_UNINIT,
    /**
     * 索引可用，但需要合并
     */
    INDEX_AVAILABLE_NEEDMERGE,
    /**
     * 索引可用，索引完全重用
     */
    INDEX_AVAILABLE_MERGE
    
}
