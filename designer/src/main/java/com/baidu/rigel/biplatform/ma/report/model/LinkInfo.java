package com.baidu.rigel.biplatform.ma.report.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

/**
 * 多维报表跳转平面报表的映射关系对象
 * 
 * @author majun04
 *
 */
public class LinkInfo implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 4146274201672309214L;
    /**
     * 跳转列源id
     */
    private String colunmSourceId;
    /**
     * 跳转列源名称
     */
    private String colunmSourceCaption;
    /**
     * 跳转到的报表id
     */
    private String targetTableId;
    
    /**
     * TODO 跳转到的报表id 为了兼容上个版本的配置 为了兼容上个版本的配置，以后考虑统一用targetTableId，并完成线上配置文件转换
     */
    private String planeTableId;
    
    /**
     * 跳转到的报表类型
     */
    private String tableType;
    
    /**
     * key为平面报表参数名称，value为多维报表对应维度对象
     */
    private Map<String, String> paramMapping = new HashMap<String, String>();

    /**
     * default generate get targetTableId
     * @return the targetTableId
     */
    public String getTargetTableId() {
        // TODO 跳转到的报表id 为了兼容上个版本的配置 为了兼容上个版本的配置，以后考虑统一用targetTableId，并完成线上配置文件转换
        if (!StringUtils.isEmpty(this.getPlaneTableId())) {
            this.setTargetTableId(this.getPlaneTableId());
            return this.getPlaneTableId();
        }
        return targetTableId;
    }

    /**
     * TODO 获取跳转到的报表id 为了兼容上个版本的配置 为了兼容上个版本的配置，以后考虑统一用targetTableId，并完成线上配置文件转换
     * @return the planeTableId
     */
    public String getPlaneTableId() {
        return planeTableId;
    }

    /**
     * default generate set targetTableId
     * @param targetTableId the targetTableId to set
     */
    public void setTargetTableId(String targetTableId) {
        this.targetTableId = targetTableId;
    }

    /**
     * default generate get tableType
     * @return the tableType
     */
    public String getTableType() {
        return tableType;
    }

    /**
     * default generate set tableType
     * @param tableType the tableType to set
     */
    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    /**
     * @return the paramMapping
     */
    public Map<String, String> getParamMapping() {
        return paramMapping;
    }

    /**
     * @param paramMapping the paramMapping to set
     */
    public void setParamMapping(Map<String, String> paramMapping) {
        this.paramMapping = paramMapping;
    }

    /**
     * @return the colunmSourceId
     */
    public String getColunmSourceId() {
        return colunmSourceId;
    }

    /**
     * @param colunmSourceId the colunmSourceId to set
     */
    public void setColunmSourceId(String colunmSourceId) {
        this.colunmSourceId = colunmSourceId;
    }

    /**
     * @return the colunmSourceCaption
     */
    public String getColunmSourceCaption() {
        return colunmSourceCaption;
    }

    /**
     * @param colunmSourceCaption the colunmSourceCaption to set
     */
    public void setColunmSourceCaption(String colunmSourceCaption) {
        this.colunmSourceCaption = colunmSourceCaption;
    }

    

}
