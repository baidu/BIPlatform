/**
 * @file: 报表新建（编辑）-- 图形组件编辑模块 -- 双坐标轴设置模块view
 * @author: weiboxue
 * @depend:
 * @date: 2015-03-27
 */

define(
    [
        'dialog',
        'report/edit/canvas/chart-setting/axis/axis-model',
        'report/edit/canvas/chart-setting/axis/axis-setting-template'
    ],
    function (
        dialog,
        AxisModel,
        axisSettingTemplate
    ) {

        //------------------------------------------
        // 引用
        //------------------------------------------

        var confirm = dialog.confirm;

        //------------------------------------------
        // 视图类的声明
        //------------------------------------------
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
                var that = this;
                that.model = new AxisModel({
                    canvasModel: option.canvasView.model,
                    reportId: option.reportId
                });
                this.model.set('compId', this.$el.find('.j-comp-setting').attr('data-comp-id'));
            },
            /**
             * 获取双坐标轴数据信息
             *
             * @param {event} event 点击事件
             * @public
             */
            setDoubleAxis: function (event) {
                var that = this;
                that.model.getCompAxis(function (data) {
                    that._openAxisDialog(data);
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
            },
            /**
             * 打开双坐标轴设置弹出框
             *
             * @param {Object} data
             * @private
             */
            _openAxisDialog: function(data) {
                var that = this;
                var html = axisSettingTemplate.render(data);
                var dimNum = data.dim.length;
                if (dimNum === 1) {
                    dialog.alert('少于两个指标,无法进行设置', '提示');
                }
                else if (dimNum === 0) {
                    dialog.alert('没有指标', '提示');
                }
                else {
                    dialog.showDialog({
                        title: '双坐标轴设置',
                        content: html,
                        dialog: {
                            width: 340,
                            height: 200,
                            resizable: false,
                            buttons: [
                                {
                                    text: '提交',
                                    click: function () {
                                        that._saveAxisFormInfo($(this));
                                    }
                                },
                                {
                                    text: '取消',
                                    click: function () {
                                        $(this).dialog('close');
                                    }
                                }
                            ]
                        }
                    });
                }
            },
            /**
             * 保存双坐标轴设置信息
             *
             * @param {￥HTMLElement} $dialog 弹出框$el元素
             * @private
             */
            _saveAxisFormInfo: function ($dialog) {
                var checkboxs = $('.axis-setting-checkbox');
                var data = {};
                // 数据结构构造
                checkboxs.each(function () {
                    var $this = $(this);
                    var name = $this.attr('name');
                    if ($this.is(":checked")) {
                        data[name] = '1';
                    }
                    else {
                        data[name] = '0';
                    }
                });

                this.model.saveAxisInfo(data, function () {
                    $dialog.dialog('close');
                    window.dataInsight.main.canvas.showReport();
                });
            }
    });

    return View;
});