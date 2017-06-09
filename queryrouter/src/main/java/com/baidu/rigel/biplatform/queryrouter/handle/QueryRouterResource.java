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
package com.baidu.rigel.biplatform.queryrouter.handle;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.baidu.rigel.biplatform.ac.exception.MiniCubeQueryException;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMember;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.query.MiniCubeConnection;
import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.data.vo.MetaJsonDataInfo;
import com.baidu.rigel.biplatform.ac.query.model.ConfigQuestionModel;
import com.baidu.rigel.biplatform.ac.query.model.DimensionCondition;
import com.baidu.rigel.biplatform.ac.query.model.MetaCondition;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.ac.util.AnswerCoreConstant;
import com.baidu.rigel.biplatform.ac.util.JsonUnSeriallizableUtils;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ac.util.ResponseResult;
import com.baidu.rigel.biplatform.ac.util.ResponseResultUtils;
import com.baidu.rigel.biplatform.queryrouter.query.exception.MetaException;
import com.baidu.rigel.biplatform.queryrouter.query.service.MetaDataService;
import com.baidu.rigel.biplatform.queryrouter.query.service.QueryContextBuilder;
import com.baidu.rigel.biplatform.queryrouter.query.service.QueryService;
import com.google.gson.JsonSyntaxException;

/**
 * queryrouter对外接口
 * 
 * @author luowenlei
 * 
 *         2015-05-07
 */
/**
 * 类QueryRouterResource.java的实现描述：TODO 类实现描述
 * 
 * @author luowenlei 2015年12月28日 下午3:41:04
 */
@RestController
@RequestMapping("/queryrouter")
@Service
public class QueryRouterResource {
    
    /**
     * logger
     */
    private static Logger logger = LoggerFactory.getLogger(QueryRouterResource.class);
    
    /**
     * log4j最长打印字符串长度
     */
    private static final int MAX_PRINT_LENGTH = 5000;
    
    /**
     * 出参参的参数“成功”
     */
    private static final String SUCCESS = "success";
    
    /**
     * 入参参的参数“问题模型参数”
     */
    private static final String PRAMA_QUESTION = "question";
    
    /**
     * metaDataService
     */
    @Resource
    private MetaDataService metaDataService;
    
    /**
     * 查询一个报表中，某个区域的数据
     * 
     * @param reportId
     * @param areaId
     * @param request
     * @return
     */
    @RequestMapping(value = "/query", method = { RequestMethod.POST })
    public ResponseResult query(HttpServletRequest request) {
        long begin = System.currentTimeMillis();
        String question = request.getParameterMap().get(PRAMA_QUESTION)[0].toString();
        if (question == null) {
            return ResponseResultUtils.getErrorResult("question is null", 100);
        }
        // convert json to QuestionModel
        ConfigQuestionModel questionModel = AnswerCoreConstant.GSON.fromJson(question,
                ConfigQuestionModel.class);
        questionModel.setQueryId(questionModel.getDataSourceInfo().getProductLine() + "-"
                + questionModel.getQueryId());
        QueryRouterContext.setQueryInfo(questionModel.getQueryId());
        logger.info("queryId:{} query current handle size:{}, begin handle questionmodel json:{}",
                questionModel.getQueryId(), QueryRouterContext.getQueryCurrentHandleSize(),
                question);
        // get DataModel
        try {
            QueryService queryService = QueryServiceFactory.getQueryService(questionModel
                    .getClass());
            DataModel dataModel = queryService.query(questionModel, null);
            if (dataModel == null) {
                return ResponseResultUtils.getErrorResult("tesseract occur an error", 1);
            }
            String dataModelJson = AnswerCoreConstant.GSON.toJson(dataModel);
            // 限制日志输出
            if (dataModelJson.length() > MAX_PRINT_LENGTH) {
                logger.info("queryId:{} response modeldata json:{}...", questionModel.getQueryId(),
                        dataModelJson.substring(0, MAX_PRINT_LENGTH));
            } else {
                logger.info("queryId:{} response modeldata json:{}", questionModel.getQueryId(),
                        dataModelJson);
            }
            logger.info("queryId:{} response query toal cost:{} ms", questionModel.getQueryId(),
                    System.currentTimeMillis() - begin);
            return ResponseResultUtils.getCorrectResult(SUCCESS, dataModelJson);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("queryId:{} error msg:{}", questionModel.getQueryId(), e.getMessage());
            // 说明模型参数传入有问题
            return ResponseResultUtils.getErrorResult(
                    "question model exception, questionmodel is incorrect." + "reason:"
                            + e.getMessage(), 100);
        } finally {
            logger.info("queryId:{} query current handle size:{} , end to handle this queryId.",
                    questionModel.getQueryId(), QueryRouterContext.getQueryCurrentHandleSize());
            QueryRouterContext.removeQueryInfo();
        }
    }
    
    @RequestMapping(value = "/meta/getMembers", method = { RequestMethod.POST })
    public ResponseResult getMembers(@RequestBody String requestJson) {
        long start = System.currentTimeMillis();
        // 将请求信息全部JSON化，需要
        if (StringUtils.isBlank(requestJson)) {
            return ResponseResultUtils.getErrorResult("get members question is null", 100);
        }
        List<MiniCubeMember> members;
        String errorMsg = null;
        try {
            Map<String, String> requestParams = parseRequestJson(requestJson);
            
            ConfigQuestionModel questionModel = AnswerCoreConstant.GSON.fromJson(
                    requestParams.get(MiniCubeConnection.QUESTIONMODEL_PARAM_KEY),
                    ConfigQuestionModel.class);
            QueryRouterContext.setQueryInfo(questionModel.getQueryId());
            setMDCContext(questionModel.getRequestParams().get("_flag"));
            members = null;
            // 普通查询，认为查询相关信息在queryCondition中
            String dimName = null;
            String levelName = null;
            if (MapUtils.isNotEmpty(questionModel.getQueryConditions())) {
                // 这种肯定只有一个，如果超过一个，忽略
                dimName = questionModel.getQueryConditions().keySet().toArray(new String[] {})[0];
                // 转换失败，
                MetaCondition dimConfition = questionModel.getQueryConditions().get(dimName);
                if (dimConfition instanceof DimensionCondition) {
                    // 查询level的members的时候，查询条件的UniqueName存放的是level的name
                    levelName = ((DimensionCondition) dimConfition).getQueryDataNodes().get(0)
                            .getUniqueName();
                } else {
                    errorMsg = "meta condition is illegal";
                }
                DataSourceInfo dataSourceInfo = questionModel.getDataSourceInfo();
                Cube cube = questionModel.getCube();
                JsonUnSeriallizableUtils.fillCubeInfo(cube);
                levelName = MetaNameUtil.parseUnique2NameArray(levelName)[1];
                members = metaDataService.getMembers(dataSourceInfo, cube, dimName, levelName,
                        questionModel.getRequestParams());
                List<MetaJsonDataInfo> metaJsons = new ArrayList<MetaJsonDataInfo>(members.size());
                if (CollectionUtils.isNotEmpty(members)) {
                    for (MiniCubeMember member : members) {
                        metaJsons.add(JsonUnSeriallizableUtils.parseMember2MetaJson(member));
                    }
                }
                StringBuilder sb = new StringBuilder();
                sb.append("get ").append(members.size()).append(" members from dimension:")
                        .append(dimName).append(" in level:").append(levelName);
                logger.info("queryId:{} cost:{}ms get members finished. data:{}",
                        QueryRouterContext.getQueryId(), System.currentTimeMillis() - start,
                        sb.toString());
                return ResponseResultUtils.getCorrectResult("query success.",
                        AnswerCoreConstant.GSON.toJson(metaJsons));
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            // 一般不会出现，出现了说明模型有问题了
            errorMsg = "json syntax exception:" + e.getMessage();
        } catch (MiniCubeQueryException e) {
            e.printStackTrace();
            errorMsg = "query members error:" + e.getMessage();
        } catch (MetaException e) {
            e.printStackTrace();
            errorMsg = "meta is illegal," + e.getMessage();
        }
        // 走到这里说明已经出错了，状态码暂时设为100，后续加个状态码表
        logger.info("queryId:{} cost:{}ms get members finished. errorMsg:{}",
                QueryRouterContext.getQueryId(), System.currentTimeMillis() - start, errorMsg);
        return ResponseResultUtils.getErrorResult(errorMsg, 100);
    }
    
    @RequestMapping(value = "/meta/getChildren", method = { RequestMethod.POST })
    // @ResponseBody
    public ResponseResult getChildren(@RequestBody String requestJson) {
        long start = System.currentTimeMillis();
        // 将请求信息全部JSON化，需要
        if (StringUtils.isBlank(requestJson)) {
            return ResponseResultUtils.getErrorResult("get members question is null", 100);
        }
        List<MiniCubeMember> children;
        String errorMsg = null;
        try {
            Map<String, String> requestParams = parseRequestJson(requestJson);
            
            ConfigQuestionModel questionModel = AnswerCoreConstant.GSON.fromJson(
                    requestParams.get(MiniCubeConnection.QUESTIONMODEL_PARAM_KEY),
                    ConfigQuestionModel.class);
            QueryRouterContext.setQueryInfo(questionModel.getQueryId());
            setMDCContext(questionModel.getRequestParams().get("_flag"));
            children = null;
            // 普通查询，认为查询相关信息在queryCondition中
            String uniqueName = null;
            String metaName = null;
            if (MapUtils.isNotEmpty(questionModel.getQueryConditions())) {
                // 这种肯定只有一个，如果超过一个，忽略
                metaName = questionModel.getQueryConditions().keySet().toArray(new String[] {})[0];
                // 转换失败，
                MetaCondition dimConfition = questionModel.getQueryConditions().get(metaName);
                if (dimConfition instanceof DimensionCondition) {
                    uniqueName = ((DimensionCondition) dimConfition).getQueryDataNodes().get(0)
                            .getUniqueName();
                } else {
                    errorMsg = "meta condition is illegal";
                }
                DataSourceInfo dataSourceInfo = questionModel.getDataSourceInfo();
                Cube cube = questionModel.getCube();
                JsonUnSeriallizableUtils.fillCubeInfo(cube);
                
                children = metaDataService.getChildren(dataSourceInfo, cube, uniqueName,
                        QueryContextBuilder.getRequestParams(questionModel, cube));
                if (CollectionUtils.isNotEmpty(children)) {
                    List<MetaJsonDataInfo> metaJsons = new ArrayList<MetaJsonDataInfo>(
                            children.size());
                    for (MiniCubeMember member : children) {
                        metaJsons.add(JsonUnSeriallizableUtils.parseMember2MetaJson(member));
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append("get ").append(children.size()).append(" children from dimension:")
                            .append(metaName).append(" by uniqueName:").append(uniqueName);
                    logger.info("queryId:{} cost:{}ms get children finished. data:{}",
                            QueryRouterContext.getQueryId(), System.currentTimeMillis() - start,
                            sb.toString());
                    return ResponseResultUtils.getCorrectResult("query success.",
                            AnswerCoreConstant.GSON.toJson(metaJsons));
                }
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            // 一般不会出现，出现了说明模型有问题了
            errorMsg = "json syntax exception:" + e.getMessage();
        } catch (MiniCubeQueryException e) {
            e.printStackTrace();
            errorMsg = "query children error:" + e.getMessage();
        } catch (MetaException e) {
            e.printStackTrace();
            errorMsg = "meta is illegal," + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            errorMsg = "server error,msg" + e.getMessage();
        }
        // 走到这里说明已经出错了，状态码暂时设为100，后续加个状态码表
        logger.info("queryId:{} cost:{}ms get children finished. errorMsg:{}",
                QueryRouterContext.getQueryId(), System.currentTimeMillis() - start, errorMsg);
        return ResponseResultUtils.getErrorResult(errorMsg, 100);
    }
    
    @RequestMapping(value = "/lookUp", method = { RequestMethod.POST })
    @ResponseBody
    public ResponseResult lookUp(@RequestBody String requestJson) {
        long start = System.currentTimeMillis();
        String errorMsg = null;
        try {
            if (StringUtils.isBlank(requestJson)) {
                return ResponseResultUtils.getErrorResult("get members question is null", 100);
            }
            
            Map<String, String> requestParams = parseRequestJson(requestJson);
            
            ConfigQuestionModel questionModel = AnswerCoreConstant.GSON.fromJson(
                    requestParams.get(MiniCubeConnection.QUESTIONMODEL_PARAM_KEY),
                    ConfigQuestionModel.class);
            QueryRouterContext.setQueryInfo(questionModel.getQueryId());
            setMDCContext(questionModel.getRequestParams().get("_flag"));
            MetaCondition uniqueNameCondition = questionModel.getQueryConditions().get(
                    MiniCubeConnection.UNIQUENAME_PARAM_KEY);
            
            if (uniqueNameCondition != null && uniqueNameCondition instanceof DimensionCondition) {
                DimensionCondition dimCondition = (DimensionCondition) uniqueNameCondition;
                String nodeUniqueName = dimCondition.getQueryDataNodes().get(0).getUniqueName();
                String uniqueName = CollectionUtils
                        .isNotEmpty(dimCondition.getQueryDataNodes()) ? nodeUniqueName : null;
                Cube cube = questionModel.getCube();
                MiniCubeMember member = metaDataService
                        .lookUp(questionModel.getDataSourceInfo(), cube, uniqueName,
                                QueryContextBuilder.getRequestParams(questionModel, cube));
                logger.info("queryId:{} cost:{}ms lookUp finished. return member name:{}",
                        QueryRouterContext.getQueryId(), System.currentTimeMillis() - start,
                        member.getName());
                return ResponseResultUtils.getCorrectResult("return member:" + member.getName(),
                        JsonUnSeriallizableUtils.parseMember2MetaJson(member));
            }
            errorMsg = "can not get uniquename info from questionmodel:"
                    + requestParams.get(MiniCubeConnection.QUESTIONMODEL_PARAM_KEY);
            
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            errorMsg = "json parse error," + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            errorMsg = "unexpected error:" + e.getMessage();
        }
        logger.info("queryId:{} cost:{}ms lookUp finished. errorMsg:{}",
                QueryRouterContext.getQueryId(), System.currentTimeMillis() - start, errorMsg);
        // 走到这里说明已经出错了，状态码暂时设为100，后续加个状态码表
        return ResponseResultUtils.getErrorResult(errorMsg, 100);
    }
    
    /**
     * 将传入的questionModel通过dispatch后分发到相应的Plugin，然后转换成DataModel对象,下载中心查询用
     * 
     * @param questionStr
     *            request questionStr
     * @return DataModel DataModel
     */
    public DataModel queryAndLog(QuestionModel questionModel) {
        long begin = System.currentTimeMillis();
        QueryRouterContext.setQueryInfo(questionModel.getQueryId());
        logger.info("queryId:{} query current handle size:{} , begin to handle this queryId.",
                questionModel.getQueryId(), QueryRouterContext.getQueryCurrentHandleSize());
        try {
            long beginGet = System.currentTimeMillis();
            QueryService queryService = QueryServiceFactory.getQueryService(questionModel
                    .getClass());
            DataModel dataModel = queryService.query(questionModel, null);
            logger.info("queryId:{} getQueryPlugin cost:{}", questionModel.getQueryId(),
                    System.currentTimeMillis() - beginGet);
            logger.info("queryId:{} response query toal cost:{} ms", questionModel.getQueryId(),
                    System.currentTimeMillis() - begin);
            return dataModel;
        } catch (Exception e) {
            logger.error("queryId:{} occur error, cost:{}ms, cause:{}", questionModel.getQueryId(),
                    System.currentTimeMillis() - begin, e.getCause().getMessage());
            return null;
        } finally {
            logger.info("queryId:{} query current handle size:{} , end to handle this queryId.",
                    questionModel.getQueryId(), QueryRouterContext.getQueryCurrentHandleSize());
            QueryRouterContext.removeQueryInfo();
        }
    }
    
    /**
     * 判断服务是否存活
     * 
     * @param request
     * @return ResponseResult status string
     */
    @RequestMapping(value = "/alive", method = { RequestMethod.GET })
    public ResponseResult checkAlive(HttpServletRequest request) {
        return ResponseResultUtils.getCorrectResult("OK", "OK");
    }
    
    private void setMDCContext(String value) {
        if (StringUtils.isNotBlank(value)) {
            MDC.put("REQUESTFLAG", value);
        }
    }
    
    public static Map<String, String> parseRequestJson(String requestJson) {
        String[] requestArray = requestJson.split("&");
        Map<String, String> requestParams = new HashMap<String, String>();
        for (String request : requestArray) {
            String[] keyValue = request.split("=");
            String value = keyValue[1];
            try {
                value = URLDecoder.decode(value, "utf-8");
            } catch (UnsupportedEncodingException e) {
                logger.warn("queryId:{} decode value:" + value + " error:{}",
                        QueryRouterContext.getQueryId(), e);
            }
            requestParams.put(keyValue[0], value);
        }
        return requestParams;
    }
}
