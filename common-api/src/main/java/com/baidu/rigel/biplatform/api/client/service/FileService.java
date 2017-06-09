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
package com.baidu.rigel.biplatform.api.client.service;


/**
 * 文件操作服务接口，提供写文件，读文件，删除文件等操作
 * 
 * @author jiangyichao
 */
public interface FileService {
    
    /**
     * 向服务器写入文件
     * 
     * @param filePath
     *            路径
     * @param content
     *            写入内容
     * @param replace
     *            如果写入文件已经存在，是否进行覆盖
     * @return 返回操作结果
     * @throws FileServiceException
     */
    public boolean write(String filePath, byte[] content, boolean replace) throws FileServiceException;

    /**
     * 向服务器写入文件，设置默认编码方式，以及默认对已经存在文件进行覆盖
     * 
     * @param filePath
     *            文件路径
     * @param content
     *            写入内容
     * @return
     * @throws FileServiceException
     */
    public boolean write(String filePath, byte[] content) throws FileServiceException;

    /**
     * 从服务器读取文件
     * 
     * @param filePath
     *            文件完整路径名(路径+文件名)
     * @param code
     *            编码方式
     * @return 读取到的文件内容
     * @throws FileServiceException
     * @throws URIException
     */
    public byte[] read(String filePath) throws FileServiceException;

    /**
     * 删除服务器端文件
     * 
     * @param filePath
     *            文件完整路径名(路径+文件)
     * @return 操作结果
     * @throws FileServiceException
     *             文件操作异常
     */
    public boolean rm(String filePath) throws FileServiceException;

    /**
     * 创建此抽象路径名称指定的目录（及只能创建一级的目录，且需要存在父目录）
     * 
     * @param dir
     *            目录名
     * @return 操作结果
     * @throws FileServiceException
     *             文件操作异常
     */
    public boolean mkdir(String dir) throws FileServiceException;

    /**
     * 创建此抽象路径指定的目录，包括所有必须但不存在的父目录
     * 
     * @param dir
     *            目录名
     * @return 操作结果
     * @throws FileServiceException
     *             文件操作异常
     */
    public boolean mkdirs(String dir) throws FileServiceException;

    /**
     * 获取目录下的所有文件列表
     * 
     * @param dir
     *            目录名
     * @return 返回该目录下的所有文件列表数组
     * @throws FileServiceException
     *             文件操作异常
     */
    public String[] ls(String dir) throws FileServiceException;

    /**
     * 对服务器文件进行移动
     * 
     * @param oldFilePath
     *            原文件完整路径名
     * @param newFilePath
     *            新文件完整路径名
     * @param replace
     *            若新文件存在，是否进行替换
     * @return 操作结果
     * @throws FileServiceException
     *             文件操作异常
     */
    public boolean mv(String oldFilePath, String newFilePath, boolean replace) throws FileServiceException;

    /**
     * 对服务器文件进行移动,默认对已存在文件进行覆盖
     * 
     * @param oldFilePath
     *            原文件完整路径名
     * @param newFilePath
     *            新文件完整路径名
     * @return 操作结果
     * @throws FileServiceException
     *             文件操作异常
     */
    public boolean mv(String oldFilePath, String newFilePath) throws FileServiceException;

    /**
     * 对服务文件进行拷贝
     * 
     * @param oldFilePath
     *            原文件完整路径名
     * @param newFilePath
     *            新文件完整路径名
     * @param replace
     *            若新文件存在，是否进行替换
     * @return 操作结果
     * @throws FileServiceException
     *             文件操作异常
     */
    public boolean copy(String oldFilePath, String newFilePath, boolean replace) throws FileServiceException;

    /**
     * 对服务文件进行拷贝,默认对已存在文件进行覆盖
     * 
     * @param oldFilePath
     *            原文件完整路径名
     * @param newFilePath
     *            新文件完整路径名
     * @return 操作结果
     * @throws FileServiceException
     *             文件操作异常
     */
    public boolean copy(String oldFilePath, String newFilePath) throws FileServiceException;
}
