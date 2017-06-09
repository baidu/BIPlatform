package com.baidu.rigel.biplatform.queryrouter.queryplugin.convert;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.ac.util.DataModelUtils;
import com.baidu.rigel.biplatform.queryrouter.handle.QueryRouterContext;
import com.baidu.rigel.biplatform.queryrouter.query.vo.QueryObject;
import com.baidu.rigel.biplatform.queryrouter.query.vo.QueryRequest;
import com.baidu.rigel.biplatform.queryrouter.query.vo.sql.Expression;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.QueryMeta;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.SqlColumn;
import com.google.common.collect.Lists;


/**
 * PlantTableUtils
 * 
 * @author luowenlei
 *
 */
public class WhereDataUtils {
    
    /**
     * logger
     */
    private static Logger logger = LoggerFactory.getLogger(WhereDataUtils.class);
    
    /**
     * 
     * transQueryRequestAndList2Map:analyze andList of queryRequest ,trans
     * andList into Map<String,List<String>>
     * 
     * @param query
     *            queryRequest
     * @return Map<String,List<String>> the result map,whose key is property and
     *         value is leafvalues
     */
    public static Map<String, List<Object>> transQueryRequestAndWhereList2Map(
            QueryRequest query, String tableName, QueryMeta queryMeta) {
        Map<String, List<Object>> resultMap = new HashMap<String, List<Object>>();
        for (Expression expression : query.getWhere().getAndList()) {
            String fieldName = expression.getProperties();
            if (StringUtils.isEmpty(fieldName) || StringUtils.isEmpty(tableName)) {
                continue;
            }
            SqlColumn sqlColumn = queryMeta.getSqlColumn(tableName, fieldName);
            boolean isChar = true;
            if (sqlColumn != null) {
                isChar = DataModelUtils.isChar(sqlColumn.getDataType());
            }
            List<Object> valueList = new ArrayList<Object>();
            for (QueryObject qo : expression.getQueryValues()) {
                if (qo == null) {
                    break;
                }
                for (String value : qo.getLeafValues()) {
                    if (StringUtils.isEmpty(sqlColumn.getDataType())) {
                        // by default
                        valueList.add(value);
                    } else if (isChar) {
                        valueList.add(value);
                    } else {
                        try {
                            valueList.add(new BigDecimal(value));
                        } catch (Exception e) {
                            logger.error(
                                    "queryId:{} sqlColumnName:{},sqlColumnDataType:{},"
                                            + " DataModelUtils.isChar(sqlColumn.getDataType()) == false,"
                                            + "but value is:{} ,can't convert BigDecimal, system will add where 1=2",
                                    QueryRouterContext.getQueryId(), sqlColumn.getName(),
                                    sqlColumn.getDataType(), value);
                            valueList = Lists.newArrayList();
                            break;
                        }
                    }
                }
            }
            if (resultMap.get(QueryMeta.getSqlColumnKey(tableName, fieldName)) != null) {
            // 如果有重复的条件选项，那么求 原先的where value与现有的valueList的where value的交集
                valueList.retainAll(resultMap.get(QueryMeta.getSqlColumnKey(tableName, fieldName)));
            }
            resultMap.put(QueryMeta.getSqlColumnKey(tableName, fieldName), valueList);
        }
        return resultMap;
    }
}
