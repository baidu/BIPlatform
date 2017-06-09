package com.baidu.rigel.biplatform.ma.download.service;

import com.baidu.rigel.biplatform.ma.download.DownloadType;

/**
 * 下载服务工厂类
 * @author yichao.jiang 2015年5月25日 下午8:20:06
 */
public class DownloadServiceFactory {

    /**
     * 获取下载表格数据服务
     * @param downType 下载服务类型
     * @return 表格下载服务
     */
    public static DownloadTableDataService getDownloadTableDataService(DownloadType downType) {
        return DownloadTableDataHelper.getDownloadTableDataService(downType.getName() + "_" +downType.getDsType());
    }
}
