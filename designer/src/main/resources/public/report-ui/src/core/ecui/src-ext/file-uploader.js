/**
 * ecui.ui.FileUploader
 * Copyright 2012 Baidu Inc. All rights reserved.
 * 
 * @file:    文件上传的简单实现
 * @author:  sushuang(sushuang)
 * @depend:  xutil.ajax, config.lang
 */

(function () {

    var core = ecui;
    var array = core.array;
    var dom = core.dom;
    var ui = core.ui;
    var util = core.util;
    var string = core.string;

    var $fastCreate = core.$fastCreate;
    var setFocused = core.setFocused;
    var extend = util.extend;
    var createDom = dom.create;
    var children = dom.children;
    var moveElements = dom.moveElements;
    var getPosition  = dom.getPosition;
    var setStyle = dom.setStyle;
    var inheritsControl = core.inherits;
    var isContentBox = core.isContentBox;
    var getStatus = core.getStatus;
    var getView = util.getView;
    var attachEvent = util.attachEvent;
    var triggerEvent = core.triggerEvent;
    var trim = string.trim;

    var MATH = Math;
    var UI_CONTROL = ui.Control;
    var UI_CONTROL_CLASS = UI_CONTROL.prototype;
    var UI_BUTTON = ui.Button;

    /**
     * 文件上传主类
     *
     * @class
     * @extends ecui.ui.Control
     */
    var UI_FILE_UPLOADER = ui.FileUploader = 
        inheritsControl(
            UI_CONTROL,
            'ui-file-uploader',
            null,
            function (el, options) {
                createView.call(this, el, options);
                bindEvent.call(this, el, options);
                this.$reset();
            }
        );

    var UI_FILE_UPLOADER_CLASS = UI_FILE_UPLOADER.prototype;

    UI_FILE_UPLOADER_CLASS.$resize = new Function();

    var HIDDEN_STYLE = 'display:block;width:0;height:0;visibility:hidden;margin:0;padding:0;border:0 solid #FFF;';
    var MASK_STYLE = 'display:block;font-family:Arial;font-size:118px;position:absolute;top:0;right:0;width:auto;margin:0;padding:0;opacity:0;filter:alpha(opacity=0);';
    var MASK_OUTER_STYLE = 'margin:0;padding:0;top:0;left:0;overflow:hidden;position:absolute';

    /**
     * 创建HTML
     * 
     * @private
     * @this {ecui.ui.FileUploaderClass} 实例本身
     * @param {HTMLElement} el 控件容器
     * @param {Objectt} options 初始化参数，更多参数同setOptions方法
     */
    function createView(el, options) {
        var o;
        var type = this.getType();

        var ifrName = String(
                type 
                + '-' 
                + MATH.round(MATH.random() * 10000000000000) 
                + '-target-iframe'
            );
        var url = options.url;

        // form
        el.innerHTML = [
            '<form method="post" target="' + ifrName + '" enctype="multipart/form-data">',
                '<input type="text" class="' + type + '-file-path" />', 
                '<div class="ui-button-g ' + UI_BUTTON.TYPES + '">&nbsp;.</div>',
                '<div class="ui-button-g ' + UI_BUTTON.TYPES + '">&nbsp;.</div>',
                '<div class="' + type + '-uploading"></div>',
                '<div style="' + HIDDEN_STYLE + '"></div>',
                '<div style="' + MASK_OUTER_STYLE + '"></div>',
            '</form>'
        ].join('');
        
        this._eForm = el.firstChild;

        o = this._eForm.getElementsByTagName('input');
        this._eFilePath = o[0];

        o = this._eForm.getElementsByTagName('div');
        this._uBrowseBtn = $fastCreate(UI_BUTTON, o[0], this);
        this._uUploadBtn = $fastCreate(UI_BUTTON, o[1], this);
        this._eLoading = o[2];
        this._eParamOuter = o[3];
        this._eFileInputOuter = o[4];

        // 初始化设置参数
        this.setOptions(options);

        // target iframe
        o = createDom(null, 'display:none');
        el.appendChild(o);
        o.innerHTML = '<iframe name="' + ifrName + '"></iframe>'
        this._eIframe = o.firstChild;

        setStyle(el, 'position', 'relative');
        setStyle(el, 'overflow', 'hidden');
        var browseBtnX = this._uBrowseBtn.getX();
        this._nMaskWidth = browseBtnX + this._uBrowseBtn.getWidth();
        this._nMaskHeight = this._eFilePath.offsetHeight;
        setStyle(this._eFileInputOuter, 'width', this._nMaskWidth + 'px');
        setStyle(this._eFileInputOuter, 'height', this._nMaskHeight + 'px');
    }

    /**
     * 重新创建 <input type="file" ... />
     * 
     * @private
     * @this {ecui.ui.FileUploaderClass} 实例本身
     * @param {HTMLElement} el 控件容器
     * @param {Objectt} options 初始化参数
     */
    function resetFileInput() {
        this._eFileInputOuter.innerHTML = '<input type="file" style="' + MASK_STYLE + '" />';
        this._eFileInput = this._eFileInputOuter.firstChild;
        this._eFileInput.setAttribute('name', this._oOptions.fileParamName);
        this._eFileInput.style.height = this._nMaskHeight + 'px';

        // 绑定事件
        attachEvent(
            this._eFileInput, 
            'change', 
            bind(this.$handleFileInputChange, this)
        );
    }

    /**
     * 绑定事件
     * 
     * @private
     * @this {ecui.ui.FileUploaderClass} 实例本身
     * @param {HTMLElement} el 控件容器
     * @param {Objectt} options 初始化参数
     */
    function bindEvent() {
        this._uUploadBtn.onclick = bind(this.$submit, this);
        
        attachEvent(
            this._eIframe,
            'load',
            bind(this.$handleUploaded, this)
        );
    }

    /**
     * 析构
     *
     * @override
     * @protected
     */
    UI_FILE_UPLOADER_CLASS.$dispose = function() {
        this._eIframe = null;
        this._eForm = null;
        this._eFilePath = null;
        this._eFileInput = null;
        this._eParamOuter = null;
        this._eLoading = null;
        this._eFileInputOuter = null;

        UI_FILE_UPLOADER.superClass.$dispose.call(this);
    };

    /**
     * 设置属性
     *
     * @public
     * @param {Object} options 属性，
     * @param {string} options.url 上传目标url
     * @param {string=} options.fileParamName 
     *                  文件的请求参数名，默认为'file'
     * @param {string} options.browseBtnText 浏览按钮的文字，
     *                  缺省为“浏览”
     * @param {string} options.uploadBtnText 上传按钮的文字
     *                  缺省为“上传”
     * @param {string} options.prompt 文件路径显示框的默认文字
     *                  缺省为“请选择上传文件...”
     * @param {function():string} options.paramGetter 
     *                  得到额外的请求参数，
     *                  返回值为请求参数字符串，
     *                  如：aaa=123&bbb=456&ccc=789
     */
    UI_FILE_UPLOADER_CLASS.setOptions = function(options) {
        if (!this._oOptions) {
            // 默认值
            this._oOptions = {
                fileParamName: 'file',
                paramGetter: new Function(),
                browseBtnText: '浏览',
                uploadBtnText: '上传',
                prompt: '请选择上传文件...'
            };
        }
        
        // 保存参数
        extend(this._oOptions, options);

        // 更新视图
        this._eForm.setAttribute('action', this._oOptions.url);
        this._eFilePath.setAttribute('value', this._oOptions.prompt);
        this._uBrowseBtn.setText(this._oOptions.browseBtnText);
        this._uUploadBtn.setText(this._oOptions.uploadBtnText);
    };

    /**
     * 设置视图状态
     *
     * @protected
     * @param {string} status 可取值为：'INIT', 'READY', 'UPLOADING'
     *                      如果缺省，则用当前this._sStatus设值
     */
    UI_FILE_UPLOADER_CLASS.$setViewStatus = function(status) {
        if (status != null) {
            if (this._sStatus == status) { 
               return;
            }
            this._sStatus = status;
        }

        switch (this._sStatus) {
            case 'INIT':
                this._uUploadBtn.disable();
                this._eFilePath.disabled = false;
                this._uBrowseBtn.enable();
                setStyle(this._eLoading, 'display', 'none');
                break;
            case 'READY':
                this._uUploadBtn.enable();
                this._eFilePath.disabled = false;
                this._uBrowseBtn.enable();
                setStyle(this._eLoading, 'display', 'none');
                break;
            case 'UPLOADING':
                this._uUploadBtn.disable();
                this._eFilePath.disabled = true;
                this._uBrowseBtn.disable();
                setStyle(this._eLoading, 'display', 'inline-block');
                break;
        }
    };

    /**
     * 解禁
     *
     * @public
     */
    UI_FILE_UPLOADER_CLASS.enable = function() {
        if (this._bDisabled) {
            this.$setViewStatus();
        }
        UI_FILE_UPLOADER.superClass.enable.call(this);
    };

    /**
     * 禁用
     *
     * @public
     */
    UI_FILE_UPLOADER_CLASS.disable = function(enable) {
        if (!this._bDisabled) {
            this._uBrowseBtn.disable();
            this._uUploadBtn.disable()
            this._eFilePath.disabled = true;
        }
        UI_FILE_UPLOADER.superClass.disable.call(this);
    };

    /**
     * file input 改变事件
     *
     * @event
     * @protected
     */
    UI_FILE_UPLOADER_CLASS.$handleFileInputChange = function() {
        this._sFilePath = this._eFileInput.value;
        this._eFilePath.value = this._sFilePath;

        this.$setViewStatus('READY');
    };

    /**
     * file input 改变事件
     *
     * @event
     * @protected
     */
    UI_FILE_UPLOADER_CLASS.$handleUploaded = function() {
        if (!this._bReady) {
            // 防止IE、ff下第一次iframe加载时触发onload
            return;
        }

        this.$reset();
        
        var responseHTML = this._eIframe.contentWindow.document.body.innerHTML;
        /**
         * 上传返回事件
         * 
         * @event
         * @param {string} responseHTML 返回的HTML
         */
        triggerEvent(this, 'uploaded', null, [responseHTML]);
    };

    /**
     * 清空
     *
     * @event
     * @protected
     */
    UI_FILE_UPLOADER_CLASS.$reset = function() {
        // IE和webkit，如果选择的文件没有变话，就不触发onchange事件
        // 并且IE不允许修改file input的value字段。
        // 从而在上传结束后，如果不清空file input，
        // 则选择同样文件，不能响应。
        // 所以用这种方式清空fileInput
        resetFileInput.call(this);        
        this._eFilePath.value = this._oOptions.prompt;
        this.$setViewStatus('INIT');
    };    

    /**
     * 提交
     *
     * @event
     * @protected
     */
    UI_FILE_UPLOADER_CLASS.$submit = function() {
        this._bReady = true;
        
        this.$prepareParam();
        this.$setViewStatus('UPLOADING');
        this._eForm.submit();
    };

    /**
     * 准备额外的提交参数
     *
     * @protected
     */    
    UI_FILE_UPLOADER_CLASS.$prepareParam = function() {
        var paramArr = (this._oOptions.paramGetter() || '').split('&');
        var htmlArr = [];
        for (var i = 0, p; i < paramArr.length; i ++) {
            p = paramArr[i].split('=');
            if (p[0] != null 
                && p[0] != ''
                && p[1] != null 
                && p[1] != ''
            ) {
                htmlArr.push(
                    '<input type="hidden" name="' + p[0] + '" value="' + p[1] + '" />'
                );
            }
        }
        this._eParamOuter.innerHTML = htmlArr.join('');
    };

    /**
     * 给函数绑定作用域
     * 
     * @private
     * @param {Function} fn 要绑定的函数
     * @param {Object} scope 作用域
     * @return {Function} 绑定作用域得到的函数
     */
    function bind(fn, scope) {
        var args = Array.prototype.slice.call(arguments);
        return function() {
            fn.apply(scope, args);
        }
    }

})();
