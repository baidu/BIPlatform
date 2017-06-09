/**
 *
 * @file: 导航菜单模块View
 * 测试文件 test/demo/nav/index.html
 * @author: lizhantong(lztlovely@126.com)
 * @depend: nav-template,dialog
 * @date: 2014-07-03
 */

define(['nav/nav-template', 'dialog'], function (template, dialog) {

    //------------------------------------------
    // 引用
    //------------------------------------------

    var alert = dialog.alert;

    //------------------------------------------
    // 常量
    //------------------------------------------

    /**
     * 菜单对应模块的初始化方法后缀
     *
     * @const
     * @type {string}
     */
    var MUNE_METHOD_PREFIX = '_init';

    //------------------------------------------
    // 视图类的声明
    //------------------------------------------

    /**
     * 导航菜单视图类
     *
     * @class
     */
    var View = Backbone.View.extend({

        //------------------------------------------
        // 公共方法区域
        //------------------------------------------

        /**
         * 构造函数
         *
         * @constructor
         */
        initialize: function () {
            this.listenTo(
                this.model,
                'change:currentMenu',
                this._currentMenuChanged
            );
            this.render();
            // 暂时隐藏其他按钮
            this.hideOther();
            // 获取当前选中菜单，并初始化其对应模块
            this._currentMenuChanged();
            window.dataInsight.navView = this;
        },

        /**
         * 渲染
         * 根据模版渲染dom元素，绑定事件
         *
         * @public
         */
        render: function () {
            var me = this;
            var model = me.model;
            var data = model.getNavData();

            $(me.el).html(template.render(data));
            me._bindEvents();
        },

        /**
         * 销毁
         * @public
         */
        destroy: function () {
            this.stopListening();
            // 删除model
            this.model.clear({silent: true});
            // 删除外挂事件
            delete window.dataInsight.navView;
        },

        //------------------------------------------
        // 私有方法区域
        //------------------------------------------

        /**
         * 绑定导航菜单事件
         * 导航菜单的点击事件
         *
         * @private
         */
        _bindEvents: function () {
            var currMenuId;
            var me = this;
            var model = me.model;
            var el = $(me.el);
            var $navContainer = el.find('.nav-main');

            // 为导航菜单绑定点击事件
            $navContainer.on('click.navMenu', '.nav-menu', function () {
                var $this = $(this);
                var id = $this.attr('id');
                // 如果点击的是当前选中项，返回
                currMenuId = model.getCurrentMenuId();
                if (currMenuId === id) {
                    return;
                }

                model.setCurrentMenuId(id);
                $this.addClass('nav-menu-focus')
                    .siblings()
                    .removeClass('nav-menu-focus');
            });
        },

        /**
         * 改变选中菜单
         *
         * @private
         */
        _currentMenuChanged: function () {
            var currMenuId;
            var firstCode;
            var model = this.model;

            currMenuId = model.getCurrentMenuId();
            firstCode = currMenuId.charAt(0).toUpperCase();
            currMenuId = firstCode + currMenuId.slice(1, currMenuId.length);

            this[MUNE_METHOD_PREFIX + currMenuId]();  // 初始化对应模块
        },

        //------------------------------------------
        // 外部接口区域
        //------------------------------------------

        /**
         * 调用数据源管理模块接口，进行初始化
         *
         * @private
         */
        _initDataSourceManager: function () {
            this._destroyPanel();
            require(['data-sources/list/main-view'], function (ReportListView) {
                new ReportListView({el: $('.j-main')});
            });
        },

        /**
         * 调用报表管理模块接口，进行初始化
         *
         * @private
         */
        _initReportManager: function () {
            this._destroyPanel();
            require(['report/list/main-view'], function (ReportListView) {
                new ReportListView({el: $('.j-main')});
            });
        },

        /**
         * 调用报表管理模块接口，进行初始化
         * @private
         */
        _initOther: function () {
            alert('正在开发，请耐心等待......');
        },

        /**
         * 调用面板模块销毁方法
         *
         * @private
         */
        _destroyPanel: function () {
            window.dataInsight && window.dataInsight.main
            && window.dataInsight.main.destroy();
        },

        /**
         * 暂时隐藏其他按钮
         *
         * @private
         */
        hideOther: function () {
            $('#other').remove();
        }

    });

    return View;
});