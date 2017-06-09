/**
 * xjschart.Tooltip
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:   提示框
 * @author:  sushuang(sushuang)
 * @depend:  raphael
 */

(function() {
    
    //----------------------------------------
    // 引用 
    //----------------------------------------
    
    var X = xjschart;
    var R = Raphael;
    var UI_ELEMENT = X.UIElement;
    var util = X.util;
    var inheritsObject = util.inheritsObject;
    var assign = util.assign;
    var extend = util.extend;
    var clone = util.clone;
    var isObject = util.isObject;
    var travelTree = util.travelTree;
    var removeElement = util.removeElement;
    var STOP_SUB_TREE_TRAVEL = util.STOP_SUB_TREE_TRAVEL;

        
    //----------------------------------------
    // 类型定义
    //----------------------------------------
    
    /**
     * 提示框
     *
     * @class
     * @extends xjschart.UIElement
     */
    var TOOLTIP = X.Tooltip = inheritsObject(
            UI_ELEMENT,
            tooltipConstructor
        );
    var TOOLTIP_PROTO = TOOLTIP.prototype;
        
    //----------------------------------------
    // 常量
    //----------------------------------------

    var DEFAULT_WIDTH = 160;
    var DEFAULT_HEIGHT = 50;
    
    //----------------------------------------
    // Tooltip 方法
    //----------------------------------------
    
    /**
     * 构造器
     *
     * @public
     * @constructor
     * @param {Object} options 初始化参数
     */
    function tooltipConstructor(options) {
        this._bVisible = true;
        this._bHover = false;
        this._oData = {};
    }

    /**
     * 初始化
     *
     * @public
     * @override
     */
    TOOLTIP_PROTO.init = function() {
        TOOLTIP.superClass.init.call(this);
    };
    
    /**
     * 析构
     *
     * @public
     * @override
     */
    TOOLTIP_PROTO.dispose = function() {
        TOOLTIP.superClass.dispose.call(this);
    };
    
    /**
     * 获得当前所有数据的拷贝，
     * 所获得的数据格式满足：可以用setData函数设置回来
     *
     * @public
     * @return {*} 当前所有数据的拷贝
     */
    TOOLTIP_PROTO.getData = function() {
        return clone(this._oData);
    };    
    
    /**
     * 设置数据
     *
     * @public
     * @param {*} data 数据，如果为null则清空
     * @param {boolean} isSilent 是否静默，
     *              静默则不执行视图改变，可直接调用render改变视图
     */
    TOOLTIP_PROTO.setData = function(data, isSilent) {
        if (data == null) { return; }
        this._oData = clone(data);
        !isSilent && this.render();
    };

    /** 
     * 显示
     *
     * @public
     * @abstract
     */
    TOOLTIP_PROTO.show = function() {
        if (!this._bVisible) {
            this._stMainView.show();
            this._stMainAgent.show();
            this._bVisible = true;
        }
    };

    /** 
     * 隐藏
     *
     * @public
     * @abstract
     */
    TOOLTIP_PROTO.hide = function() {
        if (this._bVisible) {
            this._stMainView.hide();
            this._stMainAgent.hide();
            this._bVisible = false;
        }
    };

    /** 
     * 延迟隐藏
     *
     * @public
     * @abstract
     */
    TOOLTIP_PROTO.hideDefer = function() {
        var me = this;
        clearTimeout(this._bHideTimeout);

        this._bHideTimeout = setTimeout( 
            function() {
                !me.isHover() && me.hide();
                me._bHideTimeout = null;
            },
            50
        );        
    };

    /**
     * 得到是否正在鼠标hover
     *
     * @public
     * @return {boolean} 是否正在hover
     */
    TOOLTIP_PROTO.isHover = function() {
        return this._bHover;
    };

    /**
     * 设置是否正在鼠标hover
     *
     * @public
     * @param {boolean} 是否正在hover
     */
    TOOLTIP_PROTO.setHover = function(isHover) {
        this._bHover = isHover;
    };

    /** 
     * 设置位置
     *
     * @public
     * @abstract
     * @param {number} x x坐标
     * @param {number} y y坐标
     */
    TOOLTIP_PROTO.setPosition = function(x, y) {};

    /**
     * 手动渲染
     *
     * @public
     */
    TOOLTIP_PROTO.render = function() {
        this.$render();
    };

    /**
     * 渲染，由派生类实现
     *
     * @protected
     * @abstract
     */
    TOOLTIP_PROTO.$render = function() {};

})();
