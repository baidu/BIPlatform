/*
TreeView - 定义树形视图的基本操作。
树视图控件，继承自基础控件，不可以被改变大小，可以包含普通子控件或者子树视图控件，普通子控件显示在它的文本区域，如果是子树视图控件，将在专门的子树视图控件区域显示。子树视图控件区域可以被收缩隐藏或是展开显示，默认情况下点击树视图控件就改变子树视图控件区域的状态。

树视图控件直接HTML初始化的例子:
<div ecui="type:tree-view;">
  <!-- 显示的文本，如果没有label整个内容就是节点的文本 -->
  <label>公司</label>
  <!-- 子控件 -->
  <div>董事会</div>
  <div>监事会</div>
  <div>
    <label>总经理</label>
    <div>行政部</div>
    <div>人事部</div>
    <div>财务部</div>
    <div>市场部</div>
    <div>销售部</div>
    <div>技术部</div>
  </div>
</div>

属性
_bCollapsed    - 是否收缩子树
_eChildren     - 子控件区域Element对象
_aChildren     - 子控件集合
*/
//{if 0}//
(function () {

    var core = ecui,
        array = core.array,
        dom = core.dom,
        string = core.string,
        ui = core.ui,
        util = core.util,

        indexOf = array.indexOf,
        remove = array.remove,
        addClass = dom.addClass,
        children = dom.children,
        createDom = dom.create,
        first = dom.first,
        getStyle = dom.getStyle,
        insertAfter = dom.insertAfter,
        removeClass = dom.removeClass,
        trim = string.trim,
        extend = util.extend,
        toNumber = util.toNumber,

        $fastCreate = core.$fastCreate,
        getMouseX = core.getMouseX,
        getOptions = core.getOptions,
        inheritsControl = core.inherits,
        triggerEvent = core.triggerEvent,

        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = UI_CONTROL.prototype;
//{/if}//
//{if $phase == "define"}//
    ///__gzip_original__UI_TREE_VIEW
    ///__gzip_original__UI_TREE_VIEW_CLASS
    /**
     * 初始化树视图控件。
     * options 对象支持的属性如下：
     * collapsed      子树区域是否收缩，默认为展开
     * expandSelected 是否展开选中的节点，如果不自动展开，需要点击左部的小区域图标才有效，默认自动展开
     * @public
     *
     * @param {Object} options 初始化选项
     */
    var UI_TREE_VIEW = ui.TreeView =
        inheritsControl(
            UI_CONTROL,
            'ui-treeview',
            function (el, options) {
                options.resizable = false;

                var o = first(el);

                // 检查是否存在label标签，如果是需要自动初始化树的子结点
                if (o && o.tagName == 'LABEL') {
                    // 初始化子控件
                    for (
                        var i = 0,
                            list = children(el).slice(1),
                            childItems = UI_TREE_VIEW_SETITEMS(this, el.appendChild(createDom()));
                        o = list[i++];
                    ) {
                        childItems.appendChild(o);
                    }

                    addClass(
                        el,
                        options.current = options.primary + (options.collapsed ? '-collapsed' : '-expanded')
                    );

                    if (options.collapsed) {
                        childItems.style.display = 'none';
                    }
                }
            },
            function (el, options) {
                var childTrees = this._aChildren = [];

                this._bCollapsed = options.collapsed || false;
                this._bExpandSelected = options.expandSelected !== false;

                // 初始化子控件
                for (
                    var i = 0,
                        list = children(el.lastChild),
                        o;
                    o = list[i];
                ) {
                    delete options.current;
                    (childTrees[i++] = UI_TREE_VIEW_CREATE_CHILD(o, this, options)).$setParent(this);
                }
            }
        ),
        UI_TREE_VIEW_CLASS = UI_TREE_VIEW.prototype;
//{else}//
    /**
     * 设置树视图控件的选项组 Element 对象。
     * @private
     *
     * @param {ecui.ui.TreeView} tree 树视图控件
     * @param {HTMLElement} items 子树选项组的 Element 对象
     * @return {HTMLElement} items 子树选项组的 Element 对象
     */
    function UI_TREE_VIEW_SETITEMS(tree, items) {
        tree._eChildren = items;
        items.className = tree.getType() + '-children';
        items.style.cssText = '';
        return items;
    }

    /**
     * 树视图控件刷新，根据子树视图控件的数量及显示的状态设置样式。
     * @private
     *
     * @param {ecui.ui.TreeView} control 树视图控件
     */
    function UI_TREE_VIEW_FLUSH(control) {
        control.setClass(
            control.getPrimary() + (control._aChildren.length ? control._bCollapsed ? '-collapsed' : '-expanded' : '')
        );
    }

    /**
     * 建立子树视图控件。
     * @private
     *
     * @param {HTMLElement} el 子树的 Element 对象
     * @param {ecui.ui.TreeView} parent 父树视图控件
     * @param {Object} options 初始化选项，参见 create 方法
     * @return {ecui.ui.TreeView} 子树视图控件
     */
    function UI_TREE_VIEW_CREATE_CHILD(el, parent, options) {
        el.className = (trim(el.className) || parent.getPrimary()) + parent.constructor.agent.TYPES;
        return $fastCreate(parent.constructor, el, null, extend(extend({}, options), getOptions(el)));
    }

    /**
     * 收缩/展开子树区域。
     * @private
     *
     * @param {ecui.ui.TreeView} control 树视图控件
     * @param {boolean} status 是否隐藏子树区域
     * @return {boolean} 状态是否改变
     */
    function UI_TREE_VIEW_SET_COLLAPSE(control, status) {
        if (control._eChildren && control._bCollapsed != status) {
            control._eChildren.style.display = (control._bCollapsed = status) ? 'none' : '';
            UI_TREE_VIEW_FLUSH(control);
        }
    }

    /**
     * 控件点击时改变子树视图控件的显示/隐藏状态。
     * @override
     */
    UI_TREE_VIEW_CLASS.$click = function (event) {
        if (event.getControl() == this) {
            UI_CONTROL_CLASS.$click.call(this, event);

            if (getMouseX(this) <= toNumber(getStyle(this.getBody(), 'paddingLeft'))) {
                // 以下使用 event 代替 name
                this[event = this.isCollapsed() ? 'expand' : 'collapse']();
                triggerEvent(this, event);
            }
            else {
                this.select();
            }
        }
    };

    /**
     * @override
     */
    UI_TREE_VIEW_CLASS.$dispose = function () {
        this._eChildren = null;
        UI_CONTROL_CLASS.$dispose.call(this);
    };

    /**
     * 隐藏树视图控件的同时需要将子树区域也隐藏。
     * @override
     */
    UI_TREE_VIEW_CLASS.$hide = function () {
        UI_CONTROL_CLASS.$hide.call(this);

        if (this._eChildren) {
            this._eChildren.style.display = 'none';
        }
    };

    /**
     * 树视图控件改变位置时，需要将自己的子树区域显示在主元素之后。
     * @override
     */
    UI_TREE_VIEW_CLASS.$setParent = function (parent) {
        var root = this.getRoot(),
            o = this.getParent();

        if (this == root._cSelected || this == root) {
            // 如果当前节点被选中，需要先释放选中
            // 如果当前节点是根节点，需要释放选中
            if (root._cSelected) {
                root._cSelected.alterClass('-selected');
            }
            root._cSelected = null;
        }
        else {
            remove(o._aChildren, this);
            UI_TREE_VIEW_FLUSH(o);
        }

        UI_CONTROL_CLASS.$setParent.call(this, parent);

        // 将子树区域显示在主元素之后
        if (this._eChildren) {
            insertAfter(this._eChildren, this.getOuter());
        }
    };

    /**
     * 显示树视图控件的同时需要将子树视图区域也显示。
     * @override
     */
    UI_TREE_VIEW_CLASS.$show = function () {
        UI_CONTROL_CLASS.$show.call(this);

        if (this._eChildren && !this._bCollapsed) {
            this._eChildren.style.display = '';
        }
    };

    /**
     * 添加子树视图控件。
     * @public
     *
     * @param {string|ecui.ui.TreeView} item 子树视图控件的 html 内容/树视图控件
     * @param {number} index 子树视图控件需要添加的位置序号，不指定将添加在最后
     * @param {Object} options 子树视图控件初始化选项
     * @return {ecui.ui.TreeView} 添加的树视图控件
     */
    UI_TREE_VIEW_CLASS.add = function (item, index, options) {
        var list = this._aChildren,
            o;

        if (!this._eChildren) {
            UI_TREE_VIEW_SETITEMS(this, createDom());
            insertAfter(this._eChildren, this.getOuter());
            this._eChildren.style.display = this._bCollapsed ? 'none' : '';
        }

        if (o = list[index]) {
            o = o.getOuter();
        }
        else {
            index = list.length;
            o = null;
        }

        if ('string' == typeof item) {
            o = this._eChildren.insertBefore(createDom(), o);
            o.innerHTML = item;
            item = UI_TREE_VIEW_CREATE_CHILD(o, this, options);
        }
        else {
            this._eChildren.insertBefore(item.getOuter(), o);
        }

        // 这里需要先 setParent，否则 getRoot 的值将不正确
        item.$setParent(this);
        list.splice(index, 0, item);

        UI_TREE_VIEW_FLUSH(this);

        return item;
    };

    /**
     * 收缩当前树视图控件的子树区域。
     * @public
     */
    UI_TREE_VIEW_CLASS.collapse = function () {
        UI_TREE_VIEW_SET_COLLAPSE(this, true);
    };

    /**
     * 展开当前树视图控件的子树区域。
     * @public
     */
    UI_TREE_VIEW_CLASS.expand = function () {
        UI_TREE_VIEW_SET_COLLAPSE(this, false);
    };

    /**
     * 获取当前树视图控件的所有子树视图控件。
     * @public
     *
     * @return {Array} 树视图控件列表
     */
    UI_TREE_VIEW_CLASS.getChildren = function () {
        return this._aChildren.slice();
    };

    /**
     * 获取当前树视图控件的第一个子树视图控件。
     * @public
     *
     * @return {ecui.ui.TreeView} 树视图控件，如果没有，返回 null
     */
    UI_TREE_VIEW_CLASS.getFirst = function () {
        return this._aChildren[0] || null;
    };

    /**
     * 获取当前树视图控件的最后一个子树视图控件。
     * @public
     *
     * @return {ecui.ui.TreeView} 树视图控件，如果没有，返回 null
     */
    UI_TREE_VIEW_CLASS.getLast = function () {
        return this._aChildren[this._aChildren.length - 1] || null;
    };

    /**
     * 获取当前树视图控件的后一个同级树视图控件。
     * @public
     *
     * @return {ecui.ui.TreeView} 树视图控件，如果没有，返回 null
     */
    UI_TREE_VIEW_CLASS.getNext = function () {
        var parent = this.getParent();
        return parent instanceof UI_TREE_VIEW && parent._aChildren[indexOf(parent._aChildren, this) + 1] || null;
    };

    /**
     * 获取当前树视图控件的前一个同级树视图控件。
     * @public
     *
     * @return {ecui.ui.TreeView} 树视图控件，如果没有，返回 null
     */
    UI_TREE_VIEW_CLASS.getPrev = function () {
        var parent = this.getParent();
        return parent instanceof UI_TREE_VIEW && parent._aChildren[indexOf(parent._aChildren, this) - 1] || null;
    };

    /**
     * 获取当前树视图控件的根控件。
     * @public
     *
     * @return {ecui.ui.TreeView} 树视图控件的根控件
     */
    UI_TREE_VIEW_CLASS.getRoot = function () {
        for (
            var o = this, parent;
            // 这里需要考虑Tree位于上一个Tree的节点内部
            (parent = o.getParent()) instanceof UI_TREE_VIEW && indexOf(parent._aChildren, o) >= 0;
            o = parent
        ) {}
        return o;
    };

    /**
     * 获取当前树视图控件选中的节点。
     * @public
     *
     * @return {ecui.ui.TreeView} 选中的节点
     */
    UI_TREE_VIEW_CLASS.getSelected = function () {
        return this.getRoot()._cSelected || null;
    };

    /**
     * @override
     */
    UI_TREE_VIEW_CLASS.init = function () {
        UI_CONTROL_CLASS.init.call(this);
        for (var i = 0, list = this._aChildren, o; o = list[i++]; ) {
            o.init();
        }
    };

    /**
     * 当前子树区域是否收缩。
     * @public
     *
     * @return {boolean} true 表示子树区域收缩，false 表示子树区域展开
     */
    UI_TREE_VIEW_CLASS.isCollapsed = function () {
        return !this._eChildren || this._bCollapsed;
    };

    /**
     * 将当前节点设置为选中。
     * @public
     */
    UI_TREE_VIEW_CLASS.select = function () {
        var root = this.getRoot();

        if (root._cSelected != this) {
            if (root._cSelected) {
                root._cSelected.alterClass('-selected');
            }
            this.alterClass('+selected');
            root._cSelected = this;
        }

        if (this._bExpandSelected) {
            this.expand();
        }
    };
//{/if}//
//{if 0}//
})();
//{/if}//
