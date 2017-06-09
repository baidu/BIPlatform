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
package com.baidu.rigel.biplatform.tesseract.isservice.startup;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.cache.StoreManager;
import com.baidu.rigel.biplatform.cache.util.ApplicationContextHelper;
import com.baidu.rigel.biplatform.tesseract.isservice.index.service.IndexMetaService;
import com.baidu.rigel.biplatform.tesseract.node.meta.Node;
import com.baidu.rigel.biplatform.tesseract.node.meta.NodeState;
import com.baidu.rigel.biplatform.tesseract.node.service.IsNodeService;
import com.baidu.rigel.biplatform.tesseract.util.isservice.LogInfoConstants;

/**
 * ClusterNodeCheckThread
 * 
 * @author lijin
 *
 */
@Service("clusterNodeCheckThread")
public class ClusterNodeCheckThread implements Runnable, ApplicationContextAware {
    /**
     * LOGGER
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterNodeCheckThread.class);
    
    /**
     * applicationContext
     */
    private ApplicationContext applicationContext;
    
    @Resource
    private IsNodeService isNodeService;
    @Resource
    private IndexMetaService idxMetaService;
    @Resource
    private StoreManager storeManger;
    
    @Value("${hazelLock.getLockTimeOut}")
    private int getLockTimeOut;
    
    @Value("${hazellock.lockTimeOut}")
    private int lockTimeOut;
    
    @Value("${cluster.checkInterval}")
    private int checkInterval;
    
    
    /**
     * Constructor by no param
     */
    public ClusterNodeCheckThread() {
        super();
    }
    
    public void start() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> run());
        Executors.newScheduledThreadPool(1)
            .scheduleAtFixedRate(this, checkInterval, 20000, TimeUnit.MILLISECONDS);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_THREAD_RUN_START,
            "ClusterNodeCheckThread"));
        
//        while (true) {
            // get lock
        try {
            Node node = this.applicationContext.getBean(Node.class);
            Node updateNode = this.isNodeService.getNodeByCurrNode(node);
			if (updateNode == null) {
				updateNode = node;
			}
            // update self state
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_THREAD_RUN_ACTION,
                "update self state", "begin"));
            updateNode.setNodeState(NodeState.NODE_AVAILABLE);
            updateNode.setLastStateUpdateTime(System.currentTimeMillis());
            this.isNodeService.saveOrUpdateNodeInfo(updateNode);
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_THREAD_RUN_ACTION,
                "update self state", "end"));
            
            // check others
            if(ApplicationContextHelper.getContext()==null){
                return ; 
            }
            Lock lock = this.storeManger.getClusterLock();
            
            if (lock.tryLock(this.getLockTimeOut, TimeUnit.SECONDS)) {
                LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_THREAD_RUN_ACTION,
                    "Get Lock", "Success"));
                try {
//                    lock.lock();
//                    LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_THREAD_RUN_ACTION,
//                            "Locked", "Success"));
                    this.isNodeService.markClusterBadNode();
                    LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_THREAD_RUN_ACTION,
                        "markClusterBadNode", "Success"));
                } finally {
                    lock.unlock();
                }
            }
            
            
            
        } catch (Exception e) {
            LOGGER.error(String.format(LogInfoConstants.INFO_PATTERN_THREAD_RUN_EXCEPTION,
                "ClusterNodeCheckThread"), e);
        }
//        }
        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.context.ApplicationContextAware#setApplicationContext
     * (org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        
    }
    
}
