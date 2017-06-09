(function () {
    var core = ecui,
        array = core.array,
        dom = core.dom,
        ui = core.ui,
        util = core.util,
        string = core.string,

        $fastCreate = core.$fastCreate,
        setFocused = core.setFocused,
        createDom = dom.create,
        children = dom.children,
        moveElements = dom.moveElements,
        getPosition  = dom.getPosition,
        inheritsControl = core.inherits,
        isContentBox = core.isContentBox,
        getStatus = core.getStatus,
        getView = util.getView,
        triggerEvent = core.triggerEvent,
        trim = string.trim,

        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = UI_CONTROL.prototype,

        UI_TIP_TIME_OPEN = 500,
        UI_TIP_TIME_CLOSE = 200,
        REPAINT = core.REPAINT,

        uiPsTipLayer = null;

    var UI_TIP = ui.Tip = 
        inheritsControl(
            UI_CONTROL,
            'ui-tip',
            function(el, options) {
                options.message = trim(el.innerHTML) || options.message;
                el.innerHTML = '';
            },
            function (el, options) {
                this._sTarget = options.target;
                this._sMessage = options.message;
                this._oTimer = null;
            }
        ),

        UI_TIP_CLASS = UI_TIP.prototype,
        UI_TIP_LAYER = UI_TIP_CLASS.Layer = 
        inheritsControl(
            UI_CONTROL,
            'ui-tip-layer',
            function (el, options) {
                el.appendChild(createDom(this.getTypes() + '-corner'));
                el.appendChild(createDom());
            },
            function (el, options) {
                el = children(el);
                this._eCorner = el[0];
                this.$setBody(el[1]);
            }
        ),

        UI_TIP_LAYER_CLASS = UI_TIP_LAYER.prototype;


    function UI_TIP_LAYER_GET() {
        var o;
        if (!uiPsTipLayer) {
            o = document.body.appendChild(createDom(UI_TIP_LAYER.TYPES));
            uiPsTipLayer = $fastCreate(UI_TIP_LAYER, o);
            uiPsTipLayer.cache();
            uiPsTipLayer.init();
        }
        return uiPsTipLayer;
    }


    UI_TIP_CLASS.$mouseover = function () {
        var con = this;
        UI_CONTROL_CLASS.$mouseover.call(this);
        clearTimeout(this._oTimer);
        if (!this._bShow) {
            this._oTimer = setTimeout(function () {
                con.open();
            }, UI_TIP_TIME_OPEN);
        }
    }

    UI_TIP_CLASS.$mouseout = function () {
        var con = this;
        UI_CONTROL_CLASS.$mouseout.call(this);
        clearTimeout(this._oTimer);
        if (this._bShow) {
            this._oTimer = setTimeout(function () {
                con.close()
            }, UI_TIP_TIME_CLOSE);
        }
    }

    UI_TIP_CLASS.$getTarget = function (id) {
        return document.getElementById(id);
    }

    UI_TIP_CLASS.setTarget = function (id) {
        this._sTarget = id;
    }

    UI_TIP_CLASS.open = function () {
        var layer = UI_TIP_LAYER_GET();

        if (this._sTarget) {
            o = this.$getTarget(this._sTarget);
            if (o) {
                if ('[object String]' == Object.prototype.toString.call(o)) {
                    layer.getBody().innerHTML = o;
                }
                else {
                    layer.getBody().innerHTML = o.innerHTML;
                }
            }
        }
        else if (this._sMessage) {
            layer.setContent(this._sMessage);
        }

        layer.show(this);
        this._bShow = true;
    }

    UI_TIP_CLASS.close = function () {
        UI_TIP_LAYER_GET().hide();
        this._bShow = false;
    }

    UI_TIP_LAYER_CLASS.show = function (con) {
        var pos = getPosition(con.getOuter()),
            type = this.getTypes()[0],
            view = getView(),
            cornerHeight = 13,
            w = con.getWidth(), h = con.getHeight(),
            wFix = 9, hFix = 13,
            className = [];

        if (con) {
            this._uHost = con;
        }

        UI_CONTROL_CLASS.show.call(this);
        this.resize();
        if (pos.left + this.getWidth() > view.right) {
            pos.left = pos.left + w - this.getWidth() + wFix;
            className.push('-right')
        }
        else {
            pos.left = pos.left - wFix;
            className.push('-left');
        }

        if (pos.top - cornerHeight - this.getHeight() < view.top 
                && pos.top + h + cornerHeight + this.getHeight() < view.bottom) {
            pos.top += h + cornerHeight;
            className.push('-bottom');
        }
        else {
            pos.top -= cornerHeight + this.getHeight();
            className.push('-top');
        }

        this._eCorner.className = type + '-corner ' + type + '-corner' + className.join('');
        this.setPosition(pos.left, pos.top);
    }

    UI_TIP_LAYER_CLASS.$mouseover = function () {
        UI_CONTROL_CLASS.$mouseover.call(this);
        this._uHost.$mouseover();
    }

    UI_TIP_LAYER_CLASS.$mouseout = function () {
        UI_CONTROL_CLASS.$mouseout.call(this);
        this._uHost.$mouseout();
    }

    UI_TIP_LAYER_CLASS.$resize = function () {
         var el = this._eMain,
            currStyle = el.style;

        currStyle.width = this._sWidth;
        currStyle.height = this._sHeight;
        this.repaint();
    }
})();
