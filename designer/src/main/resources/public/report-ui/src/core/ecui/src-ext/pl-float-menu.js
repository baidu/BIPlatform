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
        getPosition = dom.getPosition,
        setStyle = dom.setStyle,
        addClass = dom.addClass,
        ieVersion = dom.ieVersion,
        toNumber = util.toNumber,
        extend = util.extend,
        blank = util.blank,
        unionBoundBox = xutil.graphic.unionBoundBox,
        indexOf = array.indexOf,
        trim = string.trim,

        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = UI_CONTROL.prototype,
        UI_TREE_VIEW = ui.TreeView,
        UI_TREE_VIEW_CLASS = UI_TREE_VIEW.prototype;

    var UI_PL_FLOAT_MENU = ui.PlFloatMenu = 
        inheritsControl(
            UI_TREE_VIEW,
            'ui-float-menu',
            null,
            function (el, options) {
                var o, type = this.getTypes()[0];
                
                if (options.url) {
                    this._sUrl = options.url;
                    delete options.url;
                    addClass(el, type + '-pointer');    
                }
                
                if (options.value) {
                    this._sValue = options.value;
                    delete options.value;
                }
                if (options.text) {
                    this._sText = options.text;
                }
                if (options.isNew) {
                    el.appendChild(createDom(type + '-icon-new'));
                    delete options.isNew;
                }
                if (options.prompt) {
                    el.setAttribute('title', options.prompt);
                }
                if (options.floatTree) {
                    this._oFloaterDatasource = options.floatTree;
                    el.appendChild(createDom(type + '-icon-arror'));
                    delete options.floatTree;
                }
                options.collapsed = false;
                this._bRootHide = true;
                
                if (!options.notRoot) { // mold fake and floater
                    document.body.appendChild(o = createDom('ui-float-menu-floater', 'display:none'));
                    this._uFloater = $fastCreate(this.Floater, o, this, {});
                    options.notRoot = true;
                }
            }
        ),
        UI_PL_FLOAT_MENU_CLASS = UI_PL_FLOAT_MENU.prototype,
        
        // 浮层
        UI_PL_FLOAT_MENU_FLOATER = UI_PL_FLOAT_MENU_CLASS.Floater = 
            inheritsControl(
                UI_CONTROL,
                'ui-float-menu-floater',
                null,
                function (el, options) {
                    var type = this.getTypes()[0];
                    this._aLineList = [];
                }
            ),
        UI_PL_FLOAT_MENU_FLOATER_CLASS = UI_PL_FLOAT_MENU_FLOATER.prototype,
        
        // 浮层行
        UI_PL_FLOAT_MENU_FLOATER_LINE_CLASS = (UI_PL_FLOAT_MENU_FLOATER_CLASS.Line = (
            inheritsControl(UI_CONTROL, 'ui-float-menu-floater-line')
        )).prototype;
        


    function UI_PL_FLOAT_MENU_LOAD_DATA(con, data) {
        var i, item, o;
        for (i = 0; item = data[i]; i++) {
            o = item.text; 
            item = extend({}, item);
            item.notRoot = true;
            o = con.add(o, null, item);
            if (item.children && item.children.length > 0) {
                UI_PL_FLOAT_MENU_LOAD_DATA(o, item.children);
            }
        }
    }
    
    /**
     * 设置当前选中
     * @param {String} value 当前选中值，例如：1101:1119:21, 第一个“:”后为floater的当前选中value。如果为null则清空
     */
    UI_PL_FLOAT_MENU_CLASS.select = function (value) {
        var con, o, i, item, menuValue, floaterValue, root = this.getRoot();
        
        if (value === null) {
            root.setSelected(null);
            return;
        }
        
        o = (value = String(value)).indexOf(':');
        if (o >=0 ) {
            menuValue = value.slice(0, o);
            floaterValue = value.slice(o + 1, value.length) || null;
        } else {
            menuValue = value;   
        }
        
        if (this._sValue && this._sValue == menuValue) {
            root.setSelected(this);
            con = this;
            while ((con = con.getParent()) && con instanceof UI_PL_FLOAT_MENU) {
                con.expand();
            }
            root._sFloaterValue = floaterValue;
            root._uFloater.select(floaterValue);
        }
        else {
            for (i = 0; item = this._aChildren[i]; i++) {
                item.select(value);
            }
        }
    }

    UI_PL_FLOAT_MENU_CLASS.setSelected = function (node) {
        var con;
        if (this == this.getRoot()) {
            if (this._cSelected != node) {
                if (this._cSelected) {
                    this._cSelected.alterClass('-selected');
                    con = this._cSelected;
                    while((con = con.getParent()) && con instanceof UI_PL_FLOAT_MENU) {
                        con.alterClass('-half-selected');
                    }
                }
                if (node) {
                    node.alterClass('+selected');
                    con = node;
                    while((con = con.getParent()) && con instanceof UI_PL_FLOAT_MENU) {
                        con.alterClass('+half-selected');
                    }
                }
                this._cSelected = node;
            }

            if (node && this._bExpandSelected) {
                node.expand();
            }
        }
    }

    UI_PL_FLOAT_MENU_CLASS.getSelected = function () {
        if (this._cSelected) {
            return this._cSelected.$wrapItemData();
        } 
        else {
            return null;
        }
    }    
    
    UI_PL_FLOAT_MENU_CLASS.getBoundBox = function () {
        return unionBoundBox.apply(this, this.$getAllBounds());
    }
    
    UI_PL_FLOAT_MENU_CLASS.$getAllBounds = function (bounds) {
        var i, node, bound;
        bounds = bounds || [];
        if (this.isShow()) {
            bound = getPosition(this.getOuter());
            bound.width = this.getWidth();
            bound.height = this.getHeight();
            bounds.push(bound);
        }
        if (this._aChildren) {
            for (i = 0; node = this._aChildren[i]; i++) {
                bounds = node.$getAllBounds(bounds);
            }   
        }
        return bounds;
    }
    
    UI_PL_FLOAT_MENU_CLASS.setData = function (data) {
        var item, i;

        // 清空选中
        this.select(null);

        if (this.getRoot() !== this) {
            return;
        }
        for (i = 0; item = this._aChildren[i]; i++) {
            disposeControl(item);
        }
        this._aChildren = [];
        this._eChildren.innerHTML = '';

        UI_PL_FLOAT_MENU_LOAD_DATA(this, data);
        this.init();
    }

    UI_PL_FLOAT_MENU_CLASS.init = function () {
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

    UI_PL_FLOAT_MENU_CLASS.$wrapItemData = function () {
        return {
            menuId: this._sValue, 
            menuName: this._sText, 
            menuUrl: this._sUrl
        };
    }    

    UI_PL_FLOAT_MENU_CLASS.$floaterselect = function (data, hoveredCon) {
        if (this == this.getRoot()) {
            this.setSelected(hoveredCon || null);
            this._uFloater.hide();
            this._sFloaterValue = data.value;

            triggerEvent(
                this, 
                'change',
                null, 
                [
                    {
                        menuId: data.value,
                        menuName: data.text,
                        menuUrl: data.url
                    }
                ]
            )
        }
    }

    UI_PL_FLOAT_MENU_CLASS.$mousemove = function (event) {
        var root = this.getRoot(), floater = root._uFloater;
        if (this == root) { return; }
        
        // 是否显示hovered状态
        if (root._uHoveredMenu != this) {
            root._uHoveredMenu && root._uHoveredMenu.alterClass('-hover');
            this.alterClass('+hover');
            root._uHoveredMenu = this;
        }
        
        // 检测是否显示floater, 考虑效率。
        // tree-view加floater的实现和mouseover和mouseout比较不合，比较难用来做这件事。
        if (floater.getMenuShowMe() != this) {
            if (this._oFloaterDatasource) {
                floater.setData(this, this._oFloaterDatasource, root._sFloaterValue);
                floater.show(this);
            } else {
                floater.getMenuShowMe() && floater.hide();
            }
        }
        event.exit();
    }

    UI_PL_FLOAT_MENU_CLASS.$mouseover = function (event) {
        // 禁用hover改变和事件冒泡
        event.exit();
    }
    
    UI_PL_FLOAT_MENU_CLASS.$mouseout = function (event) {
        if (this.getRoot() == this) {
            if (this.getRoot()._uHoveredMenu) {
                this.getRoot()._uHoveredMenu.alterClass('-hover');
                this.getRoot()._uHoveredMenu = null;
            }
            if (this.getRoot()._uFloater) {
                this.getRoot()._uFloater.hide();
            }
        }
    }
    
    UI_PL_FLOAT_MENU_CLASS.$click = function (event) {
        if (event.getControl() == this) {
            UI_CONTROL_CLASS.$click.call(this, event);

            var root = this.getRoot();
            var doExpand = false;

            if (this._aChildren.length > 0
                && (!this._sUrl
                    || getMouseX(this) 
                        <= toNumber(getStyle(this.getBody(), 'paddingLeft'))
                )
            ) {
                doExpand = true;
                event.exit();
                this[event = this.isCollapsed() ? 'expand' : 'collapse']();
                triggerEvent(this, event);
            }
            
            if (!doExpand && this._sUrl) {
                root.setSelected(this);
                triggerEvent(root, 'change', this.$wrapItemData());
            }
        }        
    }
        
        
        
    ///////////////////////////////////////////////////
    // UI_PL_FLOAT_MENU_FLOATER
    
    UI_PL_FLOAT_MENU_FLOATER_CLASS.setData = function (menuHovered, datasource, floaterValue) {
        var i, item, o, lineType, datasource = datasource || [];
        
        this.hide();
        this.clear();
        
        this._uMenuHovered = menuHovered;
        // 创建floaterLine
        for (i = 0; item = datasource[i]; i++) {
            lineType = item.lineType || UI_PL_FLOAT_MENU_FLOATER_LINE_CLASS.DEFAULT_KEY;
            this.getBody().appendChild(o = createDom('ui-float-menu-floater-line'));
            o = UI_PL_FLOAT_MENU_FLOATER_LINE_CLASS.create(lineType, o, this, {datasource: item});
            this._aLineList.push(o);
            if (i < datasource.length - 1) {
                o.alterClass('+separater');   
            }
        }
        
        this.select(floaterValue);
    }
    
    UI_PL_FLOAT_MENU_FLOATER_CLASS.getMenuShowMe = function () {
        return this._uMenuShowMe;
    }
    
    UI_PL_FLOAT_MENU_FLOATER_CLASS.show = function (menuShowMe) {
        this._uMenuShowMe = menuShowMe;
        UI_CONTROL_CLASS.show.call(this);
        this.$layout();
    }
    
    UI_PL_FLOAT_MENU_FLOATER_CLASS.hide = function () {
        this._uMenuShowMe = null;
        UI_CONTROL_CLASS.hide.call(this);
    }
    
    
    UI_PL_FLOAT_MENU_FLOATER_CLASS.$layout = function () {
        var left, top, 
            menuCon = this.getParent(),
            menuHovered = this._uMenuHovered,
            menuBoundBox = menuCon.getBoundBox(),
            hoveredPos = getPosition(menuHovered.getOuter()),
            hoveredWidth = menuHovered.getWidth(),
            thisHeight = this.getHeight();
            
        left = hoveredPos.left + hoveredWidth;
        top = hoveredPos.top - 2;
        /* if (top + thisHeight > menuBoundBox.top + menuBoundBox.height) {
            top = menuBoundBox.top + menuBoundBox.height - thisHeight;   
        }
        if (top < menuBoundBox.top) {
            top = menuBoundBox.top;
        }*/
        this.setPosition(left - (ieVersion ? 1 : 2), top);
    }
    
    /**
     * 设置选中
     * @param {String} value 例如 1:22
     */
    UI_PL_FLOAT_MENU_FLOATER_CLASS.select = function (value) {
        var i, lineCon;
        for (i = 0; lineCon = this._aLineList[i]; i++) {
            lineCon.select(value);
        }
    }
    
    /**
     * 清空floater
     */
    UI_PL_FLOAT_MENU_FLOATER_CLASS.clear = function () {
        var i, lineCon;
        for (i = 0; lineCon = this._aLineList[i]; i++) {
            disposeControl(lineCon);
        }
        this._aLineList = [];
        this._uMenuHovered = null;
        this.getBody().innerHTML = '';
    }
    
    UI_PL_FLOAT_MENU_FLOATER_CLASS.$dispose = function () {
        this.getBody().innerHTML = '';
        UI_CONTROL_CLASS.$dispose.call(this);
    }    
    
    /**
     * Event handler of "floaterlineselect"
     */
    UI_PL_FLOAT_MENU_FLOATER_CLASS.$floaterlineselect = function (floaterLineCon, data) {
        var i, floaterLine;
        // 清空其他floaterLine的选中
        for (i = 0; floaterLine = this._aLineList[i]; i++) {
            if (floaterLine !== floaterLineCon) {
                floaterLine.select(null);   
            }
        }
        triggerEvent(this.getParent(), 'floaterselect', null, [data, this._uMenuHovered]);
    }
  
    ///////////////////////////////////////////////////
    // UI_PL_FLOAT_MENU_FLOATER 
    
    /**
     * Line control class factory
     */
    UI_PL_FLOAT_MENU_FLOATER_LINE_CLASS.lineControlTypeSet = {};
    UI_PL_FLOAT_MENU_FLOATER_LINE_CLASS.DEFAULT_KEY = '';
    
    UI_PL_FLOAT_MENU_FLOATER_LINE_CLASS.registerLineControlType = function (key, controlType) {
        UI_PL_FLOAT_MENU_FLOATER_LINE_CLASS.lineControlTypeSet[key] = controlType;
    }
    
    UI_PL_FLOAT_MENU_FLOATER_LINE_CLASS.create = function (key, el, parent, options) {
        return $fastCreate(UI_PL_FLOAT_MENU_FLOATER_LINE_CLASS.lineControlTypeSet[key], el, parent, options);
    }
    
    /**
     * @param {String} value 约定：如果为null，表示清空选择
     */
    UI_PL_FLOAT_MENU_FLOATER_LINE_CLASS.select = function (value) {} // blank
    
    UI_PL_FLOAT_MENU_FLOATER_LINE_CLASS.$dispose = function () {
        this.getBody().innerHTML = '';
        UI_CONTROL_CLASS.$dispose.call(this);
    }    
    
})();
