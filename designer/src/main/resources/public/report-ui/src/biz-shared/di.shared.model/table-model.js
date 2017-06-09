/**
 * di.shared.model.TableModel
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:   表格Model的基类，
 *          支持前台分页、排序，后台分页、排序，
 *          各表格页面Model可继承或聚合此类
 * @author: sushuang(sushuang)
 * @depend: xui, xutil
 */

$namespace('di.shared.model');

/**
 * @usage 先调用prepareArgs准备参数（用cmd和changeArgs指定参数），
 *        再调用persistent进行模型刷新，最后用getData获取显示数据。
 *
 * 数据格式说明：
 *    {Object|Array{Object}} sortInfo 排序信息
 *        如果为Object，结构为：
 *        {string} orderby 'asc'或'desc'或空
 *        {string} sortby 根据什么列排序
 *        如果为Array，则示按多列排序，
 *        第一列作为主排序在getData时会被返回，不常用
 *    {(Object|PageInfo)} pageInfo 分页信息
 *        {number} disabled 是否禁用
 *        {number} totalRecordCount 总记录数
 *        {number} pageSize 每页大小
 *        {number} currentPage 当前页号，从1开始
 */
(function() {
    
    //-------------------------------------------
    // 引用
    //-------------------------------------------

    var xobject = xutil.object;
    var xlang = xutil.lang;
    var extend = xobject.extend;
    var inheritsObject = xobject.inheritsObject;
    var q = xutil.dom.q;
    var g = xutil.dom.g;
    var sortList = xutil.collection.sortList;
    var hasValue = xlang.hasValue;
    var isArray = xlang.isArray;
    var XDATASOURCE = xui.XDatasource;
    var DICT = di.config.Dict;
    var URL = di.config.URL;
    var PAGE_INFO;

    $link(function() {
        PAGE_INFO = di.shared.model.PageInfo;
    });

    //-------------------------------------------
    // 类型定义
    //-------------------------------------------

    /**
     * 表格模型基础类
     *
     * @class
     * @extend xui.XDatasource
     */
    var TABLE_MODEL = 
            $namespace().TableModel = 
            inheritsObject(XDATASOURCE, constructor);
    var TABLE_MODEL_CLASS = TABLE_MODEL.prototype;

    TABLE_MODEL_CLASS.DEFAULT_PAGE_SIZE = 20;

    //-------------------------------------------
    // 方法
    //-------------------------------------------

    /**
     * 构造方法
     * 
     * @constructor
     * @private
     */
    function constructor() {
        /**
         * 所有数据
         * （在使用前端分页时，这是数据的全集）
         *
         * @type {Array.<Object>}
         * @private
         */
        this._oDatasource = [];
        /**
         * 数据信息包装（包括当页数据，分页信息，排序信息）
         *
         * @type {Object}
         * @private
         */
        this._oWrap = {
            sortInfo: {},
            pageInfo: new PAGE_INFO(),
            pageData: []
        }
    }

    /**
     * 获取数据（包括当页数据，分页信息，排序信息）
     *
     * @public
     * @return {Object} 显示数据
     *          {Array} pageData
     *          {Object} sortInfo
     *          {PageInfo} pageInfo
     */
    TABLE_MODEL_CLASS.getData = function() {
        var ret = extend({}, this._oWrap);
        ret.sortInfo = this.$getMainSortInfo(ret.sortInfo);
        return ret;
    };

    /**
     * 获得数据信息封装（包括当页数据，分页信息，排序信息）
     *（返回原引用而非副本，派生类中使用）
     *
     * @protected
     * @return {Object} 数据信息封装
     *          {Array} pageData
     *          {Object} sortInfo
     *          {PageInfo} pageInfo     
     */
    TABLE_MODEL_CLASS.$getWrap = function() {
        return this._oWrap;
    };

    /**
     * 获取所有数据
     *
     * @public
     * @return {Array.<Object>} 所有数据
     */
    TABLE_MODEL_CLASS.getDatasource = function() {
        return this._oDatasource || [];
    };

    /**
     * 准备参数
     *
     * @public
     * @param {string} cmd 命令，
     *          默认的有'CMD_INIT', 'CMD_SORT', 
     *          'CMD_PAGE_CHANGE', 'CMD_PAGE_SIZE_CHANGE'
     * @param {Object} changeArgs 需要改变的参数参数，
     *          结构如下，只传需要改变的属性
     *          {(Object|Array.<Object>)} sortInfo
     *          {(Object|PageInfo)} pageInfo
     * @return {Object} initArgs
     *          {Object} sortInfo
     *          {PageInfo} pageInfo
     */
    TABLE_MODEL_CLASS.prepareArgs = function(cmd, changeArgs) {
        var wrap = this._oWrap;
        return this['$' + cmd](cmd, changeArgs);
    };

    /**
     * 持久化Model
     *
     * @public
     * @param {Object} datasource 数据源，如果不传则使用已经持久化的数据源
     * @param {Object} initArgs 初始化参数，根据此参数初始化
     *          {(Object|Array.<Object>)} sortInfo
     *          {(Object|PageInfo)} pageInfo
     * @param {boolean} useRawData 不处理数据（用于后台分页和排序），缺省是false
     */
    TABLE_MODEL_CLASS.persistent = function(datasource, initArgs, useRawData) {
        this._oWrap = extend({}, initArgs);
        this._oDatasource = datasource || this._oDatasource;

        if (useRawData) {
            this._oWrap.pageData = datasource;
        } 
        else {
            this._oWrap.pageInfo.totalRecordCount = this._oDatasource.length;
            this.$sortTable(datasource, this._oWrap.sortInfo);
            this._oWrap.pageData = this.$pagingTable(
                datasource, 
                this._oWrap.pageInfo
            );
        }
    };


    /**
     * 命令处理，生成initArgs，可添加或重载
     *
     * @protected
     */
    TABLE_MODEL_CLASS.$CMD_INIT = function(cmd, changeArgs) {
        var wrap = this._oWrap;
        var initArgs = {};
        var pageSize = wrap.pageInfo.pageSize;
        initArgs.sortInfo = this.$initSortInfo();
        initArgs.pageInfo = this.$initPageInfo();
        pageSize && (initArgs.pageInfo.pageSize = pageSize);
        return initArgs;
    };
    TABLE_MODEL_CLASS.$CMD_SORT = function(cmd, changeArgs) {
        var wrap = this._oWrap;
        var initArgs = {};
        var pageSize = wrap.pageInfo.pageSize;
        if (this.$getMainSortInfo(changeArgs.sortInfo).sortby != 
                this.$getMainSortInfo(wrap.sortInfo).sortby
        ) {
            initArgs.sortInfo = this.$changeSortby(
                this.$getMainSortInfo(changeArgs.sortInfo).sortby
            );
        } else {
            initArgs.sortInfo = wrap.sortInfo;
            this.$getMainSortInfo(initArgs.sortInfo).orderby = 
                this.$changeOrderby(initArgs.sortInfo);
        }
        initArgs.pageInfo = wrap.pageInfo;
        pageSize && (initArgs.pageInfo.pageSize = pageSize);
        return initArgs;
    };
    TABLE_MODEL_CLASS.$CMD_CHANGE_PAGE = function(cmd, changeArgs) {
        var wrap = this._oWrap;
        var initArgs = {};
        var pageSize = wrap.pageInfo.pageSize;
        initArgs.sortInfo = wrap.sortInfo;
        initArgs.pageInfo = wrap.pageInfo;
        initArgs.pageInfo.currentPage = Number(
            changeArgs.pageInfo.currentPage
        );
        pageSize && (initArgs.pageInfo.pageSize = pageSize);
        return initArgs;
    };
    TABLE_MODEL_CLASS.$CMD_CHANGE_PAGE_SIZE = function(cmd, changeArgs) {
        var wrap = this._oWrap; 
        var initArgs = {};
        var pageSize = wrap.pageInfo.pageSize;
        initArgs.sortInfo = this.$initSortInfo();
        initArgs.pageInfo = this.$initPageInfo();
        initArgs.pageInfo.pageSize = Number(
            changeArgs.pageInfo.pageSize
        );
        return initArgs;
    };

    /**
     * 默认的pageInfo初始化，可重载
     *
     * @protected
     * @return {PageInfo} pageInfo
     */
    TABLE_MODEL_CLASS.$initPageInfo = function() {
        return new PAGE_INFO(
            {
                disabled: false,
                currentPage: 1,
                pageSize: this.DEFAULT_PAGE_SIZE
            }
        );
    };

    /**
     * 默认的sortInfo初始化，可重载
     * 
     * @protected
     * @return {(Object|Array.<Object>)} sortInfo
     */
    TABLE_MODEL_CLASS.$initSortInfo = function() {
        return { sortby: null, orderby: null, dataField: null };
    };

    /**
     * 修改sortby，可重载
     * 
     * @protected
     * @param {string} newSortby
     * @return {(Object|Array.<Object>)} sortInfo
     */
    TABLE_MODEL_CLASS.$changeSortby = function(newSortby) {
        return { sortby: newSortby, orderby: null, dataField: newSortby };
    };

    /**
     * 修改orderby，可重载
     * 
     * @protected
     * @param {(Object|Array.<Object>)} oldSortInfo
     * @return {string} orderby
     */
    TABLE_MODEL_CLASS.$changeOrderby = function(oldSortInfo) {
        var sInfo = isArray(oldSortInfo) ? oldSortInfo[0] : oldSortInfo;
        return sInfo.orderby == 'asc' ? 'desc' : 'asc'; 
    };

    /**
     * 表格排序
     * 会更新输入的原数据集和sortInfo的orderby字段
     * 不支持“还原成默认”，只在asc和desc间切换
     * 
     * @protected
     * @param {Array{Object}} datasource
     * @param {(Object|Array.<Object>)} sortInfo
     */
    TABLE_MODEL_CLASS.$sortTable = function(datasource, sortInfo) {
        if (!datasource || !sortInfo) { 
            return; 
        }
        
        var sortInfoArr = isArray(sortInfo) ? sortInfo : [sortInfo];
        for (
            var i = sortInfoArr.length - 1, o, compareFunc; 
            o = sortInfoArr[i]; 
            i --
        ) {
            if (hasValue(o.dataField) && o.orderby) {
                compareFunc = o.orderby == 'asc' ? '<' : '>'; 
                sortList(datasource, o.dataField, compareFunc, false);
            }
        }
    };    
    
    /**
     * 前端表格分页
     * 
     * @protected
     * @param {Array.<Object>} datasource
     * @param {(Object|PageInfo)} pageInfo
     * @return {Array} 当前页数据
     */
    TABLE_MODEL_CLASS.$pagingTable = function(datasource, pageInfo) {
        var start;
        var length;
        var ret = [];
        if (pageInfo.disabled) {
            start = 0;
            length = datasource.length;
        } else {
            start = (pageInfo.currentPage - 1) * pageInfo.pageSize;
            length = pageInfo.pageSize;
        }
        for (
            var i = 0, o; 
            i < length && (o = datasource[start + i]); 
            i ++
        ) {
            ret.push(o);
        }
        return ret;
    };

    /**
     * 得到主sortInfo
     *
     * @protected
     */
    TABLE_MODEL_CLASS.$getMainSortInfo = function(sortInfo) {
        return isArray(sortInfo) ? sortInfo[0] : sortInfo;
    };  

})();

