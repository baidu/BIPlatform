/**
 * di.shared.adapter.EcuiCustomTableVUIAdapter
 * Copyright 2013 Baidu Inc. All rights reserved.
 *
 * @file:    ecui提供的平面表控件的适配器
 * @author:  sushuang(sushuang)
 * @depend:  xui, xutil, ecui
 */

$namespace('di.shared.adapter');

(function() {

    var LANG = di.config.Lang;
    var assign = xutil.object.assign;
    
    /**
     * ecui提供的平面表控件的适配器
     *
     * @public
     * @param {Object} def vui的定义
     * @param {Object} options vui实例创建参数
     * @return {Object} vui adapter实例
     */
    $namespace().EcuiCustomTableVUIAdapter = function(def, options) {
        return {
            setData: setData//,
            // getValue: getValue
        };
    };

    /**
     * 设置数据
     *
     * @public
     */
    function setData(data) {
        var head = data.tableData.head || [];
        var tdata = data.tableData.data || [];
        var sortInfo = {};
        var i;
        var o;

        for (i = 0; o = head[i]; i ++) {
            if (o.orderby) {
                o.sortable = true;
                if (o.orderby == 'ASC' || o.orderby == 'DESC') {
                    sortInfo.sortby = o.field;
                    sortInfo.orderby = o.orderby.toLowerCase();
                }
            }
        }

        var options = assign({}, data, ['leftLock', 'rightLock', 'errorMsg']);
        var emptyText = LANG.EMPTY_TEXT;
        if(data.exception && data.exception != ''){
            emptyText = LANG.QUERY_ERROR_TEXT;
        }
        this.render(
            head, tdata, sortInfo, options, emptyText
        );
    }

    // /**
    //  * 取数据
    //  */
    // function getValue() {
    //     // TODO
    // }

})();

