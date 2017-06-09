/**
 * ist.opanaly.fcanaly.ui.DimSelectPanel
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    维度选择面板
 * @author:  sushuang(sushuang)
 * @depend:  xui
 */

$namespace('di.shared.ui');

(function() {
    
    //-----------------------------------
    // 引用
    //-----------------------------------
    
    var URL = di.config.URL;
    var DIALOG = di.helper.Dialog;
    var UTIL = di.helper.Util;
    var DICT = di.config.Dict;
    var LANG = di.config.Lang;
    var ecuiCreate = UTIL.ecuiCreate;
    var template = xutil.string.template;
    var q = xutil.dom.q;
    var addClass = xutil.dom.addClass;
    var createSingleton = xutil.object.createSingleton;
    var hasValueNotBlank = xutil.lang.hasValueNotBlank;
    var extend = xutil.object.extend;
    var assign = xutil.object.assign;
    var textLength = xutil.string.textLength;
    var textSubstr = xutil.string.textSubstr;
    var stringToDate = xutil.date.stringToDate;
    var trim = xutil.string.trim;
    var bind = xutil.fn.bind;
    var XVIEW = xui.XView;
    var UI_FORM = ecui.ui.Form;
    var UI_BUTTON = ecui.ui.Button;
    var UI_IND_TREE = ecui.ui.IndTree;
    var UI_CALENDAR = ecui.ui.IstCalendar;
    var DIM_SELECT_MODEL;

    $link(function() {
        DIM_SELECT_MODEL = di.shared.model.DimSelectModel;
    });

    //-----------------------------------
    // 类型声明
    //-----------------------------------

    /**
     * 维度树选择浮层
     * 单例，直接使用DIM_SELECT_PANEL()可得到实例
     * 
     * @class
     * @extends xui.XView
     */
    var DIM_SELECT_PANEL = 
        $namespace().DimSelectPanel = createSingleton(
            XVIEW,
            dimSelectPanelConstructor
        );
    var DIM_SELECT_PANEL_CLASS = DIM_SELECT_PANEL.prototype;

    /**
     * 构造函数
     *
     * @constructor
     * @param {Object} options 参数
     */
    function dimSelectPanelConstructor(options) {
        createModel.call(this, options);
        createView.call(this, options);
        this.init();
    }

    //-----------------------------------
    // 模板
    //-----------------------------------

    var TPL_MAIN = [
            '<div class="q-di-form">',
                '<label>维度选择</label>',
                '<div class="di-dim-select-tree">',
                    '<div class="q-di-tree"></div>',
                '</div>',
                '<div class="di-dim-select-cal">',
                    '<div class="q-calendar"></div>',
                '</div>',
                '<div>',
                    '<div class="di-dim-select-btn">',
                        '<div class="ui-button-g ui-button q-di-submit">确定</div>',
                        '<div class="ui-button q-di-cancel">取消</div>',
                    '</div>',
                '<div>',
            '</div>'
        ].join('');

    //-----------------------------------
    // 方法
    //-----------------------------------

    /**
     * 创建Model
     *
     * @private
     */
    function createModel() {
        this._mDimSelectModel = new DIM_SELECT_MODEL();
    };

    /**
     * 创建控件
     *
     * @private
     */
    function createView() {
        // 创建主dom
        var el = this._eMain = document.createElement('div');
        addClass(el, 'di-dim-select-panel');

        document.body.appendChild(el);
        el.innerHTML = TPL_MAIN;

        // 创建控件
        this._uForm = ecuiCreate(
            UI_FORM,
            q('q-di-form', el)[0],
            null,
            { hide: true }
        );

        this._uDimTree = ecuiCreate(
            UI_IND_TREE,
            q('q-di-tree', el)[0]
        );

        this._uCalendar = ecuiCreate(
            UI_CALENDAR,
            q('q-calendar', el)[0],
            null, 
            {
                mode: 'RANGE',
                viewMode: 'FIX',
                shiftBtnDisabled: true
            }
        );

        this._uSubmitBtn = ecuiCreate(
            UI_BUTTON,
            q('q-di-submit', el)[0]
        );
        this._uCancelBtn = ecuiCreate(
            UI_BUTTON,
            q('q-di-cancel', el)[0]
        );
    };

    /**
     * @override
     */
    DIM_SELECT_PANEL_CLASS.init = function() {
        var me = this;

        // 事件绑定
        this._mDimSelectModel.attach(
            ['sync.preprocess.TREE', this.disable, this],
            ['sync.result.TREE', this.$handleTreeSuccess, this],
            ['sync.error.TREE', this.$handleTreeError, this],
            ['sync.complete.TREE', this.enable, this]
        );
        this._mDimSelectModel.attach(
            ['sync.preprocess.SAVE', this.disable, this],
            ['sync.result.SAVE', this.$handleSubmitSuccess, this],
            ['sync.error.SAVE', this.$handleSubmitError, this],
            ['sync.complete.SAVE', this.enable, this]
        );
        this._uSubmitBtn.onclick = bind(this.$submitHandler, this);
        this._uCancelBtn.onclick = bind(this.$cancelHandler, this);

        // Init
        this._uForm.init();
        this._uDimTree.init();
        this._uSubmitBtn.init();
        this._uCancelBtn.init();
        this._uCalendar.init();

        this._uCalendar.hide();
        // this._uForm.$resize();

        this.$resetInput();
    };
    
    /**
     * @override
     */
    DIM_SELECT_PANEL_CLASS.dispose = function() {
        DIM_SELECT_PANEL.superClass.dispose.call(this);
    };

    /**
     * 打开面板
     *
     * @public
     * @param {string} mode 可取值：
     *                       'VIEW': 查看
     *                       'EDIT': 修改
     * @param {Object} options 参数
     * @param {string=} options.uniqName
     * @param {string} options.selLineName
     * @param {Function} options.commonParamGetter
     * @param {string} options.reportType 值为RTPL_OLAP_TABLE或者RTPL_OLAP_CHART
     * @param {string=} options.dimMode 模式，
     *      可选值为'NORMAL'（默认）, 'TIME'（时间维度面板）
     */
    DIM_SELECT_PANEL_CLASS.open = function(mode, options) {
        this._sMode = mode;
        this._oOptions = options;

        this.$resetInput();

        // 每次打开时从后台获取维度树和当前所选
        this._mDimSelectModel.sync(
            { 
                datasourceId: 'TREE', 
                args: this._oOptions
            }
        );
    };

    /**
     * 重置
     * 
     * @public
     */
    DIM_SELECT_PANEL_CLASS.$resetInput = function() {
        // 清空以及恢复状态
        // 如果后续只有此一行代码则移除此方法直接调用clear prompt
        this.$clearPrompt();
    };

    /**
     * 清除prompt
     *
     * @protected
     */
    DIM_SELECT_PANEL_CLASS.$clearPrompt = function() {
        // TODO
    };

    /**
     * 解禁操作
     *
     * @override
     * @public
     */
    DIM_SELECT_PANEL_CLASS.enable = function(enable) {
        if (this._bDisabled && this._sMode == 'EDIT') {
            this._uSubmitBtn.enable();
            this._uCancelBtn.enable();
            this._uDimTree.enable(); // FIXME 验证
        }
        DIM_SELECT_PANEL.superClass.enable.call(this);
    };    

    /**
     * 禁用操作
     *
     * @override
     * @public
     */
    DIM_SELECT_PANEL_CLASS.disable = function(enable) {
        if (!this._bDisabled) {
            this._uSubmitBtn.disable();
            this._uCancelBtn.disable();
            this._uDimTree.disable(); // FIXME 验证
        }
        DIM_SELECT_PANEL.superClass.disable.call(this);
    };    

    /**
     * 提交事件处理
     *
     * @protected
     * @event
     */
    DIM_SELECT_PANEL_CLASS.$submitHandler = function() {
        this._mDimSelectModel.sync(
            { 
                datasourceId: 'SAVE',
                args: extend(
                    {
                        treeSelected: this._uDimTree.getSelected(),
                        levelSelected: this._uDimTree.getLevelSelected(),
                        timeSelect: {
                            start: this._uCalendar.getDate(),
                            end: this._uCalendar.getDateEnd() 
                        }
                    },
                    this._oOptions
                )
            }
        );
    };

    /**
     * 取消事件处理
     *
     * @protected
     * @event
     */
    DIM_SELECT_PANEL_CLASS.$cancelHandler = function() {
        this._uForm.hide();
    };

    /**
     * 原因添加成功结果处理
     *
     * @protected
     */
    DIM_SELECT_PANEL_CLASS.$handleTreeSuccess = function() {
        try {
            var model = this._mDimSelectModel;

            this._uForm.showModal(DICT.DEFAULT_MASK_OPACITY);

            // 渲染维度树
            this._uDimTree.render(
                {
                    tree: model.getCurrDimTree(),
                    level: model.getCurrLevelList()
                }
            );

            if (this._oOptions.dimMode == 'TIME') {
                this._uCalendar.show();
                var timeSelect = model.getTimeSelect();
                this._uCalendar.setDate(
                    stringToDate(timeSelect.start),
                    stringToDate(timeSelect.end)
                );
            }
            else {
                this._uCalendar.hide();
            }
            
            this._uForm.center();
        }
        catch (e) {
            // 需求变化性很大，数据源很杂，真不敢保证返回数据总是匹配，
            // 所以暂用try catch
            this.$handleTreeError();
        }
    };

    /**
     * 原因添加失败结果处理
     *
     * @protected
     */
    DIM_SELECT_PANEL_CLASS.$handleTreeError = function() {
        var me = this;
        // 获取维度树出错，提示并关闭面板
        DIALOG.alert(
            LANG.GET_DIM_TREE_ERROR,
            function() {
                me._uForm.hide();
            }
        );
    };

    /**
     * 原因添加成功结果处理
     *
     * @protected
     */
    DIM_SELECT_PANEL_CLASS.$handleSubmitSuccess = function() {
        this._uForm.hide();
        /**
         * @event di.shared.ui.DimSelectPanel#submit.close
         */
        this.notify('submit.close');
    };

    /**
     * 原因添加失败结果处理
     *
     * @protected
     */
    DIM_SELECT_PANEL_CLASS.$handleSubmitError = function(status) {
        DIALOG.alert(LANG.SAVE_FAILURE);
    };

})();

