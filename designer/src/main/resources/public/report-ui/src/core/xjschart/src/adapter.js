/**
 * adapter of xjschart
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    JS图的适配
 * @author:  sushuang(sushuang)
 * @depend:  xutil
 */

(function() {
    
    var xutil = window.xutil;
    var X = window.xjschart;
    var R = window.Raphael;
    var util = X.util = {};

    var xobject = xutil.object;
    var xcollection = xutil.collection;
    var xlang = xutil.lang;
    var xfn = xutil.fn;
    var xdom = xutil.dom;
    var xnumber = xutil.number;

    util.inheritsObject = xobject.inheritsObject;
    util.assign = xobject.assign;
    util.extend = xobject.extend;
    util.clone = xobject.clone;
    util.travelTree = xcollection.travelTree;
    util.STOP_SUB_TREE_TRAVEL = xcollection.STOP_SUB_TREE_TRAVEL;
    util.isString = xlang.isString;
    util.isObject = xlang.isObject;
    util.isString = xlang.isString;
    util.bind = xfn.bind;
    util.ieVersion = xdom.ieVersion;
    util.g = xdom.g;
    
    /**
     * 简便方法，从paper中移除节点
     *
     * @public
     * @param {Object|Raphael.Element|Raphael.Set} o 节点所属的对象, 或者节点本身
     * @param {string} attrName 节点属性名，如果传值则认为o为节点所属对象
     * @return {Raphael.Element} 被移除的节点
     */
    util.removeElement = function(o, attrName) {
        var elst;
        if (attrName == null) {
            elst = o;   
        } 
        else {
            elst = o[attrName];
            delete o[attrName];   
        }
        if (elst) {
            elst.remove && elst.remove();
            elst.clear && elst.clear();
        }
        return elst;
    }

    /**
     * 取得或者值或者默认值的方便函数
     *
     * @public
     * @param {*} value 值，如果通过validFunc的判断则返回此值
     * @param {*} defaultValue，默认值，如果未通过validFunc的判断则返回此值
     * @param {function(*):boolean} validFunc 判断value是否合法的函数
     *              缺省则为：function(v) { return v != null; }
     */
    util.getValue = function(value, defaultValue, validFunc) {
        if (validFunc ? validFunc(value) : (value != null)) {
            return value;
        } 
        else {
            return defaultValue;
        }
    }
    
    /**
     * 对show函数进行包装的方法
     * 如果使用Raphael.Set来组织dom节点，
     * 所有dom节点一般都是平的，而非嵌套包含关系。
     * 对set调用show、hide时，会深入到每个set中的节点执行show、hide。
     *
     * 如果需要模拟出正常HTML的节点嵌套包含关系，
     * 即类似HTML中，内外层节点都需要控制show、hide的情况：
     * <div style="display:none">
     *      <div style="display:none"> ... 
     *      </div>
     * </div>
     * 那么可使用此函数，对Raphael.Set或Raphael.Element的原生show重载
     * 
     * 此函数需要每个需要控制show、hide的元素都有个boolean属性表示可见与否，
     * 在调用show、hide时会沿elArr根据“父”节点的这些属性进行判断，
     *（此属性的功能同上例HTML中的display:...）
     *
     * @public
     * @param {Raphael.Element} targetEl 目标Element对象
     * @param {Array.<Object>} elArr 这是targetEl的逻辑祖先节点列表，
     *          从逻辑上最外层到最内层。
     *          例如，可以是这样：[parentSet, subElement, subSubElement]
     * @param {string} visibleAttrName 每个Element或者Set中都要有个属性表示可见与否，
     *          这是属性名，默认为'_bVisible'
     */
    util.overrideShow = function(targetEl, elArr, visibleAttrName) {
        if (visibleAttrName == null) {
            visibleAttrName = '_bVisible';
        }

        var oldFunc = targetEl.show;
        targetEl.show = function() {
            if (this[visibleAttrName] != null 
                && !this[visibleAttrName]
            ) {
                return;
            }
            for (var i = 0; i < elArr.length; i ++) {
                if (elArr[i][visibleAttrName] != null 
                    && !elArr[i][visibleAttrName]
                ) {
                    return;
                }
            }
            oldFunc.call(this);
        };
    }

    /**
     * 包装dom事件对象，屏蔽浏览器差异
     *
     * @public
     * @param {Event} event dom事件
     * @return {Object} 包装后的事件
     *              {number} pageX 鼠标的X轴坐标
     *              {number} pageY 鼠标的Y轴坐标
     *              {number} which 触发事件的按键码
     *              {HTMLElement} target 触发事件的 Element 对象
     */
    util.wrapEvent = function (event) {
        var body = document.body;
        var html = util.ieVersion ? body.parentElement : body.parentNode;

        if (util.ieVersion) {
            event = window.event;
            event.pageX = html.scrollLeft + body.scrollLeft 
                - html.clientLeft + event.clientX - body.clientLeft;
            event.pageY = html.scrollTop + body.scrollTop 
                - html.clientTop + event.clientY - body.clientTop;
            event.target = event.srcElement;
            event.which = event.keyCode;
        }

        var wrap = {};
        wrap.type = event.type;
        wrap.pageX = event.pageX;
        wrap.pageY = event.pageY;
        wrap.which = event.which;
        wrap.target = event.target;
        wrap._oNative = event;

        return wrap;
    };

    /**
     * 挂载事件
     * 
     * @public
     * @param {Object} obj 响应事件的对象
     * @param {string} type 事件类型名称（不含'on'）
     * @param {Function} func 事件处理函数
     */
    util.attachEvent = util.ieVersion 
        ?   function (obj, type, func) {
                obj.attachEvent('on' + type, func);
            } 
        :   function (obj, type, func) {
                obj.addEventListener(type, func, false);
            };

    /**
     * 卸载事件
     *
     * @public
     * @param {Object} obj 响应事件的对象
     * @param {string} type 事件类型名称（不含'on'）
     * @param {Function} func 事件处理函数
     */
    util.detachEvent = util.ieVersion 
        ?   function (obj, type, func) {
                obj.detachEvent('on' + type, func);
            } 
        :   function (obj, type, func) {
                obj.removeEventListener(type, func, false);
            };

    /**
     * Raphael的Transform的扩展
     *
     * @public
     * @param {string} tstr transform字符串
     * @return {string} 当前的transfrom字符串
     */
    /*
    R.st.gTransform = function (tstr) {
        this.forEach(function(item) {
            if (!this.matrix) {
                this.matrix = new Matric;
            }
            
            item.gTransform.call(item, tstr);
        })
    };
    
    R.el.gTransform = function (tstr) {
        var _ = this._;
        if (tstr == null) {
            return _.transform;
        }
        gExtractTransform(this, tstr);

        this.clip && $(this.clip, {transform: this.matrix.invert()});
        this.pattern && updatePosition(this);
        this.node && $(this.node, {transform: this.matrix});
    
        if (_.sx != 1 || _.sy != 1) {
            var sw = this.attrs[has]("stroke-width") ? this.attrs["stroke-width"] : 1;
            this.attr({"stroke-width": sw});
        }

        return this;        
    };

    var gExtractTransform = function (el, tstr) {
        if(tstr == null) {
            return el._.transform;
        }
        tstr = Str(tstr).replace(/\.{3}|\u2026/g, el._.transform || E);
        var tdata = R.parseTransformString(tstr), deg = 0, dx = 0, dy = 0, sx = 1, sy = 1, _ = el._, m = new Matrix;
        _.transform = tdata || [];
        if(tdata) {
            for(var i = 0, ii = tdata.length; i < ii; i ++) {
                var t = tdata[i], tlen = t.length, command = Str(t[0]).toLowerCase(), absolute = t[0] != command, inver = absolute ? m.invert() : 0, x1, y1, x2, y2, bb;
                if(command == "t" && tlen == 3) {
                    if(absolute) {
                        x1 = inver.x(0, 0);
                        y1 = inver.y(0, 0);
                        x2 = inver.x(t[1], t[2]);
                        y2 = inver.y(t[1], t[2]);
                        m.translate(x2 - x1, y2 - y1);
                    } 
                    else {
                        m.translate(t[1], t[2]);
                    }
                } 
                else if(command == "r") {
                    if(tlen == 2) {
                        bb = bb || el.getBBox(1);
                        m.rotate(t[1], bb.x + bb.width / 2, bb.y + bb.height / 2);
                        deg += t[1];
                    } 
                    else if(tlen == 4) {
                        if(absolute) {
                            x2 = inver.x(t[2], t[3]);
                            y2 = inver.y(t[2], t[3]);
                            m.rotate(t[1], x2, y2);
                        } 
                        else {
                            m.rotate(t[1], t[2], t[3]);
                        }
                        deg += t[1];
                    }
                } 
                else if(command == "s") {
                    if(tlen == 2 || tlen == 3) {
                        bb = bb || el.getBBox(1);
                        m.scale(t[1], t[tlen - 1], bb.x + bb.width / 2, bb.y + bb.height / 2);
                        sx *= t[1];
                        sy *= t[tlen - 1];
                    } 
                    else if(tlen == 5) {
                        if(absolute) {
                            x2 = inver.x(t[3], t[4]);
                            y2 = inver.y(t[3], t[4]);
                            m.scale(t[1], t[2], x2, y2);
                        } 
                        else {
                            m.scale(t[1], t[2], t[3], t[4]);
                        }
                        sx *= t[1];
                        sy *= t[2];
                    }
                } 
                else if(command == "m" && tlen == 7) {
                    m.add(t[1], t[2], t[3], t[4], t[5], t[6]);
                }
                _.dirtyT = 1;
                el.matrix = m;
            }
        }

        el.matrix = m;

        _.sx = sx;
        _.sy = sy;
        _.deg = deg;
        _.dx = dx = m.e;
        _.dy = dy = m.f;

        if(sx == 1 && sy == 1 && !deg && _.bbox) {
            _.bbox.x += +dx;
            _.bbox.y += +dy;
        } 
        else {
            _.dirtyT = 1;
        }
    };
    */
    
})();