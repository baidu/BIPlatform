package com.baidu.rigel.biplatform.queryrouter.queryplugin.convert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.baidu.rigel.biplatform.ac.minicube.CallbackLevel;
import com.baidu.rigel.biplatform.ac.minicube.ExtendMinicubeMeasure;
import com.baidu.rigel.biplatform.ac.minicube.MiniCube;
import com.baidu.rigel.biplatform.ac.model.Aggregator;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.DimensionType;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.MeasureType;
import com.baidu.rigel.biplatform.ac.query.model.AxisMeta;
import com.baidu.rigel.biplatform.ac.query.model.AxisMeta.AxisType;
import com.baidu.rigel.biplatform.ac.query.model.ConfigQuestionModel;
import com.baidu.rigel.biplatform.ac.query.model.DimensionCondition;
import com.baidu.rigel.biplatform.ac.query.model.MeasureCondition;
import com.baidu.rigel.biplatform.ac.query.model.MetaCondition;
import com.baidu.rigel.biplatform.ac.query.model.QueryData;
import com.baidu.rigel.biplatform.ac.query.model.SQLCondition.SQLConditionType;
import com.baidu.rigel.biplatform.ac.query.model.SortRecord;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.queryrouter.calculate.operator.model.Operator;
import com.baidu.rigel.biplatform.queryrouter.calculate.operator.utils.OperatorUtils;
import com.baidu.rigel.biplatform.queryrouter.handle.QueryRouterContext;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.Column;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.ColumnCondition;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.ColumnType;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.JoinOn;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.JoinTable;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.PlaneTableQuestionModel;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.QuestionModelTransformationException;
import com.baidu.rigel.biplatform.queryrouter.queryplugin.sql.model.SqlConstants;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


/**
 * PlantTableUtils
 * 
 * @author luowenlei
 *
 */
public class PlaneTableUtils {
    
    /**
     * Logger
     */
    private static Logger logger = LoggerFactory.getLogger(PlaneTableUtils.class);
    
    /**
     * CUBE_TIME_TABLE_NAME
     */
    public static final String CUBE_TIME_TABLE_NAME = "ownertable";
    
    /**
     * 参数需要计算page的totalsize
     */
    private static final int PARMA_NEED_CONTAIN_TOTALSIZE = -1;
    
    /**
     * 通过多维表查询对象转换成平面表查询对象
     * 
     * @param configQuestionModel
     * @return PlaneTableQuestionModel 平面表查询对象
     */
    public static PlaneTableQuestionModel convertConfigQuestionModel2PtQuestionModel(
            ConfigQuestionModel configQuestionModel) {
        PlaneTableQuestionModel planeTableQuestionModel = new PlaneTableQuestionModel();
        MiniCube miniCube = (MiniCube) configQuestionModel.getCube();
        planeTableQuestionModel.setDataSourceInfo(configQuestionModel.getDataSourceInfo());
        Map<String, Column> allColumns = PlaneTableUtils.getAllColumns(
                configQuestionModel.getCube(), configQuestionModel.getSortRecord());

        planeTableQuestionModel.setSelection(PlaneTableUtils.getSelection(configQuestionModel, allColumns));
        planeTableQuestionModel.setQueryConditions(PlaneTableUtils.convertQueryConditions(miniCube,
                allColumns, configQuestionModel.getQueryConditions()));
        planeTableQuestionModel.setRequestParams(configQuestionModel.getRequestParams());
        planeTableQuestionModel.setSortRecord(configQuestionModel.getSortRecord());
        planeTableQuestionModel.setPageInfo(configQuestionModel.getPageInfo());
        // 如果为getRecordSize为-1，那么需要搜索dataModel.getRecordSize() from database
        if (configQuestionModel.getPageInfo() != null
                && configQuestionModel.getPageInfo().getTotalRecordCount() == PARMA_NEED_CONTAIN_TOTALSIZE) {
            planeTableQuestionModel.setGenerateTotalSize(true);
        }
        planeTableQuestionModel
                .setQueryConditionLimit(configQuestionModel.getQueryConditionLimit());
        planeTableQuestionModel.setSource(miniCube.getSource());
        planeTableQuestionModel.setMetaMap(allColumns);
        return planeTableQuestionModel;
    }
    
    /**
     * 设置有序的选择查询的列
     * 
     * @param configQuestionModel
     *            configQuestionModel
     * @param metaMap
     *            metaMap
     * @return List<String> SelectionList
     */
    public static List<String> getSelection(ConfigQuestionModel configQuestionModel,
            Map<String, Column> metaMap) {
        List<String> result = new ArrayList<String>();
        if (CollectionUtils.isEmpty(metaMap)) {
            return result;
        }
        if (MapUtils.isEmpty(configQuestionModel.getAxisMetas())) {
            return result;
        }
        
        // 判断是否为aggsql
        if (OperatorUtils.isAggQuery(metaMap)) {
            return getSelectionNotOrdered(configQuestionModel, metaMap);
        } else {
            // 如果axisMetaMeasures.getQueryItemsOrder()没有值，那么返回无序的List
            if (configQuestionModel.getAxisMetas().get(AxisType.COLUMN) != null) {
                AxisMeta axisMetaMeasures = (AxisMeta) configQuestionModel.getAxisMetas().get(
                        AxisType.COLUMN);
                if (CollectionUtils.isEmpty(axisMetaMeasures.getQueryItemsOrder())) {
                    return getSelectionNotOrdered(configQuestionModel, metaMap);
                } else {
                    return axisMetaMeasures.getQueryItemsOrder();
                }
            } else {
                return getSelectionNotOrdered(configQuestionModel, metaMap);
            }
        }
        

    }
    
    /**
     * 设置无序的选择查询的列
     * 
     * @param configQuestionModel
     *            configQuestionModel
     * @param metaMap
     *            metaMap
     * @return List<String> SelectionList
     */
    public static List<String> getSelectionNotOrdered(ConfigQuestionModel configQuestionModel,
            Map<String, Column> metaMap) {
        List<String> result = new ArrayList<String>();
        if (configQuestionModel.getAxisMetas().get(AxisType.COLUMN) != null) {
            AxisMeta axisMetaMeasures = (AxisMeta) configQuestionModel.getAxisMetas().get(
                    AxisType.COLUMN);
            // 获取指标元数据
            if (axisMetaMeasures.getQueryMeasures() != null) {
                for (String measureName : axisMetaMeasures.getQueryMeasures()) {
                    result.add("[Measure].[" + measureName + "]");
                }
            }

            // 获取指标元数据
            if (axisMetaMeasures.getCrossjoinDims() != null) {
                for (String dimName : axisMetaMeasures.getCrossjoinDims()) {
                    result.add("[Dimension].[" + dimName + "]");
                }
            }
        }

        if (configQuestionModel.getAxisMetas().get(AxisType.ROW) != null) {
            // 获取维度元数据
            AxisMeta axisMetaDims = (AxisMeta) configQuestionModel.getAxisMetas().get(AxisType.ROW);
            if (axisMetaDims.getCrossjoinDims() != null) {
                for (String dimName : axisMetaDims.getCrossjoinDims()) {
                    result.add("[Dimension].[" + dimName + "]");
                }
            }

            // 获取维度元数据
            if (axisMetaDims.getQueryMeasures() != null) {
                for (String measureName : axisMetaDims.getQueryMeasures()) {
                    result.add("[Measure].[" + measureName + "]");
                }
            }
        }
        return result;
    }
    
    /**
     * 获取所有列的元数据
     * 
     * @param configQuestionModel
     *            configQuestionModel
     * @return Map<String, Column> 所有列的元数据
     */
    public static Map<String, Column> getAllColumns(Cube cube, SortRecord sortRecord) {
        MiniCube miniCube = (MiniCube) cube;
        Map<String, Column> allColumns = Maps.newConcurrentMap();
        // 获取指标元数据
        miniCube.getMeasures().forEach((k, v) -> {
            String formula = null;
            if (v.getAggregator() == Aggregator.CALCULATED
                    && v instanceof ExtendMinicubeMeasure) {
                formula = ((ExtendMinicubeMeasure) v).getFormula();
            }
            boolean isCB = v.getType() == MeasureType.CALLBACK ? true : false;
            Column c = buildMeasureColumn(
                    miniCube.getSource(), v.getDefine(), v.getCaption(), v.getAggregator(), formula, false, isCB);
            String measureKey = "[Measure].[" + k + "]";
            allColumns.put(measureKey, c);
        });
        
        // 获取基本维度元数据
        for (String k : miniCube.getDimensions().keySet()) {
            Dimension v = miniCube.getDimensions().get(k);
            if (v.getType() == DimensionType.GROUP_DIMENSION) {
                Column dimension = new Column();
                dimension.setKeys(Lists.newArrayList());
                dimension.setColumnType(ColumnType.GROUP);
                for (Entry<String, Level> entry : v.getLevels().entrySet()) {
                    String tableName = entry.getValue().getDimTable();
                    String dimKey = "";
                    if (tableName != null && !miniCube.getSource().equals(tableName)) {
                        dimKey = "[Dimension].[" + tableName + "_" + entry.getKey() + "]";
                    } else {
                        dimKey = "[Dimension].[" + entry.getKey() + "]";
                    }
                    Column columnLvl = new Column();
                    setSingleCommonLevelData(columnLvl, entry.getValue(), miniCube.getSource());
                    allColumns.put(dimKey, columnLvl);
                    dimension.getKeys().add(dimKey);
                }
                allColumns.put("[Dimension].[" + k + "]", dimension);
            } else {
                String dimKey = "[Dimension].[" + k + "]";
                Column column = new Column();
                setSingleCommonDimensionData(column, v, miniCube.getSource());
                allColumns.put(dimKey, column);
            }
        }
        
        // 获取关联信息
        for (Entry<String, Column> entry : allColumns.entrySet()) {
            Column column = entry.getValue();
            if (column.getColumnType() == ColumnType.JOIN
                    && !column.getTableName().equals(column.getFacttableName())
                    && column.getJoinTable() != null
                    && !column.getJoinTable().getJoinOnList().isEmpty()) {
                // 正常维度
                String dimPk = column.getJoinTable().getJoinOnList().get(0).getJoinTableFieldName();
                // 添加维度表关联字段，需要添加一个关联的维度表的主键信息
                String dimpk = "[Dimension].[" + column.getTableName() + "_" + dimPk + "]";
                Column columnPrimary = new Column();
                setSingleDimensionPrimaryKeyData(columnPrimary, column, miniCube.getSource());
                allColumns.put(dimpk, columnPrimary);
                
                // 添加事实表表关联数据信息
                String sourcefk = "[Measure].[" + column.getFacttableColumnName() + "]";
                Column c = buildMeasureColumn(
                        miniCube.getSource(), column.getFacttableColumnName(),
                        column.getFacttableColumnName(), Aggregator.NONE, "", true, false);
                allColumns.put(sourcefk, c);
            } else if (column.getColumnType() == ColumnType.JOIN
                    && column.getTableName().equals(column.getFacttableName())){
                // 添加事实表表退化维字段数据信息
                String sourcefk = "[Measure].[" + column.getFacttableColumnName() + "]";
                Column c  = DeepcopyUtils.deepCopy(column);
                c.setJoinTable(null);
                c.setSortRecord(null);
                c.setOperator(null);
                allColumns.put(sourcefk, c);
            } else if (column.getColumnType() == ColumnType.TIME){
                // 添加事实表表时间字段数据信息
                String sourcefk = "[Measure].[" + column.getFacttableColumnName() + "]";
                Column c = buildMeasureColumn(
                        miniCube.getSource(), column.getFacttableColumnName(),
                        column.getFacttableColumnName(), Aggregator.NONE, "", true, false);
                c.setTableFieldName(column.getFacttableColumnName());
                c.setJoinTable(null);
                c.setSortRecord(null);
                c.setOperator(null);
                allColumns.put(sourcefk, c);
            } else if (column.getColumnType() == ColumnType.CALLBACK){
                // 添加事实表表callback字段数据信息
                String sourcefk = "[Measure].[" + column.getFacttableColumnName() + "]";
                Column c = buildMeasureColumn(
                        miniCube.getSource(), column.getFacttableColumnName(),
                        column.getFacttableColumnName(), Aggregator.NONE, "", true, false);
                allColumns.put(sourcefk, c);
            }
        }
        setSortRecordInfo(allColumns, sortRecord);
        return allColumns;
    }
    
    protected static void setSortRecordInfo(Map<String, Column> allColumns, SortRecord sortRecord) {
        if (sortRecord == null) {
            return;
        }
        String orderColumnNameTmp = sortRecord
                .getSortColumnUniquename();
        if (!MetaNameUtil.isUniqueName(orderColumnNameTmp)) {
            throw new QuestionModelTransformationException(
                    "string of 'SortColumnUniquename' isn't well fromed as [data].[data] .");
        }
        Column column = allColumns.get(orderColumnNameTmp);
        if (column == null) {
            throw new QuestionModelTransformationException(
                    "string of 'SortColumnUniquename' is not any of cube column.");
        }
        if (column.getColumnType() == ColumnType.COMMON
                || column.getColumnType() == ColumnType.JOIN) {
            column.setSortRecord(sortRecord);
        }
        
    }
    
    /**
     * 设置一个普通维度的基本信息
     *
     * @param column
     *            column
     * @param v
     *            dimension
     * @param source
     *            source
     */
    private static void setSingleCommonDimensionData(Column column, Dimension v, String source) {
        Level oneDimensionSource = null;
        Level[] levels = v.getLevels().values().toArray(new Level[0]);
        oneDimensionSource = levels[levels.length - 1];
        column.setName(oneDimensionSource.getName());
        column.setTableFieldName(oneDimensionSource.getName());
        column.setRelatedColumn(false);
        column.setCaption(v.getCaption());
        column.setFacttableName(source);
        column.setFacttableColumnName(oneDimensionSource.getFactTableColumn());
        column.setFacttableColumnName(v.getFacttableColumn());
        if (PlaneTableUtils.isTimeDimension(v)) {
            // 如果为时间维度，转换成事实表的时间字段
            column.setTableFieldName(column.getFacttableColumnName());
            column.setColumnType(ColumnType.TIME);
            column.setTableName(oneDimensionSource.getDimTable());
        } else if (PlaneTableUtils.isCallbackDimension(v)) {
            // 如果为callback维度，转换成事实表的时间字段
            column.setColumnType(ColumnType.CALLBACK);
        } else {
            column.setColumnType(ColumnType.JOIN);
            column.setTableName(oneDimensionSource.getDimTable());
            column.setJoinTable(convertLevels2JoinList(levels[0]));
        }
    }
    
    /**
     * 设置一个普通维度的基本信息
     *
     * @param column
     *            column
     * @param v
     *            dimension
     * @param source
     *            source
     */
    private static void setSingleDimensionPrimaryKeyData(Column column, Column columnOri, String source) {
        String dimPk = columnOri.getJoinTable().getJoinOnList().get(0).getJoinTableFieldName();
        column.setColumnType(ColumnType.JOIN);
        column.setTableName(columnOri.getTableName());
        column.setJoinTable(null);
        column.setRelatedColumn(true);
        column.setName(dimPk);
        column.setTableFieldName(dimPk);
        column.setCaption(dimPk);
        column.setFacttableName(source);
        column.setFacttableColumnName(columnOri.getFacttableColumnName());
    }
    
    
    protected static Column buildMeasureColumn(String sourceTableName, String define,
            String caption, Aggregator aggregator, String formula, boolean isRelatedColumn, boolean isCB){
        Column oneMeasure = new Column();
        oneMeasure.setName(define);
        oneMeasure.setTableFieldName(define);
        if (isCB) {
            oneMeasure.setColumnType(ColumnType.MEASURE_CALLBACK);
        } else {
            oneMeasure.setColumnType(ColumnType.COMMON);
        }
        oneMeasure.setTableName(sourceTableName);
        oneMeasure.setFacttableName(sourceTableName);
        oneMeasure.setFacttableColumnName(define);
        oneMeasure.setCaption(caption);
        oneMeasure.setRelatedColumn(isRelatedColumn);
        Operator operator = new Operator();
        oneMeasure.setOperator(operator);
        operator.setAggregator(aggregator);
        if (aggregator == Aggregator.CALCULATED && !isCB) {
            oneMeasure.setColumnType(ColumnType.CAL);
            operator.setFormula(formula);
        }
        return oneMeasure;
    }
    /**
     * 设置一个普通Level的基本信息
     *
     * @param column
     *            column
     * @param v
     *            dimension
     * @param source
     *            source
     */
    private static void setSingleCommonLevelData(Column column, Level v, String source) {
        Level oneDimensionSource = v;
        column.setColumnType(ColumnType.JOIN);
        column.setTableName(oneDimensionSource.getDimTable());
        column.setJoinTable(convertLevels2JoinList(v));
        column.setName(oneDimensionSource.getName());
        column.setTableFieldName(oneDimensionSource.getName());
        column.setCaption(v.getCaption());
        column.setFacttableName(source);
        column.setFacttableColumnName(oneDimensionSource.getFactTableColumn());
    }
    
    /**
     * convertLevels2JoinList
     *
     * @param levels
     * @return List<Join>
     */
    private static JoinTable convertLevels2JoinList(Level level) {
        JoinTable join = new JoinTable();
        if (level == null) {
            return null;
        }
        join.setTableName(level.getDimTable());
        JoinOn joinOn = new JoinOn();
        joinOn.setFacttableName(SqlConstants.SOURCE_TABLE_ALIAS_NAME);
        joinOn.setFacttableColumnName(level.getFactTableColumn());
        joinOn.setJoinTableName(level.getDimTable());
        joinOn.setJoinTableFieldName(level.getPrimaryKey());
        join.getJoinOnList().add(joinOn);
        return join;
    }
    
    /**
     * 转换QueryCondition
     * 
     * @param allColums
     *            allColums
     * @param metaConditionMap
     *            metaConditionMap
     * 
     * @return Map<String, MetaCondition> 转换后带有columncondition的metacondition
     */
    public static Map<String, MetaCondition> convertQueryConditions(MiniCube miniCube,
            Map<String, Column> allColums, Map<String, MetaCondition> metaConditionMap) {
        Map<String, MetaCondition> result = new HashMap<String, MetaCondition>();
        for (String k : metaConditionMap.keySet()) {
            
            MetaCondition metaCondition = metaConditionMap.get(k);
            if (metaCondition instanceof MeasureCondition) {
                // 判断是指标查询
                
                String key = "[Measure].[" + k + "]";
                MeasureCondition measureCondition = (MeasureCondition) metaCondition;
                if (measureCondition.getMeasureConditions() != null) {
                    ColumnCondition columnCondition = new ColumnCondition(key);
                    columnCondition.setColumnConditions(measureCondition.getMeasureConditions());
                    result.put(key, columnCondition);
                }
            } else if (metaCondition instanceof DimensionCondition) {
                // 判断条件为维度组查询
                
                Dimension d = miniCube.getDimensions().get(k);
                if (d.getType() != DimensionType.GROUP_DIMENSION) {
                    // 普通维度条件
                    String key = "[Dimension].[" + k + "]";
                    ColumnCondition columnCondition = getDimensionColumnCondition(metaCondition,
                            key, 0);
                    if (columnCondition != null) {
                        result.put(key, columnCondition);
                    }
                } else {
                    // 维度组条件，此情况下目前只能处理维度中in查询
                    Dimension dimensionGroup = d;
                    Object[] levels = dimensionGroup.getLevels().values().toArray();
                    for (int i = 0; i < levels.length; i++) {
                        Level level = (Level) levels[i];
                        String printKeys = "";
                        Column column = null;
                        if (CUBE_TIME_TABLE_NAME.equals(level.getDimTable())
                                || miniCube.getSource().equals(level.getDimTable())) {
                            logger.warn(
                                    "queryId:{} cann't handle 'column of source table,time,"
                                    + "callback in dimension group now.',"
                                    + "allcolumn keys:{}", QueryRouterContext.getQueryId(), printKeys);
                            continue;
                        }
                        // 查找对应的字段信息
                        String currentKey = "";
                        for (String key : allColums.keySet()) {
                            printKeys = printKeys + key + ",";
                            String name = key.substring(key.lastIndexOf("[") + 1, key.lastIndexOf("]"));
                            // 目前获取uniquename的方式
                            String lvlName = level.getDimTable() + "_" + level.getName();
                            if (lvlName.equals(name)) {
                                currentKey = key;
                                column = allColums.get(key);
                                break;
                            }
                        }
                        if (column == null) {
                            // 此处如果出现证明cube不正确
                            logger.warn(
                                    "queryId:{} group dimension:{} levels:{} can not find in allColumns,"
                                            + "allcolumn keys:{}", QueryRouterContext.getQueryId(),
                                                    d.getName(), level.getName(), printKeys);
                            continue;
                        }
                        // 在cube找到对应的column
                        
                        // 此处目前只处理维度
                        // 根据level中的index与condition中unique 那么的数据作为in条件
                        ColumnCondition columnCondition = getDimensionColumnCondition(
                                metaCondition, currentKey, i+1);
                        if (columnCondition != null) {
                            result.put(currentKey, columnCondition);
                        }
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * getDimensionColumnCondition
     *
     * @param metacondition
     * @param allColumnMapkey
     * @param dimensionGroupIndex
     *            level中对应的维度index与metacondition众uniquename中的 []index位置一样
     *            如果index为-1或大于(uniquename[].length - 1)，则去最后一个uniquename []中的值
     * @return columnCondition
     */
    private static ColumnCondition getDimensionColumnCondition(MetaCondition metacondition,
            String allColumnMapkey, int dimensionGroupIndex) {
        DimensionCondition dimensionCondition = (DimensionCondition) metacondition;
        if (CollectionUtils.isEmpty(dimensionCondition.getQueryDataNodes())) {
            // 如果QueryDataNodes 为null的情况
            return null;
        }
        if (dimensionCondition.getQueryDataNodes().size() == 1
                && MetaNameUtil.isAllMemberUniqueName(dimensionCondition.getQueryDataNodes()
                        .get(0).getUniqueName(), dimensionGroupIndex)) {
            // 如果为一个查询节点并且为all members的情况
            return null;
        }
        ColumnCondition columnCondition = SqlColumnUtils
                .buildColumnCondition(SQLConditionType.IN, new ArrayList<String>());
        for (QueryData queryData : dimensionCondition.getQueryDataNodes()) {
            if (MetaNameUtil.isAllMemberUniqueName(dimensionCondition.getQueryDataNodes().get(0)
                    .getUniqueName(), dimensionGroupIndex)) {
                // 过滤等于all的情况
                continue;
            }
            String[] str = MetaNameUtil.parseUnique2NameArray(queryData.getUniqueName());
            String value = null;
            if (str.length <= 1) {
                // cube 不正确
                logger.warn("queryId:{} length of uniquename:{} is not right.",
                        QueryRouterContext.getQueryId(), queryData.getUniqueName());
                return null;
            }
            if (dimensionGroupIndex < 1 || dimensionGroupIndex > str.length - 1) {
            // 如果dimensionGroupIndex非法,那么设置为取数组最后的数据
            // 大于的情况，可能是点击下钻时出来的情况，默认取数组最后的数据，此处需要优化
                dimensionGroupIndex = str.length - 1;
            }
            value = str[dimensionGroupIndex];
            columnCondition.getColumnConditions().getConditionValues().add(value);
        }
        return columnCondition;
    }
    
    /**
     * isTimeDimension
     * 
     * @param dimension
     *            dimension
     * @return isTimeDimension
     */
    public static boolean isTimeDimension(Dimension dimension) {
        if (dimension != null
                && dimension.getType() != null
                && dimension.getType() == DimensionType.TIME_DIMENSION) {
            // 判断是否是timedimension维度
            return true;
        }
        return false;
    }
    
    /**
     * isCallbackDimension
     * 
     * @param dimension
     *            dimension
     * @return isCallbackDimension
     */
    public static boolean isCallbackDimension(Dimension dimension) {
        if (!dimension.getLevels().isEmpty()) {
            if (dimension.getLevels().entrySet().iterator().next().getValue() instanceof CallbackLevel) {
                // 判断是否是callback维度
                return true;
            }
        }
        return false;
    }
}
