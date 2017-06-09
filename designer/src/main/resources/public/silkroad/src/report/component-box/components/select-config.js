/**
 * @file:   form（component）中对应select（vui）的配置信息
 * @author: lzt(lztlovely@126.com)
 * @date:   2014/11/26
 */
define(
    [
        'constant',
        'report/component-box/components/select-vm-template'
    ],
    function (
        Constant,
        SelectVmTemplate
    ) {
        // 单选下拉框id后缀
        var selectIdSuffix = Constant.COMPONENT_ID_SUFFIX.SELECT;

        // 单选下拉框 实例 描述信息（从report-ui里面获取）
        var entityDescription = {
            "clzType": "VUI",
            "clzKey": "ECUI_SELECT",
            "dataOpt": {
                "textNone": "未选择",
                "textAll": "全部",
                "selectAllText": "全部",
                "selectAllButton": true
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
            data.id = id + selectIdSuffix;
            data.name = dynamicData.serverData.id;
            return data;
        }

        return {
            type: 'SELECT',
            mold: 'ECUI_SELECT', // 单选ECUI_SELECT，多选ECUI_MULTI_SELECT
            iconClass: 'select',
            caption: '下拉框',
            defaultWidth: 300,
            defaultHeight: 33,
            defaultValue: 'false',
            vm: {
                render: function (data) {
                    return SelectVmTemplate.render({
                        id: data.rootId + data.serverData.id + selectIdSuffix
                    });
                }
            },
            processRenderData: processRenderData,
            entityDescription: entityDescription
        };

    }
);