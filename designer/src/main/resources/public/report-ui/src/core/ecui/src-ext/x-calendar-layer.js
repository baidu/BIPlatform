/**
 * ecui.ui.XCalendarLayer
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    富日历的日历层，
 *           支持日、周、月、季不同粒度时间选择，
 *           支持单选、多选、范围选
 * @author:  sushuang(sushuang)
 * @depend:  ecui
 */

(function () {

    var core = ecui;
    var array = core.array;
    var dom = core.dom;
    var ui = core.ui;
    var string = core.string;
    var util = core.util;
    var cutil = ui.XCalendarUtil;

    var DATE = Date;
    var REGEXP = RegExp;
    var DOCUMENT = document;
    var objProtoToString = Object.prototype.toString;

    var pushArray = array.push;
    var children = dom.children;
    var createDom = dom.create;
    var getParent = dom.getParent;
    var getPosition = dom.getPosition;
    var moveElements = dom.moveElements;
    var setText = dom.setText;
    var addClass = dom.addClass;
    var formatDate = string.formatDate;
    var getView = util.getView;
    var encodeHTML = string.encodeHTML;
    var compareDate = cutil.compareDate;
    var date2Arr = cutil.date2Arr;
    var arr2Date = cutil.arr2Date;
    var getWorkday = cutil.getWorkday;
    var minDate = cutil.minDate;
    var maxDate = cutil.maxDate;
    var isDate = cutil.isDate;
    var isArray = cutil.isArray;
    var isString = cutil.isString;
    var isNumber = cutil.isNumber;
    var cloneADate = cutil.cloneADate;
    var setSltValue = cutil.setSltValue;
    var getSltValue = cutil.getSltValue;
    var getWeekInfo = cutil.getWeekInfo;
    var getQuarter = cutil.getQuarter;
    var initSlt = cutil.initSlt;

    var $fastCreate = core.$fastCreate;
    var inheritsControl = core.inherits;
    var triggerEvent = core.triggerEvent;
    var setFocused = core.setFocused;

    var UI_CONTROL = ui.Control;
    var UI_CONTROL_CLASS = UI_CONTROL.prototype;
    var UI_BUTTON = ui.Button;
    var UI_BUTTON_CLASS = UI_BUTTON.prototype;
    var UI_INPUT_CONTROL = ui.InputControl;
    var UI_INPUT_CONTROL_CLASS = UI_INPUT_CONTROL.prototype;
    var UI_SELECT = ui.Select;
    var UI_X_CALENDAR_MD_VIEW = ui.XCalendarMDView;
    var UI_X_CALENDAR_MW_VIEW = ui.XCalendarMWView;
    var UI_X_CALENDAR_YM_VIEW = ui.XCalendarYMView;
    var UI_X_CALENDAR_YQ_VIEW = ui.XCalendarYQView;


    /**
     * 富日历的日历层
     * 
     * @param {Object} options 参数
     * @param {(string|Date|number|Array)=} options.date 初始时间，缺省则为new Date()
     *          可为表示时间的string：格式为：（以下时间字符串都用此格式）
     *              2012-12-12 （对应时间粒度：D）
     *              2012-12-12 （对应时间粒度：W, 对应当周第一天）
     *              2012-12    （对应时间粒度：M）
     *              2012-Q1    （对应时间粒度：Q）
     *          也可为时间对象,
     *          也可为时间戳。
     *          如果selMode为：
     *              SINGLE      date型如2012-12-12或相应Date对象；
     *              MULTIPLE    date型如[2012-12-12, 2012-03-04, 2012-11-15, ...]或相应Date对象数组；
     *              RANGE       date型如[2012-03-04, 2012-11-15]或者相应Date对象数组，数组长为2，第一元素表示范围开始，第二元素表示范围结束。
     * @param {Object} options.range
     * @param {(string|number|Date)=} options.range.offsetBase 如果范围设定使用偏移方式的话（如'+1d'），此参数为偏移的基础，缺省则为new Date()
     *          可为时间字符串、时间对象、时间戳
     * @param {(string|number|Date)=} options.range.start 当前时间戳（用于传来系统时间）
     *          可为时间字符串、时间对象、时间戳、偏移表达式（型如'+1d', '-2M', '+4y'）
     * @param {(string|number|Date)=} options.range.end 当前时间戳（用于传来系统时间）
     *          可为时间字符串、时间对象、时间戳、偏移表达式（型如'+1d', '-2M', '+4y'）
     * @param {Array.<Object>=} options.selModeList 要使用的选择类型，值为'SINGLE', 'MULTIPLE', 'RANGE'中的一个或几个，缺省则全开启
     *          每项为：{ text: 'xxxxxx', value: 'SINGLE', prompt: '这是说明提示,可忽略' }，其中value和selMode对应
     * @param {string=} options.selMode 初始选择类型，值可为：'SINGLE', 'MULTIPLE', 'RANGE'，缺省则取'SINGLE'
     * @param {Object=} options.model 当前数据对象，如果不传入则自己创建。传入，则可多个实例共享model（参见render方法）
     * @class
     */
    var UI_X_CALENDAR_LAYER = ui.XCalendarLayer = 
        inheritsControl(
            UI_CONTROL,
            'ui-x-calendar-layer',
            null,
            function (el, options) {
                createModel.call(this, el, options);
                createView.call(this, el, options);
            }
        );

    var UI_X_CALENDAR_LAYER_CLASS = UI_X_CALENDAR_LAYER.prototype;
    var UI_X_CALENDAR_LAYER_STEP_BUTTON_CLASS = (UI_X_CALENDAR_LAYER_CLASS.StepButton = inheritsControl(UI_BUTTON, null)).prototype;
    var UI_X_CALENDAR_LAYER_SELECT_CLASS = (UI_X_CALENDAR_LAYER_CLASS.Select = inheritsControl(UI_SELECT, null)).prototype;
    UI_X_CALENDAR_LAYER_MD_VIEW_CLASS = (UI_X_CALENDAR_LAYER_CLASS.MDView = inheritsControl(UI_X_CALENDAR_MD_VIEW, null)).prototype;
    UI_X_CALENDAR_LAYER_MW_VIEW_CLASS = (UI_X_CALENDAR_LAYER_CLASS.MWView = inheritsControl(UI_X_CALENDAR_MW_VIEW, null)).prototype;
    UI_X_CALENDAR_LAYER_YM_VIEW_CLASS = (UI_X_CALENDAR_LAYER_CLASS.YMView = inheritsControl(UI_X_CALENDAR_YM_VIEW, null)).prototype;
    UI_X_CALENDAR_LAYER_YQ_VIEW_CLASS = (UI_X_CALENDAR_LAYER_CLASS.YQView = inheritsControl(UI_X_CALENDAR_YQ_VIEW, null)).prototype;

    UI_X_CALENDAR_LAYER_SELECT_CLASS.Options = inheritsControl(
        UI_X_CALENDAR_LAYER_SELECT_CLASS.Options, 
        null, 
        null, 
        function(el, options) {
            addClass(el, 'ui-x-calendar-layer-select-options'); 
        }
    );

    UI_X_CALENDAR_LAYER_SEL_MODE_CLASS = (UI_X_CALENDAR_LAYER_CLASS.SelMode = 
        inheritsControl(UI_CONTROL, 'ui-x-calendar-layer-selmode')
    ).prototype;

    /**
     * 数据Model，存储当前时间、时间范围等
     *
     * @class
     * @param {Object} options 参数参见setDatasource方法
     */
    var UI_X_CALENDAR_MODEL = UI_X_CALENDAR_LAYER_CLASS.Model = modelConstructor;
    var UI_X_CALENDAR_MODEL_CLASS = UI_X_CALENDAR_MODEL.prototype;

    // 默认值
    UI_X_CALENDAR_MODEL_CLASS.DEFAULT = {
        selMode: 'SINGLE',
        timeType: 'D',
        selModeList: [
            { text: '单选', value: 'SINGLE', prompt: '单项选择' }
//            { text: '范围多选', value: 'RANGE', prompt: '范围选择，点击一下选择开始值，再点击一下选择结束值' },
//            { text: '任意多选', value: 'MULTIPLE', prompt: '可以选择离散的多项' }
        ]
    };

    var DATE_REG = /^(\d+)(\-(\d+)(\-(\d+))?)?$/;
    var QUARTER_REG = /^(\d+)\-Q(\d)/;

    //----------------------------------------------------
    // 工具方法
    //----------------------------------------------------

    function parseRange(start, end, offsetBase, cellTimeType) {
        var res = {};
        var o = res.offsetBase = parseInputDate(offsetBase || new Date());

        o = [o.getFullYear(), o.getMonth(), o.getDate()];
        var p = {y: 0, m: 1, d: 2};
        var t;

        if (/^([-+]?)(\d+)([ymd])$/.test((start || '').toLowerCase())) {
            t = o.slice();
            if (!REGEXP.$1 || REGEXP.$1 == '+') {
                t[p[REGEXP.$3]] += parseInt(REGEXP.$2, 10);
            }
            else {
                t[p[REGEXP.$3]] -= parseInt(REGEXP.$2, 10);
            }
            res.start = new Date(t[0], t[1], t[2]);
        }
        else {
            res.start = parseInputDate(start);
        }

        if (/^([-+]?)(\d+)([yMd])$/.test((end || '').toLowerCase())) {
            t = o.slice();
            if (!REGEXP.$1 || REGEXP.$1 == '+') {
                t[p[REGEXP.$3]] += parseInt(REGEXP.$2, 10);
            }
            else {
                t[p[REGEXP.$3]] -= parseInt(REGEXP.$2, 10);
            }
            res.end = new Date(t[0], t[1], t[2]);
        }
        else {
            res.end = parseInputDate(end);
        }

        if (res.start && res.end && compareDate(res.start, res.end, cellTimeType) > 0) {
            var tmp = res.end;
            res.end = res.start;
            res.start = tmp;
        }

        return res ? res : {};
    }

    function parseInputDate(input) {
        var ret;

        if (input == null) {
            ret = null;
        }
        else if (isArray(input)) {
            ret = [];
            for (var i = 0; i < input.length; i ++) {
                ret.push(parseInputDate(input[i]));
            }
        }
        else if (isDate(input)) {
            ret = input;
        }
        else if (isString(input)) {
            ret = parseDateStr(input);
        }
        else if (isNumber(input)){
            ret = new Date(input);
        }

        return ret;
    }

    function parseDateStr(dateStr) {
        if (DATE_REG.test(dateStr)) {
            return new Date(REGEXP.$1, (REGEXP.$3 || 1) - 1, REGEXP.$5 || 1);
        }
        else if (QUARTER_REG.test(dateStr)) {
            var par = [0, 0, 3, 6, 9];
            return new Date(REGEXP.$1, par[REGEXP.$2], 1);
        }
        
        return null;
    };

    function goViewStep(base, step, timeType) {
        step = Number(step);
        base = arr2Date(base, true);
        if (timeType == 'D' || timeType == 'W') {
            base.setMonth(base.getMonth() + step);
        }
        else if (timeType == 'M' || timeType == 'Q') {
            base.setFullYear(base.getFullYear() + step);
        }
        return base;
    }

    function goCellStep(base, step, timeType) {
        base = arr2Date(base, true);
        step = Number(step);
        if (timeType == 'D') {
            base.setDate(base.getDate() + step);
        }
        else if (timeType == 'W') {
            base.setDate(base.getDate() + step * 7);
        }
        else if (timeType == 'M') {
            base.setMonth(base.getMonth() + step);
        }
        else if (timeType == 'Q') {
            base.setMonth(base.getMonth() + step * 3);
        }
        return base;
    }

    //----------------------------------------------------
    // 构造方法
    //----------------------------------------------------

    function createModel(el, options) {
        if (options.model) {
            // model可以外部传入
            this._oModel = options.model;
        }
        else {
            // 设默认值
            var dft = this._oModel.DEFAULT;
            if (!options.selMode) {
                options.selMode = dft.selMode;
            }
            if (!options.timeType) {
                options.timeType = dft.timeType;
            }
            if (!options.selModeList) {
                options.selModeList = dft.selModeList;
            }
            this._oModel = new this.Model(options);
        }
    }

    function createView(el, options) {
        var type = this.getTypes()[0];
        var me = this;
        var html = [];
        var stepBtnClass = this.StepButton;
        var selectClass = this.Select;

        var model = this._oModel;
        var timeType = model.getTimeType();
        var aDate = model.getDate();
        var range = model.getRange();
        var hasMonthSlt = timeType == 'D' || timeType == 'W';

        var timeTypeDef = {
                D: { clz: this.MDView, st: '-md-view', btns: '-buttons-md' },
                W: { clz: this.MWView, st: '-md-view', btns: '-buttons-md' },
                M: { clz: this.YMView, st: '-ym-view', btns: '-buttons-ym' },
                Q: { clz: this.YQView, st: '-yq-view', btns: '-buttons-yq' }
            }[timeType];

        var o;
        var i;

        html.push('<div class="'+ type +'-buttons ' + type + timeTypeDef.btns + ' ">');

        // 后退按钮
        html.push('<div class="'+ type +'-btn-prv'+ UI_BUTTON.TYPES +'"></div>');

        // 年下拉框
        html.push('<select class="'+ type +'-slt-year'+ UI_SELECT.TYPES +'">');
        html.push('</select>');

        // 月下拉框
        if (hasMonthSlt) {
            html.push('<select class="' + type + '-slt-month' + UI_SELECT.TYPES + '">');
            for (i = 1; i <= 12; i++) {
                html.push('<option value="' + i +'">'+ (i < 10 ? '0' : '') + i + '</option>');
            }
            html.push('</select>');
        }

        // 前进按钮
        html.push('<div class="' + type + '-btn-nxt' + UI_BUTTON.TYPES + '"></div>');
        
        html.push('</div>');

        // selMode 选择区
        html.push('<div class="' + type + '-selmode"></div>');

        // 日历面板
        html.push('<div class="' + type + timeTypeDef.st + ' ' + timeTypeDef.clz.TYPES + '"></div>');

        el.innerHTML = html.join('');
        el = children(el);

        o = children(el[0]);
        i = 0;
        
        this._uPrvBtn = $fastCreate(stepBtnClass, o[i ++], this);
        this._uPrvBtn._nStep = -1;

        this._uYearSlt = $fastCreate(selectClass, o[i ++], this);

        if (hasMonthSlt) {
            this._uMonthSlt = $fastCreate(selectClass, o[i ++], this);
        }

        this._uNxtBtn = $fastCreate(stepBtnClass, o[i ++], this);
        this._uNxtBtn._nStep = 1;

        this._uSelMode = $fastCreate(this.SelMode, el[1], this);

        this._uCalView = $fastCreate(
            timeTypeDef.clz, el[2], this, { model: model }
        );

        this._uCalView.onchange = function (aDate) {
            /**
             * @event
             */
            triggerEvent(me, 'change', null, [aDate]);
        };
        this._uCalView.ondateclick = function (aDate) {
            /**
             * @event
             */
            triggerEvent(me, 'dateclick', null, [aDate]);
        };

        this.render();
    }

    UI_X_CALENDAR_LAYER_CLASS.$setSize = new Function();

    UI_X_CALENDAR_LAYER_CLASS.setDatasource = function (datasource, silent) {
        this._oModel.setDatasource(datasource);
        !silent && this.render();
    };

    UI_X_CALENDAR_LAYER_CLASS.$flushCalView = function (force) {
        var timeType = this._oModel.getTimeType();
        var calView = this._uCalView;
        var d = [];
        (o = Number(getSltValue(this._uYearSlt))) && d.push(o);
        (o = Number(getSltValue(this._uMonthSlt))) && d.push(o - 1);

        calView.setView(d);
    };

    /** 
     * 渲染
     *
     * @public
     * @param {Object} opt
     * @param {Date} viewDate 决定面板显示的日期
     * @param {boolean} remainSlt 是否不重新绘制日期选择下拉框（默认false）
     * @param {boolean} remainSelMode 是否不重绘selMode选择区（默认false）
     * @param {boolean} remainTimeView 是否保留当前view（默认false）
     */  
    UI_X_CALENDAR_LAYER_CLASS.render = function (opt) {
        opt = opt || {};
        var model = this.getModel();

        !opt.remainSlt && this.$resetSltDatasource();
        !opt.remainSelMode && this.$resetSelModeCtrl();
        
        if (!opt.remainTimeView) {
            var aDate = this._oModel.getDate();
            var viewDate = opt.viewDate 
                // 默认取最后一个选中日期作为当前要显示的面板
                || aDate[aDate.length - 1]
                || (
                    opt = new Date(), 
                    opt.setFullYear(
                        Math.min(
                            Math.max(model._nYearRangeStart, opt.getFullYear()), 
                            model._nYearRangeEnd
                        )
                    ),
                    opt
                );

            // 设置monthSlt, yearSlt
            setSltValue(this._uYearSlt, viewDate.getFullYear());
            setSltValue(this._uMonthSlt, viewDate.getMonth() + 1);
            this.$resetStepBtn();
        }

        this.$flushCalView();
    };

    UI_X_CALENDAR_LAYER_CLASS.$resetSelModeCtrl = function () {
        var type = this.getTypes()[0];
        var uSelMode = this._uSelMode;
        var outer = uSelMode.getOuter();
        var model = this.getModel();
        var aSelModeList = model.getSelModeList();
        var selMode = model.getSelMode();

        // 清除
        outer.innerHTML = '';

        if (!aSelModeList || !aSelModeList.length) {
            outer.style.display = 'none';
        }
        else {
            outer.style.display = '';
        }

        // 添加
        var html = [];
        var i;
        var o;
        var checked;
        var prompt;
        for (i = 0; o = aSelModeList[i]; i ++) {
            prompt = o.prompt ? (' title="' + encodeHTML(o.prompt) + '" ') : '';
            checked = o.value == selMode ? ' checked="checked" ' : '';
            html.push(
                '<input ' + prompt + ' type="radio" name="' + type + '-selmode-radio-' + this.getUID() 
                + '" class="' + type + '-selmode-radio" ' + checked 
                + ' data-selmode="' + o.value + '"/>'
            );
            html.push('<span ' + prompt + ' class="' + type + '-selmode-text">' + encodeHTML(o.text) + '</span>');
        }
        outer.innerHTML = html.join('');

    };

    UI_X_CALENDAR_LAYER_CLASS.$resetSltDatasource = function () {
        var range = this._oModel.getRange();
        var yearSlt = this._uYearSlt;
        var model = this.getModel();
        if (!yearSlt) { return; }

        var yearBase = (range.offsetBase || new Date()).getFullYear();
        var yearRangeStart = range.start 
            ? range.start.getFullYear() : (yearBase - 5);
        var yearRangeEnd = range.end 
            ? range.end.getFullYear() : (yearBase + 5);

        var oldValue = Number(getSltValue(yearSlt));
        var newValue;

        // 清除
        yearSlt.setValue(null);
        while(yearSlt.remove(0)) {}

        // 添加
        for (var i = yearRangeStart; i <= yearRangeEnd; i++) {
            yearSlt.add(String(i), null, { value: Number(i) });
            i == oldValue && (newValue = i);
        }

        model._nYearRangeStart = yearRangeStart;
        model._nYearRangeEnd = yearRangeEnd;

        yearSlt.setValue(newValue != null ? newValue : yearRangeStart);
        this.$resetStepBtn();
    };

    UI_X_CALENDAR_LAYER_CLASS.$resetStepBtn = function () {
        var yearSltValue = Number(getSltValue(this._uYearSlt));
        var monthSltValue = Number(getSltValue(this._uMonthSlt));
        var model = this.getModel();
        var timeType = model.getTimeType();

        // 只考虑yearSlt是否够显示即可
        var d = [yearSltValue, monthSltValue - 1];
        d = goViewStep(d, 1, timeType);
        this._uNxtBtn[
            d.getFullYear() > model._nYearRangeEnd ? 'disable' : 'enable'
        ]();

        d = [yearSltValue, monthSltValue - 1];
        d = goViewStep(d, -1, timeType);
        this._uPrvBtn[
            d.getFullYear() < model._nYearRangeStart ? 'disable' : 'enable'
        ]();
    };

    UI_X_CALENDAR_LAYER_CLASS.getDate = function () {
        return this._oModel.getDate();
    };

    UI_X_CALENDAR_LAYER_CLASS.getModel = function () {
        return this._oModel;
    };

    UI_X_CALENDAR_LAYER_CLASS.getValue = UI_X_CALENDAR_LAYER_CLASS.getDate;

    UI_X_CALENDAR_LAYER_CLASS.getTimeType = function () {
        return this._oModel.getTimeType();
    };

    UI_X_CALENDAR_LAYER_CLASS.init = function () {
        this._uMonthSlt && this._uMonthSlt.init();
        this._uYearSlt && this._uYearSlt.init();
        this._uCalView.init();
        UI_X_CALENDAR_LAYER.superClass.init.call(this);
    };

    //----------------------------------------------------
    // 下拉选择年月
    //----------------------------------------------------

    UI_X_CALENDAR_LAYER_SELECT_CLASS.onchange = function () {
        var par = this.getParent()
        par.$resetStepBtn();
        par.$flushCalView();
    };

    //----------------------------------------------------
    // 前进后退 button
    //----------------------------------------------------

    UI_X_CALENDAR_LAYER_STEP_BUTTON_CLASS.onclick = function () {
        var layer = this.getParent();
        var yearSlt = layer._uYearSlt;
        var monthSlt = layer._uMonthSlt;
        var d = [
            Number(getSltValue(yearSlt)), 
            Number((getSltValue(monthSlt) || 1) - 1), 
            1
        ];

        d = goViewStep(d, this._nStep, layer.getModel().getTimeType());
        setSltValue(yearSlt, d.getFullYear());
        setSltValue(monthSlt, d.getMonth() + 1);
        layer.$resetStepBtn();
        layer.$flushCalView();
    };

    //----------------------------------------------------
    // selmode 选择
    //----------------------------------------------------

    UI_X_CALENDAR_LAYER_SEL_MODE_CLASS.onclick = function (event) {
        var par = this.getParent();
        var target = event.target;
        if (target.tagName == 'INPUT') {
            var model = par.getModel()
            model.setDatasource({ selMode: target.getAttribute('data-selmode') });
            par.$flushCalView();
            /**
             * @event
             */
            triggerEvent(par, 'change', null, [model.getDate()]);
        }
    };

    //----------------------------------------------------
    // Calendar Model
    //----------------------------------------------------

    function modelConstructor(options) {
        this._aDate = [];
        this._aDefaultDate = [];
        this._oRange = {};
        this._aHoverDate = [];

        this.setDatasource(options);
    };

    /**
     * 设置model数据
     * 
     * @public
     * @param {Object} datasource 设置
     * @param {string} datasource.selMode
     * @param {Array.<Object>} datasource.selModeList 
     * @param {string} datasource.timeType
     * @param {Object} datasource.range
     * @param {Date|string} datasource.range.start
     * @param {Date|string} datasource.range.end
     * @param {Date|string} datasource.range.offsetBase
     * @param {Date|string|Array} datasource.date 当前选中
     */
    UI_X_CALENDAR_MODEL_CLASS.setDatasource = function (datasource) {
        datasource = datasource || {};

        // 设置forbidEmpty
        if (datasource.forbidEmpty != null) {
            this._bForbidEmpty = datasource.forbidEmpty || false;
        }

        // 设置timeType
        if (datasource.timeType) {
            this._sTimeType = datasource.timeType;
            // 周模式下，使用日的日历表示，所以cellTimeType和timeType不同
            // 这是个坑，以后删改代码时可能会踩
            this._sCellTimeType = datasource.cellTimeType;
            if (this._sCellTimeType == 'W') {
                this._sCellTimeType = 'D';
            }
        }

        // 设置selModelList
        var selModeListChange;
        if (datasource.selModeList) {
            selModeListChange = this._aSelModeList = datasource.selModeList;
        }

        // 设置selMode (在传入selMode或者selModeList改变时)
        var newSelMode;
        if ((newSelMode = datasource.selMode) || selModeListChange) {
            // 改变selMode时，会做相应转化
            var oldSelMode = this._sSelMode;
            this._sSelMode = newSelMode || selModeListChange[0].value;
            this.$switchSelMode(oldSelMode, newSelMode);
        }

        // 设置range
        var range = datasource.range;
        if (range) {
            this._oRange = parseRange(
                range.start, 
                range.end, 
                range.offsetBase, 
                this._sCellTimeType
            );
            this.$clipByRange(this._aDate);
        }

        // 设置defaultDate
        var aDefaultDate = datasource.defaultDate;
        if (aDefaultDate) {
            this._aDefaultDate = this.$parseADate(aDefaultDate);
        }

        // 设置_aDate
        var aDate = datasource.date;
        if (aDate) {
            this._aDate = this.$parseADate(aDate);
        }
        
        // 如果禁止为空
        if (this._bForbidEmpty && !this._aDate.length) {
            this._aDate = cloneADate(this._aDefaultDate);
        }

        // 规整
        if (this._sSelMode == 'SINGLE') {
            this._aDate = this._aDate.slice(0, 1);
        }
        else if (this._sSelMode == 'RANGE') {
            this._aDate = this._aDate.slice(0, 2);
        }
    };

    UI_X_CALENDAR_MODEL_CLASS.$parseADate = function (aDate) {
        var aDate = parseInputDate(aDate) || [];
        if (!isArray(aDate)) {
            aDate = [aDate];
        }

        if (this._sSelMode == 'RANGE' 
            && aDate[0]
            && aDate[1] 
            && compareDate(aDate[0], aDate[1], this._sCellTimeType) > 0
        ) {
            var tmp = aDate[1];
            aDate[1] = aDate[0];
            aDate[0] = tmp;
        }
        this.$clipByRange(aDate);

        return aDate;
    };    

    UI_X_CALENDAR_MODEL_CLASS.getDate = function () {
        return this._aDate;
    };

    UI_X_CALENDAR_MODEL_CLASS.getDefaultDate = function () {
        return this._aDefaultDate;
    };

    UI_X_CALENDAR_MODEL_CLASS.getTimeType = function () {
        return this._sTimeType;
    };
    
    UI_X_CALENDAR_MODEL_CLASS.goStep = function (step) {
        for (var i = 0, d; i < this._aDate.length; i ++) {
            if (d = this._aDate[i]) {
                this._aDate[i] = goCellStep(d, step, this._sTimeType);
            }
        }
    };    

    UI_X_CALENDAR_MODEL_CLASS.getHoverDate = function (selMode) {
        return this._aHoverDate || [];
    };

    UI_X_CALENDAR_MODEL_CLASS.getSelMode = function () {
        return this._sSelMode;
    };    

    UI_X_CALENDAR_MODEL_CLASS.getSelModeList = function () {
        return this._aSelModeList;
    };    

    UI_X_CALENDAR_MODEL_CLASS.getRange = function () {
        return this._oRange;
    };

    UI_X_CALENDAR_MODEL_CLASS.$clipByRange = function (aDate) {
        var range = this.getRange();
        var timeType = this._sTimeType;

        for (var i = 0, date; i < aDate.length; ) {
            if ((date = aDate[i])
                && (!range.start || compareDate(date, range.start, timeType) >= 0)
                && (!range.end || compareDate(range.end, date, timeType) >= 0)
            ) {
                i ++;
            }
            else {
                this._sSelMode == 'RANGE'
                    // range模式下如果不在范围内则全清空
                    ? (aDate = [])
                    : aDate.splice(i, 1);
            }
        }
    };

    UI_X_CALENDAR_MODEL_CLASS.testEdge = function (step) {
        var timeType = this._sTimeType;
        var aDate = this.getDate().slice();
        var range = this.getRange();
        var m;

        var lowerBound = range.start;
        var upperBound = range.end;

        if (!lowerBound) {
            lowerBound = [this._nYearRangeStart, 0, 1];
        }
        if (!upperBound) {
            upperBound = [this._nYearRangeEnd, 11, 31];
        }

        if (!aDate.length) {
            return false;
        }

        if (step < 0 && lowerBound) {
            m = minDate.apply(null, [timeType].concat(aDate));

            return compareDate(
                goCellStep(m, step, timeType),
                lowerBound,
                timeType
            ) >= 0;
        }   
        else if (step > 0 && upperBound) {
            m = maxDate.apply(null, [timeType].concat(aDate));

            return compareDate(
                goCellStep(m, step, timeType),
                upperBound,
                timeType
            ) <= 0;
        }
        else {
            return true;
        }
    };

    UI_X_CALENDAR_MODEL_CLASS.udateDateByClick = function (thisClick) {
        var modelDate = this.getDate();
        var selMode = this.getSelMode();
        var hasChange = true;
        var timeType = this._sTimeType;

        if (selMode == 'RANGE') {
            modelDate[0] && !modelDate[1]
                // 只选了上界的情况
                ? (modelDate[1] = thisClick)
                // 未选或者已全选的情况
                : (modelDate = [thisClick])
        }
        else if (selMode == 'SINGLE') {
            modelDate[0] && compareDate(modelDate[0], thisClick, timeType) == 0 
                ? (hasChange = false)
                : (modelDate[0] = thisClick);
        }
        else if (selMode == 'MULTIPLE') {
            var del = false;
            for (var i = 0, o; o = modelDate[i]; ) {
                if (compareDate(o, thisClick, timeType) == 0) {
                    modelDate.splice(i, 1);
                    del = true;
                }
                else {
                    i ++;
                }
            }
            !del && modelDate.push(thisClick);
        }

        // 更新model
        this.setDatasource({ date: modelDate });

        return hasChange;
    };

    UI_X_CALENDAR_MODEL_CLASS.updateHoverDate = function (refDate, isHover) {
        var dateArr = [];
        var modelDate = this.getDate();

        if (!isHover) {
            this._aHoverDate = [];
        }
        else {
            if (this._sSelMode == 'RANGE' && modelDate[0] && !modelDate[1]) {
                if (compareDate(modelDate[0], refDate, this._sTimeType) > 0) {
                    dateArr = [refDate, modelDate[0]];
                }
                else {
                    dateArr = [modelDate[0], refDate];
                }
            }
            else {
                dateArr = [refDate];
            }   
            this._aHoverDate = dateArr;
        }
    };

    UI_X_CALENDAR_MODEL_CLASS.$switchSelMode = function (oldSelMode, newSelMode) {
        if (oldSelMode == newSelMode || oldSelMode == null || newSelMode == null) {
            return;
        }
        else {
            this._aDate = [];
        }
    }

})();
