define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,measureId=$data.measureId,$each=$utils.$each,indList=$data.indList,$ind=$data.$ind,$index=$data.$index,$escape=$utils.$escape,topTypeList=$data.topTypeList,$option=$data.$option,key=$data.key,topType=$data.topType,recordSize=$data.recordSize,$out='';$out+='<!--\n数据例子：\nvar demoData = {\n    measureId: \'\',\n    reocrdSize: \'\',\n    topType: \'\',\n    indList: [\n\n    ],\n    topTypeList: {\n        desc: bottom,\n        asc: top,\n        none: none\n    }\n};\n-->\n<div class="topn-indlist">\n    <div class="topn-indlist-item">\n        <span>指标选择：</span>\n        <select name="measureId">\n            <option value=""\n            ';
        if(!measureId){
        $out+=' selected="selected" ';
        }
        $out+='>请选择</option>\n            ';
        $each(indList,function($ind,$index){
        $out+='\n            <option value=';
        $out+=$escape($ind.id);
        $out+='\n            ';
        if(measureId && $ind.id === measureId){
        $out+=' selected="selected" ';
        }
        $out+='>';
        $out+=$escape($ind.caption);
        $out+='\n            </option>\n            ';
        });
        $out+='\n        </select>\n    </div>\n    <div class="topn-indlist-item">\n        <span>排序方式：</span>\n        <select name="topType">\n            ';
        $each(topTypeList,function($option,key){
        $out+='\n            <option value=';
        $out+=$escape(key);
        $out+='\n            ';
        if(topType && key === topType){
        $out+='\n            selected="selected"\n            ';
        }
        $out+='>';
        $out+=$escape($option);
        $out+='\n            </option>\n            ';
        });
        $out+='\n        </select>\n    </div>\n    <div class="topn-indlist-item">\n        <span>请输入条数：</span>\n        <input type="text" name="recordSize" value="';
        $out+=$escape(recordSize);
        $out+='"/>\n    </div>\n</div>\n';
        return $out;
    }
    return { render: anonymous };
});