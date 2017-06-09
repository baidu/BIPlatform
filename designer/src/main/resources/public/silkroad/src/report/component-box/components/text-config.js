/**
 * @file:   文本框的配置信息
 * @author: weiboxue(wbx_901118@sina.com)
 * @date:   2014/12/1
 */
define(
    [
        'constant',
        'report/component-box/components/text-vm-template'
    ],
    function (
        Constant,
        TextVmTemplate
    ) {
        // 文本框下拉框id后缀
        var textIdSuffix = Constant.COMPONENT_ID_SUFFIX.TEXT;

        // 文本框 实例 描述信息（从report-ui里面获取）
        var entityDescription = {};
        // 格式化文本框组件添加唯一ID
        var textAll = '';
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
            data.id = id + textIdSuffix;

            return data;
        }

        return {
            type: 'TEXT',
            iconClass: 'text',
            caption: '文本框',
            defaultWidth: 300,
            defaultHeight: 33,
            vm: {
                render: function (data) {
                    // 为文本框组件设定唯一ID
                    var time = String(new Date().getTime());
                    var textOne = TextVmTemplate.render().split('id="')[0];
                    var textTwo = TextVmTemplate.render().split('id="')[1];
                    textAll = textOne + 'id="' + 'TEXT' +time + textTwo;
                    return textAll;
                }
            },
            processRenderData: processRenderData,
            entityDescription: entityDescription
        };

    }
);