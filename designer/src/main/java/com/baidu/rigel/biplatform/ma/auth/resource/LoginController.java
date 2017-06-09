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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.baidu.rigel.biplatform.ac.util.AesUtil;
import com.baidu.rigel.biplatform.ma.auth.bo.ProductlineInfo;
import com.baidu.rigel.biplatform.ma.auth.service.ProductLineManageService;
import com.baidu.rigel.biplatform.ma.model.consts.Constants;
import com.baidu.rigel.biplatform.ma.resource.ResponseResult;

/**
 * 
 * 登录控制服务 为测试环境提供，后续提供产品线管理功能
 * 
 * @author david.wang
 *
 */
@RestController
@RequestMapping("/silkroad/login")
public class LoginController extends RandomValidateCodeController {
    /**
     * 日志对象
     */
    private static final Logger LOG = LoggerFactory.getLogger(LoginController.class);
    
    /**
     * 用户服务对象
     */
    @Resource 
    ProductLineManageService userManageService;
    
    /**
     * 
     * 用户登录
     * 
     * @return 返回主页面
     */
    @RequestMapping(method = { RequestMethod.POST })
    @ResponseBody
    public ResponseResult login(HttpServletRequest request, HttpServletResponse response) {
        ResponseResult rs = checkValidateCode(request);
        if (rs.getStatus() == ResponseResult.FAILED) {
            return rs;
        }
        // 获取用户登录信息
        String productLine = request.getParameter("name");
        LOG.info("login user info : user = " + productLine);
        String pwd = request.getParameter("pwd");
        // modify by jiangyichao at 2014-09-12 加密产品线
        String productLineEncrypt = productLine;
        String pwdEncrypt = pwd;
        // 对产品线信息进行加密
        try {            
            // 加密产品线
            productLineEncrypt = AesUtil.getInstance().encryptAndUrlEncoding(productLine, securityKey);
            // 加密密码
            pwdEncrypt = AesUtil.getInstance().encryptAndUrlEncoding(pwd, securityKey);
        } catch (Exception e) {
            // 加密过程发生异常
            LOG.warn(e.getMessage(), e);
            rs.setStatus(1);
            rs.setStatusInfo("认证密钥错误 ");
            return rs;
        }
        // 使用未加密的用户名和加密后的密码查询用户
        ProductlineInfo user = userManageService.queryUser(productLine, pwdEncrypt);
        if (user == null) {
            rs.setStatus(1);
            rs.setStatusInfo("用户名或密码错误");
            return rs;
        }
        response.addHeader("Access-Control-Allow-Origin", "*");      
        // 在请求中添加产品线的cookie信息
        Cookie productLineCookie = new Cookie(Constants.BIPLATFORM_PRODUCTLINE, productLineEncrypt);
        productLineCookie.setPath(Constants.COOKIE_PATH);
        response.addCookie(productLineCookie);
        Cookie[] cookies = request.getCookies ();
        for (Cookie cookie  : cookies) {
            if ("prevReq".equals (cookie.getName ())) {
                rs.setData (cookie.getValue ());
                break;
            }
        }
        // 在请求中添加sessionId的cookie信息
        LOG.info("user [" + productLine + "] login bi-platform successfully");
        rs.setStatus(0);
        rs.setStatusInfo("successfully");
        return rs;
    }
}
