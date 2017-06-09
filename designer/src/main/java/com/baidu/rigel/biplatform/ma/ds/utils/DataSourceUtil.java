package com.baidu.rigel.biplatform.ma.ds.utils;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceGroupDefine;
import com.baidu.rigel.biplatform.ma.report.utils.ContextManager;

/**
 * 数据源工具类
 * 
 * @author jiangyichao
 *
 */
@Service
public final class DataSourceUtil {
    
    /**
     * 数据源组路径
     */
    private static String dsFileBaseDir;
    
    /**
     * 注入数据源组存放路径
     * 
     * @param dsDir
     */
    @Value("${biplatform.ma.ds.location}")
    public void setDsFileBaseDir(String dsDir) {
        DataSourceUtil.dsFileBaseDir = dsDir;
    }
    
    
    /**
     * 
     * @param ds
     * @return
     */
    public static String getDsFileName(DataSourceDefine ds) {
        String basePath = getDsFileStoreDir ();
        return basePath + File.separator + ds.getId () + "_" + ds.getName();
    }
    /**
     * 根据文件名，返回文件的详细路径
     * 
     * @param fileName
     *            文件名
     * @return 文件路径信息
     */
    public static String getDsFileName(String fileName) {
        String basePath = getDsFileStoreDir ();
        return basePath + File.separator + fileName;
    }
    
    /**
     * 获取数据源组文件的文件名（含路径），对数据源组名称进行hash
     * 
     * @param dsG
     *            数据源组定义
     * @return 返回数据源组定义文件文件名(绝对路径)
     */
    public static String getDsGroupFileNameUsingHashCode(DataSourceGroupDefine dsG) {
        String basePath = getDsGroupFileStoreDir ();
        String nameHashCode = null;
        if(dsG.getName() != null) {
            nameHashCode = String.valueOf(dsG.getName().hashCode());
        }
        return basePath + File.separator + dsG.getId () + "_" + nameHashCode;
    }
    
    /**
     * 
     * @param dsG
     * @return
     */
    public static String getDsGroupFileName(DataSourceGroupDefine dsG) {
        String basePath = getDsGroupFileStoreDir ();
        return basePath + File.separator + dsG.getId () + "_" + dsG.getName();
    }
    
    /**
     * 根据文件名，返回文件的详细路径信息
     * 
     * @param fileName
     *            文件名称
     * @return 文件的详细路径
     */
    public static String getDsGroupFileName(String fileName) {
        return getDsFileName (fileName);
    }
    
    /**
     * 获取当前产品线数据源的存储路径
     * 
     * @return 数据源存储路径
     */
    public static String getDsFileStoreDir() {
        return getDsFileStoreDir (ContextManager.getProductLine ());
    }
    
    /**
     * 获取某产品线下的数据源存储路径
     * 
     * @param productLine
     *            产品线
     * @return 数据源存储路径
     */
    public static String getDsFileStoreDir(String productLine) {
        String basePath = productLine + File.separator + dsFileBaseDir;
        return basePath;
    }
    
    /**
     * 获取数据源组的存储路径
     * 
     * @return 数据源组存储路径
     */
    public static String getDsGroupFileStoreDir() {
        return getDsFileStoreDir ();
    }
    
    /**
     * 获取某产品线下数据源组存储路径
     * 
     * @param productLine
     *            产品线
     * @return 数据源组存储路径
     */
    public static String getDsGroupFileStoreDir(String productLine) {
        return getDsFileStoreDir (productLine);
    }
}
