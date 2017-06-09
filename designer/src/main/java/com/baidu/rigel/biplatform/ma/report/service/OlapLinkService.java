package com.baidu.rigel.biplatform.ma.report.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaType;
import com.baidu.rigel.biplatform.ma.report.model.LinkInfo;
import com.baidu.rigel.biplatform.ma.report.model.LinkParams;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.query.QueryContext;

/**
 * 多维报表跳转操作相关service
 * 
 * @author majun04
 *
 */
public interface OlapLinkService {
    /**
     * 根据条件得到所有本产品线下所有包含报表组件的ReportDesignModel列表
     * 
     * @return 符合条件的ReportDesignModel列表
     */
    public HashMap<ReportDesignModel, ExtendAreaType> getDesignModelListByTypeMap(
            List<ExtendAreaType> typeList);

    /**
     * 从报表设计模型中，得到所有报表设计用所有维度
     * 
     * @param tableDesignModel 报表设计模型
     * @param tableType 报表组件类型
     * @return 得到报表参数列表
     */
    public List<Dimension> getTargetTableDimList(ReportDesignModel tableDesignModel, String tableType);

    /**
     * 根据多维报表模型和table对应的区域，得到所有table中要有的维度对象
     * 
     * @param olapTableDesignModel 多维报表设计模型
     * @param olapTableArea 多维报表area区域对象
     * @return 维度对象列表
     */
    public List<Dimension> getOlapDims(ReportDesignModel olapTableDesignModel, ExtendArea olapTableArea);

    /**
     * 根据传入的uniqueName和公共条件，解析并构造跳转所需的条件对象
     * 
     * @param uniqueName uniqueName
     * @param olapTableDesignModel olapTableDesignModel
     * @param queryContext queryContext
     * @return 构造完毕的条件对象
     */
    public Map<String, Map<String, String>> buildConditionMapFromRequestParams(String uniqueName,
            ReportDesignModel olapTableDesignModel, QueryContext queryContext);

    /**
     * 根据跳转对象和前端传入的条件对象，构建LinkBridgeParams对象
     * 
     * @param linkInfo 跳转对象
     * @param conditionMap 条件map对象
     * @return 返回构造完成的LinkBridgeParams对象
     */
    public Map<String, LinkParams> buildLinkBridgeParams(LinkInfo linkInfo,
            Map<String, Map<String, String>> conditionMap);
}
