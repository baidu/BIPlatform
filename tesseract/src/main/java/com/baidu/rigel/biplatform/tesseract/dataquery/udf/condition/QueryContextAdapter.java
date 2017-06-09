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
package com.baidu.rigel.biplatform.tesseract.dataquery.udf.condition;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMeasure;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.model.AxisMeta.AxisType;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.tesseract.model.MemberNodeTree;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.QueryContextBuilder;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryContext;

/**
 * Description: QueryContextAdapter
 * @author david.wang
 *
 */
public class QueryContextAdapter extends QueryContext {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 7631126790794933111L;
    
    /**
     * queryContext
     */
    private final QueryContext queryContext;
    
    /**
     * questionModel
     */
    private final QuestionModel questionModel;
    
    /**
     * cube
     */
    private final Cube cube;
    
    /**
     * dataSoruceInfo
     */
    private final DataSourceInfo dataSoruceInfo;
    
    private final QueryContextBuilder builder;

    /**
     * 构造函数
     * @param queryContext
     * @param questionModel
     * @param cube
     * @param dataSoruceInfo
     */
    public QueryContextAdapter(QueryContext queryContext,
            QuestionModel questionModel, Cube cube,
            DataSourceInfo dataSoruceInfo, QueryContextBuilder builder) {
        super();
        this.queryContext = queryContext;
        this.questionModel = questionModel;
        this.cube = cube;
        this.dataSoruceInfo = dataSoruceInfo;
        this.builder = builder;
    }

    /**
     * @return the queryContext
     */
    public QueryContext getQueryContext() {
        return queryContext;
    }

    /**
     * @return the questionModel
     */
    public QuestionModel getQuestionModel() {
        return questionModel;
    }

    /**
     * @return the cube
     */
    public Cube getCube() {
        return cube;
    }

    /**
     * @return the dataSoruceInfo
     */
    public DataSourceInfo getDataSoruceInfo() {
        return dataSoruceInfo;
    }

    /**
     * @return the builder
     */
    public QueryContextBuilder getBuilder() {
        return builder;
    }

    
    /*
     * (non-Javadoc) 
     * @see com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryContext#getColumnMemberTrees() 
     */
    @Override
    public List<MemberNodeTree> getColumnMemberTrees() {
        return queryContext.getColumnMemberTrees();
        
    }

    
    /*
     * (non-Javadoc) 
     * @see com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryContext#setColumnMemberTrees(java.util.List) 
     */
    @Override
    public void setColumnMemberTrees(List<MemberNodeTree> columnMemberTrees) {
        queryContext.setColumnMemberTrees(columnMemberTrees);
        
    }

    
    /*
     * (non-Javadoc) 
     * @see com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryContext#getQueryMeasures() 
     */
    @Override
    public List<MiniCubeMeasure> getQueryMeasures() {
        return queryContext.getQueryMeasures();
        
    }

    
    /*
     * (non-Javadoc) 
     * @see com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryContext#setQueryMeasures(java.util.List) 
     */
    @Override
    public void setQueryMeasures(List<MiniCubeMeasure> queryMeasures) {
        queryContext.setQueryMeasures(queryMeasures);
        
    }

    
    /*
     * (non-Javadoc) 
     * @see com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryContext#getRowMemberTrees() 
     */
    @Override
    public List<MemberNodeTree> getRowMemberTrees() {
        return queryContext.getRowMemberTrees();
        
    }

    
    /*
     * (non-Javadoc) 
     * @see com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryContext#setRowMemberTrees(java.util.List) 
     */
    @Override
    public void setRowMemberTrees(List<MemberNodeTree> rowMemberTrees) {
        queryContext.setRowMemberTrees(rowMemberTrees);
        
    }

    
    /*
     * (non-Javadoc) 
     * @see com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryContext#getFilterMemberValues() 
     */
    @Override
    public Map<String, Set<String>> getFilterMemberValues() {
        return queryContext.getFilterMemberValues();
        
    }

    
    /*
     * (non-Javadoc) 
     * @see com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryContext#setFilterMemberValues(java.util.Map) 
     */
    @Override
    public void setFilterMemberValues(Map<String, Set<String>> filterMemberValues) {
        queryContext.setFilterMemberValues(filterMemberValues);
        
    }

    
    /*
     * (non-Javadoc) 
     * @see com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryContext#getFilterExpression() 
     */
    @Override
    public Map<String, List<String>> getFilterExpression() {
        return queryContext.getFilterExpression();
        
    }

    
    /*
     * (non-Javadoc) 
     * @see com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryContext#setFilterExpression(java.util.Map) 
     */
    @Override
    public void setFilterExpression(Map<String, List<String>> filterExpression) {
        queryContext.setFilterExpression(filterExpression);
        
    }

    
    /*
     * (non-Javadoc) 
     * @see com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryContext#addMemberNodeTreeByAxisType(com.baidu.rigel.biplatform.ac.query.model.AxisMeta.AxisType, com.baidu.rigel.biplatform.tesseract.model.MemberNodeTree) 
     */
    @Override
    public void addMemberNodeTreeByAxisType(AxisType axisType, MemberNodeTree nodeTree) {
        queryContext.addMemberNodeTreeByAxisType(axisType, nodeTree);
        
    }
    
}
