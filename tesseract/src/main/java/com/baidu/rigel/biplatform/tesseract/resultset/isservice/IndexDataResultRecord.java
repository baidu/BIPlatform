package com.baidu.rigel.biplatform.tesseract.resultset.isservice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IndexDataResultRecord implements Serializable,
		TesseractResultRecord {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4152257061309543135L;

	/**
     * 一行记录，结果数据
     */
    private Serializable[] fieldArray;    
    
    /**
     * groupBy 将groupBy的字段的值用,分隔
     */
    private String groupBy;
    
	public IndexDataResultRecord(Serializable[] feildArray,String groupBy) {
		 super();
	        this.fieldArray = feildArray;
	        this.groupBy=groupBy;
	}
	
	public IndexDataResultRecord(int fieldLength){
		this.fieldArray = new Serializable[fieldLength];
	}

	
   
    
    /*
     * (non-Javadoc)
     * @see com.baidu.rigel.biplatform.tesseract.resultset.isservice.TesseractResultRecord#getField(int)
     */
    @Override
    public Serializable getField(int columnIndex)  {
        if (this.fieldArray != null && columnIndex < this.fieldArray.length) {
            return fieldArray[columnIndex];
        }
        System.out.println(this.toString());
        throw new IllegalArgumentException("FieldIndex:" + columnIndex);
    }
    
    
    
    
    public void setField(int columnIndex, Serializable newField) {
        if (columnIndex < 0 || columnIndex > fieldArray.length) {
            throw new IndexOutOfBoundsException("index:" + columnIndex + " fieldArray length:" + fieldArray.length);
        }
        this.fieldArray[columnIndex] = newField;
    }
    
    /**
     * 
     * getFieldList
     * 
     * @return List<Object>
     */
    public List<Serializable> getFieldList() {
        List<Serializable> fieldList = new ArrayList<Serializable>();
        for (Serializable field : this.fieldArray) {
            fieldList.add(field);
        }
        return fieldList;
    }
    
    
    
    
    @Override
    public String toString() {
        return "ResultRecord [fieldArray=" + Arrays.toString(fieldArray) + ", groupBy=" + groupBy
                + "]";
    }
    
    public int getFieldArraySize() {
        if(this.fieldArray != null) {
            return this.fieldArray.length;
        }
        return 0;
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(fieldArray);
		result = prime * result + ((groupBy == null) ? 0 : groupBy.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof IndexDataResultRecord)) {
			return false;
		}
		IndexDataResultRecord other = (IndexDataResultRecord) obj;
		if (!Arrays.equals(fieldArray, other.fieldArray)) {
			return false;
		}
		if (groupBy == null) {
			if (other.groupBy != null) {
				return false;
			}
		} else if (!groupBy.equals(other.groupBy)) {
			return false;
		}
		return true;
	}
    
    
    




}
