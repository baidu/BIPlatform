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
package com.baidu.rigel.biplatform.ma.resource.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.baidu.rigel.biplatform.ac.minicube.CallbackLevel;
import com.baidu.rigel.biplatform.ac.minicube.MiniCube;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.OlapElement;
import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.query.data.HeadField;
import com.baidu.rigel.biplatform.ac.query.data.TableData;
import com.baidu.rigel.biplatform.ac.query.data.TableData.Column;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ac.util.UnicodeUtils;
import com.baidu.rigel.biplatform.ma.model.consts.Constants;
import com.baidu.rigel.biplatform.ma.model.utils.GsonUtils;
import com.baidu.rigel.biplatform.ma.report.exception.PivotTableParseException;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaType;
import com.baidu.rigel.biplatform.ma.report.model.FormatModel;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.LinkInfo;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.query.QueryAction;
import com.baidu.rigel.biplatform.ma.report.query.QueryAction.OrderDesc;
import com.baidu.rigel.biplatform.ma.report.query.newtable.bo.OperationColumnDefine;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.CellData;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.ColDefine;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.ColField;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.PivotTable;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.PlaneTable;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.PlaneTableColDefine;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.RowDefine;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.RowHeadField;
import com.baidu.rigel.biplatform.ma.report.utils.ItemUtils;
import com.baidu.rigel.biplatform.ma.report.utils.QueryUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 类DataModelUtils.java的实现描述：DataModel操作工具类
 * 
 * @author xiaoming.chen 2013-12-5 下午2:51:58
 */
public final class DataModelUtils {

    /**
     * logger
     */
    public static final Logger LOG = LoggerFactory.getLogger(DataModelUtils.class);

    /**
     * memeber的uniqname
     */
    public static final String EXT_INFOS_MEM_UNIQNAME = "mem_uniqname";

    /**
     * member的dim
     */
    public static final String EXT_INFOS_MEM_DIMNAME = "mem_dimname";

    /**
     * member的leveltype
     */
    public static final String EXT_INFOS_MEM_LEVELTYPE = "mem_leveltype";

    /**
     * member的child信息
     */
    public static final String EXT_INFOS_MEM_HASCHILD = "mem_haschild";

    /**
     * DIV_DIM
     */
    public static final String DIV_DIM = "_12345FORDIV_";

    /**
     * POS_PREFIX
     */
    public static final String POS_PREFIX = "[Rights]";

    /**
     * POS_DIV
     */
    public static final String POS_DIV = "_=_";

    /**
     * DIV_DIM_NODE
     */
    public static final String DIV_DIM_NODE = "  -  ";

    /**
     * DIV_NODE
     */
    public static final String DIV_NODE = ": ";

    /**
     * HTTP_CACHE_KEY_GENERATE_FACTOR_PAIR
     */
    public static final String HTTP_CACHE_KEY_GENERATE_FACTOR_PAIR = "*_^";

    /**
     * PARAM_VALUES_SPLIT
     */
    public static final String PARAM_VALUES_SPLIT = "^_^";

    /**
     * REPORT_IMAGE_POSTFIX
     */
    public static final String REPORT_IMAGE_POSTFIX = "_*_imageof_*_";

    /**
     * USE_IMAGE
     */
    public static final String USE_IMAGE = "useImage";

    /**
     * member节点是否已经展开
     */
    public static final String EXT_INFOS_MEM_EXPAND = "mem_expand";

    /**
     * DataModelUtils
     */
    private DataModelUtils() {

    }
    

    /**
     * 下钻时，需要考虑级联下拉框用到的维度组组合下钻场景，如果带有父节点，需要截取掉父节点的datamodel数据才能进行merage update by majun
     * 
     * @param dm4Merage dm4Merage
     * @param uniqueName uniqueName
     * @param condition condition
     * @return 返回merge完的 DataModel
     */
    public static DataModel truncationDataModel(DataModel dm4Merage, String uniqueName, String condition) {
        HeadField fisrtHf = dm4Merage.getRowHeadFields().get(0);

        if (MetaNameUtil.isAllMemberUniqueName(fisrtHf.getValue()) && (condition.split("\\}").length == 1)) {
            // String uniqueName4split = String.valueOf(queryParams.get("uniqueName"));
            int subCount = MetaNameUtil.parseUnique2NameArray(uniqueName).length - 1;
            for (int i = 0; i < subCount - 1; i++) {
                HeadField df = fisrtHf.getChildren().get(0);
                fisrtHf = df;
            }
            dm4Merage.setRowHeadFields(fisrtHf.getChildren());
            dm4Merage.setRecordSize(dm4Merage.getRecordSize() - subCount);
            // 去掉一层父级，列数据也需要对应去掉一级
            for (List<BigDecimal> baseDataList : dm4Merage.getColumnBaseData()) {
                for (int i = 0; i < subCount; i++) {
                    baseDataList.remove(0);
                }
            }
        }
        return dm4Merage;
    }
    /**
     * 将DataModel转换成前端展现需要的PivotTable
     * 
     * @param oriDataModel 待转换的DataModel
     * @param needLimit 是否需要限制输出结果
     * @param limitSize 限制的大小
     * @param hideWhiteRow 是否隐藏空白行
     * @return 转换后的PivotTable
     * @throws Exception
     */
    /**
     * @param cube
     * @param oriDataModel
     * @param needLimit
     * @param limitSize
     * @param hideWhiteRow
     * @param logicModel
     * @return
     * @throws PivotTableParseException
     */
    public static PivotTable transDataModel2PivotTable(Cube cube, DataModel oriDataModel, boolean needLimit,
            int limitSize, boolean hideWhiteRow, LogicModel logicModel) throws PivotTableParseException {

        PivotTable pTable = new PivotTable();
        if (oriDataModel == null) {
            return pTable;
        }
        long current = System.currentTimeMillis();
        DataModel dataModel = oriDataModel;
        if (hideWhiteRow) {
            try {
                dataModel = (DataModel) DeepcopyUtils.deepCopy(oriDataModel);
            } catch (Exception e) {
                LOG.error("Fail in deepCopy datamodel. ");
                PivotTableParseException parseEx = new PivotTableParseException(e);
                throw parseEx;
            }
        }
        List<HeadField> colHeadFields = dataModel.getColumnHeadFields();
        List<HeadField> rowHeadFields = dataModel.getRowHeadFields();

        // build colField
        List<List<ColField>> colFields = new ArrayList<List<ColField>>();

        int colHeight = getHeightOfHeadFieldList(colHeadFields);
        pTable.setColHeadHeight(colHeight);
        // s2. trans colField
        colFields = transColHeadFields2ColFields(colHeadFields);
        // s1. calc colHeight
        // s2. trans colField
        // s3. if rowAxis's exists,fill the first col of colFields
        String[] dimCaptions = getDimCaptions(cube, logicModel); // getDimCaptions(cube, rowHeadFields);
        int rowWidth = getHeightOfHeadFieldList(rowHeadFields);
        if (rowHeadFields != null && rowHeadFields.size() != 0) {
            List<ColField> firstColFields = colFields.get(0);
            for (int i = 0; i < dimCaptions.length; i++) {
                ColField firstColField = new ColField();
                firstColField.setRowspan(colHeight);
                firstColField.setColSpan(1);
                firstColField.setUniqName("test");
                firstColField.setV(dimCaptions.length > i ? dimCaptions[i] : "");
                firstColFields.add(i, firstColField);
            }
        }
        pTable.setColFields(colFields);

        // build colDefine
        List<ColDefine> colDefineList = new ArrayList<ColDefine>(); // 长度即列数即宽度
        // 获取叶子节点
        List<HeadField> leafNodeList = getLeafNodeList(colHeadFields);
        Cube tempCube = QueryUtils.transformCube(cube);
        for (HeadField headField : leafNodeList) {
            ColDefine colDefine = new ColDefine();
            colDefine.setUniqueName(headField.getValue());
            colDefine.setCaption(transStrList2Str(getAllCaptionofHeadField(headField), "-", true));
            colDefine.setShowUniqueName(transStrList2Str(getAllMemberDimConcatUniqname(headField), DIV_DIM, true));
            // membershowname,当前member的caption
            colDefine.setShowAxis(transStrList2Str(getAllCaptionofHeadField(headField), DIV_DIM_NODE, true));
            // 将形如[measure].[show]的UniqueName中取得该指标在模型定义中的olapElementId，然后返回给前端做判断跳转和传参之用
            String measureName = MetaNameUtil.getNameFromMetaName(headField.getValue());
            String measureNameId = "";
            if (tempCube.getMeasures().get(measureName) != null) {
                measureNameId = tempCube.getMeasures().get(measureName).getId();
            }
            colDefine.setOlapElementId(measureNameId);

            Map<String, Object> extInfos = headField.getExtInfos();
            colDefine.setCurrentSort(extInfos.get("sortType") == null ? "NONE" : extInfos.get("sortType").toString());
            colDefineList.add(colDefine);

        }
        pTable.setColDefine(colDefineList);

        // build rowDefine;
        // s1. calc actual size
        // s2. fill rowDefine
        List<RowDefine> rowDefineList = new ArrayList<RowDefine>();
        if (rowHeadFields == null) {
            rowHeadFields = Lists.newArrayList();
        }
        List<HeadField> rowLeafNodeList = getLeafNodeList(rowHeadFields);
        int maxRowSpan = getRowSpan(rowHeadFields);
        if (needLimit && limitSize != 0 && maxRowSpan > 1) {
            int count = 1;
            while (maxRowSpan * (count + 1) < limitSize) {
                count++;
            }
            limitSize = maxRowSpan * count;
        }

        // int actualSize=getLeafFileds(rowHeadFields).size();
        pTable.setActualSize(rowLeafNodeList.size());
        int count = 0;
        for (HeadField headField : rowLeafNodeList) {

            RowDefine rowDefine = new RowDefine();
            /**
             * TODO 删除
             */
            String lineUniqueName = headField.getNodeUniqueName();
            rowDefine.setUniqueName(lineUniqueName);
            rowDefine.setShowXAxis(transStrList2Str(getAllCaptionofHeadField(headField), DIV_DIM_NODE, true));
            /**
             * 默认第一行是选中的
             */
            if (count == 0) {
                rowDefine.setSelected(true);
            }
            rowDefineList.add(rowDefine);

            // 增加展现条数限定
            count++;
            if (needLimit && count >= limitSize) {
                break;
            }
        }
        pTable.setRowDefine(rowDefineList);

        // build rowHeadFields;
        // s1. fill rowFields
        List<List<RowHeadField>> rowFields = new ArrayList<List<RowHeadField>>();

        rowFields = transRowHeadFields2RowFields(rowHeadFields, needLimit, limitSize, cube);
        modify(rowFields);
        pTable.setRowHeadFields(rowFields);
        pTable.setRowHeadWidth(rowWidth);
        // 按展现条数截取columnBaseData
        List<List<BigDecimal>> source = dataModel.getColumnBaseData();
        List<List<CellData>> cellDataSource = parseCellDatas(source);
        List<List<CellData>> columnBasedData = getColumnBasedDataCut(cellDataSource, needLimit, limitSize);

        // build cellDataSetRowBased;
        List<List<CellData>> rowBasedData = transColumnBasedData2RowBasedData(columnBasedData);
        if (rowBasedData.size() > 1 || !hasSumRow(rowFields)) {
            pTable.setDataSourceRowBased(rowBasedData);
            // build cellDataSetColumnBased;
            pTable.setDataSourceColumnBased(columnBasedData);
        }

        // build stat;
        pTable.setDataColumns(pTable.getDataSourceColumnBased().size());
        pTable.setDataRows(pTable.getDataSourceRowBased().size());

        LOG.info("transfer datamodel 2 pivotTable cost:" + (System.currentTimeMillis() - current) + "ms!");

        // PivotTableUtils.addSummaryRowHead(pTable);
        pTable.setOthers(oriDataModel.getOthers());
        if (pTable.getRowHeadFields() == null) {
            pTable.setRowHeadFields(Lists.newArrayList());
        }
        return pTable;
    }

    public static String[] getDimCaptions(Cube cube, LogicModel logicModel) {
        if (cube == null || logicModel == null) {
            return new String[0];
        }
        Item[] rows = logicModel.getRows();
        if (rows == null || rows.length == 0) {
            return new String[0];
        }
        String[] rs = new String[rows.length];
        for (int i = 0; i < rows.length; ++i) {
            rs[i] = cube.getDimensions().get(rows[i].getOlapElementId()).getCaption();
        }
        return rs;
    }

    /**
     * 将DataModel转为PlaneTable平面表
     * 
     * @param logicModel 逻辑模型
     * @param formatModel 格式模型
     * @param cube cube 立方体
     * @param dataModel 数据模型
     * @return
     */
    public static PlaneTable transDataModel2PlaneTable(Cube cube, DataModel dataModel, LogicModel logicModel,
            FormatModel formatModel, QueryAction queryAction) {
        PlaneTable planeTable = new PlaneTable();
        if (dataModel == null) {
            return planeTable;
        }
        if (formatModel == null) {
            return planeTable;
        }
        if (logicModel == null) {
            return planeTable;
        }
        // 记录转换时间
        long current = System.currentTimeMillis();
        // 获取数据模型中的表定义
        TableData tableData = dataModel.getTableData();
        if (tableData == null) {
            return planeTable;
        }
        List<String> keys = getKeysInOrder(cube, logicModel, tableData.getColumns());
        // 表的列定义
        List<Column> columns = tableData.getColumns();
        // 设置平面表列属性信息
        planeTable.setColDefines(getColDefinesInOrder(cube, logicModel, columns, formatModel, queryAction));
        planeTable.setOperationColumnDefine(generateOperationColumnList(formatModel.getLinkInfo()));
        // 表的数据定义
        Map<String, List<String>> data = tableData.getColBaseDatas();
        List<Map<String, String>> planeTableData = Lists.newArrayList();

        // 本次查询总的数据条数
        int totalRecordSize = 0;

        // TODO 优化，对数据进行遍历，并转换
        if (data != null && data.size() != 0) {
            // 获取总的数据条数
            totalRecordSize = getTotalRecordSizeOfPlaneTable(data);
            // 将以列存储的数据转为以行存储
            planeTableData = transPlaneTableFromCol(totalRecordSize, data, keys, cube, logicModel);
        }
        // 设置平面表数据信息
        planeTable.setData(planeTableData);
        // 设置平面表总的数据条数大小
        planeTable.getPageInfo().setTotalRecordCount(dataModel.getRecordSize());
        LOG.info("transfer datamodel 2 planeTable cost:" + (System.currentTimeMillis() - current) + "ms!");
        LOG.info("the planeTable info is " + GsonUtils.toJson(planeTable));
        return planeTable;
    }

    /**
     * 获取正确的列属性信息，并设置相应的属性条件
     * 
     * @param cube
     * @param logicModle
     * @param columns
     * @param formatModel
     * @param queryAction
     * @return
     */
    private static List<PlaneTableColDefine> getColDefinesInOrder(Cube cube, LogicModel logicModel,
            List<Column> columns, FormatModel formatModel, QueryAction queryAction) {

        List<PlaneTableColDefine> colDefines = Lists.newArrayList();
        // 分别获取数据模型、提示信息、文本对齐信息
        Map<String, String> dataFormat = formatModel.getDataFormat();
        Map<String, String> toolTips = formatModel.getToolTips();
        Map<String, String> textAlignFormat = formatModel.getTextAlignFormat();
        List<String> keys = getKeysInOrder(cube, logicModel, columns);
        Item[] items = logicModel.getColumns();

        // 获取排序维度或者指标
        OrderDesc orderDesc = queryAction.getOrderDesc();
        // Item的索引
        int itemIndex = 0;
        // 是否已经设置排序列
        boolean setOrder = false;
        // 构建列属性
        for (String key : keys) {
            for (Column column : columns) {
                if (column.key.equals(key)) {
                    PlaneTableColDefine colDefine = new PlaneTableColDefine();
                    // 设置列的id
                    colDefine.setId(items[itemIndex].getOlapElementId());
                    // 设置表头
                    colDefine.setTitle(column.caption);
                    // 设置表域名称
//                    String displayKey = StringUtils.replace(StringUtils
//                            .replace(column.key, "[Dimension].", ""), "[Measure].", "");
                    colDefine.setField(column.key);
                    // 设置linkBridge
                    Map<String, LinkInfo> linkInfoMap = formatModel.getLinkInfo();
                    String linkBridgeId = items[itemIndex].getOlapElementId();
                    if (!StringUtils.isEmpty(linkBridgeId)) {
                        LinkInfo linkInfo = linkInfoMap.get(linkBridgeId);
                        // 这里严格判断，只有当设置了明细跳转表，并且参数映射也已经设置完成，才在多维报表处展示超链接
                        if (linkInfo != null && !StringUtils.isEmpty(linkInfo.getTargetTableId())
                                && !MapUtils.isEmpty(linkInfo.getParamMapping())) {
                            colDefine.setLinkBridge(linkBridgeId);
                        }

                    }
                    String name = column.name;

                    // 设置提示信息
                    if (toolTips != null) {
                        String tips = toolTips.get(name);
                        if (StringUtils.isEmpty(tips)) {
                            tips = toolTips.get(column.tableName + "_" + column.name);
                            if (StringUtils.isEmpty(tips)) {
                                tips = name;
                            }
                        }
                        colDefine.setToolTip(tips);
                    }
                    // 设置文本对齐信息
                    if (textAlignFormat != null) {
                        String align = textAlignFormat.get(name);
                        if (StringUtils.isEmpty(align)) {
                            align = textAlignFormat.get(column.tableName + "_" + column.name);
                            if (StringUtils.isEmpty(align)) {
                                align = "left";
                            }
                        }
                        colDefine.setAlign(align);
                    }
                    boolean isMeasure = isMeasure(column.tableName + "." + column.name, cube);
                    if (isMeasure) {
                        if (dataFormat != null) {
                            String formatStr = dataFormat.get("defaultFormat");
                            if (!StringUtils.isEmpty(dataFormat.get(name))) {
                                formatStr = dataFormat.get(name);
                            }
                            if (!StringUtils.isEmpty(formatStr)) {
                                colDefine.setFormat(formatStr);
                            }
                        }
                        // 设置排序信息
                        if (orderDesc != null && column.name.equals(orderDesc.getName()) && !setOrder) {
                            colDefine.setOrderby(orderDesc.getOrderType().toLowerCase());
                            setOrder = true;
                        }
                    } else {
                        // TODO 之后需要修改
                        if (orderDesc != null
                                && ((column.tableName + "_" + column.name).equals(orderDesc.getName()) || column.name
                                        .equals(orderDesc.getName())) && !setOrder) {
                            colDefine.setOrderby(orderDesc.getOrderType().toLowerCase());
                            setOrder = true;
                        }
                        colDefine.setFormat(null);
                    }
                    // 设置维度、指标信息
                    colDefine.setIsMeasure(isMeasure);
                    // 添加到列属性信息列表中
                    colDefines.add(colDefine);
                    break;
                }
            }
            // 更新索引
            itemIndex++;
        }
        return colDefines;
    }

    /**
     * 判断某个名称对应的字段是维度还是指标
     * 
     * @param name
     * @param cube
     * @return
     */
    public static boolean isMeasure(String name, Cube cube) {
        MiniCube miniCube = (MiniCube) cube;
        String colName = "";
        try {
            colName = name.split("\\.")[1];
        } catch (Exception e) {
            colName = name;
        }
        Cube cubeNew = QueryUtils.transformCube(miniCube);
        return cubeNew.getMeasures().get(colName) != null;
    }

    /**
     * 获取平面表数据记录条数
     * 
     * @param data
     * @return
     */
    private static int getTotalRecordSizeOfPlaneTable(Map<String, List<String>> data) {
        int totalRecordSize = 0;
        // 获取数据条数的大小，所有Key对应的数据条目条数一致
        for (String key : data.keySet()) {
            List<String> values = data.get(key);
            totalRecordSize = values.size();
            break;
        }
        return totalRecordSize;
    }

    /**
     * 将平面表数据由基于列存储方式转为基于行存储方式
     * 
     * @param totalRecordSize 总的数据条数
     * @param data 数据
     * @param cube 立方体
     * @param logicModel 逻辑模型，用于控制转换后的顺序
     * @return
     */
    private static List<Map<String, String>> transPlaneTableFromCol(int totalRecordSize,
            Map<String, List<String>> data, List<String> keys, Cube cube, LogicModel logicModel) {

        List<Map<String, String>> planeTableData = Lists.newArrayList();
        for (int i = 0; i < totalRecordSize; i++) {
            Map<String, String> value = Maps.newLinkedHashMap();
            for (String key : keys) {
                try {
//                    String displayKey = StringUtils.replace(StringUtils
//                            .replace(key, "[Dimension].", ""), "[Measure].", "");
                    value.put(key, UnicodeUtils.unicode2String(data.get(key).get(i)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            planeTableData.add(value);
        }
        return planeTableData;
    }

    /**
     * 获取平面表DataModel中数据的正确的key顺序
     * 
     * @param cube
     * @param logicModel
     * @param columns
     * @return
     */
    private static List<String> getKeysInOrder(Cube cube, LogicModel logicModel, List<Column> columns) {
        // 获取Cube中的维度信息
        // Map<String, Dimension> dimensions = miniCube.getDimensions();
        // // 获取Cube中的指标信息
        // Map<String, Measure> measures = miniCube.getMeasures();
        final Map<String, String> tmp = Maps.newHashMap();
        columns.forEach(col -> {
            tmp.put(col.tableName + "." + col.name, col.key);
        });
        // 纵轴
        Item[] cols = logicModel.getColumns();
        // 存储列的key，key = 表明.列名
        List<String> keys = Lists.newArrayList();
        for (Item col : cols) {
            // boolean finished = false;
            // 处理维度
            OlapElement ele = ItemUtils.getOlapElementByItem(col, cube.getSchema(), cube.getId());
            if (ele != null) {
                keys.add(getRealKey(ele, cube, tmp));
            } else {
                LOG.debug("can't get olap element with cube id : " + cube.getId());
                throw new RuntimeException("can't get olap element with cube id : " + cube.getId());
            }
            // for (Dimension dimension : dimensions.values()) {
            // if (dimension.getType() == DimensionType.TIME_DIMENSION
            // && dimension.getId().equals(col.getOlapElementId())) {
            // // 如果为时间维度，转换成事实表的时间字段
            // keys.add(((MiniCube) cube).getSource() + "." + dimension.getFacttableColumn());
            // finished = true;
            // break;
            // }
            // if (dimension.getId().equals(col.getOlapElementId())) {
            // Level l = dimension.getLevels ().values ().toArray (new Level[0])[0];
            // keys.add(l.getDimTable () + "." + l.getName ());
            // finished = true;
            // break;
            // }
            // }
            // if (!finished) {
            // // 处理指标
            // for (Measure measure : measures.values()) {
            // if (measure.getId().equals(col.getOlapElementId())) {
            // keys.add(((MiniCube)cube).getSource() + "." + measure.getName());
            // break;
            // }
            // }
            // }
        }
        return keys;
    }
    
    /**
     * 获取平面表DataModel中数据的正确的key顺序
     * 
     * @param cube
     * @param logicModel
     * @param columns
     * @return
     */
    public static List<String> getKeysInOrder(Cube cube, LogicModel logicModel) {
        // 获取Cube中的维度信息
        // 纵轴
        Item[] cols = logicModel.getColumns();
        // 存储列的key，key = 表明.列名
        List<String> keys = Lists.newArrayList();
        for (Item col : cols) {
            // boolean finished = false;
            // 处理维度
            OlapElement ele = ItemUtils.getOlapElementByItem(col, cube.getSchema(), cube.getId());
            if (ele != null) {
                keys.add(getRealKey(ele, cube));
            } else {
                LOG.debug("can't get olap element with cube id : " + cube.getId());
                throw new RuntimeException("can't get olap element with cube id : " + cube.getId());
            }
        }
        return keys;
    }

    /**
     * 获取数据存储对应的key值信息 getRealKey
     * 
     * @param ele olapElement
     * @param cube 立方体信息
     * @param tmp key值map
     * @return
     */
    private static String getRealKey(OlapElement ele, Cube cube, Map<String, String> tmp) {
        return tmp.get(getRealKey(ele, cube));
    }
    
    /**
     * 获取数据存储对应的key值信息 getRealKey
     * 
     * @param ele olapElement
     * @param cube 立方体信息
     * @param tmp key值map
     * @return
     */
    private static String getRealKey(OlapElement ele, Cube cube) {
        String tmpKey = null;
        // 如果是维度
        if (ele instanceof Dimension) {
            Dimension dim = (Dimension) ele;
            Level level = dim.getLevels().values().toArray(new Level[0])[0];
            // 普通维度
            tmpKey = level.getDimTable() + "." + level.getName();
        } else {
            // 如果是指标
            tmpKey = ((MiniCube) cube).getSource() + "." + ((Measure) ele).getDefine();
        }
        return tmpKey;
    }

    private static boolean hasSumRow(List<List<RowHeadField>> rowFields) {
        if (rowFields == null) {
            return false;
        }
        if (CollectionUtils.isEmpty(rowFields)) {
            return false;
        }
        if (CollectionUtils.isEmpty(rowFields.get(0))) {
            return false;
        }
        RowHeadField firstRow = rowFields.get(0).get(0);
        if (MetaNameUtil.isAllMemberUniqueName(firstRow.getUniqueName())) {
            return true;
        }
        return firstRow.getV() != null && firstRow.getV().contains("合计");
    }

    // /**
    // *
    // * @param cube
    // * @param rowHeadFields
    // * @return String[]
    // */
    // private static String[] getDimCaptions(Cube cube, List<HeadField> rowHeadFields) {
    // List<String> captions = Lists.newArrayList();
    // if (CollectionUtils.isEmpty(rowHeadFields)) {
    // return new String[] {};
    // }
    // HeadField headField = rowHeadFields.get(0);
    // // for (HeadField headField : rowHeadFields) {
    // if (!CollectionUtils.isEmpty(headField.getNodeList())) {
    // Collections.addAll(captions, getDimCaptions(cube, headField.getNodeList()));
    // }
    // String uniqueName = headField.getNodeUniqueName();
    // // TODO 这里有问题，需要重新考虑
    // if ("合计".equals(headField.getCaption())) {
    // uniqueName = headField.getChildren().get(0).getValue();
    // } else {
    // uniqueName = headField.getValue();
    // }
    // String dimName = MetaNameUtil.getDimNameFromUniqueName(uniqueName);
    // captions.add(getDimensionCaptionByName(cube, dimName));
    // return captions.toArray(new String[0]);
    // }

    // /**
    // *
    // * @param cube
    // * @param dimName
    // * @return
    // */
    // private static String getDimensionCaptionByName(Cube cube, String dimName) {
    // for (Dimension dim : cube.getDimensions().values()) {
    // if (dim.getName().equals(dimName)) {
    // return dim.getCaption();
    // }
    // }
    // return dimName;
    // }

    /**
     * @param rowFields
     */
    private static void modify(List<List<RowHeadField>> rowFields) {
        /**
         * 设置默认的下钻、展开策略
         */
        if (CollectionUtils.isEmpty(rowFields)) {
            return;
        }

        int rowHeadWith = rowFields.get(0).size();
        for (List<RowHeadField> rowHeads : rowFields) {
            if (CollectionUtils.isEmpty(rowHeads)) {
                return;
            }
            int rowSize = rowHeads.size();
            int strategyIndex = rowHeadWith - rowSize;
            for (int i = strategyIndex; i < rowSize; i++) {
                RowHeadField rowHead = rowHeads.get(i);
                if (rowHeadWith != 1 && i == 0) {
                    /**
                     * 多个维度中的第一个维度，用链接下钻方式
                     */
                    if (rowHead.getExpand() != null && rowHead.getExpand()) {
                        /**
                         * 原来是加号的，要设置成链接下钻为true，否则为false
                         */
                        rowHead.setDrillByLink(true);
                        rowHead.setExpand(null);
                    } else {
                        rowHead.setDrillByLink(false);
                        rowHead.setExpand(null);
                    }
                } else {
                    rowHead.setDrillByLink(false);
                    if (i == 0 && rowHead.getIndent() == 0) {
                        rowHead.setExpand(null);
                    }
                }
            }
        }
    }

    private static List<List<CellData>> parseCellDatas(List<List<BigDecimal>> source) {
        List<List<CellData>> cellDatas = Lists.newArrayList();
        for (List<BigDecimal> sourcePiece : source) {
            List<CellData> cellRow = Lists.newArrayList();
            for (BigDecimal data : sourcePiece) {
                cellRow.add(parseCellData(data));
            }
            cellDatas.add(cellRow);
        }
        return cellDatas;
    }

    private static CellData parseCellData(BigDecimal value) {
        CellData data = new CellData();
        data.setCellId("");
        data.setFormattedValue("I,III.DD");
        if (value != null) {
            value = value.setScale(8, RoundingMode.HALF_UP);
            data.setV(value);
        } else {
            data.setV(value);
        }
        return data;
    }

    /**
     * 转换数据
     * 
     * @param columnBasedData
     * @return List<List<CellData>>
     */
    private static List<List<CellData>> transColumnBasedData2RowBasedData(List<List<CellData>> columnBasedData) {
        List<List<CellData>> rowBasedData = new ArrayList<List<CellData>>();

        for (List<CellData> currColumnData : columnBasedData) {
            for (int i = 0; i < currColumnData.size(); i++) {
                // 当前列的第i行
                List<CellData> currRowData = new ArrayList<CellData>();
                if (rowBasedData.size() >= i + 1) {
                    currRowData = rowBasedData.get(i);
                } else {
                    rowBasedData.add(currRowData);
                }

                currRowData.add(currColumnData.get(i));

            }
        }

        return rowBasedData;

    }

    /**
     * 
     * @param headFields
     * @return
     */
    private static boolean ifAllHeadFieldsHasSubChild(List<HeadField> headFields) {
        boolean result = false;
        for (HeadField headField : headFields) {
            if (headField != null && (headField.getNodeList().size() > 0 || headField.getChildren().size() > 0)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * 
     * @param columnBasedData
     * @param needLimit
     * @param limitSize
     * @return
     */
    private static List<List<CellData>> getColumnBasedDataCut(List<List<CellData>> columnBasedData, boolean needLimit,
            int limitSize) {
        if (!needLimit) {
            return columnBasedData;
        }

        List<List<CellData>> result = new ArrayList<List<CellData>>();

        for (List<CellData> currList : columnBasedData) {
            if (currList.size() > limitSize) {
                currList = currList.subList(0, limitSize);
            }
            if (needLimit && limitSize > 0 && result.size() >= limitSize) {
                break;
            }
            result.add(currList);

        }
        return result;
    }

    /**
     * 
     * @param rowHeadFields
     * @param needLimit
     * @param limitSize
     * @param cube
     * @return
     */
    private static List<List<RowHeadField>> transRowHeadFields2RowFields(List<HeadField> rowHeadFields,
            boolean needLimit, int limitSize, Cube cube) {
        List<List<RowHeadField>> rowFieldList = new ArrayList<List<RowHeadField>>();
        // int rowHeight=getHeightOfHeadFieldList(rowHeadFields);
        if (rowHeadFields == null || rowHeadFields.size() == 0) {
            return null;
        }
        // List<String> allMemUniqNameList=getAllMemUniqNameList(rowHeadFields);
        // int rowWidth=DataModelUtils.getLeafFileds(rowHeadFields).size();
        List<HeadField> leafFileds = DataModelUtils.getLeafNodeList(rowHeadFields);
        SimpleDateFormat src = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat target = new SimpleDateFormat("yyyy-MM-dd");
        List<HeadField> ancestorFileds = null;
        // hasStoredMap用于记录已经存过的rowField
        Map<String, HeadField> hasStoredMap = new HashMap<String, HeadField>();
        for (int j = 0; j < leafFileds.size(); ++j) {
            HeadField filed = leafFileds.get(j);
            ancestorFileds = getHeadListOutofHead(filed);
            Collections.reverse(ancestorFileds);
            List<RowHeadField> idxRowField = new ArrayList<RowHeadField>();
            for (int i = 0; i < ancestorFileds.size(); i++) {
                HeadField headField = ancestorFileds.get(i);
                if (i == 0 && hasStoredMap.get(headField.getValue()) != null) {
                    if (headField.getParent() == null) {
                        continue;
                    } else if (headField.getParent().equals(hasStoredMap.get(headField.getValue()).getParent())
                            && headField.getParent().getParent() == null) {
                        continue;
                    }
                } else {
                    hasStoredMap.put(headField.getValue(), headField);
                }

                RowHeadField rowField = new RowHeadField();
                // List<HeadField> tmpList=new ArrayList<HeadField>();
                // tmpList.add(headField);
                int currWidth = headField.getLeafSize();
                rowField.setIndent(getIndentOfHeadField(headField, 0));
                // rowField.setColspan(1);
                rowField.setRowspan(currWidth == 0 ? 1 : currWidth);
                String lineUniqueName = headField.getNodeUniqueName();
                rowField.setUniqueNameAll(lineUniqueName);
                rowField.setUniqueName(headField.getValue());
                String caption = headField.getCaption();
                /**
                 * 把周的开始caption换成完整的caption
                 */
                // TODO 临时方案，需要后续调整
                if (isTimeDim(headField.getValue())) {
                    try {
                        rowField.setV(target.format(src.parse(caption)));
                    } catch (ParseException e) {
                    }
                } else {
                    rowField.setV(caption);
                }
                /**
                 * 设置原始展开状态
                 */
                if (!headField.isHasChildren()) {
                    rowField.setExpand(null);
                } else if (CollectionUtils.isEmpty(headField.getChildren()) && headField.getParentLevelField() == null
                        && headField.getParent() == null) {
                    String dimNameFromUniqueName = MetaNameUtil.getDimNameFromUniqueName(rowField.getUniqueName());
                    Dimension dim = getDimByName(cube, dimNameFromUniqueName);
                    final int length = MetaNameUtil.parseUnique2NameArray(rowField.getUniqueName()).length;
                    final boolean lastLevel = dim.getLevels().size() >= length;
                    Level level = dim.getLevels().values().toArray(new Level[0])[0];
                    if (dim != null && (lastLevel || (level instanceof CallbackLevel))) {
                        rowField.setExpand(true);
                    } else {
                        rowField.setExpand(null);
                    }
                } else if (!CollectionUtils.isEmpty(headField.getChildren())) {
                    if (headField.getLeafSize() == 0 && headField.getParent() == null
                            && headField.getParentLevelField() == null) {
                        rowField.setExpand(null);
                    } else {
                        rowField.setExpand(false);
                    }
                } else {
                    rowField.setExpand(true);
                }

                rowField.setDrillByLink(false);
                rowField.setDimName((String) headField.getExtInfos().get(EXT_INFOS_MEM_DIMNAME));
                rowField.setIndent(getIndentOfHeadField(headField, 0));
                rowField.setValueAll(transStrList2Str(getAllCaptionofHeadField(headField), "-", true));
                idxRowField.add(rowField);
            }
            if (needLimit && rowFieldList.size() >= limitSize) {
                break;
            }
            if (idxRowField.isEmpty()) {
                continue;
            }
            rowFieldList.add(idxRowField);

        }

        return rowFieldList;

    }

    private static Dimension getDimByName(Cube cube, String dimName) {
        Map<String, Dimension> dims = cube.getDimensions();
        for (Map.Entry<String, Dimension> dim : dims.entrySet()) {
            if (dimName.equals(dim.getValue().getName())) {
                return dim.getValue();
            }
        }
        return null;
    }

    private static boolean isTimeDim(String value) {
        if (MetaNameUtil.isAllMemberUniqueName(value)) {
            return false;
        }
        return value.contains("ownertable_TimeDay");
    }

    /**
     * 给出任意一个headField的祖先链
     * 
     * @param headField
     * @return
     */
    private static List<HeadField> getHeadListOutofHead(HeadField headField) {
        List<HeadField> resultList = new ArrayList<HeadField>();
        if (headField == null) {
            return resultList;
        } else {
            resultList.add(headField);
            resultList.addAll(getHeadListOutofHead(headField.getParentLevelField()));
        }
        return resultList;
    }

    /**
     * 
     * @param headField
     * @param indent
     * @return
     */
    private static int getIndentOfHeadField(HeadField headField, int indent) {
        if (headField != null && (headField.getParent() != null || headField.isHasChildren())) {
            return getIndentOfHeadField(headField.getParent(), indent + 1);
        } else {
            return indent;
        }
    }

    /**
     * 
     * @param headField
     * @return
     */
    private static List<String> getAllMemberDimConcatUniqname(HeadField headField) {
        List<String> resultList = new ArrayList<String>();
        if (headField == null) {
            return resultList;
        } else {
            Map<String, Object> extInfos = headField.getExtInfos();

            if (extInfos != null && extInfos.get(EXT_INFOS_MEM_UNIQNAME) != null) {
                String uniqueName = (String) headField.getExtInfos().get(EXT_INFOS_MEM_UNIQNAME);
                if (headField.getExtInfos().get(EXT_INFOS_MEM_DIMNAME) != null
                        && StringUtils.isNotBlank((String) headField.getExtInfos().get(EXT_INFOS_MEM_DIMNAME))) {
                    uniqueName = headField.getExtInfos().get(EXT_INFOS_MEM_DIMNAME) + "_" + uniqueName;
                }
                resultList.add(uniqueName);
            }

            resultList.addAll(getAllMemberDimConcatUniqname(headField.getParentLevelField()));
        }
        return resultList;
    }

    /**
     * 
     * @param headField
     * @return
     */
    private static List<String> getAllCaptionofHeadField(HeadField headField) {
        List<String> resultList = new ArrayList<String>();
        if (headField == null) {
            return resultList;
        } else {
            resultList.add(headField.getCaption());
            resultList.addAll(getAllCaptionofHeadField(headField.getParentLevelField()));
        }
        return resultList;
    }

    /**
     * 
     * @param headFields
     * @return
     */
    private static int getHeightOfHeadFieldList(List<HeadField> headFields) {
        int maxHeight = 0;
        if (headFields == null || headFields.size() == 0) {
            return 0;
        } else if (!ifAllHeadFieldsHasSubChild(headFields)) {
            return 1;
        } else {
            for (HeadField headField : headFields) {
                int currHeight = 1 + getHeightOfHeadFieldList(headField.getNodeList());
                if (currHeight > maxHeight) {
                    maxHeight = currHeight;
                }
            }
        }
        return maxHeight;
    }

    /**
     * 
     * @param strList
     * @param split
     * @param isRevert
     * @return
     */
    private static String transStrList2Str(List<String> strList, String split, boolean isRevert) {
        StringBuffer sb = new StringBuffer();
        if (strList == null || strList.size() == 0) {
            return sb.toString();
        }

        if (isRevert) {
            Collections.reverse(strList);
        }

        sb.append(strList.get(0));
        for (int i = 1; i < strList.size(); i++) {
            sb.append(split);
            sb.append(strList.get(i));
        }
        return sb.toString();

    }

    /**
     * 获取List<HeadField>结构下的所有叶子节点
     * 
     * @param headFields
     * @return
     */
    private static List<HeadField> getLeafNodeList(List<HeadField> headFields) {
        List<HeadField> resultList = new ArrayList<HeadField>();

        for (HeadField headField : headFields) {
            resultList.addAll(headField.getLeafFileds(true));
        }
        return resultList;

    }

    /**
     * 获取第一行的rowspan
     * 
     * @param rowHeadFields 行上的节点
     * @return 返回第一行的rowspan
     */
    private static int getRowSpan(List<HeadField> rowHeadFields) {
        if (!CollectionUtils.isEmpty(rowHeadFields)) {
            for (HeadField filed : rowHeadFields) {
                return filed.getLeafSize();
            }
        }
        return 0;
    }

    /**
     * 
     * @param colHeadFields
     * @return
     */
    private static List<List<ColField>> transColHeadFields2ColFields(List<HeadField> colHeadFields) {
        List<List<ColField>> colFieldList = new ArrayList<List<ColField>>();
        int colHeight = getHeightOfHeadFieldList(colHeadFields);
        if (colHeadFields == null || colHeadFields.size() == 0) {
            return null;
        }
        Map<String, HeadField> hasStoredMap = new HashMap<String, HeadField>();

        for (int i = 0; i < colHeight; i++) {
            // 当前处理第i层数据
            List<ColField> idxColField = new ArrayList<ColField>();
            if (colFieldList.size() >= i + 1) {
                idxColField = colFieldList.get(i);
            } else {
                colFieldList.add(idxColField);
            }
            // 第i层节点数据
            List<HeadField> idxHeadFieldList = getIdxHeadFieldsForCol(colHeadFields, i + 1);
            for (HeadField headField : idxHeadFieldList) {
                if (hasStoredMap.get(headField.getValue()) != null) {
                    continue;
                } else {
                    hasStoredMap.put(headField.getValue(), headField);
                }
                ColField colField = new ColField();
                colField.setColSpan(headField.getLeafSize());
                colField.setUniqName(headField.getNodeUniqueName());
                colField.setV(headField.getCaption());
                if ((i + 1 < colHeight) && (headField.getNodeList() == null || headField.getNodeList().size() == 0)) {
                    colField.setRowspan(colHeight - i);
                } else {
                    colField.setRowspan(1);
                }
                idxColField.add(colField);
            }
            // colFieldList.add(idxColField);

        }

        return colFieldList;

    }

    /**
     * 
     * @param headFields
     * @param i
     * @return
     */
    private static List<HeadField> getIdxHeadFieldsForCol(List<HeadField> headFields, int i) {
        List<HeadField> resultList = new ArrayList<HeadField>();
        if (i == 1) {
            for (HeadField head : headFields) {
                resultList.add(head);
                resultList.addAll(head.getChildren());
            }
        } else {
            for (HeadField head : headFields) {
                List<HeadField> currList = getIdxHeadFieldsForCol(head.getNodeList(), i - 1);
                if (currList != null && currList.size() != 0) {
                    resultList.addAll(currList);
                }
                for (HeadField child : head.getChildren()) {
                    resultList.addAll(getIdxHeadFieldsForCol(child.getNodeList(), i - 1));
                }
            }
        }
        return resultList;
    }

    /**
     * @param oriDataModel
     * @param newDataModel
     * @param rowNum
     * @return
     */
    public static DataModel merageDataModel(DataModel oriDataModel, DataModel newDataModel, int rowNum) {
        DataModel dataModel = new DataModel();
        dataModel.setColumnBaseData(oriDataModel.getColumnBaseData());
        dataModel.setColumnHeadFields(oriDataModel.getColumnHeadFields());
        dataModel.setRowHeadFields(oriDataModel.getRowHeadFields());
        dataModel.setOperateIndex(oriDataModel.getOperateIndex());
        List<HeadField> rowHeadFields = dataModel.getRowHeadFields();
        // 设置缩进以及父子关系
        HeadField realRowHead = getRealRowHeadByRowNum(rowNum, rowHeadFields);
        if (realRowHead == null) {
            throw new IllegalStateException("can not found head field with row number " + rowNum);
        }
        realRowHead.getExtInfos().put(EXT_INFOS_MEM_EXPAND, false);

        realRowHead.setChildren(newDataModel.getRowHeadFields().get(0).getChildren());
        realRowHead.getChildren().forEach(tmp -> {
            tmp.setNodeUniqueName(null);
            tmp.setParentLevelField(realRowHead.getParentLevelField());
            tmp.setParent(realRowHead);
            tmp.getNodeUniqueName();
        });
        realRowHead.setNodeList(newDataModel.getRowHeadFields().get(0).getNodeList());
        realRowHead.getNodeList().forEach(tmp -> {
            tmp.setNodeUniqueName(null);
            tmp.getNodeUniqueName();
        });
        realRowHead.setNodeUniqueName(null);
        realRowHead.getNodeUniqueName();
        List<List<BigDecimal>> rowBaseData = transData(dataModel.getColumnBaseData());
        List<List<BigDecimal>> tmp = transData(newDataModel.getColumnBaseData());
        for (int i = 1; i < tmp.size(); ++i) {
            rowBaseData.add(rowNum + i, tmp.get(i));
        }
        // rowBaseData.addAll(rowNum, transData(newDataModel.getColumnBaseData()));
        dataModel.setColumnBaseData(transData(rowBaseData));
        return dataModel;
    }

    /**
     * @param rowNum
     * @param rowHeadFields
     * @return
     */
    public static HeadField getRealRowHeadByRowNum(int rowNum, List<HeadField> rowHeadFields) {
        List<HeadField> tmp = com.baidu.rigel.biplatform.ac.util.DataModelUtils.getLeafNodeList(rowHeadFields);
        return tmp.get(rowNum);
    }

    /**
     * 
     * @param datas
     * @return
     */
    private static List<List<BigDecimal>> transData(List<List<BigDecimal>> datas) {
        List<List<BigDecimal>> rs = Lists.newArrayList();
        for (int i = 0; i < datas.size(); ++i) {
            for (int j = 0; j < datas.get(i).size(); ++j) {
                if (rs.size() <= j) {
                    rs.add(Lists.newArrayList());
                }
                rs.get(j).add(datas.get(i).get(j));
            }
        }
        return rs;
    }

    /**
     * 
     * @param dataModel
     * @param rowNum
     * @return
     */
    public static DataModel removeDataFromDataModel(DataModel dataModel, int rowNum) {
        if (dataModel == null) {
            throw new IllegalArgumentException("previous result is empty");
        }
        DataModel newDataModel = DeepcopyUtils.deepCopy(dataModel);
        List<HeadField> rowHeadFields = newDataModel.getRowHeadFields();
        HeadField headField = getRealRowHeadByRowNum(rowNum, rowHeadFields);
        if (headField == null) {
            throw new IllegalStateException("can not found head field with row number " + rowNum);
        }
        int childSize = getChildSize(headField.getChildren());
        childSize = childSize + headField.getChildren().size();
        headField.setChildren(Lists.newArrayList());
        headField.getExtInfos().put(EXT_INFOS_MEM_EXPAND, true);
        rowHeadFields = replaceHeadFieldToCorrectLocation(rowHeadFields, headField);
        List<List<BigDecimal>> datas = transData(newDataModel.getColumnBaseData());
        List<List<BigDecimal>> newDatas = Lists.newArrayList();
        for (int i = 0; i < datas.size(); ++i) {
            if (i > rowNum && i <= childSize + rowNum) {
                continue;
            }
            newDatas.add(datas.get(i));
        }
        newDataModel.setColumnBaseData(transData(newDatas));
        return newDataModel;
    }

    /**
     * 
     * @param rowHeadFields
     * @param headField
     */
    private static List<HeadField>
            replaceHeadFieldToCorrectLocation(List<HeadField> rowHeadFields, HeadField headField) {
        List<HeadField> rs = Lists.newArrayList();
        for (HeadField tmp : rowHeadFields) {
            if (tmp.getNodeUniqueName().equals(headField.getNodeUniqueName())) {
                rs.add(headField);
            } else {
                rs.add(tmp);
            }
            if (tmp.getChildren() == null || tmp.getChildren().isEmpty()) {
                tmp.setChildren(replaceHeadFieldToCorrectLocation(tmp.getChildren(), headField));
            }
        }
        return rs;
    }

    /**
     * 
     * @param fields
     * @return int 孩子节点个数
     */
    private static int getChildSize(List<HeadField> fields) {
        if (fields == null || fields.isEmpty()) {
            return 0;
        }
        return fields.stream().map(field -> {
            return getChildSize(field.getChildren()) + field.getChildren().size();
        }).reduce(0, (x, y) -> x + y);
    }

    /**
     * 
     * decorateTable
     * 
     * @param formatModel formatModel
     * @param table table
     * @param targetArea targetArea
     * @param reportDesignModel reportDesignModel
     */
    public static void decorateTable(FormatModel formatModel, PivotTable table, ExtendArea targetArea,
            ReportDesignModel reportDesignModel) {
        decorateTable(formatModel, table);
        Map<String, Object> otherSetting = targetArea.getOtherSetting();
        // 当表格为liteolap表时，需要取到ref的area，才能取到正确的otherSetting
        if (targetArea.getType() == ExtendAreaType.LITEOLAP_TABLE) {
            otherSetting = reportDesignModel.getExtendById(targetArea.getReferenceAreaId()).getOtherSetting();
        }
        boolean isShowZero = DataModelUtils.isShowZero(otherSetting);
        if (isShowZero) {
            List<List<CellData>> cellDatas = table.getDataSourceColumnBased();
            int cellDatasIndex = 0;
            if (cellDatas != null) {
                while (cellDatasIndex < cellDatas.size()) {
                    List<CellData> cellData = cellDatas.get(cellDatasIndex);
                    cellDatasIndex++;
                    if (cellData != null) {
                        int cellDataIndex = 0;
                        while (cellDataIndex < cellData.size()) {
                            if (cellData.get(cellDataIndex).getV() == null) {
                                cellData.get(cellDataIndex).setV(BigDecimal.ZERO);
                            }
                            cellDataIndex++;
                        }
                    }
                }
            }
        }
    }

    /**
     * 修饰平面表数据 decoratePlaneTable
     * 
     * @param table
     * @param isShowZero
     */
    public static void decoratePlaneTable(PlaneTable table, boolean isShowZero) {
        List<Map<String, String>> newDatas = Lists.newArrayList();
        List<Map<String, String>> datas = table.getData();
        if (isShowZero) {
            datas.forEach(data -> {
                Map<String, String> newData = Maps.newHashMap();
                data.forEach((k, v) -> {
                    if (StringUtils.isEmpty(v)) {
                        newData.put(k, "0");
                    } else {
                        newData.put(k, v);
                    }
                });
                newDatas.add(newData);
            });
            table.setData(newDatas);
        }
    }

    /**
     * 
     * @param formatModel 格式模型
     * @param table 透视表
     */
    /**
     * @param formatModel
     * @param table
     */
    public static void decorateTable(FormatModel formatModel, PivotTable table) {
        if (formatModel == null) {
            return;
        }

        Map<String, String> dataFormat = formatModel.getDataFormat();
        Map<String, String> toolTips = formatModel.getToolTips();
        Map<String, String> textAlignFormat = formatModel.getTextAlignFormat();
        Map<String, LinkInfo> linkInfoMap = formatModel.getLinkInfo();
        
        String defaultTextAlign = Constants.DEFALUT_ALIGN_FORMAT;
        if (textAlignFormat.containsKey(Constants.DEFAULT_ALIGN_FORMAT_KEY)) {
            defaultTextAlign = textAlignFormat.get(Constants.DEFAULT_ALIGN_FORMAT_KEY);
        }

        List<ColDefine> colDefineList = table.getColDefine();
        for (ColDefine define : colDefineList) {

            String olapElementId = define.getOlapElementId();
            if (!StringUtils.isEmpty(olapElementId)) {
                LinkInfo linkInfo = linkInfoMap.get(olapElementId);
                // 这里严格判断，只有当设置了跳转表，并且参数映射也已经设置完成，才在多维报表处展示超链接
                if (linkInfo != null && !StringUtils.isEmpty(linkInfo.getTargetTableId())
                        && !MapUtils.isEmpty(linkInfo.getParamMapping())) {
                    define.setLinkBridge(olapElementId);
                }

            }
            String uniqueName = define.getUniqueName();
            uniqueName = MetaNameUtil.parseUnique2NameArray(define.getUniqueName())[1];
            if (dataFormat != null) {
                String formatStr = dataFormat.get("defaultFormat");
                if (!StringUtils.isEmpty(dataFormat.get(uniqueName))) {
                    formatStr = dataFormat.get(uniqueName);
                }
                if (!StringUtils.isEmpty(formatStr)) {
                    define.setFormat(formatStr);
                }
            }
            if (toolTips != null) {
                String toolTip = toolTips.get(uniqueName);
                if (StringUtils.isEmpty(toolTip)) {
                    toolTip = uniqueName;
                }
                define.setToolTip(toolTip);
            }
            if (textAlignFormat != null) {
                String align = textAlignFormat.get(uniqueName);
                if (StringUtils.isEmpty(align)) {
                    align = defaultTextAlign;
                }
                define.setAlign(align);
            }
        }
        /**
         * 为pivottable增加操作列属性, add by majun
         */
        OlapLinkUtils.addOperationColum(linkInfoMap, table);

    }

    /**
     * 将平面表DataModel转为csv文件
     * 
     * @param cube
     * @param dataModel
     * @return
     */
    public static String convertDataModel2CsvStringForPlaneTable(Cube cube, DataModel dataModel, LogicModel logicModel,
            Map<String, Object> setting) {
         long begin = System.currentTimeMillis();
        StringBuilder rs = new StringBuilder();
        if (dataModel == null) {
            return rs.toString();
        }
        // 获取平面表数据
        TableData tableData = dataModel.getTableData();
        if (tableData == null) {
            // 如果没有数据，返回空串
            return "";
        }
        // 获取列属性信息
        List<Column> columns = tableData.getColumns();
        // 获取正确的下载顺序
        List<String> keys = getKeysInOrder(cube, logicModel, columns);
        List<TableData.Column> columnsInOrder = new ArrayList<TableData.Column>();
        for (String key : keys) {
            for (Column column : columns) {
                if (column.key.equals(key)) {
                    columnsInOrder.add(column);
                    if (com.baidu.rigel.biplatform.ac.util.DataModelUtils.isChar(column.dataType)) {
                        rs.append("\t" + column.caption + ",");
                    } else {
                        rs.append(column.caption + ",");
                    }
                    break;
                }
            }
        }
        // 替换最后一个","
        rs.replace(rs.length() - 1, rs.length(), "");
        rs.append("\r\n");

        // 获取数据信息
        Map<String, List<String>> data = tableData.getColBaseDatas();
        // 获取总的数据条数
        int totalRecordSize = getTotalRecordSizeOfPlaneTable(data);
        // 将列存储数据转为行存储数据，该数据已经有序
        List<Map<String, String>> rowBasedDatas = transPlaneTableFromCol(totalRecordSize, data, keys, cube, logicModel);
        // 构建数据
        for (int i = 0; i < totalRecordSize; i++) {
            Map<String, String> rowBasedData = rowBasedDatas.get(i);
            for (Column column : columnsInOrder) {
                if (!isShowZero(setting) && StringUtils.isEmpty(rowBasedData.get(column.key))) {
                    rs.append("-" + ",");
                } else if (isShowZero(setting) && StringUtils.isEmpty(rowBasedData.get(column.key))) {
                    rs.append("0" + ",");
                } else {
                    String dataCell = rowBasedData.get(column.key);
                    if (DataModelUtils.isNumeric(dataCell)) {
                        rs.append(dataCell + ",");
                    } else {
                        rs.append("\t" + dataCell + ",");
                    }
                }
            }
            rs.replace(rs.length() - 1, rs.length(), "");
            rs.append("\r\n");
        }
        LOG.info("transfer datamodel 2 csv string cost:" + (System.currentTimeMillis() - begin) + "ms!");
        return rs.toString();
    }
    
    public static boolean isNumeric(String str){ 
        Pattern pattern = Pattern.compile("(-?[0-9]+.?[0-9]+)|[0-9]|-[0-9]"); 
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false; 
        } 
        return true; 
    }
    
    /**
     * 在显示前对DataModel的数据进行处理，该方法主要针对下载而言 preProcessDataModel4Show
     * 
     * @param dataModel 数据模型
     * @param setting 配置信息
     */
    public static DataModel preProcessDataModel4Show(DataModel dataModel, Map<String, Object> setting) {
        if (setting == null) {
            return dataModel;
        }

        // 当前对是否显示0还是-进行处理
        if (isShowZero(setting)) {
            List<List<BigDecimal>> allDatas = Lists.newArrayList();
            for (List<BigDecimal> sourcePiece : dataModel.getColumnBaseData()) {
                List<BigDecimal> rowData = Lists.newArrayList();
                for (BigDecimal data : sourcePiece) {
                    if (data == null) {
                        rowData.add(BigDecimal.ZERO);
                    } else {
                        rowData.add(data);
                    }
                }
                allDatas.add(rowData);
            }
            dataModel.setColumnBaseData(allDatas);
        }
        return dataModel;
    }

    /**
     * 判断某个表格中的数据是显示0还是- isShowZero
     * 
     * @param setting
     * @return
     */
    public static boolean isShowZero(Map<String, Object> setting) {
        // 默认不做非0处理
        boolean isShowZero = false;
        if (setting != null && setting.containsKey(Constants.IS_SHOW_ZERO)) {
            isShowZero = Boolean.valueOf((String) setting.get(Constants.IS_SHOW_ZERO));
        }
        return isShowZero;
    }

    /**
     * 将dataModel转化为csv文件
     * 
     * @param dataModel
     * @return 转换后的文件
     */
    public static String convertDataModel2CsvString(Cube cube, DataModel dataModel, LogicModel logicModel) {
        StringBuilder rs = new StringBuilder();

        List<List<BigDecimal>> rowDatas = convertToRowData(dataModel.getColumnBaseData());
        String[] captions = getDimCaptions(cube, logicModel);
        // getMaxDepth4Dim(dataModel.getRowHeadFields());
        for (String caption : captions) {
            rs.append(caption + ",");
        }
        final int colSize = dataModel.getColumnHeadFields().size();
        for (int i = 0; i < colSize; ++i) {
            rs.append(dataModel.getColumnHeadFields().get(i).getCaption());
            if (i < colSize - 1) {
                rs.append(",");
            } else {
                rs.append("\r\n");
            }
        }
        List<List<String>> rowCaptions = genRowCaptions(dataModel.getRowHeadFields());
        for (int i = 0; i < rowCaptions.size(); ++i) {
            rowCaptions.get(i).forEach(str -> {
                rs.append(str + ",");
            });
            for (int j = 0; j < rowDatas.get(i).size(); ++j) {
                rs.append(rowDatas.get(i).get(j) == null ? "-" : rowDatas.get(i).get(j));
                if (j < rowDatas.get(i).size() - 1) {
                    rs.append(",");
                } else {
                    rs.append("\r\n");
                }
            }
        }
        return rs.toString();
    }

    private static List<List<BigDecimal>> convertToRowData(List<List<BigDecimal>> columnBaseData) {
        List<List<BigDecimal>> rowBasedData = new ArrayList<List<BigDecimal>>();

        for (List<BigDecimal> currColumnData : columnBaseData) {
            for (int i = 0; i < currColumnData.size(); i++) {
                // 当前列的第i行
                List<BigDecimal> currRowData = new ArrayList<BigDecimal>();
                if (rowBasedData.size() >= i + 1) {
                    currRowData = rowBasedData.get(i);
                } else {
                    rowBasedData.add(currRowData);
                }
                currRowData.add(currColumnData.get(i));
            }
        }

        return rowBasedData;
    }

    private static List<List<String>> genRowCaptions(List<HeadField> rowHeadFields) {
        List<List<String>> rs = Lists.newArrayList();
        for (int i = 0; i < rowHeadFields.size(); ++i) {
            final HeadField headField = rowHeadFields.get(i);
            final List<HeadField> nodeList = headField.getNodeList();
            List<String> tmp = Lists.newArrayList();
            if (nodeList == null || nodeList.size() == 0) {
                tmp.add(rowHeadFields.get(i).getCaption());
            } else {
                List<List<String>> nodeListCaption = genRowCaptions(headField.getNodeList());
                nodeListCaption.forEach(list -> {
                    list.add(0, headField.getCaption());
                });
                rs.addAll(nodeListCaption);
            }
            rs.add(tmp);
            if (headField.getChildren() != null && headField.getChildren().size() > 0) {
                rs.addAll(genRowCaptions(headField.getChildren()));
            }
        }
        List<List<String>> tmp = Lists.newArrayList();
        rs.stream().forEach(list -> {
            if (list != null && list.size() > 0) {
                tmp.add(list);
            }
        });
        return tmp;
    }
    
    /**
     * 返回新多维表需要的操作列对象集合
     * 
     * @param linkInfoMap linkInfoMap
     * @return 返回新多维表需要的操作列对象集合
     */
    public static List<OperationColumnDefine> generateOperationColumnList(Map<String, LinkInfo> linkInfoMap) {
        List<OperationColumnDefine> operationColumns = Lists.newArrayList();
        List<LinkInfo> savedOperationLinkInfoList = OlapLinkUtils.getOperationColumKeys(linkInfoMap);
        if (!CollectionUtils.isEmpty(savedOperationLinkInfoList)) {
            for (LinkInfo linkInfo : savedOperationLinkInfoList) {
                OperationColumnDefine operationColumn = new OperationColumnDefine();
                operationColumn.setLinkBridge(linkInfo.getColunmSourceId());
                operationColumn.setName(linkInfo.getColunmSourceCaption());
                operationColumns.add(operationColumn);
            }
        }
        return operationColumns;
    }

}
