/**
 * di.shared.vui.HiddenInput
 * Copyright 2014 Baidu Inc. All rights reserved.
 *
 * @file:    隐藏的输入，用于传递报表引擎外部传来的参数
 * @author:  luowenlei(luowenlei)
 * @depend:  xui, xutil
 */








$namespace('di.shared.vui');

(function () {
    
    //------------------------------------------
    // 引用 
    //------------------------------------------

    var inheritsObject = xutil.object.inheritsObject;
    var extend = xutil.object.extend;
    var encodeHTML = xutil.string.encodeHTML;
    var XOBJECT = xui.XObject;

    //------------------------------------------
    // 类型声明 
    //------------------------------------------

    /**
     * 用于传递报表引擎外部传来的参数
     * 
     * @class
     * @extends xui.XView
     * @param {Object} options
     * @param {HTMLElement} options.el 容器元素
     */
    var RANGE_CALENDAR = $namespace().RangeCalendar =
            inheritsObject(XOBJECT, constructor);
    var RANGE_CALENDAR_CLASS = RANGE_CALENDAR.prototype;
    
    //------------------------------------------
    // 方法
    //------------------------------------------

    /**
     * 构造函数
     *
     * @private
     * @param {Object} options 参数
     */
    function constructor(options) {
        var me = this;
        this.renderCalendar(options);
    };

    /**
     * 设置数据
     *
     * @public
     * @param {Object} data 数据
     * @param {(Object|Array}} data.datasource 数据集
     * @param {*} data.value 当前数据
     */
    RANGE_CALENDAR_CLASS.renderCalendar = function (options) {
        var me = this;
        var calendarId = 'range-cal-' + (new Date()).getTime();
        var html = '<div id="' + calendarId + '"></div>';
        options.el.innerHTML = html;
        var cal = esui.create(
            'RangeCalendar',
            {
                "type": "RangeCalendar",
                "name": calendarId,
                "id": calendarId,
                "value": "",
                "endlessCheck": false,
                "isEndless": false,
                "showedShortCut": "昨天,最近7天,上周",
                "extensions": [],
                "renderOptions": {},
                "main": document.getElementById(calendarId)
            }
        );
        esui.config({ uiClassPrefix: 'esui-ui' });
        // 设置可选择的范围，结束时间为昨天以后可以选择
        cal.range.end = new Date(Kalendae.moment().subtract({d:0}).format('YYYY-MM-DD'));
        // 设置默认选择的范围，昨天
        cal.setRawValue({
            begin: new Date(Kalendae.moment().subtract({d:0}).format('YYYY-MM-DD')),
            end: new Date(Kalendae.moment().subtract({d:0}).format('YYYY-MM-DD'))
        });
        cal.render();
        cal.on('change', function (event) {
            var begin = me.getDateStr(event.begin);
            var end = me.getDateStr(event.end);
            me.currentDate = begin + ' - ' + end;
            me.notify('calChangeDate', me.currentDate);
        });
        this.rangeCal = cal;
        this.calendarId = calendarId;
    };
    /**
     * 设置数据
     *
     * @public
     * @param {Object} data 数据
     * @param {(Object|Array}} data.datasource 数据集
     * @param {*} data.value 当前数据
     */
    RANGE_CALENDAR_CLASS.setData = function (data) {
        var startDateOpt = data.rangeTimeTypeOpt.startDateOpt;
        var endDateOpt = data.rangeTimeTypeOpt.endDateOpt;
        var begin;
        var end;
        if (startDateOpt < 0){
            begin = Kalendae.moment().subtract({d:Math.abs(startDateOpt)}).format('YYYY-MM-DD');
        } else {
            begin = Kalendae.moment().add({d:Math.abs(startDateOpt)}).format('YYYY-MM-DD');
        }
        if (endDateOpt < 0){
            end = Kalendae.moment().subtract({d:Math.abs(endDateOpt)}).format('YYYY-MM-DD');
        } else {
            end = Kalendae.moment().add({d:Math.abs(endDateOpt)}).format('YYYY-MM-DD');
        }

        Kalendae.moment().subtract({d:Math.abs(startDateOpt)});
        var setting = {
            begin: new Date(begin),
            end: new Date(end)
        };
        this.currentDate = begin + ' - ' + end;
        this.rangeCal.setRawValue(setting);
        this.rangeCal.render();
        this._oData = data;
    };

    /**
     * 获取时间,将date转换成字符串类型
     *
     * @public
     * @param {Object} date日期对象
     */
    RANGE_CALENDAR_CLASS.getDateStr = function (date) {
        var month = (date.getMonth()+1);
        var day = date.getDate();
        if (month < 10) {
            month = '0' + month;
        }
        if (day < 10) {
            day = '0' + day;
        }
        return date.getFullYear() + '-' + month + '-' + day;
    };
    /**
     * 得到当前值
     *
     * @public
     * @return {*} 当前数据
     */
    RANGE_CALENDAR_CLASS.getValue = function () {
        if (this.currentDate === undefined) {
            // 设置默认值
            var defStart = Kalendae.moment().subtract({d:0}).format('YYYY-MM-DD');
            var defEnd = Kalendae.moment().subtract({d:0}).format('YYYY-MM-DD');
            var setting = {
                begin: new Date(defStart),
                end: new Date(defEnd)
            };
            this.currentDate = defStart + ' - ' + defEnd;
            this.rangeCal.setRawValue(setting);
            this.rangeCal.render();
            return {
                start: defStart,
                end: defEnd,
                granularity: 'D'
            };
        }
        var saveDataObj = this.convertInputValue2SaveData(this.currentDate);
        var data = {
            start: saveDataObj.dataStartStr,
            end: saveDataObj.dataEndStr,
            granularity: 'D'
        };

        return data;
    };

    /**
     * 通过input value获取时间时间格式YYYY-MM-DD
     *
     * @public
     * @return {*} 当前数据
     */
    RANGE_CALENDAR_CLASS.convertInputValue2SaveData = function (inputValue) {
        var dateArr = inputValue.split(' - ');
        return {
            dataStartStr: dateArr[0].replace(' ', ''),
            dataEndStr: dateArr[1].replace(' ', '')
        };
    };

})();


