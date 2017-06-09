/*
Tab - 定义分页选项卡的操作。
选项卡控件，继承自基础控件，实现了选项组接口。每一个选项卡都包含一个头部区域与容器区域，选项卡控件存在互斥性，只有唯一的一个选项卡能被选中并显示容器区域。

直接初始化选项卡控件的例子
<div ecui="type:tab;selected:1">
    <!-- 包含容器的选项卡 -->
    <div>
        <label>标题1</label>
        <!-- 这里是容器 -->
        ...
    </div>
    <!-- 仅有标题的选项卡，以下selected定义与控件定义是一致的，可以忽略其中之一 -->
    <label ecui="selected:true">标题2</label>
</div>

属性
_bButton         - 向前向后滚动按钮是否显示
_oSelected       - 初始化时临时保存当前被选中的选项卡
_aPosition       - 选项卡位置缓存
_cSelected       - 当前选中的选项卡
_uPrev           - 向前滚动按钮
_uNext           - 向后滚动按钮
$$titleWidth     - 标签区域的宽度

Item属性
_sContainer      - 容器 DOM 元素的布局属性
_eContainer      - 容器 DOM 元素
*/
//{if 0}//
(function () {

    var core = ecui,
        array = core.array,
        dom = core.dom,
        ui = core.ui,
        util = core.util,

        MATH = Math,
        MAX = MATH.max,
        MIN = MATH.min,

        indexOf = array.indexOf,
        createDom = dom.create,
        moveElements = dom.moveElements,
        removeDom = dom.remove,
        first = dom.first,
        setStyle = dom.setStyle,
        extend = util.extend,
        toNumber = util.toNumber,

        $fastCreate = core.$fastCreate,
        inheritsControl = core.inherits,
        triggerEvent = core.triggerEvent,

        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = UI_CONTROL.prototype,
        UI_BUTTON = ui.Button,
        UI_ITEM = ui.Item,
        UI_ITEM_CLASS = UI_ITEM.prototype,
        UI_ITEMS = ui.Items;
//{/if}//
//{if $phase == "define"}//
    /**
     * 初始化选项卡控件。
     * options 对象支持的特定属性如下：
     * selected 选中的选项序号，默认为0
     * @protected
     *
     * @param {Object} options 初始化选项
     */
    ///__gzip_original__UI_TAB
    ///__gzip_original__UI_TAB_BUTTON
    ///__gzip_original__UI_TAB_ITEM
    var UI_TAB = ui.Tab =
        inheritsControl(
            UI_CONTROL,
            'ui-tab',
            null,
            function (el, options) {
                //__gzip_original__buttonParams
                var type = this.getType(),
                    o = createDom(type + '-title', 'position:relative;overflow:hidden');

                this._oSelected = options.selected || 0;

                // 生成选项卡头的的DOM结构
                o.innerHTML = '<div class="' + type + '-prev' + this.Button.TYPES +
                    '" style="position:absolute;display:none;left:0px"></div><div class="' +
                    type + '-next' + this.Button.TYPES +
                    '" style="position:absolute;display:none"></div><div class="' +
                    type + '-items" style="position:absolute;white-space:nowrap"></div>';

                moveElements(el, options = o.lastChild);
                el.appendChild(o);
                this.$setBody(options);

                this.$initItems();

                // 滚动按钮
                this._uNext = $fastCreate(this.Button, options = options.previousSibling, this);
                this._uPrev = $fastCreate(this.Button, options.previousSibling, this);
            }
        ),
        UI_TAB_CLASS = UI_TAB.prototype,

        /**
         * 初始化选项卡控件的按钮部件。
         * @protected
         *
         * @param {Object} options 初始化选项
         */
        UI_TAB_BUTTON_CLASS = (UI_TAB_CLASS.Button = inheritsControl(UI_BUTTON, 'ui-tab-button')).prototype,

        /**
         * 初始化选项卡控件的选项部件。
         * options 对象支持的特定属性如下：
         * selected 当前项是否被选中
         * @protected
         *
         * @param {Object} options 初始化选项
         */
        UI_TAB_ITEM_CLASS =
            (UI_TAB_CLASS.Item = inheritsControl(
                UI_ITEM,
                null,
                null,
                function (el, options) {
                    //__gzip_original__parent
                    var parent = options.parent;

                    if (el.tagName != 'LABEL') {
                        var o = first(el),
                            tmpEl;

                        moveElements(el, tmpEl = createDom(options.primary + '-container'), true);
                        el.appendChild(o);
                        this.setContainer(tmpEl);
                    }

                    setStyle(el, 'display', 'inline-block');

                    if (parent && options.selected) {
                        parent._oSelected = this;
                    }
                }
            )).prototype;
//{else}//
    /**
     * 刷新向前向右滚动按钮的可操作状态。
     * @private
     *
     * @param {ecui.ui.Tab} control Tab 控件对象
     */
    function UI_TAB_FLUSH_BUTTON(control) {
        var left = toNumber(control.getBody().style.left);

        control._uPrev[left < control._uPrev.getWidth() ? 'enable' : 'disable']();
        control._uNext[
            left > control.getBodyWidth() - control.$$titleWidth - control._uNext.getWidth() ? 'enable' : 'disable'
        ]();
    }

    extend(UI_TAB_CLASS, UI_ITEMS);

    /**
     * @override
     */
    UI_TAB_BUTTON_CLASS.$click = function (event) {
        UI_CONTROL_CLASS.$click.call(this, event);

        //__gzip_original__pos
        var parent = this.getParent(),
            style = parent.getBody().style,
            pos = parent._aPosition,
            index = parent.$getLeftMostIndex();

        index = MIN(
            MAX(0, index + (parent._uPrev == this ? toNumber(style.left) != pos[index] ? 0 : -1 : 1)),
            pos.length - 1
        );

        style.left = MAX(pos[index], parent.getBodyWidth() - parent.$$titleWidth - parent._uNext.getWidth()) + 'px';
        UI_TAB_FLUSH_BUTTON(parent);
    };

    /**
     * @override
     */
    UI_TAB_ITEM_CLASS.$cache = function (style, cacheSize) {
        UI_ITEM_CLASS.$cache.call(this, style, cacheSize);

        this.$$marginLeft = toNumber(style.marginLeft);
        this.$$marginRight = toNumber(style.marginRight);
    };

    /**
     * @override
     */
    UI_TAB_ITEM_CLASS.$click = function (event) {
        UI_ITEM_CLASS.$click.call(this, event);
        this.getParent().setSelected(this);
    };

    /**
     * @override
     */
    UI_TAB_ITEM_CLASS.$dispose = function () {
        this._eContainer = null;
        UI_ITEM_CLASS.$dispose.call(this);
    };

    /**
     * @override
     */
    UI_TAB_ITEM_CLASS.$setParent = function (parent) {
        //__gzip_original__el
        var el = this._eContainer;

        UI_ITEM_CLASS.$setParent.call(this, parent);
        if (el) {
            if (parent) {
                parent.getMain().appendChild(el);
            }
            else {
                removeDom(el);
            }
        }
    };

    /**
     * 获取选项卡对应的容器元素。
     * @public
     *
     * @return {HTMLElement} 选项卡对应的容器元素
     */
    UI_TAB_ITEM_CLASS.getContainer = function () {
        return this._eContainer;
    };

    /**
     * 设置选项卡对应的容器元素。
     * @public
     *
     * @param {HTMLElement} el 选项卡对应的容器元素
     */
    UI_TAB_ITEM_CLASS.setContainer = function (el) {
        var parent = this.getParent();

        if (this._eContainer) {
            removeDom(this._eContainer);
        }
        if (this._eContainer = el) {
            if ((this._sContainer = el.style.display) == 'none') {
                this._sContainer = '';
            }

            if (parent) {
                parent.getMain().appendChild(el);

                // 如果当前节点被选中需要显示容器元素，否则隐藏
                el.style.display = parent._cSelected == this ? this._sContainer : 'none';
            }
        }
    };

    /**
     * @override
     */
    UI_TAB_CLASS.$alterItems = function () {
        // 第一次进入时不需要调用$setSize函数，否则将初始化两次
        if (this._aPosition) {
            this.$setSize(this.getWidth());
        }

        for (
            var i = 0,
                list = this.getItems(),
                pos = this._aPosition = [this._uPrev.getWidth()],
                lastItem = {$$marginRight: 0},
                o;
            o = list[i++];
            lastItem = o
        ) {
            pos[i] = pos[i - 1] - MAX(lastItem.$$marginRight, o.$$marginLeft) - o.getWidth();
        }
    };

    /**
     * @override
     */
    UI_TAB_CLASS.$cache = function (style, cacheSize) {
        UI_ITEMS.$cache.call(this, style, cacheSize);

        this._uPrev.cache(true, true);
        this._uNext.cache(true, true);

        this.$$titleWidth = this.getBody().offsetWidth;
    };

    /**
     * 获得当前显示的选项卡中左边元素的索引，只在能左右滚动时有效。
     * @protected
     *
     * @return {number} 最左边元素的索引
     */
    UI_TAB_CLASS.$getLeftMostIndex = function () {
        for (var left = toNumber(this.getBody().style.left), pos = this._aPosition, i = pos.length; i--; ) {
            if (left <= pos[i]) {
                return i;
            }
        }
    };

    /**
     * @override
     */
    UI_TAB_CLASS.$remove = function (child) {
        if (this._cSelected == child) {
            var list = this.getItems(),
                index = indexOf(list, child);

            // 跳到被删除项的后一项
            this.setSelected(index == list.length - 1 ? index - 1 : index + 1);
        }

        UI_ITEMS.$remove.call(this, child);
    };

    /**
     * @override
     */
    UI_TAB_CLASS.$setSize = function (width, height) {
        UI_CONTROL_CLASS.$setSize.call(this, width, height);

        //__gzip_original__prev
        //__gzip_original__next
        var prev = this._uPrev,
            next = this._uNext,
            style = this.getBody().style;

        width = this.getBodyWidth();
        if (this.$$titleWidth > width) {
            width -= next.getWidth();
            next.getOuter().style.left = width + 'px';

            if (this._bButton) {
                // 缩小后变大，右边的空白自动填补
                width -= this.$$titleWidth;
                if (toNumber(style.left) < width) {
                    style.left = width + 'px';
                }
            }
            else {
                prev.$show();
                next.$show();
                style.left = prev.getWidth() + 'px';
                this._bButton = true;
            }

            UI_TAB_FLUSH_BUTTON(this);
        }
        else if (this._bButton) {
            prev.$hide();
            next.$hide();
            style.left = '0px';
            this._bButton = false;
        }
    };

    /**
     * 获得当前选中的选项卡控件。
     *
     * @return {ecui.ui.Tab.Item} 选中的选项卡控件
     */
    UI_TAB_CLASS.getSelected = function () {
        return this._cSelected;
    };

    /**
     * @override
     */
    UI_TAB_CLASS.init = function () {
        this._uPrev.init();
        this._uNext.init();
        UI_ITEMS.init.call(this);
        for (var i = 0, list = this.getItems(), o; o = list[i++];) {
            o.$setSize(o.getWidth(), o.getHeight());
        }
        this.setSelected(this._oSelected);
    };

    /**
     * 设置被选中的选项卡。
     * @public
     *
     * @param {number|ecui.ui.Tab.Item} 选项卡子选项的索引/选项卡子选项控件
     */
    UI_TAB_CLASS.setSelected = function (item) {
        //__gzip_original__prev
        var i = 0,
            list = this.getItems(),
            prev = this._uPrev,
            style = this.getBody().style,
            left = toNumber(style.left),
            o;

        if ('number' == typeof item) {
            item = list[item];
        }
        if (this._cSelected != item) {
            for (; o = list[i++]; ) {
                if (o._eContainer) {
                    o._eContainer.style.display = o == item ? o._sContainer : 'none';
                }
            }

            if (this._cSelected) {
                this._cSelected.alterClass('-selected');
            }

            if (item) {
                item.alterClass('+selected');
                o = this._aPosition[indexOf(list, item)] - (prev.isShow() ? 0 : prev.getWidth());

                // 如果当前选中的项没有被完全显示(例如处于最左或最右时)，设置成完全显示
                if (left < o) {
                    style.left = o + 'px';
                }
                else {
                    o -= item.getWidth() 
                        + (prev.isShow() ? prev.getWidth() : 0) 
                        // + prev.getWidth()
                        + (this._uNext.isShow() ? this._uNext.getWidth() : 0)
                        // + this._uNext.getWidth()
                        - this.getBodyWidth();
                    if (left > o) {
                        style.left = o + 'px';
                    }
                }
                UI_TAB_FLUSH_BUTTON(this);
            }

            this._cSelected = item;
            triggerEvent(this, 'change');
        }
    };
//{/if}//
//{if 0}//
})();
//{/if}//