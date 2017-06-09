/**
 * di.shared.vui.SimpleRadio
 * Copyright 2014 Baidu Inc. All rights reserved.
 *
 * @file:    平铺的单选框组件（暂时使用原生单选框，没有使用图片美化）
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
     * 平铺的单选框组件
     * 
     * @class
     * @extends xui.XView
     * @param {Object} options
     * @param {HTMLElement} options.el 容器元素
     */
    var SIMPLE_RADIO = $namespace().SimpleRadio = 
            inheritsObject(XOBJECT, constructor);
    var SIMPLE_RADIO_CLASS = SIMPLE_RADIO.prototype;
    
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
     * @public
     * @param {Object} data 数据对象
     * @param {Array<Object>} data.datasource 数据源
     */
    SIMPLE_RADIO_CLASS.setData = function (data) {
        render.call(this, data.datasource || []);
    };

    /**
     * 得到当前值
     *
     * @public
     * @return {Array.<string>} 当前数据
     */
    SIMPLE_RADIO_CLASS.getValue = function () {
        var el = this._eMain;
        var inputs = el.getElementsByTagName('input');
        var checkedValue = '';
        
        for (var i = 0, input; input = inputs[i]; i++) {
            if (input.checked) {
                checkedValue = input.value;
                
                break;
            }
        }
        
        return checkedValue;
    };
    
    /**
     * 设置单选框组件的选中值
     *
     * @public
     * @param {Array.<string>} value 需要设置的值
     */
    SIMPLE_RADIO_CLASS.setValue = function (value) {
        value = value || '';
        
        var el = this._eMain;
        var inputs = el.getElementsByTagName('input');
        
        for (var i = 0, input; input = inputs[i]; i++) {
            
            if (input.value == String(value)) {
                input.checked = true;
            }
            else {
                input.checked = false;
            }
        }
    };
    
    /**
     * 根据数据进行视图渲染
     * 
     * @private
     * @param {Array.<Object>} data 数据
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
        //  单选框内部name，用于保持单选效果（没有name的话 单选框就不会互斥）
        var simpleRadioName = '_simpleRadioName';
        
        for (var i = 0, len = data.length; i < len; i++) {
            html.push(
                '<label class="">',
                    '<input class="" type="radio" ',
                        'name="', simpleRadioName, '" ',
                        'value="', data[i].value, '" />',
                        
                    '<span class="">',
                        data[i].text,
                    '</span>',
                '</label>'
            );
        }
        
        el.innerHTML = html.join('');
    }
})();