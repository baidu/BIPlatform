/**
 * ecui.ui.TabContainer
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * desc:    TAB页容器基类
 * author:  sushuang(sushuang)
 * depend:  ecui
 */

(function() {

    var array = ecui.array;
    var dom = ecui.dom;
    var ui = ecui.ui;
    var util = ecui.util;
    var string = ecui.string;
    var MAX = Math.max;

    var indexOf = array.indexOf;
    var $fastCreate = ecui.$fastCreate;
    var inheritsControl = ecui.inherits;
    var triggerEvent = ecui.triggerEvent;
    var disposeControl = ecui.dispose;
    var moveElements = dom.moveElements;
    var removeDom = dom.remove;
    var encodeHTML = string.encodeHTML;
    // 引了外部包
    var template = xutil.string.template;
    var textLength = xutil.string.textLength;
    var textSubstr = xutil.string.textSubstr;
    var blank = util.blank;
    var q = xutil.dom.q;

    var createDom = dom.create;
    var getStyle = dom.getStyle;
    var extend = util.extend;

    var UI_CONTROL = ui.Control;
    var UI_TAB = ui.Tab;
    var UI_TAB_CLASS = UI_TAB.prototype;
    var UI_ITEMS = ui.Items;
    var UI_BUTTON = ui.Button;

    /**
     * tab控件
     * 
     * @class
     * @param {Object} options 初始化参数
     */
    var UI_TAB_CONTAINER = ui.TabContainer = inheritsControl(UI_TAB);
    var UI_TAB_CONTAINER_CLASS = UI_TAB_CONTAINER.prototype;
        
    var UI_TAB_ITEM_EXT_CLASS = (UI_TAB_CONTAINER_CLASS.Item = inheritsControl(
            UI_TAB_CLASS.Item, 
            null, 
            null,
            function(el, options) {
                var type = this.getType();

                el.innerHTML = template(TPL_ITEM, {   
                    currClass: this._sClass, 
                    content: el.innerHTML, 
                    close: options.canClose ? TPL_CLOSE_BTN : ''
                });
                    
                this._oMemo = options.memo;
                if (options.canClose) {
                    this._uCloseBtn = $fastCreate(
                        this.CloseBtn, 
                        q('q-close-btn', el)[0], 
                        this, 
                        { primary:'ui-tab-close-btn' }
                    );
                }
            }
        )).prototype;
        
    var UI_TAB_CLOSE_BTN_CLASS = (UI_TAB_ITEM_EXT_CLASS.CloseBtn = 
            inheritsControl(UI_BUTTON)).prototype;

    var UI_TAB_BUTTON_CLASS = (
            UI_TAB_CONTAINER_CLASS.Button = inheritsControl(
                UI_TAB_CLASS.Button,
                null,
                function(el, options) {
                    var type = this.getType();
                    el.appendChild(createDom(type + '-icon'));
                }
            )
        ).prototype;
    
    /*模板*/
    var TPL_ITEM = [
            '<div class="#{currClass}-ledge"></div>',
            '<div class="#{currClass}-lledge"></div>',
            '<div class="#{currClass}-inner">',
                '<span class="#{currClass}-text">#{content}</span>',
                '#{close}',
            '</div>',
            '<div class="#{currClass}-lledge"></div>',
            '<div class="#{currClass}-ledge"></div>'
        ].join('');
    var TPL_CLOSE_BTN = [
            '<span class="ui-tab-close-btn q-close-btn">',
                '<span class="ui-tab-close-btn-icon"></span>',
            '</span>'
        ].join('');
                    
    /**
     * @override
     */
    UI_TAB_CONTAINER_CLASS.$dispose = function() {
        UI_TAB_CONTAINER.superClass.$dispose.call(this);
    };        
        
    /**
     * @override
     */
    UI_TAB_CONTAINER_CLASS.$alterItems = function() {
        this.cache(true, true);
        UI_TAB_CONTAINER.superClass.$alterItems.call(this);
    };

    /**
     * 增加 tab
     * @public 
     * 
     * @param {ecui.ui.Control|Function} tabContent tab页内控件，
     *          或者用于创建页内控件的回调函数
     *          如果为回调函数，则函数参数为：
     *              {HTMLElement} tabEl item的container元素
     *              {ecui.ui.Tab} tabCtrl 父控件
     *              {ecui.ui.Item} tabItem项
     *          返回值为：
     *              {ecui.ui.Control} 页内对象
     * @param {Object} options 参数
     * @param {number} options.index 位置，可缺省
     * @param {string} options.title 页面标题，可缺省
     * @param {boolean} options.canClose 是否可以关闭，默认不可关闭
     * @param {HTMLElement=} options.tabEl 指定的tab el，可缺省
     * @param {HTMLElement=} options.contentEl 指定的content el，可缺省
     * @param {Any} options.memo 附加参数
     * @return {Object}
     *          {ecui.ui.Item} tabItem 子选项控件
     *          {(ecui.ui.Control|HTMLElement)} tabContent 子选项容器
     */    
    UI_TAB_CONTAINER_CLASS.addTab = function(tabContent, options) {
        options = options || {};
        options.canClose = options.canClose || false; 

        var el = options.tabEl;
        if (!el) {
            el = createDom();
            this.getBody().appendChild(el);
        }
        if (el.tagName != 'LABEL') {
            el.innerHTML = '<label>' + options.title + '</label>';
        }
        
        var tabItem = this.add(el, options.index, options);

        if (options.contentEl) {
            tabItem.setContainer(options.contentEl);
        }
        
        if (Object.prototype.toString.call(tabContent) 
                == '[object Function]'
        ) {
            tabContent = tabContent(
                tabItem.getContainer(),
                this,
                tabItem,
                options
            );
        }

        // tabContent && tabContent.$setParent(this);

        return { tabItem: tabItem, tabContent: tabContent };
    };
        
    /**
     * 选择tab
     * @public
     * 
     * @param {ecui.ui.Item} tabItem 被选中的项的控件
     */
    UI_TAB_CONTAINER_CLASS.selectTab = function(tabItem) {
        this.setSelected(tabItem);
    };
    
    /**
     * 关闭tab
     * @public
     * 
     * @param {string} tabId tab的标志
     */
    UI_TAB_CONTAINER_CLASS.$closeTab = function(item) {
        this.remove(item);
    };
    
    //----------------------------------------
    // UI_TAB_ITEM_EXT
    //----------------------------------------
    
    /**
     * 得到附加信息
     * @public
     * 
     * @return {Any} 附加信息
     */
    UI_TAB_ITEM_EXT_CLASS.getMemo = function() {
        return this._oMemo;
    };

    /**
     * 更新标题，并支持过长截断
     * @public
     * 
     * @param {string} title 标题
     */
    UI_TAB_ITEM_EXT_CLASS.setTitle = function(title) {
        var titleEl = q(this._sClass + '-text', this.getOuter())[0];
        var parent = this.getParent();

        if (titleEl) {
            var fullTitle = encodeHTML(title);
            var shortTitle;
            if (textLength(title) > 36) {
                shortTitle = encodeHTML(textSubstr(title, 0, 36) + '...');
            } 
            else {
                shortTitle = fullTitle;
            }
            titleEl.innerHTML = '<label title="' + fullTitle + '">' 
                + shortTitle + '</label>';   

            parent.$alterItems();
            // 增加标题后调整位置
            // TODO
            // 这段逻辑晦涩复杂，效果差强人意，后续重构
            var style = parent.getBody().style;
            var left = parseInt(style.left);
            var itemIndex = indexOf(parent.getItems(), this);
            var itemLeft = parent._aPosition[itemIndex] 
                - (parent._uPrev.isShow() ? 0 : parent._uPrev.getWidth());

            if (left + parent.getBodyWidth() + itemLeft - this.getWidth() < 0) {
                style.left = 
                    MAX(
                        parent._aPosition[itemIndex], 
                        parent.getBodyWidth() - parent.$$titleWidth 
                            - parent._uNext.getWidth()
                    ) 
                    + 'px';
            }
        }
    };

    /**
     * 设置选项卡对应的容器元素。
     * （重载，不将容器元素添加到parent的eMain中。
     *
     * @public
     * @override
     * @param {HTMLElement} el 选项卡对应的容器元素
     */
    UI_TAB_ITEM_EXT_CLASS.setContainer = function (el) {
        var parent = this.getParent();

        if (this._eContainer) {
            removeDom(this._eContainer);
        }
        if (this._eContainer = el) {
            if ((this._sContainer = el.style.display) == 'none') {
                this._sContainer = '';
            }

            if (parent) {
                // 如果当前节点被选中需要显示容器元素，否则隐藏
                el.style.display = parent._cSelected == this 
                    ? this._sContainer : 'none';
            }
        }
    };
        
    /**
     * @override
     */
    UI_TAB_ITEM_EXT_CLASS.$click = function(event) {
        // 更改当前tab
        var par = this.getParent();
        var selected = par.getSelected();

        if (triggerEvent(par, 'beforechange', null, [this, selected]) !== false) {
            UI_TAB_CONTAINER_CLASS.Item.superClass.$click.apply(this, arguments);
            triggerEvent(par, 'afterchange', null, [this, selected]);
        }        
    };

    //----------------------------------------
    // UI_TAB_CLOSE_BTN
    //----------------------------------------
            
    /**
     * @override
     */
    UI_TAB_CLOSE_BTN_CLASS.$click = function(event) {
        // 关闭tab
        var item = this.getParent();
        var tabContainer = item.getParent();
        if (triggerEvent(tabContainer, 'tabclose', null, [item]) !== false) {
            tabContainer.$closeTab(item);
            tabContainer.$alterItems();
        }
        event.stopPropagation();
    };
    
})();

