/**
 * di.shared.model.PanelPageRadioAdapter
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    PanelPageManager的适配器（RADIO型）
 * @author:  sushuang(sushuang)
 */

$namespace('di.shared.model');

(function () {
    
    var bind = xutil.fn.bind; 
        
    var PANEL_PAGE_RADIO_ADAPTER = $namespace().PanelPageRadioAdapter = {};
        
    /**
     * 绑定事件
     */
    PANEL_PAGE_RADIO_ADAPTER.$bind = function () {
        this._uPanelPageContainer.onbeforechange = bind(this.$pageBeforeChangeHandler, this);
        this._uPanelPageContainer.onafterchange = bind(this.$pageAfterChangeHandler, this);
    };
    
    /**
     * 增加item
     */
    PANEL_PAGE_RADIO_ADAPTER.$addItem = function (panelPage, options) {
        var container = this._uPanelPageContainer,
            content = container.add({value: options.id, text: options.title}, 
                function() { return panelPage({el: null, parent: container, pageId: options.pageId}); });
        return {content: content, item: options.id};
    };
        
    /**
     * 选择item
     */
    PANEL_PAGE_RADIO_ADAPTER.$selectItem = function (pageWrap) {
        this._uPanelPageContainer.select(pageWrap.item);
    };
    
    /**
     * 得到pageId
     */
    PANEL_PAGE_RADIO_ADAPTER.$retrievalPageId = function () {
        return arguments[0];
    }
    
    /**
     * 更改标题
     */
    PANEL_PAGE_RADIO_ADAPTER.$setTitle = function (pageId, title) {
        // not supported yet
    }
        
    /**
     * 打标记
     */
    PANEL_PAGE_RADIO_ADAPTER.$$mark = function (pageId, mark) {
        // not supported yet
    }
        
})();

