/**
 *
 * @file:    弹窗公共方法
 * 测试文件：test/demo/dialog.htm
 * @author:  lizhantong(lztlovely@126.com)
 */
define(function () {


    //------------------------------------------
    // 对象的声明
    //------------------------------------------

    var Dialog = {};

    /**
     * alert
     * 提示框
     * @public
     * @param {string} content 提示信息
     */
    Dialog.alert = function (content, title, callback) {
        var options = {
            dialog: {
                title: title,
                height: 'auto',
                width: 'auto',
                buttons: {
                    '确定': function () {
                        $(this).dialog("close");
                        callback && callback();
                    }
                }
            },
            title: '提示信息',
            content: content
        };
        dialog(options);
    };

    /**
     * warning
     * 警告提示框
     * @public
     * @param {string} content 提示信息
     */
    Dialog.warning = function (content, callback) {
        content = '<div class="alert-icon alert-warning">' + content + '</div>';
        Dialog.alert(content, '系统警告提示', callback);
    };

    /**
     * 操作成功提示框
     * @param {string} content 提示信息
     * @public
     */
    Dialog.success = function (content, callback) {
        content = '<div class="alert-icon alert-success">' + content + '</div>';
        Dialog.alert(content, '操作成功', callback);
    };

    /**
     * warning
     * 错误提示框
     * @public
     * @param {string} content 提示信息
     */
    Dialog.error = function (content, callback) {
        content = '<div class="alert-icon alert-error">' + content + '</div>';
        Dialog.alert(content, '系统错误提示', callback);
    };

    /**
     * 弹出对话框
     * @param {string} content html片段
     * @param {Function} callback点击提交按钮后的回调事件
     *
     * @public
     */
    Dialog.showDialog = function (o) {
        var options = {
            dialog: {
                height: 130,
                width: 270
            },
            content: '亲,你没输入内容哦...o_o'
        };
        $.extend(true, options, o);
        options.containerstyle = {
            'width': options.dialog.width + 'px',
            'height': options.dialog.height + 'px',
            'overflow': 'auto'
        };
        return dialog(options);
    };

    /**
     * 确认提示框
     * @param {string} content html片段
     * @param {Function} callback点击提交按钮后的回调事件     *
     * @public
     */
    Dialog.confirm = function (content, callback, cancel) {
        var callback = callback || function(){};
        var cancel = cancel || function () {};
        var options = {
            dialog: {
                buttons: {
                    "确定": function () {
                        $(this).dialog("close");
                        callback();
                    },
                    '取消': function () {
                        $(this).dialog("close");
                        cancel();
                    }
                }
            },
            title: '确定',
            content: content
        };
        dialog(options);
    };

    /**
     * dialog
     * @param {string} content html片段
     * @param {Function} callback点击提交按钮后的回调事件
     * @public
     */
    function dialog(options) {
        var diaOptions = {
            autoOpen: true,
            height: undefined,
            width: undefined,
            modal: true,
            close: function () {
                $(this).remove();
            }
        };
        var diaOptions = $.extend(true, diaOptions, options.dialog);
        var title = options.title || '&nbsp;';
        var html = [
            '<div title="', title, '">',
            options.content,
            '</div>'
        ].join('');
        var $container = $(html);
        options.containerstyle && $container.css(options.containerstyle);
        $container.appendTo($(document.body));
        $container.dialog(diaOptions);
        return $container;
    }

    return Dialog;
})
;
   
   
  