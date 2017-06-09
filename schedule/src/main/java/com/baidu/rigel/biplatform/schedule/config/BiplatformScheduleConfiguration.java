package com.baidu.rigel.biplatform.schedule.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.baidu.rigel.biplatform.schedule.job.DefaultJob;

/**
 * 调度模块配置类
 * 
 * @author majun04
 *
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnProperty(prefix = "", name = "schedule.enable", havingValue = "true")
@ImportResource({ "conf/schedule.xml" })
public class BiplatformScheduleConfiguration {

  

    @Bean(name = "methodInvokingJobDetailFactoryBean")
    public MethodInvokingJobDetailFactoryBean methodInvokingJobDetailFactoryBean() {
        MethodInvokingJobDetailFactoryBean methodInvokingJobDetailFactoryBean =
                new MethodInvokingJobDetailFactoryBean();
        methodInvokingJobDetailFactoryBean.setTargetObject(defaultJob());
        methodInvokingJobDetailFactoryBean.setTargetMethod("doExcute");
        return methodInvokingJobDetailFactoryBean;
    }

    @Bean
    public DefaultJob defaultJob() {
        DefaultJob defaultJob = new DefaultJob();
        return defaultJob;
    }

    @Bean(name = "cronTriggerFactoryBean")
    public CronTriggerFactoryBean cronTriggerFactoryBean() {
        CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
        cronTriggerFactoryBean.setCronExpression("0/30 * * * * ?");
        cronTriggerFactoryBean.setJobDetail(methodInvokingJobDetailFactoryBean().getObject());
        return cronTriggerFactoryBean;
    }

    @Bean(name = "schedulerBean")
    public SchedulerFactoryBean schedulerBean() {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setTriggers(cronTriggerFactoryBean().getObject());
        return schedulerFactoryBean;
    }

}
