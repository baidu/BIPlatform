/**
 * Copyright (c) 2014 Baidu, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model;

import java.io.Serializable;
import java.util.List;

import com.baidu.rigel.biplatform.ac.query.model.SortRecord;
import com.baidu.rigel.biplatform.queryrouter.calculate.operator.model.Operator;

/**
 * 
 * Description: sql数据表列元数据信息描述
 * 
 * @author 罗文磊
 *
 */
/**
 * 类SqlColumn.java的实现描述：TODO 类实现描述 
 * @author luowenlei 2015年11月30日 下午2:41:11
 */
public class SqlColumn implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 3151301875582323397L;
    
    /**
     * name
     */
    private String name;
    
    /**
     * operator
     */
    private Operator operator;
    
    /**
     * columnKey
     */
    public String columnKey;
    
    /**
     * type
     */
    public ColumnType type;
    
    /**
     * dataType
     */
    public String dataType;
    
    /**
     * sql查询的唯一的列名标示
     */
    public String sqlUniqueColumn;
    
    /**
     * tableFieldName
     */
    public String tableFieldName;
    
    /**
     * factTableFieldName
     */
    public String factTableFieldName;
    
    /**
     * caption
     */
    public String caption;
    
    /**
     * tableName
     */
    public String tableName;
    
    /**
     * tableNames
     */
    public String sourceTableName;
    
    /**
     * columnCondition
     */
    public ColumnCondition columnCondition;
    
    /**
     * join
     */
    private JoinTable joinTable;
    
    /**
     * group维度时，存放allcolumn key
     */
    private List<String> keys;
    
    /**
     * sortRecord
     */
    private SortRecord sortRecord;
    
    /**
     * default generate get sortRecord
     * @return the sortRecord
     */
    public SortRecord getSortRecord() {
        return sortRecord;
    }

    /**
     * default generate set sortRecord
     * @param sortRecord the sortRecord to set
     */
    public void setSortRecord(SortRecord sortRecord) {
        this.sortRecord = sortRecord;
    }

    /**
     * 是否是关联字段
     */
    private boolean isRelatedColumn = false;

    /**
     * default generate get keys
     * @return the keys
     */
    public List<String> getKeys() {
        return keys;
    }

    /**
     * default generate set keys
     * @param keys the keys to set
     */
    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    /**
     * default generate get isRelatedColumn
     * @return the isRelatedColumn
     */
    public boolean isRelatedColumn() {
        return isRelatedColumn;
    }

    /**
     * default generate set isRelatedColumn
     * @param isRelatedColumn the isRelatedColumn to set
     */
    public void setRelatedColumn(boolean isRelatedColumn) {
        this.isRelatedColumn = isRelatedColumn;
    }

    /**
     * default generate get joinTable
     * 
     * @return the joinTable
     */
    public JoinTable getJoinTable() {
        return joinTable;
    }
    
    /**
     * default generate set joinTable
     * 
     * @param joinTable
     *            the joinTable to set
     */
    public void setJoinTable(JoinTable joinTable) {
        this.joinTable = joinTable;
    }
    
    /**
     * getDataType
     * 
     * @return the dataType
     */
    public String getDataType() {
        return dataType;
    }
    
    /**
     * setDataType
     * 
     * @param dataType
     *            the dataType to set
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    
    /**
     * getFactTableFieldName
     * 
     * @return the factTableFieldName
     */
    public String getFactTableFieldName() {
        return factTableFieldName;
    }
    
    /**
     * setFactTableFieldName
     * 
     * @param factTableFieldName
     *            the factTableFieldName to set
     */
    public void setFactTableFieldName(String factTableFieldName) {
        this.factTableFieldName = factTableFieldName;
    }
 
    /**
     * getType
     * 
     * @return the type
     */
    public ColumnType getType() {
        return type;
    }
    
    /**
     * setType
     * 
     * @param type
     *            the type to set
     */
    public void setType(ColumnType type) {
        this.type = type;
    }
    
    /**
     * columnCondition
     * 
     * @return the columnCondition to get
     */
    public ColumnCondition getColumnCondition() {
        return columnCondition;
    }
    
    /**
     * setColumnCondition
     * 
     * @param columnCondition
     *            the columnCondition to set
     */
    public void setColumnCondition(ColumnCondition columnCondition) {
        this.columnCondition = columnCondition;
    }
    
    /**
     * getSourceTableName
     * 
     * @return the sourceTableName to get
     */
    public String getSourceTableName() {
        return sourceTableName;
    }
    
    /**
     * setSourceTableName
     * 
     * @param sourceTableName
     *            the sourceTableName to set
     */
    public void setSourceTableName(String sourceTableName) {
        this.sourceTableName = sourceTableName;
    }
    
    /**
     * getColumnKey
     * 
     * @return the columnKey
     */
    public String getColumnKey() {
        return columnKey;
    }
    
    /**
     * setColumnKey
     * 
     * @param columnKey
     *            the columnKey to set
     */
    public void setColumnKey(String columnKey) {
        this.columnKey = columnKey;
    }
    
    /**
     * getSqlUniqueColumn
     * 
     * @return String sqlUniqueColumn
     */
    public String getSqlUniqueColumn() {
        return sqlUniqueColumn;
    }
    
//    /**
//     * default generate get isFactTableJoinField
//     * @return the isFactTableJoinField
//     */
//    public boolean isFactTableJoinField() {
//        return isFactTableJoinField;
//    }
//
//    /**
//     * default generate set isFactTableJoinField
//     * @param isFactTableJoinField the isFactTableJoinField to set
//     */
//    public void setFactTableJoinField(boolean isFactTableJoinField) {
//        this.isFactTableJoinField = isFactTableJoinField;
//    }

    /**
     * setSqlUniqueColumn
     * 
     * @param String
     *            sqlUniqueColumn
     */
    public void setSqlUniqueColumn(String sqlUniqueColumn) {
        this.sqlUniqueColumn = sqlUniqueColumn;
    }
    
    /**
     * getTableFieldName
     * 
     * @return tableFieldName tableFieldName
     */
    public String getTableFieldName() {
        return tableFieldName;
    }
    
    /**
     * setTableFieldName
     * 
     * @param tableFieldName
     *            tableFieldName
     */
    public void setTableFieldName(String tableFieldName) {
        this.tableFieldName = tableFieldName;
    }
    
    /**
     * getCaption
     * 
     * @return caption caption
     */
    public String getCaption() {
        return caption;
    }
    
    /**
     * setCaption
     * 
     * @param caption
     *            caption
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }
    
    /**
     * getTableName
     * 
     * @return tableName tableName
     */
    public String getTableName() {
        return tableName;
    }
    
    /**
     * setTableName
     * 
     * @param tableName
     *            tableName
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    

    /**
     * default generate get name
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * default generate set name
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * default generate get operator
     * @return the operator
     */
    public Operator getOperator() {
        return operator;
    }

    /**
     * default generate set operator
     * @param operator the operator to set
     */
    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((caption == null) ? 0 : caption.hashCode());
        result = prime * result + ((columnCondition == null) ? 0 : columnCondition.hashCode());
        result = prime * result + ((columnKey == null) ? 0 : columnKey.hashCode());
        result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
        result = prime * result
                + ((factTableFieldName == null) ? 0 : factTableFieldName.hashCode());
        result = prime * result + ((joinTable == null) ? 0 : joinTable.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((operator == null) ? 0 : operator.hashCode());
        result = prime * result + ((sourceTableName == null) ? 0 : sourceTableName.hashCode());
        result = prime * result + ((tableFieldName == null) ? 0 : tableFieldName.hashCode());
        result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SqlColumn other = (SqlColumn) obj;
        if (caption == null) {
            if (other.caption != null)
                return false;
        } else if (!caption.equals(other.caption))
            return false;
        if (columnCondition == null) {
            if (other.columnCondition != null)
                return false;
        } else if (!columnCondition.equals(other.columnCondition))
            return false;
        if (columnKey == null) {
            if (other.columnKey != null)
                return false;
        } else if (!columnKey.equals(other.columnKey))
            return false;
        if (dataType == null) {
            if (other.dataType != null)
                return false;
        } else if (!dataType.equals(other.dataType))
            return false;
        if (factTableFieldName == null) {
            if (other.factTableFieldName != null)
                return false;
        } else if (!factTableFieldName.equals(other.factTableFieldName))
            return false;
        if (joinTable == null) {
            if (other.joinTable != null)
                return false;
        } else if (!joinTable.equals(other.joinTable))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (operator == null) {
            if (other.operator != null)
                return false;
        } else if (!operator.equals(other.operator))
            return false;
        if (sourceTableName == null) {
            if (other.sourceTableName != null)
                return false;
        } else if (!sourceTableName.equals(other.sourceTableName))
            return false;
        if (tableFieldName == null) {
            if (other.tableFieldName != null)
                return false;
        } else if (!tableFieldName.equals(other.tableFieldName))
            return false;
        if (tableName == null) {
            if (other.tableName != null)
                return false;
        } else if (!tableName.equals(other.tableName))
            return false;
        if (type != other.type)
            return false;
        return true;
    }


}