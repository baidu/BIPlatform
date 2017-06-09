/**
 * di.shared.model.GlobalModel
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * desc:    [通用模型] 全局数据模型
 * author:  sushuang(sushuang)
 */

$namespace('di.shared.model');

/**
 * @usage 单例，直接如此获取单例即可：var g = di.shared.GlobalModel();
 */
(function() {
    
    /* 外部引用 */
    var inherits = xutil.object.inherits;
    var USER_MODEL;
    var AUTH_MODEL;
    var DATE_MODEL;
    var GLOBAL_MENU_MANAGER;
    var XDATASOURCE = xui.XDatasource;
    
    $link(function() {
        var sharedNS = di.shared;
        USER_MODEL = sharedNS.model.UserModel;
        AUTH_MODEL = sharedNS.model.AuthModel;
        DATE_MODEL = sharedNS.model.DateModel;
        GLOBAL_MENU_MANAGER = sharedNS.model.GlobalMenuManager;
    });
    
    /* 类型声明 */
    var GLOBAL_MODEL = $namespace().GlobalModel = function(options) {
            if (instance && options) {
                throw new Error('global model has been created');
            }
            if (!instance && !options) {
                throw new Error('global model creation needs options');
            }

            if (!instance) {
                (instance = new SINGLETON(options))
            }
            return instance;
        };
    var GLOBAL_MODEL_CLASS = inherits(GLOBAL_MODEL, XDATASOURCE);
        
    function SINGLETON(options) {
        XDATASOURCE.client.call(this);
        
        this._sBizKey = options.bizKey;

        // 初始化全局模型
        this._mUserModel = new USER_MODEL();
        this._mAuthModel = new AUTH_MODEL();
        this._mDateModel = new DATE_MODEL();
        this._mDateModel.setData(options);

        this._sGlobalType = options.globalType;
        if (this._sGlobalType == 'CONSOLE') {
            this._mGlobalMenuManager = new GLOBAL_MENU_MANAGER(options)
        }
    };
    
    var instance;

    /**
     * 获得DateModel
     * @public
     */
    GLOBAL_MODEL_CLASS.getDateModel = function() {
        return this._mDateModel;
    };
    
    /**
     * 获得UserModel
     * @public
     */
    GLOBAL_MODEL_CLASS.getUserModel = function() {
        return this._mUserModel;
    };
    
    /**
     * 获得AuthModel
     * @public
     */
    GLOBAL_MODEL_CLASS.getAuthModel = function() {
        return this._mAuthModel;
    };
    
    /**
     * 获得GlobalMenuManager
     * @public
     */
    GLOBAL_MODEL_CLASS.getGlobalMenuManager = function() {
        return this._mGlobalMenuManager;
    };

    /**
     * 获得bizkey（目前的逻辑，全局唯一）
     * @public
     */
    GLOBAL_MODEL_CLASS.getBizKey = function() {
        return this._sBizKey;
    };

    inherits(SINGLETON, GLOBAL_MODEL);
    
})();

