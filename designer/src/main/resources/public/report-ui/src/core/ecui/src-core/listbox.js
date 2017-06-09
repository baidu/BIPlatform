/*
Listbox - 定义了多项选择的基本操作。
多选框控件，继承自截面控件，实现了选项组接口，多个交换框，可以将选中的选项在互相之间移动。多选框控件也可以单独的使用，选中的选项在表单提交时将被提交。

多选框控件直接HTML初始化的例子:
<div ecui="type:listbox;name:test">
    <!-- 这里放选项内容 -->
    <li>选项</li>
    ...
</div>

属性
_sName  - 多选框内所有input的名称

选项属性
_eInput - 选项对应的input，form提交时使用
*/
//{if 0}//
(function () {

    var core = ecui,
        dom = core.dom,
        ui = core.ui,
        util = core.util,

        undefined,

        getText = dom.getText,
        setInput = dom.setInput,
        extend = util.extend,

        inheritsControl = core.inherits,

        UI_PANEL = ui.Panel,
        UI_ITEM = ui.Item,
        UI_ITEM_CLASS = UI_ITEM.prototype,
        UI_ITEMS = ui.Items;
//{/if}//
//{if $phase == "define"}//
    /**
     * 初始化多选框控件。
     * @public
     *
     * @param {Object} options 初始化选项
     */
    ///__gzip_original__UI_LISTBOX
    ///__gzip_original__UI_LISTBOX_ITEM
    var UI_LISTBOX = ui.Listbox =
        inheritsControl(
            UI_PANEL,
            'ui-listbox',
            function (el, options) {
                options.hScroll = false;
            },
            function (el, options) {
                this._sName = options.name || '';
                this.$initItems();
            }
        ),
        UI_LISTBOX_CLASS = UI_LISTBOX.prototype,

        /**
         * 初始化多选框控件的选项部件。
         * @public
         *
         * @param {Object} options 初始化选项
         */
        UI_LISTBOX_ITEM_CLASS = (UI_LISTBOX_CLASS.Item = inheritsControl(
            UI_ITEM,
            null,
            null,
            function (el, options) {
                el.appendChild(this._eInput = setInput(null, options.parent._sName, 'hidden')).value =
                    options.value === undefined ? getText(el) : options.value;
                this.setSelected(!!options.selected);
            }
        )).prototype;
//{else}//
    extend(UI_LISTBOX_CLASS, UI_ITEMS);

    /**
     * @override
     */
    UI_LISTBOX_ITEM_CLASS.$click = function (event) {
        UI_ITEM_CLASS.$click.call(this, event);
        this.setSelected(!this.isSelected());
    };

    /**
     * @override
     */
    UI_LISTBOX_ITEM_CLASS.$dispose = function () {
        this._eInput = null;
        UI_ITEM_CLASS.$dispose.call(this);
    };

    /**
     * @override
     */
    UI_LISTBOX_ITEM_CLASS.$setParent = function (parent) {
        UI_ITEM_CLASS.$setParent.call(this, parent);

        if (parent instanceof UI_LISTBOX) {
            this._eInput = setInput(this._eInput, parent._sName);
        }
    };

    /**
     * 判断多选框的选项控件是否被选中。
     * @public
     *
     * @return {boolean} 选项是否被选中
     */
    UI_LISTBOX_ITEM_CLASS.isSelected = function () {
        return !this._eInput.disabled;
    };

    /**
     * 设置选中状态。
     * @public
     *
     * @param {boolean} status 是否选中，默认为选中
     */
    UI_LISTBOX_ITEM_CLASS.setSelected = function (status) {
        this.alterClass('selected', this._eInput.disabled = status === false);
    };

    /**
     * @override
     */
    UI_LISTBOX_CLASS.$alterItems = function () {
        //__transform__items_list
        var items = this.getItems(),
            vscroll = this.$getSection('VScrollbar'),
            step = items.length && items[0].getHeight();

        if (step) {
            vscroll.setStep(step);
            this.setItemSize(
                this.getBodyWidth() - (items.length * step > this.getBodyHeight() ? vscroll.getWidth() : 0),
                step
            );
            this.$setSize(0, this.getHeight());
        }
    };

    /**
     * 获取控件的表单项名称。
     * 多选框控件可以在表单中被提交，getName 方法返回提交时用的表单项名称，表单项名称可以使用 setName 方法改变。
     * @public
     *
     * @return {string} 表单项名称
     */
    UI_LISTBOX_CLASS.getName = function () {
        return this._sName;
    };

    /**
     * 获取所有选中的选项。
     * @public
     *
     * @return {Array} 选项数组
     */
    UI_LISTBOX_CLASS.getSelected = function () {
        for (var i = 0, list = this.getItems(), o, result = []; o = list[i++]; ) {
            if (o.isSelected()) {
                result.push(o);
            }
        }
        return result;
    };

    /**
     * 选中所有的选项。
     * 某些场景下，需要多选框控件的内容都可以被提交，可以在表单的 onsubmit 事件中调用 selectAll 方法全部选择。
     * @public
     */
    UI_LISTBOX_CLASS.selectAll = function () {
        for (var i = 0, list = this.getItems(), o; o = list[i++]; ) {
            o.setSelected();
        }
    };

    /**
     * 设置控件的表单项名称。
     * 多选框控件可以在表单中被提交，setName 方法设置提交时用的表单项名称，表单项名称可以使用 getName 方法获取。
     * @public
     *
     * @param {string} name 提交用的名称
     */
    UI_LISTBOX_CLASS.setName = function (name) {
        for (var i = 0, list = this.getItems(), o; o = list[i++]; ) {
            // 需要将下属所有的输入框名称全部改变
            o._eInput = setInput(o._eInput, name);
        }
        this._sName = name;
    };
//{/if}//
//{if 0}//
})();
//{/if}//