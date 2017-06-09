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
package com.baidu.rigel.biplatform.tesseract.isservice.search.agg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.ac.model.Aggregator;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryMeasure;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryRequest;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.SearchIndexResultRecord;
import com.google.common.collect.Sets;

/**
 * 
 * 聚集计算
 * 
 * @author lijin   
 *
 */
public class AggregateCompute {
    
    /**
     * LOGGER
     */
    private static Logger LOGGER = LoggerFactory.getLogger(AggregateCompute.class);
    
    /**
     * 
     * @param dataList
     * @return List<SearchIndexResultRecord>
     */
    public static List<SearchIndexResultRecord> distinct(List<SearchIndexResultRecord> dataList){
        return dataList.stream().distinct().collect(Collectors.toList());
    }
    
    /**
     * 聚集计算
     * 
     * @param dataList
     *            待计算的数据
     * @param query
     *            原始查询请求
     * @return LinkedList<ResultRecord> 计算后的数据
     */
    public static List<SearchIndexResultRecord> aggregate(List<SearchIndexResultRecord> dataList,
        int dimSize, List<QueryMeasure> queryMeasures) {
        
        if (CollectionUtils.isEmpty(queryMeasures) || CollectionUtils.isEmpty(dataList)) { 
            LOGGER.info("no need to group.");
            return dataList;
        }
        
        Set<Integer> countIndex = Sets.newHashSet();
        for (int i = 0 ; i < queryMeasures.size() ; i++) {
            if (queryMeasures.get(i).getAggregator().equals(Aggregator.DISTINCT_COUNT)) {
                if (LOGGER.isDebugEnabled ()) {
                    LOGGER.info ( queryMeasures.get(i) + " ============= begin print values ===== ===="); 
                    final int tmp = i;
                    dataList.forEach (rs -> LOGGER.info (rs.getField (tmp) + ""));
                    LOGGER.info ( " ============= end print measure values =============="); 
                }
                countIndex.add(i);
            }
        }
        
        int arraySize = dataList.get(0).getFieldArraySize();
        
        long current = System.currentTimeMillis();
        Stream<SearchIndexResultRecord> stream =
            dataList.size() > 300000 ? dataList.parallelStream() : dataList.stream();
        
        int defaultSize = (int) (dataList.size() > 100 ? dataList.size() * 0.01 : dataList.size());
        
        final BinaryOperator<SearchIndexResultRecord> reduceOperation = (x,y) ->{
            if(!y.getGroupBy().equals(x.getGroupBy())) {
                x = SearchIndexResultRecord.of(arraySize);
                x.setGroupBy(y.getGroupBy());
                for(int i = 0; i < dimSize; i++) {
                    x.setField(i, y.getField(i));
                }
            }
            try {
                int index = dimSize;
                for(int i = 0; i < queryMeasures.size(); i++){
                    QueryMeasure measure = queryMeasures.get(i);
                    index = i + dimSize;
                    if (measure.getAggregator().equals(Aggregator.DISTINCT_COUNT)) {
                        if(!x.getDistinctMeasures().containsKey(i)) {
                            x.getDistinctMeasures().put(i, new HashSet<>(defaultSize));
                        }
                        
                        if(y.getDistinctMeasures().containsKey(i)) {
                            x.getDistinctMeasures().get(i).addAll(y.getDistinctMeasures().get(i));
                        } else if(y.getField(index) != null) {
                            x.getDistinctMeasures().get(i).add(y.getField(index));
                        }
                        
                    } else {
                        x.setField(index, measure.getAggregator().aggregate(x.getField(index), y.getField(index)));
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return x;
        };
        final Collector<SearchIndexResultRecord, ?, SearchIndexResultRecord> reducing =
                Collectors.reducing(SearchIndexResultRecord.of(arraySize), reduceOperation);
        Map<String, SearchIndexResultRecord> groupResult = stream.collect(
                Collectors.groupingByConcurrent(SearchIndexResultRecord::getGroupBy, reducing));
        
        if(CollectionUtils.isNotEmpty(countIndex)) {
            groupResult.values().forEach(record -> {
                for (int index : countIndex) {
                    if(record.getDistinctMeasures() != null && record.getDistinctMeasures().containsKey(index)) {
                        record.setField(dimSize + index, record.getDistinctMeasures().get(index).size());
                    }
                }
            });
        }
        LOGGER.info("group agg(sum) cost: {}ms, size:{}!", (System.currentTimeMillis() - current), groupResult.size());
        return new ArrayList<>(groupResult.values());
    }
    

    /**
     * @param resultQ
     * @param query
     * @return
     */
    public static List<SearchIndexResultRecord> aggregate(List<SearchIndexResultRecord> resultQ, 
        QueryRequest query) {
        if (query.getGroupBy() != null && CollectionUtils.isNotEmpty(resultQ)) {
            int dimSize = query.getSelect().getQueryProperties().size();
            List<QueryMeasure> queryMeasures = query.getSelect().getQueryMeasures();
            return aggregate(resultQ, dimSize, queryMeasures);
        } else {
            return resultQ;
        }
    }
    
}
