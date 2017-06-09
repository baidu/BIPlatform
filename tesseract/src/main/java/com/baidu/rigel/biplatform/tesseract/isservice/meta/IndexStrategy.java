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
package com.baidu.rigel.biplatform.tesseract.isservice.meta;

import java.util.List;

/**
 * IndexStrategy
 * @author lijin
 *
 */
public class IndexStrategy {
    /**
     * sqlQueryList
     */
    private List<SqlQuery> sqlQueryList;
    /**
     * idxMeta
     */
    private IndexMeta idxMeta;
    
    /**
     * IndexStrategy 构造函数
     * @param sqlQueryList sqlQueryList
     */
    public IndexStrategy(List<SqlQuery> sqlQueryList) {
        this.sqlQueryList = sqlQueryList;
        idxMeta = new IndexMeta();
    }

    /**
     * getter method for property sqlQueryList
     * @return the sqlQueryList
     */
    public List<SqlQuery> getSqlQueryList() {
        return sqlQueryList;
    }

    /**
     * setter method for property sqlQueryList
     * @param sqlQueryList the sqlQueryList to set
     */
    public void setSqlQueryList(List<SqlQuery> sqlQueryList) {
        this.sqlQueryList = sqlQueryList;
    }

    /**
     * getter method for property idxMeta
     * @return the idxMeta
     */
    public IndexMeta getIdxMeta() {
        if (idxMeta == null) {
            this.idxMeta = new IndexMeta();
        }
        return idxMeta;
    }

    /**
     * setter method for property idxMeta
     * @param idxMeta the idxMeta to set
     */
    public void setIdxMeta(IndexMeta idxMeta) {
        this.idxMeta = idxMeta;
    }
    
    
    
    
    
}
