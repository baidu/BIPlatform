package com.baidu.rigel.biplatform.ma.report.utils;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.LevelType;
import com.baidu.rigel.biplatform.ac.model.OlapElement;
import com.baidu.rigel.biplatform.ac.model.TimeType;
import com.baidu.rigel.biplatform.ac.query.model.PageInfo;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ac.util.TimeUtils;
import com.baidu.rigel.biplatform.cache.util.ApplicationContextHelper;
import com.baidu.rigel.biplatform.ma.model.service.PositionType;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaContext;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaType;
import com.baidu.rigel.biplatform.ma.report.model.ReportParam;
import com.baidu.rigel.biplatform.ma.report.model.PlaneTableFormat.PaginationSetting;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.query.QueryAction;
import com.baidu.rigel.biplatform.ma.report.query.QueryContext;
import com.baidu.rigel.biplatform.ma.report.query.ReportRuntimeModel;
import com.baidu.rigel.biplatform.ma.report.query.chart.SeriesInputInfo.SeriesUnitType;
import com.baidu.rigel.biplatform.ma.resource.cache.ReportModelCacheManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 数据查询utils
 * 
 * @author majun04
 *
 */
public class QueryDataUtils {
    /**
     * pageSize
     */
    private static final String PAGE_SIZE = "pageSize";

    /**
     * currentPage
     */
    private static final String CURRENT_PAGE = "currentPage";

    /**
     * totalRecordCount
     */
    private static final String TOTAL_RECORD_COUNT = "totalRecordCount";

    /**
     * private constructor
     */
    private QueryDataUtils() {

    }

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryDataUtils.class);

    /**
     * 每次报表请求时都需要运行的第一步，更新runtime模型的各上下文
     * 
     * @param model 报表设计模型
     * @param runTimeModel 报表运行时模型
     * @param contextParams http请求封装参数
     * @param params 报表运行时参数
     * @param condition 平面表条件
     * @return 返回修改完成的ReportRuntimeModel
     */
    public static ReportRuntimeModel modifyRuntimeModel4RuntimeContext(ReportDesignModel model,
            ReportRuntimeModel runTimeModel, Map<String, String[]> contextParams, Map<String, String> params,
            Map<String, String> condition) {
        for (String key : contextParams.keySet()) {
            /**
             * 更新runtimeModel的全局上下文参数
             */
            String[] value = contextParams.get(key);
            if (value != null && value.length > 0 && !StringUtils.isEmpty(value[0])) {
                String realValue = modifyFilterValue(value[0]);
                if (realValue != null) {
                    // 移除运行态模型的Context中的已有时间维度,保证有且仅有一个时间维度
                    boolean isTimeDim =
                            realValue.contains("start") && realValue.contains("end")
                                    && realValue.contains("granularity");
                    if (isTimeDim) {
                        for (Entry<String, Object> tmpEntry : runTimeModel.getContext().getParams().entrySet()) {
                            String tmpStr = String.valueOf(tmpEntry.getValue());
                            if (tmpStr.contains("start") || tmpStr.contains("end") || tmpStr.contains("granularity")) {
                                runTimeModel.getContext().removeParam(tmpEntry.getKey());
                                Map<String, QueryContext> localContext = runTimeModel.getLocalContext();
                                localContext.forEach((k, v) -> {
                                    v.reset();
                                });
                                break;
                            }
                        }
                    }
                    runTimeModel.getContext().getParams().put(getRealKey(model, key), realValue);
                    runTimeModel.getLocalContext().forEach((k, v) -> {
                        v.getParams().put(getRealKey(model, key), realValue);
                    });
                } else {
                    runTimeModel.getContext().removeParam(getRealKey(model, key));
                }
                if (params.containsKey(key)) {
                    String paramName = params.get(key);
                    String tmp = getParamRealValue(value[0]);
                    if (StringUtils.hasText(tmp)) {
                        runTimeModel.getContext().put(paramName, tmp);
                    } else {
                        runTimeModel.getContext().removeParam(paramName);
                    }
                }

                // 将平面表查询条件放入RuntimeModel的context中
                if (condition.containsKey(key)) {
                    String conditionName = condition.get(key);
                    String tmp = getParamRealValue(value[0]);
                    if (StringUtils.hasText(tmp)) {
                        runTimeModel.getContext().put(conditionName, tmp);
                    } else {
                        runTimeModel.getContext().removeParam(conditionName);
                    }
                }

            } else {
                runTimeModel.getContext().put(getRealKey(model, key), "");
                if (params.containsKey(key)) {
                    String paramName = params.get(key);
                    runTimeModel.getContext().put(paramName, "");
                }

                if (condition.containsKey(key)) {
                    String conditionName = condition.get(key);
                    runTimeModel.getContext().put(conditionName, "");
                }
            }

        }
        return runTimeModel;
    }

    /**
     * 临时方案，后续需要调整
     * 
     * @param tmpValue
     * @return String
     */
    private static String modifyFilterValue(String tmpValue) {
        if (tmpValue.contains("start") && tmpValue.contains("end")) {
            return genNewStartAndEnd(tmpValue);
        }
        String[] tmpValueArray = tmpValue.split(",");
        if (tmpValueArray.length == 1) {
            return tmpValue;
        }
        StringBuilder rs = new StringBuilder();
        for (int i = 0; i < tmpValueArray.length; ++i) {
            if (MetaNameUtil.isUniqueName(tmpValueArray[i])) {
                String[] metaName = MetaNameUtil.parseUnique2NameArray(tmpValueArray[i]);
                String value = metaName[metaName.length - 1];
                if (StringUtils.isEmpty(value) || value.contains(":")) {
                    continue;
                }
                rs.append(tmpValueArray[i]);
                if (i <= tmpValueArray.length - 1) {
                    rs.append(",");
                }
            }
        }
        return rs.toString();

    }

    /**
     * TODO 重新获取日期的开始和结束 ，add by jiangyichao
     */
    private static String genNewStartAndEnd(String timeValue) {
        String start;
        String end;
        String result = null;
        try {
            JSONObject json = new JSONObject(String.valueOf(timeValue));
            start = json.getString("start").replace("-", "");
            end = json.getString("end").replace("-", "");
            String granularity = json.getString("granularity");
            // 保证开始时间小于结束时间
            if (start.compareTo(end) > 0) {
                String tmp = start;
                start = end;
                end = tmp;
            }
            Map<String, String> time = null;
            switch (granularity) {
            // 年
                case "Y":
                    time = TimeUtils.getTimeCondition(start, end, TimeType.TimeYear);
                    break;
                // 季度
                case "Q":
                    time = TimeUtils.getTimeCondition(start, end, TimeType.TimeQuarter);
                    break;
                // 月份
                case "M":
                    time = TimeUtils.getTimeCondition(start, end, TimeType.TimeMonth);
                    break;
                // 星期
                case "W":
                    time = TimeUtils.getTimeCondition(start, end, TimeType.TimeWeekly);
                    break;
                // 天
                case "D":
                    time = TimeUtils.getTimeCondition(start, end, TimeType.TimeDay);
                    break;
                default:
                    break;
            }
            start = time.get("start");
            end = time.get("end");
            json.put("start", start);
            json.put("end", end);
            LOGGER.info("start time is [" + start + "],and end time is [" + end + "]");
            result = json.toString();
        } catch (Exception e) {
            LOGGER.debug("the input time format is wrong" + timeValue, e);
        }
        return result;
    }

    /**
     * 
     * @param model {@link ReportDesignModel}
     * @param key String
     * @return String real key
     */
    private static String getRealKey(ReportDesignModel model, String key) {
        if (model != null && model.getExtendById(key) != null) {
            if (model.getExtendById(key).listAllItems().isEmpty()) {
                return key;
            }
            return model.getExtendById(key).listAllItems().keySet().toArray(new String[0])[0];
        }
        return key;
    }

    public static String getParamRealValue(String realValue) {
        // modify by yichao.jiang 接收url传递过来的时间参数，并进行转换
        if (realValue.contains("start") && realValue.contains("end")) {
            return genNewStartAndEnd(realValue);
        }
        String[] tmp = realValue.split(",");
        if (tmp.length == 1) {
            if (StringUtils.isEmpty(tmp)) {
                return realValue;
            }
            if (MetaNameUtil.isUniqueName(tmp[0])) {
                String[] metaName = MetaNameUtil.parseUnique2NameArray(tmp[0]);
                return metaName[metaName.length - 1];
            }
        }
        StringBuilder rs = new StringBuilder();
        for (int i = 0; i < tmp.length; ++i) {
            if (StringUtils.isEmpty(tmp[i]) || tmp[i].contains(":")) {
                continue;
            }
            String[] metaName = MetaNameUtil.parseUnique2NameArray(tmp[i]);
            rs.append(metaName[metaName.length - 1]);
            if (i <= tmp.length - 2) {
                rs.append(",");
            }
        }
        return rs.toString();
    }

    /**
     * 更新runTimeModel指定区域上下文，并返回结合外部http参数替换之后的最终请求参数map
     * 
     * @param runTimeModel runTimeModel
     * @param areaId 区域id
     * @param contextParams http请求map
     * @return 返回结合外部http参数替换之后的最终请求参数map
     */
    public static Map<String, Object> updateLocalContextAndReturn(ReportRuntimeModel runTimeModel, String areaId,
            Map<String, String[]> contextParams) {
        /**
         * 查询区域的时候，会按照当前的参数更新区域上下文
         */
        QueryContext localContext = runTimeModel.getLocalContextByAreaId(areaId);
        // localContext.reset ();

        /**
         * 查询参数，首先载入全局上下文，再覆盖局部上下文
         */
        final Map<String, Object> queryParams = Maps.newHashMap();
        /**
         * TODO 暂时用全局的覆盖本地的参数，以后考虑是否会有问题
         */
        Map<String, Object> localParams = localContext.getParams();

        if ("true".equals(localParams.get("isOverride"))) {
            queryParams.putAll(localParams);

            runTimeModel.getContext().getParams().forEach((key, value) -> {
                if (!queryParams.containsKey(key)) {
                    queryParams.put(key, value);
                }
            });

            return queryParams;
        }
        queryParams.putAll(localParams);
        if (runTimeModel.getContext() != null) {
            queryParams.putAll(runTimeModel.getContext().getParams());
        } else {
            throw new RuntimeException("没有初始化？？");
        }
        Map<String, Object> tmp = Maps.newConcurrentMap();
        queryParams.forEach((k, v) -> {
            if (v != null && !StringUtils.isEmpty(v.toString())) {
                tmp.put(k, v);
            }
        });
        // 用当前查询参数，覆盖旧参数
        for (String key : contextParams.keySet()) {
            /**
             * 更新runtimeModel的区域上下文参数
             */
            String[] value = contextParams.get(key);
            if (value != null && value.length > 0) {
                tmp.put(key, value[0]);
            }
        }
        return tmp;
    }

    /**
     * 为平面报表构建分页信息
     * 
     * @param runTimeModel runTimeModel
     * @param targetArea targetArea
     * @param action action
     * @param request request
     * @return 返回构建完成的分页对象
     */
    public static PageInfo constructPageInfo4Query(ReportRuntimeModel runTimeModel, ExtendArea targetArea,
            QueryAction action, HttpServletRequest request) {
        // 分页信息
        PageInfo pageInfo = new PageInfo();
        // 获取分页设置信息
        PaginationSetting pageSetting = targetArea.getPlaneTableFormat().getPageSetting();
        if (pageSetting.getIsPagination()) {
            // 设置分页大小
            if (StringUtils.hasLength(request.getParameter(PAGE_SIZE))) {
                pageInfo.setPageSize(Integer.valueOf(request.getParameter(PAGE_SIZE)));
            }
            // 设置当前页
            if (StringUtils.hasLength(request.getParameter(CURRENT_PAGE))) {
                pageInfo.setCurrentPage(Integer.valueOf(request.getParameter(CURRENT_PAGE)) - 1);
            }
            // 设置总的记录数
            if (StringUtils.hasLength(request.getParameter(TOTAL_RECORD_COUNT))) {
                pageInfo.setTotalRecordCount(Integer.valueOf(request.getParameter(TOTAL_RECORD_COUNT)));
            } else {
                pageInfo.setTotalRecordCount(-1);
            }
        } else {
            // 如果没有分页，则设置第一个页(对于数据库，分页从0开始)
            pageInfo.setCurrentPage(0);
            // 设置不分页情况下，查询的条数
            pageInfo.setPageSize(pageSetting.getPageSize());
            // 当该数设置为-1时，不进行count(*)查询
            pageInfo.setTotalRecordCount(-1);
        }

        return pageInfo;
    }

    /**
     * 获取扩展区域中定义的chartType
     * 
     * @param targetArea ExtendArea
     * @return SeriesUnitType
     */
    public static Map<String, String> getChartTypeWithExtendArea(ReportDesignModel model, ExtendArea targetArea) {
        Map<String, String> chartTypes = Maps.newHashMap();
        if (targetArea.getType() == ExtendAreaType.LITEOLAP_CHART) {
            chartTypes.put("null", SeriesUnitType.LINE.name());
            return chartTypes;
        }
        targetArea
                .listAllItems()
                .values()
                .stream()
                .filter(item -> {
                    return item.getPositionType() == PositionType.Y || item.getPositionType() == PositionType.CAND_IND;
                })
                .forEach(
                        item -> {
                            OlapElement element =
                                    ReportDesignModelUtils.getDimOrIndDefineWithId(model.getSchema(),
                                            targetArea.getCubeId(), item.getOlapElementId());
                            Object chartType = item.getParams().get("chartType");
                            if (chartType == null) {
                                chartTypes.put(element.getUniqueName(), SeriesUnitType.COLUMN.name());
                            } else {
                                chartTypes.put(element.getUniqueName(), chartType.toString());
                            }
                        });
        return chartTypes;
    }

    /**
     * 
     * @param dim
     * @return
     */
    public static boolean isCallbackDim(Dimension dim) {
        if (dim == null) {
            return false;
        }
        Level level = dim.getLevels().values().toArray(new Level[0])[0];
        return isCallbackLevel(level);
    }

    /**
     * 判断某个level是否为callback isCallbackLevel
     * 
     * @param level
     * @return
     */
    public static boolean isCallbackLevel(Level level) {
        return level != null && level.getType() == LevelType.CALL_BACK;
    }

    /**
     * 获取本次查询对应的参数信息
     * 
     * @param areaId
     * @param request
     * @param targetArea
     * @param runTimeModel
     * @return ExtendAreaContext
     */
    public static ExtendAreaContext getAreaContext(String areaId, HttpServletRequest request, ExtendArea targetArea,
            ReportRuntimeModel runTimeModel) {
        Map<String, Object> queryParams =
                QueryDataUtils.updateLocalContextAndReturn(runTimeModel, areaId, request.getParameterMap());
        runTimeModel.getLocalContextByAreaId(areaId).getParams().putAll(queryParams);
        String reportModelId = runTimeModel.getReportModelId();
        ReportModelCacheManager reportModelCacheManager =
                (ReportModelCacheManager) ApplicationContextHelper.getContext().getBean("reportModelCacheManager");
        ExtendAreaContext areaContext = reportModelCacheManager.getAreaContext(reportModelId, targetArea.getId());
        areaContext.getParams().clear();
        areaContext.getParams().putAll(queryParams);
        return areaContext;
    }

    public static void fillBackParamValues(final ReportRuntimeModel runtimeModel, Dimension dim,
            Map<String, Object> datasource) {
        runtimeModel.getLocalContext().forEach((k, v) -> {
            if (v.getParams().containsKey(dim.getId())) {
                Object value = v.getParams().get(dim.getId());
                if (value != null && value instanceof String) {
                    List<String> lists = Lists.newArrayList(((String) value).split(","));
                    datasource.put("value", lists);
                }
            }
        });
    }

    /**
     * 获取某个维度对应的"参数设置"部分的名称
     * 
     * @param dim
     * @param model
     * @return
     */
    public static String getParamName(Dimension dim, ReportDesignModel model) {
        String value = null;
        Map<String, ReportParam> params = model.getParams();
        if (params != null && params.size() != 0) {
            for (ReportParam param : params.values()) {
                if (param.getElementId().equals(dim.getId())) {
                    return param.getName();
                }
            }
        }
        return value;
    }

    /**
     * 根据uniqueName解析出最后一个value的值
     * 
     * @param callbackParamName
     * @param uniqueName
     * @return
     */
    public static String getCallbackParamValue(String callbackParamName, String uniqueName) {
        if (!StringUtils.isEmpty(callbackParamName) && MetaNameUtil.isUniqueName(uniqueName)) {
            String nameArray[] = MetaNameUtil.parseUnique2NameArray(uniqueName);
            return nameArray[nameArray.length - 1];
        }
        return null;
    }
}
