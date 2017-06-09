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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.query.data.DataModel.FillDataType;
import com.baidu.rigel.biplatform.ac.query.data.HeadField;
import com.baidu.rigel.biplatform.ac.query.data.HeadFieldComparator;
import com.baidu.rigel.biplatform.ac.query.model.SortRecord;
import com.baidu.rigel.biplatform.ac.query.model.SortRecord.SortType;
import com.google.common.collect.Lists;

/**
 * DataModel操作工具类
 * 
 * @author xiaoming.chen
 *
 */
public class DataModelUtils {

    /**
     * logger
     */
    private static Logger logger = LoggerFactory.getLogger(DataModelUtils.class);

    /**
     * 通过带数据的列头和行头信息构建DataModel
     * 
     * @param columnBaseHeadField 带有数据的列头信息
     * @param rowHeadDefine 行头信息
     * @return 构建的DataModel
     */
//    private static DataModel buildDataModel(List<HeadField> columnBaseHeadField, List<HeadField> rowHeadDefine) {
//        DataModel dataModel = new DataModel();
//        dataModel.setRowHeadFields(rowHeadDefine);
//        dataModel.setColumnHeadFields(columnBaseHeadField);
//        fillColumnData(dataModel, FillDataType.COLUMN);
//        return dataModel;
//    }

    /**
     * 在指定的头信息中查找符合指定UniqueName的第一个节点
     * 
     * @param fileds 头信息
     * @param uniqueName // TODO 后续需要修改，应该传递的是一个UniqueName的集合，按照顺序查找下去
     * @return
     */
    private static int foundIndexByLeafeValue(List<HeadField> fileds, String uniqueName) {
        if (CollectionUtils.isEmpty(fileds)) {
            throw new IllegalArgumentException("root fields can not be empty!");
        }
        if (StringUtils.isBlank(uniqueName)) {
            throw new IllegalArgumentException("value can not be empty!");
        }
        List<HeadField> leafFileds = new ArrayList<HeadField>(50);
        for (HeadField columnFiled : fileds) {
            leafFileds.addAll(columnFiled.getLeafFileds(false));
        }
        int index = -1;
        for (int i = 0; i < leafFileds.size(); i++) {
            if (uniqueName.equals(leafFileds.get(i).getValue())) {
                index = i;
            }
        }
        if (index == -1) {
            throw new IllegalArgumentException("can not found leaf field with value:" + uniqueName);
        }
        return index;
    }

    /**
     * 将数据按照类型合并到原来的dataModel中
     * 
     * @param dataModel 数据母体
     * @param datas 待合并的数据
     * @param type 合并方式
     * @return 合并后的整个数据母体
     */
//    public static DataModel addData(DataModel dataModel, List<HeadField> datas, FillDataType type) {
//        if (dataModel == null) {
//            throw new IllegalArgumentException("dataModel can not be null");
//        }
//        if (CollectionUtils.isEmpty(datas)) {
//            return dataModel;
//        }
//        // 按照行或者列的类型将数据封装到表头属性中
//        fillFieldData(dataModel, type);
//        if (type.equals(FillDataType.COLUMN)) {
//            dataModel.getColumnHeadFields().addAll(datas);
//        } else {
//            dataModel.getRowHeadFields().addAll(datas);
//        }
//        // 根据不同的类型将数据重新塞到dataModel的数据区中
//        fillColumnData(dataModel, type);
//        return dataModel;
//    }

    /**
     * 将DataModel按照行进行排序
     * 
     * @param dataModel 待排序的DataModel
     * @param sort 排序类型
     * @throws IllegalAccessException 排序的列非法
     */
    private static void sortByRow(DataModel dataModel, SortType sort) throws IllegalAccessException {
        if (dataModel == null) {
            throw new IllegalArgumentException("can not sort empty dataModel!");
        }
        fillFieldData(dataModel, FillDataType.ROW);
        buildSortSummary(dataModel);
        sortListHeadFields(dataModel.getRowHeadFields(), sort);
        fillColumnData(dataModel, FillDataType.ROW);
    }

    /**
     * 将头信息进行排序
     * 
     * @param headFields 待排序的头信息（包含排序的数据）
     * @param sort 排序方案
     */
    private static void sortListHeadFields(List<HeadField> headFields, SortType sort) {
        if (CollectionUtils.isNotEmpty(headFields)) {
            for (HeadField filed : headFields) {
                sortListHeadFields(filed.getNodeList(), sort);
                sortListHeadFields(filed.getChildren(), sort);
                for (HeadField child : filed.getChildren()) {
                    sortListHeadFields(child.getNodeList(), sort);
                }
            }
            Collections.sort(headFields, new HeadFieldComparator(sort));
        }

    }

    /**
     * 将指定头信息中的数据封装成表格展现的纯数据双重List
     * 
     * @param dataModel 整个DataModel
     * @param type 获取数据的行头类型，行还是列
     */
    private static void fillColumnData(DataModel dataModel, FillDataType type) {

        List<HeadField> columnLeafs = getLeafNodeList(dataModel.getColumnHeadFields());
        List<HeadField> rowLeafs = getLeafNodeList(dataModel.getRowHeadFields());
        dataModel.getColumnBaseData().clear();
        if (type.equals(FillDataType.COLUMN)) {

            for (int i = 0; i < columnLeafs.size(); i++) {
                HeadField rowField = columnLeafs.get(i);
                dataModel.getColumnBaseData().add(rowField.getCompareDatas());
                while (dataModel.getColumnBaseData().get(i).size() < rowLeafs.size()) {
                    dataModel.getColumnBaseData().get(i).add(null);
                }
            }
        } else if (type.equals(FillDataType.ROW)) {

            List<List<BigDecimal>> columnBaseData = new ArrayList<List<BigDecimal>>(columnLeafs.size());
            for (int i = 0; i < rowLeafs.size(); i++) {
                HeadField rowFiled = rowLeafs.get(i);
                for (int j = 0; j < columnLeafs.size(); j++) {
                    if (columnBaseData.size() <= j) {
                        columnBaseData.add(new ArrayList<BigDecimal>(columnLeafs.size()));
                    }
                    if (rowFiled.getCompareDatas().size() <= j) {
                        columnBaseData.get(j).add(null);
                    } else {
                        columnBaseData.get(j).add(rowFiled.getCompareDatas().get(j));
                    }
                }
            }
            dataModel.setColumnBaseData(columnBaseData);

        }
    }

    /**
     * 将数据按照类型回填到Datamodel的头信息中
     * 
     * @param dataModel 整个DataModel
     * @param type 填充数据的头类型，行还是列
     */
    public static void fillFieldData(DataModel dataModel, FillDataType type) {
        List<HeadField> columnLeafs = getLeafNodeList(dataModel.getColumnHeadFields());
        List<HeadField> rowLeafs = getLeafNodeList(dataModel.getRowHeadFields());
        if (type.equals(FillDataType.COLUMN)) {
            if (!CollectionUtils.isEmpty(columnLeafs) && dataModel.getColumnBaseData () != null) {
                for (int i = 0; i < columnLeafs.size(); i++) {
                    columnLeafs.get(i).setCompareDatas(dataModel.getColumnBaseData().get(i));
                }
            }
        } else if (type.equals(FillDataType.ROW)) {
            if (CollectionUtils.isNotEmpty(rowLeafs)) {
                List<BigDecimal> rowDatas = null;
                for (int i = 0; i < rowLeafs.size(); i++) {
                    rowDatas = new ArrayList<BigDecimal>(rowLeafs.size());
                    for (int j = 0; j < columnLeafs.size(); j++) {
                        rowDatas.add(dataModel.getColumnBaseData().get(j).get(i));
                    }
                    rowLeafs.get(i).setCompareDatas(rowDatas);
                }
            }
        }
    }

    /**
     * 将DataModel中的数据按照指定信息进行汇总
     * 
     * @param dataModel 待汇总的DataModel
     * @throws IllegalAccessException 无法按照指定列进行汇总
     */
    private static void buildSortSummary(DataModel dataModel) throws IllegalAccessException {
        List<HeadField> rowLeafs = getLeafNodeList(dataModel.getRowHeadFields());
        if (CollectionUtils.isNotEmpty(rowLeafs)) {
            if (dataModel.getOperateIndex() > rowLeafs.get(0).getCompareDatas().size()) {
                throw new IllegalAccessException("can not access operate index:" + dataModel.getOperateIndex());
            }
            for (HeadField rowHeadField : dataModel.getRowHeadFields()) {
                List<HeadField> leafFileds = rowHeadField.getLeafFileds(true);
                if (CollectionUtils.isNotEmpty(leafFileds)) {

                    Queue<HeadField> queue = new LinkedList<HeadField>(leafFileds);
                    while (!queue.isEmpty()) {
                        HeadField leafFiled = queue.remove();
                        if (CollectionUtils.isNotEmpty(leafFiled.getCompareDatas())) {
                            leafFiled.setSummarizeData(leafFiled.getCompareDatas().get(dataModel.getOperateIndex()));
                        }
                        HeadField parent = null;
                        if (leafFiled.getParent() != null) {
                            parent = leafFiled.getParent();
                        } else if (leafFiled.getParentLevelField() != null) {
                            parent = leafFiled.getParentLevelField();
                            if (!queue.contains(parent)) {
                                queue.add(parent);
                            }
                        }

                        if (parent != null && CollectionUtils.isEmpty(parent.getCompareDatas())) {
                            parent.setSummarizeData(BigDecimalUtils.addBigDecimal(parent.getSummarizeData(),
                                    leafFiled.getSummarizeData()));
                        }
                    }
                }

            }
        }
    }

    /**
     * 过滤DataModel中的空白行
     * 
     * @param dataModel 待过滤的DataModel
     * @return 过滤的行数
     */
    public static int filterBlankRow(DataModel dataModel) {
        int blankRowCount = 0;
        fillFieldData(dataModel, FillDataType.ROW);
        List<HeadField> leafFileds = getLeafNodeList(dataModel.getRowHeadFields());
        if (CollectionUtils.isNotEmpty(leafFileds)) {
            for (HeadField field : leafFileds) {
                boolean isBlank = true;
                for (BigDecimal data : field.getCompareDatas()) {
                    if (data != null) {
                        isBlank = false;
                        break;
                    }
                }
                if (isBlank) {
                    blankRowCount++;
                    removeField(field, dataModel.getRowHeadFields());
                }
            }
        }
        List<HeadField> summaryHeadFields = dataModel.getRowHeadFields();
        Iterator<HeadField> iterator = summaryHeadFields.iterator();
        while (iterator.hasNext()) {
            HeadField summaryHead = iterator.next();
            // 如果去除空白行之后，发现还剩下一个无意义的汇总节点，那么也需要将这个汇总节点remove掉   update by  majun
            if (CollectionUtils.isEmpty(summaryHead.getChildren())
                    && CollectionUtils.isEmpty(summaryHead.getNodeList())) {
                iterator.remove();
            }
        }
        fillColumnData(dataModel, FillDataType.ROW);
        logger.info ("[INFO] ---- ---- ---- ---- ---- after remove filter blank row ,current dataModel is : " + dataModel);
        return blankRowCount;
    }

    /**
     * 从当前节点的父节点中移除
     * 
     * @param field 待移除节点
     * @param rowHeadFields datamodel行上节点
     */
    private static void removeField(HeadField field, List<HeadField> rowHeadFields) {
        HeadField parentField = field.getParentLevelField();
        if (field.getParentLevelField() != null) {
            parentField.getNodeList().remove(field);
            if (parentField.getChildren ().isEmpty() && parentField.getNodeList ().isEmpty ()) {
                removeField(parentField, rowHeadFields);
            }
        } else {
            rowHeadFields.remove(field);
        }
        if (field.getParent() != null) {
            field.getParent().getChildren().remove(field);
        }
    }

    /**
     * 获取第一行的rowspan
     * 
     * @param rowHeadFields 行上的节点
     * @return 返回第一行的rowspan
     */
//    public static int getRowSpan(List<HeadField> rowHeadFields) {
//        if (CollectionUtils.isNotEmpty(rowHeadFields)) {
//            for (HeadField filed : rowHeadFields) {
//                return filed.getLeafSize();
//            }
//        }
//        return 0;
//    }

    /**
     * 把一个周的开始caption，换成一个完整周的caption
     * 
     * @param startDay 开始时间
     * @param pattern 时间的样式
     * @return 完整caption
     * @throws ParseException 异常
     */
//    public static String parseToEntireWeekCaption(String startDay, String pattern) throws ParseException {
//        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
//        Calendar calendar = new GregorianCalendar(2012, Calendar.JANUARY, 1);
//        calendar.setTime(sdf.parse(startDay));
//        calendar.add(Calendar.DAY_OF_MONTH, 6);
//        String endDay = sdf.format(calendar.getTime());
//        return startDay + "~" + endDay;
//    }

    /**
     * 获取List<HeadField>结构下的所有叶子节点
     * 
     * @param headFields 返回节点列表下的所有叶子
     * @return 叶子节点
     */
    public static List<HeadField> getLeafNodeList(List<HeadField> headFields) {
        List<HeadField> resultList = new ArrayList<HeadField>();

        for (HeadField headField : headFields) {
            resultList.addAll(headField.getLeafFileds(true));
        }
        return resultList;

    }

    /**
     * 给出任意一个headField的祖先链
     * 
     * @param headField 指定节点
     * @return 指定节点的从小到大的祖先链
     */
//    private static List<HeadField> getHeadFieldListOutofHeadField(HeadField headField) {
//        List<HeadField> resultList = new ArrayList<HeadField>();
//        if (headField == null) {
//            return resultList;
//        } else {
//            resultList.add(headField);
//            resultList.addAll(getHeadFieldListOutofHeadField(headField.getParentLevelField()));
//        }
//        return resultList;
//    }

    // /**
    // * 根据节点的唯一名称，获取这个节点对应的祖先链
    // * @param nodeUniqueName
    // * @return
    // */
    // public static List<HeadField> getHeadFieldListOutofHeadField(String nodeUniqueName){
    //
    //
    // }
    //
    // /**
    // * 根据节点的唯一名称获取节点
    // * @param nodeUniqueName
    // * @return
    // */
    // public static HeadField getHeadFieldByNodeUniqueName(String nodeUniqueName){
    //
    // }

    /**
     * 将节点的UniqueName转换成节点的节点的value列表
     * 
     * @param nodeUniqueName
     * @return
     */
    public static String[] parseNodeUniqueNameToNodeValueArray(String nodeUniqueName) {
        if (StringUtils.isBlank(nodeUniqueName)) {
            throw new IllegalArgumentException("node unique is illegal:" + nodeUniqueName);
        }
        String preSplitUniqueName = nodeUniqueName;
        if (preSplitUniqueName.startsWith("{")) {
            preSplitUniqueName = preSplitUniqueName.substring(1);
        }
        if (preSplitUniqueName.endsWith("}")) {
            preSplitUniqueName = preSplitUniqueName.substring(0, preSplitUniqueName.length() - 1);
        }
        // 先按照].[去截取，以后考虑更好方法
        return StringUtils.splitByWholeSeparator(preSplitUniqueName, "}.{");

    }

    /**
     * 递归计算节点的深度
     * 
     * @param headField 指定接待
     * @param indent 当前节点深度
     * @return 节点距离顶层的深度
     */
//    private static int getIndentOfHeadField(HeadField headField, int indent) {
//        if (headField != null && headField.getParent() != null) {
//            return getIndentOfHeadField(headField.getParent(), indent + 1);
//        } else {
//            return indent;
//        }
//    }

    /**
     * 获取节点和顶层节点的深度
     * 
     * @param headField 指定节点
     * @return 节点深度
     */
//    public static int getIndentOfHeadField(HeadField headField) {
//        return getIndentOfHeadField(headField, 0);
//    }

    /**
     * 将基于列存储的数据转换成基于行存储的结构
     * 
     * @param columnBasedData 列式存储数据
     * @return 行式存数数据
     */
//    public static List<List<BigDecimal>> transColumnBasedData2RowBasedData(List<List<BigDecimal>> columnBasedData) {
//        List<List<BigDecimal>> rowBasedData = new ArrayList<List<BigDecimal>>();
//
//        for (List<BigDecimal> currColumnData : columnBasedData) {
//            for (int i = 0; i < currColumnData.size(); i++) {
//                // 当前列的第i行
//                List<BigDecimal> currRowData = new ArrayList<BigDecimal>();
//                if (rowBasedData.size() >= i + 1) {
//                    currRowData = rowBasedData.get(i);
//                } else {
//                    rowBasedData.add(currRowData);
//                }
//
//                currRowData.add(currColumnData.get(i));
//
//            }
//        }
//
//        return rowBasedData;
//
//    }

    /**
     * 对DataModel进行排序
     * 
     * @param dataModel 待排序的DataModel
     * @param sortRecord 排序的信息
     */
    public static void sortDataModelBySort(DataModel dataModel, SortRecord sortRecord) {
        if (sortRecord != null && StringUtils.isNotBlank(sortRecord.getSortColumnUniquename())
                && !sortRecord.getSortType().equals(SortType.NONE)) {
            try {
                int index =
                        DataModelUtils.foundIndexByLeafeValue(dataModel.getColumnHeadFields(),
                        sortRecord.getSortColumnUniquename());
                dataModel.setOperateIndex(index);
                DataModelUtils.sortByRow(dataModel, sortRecord.getSortType());
            } catch (Exception e) {
                logger.warn("can not sort by  columnName:" + sortRecord.getSortColumnUniquename());
            }
        }
    }

    /**
     * 按照指定长度截取查询结果
     * @param oriModel 原始结果
     * @param recordSize 截取长度
     * @return DataModel 新数据模型
     */
    public static DataModel truncModel(DataModel oriModel,  int recordSize) {
        DataModel model = new DataModel();
        model.setColumnHeadFields(oriModel.getColumnHeadFields());
        model.setRecordSize(getRecordSize(oriModel.getColumnBaseData()));
        // 纪录数小于或等于指定行数
        if (model.getRecordSize() <= recordSize) {
            model.setColumnBaseData(oriModel.getColumnBaseData());
            model.setColumnHeadFields(oriModel.getColumnHeadFields());
            model.setOperateIndex(oriModel.getOperateIndex());
            model.setRowHeadFields(oriModel.getRowHeadFields());
            return model;
        }
        // 截取指定行数数据
        int tmp = recordSize;
        List<List<BigDecimal>> datas = Lists.newArrayList();
        oriModel.getColumnBaseData().forEach(list -> {
            List<BigDecimal> data = Lists.newArrayList();
            for (int i = 0; i < tmp; ++i) {
                data.add(list.get(i));
            }
            datas.add(data);
        });
        model.setColumnBaseData(datas);
        // 封装行头数据
        List<HeadField> rowHeadFields = Lists.newArrayList();
        int originRecord = recordSize;
        for (int i = 0; i < oriModel.getRowHeadFields().size() ; ++i) {
            // 处理第i个节点
            HeadField field = oriModel.getRowHeadFields().get(i);
            // 添加处理后的第一个节点到行头
            rowHeadFields.add(field);
            if (i == originRecord - 1) {
                break;
            }
            boolean hasNodeList = false;
            // 如果当前行的NodeList不等于空，先处理NodeList
            if (field.getNodeList() != null && field.getNodeList().size() > 0) {
                hasNodeList = true;
                HeadFieldDetailRecord record = truncFieldHeads(field.getNodeList(), recordSize);
                field.setNodeList(record.headFields);
                if (record.count >= recordSize) {
                    field.setChildren(Lists.newArrayList());
                    break;
                } else {
                    recordSize = recordSize - record.count;
                }
            }
            
            // 处理孩子节点
            if (field.getChildren() != null && field.getChildren().size() > 0) { // 处理孩子节点
                if (!hasNodeList) {
                    recordSize -= 1;
                }
                HeadFieldDetailRecord record = truncFieldHeads(field.getChildren(), recordSize);
                field.setChildren(record.headFields);
                if (record.count >= recordSize) {
                    break;
                } else {
                    recordSize = recordSize - record.count;
                }
            }
        }
        model.setRowHeadFields(rowHeadFields);
        return model;
    }

    /**
     * 
     * @param nodeList
     * @param recordSize
     * @return HeadFieldDetailRecord
     */
    private static HeadFieldDetailRecord truncFieldHeads(List<HeadField> nodeList, int recordSize) {
        int oriRecordSize = recordSize;
        List<HeadField> tmp = Lists.newArrayList();
        for (int i = 0; i < nodeList.size(); ++i) {
            if (recordSize <= 0) {
                break;
            }
            HeadField field = nodeList.get(i);
            // 没有NodeList，只有children
            if (field.getNodeList() == null || field.getNodeList().size() == 0) {
                // 没有孩子节点，直接返回
                if (field.getChildren() == null || field.getChildren().size() == 0) {
                    tmp.add(field);
                    recordSize -= 1;
                } else { // 没有NodeList，只有children
                    HeadFieldDetailRecord record = truncFieldHeads(field.getChildren(), recordSize - 1);
                    field.setChildren(record.headFields);
                    recordSize -= record.count;
                    tmp.add(field);
                    if (recordSize <= 0) {
                        recordSize = 0;
                        break;
                    }
                }
            } else {
                HeadFieldDetailRecord record = truncFieldHeads(field.getNodeList(), recordSize);
                field.setNodeList(record.headFields);
                recordSize -= record.count;
                tmp.add(field);
                if (recordSize <= 0) {
                    recordSize = 0;
                    field.setChildren(Lists.newArrayList());
                    break;
                }
            }
        }
        return new HeadFieldDetailRecord(oriRecordSize - recordSize, tmp);
    }

    /**
     * 获取结果集原始纪录数
     * @param columnBaseData 基于列的结果集数据
     * @return 纪录数
     */
    private static int getRecordSize(List<List<BigDecimal>> columnBaseData) {
        if (columnBaseData == null || columnBaseData.size() == 0) {
            return 0;
        }
        if (columnBaseData.get(0) == null) {
            return 0;
        }
        return columnBaseData.get(0).size();
    }
    
    /**
     * isChar
     *
     * @param dateType
     * @return
     */
    public static boolean isChar(String dateType) {
        // 目前只日志mysql协议，没有对其他数据源类型兼容
        if (StringUtils.isEmpty(dateType) 
                || dateType.toLowerCase().contains("char")
                || dateType.toLowerCase().contains("time")
                || dateType.toLowerCase().contains("text")
                || dateType.toLowerCase().contains("date")
                || dateType.toLowerCase().contains("blob")) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * 
     * @author david.wang
     *
     */
    private static class HeadFieldDetailRecord {
        
        /**
         * 
         */
        public final int count;
        
        /**
         * 
         */
        public final List<HeadField> headFields;
        
        /**
         * 
         * @param count
         * @param headFields
         */
        public HeadFieldDetailRecord(int count, List<HeadField> headFields) {
            super();
            this.count = count;
            this.headFields = headFields;
        }
        
    }

//	public static void sortDataModelBySort(DataModel dataModel,
//			SortRecord sortRecord, Comparator<HeadField> headFieldComparator) {
//		if (sortRecord != null && StringUtils.isNotBlank(sortRecord.getSortColumnUniquename())
//                && !sortRecord.getSortType().equals(SortType.NONE)) {
//            try {
//                int index =
//                        DataModelUtils.foundIndexByLeafeValue(dataModel.getColumnHeadFields(),
//                                sortRecord.getSortColumnUniquename());
//                dataModel.setOperateIndex(index);
//                sortByRow(dataModel, sortRecord.getSortType(), headFieldComparator);
//            } catch (Exception e) {
//                logger.warn("can not sort by  columnName:" + sortRecord.getSortColumnUniquename());
//            }
//        }
//	}

//	private static void sortByRow(DataModel dataModel, SortType sort,
//			Comparator<HeadField> headFieldComparator) throws IllegalAccessException {
//		if (dataModel == null) {
//            throw new IllegalArgumentException("can not sort empty dataModel!");
//        }
//        fillFieldData(dataModel, FillDataType.ROW);
//        buildSortSummary(dataModel);
//        sortListHeadFields(dataModel.getRowHeadFields(), sort, headFieldComparator);
//        fillColumnData(dataModel, FillDataType.ROW);
//	}

//	private static void sortListHeadFields(List<HeadField> headFields,
//			SortType sort, Comparator<HeadField> headFieldComparator) {
//		 if (CollectionUtils.isNotEmpty(headFields)) {
//	            for (HeadField filed : headFields) {
//	                sortListHeadFields(filed.getNodeList(), sort, headFieldComparator);
//	                sortListHeadFields(filed.getChildren(), sort, headFieldComparator);
//	                for (HeadField child : filed.getChildren()) {
//	                    sortListHeadFields(child.getNodeList(), sort, headFieldComparator);
//	                }
//	            }
//	            Collections.sort(headFields, headFieldComparator);
//	        }
//	}

}
