/**
 * @file: 报表新建（编辑）-- 图形组件编辑模块 -- 个性化设置
 * @author: lizhantong
 * @depend:
 * @date: 2015-07-07
 */

define(
    [
        'dialog',
        'report/edit/canvas/chart-setting/individuation/individuation-model',
        'report/edit/canvas/chart-setting/individuation/individuation-template'
    ],
    function (
        dialog,
        IndividuationModel,
        IndividuationTemplate
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
                'click .j-set-individuation': 'getIndividuationData'
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
             * @constructor
             */
            initialize: function (option) {
                var that = this;

                that.model = new IndividuationModel({
                    canvasModel: option.canvasView.model,
                    reportId: option.reportId
                });

                this.model.set('compId', that.$el.find('.j-comp-setting').attr('data-comp-id'));
            },

            /**
             * 获取个性化设置信息
             *
             * @param {event} event 点击事件
             *
             * @public
             */
            getIndividuationData: function (event) {
                var that = this;
                that.model.getIndividuationData(function (data) {
                    that._openIndividuationDialog(data);
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
             * 绑定事件
             *
             * @private
             */
            _bindEvent: function () {
                var $isShowTitle = $('.j-isShowTitle');
                $isShowTitle.unbind();
                $isShowTitle.change(function () {
                    var isChked = $(this).is(':checked');
                    if (isChked) {
                        $('.j-appearance-set-title').show();
                    }
                    else {
                        $('.j-appearance-set-title').hide();
                    }
                });
            },

            /**
             * 打开设置弹出框
             *
             * @param {Object} data 个性化设置信息
             *
             * @private
             */
            _openIndividuationDialog: function (data) {
                var that = this;
                var html = IndividuationTemplate.render(data);
                dialog.showDialog({
                    title: '个性化设置',
                    content: html,
                    dialog: {
                        width: 340,
                        height: 300,
                        resizable: false,
                        buttons: [
                            {
                                text: '提交',
                                click: function () {
                                    that._saveIndividuationInfo($(this));
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
                that._bindEvent();
            },

            /**
             * 保存设置信息
             *
             * @param {$HTMLElement} $dialog 弹出框$el元素
             * @private
             */
            _saveIndividuationInfo: function ($dialog) {
                var appearItems = $('.j-appearance-setting .j-appearance-item');
                var data = {};
                var appearance = {};

                appearItems.each(function () {
                    var $chk = $(this).find('input[type="checkbox"]');
                    var name = $chk.attr('name');
                    if (name === 'isShowTitle' && $chk.is(':checked')) {
                        appearance.chartTitle = $('.j-appearance-set-title input').val();
                    }
                    appearance[name] = $chk.is(':checked');

                });
                data.appearance = JSON.stringify(appearance);

                this.model.saveIndividuationInfo(data, function () {
                    $dialog.dialog('close');
                    window.dataInsight.main.canvas.showReport();
                });
            }
        });
    }
);