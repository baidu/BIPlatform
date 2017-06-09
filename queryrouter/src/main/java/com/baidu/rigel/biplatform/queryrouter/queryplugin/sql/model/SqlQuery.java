package com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.ac.query.model.PageInfo;
import com.baidu.rigel.biplatform.queryrouter.calculate.operator.utils.OperatorUtils;
import com.google.common.collect.Lists;

/**
 * sql select
 * 
 * @author luowenlei
 *
 */
public class SqlQuery implements Serializable {
    
    /**
     * logger
     */
    private Logger logger = LoggerFactory.getLogger(SqlQuery.class);
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5653873904654535901L;
    
    /**
     * select
     */
    private Select select;
    /**
     * from
     */
    private From from;
    /**
     * where
     */
    private Where where;
    /**
     * join
     */
    private Join join;
    
    /**
     * groupBy
     */
    private GroupBy groupBy;
    
    /**
     * orderBy
     */
    private OrderBy orderBy;
    /**
     * pageInfo
     */
    private PageInfo pageInfo;
    
    /**
     * driver
     */
    private String driver;
    
    /**
     * isAggSql
     *
     * @return
     */
    public boolean isAggSql() {
        if (this.select == null || CollectionUtils.isEmpty(this.select.getSelectList())) {
            return false;
        }
        for (SqlColumn sqlColumn : this.select.getSelectList()) {
            if (OperatorUtils.isAggSqlColumn(sqlColumn)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * toSql
     * 
     * @return
     */
    public String toSql() {
        StringBuffer sqlExpression = new StringBuffer();
        if (select == null || CollectionUtils.isEmpty(select.getSelectList())) {
            return "";
        }
        try {
            sqlExpression.append(select.getSql());
            sqlExpression.append(from.getSql());
            if (join != null) {
                sqlExpression.append(join.getSql());
            }
            sqlExpression.append(where.getSql());
            this.groupBy.getGroupByList().clear();
            this.groupBy.getGroupByList().addAll(
                    this.getGroupByColumns(this.select.getSelectList()));
            sqlExpression.append(groupBy.getSql());
            sqlExpression.append(orderBy.getSql());
            return this.getFormatSql(this.generatePageInfo(sqlExpression.toString()));
        } catch (Exception e) {
            logger.error("occur sql exception:{}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * getGroupByColumns
     *
     * @param needColums
     * @return
     */
    public List<SqlColumn> getGroupByColumns(List<SqlColumn> needColums) {
        List<SqlColumn> groupByColumns = Lists.newArrayList();
        if (!OperatorUtils.isAggQuery(needColums)) {
            return groupByColumns;
        }
        for (SqlColumn sqlColumn : needColums) {
            if (!OperatorUtils.isAggSqlColumn(sqlColumn)) {
                groupByColumns.add(sqlColumn);
            }
        }
        return groupByColumns;
    }
    
    /**
     * toCountSql
     * 
     * @return
     */
    public String toCountSql() {
        StringBuffer sqlExpression = new StringBuffer();
        if (select == null || CollectionUtils.isEmpty(select.getSelectList())) {
            return "";
        }
        try {
            sqlExpression.append(select.getCountSql());
            sqlExpression.append(from.getSql());
            sqlExpression.append(where.getSql());
        } catch (Exception e) {
            logger.error("occur sql exception:{}", e.getMessage());
            throw e;
        }
        return this.getFormatSql(sqlExpression.toString());
    }
    
    /**
     * 生成mysql sql语句
     * 
     * @param String
     *            finallySql
     * 
     */
    public String generatePageInfo(String finallySql) {
        if (pageInfo == null) {
            return finallySql;
        }
        StringBuffer limitStringBuffer = new StringBuffer();
        int start = 0;
        int size = -1;
        
        if (pageInfo.getCurrentPage() < 0) {
            pageInfo.setCurrentPage(0);
        }
        if (pageInfo.getPageSize() < 0) {
            pageInfo.setPageSize(0);
        }
        start = pageInfo.getCurrentPage() * pageInfo.getPageSize();
        size = pageInfo.getPageSize();
        switch (driver) {
            case "com.mysql.jdbc.Driver": {
                limitStringBuffer.append(" limit ");
                limitStringBuffer.append(start);
                limitStringBuffer.append(SqlConstants.COMMA);
                limitStringBuffer.append(size);
                return finallySql + limitStringBuffer.toString();
            }
            case "com.baidu.rigel.druid.sql.jdbc.DruidDriver": {
                limitStringBuffer.append(" limit ");
                limitStringBuffer.append(start);
                limitStringBuffer.append(SqlConstants.COMMA);
                limitStringBuffer.append(size);
                return finallySql + limitStringBuffer.toString();
            }
            case "Oracle.jdbc.driver.OracleDriver": {
                int end = -1;
                start = pageInfo.getCurrentPage() * pageInfo.getPageSize();
                end = start + pageInfo.getPageSize();
                StringBuffer pageString = new StringBuffer();
                pageString.append("SELECT * FROM (");
                pageString.append("SELECT A.*, ROWNUM RN");
                pageString.append(" FROM (SELECT * FROM ( " + finallySql + " )) A ");
                pageString.append(" WHERE ROWNUM <= " + end);
                pageString.append(") WHERE RN >=  " + start);
                return pageString.toString();
            }
            default: {
                return finallySql;
            }
        }
        
    }
    
    /**
     * toPrintSql
     * 
     * @return sql String
     */
    public String toPrintSql() {
        String sql = this.toSql();
        if (this.where == null || CollectionUtils.isEmpty(this.where.getValues())) {
            return sql;
        }
        List<Object> objects = this.where.getValues();
        String printSql = new String(sql);
        if (!StringUtils.isEmpty(printSql)) {
            for (Object value : objects) {
                if (value instanceof String) {
                    printSql = StringUtils.replaceOnce(printSql, "?", "'" + value.toString() + "'");
                } else {
                    printSql = StringUtils.replaceOnce(printSql, "?", value.toString());
                }
            }
            return printSql;
        } else {
            return "";
        }
    }
    
    /**
     * @return the driver
     */
    public String getDriver() {
        return driver;
    }
    
    /**
     * @param driver
     *            the driver to set
     */
    public void setDriver(String driver) {
        this.driver = driver;
    }
    
    /**
     * driver
     * 
     * @param driver
     */
    public SqlQuery(String driver, boolean hasAlias) {
        this.driver = driver;
        select = new Select(hasAlias);
        from = new From(hasAlias);
        where = new Where(hasAlias);
        join = new Join();
        groupBy = new GroupBy(hasAlias);
        orderBy = new OrderBy(hasAlias);
    }
    
    /**
     * getFormatSql
     *
     * @param sql
     * @return
     */
    public String getFormatSql(String sql) {
        sql = StringUtils.replace(sql, "  ", " ");
        sql = StringUtils.replace(sql, "  ", " ");
        return sql;
    }
    
    /**
     * getSelect
     * 
     * @return the select
     */
    public Select getSelect() {
        return select;
    }
    
    /**
     * setSelect
     * 
     * @param select
     *            the select to set
     */
    public void setSelect(Select select) {
        this.select = select;
    }
    
    /**
     * getFrom
     * 
     * @return the from
     */
    public From getFrom() {
        return from;
    }
    
    /**
     * setFrom
     * 
     * @param from
     *            the from to set
     */
    public void setFrom(From from) {
        this.from = from;
    }
    
    /**
     * default generate get groupBy
     * 
     * @return the groupBy
     */
    public GroupBy getGroupBy() {
        return groupBy;
    }
    
    /**
     * default generate set groupBy
     * 
     * @param groupBy
     *            the groupBy to set
     */
    public void setGroupBy(GroupBy groupBy) {
        this.groupBy = groupBy;
    }
    
    /**
     * getWhere
     * 
     * @return the where
     */
    public Where getWhere() {
        return where;
    }
    
    /**
     * setWhere
     * 
     * @param where
     *            the where to set
     */
    public void setWhere(Where where) {
        this.where = where;
    }
    
    /**
     * default generate get join
     * 
     * @return the join
     */
    public Join getJoin() {
        return join;
    }
    
    /**
     * default generate set join
     * 
     * @param join
     *            the join to set
     */
    public void setJoin(Join join) {
        this.join = join;
    }
    
    /**
     * getOrderBy
     * 
     * @return the orderBy
     */
    public OrderBy getOrderBy() {
        return orderBy;
    }
    
    /**
     * setOrderBy
     * 
     * @param orderBy
     *            the orderBy to set
     */
    public void setOrderBy(OrderBy orderBy) {
        this.orderBy = orderBy;
    }
    
    /**
     * getPageInfo
     * 
     * @return the pageInfo
     */
    public PageInfo getPageInfo() {
        return pageInfo;
    }
    
    /**
     * setPageInfo
     * 
     * @param pageInfo
     *            the pageInfo to set
     */
    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }
}
