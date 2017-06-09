/**
 * xjschart.BrainChart
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    脑图
 *           若想改变节点内容或样式，可继承重写BRAIN_CHART_PROTO.Node
 *           若想改变连接线样式，可继承重写BRAIN_CHART_PROTO.Connecter
 * @author:  sushuang(sushuang)
 * @depend:  raphael
 */

(function() {
    
    //----------------------------------------
    // 引用 
    //----------------------------------------
    
    var X = xjschart;
    var R = Raphael;
    var UI_ELEMENT = X.UIElement;
    var FIXED_THICKNESS_TREE_LAYOUT_STRATEGY = 
            X.FixedThicknessTreeLayoutStrategy;
    var util = X.util;
    var inheritsObject = util.inheritsObject;
    var assign = util.assign;
    var extend = util.extend;
    var clone = util.clone;
    var isString = util.isString;
    var g = util.g;
    var max = Math.max;
    var min = Math.min;
    var bind = util.bind;
    var isObject = util.isObject;
    var travelTree = util.travelTree;
    var getValue = util.getValue;
    var wrapEvent = util.wrapEvent;
    var attachEvent = util.attachEvent;
    var detachEvent = util.detachEvent;
    var STOP_SUB_TREE_TRAVEL = util.STOP_SUB_TREE_TRAVEL;
        
    //----------------------------------------
    // 类型定义
    //----------------------------------------
    
    /**
     * 脑图
     *
     * @class
     * @extends xjschart.UIElement
     */
    var BRAIN_CHART = X.BrainChart = inheritsObject(
            UI_ELEMENT,
            brainChartConstructor
        );
    var BRAIN_CHART_PROTO = BRAIN_CHART.prototype;
        
    /**
     * 脑图节点
     *
     * @class
     * @extends xjschart.UIElement
     */
    var BRAIN_CHART_NODE = BRAIN_CHART_PROTO.Node = inheritsObject(
            UI_ELEMENT,
            brainChartNodeConstructor
        );
    var BRAIN_CHART_NODE_PROTO = BRAIN_CHART_NODE.prototype;
            
    /**
     * 脑图链接器
     *
     * @class
     * @extends xjschart.UIElement
     */
    var BRAIN_CHART_CONNECTER = BRAIN_CHART_PROTO.Connecter = 
            inheritsObject(
                UI_ELEMENT,
                brainChartConnecterConstructor
            );
    var BRAIN_CHART_CONNECTER_PROTO = BRAIN_CHART_CONNECTER.prototype;
            
    /**
     * 默认为定厚度树布局策略
     */
    BRAIN_CHART_PROTO.LayoutStrategy = FIXED_THICKNESS_TREE_LAYOUT_STRATEGY;
    
    //----------------------------------------
    // 常量
    //----------------------------------------

    var DEFAULT_NODE_INTERVAL_X = 50;
    var DEFAULT_NODE_INTERVAL_Y = 20;
    var DEFAULT_NODE_WIDTH = 160;
    var ANIMATION_TRANSFORM_DURATION = 350;
    var ACTION_LOADED = 'action_loaded';
    var ACTION_EXPEND = 'action_expend';
    var ACTION_COLLAPSE = 'action_collapse';
    var ACTION_MOVE = 'action_move';
    var ACTION_NONE = 'action_none';
    
    //----------------------------------------
    // BrainChart 方法
    //----------------------------------------
    
    /**
     * 脑图构造器
     *
     * @public
     * @constructor
     * @param {Object} options 初始化参数
     *          {boolean} animation 是否动画（默认true）
     *          {boolean} fixWidth true:过大出滚动条（默认）; false:自动调整外框大小
     *          {boolean} fixHeight true:过大出滚动条; false:自动调整外框大小（默认）
     *          {Object} layoutOptions 布局选项
     *              {string} mode 'compact':紧凑型;'neat':舒展型（默认）
     *          {number} nodeIntervalX 可缺省
     *          {number} nodeIntervalY 可缺省
     *          {number} nodeWidth 可缺省
     */
    function brainChartConstructor(options) {
        this._eContainer = options.container;
        this._oNodeMap = {};

        this._oConfig = {
            nodeIntervalX: getValue(
                options.nodeIntervalX, 
                DEFAULT_NODE_INTERVAL_X
            ),
            nodeIntervalY: getValue(
                options.nodeIntervalY, 
                DEFAULT_NODE_INTERVAL_Y
            ),
            animationTransformDuration: getValue(
                options.animationTransformDuration, 
                ANIMATION_TRANSFORM_DURATION
            ),
            nodeWidth: getValue(
                options.nodeWidth, 
                DEFAULT_NODE_WIDTH
            ),
            initWidth: options.width,
            initHeight: options.height,
            fixWidth: getValue(options.fixWidth, true),
            fixHeight: getValue(options.fixHeight, false),
            animation: getValue(options.animation, true)
        };

        options.layoutOptions = options.layoutOptions || {};
        options.layoutOptions.chart = this;
        this._oLayoutStrategy = new this.LayoutStrategy(
            extend(
                {},
                options.layoutOptions,
                {

                    marginTop: options.marginTop,
                    marginRight: options.marginRight,
                    marginBottom: options.marginBottom,
                    marginLeft: options.marginLeft
                }
            )
        );

        this._nContentWidth = this._oConfig.initWidth;
        this._nContentHeight = this._oConfig.initHeight;
        this._nWidth = this._oConfig.initWidth;
        this._nHeight = this._oConfig.initHeight;
        this._nContentX = 0;
        this._nContentY = 0;

        // client样式设置
        var clientPosition = this._eClient.style.position;
        if (clientPosition != 'relative' 
            && clientPosition != 'absolute'
            && clientPosition != 'fixed'
        ) {
            this._eClient.style.position = 'relative';
        }
        this._eClient.style.overflow = 'hidden';
        if (this._oConfig.fixWidth) {
            this._eClient.style.width = this._nWidth + 'px';
        }
        if (this._oConfig.fixHeight) {
            this._eClient.style.height = this._nHeight + 'px';
        }
        this._eClient.style.border = options.borderStyle;

        // container样式设置
        this._eContainer.style.position = 'absolute';
        this._eContainer.style.top = '0px';
        this._eContainer.style.left = '0px';
        this._eContainer.style.width = this._nContentWidth + 'px';
        this._eContainer.style.height = this._nContentHeight + 'px';
        this._eContainer.style.cursor = 'move';

        // 拖动事件
        attachEvent(
            this._eContainer, 
            'mousedown', 
            bind(this.$dragMouseDownHandler, this)
        );
        attachEvent(
            document, 
            'mouseup', 
            this._fdragMouseUpHandler = 
                bind(this.$dragMouseUpHandler, this)
        );
        attachEvent(
            this._eContainer, 
            'mousemove', 
            bind(this.$dragMouseMoveHandler, this)
        );

        this._bDragging = false;
        this._uPendingTriggerNode;
        this._aPendingNode = [];
        this._aPendingConnecter = [];
        this._aAniArr = [];
    }    

    /**
     * 预处理输入参数
     *
     * @public
     * @override
     * @param {Object} options 输入参数
     */
    BRAIN_CHART_PROTO.$processOptions = function(options) {
        BRAIN_CHART.superClass.$processOptions.call(this);
        var container = options.container;
        if (isString(container)) {
            container = g(container);
        }
        this._eClient = container;

        // 创建content container
        var o = document.createElement('div');
        o.innerHTML = '<div unselectable="on" style="-moz-user-select:none;-webkit-user-select:none;" onselectstart="return false;">';
        this._eClient.appendChild(
            options.container = o.firstChild
        );

    };  
    
    /**
     * 初始化
     *
     * @public
     * @override
     */
    BRAIN_CHART_PROTO.init = function() {
        BRAIN_CHART.superClass.init.call(this);
        if (this._oData) {
            this.setData(this._oData);
        }
    };
    
    /**
     * 析构
     *
     * @public
     * @override
     */
    BRAIN_CHART_PROTO.dispose = function() {
        attachEvent(
            document, 
            'mouseup', 
            this._fdragMouseUpHandler
        );
        if (this._uRoot) {
            this._uRoot.dispose();
            this._uRoot = null;
        }
        this._eContainer = null;
        this._eClient = null;
        BRAIN_CHART.superClass.dispose.call(this);
    };
    
    /**
     * 获得当前所有数据的拷贝，
     * 所获得的数据格式满足：可以用setData函数设置回来
     *
     * @public
     * @return {Object} 当前所有数据的拷贝
     *              {Array{Object}} children 子节点列表
     *              ... 其他属性参见具体实现类的扩展
     */
    BRAIN_CHART_PROTO.getData = function() {
        var data;
        travelTree(
            this._uRoot,
            function(node, options) {
                var o = assign({}, node._oData, null, ['children']);
                if (node._aChildren) {
                    o.children = [];
                }
                if (options.parentParam.father) {
                    options.parentParam.father.children.push(o);
                }
                options.childrenParam.father = o;
                !data && (data = o);
            }, 
            '_aChildren',
            false
        );
        return data;
    };    
    
    /**
     * 设置数据
     *
     * @public
     * @param {Object} data 数据，如果为null则清空
     *              {number} id 可缺省
     *              {number} canExpend 子树是否可以展开
     *              {boolean} expended 子树默认的展开状态
     *              {Array{Object}} children 子节点列表
     *              ... 其他属性参见具体实现类的扩展
     * @param {boolean} isSilent 是否静默，
     *              静默则不执行视图改变，可直接调用render改变视图
     */
    BRAIN_CHART_PROTO.setData = function(data, isSilent) {
        if (this._uRoot) {
            this._uRoot.dispose();
            this._uRoot = null;
        }
        if (data == null) { return; }

        this._uRoot = this.$createNodeTree(data, null, true, isSilent);

        this._uPendingTriggerNode = this._uRoot;
        !isSilent && this.render();
    };

    /**
     * 追加数据（用于展开子层级异步加载数据）
     *
     * @public
     * @param {BrainNode|string} node 节点ID或者节点本身，追加到此节点之下
     * @param {Array{Object}} dataList 数据
     *          {number} id 可缺省
     *          {number} canExpend 子树是否可以展开
     *          {boolean} expended 子树默认的展开状态
     *          {Array{Object}} children 子节点列表
     * @param {boolean} isSilent 是否静默，
     *              静默则不执行视图改变，可直接调用render改变视图     
     */
    BRAIN_CHART_PROTO.appendData = function(node, dataList, isSilent) {
        var i, o, children;
        if (!(node = this.getNode(node))) { return; }
        
        for (i = 0, children = node._aChildren || []; o = children[i]; i ++) {
            o.dispose();
        }
        node._aChildren = [];
        
        if (dataList && dataList.length) {
            for (i = 0; o = dataList[i]; i ++) {
                node._aChildren.push(this.$createNodeTree(o, node, true, isSilent));
            }
            node._uConnecter._bFirstLoad = true;
    
            // 默认展开
            this.expend(node, isSilent);
        }
    };
    
    /**
     * 刷新节点内容
     *
     * @public
     * @param {Array{Object}} dataList 数据
     *          {string} id 节点id
     *          {number} canExpend 子树是否可以展开
     *          {boolean} expended 子树默认的展开状态
     *          {Array{Object}} children 子节点列表
     * @param {boolean} isSilent 是否静默，
     *              静默则不执行视图改变，可直接调用render改变视图
     */
    BRAIN_CHART_PROTO.refreshData = function(dataList, isSilent) {
        var i, o, node;
        for (i = 0, dataList = dataList || []; o = dataList[i]; i ++) {
            (node = this._oNodeMap[o.id]) && node.setData(o, isSilent);   
            isSilent && this._aPendingNode.push(node);
        }

        this._uPendingTriggerNode = null;
        !isSilent && this.render();
    };

    /**
     * 增加节点
     *
     * @protected
     * @param {Object} data 数据，树结构，子节点列表的属性名为'children'
     * @param {string} parent 节点ID，追加到此节点之下
     * @param {boolean} visibale 是否可见
     * @param {boolean} isSilent 是否静默，
     *              静默则不执行视图改变，可直接调用render改变视图
     * @param {UIElement} 创建的根节点
     */
    BRAIN_CHART_PROTO.$createNodeTree = function(data, parent, visible, isSilent) {
        var i, o, node, children;
        if (!data) { return; }
        
        node = new this.Node({data: data, parent: parent, container: this, 
            paper: this._paper, config: this._oConfig, visible: !!visible});
        node._oData.id != null && (this._oNodeMap[node._oData.id] = node);
        node.init(isSilent);
        isSilent && this._aPendingNode.push(node);
        
        visible = !!(visible && data.canExpend && data.expended);
        for (i = 0, children = data.children || []; o = children[i]; i ++) {
            node._aChildren.push(this.$createNodeTree(o, node, visible, isSilent));
        }

        node._uConnecter = new this.Connecter({baseNode: node, paper: this._paper, 
            config: this._oConfig});
        node._uConnecter.init(isSilent);
        isSilent && this._aPendingConnecter.push(node._uConnecter);
        
        return node;
    }
    
    /**
     * 获得节点对象
     *
     * @public
     * @param {string} nodeId
     * @return {BRAIN_CHART_PROTO.Node} 节点对象  
     */
    BRAIN_CHART_PROTO.getNode = function(nodeId) {
        return this._oNodeMap[nodeId];
    };

    /**
     * 展开子树
     *
     * @public
     * @param {string|BrainChartNode} node 节点或者节点id
     * @param {boolean} isSilent 是否静默，
     *              静默则不执行视图改变，可直接调用render改变视图
     */
    BRAIN_CHART_PROTO.expend = function(node, isSilent) {
        if (!node._oData.canExpend) {
            return;
        }

        node._oData.expended = true;
        node.$showCollapseBtn();        

        if (node = this.getNode(node)) {
            travelTree(
                node, 
                function(n, options) {
                    var parentData;
                    if (options.parent) {
                        parentData = options.parent._oData; 
                        n._bToVisible = 
                            !!(parentData.canExpend && parentData.expended);
                    }
                    if (!n._oData.canExpend || !n._oData.expended) {
                        // 停止遍历子树
                        return STOP_SUB_TREE_TRAVEL;
                    }
                }, 
                '_aChildren', 
                false
            );
        }

        this._uPendingTriggerNode = node;
        !isSilent && this.render();
    }
    
    /**
     * 折叠子树
     *
     * @public
     * @param {string|BrainChartNode} node 节点或者节点ID
     * @param {boolean} isSilent 是否静默，
     *              静默则不执行视图改变，可直接调用render改变视图
     */
    BRAIN_CHART_PROTO.collapse = function(node, isSilent) {
        if (!node._oData.canExpend) {
            return;   
        }

        node._oData.expended = false;
        node.$showCollapseBtn();        

        if (node = this.getNode(node)) {
            travelTree(
                node, 
                function(n, options) {
                    if (options.parent) {
                        n._bToVisible = false;
                    }
                }, 
                '_aChildren', 
                false
            );            
        }

        this._uPendingTriggerNode = node;
        !isSilent && this.render();
    }
    
    /**
     * 设置禁用
     *
     * @public
     * @override
     */
    BRAIN_CHART_PROTO.disable = function() {
        if (!this._bDisabled) {
            travelTree(
                this._uRoot, 
                function(n, options) {
                    n.disable();
                }, 
                '_aChildren', 
                false
            );            
        }
        BRAIN_CHART.superClass.disable.apply(this, arguments);
    };

    /**
     * 设置启用
     *
     * @public
     * @override
     */
    BRAIN_CHART_PROTO.enable = function() {
        if (this._bDisabled) {
            travelTree(
                this._uRoot, 
                function(n, options) {
                    n.enable();
                }, 
                '_aChildren', 
                false
            );            
        }
        BRAIN_CHART.superClass.enable.apply(this, arguments);
    };

    /**
     * 根据节点ID得到节点/子树根
     *
     * @public
     * @param {string|BrainChartNode} nodeId 节点或者节点ID
     * @return {BrainChartNode} 节点
     */
    BRAIN_CHART_PROTO.getNode = function(nodeId) {
        return isObject(nodeId) ? nodeId : this._oNodeMap[nodeId]; 
    }

    /**
     * 获得所有节点ID
     * @public
     * 
     * @return {Array} 所有的ID
     */
    BRAIN_CHART_PROTO.getAllIds = function() {
        var ret = [], id;
        for (id in this._oNodeMap) {
            ret.push(id);
        }
        return ret;
    };
        
    /**
     * 节点是否有数据
     *
     * @public
     * @param {string|BrainChartNode} node 节点或者节点ID
     * @return {boolean} 节点是否有数据
     */
    BRAIN_CHART_PROTO.needData = function(node) {
        node = this.getNode(node);
        return !!(
            node 
            && node._oData.canExpend 
            && (!node._aChildren || node._aChildren.length == 0)
        );
    };

    /**
     * 手动渲染
     *
     * @public
     */
    BRAIN_CHART_PROTO.render = function() {
        this._uTooltip.hide();
        
        var node;
        while (node = this._aPendingNode.shift()) {
            node.render();
        }

        var connecter;
        while (connecter = this._aPendingConnecter.shift()) {
            connecter.render();
        }

        this.$layout({ triggerNode: this._uPendingTriggerNode });
        this._uPendingTriggerNode = null;
    };

    /**
     * 布点
     *
     * @protected
     * @param {Object} options
     *          {BrainChartNode} triggerNode 触发节点
     */
    BRAIN_CHART_PROTO.$layout = function(options) {
        // 计算布点位置
        var size = this._oLayoutStrategy.layout();
        // 重置外壳大小
        this.$resizeBox(size);
        // 计算移动参数
        this.$calculateTransfrom(options);
        // 执行移动
        this.$doTransform(options);
    };
    
    /**
     * 执行移动
     *
     * @protected
     */
    BRAIN_CHART_PROTO.$doTransform = function() {
        var me = this;
        var aniArr = [];
            
        // 执行瞬间移动或者构造移动动画
        travelTree(
            this._uRoot, 
            function(node, options) {
                me.$doTransformNode(node, aniArr);
                me.$doTransformConnecter(node, aniArr);
            }, 
            '_aChildren', 
            false
        );

        // 停止当前动画
        for (var i = 0, ani; ani = this._aAniArr[i]; i ++) {
            ani.el.stop(ani.animation);
        }
        this._aAniArr = [];

        // 动画情况下执行移动
        if (this._oConfig.animation && aniArr.length > 0) {
            var ani = aniArr[0];
            ani.el.animate(ani.animation);
            for (var i = 1, o; o = aniArr[i]; i++) {
                o.el.animateWith(ani.el, ani.animation, o.animation);
            }
            this._aAniArr = aniArr;
        }

    };

    /**
     * 执行移动(Node)
     *
     * @protected
     * @param {Node} node 节点
     * @param {Array} aniArr 存返回结果：要执行的动画
     */
    BRAIN_CHART_PROTO.$doTransformNode = function(node, aniArr) {
        var beginOpacity, endOpacity;

        if (node._sAction == ACTION_NONE) { return; }

        // 使用动画的情况
        if (this._oConfig.animation) { 
            beginOpacity = node._sAction == ACTION_COLLAPSE ? 1 : 0;
            endOpacity = beginOpacity == 1 ? 0 : 1;

            if (node._sAction == ACTION_LOADED 
                || node._sAction == ACTION_EXPEND
            ) {
                node._stMainView
                    .transform(R.format(
                        'T{0},{1}', node._nFromX, node._nFromY
                    ))
                    .attr({opacity: beginOpacity});
            }
            node._sTransform = R.format(
                'T{0},{1}', node._nToX, node._nToY
            );

            if (node._sAction == ACTION_EXPEND 
                || node._sAction == ACTION_LOADED
            ) {
                node._bVisible = node._bToVisible;
                node.$show();
            }

            aniArr.push({
                el: node._stMainView,
                animation: R.animation(
                    { transform: node._sTransform, opacity: endOpacity }, 
                    this._oConfig.animationTransformDuration, 
                    '<>',
                    (function(n, lastShow) { 
                        return function() {
                            n._nX = n._nToX;
                            n._nY = n._nToY;
                            n._stMainAgent.transform(n._sTransform);
                            if (lastShow) {
                                n._bVisible = n._bToVisible;
                                n.$show();
                            }
                            n._stMainView.attr({opacity: 1});
                        };
                    })(node, node._sAction == ACTION_COLLAPSE)
                )
            });
        }

        // 不使用动画的情况
        else { 
            if (node._sAction == ACTION_LOADED 
                || node._sAction == ACTION_MOVE
                || node._sAction == ACTION_EXPEND
            ) {
                node._stMain.transform(
                    node._sTransform = R.format('T{0},{1}', node._nToX, node._nToY)
                );
                node._nX = node._nToX; 
                node._nY = node._nToY;
            }
            node._bVisible = node._bToVisible;
            node.$show();                    
        }
    };

    /**
     * 执行移动(Connecter)
     *
     * @protected
     * @param {Node} node 节点
     * @param {Array} aniArr 存返回结果：要执行的动画
     */
    BRAIN_CHART_PROTO.$doTransformConnecter = function(node, aniArr) {
        var beginOpacity, endOpacity;
        var connecter = node._uConnecter;

        if (connecter._sAction == ACTION_NONE) { return; }

        if (this._oConfig.animation) { // 使用动画的情况
            beginOpacity = connecter._sAction == ACTION_COLLAPSE ? 1 : 0;
            endOpacity = beginOpacity == 1 ? 0 : 1;                    

            if (connecter._sAction == ACTION_LOADED 
                || connecter._sAction == ACTION_EXPEND
            ) {
                connecter._stMainView.attr({
                    path: connecter._sFromPath, 
                    opacity: beginOpacity
                });
            }

            if (connecter._sAction == ACTION_EXPEND 
                || connecter._sAction == ACTION_LOADED
            ) {
                connecter._bVisible = connecter._bToVisible;
                connecter.$show();
            }                    

            aniArr.push({
                el: connecter._stMainView,
                animation: R.animation(
                    { path: connecter._sToPath, opacity: endOpacity }, 
                    this._oConfig.animationTransformDuration, 
                    '<>',
                    (function(c, lastShow) {
                        return function() {
                            if (lastShow) {
                                c._bVisible = c._bToVisible;
                                c.$show();
                            }
                        };
                    })(connecter, connecter._sAction == ACTION_COLLAPSE)
                )
            });
        } 

        else { // 不使用动画的情况
            if (connecter._sAction == ACTION_LOADED 
                || connecter._sAction == ACTION_MOVE
                || connecter._sAction == ACTION_EXPEND
            ) {
                connecter._stMainView.attr({path: connecter._sToPath});
            }
            connecter._bVisible = connecter._bToVisible;
            connecter.$show();                    
        }
    };

    /**
     * 计算移动参数
     *
     * @protected
     */
    BRAIN_CHART_PROTO.$calculateTransfrom = function(options) {
        var me = this;

        travelTree(
            this._uRoot, 
            function(node, opt) {        
                me.$calculateAction(node);
                node.$calculateTransform(options);
            }, 
            '_aChildren', 
            false
        );

        travelTree(
            this._uRoot, 
            function(node, opt) {        
                var connecter = node._uConnecter;
                connecter.$calculateVisible(options);
                me.$calculateAction(connecter);
                connecter.$calculateTransform(options);
            }, 
            '_aChildren', 
            false
        );
    };

    /**
     * 重置外壳大小
     *
     * @protected
     * @param {Object} size尺寸
     *          {number} width 宽
     *          {number} height 高
     */
    BRAIN_CHART_PROTO.$resizeBox = function(size) {
        var cfg = this._oConfig;
        //var i, o, boundWidth, boundHeight, cfg = this._oConfig,
        //    scrollThick = 40; // 滚动条厚度，取估计值
        
        /*
        if (!cfg.fixWidth) {
            //是否受纵向滚动条影响
            boundWidth = size.width 
                + (size.height > cfg.initHeight ? scrollThick : 0); 
            this._eContainer.style.width = 
                (
                    this._nWidth = 
                        (boundWidth > cfg.initWidth ? boundWidth : cfg.initWidth)
                ) + 'px';
        }
        if (!cfg.fixHeight) {
            //是否受横向滚动条影响
            boundHeight = size.height 
                + (size.width > cfg.initWidth ? scrollThick : 0); 
            this._eContainer.style.height = 
                (
                    this._nHeight = 
                        (boundHeight > cfg.initHeight ? boundHeight : cfg.initHeight)
                ) + 'px';
        }
        */

        if (!cfg.fixWidth) {
            this._nWidth = size.width;
            this._eClient.style.width = this._nWidth + 'px';
        }
        if (!cfg.fixHeight) {
            this._nHeight = size.height;
            this._eClient.style.height = this._nHeight + 'px';
        }
        this._nContentWidth = max(cfg.initWidth, size.width);
        this._nContentHeight = max(cfg.initHeight, size.height);
        this._paper.setSize(this._nContentWidth, this._nContentHeight);
        this._eContainer.style.width = this._nContentWidth + 'px';
        this._eContainer.style.width = this._nContentHeight + 'px';
    };

    /**
     * 内容mousedown处理
     *
     * @protected
     */
    BRAIN_CHART_PROTO.$dragMouseDownHandler = function(e) {
        e = wrapEvent(e);
        this._bDragging = true;
        this._oDragStartPoint = { 
            x: e.pageX, 
            y: e.pageY,
            contentX: this._eContainer.offsetLeft,
            contentY: this._eContainer.offsetTop 
        };
    };

    /**
     * 内容mouseup处理
     *
     * @protected
     */
    BRAIN_CHART_PROTO.$dragMouseUpHandler = function() {
        this._bDragging = false;
        this._oDragStartPoint = null;
    };

    /**
     * 内容mousemove处理
     *
     * @protected
     */
    BRAIN_CHART_PROTO.$dragMouseMoveHandler = function(e) {
        e = wrapEvent(e);
        var config = this._oConfig;

        if (this._bDragging) {
            var startPoint = this._oDragStartPoint;
            var to;
            var diff;
            if (config.fixWidth) {
                to = startPoint.contentX + e.pageX - startPoint.x;
                diff = to - this._nContentX;
                if ((diff > 0 && to < 0)
                    || (diff < 0 && to + this._nContentWidth > this._nWidth)
                ) {
                    this._nContentX = to;
                    this._eContainer.style.left = to + 'px';
                }
            }
            if (config.fixHeight) {
                to = startPoint.contentY + e.pageY - startPoint.y;
                diff = to - this._nContentY;
                if ((diff > 0 && to < 0)
                    || (diff < 0 && to + this._nContentHeight > this._nHeight)
                ) {
                    this._nContentY = to;
                    this._eContainer.style.top = to + 'px';
                }
            }
        }
    };

    /**
     * 计算移动action
     *
     * @public
     * @param {BrainNode|BrainConnecter} 节点或者连接线
     */    
    BRAIN_CHART_PROTO.$calculateAction = function (o) {
        if (o._bFirstLoad && (o._bVisible || o._bToVisible)) {
            o._sAction = ACTION_LOADED;
        } else if (o._bVisible && o._bToVisible) {
            o._sAction = ACTION_MOVE;
        } else if (!o._bVisible && o._bToVisible) {
            o._sAction = ACTION_EXPEND;
        } else if (o._bVisible && !o._bToVisible) {
            o._sAction = ACTION_COLLAPSE;
        } else {
            o._sAction = ACTION_NONE;
        }
        o._bFirstLoad = false;
    }
    
    //----------------------------------------------
    // BrainChart Node 方法
    //----------------------------------------------

    /**
     * 脑图节点构造器
     *
     * @public
     * @constructor
     * @param {Object} options 参数
     */
    function brainChartNodeConstructor(options) {
        assign(
            this, 
            options, 
            {
                '_uParent': 'parent', 
                '_uContainer': 'container', 
                '_oConfig': 'config', 
                '_bVisible': 'visible'
            }
        );
        this._oData = extend({}, options.data);
        if (!this._oData.type) {
            this._oData.type = 'DATA';
        }
        delete this._oData.children;
        this._oData.canExpend = !!this._oData.canExpend;
        this._oData.expended = !!this._oData.expended;
        this._bVisible = false;
        this._bToVisible = options.visible;
        this.$setModel(options.data);
        this._nX = this._nY = this._nToX = this._nToY 
            = this._nLayoutX = this._nLayoutY = 0;
        this._aChildren = [];
        this._sAction = ACTION_NONE;
        this._bFirstLoad = true;
    }

    /**
     * 初始化
     *
     * @public
     * @override
     * @param {boolean} isSilent 是否静默，
     *              静默则不执行视图改变，可直接调用render改变视图
     */
    BRAIN_CHART_NODE_PROTO.init = function(isSilent) {
        BRAIN_CHART_PROTO.Node.superClass.init.call(this);
        this.$initContent();
        this.$initCollapseBtn();
        this.$initBody();
        this.$orderAgent();

        !isSilent && this.render();
    };
    
    /**
     * 析构
     *
     * @public
     * @override
     */
    BRAIN_CHART_NODE_PROTO.dispose = function() {
        var i, subNode, children;

        children = this._aChildren || [];
        for (i = 0; subNode = children[i]; i++) {
            subNode.dispose();
        }

        this._aChildren = null;
        this.$dispose();
        BRAIN_CHART_NODE.superClass.dispose.call(this);
    };
    
    /**
     * 设置数据
     *
     * @public
     * @param {Object} data 数据
     * @param {boolean} isSilent 是否静默，
     *              静默则不执行视图改变，可直接调用render改变视图
     */
    BRAIN_CHART_NODE_PROTO.setData = function(data, isSilent) {
        var o = extend({}, data);
        delete o.expended;
        delete o.canExpend;
        this.$setModel(o);

        !isSilent && this.render();
    };
    
    /**
     * 获得ID
     *
     * @public
     * @param {Object} 数据
     */
    BRAIN_CHART_NODE_PROTO.getData = function() {
        return clone(this._oData);
    };
 
    /**
     * 手动渲染
     *
     * @public
     */
    BRAIN_CHART_NODE_PROTO.render = function() {
        this.$render();
    };
 
     /*
     * 计算移动参数
     * 计算_nFromX和_nFromY和_nToX和_nToY，如果为空则表示无需计算使用当前位置
     *
     * @protected
     */
    BRAIN_CHART_NODE_PROTO.$calculateTransform = function(options) {
        options = options || {}
        var triggerNode = options.triggerNode,
            fromX = triggerNode ? triggerNode._nX + triggerNode._nWidth : 0,
            fromY = triggerNode ? triggerNode._nY : 0,
            toX = triggerNode ? triggerNode._nToX + triggerNode._nWidth : 0,
            toY = triggerNode ? triggerNode._nToY : 0;

        // 计算from
        if (this._sAction == ACTION_LOADED) {
            this._nFromX = this._nFromY = 0;
            this._nToX = this._nLayoutX;
            this._nToY = this._nLayoutY;

        } else if (this._sAction == ACTION_EXPEND) {
            this._nFromX = fromX;
            this._nFromY = fromY;
            this._nToX = this._nLayoutX;
            this._nToY = this._nLayoutY;

        } else if (this._sAction == ACTION_MOVE) {
            this._nFromX = this._nFromY = null;
            this._nToX = this._nLayoutX;
            this._nToY = this._nLayoutY;
            
        } else if (this._sAction == ACTION_COLLAPSE) {
            this._nFromX = this._nFromY = null;
            this._nToX = toX;
            this._nToY = toY;

        } else {
            this._nFromX = this._nFromY = null;
            this._nToX = this._nToY = null;
        }
    };

    /**
     * 本节点的析构
     * 
     * @protected
     * @abstract
     */
    BRAIN_CHART_NODE_PROTO.$dispose = function(data) {};
    
    /**
     * 构造控件的数据模型
     *
     * @protected
     * @abstract
     */
    BRAIN_CHART_NODE_PROTO.$setModel = function(data) {};
    
    /**
     * 初始化放缩按钮
     *
     * @protected
     * @abstract
     */
    BRAIN_CHART_NODE_PROTO.$initCollapseBtn = function() {};
    
    /**
     * 初始化内容
     * @abstract
     */
    BRAIN_CHART_NODE_PROTO.$initContent = function() {};
    
    /**
     * 初始化整体
     *
     * @protected
     * @abstract
     */
    BRAIN_CHART_NODE_PROTO.$initBody = function() {};

    /**
     * 事件代理z-index
     *
     * @protected
     * @abstract
     */
    BRAIN_CHART_NODE_PROTO.$orderAgent = function() {};

    /**
     * 渲染
     *
     * @protected
     * @abstract
     */
    BRAIN_CHART_NODE_PROTO.$render = function() {};
    
    /**
     * 显示/隐藏
     *
     * @protected
     * @abstract
     */
    BRAIN_CHART_NODE_PROTO.$show = function() {};
    
    /**
     * 显示扩展/收起按钮
     *
     * @protected
     * @abstract
     */
    BRAIN_CHART_NODE_PROTO.$showCollapseBtn = function() {};

    /**
     * 显示扩展/收起按钮
     *
     * @protected
     * @abstract
     */
    BRAIN_CHART_NODE_PROTO.$showCollapseBtn = function() {};

    /**
     * 连接线的起始
     *
     * @protected
     * @abstract
     * @return {Object} 起始点
     *            {number} x
     *            {number} y
     */
    BRAIN_CHART_NODE_PROTO.$getConnecterBeginPoint = function() {};

    /**
     * 连接线的终止
     *
     * @protected
     * @abstract
     * @return {Object} 终止点
     *            {number} x
     *            {number} y
     */
    BRAIN_CHART_NODE_PROTO.$getConnecterEndPoint = function() {};

    //----------------------------------------------
    // BrainChart Connecter 方法
    //----------------------------------------------

    /**
     * 脑图连接器构造器
     * 
     * @public
     * @contructor
     * @param {Object} options 参数
     */
    function brainChartConnecterConstructor(options) {
        var i, o, children;
        assign(
            this, 
            options, 
            { '_uBaseNode': 'baseNode', '_oConfig': 'config' }
        );
        this._bVisible = this._bToVisible = false;
        this._sAction = ACTION_NONE;
    }

    /**
     * @public
     * @override
     * @param {boolean} isSilent 是否静默，
     *              静默则不执行视图改变，可直接调用render改变视图
     */
    BRAIN_CHART_CONNECTER_PROTO.init = function(isSilent) {
        BRAIN_CHART_CONNECTER.superClass.init.call(this);
        !isSilent && this.render();
    };

    /**
     * 手动渲染
     *
     * @public
     */
    BRAIN_CHART_CONNECTER_PROTO.render = function() {
        this.$render();
    };

    /*
     * 计算移动参数
     * 计算_aFromPath和_sToPath，如果为空则表示无需计算使用当前path
     *
     * @protected
     */
    BRAIN_CHART_CONNECTER_PROTO.$calculateTransform = function(options) {
        var i, o, basePoint, subPoint, subPoints,
            config = this._oConfig,
            baseNode = this._uBaseNode,
            triggerNode = options && options.triggerNode || baseNode,
            children = baseNode._aChildren || [];

        // 计算_aFromPath
        if (this._sAction == ACTION_LOADED || this._sAction == ACTION_EXPEND) {
            basePoint = baseNode.$getConnecterEndPoint();
            basePoint.x += (this._sAction == ACTION_LOADED ? 0 : triggerNode._nX);
            basePoint.y += (this._sAction == ACTION_LOADED ? 0 : triggerNode._nY);
            for (i = 0, subPoints = []; o = children[i]; i++) {
                subPoints.push({x: basePoint.x + 3, y: basePoint.y});
            }
            this._sFromPath = this.$calculateShape(basePoint, subPoints);

        } else {
            // 其他情况，使用当前path
            this._sFromPath = null;
        }

        // 计算_sToPath
        if (this._sAction == ACTION_LOADED || this._sAction == ACTION_EXPEND 
            || this._sAction == ACTION_MOVE) {
            basePoint = baseNode.$getConnecterEndPoint();
            basePoint.x += baseNode._nToX;
            basePoint.y += baseNode._nToY;
            for (i = 0, subPoints = []; o = children[i]; i++) {
                subPoint = o.$getConnecterBeginPoint();
                subPoint.x += o._nToX;
                subPoint.y += o._nToY;
                subPoints.push(subPoint);
            }
            this._sToPath = this.$calculateShape(basePoint, subPoints);

        } else if (this._sAction == ACTION_COLLAPSE) {
            basePoint = baseNode.$getConnecterEndPoint();
            basePoint.x += triggerNode._nToX;
            basePoint.y += triggerNode._nToY;
            for (i = 0, subPoints = []; o = children[i]; i++) {
                subPoints.push({x: basePoint.x + 3, y: basePoint.y});
            }
            this._sToPath = this.$calculateShape(basePoint, subPoints);

        } else {
            // 完全无显示情况
            this._sToPath = null;
        }
    };

    /*
     * 是否显示
     *
     * @protected
     */
    BRAIN_CHART_CONNECTER_PROTO.$calculateVisible = function() {
        var i, o, show = false;
            baseNode = this._uBaseNode, 
            children = baseNode._aChildren || [];

        if (children.length > 0 && baseNode._bToVisible) {
            for (i = 0; o = children[i]; i ++) {
                o._bToVisible && (show = true);
            }
        }
        this._bToVisible = show;
    };

    /**
     * 渲染
     *
     * @protected
     * @abstract
     */
    BRAIN_CHART_CONNECTER_PROTO.$render = function() {};

    /**
     * 显示或隐藏
     *
     * @protected
     * @abstract
     */
    BRAIN_CHART_CONNECTER_PROTO.$show = function() {};

    /* 
     * 计算连接线图形
     *
     * @protected
     * @abstract
     * @param {Object} basePoint
     *          {number} x
     *          {number} y
     * @param {Array{Object}} subPoints
     *          {number} x
     *          {number} y
     * @return {String} raphael path 
     */
    BRAIN_CHART_CONNECTER_PROTO.$calculateShape = function(
        basePoint, 
        subPoints
    ) {};

})();
