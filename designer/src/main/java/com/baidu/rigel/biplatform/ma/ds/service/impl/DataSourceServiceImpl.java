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
package com.baidu.rigel.biplatform.ma.ds.service.impl;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import com.baidu.rigel.biplatform.ac.util.AesUtil;
import com.baidu.rigel.biplatform.ac.util.PropertiesFileUtils;
import com.baidu.rigel.biplatform.api.client.service.FileService;
import com.baidu.rigel.biplatform.api.client.service.FileServiceException;
import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceOperationException;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceConnectionService;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceConnectionServiceFactory;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceGroupService;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceService;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceGroupDefine;
import com.baidu.rigel.biplatform.ma.report.utils.ContextManager;

/**
 * 
 * 数据源服务实现类
 * 
 * @author david.wang
 *
 */
@Service("dsService")
public class DataSourceServiceImpl implements DataSourceService {
    
    /**
     * logger 
     */
    private Logger logger = LoggerFactory.getLogger(DataSourceService.class);
    
    /**
     * 文件服务接口
     */
    @Resource(name = "fileService")
    private FileService fileService;
    
    @Value("${biplatform.ma.ds.location}")
    private String dsFileBaseDir;
    
    @Resource
    private DataSourceGroupService dataSourceGroupService;
    
    /**
     * 构造函数
     * 
     * @param productLine
     */
    public DataSourceServiceImpl() {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized DataSourceDefine saveOrUpdateDataSource(DataSourceDefine ds, String securityKey)
            throws DataSourceOperationException {
        
        checkDataSourceDefine(ds, securityKey);
        try {
            // 如果修改了数据源的名称，则先写新的数据源，然后删除原来的数据源文件
            DataSourceDefine oldDs = getDsDefine(ds.getId());
            String oldDsFileName = null;
            if (oldDs != null && !oldDs.getName().equals(ds.getName())) { // 修改了数据源名称
                oldDsFileName = getDsFileName(oldDs);
                if (this.isNameExist(ds.getName())) {
                    throw new DataSourceOperationException("name already exist : " + ds.getName());
                }
            }
            String fileName = getDsFileName(ds);
            boolean rmOperResult = false;
            if (oldDsFileName != null) { // 此处操作意味用户做了修改数据源名称操作
                rmOperResult = fileService.rm(oldDsFileName);
            }
            if (oldDsFileName == null || rmOperResult) { // 删除操作成功
                fileService.write(fileName, SerializationUtils.serialize(ds));
            }
        } catch (Exception e) {
            // 如果发生异常 考虑回滚或者其他容错操作
            logger.error(e.getMessage(), e);
            throw new DataSourceOperationException("Error Happend for save or update datasource :"
                    + e);
        }
        return ds;
    }
    
    /**
     * 校验数据源合法性 不合法抛出异常
     * 
     * @param ds
     * @throws DataSourceOperationException
     */
    private void checkDataSourceDefine(DataSourceDefine ds, String securityKey) throws DataSourceOperationException {
        if (ds == null) {
            logger.error("datasource can not be null");
            throw new DataSourceOperationException("datasource can not be null");
        }
        if (StringUtils.isBlank(ds.getProductLine())) {
            logger.error("product line can not be null");
            throw new DataSourceOperationException("product line can not be null");
        }
        // 名称一样，id不同认为是新加重名数据源，如果名称，id均相同，则认为是修改数据源
        if (isNameExist(ds.getName()) && !isNameExist(ds.getId())) {
            logger.debug("ds name alread exist");
            throw new DataSourceOperationException("ds name alread exist");
        }
        /*
         * modified by jiangyichao 验证数据库连接字符串有效性 2014-08-12
         */
        if (!isValidateConn(ds, securityKey)) {
            logger.debug("db connection info not correct");
            throw new DataSourceOperationException("db connection info not correct");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNameExist(String name) throws DataSourceOperationException {
        
        String dir = getDsFileStoreDir(ContextManager.getProductLine());
        try {
            String[] fileList = fileService.ls(dir);
            if (fileList == null || fileList.length == 0) {
                return false;
            }
            for (String fileName : fileList) {
                if (fileName.contains(name)) {
                    return true;
                }
            }
            return false;
        } catch (FileServiceException e) {
            logger.debug(e.getMessage(), e);
            throw new DataSourceOperationException(e);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidateConn(DataSourceDefine ds, String securityKey) {
    	DataSourceConnectionService<?> dsConnService = null;
        try {
        	// 获取数据源连接，如果不抛出异常，说明连接字符串有效，返回true；
            dsConnService = DataSourceConnectionServiceFactory.
            		getDataSourceConnectionServiceInstance(ds.getDataSourceType().name ());
            boolean result = dsConnService.isValidateDataSource(ds, securityKey);
            if (result) {
            	return true;
            }           
            // 如果第一次验证失败，需对密码进行加密后重新验证
            String pwd = AesUtil.getInstance().encryptAndUrlEncoding(ds.getDbPwd(), securityKey);                
            // dirty solution 兼容原有数据源定义 
            // 对密码进行加密
            ds.setDbPwd(pwd);
            return  dsConnService.isValidateDataSource(ds, securityKey);             	
        } catch (Exception e) {
			logger.error(e.getMessage());
			throw new RuntimeException(e);
        } 
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DataSourceDefine[] listAll() throws DataSourceOperationException {
        String[] listFile = null;
        try {
            listFile = fileService.ls(getDsFileStoreDir(ContextManager.getProductLine()));
        } catch (FileServiceException e) {
            logger.error(e.getMessage(), e);
            throw new DataSourceOperationException(e);
        }
        if (listFile == null || listFile.length == 0) {
            return new DataSourceDefine[0];
        }
        final List<DataSourceDefine> rs = buildResult(listFile);
        if (rs.size() != listFile.length) {
            return new DataSourceDefine[0];
        }
        return rs.toArray(new DataSourceDefine[0]);
    }
    
    /**
     * 
     * 将文件列表转换为数据源定义
     * 
     * @param productLine
     * @param listFile
     * @return
     */
    private List<DataSourceDefine> buildResult(final String[] listFile) {
        final List<DataSourceDefine> rs = new ArrayList<DataSourceDefine>();
        for (final String f : listFile) {
            try {
                DataSourceDefine ds = buildResult(f);
                rs.add(ds);
            } catch (FileServiceException e) {
                logger.debug(e.getMessage(), e);
            }
        }
        logger.info("read file successfully");
        return rs;
    }
    
    /**
     * 
     * 将文件列表转换为数据源定义
     * 
     * @param productLine
     * @param listFile
     * @return
     * @throws FileManageOperationException
     * @throws UnsupportedEncodingException
     */
    private DataSourceDefine buildResult(String file) throws FileServiceException {
        
        byte[] content = (byte[]) fileService.read(genDsFilePath(file));
        DataSourceDefine ds = null;
        try {
        	ds = (DataSourceDefine) SerializationUtils.deserialize(content);
        } catch(ClassCastException e) {
        	// 数据源组
        	DataSourceGroupDefine dsG = (DataSourceGroupDefine) SerializationUtils.deserialize(content);
        	ds = dsG.getActiveDataSource();
        }
        return ds;
    }
    
    /**
     * @param file
     * @return
     */
    private String genDsFilePath(String file) {
        return getDsFileStoreDir(ContextManager.getProductLine()) + File.separator + file;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DataSourceDefine getDsDefine(String id) throws DataSourceOperationException {
        String fileName = getDatasourceDefineNameByIdOrName(ContextManager.getProductLine(), id);
        if (fileName == null) {
            return null;
        }
        try {
            return buildResult(fileName);
        } catch (FileServiceException e) {
            logger.error("error : " + e.getMessage());
            throw new DataSourceOperationException("未找到正确的数据源定义信息", e);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DataSourceDefine getDsDefine(String queryDsId,
            @SuppressWarnings("rawtypes") Map requestParams) throws DataSourceOperationException {
        PropertiesFileUtils.readPropertiesFile();
        if (MapUtils.isNotEmpty(requestParams)
                && requestParams.get("activeds") != null) {
            // activeds 的格式为：  activeds=datasourceGroupA.datasourceA datasourceGroupA为数据源组名称，datasourceA数据源名称
            String productLine = ContextManager.getProductLine();
            try {
                String dsg = URLDecoder.decode(requestParams.get("activeds").toString(), "UTF-8");
                String[] requestDsgNames = StringUtils.split(dsg, ".");
                if (requestDsgNames.length == 2 
                        && StringUtils.isNotEmpty(requestDsgNames[0])
                        && StringUtils.isNotEmpty(requestDsgNames[1])) {
                    DataSourceGroupDefine dataSourceGroupDefine = dataSourceGroupService
                            .getDataSourceGroupDefine(productLine, requestDsgNames[0]);
                    if (dataSourceGroupDefine != null) {
                        String propertiesKey = productLine + "." + requestDsgNames[0];
                        String propertiesDs = PropertiesFileUtils.getPropertiesKey("activeds", propertiesKey);
                        logger.info("activeds properties:{}.",
                                PropertiesFileUtils.getPropertiesKey("activeds", propertiesKey));
                        if (StringUtils.isEmpty(propertiesDs)) {
                            logger.info("can not found required propertiesKey:{} in properties:{}, System will"
                                    + "select the default conf.", propertiesKey,
                                    PropertiesFileUtils.getPropertiesKey("activeds", propertiesKey));
                            return this.getDsDefine(queryDsId);
                        }
                        String[] dsNames = StringUtils.split(propertiesDs, ",");
                        logger.info("properties:{}.",
                                PropertiesFileUtils.getPropertiesKey("activeds", propertiesKey));
                        for (String key : dsNames){
                            for (DataSourceDefine dataSourceDefine : dataSourceGroupDefine
                                    .getDataSourceList().values()) {
                                if (requestDsgNames[1].equals(dataSourceDefine.getName())
                                        && key.equals(requestDsgNames[1])) {
                                    // 如果找到此数据源那么使用此数据源key
                                    logger.info("url datasource activeds:{}, change using datasourceGroup:{} datasource：{}.",
                                            requestParams.get("activeds").toString(), requestDsgNames[0], requestDsgNames[1]);
                                    return dataSourceDefine;
                                }
                            }
                        };
                    }
                }
                logger.warn("requested url datasource activeds:{}, not changed.pls check requestParam:‘activeds’",
                            requestParams.get("activeds").toString());
                
            } catch (UnsupportedEncodingException e) {
                logger.error("url parameters 'activeds' is not well formed. activeds:{}, system continue to choose default ds.",
                        requestParams.get("activeds").toString());
                e.printStackTrace();
            }
        }
        return this.getDsDefine(queryDsId);
    }
    
    /**
     * 依据数据源id或者name获取数据源定义
     * 
     * @param idOrName
     * @return
     */
    private String getDatasourceDefineNameByIdOrName(String productLine, String idOrName) {
        String dir = getDsFileStoreDir(productLine);
        String[] ds = null;
        try {
            ds = fileService.ls(dir);
        } catch (FileServiceException e1) {
            logger.debug(e1.getMessage(), e1);
        }
        if (ds == null || ds.length == 0) {
            String msg = "can not get ds define by id : " + idOrName;
            logger.error(msg);
            return null;
        }
        Set<String> tmp = new HashSet<String>();
        Collections.addAll(tmp, ds);
        Set<String> dict = new HashSet<String>();
        tmp.stream().forEach((String s) -> {
            dict.add(s.substring(0, s.indexOf("_")));
            dict.add(s.substring(s.indexOf("_") + 1));
        });
        if (!dict.contains(idOrName)) {
            return null;
        }
        
        String fileName = null;
        for (String dsFileName : ds) {
            if (dsFileName.contains(idOrName)) {
                fileName = dsFileName;
                break;
            }
        }
        return fileName;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeDataSource(String id) throws DataSourceOperationException {
        DataSourceDefine ds = getDsDefine(id);
        if (ds == null) {
            String msg = "cant't get ds define info by id : " + id;
            logger.error(msg);
            throw new DataSourceOperationException("不能通过指定数据源id找到数据源定义： id ＝ " + id);
        }
        try {
            return fileService.rm(genDsFilePath(
                getDatasourceDefineNameByIdOrName(ContextManager.getProductLine(), id)));
        } catch (FileServiceException e) {
            logger.error(e.getMessage(), e);
            throw new DataSourceOperationException(e);
        }
    }
    
    /**
     * 获取数据源文件的文件名（含路径）
     * 
     * @param ds
     * @return 返回数据源定义文件文件名（绝对路径）
     */
    private String getDsFileName(DataSourceDefine ds) {
        return getDsFileStoreDir(ContextManager.getProductLine()) + File.separator + ds.getId() + "_" + ds.getName();
    }
    
    /**
     * 获取数据源的存储路径
     * @param productLine
     * @return String
     */
    private String getDsFileStoreDir(String productLine) {
//        String productLine = ContextManager.getProductLine();
        String basePath = productLine + File.separator + dsFileBaseDir;
        return basePath;
    }

    public FileService getFileService() {
        return fileService;
    }

    /**
     * 
     * @param fileService
     * 
     */
    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataSourceDefine getDsDefine(String productLine, String dsName) throws DataSourceOperationException {
        String fileName = getDatasourceDefineNameByIdOrName(productLine, dsName);
        if (fileName == null) {
            return null;
        }
        try {
            return buildResult(fileName);
        } catch (FileServiceException e) {
            logger.error("error : " + e.getMessage());
            throw new DataSourceOperationException("未找到正确的数据源定义信息", e);
        }

    }
    
}
