/*
Suggest - 定义模拟下拉框行为的基本操作。
下拉框控件，继承自输入控件，实现了选项组接口，扩展了原生 SelectElement 的功能，允许指定下拉选项框的最大选项数量，在屏幕显示不下的时候，会自动显示在下拉框的上方。在没有选项时，下拉选项框有一个选项的高度。下拉框控件允许使用键盘与滚轮操作，在下拉选项框打开时，可以通过回车键或鼠标点击选择，上下键选择选项的当前条目，在关闭下拉选项框后，只要拥有焦点，就可以通过滚轮上下选择选项。

下拉框控件直接HTML初始化的例子:

<div ecui="type:suggest;">

</div>

属性
_nOptionSize  - 下接选择框可以用于选择的条目数量
_cSelected    - 当前选中的选项
_uText        - suggest的文本框
_uOptions     - 下拉选择框
*/
//{if 0}//
;
(function () {

    var core = ecui,
        array = core.array,
        dom = core.dom,
        string = core.string,
        ui = core.ui,
        util = core.util,
        trim = string.trim,
        undefined,
        DOCUMENT = document,
        MATH = Math,
        MAX = MATH.max,
        MIN = MATH.min,

        indexOf = array.indexOf,
        children = dom.children,
        createDom = dom.create,
        getParent = dom.getParent,
        getPosition = dom.getPosition,
        getText = dom.getText,
        insertAfter = dom.insertAfter,
        insertBefore = dom.insertBefore,
        moveElements = dom.moveElements,
        removeDom = dom.remove,
        encodeHTML = string.encodeHTML,
        extend = util.extend,
        getView = util.getView,
        setDefault = util.setDefault,

        $fastCreate = core.$fastCreate,
        getAttributeName = core.getAttributeName,
        getFocused = core.getFocused,
        inheritsControl = core.inherits,
        intercept = core.intercept,
        mask = core.mask,
        restore = core.restore,
        setFocused = core.setFocused,
        triggerEvent = core.triggerEvent,

        UI_INPUT_CONTROL = ui.InputControl,
        UI_INPUT_CONTROL_CLASS = UI_INPUT_CONTROL.prototype,
        UI_BUTTON = ui.Button,
        UI_SCROLLBAR = ui.Scrollbar,
        UI_PANEL = ui.Panel,
        UI_PANEL_CLASS = UI_PANEL.prototype,
        UI_ITEM = ui.Item,
        UI_ITEM_CLASS = UI_ITEM.prototype,
        UI_ITEMS = ui.Items;
//{/if}//
//{if $phase == "define"}//
    ///__gzip_original__UI_SELECT
    ///__gzip_original__UI_SUGGEST_CLASS
    /**
     * 初始化下拉框控件。
     * options 对象支持的属性如下：
     * browser        是否使用浏览器原生的滚动条，默认使用模拟的滚动条
     * optionSize     下拉框最大允许显示的选项数量，默认为10
     * optionsElement 下拉选项主元素
     * @public
     *
     * @param {Object} options 初始化选项
     */
    var UI_SUGGEST = ui.Suggest =
        inheritsControl(
            UI_INPUT_CONTROL,
            'ui-suggest',
            function (el, options) {
                var me = this;
                var name = el.name || options.name || '',
                    type = this.getType(),

                    id = options.id || 'id_notset',
                    optionsEl = createDom(
                        type + '-options' + this.Options.TYPES,
                        'position:absolute;z-index:65535;display:none'
                    );

                optionsEl.setAttribute('ecui_id', id);
                   
                setDefault(options, 'hidden', true);

                
                moveElements(el, optionsEl);

                el.innerHTML =
                '<span class="ui-input"></span><input name="' + name + '" value="' +
                        encodeHTML(options.value || '') + '">';

                el.appendChild(optionsEl);
                //延迟400ms触发query事件
                this._nTimeout = 400;
                //延迟的句柄
                this._nTimeoutHandler = null;
                return el;
            },
            function (el, options) {
                if (options.timeout) {
                    this._nTimeout =  options.timeout;
                }
                if (options.hide) {
                    this.getOuter().style.display = 'none'; 
                }
                el = children(el);
                var me = this;
                //上次输入的值
                this._nLastText = '';
                this._uText = $fastCreate(UI_INPUT_CONTROL, el[0], this, {capturable: false});


                this._uOptions = $fastCreate(
                    this.Options,
                    removeDom(el[2]),
                    this,
                    {hScroll: false, browser: options.browser}
                );

                this.$setBody(this._uOptions.getBody());
                // 初始化下拉区域最多显示的选项数量
                this._nOptionSize = options.optionSize || 10;

                this.$initItems();

                //注册change事件
                this._uText.$change = EVENT_TEXT_CHANGE;
                //取消滚轮事件
                //this._uText.$mousewheel = function() {};
                
            }
        ),
        UI_SUGGEST_CLASS = UI_SUGGEST.prototype,

        /**
         * 初始化下拉框控件的下拉选项框部件。
         * @public
         *
         * @param {Object} options 初始化选项
         */
        UI_SUGGEST_OPTIONS_CLASS = (UI_SUGGEST_CLASS.Options = inheritsControl(UI_PANEL)).prototype,

        /**
         * 初始化下拉框控件的选项部件。
         * @public
         *
         * @param {Object} options 初始化选项
         */
        UI_SUGGEST_ITEM_CLASS =
            (UI_SUGGEST_CLASS.Item = inheritsControl(
                UI_ITEM,
                null,
                null,
                function (el, options) {
                    this._sValue = options.value === undefined ? getText(el) : '' + options.value;
                }
            )).prototype;

    /**
    * 事件处理
    * @event 注册input文本框的onchange事件
    */
    function EVENT_TEXT_CHANGE() {
        var par = this.getParent(); 
        var value = par.getValue(); 
        
        //触发onchange事件
        triggerEvent(par, 'change', value); 
        var txt= par.getText();
        var lastTxt = par._nLastText;
        //trim后没有内容 所以不查找
        if (trim(txt) == '') {
            return; 
        }
        //如果输入不相符 就致空value
        if (trim(txt) != trim(lastTxt)) {

            par._eInput.value = '';
        } 
        //因为改变了文本，理论值不一样了
        //触发onquery事件
        if (par._nTimeoutHandler) {
            //清除之前的句柄
            clearTimeout(par._nTimeoutHandler); 
        }
        //延迟触发onquery事件
        par._nTimeoutHandler = setTimeout(function() {
            //清空事件句柄
            par._nTimeoutHandler = null;      
            triggerEvent(par, 'query', value);
            
        }, par._nTimeout);
            
    }
    /**
    * 填充输入的文字自动匹配value值
    * 用户输入文本以后 寻找相关的id,并填入sugguest框 
    * @param {ecui{Object}} suggest控件本身
    * @param {array[Object]} suggest控件本身
    * @param {string} suggest控件本身的text
    */
    function  AUTO_FILL_VALUE(ele, list, text) {
        list = list || [];
        var value = null;
        for (var i = 0, item; item = list[i++];) {
            if (item.text == text) {
                value = item.value; 
                break;
            }
        
        }
        //渲染
        if (value != null) {
            ele._eInput.value = value;
            //触发onslect
            triggerEvent(ele, 'select', {value: value, text:text}); 
        }
     
    }
//{else}//
    /**
     * 下拉框刷新。
     * @private
     *
     * @param {ecui.ui.Select} control 下拉框控件
     */
    function UI_SUGGEST_FLUSH(control) {
        var options = control._uOptions,
            scrollbar = options.$getSection('VScrollbar'),
            el = options.getOuter(),
            pos = getPosition(control.getOuter()),
            selected = control._cSelected,
            optionTop = pos.top + control.getHeight();

        if (!getParent(el)) {
            // 第一次显示时需要进行下拉选项部分的初始化，将其挂载到 DOM 树中
            DOCUMENT.body.appendChild(el);
            control.cache(false, true);
            control.$alterItems();
        }
        else {

            control.cache(false, true);
            control.$alterItems();
        }

        if (options.isShow()) {
            if (selected) {
                //setFocused(selected);
            }
            scrollbar.setValue(scrollbar.getStep() * indexOf(control.getItems(), selected));

            // 以下使用control代替optionHeight
            control = options.getHeight();

            // 如果浏览器下部高度不够，将显示在控件的上部
            options.setPosition(
                pos.left,
                optionTop + control <= getView().bottom ? optionTop : pos.top - control
            );
        }
    }

    /**
     * 改变下拉框当前选中的项。
     * @private
     * @param {ecui.ui.Select} control 下拉框控件
     * @param {ecui.ui.Select.Item} item 新选中的项
     */
    function UI_SUGGEST_CHANGE_SELECTED(control, item) {
        if (item !== control._cSelected) {

            UI_INPUT_CONTROL_CLASS.setValue.call(control, item ? item._sValue : '');
            var text = item ? item.getBody().innerHTML : '';
            control._uText.setValue(text);
            //FIX: setValue为一个对象
            control._cSelected = item;
            if (control._uOptions.isShow()) {
                setFocused(item);
            }
            //设置选中的值
            if (item && item._sValue) {


                //suggest选择事件
                //选择事件的触发
                //
                triggerEvent(control, 'select', {value: item._sValue, text:text}); 
                triggerEvent(control, item._sValue);
                //设置焦点到最后
                //bugfix ie可能会选择以后会有显示在前边问题
                setFocused(control);
                control.setFocusToEnd();
            }
        }
    }

    extend(UI_SUGGEST_CLASS, UI_ITEMS);

    /**
     * 销毁选项框部件时需要检查是否展开，如果展开需要先关闭。
     * @override
     */
    UI_SUGGEST_OPTIONS_CLASS.$dispose = function () {
        this.hide();
        UI_PANEL_CLASS.$dispose.call(this);
    };

    /**
     * 关闭选项框部件时，需要恢复强制拦截的环境。
     * @override
     */
    UI_SUGGEST_OPTIONS_CLASS.$hide = function () {
        UI_PANEL_CLASS.$hide.call(this);
        mask();
        restore();
    };

    /**
     * 对于下拉框选项，鼠标移入即自动获得焦点。
     * @override
     */
    UI_SUGGEST_ITEM_CLASS.$mouseover = function (event) {
        UI_ITEM_CLASS.$mouseover.call(this, event);
        setFocused(this);
    };

  
    /**
     * 获取选项的值。
     * getValue 方法返回选项控件的值，即选项选中时整个下拉框控件的值。
     * @public
     *
     * @return {string} 选项的值
     */
    UI_SUGGEST_ITEM_CLASS.getValue = function () {
       
        return this._sValue;
    };
    /**
     * 获取选项的值。
     * getText 方法返回选项控件的值，即选项选中时整个下拉框控件的值。
     * @public
     *
     * @return {string} 选项的值
     */
    UI_SUGGEST_ITEM_CLASS.getText = function () {
       
        return this._eBody.innerHTML;
    };
    /**
     * 设置选项的值。
     * setValue 方法设置选项控件的值，即选项选中时整个下拉框控件的值。
     * @public
     *
     * @param {string} value 选项的值
     */
    UI_SUGGEST_ITEM_CLASS.setValue = function (value) {
        var parent = this.getParent();
        this._sValue = value;
        if (parent && this == parent._cSelected) {
            // 当前被选中项的值发生变更需要同步更新控件的值
            UI_INPUT_CONTROL_CLASS.setValue.call(parent, value);
        }
    };

    /**
     * 下拉框控件激活时，显示选项框，产生遮罩层阻止对页面内 DOM 节点的点击，并设置框架进入强制点击拦截状态。
     * @override
     */
    UI_SUGGEST_CLASS.$activate = function (event) {
        if (!(event.getControl() instanceof UI_SCROLLBAR)) {
            UI_INPUT_CONTROL_CLASS.$activate.call(this, event);
            //这里不需要弹出层 ？？？
            return;
            this._uOptions.show();
            // 拦截之后的点击，同时屏蔽所有的控件点击事件
            intercept(this);
            mask(0, 65534);
            UI_SUGGEST_FLUSH(this);
            event.stopPropagation();
        }
    };

    /**
     * 选项控件发生变化的处理。
     * 在 选项组接口 中，选项控件发生添加/移除操作时调用此方法。虚方法，子控件必须实现。
     * @protected
     */
    UI_SUGGEST_CLASS.$alterItems = function () {
        var options = this._uOptions,
            scrollbar = options.$getSection('VScrollbar'),
            optionSize = this._nOptionSize,
            step = this.getBodyHeight(),
            width = this.getWidth(),
            itemLength = this.getItems().length;

        if (getParent(options.getOuter())) {
            // 设置选项框
            scrollbar.setStep(step);

            // 为了设置激活状态样式, 因此必须控制下拉框中的选项必须在滚动条以内
            this.setItemSize(
                width - options.getMinimumWidth() - (itemLength > optionSize ? scrollbar.getWidth() : 0),
                step
            );

            // 设置options框的大小，如果没有元素，至少有一个单位的高度
            options.$$mainHeight = itemLength * step + options.$$bodyHeightRevise;
            options.$setSize(width, (MIN(itemLength, optionSize) || 1) * step + options.getMinimumHeight());
        }
    };

    /**
     * @override
     */
    UI_SUGGEST_CLASS.$cache = function (style, cacheSize) {
        (getParent(this._uOptions.getOuter()) ? UI_ITEMS : UI_INPUT_CONTROL_CLASS)
            .$cache.call(this, style, cacheSize);
        this._uText.cache(false, true);
        this._uOptions.cache(false, true);
    };
    /**
     * 控件在下拉框展开时，需要拦截浏览器的点击事件，如果点击在下拉选项区域，则选中当前项，否则直接隐藏下拉选项框。
     * @override
     */
    UI_SUGGEST_CLASS.$intercept = function (event) {
        //__transform__control_o
        this._uOptions.hide();
        for (var control = event.getControl(); control; control = control.getParent()) {
            if (control instanceof this.Item) {
                if (control != this._cSelected) {
                    // 检查点击是否在当前下拉框的选项上
                    UI_SUGGEST_CHANGE_SELECTED(this, control);
                    //onchange事件需要参数 在这里获取并发送
                    var obj = {
                        text: control.getText(),
                        value: control._sValue
                    };
                    triggerEvent(this, 'change', obj);
                    //设置焦点到最后
                    //bugfix ie可能会选择以后会有显示在前边问题
                    //不需要这里调用了 直接在选中的时候调用
                    //setFocused(this);
                    //this.setFocusToEnd();
                }
                break;
            }
        }
        event.exit();
    };

    /**
     * 接管对上下键与回车/ESC键的处理。
     * @override
     */
    UI_SUGGEST_CLASS.$keydown = UI_SUGGEST_CLASS.$keypress = function (event) {
        UI_INPUT_CONTROL_CLASS['$' + event.type](event);

        var options = this._uOptions,
            scrollbar = options.$getSection('VScrollbar'),
            optionSize = this._nOptionSize,
            which = event.which,
            list = this.getItems(),
            length = list.length,
            focus = getFocused();

        if (this.isFocused()) {

            // 当前不能存在鼠标操作，否则屏蔽按键
            if (which == 40 || which == 38) {
                //bugfix
                //shift + 40 是（  ，shift + 38 是 &
                if (event && event._oNative.shiftKey) {
                    return true; 
                }
                if (length) {

                    if (options.isShow()) {
                        var uFocus = list[which = MIN(MAX(0, indexOf(list, focus) + which - 39), length - 1)];
                        setFocused(uFocus);
                        which -= scrollbar.getValue() / scrollbar.getStep();
                        scrollbar.skip(which < 0 ? which : which >= optionSize ? which - optionSize + 1 : 0);
                        event.cancelBubble = true;
                    }
                    else {
                        //不需要选择列表里的item
                        return false;
                        
                        //this.setSelectedIndex(MIN(MAX(0, indexOf(list, this._cSelected) + which - 39), length - 1));
                    }

                }
                return false;
            }
            else if (which == 27 || which == 13 && options.isShow()) {
                // 回车键选中，ESC键取消
                options.hide();
                if (which == 13) {
                    if (focus instanceof this.Item) {
                        UI_SUGGEST_CHANGE_SELECTED(this, focus);
                        //onchange事件需要参数 在这里获取并发送
                        var obj = {
                            text: focus.getText(),
                            value: focus._sValue
                        };
                        //触发change事件
                        triggerEvent(this, 'change', obj);
                    }
                }
                return false;
            }
            else {
                //可以支持用户继续输入
                //bugfix: 这里有一个bug，keypress和keydown，which可能是0 所以不用处理不然firefox会丢失 item的焦点
                if (which != 0) {
                        
                    setFocused(this._uText); 
                }

            }

        }
        
    };

    /**
     * 如果控件拥有焦点，则当前选中项随滚轮滚动而自动指向前一项或者后一项。
     * @override
     */
    UI_SUGGEST_CLASS.$mousewheel = function (event) {
        if (this.isFocused()) {
            var options = this._uOptions,
                list = this.getItems(),
                length = list.length;

            if (options.isShow()) {
                options.$mousewheel(event);
            }
            else {
                //options表示当前选项的index
                options = indexOf(list, this._cSelected) + (event.detail > 0 ? 1 : -1);
                this.setSelectedIndex(
                    length ?
                        MIN(MAX(0, options), length - 1) : null
                );
                if (options >= 0 && options < length) {
                    //鼠标滚动触发change事件
                    //triggerEvent(this, 'change');
                }
            }

            event.exit();
        }
    };

    /**
     * @override
     */
    UI_SUGGEST_CLASS.$ready = function () {
        this.setValue(this.getValue());
    };

    /**
     * 下拉框移除子选项时，如果选项是否被选中，需要先取消选中。
     * @override
     */
    UI_SUGGEST_CLASS.remove = function (item) {
        if ('number' == typeof item) {
            item = this.getItems()[item];
        }
        if (item == this._cSelected) {
            //UI_SUGGEST_CHANGE_SELECTED(this);
        }
        return UI_ITEMS.remove.call(this, item);
    };

    /**
     * 添加选项需要根据情况继续cache操作
     * @override
     */
    UI_SUGGEST_CLASS.add = function (item, index, options) {
        item = UI_ITEMS.add.call(this, item, index, options);
        if (getParent(this._uOptions.getOuter())) {
            item.cache(true, true);
        }
        return item;
    };

    /**
     * @override
     */
    UI_SUGGEST_CLASS.$setSize = function (width, height) {
        UI_INPUT_CONTROL_CLASS.$setSize.call(this, width, height);
        this.$locate();
        height = this.getBodyHeight();

        // 设置文本区域  不需要减去height
        // bugfix:  并非select 所以不要右侧的下拉箭头
        this._uText.$setSize(width = this.getBodyWidth(), height);
        //this._uText.$setSize(width = this.getBodyWidth() - height, height);

        
    };

    /**
     * 获取被选中的选项控件。
     * @public
     *
     * @return {ecui.ui.Item} 选项控件
     */
    UI_SUGGEST_CLASS.getSelected = function () {
        return this._cSelected || null;
    };
    /**
    * 获取选项的文本
    * @return {string} 选择的文本
    */
    UI_SUGGEST_CLASS.getText = function () {
        var txt = this._uText.getValue(); 
        return txt;
    };

    /**
    * 获取选项的文本
    * @param {string} 设置文本
    * @return {string} 选择的文本
    */
    UI_SUGGEST_CLASS.setText = function (txt) {
        this._uText.setValue(txt); 
    };
    /**
    * 获取suggest的文本框里的值
    */
    UI_SUGGEST_CLASS.getValue = function () {
        var value = this._eInput.value;
        var text = this.getText();
        var obj = {
            value: value,
            text: text
        };
        return obj; 
    };
    /**
     * 设置下拉框允许显示的选项数量。
     * 如果实际选项数量小于这个数量，没有影响，否则将出现垂直滚动条，通过滚动条控制其它选项的显示。
     * @public
     *
     * @param {number} value 显示的选项数量，必须大于 1
     */
    UI_SUGGEST_CLASS.setOptionSize = function (value) {
        this._nOptionSize = value;
        this.$alterItems();
        UI_SUGGEST_FLUSH(this);
    };

    /**
     * 根据序号选中选项。
     * @public
     *
     * @param {number} index 选项的序号
     */
    UI_SUGGEST_CLASS.setSelectedIndex = function (index) {
        UI_SUGGEST_CHANGE_SELECTED(this, this.getItems()[index]);
    };

    /**
     * 设置控件的值。
     * setValue 方法设置控件的值，设置的值必须与一个子选项的值相等，否则将被设置为空，使用 getValue 方法获取设置的值。
     * @public
     *
     * @param {string} value 需要选中的值
     */
    UI_SUGGEST_CLASS.setValue = function (oValue) {

        //{text:XX,value:XX}
        var value = oValue;
        if ('[object Object]' == Object.prototype.toString.call(oValue)) {
            value = oValue.value; 
        }
        
        for (var i = 0, list = this.getItems(), o; o = list[i++]; ) {
            if (o._sValue == value) {
                UI_SUGGEST_CHANGE_SELECTED(this, o);
                //text
                this._nLastText = Ovalue.text || o.getBody().innerHTML;
                return;
            }
        }

        // 找不到满足条件的项，将选中的值清除
        UI_SUGGEST_CHANGE_SELECTED(this);
    };
    /**
    * 清除suggest里的内容
    */
    UI_SUGGEST_CLASS.clear = function () {
        var items = this.getItems() || [],
            len = items.length;
        while ( len-- > 0 ) {
            this.remove(0);
        }

        this._uOptions.reset();
    };
     /**
    * 重新渲染suggest里的内容
    * @param {Array[Object]} 数据源
    */
    UI_SUGGEST_CLASS.update = function (list) {
        //清空    
        this.clear();
        var item = null;
        var el = null;
        var control = this;
        for (var i = 0, o; o = list[i++];) {
          
            item = this.add(o.text, null, {value: o.value});  
            //以后可以增加title的标识的变量
            if (true) {
                item.getOuter().title = o.text;
            }
        }
        if (!this._uOptions.isShow()) {
            this._uOptions.show();
            // 拦截之后的点击，同时屏蔽所有的控件点击事件
            intercept(this);
            mask(0, 65534);
            UI_SUGGEST_FLUSH(this);
        }
        else {
            
            control.$alterItems();
        }
        //自动填充相关id，用户
        var txt = this.getText(txt);
        AUTO_FILL_VALUE(this, list, txt); 
        //updae控件以后需要focus到文本框
        setFocused(control._uText);
        //event.stopPropagation();
    };
    //聚焦到最后
    UI_SUGGEST_CLASS.setFocusToEnd = function() {
        var input = this._uText;  
        core.setFocused(input);
        input = input._eInput;
        var len = input.value.length;
        if (document.selection) {
            var sel = input.createTextRange();
            sel.moveStart('character', len);
            sel.collapse();
            sel.select();
        } 
        else if (typeof input.selectionStart == 'number'
                && typeof input.selectionEnd == 'number') {
            input.selectionStart = input.selectionEnd = len;
        }
        
        
    };
    UI_SUGGEST_CLASS.$mousewheel = function() {};
//{/if}//
//{if 0}//
})();
//{/if}//
