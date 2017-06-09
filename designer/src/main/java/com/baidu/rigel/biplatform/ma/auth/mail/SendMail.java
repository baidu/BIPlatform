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

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 发送邮件类
 * 
 * @author jiangyichao
 */
public class SendMail {
    /**
     * text邮件类型text定义
     */
    public static final String TEXT = "text/plain;charset=GBK";
    /**
     * html格式
     */
    public static final String HTML = "text/html;charset=GBK";
    
    /**
     * 编码方式
     */
    public static final String DEFAULT_CODE = "utf-8";

    /**
     * 日志类
     */
    private static final Logger LOG = LoggerFactory.getLogger(SendMail.class);
    
    /**
     * 邮件服务器
     */
    private String mailServerHost;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 用户密码
     */
    private String password;
    /**
     * 发信人
     */
    private String fromAddress;
    /**
     * 收信人
     */
    private String toAddress;
    /**
     * Carbon Copy, 抄送邮件给某人
     */
//    private String copyTo;
    /**
     * bcc Blind Carbon Copy,隐蔽副本 隐蔽抄送给某人
     */
//    private String bc;
    /**
     * 邮件主题
     */
    private String subject;
    /**
     * 邮件内容
     */
    private BodyPart body;
    /**
     * 是否需要认证
     */
    private boolean needAuth;
    /**
     * 邮件附件
     */
//    private List<Object> attaches;
    
    

    /**
     * 构造方法
     * 
     */
    public SendMail() {
    }

//    /**
//     * 构造方法
//     * 
//     * @param mailServerHost
//     *            邮件服务器端口号
//     */
//    public SendMail(String mailServerHost) {
//        needAuth = true;
//        this.mailServerHost = mailServerHost;
//    }
//
//    /**
//     * 构造方法
//     * 
//     * @param mailServerHost
//     *            邮件服务器端口号
//     * @param userName
//     *            用户名
//     * @param password
//     *            用户密码
//     */
//    public SendMail(String mailServerHost, String userName, String password) {
//        needAuth = true;
//        this.mailServerHost = mailServerHost;
//        this.userName = userName;
//        this.password = password;
//    }

    /**
     * 获取mailServerHost
     * 
     * @return mailServerHost mailServerHost
     */
    public String getMailServerHost() {
        return mailServerHost;
    }

    /**
     * 设置mailServerHost
     * 
     * @param mailServerHost
     *            mailServerHost
     */
    public void setMailServerHost(String mailServerHost) {
        this.mailServerHost = mailServerHost;
    }

    /**
     * 获取userName
     * 
     * @return userName userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * 设置userName
     * 
     * @param userName
     *            userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * 获取password
     * 
     * @return password password
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置password
     * 
     * @param password
     *            password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取fromAddress
     * 
     * @return fromAddress fromAddress
     */
    public String getFromAddress() {
        return fromAddress;
    }

    /**
     * 设置fromAddress
     * 
     * @param fromAddress
     *            fromAddress
     */
    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    /**
     * 获取toAddress
     * 
     * @return toAddress toAddress
     */
    public String getToAddress() {
        return toAddress;
    }

    /**
     * 设置toAddress
     * 
     * @param toAddress
     *            toAddress
     */
    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

   

    

    /**
     * 获取body
     * 
     * @return body body
     */
    public BodyPart getBody() {
        return body;
    }

    /**
     * 设置body
     * 
     * @param body
     *            body
     */
    public void setBody(BodyPart body) {
        this.body = body;
    }

    /**
     * 设置邮件内容的形式
     * 
     * @param string
     *            handler参数
     * @param contentType
     *            handler参数
     */
    public void setBody(String string, String contentType) {
        try {
            body = new MimeBodyPart();
            DataHandler dh = new DataHandler(string, contentType);
            body.setDataHandler(dh);
        } catch (Exception exception) {
            LOG.warn("email setbody failed", exception);
        }
    }
    /**
     * 获取needAuth
     * 
     * @return needAuth needAuth
     */
    public boolean isNeedAuth() {
        return needAuth;
    }

    /**
     * 设置needAuth
     * 
     * @param needAuth
     *            needAuth
     */
    public void setNeedAuth(boolean needAuth) {
        this.needAuth = needAuth;
    }

    /**
     * 获取attaches
     * 
     * @return attaches attaches
     */
//    public List<Object> getAttaches() {
//        return attaches;
//    }
//
//    /**
//     * 设置attaches
//     * 
//     * @param attaches
//     *            attaches
//     */
//    public void setAttaches(List<Object> attaches) {
//        this.attaches = attaches;
//    }

    /**
     * 获取subject
     * 
     * @return subject subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * 主题设置
     * @param subject
     *        主题类型
     */
    public void setSubject(String subject) {
        String subjectTemp = "";
        try {
            subjectTemp = MimeUtility.encodeText(subject, DEFAULT_CODE, "B");
        } catch (UnsupportedEncodingException e1) {
            LOG.debug("wrong subject");
            return;
        }
        this.subject = subjectTemp;
    }
    /**
     * 设置邮件的内容的格式为文本格式
     * 
     * @param string
     *            body参数
     */
    public void setBodyAsText(String string) {
        setBody(string, TEXT);
    }

    /**
     * 以HTMl的形式存放内容
     * 
     * @param string
     *            HTML参数
     */
    public void setBodyAsHTML(String string) {
        setBody(string, HTML);
    }

    /**
     * 从文件中自动导入邮件内容
     * 
     * @param filename
     *            文件名称
     */
    public void setBodyFromFile(String filename) {
        try {
            BodyPart mdp = new MimeBodyPart();
            FileDataSource fds = new FileDataSource(filename);
            DataHandler dh = new DataHandler(fds);
            mdp.setDataHandler(dh);
        } catch (Exception exception) {
            LOG.warn("email setBodyFromFile failed", exception);
        }
    }

    /**
     * 从一个URL导入邮件的内容
     * 
     * @param url
     *            url
     */
    public void setBodyFromUrl(String url) {
        try {
            BodyPart mdp = new MimeBodyPart();
            URLDataSource ur = new URLDataSource(new URL(url));
            DataHandler dh = new DataHandler(ur);
            mdp.setDataHandler(dh);
        } catch (Exception exception) {
            LOG.warn("email setBodyFromUrl failed", exception);
        }
    }

    /**
     * 将String中的内容存放入文件showname，并将这个文件作为附件发送给收件人
     * 
     * @param string
     *            为邮件的内容
     * @param showname
     *            显示的文件的名字
     */
    public void addAttachFromString(String string, String showname) {
        try {
            BodyPart mdp = new MimeBodyPart();
            DataHandler dh = new DataHandler(string, TEXT);
            mdp.setFileName(MimeUtility.encodeWord(showname, DEFAULT_CODE, null));
            mdp.setDataHandler(dh);
        } catch (Exception exception) {
            LOG.warn("email addAttachFromString failed", exception);
        }
    }

    /**
     * filename为邮件附件 在收信人的地方以showname这个文件名来显示
     * 
     * @param filename
     *            文件名称
     * @param showname
     *            展示 名称
     */
    public void addAttachFromFile(String filename, String showname) {
        try {
            BodyPart mdp = new MimeBodyPart();
            FileDataSource fds = new FileDataSource(filename);
            DataHandler dh = new DataHandler(fds);
            mdp.setFileName(MimeUtility.encodeWord(showname, DEFAULT_CODE, null));
            mdp.setDataHandler(dh);
        } catch (Exception exception) {
            LOG.warn("email addAttachFromFile failed", exception);
        }
    }

    /**
     * 将互联网上的一个文件作为附件发送给收信人 并在收信人处显示文件的名字为showname
     * 
     * @param url
     *            url
     * @param showname
     *            展示名称
     */
    public void addAttachFromUrl(String url, String showname) {
        try {
            BodyPart mdp = new MimeBodyPart();
            URLDataSource ur = new URLDataSource(new URL(url));
            DataHandler dh = new DataHandler(ur);
            mdp.setFileName(MimeUtility.encodeWord(showname, DEFAULT_CODE, null));
            mdp.setDataHandler(dh);
        } catch (Exception exception) {
            LOG.warn("email addAttachFromUrl failed", exception);
        }
    }

    /**
     * 发送邮件,需要身份认证
     * 
     * @throws Exception
     *             抛出发送邮件异常
     */
    public void send() throws Exception {
        Properties props = new Properties();
        Session s = Session.getInstance(props, null);
        // 发送邮件服务器（SMTP），简单邮件传输协议
        Transport transport = s.getTransport("smtp");
        try {
            if (mailServerHost != null && !("").equals(mailServerHost.trim())) {
                props.setProperty("mail.smtp.mailServerHost", mailServerHost); // key
            } else {
                throw new Exception("没有指定发送邮件服务器");
            }
            if (needAuth) {
                props.setProperty("mail.smtp.auth", "true");
            }
            MimeMessage msg = new MimeMessage(s);
            // 设置邮件主题
            msg.setSubject(subject);

            // 设置邮件发送时间
            msg.setSentDate(new Date());
            // 指定发件人
            if (fromAddress != null) {
                msg.addFrom(InternetAddress.parse(fromAddress));
            } else {
                throw new Exception("没有指定发件人");
            }
            // 指定收件人
            if (toAddress != null) {
                msg.addRecipients(javax.mail.Message.RecipientType.TO,
                        InternetAddress.parse(toAddress));
            } else {
                throw new Exception("没有指定收件人地址");
            }
            
//            // 指定抄送
//            if (copyTo != null) {
//                msg.addRecipients(javax.mail.Message.RecipientType.CC,
//                        InternetAddress.parse(copyTo));
//            }
//            // 指定密送
//            if (bc != null) {
//                msg.addRecipients(javax.mail.Message.RecipientType.BCC,
//                        InternetAddress.parse(bc));
//            }
            Multipart mm = new MimeMultipart();
            // 设置邮件的附件
            if (body != null) {
                mm.addBodyPart(body);
            }
//            for (int i = 0; i < attaches.size(); i++) {
//                BodyPart part = (BodyPart) attaches.get(i);
//                mm.addBodyPart(part);
//            }
            // 设置邮件的内容
            msg.setContent(mm);
            // 保存所有改变
            msg.saveChanges();
            // 访问邮件服务器
            transport.connect(mailServerHost, userName, password);
            // 发送信息
            transport.sendMessage(msg, msg.getAllRecipients());
        } catch (Exception e) {
            LOG.debug("发送邮件失败");
            throw new Exception("发送邮件失败:", e);
        } finally {
            // 关闭邮件传输
            transport.close(); 
        }
    }
}
