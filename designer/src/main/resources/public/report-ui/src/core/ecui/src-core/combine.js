/*
Combine - 联合器插件。
*/
//{if 0}//
(function () {

    var core = ecui,
        array = core.array,
        ext = core.ext,
        util = core.util,

        REGEXP = RegExp,

        indexOf = array.indexOf,
        remove = array.remove,
        blank = util.blank,

        $connect = core.$connect,
        triggerEvent = core.triggerEvent,

        eventNames = [
            'mousedown', 'mouseover', 'mousemove', 'mouseout', 'mouseup',
            'click', 'dblclick', 'mousewheel', 'keydown', 'keypress', 'keyup',
            'focus', 'blur', 'activate', 'deactivate'
        ];
//{/if}//
//{if $phase == "define"}//
    /**
     * 控件组合。
     * 控件组合后形成一个共同体虚拟控件，虚拟控件体内所有控件的基本事件与操作将同时进行，一个控件只能被组合到一个共同体虚拟控件中。
     * @public
     *
     * @param {Array} controls 需要组合的控件列表
     * @param {Array} names 需要组合的操作名称列表，如果存在 * 表示需要加载全部的默认操作
     */
    var COMBINE = function (controls, names) {
            this._aControls = [];
            if (!names) {
                names = ['disable', 'enable'];
            }
            else if ((i = indexOf(names, '*')) >= 0) {
                names.splice(i, 1, 'disable', 'enable');
                names = eventNames.concat(names);
            }
            names.splice(0, 0, '$dispose');
            this._aNames = names;
            for (var i = 0, o; o = controls[i++]; ) {
                if ('string' == typeof o) {
                    $connect(this, EXT_COMBINE_BIND, o);
                }
                else {
                    EXT_COMBINE_BIND.call(this, o);
                }
            }
        },
        EXT_COMBINE_CLASS = COMBINE.prototype,
        EXT_COMBINE_PROXY = {};
//{else}//
    /**
     * 联合器调用方法创建。
     * 联合器的方法都创建在代理对象中，用于分组进行调用。
     * @public
     *
     * @param {string} name 需要创建的方法名
     * @return {Function} 进行分组联合调用的函数
     */
    function EXT_COMBINE_BUILD(name) {
        if (!EXT_COMBINE_PROXY[name]) {
            EXT_COMBINE_CLASS[name] = function () {
                var i = 0,
                    uid = this.getUID(),
                    combine = COMBINE[uid],
                    o;

                combine[name] = blank;
                for (; o = combine._aControls[i++]; ) {
                    if (indexOf(eventNames, name) < 0) {
                        COMBINE[uid + name].apply(o, arguments);
                    }
                    else if (o != this) {
                        triggerEvent(o, name, arguments[0]);
                    }
                }
                delete combine[name];
            };

            EXT_COMBINE_PROXY[name] = function () {
                COMBINE[this.getUID()][name].apply(this, arguments);
            };
        }

        return EXT_COMBINE_PROXY[name];
    }

    /**
     * 为控件绑定需要联合调用的方法。
     * @public
     *
     * @param {ecui.ui.Control} control 控件对象
     */
    function EXT_COMBINE_BIND(control) {
        for (var i = 0, uid = control.getUID(), o; o = this._aNames[i++]; ) {
            if (indexOf(eventNames, o) < 0) {
                COMBINE[uid + o] = control[o];
                control[o] = EXT_COMBINE_BUILD(o);
            }
            else {
                core.addEventListener(control, o, EXT_COMBINE_BUILD(o));
            }
        }
        this._aControls.push(control);
        COMBINE[uid] = this;
    }

    /**
     * 联合器释放。
     * @protected
     */
    EXT_COMBINE_PROXY.$dispose = function () {
        var i = 0,
            uid = this.getUID(),
            combine = COMBINE[uid],
            el = this.getMain(),
            o = [this.getClass()].concat(this.getTypes());

        COMBINE[uid + '$dispose'].call(this);
        el.className = o.join(' ');
        remove(combine._aControls, this);
        for (; o = combine._aNames[i++]; ) {
            delete COMBINE[uid + o];
        }
    };

    /**
     * 联合器插件加载。
     * @public
     *
     * @param {ecui.ui.Control} control 需要应用插件的控件
     * @param {string} value 插件的参数
     */
    ext.combine = function (control, value) {
        if (/(^[^(]+)(\(([^)]+)\))?$/.test(value)) {
            value = REGEXP.$3;
            new COMBINE(
                [control].concat(REGEXP.$1.split(/\s+/)),
                value.split(/\s+/)
            );
        }
    };
//{/if}//
//{if 0}//
})();
//{/if}//