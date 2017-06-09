/**
 * @file 系统入口
 * @author 赵晓强
 * @date 2014-05-22
 */
define(['url', 'backbone', 'dialog'], function (Url, Backbone, dialog) {
    var ajaxError;

    // 全局的log容错
    window.dataInsight = window.dataInsight || {};
    dataInsight.log = function (str) {
        if (typeof console === 'object' && typeof console.log === 'function') {
            console.log(str);
        }
    };

    // 全局ajax设置
    $.ajaxSetup({
        cache: false,
        type: 'GET',
        dataType: 'json',
        contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
        beforeSend: function (XMLHttpRequest) {},
        dataFilter: function (response, dataType) {
            var result;
            if (dataType == 'json' && window.JSON && window.JSON.parse) {
                result = window.JSON.parse(response);
                // 后台返回数据失败
                if (result.status && result.status !== 0) {
                    // 异常情况，不返回response，jquery会捕获异常，进入error
                    if (result.statusInfo === '') {
                        result.statusInfo = '后台错误';
                    }
                    ajaxError = {
                        status: result.status,
                        textStatus: result.statusInfo
                    };
                }
                else {
                    ajaxError = undefined;
                    // 正常情况下，返回response
                    return response;
                }
            }
            else if (dataType === 'text') {
                return response;
            }
        },
        // 如果具体调用时配置了 error，会覆盖此error
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            var str;
            if (ajaxError === undefined) {
                str = 'status:' + XMLHttpRequest.status + ',' + textStatus;
                dialog.error(str);
            }
            else {
                str = 'status:' + ajaxError.status + ',' + ajaxError.textStatus;
                dialog.error(str);
            }
        }
    });

    /**
     * 异步加载某模块，可在此处做一些路由处理
     * @param {Object} option 设置信息
     */
    var enter = function (option) {
        var windowHeight;
        var navHeight;
        var footHeight;
        var mainMinHeight;

        // 设置全局url基本路径
        Url.setWebRoot(option.webRoot);
        require(['nav/nav-view', 'nav/nav-model'], function (View, Model) {
            new View({el: $('.j-nav'), model: new Model()});
        });

        // 设置主区域的最小高度
        windowHeight = $(window).height();
        navHeight = $('.j-nav').height();
        footHeight = $('.j-foot').height();
        mainMinHeight = windowHeight - navHeight - footHeight;

        $('.j-main').css({
            'min-height': mainMinHeight + 'px'
        });
    };

    return {
        enter: enter
    };

});