define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,factTables=$data.factTables,$each=$utils.$each,$item=$data.$item,$index=$data.$index,$escape=$utils.$escape,regexps=$data.regexps,$itemReg=$data.$itemReg,index=$data.index,separateTableRuleData=$data.separateTableRuleData,sepIndex=$data.sepIndex,$itemChildren=$data.$itemChildren,$out='';$out+='<div class="title fs-14">请选择要使用的事实表（可多选）</div>\r\n';
        if(factTables.length==0){
        $out+='\r\n    <div class="empty-data ta-c">暂无数据表</div>\r\n';
        }else{
        $out+='\r\n<ul>\r\n    ';
        $each(factTables,function($item,$index){
        $out+='\r\n    <li class="data-line c-p';
        if($item.selected){
        $out+=' selected';
        }
        $out+=' j-item" data-id="';
        $out+=$escape($item.id);
        $out+='">';
        $out+=$escape($item.name);
        $out+='</li>\r\n    ';
        });
        $out+='\r\n</ul>\r\n<div class="con-set-group j-root-set-group">\r\n    <span class="btn-has-icon btn-has-icon-info c-p j-set-group">添加分表匹配规则</span>\r\n    <span class="cor-red">（注意：同一规则只需提供一张表）</span>\r\n    ';
        $each(regexps,function($itemReg,index){
        $out+='\r\n    <div class="form-common-line j-regexps-item">\r\n        <div class="form-common-text form-common-text-xl">\r\n            <select class="form-common-select-small w-100 mt-1 j-select-table">\r\n                ';
        $each(factTables,function($item,$index){
        $out+='\r\n                    ';
        if($item.selected){
        $out+='\r\n                    <option value="';
        $out+=$escape($item.id);
        $out+='" ';
        if(index===$item.id){
        $out+='selected=selected';
        }
        $out+='>';
        $out+=$escape($item.name);
        $out+='</option>\r\n                    ';
        }
        $out+='\r\n                ';
        });
        $out+='\r\n            </select>\r\n            <select class="form-common-select-small w-100 mt-1 j-select-area-date">\r\n                ';
        $each(separateTableRuleData,function($item,sepIndex){
        $out+='\r\n                <option value="';
        $out+=$escape($item.value);
        $out+='" ';
        if($itemReg.type===$item.value){
        $out+='selected=selected';
        }
        $out+='>';
        $out+=$escape($item.text);
        $out+='</option>\r\n                ';
        });
        $out+='\r\n            </select>\r\n            <select class="form-common-select-small w-100 mt-1 j-select-area-date-children">\r\n                ';
        $each(separateTableRuleData,function($item,sepIndex){
        $out+='\r\n                    ';
        if($itemReg.type===$item.value){
        $out+='\r\n                        ';
        $each($item.children,function($itemChildren,$index){
        $out+='\r\n                            <option value="';
        $out+=$escape($itemChildren.value);
        $out+='" ';
        if($itemReg.condition===$itemChildren.value){
        $out+='selected="selected"';
        }
        $out+='>';
        $out+=$escape($itemChildren.text);
        $out+='</option>\r\n                        ';
        });
        $out+='\r\n                    ';
        }
        $out+='\r\n                ';
        });
        $out+='\r\n            </select>\r\n            <input type="text" class="form-common-text-small j-input-table-prefix" placeholder="表前缀" value="';
        $out+=$escape($itemReg.prefix);
        $out+='"/>\r\n            <span class="form-common-btn-extend form-common-btn-extend-absolute j-delete" title="删除">×</span>\r\n        </div>\r\n    </div>\r\n    ';
        });
        $out+='\r\n    <div class="cor-red hide j-regexps-validate mt-10"></div>\r\n</div>\r\n<div class="form-common-line hide j-root-set-group-template">\r\n    <div class="form-common-text form-common-text-xl">\r\n        <!--<input type="text" class="" placeholder="分表匹配规则"/>-->\r\n        <select class="form-common-select-small w-100 mt-1 j-select-table">\r\n            ';
        $each(factTables,function($item,$index){
        $out+='\r\n            <option value="';
        $out+=$escape($item.id);
        $out+='">';
        $out+=$escape($item.name);
        $out+='</option>\r\n            ';
        });
        $out+='\r\n        </select>\r\n        <select class="form-common-select-small w-100 mt-1 j-select-area-date">\r\n            ';
        $each(separateTableRuleData,function($item,$index){
        $out+='\r\n            <option value="';
        $out+=$escape($item.value);
        $out+='">';
        $out+=$escape($item.text);
        $out+='</option>\r\n            ';
        });
        $out+='\r\n        </select>\r\n        <select class="form-common-select-small w-100 mt-1 j-select-area-date-children">\r\n            ';
        $each(separateTableRuleData.time.children,function($item,$index){
        $out+='\r\n            <option value="';
        $out+=$escape($item.value);
        $out+='">';
        $out+=$escape($item.text);
        $out+='</option>\r\n            ';
        });
        $out+='\r\n        </select>\r\n        <input type="text" class="form-common-text-small j-input-table-prefix" placeholder="表前缀" value="';
        $out+=$escape($item);
        $out+='"/>\r\n        <span class="form-common-btn-extend form-common-btn-extend-absolute j-delete" title="删除">×</span>\r\n    </div>\r\n</div>\r\n';
        }
        $out+='\r\n\r\n\r\n';
        return $out;
    }
    return { render: anonymous };
});