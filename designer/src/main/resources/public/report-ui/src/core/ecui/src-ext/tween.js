/*
Tween - 点击及按压动画插件
*/
//{if 0}//
(function () {

    var core = ecui,
        ext = core.ext,
        util = core.util,

        FUNCTION = Function,
        MATH = Math,
        MIN = MATH.min,
        POW = MATH.pow,

        timer = util.timer;
//{/if}//
//{if $phase == "define"}//
    /**
     * 初始化点击时动画效果。
     * options 对象支持的属性如下：
     * second    动画持续的时间
     * pressStep 按压时的间隔，如果省略不支持按压移动的动画效果
     * monitor   需要监控的属性
     * getValue  获取监控属性的值
     * setValue  设置监控属性的值
     * @public
     *
     * @param {Function|ecui.ui.Control} object 需要实现动画效果的类或者是对象
     * @param {Object} options 动画效果的初始化选项
     */
    var EXT_TWEEN =
        ext.Tween = function (object, options) {
            //__gzip_unitize__start
            //__gzip_unitize__value
            //__gzip_unitize__end
            var click = object.$click,
                activate = object.$activate,
                deactivate = object.$deactivate,
                totalTime = (options.second * 1000) || 500,
                pressStep = options.pressStep,
                getValue = options.getValue ||
                    new FUNCTION(
                        'o',
                        'return [ecui.util.toNumber(o.' + options.monitor.replace(/\|/g, '),ecui.util.toNumber(o.') +
                            ')]'
                    ),
                setValue = options.setValue ||
                    new FUNCTION(
                        'o',
                        'v',
                        'o.' + options.monitor.replace(/\|/g, '=v[0]+"px";v.splice(0,1);o.') + '=v[0]+"px"'
                    );

            /**
             * 减减速动画。
             * @private
             */
            function decelerate() {
                var options = EXT_TWEEN[this.getUID()],
                    start = options.start,
                    end = options.end,
                    value = options.value = {},
                    x = MIN((options.time += 20) / totalTime, 1),
                    name;

                if (x == 1) {
                    // 移动到达终点准备停止
                    options.stop();
                    EXT_TWEEN[this.getUID()] = null;
                }

                for (name in start) {
                    // 按比例计算当前值
                    value[name] = start[name] + (end[name] - start[name]) * (1 - POW(1 - x, 3));
                }
                setValue(this, value);
            }

            /**
             * 匀速动画。
             * @private
             */
            function steady() {
                var options = EXT_TWEEN[this.getUID()],
                    start = options.start,
                    end = options.end,
                    value = options.value,
                    flag = true,
                    tmp,
                    name;

                // 第一个flag用于检测所有的移动是否都结束
                for (name in start) {
                    tmp = 'number' == typeof pressStep ? pressStep : pressStep[name];
                    if (start[name] < end[name]) {
                        if ((value[name] += tmp) < end[name]) {
                            flag = false;
                        }
                    }
                    else if (start[name] > end[name]) {
                        if ((value[name] -= tmp) > end[name]) {
                            flag = false;
                        }
                    }
                }

                // 以下flag用于检测是否要停止移动
                if (flag) {
                    // 捕获下一步的位置
                    setValue(this, end);
                    click.call(this);
                    tmp = getValue(this);
                    for (name in tmp) {
                        if (end[name] == tmp[name]) {
                            value[name] = tmp[name];
                        }
                        else {
                            flag = false;
                        }
                    }
                    if (flag) {
                        options.stop();
                    }
                    else {
                        // 得到新的结束位置
                        options.end = tmp;
                    }
                }

                setValue(this, value);
            }

            /**
             * 开始动画。
             * @private
             *
             * @param {ecui.ui.Control} control 控件对象
             * @param {Function} action 动画函数
             * @param {number} interval 时间间隔
             * @param {Event} event 事件对象
             */
            function startTween(control, action, interval, event) {
                // 捕获动画的结束点
                click.call(control, event);

                var options = EXT_TWEEN[control.getUID()],
                    start = options.start,
                    end = options.end = getValue(control),
                    flag = false,
                    name;

                for (name in start) {
                    if (start[name] != end[name]) {
                        // 开始与结束的位置有变化，允许开始动画
                        flag = true;
                    }
                }

                if (flag) {
                    options.time = 0;
                    action.call(control);
                    options.stop = timer(action, -interval, control);
                }
            }

            if (pressStep) {

                /**
                 * 实现动画的点击方法。
                 * @protected
                 *
                 * @param {Event} event 事件对象
                 */
                object.$click = function (event) {
                    // 捕获需要到达的位置
                    var value = getValue(this);
                    click.call(this, event);
                    setValue(this, value);
                };

                /**
                 * 实现动画的激活开始方法。
                 * @protected
                 *
                 * @param {Event} event 事件对象
                 */
                object.$activate = function (event) {
                    var options = EXT_TWEEN[this.getUID()];

                    if (options) {
                        // 之前存在未结束的动画，直接结束
                        options.stop();
                        setValue(this, options.end);
                    }
                    else {
                        options = EXT_TWEEN[this.getUID()] = {};
                        options.start = getValue(this);
                        options.value = getValue(this);
                    }

                    startTween(this, steady, 40, event);

                    activate.call(this, event);
                };

                /**
                 * 实现动画的激活结束方法。
                 * @protected
                 *
                 * @param {Event} event 事件对象
                 */
                object.$deactivate = function (event) {
                    var options = EXT_TWEEN[this.getUID()];

                    // 动画转入减减速运动
                    options.stop();
                    options.start = options.value;
                    options.stop = timer(decelerate, -20, this);

                    deactivate.call(this, event);
                };
            }
            else {
                /**
                 * 实现动画的点击方法。
                 * @protected
                 *
                 * @param {Event} event 事件对象
                 */
                object.$click = function (event) {
                    var options = EXT_TWEEN[this.getUID()];

                    if (options) {
                        // 如果之前有未完成的动画，立即结束，以当前的位置作为新的开始点
                        options.stop();
                        setValue(this, options.end);
                        options.start = options.value;
                    }
                    else {
                        // 新的动画开始创建
                        options = EXT_TWEEN[this.getUID()] = {};
                        options.start = getValue(this);
                    }

                    startTween(this, decelerate, 20, event);
                };
            }
        };
//{else}//
//{/if}//
//{if 0}//
})();
//{/if}//