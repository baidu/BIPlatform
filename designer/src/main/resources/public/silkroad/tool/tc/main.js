/**
 * template-compiler 模板编译工具，基于node
 */
var fs = require('fs');
var config = require('./config');
var templateList = require('./get-listened-files');
var addTemplateWatch = require('./add-template-watch');
addTemplateWatch(templateList);