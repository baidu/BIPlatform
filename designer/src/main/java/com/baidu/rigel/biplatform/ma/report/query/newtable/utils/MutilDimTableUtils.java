package com.baidu.rigel.biplatform.ma.report.query.newtable.utils;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ma.report.model.FormatModel;
import com.baidu.rigel.biplatform.ma.report.model.LinkInfo;
import com.baidu.rigel.biplatform.ma.report.query.newtable.bo.DimDataDefine;
import com.baidu.rigel.biplatform.ma.report.query.newtable.bo.IndDataDefine;
import com.baidu.rigel.biplatform.ma.report.query.newtable.bo.MutilDimTable;
import com.baidu.rigel.biplatform.ma.report.query.newtable.bo.OperationColumnDefine;
import com.baidu.rigel.biplatform.ma.resource.utils.DataModelUtils;
import com.baidu.rigel.biplatform.ma.resource.utils.OlapLinkUtils;
import com.google.common.collect.Lists;

public class MutilDimTableUtils {

    public static final String PERSONALITY_SUMMARY_CAPTION = "summaryCaption";

    private static final String DEFAULT_SUMMARY_CAPTION = "汇总";
    private static final String DEFAULT_SUMMARY_CAPTION2 = "合计";

    /**
     * 包装新的MutilDimTable
     * 
     * @param formatModel
     * @param table
     * @param otherSetting
     */
    /**
     * @param formatModel
     * @param table
     * @param otherSetting
     */
    public static void decorateTable(FormatModel formatModel, MutilDimTable table, Map<String, Object> otherSetting) {
        if (formatModel == null) {
            return;
        }
        Map<String, String> dataFormat = formatModel.getDataFormat();
        Map<String, String> toolTips = formatModel.getToolTips();
        Map<String, String> textAlignFormat = formatModel.getTextAlignFormat();
        Map<String, LinkInfo> linkInfoMap = formatModel.getLinkInfo();

        List<IndDataDefine> indDataDefineList = table.getInds();
        for (IndDataDefine define : indDataDefineList) {

            String linkBridgeId = define.getOlapElementId();
            if (!StringUtils.isEmpty(linkBridgeId)) {
                LinkInfo linkInfo = linkInfoMap.get(linkBridgeId);
                // 这里严格判断，只有当设置了明细跳转表，并且参数映射也已经设置完成，才在多维报表处展示超链接
                if (linkInfo != null && !StringUtils.isEmpty(linkInfo.getTargetTableId())
                        && !MapUtils.isEmpty(linkInfo.getParamMapping())) {
                    define.setLinkBridge(linkBridgeId);
                }

            }
            String uniqueName = define.getId();
            // 目前只针对列上放指标进行设置，如果维度要放到列上来，该方法有严重问题
            uniqueName = uniqueName.replaceAll("\\{", "").replaceAll("\\}", "");
            uniqueName = MetaNameUtil.parseUnique2NameArray(uniqueName)[1];
            if (dataFormat != null) {
                String formatStr = dataFormat.get("defaultFormat");
                if (!StringUtils.isEmpty(dataFormat.get(uniqueName))) {
                    formatStr = dataFormat.get(uniqueName);
                }
                if (!StringUtils.isEmpty(formatStr)) {
                    define.setFormat(formatStr);
                }
            }
            if (toolTips != null) {
                String toolTip = toolTips.get(uniqueName);
                if (StringUtils.isEmpty(toolTip)) {
                    toolTip = uniqueName;
                }
                define.setToolTip(toolTip);
            }
            if (textAlignFormat != null) {
                String align = textAlignFormat.get(uniqueName);
                if (StringUtils.isEmpty(align)) {
                    align = "left";
                }
                define.setAlign(align);
            }

        }

        // 针对每个报表都可分别设置维度“汇总”的展示文案，然后在此逐一进行替换
        Object summaryCaptionObj = otherSetting.get(PERSONALITY_SUMMARY_CAPTION);
        String summaryCaptionStr = "";
        if (summaryCaptionObj != null && StringUtils.isNotEmpty(summaryCaptionObj.toString())) {
            summaryCaptionStr = summaryCaptionObj.toString();
        }
        if (StringUtils.isNotEmpty(summaryCaptionStr)) {
            for (DimDataDefine dimDataDefine : table.getDims()) {
                String origName = dimDataDefine.getName();
                // 当发现caption的最后文案是“汇总”或者“合计”的时候，才拿个性化的汇总文案进行替换
                if (origName.lastIndexOf(DEFAULT_SUMMARY_CAPTION) == origName.length() - 2
                        || origName.lastIndexOf(DEFAULT_SUMMARY_CAPTION2) == origName.length() - 2) {
                    // String newName = origName.replaceAll(DEFAULT_SUMMARY_CAPTION, summaryCaptionStr);
                    dimDataDefine.setName(summaryCaptionStr);
                }
            }
        }

        boolean isShowZero = DataModelUtils.isShowZero(otherSetting);
        if (isShowZero) {
            for (Map<String, String> dataMap : table.getData()) {
                for (Map.Entry<String, String> dataValue : dataMap.entrySet()) {
                    if (StringUtils.isEmpty(dataValue.getValue())) {
                        dataValue.setValue("0");
                    }
                }
            }

        }

        /**
         * 为table增加操作列属性, add by majun
         */
        table.setOperationColumns(DataModelUtils.generateOperationColumnList(linkInfoMap));

    }
}
