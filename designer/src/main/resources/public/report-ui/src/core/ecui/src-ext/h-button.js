/**
 * ecui.ui.HButton
 * Copyright 2013 Baidu Inc. All rights reserved
 *
 * @file:   可定制皮肤的左右结构的button
 * @author: sushuang(sushuang)
 */

 (function () {
    
    var inheritsControl = ecui.inherits;
    var UI_BUTTON = ecui.ui.Button;
    var moveElements = ecui.dom.moveElements;
    var createDom = ecui.dom.create;
    var addClass = ecui.dom.addClass;

    /**
     * 可定制皮肤的左右结构的button
     *
     * @class
     * @param {Object} options 选项
     * @param {string} options.skin 皮肤（的css类）
     * @param {string} options.text 按钮上的文字
     */
    var UI_H_BUTTON = ecui.ui.HButton =
        inheritsControl(
            UI_BUTTON,
            null,
            function (el, options) {
                if (options.skin) {
                    addClass(el, options.skin);
                    options.primary = options.skin;
                }
            },
            function (el, options) {
                var type = this.getType();

                var o = createDom(type + '-text', '', 'span');
                this.$setBody(o);
                moveElements(el, o, true);

                el.appendChild(createDom(type + '-inner', ''));
                el.firstChild.appendChild(createDom(type + '-left', '', 'span'));
                el.firstChild.appendChild(o);
                el.firstChild.appendChild(createDom(type + '-right', '', 'span'));
            }
        );

    var UI_H_BUTTON_CLASS = UI_H_BUTTON.prototype;

 }) ();