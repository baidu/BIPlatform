/**
 * di-stub
 * Copyright 2013 Baidu Inc. All rights reserved.
 *
 * @file:    报表展示的stub，给客户系统提供API
 *           注意！！此文件是嵌入到客户系统，
 *           希望客户系统不使用http缓存此文件（从而方便报表引擎对此文件的升级更新），
 *           这就需要这个文件注意写法，能在压缩后足够小。
 * @author:  sushuang(sushuang)
 */

/**
 * @usage
 *      [请求报表]
 *          在<div id="some-container"></div>上创建stub：
 *          var di = new $DataInsight$('some-container');
 *          或者假设 var el 为该div引用，则可：
 *          var di = new $DataInsight$(el);
 *
 *          开始加载报表
 *          di.load();
 *
 *          如果有报表参数需要传入，则可：
 *          di.load({
 *              data: 'someParam1=1234&someParam2=asdf&someParam2=zxcv'
 *          });
 *
 *          重复在一个dom元素上使用new $DataInsight$(el)，
 *          会先自动dispose以前绑在上面的stub实例。
 *
 *          如果需要监听di的事件，则可：
 *          di.addEventListener('eventName', eventHandler);
 *          
 *          目前只支持'linkbridge'事件，用于点击报表中的链接，新开报表页。handler参数为：
 *              {string} url 跳转目标url
 *              {string} param 须带入的参数
 */
(function(NS) {
    
    /**
     * 报表展示的stub，给客户系统提供API
     *
     * @class
     * @constructor
     * @param {(string|HTMLElement)} el 在此dom节点上进行加载
     * @param {Object=} options 参数，可缺省
     * @param {string=} options.method 请求报表时的HTTP请求方式，可为'POST'或'GET'（默认）
     * @param {string=} options.embedMode 嵌入方式，
     *      值可为'IFRAME'（缺省值）或'DIV'（目前尚不支持）
     * @param {string=} options.url DI后台url，缺省则取默认的
     * @param {string=} options.data 每次请求固定会传的请求参数（须先encodeURIComponent后再传入）
     *      （如：'aaa=123&bbb=xyz'）
     * @param {string=} options.widthMode 横向大小控制模式，可选值为：
     *          'FIX'（固定高宽）
     *          'ADAPT'（自适应）
     *          'FULLFILL'（充满，默认）
     * @param {string=} options.heightMode 纵向向大小控制模式，可选值为：
     *          'FIX'（固定高宽）
     *          'ADAPT'（自适应，默认）
     *          'FULLFILL'（充满）
     * @param {number=} options.width 指定的宽度
     * @param {number=} options.height 指定的高度
     */
    var DI_STUB = NS.$DataInsight$ = constructor;
    var DI_STUB_CLASS = DI_STUB.prototype;

    function constructor(el, options) {
        if (Object.prototype.toString.call(el) == '[object String]') {
            el = document.getElementById(el);
        }

        // 先寻找是否在此el上已经创建stub
        var exist = findStub(el);
        // 如果已经创建，则先析构此stub
        exist && exist.stub.dispose();

        /**
         * 容器el
         */
        this._eMain = el;

        /**
         * 是否已经创建
         */
        this._bCreated = false;

        /**
         * iframe
         */
        this._eIfr;

        /**
         * 参数
         */
        this._oInitOpt = options = extend({}, options);
        options.embedMode = options.embedMode || 'IFRAME';
        options.widthMode = options.widthMode || 'FULLFILL';
        options.heightMode = options.heightMode || 'ADAPT';
        options.method = (options.method || 'GET').toUpperCase();

        // 初始化liteDialog
        DI_STUB.LiteDialog && DI_STUB.LiteDialog.setEnv({ win: window });

        // 记录stub实例
        stubRepository.push(this);
    }

    /**
     * 默认的ID产品端后台链接
     */
    var DEFAULT_URL = '/_report/reportTemplate/complex/generateReport.action';

    /**
     * di占有的属性名
     */     
    var INNER_MAIN_ANCHOR = 'data-o_o-di';

    /**
     * 控制高宽的div
     */
    var IFR_CTRL_DIV = 'BODY';

    /**
     * 挂事件的属性
     */
    var STUB_EVENT_CHANNEL_OUTWARD = 'data-d-outward-d-atad';
    var STUB_EVENT_CHANNEL_INWARD = 'data-d-inward-d-atad';

    /**
     * 请求中使用的标志
     */
    var AGENT_PARAM = 'diAgent=STUB';
    var VERSION_PARAM_NAME = '__v__';

    /**
     * 保留已经创建的stub实例
     *
     * @static
     */
    var stubRepository = [];

    /**
     * 自增唯一id
     */
    var uid = 0;

    /**
     * DI嵌入页面开始，初始加载报表，或者更新重新渲染报表
     *
     * @public
     * @param {Object=} options 选项，可缺省
     * @param {string=} options.url DI后台url，缺省则取默认的
     * @param {string=} options.data 请求参数（如：'aaa=123&bbb=xyz'）
     * @param {boolean=} options.recreate 强制重新创建报表，
     *      缺省为false
     * @param {string=} optinos.method 用GET或者POST请求报表，默认为GET
     */
    DI_STUB_CLASS.load = function(options) {
        checkDisposed.call(this);
        var initOpt = this._oInitOpt;
        this._oLoadOpt = extend({}, initOpt, options);

        (initOpt.embedMode == 'DIV' ? divLoad : iframeLoad)
            .call(this);
    };

    /**
     * 析构
     *
     * @public
     */
    DI_STUB_CLASS.dispose = function() {
        checkDisposed.call(this);

        DI_STUB.LiteDialog && DI_STUB.LiteDialog.dispose();

        disposeInner.call(this);
        
        var o = findStub(this._eMain);
        o && stubRepository.splice(o.index, 1);
        this._eMain = null;
    };

    /**
     * 改变大小
     *
     * @public
     */
    DI_STUB_CLASS.setSize = function(width, height) {
        this._eIfr.style.width = width;
        this._eIfr.style.height = height;

        // 通知内部resize
        triggerInnerEvent.call(this, 'resize');
    };

    /**
     * 内部删除
     *
     * @private
     */
    function disposeInner() {
        this._eIfr = null;
        this._eForm = null;
        this._eMain.innerHTML = '';
        this._eInnerMain = null;
    }

    /**
     * 使用iframe方式加载
     *
     * @private
     */
    function iframeLoad() {
        var me = this;
        var loadOpt = this._oLoadOpt;

        if (!this._bCreated || loadOpt.recreate) {
            var eMain = this._eMain;

            // 清空
            disposeInner.call(this);
            var ifrId = 'DataInsight_'
                + (++uid) + '_' 
                + Math.round(Math.random() * 1000000000);

            // 创建iframe和POST用的form
            eMain.innerHTML = [
                '<iframe name="', ifrId, '" ',
                    'style="border-width:0;overflow:hidden;margin:0;padding:0" ',
                    'frameborder="no" border="0" marginwidth="0" marginheight="0" scrolling="no">',
                '</iframe>',
                '<form method="POST" target="', ifrId, '"></form>'
            ].join('');
            this._eIfr = eMain.firstChild;
            this._eForm = eMain.getElementsByTagName('FORM')[0];
            handleIfrResize.call(this);

            attachEvent(this._eIfr, 'load', function() {
                bindStubEventChannel.call(me);
                handleIfrResize.call(me);
                triggerInnerEvent.call(me, 'prodStart');
            });
        }

        // 开始请求后台
        (
            loadOpt.method == 'POST' 
                ? requestByPost 
                : requestByGet
        ).call(this, loadOpt);
    }

    /**
     * 用get方式请求
     * 
     * @private
     */
    function requestByGet() {
        var loadOpt = this._oLoadOpt;
        var url = loadOpt.url || DEFAULT_URL;
        var param = [];
        loadOpt.data && param.push(loadOpt.data);
        param.push(VERSION_PARAM_NAME + '=' + Math.random());
        param.push(AGENT_PARAM);
        // FIXME 
        // 简单实现
        this._eIfr.src = url + (url.indexOf('?') >= 0 ? '&' : '?') + param.join('&');
    }

    /**
     * 用post方式请求
     * 
     * @private
     */
    function requestByPost() {
        var loadOpt = this._oLoadOpt;
        var eForm = this._eForm;
        eForm.innerHTML = '';
        
        var ipts = parseInputParam(loadOpt.data);

        for (var i = 0, ipt, el; ipt = ipts[i]; i ++) {
            el = document.createElement('INPUT');
            el.type = 'hidden';
            el.value = ipt.value;
            el.name = ipt.name;
            eForm.appendChild(el);
        }

        var url = (loadOpt.url || DEFAULT_URL);
        var param = [];
        param.push(VERSION_PARAM_NAME + '=' + Math.random());
        param.push(AGENT_PARAM);

        eForm.action = url + (url.indexOf('?') >= 0 ? '&' : '?') + param.join('&');
        eForm.submit();
    }

    function parseInputParam(data) {
        var param = [];
        if (!data) { return param; }
        data = data.split('&');

        for (var i = 0, p; i < data.length; i ++) {
            if (p = data[i]) {
                p = p.split('=');
                p[0] && param.push(
                    { 
                        name: decodePercent(p[0]),
                        value: decodePercent(p[1] || '')
                    }
                );
            }
        }
        return param;
    }

    /**
     * 绑定内部事件
     * （暂时不支持垮域）
     *
     * @private
     * @event
     */
    function bindStubEventChannel() {
        var LITE_DIALOG = DI_STUB.LiteDialog || {};

        // 事件处理器
        var handleMap = {
            resize: handleIfrResize,
            prompt: LITE_DIALOG.prompt,
            waitingprompt: LITE_DIALOG.waitingPrompt,
            hideprompt: LITE_DIALOG.hidePrompt,
            linkbridge: getTriggerOuter('linkbridge'),
            sessiontimeout: getTriggerOuter('sessiontimeout')
        };

        var me = this;
        var innerMain = getInnerMain.call(this);
        if (innerMain) {
            innerMain[STUB_EVENT_CHANNEL_OUTWARD] = function(
                eventName, args
            ) {
                var fn = handleMap[eventName];
                fn && fn.apply(me, args || []);
            };
        }
    }

    /**
     * iframe方式的重置大小
     *
     * @private
     */
    function handleIfrResize() {
        var loadOpt = this._oLoadOpt;
        var widthMode = loadOpt.widthMode;
        var heightMode = loadOpt.heightMode;

        var width = widthMode == 'FIX'
            ? (loadOpt.width + 'px')
            : '100%';
        var height = heightMode == 'FIX'
            ? (loadOpt.height + 'px') 
            : '100%';

        if (widthMode == 'ADAPT' || heightMode == 'ADAPT') {
            var innerMain = getInnerMain.call(this);
            if (innerMain) {
                if (widthMode == 'ADAPT') {
                    width = innerMain.offsetWidth + 'px';
                }
                if (heightMode == 'ADAPT') {
                    height = innerMain.offsetHeight + 'px';
                }
            }
        }

        this._eIfr.style.width = width;
        this._eIfr.style.height = height;

        // 通知内部resize
        // triggerInnerEvent.call(this, 'resize');
    }

    /**
     * 报表间跳转跳转
     *
     * @private
     */
    function getTriggerOuter(eventName) {
        return function () {
            triggerOuterEvent(
                eventName, 
                Array.prototype.slice.call(arguments)
            );
        };
    }

    /**
     * 触发内部事件
     *
     * @private
     */
    function triggerInnerEvent(eventName, args) {
        var innerMain = getInnerMain.call(this) || {};
        var inter = innerMain[STUB_EVENT_CHANNEL_INWARD];
        inter && inter(eventName, args);
    }

    /**
     * 使用div方式加载
     *
     * @private
     */
    function divLoad() {
        // not implemented yet ...
    }

    /**
     * 得到内部的主dom
     *
     * @private
     * @return {HTMLElement} 内部的主dom
     */
    function getInnerMain() {
        if (this._oInitOpt.embedMode == 'DIV') {
            // TODO
        }
        else {
            if (this._eInnerMain) {
                // 已经获得innerMain，则进行缓存
                return this._eInnerMain;
            } 
            else {
                var win = getContentWindow.call(this);
                if (!win) {
                    return null;
                }
                var els = getAllEls.call(this, win.document);
                for (var i = 0, el; el = els[i]; i ++) {
                    if (el.getAttribute(INNER_MAIN_ANCHOR) == IFR_CTRL_DIV) {
                        return this._eInnerMain = el;
                    }
                }
                return null;
            }
        }
    }

    /**
     * 得到iframe的contentWindow 
     *
     * @private
     * @return {HTMLElement} iframe的contentWindow 
     */
    function getContentWindow() {
        if (this._eIfr) {
            try {
                return this._eIfr.contentWindow;
            }
            catch (e) {
            }
        }
    }

    /**
     * 检索stubrecord
     *
     * @private
     * @param {HTMLElement} el 用绑定的el检索
     * @return {Object} ret
     * @return {$DataInsight$} ret.stub
     * @return {number} ret.index
     */
    function findStub(el) {
        for (var i = 0, stub; i < stubRepository.length; i ++) {
            stub = stubRepository[i];
            if (stub._eMain == el) {
                return { stub: stub, index: i };
            }
        }
        return null;
    }

    /**
     * 检查是否已经被析构了
     *
     * @private
     * @this {Object} 本实例
     */
    function checkDisposed() {
        if (!this._eMain) { 
            throw new Error('This stub has been disposed!');
        }
    }

    //------------------------------------------------
    // Outer Event
    //------------------------------------------------

    /**
     * 事件处理器
     *    
     * @static   
     */    
    var listenerMap = {};

    /**
     * 注册事件
     *
     * @public
     * @param {string} eventName 事件名
     * @param {Function} listener 处理器
     * @return {Function} 处理器
     */
    DI_STUB_CLASS.addEventListener = function(eventName, listener) {
        var hList = listenerMap[eventName];
        if (!hList) {
            hList = listenerMap[eventName] = [];
        }
        hList.push(listener);
        return listener;        
    };

    /**
     * 解除注册事件
     *
     * @public
     * @param {string} eventName 事件名
     * @param {Function} listener 处理器
     * @return {Function} 处理器
     */
    DI_STUB_CLASS.romoveEventListener = function(eventName, listener) {
        var hList = listenerMap[eventName];
        if (hList) {
            for (var i = 0; i < hList.length;) {
                hList[i] === listener
                    ? hList.splice(i, 1)
                    : i ++;
            }
        }
    };

    /**
     * 触发外部事件
     *
     * @private
     * @param {string} eventName 事件名
     * @param {Array} args 参数列表
     */
    function triggerOuterEvent(eventName, args) {
        var hList = listenerMap[eventName];
        if (hList) {
            for (var i = 0; i < hList.length; i ++) {
                try {
                    hList[i] && hList[i].apply(null, args || []);
                }
                catch (e) {
                    // TODO
                }
            }
        }
    }

    function getOpt(opt1, opt2, opt) {

    }

    //------------------------------------------------
    // Utils
    //------------------------------------------------
        
    /** 
     * 向dom节点上挂载事件
     * 
     * @private
     */
    var attachEvent = DI_STUB.attachEvent = function (obj, type, func) {
        if (obj.attachEvent) {
            obj.attachEvent('on' + type, func);
        }
        else {
            obj.addEventListener(type, func, false);
        }
    };

    /**
     * 得到所有子el
     * 
     * @private
     * @param {HTMLElement} el 根el
     * @return {Array} 所有子el
     */
    var getAllEls = DI_STUB.getAllEls = function (el) {
        return el.all || el.getElementsByTagName('*');
    };   

    /**
     * 获取横向滚动量
     * 
     * @public
     * @return {number} 横向滚动量
     */
    var getScrollLeft = DI_STUB.getScrollLeft = function (win) {
        var d = win.document;
        return win.pageXOffset 
            || d.documentElement.scrollLeft 
            || d.body.scrollLeft;
    };

    /**
     * 获取页面视觉区域宽度
     *             
     * @public
     * @return {number} 页面视觉区域宽度
     */
    var getViewWidth = DI_STUB.getViewWidth = function (win) {
        var doc = win.document;
        var client = doc.compatMode == 'BackCompat' 
            ? doc.body : doc.documentElement;

        return client.clientWidth;
    };

    /** 
     * 删除dom节点
     * 
     * @private
     */
    var removeDom = DI_STUB.removeDom = function (el) {
        var parent = el.parentElement || el.parentNode;
        if (parent) {
            parent.removeChild(el);
        }
    };

    /**
     * 属性拷贝（对象浅拷贝）
     * target中与source中相同的属性会被覆盖。
     * prototype属性不会被拷贝。
     * 
     * @public
     * @usage extend(target, source1, source2, source3);
     * @param {(Object|Array)} target
     * @param {(Object|Array)...} source 可传多个对象，
     *          从第一个source开始往后逐次extend到target中
     * @return {(Object|Array)} 目标对象
     */
    var extend = DI_STUB.extend = function (target) {
        var sourceList = [].slice.call(arguments, 1);
        for (var i = 0, source, key; i < sourceList.length; i ++) {
            if (source = sourceList[i]) {
                for (key in source) {
                    if (source.hasOwnProperty(key)) {
                        target[key] = source[key];
                    }
                }
            }
        }
        return target;
    };    

    /**
     * 解码
     * 在decodeURIComponent基础上，兼容application/x-www-form-urlencoded中额外定义的
     * "空格会被encode成加号"这个情况。
     * 解释：
     *      encodeURIComponent依据URI规范编码：
     *          参见：http://en.wikipedia.org/wiki/Application/x-www-form-urlencoded#The_application.2Fx-www-form-urlencoded_type
     *      form表单提交时是依据较为老的application/x-www-form-urlencoded类型的编码方式：
     *          参见：http://www.w3.org/TR/REC-html40-971218/interact/forms.html#h-17.13.3.3
     *          主要区别是：空格被encode成加号
     *          依照这种方式进行encode/decode的地方是：
     *              form提交时；
     *              java.net.URIEncode/java.net.URIDecoder中；
     *              （报表引擎后台的默认decode也是这样的，所以如果出现加号，会被解码成空格）；
     *          所以，把这种encode的结果传到di-stub中的情况虽然很少，但也是存在的（比如后台依照这种规范encode，并渲染到页面上）
     *
     * @public
     * @param {string} str 要解码的字符串
     * @return {string} 解码结果
     */
    var decodePercent = DI_STUB.decodePercent = function (str) {
        if (str == null) { return ''; }
        return decodeURIComponent(str.replace(/\+/g, '%20'));
    };

})(/** @namespace */ window);