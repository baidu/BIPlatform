/**
 * di.shared.model.DITableModel
 * Copyright 2013 Baidu Inc. All rights reserved.
 *
 * @file:    DI 表模型组件
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
     * DI 表模型组件
     *
     * @class
     * @extends xui.XDatasource
     * @param {Function=} options.commonParamGetter      
     */
    var DI_RTPLCLONE_MODEL = 
            $namespace().DIRtplCloneModel = 
            inheritsObject(XDATASOURCE, constructor);
    var DI_RTPLCLONE_MODEL_CLASS = 
            DI_RTPLCLONE_MODEL.prototype;

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
    DI_RTPLCLONE_MODEL_CLASS.init = function () {};

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DI_RTPLCLONE_MODEL_CLASS.url = new XDATASOURCE.Set(
        {
            GET_DEFAUL_IMAGENAME: URL.fn('RTPL_CLONE_GETDEFAULTIMAGENAME'),
            SAVE: URL.fn('RTPL_CLONE_SAVE'),
            CLEAR: URL.fn('RTPL_CLONE_CLEAR')
        }
    );

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DI_RTPLCLONE_MODEL_CLASS.businessKey = new XDATASOURCE.Set(
        {
            GET_DEFAUL_IMAGENAME: 'RTPL_CLONE_GETDEFAULTIMAGENAME' + getUID(),
            SAVE: 'RTPL_CLONE_SAVE' + getUID(),
            CLEAR: 'RTPL_CLONE_CLEAR' + getUID()
        }
    );

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DI_RTPLCLONE_MODEL_CLASS.param = new XDATASOURCE.Set(
        {
            GET_DEFAUL_IMAGENAME: function (options) {
                return this._fCommonParamGetter(options.args);
            },
            SAVE: function (options) {
                return this._fCommonParamGetter(options.args);
            },
            CLEAR: function (options) {
                return this._fCommonParamGetter(options.args);
            }
        }
    );


    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DI_RTPLCLONE_MODEL_CLASS.complete = new XDATASOURCE.Set(
        {
            GET_DEFAUL_IMAGENAME: doComplete,
            SAVE: doComplete,
            CLEAR: doComplete
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
    DI_RTPLCLONE_MODEL_CLASS.parse = new XDATASOURCE.Set(
        {
            GET_DEFAUL_IMAGENAME: function (data) { return data; },
            SAVE: function (data) { return data; },
            CLEAR: function (data) { return data; }
        }
    );


    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DI_RTPLCLONE_MODEL_CLASS.error = new XDATASOURCE.Set(
        {
            DATA: function (status, ejsonObj, options) {
                this._oTableData = {};
                this._oBreadcrumbData = {};
            }
            // TODO
        }
    );

})();

