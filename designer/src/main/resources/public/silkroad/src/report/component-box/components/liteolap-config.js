/**
 * @file liteolap组件的配置数据信息
 * @author 赵晓强(v_zhaoxiaoqiang@163.com)
 * @date 2014-9-11
 */
define([
        'report/component-box/components/liteolap-vm-template',
        'report/component-box/components/table-config',
        'report/component-box/components/lo-chart-config'
    ],
    function (liteolapVmTemplate, tableModel, chartModel) {
        // 拖拽部分的json数据，不区分有没有form
        var selectionData = [
            {
                //"id": "snpt1.cnpt-table-meta1",
                "clzType": "COMPONENT",
                "clzKey": "OLAP_META_CONFIG",
                "sync": {
                    "viewDisable": "ALL"
                },
                "vuiRef": {
                    //"main": "snpt1.vu-table-meta1"
                },
                "interactions": [
                    {
                        "rule": {
                            "operator": "excludes"
                        },
                        "event": {
                            "name": "dataloaded.DATA"
                        },
                        "action": {
                            "name": "sync"
                        }
                    },
                    {
                        "event": {
                            //"rid": "snpt1.cnpt-table1",
                            "name": "dataloaded.LINK_DRILL"
                        },
                        "action": {
                            "name": "sync"
                        }
                    },
                    {
                        "event": {
                            //"rid": "snpt1.cnpt-table1",
                            "name": "dataloaded"
                        },
                        "action": {
                            "name": "sync"
                        }
                    }
                ],
                "dataOpt": {
                    "needShowCalcInds": true,
                    "reportType": "RTPL_OLAP_TABLE",
                    "submitMode": "IMMEDIATE"
                }
            },
            {
                //"id": "snpt1.vu-table-meta1",
                "clzType": "VUI",
                "clzKey": "OLAP_META_DRAGGER"
            },

            // 拖拽容器
            {
                markline: '---------liteolap拖拽容器---------------',
                //"id": "snpt1.vctnr-fold1",
                "clzType": "VCONTAINER",
                "clzKey": "FOLD_PANEL",
                "vpartRef": {
                    //"ctrlBtn": "snpt1.vpt-fold1-ctrlbtn1",
                    //"body": "snpt1.vpt-fold1-body1"
                },
                "dataOpt": {
                    "autoDeaf": false,
                    "autoComponentValueDisabled": false,
                    "autoVUIValueDisabled": false,
                    "defaultHide": false
                }
            },
            {
                //"id": "snpt1.vpt-fold1-body1",
                "clzType": "VPART"
            },
            {
                //"id": "snpt1.vpt-fold1-ctrlbtn1",
                "clzType": "VPART",
                "dataOpt": {
                    "expandText": "表格设置",
                    "collapseText": "收起表格设置"
                }
            }
        ];

        /**
         * 重组动态数据填充
         *
         * @param {Object} dynamicData 动态数据
         * @return {Object} 处理完成的数据
         */
        function processRenderData (dynamicData) {

            // 动态处理liteolap的拖拽区json数据
            var selectionData = processSelectionData(dynamicData);

            var tableRenderData = tableModel.processRenderData({
                rootId: dynamicData.rootId,
                serverData: {
                    id: dynamicData.serverData.tableAreaId,
                    selectionAreaId: dynamicData.serverData.selectionAreaId
                },
                hasTableMeta: true
            });
            // 这里需要把从table-config集成过来的olap-table的rowCheckMode由原有的SELECTONLY改为SELECT，不然表图联动会不起作用，updata by majun
            tableRenderData[1].dataOpt.rowCheckMode = 'SELECT';
            
            var chartRenderData = chartModel.processRenderData({
                rootId: dynamicData.rootId,
                serverData: {
                    id: dynamicData.serverData.chartAreaId,
                    tableAreaId: dynamicData.serverData.tableAreaId
                }
            });

            selectionData = selectionData.concat(tableRenderData);
            selectionData = selectionData.concat(chartRenderData);

            return selectionData;
        };

        /**
         * 动态处理json数据
         *
         * @param {Object} dynamicData 动态数据
         * @param {string} dynamicData.id LiteOlap的id
         * @param {string} dynamicData.selectionAreaId 选择区的id
         * @param {string} dynamicData.tableAreaId 表的id
         * @param {string} dynamicData.chartAreaId 图的id
         * @param {boolean} dynamicData.hasForm 是否有form在报表中
         *
         * return {Array} sData 配置信息
         */
        function processSelectionData(dynamicData) {
            var rootId = dynamicData.rootId;
            var selectionId = rootId + dynamicData.serverData.selectionAreaId;
            var tableId = rootId + dynamicData.serverData.tableAreaId;
            // selectionData
            var sData = $.extend(true, [], selectionData);

            var data1 = sData[0];
            var data2 = sData[1];
            var data3 = sData[2];
            var data4 = sData[3];
            var data5 = sData[4];

            data1.id = selectionId + '.cnpt-table-meta';
            data1.vuiRef.main = selectionId + '.vu-table-meta';

            data1.interactions[0].event.rid = tableId;
            data1.interactions[1].event.rid = tableId;
            data1.interactions[2].event.rid = tableId;

            data2.id = selectionId + '.vu-table-meta';

            data3.id = selectionId + '.vctnr-fold';
            data3.vpartRef.ctrlBtn = selectionId + '.vpt-fold-ctrlbtn';
            data3.vpartRef.body = selectionId + '.vpt-fold-body';

            data4.id = selectionId + '.vpt-fold-body';
            data5.id = selectionId + '.vpt-fold-ctrlbtn';

            return sData;
        }

        return {
            type: 'LITEOLAP',
            caption: '透视表',
            renderClass: '', // 渲染时需要的外层class
            iconClass: 'liteolap', // 工具箱中 与 拖拽过程中 的class，样式表中通过外层来复用
            defaultWidth: 900,
            defaultHeight: 500,
            vm: {
                render: function (data) {
                    var id = data.rootId + data.serverData.selectionAreaId;
                    var liteolapHtml = liteolapVmTemplate.render({
                        id: id
                    });
                    var tableHtml = tableModel.vm.render({
                        rootId: data.rootId,
                        serverData: {
                            id: data.serverData.tableAreaId
                        }
                    });
                    tableHtml = tableHtml.replace('class="comp-box di-o_o-block"', 'class="di-o_o-block"');

                    var chartHtml = chartModel.vm.render({
                        rootId: data.rootId,
                        serverData: {
                            id: data.serverData.chartAreaId
                        }
                    });
                    return '<div class="comp-box j-comp-box">' + liteolapHtml + tableHtml + chartHtml + '</div>';
                }
            },
            processRenderData: processRenderData
        };
    }
);