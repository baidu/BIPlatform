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
    var MULTIDIM_SELECT_MODEL = 
            $namespace().MultiDimSelectModel = 
            inheritsObject(XDATASOURCE, constructor);
    var MULTIDIM_SELECT_MODEL_CLASS = 
    		MULTIDIM_SELECT_MODEL.prototype;
  
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

        this._multiSelectData;
    }

    var URL_MAP = {
        MULTISELECT: {
            RTPL_OLAP_TABLE: URL.fn('DIM_MULTISELECT_TABLE'),
            RTPL_OLAP_CHART: URL.fn('DIM_MULTISELECT_CHART')
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
    MULTIDIM_SELECT_MODEL_CLASS.url = function(options) {
        return URL_MAP[options.datasourceId][options.args.reportType]();
    }

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    MULTIDIM_SELECT_MODEL_CLASS.param = new XDATASOURCE.Set(
        {
            // 请求维度树参数
            TREE: function(options) {
                var paramArr = this.$createBaseParam(options);
                if (options.args.dimMode == 'TIME') {
                    paramArr.push('isTimeDim=true');
                }
                return paramArr.join('&');
            },

             // 请求维度多选参数
            MULTISELECT: function(options) {
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
                    'selectedLevel=' + textParam(args.selectedLevel)
                );
                if(args.selectedDims){
                    for(var i = 0; i < args.selectedDims.length ; i ++){
                        paramArr.push(
                            'selectedNodes=' + textParam(args.selectedDims[i])
                        );  
                    }
                }
                
                return paramArr.join('&');
            }
        }
    );

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    MULTIDIM_SELECT_MODEL_CLASS.parse = new XDATASOURCE.Set(
        {
            MULTISELECT: function(data) {
                this._multiSelectData = data.dimValue;
            }
        }
    );

    MULTIDIM_SELECT_MODEL_CLASS.getMultiSelectData = function() {
        return this._multiSelectData;
    };


    /**
     * 构造公用参数
     * 
     * @protected
     * @param {Object} options sync参数
     * @return {Array.<string>} 公用参数
     */
    MULTIDIM_SELECT_MODEL_CLASS.$createBaseParam = function(options) {
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
        paramArr.push(
            'componentId=' + textParam(args.componentId)
        );

        return paramArr;
    };

})();

