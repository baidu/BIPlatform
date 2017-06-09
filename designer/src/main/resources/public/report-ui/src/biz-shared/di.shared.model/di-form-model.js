/**
 * di.shared.model.DIFormModel
 * Copyright 2013 Baidu Inc. All rights reserved.
 *
 * @file:    DI 表单模型组件
 * @author:  sushuang(sushuang)
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
     * DI 表单模型组件
     *
     * @class
     * @extends xui.XDatasource
     * @param {Function=} options.commonParamGetter      
     */
    var DI_FORM_MODEL = 
            $namespace().DIFormModel = 
            inheritsObject(XDATASOURCE, constructor);
    var DI_FORM_MODEL_CLASS = 
            DI_FORM_MODEL.prototype;

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
        this._fCommonParamGetter = options.commonParamGetter
    }

    /**
     * @override
     */
    DI_FORM_MODEL_CLASS.init = function () {};

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DI_FORM_MODEL_CLASS.url = new XDATASOURCE.Set(
        {
            DATA: URL.fn('FORM_DATA'),
            ASYNC_DATA: URL.fn('FORM_ASYNC_DATA'),
            UPDATE_CONTEXT: URL.fn('FORM_UPDATE_CONTEXT'),
            CASCADE_GETLEVEL: URL.fn('FORM_CASCADE_GETLEVEL'),
            REGULAR: URL.fn('FORM_REGULAR')
        }
    );    

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DI_FORM_MODEL_CLASS.businessKey = new XDATASOURCE.Set(
        {
            DATA: 'DI_FORM_MODEL_DATA_' + getUID(),
            ASYNC_DATA: 'DI_FORM_MODEL_ASYNC_DATA_' + getUID(),
            UPDATE_CONTEXT: 'DI_FORM_MODEL_UPDATE_CONTEXT_' + getUID(),
            CASCADE_GETLEVEL: 'DI_FORM_MODEL_CASCADE_GETLEVEL_' + getUID()
        }
    );

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DI_FORM_MODEL_CLASS.complete = new XDATASOURCE.Set(
        {
            DATA: doComplete,
            ASYNC_DATA: doComplete
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
    DI_FORM_MODEL_CLASS.param = new XDATASOURCE.Set(
        {
            DATA: function (options) {
                return this._fCommonParamGetter(options.args.param); 
            },
            ASYNC_DATA: function (options) {
                return this._fCommonParamGetter(options.args.param);
            },
            UPDATE_CONTEXT: function (options) {
                return this._fCommonParamGetter(options.args.param);
            },
            CASCADE_GETLEVEL: function (options) {
                return this._fCommonParamGetter(options.args.param);
            },
            REGULAR: function (options) {
                return this._fCommonParamGetter(options.args.param);
            }
        }
    );

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DI_FORM_MODEL_CLASS.parse = new XDATASOURCE.Set(
        {
            DATA: function (data, ejsonObj, options) {
                this._oInitData = (data || {}).params || {};
                return data;
            },
            ASYNC_DATA: function (data, ejsonObj, options) {
                return (data || {}).params || {};
            },
            UPDATE_CONTEXT: function (data, ejsonObj, options) {
                return (data || {}).params || {};
            },
            CASCADE_GETLEVEL: function (data, ejsonObj, options) {
                return (data || {}).params || {};
            },
            REGULAR: function (data, ejsonObj, options) {
                return (data || {}).params || {};
            }
        }
    );

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DI_FORM_MODEL_CLASS.error = new XDATASOURCE.Set(
        {
            DATA: function (status, ejsonObj, options) {
                // TODO
            },
            ASYNC_DATA: function (status, ejsonObj, options) {
                // TODO
            }
        }
    );

    /** 
     * 得到初始化数据
     *
     * @public
     * @return {Object} 初始化数据
     */
    DI_FORM_MODEL_CLASS.getInitData = function () {
        return this._oInitData;
    };    

})();

