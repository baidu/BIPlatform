/**
 * @file: 报表新建（编辑）-- 图形组件编辑模块 -- topn设置模块视图
 * @author: lizhantong
 * @depend:
 * @date: 2015-03-25
 */

define(
    [
        'dialog',
        'report/edit/canvas/chart-setting/topn/topn-model',
        'report/edit/canvas/chart-setting/topn/topn-setting-template'
    ],
    function (
        dialog,
        TopnModel,
        topnSettingTemplate
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
                'click .j-set-topn': 'getTopnList'
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

                that.model = new TopnModel({
                    canvasModel: option.canvasView.model,
                    reportId: option.reportId
                });
                this.model.set('compId', this.$el.find('.j-comp-setting').attr('data-comp-id'));
            },
            /**
             * 获取topn数据信息
             *
             * @param {event} event 点击事件
             * @public
             */
            getTopnList: function (event) {
                var that = this;
                that.model.getTopnList(function (data) {
                    that._openTopnDialog(data);
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

            //------------------------------------------
            // 私有方法区域
            //------------------------------------------

            /**
             * 打开topn设置弹出框
             *
             * @param {Object} data
             * @private
             */
            _openTopnDialog: function(data) {
                var that = this,
                    html;

                if (!data.indList) {
                    dialog.alert('没有指标');
                    return;
                }

                html = topnSettingTemplate.render(data);
                dialog.showDialog({
                    title: 'topn设置',
                    content: html,
                    dialog: {
                        width: 340,
                        height: 300,
                        resizable: false,
                        buttons: [
                            {
                                text: '提交',
                                click: function () {
                                    that._saveTopnFormInfo($(this));
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
            },
            /**
             * 保存topn设置信息
             *
             * @param {￥HTMLElement} $dialog 弹出框$el元素
             * @private
             */
            _saveTopnFormInfo: function ($dialog) {
                var selects = $('.topn-indlist').find('select');
                var $input = $('.topn-indlist').find('input');
                var data = {};

                selects.each(function () {
                    var $this = $(this);
                    var name = $this.attr('name');
                    data[name] = $this.val();
                });
                data[$input.attr('name')] = $input.val();
                this.model.saveTopnInfo(data, function () {
                    $dialog.dialog('close');
                    window.dataInsight.main.canvas.showReport();
                });
            }
    });

    return View;
});