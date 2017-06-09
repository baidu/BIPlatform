/**
 * @file: 报表新建（编辑）--  维度设置模块View
 * @author: lizhantong(lztlovely@126.com)
 * date: 2014-07-21
 */

define(['url'], function (Url) {

    //------------------------------------------
    // 模型类的声明
    //------------------------------------------

    var Model = Backbone.Model.extend({
        defaults: {
            // 时间维度-内置维度（定死的数据）
            defaultDate: {
                level: [
                    { "id": "TimeYear", "name": "年" },
                    { "id": "TimeQuarter", "name": "季" },
                    { "id": "TimeMonth", "name": "月" },
                    { "id": "TimeWeekly", "name": "周" },
                    { "id": "TimeDay", "name": "日" }
                ],
                "dateFormatOptions": {
                    "TimeDay": [ "yyyy-MM-dd", "yyyy/MM/dd" ],
                    "TimeWeekly": [ "yyyy-W", "yyyy/W" ],
                    "TimeMonth": [ "yyyy-MM", "yyyy/MM" ],
                    "TimeQuarter": [ "yyyy-QQ", "yyyy/QQ" ],
                    "TimeYear": [ "yyyy" ]
                }
            },
            id: "report1"
        },
        initialize: function () { },

        /**
         * 获取维度设置数据
         * @public
         */
        getDimSetData: function () {
            var data;
            var me = this;

            $.ajax({
                url: Url.getDimSetList(me.get('id')),
                success: function (response) {
                    if (response.status === 0) {
                        data = response.data;
                        me.set('cubes', data.cubes);
                        me.set('relationTables', data.relationTables);
                        me.set('dateRelationTables', data.dateRelationTables);

                        me.set('dim', me._buildDefaultData(data.dim));
                        me.trigger('getDimSetDataSucess');
                    }
                }
            });
        },

        /**
         * 组合维度设置默认数据
         * 如果没有设置过关联关系
         * @param {Object} data  后端返回的数据
         * @private
         * @return {Object} data 重组后成渲染模版需要的数据
         */
        _buildDefaultData: function (data) {
            var normalCubes;
            var dateCubes;
            var callbackCubes;
            var customCubes;

            normalCubes = data.normal;
            for (var i = 0, len = normalCubes.length; i < len; i++) {
                if (normalCubes[i].children.length === 0) {
                    data.normal[i].children.push({
                        currDim: '',
                        relationTable: '',
                        field: ''
                    });
                }
            }

            dateCubes = data.date;
            for (var i = 0, len = dateCubes.length; i < len; i++) {
                if (dateCubes[i].children.length === 0) {
                    data.date[i].children.push({
                        relationTable: '0',
                        currDim: '0',
                        field: '0',
                        format: '0'
                    });
                }
            }

            callbackCubes = data.callback;
            for (var i = 0, len = callbackCubes.length; i < len; i++) {
                if (callbackCubes[i].children.length === 0) {
                    data.callback[i].children.push({
                        address: '',
                        refreshType: 1,
                        currDim: ''
                    });
                }
            }

            customCubes = data.custom;
            for (var i = 0, len = customCubes.length; i < len; i++) {
                if (customCubes[i].children.length === 0) {
                    data.custom[i].children.push({
                        dimName: '',
                        sql: ''
                    });
                }
            }

            return data;
        },

        /**
         * 组合普通维度模块的数据
         * @public
         * @return {Object} data 普通维度（渲染Html模版）数据
         */
        buildNormalData: function () {
            var me = this;
            var data = {};
            data.dim = {};

            data.dim.normal = me.get('dim').normal;
            data.relationTables = me.get('relationTables');
            data.cubes = me.get('cubes');
            return data;
        },

        /**
         * 组合普通维度模块--新增一行的数据
         * @param {string} cubeId  数据立方体的id
         * @public
         * @return {Object} data 重组普通维度新增一行时，渲染html模版需要的数据
         */
        buildNormalNewLineData: function (cubeId) {
            var me = this;
            var data = {};

            data.relationTables = me.get('relationTables');
            data.currDims = me.get('cubes')[cubeId].currDims;
            return data;
        },

        /**
         * 根据关联表获取其数据
         * @param {string} tableId  表的id
         * @public
         * @return {Object} data 关联表对应的字段数据
         */
        getFieldListByRelationTable: function (tableId) {
            var me = this;
            var relationTables = me.get('relationTables');

            for (var i = 0, len = relationTables.length; i < len; i++) {
                if (relationTables[i].name === tableId) {
                    return relationTables[i].fields;
                }
            }
            return null;
        },

        /**
         * 组合时间维度模块（渲染Html模版）的数据
         * @public
         * @return {Object} data 时间维度（渲染Html模版）数据
         */
        buildDateData: function () {
            var me = this;
            var data = {};

            data.dim = {};
            data.dim.date = me.get('dim').date;
            data.dateRelationTables = me.get('dateRelationTables');
            data.defaultDate = me.get('defaultDate');
            data.cubes = me.get('cubes');
            return data;
        },

        /**
         * 时间维度（内置）-- 根据时间粒度获取时间格式
         * @param {string} level 时间粒度
         * @public
         * @return {Object} data 时间格式数据
         */
        getDateTypeByDateLevel: function (level) {
            return this.get('defaultDate').dateFormatOptions[level];
        },

        /**
         * 时间维度-- 根据被关联表获取数据
         * @param {string} tableId 表id
         * @param {string} cubeId 立方体id
         * @param {$HtmlElement} $lineBox 新的一行容器
         * @public
         */
        buildDateFieldsData: function (tableId, cubeId, $lineBox) {
            var me = this;
            var data = {};
            var cubes = me.get('cubes');
            var defaultDate;

            if (tableId === '0') {
                data.tableId = tableId;
                data.level = [];
                data.dateFormatOptions = {};
                me.trigger('getDateFieldsDataSucess', data, $lineBox);
            }
            else if (tableId === 'ownertable') {
                data.tableId = tableId;
                data.currDims = cubes[cubeId].currDims;
                defaultDate = me.get("defaultDate");
                data.fields = defaultDate.level;
                me.trigger('getDateFieldsDataSucess', data, $lineBox);
            }
            else {
                $.ajax({
                    url: Url.getDimDateRelationTableList(me.get('id'), tableId),
                    success: function (response) {
                        if (response.status === 0) {
                            data.tableId = tableId;
                            data.currDims = cubes[cubeId].currDims;
                            data.fields = response.data.fields;
                            data.dateFormatOptions = response.data.dateFormatOptions;
                            me.trigger(
                                'getDateFieldsDataSucess',
                                data,
                                $lineBox
                            );
                        }
                    }
                });
            }
        },

        /**
         * 组合回调维度模块（渲染Html模版）的数据
         * @public
         * @return {Object} data 回调维度模块（渲染Html模版）的数据
         */
        buildCallbackData: function () {
            var me = this;
            var data = {};

            data.dim = {};
            data.dim.callback = me.get('dim').callback;
            data.relationTables = me.get('relationTables');
            data.cubes = me.get('cubes');
            return data;
        },

        /**
         * 组合回调维度模块--新增一行（渲染Html模版）的数据
         * @param {string} cubeId 立方体id
         * @public
         * @return {Object} data 回调维度模块--新增一行（渲染Html模版）的数据
         */
        buildCallbackNewLineData: function (cubeId) {
            var me = this;
            var data = {};

            data.currDims = me.get('cubes')[cubeId].currDims;
            data.address = "";
            data.refreshType = 1;
            return data;
        },

        /**
         * 组合自定义维度模块（渲染Html模版）的数据
         * @public
         * @return {Object} data 自定义维度模块（渲染Html模版）的数据
         */
        buildCustomData: function () {
            var me = this;
            var data = {};

            data.dim = {};
            data.dim.custom = me.get('dim').custom;
            data.cubes = me.get('cubes');
            return data;
        },

        /**
         * 提交数据
         * @param {Object} data 往后端发的请求参数
         * @public
         */
        submit: function (data) {
            var me = this;

            $.ajax({
                url: Url.submitDimSetInfo(me.get('id')),
                type: 'POST',
                data: data,
                success: function (response) {
                    if (response.status === 0) {
                        me.trigger('submitSucess');
                    }
                }
            });
        }

    });

    return Model;
});
