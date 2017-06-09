define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$each=$utils.$each,$value=$data.$value,$index=$data.$index,$escape=$utils.$escape,$dim=$data.$dim,$out='';$out+='<div class="j-global-box">\n    <img class="j-global-add" src="src/css/img/add.png"/>\n    <div class="j-con-global-attr">\n        ';
        $each($data.para,function($value,$index){
        $out+='\n        <div class="j-global-attr">\n            <input type="text" class="parameter-name" value="';
        $out+=$escape($value.name);
        $out+='" placeholder="维度名称"/>\n            <select class="parameter-id">\n                ';
        $each($data.dimList,function($dim,$index){
        $out+='\n                <option value="';
        $out+=$escape($dim.id);
        $out+='" ';
        if($dim.id == $value.elementId){
        $out+=' selected="selected" ';
        }
        $out+='>';
        $out+=$escape($dim.caption);
        $out+='</option>\n                ';
        });
        $out+='\n            </select>\n            <input type="text" class="parameter-default" value="';
        $out+=$escape($value.defaultValue);
        $out+='" placeholder="默认值"/>\n            <input type="checkbox" class="parameter-needed" ';
        if($value.needed == true){
        $out+=' checked="checked"';
        }
        $out+='}/>\n            <div class="j-global-close"></div>\n        </div>\n        ';
        });
        $out+='\n    </div>\n</div>';
        return $out;
    }
    return { render: anonymous };
});