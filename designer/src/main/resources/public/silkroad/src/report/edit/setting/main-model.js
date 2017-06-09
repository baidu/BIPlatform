/**
 * @file
 * @author 赵晓强(longze_xq@163.com)
 * @date 2014-7-30
 */
define(['url'], function (Url) {

    return Backbone.Model.extend({

        /**
         * 构造函数
         *
         * @param {Object} option 初始化配置项
         * @constructor
         */
        initialize: function (option) {
            this.parentModel = option.parentModel;
        },

        /**
         * 加载设置“数据源展示数据”的原始数据
         *
         * @param {Function} success 加载完成后的回调函数
         * @public
         */
        loadShowData: function (success) {
            var that = this;
            $.ajax({
                url: Url.loadShowData(
                    that.id,
                    that.parentModel.get('currentCubeId')
                ),
                type: 'GET',
                success: function (data) {
                    success(data);
                }
            });
        },

        /**
         * 向后台提交“展示数据”这之后的结果
         *
         * @param {Object} data 从表单处理后的数据
         * @param {Function} closeDialog 关闭弹窗的会点函数
         * @public
         */
        submitSowData: function (data, closeDialog) {
            var that = this;

            $.ajax({
                url: Url.submitSowData(
                    that.id,
                    that.parentModel.get('currentCubeId')
                ),
                type: 'PUT',
                data: data,
                success: function () {
                    closeDialog();
                    that.parentModel.loadIndList();
                    that.parentModel.loadDimList();
                }
            });
        },

        /**
         * 修改汇总方式
         *
         * @param {Object} parentView 父view，对应edit下的main-view
         * @param {Object} indData 指标数据
         * @param {string} aggregatorValue 汇总value
         * @param {Function} success 修改成功后的回调函数
         * @public
         */
        putAggregator: function (parentView, indData, aggregatorValue, success) {
            var that = this;

            $.ajax({
                url: Url.putAggregator(
                    that.id,
                    that.parentModel.get('currentCubeId'),
                    indData.id
                ),
                type: 'PUT',
                data: {
                    'aggregator': aggregatorValue
                },
                success: function () {
                    indData.aggregator = aggregatorValue;
                    var indList = parentView.model.get('indList');
                    var mark = indList.map[aggregatorValue];
                    success(mark);
                }
            });
        },

        /**
         * 重命名指标与维度名
         *
         * @param {string} id 指标或维度的id
         * @param {string} newName 更改后的name
         * @param {string} type 指标：ind，维度：dim
         * @param {Function} success 添加成功后的回调函数
         * @public
         */
        putName: function (id, newCaption, type, success) {
            var that = this;

            $.ajax({
                url: Url.putName(
                    that.id,
                    that.parentModel.get('currentCubeId'),
                    type,
                    id
                ),
                type: 'PUT',
                data: {
                    caption: newCaption
                },
                success: function () {
                    success();
                }
            });
        },

        /**
         * 创建维度组中的子维度像
         *
         * @param {string} groupId 维度id
         * @param {string} dimId 被删除的维度id
         * @param {Function} success 添加成功后的回调函数
         * @public
         */
        deleteSubDim: function (groupId, dimId, success) {
            var that = this;

            $.ajax({
                url: Url.deleteSubDim(
                    that.id,
                    that.parentModel.get('currentCubeId'),
                    groupId,
                    dimId
                ),
                type: 'DELETE',
                success: function (data) {
                    success();
                    // ### 此处有坑 ###
                    // 此处没有对client端的数据进行实时删除，
                    // 如果后续的业务逻辑需要在此处理
                }
            });
        },

        /**
         * 删除维度组
         *
         * @param {string} 维度组Id
         * @param {Function} 删除成功后的会回调函数
         * @public
         */
        deleteDimGroup: function (groupId, success) {

            $.ajax({
                url: Url.deleteDimGroup(
                    this.id,
                    this.parentModel.get('currentCubeId'),
                    groupId
                ),
                type: 'DELETE',
                success: function (data) {
                    success();
                }
            });
        },

        /**
         * 创建维度组
         *
         * @param {string} groupName 维度组名称
         * @public
         */
        createDimGroup: function (groupName){
            var that = this;

            $.ajax({
                url: Url.createDimGroup(
                    that.id,
                    that.parentModel.get('currentCubeId')
                ),
                type: 'POST',
                data: {
                    groupName: groupName
                },
                success: function (data) {
                    that.parentModel.loadDimList();
                }
            });
        }
    });

});