/**
 * data tree
 * Copyright 2012 Baidu Inc. All rights reserved.
 * 
 * path:    data-tree.js
 * desc:    数据树
 *          在普通树控件的基础上进行扩展
 * author:  cxl(chenxinle)
 * date:    2012/03/12
 */
(function () {
    var core = ecui,
        array = core.array,
        ui = core.ui,
        array = core.array,
        dom = core.dom,
        string = core.string,
        util = core.util,

        $fastCreate = core.$fastCreate,
        getMouseX = core.getMouseX,
        inheritsControl = core.inherits,
        getOptions = core.getOptions,
        disposeControl = core.dispose,
        triggerEvent = core.triggerEvent,
        extend = util.extend,
        indexOf = array.indexOf,
        extend = util.extend,
        toNumber = util.toNumber,
        getStyle = dom.getStyle,
        first = dom.first,
        insertAfter = dom.insertAfter,
        trim = string.trim,
        blank = util.blank,

        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = UI_CONTROL.prototype,
        UI_TREE_VIEW = ui.TreeView,
        UI_TREE_VIEW_CLASS = UI_TREE_VIEW.prototype,

        UI_DATA_TREE = ui.DataTree = 
        inheritsControl(
            UI_TREE_VIEW,
            'ui-data-tree',
            function (el, options) {
                options.expandSelected = options.expandSelected === true;

                if (first(el) && 'divlabel'.indexOf(first(el).tagName.toLowerCase()) >= 0) {
                    extend(options, getOptions(first(el)));
                }

                if (options.value) {
                    options.value += '';
                }

                options.resizable = false;
            },
            function (el, options) {
                this._aSelected = [];
                this._sValue = options.value;
                this._bHideRoot = options.hideRoot === true; //是否隐藏根节点
                this._bSelectAble = options.selectable !== false;
                this._bMultiSelect = options.multi === true;
                this._bAsyn = options.asyn;
                this._bIsLeaf = options.isLeaf;
                if (options.asyn && !options.isLeaf && this._aChildren.length <= 0) {
                    this.add('Loadding', null);
                    this.collapse();
                    this._bNeedAsyn = true;                        
                }
            }
        ),
        
        UI_DATA_TREE_CLASS = UI_DATA_TREE.prototype;

    function UI_DATA_TREE_VIEW_FLUSH(control) {
        control.setClass(
            control.getPrimary() + (control._aChildren.length ? control._bCollapsed ? '-collapsed' : '-expanded' : '')
        );
    }

    UI_DATA_TREE_CLASS.init = function () {
        UI_TREE_VIEW_CLASS.init.call(this);

        if (this._bHideRoot && this == this.getRoot()) {
            this.hide();
            this.expand();
        }
    }

    UI_DATA_TREE_CLASS.$setParent = function (parent) {
        var root = this.getRoot(),
            selected = root._aSelected,
            o = this.getParent(), i;

        // 如果当前节点被选中，需要先释放选中
        if ((i = indexOf(selected, this)) >= 0) {
            root.$setSelected(this, false);
        }

        if (this !== root) {
            remove(o._aChildren, this);
            UI_DATA_TREE_VIEW_FLUSH(o);
        }

        UI_CONTROL_CLASS.$setParent.call(this, parent);

        // 将子树区域显示在主元素之后
        if (this._eChildren) {
            insertAfter(this._eChildren, this.getOuter());
        }
    }

    UI_DATA_TREE_CLASS.getValue = function () {
        return this._sValue;
    }

    UI_DATA_TREE_CLASS.getText = function () {
        return trim(this.getContent().replace(/<[^>]+>/g, ''));
    }

    UI_DATA_TREE_CLASS.getSelected = function () {
        if (this == this.getRoot()) {
            return this._aSelected.slice();
        }
    }

    UI_DATA_TREE_CLASS.getSelectedValues = function () {
        var res = [], i, item;
        if (this == this.getRoot()) {
            for (i = 0; item = this._aSelected[i]; i++) {
                res.push(item.getValue());
            }
            return this._bMultiSelect ? res : res[0];
        }
    }

    UI_DATA_TREE_CLASS.setValues = function (values) {
        var item;
        if (indexOf(values, this._sValue) >= 0) {
            this.getRoot().$setSelected(this, true);
            item = this;
            while((item = item.getParent()) && item instanceof UI_TREE_VIEW) {
                if (item.isCollapsed()) {
                    item.expand()
                }
            }
        }
        for (var i = 0, item; item = this._aChildren[i]; i++) {
            item.setValues(values);
        }
    }

    UI_DATA_TREE_CLASS.getItemByValue = function (value) {
        var res = null;

        if (this._sValue == value) {
            res = this;
        }
        for (var i = 0, item; (item = this._aChildren[i]) && res == null; i++) {
            res = item.getItemByValue(value);
        }
        return res;
    }

    UI_DATA_TREE_CLASS.load = function (datasource) {
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
    }

    UI_DATA_TREE_CLASS.$expand = function (item) {
        var superObj = item.getRoot();
        if (item._bNeedAsyn) {
            triggerEvent(superObj, 'load', null, [item.getValue(), function (data) {item.load(data)}]);
            item._bNeedAsyn = false;
        }
    }

    UI_DATA_TREE_CLASS.$click = function (event) {
        if (event.getControl() == this) {
            UI_CONTROL_CLASS.$click.call(this, event);

            if (getMouseX(this) <= toNumber(getStyle(this.getBody(), 'paddingLeft'))) {
                this.clickEC();
            }
            else {
                this.clickItem();
            }
        }
    }

    // 为QA自动化测试而暴露的api
    UI_DATA_TREE_CLASS.clickEC = function () {
        var e;
        this[e = this.isCollapsed() ? 'expand' : 'collapse']();
        triggerEvent(this.getRoot(), e, null, [this]);
    }

    // 为QA自动化测试而暴露的api
    UI_DATA_TREE_CLASS.clickItem = function () {
        var added = null;
        if (indexOf(this.getRoot()._aSelected, this) >= 0) {
            if (this._bMultiSelect) {
                added = false;    
            }
        }
        else {
            added = true;
        }
        this.getRoot().setSelected(this);
        triggerEvent(this.getRoot(), 'select', null, [this, added == true])
        if (added !== null) {
            triggerEvent(this.getRoot(), 'change', null, [this.getValue(), added]);
        }
    }

    UI_DATA_TREE_CLASS.getSelectedText = function () {
        var res = [], i, item;
        if (this == this.getRoot()) {
            for (i = 0; item = this._aSelected[i]; i++) {
                res.push(item.getText());
            }
            return res.join(',');
        }
    }

    UI_DATA_TREE_CLASS.setSelectAble = function (enable) {
        var root = this.getRoot(), i;

        if (!this.enable && (i = indexOf(root._aSelected, this)) >= 0) {
            root.$setSelected(this, false);
        }
        this._bSelectAble = enable;
    }

    UI_DATA_TREE_CLASS.$setSelected = function (node, flag) {
        var selected, i;
        if (this == this.getRoot()) {
            selected = this._aSelected;
            i = indexOf(selected, node);
            if (flag === true) {
                if (i < 0) {
                    selected.push(node);
                    node.alterClass('+selected');
                }
            }
            else if (flag === false) {
                if (i >= 0) {
                    selected.splice(i, 1);
                    node.alterClass('-selected');
                }
            }
        }
    }

    UI_DATA_TREE_CLASS.clearSelected = function () {
        var selected, i, item;
        
        if (this == this.getRoot()) {
            selected = this._aSelected;
            while(item = selected[0]) {
                this.$setSelected(item, false);
            }
        }
    }

    UI_DATA_TREE_CLASS.setSelected = function (node, force) {
        var selected, i;

        if (this == this.getRoot() && node._bSelectAble) {
            selected = this._aSelected;                    
            i = indexOf(selected, this);
            if ((i = indexOf(selected, node)) >= 0) {
                if (!force && this._bMultiSelect) {
                    this.$setSelected(node, false);
                }
            }
            else {
                if (!this._bMultiSelect && selected.length >= 1) {
                    this.$setSelected(selected[0], false);
                }
                this.$setSelected(node, true);
            }

            if (node && this._bExpandSelected) {
                node.expand();
            }
        }
    };

    UI_DATA_TREE_CLASS.$setSize = blank;
})();
