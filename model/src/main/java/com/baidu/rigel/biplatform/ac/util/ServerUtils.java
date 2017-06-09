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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.ac.exception.MiniCubeQueryException;

/**
 * 连接服务器工具类
 * 
 * @author 罗文磊
 *
 */
public class ServerUtils {
    
    /**
     * BIPLATFORM_QUERY_ROUTER_SERVER_TARGET_PARAM
     */
    public static String BIPLATFORM_DESIGNER_TARGET_PARAM = "biplatform_designer_target";
    
    /**
     * BIPLATFORM_PRODUCTLINE_PARAM
     */
    public static String BIPLATFORM_PRODUCTLINE_PARAM = "_rbk";
    
    /**
     * QUERYROUTER
     */
    public static String TARGET_TYPE = "QUERYROUTER";
    
    /**
     * logger
     */
    private static Logger logger = LoggerFactory.getLogger(ServerUtils.class);

    /**
     * setServerProperties
     *
     * @param json
     * @param productLine
     * @param params
     * @param headerParams
     * @return
     */
    public static void setServerProperties(String json, String productLine, Map<String, String> params,
            Map<String, String> headerParams) {
        String systemCode = ConfigInfoUtils.getServerSystemCode();
        String systemkey = ConfigInfoUtils.getServerSystemKey();
        if (systemCode == null || systemkey == null) {
            logger.error("properties conf at : \"server.queryrouter.systemcode\" "
                    + "or \"server.queryrouter.systemkey\"   is null");
            throw new MiniCubeQueryException(
                    "properties conf at : \"server.queryrouter.systemcode\" "
                            + "or \"server.queryrouter.systemkey\"   is null");
        }
        try {
            params.put("token", AesUtil.getInstance()
                    .encryptAndUrlEncoding(systemCode));
        } catch (Exception e) {
            logger.info("params token encrypt error, systemCode:" + systemCode);
            throw new MiniCubeQueryException(e.getMessage());
        }
        params.put("signature", Md5Util.encode(json, systemkey));
        headerParams.put(BIPLATFORM_DESIGNER_TARGET_PARAM, TARGET_TYPE);
        headerParams.put(BIPLATFORM_PRODUCTLINE_PARAM, productLine);
    }
}