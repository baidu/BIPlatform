package com.baidu.rigel.biplatform.ma.download.service.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.ac.query.MiniCubeConnection;
import com.baidu.rigel.biplatform.ac.query.MiniCubeDriverManager;
import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.model.ConfigQuestionModel;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.ma.download.service.DownloadTableDataService;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;
import com.baidu.rigel.biplatform.ma.resource.utils.DataModelUtils;
import com.google.common.collect.Maps;

/**
 * 平面表在线下载服务 
 * @author yichao.jiang 2015年5月26日 下午5:52:47
 */
public class PlaneTableOnlineDownloadServiceImpl implements DownloadTableDataService {

    /**
     * 日志对象
     */
    private static final Logger LOG = LoggerFactory.getLogger(PlaneTableOnlineDownloadServiceImpl.class);
    
    /**
     * 私有构造方法
     */
    public PlaneTableOnlineDownloadServiceImpl() {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String downloadTableData(QuestionModel questionModel, LogicModel logicModel) {
        return this.getCsvStr4PlaneTable(questionModel, logicModel, Maps.newHashMap());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String downloadTableData(QuestionModel questionModel, LogicModel logicModel, 
            Map<String, Object> setting) {
        return this.getCsvStr4PlaneTable(questionModel, logicModel, setting);
    }
    
    /**
     * 获取平面表的下载csv字符串
     * getCsvStr4PlaneTable
     * @param questionModel
     * @param logicModel
     * @param setting
     * @return
     */
    private String getCsvStr4PlaneTable(QuestionModel questionModel, LogicModel logicModel,
            Map<String, Object> setting) {
        // 强转为ConfigQuestionModel
        ConfigQuestionModel configQuestionModel = (ConfigQuestionModel) questionModel;
        // 数据连接信息
        DataSourceInfo dataSourceInfo = configQuestionModel.getDataSourceInfo();
        // 查询请求连接
        MiniCubeConnection connection = MiniCubeDriverManager.getConnection(dataSourceInfo);
        // 查询，并返回DataModel结果
        DataModel dataModel = connection.query(configQuestionModel);
        long begin = System.currentTimeMillis();
        // 获取csv字符串
        String csvString = DataModelUtils.convertDataModel2CsvStringForPlaneTable(configQuestionModel.getCube(),
                dataModel, logicModel, setting);
        LOG.info("[INFO]transform data  model to csvString cost : " + (System.currentTimeMillis() - begin) + " ms");
        return csvString;
    }
    
}
