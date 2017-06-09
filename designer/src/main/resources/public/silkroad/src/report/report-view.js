/**
 * @file 报表的公共功能 - view
 * @author 赵晓强(longze_xq@163.com)
 * @date 2014-10-10
 */
define([
        'template',
        'dialog',
        'report/report-model',
        'report/publish-report-dialog-template'
    ],
    function (
        template,
        dialog,
        ReportModel,
        publishReportDialogTemplate
    ) {
        return Backbone.View.extend({

            /**
             * 构造函数
             *
             * @constructor
             */
            initialize: function () {
                var that = this;
                that.model = new ReportModel({
                    id: that.id
                });
            },

            /**
             * 发布报表
             *
             * @param {string} type ajax请求类型
             * @param {string} reportId 报表参数
             * @public
             */
            publishReport: function (type, reportId) {
                var that = this;

                // 列表调用需要动态传递id
                reportId = reportId || this.id;

                this.model.publishReport(type, reportId, function (data) {
                    var renderData = {
                        content: data,
                        url: data === 'Not published!' ? '#' : data,
                        type: type
                    };

                    dialog.showDialog({
                        content: publishReportDialogTemplate.render(renderData),
                        title: '报表发布成功',
                        dialog: {
                            height: 500,
                            width: 810,
                            open: function () {
                                var $this = $(this);

                                // 弹框内的dom事件绑定
                                $this.find('.j-report-list').click(function () {
                                    $this.dialog('close');
                                    require(
                                        ['report/list/main-view'],
                                        function (ReportListView) {
                                            window.dataInsight.main.destroy();
                                            new ReportListView({el: $('.j-main')});
                                        }
                                    );
                                });

                                $this.find('.j-report-edit').click(function () {
                                    $this.dialog('close');
                                    require(
                                        ['report/edit/main-view'],
                                        function (EditReportView) {
                                            window.dataInsight.main.destroy();
                                            new EditReportView({
                                                el: $('.j-main'),
                                                id: reportId,
                                                isEdit: true
                                            });
                                        }
                                    );
                                });
                                that.initCopy('copyUrlBtn');
                                that.initCopy('copyTiledBtn');
                                that.initCopy('copyEmbeddedBtn');
                            },
                            buttons: {
                                '确定': function () {
                                    $(this).dialog('close');
                                }
                            }
                        }
                    });

                });
            },

            /**
             * 预览报表
             *
             * @param {string} type ajax请求类型
             * @param {string} reportId 报表参数
             * @public
             */
            previewReport: function (type, reportId) {
                // 列表调用需要动态传递id
                reportId = reportId || this.id;
                this.model.previewReport(type, reportId, function (data) {
                    window.open(data);
                });
            },

            /**
             * 初始化复制功能
             * @param {string} btnId 按钮的id
             * @public
             */
            initCopy: function (btnId) {
                var clip = null;
                clip = new ZeroClipboard.Client();

                clip.setHandCursor(true);

                clip.addEventListener('load', function (client) {});
                clip.addEventListener('mouseDown', function () {
                    var contentId = btnId + 'CopyContent';
                    var copyContent = $('#' + contentId).html();

                    copyContent = copyContent.replace(/&lt;/g, '<');
                    copyContent = copyContent.replace(/&gt;/g, '>');
                    clip.setText(copyContent);
                });
                clip.addEventListener('complete', function () {
                    alert('复制成功');
                });

                clip.glue(btnId, btnId + 'Container');
            }
        });
    }
);