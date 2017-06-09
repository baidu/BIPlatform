/*
CheckTree - 定义包含复选框的树形结构的基本操作。
包含复选框的树控件，继承自树控件。每一个选项包含一个复选框进行选择，除非特别的指定，否则子节点的复选框与父节点的复选框
自动联动。

树控件直接HTML初始化的例子:
<div ecui="type:check-tree;fold:true;id:parent;name:part">
    <!-- 当前节点的文本，如果没有整个内容就是节点的文本 -->
    <label>节点的文本</label>
    <!-- 这里放子控件，如果需要fold某个子控件，将子控件的style="display:none"即可 -->
    <li ecui="superior:other">子控件文本</li>
    <li>子控件文本(复选框默认与父控件复选框联动)</li>
    ...
</div>

属性
_oSuperior - 关联的父复选框控件ID，默认与父控件复选框关联
_uCheckbox - 复选框控件
*/
//{if 0}//
(function () {

    var core = ecui,
        dom = core.dom,
        ui = core.ui,
        util = core.util,

        inheritsControl = core.inherits,
        createDom = dom.create,

        $connect = core.$connect,
        $fastCreate = core.$fastCreate,

        UI_CHECKBOX = ui.Checkbox,

        UI_TREE_VIEW = ui.TreeView,
        UI_TREE_VIEW_CLASS = UI_TREE_VIEW.prototype;
//{/if}//
//{if $phase == "define"}//
    /**
     * 初始化复选树控件。
     * options 对象支持的属性如下：
     * name 复选框的表单项的默认名称
     * value 复选框的表单项的值
     * superior 父复选框的标识，如果为true表示自动使用上级树节点作为父复选框，其它等价false的值表示不联动
     * @public
     *
     * @param {Object} options 初始化选项
     */
    //__gzip_original__UI_CHECK_TREE
    var UI_CHECK_TREE = ui.CheckTree = 
        inheritsControl(
            UI_TREE_VIEW,
            'ui-check-tree',
            function (el, options) {
                this._oSuperior = options.superior;

                for (
                    var i = 0,
                        checkbox = this._uCheckbox = $fastCreate(
                            UI_CHECKBOX,
                            el.insertBefore(createDom(UI_CHECKBOX.types[0]), el.firstChild),
                            this,
                            {name: options.name, value: options.value, disabled: options.disabled}
                        ),
                        list = this.getChildren();
                    el = list[i++];
                ) {
                    options = el._oSuperior
                    if (options !== false) {
                        el = el._uCheckbox;
                        if (!options) {
                            el.setSubject(checkbox);
                        }
                        else {
                            $connect(el, el.setSubject, options);
                        }
                    }
                }
            }
        ),
        UI_CHECK_TREE_CLASS = UI_CHECK_TREE.prototype;
//{else}//
    /**
     * 计算控件的缓存。
     * 控件缓存部分核心属性的值，提高控件属性的访问速度，在子控件或者应用程序开发过程中，如果需要避开控件提供的方法(setSize、alterClass 等)直接操作 Element 对象，操作完成后必须调用 clearCache 方法清除控件的属性缓存，否则将引发错误。
     * @protected
     *
     * @param {CssStyle} style 基本 Element 对象的 Css 样式对象
     * @param {boolean} cacheSize 是否需要缓存控件大小，如果控件是另一个控件的部件时，不缓存大小能加快渲染速度，默认缓存
     */
    UI_CHECK_TREE_CLASS.$cache = function (style, cacheSize) {
        UI_TREE_VIEW_CLASS.$cache.call(this, style, cacheSize);
        this._uCheckbox.cache(true, true);
    };

    /**
     * 控件渲染完成后初始化的默认处理。
     * $init 方法在控件渲染完成后调用，参见 create 与 init 方法。
     * @protected
     */
    UI_CHECK_TREE_CLASS.init = function () {
        UI_TREE_VIEW_CLASS.init.call(this);
        this._uCheckbox.init();
    };

    /**
     * 获取包括当前树控件在内的全部选中的子树控件。
     * @public
     *
     * @return {Array} 全部选中的树控件列表
     */
    UI_CHECK_TREE_CLASS.getChecked = function () {
        for (var i = 0, list = this.getChildren(), result = this.isChecked() ? [this] : [], o; o = list[i++]; ) {
            result = result.concat(o.getChecked());    
        }
        return result;
    };

    /**
     * 获取当前树控件复选框的表单项的值。
     * @public
     *
     * @return {string} 表单项的值
     */
    UI_CHECK_TREE_CLASS.getValue = function () {
        return this._uCheckbox.getValue();
    };

    /**
     * 判断树控件是否选中。
     * @public
     *
     * @return {boolean} 是否选中
     */
    UI_CHECK_TREE_CLASS.isChecked = function () {
        return this._uCheckbox.isChecked();
    };

    /**
     * 设置当前树控件复选框选中状态。
     * @public
     *
     * @param {boolean} 是否选中当前树控件复选框
     */
    UI_CHECK_TREE_CLASS.setChecked = function (status) {
        this._uCheckbox.setChecked(status);    
    };

    UI_CHECK_TREE_CLASS.disable = function () {
        this._uCheckbox.disable();
        UI_TREE_VIEW_CLASS.disable.call(this);
    };

    UI_CHECK_TREE_CLASS.enable = function () {
        this._uCheckbox.enable();
        UI_CHECK_TREE_CLASS.enable.call(this);
    };

    UI_CHECK_TREE_CLASS.add = function (item, index, options) {
        var con = UI_TREE_VIEW_CLASS.add.call(this, item, index, options);
        if (con._oSuperior !== false) {
            if (!con._oSuperior) {
                con._uCheckbox.setSubject(this._uCheckbox);
            }
            else {
                con._uCheckbox.setSubject(con._oSuperior);
            }
        }
        return con;
    };

    UI_CHECK_TREE_CLASS.$ready = function () {
        this._uCheckbox.$ready();
    }
//{/if}//
//{if 0}//
})();
//{/if}//
