/**
 * xui.XPorject
 * Copyright 2012 Baidu Inc. All rights reserved.
 * 
 * @file:    一种Javascript工程组织方法
 *          [功能]
 *              (1) 各级名空间建立
 *              (2) 交叉引用/文件依赖的一种解决方案（闭包变量注入）
 * @author:  sushuang(sushuang)
 * @version: 1.0.1
 */

/**
 * @usage [引入XProject]
 *          为了在代码中方便使用XProject提供的方法，
 *          可以在工程开始时在全局定义方法的别名。
 *
 *          例如：
 *          window.$ns = xui.XProject.namespace;
 *          window.$link = xui.XProject.link;
 *          （下文中为书写简便假设已经做了如上别名定义）
 * 
 * @usage [名空间建立]
 *          假设准备建立一个类：
 *
 *          // 直接建立了名空间
 *          $ns('aaa.bbb.ccc');
 *  
 *          // 类的构造函数
 *          aaa.bbb.ccc.SomeClass = function () {
 *              // do something ...
 *          }
 *
 *          或者直接：
 *          // 类的构造函数
 *          $ns('aaa.bbb.ccc').SomeClass = function () { 
 *              // do something ...
 *          }
 *
 *          或者这种风格：
 *          // 文件开头声明名空间
 *          $ns('aaa.bbb.ccc'); 
 *          (function () {
 *              // $ns()会返回最近一次声明名空间的结果
 *              $ns().SomeClass = function () { 
 *                  // do something ...
 *              }
 *          })();
 *        
 * @usage [依赖/交叉引用/link]
 *          工程中对象的交叉引用不在这里考虑，
 *          这里考虑的是类型/全局结构定义阶段的交叉引用，
 *          如下例类型定义时：
 *
 *          (function () {
 *              // 在闭包中定义外部引用的类，
 *              // 这么做的好处至少有：方便压缩，易适应路径改动，代码简洁。
 *              var OTHER_CONTROL1 = aaa.bbb.SomeClass;
 *              var OTHER_SERVICE2 = tt.ee.SomeService;
 *              var OTHER_MODEL3 = qq.uu.ii.SomeModel;
 *              
 *              // 构造函数，定义本类
 *              $ns('aaa.bb').MyControl = function () { 
 *                  this.otherControl = new OTHER_CONTROL();
 *                  ...
 *              }
 *              ...
 *          })();
 *          这种情况下，如果多个类互相有引用（形成闭环），
 *          则不知道如何排文件顺序，来使闭包中的类型/函数引用OK，
 *          而C++/Java等常用的编译型面向对象语言都默认支持不需关心这些问题。
 * 
 *          这里使用这种解决方式：
 *          (function () {
 *              // 先在闭包中声明
 *              var OTHER_CONTROL1, OTHER_SERVICE2, OTHER_MODEL3;
 *              // 连接
 *              $link(function () {
 *                  OTHER_CONTROL1 = aaa.bbb.SomeClass;
 *                  OTHER_SERVICE2 = tt.ee.SomeService;
 *                  OTHER_MODEL3 = qq.uu.ii.SomeModel;
 *              });
 *              //构造函数，定义本类
 *              $ns('aa.bb').MyControl = function () { 
 *                  this.otherControl = new OTHER_CONTROL();
 *                  // ...
 *              }
 *              // ...
 *          })();
 *            
 *          在所有文件的最后，调用xui.XProject.doLink()，则实际注入所有的引用。
 */

(function () {
    
    var XPROJECT = xui.XProject = {};
    var NS_BASE = window;
    var TRIMER = new RegExp(
            "(^[\\s\\t\\xa0\\u3000]+)|([\\u3000\\xa0\\s\\t]+\x24)", "g"
        );

    /**
     * 延迟执行函数的集合
     *
     * @type {Array.<Function>}
     * @private
     */
    var linkSet = [];
    /**
     * 最终执行的函数集合
     *
     * @type {Array.<Function>}
     * @private
     */
    var endSet = [];
    /**
     * 最近一次的名空间
     *
     * @type {Object}
     * @private
     */
    var lastNameSpace;
    
    /**
     * (1) 创建名空间：如调用namespace("aaa.bbb.ccc")，如果不存在，则建立。
     * (2) 获得指定名空间：如上，如果存在NS_BASE.aaa.bbb.ccc，则返回。
     * (3) 获得最近一次声明的名空间：调用namespace()，不传参数，
     *      则返回最近一次调用namespace（且isRecord参数不为false）得到的结果
     * 
     * NS_BASE默认是window (@see setNamespaceBase)。 
     *
     * @public
     * @param {string=} namespacePath 名空间路径，
     *              以"."分隔，如"aaa.bbb.ccc"，
     *              如果不传参，则返回最近一次调用结果。
     * @param {boolean} isRecord 是否记录此次调用结果，缺省则表示true
     * @return {Object} 名空间对象
     */
    XPROJECT.namespace = function (namespacePath, isRecord) {
        if (arguments.length == 0) {
            return lastNameSpace;
        }
        
        var context = NS_BASE;
        var pathArr = parseInput(namespacePath).split('.');
        for (var i = 0 ;i < pathArr.length; i ++) {
            context = getOrCreateObj(context, parseInput(pathArr[i]));
        }
        
        if (isRecord !== false) {
            lastNameSpace = context
        }
        
        return context;
    };
    
    /**
     * 注册一个连接
     *
     * @public
     * @param {Function} func 链接函数
     */
    XPROJECT.link = function (func) {
        if (!isFunction(func)) {
            throw new Error (
                'Input of link must be a function but not ' + func
            );
        }
        linkSet.push(func);
    };
    
    /**
     * 执行所有连接并清空注册
     *
     * @public
     */
    XPROJECT.doLink = function () {
        for(var i = 0, o; o = linkSet[i]; i++) {
            o.call(null);
        }
        linkSet = []; 
    };
    
    /**
     * 注册一个最后执行的函数
     *
     * @public
     * @param {Function} func 链接函数
     */
    XPROJECT.end = function (func) {
        if (!isFunction(func)) {
            throw new Error (
                'Input of link must be a function but not ' + func
            );
        }
        endSet.push(func);
    };
    
    /**
     * 执行所有最后执行的注册并清空注册
     *
     * @public
     */
    XPROJECT.doEnd = function () {
        for(var i = 0, o; o = endSet[i]; i++) {
            o.call(null);
        }
        endSet = []; 
    };
    
    /**
     * 设置名空间查找根基，默认是window
     *
     * @public
     * @param {Object} namespaceBase 名空间根基
     */
    XPROJECT.setNamespaceBase = function (namespaceBase) {
        namespaceBase && (NS_BASE = namespaceBase);
    };

    /**
     * 得到名空间查找根基，默认是window
     *
     * @public
     * @return {Object} 名空间根基
     */
    XPROJECT.getNamespaceBase = function () {
        return NS_BASE;
    };
    
    /**
     * Parse输入
     *
     * @private
     * @param {string} input 输入
     * @return {boolean} parse结果
     */
    function parseInput(input) {
        var o;
        if ((o = trim(input)) == '') {
            throw new Error('Error input: ' + str);   
        } 
        else {
            return o;
        }
    }
    
    /**
     * 创建及获得路径对象
     *
     * @private
     * @param {Object} context 上下文
     * @param {string} attrName 属性名
     * @return {Object} 得到的对象
     */
    function getOrCreateObj(context, attrName) {
        var o = context[attrName];
        return o != null ? o : (context[attrName] = {});
    }
    
    /**
     * 是否函数
     *
     * @private
     * @param {*} variable 输入
     * @return {boolean} 是否函数
     */
    function isFunction(variable) {
        return Object.prototype.toString.call(variable) == '[object Function]';
    }
    
    /**
     * 字符串trim
     *
     * @private
     * @param {string} 输入
     * @return {string} 结果
     */
    function trim(source) {
        return source == null ? '' : String(source).replace(TRIMER, '');
    }
    
})();
