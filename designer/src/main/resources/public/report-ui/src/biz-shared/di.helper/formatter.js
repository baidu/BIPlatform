/**
 * di.helper.Formatter
 * Copyright 2012 Baidu Inc. All rights reserved.
 * 
 * @file:    格式化集合
 * @author:  sushuang(sushuang)
 * @depend:  xutil
 */

$namespace('di.helper');
 
(function() {
    
    //--------------------------------
    // 引用
    //--------------------------------

    var xlang = xutil.lang;
    var isFunction = xlang.isFunction;
    var isArray = xlang.isArray;
    var isString = xlang.isString;
    var hasValue = xlang.hasValue;
    var hasValueNotBlank = xlang.hasValueNotBlank;
    var encodeHTML = xutil.string.encodeHTML;
    var textLength = xutil.string.textLength;
    var textSubstr = xutil.string.textSubstr;
    var formatNumber = xutil.number.formatNumber;
    var arraySlice = Array.prototype.slice;
    var DICT;

    $link(function() {
        DICT = di.config.Dict;
    });
    
    /**
     * 约定，所有formatter第一个参数是data
     * 取得formatter使用这种方式：
     * kt.helper.Formatter('SOME_FORMATTER')
     * kt.helper.Formatter('SOME_FORMATTER', true, 'asdf', ...)
     * （从第二参数起是绑定给formatter的参数）
     * formatter的this指针，即每项的对象。
     *
     * @param {string} formatterName 格式化名
     * @param {Any...} 调用formatter时的从第二个开始的参数
     * @return {Function} formatter
     */
    var FORMATTER = $namespace().Formatter = function(formatterName) {
        var args = arraySlice.call(arguments, 1);
        return function(data) {
            var argsInput = arraySlice.call(arguments, 1);
            return FORMATTER[formatterName].apply(
                this, 
                [data].concat(args, argsInput)
            );
        }
    };

    /**
     * 统一的比率格式
     */
    FORMATTER.DEFAULT_RATE_FORMAT = 'I,III.DD%';

    /**
     * 得到用于ecui表格的formatter
     * 取得formatter使用这种方式：
     * tableFormatter('SOME_FORMATTER')
     * tableFormatter('SOME_FORMATTER', true, 'asdf', ...)
     * （从第二参数起是绑定给formatter的参数）
     * 
     * @param {(string|Object)} field 数据源项的要格式化的属性名
     *              如果为Obejct，则各域为
     *              {string} data 数据属性名
     *              {string} link 链接属性名
     * @param {string} formatterName 格式化名
     * @param {Any...} 调用formatter时的从第二个开始的参数
     * @return {Function} formatter
     */
    FORMATTER.tableFormatter = function(field, formatterName) {
        var args = arraySlice.call(arguments, 2);
        var dataField; 
        var linkField;

        if (isString(field)) {
            dataField = field;
        } 
        else {
            dataField = field.data;
            linkField = field.link;
        }   

        return function(item) {
            var text = FORMATTER[formatterName].apply(
                item, 
                [item[dataField]].concat(args)
            );
            return prepareLink(item, text, linkField);
        };
    }

    /**
     * 表格中普通文本格式化，默认encodeHTML
     * 
     * @public
     * @param {Any} data 值
     * @param {string} needEncodeHTML 默认为true
     * @return {string} 显示值
     */
    FORMATTER.SIMPLE_TEXT = function(data, needEncodeHTML) {    
        needEncodeHTML = hasValue(needEncodeHTML) ? needEncodeHTML : true;
        data = hasValueNotBlank(data) ? data : '-';
        data = needEncodeHTML ? encodeHTML(data) : data;
        return data;
    }
    
    /**
     * 截断字符，用HTML的title属性显示全部字符
     * 
     * @public
     * @param {Any} data 值
     * @param {number} length 显示字节长度
     * @param {string} needEncodeHTML 默认为true
     * @param {string} color 当截断时，显示颜色，缺省则原色
     * @param {string} classNames 补充的classNames
     * @return {string} 显示值
     */
    FORMATTER.CUT_TEXT = function(
        data, length, needEncodeHTML, color, classNames
    ) {
        var shortText = '', isCut, colorStyle = '',
        needEncodeHTML = hasValue(needEncodeHTML) ? needEncodeHTML : true;
        data = hasValueNotBlank(data) ? data : '-';

        if (textLength(data) > length) {
            shortText = textSubstr(data, 0, length - 2) + '..';
            isCut = true;
        } 
        else {
            shortText = data;
            isCut = false;
        }

        shortText = needEncodeHTML ? encodeHTML(shortText) : shortText;
        if (isCut && hasValue(color)) {
            colorStyle = 'color:' + color + '';
        }
        data = needEncodeHTML ? encodeHTML(data) : data;
        return '<span class="' + classNames + '" style="' + colorStyle + '" title="' + data + '" >' + shortText + '&nbsp;</span>'; 
    }

    /**
     * 表格中比率的格式化
     * 
     * @public
     * @param {Any} data 值
     * @param {string} format 数据格式，缺省则为'I,III.DD%'
     * @return {string} 显示值
     */
    FORMATTER.SIMPLE_RATE = function(data, format) {
        var text, flagClass;
        if (!hasValueNotBlank(data)) {
            return '-';
        }
        format = format || FORMATTER.DEFAULT_RATE_FORMAT;
        text = formatNumber(data, format);
        return text;
    }

    /**
     * 表格中普通数据格式化
     * 
     * @public
     * @param {Any} data 值
     * @param {string} format 数据格式，缺省不格式化
     * @return {string} 显示值
     */
    FORMATTER.SIMPLE_NUMBER = function(data, format) {
        data = hasValueNotBlank(data) 
            ? (!format ? data : formatNumber(data, format)) 
            : '-';
        return data;
    }

    /**
     * 表格中带颜色的数据格式化（默认正数红，负数绿）
     * 
     * @public
     * @param {Any} data 值
     * @param {string} format 数据格式，缺省不格式化
     * @param {string} positiveColor 非负数的颜色，默认'red'
     * @param {string} nagetiveColor 负数的颜色，默认'green'
     * @return {string} 显示值
     */
    FORMATTER.COLORED_NUMBER = function(
        data, format, positiveColor, nagetiveColor
    ) {    
        var style, text = '-';
        positiveColor = positiveColor || 'red';
        nagetiveColor = nagetiveColor || 'green';
        if (hasValueNotBlank(data)) {
            style = 'style="color:' + (data < 0 ? nagetiveColor : positiveColor) + '" ';
            text = '<span ' + style + '>' + (!format ? data : formatNumber(data, format)) + '</span>';
        }
        return text;
    }


    /**
     * @private
     */
    function prepareLink (item, text, linkField, dontTargetBlank) {
        var href;
        if (hasValueNotBlank(linkField)) {
            href = item[linkField];
        }
        if (!hasValueNotBlank(href) || !hasValueNotBlank(text)) { 
            return text;
        }
        var targetBlank = dontTargetBlank ? '' : ' target="_blank" ';
        return '<a ' + targetBlank + ' href="' + href + '">' + text + '</a>';
    }

})();