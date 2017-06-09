define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$escape=$utils.$escape,$out='';$out+='<!--\n数据例子：\nvar demoData = {\n    type: \'\',\n    name: \'\',\n    defaultValue: \'\',\n    sqlContidion: \'\'\n};\n-->\n<div class="silkroad-data-field-filter-set">\n    <span class="field-id" data-id="';
        $out+=$escape($data.id);
        $out+='" title="';
        $out+=$escape($data.text);
        $out+='">';
        $out+=$escape($data.text);
        $out+='</span>\n    <input class="field-name" value="';
        $out+=$escape($data.name);
        $out+='" placeholder="名称"/>\n    <select class="condition">\n        ';
        if($data.isMeasure){
        $out+='\n        <option value="EQ" ';
        if($data.sqlCondition=='EQ'){
        $out+='selected=selected ';
        }
        $out+='>等于</option>\n        <option value="NOT_EQ" ';
        if($data.sqlCondition=='NOT_EQ'){
        $out+='selected=selected ';
        }
        $out+='>不等于</option>\n        <option value="LT" ';
        if($data.sqlCondition=='LT'){
        $out+='selected=selected ';
        }
        $out+='>小于</option>\n        <option value="GT" ';
        if($data.sqlCondition=='GT'){
        $out+='selected=selected ';
        }
        $out+='>大于</option>\n        <option value="LT_EQ" ';
        if($data.sqlCondition=='LT_EQ'){
        $out+='selected=selected ';
        }
        $out+='>小于等于</option>\n        <option value="GT_EQ" ';
        if($data.sqlCondition=='GT_EQ'){
        $out+='selected=selected ';
        }
        $out+='>大于等于</option>\n        <option value="IN" ';
        if($data.sqlCondition=='IN'){
        $out+='selected=selected ';
        }
        $out+='>in</option>\n        <option value="BETWEEN_AND" ';
        if($data.sqlCondition=='BETWEEN_AND'){
        $out+='selected=selected ';
        }
        $out+='>between-and</option>\n        <option value="LIKE" ';
        if($data.sqlCondition=='LIKE'){
        $out+='selected=selected ';
        }
        $out+='>like</option>\n        ';
        }else{
        $out+='\n        <option value="EQ" ';
        if($data.sqlCondition=='EQ'){
        $out+='selected=selected ';
        }
        $out+='>等于</option>\n        <option value="IN" ';
        if($data.sqlCondition=='IN'){
        $out+='selected=selected ';
        }
        $out+='>in</option>\n        ';
        }
        $out+='\n    </select>\n    <input type="text" class="default-value" value="';
        $out+=$escape($data.defaultValue);
        $out+='" placeholder="默认值"/>\n</div>';
        return $out;
    }
    return { render: anonymous };
});