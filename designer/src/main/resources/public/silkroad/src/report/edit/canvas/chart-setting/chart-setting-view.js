/**
 * @file: 报表新建（编辑）-- 图形组件编辑模块
 * 图形topn设置、图形颜色设置（暂时还未重构完，因此只有图形所特有的设置功能）
 * @author: lizhantong
 * @depend:
 * @date: 2015-03-25
 */

define(
    [
        'report/edit/canvas/chart-setting/chart-setting-model',
        'report/edit/canvas/chart-setting/topn/topn-view',
        'report/edit/canvas/chart-setting/axis/axis-view',
        'report/edit/canvas/chart-setting/ind-color/ind-color-view',
        'report/edit/canvas/chart-setting/axis-text/axis-text-view',
        'report/edit/canvas/chart-setting/individuation/individuation-view'
    ],
    function (
        ChartSettingModel,
        TopnView,
        AxisView,
        IndColorView,
        AxisTextView,
        IndividuationView
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
             * 报表组件的编辑模块 初始化函数
             *
             * @param {Object} option 设置项
             * @param {$HTMLElement} option.el
             * @param {string} option.reportId 报表的id
             * @param {Object} option.canvasView 画布的view
             * @constructor
             */
            initialize: function (option) {
                this.model = new ChartSettingModel({
                    canvasModel: option.canvasView.model,
                    reportId: option.reportId
                });
                this.canvasView = option.canvasView;
                // 挂载topn设置视图
                this.topnView = new TopnView({
                    el: this.el,
                    reportId: this.model.get('reportId'),
                    canvasView: this.canvasView
                });
                this.indColorView = new IndColorView({
                    el: this.el,
                    reportId: this.model.get('reportId'),
                    canvasView: this.canvasView
                });
                // 挂载双坐标轴设置视图
                this.axisView = new AxisView({
                	// 挂载指标颜色设置视图
                	el: this.el,
                    reportId: this.model.get('reportId'),
                    canvasView: this.canvasView
                });
                // 挂载坐标轴名字设置视图
                this.axisTextView = new AxisTextView({
                    // 挂载指标颜色设置视图
                    el: this.el,
                    reportId: this.model.get('reportId'),
                    canvasView: this.canvasView
                });
                // 挂载个性化设置视图
                this.individuationView = new IndividuationView({
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