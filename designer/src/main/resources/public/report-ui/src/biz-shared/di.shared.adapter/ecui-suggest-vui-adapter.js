/**
 * di.shared.adapter.EcuiSuggestVUIAdapter
 * Copyright 2013 Baidu Inc. All rights reserved.
 *
 * @file:    ecui suggest的适配器
 * @author:  sushuang(sushuang)
 * @depend:  xui, xutil, ecui
 */

$namespace('di.shared.adapter');

(function() {
    
    var extend = xutil.object.extend;
    var ecuiCreate = di.helper.Util.ecuiCreate;

    /**
     * ecui suggest的适配器
     *
     * @public
     * @param {Object} def vui的定义
     * @param {Object} options vui实例创建参数
     * @param {string} options.valueType 提交时值的类型，可为
     *      'TEXT'：则getValue取到的值是text（默认）
     *      'VALUE'：则getValue取到的是value
     * @return {Object} vui adapter实例
     */
    $namespace().EcuiSuggestVUIAdapter = function(def, options) {
        return {
            create: create,
            getValue: getValueFunc[options.valueType || 'TEXT']
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
        var ctrl = ecuiCreate(def.clz, def.el, null, options);


        ctrl.$di('registerEventAgent', 'async');
        
        // 挂接事件
        ctrl.onquery = function (value) {
            /**
             * 异步加载统一的事件
             *
             * @event
             */
            ctrl.$di(
                'dispatchEvent',
                'async',
                [
                    (value || {}).text,
                    function (data) {
                        ctrl.update(data.datasource || []);
                    }
                ]
            );
        }

        return ctrl;
    }    

    /**
     * 获得当前选中数据
     *
     * @public
     * @this {Object} 目标实例
     * @return {string} 数据
     */
    var getValueFunc = {
        TEXT: function () {
            return (this.getValue() || {}).text || '';
        },
        VALUE: function () {
            return (this.getValue() || {}).value || '';
        }
    }

})();

