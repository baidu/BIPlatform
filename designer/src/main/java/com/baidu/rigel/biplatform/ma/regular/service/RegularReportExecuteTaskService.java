

/**
 * Copyright (c) 2015 Baidu, Inc. All Rights Reserved.
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

package com.baidu.rigel.biplatform.ma.regular.service;

/** 
 * 固定报表任务执行服务
 * @author yichao.jiang 
 * @version  2015年7月30日 
 * @since jdk 1.8 or after
 */
public interface RegularReportExecuteTaskService {
    
    /**
     * 执行任务，供Resource层直接调用
     * @param reportId 报表id
     * @param taskId 任务id
     * @param productLine 产品线信息
     * @return 任务执行结果
     */
    public boolean executeTask(String reportId, String taskId, String productLine);
    
    /**
     * 回滚任务，在任务重试一定次数仍然失败时调用
     * @param reportId
     * @param taskId
     * @return 理论上，该方法用于返回true，即回滚永远会成功
     */
    public boolean rollBack(String reportId, String taskId);
}

