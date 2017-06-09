package com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;

/**
 * 
 * Description: where
 * @author 罗文磊
 *
 */
public class Where extends SqlSegment {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 4324971251933970062L;
    
    /**
     * signleWhereList
     */
    private List<SingleWhere> signleWhereList = Lists.newArrayList();

    /**
     * values
     */
    private List<Object> values = Lists.newArrayList();
    
    /**
     * 是否生成PrepareSql
     */
    private boolean isGeneratePrepareSql = true;
    
    /**
     * 是否生成where 1=1 语句
     */
    private boolean whereTrueEnabled = true;

    /**
     * default generate get isGeneratePrepareSql
     * @return the isGeneratePrepareSql
     */
    public boolean isGeneratePrepareSql() {
        return isGeneratePrepareSql;
    }

    /**
     * default generate set isGeneratePrepareSql
     * @param isGeneratePrepareSql the isGeneratePrepareSql to set
     */
    public void setGeneratePrepareSql(boolean isGeneratePrepareSql) {
        this.isGeneratePrepareSql = isGeneratePrepareSql;
    }

    /**
     * getValues
     * 
     * @return the values
     */
    public List<Object> getValues() {
        return values;
    }

    /**
     * setValues
     * 
     * @param values the values to set
     */
    public void setValues(List<Object> values) {
        this.values = values;
    }

    /* (non-Javadoc)
     * @see com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.SqlSegment#getSql()
     */
    @Override
    public String getSql() {
        this.setSql("");
        this.getValues().clear();
        
        String sql = SqlConstants.WHERE_TRUE;
        
        for (SingleWhere singleWhere : signleWhereList) {
            String singleWhereSql = singleWhere.getSql();
            if (StringUtils.isNotEmpty(singleWhereSql)) {
                if (singleWhere instanceof SourceSingleWhere) {
                    this.getValues().addAll(singleWhere.getValues());
                } else {
                    if (singleWhereSql.equals(SqlConstants.SQL_TRUE)
                            && !whereTrueEnabled) {
                        continue;
                    }
                    sql = sql + SqlConstants.AND + singleWhereSql;
                    this.getValues().addAll(singleWhere.getValues());
                }
            }
        }
        // 去掉 where 1=1 语句
        if (sql.equals(SqlConstants.WHERE_TRUE)) {
            sql = "";
        } else {
            sql = StringUtils.replace(sql, SqlConstants.SQL_TRUE + SqlConstants.AND,
                    SqlConstants.SPACE);
        }
        this.setSql(sql);
        if (isGeneratePrepareSql) {
            return this.getFormatSql(super.getSql());
        } else {
            return this.getFormatSql(toPrintSql(super.getSql()));
        }

    }
    
    /**
     * toPrintSql
     * 
     * @return sql String
     */
    public String toPrintSql(String sql) {

        List<Object> objects = this.getValues();
        String printSql = new String(sql);
        if (!StringUtils.isEmpty(printSql)) {
            for (Object value : objects) {
                if (value instanceof String) {
                    printSql = StringUtils.replaceOnce(printSql, "?", "'"
                            + value.toString() + "'");
                } else {
                    printSql = StringUtils.replaceOnce(printSql, "?",
                            value.toString());
                }
            }
            return printSql;
        } else {
            return "";
        }
    }

    /**
     * 添加一个事实表中的where
     */
    public void addSourceWhere(SqlColumn sqlColumn, boolean hasAlias) {
        this.signleWhereList.add(new SourceSingleWhere(sqlColumn, hasAlias));
    }
    
    /**
     * 添加一个sql的总体where
     */
    public void addTotalWhere(SqlColumn sqlColumn, boolean hasAlias) {
        this.signleWhereList.add(new SingleWhere(sqlColumn, hasAlias));
    }
    
    /**
     * 添加一个sqlwhere
     */
    public void addWhere(SingleWhere singleWhere) {
        this.signleWhereList.add(singleWhere);
    }
    
    /**
     * isEmpty
     *
     * @return
     */
    public boolean isEmpty() {
        return this.signleWhereList.isEmpty();
    }

    /* (non-Javadoc)
     * @see com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.SqlSegment#clear()
     */
    @Override
    public void clear() {
        this.signleWhereList.clear();
        this.values.clear();
        super.clear();
    }

    /**
     * @param hasAlias
     */
    public Where(boolean hasAlias) {
        super(hasAlias);
    }

    /**
     * Where
     */
    public Where() {
        super();
    }
}