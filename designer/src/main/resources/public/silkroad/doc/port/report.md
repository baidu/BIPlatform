# 报表交互接口 #

## 新建/修改报表 ##

### 获取报表列表

Address

    GET:reports

Response

    {
       "status": 0,
       "statusInfo": "",
       "data": [
           {
               "id": 1,
               "name": ""
           }
       ]
    }

### 获取某一报表

Address

    GET:reports/[id]

Response

    {
       "status": 0,
       "statusInfo": "",
       "data": {
           "id": 1,
           "name": ""
           ...待续
       }
    }

### 发布报表

Address

    GET:reports/[id]/publish

Response

    {
       "status": 0,
       "statusInfo": "",
	   "data": "http://baidu.com"
    }

### 保存报表

Address

    POST:reports/[id]

Form Data

    vm: ''
    json: ''

Response

    {
       "status": 0,
       "statusInfo": "",
	   "data": {}
    }
### 保存json与vm报表

Address

    POST:reports/[id]/json_vm

Form Data

    vm: ''
    json: ''

Response

    {
       "status": 0,
       "statusInfo": "",
	   "data": {}
    }

### 预览报表

Address

    GET:reports/[id]/preview_info

Response

    {
       "status": 0,
       "statusInfo": "",
	   "data": "http://baidu.com"
    }
	
### 删除一报表

Address

    DELETE:reports/[id]

Response

    {
       "status": 0,
       "statusInfo": ""
    }

### 复制一报表(创建报表副本)

Address

    POST:reports/[id]/duplicate   // id:被复制报表的id；name:新报表的名称

Form Data

    name: "报表名称"

Response

    {
       "status": 0,
       "statusInfo": ""
    }

### 新建报表（第一步：命名）

Address

    POST:/reports/

Form Data

    name: "name"

Response

    {
        "status": 0, 
        "statusInfo": "提示信息", 
        "data": {
            "id": 1
        }
    }

### 增加报表数据模型的cube （第二步：建cube）

Address

     POST:/reports/[id]/start_models

Form Data

    "datasourcesId": 1,
    "selectedTables": [id1,id2], // 选中表
    "regexps":["regepx123","regepx2344", "rege456"] //正则表达式  

Response

    {
        "status": 0, 
        "statusInfo": "提示信息"
    }

// TODO 战统定维度设置等接口

### 获取报表所用数据源的选中的表的列表 ###

Address

    GET:/reports/[id]/start_models

Response

    {
        "status": 0,
        "statusInfo": "加载完成",
        "data": {
            "dsId": 1,
            "factTables": [
                {
                    "id": 1,
                    "name": "table_1"
                },
                {
                    "id": 1,
                    "name": "table_1"
                },
                {
                    "id": 1,
                    "name": "table_2_0"
                },
                {
                    "id": 1,
                    "name": "table_2_1"
                },
                {
                    "id": 1,
                    "name": "table_2_x"
                },
                {
                    "id": 1,
                    "name": "table_3_0"
                },
                {
                    "id": 1,
                    "name": "table_3_2"
                }
            ],
            "prefixs": [
                "table_2",
                "table_3"
            ]
        }
    }

### 更改衍生指标 ###

Address

      PUT:reports/[id]/cubes/[id]/derive-inds

Form Data

     data = [
         {
             name: "ACP",
             expr: "点击（click）   /  消费（csm）"
         },
         {
             name: "R",
             expr: "现金（cash）/  消费（csm）"
         }
     ];

Response

     {
         "status": 0,
         "statusInfo": ""
     }

## 编辑报表的数据模型 ##

### 获取cube列表 ###

Address

    GET:/reports/[id]/cubes

Response

    {
        "data": [
            {
                "id": 1,
                "name": "cube1"
            },
            {
                "id": 1,
                "name": "表2"
            }
        ],
        "status": 0,
        "statusInfo": ""
    }

### 获取特定cube的指标列表 ###

Address

    GET:reports/[id]/cubes/[id]/inds

Response

    {
        "data": [
            {
                "id": 1,
                "caption": "原始指标", // 指标的汉字标识
                "name": "english_name", // 对应字段的英文名
                "type": "COMMON", // "COMMON":原始指标；"CAL":衍生指标; "DEFINE":用户自定义
                "aggregator": "SUM", // 汇总方式 SUM,COUNT,AVREAGE
                "visible": true, // 是否可见，true：不可见，false：可见
                "canToDim": 0 // 是否可作为维度，false：不可以，true：可以
            }
        ],
        "status": 0,
        "statusInfo": ""
    }

### 获取指定cube的维度列表 ###

Address

    GET:reports/[id]/cubes/[id]/dims

Response

    {
        "data": {
            "dimList": [
                {
                    "id": 1,
                    "caption": "原始维度",   // 维度的汉字标识
                    "name": "english_name", // 对应字段的英文名
                    "type": "STANDARD_DIMENSION",
                    "canToInd": 0 // 是否可作为指标，false：不可以，true：可以
                },
                {
                    "id": 1,
                    "caption": "",
                    "type": "GROUP_DIMENSION",
                    "levels": [
                        {
                            "id": 1,
                            "name": "",
                            "caption": "english_name", // 对应字段的英文名
                            "type": 0                  // 0：原生，1：维度组
                        }
                    ]
                }
            }
        ],
        "status": 0,
        "statusInfo": ""
    }

### 获取特定cube的信息 ###

Address

    GET:reports/[id]/cubes/[id]

Response

    // 原始指标
    "oriInd": [
        {
            "id": 1,
            "visible": 0 // 0:不选中，1：选中
        }
    ],
    // 原生维度（）
    "oriDim": [
        {
            "id": 1,
            "visible": 0 // 0:不选中，1：选中
        }
    ]

### 修改“筛选显示数据”的数据 ###

Address

    PUT:reports/[id]/cubes/[id]

Form Data

    "oriInd": [
        {
            "id": 1,
            "visible": 0 // 0:不选中(不可见)，1：选中(可见)
        }
    ],
    // 原生维度（）
    "oriDim": [
        {
            "id": 1,
            "selected": 0 // 0:不选中，1：选中
        }
    ]

### 设置指标汇总方式

Address

    PUT:reports/[id]/cubes/[id]/inds/[id]

Form Data

    aggregator: "1" 

Response

    {
        "status": 0,
        "statwusInfo": ""
    }

### 修改指标名称
    
Address

    PUT:reports/[id]/cubes/[id]/inds/[id]

Form Data

    name: ""

Response

    {
        "status": 0,
        "statusInfo": ""
    }

### 修改维度,维度组 的名称

Address

    PUT:reports/[id]/cubes/[id]/dims/[id]

Form Data

    name: ""

Response

    {
        "status": 0,
        "statusInfo": ""
    }
     
### 指标转换到维度
 
Address

    PUT:reports/[id]/cubes/[id]/ind_to_dim/[id]

Form Data

    // 可能将来要做的功能参数
    dimId: 1 // 如果拖到维度组有此参数
    prevDimId: 2  // 前一个维度的id，如果是第一个值为-1 

Response

    {
       "status": 0,
       "statusInfo": ""
    }
     
### 维度转换到指标
 
Address

      PUT:reports/[id]/cubes/[id]/dim_to_ind/[id]

Response

    {
       "status": 0,
       "statusInfo": ""
    }

### 维度拖到维度组

Address

      POST:reports/[id]/cubes/[id]/dim-groups/[dim-group-id]/dim

Form Data

    dimId: 1

Response

    {
       "status": 0,
       "statusInfo": "",
       "data": {
           "canToInd": true
       }
    }

### 对维度组中的维度排序

Address
     
      POST:reports/[id]/cubes/[cubeId]/dim_groups/[dimGroupId]/dim_sorting

Form Data

    groupId:5         // 维度组id
    dimId:1           // 维度id
    beforeDimId:-1    // 落点的前一个维度的id，如果被拖到第一个此值为-1

Response

    {
       "status": 0,
       "statusInfo": ""
    }

### 删除维度组中的维度

Address

     DELETE:reports/[id]/cubes/[id]/dim-groups/[dim-group-id]/dims/[dim-id]

Response

    {
       "status": 0,
       "statusInfo": ""
    }

### 删除维度组

Address
       
    DELETE:reports/[id]/cubes/[id]/dim-groups/[id]  

Response

    {
       "status": 0,
       "statusInfo": ""
    } 

### 创建维度组

Address

    POST:reports/[id]/cubes/[id]/dim-groups

Form Data

    name: "维度组名"

Response

    {
       "status": 0,
       "statusInfo": ""
    }

## 报表编辑时的数据交互（画布区组件的设置与修改）

### 获取一个报表的json

Address

    GET:/reports/{reportId}/json
    
Response

    {
       "status": 0,
       "statusInfo": ""
       "data": ""
    }

### 获取一个报表的vm

Address

    GET:/reports/{reportId}/vm

Response

    {
       "status": 0,
       "statusInfo": ""
       "data": ""
    }

### 添加一个组件

Address

    POST: reports/[report_id]/extend_area/
    
Form Data
    
    type: "TABLE"
    
Response

    {
        "status": 0,
        "statusInfo": "",
        "data": {
            "id": "oo-ss-34-9"
        }
    }

### 获取某一组件的数据配置

Address

    GET: reports/[report_id]/extend_area/[extend_area_id]

Response

    {
        "status": 0,
        "statusInfo": "",
        "data":{
            "xAxis": [
                {
                    "id": "",
                    "caption": "",
                    "name": "",
                    "cubeId": "",
                    "olapElementId": "", // 维度或指标的id
                }
            ],
            "yAxis": [],
            "sAxis": []
        }
    }

### 添加某一组件的数据项

Address
    
    POST: reports/[report_id]/extend_area/[extend_area_id]/item

Form Data

    cubeId: "",
    oLapElementId: "", // 维度或指标的id
    axisType: "x" // x,s

Response

     {
         "status": 0,
         "statusInfo": "",
         "data":{
             "id": "",
             "caption": "",
             "name": "",
             "cubeId": "",
             "oLapElementId": "" // 维度或指标的id
         }
     }

### 删除某一组件的数据配置项

Address

    DELETE: reports/[report_id]/extend_area/[extend_area_id]/item/[id]/[axisType]

Response

    {
        "status": 0,
        "statusInfo": ""
    }
    
### 调整数据配置项顺序

Address

    POST: reports/[report_id]/extend_area/[areaId]/item_sorting  //areaId 组件ID

Form Data

    type: 'x'          // 轴类型，只有x、y周
    source: 1          // 起始位，第一个为0
    target: 0          // 落点的位置，第一个为0

Response

    {
        "status": 0,
        "statusInfo": ""
    }


### 设置--数据格式-请求数据格式信息

Address

    GET: reports/[report_id]/extend_area/[areaId]/dataformat  //areaId 组件ID

Response

    {
        "status": 0,
        "statusInfo": ""
        "data": {
             "defaultFormat": "I,III",
             "chechi_guo":  "I,III.DD",
             "nver_guo": "HH:mm:ss"
        }
    }

### 设置--数据格式--保存数据格式

Address

    POST: reports/[report_id]/extend_area/[areaId]/dataformat

Form Data

    dataFormat: {
         "defaultFormat": "I,III",
         "chechi_guo":  "I,III.DD",
         "nver_guo": "HH:mm:ss"
    }

Response

    {
        "status": 0,
        "statusInfo": ""
        "data": {}
    }