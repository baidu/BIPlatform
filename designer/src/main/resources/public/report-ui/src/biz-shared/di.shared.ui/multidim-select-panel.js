/**
 * ist.opanaly.fcanaly.ui.MultiDimSelectPanel
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    维度选择面板
 * @author:  sushuang(sushuang)
 * @depend:  xui
 */

$namespace('di.shared.ui');

(function() {
    
    //-----------------------------------
    // 引用
    //-----------------------------------
    
    var URL = di.config.URL;
    var DIALOG = di.helper.Dialog;
    var UTIL = di.helper.Util;
    var DICT = di.config.Dict;
    var LANG = di.config.Lang;
    var ecuiCreate = UTIL.ecuiCreate;
    var template = xutil.string.template;
    var q = xutil.dom.q;
    var addClass = xutil.dom.addClass;
    var createSingleton = xutil.object.createSingleton;
    var hasValueNotBlank = xutil.lang.hasValueNotBlank;
    var extend = xutil.object.extend;
    var assign = xutil.object.assign;
    var textLength = xutil.string.textLength;
    var textSubstr = xutil.string.textSubstr;
    var stringToDate = xutil.date.stringToDate;
    var trim = xutil.string.trim;
    var bind = xutil.fn.bind;
    var XVIEW = xui.XView;
    var UI_FORM = ecui.ui.Form;
    var UI_BUTTON = ecui.ui.Button;
    var UI_IND_TREE = ecui.ui.IndTree;
    var alert = di.helper.Dialog.alert;
    var DIM_SELECT_MODEL;

    $link(function() {
        MULTIDIM_SELECT_MODEL = di.shared.model.MultiDimSelectModel;
    });

    //-----------------------------------
    // 类型声明
    //-----------------------------------

    /**
     * 维度树选择浮层
     * 单例，直接使用MULTIDIM_SELECT_PANEL()可得到实例
     * 
     * @class
     * @extends xui.XView
     */
    var MULTIDIM_SELECT_PANEL = 
        $namespace().MultiDimSelectPanel = createSingleton(
            XVIEW,
            MultiDimSelectPanelConstructor
        );
    var MULTIDIM_SELECT_PANEL_CLASS = MULTIDIM_SELECT_PANEL.prototype;

    /**
     * 构造函数
     *
     * @constructor
     * @param {Object} options 参数
     */
    function MultiDimSelectPanelConstructor(options) {
        createModel.call(this, options);
        createView.call(this, options);
        this.init();
    }
    // 注意：needLimit属性需要ajax取到后台数据之后才会做真正的业务判断
    // 故只能在handleTreeSuccess方法执行之后再使用。
    var needLimit = false ;
    var dimLimitedSize = 30; 
    var otherLimitedSize = 300;
    
    //-----------------------------------
    // 模板
    //-----------------------------------

    var TPL_MAIN = [
            '<div class="q-di-form">',
                '<label>维度选择</label>',
                '<div class="q-di-dimlimited"></div>',
                '<div class="q-di-level"></div>',
                '<div class="di-dim-mutliselect-tree">',
                    '<div class="q-di-mutlidim"></div>',
                '</div>',
                '<div>',
                    '<div class="di-dim-select-btn">',
                        '<div class="ui-button-g ui-button q-di-submit">确定</div>',
                        '<div class="ui-button-c ui-button q-di-cancel">取消</div>',
                    '</div>',
                '<div>',
            '</div>'
        ].join('');

    //-----------------------------------
    // 方法
    //-----------------------------------

    /**
     * 创建Model
     *
     * @private
     */
    function createModel() {
        this._mDimSelectModel = new MULTIDIM_SELECT_MODEL();
    };

    /**
     * 创建控件
     *
     * @private
     */
    function createView() {
        // 创建主dom
        var el = this._eMain = document.createElement('div');
        addClass(el, 'di-dim-mutliselect-panel');

        document.body.appendChild(el);
        el.innerHTML = TPL_MAIN;

        // 创建控件
        this._uForm = ecuiCreate(
            UI_FORM,
            q('q-di-form', el)[0],
            null,
            { hide: true }
        );


        this._uSubmitBtn = ecuiCreate(
            UI_BUTTON,
            q('q-di-submit', el)[0]
        );
        this._uCancelBtn = ecuiCreate(
            UI_BUTTON,
            q('q-di-cancel', el)[0]
        );
    };

    /**
     * @override
     */
    MULTIDIM_SELECT_PANEL_CLASS.init = function() {
        var me = this;

        // 事件绑定
        this._mDimSelectModel.attach(
            ['sync.preprocess.MULTISELECT', this.disable, this],
            ['sync.result.MULTISELECT', this.$handleTreeSuccess, this],
            ['sync.error.MULTISELECT', this.$handleTreeError, this],
            ['sync.complete.MULTISELECT', this.enable, this]
        );
        this._mDimSelectModel.attach(
            ['sync.preprocess.SAVE', this.disable, this],
            ['sync.result.SAVE', this.$handleSubmitSuccess, this],
            ['sync.error.SAVE', this.$handleSubmitError, this],
            ['sync.complete.SAVE', this.enable, this]
        );
        this._uSubmitBtn.onclick = bind(this.$submitHandler, this);
        this._uCancelBtn.onclick = bind(this.$cancelHandler, this);

        // Init
        this._uForm.init();
        this._uSubmitBtn.init();
        this._uCancelBtn.init();

        // this._uForm.$resize();

        this.$resetInput();
    };
    
    /**
     * @override
     */
    MULTIDIM_SELECT_PANEL_CLASS.dispose = function() {
    	MULTIDIM_SELECT_PANEL.superClass.dispose.call(this);
    };

    /**
     * 打开面板
     *
     * @public
     * @param {string} mode 可取值：
     *                       'VIEW': 查看
     *                       'EDIT': 修改
     * @param {Object} options 参数
     * @param {string=} options.uniqName
     * @param {string} options.selLineName
     * @param {Function} options.commonParamGetter
     * @param {string} options.reportType 值为RTPL_OLAP_TABLE或者RTPL_OLAP_CHART
     * @param {string=} options.dimMode 模式，
     *      可选值为'NORMAL'（默认）, 'TIME'（时间维度面板）
     */
    MULTIDIM_SELECT_PANEL_CLASS.open = function(mode, options) {
        this._sMode = mode;
        this._oOptions = options;
        this.$resetInput();

        // 每次取消的时候，将生成的dom元素清空，避免下次不刷新页面的时候打开有脏数据影响
        var levelDom = q('q-di-level', this._eMain)[0];
            levelDom.innerHTML='';
        var mutliDimDom = q('q-di-mutlidim', this._eMain)[0];
            mutliDimDom.innerHTML='';

        // 每次打开时从后台获取维度树和当前所选
        this._mDimSelectModel.sync(
            { 
                datasourceId: 'MULTISELECT', 
                args: this._oOptions
            }
        );
    };

    /**
     * 重置
     * 
     * @public
     */
    MULTIDIM_SELECT_PANEL_CLASS.$resetInput = function() {
        // 清空以及恢复状态
        // 如果后续只有此一行代码则移除此方法直接调用clear prompt
        this.$clearPrompt();
    };

    /**
     * 清除prompt
     *
     * @protected
     */
    MULTIDIM_SELECT_PANEL_CLASS.$clearPrompt = function() {
        // TODO
    };

    /**
     * 解禁操作
     *
     * @override
     * @public
     */
    MULTIDIM_SELECT_PANEL_CLASS.enable = function(enable) {
        if (this._bDisabled && this._sMode == 'EDIT') {
            this._uSubmitBtn.enable();
            this._uCancelBtn.enable();
        }
        MULTIDIM_SELECT_PANEL.superClass.enable.call(this);
    };    

    /**
     * 禁用操作
     *
     * @override
     * @public
     */
    MULTIDIM_SELECT_PANEL_CLASS.disable = function(enable) {
        if (!this._bDisabled) {
            this._uSubmitBtn.disable();
            this._uCancelBtn.disable();
        }
        MULTIDIM_SELECT_PANEL.superClass.disable.call(this);
    };    

    /**
     * 提交事件处理
     *
     * @protected
     * @event
     */
    MULTIDIM_SELECT_PANEL_CLASS.$submitHandler = function() {
        var levelDom = q('di-level-radio', this._eMain);
        var mutliDimDom = q('di-mutlidim-checkbox', this._eMain);
        var selectedLevel;
        var selectedDims = [];
        for(var i = 0; i < levelDom.length ; i++){
           if(levelDom[i].checked == true){
                selectedLevel = levelDom[i].value;
           }
        }
        for(var j = 0; j < mutliDimDom.length ; j++){
           if(mutliDimDom[j].checked == true){
                selectedDims.push(mutliDimDom[j].value);
           }
        }

        if(selectedDims.length == 0){
            alert('请至少选中一项维度值');
            return ;
        }
        //如果维值需要限制，并且选中的节点个数超过了上限，则limitedOverstepFlag置为true
        var limitedOverstepFlag = false;
        if(needLimit && selectedDims[0].indexOf('all$') == 0 && selectedDims.length > dimLimitedSize+1){
            limitedOverstepFlag = true;
        }else if(needLimit && selectedDims[0].indexOf('all$') < 0 && selectedDims.length > dimLimitedSize){
            limitedOverstepFlag = true;
        }
        //如果维值没有明显的限制，那么会有一个默认限制个数，如果超过这个默认限制，otherOverstepFlag置为true
        var otherOverstepFlag = false;
        if(selectedDims[0].indexOf('all$') == 0 && selectedDims.length > otherLimitedSize+1){
            otherOverstepFlag = true;
        }else if(selectedDims[0].indexOf('all$') < 0 && selectedDims.length > otherLimitedSize){
            otherOverstepFlag = true;
        }

        // 先判断维度是否是明确定义为需要限制个数的，然后再判断选中的个数是否多于默认的最大限额
        if(limitedOverstepFlag){
            alert('该限制维度不能选中多于'+dimLimitedSize+'项！');
            return ;
        }else if(otherOverstepFlag){
            alert('该维度不能选中多于'+otherLimitedSize+'项！');
            return ;
        }

        this._mDimSelectModel.sync(
            { 
                datasourceId: 'SAVE',
                args: extend(
                    {
                        selectedLevel: selectedLevel,
                        selectedDims: selectedDims
                    },
                    this._oOptions
                )
            }
        );
    };

    /**
     * 取消事件处理
     *
     * @protected
     * @event
     */
    MULTIDIM_SELECT_PANEL_CLASS.$cancelHandler = function() {
        this._uForm.hide();
    };

    /**
     * 原因添加成功结果处理
     *
     * @protected
     */
    MULTIDIM_SELECT_PANEL_CLASS.$handleTreeSuccess = function() {
        try {
           var model = this._mDimSelectModel;
           // 先将提示清空
           var dimLimitedDiv = q('q-di-dimlimited', this._eMain)[0];
           dimLimitedDiv.innerHTML = '';
           this._uForm.showModal(DICT.DEFAULT_MASK_OPACITY);
           var levelDom = q('q-di-level', this._eMain)[0];
           
           // 创建查找提示信息dom，对应的在diui-dim-select.css中添加了样式
           var oFindMsg = document.createElement("div");
           oFindMsg.innerHTML = '如需查找，请按&nbsp;<span>Ctrl+f</span>';
           oFindMsg.className = 'di-dim-level-find-msg';
           levelDom.appendChild(oFindMsg);
           
           var multiData = model.getMultiSelectData();
           var selectedLevel ;
            for (var i = 0 ; i < multiData.length ; i++) {
                // 这里只对有需要的限制个数的维度进行限制
                
                    if(multiData[i].needLimit && i == 0){
                        dimLimitedDiv.innerHTML = '维值不要选中多于'+dimLimitedSize+'项';
                        needLimit = true ;
                    }else if(!multiData[i].needLimit && i == 0){
                        needLimit = false ;
                    }
                var levelRadio = document.createElement("INPUT");  
                    levelRadio.type = "radio";  
                    levelRadio.name = "level";  
                    levelRadio.value = multiData[i].name; 
                    addClass(levelRadio, 'di-level-radio');
                var levelEl = document.createElement("span");
                    levelEl.innerHTML = " " + multiData[i].caption+" ";
                    levelDom.appendChild(levelRadio);
                    levelDom.appendChild(levelEl);
                    if(multiData[i].selected == true ){
                        selectedLevel = multiData[i];
                        levelRadio.checked = "checked";
                    }
                  //要想给每个元素添加事件，需要用bind方式
                levelRadio.onclick = bind(
                    function (levelData){
                        var mutliDimDom = q('q-di-mutlidim', this._eMain)[0];
                        handleMutliDimSuccess(levelData,this._eMain);
                    },
                    this,
                    multiData[i]
                );
               
            };
            
            //默认先执行一次拼接维度值html片段的操作
            handleMutliDimSuccess(selectedLevel,this._eMain);
            this._uForm.center();
            
            //  统一加入调整代码（因为无处获知是否由 di-stub 调用、是否为双层iframe嵌套）
            DIALOG.adjustDialogPosition(this._uForm.getMain());
        }
        catch (e) {
            // 需求变化性很大，数据源很杂，真不敢保证返回数据总是匹配，
            // 所以暂用try catch
            this.$handleTreeError();
        }
    };

    // 选中层级之后，要构建该层级下的维值checkbox片段
    function handleMutliDimSuccess (selectedLevel,eMain){
        var mutliDimDom = q('q-di-mutlidim', eMain)[0];
            mutliDimDom.innerHTML = "";
            for (var j = 0 ; j < selectedLevel.children.length ; j++) {
                var dimData = selectedLevel.children[j];
                var dimCheckBox = document.createElement("INPUT");  
                    dimCheckBox.type = "checkbox";  
                    dimCheckBox.name = "selectedDims";  
                    dimCheckBox.value = dimData.name; 
                    addClass(dimCheckBox, 'di-mutlidim-checkbox'); 
                    dimCheckBox.onclick =bind(
                        function (dimData){
                            // var mutliDimDom = q('q-di-mutlidim', eMain)[0];
                            dimClickHandle(dimData,eMain);
                        },
                        this,
                        dimCheckBox
                );
                if(dimData.selected == true ){
                    dimCheckBox.checked = "checked";
                }
                var dimEl = document.createElement("span");
                dimEl.innerHTML = " " + dimData.caption;
                var dimDiv = document.createElement("div"); 
                dimDiv.appendChild(dimCheckBox);
                dimDiv.appendChild(dimEl);
                mutliDimDom.appendChild(dimDiv);
            }
        // 检查默认选中状态
        checkSelectedStatus(eMain);
    }

    //检查维度值默认勾选状态，如果“全选”被选中，则所有维值都需要被选中，
    //反之，如果其他除过“全选”以外的维值被选中，那么“全选”也应该被选中
    function checkSelectedStatus(eMain){
        var firstDimDom = q('di-mutlidim-checkbox', eMain)[0];
        var dimDoms = q('di-mutlidim-checkbox', eMain);
        if (firstDimDom.checked == true){
            for (var i = 0 ;i < dimDoms.length ; i++){
                dimDoms[i].checked = true;
            }
        } else {
            var otherFlag = true;
            for (var i = 1 ;i < dimDoms.length ; i++){
               if (dimDoms[i].checked == false){
                   otherFlag = false; 
               } 
            } 
            if (otherFlag == true){
                firstDimDom.checked=true;
            }
        }
    }

    // 点击维值的checkbox之后，要提供“全选”、“反选”功能
    function dimClickHandle(dimData,eMain){
        var firstDimDom = q('di-mutlidim-checkbox', eMain)[0];
        var dimDoms = q('di-mutlidim-checkbox', eMain);
        var flag = dimData.checked;
        // 如果选中的是第一个“全选”节点，那么不管其选中还是未选中，都将其余节点全选中或全不中
        if (dimData.value == firstDimDom.value){
            for (var i = 0 ;i < dimDoms.length ; i++){
                dimDoms[i].checked = flag;
            }
        } else{
            // 如果是别的节点，如果此次选择是不选中，那么“全选”也不选中
            if (flag == false){
                firstDimDom.checked = false;
            } else {
                var otherFlag = true;
                for (var i = 1 ;i < dimDoms.length ; i++){
                   if (dimDoms[i].checked == false){
                       otherFlag = false; 
                   } 
                } 
                if (otherFlag == true){
                    firstDimDom.checked=true;
                }
            }
        }
    }
    /**
     * 原因添加失败结果处理
     *
     * @protected
     */
    MULTIDIM_SELECT_PANEL_CLASS.$handleTreeError = function() {
        var me = this;
        // 获取维度树出错，提示并关闭面板
        DIALOG.alert(
            LANG.GET_DIM_TREE_ERROR,
            function() {
                me._uForm.hide();
            }
        );
    };

    /**
     * 原因添加成功结果处理
     *
     * @protected
     */
    MULTIDIM_SELECT_PANEL_CLASS.$handleSubmitSuccess = function() {
        this._uForm.hide();
        /**
         * @event di.shared.ui.DimSelectPanel#submit.close
         */
        this.notify('submit.close');

        this._oOptions.onconfirm();
    };

    /**
     * 原因添加失败结果处理
     *
     * @protected
     */
    MULTIDIM_SELECT_PANEL_CLASS.$handleSubmitError = function(status) {
        DIALOG.alert(LANG.SAVE_FAILURE);
    };

})();

