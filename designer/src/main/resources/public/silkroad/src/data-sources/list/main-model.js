/**
 * @file 数据源列表model
 * @author 赵晓强(longze_xq@163.com)
 * @date 2014-7-17
 */
define(['url'], function (Url) {

    return Backbone.Model.extend({

        /**
         * 加载数据源列表
         *
         * @public
         */
        loadDataSourcesList: function () {
            var that = this;
            $.ajax({
                url: Url.loadDsgroupList(),
                success: function (data) {
                    that.set('dataSourcesList', data.data);
                }
            });
        },

        /**
         * 获取数据源组中活动的数组源
         *
         * @param {Function} success 回调函数
         * @public
         */
        loadDsGroupActive: function (success) {
            var that = this;

            $.ajax({
                url: Url.loadDsGroupActive(),
                success: function (data) {
                    that.set('activeDataSourcesList', data.data);
                    success && success();
                }
            });
        },

        /**
         * 删除某一数据源
         *
         * @param {string} groupId 数据源组id
         * @param {string} dsId 数据源id
         * @public
         */
        deleteDataSources: function (groupId, dsId) {
            var that = this;

            $.ajax({
                url: Url.deleteDataSources(groupId, dsId),
                type: 'DELETE',
                success: function (data) {
                    that.loadDataSourcesList();
                }
            });
        },

        /**
         * 加载某一数据源所含的表(在设置cube模块用到此方法)
         *
         * @param {string} groupId 数据源组id
         * @param {string} dsId 数据源id
         * @param {Function} sucess(Object) 加载成功后的回调函数
         * @public
         */
        loadTables: function (groupId, dsId, success) {
            var that = this;
            $.ajax({
                url: Url.loadTables(groupId, dsId),
                success: function (data) {
                    success(data.data);
                }
            });
        },

        /**
         * 新建数据源组
         *
         * @param {string} dsGroupName 数据源组名称
         * @param {function} success 成功回调函数
         * @public
         */
        addDsGroup: function (dsGroupName, success) {
            $.ajax({
                url: Url.addDsGroup(),
                type: 'POST',
                data: {
                    name: dsGroupName
                },
                success: function (data) {
                    success(data.data.id);
                }
            });
        },

        /**
         * 编辑数据源组
         *
         * @param {string} groupId 数据源组id
         * @param {string} dsGroupName 数据源组名称
         * @param {function} success 成功回调函数
         * @public
         */
        editDsGroup: function (dsGroupId, dsGroupName, success) {
            $.ajax({
                url: Url.editDsGroup(dsGroupId),
                type: 'POST',
                data: {
                    groupName: dsGroupName
                },
                success: function () {
                    success();
                }
            });
        },

        /**
         * 删除数据源组
         *
         * @param {string} dsGroupId 数据源组id
         * @param {function} success 成功回调函数
         * @public
         */
        delDsGroup: function (dsGroupId, success) {
            $.ajax({
                url:  Url.editDsGroup(dsGroupId),
                type: 'DELETE',
                success: function () {
                    success();
                }
            });
        },

        /**
         * 重定活动的数据源
         *
         * @param {string} groupId 数据源组id
         * @param {string} dsGroupName 数据源组名称
         * @param {function} success 成功回调函数
         * @public
         */
        changeDataSourceActive: function (dsGroupId, dsId, success) {
            $.ajax({
                url:  Url.changeDsActive(dsGroupId, dsId),
                type: 'POST',
                success: function () {
                    success && success();
                }
            });
        }

    });

});