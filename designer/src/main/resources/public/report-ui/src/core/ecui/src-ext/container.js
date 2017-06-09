/**
 * ecui.ui.Container
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * desc:    组件容器
 *          提供子组件的创建及管理
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
        blank = util.blank,

        createDom = dom.create,
        getStyle = dom.getStyle,
        extend = util.extend,

        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = UI_CONTROL.prototype;

    var UI_CONTAINER = ui.Container = 
        inheritsControl(
            UI_CONTROL,
            'ui-container',
            function (el, options) {
                var o = createDom(),
                    type = this.getTypes()[0];
                // TODO
            }
        ),
        UI_CONTAINER_CLASS = UI_CONTAINER.prototype;    
     
    UI_CONTAINER_CLASS.setSize = blank; // 禁用setSize 
    
    /**
     * 创建子控件的简便方法
     * @public
     * 
     * @param {string|ecui.ui.Control|Function} type 子控件的类型
     *          如果type为Function，则调用此函数创建子控件，参数为：
     *          @param {HTMLElement} 子控件绑定的DOM元素
     *          @return {ecui.ui.Control} 子控件实例
     * @return {ecui.ui.Control} 创建好的子控件
     */
    UI_CONTAINER_CLASS.createSubControl = function (type, domCreater) {
        var o = createDom();
        
        if (type && type instanceof UI_CONTROL) {
            
        }
        // TODO
    };
    
    /**
     * 删除子控件的简便方法
     * @public
     * 
     * @param {ecui.ui.Control} control 子控件实例
     */
    UI_CONTAINER_CLASS.removeSubControl = function (control) {
        // TODO
    };

    /**
     * 创建子控件的绑定DOM元素
     * 供继承使用，默认为在父控件的getBody()中appendChild
     * @protected
     * 
     * @return {HTMLElement} 创建好的DOM元素
     */
    UI_CONTAINER_CLASS.createSubDom = function () {
        var o = createDom();
        this.getBody().appendChild(o);
        return o;
    };
    
})();
