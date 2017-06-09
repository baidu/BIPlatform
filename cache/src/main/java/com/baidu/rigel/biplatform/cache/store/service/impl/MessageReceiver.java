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
package com.baidu.rigel.biplatform.cache.store.service.impl;

import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;

import com.baidu.rigel.biplatform.cache.StoreManager;
import com.baidu.rigel.biplatform.cache.redis.listener.RedisTopicListener;
import com.baidu.rigel.biplatform.cache.util.ApplicationContextHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 *Description: for test
 * @author david.wang
 *
 */
public class MessageReceiver {
    
    private CountDownLatch latch;
    
    /** 
     * log
     */
    private Logger log = LoggerFactory.getLogger(RedisTopicListener.class);
    
    @Autowired
    public MessageReceiver (CountDownLatch latch) {
        this.latch = latch;
    }
    
    public void receiveMessage (Object msg) throws Exception {
        if(msg != null) {
//            ApplicationContextHelper.getContext ().publishEvent (e);
            String[] content = msg.toString ().split ("~~~");
            ApplicationEvent event = null;
            if (StoreManager.UDF_SETTING.containsKey (StoreManager.DESERIALIZER_KEY)) {
                final Function<Object[], Object> function = StoreManager.UDF_SETTING.get (StoreManager.DESERIALIZER_KEY);
                event = (ApplicationEvent) function.apply (new Object[] {content[1], Class.forName (content[0])});
            } else {
                Gson gson = new GsonBuilder ().create ();
                event = (ApplicationEvent) gson.fromJson (content[1], Class.forName (content[0]));
            }
            if (event != null) {
                ApplicationContextHelper.getContext ().publishEvent (event);
            }
            log.info("receive topic msg:{}, and publish into spring context.", msg);
        }
        latch.countDown ();
    }
    
}
