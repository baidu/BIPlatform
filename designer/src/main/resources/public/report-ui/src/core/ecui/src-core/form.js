/*
Form - 定义独立于文档布局的内容区域的基本操作。
窗体控件，继承自基础控件，仿真浏览器的多窗体效果，如果在其中包含 iframe 标签，可以在当前页面打开一个新的页面，避免了使用 window.open 在不同浏览器下的兼容性问题。多个窗体控件同时工作时，当前激活的窗体在最上层。窗体控件的标题栏默认可以拖拽，窗体可以设置置顶方式显示，在置顶模式下，只有当前窗体可以响应操作。窗体控件的 z-index 从4096开始，页面开发请不要使用大于或等于4096的 z-index 值。

窗体控件直接HTML初始化的例子:
<div ecui="type:form;hide:true">
  <label>窗体的标题</label>
  <!-- 这里放窗体的内容 -->
  ...
</div>

属性
_bFlag          - 初始是否自动隐藏/是否使用showModal激活
_bAutoTitle     - 标题栏是否自适应宽度
_bAutoHeight    - 高度是否自适应
_bAutoCenter    - 显示时位置是否自动居中
_uTitle         - 标题栏
_uClose         - 关闭按钮
*/
//{if 0}//
(function () {

    var core = ecui,
        array = core.array,
        dom = core.dom,
        ui = core.ui,
        string = core.string,
        util = core.util,

        undefined,
        MATH = Math,
        MAX = MATH.max,

        indexOf = array.indexOf,
        children = dom.children,
        createDom = dom.create,
        first = dom.first,
        getStyle = dom.getStyle,
        moveElements = dom.moveElements,
        encodeHTML = string.encodeHTML,
        getView = util.getView,

        $fastCreate = core.$fastCreate,
        calcHeightRevise = core.calcHeightRevise,
        calcWidthRevise = core.calcWidthRevise,
        drag = core.drag,
        inheritsControl = core.inherits,
        loseFocus = core.loseFocus,
        mask = core.mask,
        setFocused = core.setFocused,

        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = ui.Control.prototype,
        UI_BUTTON = ui.Button;
//{/if}//
//{if $phase == "define"}//
    ///__gzip_original__UI_FORM
    ///__gzip_original__UI_FORM_CLASS
    /**
     * 初始化窗体控件。
     * options 对象支持的属性如下：
     * hide         初始是否自动隐藏
     * autoTitle    title是否自适应宽度，默认自适应宽度
     * autoCenter   显示时位置是否自动居中，默认不处理
     * @public
     *
     * @param {Object} options 初始化选项
     */
    var UI_FORM = ui.Form =
        inheritsControl(
            UI_CONTROL,
            'ui-form',
            function (el, options) {
                // 生成标题控件与内容区域控件对应的Element对象
                var type = this.getType(),
                    o = createDom(type + '-body', 'position:relative;overflow:auto'),
                    titleEl = first(el);

                moveElements(el, o, true);

                if (titleEl && titleEl.tagName == 'LABEL') {
                    el.innerHTML =
                        '<div class="' + type + '-close' + this.Close.TYPES + '" style="position:absolute"></div>';
                    el.insertBefore(titleEl, el.firstChild);
                    titleEl.className = type + '-title' + this.Title.TYPES;
                    titleEl.style.position = 'absolute';
                }
                else {
                    el.innerHTML =
                        '<label class="' + type + '-title' + this.Title.TYPES +
                            '" style="position:absolute">'+ (options.title ? encodeHTML(options.title) : '') +'</label><div class="' + type + '-close' + this.Close.TYPES +
                            '" style="position:absolute"></div>';
                    titleEl = el.firstChild;
                }

                el.style.overflow = 'hidden';
                el.appendChild(o);
            },
            function (el, options) {
                this._bAutoHeight = !el.style.height;
                el = children(el);

                this._bFlag = options.hide;
                this._bAutoTitle = options.autoTitle !== false;
                this._bAutoCenter = options.autoCenter === true;

                // 初始化标题区域
                this._uTitle = $fastCreate(this.Title, el[0], this, {userSelect: false});

                // 初始化关闭按钮
                this._uClose = $fastCreate(this.Close, el[1], this);
                if (options.closeButton === false) {
                    this._uClose.$show();
                }

                this.$setBody(el[2]);
            }
        ),
        UI_FORM_CLASS = UI_FORM.prototype,

        /**
         * 初始化窗体控件的标题栏部件。
         * @public
         *
         * @param {Object} options 初始化选项
         */
        UI_FORM_TITLE_CLASS = (UI_FORM_CLASS.Title = inheritsControl(UI_CONTROL)).prototype,

        /**
         * 初始化窗体控件的关闭按钮部件。
         * @public
         *
         * @param {Object} options 初始化选项
         */
        UI_FORM_CLOSE_CLASS = (UI_FORM_CLASS.Close = inheritsControl(UI_BUTTON)).prototype,

        UI_FORM_ALL = [],   // 当前显示的全部窗体
        UI_FORM_MODAL = 0;  // 当前showModal的窗体数
//{else}//
    /**
     * 刷新所有显示的窗体的zIndex属性。
     * @protected
     *
     * @param {ecui.ui.Form} form 置顶显示的窗体
     */
    function UI_FORM_FLUSH_ZINDEX(form) {
        UI_FORM_ALL.push(UI_FORM_ALL.splice(indexOf(UI_FORM_ALL, form), 1)[0]);

        // 改变当前窗体之后的全部窗体z轴位置，将当前窗体置顶
        for (var i = 0, j = UI_FORM_ALL.length - UI_FORM_MODAL, o; o = UI_FORM_ALL[i++]; ) {
            o.getOuter().style.zIndex = i > j ? 32767 + (i - j) * 2 : 4095 + i;
        }
    }

    /**
     * 标题栏激活时触发拖动，如果当前窗体未得到焦点则得到焦点。
     * @override
     */
    UI_FORM_TITLE_CLASS.$activate = function (event) {
        UI_CONTROL_CLASS.$activate.call(this, event);
        drag(this.getParent(), event);
    };

    /**
     * 窗体关闭按钮点击关闭窗体。
     * @override
     */
    UI_FORM_CLOSE_CLASS.$click = function (event) {
        UI_CONTROL_CLASS.$click.call(this, event);
        this.getParent().hide();
    };

    /**
     * @override
     */
    UI_FORM_CLASS.$cache = function (style, cacheSize) {
        UI_CONTROL_CLASS.$cache.call(this, style, cacheSize);

        style = getStyle(this.getMain().lastChild);
        this.$$bodyWidthRevise = calcWidthRevise(style);
        this.$$bodyHeightRevise = calcHeightRevise(style);
        this._uTitle.cache(true, true);
        this._uClose.cache(true, true);
    };

    /**
     * 销毁窗体时需要先关闭窗体，并不再保留窗体的索引。
     * @override
     */
    UI_FORM_CLASS.$dispose = function () {
        if (indexOf(UI_FORM_ALL, this) >= 0) {
            // 窗口处于显示状态，需要强制关闭
            // 避免在unload时子元素已经被dispose导致getOuter函数报错
            try {
                this.$hide();
            }
            catch(e) {}
        }
        UI_CONTROL_CLASS.$dispose.call(this);
    };

    /**
     * 窗体控件获得焦点时需要将自己置于所有窗体控件的顶部。
     * @override
     */
    UI_FORM_CLASS.$focus = function () {
        UI_CONTROL_CLASS.$focus.call(this);
        UI_FORM_FLUSH_ZINDEX(this);
    };

    /**
     * 窗体隐藏时将失去焦点状态，如果窗体是以 showModal 方式打开的，隐藏窗体时，需要恢复页面的状态。
     * @override
     */
    UI_FORM_CLASS.$hide = function () {
        // showModal模式下隐藏窗体需要释放遮罩层
        var i = indexOf(UI_FORM_ALL, this);
        if (i >= 0) {
            UI_FORM_ALL.splice(i, 1);
        }

        if (i > UI_FORM_ALL.length - UI_FORM_MODAL) {
            if (this._bFlag) {
                if (i == UI_FORM_ALL.length) {
                    mask();
                }
                else {
                    // 如果不是最后一个，将遮罩层标记后移
                    UI_FORM_ALL[i]._bFlag = true;
                }
                this._bFlag = false;
            }
            UI_FORM_MODAL--;
        }

        UI_CONTROL_CLASS.$hide.call(this);
        loseFocus(this);
    };

    /**
     * @override
     */
    UI_FORM_CLASS.$setSize = function (width, height) {
        if (this._bAutoHeight) {
            height = null;
        }
        UI_CONTROL_CLASS.$setSize.call(this, width, height);
        this.$locate();

        var style = this.getMain().lastChild.style;

        style.width = this.getBodyWidth() + 'px';
        if (!this._bAutoHeight) {
            style.height = this.getBodyHeight() + 'px';
        }
        if (this._bAutoTitle) {
            this._uTitle.$setSize(this.getWidth() - this.$getBasicWidth());
        }
    };

    /**
     * 窗体显示时将获得焦点状态。
     * @override
     */
    UI_FORM_CLASS.$show = function () {
        UI_FORM_ALL.push(this);
        UI_CONTROL_CLASS.$show.call(this);
        setFocused(this);
    };

    /**
     * 窗体居中显示。
     * @public
     */
    UI_FORM_CLASS.center = function () {
        o = this.getOuter();
        o.style.position = this.$$position = 'absolute';
        o = o.offsetParent;

        if (!o || o.tagName == 'BODY' || o.tagName == 'HTML') {
            var o = getView(),
                x = o.right + o.left,
                y = o.bottom + o.top;
        }
        else {
            x = o.offsetWidth;
            y = o.offsetHeight;
        }

        this.setPosition(MAX((x - this.getWidth()) / 2, 0), MAX((y - this.getHeight()) / 2, 0));
    };

    /**
     * 如果窗体是以 showModal 方式打开的，只有位于最顶层的窗体才允许关闭。
     * @override
     */
    UI_FORM_CLASS.hide = function () {
        for (var i = indexOf(UI_FORM_ALL, this), o; o = UI_FORM_ALL[++i]; ) {
            if (o._bFlag) {
                return false;
            }
        }
        return UI_CONTROL_CLASS.hide.call(this);
    };

    /**
     * @override
     */
    UI_FORM_CLASS.init = function () {
        UI_CONTROL_CLASS.init.call(this);
        this._uTitle.init();
        this._uClose.init();
        if (this._bFlag) {
            this._bFlag = false;
            this.$hide();
        }
        else {
            this.$show();
        }
    };

    /**
     * 设置窗体控件标题。
     * @public
     *
     * @param {string} text 窗体标题
     */
    UI_FORM_CLASS.setTitle = function (text) {
        this._uTitle.setContent(text || '');
    };

    /**
     * @override
     */
    UI_FORM_CLASS.show = function () {
        if (UI_FORM_MODAL && indexOf(UI_FORM_ALL, this) < UI_FORM_ALL.length - UI_FORM_MODAL) {
            // 如果已经使用showModal，对原来不是showModal的窗体进行处理
            UI_FORM_MODAL++;
        }

        var result = UI_CONTROL_CLASS.show.call(this);
        if (!result) {
            UI_FORM_FLUSH_ZINDEX(this);
        }
        else if (this._bAutoCenter) {
            this.center();
        }

        return result;
    };

    /*
     * @override
     */
    UI_FORM_CLASS.$resize = function () {
        var style = this.getMain().lastChild.style; 

        UI_CONTROL_CLASS.$resize.call(this);
        style.width = '';
        style.height = '';
    };

    /**
     * override
     * 自适应高度时getHeight需要实时计算
     */
    UI_FORM_CLASS.getHeight = function () {
        if (this._bAutoHeight) {
            this.cache(true, true);
        }
        return UI_CONTROL_CLASS.getHeight.call(this);
    }

    /**
     * 窗体以独占方式显示
     * showModal 方法将窗体控件以独占方式显示，此时鼠标点击窗体以外的内容无效，关闭窗体后自动恢复。
     * @public
     *
     * @param {number} opacity 遮罩层透明度，默认为0.05
     */
    UI_FORM_CLASS.showModal = function (opacity) {
        if (!this._bFlag) {
            if (indexOf(UI_FORM_ALL, this) < UI_FORM_ALL.length - UI_FORM_MODAL) {
                UI_FORM_MODAL++;
            }

            mask(opacity !== undefined ? opacity : 0.05, 32766 + UI_FORM_MODAL * 2);

            this._bFlag = true;
            if (!UI_CONTROL_CLASS.show.call(this)) {
                UI_FORM_FLUSH_ZINDEX(this);
            }
            else if (this._bAutoCenter) {
                this.center(); 
            }
        }
    };
//{/if}//
//{if 0}//
})();
//{/if}//
