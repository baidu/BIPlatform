/**
 * xutil.ajax
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    工程中Ajax的统一入口。基于基本的ajax封装实现，提供便于工程开发的附加功能。
 *          功能：
 *          (1) 全局的的请求失败处理定义接口
 *          (2) 全局的等待提示定义接口
 *             （使用方式：请求时传参数showWaiting）
 *          (3) 请求超时设定及全局的超时处理定义接口
 *          (4) 提供complete和finalize事件，便于不论请求成功与否的时的处理（如某些清理）
 *          (5) 返回的一致性保证
 *              用于在不屏蔽二次点击/重复请求情况下保证只是最新的请求返回被处理。
 *              用abort方式实现，可abort重复发出的请求。
 *              没有使用为每个请求挂唯一性tokenId方式的原因是，
 *              tokenId方式不易处理这种问题：
 *              如果pending的连接已超过浏览器连接上限，用户看无响应继续点击，
 *              会造成自激性连接堆积，难以恢复。
 *              但是abort方式的缺点是，如果重复请求过于频繁（例如由用户点击过快造成），
 *              容易对后台造成压力。暂时未支持对请求过频繁的限制（TODO）。
 *              （使用方式：请求时传参数businessKey）
 *          (6) abort支持的完善
 *              在多局部刷新的web应用中，在适当时点可以abort掉未完成的请求，
 *              防止返回处理时因相应的dom已不存在而出错。
 *          (7) 多个请求同步（最后一个请求返回时才执行回调）的支持。
 *              参见createSyncWrap方法
 * @author:  sushuang(sushuang)
 * @depend:  tangram.ajax, e-json, xutil.ajax
 */

(function () {
    
    var AJAX = xutil.ajax;
    var exRequest = baidu.ejson.request;
    var dataCache;
        
    /**
     * 外部接口，可以在工程中定义这些方法的实现或变量的赋值（也均可缺省）
     */
    /**
     * 默认的ajax调用选项，常用于工程的统一配置。
     * 可以被真正调用ajax时传的options覆盖
     *
     * @type {Object}
     * @public
     * @see ajax.request
     */
    AJAX.DEFAULT_OPTIONS = null;
    /**
     * 全局统一的请求失败处理函数
     * 先调用自定义的失败处理函数，再调用此统一的失败处理函数。
     * 如果前者返回false，则不会调用后者。
     *
     * @type {Function}
     * @public
     * @param {number} status ajax返回状态
     * @param {(Object|string)} obj e-json整体返回的数据
     * @param {Function} defaultCase 可用此函数替换默认情况的处理函数
     */
    AJAX.DEFAULT_FAILURE_HANDLER = null;
    /**
     * 全局统一的请求超时处理函数
     * （无参数返回值）
     *
     * @type {Function}
     * @public
     */
    AJAX.DEFAULT_TIMEOUT_HANDLER = null;
    /**
     * 全局统一的请求函数
     *
     * @type {Function}
     * @public
     * @return {string} 参数字符串，如a=5&a=2&b=xxx
     */
    AJAX.DEFAULT_PARAM = null;
    /**
     * 用于显示全局的等待提示，当第一个需要显示等待的请求发生时会调用
     *
     * @type {Function}
     * @public
     */
    AJAX.SHOW_WAITING_HANDLER = null;
    /**
     * 用于隐藏全局的等待提示，当最后一个需要显示等待的请求结束时会调用
     *
     * @type {Function}
     * @public
     */
    AJAX.HIDE_WAITING_HANDLER = null;
    /**
     * 默认是否显示等待提示，默认为false，可在工程中修改此默认定义
     *
     * @type {Function}
     * @public
     */
    AJAX.DEFAULT_SHOW_WAITING = false;
        
    /**
     * 记录所有请求未结束的xhr，
     * 格式：{requestId: {xhr: <xhr>, clear: <clear>}}
     * 
     * @type {Object}
     * @private
     */
    var xhrSet = {};
    /**
     * 记录指定了businessKey的请求，
     * 格式：{businessKey: requestId}
     *
     * @type {Object}
     * @private
     */
    var businessSet = {};
    /**
     * 记录所有需要显示等待的requestId，
     * 是xhrSet的子集，
     * 格式：{requestId: 1}
     *
     * @type {Object}
     * @private
     */
    var waitingSet = {};
    /**
     * waitingSet的大小
     *
     * @type {number}
     * @private
     */
    var waitingCount = 0;
    /**
     * 唯一性ID
     *
     * @type {number}
     * @private
     */
    var uniqueIndex = 1;
    
    /**
     * append默认的参数
     *
     * @private
     * @param {string} data 参数
     */
    function appendDefaultParams(data) {
        var paramArr = [];

        if (hasValue(data) && data !== '') {
            paramArr.push(data);
        }

        var defaultParamStr = AJAX.DEFAULT_PARAM ? AJAX.DEFAULT_PARAM() : '';
        if (hasValue(defaultParamStr) && defaultParamStr !== '') {
            paramArr.push(defaultParamStr);
        }

        return paramArr.join('&');
    }
    
    /**
     * 打印日志
     *
     * @private
     * @param {string} msg 日志信息
     */
    function log(msg) {
        isObject(window.console) 
            && isFunction(window.console.log) 
            && window.console.log(msg);
    }
    
    /**
     * 显示等待处理
     *
     * @private
     * @param {string} requestId 请求ID
     * @param {boolean} showWaiting 是否显示等待
     */
    function handleShowWaiting(requestId, showWaiting) {
        if (showWaiting) {
            waitingSet[requestId] = 1;
            (waitingCount ++) == 0
                && AJAX.SHOW_WAITING_HANDLER 
                && AJAX.SHOW_WAITING_HANDLER();
        }
    }
    
    /**
     * 隐藏等待处理
     *
     * @private
     * @param {string} requestId 请求ID
     */
    function handleHideWaiting(requestId) {
        if (waitingSet[requestId]) {
            delete waitingSet[requestId];
            (-- waitingCount) <= 0 
                && AJAX.HIDE_WAITING_HANDLER 
                && AJAX.HIDE_WAITING_HANDLER();
        }
    }
    
    /**
     * abort处理
     *
     * @private
     * @param {string} businessKey 业务键
     * @param {string} requestId 请求ID
     */
    function handleBusinessAbort(businessKey, requestId) {
        var oldRequestId;
        if (hasValue(businessKey)) {
            (oldRequestId = businessSet[businessKey]) 
                && AJAX.abort(oldRequestId, true);
            businessSet[businessKey] = requestId;
        }
    }
    
    /**
     * 业务键清除处理
     *
     * @private
     * @param {string} businessKey 业务键
     */
    function handleBusinessClear(businessKey) {
        if (hasValue(businessKey)) {
            delete businessSet[businessKey];   
        }
    }
    
    /**
     * 发送请求
     * 
     * @public
     * @param {string} url
     * @param {Objet} options
     * @param {string} options.data 发送参数字符串，GET时会拼装到URL
     * @param {string} options.method 表示http method, 'POST'或'GET', 默认'POST'
     * @param {string} options.businessKey 业务键，提供自动abort功能。
     *              缺省则不用此功能。
     *              如果某业务键的请求尚未返回，又发起了同一业务键的请求，
     *              则前者自动被abort。
     *              这样保证了请求返回处理的一致性，
     *              在请求可以重复发起的环境下较有意义
     *              （例如用户连续点击“下一页”按钮刷新列表，
     *              同时为用户体验而不会在返回前屏蔽点击时）。
     * @param {boolean} options.showWaiting 是否需要显示等待，true则计入等待集合，
     *              在相应时机调用SHOW_WAITING_HANDLER和HIDE_WAITING_HANDLER；
     *              false则忽略。默认值由DEFAULT_SHOW_WAITING指定。
     * @param {Function} options.onsuccess 请求成功的回调函数
     *              param {Object} data e-json解析出的业务数据
     *              param {Object} obj e-json返回数据整体
     * @param {Function} options.onfailure 请求失败的回调函数
     *              param {number} status e-json返回状态
     *              param {(Object|string)} obj e-json返回数据整体
     * @param {Function} options.oncomplete 返回时触发的回调函数，
     *              先于onsuccess或onfailure执行
     *              param {(Object|string)} obj e-json返回的数据整体 
     *              return {boolean} 如果返回false，则onsucces和onfailure都不执行
     * @param {Function} options.onfinalize 返回时触发的回调函数，
     *              后于onsuccess或onfailure执行
     *              param {(Object|string)} obj e-json返回的数据整体
     * @param {Function} options.defaultFailureHandler 
     *              请求自定的默认的失败处理函数，可缺省
     *              param {number} status e-json返回状态
     *              param {(Object|string)} obj e-json返回数据整体
     * @param {number} options.timeout 请求超时时间，默认是无限大
     * @param {Function} options.ontimeout 超时时的回调
     * @param {string} options.syncName 用于请求的同步，参见createSyncWrap方法
     * @param {Object} options.syncWrap 用于请求的同步，参见createSyncWrap方法
     * @return {string} options.requestId request的标志，用于abort
     */
    AJAX.request = function (url, options, param) {

        options = extend(
            extend(
                {}, AJAX.DEFAULT_OPTIONS || {}
            ), 
            options || {}
        );
        var requestId = 'AJAX_' + (++ uniqueIndex);
        var businessKey = options.businessKey;
        var defaultFailureHandler = 
                options.defaultFailureHandler || null;
        var timeout = options.timeout || 0;
        var ontimeout = options.ontimeout;
        var onfailure = options.onfailure;
        var onsuccess = options.onsuccess;
        var oncomplete = options.oncomplete;
        var onfinalize = options.onfinalize;
        var showWaiting = options.showWaiting || AJAX.DEFAULT_SHOW_WAITING;
        var syncWrap = options.syncWrap;
        var syncName = options.syncName;
        var xhr;
        
        function clear() {
            defaultFailureHandler = ontimeout = 
            onfailure = onsuccess = 
            onfinalize = oncomplete = xhr = options = null;

            delete xhrSet[requestId];
            handleBusinessClear(businessKey);
            handleHideWaiting(requestId);
        }

        // tangram的ajax提供的屏蔽浏览器缓存
        options.noCache = true;

        options.method = options.method || 'POST';

        options.data = appendDefaultParams(options.data || '');


        // 构造sucess handler
        options.onsuccess = function (data, obj) {
            if (requestId in xhrSet) { // 判断abort
                try {
                    if (!oncomplete || oncomplete(obj) !== false) {
                        // 如果是固定报表，会发一次请求，请求回来就缓存数据
                        if (param.reportType
                            && param.reportType === 'REGULAR'
                            && !dataCache
                        ) {
                            dataCache = data;
                            data = {
                                statusInfo: '',
                                status: 0,
                                data: '',
                                properties: {}
                            };
                        }
                        onsuccess(data, obj);
                    }
                    onfinalize && onfinalize(obj);
                } 
                catch (e) {
                    AJAX.errorMsg = e.message;
                }
                finally {
                    syncWrap && syncWrap.done(syncName);
                    clear();
                }
            }
        };

        // 构造failure handler
        options.onfailure = function (status, obj) {
            var needDef;
            if (requestId in xhrSet) { // 判断abort
                try {
                    if (!oncomplete || oncomplete(obj) !== false) {
                        needDef = onfailure(status, obj);
                    }
                    onfinalize && onfinalize(obj);
                } 
                catch (e) {
                    AJAX.errorMsg = e.message;
                }
                finally {
                    if (needDef !== false) {
                        if (AJAX.DEFAULT_FAILURE_HANDLER) {
                            AJAX.DEFAULT_FAILURE_HANDLER(
                                status, obj, defaultFailureHandler
                            );
                        }
                        else if (defaultFailureHandler) {
                            defaultFailureHandler(status, obj);
                        }
                    }
                    syncWrap && syncWrap.done(syncName);
                    clear();
                }
            }
        };

        // 构造timeout handler
        options.ontimeout = function () {
            try {
                if (!oncomplete || oncomplete(obj) !== false) {
                    ontimeout && ontimeout();
                }
                onfinalize && onfinalize(obj);
            } 
            catch (e) {
                AJAX.errorMsg = e.message;
            }
            finally {
                AJAX.DEFAULT_TIMEOUT_HANDLER 
                    && AJAX.DEFAULT_TIMEOUT_HANDLER();
                syncWrap && syncWrap.done(syncName);
                clear();
            }
        };

        if (timeout > 0) {
            options.timeout = timeout;
            options.ontimeout = timeoutHandler;
        } 
        else {
            delete options.timeout;
        }
        
        var resultData = {};
        // TODO:这块的验证
        // 如果是固定报表,如果存在固定报表缓存数据
        if (dataCache) {
            // 获取组件data
            if (param.componentId && dataCache[param.componentId]) {
                resultData = {
                    statusInfo: '',
                    status: 0,
                    data: dataCache[param.componentId]
                };
            }
            // context时，就不发请求，在这里进行模拟
            else {
                resultData = {
                    statusInfo: '',
                    status: 0,
                    data: {},
                    properties: {}
                };
            }
            onsuccess(resultData.data, resultData);
            return;
        }

        handleShowWaiting(requestId, showWaiting);

        handleBusinessAbort(requestId, businessKey);
        // 发送请求(第一次init_params需要发请求)
        xhrSet[requestId] = {
            xhr: exRequest(url, options),
            clear: clear
        };
        return requestId;
    };

    /**
     * 发送POST请求
     * 
     * @public
     * @param {string} url
     * @param {string} data 发送参数字符串，GET时会拼装到URL
     * @param {Function} onsuccess @see AJAX.request
     * @param {Function} onfailure @see AJAX.request
     * @param {Objet} options @see AJAX.request
     * @return {string} requestId request的标志，用于abort
     */
    AJAX.post = function (url, data, onsuccess, onfailure, options) {
        options = options || {};
        options.method = 'POST';
        options.data = data;
        options.onsuccess = onsuccess;
        options.onfailure = onfailure;
        return AJAX.request(url, options);
    };

    /**
     * 发送GET请求
     * 
     * @public
     * @param {string} url
     * @param {string} data 发送参数字符串，GET时会拼装到URL
     * @param {Function} onsuccess @see AJAX.request
     * @param {Function} onfailure @see AJAX.request
     * @param {Objet} options @see AJAX.request
     * @return {string} requestId request的标志，用于abort
     */
    AJAX.get = function (url, data, onsuccess, onfailure, options) {
        options = options || {};
        options.method = 'GET';
        options.data = data;
        options.onsuccess = onsuccess;
        options.onfailure = onfailure;
        return AJAX.request(url, options);        
    };

    /**
     * 按requestId终止请求，或终止所有请求
     * 如果已经中断或结束后还调用此方法，不执行任何操作。
     * 
     * @public
     * @param {string} requestId request的标志，
     *          如果缺省则abort所有未完成的请求
     * @param {boolean} silence abort后是否触发回调函数（即onfailure）
     *          true则不触发，false则触发，缺省为true
     */
    AJAX.abort = function (requestId, silence) {
        var willAbort = [];
        var i;
        var wrap;
        silence = silence || true;
        
        if (hasValue(requestId)) {
            (requestId in xhrSet) && willAbort.push(requestId);
        } 
        else {
            for (i in xhrSet) { willAbort.push(i); }
        }
        
        for (i = 0; requestId = willAbort[i]; i++) {
            try {
                wrap = xhrSet[requestId];
                silence && delete xhrSet[requestId];
                wrap.xhr.abort();
                wrap.clear.call(null);
            } catch (e) {
                log(
                    '[ERROR] abort ajax error. requestId=' + 
                        requestId + ', e=' + e
                );
            }
        }
    };
    
    /**
     * 按业务键（businessKey）终止请求
     * 如果已经中断或结束后还调用此方法，不执行任何操作。
     * 
     * @public
     * @param {string} businessKey 业务键
     * @param {boolean} silence abort后是否触发回调函数（即onfailure）
     *          true则不触发，false则触发，缺省为true
     */
    AJAX.abortBusiness = function (businessKey, silence) {
        var requestId = businessSet[businessKey];
        if (hasValue(requestId)) {
            delete businessSet[businessKey];
            AJAX.abort(requestId);
        }
    };

    /**
     * 创建一个同步对象，用于多个请求同步返回
     * 
     * @public
     * @usage 假如回调函数callbackX需要在请求a和请求b都返回后才被调用，则这样做：
     *        (1) 创建个“同步对象”
     *          var reqWrap = ajax.syncRequest(
     *              ['a', 'b'], 
     *              function() { ... this is the callback } 
     *          );
     *        (2) 请求时作为参数传入
     *          // 请求a
     *          ajax.request(url, { syncName: 'a', syncWrap: reqWrap }); 
     *          // 请求b
     *          ajax.request(url, { syncName: 'b', syncWrap: reqWrap });
     *          这样，reqWrap中定义的回调函数就会在a和b都返回后被执行了。
     * 
     * @param {Array} syncNameList 命名集合
     * @param {Function} callback 回调函数
     * @return {Object} 同步对象，用作request参数
     */
    AJAX.createSyncWrap = function (syncNameList, callback) {
        return new SyncWrap(syncNameList, callback);
    };

    /**
     * 用于多个请求同步的包装
     *
     * @constructor
     * @private
     * @param {Array} syncNameList 同步名列表
     * @param {Array} callback 结束回调
     */
    function SyncWrap(syncNameList, callback) {
        var i;
        this.syncNameMap = {};
        for (i = 0, syncNameList = syncNameList || []; i < syncNameList.length; i ++) {
            this.syncNameMap[syncNameList[i]] = 0;
        }
        this.callback = callback || new Function();
    }

    /**
     * 同步结束
     *
     * @public
     * @param {string} syncName 同步名
     */
    SyncWrap.prototype.done = function (syncName) {
        var name;
        this.syncNameMap[syncName] = 1;
        for (name in this.syncNameMap) {
            if (!this.syncNameMap[name]) { return; }
        }
        this.callback.call(null);
    };

    /**
     * 扩展
     *
     * @private
     * @param {Object} target 目标对象
     * @param {Object} source 源对象
     * @return {Object} 扩展结果
     */
    function extend(target, source) {
        for (var key in source) { target[key] = source[key]; }
        return target;
    }

    /**
     * 是否函数
     *
     * @private
     * @param {*} variable 输入
     * @return {boolean} 是否函数
     */
    function isFunction(variable) {
        return Object.prototype.toString.call(variable) == '[object Function]';        
    }

    /**
     * 是否有值
     *
     * @private
     * @param {*} variable 输入
     * @return {boolean} 是否有值
     */
    function hasValue(variable) {
        return variable != null;
    }

    /**
     * 是否对象
     *
     * @private
     * @param {*} variable 输入
     * @return {boolean} 是否对象
     */
    function isObject(variable) {
        return variable === Object(variable);
    }

})();
