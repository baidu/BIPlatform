package com.baidu.rigel.biplatform.ma.resource.view.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaType;
import com.google.common.collect.Lists;

/**
 * olap跳转信息view类
 * 
 * @author majun04
 *
 */
public class OlapLinkViewObject implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -558746725378781626L;
    /**
     * 所有能被跳转到的报表信息列表
     */
    private List<Map<String, Object>> tableList = new ArrayList<Map<String, Object>>();
    /**
     * 所有多维报表上能添加链接的列信息列表
     */
    private List<Map<String, String>> columnDefine = new ArrayList<Map<String, String>>();
    /**
     * 操作列信息
     */
    private List<Map<String, String>> operationColumn = new ArrayList<Map<String, String>>();

    /**
     * 为tableList增加实例对象
     * 
     * @param text 报表名称
     * @param value 报表id
     * 
     */
    @SuppressWarnings("unchecked")
    public void addTable(ExtendAreaType extendAreaType, String text, String value) {
        Map<String, String> tableVo = new HashMap<String, String>();
        tableVo.put("text", text);
        tableVo.put("value", value);
        Map<String, Object> typeMap = this.findTableMapByExendAreaType(extendAreaType);
        ((List<Map<String, String>>) typeMap.get("list")).add(tableVo);
    }

    /**
     * 根据ExtendType类型获取Map
     *
     * @param extendAreaType
     * @return
     */
    private Map<String, Object> findTableMapByExendAreaType(ExtendAreaType extendAreaType) {
        for (Map<String, Object> map : this.tableList) {
            if (extendAreaType.toString().equals(map.get("type"))) {
                return map;
            }
        }
        Map<String, Object> map = new HashMap<String, Object>();
        this.tableList.add(map);
        map.put("type", extendAreaType.toString());
        map.put("name", extendAreaType.getName());
        map.put("list", Lists.newArrayList());
        return map;
    }

    /**
     * 为colunmDefine增加实体
     * 
     * @param text 多维报表列名称
     * @param value 多维报表列标识
     * @param selectedTable 已经被选中要跳转的平面报表id
     */
    public void addColunmDefine(ExtendAreaType extendAreaType, String text, String value, String selectedTable) {
        Map<String, String> colunmDefineMap = new HashMap<String, String>();
        colunmDefineMap.put("text", text);
        colunmDefineMap.put("value", value);
        colunmDefineMap.put("selectedType",
                extendAreaType == null ? "" : extendAreaType.toString());
        colunmDefineMap.put("selectedTable", selectedTable);
        columnDefine.add(colunmDefineMap);
    }

    /**
     * 为OperationColumn增加实体
     * @param text 操作列名称
     * @param value 操作列id标识
     * @param selectedTable 被选中要跳转的平面报表id
     */
    public void addOperationColumn(ExtendAreaType extendAreaType, String text, String value, String selectedTable) {
        Map<String, String> operationColumnMap = new HashMap<String, String>();
        operationColumnMap.put("text", text);
        operationColumnMap.put("value", value);
        operationColumnMap.put("selectedType",
                extendAreaType == null ? "" : extendAreaType.toString());
        operationColumnMap.put("selectedTable", selectedTable);
        operationColumn.add(operationColumnMap);
    }

    /**
     * default generate get tableList
     * @return the tableList
     */
    public List<Map<String, Object>> getTableList() {
        return tableList;
    }

    /**
     * default generate set tableList
     * @param tableList the tableList to set
     */
    public void setTableList(List<Map<String, Object>> tableList) {
        this.tableList = tableList;
    }

    /**
     * @return the columnDefine
     */
    public List<Map<String, String>> getColumnDefine() {
        return columnDefine;
    }

    /**
     * @param columnDefine the columnDefine to set
     */
    public void setColumnDefine(List<Map<String, String>> columnDefine) {
        this.columnDefine = columnDefine;
    }

    /**
     * @return the operationColumn
     */
    public List<Map<String, String>> getOperationColumn() {
        return operationColumn;
    }

    /**
     * @param operationColumn the operationColumn to set
     */
    public void setOperationColumn(List<Map<String, String>> operationColumn) {
        this.operationColumn = operationColumn;
    }

}
