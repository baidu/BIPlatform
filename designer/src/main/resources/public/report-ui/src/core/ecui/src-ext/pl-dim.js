(function () {
    var core = ecui,
        array = core.array,
        dom = core.dom,
        ui = core.ui,
        util = core.util,
        string = core.string,

        $fastCreate = core.$fastCreate,
        setFocused = core.setFocused,
        disposeControl = core.dispose,
        getOptions = core.getOptions,
        createDom = dom.create,
        children = dom.children,
        moveElements = dom.moveElements,
        getPosition  = dom.getPosition,
        getStyle = dom.getStyle,
        setStyle = dom.setStyle,
        inheritsControl = core.inherits,
        getView = util.getView,
        extend = util.extend,
        blank = util.blank,
        triggerEvent = core.triggerEvent,

        UI_CONTROL = ui.Control,
        UI_FORM = ui.Form,
        UI_CONTROL_CLASS = UI_CONTROL.prototype,
        UI_PL_INPUT_TREE = ui.PlInputTree,
        UI_PL_INPUT_TREE_CLASS = UI_PL_INPUT_TREE.prototype,

        UI_PL_DIM = ui.PlDim = 
        inheritsControl(
            UI_CONTROL,
            'ui-dim',
            function (el, options) {
                options.resizable = false;
                el.innerHTML = UI_PL_DIM.getInnerHTML(options);
            },
            function (el, options) {
            }
        ),

        UI_PL_DIM_CLASS = UI_PL_DIM.prototype,
        UI_PL_DIM_INPUT_TREE_CLASS = (
            UI_PL_DIM_CLASS.Item = inheritsControl(
                UI_PL_INPUT_TREE, 
                'ui-dim-item', 
                function (el, options) {
                    this._sDimId = options.dimid; 
                    this._bSingleMode = options.multi === false;
                    if (options.tips) {
                        this.getOuter().title = options.tips;
                    }
                } 
            )
        ).prototype,
        UI_PL_DIM_INPUT_TREE_LAYER_CLASS = (UI_PL_DIM_INPUT_TREE_CLASS.Layer = inheritsControl(UI_PL_INPUT_TREE_CLASS.Layer)).prototype;

    function eachItems(con, callback) {
        for (var i = 0, item; item = con._aItems[i]; i++) {
            callback.call(null, item, i);
        }
    }

    function getChildTreeHTML(item, html) {
        if (item.children && item.children.length > 0) {
            html.push('<div><label ecui="value:'+ item.dimNodeId + 
                (item.selectable === false ? ';selectable:false': '') + '">' + item.dimNodeName + '</label>');
            for (var i = 0, child; child = item.children[i]; i++) {
                getChildTreeHTML(child, html);
            }
            html.push('</div>');
        }
        else {
            html.push('<div ecui="value:'+ item.dimNodeId + 
                (item.selectable === false ? ';selectable:false': '') + '">' + item.dimNodeName + '</div>');
        }
    }

    function getTreeHTML(root, html) {
        html.push('<label ecui="value:'+ root.dimNodeId + 
            (root.selectable === false ? ';selectable:false': '') + '">' + root.dimNodeName + '</label>');
        for (var i = 0, child; root.children && (child = root.children[i]); i++) {
            getChildTreeHTML(child, html);
        }
    }

    UI_PL_DIM.getInnerHTML = function (options) {
        var datasource = options.datasource || [],
            html = [], i, item, linkage;

        for (i = 0; item = datasource[i]; i++) {
            html.push('<div ecui="title:' + item.dim.dimName + ';dimid:' + item.dim.dimId);
            /**if (item.selItem && item.selItem.length > 0) {
                html.push(';value:' + item.selItem.join(','));
            }**/
            if (item.multi !== undefined) {
                html.push(';multi:' + item.multi);
            }
            if (item.tips) {
                html.push(';tips:' + encodeURIComponent(item.tips)); // escape spetial characters
            }
            html.push('">');
            getTreeHTML(item.rootNode, html);
            html.push('</div>');
        }
        return html.join('');
    }
    
    UI_PL_DIM_CLASS.init = function () {
        var priUID = null;
        UI_CONTROL_CLASS.init.call(this);
    };

    UI_PL_DIM_CLASS.$setSize = blank;

    UI_PL_DIM_CLASS.setData = function (data, options) {
        options = options || {};
        options.datasource = data;
        var el = this.getBody();
        this.getBody().innerHTML = UI_PL_DIM.getInnerHTML(options);
        
        // TODO 修整下，不要这么麻烦走HTML了
        var i, item, itemOptions, con, type = this.getTypes()[0];

        this._aItems = [];
        this._nTextLen = options.textLen || 15;
        this._nMaxSelected = options.maxSelected || Number.MAX_VALUE;
        el = children(el);
        for( i = 0; item = el[i]; i ++) {
            (itemOptions = getOptions(item)) && itemOptions.tips && 
                (itemOptions.tips = decodeURIComponent(itemOptions.tips));
            options = extend({
                textLen: this._nTextLen,
                maxSelected: this._nMaxSelected
            }, itemOptions);
            if (options.multi !== false) {
                options.multi = true;
            }
            item.className = type + '-item';
            this._aItems.push(con = $fastCreate(this.Item, item, this, options));
            con.init();
        }
    }
    
    /**
     * 设置维度值
     *
     * {dimId:'xxx,xxxx,xxx', dimId:'xxxx'}
     *
     */
    UI_PL_DIM_CLASS.setValues = function (values) {
        eachItems(this, function (item, idx) {
            item.clear();
            item.setValues(values[item.getDimId()]);
        });
    }

    UI_PL_DIM_CLASS.setValuesStr = function (str) {
        var values = {}, i, item;
        str = str.split('|');
        for (i = 0; item = str[i]; i++) {
            item = item.split(':');
            values[item[0]] = item[1];
        }
        this.setValues(values);
    }

    UI_PL_DIM_CLASS.getValues = function () {
        var res = {};
        eachItems(this, function (item, idx) {
            key = item.getDimId();
            res[key] = item.getValue();
        });
        return res;
    }

    UI_PL_DIM_CLASS.getValuesStr = function () {
        var res = this.getValues(), key,
            str = [];

        for (key in res) {
            str.push(key + ':' + res[key]);
        }
        return str.join('|');
    }

    UI_PL_DIM_INPUT_TREE_CLASS.init = function () {
        var tree = this._uLayer._uTree,
            childs = tree.getChildren(), i, item;

        UI_PL_INPUT_TREE_CLASS.init.call(this);

        //默认展开到第三层级
        tree.expand();
        for (i = 0; item = childs[i]; i++) {
            if (item.getChildren().length > 0) {
                item.expand();
            }
        }
        
    }

    UI_PL_DIM_INPUT_TREE_CLASS.$activate = blank;

    UI_PL_DIM_INPUT_TREE_CLASS.$click = function (e) {
        UI_PL_INPUT_TREE_CLASS.$click.call(this, e);
        if (e.getControl() == this) {
            this._uLayer[this._uLayer.isShow() ? 'hide' : 'show'].call(this._uLayer);
        }
    }

    UI_PL_DIM_INPUT_TREE_CLASS.onchange = function (values) {
        var par = this.getParent();

        triggerEvent(par, 'change', null, [this.getDimId(), values]);
    }

    UI_PL_DIM_INPUT_TREE_CLASS.getDimId = function () {
        return this._sDimId;
    }

    UI_PL_DIM_INPUT_TREE_CLASS.$setText = function (text) {
        UI_PL_INPUT_TREE_CLASS.$setText.call(this, text);
        if (text.indexOf(',') >= 0) {
            this.alterClass('+primary');
        }
        else {
            this.alterClass('-primary');
        }
    }

    UI_PL_DIM_INPUT_TREE_LAYER_CLASS.$show = function () {
        var par = this.getParent(), zIndex = null;

        while (par) {
            if (par instanceof UI_FORM && par.isShow()) {
                zIndex = parseInt(getStyle(par.getOuter(), 'zIndex')) + 1;
            }
            par = par.getParent();
        }
        setStyle(this.getOuter(), 'zIndex', zIndex ? zIndex : '');
        UI_PL_INPUT_TREE_CLASS.Layer.prototype.$show.call(this);
    }
})();
