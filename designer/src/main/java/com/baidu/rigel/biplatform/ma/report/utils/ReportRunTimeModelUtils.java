package com.baidu.rigel.biplatform.ma.report.utils;

import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaType;
import com.baidu.rigel.biplatform.ma.report.model.LiteOlapExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;

/**
 * ReportRunTimeModelUtils，集中处理普通类型和liteolap类型的报表模型结果
 * 
 * @author majun04
 *
 */
public class ReportRunTimeModelUtils {
    /**
     * 根据区域属性，返回对应的区域逻辑模型
     * 
     * @param targetArea targetArea
     * @param model reportDesignmodel
     * @return 返回对应的区域逻辑模型
     */
    public static LogicModel getLogicModel4AnyType(ExtendArea targetArea, ReportDesignModel model) {
        LogicModel targetLogicModel = null;
        LogicModel logicModel = targetArea.getLogicModel();
        if (targetArea.getType() == ExtendAreaType.LITEOLAP_TABLE) {
            LiteOlapExtendArea liteOlapArea = (LiteOlapExtendArea) model.getExtendById(targetArea.getReferenceAreaId());
            targetLogicModel = liteOlapArea.getLogicModel();
        } else {
            targetLogicModel = logicModel;
        }

        if (targetLogicModel == null) {
            targetLogicModel = new LogicModel();
        }
        return targetLogicModel;
    }

    /**
     * 根据区域不同属性，返回对应的实际areaId
     * 
     * @param targetArea targetArea
     * @param model reportDesignmodel
     * @param areaId areaId
     * @return 返回对应的实际areaId
     */
    public static String getAreaId4AnyType(ExtendArea targetArea, ReportDesignModel model, String areaId) {
        String logicModelAreaId = areaId;
        if (targetArea.getType() == ExtendAreaType.LITEOLAP_TABLE) {
            LiteOlapExtendArea liteOlapArea = (LiteOlapExtendArea) model.getExtendById(targetArea.getReferenceAreaId());
            logicModelAreaId = liteOlapArea.getId();
        }

        return logicModelAreaId;
    }
}
