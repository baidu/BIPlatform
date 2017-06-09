/**
 * di.shared.ui.OlapMetaConfig
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    多维分析报表原数据选择面板
 * @author:  sushuang(sushuang)
 * @depend:  xui, xutil
 */

$namespace('di.shared.ui');

(function () {
    
    //------------------------------------------
    // 引用 
    //------------------------------------------

    var DICT = di.config.Dict;
    var UTIL = di.helper.Util;
    var DIALOG = di.helper.Dialog;
    var inheritsObject = xutil.object.inheritsObject;
    var addClass = xutil.dom.addClass;
    var ecuiDispose = UTIL.ecuiDispose;
    var extend = xutil.object.extend;
    var assign = xutil.object.assign;
    var q = xutil.dom.q;
    var bind = xutil.fn.bind;
    var objKey = xutil.object.objKey;
    var template = xutil.string.template;
    var ecuiCreate = UTIL.ecuiCreate;
    var LINKED_HASH_MAP = xutil.LinkedHashMap;
    var getUID = xutil.uid.getIncreasedUID;
    var INTERACT_ENTITY = di.shared.ui.InteractEntity;
    
    //------------------------------------------
    // 类型声明 
    //------------------------------------------

    /**
     * 元数据（指标维度）条件拖动选择
     * 
     * @class
     * @extends xui.XView
     * @param {Object} options
     * @param {Object} options.reportType 类型，
     *          TABLE(默认)或者CHART
     * @param {string} options.submitMode 提交模式，可选值为
     *      'IMMEDIATE'（输入后立即提交，默认）
     *      'CONFIRM'（按确定按钮后提交）
     * @param {boolean} options.needShowCalcInds 计算列是否作为指标
     */
    var OLAP_META_CONFIG = $namespace().OlapMetaConfig = 
        inheritsObject(INTERACT_ENTITY);
    var OLAP_META_CONFIG_CLASS = OLAP_META_CONFIG.prototype;
    
    /**
     * 定义
     */
    OLAP_META_CONFIG_CLASS.DEF = {
        // 暴露给interaction的api
        exportHandler: {
            sync: { datasourceId: 'DATA' },
            syncLiteOlapInds: { datasourceId: 'LITEOLAP_INDS_DATA' },
            clear: {}
        },
        // 主元素的css
        className: 'olap-meta-config',
        // model配置
        model: {
            clzPath: 'di.shared.model.OlapMetaConfigModel'
        }
    };

    //------------------------------------------
    // 方法
    //------------------------------------------

    /**
     * 创建Model初始化参数
     *
     * @private
     * @param {Object} options 参数
     */
    OLAP_META_CONFIG_CLASS.$createModelInitOpt = function (options) {
        return { reportType: options.reportType };
    };
    
    /**
     * 创建View
     *
     * @private
     * @param {Object} options 参数
     */
    OLAP_META_CONFIG_CLASS.$createView = function (options) {
        /**
         * 是否计算列作为指标显示
         *
         * @type {boolean}
         * @private
         */
        this._bNeedShowCalcInds = options.needShowCalcInds || false;
        /**
         * 支持外部配置的datasourceId设置
         *
         * @type {Object}
         * @private
         */
        var did = this._oDatasourceId = options.datasourceId || {};
        did.DATA = did.DATA || 'DATA';
        did.SELECT = did.SELECT || 'SELECT';
        /**
         * 提交模式
         * 
         * @type {string}
         * @private 
         */
        this._sSubmitMode = options.submitMode;

        this._uOlapMetaSelector = this.$di('vuiCreate', 'main');
    };
    
    /**
     * 初始化
     *
     * @public
     */
    OLAP_META_CONFIG_CLASS.init = function () {
        // 事件绑定
        this.getModel().attach(
            ['sync.preprocess.DATA', this.$syncDisable, this, 'DATA'],
            ['sync.result.DATA', this.$renderMain, this],
            ['sync.error.DATA', this.$handleMetaError, this],
            ['sync.complete.DATA', this.$syncEnable, this, 'DATA']
        );
        this.getModel().attach(
            ['sync.preprocess.LITEOLAP_INDS_DATA', this.$syncDisable, this, 'LITEOLAP_INDS_DATA'],
            ['sync.result.LITEOLAP_INDS_DATA', this.$renderLiteOlapMain, this],
            ['sync.error.LITEOLAP_INDS_DATA', this.$handleMetaError, this],
            ['sync.complete.LITEOLAP_INDS_DATA', this.$syncEnable, this, 'LITEOLAP_INDS_DATA']
        );
        this.getModel().attach(
            ['sync.preprocess.SELECT', this.$syncDisable, this, 'SELECT'],
            ['sync.result.SELECT', this.$handleSelected, this],
            ['sync.error.SELECT', this.$handleSelectError, this],
            ['sync.complete.SELECT', this.$syncEnable, this, 'SELECT']
        );
        this.getModel().attach(
            ['sync.preprocess.LIST_SELECT', this.$syncDisable, this, 'LIST_SELECT'],
            ['sync.result.LIST_SELECT', this.$handleSelected, this],
            ['sync.error.LIST_SELECT', this.$handleSelectError, this],
            ['sync.complete.LIST_SELECT', this.$syncEnable, this, 'LIST_SELECT']
        );
        this._uOlapMetaSelector.$di(
            'addEventListener',
            'change', 
            this.$handleChange, 
            this
        );

        this._uOlapMetaSelector.$di('init');
        this.getModel().init();
    };

    /**
     * @override
     */
    OLAP_META_CONFIG_CLASS.dispose = function () {
        this._uOlapMetaSelector && this._uOlapMetaSelector.dispose();
        this.getModel() && this.getModel().dispose();
        OLAP_META_CONFIG.superClass.dispose.call(this);
    };

    /**
     * 从后台获取数据并渲染
     *
     * @public
     */
    OLAP_META_CONFIG_CLASS.sync = function () {
        var datasourceId = this._oDatasourceId.DATA;

        // 视图禁用
        /*
        var diEvent = this.$di('getEvent');
        var vd = diEvent.viewDisable;
        vd && this.getModel().attachOnce(
            ['sync.preprocess.' + datasourceId, vd.disable],
            ['sync.complete.' + datasourceId, vd.enable]
        );*/

        // 请求后台
        this.$sync(
            this.getModel(),
            datasourceId,
            {
                needShowCalcInds: this._bNeedShowCalcInds,
                inEditMode: false,
                componentId: this.$di('getId').split('.')[1]
            },
            this.$di('getEvent')
        );
    };

    // 获取liteOlap的指标选择下拉框数据
    OLAP_META_CONFIG_CLASS.syncLiteOlapInds = function () {
        var datasourceId = 'LITEOLAP_INDS_DATA';
        // 视图禁用
        /*
        var diEvent = this.$di('getEvent');
        var vd = diEvent.viewDisable;
        vd && this.getModel().attachOnce(
            ['sync.preprocess.' + datasourceId, vd.disable],
            ['sync.complete.' + datasourceId, vd.enable]
        );*/

        // 请求后台
        this.$sync(
            this.getModel(),
            datasourceId,
            {
                needShowCalcInds: this._bNeedShowCalcInds,
                inEditMode: false
            },
            this.$di('getEvent')
        );
    };

    /**
     * 清空视图
     * 
     * @public
     */
    OLAP_META_CONFIG_CLASS.clear = function () {  
        // TODO
    };

    /**
     * 渲染主体
     * 
     * @protected
     */
    OLAP_META_CONFIG_CLASS.$renderMain = function (data, ejsonObj, options) {
        var me = this;
        var el = this.$di('getEl');

        var imme = this._sSubmitMode == 'IMMEDIATE';
            // 该标识是用以区分是否需要禁止行列拖走最后一个元素
            imme = true ;
        var model = this.getModel();

        this._uOlapMetaSelector.$di(
            'setData', 
            {
                inddim: model.getIndDim(),
                selLineDataWrap: model.getSelLineWrap(),
                seriesCfg: model.getSeriesCfg(),
                model: model,
                rule: {
                    forbidColEmpty: imme,
                    forbidRowEmpty: imme
                }
            },
            { diEvent: this.$diEvent(options) }
        );
        
        // 更新控件的元数据状态
        this._uOlapMetaSelector.$di(
            'updateData',
            this.getModel().getUpdateData()
        );
    };

    /**
     * 渲染liteOlap主体
     * 
     * @protected
     */
    OLAP_META_CONFIG_CLASS.$renderLiteOlapMain = function (data, ejsonObj, options) {
        var me = this;
        var el = this.$di('getEl');

        var imme = this._sSubmitMode == 'IMMEDIATE';
        var model = this.getModel();

        this._uOlapMetaSelector.$di(
            'setData', 
            {
                indList: model.getLiteOlapIndList(),
                model: model,
                selLineName: 'COLUMN',
                renderType: 'liteOlap',
                rule: {
                    forbidColEmpty: imme,
                    forbidRowEmpty: imme
                }
            },
            { diEvent: this.$diEvent(options) }
        );
        
        // 更新控件的元数据状态
        this._uOlapMetaSelector.$di(
            'updateData',
            this.getModel().getUpdateData()
        );
    };
    /**
     * 选择完成
     *
     * @protected
     */
    OLAP_META_CONFIG_CLASS.$handleSelected = function () {
        // 更新控件的元数据状态
        this._uOlapMetaSelector.$di(
            'updateData',
            this._mModel.getUpdateData()
        );

        if (this._sSubmitMode == 'IMMEDIATE') {
            /**
             * 提交事件
             *
             * @event
             */
            this.$di('dispatchEvent', 'submit');
        }else{
            // 指标和维度经拖拽选定后，需要和后台同步一下元数据，以防前后端状态不一致。
            this.sync();
        }
    };

    /**
     * 获取元数据选择处理
     * 
     * @protected
     */
    OLAP_META_CONFIG_CLASS.$handleChange = function (wrap) {
        var didSel = this._oDatasourceId.SELECT;

        this.$sync(
            this._mModel,
            didSel,
            null,
            null,
            didSel == 'LIST_SELECT' 
                ? {
                    selectedIndNames: wrap,
                    componentId: this.$di('getId').split('.')[1]
                }
                : {
                    changeWrap: this._mModel.diffSelected(wrap),
                    needShowCalcInds: this._bNeedShowCalcInds,
                    componentId: this.$di('getId').split('.')[1]
                }
        );
    };

    /**
     * 解禁操作
     *
     * @protected
     */
    OLAP_META_CONFIG_CLASS.enable = function () {
        this._uOlapMetaSelector && this._uOlapMetaSelector.$di('enable');
        OLAP_META_CONFIG.superClass.enable.call(this);
    };

    /**
     * 禁用操作
     *
     * @protected
     */
    OLAP_META_CONFIG_CLASS.disable = function () {
        this._uOlapMetaSelector && this._uOlapMetaSelector.$di('disable');
        OLAP_META_CONFIG.superClass.disable.call(this);
    };

    /**
     * 获取元数据初始化错误处理
     * 
     * @protected
     */
    OLAP_META_CONFIG_CLASS.$handleMetaError = function () {
        this.clear();
        DIALOG.errorAlert();
    };

    /**
     * 元数据拖拽错误处理
     * 
     * @protected
     */
    OLAP_META_CONFIG_CLASS.$handleSelectError = function () {
        DIALOG.errorAlert();
    };

})();