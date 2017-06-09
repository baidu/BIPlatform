/**
 * di.shared.ui.DITab
 * Copyright 2013 Baidu Inc. All rights reserved.
 *
 * @file:    DI tab容器
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
    var ecuiDispose = UTIL.ecuiDispose;
    var q = xutil.dom.q;
    var assign = xutil.object.assign;
    var bind = xutil.fn.bind;
    var objKey = xutil.object.objKey;
    var ecuiCreate = UTIL.ecuiCreate;
    var INTERACT_ENTITY = di.shared.ui.InteractEntity;
    var TAB_CONTAINER = ecui.ui.TabContainer;
        
    //------------------------------------------
    // 类型声明 
    //------------------------------------------

    /**
     * DI tab容器
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
    var DI_TAB = $namespace().DITab = 
            inheritsObject(INTERACT_ENTITY, constructor);
    var DI_TAB_CLASS = DI_TAB.prototype;
    
    /**
     * 定义
     */
    DI_TAB_CLASS.DEF = {
        // 主元素的css
        className: 'di-tab'
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

        this._bAutoDeaf = options.autoDeaf == null ? true : options.autoDeaf;
        this._bAutoComponentValueDisabled = 
            options.autoComponentValueDisabled == null
                ? false : options.autoComponentValueDisabled;
        this._bAutoVUIValueDisabled = 
            options.autoVUIValueDisabled == null
                ? true : options.autoVUIValueDisabled;

        this._aTab = [];        
        // TODO
        // 后续要写成vui的形式，剥离ecui
        this._uTab = ecuiCreate(TAB_CONTAINER, o);
        this._aBodyPart = [];

        // 添加tab 创建vpart实例
        var tabs = this.$di('getRef', 'vpartRef', 'tab');
        var bodys = this.$di('getRef', 'vpartRef', 'body');
        for (var i = 0, tabDef, bodyDef; tabDef = tabs[i]; i ++) {
            bodyDef = bodys[i];
            this._aTab.push(
                this._uTab.addTab(
                    null, 
                    assign(
                        {
                            tabEl: tabDef.el,
                            contentEl: bodyDef.el
                        },
                        tabDef.$di('getOpt', 'dataOpt'),
                        ['title', 'canClose']
                    )
                )
            );

            this._aBodyPart.push(this.$di('vpartCreate', 'body.' + i));
        }
    };

    /**
     * 初始化
     *
     * @public
     */
    DI_TAB_CLASS.init = function() {
        var me = this;

        // 事件绑定
        this._uTab.onafterchange = function(ctrlItem, lastCtrlItem) {

            // 设置耳聋
            me.$resetDisabled();

            for (
                var i = 0, item, bodyPart; 
                bodyPart = me._aBodyPart[i], item = me._aTab[i]; 
                i ++
            ) {
                /** 
                 * vpart显示事件
                 * 
                 * @event
                 */
                if (item.tabItem == ctrlItem) {
                    bodyPart.$di('dispatchEvent', 'active');
                }
                /** 
                 * vpart隐藏事件
                 * 
                 * @event
                 */
                if (item.tabItem == lastCtrlItem) {
                    bodyPart.$di('dispatchEvent', 'inactive');
                }
            }

            /**
             * 渲染完毕事件
             *
             * @event
             */
            me.$di('dispatchEvent', 'rendered');
            /**
             * tab更改事件
             *
             * @event
             */
            me.$di('dispatchEvent', 'change');
        }

        var opt = this.$di('getOpt', 'dataOpt');
        // 默认选中
        var selIndex = opt.selected - 1;
        var sel;
        if (sel = this._aTab[selIndex]) {
            this._uTab.selectTab(sel.tabItem);
            me.$di('dispatchEvent', 'rendered');
        }

        this.$resetDisabled();

        sel && this._aBodyPart[selIndex].$di('dispatchEvent', 'active');
    };

    /**
     * @protected
     */
    DI_TAB_CLASS.$resetDisabled = function() {
        var key = this.$di('getId');
        var bodys = this.$di('getRef', 'vpartRef', 'body', 'DEF');

        for (var i = 0, tab, inners, notCurr; tab = this._aTab[i]; i ++) {
            notCurr = this._uTab.getSelected() != tab.tabItem;

            inners = bodys[i].$di(
                'getRef', 'componentRef', 'inner', 'INS'
            ) || [];

            for (var j = 0; j < inners.length; j ++) {
                if (inners[j]) {
                    this._bAutoDeaf 
                        && inners[j].$di('setDeaf', notCurr, key);
                    this._bAutoComponentValueDisabled 
                        && inners[j].$di('setValueDisabled', notCurr, key);
                }
            }

            if (this._bAutoVUIValueDisabled) {
                inners = bodys[i].$di(
                    'getRef', 'vuiRef', 'inner', 'INS'
                ) || [];

                for (var j = 0; j < inners.length; j ++) {
                    inners[j] && inners[j].$di('setValueDisabled', notCurr, key);
                }
            }
        }    
    };

    /**
     * @override
     */
    DI_TAB_CLASS.dispose = function() {
        this._uTab && ecuiDispose(this._uTab);
        this._aTab = [];
        DI_TAB.superClass.dispose.call(this);
    };

    /**
     * 窗口改变后重新计算大小
     *
     * @public
     */
    DI_TAB_CLASS.resize = function() {
        this._uTab && this._uTab.resize();
    };

    /**
     * 解禁操作
     *
     * @protected
     * @param {string} key 禁用者的标志
     */
    DI_TAB_CLASS.enable = function(key) {
        this._uTab && this._uTab.enable();
        DI_TAB.superClass.enable.call(this);
    };    

    /**
     * 禁用操作
     *
     * @protected
     * @param {string} key 禁用者的标志
     */
    DI_TAB_CLASS.disable = function(key) {
        this._uTab && this._uTab.disable();
        DI_TAB.superClass.disable.call(this);
    };

})();