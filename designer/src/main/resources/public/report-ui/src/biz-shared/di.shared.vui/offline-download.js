/**
 * di.shared.vui.OfflineDownload
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    离线下载按钮和对话框
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
    var ecuiCreate = di.helper.Util.ecuiCreate;
    var isObject = xutil.lang.isObject;
    var isArray = xutil.lang.isArray;
    var template = xutil.string.template;
    var domChildren = xutil.dom.children;
    var domRemove = xutil.dom.remove;
    var getByPath = xutil.object.getByPath;
    var DICT = di.config.Dict;
    var XOBJECT = xui.XObject;
    var UI_BUTTON;
    var UI_FORM;

    $link(function () {
        UI_BUTTON = getByPath('ecui.ui.HButton');
        UI_FORM = getByPath('ecui.ui.Form');
    });
    
    //------------------------------------------
    // 类型声明 
    //------------------------------------------

    /**
     * 离线下载按钮和对话框
     * 
     * @class
     * @extends xui.XView
     * @param {Object} options
     * @param {string} options.skin 皮肤（的css类）
     * @param {string} options.text 按钮上的文字，默认为'离线下载'
     * @param {string} options.confirmText 确定按钮上的文字，默认为'确定'
     * @param {string} options.cancelText 取消按钮上的文字，默认为'取消'
     * @param {string} options.headText 提示文字，默认为'请输入邮箱'
     * @param {string} options.inputInfo 输入信息
     */
    var OFFLINE_DOWNLOAD = $namespace().OfflineDownload = 
            inheritsObject(XOBJECT, constructor);
    var OFFLINE_DOWNLOAD_CLASS = OFFLINE_DOWNLOAD.prototype;
    
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
        addClass(el, 'offline-download');

        var eel;
        var html;

        eel = document.createElement('div');
        el.appendChild(eel);
        this._uBtn = ecuiCreate(
            UI_BUTTON, 
            eel, 
            null,
            {
                text: options.text || '离线下载',
                skin: options.skin
            }
        );

        // 输入离线下载信息（如邮箱）的对话框
        if (!this._uDialog) {
            eel = document.createElement('div');
            html = [
                '<label>离线下载</label>',
                '<span class="offline-download-head">' + (options.headText || '请输入邮箱') + '</span>',
                '<input type="input" class="offline-download-input"/>',
                '<div></div>',
                '<div></div>'
            ];
            eel.innerHTML = html.join('');
            html = domChildren(eel);
            this._eInput = html[2];
            this._uDialog = ecuiCreate(UI_FORM, eel, null, { hide: true });
            this._uConfirmBtn = ecuiCreate(
                UI_BUTTON, 
                html[3],
                null,
                {
                    text: options.confirmText || '确定',
                    skin: options.skin
                }
            );
            this._uCancelBtn = ecuiCreate(
                UI_BUTTON, 
                html[4],
                null,
                {
                    text: options.cancelText || '取消',
                    skin: options.skin
                }
            );

            document.body.appendChild(eel);
        }
    };

    OFFLINE_DOWNLOAD_CLASS.init = function () {
        var me = this;

        this._uBtn.onclick = function () {
            me.$clear();
            me._uDialog.center();
            me._uDialog.showModal(DICT.DEFAULT_MASK_OPACITY);
        };

        this._uConfirmBtn.onclick = function () {
            me.notify('confirm', [me._eInput.value]);
            me._uDialog.hide();
        };

        this._uCancelBtn.onclick = function () {
            me._uDialog.hide();
        }

        this._uDialog.init();
        this._uBtn.init();
        this._uConfirmBtn.init();
        this._uCancelBtn.init();
    };   

    OFFLINE_DOWNLOAD_CLASS.$clear = function () {
        this._eInput.value = '';
    };

    OFFLINE_DOWNLOAD_CLASS.getValue = function () {
        return { email: this._eInput.value };
    };

    OFFLINE_DOWNLOAD_CLASS.dispose = function () {
        if (this._uDialog) {
            var el = this._uDialog.getOuter();
            this._uDialog.dispose();
            this._uBtn.dispose();
            this._uConfirmBtn.dispose();
            this._uCancelBtn.dispose();
            domRemove(el);
        }
    };       
    
})();