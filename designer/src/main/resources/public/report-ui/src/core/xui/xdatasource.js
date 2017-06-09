/**
 * xui.XDatasource
 * Copyright 2012 Baidu Inc. All rights reserved.
 * 
 * @file:   数据模型基类
 *
 *          使用模型（Model）和视图（View）分离的程序结构时，
 *          此类可作为模型的基类被继承及扩展，定义相应属性
 *          （@see OPTIONS_NAME），
 *          其各派生类提供前/后台的业务数据获取和管理。
 *          XDatasource推荐一定的代码结构规范，见如下@usage。
 *
 *          基础功能：
 *              (1) 向后台发送数据（用Ajax）
 *              (2) 获得数据：
 *                      主动注入数据
 *                          （出现在数据从其他代码中取得的情况，
 *                          如数据模型的依赖）
 *                      从前台取数据
 *                          （例如为了节省链接和加快速度，
 *                          JSON数据放在页面HTML中一块返回前端，
 *                          或者从本地存储中得到等）
 *                      从后台取数据
 *                          （用Ajax）
 *                  取数据顺序是：
 *                      首先看是否已有主动注入的"businessData"；
 *                      否则如果"local"定义了则从"local"中取；
 *                      否则如果"url"定义了则发Ajax请求从后台取。
 *              (3) Oberver模式的更新通知，及自定义事件
 *              (4) 多数据源的管理（参见datasourceId）
 *              (5) 推荐的请求生命期结构
 *                  （参数准备、返回值解析、结果响应、最终清理等）
 *              (6) 析构时，abort所有未完成的请求，
 *                  防止请求回来后视图、模型已经不存在导致js错误、
 *                  全局视图未清理等问题
 *
 * @author:  sushuang(sushuang)
 * @depend:  xutil
 * @version: 1.0.1
 */

/**
 *                             -----------------
 *                             |   使用说明    |
 *                             -----------------
 * ____________________________________________________________________________
 * @usage 使用XDatasource
 *        [举例] 
 *          ___________________________________________________________________
 *          (1) 定义一个新的XDatasource（如下MyDatasource），用继承的方式:
 * 
 *              如果有数据获取的参数或代码逻辑要写在MyDatasource里面
 *              （如URL，返回解过解析等逻辑），
 *              则在MyDatasource中，定义OPTIONS_NAME中指定的各参数
 *              （不需要定义则缺省即可）。
 *              其中各参数可以定义成string或者Function
 * 
 *              // 定义MyDatasource类
 *              var MyDatasource = function() {}; 
 *              inherits(MyDatasource, XDatasource);
 *              
 *              // 定义url
 *              MyDatasource.prototype.url = '/order/go.action'; 
 *
 *              // 定义param的构造
 *              MyDatasource.prototype.param = function(options) {
 *                  var paramArr = [];
 *                  paramArr.push('name=' + options.args.name);
 *                  paramArr.push('year=' + options.args.year);
 *                  paramArr.push('id=' + this._nId);
 *                  return paramArr.join('&');
 *              }
 *
 *              // 定义返回数据的解析
 *              MyDatasource.prototype.parse = function(data, obj, options) {
 *                  // do something ...
 *                  return data;
 *              }
 * 
 *          ___________________________________________________________________
 *          (2) 使用定义好的MyDatasource
 *              
 *              如果有数据获取的参数或代码逻辑是写在MyDatasource外面
 *              （如sync后改变视图的回调），
 *              则用事件的方式注册到MyDatasource里,
 *
 *              例如：
 *              MyDatasource myDatasource = new MyDatasource();
 *              绑定事件：
 *              myDatasource.attach(
 *                  'sync.result', 
 *                  function(data, obj, options) {
 *                      // do something ..., 比如视图改变 
 *                  }
 *              );
 *              myDatasource.attach(
 *                  'sync.error', 
 *                  function(status, obj, options) {
 *                      // do something ..., 比如页面提示 
 *                  }
 *              );
 *              myDatasource.attach(
 *                  'sync.timeout', 
 *                  function(options) { 
 *                      // do something ..., 比如页面提示 
 *                  }
 *              );
 *
 *              往往我们需要给事件处理函数一个scope，
 *              比如可以使用第三方库提供的bind方法。
 *              也可以直接在attach方法中输入scope。
 *
 *              当需要绑定许多事件，可以使用代码更短小的方式绑定事件。
 *              （参见xui.XObject的attach方法）
 *
 *              例如：
 *              （下例中，this是要赋给事件处理函数的scope）
 *              var bind = xutil.fn.bind;
 *              myDatasource.attach(
 *                  {
 *                      'sync.preprocess.TABLE_DATA': bind(this.disable, this),
 *                      'sync.result.TABLE_DATA': bind(this.$handleListLoaded, this),
 *                      'sync.finalize.TABLE_DATA': [  
 *                          // 一个事件多个处理函数的情况
 *                          bind(this.enable, this),
 *                          bind(this.$resetDeleteBtnView, this)
 *                      ],
 *                      'sync.result.DELETE': bind(this.$handleDeleteSuccess, this)
 *                  }
 *              ); 
 *
 *              又例如，还可以这样写：
 *              （数组第一个元素是事件名，第二个是事件处理函数，第三个是函数的scope）
 *              myDatasource.attach(
 *                  ['sync.preprocess.TABLE_DATA', this.disable, this],
 *                  ['sync.result.TABLE_DATA', this.$handleListLoaded, this],
 *                  ['sync.finalize.TABLE_DATA', this.enable, this],
 *                  ['sync.finalize.TABLE_DATA', this.$resetDeleteBtnView, this],
 *                  ['sync.result.DELETE': this.$handleDeleteSuccess, this]
 *              );
 *              
 *              需要发送数据或者获取数据时调用myDatasource.sync()，
 *              即可触发相应事件。
 * 
 *              如果要传入外部参数，则在options.args中传入，
 *              例如上例的param和parse定义，sync时直接传入参数：
 *
 *              myDatasource.sync( 
 *                  { 
 *                      args: { name: 'ss', year: 2012 } 
 *                  } 
 *              ); 
 *
 *              这样param和parse函数中即可得到参数'ss', 2012。
 * 
 *              注意，如果sync时指定了datasourceId，比如
 *              myDatasource.sync( { datasourceId:'ds1' } );
 *              则先触发sync.result.ds1事件，再触发sync.result事件。
 *              error、timeout等事件也是此规则。
 * 
 *          ___________________________________________________________________
 *          (3) 如果调用sync时数据是从本地取得，
 *              比如页面初始化时把JSON数据写在了页面的某个dom节点中，
 *              则设置"local"参数，
 * 
 *              例如：
 *              MyDatasource.prototype.local = function() {
 *                   var data;
 *                   try {
 *                      JSON.parse(
 *                          decodeHTML(
 *                              document.getElementById('DATA').innerHTML
 *                          )
 *                      );
 *                      return this.wrapEJson(data);
 *                   } catch (e) {
 *                      return this.wrapEJson(null, 99999, 'business error');
 *                   }
 *              };
 *
 *              从而sync时会调用此local函数取得数据，
 *              如果success则会走parse和result过程。 (@see OPTIONS_NAME.local)
 *          
 *          ___________________________________________________________________
 *          (4) 如果调用sync时数据已经OK不需要解析处理等，
 *              则直接对businessData进行设置。
 * 
 *              例如：
 *              myDatasource.businessData = someData;
 *              从而sync时直接取someData了，走result过程了。
 *              (@see OPTIONS_NAME.businessData)
 * 
 *          ___________________________________________________________________
 *          (5) 如果一个XDatasource中要包含多个数据源，
 *              可以把url、result等属性(@see OPTIONS_NAME)定义成XDatasource.Set，
 *              在sync时使用datasourceId指定当前sync时使用哪个数据源。
 *
 *              例如：
 *              MyDatasource.prototype.url = new xui.XDatasource.Set();
 *              MyDatasource.prototype.url['ORDER'] = 'order.action';
 *              MyDatasource.prototype.url['ADD'] = 'add.action';
 *
 *              // 这样初始化也可以
 *              MyDatasource.prototype.result = new xui.XDatasource.Set(
 *                  {
 *                      'ORDER': function() { ... }
 *                      'ADD': function() { ... }
 *                  }
 *              );
 *
 *              MyDatasource.prototype.param = function() { // func_all... };
 *              MyDatasource.prototype.param['ORDER'] = 
 *                  function() { // func_order... };
 *
 *              则：myDatasource.sync( { datasourceId: 'ORDER' } ); 
 *              或者简写为：
 *                  myDatasource.sync('ORDER'); 
 *              表示取order.action，并走相应的result（func_order）。
 *
 *              另外，上例没有找到相应的param['ORDER']，
 *              但param本身定义成了函数，则走本身（func_all）。
 * 
 * ____________________________________________________________________________
 * @usage 绑定多个XDatasource
 *              如果多个XDatasource共用一个请求，可绑定在一起处理，
 *
 *              例如：
 *              CombinedXDatasource c = new CombinedXDatasource();
 *              c.addSyncCombine(datasource1);
 *              c.addSyncCombine(datasource2, 'DATASOURCE_LIST');
 *
 *              从而：
 *              使用c.sync()时，datasource1也会被触发parse事件
 *              以及sync.result/sync.error/sycn.timeout事件
 *              使用c.sync( { datasourceId: 'DATASOURCE_LIST' } )时，
 *              datasource1、datasource2都会被触发parse事件
 *              以及sync.result/sync.error/sycn.timeout事件
 * 
 * ____________________________________________________________________________
 * @usage 工程中重写/扩展XDatasource的实现类
 *              （一般在工程中用于指定静态的url，也可在需要时用于重写方法）
 *              直接调用
 *              XDatasource.extend(
 *                  MyXDatasource, 
 *                  { url: ..., method: ... }
 *              );
 *              进行扩展。
 */

(function () {
    
    //--------------------------
    // 引用
    //--------------------------

    var XOBJECT = xui.XObject;
    var xajax = xutil.ajax;
    var xlang = xutil.lang;
    var xobject = xutil.object;
    var utilUrl = xutil.url;
    var utilString = xutil.string;
    var inheritsObject = xobject.inheritsObject;
    var extend = xobject.extend;
    var clone = xobject.clone;
    var isFunction = xlang.isFunction;
    var isArray = xlang.isArray;
    var isString = xlang.isString;
    var isObject = xlang.isObject;
    var hasValue = xlang.hasValue;
    var sliceArray = Array.prototype.slice;
    
    //--------------------------
    // 类型定义
    //--------------------------

    /**
     * Model基类
     * 
     * @class
     * @extends xui.XObject
     */
    var XDATASOURCE = xui.XDatasource = 
            inheritsObject(XOBJECT, xdatasourceConstructor);
    var XDATASOURCE_CLASS = XDATASOURCE.prototype;

    /**
     * 构造函数
     *
     * @public
     * @constructor
     * @param {Object} options
     */
    function xdatasourceConstructor(options) {
        /**
         * 事件处理器集合
         *
         * @type {Object}
         * @private
         */
        this._oEventHandlerMap = {};
        /**
         * 绑定集合，key是datasourceId
         *
         * @type {Object}
         * @private
         */
        this._oSyncCombineSet = {};
        /**
         * 无datasourceId时默认的绑定集合
         *
         * @type {Array.<xui.XDatasource>}
         * @private
         */
        this._aSyncCombineSetDefault = [];
        /**
         * 当前未完成的request集合，key为requestId
         *
         * @type {Object}
         * @private
         */
        this._oRequestSet = {};
        /**
         * sync过程中的当前datasourceId
         *
         * @type {string}
         * @private
         */
        this._sCurrentDatasourceId;
    }

    /**
     * 一个hash map。表示每个datasourceId对应的配置。
     * 所以使用时须满足的格式：
     * key为datasourceId，
     * value为datasourceId对应的参数/属性。
     * 
     * @class
     * @constructor
     * @param {Object=} set 如果为null，则初始化空Set
     */
    var SET = XDATASOURCE.Set = function (set) {
        set && extend(this, set);
    };
    
    //---------------------------
    // 属性
    //---------------------------

    /**
     * 默认的错误状态值，
     * 用于从success转为error时
     *
     * @type {number} 
     * @protected
     */
    XDATASOURCE_CLASS.DEFAULT_ERROR_STATUS = 999999999999;

    /**
     * XDatasource中可在子类中定义或继承的属性
     * 这些属性不可误指定为其他用
     *
     * @protected
     */
    XDATASOURCE_CLASS.OPTIONS_NAME = [
        /**
         * 调用sync时最初始的预处理，较少使用。
         * 可能使用在：调用sync的地方和注册preprocess的地方不在同一类中的情况
         *
         * @type {(Function|xui.XDatasource.Set)} 
         *          如果为Function：
         *              @param {Object} options 调用sync时传入的配置
         * @protected
         */
        'preprocess',

        /**
         * 主动注入的业务数据（主要意义是标志业务数据是否已经OK）,
         * 如果此属性有值表示数据已经OK，sync时不会再试图获取数据。
         *
         * @type {(Function|Any|xui.XDatasource.Set)} 
         *          如果为Function：
         *              @param {Object} options 调用sync时传入的配置
         *              @return {Any} businessData  
         * @protected
         */
        'businessData', 
        
        /**
         * 从本地取得数据
         * 例如可以数据挂在HTML中返回：
         * <div style="display:none" id="xxx"> ...some data... </div>
         * 
         * @type {(Function|Object|xui.XDatasource.Set)}
         *          如果为Function, 参数为：
         *             param {Object} options 调用sync时传入的配置
         *             return {Object} e-json规范的返回值，
         *                  可用wrapEJson方法包装得到
         *          如果为Object，则是e-json对象
         * @protected
         */
        'local',
        
        /**
         * 请求后台的url
         *
         * @type {(Function|string|xui.XDatasource.Set)} 
         *          如果为Function, 参数为：
         *             param {Object} options 调用sync时传入的配置
         *             return {string} url  
         * @protected
         */
        'url', 
        
        /**
         * 请求的HTTP方法（'POST'或'GET'），默认是POST
         *
         * @type {(Function|string|xui.XDatasource.Set)}
         *          如果为Function, 参数为：
         *             param {Object} options 调用sync时传入的配置
         *             return {string} 方法
         * @protected
         */
        'method', 
        
        /**
         * 用于阻止请求并发，同一businessKey的请求不能并发 (@see xajax)
         *
         * @type {(Function|string|xui.XDatasource.Set)} 
         *          如果为Function, 参数为：
         *             param {Object} options 调用sync时传入的配置
         *             return {string} 方法
         * @protected
         */
        'businessKey', 
        
        /**
         * 得到请求的参数字符串
         *
         * @type {(Function|string|xui.XDatasource.Set)} 
         *          如果为Function, 参数为：
         *             param {Object} options 调用sync时传入的配置
         *             return {string} 请求参数字符串   
         * @protected
         */
        'param',
        
        /**
         * 处理请求成功的结果数据
         * 
         * @type {(Function|Any|xui.XDatasource.Set)}
         *          如果为Function, 参数为：
         *             param {(Object|string)} data 获取到的业务数据
         *             param {(Object|string)} ejsonObj 后台返回全结果，一般不使用
         *             param {Object} options 调用sync时传入的配置
         *             return {Any} data 结果数据
         * @protected
         */
        'parse',
        
        /**
         * 获得数据结果
         *
         * @type {(Function|xui.XDatasource.Set)}
         *          如果为Function, 参数为：
         *             param {(Object|string)} data parse过的业务数据
         *             param {(Object|string)} ejsonObj 后台返回全结果，一般不使用
         *             param {Object} options 调用sync时传入的配置
         * @protected
         */
        'result',
        
        /**
         * 处理请求失败的结果
         *
         * @type {(Function|xui.XDatasource.Set)} 
         *          如果为Function, 参数为：
         *             param {(Object|string)} status 后台返回错误状态
         *             param {(Object|string)} ejsonObj 后台返回全结果，一般不使用
         *             param {Object} options 调用sync时传入的配置
         * @protected
         */
        'error',
        
        /**
         * 处理请求超时的结果
         * 
         * @type {(Function|xui.XDatasource.Set)} 
         *          如果为Function, 参数为：
         *             param {Object} options 调用sync时传入的配置
         * @protected
         */
        'timeout',

        /**
         * 请求返回时总归会触发的回调，先于result或error触发
         *
         * @type {(Function|xui.XDatasource.Set)} 
         *          如果为Function, 参数为：
         *             param {Object} options 调用sync时传入的配置
         * @protected
         */
        'complete',
        
        /**
         * 请求返回时总归会触发的回调，常用于最后的清理
         *
         * @type {(Function|xui.XDatasource.Set)} 
         *          如果为Function, 参数为：
         *             param {Object} options 调用sync时传入的配置
         * @protected
         */
        'finalize',
        
        /**
         * 定义请求超时的时间(ms)，缺省则不会请求超时
         *
         * @type {(Function|number|xui.XDatasource.Set)} 
         *          如果为Function, 参数为：
         *             param {Object} options 调用sync时传入的配置
         *             return {number} timout的毫秒数
         * @protected
         */
        'timeoutTime',
        
        /**
         * 如果一个XDatasource中包含多个数据源，
         * sync时用此指定当前请求使用那套url、parse、result等
         * 
         * @type {(Function|string|number)} 
         *          如果为Function, 参数为：
         *             param {Object} options 调用sync时传入的配置
         *             return {string} datasourceId
         * @protected
         */
        'datasourceId',

        /**
         * 调用ajax时额外的输入参数
         *
         * @type {(Function|Object|xui.XDatasource.Set)}
         *          如果为Function, 参数为：
         *             param {Object} options 调用sync时传入的配置
         *             return {Object} ajax参数
         * @protected
         */
        'ajaxOptions'
    ];
    
    //-------------------------------------------------------------
    // 方法                                        
    //-------------------------------------------------------------

    /**
     * 功能：
     * (1) 发送数据到后台。
     * (2) 获取数据，可能从前台直接获取，也可能通过Ajax请求后台获取。
     *
     * @public
     * @param {(Object|string)} options 参数
     *                  参数 @see OPTIONS_NAME sync时指定的参数，
     *                  用于重载xdatasource本身的配置
     *                  如果是string，则表示datasourceId
     *                  如果是Object，则属性如下：
     * @param {Object} options.datasourceId 指定数据源id
     * @param {Object} options.args 用户定义的参数
     * @return {string} requestId 如果发生后台请求，返回请求Id，一般不使用
     */
    XDATASOURCE_CLASS.sync = function (options) {
        if (isString(options)) {
            options = { datasourceId: options, args: {} };
        } 
        else {
            options = options || {};
            options.args = options.args || {};
        }

        var datasourceId = getDatasourceId.call(this, options);
        this._sCurrentDatasourceId = datasourceId;

        // 预处理
        handleSyncPreprocess.call(this, datasourceId, options);

        var data;
        var ejsonObj;
        var url;
        var requestId;

        // 已经被注入数据
        if (hasValue(
                data = handleAttr.call(
                    this, datasourceId, 'businessData', options
                )
            )
        ) { 
            handleSyncHasData.call(this, datasourceId, options, data);
        }

        // 从本地获取数据
        else if (
            hasValue(
                ejsonObj = handleAttr.call(
                    this, datasourceId, 'local', options
                )
            )
        ) { 
            handleSyncLocal.call(this, datasourceId, options, ejsonObj);
        }    

        // 从后台获取数据 
        else if (
            hasValue(
                url = handleAttr.call(this, datasourceId, 'url', options)
            )
        ) {
            requestId = handleSyncRemote.call(
                this, datasourceId, options, url
            );
        }

        delete this._sCurrentDatasourceId;

        return requestId;
    };
    
    /**
     * 默认的析构函数
     * 如果有设businessKey，则终止未完成的请求
     *
     * @public
     */
    XDATASOURCE_CLASS.dispose = function () {
        this.abortAll();
        this._oSyncCombineSet = null;
        this._aSyncCombineSetDefault = null;
        XDATASOURCE.superClass.dispose.call(this);
    };
    
    /**
     * 默认的parse函数
     *
     * @protected
     * @param {*} data ejsonObject的data域
     * @param {Object} ejsonObj e-json对象本身
     */
    XDATASOURCE_CLASS.parse = function (data, ejsonObj) { 
        return data; 
    };
    
    /**
     * 默认的datasourceId函数
     *
     * @protected
     * @param {Object} options 调用sync时传入的配置
     * @return {string} datasourceId 数据源Id
     */
    XDATASOURCE_CLASS.datasourceId = function (options) { 
        return void 0; 
    };
    
    /**
     * 主动设值，用于前端已有数据的情况
     * 不传参数则清空
     *
     * @public
     * @param {*} businessData 业务数据
     * @param {string} datsourceId 可指定datasourceId
     */
    XDATASOURCE_CLASS.setBusinessData = function (businessData, datasourceId) {
        this.businessData = businessData || null;
        notifyEvent.call(
            this, datasourceId, 'set.businessdata', {}, [businessData]
        );
    };
    
    /**
     * 得到当前的datasourceId，只在sync过程中可获得值，
     * 等同于在sync的回调中使用options.datasourceId
     *
     * @public
     * @return {string} 当前的datasourceId
     */
    XDATASOURCE_CLASS.getCurrentDatasourceId = function () {
        return this._sCurrentDatasourceId;
    };
    
    /**
     * 终止此Model管理的所有请求
     *
     * @public
     */
    XDATASOURCE_CLASS.abortAll = function () {
        var requestIdSet = clone(this._oRequestSet);
        for (var requestId in requestIdSet) {
            this.abort(requestId);
        }
        this.notify('abortAll', [requestIdSet]);
    };
    
    /**
     * 终止此Model管理的某请求
     *
     * @public
     * @param {string} requestId 请求Id，即sync方法调用的返回值
     */
    XDATASOURCE_CLASS.abort = function (requestId) {
        xajax.abort(requestId, true);
        delete this._oRequestSet[requestId];
    };
    
    /**
     * 包装成ejson对象
     *
     * @public
     * @param {*} data 业务数据
     * @param {number} status 返回状态，
     *              0为正常返回，非0为各种错误返回。缺省则为0。
     * @param {string} statusInfo 附加信息，可缺省
     * @return {Object} e-json对象
     */
    XDATASOURCE_CLASS.wrapEJson = function (data, status, statusInfo) {
        return { data: data, status: status || 0, statusInfo: statusInfo };
    };
    
    /**
     * 停止success流程，走向error流程。
     * 在parse或result中调用有效。
     * 一般用于parse或result中解析后台返回数据，
     * 发现数据错误，需要转而走向error流程的情况。
     *
     * @protected
     * @param {number=} status 错误状态码，如果不传则取DEFAULT_ERROR_STATUS
     * @param {string=} statusInfo 错误信息，可缺省
     */
    XDATASOURCE_CLASS.$goError = function (status, statusInfo) {
        this._bGoError = true;
        this._nErrorStatus = status == null ? DEFAULT_ERROR_STATUS : status;
        if (statusInfo != null) {
            this._sErrorStatusInfo = statusInfo; 
        }
    };

    /**
     * 预处理
     *
     * @private
     * @param {string} datasourceId 数据源id
     * @param {Object} options 参数
     */
    function handleSyncPreprocess(datasourceId, options) {
        handleAttr.call(this, datasourceId, 'preprocess', options);
        notifyEvent.call(this, datasourceId, 'sync.preprocess', options);
    }

    /**
     * 已有数据处理
     *
     * @private
     * @param {string} datasourceId 数据源id
     * @param {Object} options 参数
     * @param {*} data 业务数据
     */
    function handleSyncHasData(datasourceId, options, data) {
        handleAttr.call(
            this, datasourceId, 'result', options, 
            [data, this.wrapEJson(data)]
        );
        notifyEvent.call(
            this, datasourceId, 'sync.result', options, 
            [data, this.wrapEJson(data)]
        );
    }

    /**
     * 本地数据处理
     *
     * @private
     * @param {string} datasourceId 数据源id
     * @param {Object} options 参数
     * @param {(Object|string)} ejsonObj e-json对象
     */
    function handleSyncLocal(datasourceId, options, ejsonObj) {
        handleCallback.call(
            this, datasourceId, handleComplete, options, ejsonObj
        );

        if (!ejsonObj.status) { 
            // status为0则表示正常返回 (@see e-json)
            handleCallback.call(
                this, datasourceId, handleSuccess, options, ejsonObj.data, ejsonObj
            );
        }
        else {
            handleCallback.call(
                this, datasourceId, handleFailure, options, ejsonObj.status, ejsonObj
            );
        }

        handleCallback.call(
            this, datasourceId, handleFinalize, options, ejsonObj
        );
    }

    /**
     * 远程请求处理
     *
     * @private
     * @param {string} datasourceId 数据源id
     * @param {Object} options 参数
     * @param {string} url 请求url
     * @return {string} requestId 请求ID
     */
    function handleSyncRemote(datasourceId, options, url) {
        var opt = {};
        var me = this;
        var paramStr;
        var paramObj;

        // 准备ajax参数
        opt.method = 
            handleAttr.call(me, datasourceId, 'method', options) 
            || 'POST';

        opt.businessKey = 
            handleAttr.call(me, datasourceId, 'businessKey', options);

        opt.data =
            hasValue(
                paramStr = handleAttr.call(me, datasourceId, 'param', options)
            )
            ? paramStr : '';
        paramObj = utilUrl.parseParam(handleAttr.call(me, datasourceId, 'param', options));

        // TODO:
        opt.timeout = 
            handleAttr.call(me, datasourceId, 'timeoutTime', options) 
            || undefined;

        opt.onsuccess = function (data, ejsonObj) {
            handleCallback.call(
                me, datasourceId, handleSuccess, options, data, ejsonObj
            );
        };

        opt.onfailure = function (status, ejsonObj) {
            handleCallback.call(
                me, datasourceId, handleFailure, options, status, ejsonObj
            );
        };

        opt.oncomplete = function (ejsonObj) {
            handleCallback.call(
                me, datasourceId, handleComplete, options, ejsonObj
            );
            // 清除requestId
            delete me._oRequestSet[requestId];
        };

        opt.onfinalize = function (ejsonObj) {
            handleCallback.call(
                me, datasourceId, handleFinalize, options, ejsonObj
            );
        };

        opt.ontimeout = function () {
            handleCallback.call(
                me, datasourceId, handleTimeout, options
            );
        };

        opt = extend(
            opt, 
            handleAttr.call(me, datasourceId, 'ajaxOptions', options) || {}
        );
        
        this._sBusinessKey = opt.businessKey;

        //FIXME:这里需要把不需要往后端传的参数给干掉
        url = utilString.template(url, paramObj);
        // 发送ajax请求
        var requestId = xajax.request(url, opt, paramObj);
        this._oRequestSet[requestId] = 1;

        return requestId;
    }

    /**
     * 回调处理
     *
     * @private
     * @param {string} datasourceId 数据源id
     * @param {Function} callback 回调
     * @param {Object} options 参数
     */    
    function handleCallback(datasourceId, callback, options) {
        var args= sliceArray.call(arguments, 3, arguments.length);

        callback.apply(this, [datasourceId, options].concat(args));

        var i;
        var o;
        var list;

        // sync combines
        if (hasValue(datasourceId)) {
            list = this._oSyncCombineSet[datasourceId] || [];
            for (i = 0; o = list[i]; i++) {
                callback.apply(o, [datasourceId, {}].concat(args));
            }
        }

        list = this._aSyncCombineSetDefault || [];
        for (i = 0; o = list[i]; i++) {
            callback.apply(o, [datasourceId, {}].concat(args));
        }
    }
    
    /**
     * 回调处理
     *
     * @private
     * @param {string} datasourceId 数据源id
     * @param {Object} options 参数
     * @param {*} data 业务数据
     * @param {(Object|string)} ejsonObj e-json对象
     */    
    function handleSuccess(datasourceId, options, data, ejsonObj) {
        this._bGoError = false;

        function goFailure() {
            if (this._sErrorStatusInfo != null) {
                ejsonObj.statusInfo = this._sErrorStatusInfo;
            }
            handleCallback.call(
                this, 
                datasourceId, 
                handleFailure, 
                options, 
                this._nErrorStatus, 
                ejsonObj
            );
            this._bGoError = false;
            this._nErrorStatus = null;
            this._sErrorStatusInfo = null;
        }
        
        var data = handleAttr.call(
            this, datasourceId, 'parse', options, [data, ejsonObj]
        );
        if (this._bGoError) {
            goFailure.call(this);
            return;
        }

        handleAttr.call(
            this, datasourceId, 'result', options, [data, ejsonObj]
        );
        if (this._bGoError) {
            goFailure.call(this);
            return;
        }

        notifyEvent.call(
            this, datasourceId, 'sync.result', options, [data, ejsonObj]
        );
    }
    
    /**
     * 失败处理
     *
     * @private
     * @param {string} datasourceId 数据源id
     * @param {Object} options 参数
     * @param {number} status 返回状态
     * @param {(Object|string)} ejsonObj e-json对象
     */    
    function handleFailure(datasourceId, options, status, ejsonObj) {
        handleAttr.call(
            this, datasourceId, 'error', options, [status, ejsonObj]
        );
        notifyEvent.call(
            this, datasourceId, 'sync.error', options, [status, ejsonObj]
        );        
    }

    /**
     * 请求完结处理
     *
     * @private
     * @param {string} datasourceId 数据源id
     * @param {Object} options 参数
     * @param {(Object|string)} ejsonObj e-json对象
     */    
    function handleComplete(datasourceId, options, ejsonObj) {
        handleAttr.call(
            this, datasourceId, 'complete', options, [ejsonObj]
        );
        notifyEvent.call(
            this, datasourceId, 'sync.complete', options, [ejsonObj]
        );        
    }
    
    /**
     * 请求最终处理
     *
     * @private
     * @param {string} datasourceId 数据源id
     * @param {Object} options 参数
     * @param {(Object|string)} ejsonObj e-json对象
     */    
    function handleFinalize(datasourceId, options, ejsonObj) {
        handleAttr.call(
            this, datasourceId, 'finalize', options, [ejsonObj]
        );
        notifyEvent.call(
            this, datasourceId, 'sync.finalize', options, [ejsonObj]
        );        
    }
    
    /**
     * 请求超时处理
     *
     * @private
     * @param {string} datasourceId 数据源id
     * @param {Object} options 参数
     */    
    function handleTimeout(datasourceId, options) {
        handleAttr.call(this, datasourceId, 'timeout', options);
        notifyEvent.call(this, datasourceId, 'sync.timeout', options);
    }
    
    /**
     * 属性处理
     *
     * @private
     * @param {string} datasourceId 数据源id
     * @param {string} name 属性名
     * @param {Object} options 参数
     * @param {Array} args 调用参数
     */    
    function handleAttr(datasourceId, name, options, args) {
        options = options || {};
        args = args || [];
        args.push(options);
        
        var o;
        var datasourceId;

        // 优先使用options中的定义
        if (typeof options[name] != 'undefined') {
            o = options[name];
        } 
        else {
            // 次优先使用不分datasourceId的通用定义
            o = this[name];
            // 再次使用每个datasourceId的各自定义
            if (hasValue(datasourceId) 
                && isObject(o) 
                && hasValue(o[datasourceId])
            ) {
                o = o[datasourceId];
            }
        }

        if (o instanceof SET) { o = null; }

        return isFunction(o) ? o.apply(this, args) : o;
    }
    
    /**
     * 触发事件
     *
     * @private
     * @param {string} datasourceId 数据源id
     * @param {string} eventName 事件名
     * @param {Object} options 参数
     * @param {Array} args 调用参数
     */    
    function notifyEvent(datasourceId, eventName, options, args) {
        options = options || {};
        args = args || [];
        args.push(options);
        if (hasValue(datasourceId)) {
            this.notify(eventName + '.' + datasourceId, args);
        }
        this.notify(eventName, args);        
    }

    /**
     * 获得数据源id
     *
     * @private
     * @param {Object} options 参数
     * @return {string} 数据源id
     */    
    function getDatasourceId (options) {
        options = options || {};
        var datasourceId = hasValue(options.datasourceId) 
            ? options.datasourceId : this.datasourceId;
        return isFunction(datasourceId) 
            ? datasourceId.call(this, options) : datasourceId;
    }
    
    //-------------------------------------------------------------
    // [多XDatasource组合/绑定]                                               
    //-------------------------------------------------------------
    
    /**
     * 为了公用sync，绑定多个XDatasource
     * 这个功能用于多个XDatasource共享一个请求的情况。
     * sync及各种事件，会分发给被绑定的XDatasource，
     * 由他们分别处理（如做请求返回值解析，取的自己需要的部分）
     *
     * @public
     * @param {xui.XDatasource} xdatasource 要绑定的XDatasource
     * @param {string} datasourceId 绑定到此datasourceId上，
     *          缺省则绑定到所有datasourceId上
     */
    XDATASOURCE_CLASS.addSyncCombine = function (xdatasource, datasourceId) {
        if (!(xdatasource instanceof XDATASOURCE)) { 
            return;
        }

        var o;
        if (hasValue(datasourceId)) {
            if (!(o = this._oSyncCombineSet[datasourceId])) {
                o = this._oSyncCombineSet[datasourceId] = [];
            }
            o.push(xdatasource);
        } 
        else {
            this._aSyncCombineSetDefault.push(xdatasource);
        }
    };
    
    /**
     * 取消绑定XDatasource
     * 这个功能用于多个XDatasource共享一个请求的情况。
     * sync及各种事件，会分发给被绑定的XDatasource，
     * 由他们分别处理（如做请求返回值解析，取的自己需要的部分）
     *
     * @public
     * @param {xui.XDatasource} xdatasource 要取消绑定的XDatasource
     * @param {string} datasourceId 与addSyncCombine的定义须一致
     */
    XDATASOURCE_CLASS.removeSyncCombine = function (xdatasource, datasourceId) {
        if (!(xdatasource instanceof XDATASOURCE)) { return; }

        var o = hasValue(datasourceId) 
                    ? (this._oSyncCombineSet[datasourceId] || []) 
                    : (this._aSyncCombineSetDefault || []);

        for (var j = 0; j < o.length;) {
            (xdatasource === o[j]) ? o.splice(j, 1) : j++;
        }
    };
    
    //-------------------------------------------------------------
    // XDatasource扩展
    //-------------------------------------------------------------
    
    /**
     * 扩展
     * （禁止对XDatasource类本身使用extend）
     *
     * @public
     * @static
     * @param {Object} clz XDatasource子类本身
     * @param {Object} options 扩展的内容 (@see OPTIONS_NAME)
     */
    XDATASOURCE.extend = function (clz, options) {
        if (clz instanceof XDATASOURCE && clz !== XDATASOURCE) {
            extend(clz.prototype, options);
        }
    };
    
})();
