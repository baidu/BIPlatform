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
package com.baidu.rigel.biplatform.queryrouter.handle;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.baidu.rigel.biplatform.ac.util.PropertiesFileUtils;
import com.baidu.rigel.biplatform.ac.util.ResponseResult;
import com.google.gson.Gson;

/**
 * 配置文件刷新
 * 
 * @author luowenlei
 * 
 *         2015-11-03
 */
@RestController
@RequestMapping("/queryrouter/properties")
public class PropertiesFileResource {
    
    /**
     * logger
     */
    private Logger logger = LoggerFactory.getLogger(PropertiesFileResource.class);

    /**
     * 刷新配置文件
     * 
     * @param request
     * @return ResponseResult
     */
    @RequestMapping(value = "/refresh", method = { RequestMethod.GET })
    public ResponseResult refreshProperties(HttpServletRequest request) {
        PropertiesFileUtils.readPropertiesFile();
        ResponseResult responseResult = new ResponseResult();
        responseResult.setData("success");
        responseResult.setStatus(0);
        responseResult.setStatusInfo("success");
        logger.info("propertiesFile:{} refreshed properties file。",
                new Gson().toJson(PropertiesFileUtils.propertiesMap));
        return responseResult;
    }

}
