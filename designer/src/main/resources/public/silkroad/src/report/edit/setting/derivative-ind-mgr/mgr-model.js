/**
 * @file: 报表新建（编辑）-- 衍生指标管理Model
 * @author: lizhantong(lztlovely@126.com)
 * @depend:
 * @date: 2014-09-23
 */
define(['url'], function (Url) {

    return Backbone.Model.extend({
        initialize: function () {},
        /**
         * 报表新建（编辑）-edit-setting
         * 衍生指标管理-提交
         *
         * @param {Object} data 请求后端传的参数
         * @param {event} callback 回调事件
         * @public
         */
        submitMethodTypeValue: function (data, callback) {
            var that = this;
            $.ajax({
                url: Url.submitDeriveIndsInfo(
                    window.dataInsight.main.id,
                    window.dataInsight.main.model.get('currentCubeId')
                ),
                type: 'POST',
                data: { deriveInds: JSON.stringify(data) },
                success: function () {
                    // 更新左边
                    that.updateLeftPanel(callback);
                }
            });
        },
        /**
         * 更新左侧面板
         *
         * @public
         */
        updateLeftPanel: function (callback) {
            callback();
            window.dataInsight.main.model.loadIndList();
            window.dataInsight.main.model.loadDimList();
        },
        /**
         * 报表新建（编辑）-edit-setting
         * 衍生指标管理-衍生指标删除
         *
         * @param {string} measureId 请求后端传的参数
         * @param {event} callback 回调事件
         * @public
         */
        deleteInd: function (measureId, callback) {
            $.ajax({
                url: Url.deleteInd(
                    window.dataInsight.main.id,
                    window.dataInsight.main.model.get('currentCubeId'),
                    measureId
                ),
                type: 'DELETE',
                success: function () {
                    callback();
                }
            });
        }
    });


});