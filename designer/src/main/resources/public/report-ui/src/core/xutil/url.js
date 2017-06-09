/**
 * xutil.url
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    时间相关工具函数
 * @author:  sushuang(sushuang)
 * @depend:  xutil.lang
 */

(function () {
    
    var URL = xutil.url;
    var LANG = xutil.lang;
    var objProtoToString = Object.prototype.toString;
    var arrayProtoSlice = Array.prototype.slice;

    /**
     * 包装js原生的decodeURIComponent，
     * 对于undefined和null均返回空字符串
     * 
     * @public
     * @param {string} input 输入文本
     * @return {string} 输出文本
     */
    URL.decodeURIComponent = function (input) { 
        return LANG.hasValueNotBlank(input) 
            ? decodeURIComponent(input) : input;
    };
    
    /**
     * 向URL增加参数
     * 
     * @public
     * @param {string} url 输入url
     * @param {string} paramStr 参数字符串
     * @param {number} urlType url类型，1:普通URL（默认）; 2:erURL 
     * @return {string} 结果url
     */
    URL.appendParam = function (url, paramStr, urlType) {
        urlType = urlType || 1;

        if (url.indexOf('?') < 0) {
            url += (urlType == 2 ? '~' : '?') + paramStr;
        } 
        else {
            url += '&' + paramStr;
        }

        return url;
    };

    /**
     * 替换url中的参数。如果没有此参数，则添加此参数。
     * 
     * @public
     * @param {string} 输入url
     * @param {string} paramName 参数名
     * @param {string} newValue 新参数值，如果为空则给此paramName赋空字串
     * @param {number} urlType url类型，1:普通URL（默认）; 2:erURL 
     * @return {string} 结果url
     */
    URL.replaceIntoParam = function (url, paramName, newValue, urlType) {
        var retUrl = url;
        
        if (!retUrl || !LANG.hasValueNotBlank(paramName)) { 
            return retUrl; 
        }
        newValue = newValue != null ? newValue : '';

        var regexp = new RegExp('([&~?])' + paramName + '=[^&]*');
        var paramStr = paramName + '=' + newValue;
        if (regexp.test(retUrl)) { // 替换
            // js不支持反向预查
            retUrl = retUrl.replace(regexp, '$1' + paramStr); 
        } 
        else { // 添加
            retUrl = URL.appendParam(retUrl, paramStr, urlType);
        }
        return retUrl;
    };

    /**
     * 一个将请求参数转换为对象工具函数
     * 
     * @public
     * @usage url.parseParam('asdf=123&qwer=654365&t=43&t=45&t=67'); 
     *          一个将请求参数转换为对象工具函数
     *          其中如上例，返回对象{asdf:123, qwer:654365, t: [43, 45, 67]}
     * @param {string} paramStr 请求参数字符串
     * @param {Object} options 
     * @param {Object} options.dontParseBoolean 不将"true"，"false"转换为true，false
     *          默认是转换的
     * @return {Object} 请求参数封装对象，如上例
     */
    URL.parseParam = function (paramStr, options) {
        var paramMap = {};
        options = options || {};

        if (paramStr == null) {
            return paramMap;
        }

        var paramArr = paramStr.split('&');
        for (var i = 0, len = paramArr.length, o; i < len; i++) {
            o = paramArr[i] != null ? paramArr[i] : '';
            o = o.split('=');
            var key = o[0];
            var value = o[1];

            if (!options.dontParseBoolean) {
                value == 'true' && (value = true);
                value == 'false' && (value = false);
            }
            
            if (key == null) { continue; }

            if (paramMap.hasOwnProperty(key)) {
                if (objProtoToString.call(paramMap[key]) == '[object Array]') {
                    paramMap[key].push(value);
                } 
                else {
                    paramMap[key] = [paramMap[key], value];   
                }
            } 
            else {
                paramMap[key] = value;   
            }
        }
        return paramMap;
    };

    /**
     * 请求参数变为string
     * null和undefined会被转为空字符串
     * 可支持urlencoding
     * 
     * @public
     * @usage url.stringifyParam({asdf:123, qwer:654365, t: [43, 45, 67]})
     *          一个将请求参数对象转换为数组的工具函数
     *          其中如上例，返回['asdf=123', 'qwer=654365', 't=43', 't=45', 't=67'] 
     *          可自己用join('&')变为请求参数字符串'asdf=123&qwer=654365&t=43&t=45&t=67'
     *
     * @param {Object} paramObj 请求参数封装
     *      key为参数名，
     *      value为参数值，{string}或者{Array.<string>}类型   
     * @param {boolean} useEncoding 是否使用urlencoding，默认false
     * @return {Array.<string>} 请求参数数组
     */
    URL.stringifyParam = function (paramObj, useEncoding) {
        var paramArr = [];
        var textParam = URL.textParam;

        function pushParam(name, value) {
            paramArr.push(
                textParam(name, !useEncoding) 
                + '=' 
                + textParam(value, !useEncoding)
            );
        }    

        var name;
        var value;
        var i;
        for (name in (paramObj || {})) {
            value = paramObj[name];
            if (Object.prototype.toString.call(value) == '[object Array]') {
                for (i = 0; i < value.length; i ++) {
                    pushParam(name, value[i]);
                }
            }
            else {
                pushParam(name, value);
            }
        }
        return paramArr;
    };

    /**
     * 格式化文本请求参数的方便函数，统一做提交前最常需要做的事：
     * (1) 判空防止请求参数中出现null/undefined字样，
     * (2) encodeURIComponent（默认进行，可配置）
     *
     * @public
     * @param {string} str 参数值
     * @param {boolean} dontEncoding 默认false
     * @param {string} defaultValue 数据为空时默认值，缺省为''
     * @return {string} 用于传输的参数值
     */
    URL.textParam = function (str, dontEncoding, defaultValue) {
        typeof defaultValue == 'undefined' && (defaultValue = '');
        str = str == null ? defaultValue : str;
        return dontEncoding ? str : encodeURIComponent(str);
    };

    /**
     * 格式化数值请求参数的方便函数，统一做提交前最常需要做的事：
     * 防止请求参数中出现null/undefined字样，如果为空可指定默认值
     *
     * @public
     * @param {(string|number)} value 参数值
     * @param {string} defaultValue 数据为空时的默认值，缺省为''
     * @return {string} 用于传输的参数值
     */
    URL.numberParam = function (value, defaultValue) {
        typeof defaultValue == 'undefined' && (defaultValue = '');
        return (value == null || value === '') ? defaultValue : value;
    };

    /**
     * 格式化数值请求参数的方便函数，统一做提交前最常需要做的事：
     * 直接构造array请求参数，如 aaa=1&aaa=233&aaa=443 ...
     * 防止请求参数中出现null/undefined字样，如果为空可指定默认值
     * 
     * @public
     * @param {array} arr 要构成arr的参数，结构可以为两种
     *              (1) ['asdf', 'zxcv', 'qwer']
     *                  不需要传入attrName。
     *              (2) [{ t: 'asdf' }, { t: 'zxcv' }]
     *                  需要传入attrName为t。
     * @param {string} paramName 参数名
     *                  如上例，假如传入值'aaa'，
     *                  则返回值为aaa=asdf&aaa=zxcv&aaa=qwer
     * @param {string=} attrName 为arr指定每项的属性名，解释如上
     * @param {Function=} paramFunc 即每个参数的处理函数,
     *                  缺省则为xutil.url.textParam
     * @param {...*} paramFunc_args 即paramFunc的补充参数
     * @return {Array} 参数字符串数组，如['aa=1', 'aa=33', 'aa=543']
     *              可直接使用join('&')形成用于传输的参数aa=1&aa=33&aa=543
     */
    URL.wrapArrayParam = function (arr, paramName, attrName, paramFunc) {
        if (!arr || !arr.length) {
            return [];
        }
        
        paramFunc = paramFunc || URL.textParam;
        var args = arrayProtoSlice.call(arguments, 4);

        var paramArr = [];
        for (var i = 0, item; i < arr.length; i ++) {
            item = arr[i];
            if (item === Object(item)) { // 如果item为Object
                item = item[attrName];
            }
            item = paramFunc.apply(null, [item].concat(args));
            paramArr.push(paramName + '=' + item);
        }

        return paramArr;
    };

    /**
     * 获取url传参值
     * @param {string} key url参数
     * @private
     * @return {string} 匹配到的参数值
     */
    URL.request = function (key) {
        var reg = new RegExp('(^|&)' + key + '=([^&]*)(&|$)', 'i');
        var r = window.location.search.substr(1).match(reg);
        if (r != null) {
            return unescape(r[2]);
        } else {
            return null;
        }
    };

})();
