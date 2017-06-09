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

import java.io.Serializable;

/**
 * 获得结果的状态
 * 
 * @author xiaoming.chen
 *
 */
public class FetchUrlResult implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 4160417591138155621L;
    /**
     * 状态
     */
    private String status;
    /**
     * 版本
     */
    private String version;

    /**
     * get the status
     * 
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * set the status
     * 
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * get the version
     * 
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * set the version
     * 
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }
}
