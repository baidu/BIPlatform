package com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

/**
 * sql Join
 * 
 * @author luowenlei
 *
 */
public class Join extends SqlSegment {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -85610952681471460L;
    
    /**
     * joinTables,多个表的join关联
     */
    private List<JoinTable> joinTables = new ArrayList<JoinTable>();

    
    /* (non-Javadoc)
     * @see com.baidu.rigel.biplatform.queryrouter.queryplugin.plugins.model.SqlSegment#getSql()
     */
    @Override
    public String getSql() {
        if (CollectionUtils.isEmpty(joinTables)) {
            return "";
        }
        StringBuffer sql = new StringBuffer();
        for (JoinTable joinTables : joinTables) {
            sql.append(joinTables.getSql());
        }
        return this.getFormatSql(sql.toString());
    }

    /**
     * default generate get joinTables
     * @return the joinTables
     */
    public List<JoinTable> getJoinTables() {
        return joinTables;
    }

    /**
     * default generate set joinTables
     * @param joinTables the joinTables to set
     */
    public void setJoinTables(List<JoinTable> joinTables) {
        this.joinTables = joinTables;
    }
}
