/**
 * 
 */
package com.baidu.rigel.biplatform.tesseract.resultset.isservice;

import java.io.Serializable;

/**
 * @author lijin
 *
 */
public interface TesseractResultRecord {
	/**
     * 
     * 根据下标获取字段
     * 
     * @param columnIndex
     *            下标
     * @return Object
     * @throws NoSuchFieldException
     *             ,可能抛出异常
     */
	Serializable getField(int columnIndex) throws NoSuchFieldException;

}
