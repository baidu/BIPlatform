(function () {
    var core = ecui,
        ui = core.ui,
        dom = core.dom,
        string = core.string,
        array = core.array,
        util = core.util,

        $fastCreate = core.$fastCreate,
        inheritsControl = core.inherits,
        triggerEvent = core.triggerEvent,
        getOptions = core.getOptions,
        children = dom.children,
        createDom = dom.create,
        getPosition = dom.getPosition,
        remove = dom.remove,
        trim = string.trim,
        extend = util.extend,
        indexOf = array.indexOf,

        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = UI_CONTROL.prototype,
        UI_RADIO = ui.Radio,
        UI_RADIO_CLASS = UI_RADIO.prototype;

    var UI_PL_QUERY_TAB = ui.PlQueryTab = 
        inheritsControl(
            UI_CONTROL,
            'ui-query-tab',
            function (el, options) {
                var childs = children(el),
                    type = this.getTypes()[0],
                    i, item, itemOptions, value = options.value;

                this._aItems = [];
                this._bPointer = options.pointer === true;
                
                for (i = 0; item = childs[i]; i++) {
                    item.className = trim(item.className) + ' ' + type + '-item' + UI_RADIO.TYPES;
                    (itemOptions = getOptions(item)) && itemOptions.tips && (itemOptions.tips = decodeURIComponent(itemOptions.tips));
                    this._aItems[i] = $fastCreate(this.Item, item, this, extend({pointer: this._bPointer}, itemOptions));
                }

                if (options.value) {
                    this.setValue(options.value);
                }
            }
        ),

        UI_PL_QUERY_TAB_CLASS = UI_PL_QUERY_TAB.prototype,
        UI_PL_QUERY_TAB_ITEM_CLASS = (UI_PL_QUERY_TAB_CLASS.Item = 
            inheritsControl(UI_RADIO, 
                'ui-query-tab-item', 
                function (el, options) {
                    var type = this.getTypes()[0],
                        o, value;

                    if (options.pointer) {
                        this._ePointer = createDom(type + '-pointer');
                        el.appendChild(this._ePointer);
                    }
                    if (options.tips) {
                        this.getOuter().title = options.tips;
                    }
                    if (options.matrix) {
                        this._bMatrix = true;
                        this.alterClass('+matrix');
                        document.body.appendChild(o = createDom('ui-query-tab-matrix', 'display:none'));
                        this._uMatrix = $fastCreate(this.Matrix, o, this, {
                            disableds: options.disableds, 
                            xw: options.matrix.x, 
                            yw: options.matrix.y
                        });
                        el.appendChild(createDom(type + '-matrix-icon', '', 'span'));
                    }
                },
                function (el, options) {
                    var o;

                    options.overflow = false;
                    if (options.matrix && typeof options.matrix == 'string') {
                        o = options.matrix.split('~');
                        options.matrix = {};
                        options.matrix.x = parseMatrixW(o[0]);
                        options.matrix.y = parseMatrixW(o[1]);
                    }
                }
            )).prototype,

        UI_PL_QUERY_TAB_MATRIX = UI_PL_QUERY_TAB_ITEM_CLASS.Matrix = 
            inheritsControl(
                UI_CONTROL,
                'ui-query-tab-matrix',
                function (el, options) {
                    var i, j, item, value,
                        //xw = children(els[0]),
                        //yw = children(els[1]),
                        xw = options.xw,
                        yw = options.yw,
                        html = ['<table cellpadding="0" cellspacing="0"><tr><th>&nbsp;</th>'];

                    this._nX = xw.length;
                    for (i = 0; item = xw[i]; i++) {
                        html.push('<th class="'+ (i == xw.length - 1 ? 'last' : '') +'">' + item.text + '</th>');
                    }
                    html.push('</tr>');
                    for (i = 0; item = yw[i]; i++) {
                        html.push('<tr class="'+ (i == yw.length - 1 ? 'last' : '') +'"><th>' + item.text + '</th>');
                        for (j = 0; j < xw.length; j++) {
                            html.push('<td class="ui-query-tab-matrix-item '+ (j == xw.length - 1 ? 'last' : '') +'"><span class="ui-query-tab-matrix-item-icon"></span></td>');
                        }
                        html.push('</tr>');
                    }
                    html.push('</table>');
                    el.innerHTML = html.join('');
                    
                    els = el.getElementsByTagName('td');
                    this._eTH = el.getElementsByTagName('th');
                    this._aItems = [];
                    this._aDisabled = options.disableds || [];
                    for (i = 0; item = els[i]; i++) {
                        value = xw[i % xw.length].value + '|' + yw[Math.floor(i / xw.length)].value;
                        j = indexOf(this._aDisabled, value);
                        this._aItems.push(item = $fastCreate(this.Item, item, this, {value: value}));
                        if (j >= 0) {
                            item.setContent('不可选');
                            item.disable();
                        }
                    }
                },
                function (el, options) {
                    var o;

                    if (o = options.disableds) {
                        if (typeof o == 'string') {
                            options.disableds = o.split(',');
                        }
                    }
                }
            ),

        UI_PL_QUERY_TAB_MATRIX_CLASS = UI_PL_QUERY_TAB_MATRIX.prototype,
        UI_PL_QUERY_TAB_MATRIX_ITEM_CLASS = (UI_PL_QUERY_TAB_MATRIX_CLASS.Item = (
            inheritsControl(
                UI_CONTROL,
                'ui-query-tab-matrix-item',
                function (el, options) {
                    this._sValue = options.value;
                }
            )
        )).prototype;

    function parseMatrixW(str) {
        var res = [], i, item;

        str = str.split(',');
        for (i = 0; item = str[i]; i++) {
            item = item.split('|');
            res.push({text: item[0], value: item[1]});
        }
        return res;
    }

    function UI_PL_QUERY_TAB_CHANGE_VALUE(control, item) {
        var curChecked = control._oCurChecked;

        if (!curChecked || curChecked != item || item._bMatrix) {
            control._oCurChecked = item;
            triggerEvent(control, 'change', null, [item.getValue()]);
        }
    }

    UI_PL_QUERY_TAB_ITEM_CLASS.$click = function () {
        if (!this._bMatrix) {
            UI_RADIO_CLASS.$click.call(this);
            UI_PL_QUERY_TAB_CHANGE_VALUE(this.getParent(), this);
        }
    };

    UI_PL_QUERY_TAB_ITEM_CLASS.$mouseover = function () {
        UI_RADIO_CLASS.$mouseover.call(this);
        if (this._bMatrix) {
            if (!this.isChecked()) {
                this._uMatrix.$setSelected(null);
            }
            this._uMatrix.show(this);
        }
    };

    UI_PL_QUERY_TAB_ITEM_CLASS.$mousemove = function () {
        UI_RADIO_CLASS.$mousemove.call(this);
        // 防止鼠标从浮层中移动到Radio上时由于两控件重叠
        // 只触发浮层的mouseout而没有触发Radio的mouseover导致浮层会被关闭
        if (this._bMatrix) {
            this._uMatrix.$clearTimeout();
        }
    }

    UI_PL_QUERY_TAB_ITEM_CLASS.$mouseout = function () {
        UI_RADIO_CLASS.$mouseout.call(this);
        if (this._bMatrix) {
            this._uMatrix.hide();
        }
    };

    /* override */
    UI_PL_QUERY_TAB_ITEM_CLASS.getItems = function () {
        return this.getParent().getItems();
    };

    UI_PL_QUERY_TAB_ITEM_CLASS.getValue = function () {
        var value = UI_RADIO_CLASS.getValue.call(this);

        if (this._bMatrix) {
            value  += '|' + this._uMatrix.getValue();
        }

        return value;
    };

    UI_PL_QUERY_TAB_CLASS.getItems = function () {
        return this._aItems.slice();
    };

    UI_PL_QUERY_TAB_CLASS.getValue = function () {
        return this._oCurChecked ? this._oCurChecked.getValue() : null;
    };

    UI_PL_QUERY_TAB_CLASS.setValue = function (value) {
        var s;

        for (var i = 0, item; item = this._aItems[i]; i++) {
            if (!item._bMatrix && item.getValue() == value) {
                item.setChecked(true);
                this._oCurChecked = item;
            }
            else if (item._bMatrix && typeof value == 'string' && value.indexOf('|') > 0) {
                s = value.split('|');
                if (UI_RADIO_CLASS.getValue.call(item) == s[0] 
                        && item._uMatrix.$setSelected(s[1] + '|' + s[2])) {
                    item.setChecked(true);
                    this._oCurChecked = item;
                }
            }
        }
    };

    UI_PL_QUERY_TAB_CLASS.add = function (text, value, options) {
        var type = this.getTypes()[0],
            item = createDom(type + '-item' + UI_RADIO.TYPES, '', 'span');

        options = options || {};
        item.innerHTML = text;
        this.getBody().appendChild(item);
        item = $fastCreate(this.Item, item, this, extend({value: value, pointer: this._bPointer}, options));
        this._aItems.push(item);
    };

    UI_PL_QUERY_TAB_MATRIX_CLASS.getValue = function () {
        return this._oSelected ? this._oSelected.getValue() : '';
    };

    UI_PL_QUERY_TAB_MATRIX_CLASS.$setSelected = function (item) {
        var items, i, o;

        if (typeof item == 'string') {
            o = item;
            for (i = 0, items = this._aItems; item = items[i]; i++) {
                if (o == item.getValue()) {
                    break;
                }
            }
        }

        if (item == this._oSelected) {
            return false;
        }

        if (this._oSelected) {
            this._oSelected.alterClass('-selected');
        }

        if (item) {
            item.alterClass('+selected');
        }
        this._oSelected = item;
        return !!item;
    };

    UI_PL_QUERY_TAB_MATRIX_CLASS.setSelected = function (item) {
        var par = this.getParent();

        if (this.$setSelected(item)) {
            par.setChecked(true);
            UI_PL_QUERY_TAB_CHANGE_VALUE(par.getParent(), par);
            this.isShow() && this.$hide();
        }
    };

    UI_PL_QUERY_TAB_MATRIX_CLASS.$clearTimeout = function () {
        if (this._oTimer) {
            clearTimeout(this._oTimer);
            this._oTimer = null;
        }
    };

    UI_PL_QUERY_TAB_MATRIX_CLASS.$setTimeout = function (fuc, bind, time) {
        var call = function () {
            fuc.call(bind);
        }
        this.$clearTimeout();
        this._oTimer = setTimeout(call, time || 0);
    }

    UI_PL_QUERY_TAB_MATRIX_CLASS.show = function (con) {
        var pos = getPosition(con.getOuter());

        this.$clearTimeout(); 
        this.setPosition(pos.left, pos.top + con.getHeight() - 1);
        UI_CONTROL_CLASS.show.call(this);
    };

    UI_PL_QUERY_TAB_MATRIX_CLASS.highlight = function (item, flag) {
        var index = indexOf(this._aItems, item),
            len = this._nX, i, s;

        for (s = Math.floor(index / len) * len, i = s; i < s + len; i++) {
            item = this._aItems[i];
            !item.isDisabled() && item.alterClass((flag ? '+' : '-') + 'highlight');
        }
        item = this._eTH[Math.floor(index / len) + len + 1];
        if (flag) {
            item.className += ' highlight';
        }
        else {
            item.className = item.className.replace(/\s*highlight/g, '');
        }

        for (s = this._aItems.length, i = index % len; i < s; i += len) {
            item = this._aItems[i];
            !item.isDisabled() && item.alterClass((flag ? '+' : '-') + 'highlight');
        }
        item = this._eTH[index % len + 1];
        if (flag) {
            item.className += ' highlight';
        }
        else {
            item.className = item.className.replace(/\s*highlight/g, '');
        }

    };

    UI_PL_QUERY_TAB_MATRIX_CLASS.hide = function () {
        this.$setTimeout(this.$hide, this);
    };

    UI_PL_QUERY_TAB_MATRIX_CLASS.$mouseover = function () {
        UI_CONTROL_CLASS.$mouseover.call(this);
        this.$clearTimeout();
    };

    UI_PL_QUERY_TAB_MATRIX_CLASS.$mouseout = function (event) {
        if (event.target && 
                event.target.getControl && 
                this.contain(event.target.getControl()) && 
                event.target.getControl().isDisabled()) {
            event.stopPropagation();
            return;
        }
        UI_CONTROL_CLASS.$mouseout.call(this);
        this.hide();
    };

    UI_PL_QUERY_TAB_MATRIX_CLASS.$dispose = function () {
        this.$clearTimeout();
        remove(this.getOuter());
        UI_CONTROL_CLASS.$dispose.call(this);
    };

    UI_PL_QUERY_TAB_MATRIX_ITEM_CLASS.getValue = function () {
        return this._sValue;
    };

    UI_PL_QUERY_TAB_MATRIX_ITEM_CLASS.$click = function () {
        this.getParent().setSelected(this);
    };

    UI_PL_QUERY_TAB_MATRIX_ITEM_CLASS.$mouseover = function () {
        UI_CONTROL_CLASS.$mouseover.call(this);
        this.getParent().highlight(this, true);
    };

    UI_PL_QUERY_TAB_MATRIX_ITEM_CLASS.$mouseout = function () {
        UI_CONTROL_CLASS.$mouseout.call(this);
        this.getParent().highlight(this, false);
    }
})();
