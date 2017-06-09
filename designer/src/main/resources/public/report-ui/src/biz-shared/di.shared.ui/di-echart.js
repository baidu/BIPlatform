/**
 * di.shared.ui.DIChart
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    DI 图视图组件
 * @author:  sushuang(sushuang)
 * @depend:  xui, xutil
 */

$namespace('di.shared.ui');

(function () {
    
    //------------------------------------------
    // 引用 
    //------------------------------------------

    var UTIL = di.helper.Util;
    var URL = di.config.URL;
    var inheritsObject = xutil.object.inheritsObject;
    var q = xutil.dom.q;
    var bind = xutil.fn.bind;
    var download = UTIL.download;
    var foreachDo = UTIL.foreachDo;
    var DIALOG = di.helper.Dialog;
    var LANG = di.config.Lang;
    var INTERACT_ENTITY = di.shared.ui.InteractEntity;
        
    //------------------------------------------
    // 类型声明 
    //------------------------------------------

    /**
     * DI 图视图组件
     * 
     * @class
     * @extends xui.XView
     * @param {Object} options
     */
    var DI_ECHART = $namespace().DIEChart =
        inheritsObject(INTERACT_ENTITY);
    var DI_ECHART_CLASS = DI_ECHART.prototype;
    
    //------------------------------------------
    // 常量 
    //------------------------------------------

    /**
     * 暴露给interaction的api
     */
    DI_ECHART_CLASS.EXPORT_HANDLER = {
        sync: { datasourceId: 'DATA' },
        syncX: { datasourceId: 'X_DATA' },
        syncLiteOlapChart: { datasourceId: 'LITEOLAPCHART_DATA' },
        syncS: { datasourceId: 'S_DATA' },
        syncSAdd: { datasourceId: 'S_ADD_DATA' },
        syncSRemove: { datasourceId: 'S_REMOVE_DATA' },
        clear: {}
    };

    /**
     * 定义
     */
    DI_ECHART_CLASS.DEF = {
        // 暴露给interaction的api
        exportHandler: {
            sync: { datasourceId: 'DATA' },
            syncX: { datasourceId: 'X_DATA' },
            syncLiteOlapChart: { datasourceId: 'LITEOLAPCHART_DATA' },
            syncS: { datasourceId: 'S_DATA' },
            syncSAdd: { datasourceId: 'S_ADD_DATA' },
            syncSRemove: { datasourceId: 'S_REMOVE_DATA' },
            clear: {}
        },
        // 主元素的css
        className: 'di-chart',
        // model配置
        model: {
            clzPath: 'di.shared.model.DIEChartModel'
        }
    };


    //------------------------------------------
    // 方法
    //------------------------------------------

    /**
     * 创建View
     *
     * @private
     * @param {Object} options 参数
     */
    DI_ECHART_CLASS.$createView = function (options) {
        this._uChart = this.$di('vuiCreate', 'mainChart');
    };

    /**
     * 初始化
     *
     * @public
     */
    DI_ECHART_CLASS.init = function () {
        var key;
        var exportHandler = this.DEF.exportHandler;
        this._uChart.attach('chartClick', this.$chartClick, this);
        this._uChart.attach('changeRadioButton', this.$changeRadioButton, this);
        // 事件绑定
        for (key in exportHandler) {
            var id = exportHandler[key].datasourceId;
            this.getModel().attach(
                ['sync.preprocess.' + id, this.$syncDisable, this, id],
                ['sync.result.' + id, this.$renderMain, this],
                ['sync.result.' + id, this.$handleDataLoaded, this],
                ['sync.error.' + id, this.$handleDataError, this],
                ['sync.complete.' + id, this.$syncEnable, this, id]
            );
        }
        key = 'OFFLINE_DOWNLOAD';
        this.getModel().attach(
            ['sync.preprocess.' + key, this.$syncDisable, this, key],
            ['sync.error.' + key, this.$handleOfflineDownloadError, this],
            ['sync.complete.' + key, this.$syncEnable, this, key]
        );
        this.getModel().attach(
            ['sync.preprocess.CHANGE_RADIOBUTTON', this.$syncDisable, this, 'CHANGE_RADIOBUTTON'],
            ['sync.result.CHANGE_RADIOBUTTON', this.$renderMain, this],
            ['sync.result.CHANGE_RADIOBUTTON', this.$handleDataLoaded, this],
            ['sync.error.CHANGE_RADIOBUTTON', this.$handleDataError, this],
            ['sync.complete.CHANGE_RADIOBUTTON', this.$syncEnable, this, 'CHANGE_RADIOBUTTON']
        );
        foreachDo(
            [
                this.getModel(),
                this._uChart
            ], 
            'init'
        );
    };

    /**
     * @override
     */
    DI_ECHART_CLASS.dispose = function () {
        this._uChart && this._uChart.$di('dispose');
        DI_ECHART.superClass.dispose.call(this);
    };

    /**
     * 从后台获取数据并渲染
     *
     * @public
     * @param {Object} options 参数
     */
    (function () {
        var exportHandler = DI_ECHART_CLASS.DEF.exportHandler;
        for (var funcName in exportHandler) {
            DI_ECHART_CLASS[funcName] = getSyncMethod(
                exportHandler[funcName].datasourceId
            );
        }
        function getSyncMethod(datasourceId) {
            return function (options) {
                // 视图禁用
                /*
                var diEvent = this.$di('getEvent');
                var vd = diEvent.viewDisable;
                vd && this.getModel().attachOnce(
                    ['sync.preprocess.' + datasourceId, vd.disable],
                    ['sync.complete.' + datasourceId, vd.enable]
                );*/
                options = options || {};
                options.componentId = this.$di('getId').split('.')[1];
                // 请求后台
                this.$sync(
                    this.getModel(),
                    datasourceId,
                    options,
                    this.$di('getEvent')
                );
            };
        }
    })();

    /**
     * 清空视图
     * 
     * @public
     */
    DI_ECHART_CLASS.clear = function () {
        this._uChart && this._uChart.$di('setData');
    };

    /**
     * 渲染主体
     * 
     * @protected
     */
    DI_ECHART_CLASS.$renderMain = function (data, ejsonObj, options) {
        this._uChart.$di(
            'setData', 
            this.getModel().getChartData(),
            { diEvent: this.$diEvent(options) }
        );
        /**
         * 渲染事件
         *
         * @event
         */
        this.$di('dispatchEvent', this.$diEvent('rendered', options));
    };

    /**
     * 窗口改变后重新计算大小
     *
     * @public
     */
    DI_ECHART_CLASS.resize = function () {
    };

    /**
     * 解禁操作
     *
     * @protected
     */
    DI_ECHART_CLASS.enable = function () {
        foreachDo(
            [this._uChart],
            'enable'
        );
        DI_ECHART.superClass.enable.call(this);
    };

    /**
     * 禁用操作
     *
     * @protected
     */
    DI_ECHART_CLASS.disable = function () {
        foreachDo(
            [this._uChart, this._uDownloadBtn, this._uOfflineDownloadBtn], 
            'disable'
        );
        DI_ECHART.superClass.disable.call(this);
    };

    /**
     * 下载操作
     *
     * @protected
     */
    DI_ECHART_CLASS.$handleDownload = function (wrap) {
        var commonParamGetter = this.$di('getCommonParamGetter');

        var url = URL('OLAP_CHART_DOWNLOAD') 
            + '?' + commonParamGetter();
        download(url, null, true);

        // 对于下载，不进行reportTemplateId控制，直接打开
        commonParamGetter.update();
    };

    /**
     * 离线下载操作
     *
     * @protected
     */
    DI_ECHART_CLASS.$handleOfflineDownload = function () {
        var val = this._uOfflineDownloadBtn.getValue() || {};
        this.$sync(
            this.getModel(),
            'OFFLINE_DOWNLOAD',
            { email: val.email }
        );
    };

    /**
     * 数据加载成功
     * 
     * @protected
     */
    DI_ECHART_CLASS.$handleDataLoaded = function  (data, ejsonObj, options) {
        /**
         * 数据成功加载事件（分datasourceId）
         *
         * @event
         */
        this.$di(
            'dispatchEvent', 
            this.$diEvent('dataloaded.' + options.datasourceId, options)
        );

        /**
         * 数据成功加载事件
         *
         * @event
         */
        this.$di('dispatchEvent', this.$diEvent('dataloaded', options));
    };

    /**
     * 获取数据错误处理
     * 
     * @protected
     */
    DI_ECHART_CLASS.$handleDataError = function (status, ejsonObj, options) {

        // 设置空视图
        this.clear();

        /**
         * 渲染事件
         *
         * @event
         */
        this.$di('dispatchEvent', this.$diEvent('rendered', options));
        /**
         * 数据加载失败事件
         *
         * @event
         */
        this.$di('dispatchEvent', this.$diEvent('dataerror', options));
        // 暂时注释掉报错提示，用以解决当商桥嵌入报表时页签快速切换的频繁弹窗问题
        // DIALOG.alert('获取图形数据异常：' + ejsonObj.statusInfo);
    };

    /**
     * 离线下载错误处理
     * 
     * @protected
     */
    DI_ECHART_CLASS.$handleOfflineDownloadError = function (status, ejsonObj, options) {
        DIALOG.alert(LANG.SAD_FACE + LANG.OFFLINE_DOWNLOAD_FAIL);
    };

    /**
     * 图形选中
     * TODO:怎么让图形选中，执行此方法
     *
     * @protected
     */
    DI_ECHART_CLASS.$chartClick = function (options) {

//        var outParam = this.$di('getDef').outParam;
//        if (!outParam) {
//            return;
//        }
//        var params = { uniqueName: options.args.param.uniqueName };
//        params[outParam.dim] = outParam.level;
        var outParam = this.$di('getDef').outParam;
        if (!outParam) {
            return;
        }
        // 整理后端需要的数据格式
        // {
        //      6e72140667f37b984d9764f5aca6b6cb:[dim_trade_trade_l1].[广播通信]
        //      6e72140667f37b984d9764f5aca6b6cb_level:0
        // }
        var params = {};
        params[outParam.dimId] = '[' + options.dimMap[outParam.dimId] + '].[' + options.name + ']';
        params[outParam.dimId + '_level'] = outParam.level;
        this.$di(
            'dispatchEvent',
            'rowselect',
            [params]
        );
    };

    /**
     * 图形中切换指标
     *
     * @protected
     */
    DI_ECHART_CLASS.$changeRadioButton = function (index) {
        var componentId = this.$di('getId').split('.')[1];
        var paramList = {
            componentId: componentId,
            index: index
        };
        this.$sync(
            this.getModel(),
            'CHANGE_RADIOBUTTON',
            paramList,
            null,
            this.$di('getEvent')
        );
    };

})();