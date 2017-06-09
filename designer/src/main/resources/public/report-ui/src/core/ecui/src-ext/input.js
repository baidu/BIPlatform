/**
 * input
 * Copyright 2012 Baidu Inc. All rights reserved.
 * 
 * path:    count-input.js
 * desc:    带计数的文本输入框(input与textarea)
 * author:  cxl(chenxinle)
 *          modified by sushuang(sushuang) 
 * date:    2012/03/12
 */
(function () {

    var core = ecui,
        dom = core.dom,
        string = core.string,
        ui = core.ui,
        util = core.util,

        attachEvent = util.attachEvent,
        createDom = dom.create,
        addClass = dom.addClass,
        removeClass = dom.removeClass,
        removeDom = dom.remove,
        insertAfter = dom.insertAfter,
        trim = string.trim,
        setFocused = core.setFocused,
        blank = util.blank,
        triggerEvent = core.triggerEvent,
        inheritsControl = core.inherits,
        getByteLength = string.getByteLength,

        UI_CONTROL = ui.Control,
        UI_CONTROL_CLASS = UI_CONTROL.prototype,
        UI_INPUT_CONTROL = ui.InputControl,
        UI_INPUT_CONTROL_CLASS = UI_INPUT_CONTROL.prototype,


        UI_INPUT = ui.Input = inheritsControl(
            UI_INPUT_CONTROL,
            'ui-input',
            function (el, options) {
                options.resizable = false;
                this._bPromptDisabled = options.promptDisabled == null 
                    ? true : options.promptDisabled;
            },
            function (el, options) {
                var o, type = this.getType();
                
                this.getInput().style.border = '';

                if(options.maxLength){
                    this._sMaxLength = options.maxLength;
                }

                if (options.tip) {
                    o = createDom(type + '-tip', 'display:none');
                    o.innerHTML = options.tip;
                    this.getBody().appendChild(o);
                    this._eTip = o;
                    attachEvent(this._eTip, 'keypressdown', UI_INPUT_TIP_HANDLER);
                }
            }
        ),
        UI_INPUT_CLASS = UI_INPUT.prototype,

        UI_TEXTAREA = ui.Textarea = inheritsControl(
            UI_INPUT,
            'ui-textarea',
            function (el, options) {
                options.inputType = 'textarea';
                this._bCountDisabled = options.countDisabled;
            }
        ),
        UI_TEXTAREA_CLASS = UI_TEXTAREA.prototype;

    var COUNT_NORMAL_TPL = '还可以输入$字',
        COUNT_OVERFLOW_TPL = '已经超过$字',
        CHAR_SET = 'gbk';

    function UI_INPUT_TIP_HANDLER(event) {
        var e = event || window.event,
            con;
        e = e.target || e.srcElement;
        con = e.parentNode.getControl();
        con.getInput().focus();
    }

    function UI_INPUT_TIP_DISPLAY(con, show) {
        if (con._eTip) {
            con._eTip.style.display = show ? '' : 'none';
        }
    }

    UI_INPUT_CLASS.$keydown = function () {
        UI_INPUT_TIP_DISPLAY(this, false);
    };

    UI_INPUT_CLASS.$keyup = function () {
        if (!this.getValue()) {
            UI_INPUT_TIP_DISPLAY(this, true);
        }        
    };

    UI_INPUT_CLASS.$change = function () {
        this.$updateCount();
    };

    UI_INPUT_CLASS.$updateCount = function () {
        var value = this.getValue(),
            type = this.getType(),
            byteLength,
            remain;
        
        if (this._sMaxLength && this._eCount){
            byteLength = getByteLength(value, CHAR_SET);

            if (byteLength > this._sMaxLength){
                remain = [
                    '<span class="', type, '-count-overflow">',
                        Math.ceil((byteLength - this._sMaxLength) / 2),
                    '</span>'
                ].join('');
                this._eCount.innerHTML = COUNT_OVERFLOW_TPL.replace('$', remain);
            }
            else {
                remain = [
                    '<span class="', type, '-count-normal">',
                        Math.floor((this._sMaxLength - byteLength) / 2),
                    '</span>'
                ].join('');
                this._eCount.innerHTML = COUNT_NORMAL_TPL.replace('$', remain);
            }
        }
    };

    UI_INPUT_CLASS.$blur = function () {
        UI_CONTROL_CLASS.$blur.call(this);
        if (!this.getValue()) {
            UI_INPUT_TIP_DISPLAY(this, true);
        }
    };

    UI_INPUT_CLASS.$focus = function () {
        UI_CONTROL_CLASS.$focus.call(this);
        UI_INPUT_TIP_DISPLAY(this, false);
    };

    UI_INPUT_CLASS.$setSize = blank;

    UI_INPUT_CLASS.setValue = function (value) {
        UI_INPUT_CONTROL_CLASS.setValue.call(this, value);
        UI_INPUT_TIP_DISPLAY(this, value ? false : true);
        this.$updateCount();
    };

    UI_INPUT_CLASS.init = function () {
        if (!this.getValue()) {
            UI_INPUT_TIP_DISPLAY(this, true);
        }
        var type = this.getType();
        if (!this._bPromptDisabled) {
            if (!this._eBar) {
                this._eBar = createDom(type + '-bar');
                insertAfter(this._eBar, this.getOuter());
            }
            this._eBar.appendChild(
                this._ePrompt = createDom(type + '-prompt')
            );
        }
        UI_INPUT_CONTROL_CLASS.init.call(this);
    };

    /**
     * 显示错误
     *
     * @public
     * @param {boolean} error true则显示错误，false则还原
     * @prompt {string} 提示信息
     */
    UI_INPUT_CLASS.setErrorView = function (error, prompt) {
        if (error) {
            addClass(this.getOuter(), this.getType() + '-error');
        } 
        else {
            removeClass(this.getOuter(), this.getType() + '-error');
        }
        if (this._ePrompt) {
            this._ePrompt.innerHTML = prompt == null ? '' : prompt
        }
    };

    UI_TEXTAREA_CLASS.init = function () {
        var type = this.getType();
        if (this._sMaxLength && !this._bCountDisabled) {
            if (!this._eBar) {
                this._eBar = createDom(type + '-bar');
                insertAfter(this._eBar, this.getOuter());
            }            
            this._eBar.appendChild(
                this._eCount = createDom(type + '-count')
            );
            this.$updateCount();
        }
        UI_TEXTAREA.superClass.init.call(this);
    };

    UI_TEXTAREA_CLASS.$dispose = function () {
        this._eBar && removeDom(this._eBar);
        this._eBar = null;
        this._eCount = null;
        this._ePrompt = null;

        UI_TEXTAREA.superClass.$dispose.call(this);
    }

})();
