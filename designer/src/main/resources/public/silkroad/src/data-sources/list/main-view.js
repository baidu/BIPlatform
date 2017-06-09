/**
 * @file 数据源列表view
 * @author 赵晓强(longze_xq@163.com)
 * @date 2014-7-17
 */
define([
        'template',
        'dialog',
        'data-sources/list/main-model',
        'data-sources/list/main-template',
        'data-sources/list/set-name-template',
        'data-sources/list/group-item-template'
    ],
    function (
        template,
        dialog,
        MainModel,
        mainTemplate,
        setNameTemplate,
        groupItemTemplate
    ) {

        return Backbone.View.extend({

            /**
             * 事件绑定
             */
            events: {
                'click .j-add-data-sources': 'addDataSources',
                'click .j-delete-data-sources': 'deleteDataSources',
                'click .j-edit-data-sources': 'editDataSources',
                'click .j-add-data-sources-group': 'addDataSourcesGroup',
                'click .j-edit-data-sources-group': 'editDataSourcesGroup',
                'click .j-del-data-sources-group': 'delDataSourcesGroup',
                'click .j-input-data-sources': 'changeDataSourceActive'
            },

            /**
             * 构造函数
             *
             * @param {Object} option 初始化参数
             * @constructor
             */
            initialize: function () {
                var that = this;

                that.model = new MainModel();
                this.listenTo(
                    this.model,
                    'change:dataSourcesList',
                    function (model, data) {
                        that.$el.html(
                            mainTemplate.render({
                                dataSourcesGroupList: data
                            })
                        );
                    }
                );

                this.model.loadDataSourcesList();
                window.dataInsight.main = this;
            },

            /**
             * 添加数据源按钮对应的处理函数
             *
             * @public
             */
            addDataSources: function () {
                window.dataInsight.main.destroy();
                // 进数据源添加/编辑模块
                require([
                        'data-sources/create-view'
                    ],
                    function (DataSourcesCreateView) {
                        new DataSourcesCreateView({
                            el: $('.j-main'),
                            isAdd: true
                        });
                    }
                );
            },

            /**
             * 编辑数据源（调用外模块）
             *
             * @param {event} event 点击事件
             * @public
             */
            editDataSources: function (event) {
                var dataSourcesId = this.getLineId(event);
                var groupId = this.getGroupId(event);
                window.dataInsight.main.destroy();

                // 进编辑模块
                require([
                        'data-sources/create-view'
                    ],
                    function (DataSourcesCreateView) {

                        new DataSourcesCreateView({
                            el: $('.j-main'),
                            id: dataSourcesId,
                            groupId: groupId,
                            isAdd: false
                        });
                    }
                );
            },

            /**
             * 删除数据源
             *
             * @param {event} event 点击事件
             * @public
             */
            deleteDataSources: function (event) {
                var that = this;
                var dsId = this.getLineId(event);
                var groupId = this.getGroupId(event);

                dialog.confirm('是否确定删除当前数据源', function () {
                    that.model.deleteDataSources(groupId, dsId);
                });
            },

            /**
             * 添加数据源组
             *
             * @public
             */
            addDataSourcesGroup: function(event) {
                var that = this;
                dialog.showDialog({
                    title: '添加数据源组',
                    content: setNameTemplate.render({
                        text: '数据源组名称'
                    }),
                    dialog: {
                        width: 300,
                        height: 249,
                        open: function () {
                            var $this = $(this);
                            $this.find('.j-data-sources-group-name')
                                .focus(function () {
                                    $this.find('.j-validation').hide();
                            });
                        },
                        buttons: [
                            {
                                text: '提交',
                                click: function () {
                                    var $this = $(this);
                                    var name = $this.find('.j-data-sources-group-name').val();
                                    if (name == '') {
                                        $this.find('.j-validation')
                                            .html('名称不能为空')
                                            .show();
                                        return;
                                    }
                                    that.model.addDsGroup(
                                        name,
                                        function (dsGroupId) {
                                            $this.dialog('close');
                                            $('.j-data-sources-tbody').append(
                                                groupItemTemplate.render({
                                                    id: dsGroupId,
                                                    name: name
                                                })
                                            );
                                        }
                                    );
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
             * 编辑数据源组
             *
             * @public
             */
            editDataSourcesGroup: function(event) {
                var that = this;
                var $curGroup = $(event.target).parent().parent();
                var $curGroupName = $curGroup.find('label');
                var groupName = $curGroupName.text();
                var groupId = $curGroup.attr('data-id');
                dialog.showDialog({
                    title: '编辑数据源组名称',
                    content: setNameTemplate.render({
                        text: '数据源组名称',
                        name: groupName
                    }),
                    dialog: {
                        width: 300,
                        height: 249,
                        open: function () {
                            var $this = $(this);
                            $this.find('.j-data-sources-group-name')
                                .focus(function () {
                                    $this.find('.j-validation').hide();
                                });
                        },
                        buttons: [
                            {
                                text: '提交',
                                click: function () {
                                    var $this = $(this);
                                    var name = $this.find('.j-data-sources-group-name').val();
                                    if (name == '') {
                                        $this.find('.j-validation').html('名称不能为空').show();
                                        return;
                                    }
                                    that.model.editDsGroup(
                                        groupId,
                                        name,
                                        function () {
                                            $this.dialog('close');
                                            $curGroupName.text(name);
                                        }
                                    );
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
             * 删除数据源组
             *
             * @public
             */
            delDataSourcesGroup: function(event) {
                var that = this;
                var $curGroup = $(event.target).parent().parent();
                var groupId = $curGroup.attr('data-id');

                dialog.confirm('是否确定删除当前数据源组', function () {
                    that.model.delDsGroup(groupId, function() {
                        $curGroup.remove();
                        $('input[name=' + groupId + ']').each(function() {
                            $(this).parents('.j-root-line').remove();
                        });
                    });
                });
            },

            /**
             * 获取当前行的数据源id
             *
             * @param {event} event 事件
             * @public
             */
            getLineId: function (event) {
                return $(event.target).parents('.j-root-line').attr('data-id');
            },

            /**
             * 获取当前行的数据源组id
             *
             * @param {event} event 事件
             * @public
             */
            getGroupId: function (event) {
                return $(event.target).parents('.j-root-line').find('input').attr('name');
            },

            /**
             * 改变活动的数据源
             *
             * @param {event} event 事件
             * @public
             */
            changeDataSourceActive: function(event) {
                var groupId = $(event.target).attr('name');
                var dsId = $(event.target).attr('id').split('-')[1];
                this.model.changeDataSourceActive(groupId, dsId);
            },

            /**
             * 销毁当前view与其对应的model
             *
             * @public
             */
            destroy: function () {
                // 销毁 model
                this.model.clear({
                    silent: true
                });

                // 停止监听model事件
                this.stopListening();
                // 解绑jq事件
                $(this.el).unbind().empty();
            }
        });
    });