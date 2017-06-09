/**
 * xutil.dom
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    DOM相关工具函数
 * @author:  sushuang(sushuang)
 */

(function () {
    
    var DOM = xutil.dom;
    var objProtoToString = Object.prototype.toString;
    var TRIMER_REG = new RegExp(
            "(^[\\s\\t\\xa0\\u3000]+)|([\\u3000\\xa0\\s\\t]+\x24)", "g"
        );
    var SPACE_REG = /\s/;
    var USER_AGENT = navigator.userAgent;
    var DOCUMENT = document;
    var REGEXP = RegExp;

    DOM.isStrict = DOCUMENT.compatMode == 'CSS1Compat';
    DOM.ieVersion = /msie (\d+\.\d)/i.test(USER_AGENT) 
        ? DOCUMENT.documentMode || (REGEXP.$1 - 0) : undefined;
    DOM.firefoxVersion = /firefox\/(\d+\.\d)/i.test(USER_AGENT) 
        ? REGEXP.$1 - 0 : undefined;
    DOM.operaVersion = /opera\/(\d+\.\d)/i.test(USER_AGENT) 
        ? REGEXP.$1 - 0 : undefined;
    DOM.safariVersion = /(\d+\.\d)(\.\d)?\s+safari/i.test(USER_AGENT) 
        && !/chrome/i.test(USER_AGENT) ? REGEXP.$1 - 0 : undefined;
    DOM.chromeVersion = /chrome\/(\d+\.\d+)/i.test(USER_AGENT) 
        ? + REGEXP['\x241'] : undefined;
    
    /**
     * 从文档中获取指定的DOM元素 (@see tangram)
     * 
     * @public
     * @param {(string|HTMLElement)} id 元素的id或DOM元素
     * @return {(HTMLElement|null)} 获取的元素，查找不到时返回null
     */
    DOM.g = function (id) {
        if (objProtoToString.call(id) == '[object String]') {
            return document.getElementById(id);
        } 
        else if (id && id.nodeName && (id.nodeType == 1 || id.nodeType == 9)) {
            return id;
        }
        return null;
    };
    
    /**
     * 通过className获取元素 
     * （不保证返回数组中DOM节点的顺序和文档中DOM节点的顺序一致）
     * @public
     * 
     * @param {string} className 元素的class，只能指定单一的class，
     *          如果为空字符串或者纯空白的字符串，返回空数组。
     * @param {(string|HTMLElement)} element 开始搜索的元素，默认是document。
     * @return {Array} 获取的元素集合，查找不到或className参数错误时返回空数组.
     */
    DOM.q = function (className, element) {
        var result = [];

        if (!className 
            || !(className = String(className).replace(TRIMER_REG, ''))
        ) {
            return result;
        }
        
        if (element == null) {
            element = document;
        } 
        else if (!(element = DOM.g(element))) {
            return result;
        }
        
        if (element.getElementsByClassName) {
            return element.getElementsByClassName(className);
        } 
        else {
            var elements = element.all || element.getElementsByTagName("*");
            for (var i = 0, node, clzz; node = elements[i]; i++) {
                if ((clzz = node.className) != null) {
                    var startIndex = clzz.indexOf(className);
                    var endIndex = startIndex + className.length;
                    if (startIndex >= 0
                        && (
                            clzz.charAt(startIndex - 1) == '' 
                            || SPACE_REG.test(clzz.charAt(startIndex - 1))
                        )
                        && (
                            clzz.charAt(endIndex) == '' 
                            || SPACE_REG.test(clzz.charAt(endIndex))
                        )
                    ) {
                        result[result.length] = node;
                    }
                }
            }
        }
    
        return result;
    };

    /**
     * 为 Element 对象添加新的样式。
     * 
     * @public
     * @param {HTMLElement} el Element 对象
     * @param {string} className 样式名，可以是多个，中间使用空白符分隔
     */
    DOM.addClass = function (el, className) {
        // 这里直接添加是为了提高效率，因此对于可能重复添加的属性，请使用标志位判断是否已经存在，
        // 或者先使用 removeClass 方法删除之前的样式
        el.className += ' ' + className;
    };

    /**
     * 删除 Element 对象中的样式。
     * 
     * @public
     * @param {HTMLElement} el Element 对象
     * @param {string} className 样式名，可以是多个，中间用空白符分隔
     */
    DOM.removeClass = function (el, className) {
        var oldClasses = el.className.split(/\s+/).sort();
        var newClasses = className.split(/\s+/).sort();
        var i = oldClasses.length;
        var j = newClasses.length;

        for (; i && j; ) {
            if (oldClasses[i - 1] == newClasses[j - 1]) {
                oldClasses.splice(--i, 1);
            }
            else if (oldClasses[i - 1] < newClasses[j - 1]) {
                j--;
            }
            else {
                i--;
            }
        }
        el.className = oldClasses.join(' ');
    };    

    /**
     * 是否有 样式。
     * 
     * @public
     * @param {HTMLElement} el Element 对象
     * @param {string} className 样式名，可以是多个（不可重复，多个时，都拥有才返回true），中间用空白符分隔
     */
    DOM.hasClass = function (el, className) {
        var oldClasses = el.className.split(/\s+/).sort();
        var newClasses = className.split(/\s+/).sort();
        var i = oldClasses.length;
        var j = newClasses.length;

        for (; i && j; ) {
            if (oldClasses[i - 1] == newClasses[j - 1]) {
                j--;
            }
            i--;
        }

        return j <= 0;
    };

    /**
     * 获取 Element 对象的父 Element 对象。
     * 在 IE 下，Element 对象被 removeChild 方法移除时，parentNode 仍然指向原来的父 Element 对象，
     * 并且input的parentNode可能为空。
     * 与 W3C 标准兼容的属性应该是 parentElement。
     *
     * @public
     * @param {HTMLElement} el Element 对象
     * @return {HTMLElement} 父 Element 对象，如果没有，返回 null
     */
    DOM.getParent = DOM.ieVersion 
        ? function (el) {
            return el.parentElement;
        } 
        : function (el) {
            return el.parentNode;
        };

    /**
     * 获取子节点
     *
     * @public
     * @param {HTMLElement} el Element 对象
     * @return {Array.<HTMLElement>} 子节点列表
     */
    DOM.children = function (el) {
        if (!el) { return []; }

        for (var result = [], o = el.firstChild; o; o = o.nextSibling) {
            if (o.nodeType == 1) {
                result.push(o);
            }
        }
        return result;    
    };

    /**
     * 删除
     *
     * @public
     * @param {HTMLElement} el Element 对象
     */
    DOM.remove = function (el) {
        if (el) {
            var tmpEl = DOM.getParent(el);
            tmpEl && tmpEl.removeChild(el);
        }
    };
    /**
     * 获取上一个元素
     *
     * @public
     * @param {HTMLElement} el Element 对象
     */
    DOM.getPreviousSibling = function(el) {
        return el.previousElementSibling || el.previousSibling;
    };
    /**
     * 获取下一个元素
     *
     * @public
     * @param {HTMLElement} el Element 对象
     */
    DOM.getNextSibling = function(el) {
        return el.nextElementSibling || el.nextSibling;
    };
    /**
     * 挂载事件。
     * @public
     *
     * @param {Object} obj 响应事件的对象
     * @param {string} type 事件类型
     * @param {Function} func 事件处理函数
     */
    DOM.attachEvent = DOM.ieVersion ? function (obj, type, func) {
        obj.attachEvent('on' + type, func);
    } : function (obj, type, func) {
        obj.addEventListener(type, func, false);
    };

    /**
     * 卸载事件。
     * @public
     *
     * @param {Object} obj 响应事件的对象
     * @param {string} type 事件类型
     * @param {Function} func 事件处理函数
     */
    DOM.detachEvent = DOM.ieVersion ? function (obj, type, func) {
        obj.detachEvent('on' + type, func);
    } : function (obj, type, func) {
        obj.removeEventListener(type, func, false);
    };

})();

