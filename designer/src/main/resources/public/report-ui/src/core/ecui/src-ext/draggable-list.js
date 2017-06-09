/**
 * @file 可拖拽items
 * @author hades(denghongqi@gmail.com)
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

    ui.DraggableList = core.inherits(
        ui.Control,
        'ui-draggable-list',
        function(el, options) {
        },
        function(el, options) {
            this._bDisableSelected = options.disableSelected === true;
            options.targets = options.targets || '';
            this._aTargetIds = options.targets.split(',') || [];
            this._sClazz = options.clazz;
            this.$setBody(el);
            this.$initItems();

            var list = this.getItems();
            for (var i = 0; i < list.length; i++) {
                var o = list[i];
                o.$setState(o._sState);
            }
        }
    );

    var UI_DRAGGABLE_LIST = ui.DraggableList;
    var UI_DRAGGABLE_LIST_CLASS = UI_DRAGGABLE_LIST.prototype;

    /**
     * 禁用$setSize
     */
    UI_DRAGGABLE_LIST_CLASS.$setSize = util.blank;

    /**
     * 增加target控件
     * @param {string} id
     */
    UI_DRAGGABLE_LIST_CLASS.addTarget = function(id) {
        this._aTargetIds.push(id);
    };

    UI_DRAGGABLE_LIST_CLASS.Item = core.inherits(
        UI_ITEM,
        null,
        function(el, options) {
            options.userSelect = false;
        },
        function(el, options) {
            this._sValue = options.value;
            this._sText = el.innerHTML;
            this._sState = options.state || 'normal';
            this._sClazz = options.clazz;
            this._bFixed = options.fixed;
            this._sAlign = options.align;
            this._bConfigBtn = options.configBtn;
            this._sCalcColumnRefInd = options.calcColumnRefInd;
            if (this._sClazz == 'DIM') {
                dom.addClass(el, 'ui-draggable-list-item-dim');
            }
            else if (this._sClazz == 'IND') {
                dom.addClass(el, 'ui-draggable-list-item-ind');
            }
        }
    );
    var UI_DRAGGABLE_LIST_ITEM_CLASS = UI_DRAGGABLE_LIST_CLASS.Item.prototype;

    util.extend(UI_DRAGGABLE_LIST_CLASS, UI_ITEMS);

    /**
     * 要写dispose
     * @protected
     */
    UI_DRAGGABLE_LIST_CLASS.$dispose = function() {
        delete UI_ITEMS[this.getUID()];
        this.getOuter().innerHTML = '';
        util.callSuper(this, '$dispose');
    };
    

    UI_DRAGGABLE_LIST_CLASS.$alterItems = function() {
    };

    /**
     * 添加一个item
     * @public
     * @param {Object} data
     * @param {string} data.value
     * @param {string} data.text
     * @param {string} data.clazz
     * @param {boolean=} data.fixed
     * @param {boolean=} data.align
     * @param {number=} opt_index
     */
    UI_DRAGGABLE_LIST_CLASS.addItem = function(data, opt_index) {
        var el = dom.create();
        el.innerHTML = data.text;
        this.getOuter().appendChild(el);
        this.add(el, opt_index, data);
    };

    /**
     * 移除一个item
     * @public
     * @param {string} value
     */
    UI_DRAGGABLE_LIST_CLASS.removeItem = function(value) {
        this.remove(this.getItemByValue(value));
    };

    /**
     * 获取控件的clazz
     * @public
     * @return {string}
     */
    UI_DRAGGABLE_LIST_CLASS.getClazz = function() {
        return this._sClazz;
    };

    /**
     * 控件激活时触发拖动
     */
    UI_DRAGGABLE_LIST_ITEM_CLASS.$activate = function(event) {
        UI_CONTROL_CLASS.$activate.call(this, event);

        core.drag(this, event);
    };

    /**
     * 拖动开始时执行
     */
    UI_DRAGGABLE_LIST_ITEM_CLASS.$dragstart = function() {
        var el = this.getOuter();
        el.style.zIndex = 32768;
        var par = this.getParent();
        var list = par.getItems();
        var index = 0;
        var i = 0;
        while(list[i] && list[i++] != this) {
            index = i;
        }

        if (!par._cPlacehold) {
            el = dom.create('ui-draggable-list-placehold');
            par._cPlacehold = par.add(el, index, {});
            par._cPlacehold.setSize(this.getWidth(), this.getHeight());
        }
    };

    /**
     * 拖动中触发
     * @param {ecui.Event} event
     */
    UI_DRAGGABLE_LIST_ITEM_CLASS.$dragmove = function(event) {
        var el = this.getOuter();
        var par = this.getParent();
        var targetEl;
        var targetCon;

        for (var i = 0; i < par._aTargetIds.length; i++) {
            if (
                core.get(par._aTargetIds[i])
                && intersect(el, core.get(par._aTargetIds[i]).getOuter())
            ) {
                targetCon = core.get(par._aTargetIds[i]);
                targetEl = targetCon.getOuter();
                break;
            }
        }

        if (par._cCurDrop && targetCon != par._cCurDrop) {
            core.triggerEvent (par._cCurDrop, 'dragout', event, [this]);
        }
        par._cCurDrop = targetCon;

        if (!targetEl) {
            return ;
        }
        core.triggerEvent(targetCon, 'dragover', event, [this]);
    };

    /**
     * 拖动结束时触发
     * （此方法要保证能重复执行两遍，因为ecui拖拽到窗口外的问题未修）
     */
    UI_DRAGGABLE_LIST_ITEM_CLASS.$dragend = function(event) {
        var el  = this.getOuter();
        el.style.position = 'relative';
        el.style.zIndex = 0;
        this.setPosition(0, 0);

        var par = this.getParent();

        if (par._cPlacehold == null) {
            return;
        }

        par.remove(par._cPlacehold);
        par._cPlacehold = null;

        if (par._cCurDrop) {
            core.triggerEvent (par._cCurDrop, 'drop', event, [this]);
            // this.setSelected(true);
        }

        par._cCurDrop = null;
    };

    /**
     * 获取item子控件的值
     * @public
     * @return {string}
     */
    UI_DRAGGABLE_LIST_ITEM_CLASS.getValue = function() {
        return this._sValue;
    };

    /**
     * 获取控件显示的文字
     * @public
     * @return {string}
     */
    UI_DRAGGABLE_LIST_ITEM_CLASS.getText = function() {
        return this._sText;
    };

    /**
     * 获取控件的clazz
     * @public
     * @return {string}
     */
    UI_DRAGGABLE_LIST_ITEM_CLASS.getClazz = function() {
        return this._sClazz;
    };

    /**
     * 获取控件的数据封装
     * @public
     * @return {string}
     */
    UI_DRAGGABLE_LIST_ITEM_CLASS.getWrap = function() {
        return {
            value : this._sValue,
            text : this._sText,
            clazz : this._sClazz,
            fixed: this._bFixed,
            align: this._sAlign,
            configBtn: this._bConfigBtn,
            calcColumnRefInd: this._sCalcColumnRefInd
        };
    };

    /**
     * 设置item为选中或不选中状态
     * @public
     * @param {boolean} state true为选中，false为取消选中
     */
    UI_DRAGGABLE_LIST_ITEM_CLASS.setSelected = function(state) {
        var el = this.getOuter();
        dom.removeClass(el, 'ui-draggable-list-item-selected');
        if (state) {
            dom.addClass(el, 'ui-draggable-list-item-selected');
            this._sState = 'selected';
            var par = this.getParent();
            if (par._bDisableSelected) {
                this.disable();
                dom.removeClass(el, 'ui-draggable-list-item-disabled');
                dom.removeClass(el, 'ui-item-disabled');
            }
        }

        this._bSelected = state;
    };

    /**
     * 设置控件状态
     * @private
     * @param {string} state
     */
    UI_DRAGGABLE_LIST_ITEM_CLASS.$setState = function(state) {
        if (state == 'disable') {
            this.disable();
            this._sState = 'disable';
        }
        else if (state == 'selected') {
            this.setSelected(true);
        }
        else {
            this.enable();
            this.setSelected(false);
            this._sState = 'normal';
        }
    };

    /**
     * 批量设置item子控件状态
     * @public
     * @param {Object} obj
     */
    UI_DRAGGABLE_LIST_CLASS.setState = function(obj) {
        for (var i = 0, len = this.getItems().length; i < len; i++) {
            this.getItems()[i].$setState('normal');
        }
        if (obj['disable'] && obj['disable'].length) {
            for (var i = 0; i < obj['disable'].length; i++) {
                this.getItemByValue(obj['disable'][i]).$setState('disable');
            }
        }
        if (obj['selected'] && obj['selected'].length) {
            for (var i = 0; i < obj['selected'].length; i++) {
                this.getItemByValue(obj['selected'][i]).$setState('selected');
            }
        }
    };

    /**
     * 根据value获取item控件
     * @public
     * @param {value}
     * @return {ecui.ui.Control}
     */
    UI_DRAGGABLE_LIST_CLASS.getItemByValue = function(value) {
        var list = this.getItems();
        for (var i = 0; i < list.length; i++) {
            if (list[i].getValue() == value) {
                return list[i];
            }
        }

        return null;
    };

    /**
     * disable所有item子控件
     * @public
     */
    UI_DRAGGABLE_LIST_CLASS.disableAll = function() {
        var list = this.getItems();
        for (var i = 0; i < list.length; i++) {
            list[i].disable();
        }
    };

    /**
     * 判断两个元素是否相交
     * @param {HTML element} element1 要检查的元素
     * @param {HTML element} element2 要检查的元素
     * @return {boolean} 检查两个元素是否相交的结果
     */
    function intersect(element1, element2) {
        var pos1 = ecui.dom.getPosition(element1);
        var pos2 = ecui.dom.getPosition(element2);

        var maxLeft = Math.max(pos1.left, pos2.left);
        var minRight = Math.min(
            pos1.left + element1.offsetWidth, 
            pos2.left + element2.offsetWidth
        );
        var maxTop = Math.max(pos1.top, pos2.top);
        var minBottom = Math.min(
            pos1.top + element1.offsetHeight,
            pos2.top + element2.offsetHeight
        );

        return maxLeft <= minRight && maxTop <= minBottom;
    };

}) ();