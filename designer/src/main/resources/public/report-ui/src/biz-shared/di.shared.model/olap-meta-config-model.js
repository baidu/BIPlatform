/**
 * di.shared.model.OlapMetaConfigModel
 * Copyright 2013 Baidu Inc. All rights reserved.
 *
 * @file:    元数据选择Model
 * @author:  sushuang(sushuang)
 * @depend:  xui, xutil
 */

$namespace('di.shared.model');

(function () {
    
    //------------------------------------------
    // 引用
    //------------------------------------------

    var FORMATTER = di.helper.Formatter;
    var DICT = di.config.Dict;
    var LANG = di.config.Lang;
    var URL = di.config.URL;
    var UTIL = di.helper.Util;
    var inheritsObject = xutil.object.inheritsObject;
    var q = xutil.dom.q;
    var g = xutil.dom.g;
    var bind = xutil.fn.bind;
    var isArray = xutil.lang.isArray;
    var extend = xutil.object.extend;
    var assign = xutil.object.assign;
    var parse = baidu.json.parse;
    var logError = UTIL.logError;
    var getUID = xutil.uid.getUID;
    var LINKED_HASH_MAP = xutil.LinkedHashMap;
    var XDATASOURCE = xui.XDatasource;

    //------------------------------------------
    // 类型声明
    //------------------------------------------

    /**
     * 元数据选择Model
     *
     * @class
     * @extends xui.XDatasource
     * @param {Object} options
     * @param {Object} options.reportType
     * @param {Function=} options.commonParamGetter    
     */
    var OLAP_META_CONFIG_MODEL = 
            $namespace().OlapMetaConfigModel = 
            inheritsObject(XDATASOURCE, constructor);
    var OLAP_META_CONFIG_MODEL_CLASS = 
            OLAP_META_CONFIG_MODEL.prototype;
  
    //------------------------------------------
    // 方法
    //------------------------------------------

    /**
     * 构造方法
     *
     * @private
     * @param {Object} options 参数
     */
    function constructor(options) {
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
        /**
         * 指标列表
         *
         * @type {xutil.LinkedHashMap}
         * @private
         */
        this._oIndList = new LINKED_HASH_MAP(null, 'uniqName');


        /**
         * liteOlap下拉框指标列表
         *
         * @type {xutil.LinkedHashMap}
         * @private
         */
        this._liteOlapIndList = new LINKED_HASH_MAP(null, 'uniqName');
        /**
         * 维度列表
         * 
         * @type {xutil.LinkedHashMap}
         * @private
         */
        this._oDimList = new LINKED_HASH_MAP(null, 'uniqName');
        /**
         * selLine包装
         * key为selLine唯一名，value是selLine的list
         * 
         * @type {xutil.LinkedHashMap}
         * @private
         */
        this._oSelLineWrap = new LINKED_HASH_MAP(null, 'k', 'l');
        /**
         * 元数据状态
         * dimMetas: {}
         * indMetas: {}
         *      {Array.<string>} validMetaNames
         *      {Array.<string>} selectedMetaNames
         *
         * @type {Object}
         * @private
         */
        this._oStatusWrap = {};
        /**
         * 图的系列组属性
         *
         * @private
         */
        this._oSeriesCfg = {};
    }

    /**
     * @override
     */
    OLAP_META_CONFIG_MODEL_CLASS.init = function () {};

    var URL_MAP = {
        RTPL_OLAP_TABLE: {
            DATA: URL.fn('META_CONDITION_IND_DIM_TABLE'),
            LITEOLAP_INDS_DATA: URL.fn('LITEOLAP_INDS_META_DATA'),
            SELECT: URL.fn('META_CONDITION_SELECT_TABLE'),
            LIST_SELECT: URL.fn('META_CONDITION_LIST_SELECT_TABLE')
        },
        RTPL_OLAP_CHART: {
            DATA: URL.fn('META_CONDITION_IND_DIM_CHART'),
            LITEOLAP_INDS_DATA: URL.fn('LITEOLAP_INDS_META_DATA'),
            SELECT: URL.fn('META_CONDITION_SELECT_CHART'),
            LIST_SELECT: URL.fn('META_CONDITION_LIST_SELECT_CHART')
        }
    };    

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    OLAP_META_CONFIG_MODEL_CLASS.url = function (options) {
        return URL_MAP[this._sReportType][options.datasourceId]();
    }

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    OLAP_META_CONFIG_MODEL_CLASS.businessKey = new XDATASOURCE.Set(
        {
            DATA: 'OLAP_META_CONFIG_MODEL_DATA_' + getUID(),
            SELECT: 'OLAP_META_CONFIG_MODEL_SELECT_' + getUID()
        }
    );

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    OLAP_META_CONFIG_MODEL_CLASS.complete = new XDATASOURCE.Set(
        {
            DATA: doComplete,
            LITEOLAP_INDS_DATA: doComplete,
            SELECT: doComplete
        }
    );

    function doComplete(ejsonObj) {
        // 换reportTemplateId（后台生成了副本，所以约定更换为副本的id）
        // FIXME 
        // 换成非嵌入的实现方式
        this._fCommonParamGetter.update(ejsonObj.data);
    }

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    OLAP_META_CONFIG_MODEL_CLASS.param = new XDATASOURCE.Set(
        {
            DATA: function (options) { 
                return this._fCommonParamGetter(options.args.param);
            },
            LITEOLAP_INDS_DATA: function (options) { 
                return this._fCommonParamGetter(options.args.param);
            },
            SELECT: function (options) {
                var changeWrap = options.args.changeWrap;
                var paramArr = [];

                var param = {
                    from: changeWrap.from,
                    to: changeWrap.to,
                    toPosition: changeWrap.toPosition != null 
                        ? changeWrap.toPosition : -1,
                    needShowCalcInds: options.args.needShowCalcInds,
                    uniqNameList: [],
                    componentId: options.args.componentId
                };

                param.uniqNameList.push.apply(
                    param.uniqNameList,
                    changeWrap.uniqNameList
                );

                return this._fCommonParamGetter(param);
            }, 
            LIST_SELECT: function (options) {
                return this._fCommonParamGetter(options.args);
            }
        }
    );

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    OLAP_META_CONFIG_MODEL_CLASS.parse = new XDATASOURCE.Set(
        {
            DATA: function (data, ejsonObj, options) {
                try {
                    var me = this;

                    this.$clean();
                    
                    // 指标维度元数据
                    var metaData = data['metaData'];
                    this._oIndList.appendAll(metaData['inds']);
                    this._oDimList.appendAll(metaData['dims']);

                    // 设置指标还是维度标记
                    setIndDimClazz.call(this, this._oIndList, 'IND');
                    setIndDimClazz.call(this, this._oDimList, 'DIM');

                    // 图的series属性（左右轴，图类型等）
                    this._oSeriesCfg = {};
                    var seriesTypes = data['seriesTypes'] || {};
                    for (var serName in seriesTypes) {
                        this._oSeriesCfg[serName] = {
                            type: seriesTypes[serName]
                            // TODO
                            // 左右轴
                        };
                    }

                    // selLine处理
                    for (
                        var i = 0, key, list; 
                        key = data['index4Selected'][i]; 
                        i ++
                    ) {
                        this._oSelLineWrap.addLast(
                            {
                                k: key,
                                l: list = new LINKED_HASH_MAP(
                                    data['selected'][key], 
                                    'uniqName'
                                )
                            }
                        );
                        setIndDimClazz.call(this, list);
                    }

                    // 选中、禁用等状态
                    doMerge.call(this, data);
                }
                catch (e) {
                    logError(e);
                    this.$goError();
                }
            },

            LITEOLAP_INDS_DATA: function (data, ejsonObj, options) {
                try {
                    var me = this;

                    this.$clean();
                    
                    // 指标维度元数据
                    // var metaData = data['metaData']['inds'];
                    // var metaStatusData = data['metaStatusData']['indMetas']['selectedMetaNames'];
                    // var selectedInds=[];
                    // for (var i = metaData.length - 1; i >= 0; i--) {
                    //     for (var j = metaStatusData.length - 1; j >= 0; j--) {
                    //         if(metaData[i]['uniqName']== metaStatusData[j]){
                    //             selectedInds.push(metaData[i]);
                    //             break;
                    //         }
                    //     };
                    // };

                    //this._liteOlapIndList.appendAll(data['selected']['COLUMN']);


                    // 选中、禁用等状态
                    //doMerge.call(this, data);
                }
                catch (e) {
                    logError(e);
                    this.$goError();
                }
            },

            SELECT: function (data, ejsonObj, options) {
                try {
                    // 选中、禁用等状态
                    doMerge.call(this, data);

                    // 提交成功才更新本地selected的Model数据
                    this.$updateSelected(options.args.changeWrap);
                }
                catch (e) {
                    logError(e);
                    this.$goError();
                }
            }
        }
    );

    /**
     * 对selected和meta进行融合
     * 
     * @private
     */
    function doMerge(data) {

        // 用selected中的status来覆盖进meta
        if (this._oStatusWrap = data['metaStatusData']) {
            // 处理、融合
            mergeStatus.call(
                this, 
                this._oStatusWrap.indMetas, 
                this._oIndList
            );
            mergeStatus.call(
                this, 
                this._oStatusWrap.dimMetas, 
                this._oDimList
            );
        }

        // 用meta中的其余信息（如fixed、align等）覆盖回selected
        var indList = this._oIndList;
        var dimList = this._oDimList;
        this._oSelLineWrap.foreach(
            function (selLineName, selLine, index) {
                selLine.foreach(function (key, item, idx) {
                    var o;
                    if ((o = indList.get(key))
                        || (o = dimList.get(key))
                    ) {
                        extend(item, o);
                    }
                });
            }
        );
    }

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    OLAP_META_CONFIG_MODEL_CLASS.error = new XDATASOURCE.Set(
        {
            DATA: function (status, ejsonObj, options) {
                this._oIndList.clean();
                this._liteOlapIndList.clean();
                this._oDimList.clean();
                this._oSelLineWrap.clean();
                this._oStatusWrap = {};
            }
        }
    );

    OLAP_META_CONFIG_MODEL_CLASS.$clean = function() {
        this._oIndList.cleanWithoutDefaultAttr();
        this._liteOlapIndList.cleanWithoutDefaultAttr();
        this._oDimList.cleanWithoutDefaultAttr();
        this._oSelLineWrap.cleanWithoutDefaultAttr();
        this._oStatusWrap = {};
        this._oSeriesCfg = {};
    };

    /**
     * 补充设置指标维度标志，根据字典
     *
     * @private
     */
    function setIndDimClazz(list, flag) {
        var me = this;
        list.foreach(
            function (key, o) {
                if (flag) {
                    o.clazz = flag;
                }
                else if (me._oIndList.containsKey(o.uniqName)) {
                    o.clazz = 'IND';
                }
                else if (me._oDimList.containsKey(o.uniqName)) {
                    o.clazz = 'DIM';
                }
            }
        );        
    }

    /**
     * 融合status
     *
     * @private
     */
    function mergeStatus(statusWrap, baseList) {
        // 先全设为disabled
        baseList.foreach(
            function (k, item, index) {
                item.status = DICT.META_STATUS.DISABLED;
            }
        );

        if (!statusWrap) { return; }

        var validMetaNames = statusWrap.validMetaNames;
        !validMetaNames 
            && (validMetaNames = statusWrap.validMetaNames = []);

        var selectedMetaNames = statusWrap.selectedMetaNames;
        !selectedMetaNames 
            && (selectedMetaNames = statusWrap.selectedMetaNames = []);

        // 用后台返回的normal和selected列表设置状态
        // 因为visible设定的影响，后台返回的项有可能含有baseList里不存在的（小明说灰常难改），
        // 所以在这里去除不存在的
        var i;
        var o;
        var item;
        for (i = 0; i < validMetaNames.length;) {
            if (item = baseList.get(validMetaNames[i])) {
                item.status = DICT.META_STATUS.NORMAL;
                i ++;
            }
            else {
                validMetaNames.splice(i, 1);
            }
        }
        for (i = 0; i < selectedMetaNames.length;) {
            if (item = baseList.get(selectedMetaNames[i])) {
                item.status = DICT.META_STATUS.SELECTED;
                i ++;
            }
            else {
                selectedMetaNames.splice(i, 1);
            }
        }

        // 接口定的有点乱，控件需要的其实是disabled列表
        var disabledMetaNames = statusWrap.disabledMetaNames = [];
        baseList.foreach(
            function (k, item, index) {
                if (item.status == DICT.META_STATUS.DISABLED) {
                    disabledMetaNames.push(k);
                }
            }
        );
    }

    /**
     * 得到selLine包装
     *
     * @public
     * @return {xutil.LinkedHashMap} selLine
     */
    OLAP_META_CONFIG_MODEL_CLASS.getSelLineWrap = function () {
        return this._oSelLineWrap;
    };

    /**
     * 得到指标维度列表
     *
     * @public
     * @return {Object} 指标维度列表
     */
    OLAP_META_CONFIG_MODEL_CLASS.getIndDim = function () {
        return {
            indList: this._oIndList,
            dimList: this._oDimList
        };
    };

    /**
     * 得到LiteOlap指标列表
     *
     * @public
     * @return {Object} 指标维度列表
     */
    OLAP_META_CONFIG_MODEL_CLASS.getLiteOlapIndList = function () {
        return this._liteOlapIndList;
    };

    /**
     * 得到指标维度最新状态
     *
     * @public
     * @return {Object} 指标维度最新状态
     */
    OLAP_META_CONFIG_MODEL_CLASS.getUpdateData = function () {
        return this._oStatusWrap;
    };

    /**
     * 得到图的系列租设置信息
     *
     * @public
     * @return {Object} 指标维度最新状态
     */
    OLAP_META_CONFIG_MODEL_CLASS.getSeriesCfg = function () {
        return this._oSeriesCfg;
    };

    /**
     * 根据uniqName得到项
     * 
     * @public
     * @param {string} uniqName
     * @return {Object} metaItem
     */
    OLAP_META_CONFIG_MODEL_CLASS.getMetaItem = function (uniqName) {  
        var item = this._oIndList.get(uniqName);
        if (!item) {
            item = this._oDimList.get(uniqName);
        }
        return item;
    };

    /**
     * 得到选择变化信息
     * 
     * @public
     * @param {Object} selLineWrap key为行列名，value为行列选中列表 
     * @return {Object} 返回值的key为from, to, toPosition
     */
    OLAP_META_CONFIG_MODEL_CLASS.diffSelected = function (selLineWrap) {
        var srcList;
        var removeList; 
        var addList;
        var changeWrap = { uniqNameList: [] };

        for (var name in selLineWrap) {
            srcList = this._oSelLineWrap.get(name);
            diffLineSelected.call(
                this, 
                name, 
                selLineWrap[name], 
                srcList, 
                changeWrap
            );
        }

        return changeWrap;
    };

    /**
     * 得到某行选择变化信息
     * 只支持三种可能：某项此行间换位值，拖离此行，拖进此行
     * （这些处理过于复杂，后端也重复实现了这些复杂逻辑，这
     *  源于定的from-to接口，合理的方式是重构，
     *  不使用from-to方式的接口，而是传当前状态）
     * 
     * @private
     * @param {string} lineName
     * @param {Array.<string>} currLine
     * @param {xutil.LinkedHashMap} srcList 
     * @param {Object} result
     */
    function diffLineSelected(lineName, currLine, srcList, result) {
        // 在此行间换位置的情况，检查出拖动的节点
        if (currLine.length == srcList.size()) {
            var diffKeySrc;
            var diffIndex;
            var tarIndexCurr;
            var tarIndexSrc;
            var tarKeySrc;
            srcList.foreach(
                function (key, value, index) {
                    if (diffIndex == null) {
                        if (key != currLine[index]) { 
                            // 出现了第一个不一样的值
                            diffKeySrc = key; 
                            diffIndex = index;
                        }
                    }
                    else {
                        if (diffKeySrc == currLine[index]) {
                            tarIndexCurr = index;
                        }
                        if (currLine[diffIndex] == key) {
                            tarIndexSrc = index;
                            tarKeySrc = key;
                        }
                    }
                }
            );
            if (diffIndex != null) {
                result.from = lineName;
                result.to = lineName;
                result.fromLineData = currLine;
                result.toLineData = currLine;
                if (tarIndexSrc > tarIndexCurr) {
                    result.uniqName = tarKeySrc;
                    result.toPosition = diffIndex;
                }
                else {
                    result.uniqName = diffKeySrc;
                    result.toPosition = tarIndexCurr;
                }
                result.uniqNameList.push(result.uniqName);
            }
        }
        // 拖进此行的情况
        else if (currLine.length > srcList.size()) {
            for (var i = 0, name; i < currLine.length; i ++) {
                name = currLine[i];
                if (!srcList.containsKey(name)) {
                    result.uniqName = name
                    result.uniqNameList.splice(0, 1, name);
                    result.to = lineName;
                    result.toLineData = currLine;
                    if (result.toPosition == null) {
                        result.toPosition = i;
                    }
                }
            }
        }
        // 拖离此行的情况（删除或者拖到别的行）
        else if (currLine.length < srcList.size()) {
            srcList.foreach(
                function (name, value, index) {
                    if (currLine[index] != name) {
                        result.uniqName = name
                        result.uniqNameList.push(name);
                        result.from = lineName;
                        result.fromLineData = currLine;
                        return false;
                    }
                }
            );
        }
        // FIXME
        // 临时处理，FIXME，后续改和后台的接口
        result.uniqNameList.splice(1, result.uniqNameList.length - 1);
    };

    /**
     * 设置条件选择变化
     * 
     * @protected
     * @param {Object} changeWrap
     * @param {Array.<string>} changeWrap.uniqNameList
     * @param {string} changeWrap.from
     * @param {string} changeWrap.to
     * @param {number} changeWrap.toPosition
     */
    OLAP_META_CONFIG_MODEL_CLASS.$updateSelected = function (changeWrap) {
        var fromList = changeWrap.from != changeWrap.to
                ? this._oSelLineWrap.get(changeWrap.from)
                : null;
        var toList = this._oSelLineWrap.get(changeWrap.to);

        var fromLineData = changeWrap.fromLineData;
        var toLineData = changeWrap.toLineData;
        var i = 0;
        var uniqName;

        if (fromList) {
            fromList.cleanWithoutDefaultAttr();
            for (i = 0; i < fromLineData.length; i ++) {
                uniqName = fromLineData[i];
                fromList.addLast(this.getMetaItem(uniqName));
            }
        }

        if (toList) {
            toList.cleanWithoutDefaultAttr();
            for (i = 0; i < toLineData.length; i ++) {
                uniqName = toLineData[i];
                toList.addLast(this.getMetaItem(uniqName));
            }
        }

        //----------------------------------
        // ONLY FOR TESTING. TO BE DELETED.
        // console.log(changeWrap);
        // console.log('      uniqNameList= ' + changeWrap.uniqNameList);
        // console.log('      from= ' + changeWrap.from);
        // console.log('      fromLineData= ' + changeWrap.fromLineData);
        // console.log('      to= ' + changeWrap.to);
        // console.log('      toLineData= ' + changeWrap.toLineData);
        // console.log('      toPosition= ' + changeWrap.toPosition);
        // this._oSelLineWrap.foreach(function (k, item, index) {
        //     console.log('LINE NAME::: ' + k);
        //     item.foreach(function (kk, oo, ii) {
        //         var arr = [];
        //         arr.push(kk);
        //         console.log('          ' + arr.join('  '));
        //     });
        // });
    };

    //------------------------------------------------
    // 拖拽规则
    //------------------------------------------------

    

})();

