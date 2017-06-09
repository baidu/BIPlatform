/**
 * template-compiler 模板编译工具，基于node
 */

var fs = require('fs');

// 存放模板的文件夹路径
var templateDirectory = '../src'; //相对于当前文件的相对路径

// 模板文件的读取规则
var reg = /-template.html$/;

var templateFiles = readAllFile(templateDirectory, reg);
compileTemplate(templateFiles);

/*------------工具函数------------*/
/**
 * 读取指定文件夹下的全部文件，可通过正则进行过滤，返回文件路径数组
 * @param {String} root 指定文件夹路径
 * [@param] reg 对文件的过滤正则表达式,可选参数
 *
 * 注：还可变形用于文件路径是否符合正则规则，
 * 路径可以是文件夹，也可以是文件，对不存在的路径也做了容错处理*/
function readAllFile(root, reg) {
    var resultArr = [];
    var thisFn = arguments.callee;

    if (fs.existsSync(root)) {//文件或文件夹存在
        var stat = fs.lstatSync(root); // 对于不存在的文件或文件夹，此函数会报错

        if (stat.isDirectory()) {// 文件夹
            var files = fs.readdirSync(root);
            files.forEach(function (file) {
                var t = thisFn(root + '/' + file, reg);
                resultArr = resultArr.concat(t);
            });
        }
        else {
            if (reg !== undefined) {

                if (typeof reg.test == 'function' && reg.test(root)) {
                    resultArr.push(root);
                }
            }
            else {
                resultArr.push(root);
            }
        }
    }

    return resultArr;
}

/**
 * 编译模板
 * @param {Array} templateFiles 需要编译的html模板文件路径列表
 * */
function compileTemplate(templateFiles) {
    // 模板引擎的路径
    var template = require('../../dep/template-3.0.0.js');

    // 包装的头和尾
    var header = "define(['template'], function (template) {\r\n";
    var foot = "\r\n    return { render: anonymous };\r\n});";

    for (var i = 0, len = templateFiles.length; i < len; i++) {
        var templateFilePath = templateFiles[i];
        var templateJSFilePath = templateFilePath.replace('.html', '.js');
        var templateContent = fs.readFileSync(templateFilePath, "utf-8");

        try {
            var compileResult = template.compile(templateContent) + '';
            // 美化格式 与 问题修正
            var replaceArr = [
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
            ];
            //var replace = compileResult.replace;
            var item;
            for (var j = 0, jLen = replaceArr.length; j < jLen; j++) {
                item = replaceArr[j];
                compileResult = compileResult.replace(item.from, item.to);
            }
        }
        catch (e) {
            console.log('模板：' + templateFilePath + '编译错误');
        }

        // 加上外包装
        compileResult = header + compileResult;
        compileResult += foot;

        if (!fs.existsSync(templateJSFilePath)) {
            // 追加文件
            (function (templateFilePath) {
                fs.appendFile(templateFilePath.replace('.html', '.js'),
                    compileResult,
                    'utf8',
                    function (err) {
                        if (err) {
                            console.log(templateFilePath + '编译失败');
                            console.log(err);
                        } else {
                            console.log(templateFilePath + '编译完成');
                        }
                    });
            })(templateFilePath);
        }
        else {
            // 写文件
            (function (templateFilePath) {
                fs.writeFile(templateJSFilePath, compileResult, function (err) {
                    if (err) {
                        console.log(templateFilePath + '编译失败');
                        console.log(err);
                    }
                    else {
                        console.log(templateFilePath + '编译完成');
                    }
                });
            })(templateFilePath);
        }
    }
}