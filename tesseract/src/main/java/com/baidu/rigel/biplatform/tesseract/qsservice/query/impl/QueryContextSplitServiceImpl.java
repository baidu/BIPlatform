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
package com.baidu.rigel.biplatform.tesseract.qsservice.query.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.ac.minicube.ExtendMinicubeMeasure;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMeasure;
import com.baidu.rigel.biplatform.ac.model.Aggregator;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.MeasureType;
import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.query.data.DataModel.FillDataType;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.data.HeadField;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.ac.util.DataModelUtils;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ac.util.PlaceHolderUtils;
import com.baidu.rigel.biplatform.parser.CompileExpression;
import com.baidu.rigel.biplatform.parser.context.CompileContext;
import com.baidu.rigel.biplatform.parser.context.Condition;
import com.baidu.rigel.biplatform.parser.context.EmptyCondition;
import com.baidu.rigel.biplatform.parser.result.ComputeResult;
import com.baidu.rigel.biplatform.parser.result.ListComputeResult;
import com.baidu.rigel.biplatform.parser.util.ConditionUtil;
import com.baidu.rigel.biplatform.tesseract.dataquery.udf.condition.CallbackCondition;
import com.baidu.rigel.biplatform.tesseract.dataquery.udf.condition.ParseCoditionUtils;
import com.baidu.rigel.biplatform.tesseract.exception.IllegalSplitResultException;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.QueryContextBuilder;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.QueryContextSplitService;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryContext;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryContextSplitResult;
import com.baidu.rigel.biplatform.tesseract.util.DataModelBuilder;



/**
 * 按照查询上下文自动拆分实现
 * 
 * @author xiaoming.chen
 *
 */
@Service
public class QueryContextSplitServiceImpl implements QueryContextSplitService {
    
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String NONE = "NONE";
    
    @Resource
    private QueryContextBuilder queryContextBuilder;
    

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryContextSplitResult split(QuestionModel question, DataSourceInfo dsInfo, Cube cube, 
            QueryContext queryContext,
            QueryContextSplitStrategy preSplitStrategy) {
        QueryContextSplitStrategy splitStrategy = QueryContextSplitStrategy.getNextStrategy(preSplitStrategy);
        // 如果下一次拆分已经没有可拆分的了，那么说明已经不需要再进行拆分了
        if (splitStrategy != null) {
            if (splitStrategy.equals(QueryContextSplitStrategy.MeasureType)) {
                return splitByMeasureTypeStrategy(question, dsInfo, cube, queryContext);
                // TODO 后续实现
//            } else if (splitStrategy.equals(QueryContextSplitStrategy.Column)) {
//                return splitByColumnStrategy(queryContext);
//            } else {
//                return splitByRowStrategy(queryContext);
            }
        }
        return null;
    }

    /**
     * @param cube
     * @param queryContext
     * @return
     */
    private QueryContextSplitResult splitByMeasureTypeStrategy(QuestionModel question, DataSourceInfo dsInfo, Cube cube, QueryContext queryContext) {
        QueryContextSplitResult result = new QueryContextSplitResult(QueryContextSplitStrategy.MeasureType, queryContext);
        // 按照指标类型拆分，只考虑指标类型
        if (CollectionUtils.isNotEmpty(queryContext.getQueryMeasures())) {
            Set<String> callbackMeasureName = new HashSet<String>();
            CompileContext compileContext = null;
            for(Iterator<MiniCubeMeasure> it = queryContext.getQueryMeasures().iterator(); it.hasNext();) {
                MiniCubeMeasure measure = it.next();
                // 取出所有的计算列指标
                if (measure.getAggregator().equals(Aggregator.CALCULATED)) {
                    if(measure.getType().equals(MeasureType.CALLBACK)) {
                        callbackMeasureName.add(measure.getUniqueName());
                    } else {
                        ExtendMinicubeMeasure extendMeasure = (ExtendMinicubeMeasure) measure;
                        compileContext = CompileExpression.compile(extendMeasure.getFormula());
                        result.getCompileContexts().put(measure.getUniqueName(), compileContext);
                    }
                    it.remove();
                }
            }
            // 处理计算列
            Map<Condition, Set<String>> conditions = ConditionUtil.simpleMergeContexsCondition(result.getCompileContexts().values());
            if(CollectionUtils.isNotEmpty(callbackMeasureName)) {
                conditions.put(CallbackCondition.getInstance(), callbackMeasureName);
            }
            if(!queryContext.getQueryMeasures().isEmpty()) {
                if(!conditions.containsKey(EmptyCondition.getInstance())) {
                    conditions.put(EmptyCondition.getInstance(), new HashSet<>());
                }
                for(MiniCubeMeasure m : queryContext.getQueryMeasures()) {
                    if(!conditions.get(EmptyCondition.getInstance()).contains(m.getName())) {
                        conditions.get(EmptyCondition.getInstance()).add(m.getName());
                    }
                }
                
            }
            
            
            if(MapUtils.isNotEmpty(conditions)) {
                conditions.forEach((con, vars) -> {
                    QueryContext context = con.processCondition(
                            ParseCoditionUtils.decorateQueryContext(DeepcopyUtils.deepCopy(queryContext), 
                            question, cube, dsInfo, queryContextBuilder));
                    context.getQueryMeasures().clear();
                    for(String var : vars) {
                        MiniCubeMeasure measure = null;
                        if (MetaNameUtil.isUniqueName(var)) {
                            String name = MetaNameUtil.parseUnique2NameArray(var)[1];
                            measure = (MiniCubeMeasure) cube.getMeasures().get(name);
                        } else {
                            measure = (MiniCubeMeasure) cube.getMeasures().get(PlaceHolderUtils.getKeyFromPlaceHolder(var));
                        }
                        if(measure == null) {
                            throw new IllegalSplitResultException(result, "can not get measure:" + var + " from cube", "SPILT_QUESTION");
                        }
                        if(!context.getQueryMeasures().contains(measure)) {
                            context.getQueryMeasures().add(measure);
                        }
                    }
                    result.getConditionQueryContext().put(con, context);
                    
                });
            }
        }

        return result;
    }

    
    /*
     * (non-Javadoc) 
     * @see com.baidu.rigel.biplatform.tesseract.qsservice.query.QueryContextSplitService#mergeDataModel(com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryContextSplitResult) 
     */
    @Override
    public DataModel mergeDataModel(QueryContextSplitResult splitResult) {
        long current = System.currentTimeMillis();
        if(splitResult == null || splitResult.getConditionQueryContext().size() != splitResult.getDataModels().size()) {
            throw new IllegalSplitResultException(splitResult, "splitResult is null or condition size not equal", "MERGE_MODEL");
        }
        /**
         * 注意：如果分组策略只有一种，这里不能直接返回，需要对计算列进行处理
         */
        final DataModel dataModel = new DataModel();
        
        // 根据原生查询请求信息，构建行列信息，与TesseractResultSet到DataModel的流程类似
        dataModel.setColumnHeadFields(
                DataModelBuilder.buildAxisHeadFields(splitResult.getOriQueryContext().getColumnMemberTrees(),
                splitResult.getOriQueryContext().getQueryMeasures()));
        dataModel.setRowHeadFields(
            DataModelBuilder.buildAxisHeadFields(splitResult.getOriQueryContext().getRowMemberTrees(), null));
        
        List<HeadField> rowLeafs = DataModelUtils.getLeafNodeList(dataModel.getRowHeadFields());
        
        // 条件，指标UniqueName，父节点的NodeUniqueName，数据
        Map<Condition, Map<String, Map<String, List<BigDecimal>>>> dataModelDatas = 
                new HashMap<Condition, Map<String,Map<String,List<BigDecimal>>>>(splitResult.getDataModels().size());
        if(!dataModelDatas.containsKey(EmptyCondition.getInstance())) {
            dataModelDatas.put(EmptyCondition.getInstance(), new HashMap<>());
        }
        
        // 将DataModel的data按照condition分类
        splitResult.getDataModels().forEach((con, dm) -> {
            boolean isCallbackCondition = con.equals(CallbackCondition.getInstance());
            con = isCallbackCondition ? EmptyCondition.getInstance() : con;
            // 先把数据按照列封装了：将数据放入列头，方便处理并且保证数据在处理过程中不发生变化
            DataModelUtils.fillFieldData(dm, FillDataType.COLUMN);
            // 获取column field的叶子节点值
            List<HeadField> columnFields = DataModelUtils.getLeafNodeList(dm.getColumnHeadFields());
            Map<String, Map<String, List<BigDecimal>>> dataModelData = null;
            if(dataModelDatas.containsKey(con)) {
                dataModelData = dataModelDatas.get(con);
            }else {
                dataModelData = new HashMap<String, Map<String, List<BigDecimal>>>();
            }
            
            // 存放叶子节点信息
            for(HeadField field : columnFields) {
                // 当前节点为列维度的最底层节点，如果指标在列上，则为指标元定义
                if(!dataModelData.containsKey(field.getValue())) {
                    dataModelData.put(field.getValue(), new HashMap<>());
                }
                String parentKey = NONE;
                if (field.getParentLevelField() != null) {
                    parentKey = field.getParentLevelField().getNodeUniqueName();
                }
                dataModelData.get(field.getValue()).put(parentKey, field.getCompareDatas());
            }
            dataModelDatas.put(con, dataModelData);
            
        });
        

        // 常量公式计算出来的值
        Map<String, List<BigDecimal>> constantResult = new HashMap<>(1);
        // TODO 优化循环策略
        splitResult.getCompileContexts().forEach((measureName,compileContext) -> {
           Map<String, List<BigDecimal>> calCulateDatas = new HashMap<>();
           Map<String, Map<Condition, Map<String, ComputeResult>>> categoryVariableVal = new HashMap<>();
           if (MapUtils.isNotEmpty(compileContext.getConditionVariables())) {
               // compileContext.getConditionVariables().entrySet() 的map中value的Set为表达式引用的指标名称
               for(Entry<Condition,Set<String>> entry : compileContext.getConditionVariables().entrySet()) {
                   Map<String, Map<String, List<BigDecimal>>> dataModelData = dataModelDatas.get(entry.getKey());
                   if(dataModelData == null) {
                       throw new IllegalSplitResultException(splitResult, "dataModel is null by condition" + entry.getKey(), "MERGE_MODEL");
                   }
                   // parentNode uniqueName, varName, data
                   if (CollectionUtils.isNotEmpty(entry.getValue())) {
                       // 遍历表达式引用的指标名称，计算当前表达式引用的指标值
                       // TODO 
                       for(String var : entry.getValue()) {
                           String name = MetaNameUtil.generateMeasureUniqueName(PlaceHolderUtils.getKeyFromPlaceHolder(var));
                           if (!dataModelData.containsKey(name)) {
                               throw new IllegalSplitResultException(splitResult, "miss variable:" + var, "MERGE_MODEL");
                           }
                           for(String parentNodeUniqueName : dataModelData.get(name).keySet()) {
                               if(!categoryVariableVal.containsKey(parentNodeUniqueName)) {
                                   categoryVariableVal.put(parentNodeUniqueName, new HashMap<>());
                               }
                               if (!categoryVariableVal.get(parentNodeUniqueName).containsKey(entry.getKey())) {
                                   categoryVariableVal.get(parentNodeUniqueName).put(entry.getKey(), new HashMap<>());
                               }
                               categoryVariableVal.get(parentNodeUniqueName).get(entry.getKey())
                                   .put(var, new ListComputeResult(dataModelData.get(name).get(parentNodeUniqueName)));
                           }
                       }
                   } else {
                       constantResult.put(measureName, 
                           ListComputeResult.transfer(compileContext.getNode().getResult(null), rowLeafs.size()).getData());
                   }
               }
               for(String parentName : categoryVariableVal.keySet()) {
                   // 清理一下，避免对后续造成影响
                   compileContext.getVariablesResult().clear();
                   compileContext.setVariablesResult(categoryVariableVal.get(parentName));
//                   compileContext.getVariablesResult().put(entry.getKey(), categoryVariableVal.get(parentName));
                   calCulateDatas.put(parentName, 
                       ListComputeResult.transfer(compileContext.getNode().getResult(compileContext), rowLeafs.size()).getData());
               }
           }
           
           dataModelDatas.get(EmptyCondition.getInstance()).put(measureName, calCulateDatas);
        });
        mergeDataModelDatas(dataModel, dataModelDatas.get(EmptyCondition.getInstance()),constantResult);
        log.info("merge datamodel cost:{}ms",System.currentTimeMillis() - current);
        return dataModel;
    }
    
    
    
    /** 
     * mergeDataModelDatas
     * @param dataModel
     * @param datas
     * @param constantResult
     */
    private void mergeDataModelDatas(DataModel dataModel, Map<String, Map<String, List<BigDecimal>>> datas, Map<String, List<BigDecimal>> constantResult){
        List<HeadField> rowLeafs = DataModelUtils.getLeafNodeList(dataModel.getRowHeadFields());
        
        List<HeadField> oriColumnFields = DataModelUtils.getLeafNodeList(dataModel.getColumnHeadFields());
        for (HeadField field : oriColumnFields) {
            if(constantResult.containsKey(field.getValue())) {
                field.setCompareDatas(constantResult.get(field.getValue()));
            } else {
                String pName = NONE;
                if(field.getParentLevelField() != null) {
                    pName = field.getParentLevelField().getNodeUniqueName();
                }
                field.setCompareDatas(datas ==  null ? null : datas.get(field.getValue()).get(pName));
            }
        }
        
        List<HeadField> columnLeafs = DataModelUtils.getLeafNodeList(dataModel.getColumnHeadFields());
        dataModel.getColumnBaseData().clear();

        for (int i = 0; i < columnLeafs.size(); i++) {
            HeadField rowField = columnLeafs.get(i);
            dataModel.getColumnBaseData().add(rowField.getCompareDatas());
            while (dataModel.getColumnBaseData().get(i).size() < rowLeafs.size()) {
                dataModel.getColumnBaseData().get(i).add(null);
            }
        }
        
    }
    

}
