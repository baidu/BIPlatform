/**
 * di.shared.adapter.EcuiInputVUIAdapter
 * Copyright 2013 Baidu Inc. All rights reserved.
 *
 * @file:    input（单行输入，以及textarea）的适配器
 * @author:  sushuang(sushuang)
 * @depend:  xui, xutil, ecui
 */

$namespace('di.shared.adapter');

(function() {
    
    var UTIL = di.helper.Util;
    var ecuiCreate = UTIL.ecuiCreate;

    /**
     * input（单行输入，以及textarea）的适配器
     *
     * @public
     * @param {Object} def vui的定义
     * @param {Object} options vui实例创建参数
     * @return {Object} vui adapter实例
     */
    $namespace().EcuiInputVUIAdapter = function(def, options) {
        return {
            // getValue: getValue
        };
    };

})();

