/*
Radio - 定义一组选项中选择唯一选项的基本操作。
单选框控件，继承自输入框控件，实现了对原生 InputElement 单选框的功能扩展，支持对选中的图案的选择。单选框控件适用所有在一组中只允许选择一个目标的交互，并不局限于此分组的表现形式(文本、图片等)。

单选框控件直接HTML初始化的例子:
<input ecui="type:radio" name="city" value="beijing" checked="checked" type="radio">
或
<div ecui="type:radio;name:city;value:beijing;checked:true"></div>
或
<div ecui="type:radio">
  <input name="city" value="beijing" checked="checked" type="radio">
</div>

属性
_bDefault  - 默认的选中状态
*/
//{if 0}//
(function () {

    var core = ecui,
        ui = core.ui,
        util = core.util,

        undefined,

        setDefault = util.setDefault,

        getKey = core.getKey,
        inheritsControl = core.inherits,
        query = core.query,

        UI_INPUT_CONTROL = ui.InputControl,
        UI_INPUT_CONTROL_CLASS = UI_INPUT_CONTROL.prototype;
//{/if}//
//{if $phase == "define"}//
    ///__gzip_original__UI_RADIO
    ///__gzip_original__UI_RADIO_CLASS
    /**
     * 初始化单选框控件。
     * options 对象支持的属性如下：
     * @public
     *
     * @param {Object} options 初始化选项
     */
    var UI_RADIO = ui.Radio =
        inheritsControl(
            UI_INPUT_CONTROL,
            'ui-radio',
            function (el, options) {
                setDefault(options, 'hidden', true);
                setDefault(options, 'inputType', 'radio');
            },
            function (el, options) {
                // 保存节点选中状态，用于修复IE6/7下移动DOM节点时选中状态发生改变的问题
                this._bDefault = this.getInput().defaultChecked;
            }
        ),
        UI_RADIO_CLASS = UI_RADIO.prototype;
//{else}//
    /**
     * 单选框控件刷新。
     * @private
     *
     * @param {ecui.ui.Radio} control 单选框控件
     * @param {boolean|undefined} checked 新的状态，如果忽略表示不改变当前状态
     */
    function UI_RADIO_FLUSH(control, checked) {
        if (checked !== undefined) {
            var el = control.getInput();
            el.defaultChecked = el.checked = checked;
        }
        control.setClass(control.getPrimary() + (control.isChecked() ? '-checked' : ''));
    }

    /**
     * 控件点击时将控件设置成为选中状态，同时取消同一个单选框控件组的其它控件的选中状态。
     * @override
     */
    UI_RADIO_CLASS.$click = function (event) {
        UI_INPUT_CONTROL_CLASS.$click.call(this, event);
        this.setChecked(true);
    };

    /**
     * 接管对空格键的处理。
     * @override
     */
    UI_RADIO_CLASS.$keydown = UI_RADIO_CLASS.$keypress = UI_RADIO_CLASS.$keyup = function (event) {
        UI_INPUT_CONTROL_CLASS['$' + event.type].call(this, event);
        if (event.which == 32) {
            if (event.type == 'keyup' && getKey() == 32) {
                this.setChecked(true);
            }
            event.preventDefault();
        }
    };

    /**
     * @override
     */
    UI_RADIO_CLASS.$ready = function () {
        UI_RADIO_FLUSH(this);
    };

    /**
     * @override
     */
    UI_RADIO_CLASS.$reset = function (event) {
        // 修复IE6/7下移动DOM节点时选中状态发生改变的问题
        this.getInput().checked = this._bDefault;
        UI_INPUT_CONTROL_CLASS.$reset.call(this, event);
    };

    /**
     * 获取与当前单选框同组的全部单选框。
     * getItems 方法返回包括当前单选框在内的与当前单选框同组的全部单选框，同组的单选框选中状态存在唯一性。
     * @public
     *
     * @return {Array} 单选框控件数组
     */
    UI_RADIO_CLASS.getItems = function () {
        //__gzip_original__form
        var i = 0,
            list = this.getInput(),
            form = list.form,
            o = list.name,
            result = [];

        if (!o) {
            return [this];
        }
        else if (form) {
            // 必须 name 也不为空，否则 form[o] 的值在部分浏览器下将是空
            for (list = form[o]; o = list[i++]; ) {
                if (o.getControl) {
                    result.push(o.getControl());
                }
            }
            return result;
        }
        else {
            return query({type: UI_RADIO, custom: function (control) {
                return !control.getInput().form && control.getName() == o;
            }});
        }
    };

    /**
     * 判断控件是否选中。
     * @public
     *
     * @return {boolean} 是否选中
     */
    UI_RADIO_CLASS.isChecked = function () {
        return this.getInput().checked;
    };

    /**
     * 设置单选框控件选中状态。
     * 将控件设置成为选中状态，会取消同一个单选框控件组的其它控件的选中状态。
     * @public
     *
     * @param {boolean} checked 是否选中
     */
    UI_RADIO_CLASS.setChecked = function (checked) {
        if (this.isChecked() != checked) {
            if (checked) {
                for (var i = 0, list = this.getItems(), o; o = list[i++]; ) {
                    UI_RADIO_FLUSH(o, o == this);
                }
            }
            else {
                UI_RADIO_FLUSH(this, false);
            }
        }
    };

    /**
     * override
     */
    UI_RADIO_CLASS.setDefaultValue = function () {
        this._bDefault = this.isChecked();
    };
//{/if}//
//{if 0}//
})();
//{/if}//
