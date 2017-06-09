# 数据源交互接口 #

## 新建/修改数据源 ##

### 获取数据源列表ok

请求路径

    GET:/datasources
    
后端模拟

    {
        "status": 0, 
        "statusInfo": "提示信息", 
        "data": {
            [
                {
                    "id": 1,
                    "name": "数据源1"
                },
                {
                    "id": 1,
                    "name": "数据源1"
                }
            ]
        }
    }
    
### 通过ID获取一个数据源ok

请求路径

    GET:/datasources/[id]
    
后端模拟

    {
        "status": 0, 
        "statusInfo": "提示信息", 
        "data": {
            "datasourceName": "数据源123", // 数据源名称 
            "datasourceType": "mySql2", // 数据源类型
            "address": "127.0.0.1,localhost,133.33.33", // 数据库地址
            "password": "123456", // 密码
            "database": "dbtest", // 数据库名称
            "userName": "lzt", // 用户名
            "selectedTables": "id1,id2", // 选中表
            "allTables":[   // 所有表
                { "id": "id1", "name": "cube测试1"},  
                { "id": "id2", "name": "cube测试2"}, 
                { "id": "id3", "name": "cube测试3"}
            ], 
            "regexps":["regepx123","regepx2344", "rege456"] //正则表达式
        }
    }
    
### 提取数据源的表ok

请求路径

    GET:/datasources/[id]/tables
    
后端模拟

    {
        "status": 0,
        "statusInfo": "提示信息",
        "data": [
            {
                "id": "table1",
                "name": "table1"
            },
            {
                "id": "table2",
                "name": "table2"
            },
            {
                "id": "table3",
                "name": "table3"
            }
        ]
    }



### 正则表达式匹配表

请求路径

    GET: /datasources/search/matchedtables
    
前端模拟

    selectedTables: 'dataSource1,dataSource2' // 选中表
    regexps: ["/REGEX/"] // 正则表达式
    
后端模拟

    {
        "status": 0, 
        "statusInfo": "提示信息", 
        "data": {
            "relation": {
                "regexp123": "数据源1",
                "regexp456": "数据源2"
            }
        }
    }


    
### 保存数据源（新建和修改）

请求路径

    PUT/POST:/datasources
    
前端模拟

    datasourceName: '数据源1' //数据源名称
    datasourceType: '' // 数据源类型（MySql、Oracle）//TODO
    address: '127.0.0.1,11.11.11.11' // 数据库所在服务器地址（包括端口号）
    userName: 'use' // 用户名
    password: '123456' // 密码
    isEncrypt: true // 是否要加密
    database: 'dbname' // 数据库名
    selectedTables: 'dataSource1,dataSource2' // 选中表
    regexps: ["/REGEX/"] // 正则表达式
    
后端模拟

    {
        "status": 0,
        "statusInfo": "提示信息",
        "data": [
            {
                "id": "dataSource1",
                "name": "数据源1"
            },
            {
                "id": "dataSource2",
                "name": "数据源2"
            },
            {
                "id": "dataSource3",
                "name": "数据源3"
            }
        ]
    }
    
        
## 配置数据源 ##

### 获取指定数据源的cube列表 ###

请求路径
    
    GET:/datasources/[id]/cubes

后台模拟数据

    {
        "data": {
            "cubeList": [
                {
                    "id": 1,
                    "name": "cube1"
                },
                {
                    "id": 1,
                    "name": "表2"
                }
            ]
        },
        "status": 0,
        "statusInfo": ""
    }


### 获取特定cube的指标列表 ###

请求路径

    GET:datasources/[id]/cubes/[id]/inds
    
    后台模拟
    {
        "data": {
            "indList": [
                {
                    "id": 1,
                    "name": "原始指标",
                    "tag": "english_name", // 对应字段的英文名
                    "type": 0,
                    "methodType": 0,
                    "changeToDim": 0 // 是否可作为维度，0：不可以，1：可以
                },
                {
                    "id": 1,
                    "name": "衍生指标",
                    "type": 1,
                    "methodType": 0,
                    "expr": "",
                    "visible": 0 // 是否可见，0：不可见，1：可见
                }
            ]
        },
        "status": 0,
        "statusInfo": ""
    }

### 获取指定cube的维度列表 ###
	
请求路径

	GET:datasources/[id]/cubes/[id]/dims
	
后台模拟

    {
        "data": {
            "dimList": [
                {
                    "id": 1,
                    "name": '',
                    "tag": "english_name", // 对应字段的英文名
                    "type": 0, //0：原生，1：维度组
                    "changeToInd": 0 // 是否可作为指标，0：不可以，1：可以
                },
                {
                    "id": 1,
                    "name": '',
                    "type": 1, //0：原生，1：维度组
                    "dimList": [
                        {
                            "id": 1,
                            "name": '',
                            "tag": "english_name", // 对应字段的英文名
                            "type": 0, //0：原生，1：维度组
                            "changeToInd": 0 // 是否可作为指标，0：不可以，1：可以
                        }
                    ]
                }
            ]
        },
        "status": 0,
        "statusInfo": ""
    }

### 获取“筛选显示数据”的数据 ###

请求路径
    
    GET:datasources/[id]/cubes/[id]/show-config
    
后台模拟

    {
        "data": {
            "oriInd": [
                {
                    "id": 1,
                    "name": "指标名",
                    "selected": 0 //0:不选中，1：选中
                }
            ],
            // 原生维度（）
            "oriDim": [
                {
                    "id": 1,
                    "name": "",
                    "selected": 0 //0:不选中，1：选中
                }
            ]
        },
        "status": 0
    }
    
### 提交“筛选显示数据”设置结果 ###

请求路径
    
    PUT:datasources/[id]/cubes/[id]/show-config
    
后台模拟

    {
        "data": {
            "oriInd": [
                {
                    "id": "ind_name",
                    "name": "指标名1",
                    "selected": 0 //0:不选中，1：选中
                },
                {
                    "id": "ind_name2",
                    "name": "指标名2",
                    "selected": 1
                }
            ],
            // 原生维度（）
            "oriDim": [
                {
                    "id": "dim_name",
                    "name": "维度1",
                    "selected": 0 //0:不选中，1：选中
                },
                {
                    "id": "dim_name2",
                    "name": "维度2",
                    "selected": 1
                }
            ]
        },
        "status": 0
    }
    
### 设置指标汇总方式
    
请求路径

    PUT:datasources/[id]/cubes/[id]/inds/[id]/method-type

前台数据模拟

    {
        method: 1
    }

后台数据模拟

    {
        "status": 0
    }
    
### 衍生指标管理 ###

请求路径

     PUT:datasources/[id]/cubes/[id]/derive-inds
     
前台模拟

    data = [
        {
            name: 'ACP',
            expr: '点击（click）   /  消费（csm）'
        },
        {
            name: 'R',
            expr: '现金（cash）/  消费（csm）'
        }
    ];

后台模拟
    
    data = {
        status: 0,
        statusInfo: ''
    };

### 指标转换到维度

请求路径

     PUT:datasources/[id]/cubes/[id]/ind-to-dim/[id]
     
前台模拟

    {
        groupId: '' //维度组id(如果拖到维度组，有此参数)
        index: 0 //从零开始(如果拖到维度组，有此参数)
    }
    
### 维度转换到指标

请求路径

     PUT:datasources/[id]/cubes/[id]/dim-to-ind/[id]
     
    
### 维度拖到维度组/将维度从一个维度组拖到另一个维度组
    
请求路径
    
     POST:datasources/[id]/cubes/[id]/dim-groups/[dim-group-id]/dim/[dim-id]
         
前台模拟

    {
        index: 0 //从零开始
    }        
    
### 维度拖出维度组
     
 请求路径
     
      DELETE:datasources/[id]/cubes/[id]/dim-groups/[dim-group-id]/dim/[dim-id]
 
    
### 删除维度组
      
  请求路径
      
      DELETE:datasources/[id]/cubes/[id]/dim-groups/[id]  
           
### 获取维度关联的信息： ###
 
 请求路径

    GET:datasources/[id]/cubes/[id]/dim-config
    
后台模拟

    {
        "data": {
            "cubeName": "测试表",
            // 关联表数据
            "relationTables": [
                {
                    "id": "table1", 
                    "name": "表1", 
                    // 指定关联字段全集（关联字段，使用id；指定所需字段，使用name）
                    "fields": [
                        {"id": "table1Field1", "name": "普通表1-字段1"},
                        {"id": "table1Field2", "name": "普通表1-字段2"}
                    ], 
                }
            ],
            // 时间模块
            "dateRelationTables": [{
                "id": "table1", 
                "name": "表1", 
                "type": 1, // 1：是内置表的类型； 2：普通表的类型
                
                // 指定关联字段全集（关联字段，使用id；选择日期格式，使用name）
                // 如果表类型为1，使用此全集
                // 如果表类型为2，使用此全集
                "fields": [
                    {"id": "day", "name": "日"}, 
                    {"id": "week", "name": "周"}, 
                    {"id": "month", "name": "月"}, 
                    {"id": "quarter", "name": "季度"}
                ],
                
                // 时间粒度对应的时间格式
                // 如果表类型为1，粒度后面的时间格式使用
                // 如果表类型为2，选择时间格式多选框后面的时间格式使用
                // 以下数据为模拟数据，不代表实际意义
                "dateFormatOptions": {
                    "day": ["yyyy-MM-dd", "yyyy/MM/dd"],
                    "week": ["yyyy-W", "yyyy/W"],
                    "month": ["yyyy-MM", "yyyy/MM"],
                    "quarter": ["yyyy-QQ", "yyyy/QQ"]
                },
                
                // 内置表，时间粒度可选的上级粒度
                dateLevel: {
                    "day": [
                        {"id": "day", "name": "日"}, 
                        {"id": "week", "name": "周"}, 
                        {"id": "month", "name": "月"}, 
                        {"id": "quarter", "name": "季度"}
                    ],
                    "week": [{"id": "week", "name": "周"}],
                    "month": [{"id": "month", "name": "月"}, {"id": "quarter", "name": "季"}],
                    "quarter": [{"id": "quarter", "name": "季"}]
                }
            }],
            // 当前表维度字段
            "currDims": [
                {"id": "dim1", "name": "维度1"},
                {"id": "dim2", "name": "维度2"},
                {"id": "dim3", "name": "维度3"}
            ],
            "dim": {
               // 普通维度
               "normal": [{
                    "relationTable": "table1",
                    "currDim": "dim1",
                    "field": "table1Field1",
                    "selectedViewFields": ["table1ViewField1"]
                },
                {
                    "relationTable": "table2",
                    "currDim": "dim1",
                    "field": "table2Field1",
                    "selectedViewFields": ["table2ViewField1","table2ViewField2"]
                }],
                // 时间维度
                relationdDate: {
                    //被关联表
                    relationTable: '',
                    //如果表类型为1时使用，不为1时不提交
                    format: 'yyyy-MM-dd',
                    //当前维度
                    currDim: '',
                    //如果表类型为1，粒度选中情况使用此属性
                    //如果表类型为2，被关字段联情况使用此属性
                    field: 'day',
                    //如果表类型为2，selected与format都会使用（请求时需要提交）
                    //如果表类型为1，只用selected（请求时提交selected）
                    "dateLevel": {
                       "day": {
                            "selected": true,
                            "format": "yyyy-MM-dd"
                        },
                        "week": {
                            "selected": false,
                            "format": "yyyy-W"
                        },
                        "month": {
                            "selected": false,
                            "format": "yyyy-MM"
                        },
                        "quarter": {
                            "selected": true,
                            "format": "yyyy-QQ"
                        }
                    }
                },
                // 回调维度
                "callback": [{
                    "address": "测试地址1",
                    "refreshType": 1,
                    "interval": 10,
                    "currDim": "dim1" 
                },
                {
                    "address": "测试地址2",
                    "refreshType": 3,
                    "interval": 20,
                    "currDim": "dim1"  
                }]
            }
           
        },
        "status": 0,
        "statusInfo": ""
    }
    
 ### 提交维度关联信息： ###   
    
 请求路径

    POST:datasources/[id]/cubes/[id]/dim-config
    
请求参数：

    // 普通维度
    "normal": [{
        "relationTable": "table1",
        "currDim": "dim1",
        "field": "table1Field1",
        "selectedViewFields": ["table1ViewField1"]
    },{
        "relationTable": "table2",
        "currDim": "dim1",
        "field": "table2Field1",
        "selectedViewFields": ["table2ViewField1","table2ViewField2"]
    }],
    // 时间维度
    relationdDate: {
        //被关联表
        relationTable: '',
        //如果表类型为1时使用，不为1时不提交
        format: 'yyyy-MM-dd',
        //当前维度
        currDim: '',
        //如果表类型为1，粒度选中情况使用此属性
        //如果表类型为2，被关字段联情况使用此属性
        field: 'day',
        //如果表类型为2，selected与format都会使用（请求时需要提交）
        //如果表类型为1，只用selected（请求时提交selected）
        "dateLevel": {
           "day": {
                "selected": true,
                "format": "yyyy-MM-dd"
            },
            "week": {
                "selected": false,
                "format": "yyyy-W"
            },
            "month": {
                "selected": false,
                "format": "yyyy-MM"
            },
            "quarter": {
                "selected": true,
                "format": "yyyy-QQ"
            }
        }
    },
    // 回调维度
    "callback": [{
        "address": "测试地址1",
        "refreshType": 1,
        "interval": 10,
        "currDim": "dim1" 
    },
    {
        "address": "测试地址2",
        "refreshType": 3,
        "interval": 20,
        "currDim": "dim1"  
    }]
            
后台模拟：

    {
        "data": {
            "dimList": [
                {
                    "id": 1,
                    "name": '',
                    "type": 0, //0：原生，1：维度组
                    "changeToInd": 0 // 是否可作为指标，0：不可以，1：可以
                },
                {
                    "id": 1,
                    "name": '',
                    "type": 1, //0：原生，1：维度组
                    "dimList": [
                        {
                            "id": 1,
                            "name": '',
                            "type": 0, //0：原生，1：维度组
                            "changeToInd": 0 // 是否可作为指标，0：不可以，1：可以
                        }
                    ]
                }
            ]
        },
        "status": 0,
        "statusInfo": ""
    }       
    
datasources/dataSource1/cubes/1/inds/4/name
       
创建维度组

datasources/dataSource1/cubes/1/inds/4/name