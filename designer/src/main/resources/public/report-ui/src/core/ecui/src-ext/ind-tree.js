/**
 * @file 基于ecui.ui.TreeView实现的树视图。
 * @author hades(denghongqi)
 */
(function() {
    var core = ecui;
    var ui = core.ui;
    var dom = core.dom;
    var util = core.util;

    var WINDOW = window;
    var DOCUMENT = document;

    var UI_CONTROL = ui.Control;
    var UI_CONTROL_CLASS = UI_CONTROL.prototype;
    var UI_ITEMS = ui.Items;
    var UI_ITEM = ui.Item;
    var UI_ITEM_CLASS = UI_ITEM.prototype;

    ui.IndTree = core.inherits(
        ui.Control,
        'ui-indtree',
        function(el, options) {
            this._oOptions = options;
        },
        function(el, options) {
            this._bExpandSelected = options.expandSelected !== false;
            if (!dom.first(el)) {
                return el;
            }

            var o = dom.create(
                'ui-indtree-pop', 
                'display: none; position: absolute; z-index: 32767', 
                'div'
            );
            DOCUMENT.body.appendChild(o);
            this._cPop = core.$fastCreate(this.Pop, o, this, {});

            this.root = createNodeCon(dom.first(el), this, true);

            var o = dom.create('ui-indtree-all');
            dom.insertBefore(o, dom.first(el));
            this._cAll = core.$fastCreate(
                this.All, 
                o, 
                this, 
                {
                    level : getMaxLevel(this.root),
                    data : this._oLevalData
                }
            );

            flushNodeState(this.root, this._bExpandSelected);

            var list = this._cAll.getItems();
            var i;
            for (i = 0; i < list.length; i++) {
                if (list[i]._bChecked) {
                    setLevelSelected(this.root, list[i]._nLevel);
                }
            }
        }
    );

    var UI_IND_TREE = ui.IndTree;
    var UI_IND_TREE_CLASS = UI_IND_TREE.prototype;

    //禁用$setSize
    UI_IND_TREE_CLASS.$setSize = util.blank;

    /**
     * 获取选中的节点
     * @public
     * @return {Array}
     */
    UI_IND_TREE_CLASS.getSelected = function(opt_control) {
        var control = opt_control || this.root;
        var res = [];
        if (control.isSelect()) {
            res.push(control.getValue());
        }
        if (control._aChildren && control._aChildren.length) {
            var i;
            for (i = 0; i < control._aChildren.length; i++) {
                res = res.concat(this.getSelected(control._aChildren[i]))
            }
        }
        return res;
    };

    /**
     * 获取被勾选的层级全选checkbox
     * @public
     * @return {Array} 被勾选的checkbox的value集合
     */
    UI_IND_TREE_CLASS.getLevelSelected = function() {
        var all = this._cAll;
        var list = all.getItems();
        var res = [];
        var i;
        for (i = 0; i < list.length; i++) {
            if (list[i].isSelect()) {
                res.push(list[i].getValue());
            }
        }
        return res;
    };

    /**
     * 节点控件
     */
    UI_IND_TREE_CLASS.Node = core.inherits(
        ui.Control,
        'ui-indtree-node',
        function(el, options) {
            options.userSelect = false;
            el.style.position = 'relative';
            var o = dom.create('ui-indtree-node-text', '', 'div');
            dom.moveElements(el, o, true);
            el.appendChild(o);

            /*
            if (!options.selectAllBtn) {
                var o = dom.create('ui-indtree-node-btn', '', 'div');
                o.appendChild(dom.create('', '', 'b'));
                el.appendChild(o);
            }
            */
        },
        function(el, options) {
            this._sValue = options.value || '';
            this._bSelected = options.selected || false;
            this._nLevel = options.level;
            options.selectAllBtn && (this._bSelectAllBtn = options.selectAllBtn);
            /*
            if (!this._bSelectAllBtn) {
                this._cPopBtn = core.$fastCreate(
                    this.PopBtn,
                    dom.last(el),
                    this,
                    {}
                );
            }
            */
        }
    );
    var UI_IND_TREE_NODE = UI_IND_TREE_CLASS.Node;
    var UI_IND_TREE_NODE_CLASS = UI_IND_TREE_NODE.prototype;

    /**
     * 收缩子树
     * @private
     */
    UI_IND_TREE_NODE_CLASS.$collapse = function() {
        setNodeCollapse(this, true);
    }

    /**
     * 展开子树
     * @private
     */
    UI_IND_TREE_NODE_CLASS.$expand = function() {
        setNodeCollapse(this, false);
    };

    /**
     * 控件点击时改变控件的选中状态，并控制子树的现实/隐藏
     * @override
     */
    UI_IND_TREE_NODE_CLASS.$click = function(event) {
        if (this._bSelected) {
            setNodeSelected(this, false);
            var all = this._cTopPar._cAll;
            var i;
            var list = all.getItems();
            for (i = 0; i < list.length; i++) {
                if (list[i]._nLevel == this._nLevel) {
                    list[i].setChecked(false);
                }
            }
        }
        else {
            setNodeSelected(this, true);
        }

        event.stopPropagation();
    }

    /**
     * 控件mouseover的时候触发
     * @override
     */
    UI_IND_TREE_NODE_CLASS.$mouseover = function(event) {
        UI_CONTROL_CLASS.$mouseover.call(this);
        event.stopPropagation()
    };

    /**
     * 控件mouseout的时候触发
     * @override
     */
    UI_IND_TREE_NODE_CLASS.$mouseout = function(event) {
        UI_CONTROL_CLASS.$mouseout.call(this);
        event.stopPropagation();
    };

    /**
     * 判断节点是否选中
     * @public
     */
    UI_IND_TREE_NODE_CLASS.isSelect = function() {
        return this._bSelected || false;
    };

    /**
     * 返回节点的值
     */
    UI_IND_TREE_NODE_CLASS.getValue = function() {
        return this._sValue;
    }

    /**
     * 节点的展开收起子树按钮控件
     */
    UI_IND_TREE_NODE_CLASS.Icon = core.inherits(
        ui.Control,
        'ui-indtree-icon',
        function(el, options) {},
        function(el, options) {}
    );
    var UI_IND_TREE_NODE_ICON = UI_IND_TREE_NODE_CLASS.Icon;
    var UI_IND_TREE_NODE_ICON_CLASS = UI_IND_TREE_NODE_ICON.prototype;

    /**
     * 点击展开/收起图标时触发
     * @override
     */
    UI_IND_TREE_NODE_ICON_CLASS.$click = function(event) {
        UI_CONTROL_CLASS.$click.call(this);
        var par = this.getParent();
        if (par._bCollapse) {
            par.$expand();
        }
        else {
            par.$collapse();
        }

        event.stopPropagation();
    };


    /**
     * 节点上的下拉按钮控件
     */
    UI_IND_TREE_NODE_CLASS.PopBtn = core.inherits(
        ui.Control,
        'ui-indtree-btn',
        function(el, options) {},
        function(el, options) {
        }
    );
    var UI_IND_TREE_NODE_POPBTN = UI_IND_TREE_NODE_CLASS.PopBtn;
    var UI_IND_TREE_NODE_POPBTN_CLASS = UI_IND_TREE_NODE_POPBTN.prototype;

    /**
     * 鼠标移到PopBtn上要展开浮层，所有节点共用一个浮层
     * @override
     */
    UI_IND_TREE_NODE_POPBTN_CLASS.$mouseover = function(event) {
        UI_CONTROL_CLASS.$mouseover.call(this);
        var par = this.getParent();
        par._cPop.setParent(this);
        DOCUMENT.body.appendChild(par._cPop.getOuter());
        par._cPop.$render();
        par._cPop.$show();
    };

    /**
     * 鼠标从PopBtn上移出时收起浮层
     * @override
     */
    UI_IND_TREE_NODE_CLASS.$mouseout = function(event) {
        UI_CONTROL_CLASS.$mouseout.call(this);
        //var par = this.getParent();
        this._cPop.$hide();
    };

    /**
     * 节点的下拉面板控件，所有节点共用一个下拉面板
     */
    UI_IND_TREE_CLASS.Pop = core.inherits(
        ui.Control,
        'ui-indtree-pop',
        function(el, options) {
        },
        function(el, options) {}
    );
    var UI_IND_TREE_POP = UI_IND_TREE_CLASS.Pop;
    var UI_IND_TREE_POP_CLASS = UI_IND_TREE_POP.prototype;

    /**
     * 初始化浮层属性
     * @private
     */
    UI_IND_TREE_POP_CLASS.$render = function() {
        var btn = this.getParent();
        var node = btn.getParent();
        var el = this.getOuter();
        dom.removeClass(el, 'ui-indtree-pop-selected');
        if (node.isSelect()) {
            dom.addClass(el, 'ui-indtree-pop-selected');
        }
    };

    /**
     * 显示浮层
     * @override
     */
    UI_IND_TREE_POP_CLASS.$show = function(event) {
        var btn = this.getParent();
        var node = btn.getParent();
        var view = util.getView();
        var pos = dom.getPosition(node.getOuter());
        var width = this.getWidth();
        var height = this.getHeight();
        var nodeWidth = node.getWidth();
        var nodeHeight = node.getHeight();
        var x;
        var y;

        if (pos.left + nodeWidth - width >= view.left) {
            x = pos.left + nodeWidth - width;
        }
        else {
            x = pos.left;
        }

        if (pos.top + nodeHeight + height <= view.bottom) {
            y = pos.top + nodeHeight;
        }
        else {
            y = pos.top - height;
        }

        this.setPosition(x, y);

        UI_CONTROL_CLASS.$show.call(this);
    };

    /**
     * 层级全选按钮
     */
    UI_IND_TREE_CLASS.All = core.inherits(
        ui.Control,
        'ui-indtree-all',
        function(el, options) {
            var level = options.level || 0;
            var i;
            for (i = 0; i < level; i++) {
                var o = dom.create();
                var ecuiAttr = 'level:' + (i + 1) + ';';
                ecuiAttr += 'value:' + options.data[i].uniqName + ';';
                if (options.data[i].selected) {
                    ecuiAttr += 'checked:true;';
                }
                o.setAttribute('ecui', ecuiAttr);
                var e = dom.create('', '', 'label');
                e.innerHTML = '<span title="'
                    + options.data[i].caption
                    + '" class="ui-indtree-all-text">'
                    + options.data[i].caption
                    + '</span>';
                o.appendChild(e);
                if (i == 0) {
                    o.style.visibility = 'hidden';
                }
                el.appendChild(o);
            }
        },
        function(el, options) {
            this.$setBody(el);
            this.$initItems();
        }
    );
    var UI_IND_TREE_ALL = UI_IND_TREE_CLASS.All;
    var UI_IND_TREE_ALL_CLASS = UI_IND_TREE_ALL.prototype;

    util.extend(UI_IND_TREE_ALL_CLASS, UI_ITEMS);

    /**
     * 层级全选按钮的item子控件
     */
    UI_IND_TREE_ALL_CLASS.Item = core.inherits(
        ui.Control,
        'ui-indtree-all-item',
        function(el, options) {
            var o = dom.create('', '', 'input');
            o.setAttribute('type', 'checkbox');
            dom.insertBefore(o, dom.first(dom.first(el)));
        },
        function(el, options) {
            this._bChecked = options.checked === true;
            this._eCheckbox = dom.first(dom.first(el));
            this._nLevel = options.level;
            this._sValue = options.value;
            if (options.checked) {
                this._eCheckbox.checked = true;
            }
        }
    );
    var UI_IND_TREE_ALL_ITEM = UI_IND_TREE_ALL_CLASS.Item;
    var UI_IND_TREE_ALL_ITEM_CLASS = UI_IND_TREE_ALL_ITEM.prototype;

    /**
     * 层级全选item子控件的click事件处理 
     */
    UI_IND_TREE_ALL_ITEM_CLASS.$click = function(event) {
        this._bChecked = !this._bChecked;
        if (this._bChecked) {
            var tree = this.getParent().getParent().root;
            setLevelSelected(tree, this._nLevel);
        }
    };

    UI_IND_TREE_ALL_ITEM_CLASS.setChecked = function(checked) {
        var checked = checked || false;
        this._bChecked = checked;
        if (checked) {
            this._eCheckbox.checked = true;
            var tree = this.getParent().getParent().root;
            setLevelSelected(tree, this._nLevel);
        }
        else {
            this._eCheckbox.checked = false;
        }
    };

    /**
     * 层级全选checkbox是否被勾选
     * @public
     * @return {boolean}
     */
    UI_IND_TREE_ALL_ITEM_CLASS.isSelect = function() {
        return this._bChecked || false;
    };

    /**
     * 获取层级全选item的value
     * @public
     * @return {string}
     */
    UI_IND_TREE_ALL_ITEM_CLASS.getValue = function() {
        return this._sValue;
    };

    /**
     * 将每个节点生成为控件
     * @param {HTML DOM} el 生成控件的主元素
     * @param {Object} parent 节点的父控件
     * @param {boolean=} opt_isRoot 是否是根节点
     */
    function createNodeCon(el, parent, opt_isRoot) {
        var nodeEl = dom.first(el);
        dom.addClass(el, 'ui-indtree-wrap');

        var parNode = dom.getParent(el);
        if (parNode && !opt_isRoot) {
            if (dom.first(parNode) == dom.last(parNode)) {
                dom.addClass(el, 'ui-indtree-single');
            }
            else {
                if (dom.first(parNode) == el) {
                    dom.addClass(el, 'ui-indtree-first');
                }
                else if (dom.last(parNode) == el) {
                    dom.addClass(el, 'ui-indtree-last');
                }
                else {
                    dom.addClass(el, 'ui-indtree-middle');
                }
            }
        }

        dom.addClass(nodeEl, 'ui-indtree-node');
        var options = core.getOptions(nodeEl);
        options.level = opt_isRoot ? 1 : parent._nLevel + 1;
        var nodeCon = core.$fastCreate(
            UI_IND_TREE_CLASS.Node,
            nodeEl,
            parent,
            options
        );

        var par = nodeCon.getParent();
        nodeCon._cPop = par._cPop;
        if (opt_isRoot) {
            nodeCon._cTopPar = par;
        }
        else {
            nodeCon._cTopPar = par._cTopPar;
        }

        if (options.selectAllBtn) {
            par._cSelectAllBtn = nodeCon;
        }

        var childrenEl = dom.children(el)[1];
        if (!childrenEl || dom.children(childrenEl).length == 0) {
            return nodeCon;
        }

        var iconEl = dom.create('ui-indtree-icon ui-indtree-icon-collapse', '', 'div');
        dom.insertAfter(iconEl, nodeEl);

        nodeCon._cIcon = core.$fastCreate(
            UI_IND_TREE_NODE_CLASS.Icon,
            iconEl,
            nodeCon,
            {}
        );

        dom.addClass(childrenEl, 'ui-indtree-children');
        childrenEl.style.display = 'none';
        nodeCon._eChildren = childrenEl;
        nodeCon._aChildren = [];

        var o = dom.children(childrenEl);
        for (var i = 0; i < o.length; i++) {
            nodeCon._aChildren.push(createNodeCon(o[i], nodeCon));
        }

        return nodeCon;
    };

    /**
     * 刷新节点选中状态
     * @param {ecui.ui.indTree.prototype.Node} control 节点控件
     * @param {boolean} expandSelected 是否展开选中节点
     */
    function flushNodeState(control) {
        var par = control.getParent();
        control._bExpandSelected = par._bExpandSelected;
        setNodeSelected(control, control._bSelected);
        if (control._aChildren && control._aChildren.length) {
            var i;
            for (i = 0; i < control._aChildren.length; i++) {
                flushNodeState(control._aChildren[i]);
            }
        }
    };

    /**
     * 设置子树收起展开状态
     * @param {ecui.ui.indTree.prototype.Node} control 节点控件
     * @param {boolean} isCollapse 是否收缩
     */
    function setNodeCollapse(control, isCollapse) {
        if (!control._eChildren) {
            return ;
        }

        var iconEl = control._cIcon.getOuter();
        dom.removeClass(iconEl, 'ui-indtree-icon-expand');
        dom.removeClass(iconEl, 'ui-indtree-icon-collapse');

        if (isCollapse) {
            control._eChildren.style.display = 'none';
            dom.addClass(iconEl, 'ui-indtree-icon-collapse');
        }
        else {
            control._eChildren.style.display = '';
            dom.addClass(iconEl, 'ui-indtree-icon-expand');
        }
        control._bCollapse = isCollapse;

        var all = control._cTopPar._cAll;
        var maxLevel = getMaxLevel(control._cTopPar, true);
        //刷新层级全选按钮的状态
        flushAllState(all, maxLevel);
    };

    /**
     * 设置节点选中状态
     * @param {ecui.ui.indTree.prototype.Node} control 节点控件
     * @param {boolean} selected 当前节点是否选中
     * @param {boolean=} expandSelected 是否展开选中
     */
    function setNodeSelected(control, selected) {
        var el = control.getOuter();
        if (selected) {
            dom.removeClass(el, 'ui-indtree-node-selected');
            dom.addClass(el, 'ui-indtree-node-selected');
            if (control._bExpandSelected) {
                control.$expand();
            }
            // TODO
            // 注掉如下代码，改为点击“全部”后不控制其余节点，
            // 后续可改为点击“全部“后禁用其他节点
            // if (control._bSelectAllBtn) {
            //     var par = control.getParent();
            //     var i;
            //     var list = par._aChildren;
            //     for (i = 0; i < list.length; i++) {
            //         if (!list[i]._bSelectAllBtn && !list[i]._bSelected) {
            //             setNodeSelected(list[i], true);
            //         }
            //     }
            // }
        }
        else {
            dom.removeClass(el, 'ui-indtree-node-selected');
            // TODO
            // 注掉如下代码，改为点击“全部”后不控制其余节点，
            // 后续可改为点击“全部“后禁用其他节点
            // if (!control._bSelectAllBtn) {
            //     var par = control.getParent();
            //     if (par._cSelectAllBtn) {
            //         setNodeSelected(par._cSelectAllBtn, false);
            //     }
            // }
        }

        control._bSelected = selected;
    };

    /**
     * 取得当前tree展示的层级
     * @param {ecui.ui.IndTree} control
     * @param {number=} opt_isOnlyShow 当前的最大层级
     * @return {number} 当前tree展示的层级
     */
    function getMaxLevel(control, opt_isOnlyShow) {
        if (!control._nLevel) {
            control = control.root;
        }
        var level = control._nLevel;

        if (
            control._aChildren 
            && control._aChildren.length 
            && (!opt_isOnlyShow || !control._bCollapse)
        ) {
            var i;
            for (i = 0; i < control._aChildren.length; i++) {
                var n = getMaxLevel(control._aChildren[i], opt_isOnlyShow);
                if (n > level) {
                    level = n;
                }
            }
        }

        return level;
    };

    /**
     * 刷新层级全选按钮的状态
     * @param {ecui.ui.IndTree.prototype.All} control 层级全选按钮控件
     * @param {number} maxLevel 当前树的最大层级
     */
    function flushAllState(control, maxLevel) {
        var list = control.getItems();
        var i;
        for (i = 0; i < maxLevel; i++) {
            list[i].show();
        }
        for (i = maxLevel; i < list.length; i++) {
            list[i].hide();
        }
    };

    /**
     * 选中整个层级
     */
    function setLevelSelected(control, level) {
        if (control._nLevel == level) {
            setNodeSelected(control, true);
        }
        if (control._aChildren && control._aChildren.length) {
            var i;
            for (i = 0; i < control._aChildren.length; i++) {
                setLevelSelected(control._aChildren[i], level);
            }
        }
    };

    /**
     * 渲染维度树
     * @param {Object} datasource 维度树数据
     */
    UI_IND_TREE_CLASS.render = function(datasource) {
        var root = datasource.tree;
        this._oLevalData = datasource.level;
        this._oLevalData.unshift({});
        var el = this.getOuter();

        util.detachEvent(WINDOW, 'resize', core.repaint);
        this.root && this.root.dispose();
        this._cAll && this._cAll.dispose();
        this._cPop && this._cPop.dispose();

        el.innerHTML = '';
        el.appendChild(createTreeView(root));

        this.$setBody(el);
        this.$resize();
        UI_IND_TREE.client.call(this, el, this._oOptions);
        this._bCreated = false;
        this.cache(true, true);
        //UI_CONTROL_CLASS.init.call(this);

        util.attachEvent(WINDOW, 'resize', util.repaint);
        this.resize();
    };

    /**
     * 生成维度树DOM结构
     * @param {Object} obj
     * @param {HTML DOM=} opt_parent 当前树视图的父节点（可选）
     */
    function createTreeView(obj, opt_parent) {
        var wraper = dom.create();
        var node = dom.create();
        wraper.appendChild(node);

        var ecuiAttr = 'value:' + (obj.uniqName || obj.caption) + ';';
        if (obj.selected) {
            ecuiAttr += 'selected:true;';
        }
        if (/^all\$/.test(obj.uniqName)) {
            ecuiAttr += 'selectAllBtn:true;';
        }
        node.setAttribute('ecui', ecuiAttr);
        node.innerHTML = obj.caption;

        if (opt_parent) {
            opt_parent.appendChild(wraper)
        }

        if (!obj.children) {
            return wraper;
        }

        var children = dom.create();
        wraper.appendChild(children);
        var i = 0;
        for (i = 0; i < obj.children.length; i++) {
            createTreeView(obj.children[i], children);
        }

        return wraper;
    };
}) ();