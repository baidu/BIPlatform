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
package com.baidu.rigel.biplatform.ma.auth.service;

import com.baidu.rigel.biplatform.ma.auth.bo.ProductlineInfo;

/**
 * 产品线注册服务接口
 * @author jiangyichao
 *
 */
public interface ProductLineRegisterService {
    /**
     * 发送用户注册信息到管理员
     * @param user 用户对象
     * @param hostAddress 开通服务的服务器处理地址
     * @param magicStr 
     * @return 成功返回0，失败返回-1
     */
    public int sendRegisterMsgToAdministrator(ProductlineInfo user, String hostAddress, String magicStr);
    
    /**
     * 发送开通服务信息到用户
     * @param user
     * @param serviceType 服务类型，1代表线上服务，0代表线下服务
     * @return 0代表发送成功,-1代表发送失败
     */
    public int sendOpenServiceMsgToUser(ProductlineInfo user, int serviceType);
    /**
     * 开通某产品线的线上服务
     * @param user 用户对象
     * @return 成功返回0，失败返回-1
     */
    public int openOnlineService(ProductlineInfo user);
    
    /**
     * 开通某产品线的线下服务
     * @param user 用户对象
     * @return 成功返回0，失败返回-1
     */
    public int openOfflineService(ProductlineInfo user);
}
