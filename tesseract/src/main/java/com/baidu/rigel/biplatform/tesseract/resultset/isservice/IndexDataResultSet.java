/**
 * 
 */
package com.baidu.rigel.biplatform.tesseract.resultset.isservice;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet;
import com.baidu.rigel.biplatform.tesseract.resultset.exception.NotSupportedDateFormatException;
import com.baidu.rigel.biplatform.tesseract.util.DateFormatType;
import com.baidu.rigel.biplatform.tesseract.util.StringTools;

/**
 * @author lijin
 *
 */
public class IndexDataResultSet implements
		TesseractResultSet<IndexDataResultRecord> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3799015210159370093L;
	/**
     * 结果集的元数据
     */
    private Meta meta;    
    /**
     * 结果集数据
     */
    private List<IndexDataResultRecord> dataList;
//    /**
//     * 当前数据下标
//     */
//    private int currIdx;
    /**
     * 当前结果集
     */
    private IndexDataResultRecord currRecord;
    
    
    /**
     * 构造函数
     * @param meta 元数据
     */
    public IndexDataResultSet(Meta meta){
    	super();
		this.meta = meta;
		this.dataList = Collections.synchronizedList(new ArrayList<IndexDataResultRecord>());
    }

    /**
     * 构造函数
     * @param meta 元数据
     * @param size 长度
     */
	public IndexDataResultSet(Meta meta, Integer size) {
		super();
		this.meta = meta;
		this.dataList = Collections.synchronizedList (new ArrayList<IndexDataResultRecord>(size));
//		this.currIdx=0;
	}
	
	/**
	 * 增加记录
	 * @param sr 数据记录
	 * @return boolean
	 */
	public boolean addRecord(IndexDataResultRecord sr){
		return this.dataList.add(sr);
	}

	 /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet#next()
     */
    @Override
    public boolean next() throws IOException {
        boolean result = false;
        
        if (CollectionUtils.isNotEmpty(dataList)) {
            this.currRecord=this.dataList.remove(0);
            if (this.currRecord == null) {
                result = false;
            } else {
                result = true;
            }
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet#close()
     */
    @Override
    public void close() {
        // TODO Auto-generated method stub
        if (this.dataList != null) {
            this.dataList.clear();
        }
        this.dataList = null;
        this.currRecord = null;
    }

    /**
     * 
     * convert2String
     * 
     * @param field 待格式化的字段
     * @return String
     */
    private String convert2String(Object field) {

        return field == null ? null : field.toString();
    }

    /**
     * 
     * convert2Int
     * 
     * @param field 待格式化的字段
     * @return int
     */
    private int convert2Int(Object field) {
        return Integer.valueOf((String) field);
    }

    /**
     * 
     * convert2Long
     * 
     * @param field 待格式化的字段
     * @return long
     */
    private long convert2Long(Object field) {
        return Long.valueOf((String) field);
    }

    /**
     * 
     * convert2Date
     * 
     * @param field 待格式化的字段
     * @return Date
     * @throws NotSupportedDateFormatException 可以抛出异常
     * @throws ParseException 可以抛出异常
     */
    private Date convert2Date(Object field) throws NotSupportedDateFormatException, ParseException {
        String dateStr = (String) field;

        DateFormatType dateType = StringTools.dateFormatType(dateStr);
        if (dateType == null) {
            throw new NotSupportedDateFormatException();
        }
        return dateType.getFormatter().parse(dateStr);
    }

    /**
     * 
     * convert2BigDecimal
     * 
     * @param field 待格式化的字段
     * @return BigDecimal
     */
    private BigDecimal convert2BigDecimal(Object field) {

        return new BigDecimal(field.toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet#getString (int)
     */
    @Override
    public String getString(int columnIndex) throws NoSuchFieldException {
        Object field = this.currRecord.getField(columnIndex);
        return this.convert2String(field);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet#getString (java.lang.String)
     */
    @Override
    public String getString(String columnLable) throws NoSuchFieldException {
        Object field = this.currRecord.getField(this.meta.getFieldNames().get(columnLable));
        return convert2String(field);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet#getInt (int)
     */
    @Override
    public int getInt(int columnIndex) throws NoSuchFieldException {
        Object field = this.currRecord.getField(columnIndex);
        return this.convert2Int(field);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet#getInt (java.lang.String)
     */
    @Override
    public int getInt(String columnLabel) throws NoSuchFieldException {
        Object field = this.currRecord.getField(this.meta.getFieldNames().get(columnLabel));
        return convert2Int(field);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet#getLong (int)
     */
    @Override
    public long getLong(int columnIndex) throws NoSuchFieldException {
        Object field = this.currRecord.getField(columnIndex);
        return this.convert2Long(field);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet#getLong (java.lang.String)
     */
    @Override
    public long getLong(String columnLabel) throws NoSuchFieldException {
        Object field = this.currRecord.getField(this.meta.getFieldNames().get(columnLabel));
        return convert2Long(field);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet#getDate (int)
     */
    @Override
    public Date getDate(int columnIndex) throws NoSuchFieldException, NotSupportedDateFormatException, ParseException {
        Object field = this.currRecord.getField(columnIndex);
        return convert2Date(field);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet#getDate (java.lang.String)
     */
    @Override
    public Date getDate(String columnLabel) throws NoSuchFieldException, ParseException,
            NotSupportedDateFormatException {
        Object field = this.currRecord.getField(this.meta.getFieldNames().get(columnLabel));
        return convert2Date(field);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet# getBigDecimal(int)
     */
    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws NoSuchFieldException {
        Object field = this.currRecord.getField(columnIndex);
        return convert2BigDecimal(field);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet# getBigDecimal(java.lang.String)
     */
    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws NoSuchFieldException {
        Object field = this.currRecord.getField(this.meta.getFieldNames().get(columnLabel));
        if (field == null) {
            return BigDecimal.valueOf(0L);
        }
        return convert2BigDecimal(field);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet# getFieldArray()
     */
    @Override
    public String[] getFieldNameArray() {
        return this.meta.getFieldNames().keySet().toArray(new String[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet#size()
     */
    @Override
    public int size() {
        if (this.dataList != null) {
            return dataList.size();
        }
        return 0;
    }

    

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("IndexDataResultSet ");
        sb.append("[size=" + this.size() + "]");
        sb.append("[resultQ=[");
        sb.append(" get " + this.dataList.size() + " result");
        sb.append("]");
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet#getCurrentRecord()
     */
    @Override
    public IndexDataResultRecord getCurrentRecord() {
        return this.currRecord;
    }

    @Override
    public List<IndexDataResultRecord> getDataList() {
        return this.dataList;
        
    }

    /** 
     * 设置 dataList 
     * @param dataList the dataList to set 
     */
    public void setDataList(List<IndexDataResultRecord> dataList) {
    
        this.dataList = dataList;
    }

    /** 
     * 获取 meta 
     * @return the meta 
     */
    public Meta getMeta() {
    
        return meta;
    }

    /**
     * 批量增加结果
     * @param indexResultRecords
     */
    public synchronized void addAll(IndexDataResultRecord[] indexResultRecords) {
        Collections.addAll (this.dataList, indexResultRecords);
    }

}
