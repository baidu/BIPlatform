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
package com.baidu.rigel.biplatform.ma.file.serv.service;

import java.util.Map;

/**
 * 服务器端文件操作执行接口
 * 
 * @author jiangyichao
 *
 */
public interface LocalFileOperationService {
    
    /**
     * 返回文件信息
     * 
     * @param filePath
     *            路径
     * @return 文件属性信息
     */
    public Map<String, Object> getFileAttributes(String filePath);

    /**
     * 删除本地文件
     * 
     * @param filePath
     *            文件路径
     * @return 操作结果
     * @throws FileServerServiceException
     */
    public Map<String, Object> rm(String filePath);

    /**
     * mkdir创建目录操作(父目录必须存在)
     * 
     * @param dir
     *            文件路径
     * @return 操作结果
     * @throws FileServerServiceException
     */
    public Map<String, Object> mkdir(String dir);

    /**
     * mkdirs创建目录(父目录允许不存在)
     * 
     * @param dir
     *            文件路径
     * @return 操作结果
     * @throws FileServerServiceException
     */
    public Map<String, Object> mkdirs(String dir);

    /**
     * 写入本地文件
     * 
     * @param filePath
     *            路径
     * @param content
     *            文件内容
     * @param code
     *            编码方式
     * @param replace
     *            如果文件已存在，是否进行覆盖
     * @return
     */
    public Map<String, Object> write(String filePath, byte[] content, boolean replace);

    /**
     * mv移动文件操作
     * 
     * @param oldFilePath
     *            旧文件路径
     * @param newFilePath
     *            新文件路径
     * @param replace
     *            是否将新文件覆盖
     * @throws FileServerServiceException
     */
    public Map<String, Object> mv(String oldFilePath, String newFilePath, boolean replace);

    /**
     * 复制文件操作
     * 
     * @param oldFilePath
     *            旧文件路径
     * @param newFilePath
     *            新文件路径
     * @param replace
     *            是否将新文件覆盖
     * @throws FileServerServiceException
     */
    public Map<String, Object> copy(String oldFilePath, String newFilePath, boolean replace);

    /**
     * 获取文件夹列表
     * 
     * @param dir
     *            具体路径
     * @return
     */
    public Map<String, Object> ls(String dir);
    
    /**
     * 获取文件夹列表
     * 
     * @param dir
     *            具体路径
     * @return
     */
    public Map<String, Object> read(String dir);
}
