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
package com.baidu.rigel.biplatform.ac.model.callback;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.util.AnswerCoreConstant;
import com.baidu.rigel.biplatform.ac.util.HttpRequest;
import com.google.common.collect.Lists;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * 
 * Description:
 * @author david.wang
 *
 */
public final class CallbackServiceInvoker {
    
    /**
     * LOG
     */
    private static final Logger LOG = LoggerFactory.getLogger(CallbackServiceInvoker.class);
    
    /**
     * 构造函数
     */
    private CallbackServiceInvoker() {
    }
    
    /**
     * 
     * @param url callback请求url
     * @param params callback请求参数
     * @param type callback请求类型
     * @return CallbackResponse callback响应
     */
    public static CallbackResponse invokeCallback(String url, Map<String, String> params, CallbackType type) {
        return invokeCallback(url, params, type, 50000);
    }
    
    /**
     * 
     * @param url callback请求url
     * @param params callback请求参数
     * @param type callback请求类型
     * @param timeOutMillSecond 超时时间
     * @return CallbackResponse callback响应
     */
    public static CallbackResponse invokeCallback(String url, Map<String, String> params,
            CallbackType type, long timeOutMillSecond) {
        long begin = System.currentTimeMillis();
        
        if (timeOutMillSecond <= 0) {
            timeOutMillSecond = 1000;
        }
        params.put(HttpRequest.SOCKET_TIME_OUT, String.valueOf(timeOutMillSecond));
        if (LOG.isDebugEnabled ()) {
            LOG.debug("[INFO] --- --- begin invoke callback service ... ...");
            LOG.debug("[INFO] --- --- request url : {}", url);
            LOG.debug("[INFO] --- --- timeout time : {} ms", timeOutMillSecond);
            LOG.debug("[INFO] --- --- callback type : {}", type.name());
            LOG.debug("[INFO] --- --- end invoke callback service. result is : \r\n");
            LOG.debug("[INFO] -------------------------------------------------------------------------\r\n" );
        }
        try {
            
            StringBuilder logStr = new StringBuilder ();
            params.forEach ((k, v) -> {
                if (!StringUtils.isEmpty (v)) {
                    if (v.length () > 1000) {
                        logStr.append (k + "---" + v.substring (0, 1000) + "...");
                    } else {
                        logStr.append (k + "---" + v);
                    }
                    logStr.append ("\n");
                }
            });
            LOG.info ("[INFO] --- --- --- --- --- --- --- --- --- --- ---- ---- ---- -- --- --- -- - -- -  - -- - - ");
            LOG.info ("[INFO] --- --- request params : \n {}", logStr.toString ());
            LOG.info ("[INFO] --- --- --- --- --- --- --- --- --- --- ---- ---- ---- -- --- --- -- - -- -  - -- - - ");
            String responseStr = HttpRequest.sendPostWithoutRoutee(url, params);
            CallbackResponse response = convertStrToResponse(responseStr, type); 
            if (LOG.isDebugEnabled ()) {
                LOG.debug("[INFO] --- --- resposne : {}", response);
                LOG.debug("[INFO] -------------------------------------------------------------------------\r\n" );
            }
            LOG.info("[INFO] --- --- resposne : {}", response);
            long end = System.currentTimeMillis() - begin;
            LOG.info("[INFO] --- --- invoke callback service cost : " + end + "ms,"
                    + " cost on data transfer : " + (end - response.getCost()) + "ms,"
                    + " callback execute cost : " + response.getCost() + "ms") ;
            return response;
        } catch (Exception e) {
            LOG.error (e.getMessage (), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 将callback请求结果封装为CallbackResponse，如因404等错误信息需cache异常另处理
     * @param responseStr
     * @param type
     * @return CallbackResponse
     */
    private static CallbackResponse convertStrToResponse(String responseStr, CallbackType type) {
//        LOG.info("[INFO] --- --- message received from callback server  is {}", responseStr);
        CallbackResponse rs = new CallbackResponse();
        long begin = System.currentTimeMillis();
        if (StringUtils.isEmpty(responseStr)) {
            throw new RuntimeException("请求响应未满足协议规范");
        }
        JsonObject json = null;
        try {
            json = new JsonParser().parse(responseStr).getAsJsonObject();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("请求响应未满足协议规范,json格式不正确");
        }
        int status = json.get("status").getAsInt();
        String message = json.get("message") == null || json.get("message") == JsonNull.INSTANCE ? 
                "unknown" : json.get("message").getAsString();
        String provider = json.get("provider") == null || json.get("provider") == JsonNull.INSTANCE ?
                "unknown" : json.get("provider").getAsString();
        String cost = json.get("cost") == null || json.get("cost") == JsonNull.INSTANCE ?
                "" :json.get("cost").getAsString();
        String version = json.get("version") == null || json.get("version") == JsonNull.INSTANCE ?
                "unknown" :json.get("version").getAsString();
        if (LOG.isDebugEnabled ()) {
            LOG.debug("[INFO] ------------------------------callback response desc -----------------------------------");
            LOG.debug("[INFO] --- --- status : {}", status);
            LOG.debug("[INFO] --- --- message : {}", message);
            LOG.debug("[INFO] --- --- provider : {}", provider);
            LOG.debug("[INFO] --- --- cost : {}", cost);
            LOG.debug("[INFO] --- --- callback version : {}", version);
            LOG.debug("[INFO] -----------------------------end print response desc -----------------------------------");
        }
        
        LOG.info("[INFO] --- --- package result to CallbackResponse cost {} ms",
                (System.currentTimeMillis() - begin));
        rs.setCost(Integer.valueOf(StringUtils.isEmpty(cost) ? "0" : cost));
        rs.setStatus(getStatus(status));
        rs.setProvider(provider);
        rs.setVersion(version);
        rs.setMessage(getNlsMessage(status));
        if (ResponseStatus.SUCCESS.getValue() == status) {
            rs.setData(getCallbackValue(json.get("data").toString(), type));
        }
        return rs;
    }

    private static ResponseStatus getStatus(int status) {
        for (ResponseStatus tmp : ResponseStatus.values()) {
            if (tmp.getValue() == status) {
                return tmp;
            }
        }
        throw new UnsupportedOperationException("状态码错误：未知状态");
    }

    private static List<CallbackValue> getCallbackValue(String data, CallbackType type) {
        List<CallbackValue> rs = Lists.newArrayList();
        if (StringUtils.isEmpty(data)) {
            return rs;
        }
        switch (type) {
            case DIM:
                return AnswerCoreConstant.GSON.fromJson(data, new TypeToken<List<CallbackDimTreeNode>>(){}.getType());
            case MEASURE:
                return AnswerCoreConstant.GSON.fromJson(data, new TypeToken<List<CallbackMeasureVaue>>(){}.getType());
        }
        throw new IllegalStateException("错误的响应结果");
    }

    /**
     * 获取提示信息
     * @param status
     * @return String
     */
    private static String getNlsMessage(int status) {
        ResponseStatus statusType = getStatus(status);
        // 以后考虑国际化，此处为临时方案
        switch (statusType) {
            case SUCCESS:
                return "成功受理请求";
            case COOKIE_VALUE_IS_NULL:
                return "Cookie未设置正确的值";
            case INTERNAL_SERVER_ERROR:
                return "服务器内部异常";
            case INVALID_PARAM_TYPE:
                return "参数类型错误";
            case INVALIDATE_PARAM_NUM:
                return "参数个数不正确";
            case INVALIDATE_USER_ID:
                return "错误的用户身份信息";
            case MIS_PARAM:
                return "参数丢失";
            case NOT_CONTENT_COOKIE:
                return "未提供cookie信息";
            case NOT_PROVIDE_USER_ID:
                return "未提供用户身份信息";
            case PARAM_NOT_ASSIGN_VALUE:
                return "错误的参数值";
            case UN_AUTH:
                return "未授权";
            case UN_KNOW_SERVICE:
                return "错误的请求地址";
            case UN_SUPPORTED_METHOD:
                return "错误的请求方法";
            case UNKNOW_PARAMS:
                return "未知参数";
            default:
        }
        return "未知错误，请联系系统管理人员";
    }
}
