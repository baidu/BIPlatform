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
/**
 * 
 */
package com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model;

import com.baidu.rigel.biplatform.ac.query.model.MetaCondition;
import com.baidu.rigel.biplatform.ac.query.model.SQLCondition;


/**
 * 指标条件
 * 
 * @author luowenlei
 *
 */
public class ColumnCondition implements MetaCondition {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5059328459757316603L;

    /**
     * metaName 元数据的name
     */
    private String metaName;

    /**
     * columnConditions 列条件
     */
    private SQLCondition columnConditions;
    
    /**
     * metaType
     */
    private MetaType metaType;

    /**
     * construct with metaUniqueName
     * 
     * @param metaName meta unique name
     */
    public ColumnCondition(String metaName) {
        this.metaName = metaName;
    }
    
    public ColumnCondition() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.ac.query.model.MetaCondition#getMetaUniqueName ()
     */
    @Override
    public String getMetaName() {
        return metaName;
    }

    /**
     * getter method for property columnConditions
     * 
     * @return the columnConditions
     */
    public SQLCondition getColumnConditions() {
        return columnConditions;
    }

    /**
     * setter method for property columnConditions
     * 
     * @param ColumnConditions the ColumnConditions to set
     */
    public void setColumnConditions(SQLCondition columnConditions) {
        this.columnConditions = columnConditions;
    }

    @Override
    public MetaType getMetaType() {
        return metaType;
    }   
}
