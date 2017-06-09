package com.baidu.rigel.biplatform.ma.report.query.newtable.bo;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

/**
 * 前端重构表格之后的多维表格数据结构
 * 
 * @author majun04
 *
 */
public class MutilDimTable {
    /**
     * 表格横轴上的维度信息列表集合
     */
    private List<DimDataDefine> dims = Lists.newArrayList();
    /**
     * 表格纵轴上的指标信息列表集合
     */
    private List<IndDataDefine> inds = Lists.newArrayList();
    /**
     * 表格所要展示的数据对象，以map格式封装
     */
    private List<Map<String, String>> data = Lists.newArrayList();
    /**
     * 维度描述，需要按照顺序给出，用以作为前端的维度列表头展示
     */
    private List<String> dimsDesc = Lists.newArrayList();
    /**
     * 操作列对象列表
     */
    private List<OperationColumnDefine> operationColumns = Lists.newArrayList();

    /**
     * @param dims
     * @param inds
     * @param data
     */
    public MutilDimTable(List<DimDataDefine> dims, List<IndDataDefine> inds, List<Map<String, String>> data) {
        super();
        this.dims = dims;
        this.inds = inds;
        this.data = data;
    }

    /**
     * @param dims
     * @param inds
     * @param data
     * @param dimsDesc
     */
    public MutilDimTable(List<DimDataDefine> dims, List<IndDataDefine> inds, List<Map<String, String>> data,
            List<String> dimsDesc) {
        super();
        this.dims = dims;
        this.inds = inds;
        this.data = data;
        this.dimsDesc = dimsDesc;
    }

    /**
     * @return the dims
     */
    public List<DimDataDefine> getDims() {
        return dims;
    }

    /**
     * @param dims the dims to set
     */
    public void setDims(List<DimDataDefine> dims) {
        this.dims = dims;
    }

    /**
     * @return the inds
     */
    public List<IndDataDefine> getInds() {
        return inds;
    }

    /**
     * @param inds the inds to set
     */
    public void setInds(List<IndDataDefine> inds) {
        this.inds = inds;
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
     * @return the dimsDesc
     */
    public List<String> getDimsDesc() {
        return dimsDesc;
    }

    /**
     * @param dimsDesc the dimsDesc to set
     */
    public void setDimsDesc(List<String> dimsDesc) {
        this.dimsDesc = dimsDesc;
    }

    /**
     * @return the operationColumns
     */
    public List<OperationColumnDefine> getOperationColumns() {
        return operationColumns;
    }

    /**
     * @param operationColumns the operationColumns to set
     */
    public void setOperationColumns(List<OperationColumnDefine> operationColumns) {
        this.operationColumns = operationColumns;
    }

}
