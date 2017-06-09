/**
 * @file: 固定报表 Model
 * @author: lizhantong
 * @depend:
 * @date: 2015-08-03
 */


define(['url'], function (Url) {

    //------------------------------------------
    // 模型类的声明
    //------------------------------------------

    /* globals Backbone */
    var Model = Backbone.Model.extend({

        /**
         * 默认属性
         */
        defaults: {
            granularityList: {
                parent: {
                    D: '每日',
                    W: '每周',
                    M: '每月',
                    Q: '每季',
                    Y: '每年'
                },
                child: {}
            }
        },

        /**
         * 构造函数
         *
         * @constructor
         */
        initialize: function () {
            var granularityList = this.get('granularityList');
            var week = {};
            var month = {};
            for (var i = 1; i <= 7; i ++) {
                week['W' + i] = i;
            }
            for (var i = 1; i <= 28; i ++) {
                month['M' + i] = i;
            }
            month.lastDay = '最后一天';
            granularityList.child.W = week;
            granularityList.child.M = month;
            granularityList.child.Q = {
                Q1: 1,
                lastDay: '最后一天'
            };
            granularityList.child.Y = {
                Y1: 1,
                lastDay: '最后一天'
            };
        },

        /**
         * 获取任务管理列表
         *
         * @param {Function} success 回调函数
         *
         * @public
         */
        getFixReportTaskMgrList: function (success) {
            $.ajax({
                url: Url.getFixReportTaskMgrList(this.get('reportId')),
                type: 'get',
                success: function (data) {
                    var sourceData = {};
                    sourceData.taskMgrList = data.data || [];
                    success(sourceData);
                }
            });
        },

        /**
         * 启动任务
         *
         * @param {string} taskId 任务Id
         * @param {string} taskStatus 任务状态
         * @param {Function} success 回调函数
         *
         * @public
         */
        startTask: function (taskId, taskStatus, success) {
            $.ajax({
                url: Url.startTask(this.get('reportId'), taskId, taskStatus),
                type: 'post',
                success: function (data) {
                    success(data.statusInfo);
                }
            });
        },

        /**
         * 删除任务
         *
         * @param {string} taskId 任务Id
         * @param {Function} success 回调函数
         *
         * @public
         */
        delTask: function (taskId, success) {
            $.ajax({
                url: Url.delTask(this.get('reportId'), taskId),
                type: 'delete',
                success: function (data) {
                    success(data.statusInfo);
                }
            });
        },

        /**
         * 保存任务
         *
         * @param {Object} formData 请求参数
         * @param {Function} success 回调函数
         *
         * @public
         */
        saveTaskInfo: function (formData, success) {
            $.ajax({
                url: Url.saveTaskInfo(this.get('reportId')),
                data: formData,
                type: 'post',
                success: function (data) {
                    success(data);
                }
            });
        },


        /**
         * 获取任务设置信息
         *
         * @param {string} taskId 任务Id
         * @param {Function} success 回调函数
         *
         * @public
         */
        getTaskInfo: function (taskId, success) {
            var that = this;
            $.ajax({
                url: Url.getTaskInfo(this.get('reportId'), taskId),
                type: 'get',
                success: function (data) {
                    that.getParamData(taskId, success, data.data);
                }
            });
        },

        /**
         * 获取参数信息
         *
         * @param {string} taskId 任务Id
         * @param {Function} success 回调函数
         * @param {Object} taskInfo 任务信息
         *
         * @public
         */
        getParamData: function (taskId, success, taskInfo) {
            var that = this;
            var formData = {
                taskId: taskId
            };
            $.ajax({
                url: Url.getParamData(this.get('reportId')),
                data: formData,
                type: 'get',
                success: function (data) {
                    var targetData = {};
                    targetData.taskInfo = taskInfo;
                    targetData.paramList = data.data;
                    // 参数设置信息
                    if (!targetData.taskInfo.executeStrategy) {
                        targetData.taskInfo.executeStrategy = {
                            hour: '',
                            minute: '',
                            granularity: 'D'
                        };
                    }
                    var granularity = targetData.taskInfo.executeStrategy.granularity;
                    var granularityList = that.get('granularityList');
                    targetData.taskInfo.granularityList = granularityList;
                    targetData.taskInfo.granularityList.selectChild = granularityList.child[granularity];
                    success(targetData);
                }
            });
        }

    });

    return Model;
});
