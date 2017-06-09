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

    /* globals xutil */
    var inheritsObject = xutil.object.inheritsObject;
    var addClass = xutil.dom.addClass;
    var removeClass = xutil.dom.removeClass;
    var domChildren = xutil.dom.children;
    var getParent = xutil.dom.getParent;
    var hasClass = xutil.dom.hasClass;
    /* globals di */
    var confirm = di.helper.Dialog.confirm;
    var alert = di.helper.Dialog.alert;
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
    var SAVE_BUTTON = $namespace().SaveButton =
            inheritsObject(XOBJECT, constructor);
    var SAVE_BUTTON_CLASS = SAVE_BUTTON.prototype;

    //------------------------------------------
    // 常量 
    //------------------------------------------

    // 显示错误提示，验证镜像名称时使用
    var SHOW_ERROR_TIPS = true;
    var ADD_MODE = true;
    var UPDATE_MODE = false;
    // 镜像名称能保存的最大字符长度（一个中文两个英文）
    var TAB_NAME_MAX_LENGTH = 50;
    
    // 样式
    var SAVE_CLASS = {
        SAVE_CLASS_NAME: 'ui-reportSave-save',
        // 保存按钮一般样式
        SAVE_BUTTON_CLASS_NAME: 'ui-reportSave-save-saveButton',
        // 保存按钮hover样式
        SAVE_BUTTON_HOVER_CLASS_NAME: 'ui-reportSave-save-saveButton-hover',
        // 按钮操作项的样式(ul)
        OPERATE_BUTTONS_CLASS_NAME: 'ui-reportSave-save-operateButtons',
        // 隐藏样式
        HIDE: 'di-o_o-hide',
        // 弹出框中的一般样式
        DIALOG_ITEM_CLASS_NAME: 'ui-reportSave-save-dialog-form-item',
        // 弹出框中的错误提示样式
        DIALOG_ERROR_CLASS_NAME: 'ui-reportSave-save-dialog-form-error'
    };
    
    // 提示信息
    var MESSAGE = {
        HIDE: '',
        // 镜像名称验证失败提示
        NAME_WARN: '请您输入正确格式的名称',
        // 镜像名称验证失败提示
        SAME_NAME_WARN: '不能输入重复的名称',
        // 镜像名称placehoder
        NAME_PLACE_HOLDER: '请您输入正确格式的名称',
        // 如果是默认项不能被编辑提示
        TAB_UPDATE_DEFAULT_WARN: '默认项不能编辑',
        // 镜像名称超过最大个数提示
        TAB_MAX_NUM_WARN: '保存报表个数已达上限，不能继续添加'
    };
    
    
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
        var elChildrens;
        var btnOperates;
        var el = this._eMain = options.el;
        var html = [
            '<div class="', SAVE_CLASS.SAVE_BUTTON_CLASS_NAME, '">保存报表</div>',
            '<ul class="', 
                SAVE_CLASS.OPERATE_BUTTONS_CLASS_NAME, ' ', 
                SAVE_CLASS.HIDE, '">',
                '<li>新增个人报表 </li>',
                '<li>更新当前报表</li>',
            '</ul>'
        ].join('');

        addClass(el, SAVE_CLASS.SAVE_CLASS_NAME);
        el.innerHTML = html;

        // 获取保存按钮并挂载上
        elChildrens = domChildren(el);
        this._btnSave = elChildrens[0];
        this._btnOperates = elChildrens[1];
        
        btnOperates = domChildren(elChildrens[1]);
        this._btnAdd = btnOperates[0];
        this._btnUpdate = btnOperates[1];
    }

    /**
     * 初始化，把component中可通信的方法挂在到当前，绑定事件
     *
     * @public
     * @param {Object} options 参数对象
     * @param {Function} options.saveImageName 描述如下：
     * 保存镜像校验通过，就执行component中的saveImageName方法
     * @param {Function} options.getCurrentTabName 描述如下：
     * 通过执行component中的getCurrentTabName时时的获取当前选中tab的名称
     * @param {number} options.maxTabNum 最大可增添tab个数
     * 在component描述文件中设置，在component中挂载到vui-save下
     * @param {Function} options.getTabsNums 描述如下：
     * 通过执行component中的getTabsNums时时的获取当前tab的总个数
     */
    SAVE_BUTTON_CLASS.init = function (options) {
        this._saveImageNameCallBack = options.saveImageName;
        this._getCurrentTabName = options.getCurrentTabName;
        this._maxTabNum = options.maxTabNum;
        this._getTabsNums = options.getTabsNums;
        this._getAllTabName = options.getAllTabName;

        // 绑定事件
        bindEvent.call(this);
    };
    
    /**
     * 解禁操作
     *
     * @protected
     */
    SAVE_BUTTON_CLASS.disable = function () {};
    
    /**
     * 启用操作
     *
     * @protected
     */
    SAVE_BUTTON_CLASS.enable = function () {};

    /**
     * 绑定事件
     *
     * @private
     */
    function bindEvent() {
        var me = this;

        // 绑定保存按钮click与mouseleave事件
        me._btnSave.onclick = function () {
            if (me._isInDesigner) {
                return;
            }
            removeClass(me._btnOperates, SAVE_CLASS.HIDE);
        };
        me._btnSave.onmouseover = function () {
            addClass(this, SAVE_CLASS.SAVE_BUTTON_HOVER_CLASS_NAME);
        };
        me._btnSave.onmouseout = function () {
            removeClass(this, SAVE_CLASS.SAVE_BUTTON_HOVER_CLASS_NAME);
        };
        me._eMain.onmouseleave = function () {
            addClass(me._btnOperates, SAVE_CLASS.HIDE);
        };

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
            dialog.call(
                me,
                MESSAGE.HIDE,
                '',
                dialogCallback,
                ADD_MODE
            );
        };

        // 绑定更新按钮点击事件
        me._btnUpdate.onclick = function (ev) {
            var oEv = ev || window.event;
            // 隐藏按钮选项
            hideOperates(me._btnOperates, oEv);
            dialog.call(
                me,
                MESSAGE.HIDE,
                me._getCurrentTabName(),
                dialogCallback,
                UPDATE_MODE
            );
        };
    };

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

    /**
     * 弹出框事件
     * 
     * @private
     * @param {string} errorTips 错误提示
     * @param {string} value 用户输入的名称
     * @param {Function} callback 弹出框点击确定后的回调事件
     * @param {boolean} isAdd 新增或者更新
     */
    function dialog(errorTips, value, callback, isAdd) {
        var me = this;
        // 默认项不能编辑，这块的实现不是很好
        if (value === '默认') {
            alert(MESSAGE.TAB_UPDATE_DEFAULT_WARN);
            return;
        }
        var html = [
            '<div class="', SAVE_CLASS.DIALOG_ERROR_CLASS_NAME, '">',
            errorTips,
            '</div>',
            '<div class="', SAVE_CLASS.DIALOG_ITEM_CLASS_NAME, '">',
            '<label>', '名称', '</label>',
            '<input type="text" id="reportSaveName" ', 'value="', value, '"',
//                    isAdd ? '' : 'disabled="disabled"',
            ' placeholder="', MESSAGE.NAME_PLACE_HOLDER, '" />',
            '</div>'
        ].join('');
        
        confirm(
            html,
            function () {
                var name = document.getElementById('reportSaveName').value;
                // 传递this指向
                callback.call(me, isAdd, name, value);
            }
        );
    }

    /**
     * 弹出框中点击确定后的回调事件
     * 
     * 如果校验成功，就执行component中的saveImageName事件
     * $handleGetAllImagesSuccess中初始化vui-save时传入的saveImageName方法
     * 在saveImageName中区分新增和更新，分别去请求后端操作
     * 
     * 如果校验失败，继续执行dialog进行弹框（把当前值以及错误提示带进去）
     * 
     * @private
     * @param {string} isAdd true表示新增，反之为更新
     * @param {string} name 用户输入的名称
     */
    function dialogCallback(isAdd, name, oldName) {
        if (!validate(name.trim())) {
            dialog.call(
                this,
                MESSAGE.NAME_WARN,
                name,
                dialogCallback,
                isAdd
            );
        }
        else if (hasSameTabName.call(this, isAdd, name, oldName)) {
            dialog.call(
                this,
                MESSAGE.SAME_NAME_WARN,
                name,
                dialogCallback,
                isAdd
            );
        }
        else {
            this._saveImageNameCallBack(isAdd, name);
        } 
    }

    /**
     * 是否含有相同名字的tab
     *
     *
     * @private
     * @param {string} name 用户输入的名称
     * @return {string} 是否已经有相同名称
     */
    function hasSameTabName(isAdd, name, oldName) {
        var result = false;
        var tabNames = this._getAllTabName();
        // 如果是编辑
        if (!isAdd) {
            if (name === oldName) {
                var index = tabNames.indexOf(oldName);
                tabNames = tabNames.slice(0, index).concat(tabNames.slice(index + 1, tabNames.length));
            }
        }
        if (tabNames.indexOf(name) >= 0) {
            result = true;
        }
        return result;
    }

    /**
     * 验证名称
     * 
     * @private
     * @param {string} name 名称
     */
    function validate(name) {
        var l = textLength(name);
        if (name === '' || l > TAB_NAME_MAX_LENGTH) {
            return false;
        } 
        return true;
    }

})();