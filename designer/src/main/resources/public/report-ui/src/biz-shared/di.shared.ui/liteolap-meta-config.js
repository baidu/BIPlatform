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
    var LITEOLAP_META_CONFIG = $namespace().LiteOlapMetaConfig = 
        inheritsObject(INTERACT_ENTITY);
    var LITEOLAP_META_CONFIG_CLASS = LITEOLAP_META_CONFIG.prototype;
    
    /**
     * 定义
     */
    LITEOLAP_META_CONFIG_CLASS.DEF = {
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
    LITEOLAP_META_CONFIG_CLASS.$createModelInitOpt = function (options) {
        return { reportType: options.reportType };
    };
    
    /**
     * 创建View
     *
     * @private
     * @param {Object} options 参数
     */
    LITEOLAP_META_CONFIG_CLASS.$createView = function (options) {
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
    LITEOLAP_META_CONFIG_CLASS.init = function () {
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
    LITEOLAP_META_CONFIG_CLASS.dispose = function () {
        this._uOlapMetaSelector && this._uOlapMetaSelector.dispose();
        this.getModel() && this.getModel().dispose();
        LITEOLAP_META_CONFIG.superClass.dispose.call(this);
    };

    /**
     * 从后台获取数据并渲染
     *
     * @public
     */
    LITEOLAP_META_CONFIG_CLASS.sync = function () {
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
                inEditMode: false
            },
            this.$di('getEvent')
        );
    };

    // 获取liteOlap的指标选择下拉框数据
    LITEOLAP_META_CONFIG_CLASS.syncLiteOlapInds = function () {
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
                inEditMode: false,
                // 使用chart图形的id
                componentId: this.$di('getId').split('.')[1]
            },
            this.$di('getEvent')
        );
    };

    /**
     * 清空视图
     * 
     * @public
     */
    LITEOLAP_META_CONFIG_CLASS.clear = function () {  
        // TODO
    };

    /**
     * 渲染主体
     * 
     * @protected
     */
    LITEOLAP_META_CONFIG_CLASS.$renderMain = function (data, ejsonObj, options) {
        var me = this;
        var el = this.$di('getEl');

        var imme = this._sSubmitMode == 'IMMEDIATE';
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
    LITEOLAP_META_CONFIG_CLASS.$renderLiteOlapMain = function (data, ejsonObj, options) {
        var me = this;
        var el = this.$di('getEl');

        var imme = this._sSubmitMode == 'IMMEDIATE';
        var model = this.getModel();

        this._uOlapMetaSelector.$di(
            'setData', 
            {
                indList: ejsonObj.data['inds'],
                selectedInds: ejsonObj.data['currentInds'],
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
        /**
             * 提交事件
             *
             * @event
             */
        this.$di('dispatchEvent', 'submit');
    };
    /**
     * 选择完成
     *
     * @protected
     */
    LITEOLAP_META_CONFIG_CLASS.$handleSelected = function () {
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
        }
    };

    /**
     * liteOlap的指标下拉框的change事件不需要发起selectInd请求，只需发起submit提交请求即可
     * 
     * @protected
     */
    LITEOLAP_META_CONFIG_CLASS.$handleChange = function (wrap) {
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
        }
    };

    /**
     * 解禁操作
     *
     * @protected
     */
    LITEOLAP_META_CONFIG_CLASS.enable = function () {
        this._uOlapMetaSelector && this._uOlapMetaSelector.$di('enable');
        LITEOLAP_META_CONFIG.superClass.enable.call(this);
    };

    /**
     * 禁用操作
     *
     * @protected
     */
    LITEOLAP_META_CONFIG_CLASS.disable = function () {
        this._uOlapMetaSelector && this._uOlapMetaSelector.$di('disable');
        LITEOLAP_META_CONFIG.superClass.disable.call(this);
    };

    /**
     * 获取元数据初始化错误处理
     * 
     * @protected
     */
    LITEOLAP_META_CONFIG_CLASS.$handleMetaError = function () {
        this.clear();
        DIALOG.errorAlert();
    };

    /**
     * 元数据拖拽错误处理
     * 
     * @protected
     */
    LITEOLAP_META_CONFIG_CLASS.$handleSelectError = function () {
        DIALOG.errorAlert();
    };

})();