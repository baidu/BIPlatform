/*
MultiSelect - 定义多选下拉框行为的基本操作。
多选下拉框控件，继承自输入框控件，实现了选项组接口，参见下拉框控件。

下拉框控件直接HTML初始化的例子:
<select ecui="type:multi-select;option-size:3" name="test">
    <!-- 这里放选项内容 -->
    <option value="值">文本</option>
    ...
    <option value="值" selected>文本</option>
    ...
</select>

如果需要自定义特殊的选项效果，请按下列方法初始化:
<div ecui="type:multi-select;name:test;option-size:3">
    <!-- 这里放选项内容 -->
    <li ecui="value:值">文本</li>
    ...
</div>

Item属性
_eInput - 多选项的INPUT对象
*/
//{if 0}//
(function () {
    var core = ecui,
        array = core.array,
        dom = core.dom,
        ui = core.ui,
        util = core.util,

        indexOf = array.indexOf,
        getText = dom.getText,
        removeDom = dom.remove,
        createDom = dom.create,
        setInput = dom.setInput,
        extend = util.extend,
        inherits = util.inherits,

        getKey = core.getKey,
        mask = core.mask,
        inheritsControl = core.inherits,
        triggerEvent = core.triggerEvent,

        UI_INPUT_CONTROL = ui.InputControl,
        UI_INPUT_CONTROL_CLASS = UI_INPUT_CONTROL.prototype,
        UI_ITEMS = ui.Items,
        UI_SELECT = ui.Select,
        UI_SELECT_CLASS = UI_SELECT.prototype,
        UI_SELECT_ITEM = UI_SELECT_CLASS.Item,
        UI_SELECT_ITEM_CLASS = UI_SELECT_ITEM.prototype;
//{/if}//
//{if $phase == "define"}//
    /**
     * 初始化多选下拉框控件。
     * options 对象支持的属性如下：
     * optionSize 下拉框最大允许显示的选项数量，默认为5
     * @public
     *
     * @param {Object} options 初始化选项
     */
    //__gzip_original__UI_MULTI_SELECT
    //__gzip_original__UI_MULTI_SELECT_ITEM
    var UI_MULTI_SELECT = ui.MultiSelect = 
        inheritsControl(
            UI_SELECT,
            'ui-multi-select',
            function (el, options) {
                options.hide = true;
                if (options.value) {
                    options.value = options.value.toString();
                }
            },
            function(el, options) {
                var values;
                    me = this;
                // TODO:为了适应当前配置的报表没有添加此属性，需要干掉
                options.selectAllButton = true;

                if (options.maxlength) {
                    this._nTextLen = options.maxlength;
                }
                if (options.textAll) {
                    this._sTextAll = options.textAll;
                }
                if (options.textNone) {
                    this._sTextNone = '请至少选择一项' || options.textNone;
                }
                if (options.maxSelected) {
                    this._nMaxSelected = options.maxSelected;
                }
                else if (options.selectAllButton) {
                    this.add(options.selectAllText, 0, {selectAllButton: true});
                    this._bSelectAllBtn = true;
                }
                if (options.tip) {
                    this._bTip = true;
                }
                if (options.value) {
                    this.setValue(options.value);
                }
                if (options.selectAll) {
                    this._bInitSelectAll = true;
                }
                if (options.minSelected) {
                    this._nMinSelected = options.minSelected;
                }

                this._eInput.disabled = true;

                core.addEventListener(this._uOptions, 'scrollimmediately', handleScroll);
                // 当滚动条滚动后，如果有全选，就始终浮在最上面
                function handleScroll(event) {
                    var items = me.getItems();
                    var selectAllEl = items[0]._eBody;
                    if (me._bSelectAllBtn) {
                        // TODO:为items[0]添加样式
                        selectAllEl.style.top = this._uVScrollbar.getValue() + 'px';
                        core.dom.addClass(selectAllEl, 'ui-multi-select-selectall');
                    }
                }
            }
        ),
        UI_MULTI_SELECT_CLASS = UI_MULTI_SELECT.prototype,

        /**
         * 初始化多选下拉框控件的选项部件。
         * @public
         *
         * @param {Object} options 初始化选项
         */
        UI_MULTI_SELECT_ITEM = UI_MULTI_SELECT_CLASS.Item =
            inheritsControl(
            UI_SELECT_ITEM,
            'ui-multi-select-item',
            function (el, options) {
                var type = this.getTypes()[0],
                    o = createDom(type + '-icon');
                
                this._bSelectAllBtn = options.selectAllButton;
                this._sTip = options.tip ? options.tip : getText(el);

                el.insertBefore(o, el.firstChild);
                el = this._eInput =
                    options.parent.getMain().appendChild(setInput(null, options.parent.getName(), 'checkbox'));

                options.value === undefined ? el.value = '' : el.value = options.value;
                el.style.display = 'none';
            }
        ),
        UI_MULTI_SELECT_ITEM_CLASS = UI_MULTI_SELECT_ITEM.prototype;
//{else}//
    
    /**
     * 刷新全选按钮
     * @private
     */
    function UI_MULTI_SELECT_FLUSH_SELECTALL(control, status) {
        var items = control.getItems();

        if (!control._bSelectAllBtn) {
            return;
        }

        if (status === undefined) {
            status = control.getSelected().length === items.length - 1;
            items[0].$setSelected(status);
        }
        else {
            for (var i = 0, item; item = items[i]; i++) {
                item.$setSelected(status);
            }
        }
    }

    /**
     * 刷新显示区域的选中值列表。
     * @private
     *
     * @param {ecui.ui.MultiSelect} control 多选下拉框控件
     */
    function UI_MULTI_SELECT_FLUSH_TEXT(control) {
        var tip;
        if (control) {
            var btnAllSelected = false;
            for (var i = 0, list = control.getItems(), o, text = []; o = list[i++]; ) {
                if (o.isSelected()) {
                    if (o._bSelectAllBtn) {
                        btnAllSelected = true;
                    }
                    else {
                        text.push(o._sTip);
                    }

                }
            }
            tip = '<span title="'+ text.join(',') +'">';
            if (
                control._sTextAll
                && (text.length != 0 || btnAllSelected)
                && text.length == list.length + (control._bSelectAllBtn ? -1 : 0) 
            ) {
                text = control._sTextAll;
//                text = list[0]._sTip;
            }
            else if (text.length == 0 && control._sTextNone) {
                text = control._sTextNone;
            }
            else {
                text = [
                    '已选中',
                    text.length ,
                    '项'
                ].join('');
//                text = text.join(',');
//                if (control._nTextLen && text.length > control._nTextLen) {
//                    text = text.substring(0, control._nTextLen) + '...';
//                }
            }
            if (control._bTip) {
                text = tip + text + '</span>';
            }
            control.$getSection('Text').setContent(text);
        }
    }

    extend(UI_MULTI_SELECT_CLASS, UI_ITEMS);

    var originRemoveMethod = UI_MULTI_SELECT_CLASS.remove;
    UI_MULTI_SELECT_CLASS.remove = function () {
        if (this._bSelectAllBtn && this.getItems().length <= 1) {
            return null;
        }
        return originRemoveMethod.apply(this, arguments);
    };

    /**
     * 鼠标单击控件事件的默认处理。
     * 控件点击时将改变当前的选中状态。如果控件处于可操作状态(参见 isEnabled)，click 方法触发 onclick 事件，如果事件返回值不为 false，则调用 $click 方法。
     * @protected
     *
     * @param {Event} event 事件对象
     */
    UI_MULTI_SELECT_ITEM_CLASS.$click = function (event) {
        var par = this.getParent(),
            selected = par.getSelected().length;

        UI_SELECT_ITEM_CLASS.$click.call(this, event);
        if (!this.isSelected()) {
            if (!par._nMaxSelected || par._nMaxSelected >= selected + 1) {
                this.setSelected(true);
            }
        }
        else {
            if (!par._nMinSelected || par._nMinSelected <= selected - 1) {
                this.setSelected(false);
            }
        }
    };

    /**
     * 销毁控件的默认处理。
     * 页面卸载时将销毁所有的控件，释放循环引用，防止在 IE 下发生内存泄漏，$dispose 方法的调用不会受到 ondispose 事件返回值的影响。
     * @protected
     */
    UI_MULTI_SELECT_ITEM_CLASS.$dispose = function () {
        this._eInput = null;
        UI_SELECT_ITEM_CLASS.$dispose.call(this);
    };

    /**
     * 判断当前选项是否选中。
     * @protected
     *
     * @return {boolean} 当前项是否选中
     */
    UI_MULTI_SELECT_ITEM_CLASS.isSelected = function () {
        return this._eInput.checked;
    };

    /**
     *
     */
    UI_MULTI_SELECT_ITEM_CLASS.$setSelected = function (status) {
        this._eInput.checked = status !== false;
        this.setClass(this.getPrimary() + (this._eInput.checked ? '-selected' : ''));
    }

    /**
     * 设置当前选项是否选中。
     * @protected
     *
     * @param {boolean} status 当前项是否选中，默认选中
     */
    UI_MULTI_SELECT_ITEM_CLASS.setSelected = function (status) {
        this.$setSelected(status);
        UI_MULTI_SELECT_FLUSH_SELECTALL(this.getParent(), this._bSelectAllBtn ? status : undefined);
        UI_MULTI_SELECT_FLUSH_TEXT(this.getParent());
    };

    /**
     * 选项控件发生变化的处理。
     * 在 选项组接口 中，选项控件发生增加/减少操作时调用此方法。
     * @protected
     */
    UI_MULTI_SELECT_CLASS.$alterItems = function () {
        UI_SELECT_CLASS.$alterItems.call(this);
        UI_MULTI_SELECT_FLUSH_SELECTALL(this);
        UI_MULTI_SELECT_FLUSH_TEXT(this);
    };

    /**
     * 控件增加子控件事件的默认处理。
     * 选项组增加子选项时需要判断子控件的类型，并额外添加引用。
     * @protected
     *
     * @param {ecui.ui.Item} child 选项控件
     * @return {boolean} 是否允许增加子控件，默认允许
     */
    UI_MULTI_SELECT_CLASS.$append = function (item) {
        UI_SELECT_CLASS.$append.call(this, item);
        this.getMain().appendChild(setInput(item._eInput, this.getName()));
    };

    /**
     * 计算控件的缓存。
     * 控件缓存部分核心属性的值，提高控件属性的访问速度，在子控件或者应用程序开发过程中，如果需要避开控件提供的方法(setSize、alterClass 等)直接操作 Element 对象，操作完成后必须调用 clearCache 方法清除控件的属性缓存，否则将引发错误。
     * @protected
     *
     * @param {CssStyle} style 基本 Element 对象的 Css 样式对象
     * @param {boolean} cacheSize 是否需要缓存控件大小，如果控件是另一个控件的部件时，不缓存大小能加快渲染速度，默认缓存
     */
    UI_MULTI_SELECT_CLASS.$cache = UI_SELECT_CLASS.$cache;

    /**
     * 界面点击强制拦截事件的默认处理。
     * 控件在多选下拉框展开时，需要拦截浏览器的点击事件，如果点击在下拉选项区域，则选中当前项，否则直接隐藏下拉选项框，但不会改变控件激活状态。
     * @protected
     *
     * @param {Event} event 事件对象
     */
    UI_MULTI_SELECT_CLASS.$intercept = function (event) {
        for (var control = event.getControl(); control; control = control.getParent()) {
            if (control instanceof UI_MULTI_SELECT_ITEM) {
                //当多选框选项为ECUI控件时无法释放拦截，此处fix一下，by hades
                event.target = control.getOuter();
                return false;
            }
        }
        /* TODO:1把此行的单行注释去掉，把else里面的内容更换
        this.$getSection('Options').hide();
        triggerEvent(this, 'change');
        /*/
        var allValue = this.getValue();
        if(allValue.length > 0) {
            this.$getSection('Options').hide();
            triggerEvent(this, 'change');

        }
        else {
            // alert('请至少选择一项');
            // control.$getSection('Text').setContent('请至少选择一项');
        }
        //*/
        event.exit();
    };

    /**
     * 控件拥有焦点时，键盘按下/弹起事件的默认处理。
     * 如果控件处于可操作状态(参见 isEnabled)，keyup 方法触发 onkeyup 事件，如果事件返回值不为 false，则调用 $keyup 方法。
     * @protected
     *
     * @param {Event} event 事件对象
     */
    UI_MULTI_SELECT_CLASS.$keydown = UI_MULTI_SELECT_CLASS.$keypress = UI_MULTI_SELECT_CLASS.$keyup =
        function (event) {
            UI_INPUT_CONTROL_CLASS['$' + event.type].call(this, event);
            if (!this.$getSection('Options').isShow()) {
                return false;
            }

            var key = getKey();
            if (key == 13 || key == 32) {
                if (event.type == 'keyup') {
                    key = this.getActived();
                    key.setSelected(!key.isSelected());
                }
                return false;
            }
        };

    /**
     * 鼠标在控件区域滚动滚轮事件的默认处理。
     * 如果控件拥有焦点，则当前选中项随滚轮滚动而自动指向前一项或者后一项。如果控件处于可操作状态(参见 isEnabled)，mousewheel 方法触发 onmousewheel 事件，如果事件返回值不为 false，则调用 $mousewheel 方法。
     * @protected
     *
     * @param {Event} event 事件对象
     */
    UI_MULTI_SELECT_CLASS.$mousewheel = function (event) {
        var options = this.$getSection('Options');
        if (options.isShow()) {
            options.$mousewheel(event);
        }
        return false;
    };

    /**
     * 控件激活状态结束事件的默认处理。
     * @protected
     *
     * @param {Event} event 事件对象
     */
    UI_MULTI_SELECT_CLASS.$deactivate = UI_SELECT_CLASS.$deactivate;

    /**
     * 控件激活状态开始事件的默认处理。
     * @protected
     *
     * @param {Event} event 事件对象
     */
    UI_MULTI_SELECT_CLASS.$activate = function (event) {
        var con = event.getControl();
        if (!(con instanceof UI_MULTI_SELECT_ITEM)) {
            UI_SELECT_CLASS.$activate.call(this, event);
        }
    }

    /**
     * 控件自动渲染全部完成后的处理。
     * 页面刷新时，部分浏览器会回填输入框的值，需要在回填结束后触发设置控件的状态。
     * @protected
     */
    UI_MULTI_SELECT_CLASS.$ready = function () {
        UI_MULTI_SELECT_FLUSH_SELECTALL(this);
        UI_MULTI_SELECT_FLUSH_TEXT(this);

        if (this._bInitSelectAll) {
            for (var i = 0, list = this.getItems(), o; o = list[i++]; ) {
                !o._bSelectAllBtn && o.setSelected(true);
            }
        }
    };

    /**
     * 控件移除子控件事件的默认处理。
     * 选项组移除子选项时需要额外移除引用。
     * @protected
     *
     * @param {ecui.ui.Item} child 选项控件
     */
    UI_MULTI_SELECT_CLASS.$remove = function (item) {
        UI_SELECT_CLASS.$remove.call(this, item);
        this.getMain().removeChild(item._eInput);
    };

    /**
     * 设置控件的大小。
     * @protected
     *
     * @param {number} width 宽度，如果不需要设置则将参数设置为等价于逻辑非的值
     * @param {number} height 高度，如果不需要设置则省略此参数
     */
    UI_MULTI_SELECT_CLASS.$setSize = UI_SELECT_CLASS.$setSize;

    /**
     * 获取全部选中的选项控件。
     * @protected
     *
     * @return {Array} 选项控件列表
     */
    UI_MULTI_SELECT_CLASS.getSelected = function () {
        for (var i = 0, list = this.getItems(), o, result = []; o = list[i++]; ) {
            if (o.isSelected() && !o._bSelectAllBtn) {
                result.push(o);
            }
        }
        return result;
    };

    UI_MULTI_SELECT_CLASS.getValue = function () {
        var selectItems = this.getSelected(),
            items,
            res = [], i, len;
        for (i = 0, len = selectItems.length; i < len; i++) {
            if (!selectItems[i]._bSelectAllBtn) {
                res.push(selectItems[i]._eInput.value);
            }
        }
        /* TODO:1把下面这一大段注释掉便可
        // 如果有全选按钮，且一个都没选中，那么传的值为所有的值
        if (this._bSelectAllBtn && res.length === 0) {
            items = this.getItems();
            for (i = 1, len = items.length; i < len; i++) {
                res.push(items[i]._eInput.value);
            }
        }
        /*/
        //*/
        return res;
    };

    /**
     * 获取全部选项的值
     * @return {Array} 所有选项的值的列表
     */
    UI_MULTI_SELECT_CLASS.getAllValue = function() {
        var items = this.getItems();
        var res = [];
        var i = 0;
        for (i = 0; i < items.length; i++) {
            if (!items[i]._bSelectAllBtn) {
                res.push(items[i].getValue());
            }
        }
        return res;
    };

    UI_MULTI_SELECT_CLASS.selectAll = function () {
        for (var i = 0, list = this.getItems(), o; o = list[i++]; ) {
            !o._bSelectAllBtn && o.setSelected(true);
        }
    };

    UI_MULTI_SELECT_CLASS.isSelectAll = function () {
        for (var i = 0, list = this.getItems(), o; o = list[i++]; ) {
            if (!o.isSelected()) {
                return false;
            }
        }
        return true;
    };

    /**
     * 设置下拉框允许显示的选项数量。
     * 如果实际选项数量小于这个数量，没有影响，否则将出现垂直滚动条，通过滚动条控制其它选项的显示。
     * @public
     *
     * @param {number} value 显示的选项数量，必须大于 1
     */
    UI_MULTI_SELECT_CLASS.setOptionSize = UI_SELECT_CLASS.setOptionSize;

    /**
     * 设置控件的值。
     * @public
     *
     * @param {Array/String} values 控件被选中的值列表
     */
    UI_MULTI_SELECT_CLASS.setValue = function (values) {
        if ('[object Array]' != Object.prototype.toString.call(values)) {
            values = values.toString().split(',');
        }
        for (var i = 0, list = this.getItems(), o; o = list[i++]; ) {
            o.setSelected(indexOf(values, o._eInput.value) >= 0);
        }
        UI_MULTI_SELECT_FLUSH_SELECTALL(this);
        UI_MULTI_SELECT_FLUSH_TEXT(this);
    };


//{/if}//
//{if 0}//
})();
//{/if}//
