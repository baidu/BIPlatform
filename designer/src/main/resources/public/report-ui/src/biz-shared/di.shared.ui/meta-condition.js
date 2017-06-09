/**
 * di.shared.ui.MetaCondition
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    多维分析报表原数据选择面板
 * @author:  sushuang(sushuang)
 * @depend:  xui, xutil
 */

$namespace('di.shared.ui');

(function () {
    
    //------------------------------------------
    // 引用 
    //------------------------------------------

    var DICT = di.config.Dict;
    var UTIL = di.helper.Util;
    var inheritsObject = xutil.object.inheritsObject;
    var addClass = xutil.dom.addClass;
    var ecuiDispose = UTIL.ecuiDispose;
    var extend = xutil.object.extend;
    var q = xutil.dom.q;
    var bind = xutil.fn.bind;
    var objKey = xutil.object.objKey;
    var template = xutil.string.template;
    var ecuiCreate = UTIL.ecuiCreate;
    var LINKED_HASH_MAP = xutil.LinkedHashMap;
    var UI_BUTTON = ecui.ui.Button;
    var UI_DROPPABLE_LIST;
    var UI_DRAGPABLE_LIST;
    var getByPath = xutil.object.getByPath;
    var getUID = xutil.uid.getIncreasedUID;
    var XVIEW = xui.XView;
    var META_CONDITION_MODEL;
    var DIM_SELECT_PANEL;
        
    $link(function () {
        UI_DROPPABLE_LIST = getByPath('ecui.ui.DroppableList');
        UI_DRAGPABLE_LIST = getByPath('ecui.ui.DraggableList');
        META_CONDITION_MODEL = di.shared.model.MetaConditionModel;
        DIM_SELECT_PANEL = di.shared.ui.DimSelectPanel;
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
    var META_CONDITOIN = $namespace().MetaCondition = 
        inheritsObject(
            XVIEW,
            function (options) {
                createModel.call(this, options);
                createView.call(this, options);
            }
        );
    var META_CONDITOIN_CLASS = META_CONDITOIN.prototype;
    
    //------------------------------------------
    // 模板 
    //------------------------------------------

    var TPL_MAIN = [
        '<div class="#{css}-src">',
            '<div class="#{css}-ind">',
                '<div class="#{css}-head-text">选择指标：</div>',
                '<div class="#{css}-ind-line q-di-meta-ind"></div>',
            '</div>',
            '<div class="#{css}-dim">',
                '<div class="#{css}-head-text">选择维度：</div>',
                '<div class="#{css}-dim-line q-di-meta-dim"></div>',
            '</div>',
        '</div>',
        '<div class="#{css}-btns">',
            '#{btns}',
        '</div>',
        '<div class="#{css}-tar q-di-meta-tar"></div>'
    ].join('');

    var TPL_SEL_LINE = [
        '<div class="#{css}-sel">',
            '<div class="#{css}-head-text">#{txt}（#{selLineName}）：</div>',
            '<div class="#{css}-sel-line q-di-meta-sel-line"></div>',
            '#{delBtn}',
        '</div>'
    ].join('');

    var TPL_CHART_BTNS = [
        '<div class="#{css}-add-line-btn ui-button-g ui-button">增加系列组</div>',
    ].join('');

    //------------------------------------------
    // 方法
    //------------------------------------------

    /**
     * 创建Model
     *
     * @private
     * @param {Object} options 参数
     */
    function createModel(options) {
        /**
         * 类型，TABLE 或者 CHART
         *
         * @type {string}
         * @private
         */
        this._sReportType = options.reportType || 'RTPL_OLAP_TABLE';
        /**
         * 得到公用的请求参数
         *
         * @type {Function}
         * @private
         */
        this._fCommonParamGetter = options.commonParamGetter;

        this._mMetaConditionModel = new META_CONDITION_MODEL(
            { 
                reportType: this._sReportType,
                commonParamGetter: this._fCommonParamGetter
            }
        );
    };
    
    /**
     * 创建View
     *
     * @private
     * @param {Object} options 参数
     */
    function createView(options) {
        var el = this._eMain = options.el;
        var css = 'meta-condition';
        var reportType = this._sReportType;
        addClass(el, css);

        // 模板
        el.innerHTML = template(
            TPL_MAIN, 
            { 
                css: css,
                btns: reportType == 'RTPL_OLAP_CHART'
                    ? template(TPL_CHART_BTNS, { css: css })
                    : ''
            }
        );

        // selLine控件集合，key为selLineName
        this._oSelLineWrap = new LINKED_HASH_MAP();
        // selLine控件id集合，key为selLineName
        this._oSelLineIdWrap = {};

        this._aDelSelLineBtn = [];

        if (reportType == 'RTPL_OLAP_CHART') {
            this._uAddLineBtn = ecuiCreate(
                UI_BUTTON, 
                q(css + '-add-line-btn', el)[0], 
                null, 
                { primary: 'ui-button-g' }
            );
        }
    };
    
    /**
     * 初始化
     *
     * @public
     */
    META_CONDITOIN_CLASS.init = function () {
        var me = this;
        
        // 事件绑定
        this._mMetaConditionModel.attach(
            ['sync.preprocess.META_DATA', this.disable, this, 'META_COND'],
            ['sync.result.META_DATA', this.$renderMain, this],
            ['sync.error.META_DATA', this.$handleMetaError, this],
            ['sync.complete.META_DATA', this.enable, this, 'META_COND'],
            ['sync.preprocess.ADD_SERIES_GROUP', this.disable, this, 'META_COND'],
            ['sync.result.ADD_SERIES_GROUP', this.$renderMain, this],
            ['sync.error.ADD_SERIES_GROUP', this.$handleMetaError, this],
            ['sync.complete.ADD_SERIES_GROUP', this.enable, this, 'META_COND'],
            ['sync.preprocess.REMOVE_SERIES_GROUP', this.disable, this, 'META_COND'],
            ['sync.result.REMOVE_SERIES_GROUP', this.$renderMain, this],
            ['sync.error.REMOVE_SERIES_GROUP', this.$handleMetaError, this],
            ['sync.complete.REMOVE_SERIES_GROUP', this.enable, this, 'META_COND']
        );        
        this._mMetaConditionModel.attach(
            ['sync.preprocess.SELECT', this.disable, this, 'META_COND'],
            ['sync.result.SELECT', this.$refreshStatus, this],
            ['sync.error.SELECT', this.$handleSelectError, this],
            ['sync.complete.SELECT', this.enable, this, 'META_COND']
        );

        if (this._sReportType == 'RTPL_OLAP_CHART') {
            this._uAddLineBtn.onclick = function () {
                me._mMetaConditionModel.sync('ADD_SERIES_GROUP');
            }
        }

        this._mMetaConditionModel.init();
    };

    /**
     * @override
     */
    META_CONDITOIN_CLASS.dispose = function () {
        this.$disposeMeta();
        this._uAddLineBtn && this._uAddLineBtn.dispose();
        META_CONDITOIN.superClass.dispose.call(this);
    };

    /**
     * 清空selline区域
     *
     * @private
     */
    META_CONDITOIN_CLASS.$disposeMeta = function () {
        var el = this._eMain;
        this._uIndSrc && ecuiDispose(this._uIndSrc);
        this._uDimSrc && ecuiDispose(this._uDimSrc);
        q('q-di-meta-ind', el)[0].innerHTML = '';
        q('q-di-meta-dim', el)[0].innerHTML = '';
        this._oSelLineWrap.foreach(
            function (name, item, index) {
                ecuiDispose(item);
            }
        );
        this._oSelLineWrap.cleanWithoutDefaultAttr();
        for (var i = 0, btn; btn = this._aDelSelLineBtn[i]; i ++) {
            btn.dispose();
        }
        this._aDelSelLineBtn = [];
        this._oSelLineIdWrap = {};
        q('q-di-meta-tar', el)[0].innerHTML = '';
    };

    /**
     * 从后台获取数据并渲染
     *
     * @public
     */
    META_CONDITOIN_CLASS.sync = function () {
        this._mMetaConditionModel.sync(
            { datasourceId: 'META_DATA' }
        );
    };

    /**
     * 得到Model
     * 
     * @public
     * @return {di.shared.model.MetaConditionModel} metaItem
     */
    META_CONDITOIN_CLASS.getModel = function () {  
        return this._mMetaConditionModel;
    };

    /**
     * 渲染主体
     * 
     * @protected
     */
    META_CONDITOIN_CLASS.$renderMain = function () {
        var me = this;
        var el = this._eMain;

        // 先清空
        this.$disposeMeta();

        // 指标维度
        var sourceEcuiId = [
            '\x06_DI_META_COND_IND' + getUID('DI_META_COND'),
            '\x06_DI_META_COND_DIM' + getUID('DI_META_COND')
        ];
        var inddim = this._mMetaConditionModel.getIndDim();
        // 图的情况，可以重复拖动
        var disableSelected = this._sReportType == 'RTPL_OLAP_TABLE';

        // 指标控件
        var indSrc = this._uIndSrc = ecuiCreate(
            UI_DRAGPABLE_LIST,
            q('q-di-meta-ind', el)[0],
            null,
            {
                id: sourceEcuiId[0],
                disableSelected: disableSelected,
                clazz: 'IND'
            }
        );
        inddim.indList.foreach(
            function (uniqName, item) {
                indSrc.addItem(
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
        var dimSrc = this._uDimSrc = ecuiCreate(
            UI_DRAGPABLE_LIST,
            q('q-di-meta-dim', el)[0],
            null,
            {
                id: sourceEcuiId[1],
                disableSelected: disableSelected,
                clazz: 'DIM'
            }
        );
        inddim.dimList.foreach(
            function (uniqName, item) {
                dimSrc.addItem(
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

        // 增加默认的selLine
        var selLineDataWrap = this._mMetaConditionModel.getSelLineWrap();
        selLineDataWrap.foreach(
            function (name, selLineData, index) {
                me.$addSelLine(
                    name,
                    me.$getSelLineTitle(name),
                    sourceEcuiId.join(','),
                    selLineData
                );
            }
        );

        // 事件绑定
        this._uIndSrc.onchange = bind(
            this.$handleSelLineChange, 
            this, 
            this._uIndSrc
        );
        this._uDimSrc.onchange = bind(
            this.$handleSelLineChange, 
            this, 
            this._uDimSrc
        ); 
        this._oSelLineWrap.foreach(
            function (selLineName, selLineCon) {
                selLineCon.onitemclick = bind(
                    me.$handleItemClick, 
                    me, 
                    selLineName
                );
            }
        );

        // 更新控件的元数据状态
        this.$refreshStatus();
    };

    /**
     * 增加选择行
     * 
     * @protected
     * @param {string} selLineName 名
     * @param {string} selLineTitle selLine显示名
     */
    META_CONDITOIN_CLASS.$getSelLineTitle = function (selLineName) {
        var text = '';
        if (this._sReportType == 'RTPL_OLAP_TABLE') {
            if (selLineName == 'ROW') {
                text = '行';
            }
            else if (selLineName == 'FILTER') {
                text = '过滤';
            } 
            else {
                text = '列';
            }
        }
        else {
            if (selLineName == 'ROW') {
                text = '轴';
            }
            else if (selLineName == 'FILTER') {
                text = '过滤';
            } 
            else {
                text = '系列组';
            }
        }   
        return text;     
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
    META_CONDITOIN_CLASS.$addSelLine = function (
        selLineName, selLineTitle, source, selLineData
    ) {
        if (selLineName == null) {
            return;
        }
        var me = this;
        var selLineWrap = this._oSelLineWrap;
        var selLineIdWrap = this._oSelLineIdWrap;
        var useDelBtn = this._sReportType == 'RTPL_OLAP_CHART' 
            && selLineName.indexOf('COLUMN') == 0;

        // 增加selLine
        var o = document.createElement('div');
        o.innerHTML = template(
            TPL_SEL_LINE, 
            { 
                css: 'meta-condition', 
                txt: selLineTitle, 
                selLineName: selLineName,
                delBtn: useDelBtn
                    ? '<span class="ui-button">删除</span>' 
                    : ''
            }
        );
        q('q-di-meta-tar', this._eMain)[0].appendChild(o = o.firstChild);

        if (useDelBtn) {
            // 删除系列组按钮
            var btn = ecuiCreate(
                UI_BUTTON, 
                q('ui-button', o)[0], 
                null, 
                { primary: 'ui-button' }
            );

            btn.onclick = function () {
                me._mMetaConditionModel.sync(
                    { 
                        datasourceId: 'REMOVE_SERIES_GROUP', 
                        args: { selLineName: selLineName } 
                    }
                );
            }

            this._aDelSelLineBtn.push(btn);
        }

        selLineWrap.addLast(
            ecuiCreate(
                UI_DROPPABLE_LIST, 
                q('q-di-meta-sel-line', o)[0],
                null,
                {
                    id: selLineIdWrap[selLineName] = 
                        '\x06_DI_META_COND_SEL' + getUID('DI_META_COND'),
                    source: source,
                    configBtn: true
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
                            align: item.align
                        }
                    );
                }
            );
        }
    };

    /**
     * 更新控件的元数据状态
     *
     * @protected
     */
    META_CONDITOIN_CLASS.$refreshStatus = function () {
        var statusWrap = this._mMetaConditionModel.getStatusWrap();
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
    };

    /**
     * 解禁操作
     *
     * @protected
     * @param {string} key 禁用者的标志
     */
    META_CONDITOIN_CLASS.enable = function (key) {
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
            for (var i = 0, btn; btn = this._aDelSelLineBtn[i]; i ++) {
                btn.enable();
            }
            this._uAddLineBtn && this._uAddLineBtn.enable();
            META_CONDITOIN.superClass.enable.call(this);
        }
    };    

    /**
     * 禁用操作
     *
     * @protected
     * @param {string} key 禁用者的标志
     */
    META_CONDITOIN_CLASS.disable = function (key) {
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
            for (var i = 0, btn; btn = this._aDelSelLineBtn[i]; i ++) {
                btn.disable();
            }
            this._uAddLineBtn && this._uAddLineBtn.disable();
        }
        META_CONDITOIN.superClass.disable.call(this);
    };    

    /**
     * 获取元数据选择处理
     * 
     * @protected
     */
    META_CONDITOIN_CLASS.$handleSelLineChange = function () {
        var wrap = {};
        this._oSelLineWrap.foreach(
            function (k, o, index) {
                wrap[k] = o.getValue();
            }
        );
        var changeWrap = this._mMetaConditionModel.diffSelected(wrap);

        this._mMetaConditionModel.sync(
            {
                datasourceId: 'SELECT',
                args: {
                    uniqNameList: wrap[name],
                    changeWrap: changeWrap
                }
            }
        );
    };

    /**
     * selLine上指标维度点击事件处理
     * 
     * @protected
     */
    META_CONDITOIN_CLASS.$handleItemClick = function (
        selLineName, event, itemData
    ) {
        var metaItem = 
            this._mMetaConditionModel.getMetaItem(itemData.value);

        // 维度--打开维度选择面板
        if (metaItem && metaItem.clazz == 'DIM') {
            DIM_SELECT_PANEL().open(
                'EDIT',
                {
                    uniqName: itemData.value,
                    reportType: this._sReportType,
                    selLineName: selLineName,
                    dimMode: metaItem.isTimeDim ? 'TIME' : 'NORMAL',
                    commonParamGetter: this._fCommonParamGetter
                }
            );
        }
        // 指标--打开指标设置面板
        else {
            // TODO
        }
    };

    /**
     * 获取元数据初始化错误处理
     * 
     * @protected
     */
    META_CONDITOIN_CLASS.$handleMetaError = function () {
        // TODO
    };

    /**
     * 元数据拖拽错误处理
     * 
     * @protected
     */
    META_CONDITOIN_CLASS.$handleSelectError = function () {
        // TODO
    };

})();