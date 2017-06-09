package com.baidu.rigel.biplatform.queryrouter.calculate.operator.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.baidu.rigel.biplatform.ac.model.Aggregator;
import com.baidu.rigel.biplatform.queryrouter.calculate.operator.model.OperatorType;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.Column;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.ColumnType;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.SqlColumn;
import com.google.common.collect.Lists;

/**
 * 算子工具类
 * 
 * @author luowenlei
 *
 */
public class OperatorUtils {
    
    /**
     * aggList
     */
    private static Set<String> aggSet = new HashSet<String>();
    
    static {
        aggSet.add(Aggregator.SUM.name());
        aggSet.add(Aggregator.COUNT.name());
        aggSet.add(Aggregator.DISTINCT_COUNT.name());
    }
    
    /**
     * 获取判断此字段是否为AGG计算
     * 
     * @return OperatorType
     */
    public static OperatorType getOperatorType(Aggregator aggregator) {
        if (aggregator != null && aggSet.contains(aggregator.name())) {
            return OperatorType.AGG;
        } else {
            return OperatorType.OTHER;
        }
    }
    
    /**
     * 获取判断此查询是否有AGG计算
     * 
     * @return OperatorType
     */
    public static boolean isAggQuery(List<SqlColumn> needColumns) {
        if (CollectionUtils.isEmpty(needColumns)) {
            return false;
        }
        for (Object column : needColumns) {
            boolean isAggsql = false;
            isAggsql = isAggSqlColumn((SqlColumn) column);
            if (isAggsql) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取判断此查询是否有AGG计算
     * 
     * @return OperatorType
     */
    public static boolean isAggQuery(Map<String, ?> allColumns) {
        for (Object column : allColumns.values()) {
            boolean isAggsql = false;
            if (column instanceof Column) {
                isAggsql = isAggSqlColumn((Column) column);
            } else {
                isAggsql = isAggSqlColumn((SqlColumn) column);
            }
            if (isAggsql) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取判断此查询是为callback 指标
     * 
     * @return OperatorType
     */
    public static boolean isMeasureCallBackQuery(List<SqlColumn> needColumns) {
        if (CollectionUtils.isEmpty(needColumns)) {
            return false;
        }
        for (SqlColumn sqlColumn : needColumns) {
            if (sqlColumn.getType() == ColumnType.MEASURE_CALLBACK) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * findDistinctColumns
     * 
     * @return OperatorType
     */
    public static List<SqlColumn> findDistinctColumns(List<SqlColumn> sqlColumns) {
        List<SqlColumn> distSqlColumns = Lists.newArrayList();
        for (SqlColumn sqlColumn : sqlColumns) {
            if (isDistinctCountSqlColumn(sqlColumn)) {
                distSqlColumns.add(sqlColumn);
            }
        }
        return distSqlColumns;
    }
    
    /**
     * 获取判断此字段是否为AGG计算
     * 
     * @return OperatorType
     */
    public static boolean isAggSqlColumn(Column sqlColumn) {
        if (sqlColumn == null || sqlColumn.getOperator() == null) {
            return false;
        }
        if (OperatorUtils.getOperatorType(sqlColumn.getOperator().getAggregator()) == OperatorType.AGG) {
            return true;
        } else {
            return false;
        }
    }
    
    
    /**
     * 获取判断此字段是否为AGG计算
     * 
     * @return OperatorType
     */
    public static boolean isAggSqlColumn(SqlColumn sqlColumn) {
        if (sqlColumn == null || sqlColumn.getOperator() == null) {
            return false;
        }
        if (OperatorUtils.getOperatorType(sqlColumn.getOperator().getAggregator()) == OperatorType.AGG) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * 获取判断此字段是否为isDistinctCountSqlColumn
     * 
     * @return OperatorType
     */
    public static boolean isDistinctCountSqlColumn(SqlColumn sqlColumn) {
        if (sqlColumn == null || sqlColumn.getOperator() == null) {
            return false;
        }
        if (sqlColumn.getOperator().getAggregator() == Aggregator.DISTINCT_COUNT) {
            return true;
        } else {
            return false;
        }
    }
    
}
