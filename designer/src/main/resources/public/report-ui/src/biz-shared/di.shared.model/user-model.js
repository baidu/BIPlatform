/**
 * di.shared.model.UserModel
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    [通用模型] 用户数据模型
 * @author:  sushuang(sushuang)
 */

$namespace('di.shared.model');

(function () {
    
    /* 外部引用 */
    var inheritsObject = xutil.object.inheritsObject;
    var XDATASOURCE = xui.XDatasource;
        
    /* 类型声明 */
    var USER_MODEL = $namespace().UserModel = inheritsObject(XDATASOURCE);
    var USER_MODEL_CLASS = USER_MODEL.prototype;
        
    /**
     * 获得用户Id
     * @public
     * 
     * @return {string} 用户id
     */
    USER_MODEL_CLASS.getUserId = function () {
        // TODO
    };    
    
})();

