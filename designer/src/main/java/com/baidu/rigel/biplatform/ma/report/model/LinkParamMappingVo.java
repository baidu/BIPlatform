package com.baidu.rigel.biplatform.ma.report.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 多维报表和平面报表参数映射vo
 * 
 * @author majun04
 *
 */
public class LinkParamMappingVo {
    /**
     * 平面报表参数列表
     */
    private List<Map<String, String>> planeTableParamList = new ArrayList<Map<String, String>>();
    /**
     * 多维报表维度列表
     */
    private List<Map<String, String>> olapTableDimList = new ArrayList<Map<String, String>>();

    /**
     * 添加目标跳转报表参数映射内容
     * 
     * @param name 平面报表参数名称
     * @param selectedDim 多维报表维度名称
     */
    public void addTargetTableParam(String name, String caption, String selectedDim) {
        Map<String, String> planeTableParamMap = new HashMap<String, String>();
        planeTableParamMap.put("name", name);
        planeTableParamMap.put("caption", caption);
        planeTableParamMap.put("selectedDim", selectedDim);
        planeTableParamList.add(planeTableParamMap);
    }

    /**
     * 添加多维报表参数映射内容
     * 
     * @param text 多维报表参数caption
     * @param value 多维报表参数value
     */
    public void addOlapTableDim(String text, String value) {
        Map<String, String> olapTableDimMap = new HashMap<String, String>();
        olapTableDimMap.put("text", text);
        olapTableDimMap.put("value", value);
        olapTableDimList.add(olapTableDimMap);
    }

    /**
     * @return the planeTableParamList
     */
    public List<Map<String, String>> getPlaneTableParamList() {
        return planeTableParamList;
    }

    /**
     * @param planeTableParamList the planeTableParamList to set
     */
    public void setPlaneTableParamList(List<Map<String, String>> planeTableParamList) {
        this.planeTableParamList = planeTableParamList;
    }

    /**
     * @return the olapTableDimList
     */
    public List<Map<String, String>> getOlapTableDimList() {
        return olapTableDimList;
    }

    /**
     * @param olapTableDimList the olapTableDimList to set
     */
    public void setOlapTableDimList(List<Map<String, String>> olapTableDimList) {
        this.olapTableDimList = olapTableDimList;
    }

}