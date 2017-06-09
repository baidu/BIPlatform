/**
 * @file: 数据源新建模块Model
 * @author: lizhantong(lztlovely@126.com)
 * @date: 2014-07-03
 */

define(['url'], function (Url) {


    //------------------------------------------
    // 模型类的声明
    //------------------------------------------

    var Model = Backbone.Model.extend({

        /**
         * 默认属性
         */
        defaults: {
            // 标识是新增还是更新
            isAdd: true,
            // 数据源id
            id: '',
            type: '', //数据源类型
            name: '',
            connUrl: '', // 地址
            userName: '', // 用户名
            password: '', // 密码
            isEncrypt: '', //是否加密
            database: '' // 数据库
        },

        //------------------------------------------
        // 公共方法区域
        //------------------------------------------

        /**
         * 构造函数
         *
         * @constructor
         */
        initialize: function () {},

        /**
         * 模块初始化data的获取
         *
         * @public
         */
        getInitData: function () {
            var that = this;
            var isAdd = that.get('isAdd');

            // 如果是新建，直接触发render标识，进行渲染
            if (isAdd) {
                that.loadDataSourcesGroup();
            }
            // 如果是更新,就先去后端获取数据源配置信息(获取信息后，触发渲染标识，进行渲染)
            else {
                that.getCurrentDsInfo();
            }
        },

        /**
         * 根据数据源id获取到当前数据源详细信息，进行页面dom填充
         *
         * @public
         */
        getCurrentDsInfo: function () {
            var that = this;
            $.ajax({
                url: Url.getCurrentDataSourceInfo(that.get('groupId'), that.get('id')),
                success: function (data) {
                    that.set('dbData', data.data);
                }
            });
        },


        /**
         * 提交新建数据源信息
         *
         * @param {Object} data 提交的数据
         * @param {Function} success 回调函数
         * @public
         */
        submit: function (data, success) {
            var url;
            var that = this;
            var isAdd = that.get('isAdd');

            if (!isAdd) {
                url = Url.submitDataSourceInfoUpdate(data.groupId, that.get('id'));
            } else {
                url = Url.submitDataSourceInfoAdd(data.groupId);
            }
            delete data.groupId;
            data.isEncrypt = this.get('isEncrypt');
            $.ajax({
                url: url,
                type: isAdd ? 'POST' : 'PUT',
                data: data,
                success: function () {
                    success();
                }
            });
        },

        /**
         * 加载数据源列表
         *
         * @public
         */
        loadDataSourcesGroup: function () {
            var that = this;
            $.ajax({
                url: Url.loadDsgroupList(),
                success: function (data) {
                    var tarData = data.data;
                    var resData = [];
                    for (var i = 0, iLen = tarData.length; i < iLen; i ++) {
                        resData.push({
                            id: tarData[i].id,
                            name: tarData[i].name
                        });
                    }
                    that.set('groupData', resData);
                    that.set('dbData', {});
                }
            });
        }
    });

    return Model;
});