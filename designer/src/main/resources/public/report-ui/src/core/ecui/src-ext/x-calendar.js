/**
 * ecui.ui.XCalendar
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    富日历，
 *           支持日、周、月、季不同粒度时间选择，
 *           支持单选、多选、范围选
 * @author:  sushuang(sushuang)
 * @depend:  ecui
 */

/**
 * 配置方式举例：（全不配置也可，就取默认的）
 * {
 *     "forbidEmpty": false,
 *     "disableCancelBtn": false,
 *
 *     // 下文中日周月季所对应的"D"、"W"、"M"、"Q"为内建常量，
 *     // 不能变为其他表示（如不可写为"Day"、"Week"）
 *     "timeTypeList": [
 *         // 此为日周月季的切换下拉框的内容和文字配置
 *         // 例如，如果只要显示“日”和“月”，那么不配置“周”和“季”即可
 *         { "value": "D", "text": "日" },
 *         { "value": "W", "text": "周" },
 *         { "value": "M", "text": "月" },
 *         { "value": "Q", "text": "季" }
 *     ],
 *
 *     "timeTypeOpt": {
 *         // 此为日周月季每个所对应的配置
 *         // 例如，如果只要显示“日”和“月”，那么不配置“周”和“季”即可
 *         "D": {
 *             "date": ["-31D", "-1D"],
 *             "range": {
 *                  start: "2011-01-01",
 *                  end: "-1D",
 *                  offsetBase: new Date()
 *             },
 *             // selModelList表示所需要的时间点选模式
 *             // 可取枚举值（value字段）为"SINGLE"（单选），"RANGE"（首尾范围选择），"MULTIPLE"（离散多选）
 *             "selModeList": [
 *                 { "text": "单选", "value": "SINGLE", "prompt": "单项选择" }
 *             ],
 *             // selModeList表示默认的时间点选模式
 *             "selMode": "SINGLE"
 *         },
 *
 *         "W": {
 *             "date": ["-31D", "-1D"],
 *             "range": {
 *                  start: "2011-01-01",
 *                  end: "-1D",
 *                  offsetBase: new Date()
 *             },
 *             "selModeList": [
 *                 { "text": "单选", "value": "SINGLE", "prompt": "单项选择" },
 *                 { "text": "范围多选", "value": "RANGE", "prompt": "范围选择，点击一下选择开始值，再点击一下选择结束值" }
 *             ],
 *             "selMode": "RANGE"
 *         },
 *
 *         "M": {
 *             "date": ["-31D", "-1D"],
 *             "range": {
 *                  start: "2011-01-01",
 *                  end: "-1D",
 *                  offsetBase: new Date()
 *             },
 *             "selModeList": [
 *                 { "text": "单选", "value": "SINGLE", "prompt": "单项选择" },
 *                 { "text": "范围多选", "value": "RANGE", "prompt": "范围选择，点击一下选择开始值，再点击一下选择结束值" },
 *                 { "text": "散选", "value": "MULTIPLE", "prompt": "多项选择" }
 *             ],
 *             "selMode": "MULTIPLE"
 *         },
 *
 *         "Q": {
 *             "date": ["-31D", "-1D"],
 *             "range": {
 *                  start: "2011-01-01",
 *                  end: "-1D",
 *                  offsetBase: new Date()
 *             },
 *             "selModeList": [
 *                 { "text": "单选", "value": "SINGLE", "prompt": "单项选择" }
 *             ],
 *             "selMode": "SINGLE"
 *         }
 *     }
 */

(function() {

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
    var REGEXP = RegExp;

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
    var extend = util.extend;
    var getWeekInfo = cutil.getWeekInfo;
    var getQuarter = cutil.getQuarter;
    var minDate = cutil.minDate;
    var maxDate = cutil.maxDate;
    var arrProtoSlice = Array.prototype.slice;

    var $fastCreate = core.$fastCreate;
    var inheritsControl = core.inherits;
    var triggerEvent = core.triggerEvent;
    var setFocused = core.setFocused;

    var UI_CONTROL = ui.Control;
    var UI_CONTROL_CLASS = UI_CONTROL.prototype;
    var UI_INPUT_CONTROL = ui.InputControl;
    var UI_INPUT_CONTROL_CLASS = UI_INPUT_CONTROL.prototype;
    var UI_SELECT = ui.Select;
    var UI_BUTTON = ui.Button;
    var UI_LAYER = ui.XCalendarLayer;

    //-------------------------------------------------
    // 类型声明
    //-------------------------------------------------

    /**
     * 日历控件类
     *
     * @class
     * @param {Object} options 初始化选项，除去下面列出的参数，其余参见setDatasource
     * @param {string=} options.headText 最前面的显示文字，默认为：'时间粒度：'
     * @param {string=} options.rangeLinkStr 范围选择模式下，显示出的当前选中时间的连接符，默认为' 至 '
     * @param {string=} options.weekLinkStr 时间类型为周时，显示出的周首尾的连接符，默认为' ~ '
     * @param {string=} options.blankText 当前无选中时显示的文本，默认为'请选择时间' 
     */
    var UI_X_CALENDAR = ui.XCalendar =
        inheritsControl(
            UI_INPUT_CONTROL,
            'ui-x-calendar',
            function(el, options) {
                options.hidden = true;
            },
            function(el, options) {
                var type = this.getTypes()[0];
                var i;
                var item;
                var selected;
                var html = [];
                var domIndex = 0;
                var domIndexTimeType;
                var domIndexInfo;
                var domIndexLayer;
                var shiftBtnDisabled = this._bShiftBtnDisabled = options.shiftBtnDisabled;

                this._oTextOptions = {
                    blankText: options.blankText,
                    rangeLinkStr: options.rangeLinkStr,
                    weekLinkStr: options.weekLinkStr
                };

                // 提示字符
                var headText = options.headText;
                if (headText == null) {
                    headText = '时间粒度：'
                    html.push('<span class="' + type + '-head-text">' + encodeHTML(headText) + '</span>');
                    domIndex ++;
                }

                // 时间类度选择下拉框
                html.push('<select class="'+ type +'-slt-timetype'+ UI_SELECT.TYPES +'">');
                html.push('</select>');
                domIndexTimeType = domIndex ++;

                // 当前选择信息与切换
                html.push(
                    '<span class="' + type + '-btn-prv ' + type + '-btn"></span>',
                    '<span class="' + type + '-text"></span>',
                    '<span class="' + type + '-btn-cancel ' + type + '-btn"></span>',
                    '<span class="' + type + '-btn-cal ' + type + '-btn"></span>',
                    '<span class="' + type + '-btn-nxt ' + type + '-btn"></span>'
                );
                domIndexInfo = domIndex;
                domIndex += 5;

                // 日历layer
                domIndexLayer = domIndex;
                var tList = ['D', 'W', 'M', 'Q'];
                for (i = 0; item = tList[i]; i ++) {
                    html.push('<div class="'+ type +'-layer" style="position:absolute;display:none"></div>');
                    domIndex ++;
                }

                // 以下开始创建子控件实例
                var o = createDom();
                o.innerHTML = html.join('');
                var child = children(o);
                var node;

                // 时间类型选择
                if (domIndexTimeType != null) {
                    this._uTimeTypeSlt = $fastCreate(
                        this.Select, child[domIndexTimeType], this
                    );
                }

                // 显示当前选择文本
                this._eText = child[domIndexInfo + 1];
                
                // prev一天按钮
                node = child[domIndexInfo];
                if (shiftBtnDisabled) {
                    node.style.display = 'none';
                }
                this._uBtnPrv = $fastCreate(
                    this.Button, node, this, { command: 'prv', icon: true }
                );

                // 取消选择按钮
                node = child[domIndexInfo + 2];
                this._uBtnCancel = $fastCreate(
                    this.Button, node, this, { command: 'cancel', icon: true }
                );

                // 小日历按钮
                node = child[domIndexInfo + 3];
                this._uBtnCal = $fastCreate(
                    this.Button, node, this, { command: 'cal', icon: true }
                );

                // next一天按钮
                node = child[domIndexInfo + 4];
                if (shiftBtnDisabled) {
                    node.style.display = 'none';
                }
                this._uBtnNxt = $fastCreate(
                    this.Button, node, this, { command: 'nxt', icon: true }
                );

                // layers
                var layers = this._oLayers = {};
                i = 0;
                for (i = 0; item = tList[i]; i ++) {
                    node = child[domIndexLayer + i];
                    DOCUMENT.body.appendChild(node);
                    // 延后创建
                    layers[item] = node;
                }

                moveElements(o, el, true);

                // 初始化数据
                this.setDatasource(options);
            }
        );

    var UI_X_CALENDAR_CLASS = UI_X_CALENDAR.prototype;

    var UI_X_CALENDAR_BUTTON_CLASS = (
            UI_X_CALENDAR_CLASS.Button = inheritsControl(
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

    var UI_X_CALENDAR_SELECT_CLASS = (
            UI_X_CALENDAR_CLASS.Select = inheritsControl(UI_SELECT, null)
        ).prototype;

    UI_X_CALENDAR_SELECT_CLASS.Options = inheritsControl(
        UI_X_CALENDAR_SELECT_CLASS.Options, 
        null, 
        null, 
        function(el, options) {
            addClass(el, 'ui-x-calendar-select-options');
        }
    );

    var UI_X_CALENDAR_LAYER_CLASS = (
            UI_X_CALENDAR_CLASS.Layer = inheritsControl(UI_LAYER)
        ).prototype;

    var UI_X_CALENDAR_MODEL = UI_X_CALENDAR_LAYER_CLASS.Model;
    var UI_X_CALENDAR_MODEL_CLASS = UI_X_CALENDAR_MODEL.prototype;

    //-------------------------------------------------
    // 常量
    //-------------------------------------------------

    var PATTERN_SHOW_DATE = 'yyyy-MM-dd';
    var PATTERN_SHOW_MONTH = 'yyyy-MM';
    var DATE_ZERO = new Date(0);

    //----------------------------------------------
    // UI_X_CALENDAR_CLASS 的方法
    //----------------------------------------------

    /**
     * 设置数据
     *
     * @param {Object} datasource 初始化选项
     * @param {string} datasource.preText
     * @param {string=} datasource.timeType 初始的时间类度，可为'D'（日）, 'W'（周）, 'M'（月）, 'Q'（季），缺省则取'D'
     * @param {Array=} datasource.timeTypeList 时间粒度选择列表，如果为[]则没有时间粒度选择，如果为null则全部开启
     *      每项结构例如：{ text: '文字文字', value: 'D' }，其中value与timeTypeOpt的key相对应。
     * @param {Object=} datasource.timeTypeOpt 按时间粒度的日历定义，此参数结构可为：
     *      {
     *          D: { ... 日历定义 },
     *          W: { ... 日历定义 },
     *          M: { ... 日历定义 },
     *          Q: { ... 日历定义 }   
     *      }
     *      其中，"日历定义"的参数内容参见x-calendar-layer.js
     * @param {boolean} datasource.disableCancelBtn
     * @param {boolean} datasource.disablePreviousBtn
     * @param {boolean} datasource.disableNextBtn
     * @param {boolean=} datasource.forbidEmpty 禁止时间为空，如果为空，则设置为默认date。默认notEmpty为false
     */    
    UI_X_CALENDAR_CLASS.setDatasource = function (datasource, silent, renderOpt) {
        datasource = datasource || {};

        var timeTypeOpt = datasource.timeTypeOpt || {
            "D": {
                "selMode": "SINGLE",
                "date": [
                    "-31D",
                    "-1D"
                ],
                "range": [
                    "2011-01-01",
                    "-1D"
                ],
                "selModeList": [
                    {
                        "text": "单选",
                        "value": "SINGLE",
                        "prompt": "单项选择"
                    }
                ]
            }
        };
        var timeTypeList = this._aTimeTypeList = datasource.timeTypeList.length > 0
            ? datasource.timeTypeList
            : [
                { text: '日', value: 'D'},
                { text: '周', value: 'W'},
                { text: '月', value: 'M'},
                { text: '季', value: 'Q'}
            ];
        var models = this._oModels = this._oModels || {};
        var timeType = this._sTimeType = datasource.timeType 
            || (timeTypeList.length ? timeTypeList[0].value : void 0);

        if (datasource.disableCancelBtn) {
            this._uBtnCancel.hide();
        }
        if (datasource.disablePreviousBtn) {
            this._uBtnPrv.hide();
        }
        if (datasource.disableNextBtn) {
            this._uBtnNxt.hide();
        }

        // 创建或重置layer的model
        for (var i = 0, t, opt, dft; t = timeTypeList[i]; i ++) {
            t = t.value;
            opt = 
                timeTypeOpt[t] = 
                extend({ timeType: t }, timeTypeOpt[t]);

            // 设默认值
            dft = UI_X_CALENDAR_MODEL_CLASS.DEFAULT;
            if (!opt.selMode) {
                opt.selMode = dft.selMode;
            }
            if (!opt.timeType) {
                opt.timeType = dft.timeType;
            }
            if (!opt.selModeList) {
                opt.selModeList = dft.selModeList;
            }
            if (!opt.defaultDate) {
                opt.defaultDate = opt.date;
            }
            opt.forbidEmpty = datasource.forbidEmpty || false;

            !models[t]
                ? (models[t] = new UI_X_CALENDAR_MODEL(opt))
                : models[t].setDatasource(opt);
        }

        !silent && this.render(renderOpt);
    };

    /** 
     * 渲染
     *
     * @public
     * @param {Object} opt
     * @param {Date} viewDate 决定面板显示的日期
     * @param {boolean} remainSlt 是不时重新绘制日期选择下拉框
     * @param {boolean} remainLayer 是不是保留layer显示
     */  
    UI_X_CALENDAR_CLASS.render = function (opt) {
        opt = opt || {};

        var timeType = this._sTimeType;

        if (!timeType) { return;}

        var models = this._oModels;
        var timeTypeList = this._aTimeTypeList;
        var layers = this._oLayers;

        !opt.remainSlt && this.$resetTimeTypeSlt();

        for (var i = 0, t, layer, isNew; t = timeTypeList[i]; i ++) {
            t = t.value;
            isNew = false;

            // 创建并初始化layer
            if (!(layers[t] instanceof UI_CONTROL)) {
                layers[t] = $fastCreate(
                    this.Layer, layers[t], this, { model: models[t] }
                );
                layers[t].init();
                isNew = true;
            }

            layer = layers[t];

            if (t == timeType) {
                if (layer._bLayerShow && !opt.remainLayer) {
                    layer.hide();
                }
            }
            else {
                layers[t].hide();
            }
        }

        this.$flushThis();
    };    

    UI_X_CALENDAR_CLASS.$setSize = new Function();

    UI_X_CALENDAR_CLASS.$resetTimeTypeSlt = function () {
        var timeTypeList = this._aTimeTypeList;
        var slt = this._uTimeTypeSlt;
        if (!slt) { return; }

        // 清除
        slt.setValue(null);
        while(slt.remove(0)) {}

        // 添加
        for (var i = 0, t, item; t = timeTypeList[i]; i ++) {
            slt.add(String(t.text), null, { value: t.value });
        }

        slt.setValue(this._sTimeType);
    };

    UI_X_CALENDAR_CLASS.$showLayer = function() {
        var layer = this.getCurrLayer();
        var anchor = this._bShiftBtnDisabled 
            ? this._eText : this._uBtnPrv.getOuter();
        var pos = getPosition(anchor);
        var posTop = pos.top + this.getHeight();

        if (!layer._bLayerShow) {
            layer.render({ remainSlt: true, remainSelMode: true });
            layer.show();
            setFocused(layer);

            var height = layer.getHeight();
            layer.setPosition(
                pos.left,
                posTop + height <= getView().bottom 
                    ? posTop : pos.top - height
            );
        }
    }

    UI_X_CALENDAR_CLASS.$clear = function() {
        var model = this.getModel();
        this.getModel().setDatasource({ date: [] });
        this.$flushThis();
    }

    UI_X_CALENDAR_CLASS.$flushThis = function() {
        var curDate = this._oDate;
        var model = this.getModel();

        var txt = this.$getShowText();
        this._eText.innerHTML = txt.shortHTML;
        txt.fullText && this._eText.setAttribute('title', txt.fullText);
        this._uBtnPrv[model.testEdge(-1) ? 'enable' : 'disable']();
        this._uBtnNxt[model.testEdge(1) ? 'enable' : 'disable']();
    }

    UI_X_CALENDAR_CLASS.$getSingleText = function (date) {
        options = this._oTextOptions || {};
        var model = this.getModel();
        var timeType = this._sTimeType;

        if (!date) { return ''; }

        if (timeType == 'D') {
            return formatDate(date, PATTERN_SHOW_DATE);
        }
        else if (timeType == 'W') {
            var weekInfo = getWeekInfo(date);
            var range = model.getRange();
            // 只有week时有range问题，因为week是用日显示的，
            // 当range在半周时会表现出来
            return formatDate(
                    range.start
                        ? maxDate('D', weekInfo.workday, range.start)
                        : weekInfo.workday,
                    PATTERN_SHOW_DATE
                )
                + (options.weekLinkStr || ' ~ ')
                + formatDate(
                    range.end
                        ? minDate('D', weekInfo.weekend, range.end)
                        : weekInfo.weekend, 
                    PATTERN_SHOW_DATE
                );
        }
        else if (timeType == 'M') {
            return formatDate(date, PATTERN_SHOW_MONTH);
        }
        else if (timeType == 'Q') {
            return date.getFullYear() + '-Q' + getQuarter(date);
        }
    };

    UI_X_CALENDAR_CLASS.$getShowText = function () {
        options = this._oTextOptions || {};
        var type = this.getType();
        var model = this.getModel();
        var aDate = model.getDate();
        var timeType = this._sTimeType;
        var selMode = model.getSelMode();
        var shortText;
        var fullText;
        var rangeLinkStr = options.rangeLinkStr || ' 至 ';
        var tmp;

        if (!aDate[0]) {
            shortHTML = [
                '<span class="', type, '-blank', '">',
                    encodeHTML(options.blankText || '请选择时间'),
                '</span>'
            ].join('');
            return { shortHTML: shortHTML, fullText: '' };
        }

        if (selMode == 'SINGLE') {
            fullText = shortText = this.$getSingleText(aDate[0], options);
        }
        else if (selMode == 'RANGE') {
            if (timeType == 'W') {
                shortText = this.$getSingleText(aDate[0], options);
                tmp = this.$getSingleText(aDate[1], options);
                fullText = '[' + shortText + ']'
                    + rangeLinkStr + (tmp ? '[' + tmp + ']' : '');
                shortText += ', ...';
            }
            else {
                shortText = fullText = this.$getSingleText(aDate[0], options) 
                    + rangeLinkStr
                    + this.$getSingleText(aDate[1], options);
            }
        }
        else if (selMode == 'MULTIPLE') {
            shortText = this.$getSingleText(aDate[0], options) + ', ...';
            fullText = [];
            for (var i = 0; i < aDate.length; i ++) {
                fullText.push(this.$getSingleText(aDate[i], options));
            }

            fullText = '[' + fullText.join('], [') + ']';
        }

        return { shortHTML: encodeHTML(shortText), fullText: fullText };
    };    

    UI_X_CALENDAR_CLASS.$click = function(event) {
        UI_INPUT_CONTROL_CLASS.$click.call(this);
        if (event.target == this._eText) {
            this.$showLayer();
        }
    };

    UI_X_CALENDAR_CLASS.$activate = function (event) {
        UI_INPUT_CONTROL_CLASS.$activate.call(this, event);
        this.$showLayer();
    };

    UI_X_CALENDAR_CLASS.$goStep = function(step) {
        this.getModel().goStep(step);
        this.getCurrLayer().render({ remainSlt: true, remainSelMode: true });
        this.$flushThis();
    };

    UI_X_CALENDAR_CLASS.getModel = function() {
        return this.getCurrLayer().getModel();
    };
    
    UI_X_CALENDAR_CLASS.getCurrLayer = function() {
        return this._oLayers[this._sTimeType];
    };
    
    UI_X_CALENDAR_CLASS.getDate = function() {
        return this.getModel().getDate();
    };

    UI_X_CALENDAR_CLASS.getValue = UI_X_CALENDAR_CLASS.getDate;

    UI_X_CALENDAR_CLASS.getTimeType = function() {
        return this.getModel().getTimeType();
    };

    UI_X_CALENDAR_CLASS.getSelMode = function() {
        return this.getModel().getSelMode();
    };

    UI_X_CALENDAR_CLASS.init = function() {
        UI_INPUT_CONTROL_CLASS.init.call(this);
        this._uBtnCal.init();
        this._uBtnCancel.init();
        this._uBtnNxt.init();
        this._uBtnPrv.init();
        this._uTimeTypeSlt.init();
    };

    //----------------------------------------------
    // UI_X_CALENDAR_BUTTON_CLASS 的方法
    //----------------------------------------------

    UI_X_CALENDAR_BUTTON_CLASS.$click = function (event) {
        var par = this.getParent();
        var changed;
        switch(this._sCommand) {
            case 'prv':
                par.$goStep(-1);
                changed = true;
                break;
            case 'nxt':
                par.$goStep(1);
                changed = true;
                break;
            case 'cal':
                par.$showLayer();
                break;
            case 'cancel': 
                par.$clear();
                changed = true;
                break;
        }
        // TODO:找宿爽确认修改是否合理
        if (this._sCommand !== 'cal') {
            /**
             * @event
             */
            triggerEvent(
                par, 'change', null, [par.getModel().getDate().slice()]
            );
        }
//        /**
//         * @event
//         */
//        triggerEvent(
//            par, 'change', null, [par.getModel().getDate().slice()]
//        );

        event.exit();
    };

    //----------------------------------------------
    // UI_X_CALENDAR_SELECT_CLASS 的方法
    //----------------------------------------------

    UI_X_CALENDAR_SELECT_CLASS.onchange = function () {
        var par = this.getParent();
        par._sTimeType = this.getValue();
        par.$flushThis();

        /**
         * @event
         */
        triggerEvent(
            par, 'change', null, [par.getModel().getDate().slice()]
        );
    };

    //--------------------------------------------------------------
    // UI_X_CALENDAR_LAYER_CLASS 的方法
    //--------------------------------------------------------------

    UI_X_CALENDAR_LAYER_CLASS.$blur = function () {
        this.hide();
    };
    
    UI_X_CALENDAR_LAYER_CLASS.onchange = function() {
        var par = this.getParent();
        par.$flushThis();
        this._bLayerChanged = true;
    };    

    UI_X_CALENDAR_LAYER_CLASS.ondateclick = function() {
        var model = this.getModel();
        var selMode = model.getSelMode();
        var aDate = model.getDate();

        if (selMode == 'SINGLE') {
            this.hide();
        }
    };

    UI_X_CALENDAR_LAYER_CLASS.show = function() {
        this._bLayerShow = true;
        this._bLayerChanged = false;
        UI_X_CALENDAR_CLASS.Layer.superClass.show.apply(this, arguments);
    };

    UI_X_CALENDAR_LAYER_CLASS.hide = function() {
        if (this._bLayerShow) {

            var par = this.getParent();
            var model = this.getModel();
            var selMode = model.getSelMode();
            var aDate = model.getDate();

            // 对于范围选择时只选了一半就关掉日历面板的情况，直接补全
            if (selMode == 'RANGE' && aDate[0] && !aDate[1]) {
                aDate[1] = new Date(aDate[0].getTime());
                par.$flushThis();
            }

            this._bLayerShow = false;

            par && triggerEvent(par, 'layerhide');

            if (this._bLayerChanged) {
                triggerEvent(
                    par, 'change', null, [this.getModel().getDate().slice()]
                );
                this._bLayerChanged = false;
            }
        }

        UI_X_CALENDAR_CLASS.Layer.superClass.hide.apply(this, arguments);
    };

})();

