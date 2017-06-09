/**
 * liteTable - 简单表格
 *
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
        addClass = dom.addClass,
        removeClass = dom.removeClass,
        attachEvent = util.attachEvent,
        encodeHTML = string.encodeHTML,

        inheritsControl = core.inherits,
        triggerEvent = core.triggerEvent,

        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = UI_CONTROL.prototype;

    var UI_LITE_TABLE = ui.LiteTable =
        inheritsControl(
            UI_CONTROL,
            'ui-lite-table',
            function (el, options) {
                options.resizable = false;
            },
            function (el, options) {
                this._aData = [];
                this._aFields = [];
                this._eCheckboxAll = null;
                this._aCheckboxs = [];
                this._sEmptyText = options.emptyText || '暂无数据';
                this._bCheckedHighlight = options.checkedHighlight === true;
                this._bFixMode = options.fixMode;
            }
        );

    var UI_LITE_TABLE_CLASS = UI_LITE_TABLE.prototype;

    var DELEGATE_EVENTS = ['click', 'mouseup', 'mousedown'];

    // 默认处理函数(排序)
    function handleSortClick(event, control) {
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

    // 默认处理函数(checkbox-all)
    function handleCheckboxAllClick (event, control) {
        control.$refreshCheckbox(this.checked);
    }

    // 默认处理函数(checkbox)
    function handleCheckboxClick (event, control) {
        control.$refreshCheckbox();
    }

    // 得到默认处理函数
    function getDefaultEventHandler() {
        var type = this.getType();

        defaultEventMap = {};

        defaultEventMap['click th.' + type + '-hcell-sort'] = 
            handleSortClick;
        defaultEventMap['click input.' + type + '-checkbox-all'] = 
            handleCheckboxAllClick;
        defaultEventMap['click input.' + type + '-checkbox'] = 
            handleCheckboxClick;

        return defaultEventMap;
    }

    function copyArray(data) {
        var res = [], i, item;

        for (i = 0; item = data[i]; i++) {
            res.push(extend({}, item));
        }

        return res;
    }

    function getHanlderByType(eventType) {
        var handlers = [], item;

        var events = extend({}, this.events);
        events = extend(events, getDefaultEventHandler.call(this));

        for (var key in events) {
            item = {handler: events[key]};
            key = key.split(/\s+/);
            if (key[0] == eventType) {
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

    function buildTabeBody(fields, datasource, type) {
        var i, item, j, field, html = [], str, skip, colspan, rowspan, className;

        for (i = 0; item = datasource[i]; i++) {
            html.push('<tr class="'+ type +'-row">')

            for (j = 0; field = fields[j]; j++) {
                skip = false;
                cellHTML = [];
                colspan = '';
                rowspan = '';
                className = type + '-cell';
                if (j == 0) {
                    className += ' ' + type + '-cell-left';
                }
                if (field.align) {
                    className += ' ' + type + '-cell-align-' + field.align;
                }
                else if (field.checkbox) {
                    className += ' ' + type + '-cell-align-center';
                }
                if (field.checkbox) {
                    cellHTML.push('<input type="checkbox" value="'+ item[field.content] + '" class="'+ type +'-checkbox"');
                    if (field.checkedField && item[field.checkedField] == true) {
                        cellHTML.push(' checked="checked"');
                    }
                    cellHTML.push(' />');
                }
                else {
                    if (typeof field.content == 'function') {
                        o = field.content.call(null, item, i);
                        if (o === Object(o)) {
                            if (o.html) { cellHTML.push(o.html); }
                            else { skip = true; } 
                            if (o.colspan) { colspan = ' colspan="' + o.colspan + '" '; }
                            if (o.rowspan) { rowspan = ' rowspan="' + o.rowspan + '" '; }
                        } else {
                            cellHTML.push(o);
                        }
                    }
                    else {
                        str = item[field.content];
                        if (!str && str != 0) {
                            str = '&nbsp;';
                        }
                        else {
                            str = encodeHTML(str + '');
                        }
                        cellHTML.push(str);
                    }
                }
                if (!skip) {
                    html.push('<td ' + colspan + ' ' + rowspan + ' cell-pos="' + j + '-' + i + '" class="'+ className +'">');
                    Array.prototype.push.apply(html, cellHTML);
                    html.push('</td>');
                }
            }
            html.push('</tr>')
        }

        return html.join('');
    };

    function cellLinkHandler(control) {
        if (control._bDisabled) {
            return;
        }

        var x = this.getAttribute('link-pos-x');
        var y = this.getAttribute('link-pos-y');
        var linkName = this.getAttribute('link');
        triggerEvent(
            control, 
            'celllink',
            null,
            [
                control._aData[y],
                control._aFields[x],
                linkName,
                x, 
                y,
                this
            ]
        );
    };

    /**
     * @override
     */
    UI_LITE_TABLE_CLASS.$setSize = blank;

    /**
     * @override
     */
    UI_LITE_TABLE_CLASS.init = function () {
        var i, item, ele = this.getOuter(),
            control = this;

        UI_CONTROL_CLASS.init.call(this);

        // 添加控件全局的事件监听
        // 只支持click mousedown mouseup
        for (i = 0; item = DELEGATE_EVENTS[i]; i++) {
            attachEvent(ele, item, (function (name) {
                return function (event) {
                    var e = event || window.event;
                    e.targetElement = e.target || e.srcElement;
                    control.$fireEventHanlder(name, e);
                }
            })(item));
        }
    }

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
    UI_LITE_TABLE_CLASS.setData = function (datasource, sortInfo, isSilent) {
        this._aData = copyArray(datasource);
        if (sortInfo) {
            this._sSortby = sortInfo.sortby || '';
            this._sOrderby = sortInfo.orderby || '';
        }

        !isSilent && this.render();
    };

    /**
     * 得到表格数据
     * 
     * @public
     * @param {boolean} getOrigin true则得到原对象，false或缺省则得到副本
     * @return {Array.<Object>} 表格数据
     */
    UI_LITE_TABLE_CLASS.getData = function (getOrigin) {
        return getOrigin ? this._aData : copyArray(this._aData);
    };

    UI_LITE_TABLE_CLASS.getDataByField = function (o, field) {
        var i, item;

        field = field || 'id';
        for (i = 0; item = this._aData[i]; i++) {
            if (item[field] == o) {
                return extend({}, item);
            }
        }

        return null;
    };

    UI_LITE_TABLE_CLASS.setCellContent = function (x, y, content) {
        var tdList = this._eBody.getElementsByTagName('td');
        for (
            var i = 0, tdEl, pos, xy; 
            tdEl = tdList[i]; 
            i ++
        ) {
            if (pos = tdEl.getAttribute('cell-pos')) {
                xy = pos.split('-');
                if (x == xy[0] && y == xy[1]) {
                    tdEl.innerHTML = content;
                }
            }
        }
    };

    /**
     * 设置单元格高亮
     * @public
     * 
     * @param {number} x x坐标，缺省则意为一列
     * @param {number} y y坐标，缺省则意为一行
     * @param {boolean} isHighlight 是否高亮
     */    
    UI_LITE_TABLE_CLASS.setHighlight = function (x, y, isHighlight) {
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
     * 设置表格的列信息
     * @public
     * 
     * @param {Array} fields 列信息
     * @param {Boolean} isSilent 静默模式 如果true的话 不会立刻重绘表格 需要手动调用render
     * @param {Boolean} absoluteFields xy定位方式
     */
    UI_LITE_TABLE_CLASS.setFields = function (fields, isSilent, absoluteFields) {
        this._aFields = copyArray(fields);
        this._aAbsoluteFields = absoluteFields;

        !isSilent && this.render();
    };
    
    /**
     * 得到表格的列信息
     * @public
     * 
     * @param {boolean} getOrigin true则得到原对象，false或缺省则得到副本
     * @return {Array} fields 列信息
     */
    UI_LITE_TABLE_CLASS.getFields = function (getOrigin) {
        return getOrigin ? this._aFields : copyArray(this._aFields);
    };

    /**
     * 获取当前选择的行单选框value
     * @public
     */
    UI_LITE_TABLE_CLASS.getSelection = function () {
        var ids = [], i, item;

        for (i = 0; item = this._aCheckboxs[i]; i++) {
            item.checked && ids.push(item.value);
        }

        return ids;
    };

    /**
     * 设置空值时显示的文本
     * @public
     */
    UI_LITE_TABLE_CLASS.setEmptyText = function (text) {
        this._sEmptyText = text;
    };

    /**
     * 得到空值时显示的文本
     * @public
     */
    UI_LITE_TABLE_CLASS.getEmptyText = function () {
        return this._sEmptyText;
    };

    /**
     * 重新绘制表格
     * @public
     * 如果使用link，则生成的html中如此写：<a link="aaa">点击</a>
     * 然后监听LiteTable的oncelllink事件，参数为：
     *      {Object} item 表行对象
     *      {Object} field 表列对象
     *      {string} linkName 定义的链接名
     *      {number} x 表项的x位置
     *      {number} y 表项的y位置
     *      {HTMLElement} el 触发事件的dom
     */
    UI_LITE_TABLE_CLASS.render = function () {
        var type = this.getTypes()[0],
            width = ' width="100%" ',
            html = [],
            i, j, item, colspan, rowspan,
            fields = this._aFields, 
            absoluteFields = this._aAbsoluteFields, 
            datasource = this._aData;

        if (this._bFixMode) {
            width = '';
        } 

        html.push('<table cellpadding="0" cellspacing="0" ' + width + ' class="'+ type +'-table">');

        if (!fields || fields.length <= 0) {
            return;
        }

        // 渲染表头
        if (absoluteFields) {
            // 绝对定位模式
            for (i = 0; i < absoluteFields.length; i++) {
                html.push('<tr class="'+ type +'-head">');
                for (j = 0; j < absoluteFields[i].length; j++ ) {
                    if (!(item = absoluteFields[i][j]))  { continue; }
                    Array.prototype.push.apply(html, this.$renderHCell(item, j == 0));
                }
                html.push('</tr>');
            }
            
        } else {
            // 普通模式
            html.push('<tr class="'+ type +'-head">');
            for (i = 0; item = fields[i]; i++ ) {
                Array.prototype.push.apply(html, this.$renderHCell(item, i == 0));
            }
            html.push('</tr>');
        }

        // 渲染无数据表格
        if (!datasource || datasource.length <= 0) {
            html.push('<tr class="'+ type +'-row"><td colspan="'
                    + fields.length +'" class="'+ type +'-cell-empty">'+ this._sEmptyText +'</td></tr>');
        }
        else {
           html.push(buildTabeBody(fields, datasource, type));
        }

        html.push('</table>');

        this.setContent(html.join(''));

        // 重新捕获所有的行当选框
        this.$bindCheckbox();
        if (this._eCheckboxAll) {
            this.$refreshCheckbox();
        }

        // link的事件绑定
        this.$bindLink();
    };

    UI_LITE_TABLE_CLASS.$renderHCell = function (item, isLeft) {
        var type = this.getTypes()[0], html = [], item, className, colspan, rowspan;

        className = type + '-hcell';
        colspan = item.colspan ? ' colspan="' + item.colspan + '" ' : '';
        rowspan = item.rowspan ? ' rowspan="' + item.rowspan + '" ' : '';

        if (isLeft) {
            className += ' ' + type + '-hcell-left';
        }
        if (item.checkbox) {
            className += ' ' + type + '-hcell-checkbox';
            html.push('<th class="'+ className +'"><input type="checkbox" class="'+ type +'-checkbox-all" /></th>');

        } else {
            html.push('<th');
            if (item.width) {
                html.push(' style="width:' + item.width + 'px" ');
            }
            if (item.sortable) {
                className += ' ' + type + '-hcell-sort';
                if (item.field && item.field == this._sSortby) {
                    className += ' ' + type + '-hcell-sort-' + this._sOrderby;
                }
                html.push(' data-field="'+ item.field +'"');
                if (item.orderby) {
                    html.push(' data-orderby="' + item.orderby + '"');
                }
            }
            html.push(' ' + colspan + ' ' + rowspan + ' class="' + className + '">' + item.title + '</th>');
        }    
        return html;
    }; 

    /**
     * 获取表格当前所有行单选框的引用
     * @private
     */
    UI_LITE_TABLE_CLASS.$bindCheckbox = function () {
        var inputs = this.getBody().getElementsByTagName('input'),
            i, item, type = this.getTypes()[0];

        this._aCheckboxs = [];
        this._eCheckboxAll = null;

        for (i = 0; item = inputs[i]; i++) {
            if (item.type == 'checkbox' && item.className.indexOf(type + '-checkbox-all') >= 0) {
                this._eCheckboxAll = item;
            }
            else if (item.type == 'checkbox' && item.className.indexOf(type + '-checkbox') >= 0) {
                this._aCheckboxs.push(item);
            }
        }
    };

    /**
     * 绑定link的事件
     * @private
     */
    UI_LITE_TABLE_CLASS.$bindLink = function() {  
        var me = this;
        var tdList = this._eBody.getElementsByTagName('td');
        var onlink = function() {
            cellLinkHandler.call(this, me);
            return false;
        };
        for (
            var i = 0, tdEl, aList, pos, xy; 
            tdEl = tdList[i]; 
            i ++
        ) {
            aList = tdEl.getElementsByTagName('a');
            if (pos = tdEl.getAttribute('cell-pos')) {
                xy = pos.split('-');
                for (var j = 0, aEl, linkName; aEl = aList[j]; j ++) {
                    if (linkName = aEl.getAttribute('link')) {
                        aEl.setAttribute('link-pos-x', xy[0]);
                        aEl.setAttribute('link-pos-y', xy[1]);
                        aEl.onclick = onlink;
                    }
                }
            }
        }
    };

    /**
     * 刷新表格的行单选框
     * @private
     *
     * @param {Boolean} checked 全选/全不选 如果忽略此参数则根据当前表格的实际选择情况来设置“全选”的勾选状态
     */
    UI_LITE_TABLE_CLASS.$refreshCheckbox = function (checked) {
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

        this._eCheckboxAll.checked = checked !== undefined ? checked : newChecked;
    };

    /**
     * 触发表格events中定义的事件
     * @private
     *
     * @param {String} eventType 事件类型
     * @param {Event} nativeEvent 原生事件参数
     */
    UI_LITE_TABLE_CLASS.$fireEventHanlder = function (eventType, nativeEvent) {
        var handlers = getHanlderByType.call(this, eventType);
        var target = nativeEvent.targetElement;
        var elOuter = this.getOuter();

        while (target && target != elOuter && handlers.length > 0) {        
            for (var i = 0, item; item = handlers[i];) {
                if (checkElementBySelector(target, item.selector)) {
                    item.handler.call(target, nativeEvent, this);
                    handlers.splice(i, 1);
                }
                else {
                    i ++;
                }
            }
            target = target.parentNode;
        }
    };

    /**
     * @override
     */
    UI_LITE_TABLE_CLASS.$dispose = function () {
        this._aCheckboxs = [];
        this._eCheckboxAll = null;
        UI_CONTROL_CLASS.$dispose.call(this);
    };
})();
