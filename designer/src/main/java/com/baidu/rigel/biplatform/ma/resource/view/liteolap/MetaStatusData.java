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
 * MetaStatusData视图
 * 
 * @author zhongyi
 *
 */
public class MetaStatusData implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 8453275134157348891L;

    private String[] cubeNames;
    
    private ElementMeta dimMetas;
    
    private ElementMeta indMetas;
    
    private int selectCubeNum = 1;

    /**
     * @return the cubeNames
     */
    public String[] getCubeNames() {
        return cubeNames;
    }

    /**
     * @param cubeNames the cubeNames to set
     */
    public void setCubeNames(String[] cubeNames) {
        this.cubeNames = cubeNames;
    }

    /**
     * @return the dimMetas
     */
    public ElementMeta getDimMetas() {
        return dimMetas;
    }

    /**
     * @param dimMetas the dimMetas to set
     */
    public void setDimMetas(ElementMeta dimMetas) {
        this.dimMetas = dimMetas;
    }

    /**
     * @return the indMetas
     */
    public ElementMeta getIndMetas() {
        return indMetas;
    }

    /**
     * @param indMetas the indMetas to set
     */
    public void setIndMetas(ElementMeta indMetas) {
        this.indMetas = indMetas;
    }

    /**
     * @return the selectCubeNum
     */
    public int getSelectCubeNum() {
        return selectCubeNum;
    }

    /**
     * @param selectCubeNum the selectCubeNum to set
     */
    public void setSelectCubeNum(int selectCubeNum) {
        this.selectCubeNum = selectCubeNum;
    }
}
