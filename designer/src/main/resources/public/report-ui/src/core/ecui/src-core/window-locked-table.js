/*
修改版的LockedTable，为性能而优化。

LockedTable - 定义允许左右锁定若干列显示的高级表格的基本操作。
允许锁定左右两列的高级表格控件，继承自表格控件，内部包含两个部件——锁定的表头区(基础控件)与锁定的行内容区(基础控件)。

锁定列高级表格控件直接HTML初始化的例子:
<div ecui="type:locked-table;left-lock:2;right-lock:1">
    <table>
        <!-- 当前节点的列定义，如果有特殊格式，需要使用width样式 -->
        <thead>
            <tr>
                <th>标题</th>
                ...
            </tr>
        </thead>
        <tbody>
            <!-- 这里放单元格序列 -->
            <tr>
                <td>单元格一</td>
                ...
            </tr>
            ...
        </tbody>
    </table>
</div>

属性
_nLeft       - 最左部未锁定列的序号
_nRight      - 最右部未锁定列的后续序号，即未锁定的列序号+1
_aLockedRow  - 用于显示锁定区域的行控件数组
_uLockedHead - 锁定的表头区
_uLockedMain - 锁定的行内容区

表格行与锁定行属性
_eFill       - 用于控制中部宽度的单元格
*/
//{if 0}//
(function () {

    var core = ecui,
        array = core.array,
        dom = core.dom,
        ui = core.ui,
        util = core.util,

        MATH = Math,
        MAX = MATH.max,
        REGEXP = RegExp,
        USER_AGENT = navigator.userAgent,

        indexOf = array.indexOf,
        children = dom.children,
        createDom = dom.create,
        getParent = dom.getParent,
        getAttribute = dom.getAttribute,
        insertBefore = dom.insertBefore,
        insertAfter = dom.insertAfter,
        removeDom = dom.remove,
        blank = util.blank,
        toNumber = util.toNumber,

        $fastCreate = core.$fastCreate,
        disposeControl = core.dispose,
        $bind = core.$bind,
        inheritsControl = core.inherits,

        firefoxVersion = /firefox\/(\d+\.\d)/i.test(USER_AGENT) ? REGEXP.$1 - 0 : undefined

        eventNames = [
            'mousedown', 'mouseover', 'mousemove', 'mouseout', 'mouseup',
            'click', 'dblclick', 'focus', 'blur', 'activate', 'deactivate',
            'keydown', 'keypress', 'keyup', 'mousewheel'
        ],

        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = UI_CONTROL.prototype,
        UI_TABLE = ui.Table,
        UI_TABLE_CLASS = UI_TABLE.prototype,
        UI_TABLE_ROW = UI_TABLE_CLASS.Row,
        UI_TABLE_ROW_CLASS = UI_TABLE_ROW.prototype;
//{/if}//
//{if $phase == "define"}//
    /**
     * 初始化高级表格控件。
     * options 对象支持的属性如下：
     * left-lock  左边需要锁定的列数
     * right-lock 右边需要锁定的列数
     * @public
     *
     * @param {Object} options 初始化选项
     */
    //__gzip_original__UI_LOCKED_TABLE
    //__gzip_original__UI_LOCKED_TABLE_ROW
    var UI_LOCKED_TABLE = ui.WindowLockedTable =
        inheritsControl(
            UI_TABLE,
            '*locked-table',
            null,
            function (el, options) {

                // ==========================
                // var ddd = new Date();                

                var i = 0,
                    type = this.getType(),
                    headRows = this._aHeadRows,
                    rows = headRows.concat(this._aRows),
                    lockedEl = createDom('', 'position:absolute;top:0px;left:0px;overflow:hidden'),
                    list = [],
                    lockedRows = this._aLockedRow = [],
                    lockedHeadRows = this._aLockedHeadRow = [],
                    o;

                this._nLeft = options.leftLock || 0;
                this._nRight = this.getColumnCount() - (options.rightLock || 0);

                // 以下使用 options 代替 rows
                for (; el = rows[i]; ) {
                    el = el.getMain();
                    list[i++] =
                        '<tr class="' + el.className + '" style="' + el.style.cssText +
                            '"><td style="padding:0px;border:0px"></td></tr>';
                }

                lockedEl.innerHTML = ''
                    + '<div class="' + type + '-locked-head" style="position:absolute;top:0px;left:0px">'
                        + '<div style="white-space:nowrap;position:absolute">' 
                            + '<table cellspacing="0"><thead>' + list.splice(0, headRows.length).join('') + '</thead></table>' 
                        + '</div>'
                    + '</div>'
                    + '<div class="' + type + '-locked-layout" style="position:absolute;left:0px;overflow:hidden">'
                        + '<div style="white-space:nowrap;position:absolute;top:0px;left:0px">'
                            + '<table cellspacing="0"><tbody>' + list.join('') + '</tbody></table>'
                        + '</div>'
                    + '</div>';

                // 初始化锁定的表头区域，以下使用 list 表示临时变量
                o = this._uLockedHead = $fastCreate(UI_CONTROL, lockedEl.firstChild, this);
                o.$setBody(el = o.getMain().lastChild.lastChild.firstChild);

                for (i = 0, list = children(el); o = list[i]; ) {
                    lockedHeadRows[i] = UI_LOCKED_TABLE_CREATE_LOCKEDROW(o, headRows[i++]);
                }

                o = this._uLockedMain = $fastCreate(UI_CONTROL, el = lockedEl.lastChild, this);
                o.$setBody(el = el.lastChild);

                // 增加占位dom
                // 增加占位行
                if (this._aRows.length > 0) {
                    o = createDom();
                    o.innerHTML = '<tr data-placeholder="top" style="height:0"><td>&nbsp;</td></tr>';
                    insertBefore(o.firstChild, this._aRows[0].getOuter());
                    o.innerHTML = '<tr data-placeholder="bottom" style="height:0"><td>&nbsp;</td></tr>';
                    insertAfter(o.firstChild, this._aRows[this._aRows.length - 1].getOuter());
                }

                for (i = 0, list = children(el.lastChild.lastChild); o = list[i]; ) {
                    lockedRows[i] = UI_LOCKED_TABLE_CREATE_LOCKEDROW(o, this._aRows[i++]);
                }
                insertBefore(lockedEl.firstChild, this._uHead.getOuter());
                insertBefore(lockedEl.firstChild, getParent(this.getBody()));

                // 增加占位dom
                // 增加占位行
                if (lockedRows.length > 0) {
                    o = createDom();
                    o.innerHTML = '<tr data-locked-placeholder="top" style="height:0"><td>&nbsp;</td></tr>';
                    insertBefore(o.firstChild, lockedRows[0].getOuter());
                    o.innerHTML = '<tr data-locked-placeholder="bottom" style="height:0"><td>&nbsp;</td></tr>';
                    insertAfter(o.firstChild, lockedRows[lockedRows.length - 1].getOuter());
                }

                // console.log('=================== locked-table constructor] ' + ((new Date()).getTime() - ddd));
                // ddd = new Date();    

                this._oWin = {
                    total: options.winTotal,
                    top: 0,
                    bottom: this._aRows.length
                };
            }
        );
        UI_LOCKED_TABLE_CLASS = UI_LOCKED_TABLE.prototype,

        /**
         * 初始化高级表格控件的行部件。
         * @public
         *
         * @param {Object} options 初始化选项
         */
        UI_LOCKED_TABLE_ROW_CLASS = (UI_LOCKED_TABLE_CLASS.Row = inheritsControl(UI_TABLE_CLASS.Row)).prototype;
//{else}//
    /**
     * 建立锁定行控件。
     * @private
     *
     * @param {HTMLElement} el 锁定行的 Element 元素
     * @param {ecui.ui.Table.Row} row 表格基本行控件
     */
    function UI_LOCKED_TABLE_CREATE_LOCKEDROW(el, row) {
        $bind(el, row);
        row._eFill = el.lastChild;

        return row;
    }

    /**
     * 拆分行内的单元格到锁定列或基本列中。
     * @private
     *
     * @param {ecui.ui.LockedTable.LockedHead|ecui.ui.LockedTable.LockedRow} locked 锁定表头控件或者锁定行控件
     */
    function UI_LOCKED_TABLE_ROW_SPLIT(locked) {
        var i = 0,
            table = locked.getParent(),
            cols = table.getHCells(),
            list = locked.$getElements(),
            baseBody = locked.getBody(),
            lockedBody = getParent(locked._eFill),
            el = lockedBody.firstChild,
            o;

        for (; cols[i]; ) {
            if (i == table._nLeft) {
                el = baseBody.firstChild;
            }
            if (o = list[i++]) {
                if (el != o) {
                    (i <= table._nLeft || i > table._nRight ? lockedBody : baseBody).insertBefore(o, el);
                }
                else {
                    el = el.nextSibling;
                }
            }
            if (i == table._nRight) {
                el = locked._eFill.nextSibling;
            }
        }
    }

    /**
     * 拆分所有行内的单元格到锁定列或基本列中。
     * @private
     *
     * @param {ecui.ui.LockedTable} table 锁定式表格控件
     */
    function UI_LOCKED_TABLE_ALL_SPLIT(table) {
        for (var i = 0, o; o = table._aLockedHeadRow[i++]; ) {
            UI_LOCKED_TABLE_ROW_SPLIT(o);
        }
        for (var i = 0, o; o = table._aLockedRow[i++]; ) {
            if (!o.getOuter().getAttribute('data-locked-placeholder')) {
                UI_LOCKED_TABLE_ROW_SPLIT(o);
            }
        }
    }

    /**
     * @override
     */
    UI_LOCKED_TABLE_ROW_CLASS.$dispose = function () {
        this._eFill = null;
        UI_TABLE_ROW_CLASS.$dispose.call(this);
    };

    /**
     * @override
     */
    UI_LOCKED_TABLE_CLASS.$cache = function (style, cacheSize) {

        // ==========================
        // var ttt = new Date();
        // var ddd = ttt;

        var win = this._oWin;
        if (win) {
            win.lineHeight = this._aRows[0].getCell(this._nLeft).getOuter().offsetHeight;
            win.totalHeight = win.lineHeight * win.total;
        }

        UI_TABLE_CLASS.$cache.call(this, style, cacheSize);

        // console.log('=================== locked-table $cache super class cache] ' + ((new Date()).getTime() - ddd));
        // var ddd = new Date();

        var i = 0,
            rows = this.getRows(),
            cols = this.getHCells(),
            pos = cols[this._nLeft].$$pos;

        this.$$paddingTop = MAX(this.$$paddingTop, this._uLockedHead.getBody().offsetHeight);
        this.$$mainWidth -=
            (this.$$paddingLeft = pos) +
                (this.$$paddingRight =
                    this._nRight < cols.length ? this.$$mainWidth - cols[this._nRight].$$pos : 0);

        // console.log('=================== locked-table $cache 1] ' + ((new Date()).getTime() - ddd));
        // var ddd = new Date();

        // 以下使用 style 代替临时变量 o
        for (; style = cols[i++]; ) {
            style.$$pos -= pos;
        }

        // console.log('=================== locked-table $cache 2 (col)] ' + ((new Date()).getTime() - ddd));
        // var ddd = new Date();

        for (i = 0, pos = 0; style = rows[i++]; ) {
            style.getCell(this._nLeft).cache(false, true);
            style.$$pos = pos;
            pos += MAX(style.getHeight(), style._eFill.offsetHeight);
        }

        // ======================== ch 35
        // console.log('=================== locked-table $cache 3 (row)] ' + ((new Date()).getTime() - ddd));
        // var ddd = new Date();

        if (pos) {
            this.$$mainHeight = pos;
            if (!this._bCreated) {
                this.$$mainHeight += this.$$paddingTop;
            }
        }

        this._uLockedHead.cache(false, true);
        this._uLockedMain.cache(false, true);

        // console.log('=================== locked-table $cache 4 (locked)] ' + ((new Date()).getTime() - ddd));
        // var ddd = new Date();

        // console.log('=================== locked-table $cache] ' + ((new Date()).getTime() - ttt));
    };

    /**
     * @override
     */
    UI_LOCKED_TABLE_CLASS.$pagescroll = function () {
        UI_TABLE_CLASS.$pagescroll.call(this);
        if (!this._uVScrollbar) {
            this._uLockedHead.getOuter().style.top = this._uHead.getOuter().style.top
        }
    };

    /**
     * @override
     */
    UI_LOCKED_TABLE_CLASS.$resize = function () {
        // ==========================
        // var ddd = new Date();

        var o = this.getMain().style;
        o.paddingLeft = o.paddingRight = '';
        this.$$paddingLeft = this.$$paddingRight = 0;

        // console.log('=================== locked-table $resize start] ' + ((new Date()).getTime() - ddd));
        // ddd = new Date();

        UI_TABLE_CLASS.$resize.call(this);

        // console.log('=================== locked-table $resize superclass resize] ' + ((new Date()).getTime() - ddd));
        // ddd = new Date();

    };

    /**
     * @override
     */
    UI_LOCKED_TABLE_CLASS.$scroll = function () {
        UI_TABLE_CLASS.$scroll.call(this);
        this._uLockedMain.getBody().style.top = this.getBody().style.top;
    };

    /**
     * @override
     */
    UI_LOCKED_TABLE_CLASS.$setSize = function (width, height) {
        // ====================================
        // var ddd = new Date();
        // var ttt = new Date();

        var o = this.getMain().style,
            i = 0,
            layout = getParent(this.getBody()),
            lockedHead = this._uLockedHead,
            lockedMain = this._uLockedMain,
            style = getParent(getParent(lockedHead.getBody())).style,
            win = this._oWin,
            lineHeight;

        // 窗口设置
        if (win && win.total > this._aRows.length) {
            
        }

        // console.log('=================== locked-table $setSize start] ' + ((new Date()).getTime() - ddd));
        // ddd = new Date();

        o.paddingLeft = this.$$paddingLeft + 'px';
        o.paddingRight = this.$$paddingRight + 'px';

        // console.log('=================== locked-table $setSize 1] ' + ((new Date()).getTime() - ddd));
        // ddd = new Date();
        
        UI_TABLE_CLASS.$setSize.call(this, width, height);

        // console.log('=================== locked-table $setSize 2] ' + ((new Date()).getTime() - ddd));
        // ddd = new Date();
        
        o = this._uHead.getWidth() + this.$$paddingLeft + this.$$paddingRight;
        (o < 0 || isNaN(o)) && (o = 0);

        // console.log('=================== locked-table $setSize 3] ' + ((new Date()).getTime() - ddd));
        // ddd = new Date();

        lockedHead.$setSize(o, this.$$paddingTop);

        // console.log('=================== locked-table $setSize 4] ' + ((new Date()).getTime() - ddd));
        // ddd = new Date();

        style.height = this.$$paddingTop + 'px';
        this._uLockedMain.$setSize(o, toNumber(layout.style.height));

        // console.log('=================== locked-table $setSize 5] ' + ((new Date()).getTime() - ddd));
        // ddd = new Date();

        style.width = this._uLockedMain.getBody().lastChild.style.width = o + 'px';
        this._uLockedMain.getOuter().style.top = this.$$paddingTop + 'px';
        
        // bugfix: 尽管有已经有padding来定位内容区，但是如果不设left，还是有可能初始定位到左上角。
        this._uHead.getOuter().style.left = this.$$paddingLeft + 'px';

        width = layout.style.width;

        // 统一行高
        // 分别设置表头与内容区域
        var rows = this._aLockedHeadRow,
            minHeight;

        // console.log('=================== locked-table $setSize 6] ' + ((new Date()).getTime() - ddd));
        // ddd = new Date();


        // 设置表头， 处理多行表头的问题
        height = this.$$paddingTop / rows.length; 
        for (i = 0; o = rows[i]; i++) {
            o._eFill.style.width = width;
            o._eFill.style.height = height + 'px';
            o = o.getCell(this._nLeft);
            if (o) {
                minHeight = firefoxVersion ? 0 : o.$getBasicHeight();
                isNaN(minHeight) && (minHeight = 0);
                o = o.getOuter();
                style = getAttribute(o, 'rowSpan') || 0;
                if (style) {
                    style = parseInt(style, 10);
                }
                o.style.height = MAX(style * height - minHeight, 0) + 'px';
            }
        }

        // console.log('=================== locked-table $setSize 7] ' + ((new Date()).getTime() - ddd));
        // ddd = new Date();


        // 设置表格内容行
        rows = this._aLockedRow;
        for (i = 0; o = rows[i]; i++) {
            // ==================================
            // ddd1 = new Date();

            // ================================== ch sometimes 1
            o._eFill.style.width = width;

            //================================== 下面一个循环 ch 1205 （从olap-table.resize来时这段共用了315）
            // 下面这些内容，就是检查为_eFill和getCell(this._nLeft)的高度是否一样，不一样则设置高度强制一样。
            // 通过css来保证这些，所以去掉这段代码。

            /*
            console.log('=================== locked-table $setSize hot_0] ' + ((new Date()).getTime() - ddd1));
            ddd1 = new Date();

            // ================================== ch 4 (着重优化)
            style = MAX(height = o.getCell(this._nLeft).getOuter().offsetHeight, o._eFill.offsetHeight);

            console.log('=================== locked-table $setSize hot_1] ' + ((new Date()).getTime() - ddd1));
            
            // ==================================
            ddd1 = new Date();

            // ================================== 一般不走此两条分支
            if (style > o._eFill.offsetHeight) {
                o._eFill.style.height = style + 'px';
            }
            else if (height < style) {
                minHeight = firefoxVersion ? 0 : o.getCell(this._nLeft).$getBasicHeight();
                o.getCell(this._nLeft).getOuter().style.height = MAX(style - minHeight, 0) + 'px';
            }

            console.log('=================== locked-table $setSize hot_2] ' + ((new Date()).getTime() - ddd1));
            */
        }

        // console.log('=================== locked-table $setSize end (hot!!) (into)] ' + ((new Date()).getTime() - ddd));
        // console.log('=================== locked-table $setSize total] ' + ((new Date()).getTime() - ttt));
    };

    /**
     * @override
     */
    UI_LOCKED_TABLE_CLASS.addColumn = function (options, index) {
        if (index >= 0) {
            if (index < this._nLeft) {
                this._nLeft++;
            }
            if (index < this._nRight) {
                this._nRight++;
            }
        }
        return UI_TABLE_CLASS.addColumn.call(this, options, index);
    };

    /**
     * @override
     */
    UI_LOCKED_TABLE_CLASS.removeRow = function (index) {
        var i = 0,  row = this._aRows[index], o,
            lockedTR = row._eFill.parentNode;

        if (row) {
            row.hide();
            o = row.getOuter();
            disposeControl(row);
            removeDom(o, true);
            removeDom(lockedTR, true);
            this._aRows.splice(index, 1);
            this._aLockedRow.splice(index, 1);
            this.repaint();
        }
    };

    /**
     * @override
     */
    UI_LOCKED_TABLE_CLASS.addRow = function (data, index) {

        //__gzip_original__lockedRow
        var row = UI_TABLE_CLASS.addRow.call(this, data, index),
            index = indexOf(this.getRows(), row),
            lockedRow = this._aLockedRow[index],
            el = row.getMain(),
            o = createDom();

        o.innerHTML = '<table cellspacing="0"><tbody><tr class="' + el.className + '" style="' + el.style.cssText +
            '"><td style="padding:0px;border:0px"></td></tr></tbody></table>';

        o = UI_LOCKED_TABLE_CREATE_LOCKEDROW(el = o.lastChild.lastChild.lastChild, row);
        lockedRow = lockedRow ? lockedRow._eFill.parentNode : null;
        this._uLockedMain.getBody().lastChild.lastChild.insertBefore(el, lockedRow);
        this._aLockedRow.splice(index, 0, o);
        UI_LOCKED_TABLE_ROW_SPLIT(o);

        this.repaint();

        return row;
    };

    /**
     * @override
     */
    UI_LOCKED_TABLE_CLASS.init = function () {
        // ==========================
        // var ddd = new Date();

        // ========================== ch 25
        UI_LOCKED_TABLE_ALL_SPLIT(this);

        // console.log('=================== locked-table init (locked-table split)] ' + ((new Date()).getTime() - ddd));
        // ddd = new Date();

        UI_TABLE_CLASS.init.call(this);
    };

    /**
     * @override
     */
    UI_LOCKED_TABLE_CLASS.removeColumn = function (index) {
        UI_TABLE_CLASS.removeColumn.call(this, index);
        if (index >= 0) {
            if (index < this._nLeft) {
                this._nLeft--;
            }
            if (index < this._nRight) {
                this._nRight--;
            }
        }
    };

    /**
     * 初始化需要执行关联控制的行控件鼠标事件的默认处理。
     * 行控件鼠标事件发生时，需要通知关联的行控件也同步产生默认的处理。
     * @protected
     */
    (function () {
        function build(name) {
            UI_LOCKED_TABLE_ROW_CLASS[name] = function (event) {
                UI_CONTROL_CLASS[name].call(this, event);
                getParent(this._eFill).className = this.getMain().className;
            };
        }

        for (var i = 0; i < 11; ) {
            build('$' + eventNames[i++]);
        }
    })();
//{/if}//
//{if 0}//
})();
//{/if}//
