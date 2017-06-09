/*
PopupMenu - 定义弹出菜单项的基本操作。
弹出菜单控件，继承自基础控件，实现了选项组接口。弹出式菜单操作时不会改变当前已经激活的对象，任何点击都将导致弹出菜单消失，弹出菜单默认向右展开子菜单，如果右部已经到达浏览器最边缘，将改为向左显示。

弹出菜单控件直接HTML初始化的例子:
<div ecui="type:popup">
  <!-- 这里放选项内容 -->
  ...
  <div>菜单项</div>
  <!-- 包含子菜单项的菜单项 -->
  <div>
    <label>菜单项</label>
    <!-- 这里放子菜单项 -->
    ...
    <div>子菜单项</div>
  </div>
</div>

属性
_nOptionSize - 弹出菜单选项的显示数量，不设置将全部显示
_uPrev       - 向上滚动按钮
_uNext       - 向下滚动按钮

子菜单项属性
_cSubPopup   - 下级弹出菜单的引用
*/
//{if 0}//
(function () {

    var core = ecui,
        array = core.array,
        dom = core.dom,
        ui = core.ui,
        util = core.util,

        DOCUMENT = document,
        MATH = Math,
        MAX = MATH.max,
        MIN = MATH.min,

        indexOf = array.indexOf,
        createDom = dom.create,
        first = dom.first,
        getParent = dom.getParent,
        getPosition = dom.getPosition,
        moveElements = dom.moveElements,
        removeDom = dom.remove,
        blank = util.blank,
        extend = util.extend,
        getView = util.getView,
        toNumber = util.toNumber,

        $fastCreate = core.$fastCreate,
        inheritsControl = core.inherits,
        intercept = core.intercept,
        restore = core.restore,

        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = UI_CONTROL.prototype,
        UI_BUTTON = ui.Button,
        UI_BUTTON_CLASS = UI_BUTTON.prototype,
        UI_ITEM = ui.Item,
        UI_ITEM_CLASS = UI_ITEM.prototype,
        UI_ITEMS = ui.Items;
//{/if}//
//{if $phase == "define"}//
    /**
     * 初始化弹出菜单控件。
     * options 对象支持的属性如下：
     * optionSize 弹出菜单选项的显示数量，不设置将全部显示
     * @public
     *
     * @param {Object} options 初始化选项
     */
    ///__gzip_original__UI_POPUP_MENU
    ///__gzip_original__UI_POPUP_MENU_BUTTON
    ///__gzip_original__UI_POPUP_MENU_ITEM
    var UI_POPUP_MENU = ui.PopupMenu =
        inheritsControl(
            UI_CONTROL,
            'ui-popup',
            null,
            function (el, options) {
                //__gzip_original__buttonParams
                var type = this.getType();

                removeDom(el);
                el.style.cssText += ';position:absolute;overflow:hidden';
                if (this._nOptionSize = options.optionSize) {
                    var o = createDom(type + '-body', 'position:absolute;top:0px;left:0px');

                    moveElements(el, o);

                    el.innerHTML =
                        '<div class="' + type + '-prev' + this.Button.TYPES +
                            '" style="position:absolute;top:0px;left:0px"></div><div class="' +
                            type + '-next' + this.Button.TYPES + '" style="position:absolute"></div>';

                    this.$setBody(el.insertBefore(o, el = el.firstChild));

                    this._uPrev = $fastCreate(this.Button, el, this);
                    this._uNext = $fastCreate(this.Button, el.nextSibling, this);
                }

                // 初始化菜单项
                this.$initItems();
            }
        ),
        UI_POPUP_MENU_CLASS = UI_POPUP_MENU.prototype,

        /**
         * 初始化弹出菜单控件的按钮部件。
         * @public
         *
         * @param {Object} options 初始化选项
         */
        UI_POPUP_MENU_BUTTON_CLASS = (UI_POPUP_MENU_CLASS.Button = inheritsControl(
            UI_BUTTON,
            null,
            function (el, options) {
                options.userSelect = options.focusable = false;
            }
        )).prototype,

        /**
         * 初始化弹出菜单控件的选项部件。
         * @public
         *
         * @param {Object} options 初始化选项
         */
        UI_POPUP_MENU_ITEM_CLASS = (UI_POPUP_MENU_CLASS.Item = inheritsControl(
            UI_ITEM,
            null,
            null,
            function (el, options) {
                var o = first(el),
                    tmpEl;

                if (o && o.tagName == 'LABEL') {
                    moveElements(el, tmpEl = createDom(options.parent.getPrimary() + UI_POPUP_MENU.TYPES));
                    el.appendChild(o);
                    this._cSubPopup = $fastCreate(UI_POPUP_MENU, tmpEl, this, extend({}, options));
                }

                UI_POPUP_MENU_ITEM_FLUSH(this);
            }
        )).prototype,

        UI_POPUP_MENU_CHAIN = [];
//{else}//
    /**
     * 弹出菜单选项样式刷新。
     * @private
     *
     * @param {ecui.ui.PopupMenu.Item} item 选项控件
     */
    function UI_POPUP_MENU_ITEM_FLUSH(item) {
        if (item) {
            item.setClass(item.getPrimary() + (item.getItems().length ? '-branch' : ''));
        }
    }

    extend(UI_POPUP_MENU_CLASS, UI_ITEMS);

    /**
     * @override
     */
    UI_POPUP_MENU_BUTTON_CLASS.$click = function (event) {
        UI_BUTTON_CLASS.$click.call(this, event);

        var parent = this.getParent(),
            style = parent.getBody().style,
            list = parent.getItems(),
            height = list[0].getHeight(),
            prevHeight = parent._uPrev.getHeight(),
            top = (toNumber(style.top) - prevHeight) / height;

        style.top =
            MIN(MAX(parent._uPrev == this ? ++top : --top, parent._nOptionSize - list.length), 0) * height +
                prevHeight + 'px';
    };

    /**
     * @override
     */
    UI_POPUP_MENU_ITEM_CLASS.$click = function (event) {
        UI_ITEM_CLASS.$click.call(this, event);
        if (!this.getItems().length) {
            // 点击最终项将关闭打开的全部弹出菜单
            UI_POPUP_MENU_CHAIN[0].hide();
        }
    };

    /**
     * @override
     */
    UI_POPUP_MENU_ITEM_CLASS.$deactivate = function (event) {
        UI_ITEM_CLASS.$deactivate.call(this, event);
        if (!this.contain(event.getControl())) {
            // 如果没有在菜单项上形成完整的点击，同样关闭弹出菜单，不触发click事件
            UI_POPUP_MENU_CHAIN[0].hide();
        }
    };

    /**
     * @override
     */
    UI_POPUP_MENU_ITEM_CLASS.$mouseover = function (event) {
        // 改变菜单项控件的显示状态
        UI_ITEM_CLASS.$mouseover.call(this, event);

        var o = getView(),
            subPopup = this._cSubPopup,
            popup = this.getParent(),
            index = indexOf(UI_POPUP_MENU_CHAIN, popup),
            supPopup = UI_POPUP_MENU_CHAIN[index - 1],
            oldSubPopup = UI_POPUP_MENU_CHAIN[index + 1],
            pos = getPosition(this.getOuter()),
            x = pos.left,
            width;

        if (oldSubPopup != subPopup) {
            // 隐藏之前显示的下级弹出菜单控件
            if (oldSubPopup) {
                oldSubPopup.hide();
            }

            if (this.getItems().length) {
                popup._cExpanded = this;
                this.alterClass('+expanded');

                subPopup.show();

                // 计算子菜单应该显示的位置，以下使用oldSubPopup表示left
                width = subPopup.getWidth();
                oldSubPopup = x + this.getWidth() - 4;
                x -= width - 4;

                // 优先计算延用之前的弹出顺序的应该的位置，显示新的子弹出菜单
                subPopup.setPosition(
                    oldSubPopup + width > o.right || supPopup && supPopup.getX() > popup.getX() && x > o.left ?
                        x : oldSubPopup,
                    pos.top - 4
                );
            }
        }
    };

    /**
     * 添加子选项控件。
     * @public
     *
     * @param {string|Element|ecui.ui.Item} item 选项控件的 html 内容/控件对应的 Element 对象/选项控件
     * @param {number} index 子选项控件需要添加的位置序号
     * @param {Object} options 子控件初始化选项
     * @return {ecui.ui.Item} 子选项控件
     */
    UI_POPUP_MENU_ITEM_CLASS.add = function (item, index, options) {
        var parent = this.getParent();

        return (this._cSubPopup = this._cSubPopup ||
                    $fastCreate(UI_POPUP_MENU, createDom(parent.getPrimary() + parent.constructor.agent.TYPES), this)
        ).add(item, index, options);
    };

    /**
     * 获取全部的子选项控件。
     * @public
     *
     * @return {Array} 子选项控件数组
     */
    UI_POPUP_MENU_ITEM_CLASS.getItems = function () {
        return this._cSubPopup && this._cSubPopup.getItems() || [];
    };

    /**
     * 移除子选项控件。
     * @public
     *
     * @param {number|ecui.ui.Item} item 选项控件的位置序号/选项控件
     * @return {ecui.ui.Item} 被移除的子选项控件
     */
    UI_POPUP_MENU_ITEM_CLASS.remove = function (item) {
        return this._cSubPopup && this._cSubPopup.remove(item);
    };

    /**
     * @override
     */
    UI_POPUP_MENU_CLASS.$alterItems = function () {
        UI_POPUP_MENU_ITEM_FLUSH(this.getParent());

        if (getParent(this.getOuter())) {
            //__gzip_original__optionSize
            var list = this.getItems(),
                len = list.length,
                height = len && list[0].getHeight(),
                optionSize = this._nOptionSize,
                prev = this._uPrev,
                next = this._uNext,
                prevHeight = 0,
                bodyWidth = this.getBodyWidth();

            this.setItemSize(bodyWidth, height);

            height *= MIN(optionSize, len);
            if (optionSize) {
                if (len > optionSize) {
                    prev.show();
                    next.show();
                    prev.$setSize(bodyWidth);
                    next.$setSize(bodyWidth);

                    // 以下使用 prev 代替向上滚动按钮的高度，使用 next 代替向下滚动按钮的高度
                    prevHeight = prev.getHeight();
                    next.setPosition(0, prevHeight + height);
                    height += prevHeight + next.getHeight();
                }
                else {
                    prev.hide();
                    next.hide();
                }
            }

            this.getBody().style.top = prevHeight + 'px';
            this.setBodySize(0, height);
        }
    };

    /**
     * @override
     */
    UI_POPUP_MENU_CLASS.$cache = function (style, cacheSize) {
        UI_ITEMS.$cache.call(this, style, cacheSize);

        if (this._uPrev) {
            this._uPrev.cache(true, true);
        }
        if (this._uNext) {
            this._uNext.cache(true, true);
        }
    };

    /**
     * @override
     */
    UI_POPUP_MENU_CLASS.$dispose = function () {
        // 这里取消展开项的引用，是为了防止全页面 unload 时，展开项在弹出菜单之前被释放了，从而调用 alterClass 恢复状态出错。
        this._cExpanded = null;
        this.hide();
        UI_ITEMS.$dispose.call(this);
    };

    /**
     * @override
     */
    UI_POPUP_MENU_CLASS.$hide = function () {
        for (var i = indexOf(UI_POPUP_MENU_CHAIN, this), index = i, o; o = UI_POPUP_MENU_CHAIN[i++]; ) {
            // 关闭弹出菜单需要同步关闭所有后续的弹出菜单
            if (o._cExpanded) {
                o._cExpanded.alterClass('-expanded');
            }

            UI_CONTROL_CLASS.$hide.call(o);
        }

        if (index) {
            UI_POPUP_MENU_CHAIN = UI_POPUP_MENU_CHAIN.slice(0, index);
            UI_POPUP_MENU_CHAIN[index - 1]._cExpanded.alterClass('-expanded');
        }
        else {
            UI_POPUP_MENU_CHAIN = [];
            restore();
        }
    };

    /**
     * @override
     */
    UI_POPUP_MENU_CLASS.$intercept = function (event) {
        for (var control = event.getControl(); control; control = control.getParent()) {
            if (control instanceof this.Item) {
                // 点击发生在按钮上可能触发点击事件，不默认调用 restore 恢复状态
                return false;
            }
        }
        // 点击发生在其它区域需要关闭弹出菜单，restore 在 $hide 中触发
        UI_POPUP_MENU_CHAIN[0].hide();
        return false;
    };

    /**
     * @override
     */
    UI_POPUP_MENU_CLASS.$remove = function (item) {
        if (this._cExpanded == item) {
            UI_POPUP_MENU_CHAIN[indexOf(UI_POPUP_MENU_CHAIN, this) + 1].hide();
        }
        UI_ITEMS.$remove.call(this, item);
    };

    /**
     * @override
     */
    UI_POPUP_MENU_CLASS.$show = function () {
        UI_CONTROL_CLASS.$show.call(this);

        var o = getView(),
            el = this.getOuter(),
            length = UI_POPUP_MENU_CHAIN.length,
            pos;
        
        if (!getParent(el)) {
            DOCUMENT.body.appendChild(el);
            this.$alterItems();
        }

        pos = getPosition(el);

        // 限制弹出菜单不能超出屏幕
        this.setPosition(
            MIN(MAX(pos.left, o.left), o.right - this.getWidth()),
            MIN(MAX(pos.top, o.top), o.bottom - this.getHeight())
        );

        if (!length) {
            // 第一个弹出菜单，需要屏蔽鼠标点击
            intercept(this);
        }

        el.style.zIndex = 32768 + length;
        UI_POPUP_MENU_CHAIN.push(this);
    };

    /**
     * 弹出菜单无法指定挂载位置。
     * @override
     */
    UI_POPUP_MENU_CLASS.appendTo = UI_POPUP_MENU_CLASS.setParent = blank;

    /**
     * @override
     */
    UI_POPUP_MENU_CLASS.cache = function (cacheSize, force) {
        if (getParent(this.getOuter())) {
            UI_CONTROL_CLASS.cache.call(this, cacheSize, force);
        }
    };

    /**
     * @override
     */
    UI_POPUP_MENU_CLASS.repaint = function () {
        if (getParent(this.getOuter())) {
            UI_CONTROL_CLASS.repaint.call(this);
        }
    };
//{/if}//
//{if 0}//
})();
//{/if}//