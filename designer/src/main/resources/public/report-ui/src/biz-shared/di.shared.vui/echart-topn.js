/**
 * di.shared.vui.SaveButton
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    文字区
 * @author:  lizhantong(lztlovely@126.com)
 * @depend:  xui, xutil
 */

$namespace('di.shared.vui');
    
(function () {


    //------------------------------------------
    // 引用 
    //------------------------------------------


    var inheritsObject = xutil.object.inheritsObject;
    var addClass = xutil.dom.addClass;
    var removeClass = xutil.dom.removeClass;
    var domChildren = xutil.dom.children;
    var getParent = xutil.dom.getParent;
    var hasClass = xutil.dom.hasClass;
    var confirm = di.helper.Dialog.confirm;
    var alert = di.helper.Dialog.alert;
    var domQ = xutil.dom.q;
    var extend = xutil.object.extend;
    var encodeHTML = xutil.string.encodeHTML;
    var isObject = xutil.lang.isObject;
    var isArray = xutil.lang.isArray;
    var template = xutil.string.template;
    var textLength = xutil.string.textLength;
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
    var ECHART_TOPN = $namespace().EchartTopn =
            inheritsObject(XOBJECT, constructor);
    var ECHART_TOPN_CLASS = ECHART_TOPN.prototype;
    
    
    //------------------------------------------
    // 常量 
    //------------------------------------------


    // 显示错误提示，验证镜像名称时使用
    var SHOW_ERROR_TIPS = true;
    // 隐藏错误提示，验证镜像名称时使用
    var HIDE_ERROR_TIPS = false;
    var ADD_MODE = true;
    var UPDATE_MODE = false;
    // 镜像名称能保存的最大字符长度（一个中文两个英文）
    var TAB_NAME_MAX_LENGTH = 50;
    
    //------------------------------------------
    // 方法
    //------------------------------------------

    
    /**
     * 构造函数
     *
     * @private
     * @param {Object} options 参数
     * @param {Object} options.el 容器元素
     */
    function constructor(options) { 
        var el = this._eMain = options.el;
        var html = [
            '<div class="echart-topn">',
               '<div class="echart-topn-item">',
                    '<span>指标:</span>',
                    // TODO:写成活的
                    '<select name="measureId">',
                        '<option>123</option>',
                        '<option>123</option>',
                    '</select>',
                '</div>',
                // TODO:写成活的
                '<div class="echart-topn-item">',
                    '<span>排序：</span>',
                    '<select name="topType">',
                        '<option>123</option>',
                        '<option>123</option>',
                    '</select>',
                '</div>',
                '<div class="echart-topn-item">',
                    '<span>条数：</span>',
                    '<input type="text" name="recordSize" value=""/>',
                '</div>',
                '<div class="echart-topn-item-btn">',
                    '<input type="button" value="确定">',
                '</div>',
            '</div>'
        ].join('');

        el.innerHTML = html;
        // 获取保存按钮并挂载上
        var elChildrens = domChildren(el);
        // TODO：获取确定按钮，挂载
//        this._btnOK = elChildrens[0];
//        this._btnOperates = elChildrens[1];
//        btnOperates = domChildren(elChildrens[1]);
//        this._btnAdd = btnOperates[0];
//        this._btnUpdate = btnOperates[1];
    }

    /**
     * 初始化，把component中可通信的方法挂在到当前，绑定事件
     *
     * @public
     * @param {Object} options 参数对象
     */
    ECHART_TOPN_CLASS.init = function (options) {
        this._saveImageNameCallBack = options.saveImageName;
        this._getCurrentTabName = options.getCurrentTabName;
        this._maxTabNum = options.maxTabNum;
        this._getTabsNums = options.getTabsNums;
        // 绑定事件
        bindEvent.call(this);
    };

    /**
     * 绑定事件
     *
     * @private
     */
    function bindEvent() {
        var me = this;

        // 绑定保存按钮click与mouseleave事件
        me._btnSave.onclick = function () {
            removeClass(me._btnOperates, SAVE_CLASS.HIDE);
        }
        me._btnSave.onmouseover = function () {
            addClass(this, SAVE_CLASS.SAVE_BUTTON_HOVER_CLASS_NAME);
        }
        me._btnSave.onmouseout = function () {
            removeClass(this, SAVE_CLASS.SAVE_BUTTON_HOVER_CLASS_NAME);
        }
        me._eMain.onmouseleave = function () {
            addClass(me._btnOperates, SAVE_CLASS.HIDE);
        }

        // 绑定新增按钮点击事件
        me._btnAdd.onclick = function (ev) {
            var oEv = ev || window.event;

            // 隐藏按钮选项
            hideOperates(me._btnOperates, oEv);
            
            if (me._getTabsNums() > me._maxTabNum) {
                alert(MESSAGE.TAB_MAX_NUM_WARN);
                return; 
            }
            // 保证this指向
            dialog.call(me, 
                        HIDE_ERROR_TIPS, 
                        '', 
                        dialogCallback, 
                        ADD_MODE);
        }

        // 绑定更新按钮点击事件
        me._btnUpdate.onclick = function (ev) {
            var oEv = ev || window.event;

            // 隐藏按钮选项
            hideOperates(me._btnOperates, oEv);
            
            dialog.call(me, 
                        HIDE_ERROR_TIPS,
                        me._getCurrentTabName(), 
                        dialogCallback, 
                        UPDATE_MODE);
        }
    };

    /**
     * 设置父亲包含块的z-Index
     * 
     * @private
     * @param {HTMLElement} el vui-save的容器
     */
    function resetContainParentZIndex(el) {
    	 var parentClassName = 'di-o_o-block';
         var parent = el.parentNode;

         while (parent) {
             parent.style.zIndex = 100;
             if (hasClass(parent, parentClassName)) {
                 break;
             }
             parent = getParent(parent);
         }
    }

    /**
     * 隐藏按钮操作项
     * 
     * @private
     * @param {HTMLElement} el 按钮操作项
     * @param {Event} ev 事件
     */
    function hideOperates(el, ev) {
        // 隐藏按钮选项
        addClass(el, SAVE_CLASS.HIDE);

        // 阻止事件冒泡
        ev.stopPropagation 
        ? (ev.stopPropagation()) 
        : (ev.cancelBubble = true);
    }

})();