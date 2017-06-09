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
package com.baidu.rigel.biplatform.queryrouter.query.udf;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.baidu.rigel.biplatform.parser.context.CompileContext;
import com.baidu.rigel.biplatform.parser.context.Condition;
import com.baidu.rigel.biplatform.parser.exception.IllegalCompileContextException;
import com.baidu.rigel.biplatform.parser.node.FunctionNode;
import com.baidu.rigel.biplatform.parser.node.Node;
import com.baidu.rigel.biplatform.parser.result.ComputeResult;
import com.baidu.rigel.biplatform.parser.result.ListComputeResult;
import com.baidu.rigel.biplatform.parser.result.SingleComputeResult;

/**
 * 
 * Description: 根据时间偏移量计算指标数据
 * 
 * @author david.wang
 *
 */
public class HelloFunction extends FunctionNode {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -2557343941040637669L;
    
    @Override
    public String getName() {
        return "hello";
    }
    
    @Override
    public Map<Condition, Set<String>> mergeCondition(Node node) {
        return node.collectVariableCondition();
    }
    
    @Override
    protected BigDecimal compute(BigDecimal arg1, BigDecimal arg2) {
        return BigDecimal.ZERO.subtract(arg1);
    }
    
    @Override
    public ComputeResult getResult(CompileContext context) throws IllegalCompileContextException {
        Node args = getArgs().get(0);
        Node args2 = getArgs().get(1);
        ListComputeResult rs = (ListComputeResult) args.getResult(context);
        SingleComputeResult rs1 = (SingleComputeResult) args2.getResult(context);
        List<BigDecimal> tmp = new ArrayList<>();
        for (BigDecimal data : rs.getData()) {
            if (data == null) {
                tmp.add(rs1.getData());
            } else {
                tmp.add(rs1.getData().add(data));
            }
            
        }
        return new ListComputeResult(tmp);
    }
    
    @Override
    public int getArgsLength() {
        return 2;
    }
    
}
