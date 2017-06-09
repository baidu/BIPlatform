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
package com.baidu.rigel.biplatform.ma.file.serv.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务器端文件操作辅助类
 * 
 * @author jiangyichao
 *
 */
public final class LocalFileOperationUtils {
    
    /**
     * RESULT
     */
    public static final String RESULT = "result";
    
    /**
     * FAILE
     */
    public static final String FAIL = "fail";
    
    /**
     * MSG
     */
    public static final String MSG = "msg";
    
    /**
     * SUCCESS
     */
    public static final String SUCCESS = "success";

    /**
     * 日志输出对象
     */
    private static final Logger LOG = LoggerFactory.getLogger (LocalFileOperationUtils.class);
    
    /**
     * LocalFileOperationUtils
     */
    LocalFileOperationUtils() {
        
    }
    /**
     * 在本地创建新文件
     * 
     * @param filePath
     *            需要创建的文件
     */
    public static boolean createFile(String filePath) {
        try {
            if (StringUtils.isBlank(filePath)) {
                return false;
            }
            File file = new File(filePath);
            // 创建文件未目录
            if (filePath.endsWith("/")) {
                return false;
            }
            int pos = filePath.lastIndexOf("/");
            // 路径包括文件名和文件夹名，先创建文件夹，之后创建文件
            String dir = filePath.substring(0, pos);
            File dirFile = new File(dir);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            return file.createNewFile();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 将content内容写入本地文件
     * 
     * @param file
     *            需要写入的文件
     * @param content
     *            写入文件内容
     * @param code
     *            编码方式
     */
    public static boolean writeFile(File file, byte[] content) {
        FileOutputStream fileOutputStream = null;
        try {
            if (file == null || content == null || content.length == 0) {
                return false;
            }
            
            fileOutputStream = new FileOutputStream(file);
            // 写入本地
            fileOutputStream.write(content);
            fileOutputStream.flush();
            return true;
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        return false;
    }

    /**
     * 从原文件读取内容，并写入新文件，在执行此方法前，已对原文件和目标文件是否存在进行了判断
     * 
     * @param oldFile
     *            原文件
     * @param newFile
     *            新文件
     * @return
     * @throws IOException
     */
    private static boolean copy(File oldFile, File newFile) {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            fileInputStream = new FileInputStream(oldFile);
            fileOutputStream = new FileOutputStream(newFile);
            byte[] buf = new byte[1024];
            int len = 0;
            // 读取原文件内容，然后写入新文件
            while ((len = fileInputStream.read(buf)) != -1) {
                fileOutputStream.write(buf, 0, len);
                fileOutputStream.flush();
            }
            return true;
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * 移动文件操作
     * @param oldFilePath
     * @param newFilePath
     * @param replace
     * @return
     */
    public static Map<String, Object> mv(String oldFilePath, String newFilePath, boolean replace) {
        File oldFile = new File(oldFilePath);
        File newFile = new File(newFilePath);
        Map<String, Object> result = preCheck(oldFile, newFile, replace);
        if (result != null) {
            return result;
        }
        return doAction(oldFile, newFile, true);
        
    }

    /**
     * 检查是否可以进行操作
     * @param newFilePath
     * @param replace
     * @param oldFile
     * @param newFile
     */
    private static Map<String, Object> preCheck(File oldFile, File newFile, boolean replace) {
        Map<String, Object>  result = new HashMap<String, Object>();
        // 原文件不存在
        if (!oldFile.exists()) {
            result.put(RESULT, FAIL);
            result.put(MSG, "原文件不存在");
            return result;
        }
        // 新文件已存在，不进行覆盖
        if (newFile.exists() && !replace) {
            result.put(RESULT, FAIL);
            result.put(MSG, "新文件已经存在");
            return result;
        }
        if (!newFile.exists()) {
            boolean rs = false;
            try {
                rs = newFile.createNewFile();
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
            // 新文件不存在，首先进行创建
            if (!rs) {
                result.put(RESULT, FAIL);
                result.put(MSG, "新文件创建失败");
                return result;
            }
        }
        return null;
    }
    
    /**
     * 复制文件辅助处理逻辑
     * 
     * @param oldFilePath
     *            旧路径
     * @param newFilePath
     *            新路径
     * @param replace
     *            是否对已存在内容进行覆盖
     * @return
     */
    public static Map<String, Object> copy(String oldFilePath, String newFilePath, boolean replace) {
        File oldFile = new File(oldFilePath);
        File newFile = new File(newFilePath);
        Map<String, Object> result = preCheck(oldFile, newFile, replace);
        if (result != null) {
            return result;
        }
        // 调用辅助处理逻辑
        return doAction(oldFile, newFile, false);
    }

    /**
     * 
     * 执行对文件的操作
     * @param oldFile
     *            旧文件
     * @param newFile
     *            新文件
     * @param delete
     *            是否将原文件删除
     * @return
     */
    private static Map<String, Object> doAction(File oldFile, File newFile, boolean delete) {
        Map<String, Object> result = new HashMap<String, Object>();
        // 读取原文件内容，然后写入新文件
        if (!copy(oldFile, newFile)) {
            result.put(RESULT, FAIL);
            result.put(MSG, "读取原文件内容，写入新文件失败");
            return result;
        }
        // 如果是移动命令，则将原文件删除；否则，如果为复制命令，保留原文件
        if (delete) {
            if (!oldFile.delete()) {
                result.put(RESULT, FAIL);
                result.put(MSG, "原文件删除失败");
                return result;
            }
        }
        result.put(RESULT, "success");
        result.put(MSG, "文件复制/移动成功");
        return result;
    }
}
