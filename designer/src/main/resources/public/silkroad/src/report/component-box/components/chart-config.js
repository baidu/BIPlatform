/**
 * @file chart组件的配置数据信息
 * @author 赵晓强(v_zhaoxiaoqiang@163.com)
 * @date 2014-9-10
 */
define(
    [
        'constant',
        'report/component-box/components/chart-vm-template'
    ],
    function (
        Constant,
        ChartVmTemplate
    ) {
        // Chart图形 id后缀
        var chartIdSuffix = Constant.COMPONENT_ID_SUFFIX.CHART;

        // Chart图形 实例 描述信息（从report-ui里面获取）
        var entityDescription = [
            {
                clzType: 'COMPONENT',
                clzKey: 'DI_ECHART',
                sync: {viewDisable: 'ALL'},
                vuiRef: {},
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
                clzKey: 'E_CHART',
                dataOpt: {
                    height: 260,
                    legend: {xMode: 'pl'},
                    weekViewRange: [null, '-1d']
                }
            }
        ];

        var processRenderData = function (dynamicData) {
            var id = dynamicData.rootId + dynamicData.serverData.id;
            var data = $.extend(true, [], entityDescription);
            data[0].id = id;
            data[0].vuiRef = {
                "mainChart": id + chartIdSuffix
            };
            data[1].id = id + chartIdSuffix;
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
                    var renderData = {
                        id: data.rootId + data.serverData.id
                    };
                    return ChartVmTemplate.render(renderData);
                }
            },
            entityDescription: entityDescription,
            processRenderData: processRenderData
        };
    });