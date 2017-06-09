/**
 * ecui.ui.SwitchButton
 * Copyright 2013 Baidu Inc. All rights reserved
 *
 * @file:   可切换状态的button
 * @author: sushuang(sushuang)
 */

 (function () {
    
    var inheritsControl = ecui.inherits;
    var UI_H_BUTTON = ecui.ui.HButton;
    var moveElements = ecui.dom.moveElements;
    var createDom = ecui.dom.create;
    var triggerEvent = ecui.triggerEvent;
    var addClass = ecui.dom.addClass;
    var extend = ecui.util.extend;

    /**
     * 可切换状态的button
     *
     * @class
     * @extends {ecui.ui.Control}
     * @param {Array.<Object>} statusList
     *      内部元素为 text ... value ...
     * @param {number} status
     */
    var UI_SWITCH_BUTTON = ecui.ui.SwitchButton =
        inheritsControl(
            UI_H_BUTTON,
            null,
            null,
            function (el, options) {
                var type = this.getType();
                this._aStatusList = options.statusList || [];
                var index;
                for (var i = 0, o; o = this._aStatusList[i]; i ++) {
                    if (o.value == options.status) {
                        index = i;
                        break;
                    }
                }
                this.$switchStatus(index);
            }
        );

    var UI_SWITCH_BUTTON_CLASS = UI_SWITCH_BUTTON.prototype;

    UI_SWITCH_BUTTON_CLASS.$click = function () {
        UI_SWITCH_BUTTON.superClass.$click.apply(this, arguments);
        this.$switchStatus();
        triggerEvent(this, 'change');
    };

    UI_SWITCH_BUTTON_CLASS.$switchStatus = function (index) {
        var statusList = this._aStatusList;
        if (statusList.length == 0) {
            return;
        }

        var nextIndex = index != null 
            ? index
            : (
                this._nIndex == null
                ? 0
                : (this._nIndex + 1) % statusList.length
            );
        this._nIndex = nextIndex;

        this.setText(statusList[this._nIndex].text);
    };

    UI_SWITCH_BUTTON_CLASS.getValue = function () {
        return this._aStatusList[this._nIndex].value;
    };

 }) ();
