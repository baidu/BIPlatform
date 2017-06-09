/*
Label - 定义事件转发的基本操作。
标签控件，继承自基础控件，将事件转发到指定的控件上，通常与 Radio、Checkbox 等控件联合使用，扩大点击响应区域。

标签控件直接HTML初始化的例子:
<div ecui="type:label;for:checkbox"></div>

属性
_cFor - 被转发的控件对象
*/
//{if 0}//
(function () {

    var core = ecui,
        ui = core.ui,
        util = core.util,

        inheritsControl = core.inherits,
        $connect = core.$connect,
        triggerEvent = core.triggerEvent,
        blank = util.blank,

        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = UI_CONTROL.prototype,

        AGENT_EVENT = ['click', 'mouseover', 'mouseout', 'mouseup', 'mousedown'];
//{/if}//
//{if $phase == "define"}//
    /**
     * 初始化标签控件。
     * options 对象支持的属性如下：
     * for 被转发的控件 id
     * @public
     *
     * @param {Object} options 初始化选项
     */
    //__gzip_original__UI_LABEL
    var UI_LABEL = ui.Label = inheritsControl(
            UI_CONTROL,
            'ui-label',
            null,
            function (el, options) {
                this._bResizable = false;
                $connect(this, this.setFor, options['for']);
            }
        ),
        UI_LABEL_CLASS = UI_LABEL.prototype;
//{else}//
    /**
     * 设置控件的事件转发接收控件。
     * setFor 方法设置事件转发的被动接收者，如果没有设置，则事件不会被转发。
     * @public
     *
     * @param {ecui.ui.Control} control 事件转发接收控件
     */
    UI_LABEL_CLASS.setFor = function (control) {
        this._cFor = control;
    };


    UI_LABEL_CLASS.$setSize = blank;

    // 设置事件转发
    (function () {
        var i, name;
        
        for (i = 0; name = AGENT_EVENT[i]; i++) {
            UI_LABEL_CLASS['$' + name]  = (function (name) {
                return function (event) {
                    UI_CONTROL_CLASS['$' + name].call(this, event);

                    var control = this._cFor;
                    if (control && !control.isDisabled()) {
                        triggerEvent(control, name, event);
                    }
                };
            })(name);
        }
    })();
//{/if}//
//{if 0}//
})();
//{/if}//
