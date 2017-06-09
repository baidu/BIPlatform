/**
 * @file: 报表新建（编辑）-- 表格组件编辑模块 View
 *
 * @author: lizhantong
 * @depend:
 * @date: 2015-04-27
 */

define(
    [
        'report/edit/canvas/table-setting/link/link-view',
        'report/edit/canvas/olap-table-setting/olap-table-setting-model'
    ],
    function (
        LinkView,
        OlapTableSettingModel
    ) {

        //------------------------------------------
        // 视图类的声明
        //------------------------------------------

        /* globals Backbone */
        return Backbone.View.extend({

            //------------------------------------------
            // 公共方法区域
            //------------------------------------------

            /**
             * 初始化函数
             *
             * @param {Object} option 设置项
             */
            initialize: function (option) {
                this.model = new OlapTableSettingModel({
                    canvasModel: option.canvasView.model,
                    reportId: option.reportId
                });
                this.canvasView = option.canvasView;
                // 跳转设置
                this.linkView = new LinkView({
                    el: this.el,
                    reportId: this.model.get('reportId'),
                    canvasView: this.canvasView
                });
            },

            /**
             * 销毁
             * @public
             */
            destroy: function () {
                this.stopListening();
                // 删除model
                this.model.clear({silent: true});
                delete this.model;
                // 在这里没有把el至为empty，因为在点击图行编辑时，会把图形编辑区域重置，无需在这里
                this.$el.unbind();
            }
        });
    }
);