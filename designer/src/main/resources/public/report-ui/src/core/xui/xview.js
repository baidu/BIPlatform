/**
 * xui.XView
 * Copyright 2012 Baidu Inc. All rights reserved.
 * 
 * @file:    视图基类
 * @author:  sushuang(sushuang)
 * @depend:  xutil
 * @usage:   
 *          (1) 须实现xui.XView.domReady函数
 *          (2) 页面中使用：
 *              <script type="text/javascript">
 *                  xui.XView.start("aaa.bbb.ccc.SomePageView");
 *              </script>
 *              则启动了SomePageView类
 */

(function () {
    
    var XOBJECT = xui.XObject;
    var getByPath = xutil.object.getByPath;
    var inheritsObject = xutil.object.inheritsObject;
    
    /**
     * 视图基类
     *
     * @class
     */
    var XVIEW = xui.XView = inheritsObject(
        XOBJECT, 
        function (options) {
            this._el = options.el;
        }
    );
    var XVIEW_CLASS = XVIEW.prototype;

    /** 
     * 得到主DOM元素
     *
     * @public
     */
    XVIEW_CLASS.getEl = function() {
        return this._el;
    };

    /** 
     * 设置主DOM元素
     *
     * @public
     */
    XVIEW_CLASS.setEl = function(el) {
        this._el = el;
    };

    /** 
     * 析构
     *
     * @public
     */
    XVIEW_CLASS.dispose = function() {
        this._el = null;
    };

    /**
     * 页面开始
     * 
     * @public
     * @static
     * @param {string} viewPath 页面对象的路径
     * @param {Object} options 参数 
     * @return {ecui.ui.Control} 创建的页面对象
     */    
    XVIEW.start = function (viewPath, options) {
        var viewClass;
        
        XVIEW.$domReady(
            function () {
                // 前中后三级控制 - 前
                XVIEW.$preStart && XVIEW.$preStart(viewPath, options);

                // 前中后三级控制 - 中
                // 控制端的页面初始化的控制器 "di.console.frame.ui.MainPage"
                // spa中的初始化也用到此逻辑
                viewPath && (viewClass = getByPath(viewPath));
                viewClass && (new viewClass(options)).init();

                // 前中后三级控制 - 后
                // 系统预制结束，具体的业务数据开始加载并生成dom并绑定事件
                XVIEW.$postStart && XVIEW.$postStart(viewPath, options);
            }
        );
    };

    /**
     * 初始前的预处理
     * 
     * @private
     * @abstract
     * @static
     * @param {string} viewPath 页面对象的路径
     * @param {Object} options 参数 
     */
    XVIEW.$preStart = function (viewPath, options) {};

    /**
     * 初始后的处理
     * 
     * @private
     * @abstract
     * @static
     * @param {string} viewPath 页面对象的路径
     * @param {Object} options 参数 
     */
    XVIEW.$postStart = function (viewPath, options) {};

    /**
     * DOM READY函数，由工程自己定义
     * 
     * @private
     * @abstract
     * @static
     * @param {Function} callback
     */
    XVIEW.$domReady = null;

})();
