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
package com.baidu.rigel.biplatform.queryrouter.query.service.utils;

/**
 * LogInfoConstants
 * 
 * @author lijin
 *
 */
public class LogInfoConstants {
    // Netty Handler
    /**
     * INFO_PATTERN_MESSAGE_RECEIVED_BEGIN ， %s: message received and begin
     * process
     */
    public static final String INFO_PATTERN_MESSAGE_RECEIVED_BEGIN = " %s: message received and begin process";
    /**
     * INFO_PATTERN_MESSAGE_RECEIVED_END，%s: message received and end process
     */
    public static final String INFO_PATTERN_MESSAGE_RECEIVED_END = " %s: message received and end process";
    
    // function
    /**
     * INFO_PATTERN_FUNCTION_BEGIN 方法内部-start， %s: [params:%s] start
     */
    public static final String INFO_PATTERN_FUNCTION_BEGIN = " %s: [params:%s] start ";
    /**
     * INFO_PATTERN_FUNCTION_PROCESS 方法内部-记录过程: %s: [params:%s]
     * process:[Action:%s]
     */
    public static final String INFO_PATTERN_FUNCTION_PROCESS = " %s: [params:%s] process:[Action:%s] ";
    /**
     * INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM 方法内部-记录过程，不记录参数: %s:
     * process:[Action:%s]
     */
    public static final String INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM = " %s: process:[Action:%s] ";
    /**
     * INFO_PATTERN_FUNCTION_END 方法内部-end， %s: [params:%s] end
     */
    public static final String INFO_PATTERN_FUNCTION_END = " %s: [params:%s] end ";
    
    /**
     * INFO_PATTERN_FUNCTION_EXCEPTION 方法内部-异常，%s: [params:%s] exception occured
     */
    public static final String INFO_PATTERN_FUNCTION_EXCEPTION = " %s: [params:%s] exception occured ";
    
    /**
     * INFO_PATTERN_FUNCTION_ERROR 方法内部-错误，%s: [params:%s] error occured
     */
    public static final String INFO_PATTERN_FUNCTION_ERROR = " %s: [params:%s] error occured ";
    
    // event & publish
    
    /**
     * INFO_PATTERN_PUBLISH_EVENT_SUCC 事件发布成功，%s: publish event success
     * [event:%s]
     */
    public static final String INFO_PATTERN_PUBLISH_EVENT_SUCC = " %s: publish event success [event:%s]";
    
    /**
     * INFO_PATTERN_PUBLISH_LOCALEVENT_SUCC 本地发布事件成功，%s: publish local event
     * success [event:%s]
     */
    public static final String INFO_PATTERN_PUBLISH_LOCALEVENT_SUCC = " %s: publish local event success [event:%s]";
    
    /**
     * INFO_PATTERN_PUBLISH_EVENT_FAIL 事件发布失败， %s: publish event fail
     */
    public static final String INFO_PATTERN_PUBLISH_EVENT_FAIL = " %s: publish event fail ";
    
    /**
     * INFO_PATTERN_GET_MULTIPOST_EVENT_SUCC 捕获组播事件成功， %s: receive
     * multi-broadcase
     */
    public static final String INFO_PATTERN_GET_MULTIBROAD_EVENT_SUCC = " %s: receive multi-broadcase "
            + "event success [event:%s]";
    
    /**
     * INFO_PATTERN_ON_LISTENER_BEGIN Listener处理开始，Listener %s: [event:%s]
     * process begin
     */
    public static final String INFO_PATTERN_ON_LISTENER_BEGIN = " Listener %s: [event:%s] process begin";
    
    /**
     * INFO_PATTERN_ON_LISTENER_END Listener处理结束，Listener %s: [event:%s] process
     * end
     */
    public static final String INFO_PATTERN_ON_LISTENER_END = " Listener %s: [event:%s] process end";
    
    // Compress & decompress
    /**
     * INFO_PATTERN_COMPRESS_PROCESS 压缩过程，Compressing Files: [Action:%s]
     */
    public static final String INFO_PATTERN_COMPRESS_PROCESS = "Compressing Files: [Action:%s]";
    
    /**
     * INFO_PATTERN_COMPRESS_ERROR 压缩过程出错，Error occured when compressing Files:
     * [Action:%s]
     */
    public static final String INFO_PATTERN_COMPRESS_ERROR = "Error occured when compressing Files: [Action:%s]";
    
    /**
     * INFO_PATTERN_DECOMPRESS_PROCESS 解压缩过程，Decompressing Files: [Action:%s]
     */
    public static final String INFO_PATTERN_DECOMPRESS_PROCESS = "Decompressing Files: [Action:%s]";
    
    /**
     * INFO_PATTERN_DECOMPRESS_ERROR 压缩过程出错，Error occured when decompressing
     * Files: [Action:%s]
     */
    public static final String INFO_PATTERN_DECOMPRESS_ERROR = "Error occured when decompressing Files: [Action:%s]";
    
    /**
     * INFO_PATTERN_FILEWRITE_PROCESS 写文件过程，"Writing Files: [Action:%s]
     */
    public static final String INFO_PATTERN_FILEWRITE_PROCESS = "Writing Files: [Action:%s]";
    
    /**
     * INFO_PATTERN_FILEWRITE_ERROR 写文件出错，Error occured when processing Files:
     * [Action:%s]
     */
    public static final String INFO_PATTERN_FILEPROCESS_ERROR = "Error occured when processing Files: [Action:%s]";
    
    /**
     * INFO_PATTERN_THREAD_RUN_START 后台线程run方法执行开始，Deamon Thread[%s] begin to
     * run
     */
    public static final String INFO_PATTERN_THREAD_RUN_START = "Deamon Thread[%s] begin to run ";
    
    /**
     * INFO_PATTERN_THREAD_RUN_EXCEPTION 后台线程run方法执行异常，Deamon Thread[%s] occured
     * Exception
     */
    public static final String INFO_PATTERN_THREAD_RUN_EXCEPTION = "Deamon Thread[%s] occured Exception";
    
    /**
     * INFO_PATTERN_THREAD_RUN_ACTION 后台线程run方法动作及结果，Action:[%s] State:[%s]
     */
    public static final String INFO_PATTERN_THREAD_RUN_ACTION = "Action:[%s] State:[%s]";
    
    /**
     * INFO_PATTERN_FILEREAD_PROCESS 读文件过程，Reading Files: [Action:%s]
     */
    public static final String INFO_PATTERN_FILEREAD_PROCESS = "Reading Files: [Action:%s]";
    
}
