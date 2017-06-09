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
package com.baidu.rigel.biplatform.ma.resource.view.liteolap;

import java.io.Serializable;

/**
 * 图形指标的视图
 * 
 * @author zhongyi
 *
 */
public class IndCandicateForChart implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 328316375452895509L;
    
    private String caption = "未命名";
    
    private String custIndName = "nan";

    /**
     * @return the caption
     */
    public String getCaption() {
        return caption;
    }

    /**
     * @param caption the caption to set
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     * @return the custIndName
     */
    public String getCustIndName() {
        return custIndName;
    }

    /**
     * @param custIndName the custIndName to set
     */
    public void setCustIndName(String custIndName) {
        this.custIndName = custIndName;
    }
    
    
}
