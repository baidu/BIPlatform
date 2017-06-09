/**
 * @file: 报表新建（编辑）-- 维度设置模块View
 * @author: lizhantong(lztlovely@126.com)
 * @depend:
 * @date: 2014-07-07
 */

define(
    [
        'report/dim-set/model',
        'report/dim-set/html-template/main-template',
        'report/dim-set/html-template/normal-template',
        'report/dim-set/html-template/normal-new-line-template',
        'report/dim-set/html-template/date-template',
        'report/dim-set/html-template/date-changed-template',
        'report/dim-set/html-template/callback-template',
        'report/dim-set/html-template/callback-new-line-template',
        'report/dim-set/html-template/custom-template',
        'report/dim-set/html-template/custom-new-line-template',
        'dialog'
    ],
    function (
        Model,
        mainTemplate,
        normalTemplate,
        normalNewLineTemplate,
        dateTemplate,
        dateChangedTemplate,
        callbackTemplate,
        callbackNewLineTemplate,
        customTemplate,
        customNewLineTemplate,
        dialog
    ) {

        //------------------------------------------
        // 引用
        //------------------------------------------

        var confirm = dialog.confirm;

        //------------------------------------------
        // 常量
        //------------------------------------------

        /**
         * 普通维度提示信息
         *
         * @const
         * @type {string}
         */
        var NORMAL_MSG = {
            SETTING_MODULE_MSG: '行',
            MAIN_TABLE_MSG: '：请选择主数据表',
            MAIN_TABLE_FIELD_MSG: '：请选择主表字段',
            RELATION_TABLE__MSG: '：请选择关联数据表',
            RELATION_TABLE_FIELD__MSG: '：请选择关联表字段'
        };

        /**
         * 回调维度提示信息
         *
         * @const
         * @type {string}
         */
        var CALLBACK_MSG = {
            SETTING_MODULE_MSG: '模块',
            DIM_MSG: '：请选择回调字段',
            ADDRESS_MSG: '：请填写正确格式的回调地址',
            INTERVAL_MSG: '：请填写正确格式的时间间隔'
        };

        /**
         * 时间维度提示信息
         *
         * @const
         * @type {string}
         */
        var DATE_MSG = {
            RELATION_TABLE_MSG: '：请选择被关联表',
            DATE_FIELD_MSG: '：请选择时间字段',
            LEVEL_MSG: '：请选择粒度',
            // FORMAT_MSG: '：日期格式中的每一个都要选择',
            FORMAT_MSG: '：请选择时间格式',
            RELATION_FIELD_MSG: '：请指定关联字段'
        };

        /**
         * 自定义维度提示信息
         *
         * @const
         * @type {string}
         */
        var CUSTOM_MSG = {
            SETTING_MODULE_MSG: '模块',
            DIM_MSG: '：请选择回调字段',
            DIM_NAME_MSG: '：请输入正确格式的维度名称',
            SQL_MSG: '：请填写正确格式的sql语句'
        };

        //------------------------------------------
        // 视图类的声明
        //------------------------------------------

        /**
         * 维度设置视图类
         *
         * @class
         */
        var View = Backbone.View.extend({

            //------------------------------------------
            // 公共方法区域
            //------------------------------------------

            initialize: function (option) {
                var me = this;
                var model = this.model = new Model();

                this.model.set('id', option.id);
                model.on('submitSucess', me._initEditReportView, me);
                model.on('getDimSetDataSucess', me.render, me);
                model.on(
                    'getDateFieldsDataSucess',
                    me._getDateFieldsDataSucess,
                    me
                );
                model.getDimSetData();
            },

            events: {
                // tab点击事件
                'click .classification': '_dimTypeClick',
                // 普通维度区域
                'click .j-normal-cube-open': '_closeNormalCube',
                'click .j-normal-cube-close': '_openNormalCube',
                'click .j-normal-delete': '_deleteNormalLine',
                'click .j-normal-add': '_addNormalLine',
                'change .j-normal-relation-table-select': '_normalRelationTableChanged',
                // 时间维度
                'change .j-owner-date-level-select': '_dateTypeChanged',
                'change .j-relation-table-select': '_relationTableChanged',
                // 回调维度区域
                'click .j-callback-cube-open': '_closeCallbackCube',
                'click .j-callback-cube-close': '_openCallbackCube',
                'click .j-callback-delete': '_deleteCallbackLine',
                'click .j-callback-add': '_addCallbackLine',
                // 自定义维度区域
                'click .j-custom-delete': '_deleteCustomLine',
                'click .j-custom-add': '_addCustomLine',
                'click .j-custom-field': '_appendToSqlText',
                'focus .j-custom-sql': '_txtSqlFocus',
                'change .j-custom-sql': '_txtSqlBlur',
                // 提交区域
                'click .j-dim-set-ok': '_submit',
                'click .j-dim-set-cancel': '_cancel',
                'click .j-dim-set-prev': '_returnSetCube'
            },

            /**
             * 渲染
             * 根据模版渲染dom元素，绑定事件
             * @public
             */
            render: function () {
                var me = this;
                var model = me.model;
                var normalData = model.buildNormalData();
                var dateData = model.buildDateData();
                var callbackData = model.buildCallbackData();

                // 根据模版进行渲染
                $(me.el).html(mainTemplate.render());
                $('.j-dim-setting-body').append(normalTemplate.render(normalData));
                $('.j-dim-setting-body').append(dateTemplate.render(dateData));
                $('.j-dim-setting-body').append(callbackTemplate.render(callbackData));

                me._renderCustom();
            },

            _renderCustom: function () {
                var $mainContainer,
                    customMainBoxs,
                    $customMainBox,
                    $fieldBox,
                    fieldBoxHeight,
                    relationBoxNum,
                    customData;

                customData = this.model.buildCustomData();
                $('.j-dim-setting-body').append(customTemplate.render(customData));

                $mainContainer = $('.j-custom-main');
                customMainBoxs = $mainContainer.find('.j-custom-main-box');

                // 动态设置每一个主表字段列表的高度
                for(var i = 0, len = customMainBoxs.length; i < len; i++) {
                    $customMainBox = $(customMainBoxs[i]);
                    relationBoxNum = $customMainBox
                            .find('.j-custom-relation-box')
                            .length;
                    $fieldBox = $customMainBox
                            .find('.custom-main-table-fields-box');
                    // 重设字段列表高度
                    fieldBoxHeight = parseInt($fieldBox.css('height'));
                    fieldBoxHeight += (relationBoxNum-1) * 105;
                    $fieldBox.css('height', fieldBoxHeight + 'px')
                }
            },

            /**
             * 销毁
             * @public
             */
            destroy: function () {
                this.stopListening();
                // 删除model
                this.model.clear({silent: true});
                delete this.model;
                this.$el.unbind().empty();
            },

            //------------------------------------------
            // 私有方法区域
            //------------------------------------------

            /**
             * 维度类型点击事件
             * @private
             */
            _dimTypeClick: function (event) {
                var $target;

                if (event.target.tagName.toLowerCase() === 'span') {
                    $target = $(event.target).parent();
                }
                else if (event.target.tagName.toLowerCase() === 'li') {
                    $target = $(event.target);
                }
                this._changeTab($target);
            },

            /**
             * 切换tab
             * @private
             */
            _changeTab: function (target) {
                var tabId,
                    tabIdCode;

                target.addClass('classification-focus')
                    .siblings()
                    .removeClass('classification-focus');

                tabId = target.attr('id');
                tabIdCode =  tabId.split('-');
                $('.dim-container-' + tabIdCode[tabIdCode.length-1]).show()
                    .siblings()
                    .hide();
            },

            /**
             * 普通维度--关联数据表改变之后
             * @private
             */
            _normalRelationTableChanged: function (event) {
                var $target;
                var $relationField;
                var tableId;
                var fieldList = [];
                var html = ['<option value="0">请选择</option>'];
                var me = this;
                var model = me.model;

                // 获取关联数据表，取其对应字段列表
                $target = $(event.target);
                tableId = $target.val();
                // 替换关联表字段
                $relationField = $target.next('select');
                if (tableId === '0') {
                    $relationField.html(html.join(''));
                }
                else {
                    fieldList = model.getFieldListByRelationTable(tableId);
                    for(var i = 0, len = fieldList.length; i < len; i++) {
                        html.push(
                            '<option value="', fieldList[i].name, '">',
                            fieldList[i].comment,
                            '</option>'
                        );
                    }
                    $relationField.html(html.join(''));
                }

            },

            /**
             * 普通维度--收缩cube
             * @private
             */
            _closeNormalCube: function (event) {
                var $target;

                $target = $(event.target);
                $target.removeClass('normal-cube-open j-normal-cube-open')
                    .addClass('normal-cube-close j-normal-cube-close');
                $target.next('div').hide();
            },

            /**
             * 普通维度--展开cube
             * @private
             */
            _openNormalCube: function (event) {
                var $target;

                $target = $(event.target);
                $target.removeClass('normal-cube-close j-normal-cube-close')
                    .addClass('normal-cube-open j-normal-cube-open');

                $target.next('div').show();
            },

            /**
             * 普通维度--删除cube模块中的一行
             * @private
             */
            _deleteNormalLine: function (event) {
                var $target;
                var $lineBox;
                var $prevlineBox;
                var $nextlineBox;
                var relationBoxNum;

                confirm('您确定要删除吗？', function () {
                    $target = $(event.target);
                    $lineBox = $target.parent();
                    $prevlineBox = $lineBox.prev();
                    $nextlineBox = $lineBox.next();
                    relationBoxNum = $lineBox
                        .parent()
                        .find('.j-normal-relation-box')
                        .length;
                    if ( relationBoxNum === 1) {
                        var selects = $lineBox
                            .parent().find('select');
                        for (var i = 0, len = selects.length; i < len; i++) {
                            $(selects[i]).val('0');
                        }
                        return;
                    }
                    // 如果删除的是第一行
                    if ($lineBox.find('.normal-broken-line').length === 0
                        && $nextlineBox
                    ) {
                        $nextlineBox.find('.normal-broken-line').remove();
                    }
                    // 如果删除的是含有 增加按钮 的那一行,就把上面那一行增加一个 增加按钮
                    if ($lineBox.find('.j-normal-add').length > 0
                        && $prevlineBox
                    ) {
                        $prevlineBox.append(
                            '<span class="add j-normal-add"></span>'
                        );
                    }
                    $target.parent().remove();
                });

            },

            /**
             * 普通维度--新增cube模块中的一行
             * @private
             */
            _addNormalLine: function (event) {
                var $target;
                var $cubeBox;
                var $lineBox;
                var newLineData;
                var cubeId;
                var me = this;
                var model = me.model;

                $target = $(event.target);
                $cubeBox = $target.parent().parent().parent();
                $lineBox =  $target.parent().parent();

                cubeId = $cubeBox.find('.cube-name').attr('cubeId');
                newLineData = model.buildNormalNewLineData(cubeId);
                $lineBox.append(normalNewLineTemplate.render(newLineData));
                $target.remove();
            },

            /**
             * 普通维度--获取普通维度提交的数据
             * @private
             */
            _getNormalData: function (result) {
                var me = this;
                var targetDatas = [];
                var $mainContainer = $('.j-normal-main');
                var normalMainBoxs =  $mainContainer.find('.j-normal-main-box');

                // 循环cube容器列表
                for(var i = 0, ilen = normalMainBoxs.length; i < ilen; i++) {
                    var $normalMainBox = $(normalMainBoxs[i]);
                    var $errorMsg = $normalMainBox.find('.j-normal-error-msg');
                    var relationBoxs = $normalMainBox.find('.j-normal-relation-box');
                    var $cubeSpan = $normalMainBox.find('.cube-name');
                    var cubeName = $cubeSpan.text();
                    var targetData = {};

                    targetData.cubeId = $cubeSpan.attr('cubeId');
                    targetData.children = [];

                    for(var j = 0, jlen = relationBoxs.length; j < jlen; j++) {
                        // 获取主表字段、关联表、关联表字段数据
                        var childData = {};
                        var errorMsg;
                        var selects = $(relationBoxs[j]).find('select');

                        childData.currDim = $(selects[0]).val();
                        childData.relationTable = $(selects[1]).val();
                        childData.field = $(selects[2]).val();
                        // 最后一行关联关系
                        if (j === (jlen-1) &&
                            (childData.currDim === '0' &&
                                childData.relationTable === '0' &&
                                childData.field === '0')
                        ) {
                            continue;
                        }
                        errorMsg = ''
                            + cubeName
                            + ' --- 第'
                            + (j+1)
                            + NORMAL_MSG.SETTING_MODULE_MSG;

                       if (childData.currDim === '0') {
                            me._changeTab($('#j-tab-normal'));
                            $errorMsg.html(errorMsg + NORMAL_MSG.MAIN_TABLE_FIELD_MSG).show();
                            return false;
                        }
                        else if (childData.relationTable === '0') {
                            me._changeTab($('#j-tab-normal'));
                            $errorMsg.html(errorMsg + NORMAL_MSG.RELATION_TABLE__MSG).show();
                            return false;
                        }
                        else if (childData.field === '0') {
                            me._changeTab($('#j-tab-normal'));
                            $errorMsg.html(errorMsg + NORMAL_MSG.RELATION_TABLE_FIELD__MSG).show();
                            return false;
                        } else {
                            $errorMsg.html('').hide();
                        }
                        targetData.children.push(childData);
                    }
                    targetDatas.push(targetData);
                }
                result.normal = JSON.stringify(targetDatas);
                return true;
            },

            /**
             * 时间维度(内置)--时间粒度改变
             * @private
             */
            _dateTypeChanged: function (event) {
                var me = this;
                var model = me.model;
                var $target = $(event.target);
                var level = $target.val();
                var list = model.getDateTypeByDateLevel(level);
                var html = [
                    '<option value="0">请选择</option>'
                ];

                for(var i = 0, len = list.length; i < len; i++) {
                    html.push(
                        '<option value="',
                        list[i]
                        ,'">',
                        list[i],
                        '</option>'
                    );
                }
                $target.siblings('.j-owner-date-type-select').html(html.join(''));
            },

            /**
             * 时间维度--被关联表改变
             * @private
             */
            _relationTableChanged: function (event) {
                var me = this;
                var model = me.model;
                var $target = $(event.target);
                var tableId;
                var $lineBox = $target.parent().parent();
                var cubeId = $target.parent().parent().parent().find('.cube-name').attr('cubeId');

                tableId = $target.val();
                model.buildDateFieldsData(tableId, cubeId, $lineBox);

            },

            /**
             * 时间维度--被关联表改变
             * @private
             */
            _getDateFieldsDataSucess: function (data, $lineBox) {
                var $twoPart;
                var $threePart;

                // 改变后，如果是内置表
                if (data.tableId === 'ownertable' || data.tableId === '0') {
                    $lineBox
                        .removeClass('date-relation-normal')
                        .addClass('date-relation-owner');
                }
                else {
                    $lineBox
                        .removeClass('date-relation-owner')
                        .addClass('date-relation-normal');
                }

                $twoPart = $lineBox.find('.j-date-two-part');
                $threePart = $lineBox.find('.j-date-three-part');
                $twoPart && $twoPart.remove();
                $threePart && $threePart.remove();

                $lineBox.append(dateChangedTemplate.render(data));
                // console.log(data);
            },

            /**
             * 时间维度--获取时间维度提交的数据
             * @private
             */
            _getDateData: function (result) {
                var me = this;
                var targetDatas = [];
                var $mainContainer = $('.j-date-main');
                var dateMainBoxs =  $mainContainer.find('.j-date-main-box');

                // 循环cube容器列表
                for(var i = 0, len = dateMainBoxs.length; i < len; i++) {
                    var $dateMainBox = $(dateMainBoxs[i]);
                    var $errorMsg = $dateMainBox.find('.j-date-error-msg');
                    var $cubeSpan = $dateMainBox.find('.cube-name');
                    var cubeName = $cubeSpan.text();
                    var targetData = {};
                    var children = {};
                    var selects;
                    var errorMsg;

                    targetData.cubeId = $cubeSpan.attr('cubeId');
                    targetData.children = [];
                    children.relationTable = $dateMainBox
                            .find('.j-relation-table-select')
                            .val();

                    errorMsg = cubeName;
                    // 如果被关联表是请选择，那么
                    if (children.relationTable === '0') {
                        selects = $dateMainBox
                            .find('.date-relation-owner')
                            .find('.j-date-two-part')
                            .find('select');
                        if ($(selects[0]).val() === '0'
                            && $(selects[1]).val() === '0'
                            && $(selects[2]).val() === '0'
                         ) {
                            targetDatas.push(targetData);
                            continue;
                        }
                        else {
                            me._changeTab($('#j-tab-date'));
                            $errorMsg.html(errorMsg + DATE_MSG.RELATION_TABLE_MSG).show();
                            return false;
                        }
                    }
                    else if (children.relationTable === 'ownertable') {
                        selects = $dateMainBox
                            .find('.date-relation-owner')
                            .find('.j-date-two-part')
                            .find('select');
                        children.currDim = $(selects[0]).val();
                        children.field = $(selects[1]).val();
                        children.format = $(selects[2]).val();
                        if (children.currDim === '0') {
                            me._changeTab($('#j-tab-date'));
                            $errorMsg.html(errorMsg + DATE_MSG.DATE_FIELD_MSG).show();
                            return false;
                        }
                        else if (children.field === '0') {
                            me._changeTab($('#j-tab-date'));
                            $errorMsg.html(errorMsg + DATE_MSG.LEVEL_MSG).show();
                            return false;
                        }
                        else if (children.format === '0') {
                            me._changeTab($('#j-tab-date'));
                            $errorMsg.html(errorMsg + DATE_MSG.FORMAT_MSG).show();
                            return false;
                        }
                        else {
                            $errorMsg.html('').hide();
                        }

                    }
                    else {
                        selects = $dateMainBox
                            .find('.date-relation-normal')
                            .find('.j-date-two-part')
                            .find('select');
                        children.currDim = $(selects[0]).val();
                        children.field = $(selects[1]).val();
                        if (children.currDim === '0') {
                            me._changeTab($('#j-tab-date'));
                            $errorMsg.html(errorMsg + DATE_MSG.RELATION_FIELD_MSG).show();
                            return false;
                        }
                        else if (children.field === '0') {
                            me._changeTab($('#j-tab-date'));
                            $errorMsg.html(errorMsg + DATE_MSG.RELATION_FIELD_MSG).show();
                            return false;
                        }
                        else {
                            $errorMsg.html('').hide();
                        }
                        children.dateLevel = {};
                        selects = $dateMainBox
                            .find('.date-relation-normal')
                            .find('.j-date-three-part')
                            .find('select');

                        for (var j = 0, jLen = selects.length; j < jLen; j++) {
                            var formatKey = $(selects[j]).attr('formatKey');

                            children.dateLevel[formatKey] = {};
                            var ft = children.dateLevel[formatKey] = $(selects[j]).val();
                            if (ft === '0') {
                                me._changeTab($('#j-tab-date'));
                                $errorMsg.html(errorMsg + DATE_MSG.FORMAT_MSG).show();
                                return false;
                            }
                            else {
                                $errorMsg.html('').hide();
                            }
                        }
                    }
                    targetData.children.push(children);
                    targetDatas.push(targetData);
                }
                result.date = JSON.stringify(targetDatas);
                return true;
            },

            /**
             * 回调维度--收缩cube
             * @private
             */
            _closeCallbackCube: function (event) {
                var $target;

                $target = $(event.target);
                $target.removeClass(
                    'callback-cube-open j-callback-cube-open'
                ).addClass(
                    'callback-cube-close j-callback-cube-close'
                );
                $target.next('div').hide();
            },

            /**
             * 回调维度--展开cube
             * @private
             */
            _openCallbackCube: function (event) {
                var $target;

                $target = $(event.target);
                $target.removeClass(
                    'callback-cube-close j-callback-cube-close'
                ).addClass(
                    'callback-cube-open j-callback-cube-open'
                );

                $target.next('div').show();
            },

            /**
             * 回调维度--新增cube模块中的一行
             * @private
             */
            _addCallbackLine: function (event) {
                var $target;
                var $cubeBox;
                var $lineBox;
                var $relationBox;
                var newLineData;
                var cubeId;
                var me = this;
                var model = me.model;

                $target = $(event.target);
                $cubeBox = $target.parent().parent().parent();
                $lineBox = $target.parent().parent();
                $relationBox =  $target.parent();


                cubeId = $cubeBox.find('.cube-name').attr('cubeId');
                newLineData = model.buildCallbackNewLineData(cubeId);
                newLineData.boxIndex = $cubeBox.attr('bodyIndex');
                newLineData.lineIndex = parseInt($relationBox.attr('bodyIndex')) + 1;

                $lineBox.append(callbackNewLineTemplate.render(newLineData));
                $target.remove();
            },

            /**
             * 回调维度--删除cube模块中的一行
             * @private
             */
            _deleteCallbackLine: function (event) {
                var $target;
                var $lineBox;
                var $prevlineBox;
                var $nextlineBox;
                var relationBoxNum;

                confirm('您确定要删除吗？', function () {
                    $target = $(event.target);
                    $lineBox = $target.parent();
                    $prevlineBox = $lineBox.prev();
                    $nextlineBox = $lineBox.next();

                    relationBoxNum = $lineBox
                        .parent()
                        .find('.j-callback-relation-box')
                        .length;
                    if ( relationBoxNum === 1) {
                        $lineBox.find('select').val('0');
                        $lineBox.find('input[type="text"]').val('');
                        $lineBox.find('input[type="radio"]').val('1');
                        return;
                    }
                    // 如果删除的是第一行
                    if ($lineBox.find('.normal-broken-line').length === 0
                        && $nextlineBox
                        ) {
                        $nextlineBox.find('.callback-broken-line').remove();
                    }
                    // 如果删除的是含有 增加按钮 的那一行,就把上面那一行增加一个 增加按钮
                    if ($lineBox.find('.j-callback-add').length > 0
                        && $prevlineBox
                        ) {
                        $prevlineBox.append(
                            '<span class="add j-callback-add"></span>'
                        );
                    }
                    $target.parent().remove();
                });
            },

            /**
             * 回调维度--获取回调维度提交的数据
             * @private
             */
            _getCallbackData: function (result) {
                var me = this;
                var targetDatas = [];
                var $mainContainer = $('.j-callback-main');
                var callbackMainBoxs = $mainContainer.find('.j-callback-main-box');

                // 循环cube容器列表
                for(var i = 0, ilen = callbackMainBoxs.length; i < ilen; i++) {
                    var $callbackMainBox = $(callbackMainBoxs[i]);
                    var $errorMsg = $callbackMainBox.find('.j-callback-error-msg');
                    var relationBoxs = $callbackMainBox
                                .find('.j-callback-relation-box');
                    var $cubeSpan = $callbackMainBox.find('.cube-name');
                    var cubeName = $cubeSpan.text();
                    var targetData = {
                        children: []
                    };

                    targetData.cubeId = $cubeSpan.attr('cubeId');

                    for(var j = 0, jlen = relationBoxs.length; j < jlen; j++) {
                        var errorMsg;
                        var childData = {};
                        errorMsg = ''
                            + cubeName
                            +' --- 第'
                            + (j + 1)
                            + CALLBACK_MSG.SETTING_MODULE_MSG;

                        childData.currDim = $(relationBoxs[j])
                                .find('select')
                                .val();
                        childData.address = $.trim(
                            $(relationBoxs[j])
                                .find('.j-callback-address-input')
                                .val()
                        );
                        childData.refreshType = $(relationBoxs[j])
                            .find('input[type="radio"]:checked')
                            .val();
                        if (j === (jlen-1) &&
                            (childData.currDim === '0' &&
                                childData.address === '' &&
                                childData.refreshType === '1')
                            ) {
                            continue;
                        }

                        if (childData.currDim === '0') {
                            me._changeTab($('#j-tab-callback'));
                            $errorMsg.html(errorMsg + CALLBACK_MSG.DIM_MSG).show();
                            return false;
                        } else if (!validateAddress(childData.address)) {
                            me._changeTab($('#j-tab-callback'));
                            $errorMsg.html(errorMsg + CALLBACK_MSG.ADDRESS_MSG).show();
                            return false;
                        } else {
                            $errorMsg.html('').hide();
                        }

                        if ( childData.refreshType === '3') {
                            childData.interval = $.trim(
                                $(relationBoxs[j])
                                    .find('.j-callback-cache-type-interval')
                                    .val()
                            );
                            if (!validateInterval(childData.interval)) {
                                me._changeTab($('#j-tab-callback'));
                                $errorMsg.html(errorMsg + CALLBACK_MSG.INTERVAL_MSG).show();
                                return false;
                            } else {
                                $errorMsg.html('').hide();
                            }
                        }
                        targetData.children.push(childData);
                    }
                    targetDatas.push(targetData);
                }
                result.callback = JSON.stringify(targetDatas);
                return true;

                function validateAddress(address) {
                    var pattern = /[\u4e00-\u9fa5]/; // 中文校验
                    if (address === '' || pattern.test(address)) {
                        return false;
                    }
                    return true;
                }

                function validateInterval(time) {
                    var pattern = /^[1-9]\d*$/; // 数字
                    if (time === '' || !pattern.test(time)) {
                        return false;
                    }
                    return true;
                }
            },

            /**
             * 自定义维度--删除cube模块中的一行
             * @private
             */
            _deleteCustomLine: function (event) {
                var $target;
                var $lineBox;
                var $prevlineBox;
                var $fieldBox;
                var height;
                var relationBoxNum;

                confirm('您确定要删除吗？', function () {
                    $target = $(event.target);
                    $lineBox = $target.parent();
                    $prevlineBox = $lineBox.prev();
                    $fieldBox = $lineBox.parent().prev();
                    height = parseInt($fieldBox.css('height')) - 105;

                    relationBoxNum = $lineBox
                        .parent()
                        .find('.j-custom-relation-box')
                        .length;
                    if ( relationBoxNum === 1) {
                        $lineBox.find('input[type=text]').val('');
                        $lineBox.find('textarea').val('');
                        return;
                    }

                    // 如果删除的是含有 增加按钮 的那一行,就把上面那一行增加一个 增加按钮
                    if ($lineBox.find('.j-custom-add').length > 0
                        && $prevlineBox
                    ) {
                        $prevlineBox.append(
                            '<span class="add j-custom-add"></span>'
                        );
                    }
                    $target.parent().remove();
                    $fieldBox.css('height', height + 'px');
                });
            },

            /**
             * 自定义维度--新增cube模块中的一行
             * @private
             */
            _addCustomLine: function (event) {
                var $target = $(event.target);
                var $lineBox = $target.parent().parent();
                var $fieldBox;
                var height;

                $fieldBox = $lineBox.prev();
                $lineBox.append(customNewLineTemplate.render($fieldBox.attr('bodyIndex')));
                $target.remove();

                height = parseInt($fieldBox.css('height')) + 105;
                $fieldBox.css('height', height + 'px');
            },

            /**
             * 自定义维度--点击主表字段，添加到右边sql语句框
             * @private
             */
            _appendToSqlText: function (event) {
                var $target;
                var fieldName;
                var $focusTextSql;
                var fieldBodyIndex;
                var textBodyIndex;
                var content;
                var cursorPostion;

               // 获取到点击字段元素以及text元素
                $target = $(event.target);
                fieldName = ' ' + $target.text() + ' ';
                $focusTextSql = $(this.focusTextSql);

               // 获取两者的模块index,用来判断两者是否属于同一个模块
                fieldBodyIndex = $target.attr('bodyIndex');
                textBodyIndex = $focusTextSql.attr('bodyIndex');

                if ($focusTextSql && (fieldBodyIndex === textBodyIndex)) {
                    content = $focusTextSql.val();
                    cursorPostion = $.getCursorPosition($focusTextSql[0]);

                    content = content.slice(0, cursorPostion)
                        + fieldName
                        + content.slice(cursorPostion, content.length);

                    $focusTextSql.val(content);
                    $focusTextSql.diFocus(cursorPostion + fieldName.length);
                }
            },

            /**
             * 自定义维度--sql语句框得到焦点
             * @private
             */
            _txtSqlFocus: function (event) {
                var me = this;
                me.focusTextSql = event.target;
            },

            _txtSqlBlur: function (event) {
                var me = this;
                var $target = $(event.target);

                if (me._validateSql($target.val())) {  // 如果校验OK
                    $target
                        .next('span')
                        .removeClass('custom-create-new-dim-texts-wrong')
                        .addClass('custom-create-new-dim-texts-right');
                }
                else { // 如果校验fail
                    $target
                        .next('span')
                        .removeClass('custom-create-new-dim-texts-right')
                        .addClass('custom-create-new-dim-texts-wrong');
                }
            },

            /**
             * 自定义维度--sql语句框失去焦点校验
             * @private
             */
            _validateSql:function (sql) {
                if (!$.trim(sql)) {
                    return false;
                }
                return true;
            },
            /**
             * 自定义维度--获取提交的数据
             * @private
             */
            _getCustomData: function (result) {
                var me = this;
                var targetDatas = [];
                var $mainContainer = $('.j-custom-main');
                var customMainBoxs = $mainContainer.find('.j-custom-main-box');

                // 循环cube容器列表
                for(var i = 0, ilen = customMainBoxs.length; i < ilen; i++) {
                    var targetData = {};
                    var $customMainBox = $(customMainBoxs[i]);
                    var relationBoxs = $customMainBox
                            .find('.j-custom-relation-box');
                    var $errorMsg = $customMainBox.find('.j-custom-error-msg');
                    // 获取cube标签元素
                    var $cubeSpan = $customMainBox.find('.cube-name');
                    var cubeName = $cubeSpan.text();

                    targetData.cubeId = $cubeSpan.attr('cubeId');
                    targetData.children = [];

                    for(var j = 0, jlen = relationBoxs.length; j < jlen; j++) {
                        var errorMsg = ''
                                + cubeName
                                + ' --- 第'
                                + (j+1)
                                + CUSTOM_MSG.SETTING_MODULE_MSG;
                        var childData = {};

                        childData.dimName = $.trim(
                            $(relationBoxs[j])
                                .find('input[type="text"]')
                                .val()
                        );
                        childData.sql = $.trim(
                            $(relationBoxs[j])
                                .find('textarea')
                                .val()
                        );

                        if (j === (jlen-1) &&
                            (childData.dimName === '' &&
                                childData.sql === '')
                            ) {
                            continue;
                        }

                        if (!childData.dimName) {
                            me._changeTab($('#j-tab-custom'));
                            $errorMsg.html(errorMsg + CUSTOM_MSG.DIM_NAME_MSG).show();
                            return false;
                        } else if (!childData.sql) {
                            me._changeTab($('#j-tab-custom'));
                            $errorMsg.html(errorMsg + CUSTOM_MSG.SQL_MSG).show();
                            return false;
                        } else {
                            $errorMsg.html('').hide();
                        }
                        targetData.children.push(childData);
                    }
                    targetDatas.push(targetData);
                }
                result.custom = JSON.stringify(targetDatas);
                return true;
            },

            /**
             * 提交数据
             * @private
             */
            _submit: function (event) {
                var me = this;
                var model = me.model;
                // 获取普通纬度设置的数据
                var result = {};

                if (!me._getNormalData(result)) {
                    return false;
                }
                else if (!me._getDateData(result)) {
                    return false;
                }
                else if (!me._getCallbackData(result)) {
                    return false;
                }
                else if (!me._getCustomData(result)) {
                    return false;
                }
                model.submit(result);
            },

            /**
             * 提交数据成功
             * @private
             */
            _submitSucess: function () {
                this._iniLeftPanel();
            },

            /**
             * 取消按钮
             * @private
             */
            _cancel: function (event) {
                this._initReport();
            },

            _returnSetCube: function (event) {
                this._initSetCube();
            },
            //------------------------------------------
            // 外部接口区域
            //------------------------------------------

            /**
             * 初始化报表编辑页面
             *
             * @private
             */
            _initEditReportView: function() {
                var model = this.model;
                var id = model.get('id');

                require(['report/edit/main-view'], function (EditReportView) {
                    window.dataInsight.main = new EditReportView({
                        el: $('.j-main'),
                        id: id,
                        isEdit: false
                    });
                });
                this.destroy();

            },

            /**
             * 调用cube设置模块接口
             * @private
             */
            _initSetCube: function () {
                var model = this.model;
                var id = model.get('id');

                require(['report/set-cube/cube-view'], function (SetCubeView) {
                    new SetCubeView({
                        el: $('.j-main'),
                        id: id,
                        edit: true
                    });
                });
                this.destroy();
            },

            /**
             * 调用报表管理模块接口
             * @private
             */
            _initReport: function () {
                require(['report/list/main-view'], function (ListView) {
                    new ListView({
                        el: $('.j-main')
                    });
                });
                this.destroy();
            }


    });

        return View;
});