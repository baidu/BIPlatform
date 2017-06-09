/**
 * di.shared.model.DIFactory
 * Copyright 2013 Baidu Inc. All rights reserved.
 *
 * @file:    工厂
 *           约定：
 *              各种组件的类型均从这里获取，不直接引用。
 *              全局实例从这里获取。
 *
 * @author:  sushuang(sushuang),lizhantong(lztlovely@126.com)
 * @depend:  xui, xutil
 */

$namespace('di.shared.model');

(function () {

    //-------------------------------------------------------
    // 引用 
    //-------------------------------------------------------

    var UTIL = di.helper.Util;
    var DICT = di.config.Dict;
    var xlang = xutil.lang;
    var xobject = xutil.object;
    var isString = xlang.isString;
    var isArray = xlang.isArray;
    var isObject = xlang.isObject;
    var isFunction = xlang.isFunction;
    var isEmptyObj = xobject.isEmptyObj;
    var getByPath = xobject.getByPath;
    var objKey = xobject.objKey;
    var assign = xobject.assign;
    var extend = xobject.extend;
    var merge = xobject.merge;
    var clone = xobject.clone;
    var getUID = xutil.uid.getUID;
    var bind = xutil.fn.bind;
    var assert = UTIL.assert;
    var arraySlice = [].slice;
    var UNDEFINED;
    // FIXME
    // 独立此引用
    var ecuiAddEventListener = UTIL.ecuiAddEventListener;
    var ecuiTriggerEvent = UTIL.ecuiTriggerEvent;
    var targetBlank = UTIL.targetBlank;
    var objProtoToString = Object.prototype.toString;
    var hasOwnProperty = Object.prototype.hasOwnProperty;
    var arrayPush = Array.prototype.push;
    var isEcuiControl = UTIL.isEcuiControl;
    var evalJsonLogic = UTIL.evalJsonLogic;
    var XOBJECT = xui.XObject;
    var COMMON_PARAM_FACTORY;
    var ARG_HANDLER_FACTORY;
    var replaceIntoParam = xutil.url.replaceIntoParam;

    $link(function () {
        ARG_HANDLER_FACTORY = di.shared.arg.ArgHandlerFactory;
        COMMON_PARAM_FACTORY = di.shared.model.CommonParamFactory;
    });

    //----------------------------------------------------------
    // 类型声明 
    //----------------------------------------------------------

    /**
     * Unit工厂
     *
     * @usage
     *      单例，
     *      这样得到实例：var factory = di.shared.model.DIFactory();
     */
    $namespace().DIFactory = function () {
        if (!instance) {

            instance = {
                installClz: installClz,
                getClz: getClz,
                addEntity: addEntity,
                removeEntity: removeEntity,
                createIns: createIns,
                mountInteractions: mountInteractions,
                mountInteraction: mountInteraction,
                getDIMethod: getDIMethod,
                getEntity: function (id, mode) {
                    return getEntity(id, mode == 'RAW' ? 'DEF' : mode);
                },
                findEntity: findEntity,
                forEachEntity: forEachEntity,
                createDIEvent: createDIEvent,
                getRef: getRef,
                mergeOpt: mergeOpt,
                setGlobalTemp: setGlobalTemp,
                getGlobalTemp: getGlobalTemp,
                setEventChannel: setEventChannel,
                getEventChannel: getEventChannel,
                setInteractMemo: setInteractMemo,
                getInteractMemo: getInteractMemo,
                rootSnippet: rootSnippet,
                isDIEvent: isDIEvent,
                setFuncAuth: setFuncAuth,
                setDIAgent: function (diAgt) {
                    instance.diAgent = diAgt;
                },
                setDIReportImageId: function (rptImgId) {
                    instance.reportImageId = rptImgId;
                },
                getDIReportImageId: function () {
                    return instance.reportImageId;
                },
                dispose: function () {
                    resetInstanceAttributes();
                },
                INIT_EVENT_NAME: INIT_EVENT_NAME,
                INIT_EVENT_AGENT_ID: INIT_EVENT_AGENT_ID
            };

            resetInstanceAttributes();
        }
        return instance;
    };


    //----------------------------------------------------------
    // 常量/内部变量
    //----------------------------------------------------------

    /**
     * 为实例挂载属性或方法时使用的前缀，
     * 以及一些隐含的引用名
     */
    var DI_ATTR_PREFIX = '\x06diA^_^';
    var DI_METHOD_PREFIX = '\x06diM^_^';
    var DI_ADAPTER_METHOD_PREFIX = '\x06diMAdpt^_^';
    var DI_DEF_TAG = '\x06diDef^_^';
    var DI_EVENT_AGENT_TAG = '\x06diEAgt^_^';
    var DI_EVENT_TAG = '\x06diEvt^_^';
    var DI_TMP_TAG = '\x06diTmp^_^';
    var DI_OPT_HOME_TAG = '\x06diOpt^_^';
    var DI_OPT_CACHE_TAG = '\x06diOpt_cache^_^';
    var DI_OPT_ID_TAG = '\x06diOpt_id^_^';
    var SEP = '\x06_';
    var INIT_EVENT_NAME = '\x06diEvt_init^_^';
    var INIT_EVENT_AGENT_ID = '\x06diEvtAgtId_init^_^';

    /**
     * clzType
     */
    var INS_CLZ_TYPE = [
        'SNIPPET',
        'COMPONENT',
        'VUI',
        'VCONTAINER',
        'VPART'
    ];

    /**
     * 默认的vui adapter
     */
    var GENERAL_ADAPTER_METHOD_PATH = 'di.shared.adapter.GeneralAdapterMethod';
    var generalAdapterMethod;

    /**
     * DIFactory实例
     */
    var instance;


    //-----------------------------------------------------------------
    // 契约方法
    //-----------------------------------------------------------------

    /**
     * 调用挂载到各个实例上的di方法（如果找不到，则调用同名原有方法）
     * 挂载后，在实例中使用this.$di('someMethodName')调用挂载的方法
     * 如果调用时要传参，则为this.$di('someMethodName', arg1, arg2)
     * （PS：之所以没做成$di('someMethodName')(arg1, arg2)的样子，
     * 因为这样不好得到this）
     *
     * @param {string} methodName 方法名
     * @param {*...} args 调用参数
     *      支持apply和call，methodName后面一个参数可为apply或call，
     *      后面继续的参数是scope，以及其他参数，同apply和call的用法。
     *      如：some.$di('apply', this, [1234, 5214]);
     */
    var $di = {
        INS: function (methodName) {
            var scope;
            var args;
            var mk = arguments[1];

            if (mk == 'call') {
                scope = arguments[2];
                args = arraySlice.call(arguments, 3);
            }
            else if (mk == 'apply') {
                scope = arguments[2];
                args = arguments[3];
            }
            else {
                scope = this;
                args = arraySlice.call(arguments, 1);
            }

            return (
                // 寻找di挂载的方法
                this[DI_METHOD_PREFIX + methodName]
                // 如果找不到，找adapter方法
                || this[DI_ADAPTER_METHOD_PREFIX + methodName]
                // 如果找不到，则调用同名原有方法
                || this[methodName]
            ).apply(scope, args);
        },
        DEF: function (methodName) {
            var scope;
            var args;
            var mk = arguments[1];

            if (mk == 'call') {
                scope = arguments[2];
                args = arraySlice.call(arguments, 3);
            }
            else if (mk == 'apply') {
                scope = arguments[2];
                args = arguments[3];
            }
            else {
                scope = this;
                args = arraySlice.call(arguments, 1);
            }

            return DEF_CONTRACT_METHOD[methodName]
                .apply(scope, args);
        }
    };

    /**
     * 通用契约方法，用于注入
     */
    var COMMON_CONTRACT_METHOD = {
        /**
         * COMPONENT创建时需要调用
         */
        start: function (options) {
            var opt = options[DI_TMP_TAG];
            this.$di('setId', opt.id);
            this.$di('setEl', opt.el);

            var def = getEntity(opt.id, 'DEF');
            if (opt.el && opt.el.style.display == 'none') {
                setDIAttr(this, 'styleDisplay', def.styleDisplay);
            }

            var func;
            (func = getDIMethod(this, 'setTplMode'))
            && func.call(this, opt.tplMode);
            (func = getDIMethod(this, 'setCommonParamGetter'))
            && func.call(this, opt.commonParamGetter);
        },

        getDIFactory: function () {
            return instance;
        },

        getMethod: function (methodName) {
            return getDIMethod(this, methodName);
        },

        getDef: function () {
            return getEntity(
                COMMON_CONTRACT_METHOD.getId.call(this),
                'DEF'
            );
        },

        setId: function (id) {
            setDIAttr(this, 'id', id);
        },

        getId: function () {
            return getDIAttr(this, 'id');
            // return getAttrIncludeGlobal(this, 'id');
        },

        setEl: function (el) {
            setDIAttr(this, 'el', el);
        },

        getEl: function (id) {
            return getDIAttr(this, 'el');
        },

        diShow: function () {
            var def = this.$di('getDef');
            var el = this.$di('getEl');
            var styleDisplay = getDIAttr(this, 'styleDisplay');
            var hideByAuth = getDIAttr(this, 'hideByAuth');
            if (styleDisplay != null && !hideByAuth) {
                setDIAttr(this, 'styleDisplay', null);
                el.style.display = styleDisplay;
            }
        },

        diHide: function () {
            var el = this.$di('getEl');
            if (el && getDIAttr(this, 'styleDisplay') == null) {
                setDIAttr(this, 'styleDisplay', el.style.display);
                el.style.display = 'none';
            }
        },

        /**
         * 设置耳聋，聋则不收到任何事件
         *
         * @param {boolean} isDeaf 是否耳聋
         * @param {string=} key 禁用者的标志，缺省则忽略
         */
        setDeaf: function (isDeaf, key) {
            var keyName = 'deaf';

            // 设置禁用，并记录objKey
            if (isDeaf) {
                objKey.add(this, key, keyName);
                setDIAttr(this, keyName, true);
            }
            // 所有key都清除了，或者未传key，才解除禁用
            else {
                objKey.remove(this, key, keyName);
                (key == null || objKey.size(this, keyName) == 0)
                && setDIAttr(this, keyName, false);
            }
        },

        isDeaf: function () {
            return getDIAttr(this, 'deaf');
        },

        /**
         * 设置getValue禁用
         *
         * @param {boolean} valueDisabled 是否getValue禁用
         * @param {string=} key 禁用者的标志，缺省则忽略
         */
        setValueDisabled: function (valueDisabled, key) {
            var keyName = 'valueDisabled';

            // 设置禁用，并记录objKey
            if (valueDisabled) {
                objKey.add(this, key, keyName);
                setDIAttr(this, keyName, true);
            }
            // 所有key都清除了，或者未传key，才解除禁用
            else {
                objKey.remove(this, key, keyName);
                (key == null || objKey.size(this, keyName) == 0)
                && setDIAttr(this, keyName, false);
            }
        },

        isValueDisabled: function () {
            return getDIAttr(this, 'valueDisabled');
        },

        getClzType: 'placeholder',

        /**
         * 解禁操作
         *
         * @protected
         * @param {string=} key 禁用者的标志
         */
        disable: function (key) {
            objKey.add(this, key);

            if (!getDIAttr(this, 'disabled')) {
                setDIAttr(this, 'disabled', true);
                this.disable && this.disable();
            }
        },

        /**
         * 禁用操作
         *
         * @protected
         * @param {string=} key 禁用者的标志，空则无条件解禁
         */
        enable: function (key) {
            objKey.remove(this, key);

            if (objKey.size(this) == 0 && getDIAttr(this, 'disabled')) {
                setDIAttr(this, 'disabled', false);
                this.enable && this.enable();
            }
        },

        /**
         * 得到opt或opt值的统一入口
         *
         * @public
         * @param {string} optName 如cfgOpt、baseOpt
         * @param {string=} attr 属性名，如果为空，则得到opt本身
         * @return {Object} 得到的opt
         */
        getOpt: function (optName, attr) {
            var def = getEntity(this.$di('getId'), 'RAW');
            return getOpt(def, optName, attr, { clone: true });
        },

        /**
         * 设置def的参数
         *
         * @public
         * @param {string} optName 如cfgOpt、baseOpt
         * @param {string} attr 属性名
         * @param {*} value 属性值
         */
        setOpt: function (optName, attr, value) {
            var def = getEntity(this.$di('getId'), 'RAW');
            setOpt(def, optName, attr, value);
        },

        /**
         * @param {string} refName 如'vuiRef'，'vpartRef'
         * @param {string} refPath 引用定位路径，如'someAttr.some[4][5].some'
         * @param {string=} modee 值为'DEF'（默认）或者'INS'
         * @return {(Array.<Object>|Object)} ref的数组
         *      例如：vuiDef的内容为：
         *      {string} vuiDef.id ID
         *      {Object} vuiDef.clz 类
         *      {string} vuiDef.clzKey 类key
         *      {Object} vuiDef.initObject 初始化参数，可能为空
         */
        getRef: function (refName, refPath, mode) {
            return getRef(this, refName, refPath, mode);
        },

        /**
         * 为给定的事件注册事件代理。注册事件代理后，
         * 对此事件的addEventListener和dispatch都只针对于代理，屏蔽了原生事件。
         * 此方法常在vui的adapter中用于隔离原生事件。
         * 例如：
         *      某控件有事件change，为了挂接到DI，需要为其写adapter，
         *      adapter中有需要修改change事件的含义以符合COMPONENT的需求。
         *      则在adapter中，首先：
         *          ctrl.$di('registerEventAgent', 'change');
         *      然后：
         *          ctrl.onchange = function () { 
         *              // ...
         *              ctrl.$di('dispatchEvent', 'change');
         *          };
         *      因为有了eventAgent，那么向ctrl挂事件（ctrl.$di('addEventListener', ...)
         *      的时候只会挂到eventAgent上，而不会深入到ctrl本身。
         *
         * @public
         * @param {string=} eventName 事件名，缺省则对于全部事件都使用event agent
         */
        registerEventAgent: function (eventName) {
            registerEventAgent(this, eventName);
        },

        /**
         * 添加事件监听
         * 目前只支持XObject和ecui
         *
         * @param {string} eventName 事件名
         * @param {Function} handler 事件处理函数
         * @param {Object} scope 域，handler执行时的this
         * @param {Object=} options 选项
         * @param {string=} options.interactionId interact的id
         * @param {string=} options.dispatcherId 触发event的di ins的id
         * @param {Function=} options.argHandler 参数转化函数，用于适配事件的参数
         *      输入为 {Array} 参数构成的数组
         *      输出为 {Array} 转化完成后的参数构成的数组
         *      注意argHandler如果要更改原参数对象的内容，需要新建副本，
         *      以免影响其他事件处理器的响应。
         * @param {Array} options.bindArgs 绑定的参数，总在最前面传入handler，
         *      但是不会传入argHandler
         * @param {boolean=} options.once handler是否只调用一次后就注销
         * @param {boolean=} options.dontClone 是否禁用clone。默认不禁用。
         *      clone的用意是，每次创建一个参数副本给事件处理器，
         *      防止事件处理修改了参数而影响其他事件处理器的调用，
         *      只有普通Object和Array和基本类型能被clone，否则抛置异常。
         * @param {(Function|boolean)=} checkDeaf 检查是否deaf，deaf则不响应事件
         *      默认是true，如果传false则不检查，如果传function则用此function检查
         * @param {string} options.viewDisableDef 视图禁用定义
         * @param {(Array|Object)=} options.rule 事件路径定义
         */
        addEventListener: function (
            eventName, handler, scope, options
        ) {
            assert(
                    eventName && handler && scope,
                'Event listener can not be empty.'
            );

            options = options || {};
            var argHandler = options.argHandler;
            var dontClone = options.dontClone;
            var once = options.once;
            var checkDeaf = options.checkDeaf;
            var bindArgs = options.bindArgs || [];
            var id = options.id;
            var dispatcherId = options.dispatcherId;
            var interactionId = options.interactionId;
            var viewDisableDef = options.viewDisableDef;
            var rule = options.rule;
            var eventMatchMode = options.eventMatchMode;
            var eventAgent = getEventAgentByName(this, eventName) || this;

            var newHandler = function () {
                // 耳聋则不响应事件
                if (checkDeaf !== false
                    && (isFunction(checkDeaf)
                        ? checkDeaf(scope)
                        : (scope && scope.$di && scope.$di('isDeaf'))
                        )
                ) {
                    return;
                }

                // 处理diEvent
                var diEvent = arguments[0];
                var args = Array.prototype.slice.call(
                    arguments,
                    isDIEvent(diEvent)
                        ? (
                        // diEvent或者由事件dispatch者传来
                        //（从而支持interactPath）
                        diEvent = cloneEvent(diEvent),
                            1
                        )
                        : (
                        // diEvent未传来，则在此处创建。
                        diEvent = createDIEvent(eventName),
                            0
                        )
                );
                // 注入触发事件的ins的diId
                setEventAttr(diEvent, 'dispatcherId', dispatcherId, true);
                setEventAttr(diEvent, 'interactionId', interactionId, true);
                diEvent.viewDisableDef = viewDisableDef;

                // 对interactionRule求值
                if (rule && !evalJsonLogic(
                        rule,
                        bind(evalRule, null, diEvent)
                    )
                ) {
                    return;
                }

                // 克隆参数
                !dontClone && (args = argsClone(args));

                // 执行arg handler
                args = argHandler ? argHandler.call(scope, args) : args;

                // 设定interact memo
                scope.$di && setInteractMemo(scope, 'diEvent', diEvent);

                // 执行action
                var ret = handler.apply(scope, bindArgs.concat(args));

                // 清除interact memo
                scope.$di && setInteractMemo(scope, 'diEvent', UNDEFINED);

                return ret;
            };

            // FIXME 
            // 这部分应该拆出来。
            // 现在这么写耦合了ecui。这是现在还遗留的耦合ecui的地方，还没来得及改。
            // 考虑后续可能会引入其他控件库（比如嫌ecui重的时候），往后加else是较邋遢的结构。后续改。            
            if (eventAgent instanceof XOBJECT) {
                eventAgent[once ? 'attachOnce' : 'attach'](eventName, newHandler);
            }
            else if (isEcuiControl(eventAgent)) {
                ecuiAddEventListener(eventAgent, eventName, newHandler, once);
            }

            options = null;
        },

        /**
         * 分发事件
         * 目前只支持XObject和ecui
         *
         * @param {(string|DIEvent)} eventName 事件名（或者diEvent对象）
         * @param {Array} args 事件参数
         */
        dispatchEvent: function (eventName, args, options) {
            options = options || {};

            var eventAgent = getEventAgentByName(this, eventName) || this;

            // diEvent用以支持interactPath功能
            if (isDIEvent(eventName)) {

                // 这个限制，是为了保证：收到diEvent的eventHandler都是用$di('reportTemplate')注册的
                // 因为diEvent要暗自用第一个参数传递，$di('addEventListener')注册的才能识别
                assert(
                    eventAgent != this,
                    '如果使用diEvent，必须先registerEventAgent。'
                );

                // 暗自用第一个参数传递diEvent对象
                (args = args || []).splice(0, 0, eventName);
                eventName = eventName.getEventName();
            }

            // FIXME 
            // 这部分应该拆出来。后序改。说明同上面addEventListener中。            
            if (eventAgent instanceof XOBJECT) {
                eventAgent.notify(eventName, args);
            }
            else if (isEcuiControl(efventAgent)) {
                ecuiTriggerEvent(eventAgent, eventName, null, args);
            }
        }
    };

    var DEF_CONTRACT_METHOD = {
        getDIFactory: COMMON_CONTRACT_METHOD.getDIFactory,
        getMethod: function (methodName) {
            return DEF_CONTRACT_METHOD[methodName];
        },
        setId: function (id) {
            this.id = id;
        },
        getId: function () {
            return this.id;
        },
        getOpt: COMMON_CONTRACT_METHOD.getOpt,
        setOpt: COMMON_CONTRACT_METHOD.setOpt,
        getRef: COMMON_CONTRACT_METHOD.getRef
    };

    var COMPONENT_CONTRACT_METHOD = {
        setTplMode: function (tplMode) {
            setDIAttr(this, 'tplMode', tplMode);
        },

        getTplMode: function () {
            return getDIAttr(this, 'tplMode');
            // return getAttrIncludeGlobal(this, 'tplMode');
        },

        /**
         * 创建VUI实例
         * 如果工厂里有VUI定义，则用工厂里的定义创建，
         * 否则返回空
         *
         * 例：在component中创建一个vui，
         *  这个vui本身是一个ecui控件，
         *  如果在模板中有定义，则用模板中定义的创建，
         *  否则使用ecui的$fastCreate创建：
         *      var options = { myAttr1: 1, myAttr2: 'yyy' };
         *      this._uSomeControl = this.$di
         *          && this.$di('create', ['theVUINameInTpl', 1], options)
         *          || ecui.$fastCreate(ecui.ui.MyControl, mainEl, null, options);
         *
         * @param {string} refPath 引用定位路径，如'someAttr.some[4][5].some'
         * @param {Object=} options 被创建者需要的初始化参数
         * @return {Object} vui实例，如果创建失败则返回空
         */
        vuiCreate: function (refPath, options) {
            var def = this.$di('getRef', 'vuiRef', refPath, 'DEF');
            if (!def) { return null; }

            options = mergeOpt(
                def,
                extend({}, options, { id: def.id, el: def.el }),
                'DATA_INIT'
            );

            // vuiSet用于component引用自身的vui
            var vuiSet = getMakeAttr(this, 'vuiSet');
            var vuiSetKey = makePathKey(refPath);

            assert(
                !vuiSet[vuiSetKey],
                'vui已经存在: refPath=' + refPath + ' vuiSetKey=' + vuiSetKey
            );

            // 设置默认值
            if (getOpt(def, 'cfgOpt', 'paramMode') == null) {
                setOpt(def, 'cfgOpt', 'paramMode', 'NORMAL');
            }

            // 得到适配器和适配方法
            var adptMethod = def.adapterMethod || {};
            var adpt = def.adapter && def.adapter(def, options) || {};

            // 创建实例
            var ins;
            if (adpt['create']) {
                ins = adpt['create'](def, options);
            }
            else if (adptMethod['create']) {
                ins = generalAdapterMethod[adptMethod['create']](def, options);
            }

            // 实例创建失败
            if (!ins) {
                return null;
            }

            // 绑定$di
            ins.$di = $di.INS;

            // 家长的引用
            setDIAttr(ins, 'parent', this);

            // 设置基本属性
            setDIAttr(ins, 'id', def.id);
            setDIAttr(ins, 'el', def.el);
            if (def.el && def.el.style.display == 'none') {
                setDIAttr(ins, 'styleDisplay', def.styleDisplay);
            }

            // 保存实例
            vuiSet[vuiSetKey] = ins;
            setDIAttr(ins, 'parentVUISetKey', vuiSetKey);
            addEntity(ins, 'INS');

            // 拷贝adapter方法到实例上
            var setDataMethod;
            var methodName;
            for (methodName in adptMethod) {
                assert(
                    !COMMON_CONTRACT_METHOD[methodName],
                    'common contract method can not be overwrite!' + methodName
                );
                if (methodName != 'create') {
                    // adapter method专门存储。
                    // 这设计看起来不好看，但是够用了，不用加更复杂的机制。
                    // 因为：
                    // (1) 不宜让adapterMethod覆盖控件原有方法。
                    // 因为控件原有方法还可能被控件自身调用。
                    // (2) 不能在此处使用setDIMethod。
                    // 因为adapterMethod理应在installClz中挂上的DIMethod下层，
                    // 被它们调用。而如果此处用setDIMethod，就override了后者。
                    setDIAdapterMethod(
                        ins,
                        methodName,
                        generalAdapterMethod[methodName]
                    );
                }
            }
            for (methodName in adpt) {
                assert(
                    !COMMON_CONTRACT_METHOD[methodName],
                    'common contract method can not be overwrite!' + methodName
                );
                if (methodName != 'create') {
                    // 说明同上
                    setDIAdapterMethod(
                        ins,
                        methodName,
                        adpt[methodName]
                    );
                }
            }

            return ins;
        },

        /**
         * component获得自己的vui实例
         *
         * @public
         * @param {string} refPath 引用定位路径，如'someAttr.some[4][5].some'
         * @return {Object} vui实例
         */
        vuiGet: function (refPath) {
            return getMakeAttr(this, 'vuiSet')[makePathKey(refPath)];
        },

        /**
         * Component的getValue的统一实现，
         * 遍历每个vui，调用其getValue方法，
         * 用每个vui的name作为key，组成返回值对象。
         * （如果没有name，则不会被getValue），
         * 如果要控制某个vui的getValue，可自己实现vuiGetValue方法
         *
         * @public
         * @return {Object} value
         */
        getValue: function () {
            var def = this.$di('getDef');
            var valueDisabledMode = def.valueDisabledMode;

            var cmptValDisabled = this.$di('isValueDisabled');
            if (cmptValDisabled && valueDisabledMode == 'NORMAL') {
                return null;
            }

            var value = {};
            var vuiSet = getMakeAttr(this, 'vuiSet');
            var vuiIns;
            var vuiDef;
            var vuiValue;

            if (this.getValue) {
                value = this.getValue() || {};
            }

            var valDisabled;
            for (var refPathKey in vuiSet) {
                vuiIns = vuiSet[refPathKey];
                vuiDef = vuiIns.$di('getDef');
                valDisabled = cmptValDisabled || vuiIns.$di('isValueDisabled');

                if (vuiDef.name == null
                    || (valDisabled && valueDisabledMode == 'NORMAL')
                ) {
                    continue;
                }

                value[vuiDef.name] = valDisabled && valueDisabledMode == 'DI'
                    ? null
                    : (
                        isObject(vuiValue = vuiIns.$di('getValue'))
                            ? COMMON_PARAM_FACTORY.markParamMode(
                                vuiValue,
                                getOpt(vuiDef, 'cfgOpt', 'paramMode')
                            )
                            : vuiValue
                    );
            }

            return value;
        },

        /**
         * COMPONENT中，在interaction时得到event，
         * 其中含有disableFunc和enableFunc，
         * 调用则会执行disable和enable.
         * 用于在异步行为时做用户操作屏蔽。
         * 只能在interaction的action开始执行时调用
         *
         * @public
         * @return {Object} event
         *      {Function} event.disableFunc
         *      {Function} event.enableFunc
         */
        getEvent: function () {
            var event = getInteractMemo(this, 'diEvent');
            /*
             // 使用sync view disable配置代替
             var visDef = event.viewDisableDef;
             if (visDef) {
             var key = 'INTERACTION_VIEW_DISABLE_' + this.$di('getId');
             event.viewDisable = {
             disable: makeViewDisableFunc(visDef, 'disable', key),
             enable: makeViewDisableFunc(visDef, 'enable', key)
             }
             };
             */
            return event;
        },

        getEventChannel: getEventChannel,

        setCommonParamGetter: function (commonParamGetter) {
            setDIAttr(this, 'commonParamGetter', commonParamGetter);
        },

        getCommonParamGetter: function () {
            return getDIAttr(this, 'commonParamGetter');
        },

        getReportTemplateId: function () {
            var getter = getDIAttr(this, 'commonParamGetter');
            if (getter) {
                return getter.getReportTemplateId()
            }
        },

        /**
         * 报表跳转
         *
         * @protected
         * @param {string} linkBridgeType 跳转类型，值可为'I'(internal)或者'E'(external)
         * @param {string} url 目标url
         * @param {string} param 参数
         */
        linkBridge: function (linkBridgeType, url, param) {
            // 报表引擎内部处理，直接跳转
            if (linkBridgeType == 'I') {
                targetBlank(url + '?' + param);
            }
            // 给di-stub发事件，由引用报表引擎的系统来跳转
            else if (linkBridgeType == 'E') {
                instance.eventChannel && instance.eventChannel.triggerEvent(
                    'linkbridge',
                    [url, param]
                );
            }
        },

        /**
         * 报表刷新
         *
         * @protected
         * @param {Object} paramObj 新添加的参数
         * @param {Object} paramObj.reportImageId 镜像id
         */
        reloadReport: function (paramObj) {
            // 如果是从di-stub初始化的报表
            if (instance.diAgent === 'STUB') {
                instance.eventChannel.triggerEvent('reloadReport', [paramObj]);
            }
            // 如果是用户自己创建iframe加载的报表
            else {
                var url = window.location.href;
                if (url.indexOf('?') > 0) {
                    if (url.indexOf('reportImageId') > 0) {
                        var urlArr = url.split('&');
                        urlArr.splice(urlArr.length - 1, 1, 'reportImageId=' + paramObj.reportImageId);
                        url = urlArr.join('&');
                    }
                    else {
                        url += '&reportImageId=' + paramObj.reportImageId;
                    }
                }
                else {
                    url += '?reportImageId=' + paramObj.reportImageId;
                }
                window.location.href = url;
            }
        },

        /**
         * 执行view disable
         *
         * @protected
         * @param {string} actName 值为disable或者enable
         * @param {string} datasourceId
         */
        syncViewDisable: function (actName, datasourceId) {
            assert(
                    actName == 'enable' || actName == 'disable',
                    'Wrong actName: ' + actName
            );
            var def = this.$di('getDef');
            var key = 'ASYNC_VIEW_DISABLE_' + this.$di('getId');
            var vdDef = (def.sync || {}).viewDisable;
            doViewDisable(
                vdDef == 'ALL'
                    ? vdDef
                    : (isObject(vdDef) && vdDef[datasourceId]),
                actName,
                key
            );
        },

        /**
         * 因为功能权限而禁用vui, 此为默认行为，可重载改变
         *
         * @public
         */
        funcAuthVerify: function () {
            var vuiSet = getMakeAttr(this, 'vuiSet');
            var vuiIns;
            var vuiDef;

            for (var refPathKey in vuiSet) {
                vuiIns = vuiSet[refPathKey];
                vuiDef = vuiIns.$di('getDef');
                if (// 如果vui配了funcAuth，则要检查查权限
                    vuiDef.funcAuth
                    && !(vuiDef.funcAuth in instance.funcAuthKeys)
                ) {
                    // 没权限禁用
                    vuiIns.$di('getEl').style.display = 'none';
                    setDIAttr(vuiIns, 'hideByAuth', true);
                }
            }
        }
    };

    var VCONTAINER_CONTRACT_METHOD = {
        /**
         * 创建VPART实例
         * 如果工厂里有VPART定义，则用工厂里的定义创建，
         * 否则返回空
         *
         * @param {string} refPath 引用定位路径，如'someAttr.some[4][5].some'
         * @param {Object=} options 被创建者需要的初始化参数
         * @return {Object} vpart实例，如果创建失败则返回空
         */
        vpartCreate: function (refPath, options) {
            var def = this.$di('getRef', 'vpartRef', refPath, 'DEF');
            if (!def) { return null; }

            options = mergeOpt(
                def,
                extend({}, options, { id: def.id, el: def.el }),
                'DATA_INIT'
            );

            // vpartSet用于component引用自身的vpart
            var vpartSet = getMakeAttr(this, 'vpartSet');
            var vpartSetKey = makePathKey(refPath);

            assert(
                !vpartSet[vpartSetKey],
                'vpart已经存在: refPath=' + refPath + ' vpartSetKey=' + vpartSetKey
            );

            // 创建实例
            var ins = new def.clz(options);

            // 实例创建失败
            if (!ins) {
                return null;
            }

            // 绑定$di
            ins.$di = $di.INS;

            // 家长的引用
            setDIAttr(ins, 'parent', this);

            // 设置基本属性
            setDIAttr(ins, 'id', def.id);
            setDIAttr(ins, 'el', def.el);
            if (def.el && def.el.style.display == 'none') {
                setDIAttr(ins, 'styleDisplay', def.styleDisplay);
            }

            // 保存实例
            vpartSet[vpartSetKey] = ins;
            setDIAttr(ins, 'parentVPartSetKey', vpartSetKey);
            addEntity(ins, 'INS');

            return ins;
        },

        /**
         * vcontainer获得自己的vpart实例
         *
         * @public
         * @param {string} refPath 引用定位路径，如'someAttr.some[4][5].some'
         * @return {Object} vui实例
         */
        vpartGet: function (refPath) {
            return getMakeAttr(this, 'vpartSet')[makePathKey(refPath)];
        }
    };

    var VUI_CONTRACT_METHOD = {
        /**
         * vui本身要求提供setData方法，
         * vui提供的setData意为重新设置完全数据并渲染
         * 这里的setData方法又为vui的setData方法的加了一层包装，
         * 用于将模板里自定义的dataOpt与传入的options融合
         * （融合顺序依照mergeOpt方法的定义）。
         * Component对vui进行操作时须调用此setData方法，
         * （如：this._uSomeVUi.$di('setData', data);）
         * 而非直接调用vui本身提供的setData方法。
         *
         * @public
         * @param {*} data
         * @param {Object=} options 参数
         * @param {*=} options.forceData 最高merge优先级的data
         * @param {Object=} options.diEvent di事件
         */
        setData: function (data, options) {
            if (!this.$di) {
                return this.setData.apply(this, arguments);
            }

            var existMethod = getDIAdapterMethod(this, 'setData') || this.setData;
            if (existMethod) {
                options = options || {};
                var result = mergeOpt(
                    this.$di('getDef'), data, 'DATA_SET', options
                );
                // isSilent的统一支持
                return existMethod.call(this, result, data);
            }
        },

        /**
         * vui的getValue方法的封装
         *
         * @public
         */
        getValue: function () {
            if (!this.$di) {
                return this.setData.apply(this, arguments);
            }

            var existMethod = getDIAdapterMethod(this, 'getValue') || this.getValue;
            if (existMethod) {
                return this.$di('isValueDisabled')
                    ? null
                    : existMethod.call(this);
            }
        },

        /**
         * vui的init方法的封装
         *
         * @public
         */
        init: function () {
            if (!this.$di) {
                return this.init.apply(this, arguments);
            }

            var existMethod = getDIAdapterMethod(this, 'init') || this.init;
            mountInteractions(this);
            existMethod && existMethod.call(this);
        },

        /**
         * 析构
         *
         * @public
         */
        dispose: function () {
            if (!this.$di) {
                return this.dispose.apply(this, arguments);
            }

            var existMethod = getDIAdapterMethod(this, 'dispose') || this.dispose;
            var vuiSet = getDIAttr(getDIAttr(this, 'parent'), 'vuiSet');
            if (vuiSet) {
                delete vuiSet[getDIAttr(this, 'parentVUISetKey')];
            }
            removeEntity(this);
            existMethod && existMethod.call(this);
            this.$di('setEl', null);
        }
    };

    var VPART_CONTRACT_METHOD = {
        /**
         * 析构
         *
         * @public
         */
        dispose: function () {
            var vpartSet = getDIAttr(getDIAttr(this, 'parent'), 'vpartSet');
            if (vpartSet) {
                delete vpartSet[getDIAttr(this, 'parentVPartSetKey')];
            }
            removeEntity(this);
            this.dispose && this.dispose.call(this);
            this.$di('setEl', null);
        }
    }



    //----------------------------------------------------------------------
    // rule相关
    //----------------------------------------------------------------------

    /**
     * 处理interaction规则
     *
     * @private
     * @param {Object} diEvent
     * @param {Array.<Object>} atomRule
     *      结构例如：
     *      { operator: 'includes', interactionIds: ['aaaaa-rid1', 'aaaa-rid2' ]}
     * @return {boolean} 判断结果
     */
    function evalRule(diEvent, atomRule) {
        // 目前支持的operator：
        var ruleMap = {
            includes: evalRuleIncludesExcludes,
            excludes: evalRuleIncludesExcludes,
            equals: evalRuleEquals
        };

        assert(
                atomRule.operator in ruleMap,
                'Illegal rule: ' + atomRule.operator
        );

        return ruleMap[atomRule.operator](diEvent, atomRule);
    }

    /**
     * 处理interaction规则 incudes excludes
     *
     * @private
     * @param {Object} diEvent
     * @param {Array.<Object>} atomRule
     *      结构例如：
     *      { operator: 'includes', interactionIds: ['aaaaa-rid1', 'aaaa-rid2' ]}
     * @return {boolean} 判断结果
     */
    function evalRuleIncludesExcludes(diEvent, atomRule) {
        if (!diEvent) { return false; }

        var rSet = { includes: {}, excludes: {} };

        for (var j = 0; j < (atomRule.interactionIds || []).length; j ++) {
            rSet[atomRule.operator][atomRule.interactionIds[j]] = 1;
        }

        var path = getEventAttr(diEvent, 'interactPath');
        for (var i = 0, e, iid; e = path[path.length - i - 1]; i ++) {
            iid = getEventAttr(e, 'interactionId');

            if (iid in rSet.excludes) {
                return false;
            }

            if (rSet.includes[iid]) {
                delete rSet.includes[iid];
            }
        }

        if (!isEmptyObj(rSet.includes)) {
            return false;
        }

        return true;

        // TODO
        // 按路径模式匹配的代码（如下类似），后续有需求再加
        // for (
        //     var i = 0, e, eDef; 
        //     eDef = interactPathDef[dlen - i - 1], e = realPath[rlen - i - 1];
        //     i ++
        // ) {
        //     if (!eDef) {
        //         if (eventMatchMode == 'EXACT') { return false; }
        //         else { break; }
        //     }

        //     if (getEventAttr(e, 'dispatcherId') != eDef.dispatcherId
        //         || getEventAttr(e, 'eventName') != eDef.name
        //     ) {
        //         return false;
        //     }
        // }
    }

    /**
     * 处理interaction规则 equals
     *
     * @private
     * @param {Object} diEvent
     * @param {Array.<Object>} atomRule
     *      结构例如：
     *      { atomRule: 'equals', argHandlers: [ ... ], value: 1234 }
     * @return {boolean} 判断结果
     */
    function evalRuleEquals(diEvent, atomRule) {
        var val = parseArgHandlerDesc(atomRule).call(null, [])[0];
        return val == atomRule.value;
    }





    //-------------------------------------------------------------------
    // DI Event
    //-------------------------------------------------------------------

    /**
     * DI事件
     *
     * @private
     * @param {string=} eventName 事件名
     * @param {Object=} options 参数
     * @param {string=} options.dispatcherId 触发event的di ins的id
     * @param {string=} options.interactionId interaction的id
     * @param {string=} options.isClone 是否是clone
     * @param {Array.<Object>=} options.interactPath 事件路径
     * @return {Function} event实例
     */
    function createDIEvent(eventName, options) {
        options = options || {};

        var evt = function (eName) {
            return createDIEvent(
                eName,
                // interactPath上所有event对象都引用本interactPath
                { interactPath: evt[DI_EVENT_TAG].interactPath }
            );
        }

        // event对象中保存数据的地方
        var repo = evt[DI_EVENT_TAG] = {
            eventName: eventName,
            dispatcherId: options.dispatcherId,
            interactionId: options.interactionId,
            interactPath: (options.interactPath || []).slice()
        };

        // 最新一个event总在interactPath末尾
        var path = repo.interactPath;
        options.isClone
            ? path.splice(path.length - 1, 1, evt)
            : path.push(evt);

        // event对象的方法
        extend(evt, DI_EVENT_METHODS);

        return evt;
    };

    var DI_EVENT_METHODS = {
        /**
         * 得到事件名
         *
         * @public
         * @this {Object} diEvent对象
         * @return {string} 事件名
         */
        getEventName: function () {
            return this[DI_EVENT_TAG].eventName;
        },

        /**
         * 得到interactionId
         *
         * @public
         * @this {Object} diEvent对象
         * @return {string} interactionIdId
         */
        getInteractionId: function () {
            return this[DI_EVENT_TAG].interactionId;
        }

        /**
         * 是否为用户触发的事件中的第一个事件
         *
         * @public
         */
        // isUserFirst: function () {
        //     var path = this[DI_EVENT_TAG].interactPath;
        //     return path && path[0] && path[0].getEventName() != INIT_EVENT_NAME
        // },

        /**
         * 是否为自然初始化的事件中的第一个有效事件
         *
         * @public
         */
        // isInitFirst: function () {
        //     var path = this[DI_EVENT_TAG].interactPath;
        //     if (path 
        //         && path[0] 
        //         && path[0].getEventName() == INIT_EVENT_NAME
        //         && path[1] === this
        //     ) {
        //         return true;
        //     }
        //     else {
        //         return false;
        //     }
        // }
    };

    /**
     * 得到副本
     *
     * @public
     * @this {Event} 对象
     * @param {Object} event 事件对象
     * @return {string} 事件
     */
    function cloneEvent(event) {
        var repo = event[DI_EVENT_TAG];
        return createDIEvent(
            repo.eventName,
            {
                dispatcherId: repo.dispatcherId,
                interactionId: repo.interactionId,
                interactPath: repo.interactPath,
                isClone: true
            }
        );
    }

    /**
     * 得到event对象的属性值
     *
     * @private
     */
    function getEventAttr(event, attrName) {
        return event[DI_EVENT_TAG][attrName];
    }

    /**
     * 设置event对象的属性值
     *
     * @private
     */
    function setEventAttr(event, attrName, value, checkExist) {
        if (checkExist && event[DI_EVENT_TAG][attrName] !== UNDEFINED) {
            throw new Error('请使用diEvent("newEventName")创建新的diEvent实例');
        }
        event[DI_EVENT_TAG][attrName] = value;
    }

    /**
     * 是否为event对象
     *
     * @private
     */
    function isDIEvent(obj) {
        return isObject(obj) && obj[DI_EVENT_TAG];
    }





    //--------------------------------------------------------------------
    // DI Opt 相关方法
    //--------------------------------------------------------------------

    /**
     * 初始化opt
     * 现在支持的opt定义方式：
     *      (1) def[optName] ==> Object
     *      (2) def[optName + 's'] ==> Array
     *
     * @private
     * @param {Object} src 源
     * @param {string} optName opt名
     * @return {Object} opt
     */
    function initializeOpt(def, optName) {

        // 创建optCache
        var optCacheHome = def[DI_OPT_CACHE_TAG];
        if (!optCacheHome) {
            optCacheHome = def[DI_OPT_CACHE_TAG] = {};
        }
        optCacheHome[optName] = {};

        // 创建opt存储位置
        var optHome = def[DI_OPT_HOME_TAG];
        if (!optHome) {
            optHome = def[DI_OPT_HOME_TAG] = {};
        }

        var opt = optHome[optName] = def[optName] || {};
        var opts = optHome[optName + 's'] = def[optName + 's'] || [];

        // 删除def[optName]防止直接得到（只允许通过getOpt方法得到）
        def[optName] = null;
        def[optName + 's'] = null;

        // 生成id，用于optCache
        opt[DI_OPT_ID_TAG] = 'DI_OPT_' + getUID('DI_OPT');
        for (var i = 0; i < opts.length; i ++) {
            opts[i][optName][DI_OPT_ID_TAG] = 'DI_OPT_' + getUID('DI_OPT');
        }
    }

    /**
     * 提取定义的opt
     *
     * @private
     * @param {Object} src 源
     * @param {string} optName opt名
     * @param {string=} attr 属性名，如果为空，则得到opt本身
     * @param {Obejct=} options 参数
     * @param {Object=} options.diEvent di事件
     * @param {boolean=} options.clone 是否返回副本，默认是false
     * @return {Object} opt
     */
    function getOpt(def, optName, attr, options) {
        options = options || {};

        var optHome = def[DI_OPT_HOME_TAG];
        var optCache = def[DI_OPT_CACHE_TAG][optName];
        var opt = optHome[optName];
        var opts = optHome[optName + 's'];
        var diEvent = options.diEvent;
        var i;
        var o;
        var ret;
        var matchedOpt = [];
        var matchedIds = [];
        var evalRuleFunc = bind(evalRule, null, diEvent);

        matchedOpt.push(opt);
        matchedIds.push(opt[DI_OPT_ID_TAG]);

        // 根据rule找到匹配的opt
        for (i = 0; i < opts.length; i ++) {
            if ((o = opts[i])
                && o.rule
                && o[optName]
                && evalJsonLogic(o.rule, evalRuleFunc)
            ) {
                matchedOpt.push(o[optName]);
                matchedIds.push(o[optName][DI_OPT_ID_TAG]);
            }
        }

        var cacheKey = matchedIds.join(SEP);

        // 优先取缓存，否则merge
        if (!(ret = optCache[cacheKey])) {
            ret = optCache[cacheKey] = {};
            for (i = 0; i < matchedOpt.length; i ++) {
                merge(
                    ret,
                    matchedOpt[i],
                    { overwrite: true, clone: 'WITHOUT_ARRAY' }
                );
            }
        }

        if (attr != null) {
            ret = ret[attr];
        }

        return options.clone
            ? clone(ret, { exclusion: [DI_OPT_CACHE_TAG] })
            : ret;
    }

    /**
     * 设置opt
     *
     * @private
     * @param {Object} src 源
     * @param {string} optName 如cfgOpt、dataOpt
     * @param {string} attr 属性名
     * @param {*} value 属性值
     */
    function setOpt(def, optName, attr, value) {
        def[DI_OPT_HOME_TAG][optName][attr] = value;

        // 清除optcache
        def[DI_OPT_CACHE_TAG][optName] = {};
    }

    /**
     * 融合参数
     *
     * @public
     * @param {Object} def 目标实例定义
     * @param {Object} invokerData 调用者提供的options
     * @param {string} optType 可为'INIT', 'DATA'
     * @param {Object=} options
     * @param {Object=} options.forceData 最高等级的参数
     * @param {Object=} options.diEvent di事件
     */
    function mergeOpt(def, invokerData, optType, options) {
        def = def || {};
        options = options || {};
        var ret = {};

        // 使用了clone模式的merge，但是为减少消耗，不clone array
        var mOpt = { overwrite: true, clone: 'WITHOUT_ARRAY' };
        var mOpt2 = extend({}, mOpt, { exclusion: [DI_OPT_ID_TAG] });
        var optopt = { diEvent: options.diEvent };

        var clzDef = getClz(def.clzKey) || {};
        var clzDataOpt = getOpt(clzDef, 'dataOpt', null, optopt);
        var dataOpt = getOpt(def, 'dataOpt', null, optopt);

        merge(ret, clzDataOpt, mOpt2);
        merge(ret, invokerData, mOpt);
        merge(ret, dataOpt, mOpt2);

        if (optType == 'DATA_SET') {
            merge(ret, getOpt(def, 'dataSetOpt', null, optopt), mOpt2);
        }
        else if (optType == 'DATA_INIT') {
            merge(ret, getOpt(def, 'dataInitOpt', null, optopt), mOpt2);
        }
        else {
            throw new Error('error optType:' + optType);
        }

        options.forceData &&
        merge(ret, options.forceData, mOpt);

        return ret;
    }






    //-----------------------------------------------------------------------
    // Arg Handler 相关
    //-----------------------------------------------------------------------

    /**
     * 解析argHandler定义
     *
     * @param {Object} container 定义argHandler的容器
     * @param {Object=} scope 可缺省
     * @private
     */
    function parseArgHandlerDesc(container, scope) {
        var argH;
        var argHs = [];

        if (argH = container.argHandler) {
            argHs.push(argH);
        }
        if (argH = container.argHandlers) {
            argHs.push.apply(argHs, argH);
        }

        for (var i = 0; i < argHs.length; i ++) {
            argHs[i] = [scope].concat(argHs[i]);
        }

        return ARG_HANDLER_FACTORY.apply(null, argHs);
    }






    //-----------------------------------------------------------------------
    // DI Factory方法
    //-----------------------------------------------------------------------

    /**
     * 对注册的类实例化并enhance（对各种类挂载DI提供的方法）。
     * 各种方法，均用setDIMethod的方式绑定到类的prototype上。
     * (a) 不使用直接覆盖原有方法的方式，因为不能改变原有方法的行为，
     * 而原有方法还会被其他地方（如自身、如组合某控件的其他控件）调用。
     * (b) 挂载上DI方法后，DI对此类生成的实例的操作，均使用$('someMethod', ...)进行。
     *
     * @private
     */
    function installClz() {
        var clzKey;
        var clzDef;
        var proto;

        generalAdapterMethod = getByPath(
            GENERAL_ADAPTER_METHOD_PATH,
            $getNamespaceBase()
        );

        for (clzKey in DICT.CLZ) {
            instance.repository['CLZ'][clzKey] = clzDef = clone(DICT.CLZ[clzKey]);

            // 得到类实例
            if (clzDef.clzPath
                && (clzDef.clz = getByPath(clzDef.clzPath, $getNamespaceBase()))
            ) {
                proto = clzDef.clz.prototype;

                // 当有公用一个类时，不需要重复绑定了。
                if (!getDIAttr(proto, 'protoInstalled', true)) {
                    setDIAttr(proto, 'protoInstalled', 1);

                    // 绑定$di
                    proto.$di = $di.INS;

                    // 添加约定方法
                    mountMethod(
                        proto,
                        [
                            'start',
                            'getDIFactory',
                            'setId',
                            'getId',
                            'getDef',
                            'isDeaf',
                            'setDeaf',
                            'setEl',
                            'getEl',
                            'disable',
                            'enable',
                            'diShow',
                            'diHide',
                            'setValueDisabled',
                            'isValueDisabled',
                            'addEventListener',
                            'dispatchEvent',
                            'registerEventAgent',
                            'getOpt',
                            'setOpt',
                            'getRef'
                        ],
                        COMMON_CONTRACT_METHOD
                    );

                    if (clzDef.clzType == 'COMPONENT') {
                        mountMethod(
                            proto,
                            [
                                'setTplMode',
                                'getTplMode',
                                'vuiCreate',
                                'vuiGet',
                                'getValue',
                                'getEvent',
                                'getEventChannel',
                                'getCommonParamGetter',
                                'setCommonParamGetter',
                                'getReportTemplateId',
                                'linkBridge',
                                'syncViewDisable',
                                'funcAuthVerify',
                                'reloadReport'
                            ],
                            COMPONENT_CONTRACT_METHOD
                        );
                    }

                    if (clzDef.clzType == 'VCONTAINER') {
                        mountMethod(
                            proto,
                            [
                                'vpartCreate',
                                'vpartGet'
                            ],
                            VCONTAINER_CONTRACT_METHOD
                        );
                    }

                    if (clzDef.clzType == 'VUI') {
                        mountMethod(
                            proto,
                            [
                                'setData',
                                'getValue',
                                'init',
                                'dispose'
                            ],
                            VUI_CONTRACT_METHOD
                        );
                    }

                    if (clzDef.clzType == 'VPART') {
                        mountMethod(
                            proto,
                            [
                                'dispose'
                            ],
                            VPART_CONTRACT_METHOD
                        );
                    }

                    // 赋予类型
                    setDIMethod(
                        proto,
                        'getClzType',
                        (function (clzType) {
                            return function () { return clzType; }
                        })(clzDef.clzType)
                    );
                }
            }

            // 得到adapter实例
            clzDef.adapterPath && (
                clzDef.adapter =
                    getByPath(clzDef.adapterPath, $getNamespaceBase())
                );

            // 选项初始化
            initializeOpt(clzDef, 'dataOpt');
            initializeOpt(clzDef, 'dataInitOpt');
            initializeOpt(clzDef, 'dataSetOpt');
            initializeOpt(clzDef, 'valueGetOpt');
            initializeOpt(clzDef, 'cfgOpt');
        }
    }

    /**
     * 为类挂载di的方法。如果类中已经有此方法，则不挂载。
     *
     * @private
     * @param {Object} proto 类的prototype
     * @param {Array.<string>} methodNameList 方法名
     * @param {Array.<string>} methodSet 方法集合
     */
    function mountMethod(proto, methodNameList, methodSet) {
        for (
            var i = 0, methodName, prefixedMethodName;
            methodName = methodNameList[i];
            i ++
        ) {
            setDIMethod(proto, methodName, methodSet[methodName]);
        }
    }

    /**
     * 创建di实例
     *
     * @private
     * @param {Object} def 实例定义
     * @param {Object} options 初始化参数
     * @param {string} options.tplMode （默认为'FROM_SNIPPET'）
     * @param {string} options.commonParamGetter
     * @return {Object} 创建好的实例
     */
    function createIns(def, options) {
        options = options || {};
        // 为了下面new时能在构造方法中访问这些数据，
        // 所以放到globalTemp中
        var opt = {
            id: def.id,
            el: def.el,
            // 标志html片段从snippet中取，而不是组件自己创建
            tplMode: options.tplMode || 'FROM_SNIPPET',
            commonParamGetter: options.commonParamGetter
        };
        opt[DI_TMP_TAG] = extend({}, opt);

        var ins = new def.clz(
            mergeOpt(def, extend(options, opt), 'DATA_INIT')
        );

        addEntity(ins);

        return ins;
    }

    /**
     * 根据配置，挂载多个interaction
     *
     * @public
     * @param {Object} ins 实例
     */
    function mountInteractions(ins) {
        var def = ins.$di('getDef');

        // 模板中定义的事件绑定(interaction)
        if (!def.interactions) { return; }

        for (
            var i = 0, interact;
            interact = def.interactions[i];
            i ++
        ) {
            mountInteraction(ins, interact);
        }
    }

    /**
     * 根据配置，挂载interaction
     *
     * @public
     * @param {Object} ins 实例
     */
    function mountInteraction(ins, interact) {
        var def = ins.$di('getDef');

        var events = [];
        interact.event && events.push(interact.event);
        interact.events && arrayPush.apply(events, interact.events);

        for (var j = 0, evt, triggerIns; j < events.length; j ++) {
            evt = events[j];
            triggerIns = evt.triggerIns || getEntity(evt.rid, 'INS');

            // 设置这个断言的部分原因是，vui事件不保证能提供diEvent
            assert(
                triggerIns.$di('getDef').clzType != 'VUI',
                '不允许监听vui事件'
            );

            if (!triggerIns) { return; }

            triggerIns.$di(
                'addEventListener',
                evt.name,
                getDIMethod(ins, interact.action.name),
                ins,
                {
                    interactionId: interact.id,
                    dispatcherId: evt.rid,
                    argHandler: parseArgHandlerDesc(interact, ins),
                    once: interact.once,
                    viewDisableDef: interact.viewDisable,
                    rule: evt.rule
                        ? ['and', interact.rule, evt.rule]
                        : interact.rule
                }
            );
        }
    }

    /**
     * 根据引用路径（refPath）得到引用。
     * 路径可直接指向对象树叶节点，也可以指向途中的节点。
     *
     * @public
     * @param {Object} obj 目标INS或者DEF
     * @param {string} refName 如'vuiRef'，'vpartRef'
     * @param {string} refPath 引用定位路径，如'someAttr.some[4][5].some'
     * @param {string=} mode 值为'DEF'（默认）或者'INS'
     * @param {Object=} options 选项
     * @param {boolean=} options.flatReturn
     *      true则返回一个数组，里面是所有目标实例，
     *      false则返回源结构，里面的id会替换为目标实例（默认）。
     * @return {(Array.<Object>|Object)} ref数组或者ref项
     */
    function getRef(obj, refName, refPath, mode, options) {
        options = options || {};

        var refBase = (
            getEntity(obj.$di('getId'), 'DEF') || {}
        )[refName];

        if (!refBase) { return null; }

        return findEntity(
            getByPath(refPath, refBase),
            mode,
            { isClone: true, flatReturn: options.flatReturn }
        );
    }

    /**
     * 设置方法，如果已经有此方法的话（除非在prototype上），报错（以免后续开发中弄错）
     *
     * @private
     * @param {Object} o 类的prototype或者实例
     * @param {string} methodName 方法名
     * @param {Function} method 方法
     */
    function setDIMethod(o, methodName, method) {
        var pName = DI_METHOD_PREFIX + methodName;
        assert(!o.hasOwnProperty(pName), 'diMethod exists! ' + methodName);
        o[pName] = method;
    }

    /**
     * 获取方法
     *
     * @private
     * @param {Object} o 类的prototype或者实例
     * @param {string} methodName 方法名
     * @return {Function} method 方法
     */
    function getDIMethod(o, methodName) {
        // 寻找di挂载的方法
        return o[DI_METHOD_PREFIX + methodName]
            // 如果找不到，则返回同名原有方法
            || o[methodName];
    }

    /**
     * 设置adapter方法，如果已经有此方法的话（除非在prototype上），报错（以免后续开发中弄错）
     *
     * @private
     * @param {Object} o 类的prototype或者实例
     * @param {string} methodName 方法名
     * @param {Function} method 方法
     */
    function setDIAdapterMethod(o, methodName, method) {
        var pName = DI_ADAPTER_METHOD_PREFIX + methodName;
        assert(!o.hasOwnProperty(pName), 'diAdapterMethod exists! ' + methodName);
        o[pName] = method;
    }

    /**
     * 获取adapter方法
     *
     * @private
     * @param {Object} o 类的prototype或者实例
     * @param {string} methodName 方法名
     * @return {Function} method 方法
     */
    function getDIAdapterMethod(o, methodName) {
        return o[DI_ADAPTER_METHOD_PREFIX + methodName];
    }

    /**
     * 得到类
     *
     * @public
     * @param {string} clzKey 类的key
     * @return {Object} clzDef 类定义
     *      clzDef.clz 类
     *      clzDef.clzKey 类key
     *      clzDef.clzPath 类路径
     *      clzDef.adapterPath 适配器路径
     *      clzDef.adapter 适配器
     *      clzDef.dataOpt 初始化参数
     */
    function getClz(clzKey) {
        return instance.repository['CLZ'][clzKey];
    }

    /**
     * 添加实体（ins或def）
     *
     * @public
     * @param {Object} o 实例或实例定义
     * @param {string} mode 'INS'（默认）, 'DEF'
     * @return {DIFactory} 本身
     */
    function addEntity(o, mode) {
        if (mode == 'DEF') {
            if (o.clzType && o.id) {

                // 装上clz
                var clzDef = getClz(
                        o.clzKey || DICT.DEFAULT_CLZ_KEY[o.clzType]
                );
                o = merge(clone(clzDef), o);

                // def标志
                o[DI_DEF_TAG] = true;

                // 赋予$di
                o.$di = $di.DEF;

                // 选项初始化
                initializeOpt(o, 'dataOpt');
                initializeOpt(o, 'dataInitOpt');
                initializeOpt(o, 'dataSetOpt');
                initializeOpt(o, 'valueGetOpt');
                initializeOpt(o, 'cfgOpt');

                // 保存
                instance.repository[o.clzType + '_DEF'][o.id] = assign({}, o);
            }
        }
        else {
            instance.repository[o.$di('getClzType')][o.$di('getId')] = o;
        }
        return instance;
    }

    /**
     * 删除实例
     *
     * @public
     * @param {Object} o 实例或实例定义
     */
    function removeEntity(o) {
        if (o[DI_DEF_TAG]) {
            delete instance.repository[o.clzType + '_DEF'][o.id];
        }
        else {
            delete instance.repository[o.$di('getClzType')][o.$di('getId')];
        }
    }

    /**
     * 得到实例
     *
     * @private
     * @param {string} id 实例id
     * @param {string} mode 'INS', 'DEF'（默认）, 'RAW'（原定义对象，内部使用）
     * @return {Object} 实例
     */
    function getEntity(id, mode) {
        var suffix = mode == 'INS' ? '' : '_DEF';
        var o;
        var ret;
        var optCache;

        for (var i = 0, clzType; clzType = INS_CLZ_TYPE[i]; i ++) {
            if (clzType != 'CLZ'
                && (o = instance.repository[clzType + suffix][id])
            ) {
                if (mode == 'INS' || mode == 'RAW') {
                    return o;
                }
                // mode为'DEF'则返回副本
                else {
                    ret = clone(o, { exclusion: [DI_OPT_CACHE_TAG] });
                    // 不克隆optCache节省开销
                    ret[DI_OPT_CACHE_TAG] = o[DI_OPT_CACHE_TAG];
                    return ret;
                }
            }
        }
        return null;
    }

    /**
     * 为对象装填ins或def，或者返回装填好的副本
     *
     * @public
     * @param {(Object|Array)} target 目标对象中，
     *      只可以含有Object或Array或实例id
     * @param {string} mode 'INS', 'DEF'（默认）
     * @param {Object=} options 选项
     * @param {boolean=} options.flatReturn
     *      true则返回一个数组，里面是所有目标实例，
     *      false则返回源结构，里面的id会替换为目标实例（默认）。
     * @param {boolean=} options.isClone 是否是clone模式，
     *      true则不修改target，返回值是新对象，
     *      false则修改target，返回target。（默认）
     * @return {Object} target 源对象
     */
    function findEntity(target, mode, options) {
        options = options || {}
        var result;
        var i;
        var flatRet = options.flatReturn ? [] : null;

        if (isArray(target)) {
            result = options.isClone ? [] : target;
            for (i = 0; i < target.length; i ++) {
                target.hasOwnProperty(i)
                && (result[i] = findEntity(target[i], mode));
            }
        }
        else if (isObject(target)) {
            result = options.isClone ? {} : target;
            for (i in target) {
                target.hasOwnProperty(i)
                && (result[i] = findEntity(target[i], mode));
            }
        }
        else {
            result = getEntity(target, mode);
            flatRet && flatRet.push(result);
        }

        return flatRet ? flatRet : result;
    }

    /**
     * 遍历unit
     *
     * @protected
     * @param {(string|Array)} clzType 单值或数组，
     *      如果是数组，则顺序遍历
     * @param {Function} callback 回调，参数为
     *              {Object} def
     *              {Object} ins
     *              {string} id
     */
    function forEachEntity(clzType, callback) {
        clzType = isString(clzType)
            ? [clzType] : (clzType || []);

        for (var i = 0, c, repoIns, repoDef; c = clzType[i]; i ++) {
            var repoDef = instance.repository[c + '_DEF'];
            var repoIns = instance.repository[c];
            for (var id in repoDef) {
                repoDef[id] && callback(repoDef[id], repoIns[id], id);
            }
        }
    }

    /**
     * 设置di私有的属性
     *
     * @private
     * @param {Object} o 目标ins
     * @param {string} attrName 属性名
     * @param {*} attrValue 属性值
     */
    function setDIAttr(o, attrName, attrValue) {
        if (o && attrName != null) {
            o[DI_ATTR_PREFIX + attrName] = attrValue;
        }
    }

    /**
     * 得到di私有的属性
     *
     * @private
     * @param {Object} o 来源ins
     * @param {string} attrName 属性名
     * @param {bolean} notProto 排除prototype上的，默认为false
     * @return {*} attrValue 属性值
     */
    function getDIAttr(o, attrName, notProto) {
        if (o && attrName != null) {
            var name = DI_ATTR_PREFIX + attrName;
            return (!notProto || o.hasOwnProperty(name)) ? o[name] : null;
        }
        return null;
    }

    /**
     * 获得对象，如果没有就创建
     *
     * @param {Object} di实例
     * @param {string} attrName
     * @param {*=} makeValue 如果没有，则创建的值，默认为{}
     * @private
     */
    function getMakeAttr(ins, attrName, makeValue) {
        if (makeValue === UNDEFINED) {
            makeValue = {};
        }
        var value = getDIAttr(ins, attrName);
        if (value === UNDEFINED) {
            setDIAttr(ins, attrName, value = makeValue);
        }
        return value;
    }

    /**
     * 得到di私有的属性，如果没有则从global中取
     * 专用于new创建时
     *
     * @private
     * @param {Object} o 来源对象
     * @param {string} attrName 属性名
     * @return {*} attrValue 属性值
     */
    function getAttrIncludeGlobal(o, attrName) {
        var ret = getDIAttr(o, attrName);
        if (ret == null) {
            ret = (getGlobalTemp('DI_DEF_FOR_NEW') || {})[attrName];
        }
        return ret;
    }

    /**
     * 设置事件通道
     *
     * @public
     * @param {Object} ec 事件通道
     */
    function setEventChannel(ec) {
        instance.eventChannel = ec;
    }

    /**
     * 得到事件通道
     *
     * @public
     * @param {Object} 事件通道
     */
    function getEventChannel() {
        return instance.eventChannel;
    }

    /**
     * 设置或获取临时全局参数
     * 除非一些不好处理的问题，
     * 否则不建议使用！
     *
     * @public
     * @param {string} key 使用者标志
     * @param {*} data
     */
    function setGlobalTemp(key, data) {
        instance.globalTempData[key] = data;
    }

    /**
     * 设置或获取临时全局参数
     * 除非一些不好处理的问题，
     * 否则不建议使用！
     *
     * @public
     * @param {string} key 使用者标志
     * @return {*} data
     */
    function getGlobalTemp(key) {
        return instance.globalTempData[key];
    }

    /**
     * refPath变成唯一的key
     *
     * @private
     */
    function makePathKey(refPath) {
        return refPath.replace(/[\]\s]/g, '').replace(/\[/g, '.');
    }

    /**
     * 创建事件代理
     *
     * @private
     */
    function registerEventAgent(obj, eventName) {
        var agent = obj[DI_EVENT_AGENT_TAG];
        if (!agent) {
            agent = obj[DI_EVENT_AGENT_TAG] = new XOBJECT();
            agent.eventNameMap = {};
        }
        if (eventName != null) {
            agent.eventNameMap[eventName] = 1;
        }
        else {
            agent.eventNameAll = 1;
        }
    }

    /**
     * 得到事件代理
     *
     * @private
     */
    function getEventAgentByName(obj, eventName) {
        var agent = obj[DI_EVENT_AGENT_TAG];
        if (agent
            && (
                agent.eventNameAll
                || agent.eventNameMap[eventName]
                )
        ) {
            return agent;
        }
    }

    // @deprecated
    // 为兼容原有报表而保留
    function rootSnippet(id) {
        if (!id && !rootSnippet) {
            return null;
        }
        id && (rootSnippet = id) || (id = rootSnippet);
        var def = getEntity(id, 'DEF');
        return def;
    }

    /*
     function makeViewDisableFunc(disDef, actName, key) {
     if (!disDef) { return null; }

     var repCmpt = instance.repository['COMPONENT'];
     var repCtnr = instance.repository['VCONTAINER'];

     if (disDef == 'ALL') {
     disDef = [];
     for (id in repCmpt) { disDef.push(id); }
     for (id in repCtnr) { disDef.push(id); }
     }

     return function () {
     for (var i = 0, ins, id; i < disDef.length; i ++) {
     id = disDef[i];
     ins = repCmpt[id] || repCtnr[id];
     ins && ins.$di(actName, key);
     }
     }
     }*/

    function setFuncAuth(auth) {
        if (!auth) {
            return;
        }
        instance.funcAuthKeys = {};
        for (var i = 0; i < (auth || []).length; i ++) {
            instance.funcAuthKeys[auth[i]] = 1;
        }
    }

    function doViewDisable(disDef, actName, key) {
        if (!disDef) { return null; }

        var repCmpt = instance.repository['COMPONENT'];
        var repCtnr = instance.repository['VCONTAINER'];

        if (disDef == 'ALL') {
            disDef = [];
            for (var id in repCmpt) { disDef.push(id); }
            for (var id in repCtnr) { disDef.push(id); }
        }

        for (var i = 0, ins, id; i < disDef.length; i ++) {
            id = disDef[i];
            ins = repCmpt[id] || repCtnr[id];
            ins && ins.$di(actName, key);
        }
    }

    function setInteractMemo(ins, attr, value) {
        var memo = getDIAttr(ins, 'interactMemo');
        if (!memo) {
            setDIAttr(ins, 'interactMemo', memo = {});
        }
        if (value !== UNDEFINED) {
            memo[attr] = value;
        }
        else {
            delete memo[attr];
        }
    }

    function getInteractMemo(ins, attr) {
        var memo = getDIAttr(ins, 'interactMemo');
        return memo ? memo[attr] : UNDEFINED;
    }

    /**
     * 参数clone
     * 如果不为可clone的类型，则抛出异常
     *
     * @private
     * @param {*} args
     * @return {*} clone结果
     */
    function argsClone(args) {
        var result;
        var i;
        var len;
        var objStr = objProtoToString.call(args);
        var isArr;

        if (objStr == '[object Date]') {
            result = new Date(args.getTime());
        }
        else if (
            objStr == '[object Function]'
            || objStr == '[object RegExp]'
        ) {
            result = args;
        }
        else if (
        // array也用下面方式复制，从而非数字key属性也能被复制
            (isArr = objStr == '[object Array]')
            // 对于其他所有Object，先检查是否是可以拷贝的object，
            // 如果不是，抛出异常，防止隐含错误
            || args === Object(args)
        ) {
            result = isArr ? [] : {};
            !isArr && checkObjectClonable(args);
            for (i in args) {
                if (args.hasOwnProperty(i)) {
                    result[i] = argsClone(args[i]);
                }
            }
        }
        else {
            result = args;
        }
        return result;
    }

    /**
     * 检查对象是否可以拷贝。
     * 如果不可以，抛出异常；
     */
    function checkObjectClonable(obj) {
        var clonable = true;

        // 排除DOM元素
        if (Object.prototype.toString.call(obj) != '[object Object]'
            // 但是在IE中，DOM元素对上一句话返回true，
            // 所以使用字面量对象的原型上的isPrototypeOf来判断
            || !('isPrototypeOf' in obj)) {
            clonable = false;
        }

        // 试图排除new somefunc()创建出的对象
        if (// 如果没有constructor则通过
            obj.constructor
            // 有constructor但不在原型上时通过
            && !hasOwnProperty.call(obj, 'constructor')
            // 用isPrototypeOf判断constructor是否为Object对象本身
            && !hasOwnProperty.call(obj.constructor.prototype, 'isPrototypeOf')
        ) {
            clonable = false;
        }

        if (!clonable) {
            throw new Error('Object can not be clone: ' + obj);
        }
    };

    /**
     * instance下面的变量回归默认状态
     */
    function resetInstanceAttributes() {
        /**
         * 库
         */
        instance.repository = {
            // 类库
            CLZ: {},
            // 各种实例库
            SNIPPET: {},
            SNIPPET_DEF: {},
            COMPONENT: {},
            COMPONENT_DEF: {},
            VUI: {},
            VUI_DEF: {},
            VCONTAINER: {},
            VCONTAINER_DEF: {},
            VPART: {},
            VPART_DEF: {}
        };
        /**
         * 根snippet
         */
        instance.rootSnippetId = null;

        /**
         * 对外事件通道
         */
        instance.eventChannel = null;

        /**
         * 设置或获取临时全局参数，参见setGlobalTemp
         */
        instance.globalTempData = {};

        /**
         * 功能权限key集合
         */
        instance.funcAuthKeys = {};

        /**
         * 客户端标志（STUB、或空）
         */
        instance.diAgent = null;

        /**
         * 报表镜像id
         */
        instance.reportImageId = null;
    }

})();