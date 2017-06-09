define(['template'], function (template) {
    function anonymous($data,$filename
        /**/) {
        'use strict';
        $data=$data||{};
        var $utils=template.utils,$helpers=$utils.$helpers,$each=$utils.$each,option=$data.option,$index=$data.$index,$escape=$utils.$escape,$out='';$out+='<!-- 其他操作 -->\n<div class="data-format">\n    <div class="data-format-alone c-f">\n        <div class="data-format-black f-l c-f w-p-100">\n            <input type="checkbox" name="isPagination" class="f-l c-p j-isPagination" ';
        if($data.isPagination==true){
        $out+='\n                   checked="checked" ';
        }
        $out+=' />\n            <label class="f-l ml-2">显示分页</label>\n        </div>\n        <div class="data-format-black f-l c-f w-p-100 j-isPaginationBox ';
        if($data.isPagination!=true){
        $out+='hide';
        }
        $out+='">\n            <label class="f-l">请输入页记录数：</label>\n            <input class="f-l input-text ml-15 w-100 j-pageSize" type="text" placeholder="请输入数字"/>\n            <span type="button" class="form-common-input-button f-l ml-5 j-new-pageSize">新增页码</span>\n        </div>\n        <div class="data-format-black f-l c-f w-p-100 j-isPaginationBox ';
        if($data.isPagination!=true){
        $out+='hide';
        }
        $out+='">\n            <label class="f-l">默认页记录数：</label>\n            <select class="f-l ml-25 h-25 w-105 c-p j-pageSizeOptions">\n                ';
        $each($data.pageSizeOptions,function(option,$index){
        $out+='\n                <option value="';
        $out+=$escape(option);
        $out+='" ';
        if(option==$data.pageSize){
        $out+='selected="selected"';
        }
        $out+='>';
        $out+=$escape(option);
        $out+='</option>\n                ';
        });
        $out+='\n            </select>\n            <span type="button" class="form-common-input-button f-l ml-5 j-reset-pageSize">恢复默认</span>\n        </div>\n        <div class="data-format-black f-l c-f w-p-100 j-notPaginationBox ';
        if($data.isPagination==true){
        $out+='hide';
        }
        $out+='">\n            <label class="f-l">请输入默认记录数：</label>\n            <input class="f-l input-text w-150" type="text" ';
        if($data.isPagination!=true){
        $out+='value=';
        $out+=$escape($data.pageSize);
        }
        $out+=' placeholder="请输入数字"/>\n        </div>\n        <div class="data-format-black f-l c-f w-p-100 cor-red hide j-pagination-error-msg"></div>\n    </div>\n</div>';
        return $out;
    }
    return { render: anonymous };
});