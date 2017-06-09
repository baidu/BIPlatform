/**
 * ecui.XObject
 * Copyright 2012 Baidu Inc. All rights reserved.
 * 
 * @file:    视图和模型的基类
 * @author:  sushuang(sushuang)
 * @depend:  xutil.object
 * @version: 1.0.1
 */

(function () {

    //----------------------------------
    // 引用
    //----------------------------------
    
    var xobject = xutil.object;
    var inheritsObject = xobject.inheritsObject;
    var objProtoToString = Object.prototype.toString;
    var arrayProtoSlice = Array.prototype.slice;
    
    //----------------------------------
    // 类型定义
    //----------------------------------
    
    /**
     * 视图和模型的基类
     *
     * @class
     */
    var XOBJECT = xui.XObject = 
            inheritsObject(null, xobjectConstructor);
    var XOBJECT_CLASS = XOBJECT.prototype;
    
    /**
     * 构造函数
     *
     * @public
     * @constructor
     * @param {Object} options 参数     
     */
    function xobjectConstructor(options) {
        /**
         * 事件监听器集合
         * key: eventName
         * value: {Array.<Object>} 监听器列表
         *
         * @type {Object} 
         * @private
         */
        this._oEventHandlerMap = {};

        /**
         * 是否禁用（不可交互）
         *
         * @type {boolean} 
         * @private
         */
        this._bDisabled = false;
    }

    //----------------------------------
    // 基本方法
    //----------------------------------

    /**
     * 默认的初始化函数
     *
     * @public
     */
    XOBJECT_CLASS.init = function () {};
    
    /**
     * 默认的析构函数
     * 如果有设businessKey，则终止未完成的请求
     *
     * @public
     */
    XOBJECT_CLASS.dispose = function () {
        this._oEventHandlerMap = {};
    };

    /**
     * 是否禁用（不可交互）
     *
     * @public
     * @return {boolean} 是否禁用
     */
    XOBJECT_CLASS.isDisabled = function () {
        return !!this._bDisabled;
    };
    
    /**
     * 设置禁用（不可交互）
     *
     * @public
     * @return {boolean} 是否执行了禁用
     */
    XOBJECT_CLASS.disable = function () {
        if (!this._bDisabled) {
            this._bDisabled = true;
            return true;
        }
        return false;
    };
    
    /**
     * 设置启用（可交互）
     *
     * @public
     * @return {boolean} 是否执行了启用
     */
    XOBJECT_CLASS.enable = function () {
        if (this._bDisabled) {
            this._bDisabled = false;
            return true;
        }
        return false;
    };
    
    //------------------------------------------
    // 事件/通知/Observer相关方法
    //------------------------------------------
    
    /**
     * 注册事件监听器
     * 重复注册无效
     *
     * @public
     * @param {(string|Object|Array)} eventName 
     *                  类型是string时表示事件名，
     *                  类型是Object或Array时，含义见下方用法举例
     * @param {Function} handler 监听器
     * @param {Object=} context 即handler调用时赋给的this，
     *                  缺省则this为XDatasource对象本身
     * @param {...*} args handler调用时绑定的前几个参数
     * @usage
     *      [用法举例一] 
     *          myModel.attach('sync.result', this.eventHandler, this);
     *      [用法举例二] （同时绑定很多事件）
     *          var bind = xutil.fn.bind;
     *          myModel.attach(
     *              {    
     *                  'sync.parse': bind(this.handler1, this, arg1, arg2),
     *                  'sync.preprocess': bind(this.handler2, this),
     *                  'sync.result.INIT': bind(this.handler3, this),
     *                  'sync.result.DATA': [
     *                      bind(this.handler4, this),
     *                      bind(this.handler5, this, arg3),
     *                      bind(this.handler6, this)
     *                  ]
     *              }
     *      [用法举例三] （同时绑定很多事件）
     *          myModel.attach(
     *              ['sync.parse', this.handler1, this, arg1, arg2],
     *              ['sync.preprocess', this.handler2, this],
     *              ['sync.result.INIT', this.handler3, this],
     *              ['sync.result.DATA', this.handler4, this],
     *              ['sync.result.DATA', this.handler5, this, arg3],
     *              ['sync.result.DATA', this.handler6, this]
     *          );
     */
    XOBJECT_CLASS.attach = function (eventName, handler, context, args) {
        parseArgs.call(this, attach, arrayProtoSlice.call(arguments));
    };

    /**
     * 事件注册
     *
     * @private
     * @this {xui.XObject} XObject实例自身
     * @param {Object} handlerWrap 事件监听器封装
     */
    function attach(handlerWrap) {
        handlerWrap.once = false;
        doAttach.call(this, handlerWrap);
    }

    /**
     * 注册事件监听器，执行一次即被注销
     * 重复注册无效
     *
     * @public
     * @param {(string|Object|Array)} eventName 
     *                  类型是string时表示事件名，
     *                  类型是Object或Array时，含义见attach方法的用法举例
     * @param {Function} handler 监听器
     * @param {Object=} context 即handler中的this，
     *                  缺省则this为XDatasource对象本身
     * @param {...*} args handler执行时的前几个参数
     * @usage 用法举例同attach方法
     */
    XOBJECT_CLASS.attachOnce = function (eventName, handler, context, args) {
        parseArgs.call(this, attachOnce, arrayProtoSlice.call(arguments));
    };

    /**
     * 事件注册，执行一次即被注销
     *
     * @private
     * @this {xui.XObject} XObject实例自身
     * @param {Object} handlerWrap 事件监听器封装
     */
    function attachOnce(handlerWrap) {
        handlerWrap.once = true;
        doAttach.call(this, handlerWrap);
    }
    
    /**
     * 注册事件监听器
     * 重复注册无效
     *
     * @private
     * @this {xui.XObject} XObject实例自身
     * @param {Object} handlerWrap 事件监听器封装
     */
    function doAttach(handlerWrap) {
        var handlerList = this._oEventHandlerMap[handlerWrap.eventName];
        if (!handlerList) {
            handlerList = this._oEventHandlerMap[handlerWrap.eventName] = [];
        }
        if (getHandlerWrapIndex.call(this, handlerWrap) < 0) {
            handlerList.push(handlerWrap);
        }
    }

    /**
     * 注销事件监听器
     * 如果传了context参数，则根据handler和context来寻找已经注册的监听器，
     * 两者同时批评才会命中并注销。
     * （这样做目的是：
     *      当handler是挂在prototype上的类成员方法时，可用传context来区别，
     *      防止监听器注销影响到同类的其他实例
     *  ）
     * 如果context缺省，则只根据handler寻找已经注册了的监听器。
     *
     * @public
     * @param {(string|Object|Array)} eventName
     *                  类型是string时表示事件名，
     *                  类型是Object或Array时，含义见下方用法举例
     * @param {Function} handler 监听器
     * @param {Object=} context 即注册时handler中的this，
     *                  缺省则this为XDatasource对象本身
     * @usage
     *      [用法举例一] 
     *          myModel.detach('sync.result', this.eventHandler);
     *      [用法举例二] （同时注销绑定很多事件）
     *          myModel.detach(
     *              {    
     *                  'sync.parse': handler1,
     *                  'sync.preprocess': handler2,
     *                  'sync.result.DATA': [
     *                      handler5,
     *                      handler6
     *                  ]
     *              }
     *      [用法举例三] （同时注销绑定很多事件）
     *          myModel.detach(
     *              ['sync.parse', this.handler1],
     *              ['sync.result.INIT', this.handler3],
     *              ['sync.result.DATA', this.handler4],
     *              ['sync.result.DATA', this.handler5],
     *              ['sync.result.DATA', this.handler6]
     *          );
     */
    XOBJECT_CLASS.detach = function (eventName, handler, context) {
        parseArgs.call(this, doDetach, arrayProtoSlice.call(arguments));        
    };

    /**
     * 注销注册事件监听器
     *
     * @private
     * @this {xui.XObject} XObject实例自身
     * @param {Object} handlerWrap 事件监听器封装
     */
    function doDetach(handlerWrap) {
        var index = getHandlerWrapIndex.call(this, handlerWrap);
        if (index >= 0) {
            this._oEventHandlerMap[handlerWrap.eventName].splice(index, 1);
        }
    }    
    
    /**
     * 注销某事件的所有监听器
     *
     * @public
     * @param {string} eventName 事件名
     */
    XOBJECT_CLASS.detachAll = function (eventName) {
        delete this._oEventHandlerMap[eventName];
    };
    
    /**
     * 触发事件
     *
     * @public
     * @param {string} eventName 事件名
     * @param {Array} paramList 参数，可缺省
     * @return {boolean} 结果，
     *      有一个事件处理器返回false则为false，否则为true
     */
    XOBJECT_CLASS.notify = function (eventName, paramList) {
        var result = true;
        var onceList = [];
        var handlerList = this._oEventHandlerMap[eventName] || [];

        var i;
        var o;
        var handlerWrap;
        for (i = 0; handlerWrap = handlerList[i]; i++) {
            o = handlerWrap.handler.apply(
                handlerWrap.context, 
                (handlerWrap.args || []).concat(paramList || [])
            );
            (o === false) && (result = false);

            if (handlerWrap.once) {
                onceList.push(handlerWrap);
            }
        }
        for (i = 0; handlerWrap = onceList[i]; i++ ) {
            this.detach(eventName, handlerWrap.handler, handlerWrap.context);
        }
        return result;
    };

    /**
     * 构造handlerWrap
     *
     * @private
     * @this {xui.XObject} XObject实例自身
     * @param {string} eventName 事件名
     * @param {Function} handler 监听器
     * @param {Object} context 即handler中的this，
     *                  缺省则this为XDatasource对象本身
     * @param {...*} args handler执行时的前几个参数
     * @return {Object} wrap
     */
    function makeWrap(eventName, handler, context, args) {
        args = arrayProtoSlice.call(arguments, 3);
        args.length == 0 && (args = null);

        return {
            eventName: eventName,
            handler: handler,
            context: context || this,
            args: args
        };
    }
    
    /**
     * 处理函数参数
     *
     * @private
     * @this {xui.XObject} XObject实例自身
     * @param {Function} func 要执行的方法
     * @param {Array} args 输入的函数参数
     */
    function parseArgs(func, args) {
        var firstArg = args[0];

        if (objProtoToString.call(firstArg) == '[object String]') {
            func.call(this, makeWrap.apply(this, args));
        }

        else if (objProtoToString.call(firstArg) == '[object Array]') {
            for (var i = 0; i < args.length; i ++) {
                func.call(this, makeWrap.apply(this, args[i]));
            }
        }

        else if (firstArg === Object(firstArg)) {
            var hand;
            for (var eventName in firstArg) {
                hand = firstArg[eventName];

                if (objProtoToString.call(hand) == '[object Array]') {
                    for (var i = 0; i < hand.length; i ++) {
                        func.call(
                            this,
                            makeWrap.call(this, eventName, hand[i])
                        );
                    }
                }
                else {
                    func.call(this, makeWrap.call(this, eventName, hand));
                }
            }
        }
    }
    
    /**
     * 获得index
     *
     * @private
     * @this {xui.XObject} XObject实例自身
     * @param {Object} handlerWrap 事件监听器封装
     */
    function getHandlerWrapIndex(handlerWrap) {
        var handlerList = this._oEventHandlerMap[handlerWrap.eventName];
        if (handlerList) {
            for (var i = 0, wrap; wrap = handlerList[i]; i++ ) {
                if (wrap.handler === handlerWrap.handler
                    && wrap.context === handlerWrap.context
                ) {
                    return i;   
                }
            }
        }
        return -1;
    };
    
})();
