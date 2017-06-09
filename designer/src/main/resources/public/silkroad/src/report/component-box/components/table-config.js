/**
 * @file 表格组件的配置数据信息
 * @author 赵晓强(v_zhaoxiaoqiang@163.com)
 * @date 2014-9-10
 */
define([
        'report/component-box/components/table-vm-template'
    ],
    function (tableVmTemplate) {

        var entityDescription = [
            {
                "configType": "DI_TABLE", // 用于标示form处理方式
                "clzType": "COMPONENT",
                "clzKey": "DI_TABLE",
                "sync": { "viewDisable": "ALL" },
                "vuiRef": {

                },
                interactions: [
                    {
                        events: [
                            {
                                "rid": "snpt.form",
                                "name": "dataloaded"
                            },
                            {
                                rid: "snpt.form",
                                name: "submit"
                            }
                        ],
                        action: {
                            name: "sync"
                        },
                        argHandlers: [
                            ["clear"],
                            ["getValue", "snpt.cnpt-form"]
                        ]
                    }
                ]
            },
            {
                "clzType": "VUI",
                "clzKey": "OLAP_TABLE",
                "name": "table",
                "dataOpt": {
                    "rowHCellCut": 30,
                    "hCellCut": 30,
                    "cCellCut": 30,
                    "vScroll": true,
                    "rowCheckMode": "SELECTONLY"
                }
            },
            {
                "clzType": "VUI",
                "clzKey": "BREADCRUMB",
                "dataOpt": {
                    "maxShow": 6
                }
            },
            {
                "clzType": "VUI",
                "clzKey": "H_BUTTON",
                "dataOpt": {
                    "skin": "ui-download-btn",
                    "text": "导出当前所有数据"
                }
            },
            {
                "clzType": "VUI",
                "clzKey": "TEXT_LABEL",
                "tpl": "符合条件的数据只显示#{currRecordCount}条",
                "dataInitOpt": { "hide": true }
            }
            //{
            //    "clzType": "VUI",
            //    "clzKey": "RICH_SELECT"
            //}
        ];
        var processRenderData = function (dynamicData) {
            var id = dynamicData.rootId + dynamicData.serverData.id;
            var data = $.extend(true, [], entityDescription);
            data[0].id = id;
            data[0].vuiRef = {
                "mainTable": id + "-vu-table",
                "breadcrumb": id + "-vu-table-breadcrumb",
                "download": id + "-vu-table-download",
                "countInfo": id + "-vu-table-count"
                //richSelect: id + '-vu-table-rich-select'
            };

            // 如果有拖拽区域
            if (dynamicData.hasTableMeta) {
                data[0].interactions.push(
                    addTableMetaEvent(
                        dynamicData.rootId
                        + dynamicData.serverData.selectionAreaId
                        + '.cnpt-table-meta'
                    )
                );
            }

            data[1].id = id + '-vu-table';
            data[2].id = id + '-vu-table-breadcrumb';
            data[3].id = id + '-vu-table-download';
            data[4].id = id + '-vu-table-count';
            //data[5].id = id + '-vu-table-rich-select';
            return data;
        };

        // 添加表格对拖拽区域的事件关联
        function addTableMetaEvent(id) {
           return {
                "event": {
                    "rid": id,
                    "name": "submit"
                },
                "action": {
                    "name": "sync"
                },
                "argHandlers": [
                    [
                        "clear"
                    ],
                    [
                        "getValue",
                        "snpt.cnpt-form"
                    ]
                ]
            };
        }
        return {
            type: 'TABLE',
            caption: '表格',
            renderClass: '', // 渲染时需要的外层class
            iconClass: 'table', // 工具箱中 与 拖拽过程中 的class，样式表中通过外层来复用
            defaultWidth: 500,
            defaultHeight: 289,
            vm: {
                render: function (data) {
                    return tableVmTemplate.render({
                        id: data.rootId + data.serverData.id
                    });
                }
            },
            entityDescription: entityDescription,
            processRenderData: processRenderData
        };
    }
);