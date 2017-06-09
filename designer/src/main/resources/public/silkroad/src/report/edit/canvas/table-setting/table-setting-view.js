/**
 * @file: 报表新建（编辑）-- 表格组件编辑模块 View
 *
 * @author: lizhantong
 * @depend:
 * @date: 2015-04-27
 */

define(
    [
        'report/edit/canvas/table-setting/table-setting-model',
        'report/edit/canvas/table-setting/text-align/text-align-view',
        'report/edit/canvas/table-setting/link/link-view',
        'report/edit/canvas/table-setting/other-setting/other-setting-view'
    ],
    function (
        TableSettingModel,
        TextAlignView,
        LinkView,
        OtherSettingView
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
             * @param {$HTMLElement} option.el
             * @param {string} option.reportId 报表的id
             * @param {Object} option.canvasView 画布的view
             * @constructor
             */
            initialize: function (option) {
                this.model = new TableSettingModel({
                    canvasModel: option.canvasView.model,
                    reportId: option.reportId
                });
                this.canvasView = option.canvasView;

                // 文本居中设置
                this.textAlignView = new TextAlignView({
                    el: this.el,
                    reportId: this.model.get('reportId'),
                    canvasView: this.canvasView
                });

                // 跳转设置
                this.linkView = new LinkView({
                    el: this.el,
                    reportId: this.model.get('reportId'),
                    canvasView: this.canvasView
                });

                // 其他操作设置
                this.othersView = new OtherSettingView({
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