/**
 * di.shared.adapter.XCalendarVUIAdapter
 * Copyright 2013 Baidu Inc. All rights reserved.
 *
 * @file:    CalendarPlus的适配器
 * @author:  sushuang(sushuang)
 * @depend:  xui, xutil, ecui
 */

$namespace('di.shared.adapter');

(function() {
    
    var UTIL = di.helper.Util;
    var ecuiCreate = UTIL.ecuiCreate;
    var ecuiDispose = UTIL.ecuiDispose;
    var detachEvent = ecui.util.detachEvent;
    var attachEvent = ecui.util.attachEvent;
    var disposeControl = ecui.dispose;
    var repaint = ecui.repaint;
    var parseTimeDef = UTIL.parseTimeDef;
    var parseTimeUnitDef = UTIL.parseTimeUnitDef;
    var formatTime = UTIL.formatTime;
    var assign = xutil.object.assign;
    var clone = xutil.object.clone;
    var X_CALENDAR;
    var GLOBAL_MODEL;

    $link(function() {
        X_CALENDAR = ecui.ui.XCalendar;
        GLOBAL_MODEL = di.shared.model.GlobalModel;
    });

    /**
     * XCalendar的适配器
     *
     * @public
     * @param {Object} def vui的定义
     * @param {Object} options vui实例创建参数
     * @return {Object} vui adapter实例
     */
    $namespace().XCalendarVUIAdapter = function(def, options) {
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
     * @param {Object} options.granularities 粒度，
     *      'D', 'W', 'M', 'Q', 'Y'
     *          每项的配置，含有：
     *          options.start 开始时间，绝对值（如2012-12-12）或相对当前时间的值（如-5d）
     *          options.end 结束时间，格式同上
     * @return {Object} 创建的实例
     */
    function create(def, options) {
        return ecuiCreate(X_CALENDAR, def.el, null, prepareInitData(options));
    }

    /**
     * 设置数据
     *
     * @public
     */
    function setData(data) {
        this.setDatasource(prepareSetData(data));
    }

    /**
     * 准备数据
     *
     * @private
     */
    function prepareInitData(options) {
        var opt = assign(
            {}, 
            options, 
            [   
                'viewMode', 
                'headText', 
                'rangeLinkStr', 
                'weekLinkStr', 
                'blankText', 
                'forbidEmpty'
            ]
        );
        opt.timeTypeList = [];
        return opt;
    }

    /**
     * 准备数据
     *
     * @private
     */
    function prepareSetData(options) {
        var now = GLOBAL_MODEL().getDateModel().now();
        var opt = clone(options);
        var timeTypeOpt = options.timeTypeOpt;
        var timeType;
        var o;
        var i;
        var dArr;
        var unit;
        var offsetBase;
        opt.timeTypeOpt = opt.timeTypeOpt || {};

        for (timeType in timeTypeOpt) {
            o = opt.timeTypeOpt[timeType] = timeTypeOpt[timeType];
            dArr = parseTimeDef(o.date, [now, now]);

            // FIXME
            // 这里对于任意散选的情况，只支持了start，也就是只能这么配：
            // [[-1D], [-4D], ...] 而不能 [[-5D, -1D], [-9W, -6D], ...]
            if (dArr.length > 1) {
                o.date = [];
                for (i = 0; unit = dArr[i]; i ++) {
                    o.date.push(formatObjTime(unit, timeType).start);
                }
            }
            else {
                unit = formatObjTime(dArr[0],timeType);
                o.date = unit.end ? [unit.start, unit.end] : [unit.start];
            }
            o.range = formatObjTime(
                parseTimeUnitDef(o.range, [now, now]) || {},
                timeType
            );
            o.range.offsetBase = now;
        }

        return opt;
    }

    /**
     * 格式化时间
     * 
     * @private
     */    
    function formatObjTime(obj, timeType) {
        for (var key in obj) {
            obj[key] = formatTime(obj[key], timeType);
        }
        return obj;
    }

    /**
     * 获得当前选中数据
     *
     * @public
     * @this {Object} 目标实例
     * @return {Object} 数据
     */
    function getValue() {
        // TODO
        // 现在后台还不支持多选，只支持单选和范围选择
        var aDate = this.getValue();
        var timeType = this.getTimeType();
        return {
            start: formatTime(aDate[0], timeType),
            end: formatTime(aDate[1] || aDate[0], timeType),
            granularity: timeType
        };
    }

})();

