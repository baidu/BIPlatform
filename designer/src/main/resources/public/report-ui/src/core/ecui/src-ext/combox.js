/*
Combox - 定义可输入下拉框行为的基本操作。
可输入下拉框控件，继承自下拉框控件，在下拉框控件的基础上允许选项框可输入内容。

可输入下拉框控件直接HTML初始化的例子:
<select ecui="type:combox" name="age">
  <option value="20">20</option>
  <option value="21" selected="selected">21</option>
  <option value="22">22</option>
</select>
或
<div ecui="type:combox;name:age;value:21">
  <div ecui="value:20">20</div>
  <div ecui="value:21">21</div>
  <div ecui="value:22">22</div>
</div>

如果需要自定义特殊的选项效果，请按下列方法初始化:
<div ecui="type:combox">
    <!-- 如果ec中不指定name，也可以在input中指定 -->
    <input name="test" />
    <!-- 这里放选项内容 -->
    <li value="值">文本</li>
    ...
</div>
*/
//{if 0}//
(function () {

    var core = ecui,
        ui = core.ui,

        inheritsControl = core.inherits,

        UI_SELECT = ui.Select,
        UI_SELECT_CLASS = UI_SELECT.prototype;
//{/if}//
//{if $phase == "define"}//
    ///__gzip_original__UI_COMBOX
    ///__gzip_original__UI_COMBOX_CLASS
    /**
     * 初始化可输入下拉框控件。
     * options 对象支持的属性如下：
     * @public
     *
     * @param {Object} options 初始化选项
     */
    var UI_COMBOX = ui.Combox =
        inheritsControl(
            UI_SELECT,
            '*ui-combox',
            function (el, options) {
                this.$getSection('Text').getOuter().style.display = 'none';
            },
            function (el, options) {
                options.hidden = false;
            }
        ),
        UI_COMBOX_CLASS = UI_COMBOX.prototype;
//{else}//
    /**
     * 设置控件的大小。
     * @protected
     *
     * @param {number} width 宽度，如果不需要设置则将参数设置为等价于逻辑非的值
     * @param {number} height 高度，如果不需要设置则省略此参数
     */
    UI_COMBOX_CLASS.$setSize = function (width, height) {
        UI_SELECT_CLASS.$setSize.call(this, width, height);
        this.getInput().style.width = this.$getSection('Text').getWidth() + 'px';
    };
//{/if}//
//{if 0}//
})();
//{/if}//