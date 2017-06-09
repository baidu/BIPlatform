/**
 * di.shared.ui.OlapMetaDragger
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    多维分析报表元数据拖拽
 * @author:  sushuang(sushuang)
 * @depend:  xui, xutil
 */

$namespace('di.shared.vui');

(function () {
    
    //------------------------------------------
    // 引用 
    //------------------------------------------

    var UTIL = di.helper.Util;
    var inheritsObject = xutil.object.inheritsObject;
    var addClass = xutil.dom.addClass;
    var extend = xutil.object.extend;
    var q = xutil.dom.q;
    var bind = xutil.fn.bind;
    var objKey = xutil.object.objKey;
    var template = xutil.string.template;
    var LINKED_HASH_MAP = xutil.LinkedHashMap;
    var getByPath = xutil.object.getByPath;
    var getUID = xutil.uid.getIncreasedUID;
    var XOBJECT = xui.XObject;
    var UI_DROPPABLE_LIST;
    var UI_DRAGPABLE_LIST;
    var MULTIDIM_SELECT_PANEL;
    var ecuiCreate = UTIL.ecuiCreate;
    var ecuiDispose = UTIL.ecuiDispose;

    $link(function () {
        UI_DROPPABLE_LIST = getByPath('ecui.ui.DroppableList');
        UI_DRAGPABLE_LIST = getByPath('ecui.ui.DraggableList');
        MULTIDIM_SELECT_PANEL = di.shared.ui.MultiDimSelectPanel;
    });
    
    //------------------------------------------
    // 类型声明 
    //------------------------------------------

    /**
     * 元数据（指标维度）条件拖动选择
     * 
     * @class
     * @extends xui.XView
     * @param {Object} options
     * @param {HTMLElement} options.el 容器元素
     * @param {Object} options.reportType 类型，
     *          TABLE(默认)或者CHART
     * @param {Function=} options.commonParamGetter 公共参数获取     
     */
    var OLAP_META_DRAGGER = $namespace().OlapMetaDragger = 
            inheritsObject(XOBJECT, constructor);
    var OLAP_META_DRAGGER_CLASS = OLAP_META_DRAGGER.prototype;
    
    //------------------------------------------
    // 模板 
    //------------------------------------------

    var TPL_MAIN = [
        '<div class="meta-condition-src">',
            '<div class="meta-condition-ind">',
                '<div class="meta-condition-head-text">选择指标：</div>',
                '<div class="meta-condition-ind-line q-di-meta-ind"></div>',
            '</div>',
            '<div class="meta-condition-dim">',
                '<div class="meta-condition-head-text">选择维度：</div>',
                '<div class="meta-condition-dim-line q-di-meta-dim"></div>',
            '</div>',
        '</div>',
        '<div class="meta-condition-tar q-di-meta-tar">',
        '</div>'
    ].join('');

    var TPL_SEL_LINE = [
        '<div class="meta-condition-sel">',
            '<div class="meta-condition-head-text">#{0}</div>',
            '<div class="meta-condition-sel-line q-di-meta-sel-line"></div>',
        '</div>'
    ].join('');

    var DEFAULT_SEL_LINE_TITLE = {
        ROW: '维度：',
        FILTER: '条件：',
        COLUMN: '指标：'
    };


    //------------------------------------------
    // 方法
    //------------------------------------------

    /**
     * 创建Model
     *
     * @private
     * @param {Object} options 参数
     */
    function constructor(options) {
        var el = this._eMain = options.el;
        addClass(el, 'meta-condition');

        // 模板
        el.innerHTML = TPL_MAIN;

        this._sReportType = options.reportType || 'RTPL_OLAP_TABLE';
        
        // 控件/DOM引用
        this._eSelLineArea = q('q-di-meta-tar', el)[0];

        // selLine控件集合，key为selLineName
        this._oSelLineWrap = new LINKED_HASH_MAP();
        // selLine控件id集合，key为selLineName
        this._oSelLineIdWrap = {};
    };
    
    /**
     * 初始化
     *
     * @public
     */
    OLAP_META_DRAGGER_CLASS.init = function () {
    };

    /**
     * 设置数据
     *
     * @public
     * @param {Object} data 数据
     * @param {Object} data.inddim
     *      控件所需的item 的数据结构
     *      {string} uniqName （相当于控件的value）
     *      {string} caption （相当于控件的text）
     *      {string} clazz （标志是'IND'还是'DIM'）
     *      {boolean} fixed 是否固定
     *      {string} align item居左（'LEFT'）还是居右（'RIGHT'）
     * @param {Object} data.selLineDataWrap
     * @param {Object=} data.selLineTitleDef 标题定义，
     *      形如{ ROW: '行', COLUMN: '列, FITER: '过滤' }，
     *      为空则取默认。
     * @param {Object=} data.rule 拖拽规则
     *      FIXME
     *      这些规则配置没有实现，后续重构规则配置
     *      {Object=} data.rule.IND 指标规则
     *      {Object=} data.rule.DIM 维度规则
     *          规则项有：
     *              {Array.<string>} dropPos 可下落的位置（null则全可下落）
     *                  每项值可为'COL'\'ROW'\'FILTER'
     *      {Object=} data.rule.COL 列规则
     *      {Object=} data.rule.ROW 行规则
     *      {Object=} data.rule.FILTER 过滤器规则
     *          规则项有：
     *              {boolean} canEmpty 是否可为空（默认true）
     *              {boolean} draggable 是否可拖拽（默认true）
     *              {boolean} selectable 是否可选择（默认true）
     * @param {boolean} isSilent
     */
    OLAP_META_DRAGGER_CLASS.setData = function (data, isSilent) {
        this._oData = data || {};
        this._mModel = data.model;
        this._oRule = data.rule || {};
        !isSilent && this.render();
    };

    /**
     * 渲染
     *
     * @public
     */
    OLAP_META_DRAGGER_CLASS.render = function () {
        var me = this;
        var el = this._eMain;
        var data = this._oData;

        // 清空
        this.$disposeInner();

        // 指标维度
        var sourceEcuiId = [
            '\x06_DI_META_COND_IND' + getUID('DI_META_COND'),
            '\x06_DI_META_COND_DIM' + getUID('DI_META_COND')
        ];
        var inddim = data.inddim;

        // 指标控件
        this._uIndSrc = ecuiCreate(
            UI_DRAGPABLE_LIST,
            q('q-di-meta-ind', el)[0],
            null,
            {
                id: sourceEcuiId[0],
                disableSelected: true, // 暂禁止重复拖动
                clazz: 'IND'
            }
        );
        inddim.indList.foreach(
            function (uniqName, item) {
                me._uIndSrc.addItem(
                    {
                        value: item.uniqName, 
                        text: item.caption, 
                        clazz: item.clazz,
                        fixed: item.fixed,
                        align: item.align
                    }
                );
            }
        );

        // 维度控件
        this._uDimSrc = ecuiCreate(
            UI_DRAGPABLE_LIST,
            q('q-di-meta-dim', el)[0],
            null,
            {
                id: sourceEcuiId[1],
                disableSelected: true,
                clazz: 'DIM'
            }
        );
        inddim.dimList.foreach(
            function (uniqName, item) {
                me._uDimSrc.addItem(
                    {
                        value: item.uniqName, 
                        text: item.caption, 
                        clazz: item.clazz,
                        fixed: item.fixed,
                        align: item.align,
                        configBtn: item.isConfig
                    }
                );
            }
        );

        // 增加默认的selLine
        data.selLineDataWrap.foreach(
            function (name, selLineData, index) {
                me.$addSelLine(
                    name,
                    (data.selLineTitleDef || DEFAULT_SEL_LINE_TITLE)[
                        name.split('_')[0]
                    ],
                    sourceEcuiId.join(','),
                    selLineData
                );
            }
        );

        // 事件绑定
        this._uIndSrc.onchange = bind(this.$handleSelLineChange, this);
        this._uDimSrc.onchange = bind(this.$handleSelLineChange, this); 
        this._oSelLineWrap.foreach(
            function (selLineName, selLineCtrl) {
                selLineCtrl.onitemclick = bind(
                    me.$handleItemClick, 
                    me, 
                    selLineName
                );

                selLineCtrl.oncheckdroppable = bind(
                    me.$checkSelLineDroppable, me
                );
                selLineCtrl.oncheckdraggable = bind(
                    me.$checkSelLineDraggable, me
                );
            }
        );
    };

    /**
     * @override
     */
    OLAP_META_DRAGGER_CLASS.dispose = function () {
        this.$disposeInner();
        this._eSelLineArea = null;
        OLAP_META_DRAGGER.superClass.dispose.call(this);
    };

    /**
     * 内部清空
     * 
     * @protected
     */
    OLAP_META_DRAGGER_CLASS.$disposeInner = function () {
        if (this._uIndSrc) {
            ecuiDispose(this._uIndSrc);
            this._uIndSrc = null;
        }
        if (this._uDimSrc) {
            ecuiDispose(this._uDimSrc);
            this._uDimSrc = null;
        }
        this._oSelLineWrap.foreach(
            function (name, item, index) {
                ecuiDispose(item);
            }
        );
        this._eSelLineArea.innerHTML = '';
        this._oSelLineWrap.cleanWithoutDefaultAttr();
        this._oSelLineIdWrap = {};
    };

    /**
     * 增加选择行
     * 
     * @protected
     * @param {string} selLineName selLine名
     * @param {string} selLineTitle selLine显示名
     * @param {string} source 来源ecui控件id
     * @param {xutil.LinkedHashMap=} selLineData selLine数据
     */
    OLAP_META_DRAGGER_CLASS.$addSelLine = function (
        selLineName, selLineTitle, source, selLineData
    ) {
        if (selLineName == null) {
            return;
        }
        var selLineWrap = this._oSelLineWrap;
        var selLineIdWrap = this._oSelLineIdWrap;

        // 增加selLine
        var o = document.createElement('div');
        o.innerHTML = template(TPL_SEL_LINE, selLineTitle);
        this._eSelLineArea.appendChild(o = o.firstChild);

        selLineWrap.addLast(
            ecuiCreate(
                UI_DROPPABLE_LIST, 
                q('q-di-meta-sel-line', o)[0],
                null,
                {
                    id: selLineIdWrap[selLineName] = 
                        '\x06_DI_META_COND_SEL' + getUID('DI_META_COND'),
                    source: source,
                    name: selLineName,
                    configBtn: false
                }
            ),
            selLineName
        );

        // 设置新增控件target，并对所有其他selLine设置target
        for (var name in selLineIdWrap) {
            if (name != selLineName) {
                selLineWrap.get(name).addTarget(selLineIdWrap[selLineName]);
            }
            selLineWrap.get(selLineName).addTarget(selLineIdWrap[name]);
        }
        this._uIndSrc.addTarget(selLineIdWrap[selLineName]);
        this._uDimSrc.addTarget(selLineIdWrap[selLineName]);

        // 初始数据
        if (selLineData) {
            selLineData.foreach( 
                function (uniqName, item, index) {
                    selLineWrap.get(selLineName).addItem(
                        {
                            value: item.uniqName, 
                            text: item.caption,
                            clazz: item.clazz,
                            fixed: item.fixed,
                            align: item.align,
                            configBtn: item.isConfig
                        }
                    );
                }
            );
        }
    };

    /**
     * 更新控件的元数据状态
     *
     * @public
     */
    OLAP_META_DRAGGER_CLASS.refreshStatus = function (statusWrap) {
        if (statusWrap) {
            this._uIndSrc.setState(
                { 
                    disable: statusWrap.indMetas.disabledMetaNames,
                    selected: statusWrap.indMetas.selectedMetaNames
                }
            );
            this._uDimSrc.setState(
                { 
                    disable: statusWrap.dimMetas.disabledMetaNames,
                    selected: statusWrap.dimMetas.selectedMetaNames
                }
            );
        }
    };

    /**
     * 解禁操作
     *
     * @protected
     * @param {string} key 禁用者的标志
     */
    OLAP_META_DRAGGER_CLASS.enable = function (key) {
        // TODO 检查
        objKey.remove(this, key);

        if (objKey.size(this) == 0 && this._bDisabled) {
            this._uIndSrc && this._uIndSrc.enable();
            this._uDimSrc && this._uDimSrc.enable();
            this._oSelLineWrap.foreach(
                function (name, item, index) {
                    item.enable();
                }
            );
            OLAP_META_DRAGGER.superClass.enable.call(this);
        }
    };    

    /**
     * 禁用操作
     *
     * @protected
     * @param {string} key 禁用者的标志
     */
    OLAP_META_DRAGGER_CLASS.disable = function (key) {
        objKey.add(this, key);

        // TODO 检查
        if (!this._bDisabled) {
            this._uIndSrc && this._uIndSrc.disable();
            this._uDimSrc && this._uDimSrc.disable();
            this._oSelLineWrap.foreach(
                function (name, item, index) {
                    item.disable();
                }
            );
        }
        OLAP_META_DRAGGER.superClass.disable.call(this);
    };    

    /**
     * 获取元数据选择处理
     * 
     * @protected
     */
    OLAP_META_DRAGGER_CLASS.$handleSelLineChange = function (
        itemData, itemIndex, selLineName, oriItemIndex, oriSelLineName
    ) {
        var wrap = {};
        this._oSelLineWrap.foreach(
            function (k, o, index) {
                wrap[k] = o.getValue();
            }
        );

        var changeWrap = {
            from: oriSelLineName,
            to: selLineName,
            toPosition: itemIndex,
            uniqNameList: [itemData.value]
        };

        // 根据规则修正变化
        // this.$fixSelLineChange(itemData, itemIndex, selLineName, changeWrap);

        /**
         * 选择变化事件
         *
         * @event
         */
        this.notify('sellinechange', [wrap, changeWrap]);
    };

    /**
     * selLine上指标维度点击事件处理
     * 
     * @protected
     */
    OLAP_META_DRAGGER_CLASS.$handleItemClick = function (
        selLineName, event, itemData
    ) {
        var metaItem = 
            this._mModel.getMetaItem(itemData.value);

        // 维度--打开维度选择面板
        if (metaItem && metaItem.clazz == 'DIM') {
            var me = this;
            var wrap = {};
                this._oSelLineWrap.foreach(
                    function (k, o, index) {
                        wrap[k] = o.getValue();
                    }
                );
            MULTIDIM_SELECT_PANEL().open(
                'EDIT',
                {
                    componentId: this.$di('getId').split('.')[1],
                    uniqName: itemData.value,
                    reportType: this._sReportType,
                    selLineName: selLineName,
                    dimMode: metaItem.isTimeDim ? 'TIME' : 'NORMAL',
                    onconfirm: function(){
                        me.notify('selitemchange',[wrap]);
                    },
                    commonParamGetter: this._mModel._fCommonParamGetter
                }
            );
        }
        // 指标--打开指标设置面板
        else {
            // TODO
        }
    };

    /**
     * 从selline中寻找item
     *
     * @private
     * @param {string} clazz 'IND'或者'DIM'
     * @param {string=} selLineName 指定的selLineName，缺省则全局找
     * @param {Item=} exclude 排除
     * @return {Array.<Object>} 每项中含有：
     *          item：查找到的item
     *          selLineName：行名
     *          index：item的index
     */
    OLAP_META_DRAGGER_CLASS.$findItemFromSelLine = function(
        clazz, selLineName, exclude
    ) {
        var ret = [];

        function findInLine(selLineName, selLine) {
            var itemList = selLine.getItems();
            for (var i = 0, item; item = itemList[i]; i ++) {
                if (item != exclude && item.getClazz() == clazz) {
                    ret.push(
                        { 
                            item: item, 
                            selLineName: selLineName, 
                            index: i 
                        }
                    );
                }
            }
        }

        if (selLineName) {
            findInLine(selLineName, this._oSelLineWrap.get(selLineName));
        }
        else {
            this._oSelLineWrap.foreach(findInLine);
        }

        return ret;
    }


    //---------------------------------------------------
    // 拖拽规则(后续重构) FIXME
    //---------------------------------------------------

    /**
     * selLine上检查是否可以drop
     * 
     * @protected
     */
    OLAP_META_DRAGGER_CLASS.$checkSelLineDroppable = function (
        itemData, index, selLineName
    ) {
        var rule = this._oRule;
        // var ruleIND = rule.IND || {};
        // var ruleDIM = rule.DIM || {};

        // 规则 FORBID_1：指标只能拖到列上
        if (itemData.clazz == 'IND' && selLineName.indexOf('COL') < 0) {
            return false;
        }

        // 规则 FORBID_5：维度不能拖到列上
        // if (itemData.clazz == 'DIM' && selLineName.indexOf('COL') >= 0) {
        //     return false;
        // }

        // 规则 FORBID_7：filter不能drop
        // if (selLineName.indexOf('FILTER') >= 0) {
        //     return false;
        // }

        var selLine = this._oSelLineWrap.get(selLineName);

        // 规则 FORBID_4：有align标志的，只能在左或右
        // 这里假设后台来的数据都已经是align正确的，前台仅就拖拽行为进行限制
        var items = selLine.getItems();
        var item;
        if ((
                (item = items[index]) 
                && item.getWrap().align == 'LEFT'
            )
            || (
                (item = items[index - 1]) 
                && item.getWrap().align == 'RIGHT'
            )
        ) {
            return false;
        }

        return true;
    };
    
    /**
     * selLine上检查是否可以drag
     * 
     * @protected
     */    
    OLAP_META_DRAGGER_CLASS.$checkSelLineDraggable = function (
        itemData, index, selLineName
    ) {
        var rule = this._oRule;

        // 规则 FORBID_2：禁止指标维度全部拖空
        var selLine = this._oSelLineWrap.get(selLineName);
        if (selLine.count() <= 1) {
            if (rule.forbidColEmpty && selLineName.indexOf('COL') >= 0) {
                return false;
            }
            if (rule.forbidRowEmpty && selLineName.indexOf('ROW') >= 0) {
                return false;
            }
        }

        // 规则 FORBID_3：有fixed标志的，不能拖走
        if (itemData.fixed) {
            return false;
        }

        // 规则 FORBID_6：filter不能操作（禁止拖动、放大镜）
        // if (selLineName.indexOf('FILTER') >= 0) {
        //     return false;
        // }

        return true;
    }

    /**
     * 根据规则对拖拽结果进行修正
     * （这段逻辑没有启用，后面会移到后台）
     * 
     * @protected
     * @deprecated
     */
    OLAP_META_DRAGGER_CLASS.$fixSelLineChange = function (
        itemData, itemIndex, selLineName, changeWrap
    ) {
        if (itemIndex == null) {
            // 移除的情况，不作修正
            return;
        }
        
        // 规则 FIX_1：所有指标和计算列，总是连在一起。
        //          （指标和计算列的连带暂未实现）

        // 规则 FIX_2：指标区要么在头部，要么在尾部。

        // 被移动的项是否是计算列
        var isCal = (itemData.calcColumnRefInd || []).length > 0;
        var selLine = this._oSelLineWrap.get(selLineName);
        var selLineItems = selLine.getItems() || [];
        var dragItem = selLineItems[itemIndex];
        var prev = selLineItems[itemIndex - 1];
        var next = selLineItems[itemIndex + 1];
        var prevData = prev && prev.getWrap();
        var nextData = next && next.getWrap();
        var oList;
        var o;
        var des;
        var targetIndex;
        var i;

        // 判断dragItem的两边状况
        var side = { IND: [], DIM: [], WALL: [] };
        prevData 
            ? (side[prevData.clazz][0] = 1)
            : (side.WALL[0] = 1);
        nextData 
            ? (side[nextData.clazz][1] = 1)
            : (side.WALL[1] = 1);

        // IF 拖拽的dragItem是dim
        if (itemData.clazz == 'DIM') {
            // IF dragItem两边都是dim，THEN do nothing

            // IF dragItem一边是ind，另一边是dim，THEN do nothing

            // IF dragItem一边是ind，另一边是墙 
            if (side.IND.length > 0 && side.WALL.length > 0) {
                // THEN 同行所有dim都移入ind区和dragItem间
                oList = this.$findItemFromSelLine('DIM', selLineName, dragItem);                                
                for (i = 0; o = oList[i]; i ++) {
                    this._oSelLineWrap.get(o.selLineName).remove(o.item);
                }
                for (i = 0; o = oList[i]; i ++) {
                    selLine.add(o.item, side.IND[0] ? (selLine.count() - 1) : 1);
                }
            }

            // IF dragItem两边都是ind
            else if (side.IND[0] && side.IND[1]) {
                // THEN 往两边找到dim区，item移入dim区和ind区之间
                // 用首尾判断即可
                des = selLineItems[0].getClazz() == 'DIM';
                for (
                    i = des ? 0 : (selLineItems.length - 1); 
                    o = selLineItems[i]; 
                    i += des ? 1 : -1
                ) {
                    if (o.getClazz() == 'IND') {
                        targetIndex = des ? i : (i + 1);
                        break;
                    }
                }
                selLine.remove(dragItem);
                selLine.add(
                    dragItem, 
                    targetIndex <= itemIndex ? targetIndex : targetIndex - 1
                );
            }
        }

        // IF 拖拽的dragItem是ind
        else if (itemData.clazz == 'IND') {
            // IF dragItem两边都是ind，THEN do nothing

            // IF dragItem一边是ind，另一边是dim，THEN do nothing

            // IF dragItem一边是dim，另一边是墙 
            if (side.DIM.length > 0 && side.WALL.length > 0) {
                // THEN 全局所有ind都移入dim区和dragItem间
                oList = this.$findItemFromSelLine('IND', null, dragItem);
                for (i = 0; o = oList[i]; i ++) {
                    this._oSelLineWrap.get(o.selLineName).remove(o.item);
                }
                for (i = 0; o = oList[i]; i ++) {
                    selLine.add(o.item, side.DIM[0] ? (selLine.count() - 1) : 1);
                }
            }

            // IF dragItem两边都是dim
            else if (side.DIM[0] && side.DIM[1]) {
                // THEN 找到离墙近的那边，把dragItem移动到墙边，
                des = itemIndex > (selLineItems.length - 1) / 2;
                selLine.remove(dragItem);
                selLine.add(dragItem, des ? selLine.count() : 0);
                
                // 再把所有ind移动到dragItem和dragItem之间
                oList = this.$findItemFromSelLine('IND', null, dragItem);
                for (i = 0; o = oList[i]; i ++) {
                    this._oSelLineWrap.get(o.selLineName).remove(o.item);
                }
                for (i = 0; o = oList[i]; i ++) {
                    selLine.add(o.item, des ? (selLine.count() - 1) : 1);
                }
            }
        }

        // 修正changeWrap的toPosition
        selLineItems = selLine.getItems() || [];
        for (i = 0; o = selLineItems[i]; i ++) {
            if (o.getClazz == 'IND') {

            }
        }
    };

})();