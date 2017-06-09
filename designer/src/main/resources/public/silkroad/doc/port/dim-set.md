
# 获取维度关联的信息： #

请求路径

    GET: reports/[id]/dim-config
    
后台模拟

    {
        "data": {
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
                },
                {}
            ],
             // 时间模块
            "dateRelationTables": [
                {
                    "id": "table1",
                    "name": "表1"
                },
                // 下面是表是选中的情况
                {
                    "id": "table2",
                    "name": "表1",
                     "fields": [
                         {"id": "day", "name": "日"},
                         {"id": "week", "name": "周"},
                         {"id": "month", "name": "月"},
                         {"id": "quarter", "name": "季度"}
                     ],
                     "dateFormatOptions": {
                         "day": ["yyyy-MM-dd", "yyyy/MM/dd"],
                         "week": ["yyyy-W", "yyyy/W"],
                         "month": ["yyyy-MM", "yyyy/MM"],
                         "quarter": ["yyyy-QQ", "yyyy/QQ"]
                     }

                }
            ],
            // 当前表维度字段
            "cubes": {
                "cube1": {
                    "name": "cube测试1",
                    "currDims": [
                        {"id": "dim1", "name": "维度1"},
                        {"id": "dim2", "name": "维度2"},
                        {"id": "dim3", "name": "维度3"}
                    ],
                    // allFields字段在自定义维度里面使用
                    "allFields": ["field1", "field2", "field3", "field1", "field2", "field3" ,"field1", "field2", "field3"]
                },
                "cube2": {
                    "name": "cube测试2",
                    "currDims": [
                        {"id": "dim1", "name": "维度1"},
                        {"id": "dim2", "name": "维度2"},
                        {"id": "dim3", "name": "维度3"}
                    ],
                    "allFields": ["field1", "field2", "field3"]
                }
            },
            "dim": {
               // 普通维度
               "normal": [
                   {
                       cubeId: "cube1",
                       children: [{
                            "currDim": "dim1",  // 主表字段
                            "relationTable": "table1",  // 关联表
                            "field": "table1Field1"    // 关联表字段
                        },
                        {
                            "currDim": "dim1",
                            "relationTable": "table2",
                            "field": "table2Field1"
                        }]
                    },
                    {}
                ],
                // 时间维度
                date: [
                    {
                        "cubeId": "cube1",
                        "children": {[
                            "relationTable": "0",   // 被关联表
                            "currDim": "dim1",  // 当前维度
                            //如果表类型为内置表，粒度选中情况使用此属性
                            //如果表类型为普通表，被关字段联情况使用此属性
                            "field": "day",
                            "format": "yyyy-MM-dd", // 如果表类型为内置表时提交，普通表时不提交
                            // 如果表类型为普通表时提交，内置表时不提交
                            "dateLevel": {
                                  "day": "yyyy-MM-dd",
                                  "week": "yyyy-W",
                                  "month": "yyyy-MM",
                                  "quarter": "yyyy-QQ"
                            }
                        ]}
                    }
                ],
                // 回调维度
                "callback":[
                    {
                        cubeId: "",
                        children: [
                            {
                                "address": "测试地址1",
                                "refreshType": 1,
                                "interval": 10,
                                "currDim": "dim1"
                            },
                            {}
                        ]
                    }
                ],
                // 自定义维度
                "custom": [{
                       cubeId: "",
                       children: [{
                          "dimName": "",
                          "sql": ""
                      }]
                }]
            }
           
        },
        "status": 0,
        "statusInfo": ""
    }


# （时间维度）获取普通表的字段 #

    请求路径
        GET: reports/[id]/tables/[id]

    请求参数
        无

    后台返回
        {
            data: {
                 "fields": [
                     {"id": "day", "name": "日"},
                     {"id": "week", "name": "周"},
                     {"id": "month", "name": "月"},
                     {"id": "quarter", "name": "季度"}
                 ],
                 "dateFormatOptions": {
                     "day": ["yyyy-MM-dd", "yyyy/MM/dd"],
                     "week": ["yyyy-W", "yyyy/W"],
                     "month": ["yyyy-MM", "yyyy/MM"],
                     "quarter": ["yyyy-QQ", "yyyy/QQ"]
                 }
            }
             "status": 0,
             "statusInfo": ""
        }




# （时间维度）获取内置表的数据格式（自己在前端写死） #

    defaultDate: {
        level: [
            {"id": "day", "name": "日"},
            {"id": "week", "name": "周"},
            {"id": "month", "name": "月"},
            {"id": "quarter", "name": "季度"}
       ],
       "dateFormatOptions": {
            "day": ["yyyy-MM-dd", "yyyy/MM/dd"],
            "week": ["yyyy-W", "yyyy/W"],
            "month": ["yyyy-MM", "yyyy/MM"],
            "quarter": ["yyyy-QQ", "yyyy/QQ"]
        }
    }

# 保存维度关联的信息： #

    请求路径

        POST: reports/[id]/dim-config

    参数：

         "normal": "[
             {
                 cubeId: "cube1",
                 children: [{
                      "currDim": "dim1",  // 主表字段
                      "relationTable": "table1",  // 关联表
                      "field": "table1Field1"    // 关联表字段
                  },
                  {
                      "currDim": "dim1",
                      "relationTable": "table2",
                      "field": "table2Field1"
                  }]
              },
              {}
          ]",
          // 时间维度
          "date": "[
              {
                  "cubeId": "cube1",
                  "relationTable": "0",   // 被关联表
                  "currDim": "dim1",  // 当前维度
                  //如果表类型为内置表，粒度选中情况使用此属性
                  //如果表类型为普通表，被关字段联情况使用此属性
                  "field": "day",
                  "format": "yyyy-MM-dd", // 如果表类型为内置表时提交，普通表时不提交
                  // 如果表类型为普通表时提交，内置表时不提交
                  "dateLevel": {
                      "day": "yyyy-MM-dd",
                      "week": "yyyy-W",
                      "month": "yyyy-MM",
                      "quarter": "yyyy-QQ"
                  }
              }
          ]",
          // 回调维度
          "callback": "[
              {
                  cubeId: "",
                  children: [
                      {
                          "address": "测试地址1",
                          "refreshType": 1,
                          "interval": 10, // refreshType等于3时，后端会去读这个属性
                          "currDim": "dim1"
                      },
                      {}
                  ]
              }
          ]",
          // 自定义维度
          "custom": "[{
                 cubeId: "",
                 children: [{
                    "dimName": "",
                    "sql": ""
                }]
          }]"

    后台返回

        {
            "status": 0,
            "statusInfo": ""
        }
