/**
 * @file: 报表新建（编辑）-- 图形组件编辑模块
 * 图形topn设置、图形颜色设置（暂时还未重构完，因此只有图形所特有的设置功能）
 * @author: lizhantong
 * @depend:
 * @date: 2015-03-25
 */

define(
    [
        'report/edit/canvas/chart-setting/cascade-select-setting-model'
    ],
    function (
       CascadeSelectSettingModel
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
                'click .j-cascade-setting': 'setCascade'
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
            },
            /**
             * 级联关系设定
             *
             * @param {event} event 点击事件
             * @public
             */
            setCascade: function (event) {
                alert(0);
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