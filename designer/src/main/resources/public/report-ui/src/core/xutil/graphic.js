/**
 * xutil.graphic
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    图形图像相关工具函数
 * @author:  sushuang(sushuang)
 * @depend:  none
 */

(function () {
    
    var GRAPHIC = xutil.graphic; 

    /**
     * 合并外界矩形
     *
     * @public
     * @param {Object...} bound...，可传入多个。
     *      bound格式：{left:..,top:..,width:..height:..}
     * @return {Object} 最大外界构成的新bound。如果为null则表示输入全为空。
     */
    GRAPHIC.unionBoundBox = function () {
        var left;
        var top;
        var right;
        var bottom;
        var width;
        var height;
        var bound = null, subBound;

        for(var i = 0, l = arguments.length; i < l; i ++) {
            if( !( subBound = arguments[i])) {
                continue;
            }

            if( !bound) {
                bound = subBound;
            } 
            else {
                left = subBound.left < bound.left 
                    ? subBound.left : bound.left;
                top = subBound.top < bound.top 
                    ? subBound.top : bound.top;
                right = subBound.left + subBound.width;
                width = right > bound.left + bound.width 
                    ? right - bound.left : bound.width;
                bottom = subBound.top + subBound.height;
                height = bottom > bound.top + bound.height 
                    ? bottom - bound.top : bound.height;
                bound.left = left;
                bound.top = top;
                bound.width = width;
                bound.height = height;
            }
        }
        return bound;
    };

})();
        