/**
 * di.shared.ui.DITable
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    DI OLAP表视图组件
 *          （这个命名不好，历史原因。
 *          其实现在来说应该叫做DIPivotTable或DIOlapTable。
 *          因为并列的有DIPlaneTable。）
 * @author:  sushuang(sushuang)
 * @depend:  xui, xutil
 */

$namespace('di.shared.ui');

(function () {
    
    //------------------------------------------
    // 引用 
    //------------------------------------------

    var UTIL = di.helper.Util;
    var URL = di.config.URL;
    var inheritsObject = xutil.object.inheritsObject;
    var addClass = xutil.dom.addClass;
    var assign = xutil.object.assign;
    var q = xutil.dom.q;
    var bind = xutil.fn.bind;
    var objKey = xutil.object.objKey;
    var getByPath = xutil.object.getByPath;
    var download = UTIL.download;
    var foreachDo = UTIL.foreachDo;
    var DIALOG = di.helper.Dialog;
    var LANG = di.config.Lang;
    var INTERACT_ENTITY = di.shared.ui.InteractEntity;
    var ARG_HANDLER_FACTORY;
    var alert = di.helper.Dialog.alert;

    $link(function () {
        ARG_HANDLER_FACTORY = di.shared.arg.ArgHandlerFactory;
    });

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
    var DI_RTPLCLONE = $namespace().DIRtplClone = 
        inheritsObject(INTERACT_ENTITY);
    var DI_RTPLCLONE_CLASS = DI_RTPLCLONE.prototype;
    
    //------------------------------------------
    // 常量 
    //------------------------------------------

    /**
     * 定义
     */
    DI_RTPLCLONE_CLASS.DEF = {
        // 主元素的css
        className: 'di-rtplclone',
        // model配置
        model: {
            clzPath: 'di.shared.model.DIRtplCloneModel'
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
    DI_RTPLCLONE_CLASS.$createView = function (options) {
        var el = this.$di('getEl');
         // 
        this._saveRtplCloneBtn = this.$di('vuiCreate', 'saveRtplClone');
        this._clearRtplCloneBtn = this.$di('vuiCreate', 'clearRtplClone');
    };

    /**
     * 初始化
     *
     * @public
     */
    DI_RTPLCLONE_CLASS.init = function () {
        var me = this;
        var key;
        var model = this.getModel();
        var saveRtplCloneBtn = this._saveRtplCloneBtn;
        var clearRtplCloneBtn = this._clearRtplCloneBtn;

        foreachDo(
            [
             	saveRtplCloneBtn,
             	clearRtplCloneBtn
            ],
            'init'
        );
        // 事件绑定
        model.attach(
            ['sync.preprocess.SAVE', this.disable, this],
            ['sync.result.SAVE', this.$handleSaveSuccess, this],
            ['sync.error.SAVE', this.$handleError, this],
            ['sync.complete.SAVE', this.enable, this]
        );
        model.attach(
            ['sync.preprocess.GET_DEFAUL_IMAGENAME', this.disable, this],
            ['sync.result.GET_DEFAUL_IMAGENAME', this.$handleClear, this],
            ['sync.error.GET_DEFAUL_IMAGENAME', this.$handleError, this],
            ['sync.complete.GET_DEFAUL_IMAGENAME', this.enable, this]
        );
        model.attach(
            ['sync.preprocess.CLEAR', this.disable, this],
            ['sync.result.CLEAR', this.$handleClearSuccess, this],
            ['sync.error.CLEAR', this.$handleError, this],
            ['sync.complete.CLEAR', this.enable, this]
        );
        saveRtplCloneBtn && (
        		saveRtplCloneBtn.onclick = bind(this.$handleSaveRtplClone, this)
        );
        clearRtplCloneBtn && (
        		clearRtplCloneBtn.onclick = bind(this.$handleClearRtplClone, this)
        );
    };

    /**
     * @override
     */
    DI_RTPLCLONE_CLASS.dispose = function () {
        foreachDo(
            [
                this._saveRtplCloneBtn,
                this._clearRtplCloneBtn,
            ],
            'dispose'
        );
    	DI_RTPLCLONE.superClass.dispose.call(this);
    };

    /**
     * 从后台获取数据并渲染
     *
     * @public
     * @event
     * @param {Object} options 参数
     */
    DI_RTPLCLONE_CLASS.sync = function (options) {

        // 视图禁用
        /*
        var diEvent = this.$di('getEvent');
        var vd = diEvent.viewDisable;
        vd && this.getModel().attachOnce(
            ['sync.preprocess.DATA', vd.disable],
            ['sync.complete.DATA', vd.enable]
        );*/

    };

    /**
     * 视图清空
     *
     * @public
     * @event
     */
    DI_RTPLCLONE_CLASS.clear = function () {
    };

    /**
     * 保存成功之后提醒
     * 
     * @protected
     */
    DI_RTPLCLONE_CLASS.$handleSaveSuccess = function (data, ejsonObj, options) {
        alert(LANG.SMILE_FACE + '保存成功');
    };


    DI_RTPLCLONE_CLASS.$handleClearSuccess = function (data, ejsonObj, options) {
        alert(LANG.SMILE_FACE + '清除成功，将在下次进入页面时生效');
    };

    DI_RTPLCLONE_CLASS.$handleClear = function (data, ejsonObj, options) {
        //alert(data.defaultImageName);
        var reportImageName = data.defaultImageName;
        var commonParamGetter = this.$di('getCommonParamGetter');
        var model = this.getModel();
         // 清除默认镜像的时候，需要先获取默认镜像的名称
        model.sync(
            { 
                datasourceId: 'CLEAR', 
                args: {
                    reportImageName: reportImageName
                }
            }
        );
        commonParamGetter.update();
    };

    /**
     * 操作失败之后提醒
     * 
     * @protected
     */
    DI_RTPLCLONE_CLASS.$handleError = function (data, ejsonObj, options) {
        //alert('操作异常');
    };

    /**
     * 窗口改变后重新计算大小
     *
     * @public
     */
    DI_RTPLCLONE_CLASS.resize = function () {

    };

    /**
     * 解禁操作
     *
     * @protected
     */
    DI_RTPLCLONE_CLASS.enable = function () {
        foreachDo(
            [
                this._saveRtplCloneBtn,
                this._clearRtplCloneBtn
            ],
            'enable'
        ); 
    	DI_RTPLCLONE.superClass.enable.call(this);
    };

    /**
     * 禁用操作
     *
     * @protected
     */
    DI_RTPLCLONE_CLASS.disable = function () {
        foreachDo(
            [
                this._saveRtplCloneBtn,
                this._clearRtplCloneBtn
            ],
            'disable'
        ); 
    	DI_RTPLCLONE.superClass.disable.call(this);
    };

    /**
     * 保存镜像操作
     *
     * @protected
     */
    DI_RTPLCLONE_CLASS.$handleSaveRtplClone = function (wrap) {
        var commonParamGetter = this.$di('getCommonParamGetter');
        var model = this.getModel();
         // 保存的时候发镜像保存请求
        model.sync(
            { 
                datasourceId: 'SAVE', 
                args: {
                    asDefault: true
                }
            }
        );
        commonParamGetter.update();

    };
    
    /**
     * 清除镜像操作
     *
     * @protected
     */
    DI_RTPLCLONE_CLASS.$handleClearRtplClone = function (wrap) {
        var commonParamGetter = this.$di('getCommonParamGetter');
        var model = this.getModel();
         // 清除默认镜像的时候，需要先获取默认镜像的名称
        model.sync(
            { 
                datasourceId: 'GET_DEFAUL_IMAGENAME', 
                args: {
                    asdadasda: 'asdadadad'
                }
            }
        );
        commonParamGetter.update();
    };

})();