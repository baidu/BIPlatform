define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$each=$utils.$each,componentList=$data.componentList,group=$data.group,$index=$data.$index,$escape=$utils.$escape,item=$data.item,$out='';$out+='<div class="con-component-box j-con-component-box">\r\n\r\n    <!--<select id="component-group-selector">-->\r\n        <!--';
        $each(componentList,function(group,$index){
        $out+='-->\r\n        <!--<option value="';
        $out+=$escape(group.id);
        $out+='">';
        $out+=$escape(group.caption);
        $out+='</option>-->\r\n        <!--';
        });
        $out+='-->\r\n    <!--</select>-->\r\n\r\n    ';
        $each(componentList,function(group,$index){
        $out+='\r\n    <div class="con-component j-con-component" data-group-id="';
        $out+=$escape(group.id);
        $out+='">\r\n        ';
        $each(group.items,function(item,$index){
        $out+='\r\n        <div class="component-item ';
        $out+=$escape(item.iconClass);
        $out+=' j-component-item" data-component-type="';
        $out+=$escape(item.type);
        $out+='" data-mold="';
        $out+=$escape(item.mold);
        $out+='" data-set-all="';
        $out+=$escape(item.defaultValue);
        $out+='">\r\n            <img src="src/css/img/global-menu/comp-menu-';
        $out+=$escape(item.iconClass);
        $out+='.png"/>\r\n            ';
        $out+=$escape(item.caption);
        $out+='\r\n        </div>\r\n        ';
        });
        $out+='\r\n    </div>\r\n    ';
        });
        $out+='\r\n</div>';
        return $out;
    }
    return { render: anonymous };
});