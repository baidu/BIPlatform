package com.baidu.rigel.biplatform.queryrouter.handle;

import com.baidu.rigel.biplatform.cache.util.ApplicationContextHelper;
import com.baidu.rigel.biplatform.queryrouter.query.service.QueryService;

/**
 * 类QueryServiceFactory.java的实现描述：TODO 类实现描述 
 * @author luowenlei 2016年1月22日 下午1:23:03
 */
public class QueryServiceFactory {

    /**
     * getQueryService
     *
     * @param clz
     * @return
     * @throws RuntimeException
     * @throws Exception
     */
    public static QueryService getQueryService(Class<?> clz) throws RuntimeException, Exception {
        switch (clz.getName()) {
            case "com.baidu.rigel.biplatform.ac.query.model.ConfigQuestionModel" : {
                return (QueryService) ApplicationContextHelper.getContext().getBean(
                        "qmQueryService");
            }
            case "com.baidu.rigel.biplatform.ac.query.model.SqlQuestionModel" : {
                return (QueryService) ApplicationContextHelper.getContext().getBean(
                        "sqlQueryService");
            }
            default : {
                return null;
            }
        }
    }
}
