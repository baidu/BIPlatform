/**
 * di.shared.ui.GeneralSnippet
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    DI 片段
 * @author:  sushuang(sushuang)
 * @depend:  xui, xutil
 */

$namespace('di.shared.ui');

(function() {
    
    //------------------------------------------
    // 引用 
    //------------------------------------------

    var UTIL = di.helper.Util;
    var inheritsObject = xutil.object.inheritsObject;
    var q = xutil.dom.q;
    var bind = xutil.fn.bind;
    var objKey = xutil.object.objKey;
    var INTERACT_ENTITY = di.shared.ui.InteractEntity;
        
    //------------------------------------------
    // 类型声明 
    //------------------------------------------

    /**
     * DI 片段
     * 
     * @class
     * @extends xui.XView
     */
    var SNIPPET = $namespace().GeneralSnippet = 
            inheritsObject(INTERACT_ENTITY, constructor);
    var SNIPPET_CLASS = SNIPPET.prototype;
    
    //------------------------------------------
    // 方法
    //------------------------------------------

    /**
     * 创建Model
     *
     * @constructor
     * @private
     * @param {Object} options 参数
     */
    function constructor(options) {
        // ...
    };
    
    /**
     * @override
     */
    SNIPPET_CLASS.dispose = function() {
    };

})();