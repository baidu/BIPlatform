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
package com.baidu.rigel.biplatform.ma.resource.cache;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.ma.report.utils.ContextManager;

/**
 * cache名称检查工具类
 * @author zhongyi
 * 
 *         2014-7-31
 */
@Service("nameCheckCacheManager")
public class NameCheckCacheManager {

//    /**
//     * logger
//     */
//    private Logger logger = LoggerFactory.getLogger(NameCheckCacheManager.class);
//    /**
//     * REPORT_NAME_SIGN
//     */
//    private static final String REPORT_NAME_SIGN = "REPORT_NAME_";
    
    /**
     * cacheManagerForReource
     */
    @Resource
    private CacheManagerForResource cacheManagerForReource;
    
//    @Resource
//    private ReportDesignModelService reportDesignModelService;
    
    /**
     * 检查报表名称是否存在
     * @param reportName
     * @return
     */
//    public boolean existsReportName(String reportName) {
//        String productLine = ContextManager.getProductLine();
//        String key = CacheKeyGenerator.generateJointedKey(REPORT_NAME_SIGN, productLine, reportName);
//        Object sign = cacheManagerForReource.getFromCache(key);
//        if (sign == null) {
//            return false;
//        }
//        return true;
//    }
    
    
//    /**
//     * 添加使用的报表名称到缓存
//     * @param reportName
//     */
//    public void useReportName(String reportName) {
//        String productLine = ContextManager.getProductLine();
//        String key = CacheKeyGenerator.generateJointedKey(REPORT_NAME_SIGN, productLine, reportName);
//        try {
//            cacheManagerForReource.setToCache(key, 1);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
    
    /**
     * 检测当前id是否正在使用
     * @param dsName
     * @return
     */
    public boolean existsDSName(String dsName) {
        String productLine = ContextManager.getProductLine();
        String key = CacheKeyGenerator.generateJointedKey(productLine, dsName);
        Object sign = cacheManagerForReource.getFromCache(key);
        if (sign == null) {
            return false;
        }
        return true;
    }
    
    /**
     * 检测当前id是否正在使用
     * @param dsGroupName 数据源组名称
     * @param dsName 数据源名称
     * @return
     */
    public boolean existsDSName(String dsGroupName, String dsName) {
    	String productLine = ContextManager.getProductLine();
    	String key = CacheKeyGenerator.generateJointedKey(productLine, dsGroupName, dsName);
    	Object sign = cacheManagerForReource.getFromCache(key);
    	if (sign == null) {
    		return false;
    	}
    	return true;
    }
    /**
     * 添加正在使用的数据源id到缓存
     * @param dsName
     */
    public void useDSName(String dsName) {
        String productLine = ContextManager.getProductLine();
        String key = CacheKeyGenerator.generateJointedKey(productLine, dsName);
        try {
            cacheManagerForReource.setToCache(key, 1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 添加正在使用的数据源id到缓存
     * @param dsGroupName
     * @param dsName
     */
    public void userDSName(String dsGroupName, String dsName) {
    	String productLine = ContextManager.getProductLine();
        String key = CacheKeyGenerator.generateJointedKey(productLine, dsGroupName, dsName);
        try {
            cacheManagerForReource.setToCache(key, 1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 
     * @param resource
     */
    protected void setCacheManagerForResource(CacheManagerForResource resource) {
        this.cacheManagerForReource = resource;
    }
    
}