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
package com.baidu.rigel.biplatform.ac.query.model;

import java.io.Serializable;

/**
 * 平面表问题模型
 * 
 * @author 罗文磊
 *
 */
public class SqlQuestionModel extends QuestionModel implements Serializable {
    
    /**
     * default generate id
     */
    private static final long serialVersionUID = 1576666141762101245L;
    
    /**
     * sql,查询 sql
     */
    private String sql;
    
    /**
     * 是否是异步查询
     */
    private boolean isAsyn = false;
    
    /**
     * default generate get sql
     * 
     * @return the sql
     */
    public String getSql() {
        return sql;
    }
    
    /**
     * default generate set sql
     * 
     * @param sql
     *            the sql to set
     */
    public void setSql(String sql) {
        this.sql = sql;
    }

    /**
     * default generate get isAsyn
     * @return the isAsyn
     */
    public boolean isAsyn() {
        return isAsyn;
    }

    /**
     * default generate set isAsyn
     * @param isAsyn the isAsyn to set
     */
    public void setAsyn(boolean isAsyn) {
        this.isAsyn = isAsyn;
    }
}