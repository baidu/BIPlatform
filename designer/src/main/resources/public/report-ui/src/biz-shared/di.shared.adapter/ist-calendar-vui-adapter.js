/**
 * di.shared.adapter.IstCalendarVUIAdapter
 * Copyright 2013 Baidu Inc. All rights reserved.
 *
 * @file:    IstCalendar的适配器
 * @author:  sushuang(sushuang)
 * @depend:  xui, xutil, ecui
 */

$namespace('di.shared.adapter');

(function() {
    
    var UTIL = di.helper.Util;
    var ecuiCreate = UTIL.ecuiCreate;
    var dateToString = xutil.date.dateToString;
    var parseTimeUnitDef = UTIL.parseTimeUnitDef;
    var assign = xutil.object.assign;
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
    $namespace().IstCalendarVUIAdapter = function(def, options) {
        return {
            create: create,
            getValue: getValue
        };
    };

    /**
     * 创建
     *
     * @public
     * @param {Object} def vui定义
     * @param {Object} options 初始化参数
     * @param {string} options.start 开始时间，
     *                      绝对值（如2012-12-12）
     *                      或相对于系统时间的偏移（如-5d）
     * @param {string} options.end 结束时间，格式同上。如果和range同时存在，则end优先
     * @param {string} options.range 区间，相对于start的偏移（如-4d）
     * @param {string} options.defaultTime 默认时间
     * @return {Object} 创建的实例
     */
    function create(def, options) {
        var now = GLOBAL_MODEL().getDateModel().now();

        var opt = {};

        opt.now = now.getTime();

        var defTime = parseTimeUnitDef(options.defaultTime, [now, now]) || {};
        opt.date = defTime.start || new Date();
        opt.dateEnd = defTime.end;

        var range = parseTimeUnitDef(options.range, [now, now]);
        if (range) {
            opt.start = range.start;
            opt.end = range.end;
        }

        // 其他选项
        assign(opt, options, ['mode', 'viewMode', 'shiftBtnDisabled']);

        return ecuiCreate(def.clz, def.el, null, opt);
    }

    /**
     * 获得当前选中数据
     *
     * @public
     * @this {Object} 目标实例
     * @return {Object} 数据
     */
    function getValue() {
        var start = dateToString(this.getDate());

        if (this.getMode() == 'RANGE') {
            return {
                start: start,
                end: dateToString(this.getDateEnd())
            };
        }
        else {
            return {
                start: start,
                end: start
            }
        }
    }

})();

