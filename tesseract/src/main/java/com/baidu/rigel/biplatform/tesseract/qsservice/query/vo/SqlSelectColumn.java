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
package com.baidu.rigel.biplatform.tesseract.qsservice.query.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 查询的属性
 * 
 * @author luowenlei
 *
 */
public class SqlSelectColumn implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 3513983220906833848L;

    /**
     * select sql中select的字段
     */
    private String select;
    
    /**
     * sqlSelectColumnType
     */
    private SqlSelectColumnType sqlSelectColumnType;

    /**
     * operator sql中select的字段的agg或其他算子
     */
    private String operator;

	/**
	 * getSqlSelectColumnType
	 * 
	 * @return the sqlSelectColumnType
	 */
	public SqlSelectColumnType getSqlSelectColumnType() {
		if (sqlSelectColumnType == null) {
			this.sqlSelectColumnType = SqlSelectColumnType.COMMON;
		}
		return sqlSelectColumnType;
	}

	/**
	 * setSqlSelectColumnType
	 * 
	 * @param sqlSelectColumnType the sqlSelectColumnType to set
	 */
	public void setSqlSelectColumnType(SqlSelectColumnType sqlSelectColumnType) {
		this.sqlSelectColumnType = sqlSelectColumnType;
	}

	/**
	 * @return the select
	 */
	public String getSelect() {
		return select;
	}

	/**
	 * @param select the select to set
	 */
	public void setSelect(String select) {
		this.select = select;
	}

	/**
	 * @return the operator
	 */
	public String getOperator() {
		return operator;
	}

	/**
	 * @param operator the operator to set
	 */
	public void setOperator(String operator) {
		this.operator = operator;
	}

	/**
	 * SqlSelectColumn
	 * 
	 * @param select
	 */
	public SqlSelectColumn(String select) {
		this.select = select;
	}
}