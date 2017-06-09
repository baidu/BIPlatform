define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$each=$utils.$each,dimList=$data.dimList,$item=$data.$item,$index=$data.$index,$escape=$utils.$escape,$out='';$out+='<div class="j-global-attr">\r\n    <input type="text" class="parameter-name" placeholder="维度名称"/>\r\n    <select class="parameter-id">\r\n        ';
        $each(dimList,function($item,$index){
        $out+='\r\n        <option value="';
        $out+=$escape($item.id);
        $out+='">';
        $out+=$escape($item.caption);
        $out+='</option>\r\n        ';
        });
        $out+='\r\n    </select>\r\n    <input type="text" class="parameter-default" placeholder="默认值"/>\r\n    <input type="checkbox" class="parameter-needed"/>\r\n    <div class="j-global-close"></div>\r\n</div>';
        return $out;
    }
    return { render: anonymous };
});