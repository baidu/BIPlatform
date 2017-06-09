/**
 * input tree
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * path:    input-tree.js
 * desc:    树层级输入框
 * author:  cxl(chenxinle)
 * date:    2012/03/12
 */
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
        addClass = dom.addClass,
        getElementsByClass = dom.getElementsByClass,
        previous = dom.previous,
        children = dom.children,
        encodeHTML = string.encodeHTML,
        moveElements = dom.moveElements,
        getPosition  = dom.getPosition,
        inheritsControl = core.inherits,
        getView = util.getView,
        extend = util.extend,
        blank = util.blank,
        triggerEvent = core.triggerEvent,

        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = UI_CONTROL.prototype,
        UI_BUTTON = ui.Button,
        UI_BUTTON_CLASS = UI_BUTTON.prototype,
        UI_INPUT_CONTROL = ui.InputControl,
        UI_INPUT_CONTROL_CLASS = UI_INPUT_CONTROL.prototype;

    var UI_INPUT_TREE = ui.InputTree =
            inheritsControl(
                UI_INPUT_CONTROL,
                'ui-input-tree',
                function (el, options) {
                    var type = this.getTypes()[0],
                        o = createDom();

                    o.innerHTML = '<div class="'+ type +'-layer" '
                        + ' style="position:absolute;display:none; z-index:65535; height:230px; width:250px">'
                        + '<div class="'
                        + UI_DATA_TREE.types[0] +'"></div></div>';

                    o = o.firstChild;

                    moveElements(el, o.lastChild, true);
                    options._eLayer = document.body.appendChild(o);

                    el.innerHTML = ''
                        + '<span class="'+ type +'-text"></span>'
                        + '<span class="'+ type +'-cancel"></span>'
                        + '<span class="'+ type +'-button"></span>'
                        + '<input type="hidden name="'+ (options.name || '') +'" />';

                    options.hidden = true;
                    if (options.value) {
                        options.value += '';
                    }
                },
                function (el, options) {
                    var childs;

                    if (options.value) {
                        UI_INPUT_CONTROL_CLASS .setValue.call(this, options.value);
                    }

                    childs = children(el);

                    this._eText = childs[0];
                    this._uCancel = $fastCreate(this.Cancel, childs[1], this);
                    this._uLayer = $fastCreate(this.Layer, options._eLayer, this, {asyn : options.asyn});
                    options._eLayer = null;
                    delete options._eLayer;

                    if (options.hideCancel === true) {
                        this._bHideCancel = true;
                        this._uCancel.$hide();
                    }
                }
            ),

        UI_INPUT_TREE_CLASS = UI_INPUT_TREE.prototype,

        UI_INPUT_TREE_LAYER = UI_INPUT_TREE_CLASS.Layer =
            inheritsControl(
                UI_CONTROL,
                'ui-input-tree-layer',
                null,
                function (el, options) {
                    el.style.position = 'absolute';
                    // 改为在setData中创建
                    // this._uTree = $fastCreate(this.Tree, el.firstChild, this, {collapsed:true, asyn: options.asyn});
                }
            ),
        UI_INPUT_TREE_LAYER_CLASS = UI_INPUT_TREE_LAYER.prototype,

        UI_DATA_TREE = ui.DataTree,

        UI_INPUT_TREE_CANCEL_CLASS = (UI_INPUT_TREE_CLASS.Cancel = inheritsControl(UI_CONTROL)).prototype,
        UI_INPUT_TREE_LAYER_TREE_CLASS = (UI_INPUT_TREE_LAYER_CLASS.Tree = inheritsControl(UI_DATA_TREE)).prototype;

    function UI_INPUT_TREE_FLUSH(con) {
        if (con.getValue() == '') {
            con._uCancel.hide();
        }
        else if (!con._bHideCancel) {
            con._uCancel.show();
        }
    }

    //////////////////////////
    /**
     * 设置初始值
     *
     * @public
     * @param {Object} data 数据
     * @param {Object} data.root 初始树根
     *      每个节点内容为：
     *          {string} text
     *          {string} value
     *          {boolean} isLeaf
     * @param {string} data.selected 初始选中
     */
    UI_INPUT_TREE_CLASS.setData = function (data, options) {
        options = options || {};

        if (!data || !data.root) {
            return;
        }

        var html = [];

        function travelTree(node, isRoot) {
            var children = node.children || [];

            if (children.length == 0) {
                html.push(
                    '<div ecui="value:', encodeHTML(String(node.value)), ';isLeaf:', !!node.isLeaf, '">',
                    encodeHTML(node.text),
                    '</div>'
                );
            }
            else {
                html.push(
                    '<div>',
                    '<label ecui="value:', encodeHTML(String(node.value)), ';isLeaf:', !!node.isLeaf, '">',
                    encodeHTML(node.text),
                    '</label>'
                );
                for (var i = 0, child; child = children[i]; i ++) {
                    travelTree(child);
                }
                html.push('</div>');
            }
        }

        travelTree(data.root);

        var layer = this._uLayer;
        var o = layer.getBody();
        o.innerHTML = html.join('');
        addClass(o.firstChild, UI_DATA_TREE.types[0]);

        layer._uTree = $fastCreate(
            layer.Tree,
            o.firstChild,
            layer,
            extend(
                {
                    collapsed: true,
                    value: String(data.root.value),
                    isLeaf: data.root.isLeaf
                },
                options
            )
        );

        layer._uTree.init();
        // 为了适应数据是array的情况
        var rootChildrenEl = getElementsByClass(this._uLayer._eBody, 'div', 'ui-data-tree-children')[0];
        var rootEl = previous(rootChildrenEl);
        addClass(rootEl, 'hide');
        rootChildrenEl.style.display = 'block';
        if (data.selected != null ) {
            this.setValue(String(data.selected));
        }
    }

    UI_INPUT_TREE_CLASS.$activate = function () {
        this._uLayer.show();
    }
    UI_INPUT_TREE_CLASS.getValue = function () {

        var text = this._eText.innerHTML;
        var value = this._eInput.value;
        return {
            text: text,
            value: value
        }
    }

    UI_INPUT_TREE_CLASS.init = function () {
        var value = this.getValue();

        this.setValue(value);
        this._uLayer.init();
        UI_INPUT_CONTROL_CLASS.init.call(this);
    }

    UI_INPUT_TREE_CLASS.$setText = function (value) {
        if (value && value.length > 15) {
            value = value.substring(0, 15) + '...';
        }
        this._eText.innerHTML = value;
    }

    UI_INPUT_TREE_CLASS.setValue = function (value) {
        var tree = this._uLayer._uTree;
        if (!tree) { return; }

        if ('[object Object]' == Object.prototype.toString.call(value)) {
            UI_INPUT_CONTROL_CLASS.setValue.call(this, value.value);
            tree.clearSelected();
            tree.setValues([value.value]);
            this.$setText(tree.getSelectedText());
            UI_INPUT_TREE_FLUSH(this);
        }
        else {
            //转化为字符串
            value = value + '';
            UI_INPUT_CONTROL_CLASS.setValue.call(this, value);
            tree.clearSelected();
            tree.setValues([value]);
            this.$setText(tree.getSelectedText());
            UI_INPUT_TREE_FLUSH(this);
        }
    }

    UI_INPUT_TREE_CLASS.clear = function () {
        var tree = this._uLayer._uTree;

        tree.clearSelected();
        UI_INPUT_CONTROL_CLASS.setValue.call(this, '');
        this.$setText('');
        UI_INPUT_TREE_FLUSH(this);
    }

    /**
     * 重新收起input-tree,清理用户操作痕迹
     * @public
     */
    UI_INPUT_TREE_CLASS.clearState = function() {
        var tree = this._uLayer._uTree;
        collapseTree(tree);

        function collapseTree(tree) {
            tree.collapse();
            var children = tree.getChildren();
            if (children && children.length) {
                for (var i = 0; i < children.length; i++) {
                    collapseTree(children[i]);
                }
            }
        };
    };

    /**
     * 根据value获取树中的节点
     * @public
     * @param {string} value
     */
    UI_INPUT_TREE_CLASS.getTreeNodeByValue = function(value) {
        return this._uLayer.getTreeNodeByValue(value);
    };

    /**
     * 设置输入文本框的值
     * @public
     * @param {string} text
     */
    UI_INPUT_TREE_CLASS.setText = function(text) {
        this.$setText(text);
    };

    UI_INPUT_TREE_CLASS.expand = function (value, callback) {
        var me = this;

        this._uLayer.expand(value, function () {
            callback.call(me);
        });
    }

    UI_INPUT_TREE_CLASS.selectParent = function (value) {
        var node = this._uLayer.getTreeNodeByValue(value);

        if (node != node.getRoot()) {
            node = node.getParent();
        }

        this.setValue(node.getValue());
    }

    UI_INPUT_TREE_LAYER_CLASS.init = function () {
        this._uTree && this._uTree.init();
        UI_CONTROL_CLASS.init.call(this);
    }

    UI_INPUT_TREE_LAYER_CLASS.$blur = function () {
        this.hide();
    }

    UI_INPUT_TREE_LAYER_CLASS.expand = function (value, callback) {
        var tree = this._uTree,
            node = tree.getItemByValue(value);
        if (node) {
            node.expand();
            tree.onexpand(node, callback);
        }
    }

    UI_INPUT_TREE_LAYER_CLASS.getTreeNodeByValue = function (value) {
        return this._uTree.getItemByValue(value);
    }

    UI_INPUT_TREE_LAYER_CLASS.show = function () {
        var par = this.getParent(), pos, o, view;

        UI_CONTROL_CLASS.show.call(this);

        if (par) {
            pos = getPosition(par.getOuter());
            view = getView();
            o = pos.top;
            /*
             if (o + par.getHeight() + this.getHeight() > view.bottom) {
             if (o - view.top > this.getHeight()) {
             pos.top = o - this.getHeight();
             }
             }
             else {
             pos.top = o + par.getHeight();
             }
             */

            pos.top = o + par.getHeight();

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

    UI_INPUT_TREE_CANCEL_CLASS.$click = function () {
        var par = this.getParent();
        UI_CONTROL_CLASS.$click.call(this);

        par.$setText('');
        UI_INPUT_CONTROL_CLASS.setValue.call(par, '');
        par._uLayer._uTree.clearSelected();
        UI_INPUT_TREE_FLUSH(par);
    }

    UI_INPUT_TREE_CANCEL_CLASS.$activate = UI_BUTTON_CLASS.$activate;

    UI_INPUT_TREE_LAYER_TREE_CLASS.onselect = function (con, added) {
        var superObj = this.getParent().getParent();
        UI_INPUT_CONTROL_CLASS.setValue.call(superObj, con.getValue());
        superObj.$setText(con.getText());
        UI_INPUT_TREE_FLUSH(superObj);
        this.getParent().hide();
        triggerEvent(superObj, 'change', null, [con.getValue()]);
    }

    UI_INPUT_TREE_LAYER_TREE_CLASS.onexpand = function (item, callback) {
        var superObj = this.getParent().getParent(),
            callback = callback || blank;

        var layer =  superObj._uLayer.getOuter(),
            scrollHeight = layer.scrollTop;
        var setScroll = function() {
            layer.scrollTop = scrollHeight ;
            layer = null;
        }
        if (item._bNeedAsyn) {
            triggerEvent(superObj, 'loadtree', null, [item.getValue(), function (data) {
                item.load(data);
                callback.call(null);
                setScroll();
            }]);
            item._bNeedAsyn = false;
        }
        else {
            callback.call(null);
            setScroll();
        }
    }

    UI_INPUT_TREE_LAYER_TREE_CLASS.load = function (datasource) {
        var i, item, text;

        for (i = 0; item = this._aChildren[i]; i++) {
            disposeControl(item);
        }
        this._aChildren = [];
        this._eChildren.innerHTML = '';

        if (!datasource || datasource.length <= 0) {
            this.setClass(this.getPrimary());
            return;
        }

        for (i = 0; item = datasource[i]; i++) {
            text = item.text;
            item = extend({asyn: this._bAsyn}, item);
            delete item.text;
            this.add(text, null, item).init();
        }

    }
})();
