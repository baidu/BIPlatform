# 数据源交互接口 #
     
## 保存数据源（新建和修改）

请求路径

    PUT/POST:/datasources(/[id])
    
前端模拟

    name: '数据源1' //数据源名称
    datasourceType: '' // 数据源类型（MySql、Oracle）//TODO
    hostAndPort: ['127.0.0.1','11.11.11.11'] // 数据库所在服务器地址（包括端口号）
    dbUser: 'use' // 用户名
    dbPwd: '123456' // 密码
    isEncrypt: true // 是否要加密(新建：false；编辑不修改：true；)
    dbInstance: 'dbname' // 数据库名
    
后端模拟

    {
        "status": 0,
        "statusInfo": "提示信息"
    }

## 删除数据源

请求路径

    DELETE:/datasources/[id]
    
后端模拟

    {
        "status": 0,
        "statusInfo": "提示信息"
    }

## 通过ID获取一个数据源

请求路径

    GET:/datasources/[id]
    
后端模拟

    {
        "status": 0, 
        "statusInfo": "提示信息", 
        "data": {
            "name": "数据源123", // 数据源名称 
            "datasourceType": "mySql2", // 数据源类型
            "hostAndPort": ['127.0.0.1','11.11.11.11'], // 数据库地址
            "dbUser": "use", // 用户名
            "dbPwd": "123456", // 密码
            "database": "dbtest", // 数据库名称
            "dbInstance": "lzt" // 用户名
        }
    }
        
## 提取数据源的表

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

## 获取数据源列表

请求路径

    GET:/datasources
    
后端模拟

    {
        "status": 0, 
        "statusInfo": "提示信息", 
        "data": [
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
  