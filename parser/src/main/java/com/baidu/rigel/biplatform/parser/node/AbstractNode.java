
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
package com.baidu.rigel.biplatform.parser.node;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.baidu.rigel.biplatform.parser.context.CompileContext;
import com.baidu.rigel.biplatform.parser.context.Condition;
import com.baidu.rigel.biplatform.parser.exception.IllegalCompileContextException;
import com.baidu.rigel.biplatform.parser.result.ComputeResult;
import com.baidu.rigel.biplatform.parser.result.ListComputeResult;
import com.baidu.rigel.biplatform.parser.result.SingleComputeResult;
import com.baidu.rigel.biplatform.parser.result.ComputeResult.ResultType;

/** 
 *  
 * @author xiaoming.chen
 * @version  2014年12月8日 
 * @since jdk 1.8 or after
 */
public abstract class AbstractNode implements Node {
    
    
    /** 
     * serialVersionUID
     */
    private static final long serialVersionUID = -4365139962446658590L;

    
    /** 
     * id 每个节点都有个唯一的ID
     */
    private String id;
    
    
    /** 
     * result 结果
     */
    protected ComputeResult result;
    
    protected Map<Condition, Set<String>> conditions;
    
    /** 
     * 获取 id 
     * @return the id 
     */
    public String getId() {
    
        return id;
    }

    /** 
     * 设置 id 当且仅当当前节点需要和其它节点一致的时候，那么将已有的ID设置给当前节点，
     * 一般变量节点使用
     * 慎用
     * @param id the id to set 
     */
    public void setId(String id) {
    
        this.id = id;
    }
    
    
    /** 
     * processNodes
     * @throws IllegalCompileContextException 
     */
    protected void processNodes(List<Node> nodes, CompileContext context) throws IllegalCompileContextException {
        if(CollectionUtils.isNotEmpty(nodes)) {
            List<BigDecimal> multiResults = null;
            BigDecimal singleResult = null;
            ComputeResult result = null;
            for(Node node : nodes) {
                result = node.getResult(context);
                if(result.getResultType().equals(ResultType.SINGLE)) {
                    SingleComputeResult single = (SingleComputeResult) result;
                    if(singleResult == null) {
                        singleResult = single.getData();
                    } else {
                        singleResult = computeData(singleResult, single.getData());
                    }
                } else {
                    ListComputeResult listResult = (ListComputeResult) result;
                    if (CollectionUtils.isEmpty(multiResults)) {
                        multiResults = new ArrayList<BigDecimal>(listResult.getData());
                    } else if(CollectionUtils.isNotEmpty(listResult.getData())){
                        // 先保证2个list的长度是一致的，如果不一致，直接抛异常
                        if(multiResults.size() != listResult.getData().size()) {
                            throw new IllegalArgumentException("operate argument length is not the same,one:" + listResult.getData().size()
                                    + " the other:" + multiResults.size());
                        } else {
                            for (int i = 0; i < multiResults.size(); i++) {
                                multiResults.set(i, computeData(multiResults.get(i), listResult.getData().get(i)));
                            }
                        }
                    }
                }
            }
            if(multiResults != null) {
                if(singleResult != null) {
                    for (int i = 0; i < multiResults.size(); i++) {
                        multiResults.set(i, computeData(multiResults.get(i), singleResult));
                    }
                }
                setResult(new ListComputeResult(multiResults));
            } else {
                setResult(new SingleComputeResult(singleResult));
            }
        }
    }
    
    /** 
     * checkCompileContext
     * @param context
     * @throws IllegalCompileContextException
     */
    protected void checkCompileContext(CompileContext context) throws IllegalCompileContextException {
        if(context == null) {
            throw new IllegalArgumentException("illegal context");
        }else {
            if(context.getVariablesResult() == null) {
                throw new IllegalCompileContextException("no variable result return", context);
            }
        }
    }
    
    
    @Override
    public Map<Condition, Set<String>> collectVariableCondition() {
        if(this.conditions == null) {
            this.conditions = collect();
        }
        return this.conditions;
    }
    
    protected abstract Map<Condition, Set<String>> collect();
    
    /** 
     * compute
     * @param arg1
     * @param arg2
     * @return
     */
    private BigDecimal computeData(BigDecimal arg1, BigDecimal arg2) {
        if(arg1 == null && arg2 == null) {
            return null;
        }
        if (arg1 == null) {
            arg1 = BigDecimal.ZERO;
        }
        if (arg2 == null) {
            arg2 = BigDecimal.ZERO;
        }
        return compute(arg1, arg2);
    }
    
    protected abstract BigDecimal compute(BigDecimal arg1, BigDecimal arg2);
    
    
    public void setResult(ComputeResult t) {
        this.result = t;
    }
}

