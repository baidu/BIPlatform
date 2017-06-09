/**
 * 
 */
package com.baidu.rigel.biplatform.queryrouter.query.vo.sql;

import java.io.Serializable;


/**
 * 类Between.java的实现描述：TODO 类实现描述 
 * @author luowenlei 2015年12月9日 下午9:04:26
 */
public class Between implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 3643364850338802398L;
    
    /**
     * properties
     */
    private String properties;
    
    /**
     * start
     */
    private String start;
    
    /**
     * end
     */
    private String end;
    
    /**
     * getProperties
     *
     * @return
     */
    public String getProperties() {
        return properties;
    }
    
    /**
     * setProperties
     *
     * @param properties
     */
    public void setProperties(String properties) {
        this.properties = properties;
    }
    
    /**
     * getStart
     *
     * @return
     */
    public String getStart() {
        return start;
    }
    
    /**
     * setStart
     *
     * @param start
     */
    public void setStart(String start) {
        this.start = start;
    }
    
    /**
     * getEnd
     *
     * @return
     */
    public String getEnd() {
        return end;
    }
    
    /**
     * setEnd
     *
     * @param end
     */
    public void setEnd(String end) {
        this.end = end;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return properties + " between '" + start + "' and '" + end + "'";
    }
    
}
