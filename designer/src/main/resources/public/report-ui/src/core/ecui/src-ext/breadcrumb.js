/**
 * ecui.ui.Breadcrumb
 * Copyright 2013 Baidu Inc. All rights reserved
 *
 * @file:   面包屑导航
 * @author: sushuang(sushuang)
 */

 (function() {
    
    var core = ecui;
    var ui = core.ui;
    var inheritsControl = core.inherits;
    var triggerEvent = core.triggerEvent;
    var disposeControl = core.dispose;
    var UI_CONTROL = ui.Control;
    var UI_CONTROL_CLASS = UI_CONTROL.prototype;

    /**
     * 面包屑导航
     *
     * @class
     * @extends {ecui.ui.Control}
     */
    var UI_BREADCRUMB = ui.Breadcrumb =
        inheritsControl(
            UI_CONTROL,
            'ui-breadcrumb',
            null,
            function(el, options) {
                this.$setOptions(options);
            }
        );
    var UI_BREADCRUMB_CLASS = UI_BREADCRUMB.prototype;

    //--------------------------------------------------
    // UI_BREADCRUMB 方法
    //--------------------------------------------------

    UI_BREADCRUMB_CLASS.$setSize = new Function();
    /**
     * 设置参数
     * 
     * @protected
     * @param {Object} options 参数
     * @parma {number=} options.maxShow 最大显示几项，
     *      如果超出，则中部会为'...'。如果不传此参数全显示。
     * @param {number=} options.hidePosPercent 如果设定了maxShow后，
     *      此参数决定了，如果超出后，那部分会使用“...”来隐藏。
     *      此参数是0到1之前的小数，默认为0.5，表示50%处隐藏。
     * @param {Array.<Object>} options.datasource 主体数据
     *      其中数组每项含有属性：
     *          {string} text 显示文字
     *          {number} value 值
     *          {boolean} disabled 是否可以点击
     *          {string=} url 值，可缺省，如果使用url，
     *              则不会触发change事件
     */
    UI_BREADCRUMB_CLASS.$setOptions = function(options) {
        this._oOptions = options || {};
        this._aDatasource = this._oOptions.datasource || [];
    };

    /**
     * 设置数据并渲染
     *
     * @public
     * @param {string} data 参数，参见setOptions
     */
    UI_BREADCRUMB_CLASS.setData = function(data) {
        this.$setOptions(data);

        this.$disposeInner();

        this.$renderHTML();

        this.$bindEvent();
    };

    /**
     * 渲染HTML
     *
     * @protected
     */
    UI_BREADCRUMB_CLASS.$renderHTML = function() {
        var type = this.getType();
        var html = [];

        // 是否过长，中间需要隐藏
        var hidePos = this.$calculateHide();

        // 渲染
        var hidePushed = false;
        for (var i = 0, item, url; item = this._aDatasource[i]; i ++) {
            url = item.url || '#';
            if (i >= hidePos.start && i <= hidePos.end) {
                if (!hidePushed) {
                    html.push('<span class="' + type + '-hide-item">...<span>');
                    hidePushed = true;
                }
            }
            else if (item.disabled) {
                html.push('<span class="' + type + '-text-item">' + item.text + '<span>');
            }
            else {
                html.push(
                    '<a href="' + url + '" class="' + type + '-link-item" data-breadcrumb-index="' + i +'">' + item.text + '</a>'
                );
            }
        }
        var sepHTML = '<span class="' + type + '-sep">&gt;</span>';
        this.getBody().innerHTML = html.join(sepHTML);
    };

    /**
     * 计算隐藏的起止
     *
     * @protected
     */
    UI_BREADCRUMB_CLASS.$calculateHide = function() {
        var hidePos = {};
        var maxShow = this._oOptions.maxShow;
        var dataLength = this._aDatasource.length;

        if (dataLength > maxShow) {
            if (maxShow == 1) {
                hidePos.start = 0;
                hidePos.end = dataLength - 2;
            }
            else if (maxShow > 1) {
                var per = this._oOptions.hidePosPercent;
                if (per == null || per < 0 || per > 1) {
                    per = 0.5;
                }
                var anchor = Math.floor((maxShow - 1) * per);
                hidePos.start = anchor;
                hidePos.end = dataLength - (maxShow - anchor) - 1;
            }
        }

        return hidePos;
    };

    /**
     * 事件绑定
     *
     * @protected
     */
    UI_BREADCRUMB_CLASS.$bindEvent = function() {
        var me = this;
        var aEls = this.getBody().getElementsByTagName('a');
        for (var i = 0, aEl; aEl = aEls[i]; i ++) {
            if (aEl.getAttribute('data-breadcrumb-index') && aEl.href != '#') {
                aEl.onclick = function() {
                    if (!me._bDisabled) {
                        var ii = this.getAttribute('data-breadcrumb-index');
                        triggerEvent(me, 'change', null, [me._aDatasource[ii]]);
                    }
                    return false;
                }
            }
        }
    };

    /**
     * 析构内部
     * 
     * @protected
     */
    UI_BREADCRUMB_CLASS.$disposeInner = function() {
        this.getBody().innerHTML = '';
    };

 }) ();