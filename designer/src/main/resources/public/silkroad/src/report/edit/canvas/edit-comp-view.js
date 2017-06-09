/**
 * @file 编辑某一组件的相关操作，包括：
 *       删除某一组件
 *       打开组件配置器
 *       接收数据项
 * @author 赵晓强(longze_xq@163.com)
 * @date 2014-8-14
 */
define([
        'template',
        'dialog',
        'constant',
        'report/edit/canvas/edit-comp-model',
        'report/edit/canvas/chart-setting/chart-setting-view',
        'report/edit/canvas/table-setting/table-setting-view',
        'report/edit/canvas/plane-table-setting/plane-table-setting-view',
        'report/edit/canvas/olap-table-setting/olap-table-setting-view',
        // html模板区域
        'report/edit/canvas/comp-setting-default-template',
        'report/edit/canvas/comp-setting-time-template',
        'report/edit/canvas/comp-setting-liteolap-template',
        'report/edit/canvas/comp-setting-chart-template',
        'report/edit/canvas/comp-setting-caselect-template',
        'report/edit/canvas/vui-setting-select-template',
        'report/edit/canvas/ecui-input-tree-setting-template',
        'report/edit/canvas/default-selected-time-setting-template',
        'report/edit/canvas/default-selected-range-time-setting-template',
        'report/edit/canvas/data-format-setting-template',
        'report/edit/canvas/comp-relation-event-setting-template',
        'report/edit/canvas/plane-table-setting/vui-setting-plane-table-template',
        'common/float-window',
        'report/edit/canvas/chart-icon-list-template',
        'report/edit/canvas/norm-info-depict-template'
    ],
    function (
        template,
        dialog,
        Constant,
        EditCompModel,
        ChartSettingView,
        TableSettingView,
        PlaneTableSettingView,
        OlapTableSettingView,
        // html模板区域
        compSettingDefaultTemplate,
        compSettingTimeTemplate,
        compSettingLITEOLAPTemplate,
        compSettingChartTemplate,
        compCascadeSelectTemplate,
        vuiSettingSelectTemplate,
        ecuiInputTreeSettingTemplate,
        defaultSelectedTimeSettingTemplate,
        defaultSelectedRangeTimeSettingTemplate,
        dataFormatSettingTemplate,
        compRelationEventSettingTemplate,
        vuiSettingPlaneTableTemplate,
        FloatWindow,
        indMenuTemplate,
        normInfoDepictTemplate
    ) {

        return Backbone.View.extend({
            events: {
                'click .j-comp-setting .j-delete': 'deleteCompAxis',
                'click .j-report': 'removeCompEditBar',
                'click .j-set-default-time': 'openTimeSettingDialog',
                'click .j-set-data-format': 'getDataFormatList',
                'click .j-set-relation': 'setCompRelationEvent',
                'click .j-norm-info-depict': 'getNormInfoDepict',
                'click .item .j-icon-chart': 'showChartList',
                'change .select-type': 'selectTypeChange',
                'change .select-calendar-type': 'selectCalendarTypeChange',
                // 'click .j-others-operate': 'getFilterBlankLine',
                'change .j-select-setAll': 'changeSelectAll'
            },

            //------------------------------------------
            // 设置组件关联关系
            //------------------------------------------
            /**
             * 设置单选下拉框默认值
             *
             * @param {event} event 事件焦点（单选下拉框多选框）
             * @public
             */
            changeSelectAll: function (event) {
                var that = this;
                var $target = $(event.target);
                var compId = $target.attr('data-comp-id');
                var checked = $target[0].checked;
                var allName = $target.attr('value');

                // 设置单选下拉框默认值
                that.selectSetAll(checked, allName, compId);
            },
            /**
             * 设置组件关联关系
             *
             * @param {event} event 点击事件（报表组件上的 关联 按钮）
             * @public
             */
            setCompRelationEvent: function (event) {
                var that = this;
                // 获取组件本身的属性信息
                // 当前编辑组件的 组件id（组件在silkroad端主要使用的id）
                var activeSilkroadCompId = that.getActiveCompId();
                // 当前编辑组件的 组件id（组件在report-ui端主要使用的id）
                var activeProductCompId = that.getActiveReportCompId();
                var reportJson = this.model.get('canvasModel').reportJson;
                var entityDefs = reportJson.entityDefs; // 组件实例数组
                var activeEntity = $.getTargetElement(activeSilkroadCompId, entityDefs);
                var activeCompRealtionIds = $.getEntityInteractionsId(activeEntity);
                var proportionW;
                var proportionH;
                that.model.getCompAxis(activeSilkroadCompId, openDialog);
                // 渲染缩略图
                function renderThumbnail() {
                    // 渲染缩略图
                    var canvasWidth = $('.j-report').width();
                    // TODO:此高度好像是有问题的
                    var canvasHeight = $('.di-o_o-body').height();
                    // 获取缩略比例
                    var sumbnailWidth = $('.comp-realtion-box').width();
                    var sumbnailHeight = $('.comp-realtion-box').height();
                    proportionW = sumbnailWidth / canvasWidth;
                    proportionH = sumbnailHeight / canvasHeight;
                    // 获取到所有组件
                    var compItems = $('.j-report').find('.j-component-item');
                    compItems.each(function () {
                        appendThumbnail($(this));
                    });
                    // 绑定缩略图里面checkbox事件，当点击后，改变当前选中状态
                    var $chks = $('input[name="comp-thumbnail"]');
                    $chks.unbind().click(function () {
                        if ($(this).attr('checked')) {
                            $(this).removeAttr('checked');
                        }
                        else {
                            $(this).attr('checked', 'checked');
                        }
                    });
                    var $eventOutParam = $('.j-comp-relation-event-out-param');
                    eventRelationChange($eventOutParam);
                    $eventOutParam.unbind().change(function () {
                        eventRelationChange($eventOutParam);
                    });
                }

                function eventRelationChange($eventOutParam) {
                    var val = $eventOutParam.val();
                    var dimGroup = $eventOutParam.find('option:selected').attr('dimGroup');
                    if (dimGroup === 'false') {
                        $('.span-level').hide();
                        $('.j-comp-relation-event-out-param-level').hide();
                    }
                    else {
                        $('.span-level').show();
                        $('.j-comp-relation-event-out-param-level').show();
                    }
                }

                // 打开弹出框
                function openDialog(data) {
                    if (!data.xAxis || !data.yAxis) {
                        dialog.alert('请先拖入指标和维度');
                        return;
                    }
                    if (data.xAxis.length < 0 || data.yAxis.length < 0) {
                        dialog.alert('请先拖入指标和维度');
                        return;
                    }
                    var htmlData = {};
                    htmlData.outParamDim = data.xAxis;
                    htmlData.selectDimId = activeEntity.outParam ? activeEntity.outParam.dimId : null;
                    htmlData.selectLevel = activeEntity.outParam ? activeEntity.outParam.level : null;
                    htmlData.selectDimName = activeEntity.outParam ? activeEntity.outParam.dimName : null;
                    var levelData = {
                        '1': '当前级',
                        '2': '下一级'
                    };
                    htmlData.outParamLevel = levelData;
                    var html;
                    html = compRelationEventSettingTemplate.render(htmlData);
                    dialog.showDialog({
                        title: '组件关联关系设置',
                        content: html,
                        dialog: {
                            width: 550,
                            height: 550,
                            resizable: false,
                            buttons: [
                                {
                                    text: '提交',
                                    click: function () {
                                        saveCompRelation($(this));
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
                    renderThumbnail();
                }

                /**
                 * 保存数据关联关系
                 * 不管被关联组件原来是否存在关联关系，按照缩略图里面的选中状态重新进行关联
                 * 情况1：如果原来存在关联，缩略图里面并无修改，那么json中之前关联关系依旧
                 * 情况2：如果原来存在关联关系，缩略图里面取消了此关系，那么json中取消关联关系
                 * 情况3：如果原来不存在关联关系，缩略图里面进行了关联，那么json中添加关联关系
                 */
                function saveCompRelation($dialog) {
                    // 获取到选中的待关联组件
                    var $chks = $('input[name="comp-thumbnail"]');
                    var compIdArry = [];
                    // 当前编辑组件是否与别的组件有关联的标识
                    var hasInteractions = false;
                    //var selCompIdArr = [];
                    $chks.each(function () {
                        var $this = $(this);
                        var idObj = {};
                        if ($this.attr('checked')) {
                            idObj.checked = true;
                        }
                        else {
                            idObj.checked = false;
                        }
                        idObj.id = $(this).val();
                        compIdArry.push(idObj);
                    });
                    
                    // 循环遍历组件，往reportJson中添加关联关系
                    for (var x = 0, xLen = compIdArry.length; x < xLen; x++) {
                        // 获取被关联组件的json配置信息
                        var curEntity = $.getTargetElement(compIdArry[x].id, entityDefs);
                        var intTemp = {
                            event: {
                                rid: activeProductCompId,
                                // TODO:对应修改
                                name: 'rowselect'
                            },
                            action: {
                                name: 'sync'
                            }
//                            action: {
//                                name: 'syncLiteOlapInds'
//                            },
//                            dataOpt: {
//                                needShowCalcInds: true,
//                                submitMode: 'IMMEDIATE',
//                                reportType: 'RTPL_OLAP_TABLE',
//                                datasourceId: {
//                                    SELECT: 'LIST_SELECT'
//                                }
//                            }
                        };
//                        curEntity.outParamDim = 'lzt';
//                        curEntity.outParamDimLevel = 'top';
                        // 如果存在事件关联
                        if (curEntity.interactions) {
                            // 判断当前实例中是否已经有对应事件关联
                            var hasRelation = $.hasRelation(activeProductCompId, curEntity);
                            if (hasRelation > -1) {
                                curEntity.interactions.splice(hasRelation, 1);
                            }
                            // 如果勾选，就添加关联关系
                            if (compIdArry[x].checked) {
                                curEntity.interactions.push(intTemp);
                                
                                // 只要设置关联时，不管被关联方是一个组件还是两个组件，只有有关联，hasInteractions就需要被设置为true。 update by majun 
                                hasInteractions = true;
                            }
                        }
                        // 如果不存在事件关联
                        else {
                            curEntity.interactions = [];
                            curEntity.interactions.push(intTemp);
                        }
                    }
                    if (!activeEntity.outParam) {
                        activeEntity.outParam = {};
                    }
                    var dimValue = $('.j-comp-relation-event-out-param').val().split('$');
                    activeEntity.outParam.dimId = dimValue[0];
                    activeEntity.outParam.dimName = dimValue[1];
                    activeEntity.outParam.level = $('.j-comp-relation-event-out-param-level').val();
                    
                    changeTableCheckType(activeEntity,hasInteractions);
                    // TODO:保存json，关闭窗口
                    // 保存vm与json，保存成功后展示报表
                    that.model.canvasModel.saveJsonVm(
                        function () {
                            $dialog.dialog('close');
                            that.canvasView.showReport.call(that.canvasView);
                        }
                    );
                }
                
                // 在设置表图关联时，改变表格行选中时的选中动作，目前只支持行单选，如果表格没有和别的组件关联，rowCheckMode直接置为SELECTONLY。 update by majun
                function changeTableCheckType(activeEntity,hasInteractions) {
                	if (activeEntity.vuiRef.mainTable) {
                		var vuiTableId = activeEntity.vuiRef.mainTable;
                		var tableVuiDef = $.getVuiTargetElement(vuiTableId, entityDefs);
                		if (tableVuiDef.dataOpt.rowCheckMode) {
                			tableVuiDef.dataOpt.rowCheckMode = 'SELECT';
                			if(!hasInteractions) {
                				tableVuiDef.dataOpt.rowCheckMode = 'SELECTONLY';
                			}
                		}
                	}
                }
                
                // 添加缩略图
                function appendThumbnail($this) {
                    var tW = $this.width();
                    var tH = $this.height();
                    var tL = parseInt($this.css('left'));
                    var tT = parseInt($this.css('top'));
                    var nW = tW * proportionW;
                    var nH = tH * proportionH;
                    var nT = tT * proportionH;
                    var nL = tL * proportionW;
                    var compType = $this.attr('data-component-type');
                    var compId = $this.attr('data-comp-id');
                    var reportCompId = $this.attr('report-comp-id');
                    var curEntity = $.getTargetElement(compId, entityDefs);
                    if (!curEntity) {
                        return;
                    }
                    var clzType = curEntity.clzType;
                    var imgName = '';
                    switch (compType) {
                        case 'CHART':
                            imgName = 'chart';
                            break;
                        case 'TABLE':
                            imgName = 'table';
                            break;
                    }
                    var chkStr = '';
                    var checkedStr = '';
                    // 如果不是当前组件的缩略图，可以勾选
                    if (clzType === 'COMPONENT') {
                        // 如果此组件，已经被当前编辑状态的组件所关联，那么将不能设定关联关系
                        if (activeSilkroadCompId !== compId && !$.isInArray(reportCompId, activeCompRealtionIds)) {
                            var curCompInteraIds;
                            if (curEntity) {
                                curCompInteraIds = $.getEntityInteractionsId(curEntity);
                                if ($.isInArray(activeProductCompId, curCompInteraIds)) {
                                    checkedStr = ' checked=checked';
                                }
                            }
                            chkStr = '<input type="checkbox" name="comp-thumbnail" ' + checkedStr + ' value="' + compId + '" />';
                        }
                        var $Div = $(
                            ['<div class="comp-thumbnail">',
                                chkStr,
                                '<div class="comp-thumbnail-pic">',
                                // TODO:路径可能会改
                                '<img src="src/css/img/thumbnail-', imgName, '.png"/>',
                                '</div>',
                                '</div>'].join('')
                        );
                        // 添加位置信息以及宽度高度
                        $Div.css({
                            width: nW + 'px',
                            height: nH + 'px',
                            left: nL + 'px',
                            top: nT + 'px'
                        });
                        // 添加其他有用属性信息
                        $Div.attr('data-component-type', compType);
                        $Div.attr('data-mold', $this.attr('data-mold'));
                        $Div.attr('data-comp-id', compId);
                        $('.comp-realtion-box').append($Div);
                    }
                }
            },
            /**
             * 下拉框类型改变
             *
             * @param {event} event 点击事件（报表组件上的编辑按钮）
             * @public
             */
            selectTypeChange: function (event) {
                // 下拉框类型
                var $target = $(event.target);
                var selType = $target.val();
                var entityDefs = this.model.canvasModel.reportJson.entityDefs;
                var compId = $target.attr('data-comp-id');
                // 先析构组件
                this.canvasView._component.dispose();
                // TODO:添加维度为空时，限制
                // 修改entity中下拉框类型
                var text = $.trim($target.parent().parent().find('.j-line-x').find('.j-item-text').text()).split('（')[0];
                for (var i = 0, iLen = entityDefs.length; i < iLen; i++) {
                    if (compId === entityDefs[i].compId
                        &&
                        (entityDefs[i].clzKey === 'ECUI_SELECT'
                         || entityDefs[i].clzKey === 'ECUI_MULTI_SELECT'
                        )
                        && entityDefs[i].dataOpt
                    ) {
                        entityDefs[i].dataOpt.textAll = "全部" + text;
                        entityDefs[i].dataOpt.selectAllText = "全部" + text;
                        entityDefs[i].clzKey = selType;
                    }
                }

                // 修改reportVm中对应组件div的data-mold属性
                var selects = this.canvasView.model.$reportVm
                    .find('[data-component-type=SELECT]');
                selects.each(function () {
                    var $this = $(this);
                    if ($this.attr('data-comp-id') === compId) {
                        $this.attr('data-mold', selType);
                    }
                });

                // 修改reportVm中对应组件div的data-set-all属性
                var defaults = this.canvasView.model.$reportVm
                    .find('[data-component-type=SELECT]');
                var $checkbox = $target.parent().find('.select-default');
                var $checked = $checkbox.find('.select-default-value');
                defaults.each(function () {
                    var $this = $(this);
                    if ($this.attr('data-comp-id') === compId) {
                        $this.attr('data-mold', selType);
                        if (selType == 'ECUI_SELECT') {
                            $checkbox.css('display', 'inline-block');
                            $(this).attr('data-set-all', $checked[0].checked);
                        }
                        else {
                            $(this).removeAttr('data-set-all');
                            $target.parent().find('.select-default').hide();
                        }
                    }
                });

                // 保存vm与json，保存成功后展示报表
                this.model.canvasModel.saveJsonVm(
                    this.canvasView.showReport.call(this.canvasView)
                );

            },
            /**
             * 日历下拉框类型改变
             *
             * @param {event} event 点击事件（报表组件上的编辑按钮）
             * @public
             */
            selectCalendarTypeChange: function (event) {
                // 下拉框类型
                var that = this;
                var $target = $(event.target);
                var selType = $target.val();
                var entityDefs = this.model.canvasModel.reportJson.entityDefs;
                var compId = $target.attr('data-comp-id');
                var activeSilkroadCompId = that.getActiveCompId();
                that.model.getCompAxis(activeSilkroadCompId, handleSelectedChange);
                function handleSelectedChange(data) {
                    if (data.xAxis === null || data.xAxis.length <= 0) {
                        if ('CAL_SELECT' === selType) {
                            $target.val('DOUBLE_CAL_SELECT');
                        }
                        else {
                            $target.val('CAL_SELECT');
                        }
                        dialog.alert('请选维度');
                        return;
                    }
                    // 先析构组件
                    that.canvasView._component.dispose();

                    // 修改entity中下拉框类型,设置可拖拽面板中的属性值
                    for (var i = 0, iLen = entityDefs.length; i < iLen; i++) {
                        if (compId === entityDefs[i].compId) {
                            //entityDefs[i].clzKey = selType;
                            // 获取组件的配置信息
                            var compType = $target.attr('data-comp-type');
                            var compData = that.model.canvasModel.compBoxModel.getComponentData(compType);
                            if ('CAL_SELECT' === selType) {
                                entityDefs[i].clzKey = compData.entityDescription.clzKey;
                            } else if ('DOUBLE_CAL_SELECT' === selType) {
                                entityDefs[i].clzKey = compData.entityDescriptionRangeCalendar.clzKey;
                                entityDefs[i].dataSetOpt.rangeTimeTypeOpt =
                                    compData.entityDescriptionRangeCalendar.dataSetOpt.rangeTimeTypeOpt;
                            }

                        }
                    }

                    // 修改reportVm中对应组件div的data-mold属性
                    var selects = that.canvasView.model.$reportVm
                        .find('[data-component-type=TIME_COMP]');
                    selects.each(function () {
                        var $this = $(this);
                        if ($this.attr('data-comp-id') === compId) {
                            $this.attr('data-mold', selType);
                        }
                    });

                    // 保存vm与json，保存成功后展示报表
                    that.model.canvasModel.saveJsonVm(
                        that.canvasView.showReport.call(that.canvasView)
                    );
                }

            },
            /**
             * 报表组件的编辑模块 初始化函数
             *
             * @param {$HTMLElement} option.el
             *        .j-canvas（包括配置栏、工具箱、报表展示区） 与canvas的el相同
             * @param {string} option.reportId 报表的id
             * @param {Object} option.canvasView 画布的view
             * @constructor
             */
            initialize: function (option) {
                var model = this.model = new EditCompModel({
                    canvasModel: option.canvasView.model,
                    reportId: option.reportId
                });
                this.canvasView = option.canvasView;
                this.$conCompSetting = this.$el.find('.j-con-comp-setting');
            },

            /**
             * 初始化组件配置区
             *
             * @param {event} event 点击事件（报表组件上的编辑按钮）
             * @public
             */
            initCompConfigBar: function (event) {
                var that = this;
                var $target = $(event.target);
                var $shell = $target.parents('.j-component-item');
                // 在silkroad中额外添加的组件id
                var compId = $shell.attr('data-comp-id');
                // 组件本身的id
                var reportCompId = $shell.attr('report-comp-id');
                var compType = $shell.attr('data-component-type');
                var compMold = $shell.attr('data-mold');
                var compAll = $shell.attr('data-set-all');
                that.model.compId = compId;
                that.model.compType = compType;
                that.model.compAll = compAll;

                // 需要先处理一下之前可能存在的编辑条与active状态
                that.hideEditBar();

                // 还原组件配置信息
                that.model.getCompAxis(compId, function (data) {
                    data.compId = compId;
                    var template = that._adapterEditCompTemplate(compType);
                    data.compType = compType;
                    data.compAll = compAll;
                    data.reportCompId = reportCompId;
                    compMold && (data.compMold = compMold);
                    var html = template.render(data);
                    var $compSetting = that.$el.find('.j-con-comp-setting');
                    $compSetting.html(html);
                    that.initLineAccept(compType);
                    $shell.addClass('active').mouseout();
                    // 初始化横轴和纵轴数据项的顺序调整
                    that.initSortingItem();

                    // 调整画布大小
                    that.canvasView.parentView.ueView.setSize();
                    that['_init' + compType + 'SettingView'] && (that['_init' + compType + 'SettingView']($compSetting[0]));
                });
            },

            /**
             * 适配组件的编辑模板
             *
             * @param {string} type 组件类型
             * @privite
             */
            _adapterEditCompTemplate: function (type) {
                var template;

                switch (type) {
                    case 'TIME_COMP' :
                        template = compSettingTimeTemplate;
                        break;
                    case 'LITEOLAP' :
                        template = compSettingLITEOLAPTemplate;
                        break;
                    case 'CHART' :
                        template = compSettingChartTemplate;
                        break;
                    case 'SELECT' :
                        template = vuiSettingSelectTemplate;
                        break;
                    case 'SINGLE_DROP_DOWN_TREE':
                        template = ecuiInputTreeSettingTemplate;
                        break;
                    case 'CASCADE_SELECT':
                        template = compCascadeSelectTemplate;
                        break;
                    case 'PLANE_TABLE':
                        template = vuiSettingPlaneTableTemplate;
                        break;
//                    case 'MULTISELECT' :
//                        template = vuiSettingSelectTemplate;
//                        break;
                    default :
                        template = compSettingDefaultTemplate;
                        break;
                }

                return template;
            },

            /**
             * 初始化接收拖拽进来的维度和指标 的功能
             *
             * @param {string} type 组件的类型
             * @public
             */
            initLineAccept: function (type) {
                var that = this;
                var selector;
                var dStr = '.j-olap-element';
                var tStr = '.j-time-dim';
                switch (type) {
                    case 'TIME_COMP' :
                        selector = tStr;
                        break;
                    case 'LITEOLAP' :
                        selector = dStr;
                        break;
                    default :
                        selector = dStr;
                        break;
                }

                $('.j-comp-setting-line', this.el).droppable({
                    accept: selector,
                    drop: function (event, ui) {
                        var $this = $(this);
                        that.addCompAxis(ui, $this, type);
                        $this.removeClass('active');
                        if ($this.attr('data-axis-type') == 'y') {
                            $('.norm-empty-prompt').hide();
                        }
                    },
                    out: function (event, ui) {
                        $(this).removeClass('active');
                    },
                    over: function (event, ui) {
                        $(this).addClass('active');
                    },
                    helper: 'clone'
                });
            },

            /**
             * 初始化横轴和纵轴数据项的顺序调整
             *
             * @public
             */
            initSortingItem: function () {
                var that = this;
                // 向后台提交的数据
                var data = { };

                $('.j-line-x,.j-line-y', this.el).sortable({
                    items: ".j-root-line,.j-cal-ind,.j-group-title",
                    axis: 'x',
                    start: function (event, ui) {
                        ui.placeholder.height(0);
                        data.source = ui.item.parent().find('[data-id]').index(ui.item);
                    },
                    stop: function (event, ui) {
                        var $parent = ui.item.parent();
                        var compId = $parent.parent().attr('data-comp-id');
                        data.target = $parent.find('[data-id]').index(ui.item);
                        data.type = $parent.attr('data-axis-type');
                        if (data.source != data.target) {
                            that.model.sortingCompDataItem(compId, data, function () {
                                that.canvasView.showReport();
                            });
                        }
                    }
                });
            },

            /**
             * 删除 指标或维度（从数据轴中）
             *
             * @param {event} event 点击事件
             * @public
             */
            deleteCompAxis: function (event) {
                event.stopPropagation();
                var that = this;
                var $target = $(event.target);
                // 还原默认值
                var $seldefault = $target.parent().parent();
                // 当前控件ID
                var compId = $seldefault.parent().attr('data-comp-id');
                // 删除维度初始化单选下拉框默认值
                if ($seldefault.parent().attr('data-comp-type') == 'SELECT') {
                    var $allcheck = $seldefault.parent().find('input[class ^= "select-default-value"]');
                    $allcheck.removeAttr('checked');
                    var checked = $allcheck[0].checked;
                    var allName = $allcheck.attr('value');
                    // 设置单选下拉框默认值
                    that.selectSetAll(checked, allName, compId);
                    $seldefault.next().find('.select-default-name').text('全部');
                    $seldefault.next().find('.select-default-value').val('全部');
                }
                var data = {};
                var selector;
                var attr;

                selector = '.j-comp-setting';
                var $compSetting = $target.parents(selector);
                var compId = $compSetting.attr('data-comp-id');
                var compType = $compSetting.attr('data-comp-type');

                selector = '.j-comp-setting-line';
                attr = 'data-axis-type';
                var axisType = $target.parents(selector).attr(attr);

                attr = 'data-id';
                var olapId = $target.parent().attr(attr);
                that.model.deleteCompAxis(
                    compId,
                    axisType,
                    olapId,
                    function () {
                        var $item = $target.parent();
                        var oLapElementId = $item.attr('data-id');
                        $target.parent().remove();
                        that.afterDeleteCompAxis({
                            oLapElementId: oLapElementId,
                            compType: compType,
                            axisType: axisType,
                            $item: $target.parent()
                        });
                        // 调整画布大小
                        // that.canvasView.parentView.ueView.setSize();
                        // 重新渲染报表
                        that.canvasView.showReport();
                        // 还原指标和维度的可互拖
                        if (that.$el.find('[data-id=' + oLapElementId + ']').length == 0) {
                            var str = ' .j-root-line[data-id=' + oLapElementId + ']';
                            $('.j-con-org-ind' + str).addClass('j-can-to-dim');
                            $('.j-con-org-dim' + str).addClass('j-can-to-ind');
                        }
                    }
                );
            },

            /**
             * 删除完成数据项之后要做的特殊dom处理
             *
             * @param {Object} option 配置参数
             * @param {string} option.oLapElemenId 数据项id
             * @param {string} option.compType 组件类型
             * @param {string} option.axisType 轴类型
             * @public
             */
            afterDeleteCompAxis: function (option) {
                // 容错
                if (option.compType === undefined) {
                    return;
                }

                var compType = this._switchCompTypeWord(option.compType);
                this['afterDelete' + compType + 'CompAxis'](option);
            },

            /**
             * 删除完成LiteOlap组件的数据项之后要做的特殊dom处理
             *
             * @param {Object} option 配置参数
             * @public
             */
            afterDeleteLiteOlapCompAxis: function (option) {
                var that = this;
                var $compSetting = that.$el.find('.j-comp-setting');
                var axisType = option.axisType;
                var isXYS = 'xys'.indexOf(axisType) > -1;

                var selector = '[data-id=' + option.oLapElementId + ']';
                // 数据项
                var $items = $compSetting.find(selector);
                // 备选区有当前删掉的数据项
                selector = '.j-delete';
                var html = '<span class="icon-letter j-delete">×</span>';
                if ($items.length == 1 && $items.find(selector).length == 0) {
                    $items.append(html);
                }
            },

            /**
             * 删除完成时间组件的数据项之后要做的特殊dom处理
             *
             * @param {Object} option 配置参数
             * @public
             */
            afterDeleteTimeCompAxis: function (option) {
                var compBoxModel = this.model.canvasModel.compBoxModel;
                var calendarModel = compBoxModel.getComponentData('TIME_COMP');
                var compId = this.getActiveCompId();
                var editCompModel = this.canvasView.editCompView.model;
                var json = editCompModel.getCompDataById(compId)[0];
                var name = option.$item.attr('data-name');
                var letter = calendarModel.switchLetter(name);

                // 分别删除
                var list = json.dataSetOpt.timeTypeList;
                for (var i = 0, len = list.length; i < len; i++) {
                    if (letter == list[i].value) {
                        list.splice(i, 1);
                        break;
                    }
                }
                delete json.dataSetOpt.timeTypeOpt[letter];
                delete json.dateKey[letter];
                //json.name = json.dateKey[json.dataSetOpt.timeTypeList[0].value];
                this.model.canvasModel.saveJsonVm();
            },
            afterDeleteCascadeSelectCompAxis: function (option) {

            },
            afterDeleteChartCompAxis: function (option) {

            },
            afterDeletePlaneTableCompAxis: function (option) {

            },
            afterDeleteTableCompAxis: function (option) {

            },
            afterDeleteSelectCompAxis: function (option) {

            },
            /**
             * 添加组件的数据关联配置（指标 或 维度）
             *
             * @param {Object} ui 被拖拽的对象（里面包含被拖拽的本体元素）
             * @param {$HTMLElement} $acceptUi 接收拖拽元素的元素
             * @public
             */
            addCompAxis: function (ui, $acceptUi) {
                var $draggedUi = ui.helper;
                var that = this;
                var selector;
                var str;
                var cubeId;
                var oLapElemenType;
                var alert = dialog.alert;
                selector = '.j-comp-setting';
                var $root = $acceptUi.parents(selector);
                var compId = $root.attr('data-comp-id');
                var compType = $root.attr('data-comp-type');
                var $item = $draggedUi.clone().attr('style', '');
                var itemId = $item.attr('data-id');
                // 默认值选择
                var $selectDefault = $('.select-default');
                // 单选下拉框添加维度时，初始化默认值设定
                if (compType === 'SELECT') {
                    // 获取当前单选下拉框设定默认值元素
                    var $selectValue = $('input[class ^= "select-default-value"]');
                    $selectValue.removeAttr('checked');
                    var $selectName = $('.select-default-name');
                    // 当前单选下拉框默认值判定变量
                    var checked = $selectValue[0].checked;
                    // 当前单选下拉框默认值
                    var allName = $selectValue.attr('value');
                    // 设置单选下拉框默认值
                    that.selectSetAll(checked, allName, compId);
                }
                if (compType === 'PLANE_TABLE' && $acceptUi.attr('data-axis-type') === 's') {
                    //var $yItems = $('.j-line-y').find('.item');
                    //var hasSameItem = false;
                    //$yItems.each(function () {
                    //    if ($(this).attr('data-id') === itemId) {
                    //        hasSameItem = true;
                    //    }
                    //});
                    //if (!hasSameItem) {
                    //    // TODO:修改提示信息
                    //    alert('列轴没有此项');
                    //    return;
                    //}
                }
                if ($.isInArray(compType, Constant.DRAG_SINGLE_DIM)) {
                    if ($('.data-axis-line .item').length >= 1) {
                        alert('只能拖一个维度');
                        return;
                    }
                }
                else if ($.isInArray(compType, Constant.DRAG_SINGLE_DIMGROUP)) {
                    if ($draggedUi.attr('data-group') !== 'item-group'
                        || $('.data-axis-line .item').length >= 1
                    ) {
                        alert('只能拖一个维度组');
                        return;
                    }
                }
                var $spans = $item.find('span');
                // 维度组
                if ($item.hasClass('j-group-title')) {
                    $item.addClass('item');
                }
                // 普通指标和维度
                else {
                    $spans.eq(1).remove();
                }
                // 使维度指标不能互换 - 因为维度或指标被使用了
                ui.draggable.removeClass('j-can-to-dim j-can-to-ind');
                // 采用非维度 即 指标 的策略
                selector = '.j-data-sources-setting-con-ind';
                oLapElemenType = ui.draggable.parents(selector);
                oLapElemenType = oLapElemenType.length ? 'ind' : 'dim';
                // 添加指标（维度）项到XY
                if (!that._addDimOrIndDomToXY($item, oLapElemenType, compType)) {
                    return;
                }

                cubeId = that.canvasView.parentView.model.get('currentCubeId');
                var data = {
                    cubeId: cubeId,
                    oLapElementId: itemId,
                    axisType: $acceptUi.attr('data-axis-type')
                };

                // 避免调顺序产生拖入的干扰
                $item.removeClass('j-olap-element').addClass('c-m');
                that.model.addCompAxis(compId, data, function () {
                    // 去除时间维度的接收拖拽与调序的冲突
                    $item.removeClass('j-time-dim');
                    // 成功后再添加
                    $acceptUi.append($item);
                    that.afterAddCompAxis({
                        compType: compType,
                        oLapElemenType: oLapElemenType,
                        oLapElementId: data.oLapElementId,
                        axisType: data.axisType,
                        $item: $item
                    });
                    // 判断为下拉框的话更改默认设置
                    if (compType == 'SELECT') {
                        if ($selectDefault.is(':visible')) {
                            var all = $selectName.text();
                            var dimname = $acceptUi.find('span').eq(1).text().split('（')[0];
                            $selectValue.val(all + dimname);
                            $selectName.text(all + dimname);
                        }
                    }
                    // 刷新报表展示
                    that.canvasView.showReport();
                    // 调整画布大小
                    that.canvasView.parentView.ueView.setSize();
                });
            },
            _addDimOrIndDomToXY: function ($item, oLapElemenType, compType) {
                // 在轴上添加指标与维度项的dom
                var alert = dialog.alert;
                var str;
                // 指标
                if (oLapElemenType === 'ind') {
                    // TODO:如果是单图
                    if (compType === 'CHART') {
                        var indItems = $('.j-line-y').find('div');
//                        if (indItems.length >= 1) {
//                            alert('单图指标不能继续拖拽');
//                            return false;
//                        }
                        var chartType = $(indItems[0]).find('.icon-chart').attr('chart-type');
                        var typeSubsidiary = chartTypeSubsidiary(chartType);
                        if (typeSubsidiary !== 0) {
                            chartType = 'column';
                        }
                        str = '<span class="icon-chart ' + chartType + ' j-icon-chart" chart-type="' + chartType + '" ></span>';
                        $item.prepend(str);
                    }
                }
                str = '<span class="icon hide j-delete" title="删除">×</span>';
                $item.append(str);
                $($item.find('.j-item-text')).removeClass('ellipsis').addClass('icon-font');
                // TODO:判断其他图形的类型
                return true;
            },
            /**
             * 显示图形列表
             *
             * @param {Object} ui 被拖拽的对象（里面包含被拖拽的本体元素）
             * @param {$HTMLElement} $acceptUi 接收拖拽元素的元素
             * @public
             */
            showChartList: function (event) {
                var that = this;
                var $target = $(event.target);
                var selector = '.j-comp-setting';
                var $compSetting = $target.parents(selector);
                var compId = $compSetting.attr('data-comp-id');
                var olapId = $target.parent().attr('data-id');
                var oldChartType = $target.attr('chart-type');
                var chartTypes = Constant.CHART_TYPES;

                for (var key in chartTypes) {
                    chartTypes[key] = false;
                }
                chartTypes[oldChartType] = true;
                if (!that.chartList) {
                    that.chartList = new FloatWindow({
                        direction: 'vertical',
                        content: indMenuTemplate.render(chartTypes)
                    });
                }
                else {
                    that.chartList.redraw(indMenuTemplate.render(chartTypes));
                }
                // FIXME:这块的实现不是很好，需要修改
                $('.comp-setting-charticons span').unbind();
                $('.comp-setting-charticons span').click(function () {
                    var $this = $(this);
                    var selectedChartType = $this.attr('chart-type');
                    // 如果是饼图的话，比较麻烦，不能同时选择两个饼图
                    var $chartTypes = $target.parent().siblings('div');
                    if (selectedChartType === 'pie') {
//                        if ($chartTypes.length >= 1) {
//                            alert('饼图只能选择一个指标');
//                            return;
//                        }
                    }
//                    if (selectedChartType === 'bar') {
//                        var isAllBar = true;
//                        $chartTypes.each(function () {
//                            alert();
//                        });
//                    }
                    // 纵轴、候选指标改变
                    that.model.changeCompItemChartType(
                        compId,
                        olapId,
                        selectedChartType,
                        function () {
                            var dimItems = $('.j-line-y').find('div');
                            var candSiblings = $('.j-line-cand-ind').find('div');
                            // 改变当前y轴中其他指标的类型
                            // 判断当前chartType归属，如果是单选图，y轴与候选指标区域中的类型更换成一致的
                            // 如果是0，说明是单图
                            var typeSubsidiary = chartTypeSubsidiary(selectedChartType);
                            dimItems.each(function () {
                                changeIconType($(this), selectedChartType, typeSubsidiary);
                            });
                            candSiblings.each(function () {
                                changeIconType($(this), selectedChartType, typeSubsidiary);
                            });
                            // TODO:如果是
                            $target.removeClass(oldChartType).addClass(selectedChartType);
                            $target.attr('chart-type', selectedChartType);
                            that.chartList.hide();
                            that.canvasView.showReport();
                        }
                    );
                });
                that.chartList.show($(event.target).parent());
                // 修改候选指标里面的图形图标
                function changeIconType($this, chartType, isCombine) {
                    var $chartSpan = $this.find('.icon-chart');
                    var temOldType = $chartSpan.attr('chart-type');

                    if (isCombine) {
                        if (chartTypeSubsidiary(temOldType) === 0) {
                            $chartSpan.removeClass(temOldType).addClass(chartType);
                            $chartSpan.attr('chart-type', chartType);
                        }
                    }
                    else {
                        $chartSpan.removeClass(temOldType).addClass(chartType);
                        $chartSpan.attr('chart-type', chartType);
                    }
                }
            },
            /**
             * 添加完成数据项之后要做的特殊dom处理
             *
             * @param {Object} option 配置参数
             * @param {string} option.compType 组件类型
             * @param {string} option.oLapElemenType 数据项类型
             * @param {string} option.oLapElemenId 数据项id
             * @param {string} option.axisType 轴类型
             * @param {$HTMLElement} option.$item 数据项dom
             * @public
             */
            afterAddCompAxis: function (option) {
                // 容错
                if (option.compType === undefined) {
                    return;
                }
                var compType = this._switchCompTypeWord(option.compType);
                this['afterAdd' + compType + 'CompAxis'](option);
            },

            /**
             * 添加完成 LITEOLAP组件的 数据项之后要做的特殊dom处理
             *
             * @param {Object} option 配置参数
             * @param {string} option.compType 组件类型
             * @param {string} option.oLapElemenType 数据项类型
             * @param {string} option.oLapElemenId 数据项id
             * @param {string} option.axisType 轴类型
             * @param {$HTMLElement} option.$item 数据项dom
             * @public
             */
            afterAddLiteOlapCompAxis: function (option) {
                var that = this;
                var $compSetting = that.$el.find('.j-comp-setting');
                var isXYS = 'xys'.indexOf(option.axisType) > -1;

                // 如果拖到轴区
                if (isXYS) {
                    processCand(option, $compSetting);
                }
            },
            /**
             * 添加完成数据项之后要做的特殊dom处理-图形
             *
             * @param {Object} option 配置参数
             * @param {string} option.compType 组件类型
             * @param {string} option.oLapElemenType 数据项类型
             * @param {string} option.oLapElemenId 数据项id
             * @param {string} option.axisType 轴类型
             * @param {$HTMLElement} option.$item 数据项dom
             * @public
             */
            afterAddChartCompAxis: function (option) {
                var that = this;
                var $compSetting = that.$el.find('.j-comp-setting');
                var isXYS = 'xys'.indexOf(option.axisType) > -1;

                // 如果拖到轴区
                if (isXYS) {
                    processCand(option, $compSetting);
                }
            },
            /**
             * 添加完成数据项之后要做的特殊dom处理-下拉框
             *
             * @param {Object} option 配置参数
             * @param {string} option.compType 组件类型
             * @param {string} option.oLapElemenType 数据项类型
             * @param {string} option.oLapElemenId 数据项id
             * @param {string} option.axisType 轴类型
             * @param {$HTMLElement} option.$item 数据项dom
             * @public
             */
            afterAddSelectCompAxis: function (option) {
                var compId = this.getActiveCompId();
                var id = option.$item.attr('data-id');
                var editCompModel = this.canvasView.editCompView.model;
                var entityDefs = editCompModel.canvasModel.reportJson.entityDefs;
                var text = $.trim(option.$item.find('.j-item-text').text().split('（')[0]);

                for (var i = 0, len = entityDefs.length; i < len; i++) {
                    if (entityDefs[i].compId == compId
                        &&
                        (entityDefs[i].clzKey === 'ECUI_MULTI_SELECT'
                         || entityDefs[i].clzKey === 'ECUI_SELECT'
                        )
                        && entityDefs[i].dataOpt
                    ) {
                        entityDefs[i].dimId = id;
                        entityDefs[i].dataOpt.textAll = "全部" + text;
                        entityDefs[i].dataOpt.selectAllText = "全部" + text;
                    }
                }
                this.model.canvasModel.saveJsonVm();
            },
            /**
             * 添加完成数据项之后要做的特殊dom处理-下拉树
             *
             * @param {Object} option 配置参数
             * @param {string} option.compType 组件类型
             * @param {string} option.oLapElemenType 数据项类型
             * @param {string} option.oLapElemenId 数据项id
             * @param {string} option.axisType 轴类型
             * @param {$HTMLElement} option.$item 数据项dom
             * @public
             */
            afterAddSingleDropDownTreeCompAxis: function (option) {
                var compId = this.getActiveCompId();
                var id = option.$item.attr('data-id');
                var editCompModel = this.canvasView.editCompView.model;
                var entityDefs = editCompModel.canvasModel.reportJson.entityDefs;

                for (var i = 0, len = entityDefs.length; i < len; i++) {
                    if (entityDefs[i].compId == compId) {
                        entityDefs[i].dimId = id;
                    }
                }
                this.model.canvasModel.saveJsonVm();
            },
            /**
             * 添加完成数据项之后要做的特殊dom处理-下拉树
             *
             * @param {Object} option 配置参数
             * @param {string} option.compType 组件类型
             * @param {string} option.oLapElemenType 数据项类型
             * @param {string} option.oLapElemenId 数据项id
             * @param {string} option.axisType 轴类型
             * @param {$HTMLElement} option.$item 数据项dom
             * @public
             */
            afterAddCascadeSelectCompAxis: function (option) {
                var compId = this.getActiveCompId();
                var id = option.$item.attr('data-id');
                var editCompModel = this.canvasView.editCompView.model;
                var entityDefs = editCompModel.canvasModel.reportJson.entityDefs;
                var allDim = window.dataInsight.main.model.attributes.dimList;
                var groupItem = [];
                var selectAllDimArray = [];
                for (var i = 0; i < allDim.length; i ++) {
                    if (allDim[i].id === option.oLapElementId) {
                        groupItem = allDim[i].levels;
                    }
                }
                if (groupItem.length === 0) {
                    selectAllDim = {};
                }
                else {
                    for (var i = 0; i < groupItem.length; i ++) {
                        var selectAllDim = {};
                        selectAllDim.value = groupItem[i].caption;
                        selectAllDim.text = selectAllDim[i + 1];
                        selectAllDimArray.push(selectAllDim);
                    }
                }
                for (var i = 0, len = entityDefs.length; i < len; i++) {
                    if (entityDefs[i].compId == compId) {
                        entityDefs[i].dimId = id;
                        entityDefs[i].selectAllDim = selectAllDimArray;
                    }
                }
                this.model.canvasModel.saveJsonVm();
            },
            /**
             * 添加完成数据项之后要做的特殊dom处理-表格
             *
             * @param {Object} option 配置参数
             * @param {string} option.compType 组件类型
             * @param {string} option.oLapElemenType 数据项类型
             * @param {string} option.oLapElemenId 数据项id
             * @param {string} option.axisType 轴类型
             * @param {$HTMLElement} option.$item 数据项dom
             * @public
             */
            afterAddTableCompAxis: function (option) {
            },
            afterAddPlaneTableCompAxis: function (option) {
            },
            /**
             * 添加完成 时间控件 数据项之后要 特别处理json（采用了拼字符串的方式调用）
             *
             * @param {Object} option 配置参数
             * @public
             */
            afterAddTimeCompAxis: function (option) {
                var compBoxModel = this.model.canvasModel.compBoxModel;
                var calendarModel = compBoxModel.getComponentData('TIME_COMP');
                var compId = this.getActiveCompId();
                var editCompModel = this.canvasView.editCompView.model;
                var json = editCompModel.getCompDataById(compId)[0];
                // 数据项的name和id
                var name = option.$item.attr('data-name');
                var id = option.$item.attr('data-id');
                var calendarConfig = calendarModel.timeTypeConfig;
                // 获取两个设置，分别push
                var timeTypeList;
                var timeTypeOpt;
                var letter = calendarModel.switchLetter(name);

                timeTypeList = calendarConfig.timeTypeList[letter];
                timeTypeOpt = calendarConfig.timeTypeOpt[letter];
                // TODO （先不注意顺序，后续需要对顺序做处理）
                json.dataSetOpt.timeTypeList.push(timeTypeList);
                json.dataSetOpt.timeTypeOpt[letter] = timeTypeOpt;
                json.dateKey[letter] = id;
                json.name = json.dateKey[json.dataSetOpt.timeTypeList[0].value];
                this.model.canvasModel.saveJsonVm();
            },

            /**
             * 类型转换
             *
             * @param {string} oldType 转换前的类型
             * @private
             * @return {string} newType 转换后的类型
             */
            _switchCompTypeWord: function (oldType) {
                var newType;

                switch (oldType) {
                    case 'LITEOLAP':
                        newType = 'LiteOlap';
                        break;
                    case 'TIME_COMP':
                        newType = 'Time';
                        break;
                    case 'CHART':
                        newType = 'Chart';
                        break;
                    case 'SELECT':
                        newType = 'Select';
                        break;
                    case 'TABLE':
                        newType = 'Table';
                        break;
                    case 'PLANE_TABLE':
                        newType = 'PlaneTable';
                        break;
                    case 'SINGLE_DROP_DOWN_TREE':
                        newType = 'SingleDropDownTree';
                        break;
                    case 'CASCADE_SELECT':
                        newType = 'CascadeSelect';
                        break;
                }
                return newType;
            },

            /**
             * 获取组建编辑区和编辑区所对应的组件
             *
             * @private
             */
            _getEditBarAndActiveComp: function () {
                var $compSetting = this.$el.find('.j-comp-setting');
                var compId;
                var selector;
                var result = {};

                // 设置区不存在就不做任何操作
                if ($compSetting.length === 0) {
                    return result;
                }
                else {
                    result.$compSetting = $compSetting;
                }
                compId = $compSetting.attr('data-comp-id');

                // 去除组件的活动状态样式
                selector = '.j-component-item[data-comp-id=' + compId + ']';
                result.$activeComp = this.$el.find(selector);

                return result;
            },

            /**
             * 去除报表组件配置区（去除组件的活动状态样式）
             *
             * @public
             */
            hideEditBar: function () {
                var elements = this._getEditBarAndActiveComp();

                // 去除报表组件配置
                if (elements.$compSetting !== undefined) {
                    elements.$compSetting.remove();
                }

                // 去除组件的活动状态样式
                if (elements.$activeComp !== undefined) {
                    elements.$activeComp.removeClass('active');
                }
                // 调整画布大小
                this.canvasView.parentView.ueView.setSize();
            },

            /**
             * 激活正在编辑的组件（由于报表渲染之后会丢失此状态，所以需要另作处理）
             *
             * @public
             */
            activeComp: function () {
                var elements = this._getEditBarAndActiveComp();

                // 添加组件的活动状态样式
                if (elements.$activeComp !== undefined) {
                    elements.$activeComp.addClass('active');
                }
            },

            /**
             * 点击画布的空白区域隐藏组件编辑区
             *
             * @param {event} event 点击事件
             * @public
             */
            removeCompEditBar: function (event) {
                var that = this;

                if ($(event.target).parent().hasClass('j-report')) {
                    that.hideEditBar();
                }
            },

            /**
             * 打开默认时间设置弹框
             *
             * @param {event} event 点击事件
             * @public
             */
            openTimeSettingDialog: function (event) {
                var that = this;
                var compBoxModel = that.model.canvasModel.compBoxModel;
                var compId = that.getActiveCompId();
                var compData = that.model.getCompDataById(compId);
                if (compData[0].clzKey === 'RANGE_CALENDAR') {
                    var compBoxModel = that.model.canvasModel.compBoxModel;
                    // 可做逻辑拆分，将部分代码拆分到model中
                    var renderTemplateData = null;
                    if (compData[0].dataSetOpt.rangeTimeTypeOpt !== undefined) {
                        renderTemplateData = {
                            start: compData[0].dataSetOpt.rangeTimeTypeOpt.startDateOpt,
                            end: compData[0].dataSetOpt.rangeTimeTypeOpt.endDateOpt
                        };
                    } else {
                        renderTemplateData = compBoxModel.getComponentData('TIME_COMP').rangeConfig;
                    }
                    var html = defaultSelectedRangeTimeSettingTemplate.render({
                        item: renderTemplateData
                    });
                }
                else {
                    var compBoxModel = that.model.canvasModel.compBoxModel;
                    // 可做逻辑拆分，将部分代码拆分到model中
                    var deSwitchConfig = compBoxModel.getComponentData('TIME_COMP').deSwitchConfig;
                    var renderTemplateData = deSwitchConfig(compData[0].dataSetOpt.timeTypeOpt);
                    console.log(renderTemplateData);
                    var html = defaultSelectedTimeSettingTemplate.render({
                        list: renderTemplateData
                    });

                }

                /**
                 * 从表单中提取配置数据
                 *
                 * @param {$HTMLElement} $dialog 弹框内容区
                 * @return {Object} 配置参数
                 */
                function getDataFromForm($dialog) {
                    var arr = [];
                    var $item = $dialog.find('.j-item');
                    $item.each(function () {
                        var data = {};
                        var $this = $(this);
                        // 粒度
                        var particleSize = $this.attr('data-type');
                        // 相对时间的单位
                        var unit = $this.find('select').val();
                        // 相对值
                        var val = $this.find('input').val();

                        data.type = particleSize;
                        // 默认时间，单个可能是单选
                        data.date = [val + unit];
                        // 设置data range的情况
                        data.startDateOpt = $this.find('[name="startDateSetting"]').val();
                        data.endDateOpt = $this.find('[name="endDateSetting"]').val();
                        arr.push(data);
                    });

                    return arr;
                }

                dialog.showDialog({
                    title: '默认选中时间设置',
                    content: html,
                    dialog: {
                        width: 300,
                        height: 249,
                        open: function () {
                            // TODO 翻译选项的功能
                        },
                        buttons: [
                            {
                                text: '提交',
                                click: function () {
                                    var $this = $(this);
                                    var rangeStart = $this.find('[name="startDateSetting"]').val();
                                    var rangeEnd = $this.find('[name="endDateSetting"]').val();
                                    // 如果设置range时间时，rangeend如果大于rangestart则不能设置。
                                    if (rangeStart !== undefined && rangeEnd !== undefined) {
                                        if (parseInt(rangeEnd) < parseInt(rangeStart)) {
                                            dialog.alert("设置的默认结束时间应小于默认开始时间");
                                            return;
                                        }
                                    }
                                    // 如果设置single时间时，则不能设置。
                                    var singleDateSetting = $this.find('[name="singleDateSetting"]').val();
                                    if (singleDateSetting !== undefined) {
                                        if (parseInt(singleDateSetting) > 0) {
                                            dialog.alert("设置的默认的时间点应为负数");
                                            return;
                                        }
                                    }
                                    // 提取表单数据
                                    var data = getDataFromForm($this);
                                    // 处理并回填json
                                    that.model.updateCalendarJson(
                                        data,
                                        function () {
                                            $this.dialog('close');
                                            that.canvasView.showReport();
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
             * 获取数据格式信息，并弹框展现
             *
             * @param {event} event 点击事件
             * @public
             */
            getDataFormatList: function (event) { //TODO:实现业务逻辑
                var that = this;
                var compId = that.getActiveCompId();

                that.model.getDataFormatList(compId, openDataFormatDialog);

                /**
                 * 打开数据格式设置弹框
                 */
                function openDataFormatDialog(data) {
                    var html;
                    if ($.isObjectEmpty(data.dataFormat)) {
                        dialog.alert('没有指标');
                        return;
                    }

                    html = dataFormatSettingTemplate.render(
                        data
                    );
                    if ($('.j-line-y').find('div').length != 0) {
                        dialog.showDialog({
                            title: '数据格式',
                            content: html,
                            dialog: {
                                width: 340,
                                height: 400,
                                resizable: false,
                                buttons: [
                                    {
                                        text: '提交',
                                        click: function () {
                                            saveDataFormInfo($(this));
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
                    }
                    else {
                        $('.norm-empty-prompt').show();
                    }
                }

                /**
                 * 保存数据格式
                 */
                function saveDataFormInfo($dialog) {
                    var selects = $('.data-format').find('select');
                    var data = {};

                    selects.each(function () {
                        var $this = $(this);
                        var name = $this.attr('name');
                        data[name] = $this.val();
                        //console.log(data[name]);
                    });
                    that.model.saveDataFormatInfo(compId, data, function () {
                        $dialog.dialog('close');
                        that.canvasView.showReport();
                    });
                }
            },

            /**
             * 获取指标描述信息，并弹框展现
             *
             * @param {event} event 点击事件
             * @public
             */
            getNormInfoDepict: function (event) { //TODO:实现业务逻辑
                var that = this;
                var compId = that.getActiveCompId();
                that.model.getNormInfoDepict(compId, openDataFormatDialog);
                /**
                 * 打开数据格式设置弹框
                 */
                function openDataFormatDialog(data) {
                    var html;
                    if (!data) {
                        dialog.alert('没有指标');
                        return;
                    }
                    html = normInfoDepictTemplate.render(
                        data
                    );
                    if ($('.j-line-y').find('div').length != 0) {
                        dialog.showDialog({
                            title: '指标信息描述',
                            content: html,
                            dialog: {
                                width: 340,
                                height: 400,
                                resizable: false,
                                buttons: [
                                    {
                                        text: '提交',
                                        click: function () {
                                            saveNormInfoDepict($(this));
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
                    }
                    else {
                        $('.norm-empty-prompt').show();
                    }
                }

                /**
                 * 保存数据格式
                 */
                function saveNormInfoDepict($dialog) {
                    var texts = $('.data-format').find('input');
                    var data = {};

                    texts.each(function () {
                        var $this = $(this);
                        var name = $this.attr('name');
                        data[name] = $this.val();
                    });
                    that.model.saveNormInfoDepict(compId, data, function () {
                        $dialog.dialog('close');
                        that.canvasView.showReport();
                    });
                }
            },

            /**
             * 获取活动状态的组件的id
             *
             * @public
             * @return {string} 组件id
             */
            getActiveCompId: function () {
                var $compSetting = this.$conCompSetting.find('.j-comp-setting');
                return $compSetting.attr('data-comp-id');
            },
            getActiveReportCompId: function () {
                var $compSetting = this.$conCompSetting.find('.j-comp-setting');
                return $compSetting.attr('report-comp-id');
            },
            /**
             * 下拉卡框添加默认值公共函数
             *
             * @param {string} checked 默认值判断条件
             * @param {string} allName 当前默认值
             * @param {number} compId 当前控件ID
             * @public
             */
            selectSetAll: function (checked, allName, compId) {
                var selects = this.canvasView.model.$reportVm
                    .find('[data-component-type=SELECT]');
                selects.each(function () {
                    var $this = $(this);
                    if ($this.attr('data-comp-id') === compId) {
                        $this.attr('data-set-all', checked);
                    }
                });
                // TODO:在entityDef属性中加入新的属性，或者干掉
                var entityDefs = this.model.canvasModel.reportJson.entityDefs;
                var entityDef;
                for (var i = 0, iLen = entityDefs.length; i < iLen; i ++) {
                    if (entityDefs[i].clzType === 'VUI' && entityDefs[i].compId === compId) {
                        entityDef = entityDefs[i];
                        entityDef.hasAllNode = checked;
                        entityDef.hasAllNodeText = allName;
                        break;
                    }
                }
                // 保存vm与json，保存成功后展示报表
                this.model.canvasModel.saveJsonVm(
//                    this.canvasView.showReport.call(this.canvasView)
                );
            },
            /**
             * 初始化图形设置区域视图
             *
             * @param {HTMLEelement} 图形编辑区的dom元素
             * @private
             */
            _initCHARTSettingView: function (el) {
                if(this.chartSettingView) {
                    this.chartSettingView.destroy();
                }
                this.chartSettingView = new ChartSettingView({
                    el: el,
                    reportId: this.model.reportId,
                    canvasView: this.canvasView
                });
            },
            /**
             * 初始化表格设置区域视图
             *
             * @param {HTMLEelement} 表格编辑区的dom元素
             * @private
             */
            _initTABLESettingView: function (el) {
                if(this.tableSettingView) {
                    this.tableSettingView.destroy();
                }
                this.tableSettingView = new TableSettingView({
                    el: el,
                    reportId: this.model.reportId,
                    canvasView: this.canvasView
                });
            },

            _initPLANE_TABLESettingView: function (el) {
                if(this.planeTableSettingView) {
                    this.planeTableSettingView.destroy();
                }
                this.planeTableSettingView = new PlaneTableSettingView({
                    el: el,
                    reportId: this.model.reportId,
                    canvasView: this.canvasView
                });
            },

            /**
             * 初始化透视表格设置区域视图
             *
             * @param {Object} el 表格编辑区的dom元素
             */
            _initLITEOLAPSettingView: function (el) {
                if (this.olapTableSettingView) {
                    this.olapTableSettingView.destroy();
                }
                this.olapTableSettingView = new OlapTableSettingView({
                    el: el,
                    reportId: this.model.reportId,
                    canvasView: this.canvasView
                });
            }

        });

        /**
         * 处理候选区域
         * 当指标（维度）拖入纵（横）轴后，候选指标（维度）区域添加指标（维度）的逻辑
         * 当备选区域没有拖入项时，那么把拖入项的删除按钮干掉，然后放入备选区域，意指：首选区域有的，备选区域不能删除
         * 当备选区域已有拖入项时，那么把备选区域中已有项的删除按钮干掉（因为之前当前项在首选区域没有，本身是需要删除按钮的）
         * @param {Object} option 点击事件（报表组件上的编辑按钮）
         * @param {$Htmlelement} $compSetting 点击事件（报表组件上的编辑按钮）
         * @private
         */
        function processCand(option, $compSetting) {
            var oLapElementId = option.oLapElementId;
            var selector = '[data-id=' + oLapElementId + ']';
            // 数据项
            var $items = $compSetting.find(selector);

            // 备选区没有当前拖进来的数据项
            if ($items.length == 1) {
                if (option.oLapElemenType == 'ind') {
                    selector = '.j-line-cand-ind';
                }
                else {
                    selector = '.j-line-cand-dim';
                }
                var $itemClone = option.$item.clone();
                $itemClone.find('.j-delete').remove();
                $compSetting.find(selector).append($itemClone);
            }
            // 备选区已有当前拖进来的数据
            else if ($items.length == 2) {
                // 移除删除图标
                var $delete = $items.eq(1).find('.j-delete');
                if ($delete.length == 1) {
                    $delete.remove();
                }
            }
        }

        /**
         * 判断是图形附属于哪一种（单选或多选）
         * @param {string} type 当前图形
         * @return {number} result 0:单个 1:组合
         * @private
         */
        function chartTypeSubsidiary(type) {
            var singleChart = Constant.SINGLE_CHART;
            var combinationChart = Constant.COMBINATION_CHART;
            if ($.isInArray(type, singleChart)) {
                return 0;
            }
            if ($.isInArray(type, combinationChart)) {
                return 1;
            }
        }
    }
);
