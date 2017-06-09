/**
 * @file: 报表新建（编辑）-- 图形组件编辑模块
 * @author: lizhantong
 * date: 2015-03-25
 */

define(['url'], function (Url) {

    //------------------------------------------
    // 模型类的声明
    //------------------------------------------

    var Model = Backbone.Model.extend({

        /**
         * 构造函数
         *
         * @constructor
         */
        initialize: function () {},

        /**
         * 获取指标设置信息
         *
         * @param {string} itemId 指标项
         * @param {Function} success 回调函数
         * @public
         */
        getFieldFilterInfo: function (itemId, success) {
            var that = this;
            var data = {
                name: 'test',
                defaultValue: '默认值',
                sqlCondition: '='

            };
            $.ajax({
                url: Url.getFieldFilterInfo(
                    that.get('reportId'),
                    that.get('compId'),
                    itemId
                ),
                type: 'get',
                success: function (data) {
                    success(data.data);
                }
            });
        },

        /**
         * 保存指标设置信息
         *
         * @param {string} fieldId 指标项
         * @param {Object} data 设置保存信息
         * @param {Function} success 回调函数
         * @public
         */
        saveFieldFilterInfo: function (fieldId, data, success) {
            var that = this;
            $.ajax({
                url: Url.getFieldFilterInfo(that.get('reportId'), that.get('compId'), fieldId),
                data: data,
                type: 'post',
                success: function () {
                    success();
                }
            });
        }
    });

    return Model;
});
