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
package com.baidu.rigel.biplatform.queryrouter.queryplugin.convert;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.query.data.TableData;
import com.baidu.rigel.biplatform.ac.query.data.TableData.Column;
import com.baidu.rigel.biplatform.ac.util.UnicodeUtils;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.ColumnType;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.SqlColumn;


/**
 * 
 * SQLDataQueryService的实现类
 * 
 * @author luowenlei
 *
 */
@Service("dataModelConvertService")
public class DataModelConvertService {

    /**
     * executeSql
     * 
     * @param questionModel
     *            questionModel
     * @param SqlExpression
     *            sqlExpression
     * @return DataModel DataModel
     */
    public DataModel convert(List<SqlColumn> needColums, List<Map<String, Object>> rowBasedList) {
        // init DataModel
        DataModel dataModel = getEmptyDataModel(needColums);
        // 设置DataModel的ColBased Data
        if (CollectionUtils.isEmpty(rowBasedList)) {
            return dataModel;
        } else {
            fillModelTableData(dataModel, needColums, rowBasedList);
            return dataModel;
        }
    }
    
    /**
     * getEmptyDataModel,初始化dataModel
     * 
     * @param List<SqlColumn> needColums 需要select的字段
     * @return DataModel DataModel
     */
    public DataModel getEmptyDataModel(List<SqlColumn> needColums) {
        DataModel dataModel = new DataModel();
        dataModel.setTableData(new TableData());
        dataModel.getTableData().setColumns(new ArrayList<Column>());
        dataModel.getTableData().setColBaseDatas(
                new HashMap<String, List<String>>());
        if (needColums == null || needColums.isEmpty()) {
            return dataModel;
        }
        // 必须为有序的column
        for (SqlColumn colDefine : needColums) {
            String tableName = "";
            tableName = colDefine.getTableName();
            if (ColumnType.COMMON == colDefine.getType()) {
                tableName = colDefine.getSourceTableName();
            }
            Column colum = new Column(colDefine.getColumnKey(),
                    colDefine.getName(), colDefine.getCaption(),
                    colDefine.getDataType(), tableName);
            dataModel.getTableData().getColumns().add(colum);
            dataModel
                    .getTableData()
                    .getColBaseDatas()
                    .put(colDefine.getColumnKey(),
                            new ArrayList<String>());
        }
        return dataModel;
    }

    /**
     * 将Rowbased数据集转成colbased的数据集
     * 
     * @param dataModel
     *            dataModel
     * @param rowBasedList
     *            rowBasedList
     */
    public void fillModelTableData(DataModel dataModel,
            List<SqlColumn> needColums, List<Map<String, Object>> rowBasedList) {
        if (dataModel == null || dataModel.getTableData() == null
                || dataModel.getTableData().getColBaseDatas().isEmpty()) {
            return ;
        }
        Map<String, List<String>> rowBaseData = dataModel.getTableData()
                .getColBaseDatas();
        rowBasedList.forEach((row) -> {
            for (SqlColumn column : needColums) {
                String tableDataColumnKey = column.getColumnKey();
                String cell = "";
                if (column.getSqlUniqueColumn() != null && row.get(column.getSqlUniqueColumn()) != null) {
                    Object obj = row.get(column.getSqlUniqueColumn());
                    if (obj instanceof BigDecimal) {
                        cell = ((BigDecimal) obj).toPlainString();
                    } else {
                        cell = obj.toString();
                    }
                }
                // get Data from
                List<String> oneColData = rowBaseData.get(tableDataColumnKey);
                oneColData.add(UnicodeUtils.string2Unicode(cell));
            }
        });
    }
}
