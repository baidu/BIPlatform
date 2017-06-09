/**
 * Created by v_zhaoxiaoqiang on 2014-7-17
 *
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
         * 加载报表列表
         *
         * @public
         */
        loadReportList: function () {
            var that = this;

            $.ajax({
                url: Url.loadReportList(),
                success: function (data) {
                    that.set('reportList', data.data);
                }
            });
        },

        /**
         * 删除报表
         *
         * @param {string} reportId 报表id
         * @public
         */
        deleteReport: function (reportId) {
            var that = this;

            $.ajax({
                url: Url.deleteReport(reportId),
                type: 'DELETE',
                success: function (data) {
                    that.loadReportList();
                }
            });
        },

        /**
         * 预览报表
         *
         * @param {string} reportId 报表id
         * @param {Function} success 成功回调函数
         * @public
         */
        showReport: function (reportId, success) {
            var that = this;

            $.ajax({
                url: Url.showReport(reportId),
                type: 'GET',
                success: function (data) {
                    success(data.data);
                }
            });
        },

        /**
         * 新建报表
         *
         * @param {string} reportName 报表样式名称
         * @param {Function} success 成功回调函数
         * @public
         */
        addReport: function (reportName, success) {
            $.ajax({
                url: Url.addReport(),
                type: 'POST',
                data: {
                    name: reportName
                },
                success: function (data) {
                    success(data.data.id);
                }
            });
        },

        /**
         * 复制报表（创建报表副本）
         *
         * @param {event} reportId 报表id
         * @param {event} reportName 报表样式名称
         * @param {Function} success 成功回调函数
         * @public
         */
        copyReport: function (reportId, reportName, success) {
            var that = this;

            $.ajax({
                url: Url.copyReport(reportId),
                type: 'POST',
                data: {
                    name: reportName
                },
                success: function (data) {
                    success(data);
                    that.loadReportList();
                }
            });
        },

        /**
         * 获取被点击的行的reportId
         *
         * @param {event} reportId 报表id
         * @param {Function} success 成功回调函数
         * @public
         */
        getPublishInfo: function (reportId, success) {
            $.ajax({
                url: Url.getPublishInfo(reportId),
                success: function (data) {
                    success(data);
                }
            });
        }
    });

});