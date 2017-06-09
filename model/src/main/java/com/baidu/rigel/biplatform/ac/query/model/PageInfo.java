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
package com.baidu.rigel.biplatform.ac.query.model;

import java.io.Serializable;

/**
 * 分页信息
 * @author xiaoming.chen
 *
 */
public class PageInfo implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -7767822498708728360L;
    
    /**
     * DEFAULT_PAGE_SIZE 默认每页记录数
     */
    public static final int DEFAULT_PAGE_SIZE = 500;
    
    /**
     * totalRecordCount 总记录数
     */
    private int totalRecordCount;
    
    /**
     * currentPage 当前第几页
     */
    private int currentPage;
    
    /**
     * totalPage 总页数
     */
    private int totalPage;
    
    /**
     * pageSize 每页的记录数
     */
    private int pageSize = PageInfo.DEFAULT_PAGE_SIZE;

    /**
     * getter method for property totalRecordCount
     * @return the totalRecordCount
     */
    public int getTotalRecordCount() {
        return totalRecordCount;
    }

    /**
     * setter method for property totalRecordCount
     * @param totalRecordCount the totalRecordCount to set
     */
    public void setTotalRecordCount(int totalRecordCount) {
        this.totalRecordCount = totalRecordCount;
    }

    /**
     * getter method for property currentPage
     * @return the currentPage
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * setter method for property currentPage
     * @param currentPage the currentPage to set
     */
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    /**
     * getter method for property totalPage
     * @return the totalPage
     */
    public int getTotalPage() {
        return totalPage;
    }

    /**
     * setter method for property totalPage
     * @param totalPage the totalPage to set
     */
    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    /**
     * getter method for property pageSize
     * @return the pageSize
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * setter method for property pageSize
     * @param pageSize the pageSize to set
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "PageInfo [totalRecordCount=" + totalRecordCount + ", currentPage=" + currentPage + ", totalPage="
            + totalPage + ", pageSize=" + pageSize + "]";
    }
    
}
