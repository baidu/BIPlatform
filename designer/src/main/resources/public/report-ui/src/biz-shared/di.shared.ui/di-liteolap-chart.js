/**
 * di.shared.ui.DILiteOlapChart
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
    var DI_LITEOLAP_CHART = $namespace().DILiteOlapChart = 
        inheritsObject(INTERACT_ENTITY);
    var DI_LITEOLAP_CHART_CLASS = DI_LITEOLAP_CHART.prototype;
    
    //------------------------------------------
    // 常量 
    //------------------------------------------

    /**
     * 暴露给interaction的api
     */
    DI_LITEOLAP_CHART_CLASS.EXPORT_HANDLER = {
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
    DI_LITEOLAP_CHART_CLASS.DEF = {
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
    DI_LITEOLAP_CHART_CLASS.$createView = function (options) {
        var el = this.$di('getEl');

        this._uChart = this.$di('vuiCreate', 'mainChart');

        // 下载按钮
        this._uDownloadBtn = this.$di('vuiCreate', 'download');

        // 离线下载
        this._uOfflineDownloadBtn = this.$di('vuiCreate', 'offlineDownload');
    };

    /**
     * 初始化
     *
     * @public
     */
    DI_LITEOLAP_CHART_CLASS.init = function () {
        var key;
        var exportHandler = this.DEF.exportHandler;
        
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
        this._uDownloadBtn && (
            this._uDownloadBtn.onclick = bind(this.$handleDownload, this)
        );
        this._uOfflineDownloadBtn && (
            this._uOfflineDownloadBtn.attach('confirm', this.$handleOfflineDownload, this)
        );

        foreachDo(
            [
                this.getModel(),
                this._uChart, 
                this._uDownloadBtn,
                this._uOfflineDownloadBtn
            ], 
            'init'
        );
    };

    /**
     * @override
     */
    DI_LITEOLAP_CHART_CLASS.dispose = function () {
        this._uChart && this._uChart.$di('dispose');
        DI_LITEOLAP_CHART.superClass.dispose.call(this);
    };

    /**
     * 从后台获取数据并渲染
     *
     * @public
     * @param {Object} options 参数
     */
    (function () {
        var exportHandler = DI_LITEOLAP_CHART_CLASS.DEF.exportHandler;
        for (var funcName in exportHandler) {
            DI_LITEOLAP_CHART_CLASS[funcName] = getSyncMethod(
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
    DI_LITEOLAP_CHART_CLASS.clear = function () {  
        this._uChart && this._uChart.$di('setData');
    };

    /**
     * 渲染主体
     * 
     * @protected
     */
    DI_LITEOLAP_CHART_CLASS.$renderMain = function (data, ejsonObj, options) {
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
    DI_LITEOLAP_CHART_CLASS.resize = function () {
    };

    /**
     * 解禁操作
     *
     * @protected
     */
    DI_LITEOLAP_CHART_CLASS.enable = function () {
        foreachDo(
            [this._uChart, this._uDownloadBtn, this._uOfflineDownloadBtn], 
            'enable'
        );
        DI_LITEOLAP_CHART.superClass.enable.call(this);
    };    

    /**
     * 禁用操作
     *
     * @protected
     */
    DI_LITEOLAP_CHART_CLASS.disable = function () {
        foreachDo(
            [this._uChart, this._uDownloadBtn, this._uOfflineDownloadBtn], 
            'disable'
        );
        DI_LITEOLAP_CHART.superClass.disable.call(this);
    };    

    /**
     * 下载操作
     *
     * @protected
     */
    DI_LITEOLAP_CHART_CLASS.$handleDownload = function (wrap) {
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
    DI_LITEOLAP_CHART_CLASS.$handleOfflineDownload = function () {
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
    DI_LITEOLAP_CHART_CLASS.$handleDataLoaded = function  (data, ejsonObj, options) {
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
    DI_LITEOLAP_CHART_CLASS.$handleDataError = function (status, ejsonObj, options) {

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
        DIALOG.alert('获取图形数据异常：' + ejsonObj.statusInfo);
    };

    /**
     * 离线下载错误处理
     * 
     * @protected
     */
    DI_LITEOLAP_CHART_CLASS.$handleOfflineDownloadError = function (status, ejsonObj, options) {
        DIALOG.alert(LANG.SAD_FACE + LANG.OFFLINE_DOWNLOAD_FAIL);
    };

})();