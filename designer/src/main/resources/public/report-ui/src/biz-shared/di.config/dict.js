/**
 * di.config.Dict
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    data insight 全局(包括console和product)的ajax的配置
 * @author:  sushuang(sushuang), lizhantong(lztlovely@126.com)
 */

$namespace('di.config');

(function() {

    /**
     * DICT初始化，此方法会在生成的repo-dict.js中自动被调用
     */
    var DICT = $namespace().Dict = function () {
        if (!initialized) {
            this.reset();
            initialized = true;
        }
        return DICT;
    };

    /**
     * DICT恢复默认状态
     */
    DICT.reset = function () {
        // 初始化类引用
        DICT.CLZ = {};
        for (var i = 0, clzDef; clzDef = DICT.CLZ_DEFS[i]; i ++) {
            if (clzDef.clzKey in DICT.CLZ) {
                throw new Error("dupicate clzKey: " + clzDef.clzKey);
            }
            DICT.CLZ[clzDef.clzKey] = clzDef;
        }
    }

    var initialized = false;

    DICT.DOM_FLAG_BEGIN = '<!-- DI_BEGIN^_^DONT_MODIFY_ME -->';
    DICT.DOM_FLAG_END = '<!-- DI_END^_^DONT_MODIFY_ME -->';
    DICT.RTPL_VIRTUAL = 'RTPL_VIRTUAL';
    DICT.RTPL_VIRTUAL_ID = 'RTPL_VIRTUAL_ID';

    /**
     * 目前支持的图的类型枚举
     */
    DICT.GRAPH_DEFS = [
        { name: 'line', text: '折线', yAxisNameSet: ['left', 'right'] },
        { name: 'bar', text: '柱', yAxisNameSet: ['left', 'right'] },
        { name: 'pie', text: '饼', yAxisNameSet: [] },
        { name: 'beaker', text: '烧杯', yAxisNameSet: []}
    ];
    DICT.getGraphByType = function (type) {
        for (var i = 0, o; o = DICT.GRAPH_DEFS[i]; i ++) {
            if (o.name == type) { return o; }
        }
    }

    /**
     * 同后台的 “reportTemplateType”
     */
    DICT.REPORT_TYPE = {
        RTPL_OLAP_TABLE: 1,
        RTPL_OLAP_CHART: 1,
        RTPL_PLANE_TABLE: 1,
        RTPL_VIRTUAL: 1
    };

    /**
     * 标记且功能性css，在console会起标记作用, 定义在di.css中
     */
    DICT.FLAG_CSS = [
        'di-o_o-body',
        'di-o_o-block',
        'di-o_o-line',
        'di-o_o-item',
        'di-o_o-space-l1'
    ];

    /**
     * DI 用户定义的前缀
     */
    DICT.PARAM_PREFIX = 'DI_P_';

    /**
     * 视图模版部署相对路径
     */
    DICT.VTPL_ROOT = 'asset-d';

    /**
     * 视图模版部署相对路径
     */
    DICT.MOLD_PATH = 'asset-d/-com-/mold';

    /**
     * 默认的遮罩透明度
     */
    DICT.DEFAULT_MASK_OPACITY = 0.5;

    /**
     * di snippet中的attr
     */
    DICT.DI_ATTR = 'data-o_o-di';

    /**
     * 自动化测试用的id属性
     */
    DICT.TEST_ATTR = 'data-o_o-di-test';

    /**
     * 指标维度元数据视图状态
     */
    DICT.META_STATUS = {
        DISABLED: 0,
        NORMAL: 1,
        SELECTED: 2
    };

    /**
     * 报表根路径
     */
    DICT.REPORTS = 'reports';

    /**
     * 报表描述文件路径
     */
    DICT.REPORT_JSON = 'report_json';

})();