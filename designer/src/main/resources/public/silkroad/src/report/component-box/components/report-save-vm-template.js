define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$escape=$utils.$escape,id=$data.id,$out='';$out+='<div class="di-o_o-line">\n    <div class="di-o_o-item" data-o_o-di="';
        $out+=$escape(id);
        $out+='-vu-tab-button">\n    </div>\n</div>\n<div class="di-o_o-item" data-o_o-di="';
        $out+=$escape(id);
        $out+='">\n    <div class="di-o_o-item" data-o_o-di="';
        $out+=$escape(id);
        $out+='-vu-save-button">\n    </div>\n</div>';
        return $out;
    }
    return { render: anonymous };
});