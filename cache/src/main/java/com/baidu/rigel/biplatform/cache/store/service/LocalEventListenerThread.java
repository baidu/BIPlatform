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
package com.baidu.rigel.biplatform.cache.store.service;

import java.util.EventObject;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;

import com.baidu.rigel.biplatform.cache.StoreManager;

/**
 * LocalEventListenerThread 本地监听事件线程
 * 
 * @author lijin
 *
 */
public class LocalEventListenerThread implements ApplicationContextAware {
    /**
     * LOGGER
     */
    private static final Logger LOGGER = LoggerFactory
                                                    .getLogger(LocalEventListenerThread.class);

    /**
     * context
     */
    private ApplicationContext context;

    /**
     * storeManager
     */
    @Resource
    private StoreManager storeManager;

    /**
     * 从集群中拿出事件发布为本地事件
     */
    public void getClusterEventAndPublish() {

        try {
            EventObject item = this.storeManager.getNextEvent();
            
            context.publishEvent((ApplicationEvent) item);
            LOGGER.info("publish topic event : {} success", item);

        } catch (Exception e) {
            LOGGER.error("get event from topic catch error:{}", e);

        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.context.ApplicationContextAware#setApplicationContext
     * (org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.context = applicationContext;

    }

    /**
     * @param storeManager the storeManager to set
     */
    public void setStoreManager(StoreManager storeManager) {
        this.storeManager = storeManager;
    }

    
}
