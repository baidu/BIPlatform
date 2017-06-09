/**
 * 编译HTML模板成js模块文件
 *
 * @param {string} templateFilePath HTML木耙文件路径
 */
var fs = require('fs');
var config = require('./config');

/**
 * 日志输出
 *
 * @param {string} templateFilePath 模板文件路径
 * @param {Object} err 错误对象
 */
function printLog (templateFilePath, err) {
    if (err) {
        console.log(templateFilePath + '编译失败');
        console.log(err);
    } else {
        var str = templateFilePath.replace(config.projectPath, '');
        console.log(str + '编译完成');
    }
}

/**
 * 编译HTML模板成js模块文件
 *
 * @param {string} templateFilePath HTML木耙文件路径
 */
function compileTemplate(templateFilePath) {
    var templateEngine = require(config.templateEnginePath);
    // 美化格式 与 问题修正
    var templateReplaceArr = config.templateReplaceArr;
    var header = config.templateHeader;
    var foot = config.templateFoot;

    if (typeof fileArray == 'string') {
        fileArray = new Array(fileArray);
    }

    var templateJSFilePath = templateFilePath.replace('.html', '.js');
    var templateContent = fs.readFileSync(templateFilePath, "utf-8");

    try {
        var compileResult = templateEngine.compile(templateContent) + '';
        var item;
        for (var i = 0, len = templateReplaceArr.length; i < len; i++) {
            item = templateReplaceArr[i];
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
                    printLog(templateFilePath, err);
                });
        })(templateFilePath);
    }
    else {
        // 写文件
        (function (templateFilePath) {
            fs.writeFile(templateJSFilePath, compileResult, function (err) {
                printLog(templateFilePath, err);
            });
        })(templateFilePath);
    }
}

module.exports = compileTemplate;