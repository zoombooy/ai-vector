# API 接口文档

## 基础信息

- **Base URL**: `http://localhost:8080`
- **认证方式**: Bearer Token (JWT)
- **请求头**: 
  - `Content-Type: application/json`
  - `Authorization: Bearer {token}`

## 响应格式

所有接口统一返回格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1234567890
}
```

## 认证接口

### 1. 用户登录

**接口**: `POST /auth/login`

**请求参数**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "username": "admin",
    "realName": "系统管理员",
    "roles": ["ROLE_ADMIN"],
    "permissions": ["system", "user", "model", "knowledge"]
  }
}
```

### 2. Token 验证

**接口**: `GET /auth/validate?token={token}`

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

### 3. 退出登录

**接口**: `POST /auth/logout`

**响应**:
```json
{
  "code": 200,
  "message": "操作成功"
}
```

## 文档管理接口

### 1. 上传文档

**接口**: `POST /document/upload`

**请求类型**: `multipart/form-data`

**请求参数**:
- `categoryId`: 分类ID (必填)
- `title`: 文档标题 (必填)
- `file`: 文件 (必填)
- `tags`: 标签 (可选)

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "docTitle": "测试文档",
    "docType": "pdf",
    "fileSize": 102400,
    "status": 1,
    "createTime": "2024-01-01 12:00:00"
  }
}
```

### 2. 分页查询文档

**接口**: `GET /document/page`

**请求参数**:
- `pageNum`: 页码 (默认: 1)
- `pageSize`: 每页大小 (默认: 10)
- `categoryId`: 分类ID (可选)
- `keyword`: 关键词 (可选)

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "docTitle": "测试文档",
        "docType": "pdf",
        "categoryId": 1,
        "fileSize": 102400,
        "viewCount": 10,
        "status": 1,
        "createTime": "2024-01-01 12:00:00"
      }
    ],
    "total": 100,
    "current": 1,
    "size": 10
  }
}
```

### 3. 获取文档详情

**接口**: `GET /document/{id}`

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "docTitle": "测试文档",
    "docType": "pdf",
    "categoryId": 1,
    "content": "文档内容...",
    "fileSize": 102400,
    "viewCount": 11,
    "status": 1,
    "createTime": "2024-01-01 12:00:00"
  }
}
```

### 4. 删除文档

**接口**: `DELETE /document/{id}`

**响应**:
```json
{
  "code": 200,
  "message": "操作成功"
}
```

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 参数缺失 |
| 402 | 参数格式不正确 |
| 1001 | 未认证 |
| 1002 | Token无效 |
| 1003 | Token已过期 |
| 1004 | 无权限访问 |
| 2001 | 用户不存在 |
| 2004 | 密码错误 |
| 6002 | 文档不存在 |
| 8001 | 文件上传失败 |
| 8002 | 文件大小超出限制 |
| 8003 | 文件类型不支持 |
| 9001 | 系统错误 |

