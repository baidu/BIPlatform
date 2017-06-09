define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$each=$utils.$each,indList=$data.indList,$ind=$data.$ind,name=$data.name,$escape=$utils.$escape,options=$data.options,$option=$data.$option,optionKey=$data.optionKey,$out='';$out+='<!--\n数据例子：\nvar demoData = {\n    options: {\n        \'left\': \'居左\'，\n        \'center\': \'居中\'，\n        \'right\': \'居右\'\n    },\n    indList: {\n        click: {\n            caption: \'\',\n            align: \'\'\n        }\n    },\n    dimList: {\n        click: {\n            caption: \'\',\n            align: \'\'\n        }\n   }\n};\n-->\n<!-- 指标颜色设置 -->\n<div class="text-align-set">\n    <div class="text-align-set-area">\n        <label>对各指标文本进行单独设置</label>\n        ';
        $each(indList,function($ind,name){
        $out+='\n        <div class="text-align-set-item">\n            <label>';
        $out+=$escape($ind.caption);
        $out+='：</label>\n            <select name="';
        $out+=$escape(name);
        $out+='">\n                ';
        $each(options,function($option,optionKey){
        $out+='\n                <option value=';
        $out+=$escape(optionKey);
        $out+='\n                ';
        if($ind.align && optionKey === $ind.align){
        $out+=' selected="selected"\n                ';
        }
        $out+='>';
        $out+=$escape($option);
        $out+='</option>\n                ';
        });
        $out+='\n            </select>\n        </div>\n        ';
        });
        $out+='\n    </div>\n</div>\n';
        return $out;
    }
    return { render: anonymous };
});