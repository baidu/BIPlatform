/**
 * @file
 * @author 赵晓强(longze_xq@163.com)
 * @date 2014-7-28
 */
define(['url'], function (Url) {

    return Backbone.Model.extend({

        /**
         * 构造函数
         *
         * @param {Object} option 初始化配置项
         * @param {boolen} option.isEdit 是否是编辑
         * @constructor
         */
        initialize: function () {
        },

        /**
         * 皮肤更换
         *
         * @param {string} reportId 报表id
         * @param {string} type 报表皮肤id
         * @public
         */
        getSkinType: function (reportId, type) {
            var that = this;
            $.ajax({
                url:Url.getSkinType(reportId, type),
                type: 'POST',
                success: function () {
                }
            });
        },
        /**
         * 更改报表名称
         *
         * @param {string} reportId 报表id
         * @public
         */
        editReportName: function (reportId) {
            $.ajax({
                type: "GET",
                dataType: "json",
                cache: false,
                timeout: 10000,
                url: Url.editReportName(reportId),
                success: function(data){
                    if (data["status"] === 0) {
                        var reportName = data["data"].name;
                        $('.reportName').html(reportName);
                    }
                }
            });
        }
    });
});