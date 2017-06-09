/*
Pager - 分页控件。
分页控件，配合表格控件使用，翻页时触发change事件，可在其中进行表格数据的更新。

分页控件直接HTML初始化的例子:
<div type="type:pager;pageSize:10;maxNum:40" class="ui-pager"></div>

属性
nPage:      当前的页码(从1开始记数)
nPageSize:  每页的记录数
nTotal:     总记录数

事件
change:     切换了分页

*/
//{if 0}//
(function () {

    var core = ecui,
        dom = core.dom,
        string = core.string,
        array = core.array,
        ui = core.ui,
        util = core.util,

        undefined,
        MATH = Math,

        createDom = dom.create,
        children = dom.children,
        extend = util.extend,
        blank = util.blank,

        $fastCreate = core.$fastCreate,
        inheritsControl = core.inherits,
        triggerEvent = core.triggerEvent,

        UI_CONTROL = ui.Control,
        UI_BUTTON = ui.Button,
        UI_SELECT = ui.Select,
        UI_ITEM = ui.Item,
        UI_ITEMS = ui.Items,
        UI_CONTROL_CLASS = UI_CONTROL.prototype,
        UI_BUTTON_CLASS = UI_BUTTON.prototype,
        UI_ITEM_CLASS = UI_ITEM.prototype,
        UI_SELECT_CLASS = UI_SELECT.prototype;
//{/if}//
//{if $phase == "define"}//
    ///__gzip_original__UI_INPUT_CONTROL
    ///__gzip_original__UI_INPUT_CONTROL_CLASS
    /**
     * 初始化分页控件。
     * options 对象支持的属性如下：
     *      {Number} pageSize   每页的最大记录数
     *      {Number} total      记录总数 
     *      {Number} page      当前页码
     *
     * @public
     *
     * @param {Object} options 初始化选项
     */
    var UI_PAGER = ui.Pager =
        inheritsControl(
            UI_CONTROL,
            'ui-pager',
            function (el, options) {
                var type = this.getTypes()[0],
                    i, len, html = [];
                
                if (!options.showCount || options.showCount < 3) {
                    len = this._nShowCount = 7;
                }
                else {
                    len = this._nShowCount = options.showCount;
                }
                this._bOMSButton = options.omsButton !== false;
                html.push('<div class="' + type + '-button-prv ' + type + '-button">上一页</div><div class="'+ type +'-items">');
                for (i = 0; i < len; i++) {
                    if (i == 1 || i == len - 1) {
                        html.push('<div class="'+ type +'-item-oms" ecui="disabled:true">...</div>');
                    }
                    html.push('<div class="'+ type +'-item"></div>');
                }
                html.push('</div><div class="' + type + '-button-nxt ' + type + '-button">下一页</div>');

                el.innerHTML = html.join('');

            },
            function (el, options) {
                el = children(el);

                this._bResizable = false;
                this._nPage = options.page || 1;
                this._nPageSize = options.pageSize || 50;
                this._nTotal = options.total || 0;

                this._uPrvBtn = $fastCreate(this.Button, el[0], this);
                this.$setBody(el[1]);
                this._uNxtBtn = $fastCreate(this.Button, el[2], this);
                this.$initItems();
            }
        ),
        UI_PAGER_CLASS = UI_PAGER.prototype,
        UI_PAGER_BUTTON = UI_PAGER_CLASS.Button = 
        inheritsControl(
            UI_BUTTON, 
            'ui-pager-button', 
            function (el, options) {
                var type = this.getTypes()[0],
                    o = createDom(type + '-icon');

                el.insertBefore(o, el.firstChild);
            }
        ),
        UI_PAGER_BUTTON_CLASS = UI_PAGER_BUTTON.prototype,
        UI_PAGER_ITEM_CLASS = (UI_PAGER_CLASS.Item = inheritsControl(UI_ITEM, 'ui-pager-item', function (el, options) {
            options.resizeable = false; 
        })).prototype;
//{else}//

    extend(UI_PAGER_CLASS, UI_ITEMS);
    
    /**
     * 分页按钮事件处理函数
     * 根据按钮的step属性确定需要切换的页码
     * @private
     */
    function UI_PAGER_BTN_CLICK(event){
        var par = this.getParent(),
            curIndex = par._nPage,
            maxNum = par.getMaxPage(),
            n = this.getStep();

        UI_CONTROL_CLASS.$click.call(this);

        if (n.charAt(0) == '+') {
            curIndex += parseInt(n.substring(1), 10);
            //+0 尾页
            if (curIndex == par._nPage) {
                curIndex = maxNum;
            }
            else if (curIndex > maxNum) {
                curIndex = par._nPage;
            }
        }
        else if (n.charAt(0) == '-') {
            curIndex -= parseInt(n.substring(1), 10);
            //-0 首页
            if (curIndex == par._nPage) {
                curIndex = 1;
            }
            else if (curIndex < 1) {
                curIndex = par._nPage;
            }
        }
        else {
            curIndex = parseInt(n, 10);
        }

        if (par._nPage != curIndex) {
            triggerEvent(par, 'change', null, [curIndex]);
        }
    }

    /**
     * 控件刷新
     * 根据当前的页码重置按钮
     * @private
     */
    function UI_PAGER_REFRESH(con) {
        var items = con._aPageBtn,
            max = con.getMaxPage(),
            idx = con._nPage,
            showCount = con._nShowCount,
            nHfNum = parseInt(showCount / 2, 10),
            start = idx - nHfNum > 0 ? idx - nHfNum : 1,
            end, i, item;

        if (idx == 1) {
            con._uPrvBtn.disable();
        }
        else {
            con._uPrvBtn.enable();
        }

        if (idx == max || max == 0) {
            con._uNxtBtn.disable();
        }
        else {
            con._uNxtBtn.enable();
        }

        if (start + showCount - 1 > max && max - showCount >= 0) {
            start = max - showCount + 1;
        }
        for (i = 0; item = items[i]; i++) {
            end = start + i;
            item.setContent(end);
            item.setStep(end);
            item.setSelected(idx == end);
            if (end > max) {
                item.hide();
            }
            else {
                item.show();
            }
        }

        UI_PAGER_OMS_REFRESH(con);
    }
   
    /**
     * 刷新more符号按钮
     * @private
     */
    function UI_PAGER_OMS_REFRESH(con) {
        var items = con._aPageBtn,
            omsBtn = con._aOMSBtn,
            max = con.getMaxPage(),
            item;

        if (!con._bOMSButton) {
            return;
        }
        
        if (items[0].getContent() != '1') {
            items[0].setContent(1);
            items[0].setStep(1);
            omsBtn[0].show();
        }
        else {
            omsBtn[0].hide();
        }

        item = items[items.length - 1];
        if (item.isShow() && item.getContent() != max) {
            item.setContent(max);
            item.setStep(max);
            omsBtn[1].show();
        }
        else {
            omsBtn[1].hide();
        }
    }

    UI_PAGER_ITEM_CLASS.$setSize = blank;

    /**
     * 设置页码按钮的选择状态
     * @public
     *
     * @param {Boolean} flag 是否选中
     */
    UI_PAGER_ITEM_CLASS.setSelected = function (flag) {
        this.alterClass((flag ? '+' : '-') + 'selected');
    };

    /**
     * 设置按钮的步进
     * +/-n 向前/后翻n页
     * +0 尾页 -0 首页
     * @public
     *
     * @param {String} n 步进
     */
    UI_PAGER_BUTTON_CLASS.setStep = UI_PAGER_ITEM_CLASS.setStep = function (n) {
        this._sStep = n + '';
    };

    /**
     * 获取步进
     * @public
     *
     * @return {String} 步进
     */
    UI_PAGER_BUTTON_CLASS.getStep = UI_PAGER_ITEM_CLASS.getStep = function () {
        return this._sStep;
    };

    /**
     * @override
     */
    UI_PAGER_BUTTON_CLASS.$click = UI_PAGER_ITEM_CLASS.$click = UI_PAGER_BTN_CLICK;

    /**
     * 得到最大的页数
     * @public
     *
     * @return {Number} 最大的页数
     */
    UI_PAGER_CLASS.getMaxPage = function () {
        return MATH.ceil(this._nTotal / this._nPageSize);
    };

    /**
     * 得到最大的记录数
     * @public
     *
     * @return {Number} 最大的记录数
     */
    UI_PAGER_CLASS.getTotal = function () {
        return this._nTotal;
    };

    /**
     * 得到最大的记录数
     * @public
     *
     * @return {Number} 最大的记录数
     */
    UI_PAGER_CLASS.getTotal = function () {
        return this._nTotal;
    };

    /**
     * 翻页
     * 不会对参数进行有效检查
     * @public
     *
     * @param {Number} i 目标页码
     */
    UI_PAGER_CLASS.go = function (i) {
        this._nPage = i;
        UI_PAGER_REFRESH(this); 
    };

    /**
     * 设置每页的记录数
     * @public
     *
     * @param {Number} num 记录数
     */
    UI_PAGER_CLASS.setPageSize = function (num) {
        this._nPageSize = num;
        this._nPage = 1;
        UI_PAGER_REFRESH(this); 
    };

    /**
     * 设置总记录数
     * @public
     *
     * @param {Number} num 记录数
     */
    UI_PAGER_CLASS.setTotal = function (num) {
        this._nTotal = num;
        this._nPage = 1;
        UI_PAGER_REFRESH(this); 
    };

    /**
     * 初始化函数
     * 初始化设置并根据初始参数设置控件各部件的状态
     *
     * @override
     */
    UI_PAGER_CLASS.init = function () {
        var i, item, items = this.getItems();

        this._uPrvBtn.setStep('-1');
        this._uNxtBtn.setStep('+1');
        this._aOMSBtn = [];
        this._aPageBtn = [];
        UI_CONTROL_CLASS.init.call(this);
        for (i = 0; item = items[i]; i++) {
            item.init();
            if (i == 1 || i == items.length - 2) {
                this._aOMSBtn.push(item);
                item.hide();
            }
            else {
                this._aPageBtn.push(item);
            }
        }
        UI_PAGER_REFRESH(this);
    };

    /**
     * override
     */
    UI_PAGER_CLASS.$setSize = blank;

//{/if}//
//{if 0}//
})();
//{/if}//
