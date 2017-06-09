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
package com.baidu.rigel.biplatform.tesseract.isservice.event;

import java.io.Serializable;
import java.util.List;

import org.springframework.context.ApplicationEvent;

/**
 * IndexUpdateEvent
 * 
 * @author lijin
 *
 */
public class IndexUpdateEvent extends ApplicationEvent {
    
    /**
     * serialVersionUID long
     */
    private static final long serialVersionUID = 8112642973775344719L;
    /**
     * 更新的信息
     */
    private IndexUpdateInfo updateInfo;
    
    /**
     * IndexUpdateInfo
     * 
     * @author lijin
     *
     */
    public static class IndexUpdateInfo implements Serializable {
        /**
         * serialVersionUID long
         */
        private static final long serialVersionUID = -3323942151032877332L;
        
        /**
         * 这次更新的提供服务的索引路径
         */
        private List<String> idxServicePathList;
        /**
         * 这次更新的不提供服务的索引路径
         */
        private List<String> idxNoServicePathList;
        
        /**
         * Constructor by
         * 
         * @param idxServicePathList
         * @param idxNoServicePathList
         */
        public IndexUpdateInfo(List<String> idxServicePathList, List<String> idxNoServicePathList) {
            super();
            this.idxServicePathList = idxServicePathList;
            this.idxNoServicePathList = idxNoServicePathList;
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "IndexUpdateInfo [idxServicePathList=" + idxServicePathList
                + ", idxNoServicePathList=" + idxNoServicePathList + "]";
        }
        
        /**
         * getter method for property idxServicePathList
         * 
         * @return the idxServicePathList
         */
        public List<String> getIdxServicePathList() {
            return idxServicePathList;
        }
        
        /**
         * setter method for property idxServicePathList
         * 
         * @param idxServicePathList
         *            the idxServicePathList to set
         */
        public void setIdxServicePathList(List<String> idxServicePathList) {
            this.idxServicePathList = idxServicePathList;
        }
        
        /**
         * getter method for property idxNoServicePathList
         * 
         * @return the idxNoServicePathList
         */
        public List<String> getIdxNoServicePathList() {
            return idxNoServicePathList;
        }
        
        /**
         * setter method for property idxNoServicePathList
         * 
         * @param idxNoServicePathList
         *            the idxNoServicePathList to set
         */
        public void setIdxNoServicePathList(List<String> idxNoServicePathList) {
            this.idxNoServicePathList = idxNoServicePathList;
        }
        
    }
    
    /**
     * 
     * Constructor by
     * 
     * @param source
     *            source
     */
    public IndexUpdateEvent(Object source) {
        super(source);
        if (source instanceof IndexUpdateInfo) {
            this.updateInfo = (IndexUpdateInfo) source;
        }
        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "IndexUpdateEvent [updateInfo=" + updateInfo + "]";
    }
    
    /**
     * getter method for property updateInfo
     * 
     * @return the updateInfo
     */
    public IndexUpdateInfo getUpdateInfo() {
        return updateInfo;
    }
    
    /**
     * setter method for property updateInfo
     * 
     * @param updateInfo
     *            the updateInfo to set
     */
    public void setUpdateInfo(IndexUpdateInfo updateInfo) {
        this.updateInfo = updateInfo;
    }
    
}
