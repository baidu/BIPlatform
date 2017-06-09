package com.baidu.rigel.biplatform.ma.auth.bo;

import java.io.Serializable;

public class ReportDesignModelBo implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7507257997737471587L;
    
    /**
     * id 无业务含义
     */
    private String id;
    
    /**
     * runTimeId
     */
    private String runTimeId;
    
    /**
     * 引用的数据源id
     */
    private String dsId;
    
    /**
     * 报表名称
     */
    private String name;
    
    /**
     * 主题
     */
    private String theme;
    
    /**
     * token
     */
    private String token;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the runTimeId
     */
    public String getRunTimeId() {
        return runTimeId;
    }

    /**
     * @param runTimeId the runTimeId to set
     */
    public void setRunTimeId(String runTimeId) {
        this.runTimeId = runTimeId;
    }

    /**
     * @return the dsId
     */
    public String getDsId() {
        return dsId;
    }

    /**
     * @param dsId the dsId to set
     */
    public void setDsId(String dsId) {
        this.dsId = dsId;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the theme
     */
    public String getTheme() {
        return theme;
    }

    /**
     * @param theme the theme to set
     */
    public void setTheme(String theme) {
        this.theme = theme;
    }

    /**
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * @param token the token to set
     */
    public void setToken(String token) {
        this.token = token;
    }
    
    
}
