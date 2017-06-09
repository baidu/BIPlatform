/**
 * di.shared.ui.InteractEntity
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    Base Entity
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
    var assign = xutil.object.assign;
    var addClass = xutil.dom.addClass;
    var isObject = xutil.lang.isObject;
    var q = xutil.dom.q;
    var bind = xutil.fn.bind;
    var objKey = xutil.object.objKey;
    var getByPath = xutil.object.getByPath;
    var XOBJECT = xui.XObject;
    var LANG = di.config.Lang;
    var AJAX = di.config.Ajax;
    var alert;
    
    $link(function () {
        alert = di.helper.Dialog.alert;
    });    

    /**
     * Base Entity
     * 
     * @class
     * @extends xui.XView
     * @param {Object} options
     * @param {HTMLElement} options.el 容器元素
     * @param {Function=} options.commonParamGetter 
     *      得到公用的请求参数     
     */
    var INTERACT_ENTITY = $namespace().InteractEntity = 
        inheritsObject(
            XOBJECT,
            function (options) {

                // di开始必须
                this.$di('start', options);

                // 统一注册事件代理
                this.$di('registerEventAgent');

                // 禁用自身的notify和attach（只允许使用$di提供的）
                this.notify = this.attach = this.attachOnce =                 
                    function () {
                        throw new Error('Forbiden function');
                    };

                // 挂主cssClass
                var el = this.$di('getEl');
                var className = this.DEF.className;
                el && className && addClass(el, className);

                // 根据DEF创建model
                this.$createModelByDef(options);

                // 创建view
                this.$createView && this.$createView(options);
            }
        );
    var INTERACT_ENTITY_CLASS = INTERACT_ENTITY.prototype;
    
    /**
     * 定义信息
     */
    INTERACT_ENTITY_CLASS.DEF = {};

    /**
     * 根据定义信息，创建model
     *
     * @private
     */
    INTERACT_ENTITY_CLASS.$createModelByDef = function (options) {
        var modelDef = this.DEF.model;
        if (!modelDef) { return; }

        var clz = modelDef.clz 
            || (modelDef.clzPath && getByPath(modelDef.clzPath));
        if (!clz) { return; }

        // 创建model实例
        this._mModel = new clz(
            assign(
                {
                    commonParamGetter: this.$di('getCommonParamGetter'),
                    diFactory: this.$di('getDIFactory')
                },
                this.$createModelInitOpt(options)
            )
        );

        // 绑定默认方法   
        this._mModel.ajaxOptions = {
            defaultFailureHandler:
                bind(this.$defaultFailureHandler, this)
        };
    };

    /**
     * 得到model初始化参数
     * 由派生类自行实现
     *
     * @protected
     * @return {Object} 初始化参数
     */
    INTERACT_ENTITY_CLASS.$createModelInitOpt = function (options) {
        return {};
    };

    /**
     * 得到model
     *
     * @public
     * @return {Object} model
     */
    INTERACT_ENTITY_CLASS.getModel = function () {
        return this._mModel;
    };

    /**
     * 组装model sync的参数的统一方法
     *
     * @protected
     * @param {Object} model
     * @param {string} datasourceId
     * @param {Object} param
     * @param {Object} diEvent
     * @param {Object} opt
     * @param {Object} ajaxOptions
     */
    INTERACT_ENTITY_CLASS.$sync = function (
        model, datasourceId, param, diEvent, opt, ajaxOptions
    ) {
        var o = {
            datasourceId: datasourceId,
            args: {
                param: param,
                diEvent: diEvent
            }
        }
        assign(o.args, opt);
        // 为sync方法新增ajaxOptions参数，以后每次ajax请求都可以有单独自己的行为
        assign(o, ajaxOptions);
        return model.sync(o);
    };

    /**
     * 创建或得到dievent的方便方法
     * 用法一：$diEvent(options) 
     *      则得到options中的原有的diEvent（可能为undefined） 
     * 用法二：$diEvent('someEventName', options) 
     *      则得到事件名为'someEventName'的衍生diEvent，
     *      或者（没有使用diEvent时）得到eventnName本身
     *
     * @protected
     * @param {(string|Object)} eventName 如果为对象则表示此参数为options
     * @param {string=} options 走xdatasource的options，里面含有传递的diEvent属性
     */
    INTERACT_ENTITY_CLASS.$diEvent = function (eventName, options) {
        if (arguments.length == 1 && isObject(eventName)) {
            options = eventName;
            eventName = null;
        }

        var diEvent = options.args.diEvent;
        return eventName
            ? (diEvent ? diEvent(eventName) : eventName)
            : diEvent;
    };

    /**
     * sync时的解禁操作
     *
     * @protected
     */
    INTERACT_ENTITY_CLASS.$syncEnable = function (datasourceId) {
        this.$di('syncViewDisable', 'enable', datasourceId);
        this.$di('enable', 'DI_SELF_' + datasourceId);
    };

    /**
     * sync时的禁用操作
     *
     * @protected
     */
    INTERACT_ENTITY_CLASS.$syncDisable = function (datasourceId) {
        this.$di('syncViewDisable', 'disable', datasourceId);
        this.$di('disable', 'DI_SELF_' + datasourceId);
    };

    /**
     * 请求失败的默认处理
     *
     * @protected
     */
    INTERACT_ENTITY_CLASS.$defaultFailureHandler = function (status, ejsonObj) {
        var eventChanel = this.$di('getEventChannel');

        switch (status) {
            case AJAX.ERROR_SESSION_TIMEOUT: // session 过期
                eventChanel.triggerEvent('sessiontimeout');
                alert(LANG.SAD_FACE + LANG.RE_LOGIN, null, true);
                break;
            case AJAX.ERROR_PARAM: // olap查询参数错误，由应用程序自己处理
                break;
            default:
                alert(LANG.SAD_FACE + LANG.ERROR);
        }
    };

})();