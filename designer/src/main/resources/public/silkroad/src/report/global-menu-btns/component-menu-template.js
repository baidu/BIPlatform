define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$out='';$out+='<div class="global-menus component-menu j-all-menus" id="component">\r\n</div>\r\n<div class="global-menus skin-menu j-all-menus" id="skin-report">\r\n    <div class="skin-menu-box">\r\n        <div class=\'skin-type j-skin-btn\' id="di">\r\n            <div class="classic-skin skin-pic"></div>\r\n            <div class="skin-name">经典</div>\r\n        </div>\r\n        <div class=\'skin-type j-skin-btn\' id="bb">\r\n            <div class="lightBlue-skin skin-pic"></div>\r\n            <div class="skin-name">浅蓝</div>\r\n        </div>\r\n    </div>\r\n</div>';
        return $out;
    }
    return { render: anonymous };
});