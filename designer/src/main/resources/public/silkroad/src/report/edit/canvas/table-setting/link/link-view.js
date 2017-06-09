/**
 * @file: 报表新建（编辑）-- 表格组件编辑模块 -- 跳转设置view lzt
 *
 * @author: lizhantong
 * @depend:
 * @date: 2015-06-10
 */
define(
    [
        'dialog',
        'report/edit/canvas/table-setting/link/link-model',
        'report/edit/canvas/table-setting/link/link-template',
        'report/edit/canvas/table-setting/link/link-param-template',
        'report/edit/canvas/table-setting/link/link-operation-column-item-template',
        'report/edit/canvas/table-setting/link/link-param-add-template'
    ],
    function (
        dialog,
        LinkModel,
        LinkSettingTemplate,
        LinkSettingParamTemplate,
        LinkSettingColumnItemTemplate,
        LinkParamSettingAddTemplate
    ) {

        //------------------------------------------
        // 视图类的声明
        //------------------------------------------
        var View = Backbone.View.extend({
            events: {
                'click .j-set-link': 'dialogLinkSetting'
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

                that.model = new LinkModel({
                    canvasModel: option.canvasView.model,
                    reportId: option.reportId
                });
                this.model.set('compId', this.$el.find('.j-comp-setting').attr('data-comp-id'));
            },

            /**
             * 获取列跳转平面表设置数据
             *
             * @param {event} event 点击事件
             * @public
             */
            dialogLinkSetting: function (event) {
                var that = this;
                that.model.getColumnLinkPlaneList(function (data) {
                    that._openColumnLinkPlaneDialog(data);
                    that.bindEvents();
                });
            },

            /**
             * 绑定跳转设置中的所有事件
             *
             * @public
             */
            bindEvents: function() {
                var that = this;
                var $next = $('.j-table-link-set-column-table .j-next');
                var $back = $('.j-table-link-set-param-table .j-back');
                var $ok = $('.j-table-link-set-param-table .j-ok');
                $next.unbind();
                $back.unbind();
                $ok.unbind();
                $next.bind('click', function () {
                    that.saveColumnTableRelation($(this));
                });
                $back.bind('click', function () {
                    $('.j-table-link-set-first').show();
                    $('.j-table-link-set-param-table').hide();
                });
                $ok.bind('click', function () {
                    that.saveParamRelation();
                });
                $('.j-table-link-set-plane-table').unbind();
                $('.j-table-link-set-plane-table').change(function (event) {
                    that.showSetParamBtn(event);
                });
                that.bindOperationColumnEvents();
            },

            /**
             * 绑操作列设置相关的事件
             *
             * @public
             */
            bindOperationColumnEvents: function () {
                var that = this;
                var $add = $('.j-table-link-operation-column .j-add');
                var $items = $('.j-table-link-operation-column-items');
                var planeTableList;
                var model = that.model;
                $add.unbind();
                $add.bind('click', function () {
                    var data = {};
                    data.planeTableList = model.get('planeTableList');
                    var operationColumnId = model.get('operationColumnId');
                    var ids = operationColumnId.split('_');
                    var id = ids[0];
                    var index = Number(ids[1]);
                    index ++;
                    id += '_' + index;
                    model.set('operationColumnId', id);
                    data.operationColumnId = id;
                    $items.append(
                        LinkSettingColumnItemTemplate.render(data)
                    );
                    delAndNext();
                });
                delAndNext();

                function delAndNext() {
                    var $operationColdel = $('.j-table-link-operation-column-items .j-del');
                    var $normalDel = $('.j-table-link-normal-items .j-del');
                    var $next = $('.j-table-link-operation-column-items .j-next');
                    $operationColdel.unbind();
                    $normalDel.unbind();
                    $next.unbind();
                    $('.j-table-link-set-plane-table').unbind();
                    $operationColdel.bind('click', function () {
                        var $this = $(this);
                        dialog.confirm('确定删除吗？', function () {
                            // 如果是新建
                            if ($this.attr('data-status')) {
                                $this.parent().remove();
                            }
                            else {
                                var id = $this.parent().find('input').attr('data-value');
                                that.delOperationColumn(id, $this.parent());
                            }
                        });
                    });
                    $normalDel.bind('click', function () {
                        var $this = $(this);
                        dialog.confirm('确定清除关联关系吗？', function () {
                            var id = $this.parent().find('label').attr('data-value');
                            that.delNormalColumn(id, $this);

                        });
                    });
                    $next.bind('click', function () {
                        that.saveColumnTableRelation($(this));
                    });
                    $('.j-table-link-set-plane-table').change(function (event) {
                        that.showSetParamBtn(event);
                    });
                }
            },

            /**
             * 显示设置参数按钮
             * @param {event} event 事件
             *
             * @public
             */
            showSetParamBtn: function (event) {
                var $target = $(event.target);
                if (!$target.val()) {
                    $target.next().hide();
                }
                else {
                    $target.next().show();
                }
            },

            /**
             * 显示参数设置
             * @param {Object} param 参数关系
             *
             * @public
             */
            showParamSetting: function (param) {
                var that = this;
                that.model.getParamSetList(param, function (data) {
                    $('.j-table-link-set-param-items').html(
                        LinkSettingParamTemplate.render(data)
                    );
//                    $('.j-param-set-add').unbind();
//                    $('.j-param-set-add').bind('click', function () {
//                        $(this).before(
//                            LinkParamSettingAddTemplate.render(data)
//                        );
//                    });
                });
            },

            /**
             * 保存列跳转平面表设置
             * @param {Object} target 触发元素
             *
             * @public
             */
            saveColumnTableRelation: function (target) {
                var that = this;
                var curParam = {};
                var data = [];
                var items = $('.j-table-link-set-column-table .table-link-set-item');
                var isSubmit = true;
                items.each(function () {
                    var $this = $(this);
                    var $label = $this.find('label');
                    var id = $label.attr('data-value');
                    var text = $label.attr('data-text');
                    var value = $this.find('select').val();
                    data.push({
                        id: id,
                        selectedTable: value,
                        text: text
                    });
                });
                items = $('.j-table-link-operation-column-items .table-link-set-item');
                var repeatArry = [];
                items.each(function () {
                    var $this = $(this);
                    var $input = $this.find('input');
                    var id = $input.attr('data-value') || null;
                    var text = $input.val().trim();

                    if (text === '') {
                        dialog.alert('请输入正确的名字');
                        isSubmit = false;
                        return isSubmit;
                    }
                    // 如果名称重复，
                    if ($.isInArray(text, repeatArry)) {
                        dialog.alert('操作列名字不能重复');
                        isSubmit = false;
                        return isSubmit;
                    }
                    repeatArry.push(text);
                    var value = $this.find('select').val();
                    data.push({
                        id: id,
                        selectedTable: value,
                        text: text
                    });
                });
                if (!isSubmit) {
                    return;
                }
                curParam.linkInfo = JSON.stringify(data);
                var nextParam = {};
                nextParam.planeTableId = target.prev('select').val();
                nextParam.olapElementId = target.prev().prev().attr('data-value');
                that.olapElementId = nextParam.olapElementId;
                that.model.saveColumnTableRelation(curParam, function () {
                    $('.j-table-link-set-first').hide();
                    $('.j-table-link-set-param-table').show();
                    that.showParamSetting(nextParam);
                });
            },

            /**
             * 删除操作列
             * @param {string} operationColId 操作列id
             * @param {$HTMLElement} $delEl 待删除操作列
             *
             * @public
             *
             */
            delOperationColumn: function (operationColId, $delEl) {
                var that = this;
                that.model.delOperationColumn(operationColId, function () {
                    $delEl && $delEl.remove();
                });
            },

            /**
             * 删除关联关系
             * @param {string} id 指标id
             * @param {$HTMLElement} $el 操作按钮元素
             *
             * @public
             *
             */
            delNormalColumn: function (id, $el) {
                var that = this;
                that.model.delOperationColumn(id, function () {
                    $el.siblings('select').val('');
                    $el.siblings('span').hide();
                });
            },

            /**
             * 保存参数设置
             *
             * @public
             */
            saveParamRelation: function () {
                var that = this;
                var items = $('.j-table-link-set-param-table .table-link-set-item');
                var data = [];
                var param = {};
                items.each(function () {
                    var $this = $(this);
                    var paramName = $this.find('label').attr('data-value');
                    var selectedDim = $this.find('select').val();
                    data.push({
                        paramName: paramName,
                        selectedDim: selectedDim
                    });
                });
                param.mappingInfo = JSON.stringify(data);
                param.olapElementId = that.olapElementId;
                that.model.saveParamRelation(param, function () {
                    that.$dialog.dialog('close');
                    var canvas = window.dataInsight.main.canvas;
                    canvas.showReport();
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
            _openColumnLinkPlaneDialog: function(data) {
                var that = this,
                    html;

                if ($.isEmptyObject(data.columnDefine)) {
                    dialog.alert('没有指标');
                    return;
                }

                html = LinkSettingTemplate.render(data);
                that.$dialog = dialog.showDialog({
                    title: '跳转设置',
                    content: html,
                    dialog: {
                        width: 440,
                        height: 450,
                        resizable: false
                    }
                });

            }

        });

        return View;
    });