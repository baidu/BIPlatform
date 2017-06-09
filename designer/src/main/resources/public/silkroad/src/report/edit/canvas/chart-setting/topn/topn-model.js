/**
 * @file: 报表新建（编辑）-- 图形组件编辑模块 -- topn设置模块model
 * @author: lizhantong
 * @depend:
 * @date: 2015-03-25
 */


define(['url'], function (Url) {

    //------------------------------------------
    // 模型类的声明
    //------------------------------------------

    var Model = Backbone.Model.extend({
        defaults: {},
        initialize: function () { },
        /**
         * 提交topn数据
         *
         * @param {Function} success 回调函数
         * @public
         */
        saveTopnInfo: function (data, success) {
            var formData = {
                top: JSON.stringify(data)
            };
            $.ajax({
                url: Url.getTopnList(this.get('reportId'), this.get('compId')),
                type: 'POST',
                data: formData,
                success: function () {
                    success();
                }
            });
        },
        /**
         * 获取topn设置信息
         *
         * @param {Function} success 回调函数
         * @public
         */
        getTopnList: function (success) {
            var that = this;
            that.getCompAxis(getTopnAjax);
            var xyList;
            function getTopnAjax(xyData) {
                xyList = xyData;
                $.ajax({
                    url: Url.getTopnList(that.get('reportId'), that.get('compId')),
                    type: 'get',
                    success: function (data) {
                        var sourceData = data.data ? data.data: {};
                        // 缺少选中指标的数据
                        sourceData.indList = xyList.yAxis;
                        sourceData.topTypeList = {
                            NONE: 'none',
                            DESC: 'top',
                            ASC: 'bottom'
                        };
                        success(sourceData);
                    }
                });
            }
        },
        /**
         * 获取组件的数据关联配置（指标、维度、切片）
         *
         * @param {string} compId 组件id
         * @param {Function} success 数据load完成后的回调函数
         * @public
         */
        getCompAxis: function (success) {
            var that = this;
            $.ajax({
                url: Url.getCompAxis(that.get('reportId'), that.get('compId')),
                success: function (data) {
                    success(data.data);
                }
            });
        }
    });

    return Model;
});
