/**
 * xui.ui.HChart
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    基于ECharts的js图
 *           (最早源自pl-charts.js by cxl(chenxinle))
 * @author:  sushuang(sushuang)
 * @depend:  xui, xutil, echarts
 */

(function () {
    /* globals xutil */
    var stringTemplate = xutil.string.template;
    var addClass = xutil.dom.addClass;
    var removeClass = xutil.dom.removeClass;
    var q = xutil.dom.q;
    var domChildren = xutil.dom.children;
    var domGetParent = xutil.dom.getParent;
    var getPreviousSibling = xutil.dom.getPreviousSibling;
    var getNextSibling = xutil.dom.getNextSibling;
    var inheritsObject = xutil.object.inheritsObject;
    var merge = xutil.object.merge;
    var clone = xutil.object.clone;
    var formatNumber = xutil.number.formatNumber;
    var isArray = xutil.lang.isArray;
    var attachEvent = xutil.dom.attachEvent;
    var detachEvent = xutil.dom.detachEvent;
    var XOBJECT = xui.XObject;

    // 图空间样式设置
    var styleConfiguration = {
        textStyle: {
            fontFamily: '微软雅黑',
            fontSize: '12',
            color: '#636363'
        },
        // 图例
        legendStyle: {
            fontFamily: '微软雅黑',
            fontSize: '12',
            color: '#000'
        },
        // tips
        tooltips: {
            fontFamily: '微软雅黑',
            fontSize: '12',
            color: '#fff'
        },
        // 地图值域
        dataRangeStyle : {
            fontFamily: '微软雅黑',
            fontSize: '12',
            color: '#000'
        },
        // 地图值域色阶
        dataRangeColor : [
            '#D87256',
            '#E5977C',
            '#E5B483',
            '#D6CE90',
            '#A5C491',
            '#9FCCC5',
            '#B4E0EA',
            '#D8F0F6',
            '#EFF9FD',
            '#F7FBFF',
            '#E5E5E5'
        ],
        // 轴线
        lineStyle: {
            color: '#00AEF3',
            type: 'solid',
            width: 2
        },
        // 网格线
        splitLine : {
            show:true,
            lineStyle: {
                color: '#EEEEEE',
                type: 'solid',
                width: 1
            }
        },
        // 网格区域
        splitArea : {
            show: true,
            areaStyle:{
                color:['rgba(255, 255, 255, 0)','rgba(237, 237, 237, 0.3)']
            }
        }
    };

    /**
     * 基于e-chart的JS图
     *
     * @class
     * @extends {xui.ui.Control}
     */
    var UI_E_CHART = xui.ui.EChart =
        inheritsObject(
            XOBJECT,
            function (options) {
                var el = this.el = options.el;
                this._sType = 'xui-e-chart';
                addClass(el, this._sType);
                var type = this._sType;
                // TODO:优化，header估计得干掉
                el.innerHTML = [
                    '<div class="' + type + '-header"></div>',
                    '<div class="' + type + '-content"></div>'
                ].join('');
                this._eHeader = el.childNodes[0];
                this._eContent = el.childNodes[1];
            }
        );
    var UI_E_CHART_CLASS = UI_E_CHART.prototype;

    /**
     * 初始化
     */
    UI_E_CHART_CLASS.init = function () {};

    /**
     * 设置数据
     *
     * @public
     * @param {Object} dataWrap 数据
     * @param {boolean=} isSilent 是否静默（不渲染），缺省则为false
     */
    UI_E_CHART_CLASS.setData = function (dataWrap, isSilent) {
        this._zoomSelectedButton = 0;
        dataWrap = dataWrap || {};
        this._bSeriesHasValue = null;
        this._nWidth = dataWrap.width;
        this._nHeight = dataWrap.height;
        /**
         * x轴定义
         * 例如：
         *  xAxis: [
         *      {
         *          type: 'quarter', // 或'category', 'date', 'month'等，参见EXT_AXIS_FORMAT
         *          data: ['2012-Q1', '2012-Q2']
         *      }
         *  ];
         */
        this._aXAxis = dataWrap.xAxis || [];
        this._zoomStart = 0;
        this._zoomEnd = this._aXAxis.data
            ? this._aXAxis.data.length - 1
            : 0;
        /**
         * y轴定义
         * 例如：
         *  xAxis: [
         *      {
         *          format: 'I,III.DD%', // 显示格式
         *          title: '我是y轴上的描述文字'
         *      }
         *  ];
         */
        this._aYAxis = dataWrap.yAxis || [];
        /**
         * 系列数据
         * 例如：
         *  series: [
         *      {
         *          name: '我是系列1',
         *          data: [1234.1234, 12344.333, 57655]
         *      },
         *      {
         *          name: '我是系列2',
         *          data: [566.1234, 565, 9987]
         *      }
         *  ];
         */
        // 晓强测试
        /*
        dataWrap.series[0].data[0]=10;
        dataWrap.series[0].data[1]=20;
        dataWrap.series[0].data[2]=30;
        dataWrap.series[0].data[3]=40;
        dataWrap.series[0].data[4]=30;
        dataWrap.series[0].data[5]=20;
        dataWrap.series[0].data[6]=10;
        dataWrap.series[0].barMaxWidth=10;
        */
        this._aSeries = dataWrap.series || []; //barMaxWidth  yAxisName
        /**
         * 用户自定义rangeselector的按钮
         * 例如：
         *  rangeSelector: {
         *      byAxisType: {
         *          date: {
         *              buttons: [
         *                  { type: 'thisMonth', text: '本月', by: 'max' },
         *                  { type: 'all', text: '全部' }
         *              ],
         *              selected: 0
         *          }
         *      }
         *  }
         */
        this._oRangeSelector = dataWrap.rangeSelector;
        /**
         * 用户自定义legend的模式（外观+行为）
         * 例如：
         *  legend: {
         *      xMode: 'pl' // PL模式的legend。缺省则使用默认模式。
         *  }
         */
        this._oLegend = dataWrap.legend || {};
        /**
         * 数据为空时的html
         */
        this._sEmptyHTML = dataWrap.emptyHTML || '数据为空';

        this._allMeasures = dataWrap.allMeasures;
        this._defaultMeasures = dataWrap.defaultMeasures;
        this._allDims = dataWrap.allDims;
        this._defaultDims = dataWrap.defaultDims;
        this._mapMinValue = dataWrap.mapMinValue;
        this._mapMaxValue = dataWrap.mapMaxValue;
        this._dimMap = dataWrap.dimMap;

        // 个性化设置
        dataWrap.appearance && (this._appearance = dataWrap.appearance);
        dataWrap.render && (this._render = dataWrap.render);

        !isSilent && this.render();
    };

    //------------------------------------------
    // 图形备选区域模块
    //------------------------------------------

    /**
     * 判断图表是否显示图例，并设置默认选项
     *
     * @protected
     */
    UI_E_CHART_CLASS.$getDefaultMeasures = function (chartType){
        return (this._defaultMeasures
                && this._defaultMeasures.length > 0
                && chartType != 'line'
            ) ? [this._defaultMeasures[0]] : this._defaultMeasures;
    };

    /**
     * 生成指标切换按钮
     *
     * @protected
     */
    UI_E_CHART_CLASS.$renderIndArea = function () {
        var me = this,
            allMeasures = me._allMeasures,
            defaultMeasures = me.$getDefaultMeasures(me._chartType),
            measureHtml = [];

        // 渲染图形中备选区模块
        // 单选
        var radioName = 'echarts-candidate-radio-' + new Date().getTime();
        for (var i = 0,  iLen = allMeasures.length; i < iLen; i ++) {
            var checkAbr = isInArray(allMeasures[i], defaultMeasures) ? 'checked="checked"' : '';
            var radioId = [
                'allMeasures-radio',
                new Date().getTime(),
                i
            ].join('');
            var opt = {
                rName: radioName,
                rId: radioId,
                checked: checkAbr,
                text: allMeasures[i]
            };
            var tpl = ''
                + '<input type="radio" name="#{rName}" id="#{rId}" #{checked} />'
                + '<label for="#{rId}">#{text}</label>';

            measureHtml.push(stringTemplate(tpl, opt));
        }
        me._eHeader.innerHTML = stringTemplate(
            '<div class="echarts-candidate" id="echarts-candidate">#{html}</div>',
            {
                html: measureHtml.join('')
            }
        );
        me._eCandidateBox = domChildren(me._eHeader)[0];
        // 重新设置单选按钮位置区域居中
        var candWidth = me._eCandidateBox.offsetWidth;
        var headWidth = me._eHeader.offsetWidth;
        me._eCandidateBox.style.marginLeft = (headWidth - candWidth) / 2 + 'px';
        var inputRadios = me._eCandidateBox.getElementsByTagName('input');

        for (var i = 0, iLen = inputRadios.length; i < iLen; i ++) {
            inputRadios[i].onclick = (function (j) {
                return function () {
                    me.notify('changeRadioButton', String(j));
                };
            })(i);
        }

            // if (this._chartType === 'line') {
            //     // 多选
            //     // 由于商桥的需求，折线图没有checkbox显示
            //     for (var i = 0, iLen = allMeasures.length; i < iLen; i ++) {
            //         measureHtml.push(
            //             '<input type="checkbox" name="echarts-candidate" ',
            //             isInArray(allMeasures[i], defaultMeasures) ? 'checked="checked" ' : '',
            //             '/>',
            //             '<label>',allMeasures[i],'</label>'
            //         );
            //     }
            //     this._eHeader.innerHTML = '<div class="echarts-candidate" id="echarts-candidate">'
            //         + measureHtml.join('')
            //         + '</div>';
            //     // 绑定备选区按钮事件
            //     this._eCandidateBox = domChildren(this._eHeader)[0];
            //     attachEvent(this._eCandidateBox, 'click', function (ev) {
            //         var oEv = ev || window.event;
            //         var target = oEv.target || oEv.srcElement;
            //         candidateClick.call(me, target);
            //     });
            //
            // }
    };


    // 备选区按钮点击事件
    function candidateClick(oTarget) {
        var resultName = '';

        if (oTarget.tagName.toLowerCase() === 'input') {
            resultName = getNextSibling(oTarget).innerHTML;
            if (oTarget.type === 'radio') {
                this._defaultMeasures = [resultName];
            }
            else {
                // 如果是多选，那么限制不能少于一个
                var chkBoxs = domChildren(domGetParent(oTarget));
                for (var sum = 0, i = 0, iLen = chkBoxs.length; i < iLen; i ++) {
                    if (chkBoxs[i].type === 'checkbox' && chkBoxs[i].checked) {
                        sum ++ ;
                    }
                }
                if (sum === 0) {
                    oTarget.checked = true;
                }
                else {
                    this._defaultMeasures = getCurrentCandidate(resultName, this._defaultMeasures);
                }
            }
            this.$disposeChart();
            this.$createChart(this.$initOptions());
        }
    }
    // 在数组中是否存在
    function isInArray(item, array) {
        var flag = false;
        for (var i = 0; i < array.length; i ++) {
            if (item === array[i]) {
                flag = true;
            }
        }
        return flag;
    }
    // 获取备选区中当前显示的内容
    function getCurrentCandidate(name, currentSelects) {
        var isHave = false;
        var result = [];

        for (var i = 0; i < currentSelects.length; i ++) {
            if (currentSelects[i] === name) {
                isHave = true;
            }
            else {
                result.push(currentSelects[i]);
            }
        }
        // 如果本身就没有name元素，就添加进去
        if (!isHave) {
            result.push(name);
        }
        return result;
    }

    /**
     * 设置数据
     *
     * @protected
     */
    UI_E_CHART_CLASS.$setupSeries = function (options) {
        var series = [];
        var seryKind = {};
        var tempData = [];
        var xAxis = this._aXAxis;
        var colors =  [
            '#A5D6D2', '#C1232B', '#B5C334', '#4cc6f7','#FCCE10',
            '#E87C25', '#27727B', '#FAD860', '#F3A43B','#60C0DD'
        ];
        var defChinaMapArr = [
            {name: '北京',value: 0},
            {name: '天津',value: 0},
            {name: '上海',value: 0},
            {name: '重庆',value: 0},
            {name: '河北',value: 0},
            {name: '河南',value: 0},
            {name: '云南',value: 0},
            {name: '辽宁',value: 0},
            {name: '黑龙江',value: 0},
            {name: '湖南',value: 0},
            {name: '安徽',value: 0},
            {name: '山东',value: 0},
            {name: '新疆',value: 0},
            {name: '江苏',value: 0},
            {name: '浙江',value: 0},
            {name: '江西',value: 0},
            {name: '湖北',value: 0},
            {name: '广西',value: 0},
            {name: '甘肃',value: 0},
            {name: '山西',value: 0},
            {name: '内蒙古',value: 0},
            {name: '陕西',value: 0},
            {name: '吉林',value: 0},
            {name: '福建',value: 0},
            {name: '贵州',value: 0},
            {name: '广东',value: 0},
            {name: '青海',value: 0},
            {name: '西藏',value: 0},
            {name: '四川',value: 0},
            {name: '宁夏',value: 0},
            {name: '海南',value: 0},
            {name: '台湾',value: 0},
            {name: '香港',value: 0},
            {name: '澳门',value: 0}
        ];
        var defChinaMapKey = {};
        for (var i = 0, ser, serDef; serDef = this._aSeries[i]; i ++) {
            seryKind[serDef.type] = seryKind[serDef.type]
                ? seryKind[serDef.type] + 1
                : 1;
            ser = { data: [] };
            ser.name = serDef.name || '';
            ser.colorDefine = serDef.colorDefine || void 0;
            ser.format = serDef.format || void 0;
            ser.type = (serDef.type === 'column' ? 'bar' : serDef.type);
            if (ser.type !== 'map' && ser.type !== 'pie') {
                ser.itemStyle = {
                    normal: {
                        color: colors[i]
                    }
                };
                if (ser.colorDefine) {
                    ser.itemStyle.normal.color = ser.colorDefine;
                }
            }
            (serDef.id !== null) && (ser.id = serDef.id);
            // TODO:这个data需要后端注意一下数据格式
            ser.data = serDef.data;
            var defaultMeasures = this.$getDefaultMeasures(ser.type);
            if (defaultMeasures) {
                if (ser.type === 'bar') {
                    if (isInArray(ser.name, defaultMeasures)) {
                        ser.yAxisIndex = 0;
                        series.push(ser);
                    }
                }
                else if (ser.type === 'column') {
                    if (isInArray(ser.name, defaultMeasures)) {
                        ser.type = 'bar';
                        series.push(ser);
                    }
                }
                else if (ser.type === 'pie') {
                	// 将饼图的开始绘图位置改为3点钟方向，以避免指标太多，显示拥挤的问题 update by majun
                	ser.startAngle = 360;
                    if (isInArray(ser.name, defaultMeasures)) {
                        series.push(ser);
                    }
                    // 设置饼图的指标描述为微软雅黑
                    ser.itemStyle = {
                        normal : {
                            label : {
                                textStyle : {
                                    fontFamily : '微软雅黑',
                                    fontSize : 14
                                }
                            }
                        }
                    }
                }
                else if (ser.type === 'line') {
                    // 在有两个以上的指标时进行双轴设定 - 博学
                    if (this._aSeries.length > 1) {
                        ser.yAxisIndex = serDef.yAxisIndex || 0;
                    }
                    ser.symbol = 'none'; // 线图上的点的形状
                    if (isInArray(ser.name, defaultMeasures)) {
                        tempData.push(ser);
                    }
                }
                else if (ser.type === 'map') {
                    ser.mapType = 'china';
                    ser.roam = false;
                    ser.itemStyle = {
                        normal:{ label:{ show:true } },
                        emphasis:{ label:{ show:true } }
                    };
                    if (isInArray(ser.name, defaultMeasures)) {
                        var serData = [];
                        for (var n = 0; n < defChinaMapArr.length; n ++ ) {
                            var tData = defChinaMapArr[n];
                            for (var x = 0; x < ser.data.length; x ++ ) {
                                if (tData.name === xAxis.data[x]) {
                                    if (ser.format && ser.format.indexOf('%') >= 0) {
                                        tData.value = ser.data[x] * 100;
                                    }
                                    else {
                                        tData.value = ser.data[x];
                                    }
                                    break;
                                }
                            }

                            serData.push(tData);
                        }
                        if (isInArray(ser.name, defaultMeasures)) {
                            ser.data = serData;
                            series.push(ser);
                        }
                    }
                }
            }
            else {
                if (ser.type === 'bar') {
                    ser.yAxisIndex = 0;
                    series.push(ser);
                }
                else if (ser.type === 'column') {
                    ser.type = 'bar';
                    series.push(ser);
                }
                else if (ser.type === 'pie') {
                	// 将饼图的开始绘图位置改为3点钟方向，以避免指标太多，显示拥挤的问题 update by majun
                	ser.startAngle=360;
                    series.push(ser);
                }
                else if (ser.type === 'line') {
                    ser.yAxisIndex = 0;
                    tempData.push(ser);
                }
                else if (ser.type === 'map') {
                    ser.mapType = 'china';
                    ser.roam = false;
                    ser.itemStyle = {
                        normal:{ label:{ show:true } },
                        emphasis:{ label:{ show:true } }
                    };
                    var serData = [];
                    for (var n = 0; n < defChinaMapArr.length; n ++ ) {
                        var tData = defChinaMapArr[n];
                        for (var x = 0; x < ser.data.length; x ++ ) {
                            if (tData.name === xAxis.data[x]) {
                                if (ser.format && ser.format.indexOf('%') >= 0) {
                                    tData.value = ser.data[x] * 100;
                                }
                                else {
                                    tData.value = ser.data[x];
                                }
                                break;
                            }
                        }
                        serData.push(tData);
                    }
                    ser.data = serData;
                    series.push(ser);
                }
            }

        }
        series = series.concat(tempData);
        if (seryKind.line >= 1 && seryKind.bar >= 1) {
            this._isAddYxis = true;
        }
        // series中只允许有一个饼图。
        if (this._chartType === 'pie') {
            var targetSeries = [{}];
            for(var key in series[0]) {
                series[0].hasOwnProperty(key) && (targetSeries[0][key] = series[0][key]);
            }
            targetSeries[0].data = [];
            for (var k = 0; k < series[0].data.length; k ++) {
                var  kser = series[0].data[k];
                var tarData = {
                    value: kser,
                    name: xAxis.data[k]
                };
                targetSeries[0].data.push(tarData);
            }
            series = targetSeries;
        }
        if (this._chartType === 'bar') {
            for (var i = 0, iLen = series.length; i < iLen; i ++) {
                series[i].data = series[i].data.reverse();
            }
        }
        options.series = series;
    };
    /**
     * 设置x轴
     *
     * @private
     */
    UI_E_CHART_CLASS.$setupXAxis = function (options) {
        var xAxis =  {
            type: 'category',
            boundaryGap: true,
            axisLine: {
                onZero: false
            },
            data: this._aXAxis.data,
            // 设置x轴字体样式
            axisLabel: {
                textStyle: styleConfiguration.textStyle
            },
            // x方向网格线 - 博学
            splitLine : styleConfiguration.splitLine
        };
        // 设置x轴颜色
        xAxis.axisLine.lineStyle = styleConfiguration.lineStyle;
        if (this._aXAxis.type === 'date') {
            xAxis.showDataType = 'date';
        }
        // 如果是柱状图Y轴放右边（条形图X轴和Y周和其他的相反） - 晓强
        if (this._chartType === 'bar') {
            xAxis.position = 'right';
            // 当图形为条形图时，暂时将动画关掉，以避免条形从左向右铺开的动画效果 update by majun
            options.animation = false;
            options.grid.x = 60;
            options.grid.x2 = 130;

            // Y轴调到右边需要数据翻转 晓强
            if (options.series && options.series.length > 0) {
                var series = options.series;
                for (var i = 0, len = series.length; i < len; i++) {
                    var sData = series[i].data;

                    for (var j = 0, jLen = sData.length; j < jLen; j++) {
                        if (sData[j] > 0) {
                            sData[j] =  -1 * sData[j];
                        }
                    }
                }
            }
        }

        // 如果是正常图形（柱形图与线图），那么x轴在下面显示
        if (this._chartType === 'column' || this._chartType === 'line') {
            options.xAxis = xAxis;
        }
        else if (this._chartType === 'pie') {

        }
        else {
            xAxis.data = xAxis.data.reverse();
            options.yAxis = xAxis;
        }
        return options;
    };

    /**
     * 设置y轴，支持多轴
     *
     * @param {Object} options eCharts接口配置项
     * @param {Object} options.grid 直角坐标系内绘图网格
     * @param {Object} options.dataZoom 数据区域缩放
     * @param {Object} options.series 数据内容
     *
     * @private
     */
    UI_E_CHART_CLASS.$setupYAxis = function (options) {
        var yAxis = [];
        var axisCaption;
        var settings = {
            chartType: this._chartType,
            splitArea: styleConfiguration.splitArea,
            splitLine: styleConfiguration.splitLine,
            textStyle: styleConfiguration.çƒ,
            lineStyle: styleConfiguration.lineStyle
        };

        this._render && (axisCaption = this._render.axisCaption);
        // 如果是饼图、地图，不需要设置刻度轴；如果没有series，返回；
        if (this._chartType === 'pie'
            || this._chartType === 'map'
            || this._aSeries.length < 1
        ) {
            return;
        }

        // 单刻度轴情况
        if (
            (this._chartType === 'column' || this._chartType === 'bar')
            && axisCaption
        ) {
            var name = this._aSeries[0].yAxisName;
            axisCaption && (settings.name = axisCaption[name]);
            settings.format = this._aSeries[0].format;
            yAxis.push(setBasicItems(settings));
        }

        // 双刻度轴情况
        if (this._chartType === 'line' && axisCaption) {
            var series = this._aSeries;
            var leftName = [];
            var rightName = [];
            var leftFormat;
            var rightFormat;
            var leftCount = 0;
            var rightCount = 0;

            for (var i = 0, iLen = series.length, tSer; i < iLen; i ++) {
                tSer = series[i];
                var name = tSer.yAxisName;
                if (tSer.yAxisIndex === '0') {
                    if (name && axisCaption[name]) {
                        leftName.push(axisCaption[name]);
                    }
                    leftFormat = tSer.format;
                    leftCount ++;
                }
                else {
                    rightName.push(axisCaption[name]);
                    rightFormat = tSer.format;
                    rightCount ++;
                }

            }
            if (leftName.length === 1) {
                leftName = leftName[0];
            }
            else if (leftName.length > 1) {
                leftName = leftName.join(',');
            }

            if (rightName.length === 1) {
                rightName = rightName[0];
            }
            else if (rightName.length > 1) {
                rightName = rightName.join(',');
            }
            if (leftCount) {
                // 左刻度轴设置
                var leftSettings = merge(clone(settings), {name: leftName, format: leftFormat});
                yAxis.push(setBasicItems(leftSettings));
            }

            if (rightCount) {
                // 右刻度值设置
                var rightSettings = merge(clone(settings), {name: rightName, format: rightFormat});
                yAxis.push(setBasicItems(rightSettings));
            }
        }

        // 数据为空时横轴显示修改 博学
        if (this._chartType === 'bar') {
            var xAxisSeries = [];
            var xAxisData = [];
            var sum = 0; // 为0的数据个数
            var nowxAxis = 0; // 判断数据书否全部为0
            var arrSeries = options.series;

            // 判断是否查出数据，没有数据情况判断方式是数据为0
            for (var i = 0; i < arrSeries.length; i ++) {

                xAxisData.push(arrSeries[i].data.length);
                for (var j = 0; j < arrSeries[i].data.length; j ++) {

                    if (arrSeries[i].data[j] == 0) {
                        sum += 1;
                    }
                }

                xAxisSeries.push(sum);
                sum = 0;
            }
            // 通过判断为0数据并对same进行累加为判断数据准备
            for (var i = 0; i < xAxisSeries.length; i ++) {
                if (xAxisSeries[i] === xAxisData[i]) {
                    nowxAxis += 1;
                }
            }
            if (
                xAxisSeries.length === nowxAxis
                && xAxisData.length === nowxAxis
            ) {
                yAxis[0].max = 0;
                yAxis[0].min = -10;
            }
            options.xAxis = yAxis;
        }
        else if (this._chartType === 'column') {
            options.yAxis = yAxis;
        }
        else if (this._chartType === 'line') {
            options.yAxis = yAxis;
        }

        /**
         * 设置yAxis高级项
         *
         * @param {Object} advOpt 设置项
         * @param {string} advOpt.chartType 图形种类
         *
         * @param {Object} advOpt.splitArea 分隔区域
         * @param {Object} advOpt.splitArea.show 是否显示
         * @param {Object} advOpt.splitArea.areaStyle 区域样式
         *
         * @param {Object} advOpt.splitLine 分隔线
         * @param {Object} advOpt.splitLine.show 是否显示
         * @param {Object} advOpt.splitLine.lineStyle 线条样式
         *
         * @private
         * @return {Object} item 坐标轴刻度配置项
         */
        function setBasicItems(advOpt) {
            var item = {};
            item.type = 'value';

            // 设置坐标轴名字
            advOpt.name && (item.name = advOpt.name);

            advOpt.name && (
                item.nameLocation = (advOpt.chartType === 'bar')
                    ? 'start' : 'end'
            );
            item.nameTextStyle = {
                fontFamily: '微软雅黑',
                fontSize: '12',
                color: '#000'
            };
            // 设置y轴网格
            item.splitArea = advOpt.splitArea;
            item.splitLine = advOpt.splitLine;

            // 设置图形刻度
            item.axisLabel = {};
            item.axisLabel.formatter = function (value) {
                var result;
                var w;
                var y;
                if (advOpt.format && advOpt.format.indexOf('%') >= 0) {
                    if (advOpt.chartType === 'bar') {
                        value = - value;
                    }
                    result = formatNumber(
                        value,
                        advOpt.format,
                        null,
                        null,
                        true
                    );
                }
                else {
                    // 确定可以转换成数字
                    if (!Number.isNaN(value / 1)) {
                        w = 10000; // 万
                        y = 100000000; // 亿

                        // Y轴调到右边需要数据翻转
                        if (advOpt.chartType === 'bar') {
                            value *= -1;
                        }
                        result = value;

                        if (value >= w && value <= y) {
                            result = (value / w).toFixed(0) + '万';
                        }
                        else if (value > y) {
                            result = (value / y).toFixed(0) + '亿';
                        }
                    }
                }

                return result;
            };
            // 字体修改
            item.axisLabel.textStyle = advOpt.textStyle;

            // y轴颜色设定
            item.axisLine = {};
            item.axisLine.lineStyle = advOpt.lineStyle;
            return item;

        }
    };

    /**
     * 设置图例
     *
     * @param {Object} options eCharts接口配置项
     * @param {Object} options.grid 直角坐标系内绘图网格
     * @param {Object} options.dataZoom 数据区域缩放
     * @param {Object} options.series 数据内容
     *
     * @private
     */
    UI_E_CHART_CLASS.$setupLegend = function (options) {

        // 控制图例位置 需要同事修改下面两处 - 晓强
        // 控制图例位置 UI_E_CHART_CLASS.$setupLegend
        // 控制grid的位置 UI_E_CHART_CLASS.$initOptions

        // var legend = {x: 'center', y: '20'};
        var legend = {x: 'center', y: 'top'};
        var data = [];
        var defaultMeasures = this.$getDefaultMeasures(this._chartType);

        if (this._chartType === 'pie') {
            for (var i = 0; i < this._aXAxis.data.length; i++) {
                data[i] = this._aXAxis.data[i];
            }
        }
        else {
            if (this._aSeries && this._aSeries.length > 0) {
                for (var i = 0; i < this._aSeries.length; i++) {
                    if (defaultMeasures) {
                        if (isInArray(this._aSeries[i].name, defaultMeasures)) {
                            data.push(this._aSeries[i].name);
                        }
                    }
                    else {
                        data.push(this._aSeries[i].name);
                    }
                }
            }
        }
        legend.data = data;
        legend.textStyle = styleConfiguration.legendStyle;
        options.legend = legend;
    };

    /**
     * 设置工具箱
     *
     * @protected
     */
    UI_E_CHART_CLASS.$setupToolBox = function (options) {
        var series;
        var itemChartType = {};
        var chartTypeLen = 0;
        // 如果是柱状图或者条形图，series中数据大与一个，且每一个的图形一致；才显示图形种类
        if (this._chartType === 'bar' || this._chartType === 'column') {
            series = this._aSeries;
            for (var i = 0; i < series.length; i++ ) {
                itemChartType[series[i].type] = 1;
            }
            for (var key in itemChartType) {
                if (itemChartType.hasOwnProperty(key)) {
                    chartTypeLen ++;
                }
            }
            if (series.length === 1 || chartTypeLen >= 2) {
                return;
            }
        }
    };
    /**
     * 设置dataRoom
     *
     * @private
     */
    UI_E_CHART_CLASS.$setupDataRoom = function (options) {
        // 此方法内只接受data中的start与end
        var dataZoom = {};
        var categories = {};

        if (this._aXAxis) {
            categories = this._aXAxis;
        }

        if (
            this._chartType === 'column'
            || this._chartType === 'bar'
            || this._chartType === 'line'
        ) {
            dataZoom.show = false;
            var xNums = categories.data ? categories.data.length : 0;
            var enableSelectRange = false;

            enableSelectRange = (xNums > 10 && this._aXAxis.type !== 'category')
                ? true
                : enableSelectRange;
            dataZoom.show = enableSelectRange;

            dataZoom.realtime = true;
            if (this._zoomStart === 0) {
                dataZoom.start = this._zoomStart;
            }
            else {
                dataZoom.start = Math.round(101 / xNums * this._zoomStart);
            }

            if (this._zoomEnd === (xNums - 1 )) {
                dataZoom.end = 100;
            }
            else {
                dataZoom.end = Math.round(101 / xNums * this._zoomEnd);
            }

            // 动态设置dataRoom的垂直定位 - 晓强
            // dataZoom.y = $(this.el).height() - 50;
            this.el.offsetHeight - 50;
            options.dataZoom = dataZoom;
        }
    };

    //------------------------------------------
    // 设置图形tooltip区域
    //------------------------------------------
    /**
     * 设置提示浮层
     *
     * @protected
     */
    UI_E_CHART_CLASS.$setupTooltip = function (options) {
        var me = this;
        var toolTip = {};
        if (this._chartType === 'pie') {
            toolTip.formatter = '{a} <br/>{b} : {c} ({d}%)';
            toolTip.trigger = 'item';
            // 设置提示字体
            toolTip.textStyle =styleConfiguration.tooltips;
        }
        else if (this._chartType === 'map') {
            toolTip.trigger = 'item';
            toolTip.formatter = function (data) {
                return mapToolTipFunc(data, options.series);
            };
            // 设置提示字体
            toolTip.textStyle = styleConfiguration.tooltips;
        }
        else if (this._chartType === 'bar') {
            toolTip.trigger = 'axis';
            // 设置鼠标hover之后，隐藏掉图形的坐标轴显示线 update by majun
            var axisPointer = {
                type : 'none'
            };
            toolTip.axisPointer = axisPointer;
            toolTip.formatter =  function(data, ticket, callback) {
                var res = data[0][1];
                for (var i = 0, l = data.length; i < l; i++) {
                    var valueFormat = options.series[i].format;
                    var valueLable = data[i][2];
                    // Y轴调到右边需要数据翻转 晓强
                    if (me._chartType === 'bar') {
                        valueLable = -1 * valueLable;
                    }
                    // 当发现图数据有配置format属性时，按format所示进行展示
                    // 当没有format的时候，展示原值
                    if (valueFormat) {
                        valueLable = formatNumber(
                                valueLable,
                                valueFormat,
                                null,
                                null,
                                true
                        );
                    }

                    res += '<br/>' + data[i][0] + ' : ' + valueLable;
                }
                return res;
            };
            // 设置提示字体 - 博学
            toolTip.textStyle = styleConfiguration.tooltips;
        }
        else {
            toolTip.trigger = 'axis';
            // 在此将提示信息的format属性加上以便方便显示
            toolTip.formatter =  function(data, ticket, callback) {
                var res = data[0][1];
                // 如果为date类型则设置显示周
                if (options.xAxis.showDataType === 'date'){
                    var weekStr = ['周日','周一','周二','周三','周四','周五','周六'][new Date(data[0][1]).getDay()];
                    res = res + ' （' + weekStr + '）';
                }
                for (var i = 0, l = data.length; i < l; i++) {

                    var valueFormat = options.series[i].format;
                    var valueLable = data[i][2];

                    // 当发现图数据有配置format属性时，按format所示进行展示
                    // 当没有format的时候，展示原值
                    if (valueFormat) {
                        valueLable = formatNumber(
                                data[i][2],
                                valueFormat,
                                null,
                                null,
                                true
                        );
                    }
                    // Y轴调到右边需要数据翻转 晓强
                    if (me._chartType === 'bar') {
                        valueLable = -1 * valueLable;
                    }
                    res += '<br/>' + data[i][0] + ' : ' + valueLable;
                }
                return res;
            };
            toolTip.textStyle = styleConfiguration.tooltips;
        }
        options.tooltip = toolTip;
    };
    /**
     * 地图tooltip
     *
     * @private
     */
    function mapToolTipFunc(data, series) {
        var names,
            areaValue,
            areaName = data[1],
            str = areaName;

        data[0] && (names = data[0].split(' '));
        if (isArray(names)) {
            for (var i = 0, iLen = names.length; i < iLen; i ++) {
                for (var j = 0, jLen = series.length; j < jLen; j++) {
                    if (series[j].name === names[i]) {
                        areaValue = getAreaValue(areaName, series[j].data, series[i].format);
                        str += '<br/>' + series[j].name + ':' + areaValue;
                    }
                }
            }
        }
        else {
            str += ': -';
        }
        return str;
    }
    /**
     * 根据地图地区名获取值
     *
     * @private
     */
    function getAreaValue(areaName, dataArray, format) {
        var result= '';

        for (var x = 0, xLen = dataArray.length; x < xLen; x++) {

            if (dataArray[x].name === areaName) {
                result = dataArray[x].value;
                if (format && format.indexOf('%') >= 0) {
                    result = result / 100;
                }
                if (format) {
                    result = formatNumber(
                        result,
                        format,
                        null,
                        null,
                        true
                    );
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 重新渲染图表
     *
     * @public
     */
    UI_E_CHART_CLASS.render = function () {
        var tpl,
            noDataContainer,
            me = this;
        me.$disposeChart();
        for (var i = 0, ser; ser = this._aSeries[i]; i ++) {
            this._chartType = ser.type;
        }

        // 如果显示指标区域 并且 有指标,则加载指标区域
        // 做兼容老保表
        // 如果个性化设置不存在;或者，个性化设置存在，显示图例不存在；此时，就是老报表状态；除了线图，都添加指标区域；
        if (
            !this._appearance
            || (
                this._appearance
                && (
                    this._appearance.isShowInds === null
                    || this._appearance.isShowInds === undefined
                )
            )
        ) {
            if (this._chartType !== 'line') {
                this.$renderIndArea();
            }
        }

        // 正常逻辑
        if (
            this._appearance
            && this._appearance.isShowInds
            && this._allMeasures
            && this._allMeasures.length > 0
        ) {
            this.$renderIndArea();
        }

        // 如果没有数据，图形显示 暂无数据
        if (
            !me._aSeries
            || (me._aSeries && me._aSeries.length === 0)
            || (me._chartType === 'pie'
                && me._aSeries
                && me._aSeries.length > 0
                && me._aSeries[0].data.length === 0
            )
        ) {
            me._eContent.style.height = (me.el.offsetHeight - me._eHeader.offsetHeight) + 'px';
            tpl = '<div class="#{dClass}-empty"><div class="#{dClass}-empty-img"></div></div>';
            me._eContent.innerHTML = stringTemplate(
                tpl,
                { dClass: me._sType }
            );
            noDataContainer = q(me._sType + '-empty-img', me._eContent)[0];
            noDataContainer.style.marginTop = (
                (me._eContent.offsetHeight - noDataContainer.offsetHeight) / 2
            ) + 'px';
            return;
        }
        me.$createChart(me.$initOptions());
    };

    /**
     * 创建图表
     *
     * @public
     */
    UI_E_CHART_CLASS.$createChart = function (options) {
        var me = this;
        // 重设图形区域（头部和内容）
        me._eContent.style.height = (me.el.offsetHeight - me._eHeader.offsetHeight) + 'px';
        me._oChart = echarts.init(this._eContent);
        me._oChart.setOption(options);
        me._oChart.on(echarts.config.EVENT.CLICK, chartClick);
        function chartClick(args) {
            var o = {
                name: args.name,
                dimMap: me._dimMap
            };
            me.notify('chartClick', o);
        }
    };

    /**
     * 构建图表参数
     *
     * @private
     */
    UI_E_CHART_CLASS.$initOptions = function () {
        var options = {};
        // 显示标题
        if (this._appearance && this._appearance.isShowTitle) {
            options.title = {
                text: this._appearance.chartTitle,
                x: 'center',
                y: 'top',
                textStyle: {
                    fontSize: 12,
                    fontWeight: 'normal',
                    fontFamily: '微软雅黑'
                }
            };
        }

        this.$setupSeries(options);
        this.$setupTooltip(options);

        if (
            this._chartType === 'column'
            || this._chartType === 'bar'
            || this._chartType === 'line'
            || this._chartType === 'pie'
        ) {
            if (this._chartType !== 'pie') {
                // 控制图例位置 需要同事修改下面两处 - 晓强
                // 控制图例位置 UI_E_CHART_CLASS.$setupLegend
                // 控制grid的位置 UI_E_CHART_CLASS.$initOptions
                options.grid = {
                    x: 80,
                    x2: 80,
                    y: 20,
                    borderWidth: 0
                };
                if (this._appearance && this._appearance.isShowTitle) {
                    options.grid.y = 30;
                }
                // 当不为饼图时，都需要设置x轴属性，否则图形都显示不出来 updata by majun
                this.$setupXAxis(options);
            }
            this.$setupDataRoom(options);
            // 可视数据区DataRoom影响距y2的值 - 晓强
            if (options.grid && options.dataZoom) {
                options.grid.y2 = options.dataZoom.show ? 90 : 33;
            }

            this.$setupToolBox(options);
            this.$setupYAxis(options);
        }
        else if (this._chartType === 'map') {
            var splitNum = 10;
            //  TODO:要考虑到负数
            // [
            //     {start: 1000},
            //     {start: 900, end: 1000},
            //     {start: 800, end: 900},
            //     {start: 700, end: 800},
            //     {start: 10, end: 200, label: '10 到 200（自定义label）'},
            //     {start: 5, end: 5, label: '5（自定义特殊颜色）', color: 'black'},
            //     {end: this._mapMinValue}
            // ]
            var format;
            var hasPercent;
            if (options && options.series && options.series[0]) {
                format = options.series[0].format;
            }
            if (format && format.indexOf('%') >= 0) {
                hasPercent = true;
            }

            if (hasPercent) {
                this._mapMaxValue = this._mapMaxValue > 1 ? 1 : this._mapMaxValue;
            }

            var min = hasPercent ? this._mapMinValue * 100: this._mapMinValue;
            var max = hasPercent ? this._mapMaxValue * 100: this._mapMaxValue;

            var split = (max - min) / splitNum;
            var splitList = [{ start: max }];
            var i = 1;
            var tStart = 0;
            var tEnd = 0;
            while (i <= (splitNum - 2)) {
                if (hasPercent) {
                    tStart = (max - split * i);
                    tEnd = (max - split * (i - 1));
                }
                else {
                    tStart = max - split * i;
                    tEnd = max - split * (i - 1);
                }
                var item = {
                    start: tStart,
                    end: tEnd
                };
                splitList.push(item);
                i++;
            }
            splitList.push({
                start: tStart,
                end: min
            });
            splitList.push({
                end: 0
            });

            if (max) {
                options.dataRange = {
                    x: 'left',
                    y: 'bottom',
                    text: ['高', '低'],           // 文本，默认为数值文本
                    itemGap: 0,
                    // 设置地图值域字体
                    textStyle: styleConfiguration.dataRangeStyle,
                    color: styleConfiguration.dataRangeColor,
                    splitList: splitList
                };
            }
        }

        if (this._chartType === 'pie') {
            // 拖拽重计算在线上项目应用不多，且有bug，先行关闭该高级功能 updata by majun
            options.calculable = false;
            var colors = [
                '#C0504E', '#4F81BC', '#9BBB58', '#FFB981', '#D97A81',
                '#D6A7C9', '#7E95D8', '#70CBA0', '#B7Cb8C', '#E6D88D'
            ];
            // 饼图每个块的颜色，按照UE给出的标准进行重设
            options.color = colors;
        }

        // 做兼容老保表
        // 如果个性化设置不存在;或者，个性化设置存在，显示图例不存在；此时，就是老报表状态；如果是线图，就添加图例，否则，就不添加；
        if (
            !this._appearance
            || (
                this._appearance
                && (
                    this._appearance.isShowLegend === null
                    || this._appearance.isShowLegend === undefined
                )
            )
        ) {
            if (this._chartType === 'line') {
                this.$setupLegend(options);
            }
        }

        // 正常逻辑，如果显示图例
        if (this._appearance && this._appearance.isShowLegend) {
            this.$setupLegend(options);
        }

        return options;
    };


    /**
     * 销毁图表
     *
     * @private
     */
    UI_E_CHART_CLASS.$disposeChart = function () {
        document.onkeydown = null;
        if (this._oChart) {
            this._oChart.clear();
            this._oChart.dispose();
            this._oChart = null;
        }
        this._eContent && (this._eContent.innerHTML = '');
    };

    /**
     * 销毁图表
     *
     * @private
     */
    UI_E_CHART_CLASS.$disposeHeader = function () {
        this._eHeader && (this._eHeader.innerHTML = '');
    };

    /**
     * @override
     */
    UI_E_CHART_CLASS.dispose = function () {
        this.$disposeChart();
        UI_E_CHART.superClass.dispose.call(this);
    };

})();
