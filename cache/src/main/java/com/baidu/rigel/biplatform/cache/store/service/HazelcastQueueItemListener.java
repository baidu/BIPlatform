/**
 * 
 */
package com.baidu.rigel.biplatform.cache.store.service;

import java.util.EventObject;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.ItemEvent;
import com.hazelcast.core.ItemListener;

/**
 * HazelcastQueueItemListener用于监听集群中事件添加动作，当集群中添加了新的事件，本listener会把事件取出转成本地spring事件
 * @author lijin
 *
 */
public class HazelcastQueueItemListener implements ItemListener<EventObject> {

    /**
     * LOGGER
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastQueueItemListener.class);
    
    
    @Resource
    private LocalEventListenerThread localEventListenerThread;
    
    /* (non-Javadoc)
     * @see com.hazelcast.core.ItemListener#itemAdded(com.hazelcast.core.ItemEvent)
     */
    @Override
    public void itemAdded(ItemEvent<EventObject> item) {
        LOGGER.info("Event add Triggr");
        this.localEventListenerThread.getClusterEventAndPublish();
        
    }

    /* (non-Javadoc)
     * @see com.hazelcast.core.ItemListener#itemRemoved(com.hazelcast.core.ItemEvent)
     */
    @Override
    public void itemRemoved(ItemEvent<EventObject> item) {
        // TODO Auto-generated method stub
        
    }

    
    
}
