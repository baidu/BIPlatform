/**
 * di.shared.ui.GeneralVContainer
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    VCONTAINER
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
    var assign = xutil.object.assign;
    var bind = xutil.fn.bind;
    var objKey = xutil.object.objKey;
    var XVIEW = xui.XView;
        
    //------------------------------------------
    // 类型声明 
    //------------------------------------------

    /**
     * VCONTAINER
     * 
     * @class
     * @extends xui.XView
     */
    var GENERAL_VCONTAINER = $namespace().GeneralVContainer = 
            inheritsObject(XVIEW, constructor);
    var GENERAL_VCONTAINER_CLASS = GENERAL_VCONTAINER.prototype;
    
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
        this._oOptions = assign({}, options);
    };
    
    /**
     * @override
     */
    GENERAL_VCONTAINER_CLASS.dispose = function() {
        this.$di('disposeMainEl');
    };

})();