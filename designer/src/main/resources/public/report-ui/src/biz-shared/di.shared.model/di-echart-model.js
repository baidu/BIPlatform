/**
 * di.shared.model.DIChartModel
 * Copyright 2013 Baidu Inc. All rights reserved.
 *
 * @file:    DI 图模型组件
 * @author:  sushuang(sushuang)
 * @depend:  xui, xutil
 */

$namespace('di.shared.model');

(function() {
    
    //------------------------------------------
    // 引用
    //------------------------------------------

    var URL = di.config.URL;
    var UTIL = di.helper.Util;
    var inheritsObject = xutil.object.inheritsObject;
    var wrapArrayParam = xutil.url.wrapArrayParam;
    var textParam = xutil.url.textParam;
    var logError = UTIL.logError;
    var getUID = xutil.uid.getUID;
    var XDATASOURCE = xui.XDatasource;

    //------------------------------------------
    // 类型声明
    //------------------------------------------

    /**
     * DI 图模型组件
     *
     * @class
     * @extends xui.XDatasource
     * @param {Function=} options.commonParamGetter      
     */
    var DI_ECHART_MODEL =
            $namespace().DIEChartModel =
            inheritsObject(XDATASOURCE, constructor);
    var DI_ECHART_MODEL_CLASS =
            DI_ECHART_MODEL.prototype;

    //------------------------------------------
    // 方法
    //------------------------------------------

    /**
     * 构造方法
     *
     * @private
     * @param {Object} options 参数
     */
    function constructor(options) {
        /**
         * 得到公用的请求参数
         *
         * @type {Function}
         * @private
         */
        this._fCommonParamGetter = options.commonParamGetter;
        /**
         * 图后台返回的原始数据
         *
         * @type {Object}
         * @private
         */
        this._oRawChartData = {};
        /**
         * 图前台显示的数据
         *
         * @type {Object}
         * @private
         */
        this._oChartData = {};
    }

    /**
     * @override
     */
    DI_ECHART_MODEL_CLASS.init = function() {};

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DI_ECHART_MODEL_CLASS.url = new XDATASOURCE.Set(
        {
            DATA: URL.fn('OLAP_CHART_DATA'),
            X_DATA: URL.fn('OLAP_CHART_X_DATA'),
            LITEOLAPCHART_DATA: URL.fn('LITEOLAP_CHART_DATA'),
            S_DATA: URL.fn('OLAP_CHART_S_DATA'),
            S_ADD_DATA: URL.fn('OLAP_CHART_S_ADD_DATA'),
            S_REMOVE_DATA: URL.fn('OLAP_CHART_S_REMOVE_DATA'),
            OFFLINE_DOWNLOAD: URL.fn('OLAP_CHART_OFFLINE_DOWNLOAD'),
            CHANGE_RADIOBUTTON: URL.fn('OLAP_CHART_CHANGE_RADIOBUTTON')
        }
    );

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DI_ECHART_MODEL_CLASS.businessKey = new XDATASOURCE.Set(
        {
            DATA: 'DI_ECHART_MODEL_DATA_' + getUID(),
            X_DATA: 'DI_ECHART_MODEL_X_DATA_' + getUID(),
            LITEOLAPCHART_DATA: 'DI_LITEOLAP_CHART_DATA_' + getUID(),
            S_DATA: 'DI_ECHART_MODEL_S_DATA_' + getUID(),
            S_ADD_DATA: 'DI_ECHART_MODEL_S_ADD_DATA_' + getUID(),
            S_REMOVE_DATA: 'DI_ECHART_MODEL_S_REMOVE_DATA_' + getUID(),
            OFFLINE_DOWNLOAD: 'DI_CHART_OFFLINE_DOWNLOAD_' + getUID(),
            CHANGE_RADIOBUTTON: 'DI_CHART_CHANGE_RADIOBUTTON_' + getUID()
        }
    );

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DI_ECHART_MODEL_CLASS.param = new XDATASOURCE.Set(
        {
            DATA: doParam,
            X_DATA: doParam,
            S_DATA: doParam,
            S_ADD_DATA: doParam,
            S_REMOVE_DATA: doParam,
            CHANGE_RADIOBUTTON:doParam,
            LITEOLAPCHART_DATA: function (options) {
                var reportTemplateId = this._fCommonParamGetter.getReportTemplateId();
                var paramArr = [];
                paramArr.push(this._fCommonParamGetter());
                paramArr.push('analysisType=timetrend');
                paramArr.push('sourceTemplateId=' + reportTemplateId);
                paramArr.push('componentId=' + options.args.param.componentId);
                if(options.args.param.COLUMN && options.args.param.COLUMN.length > 0){
                    var indNames = options.args.param.COLUMN;
                    for (var i = 0 ; i < indNames.length ; i++) {
                        paramArr.push('indNames='+textParam(indNames[i]));
                    };
                }
                return paramArr.join('&');
            },
            OFFLINE_DOWNLOAD: function (options) {
                return this._fCommonParamGetter(
                    { mainTo: options.args.param.email }
                );
            }
        }
    );
    function doParam(options) {
        var param = options.args.param;
        
        if (param.uniqueName) {
            // FIXME
            // 兼容老代码，现在还有用吗？
            param.dimTags = param.uniqueName;
            delete param.uniqueName;
        }

        if (param.uniqueNames) {
            // @deprecated
            // 兼容老报表，新报表中直接用argHandler中的dimTagsList即可
            param.dimTagsList = param.uniqueNames;
            delete param.uniqueNames;
        }

        return this._fCommonParamGetter(param);
    }

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DI_ECHART_MODEL_CLASS.complete = new XDATASOURCE.Set(
        {
            DATA: doComplete,
            X_DATA: doComplete,
            LITEOLAPCHART_DATA: doComplete,
            S_DATA: doComplete,
            S_ADD_DATA: doComplete,
            S_REMOVE_DATA: doComplete,
            OFFLINE_DOWNLOAD: doComplete,
            CHANGE_RADIOBUTTON: doComplete
        }
    );

    function doComplete(ejsonObj) {
        // 换reportTemplateId（后台生成了副本，所以约定更换为副本的id）
        // FIXME 
        // 换成非嵌入的实现方式
        this._fCommonParamGetter.update(ejsonObj.data);
    }

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DI_ECHART_MODEL_CLASS.parse = new XDATASOURCE.Set(
        {
            DATA: doParse,
            X_DATA: doParse,
            LITEOLAPCHART_DATA: doParse,
            S_DATA: doParse,
            S_ADD_DATA: doParse,
            S_REMOVE_DATA: doParse,
            CHANGE_RADIOBUTTON: doParse
        }
    );

    /**
     * 图数据解析
     *
     * @private
     */
    function doParse(data, ejsonObj, options) {
        try {
            var rawData = this._oRawChartData = data['reportChart'];

            // 解析图后台返回数据
            var chartData = {};

            // FIXME:暂时所有datetime类型了都作date类型
            if (rawData.xAxisType == 'datetime') {
                rawData.xAxisType = 'date';
            }
            chartData.chartType = 'line';
            chartData.series = [];
            for (var x = 0, item; item = rawData.seriesData[x]; x ++) {
                chartData.series[x] = {};
                chartData.series[x].colorDefine = item.colorDefine;
                chartData.series[x].format = item.format;
                chartData.series[x].name = item.name;
                chartData.series[x].type = item.type;
                chartData.series[x].yAxisName = item.yAxisName;
                // 双y轴数据设置
                chartData.series[x].yAxisIndex = item.position;
                chartData.series[x].data = [];
                for (var y = 0, yLen = item.data.length; y < yLen; y ++) {
                    if (item.type === 'pie' && !item.data[y]) {
                        continue;
                    }
                    chartData.series[x].data[y] = item.data[y] ? item.data[y] : 0;
                }
            }

            // 横轴
            chartData.xAxis = {
                type: rawData.xAxisType,
                data: rawData.xAxisCategories
            };

            // 多y轴的处理
            // 兼容老代码：如果没有多轴的情况，就不进行轴设置
            var yNameMap = {};
            var k;
            var ser;
            for (k = 0; ser = chartData.series[k]; k ++) {
                yNameMap[ser.yAxisName] = ser.yAxisIndex;
            }
            k = 0;

            // y轴
            chartData.yAxis = [];
            if (rawData.yAxises) {
                for (var i = 0, ya; ya = rawData.yAxises[i]; i ++) {
                    // rawData.yAxises中的y轴可能比实际series中使用的y轴多，
                    // 所以只有实际使用的，才会被设置
                    if (yNameMap[ya.name]) {
                        chartData.yAxis.push(
                            {
                                // 数值的格式化
                                format: ya.format,
                                // 轴上的文字
                                title: ya.unitName ? { text: ya.unitName } : null
                            }
                        );
                        // 记录index
                        yNameMap[ya.name] = i;
                    }
                }
            }

            // 对series设置y轴的index
            var yAxisNum = 0;
            for (k in yNameMap) { yAxisNum ++; }
            if (yAxisNum > 1) {
                for (k = 0; ser = chartData.series[k]; k ++) {
                    ser.yAxisIndex = yNameMap[ser.yAxisName];
                }
            }
            chartData.appearance = rawData.appearance;
            chartData.render = rawData.render;
            chartData.allMeasures = rawData.allMeasures;
            chartData.defaultMeasures = rawData.defaultMeasures;
            chartData.allDims = rawData.allDims;
            chartData.defaultDims = rawData.defaultDims;
            // 在地图设置颜色值范围时，需要值的最大值和最小值
            chartData.mapMaxValue = rawData.maxValue;
            chartData.mapMinValue = rawData.minValue;
            chartData.dimMap = rawData.dimMap;
            this._oChartData = chartData;                    
        }
        catch (e) {
            logError(e);
            this.$goError();
        }
    }

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DI_ECHART_MODEL_CLASS.error = new XDATASOURCE.Set(
        {
            DATA: doError,
            X_DATA: doError,
            LITEOLAPCHART_DATA: doError,
            S_DATA: doError,
            S_ADD_DATA: doError,
            S_REMOVE_DATA: doError,
            CHANGE_RADIOBUTTON: doError
        }
    );

    /**
     * 数据错误处理
     *
     * @private
     */
    function doError(status, ejsonObj, options) {    
        this._oRawChartData = {};
        this._oChartData = {};
    }

    /**
     * 得到图数据
     *
     * @public
     * @return {Object} 图数据
     */
    DI_ECHART_MODEL_CLASS.getChartData = function() {
        return this._oChartData;
    };

})();
