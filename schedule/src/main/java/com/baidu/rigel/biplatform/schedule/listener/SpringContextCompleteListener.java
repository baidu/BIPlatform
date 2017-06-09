package com.baidu.rigel.biplatform.schedule.listener;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

/**
 * spring上下文加载完毕后执行的事件
 * 
 * @author majun04
 *
 */
@Service
public class SpringContextCompleteListener implements ApplicationListener<ContextRefreshedEvent>,
        ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(SpringContextCompleteListener.class);

    private ApplicationContext applicationContext = null;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        LOG.info("begin do ContextRefreshedEvent.....");
        Map<String, ServerStartedJob> serverStartedJobBeanMap =
                (Map<String, ServerStartedJob>) applicationContext.getBeansOfType(ServerStartedJob.class);
        if (!MapUtils.isEmpty(serverStartedJobBeanMap)) {
            for (Map.Entry<String, ServerStartedJob> serverStartedJobBean : serverStartedJobBeanMap.entrySet()) {
                LOG.info("the bean " + serverStartedJobBean.getValue().getClass() + " begin  doInitJob.....");
                serverStartedJobBean.getValue().doInitJob();
                LOG.info("the bean " + serverStartedJobBean.getValue().getClass() + " finish  initJob.....");
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
