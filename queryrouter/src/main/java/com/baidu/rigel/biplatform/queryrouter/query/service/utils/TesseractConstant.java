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
package com.baidu.rigel.biplatform.queryrouter.query.service.utils;


/**
 * Tesseract定义的公共常量
 * 
 * @author xiaoming.chen
 *
 */
public class TesseractConstant {
    
    /**
     * 小数点后保留8位
     */
    public static final int IND_SCALE = 8;
    
    /**
     * cube的事实表是分表的，则source是以MINI_CUBE_MULTI_FACTTABLE_SPLITTER分隔的字符串
     */
    public static final String MINI_CUBE_MULTI_FACTTABLE_SPLITTER = ",";
    
    /**
     * 字符串分隔符
     */
    public static final String STR_SPLITTER = "_";
    
    /**
     * 每次读取文件的大小
     */
    public static final int FILE_BLOCK_SIZE = 1024 * 512;
    
    /**
     * FEED_BACK_MSG_RESULT_SUCC
     */
    public static final String FEED_BACK_MSG_RESULT_SUCC = "SUCC";
    
    /**
     * FEED_BACK_MSG_RESULT_FAIL
     */
    public static final String FEED_BACK_MSG_RESULT_FAIL = "FAIL";
    
    /**
     * 消息反回格式
     */
    public static final String FEED_BACK_MSG_PATTERN_MSG_ALL = "%s:%s";
    
    /**
     * 事实表默认的id字段
     */
    public static final String FACTTABLE_ID_DEFAULT = "id";
    
    /**
     * DECOMPRESSION_FILENAME_SPLITTER
     */
    public static final String DECOMPRESSION_FILENAME_SPLITTER = "/";
    
    /**
     * 重试次数
     */
    public static final int RETRY_TIMES = 3;
    
    /**
     * callback 汇总节点key
     */
    public static final String SUMMARY_KEY = "SUMMARY";
    
    /**
     * 是否采用二八原则进行统计计算 目前只有统计图支持此操作 注意：请勿修改此参数 此处为临时方案
     */
    public static final String NEED_OTHERS = "NEED_OTHERS";
    
    /**
     * 注意：此处为临时方案，请勿擅自修改此参数，否则可能导致逻辑错误
     */
    public static final String NEED_OTHERS_VALUE = "1";
    
    
}
