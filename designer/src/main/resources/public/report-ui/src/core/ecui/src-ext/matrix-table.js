/**
 * ui.MatrixTable
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * desc:    矩阵表格
 *          支持行列表头树结构，横纵向内容滚动条并表头固定
 *          单元格高宽必须指定，整体高宽必须指定
 * author:  sushuang(sushuang)
 */

(function () {

    var core = ecui,
        string = core.string,
        ui = core.ui,
        util = core.util,
        dom = core.dom,
        string = core.string,
        undefined,
        extend = util.extend,
        blank = util.blank,
        createDom = dom.create,
        addClass = dom.addClass,
        removeClass = dom.removeClass,
        setStyle = dom.setStyle,
        attachEvent = util.attachEvent,
        encodeHTML = string.encodeHTML,
        inheritsControl = core.inherits,
        triggerEvent = core.triggerEvent,
        pushArray = Array.prototype.push,
        UI_PANEL = ui.Panel,
        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = UI_CONTROL.prototype;

    var UI_MATRIX_TABLE = ui.MatrixTable =
        inheritsControl(
            UI_PANEL,
            'ui-matrix-table',
            function (el, options) {
                var type = this.getType();

                this._nBorderWidth = parseInt(options.borderWidth) || 1;
                this._nPanelWidth = parseInt(options.width) || 0;
                this._nPanelHeight = parseInt(options.height) || 0;

                setStylePosition(el, 'relative');
                setStyleSize(el, this._nPanelWidth, this._nPanelHeight);

                el.appendChild(
                    this._eBodyContainer = createDom(type + '-body-container')
                );
                el.appendChild(
                    this._eColHeadContainer = createDom(type + '-col-head-container')
                );
                el.appendChild(
                    this._eRowHeadContainer = createDom(type + '-row-head-container')
                );
                el.appendChild(
                    this._eCornerContainer = createDom(type + '-conner-container')
                );
                setStylePosition(this._eCornerContainer, 'absolute');
                setStylePosition(this._eColHeadContainer, 'absolute');
                setStylePosition(this._eRowHeadContainer, 'absolute');
                setStylePosition(this._eBodyContainer, 'absolute');
            },
            function (el, options) {
                this._aData = [];
                this._sEmptyText = options.emptyText || '暂无数据';
            }
        ),

        UI_MATRIX_TABLE_CLASS = UI_MATRIX_TABLE.prototype,

        DELEGATE_EVENTS = ['click', 'mouseup', 'mousedown'],

        DEFAULT_GAP = 20,

        // 默认处理函数
        DEFAULT_EVENTS = {
            'click th.ui-lite-table-hcell-sort': function (event, control) {
                var field = this.getAttribute('data-field'),
                    orderby, originOrderby;

                if (this.className.indexOf('-sort-desc') >= 0) {
                    originOrderby = 'desc';
                    orderby = 'asc';
                }
                else if (this.className.indexOf('-sort-asc') >= 0) {
                    originOrderby = 'asc';
                    orderby = 'desc'
                }
                else {
                    originOrderby = null;
                    orderby = this.getAttribute('data-orderby') || 'desc';
                }

                triggerEvent(control, 'sort', null, [field, orderby, originOrderby]);
            }
        };

    function clone(source) {
        var result, i, len;
        if (Object.prototype.toString.call(source) == '[object Array]') {
            result = [];
            for (i = 0, len = source.length; i < len; i++) {
                result[result.length] = clone(source[i]);
            }
        } else if (Object.prototype.toString.call(source) == '[object Date]') {
            result = new Date(source.getTime());
        } else if (source === Object(source)) {
            result = {};
            for (i in source) {
                if (source.hasOwnProperty(i)) {
                    result[i] = clone(source[i]);
                }
            }
        } else {
            result = source;
        }
        return result;
    }

    function getHanlderByType(events, type) {
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

    /**
     * 触发表格events中定义的事件，故意不许继承
     * @private
     *
     * @param {String} eventType 事件类型
     * @param {Event} nativeEvent 原生事件参数
     */
    function fireEventHanlder(eventType, nativeEvent) {
        var events = getHanlderByType(this.events, eventType),
            i, item, target = nativeEvent.targetElement, selector;

        for (i = 0; item = events[i]; i++) {
            if (checkElementBySelector(target, item.selector)) {
                item.handler.call(target, nativeEvent, this);
            }
        }
    }

    /**
     * @override
     */
    UI_MATRIX_TABLE_CLASS.init = function () {
        var i, item, 
            el = this.getOuter(),
            control = this;

        UI_CONTROL_CLASS.init.call(this);

        // 添加控件全局的事件监听
        // 只支持click mousedown mouseup
        for (i = 0; item = DELEGATE_EVENTS[i]; i++) {
            attachEvent(el, item, (function (name) {
                return function (event) {
                    var e = event || window.event;
                    e.targetElement = e.target || e.srcElement;
                    fireEventHanlder.call(control, name, e);
                }
            })(item));
        }
    };

    /**
     * @override
     */
    UI_MATRIX_TABLE_CLASS.$dispose = function () {
        this._eColHeadContainer = null;
        this._eBodyContainer = null;
        this._eRowHeadContainer = null;
        this._eCornerContainer = null;
        UI_CONTROL_CLASS.$dispose.call(this);
    };

    /**
     * 设置表格的数据
     * @public
     * 
     * @param {Array} datasource 表格数据
     * @param {Object} sortInfo 排序信息
     *          {String} sortby 排序字段
     *          {String} orderby 排序方式
     * @param {Boolean} isSilent 静默模式 如果true的话 不会立刻重绘表格 需要手动调用render
     */
    UI_MATRIX_TABLE_CLASS.setData = function (datasource, sortInfo, isSilent) {
        this._aData = clone(datasource);
        if (sortInfo) {
            this._sSortby = sortInfo.sortby || '';
            this._sOrderby = sortInfo.orderby || '';
        }

        !isSilent && this.render();
    };

    UI_MATRIX_TABLE_CLASS.getData = function () {
        return clone(this._aData);
    };

    UI_MATRIX_TABLE_CLASS.getDataByField = function (o, field) {
        var i, item;

        field = field || 'id';
        for (i = 0; item = this._aData[i]; i++) {
            if (item[field] == o) {
                return extend({}, item);
            }
        }

        return null;
    };

    /**
     * 设置表格的列信息
     * @public
     * 
     * @param {Array.Object} colFields 列信息，每个Object为树根
     *              {Array.Object} children 子树列表
     * @param {Array.Object} rowFields 行信息，每个Object为树根，children是子树列表，可缺省
     *              {Array.Object} children 子树列表
     * @param {Array.Object} cornerFields 角格信息，决定rowFields的宽度，每个Object为树根，children是子树列表
     * @param {Boolean} isSilent 静默模式 如果true的话 不会立刻重绘表格 需要手动调用render
     */
    UI_MATRIX_TABLE_CLASS.setFields = function (colFields, rowFields, cornerFields, isSilent) {
        var i, wrap, len;

        this._aColFields = clone(colFields);
        this._aRowFields = clone(rowFields);
        this._aCornerFields = clone(cornerFields);
        this.$createFields(colFields, cornerFields, true);
        this.$createFields(rowFields, cornerFields, false);
        this._nColLength = this._aColAbsoluteFields[0].length;
        this._nRowLength = this._aRowAbsoluteFields 
            ? this._aRowAbsoluteFields.length : 0;

        this.$evaluateWidth();

        !isSilent && this.render();
    };

    /**
     * 渲染空表格
     * @public
     *
     */
    UI_MATRIX_TABLE_CLASS.renderEmpty = function () {
        this.setFields([{title:'&nbsp;', width: 200, content: 'empty', align: 'center'}], [{title:'&nbsp;'}], [{title:'&nbsp;', width: 10}], true);
        this.setData([{empty: this._sEmptyText}], true);
        this.render();
    };

    /**
     * 设置空值时显示的文本
     * @public
     */
    UI_MATRIX_TABLE_CLASS.setEmptyText = function (text) {
        this._sEmptyText = text;
    };

    /**
     * 得到空值时显示的文本
     * @public
     */
    UI_MATRIX_TABLE_CLASS.getEmptyText = function () {
        return this._sEmptyText;
    };

    /**
     * 设置单元格高亮
     * @public
     * 
     * @param {number} x x坐标
     * @param {number} y y坐标
     * @param {boolean} isHighlight 是否高亮
     */    
    UI_MATRIX_TABLE_CLASS.setHighlight = function (x, y, isHighlight) {
        var i, o, pos, xy, 
            type = this.getType(),
            eCells = this.getBody().getElementsByTagName('td');

        for (i = 0; o = eCells[i]; i ++) {
            if (pos = o.getAttribute('cell-pos')) {
                xy = pos.split('-');
                if ((x == null || xy[0] == x) && (y == null || xy[1] == y)) {
                    isHighlight 
                        ? addClass(o, type + '-highlight') 
                        : removeClass(o, type + '-highlight');
                }
            }
        }
    };

    /**
     * 重新绘制表格
     * @public
     */
    UI_MATRIX_TABLE_CLASS.render = function () {

        // 渲染表头
        this.$renderColHead();
        this.$renderRowHead();
        this.$renderCorner();

        if (!this._aData || this._aData.length <= 0) {
            // 渲染无数据表格
            this.$renderEmpty();
        }
        else {
           this.$renderBody();
        }

        this.$layout();
    };

    UI_MATRIX_TABLE_CLASS.$renderCorner = function () {
        var i, item, html = [], width = 0, height = 0, type = this.getType();
        if (this._aColPlainFields.length 
            && this._aRowAbsoluteFields
            && this._aRowAbsoluteFields.length) {
            html.push('<div class="', type, '-head-corner ', '"></div>');
        }   
        this._eCornerContainer.innerHTML = html.join('');
    };

    UI_MATRIX_TABLE_CLASS.$renderColHead = function () {
        var type = this.getType(),
            html = ['<table cellpadding="0" style="table-layout:fixed;white-space:nowrap" cellspacing="0" class="', 
                type, '-col-head-table">'],
            line, i, j, wrap, onlink, item, me = this;

        for (i = 0; line = this._aColAbsoluteFields[i]; i++) {
            html.push('<tr class="', type, '-head">');
            for (j = 0; j < line.length; j++ ) {
                if (wrap = line[j])  { 
                    pushArray.apply(html, this.$renderColHCell(wrap, j, i));
                }
            }
            html.push('</tr>');
        }

        this._eColHeadContainer.innerHTML = html.join('');

        // 挂link的回调事件
        as = this._eColHeadContainer.getElementsByTagName('a');
        onlink = function() {
            colLinkHandler.call(this, me);
            return false;
        };
        for (i = 0; item = as[i]; i ++) {
            item.onclick = onlink; 
        }
    };

    UI_MATRIX_TABLE_CLASS.$renderRowHead = function () {
        var type = this.getType(),
            html = ['<table cellpadding="0" style="table-layout:fixed;white-space:nowrap" cellspacing="0" class="', 
                type, '-row-head-table">'],
            col, line, i, j, wrap, item, me = this;

        if (!this._aRowAbsoluteFields || this._aRowAbsoluteFields.length == 0) {
            this._eRowHeadContainer.innerHTML = '';
            return;
        }   

        col = this._aRowAbsoluteFields[0] || [];
        for (i = 0; i < col.length; i++) {
            html.push('<tr class="', type, '-row-head">');
            for (j = 0; line = this._aRowAbsoluteFields[j]; j++ ) {
                if (wrap = line[i])  { 
                    pushArray.apply(html, this.$renderRowHCell(wrap, j, i));
                }
            }
            html.push('</tr>');
        }
        
        this._eRowHeadContainer.innerHTML = html.join('');

        // 挂link的回调事件
        as = this._eRowHeadContainer.getElementsByTagName('a');
        onlink = function() {
            rowLinkHandler.call(this, me);
            return false;
        };
        for (i = 0; item = as[i]; i ++) {
            item.onclick = onlink; 
        }
    };

    UI_MATRIX_TABLE_CLASS.$renderColHCell = function (wrap, x, y) {
        var type = this.getType(), html = [], 
            field = wrap.field, 
            className, span, str;

        className = type + '-hcell';
        span = wrap.span ? ' colspan="' + wrap.span + '" ' : '';

        if (y == 0) {
            className += ' ' + type + '-hcell-top';
        }
        html.push('<th');
        if (field.width) {
            html.push(' style="width:' + field.width + 'px"');
        }
        if (field.sortable) {
            className += ' ' + type + '-hcell-sort';
            if (field.field && field.field == this._sSortby) {
                className += ' ' + type + '-hcell-sort-' + this._sOrderby;
            }
            html.push(' data-field="'+ field.field +'"');
            if (field.orderby) {
                html.push(' data-orderby="' + field.orderby + '"');
            }
        }
        str = field.title;
        if (typeof field.headLink == 'function'
            && field.headLink.call(null, field, x, y)
        ) {
            str = '<a link-type="col" href="#" link-x="' + x + '" link-y="' + y + '" >' + str + '</a>';
        }        
        html.push(
            ' ', span + ' ', ' class="', className, '">', 
            '<div class="', type, '-hcell-inner">',
            str, 
            '</div>',
            '</th>'
        );
        return html;
    }; 

    UI_MATRIX_TABLE_CLASS.$renderRowHCell = function (wrap, x, y) {
        var type = this.getType(), html = [], 
            field = wrap.field, 
            className, span, str;

        className = type + '-rowhcell';
        span = wrap.span ? ' rowspan="' + wrap.span + '" ' : '';

        if (x == 0) {
            className += ' ' + type + '-rowhcell-left';
        }
        html.push('<td');
        if (wrap.colWidth) {
            html.push(' style="width:' + wrap.colWidth + 'px"');
        }
        str = field.title;
        if (typeof field.headLink == 'function'
            && field.headLink.call(null, field, x, y)
        ) {
            str = '<a link-type="row" href="#" link-x="' + x + '" link-y="' + y + '" >' + str + '</a>';
        }         
        html.push(
            ' ', span + ' ', ' class="', className, '">',
            '<div class="', type, '-rowhcell-inner">',
            field.title, 
            '</div>',
            '</td>'
        );
        return html;
    }; 

    UI_MATRIX_TABLE_CLASS.$renderEmpty = function () {
        var type = this.getType(), html = [];
        html.push(
            '<table cellpadding="0" cellspacing="0" style="table-layout:fixed;white-space:nowrap" class="', type, '-table">',
                '<tr class="', type, '-row">', 
                    '<td colspan="', this._nColLength, '" class="', type, '-cell-empty">',
                        this._sEmptyText,
                    '</td>',
                '</tr>',
            '</table>'
        );
        this._eBodyContainer.innerHTML = html.join('');
    };

    UI_MATRIX_TABLE_CLASS.$renderBody = function () {
        var i, j, item, field, rowField, html = [], str, colspan, rowspan, 
            styleStr, className, as, onlink,
            type = this.getType(),
            me = this,
            html = ['<table cellpadding="0" cellspacing="0" style="table-layout:fixed;white-space:nowrap" class="', type, '-table">'];

        for (i = 0; item = this._aData[i]; i++) {
            html.push('<tr class="'+ type +'-row">')
            rowField = this._aRowPlainFields ? this._aRowPlainFields[i] : null;

            for (j = 0; field = this._aColPlainFields[j]; j++) {
                cellHTML = [];
                colspan = '';
                rowspan = '';
                className = type + '-cell';
                styleStr = '';

                if (field.width) {
                    styleStr += ' width:' + field.width + 'px ';
                }
                if (j == 0) {
                    className += ' ' + type + '-cell-left';
                }
                if (field.align) {
                    className += ' ' + type + '-cell-align-' + field.align;
                }

                if (typeof field.content == 'function') {
                    str = field.content.call(null, item, i, j, field, rowField);
                }
                else {
                    str = item[field.content];
                    if (!str && str != 0) {
                        str = '&nbsp;';
                    }
                    else {
                        str = encodeHTML(str + '');
                    }
                }

                if (typeof field.link == 'function'
                    && field.link.call(null, item, j, i, field, rowField)
                ) {
                    str = '<a link-type="cell" href="#" link-y="' + i + '" link-x="' + j + '" >' + str + '</a>';
                }

                cellHTML.push(str);

                html.push('<td ' + colspan + ' ' + rowspan + ' cell-pos="' + j + '-' + i + '" class="'+ className +'" style="' + styleStr + '">');
                html.push('<div class="' + type + '-cell-inner">');
                pushArray.apply(html, cellHTML);
                html.push('</div>');
                html.push('</td>');
            }
            html.push('</tr>')
        }
        html.push('</table>');

        this._eBodyContainer.innerHTML = html.join('');

        // 挂link的回调事件
        as = this._eBodyContainer.getElementsByTagName('a');
        onlink = function() {
            cellLinkHandler.call(this, me);
            return false;
        };
        for (i = 0; item = as[i]; i ++) {
            item.onclick = onlink; 
        }
    };

    UI_MATRIX_TABLE_CLASS.$createFields = function (fields, cornerFields, isCol) {    
        var i, j, wrap, deep, field, absoluteFields = [], 
            plainFields, subStartIndex, len, abName, plName;

        if (!fields || fields.length == 0) {
            return;
        }

        // 深度优先后序遍历
        subStartIndex = 0;
        for (j = 0; field = fields[j]; j ++) {
            subStartIndex += travelFieldsTree(
                field, 0, subStartIndex, absoluteFields
            );
        }

        // 计算rowHead的每列宽度
        if (cornerFields) {
            for (i = 0; field = cornerFields[i]; i ++) {
                if (absoluteFields[i] && field.width)
                for (j = 0; j < absoluteFields[i].length; j ++) {
                    if (wrap = absoluteFields[i][j]) {
                        wrap.colWidth = field.width;
                    }
                }
            }
        }
        this[abName] = absoluteFields;

        len = absoluteFields.length;
        plainFields = [];
        for (i = 0; wrap = absoluteFields[len - 1][i]; i ++) {
            plainFields.push(wrap.field);
        } 

        if (isCol) {
            this._aColAbsoluteFields = absoluteFields;
            this._aColPlainFields = plainFields;
        } else {
            this._aRowAbsoluteFields = absoluteFields;
            this._aRowPlainFields = plainFields;
        }
    };

    function travelFieldsTree(field, deep, startIndex, absoluteFields) {
        var i, child, children, span = 0;

        if (!field) {
            return span;
        }

        children = field.children || [];
        for (i = 0; child = children[i]; i ++) {
            span += travelFieldsTree(
                child, deep + 1, startIndex + span, absoluteFields
            );
        }

        !absoluteFields[deep] && (absoluteFields[deep] = []);
        absoluteFields[deep][startIndex] = {
            field: field, 
            deep: deep,
            span: span || undefined
        };
        return span || 1;
    }

    /**
     * 布局
     * @public
     */
    UI_MATRIX_TABLE_CLASS.$layout= function () {
        var bodyWidth, bodyHeight;

        // 给panel的body也设置估算的宽度，因为它是absolute的，
        // 如果不设，内部表格宽度设置不能生效
        setStyle(
            this.getBody(), 
            'width', 
            (this._nColHeadWidthEval + this._nRowHeadWidthEval + 5) + 'px'
        );

        // 设置正式的宽度
        this._nColHeadHeight = this._eColHeadContainer.offsetHeight;
        this._nColHeadWidth = this._eColHeadContainer.offsetWidth;
        this._nRowHeadWidth = this._eRowHeadContainer.offsetWidth;
        this._nRowHeadHeight = this._eRowHeadContainer.offsetHeight;
        bodyWidth = this._nColHeadWidth + this._nRowHeadWidth;
        bodyHeight = this._nColHeadHeight + this._nRowHeadHeight;

        setStyle(this._eCornerContainer, 'width', (this._nRowHeadWidth - this._nBorderWidth * 2) + 'px');
        setStyle(this._eCornerContainer, 'height', (this._nColHeadHeight - this._nBorderWidth * 2) + 'px');
        setStyle(this.getBody(), 'width', bodyWidth + 'px');
        setStyle(this.getBody(), 'height', bodyHeight + 'px');
        setStyle(this._eBodyContainer, 'top', this._nColHeadHeight + 'px');
        setStyle(this._eBodyContainer, 'left', this._nRowHeadWidth + 'px');
        setStyle(this._eColHeadContainer, 'left', this._nRowHeadWidth + 'px');
        setStyle(this._eRowHeadContainer, 'top', this._nColHeadHeight + 'px');

        // 重置外框的宽度
        this.cache(true, true);
        // TODO
        // 没功夫整，暂时先用DEFAULT_GAP这个凑合的办法
        this.setSize(
            bodyWidth + DEFAULT_GAP > this._nPanelWidth 
                ? this._nPanelWidth : bodyWidth + DEFAULT_GAP,
            bodyHeight + DEFAULT_GAP > this._nPanelHeight 
                ? this._nPanelHeight : bodyHeight + DEFAULT_GAP
        );
    };

    /**
     * 定位
     * @public
     */
    UI_MATRIX_TABLE_CLASS.$setPosition= function (sx, sy) {
        sx = sx || 0;
        sy = sy || 0;
        setStyle(this._eColHeadContainer, 'top', sy + 'px');
        setStyle(this._eRowHeadContainer, 'left', sx + 'px');
        setStyle(this._eCornerContainer, 'top', sy + 'px');
        setStyle(this._eCornerContainer, 'left', sx + 'px');
    };

    /**
     * 定位
     * @public
     */
    UI_MATRIX_TABLE_CLASS.$evaluateWidth = function () {
        var i, o;

        this._nColHeadWidthEval = 0;
        if (this._aColPlainFields) {
            for (i = 0; o = this._aColPlainFields[i]; i ++) {
                this._nColHeadWidthEval += o.width + 1;
            }
        }

        this._nRowHeadWidthEval = 0;
        if (this._aRowAbsoluteFields) {
            for (i = 0; i < this._aRowAbsoluteFields.length; i ++) {
                o = this._aRowAbsoluteFields[i][0];
                this._nRowHeadWidthEval += o.colWidth + this._nBorderWidth;
            }
        }
    };

    function cellLinkHandler(control) {
        var x = this.getAttribute('link-x');
        var y = this.getAttribute('link-y');
        triggerEvent(
            control, 
            'celllink', 
            null,
            [
                control._aData[y], 
                x, y, 
                control._aColPlainFields[x], control._aRowPlainFields[y]
            ]
        );
    };

    function colLinkHandler(control) {
        var x = this.getAttribute('link-x');
        var y = this.getAttribute('link-y');
        triggerEvent(
            control, 
            'collink', 
            null,
            [control._aColAbsoluteFields[y][x].field, x, y]
        );
    };

    function rowLinkHandler(control) {
        var x = this.getAttribute('link-x');
        var y = this.getAttribute('link-y');
        triggerEvent(
            control, 
            'rowllink', 
            null,
            [control._aRowAbsoluteFields[x][y].field, x, y]
        );
    };

    /**
     * @private
     */
    function setStylePosition(control, postionType) {
        setStyle(control, 'position', postionType);
        setStyle(control, 'top', '0px');
        setStyle(control, 'left', '0px');
    };

    /**
     * @private
     */
    function setStyleSize(control, width, height) {
        setStyle(control, 'width', width + 'px');
        setStyle(control, 'height', height + 'px');
    };

    /**
     * @override
     */
    UI_MATRIX_TABLE_CLASS.$scroll = function () {
        UI_MATRIX_TABLE.superClass.$scroll.apply(
            this, 
            Array.prototype.slice.call(arguments, 1)
        );
        this.$setPosition(this.getScrollLeft(), this.getScrollTop());
    };

})();
