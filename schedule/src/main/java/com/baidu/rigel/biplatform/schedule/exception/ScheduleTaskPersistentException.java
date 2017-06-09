package com.baidu.rigel.biplatform.schedule.exception;

/**
 * 调度任务在持久化时的业务异常类
 * 
 * @author majun04
 *
 */
public class ScheduleTaskPersistentException extends ScheduleException {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 6321816359568164412L;

    /**
     * ScheduleTaskPersistentException constructor
     */
    public ScheduleTaskPersistentException() {
        super();
    }

    /**
     * ScheduleTaskPersistentException constructor
     * 
     * @param message message
     * @param cause cause
     */
    public ScheduleTaskPersistentException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * ScheduleTaskPersistentException constructor
     * 
     * @param message message
     */
    public ScheduleTaskPersistentException(String message) {
        super(message);
    }

    /**
     * ScheduleTaskPersistentException constructor
     * 
     * @param cause cause
     */
    public ScheduleTaskPersistentException(Throwable cause) {
        super(cause);
    }

}
