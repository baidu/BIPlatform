/**
 * @file 处理一些改善用户体验的操作，并且这些操作不涉及与后台的数据交互
 *
 * @author 赵晓强(longze_xq@163.com)
 * @date 2014-8-1
 */
define(function () {

    return Backbone.View.extend({
        events: {
            'click .j-group-title .j-icon-fold': 'foldDimGroup'
        },

        /**
         * 构造函数
         *
         * @param {$HTMLElement} option.el   主区.j-main
         * @constructor
         */
        initialize: function (option) {
            this.parentView = option.parentView;

            this.initLeftResize();
            this.setSize();
            //this.initEditReportBodyScroll();
        },

        /**
         * 维度组的折叠与展开
         *
         * @param {event} event 折叠维度组
         */
        foldDimGroup: function (event) {
            var $icon = $(event.target);
            var $group = $icon.parents('.j-dim-group');
            var titleHeight = $icon.parents('.j-group-title').outerHeight();
            var groupHeight = $group.outerHeight();
            if (groupHeight > titleHeight + 10) {
                $group.height(titleHeight);
                $icon.removeClass('fs-18').html('+');
            }
            else {
                $group.height('auto');
                $icon.addClass('fs-18').html('－');
            }
        },

        /**
         * 设置左边可拖拽折叠
         *
         * @public
         */
        initLeftResize: function () {
            var space = 7; // 间距
            var $canvas = $('.j-canvas');
            this.$el.find('.j-data-sources-setting').resizable({
                minWidth: 0,
                axis: "x",
                resize: function (event, ui) {
                    var width = ui.helper.width() + space;
                    $canvas.css({'padding-left': width + 'px'});
                }
            }).find('.ui-resizable-s,.ui-resizable-se').remove();
            // 初始化左宽度
            var width = $('.j-data-sources-setting').width() + space;
            $canvas.css({'padding-left': width + 'px'});
        },

        /**
         * 更具页面的大小，调整布局
         *
         * @public
         */
        setSize: function () {
            var globalBtnHeight = 34 + 2; // 34（高度）+ 2（外边距）
            var height = $(window).height() - $('.j-nav').height() - globalBtnHeight;
            $('.j-scroll-data-sources').height(height);
            var $report = $('.j-report');
            var otherHeight = $report.outerHeight(true) - $report.height();
            var $compSetting = $('.j-comp-setting');
            otherHeight = otherHeight + $compSetting.outerHeight(true);
            $report.height(height - otherHeight);
            $('.j-foot').hide();
        },

        /**
         * 初始化编辑报表body上的滚动条(此方法暂时不用)
         * 版权信息的正常显示需要改变左侧数据项容器的高度
         * “发布”按钮的正确位置也需要重新定位
         *
         * @public
         */
        initEditReportBodyScroll: function () {
            var $window = $(window);
            var $scrollDataSources = $('.j-scroll-data-sources');
            var navHeight = $('.j-nav').height();
            var $foot = $('.j-foot');
            var footHeight = $foot.height();
            var $buttons = $('.j-button-publish-report,.j-button-save-report');

            $window.scroll(function () {
                var $this = $(this);
                var height;
                var scrollTop = $this.scrollTop();
                var scrollHeight = $(document).height();
                var windowHeight = $this.height();
                var btnBottom = 7; // 按钮距底部的距离
                var positionBottom;
                // 滚动条距下的位置
                var bottomSpace = scrollHeight - scrollTop - windowHeight;
                // 在底部
                if(bottomSpace <= footHeight){
                    height = windowHeight - navHeight - footHeight + bottomSpace;
                    positionBottom = footHeight - bottomSpace + btnBottom;
                }
                // 为了提高性能，减少dom交互
                else {
                    height = windowHeight - navHeight;
                    positionBottom = 5;
                }
                $scrollDataSources.height(height);
                $buttons.css({'bottom': positionBottom + 'px'});
            });
        }
    });
});