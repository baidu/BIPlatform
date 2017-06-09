/**
 * @file: 报表组件组合后渲染的入口
 * @author: lizhantong(lztlovely@126.com)
 * date:     2014/07/31
 */

define(function () {

    //------------------------------------------
    // 引用
    //------------------------------------------
    var GLOBAL_MODEL = di.shared.model.GlobalModel;
    var Engine = di.shared.model.Engine;
    var engine;
    var globalModel;
    var options = {
        webRoot: '/silkroad'
    };

    //------------------------------------------
    // 对外提供接口
    //------------------------------------------
    var enter = {
        start: start,
        dispose: dispose
    };

    /**
     * 画布中的报表引擎入口
     *
     */
    function start(o) {
        var jsonArray = [];
        jsonArray.push(o.rptJson);
        o.parentEl.innerHTML = o.rptHtml;
        o.reportId && (options.reportId = o.reportId);
        options.reportBody = o.parentEl.parent;

        if (!globalModel) {
            globalModel = GLOBAL_MODEL(options);
        }

        engine = new Engine(options);
        engine.start(engine.mergeDepict(jsonArray));
    }


    /**
     * 画布中的报表释放
     *
     */
    function dispose() {
        engine.dispose();
    }

    return enter;
});