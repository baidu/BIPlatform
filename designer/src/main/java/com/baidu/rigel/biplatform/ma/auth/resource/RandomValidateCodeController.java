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
package com.baidu.rigel.biplatform.ma.auth.resource;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.baidu.rigel.biplatform.ma.model.consts.Constants;
import com.baidu.rigel.biplatform.ma.resource.BaseResource;
import com.baidu.rigel.biplatform.ma.resource.ResponseResult;
import com.baidu.rigel.biplatform.ma.resource.cache.CacheManagerForResource;

/**
 * 
 * Description:
 * @author david.wang
 *
 */
@RestController
public class RandomValidateCodeController extends BaseResource {

    /**
     * cacheManagerForResource
     */
    @Resource
    CacheManagerForResource cacheManagerForResource;
    
    /**
     * 
     * @param request
     * @param response
     */
    @RequestMapping(value = "/silkroad/auth", method = { RequestMethod.POST, RequestMethod.GET })
    public void genRandomValidateCode(HttpServletRequest request, HttpServletResponse response) {
        RandomValidateCode.getRandcode(request, response, cacheManagerForResource);
    }
    
    /**
     * 
     * @return ResponseResult
     */
    ResponseResult checkValidateCode(HttpServletRequest request) {
        String key = null;
        ResponseResult rs = new ResponseResult();
        if (request.getCookies() == null) {
            rs.setStatus(ResponseResult.FAILED);
            rs.setStatusInfo("请输入验证码");
            return rs;
        }
        for (Cookie tmp : request.getCookies()) {
            if (tmp.getName().equals(Constants.RANDOMCODEKEY)) {
                key = tmp.getValue();
                break;
            }
        }
        String valicateCode = request.getParameter("validateCode");
        if (StringUtils.isEmpty(valicateCode) || StringUtils.isEmpty(key)) {
            rs.setStatus(ResponseResult.FAILED);
            rs.setStatusInfo("请输入验证码");
        } else if (this.cacheManagerForResource.getFromCache(key) != null 
            && valicateCode.equalsIgnoreCase(cacheManagerForResource.getFromCache(key).toString())) {
            rs.setStatus(ResponseResult.SUCCESS);
            this.cacheManagerForResource.deleteFromCache(key);
        } else {
            rs.setStatusInfo("测试可以随便输入验证码");
        }
        return rs;
    }
    
}
