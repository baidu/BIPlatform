(function () {
    var core = ecui,
        dom = core.dom,
        ui = core.ui,

        inheritsControl = core.inherits,
        createDom = dom.create,
        moveElements = dom.moveElements,

        UI_BUTTON = ui.Button;

    var UI_PL_BUTTON = ui.PlButton = 
        inheritsControl(
            UI_BUTTON,
            'ui-button',
            function (el, options) {
                var o = createDom(),
                    type = this.getTypes()[0];
            
                moveElements(el, o, true);
                el.innerHTML = '<span class="'+ type +'-inner"></span>';
                moveElements(o, el.firstChild, true);

                if (options.icon) {
                    o = createDom(type + '-icon', '',  'span');
                    el.appendChild(o);
                }
            }
        ),
        UI_PL_BUTTON_CLASS = UI_PL_BUTTON.prototype;
        
    /**
     * 设置控件内部的内容。
     * @public
     *
     * @param {any} innerHTML 内部的内容
     */
    UI_PL_BUTTON_CLASS.setInner = function (innerHTML) {
    	this.getBody().firstChild.innerHTML = innerHTML;
    	this.$resize();
    };
    
    /**
     * 隐藏控件，无论当前是显示状态还是隐藏状态。
     * @public
     *
     * @param {any} innerHTML 内部的内容
     */
    UI_PL_BUTTON_CLASS.hideForce = function () {
    	this.$hide();
    };
    
    /**
     * 显示控件，无论当前是显示状态还是隐藏状态。
     * @public
     *
     * @param {any} innerHTML 内部的内容
     */
    UI_PL_BUTTON_CLASS.showForce = function () {
    	this.$show();
    };
    
    

})();
