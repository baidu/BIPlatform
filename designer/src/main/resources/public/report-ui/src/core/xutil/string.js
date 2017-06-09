/**
 * xutil.string
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    字符串相关工具函数
 * @author:  sushuang(sushuang)
 * @depend:  xutil.lang
 */

(function () {
    
    var STRING = xutil.string;
    var LANG = xutil.lang;
    var TRIMER = new RegExp(
            "(^[\\s\\t\\xa0\\u3000]+)|([\\u3000\\xa0\\s\\t]+\x24)", "g"
        );
    
    /**
     * 删除目标字符串两端的空白字符 (@see tangram)
     * 
     * @pubilc
     * @param {string} source 目标字符串
     * @returns {string} 删除两端空白字符后的字符串
     */
    STRING.trim = function (source) {
        return source == null 
            ? ""
            : String(source).replace(TRIMER, "");
    };
    
    /**
     * HTML编码，包括空格也会被编码
     * 
     * @public
     * @param {string} text 要编码的文本
     * @param {number} blankLength 每个空格的长度，
     *      为了显示效果，可调整长度，缺省为1
     */
    STRING.encodeHTMLWithBlank = function (text, blankLength) {
        var blankArr=[];
        blankLength = blankLength || 1;
        for(var i = 0; i < blankLength; i++) {
            blankArr.push('&nbsp;');
        }
        return STRING.encodeHTML(text).replace(/ /g, blankArr.join(''));
    };
    
    /**
     * 对目标字符串进行html编码 (@see tangram)
     * 编码字符有5个：&<>"'
     * 
     * @public
     * @param {string} source 目标字符串
     * @returns {string} html编码后的字符串
     */
    STRING.encodeHTML = function (source) {
        return String(source)
                    .replace(/&/g,'&amp;')
                    .replace(/</g,'&lt;')
                    .replace(/>/g,'&gt;')
                    .replace(/"/g, "&quot;")
                    .replace(/'/g, "&#39;");
    };
        
    /**
     * 对目标字符串进行html解码(@see tangram)
     * 
     * @public
     * @param {string} source 目标字符串
     * @returns {string} html解码后的字符串
     */
    STRING.decodeHTML = function (source) {
        var str = String(source)
                    .replace(/&quot;/g,'"')
                    .replace(/&lt;/g,'<')
                    .replace(/&gt;/g,'>')
                    .replace(/&amp;/g, "&");
        //处理转义的中文和实体字符
        return str.replace(/&#([\d]+);/g, function (_0, _1){
            return String.fromCharCode(parseInt(_1, 10));
        });
    };

    /**
     * 解码
     * 在decodeURIComponent基础上，兼容application/x-www-form-urlencoded中额外定义的
     * "空格会被encode成加号"这个情况。
     * 解释：
     *      encodeURIComponent依据URI规范编码：
     *          参见：http://en.wikipedia.org/wiki/Application/x-www-form-urlencoded#The_application.2Fx-www-form-urlencoded_type
     *      form表单提交时是依据较为老的application/x-www-form-urlencoded类型的编码方式：
     *          参见：http://www.w3.org/TR/REC-html40-971218/interact/forms.html#h-17.13.3.3
     *          主要区别是：空格被encode成加号
     *          依照这种方式进行encode/decode的地方是：
     *              form提交时；
     *              java.net.URIEncode/java.net.URIDecoder中；
     *              （报表引擎后台的默认decode也是这样的，所以如果出现加号，会被解码成空格）；
     *          所以，把这种encode的结果传到di-stub中的情况虽然很少，但也是存在的（比如后台依照这种规范encode，并渲染到页面上）
     *
     * @public
     * @param {string} str 要解码的字符串
     * @return {string} 解码结果
     */
    STRING.decodePercent = function (str) {
        if (str == null) { return ''; }
        return decodeURIComponent(str.replace(/\+/g, '%20'));
    };    
        
    /**
     * 得到可显示的文本的方便函数，便于业务代码中批量使用
     * 
     * @public
     * @param {string} source 原文本
     * @param {string} defaultText 如果source为空，则使用defaultText，缺省为''。
     *      例如页面上表格内容为空时，显示'-'
     * @param {boolean} needEncodeHTML 是否要进行HTML编码，缺省为false
     * @param {Object} htmlEncoder HTML编码器，缺省为STRING.encodeHTML
     */
    STRING.toShowText = function (source, defaultText, needEncodeHTML, htmlEncoder) {
        defaultText =  LANG.hasValue(defaultText) ? defaultText : '';
        htmlEncoder = htmlEncoder || STRING.encodeHTML;
        var text = LANG.hasValueNotBlank(source) ? source : defaultText;
        needEncodeHTML && (text = htmlEncoder(text));
        return text;
    };
    
    /**
     * 参见toShowText
     *
     * @public
     */
    STRING.htmlText = function (source, defaultText, needEncodeHTML) {
        if (defaultText == null) {
            defaultText = '';
        }
        if (needEncodeHTML == null) {
            needEncodeHTML = true
        }
        return STRING.toShowText(source, defaultText, needEncodeHTML);
    }

    /**
     * 去除html/xml文本中的任何标签
     * （前提是文本没有被encode过）
     * 
     * @public
     * @param {string} source 输入文本
     * @return {string} 输出文本
     */
    STRING.escapeTag = function (source) {
        if (!LANG.hasValueNotBlank(source)) {
            return '';
        }
        return String(source).replace(/<.*?>/g,'');
    };
    
    /**
     * 将目标字符串中可能会影响正则表达式构造的字符串进行转义。(@see tangram)
     * 给以下字符前加上“\”进行转义：.*+?^=!:${}()|[]/\
     * 
     * @public
     * @param {string} source 目标字符串
     * @return {string} 转义后的字符串
     */
    STRING.escapeReg = function (source) {
        return String(source)
                .replace(
                    new RegExp("([.*+?^=!:\x24{}()|[\\]\/\\\\])", "g"), 
                    '\\\x241'
                );
    };    
    
    /**
     * 求字符串的字节长度，非ASCII字符算两个ASCII字符长
     * 
     * @public
     * @param {string} str 输入文本
     * @return {number} 字符串字节长度
     */
    STRING.textLength = function (str){
        if (!LANG.hasValue(str)) { return 0; };
        return str.replace(/[^\x00-\xFF]/g,'**').length;
    };

    /**
     * 截取字符串，如果非ASCII字符，
     * 算两个字节长度（一个ASCII字符长度是一个单位长度）
     * 
     * @public
     * @param {string} str 输入文本
     * @param {number} start 从第几个字符开始截取
     * @param {number} length 截取多少个字节长度
     * @return {string} 截取的字符串
     */
    STRING.textSubstr = function (str, start, length) {
        if (!LANG.hasValue(str)) {
            return '';
        }
        var count=0;
        for(var i = start, l = str.length; i < l && count < length; i++) {
            str.charCodeAt(i) > 255 ? (count += 2) : (count++);
        }
        count > length && i--;
        return str.substring(start, i); 
    };
    
    /**
     * 折行，如果非ASCII字符，算两个单位长度（一个ASCII字符长度是一个单位长度）
     * 
     * @public
     * @param {string} str 输入文本
     * @param {number} length 每行多少个单位长度
     * @param {string} lineSeparater 换行符，缺省为\r
     * @return {string} 折行过的文本
     */
    STRING.textWrap = function (str, length, lineSeparater) {
        lineSeparater = lineSeparater || '\r';
        if (length < 2)  {
            throw Error ('illegle length');
        }
        if (!LANG.hasValueNotBlank(str)) {
            return '';
        }
        
        var i = 0;
        var lineStart=0;
        var l=str.length;
        var count=0;
        var textArr=[];
        var lineStart;

        while(true) {
            if (i>=l) {
                textArr.push(str.substring(lineStart, l+1));
                break;  
            }
            str.charCodeAt(i)>255 ? (count+=2) : (count++);
            if(count>=length) {
                (count>length) && (i=i-1);
                textArr.push(str.substring(lineStart, i+1));
                lineStart = i+1;
                count = 0;
            }
            i++;
        }
        return textArr.join(lineSeparater);     
    };
 
    /**
     * 按照模板对目标字符串进行格式化 (@see tangram)
     *
     * @public
     * @usage 
     *      template('asdf#{0}fdsa#{1}8888', 'PA1', 'PA2') 
     *      返回asdfPA1fdsaPA28888。
     *      template('asdf#{name}fdsa#{area}8888, { name: 'PA1', area: 'PA2' }) 
     *      返回asdfPA1fdsaPA28888。   
     * @param {string} source 目标字符串
     * @param {(Object|...string)} options 提供相应数据的对象
     * @return {string} 格式化后的字符串
     */
    STRING.template = function (source, options) {
        source = String(source);
        var data = Array.prototype.slice.call(arguments, 1);
        var toString = Object.prototype.toString;

        if(data.length) {
            data = data.length == 1 ? 
                (options !== null && 
                    (/\[object Array\]|\[object Object\]/.test(
                        toString.call(options)
                    )) 
                        ? options : data
                ) : data;

            return source.replace(
                /#\{(.+?)\}/g, 
                function (match, key) {
                    var replacer = data[key];
                    if('[object Function]' == toString.call(replacer)) {
                        replacer = replacer(key);
                    }
                    return ('undefined' == typeof replacer ? '' : replacer);
                }
            );

        }
        return source;
    };

    /**
     * 是否以XX为结束
     * @public 
     * 
     * @param {string} str
     * @param {string} end
     * @return {boolean} 是或否
     */
    STRING.endWith = function (str, end) {
        if (str && end) {
            return str.lastIndexOf(end) 
                === str.length - end.length;
        }
        return false;
    }; 

})();
