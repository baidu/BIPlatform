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

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.tesseract.isservice.index.service.IndexMetaService;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexMeta;
import com.baidu.rigel.biplatform.tesseract.node.meta.Node;
import com.baidu.rigel.biplatform.tesseract.node.meta.NodeState;
import com.baidu.rigel.biplatform.tesseract.node.service.IndexAndSearchServer;
import com.baidu.rigel.biplatform.tesseract.node.service.IsNodeService;
import com.baidu.rigel.biplatform.tesseract.util.isservice.LogInfoConstants;

/**
 * IndexAndSearchStartupListener
 * 
 * @author lijin
 *
 */
@Service
public class IndexAndSearchStartupListener implements ApplicationContextAware,
        ApplicationListener<ContextRefreshedEvent> {
    
    /**
     * LOGGER
     */
    private static final Logger LOGGER = LoggerFactory
        .getLogger(IndexAndSearchStartupListener.class);
    /**
     * context
     */
    private ApplicationContext context;
    
    /**
     * isNodeService
     */
    @Resource(name = "isNodeService")
    private IsNodeService isNodeService;
    
    @Resource
    private IndexMetaService idxMetaService;
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.context.ApplicationListener#onApplicationEvent(org
     * .springframework.context.ApplicationEvent)
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_ON_LISTENER_BEGIN,
            "IndexAndSearchStartupListener.onApplicationEvent", event));
        if (event == null) {
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
                "IndexAndSearchStartupListener.onApplicationEvent", event));
            throw new IllegalArgumentException();
        }
        // 程序初始化结束后
        // 启动索引查询服务
        IndexAndSearchServer isServer = this.context.getBean(IndexAndSearchServer.class);        
        
        isServer.start();
        int count = 0;
        try {
            while (!isServer.isRunning()) {
                Thread.sleep(500);
                count++;
                if (count > 10) {
                    throw new Exception();
                }
            }
        } catch (Exception e) {
            LOGGER.error(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_ERROR,
                "IndexAndSearchStartupListener.onApplicationEvent-Server startup failed "), e);
            
        }
        
        if (isServer.isRunning()) {
            // 注册信息
            if (isServer.getNode() != null) {
                isServer.getNode().setNodeState(NodeState.NODE_AVAILABLE);
                isServer.getNode().setLastStateUpdateTime(System.currentTimeMillis());
                this.isNodeService.saveOrUpdateNodeInfo(isServer.getNode());
            }
            
            // 恢复本地镜像
            Node currNode = isServer.getNode();
            if (currNode != null) {
            	 // 恢复本地元数据
                loadLocalInfo(currNode);
            }
            
            // 启动状态更新&检查线程
            ClusterNodeCheckThread clusterNodeCheckThread = this.context
                .getBean(ClusterNodeCheckThread.class);
            clusterNodeCheckThread.start();
            
        } else {
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_ERROR,
                "IndexAndSearchStartupListener.onApplicationEvent-Server is not running ", event));
        }
        
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_ON_LISTENER_END,
            "IndexAndSearchStartupListener.onApplicationEvent", event));
        return;
        
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
        this.context = applicationContext;
        
    }
    
    private void loadLocalInfo(Node node) {
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN, "loadLocalInfo",
            "[Node:" + node + "]"));
        List<IndexMeta> idxMetaList=this.idxMetaService.loadIndexMetasLocalImage(node.getIndexBaseDir(), node.getNodeKey(), node.getClusterName());
        this.idxMetaService.recoverLocalIndexMetaWithCluster(idxMetaList, node.getClusterName());
       
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END, "loadLocalInfo", "[Node:" + node + "]"));
    }
    
   
    
}
