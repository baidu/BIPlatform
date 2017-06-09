package com.baidu.rigel.biplatform.ma.ds.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import com.baidu.rigel.biplatform.api.client.service.FileService;
import com.baidu.rigel.biplatform.api.client.service.FileServiceException;
import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceOperationException;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceGroupService;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceService;
import com.baidu.rigel.biplatform.ma.ds.utils.DataSourceUtil;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceGroupDefine;
import com.baidu.rigel.biplatform.ma.report.utils.ContextManager;

/**
 * 数据源组服务接口实现
 * 
 * @author jiangyichao
 *
 */
@Service("dsGroupService")
public class DataSourceGroupServiceImpl implements DataSourceGroupService {

    /**
     * 日志对象
     */
    private static final Logger LOG = LoggerFactory.getLogger(DataSourceGroupServiceImpl.class);
    /**
     * 数据源服务
     */
    @Resource(name = "dsService")
    private DataSourceService dsService;

    /**
     * 文件服务接口
     */
    @Resource(name = "fileService")
    private FileService fileService;

    /**
     * @{inheritDoc
     */
    @Override
    public DataSourceGroupDefine saveOrUpdateDataSourceGroup(DataSourceGroupDefine dsG, String securityKey)
            throws DataSourceOperationException {
        // 检测数据源组
        checkDataSourceGroupDefine(dsG, securityKey);
        try {
            // 如果修改了数据源的名称，则先写新的数据源，然后删除原来的数据源文件
            DataSourceGroupDefine oldDsG = this.getDataSourceGroupDefine(dsG.getId());
            String oldDsFileName = null;
            if (oldDsG != null && !oldDsG.getName().equals(dsG.getName())) { // 修改了数据源组名称
                oldDsFileName = DataSourceUtil.getDsGroupFileName(oldDsG);
                if (this.isNameExist(dsG.getName())) {
                    throw new DataSourceOperationException("datasource group name already exist : " + dsG.getName());
                }
            }

            boolean rmOperResult = false;
            if (oldDsG != null) {
                String fileName = DataSourceUtil.getDsGroupFileName(oldDsG);
                // 删除原有名称未使用HashCode的数据源组
                if (fileName != null) {
                    try {
                        rmOperResult = fileService.rm(fileName);
                    } catch (Exception e) {
                        fileName = DataSourceUtil.getDsGroupFileNameUsingHashCode(oldDsG);
                        if (fileName != null) {
                            rmOperResult = fileService.rm(fileName);
                        }
                    }
                }
            }

            // 获取新的数据源组文件名称，该数据源名称已经经过HashCode
            String fileNameUsingHashCode = DataSourceUtil.getDsGroupFileNameUsingHashCode(dsG);
            // if (oldDsFileName != null) { // 此处操作意味用户做了修改数据源名称操作
            // rmOperResult = fileService.rm(oldDsFileName);
            // }
            if (oldDsFileName == null || rmOperResult) { // 删除操作成功
                fileService.write(fileNameUsingHashCode, SerializationUtils.serialize(dsG));
            }
        } catch (Exception e) {
            // 如果发生异常 考虑回滚或者其他容错操作
            LOG.error(e.getMessage(), e);
            throw new DataSourceOperationException("Error Happend for save or update datasource :" + e);
        }
        return dsG;
    }

    /**
     * 检验数据源组是否合法
     * 
     * @param dsG 数据源组
     * @param securityKey 密钥
     * @throws DataSourceOperationException 数据源操作异常
     */
    private void checkDataSourceGroupDefine(DataSourceGroupDefine dsG, String securityKey)
            throws DataSourceOperationException {
        if (dsG == null) {
            LOG.error("datasource group can not be null");
            throw new DataSourceOperationException("datasource group can not be null");
        }
        if (StringUtils.isBlank(dsG.getProductLine())) {
            LOG.error("product line can not be null");
            throw new DataSourceOperationException("product line can not be null");
        }
        // 名称一样，id不同，则为新增重名数据源组；如果名称和id都一样，则为修改数据源
        if (isNameExist(dsG.getName()) && !isNameExist(dsG.getId())) {
            LOG.debug("datasource group's name already exist");
            throw new DataSourceOperationException("datasource group's name already exist");
        }
        if (!isValidate(dsG, securityKey)) {
            // 不检查目前组内所有的数据源是否有效
            LOG.warn("some of db connection info not correct,please check.");
        }
    }

    /**
     * @{inheritDoc
     */
    @Override
    public boolean removeDataSourceGroup(String id) throws DataSourceOperationException {
        DataSourceGroupDefine dsG = this.getDataSourceGroupDefine(id);
        if (dsG == null) {
            String msg = "cant't get ds define info by id : " + id;
            LOG.error(msg);
            throw new DataSourceOperationException("不能通过指定数据源组id找到数据源组定义： id ＝ " + id);
        }
        try {
            // 获取文件名
            String fileName = getDatasourceGroupDefineNameByIdOrName(ContextManager.getProductLine(), id);
            return fileService.rm(DataSourceUtil.getDsGroupFileName(fileName));
        } catch (FileServiceException e) {
            LOG.error(e.getMessage(), e);
            throw new DataSourceOperationException(e);
        }
    }

    /**
     * @{inheritDoc
     */
    @Override
    public boolean isNameExist(String name) throws DataSourceOperationException {
        // 获取数据源组存储路径
        String dir = DataSourceUtil.getDsGroupFileStoreDir(ContextManager.getProductLine());
        try {
            String[] fileList = fileService.ls(dir);
            if (fileList == null || fileList.length == 0) {
                return false;
            }
            for (String fileName : fileList) {
                // 如果名称或者名称的hashCode存在，则认为该文件存在
                if (fileName.contains(name) || fileName.contains(String.valueOf(name.hashCode()))) {
                    return true;
                }
            }
            return false;
        } catch (FileServiceException e) {
            LOG.debug(e.getMessage(), e);
            throw new DataSourceOperationException(e);
        }
    }

    /**
     * @{inheritDoc
     */
    @Override
    public boolean isValidate(DataSourceGroupDefine dsG, String securityKey) throws DataSourceOperationException {
        if (dsG == null) {
            return false;
        }
        for (DataSourceDefine ds : dsG.getDataSourceList().values()) {
            if (!dsService.isValidateConn(ds, securityKey)) {
                LOG.warn("datasource:{} in dataSoucegroup:{} is not incollect,please check.",
                        ds.getName(), dsG.getName());
                return false;
            }
        }
        return true;
    }

    /**
     * @{inheritDoc
     */
    @Override
    public DataSourceGroupDefine[] listAll() throws DataSourceOperationException {
        String[] listFile = null;
        try {
            listFile = fileService.ls(DataSourceUtil.getDsGroupFileStoreDir(ContextManager.getProductLine()));
        } catch (FileServiceException e) {
            LOG.error(e.getMessage(), e);
            throw new DataSourceOperationException(e);
        }
        if (listFile == null || listFile.length == 0) {
            return new DataSourceGroupDefine[0];
        }
        final List<DataSourceGroupDefine> rs = this.readDsGroupDefineFromFile(listFile);
        if (rs.size() != listFile.length) {
            return new DataSourceGroupDefine[0];
        }
        return rs.toArray(new DataSourceGroupDefine[0]);
    }

    /**
     * @{inheritDoc
     */
    @Override
    public DataSourceGroupDefine getDataSourceGroupDefine(String id) throws DataSourceOperationException {
        String fileName = this.getDatasourceGroupDefineNameByIdOrName(ContextManager.getProductLine(), id);
        if (fileName == null) {
            return null;
        }
        try {
            return this.readDsGroupDefineFromFile(fileName);
        } catch (FileServiceException e) {
            LOG.error("error : " + e.getMessage());
            throw new DataSourceOperationException("未找到正确的数据源组定义信息", e);
        }
    }

    /**
     * @{inheritDoc
     */
    @Override
    public DataSourceGroupDefine getDataSourceGroupDefine(String productLine, String name)
            throws DataSourceOperationException {
        String fileName = this.getDatasourceGroupDefineNameByIdOrName(productLine, name);
        if (fileName == null) {
            // 如果第一次fileName为null，需判断名称的hashCode是否存在
            fileName = this.getDatasourceGroupDefineNameByIdOrName(productLine, String.valueOf(name.hashCode()));
            if (fileName == null) {
                return null;
            }
        }
        try {
            return this.readDsGroupDefineFromFile(fileName);
        } catch (FileServiceException e) {
            LOG.error("error : " + e.getMessage());
            throw new DataSourceOperationException("未找到正确的数据源组定义信息", e);
        }
    }

    /**
     * 由文件中读取数据源组定义
     * 
     * @param fileName 文件名
     * @return 数据源组定义
     */
    private DataSourceGroupDefine readDsGroupDefineFromFile(String fileName) throws FileServiceException {
        // 传过来的文件名，为兼容原有逻辑，有可能是名称或者名称的hashcode
        // 先获取文件完整名称
        String fullFileName = DataSourceUtil.getDsGroupFileName(fileName);
        byte[] content = (byte[]) fileService.read(fullFileName);
        DataSourceGroupDefine dsG = null;
        try {
            dsG = (DataSourceGroupDefine) SerializationUtils.deserialize(content);
            if (dsG != null) {
                // 获取使用hashCode作为名称的文件名
                String fileNameUsingHashCode = DataSourceUtil.getDsGroupFileNameUsingHashCode(dsG); 
                // 兼容已有数据源组
                // 如果该文件已经使用HashCode，则忽略；否则，则需将源文件删除，写入使用HashCode的文件
                if (!fullFileName.equals(fileNameUsingHashCode)) {
                    // 删除源文件
                    fileService.rm(fullFileName);
                    fileService.write(fileNameUsingHashCode, SerializationUtils.serialize(dsG));
                }                
            }
            // // TODO dirty solution
            // // 如果仅有一个数据源，将原有数据源组的id赋值给当前活动的数据源
            // if (dsG.listAll().length == 2 ) {
            // Map<String, DataSourceDefine> dsS = dsG.getDataSourceList();
            // Map<String, DataSourceDefine> dsSNew = Maps.newHashMap();
            // for(String key : dsS.keySet()) {
            // if (key == dsG.getId()) {
            // dsSNew.put(key, dsS.get(key));
            // }
            // }
            // dsG.setDataSourceList(dsSNew);
            // fileService.rm(DataSourceUtil.getDsGroupFileName(dsG));
            // // 写入数据源组文件
            // fileService.write(DataSourceUtil.getDsGroupFileName(dsG),
            // SerializationUtils.serialize(dsG));
            // }
        } catch (ClassCastException e) {
            // 兼容未使用数据源组
            dsG = new DataSourceGroupDefine();
            DataSourceDefine ds = (DataSourceDefine) SerializationUtils.deserialize(content);
            // 删除源文件
            fileService.rm(DataSourceUtil.getDsFileName(ds));

            dsG.setId(ds.getId());
            // ds.setId(UuidGeneratorUtils.generate());
            dsG.addDataSourceDefine(ds);
            dsG.setActiveDataSource(ds);

            dsG.setName(ds.getName());
            dsG.setProductLine(ds.getProductLine());
            // 写入数据源组文件
            fileService.write(DataSourceUtil.getDsGroupFileNameUsingHashCode(dsG), SerializationUtils.serialize(dsG));
        }
        return dsG;
    }

    /**
     * 
     * 将文件列表转换为数据源组定义
     * 
     * @param listFile
     * @return
     */
    private List<DataSourceGroupDefine> readDsGroupDefineFromFile(final String[] listFile) {
        final List<DataSourceGroupDefine> rs = new ArrayList<DataSourceGroupDefine>();
        for (final String fileName : listFile) {
            try {
                DataSourceGroupDefine ds = this.readDsGroupDefineFromFile(fileName);
                rs.add(ds);
            } catch (FileServiceException e) {
                LOG.debug(e.getMessage(), e);
            }
        }
        LOG.info("read file successfully");
        return rs;
    }

    /**
     * 根据id或者name获取数据源组的文件名
     * 
     * @param productLine 产品线名称
     * @param idOrName id或者name
     * @return 文件名称
     */
    private String getDatasourceGroupDefineNameByIdOrName(String productLine, String idOrName) {
        String dir = DataSourceUtil.getDsGroupFileStoreDir(productLine);
        String[] ds = null;
        try {
            ds = fileService.ls(dir);
        } catch (FileServiceException e1) {
            LOG.debug(e1.getMessage(), e1);
        }
        if (ds == null || ds.length == 0) {
            String msg = "can not get ds define by id : " + idOrName;
            LOG.error(msg);
            return null;
        }
        Set<String> tmp = new HashSet<String>();
        Collections.addAll(tmp, ds);
        Set<String> dict = new HashSet<String>();
        // 分别添加id和名称
        tmp.stream().forEach((String s) -> {
            dict.add(s.substring(0, s.indexOf("_")));
            dict.add(s.substring(s.indexOf("_") + 1));
        });
        // 如果不包含，则返回空
        if (!dict.contains(idOrName)) {
            return null;
        }
        // 返回完整名称
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
     * @{inheritDoc
     */
    @Override
    public DataSourceDefine getDataSourceDefine(String id, String subId) throws DataSourceOperationException {
        // 获取数据源组定义
        DataSourceGroupDefine dsG = this.getDataSourceGroupDefine(id);
        if (dsG == null) {
            LOG.error("can't get datasource group with id:" + id);
            return null;
        }
        Map<String, DataSourceDefine> dataSourceList = dsG.getDataSourceList();
        if (!dataSourceList.containsKey(subId)) {
            LOG.error("can't get datasource with subId:" + subId);
            return null;
        }
        return dataSourceList.get(subId);
    }
}
