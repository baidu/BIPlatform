/**
 * @file:   form（component）中对应cascade-select（vui）的配置信息
 * @author: weiboxue(wbx_901118@sina.com)
 * @date:   2015/02/04
 */
define(
    [
        'constant',
        'report/component-box/components/cascade-select-vm-template'
    ],
    function (
        Constant,
        CasCadeSelectVmTemplate
    ) {

        // 单选下拉框id后缀
        var selectIdSuffix = Constant.COMPONENT_ID_SUFFIX.CASCADE_SELECT;

        // 单选下拉框 实例 描述信息（从report-ui里面获取）
        var entityDescription = {
            clzType: 'VUI',
            clzKey: 'CASCADE_SELECT'
        };

        /**
         * 处理渲染数据（json的数据）
         *
         * @param {Object} dynamicData 动态数据
         *
         * @private
         * @return {Object} 处理之后的数据
         */
        function processRenderData(dynamicData) {
            var id = dynamicData.rootId + dynamicData.serverData.id;
            var data = $.extend(true, {}, entityDescription);

            data.id = id + selectIdSuffix;
            data.name = dynamicData.serverData.id;

            return data;
        }

        return {
            type: 'CASCADE_SELECT',
            iconClass: 'cascade-select',
            caption: '级联下拉框',
            defaultWidth: 450,
            defaultHeight: 33,
            defaultValue: 'false',
            vm: {
                render: function (data) {
                    return CasCadeSelectVmTemplate.render({
                        id: data.rootId + data.serverData.id + selectIdSuffix
                    });
                }
            },
            processRenderData: processRenderData,
            entityDescription: entityDescription
        };

    }
);