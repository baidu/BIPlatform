/**
 * di.shared.vui.TextLabel
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    文字区
 * @author:  sushuang(sushuang)
 * @depend:  xui, xutil
 */

$namespace('di.shared.vui');

(function () {
    
    //------------------------------------------
    // 引用 
    //------------------------------------------

    var inheritsObject = xutil.object.inheritsObject;
    var addClass = xutil.dom.addClass;
    var extend = xutil.object.extend;
    var encodeHTML = xutil.string.encodeHTML;
    var isObject = xutil.lang.isObject;
    var isArray = xutil.lang.isArray;
    var template = xutil.string.template;
    var XOBJECT = xui.XObject;

    //------------------------------------------
    // 类型声明 
    //------------------------------------------

    /**
     * 文字区
     * 直接指定文字，或者html，
     * 或者模板（模板形式参见xutil.string.template）
     * 初始dom中的内容被认为是初始模板。
     * 也可以用参数传入模板。
     * 
     * @class
     * @extends xui.XView
     * @param {Object} options
     * @param {HTMLElement} options.el 容器元素
     */
    var TEXT_LABEL = $namespace().TextLabel = 
            inheritsObject(XOBJECT, constructor);
    var TEXT_LABEL_CLASS = TEXT_LABEL.prototype;
    
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
        var el = this._eMain = options.el;
        addClass(el, 'vui-text-area');

        this._sInitTpl = el.innerHTML;
        el.innerHTML = '';

        this.setData(options);
    };
    
    /**
     * 设置数据
     *
     * @public
     * @param {Object} data 数据
     * @param {string} data.html html
     * @param {string} data.text 文本
     * @param {string} data.tpl 模板
     * @param {(Array|Object)} data.args 参数
     */
    TEXT_LABEL_CLASS.setData = function (data) {
        var el = this._eMain;
        data = data || {};

        if (data.html != null) {
            el.innerHTML = data.html;
        }
        else if (data.text != null) {
            el.innerHTML = encodeHTML(data.text);
        }
        else if (data.tpl != null) {
            renderTpl.call(this, data.tpl, data.args);
        }
        else if (this._sInitTpl != null) {
            renderTpl.call(this, this._sInitTpl, data.args);
        }
    };

    /**
     * 按照模板渲染
     * 
     * @private
     */
    function renderTpl(tpl, args) {
        var el = this._eMain;

        if (isObject(args)) {
            el.innerHTML = template(tpl, args);
        }
        else if (isArray(args)) {
            el.innerHTML = template.apply(null, tpl, args);
        }
        else {
            el.innerHTML = template.tpl || '';
        }
    }

})();