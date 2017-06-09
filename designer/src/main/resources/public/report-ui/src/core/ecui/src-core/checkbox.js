/*
Checkbox - 定义单个设置项选择状态的基本操作。
复选框控件，继承自输入控件，实现了对原生 InputElement 复选框的功能扩展，支持复选框之间的主从关系定义。当一个复选框的“从复选框”选中一部分时，“主复选框”将处于半选状态，这种状态逻辑意义上等同于未选择状态，但显示效果不同，复选框的主从关系可以有多级。复选框控件适用所有在一组中允许选择多个目标的交互，并不局限于此分组的表现形式(文本、图片等)。

复选框控件直接HTML初始化的例子:
<input ecui="type:checkbox;subject:china" name="city" value="beijing" checked="checked" type="checkbox">
或
<div ecui="type:checkbox;name:city;value:beijing;checked:true;subject:china"></div>
或
<div ecui="type:checkbox;subject:china">
  <input name="city" value="beijing" checked="checked" type="checkbox">
</div>

属性
_bDefault        - 默认的选中状态
_nStatus         - 复选框当前的状态，0--全选，1--未选，2--半选
_cSubject        - 主复选框
_aDependents     - 所有的从属复选框
*/
//{if 0}//
(function () {

    var core = ecui,
        array = core.array,
        ui = core.ui,
        util = core.util,

        undefined,

        remove = array.remove,
        setDefault = util.setDefault,

        $connect = core.$connect,
        getKey = core.getKey,
        inheritsControl = core.inherits,

        UI_INPUT_CONTROL = ui.InputControl,
        UI_INPUT_CONTROL_CLASS = UI_INPUT_CONTROL.prototype;
//{/if}//
//{if $phase == "define"}//
    ///__gzip_original__UI_CHECKBOX
    ///__gzip_original__UI_CHECKBOX_CLASS
    /**
     * 初始化复选框控件。
     * options 对象支持的属性如下：
     * subject 主复选框 ID，会自动与主复选框建立关联后，作为主复选框的从属复选框之一
     * @public
     *
     * @param {Object} options 初始化选项
     */
    var UI_CHECKBOX = ui.Checkbox =
        inheritsControl(
            UI_INPUT_CONTROL,
            'ui-checkbox',
            function (el, options) {
                setDefault(options, 'hidden', true);
                setDefault(options, 'inputType', 'checkbox');
            },
            function (el, options) {
                // 保存节点选中状态，用于修复IE6/7下移动DOM节点时选中状态发生改变的问题
                this._bDefault = this.getInput().defaultChecked;
                this._aDependents = [];

                $connect(this, this.setSubject, options.subject);
            }
        ),
        UI_CHECKBOX_CLASS = UI_CHECKBOX.prototype;
//{else}//
    /**
     * 改变复选框状态。
     * @private
     *
     * @param {ecui.ui.Checkbox} control 复选框对象
     * @param {number} status 新的状态，0--全选，1--未选，2--半选
     */
    function UI_CHECKBOX_CHANGE(control, status) {
        if (status !== control._nStatus) {
            // 状态发生改变时进行处理
            control.setClass(control.getPrimary() + ['-checked', '', '-part'][status]);

            control._nStatus = status;

            var el = control.getInput();
            el.defaultChecked = el.checked = !status;

            // 如果有主复选框，刷新主复选框的状态
            if (control._cSubject) {
                UI_CHECKBOX_FLUSH(control._cSubject);
            }
        }
    }

    /**
     * 复选框控件刷新，计算所有从复选框，根据它们的选中状态计算自身的选中状态。
     * @private
     *
     * @param {ecui.ui.Checkbox} control 复选框控件
     */
    function UI_CHECKBOX_FLUSH(control) {
        for (var i = 0, status, o; o = control._aDependents[i++]; ) {
            if (status !== undefined && status != o._nStatus) {
                status = 2;
                break;
            }
            status = o._nStatus;
        }

        if (status !== undefined) {
            UI_CHECKBOX_CHANGE(control, status);
        }
    }

    /**
     * 控件点击时改变当前的选中状态。
     * @override
     */
    UI_CHECKBOX_CLASS.$click = function (event) {
        UI_INPUT_CONTROL_CLASS.$click.call(this, event);
        this.setChecked(!!this._nStatus);
    };

    /**
     * @override
     */
    UI_CHECKBOX_CLASS.$dispose = function () {
        var arr = this._aDependents.slice(0),
            i, o;

        this.setSubject();
        for (i = 0; o = arr[i]; i++) {
            o.setSubject();
        }
        UI_INPUT_CONTROL_CLASS.$dispose.call(this);
    };

    /**
     * 接管对空格键的处理。
     * @override
     */
    UI_CHECKBOX_CLASS.$keydown = UI_CHECKBOX_CLASS.$keypress = UI_CHECKBOX_CLASS.$keyup = function (event) {
        UI_INPUT_CONTROL_CLASS['$' + event.type].call(this, event);
        if (getKey() == 32) {
            // 屏蔽空格键，防止屏幕发生滚动
            if (event.type == 'keyup') {
                this.setChecked(!!this._nStatus);
            }
            event.preventDefault();
        }
    };

    /**
     * @override
     */
    UI_CHECKBOX_CLASS.$ready = function () {
        if (!this._aDependents.length) {
            // 如果控件是主复选框，应该直接根据从属复选框的状态来显示自己的状态
            UI_CHECKBOX_CHANGE(this, this.getInput().checked ? 0 : 1);
        }
    };

    /**
     * @override
     */
    UI_CHECKBOX_CLASS.$reset = function (event) {
        // 修复IE6/7下移动DOM节点时选中状态发生改变的问题
        this.getInput().checked = this._bDefault;
        UI_INPUT_CONTROL_CLASS.$reset.call(this, event);
    };

    /**
     * 获取全部的从属复选框。
     * 复选框控件调用 setSubject 方法指定了主复选框后，它就是主复选框的从属复选框之一。
     * @public
     *
     * @return {Array} 复选框控件数组
     */
    UI_CHECKBOX_CLASS.getDependents = function () {
        return this._aDependents.slice();
    };

    /**
     * 获取主复选框。
     * getSubject 方法返回调用 setSubject 方法指定的主复选框控件。
     * @public
     *
     * @return {ecui.ui.Checkbox} 复选框控件
     */
    UI_CHECKBOX_CLASS.getSubject = function () {
        return this._cSubject || null;
    };

    /**
     * 判断控件是否选中。
     * @public
     *
     * @return {boolean} 是否选中
     */
    UI_CHECKBOX_CLASS.isChecked = function () {
        return !this._nStatus;
    };

    /**
     * 设置复选框控件选中状态。
     * @public
     *
     * @param {boolean} checked 是否选中
     */
    UI_CHECKBOX_CLASS.setChecked = function (checked) {
        UI_CHECKBOX_CHANGE(this, checked ? 0 : 1);
        // 如果有从属复选框，全部改为与当前复选框相同的状态
        for (var i = 0, o; o = this._aDependents[i++]; ) {
            o.setChecked(checked);
        }
    };

    /**
     * override
     */
    UI_CHECKBOX_CLASS.setDefaultValue = function () {
        this._bDefault = this.isChecked();
    };

    /**
     * 设置主复选框。
     * setSubject 方法指定主复选框控件后，可以通过访问主复选框控件的 getDependents 方法获取列表，列表中即包含了当前的控件。请注意，控件从 DOM 树上被移除时，不会自动解除主从关系，联动可能出现异情，此时请调用 setSubject 方法传入空参数解除主从关系。
     * @public
     *
     * @param {ecui.ui.Checkbox} checkbox 主复选框
     */
    UI_CHECKBOX_CLASS.setSubject = function (checkbox) {
        var oldSubject = this._cSubject;
        if (oldSubject != checkbox) {
            this._cSubject = checkbox;

            if (oldSubject) {
                // 已经设置过主复选框，需要先释放引用
                remove(oldSubject._aDependents, this);
                UI_CHECKBOX_FLUSH(oldSubject);
            }

            if (checkbox) {
                checkbox._aDependents.push(this);
                UI_CHECKBOX_FLUSH(checkbox);
            }
        }
    };
//{/if}//
//{if 0}//
})();
//{/if}//
