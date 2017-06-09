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
package com.baidu.rigel.biplatform.ma.auth.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import com.baidu.rigel.biplatform.ma.auth.resource.RegisterController;

/**
 * 注册邮件配置类
 * @author jiangyichao
 *
 */
public final class RegisterMailConfig {
    
    /**
     * 发送方email
     */
    private static String SENDER_MAIL = "biplatform.ma.auth.register.mail.senderMail";
    
    /**
     * 接收方email
     */
    private static String ADMINISTRATOR = "biplatform.ma.auth.register.mail.administrator";
    
    /**
     * 注册邮件邮箱标题
     */
    private static String SUBJECT_FOR_REGISTER = "biplatform.ma.auth.register.mail.subjectForRegister";
    
    /**
     * 开通服务邮件标题
     */
    private static String SUBJECT_FOR_OPENSERVICE = "biplatform.ma.auth.register.mail.subjectForOpenService";
    
    /**
     * email服务器地址
     */
    private static String MAIL_SERVER_HOST = "biplatform.ma.auth.register.mail.mailServerHost";
    
    /**
     * 配置信息
     */
    private static Properties CONFIG = new Properties();
    
    /**
     * 静态初始化
     */
    static {
        loads();
    }
    
    /**
     * 构造函数
     */
    private RegisterMailConfig() {
    }

    /**
     * 加载静态资源
     */
    public static void loads() {
        InputStream is = null;
        try {
            is = RegisterController.class.getClassLoader()
                    .getResourceAsStream("application.properties");   
            CONFIG.load(new InputStreamReader(is, "utf-8"));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 获取发送方email
     * @return
     */
    public static String getSenderMail() {
        return CONFIG.getProperty(SENDER_MAIL);
    }
    
    /**
     * 获取管理员email
     * @return
     */
    public static String getAdministrator() {
        return CONFIG.getProperty(ADMINISTRATOR);
    }

    /**
     * 获取注册邮件主题
     * @return
     */
    public static String getSubjectForRegister() {
        return CONFIG.getProperty(SUBJECT_FOR_REGISTER);
    }
    
    /**
     * 获取开通服务邮件主题
     * @return
     */
    public static String getSubjectForOpenService() {
        return CONFIG.getProperty(SUBJECT_FOR_OPENSERVICE);
    }
    
    /**
     * 获取邮件服务器地址
     * @return
     */
    public static String getMailServerHost() {
        return CONFIG.getProperty(MAIL_SERVER_HOST);
    }
}
