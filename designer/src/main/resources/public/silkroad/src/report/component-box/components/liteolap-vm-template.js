define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$escape=$utils.$escape,id=$data.id,$out='';$out+='<!--拖拽区域-->\r\n<div data-o_o-di="';
        $out+=$escape(id);
        $out+='.vctnr-fold">\r\n</div>\r\n<div class="di-o_o-line">\r\n    <div class="di-o_o-item" data-o_o-di="';
        $out+=$escape(id);
        $out+='.vpt-fold-ctrlbtn">\r\n    </div>\r\n</div>\r\n<div data-o_o-di="';
        $out+=$escape(id);
        $out+='.vpt-fold-body">\r\n    <div class="ka-block-table-meta" data-o_o-di="';
        $out+=$escape(id);
        $out+='.cnpt-table-meta">\r\n        <div class="ka-table-meta" data-o_o-di="';
        $out+=$escape(id);
        $out+='.vu-table-meta">\r\n        </div>\r\n    </div>\r\n</div>\r\n';
        return $out;
    }
    return { render: anonymous };
});