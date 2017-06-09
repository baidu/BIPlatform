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
package com.baidu.rigel.biplatform.tesseract.isservice.search.collector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;

/**
 * TesseractDocCollector
 * 
 * @author lijin
 *
 */
public class TesseractDocCollector extends Collector {
    
    /**
     * resultDocIdList
     */
    private List<Integer> resultDocIdList;
    
    /**
     * resultDocBaseDocIdMap
     */
    private Map<Integer, List<Integer>> resultDocBaseDocIdMap;
    
    /**
     * docBase
     */
    private int docBase;
    
    
    /**
     *    
     * Constructor by no param
     */
    public TesseractDocCollector() {        
        this.resultDocIdList = new ArrayList<Integer>();
        this.resultDocBaseDocIdMap = new HashMap<Integer, List<Integer>>();
        
    }    
    
    /*
     * (non-Javadoc)
     * @see org.apache.lucene.search.Collector#setScorer(org.apache.lucene.search.Scorer)
     */
    @Override
    public void setScorer(Scorer scorer) throws IOException {
    }
    
    /*
     * (non-Javadoc)
     * @see org.apache.lucene.search.Collector#setNextReader(org.apache.lucene.index.AtomicReaderContext)
     */
    @Override
    public void setNextReader(AtomicReaderContext context) throws IOException {
        this.docBase = context.docBase;
    
        
    }
    
    /*
     * (non-Javadoc)
     * @see org.apache.lucene.search.Collector#collect(int)
     */
    @Override
    public void collect(int doc) throws IOException {
        int docId = this.docBase + doc;
        this.resultDocIdList.add(docId);
        List<Integer> idList = this.resultDocBaseDocIdMap.get(this.docBase);
        if (idList == null) {
            idList = new ArrayList<Integer>();
        }
        idList.add(doc);
        this.resultDocBaseDocIdMap.put(this.docBase, idList);
    }
    
    
    /*
     * (non-Javadoc)
     * @see org.apache.lucene.search.Collector#acceptsDocsOutOfOrder()
     */
    @Override
    public boolean acceptsDocsOutOfOrder() {
        return true;
    }


    /**
     * getter method for property resultDocIdList
     * @return the resultDocIdList
     */
    public List<Integer> getResultDocIdList() {
        return resultDocIdList;
    }

    /**
     * getter method for property resultDocBaseDocIdMap
     * @return the resultDocBaseDocIdMap
     */
    public Map<Integer, List<Integer>> getResultDocBaseDocIdMap() {
        return resultDocBaseDocIdMap;
    }

    
    
    
    
}
