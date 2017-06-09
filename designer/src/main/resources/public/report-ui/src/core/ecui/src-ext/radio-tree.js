/*
RadioTree - 定义单选框的树形结构的基本操作。
包含单选框的树控件，继承自树控件，每次点击可以选择一个树节点。

树控件直接HTML初始化的例子:
<div ecui="type:radio-tree;fold:true;name:part">
    <!-- 当前节点的文本，如果没有整个内容就是节点的文本 -->
    <label>节点的文本</label>
    <!-- 这里放子控件，如果需要fold某个子控件，将子控件的style="display:none"即可 -->
    <li>子控件文本</li>
    ...
</div>

属性
_sName     - 节点项的名称
_sValue    - 节点项的值
_eInput    - 树的根节点拥有，保存树对应的提交 INPUT
_cSelected - 树的根节点拥有，保存当前选中的项
*/
//{if 0}//
(function () {
 
    var core = ecui,
        dom = core.dom,
        ui  = core.ui,
        util = core.util,

        getStyle = dom.getStyle,
        removeDom = dom.remove,
        setInput = dom.setInput,
        blank = util.blank,
        inherits = util.inherits,
        toNumber = util.toNumber,

        getMouseX = core.getMouseX,

        UI_TREE = ui.Tree,
        UI_TREE_CLASS = UI_TREE.prototype;
//{/if}//
//{if $phase == "define"}//
    /**
     * 初始化单选树控件。
     * options 对象支持的属性如下：
     * name 单选框的表单项的默认名称
     * value 单选框的表单项的值
     * @public
     *
     * @param {Object} options 初始化选项
     */
    //__gzip_original__UI_RADIO_TREE
    var UI_RADIO_TREE = 
        ui.RadioTree = function (el, options) {
            UI_TREE.call(this, el, options);
            this._sName = options.name;
            this._sValue = options.value;
        },
        UI_RADIO_TREE_CLASS = inherits(UI_RADIO_TREE, UI_TREE);
//{else}//
    /**
     * 设置当前树控件的表单提交项的值
     * @private
     *
     * @param {ecui.ui.RadioTree} tree 树控件
     * @param {InputElement} input 输入框 Element 对象
     */
    function UI_RADIO_TREE_SETVALUE(tree, input) {
        tree.getBody().appendChild(tree._eInput = setInput(input, tree._sName, 'hidden'));
        tree._eInput.value = tree._sValue;
    }

    /**
     * 鼠标单击控件事件的默认处理。
     * @protected
     *
     * @param {Event} event 事件对象
     */
    UI_RADIO_TREE_CLASS.$click = function (event) {
        if (getMouseX(this) <= toNumber(getStyle(this.getBody(), 'paddingLeft'))) {
            var root = this.getRoot(),
                selected = root._cSelected;

            if (selected != this) {
                if (selected) {
                    selected.alterClass('selected', true);
                    selected = selected._eInput;
                }
                UI_RADIO_TREE_SETVALUE(this, selected);
                this.alterClass('selected');
                root._cSelected = this;
            }

            this.setFold = blank;
        }

        UI_TREE_CLASS.$click.call(this, event);
        delete this.setFold;
    };

    /**
     * 销毁控件的默认处理。
     * 页面卸载时将销毁所有的控件，释放循环引用，防止在 IE 下发生内存泄漏，$dispose 方法的调用不会受到 ondispose 事件返回值的影响。
     * @protected
     */
    UI_RADIO_TREE_CLASS.$dispose = function () {
        this._eInput = null;
        UI_TREE_CLASS.$dispose.call(this);
    };

    /**
     * 直接设置父控件。
     * @protected
     *
     * @param {ecui.ui.Control} parent ECUI 控件对象
     */
    UI_RADIO_TREE_CLASS.$setParent = function (parent) {
        var root = this.getRoot(),
            selected = root._cSelected;

        UI_TREE_CLASS.$setParent.call(this, parent);

        if (this == selected) {
            selected.alterClass('selected', true);
            if (selected._eInput) {
                removeDom(selected._eInput);
            }
            root._cSelected = null;
        }

        selected = this._cSelected;
        if (selected) {
            selected.alterClass('selected', true);
            if (selected._eInput) {
                removeDom(selected._eInput);
            }
            this._cSelected = null;
        }
    };

    /**
     * 获取控件的表单项名称。
     * @public
     *
     * @return {string} INPUT 对象名称
     */
    UI_RADIO_TREE_CLASS.getName = function () {
        return this._sName;
    };

    /**
     * 获取当前树控件选中的项。
     * @public
     *
     * @return {ecui.ui.Tree} 树控件选中的项
     */
    UI_RADIO_TREE_CLASS.getSelected = function () {
        return this.getRoot()._cSelected;
    };

    /**
     * 获取控件的值。
     * @public
     *
     * @return {string} 控件的值
     */
    UI_RADIO_TREE_CLASS.getValue = function () {
        return this._sValue;
    };
//{/if}//
//{if 0}//
})();
//{/if}//