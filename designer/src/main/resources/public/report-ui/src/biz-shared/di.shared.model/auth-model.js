/**
 * di.shared.model.AuthModel
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * desc:    [通用模型] 权限数据模型
 * author:  sushuang(sushuang)
 */

$namespace('di.shared.model');

(function () {
    
    /* 外部引用 */
    var inheritsObject = xutil.object.inheritsObject;
    var XDATASOURCE = xui.XDatasource;
        
    /* 类型声明 */
    var AUTH_MODEL = $namespace().AuthModel = inheritsObject(XDATASOURCE);
    var AUTH_MODEL_CLASS = AUTH_MODEL.prototype;
        
    /**
     * 获得用户Id
     * @public
     * 
     * @return {string} 用户id
     */
    AUTH_MODEL_CLASS.getUserId = function () {
        // TODO
    };    
    
})();

