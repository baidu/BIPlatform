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
package com.baidu.rigel.biplatform.ac.query.model;

import java.io.Serializable;

import com.baidu.rigel.biplatform.ac.model.Cube;

/**
 * 问题模型(配置端专用，临时传递一个未发布的cube和数据源)
 * 
 * @author xiaoming.chen
 *
 */
public class ConfigQuestionModel extends QuestionModel implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 7462160317025332897L;

    /**
     * 查询的cube （如果有cubeID优先使用cubeId去查）
     */
    private Cube cube;


    /**
     * get cube
     * 
     * @return the cube
     */
    public Cube getCube() {
        return cube;
    }

    /**
     * set cube with cube
     * 
     * @param cube the cube to set
     */
    public void setCube(Cube cube) {
        this.cube = cube;
    }


    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ConfigQuestionModel [cube=" + cube + ", dataSourceInfo=" + this.getDataSourceInfo() + "]";
    }

}