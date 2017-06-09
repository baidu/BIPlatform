define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$escape=$utils.$escape,text=$data.text,name=$data.name,$out='';$out+='<div class="set-report-name t-c j-set-report-name">\r\n    <div class="text">\r\n        ';
        $out+=$escape(text);
        $out+='\r\n    </div>\r\n    <div class="form-common-text form-common-text-230">\r\n        <input type="text" class="j-report-name" placeholder="" value="';
        $out+=$escape(name);
        $out+='"/>\r\n        <span class="form-common-text-validation hide j-validation"></span>\r\n    </div>\r\n</div>';
        return $out;
    }
    return { render: anonymous };
});