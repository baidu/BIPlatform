/**
 * di.shared.ui.DIForm
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    DI 表单视图组件
 * @author:  sushuang(sushuang)
 * @depend:  xui, xutil
 */

$namespace('di.shared.ui');

(function() {

    //------------------------------------------
    // 引用 
    //------------------------------------------

    var UTIL = di.helper.Util;
    var inheritsObject = xutil.object.inheritsObject;
    var q = xutil.dom.q;
    var bind = xutil.fn.bind;
    /* globals di */
    var DIALOG = di.helper.Dialog;
    /* globals xutil */
    var objKey = xutil.object.objKey;
    var isObject = xutil.lang.isObject;
    var UrlRequest = xutil.url.request;
    var INTERACT_ENTITY = di.shared.ui.InteractEntity;
    var extend = xutil.object.extend;

    //------------------------------------------
    // 类型声明 
    //------------------------------------------

    /**
     * DI 表单视图组件
     *
     * @class
     * @extends xui.XView
     * @param {Object} options
     * @param {string} options.submitMode 提交方式，可选值为
     *      'IMMEDIATE'（输入后立即提交，默认）
     *      'CONFIRM'（按确定按钮后提交）
     * @param {(Object|boolean)=} options.confirmBtn 是否有确认按钮
     *      如果为Object则内容为，{ text: '按钮文字' }
     */
    var DI_FORM = $namespace().DIForm =
        inheritsObject(INTERACT_ENTITY);
    var DI_FORM_CLASS = DI_FORM.prototype;

    /**
     * 定义
     */
    DI_FORM_CLASS.DEF = {
        // 暴露给interaction的api
        exportHandler: {
            sync: { datasourceId: 'DATA' },
            clear: {}
        },
        // 主元素的css
        className: 'di-form',
        // model配置
        model: {
            clzPath: 'di.shared.model.DIFormModel'
        }
    };

    //------------------------------------------
    // 方法
    //------------------------------------------

    /**
     * 创建View
     *
     * @private
     * @param {Object} options 参数
     */
    DI_FORM_CLASS.$createView = function (options) {
        this._oOptions = extend({}, options);
        options.submitMode = options.submitMode || 'IMMEDIATE';

        // 创建参数输入控件
        this._aInput = [];
        for (var i = 0, o; o = this.$di('vuiCreate', 'input.' + i); i ++) {
            this._aInput.push(o);
            // 使用json格式传输数据
            o.$di('setOpt', 'cfgOpt', 'paramMode', 'JSON');
            // 在vui中为clzKey为RANGE_CALENDAR的注册事件
            if (o.$di('getDef').clzKey === 'RANGE_CALENDAR'){
                o.attach('calChangeDate', this.$handleChange, this);
            }
            // 级联下拉框
            if (o.$di('getDef').clzKey === 'CASCADE_SELECT'){
                // 多级下拉框获取下一级内容
                o.attach('cascadeGetNextLevel', this.$cascadeGetNextLevel, this);
                // 多级下拉框加载完毕之后，更新context
                o.attach('cascadeSelectUpdateContext', this.$submit, this);
                // 多级下拉框改变内容之后
                o.attach('cascadeSelectChange', this.$handleChange, this);
            }
        }

        // 创建“确认”控件
        this._uConfirmBtn = this.$di('vuiCreate', 'confirm');
    };

    /**
     * 初始化
     *
     * @public
     */
    DI_FORM_CLASS.init = function() {
        var me = this;
        var i;
        var input;
        var def;
        var cfgOpt;

        // 绑定组件事件
        this.getModel().attach(
            ['sync.preprocess.DATA', this.$syncDisable, this, 'DATA'],
            ['sync.result.DATA', this.$renderMain, this],
            ['sync.error.DATA', this.$handleDataError, this],
            ['sync.complete.DATA', this.$syncEnable, this, 'DATA'],

            ['sync.preprocess.REGULAR', this.$syncDisable, this, 'REGULAR'],
            ['sync.result.REGULAR', this.$renderMain, this],
            ['sync.error.REGULAR', this.$handleDataError, this],
            ['sync.complete.REGULAR', this.$syncEnable, this, 'REGULAR'],

            // ASYNC不加disable，否则suggest框会在disasble的时候动input框，与输入法冲突。            
            ['sync.result.ASYNC_DATA', this.$renderAsync, this],
            ['sync.error.ASYNC_DATA', this.$handleAsyncError, this],

            ['sync.preprocess.UPDATE_CONTEXT', this.$syncDisable, this, 'UPDATE_CONTEXT'],
            ['sync.result.UPDATE_CONTEXT', this.$renderUpdateContext, this],
            ['sync.result.UPDATE_CONTEXT', this.$handleDataLoaded, this],
            ['sync.error.UPDATE_CONTEXT', this.$handleUpdateContextError, this],
            ['sync.complete.UPDATE_CONTEXT', this.$syncEnable, this, 'UPDATE_CONTEXT'],

            ['sync.preprocess.CASCADE_GETLEVEL', this.$syncDisable, this, 'CASCADE_GETLEVEL'],
            ['sync.result.CASCADE_GETLEVEL', this.$cascadeGetNextLevelSuccess, this],
            ['sync.error.CASCADE_GETLEVEL', this.$handleDataError, this],
            ['sync.complete.CASCADE_GETLEVEL', this.$syncEnable, this, 'CASCADE_GETLEVEL']
        );

        // 绑定控件事件
        for (i = 0; input = this._aInput[i]; i ++ ) {
            def = input.$di('getDef');
            cfgOpt = input.$di('getOpt', 'cfgOpt');

            // 改变事件
            if (!cfgOpt.changeSilent) {
                input.$di(
                    'addEventListener',
                    'change',
                    this.$handleChange,
                    this
                );
            }

            // 异步取值事件
            if (cfgOpt.async) {
                input.$di(
                    'addEventListener',
                    'async',
                    this.$handleAsync,
                    this,
                    { bindArgs: [input] }
                );
            }
        }

        if (this._uConfirmBtn) {
            this._uConfirmBtn.onclick = function() {
                me.$submit();
            }
        }

        for (i = 0; input = this._aInput[i]; i ++ ) {
            input.$di('init');
        }
    };

    /**
     * @override
     */
    DI_FORM_CLASS.dispose = function() {
        for (var i = 0, input; input = this._aInput[i]; i ++ ) {
            input.$di('dispose');
        }
        DI_FORM.superClass.dispose.call(this);
    };

    /**
     * 从后台获取数据并渲染
     *
     * @public
     */
    DI_FORM_CLASS.sync = function(options) {

        // 视图禁用
        /*
         var diEvent = this.$di('getEvent');
         var vd = diEvent.viewDisable;
         vd && this.getModel().attachOnce(
         ['sync.preprocess.DATA',  vd.disable],
         ['sync.complete.DATA', vd.enable]
         );*/
        var reportType = this.$di('getDef').reportType;
        // var taskId = UrlRequest('taskId');
        var url = window.location.href;
        var taskId = url.replace(/(^.*regular\/)|(\/report_vm.*$)/g, '');
        var args = {};
        if (taskId) {
            args.taskId = taskId;
        }
        // 初始化参数
        var paramList = [];
        for (var i = 0, input; i < this._aInput.length; i ++) {
            input = this._aInput[i];
            paramList.push(input.$di('getDef').name);
        }
        args.paramList = paramList.join(',');
        reportType && (args.reportType = reportType);

        if (reportType && reportType === 'REGULAR') {
            this.$sync(
                this.getModel(),
                'REGULAR',
                args,
                null,
                this.$di('getEvent')
            );
        }
        else {
            this.$sync(
                this.getModel(),
                'DATA',
                args,
                null,
                this.$di('getEvent')
            );
        }
    };

    /**
     * 清空视图
     *
     * @public
     */
    DI_FORM_CLASS.clear = function(options) {
        // TODO
    };

    /**
     * 提交
     *
     * @protected
     */
    DI_FORM_CLASS.$submit = function() {
        this.$sync(
            this.getModel(),
            'UPDATE_CONTEXT',
            buildContextParam(this)
        );
    };

    /**
     * 渲染主体
     *
     * @protected
     */
    DI_FORM_CLASS.$renderMain = function(data, ejsonObj, options) {
        var setDataOpt = { diEvent: this.$diEvent(options) };
        var inputs = this._aInput;
        var hasCascadeSelect = false;
        // 设置数据并渲染
        for (var i = 0, input, clzKey; i < inputs.length; i++ ) {
            input = inputs[i];
            clzKey = input.$di('getDef').clzKey;
            (clzKey === 'CASCADE_SELECT') && (hasCascadeSelect = true);
            var curData = buildData(ejsonObj.data, input);
            input.$di(
                'setData',
                curData,
                setDataOpt
            );
        }
        if (!hasCascadeSelect) {
            // 如果有级联下拉框，就在级联下拉框框中触发context请求
            this.$sync(
                this.getModel(),
                'UPDATE_CONTEXT',
                buildContextParam(this)
            );
        }
    };

    /**
     * 渲染同步
     *
     * @protected
     */
    DI_FORM_CLASS.$renderAsync = function(data, ejsonObj, options) {
        var args = options.args;
        args.callback(ejsonObj.data[args.input.$di('getDef').name] || {});
    };

    /**
     * 渲染同步
     *
     * @protected
     */
    DI_FORM_CLASS.$renderUpdateContext= function(data, ejsonObj, options) {
        /**
         * 渲染事件
         *
         * @event
         */
        this.$di('dispatchEvent', this.$diEvent('rendered', options));
    };

    /**
     * 窗口改变后重新计算大小
     *
     * @public
     */
    DI_FORM_CLASS.resize = function() {
    };

    /**
     * 解禁操作
     *
     * @protected
     */
    DI_FORM_CLASS.enable = function() {
        for (var i = 0, input; input = this._aInput[i]; i ++) {
            input.$di('enable');
        }
        this._uConfirmBtn && this._uConfirmBtn.$di('enable');
        DI_FORM.superClass.enable.call(this);
    };

    /**
     * 禁用操作
     *
     * @protected
     */
    DI_FORM_CLASS.disable = function() {
        for (var i = 0, input; input = this._aInput[i]; i ++) {
            input.$di('disable');
        }
        this._uConfirmBtn && this._uConfirmBtn.$di('disable');
        DI_FORM.superClass.disable.call(this);
    };

    /**
     * 初始数据加载完成
     *
     * @protected
     */
    DI_FORM_CLASS.$handleDataLoaded = function(data, ejsonObj, options) {
        /**
         * 初始数据加载完成
         *
         * @event
         */
        this.$di(
            'dispatchEvent',
            this.$diEvent('dataloaded', options)
        );
    };

    /**
     * 条件变化事件
     *
     * @event
     * @protected
     */
    DI_FORM_CLASS.$handleChange = function() {
        if (this._oOptions.submitMode == 'IMMEDIATE') {
            this.$submit();
        }
    };

    DI_FORM_CLASS.$cascadeGetNextLevel = function(option) {
        this.$sync(
            this.getModel(),
            'CASCADE_GETLEVEL',
            option.param,
            null,
            {
                callback: option.callback,
                input: option.input
            }
        );
    };

    DI_FORM_CLASS.$cascadeGetNextLevelSuccess = function(status, ejsonObj, options) {
        options.args.callback.call(options.args.input, ejsonObj.data);
    };

    /**
     * 异步取数据事件
     *
     * @event
     * @protected
     */
    DI_FORM_CLASS.$handleAsync = function(input, value, callback) {
        var name = input.$di('getDef').name;
        var arg = {};

        this.$sync(
            this.getModel(),
            'ASYNC_DATA',
            {
                componentId: name,
                uniqueName: value
            },
            null,
            {
                value: value,
                callback: callback,
                input: input
            }
        );
    };

    /**
     * 获取数据错误处理
     *
     * @protected
     */
    DI_FORM_CLASS.$handleDataError = function(status, ejsonObj, options) {
        // 清空视图
        this.clear();

        this.$di('dispatchEvent', this.$diEvent('rendered', options));
    };

    /**
     * 获取数据错误处理
     *
     * @protected
     */
    DI_FORM_CLASS.$handleUpdateContextError = function(status, ejsonObj, options) {
        // 清空视图
        this.clear();

        this.$di('dispatchEvent', this.$diEvent('rendered', options));
    };

    /**
     * 获取async数据错误处理
     *
     * @protected
     */
    DI_FORM_CLASS.$handleAsyncError = function() {
        // TODO
        this.$di('dispatchEvent', 'rendered');
    };
    /**
     * 重组form里面input标签需要的默认数据
     *
     * @private
     * @param {Object} data 数据
     * @param {HTMLElement} el form里面的表单元素
     */
    function buildData(data, el) {
        var curData;
        var def = el.$di('getDef');
        var sourceData = data;
        var defaultData;

        // 如果data存在，再进行赋值
//         if (data) {
//         	sourceData = data.params;
//         	defaultData = data.interactResult;
//         }

        // 如果是时间，把时间默认数据格式重组为{ timeType: 'M' }返回
        if (def.clzKey === 'X_CALENDAR') {
            defaultData && (curData = defaultData[def.name]);

            if (curData && curData.value) {
                curData = {
                    timeType: curData.value.granularity
                };
            }
        }
        else if (def.clzKey === 'ECUI_SELECT') {
            // 如果渲染数据存在，就获取到当前渲染数据
            sourceData && (curData = sourceData[def.name]);
            if (curData && curData.datasource && def.hasAllNode) {
                curData.datasource.unshift({
                    'parent': '',
                    'text': def.hasAllNodeText,
                    'value': ''
                });
                // 如果当前渲染数据存在
                defaultData
                && defaultData[def.name]
                && defaultData[def.name].value
                    // 更新渲染数据里面的value为默认值
                && (curData.value = defaultData[def.name].value);
            }
        }
        else {
            // 如果渲染数据存在，就获取到当前渲染数据
            sourceData && (curData = sourceData[def.name]);
            // 如果当前渲染数据存在
            curData
            && curData.datasource
                // 渲染数据的默认值存在
            && defaultData
            && defaultData[def.name]
            && defaultData[def.name].value
                // 更新渲染数据里面的value为默认值
            && (curData.value = defaultData[def.name].value);
        }

        return curData;
    }

    /**
     * 提交context请求时，需要的form里面的参数
     *
     * @private
     * @param {Object} that DI_FORM_CLASS指向
     */
    function buildContextParam(that) {
        // 提交之前，先更换日历参数的key
        // 应后端要求，日历中的每个粒度的参数key都是不一样的
        var inputs = that._aInput;
        var dateName;
        var dateKey;
        var name;
        var options = {};
        for (var i = 0, input; i < inputs.length; i ++ ) {
            input = inputs[i];
            var clzKey = input.$di('getDef').clzKey;
            if (clzKey === 'X_CALENDAR') {
                dateName = input.$di('getDef').name;
                dateKey = input.$di('getDef').dateKey;
            }
            else if (clzKey === 'RANGE_CALENDAR') {
                dateKey = input.$di('getDef').dateKey.D;
                var dataValue = input.$di('getValue');
                dataValue.start = dataValue.start.replace(new RegExp("/","gm"),'-');
                dataValue.end = dataValue.end.replace(new RegExp("/","gm"),'-');
                options[dateKey] = JSON.stringify(dataValue);
            }
            else if (clzKey === 'CASCADE_SELECT') {
                name = input.$di('getDef').name;
                options[input.$di('getDef').dimId] = that.$di('getValue')[name];
            }
            else {
                if (clzKey !== 'H_BUTTON') {
                    name = input.$di('getDef').name;
                    options[input.$di('getDef').dimId] = that.$di('getValue')[name].join(',');
                }
            }
        }
        if (dateName) {
            var dateParam = that.$di('getValue')[dateName];
            options[dateKey[dateParam.granularity]] = dateParam;
        }
        return options;
    }

})();