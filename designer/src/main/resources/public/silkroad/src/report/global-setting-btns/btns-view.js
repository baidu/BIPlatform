/**
 * @file 参数维度-view
 * @author weiboxue(wbx_901118@sina.com)
 * @date 2014-12-1
 */
define([
        'template',
        'dialog',
        'report/global-setting-btns/para-btn-template',
        'report/global-setting-btns/select-template',
        'report/global-setting-btns/btns-model'
    ],
    function (
        template,
        dialog,
        ParaBtnTemplate,
        SelectTemplate,
        btnsModel
    ) {
        var btnAttr = {
            ID: 'global-set-'
        };
        return Backbone.View.extend({

            /**
             * 构造函数
             *
             * @constructor
             */
            initialize: function () {
                this.model = new btnsModel();
            },

            /**
             * 设置参数维度
             *
             * @public
             */
            setGlobal: function (arr) {
                var that = this;
                var reportId = window.dataInsight.main.id;
                this.model.getParameterDim(reportId, openDataFormatDialog);
                /**
                 * 打开数据格式设置弹框
                 */
                function openDataFormatDialog(data) {
                    var html;
                    // 初始化参数维度数据(包括维度数据与参数维度数据合并)
                    var initdata = {};
                    initdata.para = data.data;
                    initdata.dimList = arr.dimList;
                    //arr data;
                    html = ParaBtnTemplate.render(
                        initdata
                    );
                    dialog.showDialog({
                        content: html,
                        title: '参数维度设置',
                        dialog: {
                            height: 400,
                            width: 500,
                            open: function () {
                                var $this = $(this);
                                // 删除维度事件绑定
                                $this.on('click', '.j-global-close', function () {
                                    $(this).parent().remove();
                                });
                                // 创建维度事件绑定
                                $this.find('.j-global-add').click(function () {
                                    $('.j-con-global-attr').append(SelectTemplate.render(arr));
                                });
                            },
                            buttons: {
                                '确认': function () {
                                    var $para = $(this);
                                    that._getParaDim(reportId, $para);
                                    $(this).dialog('close');
                                },
                                '取消': function () {
                                    $(this).dialog('close');
                                }
                            }
                        }
                    });
                }
            },

            /**
             * 参数维度数据请求
             *
             * @public
             */
            _getParaDim: function (reportId, $dom) {
                var result = {};
                result.params = {};
                result.params = this._getParaDimData($dom);
                this.model.getParameterDimData(reportId, result);
            },

            /**
             * 参数维度数据请求
             *
             * @public
             */
            _getParaDimData: function ($dom) {
                var $attr = $dom.find('.j-global-attr');
                var result = {};
                $attr.each(function () {
                    var paraname = $(this).find('.parameter-name').val();
                    var paradefault = $(this).find('.parameter-default').val();
                    var paraneeded = $(this).find('.parameter-needed').is(":checked");
                    //console.log(paraneeded);
                    var paraelementId = $(this).find('.parameter-id').val();
                    result[paraname] = {};
                    var para = result[paraname];
                    para.name = paraname;
                    para.defaultValue = paradefault;
                    para.needed = paraneeded;
                    para.elementId = paraelementId;
                });
                return result;
            }
        });
    }
);