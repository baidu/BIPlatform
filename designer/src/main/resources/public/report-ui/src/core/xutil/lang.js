/**
 * xutil.lang
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    基本工具函数
 * @author:  sushuang(sushuang)
 * @depend:  xutil.lang, xutil.string
 */

(function () {
    /* globals xutil */
    var LANG = xutil.lang;
    var utilTrim = xutil.commonUtil.trim;
    var objProto = Object.prototype;
    var objProtoToString = objProto.toString;
    var hasOwnProperty = objProto.hasOwnProperty;
    /**
     * 判断变量是否有值
     * null或undefined时返回false。
     * 
     * @public
     * @param {*} variable 输入变量
     * @return {boolean} 判断结果
     */
    LANG.hasValue = function (variable) {
        // undefined和null返回true，其他都返回false
        return variable != null;
    };
    
    /**
     * 判断变量是否有值，且不是空白字符串
     * null或undefined时返回false。
     * 
     * @public
     * @param {*} variable 输入变量
     * @return {boolean} 判断结果
     */
    LANG.hasValueNotBlank = function (variable) {
        return LANG.hasValue(variable)
           && (!LANG.isString(variable) || utilTrim(variable) !== '');
    };

    /**
     * 判断变量是否是空白
     * 如果variable是string，则判断其是否是空字符串或者只有空白字符的字符串
     * 如果variable是Array，则判断其是否为空
     * 如果variable是Object，则判断其是否全没有直接属性（原型上的属性不计）
     * 
     * @public
     * @param {(string|Array|Object)} variable 输入变量
     * @return {boolean} 判断结果
     */    
    LANG.isBlank = function (variable) {
        if (LANG.isString(variable)) { 
            return utilTrim(variable) === '';
        } 
        else if (LANG.isArray(variable)) {
            return variable.length === 0;
        } 
        else if (LANG.isObject(variable)) {
            for (var k in variable) {
                if (hasOwnProperty.call(variable, k)) {
                    return false;   
                }
            }
            return true;
        } 
        else {
            return !!variable;
        }
    };

    /**
     * 判断变量是否为undefined
     * 
     * @public
     * @param {*} variable 输入变量
     * @return {boolean} 判断结果
     */    
    LANG.isUndefined = function (variable) {
        return typeof variable == 'undefined';
    };
    
    /**
     * 判断变量是否为null
     * 
     * @public
     * @param {*} variable 输入变量
     * @return {boolean} 判断结果
     */    
    LANG.isNull = function (variable) {
        return variable === null;
    };
    
    /**
     * 判断变量是否为number
     * NaN和Finite时也会返回true。
     * 
     * @public
     * @param {*} variable 输入变量
     * @return {boolean} 判断结果
     */    
    LANG.isNumber = function (variable) {
        return objProtoToString.call(variable) == '[object Number]';
    };
    
    /**
     * 判断变量是否为number
     * NaN和Finite时也会返回false。
     * 
     * @public
     * @param {*} variable 输入变量
     * @return {boolean} 判断结果
     */    
    LANG.isNormalNumber = function (variable) {
        return LANG.isNumber(variable) 
            && !isNaN(variable) && isFinite(variable);
    };

    /**
     * 判断变量是否为Finite
     * 
     * @public
     * @param {*} variable 输入变量
     * @return {boolean} 判断结果
     */    
    LANG.isFinite = function (variable) {
        return LANG.isNumber(variable) && isFinite(variable);
    };
    
    /**
     * 判断变量是否为NaN
     * 不同于js本身的isNaN，undefined情况不会返回true
     * 
     * @public
     * @param {*} variable 输入变量
     * @return {boolean} 判断结果
     */    
    LANG.isNaN = function (variable) {
        // NaN是唯一一个对于'==='操作符不自反的
        return variable !== variable;
    };

    /**
     * 判断变量是否为string
     * 
     * @public
     * @param {*} variable 输入变量
     * @return {boolean} 判断结果
     */    
    LANG.isString = function (variable) {
        return objProtoToString.call(variable) == '[object String]';
    };
    
    /**
     * 判断变量是否为boolean
     * 
     * @public
     * @param {*} variable 输入变量
     * @return {boolean} 判断结果
     */    
    LANG.isBoolean = function (variable) {
        return variable === true 
            || variable === false 
            || objProtoToString.call(variable) == '[object Boolean]';        
    };
    
    /**
     * 判断是否为Function
     * 
     * @public
     * @param {*} variable 输入变量
     * @return {boolean} 判断结果
     */    
    LANG.isFunction = function (variable) {
        return objProtoToString.call(variable) == '[object Function]';
    };
    
    /**
     * 判断是否为Object
     * 
     * @public
     * @param {*} variable 输入变量
     * @return {boolean} 判断结果
     */    
    LANG.isObject = function (variable) {
         return variable === Object(variable);
    };
    
    /**
     * 判断是否为Array
     * 
     * @public
     * @param {*} variable 输入变量
     * @return {boolean} 判断结果
     */    
    LANG.isArray = Array.isArray || function (variable) {
        return objProtoToString.call(variable) == '[object Array]';
    };
       
    /**
     * 判断是否为Date
     * 
     * @public
     * @param {*} variable 输入变量
     * @return {boolean} 判断结果
     */    
    LANG.isDate = function (variable) {
        return objProtoToString.call(variable) == '[object Date]';
    };  
    
    /**
     * 判断是否为RegExp
     * 
     * @public
     * @param {*} variable 输入变量
     * @return {boolean} 判断结果
     */    
    LANG.isRegExp = function (variable) {
        return objProtoToString.call(variable) == '[object RegExp]';
    };  
    
    /**
     * 判断是否为DOM Element
     * 
     * @public
     * @param {*} variable 输入变量
     * @return {boolean} 判断结果
     */
    LANG.isElement = function (variable) {
        return !!(variable && variable.nodeType == 1);
    };
      
    /**
     * 转换为number
     * 此函数一般用于string类型的数值向number类型数值的转换, 如：'123'转换为123, '44px'转换为44
     * 遵循parseFloat的法则
     * 转换失败返回默认值（从而避免转换失败后返回NaN等）。
     * 
     * @public
     * @param {*} input 要转换的东西
     * @param {*} defaultValue 转换失败时，返回此默认值。如果defaultValue为undefined则返回input本身。
     * @return {(number|*)} 转换结果。转换成功则为number；转换失败则为defaultValue
     */
    LANG.toNumber = function (input, defaultValue) {
        defaultValue = 
            typeof defaultValue != 'undefined' ? defaultValue : input;
        return isFinite(input = parseFloat(input)) ? input : defaultValue;
    };
    
    /**
     * 用于将string类型的"true"和"false"转成boolean型
     * 如果输入参数是string类型，输入参数不为"true"时均转成false。
     * 如果输入参数不是string类型，则按照js本身的强制类型转换转成boolean（从而可以应对不知道input类型的情况）。
     * 
     * @public
     * @param {(string|*)} input 要转换的东西
     * @return {boolean} 转换结果
     */
    LANG.stringToBoolean = function (input) {
        if (LANG.isString(input)) {
            return utilTrim(input) === 'true';
        } 
        else {
            return !!input; 
        }
    };

})();
