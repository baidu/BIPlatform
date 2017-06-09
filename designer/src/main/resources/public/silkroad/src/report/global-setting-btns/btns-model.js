/**
 * @file 参数维度-model
 * @author weiboxue(wbx_901118@sina.com)
 * @date 2014-12-1
 */
define(['url'], function (Url) {
    return Backbone.Model.extend({

        /**
         * 构造函数
         * @public
         */
        initialize: function () {},

        /**
         * 参数维度
         * 获取
         *
         * @param {string} reportId 请求后端传的参数
         * @param {object} success 回调事件
         * @public
         */
        getParameterDim: function (reportId, success) {
            $.ajax({
                url: Url.getParameterDim(reportId),
                type: 'GET',
                success: function (data) {
                    success(data);
                }
            });
        },

        /**
         * 参数维度
         * 提交
         *
         * @param {string} reportId 请求后端传的参数
         * @param {object} data 数据
         * @public
         */
        getParameterDimData: function (reportId, data) {
            $.ajax({
                url: Url.getParameterDimData(reportId),
                type: 'post',
                data: 'params=' + JSON.stringify(data.params),
                success: function (data) {
                }
            });
        }
    });
});