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
package com.baidu.rigel.biplatform.api.client.service.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.api.client.service.FileService;
import com.baidu.rigel.biplatform.api.client.service.FileServiceException;
import com.baidu.rigel.biplatform.ma.common.file.protocol.Command;
import com.baidu.rigel.biplatform.ma.common.file.protocol.Request;

/**
 * 客户端文件操作实现类
 * 
 * @author jiangyichao
 *
 */
@Service("fileService")
public class FileServiceImpl implements FileService {
    
    /**
     * 请求参数：target
     */
    public static final String TARGET = "target";
    
    /**
     * 请求参数：src
     */
    public static final String SRC = "src";
    
    /**
     * 响应状态：success
     */
    public static final String SUCCESS = "success";
    
    /**
     * 响应消息：msg
     */
    public static final String MSG = "msg";
    
    /**
     * 响应状态：fail
     */
    public static final String FAIL = "FAIL";
    
    /**
     * 响应状态key：result
     */
    public static final String RESULT = "result";

    /**
     * 参数名称：replace
     */
    public static final String REPLACE = "replace";
    
    /**
     * 参数名称：dir
     */
    public static final String DIR_KEY = "dir";
    
    /**
     * 请求参数不合法时的提示信息
     */
    private static final String PARAM_ILLEGAL_MESSAGE = "请求参数不合法";
    
    /**
     * 请求代理对象
     */
    @Resource
    private RequestProxy requestProxy = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean write(String filePath, byte[] content, boolean replace) throws FileServiceException {
        if (StringUtils.isBlank(filePath)) {
            throw new FileServiceException("file path is empty");
        }
        // 参数数组
        final String srcStr = "\\";
        final String destStr = "/";
        filePath = filePath.replace(srcStr, destStr);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(DIR_KEY, filePath);
        params.put(REPLACE, Boolean.valueOf(replace));
        params.put("data", content);
       
        Request request = new Request();
        request.setCommand(Command.WRITE);
        request.setParams(params);
        try {
            // 发送数据，并获得结果
            Map<String, Object> map = requestProxy.doActionOnRemoteFileSystem(request);
            // 结果正确，返回true
            if (map.get(RESULT).equals(SUCCESS)) {
                return true;
            }
            // 否则抛出异常信息
            throw new FileServiceException(map.get(MSG).toString());
        } catch (FileServiceException e) {
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean write(String filePath, byte[] content) throws FileServiceException {
        return this.write(filePath, content, true);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] read(String filePath) throws FileServiceException {
        
        if (StringUtils.isBlank(filePath)) {
            throw new FileServiceException("file path is empty");
        }
        filePath = filePath.replace("\\", "/");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(DIR_KEY, filePath);
        
        Request request = new Request();
        request.setCommand(Command.FILE_ATTRIBUTES);
        request.setParams(params);
        
        try {
            // 发送参数，并获得结果
            Map<String, Object> map = requestProxy.doActionOnRemoteFileSystem(request);
            // 返回结果失败，抛出异常信息
            if (map.get(RESULT).equals(FAIL)) {
                throw new FileServiceException(map.get(MSG).toString());
            }
            // filePath文件类型不是文件名，抛出异常
            if (!map.get("type").equals("file")) {
                throw new FileServiceException("请求路径不是文件名");
            }
//            // 文件大小，单位为k
//            double size = Double.parseDouble(map.get("size").toString()) / 1024;
//            // 客户端规定的文件大小，单位为k
//            double fileReadSize = ClientConfig.getFileReadSize();
//            // 请求文件大小大于规定文件大小
//            if (size > fileReadSize) {
//                throw new FileServiceException("请求文件大小>" + fileReadSize + "k,无法完成传输");
//            }
            // 满足所有文件条件，读取文件内容，并返回
            Request readRequest = new Request();
            readRequest.setCommand(Command.READ);
            readRequest.setParams(params);
            Map<String, Object> result = requestProxy.doActionOnRemoteFileSystem(readRequest);
            return (byte[]) result.get("data");
        } catch (FileServiceException e) {
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean rm(String filePath) throws FileServiceException {
        // 参数非空检查
        if (StringUtils.isEmpty(filePath)) {
            throw new FileServiceException(PARAM_ILLEGAL_MESSAGE);
        }
        filePath = filePath.replace("\\", "/");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(DIR_KEY, filePath);

        Request request = new Request();
        request.setCommand(Command.RM);
        request.setParams(params);
        
        // 参数数组
        try {
            // 发送命令，并获取结果
            Map<String, Object> map = requestProxy.doActionOnRemoteFileSystem(request);
            // 结果成功，返回true
            if (map.get(RESULT).equals(SUCCESS)) {
                return true;
            }
            // 结果失败，抛出异常信息
            throw new FileServiceException(map.get(MSG).toString());
        } catch (FileServiceException e) {
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean mkdir(String dir) throws FileServiceException {
        // 参数非空检查
        if (StringUtils.isEmpty(dir)) {
            throw new FileServiceException(PARAM_ILLEGAL_MESSAGE);
        }
        dir = dir.replace("\\", "/");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(DIR_KEY, dir);
        
        // 参数数组
        Request request = new Request();
        request.setCommand(Command.MKDIR);
        request.setParams(params);
        
        try {
            // 发送命令，并获得返回结果
            Map<String, Object> map = requestProxy.doActionOnRemoteFileSystem(request);
            // 结果成功，返回true
            if (map.get(RESULT).equals(SUCCESS)) {
                return true;
            }
            // 结果失败，返回异常信息
            throw new FileServiceException(map.get(MSG).toString());
        } catch (FileServiceException e) {
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean mkdirs(String dir) throws FileServiceException {
        // 参数非空检查
        if (StringUtils.isEmpty(dir)) {
            throw new FileServiceException(PARAM_ILLEGAL_MESSAGE);
        }
        dir = dir.replace("\\", "/");
       
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(DIR_KEY, dir);

        Request request = new Request();
        request.setCommand(Command.MKDIRS);
        request.setParams(params);
        
        try {
            // 发送参数数组，并获得返回结果
            Map<String, Object> map = requestProxy.doActionOnRemoteFileSystem(request);
            // 结果成功，返回true
            if (map.get(RESULT).equals(SUCCESS)) {
                return true;
            }
            // 结果失败，抛出异常信息
            throw new FileServiceException(map.get(MSG).toString());
        } catch (FileServiceException e) {
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean mv(String oldFilePath, String newFilePath, boolean replace) throws FileServiceException {
        // 参数非空检查
        if (StringUtils.isEmpty(oldFilePath) || StringUtils.isEmpty(newFilePath)) {
            throw new FileServiceException(PARAM_ILLEGAL_MESSAGE);
        }
        // 参数数组
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(SRC, oldFilePath);
        params.put(TARGET, newFilePath);
        params.put(REPLACE, Boolean.valueOf(replace));
        
        Request request = new Request();
        request.setCommand(Command.MV);
        request.setParams(params);
        try {
            // 发送参数数组，并获得返回结果
            Map<String, Object> map = requestProxy.doActionOnRemoteFileSystem(request);
            // 结果成功，返回true
            if (map.get(RESULT).equals(SUCCESS)) {
                return true;
            }
            // 结果失败，抛出异常信息
            throw new FileServiceException(map.get(MSG).toString());
        } catch (FileServiceException e) {
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean mv(String oldFilePath, String newFilePath) throws FileServiceException {
        return this.mv(oldFilePath, newFilePath, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean copy(String oldFilePath, String newFilePath, boolean replace) throws FileServiceException {
        // 参数非空检查
        if (StringUtils.isEmpty(oldFilePath) || StringUtils.isEmpty(newFilePath)) {
            throw new FileServiceException(PARAM_ILLEGAL_MESSAGE);
        }
        // 参数数组
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(SRC, oldFilePath);
        params.put(TARGET, newFilePath);
        params.put(REPLACE, Boolean.valueOf(replace));
        
        Request request = new Request();
        request.setCommand(Command.COPY);
        request.setParams(params);
        try {
            // 发送参数数组，并获得返回结果
            Map<String, Object> map = requestProxy.doActionOnRemoteFileSystem(request);
            // 结果成功，返回true
            if (map.get(RESULT).equals(SUCCESS)) {
                return true;
            }
            // 结果失败，抛出异常信息
            throw new FileServiceException(map.get(MSG).toString());
        } catch (FileServiceException e) {
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean copy(String oldFilePath, String newFilePath) throws FileServiceException {
        return this.copy(oldFilePath, newFilePath, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] ls(String dir) throws FileServiceException {
        // 参数非空检查
        if (StringUtils.isEmpty(dir)) {
            throw new FileServiceException(PARAM_ILLEGAL_MESSAGE);
        }
        dir = dir.replace("\\", "/");
        // 参数数组，用于获取文件信息
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(DIR_KEY, dir);
        
        Request request = new Request();
        request.setCommand(Command.LS);
        request.setParams(params);
        try {
            // 发送参数，获得文件属性
            Map<String, Object> map = requestProxy.doActionOnRemoteFileSystem(request);
            // 结果失败，返回信息
            if (map.get(RESULT) == null || map.get(RESULT).equals(FAIL)) {
                throw new FileServiceException(map.get(MSG).toString());
            }
            // 返回文件列表
            return  (String[]) map.get("fileList");
        } catch (FileServiceException e) {
            throw e;
        }
    }

    protected void setRequestProxy(RequestProxy requestProxy) {
        this.requestProxy = requestProxy;
    }
}
