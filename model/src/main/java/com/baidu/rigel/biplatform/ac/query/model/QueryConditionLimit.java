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
package com.baidu.rigel.biplatform.ac.query.model;

import java.io.Serializable;

/**
 * 查询条件限制（查询的维值乘积大于指定额度报警设置）
 * 
 * @author xiaoming.chen
 *
 */
public class QueryConditionLimit implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -5880346585515375933L;

    /**
     * DEFAULT_WARNNING_CONDITION_SIZE 默认条件乘积大于100000报警
     */
    public static final int DEFAULT_WARNNING_CONDITION_SIZE = 50000;

    /**
     * warnningConditionSize 报警条件大小
     */
    private int warnningConditionSize = QueryConditionLimit.DEFAULT_WARNNING_CONDITION_SIZE;

    /**
     * warningAtOverFlow 超过报警大小是否报警，默认抛出 QueryConditionOverFlowException
     */
    private boolean warningAtOverFlow = true;

    /**
     * getter method for property warnningConditionSize
     * 
     * @return the warnningConditionSize
     */
    public int getWarnningConditionSize() {
        return warnningConditionSize;
    }

    /**
     * setter method for property warnningConditionSize
     * 
     * @param warnningConditionSize the warnningConditionSize to set
     */
    public void setWarnningConditionSize(int warnningConditionSize) {
        this.warnningConditionSize = warnningConditionSize;
    }

    /**
     * getter method for property warningAtOverFlow
     * 
     * @return the warningAtOverFlow
     */
    public boolean isWarningAtOverFlow() {
        return warningAtOverFlow;
    }

    /**
     * setter method for property warningAtOverFlow
     * 
     * @param warningAtOverFlow the warningAtOverFlow to set
     */
    public void setWarningAtOverFlow(boolean warningAtOverFlow) {
        this.warningAtOverFlow = warningAtOverFlow;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "QueryConditionLimit [warnningConditionSize=" + warnningConditionSize + ", warningAtOverFlow="
                + warningAtOverFlow + "]";
    }

}
