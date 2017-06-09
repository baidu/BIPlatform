define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$each=$utils.$each,planeTableParamList=$data.planeTableParamList,$planeParam=$data.$planeParam,$index=$data.$index,$escape=$utils.$escape,olapTableDimList=$data.olapTableDimList,$dim=$data.$dim,$out='';$out+='<!--\n数据例子：\nvar demoData = {\n    "olapTableParamList": [\n        {"text": "文本1", "value": "text1", "relationParam": "table1"},\n        {"text": "文本2", "value": "text2", "relationParam": "table2"},\n        {"text": "文本3", "value": "text3", "relationParam": "table3"}\n    ],\n    "planeTableParamList": [\n        {\n            "text": "表1",\n            "value": "table1"\n        },\n        {\n            "text": "表2",\n            "value": "table2"\n        },\n        {\n            "text": "表3",\n            "value": "table3"\n        }\n    ]\n};\n-->\n';
        $each(planeTableParamList,function($planeParam,$index){
        $out+='\n<div class="table-link-set-item">\n    <label class="left" data-value="';
        $out+=$escape($planeParam.name);
        $out+='">';
        $out+=$escape($planeParam.name);
        $out+='：</label>\n    <select class="right">\n        ';
        $each(olapTableDimList,function($dim,$index){
        $out+='\n        <option value=';
        $out+=$escape($dim.value);
        $out+='\n        ';
        if($dim.value===$planeParam.selectedDim){
        $out+=' selected="selected"\n        ';
        }
        $out+='>';
        $out+=$escape($dim.text);
        $out+='</option>\n        ';
        });
        $out+='\n    </select>\n</div>\n';
        });
        return $out;
    }
    return { render: anonymous };
});