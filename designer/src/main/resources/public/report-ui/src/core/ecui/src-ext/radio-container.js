/**
 * ecui.ui.RadioContainer
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * desc:    简单的Radio页容器，行为为tab类同，适于tab与contant视图上分离的场景，并且也支持多个tab共享content
 * author:  sushuang(sushuang)
 * depend:  ecui
 */

(function () {
    var core = ecui,
        array = core.array,
        dom = core.dom,
        ui = core.ui,
        util = core.util,
        string = core.string,

        $fastCreate = core.$fastCreate,
        inheritsControl = core.inherits,
        addClass = dom.addClass,
        triggerEvent = core.triggerEvent,
        disposeControl = core.dispose,
        moveElements = dom.moveElements,
        blank = util.blank,

        createDom = dom.create,
        getStyle = dom.getStyle,
        extend = util.extend,

        UI_CONTROL = ui.Control;

    /* 类型声明 */
    var UI_RADIO_CONTAINER = ui.RadioContainer = inheritsControl(
            UI_CONTROL, 'ui-radio-container', null,
            function(el, options) {
                this._aItemList = [];
                this._uCurrItem;
            }),
        UI_RADIO_CONTAINER_CLASS = UI_RADIO_CONTAINER.prototype,
        
        UI_RADIO_CONTAINER_ITME_CLASS = (UI_RADIO_CONTAINER_CLASS.Item = inheritsControl(
            UI_CONTROL, 'ui-radio-container-item', null,
            function(el, options) {
                var type = this.getType();
                if (options.itemIndex > 0) { 
                    addClass(el, type + '-space');
                }
                options = options || {};
                this._sValue = options.value;
                this._sText = options.text;
                el.innerHTML = options.text;
            }
        )).prototype;
            
    /**
     * @override
     */
    UI_RADIO_CONTAINER_CLASS.$dispose = function () {
        UI_RADIO_CONTAINER.superClass.$dispose.call(this);
    };        
    
    /**
     * 增加 
     * @public
     *
     * @param {Object} data
     *          {string} value 值，不可重复
     *          {string} text 文字
     * @param {Function} contentCreater 创建radio页内控件的回调函数
     *          @param {Object} data 每个控件里的数据
     *                  {string} value 值
     *                  {string} text 文字
     *          @return {ecui.ui.Control|HTMLElement} radio页内容控件或DOM
     * @return {ecui.ui.Control} item内容控件
     */
    UI_RADIO_CONTAINER_CLASS.add = function (data, contentCreater) {
        var i, o, item,
            el = this.getBody(),
            itemType = this.getType() + '-item';
            
        el.appendChild(o = createDom(itemType));
        item = $fastCreate(this.Item, o, this, {
            value: data.value, 
            text: data.text, 
            primary: itemType, 
            itemIndex: this._aItemList.length
        });
        this._aItemList.push(item);
        
        contentCreater && (item._uContent = contentCreater.call(item));
        
        item.$hideContent();
        
        return item._uContent;
    };
    
    /**
     * 清空
     * @public
     */
    UI_RADIO_CONTAINER_CLASS.clear = function () {
        var i, item;
        for (i = 0; item = this._aItemList[i]; i ++) {
            item.$hideContent();
            item._uContent = null;
            disposeControl(item);
        }
        this._aItemList = [];
        this.getBody().innerHTML = '';
    };

    /**
     * 选择
     * @public
     * 
     * @param {string} value 选择某个值对应的项
     */
    UI_RADIO_CONTAINER_CLASS.select = function (value) {
        var i, o, toShowContent;
        for (i = 0; o = this._aItemList[i]; i++) {
            if (o._sValue == value) {
                this._uCurrItem = o;
                o.alterClass('+selected');
                toShowContent = o._uContent;
            } else {
                o.alterClass('-selected');
            }
        }
        for (i = 0; o = this._aItemList[i]; i++) {
            if (o._uContent == toShowContent) {
                o.$showContent();
            } else {
                o.$hideContent();
            }
        }
    };
    
    //--------------------------------------------------------
    // UI_RADIO_CONTAINER_ITME
    //--------------------------------------------------------
    
    /**
     * @override
     */
    UI_RADIO_CONTAINER_ITME_CLASS.$click = function (event) {
        // 更改当前tab
        var container = this.getParent();
        if (triggerEvent(container, 'beforechange', null, [this._sValue]) !== false) {
            container.select(this._sValue);
            triggerEvent(container, 'change', null, [this._sValue]);
            triggerEvent(container, 'afterchange', null, [this._sValue]);
        }
    };

    /**
     * 显示content
     * @protected
     */
    UI_RADIO_CONTAINER_ITME_CLASS.$showContent = function () {
        if (this._uContent) {
            if (this._uContent instanceof UI_CONTROL) {
                this._uContent.getOuter().style.display = '';
            } else {
                this._uContent.style.display = '';
            }
        }
    };  
    
    /**
     * 隐藏content
     * @protected
     */
    UI_RADIO_CONTAINER_ITME_CLASS.$hideContent = function () {
        if (this._uContent) {
            if (this._uContent instanceof UI_CONTROL) {
                this._uContent.getOuter().style.display = 'none';
            } else {
                this._uContent.style.display = 'none';
            }
        }
    };  
})();

