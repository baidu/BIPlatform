/**
 * Created by lzt on 2014/8/12 0012.
 */
define(function () {
    var VmJson = {
        date: {
            html: [
                '<div class="di-o_o-block cond-block" data-o_o-di="snpt1.cpnt-form1">',
                ' <div class=" di-o_o-line">',
                '<div class=" di-o_o-item" data-o_o-di="snpt1.cpnt-form1-vu-1399874275096_96">',
                '</div>',
                '</div>',
                '</div>',
                '<div class="di-o_o-line" style="position:static">',
                '<div class="di-o_o-item" data-o_o-di="snpt1.vu-form1-confirm1">',
                '</div>',
                '</div>'
            ],
            json: {
                "desc": "查询条件||多维图形||多维表格",
                "diKey": "DEPICT",
                "clzDefs": [
                    {
                        "clzKey": "OLAP_TABLE",
                        "dataOpt": {
                            "emptyHTML": "未查询到相关数据"
                        }
                    },
                    {
                        "clzKey": "ECUI_SELECT",
                        "dataOpt": {
                            "optionSize": 10
                        }
                    }
                ],
                "entityDefs": [
                    {
                        "id": "snpt1",
                        "clzType": "SNIPPET"
                    },
                    {
                        "id": "snpt1.cpnt-form1",
                        "clzType": "COMPONENT",
                        "clzKey": "DI_FORM",
                        "reportType": "RTPL_VIRTUAL",
                        "init": {
                            "action": {
                                "name": "sync"
                            }
                        },
                        "sync": {
                            "viewDisable": "ALL"
                        },
                        "vuiRef": {
                            "input": [
                                "snpt1.cpnt-form1-vu-1399874275096_96"
                            ],
                            "confirm": "snpt1.vu-form1-confirm1"
                        },
                        "dataOpt": {
                            "submitMode": "CONFIRM"
                        }
                    },
                    {
                        "id": "snpt1.vu-form1-confirm1",
                        "clzType": "VUI",
                        "clzKey": "H_BUTTON",
                        "dataOpt": {
                            "skin": "ui-normal-btn",
                            "text": "查询"
                        }
                    },
                    {
                        "id": "snpt1.cpnt-form1-vu-1399874275096_96",
                        "clzType": "VUI",
                        "dataSetOpt": {
                            "forbidEmpty": false,
                            "disableCancelBtn": false,
                            "timeTypeList": [
                                {
                                    "value": "D",
                                    "text": "日"
                                },
                                {
                                    "value": "W",
                                    "text": "周"
                                },
                                {
                                    "value": "M",
                                    "text": "月"
                                },
                                {
                                    "value": "Q",
                                    "text": "季"
                                },
                                {
                                    "value": "Y",
                                    "text": "年"
                                }
                            ],
                            "timeTypeOpt": {
                                "D": {
                                    "selMode": "SINGLE",
                                    "date": [
                                        "-31D",
                                        "-1D"
                                    ],
                                    "range": [
                                        "2011-01-01",
                                        "-1D"
                                    ],
                                    "selModeList": [
                                        {
                                            "text": "单选",
                                            "value": "SINGLE",
                                            "prompt": "单项选择"
                                        }
                                    ]
                                },
                                "Y": {
                                    "selMode": "SINGLE",
                                    "date": [
                                        "2011",
                                        "2013"
                                    ],
                                    "range": [
                                        "2001",
                                        "2014"
                                    ],
                                    "selModeList": [
                                        {
                                            "text": "单选"
                                        }
                                    ]
                                },
                                "W": {
                                    "selMode": "RANGE",
                                    "date": [
                                        "-31D",
                                        "-1D"
                                    ],
                                    "range": [
                                        "2011-01-01",
                                        "-1D"
                                    ],
                                    "selModeList": [
                                        {
                                            "text": "单选",
                                            "value": "SINGLE",
                                            "prompt": "单项选择"
                                        },
                                        {
                                            "text": "范围多选",
                                            "value": "RANGE",
                                            "prompt": "范围选择，点击一下选择开始值，再点击一下选择结束值"
                                        }
                                    ]
                                },
                                "M": {
                                    "selMode": "MULTIPLE",
                                    "date": [
                                        "-31D",
                                        "-1D"
                                    ],
                                    "range": [
                                        "2011-01-01",
                                        "-1D"
                                    ],
                                    "selModeList": [
                                        {
                                            "text": "单选",
                                            "value": "SINGLE",
                                            "prompt": "单项选择"
                                        },
                                        {
                                            "text": "范围多选",
                                            "value": "RANGE",
                                            "prompt": "范围选择，点击一下选择开始值，再点击一下选择结束值"
                                        }
                                    ]
                                },
                                "Q": {
                                    "selMode": "SINGLE",
                                    "date": [
                                        "-31D",
                                        "-1D"
                                    ],
                                    "range": [
                                        "2011-01-01",
                                        "-1D"
                                    ],
                                    "selModeList": [
                                        {
                                            "text": "单选",
                                            "value": "SINGLE",
                                            "prompt": "单项选择"
                                        }
                                    ]
                                }
                            }
                        },
                        "name": "dim_time^_^the_date",
                        "clzKey": "X_CALENDAR"
                    }
                ]
            }
        },
        dateChart: {
            html: [
                '<div class="di-o_o-block cond-block" data-o_o-di="snpt1.cpnt-form1">',
                ' <div class=" di-o_o-line">',
                '<div class=" di-o_o-item" data-o_o-di="snpt1.cpnt-form1-vu-1399874275096_96">',
                '</div>',
                '</div>',
                '</div>',
                '<div class="di-o_o-line" style="position:static">',
                '<div class="di-o_o-item" data-o_o-di="snpt1.vu-form1-confirm1">',
                '</div>',
                '</div>',
                '<div class="di-o_o-block" data-o_o-di="snpt1.cpnt-chart1">',
                '<div data-o_o-di="snpt1.vu-chart1"></div>',
                '</div>'
            ],
            json: {
                "desc": "查询条件||多维图形||多维表格",
                "diKey": "DEPICT",
                "clzDefs": [
                    {
                        "clzKey": "OLAP_TABLE",
                        "dataOpt": {
                            "emptyHTML": "未查询到相关数据"
                        }
                    },
                    {
                        "clzKey": "ECUI_SELECT",
                        "dataOpt": {
                            "optionSize": 10
                        }
                    }
                ],
                "entityDefs": [
                    {
                        "id": "snpt1",
                        "clzType": "SNIPPET"
                    },
                    {
                        "id": "snpt1.cpnt-form1",
                        "clzType": "COMPONENT",
                        "clzKey": "DI_FORM",
                        "reportType": "RTPL_VIRTUAL",
                        "init": {
                            "action": {
                                "name": "sync"
                            }
                        },
                        "sync": {
                            "viewDisable": "ALL"
                        },
                        "vuiRef": {
                            "input": [
                                "snpt1.cpnt-form1-vu-1399874275096_96"
                            ],
                            "confirm": "snpt1.vu-form1-confirm1"
                        },
                        "dataOpt": {
                            "submitMode": "CONFIRM"
                        },
                        "reportTemplateId": "RTPL_VIRTUAL_ID"
                    },
                    {
                        "id": "snpt1.vu-form1-confirm1",
                        "clzType": "VUI",
                        "clzKey": "H_BUTTON",
                        "dataOpt": {
                            "skin": "ui-normal-btn",
                            "text": "查询"
                        }
                    },
                    {
                        "id": "snpt1.cpnt-form1-vu-1399874275096_96",
                        "clzType": "VUI",
                        "dataSetOpt": {
                            "forbidEmpty": false,
                            "disableCancelBtn": false,
                            "timeTypeList": [
                                {
                                    "value": "D",
                                    "text": "日"
                                },
                                {
                                    "value": "W",
                                    "text": "周"
                                },
                                {
                                    "value": "M",
                                    "text": "月"
                                },
                                {
                                    "value": "Q",
                                    "text": "季"
                                }
                            ],
                            "timeTypeOpt": {
                                "D": {
                                    "selMode": "SINGLE",
                                    "date": [
                                        "-31D",
                                        "-1D"
                                    ],
                                    "range": [
                                        "2011-01-01",
                                        "-1D"
                                    ],
                                    "selModeList": [
                                        {
                                            "text": "单选",
                                            "value": "SINGLE",
                                            "prompt": "单项选择"
                                        }
                                    ]
                                },
                                "W": {
                                    "selMode": "RANGE",
                                    "date": [
                                        "-31D",
                                        "-1D"
                                    ],
                                    "range": [
                                        "2011-01-01",
                                        "-1D"
                                    ],
                                    "selModeList": [
                                        {
                                            "text": "单选",
                                            "value": "SINGLE",
                                            "prompt": "单项选择"
                                        },
                                        {
                                            "text": "范围多选",
                                            "value": "RANGE",
                                            "prompt": "范围选择，点击一下选择开始值，再点击一下选择结束值"
                                        }
                                    ]
                                },
                                "M": {
                                    "selMode": "MULTIPLE",
                                    "date": [
                                        "-31D",
                                        "-1D"
                                    ],
                                    "range": [
                                        "2011-01-01",
                                        "-1D"
                                    ],
                                    "selModeList": [
                                        {
                                            "text": "单选",
                                            "value": "SINGLE",
                                            "prompt": "单项选择"
                                        },
                                        {
                                            "text": "范围多选",
                                            "value": "RANGE",
                                            "prompt": "范围选择，点击一下选择开始值，再点击一下选择结束值"
                                        }
                                    ]
                                },
                                "Q": {
                                    "selMode": "SINGLE",
                                    "date": [
                                        "-31D",
                                        "-1D"
                                    ],
                                    "range": [
                                        "2011-01-01",
                                        "-1D"
                                    ],
                                    "selModeList": [
                                        {
                                            "text": "单选",
                                            "value": "SINGLE",
                                            "prompt": "单项选择"
                                        }
                                    ]
                                }
                            }
                        },
                        "dateKey": {
                            "D": "lztd",
                            "W": "lztw",
                            "M": "lztm",
                            "Q": "lztq"
                        },
                        "name": "dim_time^_^the_date",
                        "clzKey": "X_CALENDAR"
                    },
                    {
                        "id": "snpt1.cpnt-chart1",
                        "clzType": "COMPONENT",
                        "clzKey": "DI_CHART",
                        "sync": { "viewDisable": "ALL" },
                        "vuiRef": {
                            "mainChart": "snpt1.vu-chart1"
                        },
                        "interactions": [
                            {
                                "events": [
                                    { "rid": "snpt1.cpnt-form1", "name": "submit" },
                                    { "rid": "snpt1.cpnt-form1", "name": "dataloaded" }
                                ],
                                "action": { "name": "sync" },
                                "argHandlers": [
                                    ["clear"],
                                    ["getValue", "snpt1.cpnt-form1"]
                                ]
                            }
                        ]
                    },
                    {
                        "id": "snpt1.vu-chart1",
                        "clzType": "VUI",
                        "clzKey": "H_CHART",
                        "dataOpt": {
                            "height": 260,
                            "legend": { "xMode": "pl" },
                            "weekViewRange": [null, "-1d"]
                        }
                    }
                ]
            }
        },
        dateEChart: {
            html: [
                '<div class="di-o_o-block cond-block" data-o_o-di="snpt1.cpnt-form1">',
                ' <div class=" di-o_o-line">',
                '<div class=" di-o_o-item" data-o_o-di="snpt1.cpnt-form1-vu-1399874275096_96">',
                '</div>',
                '</div>',
                '</div>',
                '<div class="di-o_o-line" style="position:static">',
                '<div class="di-o_o-item" data-o_o-di="snpt1.vu-form1-confirm1">',
                '</div>',
                '</div>',
                '<div class="component-item shell-component ui-resizable"  style="width: 632px; height: 445px; background: rgb(255, 255, 255);" >',
                '<div class="comp-box di-o_o-block" data-o_o-di="snpt1.cpnt-chart1">',
                '<div data-o_o-di="snpt1.vu-chart1"></div>',
                '</div>',
                '</div>'
            ],
            json: {
                "desc": "查询条件||多维图形||多维表格",
                "diKey": "DEPICT",
                "clzDefs": [
                    {
                        "clzKey": "OLAP_TABLE",
                        "dataOpt": {
                            "emptyHTML": "未查询到相关数据"
                        }
                    },
                    {
                        "clzKey": "ECUI_SELECT",
                        "dataOpt": {
                            "optionSize": 10
                        }
                    }
                ],
                "entityDefs": [
                    {
                        "id": "snpt1",
                        "clzType": "SNIPPET"
                    },
                    // form
                    {
                        "id": "snpt1.cpnt-form1",
                        "clzType": "COMPONENT",
                        "clzKey": "DI_FORM",
                        "reportType": "RTPL_VIRTUAL",
                        "init": {
                            "action": {
                                "name": "sync"
                            }
                        },
                        "sync": {
                            "viewDisable": "ALL"
                        },
                        "vuiRef": {
                            "input": [
                                "snpt1.cpnt-form1-vu-1399874275096_96"
                            ],
                            "confirm": "snpt1.vu-form1-confirm1"
                        },
                        "dataOpt": {
                            "submitMode": "CONFIRM"
                        },
                        "reportTemplateId": "RTPL_VIRTUAL_ID"
                    },
                    {
                        "id": "snpt1.vu-form1-confirm1",
                        "clzType": "VUI",
                        "clzKey": "H_BUTTON",
                        "dataOpt": {
                            "skin": "ui-normal-btn",
                            "text": "查询"
                        }
                    },
                    {
                        "id": "snpt1.cpnt-form1-vu-1399874275096_96",
                        "clzType": "VUI",
                        "dataSetOpt": {
                            "forbidEmpty": false,
                            "disableCancelBtn": false,
                            "timeTypeList": [
                                {
                                    "value": "D",
                                    "text": "日"
                                },
                                {
                                    "value": "W",
                                    "text": "周"
                                },
                                {
                                    "value": "M",
                                    "text": "月"
                                },
                                {
                                    "value": "Q",
                                    "text": "季"
                                }
                            ],
                            "timeTypeOpt": {
                                "D": {
                                    "selMode": "SINGLE",
                                    "date": [
                                        "-31D",
                                        "-1D"
                                    ],
                                    "range": [
                                        "2011-01-01",
                                        "-1D"
                                    ],
                                    "selModeList": [
                                        {
                                            "text": "单选",
                                            "value": "SINGLE",
                                            "prompt": "单项选择"
                                        }
                                    ]
                                },
                                "W": {
                                    "selMode": "RANGE",
                                    "date": [
                                        "-31D",
                                        "-1D"
                                    ],
                                    "range": [
                                        "2011-01-01",
                                        "-1D"
                                    ],
                                    "selModeList": [
                                        {
                                            "text": "单选",
                                            "value": "SINGLE",
                                            "prompt": "单项选择"
                                        },
                                        {
                                            "text": "范围多选",
                                            "value": "RANGE",
                                            "prompt": "范围选择，点击一下选择开始值，再点击一下选择结束值"
                                        }
                                    ]
                                },
                                "M": {
                                    "selMode": "MULTIPLE",
                                    "date": [
                                        "-31D",
                                        "-1D"
                                    ],
                                    "range": [
                                        "2011-01-01",
                                        "-1D"
                                    ],
                                    "selModeList": [
                                        {
                                            "text": "单选",
                                            "value": "SINGLE",
                                            "prompt": "单项选择"
                                        },
                                        {
                                            "text": "范围多选",
                                            "value": "RANGE",
                                            "prompt": "范围选择，点击一下选择开始值，再点击一下选择结束值"
                                        }
                                    ]
                                },
                                "Q": {
                                    "selMode": "SINGLE",
                                    "date": [
                                        "-31D",
                                        "-1D"
                                    ],
                                    "range": [
                                        "2011-01-01",
                                        "-1D"
                                    ],
                                    "selModeList": [
                                        {
                                            "text": "单选",
                                            "value": "SINGLE",
                                            "prompt": "单项选择"
                                        }
                                    ]
                                }
                            }
                        },
                        "dateKey": {
                            "D": "lztd",
                            "W": "lztw",
                            "M": "lztm",
                            "Q": "lztq"
                        },
                        "name": "dim_time^_^the_date",
                        "clzKey": "X_CALENDAR"
                    },
                    // chart
                    {
                        "id": "snpt1.cpnt-chart1",
                        "clzType": "COMPONENT",
                        "clzKey": "DI_ECHART",
                        "sync": { "viewDisable": "ALL" },
                        "vuiRef": {
                            "mainChart": "snpt1.vu-chart1"
                        },
                        "interactions": [
                            {
                                "events": [
                                    { "rid": "snpt1.cpnt-form1", "name": "submit" },
                                    { "rid": "snpt1.cpnt-form1", "name": "dataloaded" }
                                ],
                                "action": { "name": "sync" },
                                "argHandlers": [
                                    ["clear"],
                                    ["getValue", "snpt1.cpnt-form1"]
                                ]
                            }
                        ]
                    },
                    {
                        "id": "snpt1.vu-chart1",
                        "clzType": "VUI",
                        "clzKey": "E_CHART",
                        "dataOpt": {
                            "height": 260,
                            "legend": { "xMode": "pl" },
                            "weekViewRange": [null, "-1d"]
                        }
                    }
                ]
            }
        },
        // 图形vm和json
        chart: {
            html: [
                '<div class="di-o_o-block" data-o_o-di="snpt1.cpnt-chart1">',
                '<div data-o_o-di="snpt1.vu-chart1"></div>',
                '</div>'
            ],
            json: {
                "desc": "查询条件||多维图形||多维表格",
                "diKey": "DEPICT",
                "clzDefs": [
                    {
                        "clzKey": "OLAP_TABLE",
                        "dataOpt": {
                            "emptyHTML": "未查询到相关数据"
                        }
                    },
                    {
                        "clzKey": "ECUI_SELECT",
                        "dataOpt": {
                            "optionSize": 10
                        }
                    }
                ],
                "entityDefs": [
                    {
                        "id": "snpt1",
                        "clzType": "SNIPPET"
                    },
                    {
                        "id": "snpt1.cpnt-chart1",
                        "clzType": "COMPONENT",
                        "clzKey": "DI_CHART",
                        "sync": { "viewDisable": "ALL" },
                        "vuiRef": {
                            "mainChart": "snpt1.vu-chart1"
                        },
                        "init": {
                            "action": { "name": "sync" }
                        }
                    },
                    {
                        "id": "snpt1.vu-chart1",
                        "clzType": "VUI",
                        "clzKey": "H_CHART",
                        "dataOpt": {
                            "height": 260,
                            "legend": { "xMode": "pl" },
                            "weekViewRange": [null, "-1d"]
                        }
                    }
                ]
            }
        },
        // 图形vm和json
        liteolapChart: {
            html: [
                '<div data-o_o-di="snpt1.cpnt-liteolapchart-meta1">',
                '<div class="di-o_o-line">',
                '<span class="di-o_o-item">选择指标：</span><span class="di-o_o-item" data-o_o-di="snpt1.vu-liteolapchart-meta1"></span>',
                '</div>',
                '</div>',
                '<div class="di-o_o-block" data-o_o-di="snpt1.cpnt-liteolapchart1">',
                '<div data-o_o-di="snpt1.vu-liteolapchart1" style="height: 260px">',
                '</div>',
                '</div>'
            ],
            json: {
                "desc": "查询条件||多维图形||多维表格",
                "diKey": "DEPICT",
                "clzDefs": [
                    {
                        "clzKey": "OLAP_TABLE",
                        "dataOpt": {
                            "emptyHTML": "未查询到相关数据"
                        }
                    },
                    {
                        "clzKey": "ECUI_SELECT",
                        "dataOpt": {
                            "optionSize": 10
                        }
                    }
                ],
                "entityDefs": [
                    {
                        "id": "snpt1",
                        "clzType": "SNIPPET"
                    },
                    {
                        "id": "snpt1.cpnt-liteolapchart-meta1",
                        "clzType": "COMPONENT",
                        "clzKey": "LITEOLAP_META_CONFIG",
                        "sync": {
                            "viewDisable": "ALL"
                        },
                        "vuiRef": {
                            "main": "snpt1.vu-liteolapchart-meta1"
                        },
                        "dataOpt": {
                            "needShowCalcInds": true,
                            "submitMode": "IMMEDIATE",
                            "reportType": "RTPL_OLAP_TABLE",
                            "datasourceId": {
                                "SELECT": "LIST_SELECT"
                            }
                        }
                    },
                    {
                        "id": "snpt1.vu-liteolapchart-meta1",
                        "clzType": "VUI",
                        "clzKey": "OLAP_META_IND_SELECT"
                    },
                    {
                        "id": "snpt1.cpnt-liteolapchart1",
                        "clzType": "COMPONENT",
                        "clzKey": "DI_LITEOLAP_CHART",
                        "init": {
                            "action": {
                                "name": "sync"
                            }
                        },
                        "sync": {
                            "viewDisable": "ALL"
                        },
                        "vuiRef": {
                            "mainChart": "snpt1.vu-liteolapchart1"
                        },
                        "interactions": [
                            {
                                "event": {
                                    "rid": "snpt1.cpnt-liteolapchart-meta1",
                                    "name": "submit"
                                },
                                "action": {
                                    "name": "syncLiteOlapChart"
                                },
                                "argHandlers": [
                                    [
                                        "clear"
                                    ],
                                    [
                                        "getValue",
                                        "snpt1.vu-liteolapchart-meta1"
                                    ]
                                ]
                            }
                        ],
                        "reportTemplateId": "PERSISTENT^_^zongheng^_^node2^_^2125329485",
                        "reportType": "RTPL_OLAP_TABLE"
                    },
                    {
                        "id": "snpt1.vu-liteolapchart1",
                        "clzType": "VUI",
                        "clzKey": "H_CHART",
                        "dataOpt": {
                            "legend": {
                                "xMode": "pl"
                            },
                            "weekViewRange": [
                                null,
                                "-1d"
                            ]
                        }
                    }
                ]
            }
        },
        // 表格vm和json
        table: {
            html: [
                '<div style="width: 500px">',
                '<div class="di-o_o-block table-block" data-o_o-di="snpt1.cpnt-table1">',
                '<div class="di-o_o-line">',
                '<div class="di-o_o-item" data-o_o-di="snpt1.vu-table1-download1"></div>',
                '</div>',
                '<div class="di-o_o-line">',
                '<div class="" data-o_o-di="snpt1.vu-table1-breadcrumb1"></div>',
                '</div>',
                '<div class="di-o_o-line">',
                '<div class="vu-table" data-o_o-di="snpt1.vu-table1"></div>',
                '</div>',
                '<div class="di-o_o-line">',
                '<div class="di-table-prompt">',
                '<div class="di-table-count" data-o_o-di="snpt1.vu-table1-count1">',
                '符合查询条件的数据只显示<span class="di-table-count-num">#{currRecordCount}</span>条。',
                '</div>',
                '</div>',
                '</div>',
                '</div>',
                '</div>'
            ],
            json: {
                "desc" : "查询条件||多维表格",
                "diKey": "DEPICT",
                "clzDefs": [
                    {
                        "clzKey": "OLAP_TABLE",
                        "dataOpt": {
                            "emptyHTML": "未查询到相关数据"
                        }
                    },
                    {
                        "clzKey": "ECUI_SELECT",
                        "dataOpt": {
                            "optionSize": 10
                        }
                    }
                ],
                "entityDefs": [
                    {
                        "id": "snpt1",
                        "clzType": "SNIPPET"
                    },
                    {
                        "id": "snpt1.cpnt-table1",
                        "clzType": "COMPONENT",
                        "clzKey": "DI_TABLE",
                        "sync": { "viewDisable": "ALL" },
                        "init": {
                            "action": { "name": "sync" }
                        },
                        "vuiRef": {
                            "mainTable": "snpt1.vu-table1",
                            "breadcrumb": "snpt1.vu-table1-breadcrumb1",
                            "download": "snpt1.vu-table1-download1",
                            "countInfo": "snpt1.vu-table1-count1"
                        },
                        "init": {
                            "action": { "name": "sync" }
                        }
                    },
                    {
                        "id": "snpt1.vu-table1",
                        "clzType": "VUI",
                        "clzKey": "OLAP_TABLE",
                        "name": "table",
                        "dataOpt": {
                            "rowHCellCut": 30,
                            "hCellCut": 30,
                            "cCellCut": 30,
                            "vScroll": true
                        }
                    },
                    {
                        "id": "snpt1.vu-table1-breadcrumb1",
                        "clzType": "VUI",
                        "clzKey": "BREADCRUMB",
                        "dataOpt": {
                            "maxShow": 6
                        }
                    },
                    {
                        "id": "snpt1.vu-table1-download1",
                        "clzType": "VUI",
                        "clzKey": "H_BUTTON",
                        "dataOpt": {
                            "skin": "ui-download-btn",
                            "text": "下载数据"
                        }
                    },
                    {
                        "id": "snpt1.vu-table1-count1",
                        "clzType": "VUI",
                        "clzKey": "TEXT_LABEL",
                        "dataInitOpt": { "hide": true }
                    }
                ]
            }
        },
        twoTable: {
            html: [
                '<div class="di-o_o-block table-block" data-o_o-di="snpt1.cpnt-table1">',
                '<div class="di-o_o-line">',
                '<div class="di-o_o-item" data-o_o-di="snpt1.vu-table1-download1"></div>',
                '</div>',
                '<div class="di-o_o-line">',
                '<div class="" data-o_o-di="snpt1.vu-table1-breadcrumb1"></div>',
                '</div>',
                '<div class="di-o_o-line">',
                '<div class="vu-table" data-o_o-di="snpt1.vu-table1"></div>',
                '</div>',
                '<div class="di-o_o-line">',
                '<div class="di-table-prompt">',
                '<div class="di-table-count" data-o_o-di="snpt1.vu-table1-count1">',
                '符合查询条件的数据只显示<span class="di-table-count-num">#{currRecordCount}</span>条。',
                '</div>',
                '</div>',
                '</div>',
                '</div>',
                '<div class="di-o_o-block table-block" data-o_o-di="snpt1.cpnt-table2">',
                '<div class="di-o_o-line">',
                '<div class="di-o_o-item" data-o_o-di="snpt1.vu-table1-download2"></div>',
                '</div>',
                '<div class="di-o_o-line">',
                '<div class="" data-o_o-di="snpt1.vu-table1-breadcrumb2"></div>',
                '</div>',
                '<div class="di-o_o-line">',
                '<div class="vu-table" data-o_o-di="snpt1.vu-table2"></div>',
                '</div>',
                '<div class="di-o_o-line">',
                '<div class="di-table-prompt">',
                '<div class="di-table-count" data-o_o-di="snpt1.vu-table1-count2">',
                '符合查询条件的数据只显示<span class="di-table-count-num">#{currRecordCount}</span>条。',
                '</div>',
                '</div>',
                '</div>',
                '</div>'
            ],
            json: {
                "desc" : "查询条件||多维表格",
                "diKey": "DEPICT",
                "clzDefs": [
                    {
                        "clzKey": "OLAP_TABLE",
                        "dataOpt": {
                            "emptyHTML": "未查询到相关数据"
                        }
                    },
                    {
                        "clzKey": "ECUI_SELECT",
                        "dataOpt": {
                            "optionSize": 10
                        }
                    }
                ],
                "entityDefs": [
                    {
                        "id": "snpt1",
                        "clzType": "SNIPPET"
                    },
                    {
                        "id": "snpt1.cpnt-table1",
                        "clzType": "COMPONENT",
                        "clzKey": "DI_TABLE",
                        "sync": { "viewDisable": "ALL" },
                        "init": {
                            "action": { "name": "sync" }
                        },
                        "vuiRef": {
                            "mainTable": "snpt1.vu-table1",
                            "breadcrumb": "snpt1.vu-table1-breadcrumb1",
                            "download": "snpt1.vu-table1-download1",
                            "countInfo": "snpt1.vu-table1-count1"
                        },
                        "init": {
                            "action": { "name": "sync" }
                        }
                    },
                    {
                        "id": "snpt1.vu-table1",
                        "clzType": "VUI",
                        "clzKey": "OLAP_TABLE",
                        "name": "table",
                        "dataOpt": {
                            "rowHCellCut": 30,
                            "hCellCut": 30,
                            "cCellCut": 30,
                            "vScroll": true
                        }
                    },
                    {
                        "id": "snpt1.vu-table1-breadcrumb1",
                        "clzType": "VUI",
                        "clzKey": "BREADCRUMB",
                        "dataOpt": {
                            "maxShow": 6
                        }
                    },
                    {
                        "id": "snpt1.vu-table1-download1",
                        "clzType": "VUI",
                        "clzKey": "H_BUTTON",
                        "dataOpt": {
                            "skin": "ui-download-btn",
                            "text": "下载数据"
                        }
                    },
                    {
                        "id": "snpt1.vu-table1-count1",
                        "clzType": "VUI",
                        "clzKey": "TEXT_LABEL",
                        "dataInitOpt": { "hide": true }
                    },

                    {
                        "id": "snpt1.cpnt-table2",
                        "clzType": "COMPONENT",
                        "clzKey": "DI_TABLE",
                        "sync": { "viewDisable": "ALL" },
                        "init": {
                            "action": { "name": "sync" }
                        },
                        "vuiRef": {
                            "mainTable": "snpt1.vu-table2",
                            "breadcrumb": "snpt1.vu-table1-breadcrumb2",
                            "download": "snpt1.vu-table1-download2",
                            "countInfo": "snpt1.vu-table1-count2"
                        },
                        "init": {
                            "action": { "name": "sync" }
                        }
                    },
                    {
                        "id": "snpt1.vu-table2",
                        "clzType": "VUI",
                        "clzKey": "OLAP_TABLE",
                        "name": "table",
                        "dataOpt": {
                            "rowHCellCut": 30,
                            "hCellCut": 30,
                            "cCellCut": 30,
                            "vScroll": true
                        }
                    },
                    {
                        "id": "snpt1.vu-table1-breadcrumb2",
                        "clzType": "VUI",
                        "clzKey": "BREADCRUMB",
                        "dataOpt": {
                            "maxShow": 6
                        }
                    },
                    {
                        "id": "snpt1.vu-table1-download2",
                        "clzType": "VUI",
                        "clzKey": "H_BUTTON",
                        "dataOpt": {
                            "skin": "ui-download-btn",
                            "text": "下载数据"
                        }
                    },
                    {
                        "id": "snpt1.vu-table1-count2",
                        "clzType": "VUI",
                        "clzKey": "TEXT_LABEL",
                        "dataInitOpt": { "hide": true }
                    }
                ]
            }
        },
        liteolapTableChart: {
            html: [
                '<div class="di-o_o-block cond-block" data-o_o-di="snpt1.cpnt-form1">',
                ' <div class=" di-o_o-line">',
                '<div class=" di-o_o-item" data-o_o-di="snpt1.cpnt-form1-vu-1399874275096_96">',
                '</div>',
                '</div>',
                '</div>',
                // 拖拽区域
                '<div data-o_o-di="snpt1.vctnr-fold1">',
                '</div>',
                ' <div class="di-o_o-line">',
                '<div class="di-o_o-item" data-o_o-di="snpt1.vpt-fold1-ctrlbtn1">',
                ' </div>',
                '</div>',
                '<div data-o_o-di="snpt1.vpt-fold1-body1">',
                ' <div class="ka-block-table-meta" data-o_o-di="snpt1.cpnt-table-meta1">',
                '<div class="ka-table-meta" data-o_o-di="snpt1.vu-table-meta1">',
                '</div>',
                ' </div>',
                '</div>',
                // 表格
                '<div class="di-o_o-block table-block" data-o_o-di="snpt1.cpnt-table1">',
                '<div class="di-o_o-line">',
                '<div class="di-o_o-item" data-o_o-di="snpt1.vu-table1-download1"></div>',
                '</div>',
                '<div class="di-o_o-line">',
                '<div class="" data-o_o-di="snpt1.vu-table1-breadcrumb1"></div>',
                '</div>',
                '<div class="di-o_o-line">',
                '<div class="vu-table" data-o_o-di="snpt1.vu-table1"></div>',
                '</div>',
                '<div class="di-o_o-line">',
                '<div class="di-table-prompt">',
                '<div class="di-table-count" data-o_o-di="snpt1.vu-table1-count1">',
                '符合查询条件的数据只显示<span class="di-table-count-num">#{currRecordCount}</span>条。',
                '</div>',
                '</div>',
                '</div>',
                '</div>',
                // 图形
                ' <div data-o_o-di="snpt1.cpnt-liteolapchart-meta1">',
                '<div class="di-o_o-line">',
                '<span class="di-o_o-item">选择指标：</span><span class="di-o_o-item" data-o_o-di="snpt1.vu-liteolapchart-meta1"></span>',
                '</div>',
                '</div>',
                '<div class="di-o_o-block" data-o_o-di="snpt1.cpnt-liteolapchart1">',
                '<div data-o_o-di="snpt1.vu-liteolapchart1" style="height: 260px">',
                '</div>',
                '</div>'
            ],
            json: {
                "desc" : "查询条件||多维表格",
                "diKey": "DEPICT",
                "clzDefs": [
                    {
                        "clzKey": "OLAP_TABLE",
                        "dataOpt": {
                            "emptyHTML": "未查询到相关数据"
                        }
                    },
                    {
                        "clzKey": "ECUI_SELECT",
                        "dataOpt": {
                            "optionSize": 10
                        }
                    }
                ],
                "entityDefs": [
                    {
                        "id": "snpt1",
                        "clzType": "SNIPPET"
                    },
                    // form
                    {
                        "id": "snpt1.cpnt-form1",
                        "clzType": "COMPONENT",
                        "clzKey": "DI_FORM",
                        "reportType": "RTPL_VIRTUAL",
                        "init": {
                            "action": {
                                "name": "sync"
                            }
                        },
                        "sync": {
                            "viewDisable": "ALL"
                        },
                        "vuiRef": {
                            "input": [
                                "snpt1.cpnt-form1-vu-1399874275096_96"
                            ]
                        },
                        "dataOpt": {
                            "submitMode": "IMMEDIATE"
                        },
                        "reportTemplateId": "RTPL_VIRTUAL_ID"
                    },
//                    {
//                        "id": "snpt1.vu-form1-confirm1",
//                        "clzType": "VUI",
//                        "clzKey": "H_BUTTON",
//                        "dataOpt": {
//                            "skin": "ui-normal-btn",
//                            "text": "查询"
//                        }
//                    },
                    {
                        "id": "snpt1.cpnt-form1-vu-1399874275096_96",
                        "clzType": "VUI",
                        "dataSetOpt": {
                            "forbidEmpty": false,
                            "disableCancelBtn": false,
                            "timeTypeList": [
                                {
                                    "value": "D",
                                    "text": "日"
                                }
//                                {
//                                    "value": "W",
//                                    "text": "周"
//                                },
//                                {
//                                    "value": "M",
//                                    "text": "月"
//                                },
//                                {
//                                    "value": "Q",
//                                    "text": "季"
//                                }
                            ],
                            "timeTypeOpt": {
                                "D": {
                                    "selMode": "SINGLE",
                                    "date": [
                                        "-1D",
                                        "-1D"
                                    ],
                                    "range": [
                                        "2011-01-01",
                                        "-1D"
                                    ],
                                    "selModeList": [
                                        {
                                            "text": "单选",
                                            "value": "SINGLE",
                                            "prompt": "单项选择"
                                        }
                                    ]
                                }
//                                "W": {
//                                    "selMode": "RANGE",
//                                    "date": [
//                                        "-31D",
//                                        "-1D"
//                                    ],
//                                    "range": [
//                                        "2011-01-01",
//                                        "-1D"
//                                    ],
//                                    "selModeList": [
//                                        {
//                                            "text": "单选",
//                                            "value": "SINGLE",
//                                            "prompt": "单项选择"
//                                        },
//                                        {
//                                            "text": "范围多选",
//                                            "value": "RANGE",
//                                            "prompt": "范围选择，点击一下选择开始值，再点击一下选择结束值"
//                                        }
//                                    ]
//                                },
//                                "M": {
//                                    "selMode": "MULTIPLE",
//                                    "date": [
//                                        "-31D",
//                                        "-1D"
//                                    ],
//                                    "range": [
//                                        "2011-01-01",
//                                        "-1D"
//                                    ],
//                                    "selModeList": [
//                                        {
//                                            "text": "单选",
//                                            "value": "SINGLE",
//                                            "prompt": "单项选择"
//                                        },
//                                        {
//                                            "text": "范围多选",
//                                            "value": "RANGE",
//                                            "prompt": "范围选择，点击一下选择开始值，再点击一下选择结束值"
//                                        }
//                                    ]
//                                },
//                                "Q": {
//                                    "selMode": "SINGLE",
//                                    "date": [
//                                        "-31D",
//                                        "-1D"
//                                    ],
//                                    "range": [
//                                        "2011-01-01",
//                                        "-1D"
//                                    ],
//                                    "selModeList": [
//                                        {
//                                            "text": "单选",
//                                            "value": "SINGLE",
//                                            "prompt": "单项选择"
//                                        }
//                                    ]
//                                }
                            }
                        },
                        "name": "dim_time^_^the_date",
                        "dateKey": {
                            "D": "lztd",
                            "W": "lztw",
                            "M": "lztm",
                            "Q": "lztq"
                        },
                        "clzKey": "X_CALENDAR"
                    },
                    // 表格
                    {
                        "id": "snpt1.cpnt-table1",
                        "clzType": "COMPONENT",
                        "clzKey": "DI_TABLE",
                        "sync": { "viewDisable": "ALL" },
                        // 如果有form，就把当前的init给去掉，使用interactions里面的form里面相关的
//                        "init": {
//                            "action": { "name": "sync" }
//                        },
                        "vuiRef": {
                            "mainTable": "snpt1.vu-table1",
                            "breadcrumb": "snpt1.vu-table1-breadcrumb1",
                            "download": "snpt1.vu-table1-download1",
                            "countInfo": "snpt1.vu-table1-count1"
                        },
                        "interactions": [
                            // 如果有form，就用这个form；没有的话，就用上面的init
                            {
                                "events": [
                                    {
                                        "rid": "snpt1.cpnt-form1",
                                        "name": "dataloaded"
                                    },
                                    {
                                        "rid": "snpt1.cpnt-form1",
                                        "name": "submit"
                                    }
                                ],
                                "action": {
                                    "name": "sync"
                                },
                                "argHandlers": [
                                    [
                                        "clear"
                                    ],
                                    [
                                        "getValue",
                                        "snpt1.cpnt-form1"
                                    ]
                                ]
                            },
                            {
                                //"id": "snpt1.cpnt-table1-itrct5",
                                "event": {
                                    "rid": "snpt1.cpnt-table-meta1",
                                    "name": "submit"
                                },
                                "action": {
                                    "name": "sync"
                                },
                                "argHandlers": [
                                    [
                                        "clear"
                                    ],
                                    [
                                        "getValue",
                                        "snpt1.cpnt-form1"
                                    ]
                                ]
                            }
                        ]
                    },
                    {
                        "id": "snpt1.vu-table1",
                        "clzType": "VUI",
                        "clzKey": "OLAP_TABLE",
                        "name": "table",
                        "dataOpt": {
                            "rowHCellCut": 30,
                            "hCellCut": 30,
                            "cCellCut": 30,
                            "vScroll": true,
                            "rowCheckMode": "SELECT"
                        }
                    },
                    {
                        "id": "snpt1.vu-table1-breadcrumb1",
                        "clzType": "VUI",
                        "clzKey": "BREADCRUMB",
                        "dataOpt": {
                            "maxShow": 6
                        }
                    },
                    {
                        "id": "snpt1.vu-table1-download1",
                        "clzType": "VUI",
                        "clzKey": "H_BUTTON",
                        "dataOpt": {
                            "skin": "ui-download-btn",
                            "text": "下载数据"
                        }
                    },
                    {
                        "id": "snpt1.vu-table1-count1",
                        "clzType": "VUI",
                        "clzKey": "TEXT_LABEL",
                        "dataInitOpt": { "hide": true }
                    },
                    // 拖拽容器
                    {
                        "id": "snpt1.vctnr-fold1",
                        "clzType": "VCONTAINER",
                        "clzKey": "FOLD_PANEL",
                        "vpartRef": {
                            "ctrlBtn": "snpt1.vpt-fold1-ctrlbtn1",
                            "body": "snpt1.vpt-fold1-body1"
                        },
                        "dataOpt": {
                            "autoDeaf": false,
                            "autoComponentValueDisabled": false,
                            "autoVUIValueDisabled": false,
                            "defaultHide": false
                        }
                    },
                    {
                        "id": "snpt1.vpt-fold1-body1",
                        "clzType": "VPART"
                    },
                    {
                        "id": "snpt1.vpt-fold1-ctrlbtn1",
                        "clzType": "VPART",
                        "dataOpt": {
                            "expandText": "表格设置",
                            "collapseText": "收起表格设置"
                        }
                    },
                    // 拖拽组件
                    {
                        "id": "snpt1.cpnt-table-meta1",
                        "clzType": "COMPONENT",
                        "clzKey": "OLAP_META_CONFIG",
                        "sync": {
                            "viewDisable": "ALL"
                        },
                        "vuiRef": {
                            "main": "snpt1.vu-table-meta1"
                        },
                        "interactions": [
                            {
                                "rule": {
                                    "operator": "excludes"
                                },
                                "event": {
                                    "rid": "snpt1.cpnt-table1",
                                    "name": "dataloaded.DATA"
                                },
                                "action": {
                                    "name": "sync"
                                }
                            },
                            {
                                "event": {
                                    "rid": "snpt1.cpnt-table1",
                                    "name": "dataloaded.LINK_DRILL"
                                },
                                "action": {
                                    "name": "sync"
                                }
                            },
                            {
                                "event": {
                                    "rid": "snpt1.cpnt-table1",
                                    "name": "dataloaded"
                                },
                                "action": {
                                    "name": "sync"
                                }
                            }
                        ],
                        "dataOpt": {
                            "needShowCalcInds": true,
                            "reportType": "RTPL_OLAP_TABLE",
                            //"submitMode": "CONFIRM"
                            "submitMode": "IMMEDIATE"
                        }
                    },
                    {
                        "id": "snpt1.vu-table-meta1",
                        "clzType": "VUI",
                        "clzKey": "OLAP_META_DRAGGER"
                    },
                    // 图形上面的下拉框
                    {
                        "id": "snpt1.cpnt-liteolapchart-meta1",
                        "clzType": "COMPONENT",
                        "clzKey": "LITEOLAP_META_CONFIG",
                        "sync": {
                            "viewDisable": "ALL"
                        },
                        "vuiRef": {
                            "main": "snpt1.vu-liteolapchart-meta1"
                        },
                        "interactions": [
                            {
                                "event": {
                                    "rid": "snpt1.cpnt-table1",
                                    "name": "dataloaded"
                                },
                                "action": {
                                    "name": "syncLiteOlapInds"
                                }
                            },
                            {
                                "event": {
                                    "rid": "snpt1.cpnt-table1",
                                    "name": "rowselect"
                                },
                                "action": {
                                    "name": "syncLiteOlapInds"
                                }
                            }
                        ],
                        "dataOpt": {
                            "needShowCalcInds": true,
                            "submitMode": "IMMEDIATE",
                            "reportType": "RTPL_OLAP_TABLE",
                            "datasourceId": {
                                "SELECT": "LIST_SELECT"
                            }
                        }
//                        "reportTemplateId": "PERSISTENT^_^zongheng^_^node2^_^2125329485",
//                        "reportType": "RTPL_OLAP_TABLE"
                    },
                    {
                        "id": "snpt1.vu-liteolapchart-meta1",
                        "clzType": "VUI",
                        "clzKey": "OLAP_META_IND_SELECT"
                    },
                    // 图形
                    {
                        "id": "snpt1.cpnt-liteolapchart1",
                        "clzType": "COMPONENT",
                        "clzKey": "DI_LITEOLAP_CHART",
                        "sync": {
                            "viewDisable": "ALL"
                        },
                        "vuiRef": {
                            "mainChart": "snpt1.vu-liteolapchart1"
                        },
                        "interactions": [
                            {
                                "event": {
                                    "rid": "snpt1.cpnt-liteolapchart-meta1",
                                    "name": "submit"
                                },
                                "action": {
                                    "name": "syncLiteOlapChart"
                                },
                                "argHandlers": [
                                    [
                                        "clear"
                                    ],
                                    [
                                        "getValue",
                                        "snpt1.vu-liteolapchart-meta1"
                                    ]
                                ]
                            }
                        ]
//                        "reportTemplateId": "PERSISTENT^_^zongheng^_^node2^_^2125329485",
//                        "reportType": "RTPL_OLAP_TABLE"
                    },
                    {
                        "id": "snpt1.vu-liteolapchart1",
                        "clzType": "VUI",
                        "clzKey": "E_CHART",
                        "dataOpt": {
                            "legend": {
                                "xMode": "pl"
                            },
                            "weekViewRange": [
                                null,
                                "-1d"
                            ]
                        }
                    }
                ]
            }
        },
        handsonTable: {
            html: [
                '<div class="di-o_o-block table-block" data-o_o-di="snpt1.cpnt-table1">',
                '<div class="di-o_o-line">',
                '<div class="vu-table" data-o_o-di="snpt1.vu-table1"></div>',
                '</div>',
                '</div>'
            ],
            json: {
                "desc" : "查询条件||多维表格",
                "diKey": "DEPICT",
                "clzDefs": [
                    {
                        "clzKey": "OLAP_TABLE",
                        "dataOpt": {
                            "emptyHTML": "未查询到相关数据"
                        }
                    },
                    {
                        "clzKey": "ECUI_SELECT",
                        "dataOpt": {
                            "optionSize": 10
                        }
                    }
                ],
                "entityDefs": [
                    {
                        "id": "snpt1",
                        "clzType": "SNIPPET"
                    },
                    {
                        "id": "snpt1.cpnt-table1",
                        "clzType": "COMPONENT",
                        "clzKey": "DI_TABLE",
                        "sync": { "viewDisable": "ALL" },
                        "init": {
                            "action": { "name": "sync" }
                        },
                        "vuiRef": {
                            "mainTable": "snpt1.vu-table1"
                        },
                        "init": {
                            "action": { "name": "sync" }
                        }
                    },
                    {
                        "id": "snpt1.vu-table1",
                        "clzType": "VUI",
                        "clzKey": "HANDSON_TABLE",
                        "name": "table"
                    }
                ]
            }
        },
        // 树结构
        treeChart: {
            html: [
                '<div class="di-o_o-block cond-block" data-o_o-di="snpt1.cpnt-form1">',
                ' <div class=" di-o_o-line">',
                '<div class=" di-o_o-item" data-o_o-di="snpt1.cpnt-form1-vu-1399874275096_96">',
                '</div>',
                '</div>',
                '</div>',
                '<div class="di-o_o-line" style="position:static">',
                '<div class="di-o_o-item" data-o_o-di="snpt1.vu-form1-confirm1">',
                '</div>',
                '</div>',
                '<div class="di-o_o-block" data-o_o-di="snpt1.cpnt-chart1">',
                '<div data-o_o-di="snpt1.vu-chart1"></div>',
                '</div>'
            ],
            json: {
                "desc": "查询条件||多维图形||多维表格",
                "diKey": "DEPICT",
                "clzDefs": [
                    {
                        "clzKey": "OLAP_TABLE",
                        "dataOpt": {
                            "emptyHTML": "未查询到相关数据"
                        }
                    },
                    {
                        "clzKey": "ECUI_SELECT",
                        "dataOpt": {
                            "optionSize": 10
                        }
                    }
                ],
                "entityDefs": [
                    {
                        "id": "snpt1",
                        "clzType": "SNIPPET"
                    },
                    {
                        "id": "snpt1.cpnt-form1",
                        "clzType": "COMPONENT",
                        "clzKey": "DI_FORM",
                        "reportType": "RTPL_VIRTUAL",
                        "init": {
                            "action": {
                                "name": "sync"
                            }
                        },
                        "sync": {
                            "viewDisable": "ALL"
                        },
                        "vuiRef": {
                            "input": [
                                "snpt1.cpnt-form1-vu-1399874275096_96"
                            ],
                            "confirm": "snpt1.vu-form1-confirm1"
                        },
                        "dataOpt": {
                            "submitMode": "IMMEDIATE"
                        },
                        "reportTemplateId": "RTPL_VIRTUAL_ID"
                    },
                    {
                        "id": "snpt1.vu-form1-confirm1",
                        "clzType": "VUI",
                        "clzKey": "H_BUTTON",
                        "dataOpt": {
                            "skin": "ui-normal-btn",
                            "text": "查询"
                        }
                    },
                    {
                        "id": "snpt1.cpnt-form1-vu-1399874275096_96",
                        "clzType": "VUI",
                        "clzKey": "ECUI_INPUT_TREE",
                        "name": "38f9387fd4261d805c33f9d866f0d780",
                        "cfgOpt": { "async": true }
                    },
                    {
                        "id": "snpt1.cpnt-chart1",
                        "clzType": "COMPONENT",
                        "clzKey": "DI_ECHART",
                        "sync": { "viewDisable": "ALL" },
                        "vuiRef": {
                            "mainChart": "snpt1.vu-chart1"
                        },
                        "interactions": [
                            {
                                "events": [
                                    { "rid": "snpt1.cpnt-form1", "name": "submit" },
                                    { "rid": "snpt1.cpnt-form1", "name": "dataloaded" }
                                ],
                                "action": { "name": "sync" },
                                "argHandlers": [
                                    ["clear"],
                                    ["getValue", "snpt1.cpnt-form1"]
                                ]
                            }
                        ]
                    },
                    {
                        "id": "snpt1.vu-chart1",
                        "clzType": "VUI",
                        "clzKey": "E_CHART",
                        "dataOpt": {
                            "height": 260,
                            "legend": { "xMode": "pl" },
                            "weekViewRange": [null, "-1d"]
                        }
                    }
                ]
            }
        }
    };
    return VmJson;
});
