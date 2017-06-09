/**
 * di.shared.vui.HiddenInput
 * Copyright 2014 Baidu Inc. All rights reserved.
 *
 * @file:    富文本下拉框
 * @author:  lizhantong
 * @depend:  xui, xutil
 */

$namespace('di.shared.vui');

(function () {

    //------------------------------------------
    // 引用
    //------------------------------------------

    var inheritsObject = xutil.object.inheritsObject;
    var XOBJECT = xui.XObject;

    //------------------------------------------
    // 类型声明
    //------------------------------------------

    /**
     * 用于传递报表引擎外部传来的参数
     *
     * @class
     * @extends xui.XView
     * @param {Object} options
     * @param {HTMLElement} options.el 容器元素
     */
    var RICH_SELECT = $namespace().RichSelect =
        inheritsObject(XOBJECT, constructor);
    var RICH_SELECT_CLASS = RICH_SELECT.prototype;

    //------------------------------------------
    // 方法
    //------------------------------------------

    /**
     * 构造函数
     *
     * @private
     * @param {Object} options 参数
     * @param {HTMLElement} options.el 容器
     */
    function constructor(options) {
        this._eMain = options.el;
        this.$renderFlag = false;
    }

    /**
     * 构造函数
     *
     * @public
     */
    RICH_SELECT_CLASS.init = function () {};

    /**
     * 渲染控件
     *
     * @public
     * @param {Object} data 数据
     * @param {(Object|Array}} data.datasource 数据集
     */
    RICH_SELECT_CLASS.render = function (data) {
        var el = this._eMain;
        var that = this;
        if (!that.$renderFlag) {
            $(el).screenXingWeiZhiBiao(
                [
                    {
                        data: data,
                        clickCallback: function (param) {
                            that.notify('richSelectChange', param);
                        },
                        instructionText: '指标选择：'
                    }
                ]
            );
            that.$renderFlag = true;
        }
    };

    /**
     * 析构
     *
     * @public
     */
    RICH_SELECT_CLASS.dispose = function () {};

})();


