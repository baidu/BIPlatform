/**
 * di.shared.adapter.HChartVUIAdapter
 * Copyright 2013 Baidu Inc. All rights reserved.
 *
 * @file:    HChart的适配器
 * @author:  sushuang(sushuang)
 * @depend:  xui, xutil, ecui
 */

$namespace('di.shared.adapter');

(function() {
    
    var UTIL = di.helper.Util;
    var parseTimeUnitDef = UTIL.parseTimeUnitDef;
    var formatTime = UTIL.formatTime;
    var dateToString = xutil.date.dateToString;
    var GLOBAL_MODEL;

    $link(function() {
        GLOBAL_MODEL = di.shared.model.GlobalModel;
    });

    /**
     * IstCalendar的适配器
     *
     * @public
     * @param {Object} def vui的定义
     * @param {Object} options vui实例创建参数
     * @return {Object} vui adapter实例
     */
    $namespace().EChartVUIAdapter = function(def, options) {
        return {
            setData: setData
        };
    };

    /**
     * 设置数据
     *
     * @public
     */
    function setData(data) {
        var now = GLOBAL_MODEL().getDateModel().now();

        if (data.weekViewRange) {
            var range = parseTimeUnitDef(data.weekViewRange, [now, now]);

            if (range) {
                var fmt = 'yyyy-MM-dd';
                range[0] = range.start ? dateToString(range.start, fmt) : null;
                range[1] = range.end ? dateToString(range.end, fmt) : null;

                for (
                    var i = 0, xAxisDef; 
                    xAxisDef = (data.xAxis || [])[i]; 
                    i ++
                ) {
                    xAxisDef.range = range;
                }
            }
        }

        this.setData(data);
    }

})();

