/**
 * @file: 维度组管理
 * @author 赵晓强(longze_xq@163.com)
 * @date 2014-10-22
 */
define(['url'], function (Url) {

    return Backbone.Model.extend({
        initialize: function () {},
        /**
         * 报表新建（编辑）-edit-setting
         * 衍生指标管理-提交
         *
         * @param {Object} data 请求后端传的参数
         * @param {event} callback 回调事件
         * @public
         */
        submitMethodTypeValue: function (data, callback) {
            var that = this;

            $.ajax({
                url: Url.submitDeriveIndsInfo(
                    window.dataInsight.main.id,
                    window.dataInsight.main.model.get('currentCubeId')
                ),
                type: 'POST',
                data: { deriveInds: JSON.stringify(data) },
                success: function () {
                    // 更新左边
                    that.updateLeftPanel(callback);
                }
            });
        }
    });


});