/**
 * @file: 报表新建（编辑）-- 平面表格组件编辑模块 -- 分页设置model
 * @author: lizhantong
 * @depend:
 * @date: 2015-07-10
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
         * 获取分页设置数据
         *
         * @param {Function} success 回调函数
         *
         * @public
         */
        getPaginationData: function (success) {
            $.ajax({
                url: Url.getPaginationData(this.get('reportId'), this.get('compId')),
                type: 'get',
                success: function (data) {
                    var sourceData = data.data;
                    success(sourceData.pagination);
                }
            });
            // success({isPagination: true, pageSize: 20, pageSizeOptions: [10, 20, 30]});
        },

        /**
         * 提交其他设置信息
         *
         * @param {Object} data 其他设置信息
         * @param {Function} success 回调函数
         * @public
         */
        savePaginationData: function (data, success) {
            $.ajax({
                url: Url.getPaginationData(this.get('reportId'), this.get('compId')),
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
