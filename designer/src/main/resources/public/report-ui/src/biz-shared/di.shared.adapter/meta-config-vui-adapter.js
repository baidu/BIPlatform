/**
 * di.shared.adapter.MetaConfigVUIAdapter
 * Copyright 2013 Baidu Inc. All rights reserved.
 *
 * @file:    元数据选择控件的适配器
 * @author:  sushuang(sushuang)
 * @depend:  xui, xutil, ecui
 */

$namespace('di.shared.adapter');

(function () {

    var dateToString = xutil.date.dateToString;
    var isArray = xutil.lang.isArray;
    var DICT = di.config.Dict;
    var markParamMode;

    $link(function () {
        markParamMode = di.shared.model.CommonParamFactory.markParamMode;
    });

    /**
     * 元数据选择控件的适配器
     *
     * @public
     * @param {Object} def vui的定义
     * @param {Object} options vui实例创建参数
     * @return {Object} vui adapter实例
     */
    $namespace().MetaConfigVUIAdapter = function (def, options) {
        var clzKey = def.clzKey;

        return {
            create: CREATE_METHOD[clzKey],
            setData: SET_DATA_METHOD[clzKey],
            updateData: UPDATE_DATA_METHOD[clzKey],
            getValue: GET_VALUE_METHOD[clzKey]
        };
    };

    /**
     * 创建
     *
     * @public
     * @param {Object} def vui定义
     * @param {Object} options 初始化参数
     * @param {string} options.start 开始时间，
     *                      绝对值（如2012-12-12）
     *                      或相对于系统时间的偏移（如-5d）
     * @param {string} options.end 结束时间，格式同上。如果和range同时存在，则end优先
     * @param {string} options.range 区间，相对于start的偏移（如-4d）
     * @param {string} options.defaultTime 默认时间
     * @return {Object} 创建的实例
     */
    var CREATE_METHOD = {
        OLAP_META_DRAGGER: create4Dragger,
        OLAP_META_IND_SELECT: create4Select,
        OLAP_META_IND_MULTI_SELECT: create4Select
    };

    function create4Dragger(def, options) {
        var ins = new def.clz(options)            
        ins.$di('registerEventAgent', 'change');

        ins.attach(
            'sellinechange', 
            function (wrap) {
                ins.$di('dispatchEvent', 'change', [wrap]);
            }
        );
        ins.attach(
            'selitemchange', 
            function (wrap) {
                ins.$di('dispatchEvent', 'change', [wrap]);
            }
        );
        return ins;
    }

    function create4Select(def, options) {
        var ins = new def.clz(options);
        ins.$di('registerEventAgent', 'change');

        ins.attach(
            'change', 
            function (wrap) {
                // 标志用JSON传输
                markParamMode(wrap, 'JSON');
                ins.$di('dispatchEvent', 'change', [wrap]);
            }
        );
        return ins;
    }

    /**
     * 设置初始化数据
     * 
     * @public
     * @param {Object} data 数据
     */
    var SET_DATA_METHOD = {
        OLAP_META_DRAGGER: setDataMethod,
        OLAP_META_IND_SELECT: setDataMethod,
        OLAP_META_IND_MULTI_SELECT: setDataMethod
    };

    function setDataMethod(data) {
        this.setData(data);
    }

    /**
     * 更新当前选中数据
     *
     * @public
     * @this {Object} 目标实例
     * @return {Object} 数据
     */
    var UPDATE_DATA_METHOD = {
        OLAP_META_DRAGGER: function (data) {
            this.refreshStatus(data);
        },
        OLAP_META_IND_SELECT: function (data) {
            // do nothing
        },
        OLAP_META_IND_MULTI_SELECT: function (data) {
            // do nothing
        }
    };

    /**
     * 获得当前选中数据
     *
     * @public
     * @this {Object} 目标实例
     * @return {Object} 数据
     */
    var GET_VALUE_METHOD = {
        OLAP_META_DRAGGER: function (data) {
            return this.getValue();
        },
        OLAP_META_IND_SELECT: function (data) {
            var val = this.getValue();
            markParamMode(val, 'JSON');
            return val;
        },
        OLAP_META_IND_MULTI_SELECT: function (data) {
            var val = this.getValue();
            markParamMode(val, 'JSON');
            return val;
        }
    };

})();

