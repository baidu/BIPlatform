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
package com.baidu.rigel.biplatform.ac.util;

import org.apache.commons.lang.StringUtils;

/**
 * REST工具类
 * 
 * @author zhongyi
 *
 * 
 */
public class ResponseResultUtils {

    /**
     * 构建返回结果
     * 
     * @param successMessage
     * @param errorMessage
     * @param data
     * @return
     */
    public static ResponseResult getResult(String successMessage,
            String errorMessage, Object data) {
        ResponseResult rs = new ResponseResult();
        if (data == null) {
            rs.setStatus(ResponseResult.FAILED);
            rs.setStatusInfo(errorMessage);
        } else {
            rs.setStatus(ResponseResult.SUCCESS);
            rs.setStatusInfo(successMessage);
            rs.setData(data);
        }
        return rs;
    }

    /**
     * 构建返回结果
     * 
     * @param errorMessage
     * @param errorCode
     * @return
     */
    public static ResponseResult getErrorResult(String errorMessage,
            int errorCode) {
        ResponseResult rs = new ResponseResult();
        rs.setStatus(errorCode);
        rs.setStatusInfo(errorMessage);
        return rs;
    }

    /**
     * 构建返回结果
     * 
     * @param successMessage
     * @param data
     * @return
     */
    public static ResponseResult getCorrectResult(String successMessage,
            Object data) {
        ResponseResult rs = new ResponseResult();
        rs.setStatus(ResponseResult.SUCCESS);
        // rs.setStatusInfo(successMessage);
        rs.setData(data);
        try {
            String errorMessage = (String) ThreadLocalPlaceholder
                    .getProperty(ThreadLocalPlaceholder.ERROR_MSG_KEY);
            if (StringUtils.isNotBlank(errorMessage)) {
                rs.setStatusInfo(errorMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ThreadLocalPlaceholder
                    .unbindProperty(ThreadLocalPlaceholder.ERROR_MSG_KEY);
        }
        return rs;
    }

    /**
     * 构建返回结果
     * 
     * @param successMessage
     * @param String
     *            data
     * @return
     */
    public static ResponseResult getCorrectResult(String successMessage,
            String data) {
        ResponseResult rs = new ResponseResult();
        rs.setStatus(ResponseResult.SUCCESS);
        rs.setData(data);
        try {
            String errorMessage = (String) ThreadLocalPlaceholder
                    .getProperty(ThreadLocalPlaceholder.ERROR_MSG_KEY);
            if (StringUtils.isNotBlank(errorMessage)) {
                rs.setStatusInfo(errorMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ThreadLocalPlaceholder
                    .unbindProperty(ThreadLocalPlaceholder.ERROR_MSG_KEY);
        }
        return rs;
    }
}