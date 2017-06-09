/**
 * @file: 报表新建（编辑）-- 表格组件编辑模块 -- 其他设置model
 * @author: lizhantong
 * @depend:
 * @date: 2015-04-27
 */


define(['url'], function (Url) {

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
         * 获取其他设置数据
         *
         * @param {Function} success 回调函数
         *
         * @public
         */
        getOtherSettingData: function (success) {
            $.ajax({
                url: Url.getTableOtherSetting(this.get('reportId'), this.get('compId')),
                type: 'get',
                success: function (data) {
                    var sourceData = data.data;
                    var targetData = {};

                    if (sourceData) {
                        if (JSON.stringify(sourceData) === '{}') {
                            targetData.filterBlank = 'false';
                            targetData.canChangedMeasure = 'false';
                            targetData.needSummary = 'true';
                            targetData.isShowZero = 'true';
                        }
                        else {
                            targetData.filterBlank = sourceData.filterBlank
                                ? sourceData.filterBlank : 'false';

                            targetData.canChangedMeasure = sourceData.canChangedMeasure
                                ? sourceData.canChangedMeasure : 'false';

                            targetData.needSummary = sourceData.needSummary
                                ? sourceData.needSummary : 'true';

                            targetData.isShowZero = sourceData.isShowZero
                                ? sourceData.isShowZero : 'true';
                        }
                    }
                    success(targetData);
                }
            });
        },

        /**
         * 提交其他设置信息
         *
         * @param {Object} data 其他设置信息
         * @param {Function} success 回调函数
         * @public
         */
        saveOtherSettingData: function (data, success) {
            var compId = this.get('compId');
            var formData = {
                areaId: compId,
                others: JSON.stringify(data)
            };

            $.ajax({
                url: Url.getTableOtherSetting(this.get('reportId'), this.get('compId')),
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