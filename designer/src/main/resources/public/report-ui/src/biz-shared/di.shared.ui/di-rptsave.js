/**
 * di.shared.ui.DIReportSave
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    DI 模板镜像操作组件
 * @author:  lizhantong(lztlovely@126.com)
 * @depend:  xui, xutil
 */

$namespace('di.shared.ui');

(function () {

    //------------------------------------------
    // 引用 
    //------------------------------------------

    /* globals di */
    var UTIL = di.helper.Util;

    /* globals xutil */
    var inheritsObject = xutil.object.inheritsObject;
    var INTERACT_ENTITY = di.shared.ui.InteractEntity;
    var DIALOG = di.helper.Dialog;
    var alert = DIALOG.alert;
    var foreachDo = UTIL.foreachDo;


    //------------------------------------------
    // 类型声明 
    //------------------------------------------


    /**
     * DI 模板镜像操作组件
     * 
     * @class
     * @extends xui.XView
     * @param {Object} options
     */
    var DI_REPORTSAVE = $namespace().DIReportSave =
        inheritsObject(INTERACT_ENTITY);
    var DI_REPORTSAVE_CLASS = DI_REPORTSAVE.prototype;

    //------------------------------------------
    // 常量 
    //------------------------------------------

    /**
     * 定义
     */
    DI_REPORTSAVE_CLASS.DEF = {
        // 主元素的css
        className: 'di-reportsave',
        // model配置
        model: {
            clzPath: 'di.shared.model.DIRtplSaveModel'
        }
    };

    //------------------------------------------
    // 方法
    //------------------------------------------

    /**
     * 创建View
     *
     * @private
     * @param {Object} options 参数
     */
    DI_REPORTSAVE_CLASS.$createView = function (options) {
        // 创建tab组件
        this._saveRptTabBtn = this.$di('vuiCreate', 'saveRptTab');
        this._saveRptTabBtn._maxTabNum = this.$di('getDef').maxTabNum || 4;
        // 创建保存组件
        this._saveRptSaveBtn = this.$di('vuiCreate', 'saveRptSave');
        this._saveRptSaveBtn._isInDesigner = this.$di('getDef').isInDesigner;
        this._isInDesigner = this._saveRptSaveBtn._isInDesigner;
        this._saveRptTabBtn._isInDesigner = this.$di('getDef').isInDesigner;
    };

    /**
     * 初始化
     *
     * @public
     */
    DI_REPORTSAVE_CLASS.init = function () {
        var model = this.getModel();
        
        // 新增报表
        model.attach(
            ['sync.preprocess.ADD', this.disable, this],
            ['sync.result.ADD', this.$handleAddImageSuccess, this],
            ['sync.error.ADD', this.$handleError, this],
            ['sync.complete.ADD', this.enable, this]
        );

        // 更新报表
        model.attach(
            ['sync.preprocess.UPDATE', this.disable, this],
            [
                'sync.result.UPDATE',
                this.$handleUpdateImageSuccess,
                this
            ],
            ['sync.error.UPDATE', this.$handleError, this],
            ['sync.complete.UPDATE', this.enable, this]
        );

        // 获取tab全部镜像
        model.attach(
            ['sync.preprocess.GET_IMAGES', this.disable, this],
            [
                'sync.result.GET_IMAGES',
                this.$handleGetAllImagesSuccess,
                this
            ],
            ['sync.error.GET_IMAGES', this.$handleError, this],
            ['sync.complete.GET_IMAGES', this.enable, this]
        );

        this.$handleGetAllImages();
    };

    /**
     * 新增报表镜像-触发ajax请求
     *
     * @param {string} name 镜像名称
     * @protected
     */
    DI_REPORTSAVE_CLASS.$handleAddImage = function (name) {
        var model = this.getModel();
        var args = {
            reportImageName: name,
            asDefault: true
        };
        this._isInDesigner && (args.isInDesigner = this._isInDesigner);

        model.sync(
            {datasourceId: 'ADD', args: args}
        );
    };
    
    /**
     * 新增报表镜像-ajax请求成功回调
     *
     * @param {Object} data 数据
     * @param {Object} ejsonObj json数据
     * @param {Object} options 参数
     * @protected
     */
    DI_REPORTSAVE_CLASS.$handleAddImageSuccess = function (data, ejsonObj, options) {
        var saveRptTabBtn = this._saveRptTabBtn;
        if (ejsonObj.status === 0) {
            saveRptTabBtn.appendTab(
                data.reportImageId, 
                options.args.reportImageName
            );
        }
        else {
            alert(ejsonObj.statusInfo);
        }
    };

    /**
     * 更新报表镜像-触发ajax请求
     *
     * @param {string} id 镜像id
     * @param {string} name 镜像name
     * @protected
     */
    DI_REPORTSAVE_CLASS.$handleUpdateImage = function (id, name) {
        var model = this.getModel();
        var args = {reportImageName: name};
        this._isInDesigner && (args.isInDesigner = this._isInDesigner);
        model.sync({datasourceId: 'UPDATE', args: args});
    };
    
    /**
     * 更新报表镜像-ajax请求成功回调
     *
     * @param {Object} data 数据
     * @param {Object} ejsonObj json数据
     * @param {Object} options 参数
     * @protected
     */
    DI_REPORTSAVE_CLASS.$handleUpdateImageSuccess = function (data, ejsonObj, options) {
        var saveRptTabBtn = this._saveRptTabBtn;

        if (ejsonObj.status === 0) {
           saveRptTabBtn.updateCurrentTab(options.args.reportImageName);
        }
        else {
            alert(ejsonObj.statusInfo);
        }
    };

    /**
     * 获取全部报表镜像
     * 
     * @protected
     */ 
    DI_REPORTSAVE_CLASS.$handleGetAllImages = function () {
        var model = this.getModel();
        var args = {};

        this._isInDesigner && (args.isInDesigner = this._isInDesigner);
        model.sync({datasourceId: 'GET_IMAGES', args: args});
    };
    
    /**
     * 获取报表镜像-ajax请求成功回调
     * vui初始化入口
     *
     * @param {Object} data 数据
     * @param {Object} ejsonObj json数据
     * @param {Object} options 参数
     * @protected
     */ 
    DI_REPORTSAVE_CLASS.$handleGetAllImagesSuccess = function (data, ejsonObj, options) {
        var me = this;
        var saveRptTabBtn = this._saveRptTabBtn;
        var saveRptSaveBtn = this._saveRptSaveBtn;
        // 如果是第一次进来currentImgId为undefined
        var currentImgId = this.$di('getDIFactory').getDIReportImageId();
        
        // 如果currentImgId为undefined，就去后端取当前报表选中值
        // 报表选中值可能有，也可能没有
        if (!currentImgId) {
            currentImgId = request('reportImageId');
        }

        if (ejsonObj.status === 0) {
            saveRptTabBtn.init(
                currentImgId,
                getHandleDeleteImage.call(this),
                me.reloadReport(),
                data
            );
            
            var options = {
                maxTabNum: saveRptTabBtn._maxTabNum,
                getTabsNums: function () {
                    return saveRptTabBtn.getTabsNums();
                },
                saveImageName: function (isAdd, name) {
                    if (isAdd) {
                        me.$handleAddImage(name);
                    }
                    else {
                        me.$handleUpdateImage(currentImgId, name);
                    }
                },
                getCurrentTabName: function () {
                    return saveRptTabBtn.getCurrentTabName();
                },
                getAllTabName: function () {
                    return saveRptTabBtn.getAllTabName();
                }
            };

            saveRptSaveBtn.init(options);
        }
        else {
            alert(ejsonObj.statusInfo);
        }
    };
     
    /**
     * 解禁操作
     *
     * @protected
     */
    DI_REPORTSAVE_CLASS.enable = function () {
        foreachDo(
            [
                this._saveRptTabBtn,
                this._saveRptSaveBtn
            ],
            'enable'
        );
    };
    
    /**
     * 禁用操作
     *
     * @protected
     */
    DI_REPORTSAVE_CLASS.disable = function () {
        foreachDo(
            [
                this._saveRptTabBtn,
                this._saveRptSaveBtn
            ],
            'disable'
        );
    };
    
    /**
     * 操作失败之后提醒
     *
     * @param {Object} data 数据
     * @param {Object} ejsonObj json数据
     * @param {Object} options 参数
     *
     * @protected
     */
    DI_REPORTSAVE_CLASS.$handleError = function (data, ejsonObj, options) {
    	alert(ejsonObj.statusInfo);
    };

    /**
     * 窗口改变后重新计算大小
     *
     * @public
     */
    DI_REPORTSAVE_CLASS.resize = function () {};
    
    /**
     * @override
     */
    DI_REPORTSAVE_CLASS.dispose = function () {};
    
    /**
     * 重新刷新报表
     * 
     * @public
     */
    DI_REPORTSAVE_CLASS.reloadReport = function () {
        var me = this;
        return function (imgId) {
            me.$di('reloadReport', {reportImageId: imgId});
        };
    };
    
    /**
     * 删除镜像-ajax请求函数
     * 函数的主要部分，是返回的匿名函数，在调用getHandleDeleteImage时
     * getHandleDeleteImage.call(this)，注意this为component
     * 
     * @private
     * @returns {Function} 执行删除请求的匿名函数
     */
    function getHandleDeleteImage() {
        var me = this;
        var model = this.getModel();
        var saveRptTabBtn = this._saveRptTabBtn;
        var t = {
            imgId: '',
            callback: null
        };
        // 追加事件，所以放在外面
        model.attach(
            ['sync.preprocess.DELETE', me.disable, me],
            [
                'sync.result.DELETE',
                function (data, ejsonObj, options) {
                    if (ejsonObj.status === 0) {
                        // 如果删除的不是当前，就回vui进行dom删除操作
                        if (t.callback) {
                            t.callback.call(saveRptTabBtn, t.imgId);
                        }
                        // 删除的是当前，就在component中进行进行页面刷新
                        else {
                            me.$di('reloadReport', {reportImageId: options.args.preImageId});
                        }
                    }
                    else {
                        alert(ejsonObj.statusInfo);
                    }
                },
                me
            ],
            ['sync.error.DELETE', me.$handleError, me],
            ['sync.complete.DELETE', me.enable, me]
        );
        
        return function (imgId, imgName, preImgId, deleteTabCallBack) {
            var args = {
                reportId: imgId
            };
            this._isInDesigner && (args.isInDesigner = this._isInDesigner);

            t.callback = deleteTabCallBack;
            t.imgId = imgId;
            preImgId ? (args.preImageId = preImgId) : null;
            model.sync({datasourceId: 'DELETE', args: args});
        };
    }

    /**
     * 获取url传参值
     * @param {string} key url 参数
     * @private
     * @return {string} 匹配到的参数值
     */
    function request(key) {
        var reg = new RegExp('(^|&)' + key + '=([^&]*)(&|$)', 'i');
        var r = window.location.search.substr(1).match(reg);
        if (r != null) {
            return unescape(r[2]);
        } else {
            return null;
        }
    }
})();