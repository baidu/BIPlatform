package com.baidu.rigel.biplatform.ma.resource;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.baidu.rigel.biplatform.ac.query.model.ConfigQuestionModel;
import com.baidu.rigel.biplatform.ac.query.model.PageInfo;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.asyndownload.AyncAddDownloadTaskServiceFactory;
import com.baidu.rigel.biplatform.asyndownload.bo.AddTaskParameters;
import com.baidu.rigel.biplatform.asyndownload.bo.AddTaskStatus;
import com.baidu.rigel.biplatform.ma.download.DownloadType;
import com.baidu.rigel.biplatform.ma.download.service.DownloadServiceFactory;
import com.baidu.rigel.biplatform.ma.download.service.DownloadTableDataService;
import com.baidu.rigel.biplatform.ma.ds.exception.DataSourceOperationException;
import com.baidu.rigel.biplatform.ma.ds.service.DataSourceService;
import com.baidu.rigel.biplatform.ma.model.consts.Constants;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine;
import com.baidu.rigel.biplatform.ma.report.exception.QueryModelBuildException;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaContext;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.query.QueryAction;
import com.baidu.rigel.biplatform.ma.report.query.ReportRuntimeModel;
import com.baidu.rigel.biplatform.ma.report.service.QueryBuildService;
import com.baidu.rigel.biplatform.ma.report.service.ReportModelQueryService;
import com.baidu.rigel.biplatform.ma.report.utils.QueryDataUtils;
import com.baidu.rigel.biplatform.ma.report.utils.QueryUtils;
import com.baidu.rigel.biplatform.ma.resource.cache.ReportModelCacheManager;
import com.baidu.rigel.biplatform.ma.resource.utils.DataModelUtils;

/**
 * 专为下载提供服务的action
 * 
 * @author majun04
 *
 */
@RestController
@RequestMapping("/silkroad/reports")
public class DownloadDataResource extends BaseResource {
    /**
     * logger
     */
    private Logger logger = LoggerFactory.getLogger(DownloadDataResource.class);

    /**
     * reportModelCacheManager
     */
    @Resource
    private ReportModelCacheManager reportModelCacheManager;
    /**
     * 报表数据查询服务
     */
    @Resource
    private ReportModelQueryService reportModelQueryService;
    /**
     * dsService
     */
    @Resource
    private DataSourceService dsService;

    /**
     * queryBuildService
     */
    @Resource
    private QueryBuildService queryBuildService;

    /**
     * 平面表下载请求，下载数据，仅6万条，同步下载
     * 
     * @param reportId
     * @param areaId
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{reportId}/downloadOnline/{areaId}", method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseResult downloadForPlaneTable(@PathVariable("reportId") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        long begin = System.currentTimeMillis();
        ResponseResult rs = new ResponseResult();
        ReportDesignModel report = this.getDesignModelFromRuntimeModel(reportId);
        if (report == null) {
            throw new IllegalStateException("未知报表定义，请确认下载信息");
        }
        ExtendArea targetArea = report.getExtendById(areaId);

        // 从runtimeModel中取出designModel
        ReportDesignModel designModel = this.getDesignModelFromRuntimeModel(reportId);
        // 通过上一次的查询条件按获取Questionmodel
        QuestionModel questionModel = this.getQuestionModelFromQueryAction(reportId, areaId, request);
        // 在线下载需要添加6万条限制
        PageInfo pageInfo = new PageInfo();
        pageInfo.setCurrentPage(0);
        pageInfo.setPageSize(60000);
        pageInfo.setTotalRecordCount(0);
        questionModel.setPageInfo(pageInfo);
        // 下载类型
        // String downloadType = DownloadType.PLANE_TABLE_ONLINE.getName();
        // 获取下载服务
        DownloadType downloadType = DownloadType.PLANE_TABLE_ONLINE;
        downloadType.setDsType(dsService.getDsDefine(designModel.getDsId()).getDataSourceType().name());
        DownloadTableDataService downloadService = DownloadServiceFactory.getDownloadTableDataService(downloadType);
        Map<String, Object> setting = targetArea.getOtherSetting();
        // 获取下载字符串
        String csvString =
                downloadService.downloadTableData(questionModel, designModel.getExtendById(areaId).getLogicModel(),
                        setting);

        response.setCharacterEncoding("utf-8");
        response.setContentType("application/vnd.ms-excel;charset=GBK");
        response.setContentType("application/x-msdownload;charset=GBK");
        // 写入文件
        final String fileName = report.getName();
        // + "_" + timeRange.toString();
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf8") + ".csv");
        byte[] content = csvString.getBytes("GBK");
        response.setContentLength(content.length);
        OutputStream os = response.getOutputStream();
        os.write(content);
        os.flush();
        rs.setStatus(ResponseResult.SUCCESS);
        rs.setStatusInfo("successfully");

        logger.info("[INFO]download data cost : " + (System.currentTimeMillis() - begin) + " ms");
        return rs;
    }

    /**
     * @param reportId
     * @return ReportDesignModel
     */
    ReportDesignModel getDesignModelFromRuntimeModel(String reportId) {
        return reportModelCacheManager.getRuntimeModel(reportId).getModel();
    }

    /**
     * 通过上一次查询的QueryAction，获取questionModel
     *
     * @param reportId reportId
     * @param areaId areaId
     * @param request HttpServletRequest
     * @return QuestionModel 问题模型
     */
    private QuestionModel getQuestionModelFromQueryAction(String reportId, String areaId, HttpServletRequest request) {
        ReportDesignModel report = this.getDesignModelFromRuntimeModel(reportId);
        if (report == null) {
            throw new IllegalStateException("未知报表定义，请确认下载信息");
        }
        ExtendArea targetArea = report.getExtendById(areaId);
        ReportRuntimeModel runtimeModel = reportModelCacheManager.getRuntimeModel(reportId);

        // 从runtimeModel中取出designModel
        ReportDesignModel designModel = this.getDesignModelFromRuntimeModel(reportId);

        /**
         * TODO 增加参数信息
         */
        Map<String, Object> tmp = QueryUtils.resetContextParam(request, designModel);
        tmp.forEach((k, v) -> {
            runtimeModel.getLocalContextByAreaId(areaId).put(k, v);
        });

        // 获取查询条件信息
        ExtendAreaContext areaContext = QueryDataUtils.getAreaContext(areaId, request, targetArea, runtimeModel);
        // 设置查询不受限制
        areaContext.getParams().put(Constants.NEED_LIMITED, false);
        // 获取查询action
        // 获取上一次查询的QueryAction
        QueryAction action = runtimeModel.getPreviousQueryAction(areaId);
        if (action != null) {
            action.setChartQuery(false);
        } else {
            throw new RuntimeException("下载失败");
        }

        // 构建平面表下载分页信息
        PageInfo pageInfo = new PageInfo();
        // 默认设置为100000，这样后端不会对其实施count(*)求总的记录数
        pageInfo.setTotalRecordCount(100000);
        pageInfo.setCurrentPage(-1);
        // 默认设置为10万条记录
        pageInfo.setPageSize(-1);
        // 获取数据源信息
        DataSourceDefine dsDefine;
        QuestionModel questionModel = null;
        try {
            dsDefine = dsService.getDsDefine(designModel.getDsId(), areaContext.getParams());
            questionModel =
                    QueryUtils.convert2QuestionModel(dsDefine, designModel, action, areaContext.getParams(), null,
                            securityKey);
        } catch (DataSourceOperationException e) {
            logger.error(e.getCause().getMessage());
        } catch (QueryModelBuildException e) {
            logger.error(e.getCause().getMessage());
        }
        return questionModel;
    }

    /**
     * 平面表下载请求，默认下载全部数据，异步下载
     * 
     * @param reportId
     * @param areaId
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{reportId}/downloadOnline/asyn/{areaId}", method = { RequestMethod.GET,
            RequestMethod.POST })
    public ResponseResult downloadAsynForPlaneTable(@PathVariable("reportId") String reportId,
            @PathVariable("areaId") String areaId, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ResponseResult rs = new ResponseResult();
        String receiveMail = request.getParameter("receiveMail");
        String totalCount = request.getParameter("totalCount");
        if (StringUtils.isEmpty(receiveMail)) {
            rs.setStatus(ResponseResult.FAILED);
            rs.setStatusInfo("输入的邮箱不能为空");
            return rs;
        }
        if (StringUtils.isEmpty(totalCount)) {
            rs.setStatus(ResponseResult.FAILED);
            rs.setStatusInfo("下载的数据行数为0");
            return rs;
        }
        long begin = System.currentTimeMillis();
        // 从runtimeModel中取出designModel
        ReportDesignModel designModel = this.getDesignModelFromRuntimeModel(reportId);
        QuestionModel questionModel = this.getQuestionModelFromQueryAction(reportId, areaId, request);

        // setPageInfo
        PageInfo pageInfo = new PageInfo();
        pageInfo.setPageSize(5000);
        pageInfo.setCurrentPage(-1);
        pageInfo.setTotalRecordCount(Integer.valueOf(totalCount));
        questionModel.setPageInfo(pageInfo);

        // 获取cookies信息
        Cookie[] cookies = request.getCookies();
        HashMap<String, String> cookiesMap = new HashMap<String, String>();
        for (int i = 0; i < cookies.length; i++) {
            cookiesMap.put(cookies[i].getName(), cookies[i].getValue());
        }

        // 设置显示列的顺序
        LogicModel logicModel = designModel.getExtendById(areaId).getLogicModel();
        ConfigQuestionModel configQuestionModel = (ConfigQuestionModel) questionModel;
        try {
            Object obj =
                    AyncAddDownloadTaskServiceFactory
                            .getAyncAddDownloadTaskService("defaultAyncAddDownloadTaskService");
            AddTaskParameters addTaskParameters = new AddTaskParameters();
            addTaskParameters.setQuestionModel(questionModel);
            addTaskParameters.setRecMail(receiveMail);
            addTaskParameters.setReportName(designModel.getName());
            addTaskParameters.setCookies(cookiesMap);
            logger.info("download request referer object:{]", request.getHeaders("referer"));
            logger.info("download request referer value:{]", request.getHeaders("referer").nextElement());
            addTaskParameters.setRequestUrl(request.getHeaders("referer").nextElement());
            addTaskParameters.setColumns(DataModelUtils.getKeysInOrder(configQuestionModel.getCube(), logicModel));
            AddTaskStatus result =
                    (AddTaskStatus) obj.getClass().getMethod("addTask", AddTaskParameters.class)
                            .invoke(obj, addTaskParameters);
            if (result.getStatus() == 0) {
                rs.setStatus(ResponseResult.SUCCESS);
                rs.setStatusInfo("successfully");
            } else {
                rs.setStatus(ResponseResult.FAILED);
                rs.setStatusInfo("下载任务添加失败");
            }
            logger.info("[INFO]handle download asyn data cost : " + (System.currentTimeMillis() - begin) + " ms");
        } catch (Exception e) {
            rs.setStatusInfo("此下载服务目前不可用");
            rs.setStatus(ResponseResult.FAILED);
        }
        return rs;
    }

}
