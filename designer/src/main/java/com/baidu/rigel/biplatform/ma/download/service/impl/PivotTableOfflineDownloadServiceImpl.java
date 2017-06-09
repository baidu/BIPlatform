/*
 * Copyright 2000-2011 baidu.com All right reserved. 
 */
package com.baidu.rigel.biplatform.ma.download.service.impl;

import java.util.Map;

import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.ma.download.service.DownloadTableDataService;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;

/**
 * 多维表离线数据下载实现类
 * @author yichao.jiang 2015年5月25日 下午8:08:26
 */
public class PivotTableOfflineDownloadServiceImpl implements DownloadTableDataService {

    /**
     * 私有构造方法
     */
    public PivotTableOfflineDownloadServiceImpl() {        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String downloadTableData(QuestionModel questionModel, LogicModel logicModel ) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("当前暂不支持");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String downloadTableData(QuestionModel questionModel, LogicModel logicModel, 
            Map<String, Object> setting) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("当前暂不支持");       
    }   
}
