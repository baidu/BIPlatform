/**
 * di.shared.model.PageInfo
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:   分页信息对象
 *          可屏蔽前后台对分页对象的定义不一致的情况
 * @author: sushuang(sushuang)
 */

$namespace('di.shared.model');

(function() {
    
    //---------------------------------------
    // 引用
    //---------------------------------------
    
    var textParam = xutil.url.textParam;
    var clone = xutil.object.clone;
        
    //---------------------------------------
    // 类型声明
    //---------------------------------------

    /**
     * 分页对象
     *
     * @class
     * @constructor
     * @param {(Object|PageInfo)=} pageInfo 分页信息，可缺省
     *          {number} disabled 是否禁用
     *          {number} totalRecordCount 总记录数
     *          {number} pageSize 每页大小
     *          {number} currentPage 当前页号，从1开始
     */
    var PAGE_INFO = $namespace().PageInfo = function(pageInfo) {
        /**
         * 是否禁用
         *
         * @type {boolean}
         * @public
         */
        this.disable;
        /**
         * 总记录数
         *
         * @type {number}
         * @public
         */
        this.totalRecordCount;
        /**
         * 每页大小
         *
         * @type {number}
         * @public
         */
        this.pageSize;

        this.setData(pageInfo);
    };
    var PAGE_INFO_CLASS = PAGE_INFO.prototype;
        
    /**
     * 设置数据
     * 
     * @public
     * @param {Object} pageInfo 分页信息，
     *          如果pageInfo某个属性没有值，则此属性不会被设值改动
     *          {number} disabled 是否禁用
     *          {number} totalRecordCount 总记录数
     *          {number} pageSize 每页大小
     *          {number} currentPage 当前页号，从1开始
     */
    PAGE_INFO_CLASS.setData = function(pageInfo) {
        if (pageInfo) {
            if (pageInfo.disabled != null) {
                this.disabled = pageInfo.disabled;
            }
            if (pageInfo.totalRecordCount != null) {
                this.totalRecordCount = pageInfo.totalRecordCount;
            }
            if (pageInfo.pageSize != null) {
                this.pageSize = pageInfo.pageSize;
            }
            if (pageInfo.currentPage != null) {
                this.currentPage = pageInfo.currentPage;
            }
        }
    };

    /**
     * 用后台数据设置page info
     * 
     * @public
     * @param {Object} serverPageInfo 后台page info的json对象
     * @param {string=} type 后台page bean类型，
     *              可取值：'TCOM', 
     *              为空则是默认模式
     */
    PAGE_INFO_CLASS.setServerData = function(serverPageInfo, type) {
        var pageInfo;

        switch (type) {
            case 'TCOM': 
                pageInfo = {};
                if (serverPageInfo) {
                    pageInfo.disabled = false;
                    pageInfo.totalRecordCount = 
                        parseInt(serverPageInfo.totalRecNum) || 0;
                    pageInfo.pageSize = 
                        parseInt(serverPageInfo.pageSize) || 0;
                    pageInfo.currentPage = 
                        parseInt(serverPageInfo.curPageNum) || 0;
                }
                break;

            default:
                pageInfo = serverPageInfo;
        }

        this.setData(pageInfo);
    };

    /**
     * 得到请求server的参数
     * 
     * @public
     * @param {string=} prefix 参数名前缀，如: 
     *              请求参数想要为'model.page.cur_page_num ...'，
     *              则此参数可传'model.page.',
     *              缺省为'page.'
     * @param {string=} type 后台page bean类型，
     *              可取值：'TCOM', 
     *              为空则是默认模式
     * @return {string} 后台的page info的请求参数
     */
    PAGE_INFO_CLASS.getServerParam = function(prefix, type) {
        var paramArr = [];

        if (prefix == null) {
            prefix = 'page.';
        }

        switch (type) {
            case 'TCOM': 
                paramArr.push(
                    prefix + 'curPageNum=' + textParam(this.currentPage)
                );
                paramArr.push(
                    prefix + 'pageSize=' + textParam(this.pageSize)
                );
                break;

            default:
                paramArr.push(
                    prefix + 'currentPage' + textParam(this.currentPage)
                );
                paramArr.push(
                    prefix + 'pageSize' + textParam(this.pageSize)
                );
        }

        return paramArr.join('&');            
    };

})();

