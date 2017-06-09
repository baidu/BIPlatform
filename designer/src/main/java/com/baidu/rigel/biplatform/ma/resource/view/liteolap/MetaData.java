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
 * MetaData视图
 * 
 * @author zhongyi
 *
 */
public class MetaData implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 4938386724971232543L;
    
    private String[] cubeNames;
    
    private LiteOlapDim[] dims;
    
    private LiteOlapInd[] inds;
    
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
     * @return the dims
     */
    public LiteOlapDim[] getDims() {
        return dims;
    }

    /**
     * @param dims the dims to set
     */
    public void setDims(LiteOlapDim[] dims) {
        this.dims = dims;
    }

    /**
     * @return the inds
     */
    public LiteOlapInd[] getInds() {
        return inds;
    }

    /**
     * @param inds the inds to set
     */
    public void setInds(LiteOlapInd[] inds) {
        this.inds = inds;
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
