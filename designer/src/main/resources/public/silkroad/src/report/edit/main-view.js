/**
 * @file
 * @author 赵晓强(longze_xq@163.com)
 * @date 2014-7-28
 */
define([
        'template',
        'dialog',
        'report/edit/main-model',
        'report/edit/main-template',
        'report/edit/ind-template',
        'report/edit/dim-template',
        'report/edit/drag-ind-dim/main-view',
        'report/edit/ue-view',
        'report/global-setting-btns/btns-view'
    ],
    function (
        template,
        dialog,
        MainModel,
        mainTemplate,
        indTemplate,
        dimTemplate,
        DragView,
        UEView,
        BtnsView
        ) {
        return Backbone.View.extend({
            // view事件绑定
            events: {
                'change .j-cube-select': 'changeCube',
                'click .j-global-para': 'setglobalbtn'
            },

            /**
             * 构造函数
             *
             * @param {Object} option 初始化配置项
             * @param {boolen} option.isEdit 是否是编辑
             * @constructor
             */
            initialize: function (option) {
                this.model = new MainModel({
                    id: this.id,
                    isEdit: option.isEdit
                });
                this.model.loadCubeList();
                this.btnsView = new BtnsView();
                this.initListening();
            },

            /**
             * 参数维度弹出框事件
             */
            setglobalbtn: function () {
                this.model.loadDimList();
                var dim = {};
                dim.dimList = this.model.get('dimList');
                this.btnsView.setGlobal(dim);
            },

            /**
             * 获取cube列表
             *
             * @param {event} event 下拉框值改变的事件
             * @public
             */
            changeCube: function (event) {
                var value = $(event.target).val();
                this.model.set({'currentCubeId': value});
                this.dragView.initAll();
            },

            /**
             * 初始化事件监听
             *
             * @public
             */
            initListening: function () {
                var that = this;

                // 因为每张报表只有一个cube列表，所以只需监听加载一次
                this.listenToOnce(
                    this.model,
                    'change:cubeList',
                    function (model, data) {
                        that.$el.html(mainTemplate.render({
                            cubeList: data
                        }));
                        that.initChildView();
                    }
                );

                this.listenTo(
                    this.model,
                    'change:indList',
                    function (model, data) {
                        that.$el.find('.j-data-sources-setting-con-ind').html(
                            indTemplate.render({indList: data})
                        );
                        that.dragView.initAll();
                    }
                );

                this.listenTo(
                    this.model,
                    'change:dimList',
                    function (model, data) {
                        that.$el.find('.j-data-sources-setting-con-dim').html(
                            dimTemplate.render({dimList: data}));
                        that.dragView.initAll();
                    }
                );

            },

            /**
             * 初始化编辑报表需要的子模块
             *
             * @public
             */
            initChildView: function () {
                var that = this;

                // 左边的操作，包括：设置指标汇总方式、重命名、创建维度组
                require(['report/edit/setting/main-view'], function (View) {
                    new View({
                        el: that.$el.find('.j-data-sources-setting'),
                        id: that.id,
                        parentView: that
                    });
                });

                // 左边自身的拖拽与向右边的拖拽
                that.dragView = new DragView({
                    el: that.$el.find('.j-data-sources-setting'),
                    id: that.id,
                    parentView: that
                });

                // 增加用户体验的操作，维度组的折叠，左边可拖拽调整大小
                that.ueView = new UEView({
                    el: that.$el,
                    id: that.id,
                    parentView: that
                });

                // 右边画布的初始化（包括组件箱，与数据配置框）
                require(['report/edit/canvas/canvas-view'], function (View) {
                    that.canvas = new View({
                        el: that.$el.find('.j-canvas'),
                        id: that.id,
                        parentView: that
                    });
                });
                // FIXME:临时使用，重构时，逻辑干掉
                $(document).mousedown(function (e) {
                    // 如果触发元素，不属于组件添加按钮区域
                    if (
                        $('.j-global-btn')[0]
                        && !$.contains($('.j-global-btn')[0], e.target)
                    ) {
                        // 如果触发元素，不属于组件区域
                        if (
                            $('.j-all-menus')[0] && (!$.contains($('.j-global-menu')[0], e.target))
                        ) {
                            // 上面两个条件都满足，组件区域如果显示，那么就该隐藏掉
                            //if ($('.j-con-component') && (!$('.j-con-component').is(':hidden'))) {
                            $('.j-all-menus').each(function () {
                                $(this).hide();
                            });
                            //$('.j-all-menus').hide();
                            //}
                        }
                    }
                });
            },

            /**
             * 销毁当前view
             *
             * @public
             */
            destroy: function () {
                // 销毁 model
                this.model.clear({silent: true});
                // 停止监听model事件
                this.stopListening();
                // 解绑jq事件
                $(this.el).unbind().empty();
                this.canvas.destroy();
                $('.j-foot').show();
            }
        });
    }
);