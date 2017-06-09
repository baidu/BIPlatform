
/**
 * Copyright (c) 2015 Baidu, Inc. All Rights Reserved.
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

package com.baidu.rigel.biplatform.ma.report.model;

import java.io.Serializable;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * 平面表样式
 * 
 * @author yichao.jiang
 * @version 2015年7月8日
 * @since jdk 1.8 or after
 */
public class PlaneTableFormat implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -5227900447556257392L;
    /**
     * 分页信息
     */
    private PaginationSetting pageSetting = new PaginationSetting();

    /**
     * 获取 pageSetting
     * 
     * @return the pageSetting
     */
    public PaginationSetting getPageSetting() {

        return pageSetting;
    }

    /**
     * 设置 pageSetting
     * 
     * @param pageSetting the pageSetting to set
     */
    public void setPageSetting(PaginationSetting pageSetting) {

        this.pageSetting = pageSetting;
    }

    /**
     * 平面分页设置信息
     * 
     * @author yichao.jiang
     * @version 2015年7月8日
     * @since jdk 1.8 or after
     */
    public class PaginationSetting implements Serializable {
        
        /**
         * serialVersionUID
         */
        private static final long serialVersionUID = -8498897428560699196L;

        /**
         * 是否分页
         */
        private boolean isPagination = true;

        /**
         * 每一页的记录数
         */
        private int pageSize = 1000;

        /**
         * 用户设置的每页大小
         */
        private Set<Integer> pageSizeOptions = Sets.newTreeSet();

        /**
         * 获取 isPagination
         * 
         * @return the isPagination
         */
        public boolean getIsPagination() {

            return isPagination;
        }

        /**
         * 设置 isPagination
         * 
         * @param isPagination the isPagination to set
         */
        public void setIsPagination(boolean isPagination) {

            this.isPagination = isPagination;
        }

        /**
         * 获取 pageSize
         * 
         * @return the pageSize
         */
        public int getPageSize() {

            return pageSize;
        }

        /**
         * 设置 pageSize
         * 
         * @param pageSize the pageSize to set
         */
        public void setPageSize(int pageSize) {

            this.pageSize = pageSize;
        }

        /**
         * 获取 pageSizeOptions
         * 
         * @return the pageSizeOptions
         */
        public Set<Integer> getPageSizeOptions() {

            return pageSizeOptions;
        }

        /**
         * 设置 pageSizeOptions
         * 
         * @param pageSizeOptions the pageSizeOptions to set
         */
        public void setPageSizeOptions(Set<Integer> pageSizeOptions) {

            this.pageSizeOptions = pageSizeOptions;
        }

        /**
         * 构造函数，默认设置10,50,100三个分页条数
         */
        public PaginationSetting() {
            pageSizeOptions.add(10);
            pageSizeOptions.add(50);
            pageSizeOptions.add(100);
        }
    }

}
