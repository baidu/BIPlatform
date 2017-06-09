/**
 * @file:    数据源新建模块View
 * @author:  lizhantong(lztlovely@126.com)
 * @depend:  lzt/create-template
 */
define([
        'data-sources/create-template',
        'data-sources/create-model'
    ],
    function (template, Model) {

        /**
         * 数据源新建模块视图类
         *
         * @class
         */
        return Backbone.View.extend({

            //------------------------------------------
            // 公共方法区域
            //------------------------------------------

            /**
             * 事件绑定
             */
            events: {
                'click .j-extend-line-link': 'extendLine',
                'click .j-add-address': '_addReserveAddress',
                'click .j-delete-address': '_deleteInputInfo',
                'click .j-button-submit': '_submit',
                'click .j-button-cancel': '_cancel',
                'focus .j-input-password': '_clearPassword',
                'blur .j-input-password': '_revertPassword'
            },

            /**
             * 构造函数
             *
             * @constructor
             */
            initialize: function (option) {
                var that = this;
                var modelData = {};

                if (option.id !== undefined) {
                    modelData.id = option.id;
                }
                if (option.groupId !== undefined) {
                    modelData.groupId = option.groupId;
                }

                that.model = new Model(modelData);
                that.model.set({
                    isAdd: option.isAdd,
                    isEncrypt: true
                });

                // 添加监听
                that.listenTo(
                    that.model,
                    'change:dbData',
                    function (model, data) {
                        var html = template.render(data);
                        that.$el.html(html);
                        var $groupItem = $('.j-data-sources-info-group-name');
                        var groupData = model.get('groupData');
                        var selHtml = [];

                        if (model.get('isAdd')) {
                            for (var i = 0, iLen = groupData.length; i < iLen; i ++ ) {
                                selHtml.push(
                                    '<option value="'
                                    + groupData[i].id
                                    + '">'
                                    + groupData[i].name
                                    + '</option>'
                                );
                            }
                            $groupItem.find('select').append(selHtml.join(''));
                        }
                    }
                );

                this.model.getInitData();
                window.dataInsight.main = this;
            },

            /**
             * 扩展行（高级选项数据行）的显隐
             *
             * @public
             */
            extendLine: function (event) {
                var $arrow = $(event.target).find('.j-icon-arrow');
                var $extendLine = this.$el.find('.j-extend-line');

                if ($extendLine.hasClass('hide')) {
                    $extendLine.removeClass('hide');
                    $arrow.removeClass('icon-arrow-down').addClass('icon-arrow-up');
                }
                else {
                    $extendLine.addClass('hide');
                    $arrow.removeClass('icon-arrow-up').addClass('icon-arrow-down');
                }
            },

            /**
             * 获取form中的数据，并将其转换为对象，同时做数据格式检验
             * （通过name来做）（如果验证失败返回false）
             *
             * @public
             * @return {bool|Object} data|validateResult
             */
            getFormData: function () {
                // 有name的表单项
                var $formDataItem = this.$el.find('[name]');
                var data = {};
                var validateResult = true;
                var num255 = '25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]';
                var hostAndPortRule = ''
                    + '('
                    // ip:端口 模式
                    + '^'
                    // 开头是http，https 加 "://" 或者没有
                    + '((https?):\\/\\/)?'
                    + '(' + num255 + ')'
                    + '\\.'
                    + '(' + num255 + '|0)'
                    + '\\.'
                    + '(' + num255 + '|0)'
                    + '\\.'
                    + '(' + num255 + ')'
                    + '(:\\d{0,5})?$'
                    + ')|('
                    // 域名模式
                    + '^('
                          // 开头是http，https 加 "://" 或者没有
                    +     '((https?):\\/\\/)?'
                          // 以字母开头，后面a-z,0-9,还有 中划线（0到多个）
                    +     '[a-z0-9]([a-z0-9\\-]*[\\.])+'
                    + ')'
                    // [a-z]{2} 一种偷懒的写法，包括cn（中国），jp（日本）等，实在太多写不过来
                    + '([a-z]{2}|aero|arpa|biz|com|coop|edu|gov|info|int|jobs|mil|museum|name|nato|net|org|pro|travel)$'
                    + ')';

                // 验证规则
                var validateConfig = {
                    'name': /.+/,
//                    数据库地址
//                    TODO:修改为更全更合理的校验
//                    'hostAndPort': new RegExp(hostAndPortRule),
                    'dbInstance': /.+/,
                    'dbUser': /.+/,
                    'dbPwd': /.+/,
                    'dbUser': /.+/
                };

                // 进行验证，并显隐失败信息
                $formDataItem.each(function () {
                    var $item = $(this);
                    var $validateMessage = $item.next();
                    var attrName = $item.attr('name');
                    var validateRule = validateConfig[attrName];

                    if (attrName === 'groupId' && $item.is('input')) {
                        data[attrName] = $item.attr('group-id');
                    }
                    else {
                        data[attrName] = $item.val();
                    }

                    if (validateRule && validateRule.constructor == RegExp) {
                        if (validateRule.test(data[attrName])) {
                            $validateMessage.addClass('hide');
                        }
                        // 验证失败
                        else {
                            validateResult = false;
                            $validateMessage.removeClass('hide');
                        }
                    }

                    // 后续支持备库地址需要 改进代码
                });

                var advancedItem = this.$el.find('.j-advanced-properties').find('.j-item');
                var advancedProperties = {};
                advancedItem.each(function() {
                    var $key = $(this).find('.j-item-key').val();
                    var $value = $(this).find('.j-item-value').val();
                    if ($.trim($key) && $.trim($value)) {
                        advancedProperties[$key] = $value;
                    }
                });
                data.advancedProperties =JSON.stringify(advancedProperties) ;
                return validateResult ? data : validateResult;
            },

            /**
             * 销毁新建数据源模块
             *
             * @public
             */
            destroy: function () {
                // 销毁 model
                this.model.clear({silent: true});
                // 停止监听model事件
                this.stopListening();
                // 解绑jq事件
                this.$el.unbind().empty();
            },

            //------------------------------------------
            // 私有方法区域
            //------------------------------------------

            /**
             * 如果是更新数据源，当密码输入框聚焦时，清空输入框
             *
             * @private
             */
            _clearPassword: function () {
                var isAdd = this.model.get('isAdd');

                if (!isAdd) {
                    $('.j-input-password').val('');
                }
            },

            /**
             * 如果是更新数据源，当密码输入框失去焦点时，
             * 如果用户没有输入其他有效密码，就恢复后端传来的密码
             *
             * @private
             */
            _revertPassword: function () {
                var model = this.model;
                var isAdd = this.model.get('isAdd');

                if (!isAdd) {
                    var $el = $('.j-input-password');
                    var val = $.trim($el.val());
                    if (val == '') {
                        $el.val(this.model.get('dbData').dbPwd);
                        model.set({
                            isEncrypt: true
                        });
                    }
                    else {
                        model.set({
                            isEncrypt: false
                        });
                    }
                }
                else {
                    model.set({
                        isEncrypt: false
                    });
                }
            },

            /**
             * 添加一个备选数据库地址
             *
             * @private
             */
            _addReserveAddress: function () {
                var selector = '.j-data-sources-part .j-datasource-reserveAddress-moudle';

                $('.j-datasource-database-box')
                    .before($(selector).clone(true));
            },

            /**
             * 删除备选数据库输入框
             *
             * @param {event} event 事件
             * @private
             */
            _deleteInputInfo: function (event) {
                var $target = $(event.target);
                $target.parent().parent().remove();
            },

            /**
             * 提交
             *
             * @private
             */
            _submit: function () {
                var that = this;
                var data = that.getFormData();
                if (data) {
                    this.model.submit(data, function () {
                        that.destroy();
                        that._initDatasourceList();
                    });
                }
            },

            /**
             * 取消按钮点击事件
             *
             * @private
             */
            _cancel: function () {
                this.destroy();
                this._initDatasourceList();
            },

            //------------------------------------------
            // 调用外部接口区域
            //------------------------------------------

            /**
             * 调用数据源管理模块接口
             *
             * @private
             */
            _initDatasourceList: function () {
                require(
                    ['data-sources/list/main-view'],
                    function (View) {
                        new View({
                            el: $('.j-main')
                        });
                    }
                );
            }
        });
    });