define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$escape=$utils.$escape,id=$data.id,$out='';$out+='<div data-o_o-di="';
        $out+=$escape(id);
        $out+='form" data-comp-id="comp-id-form">\r\n</div>';
        return $out;
    }
    return { render: anonymous };
});