package com.baidu.rigel.biplatform.schedule.exception;

/**
 * 产品线名称为空时的异常封装类
 * 
 * @author majun04
 *
 */
public class ProductLineNameIsNullException extends ScheduleException {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 2750143867802099889L;

    /**
     * ProductLineNameNullException
     */
    public ProductLineNameIsNullException() {
        super();
    }

    /**
     * ProductLineNameNullException
     * 
     * @param message message
     * @param cause cause
     */
    public ProductLineNameIsNullException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * ProductLineNameNullException
     * 
     * @param message message
     */
    public ProductLineNameIsNullException(String message) {
        super(message);
    }

    /**
     * ProductLineNameNullException
     * 
     * @param cause cause
     */
    public ProductLineNameIsNullException(Throwable cause) {
        super(cause);
    }

}
