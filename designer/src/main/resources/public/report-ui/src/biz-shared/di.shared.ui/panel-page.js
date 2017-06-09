/**
 * ecui.ui.PanelPage
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    容器中子页面基类
 * @author:  sushuang(sushuang)
 */

$namespace('di.shared.ui');

(function() {

    var inheritsObject = xutil.object.inheritsObject;
    var arraySlice = Array.prototype.slice;
    var addClass = xutil.dom.addClass;
    var UI_CONTROL = ecui.ui.Control;
    var UI_CONTROL_CLASS = UI_CONTROL.prototype;
    var XVIEW = xui.XView;

    var PANEL_PAGE = $namespace().PanelPage = 
        inheritsObject(
            XVIEW,
            function(options) {
                addClass(options.el, 'panel-page');
                this._bVisible = true;
                this._aPendingUpdater = [];
                this._sPageId = options.pageId;
                this._sFromPageId = options.fromPageId;
                this._sPageTitle = options.pageTitle;
            }
        );
    var PANEL_PAGE_CLASS = PANEL_PAGE.prototype;
    
    /**
     * 析构
     * @override
     * @private
     */
    PANEL_PAGE_CLASS.dispose = function() {
        this._aPendingUpdater = [];
        PANEL_PAGE.superClass.dispose.call(this);
    };

    /**
     * 更新page视图
     * 因为page视图在要被更新时，可能正处于隐藏的状态，
     *（例如ajax回调时页面已经被切走），
     * 这样有可能导致dom计算出问题（根据page具体实现而定）。
     * 此方法用于延迟更新视图的情况，
     * 如果页面处于显示状态，则正常执行视图更新
     * 如果页面处于隐藏状态，则到显示时（active时）再执行视图更新
     * 
     * @public
     * @param {!Function} updater 更新器（回调）
     * @param {Object=} scope updater执行的scope，缺省则为window
     * @param {...*} args updater执行时传递的参数
     */
    PANEL_PAGE_CLASS.updateView = function(updater, scope, args) {
        if (this._bVisible) {
            updater.apply(scope, arraySlice.call(arguments, 2));
        }
        else {
            this._aPendingUpdater.push(
                {
                    updater: updater,
                    scope: scope,
                    args: arraySlice.call(arguments, 2)
                }
            );
        }
    };

    /**
     * 得到pageId
     *
     * @public
     * @return {string} pageId
     */
    PANEL_PAGE_CLASS.getPageId = function() {
        return this._sPageId;
    };
    
    /**
     * 得到pageTitle
     *
     * @public
     * @return {string} pageTitle
     */
    PANEL_PAGE_CLASS.getPageTitle = function() {
        return this._sPageTitle;
    };
    
    /**
     * 得到来源的pageId
     *
     * @public
     * @return {string} fromPageId
     */
    PANEL_PAGE_CLASS.getFromPageId = function() {
        return this._sFromPageId;
    };
    
    /**
     * 设置来源的pageId
     *
     * @public
     * @param {string} fromPageId
     */
    PANEL_PAGE_CLASS.setFromPageId = function(fromPageId) {
        this._sFromPageId = fromPageId;
    };
    
    /**
     * 激活，PanelPageManager使用
     *
     * @public
     */
    PANEL_PAGE_CLASS.active = function(options) {
        this._bVisible = true;

        // 执行panding的视图更新
        var updaterWrap;
        while(updaterWrap = this._aPendingUpdater.shift()) {
            updaterWrap.updater.apply(updaterWrap.scope, updaterWrap.args);
        }

        this.$active(options || {});
    };

    /**
     * 睡眠，PanelPageManager使用
     *
     * @public
     */
    PANEL_PAGE_CLASS.inactive = function(options) {
        this._bVisible = false;
        this.$inactive(options);
    };

    /**
     * 是否active
     *
     * @public
     */
    PANEL_PAGE_CLASS.isActive = function() {
        return this._bVisible;
    };

    /**
     * 激活，由派生类实现
     *
     * @protected
     * @abstract
     */
    PANEL_PAGE_CLASS.$active = function() {};
    
    /**
     * 睡眠，由派生类实现
     *
     * @protected
     * @abstract
     */
    PANEL_PAGE_CLASS.$inactive = function() {};

})();
