/*
XCalendarMDView / XCalendarMWView - 日历的月日/月周视图
日历视图控件，继承自基础控件，不包含年/月/日的快速选择与切换，如果需要实现这些功能，请将下拉框(选择月份)、输入框(输入年份)等组合使用建立新的控件或直接在页面上布局并调用接口。

属性
_nYear      - 年份
_nMonth     - 月份(0-11)
_aCells     - 日历控件内的所有单元格，其中第0-6项是日历的头部星期名称
_oRange     - 默认的选择范围，只能通过初始化时的参数进行赋值

子控件属性
_nDay       - 从本月1号开始计算的天数，如果是上个月，是负数，如果是下个月，会大于当月最大的天数
*/
(function () {

    var core = ecui;
    var array = core.array;
    var dom = core.dom;
    var ui = core.ui;
    var util = core.util;

    var DATE = Date;
    var objProtoToString = Object.prototype.toString;

    var extend = util.extend;
    var indexOf = array.indexOf;
    var addClass = dom.addClass;
    var getParent = dom.getParent;
    var removeClass = dom.removeClass;
    var setText = dom.setText;

    var $fastCreate = core.$fastCreate;
    var inheritsControl = core.inherits;
    var triggerEvent = core.triggerEvent;

    var UI_CONTROL = ui.Control;

    //-------------------------------------------------------------
    // 工具方法
    //-------------------------------------------------------------

    var UI_X_CALENDAR_UTIL = ui.XCalendarUtil = {};
    var DAY_MILLISECOND = 24*60*60*1000;

    var compareDate = UI_X_CALENDAR_UTIL.compareDate = function (a, b, timeType) {
        a = formatDateByTimeType(a, timeType, true);
        b = formatDateByTimeType(b, timeType, true);

        return a[0] != b[0]
            ? (a[0] > b[0] ? 1 : -1)
            : (
                a[1] != b[1]
                    ? (a[1] > b[1] ? 1 : -1)
                    : (
                        a[2] != b[2]
                            ? (a[2] > b[2] ? 1 : -1)
                            : 0
                    )
            );
    };

    var formatDateByTimeType = UI_X_CALENDAR_UTIL.formatDateByTimeType = function (
        date, timeType, retArrOrDate
    ) {
        if (!date) { return; }

        if (timeType == 'D') {
            date = date2Arr(date, true);
        }
        else if (timeType == 'W') {
            date = date2Arr(getWorkday(date));
        }
        else if (timeType == 'M') {
            date = date2Arr(date, true);
            date[2] = 1;
        }
        else if (timeType == 'Q') {
            date = getQuarterBegin(date, true);
        }

        return retArrOrDate ? date : new Date(date[0], date[1], date[2]);
    };

    var date2Arr = UI_X_CALENDAR_UTIL.date2Arr = function (d, willCreate) {
        return d == null 
            ? d
            : !isArray(d) 
                ? [d.getFullYear(), d.getMonth(), d.getDate()]
                : willCreate
                    ? [d[0], d[1], d[2]]
                    : d;
    };

    var arr2Date = UI_X_CALENDAR_UTIL.arr2Date = function (d, willCreate) {
        return d == null
            ? d
            : isArray(d) 
                ? new Date(d[0], d[1] || 0, d[2] || 1)
                : willCreate
                    ? new Date(d.getFullYear(), d.getMonth(), d.getDate())
                    : d;
    };

    var getQuarterBegin = UI_X_CALENDAR_UTIL.getQuarterBegin = function (date, retArrOrDate) {
        if (!date) { return null; }
        date = date2Arr(date);
        var quarter = getQuarter(date);
        var mon = [0, 0, 3, 6, 9];
        return retArrOrDate 
            ? [date[0], mon[quarter], 1]
            : new Date(date[0], mon[quarter], 1);
    };

    var getQuarter = UI_X_CALENDAR_UTIL.getQuarter = function (date) {
        if (!date) { return null; }
        date = date2Arr(date);
        return Math.floor(date[1] / 3) + 1 ;
    };

    var getWorkday = UI_X_CALENDAR_UTIL.getWorkday = function (date) {
        date = arr2Date(date, true);
        date.setDate(date.getDate() - (6 + date.getDay()) % 7);
        return date;
    };

    var minDate = UI_X_CALENDAR_UTIL.minDate = function (timeType) {
        var args = arguments;
        var m = args[1];
        for (var i = 1, o; i < args.length; i ++) {
            if ((o = args[i]) && compareDate(m, o, timeType) > 0) {
                m = o;
            }
        }
        return m;
    };

    var maxDate = UI_X_CALENDAR_UTIL.maxDate = function (timeType) {
        var args = arguments;
        var m = args[1];
        for (var i = 1, o; i < args.length; i ++) {
            if ((o = args[i]) && compareDate(m, o, timeType) < 0) {
                m = o;
            }
        }
        return m;
    };

    var initSlt = UI_X_CALENDAR_UTIL.initSlt = function (slt, dataWrap) {
        // 清除
        slt.setValue(null);
        while(slt.remove(0)) {}
        // 添加
        for (var i = 0, o; o = dataWrap.list[i]; i++) {
            slt.add(String(o.text), null, { value: o.value });
        }
        slt.setValue(dataWrap.selected);
    };    

    var isDate = UI_X_CALENDAR_UTIL.isDate = function (input) {
        return objProtoToString.call(input) == '[object Date]';
    };

    var isArray = UI_X_CALENDAR_UTIL.isArray = function (input) {
        return objProtoToString.call(input) == '[object Array]';
    };

    var isString = UI_X_CALENDAR_UTIL.isString = function (input) {
        return objProtoToString.call(input) == '[object String]';
    };

    var isNumber = UI_X_CALENDAR_UTIL.isNumber = function (input) {
        return objProtoToString.call(input) == '[object Number]';
    };

    var setSltValue = UI_X_CALENDAR_UTIL.setSltValue = function (sltCtrl, value) {
        sltCtrl && sltCtrl.setValue(value);
    };

    var getSltValue = UI_X_CALENDAR_UTIL.getSltValue = function (sltCtrl) {
        return sltCtrl ? sltCtrl.getValue() : void 0;
    };

    var getWeekInfo = UI_X_CALENDAR_UTIL.getWeekInfo = function (date) {
        var weekDay = date.getDay();
        var pre = -((weekDay + 6) % 7), next = (7 - weekDay) % 7;
        var weekInfo = {
            monday: new Date(date.getTime() + pre * DAY_MILLISECOND), 
            sunday: new Date(date.getTime() + next * DAY_MILLISECOND)
        };
        weekInfo.workday = weekInfo.monday;
        weekInfo.weekend = weekInfo.sunday;
        return weekInfo;
    }

    var cloneADate = UI_X_CALENDAR_UTIL.cloneADate = function (aDate) {
        if (!aDate) {
            return;
        }

        var ret = [];
        for (var i = 0, o; i < aDate.length; i ++) {
            if (o = aDate[i]) {
                ret.push(isDate(o) ? new Date(o.getTime()) : o.slice());
            }
        }

        return ret;
    }

    // function pad(value, count) {
    //     value = (value == null || isNaN(value)) ? '' : String(value);
    //     if (value.length < count) {
    //         value = Array(count - value.length + 1).join('0') + value;
    //     }
    //     return value;
    // }

    // function getDateKey(date) {
    //     if (isDate(date)) {
    //         return [
    //             pad(date.getFullYear(), 4), 
    //             pad(date.getMonth(), 2), 
    //             pad(date.getDate(), 2)
    //         ].join('-');
    //     }
    //     else if (isArray(date)) {
    //         return [
    //             pad(date[0], 4), 
    //             pad(date[1], 2), 
    //             pad(date[2], 2)
    //         ].join('-');
    //     }
    // }

    //-------------------------------------------------------------
    // UI_X_CALENDAR_VIEW
    //-------------------------------------------------------------

    /**
     * 初始化日历控件（公用）。
     *
     * @public
     * @param {Object} options 初始化选项
     */
    var UI_X_CALENDAR_VIEW = 
        inheritsControl(UI_CONTROL, 'ui-x-calendar-view');
    var UI_X_CALENDAR_VIEW_CLASS = UI_X_CALENDAR_VIEW.prototype;

    /**
     * 初始化日历控件的单元格部件。
     * @public
     *
     * @param {Object} options 初始化选项
     */
    UI_X_CALENDAR_VIEW_CELL_CLASS = (
        UI_X_CALENDAR_VIEW_CLASS.Cell = inheritsControl(UI_CONTROL)
    ).prototype;
    UI_X_CALENDAR_VIEW_HCELL_CLASS = (
        UI_X_CALENDAR_VIEW_CLASS.HCell = inheritsControl(UI_CONTROL)
    ).prototype;

    UI_X_CALENDAR_VIEW_CLASS.WEEKNAMES = [
        '一', '二', '三', '四', '五', '六', '日'
    ];
    UI_X_CALENDAR_VIEW_CLASS.MONTH = [
        '一', '二', '三', '四', '五', '六', '七', '八', '九', '十', '十一', '十二'
    ];
    UI_X_CALENDAR_VIEW_CLASS.QUARTER = [
        '第一季度（Q1，一月至三月）', 
        '第二季度（Q2，四月至六月）',
        '第三季度（Q3，七月至九月）', 
        '第四季度（Q4，十月至十二月）'
    ];

    //-------------------------------------------------------------
    // view 公用控件方法
    //-------------------------------------------------------------

    UI_X_CALENDAR_VIEW_CLASS.$setSize = new Function();

    /**
     * 获取日历控件Model。
     *
     * @public
     * @return {Object} model
     */
    UI_X_CALENDAR_VIEW_CLASS.getModel = function () {
        return this._oModel;
    };

    UI_X_CALENDAR_VIEW_CLASS.setView = function (date) {
        this.$setView(date);
        this.$flushByRange();
        this.$flushSelected();
        this.$flushHover();
    };

    UI_X_CALENDAR_VIEW_CLASS.$flushByRange = function() {
        var model = this._oModel;
        var range = model.getRange();
        var start = range.start;
        var end = range.end;
        var aDate = this._oModel.getDate();
        var timeType = model.getTimeType();

        // 周使用日模式展示，所以range也用日模式
        if (timeType == 'W') {
            timeType = 'D';
        }

        this.$forEachCell(
            function (cell, index, inMonth) {
                var currDate = cell.getCellInfo();
                (
                    (!start || compareDate(currDate, start, timeType) >= 0)
                    && (!end || compareDate(end, currDate, timeType) >= 0)
                )
                    ? cell.open()
                    : cell.close();
            }
        )
    };

    UI_X_CALENDAR_VIEW_CLASS.$doFlushSelected = function (getIndexMapFunc, timeType) {
        var model = this._oModel;
        var modelDate = model.getDate();
        var selMode = model.getSelMode();
        var viewInfo = this.getViewInfo();
        var indexMap;
        var range;

        if (selMode == 'RANGE' && modelDate[0] && modelDate[1]) {
            range = modelDate;
        }
        else {
            indexMap = getIndexMapFunc(
                viewInfo, modelDate, this.cellValue2IndexMap
            );
        }

        this.$forEachCell(
            function (cell, index) {
                if (cell._bClosed) { return; }

                var d = cell.getUnitInfo();
                var isIn = range
                    ? (
                        compareDate(d, range[0], timeType) >= 0
                        && compareDate(range[1], d, timeType) >= 0
                    )
                    : index in indexMap;
                var selected;
                var opt;

                isIn
                    ? (selected = true, opt = '+')
                    : (selected = false, opt = '-');

                selected != cell._bSelected && (
                    cell._bSelected = selected,
                    cell.alterClass(opt + 'selected')
                );
            }
        );
    }    

    UI_X_CALENDAR_VIEW_CLASS.$doFlushHover = function (getIndexMapFunc, timeType) {
        var model = this.getModel();
        var selMode = model.getSelMode();
        var modelDate = model.getHoverDate();
        var viewInfo = this.getViewInfo();
        var indexMap;
        var range;

        if (selMode == 'RANGE' && modelDate[0] && modelDate[1]) {
            range = modelDate;
        }
        else {
            indexMap = getIndexMapFunc(
                viewInfo, modelDate, this.cellValue2IndexMap
            );
        }

        this.$forEachCell(
            function (cell, index) {
                if (cell._bClosed) { return; }

                var d = cell.getUnitInfo();
                if (range
                        ? (
                            compareDate(d, range[0], timeType) >= 0
                            && compareDate(range[1], d, timeType) >= 0
                        )
                        : index in indexMap
                ) {

                    !cell._bHover && (
                        cell.alterClass('+hover'),
                        cell._bHover = true
                    );
                } 
                else {
                    cell._bHover && (
                        cell.alterClass('-hover'),
                        cell._bHover = false
                    );
                }
            }
        );
    }

    //-------------------------------------------------------------
    // Cell 公用方法
    //-------------------------------------------------------------

    UI_X_CALENDAR_VIEW_CELL_CLASS.close = function () {
        if (this._bSelected) {
            this.alterClass('-selected');
            this._bSelected = false;
        }
        if (this._bHover) {
            this.alterClass('-hover');
            this._bHover = false;
        }
        this._bClosed = true;
        this.disable();
    };

    UI_X_CALENDAR_VIEW_CELL_CLASS.open = function () {
        this._bClosed = false;
        this.enable();
    }

    UI_X_CALENDAR_VIEW_CELL_CLASS.$mouseover = function (event) {
        var par = this.getParent();
        var cc = this.getUnitInfo();
        par.getModel().updateHoverDate(arr2Date(this.getUnitInfo()), true);
        par.$flushHover();
    };

    UI_X_CALENDAR_VIEW_CELL_CLASS.$mouseout = function (event) {
        var par = this.getParent();
        var cc = this.getUnitInfo();
        par.getModel().updateHoverDate(arr2Date(this.getUnitInfo()), false);
        par.$flushHover();
    };

    UI_X_CALENDAR_VIEW_CELL_CLASS.$click = function(event) {
        var par = this.getParent()
        var model = par.getModel();
        if (model.udateDateByClick(arr2Date(this.getUnitInfo()))) {

            // 更新view
            par.$flushSelected();

            /**
             * @event
             */
            triggerEvent(par, 'change', null, [model.getDate()]);
            /**
             * @event
             */
            triggerEvent(par, 'dateclick', null, [arr2Date(this.getUnitInfo())]);
        }
    };

    //-------------------------------------------------------------
    // MD View
    //-------------------------------------------------------------

   /**
     * 初始化日历控件（month－day）。
     *
     * @public
     * @param {Object} options 初始化选项
     */
    var UI_X_CALENDAR_MD_VIEW = ui.XCalendarMDView =
        inheritsControl(UI_X_CALENDAR_VIEW, null, null, mConstructor);
    var UI_X_CALENDAR_MD_VIEW_CLASS = UI_X_CALENDAR_MD_VIEW.prototype;

    var UI_X_CALENDAR_MD_VIEW_CELL_CLASS = (
        UI_X_CALENDAR_MD_VIEW_CLASS.Cell = 
            inheritsControl(UI_X_CALENDAR_VIEW_CLASS.Cell)
    ).prototype;

    function mConstructor(el, options) {
        var type = this.getType();
        var list = [];
        var i = 0;
        var o;

        this._oModel = options.model;

        el.style.overflow = 'auto';

        for (; i < 7; ) {
            list[i] =
                '<td class="' + type + '-title' + this.Cell.TYPES + (i == 6 ? type + '-title-last' : '') + '">' +
                    this.WEEKNAMES[i++] + '</td>';
        }
        list[i] = '</tr></thead><tbody><tr>';
        for (; ++i < 50; ) {
            list[i] =
                '<td class="' + type + '-item' + this.Cell.TYPES +  (i % 7 ? '' : type + '-item-last') + '"></td>' +
                    (i % 7 ? '' : '</tr><tr>');
        }

        el.innerHTML =
            '<table cellspacing="0" cellpadding="0"><thead><tr>' + list.join('') + '</tr></tbody></table>';

        this._aCells = [];
        list = el.getElementsByTagName('TD');
        for (i = 0; o = list[i]; i ++) {
            // 日历视图单元格禁止改变大小
            this._aCells[i] = $fastCreate(
                i < 7 ? this.HCell : this.Cell, 
                o, 
                this, 
                { resizable: false }
            );
        }
    }

    UI_X_CALENDAR_MD_VIEW_CLASS.$flushSelected = function () {
        return this.$doFlushSelected(getIndexMapByDate, 'D');
    };
    
    UI_X_CALENDAR_MD_VIEW_CLASS.$flushHover = function () {
        this.$doFlushHover(getIndexMapByDate, 'D');
    };

    UI_X_CALENDAR_MD_VIEW_CLASS.$forEachCell = function (callback) {
        var lastDayOfCurrMonth = new DATE(this._nYear, this._nMonth + 1, 0).getDate();

        for (var i = 7, cell; cell = this._aCells[i]; i ++) {
            if (cell._nDay > 0 
                && cell._nDay <= lastDayOfCurrMonth
                && callback.call(this, cell, i) === false
            ) {
                break;
            }
        }
    };

    /**
     * 是否当前的view。例如，2012年4月和2012年3月是两个view
     *
     * @public
     * @param {{Date|Array}} date
     */
    UI_X_CALENDAR_MD_VIEW_CLASS.isCurrView = function (date) {
        date = date2Arr(date);
        return date[0] == this._nYear && date[1] == this._nMonth;
    };

    /**
     * 得到当前的view的信息，用date表示
     *
     * @public
     * @param {Array} viewInfo
     */
    UI_X_CALENDAR_MD_VIEW_CLASS.getViewInfo = function () {
        return [this._nYear, this._nMonth];
    };

    /**
     * 设置日历控件当前显示的日期。
     *
     * @public
     * @param {{Date|Array}} date
     */
    UI_X_CALENDAR_MD_VIEW_CLASS.$setView = function (date) {
        date = date2Arr(date);
        var i = 7;
        var year = date[0];
        var month = date[1];
        // 得到上个月的最后几天的信息，用于补齐当前月日历的上月信息位置;
        var o = new DATE(year, month, 0);
        var day = 1 - o.getDay();
        var lastDayOfLastMonth = o.getDate();
        // 得到当前月的天数;
        var lastDayOfCurrMonth = new DATE(year, month + 1, 0).getDate();
        var model = this._oModel;
        var range = model.getRange();
        var rangeStart = range.start;
        var rangeEnd = range.end;
        var currDate;
        var cellDay;

        if (this._nYear != year || this._nMonth != month) {
            this._nYear = year;
            this._nMonth = month;

            // cell值到_aCell索引的映射，便于查询
            this.cellValue2IndexMap = {};

            currDate = new DATE(year, month, 1);

            for (; o = this._aCells[i]; i ++) {
                if (month = day > 0 && day <= lastDayOfCurrMonth) {
                    currDate.setDate(day);
                    if ((!rangeStart || rangeStart <= currDate) 
                        && (!rangeEnd || rangeEnd >= currDate)) {
                        o.open();
                    }
                    else {
                        o.close();
                    }
                }
                else {
                    o.close();
                }

                if (i == 36 || i == 43) {
                    (o.isDisabled() ? addClass : removeClass)(
                        getParent(o.getOuter()), this.getType() + '-extra'
                    );
                }
                
                cellDay = month 
                    ? day 
                    : day > lastDayOfCurrMonth 
                        ? day - lastDayOfCurrMonth 
                        : lastDayOfLastMonth + day;

                this.setCellHTML 
                    && (this.setCellHTML(o, cellDay, day) !== false) 
                    || setText(o.getBody(), cellDay);

                this.cellValue2IndexMap[day] = i;
                o._nDay = day ++;
            }
        }
    };

    UI_X_CALENDAR_MD_VIEW_CELL_CLASS.getUnitInfo = function () {
        var par = this.getParent();
        return [par._nYear, par._nMonth, this._nDay]
    };

    UI_X_CALENDAR_MD_VIEW_CELL_CLASS.getCellInfo = UI_X_CALENDAR_MD_VIEW_CELL_CLASS.getUnitInfo;

    function getIndexMapByDate(viewInfo, dateArr, cellValue2IndexMap) {
        var ret = {};
        for (var i = 0, date; date = date2Arr(dateArr[i]); i ++) {
            if (date[0] == viewInfo[0] && date[1] == viewInfo[1]) { 
                ret[cellValue2IndexMap[date[2]]] = 1;
            }
        }
        return ret;
    }

    //-------------------------------------------------------------
    // MW View
    //-------------------------------------------------------------

    /**
     * 初始化日历控件（month－week）。
     *
     * @public
     * @param {Object} options 初始化选项
     */
    var UI_X_CALENDAR_MW_VIEW = ui.XCalendarMWView =
        inheritsControl(UI_X_CALENDAR_VIEW, null, null, mConstructor);
    var UI_X_CALENDAR_MW_VIEW_CLASS = UI_X_CALENDAR_MW_VIEW.prototype;

    var UI_X_CALENDAR_MW_VIEW_CELL_CLASS = (
        UI_X_CALENDAR_MW_VIEW_CLASS.Cell = 
            inheritsControl(UI_X_CALENDAR_VIEW_CLASS.Cell)
    ).prototype;

    UI_X_CALENDAR_MW_VIEW_CLASS.$flushSelected = function () {
        return this.$doFlushSelected(getIndexMapByWeekDate, 'W');
    };

    UI_X_CALENDAR_MW_VIEW_CLASS.$flushHover = function () {
        this.$doFlushHover(getIndexMapByWeekDate, 'W');
    };

    UI_X_CALENDAR_MW_VIEW_CLASS.$forEachCell = UI_X_CALENDAR_MD_VIEW_CLASS.$forEachCell;

    UI_X_CALENDAR_MW_VIEW_CLASS.isCurrView = UI_X_CALENDAR_MD_VIEW_CLASS.isCurrView;

    UI_X_CALENDAR_MW_VIEW_CLASS.getViewInfo = UI_X_CALENDAR_MD_VIEW_CLASS.getViewInfo;

    UI_X_CALENDAR_MW_VIEW_CLASS.$setView = UI_X_CALENDAR_MD_VIEW_CLASS.$setView;    

    UI_X_CALENDAR_MW_VIEW_CELL_CLASS.getUnitInfo = function () {
        var par = this.getParent();
        return date2Arr(getWorkday([par._nYear, par._nMonth, this._nDay]));
    };

    UI_X_CALENDAR_MW_VIEW_CELL_CLASS.getCellInfo = function () {
        var par = this.getParent();
        return [par._nYear, par._nMonth, this._nDay];
    };

    function getIndexMapByWeekDate(viewInfo, dateArr, cellValue2IndexMap) {
        var ret = {};
        var year = viewInfo[0];
        var month = viewInfo[1];
        for (var i = 0, date, workday, day; date = date2Arr(dateArr[i]); i ++) {
            // 由于可能跨月，所以本周中有一天匹配，就满足
            workday = getWorkday(date);
            for (var j = 0; j < 7; j ++, workday.setDate(workday.getDate() + 1)) {
                if (workday.getFullYear() == year
                    && workday.getMonth() == month
                ) {
                    ret[cellValue2IndexMap[workday.getDate()]] = 1;
                }
            }
        }
        return ret;
    }

    //-------------------------------------------------------------
    // YM View
    //-------------------------------------------------------------

    /**
     * 初始化日历控件（year－month）。
     *
     * @public
     * @param {Object} options 初始化选项
     */
    var UI_X_CALENDAR_YM_VIEW = ui.XCalendarYMView =
        inheritsControl(UI_X_CALENDAR_VIEW, null, null, ymConstructor);
    var UI_X_CALENDAR_YM_VIEW_CLASS = UI_X_CALENDAR_YM_VIEW.prototype;

    var UI_X_CALENDAR_YM_VIEW_CELL_CLASS = (
        UI_X_CALENDAR_YM_VIEW_CLASS.Cell = 
            inheritsControl(UI_X_CALENDAR_VIEW_CLASS.Cell)
    ).prototype;
    
    function ymConstructor(el, options) {
        var type = this.getType();
        var list;
        var i;
        var o;

        this._oModel = options.model;
        el.style.overflow = 'auto';

        for (i = 0, list = []; i < 12; i ++) {
            list.push('<td class="' + type + '-item'
                +   this.Cell.TYPES + '">'
                +   this.MONTH[i] + "月"
                +   '</td>'
                +   ((i + 1) % 3 ? '' : '</tr><tr>')
            );
        }

        el.innerHTML =
            '<table cellspacing="0"><tbody><tr>'
                +       list.join('')
                +   '</tr></tbody></table>';

        this._aCells = [];
        for (i = 0, list = el.getElementsByTagName('TD'), o;
             o = list[i];
             i ++
        ) {
            // 日历视图单元格禁止改变大小
            this._aCells[i] = $fastCreate(
                this.Cell, o, this, { resizable: false }
            );
            this._aCells[i]._nMonth = i;
        }
    }    

    UI_X_CALENDAR_YM_VIEW_CLASS.$flushSelected = function () {
        return this.$doFlushSelected(getIndexMapByMonth, 'M');
    };

    UI_X_CALENDAR_YM_VIEW_CLASS.$flushHover = function () {
        this.$doFlushHover(getIndexMapByMonth, 'M');
    };

    UI_X_CALENDAR_YM_VIEW_CLASS.$forEachCell = function (callback) {
        for (var i = 0, cell; cell = this._aCells[i]; i ++) {
            if (callback.call(this, cell, i) === false) {
                break;
            }
        }
    };

    UI_X_CALENDAR_YM_VIEW_CLASS.isCurrView = function (date) {
        date = date2Arr(date);
        return date[0] == this._nYear;
    };

    UI_X_CALENDAR_YM_VIEW_CLASS.getViewInfo = function () {
        return [this._nYear];
    };

    UI_X_CALENDAR_YM_VIEW_CLASS.$setView = function (date) {
        date = date2Arr(date);
        this._nYear = date[0];

        // cell值到_aCell索引的映射，便于查询
        if (!this.cellValue2IndexMap) {
            var cellValue2IndexMap = this.cellValue2IndexMap = {};
            for (var i = 0, cell; cell = this._aCells[i]; i ++) {
                cellValue2IndexMap[cell._nMonth] = i;
            }
        }
    };

    UI_X_CALENDAR_YM_VIEW_CELL_CLASS.getUnitInfo = function () {
        var par = this.getParent();
        return [par._nYear, this._nMonth, 1]
    };

    UI_X_CALENDAR_YM_VIEW_CELL_CLASS.getCellInfo = UI_X_CALENDAR_YM_VIEW_CELL_CLASS.getUnitInfo;

    function getIndexMapByMonth(viewInfo, dateArr, cellValue2IndexMap) {
        var ret = {};
        for (var i = 0, date; date = date2Arr(dateArr[i]); i ++) {
            if (date[0] == viewInfo[0]) { 
                ret[cellValue2IndexMap[date[1]]] = 1;
            }
        }
        return ret;
    }

    //-------------------------------------------------------------
    // YQ View
    //-------------------------------------------------------------

    /**
     * 初始化日历控件（year－quarter）。
     *
     * @public
     * @param {Object} options 初始化选项
     */
    var UI_X_CALENDAR_YQ_VIEW = ui.XCalendarYQView =
        inheritsControl(UI_X_CALENDAR_VIEW, null, null, yqConstructor);
    var UI_X_CALENDAR_YQ_VIEW_CLASS = UI_X_CALENDAR_YQ_VIEW.prototype;

    var UI_X_CALENDAR_YQ_VIEW_CELL_CLASS = (
        UI_X_CALENDAR_YQ_VIEW_CLASS.Cell = 
            inheritsControl(UI_X_CALENDAR_VIEW_CLASS.Cell)
    ).prototype;

    function yqConstructor(el, options) {
        var type = this.getType();
        var list;
        var i;
        var o;

        this._oModel = options.model;
        el.style.overflow = 'auto';

        for (i = 0, list = []; i < 4; i ++) {
            list.push('<div class="' + type + '-item'
                +   this.Cell.TYPES + '">'
                +   this.QUARTER[i]
                +   '</div>'
            );
        }

        el.innerHTML = list.join('');

        var quarterMap = [0, 3, 6, 9];
        this._aCells = [];
        for (i = 0, list = el.getElementsByTagName('div'), o;
             o = list[i]; 
             i ++
        ) {
            // 日历视图单元格禁止改变大小
            this._aCells[i] = $fastCreate(
                this.Cell, o, this, { resizable: false } 
            );
            this._aCells[i]._nMonth = quarterMap[i];
        }
    }

    UI_X_CALENDAR_YQ_VIEW_CLASS.$flushSelected = function () {
        return this.$doFlushSelected(getIndexMapByMonth, 'Q');
    };

    UI_X_CALENDAR_YQ_VIEW_CLASS.$flushHover = function () {
        this.$doFlushHover(getIndexMapByMonth, 'Q');
    };

    UI_X_CALENDAR_YQ_VIEW_CLASS.$forEachCell = UI_X_CALENDAR_YM_VIEW_CLASS.$forEachCell;

    UI_X_CALENDAR_YQ_VIEW_CLASS.isCurrView = UI_X_CALENDAR_YM_VIEW_CLASS.isCurrView;

    UI_X_CALENDAR_YQ_VIEW_CLASS.getViewInfo = UI_X_CALENDAR_YM_VIEW_CLASS.getViewInfo;

    UI_X_CALENDAR_YQ_VIEW_CLASS.$setView = UI_X_CALENDAR_YM_VIEW_CLASS.$setView;

    UI_X_CALENDAR_YQ_VIEW_CELL_CLASS.getUnitInfo = UI_X_CALENDAR_YM_VIEW_CELL_CLASS.getUnitInfo;

    UI_X_CALENDAR_YQ_VIEW_CELL_CLASS.getCellInfo = UI_X_CALENDAR_YM_VIEW_CELL_CLASS.getUnitInfo;

})();
