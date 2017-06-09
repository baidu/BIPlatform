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
package com.baidu.rigel.biplatform.ma.resource;


import java.io.File;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import com.baidu.rigel.biplatform.ma.report.utils.ContextManager;

/**
 * 
 * @author david.wang
 *
 */
@Resource(name = "ResourceRepository")
public class BaseResource {
    
    /**
     * 用户身份标识key
     */
    public static final String UID_KEY = "USER_ID";

    @Value("${biplatform.ma.ser_key}")
    protected String securityKey;
    
    /**
     * 根据产品线信息获取当前产品线加密密钥
     * @param productLine
     * @return String
     */
    protected String getSecurityKey(String productLine) {
        return null;
    }
    
    /**
     * 获取用户保存报表的路径
     * @param request
     * @return 依据用户标识获取的用户保存报表的路径
     */
    protected String getSavedReportPath(HttpServletRequest request) {
        String  path = "savedreport/preview";
        if (isNotPreview(request)) {
            path = "savereport/" + getUserIdendity(request);
        }
        return ContextManager.getProductLine () + File.separator + path;
    }

    /**
     * 从请求中获取当前访问报表用户用户标识
     * 
     * @param request
     * @return 用户标识
     */
    private String getUserIdendity(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies ();
        String uid = getUidFromCookies(cookies);
        if (StringUtils.isEmpty (uid)) {
            uid = request.getHeader (UID_KEY);
        }
        if (StringUtils.isEmpty (uid)) {
            uid = request.getParameter (UID_KEY);
        }
        if (StringUtils.isEmpty (uid)) {
            throw new RuntimeException("未提供正确的用户身份标识");
        }
        return uid;
    }

    private String getUidFromCookies(Cookie[] cookies) {
        if (cookies == null || cookies.length == 0) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (UID_KEY.equals (cookie.getName ())) {
                return cookie.getValue ();
            }
        }
        return null;
    }

    /**
     * 判断当前报表访问是否不是预览请求
     * @param request
     * @return 预览请求返回false，否则返回true
     */
    private boolean isNotPreview(HttpServletRequest request) {
        if (StringUtils.isNotBlank (request.getParameter ("reportPreview"))) {
            return false;
        }
        if (StringUtils.isNotBlank (request.getParameter ("isInDesigner"))) {
            return false;
        }
        String referer = request.getHeader ("referer");
        if (StringUtils.contains (referer, "reportPreview=true")) {
            return false;
        }
        return true;
    }
}
