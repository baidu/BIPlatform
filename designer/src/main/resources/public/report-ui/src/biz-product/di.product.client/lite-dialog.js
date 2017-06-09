/**
 * LiteDialog for di-stub
 * Copyright 2012 Baidu Inc. All rights reserved.
 * 
 * @file:    全局的提示信息 
 *           注意！！此文件是嵌入到客户系统，
 *           希望客户系统不使用http缓存此文件（从而方便报表引擎对此文件的升级更新），
 *           这就需要这个文件注意写法，能在压缩后足够小。
 * @author:  sushuang(sushuang)
 * @depends: ecui
 */

(function (NS) {
    
    var LiteDialog = NS.LiteDialog = {};

    var removeDom = NS.removeDom;
    var getScrollLeft = NS.getScrollLeft;
    var getViewWidth = NS.getViewWidth; 

    var ePrompt;
    var promptTimer;
    var win;

    /**
     * 设置环境
     *
     * @public
     */
    LiteDialog.setEnv = function (options) {
        if (win != options.win) {
            LiteDialog.dispose();
            win = options.win;
        }
    };

    /**
     * 设置环境
     *
     * @public
     */
    LiteDialog.dispose = function () {
        ePrompt && removeDom(ePrompt);
        ePrompt = null;
        win = null;
    };

    function createEl() {
        if(!ePrompt) {
            var stl = [
                'padding: 5px 10px;',
                'color: #333;',
                'background: #F9EDBE;',
                'font-family: "微软雅黑", Serif;',
                'font-size: 14px;',
                'font-weight: bold;',
                'border: 1px solid #F0C36D;',
                'border-radius: 4px;',
                '-moz-border-radius: 4px;',
                '-webkit-border-radius: 4px;',
                '-o-border-radius: 4px;',
                '-ms-border-radius: 4px;',
                'box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);',
                '-moz-box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);',
                '-webkit-box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);',
                '-o-box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);',
                '-ms-box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);'
            ].join('');

            var doc = win.document;

            ePrompt = doc.createElement('div');
            ePrompt.style.cssText = 'display:none;position:fixed;*position:absolute;' + stl;
            doc.body.appendChild(ePrompt);
        }
    }

    /**
     * 信息提示，支持自动消失
     *
     * @public
     * @param {string} text 信息
     * @param {boolean} mask 是否使用遮罩（不支持）
     * @param {number} timeout 消失时间
     */
    LiteDialog.prompt = function (text, mask, timeout) {
        var x = getScrollLeft(win) + getViewWidth(win) / 2;
        var y = 5;

        // FIXME 
        // 临时方案
        clearPromptTimer();

        createEl();

        if (ePrompt.style.display == '') {
            return false;
        }

        ePrompt.innerHTML = text;
        ePrompt.style.display = '';
        ePrompt.style.left = x - ePrompt.offsetWidth / 2 + 'px';
        ePrompt.style.top = y + 'px';

        if (timeout) {
            promptTimer = setTimeout(
                function () {
                    LiteDialog.hidePrompt();
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
    LiteDialog.waitingPrompt = function (text) {
        if (text == null) {
            text = '请稍候 ...';
        }

        var waitingTxtStl = [
            'height: 24px; ',
            'line-height: 24px;',
            'margin: 0 5px 0 5px;',
            'font-family: "微软雅黑", Serif;',
            'font-size: 14px;',
            'font-weight: bold;'
        ].join('');

        text = '<div style="' + waitingTxtStl + '">' + text + '</div>';

        LiteDialog.prompt(text);
    }
    
    /**
     * 隐藏信息提示
     *
     * @public
     * @param {string} messag 信息
     * @param {boolean} 是否使用遮罩
     * @param {number} timeout 消失时间
     */
    LiteDialog.hidePrompt = function () {
        clearPromptTimer();
        ePrompt.style.display = 'none';
    };

    function clearPromptTimer() {
        if (promptTimer) {
            clearTimeout(promptTimer);
            promptTimer = null;
        }
    }

})(window.$DataInsight$);