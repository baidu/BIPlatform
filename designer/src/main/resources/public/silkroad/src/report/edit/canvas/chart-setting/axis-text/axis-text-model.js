/**
 * @file: 报表新建（编辑）-- 图形组件编辑模块 -- 坐标轴名字设置Model
 * @author: lizhantong
 * @depend:
 * @date: 2015-07-06
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
         * 获取组件的数据关联配置（指标、维度、切片）
         *
         * @param {Function} success 数据load完成后的回调函数
         * @public
         */
        getCompAxis: function (success) {
            var that = this;

            $.ajax({
                url: Url.getCompAxis(that.get('reportId'), that.get('compId')),
                success: function (data) {
                    that.getAxisTextList(data.data, success);
                }
            });
        },

        /**
         * 获取坐标轴名字设置数据
         *
         * @param {Object} indDimList 维度指标列表
         * @param {Function} success 回调函数
         * @public
         */
        getAxisTextList: function (indDimList, success) {
            $.ajax({
                url: Url.getAxisTextList(this.get('reportId'), this.get('compId')),
                type: 'get',
                success: function (data) {
                    var sourceData = data.data;
                    var targetData = {indList: {}};
                    var inds = indDimList.candInds;

                    if (inds) {
                        for (var i = 0, len = inds.length; i < len; i ++) {
                            var name = inds[i].name;

                            targetData.indList[name] = {};
                            targetData.indList[name].caption = inds[i].caption;
                            targetData.indList[name].axisName = sourceData.hasOwnProperty(name)
                                ? sourceData[name]
                                : null;
                        }
                    }
                    success(targetData);
                }
            });

        },

        /**
         * 提交坐标轴名字设置信息
         *
         * @param {Object} data 坐标轴设置的text信息
         * @param {Function} success 回调函数
         * @public
         */
        saveAxisTextInfo: function (data, success) {
            var compId = this.get('compId');

            var formData = {
                axisCaption: JSON.stringify(data)
            };

            $.ajax({
                url: Url.getAxisTextList(this.get('reportId'), compId),
                type: 'POST',
                data: formData,
                success: function () {
                    success();
                }
            });
        }
    });

    return Model;
});
