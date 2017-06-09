define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$each=$utils.$each,dim=$data.dim,$cube=$data.$cube,i=$data.i,$escape=$utils.$escape,cubes=$data.cubes,$field=$data.$field,$index=$data.$index,j=$data.j,$out='';$out+='<div class="dim-container-custom hide">\r\n    <ul class="custom-column-names c-f">\r\n        <li class="custom-column-main-table"><span>主数据表</span></li>\r\n        <li class="custom-column-main-table-fields"><span>主表字段</span></li>\r\n        <li class="custom-column-create-dim"><span>关联数据表</span></li>\r\n    </ul>\r\n    <div class="custom-main j-custom-main">\r\n        ';
        $each(dim.custom,function($cube,i){
        $out+='\r\n        <div class="custom-main-box c-f j-custom-main-box">\r\n            <span class="cube-name" cubeId=';
        $out+=$escape($cube.cubeId);
        $out+=' title=';
        $out+=$escape(cubes[$cube.cubeId].name);
        $out+='>';
        $out+=$escape(cubes[$cube.cubeId].name);
        $out+='</span>\r\n            <span class="straight-line"></span>\r\n            <div class="custom-main-table-fields-box" bodyIndex=';
        $out+=$escape(i);
        $out+='>\r\n                    <ul>\r\n                        ';
        $each(cubes[$cube.cubeId].allFields,function($field,$index){
        $out+='\r\n                        <li class="j-custom-field" bodyIndex=';
        $out+=$escape(i);
        $out+='>';
        $out+=$escape($field.name);
        $out+='</li>\r\n                        ';
        });
        $out+='\r\n                    </ul>\r\n            </div>\r\n            <div class="custom-create-new-dim-container">\r\n                ';
        $each($cube.children,function($line,j){
        $out+='\r\n                <div class="custom-create-new-dim-box j-custom-relation-box">\r\n                    <div class="custom-create-new-dim-texts">\r\n                        <input type="text" placeholder="请输入新维度的名称" value="';
        $out+=$escape($line.dimName);
        $out+='" />\r\n                        <textarea placeholder="请输入创建维度的逻辑语句" class="j-custom-sql" bodyIndex=';
        $out+=$escape(i);
        $out+='>';
        $out+=$escape($line.sql);
        $out+='</textarea>\r\n                        <span class="';
        if($line.sql === ''){
        $out+='custom-create-new-dim-texts-wrong';
        }else{
        $out+='custom-create-new-dim-texts-right';
        }
        $out+='"></span>\r\n                    </div>\r\n                    <span class="delete j-custom-delete"></span>\r\n                    ';
        if(j === ($cube.children.length-1)){
        $out+='\r\n                    <span class="add j-custom-add"></span>\r\n                    ';
        }
        $out+='\r\n                </div>\r\n                ';
        });
        $out+='\r\n            </div>\r\n            <span class="custom-error-msg j-custom-error-msg hide"></span>\r\n        </div>\r\n        ';
        });
        $out+='\r\n    </div>\r\n    <span class="prompt mt-30">注：右下角的图标用于校验当前语句是否正确</span>\r\n</div>';
        return $out;
    }
    return { render: anonymous };
});