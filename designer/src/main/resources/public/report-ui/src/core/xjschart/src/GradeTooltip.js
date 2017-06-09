/**
 * xjschart.GradeTooltip
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:   评级提示框
 * @author:  sushuang(sushuang)
 * @depend:  raphael
 */

(function() {
    
    //---------------------------------------
    // 引用 
    //---------------------------------------
    
    var X = xjschart;
    var R = Raphael;
    var UI_ELEMENT = X.UIElement;
    var util = X.util;
    var inheritsObject = util.inheritsObject;
    var assign = util.assign;
    var extend = util.extend;
    var clone = util.clone;
    var bind = util.bind;
    var isObject = util.isObject;
    var travelTree = util.travelTree;
    var getValue = util.getValue;
    var removeElement = util.removeElement;
    var STOP_SUB_TREE_TRAVEL = util.STOP_SUB_TREE_TRAVEL;
    var TOOLTIP = X.Tooltip;
        
    //---------------------------------------
    // 类型定义
    //---------------------------------------
    
    /**
     * 提示框
     *
     * @class
     * @extends xjschart.Tooltip
     */
    var GRADE_TOOLTIP = X.GradeTooltip = inheritsObject(
            TOOLTIP,
            gradeTooltipConstructor
        );
    var GRADE_TOOLTIP_PROTO = GRADE_TOOLTIP.prototype;
        
    //---------------------------------------
    // 常量
    //---------------------------------------

    /**
     * base
     */
    var BODY_STYLE = {
            fill: '#FDF2D2',
            stroke: '#FA9346',
            'stroke-width': 2
        };  
    var BODY_PADDING = 3;
    var BODY_RADIUS = 5;
    var EVENT_AGENT_CLICK_STYLE = { 
            fill:'#FFF', 
            opacity: 0, 
            cursor: 'pointer' 
        };
    var EVENT_AGENT_STYLE = { 
            fill:'#FFF', 
            opacity: 0
        };
    var EVENT_AGENT_DISABLE_STYLE = { 
            fill:'#FFF', 
            opacity: 0, 
            cursor: 'default' 
        };

    /**
     * grade
     */
    var MAX_GRADE = 5;
    var NONE_GRADE = 0;

    /**
     * unit
     */
    var STAR_PATH = 'M9.6,13.425C9.6,13.425,4.2696,17.298,4.2696,17.298C4.2696,17.298,6.3072,11.0352,6.3072,11.0352C6.3072,11.0352,0.9774,7.1604,0.9774,7.1604C0.9774,7.1604,7.5648,7.1616,7.5648,7.1616C7.5648,7.1616,9.6012,0.9,9.6012,0.9C9.6012,0.9,11.6358,7.1604,11.6358,7.1604C11.6358,7.1604,18.2244,7.1604,18.2244,7.1604C18.2244,7.1604,12.8928,11.0346,12.8928,11.0346C12.8928,11.0346,14.9304,17.298,14.9304,17.298C14.9304,17.298,9.6,13.425,9.6,13.425C9.6,13.425,9.6,13.425,9.6,13.425C9.6,13.425,9.6,13.425,9.6,13.425'
    var UNIT_WIDTH = 26;
    var UNIT_HEIGHT = 20;
    var UNIT_WIDTH_PADDING = 3;
    var UNIT_STYLE_NORMAL = {
            fill: '#CCCACB',
            stroke: '#BAB9B5',
            'stroke-width': 1
        };
    var UNIT_STYLE_SELECTED = {
            fill: '#FA7818', 
            stroke: '#EC6816', 
            'stroke-width': 1
        };
    var UNIT_STYLE_HOVER = {
            fill: '#FDAE75', 
            stroke: '#FD9C55', 
            'stroke-width': 1
        };

    /**
     * info bar
     */
    var INFO_BAR_MAGIN_TOP = 3;
    var CANCEL_BTN_STYLE = {            
            font: '12px "微软雅黑", Arial, serif, Helvetica', 
            fill: '#4F92D8',
            'text-anchor': 'start'
        };
    var CANCEL_BTN_TEXT = '取消标记';
    var CURR_VALUE_SYTLE = {
            font: '12px "微软雅黑", Arial, serif, Helvetica', 
            fill: '#F70201',
            'text-anchor': 'start'
        };
    var CURR_VALUE_TEXT = '重要性：';

    //----------------------------------------
    // GRADE_Tooltip 方法
    //----------------------------------------
    
    /**
     * 构造器
     *
     * @public
     * @constructor
     * @param {Object} options 初始化参数
     */
    function gradeTooltipConstructor(options) {

        this._oData.currGrade = NONE_GRADE;
        this._oData.hoverGrade = NONE_GRADE;
        this._nX = 0;
        this._nY = 0;

        this._oConfig = {};

        // grade
        this._oConfig.maxGrade = getValue(
            options.maxGrade,
            MAX_GRADE
        );

        // base
        this._oConfig.bodyStyle = getValue( 
            options.bodyStyle,
            BODY_STYLE
        );
        this._oConfig.bodyPadding = getValue(
            options.bodyPadding,
            BODY_PADDING
        );

        // unit
        this._oConfig.unitStyleNormal = getValue(
            options.unitStyleNormal, 
            UNIT_STYLE_NORMAL
        );
        this._oConfig.unitStyleSelected = getValue(
            options.unitStyleSelected, 
            UNIT_STYLE_SELECTED
        );
        this._oConfig.unitStyleHover = getValue(
            options.unitStyleHover,
            UNIT_STYLE_HOVER
        );

        // info bar
        this._oConfig.infoBarDisabled = !!options.infoBarDisabled;
        this._oConfig.cancelBtnText = getValue(
            options.cancelBtnText,
            CANCEL_BTN_TEXT
        );
        this._oConfig.cancelBtnStyle = getValue(
            options.cancelBtnStyle,
            CANCEL_BTN_STYLE
        );
        this._oConfig.currValueText = getValue(
            options.currValueText,
            CURR_VALUE_TEXT
        );
        this._oConfig.currValueStyle = getValue(
            options.currValueStyle,
            CURR_VALUE_SYTLE
        );
    }

    /**
     * 初始化
     *
     * @public
     * @override
     */
    GRADE_TOOLTIP_PROTO.init = function() {
        GRADE_TOOLTIP.superClass.init.call(this);
        this.$initBody();
        this.$initUnit();
        this.$initInfoBar();
        this.$resize();
        this.toFront();
        this.hide();
    };
    
    /**
     * 析构
     *
     * @public
     * @override
     */
    GRADE_TOOLTIP_PROTO.dispose = function() {
        removeElement(this, '_eCancelBtn');
        removeElement(this, '_stCancelBtn');
        removeElement(this, '_eCurrValue');
        removeElement(this, '_stCurrValue');
        removeElement(this, '_stInfoBar');
        removeElement(this, '_stUnit');
        removeElement(this, '_stUnitAgent');
        removeElement(this, '_stBody');
        removeElement(this, '_stBodyAgent');

        GRADE_TOOLTIP.superClass.dispose.call(this);
    };
    
    /**
     * 初始化unit
     * 
     * @protected
     */
    GRADE_TOOLTIP_PROTO.$initUnit = function() {
        var config = this._oConfig;
        var padding = config.bodyPadding + (config.bodyStyle['stroke-width'] || 0);
        var baseY = padding;
        var baseX = padding;

        this._stUnit = this._paper.set();
        this._stMainView.push(this._stUnit);
        this._stUnitAgent = this._paper.set();
        this._stMainAgent.push(this._stUnitAgent);
        this._aUnitStatus = [];

        for (var i = 1, x, y, path; i <= config.maxGrade; i ++) {
            x = baseX + (i - 1) * UNIT_WIDTH + UNIT_WIDTH_PADDING;
            y = baseY;
            path = R.transformPath(STAR_PATH, R.format('T{0},{1}', x, y));

            this._stUnit.push(
                this._paper.path(path).attr(config.unitStyleNormal)
            );
            // 事件代理
            this._stUnitAgent.push(
                this._paper.rect(x, y, UNIT_WIDTH, UNIT_HEIGHT)
                .attr(EVENT_AGENT_CLICK_STYLE)
                .toFront()
                .click(bind(this.$unitClickHandler, this, i))
                .hover(
                    bind(this.$unitHoverInHandler, this, i),
                    bind(this.$unitHoverOutHandler, this, i),
                    this,
                    this
                )
            );
            this._aUnitStatus.push('NORMAL');
        }
    };

    /**
     * 初始化info bar
     * 
     * @protected
     */
    GRADE_TOOLTIP_PROTO.$initInfoBar = function() {
        var config = this._oConfig;
        var el;
        var bbox;
        var padding = config.bodyPadding + (config.bodyStyle['stroke-width'] || 0);
        var baseX = padding + UNIT_WIDTH_PADDING + 1;
        var baseY = padding + INFO_BAR_MAGIN_TOP + UNIT_HEIGHT;

        this._stInfoBar = this._paper.set();
        this._stMainView.push(this._stInfoBar);        

        // 当前值显示
        this._stCurrValue = this._paper.set().push(
            (
                el = this._paper
                    .text(baseX, 0, config.currValueText)
                    .attr(config.currValueStyle)
            )
        );
        bbox = el.getBBox();
        var halfTextFont = Math.round(bbox.height / 2);
        el.attr({ y: baseY + halfTextFont });
        baseX += bbox.width;

        this._stCurrValue.push(
            (
                this._eCurrValue = this._paper
                    .text(baseX, baseY + halfTextFont, '0')
                    .attr(config.currValueStyle)
            )
        );
        this._stInfoBar.push(this._stCurrValue);

        // 取消按钮
        this._stCancelBtn = this._paper.set().push(
            (
                this._eCancelBtn = this._paper
                    .text(0, baseY + halfTextFont, config.cancelBtnText)
                    .attr(config.cancelBtnStyle)
            )
        );
        this._stInfoBar.push(this._stCancelBtn);

        // 事件代理
        bbox = this._stCancelBtn.getBBox();
        this._eCancelBtnAgent = this._paper
            .rect(bbox.x, bbox.y, bbox.width, bbox.height)
            .attr(EVENT_AGENT_CLICK_STYLE);
        this._stMainAgent.push(this._eCancelBtnAgent);
        this._eCancelBtnAgent.click(bind(this.$cancelHandler, this));
        this._eCancelBtnAgent.hover(
            bind(this.$cancelHoverInHandler, this),
            bind(this.$cancelHoverOutHandler, this),
            this,
            this
        );
    };

    /**
     * 初始化body
     * 
     * @protected
     */
    GRADE_TOOLTIP_PROTO.$initBody = function() {
        var config = this._oConfig;

        this._stBody = this._paper.set().push(
            this._paper.rect(0, 0, 1, 1, BODY_RADIUS)
            .attr(config.bodyStyle)
        );
        this._stMainView.push(this._stBody);

        // 事件代理
        this._stBodyAgent = this._paper.set().push(
            this._paper.rect(0, 0, 1, 1, BODY_RADIUS)
            .attr(EVENT_AGENT_STYLE)
        );
        this._stMainAgent.push(this._stBodyAgent);
        this._stBodyAgent.hover(
            this.$bodyHoverInHandler, 
            this.$bodyHoverOutHandler, 
            this,
            this
        );
    };

    /**
     * 重置事件代理的z-index
     *
     * @protected
     */
    GRADE_TOOLTIP_PROTO.toFront = function() {
        this._stMainView.toFront();
        this._stBodyAgent.toFront();
        for (var i = 0; i < this._stUnitAgent.length; i ++) {
            this._stUnitAgent[i].toFront();
        }
        this._eCancelBtnAgent.toFront();
    };

    /**
     * 重置尺寸
     * @protected
     */
    GRADE_TOOLTIP_PROTO.$resize = function() {
        var config = this._oConfig;
        var padding = config.bodyPadding + (config.bodyStyle['stroke-width'] || 0);
        var bbox;

        // 计算当前尺寸
        bbox = this._stInfoBar.getBBox();
        this._nWidth = 2 * padding + this._stUnit.length * UNIT_WIDTH;
        this._nHeight = 2 * padding + UNIT_HEIGHT + INFO_BAR_MAGIN_TOP + bbox.height;

        // 放置infobar
        bbox = this._eCancelBtn.getBBox();
        var baseX = this._nWidth - padding - bbox.width - UNIT_WIDTH_PADDING - 1;
        baseX = baseX >= 0 ? baseX : 0;
        this._eCancelBtn.attr('x', baseX);
        this._eCancelBtnAgent.attr('x', baseX);

        // body尺寸
        this._stBody.attr({ width: this._nWidth, height: this._nHeight });
        this._stBodyAgent.attr({ width: this._nWidth, height: this._nHeight });
    };

    /**
     * 设置数据
     *
     * @public
     * @param {Object} data 数据，如果为null则清空
     *          {number} currGrade
     *          ... 其余属性可自己定义
     * @param {boolean} isSilent 是否静默，
     *              静默则不执行视图改变，可直接调用render改变视图
     */
    GRADE_TOOLTIP_PROTO.setData = function(data, isSilent) {
        GRADE_TOOLTIP.superClass.setData.apply(this, arguments);

        // validate
        if (this._oData.currGrade == null
            || this._oData.currGrade < 0
            || this._oData.currGrade > this._oConfig.maxGrade
        ) {
            this._oData.currGrade = NONE_GRADE;
        }
    };

    /**
     * 渲染
     *
     * @public
     * @override
     */
    GRADE_TOOLTIP_PROTO.$render = function() {
        var config = this._oConfig;

        // 星标
        var i = 1;
        for (; i <= this._oData.currGrade; i ++) {
            if (this._aUnitStatus[i - 1] != 'SELECTED') {
                this._aUnitStatus[i - 1] = 'SELECTED';
                this._stUnit[i - 1].attr(config.unitStyleSelected);
            }
        }
        for (; i <= this._oData.hoverGrade; i ++) {
            if (this._aUnitStatus[i - 1] == 'NORMAL') {
                this._aUnitStatus[i - 1] = 'HOVER';
                this._stUnit[i - 1].attr(config.unitStyleHover);
            }
        }
        for (; i <= this._stUnit.length; i ++) {
            if (this._aUnitStatus[i - 1] != 'NORMAL') {
                this._aUnitStatus[i - 1] = 'NORMAL';
                this._stUnit[i - 1].attr(config.unitStyleNormal);
            }
        }

        // 当前值
        this._eCurrValue.attr({ text: String(this._oData.currGrade) });
    };

    /**
     * 移动
     *
     * @public
     * @override
     * @param {number} x x坐标
     * @param {number} y y坐标
     */
    GRADE_TOOLTIP_PROTO.setPosition = function(x, y) {
        this._nX = x;
        this._nY = y;
        this._stMainView.transform(
            R.format('T{0},{1}', x, y)
        );
        this._stMainAgent.transform(
            R.format('T{0},{1}', x, y)
        );
        this.toFront();
    };

    /**
     * 点是否在tooltip中
     *
     * @public
     * @param {number} x x坐标
     * @param {number} y y坐标
     */
    GRADE_TOOLTIP_PROTO.isPointInsideTooltip = function(x, y) {
        return R.isPointInsideBBox(this._stBodyAgent.getBBox(), x, y);
    };

    /**
     * 产生mouseout标志
     * 调用此函数是为了：避免鼠标过快，
     * 不能触发body其余部分的mouseover(hoverin)
     *
     * @protected
     */
    GRADE_TOOLTIP_PROTO.$produceMouseOutFlag = function() {
        // 产生一个标志，供body消费
        this._bJustUnitHoverOut = true;

        var me = this;
        clearTimeout(this._nJustUnitHoverOutTimeout);
        this._nJustUnitHoverOutTimeout = setTimeout(
            function() {
                // 检查标志是否被消费
                if (me._bJustUnitHoverOut) {
                    me.$bodyHoverInHandler();
                    me.$bodyHoverOutHandler();
                }
            },
            50
        );
    };

    /**
     * 将标志消费
     *
     * @protected
     */
    GRADE_TOOLTIP_PROTO.$consumeMouseOutFlag = function() {
        this._bJustUnitHoverOut = false;
    };

    /**
     * 取消事件处理
     *
     * @protected
     */
    GRADE_TOOLTIP_PROTO.$cancelHandler = function() {
        if (!this._bDisabled) {
            this._oData.currGrade = NONE_GRADE;
            this.render();
            this.notify('change', [clone(this._oData)]);
        }
    };

    /**
     * 取消按钮 mouse over事件处理
     *
     * @protected
     */
    GRADE_TOOLTIP_PROTO.$cancelHoverInHandler = function() {
        this.$consumeMouseOutFlag();
        this._bHover = true;
    };

    /**
     * 取消按钮 mouse out事件处理
     *
     * @protected
     */
    GRADE_TOOLTIP_PROTO.$cancelHoverOutHandler = function() {
        this.$produceMouseOutFlag();
    };

    /**
     * unit点击事件处理
     *
     * @protected
     */
    GRADE_TOOLTIP_PROTO.$unitClickHandler = function(unitIndex) {
        if (!this._bDisabled) {
            this._oData.currGrade = unitIndex;
            this.render();
            this.notify('change', [clone(this._oData)]);
        }
    };

    /**
     * unit hover in事件处理
     *
     * @protected
     */
    GRADE_TOOLTIP_PROTO.$unitHoverInHandler = function(unitIndex) {
        this.$consumeMouseOutFlag();
        this._bHover = true;
        this._oData.hoverGrade = unitIndex;
        this.render();
    };

    /**
     * unit hover out事件处理
     *
     * @protected
     */
    GRADE_TOOLTIP_PROTO.$unitHoverOutHandler = function(unitIndex) {
        this._oData.hoverGrade = NONE_GRADE;
        this.render();
        this.$produceMouseOutFlag();
    };

    /**
     * body hover in事件处理
     *
     * @protected
     */
    GRADE_TOOLTIP_PROTO.$bodyHoverInHandler = function() {
        this.$consumeMouseOutFlag();

        this._bHover = true;
    };

    /**
     * body hover out事件处理
     *
     * @protected
     */
    GRADE_TOOLTIP_PROTO.$bodyHoverOutHandler = function() {
        this._bHover = false;
        this.hideDefer();
    };

})();
