package com.baidu.rigel.biplatform.ma.download.service.impl;

import java.util.Map;

import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.ma.download.service.DownloadTableDataService;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;
/**
 * 平面表离线下载服务实现
 * @author yichao.jiang 2015年5月26日 下午5:54:33
 */
public class PlaneTableOfflineDownloadServiceImpl implements DownloadTableDataService {
    
    /**
     * 私有构造方法
     */
    public PlaneTableOfflineDownloadServiceImpl() {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String downloadTableData(QuestionModel questionModel, LogicModel logicModel) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("暂时不支持");
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
