/**
 * ecui.ui.OlapTable
 * Copyright 2013 Baidu Inc. All rights reserved
 *
 * @file:   多维分析表格
 *         （行列锁定，跨多行，垮多列，树状表头等）
 * @author: sushuang(sushuang)
 */

(function() {
    //
    var core = ecui;
    var dom = core.dom;
    var array = core.array;
    var ui = core.ui;
    var string = core.string;
    var util = core.util;
    var q = xutil.dom.q;
    var xajax = xutil.ajax;
    //var URL = di.config.URL;

    var $fastCreate = core.$fastCreate;
    var inheritsControl = core.inherits;
    var triggerEvent = core.triggerEvent;
    var disposeControl = core.dispose;
    var createDom = dom.create;
    var addClass = dom.addClass;
    var setStyle = dom.setStyle;
    var removeClass = dom.removeClass;
    var getMouseX = core.getMouseX;
    var toNumber = util.toNumber;
    var getParent = dom.getParent;
    var getStyle = dom.getStyle;
    var sliceByte = string.sliceByte;
    var moveElements = dom.moveElements;
    var getAttribute = dom.getAttribute;
    var getPosition = dom.getPosition;
    var encodeHTML = string.encodeHTML;
    var remove = array.remove;
    var getView = util.getView;
    var extend = util.extend;
    var repaint = core.repaint;
    var attachEvent = util.attachEvent;
    var detachEvent = util.detachEvent;
    var pushArray = Array.prototype.push;
    // 引用了外部库
    var formatNumber = xutil.number.formatNumber;
    var MATH = Math;
    var MIN = MATH.min;
    var WINDOW = window;

    var UI_CONTROL = ui.Control;
    var UI_CONTROL_CLASS = UI_CONTROL.prototype;
    var UI_LOCKED_TABLE = ui.SlowLockedTable;
    var UI_LOCKED_TABLE_CLASS = UI_LOCKED_TABLE.prototype;

    /**
     * OLAP 表主类
     *
     * @class
     * @extends {ecui.ui.LockedTable}
     */
    var UI_OLAP_TABLE = ui.OlapTable =
        inheritsControl(
            UI_LOCKED_TABLE,
            'ui-table',
            function(el, options) {
                this.$setOptions(options);
                this.$renderHTML(el);
            }
        );
    var UI_OLAP_TABLE_CLASS = UI_OLAP_TABLE.prototype;

    var UI_OLAP_TABLE_CELL_CLASS = (
        UI_OLAP_TABLE_CLASS.Cell = inheritsControl(
            UI_LOCKED_TABLE_CLASS.Cell
        )
    ).prototype;

    var UI_TABLE_HCELL_CLASS = UI_OLAP_TABLE_CLASS.HCell.prototype;

    /**
     * 表格输入非法时的信息
     *
     * @type {string}
     * @private
     */
    var INVALID_TEXT = '数据错误';
    /**
     * 树节点缩进单位宽度
     *
     * @type {number}
     * @private
     */
    var TREE_INDENT = 15;

    //--------------------------------------------------
    // 条件格式
    //--------------------------------------------------

    /**
     * 得到条件格式样式
     *
     * @private
     * @param {Object} condFmtDef 条件格式定义
     * @param {string} ctrlCssBase 控件的css base
     * @return {Object} css和style
     */
    function getCondFmt(condFmtDef, ctrlCssBase) {
        var ret = {
            text: { css: [], style: [] },
            outer: { css: [], style: [] },
            left: { css: [], style: [] },
            right: { css: [], style: [] }
        };

        if (!condFmtDef) { return null; }

        // 箭头
        if (condFmtDef.arr) {
            ret.right.css.push(
                    ctrlCssBase + '-condfmt-arr',
                    ctrlCssBase + '-condfmt-arr-' + condFmtDef.arr
            );
        }

        // 背景色
        if (condFmtDef.bg) {
            if (condFmtDef.bg.indexOf('#') >= 0) {
                ret.outer.style.push('background-color:' + condFmtDef.bg + ';');
            }
            else {
                ret.outer.css.push(ctrlCssBase + '-condfmt-bg-' + condFmtDef.bg);
            }
        }

        // 文字颜色
        if (condFmtDef.tx) {
            if (condFmtDef.tx.indexOf('#') >= 0) {
                ret.text.style.push('color:' + condFmtDef.tx + ';');
            }
            else {
                ret.text.css.push(ctrlCssBase + '-condfmt-tx-' + condFmtDef.tx);
            }
        }

        // 文字加粗
        if (condFmtDef.wt) {
            ret.text.style.push('font-weight:bold;');
        }

        return ret;
    }

    //--------------------------------------------------
    // UI_OLAP_TABLE 方法
    //--------------------------------------------------

    /**
     * @override
     */
    UI_OLAP_TABLE_CLASS.init = function() {
        UI_OLAP_TABLE.superClass.init.call(this);
        this.$initRowChecked();
    };

    /**
     * 设置参数
     *
     * @protected
     * @param {Object} options 参数
     * @param {Array.<Object>} options.datasource 主体数据
     *      条件格式：每个节点中有：{Object} style字段。参见getCondFmt。
     * @param {Array.<Object>} options.colFields 上表头（不仅是内容区域，包括了左表头）
     *                         options.colFields.colspan 列合并数
     *                         options.colFields.rowspan 行合并数
     *                         options.colFields.uniqName 列uniqName（有疑问）
     *                         options.colFields.v 列名
     * @param {Array.<Object>} options.colDefine 列定义（不仅是内容区域，包括了左表头）
     *      排序：每个节点中有：{string} orderby字段，值可为：'asc', 'desc', 'none'（默认为空，不排序）
     *      宽度：每个节点中有：{number} width字段。可不指定（有疑问，这个width是干什么用的）
     * @param {Array.<Object>} options.rowHeadFields 左表头
     *      缩进：每个节点有{number} indent字段，值为0, 1, 2, 3 （默认为空，不缩进）
     *      链接下钻：每个节点有{boolean} drillByLink字段
     *      expand/collapse（加减号）：每个节点有{boolean} expand字段，
     *          true表示可以expand（显示加号）
     *          false表示可以collapse（显示减号）
     * @param {Array.<Object>} options.rowDefine 行定义
     * @param {string} options.emptyHTML 数据为空时的显示字符
     * @param {number=} options.rowHCellCut 行头指定长度，文字过长截断成“...”，用title提示
     * @param {number=} options.cCellCut 内容区指定长度，文字过长截断成“...”，用title提示
     * @param {number=} options.hCellCut 表头区指定长度，文字过长截断成“...”，用title提示
     * @param {boolean=} options.rowCheckMode 是否启用行选中模式，
     *      'SELECT'（单选）, 'CHECK'（多选）, 空（默认）
     * @param {Array=} options.rowChecked 初始化行选中
     * @param {Array=} options.rowCheckMax 选择条数的上限
     * @param {Array=} options.rowCheckMin 选择条数的下限
     * @param {string=} options.defaultCCellAlign 默认的内容区的align，
     *      默认为left，可为right, left, center
     * @param {boolean} options.vScroll 是否使用纵向滚动条（默认false）
     * @param {boolean} options.hScroll 是否使用横向滚动条（默认true）
     */
    UI_OLAP_TABLE_CLASS.$setOptions = function(options) {
        this._sEmptyHTML = options.emptyHTML;

        this._aData = options.datasource || [];
        this._aColFields = options.colFields || [];
        this._aColDefine = options.colDefine || [];
        this._reportTemplateId = options.reportTemplateId;
        this._aRowHeadFields = options.rowHeadFields || [];
        this._aRowDefine = options.rowDefine || [];

        // 行选择记录
        this._oRowCheck = {
            rowCheckMode: options.rowCheckMode,
            rowChecked: options.rowChecked || [],
            rowCheckMax: options.rowCheckMax || Number.MAX_VALUE,
            rowCheckMin: options.rowCheckMin || Number.MIN_VALUE,
            rowCheckCount: 0
        };
        // 如果行内有selected标志，优先
        for (var i = 0, o; o = this._aRowDefine[i]; i ++) {
            o.selected && this._oRowCheck.rowChecked.push(i);
        }

        // 文字过长截断
        this._oCut = {
            ROWHCELL: options.rowHCellCut,
            CCELL: options.cCellCut,
            HCELL: options.hCellCut
        };

        // 样式
        this._oStyle = {
            defaultCCellAlign: options.defaultCCellAlign
        };

        // this.$validate();

        this._nLeftLock = options.leftLock =
            this._bInvalid
                ? 0
                : (
                this._aRowHeadFields.length
                    ? this._aRowHeadFields[0].length : 0
                );

        this._nRightLock = options.rightLock = 0;

        options.vScroll == null && (options.vScroll = false);
        options.hScroll == null && (options.hScroll = true);
    };

    /**
     * 校验输入数据
     *
     * @protected
     */
    UI_OLAP_TABLE_CLASS.$validate = function() {
        this._bInvalid = false;

        var colCount = validateLength.call(this, this._aColFields);
        var rowHeadColCount = validateLength.call(this, this._aRowHeadFields);
        var dataCount = validateLength.call(this, this._aData);

        if (this._aColDefine.length != colCount) {
            this._bInvalid = true;
        }
        if (rowHeadColCount + dataCount != colCount) {
            this._bInvalid = true;
        }
        if (this._aRowHeadFields.length != this._aData.length) {
            this._bInvalid = true;
        }
    };

    /**
     * 校验二维数组宽高是否合法（含盖计算colspan和rowspan）
     *
     * @private
     * @this {ui.OlapTable} 控件本身
     * @return {number} length
     */
    function validateLength(matrix) {
        // // TODO 
        // // 同时colspan和rowspan
        // var baseCount = 0; // 每行的应该长度
        // var rowMaxArr = []; // 每列因rowspan而到达的高度
        // var colCount;

        // for (var i = 0, line; i < matrix.length; i ++) {
        //     line = matrix[i];

        //     if (!line) {
        //         this._bInvalid = true;
        //         return baseCount;
        //     } 

        //     colCount = 0;
        //     itemJ = 0;
        //     for (var j = 0, item; ; j ++) {
        //         item = line[itemJ ++];
        //         rowMaxArr[colCount] == null && (rowMaxArr[colCount] = -1);

        //         if (rowMaxArr[colCount] >= i) {
        //             colCount ++;
        //             continue;
        //         }
        //         else {
        //             if (item === Object(item)) {
        //                 if (item.rowspan > 1) {
        //                     rowMaxArr[colCount] = i + item.rowspan - 1;
        //                 }
        //                 else if (item.colspan > 1) {
        //                     colCount += item.colspan;
        //                     rowMaxArr[colCount] = i;
        //                 }
        //             }
        //             else {
        //                 colCount ++;
        //                 rowMaxArr[colCount] = i;
        //             }
        //         }
        //     }

        //     if (!baseCount) {
        //         baseCount = colCount;
        //     }
        //     else if (baseCount != colCount) {
        //         this._bInvalid = true;
        //         return baseCount;
        //     }
        // }
        // return baseCount;
    };

    /**
     * 设置数据并渲染表格
     *
     * @public
     * @param {string} options 参数，参见setOptions
     */
    UI_OLAP_TABLE_CLASS.setData = function(options) {

        // ===========================
        // var ttt = new Date();

        // ===========================
        // var ddd = new Date();

        detachEvent(WINDOW, 'resize', repaint);


        // ===================== ch 1200
        this.$disposeInner();

        // console.log('=================== olap-table setData start] ' + ((new Date()).getTime() - ddd));
        // ddd = new Date();

        var el = this.getOuter();
        el.innerHTML = '';
        this.$setBody(el);

        // console.log('=================== olap-table setData 1] ' + ((new Date()).getTime() - ddd));
        // ddd = new Date();

        this.$resize();

        // console.log('=================== olap-table setData 2] ' + ((new Date()).getTime() - ddd));
        // ddd = new Date();

        // ==================== ch 518
        UI_OLAP_TABLE.client.call(
            this,
            el,
            extend(
                { uid: this._sUID, primary: this._sPrimary },
                options
            )
        );
        this._bCreated = false;

        // console.log('=================== olap-table setData 3 (into)] ' + ((new Date()).getTime() - ddd));
        // ddd = new Date();

        // =================== ch 370
        this.cache(true, true);

        // console.log('=================== olap-table setData 4] ' + ((new Date()).getTime() - ddd));
        // ddd = new Date();

        // =================== ch 1102
        this.init();

        // console.log('=================== olap-table setData 51] ' + ((new Date()).getTime() - ddd));
        // ddd = new Date();
        // this.$resize();

        this.$bindCellLink();

        this.$renderTips();

        attachEvent(WINDOW, 'resize', repaint);

        // console.log('=================== olap-table setData 6] ' + ((new Date()).getTime() - ddd));
        // ddd = new Date();

        // 为优化而去掉
        // this.resize();

        // =================== ch resize：318 （里面会进入在locked-table.$setSize, 用了315）
        // console.log('=================== olap-table setData last] ' + ((new Date()).getTime() - ddd));

        // console.log('=================== olap-table setData total] ' + ((new Date()).getTime() - ttt));
    };

    /**
     * 析构内部
     *
     * @protected
     */
    UI_OLAP_TABLE_CLASS.$disposeInner = function() {
        var disposeFunc = this.$dispose;
        this.$dispose = new Function();
        disposeControl(this);
        this.$dispose = disposeFunc;
    };

    /**
     * 渲染HTML
     *
     * @protected
     * @param {HTMLElement} el 控件容器
     */
    UI_OLAP_TABLE_CLASS.$renderHTML = function(el) {

        // =================================
        // var ttt = new Date();
        // var ddd = new Date();

        var type = this.getTypes()[0];
        var datasource = this._aData || [];
        var colFields = this._aColFields || [];
        var colDefine = this._aColDefine || [];
        var reportTemplateId = this._reportTemplateId;

        var rowHeadFields = this._aRowHeadFields || [];
        var rowDefine = this._aRowDefine || [];
        var leftLock = this._nLeftLock;
        var html = [];
        var i;
        var j;
        var line;
        var wrap;

        setStyle(el, 'width', 'auto');
        setStyle(el, 'display', 'block');
        html.push('<table>');

        // 非法情况
        if (this._bInvalid) {
            html.push('<thead><tr><th class="' + type +'-hcell-empty">&nbsp;</th></tr></thead>');
            html.push('<tbody><tr><td>' + INVALID_TEXT + '</td></tr></tbody>');
        }

        // 正常情况
        else {
            // 上表头
            html.push('<thead>');
            if (!colFields.length) {
                html.push('<tr><th class="' + type + '-hcell-empty">&nbsp;</th></tr>');
            }
            else {
                for (i = 0; line = colFields[i]; i ++) {
                    html.push('<tr>');
                    for (j = 0; j < line.length; j ++) {
                        if (isPlaceholder(wrap = line[j])) {
                            continue;
                        }
                        this.$renderHCell(
                            html,
                            // 目前只有最底层才传colField
                                i == colFields.length - 1 ? colDefine[j] : null,
                            wrap,
                                j < this._nLeftLock ? j : (j - this._nLeftLock),
                            i
                        );
                    }
                    html.push('</tr>');
                }
            }
            html.push('</thead>');

            // 表内容
            html.push('<tbody>');
            if (this._bInvalid || !datasource.length) {
                html.push(
                    '<tr>',
                    '<td class="', type, '-cell-empty" align="middle" colspan="',
                    colFields.length, '">',
                    this._sEmptyHTML,
                    '</td>',
                    '</tr>'
                );
            }
            else {
                for (i = 0; line = datasource[i]; i ++) {
                    html.push('<tr class="'+ type +'-row">')
                    // 左表头
                    if (leftLock) {
                        for (j = 0; j < rowHeadFields[i].length; j ++) {
                            if (isPlaceholder(wrap = rowHeadFields[i][j])) {
                                continue;
                            }
                            this.$renderRowHCell(
                                html,
                                colDefine[j],
                                wrap,
                                j,
                                i,
                                rowHeadFields
                            );
                        }
                    }
                    // 内容
                    for (j = 0; j < line.length; j ++) {
                        wrap = line[j];
                        this.$renderCell(
                            html,
                            colDefine[leftLock + j],
                            rowDefine[i],
                            wrap,
                            j,
                            i,
                            rowHeadFields // TODO:这一块可能有问题，需要监测一下
                        );
                    }
                    html.push('</tr>');
                }
            }
        }

        html.push('</tbody></table>');

        // ==========================以上所有循环push ch 144
        // console.log('=================== olap-table html.push] ' + html.length + ' ' + ((new Date()).getTime() - ddd));

        // ====================================
        // ddd = new Date();

        html = html.join('');

        // console.log('=================== olap-table html.join("")] ' + html.length + ' ' + ((new Date()).getTime() - ddd));

        // ====================================
        // ddd = new Date();

        // ============================= ch 293 （分批加载来优化）
        el.innerHTML = html;

        // console.log('=================== olap-table renderHTML el.innerHTLM=...] ' + ((new Date()).getTime() - ddd));

        // console.log('=================== olap-table renderHTML total] ' + ((new Date()).getTime() - ttt));
        // ddd = new Date();


        return el;
    };

    function UI_TIP_HANDLER(event) {
        var e = event || window.event,
            con;
        el = e.target || e.srcElement;
        con = el.parentNode.getControl();
        //el.title = el.mal;
        //con.getInput().focus();
    }

    /**
     * 根据返回数据设置olap表格的指标解释到表格td的title标签中
     */
    UI_OLAP_TABLE_CLASS.$setMeasureDes4Table = function(data){
        var el = this.getOuter();
        var type = this.getTypes()[0];
        var tableHeaders = q(type + '-olap-ind-describe', el);
        for (var i = 0; i < tableHeaders.length; i++) {
            var header = tableHeaders[i];
            if(header.getAttribute('uniquename')){
                var uniquename = header.getAttribute('uniquename');
                if(data.descriptions[uniquename]){
                    header.title = data.descriptions[uniquename];
                }
            }
        };
    };
    /**
     * 渲染上方表头节点
     *
     * @protected
     */
    UI_OLAP_TABLE_CLASS.$renderHCell = function(
        // 只有最底层有colField
        html, colDefItem, wrap, x, y
    ) {
        var type = this.getType();
        var classStr = [type + '-hcell'];
        var classSortStr;
        var styleStr = [];
        var attrStr = [];
        var span = [];
        var innerStr = '';
        var tooltipStr = '';
        var tooltipTag = '';
        var dragStr = '';

        wrap = objWrap(wrap);
        var align = colDefItem.align || 'left';
        if (align) {
            classStr.push(type + '-cell-align-' + align);
        }
        span.push(wrap.colspan ? ' colspan="' + wrap.colspan + '" ' : '');
        span.push(wrap.rowspan ? ' rowspan="' + wrap.rowspan + '" ' : '');

        if (colDefItem && colDefItem.width) {
            //styleStr.push('width:' + colDefItem.width + 'px;');
        }
        // TODO:把排序样式放在th里面的span里面
        if (colDefItem && colDefItem.orderby) {
            classSortStr = type + '-hcell-sort-' + colDefItem.orderby;
            attrStr.push('data-orderby="' + colDefItem.orderby + '"');
        }
        if (colDefItem && colDefItem.toolTip) {
            tooltipStr = 'title="' + colDefItem.toolTip + '"';
        }
        classStr.push(type + '-olap-ind-describe');
        attrStr.push('data-cell-pos="' + x + '-' + y + '"');

        if(colDefItem && colDefItem.uniqueName){
            attrStr.push('uniqueName="' + colDefItem.uniqueName + '"');
        }
        // 如果是维度列，就不显示tooltip图标
        if (!wrap.colspan) {
            //tooltipTag += '<div class="'+ type + '-head-tips" ' + tooltipStr + '">&nbsp;</div>';
            var toolTipText = colDefItem.toolTip ? string.encodeHTML(colDefItem.toolTip) : '';
            tooltipTag += '<span class="' + type + '-head-tips" data-message="' + toolTipText + '"></span>';
            //dragStr += '<span class="' + type + '-head-drag"></span>';
        }
        else {
            dragStr += '<span class="' + type + '-head-drag"></span>';
        }
        //attrStr.push('title='+"'我就想试试title的字能有多长'");
        innerStr = this.$renderCellInner('HCELL', null, wrap, attrStr, classStr, styleStr); // 列头文本
        // 如果是ie8以下版本，需要在innerCell外面套一层div，设置表头的margin属性，
        // 不然文本过多的话会显示不全
        // TODO:如果是最后一个，就不加drag
        // var useBag = dom.ieVersion < 8;
        var sortStr = '';
        sortStr = classSortStr ? '<div class="'+ classSortStr + '"></div>' : '';
        var headThContentClas = type + '-head-th-content';
        var strThContent = (innerStr === '')
            ? ''
            : (
                '<div class="' + headThContentClas + '">'
                + '<div class="'+ type + '-head-font">' + innerStr + '</div>'
                + sortStr
                + tooltipTag
                + '</div>'
                + dragStr
            );
        html.push(
            '<th ', span.join(' '), ' ', attrStr.join(' '), ' ',
                ' class="', classStr.join(' '),
                '" style="', styleStr.join(' '),
            '">',
            strThContent,
            '</th>'
        );
    };

    /**
     * 渲染左侧表头节点
     *
     * @protected
     */
    UI_OLAP_TABLE_CLASS.$renderRowHCell = function(html, colDefItem, wrap, x, y, rowDefine) {
        // TODO:添加drag区域
        var type = this.getType();
        var classStr = [type + '-rowhcell'];
        var styleStr = [];
        var attrStr = [];
        var span = [];
        var innerStr;
        var tooltipStr = '';
        var tooltipTag = '';

        wrap = objWrap(wrap);
        var align = wrap.align || 'left';
        if (align) {
            classStr.push(type + '-cell-align-' + align);
        }
        span.push(wrap.colspan ?  ' colspan="' + wrap.colspan  +  '" ' : '');
        span.push(wrap.rowspan ?  ' rowspan="' + wrap.rowspan  +  '" ' : '');
        // 先为左侧添加背景色
        // FIXME:实现不是很好,目前只测到两个维度，多个维度时，需要待测
        if (rowDefine) {
            var rowDefines = rowDefine[y];
            var rDefLen = rowDefines.length;
            // 多个维度时对左侧表头背景色的处理
            if (rDefLen > 1) {
                if (rowDefines[0].indent != 1) {
                    classStr.push(type + '-expand-background');
                    if (rowDefines[0].indent < 1) {
                        if (wrap.indent == 0) {
                            classStr.push(type + '-expand-font');
                        }
                    }
                }
            }
            // 单独维度时对左侧表头背景色的处理
            else {
                if (rowDefines[0].indent > 1) {
                    classStr.push(type + '-expand-background');
                }
                // 单独维度时对左侧表头汇总行加粗
                else if (rowDefines[0].indent == 0) {
                    classStr.push(type + '-expand-font');
                }
            }
        }
        if (colDefItem.width) {
            styleStr.push('width:' + colDefItem.width + 'px;');
            // styleStr.push('min-width:' + colDefItem.width + 'px;');
            // styleStr.push('max-width:' + colDefItem.width + 'px;');
        }
        if (colDefItem && colDefItem.toolTip) {
            tooltipStr = colDefItem.toolTip;
        } else { // 测试数据
            tooltipStr = '测试';
        }
        attrStr.push('data-cell-pos="' + x + '-' + y + '"');
        attrStr.push('data-row-h="1"'); // 左表头的标志
        innerStr = this.$renderCellInner('ROWHCELL', null, wrap, attrStr, classStr, styleStr);

        html.push(
            '<td ',
            span.join(' '), ' ',
            attrStr.join(' '), ' ',
            ' style="', styleStr.join(' '),
            '" class="', classStr.join(' '),
            '">',
            innerStr,
            '</td>'
        );
    };

    /**
     * 渲染内容节点
     *
     * @protected
     */
    UI_OLAP_TABLE_CLASS.$renderCell = function(html, colDefItem, rowDefItem, wrap, x, y, rowDefine) {
        var type = this.getType();
        var classStr = [type + '-ccell'];
        var styleStr = [];
        var attrStr = [];
        var innerStr;
        wrap = objWrap(wrap);
        if (rowDefine) {
            var rowDefines = rowDefine[y];
            var rDefLen = rowDefines.length;
            // 多个维度时对内容区域背景色的处理
            if (rDefLen > 1) {
                if (rowDefines[0].indent != 1) {
                    // 背景色
                    classStr.push(type + '-expand-background');
                    if (rowDefines[0].indent < 1) {
                        if (rowDefines[rDefLen - 1].expand == true) {
                            classStr.push(type + '-expand-font');
                        }
                    }
                }
            }
            // 单独维度时对内容区域背景色的处理
            else {
                if (rowDefines[0].indent > 1) {
                    classStr.push(type + '-expand-background');
                }
                // 单独维度时对内容区域汇总行加粗
                else if (rowDefines[0].indent == 0) {
                    classStr.push(type + '-expand-font');
                }
            }
        }
        var align = colDefItem.align || 'left';
        if (align) {
            classStr.push(type + '-cell-align-' + align);
        }
        attrStr.push('data-cell-pos="' + x + '-' + y + '"');
        attrStr.push('data-content="1"'); // 内容节点的标志

        innerStr = this.$renderCellInner(
            'CCELL',
            colDefItem,
            wrap,
            attrStr,
            classStr,
            styleStr
        );
        html.push(
            '<td ',
                attrStr.join(' '), ' ',
                ' style="', styleStr.join(' '),
                '" class="', classStr.join(' '),
            '">',
            //'<div class="ui-table-cell-infor">' + innerStr + '</div><div class="ui-table-cell-empty"></div>',

            '<div class="' + type + '-cell-text">' + innerStr + '</div>',
            '</td>'
        );
    };

    /**
     * 节点内部结构
     *
     * @private
     * @param {string} cellType 为'ROWHCELL', 'HCELL', 'CCELL'
     * @param {Object=} defItem 列定义
     * @param {Object} wrap 节点数据
     * @param {Array} attrStr 父节点属性集合
     * @param {Array} classStr 父节点css class集合
     * @param {Array} styleStr 父节点css style集合
     * @return {string} 节点内部html
     */
    UI_OLAP_TABLE_CLASS.$renderCellInner = function(
        cellType, defItem, wrap, attrStr, classStr, styleStr
    ) {
        var indentStyle = '';
        var clz = '';
        var type = this.getType();
        var value = getWrapValue.call(this, cellType, wrap, defItem && defItem.format);
        var prompt = value.prompt;
        value = value.value;

        if (cellType === 'ROWHCELL' || cellType === 'HCELL') {
            if (prompt) {
                value = '<span class="tip-layer-div" data-message="' + string.encodeHTML(prompt) + '">' + value + '</span>';
            } else {
                value = '<span class="tip-layer-div" data-message="' + string.encodeHTML(value) + '">' + value + '</span>';
            }
        }

        if (wrap.indent) {
            // margin-left会用来判断indent的点击事件，所以结构不能变
            attrStr.push('data-indent="' + wrap.indent + '"');
            indentStyle = 'margin-left:' + parseInt(TREE_INDENT * wrap.indent, 10) + 'px;';
        }

        if (wrap.drillByLink) {
            attrStr.push('data-cell-link="true"');
            value = '<a href="#" class="' + type + '-cell-link" data-cell-link-drill-a="1">' + value + '</a>';
        }
        // 增加判断逻辑，如果改行是手动汇总行，那么linkBridge也不能有点击，否则后台没法处理
//        else if (defItem && defItem.linkBridge && wrap.cellId && wrap.cellId.indexOf('[SUMMARY_NODE].[ALL]') < 0) {
        else if ((value + '').indexOf('-') === -1 && defItem && defItem.linkBridge && !defItem.format) {
            attrStr.push('data-cell-link="true"');
            value = value.split(',');
            // value = '<a href="#" class="' + type + '-cell-link" data-cell-link-bridge-a="1">' + value + '</a>';
            var str = [];
            for (var i = 0; i < value.length; i ++) {
                str.push(
                    [
                        '<a href="#" class="', type, '-cell-link" data-cell-link-bridge-a="', i, '">',
                        value[i],
                        '</a>'
                    ].join('')
                );
            }
            value = str.join('&nbsp;&nbsp;');
        }
        else if ((value + '').indexOf('-') === -1 && defItem && defItem.linkBridge && defItem.format) {
            attrStr.push('data-cell-link="true"');
            value = '<a href="#" class="' + type + '-cell-link" data-cell-link-bridge-a="0">' + value + '</a>';
        }

        // 条件格式
        var condFmt = getCondFmt(wrap.style, type);
        if (condFmt) {
            value = (
                    condFmt.left.css.length > 0 || condFmt.left.style.length > 0
                ? '<span class="' + condFmt.left.css.join(' ')
                + '" style="' + condFmt.left.style.join(' ') + '">' + '</span>'
                : ''
                )
                + (
                        condFmt.text.css.length > 0 || condFmt.text.style.length > 0
                    ? '<span class="' + condFmt.text.css.join(' ')
                    + '" style="' + condFmt.text.style.join(' ') + '">' + value + '</span>'
                    : value
                    )
                + (
                        condFmt.right.css.length > 0 || condFmt.right.style.length > 0
                    ? '<span class="' + condFmt.right.css.join(' ')
                    + '" style="' + condFmt.right.style.join(' ') + '">' + '</span>'
                    : ''
                    );

            if (condFmt.outer.css.length > 0 || condFmt.outer.style.length > 0) {
                classStr.push.apply(classStr, condFmt.outer.css);
                styleStr.push.apply(styleStr, condFmt.outer.style);
            }
        }

        if (wrap.expand != null) {
            attrStr.push(
                    'data-e-c="' + (!wrap.expand ? 'expanded' : 'collapsed') + '"'
            );
            clz = type + '-e-c-icon ' + type
                + (!wrap.expand ? '-expanded-icon ' : '-collapsed-icon ');
            value = [
                    // '<div style="' + indentStyle + ' text-align:left;" class="'
                    '<div style="' + indentStyle + '" class="'
                    + type + '-tree-item">',
                    '<div class="' + clz + '"></div>',
                value,
                '</div>'
            ].join('');
        }
        else if (indentStyle) {
            value = '<div class="' + type + '-default-icon " style="' + indentStyle
//                + 'text-align:left;">' + value + '</div>';
                + '">' + value + '</div>';
        }

        return value;
    };

    /**
     * table生产完毕以后执行，触发sizechange事件
     *
     */
    UI_OLAP_TABLE_CLASS.$ready = function() {
        triggerEvent(this, 'sizechange');
    };

    /**
     * 浏览器resize时调整横滚的位置
     *
     * @override
     */
    UI_OLAP_TABLE_CLASS.$resize = function() {
        var me = this;
        UI_LOCKED_TABLE_CLASS.$resize.call(this);
        if (!this._bResizeTimeout) {
            this._bResizeTimeout = true;
            setTimeout(
                function() {
                    me._bResizeTimeout = false;
                    triggerEvent(me, 'sizechange');
                    me.$pagescroll();
                },
                100
            );
        }
    };

    /**
     * 绑定cell link
     *
     * @private
     */
    UI_OLAP_TABLE_CLASS.$bindCellLink = function() {
        var me = this;
        var tds = this.getOuter().getElementsByTagName('td');
        for (
            var i = 0, tdEl, aEls, aEl, o, j;
            tdEl = tds[i];
            i ++
            ) {
            if (tdEl.getAttribute('data-cell-link')) {
                aEls = tdEl.getElementsByTagName('a');

                o = getCellPosition(tdEl);
                for (j = 0; aEl = aEls[j]; j ++) {
                    if (aEl.getAttribute('data-cell-link-drill-a')) {
                        aEl.onclick = (function(wrap) {
                            return function() {
                                !me._bDisabled
                                && triggerEvent(
                                    me,
                                    'celllinkdrill',
                                    null,
                                    [wrap]
                                );
                                return false;
                            }
                        })(this._aRowHeadFields[o.y][o.x]);
                    }
                    else if (aEl.getAttribute('data-cell-link-bridge-a')) {
                        aEl.onclick = (function(colDefItem, rowDefItem) {
                            return function() {
                                var index = this.getAttribute('data-cell-link-bridge-a');
                                !me._bDisabled
                                && triggerEvent(
                                    me,
                                    'celllinkbridge',
                                    null,
                                    [colDefItem, rowDefItem, index]
                                );
                                return false;
                            }
                        })(
                            this._aColDefine[this._nLeftLock + o.x],
                            this._aRowDefine[o.y]
                        );
                    }
                }
            }
        }
    };

    /**
     * 点击某个cell的api
     *
     * @public
     * @param {number} rowIndex 内容行序号，从0开始
     * @param {number} colIndex 内容列序号，从0开始
     */
    // UI_OLAP_TABLE_CLASS.clickContentCell = function(rowIndex, colIndex) {
    //     var cell = this.getContentCell(rowIndex, colIndex);
    //     cell && cell.$handleCellClick();
    // };

    /**
     * 获取内容区单元格控件。
     *
     * @public
     * @param {number} rowIndex 内容行序号，从0开始
     * @param {number} colIndex 内容列序号，从0开始
     * @return {ecui.ui.Table.Cell} 单元格控件
     */
    UI_OLAP_TABLE_CLASS.getContentCell = function(rowIndex, colIndex) {
        rowIndex = this._aRows[rowIndex];
        return rowIndex && rowIndex.getCell(
                (this._nLeftLock || 0) + colIndex
        ) || null;
    };

    /**
     * 得到当前状态数据
     *
     * @public
     * @return {Object} 当前状态数据
     */
    UI_OLAP_TABLE_CLASS.getValue = function() {
        var rowChecked = [];
        var rows = this._aRows || [];
        for (var i = 0, row; i < rows.length; i ++) {
            if ((row = rows[i]) && row._bRowChecked) {
                rowChecked.push({ value: this._aRowDefine[i], index: i });
            }
        }
        return {
            rowChecked: rowChecked,
            rowDefine: (this._aRowDefine || []).slice(),
            colDefine: (this._aColDefine || []).slice()
            // 其他的value，后续随功能添加
        }
    };

    /**
     * 得到内容区域的row控件
     *
     * @protected
     */
    UI_OLAP_TABLE_CLASS.$getContentRow = function(rowIndex) {
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
    UI_OLAP_TABLE_CLASS.$initRowChecked = function() {
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
    UI_OLAP_TABLE_CLASS.$setRowChecked = function(rowCtrl, checked) {
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
        else if (rowCheckMode == 'SELECT' || rowCheckMode == 'SELECTONLY') {
            var rows = this._aRows || [];
            for (var i = 0, row, cell; i < rows.length; i ++) {
                if ((row = rows[i]) && row._bRowChecked) {
                    row._bRowChecked = false;
                    removeClass(row.getMain(), type + '-row-selected');
                    removeClass(getParent(row._eFill), type + '-row-selected');
                }
            }
            rowCtrl._bRowChecked = true;
            addClass(rowCtrl.getMain(), type + '-row-selected');
            addClass(getParent(rowCtrl._eFill), type + '-row-selected');
            rowCheck.rowCheckCount = 1;
        }

        return false;
    };

    /**
     * 内容行是否选中
     *
     * @private
     */
    UI_OLAP_TABLE_CLASS.$isRowChecked = function(rowCtrl) {
        return !!rowCtrl._bRowChecked;
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
                getView().bottom - getPosition(con.getOuter()).top
                - el.offsetHeight,
                con.getHeight() - el.offsetHeight
        ) + 'px';

        setStyle(el, 'zIndex', 1);
    }

    /**
     * 得到格式化的值
     *
     * @private
     * @param {string} cellType 为'ROWHCELL', 'HCELL', 'CCELL'
     * @param {Object} wrap 数据元素
     * @param {Object=} format 格式
     * @return {Object} value和prompt
     */
    function getWrapValue(cellType, wrap, format) {
        var value = wrap.str
            // 以str优先，如果没有则取v字段
            ? wrap.str
            : String(
                wrap.v == null
                ? '-'
                : format
                ? formatNumber(wrap.v, format, void 0, void 0, true)
                : wrap.v
        );
        var prompt;
        var cut = this._oCut[cellType];
        if (cut) {
            prompt = value;
            // 注释掉字符串截断
            value = sliceByte(value, cut, 'gbk');
            if (value.length < prompt.length) {
                value += '...';
            }
            /* 由于在ie7下 行头不能很好的设置宽度，所以ie7的行头统一加title；其它情况置空prompt */
            else if (!(dom.ieVersion < 8 && cellType == 'ROWHCELL')){
                prompt = null;
            }
        }
        return {
            value: encodeHTML(value),
            prompt: value && encodeHTML(value)
            // prompt: prompt && encodeHTML(prompt)
        };
    }

    /**
     * 如果wrap不是对象，包装成对象
     *
     * @private
     * @param {*} wrap 数据元素
     */
    function objWrap(wrap) {
        if (wrap !== Object(wrap)) {
            wrap = { v: wrap };
        }
        return wrap;
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

    /**
     * 判断是否placeholder（空对象为placeholder）
     */
    function isPlaceholder(o) {
        if (o !== Object(o)) {
            return false;
        }
        for (var i in o) {
            return false;
        }
        return true;
    }

    //--------------------------------------------------
    // UI_OLAP_TABLE_HCELL 方法
    //--------------------------------------------------

    UI_TABLE_HCELL_CLASS.$click = function () {
        var orderby;
        var tableCtrl = this.getParent();

        UI_CONTROL_CLASS.$click(this);

        if (orderby = this.getOuter().getAttribute('data-orderby')) {
            var pos = getCellPosition(this.getOuter());
            if (pos) {
                triggerEvent(
                    tableCtrl,
                    'sort',
                    null,
                    [tableCtrl._aColDefine[(tableCtrl._nLeftLock || 0) + pos.x]]
                );
            }
        }
    };

    /**
     * 渲染tips
     *
     * @public
     */
    UI_OLAP_TABLE_CLASS.$renderTips = function () {
        // var hCells = this._aHCells;
        var type = this.getType();
        var hCells;
        var headTableHead = dom.children(this._uHead._eBody)[0];
        headTableHead && (hCells = dom.children(headTableHead));
        if (!hCells) {
            return;
        }
        for (var i = 0; i < hCells.length; i ++) {
            // var el = hCells[i]._eBody;
            var el = hCells[i];
            var tipsEl = dom.getElementsByClass(el, 'span', type + '-head-tips');
            if (tipsEl.length > 0) {
                var target = tipsEl[0];
                /* globals esui */
                var tip = esui.create(
                    'Tip',
                    {
                        type: 'ui-tip',
                        content: target.getAttribute('data-message'),
                        showMode: 'over',
                        delayTime: 400,
                        showDuration: 400,
                        positionOpt: {top: 'bottom', left: 'left'},
                        main: target
                    }
                );
                tip.render();
            }
        }

        var rows = this._aRows;
        for (var x = 0; x < rows.length; x ++) {
            var cells = rows[x]._aElements;
            for (var y = 0; y < cells.length; y ++) {
                var el = cells[y];
                var ec;
                if (el.getAttribute('data-row-h') || (ec = el.getAttribute('data-e-c'))) {
                    var tipsEl = dom.getElementsByClass(el, 'span', 'tip-layer-div');
                    if (tipsEl.length > 0) {
                        var target = tipsEl[0];
                        /* globals esui */
                        var tipLayer = esui.create('TipLayer', {
                            arrow: 0,
                            content: target.getAttribute('data-message')
                        });

                        tipLayer.appendTo(document.body);
                        tipLayer.attachTo({
                            targetDOM: target,
                            showMode: 'over',
                            delayTime: 500,
                            showDuration: 500,
                            positionOpt: {top: 'bottom', left: 'left'}
                        });
                    }
                }
            }
        }

    };

    // UI_TABLE_HCELL_CLASS.$mouseover = function (event) {
    //     if (event.target
    //         && event.target.className === 'ui-table-head-tips'
    //         ) {
            
    //     }
    // };


    //--------------------------------------------------
    // UI_OLAP_TABLE_CELL 方法
    //--------------------------------------------------

    /**
     * 点击事件
     *
     * @event
     * @protected
     */
    UI_OLAP_TABLE_CELL_CLASS.$click = function(event) {
        UI_OLAP_TABLE_CLASS.Cell.superClass.$click.call(this, event);

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

    // UI_OLAP_TABLE_CELL_CLASS.$mouseover = function (event) {
    //     var el = this.getOuter();
    //     var tableCtrl = this.getParent().getParent();
    //     var ec;
    //     var target;
        
    // }

    /**
     * 处理cell点击事件
     *
     * @protected
     */
    UI_OLAP_TABLE_CELL_CLASS.$handleCellClick = function() {
        var el = this.getOuter();
        var tableCtrl = this.getParent().getParent();
        var ec;

        // 左表头节点
        if (el.getAttribute('data-row-h') && (ec = el.getAttribute('data-e-c'))) {
            if (getMouseX(this) <=
                toNumber(getStyle(el, 'paddingLeft'))
                + toNumber(getStyle(el.firstChild, 'marginLeft'))
                + toNumber(getStyle(el.firstChild, 'paddingLeft'))
                ) {
                var pos;
                var cellWrap;
                var rowWrap;
                if (pos = getCellPosition(this.getOuter())) {
                    cellWrap = tableCtrl._aRowHeadFields[pos.y][pos.x];
                    rowWrap = tableCtrl._aRowDefine[pos.y];
                }
                triggerEvent(
                    tableCtrl,
                    (ec == 'expanded' ? 'collapse' : 'expand'),
                    null,
                    [cellWrap, rowWrap, pos]
                );
            }
        }

        // 如果是内容节点
        if (el.getAttribute('data-content')) {
            var rowDefItem;
            if (pos = getCellPosition(this.getOuter())) {
                rowDefItem = tableCtrl._aRowDefine[pos.y];
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
                else if (rowCheckMode == 'SELECTONLY') {
                    tableCtrl.$setRowChecked(rowCtrl, true);
                    eventName = 'rowselectonly';
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
}) ();