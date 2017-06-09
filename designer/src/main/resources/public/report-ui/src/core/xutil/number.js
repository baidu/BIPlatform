/**
 * xutil.number
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @change: 修改formatNumber方法，date类数值的format支持。by MENGRAN at 2013-12-06
 * @file:    数值相关工具函数
 * @author:  sushuang(sushuang)
 * @depend:  none
 */

(function () {
    
    var NUMBER = xutil.number;
    var DATE = xutil.date; // Add by MENGRAN at 2013-12-6
            
    /**
     * 得到序数词(1st, 2nd, 3rd, 4th, ...)的英文后缀
     * 
     * @public
     * @param {number} number 序数的数值
     * @return {string} 序数词英文后缀
     */    
    NUMBER.ordinalSuffix = function (number) {
        if (number == 1) {
            return 'st';
        } 
        else if (number == 2) {
            return 'nd';
        } 
        else if (number == 3) {
            return 'rd';
        } 
        else {
            return 'th';
        }
    };
    
    /**
     * 数值前部补0
     * 
     * @public
     * @param {(number|string)} source 输入数值, 可以整数或小数
     * @param {number} length 输出数值长度
     * @return {string} 输出数值
     */
    NUMBER.pad = function (source, length) {
        var pre = "";
        var negative = (source < 0);
        var string = String(Math.abs(source));
    
        if (string.length < length) {
            pre = (new Array(length - string.length + 1)).join('0');
        }
    
        return (negative ?  "-" : "") + pre + string;
    };
    
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
    NUMBER.formatNumber = function (
        num, formatStr, usePositiveSign, cutMode, percentMultiply
    ) {
        if (!formatStr) {
            return num;
        }
        // add by majun  2014-3-20 14:27:53
        // 如果发现要格式化的数字根本就不是number类型的，则直接返回原始值
        if(isNaN(num)){
            return num;
        }

        // Add by MENGRAN at 2013-12-6
        // 导致number和date两个库循环引用了。我先这么改着。
        if (DATE.isValidFormatPattern(formatStr)) {
            return DATE.format(DATE.parse(num), formatStr);
        }

        if(DATE.TIME_REG.test(formatStr)){
            return DATE.formatTime(num,formatStr);
        }

        if (percentMultiply && /[ID]%/.test(formatStr)) {
            num = num * 100;
        }

        num = NUMBER.fixNumber(num, formatStr, cutMode); 
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
                    // new Array(matchStr.length - replaceStr.length)
                    new Array(matchStr.length - replaceStr.length + 1)
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
    NUMBER.fixNumber = function (num, formatStr, cutMode) {
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

})();
