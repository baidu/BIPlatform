/**
 * @file:   多选下拉框配置信息
 * @author: weiboxue(wbx_901118@sina.com)
 * @date:   2014/12/3
 */
define(
    [
        'constant',
        'report/component-box/components/multi-select-vm-template'
    ],
    function (
        Constant,
        MultiselectVmTemplate
    ) {
        // 多选下拉框id后缀
        var multiselectIdSuffix = Constant.COMPONENT_ID_SUFFIX.MULTISELECT;

        // 多选下拉框 实例 描述信息（从report-ui里面获取）
        var entityDescription = {
            "clzType": "VUI",
            "clzKey": "ECUI_MULTI_SELECT"
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
            data.id = id + multiselectIdSuffix;
            data.name = dynamicData.serverData.id;
            return data;
        }

        return {
            type: 'MULTISELECT',
            iconClass: 'multiselect',
            caption: '多选下拉框',
            defaultWidth: 300,
            defaultHeight: 27,
            vm: {
                render: function (data) {
                    return MultiselectVmTemplate.render({
                        id: data.rootId + data.serverData.id + multiselectIdSuffix
                    });
                }
            },
            processRenderData: processRenderData,
            entityDescription: entityDescription
        };

    }
);
