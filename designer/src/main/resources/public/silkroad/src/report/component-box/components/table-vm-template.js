define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$escape=$utils.$escape,id=$data.id,$out='';$out+='<!--table区域-->\r\n<div class="comp-box di-o_o-block" data-o_o-di="';
        $out+=$escape(id);
        $out+='" style="overflow: visible">\r\n    <!--<div class="di-o_o-line">-->\r\n        <!--<div class="" data-o_o-di="';
        $out+=$escape(id);
        $out+='-vu-table-rich-select"></div>-->\r\n    <!--</div>-->\r\n    <div class="di-o_o-line">\r\n        <div class="" data-o_o-di="';
        $out+=$escape(id);
        $out+='-vu-table-breadcrumb"></div>\r\n    </div>\r\n    <div class="di-o_o-line">\r\n        <div class="vu-table" data-o_o-di="';
        $out+=$escape(id);
        $out+='-vu-table" style="height: 160px;"></div>\r\n    </div>\r\n    <div class="di-o_o-line">\r\n        <div class="di-table-prompt">\r\n            <div class="di-table-count" data-o_o-di="';
        $out+=$escape(id);
        $out+='-vu-table-count">\r\n    \r\n            </div>\r\n            <div class="di-table-down">\r\n                \r\n                <div class="di-o_o-line di-table-down-btn" title="下载全量数据" >\r\n                    <div class="di-o_o-item" data-o_o-di="';
        $out+=$escape(id);
        $out+='-vu-table-download" ></div>\r\n                </div>\r\n            </div>\r\n        </div>\r\n    </div>\r\n</div>';
        return $out;
    }
    return { render: anonymous };
});