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
package com.baidu.rigel.biplatform.ma.resource.view.dimdetail;

import java.io.Serializable;

/**
 * 
 * 用户自定义维度明细定义
 * @author zhongyi
 *
 *         2014-7-31
 */
public class CustDimDetail implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -6097980123647899132L;
    
    /**
     * 维度名称
     */
    private String dimName;
    
    /**
     * sql
     */
    private String sql;
    
    /**
     * get the dimName
     * 
     * @return the dimName
     */
    public String getDimName() {
        return dimName;
    }
    
    /**
     * set the dimName
     * 
     * @param dimName
     *            the dimName to set
     */
    public void setDimName(String dimName) {
        this.dimName = dimName;
    }
    
    /**
     * get the sql
     * 
     * @return the sql
     */
    public String getSql() {
        return sql;
    }
    
    /**
     * set the sql
     * 
     * @param sql
     *            the sql to set
     */
    public void setSql(String sql) {
        this.sql = sql;
    }
}