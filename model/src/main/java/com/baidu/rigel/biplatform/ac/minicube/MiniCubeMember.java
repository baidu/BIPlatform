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
package com.baidu.rigel.biplatform.ac.minicube;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.ac.exception.MiniCubeQueryException;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.Member;
import com.baidu.rigel.biplatform.ac.query.MiniCubeConnection;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.data.vo.MetaJsonDataInfo;
import com.baidu.rigel.biplatform.ac.query.model.ConfigQuestionModel;
import com.baidu.rigel.biplatform.ac.query.model.DimensionCondition;
import com.baidu.rigel.biplatform.ac.query.model.QueryData;
import com.baidu.rigel.biplatform.ac.util.AnswerCoreConstant;
import com.baidu.rigel.biplatform.ac.util.ConfigInfoUtils;
import com.baidu.rigel.biplatform.ac.util.HttpRequest;
import com.baidu.rigel.biplatform.ac.util.JsonUnSeriallizableUtils;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ac.util.ResponseResult;
import com.baidu.rigel.biplatform.ac.util.ServerUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.reflect.TypeToken;

/**
 * minicube Member实现
 * 
 * @author xiaoming.chen
 *
 */
@JsonIgnoreProperties
public class MiniCubeMember extends OlapElementDef implements Member {

    /**
     * ALL_MEMBER_CAPTION
     */
    public static final String ALL_MEMBER_CAPTION = "%s汇总";

    /**
     * SUMMARY_NODE_CAPTION
     */
    public static final String SUMMARY_NODE_CAPTION = "合计";

    /**
     * SUMMARY_NODE_NAME
     */
    public static final String SUMMARY_NODE_NAME = "SUMMARY";

    /**
     * logger
     */
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -447235224901193723L;

    /**
     * children member children
     */
    private transient List<Member> children;

    /**
     * parent parent member
     */
    @JsonIgnore
    private Member parent;

    /**
     * level member level All 节点的level为维度的第一个level
     */
    @JsonIgnore
    private Level level;

    /**
     * uniqueName 维值的UniqueName
     */
    private String uniqueName;

    /**
     * 上一级的memberName
     */
    @JsonIgnore
    private String parentMemberName;
    
    /**
     * queryNodes 实际用于查询的节点
     */
    private Set<String> queryNodes;
    
    /**
     * construct with member name
     * 
     * @param name member name
     */
    public MiniCubeMember(String name) {
        super(name);
    }

    @Override
    public List<Member> getChildMembers(Cube cube, DataSourceInfo dataSourceInfo, Map<String, String> params)
            throws MiniCubeQueryException {
        if (CollectionUtils.isEmpty(this.children) && cube != null && dataSourceInfo != null) {
            if (isAll()) {
                this.children = level.getMembers(cube, dataSourceInfo, params);
            } else {
                long current = System.currentTimeMillis();
                ConfigQuestionModel questionModel = new ConfigQuestionModel();
                questionModel.setCube(cube);
                questionModel.setDataSourceInfo(dataSourceInfo);
                questionModel.setRequestParams(params);

                DimensionCondition dimCondition = new DimensionCondition(this.getLevel().getDimension().getName());
                dimCondition.getQueryDataNodes().add(new QueryData(getUniqueName()));
                questionModel.getQueryConditions().put(dimCondition.getMetaName(), dimCondition);
                Map<String, String> headerParams = new HashMap<String, String>();
                Map<String, String> requestParams = new HashMap<String, String>();
                String questionModelJson = AnswerCoreConstant.GSON.toJson(questionModel);
                requestParams.put(MiniCubeConnection.QUESTIONMODEL_PARAM_KEY,
                        questionModelJson);
                
                ServerUtils.setServerProperties(questionModelJson,
                        ((ConfigQuestionModel) questionModel).getDataSourceInfo().getProductLine(),
                        requestParams, headerParams);
                String response =
                        HttpRequest.sendPost(ConfigInfoUtils.getServerAddress()
                                + "/meta/getChildren", requestParams, headerParams);
                ResponseResult responseResult = AnswerCoreConstant.GSON.fromJson(response, ResponseResult.class);
                if (StringUtils.isNotBlank(responseResult.getData())) {
                    String memberListJson = responseResult.getData().replace("\\", "");
                    // memberListJson = memberListJson.substring(1, memberListJson.length() - 1);
                    List<MetaJsonDataInfo> metaJsons =
                            AnswerCoreConstant.GSON.fromJson(memberListJson, new TypeToken<List<MetaJsonDataInfo>>() {
                            }.getType());
                    this.children = new ArrayList<Member>(metaJsons.size());
                    if (CollectionUtils.isNotEmpty(metaJsons)) {
                        metaJsons.forEach((metaJson) -> {
                            children.add(JsonUnSeriallizableUtils.parseMetaJson2Member(cube, metaJson));
                        });
                    }

                    StringBuilder sb = new StringBuilder();
                    sb.append("execute query questionModel:").append(questionModel).append(" cost:")
                            .append(System.currentTimeMillis() - current).append("ms");
                } else {
                    throw new MiniCubeQueryException("query occur error,msg:" + responseResult.getStatusInfo());
                }
            }

            this.children.forEach((member) -> {
                // 将每个孩子的父节点设置成本身，Tesseract查询传过来的只有UniqueName，设置后可以避免
                // 取parentMember的时候重新发起查询请求
                    MiniCubeMember miniCubeMember = (MiniCubeMember) member;
                    miniCubeMember.setParent(this);
                    miniCubeMember.generateUniqueName(null);
                });
        }
        if (this.children == null) {
            this.children = new ArrayList<Member>(1);
        }

        return this.children;
    }

    @Override
    public String getCaption() {
        if (isAll()) {
            return String.format(ALL_MEMBER_CAPTION, level.getDimension().getCaption());
        }
        return super.getCaption();
    };

    @Override
    public int getChildMemberCount(Cube cube, DataSourceInfo dataSourceInfo, Map<String, String> params)
            throws MiniCubeQueryException {
        if (CollectionUtils.isEmpty(this.children)) {
            this.children = getChildMembers(cube, dataSourceInfo, params);
        }
        return this.children.size();
    }

    @Override
    public Member getParentMember(Cube cube, DataSourceInfo dataSourceInfo, Map<String, String> params)
            throws MiniCubeQueryException {
        if (parent != null) {
            return parent;
        }
        try {
            String parentUniqueName = MetaNameUtil.getParentUniqueName(uniqueName);
            if (StringUtils.isNotBlank(parentUniqueName)) {
                return cube.lookUp(dataSourceInfo, parentUniqueName, params);
            }
        } catch (Exception e) {
            logger.warn("get parentMember error.", e);
            throw new MiniCubeQueryException(e);
        }
        // 从UniqueName中查找到
        return null;
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

    @Override
    public String getUniqueName() {
        if (StringUtils.isBlank(uniqueName)) {
            this.uniqueName = generateUniqueName(null);
        }
        return this.uniqueName;
    }

    /**
     * generate member uniqueName with member name and member parentUniqueName
     * 
     * @param uniqueName 指定的UniqueName
     * @return generate uniqueName
     */
    public String generateUniqueName(String uniqueName) {
        // 特殊的UniqueName直接通过这个方法设置进去，避免无法获取parentMember生成错误的UniqueName
        if (StringUtils.isNotBlank(uniqueName)) {
            this.uniqueName = uniqueName;
            return uniqueName;
        }
        if (parent == null) {
            this.uniqueName = MetaNameUtil.makeUniqueName(level.getDimension(), getName());
        } else {
            this.uniqueName = MetaNameUtil.makeUniqueName(parent, getName());
        }
        return this.uniqueName;
    }

    /**
     * get parent
     * 
     * @return the parent
     */
    public Member getParent() {
        return parent;
    }

    /**
     * set parent with parent
     * 
     * @param parent the parent to set
     */
    public void setParent(Member parent) {
        this.parent = parent;
    }

    /**
     * set level with level
     * 
     * @param level the level to set
     */
    public void setLevel(Level level) {
        this.level = level;
    }

    /**
     * get queryNodes
     * 
     * @return the queryNodes
     */
    public Set<String> getQueryNodes() {
        if (this.queryNodes == null) {
            this.queryNodes = new HashSet<String>();
        }
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
     * set children with children
     * 
     * @param children the children to set
     */
    public void setChildren(List<MiniCubeMember> children) {
        if (CollectionUtils.isNotEmpty(children)) {
            this.children = new ArrayList<Member>(children.size());
            children.forEach((member) -> {
                this.children.add(member);
            });
        }
    }

    /**
     * get current member's children
     * NOTE：sometime, invoke current method very dangerous. if you do this,
     * please make sure the children already initialized, even though, this method can not promise you can get correct
     * result.
     * @return List<Member>
     */
    public List<Member> getChildren() {
        return this.children;
    }

    /**
     * default generate get parentMemberName
     * @return the parentMemberName
     */
    public String getParentMemberName() {
        return parentMemberName;
    }

    /**
     * default generate set parentMemberName
     * @param parentMemberName the parentMemberName to set
     */
    public void setParentMemberName(String parentMemberName) {
        this.parentMemberName = parentMemberName;
    }

}
