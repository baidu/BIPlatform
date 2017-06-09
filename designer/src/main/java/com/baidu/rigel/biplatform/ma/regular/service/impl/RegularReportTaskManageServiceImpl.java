

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

package com.baidu.rigel.biplatform.ma.regular.service.impl;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.util.HttpRequest;
import com.baidu.rigel.biplatform.ma.model.utils.GsonUtils;
import com.baidu.rigel.biplatform.ma.regular.service.RegularReportTaskManageService;
import com.baidu.rigel.biplatform.ma.resource.ResponseResult;
import com.baidu.rigel.biplatform.schedule.bo.ScheduleTaskInfo;
import com.baidu.rigel.biplatform.schedule.bo.TaskActionEnum;
import com.google.common.collect.Maps;

/** 
 * 固定报表任务管理实现类
 * @author yichao.jiang 
 * @version  2015年8月6日 
 * @since jdk 1.8 or after
 */
@Service("regularReportTaskManageService")
public class RegularReportTaskManageServiceImpl implements RegularReportTaskManageService {

    /**
     * 日志对象
     */
    private static final Logger LOG = LoggerFactory.getLogger(RegularReportTaskManageServiceImpl.class);
    
    /**
     * 请求返回值中任务id的key
     */
    private static final String TASK_ID_KEY = "taskId";
    
    /**
     * 调度服务器地址
     */
    @Value("${biplatform.ma.schedule.hostname}")
    private String scheduleHostName = "127.0.0.1";
    
    /**
     * 调度服务器端口
     */
    @Value("${biplatform.ma.schedule.host.port}")
    private String scheduleHostPort = "8090";
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String submitRegularTaskToSchedule(ScheduleTaskInfo task) {
        return this.scheduleRequestTemplate(task, TaskActionEnum.ADD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String updateRegularTaskToSchedule(ScheduleTaskInfo task) {
        return this.scheduleRequestTemplate(task, TaskActionEnum.UPDATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String deleteRegularTaskFromSchedule(ScheduleTaskInfo task) {
        return this.scheduleRequestTemplate(task, TaskActionEnum.DELETE);
    }
    
    /**
     * 调度请求模板
     * @param taskInfo
     * @param actionType
     * @return
     */
    private String scheduleRequestTemplate(ScheduleTaskInfo taskInfo, TaskActionEnum actionType) {
        String requestUrl;
        switch (actionType) {
            case ADD:
                requestUrl = this.getBaseScheduleServiceURL() + "saveTask";
                break;
            case DELETE:
                requestUrl = this.getBaseScheduleServiceURL() + "deleteTask";
                break;
            case UPDATE:
                requestUrl = this.getBaseScheduleServiceURL() + "updateTask";
                break;
            default:
                throw new UnsupportedOperationException("don't supoort [ " + actionType + " ] currently");
        }
        // 将task转为map参数信息
        Map<String, String> params = this.covObject2Map(taskInfo);
        LOG.info("current exeucte action is [ " + actionType + " ], and the taskInfo is {" + taskInfo + "}");
        String result = HttpRequest.sendGet(requestUrl, params);
        ResponseResult rs = GsonUtils.fromJson(result, ResponseResult.class);
        Map<String, String> rsMap = this.covObject2Map(rs.getData());
        LOG.info("the execute info from schedule is " + rsMap);
        if (rsMap == null || rsMap.size() == 0 || !rsMap.containsKey(TASK_ID_KEY) 
                || StringUtils.isEmpty(rsMap.get(TASK_ID_KEY))) {
            return null;
        }
        return rsMap.get(TASK_ID_KEY);
    }
    
    /**
     * 获取schedule服务地址URL
     * @return
     */
    private String getBaseScheduleServiceURL() {
        return "http://" + this.scheduleHostName + ":" + this.scheduleHostPort + "/schedule/task/";
    }
    
    /**
     * 将对象转为map
     * @param obj
     * @return Map<String, String>
     */
    private Map<String, String> covObject2Map(Object obj) {
        try {
            // 如果对象为空，则返回空的map
            if (obj == null) {
                return Maps.newHashMap();
            }
            String jsonStr;
            // 如果是字符串，则直接转为字符串
            if (obj instanceof String) {
                jsonStr = (String) obj;
            } else {
                // 如果是对象，则转为JSON字符串
                jsonStr = GsonUtils.toJson(obj);                
            }
            JSONObject json = new JSONObject(jsonStr);
            Map<String, String> rs = Maps.newHashMap();
            for (String str : JSONObject.getNames(json)) {
                rs.put(str, json.getString(str));
            }
            return rs;
        } catch (JSONException e) {
            LOG.error("can't convert this object 2 map" + obj.toString());
            return Maps.newHashMap();
        }
    }
}

