package com.baidu.rigel.biplatform.schedule.listener;

/**
 * 每次当服务启动完成之后，需要进行初始化工作的抽象接口，任何实现该接口的类，都可以在指定方法内做自己的初始化工作
 * 
 * @author majun04
 *
 */
public interface ServerStartedJob {
    /**
     * 该方法会在每次服务启动完全后，由spring的ContextRefreshedEvent触发调用
     */
    public void doInitJob();
}
