/**
 * di.shared.model.GlobalMenuManager
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * desc:    [通用模型] 全局菜单管理
 * author:  sushuang(sushuang)
 */

$namespace('di.shared.model');

/**
 * [外部注入]
 * globalMenu
 */
(function() {
    
    /* 外部引用 */
    var inheritsObject = xutil.object.inheritsObject;
    var bind = xutil.fn.bind;
    var getByPath = xutil.object.getByPath;
    var XDATASOURCE = xui.XDatasource;
        
    /* 类型声明 */
    var GLOBAL_MENU_MANAGER = $namespace().GlobalMenuManager = 
            inheritsObject(
                XDATASOURCE,
                function (options) {
                    this.businessData = options.globalMenu;
                }
            );
    var GLOBAL_MENU_MANAGER_CLASS = GLOBAL_MENU_MANAGER.prototype;
    
    /**
     * 析构
     * @protected
     */
    GLOBAL_MENU_MANAGER_CLASS.$dispose = function() {
        GLOBAL_MENU_MANAGER.superClass.$dispose.call(this);
    };
    
    /**
     * 获得当前所选
     * @public
     * 
     * @return {Object} 当前选择
     *          {string} menuId 菜单ID
     *          {string} menuName 菜单名
     *          {string} menuPage 额外数据
     *          {string} menuUrl 菜单URL
     */
    GLOBAL_MENU_MANAGER_CLASS.getSelected = function() {
        return this.businessData && this.businessData.selMenu;
    };
    
    /**
     * 获得菜单数据
     * @public
     * 
     * @return {Array} 菜单数据
     */
    GLOBAL_MENU_MANAGER_CLASS.getMenuData = function() {
        return this.businessData && this.businessData.menuList;
    }
    
    /**
     * 获得当前页面根控件类型
     * @public
     * 
     * @return {Constructor#ecui.ui.Control} 当前页面根控件类型
     */
    GLOBAL_MENU_MANAGER_CLASS.getControlClass = function() {
        var classPath = (this.getSelected() || {}).menuPage;
        return classPath ? getByPath(classPath) : null;
    };
    
    /**
     * 获得数据
     * @protected
     */
    GLOBAL_MENU_MANAGER_CLASS.parse = function(data) {
        // 从GLOBAL_MODEL中获取数据，并保存在此
        var globalMenu = data && data.globalMenu || {};
        this.businessData = {
            menuList: globalMenu.menuList, 
            selMenu: globalMenu.selMenu
        };
        return this.businessData;
    };
    
    /**
     * 顶层页跳转
     * @public
     */
    GLOBAL_MENU_MANAGER_CLASS.changeMenu = function(args) {
        // to be continued ...
    };

    /**
     * 设置
     * 在派生类中使用
     */
    GLOBAL_MENU_MANAGER_CLASS.setGlobalMenu = function(gm) {
        this._uGlobalMenu = gm;
    };    
        
})();

