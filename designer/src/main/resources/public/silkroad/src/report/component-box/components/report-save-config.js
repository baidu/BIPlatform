/**
 * @file: 报表保存（report-save）组件的配置数据信息
 * @author: lizhantong
 * @date: 2015-07-20
 */
define(
    [
        'constant',
        'report/component-box/components/report-save-vm-template'
    ],
    function (
        Constant,
        ReportSaveVmTemplate
    ) {
        // 报表保存功能 id后缀
        var tabBtnIdSuffix = Constant.COMPONENT_ID_SUFFIX.TAB_BUTTON;
        var saveBtnIdSuffix = Constant.COMPONENT_ID_SUFFIX.SAVE_BUTTON;

        // 报表保存功能 实例 描述信息（从report-ui里面获取）
        var entityDescription = [
            {
                clzType: 'COMPONENT',
                clzKey: 'DI_REPORTSAVE',
                vuiRef: {},
                maxTabNum: 5
            },
            {
                clzType: 'VUI',
                clzKey: 'SAVE_BUTTON'
            },
            {
                clzType: 'VUI',
                clzKey: 'TAB_BUTTON'
            }
        ];

        var processRenderData = function (dynamicData) {
            var id = dynamicData.rootId + dynamicData.serverData.id;
            var data = $.extend(true, [], entityDescription);
            data[0].id = id;
            data[0].vuiRef = {
                saveRptSave: id + saveBtnIdSuffix,
                saveRptTab: id + tabBtnIdSuffix
            };
            data[1].id = id + saveBtnIdSuffix;
            data[2].id = id + tabBtnIdSuffix;
            return data;
        };

        return {
            type: 'REPORT_SAVE_COMP',
            caption: '报表保存',
            iconClass: 'report-save',
            defaultWidth: 130,
            defaultHeight: 30,
            vm: {
                render: function (data) {
                    var renderData = {
                        id: data.rootId + data.serverData.id
                    };
                    return ReportSaveVmTemplate.render(renderData);
                }
            },
            entityDescription: entityDescription,
            processRenderData: processRenderData
        };
    });