package com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model;

import java.util.List;

import com.baidu.rigel.biplatform.queryrouter.queryplugin.convert.SqlColumnUtils;
import com.google.common.collect.Lists;

/**
 * sql Select
 * 
 * @author luowenlei
 *
 */
public class GroupBy extends SqlSegment {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -1938118460092726099L;

    /**
     * groupByList
     */
    private List<SqlColumn> groupByList = Lists.newArrayList();

    /**
     * default generate get groupByList
     * @return the groupByList
     */
    public List<SqlColumn> getGroupByList() {
        return groupByList;
    }

    /**
     * default generate set groupByList
     * @param groupByList the groupByList to set
     */
    public void setGroupByList(List<SqlColumn> groupByList) {
        this.groupByList = groupByList;
    }

    /* (non-Javadoc)
     * @see com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.SqlSegment#getSql()
     */
    @Override
    public String getSql() {
        // TODO Auto-generated method stub
        if (groupByList.isEmpty()) {
            return "";
        }
        return this.getFormatSql(" group by " + this.generateGroupByCause());
    }

    public String generateGroupByCause() {
        if (groupByList.isEmpty()) {
            return "";
        }
        StringBuffer groupby = new StringBuffer();
        for (SqlColumn colum : groupByList) {
            if (ColumnType.JOIN == colum.getType()
                    && !SqlColumnUtils.isFacttableColumn(colum)) {
                // 如果为Join字段,join字段肯定需要hasalias
                groupby.append(colum.getTableName() + SqlConstants.DOT
                        + colum.getTableFieldName() + SqlConstants.COMMA);
            } else {
                // 如果为其他字段
                if (this.isHasAlias()) {
                    groupby.append(SqlConstants.SOURCE_TABLE_ALIAS_NAME + SqlConstants.DOT
                            + colum.getFactTableFieldName() + SqlConstants.COMMA);
                } else {
                    groupby.append(colum.getFactTableFieldName() + SqlConstants.COMMA);
                }
            }
        }
        return this.getFormatSql(groupby.toString().substring(0, groupby.toString().lastIndexOf(SqlConstants.COMMA))
                + SqlConstants.SPACE);
    }

    public GroupBy(boolean hasAlias) {
        super(hasAlias);
        // TODO Auto-generated constructor stub
    }
    
}
