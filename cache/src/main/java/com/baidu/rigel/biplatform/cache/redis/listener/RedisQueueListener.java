
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
package com.baidu.rigel.biplatform.cache.redis.listener;

import java.util.EventObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.baidu.rigel.biplatform.cache.StoreManager;

/** 
 *  需要自己启动bean
 * @author xiaoming.chen
 * @version  2015年2月27日 
 * @since jdk 1.8 or after
 */
public class RedisQueueListener implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware {
    
    
    /** 
     * log
     */
    private Logger log = LoggerFactory.getLogger(this.getClass());
    

    @Autowired(required=false)
    private StoreManager storeManager;
    
    private ApplicationContext applicationContext;
    
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        new Thread(() -> {
            while(true) {
                try {
                    Thread.sleep(1000);
                    EventObject item = storeManager.getNextEvent();
                    if(item != null) {
                        applicationContext.publishEvent((ApplicationEvent) item);
                        log.info("publish queue event : {} success", item);
                    }
                } catch (Exception e) {
                    log.warn("get event error:{}", e);
                }
            }
        }).start();
        log.info("start redis queue thread success.");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}

