/**
 * 
 */
package com.baidu.rigel.biplatform.tesseract.isservice.search.collector;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldCache.Doubles;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.tesseract.resultset.isservice.Meta;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.SearchIndexResultRecord;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.SearchIndexResultSet;

/**
 * @author lijin
 *
 */
public class TesseractResultSetCollector extends Collector {
    
    /**
     * LOG
     */
    private static final Logger LOG = LoggerFactory.getLogger (TesseractResultSetCollector.class);
    
    /**
     * dimFields
     */
    private String[] dimFields;
    /**
     * measureFields
     */
    private String[] measureFields;    
    
    /**
     * reader
     */
    private AtomicReader reader;
    
    /**
     * cacheBinaryDocValuesMap
     */
    private Map<Integer,Map<String, BinaryDocValues>> cacheBinaryDocValuesMap;
    /**
     * cacheDoubleValuesMap
     */
    private Map<Integer,Map<String, FieldCache.Doubles>> cacheDoubleValuesMap;
    
    /**
     * resultDocBaseDocIdMap
     */
    private ConcurrentHashMap<Integer, List<Integer>> resultDocBaseDocIdMap;
    
    /**
     * 临时增加属性 缓存docBase和reader的对应关系
     */
//    private Map<Integer, AtomicReader> docBaseAndReadMap = Maps.newHashMap ();
    
    /**
     * 
     */
//    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool ();
    
    
    private int size;
    
    /**
     * docBase
     */
    private int docBase;
    
    
    /** 
     * 获取 size 
     * @return the size 
     */
    public int getSize() {
        return size;
    }


    /** 
     * 设置 size 
     * @param size the size to set 
     */
    public void setSize(int size) {
        this.size = size;
    }
    
    /**
     * 
     * Constructor by 
     * @param dimFields dimFields
     * @param measureFields measureFields
     */
    public TesseractResultSetCollector(String[] dimFields, String[] measureFields) {
        this.dimFields = dimFields;
        this.measureFields = measureFields;
        
        this.cacheBinaryDocValuesMap = new HashMap<Integer,Map<String, BinaryDocValues>>();
        this.cacheDoubleValuesMap = new HashMap<Integer,Map<String, FieldCache.Doubles>>();
        
        this.resultDocBaseDocIdMap = new ConcurrentHashMap<Integer, List<Integer>> ();
        this.size=0;
        
    }
    

    /* (non-Javadoc)
     * @see org.apache.lucene.search.Collector#setScorer(org.apache.lucene.search.Scorer)
     */
    @Override
    public void setScorer(Scorer scorer) throws IOException {

    }

    /* (non-Javadoc)
     * @see org.apache.lucene.search.Collector#collect(int)
     */
    @Override
    public void collect(int doc) throws IOException {
        this.size++;
        List<Integer> idList = this.resultDocBaseDocIdMap.get(this.docBase);
        if (idList == null) {
            idList = new ArrayList<Integer>();
        }
        idList.add(doc);
        this.resultDocBaseDocIdMap.put(this.docBase, idList);

    }

    /* (non-Javadoc)
     * @see org.apache.lucene.search.Collector#setNextReader(org.apache.lucene.index.AtomicReaderContext)
     */
    @Override
    public void setNextReader(AtomicReaderContext context) throws IOException {
        this.docBase = context.docBase;
        this.reader = context.reader();
//        this.docBaseAndReadMap.put (this.docBase, this.reader);
        Map<String, FieldCache.Doubles> currDoubleValuesMap=new HashMap<String, FieldCache.Doubles>();        
        for (String measure : measureFields) {
            currDoubleValuesMap.put(measure,
                    FieldCache.DEFAULT.getDoubles(this.reader, measure, false));
        }
        this.cacheDoubleValuesMap.put(this.docBase, currDoubleValuesMap);
        
        Map<String, BinaryDocValues> currBinaryDocValuesMap=new HashMap<String, BinaryDocValues>();
        for (String dim : dimFields) {
            currBinaryDocValuesMap.put(dim, FieldCache.DEFAULT.getTerms(this.reader, dim, false));
        }
        
        this.cacheBinaryDocValuesMap.put(this.docBase, currBinaryDocValuesMap);

    }

    /* (non-Javadoc)
     * @see org.apache.lucene.search.Collector#acceptsDocsOutOfOrder()
     */
    @Override
    public boolean acceptsDocsOutOfOrder() {
        // TODO Auto-generated method stub
        return false;
    }
    
    public SearchIndexResultSet buildResultSet (Set<String> groupByFields) {
//        Meta meta = new Meta(mergeDimAndMeasure());
        Meta meta = new Meta((String[]) ArrayUtils.addAll(this.dimFields, this.measureFields));
        SearchIndexResultSet result = new SearchIndexResultSet(meta, this.size);
        if (this.size == 0) {
            return result;
        }
        

        long begin = System.currentTimeMillis ();
        resultDocBaseDocIdMap.keySet ().parallelStream ().forEach (docbase -> {
                List<Integer> idList = resultDocBaseDocIdMap.get (docbase);
                buidlAndAddRecordIntoResult (idList, docbase, groupByFields, result);
        });
        
//        this.resultDocBaseDocIdMap.keySet().parallelStream ().forEach (docBase -> {
//           Map<String, FieldCache.Doubles> currDoubleValuesMap = this.cacheDoubleValuesMap.get(docBase);
//           Map<String, BinaryDocValues> currBinaryDocValuesMap = this.cacheBinaryDocValuesMap.get(docBase);
//          for (Integer docId : this.resultDocBaseDocIdMap.get(docBase)) {
//              Serializable[] fieldValueArray = new Serializable[this.dimFields.length
//                      + this.measureFields.length];
//              String groupBy = "";
//              int i = 0;
//              for (String dim : dimFields) {
//                  BinaryDocValues fieldValues = currBinaryDocValuesMap.get(dim);
//                  BytesRef byteRef = fieldValues.get(docId);
//                  String dimVal = byteRef.utf8ToString();
//                  fieldValueArray[i++] = dimVal;
//
//                  if (groupByFields.contains(dim)) {
//                      groupBy += dimVal + ",";
//                  }
//              }
//
//              for (String measure : this.measureFields) {
//                  FieldCache.Doubles fieldValues = currDoubleValuesMap
//              result.addRecord(record);
//          }
//        });
        
        LOG.info ("===================== build result cost {} ms", (System.currentTimeMillis () - begin));
        return result;
    }

    /**
     * 
     * @param idList
     * @param docbase
     * @param groupByFields
     * @param result
     */
    private void buidlAndAddRecordIntoResult(List<Integer> idList, Integer docbase, Set<String> groupByFields,
            SearchIndexResultSet result) {
//        List<Future<Object>> rs = new ArrayList<> ((idList.size () / 100000) + 1);
//        for (int i = 0; i < idList.size (); i += 100000) {
//            int end = i + 100000;
//            if (end >= idList.size ()) {
//                end = idList.size ();
//            }
//            rs.add (THREAD_POOL.submit (new ResultRecordBuildTask (i, end, idList, docbase, groupByFields, result)));
//        }
//        for (Future<Object> f : rs) {
//            try {
//                f.get ();
//            } catch (InterruptedException | ExecutionException e) {
//                e.printStackTrace();
//            }
//        }
//        AtomicReader reader = docBaseAndReadMap.get (docbase);
        Map<String, Doubles> measureValueCacheMap = this.cacheDoubleValuesMap.get (docbase);
        Map<String, BinaryDocValues> dimValueMapCache = this.cacheBinaryDocValuesMap.get (docbase);
        for (Integer docId : idList) {
                try {
                    Serializable[] fieldValueArray = new Serializable[dimFields.length + measureFields.length];
                    String groupBy = "";
                    int index = 0;
                    for (String dim : dimFields) {
                        BinaryDocValues fieldValues = dimValueMapCache.get (dim);
                        BytesRef byteRef = fieldValues.get (docId);
                        String dimVal = byteRef.utf8ToString ();
                        fieldValueArray[index++] = dimVal;
                        if (groupByFields.contains (dim)) {
                            groupBy += dimVal + ",";
                        }
                    }
                    
                    for (String measure : measureFields) {
                        FieldCache.Doubles fieldValues = measureValueCacheMap.get (measure);
                        fieldValueArray[index++] = fieldValues.get (docId);
                    }
                    SearchIndexResultRecord record = new SearchIndexResultRecord (fieldValueArray, groupBy);
                    result.addRecord (record);
                } catch (Exception e) {
                    LOG.error (e.getMessage (), e);
                }
        }
//        );
    }

//    private class ResultRecordBuildTask implements Callable<Object> {
//
//        private final int begin;
//        
//        private final int end;
//        /**
//         * 
//         */
//        private final List<Integer> idList;
//        
//        /**
//         * 
//         */
//        private final Integer docBase;
//        
//        /**
//         * 
//         */
//        private final Set<String> groupByFields;
//        
//        private final SearchIndexResultSet result;
//        
//        /**
//         * 
//         * @param beginIndex
//         * @param endIndex
//         * @param idList
//         */
//        public ResultRecordBuildTask (final int begin, final int end, 
//                final List<Integer> idList, final Integer docBase, 
//                final Set<String> groupByFields, SearchIndexResultSet result) {
//            this.begin = begin;
//            this.end = end;
//            this.idList = idList;
//            this.docBase = docBase;
//            this.groupByFields = groupByFields;
//            this.result = result;
//        }
//        
//        /**
//         * 根据指定id列表、起至编号大小构建查询结果单元
//         * 注意：该方法不会检验数组越界、起至索引大小不对等数据安全问题，需要由上游任务调度业务关注
//         * @return 返回结果为：[beginIndex, endIndex) 半闭区间个数个结果
//         */
//        @Override
//        public Object call() throws Exception {
//            AtomicReader reader = docBaseAndReadMap.get (docBase);
////            SearchIndexResultRecord[] rs = new SearchIndexResultRecord[idList.size ()];
////            Map<String, BinaryDocValues> fieldValueMap = Maps.newHashMap ();
////            for (String dim : dimFields) {
////                fieldValueMap.put (dim, FieldCache.DEFAULT.getTerms(reader, dim, false));
////            }
////            idList.parallelStream ().forEach (docId -> {
////                try {
////                    Serializable[] fieldValueArray = new Serializable[dimFields.length + measureFields.length];
////                    String groupBy = "";
////                    int index = 0;
////                    for (String dim : dimFields) {
////                        BinaryDocValues fieldValues = FieldCache.DEFAULT.getTerms(reader, dim, false);
////                        BytesRef byteRef = fieldValues.get(docId);
////                        String dimVal = byteRef.utf8ToString();
////                        fieldValueArray[index++] = dimVal;
////                        if (groupByFields.contains(dim)) {
////                            groupBy += dimVal + ",";
////                        }
////                    }
////
////                    for (String measure : measureFields) {
//////                        FieldCache.Doubles  fieldValues = FieldCache.DEFAULT.getDoubles(reader, measure, false);
//////                        fieldValueArray[fieldValueArrayIndex++] = fieldValues.get(docId);
////                        BinaryDocValues fieldValues = FieldCache.DEFAULT.getTerms(reader, measure, false);
////                        fieldValueArray[index++] = fieldValues.get(docId).utf8ToString ();
////                    }
////                    SearchIndexResultRecord record = 
////                                new SearchIndexResultRecord(fieldValueArray, groupBy);
////                    result.addRecord (record);
////                } catch (Exception e) {
////                    e.printStackTrace ();
////                }
////            });
//            
//            for (int j = begin; j < end; ++j) {
//                Serializable[] fieldValueArray = new Serializable[dimFields.length + measureFields.length];
//                  String groupBy = "";
//                  int index = 0;
//                  for (String dim : dimFields) {
//                      BinaryDocValues fieldValues = FieldCache.DEFAULT.getTerms(reader, dim, false);
//                      BytesRef byteRef = fieldValues.get(idList.get (j));
//                      String dimVal = byteRef.utf8ToString();
//                      fieldValueArray[index++] = dimVal;
//                      if (groupByFields.contains(dim)) {
//                          groupBy += dimVal + ",";
//                      }
//                  }
//
//                  for (String measure : measureFields) {
//                      FieldCache.Doubles  fieldValues = FieldCache.DEFAULT.getDoubles(reader, measure, false);
//                      fieldValueArray[index++] = fieldValues.get(idList.get (j));
//                  }
//
//                  SearchIndexResultRecord record = new SearchIndexResultRecord(fieldValueArray, groupBy);
//                  result.addRecord (record);
//            }
//            return null;
//        }
//        
//    }

//    /**
//     * 合并维度、指标定义
//     * 注意：此处假设dimFields measureFields不会变化，如变化，此方法会有问题
//     * @return String[] 
//     */
//    private String[] mergeDimAndMeasure() {
//        if (this.dimFields == null) {
//            return this.measureFields;
//        }
//        if (this.measureFields == null) {
//            return this.dimFields;
//        }
//        int dimFieldLength = this.dimFields.length;
//        String[] rs = new String[dimFieldLength + this.measureFields.length];
//        System.arraycopy (this.dimFields, 0, rs, 0, dimFieldLength);
//        System.arraycopy (this.measureFields, 0, rs, dimFieldLength, this.measureFields.length);
//        return rs;
//    }

}
