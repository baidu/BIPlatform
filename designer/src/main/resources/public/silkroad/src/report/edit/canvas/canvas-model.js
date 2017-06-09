/**
 * @file 报表编辑画布区操作的model
 * @author 赵晓强(longze_xq@163.com)
 * @date 2014-08-05
 */
define(
    [
        'url',
        'constant',
        'report/component-box/components/form-config'
    ],
    function (
        Url,
        Constant,
        formModel
    ) {
        var rootId = 'snpt.';

        return Backbone.Model.extend({

            /**
             * 构造函数
             *
             * @param {Object} option 初始化配置项
             * @constructor
             */
            initialize: function (option) {
                this.parentModel = option.parentModel;
                this.compBoxModel = option.compBoxModel;
                window.canvas = this;
            },

            /**
             * 初始化报表的json文件配置信息
             *
             * @param {Function} success 交互成功后的回调函数
             * @public
             */
            initJson: function (success) {
                var that = this;

                $.ajax({
                    url: Url.initJson(that.id),
                    success: function (data) {
                        if (data.data !== null) {
                            //that.reportJson = eval('(' + data.data + ')');
                            that.reportJson = JSON.parse(data.data);
                        }
                        else {
                            that.reportJson = $.extend(true, {}, that.compBoxModel.config.defaultJson);
                            // 添加form（始终有form）
                            var formModel = that.compBoxModel.config.formModel;
                            that.reportJson.entityDefs.push(formModel.processRenderData(rootId));
                        }
                        success(that.reportJson);
                    }
                });
            },

            /**
             * 初始化报表的vm文件配置信息
             *
             * @param {Function} success 交互成功后的回调函数
             * @public
             */
            initVm: function (success) {
                var that = this;

                $.ajax({
                    url: Url.initVm(that.id),
                    success: function (data) {
                        if (data.data !== null) {
                            that.$reportVm = $(data.data);
                        }
                        else {
                            that.$reportVm = $(that.compBoxModel.config.defaultVm);
                            // 添加form（始终有form）
                            var formModel = that.compBoxModel.config.formModel;
                            that.$reportVm.append(formModel.vmTemplate.render({id: rootId}));
                        }
                        success(that.$reportVm);
                    }
                });
            },

            /**
             * 是否有form（在当前json中）
             *
             * @public
             * @return {boolean} 是否有form
             */
            hasFormComponent: function () {
                var array = this.reportJson.entityDefs;

                for (var i = 0, iLen = array.length; i < iLen; i++) {
                    var entity = array[i];
                    if (entity.clzKey === 'DI_FORM') {
                        return true;
                    }
                }
                return false;
            },

            /**
             * 获取json中的form信息，如果不存在返回null
             *
             * @private
             */
            _getFormJson: function () {
                var array = this.reportJson.entityDefs;
                for (var i = 0, iLen = array.length; i < iLen; i++) {
                    var entity = array[i];
                    if (entity.clzKey === 'DI_FORM') {
                        return entity;
                    }
                }
                return null;
            },

            /**
             * 向报表中添加一个组件
             *
             * @param {Object} compData 组件的配置信息
             * @param {string} compType 组件类型
             * @param {Function} createShell 结合后台返回的组件id生成组件外壳的回调函数
             * @param {Function} success 添加成功后的回调支持
             * @public
             */
            addComp: function (compData, compType, createShell, success) {
                var that = this;

                $.ajax({
                    url: Url.addComp(that.id),
                    type: 'POST',
                    data: {
                        type: compType
                    },
                    success: function (data) {
                        var serverData = data.data;

                        // 向VM中添加数据
                        that.addCompDataToVm(
                            createShell,
                            compData,
                            compType,
                            serverData
                        );
                        // 向Json中添加数据
                        that.addCompDataToJson(compData, compType, serverData);
                        that.saveJsonVm(success);
                    }
                });
            },

            /**
             * 向Vm中添加组件的信息
             *
             * @param {Function} createShell 结合后台返回的组件id生成组件外壳的回调函数
             * @param {Object} compData 组件的配置信息
             * @param {string} compType 组件类型
             * @param {string} serverData 服务器返回的数据
             * @public
             */
            addCompDataToVm: function (
                createShell,
                compData,
                compType,
                serverData
            ) {
                // var reportId = rootId + serverData.id + Constant.COMPONENT_ID_SUFFIX[compType];
                var reportId = rootId + serverData.id;
                var vm = createShell(serverData.id, reportId);
                vm.html(
                    //'<div class="placeholer-20" style="width:100%; height:20px"></div>'
                    //+
                    compData.vm.render({
                        rootId: rootId,
                        serverData: serverData
                    })
                );
                if (compData.type === 'REPORT_SAVE_COMP') {
                    var tabVm = vm.children()[0];
                    this.$reportVm.prepend(tabVm);
                }
                this.$reportVm.append(vm);
            },

            /**
             * 添加组件时向json文件中添加数据
             *
             * @param {Object} compData 组件配置数据，在component-box/main-model中配置
             * @param {string} compType 组件类型，来源同上
             * @param {Object} serverData 服务器返回的数据
             * @public
             */
            addCompDataToJson: function (compData, compType, serverData) {
                var reportJson = this.reportJson;
                var compRenderData;
                var isAddConfirm = false;
                var isHaveConfirm = false;
                var entityDefs = reportJson.entityDefs;
                for (var i = 0; i < entityDefs.length; i ++) {
                    if (entityDefs[i].clzType === 'COMPONENT' && entityDefs[i].clzType === 'DI_FORM') {
                        if (entityDefs[i].vuiRef  && entityDefs[i].vuiRef.confirm) {
                            isHaveConfirm = true;
                        }
                    }
                }
                // 组件的json配置信息
                compRenderData = compData.processRenderData({
                    rootId: rootId,
                    serverData: serverData
                });
                // 当新添加组件时，如果有查询按钮，就修改提交模式为confirm
                if (isHaveConfirm && compRenderData.dataOpt) {
                    compRenderData.dataOpt.submitMode = 'CONFIRM';
                }
                // 添加compId，方便删除组件
                // TODO:重构
                if ($.isArray(compRenderData)) {
                    for (var i = 0, len = compRenderData.length; i < len; i++) {
                        compRenderData[i].compId = serverData.id;
                    }
                }
                else {
                    compRenderData.compId = serverData.id;
                }
                // 如果是vui，需要向form中添加配置
                if (
                    compData.entityDescription
                    && !$.isArray(compData.entityDescription)
                    && compData.entityDescription.clzType == 'VUI'
                ) {
                    formJson = this._getFormJson();
                    // 址引用，直接赋值可以生效
                    if (compData.entityDescription.clzKey === 'H_BUTTON') {
                        formJson.vuiRef.confirm = compRenderData.id;
                        isAddConfirm = true;
                    }
                    else {
                        formJson.vuiRef.input.push(compRenderData.id);
                    }

                }
                // 如果添加的是查询按钮，需要把所有查询方式替换成CONFIRM，因为默认都是IMMEDIATE
                if (isAddConfirm) {
                    for (var i = 0; i < entityDefs.length; i ++) {
                        if (entityDefs[i].clzType === 'COMPONENT') {
                            if (entityDefs[i].dataOpt && entityDefs[i].dataOpt.submitMode) {
                                entityDefs[i].dataOpt.submitMode = 'CONFIRM';
                            }
                        }
                    }
                }
                this.reportJson.entityDefs = entityDefs.concat(compRenderData);
            },

            /**
             * 删除报表中的某一组件,具体发送异步请求
             *
             * @param {string} compId 组件Id
             * @param {string} reportCompId 组件在report-ui端使用的Id
             * @param {string} compType 组件在report-ui端使用的类型
             * @param {Function} success 回调函数
             * @public
             */
            deleteComp: function (compId, reportCompId, compType, success) {
                var that = this;

                $.ajax({
                    url: Url.deleteComp(that.id, compId),
                    type: 'DELETE',
                    success: function () {
                        that._deleteComp(compId, reportCompId, compType, success);
                    }
                });
            },

            /**
             * 删除报表中的某一组件,具体处理本地数据
             *
             * @param {string} compId 组件Id
             * @param {string} reportCompId 组件在report-ui端使用的Id
             * @param {string} compType 组件在report-ui端使用的类型
             * @param {Function} success 回调函数
             * @private
             */
            _deleteComp: function (compId, reportCompId, compType, success) {
                var that = this;
                var isDeleteVUI = false;
                var isDeleteConfirm = false;
                success = success || new Function();
                // 移除vm中的东西
                var selector = '[data-comp-id=' + compId + ']';
                that.$reportVm.find(selector).remove();
                if (compType === 'REPORT_SAVE_COMP') {
                    that.$reportVm.children()[0].remove();
                }

                // 移除json中的东西
                var arr = that.reportJson.entityDefs;
                for (var i = 0; i < arr.length; i++) {
                    if (arr[i].compId == compId) {
                        // 如果是vui（条件组件）要删除form中的配置
                        if (
                            arr[i].clzType == 'VUI'
                            && $.isInArray(arr[i].clzKey, Constant.FORM_VUI_REF)
                        ) {
                            that._deleteCompFromForm(arr[i].id);
                            isDeleteVUI = true;
                        }
                        if (
                            arr[i].clzType == 'VUI'
                            &&
                            (
                                arr[i].clzKey === 'H_BUTTON'
                                && arr[i].dataOpt
                                && arr[i].dataOpt.text === '查询'
                            )
                        ) {
                            var formJson = that._getFormJson();
                            formJson.vuiRef.confirm = null;
                            delete formJson.vuiRef.confirm;
                            isDeleteVUI = true;
                            isDeleteConfirm = true;
                        }
                        arr.splice(i, 1);
                        // 某些组件的数据项可能是一组而并非一个，比如table
                        i--;
                    }
                    // TODO:如果当前组件含有被关联组件，那么也要删除关联关系:测试
                    if (arr[i].clzType === 'COMPONENT' && arr[i].interactions) {
                        for (var j = 0, jLen = arr[i].interactions.length; j < jLen; j ++) {
                            var inter =  arr[i].interactions[j];
                            if (inter.event && inter.event.rid === reportCompId) {
                                arr[i].interactions.splice(j, 1);
                            }
                            else if (inter.events) {
                                for (var x = 0, xLen = inter.events.length; x < xLen; x ++ ) {
                                    if (inter.events[x].rid === reportCompId) {
                                        inter.events.splice(x, 1);
                                    }
                                }
                            }

                        }
                    }
                }

                if (isDeleteConfirm) {
                    for (var i = 0; i < arr.length; i++) {
                        if (arr[i].clzType === 'COMPONENT') {
                            if (arr[i].dataOpt && arr[i].dataOpt.submitMode) {
                                arr[i].dataOpt.submitMode = 'IMMEDIATE';
                            }
                        }
                    }
                }

                that.saveJsonVm(success);
            },

            /**
             * 处理form，主要是对无用的form做删除
             *
             * @private
             */
            _processFrom: function () {
                var formJson = this._getFormJson();
                var vuiArr = formJson.vuiRef.input;

                // 如果已经无vui存在了，删除form
                if (vuiArr.length == 0) {
                    this._deleteComp('comp-id-form');
                }
                // TODO 删除form后需要变换所有组件渲染的方式
                this._responseFormChange();
            },

            /**
             * 从form的配置信息中删除相关的vui信息
             *
             * @param {string} vuiId
             * @private
             */
            _deleteCompFromForm: function (vuiId) {
                var formJson = this._getFormJson();
                var vuiArr = formJson.vuiRef.input;

                for (var i = 0, len = vuiArr.length; i < len; i++) {
                    if (vuiArr[i] == vuiId) {
                        vuiArr.splice(i, 1);
                        break;
                    }
                }
            },

            /**
             * 组件拖动后，更新vm中组件的位置信息
             *
             * @param {string} compId 组件id
             * @param {string} left 左坐标值
             * @param {string} top 上坐标值
             * @public
             */
            updateCompPositing: function (compId, left, top) {
                this.$reportVm.find('[data-comp-id=' + compId + ']').css({
                    left: left,
                    top: top
                });
                this.saveJsonVm();
            },


            /**
             * 失去焦点后，更新vm中组件的内容信息
             *
             * @param {string} textid 焦点元素
             * @param {element} object 焦点元素
             * @public
             */
            dateCompPositing: function (textid, object) {
                var $text = this.$reportVm.find('[id=' + textid + ']');
                if ($text.attr('id') == textid) {
                    $text.html(object);
                }
                this.saveJsonVm();
            },

            /**
             * 组件调整大小后，更新vm中组件的width与height
             *
             * @param {Object} paramObj 参数对象
             * @public
             */
            resizeComp: function (paramObj) {
                var $table = this.$reportVm.find('[data-comp-id=' + paramObj.compId + ']');
                var height = 71;
                var hasRichSel = $table.find(
                    '[data-o_o-di="snpt.'
                    + paramObj.compId
                    + '-vu-table-rich-select"]'
                ).length > 0 ? true : false;
                var hasBread = $table.find(
                    '[data-o_o-di="snpt.'
                    + paramObj.compId
                    + '-vu-table-breadcrumb"]'
                ).length > 0 ? true : false;

                if (hasRichSel) {
                    height += 37;
                }
                if (hasBread) {
                    height += 18;
                }
                $table.css({
                    width: paramObj.width,
                    height: paramObj.height
                }).find('.vu-table').height(parseInt(paramObj.height) - height);
                //}).find('.vu-table').height(parseInt(paramObj.height) - 130);
                // 上下小零件的总高度94（=40+19+35） + 39的padding-top
                // 面包屑18 + 下载文案24 + 39 + 3（添加下载文案下边距3像素-测量得到）（需要查一下）
                $table.css({
                    width: paramObj.width,
                    height: paramObj.height
                }).find('.vu-plane-table').height(parseInt(paramObj.height) - 90);
                // }).find('.vu-plane-table').height(parseInt(paramObj.height) - 126);
                // 16 18 30 22
                this.saveJsonVm();
            },

            /**
             * 保存报表
             *
             * @param {string} reportName 报表名称
             * @param {Function} success 回调函数
             * @param {Function} reportDialog 回调函数
             * @public
             */
            saveReport: function (nowReport, success, reportDialog) {
                var that = this;
                $.ajax({
                    url: Url.saveReport(that.id),
                    type: 'PUT',
                    data: {
                        json: JSON.stringify(that.reportJson),
                        vm: that.$reportVm.prop('outerHTML')
                    },
                    success: function (data) {
                        if (data["status"] === 0) {
                            success && success();
                        }
                        else {
                            var info = data["statusInfo"];
                            reportDialog && reportDialog(info, nowReport);
                        }
                    }
                });
            },

            /**
             * 保存json与vm
             *
             * @param {Function} success 回调函数
             * @public
             */
            saveJsonVm: function (success) {
                var that = this;
                success = success || new Function();
                $.ajax({
                    url: Url.saveJsonVm(that.id),
                    type: 'PUT',
                    data: {
                        json: JSON.stringify(that.reportJson),
                        vm: that.$reportVm.prop('outerHTML')
                    },
                    success: function () {
                        success();
                    }
                });
            },

            /**
             * 保存更改报表名称
             *
             * @param {string} reportId 报表id
             * @param {string} nowReportName 旧报表名称
             * @param {string} newReportName 新报表名称
             * @public
             */
            saveEditReportName: function (reportId, newReportName) {
                $.ajax({
                    type : "POST",
                    dataType : "json",
                    cache : false,
                    timeout  : 10000,
                    uri : Url.saveEditReportName(reportId, newReportName),
                    success : function(data){
                        // 根据返回值进行判断
                        if (data["status"] === 0) {
                            dialog.success(data['statusInfo']);
                        }
                        else {
                            dialog.error(data['statusInfo']);
                        }
                    }
                });
            },

            /**
             * 获取任务管理列表
             *
             * @param {string} reportId 报表id
             * @param {Function} success 回调函数
             *
             * @public
             */
            getFixReportTaskMgrList: function (reportId, success) {
                $.ajax({
                    url: Url.getFixReportTaskMgrList(reportId),
                    type: 'get',
                    success: function (data) {
                        if (data.data && data.data.length > 0) {
                            success(true);
                        }
                        else {
                            success(false);
                        }
                    }
                });
            }
        });
    }
);
