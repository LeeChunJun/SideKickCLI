# OpenGrok API 接口文档

## 概述

OpenGrok RESTful API 提供了代码搜索、项目管理和系统监控等功能。所有接口（除 `/metrics` 外）都位于 `/api/v1` 路径下。

### 认证与访问控制

- 除 `/suggester`, `/search`, `/system/ping`, `/system/indextime` 和 `/metrics` 接口外，其他接口仅允许从 `localhost` 访问
- 如需远程访问，需配置 Bearer Token 并通过 HTTPS 连接使用 `Authorization` HTTP 头进行认证

### 异步操作

部分 API 是异步的，它们返回状态码 202 (Accepted) 和一个 Location 头，其中包含用于检查 API 调用结果的状态端点 URL。一旦状态 API 返回非 202 的结果，客户端应向此 URL 发出 DELETE 请求以清理服务器资源。

---

## 目录

1. [文件相关接口](#1-文件相关接口)
2. [配置管理接口](#2-配置管理接口)
3. [项目管理接口](#3-项目管理接口)
4. [组管理接口](#4-组管理接口)
5. [搜索与建议接口](#5-搜索与建议接口)
6. [系统状态与监控接口](#6-系统状态与监控接口)
7. [消息管理接口](#7-消息管理接口)
8. [仓库管理接口](#8-仓库管理接口)
9. [异步任务状态接口](#9-异步任务状态接口)

---

## 1. 文件相关接口

### 1.1 获取文件注释

**接口**: `GET /api/v1/annotation`

**描述**: 获取文件的版本注释信息

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| path | string | 是 | 文件路径，相对于源码根目录 |

**响应示例** (200 OK):
```json
[
  {
    "revision": "c55d5891",
    "author": "Adam Hornáček",
    "description": "changeset: c55d5891\nsummary: Rewrite README.txt to use markdown syntax\nuser: Adam Hornáček <adam.hornacek@oracle.com>\ndate: Wed Aug 30 17:42:12 CEST 2017",
    "version": "1/15"
  },
  {
    "revision": "5e0c6b22",
    "author": "Vladimir Kotal",
    "description": "changeset: 5e0c6b22\nsummary: bump year\nuser: Vladimir Kotal <vlada@devnull.cz>\ndate: Thu Jul 18 14:43:01 CEST 2019",
    "version": "14/15"
  }
]
```

---

### 1.2 获取文件内容

**接口**: `GET /api/v1/file/content`

**描述**: 获取文件内容，支持不同的 Accept 头

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| path | string | 是 | 文件路径，相对于源码根目录 |

**说明**:
- 如果 Accept 头设置为 `text/plain` 且索引中存在被检测为纯文本的文档，则返回文档内容
- 如果未找到文档，返回 HTTP 404
- 如果文档类型不是纯文本，返回 HTTP 406
- 可以使用 `application/octet-stream` 绕过文档和类型检查

**响应示例** (200 OK):
```
foo
bar
```

---

### 1.3 获取文件类型

**接口**: `GET /api/v1/file/genre`

**描述**: 获取文件的类型（由分析器识别）

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| path | string | 是 | 文件路径，相对于源码根目录 |

**响应示例** (200 OK):
```
PLAIN
```

可能的值：`PLAIN`, `XREFABLE`, `IMAGE`, `DATA`, `HTML`

---

### 1.4 获取文件定义

**接口**: `GET /api/v1/file/defs`

**描述**: 获取文件中定义的符号（函数、参数等）

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| path | string | 是 | 文件路径，相对于源码根目录 |

**响应示例** (200 OK):
```json
[
  {
    "type": "function",
    "signature": "(const unsigned char * in,unsigned char * out,size_t len,const AES_KEY * key,unsigned char * ivec,const int enc)",
    "text": "void AES_cbc_encrypt(const unsigned char *in, unsigned char *out,",
    "symbol": "AES_cbc_encrypt",
    "lineStart": 5,
    "lineEnd": 20,
    "line": 20,
    "namespace": null
  },
  {
    "type": "argument",
    "signature": "(const unsigned char * in,unsigned char * out,size_t len,const AES_KEY * key,unsigned char * ivec,const int enc)",
    "text": "AES_cbc_encrypt(const unsigned char * in,unsigned char * out,size_t len,const AES_KEY * key,unsigned char * ivec,const int enc)",
    "symbol": "in",
    "lineStart": 21,
    "lineEnd": 44,
    "line": 20,
    "namespace": null
  }
]
```

---

### 1.5 获取历史记录

**接口**: `GET /api/v1/history`

**描述**: 获取文件或目录的历史记录条目

**参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| path | string | 是 | - | 文件/目录路径，相对于源码根目录 |
| withFiles | boolean | 否 | false | 是否包含文件列表 |
| start | int | 否 | 0 | 起始索引 |
| max | int | 否 | 1000 | 获取的条目数量 |

**响应示例** (200 OK):
```json
{
  "entries": [
    {
      "revision": "86b0ab6b",
      "date": 1565163646000,
      "author": "Adam Hornacek <adam.hornacek@oracle.com>",
      "tags": null,
      "message": "Try to use mvnw in CI",
      "files": [
        "/opengrok/docker/README.md"
      ]
    },
    {
      "revision": "a391bead",
      "date": 1564745178000,
      "author": "Adam Hornacek <adam.hornacek@oracle.com>",
      "tags": null,
      "message": "Fix docker readme typo",
      "files": [
        "/opengrok/docker/README.md"
      ]
    }
  ],
  "start": 5,
  "count": 3,
  "total": 24
}
```

---

### 1.6 获取目录列表

**接口**: `GET /api/v1/list`

**描述**: 获取目录中的文件和子目录列表

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| path | string | 是 | 文件/目录路径，相对于源码根目录，以 / 开头 |

**响应示例** (200 OK):
```json
[
  {
    "path": "/lucene/.github",
    "numLines": 178,
    "loc": 125,
    "date": 1673456294670,
    "description": null,
    "pathDescription": "",
    "isDirectory": true,
    "size": null
  },
  {
    "path": "/lucene/.asf.yaml",
    "numLines": 25,
    "loc": 21,
    "date": 1673456294670,
    "description": null,
    "pathDescription": "",
    "isDirectory": false,
    "size": 574
  }
]
```

---

## 2. 配置管理接口

### 2.1 获取配置

**接口**: `GET /api/v1/configuration`

**描述**: 返回配置的 XML 表示

**响应示例** (200 OK):
```xml
<?xml version="1.0" encoding="UTF-8"?>
<java version="1.8.0_172" class="java.beans.XMLDecoder">
  <object class="org.opengrok.indexer.configuration.Configuration" id="Configuration0">
    <void property="allowLeadingWildcard">
      <boolean>true</boolean>
    </void>
  </object>
</java>
```

---

### 2.2 设置配置

**接口**: `PUT /api/v1/configuration`

**描述**: 从 XML 表示设置配置（异步操作）

**请求体** (application/xml):
```xml
<?xml version="1.0" encoding="UTF-8"?>
<java version="1.8.0_172" class="java.beans.XMLDecoder">
  <object class="org.opengrok.indexer.configuration.Configuration" id="Configuration0">
    <void property="allowLeadingWildcard">
      <boolean>true</boolean>
    </void>
  </object>
</java>
```

**响应**: 201 Created

**注意**: 这是异步 API 端点

---

### 2.3 获取配置字段

**接口**: `GET /api/v1/configuration/{field}`

**描述**: 返回特定的配置字段值

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| field | string | 是 | 配置字段名称 |

**响应**: 200 OK (application/json)

---

### 2.4 设置配置字段

**接口**: `PUT /api/v1/configuration/{field}`

**描述**: 设置特定的配置字段值（异步操作）

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| field | string | 是 | 配置字段名称 |
| reindex | boolean | 否 | 指定底层数据是否也被重新索引 |

**请求体** (application/text):
```
string value of the field to set
```

**响应**: 204 No Content

**注意**: 这是异步 API 端点

---

### 2.5 重载授权框架

**接口**: `POST /api/v1/configuration/authorization/reload`

**描述**: 重载授权框架（异步操作）

**请求体**: application/text

**响应**: 204 No Content

**注意**: 这是异步 API 端点

---

## 3. 项目管理接口

### 3.1 获取所有项目

**接口**: `GET /api/v1/projects`

**描述**: 返回所有项目的列表

**响应示例** (200 OK):
```json
["foo", "bar"]
```

---

### 3.2 添加项目

**接口**: `POST /api/v1/projects`

**描述**: 添加项目及其仓库到 Web 应用配置

**请求体** (text/plain):
```
project-name
```

**响应**: 201 Created

**说明**: 
- 项目仅在索引后才可搜索并在 UI 中显示
- 如果项目已存在且启用了历史记录，将刷新其仓库列表

---

### 3.3 删除项目

**接口**: `DELETE /api/v1/projects/{project}`

**描述**: 删除项目及其仓库配置和相关数据

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| project | string | 是 | 项目名称 |

**响应**: 204 No Content

**说明**: 同时删除索引、交叉引用页面、历史索引和建议器数据

---

### 3.4 删除项目索引数据

**接口**: `DELETE /api/v1/projects/{project}/data`

**描述**: 删除项目索引数据但保留项目配置

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| project | string | 是 | 项目名称 |

**响应**: 204 No Content

**说明**: 项目数据保持不变，标记项目为未索引状态，可从零开始重新索引

---

### 3.5 删除历史缓存

**接口**: `DELETE /api/v1/projects/{project}/historycache`

**描述**: 删除项目的历史缓存

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| project | string | 是 | 项目名称 |

**响应示例** (200 OK):
```json
["/project/repository1", "/project/repository2"]
```

---

### 3.6 删除注释缓存

**接口**: `DELETE /api/v1/projects/{project}/annotationcache`

**描述**: 删除项目的注释缓存

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| project | string | 是 | 项目名称 |

**响应示例** (200 OK):
```json
["/project/repository1", "/project/repository2"]
```

---

### 3.7 标记项目为已索引

**接口**: `PUT /api/v1/projects/{project}/indexed`

**描述**: 标记项目为已索引（异步操作）

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| project | string | 是 | 项目名称 |

**请求体**: 空

**响应**: 204 No Content

**说明**: 此端点由 Indexer 在完成项目索引后使用

---

### 3.8 获取项目文件列表

**接口**: `GET /api/v1/projects/{project}/files`

**描述**: 获取索引数据库跟踪的项目文件列表

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| project | string | 是 | 项目名称 |

**响应示例** (200 OK):
```json
["/project/foo.txt", "/project/bar.txt"]
```

---

### 3.9 设置项目属性

**接口**: `PUT /api/v1/projects/{project}/property/{propertyname}`

**描述**: 设置项目的属性值

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| project | string | 是 | 项目名称 |
| propertyname | string | 是 | 属性名称 |

**请求体**: 要设置的值的字符串表示

**响应**: 204 No Content

**参考**: https://github.com/oracle/opengrok/wiki/Per-project-configuration#properties

---

### 3.10 获取项目属性

**接口**: `GET /api/v1/projects/{project}/property/{propertyname}`

**描述**: 返回项目的属性值

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| project | string | 是 | 项目名称 |
| propertyname | string | 是 | 属性名称 |

**响应**: 200 OK (application/json)

---

### 3.11 获取项目仓库列表

**接口**: `GET /api/v1/projects/{project}/repositories`

**描述**: 返回指定项目的仓库列表

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| project | string | 是 | 项目名称 |

**响应示例** (200 OK):
```json
[
  "/opengrok-master/testdata/repositories/teamware",
  "/opengrok-master/testdata/sources/rfe2575",
  "/opengrok-master/testdata/repositories/rcs_test",
  "/opengrok-master"
]
```

---

### 3.12 获取项目仓库类型

**接口**: `GET /api/v1/projects/{project}/repositories/type`

**描述**: 返回项目仓库的类型

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| project | string | 是 | 项目名称 |

**响应示例** (200 OK):
```json
["RCS", "git", "SCCS"]
```

---

### 3.13 获取已索引项目列表

**接口**: `GET /api/v1/projects/indexed`

**描述**: 返回已索引的项目列表

**响应示例** (200 OK):
```json
["sc-2", "sc-1"]
```

---

## 4. 组管理接口

### 4.1 获取所有组

**接口**: `GET /api/v1/groups`

**描述**: 返回所有组的列表

**响应示例** (200 OK):
```json
["foo", "bar"]
```

---

### 4.2 获取组的所有项目

**接口**: `GET /api/v1/groups/{group}/allprojects`

**描述**: 返回给定组的所有项目列表（包括子组的项目）

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| group | string | 是 | 组名称 |

**响应示例** (200 OK):
```json
["foo", "bar"]
```

---

### 4.3 获取组模式

**接口**: `GET /api/v1/groups/{group}/pattern`

**描述**: 返回给定组的匹配模式

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| group | string | 是 | 组名称 |

**响应示例** (200 OK):
```
^foo-.*
```

---

### 4.4 检查组匹配

**接口**: `POST /api/v1/groups/{group}/match`

**描述**: 检查（项目）名称是否匹配组模式

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| group | string | 是 | 组名称 |

**请求体** (text/plain):
```
project-name
```

**响应**: 
- 200 OK - 匹配成功
- 204 No Content - 不匹配

---

## 5. 搜索与建议接口

### 5.1 执行搜索

**接口**: `GET /api/v1/search`

**描述**: 执行代码搜索并返回结果

**参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| full | string | 否 | - | 全文搜索字段值 |
| def | string | 否 | - | 定义字段值 |
| symbol | string | 否 | - | 符号字段值 |
| path | string | 否 | - | 文件路径字段值 |
| hist | string | 否 | - | 历史字段值 |
| type | string | 否 | - | 文件类型 |
| projects | string | 否 | - | 要搜索的项目 |
| maxresults | string | 否 | 1000 | 最大返回文档数 |
| start | string | 否 | 0 | 结果起始索引 |
| sort | string | 否 | relevancy | 排序方式：relevancy, fullpath, lastmodtime |

**响应示例** (200 OK):
```json
{
  "time": 1229,
  "resultCount": 8,
  "startDocument": 0,
  "endDocument": 7,
  "results": {
    "/onnv/usr/src/lib/libnisdb/db_pickle.cc": [
      {
        "line": "pickle_file::<b>transfer</b>(pptr p, bool_t (*f) (XDR*, pptr))",
        "lineNumber": "106",
        "tag": "function in pickle_file"
      }
    ],
    "/onnv/usr/src/cmd/pools/poold/com/sun/solaris/service/pools/Resource.java": [
      {
        "line": "\tpublic void <b>transfer</b>(Resource donor, long qty) throws PoolsException",
        "lineNumber": "93",
        "tag": "method in Resource"
      },
      {
        "line": "\tpublic void <b>transfer</b>(Resource donor, List components)",
        "lineNumber": "110",
        "tag": "method in Resource"
      }
    ]
  }
}
```

---

### 5.2 获取建议

**接口**: `GET /api/v1/suggest`

**描述**: 返回搜索建议

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| projects | string | 是 | 要获取建议的项目列表 |
| field | string | 是 | 建议字段 |
| caret | string | 是 | 输入字段中的光标位置 |
| full | string | 否 | 全文搜索输入值 |
| defs | string | 否 | 定义输入值 |
| refs | string | 否 | 符号输入值 |
| path | string | 否 | 文件路径输入值 |
| hist | string | 否 | 历史输入值 |
| type | string | 否 | 类型输入值 |

**响应示例** (200 OK):
```json
{
  "time": 60,
  "suggestions": [{
    "phrase": "package",
    "projects": ["kotlin"],
    "score": 387
  }],
  "identifier": "pprttq",
  "queryText": "pprttq",
  "partialResult": false
}
```

---

### 5.3 获取建议器配置

**接口**: `GET /api/v1/suggest/config`

**描述**: 返回建议器配置

**响应示例** (200 OK):
```json
{
  "enabled": true,
  "maxResults": 10,
  "minChars": 4,
  "allowedProjects": null,
  "maxProjects": 2147483647,
  "allowedFields": ["defs", "path", "hist", "refs", "type", "full"],
  "allowComplexQueries": true,
  "allowMostPopular": true,
  "showScores": false,
  "showProjects": true,
  "showTime": false,
  "rebuildCronConfig": "0 0 * * *",
  "buildTerminationTime": 1800,
  "timeThreshold": 2000
}
```

---

### 5.4 基于查询初始化流行度数据

**接口**: `POST /api/v1/suggest/init/queries`

**描述**: 基于查询更新流行度数据

**请求体** (application/json):
```json
["http://localhost:8080/source/search?project=kotlin&full=text"]
```

**响应**: 204 No Content

---

### 5.5 基于原始数据初始化流行度

**接口**: `POST /api/v1/suggest/init/raw`

**描述**: 基于提供的数据更新流行度数据

**请求体** (application/json):
```json
[{"project": "kotlin", "field": "full", "token": "args", "increment": 100}]
```

**响应**: 204 No Content

---

### 5.6 获取流行度数据

**接口**: `GET /api/v1/suggest/popularity/{project}`

**描述**: 检索项目的流行度数据

**参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| project | string | 是 | - | 项目名称 |
| field | string | 否 | full | 检索数据的字段 |
| page | int | 否 | 0 | 数据页码 |
| pageSize | int | 否 | 100 | 页面大小 |
| all | boolean | 否 | false | 是否检索所有数据（忽略 page 和 pageSize） |

**响应示例** (200 OK):
```json
[{"iso": 3}, {"the": 3}, {"to": 3}, {"conform": 3}, {"-": 2},
 {"sc_window_set": 1}, {"_lso_outbound": 1}, {"missing": 1}]
```

---

### 5.7 重建所有项目的建议器数据

**接口**: `PUT /api/v1/suggest/rebuild`

**描述**: 重建所有项目的建议器数据（后台执行）

**响应**: 204 No Content

**说明**: 重建可能在 API 调用返回后一段时间才完成

---

### 5.8 重建指定项目的建议器数据

**接口**: `PUT /api/v1/suggest/rebuild/{project}`

**描述**: 重建给定项目的建议器数据（后台执行）

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| project | string | 是 | 项目名称 |

**响应**: 204 No Content

**说明**: 重建可能在 API 调用返回后一段时间才完成

---

## 6. 系统状态与监控接口

### 6.1 Ping 检查

**接口**: `GET /api/v1/system/ping`

**描述**: 检查 Web 应用是否已部署并运行

**响应**: 200 OK

**说明**: 此端点由 Indexer 在使用 -U 选项时使用

---

### 6.2 获取版本信息

**接口**: `GET /api/v1/system/version`

**描述**: 获取 Web 应用版本字符串

**响应示例** (200 OK):
```
1.9.0 (a5ac05426bc5029158fedffee1cd44abf033bb61)
```

---

### 6.3 获取最后索引时间

**接口**: `GET /api/v1/system/indextime`

**描述**: 检索最后索引时间（ISO 8601 格式，UTC 时区）

**响应示例** (200 OK):
```json
"2021-02-15T16:39:16.409+00:00"
```

---

### 6.4 重载包含文件

**接口**: `PUT /api/v1/system/includes/reload`

**描述**: 重载 Web 应用的所有包含文件

**响应**: 204 No Content

---

### 6.5 更新路径描述

**接口**: `POST /api/v1/system/pathdesc`

**描述**: 更新 Web 应用的路径描述

**请求体** (application/json):
```json
[
  {"path": "/foo", "description": "foo foo"},
  {"path": "/bar", "description": "bar"}
]
```

**响应**: 204 No Content

**说明**: 
- 路径相对于源码根目录，以 / 开头
- 描述存储在数据根目录下的文件中，无需在每次重新部署后手动加载

---

### 6.6 获取监控指标

**接口**: `GET /metrics/prometheus`

**描述**: 以 Prometheus 格式检索 Web 应用指标

**注意**: 此端点不在 `/api/v1` 路径下

**响应**: 200 OK (text/plain)

---

## 7. 消息管理接口

### 7.1 添加消息

**接口**: `POST /api/v1/messages`

**描述**: 向系统添加消息

**请求体** (application/json):
```json
{
  "tags": ["main"],
  "messageLevel": "info",
  "text": "test message",
  "duration": "PT10M"
}
```

**响应**: 201 Created

**说明**:
- `messageLevel` 可用值：`success`, `info`, `warning`, `error`
- `tags` 可用值：`main`（顶级消息）、项目名称、项目组名称
- `duration` 格式遵循 Java 的 `java.time.Duration` 类

---

### 7.2 删除消息

**接口**: `DELETE /api/v1/messages`

**描述**: 删除具有指定标签的消息

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| tag | string | 是 | 标签 |

**请求体** (text/plain):
```
message text to delete
```

**响应**: 204 No Content

**说明**: 仅删除具有指定文本和标签的消息

---

### 7.3 获取消息

**接口**: `GET /api/v1/messages`

**描述**: 返回具有指定标签的所有消息

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| tag | string | 是 | 标签 |

**响应示例** (200 OK):
```json
[
  {
    "expired": false,
    "created": "2019-01-23 20:39:31 CET",
    "tags": ["main"],
    "expiration": "2019-01-23 20:49:31 CET",
    "cssClass": "class",
    "text": "test&nbsp;message"
  }
]
```

---

## 8. 仓库管理接口

### 8.1 获取仓库字段值

**接口**: `GET /api/v1/repositories/property/{field}`

**描述**: 返回仓库的字段值

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| field | string | 是 | 字段名称 |
| repository | string | 否 | 仓库路径（使用本机路径分隔符，以路径分隔符开头） |

**可用字段**:
- `working` (boolean) - 仓库是否能运行底层 SCM 命令
- `type` (string) - SCM 类型
- `remote` (boolean) - SCM 源是否为远程
- `parent` (string) - SCM 的来源/父级
- `branch` (string) - 分支标识
- `currentVersion` (string) - 当前修订 ID
- `handleRenamedFiles` (boolean) - 是否为历史缓存生成处理重命名文件
- `historyEnabled` (boolean) - 是否启用历史
- `annotationCacheEnabled` (boolean) - 是否启用注释缓存

**响应**: 200 OK (application/json)

---

## 9. 异步任务状态接口

### 9.1 检查任务状态

**接口**: `GET /api/v1/status/{uuid}`

**描述**: 检查 API 请求的状态

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| uuid | string | 是 | 任务 UUID |

**响应**: 202 Accepted（仍在处理中）或其他状态码（已完成）

---

### 9.2 删除任务状态

**接口**: `DELETE /api/v1/status/{uuid}`

**描述**: 删除与 API 请求跟踪关联的状态

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| uuid | string | 是 | 任务 UUID |

**响应**: 200 OK

**说明**: 仅在 API 请求完成后执行（即 GET 请求返回适当的状态码如 201 后）

---

## 附录

### HTTP 状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | OK - 请求成功 |
| 201 | Created - 资源创建成功 |
| 202 | Accepted - 请求已接受，正在处理（异步操作） |
| 204 | No Content - 操作成功但无返回内容 |
| 400 | Bad Request - 请求参数错误 |
| 404 | Not Found - 资源不存在 |
| 406 | Not Acceptable - 不接受的内容类型 |
| 500 | Internal Server Error - 服务器内部错误 |

### 时间格式

- **ISO 8601**: `2021-02-15T16:39:16.409+00:00`
- **Unix 时间戳（毫秒）**: `1565163646000`
- **Duration**: Java `java.time.Duration` 格式，如 `PT10M` 表示 10 分钟

### 注意事项

1. **配置持久化**: 修改 Web 应用配置的接口，需要检索并存储到磁盘才能持久化（例如在应用服务器重启或 Web 应用重新部署时）

2. **异步操作**: 部分 API 是异步的，返回 202 状态码和 Location 头。客户端应轮询状态端点，完成后发送 DELETE 请求清理资源

3. **路径格式**: 所有路径参数都是相对于源码根目录的，通常以 `/` 开头

4. **认证**: 除公开接口外，其他接口需要 Bearer Token 认证并通过 HTTPS 访问

---

**文档版本**: 1.0  
**最后更新**: 2026-05-20  
**API 总数**: 41 个接口
