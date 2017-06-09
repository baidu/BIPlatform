/**
 * @file
 * @author 赵晓强(longze_xq@163.com)
 * @date 2014-7-31
 */
define(['url'], function (Url) {

    return Backbone.Model.extend({
        /**
         * 构造函数
         *
         * @param {Object} option 初始化参数
         * @param {string} option.id 报表id
         * @param {Object} option.parentModel 编辑报表的model
         * @constructor
         */
        initialize: function (option) {
            this.parentModel = option.parentModel;
        },

        url: 'reports/',

        /*
         * 维度指标的相互转换
         *
         * @param {string} from 从维度（dim）或指标（ind）
         * @param {string} to 到从维度（dim）或指标（ind）
         * @param {string} id 被移动的项id
         * @param {Function} success 交互成功后的回调函数
         * @public
         */
        indDimSwitch: function (from, to, id, success) {
            var that = this;

            $.ajax({
                url: Url.indDimSwitch(
                    that.id,
                    that.parentModel.get('currentCubeId'),
                    from,
                    to,
                    id
                ),
                type: 'PUT',
                success: function () {
                    that.parentModel.loadIndList();
                    that.parentModel.loadDimList();
                }
            });
        },

        /*
         * 维度到维度组
         *
         * @param {string} groupId 维度组ID
         * @param {string} dimId 被拖拽的维度id
         * @param {Function} success 交互成功后的回调函数
         * @public
         */
        dimToGroup: function (groupId, dimId, success) {
            var that = this;

            $.ajax({
                url: Url.dimToGroup(
                    that.id,
                    that.parentModel.get('currentCubeId'),
                    groupId
                ),
                type: 'POST',
                data: {
                    'dimId': dimId
                },
                success: function () {
                    that.parentModel.loadDimList();
                }
            });
        },

        /*
         * 对维度组中的维度进行排序
         *
         * param {string} groupId 维度组id
         * param {string} dimId 维度id
         * param {string} beforeDimId
         * @public
         */
        sortSubDim: function (groupId, dimId, beforeDimId) {

            $.ajax({
                url: Url.sortSubDim(
                    this.id,
                    this.parentModel.get('currentCubeId'),
                    groupId
                ),
                type: 'PUT',
                data: {
                    dimId: dimId,
                    beforeDimId: beforeDimId
                },
                success: function () {
                    //that.parentModel.loadDimList();
                }
            });
        }
    });
});