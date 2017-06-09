/**
 * @file
 * @author 赵晓强(longze_xq@163.com)
 * @date 2014-8-5
 */
define([
        'template',
        'dialog',
        'report/edit/canvas/canvas-model',
        'report/report-view',
        'report/component-box/main-view',
        'report/edit/canvas/edit-comp-view',
        'report/edit/canvas/edit-btns-template',
        'report/edit/canvas/guides-template',
        'report/global-menu-btns/main-view'
    ],
    function (
        template,
        dialog,
        MainModel,
        ReportView,
        ComponentBoxView,
        EditCompView,
        editBtnsTemplate,
        guidesTemplate,
        GlobalMenuView
    ) {

        return Backbone.View.extend({
            events: {
                'click .j-con-edit-btns .j-setting': 'initCompConfigBar',
                'click .j-con-edit-btns .j-delete': 'deleteComp',
                'click .j-button-save-report': 'saveReport',
                'click .j-button-close-report': 'closeReport',
                'click .j-button-publish-report': 'publishReport',
                'click .j-button-preview-report': 'previewReport',
                'click .j-comp-div': 'focusText',
                'blur .j-comp-text': 'blurText',
                'keydown .j-comp-text': 'keyDownText'
            },
            /* 判断是否保存的变量 */
            savestate: 0,
            /**
             * 构造函数
             *
             * @param {$HTMLElement} option.el .j-canvas
             * @param {string} option.id 报表id
             * @constructor
             */
            initialize: function (option) {
                var that = this;

                // 初始化工具栏菜单模块
                that.globalMenuView = new GlobalMenuView({
                    el: that.el,
                    reportId: that.id,
                    canvasView: that
                });
                // 初始化工具箱
                that.compBoxView = new ComponentBoxView({
                    el: that.el,
                    id: that.id,
                    canvasView: that
                });

                this.model = new MainModel({
                    id: that.id,
                    parentModel: option.parentView.model,
                    compBoxModel: that.compBoxView.model
                });
                that.parentView = option.parentView;

                // 初始化组件设置模块
                that.editCompView = new EditCompView({
                    el: that.el,
                    reportId: that.id,
                    canvasView: that
                });

                // 初始化报表公共功能模块
                that.reportView = new ReportView({id: that.id});

                that.model.getFixReportTaskMgrList(that.id, function (data) {
                    that.isFixReport = data;
                    // 初始化json与vm文件
                    that.model.initJson(function () {
                        that.model.initVm(function () {
                            that.showReport();
                        });
                    });
                });
            },

            /**
             * 初始化画布可接受组件
             *
             * @public
             */
            initAcceptComp: function () {
                var that = this;
                var compBoxModel = that.compBoxView.model;
                var acceptAbleClass = 'active';
                var acceptDisableClass = 'disable';
                var entityDefs = that.model.reportJson.entityDefs;

                // 定义可接收从组件箱拖出的东西
                $('.j-report', that.el).droppable({
                    accept: ".j-con-component-box .j-component-item",
                    // 焦点在里面才算里面
                    // intersect 50%在里面才算在里面(默认)
                    // touch 有接触
                    // pointer 鼠标坐标
                    // fit 全部在里面
                    tolerance: 'intersect',
                    drop: function (event, ui) {
                        var $report = $(this);
                        var compType = ui.helper.attr('data-component-type');
                        if (compType === 'H_BUTTON' || compType === 'REPORT_SAVE_COMP') {
                            var comT = (compType === 'REPORT_SAVE_COMP') ? 'DI_REPORTSAVE' : compType;
                            if (isHaveConfirmEntity(comT, entityDefs)) {
                                dialog.alert('当前组件只能拖一个');
                                return;
                            }
                        }
                        if (that.isFixReport) {
                            var componentType = [
                                'TABLE',
                                'CHART',
                                'PLANE_TABLE'
                            ];
                            if (!$.isInArray(compType, componentType)) {
                                dialog.alert('固定报表目前只支持表格，平面表，图形组件！');
                                return;
                            }
                        }
                        // TODO:修改reportJson中提交方式
                        var $realComp = ui.helper.clone().html('<div class="ta-c">组件占位，配置数据后展示组件</div>');
                        var compData = compBoxModel.getComponentData(compType);
                        $realComp.removeClass(compData.iconClass + ' active');
                        $realComp.addClass(compData.renderClass);
                        $realComp.css({ 'cursor': 'auto' });
                        $report.removeClass('active');
                        var defaultWidth = $realComp.attr('data-default-width');
                        $realComp.css({
                            width: defaultWidth + 'px',
                            height: $realComp.attr('data-default-height') + 'px'
                        });

                        // 越界拉回
                        var leftPosition = parseInt($realComp.css('left'));
                        var reportWidth = $report.width();
                        if (leftPosition / 1 + defaultWidth / 1 > reportWidth) {
                            $realComp.css('left', (reportWidth - defaultWidth - 3) + 'px');
                        }
                        if (parseInt($realComp.css('left')) < 2) {
                            $realComp.css('left', '3px');
                        }
                        if (parseInt($realComp.css('top')) < 2) {
                            $realComp.css('top', '3px');
                        }
                        that.addComp(compData, compType, $realComp);
                    },
                    helper: "clone",
                    out: function (event, ui) {
                        $(this).removeClass(acceptAbleClass);
                        ui.helper.removeClass(acceptAbleClass).addClass(acceptDisableClass);
                        ui.helper.html('<div class="ta-c">已超出画布区</div>');
                    },
                    over: function (event, ui) {
                        $(this).addClass(acceptAbleClass);
                        ui.helper.removeClass(acceptDisableClass).addClass(acceptAbleClass);
                        ui.helper.html('<div class="ta-c">组件占位，配置数据后展示组件</div>');
                    }
                });
                // 判断json中是否已经有查询按钮
                function isHaveConfirmEntity (clzKey, entitys) {
                    var result = false;
                    for (var i = 0; i < entitys.length; i++) {
                        if (
                            entitys[i].clzKey === clzKey
                            && (
                                (entitys[i].dataOpt && entitys[i].dataOpt.text === '查询')
                                || clzKey === 'DI_REPORTSAVE'
                            )

                        ) {
                            result = true;
                        }
                    }
                    return result;
                }
            },

            /**
             * 文本框组件点击切换
             *
             * @public
             */
            focusText: function (event) {
                var divTitle = '点击进行输入';
                var $now = $(event.target);
                var $divTextBox = $now.parent();
                var $divText = $now;
                var $inpText = $divTextBox.next();
                var divHtml = $divText.html();
                $divTextBox.hide();
                $inpText.show().focus();
                if (divHtml != divTitle) {
                    $inpText.val($divText.html());
                }
                else {
                    $inpText.val('');
                }
            },

            /**
             * 文本框组件失去焦点切换
             *
             * @public
             */
            blurText: function (event) {
                var divtext = $(event.target).val();
                var that = this;
                that.saveBtnsText(event, divtext);
            },

            keyDownText: function (event) {
                var divtext = $(event.target).val();
                var that = this;
                if (event.keyCode == 13) {
                    that.saveBtnsText(event, divtext);
                }
            },

            /**
             * 文本框组件失去焦点切换
             *
             * @public
             */
            saveBtnsText: function (event, content) {
                var divTitle = '点击进行输入';
                var $now = $(event.target);
                var $divTextBox = $now.prev();
                var $divText = $divTextBox.find('div');
                var textId = $divText.attr('id');
                $now.hide();
                $divTextBox.show();
                if (content != '') {
                    $divText.html(content);
                }
                else {
                    $divText.html(divTitle);
                }
                this.model.dateCompPositing(textId, content);
            },

            /**
             * 添加一个组件(提交后台获取id，并在vm与json中添加相关数据)
             *
             * @param {Object} compData 组件的配置数据
             * @param {string} compType 组件类型
             * @param {$HTMLElement} $realComp 被拖到画布上的组件的$DOM对象
             * @public
             */
            addComp: function (compData, compType, $realComp) {
                var that = this;
                that.model.addComp(
                    compData,
                    compType,
                    // 创建组件的外壳
                    function (dataCompId, reportCompId) {
                        $realComp.attr('data-comp-id', dataCompId);
                        $realComp.attr('report-comp-id', reportCompId);
                        return $realComp.clone();
                    },
                    function () {
                        var compType = $realComp.attr('data-component-type');
                        that.$el.find('[data-o_o-di="snpt"]').append($realComp);
                        that.initDrag($realComp);
                        that.initResize($realComp);
                        that.addEditBtns($realComp);
                        that.removeGuides($realComp);
                        that.addGuides($realComp);
                        $realComp.find('.j-con-edit-btns').css({
                            'width': 'auto',
                            'height': 'auto'
                        });
                        //.find('.j-fold').html('－');
                        if (compType === 'TEXT' || compType === 'H_BUTTON' || compType === 'REPORT_SAVE_COMP') {
                            that.showReport(true);
                            if (compType === 'REPORT_SAVE_COMP') {
                                dialog.alert('报表保存在设计端不能使用');
                            }
                        }

                    }
                );
            },

            /**
             * 从报表中删除一个组件
             *
             * @param {event} event 点击删除按钮触发的事件
             * @public
             */
            deleteComp: function (event) {
                var that = this;
                var $target = $(event.target);
                var $comp = $target.parents('.j-component-item');
                var compId = $comp.attr('data-comp-id');
                var reportCompId = $comp.attr('report-comp-id');
                var compType = $comp.attr('data-component-type');
                this.model.deleteComp(compId, reportCompId, compType, function () {
                    $comp.remove();
                    that.editCompView.hideEditBar();
                    // 刷新报表展示
                    that.showReport();
                });
            },

            /**
             * 初始化组件可被拖拽调整位置
             *
             * @param {$HTMLElement} $component 组件的$DOM对象（可以是多个或一个）
             * @public
             */
            initDrag: function ($component) {
                var that = this;

                $component.draggable({
                    helper: "original",
                    scroll: true,
                    scrollSensitivity: 100,
                    containment: this.$el.find('.j-report'),
                    opacity: 0.8, // 被拖拽元素的透明度
                    handle: ".j-drag", // 拖拽触发点
                    start: function (event, ui) {
                        ui.helper.attr('data-sort-startScrrolTop', ui.helper.parent().scrollTop());
                    },
                    stop: function (event, ui) {
                        //that.removeGuides(ui.helper);
                        that.updateCompPositing(ui.helper);
                        that.initSnptHeight();
                    }
                });
            },

            /**
             * 初始化组件可被调整大小
             *
             * @param {$HTMLElement} $component 组件的$DOM对象（可以是多个或一个）
             * @public
             */
            initResize: function ($component) {
                var that = this;

                $component.resizable({
                    stop: function (event, resizeObj) {
                        var paramObj = resizeObj.size;
                        paramObj.compId = $(this).attr('data-comp-id');
                        that.model.resizeComp(paramObj);
                        that.showReport(true);
                    }
                });
                // 去除左右和上线单独调整大小的相应区，有此区域会影响滚动条的呈现
                $component.find('.ui-resizable-e,.ui-resizable-s').remove();
                // 对表格组件设置拖拽的最小高度
                // 上下小零件的总高度94（=40+19+35），一行数据加表头的高度70
                $component.filter('[data-component-type="TABLE"]').resizable("option", "minHeight", 204);
                // 固定单选下拉框的高度
                that.dragWidthHeight($component, 'SELECT', 33, 33);
                // 固定多选下拉框的高度
                that.dragWidthHeight($component, 'MULTISELECT', 33, 33);

                that.dragWidthHeight($component, 'REPORT_SAVE_COMP', 33, 33);
                // 固定文本框的高度
                that.dragWidthHeight($component, 'TEXT', 33, 33);
                // 固定查询按钮的高度
                that.dragWidthHeight($component, 'H_BUTTON', 33, 33, 67);
                // 固定查询按钮的高度
                that.dragWidthHeight($component, 'TIME_COMP', 33, 33, 220);
                // 固定查询按钮的高度
                that.dragWidthHeight($component, 'SINGLE_DROP_DOWN_TREE', 33, 33, 210, 210);
                // 删除参考线-避免重复渲染产生多余的参考线
                that.removeGuides($component);
                // 调整后添加参考线
                that.addGuides($component);
            },

            /**
             * 设置组件最大最小高度
             *
             * @param {$HTMLElement} $ele 组件外壳
             * @param {string} type 组件类型
             * @param {number} minHeight 组件拖拽最小高度
             * @param {number} maxHeight 组件拖拽最大高度
             * @param {number} minWidth 组件拖拽最小宽度
             * @param {number} maxWidth 组件拖拽最大宽度
             * @public
             */
            dragWidthHeight: function ($ele, type, minHeight, maxHeight, minWidth, maxWidth) {
                $ele.filter('[data-component-type="' + type + '"]').resizable("option", "minHeight", minHeight);
                $ele.filter('[data-component-type="' + type + '"]').resizable("option", "maxHeight", maxHeight);
                $ele.filter('[data-component-type="' + type + '"]').resizable("option", "minWidth", minWidth);
                $ele.filter('[data-component-type="' + type + '"]').resizable("option", "maxWidth", maxWidth);

            },

            /**
             * 组件拖动后，更新vm中组件的位置信息
             *
             * @param {$HTMLElement} ui 组件外壳
             * @public
             */
            updateCompPositing: function(ui) {
                var compId = ui.attr('data-comp-id');
                var left = ui.css('left');
                var top = ui.css('top');

                this.model.updateCompPositing(compId, left, top);
            },

            /**
             * 添加组件拖拽参考线
             *
             * @param {$HTMLElement} $component 组件的外壳
             * @public
             */
            addGuides: function ($component) {
                $component.append(guidesTemplate.render());
            },

            /**
             * 移除组件拖拽参考线
             *
             * @param {$HTMLElement} $component 组件的外壳
             * @public
             */
            removeGuides: function ($component) {
                $component.find('.j-guide-line').remove();
            },

            /**
             * 添加编辑组件操作条
             *
             * @param {$HTMLElement} $component 组件的外壳
             * @public
             */
            addEditBtns: function ($component) {
                $component.find('.con-edit-btns').remove();
                $component.append(editBtnsTemplate.render());
                $component.find('.comp-box').css('margin-top', 0);
                // 文本框编辑数据及关联隐藏
                for (var i = 0; i < $component.length; i ++) {
                    var compType = $($component[i]).attr('data-component-type');
                    if (
                        compType === 'TEXT'
                        || compType === 'H_BUTTON'
                        || compType === 'REPORT_SAVE_COMP'
                    ) {
                        $($component[i]).find('.j-setting').remove();
                    }
                }
                /**
                 $component.find('.j-fold').click(function () {
                    var $conBtn = $(this).parent();
                    if ($conBtn.width() < 20) {
                        $conBtn.width('auto');
                        $conBtn.height('auto');
                        $(this).html('－');
                    }
                    else {
                        $conBtn.width(1);
                        $conBtn.height(1);
                        $(this).html('+');
                    }
                });
                 **/
            },

            /**
             * 保存报表
             *
             * @public
             */
            saveReport: function () {
                var that = this;
                var nowReport = this.$el.find('.reportName').text();
                this.model.saveReport(nowReport, function () {
                    dialog.success('报表保存成功。');
                }, openDataFormatDialog);
                this.savestate = 1;
                function openDataFormatDialog(title, nowReport) {
                    var html;
                    var reportId = window.dataInsight.main.id;
                    html =
                        '<div class="save-reportNameBox">'
                        + '<div class="save-reportName">' + title + '</div>'
                        + '<input type="text" class="save-reportSetName" value="' + nowReport + '"/></div>';
                    dialog.showDialog({
                        content: html,
                        title: '保存提示',
                        dialog: {
                            height: 200,
                            width: 300,
                            open: function () {
                            },
                            buttons: {
                                '确认': function () {
                                    // TODO:此逻辑应写在MODEL
                                    $.ajax({
                                        type: "POST",
                                        dataType: "json",
                                        cache: false,
                                        timeout: 10000,
                                        url: "reports/" + reportId+ "/name/" + $(".save-reportSetName").val(),
                                        success: function(data){
                                            // 根据返回值进行判断
                                            if (data["status"] === 0) {
                                                dialog.success(data["statusInfo"]);
                                            }
                                            else {
                                                dialog.error(data["statusInfo"]);
                                            }
                                        }
                                    });
                                    $(this).dialog('close');
                                },
                                '取消': function () {
                                    $(this).dialog('close');
                                }
                            }
                        }
                    });
                }
            },

            /**
             * 关闭报表
             *
             * @public
             */
            closeReport: function () {
                if (this.savestate == 0) {
                    dialog.warning('您未进行保存，请保存后关闭。');
                }
                else {
                    this._destroyPanel();
                    require(['report/list/main-view'], function (ReportListView) {
                        new ReportListView({el: $('.j-main')});
                    });
                }
            },

            /**
             * 调用面板模块销毁方法
             * @private
             */
            _destroyPanel: function () {
                window.dataInsight && window.dataInsight.main
                && window.dataInsight.main.destroy();
            },

            /**
             * 发布报表
             *
             * @public
             */
            publishReport: function () {
                if (this.savestate == 0) {
                    dialog.warning('您未进行保存，请保存后发布。');
                }
                else {
                    this.reportView.publishReport('POST');
                }
            },

            /**
             * 预览报表
             *
             * @public
             */
            previewReport: function () {
                this.reportView.previewReport('POST');
            },

            /**
             * 初始化组件配置区
             *
             * @param {event} event 点击事件（报表组件上的编辑按钮）
             * @public
             */
            initCompConfigBar: function (event) {
                this.editCompView.initCompConfigBar(event);
            },

            /**
             * 初始化报表的高度(计算报表的高度)
             *
             * @public
             */
            initSnptHeight: function () {
                var $snpt = this.$el.find('[data-o_o-di="snpt"]');
                var addHeight = 200; // 组件高度之外的附加高度，为了供调整大小方便
                var $compItems = this.$el.find('.report .j-component-item');

                var height = 0;
                $compItems.each(function (item) {
                    var $this = $(this);
                    var bottomPosition = $this.height() + parseInt($this.css('top'));
                    height = bottomPosition > height ? bottomPosition: height;
                });
                $snpt.height(height + addHeight);
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
                if (this._component) {
                    this._component.dispose();
                }
            },

            /**
             * 渲染报表
             *
             * @public
             */
            showReport: function () {
                var that = this;

                if (that.model.reportJson.entityDefs.length < 2) {
                    var html = that.model.$reportVm.prop('outerHTML');
                    that.$el.find('.j-report').html(html);
                    that.initAcceptComp();
                    return ;
                }
                var options = {
                    parentEl: that.$el.find('.j-report')[0],
                    reportId: that.id,
                    rptHtml: that.model.$reportVm.prop('outerHTML'),
                    rptJson: $.extend(true, {}, that.model.reportJson)
                };
                for (var i = 0; i < options.rptJson.entityDefs.length; i++) {
                    if (options.rptJson.entityDefs[i].clzKey === 'DI_REPORTSAVE') {
                        options.rptJson.entityDefs[i].isInDesigner = true;
                    }
                }
                that._firstShowReport = false;
                if (that._component === undefined) {
                    require(
                        ['report/component-combination/enter'],
                        function (component) {
                            that._component = component;
                            component.start(options);
                        }
                    );
                }
                else {
                    that._component.dispose();
                    that._component.start(options);
                }

                // 没有回调的支持用延时函数做简单的hack
                window.setTimeout(function () {
                    var $report = that.$el.find('.j-report');
                    var $ComponentItem = $report.find('.j-component-item');
                    that.initDrag($ComponentItem);
                    that.initResize($ComponentItem);
                    that.addEditBtns($ComponentItem);
                    that.initAcceptComp();
                    that.editCompView.activeComp();
                    that.initSnptHeight();
                }, 2000);
            }
        });
    }
);