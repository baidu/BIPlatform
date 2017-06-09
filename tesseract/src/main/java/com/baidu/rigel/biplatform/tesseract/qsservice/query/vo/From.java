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
package com.baidu.rigel.biplatform.tesseract.qsservice.query.vo;

import java.io.Serializable;

/**
 * 查询来源
 * 
 * @author xiaoming.chen
 *
 */
public class From implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 7346888853127969225L;

    /**
     * from 来源
     */
    private String from;

    /**
     * getter method for property from
     * 
     * @return the from
     */
    public String getFrom() {
        return from;
    }

    /**
     * setter method for property from
     * 
     * @param from the from to set
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * construct with
     * 
     * @param from
     */
    public From(String from) {
        this.from = from;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return from;
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
        result = prime * result + ((from == null) ? 0 : from.hashCode());
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
        From other = (From) obj;
        if (from == null) {
            if (other.from != null) {
                return false;
            }
        } else if (!from.equals(other.from)) {
            return false;
        }
        return true;
    }

}
