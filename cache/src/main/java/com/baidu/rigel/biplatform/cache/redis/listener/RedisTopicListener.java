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

import org.redisson.core.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;

import com.baidu.rigel.biplatform.cache.util.ApplicationContextHelper;

/** 
 *  
 * @author xiaoming.chen
 * @version  2015年2月27日 
 * @since jdk 1.8 or after
 */
public class RedisTopicListener implements MessageListener<Object>{

    /** 
     * log
     */
    private Logger log = LoggerFactory.getLogger(RedisTopicListener.class);
    
    @Override
    public void onMessage(Object msg) {
        if(msg != null) {
            ApplicationContextHelper.getContext().publishEvent((ApplicationEvent)msg);
            log.info("receive topic msg:{}, and publish into spring context.", msg);
        }
    }
    
}