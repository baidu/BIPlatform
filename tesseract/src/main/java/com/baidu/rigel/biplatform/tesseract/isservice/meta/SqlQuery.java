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
package com.baidu.rigel.biplatform.tesseract.isservice.meta;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import com.baidu.rigel.biplatform.ac.query.data.impl.SqlDataSourceInfo.DataBase;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.SqlSelectColumn;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.SqlSelectColumnType;

/**
 * SQLQuery
 * 
 * @author lijin
 *
 */
public class SqlQuery {
    
    /**
     * 组织sql的数据类型
     */
    public DataBase database = DataBase.MYSQL;
    
    /**
     * palo id
     */
    private static final String PALO_ID = "id";
    
    /**
     * agg DISTINCT_COUNT 算子
     */
    private static final String DISTINCT_COUNT = "DISTINCT_COUNT";
    
    /**
     * 关联的主表的别名
     */
    private static final String MAIN_TABLE_ALIAS = "mt";
    
    /**
     * agg SUM 算子
     */
    private static final String SUM = "SUM";
    
    /**
     * agg COUNT 算子
     */
    private static final String COUNT = "COUNT";

    /**
     * sql common agg operator
     */
    private static final Set<String> AGG_COMMON_OPERATOR = new HashSet<String>();

    /**
     * 查询字段
     */
    private List<String> selectList;

    /**
     * 查询字段详细信息
     */
    private HashMap<String, SqlSelectColumn> selectMap;

    private Map<String, String> sqlFunction = new HashMap<String, String>();

    public Map<String, String> getSqlFunction() {
        return sqlFunction;
    }

    public void setSqlFunction(Map<String, String> sqlFunction) {
        this.sqlFunction = sqlFunction;
    }

    /**
     * @return the aggcommonoperator
     */
    public static Set<String> getAggcommonoperator() {
        return AGG_COMMON_OPERATOR;
    }

    /**
     * isAggSql，默认为false，只有setisAggSql(ture)时测sqlQuery为agg的sql生成
     */
    private boolean isAggSql = false;

    /**
     * idName
     */
    private String idName;

    /**
     * 初始化的最大id;
     */
    private BigDecimal initMaxId;
    /**
     * 查询的表
     */
    private LinkedList<String> fromList;
    /**
     * where 条件 (所有的where都是and关系，其它关系，请写到where内部)
     */
    private List<String> whereList;

    /**
     * limit条件，key有两个：limitStart及limitEnd,其中limitStart默认为0
     */
    private Map<String, Long> limitMap;

    /**
     * LIMITMAP_KEY_LIMITSTART
     */
    private static final String LIMITMAP_KEY_LIMITSTART = "limitStart";
    /**
     * LIMITMAP_KEY_LIMITEND
     */
    private static final String LIMITMAP_KEY_LIMITEND = "limitEnd";

    /**
     * groupBy
     */
    private Set<String> groupBy;

    /**
     * orderBy
     */
    private Set<String> orderBy;

    /**
     * distinct
     */
    private boolean distinct = false;

    /**
     * @return the isAggSql
     */
    public boolean isAggSql() {
        return isAggSql;
    }

    /**
     * @param isAggSql
     *            the isAggSql to set
     */
    public void setAggSql(boolean isAggSql) {
        this.isAggSql = isAggSql;
    }

    /**
     * getter method for property selectList
     * 
     * @return the selectList
     */
    public List<String> getSelectList() {
        if (this.selectList == null) {
            this.selectList = new ArrayList<String>();
        }

        return selectList;
    }

    /**
     * @return the selectMap
     */
    public HashMap<String, SqlSelectColumn> getSelectMap() {
        if (selectMap == null) {
            selectMap = new HashMap<String, SqlSelectColumn>();
        }
        return selectMap;
    }

    /**
     * @return the selectMap
     */
    public SqlSelectColumn getSqlSelectColumn(String key) {
        if (this.getSelectMap().get(key) == null) {
            this.getSelectMap().put(key, new SqlSelectColumn(key));
        }
        return this.getSelectMap().get(key);
    }

    /**
     * setter method for property selectList
     * 
     * @param selectList
     *            the selectList to set
     */
    public void setSelectList(List<String> selectList) {
        this.selectList = selectList;
    }

    /**
     * getter method for property fromList
     * 
     * @return the fromList
     */
    public LinkedList<String> getFromList() {
        return fromList;
    }

    /**
     * setter method for property fromList
     * 
     * @param fromList
     *            the fromList to set
     */
    public void setFromList(LinkedList<String> fromList) {
        this.fromList = fromList;
    }

    /**
     * getter method for property whereList
     * 
     * @return the whereList
     */
    public List<String> getWhereList() {
        if (this.whereList == null) {
            this.whereList = new ArrayList<String>();
        }
        return whereList;
    }

    /**
     * setter method for property whereList
     * 
     * @param whereList
     *            the whereList to set
     */
    public void setWhereList(List<String> whereList) {
        this.whereList = whereList;
    }

    /**
     * getter method for property limitMap
     * 
     * @return the limitMap
     */
    public Map<String, Long> getLimitMap() {
        return limitMap;
    }

    /**
     * setter method for property limitMap
     * 
     * @param limitMap
     *            the limitMap to set
     */
    public void setLimitMap(Map<String, Long> limitMap) {
        this.limitMap = limitMap;
    }

    /**
     * setLimitMap
     * 
     * @param limitStart
     *            起始
     * @param limitEnd
     *            条数
     */
    public void setLimitMap(long limitStart, long limitEnd) {
        long start = limitStart;
        long end = limitEnd;
        if (start < 0) {
            start = 0;
        }
        if (this.limitMap == null) {
            this.limitMap = new HashMap<String, Long>();
        }
        this.limitMap.put(LIMITMAP_KEY_LIMITSTART, start);
        if (end > 0) {
            this.limitMap.put(LIMITMAP_KEY_LIMITEND, end);
        }
    }

    /**
     * @return the orderBy
     */
    public Set<String> getOrderBy() {
        if (CollectionUtils.isEmpty(orderBy)) {
            this.orderBy = new HashSet<String>();
        }
        return orderBy;
    }

    /**
     * @param orderBy
     *            the orderBy to set
     */
    public void setOrderBy(Set<String> orderBy) {
        this.orderBy = orderBy;
    }

    /**
     * 
     * 转换成SQL
     * 
     * @return String
     */
    public String toSql() {
        StringBuffer sb = new StringBuffer();
        if (CollectionUtils.isEmpty(this.selectList)) {
            return "";
        }
        // 处理select
        if (this.selectList != null) {
            sb.append("select ");
            if (this.distinct) {
                sb.append(" distinct ");
            }

            boolean needGroupBy = false;
            List<String> sqlDistinctCount = new ArrayList<String>();
            List<String> groupByNames = new ArrayList<String>();
            String selectExpresstion = "";
            for (int i = 0; i < selectList.size(); i++) {
                String select = selectList.get(i);
                if (isAggSql && StringUtils.isNotEmpty(selectMap.get(select).getOperator())) {
                // isAggSql为数据库完成agg的计算，true标示数据完成agg计算，false为数据库不完成agg的计算。
                    needGroupBy = true;
                    String selectTmp = selectMap.get(select).getOperator();
                    if (COUNT.equals(selectTmp) && DataBase.PALO == this.database) {
                        selectTmp = selectTmp + "( " + MAIN_TABLE_ALIAS + "." + PALO_ID + ") as " + select + ",";
                        selectExpresstion = selectExpresstion
                                + (sqlFunction.containsKey(select) ? sqlFunction
                                        .get(select) : selectTmp);
                    } else if (DISTINCT_COUNT.equals(selectTmp) && DataBase.PALO == this.database) {
                        selectTmp = "count( " + MAIN_TABLE_ALIAS + "." + select
                                + ") as " + select + ",";
                        selectExpresstion = selectExpresstion
                                + (sqlFunction.containsKey(select) ? sqlFunction
                                        .get(select) : selectTmp);
                    } else {
                        selectTmp = selectTmp + "( " + MAIN_TABLE_ALIAS + "." + select
                                + ") as " + select + ",";
                        selectExpresstion = selectExpresstion
                                + (sqlFunction.containsKey(select) ? sqlFunction
                                        .get(select) : selectTmp);
                    }
                } else {
                    groupByNames.add(select);
                    selectExpresstion = selectExpresstion + MAIN_TABLE_ALIAS
                            + "." + select + ",";
                }
            }
            if (StringUtils.isNotEmpty(selectExpresstion)) {
                sb.append(selectExpresstion.substring(0,
                        selectExpresstion.length() - 1));
            }
            if (this.fromList != null) {
                // 处理from
                sb.append(" from ");
                sb.append(fromList.get(0) + " " + MAIN_TABLE_ALIAS + " ");
            }

            sb.append(getWhere());

            if (CollectionUtils.isNotEmpty(groupByNames) && needGroupBy
                    && isAggSql) {
                sb.append(" group by ");
                String[] groupByArr = groupByNames.toArray(new String[0]);
                for (int i = 0; i < groupByArr.length; i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append(MAIN_TABLE_ALIAS + "." + groupByArr[i]);
                }
            }

            // 处理distinct count sql
            selectExpresstion = "";
            if (CollectionUtils.isNotEmpty(sqlDistinctCount)) {
                for (int i = 0; i < selectList.size(); i++) {
                    String select = selectList.get(i);
                    // SqlSelectColumnType.COMMON为非agg计算的普通select，OPERATOR_COMMON为非DISTINCT_COUNT agg计算的agg select，
                    if (selectMap.get(select).getSqlSelectColumnType() == SqlSelectColumnType.COMMON
                            || selectMap.get(select).getSqlSelectColumnType()
                                    == SqlSelectColumnType.OPERATOR_COMMON) {
                        selectExpresstion = selectExpresstion
                                + MAIN_TABLE_ALIAS + "." + select;
                    } else {
                        // DISTINCT_COUNT agg select，

                        selectExpresstion = selectExpresstion + select
                                + "table" + "." + select;
                    }
                    // 注意此处for里面如果有continue或break，请添加相应的判断逻辑
                    if (i < selectList.size() - 1) {
                        selectExpresstion = selectExpresstion + ",";
                    }
                }
                String tableAlias = sb.toString();
                if (!needGroupBy) {
                    tableAlias = StringUtils.replace(tableAlias, "select",
                            "select distinct");
                }
                String totalSql = "select " + selectExpresstion + " from ("
                        + tableAlias + ") " + MAIN_TABLE_ALIAS + " ";
                for (String dcSql : sqlDistinctCount) {
                    totalSql = totalSql + dcSql;
                }
                sb = new StringBuffer(totalSql);
            }

            if (CollectionUtils.isNotEmpty(this.orderBy)) {
                sb.append(" order by ");
                String[] orderByArr = this.orderBy.toArray(new String[0]);
                for (int i = 0; i < orderByArr.length; i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    if (MapUtils.isEmpty(selectMap)
                            || selectMap.get(orderByArr[i]).getSqlSelectColumnType() == SqlSelectColumnType.COMMON
                            || selectMap.get(orderByArr[i])
                                    .getSqlSelectColumnType() == SqlSelectColumnType.OPERATOR_COMMON) {
                        sb.append(MAIN_TABLE_ALIAS + "." + orderByArr[i]);
                    } else if (selectMap.get(orderByArr[i])
                            .getSqlSelectColumnType() == SqlSelectColumnType.OPERATOR_DISTINCT_COUNT) {
                        sb.append(orderByArr[i] + "table." + orderByArr[i]);
                    }
                }
            }

            if (this.limitMap != null) {
                // 添加limit
                StringBuffer limitStringBuffer = new StringBuffer();

                long limitStart = 0;
                if (this.limitMap.get(LIMITMAP_KEY_LIMITSTART) != null) {
                    limitStart = this.limitMap.get(LIMITMAP_KEY_LIMITSTART);
                }
                long limitEnd = 0;
                if (this.limitMap.get(LIMITMAP_KEY_LIMITEND) != null) {
                    limitEnd = this.limitMap.get(LIMITMAP_KEY_LIMITEND);
                }
                if (limitStart >= 0 && limitEnd > 0) {
                    limitStringBuffer.append(" limit ");
                    limitStringBuffer.append(limitStart);
                    limitStringBuffer.append(",");
                    limitStringBuffer.append(limitEnd);
                } else if (limitEnd > 0) {
                    limitStringBuffer.append(" limit ");
                    limitStringBuffer.append(limitEnd);
                }
                sb.append(limitStringBuffer);
            }
        }
        return sb.toString();
    }

    /**
     * 
     * 转换成DistinctCount Join Sql
     * 
     * @return String
     */
    public String generateDistinctCountJoinSql(String dcSelect) {
        StringBuffer sb = new StringBuffer(" left outer join ( ");
        // 处理select
        List<String> groupByNames = new ArrayList<String>();
        if (this.selectList != null) {
            String selectExpresstion = "select count(" + dcSelect + ") as "
                    + dcSelect + ",";
            for (int i = 0; i < selectList.size(); i++) {
                String select = selectList.get(i);
                if (selectMap.get(select).getSqlSelectColumnType() == SqlSelectColumnType.COMMON) {
                    selectExpresstion = selectExpresstion + select + ",";
                }
            }
            sb.append(selectExpresstion.substring(0,
                    selectExpresstion.length() - 1));

            if (this.fromList != null) {
                // 处理from
                selectExpresstion = "";
                sb.append(" from (");
                sb.append(" select distinct ");
                for (int i = 0; i < selectList.size(); i++) {
                    String select = selectList.get(i);
                    if (selectMap.get(select).getSqlSelectColumnType() == SqlSelectColumnType.COMMON) {
                        groupByNames.add(select);
                        selectExpresstion = selectExpresstion + select + ",";
                    } else if (selectMap.get(select).getSqlSelectColumnType()
                            == SqlSelectColumnType.OPERATOR_DISTINCT_COUNT) {
                        selectExpresstion = selectExpresstion + select + ",";
                    }
                }
                sb.append(selectExpresstion.substring(0,
                        selectExpresstion.length() - 1));
                sb.append(" from ");
                for (int i = 0; i < fromList.size(); i++) {
                    String from = fromList.get(i);
                    sb.append(" ");
                    sb.append(from);
                    if (i < fromList.size() - 1) {
                        sb.append(",");
                    }
                }
                sb.append(getWhere());
                sb.append(" ) t");
            }
            sb.append(" group by ");
            String[] groupByArr = groupByNames.toArray(new String[0]);
            for (int i = 0; i < groupByArr.length; i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(groupByArr[i]);
            }
        }
        sb.append(" ) " + dcSelect + "table");
        sb.append(" on 1 = 1 ");
        for (String name : groupByNames) {
            sb.append(" and " + dcSelect + "table." + name + " = "
                    + MAIN_TABLE_ALIAS + "." + name);
        }
        return sb.toString();
    }

    /**
     * getWhere expresstion
     * 
     * @return where expresstion
     */
    public String getWhere() {
        StringBuffer sb = new StringBuffer();
        if (CollectionUtils.isNotEmpty(this.whereList)) {
            sb.append(" where ");
            for (int i = 0; i < whereList.size(); i++) {
                String where = whereList.get(i);
                sb.append(" ");
                sb.append(where);
                if (i < whereList.size() - 1) {
                    sb.append(" and ");
                }
            }

        }
        return sb.toString();
    }

    /**
     * getter method for property idName
     * 
     * @return the idName
     */
    public String getIdName() {
        return idName;
    }

    /**
     * setter method for property idName
     * 
     * @param idName
     *            the idName to set
     */
    public void setIdName(String idName) {
        this.idName = idName;
    }

    /**
     * get groupBy
     * 
     * @return the groupBy
     */
    public Set<String> getGroupBy() {
        return groupBy;
    }

    /**
     * set groupBy with groupBy
     * 
     * @param groupBy
     *            the groupBy to set
     */
    public void setGroupBy(Set<String> groupBy) {
        this.groupBy = groupBy;
    }

    /**
     * @return the distinct
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * @param distinct
     *            the distinct to set
     */
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    /**
     * @return the initMaxId
     */
    public BigDecimal getInitMaxId() {
        return initMaxId;
    }

    /**
     * @param initMaxId
     *            the initMaxId to set
     */
    public void setInitMaxId(BigDecimal initMaxId) {
        this.initMaxId = initMaxId;
    }
    
    /**
     * default generate get database
     * @return the database
     */
    public DataBase getDatabase() {
        return database;
    }

    /**
     * default generate set database
     * @param database the database to set
     */
    public void setDatabase(DataBase database) {
        this.database = database;
    }

    /**
     * 构造方法
     */
    public SqlQuery() {
        // 添加sql标准agg
        AGG_COMMON_OPERATOR.add(COUNT);
        AGG_COMMON_OPERATOR.add(SUM);
    }
}