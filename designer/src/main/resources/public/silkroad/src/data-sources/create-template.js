/**
 * @file:    数据源新建模块Html Template
 * @author:  lizhantong(lztlovely@126.com)
 */

define(['template'], function (template){

    //------------------------------------------
    // 常量 
    //------------------------------------------

    var STRING = {
        'ADDRESS_PLACEHOLDER': '例如：host:port 或 a.baidu.com',
        'ADDRESS_VALIDATE': '请输入正确格式的数据库地址(ip端口 或 域名)'

    };
    var groupNameSelect = [
        '<div class="form-common-line j-data-sources-info-group-name">',
            '<span class="form-common-label">数据源组类型：</span>',
            '<select class="form-common-select" name="groupId">','<select>',
        '</div>'
    ].join('');
    var groupNameInput = [
        '<div class="form-common-line j-data-sources-info-group-name">',
            '<span class="form-common-label">数据源组名称：</span>',
            '<div class="form-common-text form-common-text-big">',
                '<input type="text" name="groupId" value="{{name}}" ',
                'group-id="{{id}}" readonly="readonly"/>',
            '</div>',
        '</div>'
    ].join('');

    //------------------------------------------
    // html模版
    //------------------------------------------
    // 其他的模板语法 doc\syntax-simple.md

    var html = [
        '<div class="form-common data-sources-create j-data-sources-create">',
            '{{if id}} ', groupNameInput, ' {{else}}', groupNameSelect, '{{/if}}',
            '<div class="form-common-line">',
                '<span class="form-common-label">数据源名称：</span>',
                '<div class="form-common-text form-common-text-big">',
                    '<input type="text" class="j-input-datasource-name"',
                        ' name="name" value="{{if ds}}{{ds.name}}{{/if}}"/>',
                    '<span class="form-common-text-validation hide">数据源名称不能为空</span>',
               '</div>',
            '</div>',
            '<div class="form-common-line">',
                '<span class="form-common-label">数据源类型：</span>',
                '<select class="form-common-select j-input-datasource-type" name="type">',
                    '<option value="MYSQL" {{if ds&&ds.dataSourceType== "MYSQL"}} selected="true"{{/if}}',
                        '>MySQL</option>',
                    '<option value="MYSQL-DBPROXY" ',
                        '{{if ds&&ds.dataSourceType=="MYSQL-DBPROXY"}} selected="true"{{/if}}',
                        '>MySQL-DBPROXY</option>',
                    '<option value="HIVE" {{if ds&&ds.dataSourceType== "HIVE"}} selected="true"{{/if}}',
                        '>HIVE</option>',
                    '<option value="PALO" {{if ds&&ds.dataSourceType== "PALO"}} selected="true"{{/if}}',
                        '>PALO</option>',
                '<select>',
            '</div>',
            '<div class="form-common-line">',
                '<span class="form-common-label">数据库地址：</span>',
                '<div class="form-common-text form-common-text-big">',
                    '<input type="text" class="j-input-datasource-address" ',
                        'name="hostAndPort" value="{{if ds}}{{ds.hostAndPort}}{{/if}}" placeholder="',
                        STRING.ADDRESS_PLACEHOLDER,
                    '"/>',
                    '<span class="form-common-text-validation hide">',
                        STRING.ADDRESS_VALIDATE,
                    '</span>',
                    '<span class="form-common-btn-extend form-common-btn-extend-absolute j-add-address" ',
                        'title="添加备用数据库">+</span>',
                '</div>',
            '</div>',
            '{{if ds}}{{each ds.reserveAddress as $value}}',
            '{{if $value}}',
            '<div class="form-common-line">',
                '<span class="form-common-label">备库地址：</span>',
                '<div class="form-common-text form-common-text-big">',
                    '<input type="text" class="j-input-datasource-address" ',
                        'placeholder="', STRING.ADDRESS_PLACEHOLDER, '" value="{{$value}}" />',
                    '<span class="form-common-text-validation hide">',
                        STRING.ADDRESS_VALIDATE, '</span>',
                    '<span class="form-common-btn-extend form-common-btn-extend-absolute j-delete-address" ',
                    'title="删除">×</span>',
                '</div>',
            '</div>',
            '{{/if}}',
            '{{/each}}{{/if}}',
            '<div class="form-common-line j-datasource-database-box">',
                 '<span class="form-common-label">数据库：</span>',
                 '<div class="form-common-text form-common-text-big">',
                     '<input type="text" class="j-input-database" name="dbInstance" ',
                     'value="{{if ds}}{{ds.dbInstance}}{{/if}}" placeholder="请输入数据库名"/>',
                     '<span class="form-common-text-validation hide">数据库不能为空</span>',
                 '</div>',
            '</div>',
            '<div class="form-common-line j-datasource-userName-box">',
                '<span class="form-common-label">用户名：</span>',
                '<div class="form-common-text form-common-text-big">',
                     '<input type="text" class="j-input-datasource-userName" ',
                         'name="dbUser" value="{{if ds}}{{ds.dbUser}}{{/if}}" placeholder="请输入正确格式的用户名"/>',
                     '<span class="form-common-text-validation hide">用户名不能为空</span>',
                '</div>',
            '</div>',
            '<div class="form-common-line">',
                '<span class="form-common-label">密码：</span>',
                '<div class="form-common-text form-common-text-big">',
                '<input type="password" class="j-input-password" ',
                    'name="dbPwd" value="{{if ds}}{{ds.dbPwd}}{{/if}}" placeholder="请输入正确格式的密码"/>',
                    '<span class="form-common-text-validation hide">密码不能为空</span>',
                '</div>',
            '</div>',
            '<div class="form-common-line">',
                '<span class="form-common-label">高级属性：</span>',
                '<div class="form-common-properties j-advanced-properties">',
                '{{if ds&&ds.advancedProperties}}',
                '{{each ds.advancedProperties as $item}}',
                '<div class="j-item">',
                    '<label>名称：</label><input class="j-item-key" type="text" value={{$index}}>',
                    '<label>属性：</label><input class="j-item-value" type="text" value={{$item}}>',
                '</div>',
                '{{/each}}',
                '{{else}}',
                '<div class="j-item">',
                    '<label>名称：</label><input class="j-item-key" type="text" value="">',
                    '<label>属性：</label><input class="j-item-value" type="text" value="">',
                '</div>',
                '<div class="j-item">',
                    '<label>名称：</label><input class="j-item-key" type="text" value="">',
                    '<label>属性：</label><input class="j-item-value" type="text" value="">',
                    '</div>',
                '<div class="j-item">',
                    '<label>名称：</label><input class="j-item-key" type="text" value="">',
                    '<label>属性：</label><input class="j-item-value" type="text" value="">',
                '</div>',
                '{{/if}}',
                '</div>',
            '</div>',
            '<div class="form-common-line c-p c-link">',
                '<span class="form-common-label j-extend-line-link">',
                '高级选项',
                '<span class="icon-arrow icon-arrow-down j-icon-arrow"></span>',
                '</span>',
            '</div>',
            '<div class="form-common-line j-extend-line hide">',
                '<span class="form-common-label">数据库编码：</span>',
                '<select class="form-common-select j-input-datasource-encoding" name="encoding">',
                    '<option value="">请选择</option>',
                    '<option value="GBK" {{if ds&&ds.encoding== "GBK"}} selected="true"{{/if}}>GBK</option>',
                    '<option value="UTF-8" {{if ds&&ds.encoding== "UTF-8"}} selected="true"{{/if}}>UTF-8</option>',
                '<select>',
            '</div>',
            '<div class="form-common-line ta-c">',
                '<span class="button button-flat-primary m-20 j-button-submit">提交</span>',
                '<span class="button button-flat m-20 j-button-cancel">取消</span>',
            '</div>',
        '</div>',

        // 小片段
        '<div class="hide j-data-sources-part">',
            '<div class="form-common-line j-datasource-reserveAddress-moudle">',
                '<span class="form-common-label w-100">备库地址：</span>',
                '<div class="form-common-text form-common-text-big">',
                    '<input type="text" class="j-input-datasource-address" placeholder="',
                    STRING.ADDRESS_PLACEHOLDER,
                    '" />',
                    '<span class="form-common-text-validation hide">',
                        STRING.ADDRESS_VALIDATE,
                    '</span>',
                    '<span class="form-common-btn-extend ',
                    'form-common-btn-extend-absolute j-delete-address" title="删除">×</span>',
                '</div>',
            '</div>',
        '</div>'
    ].join('');

    return {
        render: template.compile(html)
    };
});