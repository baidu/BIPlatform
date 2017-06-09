package com.baidu.rigel.biplatform.schedule.job;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.ac.util.HttpRequest;
import com.baidu.rigel.biplatform.schedule.bo.ScheduleTaskInfo;
import com.baidu.rigel.biplatform.schedule.constant.ScheduleConstant;
import com.google.common.collect.Maps;

/**
 * 该job只负责做httpclient的job处理
 * 
 * @author majun04
 *
 */
@Service
public class HttpClientExcuteJob extends BaseScheduleJob implements Job, BeanFactoryAware {
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(HttpClientExcuteJob.class);
    // private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private static final String SEPARATOR = "/";

    public static final String EXCUTE_ACTION_URLHOST = "${schedule.excuteActionUrlHost}";

    private static AtomicReference<String> excuteActionUrlHostRef = null;

    private ConfigurableBeanFactory beanFactoryHeld;

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.schedule.job.BaseScheduleJob#doExcute(org.quartz.JobExecutionContext)
     */
    @Override
    public void doExcute(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        Object excuteActionObj = jobDataMap.get(ScheduleConstant.EXCUTE_ACTION_KEY);
        String excuteAction = excuteActionObj == null ? "" : String.valueOf(excuteActionObj);
        // System.out.println(SDF.format(new Date()) +
        // "-----------------------HttpClientExcuteJob's excuteAction is : ["
        // + excuteAction + "]");
        LOG.info("-----------------------HttpClientExcuteJob's excuteAction is : [" + excuteAction + "]");
        String excuteActionUrlHost = excuteActionUrlHostRef.get();
        StringBuffer sb = new StringBuffer(excuteActionUrlHost);
        if (!excuteActionUrlHost.endsWith(SEPARATOR)) {
            sb.append("/");
        }
        sb.append(excuteAction);
        // TODO 联调时需要补充http参数部分逻辑
        Object taskId = jobDataMap.get(ScheduleConstant.TASK_ID);
        Object productLineName = jobDataMap.get(ScheduleConstant.PRODUCT_LINE_NAME);
        Map<String, String> params = Maps.newHashMap();
        params.put(ScheduleConstant.TASK_ID, String.valueOf(taskId));
        params.put(ScheduleConstant.PRODUCT_LINE_NAME, String.valueOf(productLineName));
        try {
            HttpRequest.sendGet(sb.toString(), params);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    // /**
    // * setExcuteActionUrlHost
    // *
    // * @param value realvalue
    // */
    // @Value("${schedule.excuteActionUrlHost}")
    // public void setExcuteActionUrlHost(String value) {
    // this.excuteActionUrlHost = value;
    // }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactoryHeld = (ConfigurableBeanFactory) beanFactory;
        String excuteActionUrlHostStr = beanFactoryHeld.resolveEmbeddedValue(EXCUTE_ACTION_URLHOST);
        excuteActionUrlHostRef = new AtomicReference<String>(excuteActionUrlHostStr);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.schedule.job.BaseScheduleJob#getCacheLockName(org.quartz.JobExecutionContext)
     */
    @Override
    public String getCacheLockName(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        ScheduleTaskInfo taskInfo = (ScheduleTaskInfo) jobDataMap.get(ScheduleConstant.SCHEDULE_TASK_OBJ_KEY);
        return taskInfo.getTaskId();
    }

}
