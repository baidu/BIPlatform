/**
 * @file 工具栏菜单-view
 * @author weiboxue(wbx_901118@sina.com)
 * @date 2014-12-24
 */
define([
        'report/global-menu-btns/component-menu-template',
        'report/global-menu-btns/main-model'
    ],
    function (
        ComponentMenuTemplate,
        MenuMainModel
        ) {
        return Backbone.View.extend({
            /**
             * 构造函数
             *
             */
            initialize: function () {
                this.model = new MenuMainModel();
            },

            /**
             * 组件区域下拉框
             *
             */
            componentMenu: function () {
                return ComponentMenuTemplate.render();
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
                    'href', '/silkroad/asset/'
                    + type
                    + '/css/-di-product-debug.css');
                $('.skin-menu').hide();
            }

        });
    }
);