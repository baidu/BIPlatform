/**
 * ecui.ui.SelectCollection
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * desc:    多个选择列组成的控件
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
        triggerEvent = core.triggerEvent,
        disposeControl = core.dispose,

        createDom = dom.create,
        getStyle = dom.getStyle,
        extend = util.extend,

        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = UI_CONTROL.prototype;

    var UI_SELECT_COLLECTION = ui.SelectCollection = 
        inheritsControl(
            UI_CONTROL,
            'ui-select-collection',
            function (el, options) {
                var o = createDom(),
                    type = this.getTypes()[0];
                // TODO
            },
            function (el, options) {
                // TODO
            }
        ),
        UI_SELECT_COLLECTION_CLASS = UI_SELECT_COLLECTION.prototype;
        
    /**
     * 销毁控件
     * @protected
     */
    UI_SELECT_COLLECTION_CLASS.$dispose = function () {
        UI_SELECT_COLLECTION.superClass.$dispose.call(this);
    };        
    
    /**
     * 设置数据源
     * @public
     * 
     * @param {Object} data 数据源
     */
    UI_SELECT_COLLECTION_CLASS.setData = function (data) {
        // TODO
    };        
    
    /**
     * 选择
     * @public
     * 
     * @param // TODO
     */
    UI_SELECT_COLLECTION_CLASS.select = function (/*TODO*/) {
        UI_SELECT_COLLECTION_CLASS.superClass.dispose.call(this);
    };        
    
    /**
     * 会发事件
     * change
     */
    
})();

