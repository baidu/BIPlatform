/*
Panel - 定义在一个小区域内截取显示大区域内容的基本操作。
截面控件，继承自基础控件，用于显示实际的内容区域超过控件显示区域的信息，通过拖拽滚动条显示完整的内容，截面控件可以设置参数决定是否自动显示水平/垂直滚动条，如果设置不显示水平/垂直滚动条，水平/垂直内容超出的部分将直接被截断，当设置两个滚动条都不显示时，截面控件从显示效果上等同于基础控件。在截面控件上滚动鼠标滑轮，将控制截面控件往垂直方向(如果没有垂直滚动条则在水平方向)前移或者后移滚动条，在获得焦点后，通过键盘的方向键也可以操作截面控件的滚动条。

截面控件直接HTML初始化的例子:
<div ecui="type:panel">
  <!-- 这里放内容 -->
  ...
</div>

属性
_bAbsolute           - 是否包含绝对定位的Element
_nWheelDelta         - 鼠标滚轮滚动一次的差值
_eBrowser            - 用于浏览器原生的滚动条实现的Element
_uVScrollbar         - 垂直滚动条控件
_uHScrollbar         - 水平滚动条控件
_uCorner             - 夹角控件
$$mainWidth          - layout区域的实际宽度
$$mainHeight         - layout区域的实际高度
*/
//{if 0}//
(function () {

    var core = ecui,
        dom = core.dom,
        ui = core.ui,
        util = core.util,

        MATH = Math,
        MAX = MATH.max,
        MIN = MATH.min,
        FLOOR = MATH.floor,

        createDom = dom.create,
        getParent = dom.getParent,
        getPosition = dom.getPosition,
        getStyle = dom.getStyle,
        moveElements = dom.moveElements,
        attachEvent = util.attachEvent,
        blank = util.blank,
        detachEvent = util.detachEvent,
        toNumber = util.toNumber,

        $fastCreate = core.$fastCreate,
        calcHeightRevise = core.calcHeightRevise,
        calcWidthRevise = core.calcWidthRevise,
        findControl = core.findControl,
        getKey = core.getKey,
        getScrollNarrow = core.getScrollNarrow,
        inheritsControl = core.inherits,
        triggerEvent = core.triggerEvent,
        wrapEvent = core.wrapEvent,

        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = UI_CONTROL.prototype,
        UI_VSCROLLBAR = ui.VScrollbar,
        UI_HSCROLLBAR = ui.HScrollbar;
//{/if}//
//{if $phase == "define"}//
    /**
     * 初始化浏览器原生滚动条控件。
     * @protected
     *
     * @param {Object} options 初始化选项
     */
    ///__gzip_original__UI_BROWSER_SCROLLBAR
    ///__gzip_original__UI_BROWSER_SCROLLBAR_CLASS
    ///__gzip_original__UI_BROWSER_VSCROLLBAR
    ///__gzip_original__UI_BROWSER_VSCROLLBAR_CLASS
    ///__gzip_original__UI_BROWSER_HSCROLLBAR
    ///__gzip_original__UI_BROWSER_HSCROLLBAR_CLASS
    ///__gzip_original__UI_BROWSER_CORNER
    ///__gzip_original__UI_BROWSER_CORNER_CLASS
    ///__gzip_original__UI_PANEL
    ///__gzip_original__UI_PANEL_CLASS
    var UI_BROWSER_SCROLLBAR =
        inheritsControl(
            UI_CONTROL,
            null,
            null,
            function (el, options) {
                detachEvent(el, 'scroll', UI_BROWSER_SCROLLBAR_SCROLL);
                attachEvent(el, 'scroll', UI_BROWSER_SCROLLBAR_SCROLL);
            }
        ),
        UI_BROWSER_SCROLLBAR_CLASS = UI_BROWSER_SCROLLBAR.prototype;
//{else}//
    /**
     * 原生滚动条滚动处理。
     * 滚动条滚动后，将触发父控件的 onscroll 事件，如果事件返回值不为 false，则调用父控件的 $scroll 方法。
     * @private
     *
     * @param {ecui.ui.Event} event 事件对象
     */
    function UI_BROWSER_SCROLLBAR_SCROLL(event) {
        triggerEvent(findControl(getParent(wrapEvent(event).target)), 'scroll');
    }

    /**
     * @override
     */
    UI_BROWSER_SCROLLBAR_CLASS.$hide = UI_BROWSER_SCROLLBAR_CLASS.hide = function () {
        this.getMain().style[this._aProperty[0]] = 'hidden';
        UI_BROWSER_SCROLLBAR_CLASS.setValue.call(this, 0);
    };

    /**
     * 直接设置控件的当前值。
     * @protected
     *
     * @param {number} value 控件的当前值
     */
    UI_BROWSER_SCROLLBAR_CLASS.$setValue = function (value) {
        this.getMain()[this._aProperty[1]] = MIN(MAX(0, value), this.getTotal());
    };

    /**
     * @override
     */
    UI_BROWSER_SCROLLBAR_CLASS.$show = UI_BROWSER_SCROLLBAR_CLASS.show = function () {
        this.getMain().style[this._aProperty[0]] = 'scroll';
    };

    /**
     * @override
     */
    UI_BROWSER_SCROLLBAR_CLASS.getHeight = function () {
        return this._aProperty[4] ? this.getMain()[this._aProperty[4]] : getScrollNarrow();
    };

    /**
     * 获取滚动条控件的最大值。
     * getTotal 方法返回滚动条控件允许滚动的最大值，最大值、当前值与滑动块控件的实际位置互相影响，通过 setTotal 设置。
     * @public
     *
     * @return {number} 控件的最大值
     */
    UI_BROWSER_SCROLLBAR_CLASS.getTotal = function () {
        return toNumber(this.getMain().lastChild.style[this._aProperty[2]]);
    };

    /**
     * 获取滚动条控件的当前值。
     * getValue 方法返回滚动条控件的当前值，最大值、当前值与滑动按钮控件的实际位置互相影响，但是当前值不允许超过最大值，通过 setValue 方法设置。
     * @public
     *
     * @return {number} 滚动条控件的当前值
     */
    UI_BROWSER_SCROLLBAR_CLASS.getValue = function () {
        return this.getMain()[this._aProperty[1]];
    };

    /**
     * @override
     */
    UI_BROWSER_SCROLLBAR_CLASS.getWidth = function () {
        return this._aProperty[3] ? this.getMain()[this._aProperty[3]] : getScrollNarrow();
    };

    /**
     * @override
     */
    UI_BROWSER_SCROLLBAR_CLASS.isShow = function () {
        return this.getMain().style[this._aProperty[0]] != 'hidden';
    };

    /**
     * 设置滚动条控件的最大值。
     * setTotal 方法设置的值不能为负数，当前值如果大于最大值，设置当前值为新的最大值，最大值发生改变将导致滑动按钮刷新。
     * @public
     *
     * @param {number} value 控件的最大值
     */
    UI_BROWSER_SCROLLBAR_CLASS.setTotal = function (value) {
        this.getMain().lastChild.style[this._aProperty[2]] = value + 'px';
    };

    /**
     * 设置滚动条控件的当前值。
     * @public
     *
     * @param {number} value 控件的当前值
     */
    UI_BROWSER_SCROLLBAR_CLASS.setValue = function (value) {
        this.$setValue(value);
        triggerEvent(this.getParent(), 'scroll');
    };

    UI_BROWSER_SCROLLBAR_CLASS.$cache =
        UI_BROWSER_SCROLLBAR_CLASS.$getPageStep = UI_BROWSER_SCROLLBAR_CLASS.$setPageStep =
        UI_BROWSER_SCROLLBAR_CLASS.$setSize = UI_BROWSER_SCROLLBAR_CLASS.alterClass =
        UI_BROWSER_SCROLLBAR_CLASS.cache = UI_BROWSER_SCROLLBAR_CLASS.getStep =
        UI_BROWSER_SCROLLBAR_CLASS.init = UI_BROWSER_SCROLLBAR_CLASS.setPosition =
        UI_BROWSER_SCROLLBAR_CLASS.setStep = UI_BROWSER_SCROLLBAR_CLASS.skip = blank;
//{/if}//
//{if $phase == "define"}//
    /**
     * 初始化浏览器原生垂直滚动条控件。
     * @public
     *
     * @param {Object} options 初始化选项
     */
    var UI_BROWSER_VSCROLLBAR =
        inheritsControl(
            UI_BROWSER_SCROLLBAR,
            null,
            null,
            function (el, options) {
                this._aProperty = ['overflowY', 'scrollTop', 'height', null, 'offsetHeight'];
            }
        );
//{/if}//
//{if $phase == "define"}//
    /**
     * 初始化浏览器原生水平滚动条控件。
     * @public
     *
     * @param {Object} options 初始化选项
     */
    var UI_BROWSER_HSCROLLBAR =
        inheritsControl(
            UI_BROWSER_SCROLLBAR,
            null,
            null,
            function (el, options) {
                this._aProperty = ['overflowX', 'scrollLeft', 'width', 'offsetWidth', null];
            }
        );
//{/if}//
//{if $phase == "define"}//
    /**
     * 初始化夹角控件。
     * @public
     *
     * @param {Object} options 初始化选项
     */
    var UI_BROWSER_CORNER = inheritsControl(UI_CONTROL),
        UI_BROWSER_CORNER_CLASS = UI_BROWSER_CORNER.prototype;
//{else}//
    (function () {
        for (var name in UI_CONTROL_CLASS) {
            UI_BROWSER_CORNER_CLASS[name] = blank;
        }
    })();
//{/if}//
//{if $phase == "define"}//
    /**
     * 初始化截面控件，截面控件支持自动展现滚动条控件，允许指定需要自动展现的垂直或水平滚动条。
     * options 对象支持的属性如下：
     * vScroll    是否自动展现垂直滚动条，默认展现
     * hScroll    是否自动展现水平滚动条，默认展现
     * browser    是否使用浏览器原生的滚动条，默认使用模拟的滚动条
     * absolute   是否包含绝对定位的Element，默认不包含
     * wheelDelta 鼠标滚轮的步长，即滚动一次移动的最小步长单位，默认总步长(差值*步长)为不大于20像素的最大值
     * @public
     *
     * @param {Object} options 初始化选项
     */
    var UI_PANEL = ui.Panel =
        inheritsControl(
            UI_CONTROL,
            'ui-panel',
            function (el, options) {
                var vscroll = options.vScroll !== false,
                    hscroll = options.hScroll !== false,
                    type = this.getType(),
                    o = createDom(
                        type + '-body',
                        'position:absolute;top:0px;left:0px' + (hscroll ? ';white-space:nowrap' : '')
                    );

                el.style.overflow = 'hidden';
                moveElements(el, o, true);

                el.innerHTML =
                    (options.browser ?
                        '<div style="position:absolute;top:0px;left:0px;overflow:auto;padding:0px;border:0px">' +
                            '<div style="width:1px;height:1px;padding:0px;border:0px"></div></div>'
                        : (vscroll ?
                            '<div class="' + type + '-vscrollbar' + this.VScrollbar.TYPES +
                                '" style="position:absolute"></div>' : '') +
                                (hscroll ?
                                    '<div class="' + type + '-hscrollbar' + this.HScrollbar.TYPES +
                                        '" style="position:absolute"></div>' : '') +
                                (vscroll && hscroll ?
                                    '<div class="' + type + '-corner' + UI_CONTROL.TYPES +
                                        '" style="position:absolute"></div>' : '')
                    ) + '<div class="' + type +
                            '-layout" style="position:relative;overflow:hidden;padding:0px"></div>';

                el.lastChild.appendChild(o);
            },
            function (el, options) {
                var i = 0,
                    browser = options.browser,
                    vscroll = options.vScroll !== false,
                    hscroll = options.hScroll !== false,
                    list = [
                        [vscroll, '_uVScrollbar', browser ? UI_BROWSER_VSCROLLBAR : this.VScrollbar],
                        [hscroll, '_uHScrollbar', browser ? UI_BROWSER_HSCROLLBAR : this.HScrollbar],
                        [vscroll && hscroll, '_uCorner', browser ? UI_BROWSER_CORNER : UI_CONTROL]
                    ],
                    o;

                this.$setBody(el.lastChild.lastChild);

                this._bAbsolute = options.absolute;
                this._nWheelDelta = options.wheelDelta;

                el = el.firstChild;
                if (browser) {
                    this._eBrowser = el;
                }

                // 生成中心区域的Element层容器，滚动是通过改变容器的left与top属性实现
                for (; o = list[i++]; ) {
                    if (o[0]) {
                        this[o[1]] = $fastCreate(o[2], el, this);
                        if (!browser) {
                            el = el.nextSibling;
                        }
                    }
                }
            }
        ),
        UI_PANEL_CLASS = UI_PANEL.prototype;
//{else}//

    UI_PANEL_CLASS.VScrollbar = UI_VSCROLLBAR;
    UI_PANEL_CLASS.HScrollbar = UI_HSCROLLBAR;

    /**
     * @override
     */
    UI_PANEL_CLASS.$cache = function (style, cacheSize) {
        UI_CONTROL_CLASS.$cache.call(this, style, cacheSize);

        var body = this.getBody(),
            mainWidth = body.offsetWidth,
            mainHeight = body.offsetHeight;

        style = getStyle(getParent(body));
        this.$$bodyWidthRevise = calcWidthRevise(style);
        this.$$bodyHeightRevise = calcHeightRevise(style);

        // 考虑到内部Element绝对定位的问题，中心区域的宽度与高度修正
        if (this._bAbsolute) {
            for (
                var i = 0,
                    list = body.all || body.getElementsByTagName('*'),
                    pos = getPosition(body);
                // 以下使用 body 代替临时的 DOM 节点对象
                body = list[i++];
            ) {
                if (body.offsetWidth && getStyle(body, 'position') == 'absolute') {
                    style = getPosition(body);
                    mainWidth = MAX(mainWidth, style.left - pos.left + body.offsetWidth);
                    mainHeight = MAX(mainHeight, style.top - pos.top + body.offsetHeight);
                }
            }
        }

        this.$$mainWidth = mainWidth;
        this.$$mainHeight = mainHeight;

        if (this._uVScrollbar) {
             this._uVScrollbar.cache(true, true);
        }
        if (this._uHScrollbar) {
             this._uHScrollbar.cache(true, true);
        }
        if (this._uCorner) {
            this._uCorner.cache(true, true);
        }
    };

    /**
     * @override
     */
    UI_PANEL_CLASS.$dispose = function () {
        this._eBrowser = null;
        UI_CONTROL_CLASS.$dispose.call(this);
    };

    /**
     * 接管对方向键的处理。
     * @override
     */
    UI_PANEL_CLASS.$keydown = UI_PANEL_CLASS.$keypress = function (event) {
        var which = getKey(),
            o = which % 2 ? this._uHScrollbar : this._uVScrollbar;

        if (which >= 37 && which <= 40 && !event.target.value) {
            if (o) {
                o.skip(which + which % 2 - 39);
            }
            return false;
        }
    };

    /**
     * 如果有垂直滚动条，则垂直滚动条随滚轮滚动。
     * @override
     */
    UI_PANEL_CLASS.$mousewheel = function (event) {
        if (this.isHovered()) {
            o = this._uVScrollbar;

            if (o && o.isShow()) {
                // 计算滚动的次数，至少要滚动一次
                var value = o.getValue(),
                    delta = this._nWheelDelta || FLOOR(20 / o.getStep()) || 1,
                    o;

                o.skip(event.detail > 0 ? delta : -delta);
                event.stopPropagation();
                // 如果截面已经移动到最后，不屏弊缺省事件
                return value == o.getValue();
            }
        }
    };

    /**
     * 控件的滚动条发生滚动的默认处理。
     * 如果控件包含滚动条，滚动条滚动时触发 onscroll 事件，如果事件返回值不为 false，则调用 $scroll 方法。
     * @protected
     */
    UI_PANEL_CLASS.$scroll = function () {
        var style = this.getBody().style;
        style.left = -MAX(this.getScrollLeft(), 0) + 'px';
        style.top = -MAX(this.getScrollTop(), 0) + 'px';
    };

    /**
     * @override
     */
    UI_PANEL_CLASS.$setSize = function (width, height) {
        UI_CONTROL_CLASS.$setSize.call(this, width, height);
        this.$locate();

        var basicWidth = this.$getBasicWidth(),
            basicHeight = this.$getBasicHeight(),
            paddingWidth = this.$$paddingLeft + this.$$paddingRight,
            paddingHeight = this.$$paddingTop + this.$$paddingBottom,
            bodyWidth = this.getWidth() - basicWidth,
            bodyHeight = this.getHeight() - basicHeight,
            mainWidth = this.$$mainWidth,
            mainHeight = this.$$mainHeight,
            browser = this._eBrowser,
            vscroll = this._uVScrollbar,
            hscroll = this._uHScrollbar,
            corner = this._uCorner,
            vsWidth = vscroll ? vscroll.getWidth() : 0,
            hsHeight = hscroll ? hscroll.getHeight() : 0, 
            innerWidth = bodyWidth - vsWidth,
            innerHeight = bodyHeight - hsHeight,
            hsWidth = innerWidth + paddingWidth,
            vsHeight = innerHeight + paddingHeight;

        // 设置垂直与水平滚动条与夹角控件的位置
        if (vscroll) {
            vscroll.setPosition(hsWidth, 0);
        }
        if (hscroll) {
            hscroll.setPosition(0, vsHeight);
        }
        if (corner) {
            corner.setPosition(hsWidth, vsHeight);
        }

        if (mainWidth <= bodyWidth && mainHeight <= bodyHeight) {
            // 宽度与高度都没有超过截面控件的宽度与高度，不需要显示滚动条
            if (vscroll) {
                vscroll.$hide();
            }
            if (hscroll) {
                hscroll.$hide();
            }
            if (corner) {
                corner.$hide();
            }
            innerWidth = bodyWidth;
            innerHeight = bodyHeight;
        }
        else {
            while (true) {
                if (corner) {
                    // 宽度与高度都超出了显示滚动条后余下的宽度与高度，垂直与水平滚动条同时显示
                    if (mainWidth > innerWidth && mainHeight > innerHeight) {
                        hscroll.$setSize(hsWidth);
                        hscroll.setTotal(browser ? mainWidth + basicWidth : mainWidth - innerWidth);
                        hscroll.$show();
                        vscroll.$setSize(0, vsHeight);
                        vscroll.setTotal(browser ? mainHeight + basicHeight : mainHeight - innerHeight);
                        vscroll.$show();
                        corner.$setSize(vsWidth, hsHeight);
                        corner.$show();
                        break;
                    }
                    corner.$hide();
                }
                if (hscroll) {
                    if (mainWidth > bodyWidth) {
                        // 宽度超出控件的宽度，高度没有超出显示水平滚动条后余下的高度，只显示水平滚动条
                        hscroll.$setSize(bodyWidth + paddingWidth);
                        hscroll.setTotal(browser ? mainWidth + basicWidth : mainWidth - bodyWidth);
                        hscroll.$show();
                        if (vscroll) {
                            vscroll.$hide();
                        }
                        innerWidth = bodyWidth;
                    }
                    else {
                        hscroll.$hide();
                    }
                }
                if (vscroll) {
                    if (mainHeight > bodyHeight) {
                        // 高度超出控件的高度，宽度没有超出显示水平滚动条后余下的宽度，只显示水平滚动条
                        vscroll.$setSize(0, bodyHeight + paddingHeight);
                        vscroll.setTotal(browser ? mainHeight + basicHeight : mainHeight - bodyHeight);
                        vscroll.$show();
                        if (hscroll) {
                            hscroll.$hide();
                        }
                        innerHeight = bodyHeight;
                    }
                    else {
                        vscroll.$hide();
                    }
                }
                break;
            }
        }

        innerWidth -= this.$$bodyWidthRevise;
        innerHeight -= this.$$bodyHeightRevise;
        (innerWidth < 0) && (innerWidth = 0);
        (innerHeight < 0) && (innerHeight = 0);

        if (vscroll) {
            vscroll.$setPageStep(innerHeight);
        }
        if (hscroll) {
            hscroll.$setPageStep(innerWidth);
        }
    
        // 设置内部定位器的大小，以下使用 corner 表示 style
        if (browser) {
            corner = browser.style;
            corner.width = bodyWidth + paddingWidth + 'px';
            corner.height = bodyHeight + paddingHeight + 'px';
        }

        corner = getParent(this.getBody()).style;
        corner.width = innerWidth + 'px';
        corner.height = innerHeight + 'px';
    };

    /**
     * 获取水平滚动条的当前值。
     * getScrollLeft 方法提供了对水平滚动条当前值的快捷访问方式，参见 getValue。
     * @public
     *
     * @return {number} 水平滚动条的当前值，如果没有水平滚动条返回 -1
     */
    UI_PANEL_CLASS.getScrollLeft = function () {
        var o = this._uHScrollbar;
        return o ? o.getValue() : -1;
    };

    /**
     * 获取垂直滚动条的当前值。
     * getScrollTop 方法提供了对水平滚动条当前值的快捷访问方式，参见 getValue。
     * @public
     *
     * @return {number} 垂直滚动条的当前值，如果没有垂直滚动条返回 -1
     */
    UI_PANEL_CLASS.getScrollTop = function () {
        var o = this._uVScrollbar;
        return o ? o.getValue() : -1;
    };

    /**
     * @override
     */
    UI_PANEL_CLASS.init = function () {
        UI_CONTROL_CLASS.init.call(this);
        if (this._uVScrollbar) {
            this._uVScrollbar.init();
        }
        if (this._uHScrollbar) {
            this._uHScrollbar.init();
        }
        if (this._uCorner) {
            this._uCorner.init();
        }
    };

    /**
     * 控件显示区域复位。
     * reset 方法设置水平滚动条或者垂直滚动条的当前值为 0。
     * @public
     */
    UI_PANEL_CLASS.reset = function () {
        if (this._uVScrollbar) {
            this._uVScrollbar.setValue(0);
        }
        if (this._uHScrollbar) {
            this._uHScrollbar.setValue(0);
        }
    };
//{/if}//
//{if 0}//
})();
//{/if}//
