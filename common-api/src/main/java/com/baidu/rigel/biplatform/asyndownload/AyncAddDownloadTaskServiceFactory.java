package com.baidu.rigel.biplatform.asyndownload;

import com.baidu.rigel.biplatform.cache.util.ApplicationContextHelper;


/**
 * 类AyncAddDownloadTaskServiceFactory.java的实现描述：异步下载工厂 类实现描述 
 * @author luowenlei 2015年8月28日 下午12:16:30
 */
public class AyncAddDownloadTaskServiceFactory {

    /**
     * getAyncAddDownloadTaskService
     *
     * @param name
     * @return AyncAddDownloadTaskService
     * @throws Exception
     */
    public static AyncAddDownloadTaskService getAyncAddDownloadTaskService(String name) throws Exception {
        try {
            Object obj = ApplicationContextHelper.getContext().getBean(name);
            if (obj == null) {
                return null;
            }
            return (AyncAddDownloadTaskService) obj;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
