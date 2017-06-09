/**
 * xutil.date
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:   类库所依赖的公共方法库
 * @author: lizhantong
 */

(function () {
    /* globals xutil */
    var COMMONUTIL = xutil.commonUtil;

    /**
     * 数值前部补0
     *
     * @public
     * @param {(number|string)} source 输入数值, 可以整数或小数
     * @param {number} length 输出数值长度
     * @return {string} 输出数值
     */
    COMMONUTIL.pad = function (source, length) {
        var pre = '';
        var negative = (source < 0);
        var string = String(Math.abs(source));
        if (string.length < length) {
            pre = (new Array(length - string.length + 1)).join('0');
        }
        return (negative ?  '-' : '') + pre + string;
    };

    /**
     * 删除目标字符串两端的空白字符 (@see tangram)
     *
     * @pubilc
     * @param {string} source 目标字符串
     * @return {string} 删除两端空白字符后的字符串
     */
    COMMONUTIL.trim = function (source) {
        var TRIMER = new RegExp(
            '(^[\\s\\t\\xa0\\u3000]+)|([\\u3000\\xa0\\s\\t]+\x24)', 'g'
        );
        return source == null
            ? ''
            : String(source).replace(TRIMER, '');
    };
})();