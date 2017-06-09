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
package com.baidu.rigel.biplatform.tesseract.isservice.index.service;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.tesseract.util.isservice.LogInfoConstants;

/**
 * IndexWriterFactory
 * 
 * @author lijin
 *
 */
public class IndexWriterFactory {
    
    /**
     * LOGGER
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexWriterFactory.class);
    
    /**
     * idxWriterMaps
     */
    private ConcurrentHashMap<String, IndexWriter> idxWriterMaps = new ConcurrentHashMap<String, IndexWriter>();

    
    /**
     * INSTANCE
     */
    private static final IndexWriterFactory INSTANCE = new IndexWriterFactory();
    
    /**
     * 单例，私有构造方法
     */
    private IndexWriterFactory() {
        
    }
    
    /**
     * 
     * getIndexWriter
     * 
     * @param idxPath
     *            索引路径
     * @return IndexWriter
     * @throws IOException
     *             IO异常
     */
    public static synchronized IndexWriter getIndexWriter(String idxPath) throws IOException {
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN, "getIndexWriter",
            "[idxPath:" + idxPath + "]"));
        IndexWriter indexWriter = null;
        if (INSTANCE.idxWriterMaps.containsKey(idxPath)) {
            indexWriter = INSTANCE.idxWriterMaps.get(idxPath);
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
                "getIndexWriter", "return exist IndexWriter "));
        } else {
            File indexFile = new File(idxPath);
            Directory directory = FSDirectory.open(indexFile);
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_4_10_1,
                    new StandardAnalyzer());
            indexWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
            indexWriterConfig.setRAMBufferSizeMB(64.0);
            indexWriterConfig.setMaxBufferedDocs(IndexWriterConfig.DISABLE_AUTO_FLUSH);
            indexWriter = new IndexWriter(directory, indexWriterConfig);
            
            INSTANCE.idxWriterMaps.put(idxPath, indexWriter);
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
                "getIndexWriter", "create new IndexWriter "));
        }
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END, "getIndexWriter",
            "[idxPath:" + idxPath + "]"));
        return indexWriter;
    }
    
    /**
     * destoryWriters
     * 
     * @param idxPath
     *            索引路径
     * @throws IOException
     *             IO异常
     */
    public static void destoryWriters(String idxPath) throws IOException {
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN, "destoryWriters",
            "[idxPath:" + idxPath + "]"));
        IndexWriter indexWriter = null;
        if (INSTANCE.idxWriterMaps.containsKey(idxPath)) {
            indexWriter = INSTANCE.idxWriterMaps.get(idxPath);
            
            try {
                indexWriter.commit();
                indexWriter.close();
            } catch (IOException e) {
                if (IndexWriter.isLocked(indexWriter.getDirectory())) {
                    IndexWriter.unlock(indexWriter.getDirectory());
                }
            }
            INSTANCE.idxWriterMaps.remove(idxPath);
        }
        
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END, "destoryWriters",
            "[idxPath:" + idxPath + "]"));
    }
    
   
    
}
