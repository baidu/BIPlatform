/**
 * @file: 固定报表 View
 * @author: lizhantong
 * @depend:
 * @date: 2015-08-03
 */

define(
    [
        'dialog',
        'report/global-menu-btns/fix-report/fix-report-model',
        'report/global-menu-btns/fix-report/fix-report-template',
        'report/global-menu-btns/fix-report/fix-report-set-template',
        'report/global-menu-btns/fix-report/fix-report-task-list-template',
        'common/select-tree/select-tree',
        'url'
    ],
    function (
        dialog,
        FixReportModel,
        FixReportTemplate,
        FixReportSetTemplate,
        FixReportTaskListTemplate,
        SelectTree,
        Url
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
                'click .j-fix-report': 'getFixReportTaskMgrList'
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
                this.model = new FixReportModel({
                    canvasModel: option.canvasView.model,
                    reportId: option.reportId
                });
            },

            /**
             * 获取任务管理页面
             *
             * @param {event} event 点击事件
             *
             * @public
             */
            getFixReportTaskMgrList: function (event) {
                var savestate = window.dataInsight.main.canvas.savestate;
                if (savestate === 0) {
                    dialog.warning('您未进行保存，请先进行保存。');
                    return;
                }
                var that = this;

                // 如果画布中存在以下组件，就不能建立固定报表
                var componentType = [
                    'TABLE',
                    'CHART',
                    'PLANE_TABLE'
                ];
                var $components = $('.j-component-item').filter('.shell-component');
                var canFixReport = true;
                $components.each(function () {
                    var tComType = $(this).attr('data-component-type');
                    if (!$.isInArray(tComType, componentType)) {
                        canFixReport = false;
                        return;
                    }
                });
                if (!canFixReport) {
                    dialog.alert('固定报表目前只支持表格，平面表，图形组件！');
                    return;
                }

                that.model.getFixReportTaskMgrList(function (data) {
                    that.openFixReportDialog(data);
                });
            },

            /**
             * 打开固定报表设置弹出框
             *
             * @param {Object} data 固定报表任务管理列表信息
             * @public
             */
            openFixReportDialog: function (data) {
                var that = this;
                var html = FixReportTemplate.render(data);
                that.$dialog = dialog.showDialog({
                    title: '固定报表',
                    content: html,
                    dialog: {
                        width: 650,
                        height: 470,
                        resizable: false
                    }
                });
                that.bindEvent();
            },

            /**
             * 绑定确定取消事件
             *
             * @public
             */
            bindEvent: function () {
                var that = this;
                var $btnCancel = $('.j-operation-btns .j-cancel');
                var $btnOk = $('.j-operation-btns .j-ok');

                that.bindTaskListEvent();

                // 新增任务－确定
                $btnOk.unbind();
                $btnOk.click(function () {
                    var executeStrategy;
                    var taskId = $('.j-fix-report-mgr .j-task-id').attr('task-id');
                    var taskName = $('.j-fix-report-mgr .j-task-id').val();
                    taskName && (taskName = taskName.trim());
                    if (!taskName) {
                        dialog.alert('请输入合法的任务名称！');
                        return;
                    }
                    var isRunNow = $('.j-fix-report-mgr .j-isRunNow').is(':checked');
                    if (!isRunNow) {
                        // TODO:校验
                        var hour = $('.j-fix-report-mgr .j-time-hour').val().trim();
                        var numHour;
                        var numMinute;
                        if (hour) {
                            numHour = Number(hour);
                            if (numHour > 24 || numHour < 0) {
                                dialog.alert('请输入合法的时间');
                                return;
                            }
                        }
                        else {
                            dialog.alert('小时不能为空');
                            return;
                        }

                        var minute = $('.j-fix-report-mgr .j-time-minute').val().trim();
                        if (minute) {
                            numMinute = Number(minute);
                            if (numMinute > 60 || numMinute < 0) {
                                dialog.alert('请输入合法的时间');
                                return;
                            }
                        }
                        else {
                            dialog.alert('分钟不能为空');
                            return;
                        }
                        var granularity = $('.j-fix-report-mgr .j-granularity-parent').val();
                        var detail = $('.j-fix-report-mgr .j-granularity-child').val();
                        executeStrategy = {
                            hour: hour,
                            minute: minute,
                            granularity: granularity,
                            detail: detail
                        };
                    }

                    var params = [];
                    var paramTrees = $('.j-param-tree');
                    paramTrees.each(function () {
                        var paramId = $(this).attr('data-param-id');
                        var paramName = $(this).attr('data-param-name');
                        var paramSelectIds = $(this).attr('data-tree-select-id');
                        var paramSelectNames = $(this).attr('data-tree-select-name');
                        var caption = $(this).attr('data-param-caption');
                        params.push({
                            paramId: paramId,
                            paramName: paramName,
                            caption: caption,
                            paramValue: {
                                id: paramSelectIds,
                                name: paramSelectNames
                            }
                        });
                    });

                    var formData = {
                        taskId: taskId,
                        taskName: taskName,
                        params: JSON.stringify(params),
                        isRunNow: isRunNow
                    };
                    !isRunNow && (formData.executeStrategy = JSON.stringify(executeStrategy));

                    that.saveTaskInfo(formData);
                });

                // 新增任务－取消
                $btnCancel.unbind();
                $btnCancel.click(function () {
                    that.$dialog.dialog('close');
                });

            },

            /**
             * 绑定任务管理页面中任务相关操作的事件
             *
             * @public
             */
            bindTaskListEvent: function () {
                var that = this;
                var $taskStart = $('.j-fix-report-mgr .j-task-start');
                var $taskDel = $('.j-fix-report-mgr .j-task-del');
                var $taskAdd = $('.j-fix-report-mgr .j-task-add');
                var $btnLook = $('.j-fix-report-mgr .j-task-look');
                var $taskUrl = $('.j-fix-report-mgr .j-task-url');

                // 任务开始
                $taskStart.unbind();
                $taskStart.click(function () {
                    var $this = $(this);
                    if ($this.hasClass('j-task-start')) {
                        var taskId = $this.parents('.task-item').attr('task-id');
                        $this.removeClass('j-task-start biplt-start')
                            .addClass('j-task-stop biplt-stop');
                        that.startTask(taskId, 0);
                    }
                    else {
                        return;
                    }
                });

                // 任务删除
                $taskDel.unbind();
                $taskDel.click(function () {
                    var $this = $(this);
                    var taskId = $this.parents('.task-item').attr('task-id');

                    dialog.confirm('是否确定删除当前任务', function () {
                        that.delTask(taskId);
                    });
                });

                // 新增任务
                $taskAdd.unbind();
                $taskAdd.click(function () {
                    that.addTask();

                });

                // 查看任务
                $btnLook.unbind();
                $btnLook.click(function () {
                    var $this = $(this);
                    var taskId = $this.parents('.task-item').attr('task-id');
                    that.getTaskInfo(taskId);
                });

                $taskUrl.unbind();
                $taskUrl.click(function () {
                    var $this = $(this);
                    var url = '<div class="word-bread w-300">' + $this.html() + '</div>';
                    dialog.alert(url);
                });
            },

            /**
             * 启动任务
             *
             * @param {string} taskId 任务Id
             * @param {string} taskStatus 任务状态
             * @public
             */
            startTask: function (taskId, taskStatus) {
                var that = this;
                that.model.startTask(taskId, taskStatus, function (data) {
                    var $div = $('[task-id="' + taskId + '"] td:eq(1) div');
                    $div.attr('title', data).html(data);
                });
            },

            /**
             * 删除任务
             *
             * @param {string} taskId 任务Id
             * @public
             */
            delTask: function (taskId) {
                var that = this;
                that.model.delTask(taskId, function () {
                    $('[task-id="' + taskId + '"]').remove();
                });
            },

            /**
             * 保存任务设置信息
             *
             * @param {Object} formData 任务设置信息
             * @public
             */
            saveTaskInfo: function (formData) {
                var that = this;
                that.model.saveTaskInfo(formData, function () {

                    that.model.getFixReportTaskMgrList(function (data) {
                        var html = FixReportTaskListTemplate.render(data);
                        $('.j-fix-report-mgr .j-fix-report-content').html(html);
                        $('.j-fix-report-mgr .j-operation-btns').hide();
                        that.bindTaskListEvent();
                    });
                });
            },

            /**
             * 获取任务设置信息
             *
             * @param {string} taskId 任务Id
             * @public
             */
            getTaskInfo: function (taskId) {
                var that = this;

                that.model.getTaskInfo(taskId, function (data) {
                    $('.j-fix-report-content').html(
                        FixReportSetTemplate.render(data)
                    );
                    $('.j-operation-btns').show();

                    that.taskMgrEvent(taskId);

                });
            },

            /**
             * 新增任务
             *
             * @public
             */
            addTask: function () {
                var that = this;
                var taskInfo = {
                    taskName: '',
                    isRunNow: false,
                    executeStrategy: {
                        hour: '',
                        minute: '',
                        granularity: 'D'
                    }
                };

                that.model.getParamData(null, function (data) {

                    $('.j-fix-report-content').html(
                        FixReportSetTemplate.render(data)
                    );
                    $('.j-operation-btns').show();

                    that.taskMgrEvent();
                }, taskInfo);
            },

            /**
             * 绑定任务设定里面的事件
             * @param {string} taskId 任务id
             *
             * @public
             */
            taskMgrEvent: function (taskId) {
                var that = this;
                var reportId = that.model.get('reportId');
                var $isRunNow = $('.j-fix-report-mgr .j-isRunNow');
                $isRunNow.click(function () {
                    if ($(this).is(':checked')) {
                        $('.j-isRunNow-set').hide();
                    }
                    else {
                        $('.j-isRunNow-set').show();
                    }
                });
                $('.j-granularity-parent').change(function () {
                    var val = $(this).val();
                    if (val === 'D') {
                        $('.j-granularity-child').html('').hide();
                        return;
                    }
                    var granularityChild = that.model.get('granularityList').child;
                    var html = [];
                    granularityChild = granularityChild[val];
                    for (var key in granularityChild) {
                        html.push('<option value="', key, '">', granularityChild[key], '</option>');
                    }
                    $('.j-granularity-child').html(html.join(''));
                    $('.j-granularity-child').show();
                });
                var el = $('.j-fix-report-mgr .j-param-tree');
                el.each(function () {
                    var elThis = this;
                    var paramId = $(elThis).attr('data-param-id');
                    var url = Url.getFixReportMgrTree(reportId, paramId);
                    var treeOption = {
                        el: $(elThis)[0],
                        async: {
                            enable: true,
                            url: url,
                            autoParam: ['id', 'name']
                            // autoParam:['id', 'name=n', "level=lv"]
                            // otherParam:{"otherParam":"zTreeAsyncTest"}
                        }
                    };

                    if (taskId) {
                        treeOption.async.otherParam = {
                            taskId: taskId
                        };
                    }
                    that['tree-' + paramId] = new SelectTree(treeOption);
                });
                var $hour = $('.j-fix-report-mgr .j-time-hour');
                var $minute = $('.j-fix-report-mgr .j-time-minute');
                $hour.unbind();
                $hour.keyup(function (event) {
                    var $input = $(event.target);
                    var val = $input.val().trim();
                    // 如果输入不合法，返回
                    if (!(/^\d+$/.test(val))) {
                        $input.val('');
                        return;
                    }
                });
                $minute.unbind();
                $minute.keyup(function (event) {
                    var $input = $(event.target);
                    var val = $input.val().trim();
                    // 如果输入不合法，返回
                    if (!(/^\d+$/.test(val))) {
                        $input.val('');
                        return;
                    }
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
            }
        });
    }
);