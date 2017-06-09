/**
 * @file liteolap组件中的chart组件的配置数据信息
 * @author 赵晓强(v_zhaoxiaoqiang@163.com)
 * @date 2014-9-15
 */
define([
        'report/component-box/components/lo-chart-vm-template'
    ],
    function (chartVmTemplate) {

        var entityDescription = [
            {
                markline: '---------liteolap图---------------',
                //id: 'snpt1.cnpt-liteolapchart-meta1',
                "clzType": "COMPONENT",
                "clzKey": "LITEOLAP_META_CONFIG",
                "sync": { "viewDisable": "ALL" },
                "vuiRef": {
                    //"main": "snpt1.vu-liteolapchart-meta1"
                },
                interactions: [
                    {
                        "event": {
                            //"rid": "snpt1.cpnt-table1",
                            "name": "dataloaded"
                        },
                        "action": {
                            "name": "syncLiteOlapInds"
                        }
                    },
                    {
                        "event": {
                            //"rid": "snpt1.cpnt-table1",
                            "name": "rowselect"
                        },
                        "action": {
                            "name": "syncLiteOlapInds"
                        }
                    }
                ],
                "dataOpt": {
                    "needShowCalcInds": true,
                    "submitMode": "IMMEDIATE",
                    "reportType": "RTPL_OLAP_TABLE",
                    "datasourceId": {
                        "SELECT": "LIST_SELECT"
                    }
                }
            },
            {
                //"id": "snpt1.vu-liteolapchart-meta1",
                "clzType": "VUI",
                "clzKey": "OLAP_META_IND_SELECT"
            },
            {
                //"id": "snpt1.cnpt-liteolapchart1",
                "clzType": "COMPONENT",
                "clzKey": "DI_LITEOLAP_CHART",
                "sync": {
                    "viewDisable": "ALL"
                },
                "vuiRef": {
                    //"mainChart": "snpt1.vu-liteolapchart1"
                },
                "interactions": [
                    {
                        "event": {
                            //"rid": "snpt1.cnpt-liteolapchart-meta1",
                            "name": "submit"
                        },
                        "action": {
                            "name": "syncLiteOlapChart"
                        },
                        "argHandlers": [
                            [
                                "clear"
                            ],
                            [
                                "getValue",
                                //"snpt1.vu-liteolapchart-meta1"
                            ]
                        ]
                    }
                ]
            },
            {
                //"id": "snpt1.vu-liteolapchart1",
                "clzType": "VUI",
                "clzKey": "E_CHART",
                "dataOpt": {
                    "legend": {
                        "xMode": "pl"
                    },
                    "weekViewRange": [
                        null,
                        "-1d"
                    ]
                }
            }
        ];

        /**
         * 动态处理json数据
         *
         * @param {Object} dynamicData 动态数据
         * @param {string} dynamicData.rootId 统一的根节点id
         * @param {string} dynamicData.serverData.id 图的id
         * @param {string} dynamicData.serverData.tableAreaId 表的id
         * @return {Array} data 处理完的json配置数据
         */
        function processRenderData (dynamicData) {
            var rootId = dynamicData.rootId;
            var id = rootId + dynamicData.serverData.id;
            var data = $.extend(true, [], this.entityDescription);
            var data0 = data[0];
            var tableId = rootId + dynamicData.serverData.tableAreaId;

            data0.id = id + '.cnpt-liteolapchart-meta';
            data0.vuiRef = {
                "main": id + ".vu-liteolapchart-meta"
            };
            data0.interactions[0].event.rid = tableId;
            data0.interactions[1].event.rid = tableId;

            data[1].id = id + '.vu-liteolapchart-meta';

            data[2].id = id + '.cnpt-liteolapchart';
            data[2].vuiRef.mainChart = id + '.vu-liteolapchart';
            var interactions = data[2].interactions[0];
            interactions.event.rid = id + '.cnpt-liteolapchart-meta';
            interactions.argHandlers[1][1] = id + '.vu-liteolapchart-meta';

            data[3].id = id + '.vu-liteolapchart';
            return data;
        };

        return {
            type: 'CHART',
            caption: '图表',
            iconClass: 'chart',
            defaultWidth: 300,
            defaultHeight: 300,
            vm: {
                render: function (data) {
                    return chartVmTemplate.render({
                        id: data.rootId + data.serverData.id
                    });
                }
            },
            entityDescription: entityDescription,
            processRenderData: processRenderData
        };
    }
);