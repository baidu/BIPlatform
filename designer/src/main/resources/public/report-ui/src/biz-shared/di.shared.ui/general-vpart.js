/**
 * di.shared.ui.GeneralVPart
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
    var GENERAL_VPART = $namespace().GeneralVPart = 
            inheritsObject(XVIEW, constructor);
    var GENERAL_VPART_CLASS = GENERAL_VPART.prototype;
    
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
    GENERAL_VPART_CLASS.dispose = function() {
        this.$di('disposeMainEl');
    };

})();