package com.baidu.rigel.biplatform.schedule.exception;

/**
 * 持久化调度任务已经存在的封装异常
 * 
 * @author majun04
 *
 */
public class PersitentTaskAlreadyExistException extends ScheduleException {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -392624487344615744L;

    /**
     * PersitentTaskAlreadyExistException
     */
    public PersitentTaskAlreadyExistException() {
        super();
    }

    /**
     * PersitentTaskAlreadyExistException
     * 
     * @param message message
     * @param cause cause
     */
    public PersitentTaskAlreadyExistException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * PersitentTaskAlreadyExistException
     * 
     * @param message message
     */
    public PersitentTaskAlreadyExistException(String message) {
        super(message);
    }

    /**
     * PersitentTaskAlreadyExistException
     * 
     * @param cause cause
     */
    public PersitentTaskAlreadyExistException(Throwable cause) {
        super(cause);
    }

}
