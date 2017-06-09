/**
 * 相关参数的配置文件
 */
var projectPath = 'D:/workspace_git/bi-platform/designer/src/main/resources/public/silkroad/';
module.exports = {
    // 启动时是否自动编译扫描到到的模板
    autoCompile: true,
    // 项目路径
    projectPath: projectPath,
    // 监听的根目录（从此目录向下监听）
    rootPath: projectPath + 'src',
    // 模板引擎路径
    templateEnginePath: projectPath + 'dep/template-3.0.0',
    // 模板文件的匹配规则
    templateFileReg: /-template.html$/,
    // 包装模板渲染结果使其可以模块化的头和尾
    templateHeader: "define(['template'], function (template) {\r\n",
    templateFoot: "\r\n    return { render: anonymous };\r\n});",
    // 对模板中的部分内容做替换
    templateReplaceArr: [
        {
            from: /^/gm,
            to: '        '
        },
        {
            from: /^        /g,
            to: '    '
        },
        {
            from: /        \}$/g,
            to: '    }'
        },
        // 修正闭包中的对外不可调用的问题
        {
            from: 'var $utils=this',
            to: 'var $utils=template.utils'
        },
        {
            from: "'use strict';",
            to: "'use strict';\r\n        $data=$data||{};\r\n        "
        },
        {
            from: "return new String($out)",
            to: "return $out"
        }
    ]
};