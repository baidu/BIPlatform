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
package com.baidu.rigel.biplatform.ma.file.serv.service.impl;

import static com.baidu.rigel.biplatform.ma.file.serv.util.LocalFileOperationUtils.FAIL;
import static com.baidu.rigel.biplatform.ma.file.serv.util.LocalFileOperationUtils.MSG;
import static com.baidu.rigel.biplatform.ma.file.serv.util.LocalFileOperationUtils.RESULT;
import static com.baidu.rigel.biplatform.ma.file.serv.util.LocalFileOperationUtils.SUCCESS;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.ma.file.serv.service.FileLocation;
import com.baidu.rigel.biplatform.ma.file.serv.service.LocalFileOperationService;
import com.baidu.rigel.biplatform.ma.file.serv.util.LocalFileOperationUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * 底层文件操作实现类
 * 
 * @author jiangyichao
 *
 */
public class LocalFileOperationServiceImpl implements LocalFileOperationService {
    
    /**
     * 日志对象
     */
    private static final Logger LOG = LoggerFactory.getLogger(LocalFileOperationServiceImpl.class);
    
    /**
     * 文件存储路径
     */
    private FileLocation location = null;
    
    public LocalFileOperationServiceImpl(FileLocation location) {
        this.location = location;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getFileAttributes(String filePath) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (StringUtils.isBlank(filePath)) {
            result.put(RESULT, FAIL);
            result.put(MSG, "文件名称为空");
            return result;
        }
        File file = new File(filePath);
        // 文件不存在
        if (!file.exists()) {
            result.put(RESULT, FAIL);
            result.put(MSG, "文件不存在");
            return result;
        }
        // 路径为目录
        if (file.isDirectory()) {
            result.put(RESULT, SUCCESS);
            result.put("type", "directory");
            return result;
        }
        FileInputStream inputStream = null;
        try {
            // 获取文件信息
            inputStream = new FileInputStream(file);
            result.put(RESULT, SUCCESS);
            result.put("type", "file");
            result.put("size", new Integer(inputStream.available()).toString());
            return result;
        } catch (IOException e) {
            result.put(RESULT, FAIL);
            result.put(MSG, "获取文件大小属性异常");
            return result;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> rm(String filePath) {
        Map<String, Object> result = new HashMap<String, Object>();
        
        if (StringUtils.isBlank(filePath)) {
            result.put(RESULT, FAIL);
            result.put(MSG, "文件路径为空");
            return result;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            result.put(RESULT, FAIL);
            result.put(MSG, "删除文件不存在");
            return result;
        }
        if (file.delete()) {
            result.put(RESULT, SUCCESS);
            result.put(MSG, "文件删除成功");
            return result;
        }
        result.put(RESULT, FAIL);
        result.put(MSG, "文件删除失败");
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> mkdir(String dir) {
        Map<String, Object> result = checkDir(dir);
        if (result.size() != 0) {
            return result;
        }
        File file = new File(dir);
        if (file.exists()) {
            result.put(RESULT, FAIL);
            result.put(MSG, "目录已经存在");
            return result;
        }
        if (file.mkdir()) {
            result.put(RESULT, SUCCESS);
            result.put(MSG, "创建目录成功");
            return result;
        }
        result.put(RESULT, FAIL);
        result.put(MSG, "创建目录失败");
        return result;
    }
    
    /**
     * 校验目录是否合法
     * 
     * @param dir
     * @return
     */
    private Map<String, Object> checkDir(String dir) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (StringUtils.isBlank(dir)) {
            result.put(RESULT, FAIL);
            result.put(MSG, "目标地址为空");
            return result;
        }
        
        if (".".equals(dir) || dir.contains("..") || dir.contains("/.")) {
            result.put(RESULT, FAIL);
            result.put(MSG, "目标地址不能为[.]或者[..]");
            return result;
        }
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> mkdirs(String dir) {
        Map<String, Object> result = this.checkDir(dir);
        if (result.size() != 0) {
            return result;
        }
        File file = new File(dir);
        if (file.mkdirs()) {
            result.put(RESULT, SUCCESS);
            result.put(MSG, "创建目录成功");
            return result;
        }
        result.put(RESULT, FAIL);
        result.put(MSG, "创建目录失败");
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> write(String filePath, byte[] content, boolean replace) {
        Map<String, Object> result = new HashMap<String, Object>();
        
        if (StringUtils.isBlank(filePath)) {
            result.put(RESULT, FAIL);
            result.put(MSG, "文件路径为null");
            return result;
        }
        
        if (content == null || content.length == 0) {
            result.put(RESULT, FAIL);
            result.put(MSG, "文件内容为空");
            return result;
        }
        
        File file = new File(filePath);
        // 新文件已经存在，不进行覆盖
        if (file.exists() && !replace) {
            result.put(RESULT, FAIL);
            result.put(MSG, "该文件已经存在");
            return result;
        }
        // 如果文件不存在，首先进行创建
        if (!file.exists()) {
            if (!LocalFileOperationUtils.createFile(filePath)) {
                result.put(RESULT, FAIL);
                result.put(MSG, "新文件创建失败");
                return result;
            }
        }
        // 写入文件内容
        if (!LocalFileOperationUtils.writeFile(file, content)) {
            result.put(RESULT, FAIL);
            result.put(MSG, "新文件写入失败");
            return result;
        }
        result.put(RESULT, SUCCESS);
        result.put(MSG, "新文件写入成功");
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> mv(String oldFilePath, String newFilePath, boolean replace) {
        // 调用移动/复制文件辅助方法
        return LocalFileOperationUtils.mv(oldFilePath, newFilePath, replace);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> copy(String oldFilePath, String newFilePath, boolean replace) {
        // 调用移动/复制文件辅助方法
        return LocalFileOperationUtils.copy(oldFilePath, newFilePath, replace);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> ls(String dir) {
        Map<String, Object> result = new HashMap<String, Object>();
        
        if (StringUtils.isBlank(dir)) {
            result.put(RESULT, FAIL);
            result.put(MSG, "请求路径为空");
            return result;
        }
        File file = new File(dir);
        if (!file.exists()) {
            result.put(RESULT, FAIL);
            result.put(MSG, "请求路径不存在");
            return result;
        }
        String[] fileList = null;
        if (file.isFile()) {
            fileList = new String[1];
            fileList[0] = dir;
        }
        fileList = file.list();
        if (fileList == null) {
            fileList = new String[0];
        }
        for (int i = 0; i < fileList.length; i++) {
            fileList[i] = fileList[i].replace(this.location.value(), "");
        }
        
        result.put(RESULT, SUCCESS);
        result.put(MSG, "请求文件列表成功");
        result.put("fileList", fileList);
        return result;
    }

    @Override
    public Map<String, Object> read(String dir) {
        Map<String, Object> result = Maps.newHashMap();
        String path = this.location.value() + "/" + dir;
        FileInputStream fi = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            fi = new FileInputStream(path);
            byte[] tmp = new byte[1024];
            int len = -1;
            while ((len = fi.read(tmp)) != -1) {
                bos.write(tmp, 0, len);
            }
        } catch (Exception e) {
            LOG.error("fail read file. ", e);
            result.put(RESULT, FAIL);
            result.put(MSG, "请求文件失败");
            return result;
        } finally {
            if (fi != null) {
                try {
                    fi.close();
                } catch (IOException e) {
                    LOG.info(e.getMessage(), e);
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    LOG.info(e.getMessage(), e);
                }
            }
        }
        result.put(RESULT, SUCCESS);
        result.put(MSG, "请求文件成功");
        result.put("data", bos.toByteArray());
        return result;
    }
}
