/*
Select - 定义模拟下拉框行为的基本操作。
下拉框控件，继承自输入控件，实现了选项组接口，扩展了原生 SelectElement 的功能，允许指定下拉选项框的最大选项数量，在屏幕显示不下的时候，会自动显示在下拉框的上方。在没有选项时，下拉选项框有一个选项的高度。下拉框控件允许使用键盘与滚轮操作，在下拉选项框打开时，可以通过回车键或鼠标点击选择，上下键选择选项的当前条目，在关闭下拉选项框后，只要拥有焦点，就可以通过滚轮上下选择选项。

下拉框控件直接HTML初始化的例子:
<select ecui="type:select" name="sex">
  <option value="male" selected="selected">男</option>
  <option value="female">女</option>
</select>
或
<div ecui="type:select;name:sex;value:male">
  <div ecui="value:male">男</div>
  <div ecui="value:female">女</div>
</div>

属性
_nOptionSize  - 下接选择框可以用于选择的条目数量
_cSelected    - 当前选中的选项
_uText        - 下拉框的文本框
_uButton      - 下拉框的按钮
_uOptions     - 下拉选择框
*/
//{if 0}//
(function () {

    var core = ecui,
        array = core.array,
        dom = core.dom,
        string = core.string,
        ui = core.ui,
        util = core.util,

        undefined,
        DOCUMENT = document,
        MATH = Math,
        MAX = MATH.max,
        MIN = MATH.min,

        indexOf = array.indexOf,
        children = dom.children,
        createDom = dom.create,
        getParent = dom.getParent,
        getPosition = dom.getPosition,
        getText = dom.getText,
        insertAfter = dom.insertAfter,
        insertBefore = dom.insertBefore,
        moveElements = dom.moveElements,
        removeDom = dom.remove,
        encodeHTML = string.encodeHTML,
        extend = util.extend,
        getView = util.getView,
        setDefault = util.setDefault,

        $fastCreate = core.$fastCreate,
        getAttributeName = core.getAttributeName,
        getFocused = core.getFocused,
        inheritsControl = core.inherits,
        intercept = core.intercept,
        mask = core.mask,
        restore = core.restore,
        setFocused = core.setFocused,
        triggerEvent = core.triggerEvent,

        UI_INPUT_CONTROL = ui.InputControl,
        UI_INPUT_CONTROL_CLASS = UI_INPUT_CONTROL.prototype,
        UI_BUTTON = ui.Button,
        UI_SCROLLBAR = ui.Scrollbar,
        UI_PANEL = ui.Panel,
        UI_PANEL_CLASS = UI_PANEL.prototype,
        UI_ITEM = ui.Item,
        UI_ITEM_CLASS = UI_ITEM.prototype,
        UI_ITEMS = ui.Items;
//{/if}//
//{if $phase == "define"}//
    ///__gzip_original__UI_SELECT
    ///__gzip_original__UI_SELECT_CLASS
    /**
     * 初始化下拉框控件。
     * options 对象支持的属性如下：
     * browser        是否使用浏览器原生的滚动条，默认使用模拟的滚动条
     * optionSize     下拉框最大允许显示的选项数量，默认为5
     * optionsElement 下拉选项主元素
     * @public
     *
     * @param {Object} options 初始化选项
     */
    var UI_SELECT = ui.Select =
        inheritsControl(
            UI_INPUT_CONTROL,
            'ui-select',
            function (el, options) {
                var name = el.name || options.name || '',
                    type = this.getType(),

                    id = options.id || 'id_notset',
                    optionsEl = createDom(
                        type + '-options' + this.Options.TYPES,
                        'position:absolute;z-index:65535;display:none'
                    );

                optionsEl.setAttribute('ecui_id', id);
                   
                setDefault(options, 'hidden', true);

                if (el.tagName == 'SELECT') {
                    var i = 0,
                        list = [],
                        elements = el.options,
                        o = el;

                    options.value = el.value;

                    // 移除select标签
                    el = insertBefore(createDom(el.className, el.style.cssText, 'span'), el);
                    removeDom(o);

                    // 转化select标签
                    for (; o = elements[i]; ) {
                        // 这里的text不进行转义，特殊字符不保证安全
                        list[i++] =
                            '<div ' + getAttributeName() + '="value:' + encodeHTML(o.value) + '">' +
                                o.text + '</div>';
                    }
                    optionsEl.innerHTML = list.join('');
                }
                else {
                    moveElements(el, optionsEl);
                }

                el.innerHTML =
                    '<span class="' + type + '-text' + UI_ITEM.TYPES + '"></span><span class="' + type + '-button' +
                        UI_BUTTON.TYPES + '" style="position:absolute"></span><input name="' + name + '" value="' +
                        encodeHTML(options.value || '') + '">';

                el.appendChild(optionsEl);

                return el;
            },
            function (el, options) {
                el = children(el);

                this._uText = $fastCreate(UI_ITEM, el[0], this, {capturable: false});
                this._uButton = $fastCreate(UI_BUTTON, el[1], this, {capturable: false});

                this._uOptions = $fastCreate(
                    this.Options,
                    removeDom(el[3]),
                    this,
                    {hScroll: false, browser: options.browser}
                );

                this.$setBody(this._uOptions.getBody());
                // 初始化下拉区域最多显示的选项数量
                this._nOptionSize = options.optionSize || 5;

                this.$initItems();
            }
        ),
        UI_SELECT_CLASS = UI_SELECT.prototype,

        /**
         * 初始化下拉框控件的下拉选项框部件。
         * @public
         *
         * @param {Object} options 初始化选项
         */
        UI_SELECT_OPTIONS_CLASS = (UI_SELECT_CLASS.Options = inheritsControl(UI_PANEL)).prototype,

        /**
         * 初始化下拉框控件的选项部件。
         * @public
         *
         * @param {Object} options 初始化选项
         */
        UI_SELECT_ITEM_CLASS =
            (UI_SELECT_CLASS.Item = inheritsControl(
                UI_ITEM,
                null,
                null,
                function (el, options) {
                    this._sValue = options.value === undefined ? getText(el) : '' + options.value;
                }
            )).prototype;
//{else}//
    /**
     * 下拉框刷新。
     * @private
     *
     * @param {ecui.ui.Select} control 下拉框控件
     */
    function UI_SELECT_FLUSH(control) {
        var options = control._uOptions,
            scrollbar = options.$getSection('VScrollbar'),
            el = options.getOuter(),
            pos = getPosition(control.getOuter()),
            selected = control._cSelected,
            optionTop = pos.top + control.getHeight();

        if (!getParent(el)) {
            // 第一次显示时需要进行下拉选项部分的初始化，将其挂载到 DOM 树中
            DOCUMENT.body.appendChild(el);
            control.cache(false, true);
            control.$alterItems();
        }

        if (options.isShow()) {
            if (selected) {
                setFocused(selected);
            }
            scrollbar.setValue(scrollbar.getStep() * indexOf(control.getItems(), selected));

            // 以下使用control代替optionHeight
            control = options.getHeight();

            // 如果浏览器下部高度不够，将显示在控件的上部
            options.setPosition(
                pos.left,
                optionTop + control <= getView().bottom ? optionTop : pos.top - control
            );
        }
    }

    /**
     * 改变下拉框当前选中的项。
     * @private
     *
     * @param {ecui.ui.Select} control 下拉框控件
     * @param {ecui.ui.Select.Item} item 新选中的项
     */
    function UI_SELECT_CHANGE_SELECTED(control, item) {
        if (item !== control._cSelected) {
            control._uText.setContent(item ? item.getBody().innerHTML : '');
            UI_INPUT_CONTROL_CLASS.setValue.call(control, item ? item._sValue : '');
            control._cSelected = item;
            if (control._uOptions.isShow()) {
                setFocused(item);
            }
        }
    }

    extend(UI_SELECT_CLASS, UI_ITEMS);

    /**
     * 销毁选项框部件时需要检查是否展开，如果展开需要先关闭。
     * @override
     */
    UI_SELECT_OPTIONS_CLASS.$dispose = function () {
        this.hide();
        UI_PANEL_CLASS.$dispose.call(this);
    };

    /**
     * 关闭选项框部件时，需要恢复强制拦截的环境。
     * @override
     */
    UI_SELECT_OPTIONS_CLASS.$hide = function () {
        UI_PANEL_CLASS.$hide.call(this);
        mask();
        restore();
    };

    /**
     * 对于下拉框选项，鼠标移入即自动获得焦点。
     * @override
     */
    UI_SELECT_ITEM_CLASS.$mouseover = function (event) {
        UI_ITEM_CLASS.$mouseover.call(this, event);
        setFocused(this);
    };

    /**
     * 获取选项的值。
     * getValue 方法返回选项控件的值，即选项选中时整个下拉框控件的值。
     * @public
     *
     * @return {string} 选项的值
     */
    UI_SELECT_ITEM_CLASS.getValue = function () {
        return this._sValue;
    };

    /**
     * 设置选项的值。
     * setValue 方法设置选项控件的值，即选项选中时整个下拉框控件的值。
     * @public
     *
     * @param {string} value 选项的值
     */
    UI_SELECT_ITEM_CLASS.setValue = function (value) {
        var parent = this.getParent();
        this._sValue = value;
        if (parent && this == parent._cSelected) {
            // 当前被选中项的值发生变更需要同步更新控件的值
            UI_INPUT_CONTROL_CLASS.setValue.call(parent, value);
        }
    };

    /**
     * 下拉框控件激活时，显示选项框，产生遮罩层阻止对页面内 DOM 节点的点击，并设置框架进入强制点击拦截状态。
     * @override
     */
    UI_SELECT_CLASS.$activate = function (event) {
        if (!(event.getControl() instanceof UI_SCROLLBAR)) {
            UI_INPUT_CONTROL_CLASS.$activate.call(this, event);
            this._uOptions.show();
            // 拦截之后的点击，同时屏蔽所有的控件点击事件
            intercept(this);
            mask(0, 65534);
            UI_SELECT_FLUSH(this);
            event.stopPropagation();
        }
    };

    /**
     * 选项控件发生变化的处理。
     * 在 选项组接口 中，选项控件发生添加/移除操作时调用此方法。虚方法，子控件必须实现。
     * @protected
     */
    UI_SELECT_CLASS.$alterItems = function () {
        var options = this._uOptions,
            scrollbar = options.$getSection('VScrollbar'),
            optionSize = this._nOptionSize,
            step = this.getBodyHeight(),
            width = this.getWidth(),
            itemLength = this.getItems().length;

        if (getParent(options.getOuter())) {
            // 设置选项框
            scrollbar.setStep(step);

            // 为了设置激活状态样式, 因此必须控制下拉框中的选项必须在滚动条以内
            // TODO:根据具体样式修改
            this.setItemSize(
                width - options.getMinimumWidth() - (itemLength > optionSize ? scrollbar.getWidth() : 0) - 15,
                step
            );

            // 设置options框的大小，如果没有元素，至少有一个单位的高度
            options.$$mainHeight = itemLength * step + options.$$bodyHeightRevise;
            options.$setSize(width, (MIN(itemLength, optionSize) || 1) * step + options.getMinimumHeight());
        }
    };

    /**
     * @override
     */
    UI_SELECT_CLASS.$cache = function (style, cacheSize) {
        (getParent(this._uOptions.getOuter()) ? UI_ITEMS : UI_INPUT_CONTROL_CLASS)
            .$cache.call(this, style, cacheSize);
        this._uText.cache(false, true);
        this._uButton.cache(false, true);
        this._uOptions.cache(false, true);
    };

    /**
     * 控件在下拉框展开时，需要拦截浏览器的点击事件，如果点击在下拉选项区域，则选中当前项，否则直接隐藏下拉选项框。
     * @override
     */
    UI_SELECT_CLASS.$intercept = function (event) {
        //__transform__control_o
        this._uOptions.hide();
        for (var control = event.getControl(); control; control = control.getParent()) {
            if (control instanceof this.Item) {
                if (control != this._cSelected) {
                    // 检查点击是否在当前下拉框的选项上
                    UI_SELECT_CHANGE_SELECTED(this, control);
                    triggerEvent(this, 'change');
                }
                break;
            }
        }
        event.exit();
    };

    /**
     * 接管对上下键与回车/ESC键的处理。
     * @override
     */
    UI_SELECT_CLASS.$keydown = UI_SELECT_CLASS.$keypress = function (event) {
        UI_INPUT_CONTROL_CLASS['$' + event.type](event);

        var options = this._uOptions,
            scrollbar = options.$getSection('VScrollbar'),
            optionSize = this._nOptionSize,
            which = event.which,
            list = this.getItems(),
            length = list.length,
            focus = getFocused();

        if (this.isFocused()) {
            // 当前不能存在鼠标操作，否则屏蔽按键
            if (which == 40 || which == 38) {
                if (length) {
                    if (options.isShow()) {
                        setFocused(list[which = MIN(MAX(0, indexOf(list, focus) + which - 39), length - 1)]);
                        which -= scrollbar.getValue() / scrollbar.getStep();
                        scrollbar.skip(which < 0 ? which : which >= optionSize ? which - optionSize + 1 : 0);
                    }
                    else {
                        this.setSelectedIndex(MIN(MAX(0, indexOf(list, this._cSelected) + which - 39), length - 1));
                    }
                }
                return false;
            }
            else if (which == 27 || which == 13 && options.isShow()) {
                // 回车键选中，ESC键取消
                options.hide();
                if (which == 13) {
                    UI_SELECT_CHANGE_SELECTED(this, focus);
                    //触发change事件
                    triggerEvent(this, 'change');
                }
                return false;
            }
        }
    };

    /**
     * 如果控件拥有焦点，则当前选中项随滚轮滚动而自动指向前一项或者后一项。
     * @override
     */
    UI_SELECT_CLASS.$mousewheel = function (event) {
        if (this.isFocused()) {
            var options = this._uOptions,
                list = this.getItems(),
                length = list.length;

            if (options.isShow()) {
                options.$mousewheel(event);
            }
            else {
                //options表示当前选项的index
                options = indexOf(list, this._cSelected) + (event.detail > 0 ? 1 : -1)
                this.setSelectedIndex(
                    length ?
                        MIN(MAX(0, options), length - 1) : null
                );
                if (options >= 0 && options < length) {
                    //鼠标滚动触发change事件
                    triggerEvent(this, 'change');
                }
            }

            event.exit();
        }
    };

    /**
     * @override
     */
    UI_SELECT_CLASS.$ready = function () {
        this.setValue(this.getValue());
    };

    /**
     * 下拉框移除子选项时，如果选项是否被选中，需要先取消选中。
     * @override
     */
    UI_SELECT_CLASS.remove = function (item) {
        if ('number' == typeof item) {
            item = this.getItems()[item];
        }
        if (item == this._cSelected) {
            UI_SELECT_CHANGE_SELECTED(this);
        }
        return UI_ITEMS.remove.call(this, item);
    };

    /**
     * 添加选项需要根据情况继续cache操作
     * @override
     */
    UI_SELECT_CLASS.add = function (item, index, options) {
        item = UI_ITEMS.add.call(this, item, index, options);
        if (getParent(this._uOptions.getOuter())) {
            item.cache(true, true);
        }
        return item;
    };

    /**
     * @override
     */
    UI_SELECT_CLASS.$setSize = function (width, height) {
        UI_INPUT_CONTROL_CLASS.$setSize.call(this, width, height);
        this.$locate();
        height = this.getBodyHeight();

        // 设置文本区域
        this._uText.$setSize(width = this.getBodyWidth() - height, height);

        // 设置下拉按钮
//        this._uButton.$setSize(height, height);
//        this._uButton.setPosition(width, 0);
    };

    /**
     * 获取被选中的选项控件。
     * @public
     *
     * @return {ecui.ui.Item} 选项控件
     */
    UI_SELECT_CLASS.getSelected = function () {
        return this._cSelected || null;
    };

    /**
     * 设置下拉框允许显示的选项数量。
     * 如果实际选项数量小于这个数量，没有影响，否则将出现垂直滚动条，通过滚动条控制其它选项的显示。
     * @public
     *
     * @param {number} value 显示的选项数量，必须大于 1
     */
    UI_SELECT_CLASS.setOptionSize = function (value) {
        this._nOptionSize = value;
        this.$alterItems();
        UI_SELECT_FLUSH(this);
    };

    /**
     * 根据序号选中选项。
     * @public
     *
     * @param {number} index 选项的序号
     */
    UI_SELECT_CLASS.setSelectedIndex = function (index) {
        UI_SELECT_CHANGE_SELECTED(this, this.getItems()[index]);
    };

    /**
     * 设置控件的值。
     * setValue 方法设置控件的值，设置的值必须与一个子选项的值相等，否则将被设置为空，使用 getValue 方法获取设置的值。
     * @public
     *
     * @param {string} value 需要选中的值
     */
    UI_SELECT_CLASS.setValue = function (value) {
        for (var i = 0, list = this.getItems(), o; o = list[i++]; ) {
            if (o._sValue == value) {
                UI_SELECT_CHANGE_SELECTED(this, o);
                return;
            }
        }

        // 找不到满足条件的项，将选中的值清除
        UI_SELECT_CHANGE_SELECTED(this);
    };
//{/if}//
//{if 0}//
})();
//{/if}//
