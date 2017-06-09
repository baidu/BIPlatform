/**
 * di.shared.model.MenuPageManager
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    [通用管理器] 菜单行为的托管，菜单页的管理
 * @author:  sushuang(sushuang)
 * @deprecated: 已弃用，因为没必要搞这么多抽象设计得这么复杂，
 *          代码适当堆一块儿反而好找。
 */

// $namespace('di.shared.model');

/**
 * [外部注入]
 * {ecui.ui.PlMenu} menu 左侧菜单
 * {di.shared.model.PanelPageManager} pangelPageManager 页面管理
 */
// (function() {
    
//     //-----------------------------------------
//     // 引用
//     //-----------------------------------------

//     var inheritsObject = xutil.object.inheritsObject; 
//     var bind = xutil.fn.bind;
//     var parseParam = xutil.url.parseParam;
//     var isString = xutil.lang.isString;
//     var getByPath = xutil.object.getByPath;
//     var ecuiCreate = di.helper.Util.ecuiCreate;
//     var XDATASOURCE = xui.XDatasource;
//     var GLOBAL_MODEL;
        
//     $link(function() {
//         GLOBAL_MODEL = di.shared.model.GlobalModel;
//     });
    
//     //-----------------------------------------
//     // 类型声明
//     //-----------------------------------------

//     /**
//      * 菜单管理类
//      *
//      * @class
//      * @extends xui.XDatasource
//      */
//     var MENU_PAGE_MANAGER = $namespace().MenuPageManager = 
//         inheritsObject(XDATASOURCE);
//     var MENU_PAGE_MANAGER_CLASS = MENU_PAGE_MANAGER.prototype;
    
//     //-----------------------------------------
//     // 方法
//     //-----------------------------------------

//     /**
//      * 初始化
//      * @public
//      */    
//     MENU_PAGE_MANAGER_CLASS.init = function() {
//         this._uMenu.onchange = bind(this.$menuChangeHandler, this);
//         this._mPanelPageManager.attach(
//             'page.active', 
//             this.$pageActiveHandler, 
//             this
//         );
//     };
    
//     /**
//      * 获得请求参数
//      * @public
//      */    
//     MENU_PAGE_MANAGER_CLASS.param = function(options) {
//         var globalMenuSel = GLOBAL_MODEL()
//             .getGlobalMenuManager()
//             .getSelected() || {};

//         return 'rootMenuId=' + (globalMenuSel.menuId || '');
//     };
    
//     /**
//      * 解析后台返回
//      * @public
//      */    
//     MENU_PAGE_MANAGER_CLASS.parse = function(data) {
//         var menuTree = data['menuTree'];
//         if (menuTree) {
//             // 菜单数据设置
//             this._uMenu.setData(menuTree.menuList);
//             // 初始时默认选择
//             // this._uMenu.select(menuTree.selMenuId);
//             // this.$menuChangeHandler(this._uMenu.getSelected());
//         }
//     };
    
//     *
//      * 菜单选择行为
//      * @protected
//      * 
//      * @param {Object} menuItem 节点数据对象
//      *          {string} menuId 节点ID
//      *          {string} menuName 节点名
//      *          {string} menuUrl 节点URL
     
//     MENU_PAGE_MANAGER_CLASS.$menuChangeHandler = function(menuItem) {
//         var page;
//         var arr;
//         var pageClass;
//         var param;
//         var menuId = menuItem.menuId;

//         arr = menuItem.menuUrl.split('?');
//         // menuPage中保存的是页面panel page类型
//         pageClass = getByPath(arr[0]);
//         param = parseParam(arr[1]);
            
//         var title = menuItem.menuName;

//         // FIXME
//         // 暂时在此处设置title
//         if (param && param.reportType == 'TABLE') {
//             title = '[表] ' + title;
//         }
//         else if (param && param.reportType == 'CHART') {
//             title = '[图] ' + title;
//         }

//         // FIXME 
//         // 暂时改为总是新建
//         var pageId;
//         if (true || !this._mPanelPageManager.exists(menuId)) {
//             // 不存在页面则新建
//             pageId = 
//             this._mPanelPageManager.add(
//                 function(opt) {
//                     var page;
//                     opt.el.appendChild(param.el = document.createElement('div'));
//                     // 这里的pageClass都是di.shared.ui.PanelPage的派生类
//                     page = new pageClass(param);
//                     page.init(); 
//                     return page;
//                 },
//                 {
//                     // FIXME
//                     // 暂时改为自动生成pageId
//                     /* pageId: menuId, */
//                     title: title,
//                     canClose: true
//                 }
//             );
//         }
        
//         // 选择激活
//         /* this._mPanelPageManager.select(menuId); */
//         this._mPanelPageManager.select(pageId); 
//     };
    
//     /**
//      * 页面选中后的行为
//      */
//     MENU_PAGE_MANAGER_CLASS.$pageActiveHandler = function(menuId) {
//         this._uMenu.select(menuId);
//     };
    
//     /**
//      * 注入管控的对象
//      *
//      * @public
//      */
//     MENU_PAGE_MANAGER_CLASS.inject = function(menu, panelPageManager) {
//         this._uMenu = menu;
//         this._mPanelPageManager = panelPageManager;
//     };

// })();

