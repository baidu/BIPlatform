/**
 * xutil.object
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    对象相关工具函数
 * @author:  sushuang(sushuang)
 * @depend:  none
 */

(function () {
    
    var OBJECT = xutil.object;
    var objProtoToString = Object.prototype.toString;
    var hasOwnProperty = Object.prototype.hasOwnProperty;
    var arraySlice = Array.prototype.slice;
    
    /**
     * getByPath和setByPath的默认context。
     * 可以在工程中修改。
     */
    OBJECT.PATH_DEFAULT_CONTEXT = window;

    /**
     * 根据对象路径得到数据。
     * 默认根是window。
     * 路径中支持特殊字符（只要不和分隔符冲突即可）。
     * 路径分隔符可以定制，默认是点号和中括号。
     * 如果未取到目标，返回null。
     * 注意：此方法不会去trim路径中的空格。
     * 例如：
     *      在window中
     *      已有var obj = { asdf: { zxcv: { qwer: 12 } } };
     *      可用getByPath('obj.asdf.zxcv.qwer'); 得到数值12。
     *      已有var obj = { aaa: [123, { fff: 678 }] };（路径中有数组）
     *      可用getByPath('aaa.2.fff', obj);
     *      或getByPath('aaa[2].fff', obj);得到数值678。
     * 
     * @public
     * @param {string} path 如xxx.sss.aaa[2][3].SomeObj，
     *      如果为null或undefined，返回context。
     * @param {Object=} context 根，缺省是window，
     *     另外可使用OBJECT.PATH_DEFAULT_CONTEXT配置缺省值
     * @param {Object=} options 选项
     * @param {string=} objDelimiter 对象定界符，默认是点号
     * @param {string=} arrBegin 数组起始标志符，默认是左方括号
     * @param {string=} arrEnd 数组结束标志符，默认是右方括号
     * @return {*} 取得的数据，如上例得到SomeObj
     */
    OBJECT.getByPath = function (path, context, options) {
        options = options || {};
        context = context || OBJECT.PATH_DEFAULT_CONTEXT;

        if (path == null) { return context; }

        var arrBegin = options.arrBegin || '[';
        var arrEnd = options.arrEnd || ']';
        var pathArr = path.split(
                options.objDelimiter != null ? options.objDelimiter : '.'
            );

        for (var i = 0, j, pai, pajs, paj; i < pathArr.length; i ++) {
            pai = pathArr[i];
            pajs = pai.split(arrBegin);

            for (j = 0; j < pajs.length; j ++) {
                paj = pajs[j];
                j > 0 && (paj = paj.split(arrEnd)[0]);

                // 如果未取到目标时context就非对象了
                if (context !== Object(context)) {
                    return;
                }

                context = context[paj];
            }
        }
        return context;
    };

    /**
     * 根据对象路径设置数据。
     * 默认根是window。
     * 如果路径中没有对象/数组，则创建之。
     * 路径中支持特殊字符（只要不和分隔符冲突即可）。
     * 路径分隔符可以定制，默认是点号和中括号。
     * 注意：此方法不会去trim路径中的空格。
     * 例如：
     *      可用setByPath('obj.asdf.zxcv', 12); 
     *      在window中生成对象obj，其内容为{ asdf: { zxcv: 12 } };
     *      又可用setByPath('asdf.aaa[2].fff', 678, obj);
     *      或者setByPath('obj.asdf.aaa[2].fff', 678);
     *      对obj赋值，使obj值最终为：
     *          { 
     *              asdf: { 
     *                  zxcv: 12,
     *                  aaa: [undefined, { fff: 678 }] 
     *              } 
     *          };（路径中有数组）
     * 
     * @public
     * @param {string} path 如xxx.sss.aaa[2][3].SomeObj
     * @param {*} value 要设置的值
     * @param {Object=} context 根，缺省是OBJECT.PATH_DEFAULT_CONTEXT
     *     另外可使用OBJECT.PATH_DEFAULT_CONTEXT配置缺省值
     * @param {Object=} options 选项
     * @param {string=} objDelimiter 对象定界符，默认是点号
     * @param {string=} arrBegin 数组起始标志符，默认是左方括号
     * @param {string=} arrEnd 数组结束标志符，默认是右方括号
     * @param {string=} conflict 当路径冲突时的处理.
     *      路径冲突指路径上已有值（即非undefined或null）但不是对象，
     *      例如假设当前已经有var obj = { a: 5 };
     *      又想setByPath('a.c.d', obj, 444)。
     *      conflict值可为：
     *          'THROW': 路径冲突时抛出异常（默认）；
     *          'IGNORE': 路径冲突时不做任何操作直接返回；
     *          'OVERLAP': 路径冲突时直接覆盖。
     */
    OBJECT.setByPath = function (path, value, context, options) {
        options = options || {};
        context = context || OBJECT.PATH_DEFAULT_CONTEXT;
        
        if (path == null) { return; }

        var arrBegin = options.arrBegin || '[';
        var arrEnd = options.arrEnd || ']';
        var conflict = options.conflict || 'THROW';
        var pathArr = path.split(
                options.objDelimiter != null ? options.objDelimiter : '.'
            );

        for (var i = 0, j, pai, pajs, paj, pv; i < pathArr.length; i ++) {
            pai = pathArr[i];
            pajs = pai.split(arrBegin);

            for (j = 0; j < pajs.length; j ++) {
                paj = pajs[j];
                j > 0 && (paj = paj.split(arrEnd)[0]);
                pv = context[paj];

                // 最终赋值
                if (i == pathArr.length - 1 && j == pajs.length - 1) {
                    context[paj] = value;
                    return;
                }
                else {
                    // 如果路径上已有值但不是对象
                    if (pv != null && pv !== Object(pv)) {
                        if (conflict == 'THROW') {
                            throw new Error('Path conflict: ' + path);
                        }
                        else if (conflict == 'IGNORE') {
                            return;
                        }
                    }

                    context = pv !== Object(pv)
                        // 如果路径上没有对象则创建
                        ? (
                            context[paj] = pajs.length > 1 && j < pajs.length - 1 
                            ? [] : {}
                        )
                        : context[paj];
                }
            }
        }
    };
    
    /**
     * 兼容性的setter，向一个对象中set数据
     * 
     * @public
     * @param {Object} container 目标对象
     * @param {string} key 关键字
     * @param {*} value 数据
     */
    OBJECT.set = function (container, key, value) {
        if (isFunction(container['set'])) {
            container['set'](key, value);
        } 
        else {
            container[key];
        }
    };

    /**
     * 在某对象中记录key/检查是否有key的方便方法
     * 
     * @public
     * @param {string=} key 如果为空，则fanhuitrue
     * @param {Object} context 需要enable/disable的对象
     * @return {boolean} 是否可以enable
     */
    OBJECT.objKey = (function () {

        /**
         * 在目标对象中会占用此成员记录key
         */
        var KEY_ATTR_NAME = '\x07__OBJ__KEY__';

        /**
         * 检查对象中是否有记录的key
         * 
         * @public
         * @param {Object} context 目标对象
         * @param {string=} key 如果为null或undefined，则返回false
         * @param {string=} keyName key种类名称，
         *      如果在对象中使用一种以上的key时，用此区别，否则缺省即可。
         * @return {boolean} 是否有key
         */
        function has(context, key, keyName) {
            if (key == null) { return false; }

            var hasKey = false;
            var keyList = getKeyList(context, keyName);

            for (var i = 0; i < keyList.length; i ++) {
                if (key == keyList[i]) {
                    hasKey = true;
                }
            }

            return hasKey;        
        }

        /**
         * 对象中key的数量
         * 
         * @public
         * @param {Object} context 目标对象
         * @param {string=} keyName key种类名称，
         *      如果在对象中使用一种以上的key时，用此区别，否则缺省即可。
         * @return {number} key的数量
         */
        function size(context, keyName) {
            return getKeyList(context, keyName).length;
        }

        /**
         * 在对象中记录key
         * 
         * @public
         * @param {Object} context 需要enable/disable的对象
         * @param {string=} key 如果为null或undefined，则不记录key
         * @param {string=} keyName key种类名称，
         *      如果在对象中使用一种以上的key时，用此区别，否则缺省即可。
         */
        function add(context, key, keyName) {
            if (key == null) { return; }

            if (!has(context, key, keyName)) {
                getKeyList(context, keyName).push(key);
            }
        }

        /**
         * 在对象中删除key
         * 
         * @public
         * @param {Object} context 需要enable/disable的对象
         * @param {string=} key 如果为null或undefined，则不删除key
         * @param {string=} keyName key种类名称，
         *      如果在对象中使用一种以上的key时，用此区别，否则缺省即可。
         */
        function remove(context, key, keyName) {
            if (key == null) { return; }

            var keyList = getKeyList(context, keyName);

            for (var i = 0; i < keyList.length; ) {
                if (key == keyList[i]) {
                    keyList.splice(i, 1);
                }
                else {
                    i ++;
                }
            }
        }

        /**
         * 得到keylist
         * 
         * @private
         * @param {Object} context 目标对象
         * @param {string=} keyName key种类名称，
         *      如果在对象中使用一种以上的key时，用此区别，否则缺省即可。
         * @return {Array} 
         */
        function getKeyList(context, keyName) {
            if (keyName == null) {
                keyName = '';
            }

            if (!context[KEY_ATTR_NAME + keyName]) {
                context[KEY_ATTR_NAME + keyName] = [];
            }

            return context[KEY_ATTR_NAME + keyName];
        }

        return {
            add: add,
            remove: remove,
            has: has,
            size: size,
            KEY_ATTR_NAME: KEY_ATTR_NAME
        };

    })();

    /**
     * 兼容性的getter，从一个对象中get数据
     * 
     * @public
     * @param {Object} container 目标对象
     * @param {string} key 关键字
     * @return {*} 数据
     */
    OBJECT.get = function (container, key) {
        if (isFunction(container['get'])) {
            return container['get'](key);
        } 
        else {
            return container[key];
        }
    };

    /**
     * 是否是空对象
     * 
     * @public
     * @param {Object} o 输入对象
     * @return {boolean} 是否是空对象
     */
    OBJECT.isEmptyObj = function (o) {    
        if (o !== Object(o)) {
            return false;
        }
        for (var i in o) {
            return false;
        }
        return true;
    };
                
    /**
     * 属性拷贝（对象浅拷贝）
     * target中与source中相同的属性会被覆盖。
     * prototype属性不会被拷贝。
     * 
     * @public
     * @usage extend(target, source1, source2, source3);
     * @param {(Object|Array)} target
     * @param {(Object|Array)...} source 可传多个对象，
     *          从第一个source开始往后逐次extend到target中
     * @return {(Object|Array)} 目标对象
     */
    OBJECT.extend = function (target) {
        var sourceList = arraySlice.call(arguments, 1);
        for (var i = 0, source, key; i < sourceList.length; i ++) {
            if (source = sourceList[i]) {
                for (key in source) {
                    if (source.hasOwnProperty(key)) {
                        target[key] = source[key];
                    }
                }
            }
        }
        return target;
    };
    
    /**
     * 属性赋值（对象浅拷贝）
     * 与extend的不同点在于，可以指定拷贝的属性，
     * 但是不能同时进行多个对象的拷贝。
     * target中与source中相同的属性会被覆盖。
     * prototype属性不会被拷贝。
     * 
     * @public
     * @param {(Object|Array)} target 目标对象
     * @param {(Object|Array)} source 源对象
     * @param {(Array.<string>|Object)} inclusion 包含的属性列表
     *          如果为{Array.<string>}，则意为要拷贝的属性名列表，
     *              如['aa', 'bb']表示将source的aa、bb属性
     *              分别拷贝到target的aa、aa上
     *          如果为{Object}，则意为属性名映射，
     *              如{'sAa': 'aa', 'sBb': 'bb'}表示将source的aa、bb属性
     *              分别拷贝到target的sAa、sBb上
     *          如果为null或undefined，
     *              则认为所有source属性都要拷贝到target中
     * @param {Array.<string>} exclusion 不包含的属性列表，
     *              如果与inclusion冲突，以exclusion为准.
     *          如果为{Array.<string>}，则意为要拷贝的属性名列表，
     *              如['aa', 'bb']表示将source的aa、bb属性分别拷贝到target的aa、aa上
     *          如果为null或undefined，则忽略此参数
     * @return {(Object|Array)} 目标对象
     */
    OBJECT.assign = function (target, source, inclusion, exclusion) {
        var i;
        var len;
        var inclusionMap = makeClusionMap(inclusion);
        var exclusionMap = makeClusionMap(exclusion);

        for (var i in source) {
            if (source.hasOwnProperty(i)) {
                if (!inclusion) {
                    if (exclusionMap[i] == null) {
                        target[i] = source[i];
                    }
                }
                else {
                    if (inclusionMap[i] != null && exclusionMap[i] == null) {
                        target[inclusionMap[i]] = source[i];
                    }
                }
            }
        }

        return target;
    };       
    
    /**
     * 对象深拷贝
     * 原型上的属性不会被拷贝。
     * 非原型上的属性中，
     * 会进行克隆的属性：
     *      值属性
     *      数组
     *      Date
     *      字面量对象(literal object @see isPlainObject)
     * 不会进行克隆只引用拷贝的属性：
     *      其他类型对象（如DOM对象，RegExp，new somefunc()创建的对象等）
     * 
     * @public
     * @param {(Object|Array)} source 源对象
     * @param {Object=} options 选项
     * @param {Array.<string>} options.exclusion 不包含的属性列表
     * @return {(Object|Array)} 新对象
     */
    OBJECT.clone = function (source, options) {
        options = options || {};
        var result;
        var i;
        var isArr;
        var exclusionMap = makeClusionMap(options.exclusion);

        if (isPlainObject(source)
            // 对于数组也使用下面方式，把非数字key的属性也拷贝
            || (isArr = isArray(source))
        ) {
            result = isArr ? [] : {};
            for (i in source) {
                if (source.hasOwnProperty(i) && !(i in exclusionMap)) {
                    result[i] = OBJECT.clone(source[i]);
                }
            }
        } 
        else if (isDate(source)) {
            result = new Date(source.getTime());
        } 
        else {
            result = source;
        }
        return result;
    };

    /**
     * 两个对象融合
     * 
     * @public
     * @param {(Object|Array)} target 目标对象
     * @param {(Object|Array)} source 源对象
     * @param {Object} options 参数
     * @param {boolean} options.overwrite 是否用源对象的属性覆盖目标对象的属性（默认true）
     * @param {(boolean|string)} options.clone 对于对象属性，
     *      如果值为true则使用clone（默认），
     *      如果值为false则直接引用，
     *      如果值为'WITHOUT_ARRAY'，则克隆数组以外的东西
     * @param {Array.<string>} options.exclusion 不包含的属性列表
     * @return {(Object|Array)} 目标对象
     */
    OBJECT.merge = function (target, source, options) {
        options = options || {};
        var overwrite = options.overwrite;
        overwrite == null && (overwrite = true);
        var clone = options.clone;
        clone == null && (clone = true);

        var exclusionMap = makeClusionMap(options.exclusion);

        if (isPlainObject(target) && isPlainObject(source)) {
            doMerge(target, source, overwrite, clone, exclusionMap);
        }
        return target;
    };

    function doMerge(target, source, overwrite, clone, exclusionMap) {
        var s;
        var t;
        
        for (var i in source) {
            s = source[i];
            t = target[i];

            if (!(i in exclusionMap) && source.hasOwnProperty(i)) {
                if (isPlainObject(t) && isPlainObject(s)) {
                    doMerge(t, s, overwrite, clone, exclusionMap);
                } 
                else if (overwrite || !(i in target)) {
                    target[i] = clone && (
                            clone != 'WITHOUT_ARRAY' || !isArray(s)
                        )
                        ? OBJECT.clone(s) 
                        : s;
                }
            }
        }
    }

    /**
     * 类继承
     *
     * @public
     * @param {Function} subClass 子类构造函数
     * @param {Function} superClass 父类
     * @return {Object} 生成的新构造函数的原型
     */
    OBJECT.inherits = function (subClass, superClass) {
        var oldPrototype = subClass.prototype;
        var clazz = new Function();

        clazz.prototype = superClass.prototype;
        OBJECT.extend(subClass.prototype = new clazz(), oldPrototype);
        subClass.prototype.constructor = subClass;
        subClass.superClass = superClass.prototype;

        return subClass.prototype;
    };

    /**
     * 模型继承
     * 生成的构造函数含有父类的构造函数的自动调用
     *
     * @public
     * @param {Function} superClass 父类，如果无父类则为null
     * @param {Function} subClassConstructor 子类的标准构造函数，
     *          如果忽略将直接调用父控件类的构造函数
     * @return {Function} 新类的构造函数
     */
    OBJECT.inheritsObject = function (superClass, subClassConstructor) {
        var agent = function (options) {
                return new agent.client(options);
            }; 
        var client = agent.client = function (options) {
                options = options || {};
                superClass && superClass.client.call(this, options);
                subClassConstructor && subClassConstructor.call(this, options);
            };
            
        superClass && OBJECT.inherits(agent, superClass);
        OBJECT.inherits(client, agent);
        client.agent = agent;

        return agent;
    };

    /**
     * 创建单例
     * 生成的构造函数含有父类的构造函数的自动调用
     *
     * @public
     * @param {Function} superClass 父类，如果无父类则为null
     * @param {Function} subClassConstructor 子类的标准构造函数，
     *          如果忽略将直接调用父控件类的构造函数
     * @return {Function} 新类的构造函数
     */
    OBJECT.createSingleton = function (superClass, subClassConstructor) {
        var instance;
        var agent = function (options) {
                return instance || (instance = new agent.client(options));
            };
        var client = agent.client = function (options) {
                options = options || {};
                superClass && superClass.client.call(this, options);
                subClassConstructor && subClassConstructor.call(this, options);
            };
            
        superClass && OBJECT.inherits(agent, superClass);
        OBJECT.inherits(client, agent);
        client.agent = agent;

        return agent;
    };

    /**
     * 试图判断是否是字面量对象 (@see jquery, tangram)
     * 字面量(literal)对象，简单来讲，
     * 即由{}、new Object()类似方式创建的对象，
     * 而DOM对象，函数对象，Date对象，RegExp对象，
     * 继承/new somefunc()自定义得到的对象都不是字面量对象。
     * 此方法尽力按通常情况排除通非字面量对象，
     * 但是不可能完全排除所有的非字面量对象。
     * 
     * @public
     * @param {Object} obj 输入对象
     * @return {boolean} 是否是字面量对象
     */
    var isPlainObject = OBJECT.isPlainObject = function (obj) {
        
        // 首先必须是Object（特别地，排除DOM元素）
        if (!obj || Object.prototype.toString.call(obj) != '[object Object]'
            // 但是在IE中，DOM元素对上一句话返回true，
            // 所以使用字面量对象的原型上的isPrototypeOf来判断
            || !('isPrototypeOf' in obj)) {
            return false;
        }

        try {
            // 试图排除new somefunc()创建出的对象
            if (// 如果没有constructor肯定是字面量对象
                obj.constructor
                // 有constructor但不在原型上时通过
                && !hasOwnProperty.call(obj, 'constructor') 
                // 用isPrototypeOf判断constructor是否为Object对象本身
                && !hasOwnProperty.call(obj.constructor.prototype, 'isPrototypeOf')
            ) {
                return false;
            }
        } catch ( e ) {
            // IE8,9时，某些情况下访问某些host objects(如window.location)的constructor时，
            // 可能抛异常，@see jquery #9897
            return false;
        }

        // 有一个继承的属性就不算字面量对象，
        // 因原型上的属性会在后面遍历，所以直接检查最后一个
        for (var key in obj) {}
        return key === undefined || hasOwnProperty.call(obj, key);
    };

    /**
     * 是否为数组
     */
    function isArray(o) {
        return objProtoToString.call(o) == '[object Array]';
    }

    /**
     * 是否为function
     */
    function isFunction(o) {
        return objProtoToString.call(o) == '[object Function]';
    }

    /**
     * 是否为Date
     */
    function isDate(o) {
        return objProtoToString.call(o) == '[object Date]';
    }

    /**
     * 做inclusion map, exclusion map
     */
    function makeClusionMap (clusion) {
        var i;
        var clusionMap = {};

        if (isArray(clusion)) {
            for (i = 0; i < clusion.length; i ++) {
                clusionMap[clusion[i]] = clusion[i];
            }
        } 
        else if (clusion === Object(clusion)) { 
            for (i in clusion) {
                clusionMap[clusion[i]] = i;
            }
        }

        return clusionMap;
    }

})();
