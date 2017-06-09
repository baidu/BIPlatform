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
package com.baidu.rigel.biplatform.ac.util;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.ac.minicube.MiniCubeDimension;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.Member;
import com.baidu.rigel.biplatform.ac.model.OlapElement;
import com.google.common.collect.Lists;

/**
 * 元数据名称操作的工具类
 * 
 * @author xiaoming.chen
 * 
 */
/**
 * 类MetaNameUtil.java的实现描述：TODO 类实现描述 
 * @author luowenlei 2016年3月24日 下午2:46:40
 */
public class MetaNameUtil {

    /**
     * UNIQUE_NAME_PATTERN 元数据UniqueName的模板
     */
    public static final String UNIQUE_NAME_FORMAT = "[%s]";

    /**
     * SUMMARY_MEMBER_NAME_PRE all节点的名称开头 \\[ [^\\]\\[] \\]
     */
    public static final String SUMMARY_MEMBER_NAME_PRE = "All_";

    /**
     * UNIQUE_NAME_REGEX uniqueName的正则
     */
    public static final String UNIQUE_NAME_REGEX = "^\\[[^\\]\\[]+\\](\\.\\[[^\\]\\[]+\\])*$";

    /**
     * LOGGER
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MetaNameUtil.class);

    /**
     * 构造UniqueName
     * 
     * @param parent 当前节点的父节点
     * @param name 当前节点的名称
     * @return 当前节点的UniqueName
     */
    public static String makeUniqueName(OlapElement parent, String name) {
        // 如果父节点为空或者父节点为all节点，直接跳过父节点
        if (parent == null) {
            return makeUniqueName(name);
        } else {
            if (isAllMemberName(parent.getName()) && (parent instanceof Member)) {
                Member member = (Member) parent;
                parent = member.getLevel().getDimension();
            }
            StringBuilder buf = new StringBuilder(64);
            buf.append(parent.getUniqueName());
            buf.append('.');
            buf.append(makeUniqueName(name));
            return buf.toString();
        }
    }
    
    
    
    /** 
     * generateMeasureUniqueName
     * @param name
     * @return
     */
    public static String generateMeasureUniqueName(String name) {
        return MetaNameUtil.makeUniqueName(Measure.MEASURE_DIMENSION_NAME) + "." + MetaNameUtil.makeUniqueName(name);
    }

    /**
     * 判断一个字符串是否是符合UniqueName格式
     * 
     * @param uniqueName UniqueName字符串
     * @return 是否UniqueName
     */
    public static boolean isUniqueName(String uniqueName) {
        if (StringUtils.isNotBlank(uniqueName) && Pattern.matches(UNIQUE_NAME_REGEX, uniqueName)) {
            return true;
        }
        return false;
    }

    /**
     * 用中括号将Member的name包住
     * 
     * @param metaName member的name
     * @return 封装好的UniqueName
     */
    public static String makeUniqueName(String metaName) {
        if (StringUtils.isBlank(metaName)) {
            throw new IllegalArgumentException("metaName can not be empty");
        }

        return String.format(UNIQUE_NAME_FORMAT, metaName);
    }

    /**
     * 用中括号将Member列表的name包住
     * 
     * @param metaNames member的name
     * @return @return 封装好的UniqueName列表
     */
    public static List<String> makeUniqueNameList(String[] metaNames) {
        List<String> makeUniqueNameList = Lists.newArrayList();
        for (String metaName : metaNames) {
            makeUniqueNameList.add(makeUniqueName(metaName));
        }
        return makeUniqueNameList;
    }

    /**
     * 将一个UniqueName转换成字符串数组
     * 
     * @param uniqueName 一个UniqueName
     * @return 字符串数组
     */
    public static String[] parseUnique2NameArray(String uniqueName) {
        // 需要处理
        if (!isUniqueName(uniqueName)) {
            throw new IllegalArgumentException("uniqueName is illegal:" + uniqueName);
        }
        uniqueName = uniqueName.substring(1, uniqueName.lastIndexOf("]"));
        return StringUtils.splitByWholeSeparator(uniqueName, "].[");
    }
    
    /**
     * 根据给定的uniqueName以及传入的序号，截取出符合序号描述的子uniqueName
     * 
     * @param uniqueName 待截取的uniqueName
     * @param index 截取第几位符合规则的字符串
     * @return 截取完成的子字符串
     */
    public static String subUniqueNameOfIndexFlag(String uniqueName, int index) {
        if (uniqueName == null || uniqueName.length() == 0) {
            return null;
        }
        String[] nameArray = parseUnique2NameArray(uniqueName);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < index; i++) {
            String singleName = nameArray[i];
            sb.append(makeUniqueName(singleName));
            if (i < index - 1) {
                sb.append(".");
            }
        }
        return sb.toString();
    }
    
    /** 
     * getNameFromMetaName 从元数据名称中获取名称信息
     * @param metaName
     * @return
     */
    public static String getNameFromMetaName(String metaName) {
        if(isUniqueName(metaName)) {
            String[] nameArr = parseUnique2NameArray(metaName);
            return nameArr[nameArr.length - 1];
        } else {
            return metaName;
        }
    }

    /**
     * 判断一个UniqueName是否是一个all节点的UniqueName
     * 
     * @param uniqueName 节点的UniqueName
     * @return 是否是all节点
     * @throws IllegalArgumentException unique格式不正确
     */
    public static boolean isAllMemberUniqueName(String uniqueName) {
        if (!isUniqueName(uniqueName)) {
            LOGGER.warn("uniqueName is illegal:" + uniqueName);
            return false;
        }
        String[] names = parseUnique2NameArray(uniqueName);
        if (names.length == 2 && isAllMemberName(names[1])) {
            return true;
        }
        return false;
    }
    
    /**
     * 判断一个UniqueName的最后一个节点是否是一个all节点的UniqueName
     * 
     * @param uniqueName 节点的UniqueName
     * @return 是否是all节点
     * @throws IllegalArgumentException unique格式不正确
     */
    public static boolean isLastAllMemberUniqueName(String uniqueName) {
        if (!isUniqueName(uniqueName)) {
            LOGGER.warn("uniqueName is illegal:" + uniqueName);
            return false;
        }
        String[] names = parseUnique2NameArray(uniqueName);
        if (isAllMemberName(names[names.length - 1])) {
            return true;
        }
        return false;
    }
    
    /**
     * 判断一个UniqueName是否是一个all节点的UniqueName,用户多级的查询
     * 
     * @param uniqueName 节点的UniqueName
     * @param 取index的值 至少2级，index从0开始
     * @return 是否是all节点
     * @throws IllegalArgumentException unique格式不正确
     */
    public static boolean isAllMemberUniqueName(String uniqueName, int index) {
        if (!isUniqueName(uniqueName)) {
            LOGGER.warn("uniqueName is illegal:" + uniqueName);
            return false;
        }

        String[] names = parseUnique2NameArray(uniqueName);
        if (index < 1 ||  index >= names.length) {
        // 至少2级，index从0开始
            index = names.length - 1;
        }
        if (names.length == 2 && isAllMemberName(names[1])) {
            return true;
        } else if (names.length > 2) {
            return isAllMemberName(names[index]);
        }
        return false;
    }

    /**
     * 判断一个字符串是否是维度的All节点的name
     * 
     * @param name 节点的name
     * @return 是否是all节点的name
     */
    public static boolean isAllMemberName(String name) {
        if (StringUtils.startsWith(name, SUMMARY_MEMBER_NAME_PRE)) {
            return true;
        }
        return false;
    }

    /**
     * 根据UniqueName获取父节点的UniqueName
     * 
     * @param uniqueName 指定节点的UniqueName
     * @return 父节点的UniqueName，为null表示没有父节点
     * @throws IllegalArgumentException unique格式不正确
     */
    public static String getParentUniqueName(String uniqueName) {
        if (!isUniqueName(uniqueName)) {
            throw new IllegalArgumentException("uniqueName is illegal:" + uniqueName);
        }
        String[] names = parseUnique2NameArray(uniqueName);
        if (names.length == 2) {
            return makeUniqueName(names[0]) + "."
                    + makeUniqueName(String.format(MiniCubeDimension.ALL_DIMENSION_NAME_PATTERN, names[0]));
        } else if (names.length < 2) {
            return null;
        }
        return uniqueName.substring(0, uniqueName.lastIndexOf(".["));
    }
    
    /**
     * 从UniqueName中获取维度名称
     * @param uniqueName
     * @return
     */
    public static String getDimNameFromUniqueName(String uniqueName){
        String[] metaNames = parseUnique2NameArray(uniqueName);
        return metaNames[0];
    }
    
    /**
     * 根据当前查询的level index，如果为2级，则返回0.
     *
     * @param levels 所有的层级元数据
     * @param uniqueName
     * @return index 为levels里面的index层，如果配合使用parseUnique2NameArray方法 index需要+1
     */
    public static int getSearchLevelIndexByUniqueName(String uniqueName) {
        if (!isUniqueName(uniqueName)) {
            return 0;
        }
        String[] names = MetaNameUtil.parseUnique2NameArray(uniqueName);
        if (names.length <= 2) {
        // 为两级的情况,[行业]。[All_行业s]
            return 0;
        }
        // 此为3级的情况
        if (MetaNameUtil.isAllMemberName(names[names.length - 1])) {
        // 如果最后一个为all s 那么返回最后一个值的后面的index [行业]。[AA]。[All_AAs]，返回AA
            return names.length - 3;
        } else {
         // 如果最后一个不为all s 那么返回最后一个值的index [行业]。[AA]。[AAA]，返回AAA
            return names.length - 2;
        }
    }
    
    /**
     * makeUniqueNamesArray
     *
     * @param array
     * @return
     */
    public static String makeUniqueNamesArray(String[] array) {
        StringBuffer sb = new StringBuffer();
        for (String value : array) {
            if (StringUtils.isEmpty(sb.toString())) {
                sb.append("[" + value + "]");
            } else {
                sb.append(".[" + value + "]");
            }
        }
        return sb.toString();
    }

}
