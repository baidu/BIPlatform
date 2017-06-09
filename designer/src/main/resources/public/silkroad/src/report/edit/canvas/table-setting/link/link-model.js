/**
 * @file: 报表新建（编辑）-- 表格组件编辑模块 -- 跳转设置模块model
 * @author: lizhantong
 * @date: 2015-06-10
 */


define(['url'], function (Url) {

    //------------------------------------------
    // 模型类的声明
    //------------------------------------------

    var Model = Backbone.Model.extend({
        defaults: {
            operationColumnId: ''
        },
        initialize: function () {},

        /**
         * 获取列跳转平面表设置数据
         *
         * @param {Function} success 回调函数
         * @public
         */
        getColumnLinkPlaneList: function (success) {
            var that = this;
            $.ajax({
                url: Url.getColumnLinkPlaneList(that.get('reportId'), that.get('compId')),
                type: 'get',
                success: function (data) {
                    success(data.data);
                    if (data.data && data.data.planeTableList) {
                        that.set('planeTableList', data.data.planeTableList);
                    }
                    if (data.data && data.data.operationColumn) {
                        var operationColumn = data.data.operationColumn;
                        var operationColumnId = operationColumn[operationColumn.length - 1].value;
                        that.set('operationColumnId', operationColumnId);
                    }
                }
            });
        },

        /**
         * 获取列跳转平面表设置数据
         *
         * @param {Function} success 回调函数
         * @public
         */
        getParamSetList: function (param, success) {
            var that = this;
            $.ajax({
                url: Url.getParamSetList(that.get('reportId'), that.get('compId')),
                type: 'get',
                data: param,
                success: function (data) {
                    that.set('paramList', data.data);
                    success(data.data);
                }
            });
//            var data = {
//                "olapTableDimList": [
//                    {"text": "文本1", "value": "text1"},
//                    {"text": "文本2", "value": "text2"},
//                    {"text": "文本3", "value": "text3"}
//                ],
//                "planeTableParamList": [
//                    {
//                        "name": "平面表1",
//                        "selectedDim": "text1"
//                    },
//                    {
//                        "name": "平面表2",
//                        "selectedDim": "text2"
//                    },
//                    {
//                        "name": "平面表3",
//                        "selectedDim": "text3"
//                    }
//                ]
//            };
//            success(data);
        },

        /**
         * 保存列跳转平面表设置
         *
         * @param {Function} success 回调函数
         * @public
         */
        saveColumnTableRelation: function (data, success) {
            var that = this;
            $.ajax({
                url: Url.getColumnLinkPlaneList(that.get('reportId'), that.get('compId')),
                type: 'POST',
                data: data,
                success: function () {
                    success();
                }
            });
        },

        /**
         * 保存参数设置信息
         *
         * @param {Function} success 回调函数
         * @public
         */
        saveParamRelation: function (data, success) {
            var that = this;
            $.ajax({
                url: Url.getParamSetList(that.get('reportId'), that.get('compId')),
                type: 'POST',
                data: data,
                success: function () {
                    success();
                }
            });
        },

        /**
         * 删除操作列
         * @param {string} linkId 链接id
         * @param {Function} success 回调函数
         *
         * @public
         *
         */
        delOperationColumn: function (linkId, success) {
            var that = this;
            $.ajax({
                url: Url.delColumnLink(that.get('reportId'), that.get('compId'), linkId),
                type: 'DELETE',
                success: function () {
                    success();
                }
            });
        }

    });

    return Model;
});
