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
 * query dim data
 * 
 * @author xiaoming.chen
 *
 */
public class QueryData implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -6091997384380423762L;
    
    /**
     * uniqueName 维值的UniqueName
     */
    private String uniqueName;
    
    /**
     * show 本身节点展现方式
     */
    private boolean show = true;
    
    /**
     * expand 是否显示当前节点子节点
     */
    private boolean expand = false;
    
    /**
     * construct with UniqueName
     * 
     * @param uniqueName 维值的UniqueName
     */
    public QueryData(String uniqueName) {
        this.uniqueName = uniqueName;
    }
    
    /**
     * getter method for property uniqueName
     * 
     * @return the uniqueName
     */
    public String getUniqueName() {
        return uniqueName;
    }
    
    /**
     * setter method for property uniqueName
     * 
     * @param uniqueName the uniqueName to set
     */
    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }
    
    /**
     * getter method for property show
     * 
     * @return the show
     */
    public boolean isShow() {
        return show;
    }
    
    /**
     * setter method for property show
     * 
     * @param show the show to set
     */
    public void setShow(boolean show) {
        this.show = show;
    }
    
    /**
     * getter method for property expand
     * 
     * @return the expand
     */
    public boolean isExpand() {
        return expand;
    }
    
    /**
     * setter method for property expand
     * 
     * @param expand the expand to set
     */
    public void setExpand(boolean expand) {
        this.expand = expand;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "QueryData [uniqueName=" + uniqueName + ", show=" + show + ", expand=" + expand
            + "]";
    }
    
}
