/**
 * di.shared.model.PanelPageTabAdapter
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    PanelPageManager的适配器（TAB型）
 * @author:  sushuang(sushuang)
 */

$namespace('di.shared.model');

(function () {
    
    var bind = xutil.fn.bind;
    var addClass = xutil.dom.addClass;
    var removeClass = xutil.dom.removeClass;
    
    var PANEL_PAGE_TAB_ADAPTER = $namespace().PanelPageTabAdapter = {};
        
    /**
     * 绑定事件
     */
    PANEL_PAGE_TAB_ADAPTER.$bind = function () {
        this._uPanelPageContainer.onbeforechange = bind(this.$pageBeforeChangeHandler, this);
        this._uPanelPageContainer.onafterchange = bind(this.$pageAfterChangeHandler, this);
        this._uPanelPageContainer.ontabclose = bind(this.$pageCloseHandler, this);
    };
    
    /**
     * 增加item
     */
    PANEL_PAGE_TAB_ADAPTER.$addItem = function (panelPage, options) {
        var o = this._uPanelPageContainer.addTab(
            function (el, parent) {
                return panelPage(
                    { 
                        el: el, 
                        parent: parent, 
                        pageId: options.pageId
                    }
                );
            }, 
            {
                title: options.title,
                index: options.index,
                canClose: options.canClose,
                memo: options.pageId
            }
        );
        return {content: o.tabContent, item: o.tabItem};
    };
    
    /**
     * 选择item
     */
    PANEL_PAGE_TAB_ADAPTER.$selectItem = function (pageWrap) {
        this._uPanelPageContainer.selectTab(pageWrap.item);
    };
        
    
    /**
     * 得到pageId
     */
    PANEL_PAGE_TAB_ADAPTER.$retrievalPageId = function () {
        var item = arguments[0];
        return item.getMemo();
    }

    /**
     * 更改标题
     */
    PANEL_PAGE_TAB_ADAPTER.$setTitle = function (pageId, title) {
        var pageWrap = this._oPanelPageSet.get(pageId);
        pageWrap && pageWrap.item.setTitle(title);
    }    

    /**
     * 打标记
     */
    PANEL_PAGE_TAB_ADAPTER.$mark = function (pageId, mark) {
        var pageWrap = this._oPanelPageSet.get(pageId);
        if (pageWrap) {
            var item = pageWrap.item;
            mark
                ? addClass(item.getOuter(), item.getType() + '-mark')
                : removeClass(item.getOuter(), item.getType() + '-mark');
        }
    }    
    
})();

