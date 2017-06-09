/**
 * 向文件列表中的每个文件添加监听
 *
 * @param {Array} fileArray 文件路径列表
 */
var fs = require('fs');
var config = require('./config');
var compileTemplate = require('./compile-template');

function watchHtmlTemplateChange(fileArray) {
    // 参数验证
    if ((!fileArray instanceof Array)) {
        console.log('Error：模板列表必须是Array');
        return;
    }
    if (fileArray.length == 0) {
        console.log('Alert:没有扫描到HTML模板');
        return;
    }

    for (var i = 0, len = fileArray.length; i < len; i++) {

        (function (templateFilePath) {
            fs.watch(templateFilePath, function (event, filename) {
                if (event == 'change') {
                    console.log('------| 通过监听修改执行的编译 |------');
                    compileTemplate(templateFilePath);
                }
            });
        })(fileArray[i]);

        // 初始化的时候是否编译模板
        if (config.autoCompile) {
            compileTemplate(fileArray[i]);
        }
    }

}

module.exports = watchHtmlTemplateChange;