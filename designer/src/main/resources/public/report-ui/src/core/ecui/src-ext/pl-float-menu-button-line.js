(function () {
    var core = ecui,
        array = core.array,
        dom = core.dom,
        ui = core.ui,
        util = core.util,
        string = core.string,

        $fastCreate = core.$fastCreate,
        inheritsControl = core.inherits,
        getMouseX = core.getMouseX,
        triggerEvent = core.triggerEvent,
        disposeControl = core.dispose,
        getOptions = core.getOptions,

        createDom = dom.create,
        getStyle = dom.getStyle,
        first = dom.first,
        moveElements = dom.moveElements,
        toNumber = util.toNumber,
        extend = util.extend,
        blank = util.blank,
        indexOf = array.indexOf,
        trim = string.trim,

        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = UI_CONTROL.prototype,
        UI_BUTTON = ui.Button,
        UI_PL_FLOAT_MENU_FLOATER_LINE = ui.PlFloatMenu.prototype.Floater.prototype.Line,
        UI_PL_FLOAT_MENU_FLOATER_LINE_CLASS = UI_PL_FLOAT_MENU_FLOATER_LINE.prototype;

    var UI_PL_FLOAT_MENU_FLOATER_BUTTON_LINE = 
            inheritsControl(
                UI_PL_FLOAT_MENU_FLOATER_LINE,
                'ui-float-menu-floater-radio-line',
                null,
                function (el, options) {
                    var o, type = this.getTypes()[0], i, item, 
                        datasource = options.datasource, me = this,
                        children = datasource.children || [];
                    
                    this._sUrl = datasource.url;
                    this._sText = datasource.text;
                    this._sValue = datasource.value;

                    this._aBtnList = [];
                    var tmpEl = createDom();

                    for (var i = 0, o; o = children[i]; i ++) {
                        tmpEl.innerHTML = '<div class="ui-button-g ui-button q-btn-table">' + o.text + '</div>';
                        el.appendChild(tmpEl.firstChild);
                        this._aBtnList.push($fastCreate(UI_BUTTON, el.lastChild, this, {}));

                        // 绑定事件
                        this._aBtnList[this._aBtnList.length - 1].onclick = (function(oo) {
                            return function() {
                                triggerEvent(me, 'itemselect', null, [oo]);
                            }
                        })(extend({},o));
                    }

                    // event handler
                    // this._uRadioList.onclick = function (itemValue) {
                    // }
                }
            ),
            
        UI_PL_FLOAT_MENU_FLOATER_BUTTON_LINE_CLASS = UI_PL_FLOAT_MENU_FLOATER_BUTTON_LINE.prototype;
        
        // Register to factory as default
        UI_PL_FLOAT_MENU_FLOATER_LINE_CLASS.registerLineControlType(
            UI_PL_FLOAT_MENU_FLOATER_LINE_CLASS.DEFAULT_KEY, UI_PL_FLOAT_MENU_FLOATER_BUTTON_LINE);

    /**
     * @param {String} value 例如 1102:22, 如果为null表示清空选择
     */
    UI_PL_FLOAT_MENU_FLOATER_LINE_CLASS.select = function (value) {
        var o, lineValue, itemValue;
        if (value !== null && value !== undefined && (value = trim(value)) !== '') {
            o = value.split(':');
            lineValue = o[0];
            itemValue = o[1];
            if (lineValue != this._sValue) { return; }
            (itemValue === undefined || trim(itemValue) === '') && (itemValue = lineValue); // fake item
            this.selectItem(itemValue);
        } 
        else { // 清空
            this.selectItem(null);
        }
    }       
        
    UI_PL_FLOAT_MENU_FLOATER_BUTTON_LINE_CLASS.selectItem = function (itemValue) {
        // do nothing
    }
    
    UI_PL_FLOAT_MENU_FLOATER_BUTTON_LINE_CLASS.$itemselect = function (itemValue) {
        triggerEvent(
            this.getParent(), 
            'floaterlineselect', 
            null, 
            [
                this, 
                {
                    value: this._sValue,
                    text: this._sText,
                    url: itemValue.url
                }
            ]
        );
    }
    
    UI_PL_FLOAT_MENU_FLOATER_BUTTON_LINE_CLASS.$dispose = function () {
        this.getBody().innerHTML = '';
        UI_CONTROL_CLASS.$dispose.call(this);
    }    

})();
