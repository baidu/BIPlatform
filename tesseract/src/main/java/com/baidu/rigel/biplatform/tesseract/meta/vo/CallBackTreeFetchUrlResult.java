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
package com.baidu.rigel.biplatform.tesseract.meta.vo;

import java.util.List;

import com.baidu.rigel.biplatform.tesseract.model.CallBackTreeNode;

/**
 * 新的岗位树结果
 * 
 * @author xiaoming.chen
 *
 */
public class CallBackTreeFetchUrlResult extends FetchUrlResult {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -1380265582913836671L;
    /**
     * 岗位数据
     */
    private List<CallBackTreeNode> data;

    /**
     * get the data
     * 
     * @return the data
     */
    public List<CallBackTreeNode> getData() {
        return data;
    }

    /**
     * set the data
     * 
     * @param data the data to set
     */
    public void setData(List<CallBackTreeNode> data) {
        this.data = data;
    }
}