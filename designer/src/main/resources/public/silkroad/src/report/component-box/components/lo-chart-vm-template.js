define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$escape=$utils.$escape,id=$data.id,$out='';$out+='<!-- liteolap 图 -->\r\n<div data-o_o-di="';
        $out+=$escape(id);
        $out+='.cnpt-liteolapchart-meta">\r\n    <div class="di-o_o-line">\r\n        <span class="di-o_o-item">选择指标：</span>\r\n        <span class="di-o_o-item" data-o_o-di="';
        $out+=$escape(id);
        $out+='.vu-liteolapchart-meta">\r\n        </span>\r\n    </div>\r\n</div>\r\n<div class="di-o_o-block" data-o_o-di="';
        $out+=$escape(id);
        $out+='.cnpt-liteolapchart">\r\n    <div data-o_o-di="';
        $out+=$escape(id);
        $out+='.vu-liteolapchart" style="height: 260px">\r\n    </div>\r\n</div>';
        return $out;
    }
    return { render: anonymous };
});