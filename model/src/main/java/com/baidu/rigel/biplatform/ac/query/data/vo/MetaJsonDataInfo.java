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
package com.baidu.rigel.biplatform.ac.query.data.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 元数据传递的json信息
 * 
 * @author xiaoming.chen
 *
 */
public class MetaJsonDataInfo implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 6958740431173359521L;

    /**
     * logger
     */
    private static Logger logger = LoggerFactory.getLogger(MetaJsonDataInfo.class);

    /**
     * dataSourceInfoKey 数据源信息KEY
     */
    private String dataSourceInfoKey;

    /**
     * cubeId cube的ID
     */
    private String cubeId;

    /**
     * dimensionName 维度名称
     */
    private String dimensionName;

    /**
     * levelName 层级名称
     */
    private String levelName;

    /**
     * memberName 维值名称
     */
    private String memberName;

    /**
     * memberUniqueName 维值UniqueName名称
     */
    private String memberUniqueName;

    /**
     * memberCaption 维值的caption
     */
    private String memberCaption;

    /**
     * queryNodes 维值对应事实表查询的ID
     */
    private Set<String> queryNodes;

    /**
     * metaType 数据类型
     */
    private MetaType metaType;

    /**
     * children
     */
    private List<MetaJsonDataInfo> children;
    
    /**
     * hasChildren 当member为 callbackMember时，用来记录callback接口返回的hasChild
     *
     */
    private Boolean hasChildren;

    /**
     * json对应数据信息类型
     * 
     * @author xiaoming.chen
     *
     */
    public enum MetaType {
        // Cube(4),
        Dimension(3), Level(2), Member(1);

        /**
         * id 类型ID
         */
        private int id;

        /**
         * constructor
         * 
         * @param id
         */
        private MetaType(int id) {
            this.id = id;
        }

        /**
         * get id
         * 
         * @return the id
         */
        public int getId() {
            return id;
        }

    }

    /**
     * constructor
     * 
     * @param metaType 数据类型
     */
    public MetaJsonDataInfo(MetaType metaType) {
        this.metaType = metaType;
    }

    /**
     * get dataSourceInfoKey
     * 
     * @return the dataSourceInfoKey
     */
    public String getDataSourceInfoKey() {
        return dataSourceInfoKey;
    }

    /**
     * set dataSourceInfoKey with dataSourceInfoKey
     * 
     * @param dataSourceInfoKey the dataSourceInfoKey to set
     */
    public void setDataSourceInfoKey(String dataSourceInfoKey) {
        this.dataSourceInfoKey = dataSourceInfoKey;
    }

    /**
     * get cubeId
     * 
     * @return the cubeId
     */
    public String getCubeId() {
        return cubeId;
    }

    /**
     * set cubeId with cubeId
     * 
     * @param cubeId the cubeId to set
     */
    public void setCubeId(String cubeId) {
        this.cubeId = cubeId;
    }

    /**
     * get dimensionName
     * 
     * @return the dimensionName
     */
    public String getDimensionName() {
        return dimensionName;
    }

    /**
     * set dimensionName with dimensionName
     * 
     * @param dimensionName the dimensionName to set
     */
    public void setDimensionName(String dimensionName) {
        this.dimensionName = dimensionName;
    }

    /**
     * get levelName
     * 
     * @return the levelName
     */
    public String getLevelName() {
        return levelName;
    }

    /**
     * set levelName with levelName
     * 
     * @param levelName the levelName to set
     */
    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    /**
     * get memberName
     * 
     * @return the memberName
     */
    public String getMemberName() {
        return memberName;
    }

    /**
     * set memberName with memberName
     * 
     * @param memberName the memberName to set
     */
    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    /**
     * get memberUniqueName
     * 
     * @return the memberUniqueName
     */
    public String getMemberUniqueName() {
        return memberUniqueName;
    }

    /**
     * set memberUniqueName with memberUniqueName
     * 
     * @param memberUniqueName the memberUniqueName to set
     */
    public void setMemberUniqueName(String memberUniqueName) {
        this.memberUniqueName = memberUniqueName;
    }

    /**
     * get memberCaption
     * 
     * @return the memberCaption
     */
    public String getMemberCaption() {
        return memberCaption;
    }

    /**
     * set memberCaption with memberCaption
     * 
     * @param memberCaption the memberCaption to set
     */
    public void setMemberCaption(String memberCaption) {
        this.memberCaption = memberCaption;
    }

    /**
     * get metaType
     * 
     * @return the metaType
     */
    public MetaType getMetaType() {
        return metaType;
    }

    /**
     * set metaType with metaType
     * 
     * @param metaType the metaType to set
     */
    public void setMetaType(MetaType metaType) {
        this.metaType = metaType;
    }

    @Override
    public String toString() {
        return "MetaJsonDataInfo [dataSourceInfoKey=" + dataSourceInfoKey + ", cubeId=" + cubeId + ", dimensionName="
                + dimensionName + ", levelName=" + levelName + ", memberName=" + memberName + ", memberUniqueName="
                + memberUniqueName + ", memberCaption=" + memberCaption + ", metaType=" + metaType + "]";
    }

    /**
     * 根据数据类型进行交易当前数据类型所需要的数据信息是否完整
     * 
     * @return 数据信息是否完整
     */
    public boolean validate() {
        if (metaType == null) {
            return false;
        }
        if (metaType.getId() <= MetaType.Dimension.getId()) {
            if (StringUtils.isBlank(dimensionName)) {
                logger.warn("dimension name " + dimensionName + " cube id can not be empty:" + cubeId);
                return false;
            }
            if (metaType.getId() <= MetaType.Level.getId()) {
                if (StringUtils.isBlank(levelName)) {
                    logger.warn("level name " + levelName + " not be empty:");
                    return false;
                }
                if (metaType.getId() <= MetaType.Member.getId()) {
                    if (StringUtils.isBlank(memberName) && StringUtils.isBlank(memberUniqueName)
                            && StringUtils.isBlank(memberCaption)) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("member info name: ").append(memberName).append(" memberUniqueName: ")
                                .append(memberUniqueName).append(" memberCaption: ").append(memberCaption)
                                .append(" can not be empty.");
                        logger.warn(sb.toString());
                        return false;
                    }
                }
            }
            return true;
        }
        logger.warn("not support metaType:" + this.metaType);
        return false;
    }

    /**
     * get queryNodes
     * 
     * @return the queryNodes
     */
    public Set<String> getQueryNodes() {
        return queryNodes;
    }

    /**
     * set queryNodes with queryNodes
     * 
     * @param queryNodes the queryNodes to set
     */
    public void setQueryNodes(Set<String> queryNodes) {
        this.queryNodes = queryNodes;
    }

    /**
     * get children
     * 
     * @return the children
     */
    public List<MetaJsonDataInfo> getChildren() {
        if (this.children == null) {
            this.children = new ArrayList<MetaJsonDataInfo>(1);
        }
        return children;
    }

    /**
     * set children with children
     * 
     * @param children the children to set
     */
    public void setChildren(List<MetaJsonDataInfo> children) {
        this.children = children;
    }

    /**
     * @return the hasChildren
     */
    public Boolean getHasChildren() {
        return hasChildren;
    }

    /**
     * @param hasChildren the hasChildren to set
     */
    public void setHasChildren(Boolean hasChildren) {
        this.hasChildren = hasChildren;
    }
    
    
}
