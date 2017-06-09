/**
 * di.config.URL
 * Copyright 2012 Baidu Inc. All rights reserved.
 * 
 * @file:    data insight 全局(包括console和product)的URL定义
 * @author:  sushuang(sushuang)
 */
$namespace('di.config');

(function() {
    
    //--------------------------------
    // 引用
    //--------------------------------

    var xextend = xui.XDatasource.extend;

    //--------------------------------
    // 类型声明
    //--------------------------------

    /**
     * 因为URL要作为权限验证，所以在使用时再加WEB_ROOT
     * web根目录, 页面初始时从后台传来，暂存在_TMP_WEB_ROOT_中
     *
     * @usage 
     *      假设有定义：kt.config.URL.SOME_TABLE_QUERY = '/some/table.action';
     *      用这样语句获得请求url： kt.config.URL('SOME_TABLE_QUERY'); 
     * @param {string} urlAttr url常量名
     * @return {string} 请求使用的url
     */
    var URL = $namespace().URL = function(urlConst) {
        var url = URL_SET[urlConst];
        if (!url) {
            throw new Error('empty url!');
        }
        return URL.getWebRoot() + url;
    };

    URL.fn = function (urlConst) {
        return xutil.fn.bind(URL, null, urlConst);
    };

    var URL_SET = {};
    var webRoot;

    /**
     * 得到运行时的web base
     * 
     * @public
     * @return {string} 运行时的web base
     */
    URL.getWebRoot = function() {
        return webRoot || $getNamespaceBase().WEB_ROOT || '';
    };

    URL.setWebRoot = function(root) {
        webRoot = root;
    };

    /**
     * 增加URL
     * 
     * @public
     * @param {string} 新增的URL
     */
    URL.addURL = function(name, url) {
        // 检查重复
        if (URL_SET[name]) {
            throw new Error('Duplicate URL! name=' + name + ' url=' + url);
        }

        // 新增
        URL_SET[name] = url;
    };

    URL.changeURL = function(name, url) {
        URL_SET[name] = url;
    };

    //--------------------------------
    // 公用URL
    //--------------------------------  

    // 打开报表编辑
    URL_SET.OLAP_REPORT_INIT = '/reportTemplate/initReportTemplate.action';
    URL_SET.CONSOLE_SAVE_TPL = '/manage/virtualTemplate/saveVirtualTemplate.action';
    URL_SET.CONSOLE_GET_COND = '/manage/virtualTemplate/getSelectableConditions.action';
    URL_SET.CONSOLE_EXIST_COND = '/manage/virtualTemplate/getSelectedConditions.action';    
    URL_SET.CONSOLE_VTPL_LIST = '/manage/virtualTemplate/getVirtualTemplateInfo.action';
//    URL_SET.CONSOLE_MOLD_LIST = '/manage/moldTemplate/getMoldTemplates.action';
    URL_SET.CONSOLE_DS_LIST = '/manage/virtualTemplate/getReportTemplates.action';
    URL_SET.CONSOLE_TO_PRE = '/manage/publish/publishToPre.action';
    URL_SET.CONSOLE_TO_RELEASE = '/manage/publish/publishToRelease.action';
    URL_SET.OLAP_SAVE = '/reportTemplate/save.action';


    // PlaneTable
    URL_SET.PLANE_TABLE_INIT = '/reportTemplate/planeTable/init.action';
    URL_SET.PLANE_TABLE_SQL_SAVE = '/reportTemplate/planeTable/create.action';
    URL_SET.PLANE_TABLE_COL_DATA = '/reportTemplate/planeTable/doMapColumns.action';
    URL_SET.PLANE_TABLE_COL_SAVE = '/reportTemplate/planeTable/saveColumns.action';
    URL_SET.PLANE_TABLE_COND_DATA = '/reportTemplate/planeTable/doMapConds.action';
    URL_SET.PLANE_TABLE_COND_SAVE = '/reportTemplate/planeTable/saveConds.action';
    URL_SET.PLANE_TABLE_PREVIEW_DATA = '/reportTemplate/planeTable/doMapPreview.action';
    // 预览
    // @param reportId
    // @param AAAfromURL=12345&BBBfromURL=67899&showColumns=AAAfromURL&showColumns=BBBfromURL& ...
    // @return {  }
    // URL_SET.PLANE_TABLE_DATA = '/reportTemplate/planeTable/preview.action';

    // 得到cube tree
    URL_SET.CUBE_META = '/meta/getCubeTree.action';
    // 得到plane table的数据源列表
    URL_SET.DATASOURCE_META = '/reportTemplate/planeTable/getDs.action';

    // 获取维度树
    URL_SET.DIM_TREE_TABLE = '/reportTemplate/table/getDimTree.action';
    URL_SET.DIM_TREE_CHART = '/reportTemplate/chart/getDimTree.action';

//    URL_SET.DIM_MULTISELECT_TABLE = '/reportTemplate/table/getDimMultiSelect.action';
//    URL_SET.DIM_MULTISELECT_CHART = '/reportTemplate/chart/getDimMultiSelect.action';

    URL_SET.DIM_MULTISELECT_TABLE = '/reports/runtime/extend_area/#{componentId}/dims/#{dimSelectName}/members';
    URL_SET.DIM_MULTISELECT_CHART = '/reportTemplate/chart/getDimMultiSelect.action';

//    URL_SET.DIM_SELECT_SAVE_TABLE = '/reportTemplate/table/updateDimNodes.action';
//    URL_SET.DIM_SELECT_SAVE_CHART = '/reportTemplate/chart/updateDimNodes.action';

    URL_SET.DIM_SELECT_SAVE_TABLE = '/reports/runtime/extend_area/#{componentId}/dims/#{dimSelectName}/members/1';
    URL_SET.DIM_SELECT_SAVE_CHART = '/reportTemplate/chart/updateDimNodes.action';

    // 指标维度元数据
    URL_SET.MEASURE_DES = '/reportTemplate/table/getMeasureDescription.action';

    //URL_SET.META_CONDITION_IND_DIM_TABLE = '/reportTemplate/table/getMetaData.action';
    // 加载拖拽区域数据
    URL_SET.META_CONDITION_IND_DIM_TABLE = '/reports/#{reportId}/runtime/extend_area/#{componentId}/config';
    // 获取图形上面的下拉框的内容
    //URL_SET.LITEOLAP_INDS_META_DATA = '/reportTemplate/liteolap/getCurrentAnalysisInds.action';
    URL_SET.LITEOLAP_INDS_META_DATA = '/reports/#{reportId}/runtime/extend_area/#{componentId}/ind_for_chart';
    URL_SET.META_CONDITION_IND_DIM_CHART = '/reportTemplate/chart/getMetaData.action';
    //URL_SET.META_CONDITION_SELECT_TABLE = '/reportTemplate/table/dragAndDrop.action';
    // 拖拽完毕后提交的请求
    URL_SET.META_CONDITION_SELECT_TABLE = '/reports/#{reportId}/runtime/extend_area/#{componentId}/item';
    URL_SET.META_CONDITION_SELECT_CHART = '/reportTemplate/chart/dragAndDrop.action';
    URL_SET.META_CONDITION_LIST_SELECT_CHART = '/reportTemplate/chart/selectInd.action'; // 这是个为list形式的元数据提交而写的临时接口
    URL_SET.META_CONDITION_LIST_SELECT_TABLE = '/reportTemplate/table/selectInd.action'; // 这是个为list形式的元数据提交而写的临时接口
    URL_SET.META_CONDITION_COL_CONFIG_GET = '/reportTemplate/table/COLCONFIGGET.action'; // 这是个为list形式的元数据提交而写的临时接口
    URL_SET.META_CONDITION_COL_CONFIG_SUBMIT = '/reportTemplate/table/COLCONFIGGET.action'; // 这是个为list形式的元数据提交而写的临时接口
    URL_SET.META_CONDITION_CANDIDATE_INIT = '/reportTemplate/configure/getTemplateMeta.action ';
    URL_SET.META_CONDITION_CANDIDATE_SUBMIT = '/repoyozrtTemplate/configure/setTemplateMeta.action ';
    URL_SET.META_CONDITION_ADD_SERIES_GROUP = '/reportTemplate/chart/addSeriesUnit.action';
    URL_SET.META_CONDITION_REMOVE_SERIES_GROUP = '/reportTemplate/chart/removeSeriesUnit.action';
    // 图设置
    URL_SET.CONSOLE_CHART_CONFIG_INIT = '/reportTemplate/chart/getChartSettings.action';
    URL_SET.CONSOLE_CHART_CONFIG_SUBMIT = '/reportTemplate/chart/updateChartSettings.action';

    // 表单
    URL_SET.FORM_ASYNC_DATA = '/reports/#{reportId}/members/#{componentId}'; // TODO:维度树获取子节点
    URL_SET.FORM_DATA = '/reports/#{reportId}/init_params';
    URL_SET.FORM_UPDATE_CONTEXT = '/reports/#{reportId}/runtime/context';
    URL_SET.FORM_CASCADE_GETLEVEL = '/reports/#{reportId}/members/#{componentId}'; // TODO:维度树获取子节点
    URL_SET.FORM_REGULAR = '/reports/#{reportId}/regular/#{taskId}';
    URL_SET.OLAP_TABLE_DATA = '/reports/#{reportId}/runtime/extend_area/#{componentId}';
    URL_SET.OLAP_TABLE_DRILL =  '/reports/#{reportId}/runtime/extend_area/#{componentId}/drill/#{action}';
    URL_SET.OLAP_TABLE_LINK_DRILL = '/reports/#{reportId}/runtime/extend_area/#{componentId}/drill';
    URL_SET.OLAP_TABLE_SELECT = '/reports/#{reportId}/runtime/extend_area/#{componentId}/selected_row';
    URL_SET.OLAP_TABLE_SORT = '/reports/#{reportId}/runtime/extend_area/#{componentId}/sort';
    URL_SET.OLAP_TABLE_DOWNLOAD = '/reports/#{reportId}/download/#{componentId}';
    URL_SET.OLAP_TABLE_RICH_SELECT_DATA = '/reports/#{reportId}/runtime/extend_area/#{componentId}/changablemeasures';
    URL_SET.OLAP_TABLE_RICH_SELECT_CHANGE = '/reports/#{reportId}/runtime/extend_area/#{componentId}/changedMeasures';
    // PLANE表（平面表）
    // URL_SET.PLANE_TABLE_DATA = '/reportTemplate/planeTable/transform.action';
    URL_SET.PLANE_TABLE_DATA = '/reports/#{reportId}/runtime/extend_area/#{componentId}';
    URL_SET.PLANE_TABLE_SORT = '/reports/#{reportId}/runtime/extend_area/#{componentId}/item/#{elementId}/sort';
    URL_SET.PLANE_TABLE_GET_FIELDSLIST = '/reports/#{reportId}/runtime/extend_area/#{componentId}/dimAndInds';
    URL_SET.PLANE_TABLE_RESET_FIELDS = '/reports/#{reportId}/runtime/extend_area/#{componentId}/reset';
    URL_SET.PLANE_TABLE_SUBMIT_FIELD_SET_INFO = '/reports/#{reportId}/runtime/extend_area/#{componentId}/submitSetInfo';
    URL_SET.PLANE_TABLE_DELETE_FIELD_SET_INFO = '/reports/#{reportId}/runtime/extend_area/#{componentId}/item/#{elementId}/removeSetInfo';
    URL_SET.PLANE_TABLE_DOWNLOAD = '/reports/#{reportId}/downloadOnline/#{componentId}';

    // 图-最新路径
    URL_SET.OLAP_CHART_DATA = '/reports/#{reportId}/runtime/extend_area/#{componentId}';
    URL_SET.LITEOLAP_CHART_DATA = '/reports/#{reportId}/runtime/extend_area/#{componentId}';
    URL_SET.OLAP_CHART_CHANGE_RADIOBUTTON = '/reports/#{reportId}/runtime/extend_area/#{componentId}/index/#{index}';
    // 报表预览
    URL_SET.REPORT_PREVIEW = '/reportTemplate/complex/generateReport.action';
    // URL_SET.REPORT_PREVIEW = '/asset-d/ditry/dev/try-standard.html';

    // 表头属性
    URL_SET.ROWHEAD_CONFIG_INIT = '/reportTemplate/table/rowHeadConfig/getDrillTypeConfig.action';
    URL_SET.ROWHEAD_CONFIG_SUBMIT = '/reportTemplate/table/rowHeadConfig/setRowHeadDrillTypes.action';

    // 行（轴）维度展示属性
    URL_SET.DIMSHOW_CONFIG_INIT = '/reportTemplate/table/rowHeadConfig/getDimShowConfig.action';
    URL_SET.DIMSHOW_CONFIG_SUBMIT = '/reportTemplate/table/rowHeadConfig/setDimShowConfig.action';

	//从模板中根据KEY获取数据
    URL_SET.GET_TEMPLATE_INFO = '/reportTemplate/configure/getTemplateInfo.action';

    //设置模板的数据格式
    URL_SET.DATA_FORMAT_SET = '/reportTemplate/data/setDataFormat.action';

    //提交设置报表的RMkey
    URL_SET.REPORT_ROWMERGE_KEY_SUBMIT = '/reportTemplate/updateProperties.action';
    //首页的报表查询url
    URL_SET.REPORT_QUERY = '/myview/queryReportList.action';
    //mold模板查询url
    URL_SET.MOLD_QUERY = '/manage/moldTemplate/getMoldTemplates.action';
    //mold模板查询url
    URL_SET.PAHNTOMJS_INFO = '/myview/getPhantomJsInfo.action';

    //报表保存镜像操作url
    URL_SET.RTPL_SAVE_ADD = '/reports/#{reportId}/new_status';
    URL_SET.RTPL_SAVE_UPDATE = '/reports/#{reportId}/status';
    URL_SET.RTPL_SAVE_GETIMAGES = '/reports/#{reportId}/status/list';
    URL_SET.RTPL_SAVE_DELETE = '/reports/#{reportId}/del_status';

})();