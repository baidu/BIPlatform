/**
 * @file: 维度组管理
 * @author 赵晓强(longze_xq@163.com)
 * @date 2014-10-22
 */
define(
    [
        'template',
        'dialog',
        'report/edit/setting/dim-group-mgr/mgr-template',
        'report/edit/setting/dim-group-mgr/mgr-model'
    ],
    function (
        template,
        dialog,
        mgrTemplate,
        Model
    ) {

    return Backbone.View.extend({

        /**
         * 构造函数
         *
         * @param {Object} option 配置项
         * @constructor
         */
        initialize: function (option) {
            this.settingView = option.settingView;
            //this.model = new Model();
        },

        /**
         * 打开衍生指标管理窗口
         *
         * @public
         */
        openDialog: function () {
            var html = mgrTemplate.render({
                dimList: this.settingView.model.get('dimList')
            });

            dialog.showDialog({
                dialog: {
                    height: 405,
                    width: 600,
                    modal: false,
                    resizable: false
                },
                content: html,
                title: '维度组管理'
            });

        }
    });
});