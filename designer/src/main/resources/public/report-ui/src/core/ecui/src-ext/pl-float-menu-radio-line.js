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
        UI_PL_FLOAT_MENU_FLOATER_LINE = ui.PlFloatMenu.prototype.Floater.prototype.Line,
        UI_PL_FLOAT_MENU_FLOATER_LINE_CLASS = UI_PL_FLOAT_MENU_FLOATER_LINE.prototype, 
        UI_PL_QUERY_TAB = ui.PlQueryTab;
        UI_PL_QUERY_TAB_CLASS = UI_PL_QUERY_TAB.prototype;

    var UI_PL_FLOAT_MENU_FLOATER_RADIO_LINE = 
            inheritsControl(
                UI_PL_FLOAT_MENU_FLOATER_LINE,
                'ui-float-menu-floater-radio-line',
                null,
                function (el, options) {
                    var o, type = this.getTypes()[0], i, item, 
                        datasource = options.datasource, me = this,
                        radioList = datasource.children || [];
                    
                    if (radioList.length == 0) { // 自动生成一个item作为代表
                        radioList.push({text: datasource.text, value: datasource.value});
                        this._bFakeItem = true;
                    } 
                    
                    this._sUrl = datasource.url;
                    this._sValue = datasource.value;
                    
                    this.getBody().appendChild(o = createDom(type + '-title'));
                    o.innerHTML = datasource.text;
                    this.getBody().appendChild(o = createDom(type + '-detail'));
                    this.getBody().appendChild(createDom('ui-common-clear'));
                    
                    o.appendChild(createDom('ui-query-tab')) && (o = o.firstChild);
                    this._uRadioList = $fastCreate(UI_PL_QUERY_TAB, o, this, {datasource: radioList});
                    this._uRadioList.init();
                    
                    // event handler
                    this._uRadioList.onchange = function (itemValue) {
                        triggerEvent(me, 'itemselect', null, [itemValue]);
                    }
                }
            ),
            
        UI_PL_FLOAT_MENU_FLOATER_RADIO_LINE_CLASS = UI_PL_FLOAT_MENU_FLOATER_RADIO_LINE.prototype;
        
        
        // Register to factory as default
        UI_PL_FLOAT_MENU_FLOATER_LINE_CLASS.registerLineControlType(
            UI_PL_FLOAT_MENU_FLOATER_LINE_CLASS.DEFAULT_KEY, UI_PL_FLOAT_MENU_FLOATER_RADIO_LINE);

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
        
    UI_PL_FLOAT_MENU_FLOATER_RADIO_LINE_CLASS.selectItem = function (itemValue) {
        this._uRadioList.setValue(itemValue);
    }
    
    UI_PL_FLOAT_MENU_FLOATER_RADIO_LINE_CLASS.$itemselect = function (itemValue) {
        var url = this._sUrl + (this._bFakeItem ? '' : ('&indId=' + itemValue)), // indId??
            value = this._sValue + (this._bFakeItem ? '' : ':' + itemValue);
        triggerEvent(this.getParent(), 'floaterlineselect', null, [this, value, url]);
    }
    
    UI_PL_FLOAT_MENU_FLOATER_RADIO_LINE_CLASS.$dispose = function () {
        this.getBody().innerHTML = '';
        UI_CONTROL_CLASS.$dispose.call(this);
    }    

})();
