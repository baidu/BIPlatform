/**
 * 
 */
package com.baidu.rigel.biplatform.tesseract.qsservice.query.vo;

import java.io.Serializable;

/**
 * @author cxm
 *
 */
public class Between implements Serializable {



	/**
	 * 
	 */
	private static final long serialVersionUID = 3643364850338802398L;
	
	private String properties;
	
	private String start;
	
	private String end;

	public String getProperties() {
		return properties;
	}

	public void setProperties(String properties) {
		this.properties = properties;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}
	
	@Override
	public String toString() {
		return properties + " between '" + start + "' and '" + end + "'";
	}

}
