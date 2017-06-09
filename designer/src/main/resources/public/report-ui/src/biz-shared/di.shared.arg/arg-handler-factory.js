/**
 * di.helper.ArgHandlerFactory
 * Copyright 2012 Baidu Inc. All rights reserved.
 * 
 * @file:    默认的参数解析方法集合
 * @author:  sushuang(sushuang)
 * @depend:  xutil
 */

$namespace('di.shared.arg');
 
(function () {
    
    //--------------------------------
    // 引用
    //--------------------------------

    var isObject = xutil.lang.isObject;
    var extend = xutil.object.extend;
    var getByPath = xutil.object.getByPath;
    var setByPath = xutil.object.setByPath;
    var isArray = xutil.lang.isArray;
    var formatTime = di.helper.Util.formatTime;
    var parseTimeUnitDef = di.helper.Util.parseTimeUnitDef;
    var assign = xutil.object.assign;
    var merge = xutil.object.merge;
    var DI_FACTORY;

    $link(function () {
        DI_FACTORY = di.shared.model.DIFactory;
    });

    /**
     * 默认的参数解析方法集合
     * 约定：所有parser的
     *      this：是参数所属的函数被调用时的scope。
     *      输入：
     *          {Array} tarArgs 要处理的参数数组。
     *          {*...} 其余参数。
     *
     * 注意GeneralArgHandler如果要更改原参数对象的内容，需要新建副本，
     * 以免影响其他事件处理器的响应。
     *
     * 得到argHandler的方法：
     *      var argHandler = di.helper.ArgHandlerFactory(
     *          [somObj1, 'handlerName1', 'asdf', 'zxcv', ...],
     *          [null, 'handlerName2', 'zxz', 1242, ...]
     *      );
     * 则得到了一个argHandler，其中会顺序调用handlerName1, handlerName2
     * handlerName1调用时，'asdf', 'zxcv', ... 会作为后面的参数自动传入，
     * handlerName2同理。
     *
     * @param {Array...} descs 
     *          每个Array：
     *              第一个元素是转换函数调用时的scope（可缺省），
     *              第二个元素是转换函数名，
     *              以后的元素是转换函数调用时，tarArgs后面的参数。
     * @return {Function} 参数转换函数
     */
    $namespace().ArgHandlerFactory = function (descs) {
        // 目前全由内部提供，后续支持可扩展
        if (arguments.length < 1) {
            return null;
        }

        var funcs = [];

        // 这其中会进行check，如果非法则返回空
        for (var i = 0, desc; i < arguments.length; i ++) {
            desc = arguments[i];
            funcs.push(
                [
                    desc[0], 
                    NS[desc[1]], 
                    desc.slice(2)
                ]
            );
            if (!funcs[funcs.length - 1][1]) {
                return null;
            }
        }

        return function (tarArgs) {
            // 链式调用各个argHandler
            for (var i = 0, func; func = funcs[i]; i ++) {
                func[1].apply(
                    func[0], 
                    [tarArgs].concat(func[2])
                );
            }
            return tarArgs;
        }
    }

    var NS = {};

    /**
     * 清除参数内容
     * 
     * @public
     * @this {Object} tarArgs所属函数被调用时的scope
     * @param {Array} tarArgs
     * @param {number=} index 参数index，如果缺省则全部清除
     */
    NS.clear = function (tarArgs, index) {
        if (index != null) {
            tarArgs[index] = void 0;
        }
        else {
            for (var i = 0; i < tarArgs.length; i ++) {
                tarArgs[i] = void 0;
            }
        }
    };

    /**
     * 对第一个参数，根据源属性路径取得值，根据目标属性路径放到结果对象中。
     * 属性路径例如'aaa.bbb[3][4].ccc.ddd'
     * 
     * @public
     * @this {Object} tarArgs所属函数被调用时的scope
     * @param {Array} tarArgs
     * @param {string} srcPath 源属性路径，如果为null，则取数组元素本身
     * @param {string} tarPath 目标属性路径，如果为null，则放到数组元素本身上
     * @param {number} index 对第哪个参数进行操作，默认为0
     * @param {Object=} options 参见xutil.object.setByPath的options
     */
    NS.attr = function (tarArgs, srcPath, tarPath, index, options) {
        index = String(index || 0);
        var value = tarArgs[index];
        setByPath(
            !tarPath ? index : (index + '.' + tarPath),
            isObject(value) ? getByPath(srcPath, value, options) : value,
            tarArgs,
            options
        );
    };

    /**
     * 对第一个参数，按arrPath得到数组，对每一个元素，按arcPath和tarPath进行转换
     * 属性路径例如'aaa.bbb[3][4].ccc.ddd'
     * 
     * @public
     * @this {Object} tarArgs所属函数被调用时的scope
     * @param {Array} tarArgs
     * @param {string} arrPath 数组目标，如果为null，则取tarArgs[0]本身
     * @param {string} srcPath 源属性路径，如果为null，则取数组元素本身
     * @param {string} tarPath 目标属性路径，如果为null，则放到数组元素本身上
     * @param {Object=} options 参见xutil.object.setByPath的options
     */
    NS.attrArr = function (tarArgs, arrPath, srcPath, tarPath, options) {
        var value = tarArgs[0];
        var arr = isObject(value)
            ? (
                arrPath 
                    ? getByPath(arrPath, value, options) 
                    : value
            )
            : null;

        if (isArray(arr)) {
            for (var i = 0, itemA; i < arr.length; i ++) {
                NS.attr(arr, srcPath, tarPath, i, options);
            }
        }
    };

    /**
     * 设置数据（用于配置时）
     * 
     * @public
     * @this {Object} tarArgs所属函数被调用时的scope
     * @param {Array} tarArgs
     * @param {*} data 数据
     * @param {number} index 向第哪个参数，默认为0
     */
    NS.setData = function (tarArgs, data, index) {
        tarArgs[index || 0] = data;
    };

    /**
     * merge数据（用于配置时）
     * 
     * @public
     * @this {Object} tarArgs所属函数被调用时的scope
     * @param {Array} tarArgs
     * @param {*} data 数据
     * @param {number} index 向第哪个参数，默认为0
     */
    NS.mergeData = function (tarArgs, data, index) {
        merge(tarArgs[index || 0], data);
    };

    /**
     * 从diIdList给定的id对应的di实例中用getValue取值，
     * 覆盖到tarArgs第一个参数中。
     *
     * @public
     * @this {Object} tarArgs所属函数被调用时的scope
     * @param {Array} tarArgs
     * @param {string} di实例的id
     * @param {string} srcPath 源属性路径
     * @param {string} tarPath 目标属性路径
     * @param {Object=} options 参见xutil.object.setByPath的options
     */
    NS.getValue = function (tarArgs, diId, srcPath, tarPath, options) {
        var ins = DI_FACTORY().getEntity(diId, 'INS');
        var o = [];
        if (ins && ins.$di) {
            var value = ins.$di('getValue');
            setByPath(
                !tarPath ? '0' : ('0.' + tarPath), 
                isObject(value) ? getByPath(srcPath, value, options) : value,
                o,
                options
            );
            
            if (isObject(o[0])) {
                extend(tarArgs[0] || (tarArgs[0] = {}), o[0]);
            }
            else {
                tarArgs[0] = o[0];
            }
        }
    };

    /**
     * 设置来源reportTemplateId
     * 覆盖到tarArgs第一个参数中。
     *
     * @public
     * @this {Object} tarArgs所属函数被调用时的scope
     * @param {Array} tarArgs
     * @param {string} di实例的id
     */
    NS.sourceTemplateId = function (tarArgs, diId) {
        var ins = DI_FACTORY().getEntity(diId, 'INS');
        if (ins && ins.$di) {
            if (!isObject(tarArgs[0])) {
                tarArgs[0] = {};
            }
            tarArgs[0].sourceTemplateId = ins.$di('getReportTemplateId');
        }
    };

    /**
     * 装载dimTagList
     * 这个东西是用于图表组件之间的联动的。
     * 用dimTagList这个属性来传递图/表当前点击的行信息
     *
     * @public
     * @this {Object} tarArgs所属函数被调用时的scope
     * @param {Array} tarArgs
     * @param {string} di实例的id
     */
    NS.dimTagsList = function (tarArgs, diId) {
        var ins = DI_FACTORY().getEntity(diId, 'INS');
        if (ins && ins.$di) {
            if (!isObject(tarArgs[0])) {
                tarArgs[0] = {};
            }
            tarArgs[0].dimTagsList = ins.$di('getDimTagsList');
        }
    };

    /**
     * 修正时间
     * 应用场景例如：可以在这里配置固定时间，隐含时间等界面输入无法不表达出的时间参数
     *
     * @public
     * @this {Object} tarArgs所属函数被调用时的scope
     * @param {Array} tarArgs
     * @param {string} attrName 参数属性名
     * @param {Object.<Array.<string>>} timeUnitDefMap 按此参数修正时间。 
     *      格式例如：{ D: ['-1Y', '0D'], W: ['-1Y', '0D'], M: ['-24M', '0D'], Q: ['-2Y', '0D'] }
     */
    NS.patchTime = function (tarArgs, attrName, timeUnitDefMap) {
        var arg = tarArgs[0];
        if (isObject(arg) && isObject(arg = arg[attrName])) {
            var gran = arg.granularity || 'D';
            arg = parseTimeUnitDef(
                timeUnitDefMap[gran], 
                [arg.start, arg.end, arg.range]
            );
            arg.start = formatTime(arg.start, gran);
            arg.end = formatTime(arg.end, gran);
            extend(tarArgs[0][attrName], arg);
        }
    };

})();