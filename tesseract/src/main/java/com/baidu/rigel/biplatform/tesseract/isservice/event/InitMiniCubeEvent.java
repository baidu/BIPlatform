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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;

import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;

/**
 * 初始化minicube的事件
 * 
 * @author lijin
 *
 */
public class InitMiniCubeEvent extends ApplicationEvent {
    
    /**
     * serialVersionUID long
     */
    private static final long serialVersionUID = -6751717297333294754L;
    
    /**
     * LOGGER
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InitMiniCubeEvent.class);
    
    /**
     * initMiniCubeInfo
     */
    private InitMiniCubeInfo initMiniCubeInfo;
    
    /**
     * 
     * InitMiniCubeInfo
     * 
     * @author lijin
     *
     */
    public static class InitMiniCubeInfo implements Serializable {
        /**
         * serialVersionUID long
         */
        private static final long serialVersionUID = 208955286286448627L;
        /**
         * cube列表
         */
        private List<Cube> cubeList;
        /**
         * 数据源信息
         */
        private List<DataSourceInfo> dataSourceInfoList;
        /**
         * 是否立刻建索引
         */
        private boolean indexAsap;
        /**
         * 是否有限数据集
         */
        private boolean limited;
        
        /**
         * Constructor by
         * 
         * @param cubeList
         * @param dataSourceInfo
         * @param indexAsap
         * @param limited
         */
        public InitMiniCubeInfo(List<Cube> cubeList, List<DataSourceInfo> dataSourceInfoList,
            boolean indexAsap, boolean limited) {
            super();
            this.cubeList = cubeList;
            this.dataSourceInfoList = dataSourceInfoList;
            this.indexAsap = indexAsap;
            this.limited = limited;
        }
        
        /**
         * getter method for property cubeList
         * 
         * @return the cubeList
         */
        public List<Cube> getCubeList() {
            return cubeList;
        }
        
        /**
         * setter method for property cubeList
         * 
         * @param cubeList
         *            the cubeList to set
         */
        public void setCubeList(List<Cube> cubeList) {
            this.cubeList = cubeList;
        }
        
        /**
         * getter method for property dataSourceInfoList
         * 
         * @return the dataSourceInfoList
         */
        public List<DataSourceInfo> getDataSourceInfo() {
            return dataSourceInfoList;
        }
        
        /**
         * setter method for property dataSourceInfoList
         * 
         * @param dataSourceInfoList
         *            the dataSourceInfoList to set
         */
        public void setDataSourceInfo(List<DataSourceInfo> dataSourceInfoList) {
            this.dataSourceInfoList = dataSourceInfoList;
        }
        
        /**
         * getter method for property indexAsap
         * 
         * @return the indexAsap
         */
        public boolean isIndexAsap() {
            return indexAsap;
        }
        
        /**
         * setter method for property indexAsap
         * 
         * @param indexAsap
         *            the indexAsap to set
         */
        public void setIndexAsap(boolean indexAsap) {
            this.indexAsap = indexAsap;
        }
        
        /**
         * getter method for property limited
         * 
         * @return the limited
         */
        public boolean isLimited() {
            return limited;
        }
        
        /**
         * setter method for property limited
         * 
         * @param limited
         *            the limited to set
         */
        public void setLimited(boolean limited) {
            this.limited = limited;
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("InitMiniCubeInfo [[cubeList=");
            if (cubeList != null) {
                for (Cube cube : cubeList) {
                    sb.append("[cubeId:" + cube.getId() + ",cubeName:" + cube.getName() + "]");
                }
            }
            sb.append("]");
            sb.append(",[dataSourceInfo=" + dataSourceInfoList + "]");
            sb.append(",[indexAsap=" + indexAsap + "]");
            sb.append(",[limited=" + limited + "]");
            sb.append("]");
            return sb.toString();
        }
        
    }
    
    /**
     * Constructor by
     * 
     * @param source
     */
    public InitMiniCubeEvent(Object source) {
        super(source);
        if (source != null && source instanceof InitMiniCubeInfo) {
            this.initMiniCubeInfo = (InitMiniCubeInfo) source;
        } else {
            LOGGER.info("can not accept param and init an empty event");
        }
    }
    
    /**
     * getter method for property initMiniCubeInfo
     * 
     * @return the initMiniCubeInfo
     */
    public InitMiniCubeInfo getInitMiniCubeInfo() {
        return initMiniCubeInfo;
    }
    
    /**
     * setter method for property initMiniCubeInfo
     * 
     * @param initMiniCubeInfo
     *            the initMiniCubeInfo to set
     */
    public void setInitMiniCubeInfo(InitMiniCubeInfo initMiniCubeInfo) {
        this.initMiniCubeInfo = initMiniCubeInfo;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "InitMiniCubeEvent [initMiniCubeInfo=" + initMiniCubeInfo + "]";
    }
    
}
