package com.baidu.com.rigel.demo.lucene;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.util.BytesRef;

public class CollectorDemo extends Collector {
	
	private Map<String,Integer> countMap;
	private AtomicReader reader;
	private String fieldName;
	
	public CollectorDemo(String fieldName){
		this.fieldName=fieldName;
		countMap=new HashMap<String,Integer>();
	}

	@Override
	public void setScorer(Scorer scorer) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void collect(int doc) throws IOException {
		BytesRef fieldValue=FieldCache.DEFAULT.getTerms(this.reader, this.fieldName, false).get(doc);
		Integer count=this.countMap.get(fieldValue.utf8ToString());
		if(count==null){
			count=0;
		}
		count++;
		this.countMap.put(fieldValue.utf8ToString(), count);
	}

	@Override
	public void setNextReader(AtomicReaderContext context) throws IOException {
		this.reader=context.reader();

	}

	@Override
	public boolean acceptsDocsOutOfOrder() {
		// TODO Auto-generated method stub
		return false;
	}

	public Map<String, Integer> getCountMap() {
		return countMap;
	}
	
	

}
