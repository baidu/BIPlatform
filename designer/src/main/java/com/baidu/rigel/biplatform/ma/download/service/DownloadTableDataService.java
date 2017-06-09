package com.baidu.rigel.biplatform.ma.download.service;

import java.util.Map;

import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;

/**
 * 
 * 表格数据下载接口类
 * @author jiangjiangyichao 2015年5月25日 下午4:43:55
 */
public interface DownloadTableDataService {
    
    /**
     * 将questionModel 转为下载所需的字符串
     * @param questionModel 问题模型
     * @param logicModel 用于保证下载数据顺序正确性
     * @return 下载所需字符串
     */
    public String downloadTableData(QuestionModel questionModel, LogicModel logicModel);
    
    /**
     * 将questionModel 转为下载所需字符串
     * @param questionModel 问题模型
     * @param logicModel 用于保证下载数据顺序正确性
     * @param setting 数据设置信息
     * @return 下载所需字符串
     */
    public String downloadTableData(QuestionModel questionModel, LogicModel logicModel,
            Map<String, Object> setting);
}
