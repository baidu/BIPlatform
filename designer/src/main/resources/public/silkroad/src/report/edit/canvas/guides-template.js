define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$out='';$out+='<div class="guide-line guide-top j-guide-line"></div>\r\n<div class="guide-line guide-right j-guide-line"></div>\r\n<div class="guide-line guide-bottom j-guide-line"></div>\r\n<div class="guide-line guide-left j-guide-line"></div>';
        return $out;
    }
    return { render: anonymous };
});