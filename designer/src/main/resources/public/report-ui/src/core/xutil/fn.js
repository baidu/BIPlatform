/**
 * xutil.fn
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    函数相关工具函数
 * @author:  sushuang(sushuang)
 * @depend:  xutil.lang
 */

(function () {
    
    var FN = xutil.fn;
    var LANG = xutil.lang;
    var slice = Array.prototype.slice;
    var nativeBind = Function.prototype.bind;
    
    /**
     * 为一个函数绑定一个作用域
     * 如果可用，使用**ECMAScript 5**的 native `Function.bind`
     * 
     * @public
     * @param {Function|string} func 要绑定的函数，缺省则为函数本身
     * @param {Object} context 作用域
     * @param {Any...} 绑定附加的执行参数，可缺省
     * @rerturn {Funtion} 绑定完得到的函数
     */
    FN.bind = function (func, context) {
        var args;
        if (nativeBind && func.bind === nativeBind) {
            return nativeBind.apply(func, slice.call(arguments, 1));
        }
        func = LANG.isString(func) ? context[func] : func;
        args = slice.call(arguments, 2);
        return function () {
            return func.apply(
                context || func, args.concat(slice.call(arguments))
            );
        };
    };

})();
