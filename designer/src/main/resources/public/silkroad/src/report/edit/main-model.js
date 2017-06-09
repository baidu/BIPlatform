/**
 * @file
 * @author 赵晓强(longze_xq@163.com)
 * @date 2014-7-28
 */
define(['url'], function (Url) {

    return Backbone.Model.extend({
        // model 事件绑定
        events: {
            'change:currentCubeId': 'loadIndList'
        },

        /**
         * 构造函数
         *
         * @param {Object} option 初始化配置项
         * @param {boolen} option.isEdit 是否是编辑
         * @constructor
         */
        initialize: function (option) {
            this.runtimeData = {
                isEdit: option.isEdit
            };
            this.listenTo(this, 'change:currentCubeId', this.loadIndList);
            this.listenTo(this, 'change:currentCubeId', this.loadDimList);
        },

        /**
         * 获取运行时报表Id（其实是通知后台做运行时处理，此Id已经对前端做了透明处理）
         *
         * @param {function} success 异步成功后的回调函数
         * @public
         */
        getRuntimeId: function (success) {
            var that = this;

            $.ajax({
                url:Url.getRuntimeId(that.id),
                type: 'POST',
                data: that.runtimeData,
                success: function () {
                    success();
                }
            });
        },

        /**
         * 加载cube列表，在此中还会调用getRuntimeId来生成运行态的报表id
         *
         * @public
         */
        loadCubeList: function () {
            var that = this;
            that.getRuntimeId(function () {
                $.ajax({
                    url: Url.loadCubeList(that.id),
                    success: function (data) {
                        that.set({'cubeList': data.data});
                        if (data.data.length > 0) {
                            that.set({'currentCubeId': data.data[0].id});
                        }
                    }
                });
            });
        },

        /**
         * 加载指标列表
         *
         * @public
         */
        loadIndList: function () {
            var that = this;

            $.ajax({
                url: Url.loadIndList(that.id, that.get('currentCubeId')),
                success: function (data) {
                    map = {
                        'SUM': 'S',
                        'COUNT': 'C',
                        'AVERAGE': 'A',
                        'DISTINCT_COUNT': 'D'
                    };
                    that.set({
                        indList: {
                            data: data.data,
                            map: map
                        }
                    });
                }
            });
        },

        /**
         * 加载维度列表
         *
         * @public
         */
        loadDimList: function () {
            var that = this;

            $.ajax({
                url: Url.loadDimList(that.id, that.get('currentCubeId')),
                success: function (data) {
                    that.set({dimList: data.data.dimList});
                }
            });
        },

        /*
         * 通过指标id获取指标项或维度数据
         *
         * @param {string} id 指标或维度id
         * @param {string} type 指标或维度类型（ind,dim）
         * @public
         * */
        getItemDataById: function (id, type) {
            var that = this;
            var data;

            function get(id, type) {
                var list = that.get(type + 'List');
                if (type == 'ind') {
                    list = list.data;
                }
                for (var i = 0, len = list.length; i < len; i++) {
                    if (list[i].id == id) {
                        return list[i];
                    }
                }
            }

            if (type === undefined) {
                var indItemData = get(id, 'ind');
                data = indItemData ? indItemData : get(id, 'dim');
            }
            else {
                data = get(id, type);
            }

            return data;
        }
    });
});