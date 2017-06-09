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
 * 元素的视图类
 * 
 * @author zhongyi
 *
 */
public class ElementMeta implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 3564866541855034000L;
    
    private String[] selectedMetaNames;
    
    private String[] validMetaNames;

    /**
     * @return the selectedMetaNames
     */
    public String[] getSelectedMetaNames() {
        return selectedMetaNames;
    }

    /**
     * @param selectedMetaNames the selectedMetaNames to set
     */
    public void setSelectedMetaNames(String[] selectedMetaNames) {
        this.selectedMetaNames = selectedMetaNames;
    }

    /**
     * @return the validMetaNames
     */
    public String[] getValidMetaNames() {
        return validMetaNames;
    }

    /**
     * @param validMetaNames the validMetaNames to set
     */
    public void setValidMetaNames(String[] validMetaNames) {
        this.validMetaNames = validMetaNames;
    }
    
}
