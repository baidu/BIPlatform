/**
 * xutil.LinkedHashMap
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    节点有序的哈希表
 *           为哈希表提供线性表能力，适合管理有唯一性id的数据集合，
 *           做为队列、链表等结构使用
 * @author:  sushuang(sushuang)
 * @depend:  none
 */

/**
 * @usage 
 *    (1) 作为HashMap
 *        var h1 = new LinkedHashMap();
 *        h1.set('name', 'ss');
 *        h1.set('age', 123);
 *        var name = h1.get('name');
 * 
 *    (2) 作为数组、链表（支持环链表，见next、previous方法）、队列
 *        // 从id字段中取值做为HashMap的key
 *        var h2 = new LinkedHashMap(null, 'id'); 
 *        h2.addLast({ id: 23, name: 'ss' });
 *        h2.addFirst({ id: 34, name: 'bbb' });
 *        h2.appendAll(
 *          [
 *              { id: 99, name: 'xx' }, 
 *              { id: 543, name: 'trr' }
 *          ]
 *        );
 *        // 得到{id:23, name: 'ss'}
 *        var data1 = h2.get(23); 
 *        // 得到{id: 23, name: 'ss'}，按index取值
 *        var data2 = h2.getAt(1); 
 *        // 得到{id: 34, name: 'bbb'}
 *        var data3 = h2.first(); 
 *        // 遍历
 *        foreach(function(key, item, index) { ... }) 
 * 
 *    (3) 从list中自动取得key，value初始化
 *        // 如下设置为自动从'id'字段中取值做为HashMap的key，
 *        // 以{id: 55, name: 'aa'}整个为数据项
 *        var h3 = new LinkedHashMap([{ id: 55, name: 'aa' }], 'id');
 *        h3.addLast({ id: 23, name: 'ss' });
 *        // 如下设置为自动从id字段中取值做为HashMap的key，
 *        // 以name字段值做为数据项
 *        var h4 = new LinkedHashMap(null, 'id', 'name');
 *        h4.addLast({ id: 23, name: 'ss' });
 *        h4.addFirst('bb', 24); //同样效果
 */
(function () {

    var namespace = xutil;
    
    /**
     * 构造函数
     * 可构造空LinkedHashMap，也可以list进行初始化
     * 
     * @public
     * @constructor
     * @param {Array.<Object>} list 初始化列表
     *          为null则得到空LinkedHashMap
     * @param {(string|Function)=} defautlKeyAttr 
     *          表示list每个节点的哪个字段要做为HashMap的key，可缺省，
     *          如果为Function：
     *              param {*} list的每个节点
     *              return {*} HashMap的key
     * @param {(string|Function)=} defaultValueAttr 
     *          表示list每个节点的哪个字段要做为HashMap的value，可缺省，
     *          缺省则取list每个节点本身做为HashMap的value
     *          如果为Function：
     *              param {*} list的每个节点
     *              return {*} HashMap的value
     * @return {LinkedHashMap} 返回新实例
     */
    var LINKED_HASH_MAP = namespace.LinkedHashMap = 
            function (list, defautlKeyAttr, defaultValueAttr) {
                this._oMap = {};
                this._oHead = null;
                this._oTail = null;
                this._nLength = 0;
                this.setDefaultAttr(defautlKeyAttr, defaultValueAttr);
                list && this.appendAll(list);
            };
    var LINKED_HASH_MAP_CLASS = LINKED_HASH_MAP.prototype;

    /**
     * 设置defautlKeyAttr和defaultValueAttr
     *
     * @public
     * @param {(string|Function)=} defautlKeyAttr 参见构造函数中描述
     * @param {(string|Function)=} defaultValueAttr 参见构造函数中描述
     */
    LINKED_HASH_MAP_CLASS.setDefaultAttr = function (
        defautlKeyAttr, defaultValueAttr
    ) {
        this._sDefaultKeyAttr = defautlKeyAttr;
        this._sDefaultValueAttr = defaultValueAttr;
    };

    /**
     * 批量在最后追加数据
     * 
     * @public
     * @param {Array} list 要增加的列表
     * @param {(string|Function)=} keyAttr 
     *      表示list每个节点的哪个字段要做为HashMap的key，
     *      缺省则按defautlKeyAttr从list每个节点中取key
     * @param {(string|Function)=} valueAttr 
     *      表示list每个节点的哪个字段要做为HashMap的value，
     *      缺省则按defautlValueAttr从list每个节点中取value，
     *      无defautlValueAttr则取list每个节点本身做为HashMap的value
     * @return {LinkedHashMap} 返回自身
     */
    LINKED_HASH_MAP_CLASS.appendAll = function (list, keyAttr, valueAttr) {
        keyAttr == null && (keyAttr = this._sDefaultKeyAttr);
        if (keyAttr == null) { return this; }
        valueAttr == null && (valueAttr = this._sDefaultValueAttr);

        list = list || [];
        for (var i = 0, len = list.length, item; i < len; i ++) {
            if (!(item = list[i])) { continue; }
            this.addLast(
                this.$retieval(item, valueAttr), 
                this.$retieval(item, keyAttr)
            );
        }
        return this;
    };

    /**
     * 在最后增加
     * 用法一：
     *      my.addLast('asdf', 11)
     *      11为key，'asdf'为value
     * 用法二：
     *      如果这样初始化
     *      var my = new LinkedHashMap(null, 'aa', 'vv');
     *      则可以
     *      my.addLast({ aa: 11, vv: 'asdf' })
     *      自动提取11做为key，'asdf'做为value
     * 用法三：
     *      如果这样初始化
     *      var my = new LinkedHashMap(null, 'aa');
     *      则可以
     *      my.addLast({ aa: 11, vv: 'asdf' })
     *      自动提取11做为key，{ aa: 11, vv: 'asdf' }做为value
     * 传两个参数则表示用法一，
     * 传一个参数则表示用法二、三（即不传key参数）
     *
     * @public
     * @param {(*|Object)} item 增加的数据
     * @param {string=} key HashMap的关键字
     * @return {LinkedHashMap} 返回自身
     */
    LINKED_HASH_MAP_CLASS.addLast = function (item, key) {
        if (key == null) {
            // 用法一
            key = this.$retieval(item, this._sDefaultKeyAttr);
            item = this.$retieval(item, this._sDefaultValueAttr);
        }

        var node = { key: key, item: item, pre: null, next: null }; 
        this._oMap[key] = node;
        this.$insert(node, this._oTail, null);
        return this;
    };

    /**
     * 在最前增加
     * 用法一：
     *      my.addFirst('asdf', 11)
     *      11为key，'asdf'为value
     * 用法二：
     *      如果这样初始化
     *      var my = new LinkedHashMap(null, 'aa', 'vv');
     *      则可以
     *      my.addFirst({ aa: 11, vv: 'asdf' })
     *      自动提取11做为key，'asdf'做为value
     * 用法三：
     *      如果这样初始化
     *      var my = new LinkedHashMap(null, 'aa');
     *      则可以
     *      my.addFirst({ aa: 11, vv: 'asdf' })
     *      自动提取11做为key，{ aa: 11, vv: 'asdf' }做为value
     * 传两个参数则表示用法一，
     * 传一个参数则表示用法二、三（即不传key参数）
     *
     * @public
     * @param {(*|Object)} item 增加的数据
     * @param {string=} key HashMap的关键字
     * @return {LinkedHashMap} 返回自身
     */
    LINKED_HASH_MAP_CLASS.addFirst = function (item, key) {
        if (key == null) {
            // 用法一
            key = this.$retieval(item, this._sDefaultKeyAttr);
            item = this.$retieval(item, this._sDefaultValueAttr);
        }

        var node = { key: key, item: item, pre: null, next: null };
        this._oMap[key] = node;
        this.$insert(node, null, this._oHead);
        return this;
    };

    /**
     * 在某项前插入
     * 用法一：
     *      my.insertBefore('asdf', 11, 333)
     *      11为key，'asdf'为value，333为插入位置refKey  
     * 用法二：
     *      如果这样初始化
     *      var my = new LinkedHashMap(null, 'aa', 'vv');
     *      则可以
     *      my.insertBefore({ aa: 11, vv: 'asdf' }, 333)
     *      自动提取11做为key，'asdf'做为value
     * 用法三：
     *      如果这样初始化
     *      var my = new LinkedHashMap(null, 'aa');
     *      则可以
     *      my.insertBefore({ aa: 11, vv: 'asdf' }, 333)
     *      自动提取11做为key，{ aa: 11, vv: 'asdf' }做为value
     * 传三个参数则表示用法一，
     * 传两个参数则表示用法二、三（即不传key参数）
     *
     * @public
     * @param {(*|Object)} item 增加的数据
     * @param {string=} key item对应的HashMap的关键字
     * @param {string} refKey 在refKey项前插入
     * @return {LinkedHashMap} 返回自身
     */
    LINKED_HASH_MAP_CLASS.insertBefore = function () {
        var item;
        var key;
        var refKey;
        var arg = arguments;
        if (arg.length == 2) {
            // 用法二、三
            item = this.$retieval(arg[0], this._sDefaultValueAttr);
            key = this.$retieval(arg[0], this._sDefaultKeyAttr);
            refKey = arg[1];
        }
        else {
            // 用法一
            item = arg[0];
            key = arg[1];
            refKey = arg[2];
        }        

        var refNode = this._oMap[refKey];
        var node = { key: key, item: item, pre: null, next: null };
        if (refNode) {
            this._oMap[key] = node;
            this.$insert(node, refNode.pre, refNode);
        }
        return this;
    };

    /**
     * 在某项后插入
     * 用法一：
     *      my.insertAfter('asdf', 11, 333)
     *      11为key，'asdf'为value，333为插入位置refKey  
     * 用法二：
     *      如果这样初始化
     *      var my = new LinkedHashMap(null, 'aa', 'vv');
     *      则可以
     *      my.insertAfter({ aa: 11, vv: 'asdf' }, 333)
     *      自动提取11做为key，'asdf'做为value
     * 用法三：
     *      如果这样初始化
     *      var my = new LinkedHashMap(null, 'aa');
     *      则可以
     *      my.insertAfter({ aa: 11, vv: 'asdf' }, 333)
     *      自动提取11做为key，{ aa: 11, vv: 'asdf' }做为value
     * 传三个参数则表示用法一，
     * 传两个参数则表示用法二、三（即不传key参数）
     * 
     * @public
     * @param {(*|Object)} item 增加的数据
     * @param {string=} key item对应的HashMap的关键字，
     * @param {string} refKey 在refKey项后插入
     * @return {LinkedHashMap} 返回自身
     */
    LINKED_HASH_MAP_CLASS.insertAfter = function () {
        var item;
        var key;
        var refKey;
        var arg = arguments;
        if (arg.length == 2) {
            // 用法二、三
            item = this.$retieval(arg[0], this._sDefaultValueAttr);
            key = this.$retieval(arg[0], this._sDefaultKeyAttr);
            refKey = arg[1];
        }
        else {
            // 用法一
            item = arg[0];
            key = arg[1];
            refKey = arg[2];
        }

        var refNode = this._oMap[refKey];
        var node = { key: key, item: item, pre: null, next: null };
        if (refNode) {
            this._oMap[key] = node;
            this.$insert(node, refNode, refNode.next);
        }
        return this;
    };

    /**
     * 在某位置插入
     * 用法一：
     *      my.insertAt('asdf', 11, 0)
     *      11为key，'asdf'为value，0为插入位置index     
     * 用法二：
     *      如果这样初始化
     *      var my = new LinkedHashMap(null, 'aa', 'vv');
     *      则可以
     *      my.insertAt({ aa: 11, vv: 'asdf' }, 0)
     *      自动提取11做为key，'asdf'做为value
     * 用法三：
     *      如果这样初始化
     *      var my = new LinkedHashMap(null, 'aa');
     *      则可以
     *      my.insertAt({ aa: 11, vv: 'asdf' }, 0)
     *      自动提取11做为key，{ aa: 11, vv: 'asdf' }做为value
     * 传三个参数则表示用法一，
     * 传两个参数则表示用法二、三（即不传key参数）
     *
     * @public
     * @param {(*|Object)} item 增加的数据
     * @param {string=} key item对应的HashMap的关键字
     * @param {Object} index 插入位置，从0开始
     * @return {LinkedHashMap} 返回自身
     */
    LINKED_HASH_MAP_CLASS.insertAt = function () {
        var item;
        var key;
        var index;
        var arg = arguments;
        if (arg.length == 2) {
            // 用法二、三
            item = this.$retieval(arg[0], this._sDefaultValueAttr);
            key = this.$retieval(arg[0], this._sDefaultKeyAttr);
            index = arg[1];
        }
        else {
            // 用法一
            item = arg[0];
            key = arg[1];
            index = arg[2];
        }

        if (index != null && index == this.size()) {
            this.addLast(item, key);
        }
        else {
            var ref = this.getAt(index);
            if (ref && ref.key != null) {
                this.insertBefore(item, key, ref.key);
            }
        }
        return this;
    };

    /**
     * 全部清除LinkedHashMap内容
     *
     * @public
     */
    LINKED_HASH_MAP_CLASS.clean = function () {
        this._oMap = {};
        this._oHead = null;
        this._oTail = null;
        this._nLength = 0;
        this._sDefaultKeyAttr = null;
        this._sDefaultValueAttr = null;
    };

    /**
     * 清除LinkedHashMap内容，但是不清除defaultKeyAttr和defaultValueAttr
     *
     * @public
     */
    LINKED_HASH_MAP_CLASS.cleanWithoutDefaultAttr = function () {
        this._oMap = {};
        this._oHead = null;
        this._oTail = null;
        this._nLength = 0;
    };

    /**
     * 设置数据
     * 用法一：
     *      my.set(11, 'asdf')
     *      11为key，'asdf'为value
     * 用法二：
     *      如果这样初始化
     *      var my = new LinkedHashMap(null, 'aa', 'vv');
     *      则可以
     *      my.set({ aa: 11, vv: 'asdf' })
     *      自动提取11做为key，'asdf'做为value
     * 用法三：
     *      如果这样初始化
     *      var my = new LinkedHashMap(null, 'aa');
     *      则可以
     *      my.set({ aa: 11, vv: 'asdf' })
     *      自动提取11做为key，{ aa: 11, vv: 'asdf' }做为value
     * 传两个参数则表示用法一，
     * 传一个参数则表示用法二、三（即不传key参数）
     * 
     * @public
     * @param {Object=} key item对应的HashMap的关键字
     * @param {(*|Object)} item 增加的数据
     * @return {LinkedHashMap} 返回自身
     */
    LINKED_HASH_MAP_CLASS.set = function () {
        var key;
        var item;
        var arg = arguments;
        if (arg.length == 1) {
            // 用法二、三
            item = arg[0];
        } 
        else {
            // 用法一
            key = arg[0];
            item = arg[1];
        }

        // 如果已存在
        var node = this._oMap[key]
        if (node) {
            node.item = item;
        }
        // 新建
        else {
            this.addLast(item, key);
        }
        return this;
    };
    
    /**
     * 取得数据
     * 
     * @public
     * @param {Object} key item对应的HashMap的关键字
     * @return {*} 取得的数据，未取到则返回null
     */
    LINKED_HASH_MAP_CLASS.get = function (key) {
        var node = this._oMap[key];
        return node ? node.item : null;
    };
    
    /**
     * 按index取得数据
     * 
     * @public
     * @param {Object} index 序号，从0开始
     * @return {Object} ret 取得的数据，
     *              例如：
     *              { key:'321', value: { id: '321', name: 'ss' } }，
     *              未取到则返回null
     * @return {number} ret.key HashMap的key
     * @return {*} ret.item 数据本身
     */
    LINKED_HASH_MAP_CLASS.getAt = function (index) {
        var ret = {};
        this.foreach(function (key, item, i) {
            if (index == i) {
                ret.key = key;
                ret.item = item;
                return false;
            }
        });
        return ret.key != null ? ret : null;
    };

    /**
     * 按key得到index
     * 
     * @public
     * @param {Object} key item对应的HashMap的关键字
     * @param {number} index 序号，从0开始，如果未找到，返回-1
     */
    LINKED_HASH_MAP_CLASS.getIndex = function (key) {
        var index = -1;
        this.foreach(function (k, item, i) {
            if (k == key) {
                index = i;
                return false;
            }
        });
        return index;
    };
    
    /**
     * 根据内容遍历，获取key
     * 
     * @public
     * @param {Object} item 内容
     * @param {Object} key item对应的HashMap的关键字
     */
    LINKED_HASH_MAP_CLASS.getKey = function (item) {
        var key;
        this.foreach(function (k, o, i) {
            if (o.item == item) {
                key = k;
                return false;   
            }
        });
        return key;
    };

    /**
     * 是否包含
     * 
     * @public
     * @param {Object} key item对应的HashMap的关键字
     * @return {boolean} 是否包含
     */
    LINKED_HASH_MAP_CLASS.containsKey = function (key) {
        return !!this.get(key);
    };

    /**
     * 将所有数据以Array形式返回
     * 
     * @public
     * @return {Array} 所有数据
     */
    LINKED_HASH_MAP_CLASS.list = function () {
        var ret = [];
        this.foreach(function (key, item) { ret.push(item); });
        return ret;
    };

    /**
     * 从链表首顺序遍历
     * 
     * @public
     * @param {Function} visitFunc 每个节点的访问函数
     *          param {string} key 每项的key
     *          param {*} item 每项
     *          param {number} index 遍历的计数
     *          return {boolan} 如果返回为false，则不再继续遍历
     */
    LINKED_HASH_MAP_CLASS.foreach = function (visitFunc) {
        var node = this._oHead;
        var i = 0;
        var goOn = true;
        while (node) {
            if (visitFunc(node.key, node.item, i++) === false) { 
                break; 
            }
            node = node.next;
        }
    };

    /**
     * 删除key对应的项
     * 
     * @public
     * @param {Object} key item对应的HashMap的关键字
     * @return {*} 被删除的项
     */
    LINKED_HASH_MAP_CLASS.remove = function (key) {
        var node = this._oMap[key];
        if (node) {
            delete this._oMap[key];
            var preNode = node.pre;
            var nextNode = node.next;
            preNode && (preNode.next = nextNode);
            nextNode && (nextNode.pre = preNode);
            this._nLength --; 
            (this._oHead == node) && (this._oHead = nextNode); 
            (this._oTail == node) && (this._oTail = preNode);
        }
        return node ? node.item : null;
    };

    /**
     * 得到LinkedHashMap大小
     * 
     * @public
     * @return {number} LinkedHashMap大小
     */
    LINKED_HASH_MAP_CLASS.size = function () {
        return this._nLength;
    };

    /**
     * 得到第一个数据
     * 
     * @public
     * @return {*} 第一个数据
     */
    LINKED_HASH_MAP_CLASS.first = function () {
        return this._oHead ? this._oHead.item : null;
    };
    
    /**
     * 得到第一个key
     * 
     * @public
     * @return {string} 第一个key
     */
    LINKED_HASH_MAP_CLASS.firstKey = function () {
        return this._oHead ? this._oHead.key : null;
    };

    /**
     * 得到最后一个数据
     * 
     * @public
     * @return {*} 最后一个数据
     */
    LINKED_HASH_MAP_CLASS.last = function () {
        return this._oTail ? this._oTail.item : null;
    };
    
    /**
     * 得到最后一个key
     * 
     * @public
     * @return {string} 最后一个key
     */
    LINKED_HASH_MAP_CLASS.lastKey = function () {
        return this._oTail ? this._oTail.key : null;
    };

    
    /**
     * 得到key对应的下一个项，未取到则返回null
     * 
     * @public
     * @param {Object} key item对应的HashMap的关键字
     * @param {boolean=} circular 如果到链尾，是否循环到链首，默认为false
     * @return {*} 取得的数据
     */
    LINKED_HASH_MAP_CLASS.next = function (key, circular) {
        var node = this.$next(key, circular);
        return node ? node.item : null;
    };
    
    /**
     * 得到key对应的下一个key，未取到则返回null
     * 
     * @public
     * @param {Object} key item对应的HashMap的关键字
     * @param {boolean=} circular 如果到链尾，是否循环到链首，默认为false
     * @return {string} 取得的key
     */
    LINKED_HASH_MAP_CLASS.nextKey = function (key, circular) {
        var node = this.$next(key, circular);
        return node ? node.key : null;
    };
    

    /**
     * 得到key对应的上一个项，未取到则返回null
     * 
     * @public
     * @param {Object} key item对应的HashMap的关键字
     * @param {boolean=} circular 如果到链尾，是否循环到链首，默认为false
     * @return {*} 取得的数据
     */
    LINKED_HASH_MAP_CLASS.previous = function (key, circular) {
        var node = this.$previous(key, circular);
        return node ? node.item : null;
    };
    
    /**
     * 得到key对应的上一个key，未取到则返回null
     * 
     * @public
     * @param {Object} key item对应的HashMap的关键字
     * @param {boolean=} circular 如果到链尾，是否循环到链首，默认为false
     * @return {string} 取得的key
     */
    LINKED_HASH_MAP_CLASS.previousKey = function (key, circular) {
        var node = this.$previous(key, circular);
        return node ? node.key : null;
    };
    
    /**
     * @protected
     */
    LINKED_HASH_MAP_CLASS.$next = function (key, circular) {
        var node = this._oMap[key];
        if (!node) { return null; }
        var next = (circular && node == this._oTail) 
                ? this._oHead : node.next;
        return next;
    };
    
    /**
     * @protected
     */
    LINKED_HASH_MAP_CLASS.$previous = function (key, circular) {
        var node = this._oMap[key];
        if (!node) { return null; }
        var pre = (circular && node == this._oHead) 
                ? this._oTail : node.pre;
        return pre;
    };
    
    /**
     * @protected
     */
    LINKED_HASH_MAP_CLASS.$retieval = function (item, attr) {
        var k;
        if (Object.prototype.toString.call(attr) == '[object Function]') {
            k = attr(item);
        } 
        else if (attr == null) {
            k = item;
        } 
        else {
            k = item[attr];
        }
        return (k === void 0) ? null : k;
    };   

    /**
     * @protected
     */
    LINKED_HASH_MAP_CLASS.$insert = function (node, preNode, nextNode) {
        node.pre = preNode;
        node.next = nextNode;
        preNode ? (preNode.next = node) : (this._oHead = node);
        nextNode ? (nextNode.pre = node) : (this._oTail = node);
        this._nLength ++;
    };
    
})();
