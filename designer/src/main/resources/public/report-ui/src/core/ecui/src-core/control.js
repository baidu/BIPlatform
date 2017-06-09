/*
Control - ECUI 的核心组成部分，定义所有控件的基本操作。
基础控件是 ECUI 的核心组成部分，对 DOM 树上的节点区域进行封装。基础控件扩展了 Element 节点的标准事件(例如得到与失去焦点、激活等)，提供了方法对控件的基本属性(例如控件大小、位置与显示状态等)进行改变，是一切控件实现的基础。基本控件拥有四种状态：焦点(focus)、悬停(hover)、激活(active)与失效(disabled)。控件在创建过程中分为三个阶段：首先是填充控件所必须的 DOM 结构，然后缓存控件的属性信息，最后进行初始化真正的渲染并显示控件。

基础控件直接HTML初始化的例子，id指定名称，可以通过ecui.get(id)的方式访问控件:
<div ecui="type:control;id:demo">
  <!-- 这里放控件包含的内容 -->
  ...
</div>

属性
_bCapturable        - 控件是否响应浏览器事件状态
_bUserSelect        - 控件是否允许选中内容
_bFocusable         - 控件是否允许获取焦点
_bDisabled          - 控件的状态，为true时控件不处理任何事件
_bCached            - 控件是否已经读入缓存
_bCreated           - 控件是否已经完全生成
_sUID               - 控件的内部ID
_sPrimary           - 控件定义时的基本样式
_sClass             - 控件的当前样式
_sWidth             - 控件的基本宽度值，可能是百分比或者空字符串
_sHeight            - 控件的基本高度值，可能是百分比或者空字符串
_sDisplay           - 控件的布局方式，在hide时保存，在show时恢复
_eMain              - 控件的基本标签对象
_eBody              - 控件用于承载子控件的载体标签，通过$setBody函数设置这个值，绑定当前控件
_cParent            - 父控件对象
_aStatus            - 控件当前的状态集合
$$width             - 控件的宽度缓存
$$height            - 控件的高度缓存
$$bodyWidthRevise   - 内容区域的宽度修正缓存
$$bodyHeightRevise  - 内容区域的高度修正缓存
$$borderTopWidth    - 上部边框线宽度缓存
$$borderLeftWidth   - 左部边框线宽度缓存
$$borderRightWidth  - 右部边框线宽度缓存
$$borderBottomWidth - 下部边框线宽度缓存
$$paddingTop        - 上部内填充宽度缓存
$$paddingLeft       - 左部内填充宽度缓存
$$paddingRight      - 右部内填充宽度缓存
$$paddingBottom     - 下部内填充宽度缓存
$$position          - 控件布局方式缓存
*/
//{if 0}//
(function () {

    var core = ecui,
        array = core.array,
        dom = core.dom,
        ui = core.ui,
        util = core.util,

        undefined,
        DOCUMENT = document,
        REGEXP = RegExp,

        USER_AGENT = navigator.userAgent,
        ieVersion = /msie (\d+\.\d)/i.test(USER_AGENT) ? DOCUMENT.documentMode || (REGEXP.$1 - 0) : undefined,

        remove = array.remove,
        addClass = dom.addClass,
        getParent = dom.getParent,
        getStyle = dom.getStyle,
        removeClass = dom.removeClass,
        removeDom = dom.remove,
        blank = util.blank,
        timer = util.timer,
        toNumber = util.toNumber,

        REPAINT = core.REPAINT,

        $bind = core.$bind,
        $clearState = core.$clearState,
        calcLeftRevise = core.calcLeftRevise,
        calcTopRevise = core.calcTopRevise,
        disposeControl = core.dispose,
        findControl = core.findControl,
        getActived = core.getActived,
        getFocused = core.getFocused,
        getHovered = core.getHovered,
        getStatus = core.getStatus,
        inheritsControl = core.inherits,
        isContentBox = core.isContentBox,
        loseFocus = core.loseFocus,
        query = core.query,
        setFocused = core.setFocused,
        triggerEvent = core.triggerEvent,

        eventNames = [
            'mousedown', 'mouseover', 'mousemove', 'mouseout', 'mouseup',
            'click', 'dblclick', 'focus', 'blur', 'activate', 'deactivate',
            'keydown', 'keypress', 'keyup', 'mousewheel'
        ];
//{/if}//
//{if $phase == "define"}//
    ///__gzip_original__UI_CONTROL
    ///__gzip_original__UI_CONTROL_CLASS
    /**
     * 初始化基础控件。
     * options 对象支持的属性如下：
     * type       控件的类型样式
     * primary    控件的基本样式
     * current    控件的当前样式
     * capturable 是否需要捕获鼠标事件，默认捕获
     * userSelect 是否允许选中内容，默认允许
     * focusable  是否允许获取焦点，默认允许
     * resizable  是否允许改变大小，默认允许
     * disabled   是否失效，默认有效
     * @public
     *
     * @param {Object} options 初始化选项
     */
    var UI_CONTROL = ui.Control =
        inheritsControl(
            null,
            null,
            null,
            function (el, options) {
                $bind(el, this);

                this._bDisabled = !!options.disabled;
                this._sUID = options.uid;
                this._sPrimary = options.primary || '';
                this._sClass = options.current || this._sPrimary;
                this._eMain = this._eBody = el;
                this._cParent = null;

                this._bCapturable = options.capturable !== false;
                this._bUserSelect = options.userSelect !== false;
                this._bFocusable = options.focusable !== false;
                if (options.resizable !== false) {
                    this._bResizable = true;
                    el = el.style;
                    this._sWidth = el.width;
                    this._sHeight = el.height;
                }
                else {
                    this._bResizable = false;
                }

                this._aStatus = ['', ' '];
            }
        ),
        UI_CONTROL_CLASS = UI_CONTROL.prototype,
        UI_CONTROL_READY_LIST,
        UI_CONTROL_QUERY_SHOW = {custom: function (control) {
            return this != control && this.contain(control) && control.isShow();
        }};
//{else}//
    /**
     * 设置控件的父对象。
     * @private
     *
     * @param {ecui.ui.Control} control 需要设置的控件对象
     * @param {HTMLElement} parent 父控件对象
     * @param {HTMLElement} parentElement 父 Element 对象
     */
    function UI_CONTROL_ALTER_PARENT(control, parent, parentElement) {
        var oldParent = control._cParent,
            el = control.getOuter(),
            flag = control._bCreated && control.isShow();

        // 触发原来父控件的移除子控件事件
        if (parent != oldParent) {
            if (oldParent) {
                if (!triggerEvent(oldParent, 'remove', null, [control])) {
                    return;
                }
            }
            if (parent) {
                if (!triggerEvent(parent, 'append', null, [control])) {
                    parent = parentElement = null;
                }
            }
        }

        if (parentElement != getParent(el)) {
            if (parentElement) {
                parentElement.appendChild(el);
            }
            else {
                removeDom(el);
            }
            // 当 DOM 树位置发生改变时，$setParent必须被执行
            control.$setParent(parent);
        }

        if (flag != (control._bCreated && control.isShow())) {
            triggerEvent(control, flag ? 'hide' : 'show', false);
        }
    }

    /**
     * 控件获得激活事件的默认处理。
     * 控件获得激活时，添加状态样式 -active。
     * @protected
     *
     * @param {ecui.ui.Event} event 事件对象
     */
    UI_CONTROL_CLASS.$activate = function () {
        this.alterClass('+active');
    };

    /**
     * 控件失去焦点事件的默认处理。
     * 控件失去焦点时，移除状态样式 -focus。
     * @protected
     *
     * @param {ecui.ui.Event} event 事件对象
     */
    UI_CONTROL_CLASS.$blur = function () {
        this.alterClass('-focus');
    };

    /**
     * 缓存控件的属性。
     * $cache 方法缓存部分控件属性的值，在初始化时避免频繁的读写交替操作，加快渲染的速度，在子控件或者应用程序开发过程中，如果需要避开控件提供的方法直接操作 Element 对象，操作完成后必须调用 clearCache 方法清除控件的属性缓存，否则将引发错误。
     * @protected
     *
     * @param {CssStyle} style 主元素的 Css 样式对象
     * @param {boolean} cacheSize 是否需要缓存控件的大小，如果控件是另一个控件的部件时，不缓存大小能加快渲染速度，默认缓存
     */
    UI_CONTROL_CLASS.$cache = function (style, cacheSize) {
        if (ieVersion < 8) {
            o = style.borderWidth;
            if (o.indexOf(' ') > 0) {
                o = o.split(' ');
                this.$$borderTopWidth = toNumber(o[0]);
                this.$$borderRightWidth = toNumber(o[1]);
                this.$$borderBottomWidth = o[2] ? toNumber(o[2]) : this.$$borderTopWidth;
                this.$$borderLeftWidth = o[3] ? toNumber(o[3]) : this.$$borderRightWidth = toNumber(o[1]);
            }
            else {
                this.$$borderTopWidth = this.$$borderLeftWidth = this.$$borderRightWidth = this.$$borderBottomWidth =
                    toNumber(o);
            }
            o = style.padding;
            if (o.indexOf(' ') > 0) {
                o = o.split(' ');
                this.$$paddingTop = toNumber(o[0]);
                this.$$paddingRight = toNumber(o[1]);
                this.$$paddingBottom = o[2] ? toNumber(o[2]) : this.$$paddingTop;
                this.$$paddingLeft = o[3] ? toNumber(o[3]) : this.$$paddingRight;
            }
            else {
                this.$$paddingTop = this.$$paddingLeft = this.$$paddingRight = this.$$paddingBottom = toNumber(o);
            }
        }
        else {
            for (
                var i = 0,
                    list = [
                        'borderTopWidth', 'borderLeftWidth', 'borderRightWidth', 'borderBottomWidth',
                        'paddingTop', 'paddingLeft', 'paddingRight', 'paddingBottom'
                    ],
                    o;
                o = list[i++];
            ) {
                this['$$' + o] = toNumber(style[o]);
            }
        }

        this.$$position = style.position;

        if (cacheSize !== false) {
            o = isContentBox();
            this.$$width = this._eMain.offsetWidth || toNumber(style.width) + (o ? this.$getBasicWidth() : 0);
            this.$$height = this._eMain.offsetHeight || toNumber(style.height) + (o ? this.$getBasicHeight() : 0);
        }
    };

    /**
     * 控件失去激活事件的默认处理。
     * 控件失去激活时，移除状态样式 -active。
     * @protected
     *
     * @param {ecui.ui.Event} event 事件对象
     */
    UI_CONTROL_CLASS.$deactivate = function () {
        this.alterClass('-active');
    };

    /**
     * 销毁控件的默认处理。
     * 页面卸载时将销毁所有的控件，释放循环引用，防止在 IE 下发生内存泄漏，$dispose 方法的调用不会受到 ondispose 事件返回值的影响。
     * @protected
     */
    UI_CONTROL_CLASS.$dispose = function () {
        try {
            triggerEvent(this, 'dispose', false);
        }
        catch (e) {
        }
        this._eMain.getControl = undefined;
        this._eMain = this._eBody = null;
        // 取消 $ready 的操作，防止控件在 onload 结束前被 dispose，从而引发 $ready 访问的信息错误的问题
        this.$ready = blank;
    };

    /**
     * 控件获得焦点事件的默认处理。
     * 控件获得焦点时，添加状态样式 -focus。
     * @protected
     *
     * @param {ecui.ui.Event} event 事件对象
     */
    UI_CONTROL_CLASS.$focus = function () {
        this.alterClass('+focus');
    };

    /**
     * 获取控件的基本高度。
     * 控件的基本高度指控件基本区域与用户数据存放区域的高度差值，即主元素与内部元素(如果相同则忽略其中之一)的上下边框宽度(border-width)与上下内填充宽度(padding)之和。
     * @public
     *
     * @return {number} 控件的基本高度
     */
    UI_CONTROL_CLASS.$getBasicHeight = function () {
        return this.$$borderTopWidth + this.$$borderBottomWidth + this.$$paddingTop + this.$$paddingBottom;
    };

    /**
     * 获取控件的基本宽度。
     * 控件的基本宽度指控件基本区域与用户数据存放区域的宽度差值，即主元素与内部元素(如果相同则忽略其中之一)的左右边框宽度(border-width)与左右内填充宽度(padding)之和。
     * @public
     *
     * @return {number} 控件的基本宽度
     */
    UI_CONTROL_CLASS.$getBasicWidth = function () {
        return this.$$borderLeftWidth + this.$$borderRightWidth + this.$$paddingLeft + this.$$paddingRight;
    };

    /**
     * 获取指定的部件。
     * $getSection 方法返回控件的一个部件对象，部件对象也是 ECUI 控件，是当前控件的组成成份，不可缺少，请不要轻易的对部件对象进行操作。
     * @protected
     *
     * @param {string} name 部件名称
     * @return {ecui.ui.Control} 部件对象
     */
    UI_CONTROL_CLASS.$getSection = function (name) {
        return this['_u' + name];
    };

    /**
     * 隐藏控件。
     * $hide 方法直接隐藏控件，控件失去激活、悬停与焦点状态，不检查控件之前的状态，因此不会导致浏览器的刷新操作。
     * @protected
     */
    UI_CONTROL_CLASS.$hide = function () {
        if (this._sDisplay === undefined) {
            if (this._bCreated) {
                for (var i = 0, list = query.call(this, UI_CONTROL_QUERY_SHOW), o; o = list[i++]; ) {
                    triggerEvent(o, 'hide', false);
                }
            }

            o = this.getOuter().style;

            // 保存控件原来的 display 值，在显示时恢复
            this._sDisplay = o.display;
            o.display = 'none';
            // 控件隐藏时需要清除状态
            $clearState(this);
        }
    };

    /**
     * 设置控件容器支持坐标定位。
     * $locate 方法执行后，容器内部 Element 对象的 offsetParent 将指向主元素(参见 getMain 方法)。
     * @protected
     */
    UI_CONTROL_CLASS.$locate = function () {
        if (this.$$position == 'static') {
            this._eMain.style.position = this.$$position = 'relative';
        }
    };

    /**
     * 鼠标移出事件的默认处理。
     * 鼠标移出控件区域时，控件失去悬停状态，移除状态样式 -hover。
     * @protected
     *
     * @param {ecui.ui.Event} event 事件对象
     */
    UI_CONTROL_CLASS.$mouseout = function () {
        this.alterClass('-hover');
    };

    /**
     * 鼠标移入事件的默认处理。
     * 鼠标移入控件区域时，控件获得悬停状态，添加状态样式 -hover。
     * @protected
     *
     * @param {ecui.ui.Event} event 事件对象
     */
    UI_CONTROL_CLASS.$mouseover = function () {
        this.alterClass('+hover');
    };

    /**
     * 控件大小变化事件的默认处理。
     * @protected
     */
    UI_CONTROL_CLASS.$resize = function () {
        //__gzip_original__el
        //__gzip_original__currStyle
        var el = this._eMain,
            currStyle = el.style;

        currStyle.width = this._sWidth;
        if (ieVersion < 8 && getStatus() != REPAINT) {
            // 修复ie6/7下宽度自适应错误的问题
            var style = getStyle(el);
            if (style.width == 'auto' && style.display == 'block') {
                currStyle.width = '100%';
                currStyle.width = el.offsetWidth - (isContentBox() ? this.$getBasicWidth() * 2 : 0) + 'px';
            }
        }
        currStyle.height = this._sHeight;
    };

    /**
     * 设置控件的内层元素。
     * ECUI 控件 逻辑上分为外层元素、主元素与内层元素，外层元素用于控制控件自身布局，主元素是控件生成时捆绑的 Element 对象，而内层元素用于控制控件对象的子控件与文本布局，三者允许是同一个 Element 对象。
     * @protected
     *
     * @param {HTMLElement} el Element 对象
     */
    UI_CONTROL_CLASS.$setBody = function (el) {
        this._eBody = el;
    };

    /**
     * 直接设置父控件。
     * 相对于 setParent 方法，$setParent 方法仅设置控件对象逻辑上的父对象，不进行任何逻辑上的检查，用于某些特殊情况下的设定，如下拉框控件中的选项框子控件需要使用 $setParent 方法设置它的逻辑父控件为下拉框控件。
     * @protected
     *
     * @param {ecui.ui.Control} parent ECUI 控件对象
     */
    UI_CONTROL_CLASS.$setParent = function (parent) {
        this._cParent = parent;
    };

    /**
     * 设置控件的大小。
     * @protected
     *
     * @param {number} width 宽度，如果不需要设置则将参数设置为等价于逻辑非的值
     * @param {number} height 高度，如果不需要设置则省略此参数
     */
    UI_CONTROL_CLASS.$setSize = function (width, height) {
        //__gzip_original__style
        var style = this._eMain.style,
            o = this._eMain.tagName,
            fixedSize = isContentBox() && o != 'BUTTON' && o != 'INPUT';

        // 防止负宽度IE下出错
        if (width && (o = width - (fixedSize ? this.$getBasicWidth() : 0)) > 0) {
            style.width = o + 'px';
            this.$$width = width;
        }

        // 防止负高度IE下出错
        if (height && (o = height - (fixedSize ? this.$getBasicHeight() : 0)) > 0) {
            style.height = o + 'px';
            this.$$height = height;
        }
    };

    /**
     * 显示控件。
     * $show 方法直接显示控件，不检查控件之前的状态，因此不会导致浏览器的刷新操作。
     * @protected
     */
    UI_CONTROL_CLASS.$show = function () {
        this.getOuter().style.display = this._sDisplay || '';
        this._sDisplay = undefined;

        if (this._bCreated) {
            for (var i = 0, list = query.call(this, UI_CONTROL_QUERY_SHOW), o; o = list[i++]; ) {
                triggerEvent(o, 'show', false);
            }
        }
    };

    /**
     * 为控件添加/移除一个扩展样式。
     * 扩展样式分别附加在类型样式与当前样式之后(参见 getTypes 与 getClass 方法)，使用-号进行分隔。如果类型样式为 ui-control，当前样式为 demo，扩展样式 hover 后，控件主元素将存在四个样式，分别为 ui-control、demo、ui-control-hover 与 demo-hover。
     * @public
     *
     * @param {string} className 扩展样式名，以+号开头表示添加扩展样式，以-号开头表示移除扩展样式
     */
    UI_CONTROL_CLASS.alterClass = function (className) {
        var flag = className.charAt(0) == '+';

        if (flag) {
            className = '-' + className.slice(1) + ' ';
        }
        else {
            className += ' ';
        }

        (flag ? addClass : removeClass)(this._eMain, this.getTypes().concat([this._sClass, '']).join(className));

        if (flag) {
            this._aStatus.push(className);
        }
        else {
            remove(this._aStatus, className);
        }
    };

    /**
     * 将控件添加到页面元素中。
     * appendTo 方法设置父元素，并使用 findControl 查找父控件对象。如果父控件发生变化，原有的父控件若存在，将触发移除子控件事件(onremove)，并解除控件与原有父控件的关联，新的父控件若存在，将触发添加子控件事件(onappend)，如果此事件返回 false，添加失败，相当于忽略 parentElement 参数。
     * @public
     *
     * @param {HTMLElement} parentElement 父 Element 对象，忽略参数控件将移出 DOM 树
     */
    UI_CONTROL_CLASS.appendTo = function (parentElement) {
        UI_CONTROL_ALTER_PARENT(this, parentElement && findControl(parentElement), parentElement);
    };

    /**
     * 控件失去焦点状态。
     * blur 方法将使控件失去焦点状态，参见 loseFocus 方法。
     * @public
     */
    UI_CONTROL_CLASS.blur = function () {
        loseFocus(this);
    };

    /**
     * 缓存控件的属性。
     * cache 方法验证控件是否已经缓存，如果未缓存将调用 $cache 方法缓存控件属性的值。在子控件或者应用程序开发过程中，如果需要避开控件提供的方法直接操作 Element 对象，操作完成后必须调用 clearCache 方法清除控件的属性缓存，否则将引发错误。
     * @public
     *
     * @param {boolean} cacheSize 是否需要缓存控件的大小，如果控件是另一个控件的部件时，不缓存大小能加快渲染速度，默认缓存
     * @param {boolean} force 是否需要强制刷新缓存，相当于之前执行了 clearCache 方法，默认不强制刷新
     */
    UI_CONTROL_CLASS.cache = function (cacheSize, force) {
        if (force || !this._bCached) {
            this._bCached = true;
            this.$cache(getStyle(this._eMain), cacheSize);
        }
    };

    /**
     * 清除控件的缓存。
     * 在子控件或者应用程序开发过程中，如果需要避开控件提供的方法直接操作 Element 对象，操作完成后必须调用 clearCache 方法清除控件的属性缓存，否则将引发错误。
     * @public
     */
    UI_CONTROL_CLASS.clearCache = function () {
        this._bCached = false;
    };

    /**
     * 判断是否包含指定的控件。
     * contain 方法判断指定的控件是否逻辑上属于当前控件的内部区域，即当前控件是指定的控件的某一级父控件。
     * @public
     *
     * @param {ecui.ui.Control} control ECUI 控件
     * @return {boolean} 是否包含指定的控件
     */
    UI_CONTROL_CLASS.contain = function (control) {
        for (; control; control = control._cParent) {
            if (control == this) {
                return true;
            }
        }
        return false;
    };

    /**
     * 控件获得失效状态。
     * 控件获得失效状态时，添加状态样式 -disabled(参见 alterClass 方法)。disable 方法导致控件失去激活、悬停、焦点状态，所有子控件的 isDisabled 方法返回 true，但不会设置子控件的失效状态样式。
     * @public
     *
     * @return {boolean} 控件失效状态是否改变
     */
    UI_CONTROL_CLASS.disable = function () {
        if (!this._bDisabled) {
            this.alterClass('+disabled');
            this._bDisabled = true;
            $clearState(this);
            return true;
        }
        return false;
    };

    /**
     * 销毁控件。
     * dispose 方法销毁控件及其所有的子控件，相当于调用 ecui.dispose(this) 方法。
     * @public
     */
    UI_CONTROL_CLASS.dispose = function () {
        disposeControl(this);
    };

    /**
     * 控件解除失效状态。
     * 控件解除失效状态时，移除状态样式 -disabled(参见 alterClass 方法)。enable 方法仅解除控件自身的失效状态，如果其父控件失效，isDisabled 方法返回 true。
     * @public
     *
     * @return {boolean} 控件失效状态是否改变
     */
    UI_CONTROL_CLASS.enable = function () {
        if (this._bDisabled) {
            this.alterClass('-disabled');
            this._bDisabled = false;
            return true;
        }
        return false;
    };

    /**
     * 控件获得焦点状态。
     * 如果控件没有处于焦点状态，focus 方法将设置控件获取焦点状态，参见 isFocused 与 setFocused 方法。
     * @public
     */
    UI_CONTROL_CLASS.focus = function () {
        if (!this.isFocused()) {
            setFocused(this);
        }
    };

    /**
     * 获取控件的内层元素。
     * getBody 方法返回用于控制子控件与文本布局的内层元素。
     * @public
     *
     * @return {HTMLElement} Element 对象
     */
    UI_CONTROL_CLASS.getBody = function () {
        return this._eBody;
    };

    /**
     * 获取控件内层可使用区域的高度。
     * getBodyHeight 方法返回能被子控件与文本填充的控件区域高度，相当于盒子模型的 content 区域的高度。
     * @public
     *
     * @return {number} 控件内层可使用区域的宽度
     */
    UI_CONTROL_CLASS.getBodyHeight = function () {
        return this.getHeight() - this.getMinimumHeight();
    };

    /**
     * 获取控件内层可使用区域的宽度。
     * getBodyWidth 方法返回能被子控件与文本填充的控件区域宽度，相当于盒子模型的 content 区域的宽度。
     * @public
     *
     * @return {number} 控件内层可使用区域的宽度
     */
    UI_CONTROL_CLASS.getBodyWidth = function () {
        return this.getWidth() - this.getMinimumWidth();
    };

    /**
     * 获取控件的当前样式。
     * getClass 方法返回控件当前使用的样式，扩展样式分别附加在类型样式与当前样式之后，从而实现控件的状态样式改变，详细的描述请参见 alterClass 方法。当前样式与 getPrimary 方法返回的基本样式存在区别，在控件生成初期，当前样式等于基本样式，基本样式在初始化后无法改变，setClass 方法改变当前样式。
     * @public
     *
     * @return {string} 控件的当前样式
     */
    UI_CONTROL_CLASS.getClass = function () {
        return this._sClass;
    };

    /**
     * 获取控件的内容。
     * @public
     *
     * @return {string} HTML 片断
     */
    UI_CONTROL_CLASS.getContent = function () {
        return this._eBody.innerHTML;
    };

    /**
     * 获取控件区域的高度。
     * @public
     *
     * @return {number} 控件的高度
     */
    UI_CONTROL_CLASS.getHeight = function () {
        this.cache();
        return this.$$height;
    };

    /**
     * 获取控件的主元素。
     * getMain 方法返回控件生成时定义的 Element 对象(参见 create 方法)。
     * @public
     *
     * @return {HTMLElement} Element 对象
     */
    UI_CONTROL_CLASS.getMain = function () {
        return this._eMain;
    };

    /**
     * 获取控件的最小高度。
     * setSize 方法不允许设置小于 getMinimumHeight 方法返回的高度值。
     * @public
     *
     * @return {number} 控件的最小高度
     */
    UI_CONTROL_CLASS.getMinimumHeight = function () {
        this.cache();
        return this.$getBasicHeight() + (this.$$bodyHeightRevise || 0);
    };

    /**
     * 获取控件的最小宽度。
     * @public
     *
     * @return {number} 控件的最小宽度
     */
    UI_CONTROL_CLASS.getMinimumWidth = function () {
        this.cache();
        return this.$getBasicWidth() + (this.$$bodyWidthRevise || 0);
    };

    /**
     * 获取控件的外层元素。
     * getOuter 方法返回用于控制控件自身布局的外层元素。
     * @public
     *
     * @return {HTMLElement} Element 对象
     */
    UI_CONTROL_CLASS.getOuter = function () {
        return this._eMain;
    };

    /**
     * 获取父控件。
     * 控件接收的事件将向父控件冒泡处理，getParent 返回的结果是 ECUI 的逻辑父控件，父控件与子控件不一定存在 DOM 树层面的父子级关系。
     * @public
     *
     * @return {ecui.ui.Control} 父控件对象
     */
    UI_CONTROL_CLASS.getParent = function () {
        return this._cParent || null;
    };

    /**
     * 获取控件的基本样式。
     * getPrimary 方法返回控件生成时指定的 primary 参数(参见 create 方法)。基本样式与通过 getClass 方法返回的当前样式存在区别，在控件生成初期，当前样式等于基本样式，基本样式在初始化后无法改变，setClass 方法改变当前样式。
     * @public
     *
     * @return {string} 控件的基本样式
     */
    UI_CONTROL_CLASS.getPrimary = function () {
        return this._sPrimary;
    };

    /**
     * 获取控件的类型。
     * @public
     *
     * @return {string} 控件的类型
     */
    UI_CONTROL_CLASS.getType = function () {
        return this.constructor.agent.types[0];
    };

    /**
     * 获取控件的类型样式组。
     * getTypes 方法返回控件的类型样式组，类型样式在控件继承时指定。
     * @public
     *
     * @return {Array} 控件的类型样式组
     */
    UI_CONTROL_CLASS.getTypes = function () {
        return this.constructor.agent.types.slice();
    };

    /**
     * 获取控件的内部唯一标识符。
     * getUID 方法返回的 ID 不是初始化选项中指定的 id，而是框架为每个控件生成的内部唯一标识符。
     * @public
     *
     * @return {string} 控件 ID
     */
    UI_CONTROL_CLASS.getUID = function () {
        return this._sUID;
    };

    /**
     * 获取控件区域的宽度。
     * @public
     *
     * @return {number} 控件的宽度
     */
    UI_CONTROL_CLASS.getWidth = function () {
        this.cache();
        return this.$$width;
    };

    /**
     * 获取控件的相对X轴坐标。
     * getX 方法返回控件的外层元素的 offsetLeft 属性值。如果需要得到控件相对于整个文档的X轴坐标，请调用 getOuter 方法获得外层元素，然后调用 DOM 的相关函数计算(例如 ecui.dom.getPosition)。
     * @public
     *
     * @return {number} X轴坐标
     */
    UI_CONTROL_CLASS.getX = function () {
        var el = this.getOuter();

        return this.isShow() ? el.offsetLeft - calcLeftRevise(el) : 0;
    };

    /**
     * 获取控件的相对Y轴坐标。
     * getY 方法返回控件的外层元素的 offsetTop 属性值。如果需要得到控件相对于整个文档的Y轴坐标，请调用 getOuter 方法获得外层元素，然后调用 DOM 的相关函数计算(例如 ecui.dom.getPosition)。
     * @public
     *
     * @return {number} Y轴坐标
     */
    UI_CONTROL_CLASS.getY = function () {
        var el = this.getOuter();

        return this.isShow() ? el.offsetTop - calcTopRevise(el) : 0;
    };

    /**
     * 隐藏控件。
     * 如果控件处于显示状态，调用 hide 方法会触发 onhide 事件，控件转为隐藏状态，并且控件会自动失去激活、悬停与焦点状态。如果控件已经处于隐藏状态，则不执行任何操作。
     * @public
     *
     * @return {boolean} 显示状态是否改变
     */
    UI_CONTROL_CLASS.hide = function () {
        if (this.isShow()) {
            triggerEvent(this, 'hide');
        }
    };

    /**
     * 控件初始化。
     * init 方法在控件缓存读取后调用，有关控件生成的完整过程描述请参见 基础控件。
     * @public
     */
    UI_CONTROL_CLASS.init = function () {
        if (!this._bCreated) {
            if (this._bDisabled) {
                this.alterClass('+disabled');
            }
            this.$setSize(this.getWidth(), this.getHeight());

            if (UI_CONTROL_READY_LIST === null) {
                // 页面已经加载完毕，直接运行 $ready 方法
                this.$ready();
            }
            else {
                if (!UI_CONTROL_READY_LIST) {
                    // 页面未加载完成，首先将 $ready 方法的调用存放在调用序列中
                    // 需要这么做的原因是 ie 的 input 回填机制，一定要在 onload 之后才触发
                    // ECUI 应该避免直接使用 ecui.get(xxx) 导致初始化，所有的代码应该在 onload 之后运行
                    UI_CONTROL_READY_LIST = [];
                    timer(function () {
                        for (var i = 0, o; o = UI_CONTROL_READY_LIST[i++]; ) {
                            o.$ready();
                        }
                        UI_CONTROL_READY_LIST = null;
                    });
                }
                if (this.$ready != blank) {
                    UI_CONTROL_READY_LIST.push(this);
                }
            }
            this._bCreated = true;
        }
    };

    /**
     * 判断控件是否处于激活状态。
     * @public
     *
     * @return {boolean} 控件是否处于激活状态
     */
    UI_CONTROL_CLASS.isActived = function () {
        return this.contain(getActived());
    };

    /**
     * 判断是否响应浏览器事件。
     * 控件不响应浏览器事件时，相应的事件由父控件进行处理。
     * @public
     *
     * @return {boolean} 控件是否响应浏览器事件
     */
    UI_CONTROL_CLASS.isCapturable = function () {
        return this._bCapturable;
    };

    /**
     * 判断控件是否处于失效状态。
     * 控件是否处于失效状态，影响控件是否处理事件，它受到父控件的失效状态的影响。可以通过 enable 与 disable 方法改变控件的失效状态，如果控件失效，它所有的子控件也会失效
     * @public
     *
     * @return {boolean} 控件是否失效
     */
    UI_CONTROL_CLASS.isDisabled = function () {
        return this._bDisabled || (!!this._cParent && this._cParent.isDisabled());
    };

    /**
     * 判断是否允许获得焦点。
     * 控件不允许获得焦点时，被点击时不会改变当前处于焦点状态的控件，但此时控件拥有框架事件响应的最高优先级。
     * @public
     *
     * @return {boolean} 控件是否允许获取焦点
     */
    UI_CONTROL_CLASS.isFocusable = function () {
        return this._bFocusable;
    };

    /**
     * 判断控件是否处于焦点状态。
     * @public
     *
     * @return {boolean} 控件是否处于焦点状态
     */
    UI_CONTROL_CLASS.isFocused = function () {
        return this.contain(getFocused());
    };

    /**
     * 判断控件是否处于悬停状态。
     * @public
     *
     * @return {boolean} 控件是否处于悬停状态
     */
    UI_CONTROL_CLASS.isHovered = function () {
        return this.contain(getHovered());
    };

    /**
     * 判断控件是否允许改变大小。
     * @public
     *
     * @return {boolean} 控件是否允许改变大小
     */
    UI_CONTROL_CLASS.isResizable = function () {
        return this._bResizable;
    };

    /**
     * 判断是否处于显示状态。
     * @public
     *
     * @return {boolean} 控件是否显示
     */
    UI_CONTROL_CLASS.isShow = function () {
        return !!this.getOuter().offsetWidth;
    };

    /**
     * 判断是否允许选中内容。
     * @public
     *
     * @return {boolean} 控件是否允许选中内容
     */
    UI_CONTROL_CLASS.isUserSelect = function () {
        return this._bUserSelect;
    };

    /**
     * 控件完全刷新。
     * 对于存在数据源的控件，render 方法根据数据源重新填充控件内容，重新计算控件的大小进行完全的重绘。
     * @public
     */
    UI_CONTROL_CLASS.render = function () {
        this.resize();
    };

    /**
     * 控件刷新。
     * repaint 方法不改变控件的内容与大小进行重绘。控件如果生成后不位于文档 DOM 树中，样式无法被正常读取，控件显示后如果不是预期的效果，需要调用 repaint 方法刷新。
     * @public
     */
    UI_CONTROL_CLASS.repaint = function () {
        this.cache(true, true);
        this.$setSize(this.getWidth(), this.getHeight());
    };

    /**
     * 控件重置大小并刷新。
     * resize 方法重新计算并设置控件的大小，浏览器可视化区域发生变化时，可能需要改变控件大小，框架会自动调用控件的 resize 方法。
     */
    UI_CONTROL_CLASS.resize = function () {
        if (this._bResizable) {
            this.$resize();
            this.repaint();
        }
    };

    /**
     * 设置控件可使用区域的大小。
     * @public
     *
     * @param {number} width 宽度
     * @param {number} height 高度
     */
    UI_CONTROL_CLASS.setBodySize = function (width, height) {
        this.setSize(width && width + this.getMinimumWidth(), height && height + this.getMinimumHeight());
    };

    /**
     * 设置控件的当前样式。
     * setClass 方法改变控件的当前样式，扩展样式分别附加在类型样式与当前样式之后，从而实现控件的状态样式改变，详细的描述请参见 alterClass 方法。控件的当前样式通过 getClass 方法获取。
     * @public
     *
     * @param {string} currClass 控件的当前样式名称
     */
    UI_CONTROL_CLASS.setClass = function (currClass) {
        var i = 0,
            oldClass = this._sClass,
            classes = this.getTypes(),
            list = [];

        currClass = currClass || this._sPrimary;

        // 如果基本样式没有改变不需要执行
        if (currClass != oldClass) {
            classes.splice(0, 0, this._sClass = currClass);
            for (; classes[i]; ) {
                list[i] = this._aStatus.join(classes[i++]);
            }
            classes[0] = oldClass;
            this._eMain.className =
                list.join('') +
                    this._eMain.className.split(/\s+/).join('  ').replace(
                        new REGEXP('(^| )(' + classes.join('|') + ')(-[^ ]+)?( |$)', 'g'),
                        ''
                    );
        }
    };

    /**
     * 设置控件的内容。
     * @public
     *
     * @param {string} html HTML 片断
     */
    UI_CONTROL_CLASS.setContent = function (html) {
        this._eBody.innerHTML = html;
    };

    /**
     * 设置当前控件的父控件。
     * setParent 方法设置父控件，将当前控件挂接到父控件对象的内层元素中。如果父控件发生变化，原有的父控件若存在，将触发移除子控件事件(onremove)，并解除控件与原有父控件的关联，新的父控件若存在，将触发添加子控件事件(onappend)，如果此事件返回 false，添加失败，相当于忽略 parent 参数。
     * @public
     *
     * @param {ecui.ui.Control} parent 父控件对象，忽略参数控件将移出 DOM 树
     */
    UI_CONTROL_CLASS.setParent = function (parent) {
        UI_CONTROL_ALTER_PARENT(this, parent, parent && parent._eBody);
    };

    /**
     * 设置控件的坐标。
     * setPosition 方法设置的是控件的 left 与 top 样式，受到 position 样式的影响。
     * @public
     *
     * @param {number} x 控件的X轴坐标
     * @param {number} y 控件的Y轴坐标
     */
    UI_CONTROL_CLASS.setPosition = function (x, y) {
        var style = this.getOuter().style;
        style.left = x + 'px';
        style.top = y + 'px';
    };

    /**
     * 设置控件的大小。
     * 需要设置的控件大小如果低于控件允许的最小值，将忽略对应的宽度或高度的设置。
     * @public
     *
     * @param {number} width 控件的宽度
     * @param {number} height 控件的高度
     */
    UI_CONTROL_CLASS.setSize = function (width, height) {
        if (this._bResizable) {
            this.cache();

            //__gzip_original__style
            var style = this._eMain.style;

            // 控件新的大小不允许小于最小值
            if (width < this.getMinimumWidth()) {
                width = 0;
            }
            if (height < this.getMinimumHeight()) {
                height = 0;
            }

            this.$setSize(width, height);

            if (width) {
                this._sWidth = style.width;
            }
            if (height) {
                this._sHeight = style.height;
            }
        }
    };

    /**
     * 显示控件。
     * 如果控件处于隐藏状态，调用 show 方法会触发 onshow 事件，控件转为显示状态。如果控件已经处于显示状态，则不执行任何操作。
     * @public
     */
    UI_CONTROL_CLASS.show = function () {
        if (!this.isShow()) {
            triggerEvent(this, 'show');
            return true;
        }
        return false;
    };

    (function () {
        // 初始化事件处理函数，以事件名命名，这些函数行为均是判断控件是否可操作/是否需要调用事件/是否需要执行缺省的事件处理，对应的缺省事件处理函数名以$开头后接事件名，处理函数以及缺省事件处理函数参数均为事件对象，仅执行一次。
        for (var i = 0, o; o = eventNames[i++]; ) {
            UI_CONTROL_CLASS['$' + o] = UI_CONTROL_CLASS['$' + o] || blank;
        }

        // 初始化空操作的一些缺省处理
        UI_CONTROL_CLASS.$intercept = UI_CONTROL_CLASS.$append = UI_CONTROL_CLASS.$remove =
            UI_CONTROL_CLASS.$zoomstart = UI_CONTROL_CLASS.$zoom = UI_CONTROL_CLASS.$zoomend =
            UI_CONTROL_CLASS.$dragstart = UI_CONTROL_CLASS.$dragmove = UI_CONTROL_CLASS.$dragend =
            UI_CONTROL_CLASS.$ready = UI_CONTROL_CLASS.$pagescroll = blank;
    })();
//{/if}//
//{if 0}//
})();
//{/if}//
