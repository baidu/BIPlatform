(function () {
    var core = ecui,
        ui = core.ui,
        dom = core.dom,
        util = core.util,
        string = core.string,

        blank = core.util.blank,
        inheritsControl = core.inherits,
        triggerEvent = core.triggerEvent,
        children = dom.children,
        formatDate = string.formatDate,
        encodeHTML = string.encodeHTML,
        attachEvent = util.attachEvent,
        formatNumber = xutil.number.formatNumber,
        extend = util.extend,
        createDom = dom.create,

        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = UI_CONTROL.prototype;

    var UI_LITE_CHART = ui.LiteChart = 
        inheritsControl(
            UI_CONTROL,
            'ui-lite-chart',
            function (el, options) {
                options.resizable = false;
            }
        ),
        UI_LITE_CHART_CLASS = UI_LITE_CHART.prototype;

    UI_LITE_CHART_CLASS.$setSize = blank;

    UI_LITE_CHART_CLASS.CHART_OPTIONS = {
        colors: ['#50bfc6', '#e9693c', '#0ca961', '#f6ab1a', '#88d915', '#0380ea', '#3c2dc9', '#8e45e9', '#f44dce', '#e21d3d'],
        global: {useUTC: false}
    };

    UI_LITE_CHART_CLASS.STR_WEEKDAY = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
        
    UI_LITE_CHART_CLASS.PARSE_DATE_HANDLER = {
        'datetime': function (data) {
            if (typeof data == 'string') {
                if (data.indexOf('-') >= 0) {
                    data = data.split('-');
                    return new Date(parseInt(data[0], 10), parseInt(data[1], 10) - 1, parseInt(data[2], 10)).getTime();
                }
                else {
                    return parseInt(data, 10);
                }
            }
            return data;
        },
        'month': function (data) {
            data = data.split('-');
            return new Date(parseInt(data[0], 10), parseInt(data[1], 10) - 1, 1).getTime();
        },
        'quarter': function (data) {
            var par = [0, 0, 3, 6, 9];

            data = data.split('Q'); 
            return new Date(parseInt(data[0], 10), par[parseInt(data[1], 10)], 1).getTime();
        },
        'year': function (data) {
            return new Date(parseInt(data, 10), 0, 1).getTime();
        },
        'category': function (data) {
            return data;
        }
    };

    UI_LITE_CHART_CLASS.FORMAT_DATE_HANDLER = {
        'datetime': function (timestamp) {
            return formatDate(new Date(timestamp), 'yyyy-MM-dd');
        },
        'month': function (timestamp) {
            return formatDate(new Date(timestamp), 'yyyy-MM');
        },
        'quarter': function (timestamp) {
            var date = new Date(timestamp),
                quarter = ['Q1', 'Q2', 'Q3', 'Q4'];

            return date.getFullYear() + '' + quarter[Math.floor(date.getMonth() / 3)];
        },
        'year': function (timestamp) {
            return new Date(timestamp).getFullYear();
        },
        'category': function (o) {
            return o;
        }
    };

    UI_LITE_CHART_CLASS.init = function () {
        Highcharts.setOptions(this.CHART_OPTIONS);
        this.render();
    };

    /**
     * 设置数据
     * @public
     */
    UI_LITE_CHART_CLASS.setData = function (dataWrap) {
        this._sChartType = dataWrap.chartType || 'line';
        this._nWidth = dataWrap.width;
        this._nHeight = dataWrap.height;
        this._aSeries = dataWrap.series || [];
        this._oCategory = dataWrap.category || {};
        this._aAxis = dataWrap.axis || [];        
        this.setupDatasource(dataWrap.data || []);
        this.render();
    };

    /**
     * 处理图表数据
     * 由于hightchart.StockChart的x值只支持datetime(时间戳)类型 所以需要将所有非datetime转化成时间戳
     * @protected
     */
    UI_LITE_CHART_CLASS.setupDatasource = function (data) {
        var chartData = [], i, item, o,
            type = this._oCategory.type,
            field = this._oCategory.field,
            parser = this.PARSE_DATE_HANDLER[type];

        for (i = 0; item = data[i]; i++) {
            o = extend({}, item);
            parser && (o[field] = parser.call(this, o[field]));
            chartData.push(o);
        }
        this._oData = chartData;
    }

    /**
     * 设置数据
     * @protected
     */
    UI_LITE_CHART_CLASS.$setupSeries = function (options) {
        var series = [], s, i, item, o, x, y, yRaw, label, data = this._oData;

        for (i = 0; item = this._aSeries[i]; i++) {
            s = {data: []};

            s.name = item.label || '';
            s.yAxis = item.axis || 0;
            s.color = item.color || null;
            s.format = item.format || null;
            item.id !== undefined && item.id !== null && (s.id = item.id);

            for (var j = 0, o; o = data[j]; j++) {
                x = o[this._oCategory.field];
                yRaw = y = o[item.field];
                label = o[item.labelField];

                if (this._sChartType == 'bar') {
                    // 禁止负条但是显示负值
                    y < 0 && (y = 0);
                }
                s.data.push([x, y, yRaw, label]);
            }
            series.push(s);
        }
        options.series = series;
    }
    
    /**
     * 设置提示浮层
     * @protected
     */
    UI_LITE_CHART_CLASS.$setupTooptip = function (options) {
        var type = this._oCategory.type, // x轴类型
            callback = this._oCategory.tipCallback,
            aSeries = this._aSeries,
            formatter = this.FORMAT_DATE_HANDLER[this._oCategory.type]; // 回调函数用于自定义浮层内容 

        if (this._sChartType == 'bar') {
            options.tooltip = {
                useHTML: false,
                shared: true,
                borderColor: '#11A4F2',
                formatter: callback || function () {
                    var i, o, htmlArr = [];
                    htmlArr.push('<span style="color:#4770A7;font-size:13px;font-weight:bold;font-family:\"微软雅黑\",Arial">' + this.x + '</span><br>');
                    for (i = 0; o = this.points[i]; i ++) {
                        if (o.series.name != null) {
                            htmlArr.push('<span style="color:' + o.series.color + ';font-size:12px;font-weight:bold">' + o.series.name + ': </span>');
                        }
                        htmlArr.push('<span style="color:#000;font-size:12px;font-family:Arial">' + o.point.config[2] + '</span>');
                        if (o.point.config[3] != null) {
                            htmlArr.push(' <span style="color:#000;font-size:12px;font-family:Arial">( ' + o.point.config[3] + ' )</span>');
                        }
                        if (i < this.points.length - 1) {
                            htmlArr.push('<br>');
                        }
                    }
                    return htmlArr.join('');
                }
            }

        } else {
            options.tooltip = {
                useHTML: false,
                shared: true,
                borderColor: '#11A4F2',
                formatter: callback || function () {
                    var i, o, htmlArr = [], sFormatter;
                    htmlArr.push(
                        '<span style="color:#4770A7;line-height:20px;font-size:13px;font-weight:bold;font-family:\"微软雅黑\",Arial">',
                        formatter(this.x),
                        '</span><br>'
                    );
                    for (i = 0; o = this.points[i]; i ++) {
                        sFormat = aSeries[i].format;
                        if (o.series.name != null) {
                            htmlArr.push(
                                '<span style="color:' + o.series.color + ';font-size:12px;font-weight:bold">',
                                o.series.name + ': ',
                                '</span>',
                                '<span style="text-align:right;color:#000;font-size:12px;font-family:Arial">',
                                sFormat != null ? formatNumber(o.y, sFormat) : o.y,
                                '</span>'
                            );
                        }
                        if (i < this.points.length - 1) {
                            htmlArr.push('<br>');
                        }
                    }
                    return htmlArr.join('');
                }
            }
        }
    }

    /**
     * 设置x轴
     *
     * @private
     */
    UI_LITE_CHART_CLASS.$setupCategory = function (options) {
        var i, o, before, after,
            FORMAT_DATE_HANDLER = this.FORMAT_DATE_HANDLER,
            category = this._oCategory,
            tickPositioner = this.$getTickPositioner(),
            xAxis = {
                gridLineWidth: 1,
                gridLineColor: '#DBDBDB',
                tickPosition: 'inside',
                title: null,
                lineColor: '#4770A7',
                lineWidth: 2,
                tickPositioner: tickPositioner,                
                labels: {overflow: null}
            };
            
        if (category.categories) {
            xAxis.categories = category.categories;
        }

        if (category.tickPixelInterval != null) {
            xAxis.tickPixelInterval = category.tickPixelInterval;
        }

        if (this._sChartType == 'bar') {
            xAxis.gridLineWidth = 0;
            xAxis.tickLength = 0;
        }

        if (this._oCategory.title != null) {
            xAxis.title = {
                enabled: true,
                text: this._oCategory.title,
                align: 'high',
                rotation: 0,
                tickInterval: 1,
                offset: 0,
                y: -12,
                x: 10
            };
        }

        if (this._oCategory.plotLines != null) {
            xAxis.plotLines = [];
            for (i = 0; o = this._oCategory.plotLines[i]; i ++) {
                if (o.value != null) {
                    o.value = this.PARSE_DATE_HANDLER[this._oCategory.type](o.value);
                    xAxis.plotLines.push(o);
                } else if (o.valueBefore != null || o.valueAfter != null) {
                    before = o.valueBefore != null ? o.valueBefore : o.valueAfter;
                    after = o.valueAfter != null ? o.valueAfter : o.valueBefore;
                    o.value = Math.round(
                        (
                            this.PARSE_DATE_HANDLER[this._oCategory.type](before)
                            + this.PARSE_DATE_HANDLER[this._oCategory.type](after)
                        ) / 2
                    );
                    xAxis.plotLines.push(o);
                }
            }
        }

        xAxis.labels.formatter = function () {
            var fun = FORMAT_DATE_HANDLER[category.type];
            return fun ? fun.call(null, this.value) : this.value;
        };
        xAxis.labels.style = {fontFamily: 'Arial,Serif,Times', fontSize: '12px', color: '#6B6B6B'};

        if (category.color) {
            xAxis.lineColor = category.color;
        }

        xAxis.tickType = category.type;

        options.xAxis = xAxis;
    }

    /**
     * 设置y轴
     * 支持多轴
     *
     * @private
     */
    UI_LITE_CHART_CLASS.$setupAxis = function (options) {
        var i, item, o, yAxis, yas = []
            align = ['right', 'left'],
            opposite = [false, true],
            labelOffset = [-7, 5];

        for (i = 0; item = this._aAxis[i]; i++) {
            yAxis = {
                gridLineWidth: 1,
                gridLineColor: '#DBDBDB',                
                lineColor: '#4770A7',
                lineWidth: 2,                
                labels: {align: 'right', x: -20},
                title: null,
                tickPosition: 'inside',
                tickmarkPlacement: 'on'
            };

            if (this._sChartType == 'bar') {
                yAxis.tickPixelInterval = 210;        
                yAxis.min = 0; // 不允许负值
            }
        
            o = {};
            if (item.color) {
                o.lineColor = item.color;
            }
            o.labels = {align: align[i % 2]};
            if (this._sChartType == 'bar') {
                o.labels.x = 20;
            } else {
                o.labels.x = labelOffset[i % 2];
            }
            o.opposite = opposite[i % 2];
            if (item.title != null) {
                o.title = {
                    enabled: true,
                    text: item.title,
                    align: 'high',
                    rotation: 0                
                };
            }
            if (item.format) {
                if (typeof item.format == 'string') {
                    o.labels.formatter = function (format) { 
                        return function () {return formatNumber(this.value, format)}
                    }(item.format);
                }
                else {
                    o.labels.formatter = function (fuc) {
                        return function () {return fuc.call(null, this.value)}
                    }(item.format);
                }
            }
            o.labels.style = {fontFamily: 'Arial,Serif,Times', fontSize: '11px', color: '#6B6B6B'};
            yas.push(extend(yAxis, o));
        }
        options.yAxis = yas.length <= 0 ? {} : yas.length > 1 ? yas : yas[0];
    }

    /**
     * 设置点
     * @protected
     */
    UI_LITE_CHART_CLASS.$setupPlotOptions = function (options) {
        if (this._sChartType == 'bar') {
            options.plotOptions = {
                bar: {
                    minPointLength: 2,
                    borderWidth: 0,
                    dataLabels: {
                        enabled: true,
                        color: '#4770A7',
                        style: {fontWeight: 'bold', fontFamily: 'Arial', fontSize: '14px'},
                        formatter: function () {
                            if (this.point.config[3] != null) {
                                return this.point.config[3]; 
                            } else {
                                return '';
                            }
                        }
                    }
                }
            };
        }
    }

    /**
     * 设置图例
     * @protected
     */
    UI_LITE_CHART_CLASS.$setupLegend = function (options) {
        options.legend = {
            enabled: true,
            align: 'center',
            borderColor: '#FFF',
            verticalAlign: 'top',
            margin: 15
        };
    }

    /**
     * 设置区域选择
     * @protected
     */
    UI_LITE_CHART_CLASS.$setupZoom = function (options) {
        var categoryType = this._oCategory.type,
            FORMAT_DATE_HANDLER = this.FORMAT_DATE_HANDLER;

        options.chart = options.chart || {};
        options.navigator = options.navigator || {};

        //暂不提供rangeselecter
        options.rangeSelector = { enabled: false };

        // 是否使用zoom和navigator
        if (this._sChartType != 'line') { 
            options.chart.zoomType = '';
        } else {
            if (categoryType == 'datetime' 
                || categoryType == 'month'
                || categoryType == 'quarter'
                || categoryType == 'year') {
                options.navigator.enabled = true;
                options.scrollbar = {enabled: true};
                options.chart.zoomType = 'x';
            } else {
                options.navigator.enabled = false;
                options.chart.zoomType = '';
                options.scrollbar = {enabled: false};
            } 
        }
        
        // navigator初始化
        if (options.navigator.enabled) {
            options.navigator.height = 30;
            options.navigator.series = {dataGrouping: {smoothed: false}};
            options.navigator.xAxis = { 
                labels: {
                    formatter: function() {
                        var fun = FORMAT_DATE_HANDLER[categoryType];
                        return fun ? fun.call(null, this.value) : this.value;
                    },
                    style: {fontFamily: 'Arial', fontSize: '11px'}
                }
            };
        }
    }

   /**
     * 设置区域选择
     * @protected
     */
    UI_LITE_CHART_CLASS.$getTickPositioner = function() {
        var control = this;
        return function(min, max) {
            return tickPositioner.call(this, control, min, max);
        };
    };    

    /**
     * 自定义的x轴刻度排布。
     * 默认的刻度排布不能准确。
     */
    function tickPositioner(control, min, max) {
        var firstSeries, 
            i, len, item, tmin, tmax, maxTickNum, 
            d, date, month, year, month30, tickShowNumber,
            winIndexStart, winIndexEnd, winIndexLength, 
            indexInterval, tickPositions = [];
        
        var axis = this;
        var tickType = axis.options.tickType || 'datetime'; //默认

        // 取第一个series进行刻度
        firstSeries = axis.series[0];
        if( !firstSeries) {
            return [min, max];
        }

        var ordinalPositions = firstSeries.processedXData;

        // 取得当前窗口
        for (i = 0, len = ordinalPositions.length; i < len; i ++) {
            item = ordinalPositions[i];
            if( !item) { continue; }

            if (item >= min && (item - min < tmin || typeof tmin == 'undefined')) { 
                winIndexStart = i; 
                tmin = item - min; 
            }
            if (item <= max && (max - item < tmax || typeof tmax == 'undefined')) { 
                winIndexEnd = i; 
                tmax = max - item; 
            }
        }
        if (typeof winIndexEnd == 'undefined' || typeof winIndexStart == 'undefined') { 
            return [min, max]; 
        }
        winIndexLength = winIndexEnd - winIndexStart + 1;

        // 计算tick的数量和间隔（各种特例处理）
        if (tickType == 'datetime' && winIndexLength == 365 || winIndexLength == 366) {
            // 一年全部日数据的特殊处理
            // 这段代码，如果要tick和datasource对上，必须是精度到天级别的数据源
            month30 = { '4': 1, '6': 1, '9': 1, '11': 1 }; // 注意月是从0开始
            d = new Date(ordinalPositions[winIndexEnd]);
            
            for (date = d.getDate(), month = d.getMonth(), year = d.getFullYear(); ; ) {
                if (month + 1 == 2 && date >= 29) {
                    d = new Date(year, month, 29);
                    if(d.getMonth() + 1 != 2) {
                        d = new Date(year, month, 28);
                    }
                } else if (month + 1 in month30 && date >= 31) {
                    d = new Date(year, month, 30);
                } else {
                    d = new Date(year, month, date);
                }
                if (d.getTime() < ordinalPositions[winIndexStart]) {
                    break;
                }
                tickPositions.splice(0, 0, d.getTime()); 
                (month <= 0) ? (( month = 11) && (year --)) : (month --);
            }

        } else {
            // 默认情况
            tickShowNumber = control._oCategory.tickShowNumber == null 
                ? 7 
                : control._oCategory.tickShowNumber;
            indexInterval = Math.ceil(winIndexLength / tickShowNumber);
            for (i = winIndexEnd; i >= winIndexStart; i -= indexInterval) {
                tickPositions.splice(0, 0, ordinalPositions[i]);
            }
        }

        return tickPositions;
    };

    /**
     * 排序Tooltip
     *
     * @private
     * @param {Array} points 待排序的点列表
     * @returns {Array} 排序后的点列表
     */
    UI_LITE_CHART_CLASS.sortPoints = function (points) {
        if (!points) { return null; }
        var newPoints = [], i, l;
        
        for (i = 0, l = points.length; i < l; i++) {
            newPoints.push(points[i]);
        } 
        
        return newPoints.sort(function(pa, pb) {
            if (!pa) { return -1; }
            if (!pb) { return 1; }
            
            if (pa.y > pb.y) { return -1; }
            else if (pa.y < pb.y) { return 1; }
            else { return 0; }
        });
    }

    /**
     * 重新渲染图表
     * @public
     */
    UI_LITE_CHART_CLASS.render = function () {
        this.$disposeChart();

        if (!this._oData || this._oData.length <= 0) {
            this.$renderEmpty();
            return;
        }

        this.$createChart(this.$initOptions()) ;
    };

    /**
     * 渲染一个无数据图表
     *
     * @private
     */
    UI_LITE_CHART_CLASS.$renderEmpty = function () {
        this.getMain().innerHTML = ' ';//'暂无数据';
    };

    UI_LITE_CHART_CLASS.$createChart = function (options) {
        var res = true;
        switch (this._sChartType) {
            case 'line': 
                options.chart.type = 'line';
                this._oChart = new Highcharts.StockChart(options);
                break;
            case 'column': 
                options.chart.type = 'column';
                this._oChart = new Highcharts.Chart(options);
                break;
            case 'bar': 
                options.chart.type = 'bar';
                this._oChart = new Highcharts.Chart(options);
                break;
        }
    };

    /**
     * 构建图表参数
     *
     * @private
     */
    UI_LITE_CHART_CLASS.$initOptions = function () {
        var options = {
            chart: {
                renderTo: this.getMain(),
                zoomType: 'x',
                width: this._nWidth,
                height: this._nHeight
            },
            credits: {enabled: false},
            title: {
                text: null
            }
        };
        if (this._aAxis.length > 0) {
            options.chart.marginRight = 30;
        }
        
        if (!dom.ieVersion) {
            options.plotOptions = { column: { shadow: true, borderWidth: 1 } };
        }
        
        this.$setupSeries(options);
        this.$setupCategory(options);
        this.$setupPlotOptions(options);
        this.$setupAxis(options);
        this.$setupTooptip(options);
        this.$setupLegend(options);
        this.$setupZoom(options);
        
        return options;
    };

    /**
     * 销毁图表
     *
     * @private
     */
    UI_LITE_CHART_CLASS.$disposeChart = function () {
        if (this._oChart) {
            this._oChart.destroy();
            this._oChart = null;
        }
    };

    /**
     * @override
     */
    UI_LITE_CHART_CLASS.$dispose = function () {
        this.$disposeChart();
        UI_CONTROL_CLASS.$dispose.call(this);
    };

})();
