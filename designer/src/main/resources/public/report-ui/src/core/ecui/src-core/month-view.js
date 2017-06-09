/*
MonthView - 定义日历显示的基本操作。
日历视图控件，继承自基础控件，不包含年/月/日的快速选择与切换，如果需要实现这些功能，请将下拉框(选择月份)、输入框(输入年份)等组合使用建立新的控件或直接在页面上布局并调用接口。

日历视图控件直接HTML初始化的例子:
<div ecui="type:month-view;year:2009;month:11"></div>

属性
_nYear      - 年份
_nMonth     - 月份(0-11)
_aCells     - 日历控件内的所有单元格，其中第0-6项是日历的头部星期名称
_oRange     - 默认的选择范围，只能通过初始化时的参数进行赋值
_oCurRange  - 当前的选择范围，通过setRange设置，如果没有设置_oCurRange 则使用 _oRange,
              当两者都存在时取交集，确定最小的范围

子控件属性
_nDay       - 从本月1号开始计算的天数，如果是上个月，是负数，如果是下个月，会大于当月最大的天数
*/
//{if 0}//
(function () {

    var core = ecui,
        array = core.array,
        dom = core.dom,
        ui = core.ui,
        util = core.util,

        DATE = Date,

        extend = util.extend,
        indexOf = array.indexOf,
        addClass = dom.addClass,
        getParent = dom.getParent,
        removeClass = dom.removeClass,
        setText = dom.setText,

        $fastCreate = core.$fastCreate,
        inheritsControl = core.inherits,
        triggerEvent = core.triggerEvent,

        UI_CONTROL = ui.Control;
//{/if}//
//{if $phase == "define"}//
    ///__gzip_original__UI_MONTH_VIEW
    ///__gzip_original__UI_MONTH_VIEW_CLASS
    /**
     * 初始化日历控件。
     * options 对象支持的属性如下：
     * year    日历控件的年份
     * month   日历控件的月份(1-12)
     * @public
     *
     * @param {Object} options 初始化选项
     */
    var UI_MONTH_VIEW = ui.MonthView =
        inheritsControl(
            UI_CONTROL,
            'ui-monthview',
            null,
            function (el, options) {
                var i = 0,
                    type = this.getType(),
                    list = [],
                    o;

                el.style.overflow = 'auto';

                for (; i < 7; ) {
                    list[i] =
                        '<td class="' + type + '-title' + this.Cell.TYPES + (i == 6 ? type + '-title-last' : '') + '">' +
                            UI_MONTH_VIEW.WEEKNAMES[i++] + '</td>';
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
                for (i = 0; o = list[i]; ) {
                    // 日历视图单元格禁止改变大小
                    this._aCells[i++] = $fastCreate(this.Cell, o, this, {resizable: false});
                }

                this._oRange = options.range || {};
                this._oCurRange = extend({}, this._oRange);

                this.setDate(options.year, options.month);
                this.setDay(options.day);
            }
        ),
        UI_MONTH_VIEW_CLASS = UI_MONTH_VIEW.prototype,

        /**
         * 初始化日历控件的单元格部件。
         * @public
         *
         * @param {Object} options 初始化选项
         */
        UI_MONTH_VIEW_CELL_CLASS = (UI_MONTH_VIEW_CLASS.Cell = inheritsControl(UI_CONTROL)).prototype;
//{else}//
    UI_MONTH_VIEW.WEEKNAMES = ['一', '二', '三', '四', '五', '六', '日'];

    function UI_MONTH_VIEW_COMPARE(a, b) {
        a = new Date(a.getFullYear(), a.getMonth(), a.getDate());
        b = new Date(b.getFullYear(), b.getMonth(), b.getDate());
        return a >= b;
    }
    
    UI_MONTH_VIEW_CLASS.$setSelected = function (cell) {
        if (this._uCellSel) {
            this._uCellSel.alterClass('-selected');
        }
        if (cell) {
            cell.alterClass('+selected');
            this._uCellSel = cell;
        }
    };

    /**
     * 点击时，根据单元格类型触发相应的事件。
     * @override
     */
    UI_MONTH_VIEW_CELL_CLASS.$click = function (event) {
        var parent = this.getParent(),
            index = indexOf(parent._aCells, this);

        if (triggerEvent(
            parent,
            index < 7 ? 'titleclick' : 'dateclick',
            event,
            index < 7 ? [index] : [new DATE(parent._nYear, parent._nMonth, this._nDay)]
        ) !== false) {
            parent.$setSelected(this);
        }
    };

    /**
     * 获取日历控件当前显示的月份。
     * @public
     *
     * @return {number} 月份(1-12)
     */
    UI_MONTH_VIEW_CLASS.getMonth = function () {
        return this._nMonth + 1;
    };

    /**
     * 获取日历控件当前显示的年份。
     * @public
     *
     * @return {number} 年份(19xx-20xx)
     */
    UI_MONTH_VIEW_CLASS.getYear = function () {
        return this._nYear;
    };

    /**
     * 日历显示移动指定的月份数。
     * 参数为正整数则表示向当前月份之后的月份移动，负数则表示向当前月份之前的月份移动，设置后日历控件会刷新以显示新的日期。
     * @public
     *
     * @param {number} offsetMonth 日历移动的月份数
     */
    UI_MONTH_VIEW_CLASS.move = function (offsetMonth) {
        var time = new DATE(this._nYear, this._nMonth + offsetMonth, 1);
        this.setDate(time.getFullYear(), time.getMonth() + 1);
    };

    UI_MONTH_VIEW_CLASS.setDay = function (day) {
        var list = this._aCells, i, o;
        if (!day) {
            this.$setSelected();
            return;
        }
        for (i = 0; o = list[i]; i++) {
            if (!o.isDisabled() && o._nDay == day) {
                this.$setSelected(o);
                break;
            }
        }
    };

    UI_MONTH_VIEW_CLASS.getDay = function () {
        var res;
        if (this._uCellSel) {
            res = this._uCellSel._nDay;
        }
        return res;
    };

    /**
     * 设置日历控件当前显示的日期。
     * @public
     *
     * @param {number} year 年份(19xx-20xx)，如果省略使用浏览器的当前年份
     * @param {number} month 月份(1-12)，如果省略使用浏览器的当前月份
     */
    UI_MONTH_VIEW_CLASS.setDate = function (year, month) {
        //__gzip_original__date
        var i = 7,
            today = new DATE(),
            year = year || today.getFullYear(),
            month = month ? month - 1 : today.getMonth(),
            // 得到上个月的最后几天的信息，用于补齐当前月日历的上月信息位置
            o = new DATE(year, month, 0),
            day = 1 - o.getDay(),
            lastDayOfLastMonth = o.getDate(),
            // 得到当前月的天数
            lastDayOfCurrMonth = new DATE(year, month + 1, 0).getDate(),
            rangeBegin = this._oCurRange.begin,
            rangeEnd = this._oCurRange.end, currDate, cellDay;

        if (this._nYear != year || this._nMonth != month) {
            this._nYear = year;
            this._nMonth = month;

            currDate = new DATE(year, month, 1);

            for (; o = this._aCells[i++]; ) {
                if (month = day > 0 && day <= lastDayOfCurrMonth) {
                    currDate.setDate(day);
                    if ((!rangeBegin || rangeBegin <= currDate) 
                        && (!rangeEnd || rangeEnd >= currDate)) {
                        o.enable();
                    }
                    else {
                        o.disable();
                    }
                }
                else {
                    o.disable();
                }

                if (i == 36 || i == 43) {
                    (o.isDisabled() ? addClass : removeClass)(getParent(o.getOuter()), this.getType() + '-extra');
                }
                
                cellDay = month ? day : day > lastDayOfCurrMonth ? day - lastDayOfCurrMonth : lastDayOfLastMonth + day;
                this.setCellHTML && (this.setCellHTML(o, cellDay, day) !== false) || setText(o.getBody(), cellDay);
                o._nDay = day++;
            }
        }
    };

    UI_MONTH_VIEW_CLASS.setRange = function(begin, end, overrideOrg) {
        var o, i, range, 
            currDate = new DATE(this._nYear, this._nMonth, 1), 
            lastDayOfCurrMonth = new DATE(this._nYear, this._nMonth + 1, 0).getDate();

        if(overrideOrg === true) {
            this._oRange.begin = begin;
            this._oRange.end = end;
        }

        if(begin) {
            if(this._oRange.begin) {
                begin = UI_MONTH_VIEW_COMPARE(begin, this._oRange.begin) ? begin : this._oRange.begin;
            }
        } 
        else {
            begin = this._oRange.begin;
        }
        if(end) {
            if(this._oRange.end) {
                end = UI_MONTH_VIEW_COMPARE(this._oRange.end, end) ? end : this._oRange.end;
            }
        } 
        else {
            end = this._oRange.end;
        }

        for( i = 0; o = this._aCells[i ++]; ) {
            if(o._nDay > 0 && o._nDay <= lastDayOfCurrMonth) {
                currDate.setDate(o._nDay);
                if(( !begin || UI_MONTH_VIEW_COMPARE(currDate, begin)) 
                && ( !end || UI_MONTH_VIEW_COMPARE(end, currDate))) {
                    o.enable();
                } else {
                    o.disable();
                }
            }
        }

        this._oCurRange.begin = begin;
        this._oCurRange.end = end;
    };


//{/if}//
//{if 0}//
})();
//{/if}//
