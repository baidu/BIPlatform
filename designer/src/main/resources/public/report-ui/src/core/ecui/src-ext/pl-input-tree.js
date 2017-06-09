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
        createDom = dom.create,
        children = dom.children,
        moveElements = dom.moveElements,
        getPosition  = dom.getPosition,
        inheritsControl = core.inherits,
        getView = util.getView,
        extend =util.extend,
        triggerEvent = core.triggerEvent,

        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = UI_CONTROL.prototype,
        UI_BUTTON = ui.Button,
        UI_BUTTON_CLASS = UI_BUTTON.prototype,
        UI_INPUT_CONTROL = ui.InputControl,
        UI_INPUT_CONTROL_CLASS = UI_INPUT_CONTROL.prototype,

        UI_PL_INPUT_TREE = ui.PlInputTree = 
        inheritsControl(
            UI_INPUT_CONTROL,
            'ui-input-tree',
            function (el, options) {
                options.hidden = true;
                if (options.value) {
                    options.value += '';
                }
            },
            function (el, options) {
                var o = createDom(),
                    type = this.getTypes()[0],
                    childs;
                
                if (options.value) {
                    UI_INPUT_CONTROL_CLASS.setValue.call(this, options.value);
                }

                o.innerHTML = '<div class="'+ type +'-title"></div><div class="'+ type +'-text"></div><div class="'+ type +'-button"></div>'
                    + '<div class="'+ type +'-layer" style="position:absolute;display:none"><div class="'+ UI_PL_DATA_TREE.types[0] +'"></div></div>';
                
                o.firstChild.appendChild(this._eInput);
                moveElements(el, o.lastChild.lastChild, true);
                el.appendChild(this._eInput);

                childs = children(o);

                this._eTitle = childs[0];
                this._eText = childs[1];
                this._uLayer = $fastCreate(this.Layer, childs[3], this, {asyn : options.asyn, multi: options.multi, rangeSelMode: options.rangeSelMode, maxSelected: options.maxSelected});
                this._nTextLen = options.textLen || 15;
                document.body.appendChild(childs[3]);

                if (options.title) {
                    this._eTitle.innerHTML = options.title + ':';
                }

                moveElements(o, el, true);
            }
        ),

        UI_PL_INPUT_TREE_CLASS = UI_PL_INPUT_TREE.prototype,

        UI_PL_INPUT_TREE_LAYER = UI_PL_INPUT_TREE_CLASS.Layer = 
        inheritsControl(
            UI_CONTROL,
            'ui-input-tree-layer',
            function (el, options) {
                el.style.position = 'absolute';
                this._uTree = $fastCreate(this.Tree, el.firstChild, this, {collapsed:true, asyn: options.asyn, multi: options.multi, maxSelected: options.maxSelected});
            }
        ),
        UI_PL_INPUT_TREE_LAYER_CLASS = UI_PL_INPUT_TREE_LAYER.prototype,
        
        UI_PL_DATA_TREE = ui.PlDataTree,
        
        UI_PL_INPUT_TREE_LAYER_TREE = UI_PL_INPUT_TREE_LAYER_CLASS.Tree = 
            inheritsControl(
                UI_PL_DATA_TREE,
                null,
                function (el, options) {
                    this._bAsyn = options.asyn;
                    if (options.asyn && this._aChildren.length <= 0) {
                        this.add('Loadding', null);
                        this.collapse();
                        this._bNeedAsyn = true;                        
                    }
                }
            ),
        UI_PL_INPUT_TREE_LAYER_TREE_CLASS = UI_PL_INPUT_TREE_LAYER_TREE.prototype;

    UI_PL_INPUT_TREE_CLASS.$activate = function () {
        this._uLayer.show();
    }

    UI_PL_INPUT_TREE_CLASS.init = function () {
        var value = this.getValue(),
            tree = this._uLayer._uTree;

        this.setValues(value);    
        this.setMultiSelect(tree._bMultiSelect);
    }

    UI_PL_INPUT_TREE_CLASS.$setText = function (value) {
        if (value && value.length > this._nTextLen) {
            value = '<span title="'+ value +'">' + value.substring(0, this._nTextLen) + '...' + '</span>';
        }
        this._eText.innerHTML = value;
    }

    UI_PL_INPUT_TREE_CLASS.setMultiSelect = function (flag) {
        var layer = this._uLayer,
            tree = layer._uTree;

        layer.alterClass(flag ? '+multi' : '-multi');
        tree.setMultiSelect(flag);
    }

    UI_PL_INPUT_TREE_CLASS.setValues = function (values) {
        var tree = this._uLayer._uTree;
        
        UI_INPUT_CONTROL_CLASS.setValue.call(this, values);
        tree.clearSelected();
        tree.setValues(values.split(','));
        this.$setText(tree.getSelectedText());
    }

    UI_PL_INPUT_TREE_CLASS.clear = function () {
        var tree = this._uLayer._uTree;

        tree.clearSelected();
        UI_INPUT_CONTROL_CLASS.setValue.call(this, '');
        this.$setText('');
    }

    UI_PL_INPUT_TREE_CLASS.$blur = function() {
        UI_CONTROL_CLASS.$blur.call(this);
        this._uLayer.hide();
    }

    UI_PL_INPUT_TREE_LAYER_CLASS.$hide = function () {
        var par = this.getParent(),
            tree = this._uTree,
            values = tree.getSelectedValues(),
            text = tree.getSelectedText();
        
        UI_INPUT_CONTROL_CLASS.setValue.call(par, values.join(','));
        par.$setText(text);
        UI_CONTROL_CLASS.$hide.call(this);
        triggerEvent(par, 'change', null, [values]);
    }

    UI_PL_INPUT_TREE_LAYER_CLASS.show = function () {
        var par = this.getParent(), pos, o, view;

        UI_CONTROL_CLASS.show.call(this);

        if (par) {
            pos = getPosition(par.getOuter());
            view = getView();
            o = pos.top;
            if (o + par.getHeight() + this.getHeight() > view.bottom) {
                if (o - view.top > this.getHeight()) {
                    pos.top = o - this.getHeight();
                }
            }
            else {
                pos.top = o + par.getHeight();
            }

            o = pos.left;
            if (o + this.getWidth() > view.right) {
                pos.left = o + par.getWidth() - this.getWidth();
            }
            else {
                pos.left = o;
            }
            this.setPosition(pos.left, pos.top);
            setFocused(this);
        }
    }

    UI_PL_INPUT_TREE_LAYER_TREE_CLASS.onselect = function (con, added) {
        if (!this._bMultiSelect) {
            this.getParent().hide();
        }
    }

    UI_PL_INPUT_TREE_LAYER_TREE_CLASS.onexpand = function (item) {
        var superObj = this.getParent().getParent();
        if (item._bNeedAsyn) {
            triggerEvent(superObj, 'loadtree', null, [item.getValue(), function (data) {item.load(data)}]);
            item._bNeedAsyn = false;
        }
    }

    UI_PL_INPUT_TREE_LAYER_TREE_CLASS.load = function (datasource) {
        var i, item, text;

        for (i = 0; item = this._aChildren[i]; i++) {
            disposeControl(item);
        }
        this._aChildren = [];
        this._eChildren.innerHTML = '';

        for (i = 0; item = datasource[i]; i++) {
            text = item.text;
            item = extend({asyn: this._bAsyn}, item);
            delete item.text;
            this.add(text, null, item).init();
        }
        
        if (!datasource || datasource.length <= 0) {
            this.setClass(this.getPrimary());
        }
    }
})();
