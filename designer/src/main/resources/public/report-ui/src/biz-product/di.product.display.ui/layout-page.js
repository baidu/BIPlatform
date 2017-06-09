/**
 * di.product.display.ui.LayoutPage
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    报表展示页面
 * @author:  sushuang(sushuang), lizhantong(lztlovely@126.com)
 * @depend:  xui, xutil
 */

$namespace('di.product.display.ui');

(function () {

    var inheritsObject = xutil.object.inheritsObject;
    var XVIEW = xui.XView;
    var SNIPPET_PARSER = di.helper.SnippetParser;
    var bind = xutil.fn.bind;
    var DICT = di.config.Dict;
    var ajaxRequest = baidu.ajax.request;
    var jsonParse = baidu.json.parse;
    var DI_FACTORY;
    var GLOBAL_MODEL;
    var URL;
    var COMMON_PARAM_FACTORY;
    var DIALOG;
    var Engine;
    var engine;
    var diFactory;
    $link(function () {
        URL = di.config.URL;
        GLOBAL_MODEL = di.shared.model.GlobalModel;
        DI_FACTORY = di.shared.model.DIFactory;
        COMMON_PARAM_FACTORY = di.shared.model.CommonParamFactory;
        DIALOG = di.helper.Dialog;
        Engine = di.shared.model.Engine;
    });

    /**
     * 报表展示页面
     *
     * @class
     * @extends xui.XView
     */
    var LAYOUT_PAGE = $namespace().LayoutPage =
        inheritsObject(XVIEW, constructor);
    var LAYOUT_PAGE_CLASS = LAYOUT_PAGE.prototype;

    /**
     * FIXME
     * 借用ecui的dom ready
     * @override
     */
    XVIEW.$domReady = ecui.dom.ready;

    /**
     * 构造函数
     *
     * @constructor
     * @public
     * @param {Object} options 初始化参数
     * @param {Object} options.externalParam 报表引擎外部传来的参数，
     *      浏览器端只回传，不识别
     */
    function constructor(options) {
        var me = this;

        options = options || {};
        options.extraOpt = options.extraOpt || {};
        engine = new Engine(options);
        diFactory = engine.getDIFactory();

        /**
         * agent标志，表示是由stub加载的还是直接url加载的
         * 值可为'STUB'或空（默认）
         */
        me._diAgent = options.diAgent;
        diFactory.setDIAgent(me._diAgent);

        /**
         * 预存报表镜像id,每次报表刷新时，会向后台提交镜像id
         * 报表初始化时，又需要这个id，就在这预存一份
         * 使用时:var diFactory = DI_FACTORY();
         * var currentImgId = diFactory.getDIReportImageId();
         */
        me._reportImageId = options.externalParam.reportImageId;
        diFactory.setDIReportImageId(me._reportImageId);

        /**
         * di-stub加载的情况下，prodStart开始的条件
         */
        me._prodStartCond = {};

        /**
         * 是否已经初始化 prodInitialized
         */
        me._prodInitialized = false;


        // 初始化全局模型
        GLOBAL_MODEL(options);

        var eventChannel = SNIPPET_PARSER().setupEventChannel(
            document, options, diFactory
        );

        // 对外事件通道注册
        if (eventChannel) {
            eventChannel.addEventListener('resize', bind(me.resize, me));
            eventChannel.addEventListener(
                'prodStart',
                function () {
                    me._prodStartCond.prodStartEvent = true;
                    me.$prodStart();
                }
            );
        }

        // 设置功能权限
        diFactory.setFuncAuth(options.funcAuth);

        // 请求depict
        me.$prepareDepict(options)
    };

    /**
     * 生产环境开始
     *
     * @private
     */
    LAYOUT_PAGE_CLASS.$prodStart = function () {
        var prodStartCond = this._prodStartCond;
        if (prodStartCond.prodInitFunc
            && (
                // 如果没有di-stub，则直接开始prodInit
                (this._diAgent != 'STUB')
                // 如果是由di-stub加载的，则依照di-stub的prodStart事件来触发开始
                // 否则不能保证保证prodInit在di-stub的iframe的onload事件完后才开始执行
                // （尤其在ie下，即便在ajax回调中，也不能保证顺序，谁快谁先）
                || prodStartCond.prodStartEvent
                )
            ) {
            prodStartCond.prodInitFunc();
        }
    };

    /**
     * 生产环境初始化
     *
     * @private
     */
    LAYOUT_PAGE_CLASS.$prodInit = function (depict) {
        if (this._prodInitialized) {
            return;
        }
        this._prodInitialized = true;
        engine.start(depict);
    };

    /**
     * @override
     */
    LAYOUT_PAGE_CLASS.dispose = function () {
        LAYOUT_PAGE.superClass.$dispose.call(this);
    };

    /**
     * 获得depict的内容
     *
     * @public
     */
    LAYOUT_PAGE_CLASS.$prepareDepict = function (options) {
        var me = this;
        var remoteDepictRef = getRemoteDepictRef(options);
        var got = [];

        for (var i = 0; i < remoteDepictRef.length; i ++) {
            if (!remoteDepictRef[i]) {
                alert('depictRef 定义错误: ' + remoteDepictRef);
                break;
            }

            // 请求depict
//            var url = (
//                options.mold
//                    ? [
//                        options.webRoot,
//                        DICT.MOLD_PATH,
//                        remoteDepictRef[i]
//                    ]
//                    : [
//                        options.webRoot,
//                        DICT.VTPL_ROOT,
//                        options.bizKey,
//                            options.phase || 'release',
//                        remoteDepictRef[i]
//                    ]
//                ).join('/') + '?__v__=' + options.repoVersion;
           //FIXME:下面的路径实现方式不好
           var url = [
                    options.webRoot,
                    DICT.REPORTS,
                    options.reportId,
                    DICT.REPORT_JSON
           ].join('/');

            ajaxRequest(
                url,
                {
                    method: 'GET',
                    onsuccess: onsuccess,
                    onfailure: bind(onfailure, null, url)
                }
            );
        }

        function onsuccess(xhr, rspText) {
            var rspObj = jsonParse(rspText);
            got.push(rspObj);

            // depicts已经全部获取时
            if (got.length >= remoteDepictRef.length) {

                me._prodStartCond.prodInitFunc = bind(
                    me.$prodInit,
                    me,
                    engine.mergeDepict(got)
                );

                me.$prodStart();
            }
        }

        function onfailure(url, xhr, rspText) {
            // 因为在实际项目中，该报警无实际意义，所以暂时注释掉该失败提示
            // alert(
            //         '获取depict失败：url=' + url
            //         + ' status=' + xhr.status
            // );
        }

        function getRemoteDepictRef() {
            // 先extraOpt，后默认reportTemplateId
            var depictRef = (options.extraOpt.depictRef || []).slice();
            depictRef.push(options.persistentReportTemplateId + '.json');
            return depictRef;
        };

    };

    /**
     * 窗口改变重新计算大小
     * resize不能再触发rendered
     *
     * @public
     */
    LAYOUT_PAGE_CLASS.resize = function () {
        diFactory.forEachEntity(
            'COMPONENT',
            function (def, ins, id) {
                ins.resize && ins.resize();
            }
        );
    };

})();

