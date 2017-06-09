package com.baidu.rigel.biplatform.ma.download.service;

import java.util.Map;

import com.baidu.rigel.biplatform.ma.download.DownloadType;
import com.baidu.rigel.biplatform.ma.download.service.impl.PivotTableOfflineDownloadServiceImpl;
import com.baidu.rigel.biplatform.ma.download.service.impl.PivotTableOnlineDownloadServiceImpl;
import com.baidu.rigel.biplatform.ma.download.service.impl.PlaneTableOfflineDownloadServiceImpl;
import com.baidu.rigel.biplatform.ma.download.service.impl.PlaneTableOnlineDownloadServiceImpl;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceType;
import com.google.common.collect.Maps;

/**
 * 
 * 表格数据下载辅助类
 * 
 * @author jiangjiangyichao 2015年5月25日 下午4:47:08
 */
public class DownloadTableDataHelper {
    /**
     * 报表数据下载仓库类
     */
    private static final Map<String, DownloadTableDataService> SERVICE_REPOSITORY = Maps.newHashMap();

    static {
        /**
         * 提供默认表格数据下载支持(在线支持、离线支持)
         */
        try {
            // TODO 修改
            String mysqlDataDownloadForPlaneTableOnline = DownloadType.PLANE_TABLE_ONLINE.getName() + "_" + DataSourceType.MYSQL;
            SERVICE_REPOSITORY.put(mysqlDataDownloadForPlaneTableOnline ,
                    new PlaneTableOnlineDownloadServiceImpl());
            String oracleDataDownloadForPlaneTableOnline = DownloadType.PLANE_TABLE_ONLINE.getName() + "_" + DataSourceType.ORACLE;
            SERVICE_REPOSITORY.put(oracleDataDownloadForPlaneTableOnline ,
                    new PlaneTableOnlineDownloadServiceImpl());
            String h2DataDownloadForPlaneTableOnline = DownloadType.PLANE_TABLE_ONLINE.getName() + "_" + DataSourceType.H2;
            SERVICE_REPOSITORY.put(h2DataDownloadForPlaneTableOnline ,
                    new PlaneTableOnlineDownloadServiceImpl());
            String paloDataDownloadForPlaneTableOnline = DownloadType.PLANE_TABLE_ONLINE.getName()
                    + "_" + DataSourceType.PALO;
            SERVICE_REPOSITORY.put(paloDataDownloadForPlaneTableOnline ,
                    new PlaneTableOnlineDownloadServiceImpl());
            String druidDataDownloadForPlaneTableOnline = DownloadType.PLANE_TABLE_ONLINE.getName()
                    + "_" + DataSourceType.DRUID;
            SERVICE_REPOSITORY.put(druidDataDownloadForPlaneTableOnline ,
                    new PlaneTableOnlineDownloadServiceImpl());
            String mysqlDataDownloadForPlaneTableOffline = DownloadType.PLANE_TABLE_OFFLINE.getName() + "_" + DataSourceType.MYSQL;
            SERVICE_REPOSITORY.put(mysqlDataDownloadForPlaneTableOffline ,
                    new PlaneTableOfflineDownloadServiceImpl());
            String oracleDataDownloadForPlaneTableOffline = DownloadType.PLANE_TABLE_OFFLINE.getName() + "_" + DataSourceType.ORACLE;
            SERVICE_REPOSITORY.put(oracleDataDownloadForPlaneTableOffline ,
                    new PlaneTableOfflineDownloadServiceImpl());
            String h2DataDownloadForPlaneTableOffline = DownloadType.PLANE_TABLE_OFFLINE.getName() + "_" + DataSourceType.H2;
            SERVICE_REPOSITORY.put(h2DataDownloadForPlaneTableOffline ,
                    new PlaneTableOfflineDownloadServiceImpl());
            String paloDataDownloadForPlaneTableOffline = DownloadType.PLANE_TABLE_OFFLINE.getName()
                    + "_" + DataSourceType.PALO;
            SERVICE_REPOSITORY.put(paloDataDownloadForPlaneTableOffline ,
                    new PlaneTableOfflineDownloadServiceImpl());
            String druidDataDownloadForPlaneTableOffline = DownloadType.PLANE_TABLE_OFFLINE.getName()
                    + "_" + DataSourceType.DRUID;
            SERVICE_REPOSITORY.put(druidDataDownloadForPlaneTableOffline ,
                    new PlaneTableOfflineDownloadServiceImpl());
            String mysqlDataDownloadForPivotTableOnline = DownloadType.PIVOT_TABLE_ONLINE.getName() + "_" + DataSourceType.MYSQL;
            SERVICE_REPOSITORY.put(mysqlDataDownloadForPivotTableOnline ,
                    new PivotTableOnlineDownloadServiceImpl());
            String oracleDataDownloadForPivotTableOnline = DownloadType.PIVOT_TABLE_ONLINE.getName() + "_" + DataSourceType.ORACLE;
            SERVICE_REPOSITORY.put(oracleDataDownloadForPivotTableOnline ,
                    new PivotTableOnlineDownloadServiceImpl());
            String h2DataDownloadForPivotTableOnline = DownloadType.PIVOT_TABLE_ONLINE.getName() + "_" + DataSourceType.H2;
            SERVICE_REPOSITORY.put(h2DataDownloadForPivotTableOnline ,
                    new PivotTableOnlineDownloadServiceImpl());
            String mysqlDataDownloadForPivotTableOffline = DownloadType.PIVOT_TABLE_OFFLINE.getName() + "_" + DataSourceType.MYSQL;
            SERVICE_REPOSITORY.put(mysqlDataDownloadForPivotTableOffline ,
                    new PivotTableOfflineDownloadServiceImpl());
            String oracleDataDownloadForPivotTableOffline = DownloadType.PIVOT_TABLE_OFFLINE.getName() + "_" + DataSourceType.ORACLE;
            SERVICE_REPOSITORY.put(oracleDataDownloadForPivotTableOffline ,
                    new PivotTableOfflineDownloadServiceImpl());
            String h2DataDownloadForPivotTableOffline = DownloadType.PIVOT_TABLE_OFFLINE.getName() + "_" + DataSourceType.H2;
            SERVICE_REPOSITORY.put(h2DataDownloadForPivotTableOffline ,
                    new PivotTableOfflineDownloadServiceImpl());
//            SERVICE_REPOSITORY.put(DownloadType.PLANE_TABLE_OFFLINE.getName(), new PlaneTableOfflineDownloadServiceImpl());
//            SERVICE_REPOSITORY.put(DownloadType.PIVOT_TABLE_ONLINE.getName(), new PivotTableOnlineDownloadServiceImpl());
//            SERVICE_REPOSITORY.put(DownloadType.PIVOT_TABLE_OFFLINE.getName(), new PivotTableOfflineDownloadServiceImpl());
        } catch (Exception e) {
        }
    }

    /**
     * 注册下载服务方法
     * 
     * @param dsType
     * @param downloadClazz
     */
    public static void registryDownloadTableDataService(String downType, @SuppressWarnings("rawtypes") Class downloadClazz) {
        try {
            SERVICE_REPOSITORY.put(downType, (DownloadTableDataService) downloadClazz.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 
     * @param downType 下载类型
     * @return DownloadTableDataService 表格下载接口
     */
    public static DownloadTableDataService getDownloadTableDataService(String downType) {
        return SERVICE_REPOSITORY.get(downType);
    }

    /**
     * 依据配置注册下载类型
     */
    public static void registryDsMetaServices(String type, @SuppressWarnings("rawtypes") Class clazz) {
        try {
            SERVICE_REPOSITORY.put(type, (DownloadTableDataService) clazz.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
