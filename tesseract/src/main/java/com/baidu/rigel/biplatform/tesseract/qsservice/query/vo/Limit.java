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
 * 查询分页信息
 * 
 * @author xiaoming.chen
 *
 */
public class Limit implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -7971667628557956441L;

    /**
     * start 查询起始位置
     */
    private int start;

    /**
     * size 查询的记录数
     */
    private int size = -1;

    /**
     * construct with
     * 
     * @param size size
     */
    public Limit(int size) {
        this(0, size);
    }

    /**
     * 仅供内部调用 constructor
     */
    protected Limit() {
    }

    /**
     * construct with
     * 
     * @param start start
     * @param size size
     */
    public Limit(int start, int size) {
        this.start = start;
        this.size = size;
    }

    /**
     * getter method for property start
     * 
     * @return the start
     */
    public int getStart() {
        return start;
    }

    /**
     * setter method for property start
     * 
     * @param start the start to set
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * getter method for property size
     * 
     * @return the size
     */
    public int getSize() {
        return size;
    }

    /**
     * setter method for property size
     * 
     * @param size the size to set
     */
    public void setSize(int size) {
        this.size = size;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return start + ", " + size;
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
        result = prime * result + size;
        result = prime * result + start;
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
        Limit other = (Limit) obj;
        if (size != other.size) {
            return false;
        }
        if (start != other.start) {
            return false;
        }
        return true;
    }

}
