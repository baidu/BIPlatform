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
package com.baidu.rigel.biplatform.ac.model;

import java.util.List;
import java.util.Map;

import com.baidu.rigel.biplatform.ac.exception.MiniCubeQueryException;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;

/**
 * 维度或者指标成员：维度或者指标的一个取值。 如某年某月某天 或者某年某月某天的某个链接被点击的次数
 * 
 * @author xiaoming.chen
 *
 */
public interface Member extends OlapElement {

    /**
     * 当前节点的孩子节点
     * 
     * @return 孩子节点列表
     * @throws MiniCubeQueryException
     */
    List<Member> getChildMembers(Cube cube, DataSourceInfo dataSourceInfo, Map<String, String> params)
            throws MiniCubeQueryException;

    /**
     * 返回孩子节点的数量
     * 
     * @return 孩子节点的数量
     * @throws MiniCubeQueryException
     */
    int getChildMemberCount(Cube cube, DataSourceInfo dataSourceInfo, Map<String, String> params)
            throws MiniCubeQueryException;

    /**
     * Returns the parent of this Member, or null if it has no parent.
     * 
     * @param cube
     * @param dataSourceInfo
     * @param params
     * @return Parent member, or null if member has no parent
     * @throws MiniCubeQueryException
     */
    Member getParentMember(Cube cube, DataSourceInfo dataSourceInfo, Map<String, String> params)
            throws MiniCubeQueryException;

    /**
     * Returns the Level of this Member.
     *
     * <p>
     * Never returns null.
     * </p>
     *
     * @return Level which this Member belongs to
     */
    Level getLevel();

    /**
     * Member中节点的name是否以 ALL_ 开头
     * 
     * @return 是否是All节点
     */
    default boolean isAll() {
        if (MetaNameUtil.isAllMemberName(getName())) {
            return true;
        } else {
            return false;
        }
    }

}
