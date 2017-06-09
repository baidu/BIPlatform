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
package com.baidu.rigel.biplatform.ma.resource.view.dimdetail;

/**
 * 刷新类型定义
 * @author jiangyichao
 *     at 2014-09-17
 */
public enum RefreshType {
    /**
     * 不刷新
     */
    NO_REFRESH(1),
    /**
     * 同cube一同刷新
     */
    REFRESH_WITH_CUBE(2),
    /**
     * 按照指定时间间隔刷新
     */
    REFRESH_WITH_INTERVAL(3);;
    /**
     * 刷新类型变量
     */
    private int refreshType;
  
    /**
     * 构造函数
     * @param refreshType
     */
    private RefreshType(int refreshType) {
        this.setRefreshType(refreshType);
    }
    /**
     * 设置刷新类型
     * @param refreshType
     */
    public void setRefreshType(int refreshType) {
        this.refreshType = refreshType;
    }
    /**
     * 获取刷新类型
     * @return
     */
    public int getRefreshType() {
        return this.refreshType;
    }
    /**
     * toString方法
     */
    @Override
    public String toString() {
        return this.name();
    }
}
