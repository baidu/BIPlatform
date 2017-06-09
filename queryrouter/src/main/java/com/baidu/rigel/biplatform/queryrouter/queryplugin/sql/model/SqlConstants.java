package com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model;

/**
 * sql 表达式字符串常量
 * @author luowenlei
 *
 */
public final class SqlConstants {

    /**
     * sql comma
     */
    public static final String COMMA = ",";

    /**
     * sql dot
     */
    public static final String DOT = ".";

    /**
     * sql space
     */
    public static final String SPACE = " ";
    
    /**
     * 参数问号
     */
    public static final String PARAM = "?";
    
    /**
     * 等于
     */
    public static final String EQUALS = "=";
    
    /**
     * LIKE_ALL
     */
    public static final String LIKE_ALL = "%";
    
    /**
     * DESC
     */
    public static final String DESC = "DESC";
    
    /**
     * sql and
     */
    public static final String AND = " and ";
    
    /**
     * sql join on
     */
    public static final String JOIN_ON = " on ";
    
    /**
     * WHERE_TRUE
     */
    public static final String WHERE_TRUE = " where 1 = 1 ";
    
    /**
     * WHERE_AND_FALSE
     */
    public static final String WHERE_FALSE = " 1 = 2 ";
    
    /**
     * SQL_TRUE
     */
    public static final String SQL_TRUE = " 1 = 1 ";

    /**
     * facttable alias name
     */
    public static final String SOURCE_TABLE_ALIAS_NAME = "biplatform_facttable_name";

    /**
     * MySql Driver
     */
    public static final String DRIVER_MYSQL = "com.mysql.jdbc.Driver";

    /**
     * Oracle Driver
     */
    public static final String DRIVER_ORACLE = "Oracle.jdbc.driver.OracleDriver";

    /**
     * 禁用new此类
     */
    private SqlConstants() {
        
    }
}
