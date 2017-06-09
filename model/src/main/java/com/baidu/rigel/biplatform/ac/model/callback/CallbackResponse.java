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
package com.baidu.rigel.biplatform.ac.model.callback;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * Description: CallbackResponse
 * @author david.wang
 *
 */
public class CallbackResponse implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -737085085913204438L;
    
    
    /**
     * 响应状态
     * @see ResponseStatus
     */
    private ResponseStatus status;
    
    /**
     * 
     */
    private String message;
    
    /**
     * 请求返回数据
     */
    private List<CallbackValue> data;
    
    /**
     * 服务花费时间
     */
    private int cost;
    
    /**
     * 提供服务节点名称
     */
    private String provider;
    
    /**
     * 当前服务版本
     */
    private String version;

    /**
     * @return the status
     */
    public ResponseStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the data
     */
    public List<? extends CallbackValue> getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(List<CallbackValue> data) {
        this.data = data;
    }

    /**
     * @return the cost
     */
    public int getCost() {
        return cost;
    }

    /**
     * @param cost the cost to set
     */
    public void setCost(int cost) {
        this.cost = cost;
    }

    /**
     * @return the provider
     */
    public String getProvider() {
        return provider;
    }

    /**
     * @param provider the provider to set
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("{ status : " + status.name());
        str.append(", message : " + message);
        str.append(", data : [");
        if (data != null) {
            for (int i = 0; i < (data.size () < 100 ? data.size () : 100); ++i) {
                str.append(data.get (i) + ",");
            }
        }
        str.append("]");
        str.append(", cost : " + cost + "ms,");
        str.append(", provider : " + provider);
        str.append(", version : " + version);
        return str.toString();
    }

}
