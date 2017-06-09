/**
 * @file: 报表新建（编辑）-- 图形组件编辑模块 -- 坐标轴名字设置View
 * @author: lizhantong
 * @depend:
 * @date: 2015-07-06
 */

define(
    [
        'dialog',
        'report/edit/canvas/chart-setting/axis-text/axis-text-model',
        'report/edit/canvas/chart-setting/axis-text/axis-text-setting-template'
    ],
    function (
        dialog,
        AxisTextModel,
        AxisTextSettingTemplate
    ) {

        //------------------------------------------
        // 视图类的声明
        //------------------------------------------

        /* globals Backbone */
        return Backbone.View.extend({

            /**
             * 事件绑定
             *
             */
            events: {
                'click .j-set-axis-text': 'getAxisTextList'
            },

            //------------------------------------------
            // 公共方法区域
            //------------------------------------------

            /**
             * 报表组件的编辑模块 初始化函数
             *
             * @param {Object} option 设置项
             * @param {$HTMLElement} option.el 模块容器
             * @param {string} option.reportId 报表的id
             * @param {Object} option.canvasView 画布的view
             *
             * @constructor
             */
            initialize: function (option) {
                var that = this;

                that.model = new AxisTextModel({
                    canvasModel: option.canvasView.model,
                    reportId: option.reportId
                });

                this.model.set('compId', that.$el.find('.j-comp-setting').attr('data-comp-id'));
            },

            /**
             * 获取坐标轴数据信息
             *
             * @param {event} event 点击事件
             * @public
             */
            getAxisTextList: function (event) {
                var that = this;
                that.model.getCompAxis(function (data) {
                    that._openAxisTextDialog(data);
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
             * 打开设置弹出框
             *
             * @param {Object} data 坐标轴设置信息
             *
             * @private
             */
            _openAxisTextDialog: function (data) {
                var that = this;
                var html;

                if ($.isEmptyObject(data.indList)) {
                    dialog.alert('没有指标');
                    return;
                }

                html = AxisTextSettingTemplate.render(data);
                dialog.showDialog({
                    title: '坐标轴名字设置',
                    content: html,
                    dialog: {
                        width: 400,
                        height: 300,
                        resizable: false,
                        buttons: [
                            {
                                text: '提交',
                                click: function () {
                                    that._saveAxisTextInfo($(this));
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
             * 保存坐标轴设置信息
             *
             * @param {$HTMLElement} $dialog 弹出框$el元素
             * @private
             */
            _saveAxisTextInfo: function ($dialog) {
                var texts = $('.j-axis-text-setting .j-axis-text-item input');
                var errorMsgs = $('.j-axis-text-setting .j-axis-text-item .j-error-msg');
                var data = {};

                var canSubmit = true;
                var strLength = 26;
                errorMsgs.hide();
                texts.each(function () {
                    var $this = $(this);
                    var val = $this.val();
                    if (getStrLength(val) > strLength) {
                        canSubmit = false;
                        $this.next('.j-error-msg').show();
                    }
                    var name = $this.attr('name');
                    data[name] = val;
                });
                if (!canSubmit) {
                    return;
                }
                this.model.saveAxisTextInfo(data, function () {
                    $dialog.dialog('close');
                    window.dataInsight.main.canvas.showReport();
                });


                function getStrLength(str) {
                    var len = 0;

                    if (str.match(/[^ -~]/g) === null) {
                        len = str.length;
                    }
                    else {
                        len = str.length + str.match(/[^ -~]/g).length;
                    }
                    return len;
                }
            }
        });
    }
);