/**
 * di.helper.Util
 * Copyright 2012 Baidu Inc. All rights reserved.
 * 
 * @file:    业务辅助函数集
 * @author:  sushuang(sushuang)
 * @depend:  xutil, tangram.ajax, tangram.json
 */

$namespace('di.helper');
 
(function () {
    
    //----------------------------------------
    // 引用
    //----------------------------------------

    var xlang = xutil.lang;
    var isFunction = xlang.isFunction;
    var isArray = xlang.isArray;
    var isString = xlang.isString;
    var stringToDate = xutil.date.stringToDate;
    var hasValue = xlang.hasValue;
    var hasValueNotBlank = xlang.hasValueNotBlank;
    var decodePercent = xutil.string.decodePercent;
    var sortList = xutil.collection.sortList;
    var dateToString = xutil.date.dateToString;
    var getWorkday = xutil.date.getWorkday;
    var getWeekend = xutil.date.getWeekend;
    var getQuarter = xutil.date.getQuarter;
    var getQuarterBegin = xutil.date.getQuarterBegin;
    var g = xutil.dom.g;
    var isDate = xutil.lang.isDate;
    var arraySlice = [].slice;
    var $fastCreate = ecui.$fastCreate;
    var stringify = baidu.json.stringify;
    var getByPath = xutil.object.getByPath;
    var DOCUMENT = document;
    var ECUI_CONTROL;
    var DIALOG;
    var LANG;
    var REGEXP = RegExp;

    $link(function () {
        ECUI_CONTROL = getByPath('ecui.ui.Control');
        DIALOG = di.helper.Dialog;
        LANG = di.config.Lang;
    });
        
    //----------------------------------------
    // 类型声明
    //----------------------------------------

    var UTIL = $namespace().Util = {};

    var DAY_MILLISEC = 1000 * 60 * 60 * 24; 
    
    //----------------------------------------
    // 方法
    //----------------------------------------

    /**
     * 就是通常用的assert
     * 
     * @public
     * @param {boolean} cond 条件真假
     * @param {string} msg 如果cond为false时的信息
     */
    UTIL.assert = function (cond, msg) {
        if (!cond) {
            throw new Error(msg || 'Assert fail!');
        }
    };

    /**
     * 在控件中初始化自己的主元素，如需使用则放在preprocess最前调用。
     * 用于这种情况：外部逻辑只构造了一个空元素（使控件定位），然后$fastCreate控件，控件自己管理自己的所有行为。
     * 
     * @public
     * @param {ecui.ui.Control} control 控件
     * @param {HTMLElement} el 控件的主元素
     * @param {Object} options 控件的初始化参数
     */
    UTIL.preInit = function (control, el, options) {
        options.primary = control.getType();
        el.className = control.getTypes().join(' ') + el.className;
    };
    
    /**
     * 初始化一个ecui控件
     * 用于这种情况：外部逻辑只构造了一个空元素（使控件定位）。
     * 
     * @public
     * @param {constructor} contorlClass ecui的类
     * @param {HTMLElement} el 控件的主元素
     * @param {ecui.ui.Control} parentControl 父控件
     * @param {Object} options 控件的初始化参数
     * @return {ecui.ui.Control} 创建好的控件
     */
    UTIL.ecuiCreate = function (controlClass, el, parentControl, options) {
        var type = controlClass.types[0];
        options = options || {};
        !options.primary && (options.primary = type);
        el.className = [
            controlClass.TYPES,
            options.primary,
            el.className
        ].join(' ');
        return $fastCreate(controlClass, el, parentControl, options);
    };

    /**
     * 析构名为"_u***"的成员ecui控件
     * 
     * @public
     * @param {Object} container 
     */
    UTIL.disposeInnerControl = function (container) {
        for (var attr in container) {
            /_u\w+/.test(attr) 
                && container[attr]
                && UTIL.ecuiDispose(container[attr]);
        }
    };

    /**
     * 检查dimSel是否全勾选
     *
     * @public
     * @param {string} dimSelStr 
     * @return {boolean} 是否valid
     */
    UTIL.validDimSel = function (dimSelStr) {
        var i, o, oo, arr;
        if (!hasValueNotBlank(dimSelStr)) { return false; }

        arr = dimSelStr.split('|');
        for (i = 0; i < arr.length; i ++) {
            if (!hasValueNotBlank(o = arr[i])) {
                return false;
            }
            oo = o.split(':');
            if (!hasValueNotBlank(oo[0]) || !hasValueNotBlank(oo[1])) {
                return false;
            }
        }  
        return true;
    };

    /**
     * 将dimSel中未选择的项补为选择维度树根节点
     *
     * @public
     * @param {string} dimSelStr 维度选择字符串
     * @param {Object} dimDatasourceMap 维度散列O
     *                  key: dimId, 
     *                  value: { datasource: dimDatasource }
     * @return {string} 补全过的dimSelStr
     */
    UTIL.completeDimSel = function (dimSelStr, dimDatasourceMap) {
        if (!hasValueNotBlank(dimSelStr)) { return false; }

        var dimSelObj = UTIL.parseDimSel(dimSelStr);
        var rootNode;
        for (var dimId in dimSelObj) {
            rootNode = dimDatasourceMap[dimId].datasource.rootNode;
            if (rootNode 
                && (!dimSelObj[dimId] || dimSelObj[dimId].length == 0)
            ) {
                dimSelObj[dimId] = [rootNode.dimNodeId];
            }
        }
        return UTIL.stringifyDimSel(dimSelObj);
    };    

    /**
     * 把字符串格式的dimSel解析成对象
     *
     * @public
     * @param {string} dimSelStr 
     * @return {Object} dimSel对象
     *          格式：{<dimId>: [<dimNodeId>, <dimNodeId>, ...], ...}
     */
    UTIL.parseDimSel = function (dimSelStr) {
        var i, o, oo, ooo, arr, ret = {};
        if (!hasValueNotBlank(dimSelStr)) { return null; }
        arr = dimSelStr.split('|');
        for (i = 0; i < arr.length; i ++) {
            if (!hasValueNotBlank(o = arr[i])) { continue; }
            oo = o.split(':');
            if (!hasValueNotBlank(oo[0])) { continue; }
            ret[oo[0]] = hasValueNotBlank(oo[1]) ? oo[1].split(',') : [];
        }
        return ret;
    };

    /**
     * 把对象格式的dimSel解析成字符串格式
     *
     * @public
     * @param {Object} dimSelObj
     *          格式：{<dimId>: [<dimNodeId>, <dimNodeId>, ...], ...}
     * @return {string} dimSel字符串
     */
    UTIL.stringifyDimSel = function (dimSelObj) {
        var dimId, arr = [];
        if (!dimSelObj) {
            return '';
        }
        for (dimId in dimSelObj) {
            arr.push(dimId + ':' + (dimSelObj[dimId] || []).join(','));
        }
        return arr.join('|');
    };

    /**
     * 得到el的属性里的json
     * 出错时会抛出异常
     * 
     * @private
     * @param {HTMLElement} el dom节点
     * @param {string} attrName 属性名
     * @return {Object} 属性信息
     */
    UTIL.getDomAttrJSON = function (el, attrName) {
        var attr = el.getAttribute(attrName);
        if (attr) {
            return (new Function('return (' + attr + ');'))();
        }
    };

    /**
     * 判断dimSel是否相同
     *
     * @public
     * @param {string} dimSelStr1 要比较的dimSel1
     * @param {string} dimSelStr2 要比较的dimSel2
     * @param {Object} dimIdMap dimId集合，在其key指定的dimId上比较
     * @return {boolean} 比较结果 
     */
    UTIL.sameDimSel = function (dimSelStr1, dimSelStr2, dimIdMap) {
        var dimId, list1, list2, 
            dimSelObj1 = UTIL.parseDimSel(dimSelStr1), 
            dimSelObj2 = UTIL.parseDimSel(dimSelStr2);

        for (dimId in dimIdMap) {
            sortList((list1 = dimSelObj1[dimId]), null, '<', false);
            sortList((list2 = dimSelObj2[dimId]), null, '<', false);
            if (list1.join(',') !== list2.join(',')) {
                return false;
            }
        }
        return true;
    };

    /**
     * 判断某个dim的选择是否相同（都为空算相同）
     *
     * @public
     * @param {Array{string}} dimNodeIdArr1 要比较的dim1
     * @param {Array{string}} dimNodeIdArr2 要比较的dim2
     * @return {boolean} 比较结果 
     */
    UTIL.sameDimNodeIdArr = function (dimNodeIdArr1, dimNodeIdArr2) {
        dimNodeIdArr1 = dimNodeIdArr1 || [];
        dimNodeIdArr2 = dimNodeIdArr2 || [];

        if (dimNodeIdArr1.length != dimNodeIdArr2.length) {
            return false;
        }

        sortList(dimNodeIdArr1, null, '<', false);
        sortList(dimNodeIdArr2, null, '<', false);

        for (var i = 0; i < dimNodeIdArr1.length; i ++) {
            if (dimNodeIdArr1[i] != dimNodeIdArr2[i]) {
                return false;
            }
        }
        return true;
    };

    /**
     * 渲染空表格
     * 
     * @public
     * @param {ecui.ui.LiteTable} tableCon table控件
     * @param {string} text 解释文字，可缺省
     */
    UTIL.emptyTable = function (tableCon, text) {
        var oldText, html = '';

        if (hasValue(text)) {
            oldText = tableCon.getEmptyText();
            tableCon.setEmptyText(text);
        }

        tableCon.setData([]);

        if (hasValue(oldText)) {
            tableCon.setEmptyText(oldText);
        }
    };

    /**
     * 渲染表格的等待状态
     * 
     * @public
     * @param {ecui.ui.LiteTable} tableCon table控件
     */
    UTIL.waitingTable = function (tableCon) {
        UTIL.emptyTable(tableCon, LANG.WAITING_HTML);
    };    

    /**
     * 得到wrap格式的当前选择
     *
     * @public
     * @param {Object} wrap，格式为：
     *          {Array.<Object>} list
     *              {string} text
     *              {*} value
     *          {*} selected
     */
    UTIL.getWrapSelected = function (wrap) {
        for (var i = 0; o = wrap.list[i]; i ++) {
            if (o.value == wrap.selected) {
                return o;
            }
        }
    };

    /**
     * 打印异常
     *
     * @public
     * @param {Error} e 异常对象
     */
    UTIL.logError = function (e) {
        try {
            if (console && console.log) {
                console.log(e);
                (e.message != null) && console.log(e.message);
                (e.stack != null) && console.log(e.stack);
            }
        } 
        catch (e) {
        }
    };

    /**
     * 解析成DI约定的字符串时间格式
     *
     * @public
     * @param {Date|number} date 目标时间或者时间戳
     * @param {string} config.granularity 时间粒度，'D', 'W', 'M', 'Q', 'Y'
     * @param {Object} options 参数
     * @param {boolean} options.firstWeekDay 为true则周数据时格式化成周一，默认false
     */
    UTIL.formatTime = function (date, granularity, options) {
        if (!date) { return; }
        if (!isDate(date)) { date = new Date(date); }
        options = options || {};

        switch (granularity) {
            case 'D': 
                return dateToString(date, 'yyyy-MM-dd');
            case 'W':
                return options.firstWeekDay 
                    // 取周一
                    ? dateToString(getWorkday(date), 'yyyy-MM-dd')
                    // 保留原来日期
                    : dateToString(date, 'yyyy-MM-dd')
            case 'M':
                return dateToString(date, 'yyyy-MM');
            case 'Q':
                return date.getFullYear() + '-Q' + getQuarter(date);
            case 'Y':
                return String(date.getFullYear());
            default: 
                return '';
        }
    };

    /**
     * 由DI约定的字符串时间格式得到Date对象
     *
     * @public
     * @param {Date|string} date 目标时间
     */
    UTIL.parseTime = function (dateStr) {
        if (!dateStr) { return null; }
        if (isDate(dateStr)) { return dateStr; }

        if (dateStr.indexOf('-Q') >= 0) {
            var par = [0, 0, 3, 6, 9];
            dateStr = dateStr.split('-Q');
            return new Date(
                parseInt(dateStr[0], 10), 
                par[parseInt(dateStr[1], 10)], 
                1
            );
        }
        else {
            return stringToDate(dateStr);
        }
    };    

    /**
     * 解析标准化的时间定义。
     * 标准化的时间定义由timeUtil数组组成，或者是单纯的一个timeUnit。
     *（timeUtil定义由parseTimeUtitDef方法规定
     * 例如: 
     *      时间定义可以是一个timeUnit: ['+1D', '+5W'] 或 [null, '-1D', '-3MB']
     *      也可以是timeUnit组成的数组: [['+1D', '+5W'], ['+5W', '+10Q'], ...]
     *
     * @param {(Array.<Array.<string>>|Array.<string>)} def 时间定义
     * @param {(Array.<string>|Array.<Date>)} ref 基准时间
     * @return {Array.<Object>} timeUnitList 结果时间单元数组，
     *      其中每个数组元素的格式见parseTimeUnitDef的返回。
     */
    UTIL.parseTimeDef = function (def, ref) {
        var dArr = [];
        var retArr = [];
        if (isArray(def) && def.length) {
            var def0 = def[0];
            if (isArray(def0)) {
                dArr = def;
            }
            else {
                dArr = [def];
            }
            for (var i = 0, unit; i < dArr.length; i ++) {
                if (isArray(unit = dArr[i])) {
                    retArr.push(UTIL.parseTimeUnitDef(unit, ref));
                }
                else {
                    UTIL.assert('TimeDef illegal: ' + def);
                }
            }
        }

        return retArr;
    };

    /**
     * 解析标准化的时间单元定义
     * 时间单元用于描述一个时间或者一段时间
     * 举例：
     *      从昨天的月初到昨天：[null, "-1D", "0MB"]
     * 
     * @param {Array.<string>} def 时间单元定义，其中：
     *      数组第一个元素表示def.start，即开始时间，
     *                      绝对值（如2012-12-12）
     *                      或相对于基准时间的偏移（如-5d）
     *      数组第二个元素表示def.end 结束时间，格式同上。（可缺省）
     *      数组第三个元素表示def.range 区间，相对于start或end的偏移（如-4d）（可缺省）
     *                  如果已经定义了start和end，则range忽略。
     *                  如果start或end只有一个被定义，则range是相对于它的偏移。
     *                  如果只有start被定义，则只取start。
     *                  例如start是+1ME，range是+5WB，
     *                  表示一个时间范围：从下月的最后一天开始，到下月最后一天往后5周的周一为止。
     * @param {(Array.<string>|Array.<Date>)} ref 基准时间
     *      格式同上，但数组中每个项都是绝对时间
     * @return {Object} timeUnit 结果时间单元
     * @return {Date} timeUnit.start 开始时间
     * @return {Date} timeUnit.end 结束时间
     */
    UTIL.parseTimeUnitDef = function (def, ref) {
        if (!def || !def.length) {
            return null;
        }

        var ret = {};
        var start = def[0];
        var end = def[1];
        var interval = def[2];

        ret.start = UTIL.parseTimeOffset(ref[0], start);
        ret.end = UTIL.parseTimeOffset(ref[1], end);

        // range情况处理
        if ((!start || !end) && interval) {
            var from;
            var to;
            if (start) {
                from = 'start';
                to = 'end';
            }
            else {
                from = 'end';
                to = 'start';
            }
            ret[to] = UTIL.parseTimeOffset(ret[from], interval);
        }
        else if (!end && !interval) {
            ret.end = ret.start;
        }

        return ret;
    };

    /**
     * 解析时间的偏移表达式
     *
     * @public
     * @param {(Date|string)} baseDate 基准时间，
     *      如果为 {string} 则格式为yyyy-MM-dd
     * @param {string} offset 偏移量，
     *      第一种情况是：
     *          用YMDWQ（年月日周季）分别表示时间粒度，
     *          用B/E表示首尾，如果没有B/E标志则不考虑首尾
     *          例如：
     *              假如baseDate为2012-05-09
     *              '+4D'表示baseDate往后4天，即2012-05-13 
     *              '-2M'表示往前2个月（的当天），即2012-03-13
     *              '2Q'表示往后2个季度（的当天），即2012-11-13
     *              '1W'表示往后1周（的当天），即2012-05-20
     *              '1WB'表示往后1周的开头（周一），即2012-05-14
     *              '-1WE'表示往前一周的结束（周日），即2012-05-06
     *              '0WE'表示本周的结束（周日），即2012-05-13
     *              月、季、年同理
     *      第二种情况是：直接指定日期，如yyyy-MM-dd，
     *          则返回此指定日期
     *      第三种情况是：空，则返回空
     * @return {Date} 解析结果
     */
    UTIL.parseTimeOffset = function (baseDate, offset) {
        if (offset == null) { return null; }
        if (!baseDate) { return baseDate; }
        
        if (isString(baseDate)) {
            baseDate = UTIL.parseTime(baseDate);
        }
        offset = offset.toUpperCase();
        
        var t = [
            baseDate.getFullYear(), 
            baseDate.getMonth(), 
            baseDate.getDate()
        ];
        var p = { Y: 0, M: 1, D: 2 };

        if (/^([-+]?)(\d+)([YMDWQ])([BE]?)$/.test(offset)) {
            var notMinus = !REGEXP.$1 || REGEXP.$1 == '+';
            var off = parseInt(REGEXP.$2);
            var timeType = REGEXP.$3;
            var beginEnd = REGEXP.$4;

            if ('YMD'.indexOf(timeType) >= 0) {
                t[p[timeType]] += notMinus ? (+ off) : (- off);
            }
            else if (timeType == 'W') {
                off = off * 7;
                t[p['D']] += notMinus ? (+ off) : (- off);
            }
            else if (timeType == 'Q') {
                off = off * 3;
                t[p['M']] += notMinus ? (+ off) : (- off);
            }
            var ret = new Date(t[0], t[1], t[2]);

            if (beginEnd) {
                if (timeType == 'Y') {
                    beginEnd == 'B'
                        ? (
                            ret.setMonth(0),
                            ret.setDate(1)
                        )
                        : (
                            ret.setFullYear(ret.getFullYear() + 1),
                            ret.setMonth(0),
                            ret.setDate(1),
                            ret.setTime(ret.getTime() - DAY_MILLISEC)
                        );
                }
                else if (timeType == 'M') {
                    beginEnd == 'B'
                        ? ret.setDate(1)
                        : (
                            ret.setMonth(ret.getMonth() + 1),
                            ret.setDate(1),
                            ret.setTime(ret.getTime() - DAY_MILLISEC)
                        );
                }
                else if (timeType == 'W') {
                    ret = (beginEnd == 'B' ? getWorkday : getWeekend)(ret);
                }
                else if (timeType == 'Q') {
                    (beginEnd == 'B') 
                        ? (ret = getQuarterBegin(ret))
                        : (
                            ret.setMonth(ret.getMonth() + 3),
                            ret = getQuarterBegin(ret),
                            ret.setTime(ret.getTime() - DAY_MILLISEC)
                        );
                }
            }

            return ret;
        }
        else {
            return UTIL.parseTime(offset);
        }
    };

    /**
     * 季度格式解析，格式形如：2012-Q1
     *
     * @param {string} dateStr 季度字符串
     * @return {Date} 季度第一天日期
     */
    UTIL.parseQuarter = function (dateStr) {
        var par = [0, 0, 3, 6, 9];
        dateStr = dateStr.split('-Q'); 
        return new Date(
            parseInt(dateStr[0], 10), 
            par[parseInt(dateStr[1], 10)], 
            1
        );
    };

    /**
     * json stringify
     *
     * @param {Object} obj 对象
     * @return {string} json 字符串
     */
    UTIL.jsonStringify = function (obj) {
        return obj ? stringify(obj) : '';
    };
    
    /**
     * ecui 发事件
     * 没有ecui时则直接返回
     *
     * @param {ecui.ui.Control} control ECUI 控件
     * @param {string} name 事件名称
     * @param {Object} event 事件对象
     * @param {Array} args 事件参数
     */
    UTIL.ecuiTriggerEvent = function (control, name, event, args) {
        if (!ecui) { return; }
        return ecui.triggerEvent(control, name, event, args);
    };  

    /**
     * ecui 添加监听器
     * 没有ecui时则直接返回
     *
     * @param {ecui.ui.Control} control ECUI 控件
     * @param {string} name 事件名称
     * @param {Function} caller 监听函数
     * @param {boolean=} once 是否只执行一次就注销
     */
    UTIL.ecuiAddEventListener = function (control, name, caller, once) {
        if (!ecui) { return; }

        var newCaller = once 
            ? function () {
                // 运行一次后就注销自己
                ecui.removeEventListener(control, name, arguments.callee);
                // 执行原来caller
                return caller.apply(this, arguments);
            }
            : caller;

        return ecui.addEventListener(control, name, newCaller);
    };    

    /**
     * ecui 析构控件
     * 没有ecui时则直接返回
     *
     * @param {ecui.ui.Control|HTMLElement} control 
     *      需要释放的控件对象或包含控件的 Element 对象
     */
    UTIL.ecuiDispose = function (control) {
        ecui && ecui.dispose(control);
    };

    /**
     * 是否是ecui控件
     *
     * @param {Object} obj 对象
     * @return {boolean} 是否是ecui控件
     */
    UTIL.isEcuiControl = function (obj) {
        return !!(ECUI_CONTROL && obj instanceof ECUI_CONTROL);
    };

    /**
     * 使用GET方式下载
     * 如果请求参数过长（如在ie下长于2000），则不应使用此方法，否则会url被截断，并且可能发不出请求（ie）
     * @see UTILdownload
     */
    UTIL.downloadByGet = function (url, onfailure, showDialog) {
        onfailure = onfailure || new Function();

        var failureHandler = showDialog 
            ? function () {
                DIALOG.alert(LANG.SAD_FACE + LANG.DOWNLOAD_FAIL, onfailure);
            }
            : onfailure;

        var elDownload = g(downloadIfrId);
        if (!elDownload) {
            elDownload = DOCUMENT.createElement('iframe');
            elDownload.id = downloadIfrId;
            elDownload.style.display = 'none';
            DOCUMENT.body.appendChild(elDownload);
        }

        elDownload.onload = function () {
            var doc = elDownload.contentWindow.document;
            
            if (doc.readyState == 'complete' || doc.readyState == 'loaded') {
                failureHandler();
            }

            elDownload.onload = null;
        };

        // 开始下载
        elDownload.src = url;
    };

    /**
     * 使用POST方式下载
     * @see UTIL.download
     */
    UTIL.downloadByPost = function (url, onfailure, showDialog) {
        onfailure = onfailure || new Function();

        var failureHandler = showDialog 
            ? function () {
                DIALOG.alert(LANG.SAD_FACE + LANG.DOWNLOAD_FAIL, onfailure);
            }
            : onfailure;

        var elIfr = g(downloadIfrId);
        if (!elIfr) {
            elIfr = DOCUMENT.createElement('iframe');
            elIfr.id = downloadIfrId;
            //elIfr.name = downloadIfrName;
            elIfr.style.display = 'none';
            DOCUMENT.body.appendChild(elIfr);
            elIfr.contentWindow.name = downloadIfrName;
        }

        var elForm = g(downloadFormId);
        if (!elForm) {
            elForm = DOCUMENT.createElement('form');
            elForm.id = downloadFormId;
            elForm.method = 'POST';
            elForm.target = downloadIfrName;
            elForm.style.display = 'none';
            DOCUMENT.body.appendChild(elForm);
        }
        else {
            elForm.innerHTML = '';
        }

        // 组织请求参数
        var urla = url.split('?');
        var urlParams = urla[1];

        // Dirty solution by MENGRAN at 2013-12-31
        var rbk = "?_rbk=";
        if (urlParams) {
            urlParams = urlParams.split('&');

            for (var i = 0, pa, ipt; i < urlParams.length; i ++) {
                if (!urlParams[i]) { continue; }

                pa = urlParams[i].split('=');
                if (pa[0]) {
                    ipt = document.createElement('INPUT');
                    ipt.type = 'hidden';
                    ipt.value = decodePercent(pa[1]);
                    ipt.name = decodePercent(pa[0]);
                    elForm.appendChild(ipt);
                    if (pa[0] === "_rbk") {
                        rbk = rbk + pa[1];
                    }
                }
            }
        }
        elForm.action = urla[0] + rbk;

        // 开始下载
        elForm.submit();
    };

    /**
     * 下载
     * 只支持下载失败的判断。
     * （在iframe的onload中使用readyState判断，如果下载成功则不会走onload）
     * 默认情况失败则弹窗提示。
     *
     * @public
     * @param {Object} url 链接和参数
     * @param {Function} onfailure 失败的回调
     * @param {boolean} showDialog 显示对话框提示。默认不显示。
     */
    UTIL.download = UTIL.downloadByPost;

    /**
     * 新开窗口
     *
     * @public
     * @param {string} url 目标url
     */
    UTIL.targetBlank = function (url) {
        var doc = document;
        var body = doc.body;
        var el = doc.createElement('a');
        el.style.display = 'none';
        el.href = url || '#';
        el.target = '_blank';
        body.appendChild(el);
        el.click();
        body.removeChild(el);
    };

    // 原生的
    UTIL.foreachDo = function (list, method, args) {
        $foreachDo.call(this, false, list, method, arraySlice.call(arguments, 2));
    };
    
    UTIL.foreachDoOri = function (list, method, args) {
        $foreachDo.call(this, true, list, method, arraySlice.call(arguments, 2));
    };

    /**
     * 对每个对象，执行方法
     *
     * @public
     * @param {Array} list 要执行方法的对象列表
     * @param {(string|Function)} method 要执行的方法名或者方法本身
     * @param {boolean=} origin true则强制使用原生的方法，就算有$di也不使用di方法
     *      没有通过diFactory创建的控件需要这么做。默认为false
     * @param {Array} args参数
     */
    function $foreachDo(origin, list, method, args) {
        for (var i = 0, o; i < list.length; i ++) {
            if (o = list[i]) {
                if (isFunction(method)) {
                    method(o);
                }
                else {
                    // origin true则强制使用原生的方法，就算有$di也不使用di方法
                    // 没有通过diFactory创建的控件需要这么做。默认为false
                    (!origin && o.$di) 
                        ? o.$di(method, 'apply', o, args)
                        : o[method].apply(o, args);
                }
            }
        }
    };

    var downloadIfrId = String(
        'download-iframe-' + Math.round(Math.random() * 10000000)
    );
    var downloadIfrName = downloadIfrId + 'NAME';
    var downloadFormId = String(
        'download-form-' + Math.round(Math.random() * 10000000)
    );

    function naming (attrName, prefix) {
        return prefix + attrName.charAt(0).toUpperCase() + attrName.slice(1);
    }
    
    function attrNaming (attrName, o) {
        var prefix = '';
        if (UTIL.isEcuiControl(o)) {
            prefix = '_u';
        } else if (isArray(o)) {
            prefix = '_a';
        } else if (isFunction(o)) {
            prefix = '_f';
        } else {
            prefix = '_m';
        }
        return naming(attrName, prefix);
    }

    //-------------------------------------------------------
    // 逻辑表达式
    //-------------------------------------------------------

    /**
     * 计算json配置的逻辑表达式
     * 
     * @public
     * @param {Object} jsonLogicExp 表达式
     *      支持与（and）、或（or）非（not）逻辑。
     *      原子语句的判断由使用提供（atomCal）
     *      原子语句必须是对象形式定义
     *      格式例如：（array的第一个元素是操作符，后面是操作数）
     *      [
     *          'and',
     *           [ 
     *               'or',
     *               { someCustomerRule: 'asdf', someValue: 1234 },
     *               { someCustomerRule: 'asdf', someValue: 1234 },
     *               { someCustomerRule: 'asdf', someValue: 1234 }
     *           ],
     *           { someCustomerRule: 'zcvcxz', someValue: 32432 }
     *      ]
     *
     * @param {Function} atomCalFunc 原子语句的计算的回调函数
     *      参数为{Object}格式的原子语句
     *      返回值为{boolean}表示判断结果
     * @return {boolean} 计算结果
     */
    UTIL.evalJsonLogic = function (jsonLogicExp, atomCalFunc) {
        if (!jsonLogicExp || !atomCalFunc) {
            jsonLogicExpError(jsonLogicExp);
        }

        var operator;
        var i;
        var ret;

        // 是逻辑表达式
        if (isArray(jsonLogicExp)) {

            jsonLogicExp.length < 2 && jsonLogicExpError(jsonLogicExp);

            operator = jsonLogicExp[0];
            if (operator == 'and') {
                ret = true;
                for (i = 1; i < jsonLogicExp.length; i ++) {
                    ret = ret && UTIL.evalJsonLogic(
                        jsonLogicExp[i], atomCalFunc
                    );
                }
                return ret;
            }
            else if (operator == 'or') {
                ret = false;
                for (i = 1; i < jsonLogicExp.length; i ++) {
                    ret = ret || UTIL.evalJsonLogic(
                        jsonLogicExp[i], atomCalFunc
                    );
                }
                return ret;
            }
            else if (operator == 'not') {
                return !UTIL.evalJsonLogic(
                    jsonLogicExp[i], atomCalFunc
                );
            }
            else {
                jsonLogicExpError(jsonLogicExp);
            }
        }
        // 是原子语句
        else {
            return atomCalFunc(jsonLogicExp);
        }
    };

    function jsonLogicExpError(jsonLogicExp, msg) {
        throw new Error(
            'Illegle json logic express, ' + (msg || '') 
            + '. ' + stringify(jsonLogicExp)
        );
    }

    //-------------------------------------------------------
    // dom相关 (modified based on tangram and ecui)
    //-------------------------------------------------------

    /**
     * 获取横向滚动量
     * 
     * @public
     * @param {Window} win 指定window
     * @return {number} 横向滚动量
     */
    UTIL.getScrollLeft = function (win) {
        win = win || window;
        var d = win.document;
        return win.pageXOffset || d.documentElement.scrollLeft || d.body.scrollLeft;
    };

    /**
     * 获取纵向滚动量
     *
     * @public
     * @param {Window} win 指定window
     * @return {number} 纵向滚动量
     */
    UTIL.getScrollTop = function (win) {
        win = win || window;
        var d = win.document;
        return win.pageYOffset || d.documentElement.scrollTop || d.body.scrollTop;
    };

    /**
     * 获取页面视觉区域宽度
     *             
     * @public
     * @param {Window} win 指定window
     * @return {number} 页面视觉区域宽度
     */
    UTIL.getViewWidth = function (win) {
        win = win || window;
        var doc = win.document;
        var client = doc.compatMode == 'BackCompat' ? doc.body : doc.documentElement;

        return client.clientWidth;
    };

    /**
     * 获取页面视觉区域高度
     * 
     * @public
     * @param {Window} win 指定window
     * @return {number} 页面视觉区域高度
     */
    UTIL.getViewHeight = function (win) {
        win = win || window;
        var doc = win.document;
        var client = doc.compatMode == 'BackCompat' ? doc.body : doc.documentElement;

        return client.clientHeight;
    };

    /**
     * 获取页面宽度
     *
     * @public
     * @param {Window} win 指定window
     * @return {number} 页面宽度
     */
    UTIL.getWidth = function (win) {
        win = win || window;
        var doc = win.document;
        var body = doc.body;
        var html = doc.documentElement;
        var client = doc.compatMode == 'BackCompat' ? body : doc.documentElement;

        return Math.max(html.scrollWidth, body.scrollWidth, client.clientWidth);
    };

    /**
     * 获取页面高度
     *             
     * @public
     * @param {Window} win 指定window
     * @return {number} 页面高度
     */
    UTIL.getHeight = function (win) {
        win = win || window;
        var doc = win.document;
        var body = doc.body;
        var html = doc.documentElement;
        var client = doc.compatMode == 'BackCompat' ? body : doc.documentElement;

        return Math.max(html.scrollHeight, body.scrollHeight, client.clientHeight);
    };

    /**
     * 解开文件名
     *
     * @public
     */
    UTIL.parseFileName = function (name) {
        if (!name) {
            return {};
        }

        var dotIndex = name.lastIndexOf('.');
        var fileName;
        var extName;

        if (dotIndex >= 0) {
            fileName = name.slice(0, dotIndex);
            extName = name.slice(dotIndex + 1);
        }
        else {
            fileName = name;
        }

        return { fileName: fileName, extName: extName, fullName: name };
    };

    //-------------------------------------------------
    // Deprecated
    //-------------------------------------------------

    /**
     * 注入ui和model的方便方法
     * 
     * @public 
     * @deprecated
     * @usage 例如：util.ref(container, 'abc', o); 
     *        则首先会去container中寻找方法setAbc调用，
     *        如果没有则直接对属性进行赋值：
     *              前缀映射举例：
     *                  {ecui.ui.Control} => _uAbc
     *                  {Array} => _aAbc
     *                  {Function} => _fAbc
     *                  {others} => _mAbc
     * @param {Object} container 目标容器
     * @param {string} attrName 属性名
     * @param {ecui.ui.Contorl|SomeModel|Array|Function} o 被设置内容
     * @return {ecui.ui.Contorl|SomeModel|Array|Function} o 被设置内容
     */
    UTIL.ref = function (container, attrName, o) {
        var f;
        if (isFunction(f = container[naming(attrName, 'set')])) {
            f.call(container, o);
        } else if (hasValue(f = attrNaming(attrName, o))){
            container[f] = o;
        }
        return o;
    };
    
    /**
     * 从对象中得到model的方便方法
     * 
     * @deprecated
     * @public 
     * @usage 例如：util.getModel(container, 'abc'); 
     *        则首先会去container中寻找方法getAbc调用，
     *        如果没有则直接从属性container._mAbc中取
     * @param {Object} container 目标容器
     * @param {string} attrName 属性名
     * @return {SomeModel} o 模型对象
     */
    UTIL.getModel = function (container, attrName) {
        var f;
        if (isFunction(f = container[naming(attrName, 'get')])) {
            return f.call(container);
        } else {
            return container[naming(attrName, '_m')];
        }
    };

    /**
     * 从字符串的"true" "false"转换成true/false
     */
    UTIL.strToBoolean = function (str) {
        return str == 'true';
    };


})();