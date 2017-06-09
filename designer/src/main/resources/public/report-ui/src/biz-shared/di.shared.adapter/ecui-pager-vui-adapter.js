/**
 * di.shared.adapter.EcuiPagerVUIAdapter
 * Copyright 2013 Baidu Inc. All rights reserved.
 *
 * @file:    ecui提供的Pager控件的适配器
 * @author:  sushuang(sushuang)
 * @depend:  xui, xutil, ecui
 */

$namespace('di.shared.adapter');

(function() {
    
    /**
     * ecui提供的Pager控件的适配器
     *
     * @public
     * @param {Object} def vui的定义
     * @param {Object} options vui实例创建参数
     * @return {Object} vui adapter实例
     */
    $namespace().EcuiPagerVUIAdapter = function(def, options) {
        return {
            setData: setData,
            getValue: getValue
        };
    };

    /**
     * 设置数据
     *
     * @public
     */
    function setData(data, source) {
        this.render(
            data.currentPage,
            data.totalRecordCount,
            String(source.pageSize)
        );
    }

    /**
     * 取数据
     */
    function getValue() {
        // TODO
    }

})();

