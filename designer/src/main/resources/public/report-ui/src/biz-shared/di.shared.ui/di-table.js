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
    var utilString = xutil.string;
    var utilUrl = xutil.url;
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

    $link(function () {
        ARG_HANDLER_FACTORY = di.shared.arg.ArgHandlerFactory;
    });

    //------------------------------------------
    // 类型声明 
    //------------------------------------------

    /**
     * DI 表视图组件
     * 
     * @class
     * @extends xui.XView
     * @param {Object} options
     */
    var DI_TABLE = $namespace().DITable = 
        inheritsObject(INTERACT_ENTITY);
    var DI_TABLE_CLASS = DI_TABLE.prototype;
    
    //------------------------------------------
    // 常量 
    //------------------------------------------

    /**
     * 定义
     */
    DI_TABLE_CLASS.DEF = {
        // 暴露给interaction的api
        exportHandler: {
            sync: { datasourceId: 'DATA' },
            clear: {}
        },
        // 主元素的css
        className: 'di-table',
        // model配置
        model: {
            clzPath: 'di.shared.model.DITableModel'
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
    DI_TABLE_CLASS.$createView = function (options) {
        var el = this.$di('getEl');
        //新增“是否需要指标解释开关，如果需要指标解释，则要发起ajax请求每个指标的说明”
        this._needMeasureDes = this.$di('getDef').needMeasureDes;
        this._uTable = this.$di('vuiCreate', 'mainTable');

        // 面包屑
        this._uBreadcrumb = this.$di('vuiCreate', 'breadcrumb', { maxShow: 5 });

        // 下载按钮
        this._uDownloadBtn = this.$di('vuiCreate', 'download');

        // 离线下载
        this._uOfflineDownloadBtn = this.$di('vuiCreate', 'offlineDownload');

        // 条目数值等信息
        // 模板配置接口：totalRecordCount, currRecordCount
        this._uCountInfo = this.$di('vuiCreate', 'countInfo');

        this._uRichSelect = this.$di('vuiCreate', 'richSelect');
    };

    /**
     * 初始化
     *
     * @public
     */
    DI_TABLE_CLASS.init = function () {
        var me = this;
        var key;
        var model = this.getModel();
        var table = this._uTable;
        var breadcrumb = this._uBreadcrumb;
        var countInfo = this._uCountInfo;
        var downloadBtn = this._uDownloadBtn;
        var offlineDownloadBtn = this._uOfflineDownloadBtn;                
        var richSelect = this._uRichSelect;
        // 事件绑定
        for (key in { 
                'DATA': 1, 
                'DRILL': 1, 
                'LINK_DRILL': 1,
                'SORT': 1
            }
        ) {
            model.attach(
                ['sync.preprocess.' + key, this.$syncDisable, this, key],
                ['sync.result.' + key, this.$renderMain, this],
                ['sync.result.' + key, this.$handleDataLoaded, this],
                ['sync.error.' + key, this.$handleDataError, this],
                ['sync.complete.' + key, this.$syncEnable, this, key]
            );
        }
        key = 'OFFLINE_DOWNLOAD';
        model.attach(
            ['sync.preprocess.' + key, this.$syncDisable, this, key],
            ['sync.error.' + key, this.$handleOfflineDownloadError, this],
            ['sync.complete.' + key, this.$syncEnable, this, key]
        );
        model.attach(
            ['sync.preprocess.CHECK', this.$syncDisable, this, 'CHECK'],
            ['sync.result.CHECK', this.$handleRowAsync, this, false],
            ['sync.error.CHECK', this.$handleRowAsync, this, true],
            ['sync.complete.CHECK', this.$syncEnable, this, 'CHECK']
        );
        model.attach(
            ['sync.preprocess.SELECT', this.$syncDisable, this, 'SELECT'],
            ['sync.result.SELECT', this.$handleRowAsync, this, false],
            ['sync.error.SELECT', this.$handleRowAsync, this, true],
            ['sync.complete.SELECT', this.$syncEnable, this, 'SELECT']
        );
        model.attach(
            ['sync.preprocess.RICH_SELECT_DATA', this.$syncDisable, this, 'RICH_SELECT_DATA'],
            ['sync.result.RICH_SELECT_DATA', this.$handleRenderRichSelect, this, false],
            ['sync.error.RICH_SELECT_DATA', this.$handleRenderRichSelect, this, true],
            ['sync.complete.RICH_SELECT_DATA', this.$syncEnable, this, 'RICH_SELECT_DATA']
        );
        model.attach(
            ['sync.preprocess.RICH_SELECT_CHANGE', this.$syncDisable, this, 'RICH_SELECT_CHANGE'],
            ['sync.result.RICH_SELECT_CHANGE', this.$handleRichSelectChangeSuccess, this, false],
            ['sync.error.RICH_SELECT_CHANGE', this.$handleRichSelectChangeSuccess, this, true],
            ['sync.complete.RICH_SELECT_CHANGE', this.$syncEnable, this, 'RICH_SELECT_CHANGE']
        );

        if(this._needMeasureDes && this._needMeasureDes == true){
           model.attach(
                ['sync.result.MEASURE_DES', this.$setMeasureDes4Table, this]
            ); 
        }
        model.init();

        table.onexpand = bind(this.$handleExpand, this);
        table.oncollapse = bind(this.$handleCollapse, this);
        table.onsort = bind(this.$handleSort, this);
        table.onrowclick = bind(this.$handleRowClick, this);
        table.onrowselect = bind(this.$handleRowCheck, this, 'rowselect', 'SELECT');
        table.onrowcheck = bind(this.$handleRowCheck, this, 'rowcheck', 'CHECK');
        table.onrowuncheck = bind(this.$handleRowCheck, this, 'rowuncheck', 'CHECK');
        table.oncelllinkdrill = bind(this.$handleLinkDrill, this);
        table.oncelllinkbridge = bind(this.$handleLinkBridge, this);

        breadcrumb && (
            breadcrumb.onchange = bind(this.$handleBreadcrumbChange, this)
        );
        downloadBtn && (
            downloadBtn.onclick = bind(this.$handleDownload, this)
        );
        offlineDownloadBtn && (
            offlineDownloadBtn.attach('confirm', this.$handleOfflineDownload, this)
        );
        richSelect && (
            richSelect.attach('richSelectChange', this.$handleRichSelectChange, this)
        );
        foreachDo(
            [
                table,
                breadcrumb,
                countInfo,
                downloadBtn,
                offlineDownloadBtn,
                richSelect
            ],
            'init'
        );

        breadcrumb && breadcrumb.hide();

        this.$di('getEl').style.display = 'none';
    };

    /**
     * @override
     */
    DI_TABLE_CLASS.dispose = function () {
        foreachDo(
            [
                this._uTable,
                this._uBreadcrumb,
                this._uCountInfo,
                this._uDownloadBtn,
                this._uOfflineDownloadBtn,
                this._uRichSelect
            ],
            'dispose'
        );
        DI_TABLE.superClass.dispose.call(this);
    };

    /**
     * 从后台获取数据并渲染
     *
     * @public
     * @event
     * @param {Object} options 参数
     */
    DI_TABLE_CLASS.sync = function (options) {

        // 视图禁用
        /*
        var diEvent = this.$di('getEvent');
        var vd = diEvent.viewDisable;
        vd && this.getModel().attachOnce(
            ['sync.preprocess.DATA', vd.disable],
            ['sync.complete.DATA', vd.enable]
        );*/
        options = options || {};
        options.componentId = this.$di('getId').split('.')[1];
        // 请求后台
        this.$sync(
            this.getModel(),
            'DATA',
            options,
            this.$di('getEvent')
        );
        // TODO:
        if (this._uRichSelect) {
            this.$sync(
                this.getModel(),
                'RICH_SELECT_DATA',
                {
                    componentId : this.$di('getId').split('.')[1]
                }
            );
        }
    };

    /**
     * 视图清空
     *
     * @public
     * @event
     */
    DI_TABLE_CLASS.clear = function () {
        foreachDo(
            [
                this._uTable,
                this._uBreadcrumb,
                this._uCountInfo
            ],
            'setData'
        );
    };

    /**
     * 渲染主体
     * 
     * @protected
     */
    DI_TABLE_CLASS.$renderMain = function (data, ejsonObj, options) {
        // 当数据为null时，清空视图
        if(data === null) {
            this.$handleDataError(ejsonObj.status, ejsonObj, options);
            return;
        }
        this.$di('getEl').style.display = '';
        this.reportId = ejsonObj.data.reportTemplateId;
        foreachDo(
            [
                this._uTable,
                this._uBreadcrumb,
                this._uCountInfo,
                this._uDownloadBtn,
                this._uOfflineDownloadBtn                
            ],
            'diShow'
        ); 

        var setDataOpt = { diEvent: this.$diEvent(options) };

        /*
         * 为了解决 展开行头、收起行头、排序、下钻时 表格的跳动问题
         * 目前采取的方案是：
         * 第①步，在表格setData之前（也就是旧表格还存在的时候），获取表格垂直和水平滚动条的滚动值；
         * 第②步，在表格setData之后（渲染了新表格），“还原”垂直和水平滚动条的原本位置。
         * 
         * 实际实践中，当拖拽滚动条后，第一次进行上述（展开、收起、排序、下钻）操作时，垂直和水平滚动条均会出现1-3px的小幅度抖动，
         * 第二次以后则不会。如果再次拖动滚动，则抖动情况仍会发生（通过点击滚动条空白部或上下左右箭头触发的滚动，则不会发生抖动）。
         * 目前还没有找到原因。推测可能是 由于滚动条在定位时，为了得到某些倍数值 而做了数值修正
         */
        var UI_SCROLLBAR_CLASS = ecui.ui.Scrollbar.prototype;
        var lastScrollTop = this._uTable._uVScrollbar.getValue();   //  上一次垂直滚动条的滚动值
        var lastScrollLeft = this._uTable._uHScrollbar.getValue();  //  上一次水平滚动条的滚动值
        
        // 表格
        this._uTable.$di('setData', data.tableData, setDataOpt);

        /*
         * ecui.ui.Scrollbar.prototype.setValue 方法，是从 table.js 的 UI_TABLE_SCROLL_SETVALUE 方法中“学”到的。
         * 一开始是使用 this._uTable._uVScrollbar.setValue(lastScrollTop)，
         * 但这么使用会先调用 UI_TABLE_SCROLL_SETVALUE 方法，导致最后使用计算后的值 而不是原来的 lastScrollTop。（这样滚动条就达不到预期的位置）
         */
        UI_SCROLLBAR_CLASS.setValue.call(this._uTable._uVScrollbar, lastScrollTop);
        UI_SCROLLBAR_CLASS.setValue.call(this._uTable._uHScrollbar, lastScrollLeft);
        //  ----------------- 代码修改结束 -----------------

        // 如果json模板地方有配置needMeasureDes为true，那么才发起相应
        if(this._needMeasureDes && this._needMeasureDes == true){
            // 根据olaptable的表头定义，发一次ajax请求，得到每个表头指标的相应描述
            var paramArr = [];
                for (var j = 0; j < this._uTable._aColDefine.length; j++) {
                    if(this._uTable._aColDefine[j] && this._uTable._aColDefine[j].uniqueName){
                       paramArr.push('colUniqueNames=' + this._uTable._aColDefine[j].uniqueName);
                    }
                };
            this.$sync(
                this.getModel(),
                'MEASURE_DES',
                { colUniqueNamesArr: paramArr },
                null,
                null,
                { 
                    ajaxOptions:{
                        showWaiting : false
                    }
                    
                }
            );
        }
        // 面包屑
        if (this._uBreadcrumb) {
            if (data.breadcrumbData.datasource
                && data.breadcrumbData.datasource.length > 0
            ) {
                this._uBreadcrumb.show();
                this._uBreadcrumb.$di('setData', data.breadcrumbData, setDataOpt);
            }
            else {
                this._uBreadcrumb.hide();
            }
        }

        // 页信息
        var countInfotpl;
        if(this._uCountInfo){
            countInfotpl = this._uCountInfo.$di('getDef').tpl
        }
        this._uCountInfo && this._uCountInfo.$di(
            'setData', 
            {
                args: {
                    totalRecordCount: data.pageInfo.totalRecordCount,
                    currRecordCount: data.pageInfo.currRecordCount
                },
                tpl: countInfotpl
            },
            setDataOpt
        );

        // 当表格数据获取异常时（主要针对callback指标），需要弹出异常原因
        if (data.tableData && data.tableData.others) {
            DIALOG.alert(data.tableData.others);
        }
        
        /**
         * 渲染事件
         *
         * @event
         */
        this.$di('dispatchEvent', this.$diEvent('rendered', options));
    };

    /**
     * 窗口改变后重新计算大小
     *
     * @public
     */
    DI_TABLE_CLASS.resize = function () {
        this._uTable && this._uTable.resize();
    };

    /**
     * 解禁操作
     *
     * @protected
     */
    DI_TABLE_CLASS.enable = function () {
        foreachDo(
            [
                this._uTable,
                this._uBreadcrumb,
                this._uCountInfo,
                this._uDownloadBtn,
                this._uOfflineDownloadBtn
            ],
            'enable'
        ); 
        DI_TABLE.superClass.enable.call(this);
    };

    /**
     * 禁用操作
     *
     * @protected
     */
    DI_TABLE_CLASS.disable = function () {
        foreachDo(
            [
                this._uTable,
                this._uBreadcrumb,
                this._uCountInfo,
                this._uDownloadBtn,
                this._uOfflineDownloadBtn
            ],
            'disable'
        ); 
        DI_TABLE.superClass.disable.call(this);
    };

    /**
     * 参见DIFactory中dimTagsList的描述
     *
     * @protected
     */
    DI_TABLE_CLASS.getDimTagsList = function () {
        return ARG_HANDLER_FACTORY(
            [this, "getValue", this.$di("getId"), "table.rowChecked", "dimTagsList"],
            [this, "attrArr", "dimTagsList", "value.uniqueName"]
        )([{}])[0].dimTagsList;
    };

    /**
     * 下载操作
     *
     * @protected
     */
    DI_TABLE_CLASS.$handleDownload = function (wrap) {
        // 先把url处理成字符串，获取到reportId
        var commonParamGetter = this.$di('getCommonParamGetter');
        var urlParam = commonParamGetter({
            componentId : this.$di('getId').split('.')[1]
        });
        // 再把url转回成对象
        var paramObj = utilUrl.parseParam(urlParam);
        // 再转成url字符串
        var url = URL('OLAP_TABLE_DOWNLOAD');
        url = utilString.template(url, paramObj);
        download(url, null, true);

        // 对于下载，不进行reportTemplateId控制，直接打开
        commonParamGetter.update();
    };

    /**
     * 离线下载操作
     *
     * @protected
     */
    DI_TABLE_CLASS.$handleOfflineDownload = function () {
        var val = this._uOfflineDownloadBtn.getValue() || {};
        this.$sync(
            this.getModel(),
            'OFFLINE_DOWNLOAD',
            { email: val.email }
        );
    };

    /**
     * 面包屑点击
     *
     * @protected
     */
    DI_TABLE_CLASS.$handleBreadcrumbChange = function (wrap) {
        this.$sync(
            this.getModel(),
            'LINK_DRILL',
            {
                componentId: this.$di('getId').split('.')[1],
                //action: 'EXPAND',
                action: 'expand',
                // 这接口定的很乱，这里是简写的uniq
                uniqueName: wrap['uniqName']
            }
        );
    };  

    /**
     * link式下钻
     *
     * @protected
     */
    DI_TABLE_CLASS.$handleLinkDrill = function (cellWrap, lineWrap) {
        this.$sync(
            this.getModel(),
            'LINK_DRILL',
            {
                componentId: this.$di('getId').split('.')[1],
                //action: 'EXPAND',
                action: 'expand',
                uniqueName: cellWrap['uniqueName'],
                lineUniqueName: (lineWrap || {})['uniqueName']
            }
        );
    };        

    /**
     * 报表跳转
     *
     * @protected
     * @param {string} linkBridgeType 跳转类型，值可为'I'(internal)或者'E'(external)
     * @param {string} url 目标url
     * @param {Object} options 参数
     */
    DI_TABLE_CLASS.$handleLinkBridge = function (colDefItem, rowDefItem, index) {
        var address = URL.getWebRoot()
            + '/reports/'
            + this.reportId
            + '/linkBridge/extend_area/'
            + this.$di('getId').split('.')[1];

        var oForm = document.createElement('form');
        document.body.appendChild(oForm);
        oForm.type = "hidden";
        oForm.method = "post";
        oForm.target = "_blank";
        oForm.action = address;

        var uniqueNameParam = document.createElement("input");
        uniqueNameParam.value = rowDefItem.uniqueName;
        uniqueNameParam.name = "uniqueName";
        oForm.appendChild(uniqueNameParam);

        var meaureParam = document.createElement("input");
        meaureParam.value = colDefItem.linkBridge.split(',')[index];
        meaureParam.name = "measureId";
        oForm.appendChild(meaureParam);
        oForm.submit();

        document.body.removeChild(oForm);
    };

    /**
     * 展开（下钻）
     *
     * @protected
     */
    DI_TABLE_CLASS.$handleExpand = function (cellWrap, lineWrap, pos) {
        this.$sync(
            this.getModel(),
            'DRILL',
            {
                componentId: this.$di('getId').split('.')[1],
                //action: 'EXPAND',
                action: 'expand',
                uniqueName: cellWrap['uniqueName'],
                lineUniqueName: (lineWrap || {})['uniqueName'],
                rowNum: pos.y

            }
        );
    };

    /**
     * 收起（上卷）
     *
     * @protected
     */
    DI_TABLE_CLASS.$handleCollapse = function (cellWrap, lineWrap, pos) {
        this.$sync(
            this.getModel(),
            'DRILL',
            {
                componentId: this.$di('getId').split('.')[1],
                //action: 'COLLAPSE',
                action: 'collapse',
                uniqueName: cellWrap['uniqueName'],
                lineUniqueName: (lineWrap || {})['uniqueName'],
                rowNum: pos.y
            }
        );
    };

    /**  
     * 行点击
     * 
     * @protected
     */
    DI_TABLE_CLASS.$handleRowClick = function (rowDefItem) {
        /**
         * 行点击事件
         *
         * @event
         */
        this.$di(
            'dispatchEvent', 
            'rowclick',
            [{
                uniqueName: rowDefItem.uniqueName
            }]
        );
    };

    /**  
     * 行选中
     * 
     * @protected
     */
    DI_TABLE_CLASS.$handleRowCheck = function (eventName, datasourceId, rowDefItem, callback) {
        if(rowDefItem.uniqueName == 'SUMMARY_CUST: [SUMMARY_NODE].[ALL]'){
            //如果发现是手动加起来的“汇总行”，那么当选中的时候，不要做任何选中操作
        }
        else{
            this.$sync(
                this.getModel(),
                datasourceId, 
                {
                    uniqueName: rowDefItem.uniqueName,
                    componentId: this.$di('getId').split('.')[1]
                },
                null,
                {
                    eventName: eventName,
                    callback: callback
                }
            );
       }
        
    };

    /**  
     * 排序
     * 
     * @protected
     */
    DI_TABLE_CLASS.$handleSort = function (colDefineItem) {

        this.$sync(
            this.getModel(),
            'SORT',
            {
                uniqueName: colDefineItem.uniqueName,
                componentId : this.$di('getId').split('.')[1],
                sortType: colDefineItem.currentSort
            }
        );
    };  
    /**
    * 根据返回数据设置olap表格的指标解释到表格td的title标签中
    */
    DI_TABLE_CLASS.$setMeasureDes4Table = function (data, ejsonObj, options) {
        this._uTable.$setMeasureDes4Table(data);
    };

    /**  
     * 行选中
     * 
     * @protected
     */
    DI_TABLE_CLASS.$handleRowAsync = function (isFailed, data, ejsonObj, options) {

        // 根据后台结果，改变行选中与否
        options.args.callback(data.selected);

        /**
         * line check模式下行选中和取消选中事件
         *
         * @event
         */
        var outParam = this.$di('getDef').outParam;
        var params = {};
        if (outParam) {
            // 整理后端需要的数据格式
            // {
            //      6e72140667f37b984d9764f5aca6b6cb:[dim_trade_trade_l1].[广播通信]
            //      6e72140667f37b984d9764f5aca6b6cb_level:0
            // }
            var uniqueName = options.args.param.uniqueName;
            uniqueName = uniqueName.replace(/{/g, '');
            var uniqueNames = uniqueName.split('}');
            for (var i = 0, iLen = uniqueNames.length; i < iLen - 1 ; i ++) {
                var tempName = uniqueNames[i].split('.')[0];
                tempName = tempName.replace('[', '').replace(']', '');
                if (outParam.dimName === tempName) {
                    params[outParam.dimId] = uniqueNames[i];
                    params[outParam.dimId + '_level'] = outParam.level;
                }
            }
        }
        else {
            params.uniqueName = options.args.param.uniqueName;
        }
        this.$di(
            'dispatchEvent',
            options.args.eventName,
            [params]
        );
    };

    /**
     * 数据加载成功
     * 
     * @protected
     */
    DI_TABLE_CLASS.$handleDataLoaded = function (data, ejsonObj, options) {
        var datasourceId = options.datasourceId;     
        var value = this.$di('getValue');
        var args;
        var param = options.args.param;

        if (datasourceId == 'DATA') {
            args = [value];
        }
        else if (datasourceId == 'LINK_DRILL') {
            args = [assign({}, param, ['uniqueName'])];
        }
        else if (datasourceId == 'DRILL') {
            args = [assign({}, param, ['uniqueName', 'lineUniqueName'])];
        }
        else if (datasourceId == 'SORT') {
            args = [assign({}, param, ['uniqueName', 'currentSort'])];
        }

        /**
         * 数据成功加载事件（分datasourceId）
         *
         * @event
         */
        this.$di(
            'dispatchEvent',
            this.$diEvent('dataloaded.' + datasourceId, options),
            args
        );

        if (datasourceId in { DATA: 1, LINK_DRILL: 1, SORT: 1 }) {
            /**
             * 数据改变事件（DRILL在逻辑上是添加数据，不算在此事件中）
             *
             * @event
             */
            this.$di(
                'dispatchEvent', 
                this.$diEvent('datachange', options), 
                [value]
            );
        }

        /**
         * 数据成功加载事件
         *
         * @event
         */
        this.$di(
            'dispatchEvent', 
            this.$diEvent('dataloaded', options), 
            [value]
        );
    };

    /**
     * 获取表格数据错误处理
     * 
     * @protected
     */
    DI_TABLE_CLASS.$handleDataError = function (status, ejsonObj, options) {
        this.$di('getEl').style.display = '';
        foreachDo(
            [
                this._uTable,
                this._uBreadcrumb,
                this._uCountInfo,
                this._uDownloadBtn,
                this._uOfflineDownloadBtn
            ],
            'diShow'
        ); 

        // 设置空视图
        this.clear();

        /**
         * 渲染事件
         *
         * @event
         */
        this.$di('dispatchEvent', this.$diEvent('rendered', options));
        /**
         * 数据加载失败事件
         *
         * @event
         */
        this.$di('dispatchEvent', this.$diEvent('dataerror', options));
        // 嵌入第三方系统之后，频繁切换页签刷新报表会因为请求中断而弹出该提示框，故先注释掉
        // DIALOG.alert('获取表格数据异常：' + ejsonObj.statusInfo);
    };

    /**
     * 离线下载错误处理
     * 
     * @protected
     */
    DI_TABLE_CLASS.$handleOfflineDownloadError = function (status, ejsonObj, options) {
        DIALOG.alert(LANG.SAD_FACE + LANG.OFFLINE_DOWNLOAD_FAIL);
    };

    DI_TABLE_CLASS.$handleRenderRichSelect = function (data, ejsonObj, options) {
        this._uRichSelect.render(options.data);
    };

    DI_TABLE_CLASS.$handleRichSelectChange = function (selectedIds) {
        this.$sync(
            this.getModel(),
            'RICH_SELECT_CHANGE',
            {
                selectedMeasures: selectedIds,
                componentId : this.$di('getId').split('.')[1]
            }
        );
    };
    DI_TABLE_CLASS.$handleRichSelectChangeSuccess = function (data, ejsonObj, options) {
        options = {};
        options.componentId = this.$di('getId').split('.')[1];
        this.$sync(
            this.getModel(),
            'DATA',
            options,
            this.$di('getEvent')
        );
    };

})();
