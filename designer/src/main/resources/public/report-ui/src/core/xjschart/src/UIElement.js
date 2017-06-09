/**
 * UIElement
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    JS图基础类
 * @author:  sushuang(sushuang)
 * @depend:  xui.xobject (for event)
 */

(function () {

    //-------------------
    // 引用
    //-------------------
    
    var X = window.xjschart;
    var util = X.util;
    var assign = util.assign;
    var isString = util.isString;
    var inheritsObject = util.inheritsObject;
    var removeElement = util.removeElement;
    var g = util.g;
    var XOBJECT = xui.XObject;
    
    //-------------------
    // 类型定义
    //-------------------

    /**
     * xjs视图基类
     *
     * @class
     * @extends xui.XObject
     */
    var UI_ELEMENT = X.UIElement = inheritsObject(
            XOBJECT, 
            uiElementConstructor
        );
    var UI_ELEMENT_PROTO = UI_ELEMENT.prototype;

    /**
     * UI_ELEMENT构造函数
     * 
     * @constructor
     * @param {Object} options 参数
     */
    function uiElementConstructor(options) {

        this.$processOptions(options);
        this._bInvalid = false;

        /**
         * x坐标
         *
         * @type {number}
         * @protected
         */
        this._nX;
        /**
         * y坐标
         *
         * @type {number}
         * @protected
         */
        this._nY;
        /**
         * 宽度
         *
         * @type {number}
         * @protected
         */
        this._nWidth;
        /**
         * 高度
         *
         * @type {number}
         * @protected
         */
        this._nHeight;
        /**
         * 数据
         *
         * @type {Object}
         * @protected
         */
        this._oData;
        /**
         * Raphael画布对象
         *
         * @type {Object}
         * @protected
         */
        this._paper;
        /**
         * 图的容器dom
         *
         * @type {HTMLElement}
         * @protected
         */
        this._eContainer;
        /**
         * 是否使用动画
         *
         * @type {boolean}
         * @protected
         */
        this._bAnimation;
        /**
         * 是否正在动画中
         *
         * @type {boolean}
         * @private
         */
        this._bAnimating = false;

        assign(
            this, 
            options, 
            {
                '_nX': 'x', 
                '_nY': 'y', 
                '_nHeight': 'height', 
                '_nWidth': 'width', 
                '_oData': 'data', 
                '_eContainer': 'container', 
                '_paper': 'paper',
                '_bAnimation': 'animation'
            }
        );

        if(isString(this._eContainer)) {
            this._eConatiner = g(this._eContainer);
        }
        
        if (!this._paper) {
            this._paper = Raphael(this._eContainer, this._nWidth, this._nHeight);
            this._bPaperHolder = true;
        }

    }

    /**
     * 初始化
     *
     * @public
     */
    UI_ELEMENT_PROTO.init = function () {
        UI_ELEMENT.superClass.init.call(this);
        this._stMain = this._paper.set().push(
            (this._stMainView = this._paper.set()),
            (this._stMainAgent = this._paper.set())
        );
    };
    
    /**
     * 设置paper
     *
     * @protected
     * @param {Object} paper Raphael的paper
     */
    UI_ELEMENT_PROTO.setPaper = function (paper) {
        this._paper = paper;
    };

    /**
     * 析构
     *
     * @public
     */
    UI_ELEMENT_PROTO.dispose = function () {
        removeElement(this, '_stMainView');
        removeElement(this, '_stMainAgent');
        removeElement(this, '_stMain');
        if (this._bPaperHolder) {
            this._paper.clear();
            this._paper.remove();
        }
        this._paper = null;
        UI_ELEMENT.superClass.dispose.call(this);
    };
    
    /**
     * 设置正在动画中
     *
     * @protected
     * @param {boolean} isAnimating 是否正在动画中
     */
    UI_ELEMENT_PROTO.$setAnimating = function (isAnimating) {
        this._bAnimating = isAnimating;
    };

    /**
     * 是否正在动画中
     *
     * @public
     * @return {boolean} 是否正在动画中
     */
    UI_ELEMENT_PROTO.isAnimating = function () {
        return !!this._bAnimating;
    };

    /**
     * 得到BoderBox
     *
     * @public
     * @return {Object} bbox 
     * @return {number} bbox.x 
     * @return {number} bbox.y
     * @return {number} bbox.width 
     * @return {number} bbox.height
     */
    UI_ELEMENT_PROTO.getBBox = function () {
        return {
            x: this._nX,
            y: this._nY,
            width: this._nWidth,
            height: this._nHeight
        }
    };

    /**
     * 得到x位置
     *
     * @public
     * @return {number} x 
     */
    UI_ELEMENT_PROTO.getX = function () {
        return this._nX;
    };

    /**
     * 得到y位置
     *
     * @public
     * @return {number} x 
     */
    UI_ELEMENT_PROTO.getY = function () {
        return this._nY;
    };

    /**
     * 得到width
     *
     * @public
     * @return {number} width 
     */
    UI_ELEMENT_PROTO.getWidth = function () {
        return this._nWidth;
    };

    /**
     * 得到height
     *
     * @public
     * @return {number} height 
     */
    UI_ELEMENT_PROTO.getHeight = function () {
        return this._nHeight;
    };

    /**
     * 设置需要重绘
     *
     * @protected
     */
    UI_ELEMENT_PROTO.$invalidate = function () {
        this._bInvalid = true;
    };

    /**
     * 处理options，用于override
     *（用于改变祖先类构造函数传入的Options）
     *
     * @protected
     * @abstract
     * @param {Object} options
     */
    UI_ELEMENT_PROTO.$processOptions = function(options) {};

})();