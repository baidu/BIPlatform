define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$each=$utils.$each,currDims=$data.currDims,$dim=$data.$dim,$index=$data.$index,$escape=$utils.$escape,relationTables=$data.relationTables,$relationTable=$data.$relationTable,$out='';$out+='<!--此模版需要数据\r\n var demoData = {\r\n        "currDims": [\r\n            {"id": "dim1", "name": "维度1"},\r\n            {"id": "dim2", "name": "维度2"},\r\n            {"id": "dim3", "name": "维度3"}\r\n        ],\r\n        "relationTables": [\r\n            {\r\n                id: "table1",\r\n                name: "表1",\r\n                // 指定关联字段全集\r\n                fields: [{id: "table1Fields1", name: "table1Fields1"},{id: "table1Fields2", name: "table1Fields2"}]\r\n            },\r\n            {\r\n                id: "table2",\r\n                name: "表2",\r\n                // 指定关联字段全集\r\n                fields: [{id: "table2Fields1", name: "table2Fields1"},{id: "table2Fields2", name: "table2Fields2"}]\r\n            }\r\n        ]\r\n };\r\n-->\r\n<div class="normal-relation-box j-normal-relation-box">\r\n    <span class="normal-broken-line"></span>\r\n    <select class="normal-relation-box-select-fields mr-20">\r\n        <!-- 循环每一行中的主表字段(cubes.cube1.currDims)-->\r\n        <option value="0">请选择</option>\r\n        ';
        $each(currDims,function($dim,$index){
        $out+='\r\n        <option value=';
        $out+=$escape($dim.name);
        $out+='>\r\n           ';
        $out+=$escape($dim.comment);
        $out+='\r\n        </option>\r\n        ';
        });
        $out+='\r\n    </select>\r\n    <span class="equal">=</span>\r\n    <select class="normal-relation-box-select-table mr-30 j-normal-relation-table-select" >\r\n        <!-- 循环关联数据表(relationTables)-->\r\n        <option value="0">请选择</option>\r\n        ';
        $each(relationTables,function($relationTable,$index){
        $out+='\r\n        <option value=';
        $out+=$escape($relationTable.id);
        $out+='>';
        $out+=$escape($relationTable.name);
        $out+='\r\n        </option>\r\n        ';
        });
        $out+='\r\n    </select>\r\n    <select class="normal-relation-box-select-fields mr-10">\r\n        <!-- 循环关联数据表(relationTables)-->\r\n        <option value="0">请选择</option>\r\n    </select>\r\n    <span class="delete j-normal-delete"></span>\r\n    <span class="add j-normal-add"></span>\r\n</div>\r\n';
        return $out;
    }
    return { render: anonymous };
});