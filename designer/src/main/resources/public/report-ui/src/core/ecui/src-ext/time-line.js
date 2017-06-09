/**
 * ecui.ui.TimeLine
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * desc:    时间线
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

    var UI_TIME_LINE = ui.TimeLine = 
        inheritsControl(
            UI_CONTROL,
            'ui-time-line',
            function (el, options) {
                var o = createDom(),
                    type = this.getTypes()[0];
                // TODO
            },
            function (el, options) {
                // TODO
            }
        ),
       UI_TIME_LINE_CLASS = UI_TIME_LINE.prototype;
        
    /**
     * 销毁控件
     * @protected
     */
    UI_TIME_LINE_CLASS.$dispose = function () {
        UI_TIME_LINE.superClass.$dispose.call(this);
    };    
        
})();

