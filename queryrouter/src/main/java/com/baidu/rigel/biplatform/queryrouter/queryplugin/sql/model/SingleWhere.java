package com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.baidu.rigel.biplatform.ac.query.model.SQLCondition;
import com.baidu.rigel.biplatform.ac.query.model.SQLCondition.SQLConditionType;
import com.baidu.rigel.biplatform.ac.util.DataModelUtils;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.SqlExpression;
import com.google.common.collect.Lists;

/**
 * 
 * Description: where
 * 
 * @author 罗文磊
 *
 */
public class SingleWhere extends SqlSegment {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 4324971251933970062L;
    
    /**
     * sqlColumn
     */
    private SqlColumn sqlColumn;
    
    /**
     * where values
     */
    private List<Object> values = Lists.newArrayList();
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.SqlSegment
     * #getSql()
     */
    @Override
    public String getSql() {
        // clear values
        this.values.clear();
        
        if (sqlColumn == null || sqlColumn.getColumnCondition() == null
                || sqlColumn.getColumnCondition().getColumnConditions() == null) {
            return SqlConstants.WHERE_FALSE;
        }
        this.generateWhereEquals(sqlColumn.getColumnCondition().getColumnConditions(),
                sqlColumn.getDataType());
        if (SqlConstants.WHERE_FALSE.equals(super.getSql())) {
            return SqlConstants.WHERE_FALSE;
        } else if (StringUtils.isEmpty(super.getSql())) {
            return SqlConstants.SQL_TRUE;
        } else {
            return this.getFormatSql(SqlExpression.getSqlColumnName(sqlColumn, this.isHasAlias())
                    + super.getSql());
        }
    }
    
    /**
     * 获取where equals等式
     * 
     * @param sqlCondition
     *            sqlCondition
     * @return where equals sting
     */
    public void generateWhereEquals(SQLCondition sqlCondition, String dateType) {
        if (sqlCondition == null) {
            return;
        }
        for (String value : sqlCondition.getConditionValues()) {
            if (DataModelUtils.isChar(dateType)) {
                values.add(value);
            } else {
                try {
                    values.add(new BigDecimal(value));
                } catch (Exception e) {
                    this.setSql(SqlConstants.WHERE_FALSE);
                    return;
                }
            }
        }
        this.getSqlWhereEqualsStr(sqlCondition.getCondition());
    }
    
    /**
     * 生成one where
     * 
     * @param conditionType
     *            conditionType
     * @param values
     *            values
     * @return sql string
     */
    public void getSqlWhereEqualsStr(SQLConditionType conditionType) {
        if (CollectionUtils.isEmpty(values)) {
            return;
        }
        switch (conditionType) {
            // 等于
            case EQ: {
                this.setSql(" = " + SqlConstants.PARAM);
                break;
            }
            // 不等于
            case NOT_EQ: {
                this.setSql(" <> " + SqlConstants.PARAM);
                break;
            }
            // 小于
            case LT: {
                this.setSql(" < " + SqlConstants.PARAM);
                break;
            }
            // 小于等于
            case LT_EQ: {
                this.setSql(" <= " + SqlConstants.PARAM);
                break;
            }
            // 大于
            case GT: {
                this.setSql(" > " + SqlConstants.PARAM);
                break;
            }
            // 大于等于
            case GT_EQ: {
                this.setSql(" >= " + SqlConstants.PARAM);
                break;
            }
            // between and
            case BETWEEN_AND: {
                this.setSql(" between " + SqlConstants.PARAM + " and " + SqlConstants.PARAM);
                break;
            }
            // in
            case IN: {
                StringBuffer inExpression = new StringBuffer(" in (");
                for (int i = 0; i < values.size(); i++) {
                    inExpression.append(SqlConstants.PARAM + SqlConstants.COMMA);
                }
                this.setSql(inExpression.toString().substring(0,
                        inExpression.toString().lastIndexOf(SqlConstants.COMMA))
                        + ")");
                break;
            }
            // like
            case LIKE: {
                if (SqlConstants.LIKE_ALL.equals(values.get(0))) {
                    this.setSql("");
                    break;
                }
                this.setSql(" like " + SqlConstants.PARAM);
                break;
            }
            default: {
                this.setSql("");
            }
        }
    }
    
    /**
     * default generate get sqlColumn
     * 
     * @return the sqlColumn
     */
    public SqlColumn getSqlColumn() {
        return sqlColumn;
    }
    
    /**
     * default generate get values
     * 
     * @return the values
     */
    public List<Object> getValues() {
        return values;
    }
    
    /**
     * SingleWhere
     */
    public SingleWhere(SqlColumn sqlColumn, boolean hasAlias) {
        this.sqlColumn = sqlColumn;
        this.setHasAlias(hasAlias);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sqlColumn == null) ? 0 : sqlColumn.hashCode());
        result = prime * result + ((values == null) ? 0 : values.hashCode());
        return result;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SingleWhere other = (SingleWhere) obj;
        if (sqlColumn == null) {
            if (other.sqlColumn != null) {
                return false;
            }
        } else if (!sqlColumn.equals(other.sqlColumn)) {
            return false;
        }
        if (values == null) {
            if (other.values != null) {
                return false;
            }
        } else if (!values.equals(other.values)) {
            return false;
        }
        return true;
    }
}