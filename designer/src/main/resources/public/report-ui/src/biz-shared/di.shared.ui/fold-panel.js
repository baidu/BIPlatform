/**
 * di.shared.ui.FoldPanel
 * Copyright 2013 Baidu Inc. All rights reserved.
 *
 * @file:    折叠面板
 * @author:  sushuang(sushuang)
 * @depend:  xui, xutil
 */

$namespace('di.shared.ui');

(function() {
    
    //------------------------------------------
    // 引用 
    //------------------------------------------

    var UTIL = di.helper.Util;
    var inheritsObject = xutil.object.inheritsObject;
    var q = xutil.dom.q;
    var assign = xutil.object.assign;
    var bind = xutil.fn.bind;
    var objKey = xutil.object.objKey;
    var INTERACT_ENTITY = di.shared.ui.InteractEntity;
        
    //------------------------------------------
    // 类型声明 
    //------------------------------------------

    /**
     * 折叠面板
     * 
     * @class
     * @extends xui.XView
     * @param {Object} options
     * @param {boolean=} options.autoDeaf 使用deaf模式，
     *                  即隐藏时deaf内部实体，默认为true
     * @param {boolean=} options.autoComponentValueDisabled component自动在隐藏时valueDisabled模式，
     *                  即隐藏时value disable内部实体，默认为false
     * @param {boolean=} options.autoVUIValueDisabled vui自动在隐藏时使用valueDisabled模式，
     *                  即隐藏时value disable内部实体，默认为true
     */
    var FOLD_PANEL = $namespace().FoldPanel = 
            inheritsObject(INTERACT_ENTITY, constructor);
    var FOLD_PANEL_CLASS = FOLD_PANEL.prototype;

    /**
     * 定义
     */
    FOLD_PANEL_CLASS.DEF = {
        // 主元素的css
        className: 'di-fold-panel'
    };

    //------------------------------------------
    // 方法
    //------------------------------------------

    /**
     * 构造函数
     *
     * @constructor
     * @public
     * @param {Object} options 参数
     */
    function constructor(options) {
        var el = this.$di('getEl');
        var o = document.createElement('div');
        el.appendChild(o);

        this._bFolded = true;
        this._bAutoDeaf = options.autoDeaf == null 
            ? true : options.autoDeaf;
        this._bAutoComponentValueDisabled = 
            options.autoComponentValueDisabled == null
                ? false : options.autoComponentValueDisabled;
        this._bAutoVUIValueDisabled = 
            options.autoVUIValueDisabled == null
                ? true : options.autoVUIValueDisabled;

        this._oBodyDef = this.$di('getRef', 'vpartRef', 'body', 'DEF');
        this._oCtrlBtnDef = this.$di('getRef', 'vpartRef', 'ctrlBtn', 'DEF');

        var defaultHide = options.defaultHide == null
                ? true : options.defaultHide;

        this.$createCtrlBtn();
        this.$resetCtrlBtnText();
        this.$ctrlBtnChange(defaultHide);
    };

    /**
     * 初始化
     *
     * @public
     */
    FOLD_PANEL_CLASS.init = function() {
        this.$resetDisabled();
    };

    /**
     * 创建ctrlBtn
     *
     * @protected
     */
    FOLD_PANEL_CLASS.$createCtrlBtn = function() {
        // 目前只支持文字式的ctrlBtn
        this._oCtrlBtnDef.el.innerHTML = [
            '<a href="#" class="di-fold-panel-ctrl-btn">',
                '<span class="di-fold-panel-ctrl-btn-text">&nbsp;</span>',
            '</a>',
            '<span class="di-fold-panel-ctrl-down"></span>'
        ].join('');

        var el = this._oCtrlBtnDef.el.firstChild;
        var me = this;
        el.onclick = function() {
            if (!me._bDisabled) { 
                me.$ctrlBtnChange();
                me.$resetDisabled();
            }
            return false;
        }
    };

    /**
     * @override
     */
    FOLD_PANEL_CLASS.dispose = function() {
        this._oCtrlBtnDef = null;
        this._oBodyDef = null;
        FOLD_PANEL.superClass.dispose.call(this);
    };

    /**
     * @protected
     */
    FOLD_PANEL_CLASS.$resetDisabled = function() {
        var inners;
        var key = this.$di('getId');

        inners = this._oBodyDef.$di(
            'getRef', 'componentRef', 'inner', 'INS'
        ) || [];

        for (var j = 0; j < inners.length; j ++) {
            if (inners[j]) {
                this._bAutoDeaf 
                    && inners[j].$di('setDeaf', this._bFolded, key);
                this._bAutoComponentValueDisabled
                    && inners[j].$di('setValueDisabled', this._bFolded, key);
            }
        }

        if (this._bAutoVUIValueDisabled) {
            inners = this._oBodyDef.$di(
                'getRef', 'vuiRef', 'inner', 'INS'
            ) || [];

            for (var j = 0; j < inners.length; j ++) {
                inners[j] && inners[j].$di('setValueDisabled', this._bFolded, key);
            }
        }
    };

    /**
     * 窗口改变后重新计算大小
     *
     * @public
     */
    FOLD_PANEL_CLASS.resize = function() {
    };

    /**
     * 设置ctrlBtn文字
     *
     * @protected
     */    
    FOLD_PANEL_CLASS.$resetCtrlBtnText = function() {
        var btnDef = this._oCtrlBtnDef;
        var dataOpt = btnDef.$di('getOpt', 'dataOpt');

        // 暂只支持链接形式
        // TODO
        btnDef.el.firstChild.firstChild.innerHTML = this._bFolded
            ? dataOpt.expandText 
            : dataOpt.collapseText;
        btnDef.el.lastChild.className = this._bFolded
            ? 'di-fold-panel-ctrl-down'
            : 'di-fold-panel-ctrl-up';
    };

    /**
     * 展开折叠
     *
     * @protected
     * @param {boolean=} toFold 是否折叠，如不传，则将折叠与否置反
     */
    FOLD_PANEL_CLASS.$ctrlBtnChange = function(toFold) {
        var style = this._oBodyDef.el.style;

        this._bFolded = toFold == null ? !this._bFolded : toFold;

        this.$resetCtrlBtnText();

        style.display = this._bFolded ? 'none' : '';

        /**
         * 渲染完事件
         *
         * @event
         */
        this.$di('dispatchEvent', 'rendered');
    };

})();