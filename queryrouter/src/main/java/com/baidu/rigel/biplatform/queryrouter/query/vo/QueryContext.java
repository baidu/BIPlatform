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
package com.baidu.rigel.biplatform.queryrouter.query.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMeasure;
import com.baidu.rigel.biplatform.ac.query.model.AxisMeta.AxisType;

/**
 * 查询上下文
 * 
 * @author xiaoming.chen
 *
 */
public class QueryContext implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -8499881971207190739L;

    /**
     * columnMemberTrees 列上的维值树，按顺序交叉
     */
    private List<MemberNodeTree> columnMemberTrees;

    /**
     * queryMeasures 列上的指标， TODO 后续需要考虑用户查询临时写的一个计算公式情况。
     */
    private List<MiniCubeMeasure> queryMeasures;

    /**
     * rowMemberTrees 行上维值交叉信息
     */
    private List<MemberNodeTree> rowMemberTrees;

    /**
     * filterMemberValues 过滤的维值
     */
    private Map<String, Set<String>> filterMemberValues;

    /**
     * filterExpression 过滤表达式，需重新考虑，保留信息，暂时不使用
     */
    private Map<String, List<String>> filterExpression;
    /**
     * 记录需要上移汇总的维度名称
     */
    private List<String> dimsNeedSumBySubLevel;

    /**
     * @return the dimsNeedSumBySubLevel
     */
    public List<String> getDimsNeedSumBySubLevel() {
        if (this.dimsNeedSumBySubLevel == null) {
            this.dimsNeedSumBySubLevel = new ArrayList<String>();
        }
        return dimsNeedSumBySubLevel;
    }

    /**
     * @param dimsNeedSumBySubLevel the dimsNeedSumBySubLevel to set
     */
    public void setDimsNeedSumBySubLevel(List<String> dimsNeedSumBySubLevel) {
        this.dimsNeedSumBySubLevel = dimsNeedSumBySubLevel;
    }

    /**
     * get columnMemberTrees
     * 
     * @return the columnMemberTrees
     */
    public List<MemberNodeTree> getColumnMemberTrees() {
        if (this.columnMemberTrees == null) {
            this.columnMemberTrees = new ArrayList<MemberNodeTree>();
        }
        return columnMemberTrees;
    }

    /**
     * set columnMemberTrees with columnMemberTrees
     * 
     * @param columnMemberTrees the columnMemberTrees to set
     */
    public void setColumnMemberTrees(List<MemberNodeTree> columnMemberTrees) {
        this.columnMemberTrees = columnMemberTrees;
    }

    /**
     * get queryMeasures
     * 
     * @return the queryMeasures
     */
    public List<MiniCubeMeasure> getQueryMeasures() {
        if (this.queryMeasures == null) {
            this.queryMeasures = new ArrayList<MiniCubeMeasure>();
        }
        return queryMeasures;
    }

    /**
     * set queryMeasures with queryMeasures
     * 
     * @param queryMeasures the queryMeasures to set
     */
    public void setQueryMeasures(List<MiniCubeMeasure> queryMeasures) {
        this.queryMeasures = queryMeasures;
    }

    /**
     * get rowMemberTrees
     * 
     * @return the rowMemberTrees
     */
    public List<MemberNodeTree> getRowMemberTrees() {
        if (this.rowMemberTrees == null) {
            this.rowMemberTrees = new ArrayList<MemberNodeTree>();
        }
        return rowMemberTrees;
    }

    /**
     * set rowMemberTrees with rowMemberTrees
     * 
     * @param rowMemberTrees the rowMemberTrees to set
     */
    public void setRowMemberTrees(List<MemberNodeTree> rowMemberTrees) {
        this.rowMemberTrees = rowMemberTrees;
    }

    /**
     * get filterMemberValues
     * 
     * @return the filterMemberValues
     */
    public Map<String, Set<String>> getFilterMemberValues() {
        if (this.filterMemberValues == null) {
            this.filterMemberValues = new HashMap<String, Set<String>>();
        }
        return filterMemberValues;
    }

    /**
     * set filterMemberValues with filterMemberValues
     * 
     * @param filterMemberValues the filterMemberValues to set
     */
    public void setFilterMemberValues(Map<String, Set<String>> filterMemberValues) {
        this.filterMemberValues = filterMemberValues;
    }

    /**
     * get filterExpression
     * 
     * @return the filterExpression
     */
    public Map<String, List<String>> getFilterExpression() {
        if (this.filterExpression == null) {
            this.filterExpression = new HashMap<String, List<String>>();
        }
        return filterExpression;
    }

    /**
     * set filterExpression with filterExpression
     * 
     * @param filterExpression the filterExpression to set
     */
    public void setFilterExpression(Map<String, List<String>> filterExpression) {
        this.filterExpression = filterExpression;
    }

    /**
     * 根据轴的类型，将维值节点扔到查询上下文中
     * 
     * @param axisType 轴的类型
     * @param nodeTree 维值树
     */
    public void addMemberNodeTreeByAxisType(AxisType axisType, MemberNodeTree nodeTree) {
        if (axisType == null || nodeTree == null) {
            throw new IllegalArgumentException("param is illegal,axisType:" + axisType + " memberNode:" + nodeTree);
        }
        if (axisType.equals(AxisType.ROW)) {
            this.getRowMemberTrees().add(nodeTree);
        } else if (axisType.equals(AxisType.COLUMN)) {
            this.getColumnMemberTrees().add(nodeTree);
        } else {
            throw new IllegalArgumentException("axisType is illegal:" + axisType);
        }
    }

}
