/**
 * di.shared.vui.MultiCheckbox
 * Copyright 2014 Baidu Inc. All rights reserved.
 *
 * @file:    多个复选框的组件
 * @author:  xuezhao(xuezhao)
 * @depend:  xui, xutil
 */

$namespace('di.shared.vui');

(function () {
    
    //------------------------------------------
    // 引用 
    //------------------------------------------

    var inheritsObject = xutil.object.inheritsObject;
    var XOBJECT = xui.XObject;

    //------------------------------------------
    // 类型声明 
    //------------------------------------------

    /**
     * 多个复选框的组件
     * 
     * @class
     * @extends xui.XView
     * @param {Object} options
     * @param {HTMLElement} options.el 容器元素
     */
    var MULTI_CHECKBOX = $namespace().MultiCheckbox = 
            inheritsObject(XOBJECT, constructor);
    var MULTI_CHECKBOX_CLASS = MULTI_CHECKBOX.prototype;
    
    //------------------------------------------
    // 方法
    //------------------------------------------

    /**
     * 构造函数
     *
     * @private
     * @param {Object} options 参数
     * @param {HTMLElement} options.el 容器元素（由di系统传入）
     * @param {string} options.id DOM元素的data-o_o-di属性值（由di系统传入）
     */
    function constructor(options) {
        this._eMain = options.el;
        
        this._eMain.innerHTML = '';
    };
    
    /**
     * 设置数据（根据数据源 生成组件内容）
     *
     * @param {Object} data 数据对象
     * @param {Array<Object>} data.datasource 数据源
     */
    MULTI_CHECKBOX_CLASS.setData = function (data) {
        render.call(this, data.datasource || []);
    };

    /**
     * 得到当前值
     *
     * @public
     * @return {Array.<string>} 当前数据
     */
    MULTI_CHECKBOX_CLASS.getValue = function () {
        var el = this._eMain;
        var inputs = el.getElementsByTagName('input');
        var checkedValues = [];
        
        for (var i = 0, input; input = inputs[i]; i++) {
            if (input.checked) {
                checkedValues.push(input.value);
            }
        }
        
        return checkedValues;
    };
    
    /**
     * 设置复选框组件的选中值
     *
     * @public
     * @param {Array.<string>} values 需要设置的值
     */
    MULTI_CHECKBOX_CLASS.setValue = function (values) {
        values = values || [];
        
        var el = this._eMain;
        var inputs = el.getElementsByTagName('input');
        
        for (var i = 0, input; input = inputs[i]; i++) {
            //  默认先取消复选框，这样在内层循环的for里可以仅对符合的value进行设置，简化重置checked的逻辑
            input.checked = false;
            
            for (var j = 0, len = values.length; j < len; j++) {
                if (input.value == String(values[j])) {
                    input.checked = true;
                    
                    break;
                }
            }
        }
    };
    
    /**
     * 根据数据进行视图渲染
     * 
     * @param {Array.<Object>} data 数据
     * @private
     * data: [
     *      { text: '全部', value: 'xxx' },
     *      { text: '医疗', value: 'xxx' },
     *      { text: '教育', value: 'xxx' },
     *      { text: '游戏', value: 'xxx' },
     *      { text: '金融', value: 'xxx' }
     *  ]
     */
    function render(data) {
        data = data || [];
        
        var el = this._eMain;
        var html = [];
        
        for (var i = 0, len = data.length; i < len; i++) {
            html.push(
                '<label class="">',
                    '<input type="checkbox" value="', data[i].value, '" />',
                    
                    '<span class="">',
                        data[i].text,
                    '</span>',
                '</label>'
            );
        }
        
        el.innerHTML = html.join('');
    }
})();