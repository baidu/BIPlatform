/**
 * 
 * Copyright 2014 Baidu Inc. All rights reserved.
 *
 * @file:    前端图形api组件
 * @author:  majun(majun04)
 * @depend:  echart
 */
 DiChart = {};
 DiChart.generateChart = function(domId,options){
     var USER_AGENT = navigator.userAgent;
     ieVersion = /msie (\d+\.\d)/i.test(USER_AGENT) ? document.documentMode || (REGEXP.$1 - 0) : undefined;
     // 图表实例化------------------
     // srcipt标签式引入
     var chartDivDom = document.getElementById(domId);
     var echartsOption = {};

     setChartDomSize();

     var myChart = echarts.init(chartDivDom);

     initEchartsOptions(options);

     myChart.setOption(echartsOption);
     
     function initEchartsOptions(options){
        // 如果发现图数据为空，或者x轴类别为空时，直接提示无数据
        if(!options.categories || options.categories.length == 0
           ||!options.seriesData || options.seriesData.length == 0){
            chartDivDom.innerHTML = '' 
                + '<div style = "width: 100%;text-align: center;font-size: 12px;color: #999;">' 
                    + '无数据显示'
                + '</div>';
        } else{
            setupTitle(options);
            setupTooltip(options);
            setupToolbox(options);
            setupDataZoom(options);
            setupLegend(options);
            setupGrid(options);
            setupXAxis(options);
            setupYAxis(options);
            setupSeries(options);
            setupCommonOption(options);
        }
     }
     // 设置标题，默认都不要标题
     function setupTitle(options){
        var chartTitle = {};
        chartTitle.text = options.title;
        chartTitle.x = 'center';
        echartsOption.title = chartTitle;
     }
     // 设置图形的通用属性，包括颜色列表，是否开启计算功能等设置
     function setupCommonOption(options){
        if(options.color){
            echartsOption.color = options.color;
        }
        if(options.calculable || options.calculable == 'true'){
            echartsOption.calculable = options.calculable;
        }
     }
     // 设置提示，这里可以设置是以轴提示还是以值提示
     function setupTooltip(options){
        var toolTip = {};
        if(hasPie(options)){
            toolTip.formatter = "{a} <br/>{b} : {c} ({d}%)";
            toolTip.trigger = 'item';
        }else{
            toolTip.trigger = 'axis';
            // 在此将提示信息的format属性加上以便方便显示
            toolTip.formatter =  function(params,ticket,callback) {
                                    var res = params[0][1];
                                    for (var i = 0, l = params.length; i < l; i++) {
                                        var valueFormat = options.seriesData[i].format;
                                        var valueLable = params[i][2];
                                        // 当发现图数据有配置format属性时，按format所示进行展示
                                        // 当没有format的时候，展示原值
                                        if(valueFormat){
                                            valueLable = formatNumber(params[i][2],valueFormat,null,null,true);
                                        }
                                        res += '<br/>' + params[i][0] + ' : ' + valueLable;
                                    }
                                    return res;
                                }
        }
        echartsOption.tooltip = toolTip;
     }
     // 设置工具栏属性
     function setupToolbox(options){
        var toolbox = {};
        var feature = {};
        toolbox.show = false ;
        toolbox.feature = feature;
        //feature.dataZoom = true;
        //feature.magicType = ['line', 'bar'];
        //feature.restore = true;
        //feature.saveAsImage = true;
        echartsOption.toolbox = toolbox;
     }
     // 设置数据缩放属性
     function setupDataZoom(options){
        if(!hasPie(options)){
            var dataZoom = {};
            dataZoom.show = false;
            if(options.categories && options.categories.length > 10){
                dataZoom.show = true;
            }
            dataZoom.realtime = false;
            dataZoom.start = 20;
            dataZoom.end = 80;
            //只有当全局属性里面配置了需要“数据缩放功能”，这里才设置dataZoom.show为true
            if(options.needDataZoom && options.needDataZoom == true){
            	dataZoom.show = true;
            	echartsOption.dataZoom = dataZoom;
            }
        }
        
     }
     // 设置图区域距离容器位置
     function setupGrid(){
        if(!hasPie(options)){
            var grid = {};
            grid.x = 80;
            //如果在全局属性中设置了网格距离左边界距离，则这里设置为全局属性中的定义值。
            if(options.gridLeftPadding){
            	grid.x = options.gridLeftPadding;
            }
            //如果在全局属性中设置了网格距离右边界距离，则这里设置为全局属性中的定义值，默认为10px。
            grid.x2 = 10;
            if(options.gridRightPadding){
            	grid.x2 = options.gridRightPadding;
            }
            echartsOption.grid = grid;
        }
     }
     // 设置图例
     function setupLegend(options){
        var legend = {};
        var data = [];
        if(hasPie(options)){
            for (var i = 0; i < options.categories.length; i++) {
                    data[i] = options.categories[i];
                };
            legend.orient = 'vertical';
        }else{
           if(options.seriesData && options.seriesData.length > 0){
                for (var i = 0; i < options.seriesData.length; i++) {
                    data[i] = options.seriesData[i].name;
                };
            } 
        }        
        
        legend.data = data;
        legend.x = 'left';
        //legend.orient = 'vertical',
        legend.padding = 5;
        legend.itemGap = 10;
        echartsOption.legend = legend;
     }
     // 设置x轴
     function setupXAxis(options){
        if(!hasPie(options)){
            var xAxis = [];
            var data = [];
            var category = {};
            
            if(options.categories && options.categories.length > 0){
                category.type = 'category';
                category.data = options.categories;
                xAxis.push(category);
            }
            // 如果发现配置需要x和y轴反转，那么在此将两边的设置也相应倒置过来
            if(options.inversion){
                echartsOption.yAxis = xAxis;
            }else{
                echartsOption.xAxis = xAxis;
            }
            
        }
     }
     // 设置y轴
     function setupYAxis(options){
        if(!hasPie(options)){
            var yAxis = [];
            if(options.valueAxisOptions && options.valueAxisOptions.length > 0){
                for (var i = 0; i < options.valueAxisOptions.length; i++) {
                    var option = options.valueAxisOptions[i];
                    var yAxisOption = {};
                    yAxisOption.type = 'value';
                    yAxisOption.splitArea = {show : true};
                    yAxisOption.boundaryGap = [0.1, 0.1];
                    if(option.splitNumber){
                        yAxisOption.splitNumber = option.splitNumber;
                    }else{  
                        yAxisOption.splitNumber = 5;
                    }
                    if(option.position){
                        yAxisOption.position = option.position;
                    }else{  
                        yAxisOption.position = 'left';
                    }
                    if(option.unit){
                        yAxisOption.axisLabel = {
                            formatter: '{value} '+ option.unit
                        }
                    }
                    yAxis.push(yAxisOption);
                };
            }else{
                var yAxisOption = {};
                yAxisOption.type = 'value';
                yAxisOption.splitArea = {show : true};
                yAxisOption.boundaryGap = [0.1, 0.1];
                yAxisOption.splitNumber = 5;
                yAxis.push(yAxisOption);
            }

            // 如果发现配置需要x和y轴反转，那么在此将两边的设置也相应倒置过来
            if(options.inversion){
                echartsOption.xAxis = yAxis;
            }else{
                echartsOption.yAxis = yAxis;
            }
        }
     }
     // 设置要展示的数据
     function setupSeries(options){
        var series = [];
        if(hasPie(options) == true){
            var pieData = getPieData(options);
            var seriesData = {};
            var data = [];
                for (var i = 0; i < options.categories.length; i++) {
                    var apieData = {};
                    // 如果图的类别多余所提供的图数据，那么设置数据的时候只设置有数据的类别
                    if(pieData.data[i]){
                        apieData.name = options.categories[i];
                        apieData.value = pieData.data[i];
                        data.push(apieData);
                    }
                };
                seriesData.name = pieData.name;
                seriesData.type = 'pie';
                seriesData.radius = '65%';
                //seriesData.center = ['50%', '50%'];
                seriesData.data = data;
                series.push(seriesData);
        }else{
            if (options.seriesData && options.seriesData.length > 0) {
                // 先检查是否是双值轴
                var isMutiValueAxis = false;
                if(options.valueAxisOptions && options.valueAxisOptions.length > 1){
                    isMutiValueAxis = true ;
                }
                for (var i = 0; i < options.seriesData.length; i++) {
                    var seriesData = options.seriesData[i];
                    var echartSeriesData = {};
                    echartSeriesData.type = seriesData.type;
                    echartSeriesData.name = seriesData.name;
                    var chartSeriesData = seriesData.data;
                    //当seriesData的个数多于categories的个数，那么以categories的个数为准
                    if(seriesData.data.length > options.categories.length){
                    	chartSeriesData = seriesData.data.slice(0, options.categories.length);
                    }
                    //当categories的个数大于seriesData的个数，需要把seriesData不足的以'-'补齐。
                    if(options.categories.length > seriesData.data.length){
                    	var appendItemIndex = seriesData.data.length;
                    	for(j = appendItemIndex ; j < options.categories.length ; j++){
                    		seriesData.data.push('-');
                    	}
                    }
                    echartSeriesData.data = chartSeriesData;
                    if(seriesData.yAxisIndex && !options.inversion && isMutiValueAxis){
                        echartSeriesData.yAxisIndex = seriesData.yAxisIndex;
                    }
                    
                    //设置每条线的自定义风格
                    var itemStyle = {};
                    var normalStyle = {};
                    var lineStyle = {};
                    if(seriesData.lineType){
                        lineStyle.type = seriesData.lineType;
                    }
                    if(seriesData.lineWidth){
                        lineStyle.width = seriesData.lineWidth;
                    }
                    if(seriesData.lineColor){
                        normalStyle.color = seriesData.lineColor;
                    }

                    normalStyle.lineStyle = lineStyle;
                    itemStyle.normal = normalStyle;
                    echartSeriesData.itemStyle = itemStyle;
                    series[i] = echartSeriesData;
                }; 
            };
        }
        
        echartsOption.series = series;
     }
     // 判断给定的接口数据是否有饼图类型，因为饼图类型比较特殊，设置legend和设置数据都需要特殊设置
     function hasPie(options){
        var flag = false;
        if(options.seriesData && options.seriesData.length > 0){
            for (var i = 0; i < options.seriesData.length; i++) {
                if(options.seriesData[i].type == 'pie'){
                    flag = true;
                    break;
                }
            };
        }
        return flag;
     }
     // 得到图形数据
     function getPieData(options){
        var pieData = {};
        for (var i = 0; i < options.seriesData.length; i++) {
                if(options.seriesData[i].type == 'pie'){
                    pieData = options.seriesData[i];
                    break;
                }
            };
        return pieData;
     }
     // 设置chart组件外层dom容器的宽度和高度
     function setChartDomSize(){
        var domWidth = ieVersion ? chartDivDom.currentStyle.width : getComputedStyle(chartDivDom).width;
        var domHeight = ieVersion ? chartDivDom.currentStyle.height : getComputedStyle(chartDivDom).height;
        if(domWidth == '0px' || domWidth == 'auto'){
            var domOffsetWidth = chartDivDom.offsetWidth;
            domWidth = domOffsetWidth;
            //chartDivDom.style.width = domOffsetWidth;
        }
        if(domHeight == '0px' || domHeight == 'auto'){
            chartDivDom.style.height = '400px';
        }

     }

    /**
     * 将数值按照指定格式进行格式化
     * 支持：
     *      三位一撇，如：'23,444,12.98'
     *      前后缀，如：'23,444$', '23,444%', '#23,444'
     *      四舍五入
     *      四舍六入中凑偶（IEEE 754标准，欧洲金融常用）
     *      正数加上正号，如：'+23.45%'
     *      
     * @public
     * @example formatNumber(10000/3, "I,III.DD%"); 返回"3,333.33%"
     * @param {number} num 要格式化的数字
     * @param {string} formatStr 指定的格式
     *              I代表整数部分,可以通过逗号的位置来设定逗号分隔的位数 
     *              D代表小数部分，可以通过D的重复次数指定小数部分的显示位数
     * @param {string} usePositiveSign 是否正数加上正号
     * @param {number} cutMode 舍入方式：
     *                      0或默认:四舍五入；
     *                      2:IEEE 754标准的五舍六入中凑偶；
     *                      other：只是纯截取
     * @param {boolean} percentMultiply 百分数（formatStr满足/[ID]%/）是否要乘以100
     *                      默认为false
     * @return {string} 格式化过的字符串
     */
      function formatNumber(
            num, formatStr, usePositiveSign, cutMode, percentMultiply
        ) {
        if (!formatStr) {
            return num;
        }

        if (percentMultiply && /[ID]%/.test(formatStr)) {
            num = num * 100;
        }

        num = fixNumber(num, formatStr, cutMode); 
        var str;
        var numStr = num.toString();
        var tempAry = numStr.split('.');
        var intStr = tempAry[0];
        var decStr = (tempAry.length > 1) ? tempAry[1] : "";
            
        str = formatStr.replace(/I+,*I*/g, function () {
            var matchStr = arguments[0];
            var commaIndex = matchStr.lastIndexOf(",");
            var replaceStr;
            var splitPos;
            var parts = [];
                
            if (commaIndex >= 0 && commaIndex != intStr.length - 1) {
                splitPos = matchStr.length - 1 - commaIndex;
                var diff;
                while (
                    (diff = intStr.length - splitPos) > 0
                    && splitPos > 0 /*防止配错引起死循环*/
                ) {
                    parts.push(intStr.substr(diff, splitPos));
                    intStr = intStr.substring(0, diff);
                }
                parts.push(intStr);
                parts.reverse();
                if (parts[0] == "-") {
                    parts.shift();
                    replaceStr = "-" + parts.join(",");
                } 
                else {
                    replaceStr = parts.join(",");
                }
            } 
            else {
                replaceStr = intStr;
            }
            
            if (usePositiveSign && replaceStr && replaceStr.indexOf('-') < 0) {
                replaceStr = '+' + replaceStr;
            }
            
            return replaceStr;
        });
        
        str = str.replace(/D+/g, function () {
            var matchStr = arguments[0]; 
            var replaceStr = decStr;
            
            if (replaceStr.length > matchStr.length) {
                replaceStr = replaceStr.substr(0, matchStr.length);
            } 
            else {
                replaceStr += (
                    new Array(matchStr.length - replaceStr.length)
                ).join('0');
            }
            return replaceStr;
        });
        // if ( !/[1-9]+/.test(str) ) { // 全零去除加减号，都不是效率高的写法
            // str.replace(/^(\+|\-)./, '');
        // } 
        return str;
    };

    /**
     * 不同方式的舍入
     * 支持：
     *      四舍五入
     *      四舍六入中凑偶（IEEE 754标准，欧洲金融常用）
     * 
     * @public
     * @param {number} cutMode 舍入方式
     *                      0或默认:四舍五入；
     *                      2:IEEE 754标准的五舍六入中凑偶
     */
    function fixNumber(num, formatStr, cutMode) {
        var formatDec = /D+/.exec(formatStr);
        var formatDecLen = (formatDec && formatDec.length>0) 
                ? formatDec[0].length : 0;
        var p;
            
        if (!cutMode) { // 四舍五入
            p = Math.pow(10, formatDecLen);
            return ( Math.round (num * p ) ) / p ;
        } 
        else if (cutMode == 2) { // 五舍六入中凑偶
            return Number(num).toFixed(formatDecLen);
        } 
        else { // 原样
            return Number(num);
        }
    };
     // ajax getting data...............

     // 过渡---------------------
     // myChart.showLoading({
     //     text: '正在努力的读取数据中...',    //loading话术
     // });

     // ajax return
     // myChart.hideLoading();

     // 图表使用-------------------
     
     // 增加些数据------------------
     // option.legend.data.push('win');
     // option.series.push({
     //         name: 'win',                            // 系列名称
     //         type: 'line',                           // 图表类型，折线图line、散点图scatter、柱状图bar、饼图pie、雷达图radar
     //         data: [112, 23, 45, 56, 233, 343, 454, 89, 343, 123, 45, 123]
     // });
    // myChart.setOption(option);


     // 图表清空-------------------
     //myChart.clear();

     // 图表释放-------------------
     //myChart.dispose();
     return myChart;
 };




