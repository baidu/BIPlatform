/**
 * @file: 报表新建（编辑）-- 图形组件编辑模块 -- 指标设置模块model
 * @author: lizhantong
 * @depend:
 * @date: 2015-03-25
 */


define(['url', 'core/helper'], function (Url, Helper) {

    //------------------------------------------
    // 模型类的声明
    //------------------------------------------

    var Model = Backbone.Model.extend({
        defaults: {},
        initialize: function () { },
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
                    that.getIndColorList(data.data, func);
                }
            });
        },
        /**
         * 获取数指标颜色设置数据
         *
         * @param {Function} success 回调函数
         * @public
         */
        getIndColorList: function (indDimList, func) {
            var that = this;
            $.ajax({
                url: Url.getIndColorList(that.get('reportId'), that.get('compId')),
                type: 'get',
                success: function (data) {
                    var sourceData = data.data;
                    var targetData = { indList: {} };
                    var inds = indDimList.candInds;
                    if(inds) {
                        for(var i = 0, len = inds.length; i < len; i ++) {
                            var name = inds[i].name;
                            targetData.indList[name] = {};
                            targetData.indList[name].caption = inds[i].caption;
                            if(sourceData.hasOwnProperty(name)) {
                                targetData.indList[name].color = sourceData[name];
                            }
                            else {
                                targetData.indList[name].color = null;
                            }
                        }
                    }
                    func(targetData);
                }
            });
        },
        /**
         * 提交指标颜色设置信息
         *
         * @param {Function} success 回调函数
         * @public
         */
        saveIndColorInfo: function (data, success) {
            var compId = this.get('compId');
            var formData = {
                areaId: compId,
                colorFormat: JSON.stringify(data)
            };
            $.ajax({
                url: Url.getIndColorList(this.get('reportId'), compId),
                type: 'POST',
                data: formData,
                success: function () {
                    success();
                }
            });
        }
    });

    return Model;
});
