/**
 * @file: 报表新建（编辑）-- 表格组件编辑模块 -- 指标文本对齐设置模块视图
 * @author: lizhantong
 * @depend:
 * @date: 2015-04-27
 */

define(
    [
        'dialog',
        'report/edit/canvas/table-setting/text-align/text-align-model',
        'report/edit/canvas/table-setting/text-align/text-align-setting-template'
    ],
    function (
        dialog,
        TextAlignModel,
        textAlignSettingTemplate
    ) {

        //------------------------------------------
        // 视图类的声明
        //------------------------------------------
        var View = Backbone.View.extend({
            events: {
                'click .j-set-text-align': 'getTextAlignList'
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

                that.model = new TextAlignModel({
                    canvasModel: option.canvasView.model,
                    reportId: option.reportId
                });
                this.model.set('compId', this.$el.find('.j-comp-setting').attr('data-comp-id'));
            },
            /**
             * 获取文本对齐数据信息
             *
             * @param {event} event 点击事件
             * @public
             */
            getTextAlignList: function (event) {
                var that = this;
                that.model.getCompAxis(function (data) {
                    that._openTextAlignDialog(data);
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
             * 打开文本对齐设置弹出框
             *
             * @param {Object} data
             * @private
             */
            _openTextAlignDialog: function(data) {
                var that = this,
                    html;

                if ($.isEmptyObject(data.indList)) {
                    dialog.alert('没有指标');
                    return;
                }

                html = textAlignSettingTemplate.render(data);
                dialog.showDialog({
                    title: '文本对齐设置',
                    content: html,
                    dialog: {
                        width: 340,
                        height: 300,
                        resizable: false,
                        buttons: [
                            {
                                text: '提交',
                                click: function () {
                                    that._saveTextAlignInfo($(this));
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
             * 保存文本对齐设置信息
             *
             * @param {$HTMLElement} $dialog 弹出框$el元素
             * @private
             */
            _saveTextAlignInfo: function ($dialog) {
                var selects = $('.text-align-set').find('select');
                var data = {};

                selects.each(function () {
                    var $this = $(this);
                    var name = $this.attr('name');
                    data[name] = $this.val();
                });
                this.model.saveTextAlignInfo(data, function () {
                    $dialog.dialog('close');
                    window.dataInsight.main.canvas.showReport();
                });
            }
    });

    return View;
});