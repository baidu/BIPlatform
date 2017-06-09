/**
 * di.shared.model.CubeMetaModel
 * Copyright 2013 Baidu Inc. All rights reserved.
 *
 * @file:    cube树原数据Model
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
    var inheritsObject = xutil.object.inheritsObject;
    var q = xutil.dom.q;
    var g = xutil.dom.g;
    var bind = xutil.fn.bind;
    var extend = xutil.object.extend;
    var assign = xutil.object.assign;
    var getUID = xutil.uid.getUID;
    var parse = baidu.json.parse;
    var stringify = baidu.json.stringify;
    var hasValue = xutil.lang.hasValue;
    var clone = xutil.object.clone;
    var stringToDate = xutil.date.stringToDate;
    var dateToString = xutil.date.dateToString;
    var textParam = xutil.url.textParam;
    var wrapArrayParam = xutil.url.wrapArrayParam;
    var LINKED_HASH_MAP = xutil.LinkedHashMap;
    var travelTree = xutil.collection.travelTree;
    var XDATASOURCE = xui.XDatasource;
    var GLOBAL_MODEL;

    $link(function () {
        GLOBAL_MODEL = di.shared.model.GlobalModel;
    });

    //------------------------------------------
    // 类型声明
    //------------------------------------------

    /**
     * cube树原数据Model
     *
     * @class
     * @extends xui.XDatasource
     */
    var CUBE_META_MODEL = 
            $namespace().CubeMetaModel = 
            inheritsObject(XDATASOURCE, constructor);
    var CUBE_META_MODEL_CLASS = 
            CUBE_META_MODEL.prototype;
  
    //------------------------------------------
    // 常量
    //------------------------------------------

    //------------------------------------------
    // 方法
    //------------------------------------------

    /**
     * 构造方法
     *
     * @private
     */
    function constructor(options) {
        this._sReportType = options.reportType;
    }

    /**
     * @override
     */
    CUBE_META_MODEL_CLASS.init = function() {};

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    CUBE_META_MODEL_CLASS.url = new XDATASOURCE.Set(
        {
            CUBE_INIT: URL.fn('CUBE_META'),
            DATASOURCE_INIT: URL.fn('DATASOURCE_META')
        }
    );

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    CUBE_META_MODEL_CLASS.param = new XDATASOURCE.Set(
        {
            // CUBE_INIT: function () {
            // },
            DATASOURCE_INIT: function () {
                return 'bizKey=' + textParam(GLOBAL_MODEL().getBizKey());
            }
        }
    );

    /**
     * @override
     * @see xui.XDatasource.prototype.OPTIONS_NAME
     */
    CUBE_META_MODEL_CLASS.result = new XDATASOURCE.Set(
        {
            CUBE_INIT: function(data) {
                this._aCubeForest = data['cubeTree'];
            },
            DATASOURCE_INIT: function (data) {
                this._aDatasourceList = data['datasourceList']; 
            }
        }
    );

    /**
     * @public
     */
    CUBE_META_MODEL_CLASS.setReportType = function(reportType) {
        this._sReportType = reportType;
    };

    /**
     * 得到cube树转为的menu结构
     *
     * @public
     * @return {xutil.LinkedHashMap} selLine
     */
    CUBE_META_MODEL_CLASS.getMenuData = function() {
        var menuTree = { menuList: [] };
        var selMenuId;
        var reportType = this._sReportType;

        ({
            RTPL_OLAP_TABLE: cubeMenu,
            RTPL_OLAP_CHART: cubeMenu,
            RTPL_PLANE_TABLE: datasourceMenu
        })[reportType].call(this);

        return { menuTree: menuTree };

        // 根据cube数据，创建menu数据
        function cubeMenu() {
            menuTree.menuList.push({ text: '选择CUBE', value: 1 });

            for (var i = 0, root; root = this._aCubeForest[i]; i ++) {
                var schemaName = root['schemaName'];
                travelTree(
                    root = clone(root['root']),
                    function(node, options) {
                        node.value = node.nodeName;
                        node.text = node.caption || ' - ';
                        if (node.children) {
                            return;
                        }
                        if (reportType == 'RTPL_OLAP_TABLE') {
                            node.url = 'di.console.editor.ui.OLAPEditor?'
                                + [
                                    'editorType=' + reportType,
                                    'reportType=' + reportType,
                                    'pageId=' + reportType + '_' + node.value + getUID(),
                                    'pageTitle=[透视表] ' + node.text,
                                    'schemaName=' + schemaName,
                                    'cubeTreeNodeName=' + node.nodeName
                                ].join('&')
                        }
                        else if (reportType == 'RTPL_OLAP_CHART') {
                            node.url = 'di.console.editor.ui.OLAPEditor?'
                                + [
                                    'editorType=' + reportType,
                                    'reportType=' + reportType,
                                    'pageId=' + reportType + '_' + node.value + getUID(),
                                    'pageTitle=[图] ' + reportType + '_' + node.text,
                                    'schemaName=' + schemaName,
                                    'cubeTreeNodeName=' + node.nodeName
                                ].join('&')
                        }
                    },
                    'children'
                );
                menuTree.menuList.push(root);
            }
            menuTree.selMenuId = 1;
        }

        // 根据plane table需要的datasource数据，创建menu数据
        function datasourceMenu() {
            var chs = [];
            menuTree.menuList.push({ text: '选择数据源', value: 1, children: chs });

            for (var i = 0, item; item = this._aDatasourceList[i]; i ++) {
                chs.push(
                    {
                        text: item.text,
                        value: item.value,
                        url: 'di.console.editor.ui.PlaneEditor?'
                            + [
                                'editorType=' + reportType,
                                'reportType=' + reportType,
                                'pageId=' + reportType + '_' + item.value + getUID(),
                                'pageTitle=[平面表] ' + reportType + '_' + item.text,
                                'datasourceName=' + item.value
                            ].join('&')
                    }
                );
            }            
        }
    };

    /**
     * 得到cube树转为的menu结构
     *
     * @deprecate
     * @public
     * @return {xutil.LinkedHashMap} selLine
     */
    // CUBE_META_MODEL_CLASS.getFullMenuByCubeMeta = function() {
    //     var menuTree = { menuList: [] };
    //     var selMenuId;

    //     menuTree.menuList.push(
    //         {
    //             text: '报表类型',
    //             value: 1
    //         }
    //     );

    //     for (
    //         var i = 0, root, schemaName; 
    //         root = this._aCubeForest[i]; 
    //         i ++
    //     ) {
    //         schemaName = root['schemaName'];
    //         travelTree(
    //             root = clone(root['root']),
    //             function(node, options) {

    //                 node.value = node.nodeName;
    //                 node.text = node.caption || ' - ';
    //                 node.floatTree = [
    //                     {
    //                         text: node.caption,
    //                         value: String(Math.random()),
    //                         url: 'schemaName=' + schemaName,
    //                         children: [
    //                             {
    //                                 text: '创建透视表',
    //                                 value: String(Math.random()),
    //                                 url: 'di.console.editor.ui.OLAPEditor?'
    //                                     + [
    //                                         'reportType=TABLE',
    //                                         'schemaName=' + schemaName,
    //                                         'cubeTreeNodeName=' + node.nodeName
    //                                     ].join('&')
    //                             },
    //                             {
    //                                 text: '创建平面表',
    //                                 value: String(Math.random()),
    //                                 url: 'di.console.editor.ui.PlaneEditor?'
    //                                     + [
    //                                         'datasourceName='
    //                                     ].join('&')
    //                             },
    //                             {
    //                                 text: '创建图',
    //                                 url: 'di.console.editor.ui.OLAPEditor?'
    //                                     + [
    //                                         'reportType=CHART',
    //                                         'schemaName=' + schemaName,
    //                                         'cubeTreeNodeName=' + node.nodeName
    //                                     ].join('&')
    //                             }
    //                         ]
    //                     }
    //                 ];
    //             },
    //             'children'
    //         );
    //         menuTree.menuList.push(root);
    //     }
    //     menuTree.selMenuId = 1;

    //     // FIXME
    //     // 临时增加：报表效果观看的入口
    //     menuTree.menuList.push(
    //         {
    //             text: '效果试验',
    //             value: 19999,
    //             url: 'di.console.editor.ui.ReportPreview'
    //         }
    //     );

    //     return { menuTree: menuTree };
    // };

})();

