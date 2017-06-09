/**
 * xjschart.FixedThicknessTreeLayoutStrategy
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    定厚度树布局策略
 * @author:  sushuang(sushuang)
 */

(function() {
    
    //----------------------------------------
    // 引用 
    //----------------------------------------
    
    var X = xjschart;
    var util = X.util;
    var inheritsObject = util.inheritsObject;
    var travelTree = util.travelTree;
        
    //----------------------------------------
    // 类型定义
    //----------------------------------------
    
    /**
     * 定厚度树布局策略
     *
     * @class
     */
    var FIXED_THICKNESS_TREE_LAYOUT_STRATEGY = 
        X.FixedThicknessTreeLayoutStrategy = 
            inheritsObject(
                null, 
                fixedThicknessLayoutStrategyConstructor
            );
    var FIXED_THICKNESS_TREE_LAYOUT_STRATEGY_PROTO = 
            FIXED_THICKNESS_TREE_LAYOUT_STRATEGY.prototype;
    
    //----------------------------------------
    // 方法
    //----------------------------------------

    /**
     * 定宽布局策略构造器
     * 
     * @public
     * @constructor
     * @param {Object} options
     *          {string} mode 可选值：
     *              'compact'紧凑的
     *              'neat'美观的（默认）
     */
    function fixedThicknessLayoutStrategyConstructor(options) { 
        this._uChart = options.chart; 
        this._oMode = options.mode || 'neat';
        this._oConfig = {};
        this._oConfig.margin = {
            top: options.marginTop || 0,
            right: options.marginRight || 0,
            bottom: options.marginBottom || 0,
            left: options.marginLeft || 0
        }
    }
    
    /**
     * 默认的布点算法
     * 这种方式假设每层的厚度都是相同的。
     * 算法是个简单的分治递归。
     *
     * @public
     * @return {Object} 尺寸
     *              {number} width
     *              {number} height
     */
    FIXED_THICKNESS_TREE_LAYOUT_STRATEGY_PROTO.layout = function() {
        var i, o, record, root = this._uChart._uRoot;
        
        if (!(record = $FIXED_THICKNESS_LAYOUT.call(this, root))) {
            return {width: 0, height: 0};
        }
        $FIXED_THICKNESS_CALCULATE_ABSOLUTE_POSITION.call(this, root, record);
        
        return $FIXED_THICKNESS_CALCULATE_SIZE.call(this, record);
    };
    
    /**
     * 根据子节点计算父节点的位置
     * 可以重载以进行视觉微调。
     * 默认使用取中点的方式。
     *
     * @protected
     * @param {Object} thisRecord 本子树的形状信息
     *          {Array{Object}} levels 本子树从儿子层开始每层的信息，如果为空表示无子节点
     *              {number} yMin 每层的y最大值
     *              {number} yMax 每层的y最小值
     *          {Array{Object}} subNodePositions 儿子层的位置
     *              {number} x（暂不用，待扩展）
     *              {number} y
     * @return {Object} 父节点的位置
     *          {number} x x坐标（暂不用，待扩展）
     *          {number} y y坐标
     */
    FIXED_THICKNESS_TREE_LAYOUT_STRATEGY_PROTO.$calculateFatherPositionBySub = function(
        thisNode,
        thisRecord
    ) {
        var thisY = thisRecord.levels.length > 0 
            ? Math.round((thisRecord.levels[0].yMax + thisRecord.levels[0].yMin) / 2) 
                - Math.round(thisNode._nHeight / 2)
            : 0;
        return {y: thisY};
    };
    
    function $FIXED_THICKNESS_CALCULATE_SIZE(record) {
        var config = this._uChart._oConfig;
        var margin = this._oConfig.margin;
        var yMax = Number.MIN_VALUE;
        for (i = 0; o = record.levels[i]; i ++ ) {
            (o.yMax > yMax) && (yMax = o.yMax);
        }
        var xMax = (config.nodeWidth + config.nodeIntervalX) * record.levels.length;        
        return {
            width: xMax + margin.left + margin.right, 
            height: yMax + margin.top + margin.bottom
        };
    }
    
    function $FIXED_THICKNESS_CALCULATE_ABSOLUTE_POSITION(root, record) {
        if (root == null) { return; }
        
        // 得到yMin作为基准
        root._nLayoutX = this._oConfig.margin.left;
        root._nLayoutY = this._oConfig.margin.top + record.levels[0].yMin;
        
        // 根据基准遍历计算绝对位置
        travelTree(
            root,
            function(node, options) {
                if (options.parent) {
                    node._nLayoutX = options.parent._nLayoutX + node._nLayoutXOffset;
                    node._nLayoutY = options.parent._nLayoutY + node._nLayoutYOffset;
                }
            }, 
            '_aChildren', 
            false
        );
    }
    
    function $FIXED_THICKNESS_LAYOUT(root) {
        var i, o, children, record, thisRecord, recordList = [], thisY;
        
        if (root == null) { return null; }
        
        children = root._aChildren || [];
        
        // 递归布局子树
        for (i = 0; o = children[i]; i++) {
            if (o._bToVisible) {
                record = $FIXED_THICKNESS_LAYOUT.call(this, o);
                record && recordList.push(record);
            }
        }
        
        // 本树布局，计算每个子树针对于本节点的偏移量，并构造thisRecord。
        // 每个子树，使用根节点的Y作为坐标基准。
        //（为避免计算过程中频繁更新位置，所以使用对于根的相对位置，最后统一更新）
        // 每个record，使用yMin作为坐标基准。
        //（因为是默认自上而下布局，所以使用yMin作为基准）
        thisRecord = {levels: [], subNodePositions: []};
        for (i = 0; o = recordList[i]; i++) {
            $FIXED_THICKNESS_LAYOUT_MERGE.call(this, thisRecord, o);
        }
        
        // 得到本节点位置，用取中点方式。
        // 可以用override方式提供外部的计算函数，用于一些视觉效果的修正。
        thisY = this.$calculateFatherPositionBySub(root, thisRecord).y;
        
        // 本节点的record
        thisRecord.levels.splice(
            0, 
            0, 
            { yMin: thisY, yMax: thisY + root._nHeight }
        );
        
        // 为子节点赋对于本节点的偏移值
        for (i = 0; o = children[i]; i++) {
            if (o._bToVisible) {
                o._nLayoutXOffset = 
                    this._uChart._oConfig.nodeWidth 
                    + this._uChart._oConfig.nodeIntervalX;
                o._nLayoutYOffset = thisRecord.subNodePositions[i].y - thisY;
            }
        }
        
        return thisRecord;
    }
    
    function $FIXED_THICKNESS_LAYOUT_MERGE(thisRecord, nextRecord) {
        var i, o, maxLevel, minGap, joinLevel, 
            thisLevels, nextLevels, offsetY, modifyRecord, 
            newYMin, newYMax, thisYMax, nextYMax;
            
        thisLevels = thisRecord.levels;
        nextLevels = nextRecord.levels;
        
        // 初始情况
        if (thisLevels.length == 0) {
            for (i = 0; o = nextLevels[i]; i++) {
                thisLevels.push({yMin: o.yMin, yMax: o.yMax});
            }
            thisRecord.subNodePositions.push({y: nextLevels[0].yMin});
            return;
        }
        
        maxLevel = thisLevels.length > nextLevels.length 
            ? thisLevels.length : nextLevels.length;
        
        // 寻找上下接点和y最值
        minGap = Number.MAX_VALUE; 
        thisYMax = nextYMax = Number.MIN_VALUE;
        for (i = 0; i < maxLevel; i++) {
            if (thisLevels[i] && nextLevels[i]) { 
                if ((o = nextLevels[i].yMin - thisLevels[i].yMax) < minGap) {
                    minGap = o;
                    joinLevel = i;
                }
            }
            if (thisLevels[i] && thisYMax < thisLevels[i].yMax) {
                thisYMax = thisLevels[i].yMax;
            }
            if (nextLevels[i] && nextYMax < nextLevels[i].yMax) {
                nextYMax = nextLevels[i].yMax;
            }
        }
        
        // 得到next的偏移量
        offsetY = thisLevels[joinLevel].yMax 
            + this._uChart._oConfig.nodeIntervalY 
            - nextLevels[joinLevel].yMin;
        offsetY = offsetY < 0 ? 0 : offsetY;
        o = thisYMax - (nextYMax + offsetY); // 防止“陷入”而影响视觉
        offsetY += o < 0 ? 0 : o;
        
        // 视觉调整，禁止相邻level“陷入”（这里未循环做严格检查）
        if (this._oMode == 'neat') {
            if (nextLevels[joinLevel + 1]) {
                o = thisLevels[joinLevel].yMax 
                    - (nextLevels[joinLevel + 1].yMin + offsetY);
                offsetY += o < 0 ? 0 : o;
            }
            if (thisLevels[joinLevel + 1]) {
                o = thisLevels[joinLevel + 1].yMax 
                    - (nextLevels[joinLevel].yMin + offsetY);
                offsetY += o < 0 ? 0 : o;   
            }
        }
        
        // next的根位置
        thisRecord.subNodePositions.push({ y: nextLevels[0].yMin + offsetY });
        
        // 根据基准把nextRecord融合thisRecord
        for (i = 0; i < maxLevel; i++) {
            if (thisLevels[i]) {
                newYMin = thisLevels[i].yMin;
            } else {
                newYMin = nextLevels[i].yMin + offsetY;   
            }
            if (nextLevels[i]) {
                newYMax = nextLevels[i].yMax + offsetY;
            } else {
                newYMax = thisLevels[i].yMax;   
            }
            thisLevels[i] = {yMin: newYMin, yMax: newYMax};
        }
    }

})();
