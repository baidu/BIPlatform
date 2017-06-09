/**
 * @file: 报表新建（编辑）-- 图形组件编辑模块
 * 图形topn设置、图形颜色设置（暂时还未重构完，因此只有图形所特有的设置功能）
 * @author: lizhantong
 * @depend:
 * @date: 2015-03-25
 */

define(
    [
        'report/edit/canvas/chart-setting/cascade-select-setting-model',
        'report/edit/canvas/chart-setting/cascade-setting/cascade-setting-view'
    ],
    function (
       CascadeSelectSettingModel,
       CascadeSettingView
    ) {
        //------------------------------------------
        // 视图类的声明
        //------------------------------------------

        /**
         * 维度设置视图类
         *
         * @class
         */
        var View = Backbone.View.extend({
            events: {
                'click .j-set-axis': 'setDoubleAxis'
            },
            //------------------------------------------
            // 公共方法区域
            //------------------------------------------

            /**
             * 报表组件的编辑模块 初始化函数
             *
             * @param {$HTMLElement} option.el
             * @param {string} option.reportId 报表的id
             * @param {Object} option.canvasView 画布的view
             * @constructor
             */
            initialize: function (option) {
                this.model = new CascadeSelectSettingModel({
                    canvasModel: option.canvasView.model,
                    reportId: option.reportId
                });
                this.canvasView = option.canvasView;
                // 挂载cascade级联关系设置视图
                this.cascadeView = new CascadeSettingView({
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

        return View;
    });