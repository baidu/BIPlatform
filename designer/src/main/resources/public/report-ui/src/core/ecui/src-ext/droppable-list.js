/**
 * @file 可拖放的items
 * @author hades(denghongqi)
 */

(function() {
    var core = ecui;
    var ui = core.ui;
    var dom = core.dom;
    var array = core.array;
    var util = core.util;

    var UI_CONTROL = ui.Control;
    var UI_CONTROL_CLASS = UI_CONTROL.prototype;
    var UI_ITEMS = ui.Items;
    var UI_ITEM = ui.Item;
    var UI_ITEM_CLASS = UI_ITEM.prototype;

    ui.DroppableList = core.inherits(
        UI_CONTROL,
        'ui-droppable-list',
        function(el, options) {
        },
        function(el, options) {
            this._sName = options.name;
            options.targets = options.targets ||'';
            this._aTargetIds = options.targets.split(',') || [];
            options.source = options.source || '';
            this._aSourceIds = options.source.split(',') || [];
            this._bConfigBtn = options.configBtn;
            this.$setBody(el);
            this.$initItems();
        }
    );

    var UI_DROPPABLE_LIST = ui.DroppableList;
    var UI_DROPPABLE_LIST_CLASS = UI_DROPPABLE_LIST.prototype;

    util.extend(UI_DROPPABLE_LIST_CLASS, UI_ITEMS);

    UI_DROPPABLE_LIST_CLASS.$alterItems = util.blank;

    /**
     * 禁用$setSize
     * @override
     */
    UI_DROPPABLE_LIST_CLASS.$setSize = util.blank;

    UI_DROPPABLE_LIST_CLASS.add = function (item, index, options) {
        options = options;
        options.configBtn = options.configBtn == null 
            ? this._bConfigBtn : options.configBtn;
        return UI_ITEMS.add.call(this, item, index, options);
    };

    /**
     * 要写dispose
     * @protected
     */
    UI_DROPPABLE_LIST_CLASS.$dispose = function() {
        delete UI_ITEMS[this.getUID()];
        this.getOuter().innerHTML = '';
        util.callSuper(this, '$dispose');
    };

    /**
     * @override
     */
    UI_DROPPABLE_LIST_CLASS.$mouseover = function() {
        UI_CONTROL_CLASS.$mouseover.call(this);
    };

    /**
     * 当有可drop元素经过时触发
     * @param {ecui.Event} event
     * @param {ecui.ui.Control} 可drop的控件
     */
    UI_DROPPABLE_LIST_CLASS.$dragover = function(event, control) {
        if (this._cPlacehold) {
            this.remove(this._cPlacehold);
        }

        var index = getInsertIndex(this, event,control);

        // 校验是否可以drop
        if (core.triggerEvent(
                this,
                'checkdroppable',
                null, 
                [control.getWrap(), index, this._sName]
            ) === false
        ) {
            this._cPlacehold = null;
            return;
        }

        var o = dom.create('ui-droppable-list-placehold');
        this.getBody().appendChild(o);
        this._cPlacehold = this.add(o, index, {placehold : true});
        this._cPlacehold.setSize(control.getWidth(), control.getHeight());
    };

    /**
     * 可拖拽元素移出时触发
     * @param {ecui.Event} event
     * @param {ecui.ui.Control} control 可drop的控件
     */
    UI_DROPPABLE_LIST_CLASS.$dragout = function(event, control) {
        if (!this._cPlacehold) {
            return ;
        }
        this.remove(this._cPlacehold);
        this._cPlacehold = null;
    };

    /**
     * 可拖拽控件drop时触发
     * @param {ecui.Event} event
     * @param {ecui.ui.Control} kedrop的控件
     */
    UI_DROPPABLE_LIST_CLASS.$drop = function(event, control, listOfCon) {
        if (!this._cPlacehold) {
            return false; 
        }

        var index = getInsertIndex(this, event,control);
        if (this._cPlacehold) {
            this.remove(this._cPlacehold);
        }

        this._cNewAdd = this.addByItem(control, index);

        //this._cNewAdd.setSize(control.getWidth(), control.getHeight());

        if (this == listOfCon && control._nOriginIndex == index) {
            return;
        }

        for (var i = 0, con; i < this._aSourceIds.length; i++) {
            con = core.get(this._aSourceIds[i]);
            if (con && con.getClazz() == control.getClazz()) {
                core.triggerEvent(
                    con, 
                    'change', 
                    null, 
                    [
                        control.getWrap(), 
                        index, 
                        this._sName, 
                        control._nOriginIndex, 
                        control._sOriginParName
                    ]
                );
            }
        }        
    };

    /**
     * 可拖拽控件throw时触发
     * @param {ecui.Event} event
     * @param {ecui.ui.Control} kedrop的控件
     */
    UI_DROPPABLE_LIST_CLASS.$throw = function(event, control, listOfCon) {
        for (var i = 0, con; i < this._aSourceIds.length; i++) {
            con = core.get(this._aSourceIds[i]);
            if (con && con.getClazz() == control.getClazz()) {
                core.triggerEvent(
                    con, 
                    'change', 
                    null,
                    [
                        control.getWrap(), 
                        null, 
                        this._sName,
                        control._nOriginIndex, 
                        control._sOriginParName
                    ]
                );
            }
        }        
    };

    /**
     * 得到item个数
     *
     * @public
     * @return {number} item个数
     */
    UI_DROPPABLE_LIST_CLASS.count = function() {
        return UI_ITEMS[this.getUID()].length;
    }

    /**
     * 是否包含此item
     *
     * @public
     * @param {Item} item
     * @return {boolean} 是否包含
     */
    UI_DROPPABLE_LIST_CLASS.contains = function(item) {
        return array.indexOf(UI_ITEMS[this.getUID()], item) >= 0;
    }

    /**
     * 添加一个item
     * @public
     * @param {Object} data
     * @param {string} data.value
     * @param {string} data.text
     * @param {string} data.clazz
     * @param {boolean=} data.fixed
     * @param {string=} data.align
     * @param {number=} opt_index
     */
    UI_DROPPABLE_LIST_CLASS.addItem = function(data, opt_index) {
        var el = dom.create();
        el.innerHTML = data.text;
        this.getOuter().appendChild(el);
        this.add(el, opt_index, data);
    };

    /**
     * 增加target控件
     * @param {string} id
     */
    UI_DROPPABLE_LIST_CLASS.addTarget = function(id) {
        this._aTargetIds.push(id);
    };

    /**
     * 移除一个item
     * @public
     * @param {string} value
     */
    UI_DROPPABLE_LIST_CLASS.removeItem = function(value) {
        this.remove(this.getItemByValue(value));
    };

    /**
     * 根绝value获取item控件
     * @public
     * @param {string} value
     * @return {ecui.ui.Control}
     */
    UI_DROPPABLE_LIST_CLASS.getItemByValue = function(value) {
        var list = this.getItems();
        for (var i = 0; i < list.length; i++) {
            if (value == list[i].getValue()) {
                return list[i];
            }
        }

        return null;
    };

    /**
     * 获取drop控件容纳的子控件的值
     * @public
     * @return {Array}
     */
    UI_DROPPABLE_LIST_CLASS.getValue = function() {
        var list = this.getItems();
        var res = [];
        for (var i = 0; i < list.length; i++) {
            res.push(list[i].getValue());
        }

        return res;
    };    

    /**
     * 获取drop控件容纳的子控件的数据
     * @public
     * @return {Array}
     */
    UI_DROPPABLE_LIST_CLASS.getWrap = function() {
        var list = this.getItems();
        var res = [];
        for (var i = 0; i < list.length; i++) {
            res.push(list[i].getWrap());
        }

        return res;
    };

    /**
     * 根据一个节点的内容，复制，添加节点
     * 
     * @public
     * @param {Item} 原节点
     * @param {number} 目标位置
     * @return {Item} 添加得到的结果
     */
    UI_DROPPABLE_LIST_CLASS.addByItem = function(srcCtrl, index) {
        var o = dom.create();
        o.innerHTML = srcCtrl.getText();
        this.getBody().appendChild(o);

        return this.add(o, index, srcCtrl.getWrap());
    };


    UI_DROPPABLE_LIST_CLASS.Item = core.inherits(
        UI_ITEM,
        null,
        function(el, options) {
            options.userSelect = false;
            this._sText = el.innerHTML;
            if (!options.placehold && options.configBtn) {
                var o = dom.create('ui-droppable-list-item-icon');
                this._bConfigBtn = options.configBtn;
                el.appendChild(o);
            }
        },
        function(el, options) {
            this._sValue = options.value;
            this._sClazz = options.clazz;
            this._bFixed = options.fixed;
            this._sAlign = options.align;
            this._sCalcColumnRefInd = options.calcColumnRefInd;
            if (this._sClazz == 'DIM') {
                dom.addClass(el, 'ui-droppable-list-item-dim');
            }
            else if (this._sClazz == 'IND') {
                dom.addClass(el, 'ui-droppable-list-item-ind');
            }
            if (!options.placehold && options.configBtn) {
                this._cIcon = core.$fastCreate(this.Icon, dom.last(el), this, {});
            }
        }
    );
    var UI_DROPPABLE_LIST_ITEM = UI_DROPPABLE_LIST_CLASS.Item;
    var UI_DROPPABLE_LIST_ITEM_CLASS = UI_DROPPABLE_LIST_ITEM.prototype;

    /**
     * 设置item子控件的值
     * @public
     * @param {string} value
     */
    UI_DROPPABLE_LIST_ITEM_CLASS.setValue = function(value) {
        this._sValue = value;
    };

    /**
     * 获取item的值
     * @public
     * @return {string}
     */
    UI_DROPPABLE_LIST_ITEM_CLASS.getValue = function() {
        return this._sValue;
    };

    /**
     * 获取item的文本
     * @public
     * @return {string}
     */
    UI_DROPPABLE_LIST_ITEM_CLASS.getText = function() {
        return this._sText;
    };

    /**
     * 获取item子控件的clazz
     * @public
     * @return {string}
     */
    UI_DROPPABLE_LIST_ITEM_CLASS.getClazz = function() {
        return this._sClazz;
    };

    /**
     * 获取item子控件的数据封装
     * @public
     * @return {string}
     */
    UI_DROPPABLE_LIST_ITEM_CLASS.getWrap = function() {
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
     * 控件激活时触发拖动
     */
    UI_DROPPABLE_LIST_ITEM_CLASS.$activate = function(event) {
        UI_CONTROL_CLASS.$activate.call(this, event);

        var par = this.getParent();
        var originIndex = array.indexOf(UI_ITEMS[par.getUID()], this);

        if (core.triggerEvent(
                par, 
                'checkdraggable', 
                null, 
                [
                    this.getWrap(), 
                    originIndex, 
                    par._sName
                ]
            ) !== false
        ) {
            this._sOriginParName = par._sName; // 拖拽之前的行名
            this._nOriginIndex = originIndex; // 拖拽之前的index
            core.drag(this, event);
        }
    };

    /**
     * 开始拖拽时触发
     */
    UI_DROPPABLE_LIST_ITEM_CLASS.$dragstart = function(event) {
    };

    /**
     * 拖拽中触发
     */
    UI_DROPPABLE_LIST_ITEM_CLASS.$dragmove = function(event) {
        this._bDragging = true;

        var par = this.getParent();
        var conArr = [];
        conArr.push(par);
        for (var i = 0; i < par._aTargetIds.length; i++) {
            if (core.get(par._aTargetIds[i])) {
                conArr.push(core.get(par._aTargetIds[i]));
            }
        }

        var el = this.getOuter();
        var targetCon;
        var targetEl;
        for (var i = 0; i < conArr.length; i++) {
            if (intersect(el, conArr[i].getOuter())) {
                targetCon = conArr[i];
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
     * 拖拽结束时触发
     * （此方法要保证能重复执行两遍，因为ecui拖拽到窗口外的问题未修）
     */
    UI_DROPPABLE_LIST_ITEM_CLASS.$dragend = function(event) {
        var par = this.getParent();

        if (!par) { return; }

        if (!par._cCurDrop) {
            if (
                event.pageX >= dom.getPosition(par.getOuter()).left
                && event.pageX <= dom.getPosition(par.getOuter()).left + par.getWidth()
                && event.pageY >= dom.getPosition(par.getOuter()).top
                && event.pageY <= dom.getPosition(par.getOuter()).top + par.getHeight()
            ) {
                par._cCurDrop = par;
            }
        }
        par.remove(this);
        if (par._cCurDrop) {
            if (core.triggerEvent(par._cCurDrop, 'drop', event, [this, par]) == false) {
                // drop失败时（例如drop被禁止时），重新加入节点
                par.addByItem(this, this._nOriginIndex);           
            }
        }
        else {
            core.triggerEvent(par, 'throw', event, [this, par]);
        }
        par._cCurDrop = null;
        this._bDragging = false;
        this._nOriginIndex = null;

        core.triggerEvent(par, 'deactivate', event);
        core.triggerEvent(par, 'blur', event);
    };

    /**
     * item上的点击按钮
     */
    UI_DROPPABLE_LIST_ITEM_CLASS.Icon = core.inherits(
        UI_CONTROL,
        'ui-droppable-list-item-icon',
        null,
        null
    );
    var UI_DROPPABLE_LIST_ITEM_ICON = UI_DROPPABLE_LIST_ITEM_CLASS.Icon;
    var UI_DROPPABLE_LIST_ITEM_ICON_CLASS = UI_DROPPABLE_LIST_ITEM_ICON.prototype;

    /**
     * 按钮的click事件
     */
    UI_DROPPABLE_LIST_ITEM_ICON_CLASS.$click = function(event) {
        UI_CONTROL_CLASS.$click.call(this);
        var item = this.getParent();
        var itemData = item.getWrap();
        core.triggerEvent(item.getParent(), 'itemclick', event, [itemData]);

        event.stopPropagation();
    };

    /**
     * 阻止按钮activate事件的冒泡
     */
    UI_DROPPABLE_LIST_ITEM_ICON_CLASS.$activate = function(event) {
        UI_CONTROL_CLASS.$activate.call(this);

        event.stopPropagation();
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

    /**
     * 计算拖拽子控件插入的index
     * @param {ecui.ui.Items} control
     * @param {ecui.Event} event
     */
    function getInsertIndex(control, event,ctrl) {
        var list = control.getItems();
        var flag;
        for (index = 0; index < list.length; index++) {
            var item = list[index];
            var el = item.getOuter();
            // 如果要插入的item本身定义为必须靠左（比如岗位维度），
            // 那么标识index为0，即为行的最左边
            if(ctrl._sAlign == 'LEFT'){
                flag = 0;
                break;
            }
            // 如果被拖拽的是维度，并且是往指标上拖，那么默认放到最左
            if(ctrl._sClazz == 'DIM' && control._sName == 'COLUMN'){
                flag = 0;
                break;
            }
            // 如果被拖拽的是指标，并且是往指标上拖，那么不允许将其插入到维度前面或者维度中间
            if(ctrl._sClazz == 'IND' && control._sName == 'COLUMN' && item._sClazz == 'DIM'){
                continue;
            }

            if (
                event.pageX <= dom.getPosition(el).left + item.getWidth()
                && event.pageY <= dom.getPosition(el).top + item.getHeight()
                && !item._bDragging
            ) {
                flag = index;
                break;
            }
        }
        return flag;
    };

}) ();