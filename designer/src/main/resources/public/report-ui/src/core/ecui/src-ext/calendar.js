(function () {

    var core = ecui,
        array = core.array,
        dom = core.dom,
        ui = core.ui,
        string = core.string,
        util = core.util,

        DATE = Date,
        REGEXP = RegExp,
        DOCUMENT = document,

        pushArray = array.push,
        children = dom.children,
        createDom = dom.create,
        getParent = dom.getParent,
        getPosition = dom.getPosition,
        moveElements = dom.moveElements,
        setText = dom.setText,
        formatDate = string.formatDate,
        getView = util.getView,

        $fastCreate = core.$fastCreate,
        inheritsControl = core.inherits,
        triggerEvent = core.triggerEvent,
        setFocused = core.setFocused,

        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = UI_CONTROL.prototype,
        UI_BUTTON = ui.Button,
        UI_BUTTON_CLASS = UI_BUTTON.prototype,
        UI_INPUT_CONTROL = ui.InputControl,
        UI_INPUT_CONTROL_CLASS = UI_INPUT_CONTROL.prototype,
        UI_SELECT = ui.Select,
        UI_MONTH_VIEW = ui.MonthView,
        UI_MONTH_VIEW_CELL = UI_MONTH_VIEW.Cell;

    /**
     * 初始化日历控件。
     * options 对象支持的属性如下：
     * year    日历控件的年份
     * month   日历控件的月份(1-12)
     * @public
     *
     * @param {Object} options 初始化选项
     */
    var UI_CALENDAR = ui.Calendar =
        inheritsControl(
            UI_INPUT_CONTROL,
            'ui-calendar',
            function (el, options) {
                options.hidden = true;
            },
            function (el, options) {
                var o = createDom(), child,
                    date, range,
                    type = this.getTypes()[0];

                o.innerHTML = '<div class="'+ type +'-text"></div><div class="'+ type +'-cancel"></div><div class="'+ type +'-button"></div>'
                    + '<div class="'+ type +'-layer" style="position:absolute;display:none"></div>';

                child = children(o);

                this._bTip = options.tip !== false;

                if (options.date) {
                    date = options.date.split('-');
                    this._oDate = new Date(date[0], parseInt(date[1], 10) - 1, date[2]);
                }
                else if (options.date === false) {
                    this._oDate = null
                }
                else {
                    this._oDate = new Date();
                }
                range = UI_CALENDAR_PARSE_RANGE(options.start, options.end);

                this._eText = child[0];

                this._uCancel = $fastCreate(this.Cancel, child[1], this);
                this._uButton = $fastCreate(UI_CONTROL, child[2], this);
                DOCUMENT.body.appendChild(child[3]);
                this._uLayer = $fastCreate(this.Layer, child[3], this, {date: this._oDate, range: range});

                moveElements(o, el, true);
            }
        ),

        UI_CALENDAR_CLASS = UI_CALENDAR.prototype,
        UI_CALENDAR_CANCEL_CLASS = (UI_CALENDAR_CLASS.Cancel = inheritsControl(UI_CONTROL)).prototype,

        UI_CALENDAR_LAYER = UI_CALENDAR_CLASS.Layer = 
        inheritsControl(
            UI_CONTROL,
            'ui-calendar-layer',
            null,
            function (el, options) {
                var html = [], o, i,
                    type = this.getTypes()[0],
                    buttonClass = this.Button,
                    selectClass = this.Select,
                    monthViewClass = this.MonthView,
                    date = options.date,
                    year = (new Date()).getFullYear();

                html.push('<div class="'+ type +'-buttons"><div class="'+ type +'-btn-prv'+ UI_BUTTON.TYPES +'"></div><select class="'+ type +'-slt-year'+ UI_SELECT.TYPES +'">');
                for ( i = year - 5; i < year + 5; i ++) {
                    html.push('<option value="'+ i +'">'+ i +'</option>');
                }
                html.push('</select><select class="'+ type +'-slt-month'+ UI_SELECT.TYPES +'">');
                for (i = 1; i <= 12; i++) {
                    html.push('<option value="'+ i +'">'+ (i < 10 ? '0' : '') + i +'</option>');
                }
                html.push('</select><div class="'+ type +'-btn-nxt'+ UI_BUTTON.TYPES +'"></div></div>');
                html.push('<div class="'+ type +'-month-view'+ UI_MONTH_VIEW.TYPES +'"></div>');
                el.innerHTML = html.join('');
                
                el = children(el);
                o = children(el[0]);

                this._uPrvBtn = $fastCreate(buttonClass, o[0], this);
                this._uPrvBtn._nStep = -1;
                this._uYearSlt = $fastCreate(selectClass, o[1], this);
                this._uMonthSlt = $fastCreate(selectClass, o[2], this);
                this._uNxtBtn = $fastCreate(buttonClass, o[3], this);
                this._uNxtBtn._nStep = 1;

                el = el[1];
                this._sMode = options.mode;
                this._uMonthView = $fastCreate(
                    monthViewClass, 
                    el, 
                    this, 
                    {
                        range : options.range, 
                        mode: options.mode
                    }
                );
            }
        ),

        UI_CALENDAR_LAYER_CLASS = UI_CALENDAR_LAYER.prototype,
        UI_CALENDAR_LAYER_BUTTON_CLASS = (UI_CALENDAR_LAYER_CLASS.Button = inheritsControl(UI_BUTTON, null)).prototype,
        UI_CALENDAR_LAYER_SELECT_CLASS = (UI_CALENDAR_LAYER_CLASS.Select = inheritsControl(UI_SELECT, null)).prototype,
        UI_CALENDAR_LAYER_MONTHVIEW_CLASS = (UI_CALENDAR_LAYER_CLASS.MonthView = inheritsControl(UI_MONTH_VIEW, null)).prototype,

        UI_CALENDAR_STR_DEFAULT = '<span class="ui-calendar-default">请选择一个日期</span>',
        UI_CALENDAR_STR_PATTERN = 'yyyy-MM-dd';


    function UI_CALENDAR_PARSE_RANGE(begin, end) {
        var now = new Date(), res = null,
            o = [now.getFullYear(), now.getMonth(), now.getDate()], t,
            p = {y:0, M:1, d:2};
        if (/^(\d+)([yMd])$/.test(begin)) {
            res = res || {};
            t = o.slice();
            t[p[REGEXP.$2]] -= parseInt(REGEXP.$1, 10);
            res.begin = new Date(t[0], t[1], t[2]);
        }
        else if ('[object String]' == Object.prototype.toString.call(begin)) {
            res = res || {};
            res.begin = new Date(begin);
        }

        if (/^(\d+)([yMd])$/.test(end)) {
            res = res || {};
            t = o.slice();
            t[p[REGEXP.$2]] += parseInt(REGEXP.$1, 10);
            res.end = new Date(t[0], t[1], t[2]);
        }
        else if ('[object String]' == Object.prototype.toString.call(end)) {
            res = res || {};
            res.end = new Date(end);
        }

        return res;
    }

    function UI_CALENDAR_TEXT_FLUSH(con) {
        var el = con._eText;
        if (el.innerHTML == '') {
            con._uCancel.hide();
            if (con._bTip) {
                el.innerHTML = UI_CALENDAR_STR_DEFAULT;
            }
        }
        else {
            con._uCancel.show();
        }
    }

    function UI_CALENDAR_MONTHVIEW_FLUSH(con, day) {
        var cal = con._uMonthView,
            month = con._uMonthSlt.getValue(),
            year = con._uYearSlt.getValue();

        if (cal.getMonth() != month || cal.getYear() != year) {
            cal.setDate(year, month);
        }
        if (con._oDateSel && 
                cal.getMonth() == con._oDateSel.getMonth() + 1 && 
                cal.getYear() == con._oDateSel.getFullYear()) {
            day = con._oDateSel.getDate();
        }
        cal.setDay(day);
    }

    UI_CALENDAR_CLASS.getDate = function () {
        return this._oDate;
    };

    UI_CALENDAR_CLASS.setDate = function (date) {
        var layer = this._uLayer,
            ntxt = date != null ? formatDate(date, UI_CALENDAR_STR_PATTERN) : '';

        if (this._uLayer.isShow()) {
            this._uLayer.hide();
        }

        this._eText.innerHTML = ntxt;
        this.setValue(ntxt);
        this._oDate = date;
        UI_CALENDAR_TEXT_FLUSH(this);
    };

    UI_CALENDAR_CLASS.$activate = function (event) {
        var layer = this._uLayer, con,
            pos = getPosition(this.getOuter()),
            posTop = pos.top + this.getHeight();

        UI_INPUT_CONTROL_CLASS.$activate.call(this, event);
        if (!layer.isShow()) {
            layer.setDate(this.getDate());
            layer.show();
            con = layer.getHeight();
            layer.setPosition(
                pos.left,
                posTop + con <= getView().bottom ? posTop : pos.top - con
            );
            setFocused(layer);
        }
    };

    UI_CALENDAR_CLASS.$cache = function (style, cacheSize) {
        UI_INPUT_CONTROL_CLASS.$cache.call(this, style, cacheSize);
        this._uButton.cache(false, true);
        this._uLayer.cache(true, true);
    };

    UI_CALENDAR_CLASS.init = function () {
        UI_INPUT_CONTROL_CLASS.init.call(this);
        this.setDate(this._oDate);
        this._uLayer.init();
    };

    UI_CALENDAR_CLASS.clear = function () {
        this.setDate(null);
    };

    UI_CALENDAR_CANCEL_CLASS.$click = function () {
        var par = this.getParent(),
            layer = par._uLayer;

        UI_CONTROL_CLASS.$click.call(this);
        par.setDate(null);
    };

    UI_CALENDAR_CANCEL_CLASS.$activate = UI_BUTTON_CLASS.$activate;

    UI_CALENDAR_LAYER_CLASS.$blur = function () {
        this.hide();
    };

    UI_CALENDAR_LAYER_CLASS.setDate = function (date, notDay) {
        var monthSlt = this._uMonthSlt,
            yearSlt = this._uYearSlt,
            year = date != null ? date.getFullYear() : (new Date()).getFullYear(),
            month = date != null ? date.getMonth() + 1 : (new Date()).getMonth() + 1;

        if (!notDay) {
            this._oDateSel = date;
        }   
        monthSlt.setValue(month);
        yearSlt.setValue(year);
        UI_CALENDAR_MONTHVIEW_FLUSH(this, notDay ? null : date ? date.getDate() : null);
    };

    UI_CALENDAR_LAYER_CLASS.getDate = function () {
        var cal = this._uMonthView;
        return new Date(cal.getYear(), cal.getMonth() - 1);
    };

    UI_CALENDAR_LAYER_CLASS.$cache = function (style, cacheSize) {
        this._uPrvBtn.cache(true, true);
        this._uNxtBtn.cache(true, true);
        this._uMonthSlt.cache(true, true);
        this._uYearSlt.cache(true, true);
        this._uMonthView.cache(true, true);
        UI_CONTROL_CLASS.$cache.call(this, style, cacheSize);
    };

    UI_CALENDAR_LAYER_CLASS.init = function () {
        UI_CONTROL_CLASS.init.call(this);
        this._uMonthSlt.init();
        this._uYearSlt.init();
        this._uMonthView.init();
    };

    UI_CALENDAR_LAYER_CLASS.ondateclick = function (event, date) {
        var par = this.getParent();
        if ((!par.getDate || par.getDate().getTime() != date.getTime()) 
            && triggerEvent(par, 'change', null, [date])
        ) {
            par.setDate(date);
        }
        this.hide();
    };

    UI_CALENDAR_LAYER_SELECT_CLASS.onchange = function () {
        var layer = this.getParent();
        UI_CALENDAR_MONTHVIEW_FLUSH(layer);
        triggerEvent(layer, 'viewchange', null, [layer.getDate().getFullYear(), layer.getDate().getMonth() + 1]);
    };

    UI_CALENDAR_LAYER_BUTTON_CLASS.$click = function () {
        var step = this._nStep,
            layer = this.getParent(),
            date = layer.getDate(),
            ndate;
        ndate = new Date(date.getFullYear(), date.getMonth() + step, 1);
        layer.setDate(ndate, true);
        triggerEvent(layer, 'viewchange', null, [ndate.getFullYear(), ndate.getMonth() + 1]);
    };

    UI_CALENDAR_LAYER_MONTHVIEW_CLASS.ondateclick = function (event, date) {
        triggerEvent(this.getParent(), 'dateclick', event, [date]);
    };


/**
 * 双日历
 */
    var UI_MULTI_CALENDAR = ui.MultiCalendar = 
        inheritsControl(
            UI_CALENDAR,
            'ui-multi-calendar',
            function (el, options) {
                options.hidden = true;
            },
            function (el, options) {
                var o = createDom(), els;

                o.innerHTML = '<input type="hidden" name="'+ (options.beginname ? options.beginname : 'beginDate') +'" />'
                    + '<input type="hidden" name="'+ (options.endname ? options.endname : 'endDate') +'" />';
                
                if (options.bdate) {
                    els = options.bdate.split('-');
                    this._oBegin = new Date (els[0], parseInt(els[1], 10) - 1, els[2]);
                }
                if (options.edate) {
                    els = options.edate.split('-');
                    this._oEnd = new Date (els[0], parseInt(els[1], 10) - 1, els[2]);
                }
                els = children(o);    
                this._eBeginInput = els[0];
                this._eEndInput = els[1];

                moveElements(o, el, true);
            }
        ),

        UI_MULTI_CALENDAR_CLASS = UI_MULTI_CALENDAR.prototype,

        UI_MULTI_CALENDAR_LAY = UI_MULTI_CALENDAR_CLASS.Layer = 
        inheritsControl(
            UI_CONTROL,
            'ui-multi-calendar-layer',
            null,
            function (el, options) {
                var type = this.getTypes()[0],
                    html = [], range = options.range || {};

                html.push('<div class="'+ type +'-cal-area"><div class="'+ type +'-text"><strong>起始时间：</strong><span></span></div><div class="'+ UI_CALENDAR_LAYER.TYPES +'"></div></div>');
                html.push('<div class="'+ type +'-cal-area"><div class="'+ type +'-text"><strong>结束时间：</strong><span></span></div><div class="'+ UI_CALENDAR_LAYER.TYPES +'"></div></div>');
                html.push('<div class="'+ type +'-buttons"><div class="ui-button-g'+ UI_BUTTON.TYPES +'">确定</div><div class="'+ UI_BUTTON.TYPES +'">取消</div></div>');

                el.innerHTML = html.join('');
                el = children(el);

                this._eBeginText = el[0].firstChild.lastChild;
                this._eEndText = el[1].firstChild.lastChild;
                this._uBeginCal = $fastCreate(this.Cal, el[0].lastChild, this, {range: range});
                this._uBeginCal._sType = 'begin';
                this._uEndCal = $fastCreate(this.Cal, el[1].lastChild, this, {range: range});
                this._uEndCal._sType = 'end';
                this._uSubmitBtn = $fastCreate(this.Button, el[2].firstChild, this);
                this._uSubmitBtn._sType = 'submit';
                this._uCancelBtn = $fastCreate(this.Button, el[2].lastChild, this);
                this._uCancelBtn._sType = 'cancel';
            }
        ),

        UI_MULTI_CALENDAR_LAY_CLASS = UI_MULTI_CALENDAR_LAY.prototype;

        UI_MULTI_CALENDAR_LAY_CAL_CLASS = (UI_MULTI_CALENDAR_LAY_CLASS.Cal = inheritsControl(UI_CALENDAR_LAYER)).prototype,

        UI_MULTI_CALENDAR_LAY_BUTTON_CLASS = (UI_MULTI_CALENDAR_LAY_CLASS.Button = inheritsControl(UI_BUTTON)).prototype,
        UI_MULTI_CALENDAR_STR_DEFAULT = '<span class="ui-multi-calendar-default">请选择时间范围</span>';
    
    function UI_MULTI_CALENDAR_TEXT_FLUSH(con) {
        var el = con._eText;
        if (el.innerHTML == '') {
            con._uCancel.hide();
            if (con._bTip) {
                el.innerHTML = UI_MULTI_CALENDAR_STR_DEFAULT;
            }
        }
        else {
            con._uCancel.show();
        }
    };

    UI_MULTI_CALENDAR_CLASS.init = function () {
        UI_INPUT_CONTROL_CLASS.init.call(this);
        this.setDate({begin: this._oBegin, end: this._oEnd});
        this._uLayer.init();
    };

    UI_MULTI_CALENDAR_CLASS.setDate = function (date) {
        var str = [], beginTxt, endTxt;

        if (date == null) {
            date = {begin: null, end: null};
        }

        beginTxt = date.begin ? formatDate(date.begin, UI_CALENDAR_STR_PATTERN) : '';
        endTxt = date.end ? formatDate(date.end, UI_CALENDAR_STR_PATTERN) : '';

        this._oBegin = date.begin;    
        this._oEnd = date.end;
        this._eBeginInput.value = beginTxt;
        this._eEndInput.value = endTxt;
        if (this._oBegin) {
            str.push(beginTxt);
        }
        if (this._oEnd) {
            str.push(endTxt);
        }
        if (str.length == 1) {
            str.push(this._oEnd ? '之前' : '之后');
            str = str.join('');
        }
        else if (str.length == 2) {
            str = str.join('至');
        }
        else {
            str = '';
        }
        this._eText.innerHTML = str;
        UI_MULTI_CALENDAR_TEXT_FLUSH(this);
    };

    UI_MULTI_CALENDAR_CLASS.getDate = function () {
        return {begin: this._oBegin, end: this._oEnd};
    };

    UI_MULTI_CALENDAR_LAY_CLASS.setDate = function (date) {
        this._oBeginDate = date.begin;
        this._oEndDate = date.end;

        if (date.begin) {
            this._eBeginText.innerHTML = formatDate(date.begin, UI_CALENDAR_STR_PATTERN);
        }
        else {
            this._eBeginText.innerHTML = '';
        }

        if (date.end) {
            this._eEndText.innerHTML = formatDate(date.end, UI_CALENDAR_STR_PATTERN);
        }
        else {
            this._eEndText.innerHTML = '';
        }

        this._uBeginCal.setDate(date.begin);
        this._uBeginCal.setRange(undefined, date.end);
        this._uEndCal.setDate(date.end);
        this._uEndCal.setRange(date.begin);
    };

    UI_MULTI_CALENDAR_LAY_CLASS.$blur = function () {
        UI_CONTROL_CLASS.$blur.call(this);
        this.hide();
    };

    UI_MULTI_CALENDAR_LAY_CLASS.init = function () {
        UI_CONTROL_CLASS.init.call(this);
        this._uBeginCal.init();
        this._uEndCal.init();
    };

    UI_MULTI_CALENDAR_LAY_CLASS.ondateset = function () {
        var par = this.getParent(),
            beginDate = this._oBeginDate,
            endDate = this._oEndDate;

        if (triggerEvent(par, 'dateset', [beginDate, endDate])) {
            par.setDate({begin: beginDate, end: endDate});
        }
        this.hide();
    };

    UI_MULTI_CALENDAR_LAY_CLASS.$setDate = function (date, type) {
        var key = type.charAt(0).toUpperCase() 
                + type.substring(1);

        this['_e' + key + 'Text'].innerHTML = formatDate(date, UI_CALENDAR_STR_PATTERN);
        this['_o' + key + 'Date'] = date;
        if (type == 'begin') {
            this._uEndCal.setRange(date);
        }
        else {
            this._uBeginCal.setRange(undefined, date);
        }
    };

    UI_MULTI_CALENDAR_LAY_CAL_CLASS.$blur = function () {
        UI_CONTROL_CLASS.$blur.call(this);
    };

    UI_MULTI_CALENDAR_LAY_CAL_CLASS.ondateclick = function (event, date) {
        var par = this.getParent();

        this._oDateSel = date;
        par.$setDate(date, this._sType);
    };

    UI_MULTI_CALENDAR_LAY_CAL_CLASS.setRange = function (begin, end) {
        this._uMonthView.setRange(begin, end);
    };

    UI_MULTI_CALENDAR_LAY_BUTTON_CLASS.$click = function () {
        var par = this.getParent();
        UI_BUTTON_CLASS.$click.call(this);
        if (this._sType == 'submit') {
            triggerEvent(par, 'dateset');
        }
        else {
            par.hide();
        }
    }

})();
