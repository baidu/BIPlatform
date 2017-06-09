/**
 * Created by weiboxue on 2/4/2015.
 */
/**
 * di.shared.vui.HiddenInput
 * Copyright 2014 Baidu Inc. All rights reserved.
 *
 * @file:    隐藏的输入，用于传递报表引擎外部传来的参数
 * @author:  luowenlei(luowenlei)
 * @depend:  xui, xutil
 */


$namespace('di.shared.vui');

(function () {

    //------------------------------------------
    // 引用
    //------------------------------------------

    var inheritsObject = xutil.object.inheritsObject;
    var extend = xutil.object.extend;
    var XOBJECT = xui.XObject;

    //------------------------------------------
    // 类型声明
    //------------------------------------------

    /**
     * 用于传递报表引擎外部传来的参数
     *
     * @class
     * @extends xui.XView
     * @param {Object} options
     * @param {HTMLElement} options.el 容器元素
     */
    var CASCADE_SELECT = $namespace().CascadeSelect =
        inheritsObject(XOBJECT, constructor);
    var CASCADE_SELECT_CLASS = CASCADE_SELECT.prototype;

    //------------------------------------------
    // 方法
    //------------------------------------------

    /**
     * 构造函数
     *
     * @private
     * @param {Object} options 参数
     */
    function constructor(options) {
        this._el = options.el;
    }

    /**
     * 设置数据
     *
     * @public
     * @param {Object} data 数据
     * @param {(Object|Array}} data.datasource 数据集
     * @param {*} data.value 当前数据
     */
    CASCADE_SELECT_CLASS.setData = function (data) {
        var me = this;
        var def = me.$di('getDef');
        var compId = def.compId;
        var selElId = compId + '-0';

        // 如果是第一次进来，没有点击任何下拉框，那么当级联下拉框初始化完毕，会去updateContext;
        // 如果已经点击过下拉框，那么接下来展示区域的加载就根据设定的事件出发类型判断了，也就是di-form中的$handleChange方法
        me._isFirstEnter = true;

        me._compId = compId;
        me._curIndex = 0;
        me._allSel = def.selectAllDim.length;

        me.$setSelectedValue(data);
        // 渲染第一个select
        me.$buildSelectHtml(selElId, data.datasource);

        // 初始化第一个下拉框
        me.$renderSingleSelect(selElId, 0);

        // 当初始化完第一个下拉框，如果是多级，就去触发第二个
        if(me._curIndex < me._allSel - 1) {
            me.getNextLevel(me._selValue);
        }
        else {
            me.$cascadeSelectChange();
        }
    };

    /**
     * 设置数据
     *
     * @public
     * @param {string} parent 父节点
     */
    CASCADE_SELECT_CLASS.getNextLevel = function(parent) {
        var me = this;
        var option = {
            param: {
                componentId: me._compId,
                uniqueName: parent
            },
            callback: me.renderNextLevel,
            input: me
        };
        me.notify('cascadeGetNextLevel', option);
    };

    /**
     * 渲染下一个下拉框
     *
     * @public
     * @param {Object} data 数据
     * @param {(Object|Array}} data.compId.datasource 数据集
     * @param {*} data.value 当前数据
     */
    CASCADE_SELECT_CLASS.renderNextLevel = function(data) {
        var me = this;
        var compId = me._compId;
        var datasource;
        var dif = me._allSel - me._curIndex - 1; // 用来确定是否当前下拉框还有子下拉框

        if (data && data[compId]) {
            data = data[compId];
            datasource = data.datasource;
        }

        // 先移除掉触发者以后的所有下拉框
        for (var i = dif - 1; i >= 0; i --) {
            var _cur = me._curIndex + (dif - i);
            var selId = compId + '-' + _cur;
            $('.dk-select').each(function() {
                if($(this).attr('id').indexOf(selId) > 0) {
                    $(this).remove();
                }
            });
        }

        if (datasource.length > 0 && dif !== 0) {
            // 渲染触发者的下一个下拉框
            var selElId = compId + '-' + (++ me._curIndex);
            me.$setSelectedValue(data);
            me.$buildSelectHtml(selElId, datasource);

            // 初始化下拉框,使用闭包主要是为了保存当前下拉框的顺序
            (function (x) {
                me.$renderSingleSelect(selElId, x);
            })(me._curIndex);

            if (me._curIndex < me._allSel - 1) {
                me.getNextLevel(me._selValue);
            }
            else {
                me.$cascadeSelectChange();
            }
        }
        else {
            me.$cascadeSelectChange();
        }
    };

    /**
     * 得到当前值
     *
     * @public
     * @return {string} 选中值
     */
    CASCADE_SELECT_CLASS.getValue = function () {
        var me = this;
        return me._selValue;
    };

    /**
     * 多级下拉框整体选中值改变
     *
     * @private
     */
    CASCADE_SELECT_CLASS.$cascadeSelectChange = function () {
        if (this._isFirstEnter) {
            this.notify('cascadeSelectUpdateContext');
        }
        else {
            this.notify('cascadeSelectChange');
        }
    };

    /**
     * 设置选中值
     * @param {Object} data 某一个下拉框的数据
     *
     * @private
     */
    CASCADE_SELECT_CLASS.$setSelectedValue = function (data) {
        var me = this;
        if (data.value && data.value.length > 0) {
            var selArr = data.value[0].split('.');
            var resArr = [];
            for (var i = 0; i <= (me._curIndex + 1); i ++) {
                resArr.push(selArr[i]);
            }
            me._selValue  = resArr.join('.');
        }
        else {
            me._selValue  = data.datasource[0].value;
        }
    };

    /**
     * 生成下拉框html
     * @param {string} selElId 下拉框id
     * @param {Object} data 某一个下拉框的数据
     *
     * @private
     */
    CASCADE_SELECT_CLASS.$buildSelectHtml = function (selElId, data) {
        var html = ['<select id="', selElId, '">'];
        for (var i = 0, len = data.length; i < len; i ++) {
            html.push(
                '<option value="', data[i].value, '"',
                data[i].value === this._selValue ? 'selected="selected"' : '',
                '>', data[i].text,
                '</option>'
            );
        }
        html.push('</select>');
        $(this._el).append(html.join(''));
    };

    /**
     * 渲染下拉框
     * @param {string} selElId 下拉框id
     * @param {number} n 下拉框的顺序
     *
     * @private
     */
    CASCADE_SELECT_CLASS.$renderSingleSelect = function (selElId, n) {
        var me = this;
        $('#' + selElId).dropkick({
            mobile: true,
            change: function () {
                me._curIndex = n;
                me._isFirstEnter = false;
                me._selValue = this.value;

                if (me._curIndex < me._allSel - 1) {
                    me.getNextLevel(this.value);
                }
                else {
                    me.$cascadeSelectChange();
                }
            }
        });
        $('select[id=' + selElId + ']').remove();
    };
})();


