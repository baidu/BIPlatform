/**
 * di.helper.SnippetParser
 * Copyright 2012 Baidu Inc. All rights reserved.
 * 
 * @file:    提供html片段的解析
 * @author:  sushuang(sushuang)
 * @depend:  xutil
 */

$namespace('di.helper');
 
(function () {
    
    //--------------------------------
    // 引用
    //--------------------------------

    var setByPath = xutil.object.setByPath;
    var getByPath = xutil.object.getByPath;
    var getParent = xutil.dom.getParent;
    var merge = xutil.object.merge;
    var DICT;
    var DIALOG;

    $link(function () {
        DICT = di.config.Dict;
        DIALOG = di.helper.Dialog;
    });

    /**
     * html片段解析器
     *
     * @usage
     *      单例，
     *      这样得到实例：var unitFactory = di.helper.SnippetParser();
     */
    $namespace().SnippetParser = function () {
        return instance = instance || {
            parseProdSnippet: parseProdSnippet,
            setupEventChannel: setupEventChannel
        };
    };

    var instance;

    var DEFAULT_DOM_ATTR_NAME = 'data-o_o-di';
    var STUB_EVENT_CHANNEL_ANCHOR = 'BODY';
    var STUB_EVENT_CHANNEL_OUTWARD = 'data-d-outward-d-atad';
    var STUB_EVENT_CHANNEL_INWARD = 'data-d-inward-d-atad';
    var ID_DELIMITER = '.';

    function setupEventChannel(el, prodDef, diFactory) {
        var els = getAllEls(el);
        var domAttrName = prodDef.domAttrName || DEFAULT_DOM_ATTR_NAME;

        // 便利dom节点
        for (var i = 0, eo, attr; eo = els[i]; i ++) {
            // 事件通道
            attr = eo.getAttribute(domAttrName);
            if (attr == STUB_EVENT_CHANNEL_ANCHOR) {
                return createStubEventChannel(eo, diFactory);
            }
        }                
    }

    /**
     * 解析生产环境的snippet
     * 
     * @public
     * @param {HTMLElement} el html片段的根节点
     * @param {Object} depict 定义描述
     * @param {Object} prodDef 生成环境定义
     * @param {Object} diFactory 工厂
     */
    function parseProdSnippet(el, depict, prodDef, diFactory) {    
        prodDef = prodDef || {};
        var domAttrName = prodDef.domAttrName || DEFAULT_DOM_ATTR_NAME;

        var els = getAllEls(el);
        var def;
        var attr;
        var ins;
        var clz;
        var clzType;
        var clzKey;
        var i;
        var j;
        var eo;

        // 做entityDef集合
        var entityDefs = depict.entityDefs || [];
        var entityDefMap = {};
        for (i = 0; i < entityDefs.length; i ++) {
            def = entityDefs[i];
            entityDefMap[def.id] = def;
        }

        // 遍历dom节点
        for (i = 0; eo = els[i]; i ++) {
            // 事件通道
            attr = eo.getAttribute(domAttrName);
            if (attr == STUB_EVENT_CHANNEL_ANCHOR) {
                // createStubEventChannel(eo, diFactory);
                continue;
            }

            // 处理实例声明的节点
            if (attr) {
                def = entityDefMap[attr];
                // TODO remove this
                if (!def) {
                    console.log();
                }
                checkId(def.id);
                
                // 注册进diFactory
                def.el = eo;
                diFactory.addEntity(def, 'DEF');
            }
        }

        // 根据id，为component寻找到逻辑隶属snippet，
        // 添加reportTemplateId的引用
        // 根据包含关系，为component寻找视图隶属snippet。
        diFactory.forEachEntity(
            ['COMPONENT'], 
            function (def, ins, id) {
                // 设置逻辑隶属snippet
                var idArr = def.id.split(ID_DELIMITER);
                var snptDef = diFactory.getEntity(idArr[0], 'DEF');
                if (!snptDef) {
                    throw new Error(def.id + ' 未定义隶属的snippet');
                }
                setByPath('belong.snippet', snptDef.id, def);

                // 向外循环，设置视图隶属snippet
                var el = def.el;
                var parentDef;
                var besnpt = getByPath('layout.parentSnippet', def);
                if (!besnpt) {
                    setByPath('layout.parentSnippet', besnpt = [], def);
                }
                while ((el = getParent(el)) && el != document) {
                    parentDef = diFactory.getEntity(
                        el.getAttribute(domAttrName), 
                        'DEF'
                    );

                    if (parentDef && parentDef.clzType == 'SNIPPET') {
                        besnpt.push(parentDef.id);
                    }
                }
            }
        );

        // 根据dom包含关系，为vpart添加其内部实体的引用
        // FIXME
        // 如果后面要vpart中能嵌套snippet，则不能如下简单处理，须考虑层级。
        diFactory.forEachEntity(
            ['VPART'],
            function(def, ins, id) {
                var subEls = getAllEls(def.el);
                var index = { COMPONENT: 0, VUI: 0 };
                var refName = { COMPONENT: 'componentRef', VUI: 'vuiRef' };

                for (var i = 0, eo, subDef, clzType; eo = subEls[i]; i ++) {
                    subDef = diFactory.getEntity(
                        eo.getAttribute(domAttrName), 
                        'DEF'
                    );

                    if (!subDef) { continue; }

                    refName[clzType = subDef.clzType] && setByPath(
                        refName[clzType] + '.inner[' + (index[clzType] ++) + ']',
                        subDef.id, 
                        def
                    );
                }
            }
        );

        // 记录根snippet
        diFactory.rootSnippet(depict.rootSnippet); 
    }

    /**
     * 生成对外事件通道
     * 
     * @private
     */
    function createStubEventChannel(el, diFactory) {  

        // outward (报表发事件，di-stub收事件)
        var triggerEvent = function(eventName, args) {
            var handler = el[STUB_EVENT_CHANNEL_OUTWARD];
            if (handler) {
                try {
                    handler(eventName, args);
                }
                catch (e) {
                    // TODO
                }
            }
        };
        
        // inward (di-stub发事件，报表收事件)
        el[STUB_EVENT_CHANNEL_INWARD] = function(eventName, args) {
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
        };

        var listenerMap = {};

        var addEventListener = function(eventName, listener) {
            var hList = listenerMap[eventName];
            if (!hList) {
                hList = listenerMap[eventName] = [];
            }
            hList.push(listener);          
        }

        var eventChannel;
        diFactory.setEventChannel(
            eventChannel = {
                anchorEl: el,
                triggerEvent: triggerEvent,
                addEventListener: addEventListener
            }
        );

        return eventChannel;
    }

    /**
     * 检查id，非法则抛出异常
     * 目前只允许使用 1-9a-zA-Z、中划线、下划线
     *
     * @private
     * @param {string} id
     */
    function checkId(id) {
        if (!/[1-9a-zA-Z\-_\.]/.test(id)) {
            throw new Error('id is illegal: ' + id);
        }
    }

    /**
     * 得到所有子el
     * 
     * @private
     * @param {HTMLElement} el 根el
     * @return {Array} 所有子el
     */
    function getAllEls(el) {
        return el.all || el.getElementsByTagName('*');
    }

})();