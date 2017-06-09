/**
 * di.shared.adapter.GeneralAdapterMethod
 * Copyright 2013 Baidu Inc. All rights reserved.
 *
 * @file:    通用的默认适配器
 *           一般在di.config.Dict中使用adapterMethods来引用此中方法，
 *           拷贝到目标对象中
 * @author:  sushuang(sushuang)
 * @depend:  xui, xutil, ecui
 */

$namespace('di.shared.adapter');

(function() {
    
    var UTIL = di.helper.Util;
    var extend = xutil.object.extend;

    /**
     * 通用的适配器方法
     */
    $namespace().GeneralAdapterMethod = {
        ecuiCreate: ecuiCreate,
        ecuiDispose: ecuiDispose,
        xuiCreate: xuiCreate,
        xuiDispose: xuiDispose
    };

    /**
     * 创建ecui控件
     *
     * @public
     * @param {Object} def vui定义
     * @param {Object} options 初始化参数
     * @return {Object} 创建的实例
     */
    function ecuiCreate(def, options) {
        return UTIL.ecuiCreate(def.clz, def.el, null, options);
    }

    /**
     * 释放ecui控件
     *
     * @public
     * @this {Object} 控件
     */
    function ecuiDispose() {
        UTIL.ecuiDispose(this);
    }

    /**
     * 创建xui-ui控件
     *
     * @public
     * @param {Object} def vui定义
     * @param {Object} options 初始化参数
     * @return {Object} 创建的实例
     */
    function xuiCreate(def, options) {
        return new def.clz(options);
    }

    /**
     * 释放xui-ui控件
     *
     * @public
     * @this {Object} 控件
     */
    function xuiDispose() {
        this.dispose && this.dispose();
    }
    
    // ...

})();

