/**
 * @file:    浮窗公共组件
 * 测试文件：test/demo/float-window.htm
 * @author:  lizhantong(lztlovely@126.com)
 */   
define(function () {
    
    /**
     * 构造函数
     *
     * @param {Object} options 参数
     * @param {Object} options.content 浮窗内容的html片段
     * @public
     */
    var FloatWindow = function (options) {
        var me = this;
        var defaultOptions = {
            direction: 'align', // align:水平方向; vertical: 垂直方向
            html: ''
        };
        $.extend(defaultOptions, options);
        this.options = defaultOptions;
        this.render();
        bindEvents(me.el);      
    };

    /**
     * 渲染
     *
     * @public
     */
    FloatWindow.prototype.render = function () {
        var options = this.options;
        var $el = $('<div></div>');
        this.el = $el[0];
        var cssText = {
            'position': 'absolute',
            'z-index': '999'
        };

        $el.css(cssText);
        $el.appendTo($(document.body));
        $el.append(options.content);
        $el.hide();
    };

    /**
     * 显示浮窗
     *
     * @public
     * @param {HTMLElement} target Element 触发点击事件的dom元素
     */
    FloatWindow.prototype.show = function (target) {
        // TODO:目前支持左右下拉框，扩展上下下拉框
        var me = this;
        var options = me.options;
        var $el = $(me.el);
        var elWidth = $el.outerWidth();
        var elHeight = $el.outerHeight();
        // 触发者的宽度、高度、距顶部距离、距左侧距离
        var topTarget = $(target).offset().top;
        var leftTarget = $(target).offset().left;
        var targetHeight = parseInt($(target).outerHeight());
        var targetWidth = parseInt($(target).outerWidth());
        
        // body的宽、高
        // document的宽度 = $(document.body).outerWidth()
        // + $(document.body).scrollLeft()
        var $body = $(document);
        var bodyWidth = $body.outerWidth();
        var bodyHeight = $body.outerHeight();
        
        // 触发者的左距 + 触发者宽度
        var w = leftTarget + targetWidth;
        // 触发者的上距 + 触发者高度
        var h = topTarget + targetHeight;

        // 水平对齐
        if (options.direction === 'align') {
            // 如果     body的距离  -  (触发者的左间距 +宽度) < 浮窗的宽度 ，
            // 那么，就在触发者左边显示
            // left值为 ：触发者的左距 - 浮窗的宽度
            if ((bodyWidth - w) < elWidth) {

                // 如果 body高度 - 触发者的top < 浮窗高度 ，那么，就在触发者上面显示
                // top值为：触发者的上距 + 触发者高度 -触发者高度
                if ((bodyHeight - topTarget) < elHeight) {
                    $el.css({
                        'top': (h - elHeight) + 'px',
                        'left': (leftTarget - elWidth) + 'px'
                    }).show();
                }
                // 如果在下面显示 ,top值为：触发者top
                else {
                    $el.css({
                        'top': topTarget + 'px',
                        'left': (leftTarget - elWidth) + 'px'
                    }).show();
                }

            }
            // 如果在右边显示， left值为 ：触发者的左距 + 浮窗宽度
            else {
                // body的距离  -  (触发者的上间距 ) < 浮窗的高度 ，那么，就在上边显示
                // top值为：触发者的上距 + 触发者高度 -触发者高度
                if ((bodyHeight - topTarget) < elHeight) {

                    $el.css({
                        'top': (h - elHeight) + 'px',
                        'left': w + 'px'
                    }).show();
                }
                // 如果在下面显示 ,top值为：触发者top
                else {
                    $el.css({
                        'top': topTarget + 'px',
                        'left': w + 'px'
                    }).show();
                }

            }
        }
        // 垂直对齐
        else {
            $el.css({
                'top': h + 'px',
                'left': leftTarget + 'px'
            }).show();
        }

    };

    FloatWindow.prototype.showAlign = function () {

    };
    /**
     * 隐藏浮窗
     *
     * @public
     */
    FloatWindow.prototype.hide = function () {
        var me = this;
        var $el = $(me.el);
        $el.hide();
    };

    /**
     * 重绘浮窗里面的内容
     *
     * @public
     * @param {string} content  html片段
     */
    FloatWindow.prototype.redraw = function (content) {
        var me = this;
        var $el = $(me.el);
        $el.html(content);
    };
    
    /**
     * 绑定事件
     * 绑定浮窗事件，与外界无关的事件
     * 目前只是绑定html的mousedown事件
     * 
     * @private
     */
    function bindEvents(el) {
        var $el = $(el);
        // 为浮窗容器添加点击事件
        // （一般是不需要的，使用层会根据提供的隐藏接口，自己去隐藏）
        // 容器中的选项会在外面绑定点击事件,触发用户绑定的点击事件,会先执行完；
        // 之后冒泡当前绑定的事件，进行窗口隐藏
        $el.click(function (event) {
            //el.hide();
        });
        
        // 为html绑定mousedown事件
        // 当触发mousedown事件时，会先判断触发者是否是浮窗中的元素
        // 如果不是，就隐藏浮窗；反之，不处理
        // 注意：如果为body绑定这个事件，会有一些隐患，比如说：没有清margin...
        // 用户点击按钮机制：先mousedown，隐藏掉浮窗，然后再执行click事件，显示浮窗
        var $html = $('html');
        $html.mousedown(function (e) {
            if (!$.contains(el, e.target)) {
                $el.hide();
            }
        });
    };

    return FloatWindow;
});