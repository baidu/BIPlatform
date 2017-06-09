define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$each=$utils.$each,$item=$data.$item,index=$data.index,$escape=$utils.$escape,$out='';$out+='<ul class="comp-setting-charticons">\r\n    ';
        $each($data,function($item,index){
        $out+='\r\n        ';
        if($item === false){
        $out+='\r\n            <li><span class="icon ';
        $out+=$escape(index);
        $out+='" chart-type="';
        $out+=$escape(index);
        $out+='"></span></li>\r\n        ';
        }else{
        $out+='\r\n            <li><span class="icon ';
        $out+=$escape(index);
        $out+=' ';
        $out+=$escape(index);
        $out+='-focus" chart-type="';
        $out+=$escape(index);
        $out+='"></span></li>\r\n        ';
        }
        $out+='\r\n    ';
        });
        $out+='\r\n</ul>';
        return $out;
    }
    return { render: anonymous };
});