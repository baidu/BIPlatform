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
package com.baidu.rigel.biplatform.ma.auth.service.impl;

import java.io.File;
import java.net.URLEncoder;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.util.AesUtil;
import com.baidu.rigel.biplatform.api.client.service.FileService;
import com.baidu.rigel.biplatform.api.client.service.FileServiceException;
import com.baidu.rigel.biplatform.ma.auth.bo.ProductlineInfo;
import com.baidu.rigel.biplatform.ma.auth.mail.SendMail;
import com.baidu.rigel.biplatform.ma.auth.service.ProductLineManageService;
import com.baidu.rigel.biplatform.ma.auth.service.ProductLineRegisterService;
import com.baidu.rigel.biplatform.ma.resource.BaseResource;

/**
 * 产品线注册服务实现
 * @author jiangyichao
 * 
 */
@Service("productLineRegisterService")
public class ProductLineRegisterServiceImpl extends BaseResource implements ProductLineRegisterService {
    
    /**
     * 发布目录
     */
    public static final String RELEASE_PATH = "release";
    
    /**
     * dev目录
     */
    public static final String DEV_PATH = "dev";
    
    /**
     * 编码方式
     */
    public static final String DEFAULT_CODE = "utf-8";
    
    /**
     * url参数连接符
     */
    public static final String URL_PARAM_SEPERATOR = "&";
    
    /**
     * 日志对象
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProductLineRegisterServiceImpl.class);
           
    /**
     * 文件服务对象
     */
    @Resource
    FileService fileService;   
    
    /**
     * 用户服务对象
     */
    @Resource
    ProductLineManageService userManageService;
    
    /**
     * 报表目录
     */
    @Value("${biplatform.ma.report.location}")
    private String report;
    
    /**
     * 数据源目录
     */
    @Value("${biplatform.ma.ds.location}")
    private String ds;
    
    /**
     * mailReceiver
     */
    @Value("${biplatform.ma.auth.register.mail.administrator}")
    private String mailReceiver;
    
    /**
     * mailSubject
     */
    @Value("${biplatform.ma.auth.register.mail.subjectForRegister}")
    private String mailSubject;
    
    /**
     * mailServer
     */
    @Value("${biplatform.ma.auth.register.mail.mailServerHost}")
    private String mailServer;
    
    /**
     * mailSender
     */
    @Value("${biplatform.ma.auth.register.mail.senderMail}")
    private String mailSender;
    
    /**
     * openServiceSubject
     */
    @Value("${biplatform.ma.auth.register.mail.subjectForOpenService}")
    private String openServiceSubject;
    
    @Value("${biplatform.ma.auth.register.mail.sender.password}")
    private String mailSenderPassowrd;
    
    /**
     * @{inheritDoc}
     */
    @Override
    public int sendRegisterMsgToAdministrator(ProductlineInfo user, String hostAddress, String magicStr) {
        SendMail sendMail = new SendMail();
        try {
            // 发送方
            sendMail.setFromAddress(mailSender);
            // 接收方，平台管理员
            sendMail.setToAddress(mailReceiver);
            // 邮件主题
            sendMail.setSubject(mailSubject + user.getDepartment());
            // 邮件服务器地址
            sendMail.setMailServerHost(mailServer);
            // 设置是否需要验证
            sendMail.setNeedAuth(false);
            // 设置发送方名字
            sendMail.setUserName(mailSender);
            // 设置发送邮件用户密码
            if (!StringUtils.isEmpty (mailSenderPassowrd)) {
                sendMail.setPassword (mailSenderPassowrd);
            }
            // 设置发送内容和格式
            sendMail.setBody(makeUpRegisterMailContent(user, hostAddress, magicStr), SendMail.HTML);
            // 发送
            sendMail.send();
            return 0;
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
        }
        return -1;
    }
    
    /**
     * 构建注册邮件内容
     * @param user
     * @param hostAddress 
     * @param magicStr 
     * @return
     */
    private String makeUpRegisterMailContent(ProductlineInfo user, String hostAddress, String magicStr) {
        StringBuilder stringBuilder = new StringBuilder();
        // 构建邮件html形式内容
        stringBuilder.append("<html><head><meta http-equiv=\"Content-Type\" "
             + "content=\"text/html; charset=utf-8\">");
        stringBuilder.append("<title>[productLine-register]</title></head><body>");
        // 添加表头信息
        stringBuilder.append("<table border=\"1\"");
        stringBuilder.append("cellspacing=\"0\" bordercolor=\"#2B2F2E\"><tr>");
        stringBuilder.append("<th width='200'>用户名</th>");
        stringBuilder.append("<th style=\"word-wrap : break-word ;\" width='250'>邮箱</th>");
        stringBuilder.append("<th width='200'>密码</th>");
        stringBuilder.append("<th width='200'>部门</th>");
        stringBuilder.append("<th width='200'>服务类型</th>");
        stringBuilder.append("<th width='300'>开通服务</th>");
        stringBuilder.append("</tr>");
        
        String serviceType = "线上";
        if (user.getServiceType() == 0) {
            serviceType = "线下";
        }
        // 添加具体用户信息
        stringBuilder.append("<tr>");
        stringBuilder.append("<td align=center rowspan = 2>" + user.getName() + "</td>");
        stringBuilder.append("<td align=center rowspan = 2>" + user.getEmail() + "</td>");
        String pwd = user.getPwd();
        // 对用户密码进行解密，以便发送给管理员查看
        try {
            pwd = AesUtil.getInstance().decodeAnddecrypt(pwd, securityKey);
        } catch (Exception e) {
            throw new IllegalArgumentException("decrypt password happened exception when send email to administrator");
        }
        stringBuilder.append("<td align=center rowspan = 2>" + pwd + "</td>");
        stringBuilder.append("<td align=center rowspan = 2>" + user.getDepartment() + "</td>");
        stringBuilder.append("<td align=center rowspan = 2>" + serviceType + "</td>");
        
        // 添加开通线上服务url
        stringBuilder.append("<td align=center><a href=" 
            + makeUpOpenServiceUrl(user, 1, hostAddress, magicStr) + ">线上服务</a></td>");
        stringBuilder.append("</tr>");
        
        stringBuilder.append("<tr>");     
        // 添加开通线下服务url
        stringBuilder.append("<td align=center><a href="
            + makeUpOpenServiceUrl(user, 0, hostAddress, magicStr) + ">线下服务</a></td>");
        stringBuilder.append("</tr>");
        
        stringBuilder.append("</table>");
        stringBuilder.append("</body></html>");
        return stringBuilder.toString();
    }
    
    /**
     * 构建开通服务url
     * @param user 用户对象
     * @param serviceType 服务类型，1代表线上服务，0代表线下服务
     * @param magicStr 
     * @param hsotAddress 服务器请求处理地址
     * @return url
     */
    private String makeUpOpenServiceUrl(ProductlineInfo user, int serviceType, String hostAddress, String magicStr) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(hostAddress);
        stringBuilder.append("/");
        if (serviceType == 1) {
            stringBuilder.append("/onlineService");
        } else {
            stringBuilder.append("/offlineService");
        }
        stringBuilder.append("?");
        try {
            stringBuilder.append("name=" + URLEncoder.encode(user.getName(), DEFAULT_CODE));
            stringBuilder.append(URL_PARAM_SEPERATOR);
            stringBuilder.append("pwd=" + URLEncoder.encode(user.getPwd(), DEFAULT_CODE));
            stringBuilder.append(URL_PARAM_SEPERATOR);
            stringBuilder.append("email=" + URLEncoder.encode(user.getEmail(), DEFAULT_CODE));
            stringBuilder.append(URL_PARAM_SEPERATOR);
            stringBuilder.append("department=");
            stringBuilder.append(URLEncoder.encode(user.getDepartment(), DEFAULT_CODE));
            stringBuilder.append(URL_PARAM_SEPERATOR);
            stringBuilder.append("serviceType=" 
                 + URLEncoder.encode(String.valueOf(user.getServiceType()), DEFAULT_CODE));   
            stringBuilder.append(URL_PARAM_SEPERATOR);
            stringBuilder.append("magicStr=" + magicStr);
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
        }
        return stringBuilder.toString();
    }

    /**
     * @{inheritDoc}
     */
    public int sendOpenServiceMsgToUser(ProductlineInfo user, int serviceType) {
        SendMail sendMail = new SendMail();
        try {
            // 发送方
            sendMail.setFromAddress(mailSender);
            // 接收方
            sendMail.setToAddress(user.getEmail());
            // 邮件主题
            sendMail.setSubject(openServiceSubject + user.getDepartment());
            // 邮件服务器地址
            sendMail.setMailServerHost(mailServer);
            // 设置是否需要验证
            sendMail.setNeedAuth(false);
            // 设置发送方名字
            sendMail.setUserName(mailSender);
            // 设置发送邮件用户密码
            if (!StringUtils.isEmpty (mailSenderPassowrd)) {
                sendMail.setPassword (mailSenderPassowrd);
            }
            // 设置发送内容和格式
            sendMail.setBody(makeUpOpenServiceMailContent(user, serviceType), SendMail.HTML);
            // 发送
            sendMail.send();
            return 0;
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
        }
        return -1;
    }
    
    /**
     * 构建开通服务邮件内容
     * @param user
     * @param serviceType 服务类型 1代表线上，0代表线下
     * @return
     */
    private String makeUpOpenServiceMailContent(ProductlineInfo user, int serviceType) {
        StringBuilder stringBuilder = new StringBuilder();
        // TODO 改成模板信息
        stringBuilder.append("<html><body>");
        stringBuilder.append("您的注册信息为:");
        stringBuilder.append("   用户名:" + user.getName());
        String serviceTypeStr = "线上服务";
        String pwd = user.getPwd();
        try {
            pwd = AesUtil.getInstance().decodeAnddecrypt(pwd, securityKey);
            stringBuilder.append("   密码:" + pwd);
            if (serviceType == 0) {
                serviceTypeStr = "线下服务";
            }
            stringBuilder.append("   服务类型:" + serviceTypeStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("decrypt password happened exception when send email to administrator");
        }
        stringBuilder.append("</body></html>");
        return stringBuilder.toString();
    }
    /**
     * @{inheritDoc}
     */
    @Override
    public int openOnlineService(ProductlineInfo user) {
       /**
        * 创建线上服务
        */
        return this.makeDirForOpenService(user, 1);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public int openOfflineService(ProductlineInfo user) {
        /**
         * 创建线下服务目录
         */
        return this.makeDirForOpenService(user, 0);
    }
    
    /**
     * 
     * @param user
     * @param serviceType 服务类型 1代表线上服务 ;0 代表线下服务
     * @return
     */
    private int makeDirForOpenService(ProductlineInfo user, int serviceType) {
        String name = user.getName();
        // TODO产品线线上线下根目录选择
        // String userRootDir = ONLINE_PATH + File.separator + name;
        String userRootDir = name;
        if (serviceType == 0) {
            userRootDir = name;
        }
        // 所有所需目录
        String reportDir = userRootDir + File.separator + report;
        String dsDir = userRootDir + File.separator + ds;
        String devDir = reportDir + File.separator + DEV_PATH;
        String releaseDir = reportDir + File.separator + RELEASE_PATH; 
        try {
            fileService.mkdir(userRootDir);
            fileService.mkdir(reportDir);
            fileService.mkdir(dsDir);
            fileService.mkdir(devDir);
            fileService.mkdir(releaseDir);
            // 保存用户信息
            userManageService.saveUser(user);
            return 0;
        } catch (FileServiceException e) {
            LOG.warn(e.getMessage(), e);
            try {
                // 如果用户信息已经存在，上述创建目录会发生异常，所以需重新进行创建
                fileService.mkdir(userRootDir);
                fileService.mkdir(reportDir);
                fileService.mkdir(dsDir);
                fileService.mkdir(devDir);
                fileService.mkdir(releaseDir);
            } catch (FileServiceException e1) {
                LOG.warn(e.getMessage(), e);
            }
        }
        return -1;
    }
}
