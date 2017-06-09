define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$escape=$utils.$escape,$each=$utils.$each,$planeTable=$data.$planeTable,$index=$data.$index,$out='';$out+='<!--\n数据例子：\nvar demoData = {\n    "planeTableParamList": [\n        {\n            "text": "表1",\n            "value": "table1"\n        },\n        {\n            "text": "表2",\n            "value": "table2"\n        },\n        {\n            "text": "表3",\n            "value": "table3"\n        }\n    ]\n};\n-->\n<li class="table-link-set-item">\n    <input value="" placeholder="请输入操作列名" data-value="';
        $out+=$escape($data.operationColumnId);
        $out+='">\n    <select class="right mr-10 j-table-link-set-plane-table">\n        <option value="">请选择平面表</option>\n        ';
        $each($data.planeTableList,function($planeTable,$index){
        $out+='\n        <option value="';
        $out+=$escape($planeTable.value);
        $out+='">';
        $out+=$escape($planeTable.text);
        $out+='</option>\n        ';
        });
        $out+='\n    </select>\n    <span type="button" class="form-common-input-button f-l hide j-next">设置参数</span>\n    <span class="biplt-radius biplt-radius-red biplt-del mt-3 ml-5 f-l c-p j-del" title="删除" data-status="add">\n        <span></span>\n    </span>\n</li>';
        return $out;
    }
    return { render: anonymous };
});