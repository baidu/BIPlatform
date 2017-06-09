/**
 * @file:   form（component）中对应select（vui）的配置信息
 * @author: lzt(lztlovely@126.com)
 * @date:   2014/11/26
 */
define(
    [
        'constant',
        'report/component-box/components/ecui-input-tree-vm-template'
    ],
    function (
        Constant,
        EcuiInputTreeTemplate
    ) {
        // 单选下拉框id后缀
        var treeIdSuffix = Constant.COMPONENT_ID_SUFFIX.EUUI_INPUT_TREE;

        // 单选下拉框 实例 描述信息（从report-ui里面获取）
        var entityDescription = {
            "clzType": "VUI",
            "clzKey": "ECUI_INPUT_TREE",
            "cfgOpt": { "async": true }
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
            data.id = id + treeIdSuffix;
            data.name = dynamicData.serverData.id;
            return data;
        }

        return {
            type: 'SINGLE_DROP_DOWN_TREE',
            iconClass: 'single-drop-down-tree',
            caption: '下拉树',
            defaultWidth: 210,
            defaultHeight: 33,
            defaultValue: 'false',
            vm: {
                render: function (data) {
                    return EcuiInputTreeTemplate.render({
                        id: data.rootId + data.serverData.id + treeIdSuffix
                    });
                }
            },
            processRenderData: processRenderData,
            entityDescription: entityDescription
        };

    }
);