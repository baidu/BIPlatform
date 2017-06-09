/**
 * @file
 * @author 赵晓强(longze_xq@163.com)
 * @date 2014-8-4
 */
define([
        'report/component-box/components/table-config',
        'report/component-box/components/chart-config',
        'report/component-box/components/calendar-config',
        'report/component-box/components/liteolap-config',
        'report/component-box/components/form-config',
        'report/component-box/components/select-config',
        'report/component-box/components/text-config',
        'report/component-box/components/h-button-config',
        'report/component-box/components/ecui-input-tree-config',
        'report/component-box/components/cascade-select-config',
        'report/component-box/components/plane-table-config',
        'report/component-box/components/report-save-config'
    ],
    function (
        tableConfig,
        chartConfig,
        calendarConfig,
        liteolapConfig,
        formConfig,
        selectConfig,
        textConfig,
        hbtnConfig,
        ecuiInputTreeConfig,
        cascadeSelectConfig,
        planeTableConfig,
        reportSaveConfig
    ) {
        var rootId = 'snpt';

        return Backbone.Model.extend({
            url: 'reports/',
            initialize: function () {

            },
            config: {
                defaultJson: {
                    "desc": "查询条件||多维表格",
                    "diKey": "DEPICT",
                    "clzDefs": [
                        {
                            "clzKey": "OLAP_TABLE",
                            "dataOpt": {
                                "emptyHTML": "未查询到相关数据"
                            }
                        },
                        {
                            "clzKey": "ECUI_SELECT",
                            "dataOpt": {
                                "optionSize": 10
                            }
                        },
                        {
                            "clzKey": "ECUI_MULTI_SELECT",
                            "dataOpt": {
                                "optionSize": 10
                            }
                        }
                    ],
                    "entityDefs": [
                        {
                            "id": rootId,
                            "clzType": "SNIPPET"
                        }
                    ]
                },
                defaultVm: '<div data-o_o-di="' + rootId + '" class="di-o_o-body"></div>',
                global: '', //全局配置，
                componentList: [
                    {
                        id: '2',
                        caption: '数据展示组件',
                        items: [
                            tableConfig,
                            chartConfig,
                            calendarConfig,
                            selectConfig,
                            liteolapConfig,
                            textConfig,
                            hbtnConfig,
                            ecuiInputTreeConfig,
                            cascadeSelectConfig,
                            planeTableConfig,
                            reportSaveConfig
                        ]
                    }
                ],
                formModel: formConfig
            },
            getComponentData: function (type) {
                var list = this.config.componentList;
                var items;

                for (var i = 0, iLen = list.length; i < iLen; i++) {
                    items = list[i].items;
                    for (var j = 0, jLen = items.length; j < jLen; j++) {
                        if (items[j].type == type) {
                            return items[j];
                        }
                    }
                }
            }
        });
    });