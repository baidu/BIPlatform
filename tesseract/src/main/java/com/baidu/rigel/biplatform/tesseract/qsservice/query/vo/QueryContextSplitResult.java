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
package com.baidu.rigel.biplatform.tesseract.qsservice.query.vo;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.parser.context.CompileContext;
import com.baidu.rigel.biplatform.parser.context.Condition;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.QueryContextSplitService.QueryContextSplitStrategy;

/**
 * 查询上下文拆分结果
 * 
 * @author xiaoming.chen
 *
 */
public class QueryContextSplitResult {

    /**
     * measureCaculateExpression 指标的计算公式， 后续的Value应该会变成计算公式解析后的对象
     */
    private Map<String, String> measureCaculateExpression;

    /**
     * splitStrategy 拆分策略，根据不同策略最后做数据合并的时候需要进行不同处理
     */
    private QueryContextSplitStrategy splitStrategy;
    

    private Map<Condition,QueryContext> conditionQueryContext;
    
    
    /** 
     * compileContexts 计算列的上下文
     */
    private Map<String, CompileContext> compileContexts;
    
    
    
    /** 
     * oriQueryContext 基础的上下文，最终结果集根据这个上下文拼接
     */
    private QueryContext oriQueryContext;
    
    
    private Map<Condition,DataModel> dataModels;
    /**
     * constructor
     * 
     * @param splitStrategy
     */
    public QueryContextSplitResult(QueryContextSplitStrategy splitStrategy, QueryContext oriContext) {
        this.splitStrategy = splitStrategy;
        // 深拷贝，避免外面的被改变
        this.oriQueryContext = DeepcopyUtils.deepCopy(oriContext);
    }


    /**
     * get measureCaculateExpression
     * 
     * @return the measureCaculateExpression
     */
    public Map<String, String> getMeasureCaculateExpression() {
        return measureCaculateExpression;
    }

    /**
     * set measureCaculateExpression with measureCaculateExpression
     * 
     * @param measureCaculateExpression the measureCaculateExpression to set
     */
    public void setMeasureCaculateExpression(Map<String, String> measureCaculateExpression) {
        this.measureCaculateExpression = measureCaculateExpression;
    }

    /**
     * get splitStrategy
     * 
     * @return the splitStrategy
     */
    public QueryContextSplitStrategy getSplitStrategy() {
        return splitStrategy;
    }

    /**
     * set splitStrategy with splitStrategy
     * 
     * @param splitStrategy the splitStrategy to set
     */
    public void setSplitStrategy(QueryContextSplitStrategy splitStrategy) {
        this.splitStrategy = splitStrategy;
    }

    /** 
     * 获取 conditionQueryContext 
     * @return the conditionQueryContext 
     */
    public Map<Condition, QueryContext> getConditionQueryContext() {
        if(MapUtils.isEmpty(conditionQueryContext)) {
            this.conditionQueryContext = new HashMap<Condition, QueryContext>();
        }
        return conditionQueryContext;
    }

    /** 
     * 设置 conditionQueryContext 
     * @param conditionQueryContext the conditionQueryContext to set 
     */
    public void setConditionQueryContext(Map<Condition, QueryContext> conditionQueryContext) {
    
        this.conditionQueryContext = conditionQueryContext;
    }


    /** 
     * 获取 oriQueryContext 
     * @return the oriQueryContext 
     */
    public QueryContext getOriQueryContext() {
    
        return oriQueryContext;
    }


    /** 
     * 设置 oriQueryContext 
     * @param oriQueryContext the oriQueryContext to set 
     */
    public void setOriQueryContext(QueryContext oriQueryContext) {
    
        this.oriQueryContext = oriQueryContext;
    }


    /** 
     * 获取 compileContexts 
     * @return the compileContexts 
     */
    public Map<String, CompileContext> getCompileContexts() {
        if(this.compileContexts == null) {
            this.compileContexts = new HashMap<>();
        }
        return compileContexts;
    }


    /** 
     * 设置 compileContexts 
     * @param compileContexts the compileContexts to set 
     */
    public void setCompileContexts(Map<String, CompileContext> compileContexts) {
        this.compileContexts = compileContexts;
    }


    /** 
     * 获取 dataModels 
     * @return the dataModels 
     */
    public Map<Condition, DataModel> getDataModels() {
        if(dataModels == null) {
            this.dataModels = new HashMap<Condition, DataModel>();
        }
        return dataModels;
    }


    /** 
     * 设置 dataModels 
     * @param dataModels the dataModels to set 
     */
    public void setDataModels(Map<Condition, DataModel> dataModels) {
    
        this.dataModels = dataModels;
    }


    
    /*
     * (non-Javadoc) 
     * @see java.lang.Object#toString() 
     */
    @Override
    public String toString() {
        return "QueryContextSplitResult [measureCaculateExpression=" + measureCaculateExpression + ", splitStrategy="
                + splitStrategy + ", conditionQueryContext=" + conditionQueryContext + ", compileContexts="
                + compileContexts + ", oriQueryContext=" + oriQueryContext + ", dataModels=" + getDataModels().size() + "]";
    }

}
