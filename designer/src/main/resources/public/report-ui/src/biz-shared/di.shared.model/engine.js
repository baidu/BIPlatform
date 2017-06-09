/**
 * di.product.display.ui.Engine
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    报表展示页面
 * @author:  lizhantong(lztlovely@126.com)
 * @depend:  xui, xutil
 */

$namespace('di.shared.model');

(function () {

    var inheritsObject = xutil.object.inheritsObject;
    var XVIEW = xui.XView;
    var SNIPPET_PARSER = di.helper.SnippetParser;
    var extend = xutil.object.extend;
    var merge = xutil.object.merge;
    var DICT = di.config.Dict;
    var DI_FACTORY;
    var COMMON_PARAM_FACTORY;


    $link(function () {
        URL = di.config.URL;
        DI_FACTORY = di.shared.model.DIFactory;
        COMMON_PARAM_FACTORY = di.shared.model.CommonParamFactory;
    });

    /**
     * 报表展示页面
     *
     * @class
     * @extends xui.XView
     */
    var ENGINE = $namespace().Engine =
        inheritsObject(XVIEW, constructor);
    var ENGINE_CLASS = ENGINE.prototype;

    /**
     * 构造函数
     *
     * @constructor
     * @public
     */
    function constructor(options) {
        // web根路径
        URL.setWebRoot(options.webRoot);

        this._diFactory = DI_FACTORY();
        this._diFactory.dispose();

        this._engineOptions = options || {};
        this._engineOptions.extraOpt = this._engineOptions.extraOpt || {};

        // 设置功能权限
        this._diFactory.setFuncAuth(this._engineOptions.funcAuth);
    }

    /**
     * 启动引擎
     *
     * @public
     * @param {Object} depict 组件json对象
     * @param {Object} depict.entityDefs 实例定义
     */
    ENGINE_CLASS.start = function (depict) {
        var me = this;
        var diFactory = me._diFactory;
        var engineOptions = me._engineOptions;
        var def;

        // 将每个报表特有的depict和公共定义的clzDef融合
        mergeClzDef(depict);

        // 初始化repo中的所有class
        diFactory.installClz();

        // 解析snippet生成def
        SNIPPET_PARSER().parseProdSnippet(
            engineOptions.reportBody || document,
            depict,
            engineOptions,
            diFactory
        );

        var eParam = engineOptions.externalParam = engineOptions.externalParam || {};
        // prod端的标志，用于后台日志记录
        eParam._V_SRC = 'PROD';
        var commonParamFactory = new COMMON_PARAM_FACTORY(
            { externalParam: eParam }
        );

        // 创建commonParamGetter
        var pGetterMapByRTPL = {};
        var pGetterMapByEntity = {};
        var rtplRoot;
        var rootSnippet = diFactory.rootSnippet();
        diFactory.forEachEntity(
            ['COMPONENT'],
            function (def, ins, id) {
                var belongSnippet = def.belong.snippet;

                // 创建commonParamGetter
                var rtplId = def.reportId
                    // 如果component上没有reportId，则从snippet上找
                    || findEntityDef(depict, belongSnippet).reportId
                    // 如果没指定reportId，默认均为最外层reportId
                    || 'RTPL_VIRTUAL_ID';

                // 寻找rootCmpts
                // FIXME
                // @deprecated
                // 对根snippet，用vm中的reportId更新（因为此时已为session loaded）
                // 这是之前的做法，现在淡化snippet的概念，不使用根snippet了，
                // 而是遍历component，找reportType为RTPL_VIRTUAL的，进行替换。
                if (rootSnippet && rootSnippet == belongSnippet) {
                    rtplRoot = rtplId;
                }

                // 创建commonParamGetter，以reportId为单位。
                // 每个commonParamGetter对应一个后台的sessinoLoaded实例，
                // 因为有需要多个conponent对应一个sessionLoaded实例的情况，
                // （如meta-config（即拖拽改维度）和olap-table需要共享sessionLoaded），
                // 所以commonParamGetter现在定为以reportId为单位，而不是以component。
                var pGetter;
                if (!(pGetter = pGetterMapByRTPL[rtplId])) {
                    pGetter = pGetterMapByRTPL[rtplId] =
                        commonParamFactory.getGetter(
                            { reportId: rtplId }
                        );
                    // 此为定位问题方便而纪录
                    pGetter.___rtplId = rtplId;
                    pGetter.___defId = id;
                }
                pGetterMapByEntity[id] = pGetter;
            }
        );

        // 更新root的reportId
        var rootGetter = pGetterMapByRTPL[rtplRoot]
            || pGetterMapByRTPL[engineOptions.persistentreportId]
            || pGetterMapByRTPL['RTPL_VIRTUAL_ID']

        // 存在没有rootGetter的情况
        rootGetter && rootGetter.update(
            { reportId: engineOptions.reportId }
        );

        // 创健建实例
        diFactory.forEachEntity(
            [
                'SNIPPET',
                'VCONTAINER',
                'COMPONENT'
            ],
            function (def, ins, id) {
                var options = {};

                // 设置上通用请求参数获取器
                if (def.clzType == 'COMPONENT') {
                    options.commonParamGetter = pGetterMapByEntity[def.id];

                    // 设置默认值
                    /**
                     * valueDisabledMode, 值可为：
                     *      'NORMAL'：如果disabled则不传参数
                     *      'DI'：如果disabled则传参数值为空（如asdf=&zxcv=)
                     *          （因为di中参数值为空则表示清空，不传则表示保留）
                     */
                        def.valueDisabledMode == null
                        && (def.valueDisabledMode = 'DI');
                }

                // 创建实例
                diFactory.createIns(def, options);
            }
        );

        // rendered事件
        diFactory.forEachEntity(
            ['VCONTAINER', 'COMPONENT'],
            function (def, ins, id) {
                ins.$di(
                    'addEventListener',
                    'rendered',
                    me.$invalidateView,
                    me
                );
            }
        );

        // component事件绑定
        diFactory.forEachEntity(
            'COMPONENT',
            function (def, ins, id) {
                diFactory.mountInteractions(ins);
            }
        );

        // 初始化
        diFactory.forEachEntity(
            [
                'SNIPPET',
                'VCONTAINER',
                'COMPONENT'
            ],
            function (def, ins, id) {
                ins.init();
            }
        );

        diFactory.addEntity(
            def = {
                "clzType": "COMPONENT",
                "id": diFactory.INIT_EVENT_AGENT_ID,
                "clzKey": "GENERAL_COMPONENT"
            },
            'DEF'
        );
        def = diFactory.getEntity(def.id, 'DEF');
        var initEventAgent = diFactory.createIns(def);

        // 初始化后行为
        diFactory.forEachEntity(
            'COMPONENT',
            function (def, ins, id) {
                if (def.init) {
                    diFactory.mountInteraction(
                        ins,
                        extend(
                            {
                                event: {
                                    rid: diFactory.INIT_EVENT_AGENT_ID,
                                    name: diFactory.INIT_EVENT_NAME
                                }
                            },
                            def.init
                        )
                    );
                }
            }
        );

        // 功能权限验证
        diFactory.forEachEntity(
            [ 'COMPONENT' ],
            function (def, ins, id) {
                ins.$di('funcAuthVerify');
            }
        );

        // 触发init事件
        initEventAgent.$di('dispatchEvent', diFactory.INIT_EVENT_NAME);
    }

    /**
     * 获取diFactory实例
     *
     * @public
     * @param {Object} depict 组件json对象
     * @param {Object} depict.entityDefs 实例定义
     */
    ENGINE_CLASS.getDIFactory = function () {
        return this._diFactory;
    };

    /**
     * 获得depict的内容
     * 获得depict的内容
     *
     * @public
     */
    ENGINE_CLASS.mergeDepict = function (rptJsonArray) {
        // 定义在snippet文件中的depict优先级最高
        rptJsonArray.splice(0, 0, this._engineOptions.extraOpt.depict || {});

        var rootSnippet;
        var prompt = {};
        var clzDefsMap = {};
        var entityDefsMap = [];
        var key;

        for (var i = 0, de; i < rptJsonArray.length; i ++) {
            if (de = rptJsonArray[i]) {
                if (de.rootSnippet) {
                    rootSnippet = de.rootSnippet;
                }
                if (de.prompt) {
                    merge(prompt, de.prompt);
                }

                var j;
                var def;
                var o;

                for (j = 0; j < (de.clzDefs || []).length; j ++) {
                    // clz定义
                    if ((def = de.clzDefs[j]) && (key = def.clzKey)) {
                        if (!(o = clzDefsMap[key])) {
                            o = clzDefsMap[key] = {};
                        }
                        merge(o, def);
                    }
                }

                for (j = 0; j < (de.entityDefs || []).length; j ++) {
                    // entity定义
                    if ((def = de.entityDefs[j]) && (key = def.id)) {
                        if (!(o = entityDefsMap[key])) {
                            o = entityDefsMap[key] = {};
                        }
                        merge(o, def);
                    }
                }
            }
        }

        var clzDefs = [];
        for (key in clzDefsMap) {
            clzDefs.push(clzDefsMap[key]);
        }
        var entityDefs = [];
        for (key in entityDefsMap) {
            entityDefs.push(entityDefsMap[key]);
        }

        return {
            rootSnippet: rootSnippet,
            prompt: prompt,
            clzDefs: clzDefs,
            entityDefs: entityDefs
        };
    };

    /**
     * 设置视图过期
     *
     * @private
     */
    ENGINE_CLASS.$invalidateView = function () {
        var me = this;

        /**
         * resize处理器
         *
         * @private
         */
        if (!this._hResizeHandler) {
            this._hResizeHandler = setTimeout(
                function () {
                    // resize
                    var eventChannel = me._diFactory.getEventChannel();
                    if (eventChannel) {
                        eventChannel.triggerEvent('resize');
                    }

                    me._hResizeHandler = null;
                },
                0
            )
        }
    };

    /**
     * 析构
     *
     * 静态的DICT回归默认状态
     * globalModel实例释放掉
     * diFactory实例释放掉
     * @public
     */
    ENGINE_CLASS.dispose = function () {
        var diFactory = this._diFactory;

        diFactory.forEachEntity(
            [ 'COMPONENT' ],
            function (def, ins, id) {
                ins.dispose();
            }
        );
        DICT.reset();
        diFactory.dispose();
    };

    /**
     * 融合clzDef
     *
     * @private
     */
    function mergeClzDef(depict) {
        var clzDefs = depict.clzDefs || [];
        var clzDefMap = {};
        for (var i = 0, clzDef; clzDef = clzDefs[i]; i ++) {
            clzDefMap[clzDef.clzKey] = clzDef;
        }
        merge(DICT.CLZ, clzDefMap);
    }

    /**
     * @private
     */
    function findEntityDef(depict, id) {
        var entityDefs;

        if (depict && (entityDefs = depict.entityDefs)) {
            for (var i = 0, o; i < entityDefs.length; i ++) {
                if ((o = entityDefs[i]) && o.id == id) {
                    return o;
                }
            }
        }
    };
})();

