define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$escape=$utils.$escape,id=$data.id,$out='';$out+='<!--table区域-->\n<div class="comp-box di-o_o-block" data-o_o-di="';
        $out+=$escape(id);
        $out+='">\n    <!--<div class="di-o_o-line">-->\n        <!--<div id="';
        $out+=$escape(id);
        $out+='-exhibition" class="ui-table-fieldset-exhibition"></div>-->\n    <!--</div>-->\n    <div class="di-o_o-line">\n        <!--<div data-o_o-di="';
        $out+=$escape(id);
        $out+='-vu-table-fieldsFilter"></div>-->\n        <!--<div class="di-o_o-item" data-o_o-di="';
        $out+=$escape(id);
        $out+='-vu-table-download" style="display:inline-block;float:right;"></div>-->\n        <div class="di-o_o-item" data-o_o-di="';
        $out+=$escape(id);
        $out+='-vu-table-download" style="display:inline-block;"></div>\n    </div>\n    <div class="di-o_o-line">\n        <div class="vu-plane-table" data-o_o-di="';
        $out+=$escape(id);
        $out+='-vu-table" style="height: 160px;"></div>\n    </div>\n    <div class="di-o_o-line">\n        <div class="" data-o_o-di="';
        $out+=$escape(id);
        $out+='-vu-table-pager"></div>\n    </div>\n\n</div>';
        return $out;
    }
    return { render: anonymous };
});