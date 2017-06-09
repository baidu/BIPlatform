package com.baidu.rigel.biplatform.schedule.job;

import java.util.concurrent.locks.Lock;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

import com.baidu.rigel.biplatform.cache.StoreManager;
import com.baidu.rigel.biplatform.cache.util.ApplicationContextHelper;

/**
 * 调度任务基础类，这里主要负责同步集群环境下调度任务状态的执行一致性，保证在同一周期内，同一任务只会被集群下某一节点执行一次
 * 
 * @author majun04
 *
 */
public abstract class BaseScheduleJob implements Job {
    /**
     * log
     */
    private static final Logger LOG = LoggerFactory.getLogger(BaseScheduleJob.class);

    private static final String DELAY_TIME = "${schedule.lock.delaytime}";
    private final ConfigurableBeanFactory beanFactory = (ConfigurableBeanFactory) ApplicationContextHelper
            .getBeanFactory();

    /*
     * (non-Javadoc)
     * 
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String cacheLockName = getCacheLockName(context);
        Lock cacheLock = getStoreManager().getClusterLock(cacheLockName);
        try {
            boolean hasLock = cacheLock.tryLock();
            // 只有当前服务节点锁住取得了分布式锁，说明这个批次调度任务被本节点抢到，这时可以执行具体任务，否则直接跳过
            if (hasLock) {
                this.doExcute(context);
                try {
                    /**
                     * 因为当前的quartz是基于ram模式进行的调度管理，不同节点直接的通信可能会有延迟，目前轻dirty的解决方案为: 取得锁之后不要马上释放 而是保持锁只有一段周期，以便同步别的节点的延迟
                     */
                    Thread.sleep(getLockDelayTime());
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage(), e);
                }
            } else {
                LOG.info("the schedule server competition task failed ，so pass~");
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } finally {
            cacheLock.unlock();
        }

    }

    /**
     * 从spring全局上下文中获取StoreManager实例
     * 
     * @return 返回StoreManager实现实例
     */
    private StoreManager getStoreManager() {
        StoreManager storeManager = (StoreManager) ApplicationContextHelper.getContext().getBean("redisStoreManager");
        if (storeManager == null) {
            storeManager = (StoreManager) ApplicationContextHelper.getContext().getBean("hazelcastStoreManager");
        }
        return storeManager;
    }

    /**
     * 从配置文件中读取lock的sleep时长
     * 
     * @return 返回锁的delay时长
     */
    private long getLockDelayTime() {
        String delayTimeStr = beanFactory.resolveEmbeddedValue(DELAY_TIME);
        return Long.valueOf(delayTimeStr);
    }

    /**
     * 实际的调度任务逻辑执行方法
     * 
     * @param context context调度任务上下文
     */
    public abstract void doExcute(JobExecutionContext context);

    /**
     * 取得分布式缓存锁名称
     * 
     * @param context 调度任务上下文
     * @return 返回分布式缓存锁名称
     */
    public abstract String getCacheLockName(JobExecutionContext context);

}
