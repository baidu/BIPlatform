//{if 0}//
(function () {
    var core = ecui,
        array = core.array,
        dom = core.dom,
        ext = core.ext,
        string = core.string,
        util = core.util,
        ui = core.ui,

        undefined,
        WINDOW = window,
        DOCUMENT = document,
        DATE = Date,
        MATH = Math,
        REGEXP = RegExp,
        ABS = MATH.abs,
        MAX = MATH.max,
        MIN = MATH.min,
        ISNAN = isNaN,

        USER_AGENT = navigator.userAgent,
        isStrict = DOCUMENT.compatMode == 'CSS1Compat',
        ieVersion = /msie (\d+\.\d)/i.test(USER_AGENT) ? DOCUMENT.documentMode || (REGEXP.$1 - 0) : undefined,
        firefoxVersion = /firefox\/(\d+\.\d)/i.test(USER_AGENT) ? REGEXP.$1 - 0 : undefined,

        indexOf = array.indexOf,
        remove = array.remove,
        addClass = dom.addClass,
        contain = dom.contain,
        createDom = dom.create,
        getAttribute = dom.getAttribute,
        getParent = dom.getParent,
        getPosition = dom.getPosition,
        getStyle = dom.getStyle,
        insertHTML = dom.insertHTML,
        ready = dom.ready,
        removeDom = dom.remove,
        removeClass = dom.removeClass,
        setStyle = dom.setStyle,
        toCamelCase = string.toCamelCase,
        attachEvent = util.attachEvent,
        blank = util.blank,
        detachEvent = util.detachEvent,
        extend = util.extend,
        getView = util.getView,
        inherits = util.inherits,
        timer = util.timer,
        toNumber = util.toNumber;
//{/if}//
//{if $phase == "define"}//
    var NORMAL  = core.NORMAL  = 0,
        LOADING = core.LOADING = 1,
        REPAINT = core.REPAINT = 2;

//__gzip_unitize__event
    var $bind,
        $connect,
        $clearState,
        $create,
        $fastCreate,
        calcHeightRevise,
        calcLeftRevise,
        calcTopRevise,
        calcWidthRevise,
        createControl,
        disposeControl,
        drag,

        /**
         * 从指定的 Element 对象开始，依次向它的父节点查找绑定的 ECUI 控件。
         * findControl 方法，会返回从当前 Element 对象开始，依次向它的父 Element 查找到的第一个绑定(参见 $bind 方法)的 ECUI 控件。findControl 方法一般在控件创建时使用，用于查找父控件对象。
         * @public
         *
         * @param {HTMLElement} el Element 对象
         * @return {ecui.ui.Control} ECUI 控件对象，如果不能找到，返回 null
         */
        findControl = core.findControl = function (el) {
            for (; el; el = getParent(el)) {
                if (el.getControl) {
                    return el.getControl();
                }
            }

            return null;
        },
        getActived,
        getAttributeName,
        getFocused,
        getHovered,
        getKey,
        getMouseX,
        getMouseY,
        getOptions,
        getScrollNarrow,
        getStatus,
        inheritsControl,
        intercept,
        isContentBox,
        loseFocus,
        mask,
        needInitClass,
        query,
        restore,
        setFocused,
        triggerEvent,
        wrapEvent,

        eventNames = [
            'mousedown', 'mouseover', 'mousemove', 'mouseout', 'mouseup',
            'click', 'dblclick', 'focus', 'blur', 'activate', 'deactivate',
            'keydown', 'keypress', 'keyup', 'mousewheel'
        ];

    (function () {
        /**
         * 创建 ECUI 事件对象。
         * @public
         *
         * @param {string} type 事件类型
         * @param {Event} event 浏览器原生事件对象，忽略将自动填充
         */
        ///__gzip_original__UI_EVENT_CLASS
        var UI_EVENT = ui.Event = function (type, event) {
                this.type = type;

                if (event) {
                    this.pageX = event.pageX;
                    this.pageY = event.pageY;
                    this.which = event.which;
                    this.target = event.target;
                    this._oNative = event;
                }
                else {
                    this.pageX = mouseX;
                    this.pageY = mouseY;
                    this.which = keyCode;
                    this.target = DOCUMENT;
                }
            },
            UI_EVENT_CLASS = UI_EVENT.prototype,

            ecuiName = 'ecui',        // Element 中用于自动渲染的 ecui 属性名称
            isGlobalId,               // 是否自动将 ecui 的标识符全局化
            structural,               // DOM结构生成的方式，0表示填充所有内容，1表示不填充控件的class，2表示完全不填充

            flgContentBox,            // 在计算宽度与高度时，是否需要修正内填充与边框样式的影响
            flgFixedOffset,           // 在计算相对位置时，是否需要修正边框样式的影响
            scrollNarrow,             // 浏览器滚动条相对窄的一边的长度

            initRecursion = 0,        // init 操作的递归次数
            lastClientWidth,          // 浏览器之前的宽度

            plugins = {},             // 扩展组件列表
            maskElements = [],        // 遮罩层组

            mouseX,                   // 当前鼠标光标的X轴坐标
            mouseY,                   // 当前鼠标光标的Y轴坐标
            keyCode = 0,              // 当前键盘按下的键值，解决keypress与keyup中得不到特殊按键的keyCode的问题
            lastClick,                // 上一次产生点击事件的信息

            status,                   // 框架当前状态
            allControls = [],         // 全部生成的控件，供释放控件占用的内存使用
            independentControls = [], // 独立的控件，即使用create($create)方法创建的控件
            namedControls,            // 所有被命名的控件的集合
            uniqueIndex = 0,          // 控件的唯一序号
            connectedControls = {},   // 等待关联的控件集合

            activedControl,           // 当前环境下被激活的控件，即鼠标左键按下时对应的控件，直到左键松开后失去激活状态
            hoveredControl,           // 当前环境下鼠标悬停的控件
            focusedControl,           // 当前环境下拥有焦点的控件

            eventListeners = {},      // 控件事件监听描述对象

            envStack = [],            // 高优先级事件调用时，保存上一个事件环境的栈
            currEnv = {               // 当前操作的环境

                // 鼠标点击时控件如果被屏弊需要取消点击事件的默认处理，此时链接将不能提交
                click: function (event) {
                    event =wrapEvent(event);

                    //__transform__control_o
                    var control = findControl(event.target);

                    if (control && control.isDisabled()) {
                        event.preventDefault();
                    }
                },

                // 鼠标左键按下需要改变框架中拥有焦点的控件
                mousedown: function (event) {
                    if (activedControl) {
                        // 如果按下鼠标左键后，使用ALT+TAB使浏览器失去焦点然后松开鼠标左键，
                        // 需要恢复激活控件状态，第一次点击失效
                        bubble(activedControl, 'deactivate');
                        activedControl = null;
                        return;
                    }

                    event = wrapEvent(event);

                    //__transform__control_o
                    var control = event.getControl(),
                        // 修复ie下跨iframe导致的事件类型错误的问题
                        flag = ieVersion < 8 && isScrollClick(event),
                        target = control;

                    if (!(lastClick && isDblClick())) {
                        lastClick = {time: new DATE().getTime()};
                    }

                    if (control) {
                        if (flag) {
                            // IE8以下的版本，如果为控件添加激活样式，原生滚动条的操作会失效
                            // 常见的表现是需要点击两次才能进行滚动操作，而且中途不能离开控件区域
                            // 以免触发悬停状态的样式改变。
                            return;
                        }

                        for (; target; target = target.getParent()) {
                            if (target.isFocusable()) {
                                if (!(target != control && target.contain(focusedControl))) {
                                    // 允许获得焦点的控件必须是当前激活的控件，或者它没有焦点的时候才允许获得
                                    // 典型的用例是滚动条，滚动条不需要获得焦点，如果滚动条的父控件没有焦点
                                    // 父控件获得焦点，否则焦点不发生变化
                                    setFocused(target);
                                }
                                break;
                            }
                        }

                        if (!flag) {
                            // 如果不是在原生滚动条区域，进行左键按下的处理
                            mousedown(control, event);
                        }
                    }
                    else {
                        if (control = findControl(target = event.target)) {
                            // 如果点击的是失效状态的控件，检查是否需要取消文本选择
                            onselectstart(control, event);
                            // 检查是否INPUT/SELECT/TEXTAREA/BUTTON标签，需要失去焦点
                            if (target.tagName == 'INPUT' || target.tagName == 'SELECT' ||
                                    target.tagName == 'TEXTAREA' || target.tagName == 'BUTTON') {
                                timer(function () {
                                    target.blur();
                                });
                            }
                        }
                        // 点击到了空白区域，取消控件的焦点
                        setFocused();
                        // 正常情况下 activedControl 是 null，如果是down按下但未点击到控件，此值为undefined
                        activedControl = undefined;
                    }
                },

                // 鼠标移入的处理，需要计算是不是位于当前移入的控件之外，如果是需要触发移出事件
                mouseover: function (event) {
                    if (currEnv.type != 'drag' && currEnv.type != 'zoom') {
                        event = wrapEvent(event);

                        //__transform__control_o
                        var control = event.getControl(),
                            parent = getCommonParent(control, hoveredControl);

                        bubble(hoveredControl, 'mouseout', event, parent);
                        bubble(control, 'mouseover', event, parent);

                        hoveredControl = control;
                    }
                },

                mousemove: function (event) {
                    event = wrapEvent(event);

                    //__transform__control_o
                    var control = event.getControl();

                    bubble(control, 'mousemove', event);
                },

                mouseup: function (event) {
                    event = wrapEvent(event);

                    //__transform__control_o
                    var control = event.getControl(),
                        commonParent;

                    if (activedControl !== null) {
                        // 如果为 null 表示之前没有触发 mousedown 事件就触发了 mouseup，
                        // 这种情况出现在鼠标在浏览器外按下了 down 然后回浏览器区域 up，
                        // 或者是 ie 系列浏览器在触发 dblclick 之前会触发一次单独的 mouseup，
                        // dblclick 在 ie 下的事件触发顺序是 mousedown/mouseup/click/mouseup/dblclick
                        bubble(control, 'mouseup', event);

                        if (activedControl) {
                            commonParent = getCommonParent(control, activedControl);
                            bubble(commonParent, 'click', event);
                            // 点击事件在同时响应鼠标按下与弹起周期的控件上触发(如果之间未产生鼠标移动事件)
                            // 模拟点击事件是为了解决控件的 Element 进行了 remove/append 操作后 click 事件不触发的问题
                            if (lastClick) {
                                if (isDblClick() && lastClick.target == control) {
                                    bubble(commonParent, 'dblclick', event);
                                    lastClick = null;
                                }
                                else {
                                    lastClick.target = control;
                                }
                            }
                            bubble(activedControl, 'deactivate', event);
                        }

                        // 将 activeControl 的设置复位，此时表示没有鼠标左键点击
                        activedControl = null;
                    }
                }
            },

            dragEnv = { // 拖曳操作的环境
                type: 'drag',

                mousemove: function (event) {
                    event = wrapEvent(event);

                    //__transform__target_o
                    var target = currEnv.target,
                        // 计算期待移到的位置
                        expectX = target.getX() + mouseX - currEnv.x,
                        expectY = target.getY() + mouseY - currEnv.y,
                        // 计算实际允许移到的位置
                        x = MIN(MAX(expectX, currEnv.left), currEnv.right),
                        y = MIN(MAX(expectY, currEnv.top), currEnv.bottom);

                    if (triggerEvent(target, 'dragmove', event, [x, y])) {
                        target.setPosition(x, y);
                    }

                    currEnv.x = mouseX + target.getX() - expectX;
                    currEnv.y = mouseY + target.getY() - expectY;
                },

                mouseup: function (event) {
                    event = wrapEvent(event);

                    //__transform__target_o
                    var target = currEnv.target;
                    triggerEvent(target, 'dragend', event);
                    activedControl = currEnv.actived;
                    restore();

                    currEnv.mouseover(event);
                    currEnv.mouseup(event);
                }
            },

            interceptEnv = { // 强制点击拦截操作的环境
                type: 'intercept',

                mousedown: function (event) {
                    event = wrapEvent(event);

                    //__transform__target_o
                    var target = currEnv.target,
                        env = currEnv,
                        control = event.getControl();

                    lastClick = null;

                    if (!isScrollClick(event)) {
                        if (control && !control.isFocusable()) {
                            // 需要捕获但不激活的控件是最高优先级处理的控件，例如滚动条
                            mousedown(control, event);
                        }
                        else if (triggerEvent(target, 'intercept', event)) {
                            // 默认仅拦截一次，框架自动释放环境
                            restore();
                        }
                        else if (!event.cancelBubble) {
                            if (env == currEnv) {
                                // 不改变当前操作环境表示希望继续进行点击拦截操作
                                // 例如弹出菜单点击到选项上时，不自动关闭并对下一次点击继续拦截
                                if (control) {
                                    mousedown(control, event);
                                }
                            }
                            else {
                                // 手动释放环境会造成向外层环境的事件传递
                                currEnv.mousedown(event);
                            }
                        }
                    }
                }
            },

            zoomEnv = { // 缩放操作的环境
                type: 'zoom',

                mousemove: function (event) {
                    event = wrapEvent(event);

                    //__gzip_original__minWidth
                    //__gzip_original__maxWidth
                    //__gzip_original__minHeight
                    //__gzip_original__maxHeight
                    //__transform__target_o
                    var target = currEnv.target,
                        width = currEnv.width = mouseX - currEnv.x + currEnv.width,
                        height = currEnv.height = mouseY - currEnv.y + currEnv.height,
                        minWidth = currEnv.minWidth,
                        maxWidth = currEnv.maxWidth,
                        minHeight = currEnv.minHeight,
                        maxHeight = currEnv.maxHeight;

                    currEnv.x = mouseX;
                    currEnv.y = mouseY;

                    width = minWidth > width ? minWidth : maxWidth < width ? maxWidth : width;
                    height = minHeight > height ? minHeight : maxHeight < height ? maxHeight : height;

                    // 如果宽度或高度是负数，需要重新计算定位
                    target.setPosition(currEnv.left + MIN(width, 0), currEnv.top + MIN(height, 0));
                    if (triggerEvent(target, 'zoom', event)) {
                        target.setSize(ABS(width), ABS(height));
                    }
                },

                mouseup: function (event) {
                    event = wrapEvent(event);

                    //__transform__target_o
                    var target = currEnv.target;
                    triggerEvent(target, 'zoomend', event);
                    activedControl = currEnv.actived;
                    restore();

                    repaint();
                    currEnv.mouseover(event);
                    currEnv.mouseup(event);
                }
            },

            /**
             * 初始化指定的 Element 对象对应的 DOM 节点树。
             * init 方法将初始化指定的 Element 对象及它的子节点，如果这些节点拥有初始化属性(参见 getAttributeName 方法)，将按照规则为它们绑定 ECUI 控件，每一个节点只会被绑定一次，重复的绑定无效。页面加载完成时，将会自动针对 document.body 执行这个方法，相当于自动执行以下的语句：ecui.init(document.body)
             * @public
             *
             * @param {Element} el Element 对象
             */
            init = core.init = function (el) {
                if (!initEnvironment() && el) {
                    var i = 0,
                        list = [],
                        options = el.all || el.getElementsByTagName('*'),
                        elements = [el],
                        o, namedMap = {};

                    if (!(initRecursion++)) {
                        // 第一层 init 循环的时候需要关闭resize事件监听，防止反复的重入
                        detachEvent(WINDOW, 'resize', repaint);
                    }

                    for (; o = options[i++]; ) {
                        if (getAttribute(o, ecuiName)) {
                            elements.push(o);
                        }
                    }

                    for (i = 0; el = elements[i]; i++) {
                        options = getOptions(el);
                        // 以下使用 el 替代 control
                        // 在datainsight中，禁止用页面生成ecui控件
                        // 在商桥的ie7核的客户端中，有可能误走到这个分支
                        break;
                        if (o = options.type) {
                            options.main = el;
                            list.push($create(ui[toCamelCase(o.charAt(0).toUpperCase() + o.slice(1))], options));
                            if (options.id) {
                                 namedMap[options.id] = list[list.length - 1];
                            }
                        }
                    }

                    for (i = 0; o = list[i++]; ) {
                        o.cache();
                    }

                    for (i = 0; o = list[i++]; ) {
                        o.init();
                    }

                    if (!(--initRecursion)) {
                        attachEvent(WINDOW, 'resize', repaint);
                    }

                    return namedMap;
                }
            },

            /**
             * 重绘浏览器区域的控件。
             * repaint 方法在页面改变大小时自动触发，一些特殊情况下，例如包含框架的页面，页面变化时不会触发 onresize 事件，需要手工调用 repaint 函数重绘所有的控件。
             * @public
             */
            repaint = core.repaint = function () {
                var i = 0,
                    list = [],
                    widthList = [],
                    o;

                if (ieVersion) {
                    // 防止 ie6/7 下的多次重入
                    o = (isStrict ? DOCUMENT.documentElement : DOCUMENT.body).clientWidth;
                    if (lastClientWidth != o) {
                        lastClientWidth = o;
                    }
                    else {
                        // 如果高度发生变化，相当于滚动条的信息发生变化，因此需要产生scroll事件进行刷新
                        onscroll(new UI_EVENT('scroll'));
                        return;
                    }
                }

                status = REPAINT;
                o = currEnv.type;
                // 隐藏所有遮罩层
                mask(false);
                if (o != 'zoom') {
                    // 改变窗体大小需要清空拖拽状态
                    if (o == 'drag') {
                        currEnv.mouseup();
                    }
                    // 按广度优先查找所有正在显示的控件，保证子控件一定在父控件之后
                    for (o = null; o !== undefined; o = list[i++]) {
                        for (var j = 0, controls = query({parent: o}); o = controls[j++]; ) {
                            if (o.isShow() && o.isResizable()) {
                                list.push(o);
                            }
                        }
                    }

                    for (i = 0; o = list[i++]; ) {
                        // 避免在resize中调用repaint从而引起反复的reflow
                        o.repaint = blank;
                        triggerEvent(o, 'resize');
                        delete o.repaint;

                        if (ieVersion < 8) {
                            // 修复ie6/7下宽度自适应错误的问题
                            o = getStyle(j = o.getMain());
                            if (o.width == 'auto' && o.display == 'block') {
                                j.style.width = '100%';
                            }
                        }
                    }

                    if (ieVersion < 8) {
                        // 由于强制设置了100%，因此改变ie下控件的大小必须从内部向外进行
                        // 为避免多次reflow，增加一次循环
                        for (i = 0; o = list[i]; ) {
                            widthList[i++] = o.getMain().offsetWidth;
                        }
                        for (; o = list[i--]; ) {
                            o.getMain().style.width =
                                widthList[i] - (flgContentBox ? o.$getBasicWidth() * 2 : 0) + 'px';
                        }
                    }

                    for (i = 0; o = list[i++]; ) {
                        o.cache(true, true);
                    }
                    for (i = 0; o = list[i++]; ) {
                        o.$setSize(o.getWidth(), o.getHeight());
                    }
                }

                if (ieVersion < 8) {
                    // 解决 ie6/7 下直接显示遮罩层，读到的浏览器大小实际未更新的问题
                    timer(mask, 0, null, true);
                }
                else {
                    mask(true);
                }
                status = NORMAL;
            };

        /**
         * 使一个 Element 对象与一个 ECUI 控件 在逻辑上绑定。
         * 一个 Element 对象只能绑定一个 ECUI 控件，重复绑定会自动取消之前的绑定。
         * @protected
         *
         * @param {HTMLElement} el Element 对象
         * @param {ecui.ui.Control} control ECUI 控件
         */
        $bind = core.$bind = function (el, control) {
            el._cControl = control;
            el.getControl = getControlByElement;
        };

        /**
         * 清除控件的状态。
         * 控件在销毁、隐藏与失效等情况下，需要使用 $clearState 方法清除已经获得的焦点与激活等状态。
         * @protected
         *
         * @param {ecui.ui.Control} control ECUI 控件
         */
        $clearState = core.$clearState = function (control) {
            var o = control.getParent();

            loseFocus(control);
            if (control.contain(activedControl)) {
                bubble(activedControl, 'deactivate', null, activedControl = o);
            }
            if (control.contain(hoveredControl)) {
                bubble(hoveredControl, 'mouseout', null, hoveredControl = o);
            }
        };

        /**
         * 为两个 ECUI 控件 建立连接。
         * 使用页面静态初始化或页面动态初始化(参见 ECUI 使用方式)方式，控件创建时，需要的关联控件也许还未创建。$connect 方法提供将指定的函数滞后到对应的控件创建后才调用的模式。如果 targetId 对应的控件还未创建，则调用会被搁置，直到需要的控件创建成功后，再自动执行(参见 create 方法)。
         * @protected
         *
         * @param {Object} caller 发起建立连接请求的对象
         * @param {Function} func 用于建立连接的方法，即通过调用 func.call(caller, ecui.get(targetId)) 建立连接
         * @param {string} targetId 被连接的 ECUI 控件 标识符，即在标签的 ecui 属性中定义的 id 值
         */
        $connect = core.$connect = function (caller, func, targetId) {
            if (targetId) {
                var target = namedControls[targetId];
                if (target) {
                    func.call(caller, target);
                }
                else {
                    (connectedControls[targetId] = connectedControls[targetId] || [])
                        .push({func: func, caller: caller});
                }
            }
        };

        /**
         * 创建 ECUI 控件。
         * $create 方法创建控件时不会自动渲染控件。在大批量创建控件时，为了加快渲染速度，应该首先使用 $create 方法创建所有控件完成后，再批量分别调用控件的 cache、init 与 repaint 方法渲染控件。options 对象支持的属性如下：
         * id         {string} 当前控件的 id，提供给 $connect 与 get 方法使用
         * main       {HTMLElement} 与控件绑捆的 Element 对象(参见 getMain 方法)，如果忽略此参数将创建 Element 对象与控件绑捆
         * parent     {ecui.ui.Control} 父控件对象或者父 Element 对象
         * primary    {string} 控件的基本样式(参见 getMainClass 方法)，如果忽略此参数将使用主元素的 className 属性
         * @protected
         *
         * @param {Function} type 控件的构造函数
         * @param {Object} options 初始化选项(参见 ECUI 控件)
         * @return {ecui.ui.Control} ECUI 控件
         */
        $create = core.$create = function (type, options) {
            type = type.client || type;
            options = options || {};

            //__gzip_original__parent
            var i = 0,
                parent = options.parent,
                el = options.main,
                o = options.primary || '',
                className;

            options.uid = 'ecui-' + (++uniqueIndex);

            if (el) {
                if (structural) {
                    className = el.className;
                }
                else {
                    el.className = className = el.className + ' ' + o + type.agent.TYPES;
                }

                // 如果没有指定基本样式，使用控件的样式作为基本样式
                if (!o) {
                    /\s*([^\s]+)/.test(className);
                    options.primary = REGEXP.$1;
                }

                // 如果指定的元素已经初始化，直接返回
                if (el.getControl) {
                    return el.getControl();
                }
            }
            else {
                // 没有传入主元素，需要自动生成，此种情况比较少见
                el = options.main = createDom(o + type.agent.TYPES);
                if (!o) {
                    options.primary = type.agent.types[0];
                }
            }

            // 生成控件
            type = new type(el, options);

            if (parent) {
//{if 0}//
                if (parent instanceof ui.Control) {
//{else}//                if (parent instanceof UI_CONTROL) {
//{/if}//
                    type.setParent(parent);
                }
                else {
                    type.appendTo(parent);
                }
            }
            else {
                type.$setParent(findControl(getParent(type.getOuter())));
            }

            oncreate(type, options);
            independentControls.push(type);

            // 处理所有的关联操作
            if (el = connectedControls[options.id]) {
                for (connectedControls[options.id] = null; o = el[i++]; ) {
                    o.func.call(o.caller, type);
                }
            }

            return type;
        };

        /**
         * 快速创建 ECUI 控件。
         * $fastCreate 方法仅供控件生成自己的部件使用，生成的控件不在控件列表中注册，不自动刷新也不能通过 query 方法查询(参见 $create 方法)。$fastCreate 方法通过分解 Element 对象的 className 属性得到样式信息，其中第一个样式为类型样式，第二个样式为基本样式。
         * @protected
         *
         * @param {Function} type 控件的构造函数
         * @param {HTMLElement} el 控件对应的 Element 对象
         * @param {ecui.ui.Control} parent 控件的父控件
         * @param {Object} options 初始化选项(参见 ECUI 控件)
         * @return {ecui.ui.Control} ECUI 控件
         */
        $fastCreate = core.$fastCreate = function (type, el, parent, options) {
            type = type.client || type;
            options = options || {};

            options.uid = 'ecui-' + (++uniqueIndex);
            if (!options.primary) {
                /\s*([^\s]+)/.test(el.className);
                options.primary = REGEXP.$1;
            }

            type = new type(el, options);
            type.$setParent(parent);

            oncreate(type, options);

            return type;
        };

        /**
         * 添加控件的事件监听函数。
         * @public
         *
         * @param {ecui.ui.Control} control ECUI 控件
         * @param {string} name 事件名称
         * @param {Function} caller 监听函数
         */
        core.addEventListener = function (control, name, caller) {
            name = control.getUID() + name;
            (eventListeners[name] = eventListeners[name] || []).push(caller);
        };

        /**
         * 获取高度修正值(即计算 padding, border 样式对 height 样式的影响)。
         * IE 的盒子模型不完全遵守 W3C 标准，因此，需要使用 calcHeightRevise 方法计算 offsetHeight 与实际的 height 样式之间的修正值。
         * @public
         *
         * @param {CssStyle} style CssStyle 对象
         * @return {number} 高度修正值
         */
        calcHeightRevise = core.calcHeightRevise = function (style) {
            return flgContentBox ? toNumber(style.borderTopWidth) + toNumber(style.borderBottomWidth) +
                    toNumber(style.paddingTop) + toNumber(style.paddingBottom)
                : 0;
        };

        /**
         * 获取左定位修正值(即计算 border 样式对 left 样式的影响)。
         * opera 等浏览器，offsetLeft 与 left 样式的取值受到了 border 样式的影响，因此，需要使用 calcLeftRevise 方法计算 offsetLeft 与实际的 left 样式之间的修正值。
         * @public
         *
         * @param {HTMLElement} el Element 对象
         * @return {number} 左定位修正值
         */
        calcLeftRevise = core.calcLeftRevise = function (el) {
            //__transform__style_o
            var style = getStyle(el.offsetParent);
            return !firefoxVersion || style.overflow != 'visible' && getStyle(el, 'position') == 'absolute' ?
                toNumber(style.borderLeftWidth) * flgFixedOffset : 0;
        };

        /**
         * 获取上定位修正值(即计算 border 样式对 top 样式的影响)。
         * opera 等浏览器，offsetTop 与 top 样式的取值受到了 border 样式的影响，因此，需要使用 calcTopRevise 方法计算 offsetTop 与实际的 top 样式之间的修正值。
         * @public
         *
         * @param {HTMLElement} el Element 对象
         * @return {number} 上定位修正值
         */
        calcTopRevise = core.calcTopRevise = function (el) {
            //__transform__style_o
            var style = getStyle(el.offsetParent);
            return !firefoxVersion || style.overflow != 'visible' && getStyle(el, 'position') == 'absolute' ?
                toNumber(style.borderTopWidth) * flgFixedOffset : 0;
        };

        /**
         * 获取宽度修正值(即计算 padding,border 样式对 width 样式的影响)。
         * IE 的盒子模型不完全遵守 W3C 标准，因此，需要使用 calcWidthRevise 方法计算 offsetWidth 与实际的 width 样式之间的修正值。
         * @public
         *
         * @param {CssStyle} style CssStyle 对象
         * @return {number} 宽度修正值
         */
        calcWidthRevise = core.calcWidthRevise = function (style) {
            return flgContentBox ? toNumber(style.borderLeftWidth) + toNumber(style.borderRightWidth) +
                    toNumber(style.paddingLeft) + toNumber(style.paddingRight)
                : 0;
        };

        /**
         * 创建 ECUI 控件。
         * 标准的创建 ECUI 控件 的工厂方法，适用于少量创建控件，生成的控件不需要任何额外的调用即可正常的显示，对于批量创建控件，请使用 $create 方法。options 对象支持的属性如下：
         * id        {string} 当前控件的 id，提供给 $connect 与 get 方法使用
         * main      {HTMLElement} 与控件绑捆的 Element 对象(参见 getMain 方法)，如果忽略此参数将创建 Element 对象与控件绑捆
         * parent    {ecui.ui.Control} 父控件对象或者父 Element 对象
         * primary   {string} 控件的基本样式(参见 getMainClass 方法)，如果忽略此参数将使用主元素的 className 属性
         * @public
         *
         * @param {string|Function} type 控件的类型名或控件的构造函数
         * @param {Object} options 初始化选项(参见 ECUI 控件)
         * @return {ecui.ui.Control} ECUI 控件
         */
        createControl = core.create = function (type, options) {
            type = $create('string' == typeof(type) ? ui[type] : type, options);
            type.cache();
            type.init();
            return type;
        };

        /**
         * 释放 ECUI 控件及其子控件占用的内存。
         * @public
         *
         * @param {ecui.ui.Control|HTMLElement} control 需要释放的控件对象或包含控件的 Element 对象
         */
        disposeControl = core.dispose = function (control) {
            var i = allControls.length,
//{if 0}//
                type = control instanceof ui.Control,
//{else}//                type = control instanceof UI_CONTROL,
//{/if}//
                namedMap = {},
                controls = [],
                o;

            if (type) {
                $clearState(control);
            }
            else {
                o = findControl(getParent(control));
                if (focusedControl && contain(control, focusedControl.getOuter())) {
                    setFocused(o);
                }
                if (activedControl && contain(control, activedControl.getOuter())) {
                    bubble(activedControl, 'deactivate', null, activedControl = o);
                }
                if (hoveredControl && contain(control, hoveredControl.getOuter())) {
                    bubble(hoveredControl, 'mouseout', null, hoveredControl = o);
                }
            }

            for (o in namedControls) {
                namedMap[namedControls[o].getUID()] = o;
            }

            for (; i--; ) {
                o = allControls[i];
                if (type ? control.contain(o) : !!o.getOuter() && contain(control, o.getOuter())) {
                    // 需要删除的控件先放入一个集合中等待遍历结束后再删除，否则控件链将产生变化
                    controls.push(o);
                    remove(independentControls, o);
                    if (o = namedMap[o.getUID()]) {
                        delete namedControls[o];
                    }
                    allControls.splice(i, 1);
                }
            }

            for (; o = controls[++i]; ) {
                o.$dispose();
            }
        };

        /**
         * 将指定的 ECUI 控件 设置为拖拽状态。
         * 只有在鼠标左键按下时，才允许调用 drag 方法设置待拖拽的 {'controls'|menu}，在拖拽操作过程中，将依次触发 ondragstart、ondragmove 与 ondragend 事件。range 参数支持的属性如下：
         * top    {number} 控件允许拖拽到的最小Y轴坐标
         * right  {number} 控件允许拖拽到的最大X轴坐标
         * bottom {number} 控件允许拖拽到的最大Y轴坐标
         * left   {number} 控件允许拖拽到的最小X轴坐标
         * @public
         *
         * @param {ecui.ui.Control} control 需要进行拖拽的 ECUI 控件对象
         * @param {ecui.ui.Event} event 事件对象
         * @param {Object} range 控件允许拖拽的范围，省略参数时，控件默认只允许在 offsetParent 定义的区域内拖拽，如果 
         *                       offsetParent 是 body，则只允许在当前浏览器可视范围内拖拽
         */
        drag = core.drag = function (control, event, range) {
            if (event.type == 'mousedown') {
                //__gzip_original__currStyle
                var parent = control.getOuter().offsetParent,
                    style = getStyle(parent);

                // 拖拽范围默认不超出上级元素区域
                extend(dragEnv, parent.tagName == 'BODY' || parent.tagName == 'HTML' ? getView() : {
                    top: 0,
                    right: parent.offsetWidth - toNumber(style.borderLeftWidth) - toNumber(style.borderRightWidth),
                    bottom: parent.offsetHeight - toNumber(style.borderTopWidth) - toNumber(style.borderBottomWidth),
                    left: 0
                });
                extend(dragEnv, range);
                dragEnv.right = MAX(dragEnv.right - control.getWidth(), dragEnv.left);
                dragEnv.bottom = MAX(dragEnv.bottom - control.getHeight(), dragEnv.top);

                initDragAndZoom(control, event, dragEnv, 'drag');
            }
        };

        /**
         * 获取指定名称的 ECUI 控件。
         * 使用页面静态初始化或页面动态初始化(参见 ECUI 使用方式)创建的控件，如果在 ecui 属性中指定了 id，就可以通过 get 方法得到控件，也可以在 Element 对象上使用 getControl 方法。
         * @public
         *
         * @param {string} id ECUI 控件的名称，通过 Element 对象的初始化选项 id 定义
         * @return {ecui.ui.Control} 指定名称的 ECUI 控件对象，如果不存在返回 null
         */
        core.get = function (id) {
            initEnvironment();
            return namedControls[id] || null;
        };

        /**
         * 获取当前处于激活状态的 ECUI 控件。
         * 激活状态，指鼠标在控件区域左键从按下到弹起的全过程，无论鼠标移动到哪个位置，被激活的控件对象不会发生改变。处于激活状态的控件及其父控件，都具有激活状态样式。
         * @public
         *
         * @return {ecui.ui.Control} 处于激活状态的 ECUI 控件，如果不存在返回 null
         */
        getActived = core.getActived = function () {
            return activedControl || null;
        };

        /**
         * 获取当前的初始化属性名。
         * getAttributeName 方法返回页面静态初始化(参见 ECUI 使用方式)使用的属性名，通过在 BODY 节点的 data-ecui 属性中指定，默认使用 ecui 作为初始化属性名。
         * @public
         *
         * @return {string} 当前的初始化属性名
         */
        getAttributeName = core.getAttributeName = function () {
            return ecuiName;
        };

        /**
         * 获取当前处于焦点状态的控件。
         * 焦点状态，默认优先处理键盘/滚轮等特殊事件。处于焦点状态的控件及其父控件，都具有焦点状态样式。通常鼠标左键的点击将使控件获得焦点状态，之前拥有焦点状态的控件将失去焦点状态。
         * @public
         *
         * @return {ecui.ui.Control} 处于焦点状态的 ECUI 控件，如果不存在返回 null
         */
        getFocused = core.getFocused = function () {
            return focusedControl || null;
        };

        /**
         * 获取当前处于悬停状态的控件。
         * 悬停状态，指鼠标当前位于控件区域。处于悬停状态的控件及其父控件，都具有悬停状态样式。
         * @public
         *
         * @return {ecui.ui.Control} 处于悬停状态的 ECUI 控件，如果不存在返回 null
         */
        getHovered = core.getHovered = function () {
            return hoveredControl;
        };

        /**
         * 获取当前有效的键值码。
         * getKey 方法返回最近一次 keydown 事件的 keyCode/which 值，用于解决浏览器的 keypress 事件中特殊按键(例如方向键等)没有编码值的问题。
         * @public
         *
         * @return {number} 键值码
         */
        getKey = core.getKey = function () {
            return keyCode;
        };

        /**
         * 获取当前鼠标光标的页面X轴坐标或相对于控件内部区域的X轴坐标。
         * getMouseX 方法计算相对于控件内部区域的X轴坐标时，按照浏览器盒子模型的标准，需要减去 Element 对象的 borderLeftWidth 样式的值。
         * @public
         *
         * @param {ecui.ui.Control} control ECUI 控件，如果省略参数，将获取鼠标在页面的X轴坐标，否则获取鼠标相对于控件内部区域的X轴坐标
         * @return {number} X轴坐标值
         */
        getMouseX = core.getMouseX = function (control) {
            if (control) {
                control = control.getBody();
                return mouseX - getPosition(control).left - toNumber(getStyle(control, 'borderLeftWidth'));
            }
            return mouseX;
        };

        /**
         * 获取当前鼠标光标的页面Y轴坐标或相对于控件内部区域的Y轴坐标。
         * getMouseY 方法计算相对于控件内部区域的Y轴坐标时，按照浏览器盒子模型的标准，需要减去 Element 对象的 borderTopWidth 样式的值。
         * @public
         *
         * @param {ecui.ui.Control} control ECUI 控件，如果省略参数，将获取鼠标在页面的Y轴坐标，否则获取鼠标相对于控件内部区域的Y轴坐标
         * @return {number} Y轴坐标值
         */
        getMouseY = core.getMouseY = function (control) {
            if (control) {
                control = control.getBody();
                return mouseY - getPosition(control).top - toNumber(getStyle(control, 'borderTopWidth'));
            }
            return mouseY;
        };

        /**
         * 获取所有被命名的控件。
         * @public
         *
         * @return {Object} 所有被命名的控件集合
         */
        core.getNamedControls = function () {
            return extend({}, namedControls);
        };

        /**
         * 从 Element 对象中获取初始化选项对象。
         * @public
         *
         * @param {HTMLElement} el Element 对象
         * @param {string} attributeName 当前的初始化属性名(参见 getAttributeName 方法)
         * @return {Object} 初始化选项对象
         */
        getOptions = core.getOptions = function (el, attributeName) {
            attributeName = attributeName || ecuiName;

            var text = getAttribute(el, attributeName),
                options;

            if (text) {
                el.removeAttribute(attributeName);
                if (core.onparseoptions) {
                    if (options = core.onparseoptions(text)) {
                        return options;
                    }
                }

                for (
                    options = {};
                    /^(\s*;)?\s*(ext\-)?([\w\-]+)\s*(:\s*([^;\s]+(\s+[^;\s]+)*)\s*)?($|;)/.test(text);
                ) {
                    text = REGEXP["$'"];

                    el = REGEXP.$5;
                    attributeName = REGEXP.$2 ? (options.ext = options.ext || {}) : options;
                    attributeName[toCamelCase(REGEXP.$3)] =
                        !el || el == 'true' ? true : el == 'false' ? false : ISNAN(+el) ? el : +el;
                }

                return options;
            }
            else {
                return {};
            }
        };

        /**
         * 获取浏览器滚动条的厚度。
         * getScrollNarrow 方法对于垂直滚动条，返回的是滚动条的宽度，对于水平滚动条，返回的是滚动条的高度。
         * @public
         *
         * @return {number} 浏览器滚动条相对窄的一边的长度
         */
        getScrollNarrow = core.getScrollNarrow = function () {
            return scrollNarrow;
        };

        /**
         * 获取框架当前的状态。
         * getStatus 方法返回框架当前的工作状态，目前支持三类工作状态：NORMAL(正常状态)、LOADING(加载状态)与REPAINT(刷新状态)
         * @public
         *
         * @return {boolean} 框架当前的状态
         */
        getStatus = core.getStatus = function () {
            return status;
        };

        /**
         * 控件继承。
         * @public
         *
         * @param {Function} superClass 父控件类
         * @param {string} type 子控件的类型样式
         * @param {Function} preprocess 控件正式生成前对选项信息与主元素结构信息调整的预处理函数
         * @param {Function} subClass 子控件的标准构造函数，如果忽略将直接调用父控件类的构造函数
         * @return {Function} 新控件的构造函数
         */
        inheritsControl = core.inherits = function (superClass, type, preprocess, subClass) {
            var agent = function (options) {
                    return createControl(agent.client, options);
                },
                client = agent.client = function (el, options) {
                    if (agent.preprocess) {
                        el = agent.preprocess.call(this, el, options) || el;
                    }
                    if (superClass) {
                        superClass.client.call(this, el, options);
                    }
                    if (subClass) {
                        subClass.call(this, el, options);
                    }
                };

            agent.preprocess = preprocess;

            if (superClass) {
                inherits(agent, superClass);

                if (type && type.charAt(0) == '*') {
                    (agent.types = superClass.types.slice())[0] = type.slice(1);
                }
                else {
                    agent.types = (type ? [type] : []).concat(superClass.types);
                }
            }
            else {
                // ecui.ui.Control的特殊初始化设置
                agent.types = [];
            }
            agent.TYPES = ' ' + agent.types.join(' ');

            inherits(client, agent);
            client.agent = agent;

            return agent;
        };

        /**
         * 设置框架拦截之后的一次点击，并将点击事件发送给指定的 ECUI 控件。
         * intercept 方法将下一次的鼠标点击事件转给指定控件的 $intercept 方法处理，相当于拦截了一次框架的鼠标事件点击操作，框架其它的状态不会自动改变，例如拥有焦点的控件不会改变。如果 $intercept 方法不阻止冒泡，将自动调用 restore 方法。
         * @public
         *
         * @param {ecui.ui.Control} control ECUI 控件
         */
        intercept = core.intercept = function (control) {
            interceptEnv.target = control;
            setEnv(interceptEnv);
        };

        /**
         * 判断容器默认是否基于 content-box 属性进行布局。
         * isContentBox 返回的是容器默认的布局方式，针对具体的元素，需要访问 box-sizing 样式来确认它的布局方式。
         * @public
         *
         * @return {boolean} 容器是否使用 content-box 属性布局
         */
        isContentBox = core.isContentBox = function () {
            return flgContentBox;
        };

        /**
         * 使控件失去焦点。
         * loseFocus 方法不完全是 setFocused 方法的逆向行为。如果控件及它的子控件不处于焦点状态，执行 loseFocus 方法不会发生变化。如果控件或它的子控件处于焦点状态，执行 loseFocus 方法将使控件失去焦点状态，如果控件拥有父控件，此时父控件获得焦点状态。
         * @public
         *
         * @param {ecui.ui.Control} control ECUI 控件
         */
        loseFocus = core.loseFocus = function (control) {
            if (control.contain(focusedControl)) {
                setFocused(control.getParent());
            }
        };

        /**
         * 使用一个层遮罩整个浏览器可视化区域。
         * 遮罩层的 z-index 样式默认取值为 32767，请不要将 Element 对象的 z-index 样式设置大于 32767。当框架中至少一个遮罩层工作时，body 标签将增加一个样式 ecui-mask，IE6/7 的原生 select 标签可以使用此样式进行隐藏，解决强制置顶的问题。
         * @public
         *
         * @param {number} opacity 透明度，如 0.5，如果省略参数将关闭遮罩层
         * @param {number} zIndex 遮罩层的 zIndex 样式值，如果省略使用 32767
         */
        mask = core.mask = function (opacity, zIndex) {
            //__gzip_original__body
            var i = 0,
                body = DOCUMENT.body,
                o = getView(),
                // 宽度向前扩展2屏，向后扩展2屏，是为了解决翻屏滚动的剧烈闪烁问题
                // 不直接设置为整个页面的大小，是为了解决IE下过大的遮罩层不能半透明的问题
                top = MAX(o.top - o.height * 2, 0),
                left = MAX(o.left - o.width * 2, 0),
                text = ';top:' + top + 'px;left:' + left +
                    'px;width:' + MIN(o.width * 5, o.pageWidth - left) +
                    'px;height:' + MIN(o.height * 5, o.pageHeight - top) + 'px;display:';

            if ('boolean' == typeof opacity) {
                text += opacity ? 'block' : 'none'; 
                for (; o = maskElements[i++]; ) {
                    o.style.cssText += text;
                }
            }
            else if (opacity === undefined) {
                removeDom(maskElements.pop());
                if (!maskElements.length) {
                    removeClass(body, 'ecui-mask');
                }
            }
            else {
                if (!maskElements.length) {
                    addClass(body, 'ecui-mask');
                }
                maskElements.push(o = body.appendChild(createDom(
                    '',
                    'position:absolute;background-color:#000;z-index:' + (zIndex || 32767)
                )));
                setStyle(o, 'opacity', opacity);
                o.style.cssText += text + 'block';
            }
        };

        /**
         * 判断是否需要初始化 class 属性。
         * @public
         *
         * @return {boolean} 是否需要初始化 class 属性
         */
        needInitClass = core.needInitClass = function () {
            return !structural;
        };

        /**
         * 查询满足条件的控件列表。
         * query 方法允许按多种条件组合查询满足需要的控件，如果省略条件表示不进行限制。condition参数对象支持的属性如下：
         * type   {Function} 控件的类型构造函数
         * parent {ecui.ui.Control} 控件的父控件对象
         * custom {Function} 自定义查询函数，传入的参数是控件对象，query 方法会将自己的 this 指针传入查询函数中
         * @public
         *
         * @param {Object} condition 查询条件，如果省略将返回全部的控件
         * @return {Array} 控件列表
         */
        query = core.query = function (condition) {
            condition = condition || {};

            //__gzip_original__parent
            for (
                var i = 0,
                    result = [],
                    parent = condition.parent,
                    custom = condition.custom,
                    o;
                o = independentControls[i++];
            ) {
                if ((!condition.type || (o instanceof condition.type)) &&
                        (parent === undefined || (o.getParent() === parent)) &&
                        (!custom || custom.call(this, o))) {
                    result.push(o);
                }
            }

            return result;
        };

        /**
         * 移除控件的事件监听函数。
         * @public
         *
         * @param {ecui.ui.Control} control ECUI 控件
         * @param {string} name 事件名称
         * @param {Function} caller 监听函数
         */
        core.removeEventListener = function (control, name, caller) {
            if (name = eventListeners[control.getUID() + name]) {
                remove(name, caller);
            }
        };

        /**
         * 恢复当前框架的状态到上一个状态。
         * restore 用于恢复调用特殊操作如 drag、intercept 与 zoom 后改变的框架环境，包括各框架事件处理函数的恢复、控件的焦点设置等。
         * @public
         */
        restore = core.restore = function () {
            if (ieVersion) {
                if (currEnv.type == 'drag' || currEnv.type == 'zoom') {
                    // 取消IE的窗体外事件捕获，如果普通状态也设置，会导致部分区域无法点击
                    DOCUMENT.body.releaseCapture();
                }
            }
            setHandler(currEnv, true);
            setHandler(currEnv = envStack.pop());
        };

        /**
         * 使 ECUI 控件 得到焦点。
         * setFocused 方法将指定的控件设置为焦点状态，允许不指定需要获得焦点的控件，则当前处于焦点状态的控件将失去焦点，需要将处于焦点状态的控件失去焦点还可以调用 loseFocus 方法。如果控件处于失效状态，设置它获得焦点状态将使所有控件失去焦点状态。需要注意的是，如果控件处于焦点状态，当通过 setFocused 方法设置它的子控件获得焦点状态时，虽然处于焦点状态的控件对象发生了变化，但是控件不会触发 onblur 方法，此时控件逻辑上仍然处于焦点状态。
         * @public
         *
         * @param {ecui.ui.Control} control ECUI 控件
         */
        setFocused = core.setFocused = function (control) {
            if (control && control.isDisabled()) {
                // 处于失效状态的控件不允许获得焦点状态
                control = null;
            }

            var parent = getCommonParent(focusedControl, control);

            bubble(focusedControl, 'blur', null, parent);
            bubble(focusedControl = control, 'focus', null, parent);
        };

        /**
         * 触发事件。
         * triggerEvent 会根据事件返回值或 event 的新状态决定是否触发默认事件处理。
         * @public
         *
         * @param {ecui.ui.Control} control 控件对象
         * @param {string} name 事件名
         * @param {ecui.ui.Event} event 事件对象，可以为 false 表示直接阻止默认事件处理
         * @param {Array} args 事件的其它参数
         * @return {boolean} 是否阻止默认事件处理
         */
        triggerEvent = core.triggerEvent = function (control, name, event, args) {
            if (args && event) {
                args.splice(0, 0, event);
            }
            else if (event) {
                args = [event];
            }
            else {
                event = {returnValue: event, preventDefault: UI_EVENT_CLASS.preventDefault};
                args = args || [];
            }

            if (listeners = eventListeners[control.getUID() + name]) {
                for (var i = 0, listeners, o; o = listeners[i++]; ) {
                    o.apply(control, args);
                }
            }

            if ((control['on' + name] && control['on' + name].apply(control, args) === false) ||
                    event.returnValue === false ||
                    (control['$' + name] && control['$' + name].apply(control, args) === false)) {
                event.preventDefault();
            }

            return event.returnValue !== false;
        };

        /**
         * 包装事件对象。
         * event 方法将浏览器产生的鼠标与键盘事件标准化并添加 ECUI 框架需要的信息到事件对象中。标准化的属性如下：
         * pageX           {number} 鼠标的X轴坐标
         * pageY           {number} 鼠标的Y轴坐标
         * which           {number} 触发事件的按键码
         * target          {HTMLElement} 触发事件的 Element 对象
         * returnValue     {boolean}  是否进行默认处理
         * cancelBubble    {boolean}  是否取消冒泡
         * exit            {Function} 终止全部事件操作
         * getControl      {Function} 获取触发事件的 ECUI 控件 对象
         * getNative       {Function} 获取原生的事件对象
         * preventDefault  {Function} 阻止事件的默认处理
         * stopPropagation {Function} 事件停止冒泡
         * @public
         *
         * @param {Event} event 事件对象
         * @return {ecui.ui.Event} 标准化后的事件对象
         */
        wrapEvent = core.wrapEvent = function (event) {
            if (event instanceof UI_EVENT) {
                // 防止事件对象被多次包装
                return event;
            }

            var body = DOCUMENT.body,
                html = getParent(body);

            if (ieVersion) {
                event = WINDOW.event;
                event.pageX = html.scrollLeft + body.scrollLeft - html.clientLeft + event.clientX - body.clientLeft;
                event.pageY = html.scrollTop + body.scrollTop - html.clientTop + event.clientY - body.clientTop;
                event.target = event.srcElement;
                event.which = event.keyCode;
            }

            if (event.type == 'mousemove') {
                lastClick = null;
            }
            mouseX = event.pageX;
            mouseY = event.pageY;

            return new UI_EVENT(event.type, event);
        };

        /**
         * 将指定的 ECUI 控件 设置为缩放状态。
         * zoom 方法将控件设置为缩放，缩放的值允许负数，用于表示反向的缩放，调用它会触发控件对象的 onzoomstart 事件，在整个 zoom 的周期中，还将触发 onzoom 与 onzoomend 事件，在释放鼠标按键时缩放操作周期结束。range 参数支持的属性如下：
         * minWidth  {number} 控件允许缩放的最小宽度 
         * maxWidth  {number} 控件允许缩放的最大宽度 
         * minHeight {number} 控件允许缩放的最小高度 
         * maxHeight {number} 控件允许缩放的最大高度 
         * @public
         *
         * @param {ecui.ui.Control} control ECUI 控件
         * @param {ecui.ui.Event} event 事件对象
         * @param {Object} range 控件允许的缩放范围参数
         */
        core.zoom = function (control, event, range) {
            if (event.type == 'mousedown') {
                // 保存现场环境
                if (range) {
                    extend(zoomEnv, range);
                }
                zoomEnv.top = control.getY();
                zoomEnv.left = control.getX();
                zoomEnv.width = control.getWidth();
                zoomEnv.height = control.getHeight();

                initDragAndZoom(control, event, zoomEnv, 'zoom');
            }
        };

        /**
         * 键盘事件处理。
         * @private
         *
         * @param {Event} event 事件对象
         */
        currEnv.keydown = currEnv.keypress = currEnv.keyup = function (event) {
            event = wrapEvent(event);

            //__gzip_original__type
            //__gzip_original__which
            var type = event.type,
                which = event.which;

            if (type == 'keydown') {
                keyCode = which;
            }
            bubble(focusedControl, type, event);
            if (type == 'keyup' && keyCode == which) {
                // 一次多个键被按下，只有最后一个被按下的键松开时取消键值码
                keyCode = 0;
            }
        };

        /**
         * 双击事件与选中内容开始事件处理。
         * @private
         *
         * @param {Event} event 事件对象
         */
        if (ieVersion) {
            // IE下双击事件不依次产生 mousedown 与 mouseup 事件，需要模拟
            currEnv.dblclick = function (event) {
                currEnv.mousedown(event);
                currEnv.mouseup(event);
            };

            // IE下取消对文字的选择不能仅通过 mousedown 事件进行
            currEnv.selectstart = function (event) {
                event = wrapEvent(event);
                onselectstart(findControl(event.target), event);
            };
        }

        /**
         * 滚轮事件处理。
         * @private
         *
         * @param {Event} event 事件对象
         */
        currEnv[firefoxVersion ? 'DOMMouseScroll' : 'mousewheel'] = function (event) {
            event = wrapEvent(event);
            
            event.detail =
                event._oNative.wheelDelta !== undefined ? event._oNative.wheelDelta / -40 : event._oNative.detail;

            // 拖拽状态下，不允许滚动
            if (currEnv.type == 'drag') {
                event.preventDefault();
            }
            else {
                bubble(hoveredControl, 'mousewheel', event);
                if (!event.cancelBubble) {
                    bubble(focusedControl, 'mousewheel', event);
                }
            }
        };

        /**
         * 获取触发事件的 ECUI 控件 对象
         * @public
         *
         * @return {ecui.ui.Control} 控件对象
         */
        UI_EVENT_CLASS.getControl = function () {
            var o = findControl(this.target);
            if (o && !o.isDisabled()) {
                for (; o; o = o.getParent()) {
                    if (o.isCapturable()) {
                        return o;
                    }
                }
            }
            return null;
        };

        /**
         * 获取原生的事件对象。
         * @public
         *
         * @return {Object} 原生的事件对象
         */
        UI_EVENT_CLASS.getNative = function () {
            return this._oNative;
        };

        /**
         * 阻止事件的默认处理。
         * @public
         */
        UI_EVENT_CLASS.preventDefault = function () {
            this.returnValue = false;
            if (this._oNative) {
                if (ieVersion) {
                    this._oNative.returnValue = false;
                }
                else {
                    this._oNative.preventDefault();
                }
            }
        };

        /**
         * 事件停止冒泡。
         * @public
         */
        UI_EVENT_CLASS.stopPropagation = function () {
            this.cancelBubble = true;
            if (this._oNative) {
                if (ieVersion) {
                    this._oNative.cancelBubble = false;
                }
                else {
                    this._oNative.stopPropagation();
                }
            }
        };

        /**
         * 终止全部事件操作。
         * @public
         */
        UI_EVENT_CLASS.exit = function () {
            this.preventDefault();
            this.stopPropagation();
        };

        /**
         * 冒泡处理控件事件。
         * @private
         *
         * @param {ecui.ui.Control} start 开始冒泡的控件
         * @param {string} type 事件类型
         * @param {ecui.ui.Event} 事件对象
         * @param {ecui.ui.Control} end 终止冒泡的控件，如果不设置将一直冒泡至顶层
         */
        function bubble(start, type, event, end) {
            event = event || new UI_EVENT(type);
            event.cancelBubble = false;
            for (; start != end; start = start.getParent()) {
                event.returnValue = undefined;
                triggerEvent(start, type, event);
                if (event.cancelBubble) {
                    return;
                }
            }
        }

        /**
         * 获取两个控件的公共父控件。
         * @private
         *
         * @param {ecui.ui.Control} control1 控件1
         * @param {ecui.ui.Control} control2 控件2
         * @return {ecui.ui.Control} 公共的父控件，如果没有，返回 null
         */
        function getCommonParent(control1, control2) {
            if (control1 != control2) {
                var i = 0,
                    list1 = [],
                    list2 = [];

                for (; control1; control1 = control1.getParent()) {
                    list1.push(control1);
                }
                for (; control2; control2 = control2.getParent()) {
                    list2.push(control2);
                }

                list1.reverse();
                list2.reverse();

                // 过滤父控件序列中重复的部分
                for (; list1[i] == list2[i]; i++) {}
                control1 = list1[i - 1];
            }

            return control1 || null;
        }

        /**
         * 获取当前 Element 对象绑定的 ECUI 控件。
         * 与控件关联的 Element 对象(例如通过 init 方法初始化，或者使用 $bind 方法绑定，或者使用 create、$fastCreate 方法生成控件)，会被添加一个 getControl 方法用于获取它绑定的 ECUI 控件，更多获取控件的方法参见 get。
         * @private
         *
         * @return {ecui.ui.Control} 与 Element 对象绑定的 ECUI 控件
         */
        function getControlByElement() {
            return this._cControl;
        }

        /**
         * 初始化拖拽与缩放操作的环境。
         * @private
         *
         * @param {ecui.ui.Control} control 需要操作的控件
         * @param {ecui.ui.Event} event 事件对象
         * @param {Object} env 操作环境对象
         * @return {string} type 操作的类型，只能是drag或者zoom
         */
        function initDragAndZoom(control, event, env, type) {
            var currStyle = control.getOuter().style,
                // 缓存，防止多次reflow
                x = control.getX(),
                y = control.getY();
            currStyle.left = x + 'px';
            currStyle.top = y + 'px';
            currStyle.position = 'absolute';

            env.target = control;
            env.actived = activedControl;
            setEnv(env);

            // 清除激活的控件，在drag中不需要针对激活控件移入移出的处理
            activedControl = null;

            triggerEvent(control, type + 'start', event);

            if (ieVersion) {
                // 设置IE的窗体外事件捕获，如果普通状态也设置，会导致部分区域无法点击
                DOCUMENT.body.setCapture();
            }
        }

        /**
         * 初始化ECUI工作环境。
         * @private
         *
         * @return {boolean} 是否执行了初始化操作
         */
        function initEnvironment() {
            if (!namedControls) {
                status = LOADING;

                // 自动加载插件
                for (o in ext) {
                    plugins[o] = ext[o];
                }

                // 设置全局事件处理
                for (o in currEnv) {
                    attachEvent(DOCUMENT, o, currEnv[o]);
                }

                namedControls = {};

                var o = getOptions(DOCUMENT.body, 'data-ecui');

                ecuiName = o.name || ecuiName;
                isGlobalId = o.globalId;
                structural = indexOf(['class', 'all'], o.structural) + 1;

                insertHTML(
                    DOCUMENT.body,
                    'BEFOREEND',
                    '<div style="position:absolute;overflow:scroll;top:-90px;left:-90px;width:80px;height:80px;' +
                        'border:1px solid"><div style="position:absolute;top:0px;height:90px"></div></div>'
                );
                // 检测Element宽度与高度的计算方式
                o = DOCUMENT.body.lastChild;
                flgContentBox = o.offsetWidth > 80;
                flgFixedOffset = o.lastChild.offsetTop;
                scrollNarrow = o.offsetWidth - o.clientWidth - 2;
                removeDom(o);

                attachEvent(WINDOW, 'resize', repaint);
                attachEvent(WINDOW, 'unload', function () {
                    for (var i = 0; o = allControls[i++]; ) {
                        o.$dispose();
                    }

                    // 清除闭包中引用的 Element 对象
                    DOCUMENT = maskElements = null;
                });
                attachEvent(WINDOW, 'scroll', onscroll);

                init(DOCUMENT.body);
                //TODO:更改BODY属性
                addClass(DOCUMENT.body, 'ecui-loaded');
                DOCUMENT.body.setAttribute("id","body-white");
                status = NORMAL;
                return true;
            }
        }

        /**
         * 判断是否为允许的双击时间间隔。
         * @private
         *
         * @return {boolean} 是否为允许的双击时间间隔
         */
        function isDblClick() {
            return lastClick.time > new DATE().getTime() - 200;
        }

        /**
         * 判断点击是否发生在滚动条区域。
         * @private
         *
         * @param {ecui.ui.Event} event 事件对象
         * @return {boolean} 点击是否发生在滚动条区域
         */
        function isScrollClick(event) {
            var target = event.target,
                pos = getPosition(target),
                style = getStyle(target);
            return event.pageX - pos.left - toNumber(style.borderLeftWidth) >= target.clientWidth !=
                event.pageY - pos.top - toNumber(style.borderTopWidth) >= target.clientHeight;
        }

        /**
         * 处理鼠标点击。
         * @private
         *
         * @param {ecui.ui.Control} control 需要操作的控件
         * @param {ecui.ui.Event} event 事件对象
         */
        function mousedown(control, event) {
            bubble(activedControl = control, 'activate', event);
            bubble(control, 'mousedown', event);
            onselectstart(control, event);
        }

        /**
         * 控件对象创建后的处理。
         * @private
         *
         * @param {ecui.ui.Control} control 
         * @param {Object} options 控件初始化选项
         */
        function oncreate(control, options) {
            if (control.oncreate) {
                control.oncreate(options);
            }
            allControls.push(control);

            if (options.id) {
                namedControls[options.id] = control;
                if (isGlobalId) {
                    WINDOW[options.id] = control;
                }
            }

            if (options.ext) {
                for (var o in options.ext) {
                    if (plugins[o]) {
                        plugins[o](control, options.ext[o], options);
                        if (o = control['$init' + o.charAt(0).toUpperCase() + toCamelCase(o.slice(1))]) {
                            o.call(control, options);
                        }
                    }
                }
            }
        }

        /**
         * 窗体滚动时的事件处理。
         * @private
         */
        function onscroll(event) {
            event = wrapEvent(event);
            for (var i = 0, o; o = independentControls[i++]; ) {
                triggerEvent(o, 'pagescroll', event);
            }
            mask(true);
        }

        /**
         * 文本选择开始处理。
         * @private
         *
         * @param {ecui.ui.Control} control 需要操作的控件
         * @param {ecui.ui.Event} event 事件对象
         */
        function onselectstart(control, event) {
            for (; control; control = control.getParent()) {
                if (!control.isUserSelect()) {
                    event.preventDefault();
                    return;
                }
            }
        }

        /**
         * 设置 ecui 环境。
         * @private
         *
         * @param {Object} env 环境描述对象
         */
        function setEnv(env) {
            var o = {};
            setHandler(currEnv, true);

            extend(o, currEnv);
            extend(o, env);
            o.x = mouseX;
            o.y = mouseY;
            setHandler(o);

            envStack.push(currEnv);
            currEnv = o;
        }

        /**
         * 设置document节点上的鼠标事件。
         * @private
         *
         * @param {Object} env 环境描述对象，保存当前的鼠标光标位置与document上的鼠标事件等
         * @param {boolean} remove 如果为true表示需要移除data上的鼠标事件，否则是添加鼠标事件
         */
        function setHandler(env, remove) {
            for (var i = 0, func = remove ? detachEvent : attachEvent, o; i < 5; ) {
                if (env[o = eventNames[i++]]) {
                    func(DOCUMENT, o, env[o]);
                }
            }
        }

        ready(init);
    })();
//{/if}//
//{if 0}//
})();
//{/if}//
