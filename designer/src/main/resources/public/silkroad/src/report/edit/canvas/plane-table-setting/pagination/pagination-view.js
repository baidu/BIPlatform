/**
 * @file: 报表新建（编辑）-- 平面表格组件编辑模块 -- 分页设置 View
 * @author: lizhantong
 * @depend:
 * @date: 2015-07-10
 */

define(
    [
        'dialog',
        'report/edit/canvas/plane-table-setting/pagination/pagination-model',
        'report/edit/canvas/plane-table-setting/pagination/pagination-template'
    ],
    function (
        dialog,
        PaginationModel,
        PaginationTemplate
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
                'click .j-set-pagination': 'getPaginationData'
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
                this.model = new PaginationModel({
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
            getPaginationData: function (event) {
                var that = this;
                that.model.getPaginationData(function (data) {
                    that._openPaginationDialog(data);
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
            _openPaginationDialog: function (data) {
                var that = this;
                var html;

                html = PaginationTemplate.render(data);
                dialog.showDialog({
                    title: '分页设置',
                    content: html,
                    dialog: {
                        width: 400,
                        height: 250,
                        resizable: false,
                        buttons: [
                            {
                                text: '提交',
                                click: function () {
                                    that._savePaginationInfo($(this));
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
             * 绑定事件
             *
             * @private
             */
            _bindEvent: function () {
                var $isPagination = $('.j-isPagination');
                var $pageSize = $('.j-isPaginationBox .j-pageSize');
                var $defaultPageSize = $('.j-notPaginationBox input');

                // 绑定是否显示分页事件
                $isPagination.unbind();
                $isPagination.change(function () {
                    var isChked = $(this).is(':checked');
                    if (isChked) {
                        $('.j-isPaginationBox').show();
                        $('.j-notPaginationBox').hide();
                    }
                    else {
                        $('.j-isPaginationBox').hide();
                        $('.j-notPaginationBox').show();
                    }
                });

                // 绑定分页码输入事件
                $pageSize.unbind();
                $pageSize.keyup(function (event) {
                    var $input = $(event.target);
                    var pageSize = $input.val().trim();

                    // 如果输入不合法，返回
                    if (!(/^\d+$/.test(pageSize))) {
                        $input.val('');
                        return;
                    }
                });

                $defaultPageSize.unbind();
                $defaultPageSize.keyup(function (event) {
                    var $input = $(event.target);
                    var pageSize = $input.val().trim();

                    // 如果输入不合法，返回
                    if (!(/^\d+$/.test(pageSize))) {
                        $input.val('');
                        return;
                    }
                });

                $('.j-new-pageSize').unbind();
                $('.j-new-pageSize').click(function () {
                    $('.j-pagination-error-msg').hide();
                    var html = [];
                    var pageSize = $('.j-pageSize').val().trim();
                    if (!$.isPositiveInt(pageSize)) {
                        $('.j-pagination-error-msg').html('请输入正确格式的数字').show();
                        return;
                    }
                    // 获取下拉框,重新渲染下拉框内容（包括了新输入的pageSize项）
                    var $pageSizeOptions = $('.j-isPaginationBox .j-pageSizeOptions');
                    var pageSizeOptions = [];
                    var optionItems = $pageSizeOptions[0].options;
                    for (var i = 0, iLen = optionItems.length; i < iLen; i ++) {
                        pageSizeOptions.push(optionItems[i].value);
                    }

                    (!$.isInArray(pageSize, pageSizeOptions))
                    && pageSizeOptions.push(pageSize);

                    pageSizeOptions.sort(function (a, b) {
                        return a - b;
                    });
                    for (var i = 0, iLen = pageSizeOptions.length; i < iLen; i ++) {
                        var selected = ' selected="selected"';
                        html.push(
                            '<option value="', pageSizeOptions[i], '"',
                            pageSize === pageSizeOptions[i] ? selected : '',
                            '>',
                            pageSizeOptions[i],
                            '</option>'
                        );
                    }
                    $('.j-pageSizeOptions').html(html.join(''));
                });
                $('.j-reset-pageSize').unbind();
                $('.j-reset-pageSize').click(function () {
                    $('.j-pagination-error-msg').hide();
                    var html = [];
                    var pageSizeOptions = [10, 50, 100];

                    for (var i = 0, iLen = pageSizeOptions.length; i < iLen; i ++) {
                        var selected = ' selected="selected"';
                        html.push(
                            '<option value="', pageSizeOptions[i], '"', '>',
                            pageSizeOptions[i],
                            '</option>'
                        );
                    }
                    $('.j-pageSizeOptions').html(html.join(''));
                });
            },

            /**
             * 保存分页设置信息
             *
             * @param {$HTMLElement} $dialog 弹出框$el元素
             * @private
             */
            _savePaginationInfo: function ($dialog) {
                $('.j-pagination-error-msg').hide();
                var that = this;
                var $pageSizeOptions;
                var pageSize;
                var pageSizeOptions;
                var options;
                var isPagination = $('.data-format-black')
                    .find('.j-isPagination')
                    .is(':checked');
                var pagination = {};

                pagination.isPagination = isPagination;
                // 如果分页
                if (isPagination) {
                    $pageSizeOptions = $('.j-isPaginationBox .j-pageSizeOptions');
                    pageSize = $pageSizeOptions.val().trim();
                    pageSizeOptions = [];
                    options = $pageSizeOptions[0].options;
                    for (var i = 0, iLen = options.length; i < iLen; i ++) {
                        pageSizeOptions.push(Number(options[i].value));
                    }

                    pagination.pageSize = Number(pageSize);
                    pagination.pageSizeOptions = pageSizeOptions;
                }
                else {
                    var val = $('.j-notPaginationBox input').val().trim();
                    if (!$.isPositiveInt(val)) {
                        $('.j-pagination-error-msg').html('请输入正确格式的数字').show();
                        return;
                    }
                    pagination.pageSize = Number(val);
                }

                var formData = {
                    pagination: JSON.stringify(pagination)
                };
                that.model.savePaginationData(formData, function () {
                    $dialog.dialog('close');
                    that._setPagination(pagination);
                });

            },


            /**
             * 处理分页控件
             *
             * @param {Object} option 分页设置信息
             *
             * @private
             */
            _setPagination: function (option) {
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

                var tableChilds = $($table.children()).children();
                var paginationBox = tableChilds[tableChilds.length - 1];

                var paginationId;
                if (!paginationBox) {
                    canvas.showReport();
                    return;
                }

                paginationId = $(paginationBox).attr('data-o_o-di');
                var hasPagination = paginationId.indexOf('table-pager'); // 分页

                // 如果不需要分页
                if (!option.isPagination) {
                    // 当前存在分页
                    if (hasPagination > 0) {
                        $table.children()[0].remove();

                        // 移除掉$reportVm中的 分页容器，重设表格高度
                        tableBox.height(tableBox.height() - 22);
                        $table = $($(tableBox).children());
                        $table.children()[$table.children().length - 1].remove();

                        // 移除掉表格组件中对富文本下拉框的关联
                        delete $.getTargetElement(compId, entityDefs).vuiRef.pager;

                        // 找到富文本下拉框json，并从json集合中移除掉
                        for (var i = 0, index = 0, iLen = entityDefs.length;
                             i < iLen; i ++
                        ) {
                            if (entityDefs[i].id === paginationId) {
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
                // 如果需要分页
                else {
                    paginationId = 'snpt.' + compId + '-vu-table-pager';
                    var json = {
                        clzType: 'VUI',
                        clzKey: 'ECUI_PAGER',
                        id: paginationId,
                        compId: compId,
                        dataOpt: {
                            pageSize: option.pageSize,
                            pageSizeOptions: option.pageSizeOptions
                        }
                    };
                    // 当前没有分页
                    if (hasPagination < 0) {
                        var html = [
                            '<div class="di-o_o-line">',
                            '<div class="" data-o_o-di="', paginationId, '"></div>',
                            '</div>'
                        ].join('');
                        // 表格组件中添加vm
                        tableBox.height(tableBox.height() + 22);
                        $table = $($(tableBox).children());
                        $table.append(html);

                        // 表格组件中添加json
                        canvasModel.reportJson.entityDefs.push(json);
                        var entity = $.getTargetElement(compId, entityDefs);
                        entity.vuiRef.pager = paginationId;
                    }
                    // 当前存在分页
                    else {
                        for (var i = 0, index = 0, iLen = entityDefs.length;
                             i < iLen; i ++
                        ) {
                            if (entityDefs[i].id === paginationId) {
                                index = i + 1;
                                break;
                            }
                        }
                        index && (
                            canvasModel.reportJson.entityDefs = entityDefs
                                .slice(0, index - 1)
                                .concat(entityDefs.slice(index))
                        );
                        canvasModel.reportJson.entityDefs.push(json);

                    }
                }

                canvas.model.saveJsonVm(function () {
                    canvas.showReport();
                });
            }
        });
    }
);