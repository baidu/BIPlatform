define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$each=$utils.$each,olapTableParamList=$data.olapTableParamList,$oParamOption=$data.$oParamOption,$index=$data.$index,$escape=$utils.$escape,planeTableParamList=$data.planeTableParamList,$pParam=$data.$pParam,$out='';$out+='<!--\n数据例子：\nvar demoData = {\n    "olapTableParamList": [\n        {"text": "文本1", "value": "text1", "relationParam": "table1"},\n        {"text": "文本2", "value": "text2", "relationParam": "table2"},\n        {"text": "文本3", "value": "text3", "relationParam": "table3"}\n    ],\n    "planeTableParamList": [\n        {\n            "text": "表1",\n            "value": "table1"\n        },\n        {\n            "text": "表2",\n            "value": "table2"\n        },\n        {\n            "text": "表3",\n            "value": "table3"\n        }\n    ]\n};\n-->\n<div class="table-link-set-item">\n    <select class="left">\n        ';
        $each(olapTableParamList,function($oParamOption,$index){
        $out+='\n        <option value=';
        $out+=$escape($oParamOption.value);
        $out+='>';
        $out+=$escape($oParamOption.text);
        $out+='</option>\n        ';
        });
        $out+='\n    </select>\n    <select class="right">\n        ';
        $each(planeTableParamList,function($pParam,$index){
        $out+='\n        <option value=';
        $out+=$escape($pParam.value);
        $out+='>';
        $out+=$escape($pParam.text);
        $out+='</option>\n        ';
        });
        $out+='\n    </select>\n</div>';
        return $out;
    }
    return { render: anonymous };
});