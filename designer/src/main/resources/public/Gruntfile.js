/**
 * grunt
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    构建程序
 * @author:  sushuang(sushuang)
 */

module.exports = function(grunt) {
    start();
    // test();

    // TODO ----------------------------------------------------------
    // (-) change skin

    /**
     * 外部传入参数：
     *  Tasks:
     *      createBiz
     *      prePublish
     *      release
     *  bizKey: 表示产品线key或者skin名
     *  skin: 默认为 di
     *      支持：bb(businessbridge)
     *  fromPhase:
     *  srcMode:
     *      min
     *      single
     *      debug
     *  mock:
     *      true
     *      false
     *  vtplName: 可以传多个vtplName（用-o_o-分割），如果值为"FALSE"表示所有vplName
     */

    // 外部定义的配置内容（repo-conf.json）
    // 单独定义是为了方便修改
    // 在prepareConf中单独解析是为了预处理，
    // 以及避免每次config.get时都做大量的模版解析
    var repoConf;
    // 即repoConf.prop，等同于grunt.config('prop')
    var prop;
    // 字典表。不仅供文件依赖的分析，而且会根据此生成js文件插入到程序代码中。
    var repoDict;

    // 简写
    var kindOf = grunt.util.kindOf;
    var arrayPush = Array.prototype.push;

    function start() {
        /**
         * 任务配置
         */
        grunt.initConfig(
            {
                pkg: grunt.file.readJSON('package.json'),
                // 变量和常量
                prop: (prop = {
                    dirBinBase: "silkroad",
                    dirBinCom: "report-ui/asset-d/-com-",
                    dirBinConsoleBase: "report-ui/asset",
                    dirBinStubBase: "report-ui/asset-p",
                    dirSrc: "report-ui/src",
                    dirSrcCore: "report-ui/src/core",
                    dirSrcCSS: "report-ui/src/css",
                    dirSrcImg: "report-ui/src/css/img",
                    dirDIModel: "report-ui/src/biz-shared/di.shared.model",
                    dirDIView: "report-ui/src/biz-shared/di.shared.ui",
                    dirVUIAdapter: "report-ui/src/biz-shared/di.shared.adapter",
                    dirInnerVUI: "report-ui/src/biz-shared/di.shared.vui",
                    dirECUICore: "report-ui/src/core/ecui/src-core",
                    dirECUIExt: "report-ui/src/core/ecui/src-ext",
                    dirXUI: "report-ui/src/core/xui-ui",
                    sBinComName: "-com-",
                    sVTPLDelimeter: "-o_o-",
                    sVTPLAll: "FALSE",
                    sCodeFlagBegin: "//==[DI=BEGIN]==[NIGEB=ID]==",
                    sCodeFlagEnd: "//==[DI=END]==[DNE=ID]==",
//                    srcEcharts: "report-ui/src/core/echarts/echarts-plain-original.js",
                    srcEchartsMap: "report-ui/src/core/echarts/echarts-all.js",
                    srcJquery: "report-ui/src/core/jquery/jquery-1.7.1.js",
                    srcRepoDict: "report-ui/src/biz-shared/di.config/repo-dict.js",
                    // silkroad端打包js
                    getDirBinProdBiz: function (bizKey) {
                        return [prop.dirBinBase, bizKey || prop.bizKey, prop.skin].join('/');
                    },
                    // silkroad端css
                    getDirBinProdCSS: function (bizKey) {
                        return [prop.dirBinBase, bizKey || prop.bizKey, prop.skin, 'css'].join('/');
                    },
                    // TODO:ailkroad端支持多套皮肤
                    getBinSilkRoadCSS: function (suffix, bizKey) {
                        // 表示依照srcMode
                        suffix === false && (suffix = '-' + prop.srcMode);
                        return [prop.dirBinBase, bizKey || prop.bizKey, prop.skin, 'css', '-di-silkroad' + (suffix || '') + '.css'].join('/');
                    },
                    // silkroad端打包img
                    getBinSilkRoadImg: function (skin, suffix, bizKey) {
                        return [prop.dirBinBase, bizKey || prop.bizKey, prop.skin, 'css', suffix].join('/');
                    },
                    // product端打包js
                    getBinProdJS: function (suffix, bizKey) {
                        suffix === false && (suffix = '-' + prop.srcMode);
                        return [prop.dirBinBase, bizKey || prop.bizKey, prop.skin, '-di-product' + (suffix || '') + '.js'].join('/');
                    },
                    // product端打包css
                    getBinProdCSS: function (suffix, bizKey) {
                        // 表示依照srcMode
                        suffix === false && (suffix = '-' + prop.srcMode);
                        return [prop.dirBinBase, bizKey || prop.bizKey, prop.skin, 'css', '-di-product' + (suffix || '') + '.css'].join('/');
                    },
                    // product端打包img
                    getDirBinProdImg: function (bizKey) {
                        return [prop.dirBinBase, bizKey || prop.bizKey, prop.skin, 'css', 'img'].join('/');
                    },
                    getBinProdAllJS: function (bizKey) {
                        return [prop.dirBinBase, bizKey || prop.bizKey, '-di-product*.js'].join('/');
                    },
                    getBinStubJS: function (suffix) {
                        suffix === false && (suffix = '-' + prop.srcMode);
                        return [prop.dirBinStubBase, 'di-stub' + (suffix || '') + '.js'].join('/');
                    },
                    prodAllImg: [
                        // TODO
                        // 后面精确一下
                        "<%= prop.dirBinCom %>/css/<%= prop.skin %>/img/*",
                        "!<%= prop.dirBinCom %>/css/<%= prop.skin %>/img/*source*",
                        "!<%= prop.dirBinCom %>/css/<%= prop.skin %>/img/*.psd"
                    ]
                }),
                // 任务配置
                clean: {
                    rebuildBiz: {
                        src: ['<%= prop.getBinProdAllJS() %>', '<%= prop.getDirBinProdCSS() %>/*.css', '<%= prop.getDirBinProdCSS() %>/img/*']
                    },
                    toStub: {
                        src: ['<%= prop.dirBinStubBase %>/di-stub*.js']
                    }
                },
                concat: {
                    createBiz: {
                        files: [
                            { src: '<%= gen.srcAllJS %>', dest: '<%= prop.getBinProdJS("-single") %>' },
                            { src: '<%= gen.srcAllCSS %>', dest: '<%= prop.getBinProdCSS("-single") %>' }
                        ]
                    },
                    toStub: {
                        files: [
                            { src: '<%= gen.srcAllJS %>', dest: '<%= prop.getBinStubJS("-single") %>' },
                        ]
                    }
                },
                jsDebugGen: {
                    createBiz: {
                        files: [
                            { src: '<%= gen.srcAllJS %>', dest: '<%= prop.getBinProdJS("-debug") %>' }
                        ]
                    },
                    toStub: {
                        files: [
                            { src: '<%= gen.srcAllJS %>', dest: '<%= prop.getBinStubJS("-debug") %>' }
                        ]
                    }
                },
                cssDebugGen: {
                    createBiz: {
                        files: [
                            { src: '<%= gen.srcAllCSS %>', dest: '<%= prop.getBinProdCSS("-debug") %>' }
                        ]
                    }
                },
                uglify: {
                    createBiz: {
                        files: [
                            { src: '<%= prop.getBinProdJS("-single") %>', dest: '<%= prop.getBinProdJS("-min") %>' }
                        ]
                    },
                    createSilkRoadBiz: {
                        files: [
                            {
                                expand: true,
                                cwd: 'silkroad/src',
                                src: '**/*.js',
                                dest: '<%= prop.getDirBinProdBiz("") %>'
                            }
                        ]
                    },
                    toStub: {
                        files: [
                            { src: '<%= prop.getBinStubJS("-single") %>', dest: '<%= prop.getBinStubJS("-min") %>' }
                        ]
                    },
                    // 这是马俊新添加的echarts
                    compressChartApiJs: {
                        src: ['src/core/echarts/dichart.js', 'src/core/echarts/echarts-plain-original.js'],
                        dest: 'asset-p/di-chart.js'
                    }
                },
                cssmin: {
                    createBiz: {
                        files: [
                            { src: '<%= prop.getBinProdCSS("-single") %>', dest: '<%= prop.getBinProdCSS("-min") %>' }
                        ]
                    },
                    createSilkRoadBiz: {
                        beautify: {
                            //中文ascii化，非常有用！防止中文乱码的神配置
                            ascii_only: true
                        },
                        files: [
                            {
                                src: [
                                    "silkroad/src/css/all.css",
                                ],
                                dest: '<%= prop.getBinSilkRoadCSS("-min") %>'
                            }
                        ]
                    }
                },
                copy: {
                    createBiz: {
                        files: [
                            { src: '<%= prop.getBinProdJS(false) %>', dest: '<%= prop.getBinProdJS() %>', filter: 'isFile', notnull: true },
                            { src: '<%= prop.getBinProdCSS(false) %>', dest: '<%= prop.getBinProdCSS() %>', filter: 'isFile', notnull: true },
                            {
                                // TODO
                                // 后面精确一下
                                expand: true,
                                src: '<%= prop.prodAllImg %>',
                                dest: '<%= prop.getDirBinProdImg() %>/',
                                filter: 'isFile',
                                notnull: true,
                                flatten: true
                            }
                        ]
                    },
                    createSilkRoadBiz: {
                        files: [
                            {
                                expand: true,
                                notnull: true,
                                flatten: true,
                                src: 'silkroad/src/css/img/*',
                                dest: '<%= prop.getBinSilkRoadImg("img") %>/'

                            },
                            {
                                expand: true,
                                notnull: true,
                                flatten: true,
                                src: 'silkroad/src/css/img/publish-report-dialog-img/*',
                                dest: '<%= prop.getBinSilkRoadImg("img/publish-report-dialog-img") %>/'
                            },
                            {
                                expand: true,
                                notnull: true,
                                flatten: true,
                                src: 'silkroad/src/css/img/report-dim-set/*',
                                dest: '<%= prop.getBinSilkRoadImg("img/report-dim-set") %>/'
                            },
                            {
                                expand: true,
                                notnull: true,
                                flatten: true,
                                src: 'silkroad/src/css/component/jquery-ui/images/*',
                                dest: '<%= prop.getBinSilkRoadImg("images") %>/'
                            }
                        ]
                    },
                    toStub: {
                        files: [
                            { src: '<%= prop.getBinStubJS(false) %>', dest: '<%= prop.getBinStubJS() %>', filter: 'isFile', notnull: true }
                        ]
                    }
                }
            }
        );

        // 引用外部任务
        grunt.loadNpmTasks('grunt-contrib-clean');
        grunt.loadNpmTasks('grunt-contrib-copy');
        grunt.loadNpmTasks('grunt-contrib-concat');
        grunt.loadNpmTasks('grunt-contrib-uglify');
        grunt.loadNpmTasks('grunt-contrib-cssmin');

        // 入口tasks-report-ui
        grunt.registerTask('createBiz', taskCreateBiz);
        grunt.registerTask('rebuildBiz', taskRebuildBiz);
        grunt.registerTask('rebuildBizAllPhase', taskRebuildBizAllPhase);
        grunt.registerTask('toStub', taskToStub);

        // 入口tasks-silkroad
        grunt.registerTask('rebuildSilkRoadBiz', taskRebuildSilkRoadBiz);

        // 内部tasks
        grunt.registerTask('analyzeSrcJSBiz', taskAnalyzeSrcJSBiz);
        grunt.registerTask('analyzeSrcJSConsole', taskAnalyzeSrcJSConsole);
        grunt.registerTask('analyzeSrcJSStub', taskAnalyzeSrcJSStub);
        grunt.registerTask('analyzeSrcCSSBiz', taskAnalyzeSrcCSSBiz);
        grunt.registerTask('analyzeSrcCSSConsole', taskAnalyzeSrcCSSConsole);
        grunt.registerTask('analyzeVTPLBiz', taskAnalyzeVTPLBiz);
        grunt.registerMultiTask('jsDebugGen', taskJSDebugGen);
        grunt.registerMultiTask('cssDebugGen', taskCSSDebugGen);

    }

    //-----------------------------------------
    // 入口
    //-----------------------------------------
    function taskRebuildSilkRoadBiz(bizKey, skin, srcMode, mock) {
        prepareConf(
            // 输入验证
            checkBaseInput({
                bizKey: bizKey,
                srcMode: srcMode || 'min',
                skin: skin,
                rangeMode: 'prodAll',
                mock: mock || false
            })
        );
        // 先检查
        requireProp('bizKey', 'srcMode', 'rangeMode');
        //checkBizExists(prop.bizKey, false);

        runTask('uglify', 'createSilkRoadBiz');
        runTask('cssmin', 'createSilkRoadBiz');
        runTask('copy', 'createSilkRoadBiz');

    }
    /**
     * 入口任务：创建产品线
     */
    function taskCreateBiz(bizKey, skin, srcMode, mock) {
        prepareConf(
            // 输入验证
            checkBaseInput({
                bizKey: bizKey,
                srcMode: srcMode || 'min',
                skin: skin,
                rangeMode: 'prodAll',
                mock: mock || false
            })
        );

        // 先检查
        requireProp('bizKey', 'skin', 'srcMode', 'rangeMode');
        checkBizExists(prop.bizKey, false);
        checkSkinExists(prop.skin);

        // 然后分析
        runTask('analyzeSrcJSBiz');
        runTask('analyzeSrcCSSBiz');

        // 然后执行操作
        runTask('concat', 'createBiz');
        runTask('uglify', 'createBiz');
        runTask('cssmin', 'createBiz');
        runTask('jsDebugGen', 'createBiz');
        runTask('cssDebugGen', 'createBiz');
        runTask('copy', 'createBiz');
    }

    /**
     * 入口任务：重新构建某产品线的dev
     */
    function taskRebuildBiz(bizKey, skin, srcMode, mock) {
        prepareConf(
            // 输入验证
            checkBaseInput({
                bizKey: bizKey,
                srcMode: srcMode || 'min',
                mock: mock || false,
                skin: skin,
                rangeMode: 'prodAll'
            })
        );

        // 先检查
        requireProp('bizKey', 'skin', 'srcMode', 'rangeMode');
        checkBizExists(prop.bizKey, true);
        checkSkinExists(prop.skin);

        // 然后分析
        runTask('analyzeSrcJSBiz');
        runTask('analyzeSrcCSSBiz');

        // 然后执行操作
        runTask('clean', 'rebuildBiz');
        runTask('concat', 'createBiz');
        runTask('uglify', 'createBiz');
        runTask('cssmin', 'createBiz');
        runTask('jsDebugGen', 'createBiz');
        runTask('cssDebugGen', 'createBiz');
        runTask('copy', 'createBiz');
    }

    /**
     * 入口任务：重新构建某产品线的所有phase
     */
    function taskRebuildBizAllPhase(bizKey, skin, srcMode, mock) {
        // TODO
        requireProp('bizKey', 'skin', 'srcMode', 'rangeMode');
    }

    /**
     * 入口任务：重新构建所有产品线的所有phase
     */
    function taskRebuildAll(skin, srcMode, mock) {
        // TODO
        requireProp('skin', 'srcMode', 'rangeMode');
    }

    /**
     * 入口任务：di stub的构建
     */
    function taskToStub(srcMode) {
        prepareConf(
            // 输入验证
            checkBaseInput({
                srcMode: srcMode || 'min',
                rangeMode: 'stubAll'
            })
        );

        // 先检查
        requireProp('srcMode', 'rangeMode');

        // 然后分析
        runTask('analyzeSrcJSStub');

        // 然后执行操作
        runTask('clean', 'toStub');
        runTask('concat', 'toStub');
        runTask('uglify', 'toStub');
        runTask('jsDebugGen', 'toStub');
        runTask('copy', 'toStub');
    }

    //-----------------------------------------
    // 内部任务
    //-----------------------------------------

    /**
     * 根据文件依赖关系和depict引用关系，生成JS代码文件列表
     */
    function taskAnalyzeSrcJSBiz() {
        requireProp('_start', 'srcMode', 'bizKey', 'mock', 'rangeMode');

        prepareDict();

        // 分析某产品线的depict文件
        var depictInfo = analyzeDepicts(prop.bizKey, 'dev');
        // 得到要加载的文件列表
        var fileList = buildSrc(prop.rangeMode, depictInfo);

        grunt.config.set('gen.srcAllJS', fileList);
    }

    /**
     * 根据文件依赖关系和depict引用关系，生成JS代码文件列表
     */
    function taskAnalyzeSrcJSConsole() {
        requireProp('_start', 'srcMode', 'mock', 'rangeMode');

        // 得到要加载的文件列表
        var fileList = buildSrc(prop.rangeMode);

        grunt.config.set('gen.srcAllJS', fileList);
    }

    /**
     * 根据文件依赖关系和depict引用关系，生成JS代码文件列表
     */
    function taskAnalyzeSrcJSStub() {
        requireProp('_start', 'srcMode', 'rangeMode');

        // 得到要加载的文件列表
        var fileList = buildSrc(prop.rangeMode);

        grunt.config.set('gen.srcAllJS', fileList);
    }

    /**
     * 根据文件依赖关系和depict引用关系，生成CSS代码文件列表
     */
    function taskAnalyzeSrcCSSBiz() {
        requireProp('_start', 'srcMode', 'skin', 'bizKey', 'mock');

        prepareDict();

        // 对于CSS，暂时使用全局，后续分
        // TODO
        var cssList = grunt.file.expand(
            { filter: 'isFile' },
            repoConf.fileAliasDict.prodBaseCSS
        );
        grunt.config.set('gen.srcAllCSS', cssList);
    }

    /**
     * 根据文件依赖关系和depict引用关系，生成CSS代码文件列表
     */
    function taskAnalyzeSrcCSSConsole() {
        requireProp('_start', 'srcMode', 'mock');

        // 对于CSS，暂时使用全局，后续分
        // TODO
        var cssList = grunt.file.expand(
            { filter: 'isFile' },
            repoConf.fileAliasDict.consoleBaseCSS
        );
        grunt.config.set('gen.srcAllCSS', cssList);
    }

    /**
     * 生成VTPL代码文件列表
     */
    function taskAnalyzeVTPLBiz() {
        requireProp('_start', 'bizKey', 'fromPhase', 'vtplName');

        prepareDict();

        // 得到要处理的vtpl文件列表
        var vtplList = [];
        if (prop.vtplName == prop.sVTPLAll) {
            // 寻找某产品线所有模版路径
            arrayPush.apply(
                vtplList,
                grunt.file.expand(
                    { filter: 'isFile' },
                    [prop.dirBinBase, prop.bizKey, prop.fromPhase, '*.json'].join('/')
                )
            );
            arrayPush.apply(
                vtplList,
                grunt.file.expand(
                    // { filter: 'isFile' },
                    [prop.dirBinBase, prop.bizKey, prop.fromPhase, '*.vm'].join('/')
                )
            );
        }
        else {
            // TODO 
            // 暂时只支持depcit和snippet同名
            // 后续支持snippet共同引用同一个depict
            // （这种情况须分析snippet文件，得到引用列表）

            var vtpls = prop.vtplName.split(prop.sVTPLDelimeter);
            var vtplNameList = [];
            var i;
            var vtpl

            for (i = 0; i < vtpls.length; i ++) {
                if (vtpl = vtpls[i]) {
                    vtplNameList.push(vtpl + '.vm');
                    vtplNameList.push(vtpl + '.json');
                }
            }

            for (i = 0; i < vtplNameList.length; i ++) {
                vtplList.push(
                    vtpl = [prop.dirBinBase, prop.bizKey, prop.fromPhase, vtplNameList[i]].join('/')
                );
                // 验证是vtpl否存在
                assert(grunt.file.exists(vtpl), 'vtpl不存在 ' + vtpl);
            }
        }

        grunt.config.set('gen.vtplList', vtplList);
    }

    /**
     * 生成JS的debug文件
     */
    function taskJSDebugGen() {
        requireProp('_start');

        this.files.forEach(function(file) {
            var code = [
                '(function (){ ',
                '   var WEB_ROOT = (window.__$DI__OPT$__ || {}).WEB_ROOT || "";'
            ];

            file.src.filter(function(filePath) {
                if (!grunt.file.exists(filePath)) {
                    assert(false, '文件不存在：' + filePath);
                    return false;
                }
                else {
                    return true;
                }
            }).map(function (filePath) {
                code.push(
                        '   document.write( \'<script src="\' + WEB_ROOT + \'/' + filePath + '" type="text/javascript"><\\/script>\' );'
                );
            });

            code.push('})();');

            grunt.file.write(file.dest, code.join('\n'));

            log('[DI] taskJSDebugGen' + file.dest);
        });
    }

    /**
     * 生成CSS的debug文件
     */
    function taskCSSDebugGen() {
        this.files.forEach(function(file) {
            var code = [];

            file.src.filter(function(filePath) {
                if (!grunt.file.exists(filePath)) {
                    assert(false, '文件不存在：' + filePath);
                    return false;
                }
                else {
                    return true;
                }
            }).map(function (filePath) {
                if (this.target == 'toConsole') {
                    code.push('@import url(../../../' + filePath + ');');
                }
                else {
                    code.push('@import url(../../../../' + filePath + ');');
                }
            });

            grunt.file.write(file.dest, code.join('\n'));

            log('[DI] taskCSSDebugGen' + file.dest);
        });
    }

    //-----------------------------------------
    // 帮助函数
    //-----------------------------------------

    function prepareConf(options) {
        options = options || {};

        var i;
        var o;

        // 外部配置文件
        repoConf = readJSON('repo-conf.json');

        // 设置prop进grunt config
        // 从而prop能被grunt.config用于模版自身解析
        repoConf.prop = prop = grunt.config.getRaw('prop') || {};
        grunt.config.set('prop', prop);

        // 外部参数传入
        prop.bizKey = options.bizKey;
        prop.skin = options.skin;
        prop.fromPhase = options.fromPhase;
        prop.vtplName = options.vtplName;
        prop.rangeMode = options.rangeMode;
        prop.srcMode = options.srcMode || 'min';
        prop.mock = options.mock || false;
        prop._start = true;

        // 进行repoConf的模版解析，对所有<%= ... %>进行赋值替换
        repoConf = configParse(repoConf);

        // globbing patterns转换
        var fileAliasDict = repoConf.fileAliasDict;

        for (var alias in fileAliasDict) {
            fileAliasDict[alias] = grunt.file.expand(fileAliasDict[alias]);
        }

        // 生成clzPath字典(便于后续分析)
        repoConf.clzPathMap = {};
        for (i = 0; i < repoConf.clzPathDict.length; i ++) {
            o = repoConf.clzPathDict[i];
            if (o && o.clzPath) {
                repoConf.clzPathMap[o.clzPath] = o;
            }
        }

        // 生成fileDepends字典（便于后续分析）
        repoConf.fileDependsMap = {};
        for (i = 0; i < repoConf.fileDependsDict.length; i ++) {
            o = repoConf.fileDependsDict[i];
            if (o && o.filePath) {
                repoConf.fileDependsMap[o.filePath] = o;
            }
        }
    }

    /**
     * 检查输入参数的正确性，防止输入错误而未发掘
     */
    function checkBaseInput(options) {
        if (options.srcMode != null) {
            assert(
                    options.srcMode in { single: 1, debug: 1, min: 1 },
                    'srcMode非法 ' + options.srcMode
            );
        }
        if (options.rangeMode != null) {
            assert(
                    options.rangeMode in { prodBiz: 1, prodAll: 1, consoleAll: 1, stubAll: 1 },
                    'rangeMode非法 ' + options.rangeMode
            );
        }
        if (options.fromPhase != null) {
            assert(
                    options.fromPhase in { dev: 1, pre: 1, release: 1 },
                    'fromPhase非法 ' + options.fromPhase
            );
        }
        return options;
    }

    function checkSkinExists(skin) {
        assert(
                skin && grunt.file.exists(
                [prop.dirBinCom, 'css', skin, ''].join('/')
            ),
                'skin不存在 ' + skin
        );
    }

    function checkBizExists(bizKey, exists) {
        var ex = grunt.file.exists(prop.getDirBinProdBiz(bizKey));
        exists
            ? assert(ex, '产品线不存在：' + bizKey)
            : assert(!ex, '产品线已存在：' + bizKey);
    }

    function prepareDict() {
        if (!repoDict) {
            repoDict = grunt.file.read(prop.srcRepoDict);
            var jsonStart = repoDict.indexOf(prop.sCodeFlagBegin);
            var jsonEnd = repoDict.indexOf(prop.sCodeFlagEnd);
            assert(
                    jsonStart >= 0 && jsonEnd > 0,
                    '解析repo-dict异常：' + jsonStart + ' ' + jsonEnd
            );
            var json = repoDict.slice(jsonStart, jsonEnd);
            repoDict = parseJSON(json);
        }
        return repoDict;
    }

    /**
     * 根据depict的需求和文件的依赖选取要加载的文件
     */
    function buildSrc(rangeMode, depictInfo) {
        var fileList = [];
        var canAppEN;
        var canMock;

        switch (rangeMode) {
            case 'prodBiz':
                // 首先加入必加载的文件
                fileList.push("xcoreProdJS", "appBegin", "prodBaseJS");
                replaceAlias(fileList, repoConf.fileAliasDict);
                fileList = parseBizSrc(depictInfo, fileList);
                canAppEN = canMock = true;
                break;
            case 'prodAll':
                fileList.push("xcoreProdJS", "appBegin", "uiAllJS", "prodAllJS");
                replaceAlias(fileList, repoConf.fileAliasDict);
                canAppEN = canMock = true;
                break;
            case 'consoleAll':
                fileList.push("lib3rdJS", "xcoreConsoleJS", "appBegin", "uiAllJS", "consoleAllJS");
                replaceAlias(fileList, repoConf.fileAliasDict);
                canAppEN = canMock = true;
                break;
            case 'stubAll':
                fileList.push("stubAllJS");
                replaceAlias(fileList, repoConf.fileAliasDict);
                canAppEN = canMock = false;
                break;
        }

        // 最后加入appEnd
        if (canAppEN) {
            fileList.push("!appEnd" /* 确保前面文件不包含append */, "appEnd");
            replaceAlias(fileList, repoConf.fileAliasDict);
        }

        // 如果有mock则加入
        if (canMock && prop.mock) {
            fileList.push('mockJS');
            replaceAlias(fileList, repoConf.fileAliasDict);
        }

        // 根据依赖构建文件
        return parseDepends(fileList);
    }

    /**
     * 得到某个产品线所依赖的JS文件列表
     */
    function parseBizSrc(depictInfo, fileList) {
        var clzKeySet = depictInfo.clzKeySet;
        var i;
        var o;

        // 生成clzKey字典
        var clzKeyDict = {};
        for (i = 0; i < repoDict.CLZ_DEFS.length; i ++) {
            o = repoDict.CLZ_DEFS[i];
            if (o && o.clzKey) {
                clzKeyDict[o.clzKey] = o;
            }
        }

        // 将clzKey转成clzPath
        var clzPathList = [];

        for (i in clzKeySet) {
            o = clzKeyDict[i];
            assert(o && o.clzPath, "clzKey未在repo-dict中有定义：clzKey=" + i);
            clzPathList.push(o.clzPath);
            // 如果有引用
            o.adapterPath && clzPathList.push(o.adapterPath);
        }

        // 从clzPath到filePath
        for (i = 0; i < clzPathList.length; i ++) {
            o = repoConf.clzPathMap[clzPathList[i]];
            assert(o && o.filePath, "clzPath未在repo-dict中有定义：clzPath=" + clzPathList[i]);
            fileList.push(o.filePath);
        }

        return fileList;
    }

    /**
     * 顺序处理filelist，深度优先遍历依赖树，并添加依赖的文件。
     * 会去重。
     * 会断开循环引用。
     * 如果某项标有“!“，表示要从fileList中去除。
     */
    function parseDepends(fileList) {
        var fileDependsMap = repoConf.fileDependsMap;
        // 递归本函数内的结果
        var resultList = [];
        var resultSet = {};
        // 检查循环引用的数组
        var gCycle = arguments[1] || {};

        for (var i = 0, item, depends; i < fileList.length; i ++) {
            if ((item = fileList[i])
                // 如果遇到循环引用，则跳过不处理
                && !(item in gCycle)
                ) {
                // 从结果集里去除的指令
                if (0 === item.indexOf('!')) {
                    removeFrom(resultList, parseDepends([item.slice(1)]));
                }
                else {
                    // 从依赖树中找depends
                    depends = ((fileDependsMap[item] || {}).depends || []).slice();
                    if (depends.length) {
                        gCycle[item] = 1;
                        depends = parseDepends(depends, gCycle);
                        delete gCycle[item];
                    }
                    // item放在其所有依赖的后面
                    depends.push(item);
                    for (var j = 0; j < depends.length; j ++) {
                        // 去除已经有的
                        if (!(depends[j] in resultSet)) {
                            resultList.push(depends[j]);
                            resultSet[depends[j]] = 1;
                        }
                    }
                }
            }
        }
        return resultList;
    }

    /**
     * 从一个list里remove另一个list的所有元素
     */
    function removeFrom(mainList, rmList) {
        var rmSet = {};
        var i = 0;
        var o;

        if (kindOf(rmList) == 'object') {
            rmSet = rmList;
        }
        else {
            for (i = 0; i < rmList.length; i ++) {
                if (o = rmList[i]) {
                    rmSet[o] = 1;
                }
            }
        }

        for (i = 0; i < mainList.length; ) {
            if ((o = mainList[i]) && o in rmSet) {
                mainList.splice(i, 1);
            }
            else {
                i ++;
            }
        }
    }

    /**
     * 别名的处理，递归地将别名替换成真实文件路径（在数组原位置上插入）
     */
    function replaceAlias(fileList, fileAliasDict) {
        if (!fileList) { return; }

        for (var i = 0, o, prefix; i < fileList.length; i ++) {
            prefix = '';
            if (o = fileList[i]) {
                if (o.indexOf('!') === 0) {
                    o = o.slice(1);
                    prefix = '!';
                }
                if (o = fileAliasDict[o]) {
                    o = kindOf(o) != 'array' ? [o] : o.slice();
                    if (prefix) {
                        for (var j = 0; j < o.length; j ++) {
                            o[j] = prefix + o[j];
                        }
                    }
                    [].splice.apply(fileList, [i, 1].concat(o));
                    i = i + o.length - 1;
                }
            }
        }

        return fileList;
    }

    /**
     * 获取某个产品线的所有（非历史版本的）depict文件内容
     */
    function analyzeDepicts(bizKey, fromPhase) {
        // 所有被depict引用到的clzKey
        var clzKeySet = {};
        var j;

        forEachDepict(
            bizKey,
            fromPhase,
            function (filePath, content) {
                // 分析clzDefs
                for (j = 0; j < (content.clzDefs || []).length; j ++) {
                    if (content.clzDefs[j] && content.clzDefs[j].clzKey) {
                        clzKeySet[content.clzDefs[j].clzKey] = 1;
                    }
                }
                // 分析entityDefs
                for (j = 0; j < (content.entityDefs || []).length; j ++) {
                    if (content.entityDefs[j] && content.entityDefs[j].clzKey) {
                        clzKeySet[content.entityDefs[j].clzKey] = 1;
                    }
                }
            }
        )

        return { clzKeySet: clzKeySet };
    }

    // Expand a config value recursively. Used for post-processing raw values
    // already retrieved from the config.
    function configParse(raw) {
        // TEMPLATE
        var cfg = grunt.util.recurse(raw, function(value) {
            // If the value is not a string, return it.
            if (typeof value !== 'string') { return value; }
            // Process the string as a template.
            return grunt.template.process(value, { data: raw });
        });

        // REPLACE ALIAS
        // 注意这里只处理了fileAliasDict和fileDependsDict的depends
        // 也就是说alias只能出现在这里
        // 如果以后有更多需求，再改
        var i;
        for (i in cfg.fileAliasDict) {
            replaceAlias(cfg.fileAliasDict[i], cfg.fileAliasDict);
        }
        for (i = 0; i < cfg.fileDependsDict.length; i ++) {
            replaceAlias(cfg.fileDependsDict[i].depends, cfg.fileAliasDict);
        }

        return cfg;
    }

    /**
     * 默认提供的readJSON，太严格，不允许有注释。
     * 所以改做可以有注释的版本
     *
     * @private
     * @param {string} data
     * @return {Object}
     */
    function parseJSON(data) {
        var obj = (new Function("return (" + data + ")"))();
        verifyJSON(obj);
        return obj;
    }

    /**
     * 校验配置文件（看是不是有空格）
     */
    function verifyJSON(obj) {
        var kind = kindOf(obj);

        if (kind == 'object' || kind == 'array') {
            for (var i in obj) {
                checkBlank(i);
                if (obj.hasOwnProperty(i)) {
                    verifyJSON(obj[i]);
                }
            }
        }
        else if (kind == 'string') {
            checkBlank(obj);
        }

        function checkBlank(str) {
            assert(str == str.trim(), "json key或value有空格：" + str);
        }
    }

    /**
     * 默认提供的readJSON，太严格，不允许有注释。
     * 所以改做可以有注释的版本
     *
     * @private
     * @param {string} path
     * @return {Object}
     */
    function readJSON(path) {
        return path ? parseJSON(grunt.file.read(path)) : null;
    }

    /**
     * 遍历某产品线所有模版
     *
     * @private
     */
    function forEachDepict(bizKey, fromPhase, callback) {
        // 获取depict文件列表
        var filePathList = grunt.file.expand(
            { filter: 'isFile' },
            [prop.dirBinBase, bizKey, fromPhase, '*.json'].join('/')
        );

        for (var i = 0, o; i < filePathList.length; i ++) {
            try {
                if ((o = readJSON(filePathList[i]))
                    // 有此标志的是depict
                    && o.diKey == 'DEPICT'
                    ) {
                    callback(filePathList[i], o);
                }
            }
            catch (e) {
                // 有可能json解析出错（比如遇到空文件），但是不能影响其他流程
                log('[DI] Parse json error: ' + e.message);
            }
        }
    }

    /**
     * function bind, 只绑定arguments，不改变this
     */
    function argBind(fn) {
        var args = Array.prototype.slice.call(arguments, 1);
        return function () {
            fn.apply(this, args.concat(arguments));
        };
    }

    /**
     * 将grunt task挂如执行队列
     */
    function runTask() {
        grunt.task.run(Array.prototype.join.call(arguments, ':'));
    }

    /**
     * 检查repo prop是否初始化
     */
    function requireProp() {
        for (var i = 0; i < arguments.length; i ++) {
            grunt.config.requires('prop.' + arguments[i]);
        }
    }

    /**
     * 对象浅拷贝
     */
    function extend(target, source) {
        for (var key in source) {
            target[key] = source[key];
        }
        return target;
    }

    /**
     * 强制判断
     */
    function assert(condition, msg) {
        if (!condition) {
            throw new Error('Assert fail! ' + msg);
        }
    }

    /**
     * 打印到控制台
     */
    function log(txt) {
        grunt.log.writeln(txt);
    }

    /**
     * 打印对象到控制台
     */
    function writeObj(obj) {
        try {
            grunt.log.writeln(JSON.stringify(obj));
        }
        catch (e) {
            grunt.log.writeln(e);
            for (var i in obj) {
                grunt.log.writeln(i + ': ' + obj[i]);
            }
        }
    }

    //-----------------------------------------
    // 试验
    //-----------------------------------------

    function test() {

        grunt.initConfig(
            {
                pkg: grunt.file.readJSON('package.json'),
                //  aaa: {
                //     options: {
                //         separator: ';'
                //     },
                //     dist: {
                //         src: ['src/**/*.js'],
                //         dest: 'dist/<%= pkg.name %>.js'
                //     }
                // } ,
                // repoConf: readJSON('repo-conf.json'),
                asdf: 'rrrrrrr <%= ccc("asdf") %> rrr',
                bbbb: [
                    [
                        { '6<%= asdf %>5': 'a<%= asdf %>t' },
                        { '6<%= asdf %>5': 'a<%= asdf %>t' }
                    ]
                ],
                ddd: { src: "<%= fff() %>", dest: 'asdf' },
                fff: function () { return [12,33,44]; },
                ccc: function (asdf) { return asdf + 'zxvzxcvxzxcvzxcvzxcvz'; },
                copy: {
                    dist: {
                        // expand: true, flatten: true, src: ['zzz/asdf'], dest: 'aaa/imgqq/gg', notnull: true, filter: "isFile"
                        src: ['zzz/*'], dest: 'aaa/imgqq/gg', notnull: true, filter: "isFile"
                    }
                },
                clean: {
                    zzz: {
                        src: 'zzz/asdf'
                    }
                },
                aaa: {
                    options: {
                        banner: ' <%= pkg.name %> <%= grunt.template.today("dd-mm-yyyy") %> \n',
                        ddd: "<%= bbbb %>"
                    },
                    dist: {
                        files: [
                            // 'dist/<%= pkg.name %>.min.js': ['<%= concat.dist.dest %>']
                            {
                                src: ['src/**/*.js'],
                                dest: 'dist/<%= pkg.name %>.js'
                            }
                        ]
                    }
                }
            }
        );

        grunt.loadNpmTasks('grunt-contrib-clean');
        grunt.loadNpmTasks('grunt-contrib-copy');
        grunt.loadNpmTasks('grunt-contrib-concat');
        grunt.loadNpmTasks('grunt-contrib-uglify');
        grunt.loadNpmTasks('grunt-contrib-cssmin');

        grunt.registerMultiTask('aaa', function (a, b, c) {
            // grunt.log.writeln(grunt.template.process);
            // grunt.log.writeln(configParse());
            // writeObj(configParse(readJSON('repo-conf.json')));
            // writeObj(grunt.config('aaa.dist.files')[0]);
            // writeObj(grunt.file.expand(['src/*']));
            // writeObj(grunt.config.get('ddd'));
            // log(grunt.config.process('ddd'));
            writeObj(grunt.file.expand({ filter: 'isFile' }, ['src/css/*']));
        });

        grunt.registerTask('bbb', function (a, b, c) {
            log(' in bbb ' + a + ' ' + b + ' ' + c);
        });
    }
};