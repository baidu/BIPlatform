define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$each=$utils.$each,dim=$data.dim,$cube=$data.$cube,i=$data.i,$escape=$utils.$escape,cubes=$data.cubes,dateRelationTables=$data.dateRelationTables,$dateRelationTable=$data.$dateRelationTable,j=$data.j,$dim=$data.$dim,$index=$data.$index,defaultDate=$data.defaultDate,$level=$data.$level,$format=$data.$format,$field=$data.$field,$out='';$out+='<div class="dim-container-date hide j-date-main">\r\n    <ul class="date-column-names c-f">\r\n        <li class="date-column-names-main-table"><span>主数据表</span></li>\r\n        <li class="date-column-names-setting"><span>配置区</span></li>\r\n    </ul>\r\n    <!--循环cube列表（dim.normal）-->\r\n    ';
        $each(dim.date,function($cube,i){
        $out+='\r\n    <div class="date-main-box c-f j-date-main-box">\r\n        <span class="cube-name" cubeId=';
        $out+=$escape($cube.cubeId);
        $out+=' title=';
        $out+=$escape(cubes[$cube.cubeId].name);
        $out+='>';
        $out+=$escape(cubes[$cube.cubeId].name);
        $out+='</span>\r\n        <span class="straight-line"></span>\r\n        ';
        if($cube.children[0].relationTable === "0" || $cube.children[0].relationTable === "ownertable"){
        $out+='\r\n        <!--内置维度-->\r\n        <div class="date-relation-owner">\r\n            <div class="date-relation-owner-first-part c-f">\r\n                <span>选择被关联表：</span>\r\n                <select class="j-relation-table-select">\r\n                    <option value="0">请选择</option>\r\n                    <option value="ownertable"\r\n                    ';
        if($cube.children[0].relationTable==="ownertable"){
        $out+='\r\n                    selected = "selected"';
        }
        $out+='>内置表</option>\r\n                    ';
        $each(dateRelationTables,function($dateRelationTable,j){
        $out+='\r\n                    <option value=';
        $out+=$escape($dateRelationTable.name);
        $out+='>';
        $out+=$escape($dateRelationTable.comment);
        $out+='</option>\r\n                    ';
        });
        $out+='\r\n                </select>\r\n            </div>\r\n            <div class="date-relation-owner-two-part c-f j-date-two-part">\r\n                <span>选择时间字段：</span>\r\n                <select>\r\n                    <option value="0">请选择</option>\r\n                    ';
        $each(cubes[$cube.cubeId].currDims,function($dim,$index){
        $out+='\r\n                    <option value=';
        $out+=$escape($dim.name);
        $out+='\r\n                    ';
        if($dim.name === $cube.children[0].currDim){
        $out+='selected="selected"\r\n                    ';
        }
        $out+='>';
        $out+=$escape($dim.comment);
        $out+='\r\n                    </option>\r\n                    ';
        });
        $out+='\r\n                </select>\r\n                <span>粒度：</span>\r\n                <select class="j-owner-date-level-select">\r\n                    <option value="0">请选择</option>\r\n                    ';
        $each(defaultDate.level,function($level,$index){
        $out+='\r\n                    <option value=';
        $out+=$escape($level.id);
        $out+='\r\n                    ';
        if($level.id === $cube.children[0].field){
        $out+='selected="selected"\r\n                    ';
        }
        $out+='>';
        $out+=$escape($level.name);
        $out+='\r\n                    </option>\r\n                    ';
        });
        $out+='\r\n                </select>\r\n                <span>时间格式：</span>\r\n                <select class="j-owner-date-type-select">\r\n                    <option value="0">请选择</option>\r\n                    ';
        $each(defaultDate.level,function($level,$index){
        $out+='\r\n                    ';
        if($level.id === $cube.children[0].field){
        $out+='\r\n                    ';
        $each(defaultDate.dateFormatOptions[$cube.children[0].field],function($format,$index){
        $out+='\r\n                    <option value=';
        $out+=$escape($format);
        $out+='\r\n                    ';
        if($format === $cube.children[0].format){
        $out+='selected="selected"\r\n                    ';
        }
        $out+='>';
        $out+=$escape($format);
        $out+='</option>\r\n                    ';
        });
        $out+='\r\n                    ';
        }
        $out+='\r\n                    ';
        });
        $out+='\r\n                </select>\r\n            </div>\r\n        </div>\r\n        ';
        }else{
        $out+='\r\n        <!--普通维度-->\r\n        <div class="date-relation-normal">\r\n            <div class="first-part c-f">\r\n                <span>选择被关联表：</span>\r\n                <!--内置表为0-->\r\n                <select class="j-relation-table-select">\r\n                    <option value="0">请选择</option>\r\n                    <option value="ownertable"\r\n                    ';
        if($cube.children[0].relationTable==="ownertable"){
        $out+='\r\n                    selected = "selected"';
        }
        $out+='>内置表</option>\r\n                    ';
        $each(dateRelationTables,function($dateRelationTable,j){
        $out+='\r\n                    <option value=';
        $out+=$escape($dateRelationTable.id);
        $out+='\r\n                    ';
        if($dateRelationTable.id === $cube.children[0].relationTable){
        $out+='\r\n                    selected = "selected"\r\n                    ';
        }
        $out+='>';
        $out+=$escape($dateRelationTable.name);
        $out+='</option>\r\n                    ';
        });
        $out+='\r\n                </select>\r\n            </div>\r\n            <div class="date-relation-normal-two-part c-f j-date-two-part">\r\n                <span>指定关联字段：</span>\r\n                <select>\r\n                    <option value="0">请选择</option>\r\n                    ';
        $each(cubes[$cube.cubeId].currDims,function($dim,$index){
        $out+='\r\n                    <option value=';
        $out+=$escape($dim.id);
        $out+='\r\n                    ';
        if($dim.id === $cube.children[0].currDim){
        $out+='selected="selected"\r\n                    ';
        }
        $out+='>';
        $out+=$escape($dim.name);
        $out+='\r\n                    </option>\r\n                    ';
        });
        $out+='\r\n                </select>\r\n                <span class="equal">=</span>\r\n\r\n                <select>\r\n                    <option value="0">请选择</option>\r\n                    ';
        $each(dateRelationTables,function($dateRelationTable,$index){
        $out+='\r\n                    ';
        if($dateRelationTable.id === $cube.children[0].relationTable){
        $out+='\r\n                    ';
        $each($dateRelationTable.fields,function($field,$index){
        $out+='\r\n                    <option value=';
        $out+=$escape($field.id);
        $out+='\r\n                    ';
        if($field.id === $cube.children[0].field){
        $out+='selected="selected"\r\n                    ';
        }
        $out+='>';
        $out+=$escape($field.name);
        $out+='\r\n                    </option>\r\n                    ';
        });
        $out+='\r\n                    ';
        }
        $out+='\r\n                    ';
        });
        $out+='\r\n                </select>\r\n            </div>\r\n            <!--需要去后台获取-->\r\n            <div class="date-relation-normal-three-part j-date-three-part">\r\n                <span class="date-relation-normal-three-part-name">日期格式：</span>\r\n                <div class="date-relation-normal-three-part-box c-f">\r\n                    ';
        $each(dateRelationTables,function($dateRelationTable,$index){
        $out+='\r\n                    ';
        if($dateRelationTable.id === $cube.children[0].relationTable){
        $out+='\r\n                    ';
        $each($dateRelationTable.fields,function($field,$index){
        $out+='\r\n                    <div class="date-relation-normal-three-part-box-date-format c-f">\r\n                        <span>';
        $out+=$escape($field.name);
        $out+='</span>\r\n                        <select>\r\n                            <option value="0">请选择</option>\r\n                            ';
        $each($dateRelationTable.dateFormatOptions[$field.id],function($format,$index){
        $out+='\r\n                            <option value="';
        $out+=$escape($format);
        $out+='" ';
        if($format === $cube.children[0].dateLevel[$field.id]){
        $out+=' selected="selected" ';
        }
        $out+='>';
        $out+=$escape($format);
        $out+='\r\n                            </option>\r\n                            ';
        });
        $out+='\r\n                        </select>\r\n                    </div>\r\n                    ';
        });
        $out+='\r\n                    ';
        }
        $out+='\r\n                    ';
        });
        $out+='\r\n                </div>\r\n            </div>\r\n        </div>\r\n        ';
        }
        $out+='\r\n        <span class="date-error-msg j-date-error-msg hide"></span>\r\n    </div>\r\n   ';
        });
        $out+='\r\n</div>\r\n\r\n';
        return $out;
    }
    return { render: anonymous };
});