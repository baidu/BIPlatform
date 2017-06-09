/**
 * repo dict
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    描述构件的引用。构建程序（buidlfront会根据此文件生成repo.js）
 * @author:  sushuang(sushuang)
 */

(function() {

    var DICT = $namespace('di.config').Dict;
    var extend = xutil.object.extend;
    var isArray = xutil.lang.isArray;

    var repoDict =

            //==[DI=BEGIN]==[NIGEB=ID]=========
            // 此注释不可改动，标记了解析段落的开始
            //=================================

        {
            /**
             * 默认的clzKey
             */
            "DEFAULT_CLZ_KEY": {
                "SNIPPET": "GENERAL_SNIPPET",
                "VCONTAINER": "GENERAL_VCONTAINER",
                "VPART": "GENERAL_VPART",
                "COMPONENT": "GENERAL_COMPONENT"
            },

            /**
             * 构件件类
             * 说明：
             * (1") 如果定义了adapterMethod"，则从di.shared.adapter."GeneralAdapterMethod"中获取方法拷贝到目标实例中
             * (2) ""如果定义了adapterPath"，则将该adapter中方法全拷贝至目标实例中。能够覆盖adapterMethod"定义。
             */
            "CLZ_DEFS": [

                //-------------------------------
                // SNIPPET
                //-------------------------------

                {
                    "clzKey": "GENERAL_SNIPPET",
                    "clzPath": "di.shared.ui.GeneralSnippet",
                    "clzType": "SNIPPET"
                },

                //-------------------------------
                // VPART
                //-------------------------------

                {
                    "clzKey": "GENERAL_VPART",
                    "clzPath": "di.shared.ui.GeneralVPart",
                    "clzType": "VPART"
                },

                //-------------------------------
                // COMPONENT
                //-------------------------------

                {
                    "clzKey": "GENERAL_COMPONENT",
                    "clzPath": "di.shared.ui.InteractEntity",
                    "clzType": "COMPONENT"
                },
                {
                    "clzKey": "DI_TABLE",
                    "clzPath": "di.shared.ui.DITable",
                    "clzType": "COMPONENT",
                    "reportTemplateTypeCandidate": ["RTPL_OLAP_TABLE"]
                },
                {
                    "clzKey" : "DI_REPORTSAVE",
                    "clzPath" : "di.shared.ui.DIReportSave",
                    "clzType" : "COMPONENT"
                },
                {
                    "clzKey": "DI_RTPLCLONE",
                    "clzPath": "di.shared.ui.DIRtplClone",
                    "clzType": "COMPONENT"
                },
                {
                    "clzKey": "DI_PLANE_TABLE",
                    "clzPath": "di.shared.ui.DIPlaneTable",
                    "clzType": "COMPONENT",
                    "reportTemplateTypeCandidate": ["RTPL_PLANE_TABLE"]
                },
                {
                    "clzKey": "DI_CHART",
                    "clzPath": "di.shared.ui.DIChart",
                    "clzType": "COMPONENT",
                    "reportTemplateTypeCandidate": ["RTPL_OLAP_CHART"]
                },
                {
                    "clzKey": "DI_ECHART",
                    "clzPath": "di.shared.ui.DIEChart",
                    "clzType": "COMPONENT",
                    "reportTemplateTypeCandidate": ["RTPL_OLAP_CHART"]
                },
                {
                    "clzKey": "DI_LITEOLAP_CHART",
                    "clzPath": "di.shared.ui.DILiteOlapChart",
                    "clzType": "COMPONENT",
                    "reportTemplateTypeCandidate": ["RTPL_OLAP_TABLE"]
                },
                {
                    "clzKey": "DI_FORM",
                    // FIX ME Jack 去repo-config.json 中查找"di.shared.ui.DIForm"，内含集中配置信息
                    "clzPath": "di.shared.ui.DIForm",
                    "clzType": "COMPONENT",
                    "vuiRefCandidate": {
                        "input": [
                            "HIDDEN_INPUT",
                            "DAY_POP_CALENDAR",
                            "RANGE_POP_CALENDAR",
                            "CALENDAR_PLUS",
                            "X_CALENDAR",
                            "RANGE_CALENDAR",
                            "ECUI_SELECT",
                            "ECUI_MULTI_SELECT",
                            "ECUI_INPUT_TREE",
                            "ECUI_SUGGEST",
                            "ECUI_INPUT",
                            "MULTI_CHECKBOX",
                            "SIMPLE_RADIO",
                            "CASCADE_SELECT"
                        ],
                        "confirm": [
                            "BUTTON",
                            "H_BUTTON"
                        ]
                    }
                },
                {
                    "clzKey": "OLAP_META_CONFIG",
                    "clzPath": "di.shared.ui.OlapMetaConfig",
                    "clzType": "COMPONENT",
                    "reportTemplateTypeCandidate": ["RTPL_OLAP_TABLE", "RTPL_OLAP_CHART"]
                },
                {
                    "clzKey": "LITEOLAP_META_CONFIG",
                    "clzPath": "di.shared.ui.LiteOlapMetaConfig",
                    "clzType": "COMPONENT",
                    "reportTemplateTypeCandidate": ["RTPL_OLAP_TABLE", "RTPL_OLAP_CHART"]
                },

                //-------------------------------
                // VCONTAINER
                //-------------------------------

                {
                    "clzKey": "GENERAL_VCONTAINER",
                    "clzPath": "di.shared.ui.GeneralVContainer",
                    "clzType": "VCONTAINER"
                },
                {
                    "clzKey": "DI_TAB",
                    "clzPath": "di.shared.ui.DITab",
                    "clzType": "VCONTAINER"
                },
                {
                    "clzKey": "FOLD_PANEL",
                    "clzPath": "di.shared.ui.FoldPanel",
                    "clzType": "VCONTAINER"
                },

                //-------------------------------
                // VUI
                //-------------------------------

                {
                    "clzKey": "HIDDEN_INPUT",
                    "clzPath": "di.shared.vui.HiddenInput",
                    "adapterMethod": { "create": "xuiCreate", "dispose": "xuiDispose" },
                    "clzType": "VUI",
                    "rtplParamHandler": [
                        "com.baidu.rigel.datainsight.engine.service.impl.DIParamHandler4SimpleTextImpl",
                        "com.baidu.rigel.datainsight.engine.service.impl.DIParamHandler4SuggestTextImpl",
                        "com.baidu.rigel.datainsight.engine.service.impl.DIParamHandler4TreeImpl",
                        "com.baidu.rigel.datainsight.engine.service.impl.DIParamHandler4HttpTreeImpl",
                        "com.baidu.rigel.datainsight.engine.service.impl.DIParamHandler4ComboBoxImpl",
                        "com.baidu.rigel.datainsight.engine.service.impl.DIParamHandler4TimeImpl",
                        "com.baidu.rigel.datainsight.engine.service.impl.DIParamHandler4ComboBoxTransferImpl",
                        "com.baidu.rigel.datainsight.engine.service.impl.DIParamHandler4HiddenPosImpl",
                        "com.baidu.rigel.datainsight.engine.service.impl.DIParamHandler4TimeFromOutsideImpl"
                    ],
                    "caption": "隐藏的输入"
                },
                {
                    "clzKey": "H_CHART",
                    "clzPath": "xui.ui.HChart",
                    "adapterMethod": { "create": "xuiCreate", "dispose": "xuiDispose" },
                    "adapterPath": "di.shared.adapter.HChartVUIAdapter",
                    "clzType": "VUI",
                    "caption": "组合图"
                },
                {
                    "clzKey": "E_CHART",
                    "clzPath": "xui.ui.EChart",
                    "adapterMethod": { "create": "xuiCreate", "dispose": "xuiDispose" },
                    "adapterPath": "di.shared.adapter.EChartVUIAdapter",
                    "clzType": "VUI",
                    "caption": "组合图"
                },
                {
                    "clzKey": "OLAP_META_DRAGGER",
                    "clzPath": "di.shared.vui.OlapMetaDragger",
                    "clzType": "VUI",
                    "adapterPath": "di.shared.adapter.MetaConfigVUIAdapter",
                    "caption": "OLAP维度拖拽选择器"
                },
                {
                    "clzKey": "TEXT_LABEL",
                    "clzPath": "di.shared.vui.TextLabel",
                    "clzType": "VUI",
                    "adapterMethod": { "create": "xuiCreate", "dispose": "xuiDispose" },
                    "caption": "简单文本标签"
                },
                {
                    "clzKey": "OLAP_META_IND_SELECT",
                    "clzPath": "di.shared.vui.OlapMetaSelect",
                    "clzType": "VUI",
                    "adapterMethod": { "create": "xuiCreate", "dispose": "xuiDispose" },
                    "adapterPath": "di.shared.adapter.MetaConfigVUIAdapter",
                    "dataOpt": {
                        "ctrlClz": "ecui.ui.Select",
                        "optionSize": 15
                    },
                    "caption": "OLAP指标单选下拉框"
                },
                {
                    "clzKey": "OLAP_META_IND_MULTI_SELECT",
                    "clzPath": "di.shared.vui.OlapMetaSelect",
                    "clzType": "VUI",
                    "adapterMethod": { "create": "xuiCreate", "dispose": "xuiDispose" },
                    "adapterPath": "di.shared.adapter.MetaConfigVUIAdapter",
                    "dataOpt": {
                        "ctrlClz": "ecui.ui.MultiSelect",
                        "optionSize": 15
                    },
                    "caption": "OLAP指标多选下拉框"
                },
                {
                    "clzKey": "DAY_POP_CALENDAR",
                    "clzPath": "ecui.ui.IstCalendar",
                    "clzType": "VUI",
                    "adapterMethod": { "dispose": "ecuiDispose" },
                    "adapterPath": "di.shared.adapter.IstCalendarVUIAdapter",
                    "dataOpt": {
                        "mode": "DAY",
                        "viewMode": "POP"
                    },
                    "rtplParamHandler": "com.baidu.rigel.datainsight.engine.service.impl.DIParamHandler4TimeImpl",
                    "caption": "简单弹出式日历（单日选择）"
                },
                {
                    "clzKey": "RANGE_POP_CALENDAR",
                    "clzPath": "ecui.ui.IstCalendar",
                    "clzType": "VUI",
                    "adapterMethod": { "dispose": "ecuiDispose" },
                    "adapterPath": "di.shared.adapter.IstCalendarVUIAdapter",
                    "dataOpt": {
                        "mode": "RANGE",
                        "viewMode": "POP"
                    },
                    "rtplParamHandler": "com.baidu.rigel.datainsight.engine.service.impl.DIParamHandler4TimeImpl",
                    "caption": "简单弹出式日历（范围选择）"
                },
                {
                    "clzKey": "CALENDAR_PLUS",
                    "clzPath": "ecui.ui.CalendarPlus",
                    "clzType": "VUI",
                    "adapterMethod": { "dispose": "ecuiDispose" },
                    "adapterPath": "di.shared.adapter.CalendarPlusVUIAdapter",
                    "rtplParamHandler": "com.baidu.rigel.datainsight.engine.service.impl.DIParamHandler4TimeImpl",
                    "editorDisable": true,
                    "caption": "混合日历（季月周切换）"
                },
                {
                    "clzKey": "X_CALENDAR",
                    "clzPath": "ecui.ui.XCalendar",
                    "clzType": "VUI",
                    "adapterMethod": { "dispose": "ecuiDispose" },
                    "adapterPath": "di.shared.adapter.XCalendarVUIAdapter",
                    "rtplParamHandler": "com.baidu.rigel.datainsight.engine.service.impl.DIParamHandler4TimeImpl",
                    "caption": "混合日历（季月周切换＋单选范围选切换）"
                },
                {
                    "clzKey": "RANGE_CALENDAR",
                    "clzPath": "di.shared.vui.RangeCalendar",
                    "adapterMethod": { "create": "xuiCreate", "dispose": "xuiDispose" },
                    "clzType": "VUI",
                    "caption": "隐藏的输入"
                },
                {
                    "clzKey": "OLAP_TABLE",
                    "clzPath": "ecui.ui.OlapTable",
                    "clzType": "VUI",
                    "adapterMethod": { "create": "ecuiCreate", "dispose": "ecuiDispose" },
                    "dataOpt": { "defaultCCellAlign": "right" },
                    "caption": "多维表（透视表/交叉表）"
                },
                {
                    "clzKey": "BEAKER_CHART",
                    "clzPath": "ecui.ui.BeakerChart",
                    "clzType": "VUI",
                    "adapterPath": "di.shared.adapter.BeakerChartVUIAdapter",
                    "caption": "烧杯图"
                },
                {
                    "clzKey": "BREADCRUMB",
                    "clzPath": "ecui.ui.Breadcrumb",
                    "clzType": "VUI",
                    "adapterMethod": { "create": "ecuiCreate", "dispose": "ecuiDispose" },
                    "caption": "面包屑"
                },
                {
                    "clzKey": "BUTTON",
                    "clzPath": "ecui.ui.Button",
                    "clzType": "VUI",
                    "adapterMethod": { "create": "ecuiCreate", "dispose": "ecuiDispose" },
                    "caption": "简单按钮"
                },
                {
                    "clzKey": "H_BUTTON",
                    "clzPath": "ecui.ui.HButton",
                    "clzType": "VUI",
                    "adapterMethod": { "create": "ecuiCreate", "dispose": "ecuiDispose" },
                    "caption": "带图标按钮"
                },
                {
                    "clzKey" : "SAVE_BUTTON",
                    "clzPath" : "di.shared.vui.SaveButton",
                    "clzType" : "VUI",
                    "adapterMethod" : {
                        "create" : "xuiCreate",
                        "dispose" : "xuiDispose"
                    }
                },
                {
                    "clzKey" : "TAB_BUTTON",
                    "clzPath" : "di.shared.vui.TabButton",
                    "clzType" : "VUI",
                    "adapterMethod" : {
                        "create" : "xuiCreate",
                        "dispose" : "xuiDispose"
                    }
                },
                {
                    "clzKey": "OFFLINE_DOWNLOAD",
                    "clzPath": "di.shared.vui.OfflineDownload",
                    "clzType": "VUI",
                    "adapterMethod": { "create": "xuiCreate", "dispose": "xuiDispose" },
                    "dataOpt": {
                        "headText": "请输入邮箱（多个邮箱使用逗号分隔）：",
                        "confirmText": "确定",
                        "cancelText": "取消",
                        "text": "离线下载1"
                    },
                    "caption": "离线下载按钮"
                },
                {
                    "clzKey" : "FIELDS_FILTER",
                    "clzPath" : "di.shared.vui.FieldsFilter",
                    "clzType" : "VUI",
                    "adapterMethod" : {
                        "create" : "xuiCreate",
                        "dispose" : "xuiDispose"
                    }
                },
                {
                    "clzKey" : "RICH_SELECT",
                    "clzPath" : "di.shared.vui.RichSelect",
                    "clzType" : "VUI",
                    "adapterMethod" : {
                        "create" : "xuiCreate",
                        "dispose" : "xuiDispose"
                    }
                },
                {
                    "clzKey": "SWITCH_BUTTON",
                    "clzPath": "ecui.ui.SwitchButton",
                    "clzType": "VUI",
                    "adapterMethod": { "create": "ecuiCreate", "dispose": "ecuiDispose" },
                    "caption": "开关按钮"
                },
                {
                    "clzKey": "ECUI_SELECT",
                    "clzPath": "ecui.ui.Select",
                    "clzType": "VUI",
                    "adapterMethod": { "create": "ecuiCreate", "dispose": "ecuiDispose" },
                    "adapterPath": "di.shared.adapter.EcuiSelectVUIAdapter",
                    "dataOpt": {
                        "optionSize": 15
                    },
                    "rtplParamHandler": "com.baidu.rigel.datainsight.engine.service.impl.DIParamHandler4ComboBoxImpl",
                    "caption": "单选下拉框"
                },
                {
                    "clzKey": "ECUI_MULTI_SELECT",
                    "clzPath": "ecui.ui.MultiSelect",
                    "clzType": "VUI",
                    "adapterMethod": { "create": "ecuiCreate", "dispose": "ecuiDispose" },
                    "adapterPath": "di.shared.adapter.EcuiSelectVUIAdapter",
                    "dataOpt": {
                        "optionSize": 15
                    },
                    "rtplParamHandler": "com.baidu.rigel.datainsight.engine.service.impl.DIParamHandler4ComboBoxImpl",
                    "caption": "多选下拉框"
                },
                {
                    "clzKey": "ECUI_INPUT_TREE",
                    "clzPath": "ecui.ui.InputTree",
                    "clzType": "VUI",
                    "adapterMethod": { "create": "ecuiCreate", "dispose": "ecuiDispose" },
                    "adapterPath": "di.shared.adapter.EcuiInputTreeVUIAdapter",
                    "rtplParamHandler": [
                        "com.baidu.rigel.datainsight.engine.service.impl.DIParamHandler4TreeImpl",
                        "com.baidu.rigel.datainsight.engine.service.impl.DIParamHandler4HttpTreeImpl"
                    ],
                    "caption": "树结构选择下拉框"
                },
                {
                    "clzKey": "ECUI_SUGGEST",
                    "clzPath": "ecui.ui.Suggest",
                    "clzType": "VUI",
                    "adapterMethod": { "create": "ecuiCreate", "dispose": "ecuiDispose" },
                    "adapterPath": "di.shared.adapter.EcuiSuggestVUIAdapter",
                    "rtplParamHandler": "com.baidu.rigel.datainsight.engine.service.impl.DIParamHandler4SuggestTextImpl",
                    "caption": "带提示的输入框"
                },
                {
                    "clzKey": "ECUI_INPUT",
                    "clzPath": "ecui.ui.Input",
                    "clzType": "VUI",
                    "adapterMethod": { "create": "ecuiCreate", "dispose": "ecuiDispose" },
                    "rtplParamHandler": "com.baidu.rigel.datainsight.engine.service.impl.DIParamHandler4SimpleTextImpl",
                    "caption": "简单输入框"
                },
                {
                    "clzKey": "MULTI_CHECKBOX",
                    "clzPath": "di.shared.vui.MultiCheckbox",
                    "adapterMethod": { "create": "xuiCreate", "dispose": "xuiDispose" },
                    "clzType": "VUI",
                    "rtplParamHandler": [
                        "复选框（平铺）",
                        "com.baidu.rigel.datainsight.engine.service.impl.DIParamHandler4ComboBoxImpl"
                    ],
                    "caption": "复选框（平铺）"
                },
                {
                    "clzKey": "SIMPLE_RADIO",
                    "clzPath": "di.shared.vui.SimpleRadio",
                    "adapterMethod": { "create": "xuiCreate", "dispose": "xuiDispose" },
                    "clzType": "VUI",
                    "rtplParamHandler": [
                        "com.baidu.rigel.datainsight.engine.service.impl.DIParamHandler4ComboBoxImpl"
                    ],
                    "caption": "单选框（平铺）"
                },
                {
                    "clzKey": "ECUI_SLOW_PLANE_TABLE",
                    "clzPath": "ecui.ui.CustomTable",
                    "clzType": "VUI",
                    "adapterMethod": { "create": "ecuiCreate", "dispose": "ecuiDispose" },
                    "adapterPath": "di.shared.adapter.EcuiCustomTableVUIAdapter",
                    "caption": "平面表"
                },
                {
                    "clzKey": "ECUI_PAGER",
                    "clzPath": "ecui.ui.ExtPager",
                    "clzType": "VUI",
                    "adapterMethod": { "create": "ecuiCreate", "dispose": "ecuiDispose" },
                    "adapterPath": "di.shared.adapter.EcuiPagerVUIAdapter",
                    "caption": "分页控件"
                },
                {
                    "clzKey": "CASCADE_SELECT",
                    "clzType": "VUI",
                    "adapterMethod": { "create": "xuiCreate", "dispose": "xuiDispose" },
                    "clzPath": "di.shared.vui.CascadeSelect",
                    "caption": "级联下拉框"
                }
            ]

        }

    //==[DI=END]==[DNE=ID]=============
    // 此注释不可改动，标记了解析段落的结束
    //=================================

        ;
    extend(DICT, repoDict);

    /**
     * 类引用处理
     */
    DICT.CLZ = {};
    for (var i = 0, clzDef; clzDef = DICT.CLZ_DEFS[i]; i ++) {
        if (clzDef.clzKey in DICT.CLZ) {
            throw new Error('dupicate clzKey: ' + clzDef.clzKey);
        }
        DICT.CLZ[clzDef.clzKey] = clzDef;

        // 规范化
        var handlers = clzDef.rtplParamHandler = clzDef.rtplParamHandler || [];
        if (!isArray(handlers)) {
            clzDef.rtplParamHandler = [handlers];
        }
    }

    /**
     * @public
     */
    DICT.findClzDef = function (clzKey, clzType) {
        for (var i = 0, def; def = DICT.CLZ_DEFS[i]; i ++) {
            if (def.clzKey == clzKey && def.clzType == clzType) {
                return def;
            }
        }
    };

    /**
     * @public
     */
    DICT.hasReportTemplateType = function (clzKey, reportTemplateType) {
        var cmpt = DICT.findClzDef(clzKey, 'COMPONENT');
        if (!cmpt) {
            return false;
        }

        // reportTemplateType全适用的情况
        if (!cmpt.reportTemplateTypeCandidate) {
            return true;
        }

        for (var i = 0, type; type = cmpt.reportTemplateTypeCandidate[i]; i ++) {
            if (reportTemplateType == type) {
                return true;
            }
        }

        return false;
    };

})();