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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import com.baidu.rigel.biplatform.ma.report.exception.CacheOperationException;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaContext;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.query.ReportRuntimeModel;
import com.baidu.rigel.biplatform.ma.report.service.ReportDesignModelService;
import com.baidu.rigel.biplatform.ma.report.utils.ContextManager;

/**
 * 
 * 报表缓存管理服务
 * 
 * @author zhongyi
 * 
 *         2014-7-31
 */
@Service("reportModelCacheManager")
public class ReportModelCacheManager {
    
    private static final Logger LOG = 
        LoggerFactory.getILoggerFactory ().getLogger (ReportModelCacheManager.class.getName ());
    
    /**
     * cacheManagerForReource
     */
    @Resource
    private CacheManagerForResource cacheManagerForReource;
    
    /**
     * reportDesignModelService
     */
    @Resource
    private ReportDesignModelService reportDesignModelService;
    
    /**
     * 从缓存中获取报表模型
     * 
     * @param reportId
     * @return
     * @throws Exception
     */
    public ReportDesignModel getReportModel(String reportId) throws CacheOperationException {
        String key = genReportKey(reportId);
        // ReportDesignModel model = (ReportDesignModel)
        // cacheManagerForReource.getFromCache(key);
        long begin = System.currentTimeMillis ();
        byte[] modelBytes = (byte[]) cacheManagerForReource.getFromCache(key);
        ReportDesignModel model = (ReportDesignModel) SerializationUtils.deserialize(modelBytes);
        
        if (model == null) {
            throw new CacheOperationException("No such Model in cache!");
        }
        LOG.info ("get report model from cache cost : {} ms", (System.currentTimeMillis () - begin));
        return model;
    }
    
    /**
     * 加载报表模型到cache
     * 
     * @param reportId
     * @throws Exception
     */
    public ReportDesignModel loadReportModelToCache(String reportId) throws CacheOperationException {
        ReportDesignModel reportModel = reportDesignModelService.getModelByIdOrName(reportId, false);
        try {
            updateReportModelToCache(reportId, reportModel);
        } catch (CacheOperationException e1) {
            throw e1;
        }
        return reportModel;
    }
    
    /**
     * 加载报表模型到cache
     * 
     * @param reportId
     * @throws Exception
     */
    public ReportDesignModel loadReleaseReportModelToCache(String reportId) throws CacheOperationException {
        ReportDesignModel reportModel = reportDesignModelService.getModelByIdOrName(reportId, true);
        try {
            updateReportModelToCache(reportId, reportModel);
        } catch (CacheOperationException e1) {
            throw e1;
        }
        return reportModel;
    }
    
    /**
     * 删除缓存中报表模型
     * 
     * @param reportId
     * @throws Exception
     */
    public void deleteReportModel(String reportId) throws CacheOperationException {
        String key = genReportKey(reportId);
        cacheManagerForReource.deleteFromCache(key);
    }
    
    /**
     * 更新报表模型到cache
     * 
     * @param reportId
     * @param reportModel
     * @throws Exception
     */
    public void updateReportModelToCache(String reportId, ReportDesignModel reportModel)
            throws CacheOperationException {
        long begin = System.currentTimeMillis ();
        String sessionReportKey = genReportKey(reportId);
        try {
            byte[] modelBytes = SerializationUtils.serialize(reportModel);
            LOG.info ("current model size : " + (modelBytes.length / 1024.0 / 1024.0) + "m");
            cacheManagerForReource.setToCache(sessionReportKey, modelBytes);
        } catch (CacheOperationException e) {
            throw e;
        }
        LOG.info ("put report model into cache cost {} ms", (System.currentTimeMillis () - begin));
    }

    /**
     * @param reportId
     * @return String
     */
    private String genReportKey(String reportId) {
        String sessionId = ContextManager.getSessionId();
        String productLine = ContextManager.getProductLine();
        String sessionReportKey = CacheKeyGenerator.generateSessionReportKey(sessionId, reportId,
            productLine);
        return sessionReportKey;
    }
    
    /**
     * 从缓存中获取运行时模型
     * 
     * @param reportId
     * @return
     * @throws CacheOperationException
     */
    public ReportRuntimeModel getRuntimeModel(String reportId) throws CacheOperationException {
        long begin = System.currentTimeMillis ();
        ReportRuntimeModel runTimeModel = getRuntimeModelUnsafety(reportId);
        if (runTimeModel == null) {
            throw new CacheOperationException("No such Model in cache!");
        }
        LOG.info ("get runtimemodel from cache cost : {} ms", (System.currentTimeMillis () - begin));
        return runTimeModel;
    }

    /**
     * 从缓存中获取运行时模型(不安全)
     * @param reportId
     * @return
     */
    public ReportRuntimeModel getRuntimeModelUnsafety(String reportId) {
        String sessionId = ContextManager.getSessionId();
        String productLine = ContextManager.getProductLine();
        String runtimeReportKey = CacheKeyGenerator.generateRuntimeReportKey(sessionId, reportId,
            productLine);
        byte[] modelBytes = (byte[]) cacheManagerForReource.getFromCache(runtimeReportKey);
        ReportRuntimeModel runTimeModel = (ReportRuntimeModel) SerializationUtils
            .deserialize(modelBytes);
        return runTimeModel;
    }
    
    
    
    
    /**
     * 加载运行模型到分布式缓存
     * 定义未同步操作，避免多次加载重复初始化并且避免runtime model壮体啊不一致
     * @param reportId
     * @throws Exception
     */
    public ReportRuntimeModel loadRunTimeModelToCache(String reportId) throws CacheOperationException {
        
        ReportDesignModel reportModel = getReportModel(reportId);
        if (reportModel == null) {
            throw new CacheOperationException("No session design model. So fail in loading runtime mode. ");
        }
        ReportRuntimeModel runTimeModel = new ReportRuntimeModel(reportId);
        try {
            // 运形态调用，应当并且仅应当初始化一次
            runTimeModel.init(reportModel, true);
            updateRunTimeModelToCache(reportId, runTimeModel);
        } catch (CacheOperationException e) {
            throw e;
        }
        return runTimeModel;
    }
    
    /**
     * 更新缓存中的报表运行模型
     * 
     * @param reportId
     * @param newRunTimeModel
     * @throws Exception
     */
    public void updateRunTimeModelToCache(String reportId, ReportRuntimeModel newRunTimeModel)
            throws CacheOperationException {
        long begin = System.currentTimeMillis ();
        String sessionId = ContextManager.getSessionId();
        String productLine = ContextManager.getProductLine();
        String runTimeKey = CacheKeyGenerator.generateRuntimeReportKey(sessionId, reportId,
            productLine);
        try {
            byte[] modelBytes = SerializationUtils.serialize(newRunTimeModel);
            LOG.info ("current model size : " + (modelBytes.length / 1024.0 / 1024.0) + "m");
            cacheManagerForReource.setToCache(runTimeKey, modelBytes);
        } catch (CacheOperationException e) {
            throw e;
        } 
        LOG.info ("put model into cache cost : " + (System.currentTimeMillis () - begin) + "ms");
    }

    /**
     * 
     * @param resource
     */
    protected void setCacheManagerForResource(CacheManagerForResource resource) {
        this.cacheManagerForReource = resource;
    }
    
    /**
     * 更新区域上下文
     * @param areaId 区域id
     * @param context 区域上下文
     */
    public void updateAreaContext(String reportId, String areaId, ExtendAreaContext context) {
        int key = genAreaKey(reportId, areaId);
        cacheManagerForReource.setToCache(String.valueOf(key), context);
    }

    /**
     * 区域id
     * @param areaId
     * @return int
     */
    private int genAreaKey(String reportId, String areaId) {
        int key = new StringBuilder()
                .append (reportId)
                .append ("_^-^_")
                .append(areaId)
                .append("_^-^_")
                .append(ContextManager.getSessionId())
                .append("_^-^_")
                .append(ContextManager.getProductLine())
                .toString().hashCode();
        return key;
    }
    
    /**
     * 
     * 依据区域id获取区域上下文
     * @param areaId 区域id
     * @return ExtendAreaContext
     * 
     */
    public ExtendAreaContext getAreaContext(String reportId, String areaId) {
        int key = genAreaKey(reportId, areaId);
        long curr = System.currentTimeMillis();
        Object rs = cacheManagerForReource.getFromCache(String.valueOf(key));
        if (rs == null) {
            return new ExtendAreaContext();
        }       
        ExtendAreaContext result = (ExtendAreaContext) rs;
        LOG.info("[INFO]getAreaContext cost:" + (System.currentTimeMillis() - curr)
                + " ms to get ExtendAreaContext from cache");
        return result;
    }
//    
//    /**
//     * 更新全局上下文
//     * @param areaId 区域id
//     * @param context 区域上下文
//     */
//    public void updateContext(String reportId, Context context) {
//        int key = genContextKey(reportId);
//        cacheManagerForReource.setToCache(String.valueOf(key), context);
//    }

//    /**
//     * 全局context的id
//     * @param reportId
//     * @return int
//     */
//    private int genContextKey(String reportId) {
//        int key = new StringBuilder()
//                .append(reportId)
//                .append("_^-^_")
//                .append(ContextManager.getSessionId())
//                .append("_^-^_")
//                .append(ContextManager.getProductLine())
//                .toString().hashCode();
//        return key;
//    }
    
    /**
     * 
     * 依据报表id获取全局上下文
     * @param reportId 全局id
     * @return Context
     * 
     */
//    public Context getContext(String reportId) {
//        int key = genContextKey(reportId);
//        if (cacheManagerForReource.getFromCache(String.valueOf(key)) == null) {
//            ReportDesignModel designModel = this.getReportModel(reportId);
//            return RuntimeEvnUtil.initRuntimeEvn(designModel, null, null);
//        }
//        return (Context) cacheManagerForReource.getFromCache(String.valueOf(key));
//    }
    
}