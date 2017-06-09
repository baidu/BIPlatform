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

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.api.client.service.FileService;
import com.baidu.rigel.biplatform.api.client.service.FileServiceException;
import com.baidu.rigel.biplatform.ma.auth.bo.ProductlineInfo;
import com.baidu.rigel.biplatform.ma.auth.service.ProductLineManageService;
import com.google.gson.Gson;

/**
 * 用户服务实现类
 * @author jiangyichao
 *
 */
@Service("userManageService")
public class ProductLineManageServiceImpl implements ProductLineManageService {

    /**
     * 日志对象
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProductLineManageServiceImpl.class);
    
    /**
     * 用户信息存储目录
     */
    private static final String USER_MSG_PATH = "user";
    
    /**
     * 存储用户信息时，用户名和密码之间的分隔符
     */
    private static final String NAME_PWD_SEPERATOR = "&";
    
    /**
     * 文件服务操作
     */
    @Resource 
    FileService fileService;
    
    /**
     * @{inheritDoc}
     */
    @Override
    public ProductlineInfo queryUser(String name, String pwd) {
        String userFileName = name + NAME_PWD_SEPERATOR + pwd;
        if (!this.existsFile(userFileName, true)) {
            return null;
        }
        String userMsgFilePath = USER_MSG_PATH + File.separator + userFileName;
        Gson gson = new Gson();
        try {
            byte[] userBytes = fileService.read(userMsgFilePath);
            ProductlineInfo user = gson.fromJson(new String(userBytes, "utf-8"), ProductlineInfo.class);
            return user;
        } catch (Exception e) {
            LOG.debug(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * @{inheritDoc}
     */
    @Override
    public boolean saveUser(ProductlineInfo user) {
        Gson gson = new Gson();
        String userJson = gson.toJson(user);
        String name = user.getName();
        String pwd = user.getPwd();
        String fileName = name + NAME_PWD_SEPERATOR + pwd;
        String userMsgFilePath = USER_MSG_PATH + File.separator + fileName;
        try {
            // 如果文件名不存在
            if (!this.existsUser(fileName)) {
                fileService.write(userMsgFilePath, userJson.getBytes(), true);
                return true;
            }
            LOG.warn(name + "is already exist");
        } catch (FileServiceException e) {
            LOG.info("user message save failed");
            LOG.debug(e.getMessage(), e);
        }
        return false;
    }
    
    /**
     * @{inheritDoc}
     */
    @Override
    public boolean existsUser(String name) {
        return this.existsFile(name, false);
    }
    
    /**
     * 查找某个用户信息是否已经存在
     * @param name 文件名
     * @param hasPwd 是否按照文件名+密码方式查询
     * @return
     */
    private boolean existsFile(String name, boolean hasPwd) {
        String[] fileList = null;
        try {
            fileList = fileService.ls(USER_MSG_PATH);
            if (fileList == null || fileList.length == 0) {
                return false;
            }
            for (String file : fileList) {
                String realName = file.split(NAME_PWD_SEPERATOR)[0];
                String userName = name.split(NAME_PWD_SEPERATOR)[0];
                if (hasPwd) {
                    realName = file;
                    userName = name;
                }
                if (realName.equals(userName)) {
                    return true;
                }
            }
        } catch (FileServiceException e) {
            LOG.debug(e.getMessage(), e);
        }
        return false;
    }
}
