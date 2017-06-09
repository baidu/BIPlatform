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
package com.baidu.rigel.biplatform.tesseract.isservice.search.service;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.tesseract.util.isservice.LogInfoConstants;

/**
 * 
 * IndexSearcher工厂
 * 
 * @author lijin
 *
 */
@Service("indexSearcherFactory")
public class IndexSearcherFactory {
    /**
     * LOGGER
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexSearcherFactory.class);
    
    /**
     * 用于缓存已经打开的索引对应的SearcherManager
     */
    private ConcurrentHashMap<String, SearcherManager> searcherManagerMaps = new ConcurrentHashMap<String, 
            SearcherManager>();
    
    /**
     * 工厂唯一实例
     */
    private static IndexSearcherFactory INSTANCE = new IndexSearcherFactory();
    
    /**
     * 线程池
     */
    private static ExecutorService EXECUTOR_POOL =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 20);
    
    /**
     * getInstance
     * 
     * @return IndexSearcherFactory
     */
    public static IndexSearcherFactory getInstance() {
        return INSTANCE;
    }
    
    /**
     * 
     * TesseractSearcherFactory 重写newSearcher方法
     * 
     * @author lijin
     *
     */
    class TesseractSearcherFactory extends SearcherFactory {
        /*
         * (non-Javadoc)
         * 
         * @see
         * org.apache.lucene.search.SearcherFactory#newSearcher(org.apache.lucene
         * .index.IndexReader)
         */
        @Override
        public IndexSearcher newSearcher(IndexReader reader) throws IOException {
        	
            return new IndexSearcher(reader, EXECUTOR_POOL);
        }
        
    }
    
    /**
     * 
     * getSearcherManager
     * 
     * @param idxPath
     *            索纼路径
     * @return SearcherManager
     * @throws IOException
     *             可能抛出异常
     */
    public synchronized SearcherManager getSearcherManager(String idxPath,boolean isLog) throws IOException {
		if (isLog) {
			LOGGER.info(String.format(
					LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN,
					"getSearcherManager", idxPath));
		}
        SearcherManager searcherManager = null;
        if (StringUtils.isEmpty(idxPath)) {
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
                "getSearcherManager", idxPath));
            throw new IllegalArgumentException();
        }
        if (this.searcherManagerMaps.containsKey(idxPath)) {
            searcherManager = this.searcherManagerMaps.get(idxPath);
        } else {
            File indexFile = new File(idxPath);
            if (indexFile.exists()) {
                Directory directory = FSDirectory.open(indexFile);
                LOGGER.info("CURRENT DIRECTORY TYPE :"+directory.getClass());
                
                searcherManager = new SearcherManager(directory,
                        this.new TesseractSearcherFactory());
            } else {
                LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
                    "getSearcherManager", "[idxPath:" + idxPath + " not exist]"));
            }
            
        }
		if (isLog) {
			LOGGER.info(String.format(
					LogInfoConstants.INFO_PATTERN_FUNCTION_END,
					"getSearcherManager", idxPath));
		}
        return searcherManager;
    }
    
    /**
     * 刷新指定路径的searchManager
     * 
     * @param idxPath
     *            idxPath
     * @throws IOException
     *             可能抛出异常
     */
    public void refreshSearchManager(String idxPath) throws IOException {
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN,
            "refreshSearchManager", idxPath));
        if (StringUtils.isEmpty(idxPath)) {
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
                "refreshSearchManager", idxPath));
            throw new IllegalArgumentException();
        }
        SearcherManager searcherManager = getSearcherManager(idxPath,true);
        if (searcherManager != null) {
            searcherManager.maybeRefresh();
        }
        
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END,
            "refreshSearchManager", idxPath));
    }
    
    /**
     * 
     * 关闭指定路径的searchManager
     * 
     * @param idxPath
     *            索引路径
     * @throws IOException
     *             可能抛出异常
     */
    public void releaseSearchManager(String idxPath) throws IOException {
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN,
            "releaseSearchManager", idxPath));
        if (StringUtils.isEmpty(idxPath)) {
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
                "releaseSearchManager", idxPath));
            throw new IllegalArgumentException();
        }
        SearcherManager searcherManager = null;
        if (this.searcherManagerMaps.containsKey(idxPath)) {
            searcherManager = this.searcherManagerMaps.get(idxPath);
            searcherManager.close();
            this.searcherManagerMaps.remove(idxPath);
        }
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END,
            "releaseSearchManager", idxPath));
    }
    
    /**
     * getter method for property searcherManagerMaps
     * 
     * @return the searcherManagerMaps
     */
    public ConcurrentHashMap<String, SearcherManager> getSearcherManagerMaps() {
        return searcherManagerMaps;
    }
    
}
