/**
 * custom-table.js
 * Copyright 2012 Baidu Inc. All rights reserved *
 * desc: 工作台项目定制的table控件，提供的功能包括表头锁定和列锁定、行选中、排序、使用render方法填充和刷新表格；表格支持跨行跨列,最多跨两行
 * author: hades(denghongqi)
 */

 (function () {
    var core = ecui,
        dom = core.dom,
        array = core.array,
        ui = core.ui,
        string = core.string,
        util = core.util,

        $fastCreate = core.$fastCreate,
        inheritsControl = core.inherits,
        triggerEvent = core.triggerEvent,
        disposeControl = core.dispose,
        $disposeControl = core.$dispose,
        createDom = dom.create,
        hasClass = dom.hasClass,
        first = dom.first,
        last = dom.last,
        children = dom.children,
        addClass = dom.addClass,
        setStyle = dom.setStyle,
        setText = dom.setText,
        getText = dom.getText,
        removeClass = dom.removeClass,
        toNumber = util.toNumber,
        getParent = dom.getParent,
        moveElements = dom.moveElements,
        getAttribute = dom.getAttribute,
        getPosition = dom.getPosition,
        encodeHTML = xutil.string.encodeHTML,
        remove = array.remove,
        getView = util.getView,
        extend = util.extend,
        repaint = core.repaint,
        attachEvent = util.attachEvent,
        detachEvent = util.detachEvent,

        // 引入了外部库
        formatNumber = xutil.number.formatNumber;

        MATH = Math,
        MIN = MATH.min,
        WINDOW = window,

        UI_LOCKED_TABLE = ui.SlowLockedTable,
        UI_LOCKED_TABLE_CLASS = UI_LOCKED_TABLE.prototype;

    var UI_CUSTOM_TABLE = ui.CustomTable =
        inheritsControl(
            UI_LOCKED_TABLE,
            'ui-table',
            function(el, options) {
                this._oOptions = options;
                this._aHeader = options.header;
                this._sSortby = options.sortby;
                this._sOrderby = options.orderby;
                if (!options.datasource) {
                    this._nLeftLock = options.leftLock || 0;
                    this._nRightLock = options.rightLock || 0;
                }

                var type = this.getTypes()[0];

                // 行选择记录
                this._oRowCheck = {
                    rowCheckMode: options.rowCheckMode,
                    rowChecked: options.rowChecked || [],
                    rowCheckMax: options.rowCheckMax || Number.MAX_VALUE,
                    rowCheckMin: options.rowCheckMin || Number.MIN_VALUE,
                    rowCheckCount: 0
                };
                // 如果行内有selected标志，优先
                for (var i = 0, o; o = (options.datasource || [])[i]; i ++) {
                    o.selected && this._oRowCheck.rowChecked.push(i);
                }

                var html = [];
                html.push('<table><thead>');

                options.leftLock = options.leftLock || 0;
                options.rightLock = options.rightLock || 0;
                var lockedTotal = options.leftLock + options.rightLock;

                if (!options.datasource) {
                    setStyle(el, 'width', '100%');
                }
                else {
                    setStyle(el, 'width', 'auto');
                    setStyle(el, 'display', 'block');
                }

                if (!options.datasource) {
                    html.push('<tr>');
                    var i;
                    for (var i = 0; i <= lockedTotal; i++) {
                        html.push('<th></th>');
                    }
                    html.push('</tr>');
                }
                else {
                    //表头目前只支持跨两行
                    if ('[object Array]' == Object.prototype.toString.call(options.fields[0])) {
                        var flag = 0;    
                        var i;
                        for (i = 0; i < options.fields.length; i++) {
                            var o = options.fields[i];
                            html.push(createHeadRow(o, this, options.fields));
                        }
                        this._aColumns = [];
                        for (var i = 0, o; o = options.fields[0][i]; i++) {
                            if (o.colspan) {
                                for (var j = 0; j < o.colspan; j++) {
                                    this._aColumns.push(extend({}, options.fields[1][flag++]));
                                }
                            }
                            else {
                                this._aColumns.push(extend({}, o));
                            }
                        }
                    }
                    else {
                        html.push(createHeadRow(options.fields, this));
                        this._aColumns = copyArray(options.fields);
                    }
                }

                html.push('</thead><tbody>');

                if(!options.datasource)  {
                    html.push('<tr>');
                    var i;
                    html.push('<td></td>');
                    html.push('</tr>');
                    options.leftLock = 0;
                    options.rightLock = 0;
                }
                else {
                    this._aData = options.datasource;

                    if (!this._aData.length) {
                        html.push('<tr>');
                        html.push(
                            '<td class="' + type + '-empty-cell'
                            + '" align="middle" colspan="'
                            + this._aColumns.length
                            + '">'
                        );
                        html.push(
                            options.errorMsg 
                            ? options.errorMsg
                            : '暂无数据，请稍后再试'
                        );
                        html.push('</td>');
                        html.push('</tr>');
                    }
                    else {
                        var i;
                        for (i = 0; i < options.datasource.length; i++) {
                            var item = options.datasource[i];
                            html.push('<tr>');
                            var j;
                            for (j = 0; j < this._aColumns.length; j++) {
                                var o = this._aColumns[j];
                                var align = o.align || 'left';
                                html.push('<td data-content="1" data-cell-pos="' + j + '-' + i + '" class="ui-table-ccell ');

                                html.push(
                                    'ui-table-cell-align-' + align + '"'
                                );

                                html.push('>');

                                var content = o.content || o.field;

                                if (typeof content == 'function') {
                                    var e = content.call(null, item, i);
                                    if (Object.prototype.toString.call(e) == '[object String]') {
                                        /*
                                        if (o.autoEllipsis) {
                                            html.push('<span title="');
                                            html.push()
                                        }
                                        */
                                    	 /*
                                        if (o.maxlength 
                                            && e
                                            && e.length > o.maxlength
                                        ) {
                                            html.push('<span class="');
                                            html.push(type + '-cell-limited"');
                                            html.push(' title="' + e + '">');
                                            html.push(encodeHTML(e.substring(0, o.maxlength)));
                                            html.push('...');
                                            html.push('</span>');
                                        }
                                        else {
                                            html.push(e);
                                        }
                                        */
                            			//update by lizhantong 2014-04-04 19:24:10
                                    	//td全部加上title
                                    	html.push('<span ');
       									html.push(' title="' + encodeHTML(e) + '">');
       									html.push(encodeHTML(e));
       									html.push('</span>');
                                    }
                                    else {
                                        var div = createDom();
                                        div.appendChild(e);
                                        html.push(div.innerHTML);
                                    }
                                }
                                else {
                                    if (o.checkbox) {
                                        html.push('<input type="checkbox"');
                                        html.push(
                                            ' class="' + type + '-checkbox"'
                                        );
                                        html.push(
                                            ' data-rownum="' + i + '"'
                                        );
                                        html.push(' />');
                                    }
                                    else {
                                        var vc = item[content];
                                        // add by majun 2014-3-20 15:05:15
                                        // 在平面报表中，如果表格里面内容为空，则显示为“-”。
                                        if(vc == null || vc == ''){
                                            vc = '-';
                                        }else if (o.format) {
                                            vc = formatNumber(vc, o.format, void 0, void 0, true);
                                        }
                                        /*
                                        if (o.maxlength 
                                            && vc
                                            && vc.length > o.maxlength
                                        ) {
                                            html.push('<span class="');
                                            html.push(type + '-cell-limited"');
                                            html.push(' title="' + encodeHTML(vc) + '">');
                                            html.push(encodeHTML(vc.substring(0, o.maxlength)));
                                            html.push('...');
                                            html.push('</span>');
                                        }
                                        else {
                                            html.push(encodeHTML(vc));
                                        }
                                        */
                                        //update by lizhantong 2014-04-04 19:26:10
                                    	//td全部加上title
                                    	//html.push('<span ');
                                    	html.push('<span class="');
                                        html.push(type + '-cell-limited"');
       									html.push(' title="' + encodeHTML(vc) + '">');
       									html.push(encodeHTML(vc));
       									html.push('</span>');
                                    }

                                    if (o.detail) {
                                        html.push('<span ecui="type:tip;asyn:true;id:');
                                        html.push('tip-' + item[o.idField] + '"');
                                    }
                                }

                                html.push('</td>');
                            }
                            html.push('</tr>');
                        }
                    }
                }

                html.push('</tbody></table>');

                el.innerHTML = html.join('');

                return el;

            },
            function(el, options) {
                //ecui.init(el);
                if (options.fields && options.datasource) {
                    initEmbedControlEvent(options.fields, options.datasource);
                }

                this.$bindCheckbox();
                return el;
            }
        ),
        UI_CUSTOM_TABLE_CLASS = UI_CUSTOM_TABLE.prototype,

        UI_CUSTOM_TABLE_CELL_CLASS = (
            UI_CUSTOM_TABLE_CLASS.Cell = inheritsControl(
                UI_LOCKED_TABLE_CLASS.Cell,
                null,
                function (el, options) {
                    options.primary = 'ui-table-cell';
                }
            )
        ).prototype,

        DELEGATE_EVENTS = ['click', 'mouseup', 'mousedown'],

        // 默认处理函数
        DEFAULT_EVENTS = {

            'click div.ui-table-hcell-sort-def': function (event, control) {
                // var field = this.getAttribute('data-field'),
                var oTh = dom.getParent(dom.getParent(this));
                var field = oTh.getAttribute('data-field');
                var id = oTh.getAttribute('data-id');
                var orderby;

                if (this.className.indexOf('-sort-desc') >= 0) {
                    orderby = 'asc';
                }
                else if (this.className.indexOf('-sort-asc') >= 0) {
                    orderby = 'desc';
                }
                else {
                    orderby = this.getAttribute('data-orderby') || 'desc';
                }

                triggerEvent(control, 'sort', null, [field, orderby.toUpperCase(), id]);
            },
            'click div.ui-table-hcell-field-set': function (event, control) {
                var oTh = dom.getParent(dom.getParent(this));
                var field = oTh.getAttribute('data-field');
                var id = oTh.getAttribute('data-id');
                var text = oTh.getAttribute('data-title');
                var isMeasure = oTh.getAttribute('data-measure');
                triggerEvent(control, 'fieldset', null, [id, field, text, isMeasure]);
            },
            'click input.ui-table-checkbox-all': function (event, control) {
                control.$refreshCheckbox(this.checked);
            },
            'click input.ui-table-checkbox': function (event, control) {
                control.$refreshCheckbox();
            }
        };      

    /** 
     * 生成表头的一行
     * 
     * @param {Array} headrow 一行表头的数据
     * @param {ecui.ui.CustomTable} con
     * @param {Array} opt_head 所有的表头数据
     * @return {string} html片段
     */
    function createHeadRow(headrow, con, opt_head) {
        var type = con.getTypes()[0];

        var html = [];
        html.push('<tr>');

        var flag = 0;
        var i = 0;
        for (i = 0; i < headrow.length; i++) {
            var o = headrow[i];

            html.push('<th ');
            html.push('data-field="');

            if (Object.prototype.toString.call(o.field) == '[object String]') {
                html.push(o.field, '" ');
            }

            if (Object.prototype.toString.call(o.id) == '[object String]') {
                html.push('data-id="');
                html.push(o.id, '" ');
            }
            if (o.title) {
                html.push('data-title="');
                html.push(o.title, '" ');
            }

            if (o.width) {
                html.push(
                    '" style="width:' + o.width + 'px;'
                    + 'min-width:' + o.width + 'px'
                );
            }

            if (o.rowspan) {
                html.push(
                    '" rowspan="' + o.rowspan
                );
            }
            if (o.colspan) {
                html.push(
                    '" colspan="' + o.colspan
                );

                var j;
                var width = 0;
                for (j = flag; j < flag + o.colspan; j++) {
                    width += opt_head[1][j].width;
                }

                html.push(
                    '" width="' + width
                );
                html.push('"');

                flag += o.colspan;
            }
            var classStr = ' class="';
            var attrStr = [];
            var align = o.align || 'left';
            classStr = classStr + type + '-cell-align-' + align + '" ';

//            if (o.sortable) {
//                classStr = classStr + type + '-hcell-sort ';
//                if (o.field && o.field == con._sSortby) {
//                    classStr = classStr + type + '-hcell-sort-' + con._sOrderby + ' "';
//                }
//                if (o.order) {
//                    html.push(
//                        ' data-orderby="' + o.order + '"'
//                    );
//                }
//            }
            if (o.orderby) {
                attrStr.push('data-orderby="' + o.orderby + '" ');
            }
            if (o.isMeasure) {
                attrStr.push('data-measure="' + o.isMeasure + '" ');
            }
            html.push(attrStr.join(''), classStr);
            html.push('>');

//            if (o.title) {
//                 //html.push(o.title);
//                 //如果是ie8以下版本，需要在innerCell外面套一层div，设置表头的margin属性，
//                //不然文本过多的话会显示不全
//                var useBag = dom.ieVersion < 8;
//                var isLastColumn = i == headrow.length - 1;
//                html.push(
//                        useBag ? ('<div class="ui-plane-table-hcell-bag ') : '',
//                        useBag && isLastColumn ? ('ui-plane-table-hcell-bag-lastcolumn') : '',
//                        useBag ? ('">') : '',
//                            o.title,
//                        useBag ? '</div>' : ''
//                );
//            }
//            if (o.title) {
//                html.push(
////                    '<div class="ui-table-head-th-content"><div class="ui-table-head-font">',
//                    '<span><span class="', type, '-hcell-content">',
//                    o.title,
//                    '</span>',
//                    '<span class="', type, '-hcell-field-set"></span></span>'
////                    '</div><div class="ui-table-hcell-sort-none"></div></div>'
//                );
//            }

            if (o.title) {
                var tipsStr = '<div class="'+ type + '-head-tips"';
                var sortStr = '';
                var filterStr = '';
                if (o.showFilter) {
                    filterStr = '<div class="' + type + '-hcell-field-set"></div>';
                }
                if (o.toolTip) {
                    tipsStr = tipsStr + 'title="' + o.toolTip + '"';
                }
                if (o.orderby) {
                    sortStr = '<div class="' + type + '-hcell-sort-' + o.orderby + ' ' + type + '-hcell-sort-def"></div>';
                }
                tipsStr = tipsStr + '">&nbsp;</div>';

                html.push(
                    '<div class="', type, '-head-th-content">',
                        '<div class="ui-table-head-font">', o.title, '</div>',
                        sortStr,
                        tipsStr,
                        filterStr,
                    '</div>'
                );
            }

            if (o.checkbox) {
                html.push(
                    '<input type="checkbox" class="'
                    + type + '-checkbox-all"'
                    + ' />'
                );
            }

            if (o.tip && o.tip.length) {
                html.push('<span ecui="type:tip; id:tip-');
                html.push(o.field);
                html.push('; message:');
                html.push(o.tip);
                html.push('"></span>');
            }

            html.push('</th>');
        }
        html.push('</tr>');

        return html.join('');
    }

    /**
     * 帮顶表格内部子控件的事件
     *
     * @param {Array} header 表头数据
     * @param {Array} datasource 表格数据
     */
    function initEmbedControlEvent(header, datasource) {
        var i = 0;
        for (i = 0; i < datasource.length; i++) {
            var item = datasource[i];
            for (var j = 0; j < header.length; j++) {
                var o = header[j];
                if (o.detail) {
                    var controlId = 'tip-' + item[o.idField];
                    if (ecui.get(controlId)) {
                        ecui.get(controlId).onloadData = (function (item, o) {
                            return function (handler) {
                                o.loadData(item, handler);
                            }
                        }) (item, o);
                    }
                }
            }
        }
    }

    UI_CUSTOM_TABLE_CLASS.getData = function () {
        return this._aData;
    };

    UI_CUSTOM_TABLE_CLASS.$createHeadRow = function (headrow) {
        var type = this.getTypes()[0];

        var tr = createDom('', '', 'tr');

        for (var i = 0, o; o = headrow[i]; i++) {
            var th = createDom('', '', 'th');
            tr.appendChild(th);
            o.title && setText(th, o.title);
            if (o.tip && o.tip.length) {
                var tipEl = createDom('', '', 'span');
                tipEl.innerHTML = '';
                tipEl.setAttribute(
                    'ecui', 
                    'type:tip; id:' + o.field + '-tip; message:' + o.tip
                );
                th.appendChild(tipEl);
                ecui.init(tipEl);
            }
            //o.width && setStyle(th, 'width', o.width + 'px');
            //o.width && th.setAttribute('width', o.width + 'px');
            o.width && setStyle(th, 'minWidth', o.width + 'px');

            o.field && th.setAttribute('data-field', o.field);

            o.rowspan && th.setAttribute('rowSpan', o.rowspan);
            o.colspan && th.setAttribute('colSpan', o.colspan);

            if (o.sortable) {
                addClass(th, type + '-hcell-sort');

                if (o.field && o.field == this._sSortby) {
                    addClass(th, type + '-hcell-sort-' + this._sOrderby);
                }
            }

            if (o.checkbox) {
                var checkboxAll = createDom('', '', 'input');
                checkboxAll.setAttribute('type', 'checkbox');
                addClass(checkboxAll, type + '-checkbox-all');
                th.appendChild(checkboxAll);
            }
        }

        return tr;
    };

    /**
     * 得到表格的列配置
     * @public
     *
     * @return {Array} 表格的列配置
     */
    UI_CUSTOM_TABLE_CLASS.getFields = function() {
        return this._aColumns;
    };

    /**
     * 重新生成表格
     * @public
     *
     * @param {Array} fields 表格的列配置
     * @param {Array} datasource 表格数据
     * @param {Object} sortinfo 排序信息
     * @param {Object} options 初始化选项
     * @param {string} errorMsg 表格为空或出错时展示的内容
     */
    UI_CUSTOM_TABLE_CLASS.render = function(
        fields, datasource, sortinfo, options, errorMsg
    ) {
        var options = extend({}, options);
        options = extend(options, this._oOptions);
        options.leftLock = this._nLeftLock;
        options.rightLock = this._nRightLock;
        options.fields = fields;
        options.datasource = datasource || [];
        var sortinfo = sortinfo || {};
        options.sortby = sortinfo.sortby;
        options.orderby = sortinfo.orderby;
        options.errorMsg = errorMsg;

        if (!datasource.length) {
            options.leftLock = 0;
            options.rightLock = 0;
        }

        this.$refresh(options);
    };

    /**
     * 获取表格当前所有行单选框的引用
     * @private
     */
    UI_CUSTOM_TABLE_CLASS.$bindCheckbox = function () {
        var inputs = this.getBody().getElementsByTagName('input'),
            i, item, type = this.getTypes()[0];

        this._aCheckboxs = [];
        this._eCheckboxAll = null;

        for (i = 0; item = inputs[i]; i++) {
            if (item.type == 'checkbox' 
                    && item.className.indexOf(type + '-checkbox-all') >= 0
            ) {
                this._eCheckboxAll = item;
            }
            else if (item.type == 'checkbox' && item.className.indexOf(type + '-checkbox') >= 0) {
                this._aCheckboxs.push(item);
            }
        }
    };

    /**
     * 刷新表格的行单选框
     * @private
     *
     * @param {Boolean} checked 全选/全不选 如果忽略此参数则根据当前表格的实际选择情况来设置“全选”的勾选状态
     */
    UI_CUSTOM_TABLE_CLASS.$refreshCheckbox = function (checked) {
        var i, item, newChecked = true, tr;

        for (i = 0; item = this._aCheckboxs[i]; i++) {
            tr = item.parentNode.parentNode;
            if (checked !== undefined) {
                item.checked = checked;
            }
            else {
                newChecked = item.checked && newChecked;
            }

            if (item.checked && this._bCheckedHighlight) {
                tr.className += ' highlight';
            }
            else if (this._bCheckedHighlight) {
                tr.className = tr.className.replace(/\s+highlight/g, '');
            }
        }

        if (this._eCheckboxAll) {
            this._eCheckboxAll.checked = checked !== undefined ? checked : newChecked;
        }
    };

    /**
     * table生产完毕以后执行，触发sizechange事件
     *
     */
    UI_CUSTOM_TABLE_CLASS.$ready = function() {
        triggerEvent(this, 'sizechange');
    };


    /**
     * 浏览器resize时调整横滚的位置
     *
     * @override
     */
    UI_CUSTOM_TABLE_CLASS.$resize = function() {
        var me = this;
        UI_LOCKED_TABLE_CLASS.$resize.call(this);
        triggerEvent(this, 'sizechange');
        setTimeout(
            function() {
                me.$pagescroll();
            },
            500
        );
    };

    /**
     * 页面滚动时保持表头和横滚浮在视窗上
     *
     * @override
     */
    UI_CUSTOM_TABLE_CLASS.$pagescroll = function() {
        UI_LOCKED_TABLE_CLASS.$pagescroll.call(this);

        if (this._uHScrollbar) {
            // 因为是在iframe中引用的，所以DI中不提供这个功能
            // setFloatHScroll(this);
        }
    };

    UI_CUSTOM_TABLE_CLASS.getSelection = function () {
        if (!this._aCheckboxs || !this._aCheckboxs.length) {
            return [];
        }

        var res = [];

        for (var i = 0, o; o = this._aCheckboxs[i++]; ) {
            if (o.checked) {
                var index = getAttribute(o, 'data-rownum') - 0;
                res.push(extend({}, this._aData[index]));
            }
        }
        return res;
    };

    /**
     * @override
     */
    UI_CUSTOM_TABLE_CLASS.init = function () {
        var i, item, ele = this.getOuter(),
            control = this;

        UI_LOCKED_TABLE_CLASS.init.call(this);

        // 添加控件全局的事件监听
        // 只支持click mousedown mouseup
        if (!this.eventAdded) {
            for (i = 0; item = DELEGATE_EVENTS[i]; i++) {
                attachEvent(ele, item, (function (name) {
                    return function (event) {
                        var e = event || window.event;
                        e.targetElement = e.target || e.srcElement;
                        control.$fireEventHandler(name, e);
                    }
                })(item));
            }
            this.eventAdded = true;
        }

        // 行选中
        this.$initRowChecked();
    };

    /**
     * 触发表格events中定义的事件
     * @private
     *
     * @param {String} eventType 事件类型
     * @param {Event} nativeEvent 原生事件参数
     */
    UI_CUSTOM_TABLE_CLASS.$fireEventHandler = function (eventType, nativeEvent) {
        var events = getHandlerByType(this.events, eventType),
            i, item, target = nativeEvent.targetElement, selector;

        for (i = 0; item = events[i]; i++) {
            if (checkElementBySelector(target, item.selector)) {
                item.handler.call(target, nativeEvent, this);
            }
        }
    }

    UI_CUSTOM_TABLE_CLASS.$refresh = function (options) {
        detachEvent(WINDOW, 'resize', repaint);

        this.$disposeInner();

        var el = this.getOuter();
        el.innerHTML = '';
        this.$setBody(el);
        this.$resize();

        // FIXME
        // 这种方式有个问题：必须要求getOuter的父节点此时有width，
        // 否则的话，上一步resize()后导致没宽度
        // 后续改！=====================================

        UI_CUSTOM_TABLE.client.call(
            this, 
            el, 
            extend(
                { uid: this._sUID, primary: this._sPrimary }, 
                options
            )
        );
        this._bCreated = false;
        this.cache(true, true);
        UI_LOCKED_TABLE_CLASS.init.call(this);

        this.init();
        // TODO
        // this.$bindCellLink();

        attachEvent(WINDOW, 'resize', repaint);
    };

    /**
     * 析构内部
     * 
     * @protected
     */
    UI_CUSTOM_TABLE_CLASS.$disposeInner = function() {
        var disposeFunc = this.$dispose;
        this.$dispose = new Function();
        disposeControl(this);
        this.$dispose = disposeFunc;
    };

    /**
     * 得到内容区域的row控件
     *
     * @protected
     */
    UI_CUSTOM_TABLE_CLASS.$getContentRow = function(rowIndex) {
        // LockedTable失去了对内容row的引用，所以用这种不太好看的方法找到
        var row;
        var cell;
        return (row = this._aRows[rowIndex])
            && (cell = row.getCell(this._nLeftLock || 0))
            && cell.getParent()
            || null;
    };

    /**
     * 设置内容行选中
     *
     * @private
     */
    UI_CUSTOM_TABLE_CLASS.$initRowChecked = function() {
        var rowCheck = this._oRowCheck;
        for (
            var i = 0, rowCtrl; 
            i < (rowCheck.rowChecked || []).length; 
            i ++
        ) {
            // LockedTable失去了对内容row的引用，所以用这种不太好看的方法找到
            if (rowCtrl = this.$getContentRow(rowCheck.rowChecked[i])) {
                this.$setRowChecked(rowCtrl, true);
            }
        }
    };    

    /**
     * 设置内容行选中
     *
     * @private
     */
    UI_CUSTOM_TABLE_CLASS.$setRowChecked = function(rowCtrl, checked) {
        var type = this.getType();
        var rowCheck = this._oRowCheck;

        var rowCheckMode = this._oRowCheck.rowCheckMode;
        // 多选
        if (rowCheckMode == 'CHECK') {
            if (checked
                && !rowCtrl._bRowChecked
                && rowCheck.rowCheckCount < rowCheck.rowCheckMax
            ) {
                rowCtrl._bRowChecked = true;
                addClass(rowCtrl.getMain(), type + '-row-checked');
                rowCheck.rowCheckCount ++;
                return true;
            }

            if (!checked 
                && rowCtrl._bRowChecked
                && rowCheck.rowCheckCount > rowCheck.rowCheckMin
            ) {
                rowCtrl._bRowChecked = false;
                removeClass(rowCtrl.getMain(), type + '-row-checked');
                rowCheck.rowCheckCount --;
                return true;
            }
        }
        // 单选
        else if (rowCheckMode == 'SELECT') {
            var rows = this._aRows || [];
            for (var i = 0, row, cell; i < rows.length; i ++) {
                if ((row = rows[i]) && row._bRowChecked) {
                    row._bRowChecked = false;
                    removeClass(row.getMain(), type + '-row-selected');
                }
            }
            rowCtrl._bRowChecked = true;
            addClass(rowCtrl.getMain(), type + '-row-selected');
            rowCheck.rowCheckCount = 1;
        }

        return false;
    };

    /**
     * 内容行是否选中
     *
     * @private
     */
    UI_CUSTOM_TABLE_CLASS.$isRowChecked = function(rowCtrl) {
        return !!rowCtrl._bRowChecked;
    };

    /**
     * 得到当前状态数据
     *
     * @public
     * @return {Object} 当前状态数据
     */
    UI_CUSTOM_TABLE_CLASS.getValue = function() {
        var rowChecked = [];
        var rows = this._aRows || [];
        for (var i = 0, row; i < rows.length; i ++) {
            if ((row = rows[i]) && row._bRowChecked) {
                rowChecked.push({ value: this._aData[i], index: i });
            }
        }
        return {
            rowChecked: rowChecked,
            data: (this._aData || []).slice()
        }
    };

    /**
     * 让表格的横滚始终悬浮在页面视窗低端
     * 
     * @param {ecui.ui.CustomTable} con
     */
    function setFloatHScroll(con) {
        var el;

        el = con._eBrowser ? con._eBrowser : con._uHScrollbar.getOuter();
        el.style.top = MIN(
            getView().bottom - getPosition(con.getOuter()).top - el.offsetHeight,
            con.getHeight() - el.offsetHeight
        ) + 'px';

        setStyle(el, 'zIndex', 1);
    }

    function getHandlerByType(events, type) {
        var handlers = [], item;

        events = extend({}, events);
        events = extend(events, DEFAULT_EVENTS);

        for (var key in events) {
            item = {handler: events[key]};
            key = key.split(/\s+/);
            if (key[0] == type) {
                item.selector = key[1];
                handlers.push(item);
            }
        }

        return handlers;
    }

    function checkElementBySelector(ele, selector) {
        var tagName, value, type, res = true;

        if (!ele && !selector) {
            return false;
        }

        selector.replace(/^([^.#]*)([.#]?)(.*)$/, function ($0, $1, $2, $3) {
            tagName = $1;
            type = $2;
            value = $3;
        });

        if (tagName && ele.tagName.toLowerCase() != tagName) {
            res = false;
        }

        if (type == '.' && !new RegExp('(^|\\s+)' + value + '(\\s+|$)').test(ele.className)) {
            res = false;
        }

        if (type == '#' && ele.id != value) {
            res = false;
        }

        return res;
    }
    
    function copyArray(data) {
        var res = [];
        for (var i = 0, o; o = data[i++]; ) {
            res.push(extend({}, o));
        }
        return res;
    }

    /**
     * 得到cell坐标
     * 
     * @protected
     * @return {Object} 形如：{x: 4, y: 5}
     */    
    function getCellPosition(el) {
        var pos = el.getAttribute('data-cell-pos');
        if (pos) {
            pos = pos.split('-');
            return { x: toNumber(pos[0]), y: toNumber(pos[1]) };
        }
        else {
            return null;
        }
    } 

   //--------------------------------------------------
    // UI_TABLE_CELL 方法
    //--------------------------------------------------

    /**
     * 点击事件
     * 
     * @event
     * @protected
     */
    UI_CUSTOM_TABLE_CELL_CLASS.$click = function(event) {
        UI_CUSTOM_TABLE_CLASS.Cell.superClass.$click.call(this, event);

        // 链接则不走handleCellClick
        if (!event.target 
            || !(
                event.target.getAttribute('data-cell-link-drill-a')
                || event.target.getAttribute('data-cell-link-bridge-a')
            )
        ) {
            this.$handleCellClick();
        }
    };

    /**
     * 处理cell点击事件
     * 
     * @protected
     */    
    UI_CUSTOM_TABLE_CELL_CLASS.$handleCellClick = function() {
        var el = this.getOuter();
        var tableCtrl = this.getParent().getParent();
        var ec;

        // 如果是内容节点
        if (el.getAttribute('data-content')) {
            var rowDefItem;
            if (pos = getCellPosition(this.getOuter())) {
                rowDefItem = tableCtrl._aData[pos.y];
            }
            // 暂全部为line选中
            triggerEvent(tableCtrl, 'rowclick', null, [rowDefItem]);

            var rowCtrl = this.getParent();

            var rowCheckMode = tableCtrl._oRowCheck.rowCheckMode;
            if (rowCheckMode) {
                var rowChecked = tableCtrl.$isRowChecked(rowCtrl);
                var eventName;

                if (rowCheckMode == 'SELECT') {
                    tableCtrl.$setRowChecked(rowCtrl, true);
                    eventName = 'rowselect';
                }
                else if (rowCheckMode == 'CHECK') {
                    if (rowChecked && tableCtrl.$setRowChecked(rowCtrl, false)) {
                        eventName = 'rowuncheck';
                    }
                    else if (!rowChecked && tableCtrl.$setRowChecked(rowCtrl, true)) {
                        eventName = 'rowcheck';
                    }
                }

                var callback = function (checked) {
                    tableCtrl.$setRowChecked(rowCtrl, checked);
                }

                eventName && triggerEvent(
                    tableCtrl,
                    eventName,
                    null,
                    [rowDefItem, callback]
                );
            }
        }
    };

 })();