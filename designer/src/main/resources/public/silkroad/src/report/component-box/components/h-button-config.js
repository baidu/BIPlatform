/**
 * @file:   form（component）中对应h-button（vui）的配置信息
 * @author: lzt(lztlovely@126.com)
 * @date:   2014/11/26
 */
define(
    [
        'constant',
        'report/component-box/components/h-button-vm-template'
    ],
    function (
        Constant,
        SelectVmTemplate
    ) {
        // 单选下拉框id后缀
        var hButtonIdSuffix = Constant.COMPONENT_ID_SUFFIX.H_BUTTON;

        // 单选下拉框 实例 描述信息（从report-ui里面获取）
        var entityDescription = {
            'clzType': 'VUI',
            'clzKey': 'H_BUTTON',
            'dataOpt': {
                'skin': 'ui-normal-btn',
                'text': '查询'
            }
        };

        /**
         * 处理渲染数据（json的数据）
         *
         * @param {Object} dynamicData 动态数据
         * @private
         * @return {Object} 处理之后的数据
         */
        function processRenderData(dynamicData) {
            var id = dynamicData.rootId + dynamicData.serverData.id;
            var data = $.extend(true, {}, entityDescription);
            data.id = id + hButtonIdSuffix;
            data.name = dynamicData.serverData.id;
            return data;
        }

        return {
            type: 'H_BUTTON',
            iconClass: 'h-button',
            caption: '查询',
            defaultWidth: 67,
            defaultHeight: 33,
            vm: {
                render: function (data) {
                    return SelectVmTemplate.render({
                        id: data.rootId + data.serverData.id + hButtonIdSuffix
                    });
                }
            },
            processRenderData: processRenderData,
            entityDescription: entityDescription
        };

    }
);