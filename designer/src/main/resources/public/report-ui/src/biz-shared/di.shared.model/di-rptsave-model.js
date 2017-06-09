/**
 * di.shared.model.DITableModel
 * Copyright 2013 Baidu Inc. All rights reserved.
 *
 * @file:    DI 模板镜像操作组件
 * @author:  lizhantong(lztlovely@126.com)
 * @depend:  xui, xutil
 */

$namespace('di.shared.model');

(function () {
    
    //------------------------------------------
    // 引用
    //------------------------------------------

    var URL = di.config.URL;
    var UTIL = di.helper.Util;
    var inheritsObject = xutil.object.inheritsObject;
    var wrapArrayParam = xutil.url.wrapArrayParam;
    var extend = xutil.object.extend;
    var logError = UTIL.logError;
    var getUID = xutil.uid.getUID;
    var XDATASOURCE = xui.XDatasource;

    //------------------------------------------
    // 类型声明
    //------------------------------------------

    /**
     * DI DI 模板镜像操作组件
     *
     * @class
     * @extends xui.XDatasource
     * @param {Function=} options.commonParamGetter      
     */
    var DI_RTPLSAVE_MODEL = 
            $namespace().DIRtplSaveModel = 
            inheritsObject(XDATASOURCE, constructor);
    var DI_RTPLSAVE_MODEL_CLASS = 
            DI_RTPLSAVE_MODEL.prototype;

    //------------------------------------------
    // 方法
    //------------------------------------------

    /**
     * 构造方法
     *
     * @private
     * @param {Object} options 参数
     */
    function constructor(options) {
        /**
         * 得到公用的请求参数
         *
         * @type {Function}
         * @private
         */
        this._fCommonParamGetter = options.commonParamGetter;
    }

    /**
     * @override
     */
    DI_RTPLSAVE_MODEL_CLASS.init = function () {};

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DI_RTPLSAVE_MODEL_CLASS.url = new XDATASOURCE.Set(
        {
            GET_IMAGES: URL.fn('RTPL_SAVE_GETIMAGES'),
            ADD: URL.fn('RTPL_SAVE_ADD'),
            UPDATE: URL.fn('RTPL_SAVE_UPDATE'),
            DELETE: URL.fn('RTPL_SAVE_DELETE')
        }
    );

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DI_RTPLSAVE_MODEL_CLASS.businessKey = new XDATASOURCE.Set(
        {
            GET_IMAGES: 'RTPL_SAVE_GETIMAGES' + getUID(),
            ADD: 'RTPL_SAVE_ADD' + getUID(),
            UPDATE: 'RTPL_SAVE_UPDATE' + getUID(),
            DELETE: 'RTPL_SAVE_DELETE' + getUID()
        }
    );

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DI_RTPLSAVE_MODEL_CLASS.param = new XDATASOURCE.Set(
        {
            GET_IMAGES: function (options) {
                return this._fCommonParamGetter(options.args);
            },
            ADD: function (options) {
                return this._fCommonParamGetter(options.args);
            },
            UPDATE: function (options) {
                return this._fCommonParamGetter(options.args);
            },
            DELETE: function (options) {
                return this._fCommonParamGetter(options.args);
            }
        }
    );


    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DI_RTPLSAVE_MODEL_CLASS.complete = new XDATASOURCE.Set(
        {
            GET_IMAGES: doComplete,
            ADD: doComplete,
            UPDATE: doComplete,
            DELETE: doComplete
        }
    );

    function doComplete(ejsonObj) {
        // 换reportTemplateId（后台生成了副本，所以约定更换为副本的id）
        // FIXME 
        // 换成非嵌入的实现方式
        this._fCommonParamGetter.update(ejsonObj.data);
    }

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DI_RTPLSAVE_MODEL_CLASS.parse = new XDATASOURCE.Set(
        {
            GET_IMAGES: function (data) { return data; },
            ADD: function (data) { return data; },
            UPDATE: function (data) { return data; },
            DELETE: function (data) { return data; }
        }
    );


    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DI_RTPLSAVE_MODEL_CLASS.error = new XDATASOURCE.Set(
        {
            DATA: function (status, ejsonObj, options) {
                this._oTableData = {};
                this._oBreadcrumbData = {};
            }
            // TODO
        }
    );

})();

