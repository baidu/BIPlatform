define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$out='';$out+='<ul class="br-3">\r\n    <!--<li class="br-3 data-sources-float-window-item j-show-data"><span>筛选显示数据</span></li>-->\r\n    <li class="br-3 data-sources-float-window-item j-change-data-sources"><span>修改数据模型</span></li>\r\n</ul>';
        return $out;
    }
    return { render: anonymous };
});