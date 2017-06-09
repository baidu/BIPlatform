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
package com.baidu.rigel.biplatform.queryrouter.query.service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;

import com.baidu.rigel.biplatform.ac.exception.MiniCubeQueryException;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMember;
import com.baidu.rigel.biplatform.ac.minicube.TimeDimension;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.DimensionType;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.LevelType;
import com.baidu.rigel.biplatform.ac.model.Member;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.queryrouter.query.exception.MetaException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 查询维值相关的接口
 * 
 * @author xiaoming.chen
 *
 */
public interface DimensionMemberService extends BeanFactoryAware {

    /**
     * CALLBACK_MEMBER_SERCICE
     */
    String CALLBACK_MEMBER_SERCICE = "callbackDimensionMemberService";

    /**
     * SQL_MEMBER_SERVICE
     */
    String SQL_MEMBER_SERVICE = "sqlDimensionMemberService";

    /**
     * TIME_MEMBER_SERVICE
     */
    String TIME_MEMBER_SERVICE = "timeDimensionMemberService";

    /**
     * dimensionMemberServiceMap 获取维值的所有实现
     */
    Map<String, DimensionMemberService> dimensionMemberServiceMap = Maps.newHashMap();

    /**
     * 查询维值
     * 
     * @param cube 立方体
     * @param level 维值所在的层级
     * @param dataSourceInfo 数据源信息
     * @param parentMember 父节点
     * @param params Callback参数，Callback维度需要
     * @return 层级的members
     * @throws MiniCubeQueryException 查询异常
     * @throws MetaException cube is illegal
     */
    List<MiniCubeMember> getMembers(Cube cube, Level level, DataSourceInfo dataSourceInfo, Member parentMember,
            Map<String, String> params) throws MiniCubeQueryException, MetaException;
    
//    /**
//     * 查询维值
//     * 
//     * @param cube 立方体
//     * @param level 维值所在的层级
//     * @param dataSourceInfo 数据源信息
//     * @param parentMember 父节点
//     * @param params Callback参数，Callback维度需要
//     * @return 层级的members
//     * @throws MiniCubeQueryException 查询异常
//     * @throws MetaException cube is illegal
//     */
//    Map<MiniCubeMember, List<MiniCubeMember>> getMembers(Cube cube, Level level, DataSourceInfo dataSourceInfo, List<Member> parentMember,
//            Map<String, String> params) throws MiniCubeQueryException, MetaException;

    /**
     * 根据name获取指定level下的member
     * 
     * @param dataSourceInfo 数据源信息
     * @param cube 数据模型定义
     * @param level 查找数据所在的层级
     * @param name 查找的节点的名称
     * @param parent 当前节点的父节点
     * @param params Callback附带参数
     * @return 查找的的维值节点
     * @throws MiniCubeQueryException 查询 过程抛出的异常
     * @throws MetaException check illegal cube
     */
    MiniCubeMember getMemberFromLevelByName(DataSourceInfo dataSourceInfo, Cube cube, Level level, String name,
            MiniCubeMember parent, Map<String, String> params) throws MiniCubeQueryException, MetaException;

    /**
     * 根据UniqueName查找Member
     * 
     * @param dataSourceInfo 数据源信息
     * @param cube UniqueName所在的cube
     * @param uniqueName 查询的UniqueName
     * @param params Callback请求的参数，只有Callback维度才需要
     * @return 返回Member
     * @throws MetaException 找不到对应UniqueName的元数据
     * @throws MiniCubeQueryException 查询异常
     * @throws IllegalArgumentException uniqueName不合法
     */
    default MiniCubeMember lookUp(DataSourceInfo dataSourceInfo, Cube cube, String uniqueName,
            Map<String, String> params) throws MetaException, MiniCubeQueryException {

        if (cube == null) {
            throw new IllegalArgumentException("cube can not be null.");
        }

        String[] names = MetaNameUtil.parseUnique2NameArray(uniqueName);
        if (names == null || names.length < 1) {
            throw new IllegalArgumentException("param uniqueName is not member unique name:" + uniqueName);
        }
        // 如果是Time开头的，默认就是时间维度
        // TODO 不能这么判断，需要根据UniqueName中的维度名称获取到维度
//        String dimName = MetaNameUtil.getDimNameFromUniqueName (uniqueName);
//         if(cube.getDimensions ().get (dimName).getType () == DimensionType.TIME_DIMENSION){
//             return TimeDimensionUtils.
//         }
        Dimension targetDim = null;
        MiniCubeMember result = null;

        targetDim = cube.getDimensions().get(names[0]);
        if (targetDim == null) {
            throw new MetaException("can not found dimension by uniqueName:" + uniqueName + " in cube:" + cube);
        }

        List<Level> levels = Lists.newArrayList(targetDim.getLevels().values());
        int i = 1;
        Iterator<Level> it = levels.iterator();

        Level level = it.next();
        if (MetaNameUtil.isAllMemberUniqueName(uniqueName) 
                && ! (targetDim instanceof TimeDimension)
                && !level.getType().equals(LevelType.CALL_BACK)) {
            return (MiniCubeMember) targetDim.getAllMember();
        }

        // 一层层往下找，好像有问题，没有测试，还得修改
        while (i < names.length) {
            String name = names[i];
            // 在级联下拉框的情况下，uniqueName会形如：[行业维度].[交通运输].[All_交通运输s]，
            // 这时查member不需遍历到最后一层all节点，而只需取到[行业维度].[交通运输]这一级member即可
            if (targetDim.getType() == DimensionType.GROUP_DIMENSION && MetaNameUtil.isAllMemberName(name)) {
                break;
            }
            MiniCubeMember member =
                    getDimensionMemberServiceByLevelType(level.getType()).getMemberFromLevelByName(dataSourceInfo,
                            cube, level, name, result, params);
            
            // TODO 需要进一步处理 解决查询维度数据为空情况
            if (member == null) {
                throw new MetaException("can not get name:" + name + "from level:" + level);
            }
            result = member;
            if (member != null && it.hasNext()) {
                result.setLevel(level);
                // TODO 需要加上父子类型的level判断，如果父子层级的话，只允许在最后一层
                level = it.next();
            }
            i++;
        }
        return result;
    }

    /**
     * @param levelType
     * @return
     */
    static DimensionMemberService getDimensionMemberServiceByLevelType(LevelType levelType) {
        if (levelType.equals(LevelType.CALL_BACK)) {
            return dimensionMemberServiceMap.get(CALLBACK_MEMBER_SERCICE);
        } else if (levelType.name().startsWith("TIME")) {
            return dimensionMemberServiceMap.get(TIME_MEMBER_SERVICE);
        } else {
            return dimensionMemberServiceMap.get(SQL_MEMBER_SERVICE);
        }
    }

    @Override
    public default void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        ListableBeanFactory listBeanFactory = (ListableBeanFactory) beanFactory;
        dimensionMemberServiceMap.putAll(listBeanFactory.getBeansOfType(DimensionMemberService.class));
    }

    public default List<MiniCubeMember> lookUpByNames (DataSourceInfo dataSourceInfo, Cube cube,
            List<String> uniqueNameList, Map<String, String> params) throws Exception {
        if (cube == null) {
            throw new IllegalArgumentException("cube can not be null.");
        }

        String[] names = MetaNameUtil.parseUnique2NameArray(uniqueNameList.get (0));
        if (names == null || names.length < 1) {
            throw new IllegalArgumentException("param uniqueName is not member unique name:" + uniqueNameList);
        }
        // 如果是Time开头的，默认就是时间维度
        // TODO 不能这么判断，需要根据UniqueName中的维度名称获取到维度
        // if(names[0].startsWith(TimeDimension.DEFAULT_TIME_DIMENSION_NAME)){
        // return TimeDimensionUtils.processTimeMember(cube, names);
        // }
        Dimension targetDim = null;
        targetDim = cube.getDimensions().get(names[0]);
        if (targetDim == null) {
            throw new MetaException("can not found dimension by uniqueName:" + uniqueNameList + " in cube:" + cube);
        }

        // 如果是Time开头的，默认就是时间维度
        // TODO 不能这么判断，需要根据UniqueName中的维度名称获取到维度
//        List<Level> levels = Lists.newArrayList(targetDim.getLevels().values());
//        Level level = levels.get (names.length - 2);
        int currentSearchLevelIndex = MetaNameUtil
                .getSearchLevelIndexByUniqueName(StringUtils.replace(uniqueNameList.get(0), ",", ""));
        Level level = targetDim.getLevels().values().toArray(new Level[0])[currentSearchLevelIndex];
         if(targetDim.isTimeDimension ()){
             return dimensionMemberServiceMap
                     .get(TIME_MEMBER_SERVICE)
                     .getMemberFromLevelByNames(dataSourceInfo, cube, level, params, uniqueNameList);
         }
        boolean hasAllMember = false;
        for (String uniqueName : uniqueNameList) {
            if (MetaNameUtil.isAllMemberUniqueName (uniqueName)) {
                hasAllMember = true;
                break;
            }
        }
        if (hasAllMember && !level.getType().equals(LevelType.CALL_BACK)) {
            List<MiniCubeMember> rs = Lists.newArrayList ();
            rs.add ((MiniCubeMember) targetDim.getAllMember());
            return rs;
        }
        return getDimensionMemberServiceByLevelType(level.getType()).getMemberFromLevelByNames(
                dataSourceInfo,
                cube, level, params, uniqueNameList);
    }

    List<MiniCubeMember> getMemberFromLevelByNames(
            DataSourceInfo dataSourceInfo, Cube cube, Level level,
            Map<String, String> params, List<String> uniqueNameList);

}
