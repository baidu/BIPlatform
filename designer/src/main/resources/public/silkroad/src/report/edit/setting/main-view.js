/**
 * @file 设置左边数据
 * @author 赵晓强(longze_xq@163.com)
 * @date 2014-7-30
 */
define([
        'template',
        'dialog',
        'common/float-window',
        'report/edit/setting/main-model',
        'report/edit/setting/show-data-template',
        'report/edit/setting/ind-menu-template',
        'report/edit/setting/dim-group-menu-template',
        'report/edit/setting/data-model-menu-template'
    ],
    function (
        template,
        dialog,
        FloatWindow,
        MainModel,
        showDataTemplate,
        indMenuTemplate,
        dimGroupMenuTemplate,
        dataModelMenuTemplate
    ) {

        return Backbone.View.extend({

            // 事件绑定配置
            events: {
                'click .j-icon-data-sources': 'setDataModel',
                'click .j-method-type': 'setInd',
                'click .j-edit-dim-name': 'editDimName',
                'click .j-delete-sub-dim': 'deleteSubDim',
                'click .j-edit-dim-group': 'editDimGroup',
                'click .j-add-dim-group-btn': 'addDimGroupSwitchToInput',
                'keyup .j-add-dim-group-input': 'addDimGroup',
                'focusout .j-add-dim-group-input': 'addDimGroupSwitchToBtn',
                'click .j-setting-derive-inds': 'settingDeriveInds',
                'click .j-setting-dim-group': 'settingDimGroup'
            },

            /**
             * 构造函数
             *
             * @param {Object} option 配置参数
             * @param {$HTMLElement} option.el .j-canvas
             * @param {string} option.id 报表id
             * @param {Object} option.parentView 父view（report/edit/main-view）
             * @constructor
             */
            initialize: function (option) {
                var that = this;
                this.model = new MainModel({
                    id: this.id,
                    parentModel: option.parentView.model
                });
                this.parentView = option.parentView;
            },

            /**
             * 对指标、维度、维度组 重命名
             *
             * @param {$HTMLElement} $lineDom 修改对象的行dom
             * @param {string} type 指标：ind，维度：dim，维度组：dim-group
             * @public
             */
            rename: function ($lineDom, type) {

                // 防止点两次
                if ($lineDom.find('.j-rename-input').length > 0) {
                    return;
                }

                var indDimType = type == 'dim-group' ? 'dim' : type;
                var that = this;
                var id = $lineDom.attr('data-id');
                var data = this.parentView.model.getItemDataById(
                    id,
                    indDimType
                );
                var $itemText = $lineDom.find('.j-item-text');
                var width = $itemText.width() - 20;

                var classStr = 'rename-input j-rename-input';
                var $input = $('<input type="text" class="' + classStr + '">');
                $input.width(width).val(data.caption);
                $itemText.hide().before($input);
                $input.focus();

                $input.keyup(function (event) {
                    if (event.keyCode == 13) {
                        var newCaption = $input.val().trim();
                        putName(newCaption);
                    }
                });

                $input.blur(function (event) {
                    var newCaption = $input.val().trim();
                    putName(newCaption);
                });

                function putName(newCaption) {
                    // 校验
                    if (newCaption === '') {
                        dialog.alert('名称不能为空');
                        return;
                    }
                    if (newCaption == data.caption) {
                        reDom(newCaption);
                        return;
                    }

                    that.model.putName(id,
                        newCaption,
                        indDimType,
                        function () {
                            // 更名成功
                            data.caption = newCaption;
                            reDom(newCaption);
                        }
                    );
                }

                // 还原DOM
                function reDom(newCaption, itemData) {
                    $input.remove();

                    if (type == 'dim-group') {
                        $itemText.show().html(newCaption);
                        var selector = '.j-con-comp-setting [data-id=' + data.id + ']';
                        $(selector).find('.j-item-text').html(newCaption);
                    }
                    else {
                        var html = newCaption + '（' + data.name + '）';
                        // 通过id更新dom（有一定的风险）
                        $('[data-id=' + data.id + ']').find('.j-item-text').show().html(html);
                        // 手动触发重新加载数据列表，后续做微创修改时可以用上面代码
                        that.parentView.model.loadDimList();
                    }
                }
            },

            /**
             * 修改数据模型（“左上角的小按钮/修改数据模型”被点击时触发）
             *
             * @param {event} 点击事件
             * @public
             */
            setDataModel: function (event) {
                var that = this;
                if (this.dataModelMenu) {
                    this.showDataModelMenu(event);
                    return;
                }

                this.dataModelMenu = new FloatWindow({
                    content: dataModelMenuTemplate.render()
                });
                this.setDataModel = this.showDataModelMenu;
                var $dataModelMenu = $(this.dataModelMenu.el);

                // 修改指显示数据
                $dataModelMenu.find('.j-show-data').click(function (event) {
                    that.openSettingShowDataDialog();
                    $dataModelMenu.hide();
                });

                // 跳转到维度设置
                $dataModelMenu.find('.j-change-data-sources').click(
                    function (event) {
                        require(['report/dim-set/view'], function (View) {
                            $dataModelMenu.hide();
                            that.parentView.destroy();
                            window.dataInsight.main = new View({
                                el: $('.j-main'),
                                id: that.id
                            });
                        });
                    }
                );

                // 初始化后做操作转向
                this.showDataModelMenu(event);
            },

            /**
             * 打开“设置显示数据”对话框
             *
             * @public
             */
            openSettingShowDataDialog: function () {
                var that = this;

                this.model.loadShowData(function (data) {
                    dialog.showDialog({
                        dialog: {
                            height: 300,
                            width: 435,
                            buttons: {
                                "提交": function () {
                                    var $dialogDom = $(this);
                                    that.submitShowData($dialogDom,
                                        function () {
                                            $dialogDom.dialog('close');
                                        });
                                },
                                '取消': function () {
                                    $(this).dialog('close');
                                }
                            }
                        },
                        content: showDataTemplate.render(data.data),
                        title: '筛选显示数据'
                    });
                });
            },

            /**
             * 整理表单数据，通过model提交显示数据
             *
             * @param $dom 弹出框dom
             * @param closeDialog 关闭弹窗的回调函数
             * @public
             */
            submitShowData: function ($dom, closeDialog) {
                var that = this;
                var data = {
                    "oriInd": [],
                    "oriDim": []
                };

                var item;
                for (var name in data) {
                    $dom.find('.j-' + name + ' input').each(function () {
                        item = {
                            "id": $(this).val(),
                            "selected": $(this).attr('checked') == 'checked' ?
                                1 :
                                0
                        };
                        data[name].push(item);
                    });
                }

                data.oriInd = JSON.stringify(data.oriInd);
                data.oriDim = JSON.stringify(data.oriDim);

                // 向后台提交数据
                that.model.submitSowData(data, closeDialog);
            },

            /**
             * 点击数据项后面的小图标，展示操作列表
             *
             * @param {event} 点击事件
             * @public
             */
            showDataModelMenu: function (event) {
                var that = this;
                var $target = $(event.target);

                this.dataModelMenu.show($target);
            },
            setInd: function (event) {
                var that = this;
                var $itemLine = $(event.target).parents('.j-root-line');
                that.id = $itemLine.attr('data-id');
                if (this.indMenu) {
                    this.showIndMenu(event);
                    return;
                }

                this.indMenu = new FloatWindow({
                    content: indMenuTemplate.render()
                });
                this.setInd = this.showIndMenu;
                var $indMenu = $(this.indMenu.el);

                // 修改指标汇总方式
                $indMenu.find('.j-method-type').click(function (event) {
                    var indData = that.model.get('activeInd');
                    var aggregatorValue;
                    var aggregatorValue = $(this).attr('data-value');

                    that.model.putAggregator(
                        that.parentView,
                        indData,
                        aggregatorValue,
                        function (mark) {
                            var view = that.parentView;
                            var selector = '.j-root-line';
                            selector += '[data-id=' + indData.id + ']';
                            var $selector = view.$el.find(selector);
                            $selector.find('.j-method-type').html(mark);
                        }
                    );

                    $indMenu.hide();
                });

                // 修改指标名称
                $indMenu.find('.j-rename').click(function () {
                    var selector = ''
                        + '.j-data-sources-setting-con-ind .j-root-line'
                        + '[data-id=' + that.id + ']';
                    that.rename(that.$el.find(selector), 'ind');
                    $indMenu.hide();
                });

                // 初始化后做操作转向
                this.showIndMenu(event);
            },
            showIndMenu: function (event) {
                var that = this;
                var $target = $(event.target);
                var id = $target.parents('.j-root-line').attr('data-id');
                var items = $(this.indMenu.el).find('.j-method-type');
                var data = this.parentView.model.getItemDataById(id, 'ind');
                this.model.set('activeInd', data);

                items.each(function (index, item) {
                    var $item = $(item);
                    if ($item.attr('data-value') == data.aggregator) {
                        $item.addClass('selected');
                    } else {
                        $item.removeClass('selected');
                    }
                });

                this.indMenu.show($target);
            },
            editDimName: function (event) {
                var $lineDom = $(event.target).parents('.j-root-line');
                this.rename($lineDom, 'dim');
            },

            /**
             * 删除维度组中的子维度项
             *
             * @param {event} event 点击事件
             */
            deleteSubDim: function (event) {
                var that = this;
                dialog.confirm('是否确定删除？', function () {
                    var $itemLine = $(event.target).parents('.j-root-line');
                    var $dimGroup = $itemLine.parents('.j-dim-group');
                    var groupId = $dimGroup.attr('data-id');
                    that.model.deleteSubDim(
                        groupId,
                        $itemLine.attr('data-id'),
                        function () {
                            $itemLine.remove();
                        }
                    );
                });
            },

            /**
             * 点击维度组小图标弹出维度编辑操作选项卡
             *
             * @param {event} event 点击事件
             * @public
             */
            editDimGroup: function (event) {
                var that = this;
                if (this.dimGroupMenu) {
                    this.showDimGroupMenu(event);
                    return;
                }

                this.dimGroupMenu = new FloatWindow({
                    content: dimGroupMenuTemplate.render()
                });
                //this.editDimGroup = this.showDimGroupMenu;
                var $dimGroupMenu = $(this.dimGroupMenu.el);

                // 修改维度组名称
                $dimGroupMenu.find('.j-rename').click(function () {
                    var activeDimGroup = that.model.get('activeDimGroup');
                    var selector = ''
                        + '.j-data-sources-setting-con-dim .j-group-title'
                        + '[data-id=' + activeDimGroup.id + ']';
                    that.rename(that.$el.find(selector), 'dim-group');
                    $dimGroupMenu.hide();
                });

                // 删除维度组
                $dimGroupMenu.find('.j-delete').click(function () {
                    var activeDimGroup = that.model.get('activeDimGroup');
                    var selector = ''
                        + '.j-data-sources-setting-con-dim .j-dim-group'
                        + '[data-id=' + activeDimGroup.id + ']';
                    var $dimGroup = that.$el.find(selector);
                    dialog.confirm('是否确定删除？', function () {
                        that.model.deleteDimGroup($dimGroup.attr('data-id'),
                            function () {
                                $dimGroup.remove();
                            });
                    });
                    $dimGroupMenu.hide();
                });

                // 初始化后做操作转向
                this.showDimGroupMenu(event);
            },

            /**
             * 显示维度组的操作列表（点击维度组后的小图标出现）
             *
             * @param {event} event 点击事件
             * @public
             */
            showDimGroupMenu: function (event) {
                var that = this;
                var $target = $(event.target);
                var id = $target.parents('.j-group-title').attr('data-id');
                var parentModel = this.parentView.model;
                var activeDimGroup = parentModel.getItemDataById(id, 'dim');

                this.model.set('activeDimGroup', activeDimGroup);
                this.dimGroupMenu.show($target);
            },

            /**
             * 切换到可添加维度的状态（隐藏按钮，显示输入框）
             *
             * @param {event} event 按钮的点击事件
             * @public
             */
            addDimGroupSwitchToInput: function (event) {
                var $this = $(event.target).hide();
                $this.next().show();
            },

            /**
             * 切换到可添加维度的状态（隐藏按钮，显示输入框）
             *
             * @param {event} event 按钮的点击事件
             * @public
             */
            addDimGroupSwitchToBtn: function (event) {
                var $target = $(event.target);
                if ($target.val() === '') {
                    $target.hide();
                    $target.prev().show();
                }
            },

            /**
             * 添加维度组
             *
             * @param {event} event 键盘弹起事件
             * @public
             */
            addDimGroup: function (event) {
                var that = this;
                if (event.keyCode == 13) {
                    var $input = $(event.target);
                    var groupName = $input.val().trim();

                    // 校验
                    if (groupName === '') {
                        dialog.alert('名称不能为空');
                        return ;
                    }
                    that.model.createDimGroup(groupName, function () {
                        // 创建成功
                        that.model.loadDimList();
                    });
                }
                // esc 键
                if (event.keyCode == 27) {
                    var $this = $(event.target).hide();
                    $this.prev().show();
                }
            },

            /**
             * 管理衍生指标
             *
             * @public
             */
            settingDeriveInds: function () {
                var that = this;

                // 第一次点击
                if (!that.deriveInds) {
                    require(['report/edit/setting/derivative-ind-mgr/mgr-view'], function (DeriveInds) {
                        var deriveInds = new DeriveInds({
                            currentCubeId: that.getCurrentCubeId()
                        });

                        that.deriveInds = deriveInds;
                        // 准备完之后再次触发点击事件，为了归并逻辑
                        that.settingDeriveInds();
                    });
                } else {
                    that.deriveInds.openDialog();
                }
            },

            /**
             * 管理维度组
             *
             * @public
             */
            settingDimGroup: function () {
                var that = this;

                // 第一次点击
                if (!that.dimGroupMgrView) {
                    require(['report/edit/setting/dim-group-mgr/mgr-view'], function (dimGroupMgrView) {
                        var dimGroupMgrView = new dimGroupMgrView({
                            settingView: that.parentView
                        });

                        that.dimGroupMgrView = dimGroupMgrView;
                        // 准备完之后再次触发点击事件，为了归并逻辑
                        that.settingDimGroup();
                    });
                } else {
                    that.dimGroupMgrView.openDialog();
                }
            },

            /**
             * 获取当前报表的cubeId
             *
             * @public
             */
            getCurrentCubeId: function () {
                var val = $('.j-cube-select', this.el).val();
                if (val) {
                    return val;
                } else {
                    throw new Error('当前cubId不可识别');
                }
            },

            /**
             * 销毁当前view及附带的model
             *
             * @public
             */
            destroy: function () {
                // 销毁 model
                this.model.clear({silent: true});
                // 停止监听model事件
                this.stopListening();
                // 解绑jq事件
                this.$el.unbind().empty();
            }

        });
    });