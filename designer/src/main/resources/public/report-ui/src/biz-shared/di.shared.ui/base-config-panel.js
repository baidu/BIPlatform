/**
 * di.shared.ui.BaseConfigPanel
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    简单配置面板的基类，做一些共性的事情，
 *           配置面板可继承此类。
 * @author:  sushuang(sushuang)
 * @depend:  xui, xutil
 */

$namespace('di.shared.ui');

(function() {
    
    //------------------------------------------
    // 引用 
    //------------------------------------------

    var URL = di.config.URL;
    var DIALOG = di.helper.Dialog;
    var UTIL = di.helper.Util;
    var DICT = di.config.Dict;
    var LANG = di.config.Lang;
    var ecuiCreate = UTIL.ecuiCreate;
    var addClass = xutil.dom.addClass;
    var disposeInnerControl = UTIL.disposeInnerControl;
    var template = xutil.string.template;
    var toShowText = xutil.string.toShowText;
    var q = xutil.dom.q;
    var inheritsObject = xutil.object.inheritsObject;
    var hasValueNotBlank = xutil.lang.hasValueNotBlank;
    var extend = xutil.object.extend;
    var assign = xutil.object.assign;
    var alert = di.helper.Dialog.alert;
    var isString = xutil.lang.isString;
    var textLength = xutil.string.textLength;
    var textSubstr = xutil.string.textSubstr;
    var stringToDate = xutil.date.stringToDate;
    var removeDom = xutil.dom.remove;
    var trim = xutil.string.trim;
    var bind = xutil.fn.bind;
    var XVIEW = xui.XView;
    var UI_FORM = ecui.ui.Form;
    var UI_BUTTON = ecui.ui.Button;
        
    $link(function() {
    });
    
    //------------------------------------------
    // 类型声明 
    //------------------------------------------

    /**
     * 简单配置面板的基类，做一些共性的事情
     * 派生类可使用单例(xutil.object.createSingle)
     * 
     * @class
     * @extends xui.XView
     */
    var BASE_CONFIG_PANEL = $namespace().BaseConfigPanel = 
            inheritsObject(XVIEW, constructor);
    var BASE_CONFIG_PANEL_CLASS = BASE_CONFIG_PANEL.prototype;

    //-----------------------------------
    // 模板
    //-----------------------------------

    var TPL_MAIN = [
            '<div class="q-di-form">',
                '<label>#{0}</label>',
                '<div class="q-di-form-content">#{1}</div>',
                '<div>',
                    '<div class="di-dim-select-btn">',
                        '<div class="q-di-submit">确定</div>',
                        '<div class="q-di-cancel">取消</div>',
                    '</div>',
                '<div>',
            '</div>'
        ].join('');

    //-----------------------------------
    // 待实现的抽象方法
    //-----------------------------------

    /**
     * 创建View
     *
     * @abstract
     * @protected
     * @param {Object} options 初始化参数
     */
    BASE_CONFIG_PANEL_CLASS.$doCreateView = function(options) {
    };

    /**
     * 创建Model
     *
     * @abstract
     * @protected
     * @param {Object} options 初始化参数
     */
    BASE_CONFIG_PANEL_CLASS.$doCreateModel = function(options) {
    };

    /**
     * 得到model
     *
     * @abstract
     * @protected
     * @return {xui.XDatasource} model
     */
    BASE_CONFIG_PANEL_CLASS.$doGetModel = function() {
        // 如果中派生类中的model就用this._mModel命名，则不用重载这个方法
        return this._mModel;
    };

    /**
     * 其他初始化
     *
     * @abstract
     * @protected
     */
    BASE_CONFIG_PANEL_CLASS.$doInit = function() {
    };

    /**
     * 得到内容tpl
     *
     * @abstract
     * @protected
     * @param {Object} options 初始化参数
     * @return {string} 内容的html模板
     */
    BASE_CONFIG_PANEL_CLASS.$doGetContentTPL = function(options) {
        return '';
    };

    /**
     * 重置输入
     * 
     * @abstract
     * @protected
     */
    BASE_CONFIG_PANEL_CLASS.$doResetInput = function() {
    };

    /**
     * 渲染内容
     * 
     * @abstract
     * @protected
     */
    BASE_CONFIG_PANEL_CLASS.$doRender = function(contentEl, data) {
    };

    /**
     * 打开
     * 
     * @abstract
     * @protected
     */
    BASE_CONFIG_PANEL_CLASS.$doOpen = function(mode, options) {
    };

    /**
     * 打开时候的请求（默认为请求后台）
     * 
     * @abstract
     * @protected
     */
    BASE_CONFIG_PANEL_CLASS.$doOpenSync = function() {
        // 每次打开时从后台获取初始值
        var dsId = this.$getDS().INIT;
        if (dsId) {
            this.$doGetModel().sync(
                { 
                    datasourceId: dsId,
                    args: this.$doGetInitArgs()
                }
            );
        }
        else {
            this.$handleInitSuccess({});
        }
    };

    /**
     * 提交（默认为请求后台）
     * 
     * @abstract
     * @protected
     */
    BASE_CONFIG_PANEL_CLASS.$doSubmit = function() {
        var args = this.$doGetSubmitArgs();
        if (isString(args)) {
            // 表示验证失败，不提交
            alert(args);
            return;
        }

        var dsId = this.$getDS().SUBMIT;
        var options = { datasourceId: dsId, args: args };
        if (dsId) {
            this.$doGetModel().sync(options);
        }
        else {
            this.$handleSubmitSuccess(null, null, options);
        }
    };

    /**
     * 取消事件处理（默认为关闭浮层）
     *
     * @protected
     * @event
     */
    BASE_CONFIG_PANEL_CLASS.$doCancel = function() {
        this.close();
    };

    /**
     * 渲染内容
     * 
     * @abstract
     * @protected
     */
    BASE_CONFIG_PANEL_CLASS.$doSubmitSuccess = function(contentEl, data) {
    };

    /**
     * 其他启用
     * 
     * @abstract
     * @protected
     */
    BASE_CONFIG_PANEL_CLASS.$doEnable = function() {
    };

    /**
     * 其他禁用
     * 
     * @abstract
     * @protected
     */
    BASE_CONFIG_PANEL_CLASS.$doDisable = function() {
    };

    /**
     * 得到初始化时的参数
     * 
     * @abstract
     * @protected
     * @return {Object} 提交参数包装，如{ aaa: 1, bbb: '123' }
     */
    BASE_CONFIG_PANEL_CLASS.$doGetInitArgs = function() {
        return {};
    };

    /**
     * 得到提交时的参数
     * 
     * @abstract
     * @protected
     * @return {Object} 提交参数包装，如{ aaa: 1, bbb: '123' }
     */
    BASE_CONFIG_PANEL_CLASS.$doGetSubmitArgs = function() {
        return {};
    };

    /**
     * 析构
     * 
     * @abstract
     * @protected
     */
    BASE_CONFIG_PANEL_CLASS.$doDispose = function() {
        // 一般不用实现
    };

    //-----------------------------------
    // 已实现的方法
    //-----------------------------------

    /**
     * 构造函数
     *
     * @constructor
     * @param {Object} options 初始化参数
     * @param {Object=} options.model
     * @param {Object=} options.parent
     * @param {string} options.cssName 主css名字 
     * @param {string} options.panelTitle 标题 
     */
    function constructor(options) {

        this._mModel = options.model;
        this._uParent = options.parent;

        // 创建Model
        this.$doCreateModel(options);

        this._bOpened = false;

        // 创建主dom
        var el = this._el = options.el || document.createElement('div');
        addClass(el, options.cssName || 'di-config-panel')

        document.body.appendChild(el);
        el.innerHTML = template(
            TPL_MAIN, 
            toShowText(options.panelTitle || this.PANEL_TITLE, '', true),
            this.$doGetContentTPL(options)
        );

        // 创建基本控件
        this._uForm = ecuiCreate(
            UI_FORM,
            q('q-di-form', el)[0],
            null,
            { hide: true }
        );
        this._uSubmitBtn = ecuiCreate(
            UI_BUTTON,
            q('q-di-submit', el)[0],
            null,
            { primary: 'ui-button-g' }
        );
        this._uCancelBtn = ecuiCreate(
            UI_BUTTON,
            q('q-di-cancel', el)[0],
            null,
            { primary: 'ui-button' }
        );
        this._eContent = q('q-di-form-content', el)[0];

        // 创建其他View
        this.$doCreateView(options);
    }

    /**
     * @override
     */
    BASE_CONFIG_PANEL_CLASS.init = function() {
        var me = this;
        var ds;
        var model = this.$doGetModel();

        this._uSubmitBtn.onclick = bind(this.$doSubmit, this);
        this._uCancelBtn.onclick = bind(this.$doCancel, this);

        this._uForm.onhide = function () {
            me._bOpened = false;
        };

        // Init
        this._uForm.init();
        this._uSubmitBtn.init();
        this._uCancelBtn.init();

        // 其他初始化
        this.$doInit();

        this.$doResetInput();
    };
    
    /**
     * @override
     */
    BASE_CONFIG_PANEL_CLASS.dispose = function() {
        // 其他析构
        this.$doDispose();

        this._uForm && this._uForm.dispose();
        this._uSubmitBtn && this._uSubmitBtn.dispose();
        this._uCancelBtn && this._uCancelBtn.dispose();
        this._eContent = null;
        removeDom(this._el);
        this._el = null;

        BASE_CONFIG_PANEL.superClass.dispose.call(this);
    };

    /**
     * 得到主DOM元素
     *
     * @public
     */
    BASE_CONFIG_PANEL_CLASS.getEl = function() {
        return this._el;
    };

    /**
     * 得到内容部分DOM元素
     *
     * @public
     */
    BASE_CONFIG_PANEL_CLASS.getContentEl = function() {
        return this._eContent;
    };

    /**
     * 打开面板
     *
     * @public
     * @param {string} mode 可取值：
     *                       'VIEW': 查看
     *                       'EDIT': 修改
     */
    BASE_CONFIG_PANEL_CLASS.open = function(mode, options) {
        this._sMode = mode;
        this._bOpened = true;
        var model = this._mModel;

        // 事件绑定
        // 允许改变DATASOURCE_ID_MAPPING，所以在open前确定DATASOURCE_ID_MAPPING即可
        if (ds = this.$getDS().INIT) {
            model.attachOnce(
                ['sync.preprocess.' + ds, openCheck(this.disable), this],
                ['sync.result.' + ds, openCheck(this.$handleInitSuccess), this],
                ['sync.error.' + ds, openCheck(this.$handleInitError), this],
                ['sync.complete.' + ds, openCheck(this.enable), this]
            );
        }
        if (ds = this.$getDS().SUBMIT) {
            model.attachOnce(
                ['sync.preprocess.' + ds, openCheck(this.disable), this],
                ['sync.result.' + ds, openCheck(this.$handleSubmitSuccess), this],
                ['sync.error.' + ds, openCheck(this.$handleSubmitError), this],
                ['sync.complete.' + ds, openCheck(this.enable), this]
            );
        }

        this.$doOpen(mode, options);

        this.$doResetInput();

        this.$doOpenSync();
    };

    /**
     * 关闭面板
     *
     * @public
     */
    BASE_CONFIG_PANEL_CLASS.close = function() {
        this._uForm.hide();
    };

    /**
     * 解禁操作
     *
     * @override
     * @public
     */
    BASE_CONFIG_PANEL_CLASS.enable = function() {
        if (this._bDisabled) {
            this._uSubmitBtn.enable();
            this._uCancelBtn.enable();
            // 其他启用
            this.$doEnable();
        }
        BASE_CONFIG_PANEL.superClass.enable.call(this);
    };    

    /**
     * 禁用操作
     *
     * @override
     * @public
     */
    BASE_CONFIG_PANEL_CLASS.disable = function() {
        if (!this._bDisabled) {
            this._uSubmitBtn.disable();
            this._uCancelBtn.disable();
            // 其他禁用
            this.$doDisable();
        }
        BASE_CONFIG_PANEL.superClass.disable.call(this);
    };    

    /**
     * 初始数据成功结果处理
     *
     * @protected
     */
    BASE_CONFIG_PANEL_CLASS.$handleInitSuccess = function(data) {
        try {
            this._uForm.showModal(DICT.DEFAULT_MASK_OPACITY);

            // 渲染内容
            this.$doRender(this.getContentEl(), data);

            this._uForm.resize();
            
            this._uForm.center();
        }
        catch (e) {
            // 需求变化性很大，数据源很杂，不敢保证返回数据总是匹配，
            // 所以用try catch
            // this.$handleInitError();
        }
    };

    /**
     * 原因添加失败结果处理
     *
     * @protected
     */
    BASE_CONFIG_PANEL_CLASS.$handleInitError = function() {
        var me = this;
        // 获取初始数据出错，提示并关闭面板
        DIALOG.alert(
            LANG.GET_DIM_TREE_ERROR,
            function() {
                me.close();
            }
        );
    };

    /**
     * 原因添加成功结果处理
     *
     * @protected
     */
    BASE_CONFIG_PANEL_CLASS.$handleSubmitSuccess = function(data, ejsonObj, options) {
        this.$doSubmitSuccess(this.getContentEl(), data, ejsonObj, options);
        this.close();
        /**
         * @event di.shared.ui.BaseConfigPanel#submit.close
         */
        this.notify('submit.close', [data, ejsonObj, options]);
    };

    /**
     * 原因添加失败结果处理
     *
     * @protected
     */
    BASE_CONFIG_PANEL_CLASS.$handleSubmitError = function(status) {
        DIALOG.alert(LANG.SAVE_FAILURE);
    };

    /**
     * @private
     */
    BASE_CONFIG_PANEL_CLASS.$getDS = function(status) {
        return this.DATASOURCE_ID_MAPPING || {};
    };

    /**
     * @public
     */
    BASE_CONFIG_PANEL_CLASS.center = function() {
        this._uForm.center();
    };

    function openCheck(fn) {
        return function () {
            if (this._bOpened) {
                return fn.apply(this, arguments);
            }
        }
    }

})();

