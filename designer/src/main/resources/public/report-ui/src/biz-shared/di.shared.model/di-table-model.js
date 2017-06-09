/**
 * di.shared.model.DITableModel
 * Copyright 2013 Baidu Inc. All rights reserved.
 *
 * @file:    DI 表模型组件
 * @author:  sushuang(sushuang)
 * @depend:  xui, xutil
 */

$namespace('di.shared.model');

(function () {
    
    //------------------------------------------
    // 引用
    //------------------------------------------

    var URL = di.config.URL;
    var UTIL = di.helper.Util;
    var inheritsObject = xutil.object.inheritsObject;
    var wrapArrayParam = xutil.url.wrapArrayParam;
    var extend = xutil.object.extend;
    var logError = UTIL.logError;
    var getUID = xutil.uid.getUID;
    var XDATASOURCE = xui.XDatasource;

    //------------------------------------------
    // 类型声明
    //------------------------------------------

    /**
     * DI 表模型组件
     *
     * @class
     * @extends xui.XDatasource
     * @param {Function=} options.commonParamGetter      
     */
    var DI_TABLE_MODEL = 
            $namespace().DITableModel = 
            inheritsObject(XDATASOURCE, constructor);
    var DI_TABLE_MODEL_CLASS = 
            DI_TABLE_MODEL.prototype;

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
         * 得到公用的请求参数
         *
         * @type {Function}
         * @private
         */
        this._fCommonParamGetter = options.commonParamGetter;
    }

    /**
     * @override
     */
    DI_TABLE_MODEL_CLASS.init = function () {};

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DI_TABLE_MODEL_CLASS.url = new XDATASOURCE.Set(
        {
            DATA: URL.fn('OLAP_TABLE_DATA'),
            DRILL: URL.fn('OLAP_TABLE_DRILL'),
            LINK_DRILL: URL.fn('OLAP_TABLE_LINK_DRILL'),
            SORT: URL.fn('OLAP_TABLE_SORT'),
            CHECK: URL.fn('OLAP_TABLE_CHECK'),
            SELECT: URL.fn('OLAP_TABLE_SELECT'),
            MEASURE_DES: URL.fn('MEASURE_DES'),
            OFFLINE_DOWNLOAD: URL.fn('OLAP_TABLE_OFFLINE_DOWNLOAD'),
            RICH_SELECT_DATA: URL.fn('OLAP_TABLE_RICH_SELECT_DATA'),
            RICH_SELECT_CHANGE: URL.fn('OLAP_TABLE_RICH_SELECT_CHANGE')
        }
    );

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DI_TABLE_MODEL_CLASS.businessKey = new XDATASOURCE.Set(
        {
            DATA: 'DI_TABLE_MODEL_DATA_' + getUID(),
            DRILL: 'DI_TABLE_MODEL_DRILL_' + getUID(),
            LINK_DRILL: 'DI_TABLE_MODEL_LINK_DRILL_' + getUID(),
            SORT: 'DI_TABLE_MODEL_SORT_' + getUID(),
            CHECK: 'DI_TABLE_MODEL_CHECK_' + getUID(),
            SELECT: 'DI_TABLE_MODEL_SELECT_' + getUID(),
            MEASURE_DES: 'MEASURE_DES_' + getUID(),
            OFFLINE_DOWNLOAD: 'DI_TABLE_OFFLINE_DOWNLOAD_' + getUID(),
            RICH_SELECT_DATA: 'DI_TABLE_MODEL_RICH_SELECT_DATA_' + getUID(),
            RICH_SELECT_CHANGE: 'DI_TABLE_MODEL_RICH_SELECT_CHANGE_' + getUID()
        }
    );

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DI_TABLE_MODEL_CLASS.param = new XDATASOURCE.Set(
        {
            DATA: function (options) {
                return this._fCommonParamGetter(options.args.param);
            },
            DRILL: function (options) {
                return createLinkDrillParam.call(this, options);
            },
            LINK_DRILL: function (options) {
                return createLinkDrillParam.call(this, options);
            },
            SORT: function (options) {
                var param = options.args.param;
                return this._fCommonParamGetter(
                    {
                        uniqueName: param.uniqueName,
                        sortType: param.sortType,
                        componentId : param.componentId
                    }
                );
            },
            CHECK: function (options) {
                return this._fCommonParamGetter(
                    { uniqueName: options.args.param.uniqueName }
                );
            },
            MEASURE_DES: function (options) {
                var baseUrl = this._fCommonParamGetter();
                var url = baseUrl + '&' + options.args.param.colUniqueNamesArr.join('&');
                return url;
            },
            SELECT: function (options) {
                return this._fCommonParamGetter(
                    {
                        uniqueName: options.args.param.uniqueName,
                        componentId : options.args.param.componentId
                    }
                );
            },
            OFFLINE_DOWNLOAD: function (options) {
                return this._fCommonParamGetter(
                    { mailTo: options.args.param.email }
                );
            },
            RICH_SELECT_DATA: function (options) {
                return this._fCommonParamGetter(options.args.param);
            },
            RICH_SELECT_CHANGE: function (options) {
                return this._fCommonParamGetter(options.args.param);
            }
        }
    );

    /**
     * 创建链接式下钻参数
     *
     * @private
     */
    function createLinkDrillParam(options) {
        var param = options.args.param;
        var paramObj = {};
        
        paramObj['uniqueName'] = param.uniqueName;
        paramObj['lineUniqueName'] = param.lineUniqueName;

        paramObj['action'] = param.action;
        paramObj['componentId'] = param.componentId;
        // FIXME
        // 现在先写死，不存在上表头下钻
        paramObj['drillAxisName'] = 'ROW';
        paramObj['rowNum'] = param.rowNum;

        return this._fCommonParamGetter(paramObj);
    }

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DI_TABLE_MODEL_CLASS.complete = new XDATASOURCE.Set(
        {
            DATA: doComplete,
            DRILL: doComplete,
            LINK_DRILL: doComplete,
            SORT: doComplete,
            SELECT: doComplete,
            CHECK: doComplete,
            MEASURE_DES: doComplete,
            OFFLINE_DOWNLOAD: doComplete,
            RICH_SELECT_DATA: doComplete,
            RICH_SELECT_CHANGE: doComplete
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
    DI_TABLE_MODEL_CLASS.parse = new XDATASOURCE.Set(
        {
            DATA: doParse,
            DRILL: doParse,
            LINK_DRILL: doParse,
            SORT: doParse,
            MEASURE_DES: function (data, ejsonObj, options) { 
                return data;
                 },
            CHECK: function (data) { return data; },
            SELECT: function (data) { return data; },
            RICH_SELECT_DATA: function (data) { return data; },
            RICH_SELECT_CHANGE: function (data) { return data; }
        }
    );

    /**
     * 解析后台数据
     * 
     * @private
     */
    function doParse(data, ejsonObj, options) {
        try {
            if (data === null) {
                return null;
            }
            var retData = {};
            var tableData = retData.tableData = data['pivottable'];

            // 控件数据
            tableData.datasource = tableData.dataSourceRowBased;

            var i;
            var j;
            var o;
            var colspan;
            var headLength;

            // 控件列定义(colDefine)构造
            var firstLine = tableData['colFields'][0];
            var rawColDefine = tableData.colDefine;
            var colDefine = [];
            for (i = 0; i < firstLine.length; i ++) {
                o = firstLine[i];
                if (!o) { continue; }
                colspan = o.colspan || 1;
                for (j = 0; j < colspan; j ++) {
                    colDefine.push({ width:1 });
                }
            }
            headLength = colDefine.length - rawColDefine.length;
            for (i = 0; i < rawColDefine.length; i ++) {
                extend(colDefine[i + headLength], rawColDefine[i]);
            }
            tableData.colDefine = colDefine;

            // 由于之前不合适的接口制定：colspan和rowspan没有占位，导致坐标对不齐，
            // 这引来了很多处理上的麻烦（前后台都麻烦）。
            // 但是后台暂时没精力改了（因为有一定牵连）。
            // 所以这里对colFields和rowHeadFields强制加上占位，使其对其。
            fixColFields(tableData, headLength);
            fixRowHeadFields(tableData, headLength);

            // 排序
            var sortType; 
            var sortKeyMap = { // 前后台接口映射
                ASC: 'asc',
                DESC: 'desc',
                NONE: 'none'
            }
            for (i = 0; i < colDefine.length; i ++) {
                if (sortType = colDefine[i].currentSort) {
                    colDefine[i].orderby = sortKeyMap[sortType];
                }
            }

            retData.tableData.reportTemplateId = data['reportTemplateId'];
            // 行选中
            retData.tableData.rowCheckMax = data['rowCheckMax'];
            retData.tableData.rowCheckMin = data['rowCheckMin'];

            // 面包屑
            var breadcrumb = data['mainDimNodes'] || [];
            if (breadcrumb) {
                for (i = 0; o = breadcrumb[i]; i ++) {
                    o.text = o['showName'];
                    o.value = i;
                    o.url = null;
                    if (i == breadcrumb.length - 1) {
                        o.disabled = true;
                    }
                    if (i == 0) {
                        o.isFirst = true;
                    }
                }
            }
            retData.breadcrumbData = {
                datasource: breadcrumb,
                maxShow: 5,
                hidePosPercent: 0.5
            }

            retData.pageInfo = {
                totalRecordCount: data['totalSize'],
                currRecordCount: data['currentSize']
            }

            // retData.tableDataOverlap = getDataOverlap(
            //     tableData, 
            //     options.args.viewStateWrap
            // );

            this._oData = retData;
            
            return retData;
        }
        catch (e) {
            logError(e);
            this.$goError();
        }
    }

    /**
     * @public
     */
    DI_TABLE_MODEL_CLASS.getData = function () {
        return this._oData;
    };

    /**
     * 得到保存的状态，用于覆盖
     *
     * @protected
     */
    // function getDataOverlap(tableData, viewStateWrap) {
    //     if (!tableData || !viewStateWrap) { return; }

    //     var dataOverlap = {};

    //     // 行选择
    //     var rowCheckedMap = viewStateWrap.rowCheckedMap;
    //     if (rowCheckedMap) {
    //         var rowChecked = [];
    //         for (var i = 0, rhd; rhd = tableData.rowDefine[i]; i ++) {
    //             (rhd.uniqueName in rowCheckedMap) && rowChecked.push(i);
    //         }

    //         dataOverlap.rowChecked = rowChecked;
    //     }

    //     return dataOverlap;
    // };

    /**
     * 对colFields进行占位补齐，使用空对象{}进行标志。
     * 约定的法则：
     *      只有左上角第一行有rowspan（前面得到了headLength），
     *      其他地方不考虑rowspan，
     *      并且呈树状展开
     * 
     * @private
     */
    function fixColFields(tableData, headLength) {
        var i;
        var j;
        var k;
        var o;
        var line;
        var rawLine;
        var colspan;
        var colFields = [];

        for (i = 0; rawLine = tableData.colFields[i]; i ++) {
            colFields.push(line = []);
            if (i > 0) {
                // 左上角区域，后台只给第一行，后面的加占位
                for (k = 0; k < headLength; k ++) {
                    line.push({});
                }
            }
            for (j = 0; j < rawLine.length; j ++) {
                line.push(o = rawLine[j]);
                colspan = (o || {}).colspan || 1;
                for (k = 1; k < colspan; k ++) {
                    // 占位
                    line.push({});
                }
            }
        }
        tableData.colFields = colFields;
    }

    /**
     * 对rowHeadFields进行占位补齐，使用空对象{}进行标志。
     * 约定的法则：
     *      不存在colspan，
     *      只有rowspan，
     *      并且呈树状展开
     *
     * @private
     */
    function fixRowHeadFields(tableData, headLength) {
        var i;
        var j;
        var line;
        var rawLine;
        var rowHeadFields = [];

        for (i = 0; rawLine = tableData.rowHeadFields[i]; i ++) {
            rowHeadFields.push(line = []);
            // 前面补齐
            for (j = 0; j < headLength - rawLine.length; j ++) {
                line.push({});
            }
            for (j = 0; j < rawLine.length; j ++) {
                line.push(rawLine[j]);
            }
        }
        tableData.rowHeadFields = rowHeadFields;
    }

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    DI_TABLE_MODEL_CLASS.error = new XDATASOURCE.Set(
        {
            DATA: function (status, ejsonObj, options) {
                this._oTableData = {};
                this._oBreadcrumbData = {};
            }
            // TODO
        }
    );

})();

