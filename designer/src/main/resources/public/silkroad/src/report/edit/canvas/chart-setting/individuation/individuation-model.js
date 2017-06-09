/**
 * @file: 报表新建（编辑）-- 图形组件编辑模块 -- 个性化设置Model
 * @author: lizhantong
 * @depend:
 * @date: 2015-07-07
 */


define(['url', 'core/helper'], function (Url, Helper) {

    //------------------------------------------
    // 模型类的声明
    //------------------------------------------

    /* globals Backbone */
    var Model = Backbone.Model.extend({

        /**
         * 构造函数
         *
         * @constructor
         */
        initialize: function () {},

        /**
         * 获取坐标轴名字设置数据
         *
         * @param {Function} success 回调函数
         * @public
         */
        getIndividuationData: function (success) {
            $.ajax({
                url: Url.getIndividuationData(this.get('reportId'), this.get('compId')),
                type: 'get',
                success: function (data) {
                    var targetData = data.data;

                    (!targetData) && (targetData = {});
                    (!targetData.appearance) && (targetData.appearance = {});

                    (targetData.appearance.isShowInds === undefined
                    || targetData.appearance.isShowInds === null
                    ) && (targetData.appearance.isShowInds = true);

                    success(targetData);
                }
            });
        },

        /**
         * 提交个性化设置信息
         *
         * @param {Object} data 坐标轴设置的text信息
         * @param {Function} success 回调函数
         * @public
         */
        saveIndividuationInfo: function (data, success) {
            var compId = this.get('compId');

            $.ajax({
                url: Url.getIndividuationData(this.get('reportId'), compId),
                type: 'POST',
                data: data,
                success: function () {
                    success();
                }
            });
        }
    });

    return Model;
});
