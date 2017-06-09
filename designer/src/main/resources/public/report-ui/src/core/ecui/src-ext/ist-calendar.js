/**
 * ecui.ui.IstCalendar
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    IST风格的日历
 *          （支持单日历时间段选择，周月季选择）
 * @author:  sushuang(sushuang)
 *          (
 *              从Pulse版本的ecui中拷贝而来
 *              (pl-calendar.js by cxl(chenxinle))，
 *              并稍做修改
 *          )
 * @depend:  ecui
 */

(function() {

    var core = ecui;
    var array = core.array;
    var dom = core.dom;
    var ui = core.ui;
    var string = core.string;
    var util = core.util;

    var DATE = Date;
    var REGEXP = RegExp;
    var DOCUMENT = document;

    var children = dom.children;
    var createDom = dom.create;
    var getParent = dom.getParent;
    var getPosition = dom.getPosition;
    var moveElements = dom.moveElements;
    var setText = dom.setText;
    var addClass = dom.addClass;
    var formatDate = string.formatDate;
    var getByteLength = string.getByteLength;
    var encodeHTML = string.encodeHTML;
    var sliceByte = string.sliceByte;
    var indexOf = array.indexOf;
    var getView = util.getView;
    var blank = util.blank;

    var $fastCreate = core.$fastCreate;
    var inheritsControl = core.inherits;
    var triggerEvent = core.triggerEvent;
    var setFocused = core.setFocused;

    var UI_CONTROL = ui.Control;
    var UI_CONTROL_CLASS = UI_CONTROL.prototype;
    var UI_INPUT_CONTROL = ui.InputControl;
    var UI_INPUT_CONTROL_CLASS = UI_INPUT_CONTROL.prototype;
    var UI_PANEL = ui.Panel;
    var UI_PANEL_CLASS = UI_PANEL.prototype;
    var UI_CALENDAR_CLASS = ui.Calendar.prototype;
    var UI_CALENDAR_LAYER_CLASS = UI_CALENDAR_CLASS.Layer.prototype;
    var UI_CALENDAR_LAYER_SELECT_CLASS = UI_CALENDAR_LAYER_CLASS.Select.prototype;
    var UI_BUTTON = ui.Button;
    var UI_MONTH_VIEW_CLASS = ui.MonthView.prototype;

    //-------------------------------------------------
    // 类型声明
    //-------------------------------------------------

    /**
     * 日历控件类
     *
     * @class
     * @param {Object} options 初始化选项
     * @param {string} options.start 范围开始点
     * @param {end} options.end 范围结束点
     * @param {string} optoins.date 初始时间，格式：2012-12-12
     * @param {string} options.dateEnd 如果是RANGE模式，表示最后时间，格式：2012-12-12
     * @param {number} optoins.now 当前时间戳（用于传来系统时间）
     * @param {string} options.mode 模式，
     *      可选值为：'DAY'(默认), 'WEEK', 'RANGE'
     * @param {string} options.viewMode 显示模式，
     *      可选值为：'POP'(默认), 'FIX' 
     * @param {boolean} options.shiftBtnDisabled 是否禁用前后移动button，默认false
     */
    var UI_IST_CALENDAR = ui.IstCalendar =
        inheritsControl(
            UI_INPUT_CONTROL,
            'ui-calendar',
            function(el, options) {
                options.hidden = true;
            },
            function(el, options) {
                var o = createDom();
                var child;
                var date;
                var type = this.getTypes()[0];

                this._sMode = options.mode || 'DAY';
                if (this._sMode == 'WEEK' || this._sMode == 'RANGE') {
                    addClass(el, type + '-range-mode');
                }

                this._sViewMode = options.viewMode || 'POP';
                if (this._sViewMode == 'FIX') {
                    addClass(el, type + '-fix-view');
                }

                o.innerHTML = [
                    '<span class="'+ type +'-btn-prv '+ type +'-btn"></span>',
                    '<span class="'+ type +'-text"></span>',
                    '<span class="'+ type +'-btn-nxt '+ type +'-btn"></span>',
                    '<span class="'+ type +'-btn-cal '+ type +'-btn"></span>',
                    '<div class="'+ type +'-layer" style="position:absolute;display:none"></div>'
                ].join('');

                child = children(o);

                this._oDate = PARSE_INPUT_DATE(options.date);
                if (this._sMode == 'RANGE') {
                    this._oDateEnd = PARSE_INPUT_DATE(options.dateEnd);
                }

                this._oRange = UI_CALENDAR_PARSE_RANGE(
                    options.start, 
                    options.end,
                    options.now
                );

                this._eText = child[1];
                
                // 后退一天按钮
                if (options.shiftBtnDisabled) {
                    child[0].style.display = 'none';
                }
                this._uBtnPrv = $fastCreate(
                    this.Button, 
                    child[0], 
                    this, 
                    { command: 'prv', icon: true }
                );

                // 前进一天按钮
                if (options.shiftBtnDisabled) {
                    child[2].style.display = 'none';
                }
                this._uBtnNxt = $fastCreate(
                    this.Button, 
                    child[2], 
                    this, 
                    { command: 'nxt', icon: true }
                );

                // 小日历按钮
                if (this._sViewMode == 'FIX') {
                    // FIX模式下不显示
                    child[3].style.display = 'none'; 
                }
                this._uBtnCal = $fastCreate(
                    this.Button, 
                    child[3], 
                    this, 
                    { command: 'cal', icon: true }
                );

                if (this._sViewMode == 'POP') {
                    DOCUMENT.body.appendChild(child[4]);
                }

                this._uLayer = $fastCreate(
                    this.Layer, 
                    child[4], 
                    this, 
                    {
                        date: this._oDate, 
                        range: this._oRange,
                        mode: this._sMode
                    }
                );

                moveElements(o, el, true);

                if (this._sViewMode == 'FIX') {
                    this.$showLayer();
                }
            }
        );

    var UI_IST_CALENDAR_CLASS = UI_IST_CALENDAR.prototype;

    var UI_IST_CALENDAR_BUTTON_CLASS = (
            UI_IST_CALENDAR_CLASS.Button = inheritsControl(
                UI_BUTTON, 
                null, 
                function(el, options){
                    var o = createDom();
                    var type = this.getType();
                
                    moveElements(el, o, true);
                    el.innerHTML = '<span class="'+ type +'-inner"></span>';
                    moveElements(o, el.firstChild, true);

                    if (options.icon) {
                        o = createDom(type + '-icon', '',  'span');
                        el.appendChild(o);
                    }

                    this._sCommand = options.command;
                }
            )
        ).prototype;

    var UI_IST_CALENDAR_LAYER_CLASS = (
            UI_IST_CALENDAR_CLASS.Layer = 
                inheritsControl(UI_CALENDAR_CLASS.Layer)
        ).prototype;

    var UI_IST_CALENDAR_LAYER_MONTH_VIEW_CLASS = (
            UI_IST_CALENDAR_LAYER_CLASS.MonthView = 
                inheritsControl(
                    UI_CALENDAR_CLASS.Layer.prototype.MonthView,
                    null,
                    function(el, options) {
                        this._sMode = options.mode;
                        this._oCellSelSet = {};
                        this._oCellHoverSet = {};
                    }
                )
        ).prototype;

    var UI_IST_CALENDAR_LAYER_MONTH_VIEW_CELL_CLASS = (
            UI_IST_CALENDAR_LAYER_MONTH_VIEW_CLASS.Cell = inheritsControl(
                UI_CALENDAR_CLASS.Layer.prototype.MonthView.prototype.Cell
            )
        ).prototype;

    var UI_IST_CALENDAR_LAYER_SELECT_OPTIONS_CLASS = (
            UI_CALENDAR_LAYER_SELECT_CLASS.Options = inheritsControl(
                UI_CALENDAR_LAYER_SELECT_CLASS.Options, 
                null, 
                null, 
                function(el, options) { 
                    addClass(el, 'ui-calendar-select-options'); 
                }
            )
        ).prototype;

    //-------------------------------------------------
    // 常量
    //-------------------------------------------------

    var UI_IST_CALENDAR_STR_PATTERN = 'yyyy-MM-dd';
    var UI_IST_CALENDAR_STR_PATTERN_SHOW = 'yyyy-MM-dd';

    var TIME_TYPE_WEEK = 1;
    var TIME_TYPE_MONTH = 2;
    var TIME_TYPE_QUARTER = 3;
    var DAY_MILLISECOND = 24*60*60*1000;
    var DATE_ZERO = new Date(0);

    //-------------------------------------------------
    // 工具方法
    //-------------------------------------------------
        
    function UI_CALENDAR_PARSE_RANGE(begin, end, now) {
        now = now != null ? new Date(now) : new Date();
        var res = {};
        var o = [now.getFullYear(), now.getMonth(), now.getDate()];
        var t;
        var p = {y:0, M:1, d:2};

        if (/^([-+]?)(\d+)([yMd])$/.test(begin)) {
            t = o.slice();
            if (!REGEXP.$1 || REGEXP.$1 == '+') {
                t[p[REGEXP.$3]] += parseInt(REGEXP.$2, 10);
            }
            else {
                t[p[REGEXP.$3]] -= parseInt(REGEXP.$2, 10);
            }
            res.begin = new Date(t[0], t[1], t[2]);
        }
        else if (
            Object.prototype.toString.call(begin) in {
                '[object String]': 1, '[object Date]': 1
            }
        ) {
            res.begin = new Date(begin);
        }

        if (/^([-+]?)(\d+)([yMd])$/.test(end)) {
            t = o.slice();
            if (!REGEXP.$1 || REGEXP.$1 == '+') {
                t[p[REGEXP.$3]] += parseInt(REGEXP.$2, 10);
            }
            else {
                t[p[REGEXP.$3]] -= parseInt(REGEXP.$2, 10);
            }
            res.end = new Date(t[0], t[1], t[2]);
        }
        else if (
            Object.prototype.toString.call(end) in {
                '[object String]': 1, '[object Date]': 1
            }
        ) {
            res.end = new Date(end);
        }

        return res ? res : {};
    }
    
    function UI_CALENDAR_WEEK_INFO(date) {
        var weekDay = date.getDay();
        var pre = -((weekDay + 6) % 7), next = (7 - weekDay) % 7;
        return {
            monday: new Date(date.getTime() + pre * DAY_MILLISECOND), 
            sunday: new Date(date.getTime() + next * DAY_MILLISECOND)
        };
    }

    function COMPARE_DATE(year1, month1, date1, year2, month2, date2) {
        if (year1 == year2) {
            if (month1 == month2) {
                if (date1 == date2) {
                    return 0;
                }
                else {
                    return date1 > date2 ? 1 : -1;
                }
            }
            else {
                return month1 > month2 ? 1 : -1;
            }
        }
        else {
            return year1 > year2 ? 1 : -1;
        }
    }

    function COMPARE_DATE_OBJ(date1, date2) {
        return COMPARE_DATE(
            date1.getFullYear(), date1.getMonth(), date1.getDate(),
            date2.getFullYear(), date2.getMonth(), date2.getDate()
        );        
    }

    function PARSE_INPUT_DATE(input) {
        var ret;
        if (input === false) {
            ret = null
        }
        else if (Object.prototype.toString.call(input) == '[object Date]') {
            ret = input;
        }
        else if (Object.prototype.toString.call(input) == '[object String]') {
            ret = input.split('-');
            ret = new Date(
                ret[0], 
                parseInt(ret[1], 10) - 1, 
                ret[2]
            );
        }
        return ret;
    }

    //----------------------------------------------
    // UI_IST_CALENDAR_BUTTON_CLASS 的方法
    //----------------------------------------------

    UI_IST_CALENDAR_BUTTON_CLASS.$click = function(event) {
        var par = this.getParent();
        switch(this._sCommand) {
            case 'prv':
                par.go(-1, -1);
                break;
            case 'nxt':
                par.go(1, 1);
                break;
            case 'cal':
                par.$showLayer();
                break;
        }
        event.exit();
    };

    //----------------------------------------------
    // UI_IST_CALENDAR_CLASS 的方法
    //----------------------------------------------

    UI_IST_CALENDAR_CLASS.$setSize = new Function();

    UI_IST_CALENDAR_CLASS.$showLayer = function() {
        var layer = this._uLayer;
        var pos = getPosition(this.getOuter());
        var posTop = pos.top + this.getHeight();

        if (!layer.isShow()) {

            layer.setDate(this.getDate());
            layer.show();
            setFocused(layer);

            if (this._sViewMode == 'POP') {
                var height = layer.getHeight();
                layer.setPosition(
                    pos.left,
                    posTop + height <= getView().bottom 
                        ? posTop : pos.top - height
                );
            }
        }
    }

    UI_IST_CALENDAR_CLASS.getMode = function() {
        return this._sMode;
    }    

    UI_IST_CALENDAR_CLASS.$flush = function() {
        var curDate = this._oDate;
        var range = this._oRange;

        if (range.begin && range.begin.getTime() == curDate.getTime()) {
            this._uBtnPrv.disable();
        }
        else {
            this._uBtnPrv.enable();
        }
        
        if (range.end && range.end.getTime() == curDate.getTime()) {
            this._uBtnNxt.disable();
        }
        else {
            this._uBtnNxt.enable();
        }
    }

    UI_IST_CALENDAR_CLASS.$click = function(event) {
        UI_INPUT_CONTROL_CLASS.$click.call(this);
        if (event.target == this._eText) {
            this.$showLayer();
        }
    };

    UI_IST_CALENDAR_CLASS.$activate = function (event) {
        var layer = this._uLayer;
        var con;
        var pos = getPosition(this.getOuter());
        var posTop = pos.top + this.getHeight();

        UI_INPUT_CONTROL_CLASS.$activate.call(this, event);
        if (!layer.isShow()) {
            layer.setDate(this.getDate(), this.getDateEnd(), null, true);
            layer.show();
            con = layer.getHeight();
            layer.setPosition(
                pos.left,
                posTop + con <= getView().bottom ? posTop : pos.top - con
            );
            setFocused(layer);
        }
    };

    UI_IST_CALENDAR_CLASS.go = function(offset, offsetEnd) {
        var newDate = new Date(
                this._oDate.getFullYear(), 
                this._oDate.getMonth(), 
                this._oDate.getDate() + offset
            );

        var newDateEnd;
        if (this._sMode == 'RANGE') {
            newDateEnd = new Date(
                this._oDateEnd.getFullYear(), 
                this._oDateEnd.getMonth(), 
                this._oDateEnd.getDate() + offsetEnd
            );
        }

        this.setDate(newDate, newDateEnd, null, true);
        triggerEvent(this, 'change', null, [newDate, newDateEnd]);
    };

    UI_IST_CALENDAR_CLASS.getDate = function() {
        return this._oDate;
    };
    
    UI_IST_CALENDAR_CLASS.getDateEnd = function() {
        return this._oDateEnd;
    };
    
    UI_IST_CALENDAR_CLASS.getWeekInfo = function() {
        return UI_CALENDAR_WEEK_INFO(this._oDate);
    };

    UI_IST_CALENDAR_CLASS.setDate = function(
        date, dateEnd, remainLayer, remainRangeSelStatus
    ) {
        var layer = this._uLayer;
        var range = this._oRange;
        var ntxt; 
        var weekInfo;

        if ((range.begin && range.begin.getTime() > date.getTime()) 
            || (range.end && range.end.getTime() < date.getTime())
        ) {
            return;
        }

        if (this._sViewMode == 'POP' && this._uLayer.isShow() && !remainLayer) {
            this._uLayer.hide();
        }
        
        if (date != null) {
            // 周模式
            if (this._sMode == 'WEEK') {
                weekInfo = UI_CALENDAR_WEEK_INFO(date);
                ntxt = formatDate(
                        maxDate(weekInfo.monday, range.begin), 
                        UI_IST_CALENDAR_STR_PATTERN_SHOW
                    )
                    + ' 至 ' 
                    + formatDate(
                        minDate(weekInfo.sunday, range.end), 
                        UI_IST_CALENDAR_STR_PATTERN_SHOW
                    );
            } 
            // 范围模式
            else if (this._sMode == 'RANGE') {
                if (!remainRangeSelStatus || !this._sRangeSelStatus) {
                    this._sRangeSelStatus = 'END';
                }
                ntxt = formatDate(date, UI_IST_CALENDAR_STR_PATTERN_SHOW);
                if (dateEnd) {
                    ntxt += ' 至 ' + formatDate(dateEnd, UI_IST_CALENDAR_STR_PATTERN_SHOW);
                }
                else {
                    if (this._sViewMode == 'POP') {
                        // 为了小日历按钮对齐而做的fake
                        ntxt += [
                            '<span class="', this.getType(), '-fake-text">',
                            ' 至 ' + formatDate(DATE_ZERO, UI_IST_CALENDAR_STR_PATTERN_SHOW),
                            '</span>',
                        ].join('');
                    }
                }
            }
            // 天模式
            else {
                ntxt = formatDate(date, UI_IST_CALENDAR_STR_PATTERN_SHOW);
            }
        } else {
            ntxt = '';
        }

        this._eText.innerHTML = ntxt;
        this.setValue(ntxt.replace(/\//g, '-'));

        this._oDate = date;
        if (this._sMode == 'RANGE') {
            this._oDateEnd = dateEnd;
        }

        if (this._sViewMode == 'FIX') {
            this._uLayer.setDate(date);
        }

        this.$flush();
    };

    UI_IST_CALENDAR_CLASS.init = function() {
        UI_INPUT_CONTROL_CLASS.init.call(this);
        this._uLayer.init();
        this.setDate(this.getDate(), this.getDateEnd());
    };

    UI_IST_CALENDAR_CLASS.$cache = function(style, cacheSize) {
        UI_INPUT_CONTROL_CLASS.$cache.call(this, style, cacheSize);
        this._uLayer.cache(true, true);
    };

    UI_IST_CALENDAR_CLASS.setRange = function(begin, end) {
        var cal = this._uLayer._uMonthView;
        cal.setRange(begin, end, true);
    };

    function minDate(date1, date2) {
        if (!date2) { return date1; }
        if (!date1) { return date2; }
        return date1.getTime() > date2.getTime() ? date2 : date1;
    }

    function maxDate(date1, date2) {
        if (!date2) { return date1; }
        if (!date1) { return date2; }
        return date1.getTime() > date2.getTime() ? date1 : date2;        
    }
    
    //--------------------------------------------------------------
    // UI_IST_CALENDAR_LAYER_CLASS 的方法
    //--------------------------------------------------------------

    UI_IST_CALENDAR_LAYER_CLASS.ondateclick = function(event, date) {
        var par = this.getParent();

        // 非RANGE模式
        if (this._sMode != 'RANGE' 
            && (!par.getDate() 
                || par.getDate().getTime() != date.getTime()
            )
        ) {
            par.setDate(date, null, null, true);
            /**
             * @event
             * @param {Date} selected date
             */
            triggerEvent(par, 'change', null, [date])
        }

        // RANGE模式
        else if (this._sMode == 'RANGE') {
            this._oDateSel = null;
            if (par._sRangeSelStatus == 'BEGIN') {
                par._sRangeSelStatus = 'END';
                var start = par.getDate();
                var end = date;
                if (start && end && COMPARE_DATE_OBJ(start, end) > 0) {
                    var tmp = end;
                    end = start;
                    start = tmp;
                }
                par.setDate(start, end, false, true);

                /**
                 * @event
                 * @param {string} ragneSelStatus 取值为'BEGIN'或'END'
                 * @param {Date} begin date
                 * @param {Date} end date
                 */
                triggerEvent(
                    par,
                    'change',
                    null,
                    [par.getDate(), date]
                )
            }
            else {
                par._sRangeSelStatus = 'BEGIN';
                // 设值后不隐藏layer
                par.setDate(date, null, true, true);
            }

        }

        // 其他
        else {
            this.hide();
        }
    };    

    UI_IST_CALENDAR_LAYER_CLASS.hide = function() {
        if (this.getParent()._sViewMode == 'FIX') {
            return;
        }

        if (this.isShow()) {
            var calCon = this.getParent();
            calCon && triggerEvent(calCon, 'layerhide');
        }
        UI_IST_CALENDAR_CLASS.Layer.superClass.hide.apply(this, arguments);
    };

    //--------------------------------------------------------------
    // UI_IST_CALENDAR_LAYER_MONTH_VIEW_CLASS 的方法
    //--------------------------------------------------------------

    UI_IST_CALENDAR_LAYER_MONTH_VIEW_CLASS.$setSelected = function(cell) {

        function removeStyle(c) { c.alterClass('-selected'); }
        function addStyle(c) { c.alterClass('+selected'); }
        var me = this;

        if (this._uCellSel) {
            // select一星期
            if (this._sMode == 'WEEK') {
                this.$travelWeek(this._uCellSel, removeStyle);
            }
            // select一天
            else if (this._sMode == 'DAY') {
                removeStyle(this._uCellSel);
            }
        }

        if (cell) {
            // select一星期
            if (this._sMode == 'WEEK') {
                this.$travelWeek(cell, addStyle);
            }
            // select一天
            else if (this._sMode == 'DAY') {
                addStyle(cell);
            }
            this._uCellSel = cell;
        }

        // select一个范围
        if (this._sMode == 'RANGE') {
            var calCon = this.getParent() 
                    ? this.getParent().getParent() : null;

            // 范围选完一半时
            if (calCon && calCon._sRangeSelStatus == 'BEGIN') {
                for (var i in this._oCellSelSet) {
                    removeStyle(this._oCellSelSet[i]);
                    delete this._oCellSelSet[i];
                }
                var cellWrap = this.$getCellByDate(calCon.getDate());
                if (cellWrap) {
                    this._oCellSelSet[cellWrap.index] = cellWrap.cell;
                    addStyle(cellWrap.cell);
                }
            }
            // 范围选完时
            else if (calCon && calCon._sRangeSelStatus == 'END') {
                this.$travelMonth(
                    function(c, i, isThisMonth) {

                        var isInRange;
                        if (isThisMonth) {
                            isInRange = me.$isCellInRange(
                                c, calCon.getDate(), calCon.getDateEnd()
                            );
                        }

                        if (isThisMonth 
                            && isInRange 
                            && !(i in me._oCellSelSet)
                        ) {
                            me._oCellSelSet[i] = c;
                            addStyle(c);
                        }
                        else if (
                            (!isInRange || !isThisMonth) 
                            && (i in me._oCellSelSet)
                        ) {
                            delete me._oCellSelSet[i];
                            removeStyle(c);
                        }
                    }
                );
            }
            // 其他情况
            else {
                for (var i in this._oCellSelSet) {
                    delete this._oCellSelSet[i];
                    removeStyle(this._oCellSelSet[i]);
                }
            }
        }
    };
    
    UI_IST_CALENDAR_LAYER_MONTH_VIEW_CLASS.$setHovered = function(
        cell, hovered
    ) {
        function addStyle(c) { c.alterClass('+hover'); }
        function removeStyle(c) { c.alterClass('-hover'); }
        var cellIndex = indexOf(this._aCells, cell);
        var me = this;

        if (cell) {
            // hover一星期
            if (this._sMode == 'WEEK') {
                this.$travelWeek(cell, (hovered ? addStyle : removeStyle));
            }

            // hover一天
            else if (this._sMode == 'DAY') {
                hovered ? addStyle(cell) : removeStyle(cell);
            }

            // hover一个范围
            else if (this._sMode == 'RANGE') {
                var calCon = this.getParent().getParent();
                var start = calCon.getDate();
                var end = new Date(this._nYear, this._nMonth, cell._nDay);
                if (start && end && COMPARE_DATE_OBJ(start, end) > 0) {
                    var tmp = end;
                    end = start;
                    start = tmp;
                }

                // 范围选完一半时
                if (calCon._sRangeSelStatus == 'BEGIN') {
                    this.$travelMonth(
                        function(c, i, isThisMonth) {
                            var isInRange;
                            if (isThisMonth) {
                                isInRange = me.$isCellInRange(c, start, end);
                            }
                            if (hovered
                                && isThisMonth 
                                && isInRange 
                                && !(i in me._oCellHoverSet)
                            ) {
                                me._oCellHoverSet[i] = c;
                                addStyle(c);
                            }
                            else if (
                                (!hovered || !isThisMonth || !isInRange)
                                && (i in me._oCellHoverSet)
                            ) {
                                delete me._oCellHoverSet[i];
                                removeStyle(c);
                            }
                        }
                    );
                }
                // 其他情况
                else {
                    this.$travelMonth(
                        function(c, i, isThisMonth) {
                            if ((!hovered || !isThisMonth)
                                && (i in me._oCellHoverSet)
                            ) {
                                delete me._oCellHoverSet[i];
                                removeStyle(c);
                            }
                        }
                    );
                    if (hovered) {
                        this._oCellHoverSet[cellIndex] = cell;
                        addStyle(cell);
                    }
                }
            }

        }
    };
    
    UI_IST_CALENDAR_LAYER_MONTH_VIEW_CLASS.$travelWeek = function(
        cell, callback
    ) {
        if (cell) {
            var currDate = new DATE(this._nYear, this._nMonth, cell._nDay);
            var index = indexOf(this._aCells, cell);
            index -= ((currDate.getDay() + 6) % 7);
            for (var i = 0; i < 7; i++) {
                callback.call(this, this._aCells[index + i]);    
            } 
        }  
    };

    UI_IST_CALENDAR_LAYER_MONTH_VIEW_CLASS.$travelMonth = function(callback) {
        var lastDateOfThisMonth = 
                new Date(this._nYear, this._nMonth + 1, 0).getDate();
        for (var i = 7, cell, isThisMonth; cell = this._aCells[i]; i ++) {
            isThisMonth = cell._nDay > 0 && cell._nDay <= lastDateOfThisMonth;
            callback(cell, i, isThisMonth);
        }
    };

    UI_IST_CALENDAR_LAYER_MONTH_VIEW_CLASS.$getCellByDate = function(date) {
        if (!date 
            || this._nYear != date.getFullYear() 
            || this._nMonth != date.getMonth()
        ) {
            return null;
        }
        var day = date.getDate();
        for (var i = 0, cell; cell = this._aCells[i]; i ++) {
            if (cell._nDay == day) { 
                return {cell: cell, index: i};
            }
        }
    };

    UI_IST_CALENDAR_LAYER_MONTH_VIEW_CLASS.$isCellInRange = function(
        cell, beginDate, endDate
    ) {
        if (!cell || !beginDate || !endDate) {
            return false;
        }

        var beginY = beginDate && beginDate.getFullYear();
        var beginM = beginDate && beginDate.getMonth();
        var beginD = beginDate && beginDate.getDate();
        var endY = endDate && endDate.getFullYear(); 
        var endM = endDate && endDate.getMonth();
        var endD = endDate && endDate.getDate();

        if ((   
                COMPARE_DATE(
                    beginY, beginM, beginD,
                    this._nYear, this._nMonth, cell._nDay
                ) <= 0
            )
            && (
                COMPARE_DATE(
                    this._nYear, this._nMonth, cell._nDay,
                    endY, endM, endD
                ) <= 0
            )
        ) {
            return true;
        }

        return false; 
    };
    
    //--------------------------------------------------------------
    // UI_IST_CALENDAR_LAYER_MONTH_VIEW_CELL_CLASS 的方法
    //--------------------------------------------------------------

    UI_IST_CALENDAR_LAYER_MONTH_VIEW_CELL_CLASS.$mouseover = function() {
        var parent = this.getParent();
        var index = indexOf(parent._aCells, this);
        // 非本月的cell已经被disabled，不会触发mouseover事件
        (index >= 7) && parent.$setHovered(this, true);  
    };
    
    UI_IST_CALENDAR_LAYER_MONTH_VIEW_CELL_CLASS.$mouseout = function() {
        var parent = this.getParent();
        var index = indexOf(parent._aCells, this);
        // 非本月的cell已经被disabled，不会触发mouseout事件
        (index >= 7) && parent.$setHovered(this, false);   
    };

    UI_CALENDAR_LAYER_SELECT_CLASS.$mousewheel = blank;

})();

