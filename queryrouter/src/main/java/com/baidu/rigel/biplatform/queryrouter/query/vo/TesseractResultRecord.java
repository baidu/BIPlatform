/**
 * 
 */
package com.baidu.rigel.biplatform.queryrouter.query.vo;

import java.io.Serializable;

/**
 * 类TesseractResultRecord.java的实现描述：TODO 类实现描述
 * 
 * @author luowenlei 2015年12月9日 下午8:35:31
 */
public interface TesseractResultRecord {
    
    /**
     * getField
     *
     * @param columnIndex
     * @return
     * @throws NoSuchFieldException
     */
    Serializable getField(int columnIndex) throws NoSuchFieldException;
    
}
