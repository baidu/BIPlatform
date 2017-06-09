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
package com.baidu.rigel.biplatform.ma.auth.bo;

import java.io.Serializable;

/**
 * 产品线定义
 * 
 * @author david.wang
 *
 */
public class ProductlineInfo implements Serializable {
    
    /**
     * 序列号
     */
    private static final long serialVersionUID = -5935579215188950609L;
    
    /**
     * 用户名
     */
    private String name;
    
    /**
     * 密码
     */
    private String pwd;
   
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 部门
     */
    private String department;
    
    /**
     * 服务类型,1代表线上服务,0代表线下服务
     */
    private int serviceType;
    
    /**
     * 描述
     */
    private String desc;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPwd() {
        return pwd;
    }
    
    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public void setDesc(String desc) {
        this.desc = desc;
    }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getServiceType() {
        return serviceType;
    }

    public void setServiceType(int serviceType) {
        this.serviceType = serviceType;
    }
    
}
