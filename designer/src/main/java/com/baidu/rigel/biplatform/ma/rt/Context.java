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
package com.baidu.rigel.biplatform.ma.rt;

import java.io.Serializable;


/**
 * 运行时上下文：用来存储运行时全局变量信息，比如全局过滤条件，全局ACL参数等
 * 注意：运行时上下文只对当前浏览报表有关，不同报表有不同上下文,不同报表之间上下文完全隔离
 * TODO 待考虑
 * @author david.wang
 * @version 1.0.0.1
 */
@Deprecated
public final class Context implements Serializable {

//    /**
//     * Context.java -- long
//     * description:
//     */
//    private static final long serialVersionUID = -8040634498675056473L;
//    
//    /**
//     * spring application，每个运行时包含一个全局的ApplicationContext
//     */
//    private ApplicationContext appContext;
//    
//    /**
//     * global params
//     */
//    private ConcurrentHashMap<String, Object> globalParams = 
//        new ConcurrentHashMap<String, Object>();
//    
////    /**
////     * 
////     */
////    private ConcurrentHashMap<String, ExtendAreaContext> localCtxMap = 
////                new ConcurrentHashMap<String, ExtendAreaContext>();
//    
//    /**
//     * 构造函数
//     * @param context ApplicationContext
//     */
//    public Context(ApplicationContext context) {
//        this.appContext = context;
//    }
//    
//    /**
//     *  获取当前运行时上下文环境对应的spring 运行时
//     * @return ApplicationContext spring运行时
//     */
//    public ApplicationContext getApplicationContext() {
//        return appContext;
//    }
//
//    /**
//     * @return the globalParams
//     */
//    public ConcurrentHashMap<String, Object> getGlobalParams() {
//        return globalParams;
//    }
//
//    /**
//     * @param globalParams the globalParams to set
//     */
//    public void setGlobalParams(ConcurrentHashMap<String, Object> globalParams) {
//        this.globalParams = globalParams;
//    }    
}
