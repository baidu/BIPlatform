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
package com.baidu.rigel.biplatform.ac.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import com.baidu.rigel.biplatform.ac.exception.MiniCubeQueryException;
import com.baidu.rigel.biplatform.ac.minicube.CallbackMember;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMeasure;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMember;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.Member;
import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.query.data.HeadField;
import com.baidu.rigel.biplatform.ac.query.data.vo.MetaJsonDataInfo;
import com.baidu.rigel.biplatform.ac.query.data.vo.MetaJsonDataInfo.MetaType;

/**
 * 对于一些对象，通过json反序列化以后，需要重新把节点的 父节点等冗余属性设置进去
 * 
 * @author xiaoming.chen
 *
 */
public class JsonUnSeriallizableUtils {

    /**
     * 将DataModel的json字符串转换成DataModel并且方向设置每个节点的父节点信息
     * 
     * @param dataModelJson DataModel的json字符串
     * @return json转换出来的DataModel对象
     */
    public static DataModel dataModelFromJson(String dataModelJson) {
        if (StringUtils.isBlank(dataModelJson)) {
            throw new IllegalArgumentException("datamodel json is empty.");
        }
        DataModel dataModel = AnswerCoreConstant.GSON.fromJson(dataModelJson, DataModel.class);
        setHeadFieldParent(dataModel.getRowHeadFields(), null, null);
        setHeadFieldParent(dataModel.getColumnHeadFields(), null, null);
        return dataModel;
    }

    /**
     * 为头信息的每个节点设置父节点和对应的上一层级节点
     * 
     * @param listFields 头信息
     * @param parentField 父节点
     * @param parentLevelField 上层节点
     */
    public static void setHeadFieldParent(List<HeadField> listFields, HeadField parentField, HeadField parentLevelField) {
        if (CollectionUtils.isNotEmpty(listFields)) {
            listFields.forEach((field) -> {
                field.setParent(parentField);
                field.setParentLevelField(parentLevelField);
                // 设置父节点，排序和缩进要用
                    setHeadFieldParent(field.getChildren(), field, parentLevelField);
                    // 设置下一个层级的节点信息，先把下个层级取到的父节点设置为null，循环的时候会自动设置
                    setHeadFieldParent(field.getNodeList(), null, field);

                });
        }
    }

    /**
     * 将cube的json转换成cube对象，并为cube包含的Dimension中的level设置对应的dimension属性
     * 
     * @param cubeJson cube的json字符串
     * @return 转换好的cube对象
     */
//    private static Cube parseCubeJson(String cubeJson) {
//        if (StringUtils.isBlank(cubeJson)) {
//            throw new IllegalArgumentException("cube json is blank.");
//        }
//        MiniCube cube = AnswerCoreConstant.GSON.fromJson(cubeJson, MiniCube.class);
//        // 需要将每个Dimension的level对应的Dimension属性重新设置进去
//        cube.getDimensions().values().forEach((dim) -> {
//            dim.getLevels().forEach((k, v) -> {
//                v.setDimension(dim);
//            });
//        });
//        return cube;
//    }

    /**
     * 回填Cube的维度中level对应的Dimension信息
     * 
     * @param cube
     */
    public static void fillCubeInfo(Cube cube) {
        if (cube == null) {
            throw new IllegalArgumentException("cube is null");
        }
        if (MapUtils.isNotEmpty(cube.getDimensions())) {
            cube.getDimensions().values().forEach((dim) -> {
                dim.getLevels().forEach((k, v) -> {
                    v.setDimension(dim);
                });
            });
        }
        if (MapUtils.isNotEmpty(cube.getMeasures())) {
            cube.getMeasures().values().forEach((measure) -> {
                MiniCubeMeasure miniCubeMeasure = (MiniCubeMeasure) measure;
                miniCubeMeasure.setCube(cube);
            });
        }
    }

    /**
     * 将metaDataJson信息转换成Member member直接序列化以后无法方向查找对应的Dimension和level，所以查找的时候，返回的只是基本信息
     * 
     * @param cube 当前member所属的cube
     * @param metaDataJson member的信息
     * @return 转换出来的member
     * @throws IllegalArgumentException 参数异常
     */
//    private static Member parseMetaJson2Member(Cube cube, String metaDataJson) {
//        if (StringUtils.isBlank(metaDataJson) || cube == null) {
//            throw new IllegalArgumentException("param is illegal. cube:" + cube + " metaDataJson:" + metaDataJson);
//        }
//        MetaJsonDataInfo metaJsonDataInfo = AnswerCoreConstant.GSON.fromJson(metaDataJson, MetaJsonDataInfo.class);
//        return parseMetaJson2Member(cube, metaJsonDataInfo);
//    }

    /**
     * @param cube
     * @param metaJsonData
     * @return
     */
    public static MiniCubeMember parseMetaJson2Member(Cube cube, MetaJsonDataInfo metaJsonData) {
        if (metaJsonData == null || !metaJsonData.validate()) {
            throw new IllegalArgumentException("meta data json is blank.");
        }
        if (metaJsonData.getMetaType().equals(MetaType.Member)) {
            MiniCubeMember member = null;
            if(metaJsonData.getHasChildren()!=null){
                CallbackMember cmember = new CallbackMember(metaJsonData.getMemberName());
                cmember.setHasChildren(metaJsonData.getHasChildren());
                member = cmember;
                
            }else {
                member = new MiniCubeMember(metaJsonData.getMemberName());
            }
             
            member.generateUniqueName(metaJsonData.getMemberUniqueName());
            member.setCaption(metaJsonData.getMemberCaption());
            Dimension dimension = cube.getDimensions().get(metaJsonData.getDimensionName());
            Level level = dimension.getLevels().get(metaJsonData.getLevelName());
            member.setQueryNodes(metaJsonData.getQueryNodes());
            member.setLevel(level);
            if (CollectionUtils.isNotEmpty(metaJsonData.getChildren())) {
                List<MiniCubeMember> children = new ArrayList<MiniCubeMember>();
                for (MetaJsonDataInfo child : metaJsonData.getChildren()) {
                    children.add(parseMetaJson2Member(cube, child));
                }
                member.setChildren(children);
            }

            return member;
        }
        throw new UnsupportedOperationException("unsupported meta data type:" + metaJsonData.getMetaType());
    }

    /**
     * 将Member对象转换成metaDataJsonInfo对象，方便转换成json
     * 
     * @param member 待转换的member
     * @return 转换后的member数据信息
     */
    public static MetaJsonDataInfo parseMember2MetaJson(MiniCubeMember member) {
        if (member == null) {
            throw new IllegalArgumentException("member is illegal. member:" + member);
        }
        MetaJsonDataInfo metaJsonDataInfo = new MetaJsonDataInfo(MetaType.Member);
        metaJsonDataInfo.setDimensionName(member.getLevel().getDimension().getName());
        metaJsonDataInfo.setLevelName(member.getLevel().getName());
        metaJsonDataInfo.setMemberCaption(member.getCaption());
        metaJsonDataInfo.setMemberName(member.getName());
        metaJsonDataInfo.setMemberUniqueName(member.getUniqueName());
        metaJsonDataInfo.setQueryNodes(member.getQueryNodes());
        /**
         * add by Jin
         */
        if(member instanceof CallbackMember){
            metaJsonDataInfo.setHasChildren(((CallbackMember)member).isHasChildren());
        }
        
        try {
            if (CollectionUtils.isNotEmpty(member.getChildren())) {
                for (Member child : member.getChildMembers(null, null, null)) {
                    metaJsonDataInfo.getChildren().add(parseMember2MetaJson((MiniCubeMember) child));
                }
            }
        } catch (MiniCubeQueryException e) {
            throw new RuntimeException (e);
        }

        return metaJsonDataInfo;
    }

}
