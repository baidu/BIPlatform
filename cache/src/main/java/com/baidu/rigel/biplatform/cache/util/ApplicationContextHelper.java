/**
 * Copyright (c) 2014 Baidu, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baidu.rigel.biplatform.cache.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * ApplicationContextHelper，主要负责存储spring的ApplicationContext上下文和beanFactory实例
 * 
 * @author xiaoming.chen
 * @version 2015年2月27日
 * @since jdk 1.8 or after
 */
@Service
public class ApplicationContextHelper implements BeanFactoryAware {

    private static ApplicationContext applicationContext;
    private static BeanFactory beanFactory;

    /**
     * setContext
     * 
     * @param context
     */
    public static void setContext(ApplicationContext context) {
        applicationContext = context;
    }

    /**
     * getContext
     * 
     * @return
     */
    public static ApplicationContext getContext() {
        return applicationContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        ApplicationContextHelper.beanFactory = beanFactory;
    }

    /**
     * getBeanFactory
     * 
     * @return beanFactory
     */
    public static BeanFactory getBeanFactory() {
        return beanFactory;
    }
}
