/**
 * di.shared.adapter.EcuiInputTreeVUIAdapter
 * Copyright 2013 Baidu Inc. All rights reserved.
 *
 * @file:    ecui input-tree的适配器
 * @author:  sushuang(sushuang)
 * @depend:  xui, xutil, ecui
 */

$namespace('di.shared.adapter');

(function() {

    var ecuiCreate = di.helper.Util.ecuiCreate;
    var dateToString = xutil.date.dateToString;
    var isArray = xutil.lang.isArray;
    var DICT = di.config.Dict;

    /**
     * ecui input tree的适配器
     *
     * @public
     * @param {Object} def vui的定义
     * @param {Object} options vui实例创建参数
     * @return {Object} vui adapter实例
     */
    $namespace().EcuiInputTreeVUIAdapter = function(def, options) {
        return {
            create: create,
            setData: setData,
            getValue: getValue
        };
    };

    /**
     * 创建
     *
     * @public
     * @param {Object} def vui定义
     * @param {Object} options 初始化参数
     * @return {Object} 创建的实例
     */
    function create(def, options) {
        // 控件初始化所须
        options.hideCancel = true;
        options.asyn = true;

        var ctrl = ecuiCreate(def.clz, def.el, null, options);

        ctrl.$di('registerEventAgent', 'async');

        // 挂接事件
        ctrl.onloadtree = function (value, func) {
            /**
             * 异步加载统一的事件
             *
             * @event
             */
            ctrl.$di(
                'dispatchEvent',
                'async',
                [
                    value,
                    function (data) {
                        // func((data.datasource || {}).children || []);
                        func((data.datasource || {}) || []);
                    }
                ]
            );
        }

        // 赋予全局浮层id，用于自动化测试的dom定位
        ctrl._uLayer.getOuter().setAttribute(DICT.TEST_ATTR, def.id);

        return ctrl;
    }

    /**
     * 设置初始化数据
     * 
     * @public
     * @param {Object} data 数据
     */
    function setData(data) {
        if (!data) {
            return;
        }
        var tarData;
        // FIXME:渊源,这一块会重写个input-tree继承于此，然后再修改
        // 基础数据结构是object，但是不支持根节点是多个（也就是第一层级只能是一个）
        // 为了支持这种情况，现在返回的数据结构是array，但是代码渲染不支持磁结构，就模拟一个object
        // 树结构渲染完毕后，再把第一层的根节点隐藏掉
        if (Object.prototype.toString.call(data.datasource) === '[object Array]') {
            tarData = {
                "text": "全部",
                "value": "1",
                children: data.datasource
            };
        }

        this.setData(
            { 
                root: tarData,
                selected: isArray(data.value) 
                    ? data.value[0] 
                    : (data.value || (data.datasource[0] && data.datasource[0].value))
            }, 
            { 
                hideCancel: data.hideCancel == null 
                    ? true : data.hideCancel, 
                asyn: data.asyn == null 
                    ? true : data.asyn
            }
        );
    }

    /**
     * 获得当前选中数据
     *
     * @public
     * @this {Object} 目标实例
     * @return {Object} 数据
     */
    function getValue() {
        var v = this.getValue();
        return v ? [v.value] : [];
    }

})();

