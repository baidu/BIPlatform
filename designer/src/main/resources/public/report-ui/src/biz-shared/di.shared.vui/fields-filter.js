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
    var bind = xutil.fn.bind;
    var extend = xutil.object.extend;
    var encodeHTML = xutil.string.encodeHTML;
    var q = xutil.dom.q;
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
    var UI_IND_TREE = ecui.ui.IndTree;

    $link(function () {
        UI_BUTTON = getByPath('ecui.ui.HButton');
        UI_FORM = getByPath('ecui.ui.Form');
    });

    //------------------------------------------
    // 类型声明 
    //------------------------------------------

    var TPL_MAIN = [
        '<label>字段调整</label>',
        '<ul class="table-fields-filter-list">',
            //'<li><input type="checkbox" name="" id="input1"><label>字段1</label></li>',
        '</ul>',
        '<div style="float:right">',
        '<div></div>',
        '<div></div>',
        '</div>'
    ].join('');

    /**
     * 表格字段过滤和对话框
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

    var FIELDS_FILTER = $namespace().FieldsFilter =
        inheritsObject(XOBJECT, constructor);
    var FIELDS_FILTER_CLASS = FIELDS_FILTER.prototype;


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
        var mainEl = this._eMain = options.el;
        addClass(mainEl, 'fields-filter');

        // 创建字段调整容器及按钮
        var fieldsFilterBtnEl = document.createElement('div');
        mainEl.appendChild(fieldsFilterBtnEl);
        this._uBtn = ecuiCreate(
            UI_BUTTON,
            fieldsFilterBtnEl,
            null,
            {
                text: options.text || '字段调整',
                skin: options.skin
            }
        );

        var dialogEl = document.createElement('div'),
            confirmEl,
            cancelEl,
            filterEl,
            dialogElChildren;
        // 字段调整显示框
        if (!this._uDialog) {
            dialogEl.innerHTML = TPL_MAIN;
            document.body.appendChild(dialogEl);

            dialogElChildren = domChildren(dialogEl);
            filterEl = dialogElChildren[1];
            confirmEl = domChildren(dialogElChildren[2])[0];
            cancelEl = domChildren(dialogElChildren[2])[1];

            this._uDialog = ecuiCreate(UI_FORM, dialogEl, null, { hide: true });

            this._uConfirmBtn = ecuiCreate(
                UI_BUTTON,
                confirmEl,
                null,
                {
                    text: options.confirmText || '确定',
                    skin: 'ui-button-g'
                }
            );

            this._uCancelBtn = ecuiCreate(
                UI_BUTTON,
                cancelEl,
                null,
                {
                    text: options.cancelText || '取消',
                    skin: 'ui-button-c'
                }
            );

        }
    };

    FIELDS_FILTER_CLASS.init = function () {
        var me = this;
        me._uDialog.init();
        me._uBtn.init();
        me._uConfirmBtn.init();
        me._uCancelBtn.init();

        me._uBtn.onclick = bind(this.$getFiledsListHandler, this);
        me._uConfirmBtn.onclick = bind(this.$submitHandler, this);
        me._uCancelBtn.onclick = bind(this.$cancelHandler, this);
    };

    FIELDS_FILTER_CLASS.$clear = function () {

    };

    FIELDS_FILTER_CLASS.getValue = function () {


    };

    FIELDS_FILTER_CLASS.dispose = function () {

    };
    FIELDS_FILTER_CLASS.$submitHandler = function() {
       var chkName = this._chkName;
       var chkbox = document.getElementsByName(chkName);
       var selectedFields = [];
       for (var i = 0; i < chkbox.length; i ++){
            if (chkbox[i].checked == true) {
                selectedFields.push(chkbox[i].id.split('-')[0]);
            }
       }
       this._uDialog.hide();
       // TODO：发个事件，到component中去
        this.notify('submitFieldsFilter', selectedFields.join(','));
    };

    FIELDS_FILTER_CLASS.$cancelHandler = function() {
        this._uDialog.hide();
    };

    FIELDS_FILTER_CLASS.$getFiledsListHandler = function() {
        var me = this;
        var option = {
            param: {},
            callback: me.$getFiledsListSuccessHandler,
            fieldsFilter: me
        };
        me.notify('getFieldsList', option);
    };

    FIELDS_FILTER_CLASS.$getFiledsListSuccessHandler = function(data) {
        var me = this,
            html = [],
            chkName,
            filterListEl,
            timeStamp = (new Date()).getTime(); // 时间戳
        me._chkName = chkName = timeStamp + 'fields';

        if (!data) {
            return;
        }
        // 拼字段列表
        for (var i = 0, iLen = data.length; i < iLen; i ++) {
            var id = data[i].id + '-' + timeStamp;
            var checked = data[i].selected ? ' checked="checked" ' : '';
            html.push(
                '<li>',
                    '<input type="checkbox" name="', chkName, '" id="', id, '"', checked, '>',
                    '<label for="', id, '">', data[i].name, '</label>',
                '</li>'
            );
        }
        filterListEl = q('table-fields-filter-list', me._uDialog._eMain)[0];
        filterListEl.innerHTML = html.join('');

        me.$clear();
        me._uDialog.center();
        me._uDialog.showModal(DICT.DEFAULT_MASK_OPACITY);
    };
})();