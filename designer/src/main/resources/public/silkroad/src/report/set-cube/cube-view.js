/**
 * @file 报表cube设置的view
 * @author 赵晓强(longze_xq@163.com)
 * @date 2014-7-17
 */
define([
        'template',
        'dialog',
        'report/set-cube/cube-model',
        'report/set-cube/main-template',
        'report/set-cube/cube-list-template'
    ],
    function (
        template,
        dialog,
        CubeModel,
        mainTemplate,
        cubeListTemplate
    ) {

        return Backbone.View.extend({

            /**
             * 事件绑定
             */
            events: {
                'click .j-root-data-sources-list .j-item': 'loadFactTableList',
                'click .j-con-cube-list .j-item': 'selectCubes',
                'click .j-root-set-group .j-set-group': 'addFormLine',
                'click .j-root-set-group .j-delete': 'deleteFormLine',
                'click .j-create-data-sources-link': 'enterCreateDataSources',
                'click .j-submit': 'submit',
                'click .j-cancel': 'cancel',
                'change .j-select-area-date': 'changeDateArea'
            },

            /**
             * 构造函数
             * @param {$HTMLElement} option.el view的顶层DOM：.j-main
             * @param {string} option.id 报表id
             * @constructor
             */
            initialize: function (option) {
                var that = this;
                this.model = new CubeModel({id: this.id});

                this.listenTo(
                    this.model,
                    'change:activeDataSourcesList',
                    function (model, data) {
                        var html = mainTemplate.render({dataSourcesList: data});
                        that.$el.html(html);
                    }
                );

                this.listenTo(
                    this.model,
                    'change:factTableList',
                    function (model, data) {
                        var separateTableRuleData = that.model.get('separateTableRuleData');
                        data.separateTableRuleData = separateTableRuleData;
                        // 其中包括factTables、prefixs两项数据
                        var html = cubeListTemplate.render(data);
                        that.$el.find('.j-con-cube-list').html(html);
                    }
                );

                this.listenTo(
                    this.model,
                    'change:selectedTable',
                    function (model, data) {
                        // 获取分表规则中所有表格下拉框
                        var selector = '.j-root-set-group .j-select-table';
                        var $selectedTablesSels = this.$el.find(selector);
                        if (!$selectedTablesSels.length) {
                            return;
                        }
                        // 获取选中表数据，在分表规则中，第一个下拉框中填充
                        var html = [];
                        $.each(data, function() {
                            html.push(
                                '<option value="', this.id, '">',
                                    this.text,
                                '</option>'
                            );
                        });

                        // 重置所有选择表下拉框中的内容
                        $selectedTablesSels.each(function () {
                            $(this).html(html.join(''));
                        });
                    }
                );

                if (option.edit === true) {
                    that.model.loadSelectedDataSources(function (groupId) {
                        that.model.dataSourcesModel.loadDsGroupActive(
                            function() {
                                // 由于只有在编辑状态下 且 刚进来时需要还原，所以在这里用参数的方式来还原数据
                                that.model.loadFactTableList(groupId, true);
                            }
                        );
                    });
                }
                else {
                    that.model.dataSourcesModel.loadDsGroupActive();
                }

                window.dataInsight.main = this;
            },

            /**
             * 加载实体表（附带选中被点击数据源的功能）
             * @param {event} event 点击左边数据源产生的事件
             * @public
             */
            loadFactTableList: function (event) {
                var $target = $(event.target);
                var dsId = $target.attr('data-id');
                var selector = '.j-root-data-sources-list .j-item.selected';
                this.$el.find(selector).removeClass('selected');
                $target.addClass('selected');

                this.model.selectedDsId = dsId;
                var groupId = $('span[data-id=' + dsId + ']').attr('group-id');
                this.model.loadFactTableList(groupId);
            },

            /**
             * 点击选中和取消选中cube操作
             * @param {event} event 点击事件
             * @public
             */
            selectCubes: function (event) {
                var $target = $(event.target);
                var $selectedTables;
                var table = [];

                if ($target.hasClass('selected')) {
                    $target.removeClass('selected');
                }
                else {
                    $target.addClass('selected');
                }

                $('.j-root-set-group .j-regexps-validate').text('').hide();
                $selectedTables = this.$el.find('.j-con-cube-list .j-item.selected');
                $selectedTables.each(function () {
                    var id = $(this).attr('data-id');
                    var text = $(this).text();
                    table.push({id: id, text: text});
                });
                this.model.set('selectedTable', table);
            },

            /**
             * 点击“添加分表匹配规则”添加一规则输入行
             *
             * @param {event} event 点击事件
             * @public
             */
            addFormLine: function (event) {
                var $target = $(event.target);
                var selector = '.j-root-set-group-template';
                var $formLine = this.$el.find(selector).clone();

                // 获取选中表数据
                selector = '.j-con-cube-list .j-item.selected';
                var $selectedTables = this.$el.find(selector);

                // 获取分表规则中所有表格下拉框
                selector = '.j-root-set-group .j-select-table';
                var $selectedTablesSels = this.$el.find(selector);

                if (!$selectedTables.length) {
                    $('.j-root-set-group .j-regexps-validate')
                        .text('提示：请先选择表格').show();
                    return;
                }
                else if ($selectedTables.length <= $selectedTablesSels.length) {
                    $('.j-root-set-group .j-regexps-validate')
                        .text('提示：不能再添加规则').show();
                    return;
                }

                // 获取选中表数据，在分表规则中，第一个下拉框中填充
                var html = [];
                $selectedTables.each(function () {
                    html.push(
                        '<option value="', $(this).attr('data-id'), '">',
                            $(this).text(),
                        '</option>'
                    );
                });
                $formLine.find('.j-select-table').html(
                    html.join('')
                );
                var $dom = $formLine.removeClass('hide j-root-set-group-template');
                $('.j-regexps-validate').before($dom.addClass('j-regexps-item'));
            },

            /**
             * 删除一规则输入行
             * @param {event} event 点击事件
             * @public
             */
            deleteFormLine: function (event) {
                var $target = $(event.target);
                $target.parents('.j-regexps-item').remove();
            },

            /**
             * 当没有数据源时提供便捷入口 — 直接添加数据源
             * @public
             */
            enterCreateDataSources: function () {
                // 进数据源添加/编辑模块
                require(
                    [
                        'data-sources/create-view'
                    ],
                    function (DataSourcesCreateView) {
                        new DataSourcesCreateView({
                            el: $('.j-main'),
                            isAdd: true
                        });
                    }
                );
            },

            /**
             * 分表规则中，地域时间先啦狂改变事件
             *
             * @param {event} event 点击事件
             * @public
             */
            changeDateArea: function (event) {
                var $target = $(event.target);
                var val = $target.val().toLowerCase();
                var html = [];
                var ruleData = this.model.get('separateTableRuleData');
                var areaDateDatas = ruleData[val].children;

                for (var i = 0, iLen = areaDateDatas.length; i < iLen; i++ ) {
                    html.push(
                        '<option value="', areaDateDatas[i].value, '">',
                            areaDateDatas[i].text,
                        '</option>'
                    );
                }
                $target.next('select').html(html.join(''));
            },

            /**
             * 提交cube设置
             * @public
             */
            submit: function () {
                var that = this;
                var selector = '.j-con-cube-list .j-item.selected';
                var $selectedTables = this.$el.find(selector);
                var $regexps = this.$el.find('.j-root-set-group .j-regexps-item');
                var data = {};

                if ($selectedTables.length == 0) {
                    dialog.error('至少需要选择一个实体表作为cube');
                    return;
                }

                selector = '.j-root-data-sources-list .j-item.selected';
                data.dataSourceId = this.$el.find(selector).attr('group-id');

                data.selectedTables = [];
                $selectedTables.each(function () {
                    data.selectedTables.push($(this).text());
                });
                data.selectedTables = data.selectedTables.join(',');

                // 获取分表规则data
                var regexps = {};
                var noRepeat = {}; // 用来做去重容器
                var noRepeatFlag = false;
                $regexps.each(function () {
                    var selects = $(this).find('select');
                    var prefix = $(this).find('input').val().trim();
                    var tableName = $(selects[0]).val();
                    var type = $(selects[1]).val();
                    var condition = $(selects[2]).val();
                    // 去重校验
                    if (noRepeat[tableName]) {
                        noRepeatFlag = true;
                        return;
                    }
                    else {
                        noRepeat[tableName] = true;
                    }
                    regexps[tableName] = {
                        type: type,
                        prefix: prefix,
                        condition: condition
                    }
                });
                if (noRepeatFlag) {
                    $('.j-root-set-group .j-regexps-validate')
                        .text('提示：表格不能重复')
                        .show();
                    return;
                }
                data.regexps = JSON.stringify(regexps);
                this.model.submit(data, function () {
                    // 提交成功
                    window.dataInsight.main.destroy();

                    require(['report/dim-set/view'], function (DimSetView) {
                        new DimSetView({
                            el: $('.j-main'),
                            id: that.id
                        });
                    });
                });
            },

            /**
             * 取消当前操作，跳转到报表列表页面
             * @public
             */
            cancel: function () {
                window.dataInsight.main.destroy();
                require(['report/list/main-view'], function (ReportListView) {
                    new ReportListView({el: $('.j-main')});
                });
            },

            /**
             * 销毁当前view
             * @public
             */
            destroy: function () {
                // 销毁 model
                this.model.clear({silent: true});
                // 停止监听model事件
                this.stopListening();
                // 解绑jq事件
                this.$el.unbind().empty();
            }
        });
    }
);