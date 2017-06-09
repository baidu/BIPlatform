/**
 * di.shared.vui.OlapMetaSelect
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    元数据的选择
 *           这是下拉框选择，每个系列组（或column）一个下拉框，
 *           因为系列组可能代表不同的图形（柱、折线），所以要分开下拉框选择
 * @author:  sushuang(sushuang)
 * @depend:  xui, xutil
 */

$namespace('di.shared.vui');

(function () {
    
    //------------------------------------------
    // 引用 
    //------------------------------------------

    var UTIL = di.helper.Util;
    var DICT = di.config.Dict;
    var inheritsObject = xutil.object.inheritsObject;
    var extend = xutil.object.extend;
    var encodeHTML = xutil.string.encodeHTML;
    var ecuiCreate = UTIL.ecuiCreate;
    var q = xutil.dom.q;
    var isArray = xutil.lang.isArray;
    var ecuiDispose = UTIL.ecuiDispose;
    var bind = xutil.fn.bind;
    var trim = xutil.string.trim;
    var template = xutil.string.template;
    var getByPath = xutil.object.getByPath;
    var UI_SELECT = ecui.ui.Select;
    var XOBJECT = xui.XObject;

    //------------------------------------------
    // 类型声明 
    //------------------------------------------

    /**
     * 隐藏的输入，用于传递报表引擎外部传来的参数
     * 
     * @class
     * @extends xui.XView
     * @param {Object} options
     * @param {HTMLElement} options.el 容器元素
     */
    var OLAP_META_SELECT = $namespace().OlapMetaSelect = 
            inheritsObject(XOBJECT, constructor);
    var OLAP_META_SELECT_CLASS = OLAP_META_SELECT.prototype;
    
    var TPL_SEL = [
        '<span>',
            '<span class="olap-meta-select-txt">#{colName}</span>',
            '<span class="olap-meta-select-sel"></span>',
        '</span>'
    ].join('');

    //------------------------------------------
    // 方法
    //------------------------------------------

    /**
     * 构造函数
     *
     * @private
     * @param {Object} options 参数
     */
    function constructor(options) {
        this._sels = {};
        this._el = options.el;
    };

    /**
     * 设置数据
     *
     * @public
     * @param {Object} data 数据
     */
    OLAP_META_SELECT_CLASS.setData = function (data) {
        // 如果发现传入的有renderType参数并且renderType为‘liteOlap’，则走setLiteOlapData逻辑
        if(data['renderType'] && (data['renderType'] == 'liteOlap')){
            this.setLiteOlapData(data);
        }
        else {
            var me = this;
            var el = this._el;

            var indList = data.inddim.indList;
            var inds = [];
            indList && indList.foreach(
                function (k, item, index) {
                    if (item.status != DICT.META_STATUS.DISABLED) {
                        inds.push(
                            { 
                                text: item.caption, 
                                value: item.uniqName 
                            }
                        );
                    }
                }
            );

            // 清空内部
            this.$disposeInner();

            // 渲染
            // 获得控件的类型（select或者multiselect，在dataOpt中设置）
            var ctrlClz = getByPath(data.ctrlClz);
            var eo;
            // 如果只有一个col，不显示“系列组”描述（表格就是只有一个的情况）
            var colNum = 0;
            data.selLineDataWrap.foreach(
                function (name, selLineData, index) {
                    if (name.indexOf('COLUMN') == 0) {
                        colNum ++;
                    }
                }
            );

            var seriesCfg = data.seriesCfg;
            data.selLineDataWrap.foreach(
                function (name, selLineData, index) {
                    // 只对系列组有效
                    if (name.indexOf('COLUMN') < 0) {
                        return;
                    }

                    // 创建控件
                    var seriesType = (seriesCfg[name] || {}).type;
                    var desc = colNum <= 1 
                        ? '' 
                        : (
                            '系列组' + name.split('_')[1] 
                            + (
                                seriesType 
                                    ? ('（' + DICT.getGraphByType(seriesType).text + '）')
                                    : ''
                            )
                        );
                    var ctrl = createCtrl(el, ctrlClz, data.ctrlClz, data, desc);
                    me._sels[name] = ctrl;

                    // 绑定事件
                    ctrl.onchange = bind(handleChange, null, me, name);

                    // 取得当前选中
                    var selected = [];
                    selLineData.foreach(function (uniqName) {
                        selected.push(uniqName);
                    });

                    // 设置数据
                    setSelectData(ctrl, inds, selected);
                }
            );  
        }
        
    };


    /**
     * 设置liteOlap数据
     *
     * @public
     * @param {Object} data 数据
     */
    OLAP_META_SELECT_CLASS.setLiteOlapData = function (data) {
        var me = this;
        var el = this._el;

        var indList = data.indList;
        var inds = [];
         // 取得当前选中
        var selected = [];
        for (var i = 0; i < indList.length; i++) {
            inds.push(
                        { 
                            text: indList[i].caption, 
                            value: indList[i].custIndName 
                        }
                    );
            // 如果传入的选中指标有值,那么取选中值给下拉框，如果没值，则取第一个元素
            if(data.selectedInds.length > 0){
                for (var j = 0; j < data.selectedInds.length; j++) {
                    if(data.selectedInds[j] == indList[i].custIndName){
                        selected.push(
                        { 
                            text: indList[i].caption, 
                            value: indList[i].custIndName 
                        }
                    );
                    }
                };
            }else{
                selected.push(
                        { 
                            text: indList[0].caption, 
                            value: indList[0].custIndName 
                        }
                        )
            }
        };

        // 清空内部
        this.$disposeInner();

        // 渲染
        // 获得控件的类型（select或者multiselect，在dataOpt中设置）
        var ctrlClz = getByPath(data.ctrlClz);
        var ctrl = createCtrl(el, ctrlClz, data.ctrlClz, data, '');
        me._sels[data.selLineName] = ctrl;

        // 绑定事件
        ctrl.onchange = bind(handleChange, null, me, name);

       
        // selLineData.foreach(function (uniqName) {
        //     selected.push(uniqName);
        // });

        // 设置数据
        setSelectData(ctrl, inds, selected);
    };
    function handleChange(me, selLineName, value) {
        // 得到的当前值
        var wrap = me.getValue();
        // 设置被change的ctrl
        // wrap[selLineName] = value;

        me.notify('change', [wrap]);
    }

    function createCtrl(el, ctrlClz, ctrlClzPath, data, colName) {
        var eo = document.createElement('DIV');
        // 创建控件
        eo.innerHTML = template(TPL_SEL, { colName: colName });
        var ctrl = ecuiCreate(
            ctrlClz, 
            q('olap-meta-select-sel', eo)[0],
            null,
            {
                primary: ctrlClzPath == 'ecui.ui.MultiSelect'
                    ? 'ui-multi-select' : 'ui-select',
                optionSize: data.optionSize 
            }
        )
        el.appendChild(eo.firstChild);
        // 禁用鼠标事件
        ctrl.$mousewheel = new Function();
        // 用于区别类型
        ctrl.$__ctrlClzPath = trim(ctrlClzPath);
        ctrl.init();
        return ctrl;
    }

    function disposeSelect(ctrl) {
        ecuiDispose(ctrl);
    }
    function setSelectData(ctrl, datasource, selected) {
        // 添加
        for (var i = 0, o; o = datasource[i]; i++) {
            var txt = String(o.text != null ? o.text : '');
            ctrl.add(
                txt, 
                null,
                { value: o.value, prompt: txt }
            );
        }

        // 设置默认选中
        selected.length && ctrl.setValue(
            ctrl.$__ctrlClzPath == 'ecui.ui.MultiSelect'
                ? selected : selected[0]['value']
        ); 
    }

    /**
     * 清空内部
     */
    OLAP_META_SELECT_CLASS.$disposeInner = function () {
        for (var selLineName in this._sels) {
            disposeSelect(this._sels[selLineName]);
        }
        this._sels = {};
        this._el.innerHTML = '';
    };

    /**
     * 得到当前值
     *
     * @public
     * @return {*} 当前数据
     */
    OLAP_META_SELECT_CLASS.getValue = function () {
        var wrap = {};
        for (var selLineName in this._sels) {
            var sel = this._sels[selLineName];
            var value;
            if (sel.$__ctrlClzPath == 'ecui.ui.Select') {
                var sl = sel.getSelected();
                value = sl ? sl.getValue() : null;
            }
            else {
                value = sel.getValue();
            }
            wrap[selLineName] = isArray(value) 
                ? value 
                : (value == null ? [] : [value]);
        }
        return wrap;
    };

})();