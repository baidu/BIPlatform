/**
 * di.shared.adapter.CalendarPlusVUIAdapter
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
    var parseTimeUnitDef = UTIL.parseTimeUnitDef;
    var formatTime = UTIL.formatTime;
    var CALENDAR_PLUS;
    var GLOBAL_MODEL;

    $link(function() {
        CALENDAR_PLUS = ecui.ui.CalendarPlus;
        GLOBAL_MODEL = di.shared.model.GlobalModel;
    });

    /**
     * CalendarPlus的适配器
     *
     * @public
     * @param {Object} def vui的定义
     * @param {Object} options vui实例创建参数
     * @return {Object} vui adapter实例
     */
    $namespace().CalendarPlusVUIAdapter = function(def, options) {
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
        return ecuiCreate(CALENDAR_PLUS, def.el, null, prepareData(options));
    }

    /**
     * 设置数据
     *
     * @public
     */
    function setData(data) {
        this.setData(prepareData(data));
        // detachEvent(window, 'resize', repaint);

        // var disposeFunc = this.$dispose;
        // this.$dispose = new Function();
        // disposeControl(this);
        // this.$dispose = disposeFunc;

        // var el = this.getOuter();
        // el.innerHTML = '';
        // this.$setBody(el);
        // this.$resize();
        // CALENDAR_PLUS.client.call(this, el, prepareData(data));
        // this._bCreated = false;
        // this.cache(true, true);
        // this.init();

        // attachEvent(window, 'resize', repaint);
    }

    /**
     * 准备数据
     *
     * @private
     */
    function prepareData(options) {
        var now = GLOBAL_MODEL().getDateModel().now();

        var defUnit = {
                defaultTime: ['0d'],
                range: ['-1Y', '0d']
            };
        var granularities = options.granularities
            // 缺省的granularity
            || { D: defUnit, W: defUnit, M: defUnit, Q: defUnit };

        var opt = {
            types: [],
            range: {},
            defaults: {}
        };

        var timeMap = {
            D: 'day', W: 'week', M: 'month', Q: 'quarter', Y: 'year'
        };

        var range;
        for (var gran in granularities) {
            opt.types.push(gran);

            range = granularities[gran];
            
            opt.defaults[timeMap[gran]] = formatObjTime(
                parseTimeUnitDef(range.defaultTime, [now, now]),
                gran
            ).start;
            opt.range[timeMap[gran]] = formatObjTime(
                parseTimeUnitDef(range.range, [now, now]) || {},
                gran
            );
        }

        return opt;
    }

    /**
     * 格式化时间
     * 
     * @private
     */    
    function formatObjTime(obj, granularity) {
        for (var key in obj) {
            obj[key] = formatTime(obj[key], granularity);
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
        var wrap = this.getDate();
        return {
            start: wrap.date,
            end: wrap.date,
            granularity: wrap.type
        };
    }

})();

