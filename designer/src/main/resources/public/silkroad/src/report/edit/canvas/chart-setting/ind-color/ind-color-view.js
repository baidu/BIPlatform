/**
 * @file: 报表新建（编辑）-- 图形组件编辑模块 -- 指标颜色设置模块视图
 * @author: lizhantong
 * @depend:
 * @date: 2015-03-26
 */

define(
    [
        'dialog',
        'report/edit/canvas/chart-setting/ind-color/ind-color-model',
        'report/edit/canvas/chart-setting/ind-color/ind-color-setting-template',
        'spectrum'
    ],
    function (
        dialog,
        IndColorModel,
        indColorSettingTemplate
    ) {

        //------------------------------------------
        // 视图类的声明
        //------------------------------------------
        var View = Backbone.View.extend({
            events: {
                'click .j-set-ind-color': 'getIndColorList'
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

                that.model = new IndColorModel({
                    canvasModel: option.canvasView.model,
                    reportId: option.reportId
                });
                this.model.set('compId', this.$el.find('.j-comp-setting').attr('data-comp-id'));
            },
            /**
             * 获取topn数据信息
             *
             * @param {event} event 点击事件
             * @public
             */
            getIndColorList: function (event) {
                var that = this;
                that.model.getCompAxis(function (data) {
                    that._openIndColorDialog(data);
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
             * 打开topn设置弹出框
             *
             * @param {Object} data
             * @private
             */
            _openIndColorDialog: function(data) {
                var that = this,
                    html;

                if ($.isEmptyObject(data.indList)) {
                    dialog.alert('没有指标');
                    return;
                }

                html = indColorSettingTemplate.render(data);
                dialog.showDialog({
                    title: '指标颜色设置',
                    content: html,
                    dialog: {
                        width: 340,
                        height: 300,
                        resizable: false,
                        buttons: [
                            {
                                text: '提交',
                                click: function () {
                                    that._saveIndColorInfo($(this));
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
                var colors =  [
                    '#A5D6D2', '#4cc6f7', '#C1232B', '#B5C334', '#FCCE10',
                    '#E87C25', '#27727B', '#FAD860', '#F3A43B', '#60C0DD'
                ];
                var defaultOption = {
                    color: '#A5D6D2',
                    showInput: true,
                    showAlpha: true,
                    className: 'full-spectrum',
                    showInitial: true,
                    showPalette: true,
                    showSelectionPalette: true,
                    maxPaletteSize: 10,
                    preferredFormat: 'hex',
                    localStorageKey: 'spectrum.demo',
//                    showPaletteOnly: true,
//                    togglePaletteOnly: true,
//                    togglePaletteMoreText: 'more',
//                    togglePaletteLessText: 'less',
                    palette: [
                        ['#A5D6D2', '#C1232B', '#B5C334', '#4cc6f7','#FCCE10'],
                        ['#E87C25', '#27727B', '#FAD860', '#F3A43B', '#60C0DD'],
                        ['#D7504B', '#C6E579', '#F4E001', '#F0805A', '#26C0C0']
                    ]
                };
                var texts = $('.ind-color').find('input');
                texts.each(function(i) {
                    var ind = data.indList[$(this).attr('name')];
                    var color;
                    ind && (color = ind.color)
                    var option =  $.extend(true, {}, defaultOption);
                    option.color = colors[i];
                    if (color) {
                        option.color = color;
                    }
                    $(this).spectrum(option);
                    $(this).attr('color',  option.color);
                });
                $('.sp-input').focusin(function (e) {e.stopPropagation()});
            },
            /**
             * 保存topn设置信息
             *
             * @param {$HTMLElement} $dialog 弹出框$el元素
             * @private
             */
            _saveIndColorInfo: function ($dialog) {
                var texts = $('.ind-color').find('input');
                var data = {};

                texts.each(function () {
                    var $this = $(this);
                    var name = $this.attr('name');
                    data[name] = ($this.val() === '') ? $this.attr('color') : $this.val();
                });
                this.model.saveIndColorInfo(data, function () {
                    $dialog.dialog('close');
                    window.dataInsight.main.canvas.showReport();
                });
            }
    });

    return View;
});