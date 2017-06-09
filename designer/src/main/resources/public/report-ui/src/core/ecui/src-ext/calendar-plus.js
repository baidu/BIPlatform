/**
 * @author quyatong
 */

(function() {
    var core = ecui,
        ui = core.ui,
        dom = core.dom,
        string = core.string,
        util = core.util,
        disposeControl = core.dispose,
        $fastCreate = core.$fastCreate,
        inheritsControl = core.inherits,
        findControl = core.findControl,
        first = dom.first,
        last = dom.last,
        children = dom.children,
        createDom = dom.create,
        removeDom = dom.remove,
        addClass = dom.addClass,
        removeClass = dom.removeClass,
        setText = dom.setText,
        moveElements = dom.moveElements,
        blank = util.blank,
        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = UI_CONTROL.prototype,
        UI_BUTTON = ui.Button,
        UI_BUTTON_CLASS = UI_BUTTON.prototype,
        UI_INPUT_CONTROL = ui.InputControl,
        UI_INPUT_CONTROL_CLASS = UI_INPUT_CONTROL.prototype,
        UI_INPUT = ui.InputControl, //.Input,
        UI_INPUT_CLASS = UI_INPUT.prototype,
        UI_SELECT = ui.Select,

        attachEvent = util.attachEvent,
        detachEvent = util.detachEvent,
        repaint = core.repaint,
        WINDOW = window,
        UI_IST_CALENDAR = ui.IstCalendar;

    var UI_CALENDAR_PLUS = ui.CalendarPlus = inheritsControl(UI_CONTROL, "ui-calendar-plus", preProcess, process);

    var UI_CALENDAR_PLUS_CLASS = UI_CALENDAR_PLUS.prototype;

    UI_CALENDAR_PLUS_CLASS.Button = inheritsControl(
        UI_BUTTON, 
        null, 
        function(el, options) {
            var o = createDom();
            var type = this.getType();
        
            moveElements(el, o, true);
            el.innerHTML = '<span class="'+ type +'-inner"></span>';
            moveElements(o, el.firstChild, true);

            o = createDom(type + '-icon', '',  'span');
            el.appendChild(o);
        }
    );

    UI_CALENDAR_PLUS_CLASS.$setSize = new Function();
    
    UI_CALENDAR_PLUS_CLASS.setData = function (options) {
        var el = this.getOuter();
        this.$disposeInner();
        el.innerHTML = '';
        this.$setBody(el);

        preProcess.call(this, el, options);
        process.call(this, el, options);
    };

    UI_CALENDAR_PLUS_CLASS.$disposeInner = function (options) {
        // 耿朋啊，我还是很需要整体setDate功能的。
        // 写的较粗，不知会不会有问题。
        for (var key in this) {
            if (this.hasOwnProperty(key) 
                && key.indexOf('_u') >= 0
                && this[key] instanceof UI_CONTROL
            ) {
                disposeControl(this[key]);
                this[key] = null;
            }
        }
    };

    /**
     * 构造函数之前的预处理
     * @param {EcuiElement} ecui元素
     * @param {Ojbect} options 这个对象的东西比较多是传入的ecui的参数
     *
     */
     function preProcess(el, options) {
        var uiType = this.getType(); 
        setDefaultOptions(options);
        var types = options.types;
        var tagNames = options.tagNames;
        var list = [];
        var listBox = [];
        for (var i = 0, item; item = types[i]; i++) {
            if (item == 'D') {
               list.push('<option value="day">' + tagNames[i] + '</option>' );
            } 
            if (item == 'W') {
               list.push('<option value="week">'  + tagNames[i] +  '</option>' );
            } 
            if (item == 'M') {
               list.push('<option value="month">'   + tagNames[i] + '</option>' );
            }
            if (item == 'Q') {
               list.push('<option value="quarter">'  + tagNames[i] + '</option>' );
            }
        }

        listBox.push('<div style="display:none" class="time-box-day"><div data-id="day-calendar" class="ui-calendar"></div></div>');
        listBox.push('<div style="display:none" class="time-box-week"><div data-id="week-calendar" class="ui-calendar"></div></div>');
        listBox.push('<div style="display:none" class="time-box-month"><div class="ui-select"></div><div class="ui-select ml10"></div></div>');
        listBox.push('<div style="display:none" class="time-box-quarter"><div class="ui-select"></div><div class="ui-select ml10"></div></div>');
        var opts = list.join(''); 
        var boxs = listBox.join('');
        var htmls = [];
        //条件选择 默认是全部显示
        htmls.push('   <div class="' + uiType + '-box">' 
                    +     '<span class="' + uiType + '-label">时间粒度：</span>'
                    +     '<select class="' + UI_SELECT.TYPES + '">' 
                    +          opts 
                    +     '</select>'
                    + '</div>'
                  );

        var istCalType = UI_IST_CALENDAR.types[0];
        // htmls.push('<span class="' + uiType + '-btnpre" >&nbsp;&lt;&lt;</span>')
        htmls.push('<span class="'+ istCalType +'-btn-prv '+ istCalType +'-btn"></span>');

        htmls.push('  <div class="' + uiType + '-box">' 
                    +      boxs    
                    + '</div>'
                  );
      
        // htmls.push('<span class="' + uiType + '-btnnext">&nbsp;&gt;&gt;</span>')
        htmls.push('<span class="'+ istCalType +'-btn-nxt '+ istCalType +'-btn"></span>');

        el.innerHTML = htmls.join('');
        
    };

    /**
    * 这是ecui的构造函数
    * @param {EcuiElement} ecui元素
    * @param {Ojbect} options 这个对象的东西比较多是传入的ecui的参数
    */
    function process(el, options) {
        var parse = parseDate;
        var format = formatDate;
        setDefaultOptions(options);
        var me = this;
        me._oRange = options.range;
    
        me.typeList = {
            'day': null,
            'week': null,
            'month': null,
            'quarter': null 
        }
        //默认选中第一个 可以配置
        this._nSelectedType = this._getInnerType(options.types[0]);
        var childs = children(el);
        //条件查询
        var conBox = childs[0];
        var select = children(conBox)[1];

        //上一--按钮和 下一按钮
        // this._uBtnPre = childs[1];
        // this._uBtnNext = childs[3];
        this._uBtnPre = $fastCreate(this.Button, childs[1], this);        
        this._uBtnNext = $fastCreate(this.Button, childs[3], this);

        this._uConditionSelect = $fastCreate(UI_SELECT, select, this, {});
        this._uConditionSelect.$setSize(100, 20);

        //去掉滚轮的
        this._uConditionSelect.$mouseWheel = function() {};
        //注册change事件 可以调到后边
        this._uConditionSelect.$change = conditionChangeHandle(this); 
        
        //时间内容
        var timeWrap = childs[2];  
        var timeBoxs = children(timeWrap); 
        var dayBox = me.typeList['day'] = timeBoxs[0];
        var weekBox = me.typeList['week'] = timeBoxs[1]; 
        var monthBox = me.typeList['month'] = timeBoxs[2]; 
        var quarterBox = me.typeList['quarter'] = timeBoxs[3]; 
        //时间控件的创立，日粒度 
        if (hasType('D', options)) { 
            //创建日粒度的控件
            createDayControl(me, options, dayBox);
        }
        //周粒度的控件
        if (hasType('W', options)) {
            createWeekControl(me, options, weekBox);
        }
        //日粒度的相关的控件
        if (hasType('M', options)) {
            createMonthControl(me, options, monthBox);
        }
        //季度粒度的控件 
        if (hasType('Q', options)) {
            createQuarterControl(me, options, quarterBox);
        }
        this._uBtnPre.onclick = btnPreNextHandle('pre', this);
        this._uBtnNext.onclick = btnPreNextHandle('next', this);

        setTimeout(function() {
            var type = me._uConditionSelect.getValue();
            //显示默认类型
            me._showCalendarByType(type);
            //设置btn的状态
            //bug fix 初始化没有设置按钮状态
            me._setBtnStatus();
          //  core.triggerEvent(me._uConditionSelect, 'change', {}, null);
        }, 100); 
    };

    /**
    * 创建 月粒度的控件
    *
    * @param {EcuiElement} me 控件本身
    * @param {Object} options 构造函数的参数
    * @param {htmlElement} eleBox  包裹占位容器
    *
    */ 
    function createQuarterControl(me, options, eleBox) {

        var parse = parseDate; 
        //对于季度的处理
        var quarterControlYear = me._uQuarterSelectYear 
                               = $fastCreate(UI_SELECT, eleBox.firstChild, me, {});
        var quarterControlQuarter = me._uQuarterSelectQuarter  
                                  = $fastCreate(UI_SELECT, eleBox.lastChild, me, {});
        
        var quarterData = function() {
            var obj = {};
            var range = options.range.quarter;
            var start = parse(range.start, 'quarter');
            var end = parse(range.end, 'quarter');
            var startYear = start.getFullYear();
            var endYear = end.getFullYear();
            var startQ = range.start.split('-')[1];
            var endQ = range.end.split('-')[1];
            var result = [];

            //根据range生成年的数据
            for (var i = startYear; i <= endYear; i++) {
                result.push({ text: i + '', value: i });
            }
            var q = [
                { text: '第一季度', value: 'Q1' }, 
                { text: '第二季度', value: 'Q2' }, 
                { text: '第三季度', value: 'Q3' }, 
                { text: '第四季度', value: 'Q4' } 
            ];
            obj.year = result;
            obj.quarter = q;
            return obj;

        }();

        //季度条件
        quarterControlYear.$setSize(100, 20);
        quarterControlQuarter.$setSize(100, 20);
        setSelectData(quarterControlYear,quarterData.year);
        setSelectData(quarterControlQuarter, quarterData.quarter);
        var defaultDate = options.defaults.quarter;
        var  date =  parse(defaultDate, 'quarter');
        var _year = date.getFullYear();
        var _q = defaultDate.split('-')[1];  
        quarterControlYear.setValue(_year);
        quarterControlQuarter.setValue(_q);

        quarterControlYear.onchange = function() {
            core.triggerEvent(me, 'change', {}, null);
        }
        quarterControlQuarter.onchange = function() {
            core.triggerEvent(me, 'change', {}, null);
        }

    };
    /**
    * 创建 月粒度的控件
    * @param {EcuiElement} me 控件本身
    * @param {Object} options 构造函数的参数
    * @param {htmlElement} eleBox  包裹占位容器
    */ 
    function createMonthControl(me, options, eleBox) {

        var parse = parseDate; 

        var monthControlYear = me._uMonthSelectYear 
        = $fastCreate(UI_SELECT, eleBox.firstChild, me, {});
        var monthControlMonth = me._uMonthSelectMonth 
        = $fastCreate(UI_SELECT, eleBox.lastChild, me, {});     

        //年数据的获取 和月的数据
        var monData = function(options) {
            var obj = {};
            var range = options.range.month;
            var start = parse(range.start, 'month');
            var end = parse(range.end, 'month');
            var startYear = start.getFullYear();
            var endYear = end.getFullYear();
            var startMonth = start.getMonth();
            var endMonth = end.getMonth();

            var result = [ ];
            var resultMon = [];
            var mon = [ '一', '二', '三', '四', '五', '六',
            '七', '八', '九', '十', '十一', '十二'
            ];

            //根据range生成年的数据
            for (var i = startYear; i <= endYear; i++) {
                result.push({ text: i + '', value: i });
            }

            //生成月的数据
            for (var i = 0, item; item = mon[i]; i ++) {
                resultMon.push({ text: item + '月' , value: i });
            }

            obj.year = result; 
            obj.month = resultMon; 
            return obj; 
        }(options);

        //月条件
        setSelectData(monthControlYear, monData.year);
        setSelectData(monthControlMonth, monData.month);
        var defaultDate = options.defaults.month;
        var  date =  parse(defaultDate, 'month');
        var _year = date.getFullYear();
        var _month = date.getMonth();
        //月控件控制大小
        monthControlYear.$setSize(100, 20);
        monthControlMonth.$setSize(100, 20);
        monthControlYear.setValue(_year);
        monthControlMonth.setValue(_month);

        monthControlYear.onchange = function() {
            core.triggerEvent(me, 'change', {}, null);
        }
        monthControlMonth.onchange = function() {
            core.triggerEvent(me, 'change', {}, null);
        }
    
    };
    /**
    * 创建 周粒度的控件
    * @param {EcuiElement} me 控件本身
    * @param {Object} options 构造函数的参数
    * @param {htmlElement} eleBox  包裹占位容器
    */
    function createWeekControl(me, options, eleBox) {
        var parse = parseDate; 
        //周控件
        var weekControl = me._uWeekCalendar
                        = $fastCreate(  UI_IST_CALENDAR, 
                                        eleBox.firstChild,
                                        me,
                                        { 
                                            mode:'WEEK', 
                                            viewMode:'POP',
                                            shiftBtnDisabled: true 
                                        }
                                    );
        var dft = parse( options.defaults.week, 'day' );
        var range = options.range.week;
        var start = parse( range.start, 'day');
        var end = parse( range.end, 'day');
        //开始时候哦周一
        //var startMonday = null;
        //结束时间的 周日
        var endMonday = getMonday(end);
        var endSunday =  new Date(endMonday.getFullYear(), endMonday.getMonth(), endMonday.getDate() + 6);
        var startMonday = getMonday(start);

        //bug fix: 修复设置week的range的时间
        weekControl.setRange(start, end);
        weekControl.setDate(dft);

        weekControl.$setSize(280, 20);
        weekControl.onchange = function() {

            core.triggerEvent(me, 'change', {}, null);
        }
    
    };
    /**
    * 创建日控件
    * @param {EcuiElement} me 控件本身
    * @param {Object} options 构造函数的参数
    * @param {htmlElement} eleBox  包裹占位容器
    */
    function createDayControl (me, options, eleBox) {
        var parse = parseDate; 
        var dayControl = me._uDayCalendar 
                       = $fastCreate(   UI_IST_CALENDAR, 
                                        eleBox.firstChild,
                                        me,
                                        {   
                                            mode:'DAY', 
                                            viewMode:'POP', 
                                            shiftBtnDisabled: true 
                                        }
                                    );
        var dft = parse( options.defaults.day , 'day');
        var range = options.range.day;
        var start = parse(range.start, 'day'); 
        var end = parse(range.end, 'day'); 

        dayControl.setRange(start, end);

        dayControl.setDate(dft);
        //日控件
        dayControl.$setSize(280, 20);
        dayControl.onchange = function() {
            core.triggerEvent(me, 'change', {}, null);
        } 
    
    };

    /**
    * @param {EcuiElement} ele 控件元素
    * @return {Function} 返回onchange的处理函数
    */
    function conditionChangeHandle(ele) {
        var me = ele;
        return function() {
            var value = this.getValue();
            me._showCalendarByType(value);
            
            core.triggerEvent(me, 'change', {}, null);
        };
    } 
    
    /**
    * 设置select的数据
    * @inner
    * @param {ECUIElement} select ecui的选择控件
    * @param {Array[Object]} select ecui的选择控件
    */
    function setSelectData(select, data) {
        data = data || [];
        for (var i = 0, len = data.length; i < len; i++) {
            var item = data[i]; 
            select.add(item.text, i, { value: item.value });
        }
    };

    /**
    *  判断有没有 该类型的控件 目前 只有D M W Q四种
    * @param {string} type
    * @return {boolean} 是否存在
    */
    function hasType(type, options) {
        var types = options.types;
        var result = false;
        for (var i = 0; i < types.length; i ++) {
            if (types[i] === type) {
                result = true; 
                break;
            } 
        }
        return result;
    }; 
    /**
    * @param {string} op 操作的简称 pre next上一日 下一日
    * @param {HtmlElement} el 控件的元素引用 
    * @return {Function} 
    */
    function btnPreNextHandle(op, el) {
        var me = el;
        return function() {
            var type = me._getDateType();
            var today = new Date()
            var cName = this.className || '';

            //如果是灰色 就不做任何处理 其实上一步暂时没有做处理
            if (cName.match(/disable/)) {
                return ; 
            }
            if (type === 'day') {

                var cal = me._uDayCalendar;
                var d = cal.getDate();       
                if (op === 'pre') {
                    d.setDate(d.getDate() - 1); 
                }
                else {
                    d.setDate(d.getDate() + 1); 
                }
                cal.setDate(d);
                //XXX: 注意 控件的setDate是触发onchage事件的，
                //所以 手动对单个控件进行赋值 需要 手动触发onchange事件
                core.triggerEvent(me, 'change', {}, null);
            }
            else if (type === 'week') {
                var cal = me._uWeekCalendar;
                var d = cal.getDate();       
                //bugfix: 修复range的end不是周日，日期选择是周日，点击下一周失败的情况
                if (op === 'pre') {
                    d.setDate(d.getDate() - 7); 
                    //全部设置成周日
                    d = getMonday(d);
                    d.setDate(d.getDate() + 6);
                }
                else {
                    d.setDate(d.getDate() + 7); 
                    //全部设置成周一
                    d = getMonday(d);
                }
                cal.setDate(d);

                core.triggerEvent(me, 'change', {}, null);
            }
            else if (type === 'month') {
                //{type: 'M', date:''}
                var date = me.getDate().date; 
                date = parseDate(date, 'month');
                var cha = (op == 'pre' ? -1 : 1);
                var newDate = new Date(date.getFullYear(), date.getMonth() + cha); 
                newDate = formatDate(newDate, 'month');
                me.setDate({ type: 'M', date: newDate});

                core.triggerEvent(me, 'change', {}, null);
            }
            else if (type === 'quarter') {
                var date = me.getDate().date; 
                date = parseDate(date, 'quarter');
                var cha = (op == 'pre' ? -3 : 3);
                var newDate = new Date(date.getFullYear(), date.getMonth() + cha); 
                newDate = formatDate(newDate, 'quarter');
                me.setDate({ type: 'Q', date: newDate});

                core.triggerEvent(me, 'change', {}, null);
            }
        
        } 
    
    };
    // 设置默认options
    // @inner
    function  setDefaultOptions(options) {
        var parse = parseDate;
        var format = formatDate;
        var today = new Date();
        var tmp = '';
        //types 可能在dom节点设置
        if (Object.prototype.toString.call(options.types) == '[object String]') {
            options.types = options.types.split(',');  
        }
        if (!options.types) {
            options.types = ['D', 'W', 'M', 'Q']
        }
        if (!options.tagNames) {
            options.tagNames = ['日数据', '周数据', '月数据', '季度数据']; 
        }
        //防止没有设置range
        if (!options.range) {
            options.range = {}; 
        }
        var range = options.range;
        if (!range.day) {
            tmp = format(today, 'day');
            range.day = { start: '2008-01-01', end: tmp };

        }
        if (!range.week) {
            tmp = format(today, 'week');
            range.week = { start: '2008-01-01', end: tmp };
        }
        if (!range.month) {
            var end = new Date();
            var month = end.getFullYear();
            if (month > 2011) {
                end = format(end, 'month'); 
            }
            else {
                end = '2012-01'; 
            }
            range.month = { start: '2008-01', end: end }; 
        }
        if (!range.quarter) {

            var end = new Date();
            var q = end.getFullYear();
            if (q > 2011) {
                end = format(end, 'quarter'); 
            }
            else {
                end = '2012-Q1' 
            }
            range.quarter = { start: '2008-01', end: end };
        }
        //设置默认值
        if (!options.defaults) {
            options.defaults = {};
        }

        var date = new Date();
        var dft = options.defaults;
        dft.day = dft.day || format(date, 'day');
        dft.week = dft.week || format(date, 'week');
        dft.month = dft.month || format(date, 'month');
        dft.quarter = dft.quarter || format(date, 'quarter');

    };
    /**
    * @param {string}  strTime
    * @param {string_opt}  type : day or week, 
    * @return {Date}  返回的日期
    */
    function parseDate(strTime, type) {
        var date = null;
        var tmp = [];
        if (strTime == null || strTime == '') {
            return null; 
        }
        if (type === 'day' || type === 'week') {
            tmp = strTime.split('-');
            date = new Date(tmp[0], +tmp[1] - 1, tmp[2]); 
        } 
        else if (type === 'month') {
            tmp = strTime.split('-');
            date = new Date(tmp[0], +tmp[1] - 1, 1); 
        }
        else if (type === 'quarter') {
            tmp = strTime.split('-');
            q = strTime.slice(-1);
            date = new Date(tmp[0], q * 3 - 3, 1); 
        }
        return date;
    };

    /**
    * Date对象转为字符串的形式 2012-01-12
    * @param {Date} date
    * @param {Date} type 输入的日期类型 : day or week, month, quarter
    * @return {string}  返回字符串
    */
    function formatDate(date, type) {
        if (!date || '[object Date]' != Object.prototype.toString.call(date)) {
            return ''; 
        }
        type = type || 'day'; 
        var year = date.getFullYear();
        var month = date.getMonth() + 1;
        var day = date.getDate();
        var str = [];
        if (type === 'day' || type === 'week') {
            str.push(year);
            str.push(month < 10 ? '0' + month : month);
            str.push(day < 10 ? '0' + day : day);
        } 
        else if (type === 'month') {
            str.push(year);
            str.push(month < 10 ? '0' + month : month);
        }
        else if (type === 'quarter') {
            str.push(year);
            var q = Math.ceil(month / 3);
            str.push('Q' + q);
        }
        return str.join('-');
    };


    /**
    *  设置按钮的样式
    *  @inner
    */
    function setBtnStatus()  {
        var me = this;
        var gran = me._getDateType();
        var range = me._oRange;
        var today = range;
        var btnPre = me._uBtnPre;
        var btnNext = me._uBtnNext;
        //today = new Date(today.getFullYear(), today.getMonth(), today.getDate())
        var cName = this.getType() + '-btn-disable';
            //天粒度
        if (gran == 'day') {
            var d = me.getDate();       
            today = parseDate(range.day.end, 'day');
            var start = parseDate(range.day.start, 'day');

            d = parseDate(d.date, 'day');
            if (d.getTime() >= today.getTime()) {
                btnNext.disable();
                // addClass(btnNext, cName); 
            } 
            else {
                btnNext.enable();
                // removeClass(btnNext , cName); 
            }
            //对于上一按钮的处理
            if (d.getTime() <= start.getTime()) {
                btnPre.disable();
                // addClass(btnPre, cName); 
            } 
            else {
                btnPre.enable();
                // removeClass(btnPre , cName); 
            } 
        }
        //周粒度
        else if (gran == 'week') {
            var d = me.getDate();       
            d = parseDate(d.date, 'day');
            today = parseDate(range.week.end, 'week');
            var monday = getMonday(today);
            var start = parseDate(range.week.start, 'week');
            if (d.getTime() >= monday.getTime()) {
                btnNext.disable();
                // addClass(btnNext , cName); 
            } 
            else {
                btnNext.enable();
                // removeClass(btnNext , cName); 
            }
            monday = getMonday(start);
            if (d.getTime() <= monday.getTime()) {
                btnPre.disable();
                // addClass(btnPre , cName); 
            } 
            else {
                btnPre.enable();
                // removeClass(btnPre , cName); 
            }
        }
        //月粒度
        else if (gran == 'month') {
            var year = me._uMonthSelectYear.getValue();
            var month = me._uMonthSelectMonth.getValue();
            var d = parseDate(range.month.end, 'month');
            //开始时间
            var ds = parseDate(range.month.start, 'month');
            var ds_month =  ds.getMonth();
            var ds_year =  ds.getFullYear();

            var d_year = d.getFullYear();
            var d_month = d.getMonth();
            var big = false;
            if (   year > d_year 
                || ((year == d_year) && (month >= d_month)) 

                //bugfix: 开始范围的需要 超过之后+1
                || ((year == ds_year) && (month + 1 < ds_month))
            ) {
                big = true;
            }
            if (big) {
                btnNext.disable();
                // addClass(btnNext , cName); 
            } 
            else {
                btnNext.enable();
                // removeClass(btnNext , cName); 
            }
            d = parseDate(range.month.start, 'month');

            var de = parseDate(range.month.end, 'month');
            de_month = de.getMonth();
            de_year = de.getFullYear();
            d_year = d.getFullYear();
            d_month = d.getMonth();
            var small = false;
            if (
                    year < d_year 
                || ((year == d_year) && (month <= d_month))

                //bugfix: 结束范围的需要 超过之后-1
                || ((year == de_year) && (month - 1 > de_month))
            ) {
                small = true;
            }
            if (small) {
                btnPre.disable();
                // addClass(btnPre , cName); 
            } 
            else {
                btnPre.enable();
                // removeClass(btnPre , cName); 
            }
        }
        //季度粒度
        else if (gran == 'quarter') {
            var year = me._uQuarterSelectYear.getValue();
            var month = me._uQuarterSelectQuarter.getValue();
            //结束range
            var d = parseDate(range.quarter.end, 'quarter');
            //开始range
            var ds = parseDate(range.quarter.start, 'quarter');
            var ds_year = ds.getFullYear();
            var ds_q = _getQ(ds.getMonth() + 1 );

            var d_year = d.getFullYear();
            var d_q = _getQ(d.getMonth() + 1 );
            var big = false;
            if (   year > d_year 
                || ((year == d_year) && month >= d_q)

                //bugfix: 开始范围的q需要 超过之后-1
                || ((year == ds_year) && +(month.slice(1)) + 1 < ds_q.slice(1)) 
            ) {
                big = true;
            }
            if (big) {
                btnNext.disable();
                // addClass(btnNext ,cName); 
            } 
            else {
                btnNext.enable();
                // removeClass(btnNext , cName); 
            }
            d = parseDate(range.quarter.start, 'quarter');
            d_year = d.getFullYear();
            d_q = _getQ(d.getMonth() + 1 );

            var de = parseDate(range.quarter.end, 'quarter');
            de_year = de.getFullYear();
            de_q = _getQ(de.getMonth() + 1 );

            var small = false;
            if (   year < d_year 
                || ((year == d_year) && month <= d_q)
                //bugfix: 结束范围的q需要 超过之后-1
                || ((year == de_year) && +month.slice(1) - 1 > de_q.slice(1))
            
            ) {
                small = true;
            }
            if (small) {
                btnPre.disable();
                // addClass(btnPre, cName); 
            } 
            else {
                btnPre.enable();
                // removeClass(btnPe, cName); 
            }
        }
        /**
        * @param {number} month 月份 从1月开始
        * @return {String} 返回字符串类型 
        */
        function _getQ(month) {
            var q = '';
            if (month >= 1 && month <= 3) {
                q = 'Q1'; 
            }
            else if (month >= 4 && month <= 6) {
                q = 'Q2'; 
            }
            else if (month >= 7 && month <= 9) {
                q = 'Q3'; 
            }
            else if (month >= 10 && month <= 12) {
                q = 'Q4'; 
            }
            return q;
        }

    };

    /**
    * 获取星期一
    * @param {Date} date 需要转化的时间
    */
    function getMonday(date) {
        var day = date.getDay();
        var dd = date.getDate();
        var yyyy = date.getFullYear();
        var mm = date.getMonth();
        var monday = null;
        var distance = 0;
        if (day >= 1) {
            dd -= day - 1; 
        }
        else {
            dd -= 6; 
        }
        monday =  new Date(yyyy, mm, dd); 
        return monday;
    };

  

    /**
    * 获取选择时间
    * @return {Object} obj
    * @return {Object} obj.type 'M' 时间类型
    * @return {Object} obj.date '1900-01' 时间格式
    */
    function getDate() {
        // day week, month quarter
        var type = this._getDateType();
        var date = null;
        var result = {
            type: 'D',
            date: ''
        };
        if (type === 'day') {
            date = this._uDayCalendar.getDate(); 
            date = formatDate(date);
            result = {
                'type': 'D',
                'date': date
            } 
        }
        else if (type === 'week') {
            date = this._uWeekCalendar.getDate(); 
            date = getMonday(date);
            date = formatDate(date);
            result = {
                type: 'W',
                date: date 
            }
        }
        else if (type === 'month') {
            var year = this._uMonthSelectYear.getValue();
            var month = this._uMonthSelectMonth.getValue();
            date = new Date(year, month, 1); 
            date = formatDate(date, 'month');
            result = {
                type: 'M',
                date: date
            }
        }
        else if (type === 'quarter') {
            var year = this._uQuarterSelectYear.getValue();
            var quarter = this._uQuarterSelectQuarter.getValue();
            if (!year || !quarter) {
                date = '';
            }
            else {
                date = year + '-' + quarter;
                result = {
                    type: 'Q',
                    date: date
                } 
            }
        }
        return result;
    };

    /**
    * @param {string} type 控件 type： day week  month year
    * @  暴露给控件的prototype上
    */
    function showCalendarByType(type) {
        
        this._uConditionSelect.setValue(type);

        var typeList = this.typeList;
        var value = type;
        var preType = this._nSelectedType;
        typeList[preType].style.display = 'none';
        //value == day, week, month , quarter
        typeList[value].style.display = 'block';
        //设置当然选中的type
        this._nSelectedType = value;
       
    }
    /**
    *  设置控件的时间
    * @param {Object} obj
    * @param {string} obj.type 时间控件类型  'M', 'D', 'W', 'Q'
    * @param {string} obj.date 时间控件的具体值 1988-03
    */
    function setDate(obj) {
        var type = obj.type || 'M'; 
        var innerType = this._getInnerType(type);
        var date = obj.date;
        var currentType = this._nSelectedType;
        if (!date) {
            return ; 
        }
        //日期
        if (type === 'D') {
            var d = parseDate(date, 'day');
            this._uDayCalendar.setDate(d); 
            innerType = 'day';
        } 
        else if (type === 'W') {
            var d = parseDate(date, 'week');
            d = getMonday(d);
            this._uWeekCalendar.setDate(d); 
            innerType = 'week';
        }
        else if (type === 'M') {
            var d = parseDate(date, 'month');
            var year = d.getFullYear();
            var month = d.getMonth();
            this._uMonthSelectYear.setValue(year);
            this._uMonthSelectMonth.setValue(month);
            innerType = 'month';
        }
        else if (type === 'Q') {
            var d = parseDate(date, 'quarter');
            if (date.length == 7) {
                var year = date.slice(0, 4);
                var q = date.slice(-2);
            }
            this._uQuarterSelectYear.setValue(year);
            this._uQuarterSelectQuarter.setValue(q);
            innerType = 'quarter';
        }

        if (innerType != currentType) {
            this._showCalendarByType(innerType);
        }

        //core.triggerEvent(this, 'change', {}, null);
    };

    
    /**
    * @param {Object} options 构造函数里的options参数 很多东西的
    * @param {Array<String>} types   'D', 'M' ===
    * @param {Object} range 构造函数里的options参数 很多东西的
    * @param {String} range.type 构造函数里的options参数 很多东西的
    * @param {String} range.date  时间日期  1988-01-03
    */
    function render(options) {
        detachEvent(WINDOW, 'resize', repaint); 
        var el = this.getOuter();
        //卸载内部子控件
        for (key in this) {
            if (/_u\w+/.test(key)) {
                disposeControl(this[key]);
            }
        }
        el.innerHTML = '';
        UI_CALENDAR_PLUS.client.call(this, el, options);
        this.cache(true, true);
        this.init();

        this.$resize();
        //恢复
        attachEvent(WINDOW, 'resize', repaint);
    
    }


    /**
    * @private 私有方法
    * @param {String} type  获取 外部的D，M，W 等 对应的内部名称
    * @return {String}返回内部对应的名称
    */
    UI_CALENDAR_PLUS_CLASS._getInnerType = function(type) {

        //获取外部的简称 对应内部的类型
        var dic = {
            'D': 'day',
            'W': 'week',
            'M': 'month',
            'Q': 'quarter'
        } 
        return dic[type];
    }
    /**
    * @private 内部方法 检测设置 控件的可用样式
    * @param {Date=} 可以传入时间
    */
    UI_CALENDAR_PLUS_CLASS._setBtnStatus = setBtnStatus;

    /**
    * @private
    * @param {string} type 日期的类型 day week month year
    */
    UI_CALENDAR_PLUS_CLASS._showCalendarByType = showCalendarByType;

    /**
    * @private   内部调用 跟外部的接口可能不符合
    * 获取当前的时间类型
    * @return {string}  
    */
    UI_CALENDAR_PLUS_CLASS._getDateType = function() {
        return this._nSelectedType; 
    };

    /**
    * @inner 内部作用，设置时间的时候  处理按钮的可选
    */
    UI_CALENDAR_PLUS_CLASS.$change = function() {
        this._setBtnStatus(); 
    };

    /**
    * 重新渲染时间控件  
    * @param {Object} options 传入构造参数重新刷新
    *
    */
    UI_CALENDAR_PLUS_CLASS.render = render;

    /**
    *  设置控件的时间
    * @param {Object} obj
    * @param {string} obj.type 时间控件类型  'M', 'D', 'W', 'Q'
    * @param {string} obj.date 时间控件的具体值 1988-03
    */
    UI_CALENDAR_PLUS_CLASS.setDate = setDate;

    /**
    * 获取选择时间
    * @return {Object} obj
    * @return {Object} obj.type 'M' 时间类型
    * @return {Object} obj.date '1900-01-02' 时间格式
    */
    UI_CALENDAR_PLUS_CLASS.getDate = getDate;

   

    })();
