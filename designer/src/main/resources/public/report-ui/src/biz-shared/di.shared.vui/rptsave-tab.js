/**
 * di.shared.vui.TabButton
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    文字区
 * @author:  lizhantong(lztlovely@126.com)
 * @depend:  xui, xutil
 */
    
$namespace('di.shared.vui');
    
(function () {
    
    
    //------------------------------------------
    // 引用 
    //------------------------------------------

    /* globals xutil */
    var inheritsObject = xutil.object.inheritsObject;
    var addClass = xutil.dom.addClass;
    var hasClass = xutil.dom.hasClass;
    var domChildren = xutil.dom.children;
    var getParent = xutil.dom.getParent;
    var encodeHTML = xutil.string.encodeHTML;
    var textLength = xutil.string.textLength;
    var textSubstr = xutil.string.textSubstr;
    /* globals di */
    var confirm = di.helper.Dialog.confirm;
    var XOBJECT = xui.XObject;
    

    //------------------------------------------
    // 类型声明 
    //------------------------------------------


    /**
     * 文字区
     * 直接指定can文字，或者html，
     * 或者模板（模板形式参见xutil.string.template）
     * 初始dom中的内容被认为是初始模板。
     * 也可以用参数传入模板。
     * 
     * @class
     * @extends xui.XView
     * @param {Object} options
     * @param {HTMLElement} options.el 容器元素
     */
    var TAB_BUTTON = $namespace().TabButton =
            inheritsObject(XOBJECT, constructor);
    var TAB_BUTTON_CLASS = TAB_BUTTON.prototype;


    //------------------------------------------
    // 常量 
    //------------------------------------------

    var TAB_CLASS = {
        // tab容器样式
        TAB_CLASS_NAME: 'ui-reportSave-tab',
        // tab里面li的一般样式
        NORMAL_TAB_CLASS_NAME: 'ui-reportSave-tab-tabNormal',
        // tab里面li的选中样式
        CURRENT_TAB_CLASS_NAME: 'ui-reportSave-tab-tabFocus',
        // tab里面li中spn选中样式
        TAB_TEXT_CLASS_NAME: 'ui-reportSave-tab-text',
        // tab里面li中a一般样式
        TAB_CLOSE_CLASS_NAME: 'ui-reportSave-tab-close',
        // tab里面li中a选中样式
        CURRENT_TAB_CLOSE_CLASS_NAME: 'ui-reportSave-tab-close-focus'
    };
    
    // 提示信息
    var MESSAGE = {
        // 默认tab不能编辑提示
        TAB_UPDATE_DEFAULT_WARN: '默认项不能编辑',
        // 删除二次提示
        TAB_DELETE_WARN: '您确定要删除此报表吗'
    };
    
    // 镜像名称最大显示长度（超过多少个就截断）
    var TAB_NAME_SHOW_LENGTH = 16;
    

    //------------------------------------------
    // 方法
    //------------------------------------------


    /**
     * 构造函数
     *
     * @private
     * @param {Object} options 参数
     * @param {Object} options.el 容器元素
     */
    function constructor(options) {
        var el = this._eMain = options.el;
        addClass(el, TAB_CLASS.TAB_CLASS_NAME);
    }
    
    /**
     * 初始化
     *
     * @public
     * @param {string} currentTabId 当前选中的tab的id
     * @param {function} preDeleteTabCallback 点击删除按钮会执行component中的事件
     * @param {Object} data 
     * @param {string} data.defaultImage
     * @param {Object} data.imageConfigs
     * @param {string} currentTabId 
     */
    TAB_BUTTON_CLASS.init = function (currentTabId, preDeleteTabCallback, reloadReportCallback, data) {
        var me = this;
        var el = this._eMain;
        var imgsData = data.reportImage;
        // 如果当前currentTabId为-1，说明是通过点击默认tab跳转过来,需要为默认添加高亮
        // 如果currentTabId为undefined，说明是第一次进来，
        // 并且后端没有返回默认，之前没有设置默认，需要为默认添加高亮
        var className = (!currentTabId || data.reportId === currentTabId)
            ? ' ' + TAB_CLASS.CURRENT_TAB_CLASS_NAME
            : '';
        var html = ['<ul>'];
        
        html.push(
            '<li class="', TAB_CLASS.NORMAL_TAB_CLASS_NAME, className, '"',
            ' imgid="', data.reportId, '" title="默认">',
                '<span class="',
                    TAB_CLASS.TAB_TEXT_CLASS_NAME, '"',
                '>默认</span>',
            '</li>'
        );
        
        for (var key in imgsData) {
            var imgName = imgsData[key];

            var liClassName = (key === currentTabId)
                ? (' ' + TAB_CLASS.CURRENT_TAB_CLASS_NAME)
                : '';
                
            var aClassName = (key === currentTabId)
                ? (' ' + TAB_CLASS.CURRENT_TAB_CLOSE_CLASS_NAME)
                : '';

            html.push(
               '<li class="', TAB_CLASS.NORMAL_TAB_CLASS_NAME, liClassName, '"',
                    'title="', encodeHTML(imgName), '"', ' imgid="', key, '">',
                    buildImageNameHtml(imgName),
                    '<a class="', TAB_CLASS.TAB_CLOSE_CLASS_NAME, aClassName,
                    '" href="javascript:void(0)">×</a>',
               '</li>'
            );
        }
        html.push('</ul>');
        el.innerHTML = html.join('');

        // 保存component中删除tab的callback
        this._preDeleteTabCallback = preDeleteTabCallback;
        this._reloadReportCallback = reloadReportCallback;

        // 保存ul的dom对象
        this._tabUl = domChildren(el)[0];
        this._tabUl.onclick = function (ev) {
            if (me._isInDesigner) {
                return;
            }
            var oEv = ev || window.event;
            tabClick.call(me,oEv);
        };
    };
    
    /**
     * 获取span标签，超过TAB_NAME_SHOW_LENGTH个就截断添加title
     * 
     * @private
     * @param {string} name 名称长度
     */
    function buildImageNameHtml(name) {
        var spanHTML = [
            '<span class="',
            TAB_CLASS.TAB_TEXT_CLASS_NAME,
            '" >',
            encodeHTML(name),
            '</span>'
        ];
        return spanHTML.join('');
    }
    
    /**
     * 禁用操作
     *
     * @protected
     */
    TAB_BUTTON_CLASS.disable = function () {
    };

    /**
     * 启用操作
     *
     * @protected
     */
    TAB_BUTTON_CLASS.enable = function () {
    };
    
    /**
     * 添加tab节点
     * 
     * @public
     * @param {string} id 需要预存的镜像id
     * @param {string} name 需要预存的镜像名称
     */
    TAB_BUTTON_CLASS.appendTab = function (id, name) {
        var tabUl = this._tabUl;
        // 创建li标签
        var oLi = document.createElement("li");
        oLi.title = encodeHTML(name);
        addClass(oLi, TAB_CLASS.NORMAL_TAB_CLASS_NAME);
        
        var html = [
            buildImageNameHtml(name),
            '<a class="', 
                TAB_CLASS.TAB_CLOSE_CLASS_NAME,
                '" href="javascript:void(0)">',
                '×',
            '</a>'
        ].join('');
        
        oLi.innerHTML = html;
        oLi.setAttribute("imgid", id);
        tabUl.appendChild(oLi);
    };
    
    /**
     * 更新当前报表镜像
     * 
     * @param {string} name 需要更新的镜像名称
     * @public
     */
    TAB_BUTTON_CLASS.updateCurrentTab = function (name) {
        var curentTab = this.getCurrentTab();
        var tabSpan = domChildren(curentTab)[0];
        var parentTab = getParent(tabSpan);
        if (tabSpan) {
            tabSpan.innerHTML = encodeHTML(name);
            parentTab.title = name;
        }
    };
     
    /**
     * 获取当前选中的tab的Element对象
     * 
     * @public
     * returns {HTMLElement} 当前选中的tab的Element对象
     */
    TAB_BUTTON_CLASS.getCurrentTab = function () {
        var tabUl =  this._tabUl;
        var oLis = domChildren(tabUl);
        
        for (var i = 0, len = oLis.length; i < len; i++) {
            if (hasClass(oLis[i], TAB_CLASS.CURRENT_TAB_CLASS_NAME)){
                 return oLis[i];
            }
        }
        return null;
    };
    
    /**
     * 获取当前选中的tab名字
     * 
     * @public
     * returns {string}  当前选中的tab名字
     * 
     */
    TAB_BUTTON_CLASS.getCurrentTabName = function () {
        var curentTab = this.getCurrentTab();
        var tabSpan = domChildren(curentTab)[0];
        
        if (tabSpan) {
            var title = tabSpan.getAttribute('title')
            return title ? title : tabSpan.innerHTML;
        }
        else {
            return '';
        }
    };

    /**
     * 获取所有tab名字
     *
     * @public
     * @return {Array}  所有tab名字
     *
     */
    TAB_BUTTON_CLASS.getAllTabName = function () {
        var tabUl = this._tabUl;
        var oLis = domChildren(tabUl);
        var tabNameArr = [];
        for (var i = 0, iLen = oLis.length; i < iLen; i ++) {
            tabNameArr.push(domChildren(oLis[i])[0].innerHTML);
        }
        return tabNameArr;
    };
    
    /**
     * 获取存在的tab个数
     * 
     * @public
     * return {string}  存在的tab的个数
     */
    TAB_BUTTON_CLASS.getTabsNums = function () {
        var tabUl = this._tabUl;
        var oLis = domChildren(tabUl);
        
        return oLis.length;
    };
    
    /**
     * 删除tab的dom对象
     * 如果删除的不是当前选中项，且删除成功，回调这个事件
     * 
     * 删除按钮事件，在init中绑定了component中的删除请求函数
     * 点击删除时，触发component中的删除请求                 
     * component中的请求函数是getHandleDeleteImage中返回的的匿名函数
     * 
     * 调用时请保证this指向vui-tab
     * 
     * @private
     * @param {string} imgId 当前删除的镜像id
     */
    function deleteTabCallBack(imgId) {
        var tabUl =  this._tabUl;
        var oLis = domChildren(tabUl);
        
        for (var i = 0, len = oLis.length; i < len; i++) {
            
            if (oLis[i].getAttribute('imgid') == imgId) {
                 tabUl.removeChild(oLis[i]);
                 
                 break;
            }
        }
    }

    
    function getPrevTabId(tabUl, imgId) {
        var oLis = domChildren(tabUl);
        var prevImgId;
        
        for (var i = 0, len = oLis.length; i < len; i++) {
            
            if (oLis[i].getAttribute('imgid') == imgId) {
                var prevTab = oLis[i].previousSibling 
                              || oLis[i].previousElementSibling;
                 prevImgId = prevTab.getAttribute('imgid');
                 
                 break;
            }
        }
        
        return prevImgId;
    }
     
    /**
     * tab点击事件
     * 
     * @private
     * @param {Event} ev 
     */
    function tabClick(ev) {
        var target = ev.target || ev.srcElement;
        var me = this;
        var imgId;
        // 如果是关闭按钮
        if (hasClass(target, TAB_CLASS.TAB_CLOSE_CLASS_NAME)) {
            ev.stopPropagation ? ev.stopPropagation() 
                               : (ev.cancelBubble = true);
            
            // 删除二次确认
            confirm(MESSAGE.TAB_DELETE_WARN, function () {
                var oLi = getParent(target);
                imgId = oLi.getAttribute('imgid');
                //XXX 获取tab名称时,如果title存在，说明是截断后的
                // 就需要获取title的内容为名称
                var tabSpan = oLi.children[0];
                var imgName = tabSpan.getAttribute('title');
                imgName ? imgName : tabSpan.innerHTML;
                //删除时，需要传 上一个imgId
                var prevImgId = getPrevTabId(me._tabUl, imgId);
                
                if (hasClass(oLi, TAB_CLASS.CURRENT_TAB_CLASS_NAME)) {
                    /**
                     * preDeleteTabCallback就是component中的事件
                     * 事件为getHandleDeleteImage中返回的匿名函数
                     */
                    me._preDeleteTabCallback(
                        imgId,
                        imgName,
                        prevImgId
                    );
                }
                else {
                    me._preDeleteTabCallback(
                        imgId,
                        imgName,
                        prevImgId,
                        deleteTabCallBack
                    );
                }
            }); 
        }
        // 如果是li
        else if (hasClass(target, TAB_CLASS.NORMAL_TAB_CLASS_NAME)) {
            // 如果点击的是当前选中项，返回，不刷新
            if (hasClass(target, TAB_CLASS.CURRENT_TAB_CLASS_NAME)) {
                return;
            }
            
            imgId = target.getAttribute('imgid');
            me._reloadReportCallback(imgId);
        }
        // 如果是span
        else if (hasClass(target, TAB_CLASS.TAB_TEXT_CLASS_NAME)) {
            var oLi = getParent(target);
            // 如果点击的是当前选中项，返回，不刷新
            if (hasClass(oLi, TAB_CLASS.CURRENT_TAB_CLASS_NAME)) {
                return;
            }
            
            imgId = oLi.getAttribute('imgid');
            me._reloadReportCallback(imgId);
        }
    }
    
})();