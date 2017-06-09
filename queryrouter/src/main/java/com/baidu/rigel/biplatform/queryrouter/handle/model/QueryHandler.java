package com.baidu.rigel.biplatform.queryrouter.handle.model;

import com.baidu.rigel.biplatform.queryrouter.queryplugin.service.JdbcHandler;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.SqlExpression;

/**
 * 类QueryRequest.java的实现描述：Sql query request 类实现描述 
 * @author luowenlei 2015年9月22日 下午6:11:06
 */
public class QueryHandler {
    
    /**
     * sqlExpression
     */
    public SqlExpression sqlExpression;
    
    /**
     * queryHandler
     */
    public JdbcHandler jdbcHandler;

    public QueryHandler(SqlExpression sqlExpression, JdbcHandler jdbcHandler) {
        this.sqlExpression = sqlExpression;
        this.jdbcHandler = jdbcHandler;
    }

    /**
     * default generate get sqlExpression
     * @return the sqlExpression
     */
    public SqlExpression getSqlExpression() {
        return sqlExpression;
    }

    /**
     * default generate set sqlExpression
     * @param sqlExpression the sqlExpression to set
     */
    public void setSqlExpression(SqlExpression sqlExpression) {
        this.sqlExpression = sqlExpression;
    }
    /**
     * default generate get queryHandler
     * @return the queryHandler
     */
    public JdbcHandler getJdbcHandler() {
        return jdbcHandler;
    }
}
