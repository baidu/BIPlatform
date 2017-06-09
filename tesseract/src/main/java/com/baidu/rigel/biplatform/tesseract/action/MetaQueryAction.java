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
package com.baidu.rigel.biplatform.tesseract.action;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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
import com.baidu.rigel.biplatform.ac.query.data.HeadField;
import com.baidu.rigel.biplatform.ac.query.data.vo.MetaJsonDataInfo;
import com.baidu.rigel.biplatform.ac.query.model.ConfigQuestionModel;
import com.baidu.rigel.biplatform.ac.query.model.DimensionCondition;
import com.baidu.rigel.biplatform.ac.query.model.MetaCondition;
import com.baidu.rigel.biplatform.ac.query.model.SortRecord;
import com.baidu.rigel.biplatform.ac.util.AnswerCoreConstant;
import com.baidu.rigel.biplatform.ac.util.DataModelUtils;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.ac.util.JsonUnSeriallizableUtils;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ac.util.ResponseResult;
import com.baidu.rigel.biplatform.ac.util.ResponseResultUtils;
import com.baidu.rigel.biplatform.tesseract.datasource.DataSourcePoolService;
import com.baidu.rigel.biplatform.tesseract.exception.DataSourceException;
import com.baidu.rigel.biplatform.tesseract.exception.MetaException;
import com.baidu.rigel.biplatform.tesseract.meta.MetaDataService;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.QueryContextBuilder;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.QueryContextSplitService.QueryContextSplitStrategy;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.QueryService;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryContext;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryRequest;
import com.baidu.rigel.biplatform.tesseract.util.TesseractConstant;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;



/**
 * 元数据查询相关接口，包括取维度的members和children
 * 
 * @author xiaoming.chen
 *
 */

@RestController
public class MetaQueryAction {
    
    private static Logger LOG = LoggerFactory.getLogger(MetaQueryAction.class);

    @Resource
    private MetaDataService metaDataService;

    @Resource
    private QueryService queryService;

    @Resource
    private DataSourcePoolService dataSourcePoolService;

    @RequestMapping(value = "/meta/getMembers", method = RequestMethod.POST)
//    @ResponseBody
    public ResponseResult getMembers(@RequestBody String requestJson) {
        // 将请求信息全部JSON化，需要
        if (StringUtils.isBlank(requestJson)) {
            return ResponseResultUtils.getErrorResult("get members question is null", 100);
        }
        List<MiniCubeMember> members;
        String errorMsg = null;
        try {
            Map<String, String> requestParams = parseRequestJson(requestJson);

            ConfigQuestionModel questionModel =
                    AnswerCoreConstant.GSON.fromJson(requestParams.get(MiniCubeConnection.QUESTIONMODEL_PARAM_KEY),
                            ConfigQuestionModel.class);
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
                    levelName = ((DimensionCondition) dimConfition).getQueryDataNodes().get(0).getUniqueName();
                } else {
                    errorMsg = "meta condition is illegal";
                }
                DataSourceInfo dataSourceInfo = questionModel.getDataSourceInfo();
                if (dataSourceInfo == null) {
                    dataSourceInfo = dataSourcePoolService.getDataSourceInfo(questionModel.getDataSourceInfoKey());
                }
                Cube cube = questionModel.getCube();
                if (cube == null) {
                    cube = metaDataService.getCube(questionModel.getCubeId());
                }
                JsonUnSeriallizableUtils.fillCubeInfo(cube);
                levelName = MetaNameUtil.parseUnique2NameArray(levelName)[1];

                members =
                        metaDataService.getMembers(dataSourceInfo, cube, dimName, levelName,
                                questionModel.getRequestParams());
                List<MetaJsonDataInfo> metaJsons = new ArrayList<MetaJsonDataInfo>(members.size());
                if (CollectionUtils.isNotEmpty(members)) {
                    for (MiniCubeMember member : members) {
                        metaJsons.add(JsonUnSeriallizableUtils.parseMember2MetaJson(member));
                    }
                }
                StringBuilder sb = new StringBuilder();
                sb.append("get ").append(members.size()).append(" members from dimension:").append(dimName)
                        .append(" in level:").append(levelName);
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
        return ResponseResultUtils.getErrorResult(errorMsg, 100);
    }

    

    @RequestMapping(value = "/meta/getChildren", method = RequestMethod.POST)
//    @ResponseBody
    public ResponseResult getChildren(@RequestBody String requestJson) {
        // 将请求信息全部JSON化，需要
        if (StringUtils.isBlank(requestJson)) {
            return ResponseResultUtils.getErrorResult("get members question is null", 100);
        }
        List<MiniCubeMember> children;
        String errorMsg = null;
        try {
            Map<String, String> requestParams = parseRequestJson(requestJson);

            ConfigQuestionModel questionModel =
                    AnswerCoreConstant.GSON.fromJson(requestParams.get(MiniCubeConnection.QUESTIONMODEL_PARAM_KEY),
                            ConfigQuestionModel.class);
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
                    uniqueName = ((DimensionCondition) dimConfition).getQueryDataNodes().get(0).getUniqueName();
                } else {
                    errorMsg = "meta condition is illegal";
                }
                DataSourceInfo dataSourceInfo = questionModel.getDataSourceInfo();
                if (dataSourceInfo == null) {
                    dataSourceInfo = dataSourcePoolService.getDataSourceInfo(questionModel.getDataSourceInfoKey());
                }
                Cube cube = questionModel.getCube();
                if (cube == null) {
                    cube = metaDataService.getCube(questionModel.getCubeId());
                }
                JsonUnSeriallizableUtils.fillCubeInfo(cube);

                children =
                            metaDataService.getChildren(dataSourceInfo, cube, uniqueName, 
                            QueryContextBuilder.getRequestParams(questionModel, cube));
                if (CollectionUtils.isNotEmpty(children)) {
                    List<MetaJsonDataInfo> metaJsons = new ArrayList<MetaJsonDataInfo>(children.size());
                    for (MiniCubeMember member : children) {
                        metaJsons.add(JsonUnSeriallizableUtils.parseMember2MetaJson(member));
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append("get ").append(children.size()).append(" children from dimension:").append(metaName)
                            .append(" by uniqueName:").append(uniqueName);
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
        return ResponseResultUtils.getErrorResult(errorMsg, 100);
    }

    @RequestMapping(value = "/query", method = RequestMethod.POST)
//    @ResponseBody
    public ResponseResult query(@RequestBody String requestJson) {
        long current = System.currentTimeMillis();
        // 将请求信息全部JSON化，需要
        if (StringUtils.isBlank(requestJson)) {
            return ResponseResultUtils.getErrorResult("get members question is null", 100);
        }
        String errorMsg = null;
        try {
            Map<String, String> requestParams = parseRequestJson(requestJson);
            
            ConfigQuestionModel questionModel =
                    AnswerCoreConstant.GSON.fromJson(requestParams.get(MiniCubeConnection.QUESTIONMODEL_PARAM_KEY),
                            ConfigQuestionModel.class);
            String queryId = questionModel.getQueryId();
            setMDCContext(questionModel.getRequestParams().get("_flag"));
            DataSourceInfo dataSourceInfo = questionModel.getDataSourceInfo();
            if (dataSourceInfo == null) {
                dataSourceInfo = dataSourcePoolService.getDataSourceInfo(questionModel.getDataSourceInfoKey());
            }
            Cube cube = questionModel.getCube();
            if (cube == null) {
                cube = metaDataService.getCube(questionModel.getCubeId());
            }
            JsonUnSeriallizableUtils.fillCubeInfo(cube);
            questionModel.setDataSourceInfo(dataSourceInfo);
            questionModel.setCube(cube);
            QueryContext queryContext = null;
            QueryContextSplitStrategy preSplitStrategy = null;
            // 拆分的查询会有查询上下文和当前拆分策略参数
            if (requestParams.containsKey(MiniCubeConnection.QUERYCONTEXT_PARAM_KEY)) {
                queryContext =
                        AnswerCoreConstant.GSON.fromJson(requestParams.get(MiniCubeConnection.QUERYCONTEXT_PARAM_KEY),
                                QueryContext.class);
            }
            if (requestParams.containsKey(MiniCubeConnection.SPLITSTRATEGY_PARAM_KEY)) {
                preSplitStrategy =
                        AnswerCoreConstant.GSON.fromJson(requestParams.get(MiniCubeConnection.SPLITSTRATEGY_PARAM_KEY),
                                QueryContextSplitStrategy.class);
            }
            LOG.info("lijinquery cost:" + (System.currentTimeMillis() - current) + " prepare to execute query.");
            
            long beforeQuery = System.currentTimeMillis();
            
            DataModel dataModel = queryService.query(questionModel, queryContext, preSplitStrategy);
            
            LOG.info("lijinquery cost:" + (System.currentTimeMillis() - beforeQuery) + " to execute query.");
            
            long curr=System.currentTimeMillis();
            if (dataModel != null) {
                if (questionModel.isFilterBlank()) {
                    DataModelUtils.filterBlankRow(dataModel);
                    LOG.info("query cost:" + (System.currentTimeMillis() - curr) + " filterBlankRow.");
                }
                curr=System.currentTimeMillis();
                dataModel = sortAndTrunc(dataModel, questionModel.getSortRecord(), 
                    questionModel.getRequestParams().get(TesseractConstant.NEED_OTHERS));
                LOG.info("query cost:" + (System.currentTimeMillis() - curr) + "ms sortAandTrunc.");
            }
            
            LOG.info("lijinquery cost:" + (System.currentTimeMillis() - current) + " success to execute query.");
            curr = System.currentTimeMillis();
            ResponseResult rs = ResponseResultUtils.getCorrectResult("query success.",
                    AnswerCoreConstant.GSON.toJson(dataModel));
            LOG.info("query queryId:{} cost:{} ms convert dataModel to json", queryId,
                    System.currentTimeMillis() - curr);
            return rs;
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            // 一般不会出现，出现了说明模型有问题了
            errorMsg = "json syntax exception:" + e.getMessage();
        } catch (MiniCubeQueryException e) {
            e.printStackTrace();
            errorMsg = "query error:" + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            errorMsg = "unexpected error:" + e.getMessage();
        } 
        LOG.error("cost:" + (System.currentTimeMillis() - current) + " error,errorMsg:" + errorMsg);
        // 走到这里说明已经出错了，状态码暂时设为100，后续加个状态码表
        return ResponseResultUtils.getErrorResult(errorMsg, 100);
    }
    
    @RequestMapping(value = "/queryIndex", method = RequestMethod.POST)
    public ResponseResult queryIndex(@RequestBody String requestJson) {
        long current = System.currentTimeMillis();
        // 将请求信息全部JSON化，需要
        if (StringUtils.isBlank(requestJson)) {
            return ResponseResultUtils.getErrorResult("get members question is null", 100);
        }
        String errorMsg = null;
        try {
            Map<String, String> requestParams = parseRequestJson(requestJson);
            
            ConfigQuestionModel questionModel = AnswerCoreConstant.GSON.fromJson(
                    requestParams.get(MiniCubeConnection.QUESTIONMODEL_PARAM_KEY),
                    ConfigQuestionModel.class);
            String queryId = questionModel.getQueryId();
            QueryContext queryContext = AnswerCoreConstant.GSON.fromJson(
                    requestParams.get(MiniCubeConnection.QUERYCONTEXT_PARAM_KEY),
                    QueryContext.class);
            long beforeQuery = System.currentTimeMillis();
            
            DataModel dataModel = queryService.queryIndex(questionModel, queryContext);
            
            LOG.info("lijinquery cost:" + (System.currentTimeMillis() - beforeQuery)
                    + " to execute query.");
            
            long curr = System.currentTimeMillis();

            LOG.info("lijinquery cost:" + (System.currentTimeMillis() - current)
                    + " success to execute query.");
            curr = System.currentTimeMillis();
            ResponseResult rs = ResponseResultUtils.getCorrectResult("query success.",
                    AnswerCoreConstant.GSON.toJson(dataModel));
            LOG.info("query queryId:{} cost:{} ms convert dataModel to json", queryId,
                    System.currentTimeMillis() - curr);
            return rs;
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            // 一般不会出现，出现了说明模型有问题了
            errorMsg = "json syntax exception:" + e.getMessage();
        } catch (MiniCubeQueryException e) {
            e.printStackTrace();
            errorMsg = "query error:" + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            errorMsg = "unexpected error:" + e.getMessage();
        }
        LOG.error("cost:" + (System.currentTimeMillis() - current) + " error,errorMsg:" + errorMsg);
        // 走到这里说明已经出错了，状态码暂时设为100，后续加个状态码表
        return ResponseResultUtils.getErrorResult(errorMsg, 100);
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
                LOG.warn("decode value:" + value + " error", e);
            }
            requestParams.put(keyValue[0], value);
        }
        return requestParams;
    }

    @RequestMapping(value = "/publish", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult publish(@RequestBody String requestJson) {
        String errorMsg = null;
        try {
            if (StringUtils.isBlank(requestJson)) {
                return ResponseResultUtils.getErrorResult("get members question is null", 100);
            }

            Map<String, String> requestParams = parseRequestJson(requestJson);

            String listCubeJson = requestParams.get(MiniCubeConnection.CUBE_PARAM_KEY);
            String dataSourceJson = requestParams.get(MiniCubeConnection.DATASOURCEINFO_PARAM_KEY);
            List<Cube> cubes = AnswerCoreConstant.GSON.fromJson(listCubeJson, new TypeToken<List<Cube>>() {
                }.getType());
            List<DataSourceInfo> dataSourceInfoList = AnswerCoreConstant.GSON.fromJson(
                    dataSourceJson, new TypeToken<List<DataSourceInfo>>() {
                    }.getType());
            metaDataService.publish(cubes, dataSourceInfoList);
            return ResponseResultUtils.getCorrectResult("success", "public cubes success.");
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            errorMsg = "json parse error," + e.getMessage();
        } catch (DataSourceException e) {
            e.printStackTrace();
            errorMsg = "init datasource error." + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            errorMsg = "unexpected error:" + e.getMessage();
        }

        // 走到这里说明已经出错了，状态码暂时设为100，后续加个状态码表
        return ResponseResultUtils.getErrorResult(errorMsg, 100);
    }

    @RequestMapping(value = "/lookUp", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult lookUp(@RequestBody String requestJson) {
        String errorMsg = null;
        try {
            if (StringUtils.isBlank(requestJson)) {
                return ResponseResultUtils.getErrorResult("get members question is null", 100);
            }

            Map<String, String> requestParams = parseRequestJson(requestJson);

            ConfigQuestionModel questionModel =
                    AnswerCoreConstant.GSON.fromJson(requestParams.get(MiniCubeConnection.QUESTIONMODEL_PARAM_KEY),
                            ConfigQuestionModel.class);
            setMDCContext(questionModel.getRequestParams().get("_flag"));
            MetaCondition uniqueNameCondition =
                    questionModel.getQueryConditions().get(MiniCubeConnection.UNIQUENAME_PARAM_KEY);

            if (uniqueNameCondition != null && uniqueNameCondition instanceof DimensionCondition) {
                DimensionCondition dimCondition = (DimensionCondition) uniqueNameCondition;
                String uniqueName =
                        CollectionUtils.isNotEmpty(dimCondition.getQueryDataNodes()) ? dimCondition.getQueryDataNodes()
                                .get(0).getUniqueName() : null;
                Cube cube = questionModel.getCube();
                MiniCubeMember member =
                        metaDataService.lookUp(questionModel.getDataSourceInfo(), cube, uniqueName,
                                QueryContextBuilder.getRequestParams(questionModel, cube));

                return ResponseResultUtils.getCorrectResult("return member:" + member.getName(),
                        JsonUnSeriallizableUtils.parseMember2MetaJson(member));
            }
            errorMsg =
                    "can not get uniquename info from questionmodel:"
                            + requestParams.get(MiniCubeConnection.QUESTIONMODEL_PARAM_KEY);

        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            errorMsg = "json parse error," + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            errorMsg = "unexpected error:" + e.getMessage();
        }

        // 走到这里说明已经出错了，状态码暂时设为100，后续加个状态码表
        return ResponseResultUtils.getErrorResult(errorMsg, 100);
    }

    @RequestMapping(value = "/refresh", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult refresh(@RequestBody String requestJson) {
        // 将请求信息全部JSON化，需要
        String errorMsg = null;
        try {
            if (StringUtils.isBlank(requestJson)) {
                return ResponseResultUtils.getErrorResult("get members question is null", 100);
            }

            Map<String, String> requestParams = parseRequestJson(requestJson);

            String dataSetString = requestParams.get(MiniCubeConnection.DATASET_PARAM_KEY);
            String dataSourceJson = requestParams.get(MiniCubeConnection.DATASOURCEINFO_PARAM_KEY);
            String paramsJason=requestParams.get(MiniCubeConnection.PARAMS);
            
            Map<String,Map<String,BigDecimal>> params=AnswerCoreConstant.GSON.fromJson(paramsJason,new TypeToken<Map<String,Map<String,BigDecimal>>>() {}.getType());
//            DataSourceInfo dataSourceInfo = AnswerCoreConstant.GSON.fromJson(dataSourceJson, DataSourceInfo.class);
            List<DataSourceInfo> dataSourceInfoList = AnswerCoreConstant.GSON.fromJson(
                    dataSourceJson, new TypeToken<List<DataSourceInfo>>() {
                    }.getType());
            
            metaDataService.refresh(dataSourceInfoList, dataSetString, params);
            
            return ResponseResultUtils.getCorrectResult("success", "refresh success.");
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            errorMsg = "json parse error," + e.getMessage();
        } catch (DataSourceException e) {
            e.printStackTrace();
            errorMsg = "init datasource error." + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            errorMsg = "unexpected error:" + e.getMessage();
        }

        // 走到这里说明已经出错了，状态码暂时设为100，后续加个状态码表
        return ResponseResultUtils.getErrorResult(errorMsg, 100);

    }
    
    @RequestMapping(value = "/hasindex", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult hasIndex(@RequestBody String requestJson) {
        // 将请求信息全部JSON化，需要
        String errorMsg = null;
        try {
            if (StringUtils.isBlank(requestJson)) {
                return ResponseResultUtils.getErrorResult("hasIndex request is null", 100);
            }
            
            Map<String, String> requestParams = parseRequestJson(requestJson);
            
            QueryRequest query = AnswerCoreConstant.GSON.fromJson(
                    requestParams.get(MiniCubeConnection.QUERYREQUEST_PARAM_KEY), QueryRequest.class); 
            
            return ResponseResultUtils.getCorrectResult("success", this.queryService.hasIndex(query));
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            errorMsg = "json parse error," + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            errorMsg = "unexpected error:" + e.getMessage();
        }
        
        // 走到这里说明已经出错了，状态码暂时设为100，后续加个状态码表
        return ResponseResultUtils.getErrorResult(errorMsg, 100);
        
    }

    /**
     * 排序并截断结果集，默认显示500条纪录
     * @param result
     * @param sortRecord
     * @param needOthers 
     * @return DataModel
     */
    private DataModel sortAndTrunc(DataModel result, SortRecord sortRecord, String needOthers) {
        if (sortRecord != null) {
            DataModelUtils.sortDataModelBySort(result, sortRecord);
        }
        if (sortRecord == null) {
            return result;
        }
//            int recordSize = sortRecord == null ? 500 : sortRecord.getRecordSize();
        // 二八原则进行统计计算 
        if (TesseractConstant.NEED_OTHERS_VALUE.equals(needOthers)) {
            //TODO 此处先简化计算，由于图形会走此处逻辑，并且图形不包含汇总合集数据 后续考虑处理包含汇总合集的情况
            return tonNSetting4Chart(result, sortRecord);
        }
        return DataModelUtils.truncModel(result, sortRecord.getRecordSize()); 
    }
    
    private void setMDCContext(String value) {
        if(StringUtils.isNotBlank(value)) {
            MDC.put("REQUESTFLAG", value);
        }
    }

    /**
     * 
     * @param result
     * @param sortRecord
     * @return DataModel
     */
    private DataModel tonNSetting4Chart(DataModel result, SortRecord sortRecord) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal tmp : result.getColumnBaseData().get(0)) {
            //TODO 如果是回调指标，这里需要如何处理？？？？
            if (tmp == null) {
                continue;
            }
            sum = sum.add(tmp);
        }
        // 此处采用默认计算
        result = DataModelUtils.truncModel(result, sortRecord.getRecordSize() - 1); 
        BigDecimal sum1 = BigDecimal.ZERO;
        for (BigDecimal tmp : result.getColumnBaseData().get(0)) {
            //TODO 如果是回调指标，这里需要如何处理？？？？
            if (tmp == null) {
                continue;
            }
            sum1 = sum1.add(tmp);
        }
        BigDecimal other = null;
        if (sum1 != BigDecimal.ZERO) {
            other = sum.subtract(sum1);
        }
        result.getColumnBaseData().get(0).add(other);
        HeadField otherRowField = DeepcopyUtils.deepCopy(result.getRowHeadFields().get(0));
        otherRowField.setSummarizeData(other);
        String caption = "其余";
        otherRowField.setCaption(caption);
        String dimName = MetaNameUtil.getDimNameFromUniqueName(otherRowField.getValue());
        String uniqueName = "[" + dimName + "].[" + caption + "]";
        String nodeUniqueName = "{" + uniqueName + "}";
        otherRowField.setNodeUniqueName(nodeUniqueName);
        otherRowField.setValue(uniqueName);
        result.getRowHeadFields().add(otherRowField);
        return result;
    }
}
