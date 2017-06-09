//{if 0}//
(function () {
//{/if}//
//{if $phase == "define"}//

//__gzip_unitize__i
//__gzip_unitize__list
//__gzip_unitize__o
//__gzip_unitize__el
//__gzip_unitize__params
    var core = ecui = {},
        array = core.array = {},
        dom = core.dom = {},
        ext = core.ext = {},
        json = core.json = {},
        string = core.string = {},
        ui = core.ui = {},
        util = core.util = {};

    //__gzip_original__WINDOW
    ///__gzip_original__DOCUMENT
    //__gzip_original__DATE
    //__gzip_original__FUNCTION
    //__gzip_original__MATH
    //__gzip_original__REGEXP
    //__gzip_original__ABS
    //__gzip_original__CEIL
    ///__gzip_original__FLOOR
    ///__gzip_original__MAX
    ///__gzip_original__MIN
    //__gzip_original__POW
    ///__gzip_original__ROUND
    ///__gzip_original__PARSEINT
    //__gzip_original__ISNAN
    var undefined,
        WINDOW = window,
        DOCUMENT = document,
        DATE = Date,
        FUNCTION = Function,
        MATH = Math,
        REGEXP = RegExp,
        ABS = MATH.abs,
        CEIL = MATH.ceil,
        FLOOR = MATH.floor,
        MAX = MATH.max,
        MIN = MATH.min,
        POW = MATH.pow,
        ROUND = MATH.round,
        PARSEINT = parseInt,
        ISNAN = isNaN;

    var USER_AGENT = navigator.userAgent,
        isStrict = DOCUMENT.compatMode == 'CSS1Compat',
        ieVersion = dom.ieVersion = /msie (\d+\.\d)/i.test(USER_AGENT) ? DOCUMENT.documentMode || (REGEXP.$1 - 0) : undefined,
        firefoxVersion = dom.firefoxVersion = /firefox\/(\d+\.\d)/i.test(USER_AGENT) ? REGEXP.$1 - 0 : undefined,
        operaVersion = dom.operaVersion = /opera\/(\d+\.\d)/i.test(USER_AGENT) ? REGEXP.$1 - 0 : undefined,
        safariVersion = dom.safariVersion = 
            /(\d+\.\d)(\.\d)?\s+safari/i.test(USER_AGENT) && !/chrome/i.test(USER_AGENT) ? REGEXP.$1 - 0 : undefined;

    // 字符集基本操作定义
    var charset = {
            utf8: {
                byteLength: function (source) {
                    return source.replace(/[\x80-\u07ff]/g, '  ').replace(/[\u0800-\uffff]/g, '   ').length;
                },

                codeLength: function (code) {
                    return code > 2047 ? 3 : code > 127 ? 2 : 1;
                }
            },

            gbk: {
                byteLength: function (source) {
                    return source.replace(/[\x80-\uffff]/g, '  ').length;
                },

                codeLength: function (code) {
                    return code > 127 ? 2 : 1;
                }
            },

            '': {
                byteLength: function (source) {
                    return source.length;
                },

                codeLength: function (code) {
                    return 1;
                }
            }
        };

    // 读写特殊的 css 属性操作
    var styleFixer = {
            display:
                ieVersion < 8 ? {
                    get: function (el, style) {
                        return style.display == 'inline' && style.zoom == 1 ? 'inline-block' : style.display;
                    },

                    set: function (el, value) {
                        if (value == 'inline-block') {
                            value = 'inline';
                            el.style.zoom = 1;
                        }
                        el.style.display = value;
                    }
                } : firefoxVersion < 3 ? {
                    get: function (el, style) {
                        return style.display == '-moz-inline-box' ? 'inline-block' : style.display;
                    },

                    set: function (el, value) {
                        el.style.display = value == 'inline-block' ? '-moz-inline-box' : value;
                    }
                } : undefined,

            opacity:
                ieVersion ? {
                    get: function (el, style) {
                        return /alpha\(opacity=(\d+)/.test(style.filter) ? ((REGEXP.$1 - 0) / 100) + '' : '1';
                    },

                    set: function (el, value) {
                        el.style.filter =
                            el.style.filter.replace(/alpha\([^\)]*\)/gi, '') + 'alpha(opacity=' + value * 100 + ')';
                    }
                } : undefined,

            'float': ieVersion ? 'styleFloat' : 'cssFloat'
        };

        /**
         * 查询数组中指定对象的位置序号。
         * indexOf 方法返回完全匹配的对象在数组中的序号，如果在数组中找不到指定的对象，返回 -1。
         * @public
         * 
         * @param {Array} list 数组对象
         * @param {Object} obj 需要查询的对象
         * @return {number} 位置序号，不存在返回 -1
         */
    var indexOf = array.indexOf = function (list, obj) {
            for (var i = list.length; i--; ) {
                if (list[i] === obj) {
                    break;
                }
            }
            return i;
        },

        /**
         * 从数组中移除对象。
         * @public
         * 
         * @param {Array} list 数组对象
         * @param {Object} obj 需要移除的对象
         */
        remove = array.remove = function (list, obj) {
            for (var i = list.length; i--; ) {
                if (list[i] === obj) {
                    list.splice(i, 1);
                }
            }
        },

        /**
         * 为 Element 对象添加新的样式。
         * @public
         * 
         * @param {HTMLElement} el Element 对象
         * @param {string} className 样式名，可以是多个，中间使用空白符分隔
         */
        addClass = dom.addClass = function (el, className) {
            // 这里直接添加是为了提高效率，因此对于可能重复添加的属性，请使用标志位判断是否已经存在，
            // 或者先使用 removeClass 方法删除之前的样式
            el.className += ' ' + className;
        },
        /**
        * 判断 Element 对象有没有当前样式
        * @public
        *
        * @param {HTMLElement} el Element 对象
        * @param {string} className 样式名，可以是多个，中间使用空白符分隔
        */
        hasClass = dom.hasClass = function (el, className) {
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
        },

        /**
         * 获取所有 parentNode 为指定 Element 的子 Element 集合。
         * @public
         * 
         * @param {HTMLElement} el Element 对象
         * @return {Array} Element 对象数组
         */
        children = dom.children = function (el) {
            for (var result = [], o = el.firstChild; o; o = o.nextSibling) {
                if (o.nodeType == 1) {
                    result.push(o);
                }
            }
            return result;    
        },

        /**
         * 判断一个 Element 对象是否包含另一个 Element 对象。
         * contain 方法认为两个相同的 Element 对象相互包含。
         * @public
         * 
         * @param {HTMLElement} container 包含的 Element 对象
         * @param {HTMLElement} contained 被包含的 Element 对象
         * @return {boolean} contained 对象是否被包含于 container 对象的 DOM 节点上
         */
        contain = dom.contain = firefoxVersion ? function (container, contained) {
            return container == contained || !!(container.compareDocumentPosition(contained) & 16);
        } : function (container, contained) {
            return container.contains(contained);
        },

        /**
         * 创建 Element 对象。
         * @public
         * 
         * @param {string} className 样式名称
         * @param {string} cssText 样式文本
         * @param {string} tagName 标签名称，默认创建一个空的 div 对象
         * @return {HTMLElement} 创建的 Element 对象
         */
        createDom = dom.create = function (className, cssText, tagName) {
            tagName = DOCUMENT.createElement(tagName || 'DIV');
            if (className) {
                tagName.className = className;
            }
            if (cssText) {
                tagName.style.cssText = cssText;
            }
            return tagName;
        },

        /**
         * 获取 Element 对象的第一个子 Element 对象。
         * @public
         *
         * @param {HTMLElement} el Element 对象
         * @return {HTMLElement} 子 Element 对象
         */
        first = dom.first = function (el) {
            return matchNode(el.firstChild, 'nextSibling');
        },
        previous = dom.previous = function (el) {
            return matchNode(el.previousSibling, 'previousSibling');
        },
        /**
         * 获取 Element 对象的属性值。
         * 在 IE 下，Element 对象的属性可以通过名称直接访问，效率是 getAttribute 方式的两倍。
         * @public
         *
         * @param {HTMLElement} el Element 对象
         * @param {string} name 属性名称
         * @return {string} 属性值
         */
        getAttribute = dom.getAttribute = ieVersion < 8 ? function (el, name) {
            return el[name];
        } : function (el, name) {
            return el.getAttribute(name);
        },

        /**
         * 获取 Element 对象的父 Element 对象。
         * 在 IE 下，Element 对象被 removeChild 方法移除时，parentNode 仍然指向原来的父 Element 对象，与 W3C 标准兼容的属性应该是 parentElement。
         * @public
         *
         * @param {HTMLElement} el Element 对象
         * @return {HTMLElement} 父 Element 对象，如果没有，返回 null
         */
        getParent = dom.getParent = ieVersion ? function (el) {
            return el.parentElement;
        } : function (el) {
            return el.parentNode;
        },

        /**
         * 获取 Element 对象的页面位置。
         * getPosition 方法将返回指定 Element 对象的位置信息。属性如下：
         * left {number} X轴坐标
         * top  {number} Y轴坐标
         * @public
         *
         * @param {HTMLElement} el Element 对象
         * @return {Object} 位置信息
         */
        getPosition = dom.getPosition = function (el) {
            var top = 0,
                left = 0,
                body = DOCUMENT.body,
                html = getParent(body);

            if (ieVersion) {
                if(!isStrict) {
                    // 在怪异模式下，IE 将 body 的边框也算在了偏移值中，需要先纠正
                    o = getStyle(body);
                    if (ISNAN(top = PARSEINT(o.borderTopWidth))) {
                        top = -2;
                    }
                    if (ISNAN(left = PARSEINT(o.borderLeftWidth))) {
                        left = -2;
                    }
                }

                o = el.getBoundingClientRect();
                top += html.scrollTop + body.scrollTop - html.clientTop + FLOOR(o.top);
                left += html.scrollLeft + body.scrollLeft - html.clientLeft + FLOOR(o.left);
            }
            else if (el == body) {
                top = html.scrollTop + body.scrollTop;
                left = html.scrollLeft + body.scrollLeft;
            }
            else if (el != html) {
                for (o = el; o; o = o.offsetParent) {
                    top += o.offsetTop;
                    left += o.offsetLeft;
                }

                if (operaVersion || (/webkit/i.test(USER_AGENT) && getStyle(el, 'position') == 'absolute')) {
                    top -= body.offsetTop;
                }

                for (var o = getParent(el), style = getStyle(el); o != body; o = getParent(o), style = el) {
                    left -= o.scrollLeft;
                    if (!operaVersion) {
                        el = getStyle(o);
                        // 以下使用 html 作为临时变量
                        html = firefoxVersion && el.overflow != 'visible' && style.position == 'absolute' ? 2 : 1;
                        top += toNumber(el.borderTopWidth) * html - o.scrollTop;
                        left += toNumber(el.borderLeftWidth) * html;
                    }
                    else if (o.tagName != 'TR') {
                        top -= o.scrollTop;
                    }
                }
            }

            return {top: top, left: left};
        },

        /**
         * 获取 Element 对象的 CssStyle 对象或者是指定的样式值。
         * getStyle 方法如果不指定样式名称，将返回 Element 对象的当前 CssStyle 对象。
         * @public
         *
         * @param {HTMLElement} el Element 对象
         * @param {string} name 样式名称
         * @return {CssStyle|Object} CssStyle 对象或样式值
         */
        getStyle = dom.getStyle = function (el, name) {
            var fixer = styleFixer[name],
                style = el.currentStyle || (ieVersion ? el.style : getComputedStyle(el, null));

            return name ? fixer && fixer.get ? fixer.get(el, style) : style[fixer || name] : style;
        },

        /**
         * 获取 Element 对象的文本。
         * @public
         *
         * @param {HTMLElement} el Element 对象
         * @return {string} Element 对象的文本
         */
        getText = dom.getText = firefoxVersion ? function (el) {
            return el.textContent;
        } : function (el) {
            return el.innerText;
        },

        /**
         * 将 Element 对象插入指定的 Element 对象之后。
         * 如果指定的 Element 对象没有父 Element 对象，相当于 remove 操作。
         * @public
         *
         * @param {HTMLElement} el 被插入的 Element 对象
         * @param {HTMLElement} target 目标 Element 对象
         * @return {HTMLElement} 被插入的 Element 对象
         */
        insertAfter = dom.insertAfter = function (el, target) {
            var parent = getParent(target);
            return parent ? parent.insertBefore(el, target.nextSibling) : removeDom(el);
        },

        /**
         * 将 Element 对象插入指定的 Element 对象之前。
         * 如果指定的 Element 对象没有父 Element 对象，相当于 remove 操作。
         * @public
         *
         * @param {HTMLElement} el 被插入的 Element 对象
         * @param {HTMLElement} target 目标 Element 对象
         * @return {HTMLElement} 被插入的 Element 对象
         */
        insertBefore = dom.insertBefore = function (el, target) {
            var parent = getParent(target);
            return parent ? parent.insertBefore(el, target) : removeDom(el);
        },

        /**
         * 向指定的 Element 对象内插入一段 html 代码。
         * @public
         * 
         * @param {HTMLElement} el Element 对象
         * @param {string} position 插入 html 的位置信息，取值为 beforeBegin,afterBegin,beforeEnd,afterEnd
         * @param {string} html 要插入的 html 代码
         */
        insertHTML = dom.insertHTML = firefoxVersion ? function (el, position, html) {
            var name = {
                    AFTERBEGIN: 'selectNodeContents',
                    BEFOREEND: 'selectNodeContents',
                    BEFOREBEGIN: 'setStartBefore',
                    AFTEREND: 'setEndAfter'
                }[position.toUpperCase()],
                range = DOCUMENT.createRange();

            range[name](el);
            range.collapse(position.length > 9);
            range.insertNode(range.createContextualFragment(html));
        } : function (el, position, html) {
            el.insertAdjacentHTML(position, html);
        },

        /**
         * 获取 Element 对象的最后一个子 Element 对象。
         * @public
         *
         * @param {HTMLElement} el Element 对象
         * @return {HTMLElement} 子 Element 对象
         */
        last = dom.last = function (el) {
            return matchNode(el.lastChild, 'previousSibling');
        },

        /**
         * 将指定的 Element 对象的内容移动到目标 Element 对象中。
         * @public
         *
         * @param {HTMLElement} source 指定的 Element 对象
         * @param {HTMLElement} target 目标 Element 对象
         * @param {boolean} all 是否移动所有的 DOM 对象，默认仅移动 ElementNode 类型的对象
         */
        moveElements = dom.moveElements = function (source, target, all) {
            //__transform__el_o
            for (var el = source.firstChild; el; el = source) {
                source = el.nextSibling;
                if (all || el.nodeType == 1) {
                    target.appendChild(el);
                }
            }
        },

        /**
         * 获取 Element 对象的下一个 Element 对象。
         * @public
         *
         * @param {HTMLElement} el Element 对象
         * @return {HTMLElement} Element 对象
         */
        next = dom.next = function (el) {
            return matchNode(el.nextSibling, 'nextSibling');
        },

        /**
         * 从页面中移除 Element 对象。
         * @public
         * 
         * @param {HTMLElement} el Element 对象
         * @return {HTMLElement} 被移除的 Element 对象
         */
        removeDom = dom.remove = function (el) {
            var parent = getParent(el);
            if (parent) {
                parent.removeChild(el);
            }
            return el;
        },

        /**
         * 删除 Element 对象中的样式。
         * @public
         * 
         * @param {HTMLElement} el Element 对象
         * @param {string} className 样式名，可以是多个，中间用空白符分隔
         */
        removeClass = dom.removeClass = function (el, className) {
            var oldClasses = el.className.split(/\s+/).sort(),
                newClasses = className.split(/\s+/).sort(),
                i = oldClasses.length,
                j = newClasses.length;

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
        },

        /**
         * 设置输入框的表单项属性。
         * 如果没有指定一个表单项，setInput 方法将创建一个表单项。
         * @public
         *
         * @param {HTMLElement} el InputElement 对象
         * @param {string} name 新的表单项名称，默认与 el 相同
         * @param {string} type 新的表单项类型，默认为 el 相同
         * @return {HTMLElement} 设置后的 InputElement 对象
         */
        setInput = dom.setInput = function (el, name, type) {
            if (!el) {
                if (type == 'textarea') {
                    el = createDom('', '', 'textarea');
                }
                else {
                    if (ieVersion < 9) {
                        return createDom('', '', '<input type="' + (type || '') + '" name="' + (name || '') + '">');
                    }
                    el = createDom('', '', 'input');
                }
            }

            name = name === undefined ? el.name : name;
            type = type === undefined ? el.type : type;

            if (el.name != name || el.type != type) {
                if ((ieVersion && type != 'textarea') ||
                        el.type != type && (el.type == 'textarea' || type == 'textarea')) {
                    insertHTML(
                        el,
                        'AFTEREND',
                        '<' + (type == 'textarea' ? 'textarea' : 'input type="' + type + '"') +
                            ' name="' + name + '" class="' + el.className +
                            '" style="' + el.style.cssText + '" ' + (el.disabled ? 'disabled' : '') +
                            (el.readOnly ? ' readOnly' : '') + '>'
                    );
                    name = el;
                    (el = el.nextSibling).value = name.value;
                    if (type == 'radio') {
                        el.checked = name.checked;
                    }
                    removeDom(name);
                }
                else {
                    el.type = type;
                    el.name = name;
                }
            }
            return el;
        },
            /**
             * 根据样式获取Element
             * @public
             *
             * @param {HTMLElement} parent Element 对象
             * @param {string} tagName 标签
             * @param {string} className 样式
             */
        getElementsByClass = dom.getElementsByClass = function(parent, tagName, className) {
            var oEls = parent.getElementsByTagName(tagName);
            var arr = [];
            for (var i = 0; i < oEls.length; i++) {
                var oClassName = oEls[i].className.split(" ");
                for (var j = 0; j < oClassName.length; j++) {
                    if (className == oClassName[j]) {
                        arr.push(oEls[i]);
                        break;
                    }
                }
            }
            return arr;
        },
        /**
         * 设置 Element 对象的样式值。
         * @public
         *
         * @param {HTMLElement} el Element 对象
         * @param {string} name 样式名称
         * @param {string} value 样式值
         */
        setStyle = dom.setStyle = function (el, name, value) {
            var fixer = styleFixer[name];
            if (fixer && fixer.set) {
                fixer.set(el, value);
            }
            else {
                el.style[fixer || name] = value;
            }
        },

        /**
         * 设置 Element 对象的文本。
         * @public
         *
         * @param {HTMLElement} el Element 对象
         * @param {string} text Element 对象的文本
         */
        setText = dom.setText = firefoxVersion ? function (el, text) {
            el.textContent = text;
        } : function (el, text) {
            el.innerText = text;
        },

        /**
         * JSON字串解析，将JSON字符串解析为JSON对象。
         * @public
         *
         * @param {string} text json字符串
         * @return {Object} json字符串描述的对象
         */
        parse = json.parse = function (text) {
            return new Function('return (' + text + ')')();
        },

        /**
         * JSON对象序列化。
         * @public
         *
         * @param {Object} source 需要序列化的对象
         * @return {string} json字符串
         */
        stringify = json.stringify = (function () {
//__gzip_unitize__result
//__gzip_unitize__source
            var escapeMap = {
                    '\b': '\\b',
                    '\t': '\\t',
                    '\n': '\\n',
                    '\f': '\\f',
                    '\r': '\\r',
                    '"' : '\\"',
                    '\\': '\\\\'
                };

            /**
             * 字符串序列化。
             * @private
             *
             * @param {string} source 需要序列化的字符串
             */
            function encodeString(source) {
                if (/["\\\x00-\x1f]/.test(source)) {
                    source = source.replace(
                        /["\\\x00-\x1f]/g,
                        function (match) {
                            var o = escapeMap[match];
                            if (o) {
                                return o;
                            }
                            o = match.charCodeAt();
                            return '\\u00' + FLOOR(o / 16) + (o % 16).toString(16);
                        }
                    );
                }
                return '"' + source + '"';
            }

            /**
             * 数组序列化。
             * @private
             *
             * @param {Array} source 需要序列化的数组
             */
            function encodeArray(source) {
                var i = 0,
                    result = [],
                    o,
                    l = source.length;

                for (var i = 0, result = [], o, l = source.length; i < l; i++) {
                    if ((o = stringify(source[i])) !== undefined) {
                        result.push(o);
                    }
                }
                return '[' + result.join(',') + ']';
            }

            /**
             * 处理日期序列化时的补零。
             * @private
             *
             * @param {number} source 数值，小于10需要补零
             */
            function pad(source) {
                return source < 10 ? '0' + source : source;
            }

            /**
             * 日期序列化。
             * @private
             *
             * @param {Date} source 需要序列化的日期
             */
            function encodeDate(source) {
                return '"' + source.getFullYear() + '-' + pad(source.getMonth() + 1) + '-' +
                        pad(source.getDate()) + 'T' + pad(source.getHours()) + ':' +
                        pad(source.getMinutes()) + ':' + pad(source.getSeconds()) + '"';
            }

            return function (source) {
                switch (typeof source) {
                case 'undefined':
                case 'function':
                case 'unknown':
                    return undefined;
                case 'number':
                    if (!isFinite(source)) {
                        return 'null';
                    }
                    // 对于有意义的数值与布尔类型直接输出
                case 'boolean':
                    return source.toString();
                case 'string':
                    return encodeString(source);
                default:
                    if (source === null) {
                        return 'null';
                    }
                    else if (source instanceof Array) {
                        return encodeArray(source);
                    }
                    else if (source instanceof Date) {
                        return encodeDate(source);
                    }
                    else {
                        var result = [],
                            o;

                        for (var i in source) {
                            if ((o = stringify(source[i])) !== undefined) {
                                result.push(encodeString(i) + ':' + o);
                            }
                        }

                        return '{' + result.join(',') + '}';
                    }
                }
            };
        })(),

        /**
         * 对目标字符串进行 html 解码。
         * @public
         *
         * @param {string} source 目标字符串
         * @return {string} 结果字符串
         */
        decodeHTML = string.decodeHTML = (function () {
            var codeTable = {
                quot: '"',
                lt: '<',
                gt: '>',
                amp: '&'
            };

            return function (source) {
                //处理转义的中文和实体字符
                return source.replace(/&(quot|lt|gt|amp|#([\d]+));/g, function(match, $1, $2) {
                    return codeTable[$1] || String.fromCharCode(+$2);
                });
            };
        })(),

        /**
         * 对目标字符串进行 html 编码。
         * encodeHTML 方法对四个字符进行编码，分别是 &<>"
         * @public
         *
         * @param {string} source 目标字符串
         * @return {string} 结果字符串
         */
        encodeHTML = string.encodeHTML = function (source) {
            return source.replace(/[&<>"']/g, function (c) {
                return '&#' + c.charCodeAt(0) + ';';
            });
        },

        /**
         * 计算字符串的字节长度。
         * 如果没有指定编码集，相当于获取字符串属性 length 的值。
         * 
         * @param {string} source 目标字符串
         * @param {string} charsetName 字符对应的编码集
         * @return {number} 字节长度
         */
        getByteLength = string.getByteLength = function (source, charsetName) {
            return charset[charsetName || ''].byteLength(source);
        },

        /**
         * 根据字节长度截取字符串。
         * 如果没有指定编码集，相当于字符串的 slice 方法。
         * 
         * @param {string} source 目标字符串
         * @param {number} length 需要截取的字节长度
         * @param {string} charsetName 字符对应的编码集
         * @return {string} 结果字符串
         */
        sliceByte = string.sliceByte = function (source, length, charsetName) {
            for (var i = 0, func = charset[charsetName || ''].codeLength; i < source.length; i++) {
                length -= func(source.charCodeAt(i));
                if (length < 0) {
                    return source.slice(0, i);
                }
            }

            return source;
        },

        /**
         * 驼峰命名法转换。
         * toCamelCase 方法将 xxx-xxx 字符串转换成 xxxXxx。
         * @public
         *
         * @param {string} source 目标字符串
         * @return {string} 结果字符串
         */
        toCamelCase = string.toCamelCase = function (source) {
            if (source.indexOf('-') < 0) {
                return source;
            }
            return source.replace(/\-./g, function (match) {
                return match.charAt(1).toUpperCase();
            });
        },

        /**
         * 将目标字符串中常见全角字符转换成半角字符。
         * 
         * @param {string} source 目标字符串
         * @return {string} 结果字符串
         */
        toHalfWidth = string.toHalfWidth = function (source) {
            return source.replace(/[\u3000\uFF01-\uFF5E]/g, function (c) {
                return String.fromCharCode(MAX(c.charCodeAt(0) - 65248, 32));
            });
        },

        /**
         * 过滤字符串两端的空白字符。
         * @public
         *
         * @param {string} source 目标字符串
         * @return {string} 结果字符串
         */
        trim = string.trim = function (source) {
            return source && source.replace(/^\s+|\s+$/g, '');
        },

        /**
         * 日期格式化。
         * @public
         *
         * @param {Date} source 日期对象
         * @param {string} pattern 日期格式描述字符串
         * @return {string} 结果字符串
         */
        formatDate = string.formatDate = function (source, pattern) {
            var year = source.getFullYear(),
                month = source.getMonth() + 1,
                date = source.getDate(),
                hours = source.getHours(),
                minutes = source.getMinutes(),
                seconds = source.getSeconds();

            return pattern.replace(/(y+|M+|d+|H+|h+|m+|s+)/g, function (match) {
                var length = match.length;
                switch (match.charAt()) {
                case 'y':
                    return length > 2 ? year : year.toString().slice(2);
                case 'M':
                    match = month;
                    break;
                case 'd':
                    match = date;
                    break;
                case 'H':
                    match = hours;
                    break;
                case 'h':
                    match = hours % 12;
                    break;
                case 'm':
                    match = minutes;
                    break;
                case 's':
                    match = seconds;
                }
                return length > 1 && match < 10 ? '0' + match : match;
            });
        },

        /**
         * 挂载事件。
         * @public
         *
         * @param {Object} obj 响应事件的对象
         * @param {string} type 事件类型
         * @param {Function} func 事件处理函数
         */
        attachEvent = util.attachEvent = ieVersion ? function (obj, type, func) {
            obj.attachEvent('on' + type, func);
        } : function (obj, type, func) {
            obj.addEventListener(type, func, false);
        },

        /*
         * 空函数。
         * blank 方法不应该被执行，也不进行任何处理，它用于提供给不需要执行操作的事件方法进行赋值，与 blank 类似的用于给事件方法进行赋值，而不直接被执行的方法还有 cancel。
         * @public
         */
        blank = util.blank = function () {
        },

        /**
         * 调用指定对象超类的指定方法。
         * callSuper 用于不确定超类类型时的访问，例如接口内定义的方法。请注意，接口不允许被子类实现两次，否则将会引发死循环。
         * @public
         *
         * @param {Object} object 需要操作的对象
         * @param {string} name 方法名称
         * @return {Object} 超类方法的返回值
         */
        callSuper = util.callSuper = function (object, name) {
            /**
             * 查找指定的方法对应的超类方法。
             * @private
             *
             * @param {Object} clazz 查找的起始类对象
             * @param {Function} caller 基准方法，即查找 caller 对应的超类方法
             * @return {Function} 基准方法对应的超类方法，没有找到基准方法返回 undefined，基准方法没有超类方法返回 null
             */
            function findPrototype(clazz, caller) {
                for (; clazz; clazz = clazz.constructor.superClass) {
                    if (clazz[name] == caller) {
                        for (; clazz = clazz.constructor.superClass; ) {
                            if (clazz[name] != caller) {
                                return clazz[name];
                            }
                        }
                        return null;
                    }
                }
            }

            //__gzip_original__clazz
            var clazz = object.constructor.prototype,
                caller = callSuper.caller,
                func = findPrototype(clazz, caller);

            if (func === undefined) {
                // 如果Items的方法直接位于prototype链上，是caller，如果是间接被别的方法调用Items.xxx.call，是caller.caller
                func = findPrototype(clazz, caller.caller);
            }

            if (func) {
                return func.apply(object, caller.arguments);
            }
        },

        /*
         * 返回 false。
         * cancel 方法不应该被执行，它每次返回 false，用于提供给需要返回逻辑假操作的事件方法进行赋值，例如需要取消默认事件操作的情况，与 cancel 类似的用于给事件方法进行赋值，而不直接被执行的方法还有 blank。
         * @public
         *
         * @return {boolean} false
         */
        cancel = util.cancel = function () {
            return false;
        },

        /**
         * 卸载事件。
         * @public
         *
         * @param {Object} obj 响应事件的对象
         * @param {string} type 事件类型
         * @param {Function} func 事件处理函数
         */
        detachEvent = util.detachEvent = ieVersion ? function (obj, type, func) {
            obj.detachEvent('on' + type, func);
        } : function (obj, type, func) {
            obj.removeEventListener(type, func, false);
        },

        /**
         * 对象属性复制。
         * @public
         *
         * @param {Object} target 目标对象
         * @param {Object} source 源对象
         * @return {Object} 目标对象
         */
        extend = util.extend = function (target, source) {
            for (var key in source) {
                target[key] = source[key];
            }
            return target;
        },

        /**
         * 获取浏览器可视区域的相关信息。
         * getView 方法将返回浏览器可视区域的信息。属性如下：
         * top        {number} 可视区域最小X轴坐标
         * right      {number} 可视区域最大Y轴坐标
         * bottom     {number} 可视区域最大X轴坐标
         * left       {number} 可视区域最小Y轴坐标
         * width      {number} 可视区域的宽度
         * height     {number} 可视区域的高度
         * pageWidth  {number} 页面的宽度
         * pageHeight {number} 页面的高度
         * @public
         *
         * @return {Object} 浏览器可视区域信息
         */
        getView = util.getView = function () {
            //__gzip_original__clientWidth
            //__gzip_original__clientHeight
            var body = DOCUMENT.body,
                html = getParent(body),
                client = isStrict ? html : body,
                scrollTop = html.scrollTop + body.scrollTop,
                scrollLeft = html.scrollLeft + body.scrollLeft,
                clientWidth = client.clientWidth,
                clientHeight = client.clientHeight;

            return {
                top: scrollTop,
                right: scrollLeft + clientWidth,
                bottom: scrollTop + clientHeight,
                left: scrollLeft,
                width: clientWidth,
                height: clientHeight,
                pageWidth: MAX(html.scrollWidth, body.scrollWidth, clientWidth),
                pageHeight: MAX(html.scrollHeight, body.scrollHeight, clientHeight)
            };
        },

        /**
         * 类继承。
         * @public
         *
         * @param {Function} subClass 子类
         * @param {Function} superClass 父类
         * @return {Object} subClass 的 prototype 属性
         */
        inherits = util.inherits = function (subClass, superClass) {
            var oldPrototype = subClass.prototype,
                clazz = new FUNCTION();
                
            clazz.prototype = superClass.prototype;
            extend(subClass.prototype = new clazz(), oldPrototype);
            subClass.prototype.constructor = subClass;
            subClass.superClass = superClass.prototype;

            return subClass.prototype;
        },

        /**
         * 设置缺省的属性值。
         * 如果对象的属性已经被设置，setDefault 方法不进行任何处理，否则将默认值设置到指定的属性上。
         * @public
         *
         * @param {Object} obj 被设置的对象
         * @param {string} key 属性名
         * @param {Object} value 属性的默认值
         */
        setDefault = util.setDefault = function (obj, key, value) {
            if (!obj.hasOwnProperty(key)) {
                obj[key] = value;
            }
        },

        /**
         * 创建一个定时器对象。
         * @public
         *
         * @param {Function} func 定时器需要调用的函数
         * @param {number} delay 定时器延迟调用的毫秒数，如果为负数表示需要连续触发
         * @param {Object} caller 调用者，在 func 被执行时，this 指针指向的对象，可以为空
         * @param {Object} ... 向 func 传递的参数
         * @return {Function} 用于关闭定时器的方法
         */
        timer = util.timer = function (func, delay, caller) {
            function build() {
                return (delay < 0 ? setInterval : setTimeout)(function () {
                    func.apply(caller, args);
                    // 使用delay<0而不是delay>=0，是防止delay没有值的时候，不进入分支
                    if (!(delay < 0)) {
                        func = caller = args = null;
                    }
                }, ABS(delay));
            }

            var args = Array.prototype.slice.call(arguments, 3),
                handle = build(),
                pausing;

            /**
             * 中止定时调用。
             * @public
             *
             * @param {boolean} pause 是否暂时停止定时器，如果参数是 true，再次调用函数并传入参数 true 恢复运行。
             */
            return function (pause) {
                (delay < 0 ? clearInterval : clearTimeout)(handle);
                if (pause) {
                    if (pausing) {
                        handle = build();
                    }
                    pausing = !pausing;
                }
                else {
                    func = caller = args = null;
                }
            };
        },

        /**
         * 将对象转换成数值。
         * toNumber 方法会省略数值的符号，例如字符串 9px 将当成数值的 9，不能识别的数值将默认为 0。
         * @public
         *
         * @param {Object} obj 需要转换的对象
         * @return {number} 对象的数值
         */
        toNumber = util.toNumber = function (obj) {
            return PARSEINT(obj) || 0;
        },

        /**
         * 设置页面加载完毕后自动执行的方法。
         * @public
         *
         * @param {Function} func 需要自动执行的方法
         */
        ready = dom.ready = (function () {
            var hasReady = false,
                list = [],
                check,
                numStyles;

            function ready() {
                if (!hasReady) {
                    hasReady = true;
                    for (var i = 0, o; o = list[i++]; ) {
                        o();
                    }
                }
            }

            if (DOCUMENT.addEventListener && !operaVersion) {
                DOCUMENT.addEventListener('DOMContentLoaded', ready, false);
            }
            else if (ieVersion && WINDOW == top) {
                check = function () {
                    try {
                        DOCUMENT.documentElement.doScroll('left');
                        ready();
                    }
                    catch (e) {
                        timer(check, 0);
                    }
                };
            }
            else if (safariVersion) {
                check = function () {
                    var i = 0,
                        list,
                        o = DOCUMENT.readyState;

                    if (o != 'loaded' && o != 'complete') {
                        timer(check, 0);
                    }
                    else {
                        if (numStyles === undefined) {
                            numStyles = 0;
                            if (list = DOCUMENT.getElementsByTagName('style')) {
                                numStyles += list.length;
                            }
                            if (list = DOCUMENT.getElementsByTagName('link')) {
                                for (; o = list[i++]; ) {
                                    if (getAttribute(o, 'rel') == 'stylesheet') {
                                        numStyles++;
                                    }
                                }
                            }
                        }
                        if (DOCUMENT.styleSheets.length != numStyles) {
                            timer(check, 0);
                        }
                        else {
                            ready();
                        }
                    }
                };
            }

            if (check) {
                check();
            }

            attachEvent(WINDOW, 'load', ready);

            return function (func) {
                if (hasReady) {
                    func();
                }
                else {
                    list.push(func);
                }
            };
        })();
//{else}//
    /**
     * 获取 Element 对象指定位置的 Element 对象。
     * @private
     *
     * @param {HTMLElement} el Element 对象
     * @param {string} direction Element 对象遍历的属性
     * @return {HTMLElement} 指定位置的 Element 对象
     */
    function matchNode(el, direction) {
        for (; el; el = el[direction]) {
            if (el.nodeType == 1) {
                break;
            }
        }
        return el;
    }

    try {
        DOCUMENT.execCommand("BackgroundImageCache", false, true);
    }
    catch (e) {
    }
//{/if}//
//{if 0}//
})();
//{/if}//
