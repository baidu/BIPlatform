/**
 * di.shared.model.PanelPageManager
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    [通用管理器] panel page关系页管理：
 *          维护页面引用，页面打开先后顺序，当前页面等。适应不同的页面展现方式（如tab方式或窗口方式等）。
 * @author:  sushuang(sushuang)
 */

$namespace('di.shared.model');

/**
 * [外部注入]
 * {ecui.ui.Control} panelPageContainer 页面容器
 */
(function() {
    
    //------------------------------------------
    // 引用
    //------------------------------------------
    
    var inheritsObject = xutil.object.inheritsObject;
    var XDATASOURCE = xui.XDatasource;
    var bind = xutil.fn.bind;
    var isString = xutil.lang.isString;
    var hasValue = xutil.lang.hasValue;
    var extend = xutil.object.extend;
    var getUID = xutil.uid.getUID;
    var getByPath = xutil.object.getByPath;
    var parseParam = xutil.url.parseParam;
    var LINKED_HASH_MAP = xutil.LinkedHashMap;
    
    //------------------------------------------
    // 类型声明
    //------------------------------------------

    var PANEL_PAGE_MANAGER = $namespace().PanelPageManager = 
        inheritsObject(
            XDATASOURCE,
            /**
             * @param {Object} options
             *          {Object} adapter 适配器
             */
            function(options) {
                // 记录页面访问顺序的队列，队尾为最近访问的
                this._oPanelPageSet = new LINKED_HASH_MAP();
                this._oCurrPageWrap;
                this._sCurrPageId;
                // 挂适配器的方法
                extend(this, options.adapter);
            }
        );
    var PANEL_PAGE_MANAGER_CLASS = PANEL_PAGE_MANAGER.prototype;
        
    /**
     * 初始化
     *
     * @public
     */
    PANEL_PAGE_MANAGER_CLASS.init = function() {
        this.$bind();
    };

    /**
     * 根据url。如果没有则创建，如果有则打开
     *
     * @public 
     * @param {string} uri 如di.some.SomePage?pageId=XXX&pageTitle=XXX&otherParam=XXX
     * @param {Object} options 其他要传入页面的参数（所有在uri中的参数，都可以用这个覆盖）
     * @param {string} options.pageId
     * @param {string} options.pageTitle
     * @param {boolean} options.forceCreate 强制创建新页面。如果为true，则传入的pageId不起作用，会新建pageId
     * @param {boolean} options.forceActive 强制激活, 默认为false
     */
    PANEL_PAGE_MANAGER_CLASS.openByURI = function(uri, options, oncreate) {
        var arr = uri.split('?');
        var pageClass = getByPath(arr[0]);
        var param = parseParam(arr[1]);
        options = options || {};
        extend(param, options);
        var forceCreate = param.forceCreate;
        var pageId = forceCreate
            ? ('PANEL_PAGE_' + getUID('PANEL_PAGE'))
            : param.pageId;
        var pageTitle = param.pageTitle;
        param.panelPageManager = this;

        // 不存在则新建tab页
        var page = this.getPage(pageId);
        if (!page || forceCreate) {
            this.add(
                function(opt) {
                    opt.el.appendChild(param.el = document.createElement('div'));
                    // 这里的pageClass都是di.shared.ui.PanelPage的派生类
                    page = new pageClass(param);
                    return page;
                },
                {
                    pageId: pageId,
                    title: pageTitle,
                    canClose: true
                }
            );
            // 初始化
            page.init();
            oncreate && oncreate(page);
        }

        // 选择激活
        this.select(pageId, param);

        return page;
    };

    /**
     * 增加 panel pange
     *
     * @public 
     * @param {ecui.ui.PanelPage|Function} panelPage 要添加的panel page，
     *          或者创建panel page的回调函数
     *          如果为函数，则：
     *          @param {Object} options 参数
     *                      {HTMLElement} el 在此dom元素内创建
     *                              （根据不同的实现类，可能为空）
     *                      {ecui.ui.Control} parent 父控件
     *                      {string} pageId 页ID
     *          @return {ecui.ui.PanelPage} 页内对象
     * @param {Object} options 参数
     *          {string} pageId 页面ID，如果不传则自动生成一个
     *          {string} title 页面标题，可缺省
     *          {number} index 序号，缺省则在最后添加
     *          {boolean} canClose 是否可以关闭
     * @return {number} 页面实例ID
     */
    PANEL_PAGE_MANAGER_CLASS.add = function(panelPage, options) {
        var o, pageId;
        options = options || {};
        
        if (!panelPage) { return null; }

        !hasValue(pageId = options.pageId) 
            && (pageId = options.pageId = this.$genPageId());

        if (this._oPanelPageSet.containsKey(pageId)) {
            throw new Error('Duplicate panel page ID! id=' + pageId); 
        }
        
        o = this.$addItem(panelPage, options);
        
        this._oPanelPageSet.addFirst(
            { page: o.content, item: o.item }, 
            pageId
        );
        
        return pageId;
    };
    
    /**
     * panel pange是否存在
     *
     * @public 
     * @param {string} panelPageWrap 页面的ID
     * @return {boolean} 是否存在
     */
    PANEL_PAGE_MANAGER_CLASS.exists = function(pageId) {
        return !!this._oPanelPageSet.containsKey(pageId);
    };
    
    /**
     * 选择 panel pange
     *
     * @public 
     * @param {string} nextPageId 页面的ID
     * @param {Object} options 额外参数
     * @param {boolean=} options.forceActive 强制激活（默认为false）
     */
    PANEL_PAGE_MANAGER_CLASS.select = function(nextPageId, options) {
        options = options || {};
        var forceActive = options.forceActive;
        var nextPageWrap = this._oPanelPageSet.get(nextPageId);
        
        if (nextPageWrap) {
            var isChange = nextPageWrap != this._oCurrPageWrap;

            if (isChange) {
                // inactive上一个页面
                if (this._oCurrPageWrap) {
                    this._oCurrPageWrap.page.inactive();
                    this.notify('page.inactive', [this._sCurrPageId]);
                }
                // tab切换
                this._oCurrPageWrap = nextPageWrap;
                var lastPageId = this._sCurrPageId;
                this._sCurrPageId = nextPageId;
                this.$selectItem(nextPageWrap);
                // 下一个页面移动到队尾
                this._oPanelPageSet.remove(nextPageId);
                this._oPanelPageSet.addLast(nextPageWrap, nextPageId);
                this.notify('page.change', [nextPageId, lastPageId]);
            }

            if (forceActive || isChange) {
                // active下一个页面
                nextPageWrap.page.active(options);
                this.notify('page.active', [nextPageId]);
            }
        }
    };

    /**
     * 跳到栈中的某一页面
     *
     * @public
     * @return {number} pageId page号
     * @return {Object} options 额外参数
     */
    PANEL_PAGE_MANAGER_CLASS.goTo = function(pageId, options) {
        this.select(pageId, options);
    };
    
    /**
     * 含有的panel page数量
     *
     * @public
     * @return {number} 数量
     */
    PANEL_PAGE_MANAGER_CLASS.size = function() {
        return this._oPanelPageSet.size();
    };
    
    /**
     * 得到页面实例
     *
     * @public
     * @param {string} pageId 页id
     * @return {PanelPage} panelPage
     */
    PANEL_PAGE_MANAGER_CLASS.getPage = function(pageId) {
        return (this._oPanelPageSet.get(pageId) || {}).page;
    };
    
    /**
     * 得到当前页面实例
     *
     * @public
     * @return {PanelPage} panelPage
     */
    PANEL_PAGE_MANAGER_CLASS.getCurrentPage = function() {
        return this._oCurrPageWrap ? this._oCurrPageWrap.page : null;
    };

    /**
     * 得到当前页面ID
     *
     * @public
     * @return {string} pageId
     */
    PANEL_PAGE_MANAGER_CLASS.getCurrentPageId = function() {
        return this._sCurrPageId;
    };
    
    /**
     * 更改标题
     *
     * @public
     * @param {string} pageId 页id
     * @param {string} title 标题
     */
    PANEL_PAGE_MANAGER_CLASS.setTitle = function(pageId, title) {
        return this.$setTitle(pageId, title);
    };

    /**
     * 打标记
     *
     * @public
     * @param {string} pageId 页id
     * @return {string} title 标题
     */
    PANEL_PAGE_MANAGER_CLASS.mark = function(pageId, mark) {
        return this.$mark(pageId, mark);
    };
    
    /**
     * page before change事件处理
     *
     * @protected
     */
    PANEL_PAGE_MANAGER_CLASS.$pageBeforeChangeHandler = function() {
        if (this._oCurrPageWrap) {
            // inactive上一页
            this._oCurrPageWrap.page.inactive();
            this.notify('page.inactive', [this._sCurrPageId]);
        }
    };
    
    /**
     * page after change事件处理
     *
     * @protected
     */
    PANEL_PAGE_MANAGER_CLASS.$pageAfterChangeHandler = function() {
        var nextPageId = this.$retrievalPageId.apply(this, arguments);
        var lastPageId = this._sCurrPageId;
        var nextPageWrap;
        
        if (nextPageWrap = this._oPanelPageSet.get(nextPageId)) {
            // 当前页面放到记录列表最后
            this._oCurrPageWrap = nextPageWrap;
            this._sCurrPageId = nextPageId;
            this._oPanelPageSet.remove(nextPageId);
            this._oPanelPageSet.addLast(nextPageWrap, nextPageId);
            this.notify('page.change', [nextPageId, lastPageId]);
            // active下一页
            nextPageWrap.page.active();
            this.notify('page.active', [nextPageId]);
        }
    };
    
    /**
     * close事件处理
     *
     * @protected
     */
    PANEL_PAGE_MANAGER_CLASS.$pageCloseHandler = function() {
        var closePageId = this.$retrievalPageId.apply(this, arguments);
        
        // 如果只有一个页面，禁止关闭 
        if (this._oPanelPageSet.size() <= 1) {
            return false;
        }
        
        var closePageWrap = this._oPanelPageSet.remove(closePageId);

        // 修正fromPageId
        this._oPanelPageSet.foreach(
            function(pageId, wrap, index) {
                if (wrap.page.getFromPageId() == closePageId) {
                    wrap.page.setFromPageId(closePageWrap.page.getFromPageId());
                }
            }
        );

        // 关闭页面
        closePageWrap.page.dispose();
        
        // 如果是当前页面，关闭后取最近访问过的一个页面
        if (this._oCurrPageWrap && this._oCurrPageWrap == closePageWrap) {
            this._oCurrPageWrap = null;
            this._sCurrPageId = null;
            this.goTo(this._oPanelPageSet.lastKey());
        }

        this.notify('page.close', [closePageId]);
    };
    
    /**
     * 生成pageId
     *
     * @protected
     * @return {string} 生成的pageId
     */
    PANEL_PAGE_MANAGER_CLASS.$genPageId = function() {
        var id = 1;
        while (this._oPanelPageSet.containsKey(id)) { id ++; }
        return id;
    };  
        
    /**
     * 注入管控对象
     *
     * @public
     */
    PANEL_PAGE_MANAGER_CLASS.inject = function(panelPageContainer) {
        // @protected
        this._uPanelPageContainer = panelPageContainer;
    };

    /**
     * 遍历pages
     *
     * @public
     */
    PANEL_PAGE_MANAGER_CLASS.forEachPage = function(callback) {
        this._oPanelPageSet.foreach(
            function (id, item, index) {
                callback(id, item.page, index);
            }
        );
    };

})();

