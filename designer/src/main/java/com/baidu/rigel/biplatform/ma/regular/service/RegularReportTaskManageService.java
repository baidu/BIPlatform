

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

import com.baidu.rigel.biplatform.schedule.bo.ScheduleTaskInfo;

/** 
 * 固定报表任务管理服务  
 * @author yichao.jiang 
 * @version  2015年8月6日 
 * @since jdk 1.8 or after
 */
public interface RegularReportTaskManageService {

    /**
     * 提交任务到调度模块
     * @param task 任务实体
     * @return 任务id
     */
    public String submitRegularTaskToSchedule(ScheduleTaskInfo task);
    
    /**
     * 更新任务到调度模块
     * @param task 任务实体
     * @return 任务id
     */
    public String updateRegularTaskToSchedule(ScheduleTaskInfo task);
    
    /**
     * 删除调度模块中的任务
     * @param task 任务实体
     * @return 任务id
     */
    public String deleteRegularTaskFromSchedule(ScheduleTaskInfo task);
}

