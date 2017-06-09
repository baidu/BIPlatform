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
package com.baidu.rigel.biplatform.tesseract.isservice.search.collector;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.util.BytesRef;

import com.baidu.rigel.biplatform.tesseract.resultset.isservice.Meta;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.ResultRecord;

/**
 * Collector，用于在检索时按doc收集数据
 * 
 * @author lijin
 *
 */
public class TesseractResultRecordCollector extends Collector {
    
    /**
     * dimFields
     */
    private String[] dimFields;
    /**
     * measureFields
     */
    private String[] measureFields;
    
    private Set<String> groupByFields;
    /**
     * reader
     */
    private AtomicReader reader;
    /**
     * meta
     */
    private Meta meta;
    /**
     * currBinaryDocValuesMap
     */
    private Map<String, BinaryDocValues> currBinaryDocValuesMap;
    /**
     * currDoubleValuesMap
     */
    private Map<String, FieldCache.Doubles> currDoubleValuesMap;
    /**
     * result
     */
    private List<ResultRecord> result;
    
    /**
     * 
     * Constructor by 
     * @param dimFields dimFields
     * @param measureFields measureFields
     */
    public TesseractResultRecordCollector(String[] dimFields, String[] measureFields, Set<String> groupByFields) {
        this.dimFields = dimFields;
        this.measureFields = measureFields;
        this.result = new ArrayList<ResultRecord>();
        
        this.currBinaryDocValuesMap = new HashMap<String, BinaryDocValues>();
        this.currDoubleValuesMap = new HashMap<String, FieldCache.Doubles>();
        
        this.meta = new Meta((String[]) ArrayUtils.addAll(dimFields, measureFields));
        if (groupByFields == null) {
            groupByFields = new HashSet<String>(1);
        }
        this.groupByFields = groupByFields;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.apache.lucene.search.Collector#acceptsDocsOutOfOrder()
     */
    @Override
    public boolean acceptsDocsOutOfOrder() {
        // TODO Auto-generated method stub
        return false;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.apache.lucene.search.Collector#collect(int)
     */
    @Override
    public void collect(int doc) throws IOException {
        //List<Serializable> fieldValueList = new ArrayList<Serializable>();
        Serializable[] fieldValueArray=new Serializable[this.dimFields.length+this.measureFields.length];
        String groupBy = "";
        int i=0;
        for (String dim : dimFields) {
            BinaryDocValues fieldValues = currBinaryDocValuesMap.get(dim);
            BytesRef byteRef = fieldValues.get(doc);
            String dimVal = byteRef.utf8ToString();
            //fieldValueList.add(dimVal);
            fieldValueArray[i++]=dimVal;
            
            if (groupByFields.contains(dim)) {
                groupBy += dimVal + ",";
            }
        }
        
        for (String measure : this.measureFields) {
            FieldCache.Doubles fieldValues = currDoubleValuesMap.get(measure);
           // fieldValueList.add(fieldValues.get(doc));
            fieldValueArray[i++]=fieldValues.get(doc);
        }
        
        ResultRecord record = new ResultRecord(fieldValueArray, this.meta);
        record.setGroupBy(groupBy);
        this.result.add(record);
        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.lucene.search.Collector#setNextReader(org.apache.lucene.index
     * .AtomicReaderContext)
     */
    @Override
    public void setNextReader(AtomicReaderContext context) throws IOException {
        this.reader = context.reader();
        for (String measure : measureFields) {
            currDoubleValuesMap.put(measure,
                    FieldCache.DEFAULT.getDoubles(this.reader, measure, false));
        }
        for (String dim : dimFields) {
            currBinaryDocValuesMap.put(dim, FieldCache.DEFAULT.getTerms(this.reader, dim, false));
        }
        
    }
    
    @Override
    public void setScorer(Scorer arg0) throws IOException {
        // TODO Auto-generated method stub
        
    }
    
    /**
     * getter method for property result
     * 
     * @return the result
     */
    public List<ResultRecord> getResult() {
        return result;
    }

    /** 
     * 获取 meta 
     * @return the meta 
     */
    public Meta getMeta() {
    
        return meta;
    }
    
}
