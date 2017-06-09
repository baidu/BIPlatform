/**
 * @file: 报表新建（编辑）-- 图形组件编辑模块 -- 双坐标轴设置模块model
 * @author: weiboxue
 * @depend:
 * @date: 2015-03-27
 */


define(['url'], function (Url) {

    //------------------------------------------
    // 模型类的声明
    //------------------------------------------

    var Model = Backbone.Model.extend({
        defaults: {},
        initialize: function () { },
        /**
         * 提交双坐标轴数据
         *
         * @param {Function} success 回调函数
         * @public
         */
        saveAxisInfo: function (data, success) {
            var formData = {
                position: JSON.stringify(data)
            };
            $.ajax({
                url: Url.getAxisList(this.get('reportId'), this.get('compId')),
                type: 'POST',
                data: formData,
                success: function () {
                    success();
                }
            });
        },
        /**
         * 获取组件的数据关联配置（指标、维度、切片）
         *
         * @param {string} compId 组件id
         * @param {Function} success 数据load完成后的回调函数
         * @public
         */
        getCompAxis: function (func) {
            var that = this;
            $.ajax({
                url: Url.getCompAxis(this.get('reportId'), this.get('compId')),
                success: function (data) {
                    that.getAxisList(data.data, func);
                }
            });
        },
        /**
         * 获取双坐标轴设置信息
         *
         * @param {Function} success 回调函数
         * @public
         */
        getAxisList: function (indDimList, success) {
            var that = this;
            $.ajax({
                url: Url.getAxisList(that.get('reportId'), that.get('compId')),
                type: 'get',
                success: function (data) {
                    var sourceData = data.data;
                    var targetData = { dim: [] };
                    var inds = indDimList.candInds;
                    if(inds) {
                        for(var i = 0, len = inds.length; i < len; i ++) {
                            var name = inds[i].name;
                            var obj = {};
                            obj.caption = inds[i].caption;
                            obj.name = inds[i].name;
                            if(sourceData.hasOwnProperty(name)) {
                                obj.axis = sourceData[name];
                            }
                            else {
                                obj.axis = '0';
                            }
                            targetData.dim.push(obj);
                        }
                    }
                    success(targetData);
                }
            });
        }
    });

    return Model;
});
