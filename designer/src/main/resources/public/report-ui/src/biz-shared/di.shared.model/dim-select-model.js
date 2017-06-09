/**
 * di.shared.model.DimSelectModel  
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    维度选择model
 * @author:  sushuang(sushuang)
 * @depend:  xui, xutil
 */

$namespace('di.shared.model');

(function() {
    
    //------------------------------------------
    // 引用
    //------------------------------------------

    var FORMATTER = di.helper.Formatter;
    var DICT = di.config.Dict;
    var LANG = di.config.Lang;
    var URL = di.config.URL;
    var UTIL = di.helper.Util;
    var extend = xutil.object.extend;
    var getByPath = xutil.object.getByPath;
    var inheritsObject = xutil.object.inheritsObject;
    var q = xutil.dom.q;
    var g = xutil.dom.g;
    var bind = xutil.fn.bind;
    var assign = xutil.object.assign;
    var hasValue = xutil.lang.hasValue;
    var stringToDate = xutil.date.stringToDate;
    var dateToString = xutil.date.dateToString;
    var textParam = xutil.url.textParam;
    var wrapArrayParam = xutil.url.wrapArrayParam;
    var arrayProtoPush = Array.prototype.push;    
    var download = UTIL.download;
    var logError = UTIL.logError;
    var XDATASOURCE = xui.XDatasource;
        
    //------------------------------------------
    // 类型声明
    //------------------------------------------

    /**
     * 维度选择Model
     *
     * @class
     * @extends xui.XDatasource
     */
    var DIM_SELECT_MODEL = 
            $namespace().DimSelectModel = 
            inheritsObject(XDATASOURCE, constructor);
    var DIM_SELECT_MODEL_CLASS = 
            DIM_SELECT_MODEL.prototype;
  
    //------------------------------------------
    // 方法
    //------------------------------------------

    /**
     * 构造方法
     *
     * @private
     * @param {Object} options 
     */
    function constructor(options) {
        /**
         * hierachy的根，子女节点是维度树
         *
         * @type {Array.<Object>}
         * @private
         */
        this._oHierarchyRoot;
        /**
         * 当前hierachy的维度树
         *
         * @type {Array.<Object>}
         * @private
         */
        this._oCurrDimTree;
        /**
         * 维度名
         *
         * @type {string} 
         * @private
         */
        this._sDimName;
        /**
         * schema名
         *
         * @type {string} 
         * @private
         */
        this._sSchemaName;
        /**
         * 维度类型, 目前可能为'TIME'或'NORMAL'
         *
         * @type {string} 
         * @private
         */
        this._sDimType;
        /**
         * 每个hierarchy的层级列表, key为hierarchy的name
         *
         * @type {Map} 
         * @private
         */
        this._oLevelMap;
    }

    var URL_MAP = {
        TREE: {
            RTPL_OLAP_TABLE: URL.fn('DIM_TREE_TABLE'),
            RTPL_OLAP_CHART: URL.fn('DIM_TREE_CHART')
        },
        SAVE: {
            RTPL_OLAP_TABLE: URL.fn('DIM_SELECT_SAVE_TABLE'),
            RTPL_OLAP_CHART: URL.fn('DIM_SELECT_SAVE_CHART')
        }
    };

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DIM_SELECT_MODEL_CLASS.url = function(options) {
        return URL_MAP[options.datasourceId][options.args.reportType]();
    }

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DIM_SELECT_MODEL_CLASS.param = new XDATASOURCE.Set(
        {
            // 请求维度树参数
            TREE: function(options) {
                var paramArr = this.$createBaseParam(options);
                if (options.args.dimMode == 'TIME') {
                    paramArr.push('isTimeDim=true');
                }
                return paramArr.join('&');
            },

            // 保存维度树当前选中参数
            SAVE: function(options) {
                var args = options.args;
                var paramArr = this.$createBaseParam(options);

                paramArr.push(
                    'hierarchyName=' + textParam(this._oCurrDimTree.name)
                );
                arrayProtoPush.apply(
                    paramArr,
                    wrapArrayParam(args.treeSelected, 'selectedNodes')
                );
                arrayProtoPush.apply(
                    paramArr,
                    wrapArrayParam(args.levelSelected, 'levelUniqueNames')
                );

                if (args.dimMode == 'TIME') {
                    // 暂时只支持范围选择
                    var start = args.timeSelect.start
                        ? dateToString(args.timeSelect.start) : '';
                    var end = args.timeSelect.end 
                        ? dateToString(args.timeSelect.end) : start;
                    paramArr.push('startDay=' + start);
                    paramArr.push('endDay=' + end);
                }
                
                return paramArr.join('&');
            }
        }
    );

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DIM_SELECT_MODEL_CLASS.parse = new XDATASOURCE.Set(
        {
            // 请求维度树后台返回解析
            TREE: function(data, ejsonObj, options) {
                try {
                    // timeType表示静态动态时间等，后面加. 0代表默认
                    var timeType = data['timeType'];
                    // 时间选择
                    this._oTimeSelect = data['timeSelects'] || {};

                    var dimTree = data['dimTree'];
                    var root = this._oHierarchyRoot = dimTree['dimTree'];
                    // 暂时都使用第一个hierarchy，后续再添加多hierarchy的支持
                    this._oCurrDimTree = root['children'][0];
                    this._oLevelMap = dimTree['hierarchyLevelUniqueNames'];
                    this._sDimName = dimTree['dimName'];
                    this._sSchemaName = dimTree['schemaName'];
                    this._sDimType = dimTree['isTimeDim'] ? 'TIME' : 'NORMAL';
                }
                catch (e) {
                    logError(e);
                    this.$goError();
                }
            }
        }
    );

    /**
     * 得到当前维度树
     * 
     * @public
     * @return {Object} 维度树
     */
    DIM_SELECT_MODEL_CLASS.getCurrDimTree = function() {
        return this._oCurrDimTree;
    };

    /**
     * 得到当前时间选择
     * 
     * @public
     * @return {Object} 时间选择
     */
    DIM_SELECT_MODEL_CLASS.getTimeSelect = function() {
        return this._oTimeSelect;
    };

    /**
     * 得到当前层级列表
     * 
     * @public
     * @return {Array.<Object>} 层级列表
     */
    DIM_SELECT_MODEL_CLASS.getCurrLevelList = function() {
        return (this._oLevelMap && this._oCurrDimTree)
            ? this._oLevelMap[this._oCurrDimTree.name]
            : null;
    };

    /**
     * 构造公用参数
     * 
     * @protected
     * @param {Object} options sync参数
     * @return {Array.<string>} 公用参数
     */
    DIM_SELECT_MODEL_CLASS.$createBaseParam = function(options) {
        var args = options.args;
        var paramArr = [];

        if (args.commonParamGetter) {
            paramArr.push(args.commonParamGetter());
        }
        paramArr.push(
            'dimSelectName=' + textParam(args.uniqName)
        );
        paramArr.push(
            'from=' + textParam(args.selLineName)
        );

        return paramArr;
    };

})();

