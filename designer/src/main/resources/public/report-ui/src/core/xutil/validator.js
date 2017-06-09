/**
 * xutil.validator
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    输入验证相关工具函数
 * @author:  sushuang(sushuang)
 * @depend:  xutil.lang
 */

(function () {
    
    var VALIDATOR = xutil.validator = {};

    var REGEXP_CASH = /^\d+(\.\d{1,2})?$/;
    var REGEXP_CASH_CAN_NAGE = /^(\+|-)?\d+(\.\d{1,2})?$/;
    var REGEXP_EMAIL = /^[_\w-]+(\.[_\w-]+)*@([\w-])+(\.[\w-]+)*((\.[\w]{2,})|(\.[\w]{2,}\.[\w]{2,}))$/;
    var REGEXP_URL = /^[^.。，]+(\.[^.，。]+)+$/;
    var REGEXP_MOBILE = /^1\d{10}$/;
    var REGEXP_ZIP_CODE = /^\d{6}$/;
    
    /**
     * 是否金额
     * 
     * @pubilc
     * @param {string} value 目标字符串
     * @param {boolean} canNagetive 是否允许负值，缺省为false
     * @returns {boolean} 验证结果
     */
    VALIDATOR.isCash = function (value, canNagetive) {
        return canNagetive 
            ? REGEXP_CASH_CAN_NAGE.test(value) : REGEXP_CASH.test(value);
    };   

    /**
     * 是否金额
     * 
     * @pubilc
     * @param {string} value 目标字符串
     * @returns {boolean} 验证结果
     */
    VALIDATOR.isURL = function (value) {
        return REGEXP_URL.test(value); 
    };

    /**
     * 是否移动电话
     * 
     * @pubilc
     * @param {string} value 目标字符串
     * @returns {boolean} 验证结果
     */
    VALIDATOR.isMobile = function (value) {
        return REGEXP_MOBILE.test(value);
    };    

    /**
     * 是否电子邮箱
     * 
     * @pubilc
     * @param {string} value 目标字符串
     * @returns {boolean} 验证结果
     */
    VALIDATOR.isEMAIL = function (value) {
        return REGEXP_EMAIL.test(value);
    };
    
    /**
     * 是否邮政编码
     * 
     * @pubilc
     * @param {string} value 目标字符串
     * @returns {boolean} 验证结果
     */
    VALIDATOR.isZipCode = function (value) {
        return REGEXP_ZIP_CODE.test(value);
    };
    
})();
