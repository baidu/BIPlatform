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
        createDom = dom.create,
        first = dom.first,
        insertAfter = dom.insertAfter,
        trim = string.trim,

        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = UI_CONTROL.prototype,
        UI_TREE_VIEW = ui.TreeView,
        UI_TREE_VIEW_CLASS = UI_TREE_VIEW.prototype,

        UI_PL_DATA_TREE = ui.PlDataTree = 
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

                if (options.selectable === false) {
                    el.selectable = false;
                    delete options.selectable;
                }
            },
            function (el, options) {
                var type = this.getTypes()[0],
                    o = createDom(type + '-icon');

                this._aSelected = [];
                this._sValue = options.value;
                this._bSelectAble = el.selectable !== false;
                el.selectable = undefined;
                this._bMultiSelect = options.multi === true;
                this._bRangeSelMode = options.rangeSelMode !== false; // 圈选模式，只叶子节点有意义，非叶节点的点击是为了圈选叶子节点
                this._sRangeSelStatus = 'RANGE_SEL_STATUS_EMPTY';
                this._nMaxSelected = options.maxSelected;
                this._eIcon = el.insertBefore(o, el.firstChild);
                o = createDom(type + '-close');
                this._eClose = el.appendChild(o);

                if (!this._bSelectAble) {
                    this.alterClass('+disselected');
                }
            }
        ),
        
        UI_PL_DATA_TREE_CLASS = UI_PL_DATA_TREE.prototype;

    function UI_PL_DATA_TREE_VIEW_FLUSH(control) {
        control.setClass(
            control.getPrimary() + (control._aChildren.length ? control._bCollapsed ? '-collapsed' : '-expanded' : '')
        );
    }

    UI_PL_DATA_TREE_CLASS.$setParent = function (parent) {
        var root = this.getRoot(),
            selected = root._aSelected,
            o = this.getParent(), i;

        // 如果当前节点被选中，需要先释放选中
        if (root._bRangeSelMode) {
            if (this._sRangeSelStatus != 'RANGE_SEL_STATUS_EMPTY') {
                root.$setSelected(this, 'RANGE_SEL_STATUS_EMPTY');
            }
        } else if ((i = indexOf(selected, this)) >= 0) {
            root.$setSelected(this, false);
        }

        if (this !== root) {
            remove(o._aChildren, this);
            UI_PL_DATA_TREE_VIEW_FLUSH(o);
        }

        UI_CONTROL_CLASS.$setParent.call(this, parent);

        // 将子树区域显示在主元素之后
        if (this._eChildren) {
            insertAfter(this._eChildren, this.getOuter());
        }
    }

    UI_PL_DATA_TREE_CLASS.getValue = function () {
        return this._sValue;
    }

    UI_PL_DATA_TREE_CLASS.getText = function () {
        return trim(this.getContent().replace(/<[^>]+>/g, ''));
    }

    UI_PL_DATA_TREE_CLASS.getSelected = function () {
        var res = [], root;
        if ((root = this) != this.getRoot()) {
            return null;
        }
        
        if (root._bRangeSelMode) {
            // 圈选模式下，如果某子树全选则取根节点，否则取子孙节点
            root.$preorderTravel(root, function(node) {
                if (node._sRangeSelStatus == 'RANGE_SEL_STATUS_ALL') {
                    res.push(node);   
                    return UI_PL_DATA_TREE_CLASS.STOP_SUB_TREE_TRAVEL;
                }
            });
            return res;            
        } 
        else {
            return root._aSelected.slice();
        }
    }

    UI_PL_DATA_TREE_CLASS.getSelectedValues = function () {
        var res = [], i, item, root;
        if ((root = this) != this.getRoot()) {
            return null;
        }
        if (root._bRangeSelMode) {
            // 圈选模式下，如果某子树全选则取根节点，否则取子孙节点
            root.$preorderTravel(root, function(node) {
                if (node._sRangeSelStatus == 'RANGE_SEL_STATUS_ALL') {
                    res.push(node.getValue());   
                    return UI_PL_DATA_TREE_CLASS.STOP_SUB_TREE_TRAVEL;
                }
            });            
        } 
        else {
            for (i = 0; item = root._aSelected[i]; i++) {
                res.push(item.getValue());
            }
        }
        return res; 
    }

    UI_PL_DATA_TREE_CLASS.getSelectedText = function () {
        var res = [], i, item, root;
        if ((root = this) != this.getRoot()) {
            return null;
        }
        if (root._bRangeSelMode) {
            // 圈选模式下，如果某子树全选则取根节点，否则取子孙节点
            root.$preorderTravel(root, function(node) {
                if (node._sRangeSelStatus == 'RANGE_SEL_STATUS_ALL') {
                    res.push(node.getText());   
                    return UI_PL_DATA_TREE_CLASS.STOP_SUB_TREE_TRAVEL;
                }
            });
            if(res.length == 0) {
                res.push('请选择');
            }
        } 
        else {
            for (i = 0; item = root._aSelected[i]; i++) {
                res.push(item.getText());
            }
        }
        return res.join(',') + ((dom.ieVersion && dom.ieVersion <= 7) ? '&nbsp;' : ''); // ie7全中文字bug
    }

    UI_PL_DATA_TREE_CLASS.setMultiSelect = function (flag) {
        if (this == this.getRoot()) {
            this._bMultiSelect = flag;
        }
    }

    UI_PL_DATA_TREE_CLASS.isMultiSelect = function () {
        return this._bMultiSelect === true;
    }
    
    UI_PL_DATA_TREE_CLASS.isLeaf = function () {
        return !this._aChildren || !this._aChildren.length;
    }

    UI_PL_DATA_TREE_CLASS.setValues = function (values) {
        var item, status;
        
        if (indexOf(values, this._sValue) >= 0) {
            if (this.getRoot()._bRangeSelMode) {
                this.getRoot().$setRangeSelSelected(this, 'RANGE_SEL_STATUS_ALL');
            } else {
                this.getRoot().$setSelected(this, true);
            }
            
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

    UI_PL_DATA_TREE_CLASS.init = function () {
        UI_TREE_VIEW_CLASS.init.call(this);
    }

    UI_PL_DATA_TREE_CLASS.load = function (datasource) {
        var i, item, text;

        for (i = 0; item = this._aChildren[i]; i++) {
            disposeControl(item);
        }
        this._aChildren = [];
        this._eChildren.innerHTML = '';

        for (i = 0; item = datasource[i]; i++) {
            text = item.text;
            item = extend({}, item);
            delete item.text;
            this.add(text, null, item).init();
        }
    }

    UI_PL_DATA_TREE_CLASS.$click = function (event) {
        var added = null;
        if (event.getControl() == this) {
            UI_CONTROL_CLASS.$click.call(this, event);

            if (getMouseX(this) <= toNumber(getStyle(this.getBody(), 'paddingLeft'))) {
                // 以下使用 event 代替 name
                this[event = this.isCollapsed() ? 'expand' : 'collapse']();
                triggerEvent(this.getRoot(), event, null, [this]);
            }
            else if (this.getRoot()._bRangeSelMode) {
                this.getRoot().setSelected(this);
            }
            else {
                if (indexOf(this.getRoot()._aSelected, this) >= 0) {
                    if (this.getRoot()._bMultiSelect) {
                        added = false;    
                    }
                }
                else {
                    added = true;
                }
                this.getRoot().setSelected(this);
                triggerEvent(this.getRoot(), 'select', null, [this, added == true])
                if (added !== null) {
                    triggerEvent(this.getRoot(), 'change', null, [this, added]);
                }
            }
        }
    }
    
    UI_PL_DATA_TREE_CLASS.$mouseover = function (event) {
        if (!this.getRoot()._bRangeSelMode) {
            UI_CONTROL_CLASS.$mouseover.call(this, event);
            return;
        }
        // 禁用hover改变和事件冒泡，在tree中用mousemove做此事
        event.exit();
    }
    
    UI_PL_DATA_TREE_CLASS.$mousemove = function (event) {
        var root = this.getRoot();
        
        if (!root._bRangeSelMode) {
            UI_CONTROL_CLASS.$mousemove.call(this, event);
            return;   
        }
        // 是否显示hovered状态
        if (root._uHovered != this) {
            if (root._uHovered) {
                root.$hoverSubTree(root._uHovered, false);
            }
            root._uHovered = this;
            root.$hoverSubTree(root._uHovered, true);
        }
        event.exit();
    }
    
    UI_PL_DATA_TREE_CLASS.$mouseout = function (event) {
        var root = this.getRoot();
        
        if (!root._bRangeSelMode) {
            UI_CONTROL_CLASS.$mousemove.call(this, event);
            return;   
        }
        if (root == this) {
            this.$hoverSubTree(this._uHovered, false);
            this._uHovered = null;
        }
    }

    UI_PL_DATA_TREE_CLASS.$hoverSubTree = function (node, flag) {
        if (this != this.getRoot()) {
            return;
        }
        flag = flag ? '+hover' : '-hover'; 
        node.alterClass(flag);
        if(this.getRoot()._bRangeSelMode) {
            this.$preorderTravel(node, function(node) {
                node.alterClass(flag);
            });
        }
    }
    
    UI_PL_DATA_TREE_CLASS.setSelectAble = function (enable) {
        var root = this.getRoot();

        if (!this.enable) {
            if (root._bRangeSelMode) {
                if (this._sRangeSelStatus != 'RANGE_SEL_STATUS_EMPTY') {
                    root.$setSelected(this, 'RANGE_SEL_STATUS_EMPTY');
                }
            }
            else if (indexOf(root._aSelected, this) >= 0) {
                root.$setSelected(this, false);
            }
        }
        this._bSelectAble = enable;
    }

    UI_PL_DATA_TREE_CLASS.$setSelected = function (node, flag) {
        var selected, i;
        
        if (this != this.getRoot()) {
            return;
        }
        
        if (this._bRangeSelMode) {
            if (flag && node._sRangeSelStatus != flag) {
                if (node._sRangeSelStatus == 'RANGE_SEL_STATUS_ALL') {
                    node.alterClass('-selected');
                } else if (node._sRangeSelStatus == 'RANGE_SEL_STATUS_HALF') {
                    node.alterClass('-half');   
                }
                node._sRangeSelStatus = flag;
                if (flag == 'RANGE_SEL_STATUS_ALL') {
                    node.alterClass('+selected');   
                } else if (flag == 'RANGE_SEL_STATUS_HALF') {
                    node.alterClass('+half');
                }
            }
        } 
        else {
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

    UI_PL_DATA_TREE_CLASS.clearSelected = function () {
        var selected, i, item;
        if (this != this.getRoot()) {
            return;
        }
        
        if(this.getRoot()._bRangeSelMode) {
            this.$preorderTravel(this, function(node) {
                node.getRoot().$setSelected(node, 'RANGE_SEL_STATUS_EMPTY');
            });
            
        } else {
            selected = this._aSelected;
            while(item = selected[0]) {
                this.$setSelected(item, false);
            }
        }

    }

    UI_PL_DATA_TREE_CLASS.$setRangeSelSelected = function (node, status) {
        var o;
        
        if (this == this.getRoot() && this._bRangeSelMode) {
            this.$preorderTravel(node, function (node) {
                node.getRoot().$setSelected(node, status);
            });
            // 祖先改变状态
            o = node;
            while(o = o.getParent()) {
                status = this.$analysisRangeSelStatus(o);
                this.$setSelected(o, status);
            }
        }        
    }
    
    UI_PL_DATA_TREE_CLASS.setSelected = function (node, force) {
        var selected, i, status;
        if (this != this.getRoot()) {
            return;
        }
        
        if (this._bRangeSelMode) { // 圈选模式
            if (node._sRangeSelStatus != 'RANGE_SEL_STATUS_ALL') {
                status = 'RANGE_SEL_STATUS_ALL'; // 子树全选
            } else {
                status = 'RANGE_SEL_STATUS_EMPTY'; // 子树空选
            }
            this.$setRangeSelSelected(node, status);
        }
        
        else if (node._bSelectAble) {
            selected = this._aSelected;                    
            i = indexOf(selected, this);
            if ((i = indexOf(selected, node)) >= 0) {
                if (!force && this._bMultiSelect && selected.length > 1) {
                    this.$setSelected(node, false);
                }
            }
            else {
                if (!this._bMultiSelect && selected.length >= 1) {
                    this.$setSelected(selected[0], false);
                }
                if (!this._nMaxSelected || !this._bMultiSelect || selected.length < this._nMaxSelected) {
                    this.$setSelected(node, true);
                }
            }

            if (node && this._bExpandSelected) {
                node.expand();
            }
        }
    }
    
    UI_PL_DATA_TREE_CLASS.$analysisRangeSelStatus = function (node) {
        var i, o, children, all = true, empty = true;
        
        if (this.getRoot()._bRangeSelMode) {
            for (i = 0, children = node._aChildren || []; o = children[i]; i++) {
                if (o._sRangeSelStatus != 'RANGE_SEL_STATUS_ALL') {
                    all = false;
                }
                if (o._sRangeSelStatus != 'RANGE_SEL_STATUS_EMPTY') {
                    empty = false;
                }
            }
            return all ? 'RANGE_SEL_STATUS_ALL' : 
                (empty ? 'RANGE_SEL_STATUS_EMPTY' : 'RANGE_SEL_STATUS_HALF');
        } else { 
            return false;
        }
    }
    
    UI_PL_DATA_TREE_CLASS.STOP_ALL_TRAVEL = 1; // 如果callback返回此值，则停止所有遍历
    
    UI_PL_DATA_TREE_CLASS.STOP_SUB_TREE_TRAVEL = 2; // 如果callback返回此值，则停止遍历本子树
    
    UI_PL_DATA_TREE_CLASS.$preorderTravel = function (travelRoot, callback) {
        var i, node, children, root, conti;
        
        if ((root = this) != this.getRoot() || travelRoot == null) {
            return;   
        }
        
        conti = callback.call(root, travelRoot);
        if (conti === UI_PL_DATA_TREE_CLASS.STOP_ALL_TRAVEL) { return conti; }
        if (conti === UI_PL_DATA_TREE_CLASS.STOP_SUB_TREE_TRAVEL) { return; }
        
        for (i = 0, children = travelRoot._aChildren || []; node = children[i]; i++) {
            conti = root.$preorderTravel(node, callback);
            if (conti === UI_PL_DATA_TREE_CLASS.STOP_ALL_TRAVEL) { return conti; }
        }
    }
    
    UI_PL_DATA_TREE_CLASS.$leafTravel = function (travelRoot, callback) {
        var root;
        if ((root = this) != this.getRoot()) {
            return;
        }
        root.$preorderTravel(travelRoot, function(node) {
            if(node.isLeaf() && node._sRangeSelStatus == 'RANGE_SEL_STATUS_ALL') {
                callback.call(root, node);
            }
        });
    }
    
})();
