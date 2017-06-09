/**
 * di.shared.adapter.EcuiSelectVUIAdapter
 * Copyright 2013 Baidu Inc. All rights reserved.
 *
 * @file:    IstCalendar的适配器
 * @author:  sushuang(sushuang)
 * @depend:  xui, xutil, ecui
 */

$namespace('di.shared.adapter');

(function() {
    
    var DICT = di.config.Dict;
    var isArray = xutil.lang.isArray;

    /**
     * ecui Select的适配器
     *
     * @public
     * @param {Object} def vui的定义
     * @param {Object} options vui实例创建参数
     * @return {Object} vui adapter实例
     */
    $namespace().EcuiSelectVUIAdapter = function (def, options) {
        return {
            setData: setData,
            getValue: getValue
        };
    };

    /**
     * 设置初始化数据
     * 
     * @public
     * @param {Object} data 数据
     * @param {Array.<Object>} data.datasource 数据集
     *      每个节点：
     *          {string} text
     *          {string} value
     * @param {Array.<string>} data.value 初始选中
     */
    function setData (data) {
        data = data || {};
        var datasource = data.datasource || [];
            
        // 清除
        this.setValue(
            getType.call(this) == 'ui-multi-select' ? [] : null
        );
        while(this.remove(0)) {}
        // 多选下拉框：如果后端返回选中值，非正常，补全多选下拉框默认选中值
        if (
            (getType.call(this) === 'ui-multi-select')
            && (!data.value || (data.value && data.value.length === 0))
        ) {
            var value = [];
            for (var i = 0, len = datasource.length; i < len; i ++) {
                value.push(datasource[i].value);
            }
            data.value = value;
        }

        // 当是多选下拉框，含有全选按钮，返回的数据中含有 全部节点 时，过滤掉此节点，然后把‘全选’按钮的text给换成此节点的text（为了适应业务逻辑，不得不加这一段代码）
        // callback维度会返回一个全部节点
        if (getType.call(this) == 'ui-multi-select'
            && this._bSelectAllBtn
            && datasource.length > 1
            && (datasource[0].text.indexOf('全部') > -1)
        ) {
            datasource = datasource.slice(1, datasource.length);
        }
        // 添加
        for (var i = 0, o; o = datasource[i]; i++) {
            var txt = String(o.text != null ? o.text : '');
            this.add(
                txt, 
                null,
                { value: o.value, prompt: txt }
            );
        }

        // 设置默认选中
        var value = data.value;
        value = (
                getType.call(this) == 'ui-multi-select'
                    ? value
                    : (value && value[0])
            )
            || (datasource[0] && datasource[0].value);
        value != null && this.setValue(value);
    }

    /**
     * 获得当前选中数据
     *
     * @public
     * @this {Object} 目标实例
     * @return {Object} 数据
     */
    function getValue() {
        var v;
        var type = getType.call(this);
        if (type == 'ui-select') {
            var sel = this.getSelected();
            v = sel ? sel.getValue() : null;
        }
        else {
            v = this.getValue();
        }
        return v == null 
            ? [] 
            : isArray(v) 
                ? v
                : [v];
    }

    function getType() {
        return this.getTypes().join(' ').indexOf('ui-multi-select') >= 0
            ? 'ui-multi-select'
            : 'ui-select';
    }

})();

