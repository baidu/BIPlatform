/**
 * @file: 报表新建（编辑）-- 表格组件编辑模块 -- 其他设置 View
 * @author: lizhantong
 * @depend:
 * @date: 2015-07-08
 */

define(
    [
        'dialog',
        'report/edit/canvas/table-setting/other-setting/other-setting-model',
        'report/edit/canvas/table-setting/other-setting/other-setting-template'
    ],
    function (
        dialog,
        OtherSettingModel,
        OtherSettingTemplate
    ) {

        //------------------------------------------
        // 视图类的声明
        //------------------------------------------

        /* globals Backbone */
        return Backbone.View.extend({

            /**
             * 事件绑定
             */
            events: {
                'click .j-others-operate': 'getOtherSettingData'
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
                this.model = new OtherSettingModel({
                    canvasModel: option.canvasView.model,
                    reportId: option.reportId
                });
                this.model.set('compId', this.$el.find('.j-comp-setting').attr('data-comp-id'));
            },

            /**
             * 获取其他设置信息
             *
             * @param {event} event 点击事件
             *
             * @public
             */
            getOtherSettingData: function (event) {
                var that = this;
                that.model.getOtherSettingData(function (data) {
                    that._openOtherSettingDialog(data);
                });
            },

            /**
             * 销毁
             *
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
             * 打开其他设置弹出框
             *
             * @param {Object} data 其他设置信息
             * @private
             */
            _openOtherSettingDialog: function (data) {
                var that = this;
                var html;

                html = OtherSettingTemplate.render(
                    data
                );
                dialog.showDialog({
                    title: '其他操作',
                    content: html,
                    dialog: {
                        width: 350,
                        height: 270,
                        resizable: false,
                        buttons: [
                            {
                                text: '提交',
                                click: function () {
                                    that._saveOtherSettingInfo($(this));
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
             * 保存其他设置信息
             *
             * @param {$HTMLElement} $dialog 弹出框$el元素
             * @private
             */
            _saveOtherSettingInfo: function ($dialog) {
                var that = this;
                var formData = {};
                var $chks = $('.data-format-black').find('input[type="checkbox"]');

                // 获取设置信息
                $chks.each(function () {
                    var name = $(this).attr('name');
                    var isChecked = $(this).is(':checked');
                    formData[name] = isChecked ? 'true' : 'false';
                });

                that.model.saveOtherSettingData(formData, function () {
                    $dialog.dialog('close');
                    that._setRichSelect(formData);
                });

            },

            /**
             * 处理富文本下拉框
             *
             * @param {Object} formData 其他设置信息
             *
             * @private
             */
            _setRichSelect: function (formData) {
                var that = this;
                var canvas = window.dataInsight.main.canvas;
                var canvasModel = canvas.model;
                var $reportVm = canvasModel.$reportVm;
                var entityDefs = canvasModel.reportJson.entityDefs;

                var compId = that.model.get('compId');
                var $table = $($('.active').children()[0]);
                var tableBox = $reportVm
                    .find('.j-component-item')
                    .filter('[data-comp-id=' + compId + ']');

                var richSelect = $($table.children()[0]).children()[0];
                var richSelectId;

                // 1.移除vm  2.移除json 3.重设表格高度
                if (!richSelect) {
                    canvas.model.saveJsonVm(function () {
                        canvas.showReport();
                    });
                    return;
                }

                richSelectId = $(richSelect).attr('data-o_o-di');
                var hasRichSel = richSelectId.indexOf('rich-select');

                // 如果不显示富文本下拉框
                if (formData.canChangedMeasure === 'false') {
                    // 获取到table中rich-select容器，并移除掉
                    if (hasRichSel > 0) {
                        $table.children()[0].remove();

                        // 移除掉$reportVm中的 rich-select容器，重设表格高度
                        tableBox.height(tableBox.height() - 37);
                        $table = $($(tableBox).children()[0]);
                        $table.children()[0].remove();

                        // 移除掉表格组件中对富文本下拉框的关联
                        delete $.getTargetElement(compId, entityDefs).vuiRef.richSelect;

                        // 找到富文本下拉框json，并从json集合中移除掉
                        for (var i = 0, index = 0, iLen = entityDefs.length;
                             i < iLen; i ++
                        ) {
                            if (entityDefs[i].id === richSelectId) {
                                index = i + 1;
                                break;
                            }
                        }
                        index && (
                            canvasModel.reportJson.entityDefs = entityDefs
                                .slice(0, index - 1)
                                .concat(entityDefs.slice(index))
                        );
                    }
                }
                else {
                    if (hasRichSel < 0) {
                        richSelectId = 'snpt.' + compId + '-vu-table-rich-select';
                        var html = [
                            '<div class="di-o_o-line">',
                            '<div class="" data-o_o-di="', richSelectId, '"></div>',
                            '</div>'
                        ].join('');
                        var json = {
                            clzType: 'VUI',
                            clzKey: 'RICH_SELECT',
                            id: richSelectId,
                            compId: compId
                        };

                        // 表格组件中添加vm
                        $table.prepend(html);
                        tableBox.height(tableBox.height() + 37);
                        $table = $($(tableBox).children()[0]);
                        $table.prepend(html);

                        // 表格组件中添加json
                        canvasModel.reportJson.entityDefs.push(json);
                        var entity = $.getTargetElement(compId, entityDefs);
                        entity.vuiRef.richSelect = richSelectId;
                    }
                }

                canvas.model.saveJsonVm(function () {
                    canvas.showReport();
                });
            }
        });
    }
);