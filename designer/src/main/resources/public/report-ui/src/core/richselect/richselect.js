/**
 * callback 数据是当前选中三级分类的name的数组
 * 考虑到多次引用 所以options 修改为以对象为元素的数组
 * 窗体的宽度可自行设置
 **/
(function ($) {
    var defaults = {};
    var checkStatusCache = {};
    var textCache = {};
    var clickCheckbox = [];
    // 创建dom
    function createDom($ele, insText) {
        // 累计的添加
        var length = $('.sxwzbText').length;
        //$('<div class="sxwzb sxwzbText sxwzbText' + length + '" data-index=' + length + '>'
        //+ '<div class="sxwzbButton"><div></div></div>'
        //+ '</div><div class="sxwzb sxwzbContent sxwzbContent' + length + '" data-index=' + length + '></div>').appendTo($ele);
        // $('<div class="sxwzb sxwzbContent sxwzbContent' + length + '" data-index=' + length + '></div>').appendTo(document.body);

        var instruction;
        insText && (instruction = '<div class="f-l rich-select-instruction">' + insText + '</div>');

        var html = [
            '<div class="c-f">',
            insText ? instruction : '',
            '<div class="sxwzb sxwzbText f-l sxwzbText', length, '" data-index=', length, '>',
                '<div class="sxwzbButton">',
                    '<div></div>',
                '</div>',
            '<div class="sxwzb sxwzbContent f-l sxwzbContent', length, '" data-index=', length, '></div>',
            '</div></div>'
        ].join('');

        $(html).appendTo($ele);
        $currentContent = $('.sxwzbContent' + length);
        return $currentContent;
    }
    // 渲染窗体内部
    function renderCheckbox(data) {
        var index = $('.sxwzbText').length - 1;
        textCache[index] = [];
        if (!data) {
            alert("数据给的不对");
            return;
        }
        var htmlArray = [];
        for (var i = 0, l = data.length; i < l; i++) {
            // 构建大分类
            htmlArray.push('<div data-value="'+ data[i].name +'" class="sxwzbTitle">'+ data[i].caption +'</div>');

            var children = data[i].children;
            htmlArray.push('<div class="sxwzbBoxs">');
            if (!children){
                continue;
            }
            // 构建分类2
            for (var j = 0, le = children.length; j < le; j++) {
                var random = Math.floor(Math.random() * 100 + 1);
                var time = new Date().getTime() + '';
                var timer = time.slice(time.length - 4) + random;
                var idStr = children[j].name + timer;
                htmlArray.push('<div class="sxwzbType"><div class="sxwzbCategories">'
                    + '<input type="checkbox" id=' + idStr + ' class="sxwzbCheckBoxP sxwzbCheckBox">'
                    + '<label for='+ idStr + '>'
                    +  children[j].caption
                    + '</label>'
                    + '</div><div class="sxwzbSubdivisions">'
                );
                var grandson = children[j].children;
                if (!grandson) {
                    continue;
                }
                // 构建分类3
                for (var m = 0, n = grandson.length; m < n; m++) {
                    grandson[m].selected = grandson[m].selected === null ? '' :String(grandson[m].selected);
                    var selected = grandson[m].selected === 'false'? '': 'true';
                    checkStatusCache[grandson[m].name] = checkStatusCache[grandson[m].name] || selected;
                    var string = '';
                    if (selected) {
                        string = 'checked';
                        if (grandson[m].selected == '') {
                            string += ' disabled';
                        }
                    }
                    var idGName = grandson[m].name + timer;
                    htmlArray.push('<div class="sxwzbSubdivision">'
                        + '<input class="sxwzbCheckBoxC sxwzbCheckBox" data-name="' + grandson[m].name +'" type="checkbox" id=' + idGName + ' ' + string +'>'
                        + '<label for='+ idGName + '>'
                        +  grandson[m].caption
                        + '</label>'
                        + '</div>'
                    );
                    grandson[m].selected === "true" && textCache[index].push(grandson[m].caption);
                }
                htmlArray.push('</div></div>');
            }
            htmlArray.push('</div>');
        }
        htmlArray.push('<div class="sxwzbButtons"><span class="uiButton uiButton-ok">确定</span>'
        + '<span class="uiButton uiButton-circle">取消</span></div>');
        return htmlArray.join('');
    }
    // 文本框显示选中元素
    function renderInitStatus(i) {
        var text = [];
        var i = arguments[0] === undefined? $('.sxwzbText').length - 1: i;
        $.each($('.sxwzbContent' + i).find('input[type="checkbox"]:checked'), function(j, item){
            text.push($(item).siblings('label').text());
        });
        $('.sxwzbText' + (i) + ' .sxwzbButton div').text(text);
    }

    // 判断是否为全选
    function judgeCheckbox(index) {
        var index = arguments[0] === undefined? $('.sxwzbText').length - 1: index;
        $.each($('.sxwzbContent' + index).find('.sxwzbType'), function(i, item){
            var childNode = $(item).find('.sxwzbSubdivisions input[type="checkbox"]'),
                checkedItem = $(item).find('.sxwzbSubdivisions input[type="checkbox"]:checked');

            var checkStatus = ((childNode.length === checkedItem.length)
            && childNode.length > 0)? true: false;
            if (checkStatus) {
                var m = 0;
                $.each(childNode, function(j, child){
                    if ($(child).prop('disabled')) {
                        m ++;
                    }
                })

                if (m == childNode.length) {
                    $(item).find('.sxwzbCheckBoxP').prop('disabled', true);
                }
            }
            $(item).find('.sxwzbCheckBoxP').prop('checked', checkStatus);
        })
    }
    // 窗体的显示与隐藏
    function bindEvent(ele) {
        /**
         * 点击非sxwzb的内容的时候 隐藏窗体
         */
        $('body').unbind('click');
        $('body').bind('click', bodyClick);

        function bodyClick(e) {
            var target = $(e.target);
            if (!(target.parents('.sxwzb').length && !target.hasClass('sxwzb'))) {
                $('.sxwzbContent').hide();
            }
        }

        ele.delegate($('.sxwzbButton'), 'click', function(e) {
            var target = e.target;
            if (target == this || $(target).hasClass('disabled')) {
                return;
            }
            var index = $(target).parents('.sxwzbText').attr('data-index');
            index = index || $(target).attr('data-index');
            if (index === undefined) {
                return;
            }
            var $currentContent = $('.sxwzbContent' + index);
            var $currentText = $('.sxwzbText' + index);
            //var offset = $currentText.offset();
            //var top = offset.top;
            //var left = offset.left;
            //var bHeight = document.documentElement.clientHeight;
            //var bWidth = document.documentElement.clientWidth;
            //var contentWidth = $currentContent.outerWidth();
            //var contentHeight = $currentContent.outerHeight();
            //var spanWidth = $currentText.outerWidth();
            //var spanHeight = $currentText.outerHeight();
            //
            //left  = left + contentWidth <= bWidth? left: left - contentWidth + spanWidth;
            //
            //if (top + contentHeight <= bHeight || top - contentHeight < 0) {
            //    top = top + spanHeight;
            //} else {
            //    top = top - contentHeight;
            //}
            // top = top + contentHeight <= bHeight? top + spanHeight: top - contentHeight;
            //$currentContent.css({
            //    'left': left + 'px',
            //    'top': top + 'px'
            //})
            $('.sxwzbContent' + index).toggle();
            e.stopPropagation();
        });
    }

    function bindChekboxEvent($parent) {
        var options = defaults;
        /**
         * 每个input的点击事件
         * checkStatus当前checkbox选中状态
         * parentNode 当前父元素sxwzbType
         * childNode 二级分类
         * checkboxP 一级分类
         */
        $parent.delegate($('input[type="checkbox"]'), 'click', function(e) {
            var $self = $(e.target);
            var tag =/input/i;
            if (!tag.test($self[0].tagName)) {
                return;
            }
            // 当前checkbox选中状态
            var checkStatus = $self.prop('checked'),
                parentNode = $self.parents('.sxwzbType'),
                childNode = parentNode.find('.sxwzbSubdivisions input[type="checkbox"]'),
                checkboxP = parentNode.find('.sxwzbCheckBoxP'),
                checkedItem = parentNode.find('.sxwzbSubdivisions input[type="checkbox"]:checked');
            var index = $(this).attr('data-index');
            clickCheckbox[index] = clickCheckbox[index] || {};
            // 点击一级分类时选中所有二级分类
            if ($self.hasClass('sxwzbCheckBoxP')) {
                $.each(childNode, function(i, item) {
                    clickCheckbox[index][$(item).attr('data-name')] = $(item).prop('checked')? false: true;
                    if (!$(item).prop('disabled')) {
                        $(item).prop('checked', checkStatus);
                    }
                })
            } else {
                // 全部选中时，选中前面的大类
                var checkStatus1 = ((childNode.length === checkedItem.length)
                && childNode.length > 0)? true: false;
                checkboxP.prop('checked', checkStatus1);
                clickCheckbox[index][$self.attr('data-name')] = checkStatus;
            }
            e.stopPropagation();
        })
        /**
         * 点击确定 重新处理
         *
         */
        $parent.delegate($('.uiButton-ok'), 'click', function(e) {
            var $target = $(e.target);
            if (!$target.hasClass('uiButton-ok')) {
                return;
            }
            var index = $(this).attr('data-index');
            var $button = $('.sxwzbText' + index).find('.sxwzbButton');
            clickCheckbox[index] = clickCheckbox[index] || {};
            var sxwzbCheckBoxC = $('.sxwzbContent' + index).find('.sxwzbCheckBoxC');
            var idArray = [];
            $.each(sxwzbCheckBoxC, function(i, item) {
                if ($(item).prop('checked')) {
                    idArray.push($(item).attr('data-name'));
                }
            })
            // 重新设置文本框中为选中的元素
            renderInitStatus(Number(index));
            // callback
            if (options[index].clickCallback) {
                options[index].clickCallback(idArray.join(','));
            }
            // 重置clickCheckbox
            $.extend(checkStatusCache, clickCheckbox[index]);
            clickCheckbox[index] = {};
            $('.sxwzbContent' + index).hide();
            e.stopPropagation();
            // 关闭窗口
            // $button.trigger('click');
        })
        /**
         * 点击取消的时候 必须重置选中状态
         * 再重新判断是否为全选
         */
        $parent.delegate($('.uiButton-circle'), 'click', function(e) {
            var $target = $(e.target);
            if (!$target.hasClass('uiButton-circle')) {
                return;
            }
            var index = $(this).attr('data-index');
            var $button = $('.sxwzbText' + index).find('.sxwzbButton');
            clickCheckbox[index] = clickCheckbox[index] || {};

            for (var i in clickCheckbox[index]) {
                var status = clickCheckbox[index][i] ===true? false: true;
                // var status = $('.sxwzbContent' + index).find('#' + i).prop('checked') === true? false: true;
                $('.sxwzbContent' + index).find('#' + i).prop('checked', status);
                delete clickCheckbox[index][i];
            }
            $('.sxwzbContent' + index).hide();
            // $button.trigger('click');

            judgeCheckbox(index);
            e.stopPropagation();
        })
    }
    /**
     *   窗体定位
     */
    function setStyle($content) {
        // 绑定事件
        bindChekboxEvent($content);
    }
    $.fn.screenXingWeiZhiBiao = function (options) {
        var parent = this;
        bindEvent(parent);

        return parent.each(function(i, item){
            var length = $('.sxwzbText').length;
            if ($('.sxwzbContent' + length).length) {
                $('.sxwzbContent' + length).remove();
                delete defaults[length];
            }
            defaults[length] = options[i];
            var $content = createDom($(this), defaults[length].instructionText);
            if (options[i] && options[i].data && options[i].data.length) {
                // render数据
                $content.append(renderCheckbox(options[i].data));
                setStyle($content);
                judgeCheckbox();
                renderInitStatus();
            } else {
                $('.sxwzbText' + length).find('.sxwzbButton').addClass('disabled');
            }
        })
    }
})(jQuery);