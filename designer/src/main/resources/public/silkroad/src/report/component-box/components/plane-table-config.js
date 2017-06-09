/**
 * @file 表格组件的配置数据信息
 * @author 赵晓强(v_zhaoxiaoqiang@163.com)
 * @date 2014-9-10
 */
define([
        'report/component-box/components/plane-table-vm-template'
    ],
    function (tableVmTemplate) {

        var entityDescription = [
            {
                configType: 'DI_PLANE_TABLE', // 用于标示form处理方式
                clzType: 'COMPONENT',
                clzKey: 'DI_PLANE_TABLE',
                sync: {viewDisable: 'ALL'},
                vuiRef: {
                },
                interactions: [
                    {
                        events: [
                            {
                                rid: 'snpt.form',
                                name: 'dataloaded'
                            },
                            {
                                rid: 'snpt.form',
                                name: 'submit'
                            }
                        ],
                        action: {
                            name: 'sync'
                        },
                        argHandlers: [
                            ['clear'],
                            ['getValue', 'snpt.cnpt-form']
                        ]
                    }
                ]
            },
            {
                clzType: 'VUI',
                clzKey: 'ECUI_SLOW_PLANE_TABLE',
                name: 'table',
                dataOpt: {
                    rowHCellCut: 30,
                    hCellCut: 30,
                    cCellCut: 30,
                    vScroll: true
                }
            },
            {
                clzType: 'VUI',
                clzKey: 'H_BUTTON',
                dataOpt: {
                    skin: 'ui-download-btn',
                    text: '导出当前所有数据'
                }
            },
            {
                clzType: 'VUI',
                clzKey: 'ECUI_PAGER',
                dataOpt: {
                    pgeSize: 10,
                    pageSizeOptions: [10, 50, 100]
                }
            }
//            {
//                clzType: 'VUI',
//                clzKey: 'FIELDS_FILTER'
//            }
        ];
        var processRenderData = function (dynamicData) {
            var id = dynamicData.rootId + dynamicData.serverData.id;
            var data = $.extend(true, [], entityDescription);
            data[0].id = id;
            data[0].vuiRef = {
                mainTable: id + '-vu-table',
                download: id + '-vu-table-download',
                pager: id + '-vu-table-pager'
//                fieldsFilter: id + '-vu-table-fieldsFilter'
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
            data[2].id = id + '-vu-table-download';
            data[3].id = id + '-vu-table-pager';
//            data[4].id = id + '-vu-table-fieldsFilter';

            return data;
        };

        // 添加表格对拖拽区域的事件关联
        function addTableMetaEvent(id) {
            return {
                event: {
                    rid: id,
                    name: 'submit'
                },
                action: {
                    name: 'sync'
                },
                argHandlers: [
                    [
                        'clear'
                    ],
                    [
                        'getValue',
                        'snpt.cnpt-form'
                    ]
                ]
            };
        }

        return {
            type: 'PLANE_TABLE',
            caption: '平面表',
            renderClass: '', // 渲染时需要的外层class
            iconClass: 'plane-table', // 工具箱中 与 拖拽过程中 的class，样式表中通过外层来复用
            defaultWidth: 600,
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