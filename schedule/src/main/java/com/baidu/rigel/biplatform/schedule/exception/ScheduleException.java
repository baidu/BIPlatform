package com.baidu.rigel.biplatform.schedule.exception;

/**
 * 调度引擎统一异常类
 * 
 * @author majun04
 *
 */
public class ScheduleException extends Exception {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -6998432723654584151L;

    public ScheduleException() {
        super();
    }

    /**
     * ScheduleException constructor
     * 
     * @param message message
     * @param cause cause
     */
    public ScheduleException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * ScheduleException constructor
     * 
     * @param message message
     */
    public ScheduleException(String message) {
        super(message);
    }

    /**
     * ScheduleException constructor
     * 
     * @param cause cause
     */
    public ScheduleException(Throwable cause) {
        super(cause);
    }

}
