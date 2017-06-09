/**
 * di.helper.Dialog
 * Copyright 2012 Baidu Inc. All rights reserved.
 * 
 * @file:    全局的提示信息 
 *           (代码拷贝自 rigel.layer。但是tip是“小窍门”的意思，而不是“提示”的意思，所以改成prompt)
 * @author:  sushuang(sushuang)
 * @depends: ecui
 */

$namespace('di.helper');

(function() {
    
    //--------------------------------
    // 引用
    //--------------------------------

    var ui = ecui;
    var encodeHTML = xutil.string.encodeHTML;
    var LANG;
    var UTIL;
    var DICT;
    var DI_FACTORY;
    /* globals xutil */
    var domQ = xutil.dom.q;

    $link(function() {
        LANG = di.config.Lang;
        DICT = di.config.Dict;
        UTIL = di.helper.Util;
        DI_FACTORY = di.shared.model.DIFactory;
    });

    //--------------------------------
    // 类型声明
    //--------------------------------

    var DIALOG = $namespace().Dialog = {};
    
    var ePrompt = null;
    var bPromptMask = false;
    var promptTimer = null;
    //  是否需要调整弹出窗口位置（当嵌套两层iframe时，可以根据父窗口滚动条修正弹出位置）
    var bAdjustDialogPosition = false;

    DIALOG.prompt = function () {
        prompt.apply(this, arguments);
    };
    DIALOG.waitingPrompt = function () {
        waitingPrompt.apply(this, arguments);
    };
    DIALOG.hidePrompt = function () {
        hidePrompt.apply(this, arguments);
    };

    /**
     * 设置prompt定义
     *
     * @public
     * @param {Object} def 定义
     * @param {string} def.anchor 值可为：
     *      'I'：internal，在报表引擎内部定位，如果是iframe加载报表引擎，这样则定位不理想），默认
     *      'E'：external，在报表引擎外定位（报表引擎所在的iframe的window上）
     * @param {string} diAgent 是否为stub
     */
    DIALOG.setPromptDef = function(def, diAgent) {
        if (diAgent == 'STUB' && def && def.anchor == 'E') {
            // 临时写法，后续规整
            // FIXME
            prompt = getRemoteDelegation('prompt');
            hidePrompt = getRemoteDelegation('hideprompt');
            waitingPrompt = getRemoteDelegation('waitingprompt');
        }
    };

    /**
     * 设置prompt定义
     *
     * @public
     */
    function getRemoteDelegation(eventName) {
        return function() {
            var eventChannel = DI_FACTORY().getEventChannel();
            if (eventChannel) {
                eventChannel.triggerEvent(eventName, arguments);
            }
        };
    };

    /**
     * 设置 弹窗定位策略（内部定位则不考虑父窗口滚动条位置；外部定位则参考父窗口滚动条位置）。
     * 方法的变量名只是为了与setPromptDef方法保持一致，其实我并不喜欢。。。（吐槽 by xlst）
     * 
     * @public
     * @param {Object} def 定义
     * @param {string='I'} def.anchor 值可为：
     *      'I'：internal，在报表引擎内部定位，如果是iframe加载报表引擎，这样则定位不理想），默认
     *      'E'：external，在报表引擎外定位（报表引擎所在的iframe的window上）
     * @param {string} diAgent 是否为stub
     */
    DIALOG.setAdjustDialogPosition = function(def, diAgent) {
        if (diAgent == 'STUB') {
            
            //  设置bAdjustDialogPosition的状态。在showDialog中会根据这个状态执行弹窗策略
            if (def && def.anchor == 'E') {
                bAdjustDialogPosition = true;
            }
            else if (def && def.anchor == 'I') {
                bAdjustDialogPosition = false;
            }
        }
    };

    /**
     * 信息提示，支持自动消失
     *
     * @public
     * @param {string} text 信息
     * @param {boolean} mask 是否使用遮罩
     * @param {number} timeout 消失时间
     */
    function prompt(text, mask, timeout) {
        var win;
        try {
            // win = window.top;
            win = window;
            // TODO
            // 在iframe中，根据定位到top中间，或者dom加到top上。
        } 
        catch (e) {
        }
        
        var x = UTIL.getScrollLeft(win) + UTIL.getViewWidth(win) / 2;
        var y = 5;

        if(!ePrompt) {
            ePrompt = document.createElement('div');
            ePrompt.style.cssText = 'display:none;position:fixed;*position:absolute';
            ePrompt.className = 'global-prompt';
            document.body.appendChild(ePrompt);
        }

        clearPromptTimer();

        if(ePrompt.style.display == '') {
            return false;
        }

        ePrompt.innerHTML = text;
        ePrompt.style.display = '';
        ePrompt.style.left = x - ePrompt.offsetWidth / 2 + 'px';
        ePrompt.style.top = y + 'px';
        if(mask) {
            ui.mask(0);
            bPromptMask = true;
        }

        if (timeout) {
            promptTimer = setTimeout(
                function () {
                    DIALOG.hidePrompt();
                }, 
                timeout
            );
        }
        return true;        
    };

    /**
     * 等待提示
     *
     * @public
     * @param {string} text 信息
     * @param {boolean} mask 是否使用遮罩
     * @param {number} timeout 消失时间
     */
    function waitingPrompt(text) {
        if (text == null) {
            text = LANG.AJAX_WAITING;
        }
        text = [
            '<div class="global-prompt-waiting"></div>',
            '<div class="global-prompt-waiting-text">', text, '</div>'
        ].join('');
        DIALOG.prompt(text);
    }
    
    /**
     * 隐藏信息提示
     *
     * @public
     * @param {string} messag 信息
     * @param {boolean} 是否使用遮罩
     * @param {number} timeout 消失时间
     */
    function hidePrompt() {
        clearPromptTimer();
        ePrompt.style.display = 'none';
        if(bPromptMask) {
            bPromptMask = false;
            ui.mask();
        }
    };
        
    function clearPromptTimer() {
        if (promptTimer) {
            clearTimeout(promptTimer);
            promptTimer = null;
        }
    }

    /**
     * 显示提示窗口
     *
     * @public
     * @param {string} text 提示信息
     * @param {string} title 标题
     * @param {Array.<Object>} buttons 按钮，其中每一项结构为
     *      {string} text 按钮文字
     *      {string} className cssClassName
     *      {Function} action 按下按钮的回调
     * @param {number=} mask 使用mask的透明值，如果不传此参数则不使用
     */
    DIALOG.showDialog = function(text, title, buttons, mask) {
        ui.$messagebox(text, title, buttons, mask);
        
        //  如果通过 setAdjustDialogPosition 方法重置了弹窗定位策略，则对 ui-messagebox 元素进行重新定位
        if (bAdjustDialogPosition) {
            var dialogElement = xutil.dom.q('ui-messagebox')[0];
            
            if (dialogElement) {
                DIALOG.adjustDialogPosition(dialogElement);
            }
        }
    };
    
    /**
     * 在页面上重新调整窗口的位置（将综合参考父窗口的滚动条高度）
     *
     * @public
     * @param {HTMLElement} dialogElement 弹窗元素（用于定位的最外层html元素）
     */
    DIALOG.adjustDialogPosition = function (dialogElement) {
        //  先try-catch，避免破坏代码结构
        try {
            //  如果此句不抛异常，说明该window有父窗口，且不会跨越
            window.frameElement.getBoundingClientRect();
        }
        catch (e) {
            //  抛异常可能是 没有父窗口、跨越、浏览器兼容。
            return;
        }
        
        /*
         * 完整公式为：
         * dialogElement.style.top =
         *     window.parent.scrollY
         *     - (window.frameElement.getBoundingClientRect().top + window.parent.scrollY)
         *     + (window.parent.innerHeight-dialogElement.offsetHeight) / 2
         * 解释：
         * ①window.parent.scrollY 为窗口滚动高度；
         * ②window.frameElement.getBoundingClientRect().top + window.parent.scrollY
         *  这一段是计算 iframe前面所有元素所占的高度（将这个计算放在本方法中，可以降低调用di-stub时的复杂度，方便用户）；
         * ③(window.parent.innerHeight-dialogElement.offsetHeight) / 2
         *  这一段是计算 弹出窗在可视范围内居中时 的top值。
         * 
         * 完整公式用中文表述，即为：弹窗的最终top值，就是 页面(iframe)实际卷轴高度值，加上 弹窗 距离可视范围顶部 的值
         */
        var win = window;
        //  实际滚动高度（扣除了 父窗口中iframe前面所有元素所占的高度）
        var actualScrollHeight = -win.frameElement.getBoundingClientRect().top;
        //  可视范围（即 弹窗居中时的相对范围）的高度
        var viewportHeight = UTIL.getViewHeight(win.parent);
        //  弹窗元素本身的高度
        var dialogElementHeight = dialogElement.offsetHeight;
        
        //  弹窗在可视范围内的相对top值（可能为负数）
        var viewportTop = (viewportHeight - dialogElementHeight) / 2;
        //  计算后的top值（使用Math.max保证 弹窗的最小定位top是滚动高度。当可视范围容纳不下整个大弹窗时，弹窗标题优先紧靠顶部）
        var styleTop = actualScrollHeight + Math.max(viewportTop, 0);
        
        //  当 可视范围容纳不下整个大弹窗，且父级页面滚动高度为0时，则可能会出现 弹窗顶部部分内容被截断 的情况。
        //      因此，需要保证 styleTop 不小于0，才能避免上述情况发生。
        dialogElement.style.top = Math.max(styleTop, 0) + 'px';
    };

    /**
     * 只含确定键的提示窗口
     *
     * @public
     * @param {String} text 提示信息
     * @param {Function} onconfirm 确定按钮的处理函数
     * @param {boolean} noBtn 是否不显示btn（不显示则禁止了一切页面的继续操作）
     */
    DIALOG.alert = function(text, onconfirm, noBtn) {
        DIALOG.showDialog(
            text, 
            '提示', 
            noBtn
                ? []
                : [
                    { 
                        text: '确定', 
                        className: 'ui-button-g', 
                        action: onconfirm 
                    }
                ], 
            DICT.DEFAULT_MASK_OPACITY
        );
    };

    /**
     * 含确定和取消键的窗口
     *
     * @public
     * @param {String} text 提示信息
     * @param {Function} ok 确定按钮的处理函数
     * @param {Function} cancel 取消按钮的处理函数
     */
    DIALOG.confirm = function(text, onconfirm, oncancel) {
        DIALOG.showDialog(
            text, 
            '确认', 
            [
                { 
                    text: '确定', 
                    className: 'ui-button-g', 
                    action: onconfirm 
                },
                { 
                    text: '取消',
                    className: 'ui-button-c',
                    action: oncancel 
                }
            ], 
            DICT.DEFAULT_MASK_OPACITY
        );
    };
    
    /**
     * 自定义键的窗口
     *
     * @public
     * @param {string} title 标题
     * @param {string} message 提示信息
     * @param {Array.<Object>} buttons 按钮，每项为：
     *          {string} text 按钮文字
     *          {string} className 样式文字
     *          {Function} action 点击的回调函数
     */
    DIALOG.dialog = function(title, message, buttons) {
        var html;
        buttons = buttons || [];
        
        html.push(
            '<div class="ui-messagebox-icon"></div>', 
            '<div class="ui-messagebox-content">',
                '<div class="ui-messagebox-text">', 
                    encodeHTML(message), 
                '</div>',
            '</div>'
        );

        DIALOG.showDialog(
            html.join(''), 
            title, 
            buttons, 
            DICT.DEFAULT_MASK_OPACITY
        );     
    };

    /**
     * 错误alert
     *
     * @public
     */
    DIALOG.errorAlert = function() {
        DIALOG.alert(LANG.ERROR);
    };

    /**
     * 遮罩层，防止二次点击
     * 如果启用，先判断body里面是否已经生成遮罩
     * 如果已经生成，就不做处理，如果没有生成，就生成一个
     * 如果禁用，就删除掉遮罩层
     * 其实，在body里面始终只存在一个遮罩层
     * 缺陷：创建删除dom操作，感觉不是很理想
     * 不过ajax请求不会很多，性能应该不会影响很大
     *
     * @private
     * @param {boolean} status 状态：启用还是禁用遮罩
     */
    DIALOG.mask = function (status) {
        var oLayerMasks = domQ('ui-reportSave-layerMask',
            document.body);
        var oLayerMask;

        // oLayerMasks为一个数组
        if (oLayerMasks.length === 1) {
            oLayerMask = oLayerMasks[0];
        }

        // 启用
        if (status) {
            // 如果 遮罩层不存在就创建一个
            // 这里用nodeType判断是否为element元素,实现不是很好
            if (!oLayerMask
                || (oLayerMask && !oLayerMask.nodeType)
            ) {
                oLayerMask = document.createElement('div');
                var maskCss = [
                    'background-color: #e3e3e3;',
                    'position: absolute;',
                    'z-index: 1000;',
                    'left: 0;',
                    'top: 0;',
                    'width: 100%;',
                    'height: 100%;',
                    'opacity: 0;',
                    'filter: alpha(opacity=0);',
                    '-moz-opacity: 0;'
                ].join('');
                oLayerMask.style.cssText = maskCss;
                oLayerMask.style.width = document.documentElement.scrollWidth + 'px';
                oLayerMask.className = 'ui-reportSave-layerMask';
                document.body.appendChild(oLayerMask);
            }
        }
        // 禁用
        else {
            if (oLayerMask && oLayerMask.nodeType) {
                document.body.removeChild(oLayerMask);
            }
        }
    };
})();