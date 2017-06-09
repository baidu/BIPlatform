/*
 修改版的LockedTable，为性能而优化。（为区别，改名为SlowLockedTable）

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
        removeDom = dom.remove,
        setStyle = dom.setStyle,
        getStyle = dom.getStyle,
        attachEvent = util.attachEvent,
        detachEvent = util.detachEvent,
        hasClass = dom.hasClass,
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
    var UI_LOCKED_TABLE = ui.SlowLockedTable =
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

                lockedEl.innerHTML =
                    '<div class="' + type + '-locked-head" style="position:absolute;top:0px;left:0px"><div style="white-space:nowrap;position:absolute"><table cellspacing="0"><thead>' + list.splice(0, headRows.length).join('') + '</thead></table></div></div><div class="' + type + '-locked-layout" style="position:absolute;left:0px;overflow:hidden"><div style="white-space:nowrap;position:absolute;top:0px;left:0px"><table cellspacing="0"><tbody>' + list.join('') + '</tbody></table></div></div>';
                // 初始化锁定的表头区域，以下使用 list 表示临时变量
                o = this._uLockedHead = $fastCreate(UI_CONTROL, lockedEl.firstChild, this);
                o.$setBody(el = o.getMain().lastChild.lastChild.firstChild);

                for (i = 0, list = children(el); o = list[i]; ) {
                    lockedHeadRows[i] = UI_LOCKED_TABLE_CREATE_LOCKEDROW(o, headRows[i++]);
                }

                o = this._uLockedMain = $fastCreate(UI_CONTROL, el = lockedEl.lastChild, this);
                o.$setBody(el = el.lastChild);

                for (i = 0, list = children(el.lastChild.lastChild); o = list[i]; ) {
                    lockedRows[i] = UI_LOCKED_TABLE_CREATE_LOCKEDROW(o, this._aRows[i++]);
                }
                insertBefore(lockedEl.firstChild, this._uHead.getOuter());
                insertBefore(lockedEl.firstChild, getParent(this.getBody()));

                // console.log('=================== locked-table constructor] ' + ((new Date()).getTime() - ddd));
                // ddd = new Date();                
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
            UI_LOCKED_TABLE_ROW_SPLIT(o);
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
        // TODO:如果当前表格宽度小于外围div宽度，那么重设表格宽度

        var testWidth = this.$$width;
        var vsWidth = this._uVScrollbar ? this._uVScrollbar.getWidth() : 0;
        var hsHeight = this._uHScrollbar ? this._uHScrollbar.getHeight() : 0;
        var innerHeight = this.getHeight() - this.$getBasicHeight() - hsHeight;

        if (this.$$mainHeight > innerHeight) { // show v scrollbar
            testWidth -= vsWidth;
        }

        if (this.$$paddingLeft + this.$$mainWidth < testWidth) {
            this.$$mainWidth = testWidth - this.$$paddingLeft;
        }


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
            style = getParent(getParent(lockedHead.getBody())).style;

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
        // this.headDrag.call(this); // 右侧表格列拖拽
        this.headFixedColumnDrag.call(this);
    };
    /**
     * 为表格添加可供拖拽的虚线，顺便绑定拖拽事件
     *
     * @const
     * @type {string}
     */
    UI_LOCKED_TABLE_CLASS.headDrag = function () {
        var me = this,
            type = me.getType(),
            mainEl = me.$di('getEl'),
            headEl = dom.getElementsByClass(mainEl, 'div', type + '-head')[0],
            layoutEl = dom.getElementsByClass(mainEl, 'div', type + '-layout')[0],
            dragBoxEl = createDom(type + '-drag-box', null, 'div'), // 拖拽接触点模块
            dotLineEl,
            disX = 0, // 这个距离是鼠标点击虚线时的位置，距离虚线左侧的距离
            curHeadTh,
            difLeft, // 虚线移动的距离
            oldPosLeft, // 表格元素居左的距离
            mainElLeft = dom.getPosition(mainEl).left;

        dragBoxEl.innerHTML = ''
            + '<span class="' + type +'-dot-box-drag"></span>'
            + '<span class="' + type + '-dot-box-line" ></span>';
        mainEl.appendChild(dragBoxEl);

        // 设置虚线高度
        dotLineEl = dom.getElementsByClass(dragBoxEl, 'span', type + '-dot-box-line')[0];
        setStyle(dotLineEl, 'height', this.$$height + 'px');

        if (headEl) {
            attachEvent(headEl, 'mouseover', headMouseOver);
            attachEvent(mainEl, 'mouseleave', function () {
                setStyle(dragBoxEl, 'display', 'none');
            });
            attachEvent(layoutEl, 'mouseleave', function () {
                setStyle(dragBoxEl, 'display', 'none');
            });
            attachEvent(dragBoxEl, 'mousedown', dragBoxMouseDown);
        }

        // 表头mouseover时，把拖拽接触点模块定位到触发元素旁
        function headMouseOver(ev) {
            var oEv = ev || window.event;
            var target = oEv.target || oEv.srcElement;

            if (hasClass(target, type + '-head-drag')) {
                curHeadTh = dom.getParent(target);
                setStyle(dragBoxEl, 'left', (dom.getPosition(target).left - mainElLeft) + 'px');
                setStyle(dragBoxEl, 'top', '0px');
                setStyle(dragBoxEl, 'display', 'block');
            }
        }

        // 虚线点击事件，先计算disX（具体看定义），再注册移动与松开事件
        function dragBoxMouseDown(ev) {
            var oEv = ev || window.event;
            // 全局捕获,生成了一个透明的层:用来解决IE8之前选中拖的BUG
            if (dragBoxEl.setCapture) {
                dragBoxEl.setCapture();
            }
            oldPosLeft = oEv.clientX;
            disX = oEv.clientX - dragBoxEl.offsetLeft;
            attachEvent(document, 'mousemove', dragBoxMouseMove);
            attachEvent(document, 'mouseup', dragBoxMouseUp);
            return false; // 阻止浏览器去做其他事情
        }

        // 虚线移动事件
        // TODO:虚线移动的最大位置与最小位置的判断
        function dragBoxMouseMove(ev) {
            var oEv = ev || window.event;
            var lineLeft = oEv.clientX - disX;
            setStyle(dragBoxEl, 'left', lineLeft + 'px');
            difLeft = oEv.clientX - oldPosLeft;
        }

        // 拖拽接触点松开事件
        function dragBoxMouseUp(ev) {
            setStyle(dragBoxEl, 'display', 'none');
            detachEvent(document, 'mousemove', dragBoxMouseMove);
            detachEvent(document, 'mouseup', dragBoxMouseUp);

            if (dragBoxEl.releaseCapture) {
                dragBoxEl.releaseCapture(); // 释放捕获
            }
            resetTableWidth();
        }

        // 重设表格宽度
        function resetTableWidth() {
            // 重设表头右侧部分宽度
            var headTableEl = dom.first(
                dom.first(
                    dom.getElementsByClass(mainEl, 'div', type + '-head')[0]
                )
            );
            setStyle(headTableEl, 'width', (parseInt(headTableEl.style.width) + difLeft) + 'px');
            // 重设表格内容右侧部分宽度
            var tableLayoutEl = dom.first(
                dom.first(
                    dom.getElementsByClass(mainEl, 'div', type + '-layout')[0]
                )
            );
            setStyle(tableLayoutEl, 'width', (parseInt(tableLayoutEl.style.width) + difLeft) + 'px');
            // 重设表头中拖拽列宽度
            setStyle(curHeadTh, 'width', (parseInt(curHeadTh.style.width) + difLeft) + 'px');
            // 重设表格内部拖拽列宽度
            var colIndex = dom.getAttribute(curHeadTh, 'data-cell-pos').split('-')[0];
            var rows = dom.children(
                dom.first(tableLayoutEl)
            );
            for (var rIndex in rows) {
                var row = rows[rIndex];
                var cols = dom.children(row);
                for (var cIndex in cols) {
                    var col = cols[cIndex];
                    var curIndex = dom.getAttribute(col, 'data-cell-pos').split('-')[0];
                    if (curIndex === colIndex) {
                        setStyle(col, 'width', (parseInt(col.style.width) + difLeft) + 'px');
                    }
                }
            }
            me.cache(getStyle(me._eMain));
            me.resize();
        }
    };

    /**
     * 表格锁定列的拖拽
     *
     * @const
     * @type {string}
     */
    UI_LOCKED_TABLE_CLASS.headFixedColumnDrag = function () {
        var me = this,
            type = me.getType(),
            mainEl = me.$di('getEl'),
            lockedHeadEl = dom.getElementsByClass(mainEl, 'div', type + '-locked-head')[0],
            lockedLayoutEl = dom.getElementsByClass(mainEl, 'div', type + '-locked-layout')[0],
            dragBoxEl = createDom(type + '-drag-box', null, 'div'), // 拖拽接触点模块
            dotLineEl,
            disX = 0, // 这个距离是鼠标点击虚线时的位置，距离虚线左侧的距离
            curHeadTh,
            difLeft, // 虚线移动的距离
            oldPosLeft, // 表格元素居左的距离
            mainElLeft = dom.getPosition(mainEl).left,
            maxBoundary = mainElLeft + mainEl.offsetWidth - 10,
            minBoundary = mainElLeft;
        maxBoundary = me._uVScrollbar.isShow() ? maxBoundary - me._uVScrollbar.$$width : maxBoundary;
        dragBoxEl.innerHTML = ''
            + '<span class="' + type +'-dot-box-drag"></span>'
            + '<span class="' + type + '-dot-box-line" ></span>';
        mainEl.appendChild(dragBoxEl);

        // 设置虚线高度
        dotLineEl = dom.getElementsByClass(dragBoxEl, 'span', type + '-dot-box-line')[0];
        setStyle(dotLineEl, 'height', this.$$height + 'px');

        if (lockedHeadEl) {
            attachEvent(lockedHeadEl, 'mouseover', headMouseOver);
            attachEvent(mainEl, 'mouseleave', function () {
                setStyle(dragBoxEl, 'display', 'none');
            });
            attachEvent(lockedLayoutEl, 'mouseleave', function () {
                setStyle(dragBoxEl, 'display', 'none');
            });
            attachEvent(dragBoxEl, 'mousedown', dragBoxMouseDown);
        }

        // 表头mouseover时，把拖拽接触点模块定位到触发元素旁
        function headMouseOver(ev) {
            var oEv = ev || window.event;
            var target = oEv.target || oEv.srcElement;

            if (hasClass(target, type + '-head-drag')) {
                curHeadTh = dom.getParent(target);
                setStyle(dragBoxEl, 'left', (dom.getPosition(target).left - mainElLeft) + 'px');
                setStyle(dragBoxEl, 'top', '0px');
                setStyle(dragBoxEl, 'display', 'block');
            }
        }

        // 虚线点击事件，先计算disX（具体看定义），再注册移动与松开事件
        function dragBoxMouseDown(ev) {
            var oEv = ev || window.event;
            // 全局捕获,生成了一个透明的层:用来解决IE8之前选中拖的BUG
            if (dragBoxEl.setCapture) {
                dragBoxEl.setCapture();
            }
            oldPosLeft = oEv.clientX;
            disX = oEv.clientX - dragBoxEl.offsetLeft;
            attachEvent(document, 'mousemove', dragBoxMouseMove);
            attachEvent(document, 'mouseup', dragBoxMouseUp);
            return false; // 阻止浏览器去做其他事情
        }

        // 虚线移动事件
        function dragBoxMouseMove(ev) {
            var oEv = ev || window.event;
            var cliX = oEv.clientX;
            if (cliX > maxBoundary) {
                //console.log('return---clientX:' + cliX + ';maxBoundary:' + maxBoundary + ';minBoundary:' + minBoundary);
                cliX = maxBoundary;
            }
            else if (cliX < minBoundary) {
                //console.log('return---clientX:' + cliX + ';maxBoundary:' + maxBoundary + ';minBoundary:' + minBoundary);
                cliX = minBoundary;
            }
            //console.log('clientX:' + cliX + ';maxBoundary:' + maxBoundary + ';minBoundary:' + minBoundary);
            var lineLeft = cliX - disX;
            setStyle(dragBoxEl, 'left', lineLeft + 'px');
            difLeft = cliX - oldPosLeft;
        }

        // 拖拽接触点松开事件
        function dragBoxMouseUp(ev) {
            setStyle(dragBoxEl, 'display', 'none');
            detachEvent(document, 'mousemove', dragBoxMouseMove);
            detachEvent(document, 'mouseup', dragBoxMouseUp);

            if (dragBoxEl.releaseCapture) {
                dragBoxEl.releaseCapture(); // 释放捕获
            }
//            if (difLeft !== 0) {
//                resetTableWidth();
//            }
            resetTableWidth();
        }

        // 重设表格宽度
        function resetTableWidth() {
            // 重设表头锁定部分宽度
            var headTableEl = dom.first(
                dom.getElementsByClass(mainEl, 'div', type + '-locked-head')[0]
            );
            setStyle(headTableEl, 'width', (parseInt(headTableEl.offsetWidth) + difLeft) + 'px');

            // 重设表格锁定部分宽度
            var tableLayoutEl = dom.first(
                dom.first(
                    dom.getElementsByClass(mainEl, 'div', type + '-locked-layout')[0]
                )
            );
            setStyle(tableLayoutEl, 'width', (parseInt(tableLayoutEl.offsetWidth) + difLeft) + 'px');

            var curHeadWidth = (parseInt(curHeadTh.style.width) + difLeft <= 0)
                ? 1
                : parseInt(curHeadTh.style.width) + difLeft;
            curHeadWidth = (curHeadWidth <= 0) ? 1: curHeadWidth;
            // 重设表头中拖拽列宽度
            setStyle(curHeadTh, 'width', curHeadWidth + 'px');
            // 重设表格锁定部分中拖拽列宽度
            var colIndex = dom.getAttribute(curHeadTh, 'data-cell-pos').split('-')[0];
            var rows = dom.children(
                dom.first(tableLayoutEl)
            );
            for (var rIndex in rows) {
                var row = rows[rIndex];
                var cols = dom.children(row);
                for (var cIndex in cols) {
                    var col = cols[cIndex];
                    var cellPos = dom.getAttribute(col, 'data-cell-pos');
                    var curIndex = null;
                    if (cellPos) {
                        curIndex = cellPos.split('-')[0];
                    }
                    var colWidth = parseInt(col.style.width) + difLeft;
                    colWidth = (colWidth <= 0) ? 1 : colWidth;
                    if (curIndex === colIndex) {
                        setStyle(col, 'width', colWidth + 'px');
                    }
                }
            }
            me.cache(getStyle(me._eMain));
            me.resize();
        }
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
