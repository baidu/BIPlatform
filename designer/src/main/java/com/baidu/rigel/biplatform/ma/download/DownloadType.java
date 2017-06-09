package com.baidu.rigel.biplatform.ma.download;

import com.baidu.rigel.biplatform.ma.model.ds.DataSourceType;

/**
 * 数据下载类型
 * @author yichao.jiang 2015年5月25日 下午4:53:41
 */
public enum DownloadType {
    /**
     * 平面表在线下载数据
     */
    PLANE_TABLE_ONLINE("planeTableOnline"),
    /**
     * 平面表离线下载数据
     */
    PLANE_TABLE_OFFLINE("planeTableOffline"),
    
    /**
     * 多维表在线下载数据
     */
    PIVOT_TABLE_ONLINE("pivotTableOnline"),
    /**
     * 多维表离线下载数据
     */
    PIVOT_TABLE_OFFLINE("pivotTableOffline");
    
    private String name;
    
    
    private String dsType;

    /**
     * default generate get name
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * default generate set name
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 私有构造方法
     * @param name
     */
    private DownloadType(String name) {
        this.setName(name);
    }

    /**
     * default generate get dsType
     * @return the dsType
     */
    public String getDsType() {
        if (dsType == null) {
            dsType = DataSourceType.MYSQL.name();
        }
        return dsType;
    }

    /**
     * default generate set dsType
     * @param dsType the dsType to set
     */
    public void setDsType(String dsType) {
        this.dsType = dsType;
    }
}
