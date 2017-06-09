/**
 * configuration of xutil.ajax
 * Copyright 2012 Baidu Inc. All rights reserved.
 * 
 * @file:    data insight 全局(包括console和product)的ajax的配置
 *          （常量和默认失败处理等）
 *          （如不服此配置，可重载）
 * @author:  sushuang(sushuang)
 * @depend:  xutil.ajax, di.config.lang
 */

$namespace('di.config');

(function() {
    
    //--------------------------------
    // 引用
    //--------------------------------

    var XAJAX = xutil.ajax;
    var isFunction = xutil.lang.isFunction;
    var LANG;
    var DIALOG;
    
    $link(function () {
        LANG = di.config.Lang;
        DIALOG = di.helper.Dialog;
    });

    //--------------------------------
    // 类型声明
    //--------------------------------

    var AJAX = $namespace().Ajax = function() {};

    /**
     * 业务错误信息
     */
    AJAX.ERROR_RTPL_ID = 10011; // reportTemplateId 不存在
    AJAX.ERROR_SESSION_TIMEOUT = 10001; // session 过期
    AJAX.ERROR_PARAM = 20001; // olap查询参数错误，由应用程序自己处理

    /**
     * 默认选项
     */
    var DEFAULT_OPTIONS = {
        showWaiting: true // 默认在ajax请求时显示waiting
    };

    /**
     * 默认的ajax失败处理
     * 
     * @public
     * @param {number} status ajax返回状态
     * @param {Object|string} ejsonObj e-json整体返回的数据
     * @param {Function} defaultCase 可用此函数替换默认情况的处理函数
     */
    AJAX.handleDefaultFailure = function(status, ejsonObj, defaultCase) {
        switch (status) {
            case 100: // 未登陆
            case 201: 
            case 301: // 重定向的情况
            case 302: // 重定向的情况
            case 99999: // 其实302时返回的是这个 ...
                // 嵌入第三方系统之后，频繁切换页签刷新报表会因为请求中断而弹出该提示框，故先注释掉
                // DIALOG.alert(LANG.SAD_FACE + LANG.RE_LOGIN, null, true);
                break;
            case 333: //没有权限
                DIALOG.alert(LANG.SAD_FACE + LANG.NO_AUTH_SYSTEM);
                break;
            case 20003: // 缺少某个维度节点
                var dimName = ejsonObj.data.dimName;
                var dimCapture = ejsonObj.data.dimCapture;
                var missedMember = ejsonObj.data.missedMember;
                DIALOG.alert(LANG.SAD_FACE + "缺少维度节点 "+dimCapture+"("+dimName+"): "+missedMember);
                break;
            case 20004: // 镜像缺少某个参数
                var statusInfo = ejsonObj.statusInfo;
                DIALOG.alert(LANG.SAD_FACE + statusInfo);
                break;
            case 1: // 返回html错误页面的情况
                DIALOG.alert(LANG.SAD_FACE + ejsonObj.statusInfo);
                break;
            case 403: // 403错误
            case 404: // 404错误
            case 405: // 405错误
            case 500: // 500错误
                DIALOG.alert(LANG.SAD_FACE + LANG.ERROR);
                break;
            default:
                if (isFunction(defaultCase)) {
                    defaultCase(status, ejsonObj);
                } 
                else {
                    DIALOG.alert(LANG.SAD_FACE + LANG.ERROR);
                }
        }
    }

    /**
     * 刷新整站
     *
     * @protected
     */
    // AJAX.reload = function() {
    //     try {
    //         window.top.location.reload();
    //     } 
    //     catch (e) {
    //         window.location.reload();
    //     }
    // }

    /**
     * 默认的timeout处理
     *
     * @public
     */
    AJAX.handleDefaultTimeout = function() {
        DIALOG.hidePrompt();
        DIALOG.mask(false);
    };
    
    /**
     * 默认的请求参数
     *
     * @public
     * @return {string} 参数字符串，如a=5&a=2&b=xxx
     */
    AJAX.getDefaultParam = function() {
        var date = new Date(), paramArr = [];
        paramArr.push('_cltime=' + date.getTime()); // 供后台log当前时间
        paramArr.push('_cltimezone=' + date.getTimezoneOffset()); // 供后台log当前时区
        return paramArr.join('&');
    };
    
    /**
     * 用于显示全局的等待提示，当第一个需要显示等待的请求发生时会调用
     *
     * @public
     */
    AJAX.showWaiting = function() {
        DIALOG.waitingPrompt(LANG.AJAX_WAITING);
        DIALOG.mask(true);
    };
    
    /**
     * 用于隐藏全局的等待提示，当最后一个需要显示等待的请求结束时会调用
     *
     * @public
     */
    AJAX.hideWaiting = function() {
        DIALOG.hidePrompt();
        DIALOG.mask(false);
    };
        
    /**
     * 挂载配置
     */
    XAJAX.DEFAULT_FAILURE_HANDLER = AJAX.handleDefaultFailure;
    XAJAX.DEFAULT_ONTIMEOUT = AJAX.handleDefaultTimeout;
    XAJAX.DEFAULT_PARAM =AJAX.getDefaultParam;
    XAJAX.SHOW_WAITING_HANDLER = AJAX.showWaiting;
    XAJAX.HIDE_WAITING_HANDLER = AJAX.hideWaiting;
    XAJAX.DEFAULT_OPTIONS = DEFAULT_OPTIONS;    

})();