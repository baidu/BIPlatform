/**
 * @file 报表的公共功能 - model
 * @author 赵晓强(longze_xq@163.com)
 * @date 2014-10-10
 */
define(['url'], function (Url) {

    return Backbone.Model.extend({

        /**
         * 构造函数
         *
         * @constructor
         */
        initialize: function () {},

        /**
         * 发布报表
         *
         * @param {string} type ajax请求类型
         * @param {string} reportId 报表参数
         * @param {Function} success 回调函数
         * @public
         */
        publishReport: function (type, reportId, success) {
            $.ajax({
                url: Url.publishReport(reportId),
                type: type,
                success: function (data) {
                    success(data.data);
                }
            });
        },

        /**
         * 预览报表
         *
         * @param {string} type ajax请求类型
         * @param {string} reportId 报表参数
         * @param {Function} success 回调函数
         * @public
         */
        previewReport: function (type, reportId, success) {
            $.ajax({
                url: Url.previewReport(reportId),
                type: type,
                success: function (data) {
                    success(data.data);
                }
            });
        }
    });

});