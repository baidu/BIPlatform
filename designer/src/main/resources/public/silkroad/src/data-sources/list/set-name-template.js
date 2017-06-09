define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$escape=$utils.$escape,text=$data.text,name=$data.name,$out='';$out+='<div class="set-report-name t-c j-set-datasource-group-name">\n    <div class="text">\n        ';
        $out+=$escape(text);
        $out+='\n    </div>\n    <div class="form-common-text form-common-text-230">\n        <input type="text" class="j-data-sources-group-name" placeholder="" value="';
        $out+=$escape(name);
        $out+='"/>\n        <span class="form-common-text-validation hide j-validation"></span>\n    </div>\n</div>';
        return $out;
    }
    return { render: anonymous };
});