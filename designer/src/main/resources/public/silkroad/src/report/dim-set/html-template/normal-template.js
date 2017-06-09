define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$each=$utils.$each,dim=$data.dim,$cube=$data.$cube,i=$data.i,$escape=$utils.$escape,cubes=$data.cubes,j=$data.j,$dim=$data.$dim,$index=$data.$index,relationTables=$data.relationTables,$relationTable=$data.$relationTable,$field=$data.$field,$out='';$out+='<div class="dim-container-normal">\r\n    <ul class="normal-column-names c-f">\r\n        <li><span>主数据表</span></li>\r\n        <li><span>主表字段</span></li>\r\n        <li><span>关联数据表</span></li>\r\n        <li><span>关联表字段</span></li>\r\n    </ul>\r\n    <div class="normal-main j-normal-main">\r\n        <!--循环cube列表（dim.normal）-->\r\n        ';
        $each(dim.normal,function($cube,i){
        $out+='\r\n        <div class="normal-main-box c-f j-normal-main-box">\r\n            <span class="cube-name" cubeId =';
        $out+=$escape($cube.cubeId);
        $out+=' title=';
        $out+=$escape(cubes[$cube.cubeId].name);
        $out+='>';
        $out+=$escape(cubes[$cube.cubeId].name);
        $out+='</span>\r\n            <span class="normal-cube-open j-normal-cube-open"></span>\r\n            <div class="normal-relation-container j-normal-relation-container c-f">\r\n                <!--循环cube中的行（dim.normal.children）-->\r\n                ';
        $each($cube.children,function($line,j){
        $out+='\r\n                <div class="normal-relation-box j-normal-relation-box">\r\n                    ';
        if(j !==0 ){
        $out+='\r\n                        <span class="normal-broken-line"></span>\r\n                    ';
        }
        $out+='\r\n                    <select class="normal-relation-box-select-fields mr-20">\r\n                        <option value="0">请选择</option>\r\n                        <!-- 循环每一行中的主表字段(cubes.cube1.currDims)-->\r\n                        ';
        $each(cubes[$cube.cubeId].currDims,function($dim,$index){
        $out+='\r\n                        <option value=';
        $out+=$escape($dim.name);
        $out+='\r\n                            ';
        if($dim.name === $line.currDim){
        $out+='selected="selected"\r\n                            ';
        }
        $out+='>';
        $out+=$escape($dim.comment);
        $out+='\r\n                        </option>\r\n                        ';
        });
        $out+='\r\n                    </select>\r\n                    <span class="equal">=</span>\r\n                    <select class="normal-relation-box-select-table mr-30 j-normal-relation-table-select" >\r\n                        <option value="0">请选择</option>\r\n                        <!-- 循环关联数据表(relationTables)-->\r\n                        ';
        $each(relationTables,function($relationTable,$index){
        $out+='\r\n                        <option value=';
        $out+=$escape($relationTable.name);
        $out+='\r\n                        ';
        if($relationTable.name === $line.relationTable){
        $out+='selected="selected"\r\n                        ';
        }
        $out+='>';
        $out+=$escape($relationTable.name);
        $out+='\r\n                        </option>\r\n                        ';
        });
        $out+='\r\n                    </select>\r\n                    <select class="normal-relation-box-select-fields mr-10">\r\n                        <option value="0">请选择</option>\r\n                        <!-- 循环关联数据表(relationTables)-->\r\n                        ';
        $each(relationTables,function($relationTable,$index){
        $out+='\r\n                        <!-- 如果关联数据表等于当前行的的关联表,那么就循环此关联表中的字段-->\r\n                        ';
        if($relationTable.name === $line.relationTable){
        $out+='\r\n                            ';
        $each($relationTable.fields,function($field,$index){
        $out+='\r\n                                <option value=';
        $out+=$escape($field.name);
        $out+='\r\n                                ';
        if($field.name === $line.field){
        $out+='selected="selected"\r\n                                ';
        }
        $out+='>';
        $out+=$escape($field.comment);
        $out+='\r\n                                </option>\r\n                            ';
        });
        $out+='\r\n                        ';
        }
        $out+='\r\n                        ';
        });
        $out+='\r\n                    </select>\r\n                    <span class="delete j-normal-delete"></span>\r\n                    ';
        if($cube.children.length === (j+1)){
        $out+='\r\n                        <span class="add j-normal-add"></span>\r\n                    ';
        }
        $out+='\r\n                </div>\r\n\r\n                ';
        });
        $out+='\r\n            </div>\r\n            <span class="normal-error-msg j-normal-error-msg hide"></span>\r\n        </div>\r\n        ';
        });
        $out+='\r\n    </div>\r\n    <span class="prompt mt-30">注：建立关联后，默认将关联表的所有字段全部取出</span>\r\n</div>';
        return $out;
    }
    return { render: anonymous };
});