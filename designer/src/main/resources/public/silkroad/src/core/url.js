/**
 * @file: 报表配置端url信息
 * @author: lizhantong(lztlovely@126.com)
 * @date: 2014-08-22
 */

define(function () {

    //--------------------------------
    // 类型声明
    //--------------------------------

    var Url = {};

    var webRoot;

    var REPORTS = 'reports';
    var DATASOURCES = 'datasources';
    var DATASOURCES_GROUP = 'dsgroup';
    /**
     * 得到运行时的web base
     *
     * @public
     * @return {string} 运行时的web base
     */
    Url.getWebRoot = function() {
        return webRoot;
    };

    /**
     * 设置根路径
     *
     * @param {string} 根路径
     * @public
     */
    Url.setWebRoot = function(root) {
        if (root === '') {
            webRoot = root;
        } else {
            webRoot = root + '/';
        }
    };


    /**
     * 获取数据源的基本路径
     *
     * @param {string=} dataSourceId 数据源id
     * @private
     * @return {string} 数据源的基本路径
     */
    function getDataSourcesBaseUrl(groupId, dataSourceId) {
        return dataSourceId === undefined
            ? (getDataSourcesGroupBaseUrl(groupId) + '/' + DATASOURCES)
            : (getDataSourcesGroupBaseUrl(groupId) + '/' + DATASOURCES + '/' + dataSourceId);
    }
    /**
     * 获取数据源组的基本路径
     *
     * @param {string=} dataSourceGroupId 数据源组id
     * @private
     * @return {string} 数据源的基本路径
     */
    function getDataSourcesGroupBaseUrl(dataSourceGroupId) {
        return dataSourceGroupId === undefined
            ? (webRoot + DATASOURCES_GROUP)
            : (webRoot + DATASOURCES_GROUP + '/' + dataSourceGroupId);
    }

    /**
     * 获取报表的基本路径
     *
     * @param {string=} reportId 报表id
     * @private
     * @return {string} 报表的基本路径
     */
    function getReportsBaseUrl(reportId) {
        return reportId === undefined
            ? (webRoot + REPORTS)
            : (webRoot + REPORTS + '/' + reportId);
    }

    /**
     * 更换皮肤提交基本路径
     *
     * @param {string} reportId 报表id
     * @public
     * @return {string} 更换皮肤提交基本路径
     */
    function getSkinType(reportId) {
        return getReportsBaseUrl(reportId);
    }

    /**
     * 获取cube的基本路径
     *
     * @param {string} reportId 数据源id
     * @param {string} cubeId cube的id
     * @private
     * @return {string} cube的基本路径
     */
    function getCubesBaseUrl(reportId, cubeId) {
        return ''
            + getReportsBaseUrl(reportId)
            + '/cubes/'
            + cubeId;
    }

    /**
     * 参数维度提交基本路径
     *
     * @param {string} reportId 报表id
     * @public
     * @return {string} 参数维度获取的基本路径
     */
    function getParameterDimData(reportId) {
        return getReportsBaseUrl(reportId);
    }

    /**
     * 参数维度获取基本路径
     *
     * @param {string} reportId 报表id
     * @public
     * @return {string} 参数维度获取的基本路径
     */
    function getParameterDim(reportId) {
        return getReportsBaseUrl(reportId);
    }

    /**
     * 获取组件区的基本路径
     *
     * @param {string} reportId 报表id
     * @param {string} componentId 组件的id
     * @private
     * @return {string} 组件区的基本路径
     */
    function getExtendAreaBaseUrl(reportId, componentId) {
        var url = getReportsBaseUrl(reportId);
        url = (componentId === undefined)
            ? (url + '/extend_area')
            : (url + '/extend_area/' + componentId);
        return url;
    }

    /**
     * 获取维度组的基本路径
     *
     * @param {string} dataSourceId 数据源id
     * @param {string} componentId 组件的id
     * @param {string=} groupId 维度组的id
     * @private
     * @return {string} 维度组的基本路径
     */
    function getDimGroupBaseUrl(reportId, cubeId, groupId) {
        var url = getCubesBaseUrl(reportId, cubeId);

        url = (groupId === undefined)
            ? (url + '/dim_groups')
            : (url + '/dim_groups/' + groupId);
        return url;
    }

    /**
     * 更换报表名称提交基本路径
     *
     * @param {string} reportId 报表id
     * @public
     * @return {string} 更换皮肤提交基本路径
     */
    function getReportName(reportId) {
        return getReportsBaseUrl(reportId);
    }
    /**
     * 更换报表名称提交基本路径
     *
     * @param {string} reportId 报表id
     * @public
     * @return {string} 更换皮肤提交基本路径
     */
    function getSaveReportName(reportId) {
        return getReportsBaseUrl(reportId);
    }

    //--------------------------------
    // 公用URL
    //--------------------------------

    /**
     * 数据源列表模块
     * 获取数据源列表url
     *
     * @public
     * @return {string} url
     */
    Url.loadDsgroupList = function () {
        return getDataSourcesGroupBaseUrl();
    };

    /**
     * 数据源列表模块
     * 删除数据源url
     *
     * @param {string} dataSourceId 数据源id
     * @public
     * @return {string} url
     */
    Url.deleteDataSources = function (groupId, dsId) {
        return getDataSourcesBaseUrl(groupId, dsId);
    };

    /**
     * 数据源新建和编辑模块
     * 获取当前数据源详细信息
     *
     * @param {string} dataSourceId 报表id
     * @public
     * @return {string} url
     */
    Url.getCurrentDataSourceInfo = function (groupId, dataSourceId) {
        return getDataSourcesBaseUrl(groupId, dataSourceId);
    };

    /**
     * 数据源新建模块
     * 提交数据源详细信息
     *
     * @public
     * @return {string} url
     */
    Url.submitDataSourceInfoAdd = function (groupId) {
        return getDataSourcesBaseUrl(groupId);
    };

    /**
     * 数据源编辑模块
     * 提交数据源详细信息
     *
     * @param {string} dataSourceId 报表id
     * @public
     * @return {string} url
     */
    Url.submitDataSourceInfoUpdate = function (groupId, dataSourceId) {
        return getDataSourcesBaseUrl(groupId, dataSourceId);
    };

    /**
     * 数据源新建和编辑模块
     * 加载某一数据源所含的表
     *
     * @param {string} dataSourceId 报表id
     * @public
     * @return {string} url
     */
    Url.loadTables = function (groupId, dataSourceId) {
        return getDataSourcesBaseUrl(groupId, dataSourceId) + '/tables';
    };

    /**
     * 数据源组新建和编辑模块
     * 数据源组新建
     *
     * @param {string} dataSourceId 报表id
     * @public
     * @return {string} url
     */
    Url.addDsGroup = function () {
        return getDataSourcesGroupBaseUrl();
    };
    /**
     * 数据源组新建和编辑模块
     * 数据源组编辑
     *
     * @param {string} dataSourceId 报表id
     * @public
     * @return {string} url
     */
    Url.editDsGroup = function (groupId) {
        return getDataSourcesGroupBaseUrl(groupId);
    };
    Url.changeDsActive = function (groupId, dsId) {
        return getDataSourcesBaseUrl(groupId, dsId) + '/changeActive';
    };
    /**
     * cube设置模块
     * 获取活动的数据源
     *
     * @param {string} dataSourceId 报表id
     * @public
     * @return {string} url
     */
    Url.loadDsGroupActive = function () {
        return getDataSourcesGroupBaseUrl('active');
    };
    /**
     * 报表列表
     * 获取报表列表url
     *
     * @public
     * @return {string} url
     */
    Url.loadReportList = function () {
        return getReportsBaseUrl();
    };

    /**
     * 报表列表
     * 预览报表
     *
     * @param {string} reportId 报表id
     * @public
     * @return {string} url
     */
    Url.showReport = function (reportId) {
        return getReportsBaseUrl(reportId) + '/preview_info';
    };

    /**
     * 报表列表
     * 删除报表url
     *
     * @param {string} reportId 报表id
     * @public
     * @return {string} url
     */
    Url.deleteReport = function (reportId) {
        return getReportsBaseUrl(reportId);
    };

    /**
     * 报表列表
     * 新建报表url
     *
     * @public
     * @return {string} url
     */
    Url.addReport = function () {
        return getReportsBaseUrl();
    };

    /**
     * 报表列表
     * 复制报表url
     *
     * @param {string} reportId 报表id
     * @public
     * @return {string} url
     */
    Url.copyReport = function (reportId) {
        return getReportsBaseUrl(reportId) +  '/duplicate';
    };

    /**
     * 报表新建（编辑）-维度设置模块
     * 获取维度设置模块默认信息列表url
     *
     * @param {string} reportId 报表id
     * @public
     * @return {string} url
     */
    Url.getDimSetList = function (reportId) {
        return getReportsBaseUrl(reportId) + '/dim_config';
    };

    /**
     * 报表新建（编辑）-维度设置模块
     * 时间维度设置：如果是普通表，去后端获取普通标对应的信息
     *
     * @param {string} reportId 报表id
     * @public
     * @return {string} url
     */
    Url.getDimDateRelationTableList = function (reportId, tableId) {
        return ''
            + getReportsBaseUrl(reportId)
            + '/tables/'
            + tableId;
    };

    /**
     * 报表新建（编辑）-维度设置模块
     * 提交维度设置模块的设置信息
     *
     * @param {string} reportId 报表id
     * @public
     * @return {string} url
     */
    Url.submitDimSetInfo = function (reportId) {
        return getReportsBaseUrl(reportId) + '/dim_config';
    };

    /**
     * 报表新建（编辑）-cube设置模块
     * 加载选中的数据源（主要用于编辑时的数据还原）对应的url
     *
     * @param {string} reportId 报表id
     * @public
     * @return {string} url
     */
    Url.loadSelectedDataSources = function (reportId) {
        return getReportsBaseUrl(reportId) + '/ds_id';
    };

    /**
     * 报表新建（编辑）-cube设置模块
     * 加载报表所对应的已经选中的cube的id数组对应的url
     *
     * @param {string} reportId 报表id
     * @public
     * @return {string} url
     */
    Url.loadReportFactTableList = function (reportId) {
        return getReportsBaseUrl(reportId) + '/cube_tables';
    };

    /**
     * 报表新建（编辑）-cube设置模块
     * 提交报表设置对应的url
     *
     * @param {string} reportId 报表id
     * @public
     * @return {string} url
     */
    Url.submitCubeSetInfo = function (reportId) {
        return getReportsBaseUrl(reportId) + '/star_models';
    };

    /**
     * 报表新建（编辑）-edit
     * getRuntimeId
     *
     * @param {string} reportId 报表id
     * @public
     * @return {string} url
     */
    Url.getRuntimeId = function (reportId) {
        return getReportsBaseUrl(reportId) + '/runtime_model';
    };

    /**
     * 报表新建（编辑）-edit
     * loadCubeList
     *
     * @param {string} reportId 报表id
     * @public
     * @return {string} url
     */
    Url.loadCubeList = function (reportId) {
        return getReportsBaseUrl(reportId) + '/cubes';
    };

    /**
     * 报表新建（编辑）-edit
     * loadIndList
     *
     * @param {string} reportId 报表id
     * @param {string} cubeId cube的id
     * @public
     * @return {string} url
     */
    Url.loadIndList = function (reportId, cubeId) {
        return getCubesBaseUrl(reportId, cubeId) + '/inds';
    };

    /**
     * 报表新建（编辑）-edit
     * loadDimList
     *
     * @param {string} reportId 报表id
     * @param {string} cubeId cube的id
     * @public
     * @return {string} url
     */
    Url.loadDimList = function (reportId, cubeId) {
        return getCubesBaseUrl(reportId, cubeId) + '/dims';
    };

    /**
     * 报表新建（编辑）-edit-canvas
     * 初始化json
     *
     * @param {string} reportId 报表id
     * @public
     * @return {string} url
     */
    Url.initJson = function (reportId) {
        return getReportsBaseUrl(reportId) + '/json';
    };

    /**
     * 报表新建（编辑）-edit-canvas
     * 初始化Vm
     *
     * @param {string} reportId 报表id
     * @public
     * @return {string} url
     */
    Url.initVm = function (reportId) {
        return getReportsBaseUrl(reportId) + '/vm';
    };

    /**
     * 报表新建（编辑）-edit-canvas
     * 新增组件
     *
     * @param {string} reportId 报表id
     * @public
     * @return {string} url
     */
    Url.addComp = function (reportId) {
        return getReportsBaseUrl(reportId) + '/extend_area';
    };

    /**
     * 报表新建（编辑）-edit-canvas
     * 删除组件
     *
     * @param {string} reportId 报表id
     * @param {string} componentId 组件id
     * @public
     * @return {string} url
     */
    Url.deleteComp = function (reportId, componentId) {
        return getExtendAreaBaseUrl(reportId, componentId) + '?isInDesigner=true';
    };

    /**
     * 报表新建（编辑）-edit-canvas
     * 保存报表
     *
     * @param {string} reportId 报表id
     * @public
     * @return {string} url
     */
    Url.saveReport = function (reportId) {
        return getReportsBaseUrl(reportId);
    };

    /**
     * 报表新建（编辑）-edit-canvas
     * 临时保存报表的json与vm
     *
     * @param {string} reportId 报表id
     * @public
     * @return {string} url
     */
    Url.saveJsonVm = function (reportId) {
        return getReportsBaseUrl(reportId) + '/json_vm';
    };


    /**
     * 报表新建（编辑）-edit-canvas
     * 发布报表
     *
     * @param {string} reportId 报表id
     * @public
     * @return {string} url
     */
    Url.publishReport = function (reportId) {
        return getReportsBaseUrl(reportId) + '/publish';
    };

    /**
     * 报表新建（编辑）-edit-canvas
     * 预览报表
     *
     * @param {string} reportId 报表id
     */
    Url.previewReport = function (reportId) {
        return getReportsBaseUrl(reportId) + '/preview';
    };


    Url.getPublishInfo = function (reportId) {
        return getReportsBaseUrl(reportId) + '/publish_info';
    };

    /**
     * 报表新建（编辑）-edit-canvas
     * 获取组件的数据关联配置（指标、维度、切片）
     *
     * @param {string} reportId 报表id
     * @param {string} componentId 组件id
     * @public
     * @return {string} url
     */
    Url.getCompAxis = function (reportId, componentId) {
        return getExtendAreaBaseUrl(reportId, componentId);
    };

    /**
     * 报表新建（编辑）-edit-canvas
     * 添加组件的数据关联配置（指标、维度、切片）
     *
     * @param {string} reportId 报表id
     * @param {string} componentId 组件id
     * @public
     * @return {string} url
     */
    Url.addCompAxis = function (reportId, componentId) {
        return getExtendAreaBaseUrl(reportId, componentId) + '/item';
    };

    /**
     * 报表新建（编辑）-edit-canvas
     * 删除组件的数据关联配置（指标、维度、切片）
     *
     * @param {string} reportId 报表id
     * @param {string} compId 报表组件id
     * @param {string} olapId 数据项id
     * @public
     * @return {string} url
     */
    Url.deleteCompAxis = function (reportId, componentId, olapId, axisType) {
        return ''
            + getExtendAreaBaseUrl(reportId, componentId)
            + '/item/'
            + olapId
            + '/type/'
            + axisType;
    };

    /**
     * 报表新建（编辑）-edit-canvas
     * 更换组件的中维度图形种类
     *
     * @param {string} reportId 报表id
     * @param {string} compId 报表组件id
     * @param {string} chartType 图形种类
     * @public
     * @return {string} url
     */
    Url.changeCompItemChartType = function (reportId, componentId, olapId, chartType) {
        return ''
            + getExtendAreaBaseUrl(reportId, componentId)
            + '/item/'
            + olapId
            + '/chart/'
            + chartType;
    };

    /**
     * 报表新建（编辑）-edit-canvas
     * 调序组件的数据关联配置（横轴，纵轴）
     *
     * @param {string} reportId 报表id
     * @param {string} compId 报表组件id
     * @public
     * @return {string} url
     */
    Url.sortingCompDataItem = function (reportId, compId) {
        return ''
            + getExtendAreaBaseUrl(reportId, compId)
            + '/item_sorting'
    };

    /**
     * 报表新建（编辑）-edit-drag-ind-dim
     * 维度指标的相互转换
     *
     * @param {string} reportId 报表id
     * @param {string} cubeId cube的id
     * @param {string} from 从维度（dim）或指标（ind）
     * @param {string} to 到从维度（dim）或指标（ind）
     * @param {string} moveItemId 被移动的项id
     * @public
     * @return {string} url
     */
    Url.indDimSwitch = function (reportId, cubeId, from, to, moveItemId) {
        return ''
            + getCubesBaseUrl(reportId, cubeId)
            + '/'
            + from
            + '-to-'
            + to
            + '/'
            + moveItemId;
    };

    /**
     * 报表新建（编辑）-edit-drag-ind-dim
     * 维度到维度组
     *
     * @param {string} reportId 报表id
     * @param {string} cubeId cube的id
     * @param {string} groupId 维度组id
     * @public
     * @return {string} url
     */
    Url.dimToGroup = function (reportId, cubeId, groupId) {
        return getDimGroupBaseUrl(reportId,cubeId, groupId) + '/dims';
    };

    /**
     * 报表新建（编辑）-edit-drag-ind-dim
     * 对维度组中的维度进行排序
     *
     * @param {string} reportId 报表id
     * @param {string} cubeId cube的id
     * @param {string} groupId 维度组id
     * @public
     * @return {string} url
     */
    Url.sortSubDim = function (reportId, cubeId, groupId) {
        return ''
            + getDimGroupBaseUrl(reportId,cubeId, groupId)
            + '/dim_sorting';

    };

    /**
     * 报表新建（编辑）-edit-setting
     * 加载设置“数据源展示数据”的原始数据
     *
     * @param {string} reportId 报表id
     * @param {string} cubeId cube的id
     * @public
     * @return {string} url
     */
    Url.loadShowData = function (reportId, cubeId) {
        return getCubesBaseUrl(reportId, cubeId) + 'show-config';
    };

    /**
     * 报表新建（编辑）-edit-setting
     * 向后台提交“展示数据”这之后的结果
     *
     * @param {string} reportId 报表id
     * @param {string} cubeId cube的id
     * @public
     * @return {string} url
     */
    Url.submitSowData = function (reportId, cubeId) {
        return getCubesBaseUrl(reportId, cubeId) + 'show-config';
    };

    /**
     * 报表新建（编辑）-edit-setting
     * putAggregator
     *
     * @param {string} reportId 报表id
     * @param {string} cubeId cube的id
     * @param {string} indId ind的id
     * @public
     * @return {string} url
     */
    Url.putAggregator = function (reportId, cubeId, indId) {
        return ''
            + getCubesBaseUrl(reportId, cubeId)
            + '/inds/'
            + indId;
    };

    /**
     * 报表新建（编辑）-edit-setting
     * putName
     *
     * @param {string} reportId 报表id
     * @param {string} cubeId cube的id
     * @param {string} type 指标：ind，维度：dim
     * @param indDimId 指标或维度的id
     * @public
     * @return {string} url
     */
    Url.putName = function (reportId, cubeId, type, indDimId) {
        return ''
            + getCubesBaseUrl(reportId, cubeId)
            + '/'
            + type
            + 's/'
            + indDimId;
    };

    /**
     * 报表新建（编辑）-edit-setting
     * 删除维度组中的维度
     *
     * @param {string} reportId 报表id
     * @param {string} cubeId cube的id
     * @param {string} indId ind的id
     * @public
     * @return {string} url
     */
    Url.deleteSubDim = function (reportId, cubeId, groupId, dimId) {
        return ''
            + getDimGroupBaseUrl(reportId, cubeId, groupId)
            + '/level/'
            + dimId;
    };

    /**
     * 报表新建（编辑）-edit-setting
     * 删除维度组
     *
     * @param {string} reportId 报表id
     * @param {string} cubeId cube的id
     * @param {string} groupId 维度组的id
     * @public
     * @return {string} url
     */
    Url.deleteDimGroup = function (reportId, cubeId, groupId) {
        return getDimGroupBaseUrl(reportId,cubeId, groupId);
    };

    /**
     * 报表新建（编辑）-edit-setting
     * 创建维度组
     *
     * @param {string} reportId 报表id
     * @param {string} cubeId cube的id
     * @public
     * @return {string} url
     */
    Url.createDimGroup = function (reportId, cubeId) {
        return getDimGroupBaseUrl(reportId,cubeId);
    };

    /**
     * 报表新建（编辑）-edit-setting
     * 衍生指标管理-提交
     *
     * @param {string} reportId 报表id
     * @param {string} cubeId cube的id
     * @public
     * @return {string} url
     */
    Url.submitDeriveIndsInfo = function (reportId, cubeId) {
        return getCubesBaseUrl(reportId, cubeId) + '/extend_measures';
    };

    /**
     * 报表新建（编辑）-edit-setting
     * 删除指标
     *
     * @param {string} reportId 报表id
     * @param {string} cubeId cube的id
     * @public
     * @return {string} url
     */
    Url.deleteInd = function (reportId, cubeId, measureId) {
        return getCubesBaseUrl(reportId, cubeId)
            + '/extend_measures'
            + '/'
            + measureId;
    };

    /**
     * 报表新建（编辑）-edit-setting
     * 获取数据格式信息
     *
     * @param {string} reportId 报表id
     * @param {string} compId 组建区域的id
     * @public
     * @return {string} url
     */
    Url.getDataFormatList = function (reportId, compId) {
        return getExtendAreaBaseUrl(reportId, compId)
            + '/dataformat';
    };

    /**
     * 报表新建（编辑）-edit-setting
     * 获取topn设置信息
     *
     * @param {string} reportId 报表id
     * @param {string} compId 组建区域的id
     * @public
     * @return {string} url
     */
    Url.getTopnList = function (reportId, compId) {
        return getExtendAreaBaseUrl(reportId, compId)
            + '/topn';
    };
    /**
     * 报表新建（编辑）- 图形编辑 - 颜色设置
     * 获取指标提示信息
     *
     * @param {string} reportId 报表id
     * @param {string} compId 组建区域的id
     * @public
     * @return {string} url
     */
    Url.getIndColorList = function (reportId, compId) {
        return getExtendAreaBaseUrl(reportId, compId)
            + '/colorformat';
    };

    /**
     * 报表新建（编辑）- 图形编辑 - 坐标轴名字设置
     * 获取指标提示信息
     *
     * @param {string} reportId 报表id
     * @param {string} compId 组建区域的id
     * @public
     * @return {string} url
     */
    Url.getAxisTextList = function (reportId, compId) {
        return getExtendAreaBaseUrl(reportId, compId)
            + '/caption';
    };

    /**
     * 报表新建（编辑）- 图形编辑 - 个性化设置
     * 获取指标提示信息
     *
     * @param {string} reportId 报表id
     * @param {string} compId 组建区域的id
     * @public
     * @return {string} url
     */
    Url.getIndividuationData = function (reportId, compId) {
        return getExtendAreaBaseUrl(reportId, compId)
            + '/individuation';
    };

    /**
     * 报表新建（编辑）- 表格编辑 - 文本对齐设置
     * 获取文本对齐相关信息
     *
     * @param {string} reportId 报表id
     * @param {string} compId 组建区域的id
     * @public
     * @return {string} url
     */
    Url.getTextAlignList = function (reportId, compId) {
        return getExtendAreaBaseUrl(reportId, compId)
            + '/textAlign';
    };

    /**
     * 报表新建（编辑）- 表格编辑 - 获取跳转设置  列-表设置数据
     * 获取文本对齐相关信息
     *
     * @param {string} reportId 报表id
     * @param {string} compId 组建区域的id
     * @public
     * @return {string} url
     */
    Url.getColumnLinkPlaneList = function (reportId, compId) {
        return getExtendAreaBaseUrl(reportId, compId)
            + '/olaplink';
    };

    /**
     * 报表新建（编辑）- 表格编辑 - 获取跳转设置  列-表设置数据
     * 删除表哥关联
     *
     * @param {string} reportId 报表id
     * @param {string} compId 组件区域的id
     * @param {string} linkId 关联Id
     * @public
     * @return {string} url
     */
    Url.delColumnLink = function (reportId, compId, linkId) {
        return getExtendAreaBaseUrl(reportId, compId)
            + '/olaplink/' + linkId;
    };

    /**
     * 报表新建（编辑）- 表格编辑 - 参数-表设置数据
     * 获取文本对齐相关信息
     *
     * @param {string} reportId 报表id
     * @param {string} compId 组建区域的id
     * @public
     * @return {string} url
     */
    Url.getParamSetList = function (reportId, compId) {
        return this.getColumnLinkPlaneList(reportId, compId) + '/paramMapping'
    };

    /**
     * 报表新建（编辑）- 表格编辑 - 获取分页设置信息
     * 获取文本对齐相关信息
     *
     * @param {string} reportId 报表id
     * @param {string} compId 组建区域的id
     * @public
     * @return {string} url
     */
    Url.getPaginationData = function (reportId, compId) {
        return getExtendAreaBaseUrl(reportId, compId)
            + '/pagination';
    };

    /**
     * 报表新建（编辑）- 平面表格编辑 - 过滤设置
     * 获取文本对齐相关信息
     *
     * @param {string} reportId 报表id
     * @param {string} compId 组建区域的id
     * @public
     * @return {string} url
     */
    Url.getFieldFilterInfo = function (reportId, compId, itemId) {
        return getExtendAreaBaseUrl(reportId, compId)
            + '/item/' + itemId + '/condition';
    };

    /**
     * 报表新建（编辑）-edit-setting
     * 获取双坐标轴设置信息
     *
     * @param {string} reportId 报表id
     * @param {string} compId 组建区域的id
     * @public
     * @return {string} url
     */
    Url.getAxisList = function (reportId, compId) {
        return getExtendAreaBaseUrl(reportId, compId)
            + '/position';
    };

    /**
     * 报表新建（编辑）-edit-setting
     * 获取topn设置信息
     *
     * @param {string} reportId 报表id
     * @param {string} compId 组建区域的id
     * @public
     * @return {string} url
     */
    Url.getAxisList = function (reportId, compId) {
        return getExtendAreaBaseUrl(reportId, compId)
            + '/position';
    };

    /**
     * 报表新建（编辑）-edit-setting
     * 获取指标提示信息
     *
     * @param {string} reportId 报表id
     * @param {string} compId 组建区域的id
     * @public
     * @return {string} url
     */
    Url.getNormInfoDepict = function (reportId, compId) {
        return getExtendAreaBaseUrl(reportId, compId)
            + '/tooltips';
    };

    /**
     * 报表新建（编辑）-edit-setting
     * 表格-其他设置
     *
     * @param {string} reportId 报表id
     * @param {string} compId 组建区域的id
     * @public
     * @return {string} url
     */
    Url.getTableOtherSetting = function (reportId, compId) {
        return getExtendAreaBaseUrl(reportId, compId)
            + '/othersetting';
    };

    /**
     * 参数维度获取
     *
     * @param {string} reportId 报表id
     * @public
     * @return {string} url
     */
    Url.getParameterDim = function (reportId) {
        return getParameterDim(reportId)
            + '/params';
    };

    /**
     * 参数维度提交
     *
     * @param {string} reportId 报表id
     * @public
     * @return {string} url
     */
    Url.getParameterDimData = function (reportId) {
        return getParameterDimData(reportId)
            + '/params';
    };

    /**
     * 更换皮肤
     *
     * @param {string} reportId 报表id
     * @param {string} type 皮肤类型
     * @public
     * @return {string} url
     */
    Url.getSkinType = function (reportId, type) {
        return getSkinType(reportId)
            + '/theme/' + type;
    };
    /**
     * 更换报表名称
     *
     * @param {string} reportId 报表id
     * @public
     * @return {string} url
     */
    Url.editReportName = function (reportId) {
        return getReportName(reportId)
            + '/name';
    };
    /**
     * 保存更换报表名称
     *
     * @param {string} reportId 报表id
     * @param {string} newReportName 新报表名称
     * @public
     * @return {string} url
     */
    Url.saveEditReportName = function (reportId, newReportName) {
        return getSaveReportName(reportId)
            + '/name/' + newReportName;
    };

    //--------------------------------
    // 固定报表
    //--------------------------------

    /**
     * 获取任务管理列表
     *
     * @param {string} reportId 报表id
     * @public
     * @return {string} url
     */
    Url.getFixReportTaskMgrList = function (reportId) {
        return getReportsBaseUrl(reportId)
            + '/regular/tasks';
    };

    /**
     * 启动任务
     *
     * @param {string} reportId 报表id
     * @param {string} taskId 任务id
     * @param {string} taskStatus 任务状态
     * @public
     * @return {string} url
     */
    Url.startTask = function (reportId, taskId, taskStatus) {
        return getReportsBaseUrl(reportId)
            + '/regular/tasks/' + taskId + '/operation/' + taskStatus;
    };

    /**
     * 删除任务
     *
     * @param {string} reportId 报表id
     * @param {string} taskId 任务id
     * @public
     * @return {string} url
     */
    Url.delTask = function (reportId, taskId) {
        return getReportsBaseUrl(reportId)
            + '/regular/tasks/' + taskId;
    };

    /**
     * 保存任务
     *
     * @param {string} reportId 报表id
     * @public
     * @return {string} url
     */
    Url.saveTaskInfo = function (reportId) {
        return getReportsBaseUrl(reportId)
            + '/regular/task';
    };

    /**
     * 获取任务设置信息
     *
     * @param {string} reportId 报表id
     * @param {string} taskId 任务id
     * @public
     * @return {string} url
     */
    Url.getTaskInfo = function (reportId, taskId) {
        return getReportsBaseUrl(reportId)
            + '/regular/tasks/' + taskId;
    };

    /**
     * 获取参数
     *
     * @param {string} reportId 报表id
     * @param {string} taskId 任务id
     * @public
     * @return {string} url
     */
    Url.getParamData = function (reportId) {
        return getReportsBaseUrl(reportId)
            + '/allparams';
    };

    /**
     * 获取树结构
     *
     * @param {string} reportId 报表id
     * @param {string} paramId 参数id
     * @public
     * @return {string} url
     */
    Url.getFixReportMgrTree = function (reportId, paramId) {
        return getReportsBaseUrl(reportId)
            + '/params/' + paramId + '/tree';
    };

    return Url;
});