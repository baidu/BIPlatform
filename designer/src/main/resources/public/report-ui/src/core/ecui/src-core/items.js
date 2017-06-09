/*
Item/Items - 定义选项操作相关的基本操作。
选项控件，继承自基础控件，用于弹出菜单、下拉框、交换框等控件的单个选项，通常不直接初始化。选项控件必须用在使用选项组接口(Items)的控件中。
选项组不是控件，是一组对选项进行操作的方法的集合，提供了基本的增/删操作，通过将 ecui.ui.Items 对象下的方法复制到类的 prototype 属性下继承接口，最终对象要正常使用还需要在类构造器中调用 $initItems 方法。
*/
//{if 0}//
(function () {

    var core = ecui,
        array = core.array,
        dom = core.dom,
        string = core.string,
        ui = core.ui,
        util = core.util,

        undefined,

        indexOf = array.indexOf,
        remove = array.remove,
        children = dom.children,
        createDom = dom.create,
        insertBefore = dom.insertBefore,
        trim = string.trim,
        blank = util.blank,
        callSuper = util.callSuper,

        $fastCreate = core.$fastCreate,
        getOptions = core.getOptions,
        inheritsControl = core.inherits,
        triggerEvent = core.triggerEvent,

        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = UI_CONTROL.prototype;
//{/if}//
//{if $phase == "define"}//
    ///__gzip_original__UI_ITEM
    ///__gzip_original__UI_ITEMS
    /**
     * 初始化选项控件。
     * @public
     *
     * @param {string|Object} options 对象
     */
    var UI_ITEM = ui.Item =
        inheritsControl(
            UI_CONTROL,
            'ui-item',
            function (el, options) {
                el.style.overflow = 'hidden';
                if (options.prompt != null) {
                    el.title = options.prompt;
                }
            }
        ),
        UI_ITEM_CLASS = UI_ITEM.prototype,
        UI_ITEMS = ui.Items = {};
//{else}//
    /**
     * 选项控件的点击事件将触发选项组的 onitemclick 事件。
     * @override
     */
    UI_ITEM_CLASS.$click = function (event) {
        UI_CONTROL_CLASS.$click.call(this, event);

        var parent = this.getParent();
        if (parent) {
            triggerEvent(parent, 'itemclick', event, [indexOf(UI_ITEMS[parent.getUID()], this)]);
        }
    };

    /**
     * 选项组只允许添加选项控件，添加成功后会自动调用 $alterItems 方法。
     * @override
     */
    UI_ITEMS.$append = function (child) {
        // 检查待新增的控件是否为选项控件
        if (!(child instanceof (this.Item || UI_ITEM)) || callSuper(this, '$append') === false) {
            return false;
        }
        UI_ITEMS[this.getUID()].push(child);
        this.$alterItems();
    };

    /**
     * @override
     */
    UI_ITEMS.$cache = function (style, cacheSize) {
        callSuper(this, '$cache');

        for (var i = 0, list = UI_ITEMS[this.getUID()], o; o = list[i++]; ) {
            o.cache(true, true);
        }
    };

    /**
     * @override
     */
    UI_ITEMS.$dispose = function () {
        delete UI_ITEMS[this.getUID()];
        callSuper(this, '$dispose');
    };

    /**
     * 初始化选项组对应的内部元素对象。
     * 选项组假设选项的主元素在内部元素中，因此实现了 Items 接口的类在初始化时需要调用 $initItems 方法自动生成选项控件，$initItems 方法内部保证一个控件对象只允许被调用一次，多次的调用无效。
     * @protected
     */
    UI_ITEMS.$initItems = function () {
        // 防止因为选项变化引起重复刷新，以及防止控件进行多次初始化操作
        this.$alterItems = this.$initItems = blank;

        UI_ITEMS[this.getUID()] = [];

        // 初始化选项控件
        for (var i = 0, list = children(this.getBody()), o; o = list[i++]; ) {
            this.add(o);
        }

        delete this.$alterItems;
    };

    /**
     * 选项组移除子选项后会自动调用 $alterItems 方法。
     * @override
     */
    UI_ITEMS.$remove = function (child) {
        callSuper(this, '$remove');
        remove(UI_ITEMS[this.getUID()], child);
        this.$alterItems();
    };

    /**
     * 添加子选项控件。
     * add 方法中如果位置序号不合法，子选项控件将添加在末尾的位置。
     * @public
     *
     * @param {string|HTMLElement|ecui.ui.Item} item 控件的 html 内容/控件对应的主元素对象/选项控件
     * @param {number} index 子选项控件需要添加的位置序号
     * @param {Object} options 子控件初始化选项
     * @return {ecui.ui.Item} 子选项控件
     */
    UI_ITEMS.add = function (item, index, options) {
        var list = UI_ITEMS[this.getUID()],
            o;

        if (item instanceof UI_ITEM) {
            // 选项控件，直接添加
            item.setParent(this);
        }
        else {
            // 根据是字符串还是Element对象选择不同的初始化方式
            if ('string' == typeof item) {
                this.getBody().appendChild(o = createDom());
                o.innerHTML = item;
                item = o;
            }

            o = this.Item || UI_ITEM;
            item.className = trim(item.className) + ' ' + this.getType() + '-item' + o.TYPES;

            options = options || getOptions(item);
            options.parent = this;
            options.select = false;
            list.push(item = $fastCreate(o, item, this, options));
            this.$alterItems();
        }

        // 改变选项控件的位置
        if (item.getParent() && (o = list[index]) && o != item) {
            insertBefore(item.getOuter(), o.getOuter());
            list.splice(index, 0, list.pop());
        }

        return item;
    };

    /**
     * 向选项组最后添加子选项控件。
     * append 方法是 add 方法去掉第二个 index 参数的版本。
     * @public
     *
     * @param {string|Element|ecui.ui.Item} item 控件的 html 内容/控件对应的主元素对象/选项控件
     * @param {Object} 子控件初始化选项
     * @return {ecui.ui.Item} 子选项控件
     */
    UI_ITEMS.append = function (item, options) {
        this.add(item, undefined, options);
    };

    /**
     * 获取全部的子选项控件。
     * @public
     *
     * @return {Array} 子选项控件数组
     */
    UI_ITEMS.getItems = function () {
        return UI_ITEMS[this.getUID()].slice();
    };

    /**
     * @override
     */
    UI_ITEMS.init = function () {
        callSuper(this, 'init');
        this.$alterItems();
    };

    /**
     * 移除子选项控件。
     * @public
     *
     * @param {number|ecui.ui.Item} item 选项控件的位置序号/选项控件
     * @return {ecui.ui.Item} 被移除的子选项控件
     */
    UI_ITEMS.remove = function (item) {
        if ('number' == typeof item) {
            item = UI_ITEMS[this.getUID()][item];
        }
        if (item) {
            item.setParent();
        }
        return item || null;
    };

    /**
     * 设置控件内所有子选项控件的大小。
     * @public
     *
     * @param {number} itemWidth 子选项控件的宽度
     * @param {number} itemHeight 子选项控件的高度
     */
    UI_ITEMS.setItemSize = function (itemWidth, itemHeight) {
        for (var i = 0, list = UI_ITEMS[this.getUID()], o; o = list[i++]; ) {
            o.$setSize(itemWidth, itemHeight);
        }
    };
//{/if}//
//{if 0}//
})();
//{/if}//
