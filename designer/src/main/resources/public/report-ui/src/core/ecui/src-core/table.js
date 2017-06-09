/*
Table - 定义由行列构成的表格的基本操作。
表格控件，继承自截面控件，对基本的 TableElement 功能进行了扩展，表头固定，不会随表格的垂直滚动条滚动而滚动，在行列滚动时，支持整行整列移动，允许直接对表格的数据进行增加/删除/修改操作。

表格控件直接HTML初始化的例子:
<div ecui="type:table">
  <table>
    <!-- 表头区域 -->
    <thead>
      <tr>
        <th style="width:200px;">公司名</th>
        <th style="width:200px;">url</th>
        <th style="width:250px;">地址</th>
        <th style="width:100px;">创办时间</th>
      </tr>
    </thead>
    <!-- 内容行区域 -->
    <tbody>
      <tr>
        <td>百度</td>
        <td>中国北京中关村</td>
        <td>1999</td>
      </tr>
    </tbody>
  </table>
</div>

属性
_aHCells     - 表格头单元格控件对象
_aRows       - 表格数据行对象
_uHead       - 表头区域

表头列属性
$$pos        - 列的坐标

行属性
$$pos        - 行的坐标
_aElements   - 行的列Element对象，如果当前列需要向左合并为null，需要向上合并为false
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
        DOCUMENT = document,
        MATH = Math,
        REGEXP = RegExp,
        MAX = MATH.max,
        MIN = MATH.min,

        USER_AGENT = navigator.userAgent,
        ieVersion = /msie (\d+\.\d)/i.test(USER_AGENT) ? DOCUMENT.documentMode || (REGEXP.$1 - 0) : undefined,

        indexOf = array.indexOf,
        children = dom.children,
        createDom = dom.create,
        first = dom.first,
        getPosition = dom.getPosition,
        getAttribute = dom.getAttribute,
        getParent = dom.getParent,
        insertBefore = dom.insertBefore,
        insertHTML = dom.insertHTML,
        next = dom.next,
        removeDom = dom.remove,
        trim = string.trim,
        extend = util.extend,
        toNumber = util.toNumber,
        getView = util.getView,

        $fastCreate = core.$fastCreate,
        disposeControl = core.dispose,
        getOptions = core.getOptions,
        inheritsControl = core.inherits,
        triggerEvent = core.triggerEvent,

        eventNames = [
            'mousedown', 'mouseover', 'mousemove', 'mouseout', 'mouseup',
            'click', 'dblclick', 'focus', 'blur', 'activate', 'deactivate',
            'keydown', 'keypress', 'keyup', 'mousewheel'
        ],

        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = UI_CONTROL.prototype,
        UI_SCROLLBAR_CLASS = ui.Scrollbar.prototype,
        UI_VSCROLLBAR = ui.VScrollbar,
        UI_PANEL = ui.Panel,
        UI_PANEL_CLASS = UI_PANEL.prototype;
//{/if}//
//{if $phase == "define"}//
    ///__gzip_original__UI_TABLE
    ///__gzip_original__UI_TABLE_CLASS
    /**
     * 初始化表格控件。
     * @public
     *
     * @param {Object} options 初始化选项
     */
    var UI_TABLE = ui.Table =
        inheritsControl(
            UI_PANEL,
            'ui-table',
            function (el, options) {
                var list, o,
                    type = this.getType();

                options.wheelDelta = 1;
                if (el.tagName == 'TABLE') {
                    var table = el;
                    insertBefore(el = createDom(table.className), table).appendChild(table);
                    if (options.width) {
                        el.style.width = options.width;
                    }
                    if (options.height) {
                        el.style.height = options.height;
                    }
                    table.className = '';
                }

                o = el.getElementsByTagName('TABLE')[0];
                list = children(o);

                o.setAttribute('cellSpacing', '0');

                if (list[0].tagName != 'THEAD') {
                    insertBefore(createDom('', '', 'thead'), list[0])
                        .appendChild(children(list[0])[0]);
                }
                
                return el;
            },
            function (el, options) {
                var i = 0,
                    type = this.getType(),
                    rows = this._aRows = [],
                    cols = this._aHCells = [],
                    colspans = [],
                    o = el.getElementsByTagName('TABLE')[0],
                    list = children(o),
                    j = list[0],
                    headRowCount = 1;

                o = children(list[0]);
                headRowCount = o.length;
                list = o.concat(children(list[1]));

                // 设置滚动条操作
                if (o = this.$getSection('VScrollbar')) {
                    o.setValue = UI_TABLE_SCROLL_SETVALUE;
                }
                if (o = this.$getSection('HScrollbar')) {
                    o.setValue = UI_TABLE_SCROLL_SETVALUE;
                }

                // 初始化表格区域
                o = createDom(type + '-head' + UI_CONTROL.TYPES, 'position:absolute;top:0px;overflow:hidden');
                o.innerHTML =
                    '<div style="white-space:nowrap;position:absolute"><table cellspacing="0"><tbody>' +
                        '</tbody></table></div>';
                (this._uHead = $fastCreate(UI_CONTROL, this.getMain().appendChild(o), this)).$setBody(j);

                // 以下初始化所有的行控件
                for (; o = list[i]; i++) {
                    o.className = trim(o.className) + this.Row.TYPES;
                    // list[i] 保存每一行的当前需要处理的列元素
                    list[i] = first(o);
                    colspans[i] = 1;
                    (rows[i] = $fastCreate(this.Row, o, this))._aElements = [];
                }

                for (j = 0; ; j++) {
                    for (i = 0; o = rows[i]; i++) {
                        if (colspans[i]-- > 1) {
                            continue;
                        }
                        if (el = list[i]) {
                            if (o._aElements[j] === undefined) {
                                o._aElements[j] = el;
                                // 当前元素处理完成，将list[i]指向下一个列元素
                                list[i] = next(el);

                                var rowspan = +getAttribute(el, 'rowSpan') || 1,
                                    colspan = colspans[i] = +getAttribute(el, 'colSpan') || 1;

                                while (rowspan--) {
                                    if (!rowspan) {
                                        colspan--;
                                    }
                                    for (o = colspan; o--; ) {
                                        rows[i + rowspan]._aElements.push(rowspan ? false : null);
                                    }
                                }
                            }
                        }
                        //如果此单元格是被行合并的，则继续处理下一个单元格
                        else if (o._aElements[j] === false) {
                            continue;
                        }
                        else {
                            // 当前行处理完毕，list[i]不再保存行内需要处理的下一个元素
                            for (j = 0; ; j++) {
                                // 以下使用 type 临时表示列的初始化参数
                                type = {};
                                for (i = 0; o = rows[i]; i++) {
                                    el = o._aElements[j];
                                    if (el === undefined) {
                                        this._aHeadRows = this._aRows.splice(0, headRowCount);
                                        return;
                                    }
                                    else if (el) {
                                        if (i < headRowCount) {
                                            extend(type, getOptions(el));
                                            el.className = trim(el.className) + this.HCell.TYPES;
                                            cols[j] = $fastCreate(this.HCell, el, this, { colIndex: j });
                                            cols[j]._oOptions = extend({}, type); //防止子列options影响父列
                                        }
                                        else {
                                            el.className =
                                                (trim(el.className) || type.primary || '') + this.Cell.TYPES;
                                            el.getControl = UI_TABLE_GETCONTROL();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ),
        UI_TABLE_CLASS = UI_TABLE.prototype,

        /**
         * 初始化表格控件的行部件。
         * @public
         *
         * @param {Object} options 初始化选项
         */
        UI_TABLE_ROW_CLASS = (UI_TABLE_CLASS.Row = inheritsControl(UI_CONTROL, 'ui-table-row')).prototype,

        /**
         * 初始化表格控件的列部件。
         * @public
         *
         * @param {Object} options 初始化选项
         */
        UI_TABLE_HCELL_CLASS = (UI_TABLE_CLASS.HCell = inheritsControl(
            UI_CONTROL, 
            'ui-table-hcell',
            function (el, options) {
                this.$$colIndex = options.colIndex;
            }
        )).prototype,

        /**
         * 初始化表格控件的单元格部件。
         * @public
         *
         * @param {Object} options 初始化选项
         */
        UI_TABLE_CELL_CLASS = (UI_TABLE_CLASS.Cell = inheritsControl(
            UI_CONTROL,
            'ui-table-cell',
            function (el, options) {
                // 单元格控件不能改变大小
                options.resizable = false;
                this.$$colIndex = options.colIndex;
            }
        )).prototype,

        /**
         * 在需要时初始化单元格控件。
         * 表格控件的单元格控件不是在初始阶段生成，而是在单元格控件第一次被调用时生成，参见核心的 getControl 方法。
         * @private
         *
         * @return {Function} 初始化单元格函数
         */
        UI_TABLE_GETCONTROL = ieVersion == 8 ? function (colIndex) {
            // 为了防止写入getControl属性而导致的reflow如此处理
            var control;
            return function () {
                return (control = control || UI_TABLE_CREATE_CELL(this, colIndex));
            };
        } : function () {
            return UI_TABLE_INIT_CELL;
        };
//{else}//
    /**
     * 初始化单元格。
     * @private
     *
     * @return {ecui.ui.Table.Cell} 单元格控件
     */
    function UI_TABLE_INIT_CELL(colIndex) {
        this.getControl = null;
        return UI_TABLE_CREATE_CELL(this, colIndex);
    }

    /**
     * 建立单元格控件。
     * @private
     *
     * @param {HTMLElement} main 单元格控件主元素
     * @return {ecui.ui.Table.Cell} 单元格控件
     */
    function UI_TABLE_CREATE_CELL(main, colIndex) {
        // 获取单元格所属的行控件
        var row = getParent(main).getControl(),
            table = row.getParent();

        return $fastCreate(
            table.Cell,
            main,
            row,
            extend(
                {colIndex: colIndex}, 
                table._aHCells[indexOf(row._aElements, main)]._oOptions
            )
        );
    }

    /**
     * 表格控件初始化一行。
     * @private
     *
     * @param {ecui.ui.Table.Row} row 行控件
     */
    function UI_TABLE_INIT_ROW(row) {
        for (var i = 0, list = row.getParent()._aHCells, el, o; o = list[i]; ) {
            if ((el = row._aElements[i++]) && el != o.getMain()) {
                o = o.getWidth() - o.getMinimumWidth();
                while (row._aElements[i] === null) {
                    o += list[i++].getWidth();
                }
                el.style.width = o + 'px';
            }
        }
    }

    /**
     * 表格控件改变显示区域值。
     * 表格控件改变显示区域时，每次尽量移动一个完整的行或列的距离。
     * @private
     *
     * @param {number} value 控件的当前值f
     */
    function UI_TABLE_SCROLL_SETVALUE(value) {
        //__gzip_original__length
        var i = 1,
            list = this.getParent()[this instanceof UI_VSCROLLBAR ? '_aRows' : '_aHCells'],
            length = list.length,
            oldValue = this.getValue();

        value = MIN(MAX(0, value), this.getTotal());

        if (value == oldValue) {
            return;
        }

        if (value > oldValue) {
            if (length == 1) {
                UI_SCROLLBAR_CLASS.setValue.call(this, this.getTotal());
                return;
            }
            for (; ; i++) {
                // 计算后移的新位置
                if (value <= list[i].$$pos) {
                    if (oldValue < list[i - 1].$$pos) {
                        i--;
                    }
                    break;
                }
            }
        }
        else {
            for (i = length; i--; ) {
                // 计算前移的新位置
                if (value >= list[i].$$pos) {
                    if (i < length - 1 && oldValue > list[i + 1].$$pos) {
                        i++;
                    }
                    break;
                }
            }
        }

        UI_SCROLLBAR_CLASS.setValue.call(this, list[i].$$pos);
    }

    /**
     * @override
     */
    UI_TABLE_ROW_CLASS.$dispose = function () {
        this._aElements = null;
        UI_CONTROL_CLASS.$dispose.call(this);
    };

    /**
     * 获取一行内所有单元格的主元素。
     * $getElement 方法返回的主元素数组可能包含 false/null 值，分别表示当前单元格被向上或者向左合并。
     * @protected
     *
     * @return {Array} 主元素数组
     */
    UI_TABLE_ROW_CLASS.$getElements = function () {
        return this._aElements.slice();
    };

    /**
     * @override
     */
    UI_TABLE_ROW_CLASS.$hide = function () {
        var i = 0,
            table = this.getParent(),
            index = indexOf(table._aRows, this),
            nextRow = table._aRows[index + 1],
            j,
            cell,
            o;

        for (; table._aHCells[i]; i++) {
            o = this._aElements[i];
            if (o === false) {
                o = table.$getElement(index - 1, i);
                // 如果单元格向左被合并，cell == o
                if (cell != o) {
                    o.setAttribute('rowSpan', +getAttribute(o, 'rowSpan') - 1);
                    cell = o;
                }
            }
            else if (o && (j = +getAttribute(o, 'rowSpan')) > 1) {
                // 如果单元格包含rowSpan属性，需要将属性添加到其它行去
                o.setAttribute('rowSpan', j - 1);
                for (j = i + 1; ; ) {
                    cell = nextRow._aElements[j++];
                    if (cell || cell === undefined) {
                        break;
                    }
                }

                o.getControl().$setParent(nextRow);
                nextRow.getBody().insertBefore(o, cell || null);
            }
        }

        UI_CONTROL_CLASS.$hide.call(this);
        table.repaint();
    };

    /**
     * @override
     */
    UI_TABLE_ROW_CLASS.$show = function () {
        var i = 0,
            table = this.getParent(),
            index = indexOf(table._aRows, this),
            nextRow = table._aRows[index + 1],
            j,
            cell,
            o;

        for (; table._aHCells[i]; i++) {
            o = this._aElements[i];
            if (o === false) {
                o = table.$getElement(index - 1, i);
                // 如果单元格向左被合并，cell == o
                if (cell != o) {
                    o.setAttribute('rowSpan', +getAttribute(o, 'rowSpan') + 1);
                    cell = o;
                }
            }
            else if (o && nextRow && nextRow._aElements[i] === false) {
                // 如果单元格包含rowSpan属性，需要从其它行恢复
                o.setAttribute('rowSpan', +getAttribute(o, 'rowSpan') + 1);
                for (j = i + 1; ; ) {
                    cell = this._aElements[j++];
                    if (cell || cell === undefined) {
                        break;
                    }
                }

                o.getControl().$setParent(this);
                this.getBody().insertBefore(o, cell || null);
            }
        }

        UI_CONTROL_CLASS.$show.call(this);
        table.resize();
    };

    /**
     * 获取单元格控件。
     * @public
     *
     * @param {number} colIndex 列序号，从0开始
     * @return {ecui.ui.Table.Cell} 单元格控件
     */
    UI_TABLE_ROW_CLASS.getCell = function (colIndex) {
        return this._aElements[colIndex] ? this._aElements[colIndex].getControl(colIndex) : null;
    };

    /**
     * 获取全部单元格控件。
     * @public
     *
     * @return {Array} 单元格控件数组
     */
    UI_TABLE_ROW_CLASS.getCells = function () {
        for (var i = this._aElements.length, result = []; i--; ) {
            result[i] = this.getCell(i);
        }
        return result;
    };

    /**
     * @override
     */
    UI_TABLE_ROW_CLASS.$cache = function (style, cacheSize) {
        UI_CONTROL_CLASS.$cache.call(this, style, cacheSize);

        // 太耗时了，用宽表头的方式凑合吧
        // if (ieVersion < 8) {
            // for fakeDom...
            // for (var i = 0, c; c = this.getCell(i); i ++) {
                // c.cache(style, cacheSize);
            // }
        // }
    };    

    /**
     * @override
     */
    UI_TABLE_ROW_CLASS.setSize = function (width, height) {
        for (var i = this._aElements.length, oldHeight = this.getHeight(); i--; ) {
            if (this._aElements[i]) {
                this._aElements[i].getControl().$setSize(null, height);
            }
        }
        this.getParent()[height > oldHeight ? 'resize' : 'repaint']();
    };

    /**
     * @override
     */
    UI_TABLE_HCELL_CLASS.$hide = function () {
        this.$setStyles('display', 'none', -this.getWidth());
    };

    /**
     * 设置整列的样式。
     * $setStyles 方法批量设置一列所有单元格的样式。
     * @protected
     *
     * @param {string} name 样式的名称
     * @param {string} value 样式的值
     * @param {number} widthRevise 改变样式后表格宽度的变化，如果省略表示没有变化
     */
    UI_TABLE_HCELL_CLASS.$setStyles = function (name, value, widthRevise) {
        //__gzip_original__cols
        var i = 0,
            table = this.getParent(),
            rows = table._aHeadRows.concat(table._aRows),
            body = this.getBody(),
            cols = table._aHCells,
            index = indexOf(cols, this),
            o = getParent(getParent(getParent(body))).style,
            j;

        body.style[name] = value;
        if (widthRevise) {
            o.width = first(table.getBody()).style.width = toNumber(o.width) + widthRevise + 'px';
        }

        for (; o = rows[i++]; ) {
            // 以下使用 body 表示列元素列表
            body = o._aElements;
            o = body[index];
            if (o) {
                o.style[name] = value;
            }
            if (widthRevise && o !== false) {
                for (j = index; !(o = body[j]); j--) {}

                var width = -cols[j].getMinimumWidth(),
                    colspan = 0;

                do {
                    if (!cols[j].getOuter().style.display) {
                        width += cols[j].getWidth();
                        colspan++;
                    }
                }
                while (body[++j] === null);

                if (width > 0) {
                    o.style.display = '';
                    o.style.width = width + 'px';
                    o.setAttribute('colSpan', colspan);
                }
                else {
                    o.style.display = 'none';
                }
            }
        }
        if (widthRevise > 0) {
            table.resize();
        }
        else {
            table.repaint();
        }
    };

    /**
     * @override
     */
    UI_TABLE_HCELL_CLASS.$show = function () {
        this.$setStyles('display', '', this.getWidth());
    };

    /**
     * 获取单元格控件。
     * @public
     *
     * @param {number} rowIndex 行序号，从0开始
     * @return {ecui.ui.Table.Cell} 单元格控件
     */
    UI_TABLE_HCELL_CLASS.getCell = function (rowIndex) {
        return this.getParent().getCell(rowIndex, indexOf(this._aHCells, this));
    };

    /**
     * 获取全部单元格控件。
     * @public
     *
     * @return {Array} 单元格控件数组
     */
    UI_TABLE_HCELL_CLASS.getCells = function () {
        for (var i = 0, index = indexOf(this.getParent()._aHCells, this), o, result = []; o = this.getParent()._aRows[i]; ) {
            result[i++] = o.getCell(index);
        }
        return result;
    };

    /**
     * @override
     */
    UI_TABLE_HCELL_CLASS.$cache = function (style, cacheSize) {
        UI_CONTROL_CLASS.$cache.call(this, style, cacheSize);

        this.getParent().$cacheByFakeColMaxWidth(this);
    };    

    /**
     * @override
     */
    UI_TABLE_HCELL_CLASS.setSize = function (width) {
        var oldWidth = this.getWidth();
        // 首先对列表头控件设置宽度，否则在计算合并单元格时宽度可能错误
        this.$setSize(width);
        this.$setStyles('width', width - this.$getBasicWidth() + 'px', width - oldWidth);
    };

    /**
     * @override
     */
    UI_TABLE_CELL_CLASS.$cache = function (style, cacheSize) {
        UI_CONTROL_CLASS.$cache.call(this, style, cacheSize);

        this.getParent().getParent().$cacheByFakeColMaxWidth(this);    
    };    

    /**
     * @override
     */
    UI_TABLE_CELL_CLASS.getHeight = function () {
        return this.getOuter().offsetHeight;
    };

    /**
     * @override
     */
    UI_TABLE_CELL_CLASS.getWidth = function () {
        return this.getOuter().offsetWidth;
    };

    /**
     * @override
     */
    UI_TABLE_CLASS.$cache = function (style, cacheSize) {
        UI_PANEL_CLASS.$cache.call(this, style, cacheSize);

        this._uHead.cache(false, true);

        // 以下使用 style 表示临时对象 o
        this.$$paddingTop = this._uHead.getBody().offsetHeight;

        for (var i = 0, pos = 0; style = this._aRows[i++]; ) {
            style.$$pos = pos;
            style.cache(true, true);
            if (!style.getOuter().style.display) {
                pos += style.getHeight();
            }
        }
        for (i = 0, pos = 0; style = this._aHCells[i++]; ) {
            style.$$pos = pos;
            style.cache(true, true);
            if (!style.getOuter().style.display) {
                pos += style.getWidth();
            }
        }
        this.$$mainWidth = pos;

        // for (i = 0, pos = 0; style = this._aHCells[i++]; ) {
            // style.getOuter().width = style.$$width + 'px';
        // }
        // 重新
        // UI_PANEL_CLASS.$cache.call(this, style, cacheSize);
        // this._uHead.cache(false, true);        
    };

    /**
     * 获取单元格主元素。
     * $getElement 方法在合法的行列序号内一定会返回一个 Element 对象，如果当前单元格被合并，将返回合并后的 Element 对象。
     * @protected
     *
     * @param {number} rowIndex 单元格的行数，从0开始
     * @param {number} colIndex 单元格的列数，从0开始
     * @return {HTMLElement} 单元格主元素对象
     */
    UI_TABLE_CLASS.$getElement = function (rowIndex, colIndex) {
        //__gzip_original__rows
        var rows = this._aRows,
            cols = rows[rowIndex] && rows[rowIndex]._aElements,
            col = cols && cols[colIndex];

        if (col === undefined) {
            col = null;
        }
        else if (!col) {
            for (; col === false; col = (cols = rows[--rowIndex]._aElements)[colIndex]) {}
            for (; !col; col = cols[--colIndex]) {}
        }
        return col;
    };

    /**
     * 页面滚动事件的默认处理。
     * @protected
     */
    UI_TABLE_CLASS.$pagescroll = function () {
        UI_PANEL_CLASS.$pagescroll.call(this);
        if (!this._uVScrollbar) {
            this._uHead.getOuter().style.top =
                MAX(getView().top - getPosition(this.getOuter()).top, 0) + 'px';
        }
    };

    /**
     * @override
     */
    UI_TABLE_CLASS.$scroll = function () {
        UI_PANEL_CLASS.$scroll.call(this);
        this._uHead.getMain().lastChild.style.left = this.getBody().style.left;
    };

    /**
     * @override
     */
    UI_TABLE_CLASS.$setSize = function (width, height) {
        var body = this.getBody(),
            vscroll = this.$getSection('VScrollbar'),
            hscroll = this.$getSection('HScrollbar'),
            mainWidth = this.$$mainWidth,
            mainHeight = this.$$mainHeight,
            vsWidth = vscroll && vscroll.getWidth(),
            hsHeight = hscroll && hscroll.getHeight(),
            basicWidth = this.$getBasicWidth(),
            basicHeight = this.$getBasicHeight(),
            mainWidthRevise = mainWidth + basicWidth,
            mainHeightRevise = mainHeight + basicHeight,
            bodyWidth = width - basicWidth,
            bodyHeight = height - basicHeight,
            o;

        this.getMain().style.paddingTop = this.$$paddingTop + 'px';
        first(body).style.width = this._uHead.getMain().lastChild.lastChild.style.width = mainWidth + 'px';
        console.log(this._uHead.getMain().lastChild.lastChild);

        // 计算控件的宽度与高度自动扩展
        if (mainWidth <= bodyWidth && mainHeight <= bodyHeight) {
            width = mainWidthRevise;
            height = mainHeightRevise;
        }
        else if (!(vscroll && hscroll &&
            mainWidth > bodyWidth - vsWidth && mainHeight > bodyHeight - hsHeight)
        ) {
            o = mainWidthRevise + (!vscroll || bodyHeight >= mainHeight ? 0 : vsWidth);
            width = hscroll ? MIN(width, o) : o;
            o = mainHeightRevise + (!hscroll || bodyWidth >= mainWidth ? 0 : hsHeight);
            height = vscroll ? MIN(height, o) : o;
        }

        UI_PANEL_CLASS.$setSize.call(this, width, height);

        this._uHead.$setSize(toNumber(getParent(body).style.width) + this._uHead.$getBasicWidth(), this.$$paddingTop);
    };

    /**
     * 增加一列。
     * options 对象对象支持的属性如下：
     * width   {number} 列的宽度
     * primary {string} 列的基本样式
     * title   {string} 列的标题
     * @public
     *
     * @param {Object} options 列的初始化选项
     * @param {number} index 被添加的列的位置序号，如果不合法将添加在末尾
     * @return {ecui.ui.Table.HCell} 表头单元格控件
     */
    UI_TABLE_CLASS.addColumn = function (options, index) {
        var i = 0,
            headRowCount = this._aHeadRows.length,
            rows = this._aHeadRows.concat(this._aRows),
            primary = options.primary || '',
            el = createDom(primary + this.HCell.TYPES, '', 'td'),
            col = $fastCreate(this.HCell, el, this),
            row,
            o;

        el.innerHTML = options.title || '';

        primary += this.Cell.TYPES;
        for (; row = rows[i]; i++) {
            o = row._aElements[index];
            if (o !== null) {
                // 没有出现跨列的插入列操作
                for (j = index; !o; ) {
                    o = row._aElements[++j];
                    if (o === undefined) {
                        break;
                    }
                }
                if (i < headRowCount) {
                    row._aElements.splice(index, 0, row.getBody().insertBefore(el, o));
                    el.setAttribute('rowSpan', headRowCount - i);
                    this._aHCells.splice(index, 0, col);
                    i = headRowCount - 1;
                }
                else {
                    row._aElements.splice(index, 0, o = row.getBody().insertBefore(createDom(primary, '', 'td'), o));
                    o.getControl = UI_TABLE_GETCONTROL();
                }
            }
            else {
                // 出现跨列的插入列操作，需要修正colspan的属性值
                var cell = this.$getElement(i - headRowCount, index),
                    j = +getAttribute(cell, 'rowspan') || 1;

                cell.setAttribute('colSpan', +getAttribute(cell, 'colSpan') + 1);
                row._aElements.splice(index, 0, o);
                for (; --j; ) {
                    rows[++i]._aElements.splice(index, 0, false);
                }
            }
        }

        col.cache();
        col.$setSize(options.width);
        col.$setStyles('width', el.style.width, options.width);
        col._oOptions = extend({}, options);

        return col;
    };

    /**
     * 增加一行。
     * @public
     *
     * @param {Array} data 数据源(一维数组)
     * @param {number} index 被添加的行的位置序号，如果不合法将添加在最后
     * @return {ecui.ui.Table.Row} 行控件
     */
    UI_TABLE_CLASS.addRow = function (data, index) {
        var i = 0,
            j = 1,
            body = this.getBody().lastChild.lastChild,
            el = createDom(),
            html = ['<table><tbody><tr class="' + this.Row.TYPES + '">'],
            rowCols = [],
            row = this._aRows[index],
            col;

        if (!row) {
            index = this._aRows.length;
        }

        for (; col = this._aHCells[i]; ) {
            if (row && row._aElements[i] === false || data[i] === false) {
                rowCols[i++] = false;
            }
            else {
                // 如果部分列被隐藏，colspan/width 需要动态计算
                rowCols[i] = true;
                html[j++] = '<td class="' + this.Cell.TYPES + '" style="';
                for (
                    var o = i,
                        colspan = col.isShow() ? 1 : 0,
                        width = col.getWidth() - col.getMinimumWidth();
                    (col = this._aHCells[++i]) && data[i] === null;
                ) {
                    rowCols[i] = null;
                    if (col.isShow()) {
                        colspan++;
                        width += col.getWidth();
                    }
                }
                rowCols[o] = true;
                html[j++] = (colspan ? 'width:' + width + 'px" colSpan="' + colspan : 'display:none') + '">' +
                    (data[o] || '') + '</td>';
            }
        }

        html[j] = '</tr></tbody></table>';
        el.innerHTML = html.join('');
        el = el.lastChild.lastChild.lastChild;

        body.insertBefore(el, row ? row.getOuter() : null);
        row = $fastCreate(this.Row, el, this);
        this._aRows.splice(index--, 0, row);

        // 以下使用 col 表示上一次执行了rowspan++操作的单元格，同一个单元格只需要增加一次
        for (i = 0, el = el.firstChild, col = null; this._aHCells[i]; i++) {
            if (o = rowCols[i]) {
                rowCols[i] = el;
                el.getControl = UI_TABLE_GETCONTROL();
                el = el.nextSibling;
            }
            else if (o === false) {
                o = this.$getElement(index, i);
                if (o != col) {
                    o.setAttribute('rowSpan', (+getAttribute(o, 'rowSpan') || 1) + 1);
                    col = o;
                }
            }
        }

        row._aElements = rowCols;
        this.resize();
        return row;
    };

    /**
     * 获取单元格控件。
     * @public
     *
     * @param {number} rowIndex 行序号，从0开始
     * @param {number} colIndex 列序号，从0开始
     * @return {ecui.ui.Table.Cell} 单元格控件
     */
    UI_TABLE_CLASS.getCell = function (rowIndex, colIndex) {
        rowIndex = this._aRows[rowIndex];
        return rowIndex && rowIndex.getCell(colIndex) || null;
    };

    /**
     * 获取表格列的数量。
     * @public
     *
     * @return {number} 表格列的数量
     */
    UI_TABLE_CLASS.getColumnCount = function () {
        return this._aHCells.length;
    };

    /**
     * 获取表头单元格控件。
     * 表头单元格控件提供了一些针对整列进行操作的方法，包括 hide、setSize(仅能设置宽度) 与 show 方法等。
     * @public
     *
     * @param {number} index 列序号，从0开始
     * @return {ecui.ui.Table.HCell} 表头单元格控件
     */
    UI_TABLE_CLASS.getHCell = function (index) {
        return this._aHCells[index] || null;
    };

    /**
     * 获取全部的表头单元格控件。
     * @public
     *
     * @return {Array} 表头单元格控件数组
     */
    UI_TABLE_CLASS.getHCells = function () {
        return this._aHCells.slice();
    };

    /**
     * 获取行控件。
     * @public
     *
     * @param {number} index 行数，从0开始
     * @return {ecui.ui.Table.Row} 行控件
     */
    UI_TABLE_CLASS.getRow = function (index) {
        return this._aRows[index] || null;
    };

    /**
     * 获取表格行的数量。
     * @public
     *
     * @return {number} 表格行的数量
     */
    UI_TABLE_CLASS.getRowCount = function () {
        return this._aRows.length;
    };

    /**
     * 获取全部的行控件。
     * @public
     *
     * @return {Array} 行控件列表
     */
    UI_TABLE_CLASS.getRows = function () {
        return this._aRows.slice();
    };

    /**
     * @override
     */
    UI_TABLE_CLASS.init = function () {
        insertBefore(this._uHead.getBody(), this._uHead.getMain().lastChild.lastChild.firstChild);
        this.$$mainHeight -= this.$$paddingTop;

        UI_PANEL_CLASS.init.call(this);

        for (var i = 0, o; o = this._aHCells[i++]; ) {
            o.$setSize(o.getWidth());
        }
        for (i = 0; o = this._aHeadRows[i++]; ) {
            UI_TABLE_INIT_ROW(o);
        }
        for (i = 0; o = this._aRows[i++]; ) {
            UI_TABLE_INIT_ROW(o);
        }
    };

    /**
     * 移除一列并释放占用的空间。
     * @public
     *
     * @param {number} index 列序号，从0开始计数
     */
    UI_TABLE_CLASS.removeColumn = function (index) {
        var i = 0,
            cols = this._aHCells,
            o = cols[index];

        if (o) {
            o.hide();

            removeDom(o.getOuter());
            disposeControl(o);
            cols.splice(index, 1);

            for (; o = this._aRows[i++]; ) {
                cols = o._aElements;
                if (o = cols[index]) {
                    if (cols[index + 1] === null) {
                        // 如果是被合并的列，需要保留
                        cols.splice(index + 1, 1);
                        continue;
                    }
                    removeDom(o);
                    if (o.getControl != UI_TABLE_GETCONTROL()) {
                        disposeControl(o.getControl());
                    }
                }
                cols.splice(index, 1);
            }
        }
    };

    /**
     * 移除一行并释放占用的空间。
     * @public
     *
     * @param {number} index 行序号，从0开始计数
     */
    UI_TABLE_CLASS.removeRow = function (index) {
        var i = 0,
            row = this._aRows[index],
            rowNext = this._aRows[index + 1],
            body = row.getBody(),
            o;

        if (row) {
            row.hide();
            for (; this._aHCells[i]; i++) {
                if (o = row._aElements[i]) {
                    if (getParent(o) != body) {
                        rowNext._aElements[i] = o;
                        for (; row._aElements[++i] === null; ) {
                            rowNext._aElements[i] = null;
                        }
                        i--;
                    }
                }
            }

            removeDom(row.getOuter());
            disposeControl(row);
            this._aRows.splice(index, 1);

            this.repaint();
        }
    };

    /**
     * @private
     *
     * 很难看的hack，但是不知道怎么办
     * 报表平台的表格希望根据单元格内容决定列宽度，而不好设死宽度。
     * 但是ie67下，无论我怎么设，表格的宽度都根据外层决定，
     * 然后列宽类似均分。
     * 谁有好办法？？？
     * 注意，得到的fakedom只能立即用，不要存着。
     */    
    UI_TABLE_CLASS.$getFakeDom = function () {
        var me = this;

        var fakeDom = this.$$fakeDom;
        var style;
        if (!fakeDom) {
            fakeDom = this.$$fakeDom = document.createElement('div');
            style = fakeDom.style;
            style.position = 'absolute';
            style.top = '0';
            style.left = '-10000px';
            style.visibility = 'hidden';
            document.body.appendChild(fakeDom);
        }

        // 清除
        if (!this.$$remover) {
            this.$$remover = setTimeout(function () {
                document.body.removeChild(fakeDom);
                fakeDom = me.$$fakeDom = style = me.$$remover = null;
            }, 0);
        }
        return fakeDom;
    };

    /**
     * 得到列的最大宽度，参见 $getFakeDom
     *
     * @private     
     * @param {Object} cell 必须是cache过的cell
     */    
    UI_TABLE_CLASS.$cacheByFakeColMaxWidth = function (cell) {
        if (ieVersion < 8) {

            var map = this.$$fakeColMaxWidthMap;
            if (!map) {
                map = this.$$fakeColMaxWidthMap = {};
            }
            var colIndex = cell.$$colIndex;

            var fakeDom = this.$getFakeDom();
            fakeDom.innerHTML = cell.getOuter().innerHTML;

            var width = fakeDom.offsetWidth 
                + (core.isContentBox() ? cell.$getBasicWidth() : 0); 

            var maxWidth;
            if (!isFakeColIgnoreCell(cell)) { 
                // 取该列最宽的cell的宽度
                maxWidth = map[colIndex];
                if (!maxWidth || width > maxWidth) {
                    map[colIndex] = maxWidth = width;
                }
            }
            else {
                maxWidth = width;
            }

            // 为该列所有cell重设$$width
            if (!isFakeColIgnoreCell(cell)) {
                for (var i = 0, row; row = this._aRows[i]; i ++) {
                    var c = row.getCell(colIndex);
                    if (c && !isFakeColIgnoreCell(c)) {
                        c.$$width = maxWidth;
                    }                
                }
                for (var i = 0, row; row = this._aHeadRows[i]; i ++) {
                    var c = row.getCell(colIndex);
                    if (c && !isFakeColIgnoreCell(c)) {
                       c.$$width = maxWidth;
                    }
                }
            }
        }
    };

    function isFakeColIgnoreCell(cell) {
        var colspan = cell.__$$colspan; // 访问次数多，缓存一下
        if (colspan == null) {
            colspan = cell.__$$colspan = cell._eMain.getAttribute('colspan');
        }
        return colspan > 1;
    }

    // 初始化事件转发信息
    (function () {
        function build(name) {
            var type = name.replace('mouse', '');

            name = '$' + name;

            UI_TABLE_ROW_CLASS[name] = function (event) {
                UI_CONTROL_CLASS[name].call(this, event);
                triggerEvent(this.getParent(), 'row' + type, event);
            };

            UI_TABLE_CELL_CLASS[name] = function (event) {
                UI_CONTROL_CLASS[name].call(this, event);
                triggerEvent(this.getParent().getParent(), 'cell' + type, event);
            };
        }

        for (var i = 0; i < 7; ) {
            build(eventNames[i++]);
        }
    })();
//{/if}//
//{if 0}//
})();
//{/if}//
