/**
 * xjschart.IstBrainChart
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    insight的脑图（继承了BrainChart，此类中主要负责渲染）
 * @author:  sushuang(sushuang)
 * @depend:  raphael
 */

(function() {
    
    //-----------------------------------
    // 引用 
    //-----------------------------------
    
    var X = xjschart;
    var R = Raphael;
    var UI_ELEMENT = X.UIElement;
    var util = X.util;
    var inheritsObject = util.inheritsObject;
    var assign = util.assign;
    var clone = util.clone;
    var extend = util.extend;
    var travelTree = util.travelTree;
    var removeElement = util.removeElement;
    var overrideShow = util.overrideShow;
    var bind = util.bind;
    var arrayPush = Array.prototype.push;
    var min = Math.min;
    var max = Math.max;
    var BRAIN_CHART = X.BrainChart;
    var BRAIN_CHART_PROTO = BRAIN_CHART.prototype;
    var TOOLTIP = X.GradeTooltip;

    //-----------------------------------
    // 类型定义 
    //-----------------------------------

    /**
     * IST风格的脑图
     *
     * @class
     * @extends xjschart.BrainChart
     */
    var IST_BRAIN_CHART = X.IstBrainChart = inheritsObject(
            BRAIN_CHART,
            istBrainChartConstructor
        );
    var IST_BRAIN_CHART_PROTO = IST_BRAIN_CHART.prototype;
        
    /**
     * IST风格的脑图的节点
     *
     * @class
     * @extends xjschart.BrainChart.prototype.Node 
     *
     * {Object} data 节点内容
     *          {number} id
     *          {string} title
     *          {string} type 节点类型，两种：'DATA'和'LABEL'，'DATA'是默认值
     *          {boolean} bodyClickDisabled
     *          {string} mainNumStr
     *          {number} mainNum
     *          {string} indFormat
     *          {number} canExpend
     *          {boolean} expended
     *          {number} currGrade
     *          {Array{Object}} poles
     *              {string} poleNumStr
     *              {number} poleNum
     *              {string} polePercentStr
     *              {number} polePercent
     */
    var IST_BRAIN_CHART_NODE_PROTO = (
            IST_BRAIN_CHART_PROTO.Node = inheritsObject(BRAIN_CHART_PROTO.Node)
        ).prototype;

    /**
     * IST风格的脑图的连接器
     *
     * @class   
     * @extends xjschart.BrainChart.prototype.Connecter
     */
    var IST_BRAIN_CHART_CONNECTER = 
            IST_BRAIN_CHART_PROTO.Connecter = 
                inheritsObject(BRAIN_CHART_PROTO.Connecter);

    var IST_BRAIN_CHART_CONNECTER_PROTO = IST_BRAIN_CHART_CONNECTER.prototype;
        
    /**
     * 布点策略，视觉调整
     *
     * @class
     * @extends xjschart.BrainChart.prototype.LayoutStrategy
     */
    var IST_BRAIN_CHART_LAYOUT_STRATEGY_PROTO = (
            IST_BRAIN_CHART_PROTO.LayoutStrategy = 
                inheritsObject(BRAIN_CHART_PROTO.LayoutStrategy)
        ).prototype;

    //-----------------------------------
    // 常量 
    //-----------------------------------

    var FIRST_TEXT_OFFSET_Y = 11;
    var BORDER_STYLE = '1px solid #E3E3E3';
    var MARGIN = {
            top: 5,
            right: 15,
            bottom: 5,
            left: 15
        };
    var GRADE_RADIO = 7;
    var GRADE_BODY_STYLE = {
            fill: '#F70102',
            stroke: '#F70102',
            'stroke-width': R.svg ? 1 : 0 // ie78半像素问题
        };
    var GRADE_FONT = {
            font: R.svg ? '11px Arial' : '14px Times',
            'font-weight': 'bold', 
            fill: '#FFF'
        };
    var NODE_TITLE_FONT = {
            font: '15px "微软雅黑", "Hoefler Text", serif, Arial',
            'font-weight': 'bold', 
            fill: '#2581B1',
            'text-anchor': 'start' 
        };
    var NODE_TITLE_DISABLED_FONT = extend(
            {}, 
            NODE_TITLE_FONT,
            { fill: '#6BB8E0' }
        );
    var NODE_MAIN_NUM_FONT = {
            font: '14px Arial, serif, Helvetica', 
            fill: '#000', 
            'font-weight': 'bold',
            'text-anchor': 'start' 
        }; 
    var NODE_MAIN_NUM_DISABLED_FONT = extend(
            {}, 
            NODE_MAIN_NUM_FONT, 
            { fill: '#888' }
        );
    var NODE_POLE_NUM_FONT = {
            font: '14px Arial, serif, Helvetica', 
            fill: '#000',
            'text-anchor': 'start'
        }; 
    var NODE_POLE_NUM_DISABLED_FONT = extend(
            {}, 
            NODE_POLE_NUM_FONT, 
            { fill: '#888' }
        );
    var BOTTOM_LINE_STYLE = { stroke: '#999' }; 
    var CONNECTOR_STYLE = { stroke: '#999' }; 
    var BOTTOM_LINE_GLOW = { 
            width: 2, 
            color: '#C5C5C5' 
        };
    var EVENT_AGENT_STYLE = { 
            fill:'#FFF', 
            opacity: 0, 
            cursor: 'pointer' 
        };
    var EVENT_AGENT_DISABLED_STYLE = extend(
            {}, 
            EVENT_AGENT_STYLE, 
            { cursor: 'default' }
        );
    var NODE_WIDTH = 194; // 节点宽度
    var BODY_AGENT_WIDTH = 180;
    var NODE_INTERVAL_X = 50;
    var NODE_INTERVAL_Y = 20;
    var BOTTOM_LINE_LENGTH = 192; // 底线长度
    var CONNECTER_BASE_INERVAL = 25;
    var TITLE_STEP_Y = 20;
    var LABEL_MARGIN_BOTTOM = 7;
    var MAIN_NUM_STEP_Y = 20;
    var POLE_NUM_STEP_Y = 18; 
    var BOTTOM_LINE_STEP_Y = 14; 
    var POLE_NUM_WIDTH = 124;  // pole值宽度
    var CONTENT_X = 5;
    var CONTENT_Y = 10; 
    var COLLAPSE_BTN_BG_STYLE = { fill:'#FFF', stroke: '#999' };
    var COLLAPSE_BTN_LINE_STYLE = { stroke: '#999' };
    var COLLAPSE_BTN_RADIO = 7;

    //----------------------------------------------
    // BrainChart 方法
    //----------------------------------------------

    /**
     * IstBrainChart的构造函数
     *
     * @public
     */
    function istBrainChartConstructor() {
        // 初始化tooltip
        this._uTooltip = new TOOLTIP({ paper: this._paper });
    }

    /**
     * 初始化
     *
     * @public
     * @override
     */
    IST_BRAIN_CHART_PROTO.init = function() {
        IST_BRAIN_CHART.superClass.init.apply(this, arguments);

        this._uTooltip.init();

        // grade tooltip选择事件
        this._uTooltip.attach('change', bind(this.$tooltipClickHandler, this));
    };

    /**
     * 析构
     *
     * @public
     * @override
     */
    IST_BRAIN_CHART_PROTO.dispose = function() {
        this._uTooltip.dispose();
        this._uTooltip = null;
        IST_BRAIN_CHART.superClass.dispose.apply(this, arguments);
    };

    /**
     * 预处理初始化参数
     *
     * @protected
     * @override
     */
    IST_BRAIN_CHART_PROTO.$processOptions = function(options) {
        IST_BRAIN_CHART.superClass.$processOptions.call(this, options);
        options.nodeWidth = NODE_WIDTH;
        options.nodeIntervalX = NODE_INTERVAL_X;
        options.nodeIntervalY = NODE_INTERVAL_Y;        
        options.borderStyle = BORDER_STYLE;
        options.marginTop = MARGIN.top;
        options.marginRight = MARGIN.right;
        options.marginBottom = MARGIN.bottom;
        options.marginLeft = MARGIN.left;
        return options;
    };
    
    /**
     * 预处理初始化参数
     *
     * @protected
     * @override
     */
    IST_BRAIN_CHART_PROTO.$tooltipClickHandler = function(tooltipData) {
        var node = this._oNodeMap[tooltipData.nodeId];
        node._oData.currGrade = tooltipData.currGrade;
        node.repaintGrade();
    };

    //----------------------------------------------
    // BrainChart Node 方法
    //----------------------------------------------

    /**
     * @protected
     * @override
     */
    IST_BRAIN_CHART_NODE_PROTO.$dispose = function() {
        removeElement(this, '_eCollapseBtnMinus');
        removeElement(this, '_eCollapseBtnMinusPath');
        removeElement(this, '_stCollapseBtnMinus');
        removeElement(this, '_eCollapseBtnPlus');
        removeElement(this, '_eCollapseBtnPlusPath');
        removeElement(this, '_stCollapseBtnPlus');
        removeElement(this, '_eCollapseBtnAgent');
        removeElement(this, '_eGradeText');
        removeElement(this, '_stGrade');
        removeElement(this, '_eBodyAgent');
        removeElement(this, '_stContent');

        IST_BRAIN_CHART_PROTO.Node.superClass.$dispose.call(this);
    };
    
    /**
     * @protected
     * @override
     */
    IST_BRAIN_CHART_NODE_PROTO.$clearContent = function() {
        this._stContent.remove();
        this._stContent.clear();
        this._stGrade && this._stGrade.remove();
    };

    /**
     * @protected
     * @override
     */
    IST_BRAIN_CHART_NODE_PROTO.$setModel = function(data) {
        this._oData = assign(
            this._oData || {}, 
            data, 
            [
                'id', 'title', 'mainNumStr', 'mainNum',
                'canExpend', 'expended', 'indFormat', 'indId'
            ]
        );
        this._oData.poles = clone(data.poles);
        
        if (data.currGrade != null) {
            this._oData.currGrade = data.currGrade;
        }
        if (this._oData.currGrade == null) {
            this._oData.currGrade = 0;
        }
    };
    
    /**
     * @protected
     * @override
     */
    IST_BRAIN_CHART_NODE_PROTO.$initCollapseBtn = function() {
        this._bCollapseBtnDisabled = false;

        var half = COLLAPSE_BTN_RADIO - 3;
        this._stCollapseBtnMinus = this._paper.set().push(
            (
                this._eCollapseBtnMinus = this._paper
                    .circle(0, 0, COLLAPSE_BTN_RADIO)
                    .attr(COLLAPSE_BTN_BG_STYLE)
                    .hide()
            ),
            (
                this._eCollapseBtnMinusPath = this._paper
                    .path(
                        this._sCollapseBtnMinusPath = 
                            R.format('M{0},{1}T{2},{3}', -half, 0, half, 0)
                    )
                    .attr(COLLAPSE_BTN_LINE_STYLE)
                    .show()
            )
        );
        this._stCollapseBtnPlus = this._paper.set().push(
            (
                this._eCollapseBtnPlus = this._paper
                    .circle(0, 0, COLLAPSE_BTN_RADIO)
                    .attr(COLLAPSE_BTN_BG_STYLE)
                    .hide()
            ),
            (
                this._eCollapseBtnPlusPath = this._paper
                    .path(
                        this._sCollapseBtnPlusPath = R.format(
                            'M{0},{1}T{2},{3}M{4},{5}T{6},{7}', 
                            -half, 0, half, 0, 0, -half, 0, half
                        )
                    )
                    .attr(COLLAPSE_BTN_LINE_STYLE)
                    .show()
            )
        );
        this._stMainView.push(this._stCollapseBtnMinus);
        this._stMainView.push(this._stCollapseBtnPlus);
        
        // 事件代理
        this._eCollapseBtnAgent = this._paper
            .circle(0, 0, COLLAPSE_BTN_RADIO + 3)
            .attr(EVENT_AGENT_STYLE)
            .hide();
        this._stMainAgent.push(this._eCollapseBtnAgent);
        this._eCollapseBtnAgent.click(bind(this.$collapseHandler, this));
    };
    
    /**
     * @protected
     * @override
     */
    IST_BRAIN_CHART_NODE_PROTO.$initContent = function() {
        this._stContent = this._paper.set();
        this._stMainView.push(this._stContent);
    };
    
    /**
     * @protected
     * @override
     */
    IST_BRAIN_CHART_NODE_PROTO.$initBody = function() {
        this._eBodyAgent = this._paper
            .rect(0, 0, BODY_AGENT_WIDTH, 1)
            .attr(EVENT_AGENT_STYLE)
            .hide();
        this._stMainAgent.push(this._eBodyAgent);
        this._eBodyAgent.click(bind(this.$bodyClickHandler, this));
        this._eBodyAgent.hover(
            this.$bodyHoverInHandler,
            this.$bodyHoverOutHandler,
            this,
            this
        );
    };
    
    /**
     * @protected
     * @override
     */
    IST_BRAIN_CHART_NODE_PROTO.$orderAgent = function() {
        this._eBodyAgent.toFront();
        this._eCollapseBtnAgent.toFront();
    };
    
    /**
     * @protected
     * @override
     */
    IST_BRAIN_CHART_NODE_PROTO.$render = function() {
        var data = this._oData;
        var paper = this._paper;
        var bbox;
        var eTitle;
        var baseY = 0;

        // 清空
        this.$clearContent();
        
        // 渲染标题
        baseY = CONTENT_Y + FIRST_TEXT_OFFSET_Y;
        this._stContent.push(
            eTitle = paper
                .text(CONTENT_X, baseY, data.title || '-')
                .attr(NODE_TITLE_FONT)
                .data('typeName', 'TITLE')
        );

        // 渲染"重要性"级别
        bbox = eTitle.getBBox();
        var eGradeBody;
        this._stGrade = this._paper.set().push(
            eGradeBody = paper
                .circle(
                    bbox.x2 + GRADE_RADIO + 5, 
                    baseY + (R.svg ? 0 : -2), 
                    GRADE_RADIO
                )
                .attr(GRADE_BODY_STYLE)
        );
        this._titleXEnd = bbox.x2 + GRADE_RADIO * 2 + 15;
        
        bbox = eGradeBody.getBBox();
        var gradeRadio = Math.round(bbox.height / 2);
        this._stGrade.push(
            this._eGradeText = paper
                .text(
                    Math.round(bbox.x) + gradeRadio,
                    Math.round(bbox.y) + gradeRadio, 
                    data.currGrade || 0
                )
                .attr(GRADE_FONT)
        );
        this._stGrade._bVisible = true;
        overrideShow(this._stGrade, [this]);
        this._stContent.push(this._stGrade);
        this.repaintGrade();

        // 渲染'DATA'类型节点数据
        if (data.type == 'DATA') {
            this._stContent.push(
                paper.text(
                    CONTENT_X, 
                    (baseY += TITLE_STEP_Y), 
                    data.mainNumStr || '-'
                )
                .attr(NODE_MAIN_NUM_FONT)
                .data('typeName', 'MAIN_NUM')
            );

            baseY += MAIN_NUM_STEP_Y;
        
            for (var i = 0, o, stepY; o = (data.poles || [])[i]; i++) {
                stepY = i == 0 ? 0 : POLE_NUM_STEP_Y;
                this._stContent.push(paper
                    .text(
                        CONTENT_X, 
                        (baseY += stepY), 
                        o.poleNumStr || '-'
                    )
                    .attr(NODE_POLE_NUM_FONT)
                    .data('typeName', 'POLE_NUM')
                );
                this._stContent.push(paper
                    .text(
                        CONTENT_X + POLE_NUM_WIDTH, 
                        baseY, 
                        o.polePercentStr || '-'
                    )
                    .attr(NODE_POLE_NUM_FONT)
                    .data('typeName', 'POLE_NUM')
                );
            }
        }
        // 渲染'LABEL'类型节点数据
        else if (data.type == 'LABEL') { 
            baseY += LABEL_MARGIN_BOTTOM;
        }

        // 渲染基线
        this._stContent.push(paper
            .path(
                R.format('M{0},{1}H{2}', 
                0, 
                (baseY += BOTTOM_LINE_STEP_Y), 
                BOTTOM_LINE_LENGTH)
            )
            .attr(BOTTOM_LINE_STYLE)
        );
        
        // 放置展开按钮
        this.$placeCollapseBtn(BOTTOM_LINE_LENGTH + COLLAPSE_BTN_RADIO, baseY);

        // 事件代理
        this._eBodyAgent.attr({
            height: (baseY - 5) > 0 ? (baseY - 5) : 0
        })
        .toFront();

        data.bodyClickDisabled 
            ? this._eBodyAgent.attr(EVENT_AGENT_DISABLED_STYLE)
            : this._eBodyAgent.attr(EVENT_AGENT_STYLE);

        // 连接部件
        this._oConnecterBeginPoint = {
            x: 0, 
            y: baseY
        };
        this._oConnecterEndPoint = {
            x: BOTTOM_LINE_LENGTH + COLLAPSE_BTN_RADIO + COLLAPSE_BTN_RADIO, 
            y: baseY
        };
        
        // 大小设置
        this._nWidth = this._oConfig.nodeWidth;
        this._nHeight = baseY + COLLAPSE_BTN_RADIO + 2;
        
        // transform
        this._sTransform && this._stMain.transform(this._sTransform);
         
        // 显示
        this.$show();
    };
    
    /**
     * 禁用操作
     *
     * @public
     * @override
     */
    IST_BRAIN_CHART_NODE_PROTO.disable = function() {
        if (!this._bDisabled) {
            this._eBodyAgent.attr(EVENT_AGENT_DISABLED_STYLE)
            this._uContainer._uTooltip.disable();
            this._stContent.forEach(
                function(itemEl, i) {
                    if (itemEl.data('typeName') == 'TITLE') {
                        itemEl.attr(NODE_TITLE_DISABLED_FONT);
                    }
                    else if (itemEl.data('typeName') == 'MAIN_NUM') {
                        itemEl.attr(NODE_MAIN_NUM_DISABLED_FONT);
                    }
                    else if (itemEl.data('typeName') == 'POLE_NUM') {
                        itemEl.attr(NODE_POLE_NUM_DISABLED_FONT);
                    }
                },
                this
            );
        }
        IST_BRAIN_CHART_PROTO.Node.superClass.disable.apply(this, arguments);
    };

    /**
     * 启用操作
     *
     * @public
     * @override
     */
    IST_BRAIN_CHART_NODE_PROTO.enable = function() {
        if (this._bDisabled) {
            this._eBodyAgent.attr(EVENT_AGENT_STYLE);
            this._uContainer._uTooltip.enable();
            this._stContent.forEach(
                function(itemEl, i) {
                    if (itemEl.data('typeName') == 'TITLE') {
                        itemEl.attr(NODE_TITLE_FONT);
                    }
                    else if (itemEl.data('typeName') == 'MAIN_NUM') {
                        itemEl.attr(NODE_MAIN_NUM_FONT);
                    }
                    else if (itemEl.data('typeName') == 'POLE_NUM') {
                        itemEl.attr(NODE_POLE_NUM_FONT);
                    }
                },
                this
            );
        }
        IST_BRAIN_CHART_PROTO.Node.superClass.enable.apply(this, arguments);
    };

    /**
     * 禁用扩展合起按钮操作
     *
     * @public
     * @override
     */
    IST_BRAIN_CHART_NODE_PROTO.disableCollapseBtn = function() {
        if (!this._bCollapseBtnDisabled) {
            this._eCollapseBtnAgent.attr(EVENT_AGENT_DISABLED_STYLE);
            this._bCollapseBtnDisabled = true;
        }
    };

    /**
     * 启用扩展合起按钮操作
     *
     * @public
     * @override
     */
    IST_BRAIN_CHART_NODE_PROTO.enableCollapseBtn = function() {
        if (this._bCollapseBtnDisabled) {
            this._eCollapseBtnAgent.attr(EVENT_AGENT_STYLE);
            this._bCollapseBtnDisabled = false;
        }
    };

    /**
     * 重绘grade显示
     *
     * @public
     * @override
     */
    IST_BRAIN_CHART_NODE_PROTO.repaintGrade = function() {
        var grade = this._oData.currGrade;
        if (!grade || grade <= 0) {
            this._stGrade._bVisible = false;
            this._stGrade.hide();
        } 
        else {
            this._stGrade._bVisible = true;
            this._stGrade.show();
            this._eGradeText.attr({ text: String(grade) });
        }
    };

    /**
     * @protected
     * @override
     */
    IST_BRAIN_CHART_NODE_PROTO.$showCollapseBtn = function() {
        var data = this._oData;
        (this._bVisible && data.canExpend && !data.expended) 
            ? this._stCollapseBtnPlus.show() 
            : this._stCollapseBtnPlus.hide();
        (this._bVisible && data.canExpend && data.expended) 
            ? this._stCollapseBtnMinus.show() 
            : this._stCollapseBtnMinus.hide();
        (this._bVisible && data.canExpend) 
            ? this._eCollapseBtnAgent.show() 
            : this._eCollapseBtnAgent.hide();
    };
    
    /**
     * @protected
     * @override
     */
    IST_BRAIN_CHART_NODE_PROTO.$show = function() {
        var stGrade = this._stGrade;

        if (this._bVisible) {
            this._stContent.show()
            this._stMainAgent.show();
        } 
        else {
            this._stContent.hide();        
            this._stMainAgent.hide();
        }

        this.$showCollapseBtn();
    };

    /**
     * @protected
     * @override
     */
    IST_BRAIN_CHART_NODE_PROTO.$getConnecterBeginPoint = function() {
        return extend({}, this._oConnecterBeginPoint);
    };

    /**
     * @protected
     * @override
     */
    IST_BRAIN_CHART_NODE_PROTO.$getConnecterEndPoint = function() {
        return extend({}, this._oConnecterEndPoint);
    };
    
    /**
     * 放置按钮
     * @protected
     */
    IST_BRAIN_CHART_NODE_PROTO.$placeCollapseBtn = function(x, y) {
        this._eCollapseBtnMinus.attr({ cx: x, cy: y });
        this._eCollapseBtnPlus.attr({ cx: x, cy: y });
        this._eCollapseBtnMinusPath.attr(
            'path', 
            R.transformPath(
                this._sCollapseBtnMinusPath, R.format('T{0},{1}', x, y)
            )
        );
        this._eCollapseBtnPlusPath.attr(
            'path', 
            R.transformPath(
                this._sCollapseBtnPlusPath, R.format('T{0},{1}', x, y)
            )
        );
        this._eCollapseBtnAgent.attr({ cx: x, cy: y });
    };

    /**
     * 展开按钮点击事件
     *
     * @protected
     */
    IST_BRAIN_CHART_NODE_PROTO.$collapseHandler = function() {
        var data = this._oData,
            willCollpase = data.expended,
            brainChart = this._uContainer;

        if (this._bDisabled || this._bCollapseBtnDisabled) { 
            return;
        }

        /**
         * @event expend 点击展开时触发，返回false则不展开
         * @event collapse 点击折叠时触发，返回false则不折叠
         */            
        if (brainChart.notify(
                willCollpase ? 'collapse' : 'expend', 
                [this]
            ) !== false
        ) {
            brainChart[willCollpase ? 'collapse' : 'expend'](this);
        }
    };

    /**
     * 节点点击事件
     *
     * @protected
     */
    IST_BRAIN_CHART_NODE_PROTO.$bodyClickHandler = function() {
        if (!this._bDisabled && !this._oData.bodyClickDisabled) {
            this._uContainer.notify('select', [this.getData()]);
        }
    };

    /**
     * 节点hover in事件
     *
     * @protected
     */
    IST_BRAIN_CHART_NODE_PROTO.$bodyHoverInHandler = function() {
        var container = this._uContainer;
        var tooltip = container._uTooltip;
        tooltip.setHover(true);
        tooltip.show();
        tooltip.setData(
            { 
                currGrade: this._oData.currGrade,
                nodeId: this._oData.id
            }
        );
        var x = this._nX + min(BODY_AGENT_WIDTH - 2, this._titleXEnd);
        var winX = -container._nContentX;
        x = max(x, 2);
        x = min(x, winX + container._nWidth - tooltip.getWidth() - 2);
        var y = this._nY + 2;
        var winY = -container._nContentY;
        y = max(y, 2);
        y = min(y, winY + container._nHeight - tooltip.getHeight() - 2);
        tooltip.setPosition(x, y);
    };

    /**
     * 节点hover out事件
     *
     * @protected
     */
    IST_BRAIN_CHART_NODE_PROTO.$bodyHoverOutHandler = function(e, x, y) {
        var tooltip = this._uContainer._uTooltip;
        tooltip.setHover(false);
        tooltip.hideDefer();
    };

    //---------------------------------------------------
    // BrainChartNode connecter 方法
    //---------------------------------------------------

    /*
     * @protected
     * @override
     */
    IST_BRAIN_CHART_CONNECTER_PROTO.$render = function() {
        // 清空
        this._stMainView.remove();
        this._stMainView.clear();
        
        this._sCurrentPath = 'M0,0T0,0';
        this._stMainView.push(
            this._paper
                .path(this._sCurrentPath)
                .attr(CONNECTOR_STYLE).hide()
        );
    };

    /*
     * @protected
     * @override
     */
    IST_BRAIN_CHART_CONNECTER_PROTO.$show = function(byNodeToVisible) {
        this._bVisible ? this._stMain.show() : this._stMain.hide();
    };

    /*
     * @protected
     * @override
     */
    IST_BRAIN_CHART_CONNECTER_PROTO.$calculateShape = function(
        basePoint, 
        subPoints
    ) {
        var pathArr = [];

        if (subPoints.length == 1) {
            pathArr.push(R.format(
                'M{0},{1}T{2},{3}', 
                basePoint.x, basePoint.y, subPoints[0].x, subPoints[0].y
            ));
        } 
        else if (subPoints.length > 1) {
            var vArr = [];
            var gap;
            var dy;
            var xi;
            // 接触点计算
            for (var i = 0, o; o = subPoints[i]; i ++) {
                gap = o.x - basePoint.x - CONNECTER_BASE_INERVAL;
                if (gap < 1) {
                    gap = 1;
                    xi = 1;
                } else {
                    xi = CONNECTER_BASE_INERVAL;
                }
                dy = o.y - basePoint.y;
                gap = Math.abs(dy) > gap ? gap : 0;
                vArr.push({
                    x: basePoint.x + xi,
                    y: dy < 0 ? (o.y + gap) : (o.y - gap)
                });
            }

            pathArr.push(R.format(
                'M{0},{1}T{2},{3}', 
                basePoint.x, 
                basePoint.y, 
                basePoint.x + xi, 
                basePoint.y
            ));
            pathArr.push(R.format(
                'M{0},{1}T{2},{3}', 
                vArr[0].x, 
                vArr[0].y, 
                vArr[vArr.length - 1].x, 
                vArr[vArr.length - 1].y
            ));

            for (var i = 0, o; o = subPoints[i]; i ++) {
                pathArr.push(R.format(
                    'M{0},{1}T{2},{3}', 
                    o.x, o.y, vArr[i].x, vArr[i].y
                ));
            }
        }

        return pathArr.join('');
    };

    //---------------------------------------------------
    // Layout Strategy 方法
    //---------------------------------------------------
    
    /**
     * @protected
     * @override
     */
    IST_BRAIN_CHART_LAYOUT_STRATEGY_PROTO.$calculateFatherPositionBySub = function(
        thisNode, 
        thisRecord
    ) {
        var middle, length, thisY;
        if ((length = thisRecord.levels.length) > 0) {
            middle = 
                Math.round(
                    (thisRecord.levels[0].yMax + thisRecord.levels[0].yMin) / 2
                )
                - Math.round(thisNode._nHeight / 2);
        }
        thisY = length > 1 ? (middle - 20) : (length == 1 ? middle : 0);
        return {y: thisY};
    };
    
})();
