define(['ztree-core', 'ztree-check'],function () {

    /**
     * 下拉树构造函数
     *
     * @param {Object} option 设置项
     * @param {$HTMLElement} option.el 模块容器
     * @param {Object} option.async 异步设置项
     *
     * @constructor
     */
    var SelectTree = function (option) {
        var defaultOptions = {
            check: {
                enable: true,
                // 父与子的关联
                chkboxType: {
                    'Y': 's',
                    'N': ''
                }
            },
            data: {
                simpleData: {
                    enable: true
                }
            }
        };

        this.el = option.el;
        option.okCallback && (this.okCallback = option.okCallback);
        option.async && (defaultOptions.async = option.async);
        this.option = defaultOptions;
        this.render();
    };

    /**
     * 渲染下拉树
     *
     * @public
     */
    SelectTree.prototype.render = function () {
        var random = Math.floor(Math.random() * 100 + 1);
        var time = new Date().getTime() + '';
        var timer = time.slice(time.length - 4) + random;
        var treeBoxId = 'select-tree-' + timer;

        var html = [
            '<span class="select-tree-input"></span>',
            '<div class="select-tree-box">',
                '<ul class="select-tree-content ztree" id="', treeBoxId, '">',
                '</ul>',
                '<div class="select-tree-foot">',
                    '<span type="button" class="select-tree-foot-btn select-tree-foot-ok j-tree-ok">',
                        '完成',
                    '</span>',
                    '<span type="button" class="select-tree-foot-btn select-tree-foot-cancel j-tree-cancel">',
                        '取消',
                    '</span>',
                '</div>',
            '</div>'
        ];
        $(this.el).html(html.join(''));
        $(this.el).addClass('select-tree').css('position', 'relative');
        this.treeId = treeBoxId;

        var treeName = $(this.el).attr('data-tree-select-name');
        if (treeName) {
            $(this.el).find('.select-tree-input').html(treeName);
        }
        this.bindEvent();

        $.fn.zTree.init($('#' + treeBoxId), this.option);
        this.treeObj = $.fn.zTree.getZTreeObj(this.treeId);
    };


    /**
     * 绑定下拉树事件
     *
     * @public
     */
    SelectTree.prototype.bindEvent = function () {
        var that = this;
        var $selInput = $('.select-tree-input', that.el);
        $selInput.unbind();
        $selInput.click(function () {
            $('.select-tree-box', that.el).slideDown();
        });

        $('.j-tree-ok', that.el).unbind();
        $('.j-tree-ok', that.el).click(function() {
            var selectedNodes = that.treeObj.getCheckedNodes();
            var selectedIds = [];
            var selectedNames = [];

            for (var key in selectedNodes) {
                selectedIds.push(selectedNodes[key].id);
                selectedNames.push(selectedNodes[key].name);
            }
            selectedIds = selectedIds.join(',');
            selectedNames = selectedNames.join(',');

            $('.select-tree-box', that.el).slideUp();
            $(that.el).attr('data-tree-select-id', selectedIds);
            $(that.el).attr('data-tree-select-name', selectedNames);
            $(that.el).find('.select-tree-input').html(selectedNames);

            that.okCallback && that.okCallback(selectedIds);
        });

        $('.j-tree-cancel', that.el).unbind();
        $('.j-tree-cancel', that.el).click(function() {
            $('.select-tree-box', that.el).slideUp();
        });
    };

    return SelectTree;
});

