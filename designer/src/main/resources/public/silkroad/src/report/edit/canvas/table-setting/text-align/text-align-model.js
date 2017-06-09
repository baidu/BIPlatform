/**
 * @file: 报表新建（编辑）-- 表格组件编辑模块 -- 文本对齐设置模块model
 * @author: lizhantong
 * @depend:
 * @date: 2015-04-27
 */


define(['url'], function (Url) {

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
                    that.getTextAlignList(data.data, func);
                }
            });
        },
        /**
         * 获取数指标颜色设置数据
         *
         * @param {Function} success 回调函数
         * @public
         */
        getTextAlignList: function (indDimList, func) {
            var that = this;
            $.ajax({
                url: Url.getTextAlignList(that.get('reportId'), that.get('compId')),
                type: 'get',
                success: function (data) {
                    var sourceData = data.data;
                    var targetData = {
                        options: {
                            'left': '居左',
                            'center': '居中',
                            'right': '居右'
                        },
                        indList: {}
                    };
                    var inds = indDimList.yAxis;
                    if (inds) {
                        for (var i = 0, len = inds.length; i < len; i ++) {
                            var name = inds[i].name;
                            targetData.indList[name] = {};
                            targetData.indList[name].caption = inds[i].caption;
                            if (sourceData
                                && sourceData.indList
                                && sourceData.indList.hasOwnProperty(name)) {
                                targetData.indList[name].align = sourceData.indList[name];
                            }
                            else {
                                targetData.indList[name].align = 'left';
                            }
                        }
                    }
//                    if(dims) {
//                        for(var i = 0, len = dims.length; i < len; i ++) {
//                            var name = dims[i].name;
//                            targetData.dimList[name] = {};
//                            targetData.dimList[name].caption = dims[i].caption;
//                            if(sourceData && sourceData.hasOwnProperty(name)) {
//                                targetData.dimList[name].align = sourceData[name];
//                            }
//                            else {
//                                targetData.dimList[name].align = 'left';
//                            }
//                        }
//                    }
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
        saveTextAlignInfo: function (data, success) {
            var compId = this.get('compId');
            var formData = {
                areaId: compId,
                textFormat: JSON.stringify(data)
            };
            $.ajax({
                url: Url.getTextAlignList(this.get('reportId'), compId),
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
