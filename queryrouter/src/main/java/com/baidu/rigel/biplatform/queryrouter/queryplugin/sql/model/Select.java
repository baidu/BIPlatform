package com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model;

import java.util.List;

import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.model.Aggregator;
import com.baidu.rigel.biplatform.queryrouter.calculate.operator.utils.OperatorUtils;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.SqlExpression;
import com.google.common.collect.Lists;

/**
 * sql Select
 * 
 * @author luowenlei
 *
 */
public class Select extends SqlSegment {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -1938118460092726099L;
    
    /**
     * selectList
     */
    private List<SqlColumn> selectList = Lists.newArrayList();
    
    /**
     * aggSelectList
     */
    private List<SqlColumn> aggSelectList = Lists.newArrayList();
    
    /**
     * isDistinct
     */
    private boolean isDistinct = false;
    
    /**
     * getSelectList
     * 
     * @return the selectList
     */
    public List<SqlColumn> getSelectList() {
        return selectList;
    }
    
    /**
     * setSelectList
     * 
     * @param selectList
     *            the selectList to set
     */
    public void setSelectList(List<SqlColumn> selectList) {
        this.aggSelectList.clear();
        for (SqlColumn sqlColumn : selectList) {
            if (OperatorUtils.isAggSqlColumn(sqlColumn)) {
                this.aggSelectList.add(sqlColumn);
            }
        }
        this.selectList = selectList;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.SqlSegment
     * #getSql()
     */
    @Override
    public String getSql() {
        if (selectList.isEmpty()) {
            return "";
        }
        if (!StringUtils.isEmpty(super.getSql())) {
            return super.getSql();
        }
        String select = "select ";
        if (this.isDistinct) {
            select = select + " distinct ";
        }
        return this.getFormatSql(select + this.generateSelectCause());
    }
    
    /**
     * generateSelectCause
     *
     * @return
     */
    public String generateSelectCause() {
        StringBuffer select = new StringBuffer();
        if (selectList.isEmpty()) {
            return "";
        }
        for (SqlColumn colum : selectList) {
            if (OperatorUtils.isAggSqlColumn(colum)) {
                // 需要聚合
                if (colum.getOperator().getAggregator() == Aggregator.DISTINCT_COUNT) {
                    select.append(Aggregator.COUNT.name() + "(distinct "
                            + SqlExpression.getSqlColumnName(colum, this.isHasAlias()) + ") as "
                            + colum.getSqlUniqueColumn() + SqlConstants.COMMA);
                } else {
                    select.append(colum.getOperator().getAggregator().name() + "("
                            + SqlExpression.getSqlColumnName(colum, this.isHasAlias()) + ") as "
                            + colum.getSqlUniqueColumn() + SqlConstants.COMMA);
                }
            } else {
                if (this.isHasAlias()) {
                    select.append(SqlExpression.getSqlColumnName(colum, this.isHasAlias()) + " as "
                            + colum.getSqlUniqueColumn() + SqlConstants.COMMA);
                } else {
                    select.append(SqlExpression.getSqlColumnName(colum, this.isHasAlias())
                            + SqlConstants.COMMA);
                }
            }
        }
        String sql = select.toString().substring(0,
                select.toString().lastIndexOf(SqlConstants.COMMA))
                + SqlConstants.SPACE;
        return this.getFormatSql(sql);
    }
    
    public String getCountSql() {
        if (!StringUtils.isEmpty(super.getSql())) {
            return super.getSql();
        }
        return " select count(*) as totalc ";
    }
    
    /**
     * default generate get aggSelectList
     * 
     * @return the aggSelectList
     */
    public List<SqlColumn> getAggSelectList() {
        return aggSelectList;
    }
    
    /**
     * default generate set isDistinct
     * 
     * @param isDistinct
     *            the isDistinct to set
     */
    public void setDistinct(boolean isDistinct) {
        this.isDistinct = isDistinct;
    }
    
    public Select(boolean hasAlias) {
        super(hasAlias);
    }
}
