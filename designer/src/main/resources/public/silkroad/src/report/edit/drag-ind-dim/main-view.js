/**
 * @file 对左边的数据可进行拖拽操作，
 * 右边的组件可接收在组件编辑模块（canvas/edit-comp-view.js）中定义
 * @author 赵晓强(longze_xq@163.com)
 * @date 2014-7-31
 */
define([
        'template',
        'dialog',
        'report/edit/drag-ind-dim/main-model'
    ],
    function (template, dialog, MainModel) {

        return Backbone.View.extend({

            /**
             * 构造函数
             *
             * @param {Object} option 配置参数
             * @constructor
             */
            initialize: function (option) {
                this.model = new MainModel({
                    id: this.id,
                    parentModel: option.parentView.model
                });
                this.parentView = option.parentView;
            },

            /**
             * 初始化全部可拖拽组件的行为
             *
             * @public
             */
            initAll: function () {
                this.initDrag();
                this.initAcceptDragToGroup();
                this.initAcceptIndToOrgDim();
                this.initAcceptDimToOrgInd();
            },

            /**
             * 初始化指标、衍生指标、维度、维度组的可拖拽
             *
             * @public
             */
            initDrag: function () {
                var that = this;
                $('.j-olap-element', that.$el).draggable({
                    //revert: "invalid",
                    helper: "clone",
                    appendTo: "body",
                    opacity: 0.8, // 被拖拽元素的透明度
                    zIndex: 10,
                    start: function (event, ui) {
                        ui.helper.addClass('olap-element-dragging');
                        ui.helper.find('.icon-letter').remove();
                    },
                    stop: function (event, ui) {
                        ui.helper.removeClass('olap-element-dragging');
                    }
                });
            },

            /**
             * 定义维度组的接收拖拽入的元素
             *
             * @public
             */
            initAcceptDragToGroup: function () {
                var that = this;

                // 定义维度组所包含的维度可以拖动排序
                $('.j-dim-group', this.el).sortable({
                    items: ".j-sub-dim",
                    axis: 'y',
                    stop: function (event, ui) {
                        // 进入的区域dom
                        var $prevItem = ui.item.prev();
                        var dimGroupId = ui.item.parent().attr('data-id');
                        var dimId = ui.item.attr('data-id');
                        var beforeDimId;
                        if ($prevItem.hasClass('j-group-title')) {
                            beforeDimId = -1;
                        } else {
                            beforeDimId = ui.item.prev().attr('data-id');
                        }
                        that.model.sortSubDim(dimGroupId, dimId, beforeDimId);
                    }
                });

                // 维度组接收原始维度和时间维度（虽然时间维度也是原始维度）
                $('.j-dim-group', this.el).droppable({
                    accept: '.j-org-dim,.j-time-dim,.j-callback-dim',
                    drop: function (event, ui) {
                        var $dimGroup = $(this);
                        var groupId = $(this).attr('data-id');
                        var dimId = ui.draggable.attr('data-id');

                        // 重复判断
                        var confirmStr = '已存在此维度是否依然执行拖拽进入';
                        var selector = '.j-sub-dim[data-id=' + dimId + ']';
                        if ($dimGroup.find(selector).length > 0) {
                            dialog.confirm(confirmStr, function () {
                                that.model.dimToGroup(groupId, dimId);
                            }, function () {
                                $dimGroup.removeClass('active');
                            });
                        }

                        that.model.dimToGroup(groupId, dimId);
                        $dimGroup.removeClass('active');
                    },
                    out: function (event, ui) {
                        $(this).removeClass('active');
                    },
                    over: function (event, ui) {
                        $(this).addClass('active');
                    },
                    helper: "clone"
                });
            },

            /**
             * 定义原始维度区域可接收指标
             *
             * @public
             */
            initAcceptIndToOrgDim: function () {
                var that = this;

                $('.j-con-org-dim', this.el).droppable({
                    accept: ".j-can-to-dim",
                    drop: function (event, ui) {
                        $(this).removeClass('active');
                        var $item = ui.draggable;
                        var id = $item.attr('data-id');
                        that.model.indDimSwitch('ind', 'dim', id);
                    },
                    out: function (event, ui) {
                        $(this).removeClass('active');
                    },
                    over: function (event, ui) {
                        $(this).addClass('active');
                    },
                    helper: "clone"
                });
            },

            /**
             * 定义原始指标区接收维度
             *
             * @public
             */
            initAcceptDimToOrgInd: function () {
                var that = this;

                $('.j-con-org-ind', this.el).droppable({
                    accept: ".j-can-to-ind",
                    drop: function (event, ui) {
                        $(this).removeClass('active');
                        var $item = ui.draggable;
                        var id = $item.attr('data-id');
                        that.model.indDimSwitch('dim', 'ind', id, function () {
                            $item.remove();
                            that.parentView.model.loadIndList();
                        });
                    },
                    out: function (event, ui) {
                        $(this).removeClass('active');
                    },
                    over: function (event, ui) {
                        $(this).addClass('active');
                    },
                    helper: "clone"
                });

            }
        });
    });