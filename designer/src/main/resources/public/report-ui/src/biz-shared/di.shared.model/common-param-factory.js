/**
 * di.shared.model.CommonParamFactory
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    通用请求参数处理器工厂
 * @author:  sushuang(sushuang)
 */

$namespace('di.shared.model');

(function () {
    
    var clone = xutil.object.clone;
    var extend = xutil.object.extend;
    var textParam = xutil.url.textParam;
    var jsonStringify = di.helper.Util.jsonStringify;
    var isArray = xutil.lang.isArray;
    var isObject = xutil.lang.isObject;

    /** 
     * 通用请求参数获取器工厂
     * 
     * @class
     * @param {Object} options 参数
     * @param {Object} options.externalParam 报表外部参数
     */
    var COMMON_PARAM_FACTORY = $namespace().CommonParamFactory = 
        function (options) {
            // 外部传来的报表参数。
            // 这些参数会回传给前端，而后在前后端传递。
            this.externalParam = options 
                && clone(options.externalParam) 
                || {};
        };
    var COMMON_PARAM_FACTORY_CLASS = COMMON_PARAM_FACTORY.prototype;

    /**
     * 要将对象格式化为json传输的标志
     */
    var STRINGIFY_FLAG = 'diF\x06^_^jsonnosj^_^\x06';

    /**
     * 如果是对象，
     * 则标注用http传输数据使用的格式，
     * 可以是stringify成json的格式，
     * 或者普通格式
     *
     * @public
     * @static
     * @param {*} data 可转为json的对象
     * @param {string} paramMode 可为'NORMAL'（默认），'JSON'
     * @return 原输入
     */
    COMMON_PARAM_FACTORY.markParamMode = function(data, paramMode) {
        if (isObject(data)) {
            if (!paramMode || paramMode == 'NORMAL') {
                delete data[STRINGIFY_FLAG];
            }
            else {
                data[STRINGIFY_FLAG] = paramMode;
            }
        }
        return data;
    };

    /**
     * 得到生产环境的getter
     *
     * @public
     * @param {Object} options 参数
     * @param {Object} options.reportTemplateId 后台的reportTemplateId
     */
    COMMON_PARAM_FACTORY_CLASS.getGetter = function(options) {
        options = options || {};

        var externalParam = this.externalParam;

        /**
         * 即后台的reportTemplateId。
         * reportTemplateId在必须以snippet为单位。
         * 每次请求后台都须调用commonParamGetter.update(data)对其进行更新，
         * 因为针对于每个报表，一个snippet中的第一个请求总要是使用记录在模板中reportTemplateId
         * （形如PERSISTENT***）来请求，后台用这个id从DB中取出报表，生成一个副本，放入缓存，
         * 并返回这个副本的reportTemplateId（形如：SESSION_LOADED***），后续，此snippet中的所有请求，
         * 都须以这个副本的reportTemplateId作为参数。
         * 所以要用update函数对这个reportTemplateId进行更新。
         */
        var reportId = options.reportId;

        /**
         * 初始为'INIT'，允许调用commonParamGetter。
         * 第一次调用而未返回时变为'FORBIDDEN'，这时再次调用则抛出异常，
         * （这是为了防止报表设计时，设计出：一个报表初始用）
         * 第一次调用返回时，变为'OPEN'，以后可随意调用。
         */
        var loadValve = 'INIT';

        /**
         * 通用参数获取器，
         * 会进行encodeURIComponent，和去除空值
         *
         * @public
         * @param {Object=} paramObj 请求参数
         *      key为参数名，
         *      value为参数值，{string}或者{Array.<string>}类型
         * @param {string=} paramMode 什么格式传输，值可为：
         *      'NORMAL'（默认）：普通格式（数组使用aa=2&aa=3&aa=5的方式，不支持对象传输）；
         *      'JSON'：使用json格式传输对象（含数组）
         * @param {Object=} options 可选参数
         * @param {Array} options.excludes 要排除的属性
         * @return {string} 最终请求参数最终请求参数
         */
        function commonParamGetter(paramObj, options) {
            options = options || {};

            if (loadValve == 'INIT') {
                loadValve = 'FORBIDDEN';
            }
            else if (loadValve == 'FORBIDDEN') {
                throw new Error('' 
                    + '一个snippet中的第一个请求不能并发，请调整报表设计。' 
                    + '在第一请求返回后再发出其他请求。'
                    + '可能引起这个错误的情况比如有：'
                    + '多个组件用同一个reportTempalteId，但并发得发请求。'
                    + '（注：多个组件用同一个reportTempalteId，这本身是允许的，比如meta－config和table共用，'
                    + '但是，他们是作为一个实例使用，目前未支持建立多个实例。）'
                );
            }

            var o = {};
            // 后天的参数的优先级比externalParam高
            extend(o, externalParam, paramObj);
            if (paramObj) {
                o.reportId = paramObj.reportId ? paramObj.reportId : externalParam.reportId;
            }
            o.reportId = o.reportId ? o.reportId : reportId;
            var excludes = options.excludes || [];
            for (var i = 0; i < excludes.length; i ++) {
                delete o[excludes[i]];
            }

            return stringifyParam(o, { paramMode: options.paramMode });
        };

        /** 
         * 通用参数更新方法
         *
         * @public
         * @return {Object} options 参数
         * @return {Object} options.reportId 后台模板id
         */
        commonParamGetter.update = function (options) {
            // 后台的约定：无论何时，
            // 总是以reprotTemplateId这个名字进行 传参 和 回传。
            var rTplId = options && options.reportId || null;
            if (rTplId) {
                loadValve = 'OPEN';
                reportId = rTplId;
            }
            else if (loadValve != 'OPEN') {
                loadValve = 'INIT';
            }
        };

        /** 
         * 得到当前reportId
         *
         * @public
         * @return {string} 当前reportTemplateId
         */
        commonParamGetter.getReportTemplateId = function () {
            return reportId;
        };

        /**
         * 挂上便于调用
         */
        commonParamGetter.markParamMode = COMMON_PARAM_FACTORY.markParamMode;

        return commonParamGetter;
    };

    /**
     * 请求参数变为string
     * null和undefined会被转为空字符串
     * 可支持urlencoding
     * 
     * @public
     * @param {Object} paramObj 请求参数封装
     *      key为参数名，
     *      value为参数值，{string}或者{Array.<string>}类型   
     * @param {Object=} options 参数
     * @param {string=} options.paramMode 什么格式传输，值可为：
     *      'NORMAL'（默认）：普通格式（数组使用aa=2&aa=3&aa=5的方式，不支持对象传输）；
     *      'JSON'：使用json格式传输对象（含数组）
     * @param {string=} options.suffix 参数名后缀
     * @return {Array.<string>} 请求参数数组
     */
    function stringifyParam(paramObj, options) {
        var paramArr = [];
        options = options || {};

        function pushParam(name, value) {
            paramArr.push(textParam(name) + '=' + textParam(value));
        }

        var name;
        var value;
        var i;

        for (name in paramObj) {
            value = paramObj[name];

            // paramMode为'JSON'，
            // 无论数组还是对象，都格式化成json传输
            if (isObject(value) 
                && (options.paramMode == 'JSON' || value[STRINGIFY_FLAG] == 'JSON')
            ) {
                // 格式化成json前清理
                delete value[STRINGIFY_FLAG];

                // 格式化成json
                pushParam(name, jsonStringify(value));

                // 格式化成json后恢复
                value[STRINGIFY_FLAG] = 1;
            }
            // 没有json化标志，则用传统方式处理
            else {
                if (isArray(value)) {
                    for (i = 0; i < value.length; i ++) {
                        pushParam(name, value[i]);
                    }
                }
                else {
                    pushParam(name, value);
                }
            }
        }

        return paramArr.join('&');
    };    

})();

