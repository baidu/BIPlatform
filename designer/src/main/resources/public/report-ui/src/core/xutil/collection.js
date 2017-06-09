/**
 * xutil.collection
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    列表、数组、集合相关工具函数
 * @author:  sushuang(sushuang)
 * @depend:  xutil.object
 */

(function () {
    
    var COLLECTION = xutil.collection;
    var OBJECT = xutil.object;
    
    /**
     * target是否在list的field域中存在
     * 
     * @public
     * @param {*} target 被检测的目标
     * @param {Array} list 被检测的数组
     * @param {string} field 数组元素的域的名字，
     *      如果为空则用list节点本身做比较的valueInList
     * @param {Function} equalsFunc 比较函数，缺省则使用“==”做比较函数
     *          参数为：
     *          param {*} target 被检测的目标
     *          param {*} valueInList list中的项
     *          return {boolean} 是否相等
     * @return {boolean} 判断结果
     */
    COLLECTION.inList = function (target, list, field, equalsFunc) {
        if (target == null || !list) {
            return false;
        }

        for(var i = 0, l = list.length, v; i < l; i ++) {
            v = list[i];
            if (v == null && field) { continue; }

            v = field ? v[field] : v;
            if (equalsFunc ? equalsFunc(target, v) : (target == v)) {
                return true;
            }
        }

        return false;
    };

    /**
     * 用类似SQL的方式检索列表
     * 
     * @public
     * @param {*} target 被检测的目标
     * @param {Array} list 被检测的数组
     * @param {string} selectField 数组元素的域的名字，用于select
     * @param {string} whereField 数组元素的域的名字，用于where
     * @param {*} whereValue 数组元素的相应域的值，用于where
     * @param {Function} equalsFunc 比较函数，缺省则使用“==”做比较函数
     *          参数为：
     *          param {*} target 被检测的目标
     *          param {8} valueInList list中的项
     *          return {boolean} 是否相等
     * @return {Array} 检索结果
     */
    COLLECTION.selectFromWhere = function (
        fromList, selectField, whereField, whereValue, equalsFunc
    ) {
        var ret = [];

        if (whereValue == null || !fromList || !whereField || !selectField) {
            return ret;
        }

        for(var i = 0, l = fromList.length, v, s; i < l; i ++) {
            if (!(v = fromList[i])) { continue };

            s = v[whereField];
            if (equalsFunc ? equalsFunc(whereValue, s) : whereValue == s) {
                ret.push(v[selectField]);
            }
        }

        return ret;
    };
    
    /**
     * 用类似SQL的方式检索列表，返回单值
     * 
     * @public
     * @param {*} target 被检测的目标
     * @param {Array} list 被检测的数组
     * @param {string} selectField 数组元素的域的名字，用于select
     * @param {string} whereField 数组元素的域的名字，用于where
     * @param {*} whereValue 数组元素的相应域的值，用于where
     * @param {Function} equalsFunc 比较函数，缺省则使用“==”做比较函数
     *          param {*} target 被检测的目标
     *          param {*} valueInList list中的项
     *          return {boolean} 是否相等
     * @return {*} 检索结果，单值
     */
    COLLECTION.selectSingleFromWhere = function (
        fromList, selectField, whereField, whereValue, compareFunc
    ) {
        var result = COLLECTION.selectFromWhere(
                fromList, selectField, whereField, whereValue, compareFunc
            );
        return (result && result.length>0) ? result[0] : null;
    };
    
    /**
     * 排序 (用冒泡实现，是稳定排序)
     * 
     * @public
     * @param {string} field 数组元素的域的名字，如果为空则用list节点本身做比较的valueInList
     * @param {(string|Function)} compareFunc 比较函数，
     *          可以传string或Function，compareFunc缺省则相当于传"<" 
     *          如果为String: 可以传：">"（即使用算术比较得出的降序）, 
     *                                "<"（即使用算术比较得出的升序）
     *          如果为Function: 意为：v1是否应排在v2前面，参数为
     *              param {*} v1 参与比较的第一个值
     *              param {*} v2 参与比较的第二个值
     *              return {boolean} 比较结果，true:v1应在v2前面；false:v1不应在v2前面
     * @param {boolean} willNew 如果为true:原list不动，新创建一个list; 
     *          如果为false:在原list上排序; 缺省:false
     * @return {Array} 排序结果
     */
    COLLECTION.sortList = function (list, field, compareFunc, willNew) {
        
        willNew && (list = OBJECT.clone(list));
        field = field != null ? field : null;    
        
        if (compareFunc == '>') {
            compareFunc = function (v1, v2) { 
                var b1 = v1 != null;
                var b2 = v2 != null;
                return (b1 && b2) 
                            ? (v1 >= v2) /*大于等于，保证稳定*/ 
                            : (b1 || !b2); /*空值算最小，同为空值返回true保证稳定*/
            }
        } 
        else if (compareFunc == '<') {
            compareFunc = function (v1, v2) { 
                var b1 = v1 != null;
                var b2 = v2 != null;
                return (b1 && b2) 
                            ? (v1 <= v2) /*小于等于，保证稳定*/ 
                            : (!b1 || b2); /*空值算最大，同为空值返回true保证稳定*/
            }
        }
        
        var item1;
        var item2; 
        var v1;
        var v2;
        var switched = true;

        for (var i = 0, li = list.length - 1; i < li && switched; i ++) {
            switched = false;
            
            for (var j = 0, lj = list.length - i - 1; j < lj; j ++) {
                item1 = list[j];
                v1 = item1 != null ? (field ? item1[field] : item1) : null;
                item2 = list[j + 1];
                v2 = item2 != null ? (field ? item2[field] : item2) : null;
                if (!compareFunc(v1, v2)) {
                    list[j] = item2;
                    list[j + 1] = item1;
                    switched = true;
                }
            }
        }

        return list;    
    };
    
    /**
     * 遍历树
     * 支持先序遍历、后序遍历、中途停止
     * 
     * @public
     * @usage
     *      travelTree(root, funciton (node, options) { 
     *          do something ... 
     *      }, '_aChildren');
     * 
     * @param {Object} travelRoot 遍历的初始
     * @param {Function} callback 每个节点的回调
     *          参数为：
     *          param {Object} node 当前访问的节点
     *          param {Object} options 一些遍历中的状态
     *          param {number} options.level 当前层级，0层为根
     *          param {number} options.index 遍历的总计数，从0开始计
     *          param {Object} options.parent 当前节点的父亲
     *          param {Object} options.globalParam 全局用的参数，在遍历的任何环节可以填入
     *          param {Object} options.parentParam
     *              先序遍历时，此对象用于在callback中取出父节点传递来的数据
     *              后序遍历时，此对象用于在callback中填入的要传递给父节点的数据
     *          param {Object} options.childrenParam 
     *              先序遍历时，此对象用于在callback中填入的要传递给子节点的数据
     *              后序遍历时，此对象用于在callback中取出子节点传递来的数据
     *          return {number} 如果为STOP_ALL_TRAVEL则停止所有遍历，
     *              如果为STOP_SUB_TREE_TRAVEL则停止遍历当前子树
     * @param {string} childrenField 子节点列表属性名，缺省为'children'
     * @param {boolean} postorder true则先序遍历（缺省值），false则后序遍历
     * @param {Object} globalParam 全局参数
     */
    COLLECTION.travelTree = function (
        travelRoot, callback, childrenField, postorder, globalParam
    ) {
        $travelTree(
            travelRoot, 
            callback, 
            childrenField, 
            postorder, 
            0, 
            null, 
            { index:0 }, 
            {}, 
            {}, 
            globalParam || {}
        );
    }

    // 用于停止所有遍历
    COLLECTION.STOP_ALL_TRAVEL = 1; 
    // 用于停止遍历当前子树
    COLLECTION.STOP_SUB_TREE_TRAVEL = 2; 
    
    function $travelTree(
        travelRoot, 
        callback, 
        childrenField, 
        postorder, 
        level, 
        parent, 
        indexRef, 
        inToChildrenParam, 
        inToParentParam, 
        globalParam
    ) {
        if (travelRoot == null) {
            return;
        }
            
        postorder = !!postorder;
        
        var conti;
        var toChildrenParam;
        var toParentParam;

        if (!postorder) {
            conti = callback.call(
                null, 
                travelRoot, 
                {
                    level: level, 
                    index: indexRef.index, 
                    parent: parent, 
                    childrenParam: (toChildrenParam = {}), 
                    parentParam: inToChildrenParam,
                    globalParam: globalParam
                }
            );
            indexRef.index ++;
        }
        
        if (conti === COLLECTION.STOP_ALL_TRAVEL) {
            return conti; 
        }
        if (conti === COLLECTION.STOP_SUB_TREE_TRAVEL) { 
            return; 
        }
        
        var children = travelRoot[childrenField || 'children'] || [];
        for (var i = 0, len = children.length, node; i < len; i ++) {
            node = children[i];
            
            conti = $travelTree(
                node, 
                callback, 
                childrenField, 
                postorder, 
                level + 1, 
                travelRoot, 
                indexRef, 
                toChildrenParam, 
                (toParentParam = {}), 
                globalParam
            );
                
            if (conti === COLLECTION.STOP_ALL_TRAVEL) { 
                return conti; 
            }
        }
        
        if (postorder && conti !== COLLECTION.STOP_ALL_TRAVEL) { 
            conti = callback.call(
                null, 
                travelRoot, 
                {
                    level: level, 
                    index: indexRef.index, 
                    parent: parent, 
                    childrenParam: toParentParam, 
                    parentParam: inToParentParam,
                    globalParam: globalParam
                }
            );
            indexRef.index ++;
        }
        
        if (conti === COLLECTION.STOP_ALL_TRAVEL) { 
            return conti; 
        }
    };    

})();
