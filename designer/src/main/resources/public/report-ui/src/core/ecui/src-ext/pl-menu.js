(function () {
    var core = ecui,
        array = core.array,
        dom = core.dom,
        ui = core.ui,
        util = core.util,

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
        indexOf = array.indexOf,

        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = UI_CONTROL.prototype,
        UI_TREE_VIEW = ui.TreeView,
        UI_TREE_VIEW_CLASS = UI_TREE_VIEW.prototype;

    var UI_PL_MENU = ui.PlMenu = 
        inheritsControl(
            UI_TREE_VIEW,
            'ui-menu',
            function (el, options) { // TODO 重构下代码，不用都放到el上
                var o = first(el);
                if (o && o.tagName.toLowerCase() == 'label') {
                    extend(options, getOptions(o));
                }
                options.collapsed = true;
                options.roothide = options.roothide !== false;
                options.collapsed = options.collapsed !== false;
            },
            function (el, options) {
                var o = createDom(),
                    type = this.getTypes()[0];
                this._sUrl = options.menuUrl;
                this._sValue = options.menuId;
                this._sText = options.menuName;
                this._sPage = options.menuPage;
                (options.isNew) && el.appendChild(createDom(type + '-icon-new'));
                (options.tips) && (this.getOuter().title = el.tips);
                this._bRootHide = options.roothide;
            }
        ),

        UI_PL_MENU_CLASS = UI_PL_MENU.prototype;

    function UI_PL_MENU_FLUSH_LEVE(con) {
        var par = con.getParent(), i, item,
            topItems = con.getRoot()._aChildren;

        //默认只在第二层级下触发展开互斥
        if (par instanceof UI_PL_MENU && indexOf(topItems, con) < 0) {
            for (i = 0; item = par._aChildren[i]; i++) {
                if (item != con && !item.isCollapsed()) {
                    item.collapse();
                }
            }
        }
    }

    function UI_PL_MENU_LOAD_DATA(con, data) {
        var i, item, menu, o;
        for (i = 0; (item = data[i]) && (menu = item.menu); i++) {
            o = menu.menuName;
            menu = extend({}, menu);
            o = con.add(o, null, menu);
            if (item.children && item.children.length > 0) {
                UI_PL_MENU_LOAD_DATA(o, item.children);
            }
        }
    }

    UI_PL_MENU_CLASS.select = function (value) {
        var con;
        if (this._sValue && this._sValue == value) {
            this.getRoot().setSelected(this);
            con = this;
            while ((con = con.getParent()) && con instanceof UI_PL_MENU) {
                con.expand();
            }
        }
        else {
            for (var i = 0, item; item = this._aChildren[i]; i++) {
                item.select(value);
            }
        }
    };

    UI_PL_MENU_CLASS.expand = function () {
        UI_TREE_VIEW_CLASS.expand.call(this);
        UI_PL_MENU_FLUSH_LEVE(this);
    };

    UI_PL_MENU_CLASS.setSelected = function (node) {
        var con;
        if (this == this.getRoot()) {
            if (this._cSelected != node) {
                if (this._cSelected) {
                    this._cSelected.alterClass('-selected');
                    con = this._cSelected;
                    while((con = con.getParent()) && con instanceof UI_PL_MENU) {
                        con.alterClass('-half-selected');
                    }
                }
                if (node) {
                    node.alterClass('+selected');
                    con = node;
                    while((con = con.getParent()) && con instanceof UI_PL_MENU) {
                        con.alterClass('+half-selected');
                    }
                }
                this._cSelected = node;
            }

            if (node && this._bExpandSelected) {
                node.expand();
            }
        }
    };
    
    UI_PL_MENU_CLASS.getSelected = function () {
        if (this._cSelected) {
            return this._cSelected.$wrapItemData();
        } else {
            return null;
        }
    };

    //控制单前只有一个节点添加hover属性
    UI_PL_MENU_CLASS.$mouseover = function (event) {
        if (this.getRoot()._uTmpHover) {
            this.getRoot()._uTmpHover.alterClass('-hover');
        }
        UI_TREE_VIEW_CLASS.$mouseover.call(this);    
        //从父节点移入时去除父节点的hover
        this.getParent().alterClass('-hover');
        event.exit()
    }

    UI_PL_MENU_CLASS.$mouseout = function (event) {
        UI_TREE_VIEW_CLASS.$mouseout.call(this);    
        if (this.getRoot() != this) {
            this.getParent().alterClass('+hover');
            this.getRoot()._uTmpHover = this.getParent();
        }
    }

    UI_PL_MENU_CLASS.$click = function (event) {
        if (event.getControl() == this) {
            UI_CONTROL_CLASS.$click.call(this, event);

            if (this._aChildren.length > 0) {
                if (this._sUrl ? getMouseX(this) <= toNumber(getStyle(this.getBody(), 'paddingLeft')) : true) {
                    // 以下使用 event 代替 name
                    event.exit();
                    this[event = this.isCollapsed() ? 'expand' : 'collapse']();
                    triggerEvent(this, event);
                }
                else {
                    this.getRoot().setSelected(this);
                    if (triggerEvent(this.getRoot(), 'select', null, [this._sValue])) {
                        triggerEvent(this.getRoot(), 'change', this.$wrapItemData()); // TODO 文本内容整一下
                    }
                }
            }
            else {
                this.getRoot().setSelected(this);
                if (triggerEvent(this.getRoot(), 'select', null, [this._sValue])) {
                    triggerEvent(this.getRoot(), 'change', this.$wrapItemData()); // TODO 文本内容整一下
                }
            }
        }
    }

    UI_PL_MENU_CLASS.$wrapItemData = function () {
        return {menuId:this._sValue, menuName: this._sText, menuUrl: this._sUrl, menuPage: this._sPage}
    }
    
    UI_PL_MENU_CLASS.setData = function (data) {
        var item, i;
        if (this.getRoot() !== this) {
            return;
        }
        for (i = 0; item = this._aChildren[i]; i++) {
            disposeControl(item);
        }
        this._aChildren = [];
        this._eChildren.innerHTML = '';

        UI_PL_MENU_LOAD_DATA(this, data);
        this.init();
    }

    UI_PL_MENU_CLASS.init = function () {
        var o, el = this._eMain;

        UI_TREE_VIEW_CLASS.init.call(this);

        if (this._aChildren && this._aChildren.length > 0) {
            o = createDom(this.getPrimary() + '-icon');
            el.insertBefore(o, el.firstChild);
        }

        this.collapse();

        if (this.getRoot() == this) {
            if (this._bRootHide) {
                this.getOuter().style.display = 'none';
            }
            this.expand();
        }
    }

})();
