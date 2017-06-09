package com.baidu.rigel.biplatform.ma.report.query.pivottable;

import java.util.List;
import java.util.Map;

import com.baidu.rigel.biplatform.ac.query.model.PageInfo;
import com.baidu.rigel.biplatform.ma.report.query.newtable.bo.OperationColumnDefine;
import com.google.common.collect.Lists;

/**
 * 平面表模型定义
 * @author yichao.jiang
 *
 */
public class PlaneTable extends BaseTable {

    /**
     * 序列id
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * 分页信息
     */
    private PageInfo pageInfo;
    
    /**
     * 平面表列上的属性定义
     */
    private List<PlaneTableColDefine> colDefs = Lists.newArrayList();
    
    /**
     * 基于列的数据信息
     */
    private List<Map<String, String>> data = Lists.newArrayList();
    
    
    /**
     * 操作列
     */
    private List<OperationColumnDefine> operationColumnDefine;
            
    /**
     * @return the pageInfo
     */
    public PageInfo getPageInfo() {
        if (this.pageInfo == null) {
            this.pageInfo = new PageInfo();
        }
        return pageInfo;
    }

    /**
     * @param pageInfo the pageInfo to set
     */
    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }

    /**
     * @return the colDefs
     */
    public List<PlaneTableColDefine> getColDefines() {
        return colDefs;
    }

    /**
     * @param colDefs the colDefs to set
     */
    public void setColDefines(List<PlaneTableColDefine> colDefs) {
        this.colDefs = colDefs;
    }
        
    /**
     * @return the data
     */
    public List<Map<String, String>> getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(List<Map<String, String>> data) {
        this.data = data;
    }

    /**
     * default generate get operationColumnDefine
     * @return the operationColumnDefine
     */
    public List<OperationColumnDefine> getOperationColumnDefine() {
        return operationColumnDefine;
    }

    /**
     * default generate set operationColumnDefine
     * @param operationColumnDefine the operationColumnDefine to set
     */
    public void setOperationColumnDefine(List<OperationColumnDefine> operationColumnDefine) {
        this.operationColumnDefine = operationColumnDefine;
    }
}
