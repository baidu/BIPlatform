/**
 * xutil.uid
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    唯一性ID相关工具函数
 * @author:  sushuang(sushuang)
 * @depend:  none
 */

(function () {
    
    var UID = xutil.uid;
    var INCREASED_UID_BASE_PUBLIC = 1;
    var INCREASED_UID_BASE_PRIVATE = {};
    
    /**
     * 获取不重复的随机串（自增，在单浏览器实例，无worker情况下保证唯一）
     * @public
     * 
     * @param {Object} options
     * @param {string} options.key UID的所属。
     *          缺省则为公共UID；传key则为私有UID。
     *          同一key对应的UID不会重复，不同的key对应的UID可以重复。
     * @return {string} 生成的UID
     */
    UID.getIncreasedUID = function (key) {
        if (key != null) {
            !INCREASED_UID_BASE_PRIVATE[key] 
                && (INCREASED_UID_BASE_PRIVATE[key] = 1);
            return INCREASED_UID_BASE_PRIVATE[key] ++;
        } 
        else {
            return INCREASED_UID_BASE_PUBLIC ++ ;
        }
    };
    
    /**
     * 也可以在应用中重载此定义
     */
    UID.getUID = UID.getIncreasedUID;
    
})();

