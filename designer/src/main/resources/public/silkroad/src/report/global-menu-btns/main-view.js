/**
 * @file 工具栏菜单-view
 * @author weiboxue(wbx_901118@sina.com)
 * @date 2014-12-24
 */
define([
        'dialog',
        'report/global-menu-btns/component-menu-template',
        'report/global-menu-btns/main-model',
        'report/global-menu-btns/fix-report/fix-report-view'
    ],
    function (
        dialog,
        ComponentMenuTemplate,
        MenuMainModel,
        FixReportView
    ) {
        return Backbone.View.extend({
            // view事件绑定
            events: {
                'click .j-global-component': 'shiftMenu',
                'click .j-button-skin': 'shiftMenu',
                'click .j-skin-btn': 'chanceTheme',
                'click .reportName': 'editReportName'
                //'click .j-button-line': 'referenceLine'
            },
            // 当前报表名字
            nowReportName: '',
            /**
             * 构造函数
             *
             */
            initialize: function (option) {
                this.model = new MenuMainModel();
                var Model = this.model;
                this.canvasView = option.canvasView;

                this.fixReportView = new FixReportView({
                    el: this.el,
                    canvasView: this.canvasView,
                    reportId: option.reportId
                });

                // 工具条按钮区域按钮添加
                this.$el.find('.j-global-btn').html(this.createBtns());
                // 工具条菜单区域菜单添加
                this.$el.find('.j-global-menu').html(ComponentMenuTemplate.render());
                // 初始化报表名字
                this.$el.find('.reportName').ready(function () {
                    Model.editReportName(window.dataInsight.main.id);
                });
                // 初始化修改报表名称操作
                this.changeReportName();
            },

            /**
             * 功能区域切换菜单
             *
             * @public
             */
            shiftMenu : function (event) {
                var nowid = $(event.target).parent().attr('id');
                var $menu = $('.global-menus').not('#' + nowid);
                $menu.hide();
                var $nowmenu = $('.comp-menu').find('#' + nowid);
                if ($nowmenu.css('display') == 'none') {
                    $nowmenu.show();
                }
                else {
                    $nowmenu.hide();
                }

            },

            /**
             * 更换皮肤
             *
             * @public
             */
            chanceTheme : function (event) {
                // 皮肤类型
                var type = '';
                var $this = $(event.target);
                // 报表id
                var reportId = window.dataInsight.main.id;
                if ($this.attr('class').indexOf('j-skin-btn') != -1) {
                    type = $this.attr('id');
                }
                else {
                    type = $this.parent().attr('id');
                }
                this.model.getSkinType(reportId, type);
                // 更换link里面的路径
                $('.link-skin').attr(
                    'href', 'asset/'
                    + type
                    + '/css/-di-product-debug.css');
//                $('.skin-menu').hide();

                // 更换线上link里面的路径
//                $('.link-skin').attr(
//                    'href', 'asset/'
//                    + type
//                    + '/css/-di-product-min.css');
//                $('.skin-menu').hide();
                // 换肤后刷新报表，完善ecui控件样式更换
                this.canvasView.showReport();
            },

            /**
             * 参考线开关
             *
             * @public
             */
            referenceLine : function () {
                var imglineurl = 'url(' + '/silkroad/src/css/img/grid.png)';
                var imgemptyurl = 'url(' + '/silkroad/src/css/img/grid-empty.png)';
                var $report = $('.report');
                console.log($report.css('background-image'));
                if ($report.css('background-image') != imglineurl) {
                    $report.css('background-image', imgemptyurl);
                    dialog.alert('背景参考线已关闭');
                }
                else {
                    $report.css('background-image', imglineurl);
                    dialog.alert('背景参考线已打开');
                }
                /**
                // 获取全部参考线
                var $line = $('.j-guide-line');
                var lineNum = $line.length;
                // 根据参考线个数进行对应操作
                if (lineNum == 0) {
                    dialog.warning('未添加组件，或未找到参考线请添加组件并重试');
                }
                else {
                    if ($line.is(':visible')) {
                        $line.hide();
                        dialog.alert('参考线已关闭，再次点击启用。');
                    }
                    else {
                        $line.show();
                        dialog.alert('参考线已打开，再次点击关闭。');
                    }
                }
                **/
            },

            // 参数区域按钮属性
            btnBox: [
                {
                    id: 'para',
                    picName: 'para',
                    title: '参数维度设置',
                    className: 'global-para'
                },
                {
                    id: 'fix-report',
                    picName: 'fix_report',
                    title: '固定报表',
                    className: 'fix-report'
                },
                {
                    id: 'component',
                    picName: 'component',
                    title: '组件工具箱',
                    className: 'global-component'
                },
                {
                    id: 'save-report',
                    picName: 'save',
                    title: '保存',
                    className: 'button-save-report'
                },
                {
                    id: 'close-report',
                    picName: 'close',
                    title: '关闭',
                    className: 'button-close-report button-right'
                },
                {
                    id: 'preview-report',
                    picName: 'preview',
                    title: '预览',
                    className: 'button-preview-report'
                },
                {
                    id: 'skin-report',
                    picName: 'skin',
                    title: '换肤设置',
                    className: 'button-skin'
                }
                /*
                {
                    id: 'reference-line',
                    picName: 'line',
                    title: '参考线设置',
                    className: 'button-line'
                }
                */
            ],
            /**
             * 创建按钮函数
             */
            createBtns: function () {
                var div = '';
                var btnBox = this.btnBox || [];
                if (btnBox.length == 0) {
                    div = '';
                }
                else {
                    for(var i = 0; i < btnBox.length; i ++) {
                        div += (
                            "<div class='global-setting-btns j-" +
                            btnBox[i].className +  "'" +
                            "title='" + btnBox[i].title + "'" + "id='" +
                            btnBox[i].id + "'>" +
                            "<img src='../silkroad/src/css/img/global-btns/btn_" + btnBox[i].picName +".png' />" +
                            "</div>" );
                    }
                }
                // 更改名称区域
                div += (
                    '<div class="reportNameBox"><div class="reportName"></div>'
                    + '<input type="text" class="reportSetName"/></div>'
                    );
                return div;
            },
            /**
             * 更改报表名称切换为编辑状态
             */
            editReportName: function () {
                var $reportSetName = $('.reportSetName');
                var $reportName = $('.reportName');
                $reportSetName.val($reportName.html());
                $reportName.hide();
                $reportSetName.show();
            },
            /**
             * 更改报表名称提交修改
             */
            changeReportName: function () {
                var $reportSetName = $('.reportSetName');
                var $reportName = $('.reportName');
                // 保存原始报表名称
                var originalReportName = $reportName.text();
                var newReportname = null;
                $reportSetName.keydown(function (ev) {
                var oEvent = ev || event;
                // 回车提交修改
                if (oEvent.keyCode == 13) {
                    // 获取更改后的新名称
                    newReportname = $reportSetName.val();
                    // TODO: 此逻辑需要写到model
                    $.ajax({
                        type: "POST",
                        dataType: "json",
                        cache: false,
                        timeout: 10000,
                        url: "reports/" + window.dataInsight.main.id+ "/name/" + newReportname,
                        success: function(data) {
                            // 根据返回值进行判断
                            if (data["status"] === 0) {
                                $reportName.html(newReportname).show();
                                $reportSetName.hide();
                                dialog.success(data["statusInfo"]);
                            }
                            else {
                                $reportName.html(originalReportName).show();
                                $reportSetName.hide();
                                dialog.error(data["statusInfo"]);
                            }
                        }
                    });
                }
                });
            }
        });
    }
);