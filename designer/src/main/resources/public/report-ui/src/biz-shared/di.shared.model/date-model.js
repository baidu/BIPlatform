/**
 * di.shared.model.DateModel
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * desc:    [通用模型] 时间数据模型
 * author:  sushuang(sushuang)
 */

$namespace('di.shared.model');

(function() {
    
    /* 外部引用 */
    var inheritsObject = xutil.object.inheritsObject; 
    var XDATASOURCE = xui.XDatasource;
        
    /* 类型声明 */
    var DATE_MODEL = $namespace().DateModel = inheritsObject(XDATASOURCE);
    var DATE_MODEL_CLASS = DATE_MODEL.prototype;
        
    /**
     * 初始化当前值
     * @override
     */
    DATE_MODEL_CLASS.setData = function(data) {
        this.businessData = true;
        this._nInitServerTime = parseInt(data.serverTime) || new Date().getTime();
        this._nServerTimeOffset = this._nInitServerTime - (new Date).getTime();
    };
    
    /**
     * 获得服务器的当前时间
     * 不保证准确的地方：
     * 1. 网路延迟没有考虑
     * 2. 如果用户在打开了网页后修改了客户端的系统时间，则此值会错误
     * @public
     * 
     * @return {Date} 当前时间
     */
    DATE_MODEL_CLASS.now = function() {
        var date = new Date();
        date.setTime(date.getTime() + this._nServerTimeOffset);
        return date;
    };
    
})();

