/**
 * @file: 报表配置端 - 获取已经存储在内存中数据的公共方法
 * @author: lizhantong
 * @date: 2015-03-26
 */

define(function () {

    //--------------------------------
    // 类型声明
    //--------------------------------

    var Helper = {};

    /**
     * 获取当前报表指标数据
     *
     * @param {Function} success 回调函数
     * @public
     * @return {Object} data 指标列表数据
     */
   Helper.getIndList = function () {
       return dataInsight.main.model.get('indList').data;
   };

   return Helper;
});