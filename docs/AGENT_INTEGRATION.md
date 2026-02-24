# Agent接入功能使用指南

## 概述

AI Manager平台支持接入外部或自己开发的Agent，通过标准的HTTP接口与平台集成。本文档介绍如何开发、部署和使用自定义Agent。

## 架构设计

### 整体架构

```
┌─────────────────┐
│   AI Manager    │
│    Platform     │
└────────┬────────┘
         │
         │ HTTP API
         │
┌────────▼────────┐
│  Agent Gateway  │
│   (QA Service)  │
└────────┬────────┘
         │
         ├─────────────┬─────────────┬─────────────┐
         │             │             │             │
    ┌────▼────┐   ┌───▼────┐   ┌───▼────┐   ┌───▼────┐
    │ Python  │   │ Python │   │  HTTP  │   │  ...   │
    │ Agent 1 │   │ Agent 2│   │ Agent  │   │        │
    └─────────┘   └────────┘   └────────┘   └────────┘
```

### 数据库表结构

1. **t_external_agent**: Agent注册表，存储Agent的基本信息和配置
2. **t_agent_execution_history**: Agent执行历史表，记录每次调用的详细信息
3. **t_agent_config**: Agent配置参数表，存储Agent的配置信息

## 快速开始

### 1. 初始化数据库

执行SQL脚本创建Agent相关的表：

```bash
mysql -u root -p ai_knowledge_platform < sql/create_agent_tables.sql
```

### 2. 开发Python Agent

#### 安装依赖

```bash
cd python-agents
pip install -r requirements.txt
```

#### 创建自定义Agent

```python
from agent_sdk import BaseAgent, AgentServer
from typing import Dict, Any, List

class MyAgent(BaseAgent):
    def __init__(self):
        super().__init__(
            agent_code="my_agent",
            agent_name="我的Agent",
            description="Agent功能描述"
        )
    
    def execute(self, input_data: Dict[str, Any]) -> Dict[str, Any]:
        # 实现你的业务逻辑
        return {"result": "success"}
    
    def get_input_schema(self) -> Dict[str, Any]:
        return {
            "type": "object",
            "properties": {
                "param1": {"type": "string", "description": "参数1"}
            },
            "required": ["param1"]
        }

if __name__ == "__main__":
    agent = MyAgent()
    server = AgentServer(agent, host="0.0.0.0", port=5000)
    server.run()
```

### 3. 启动Agent服务

```bash
python my_agent.py
```

Agent将在指定端口启动HTTP服务。

### 4. 注册Agent到平台

使用API注册Agent：

```bash
curl -X POST http://localhost:8080/agent \
  -H "Content-Type: application/json" \
  -d '{
    "agentName": "我的Agent",
    "agentCode": "my_agent",
    "agentType": "PYTHON",
    "description": "Agent功能描述",
    "endpointUrl": "http://localhost:5000/execute",
    "endpointType": "HTTP",
    "inputSchema": "{\"type\":\"object\",\"properties\":{\"param1\":{\"type\":\"string\"}}}",
    "timeout": 30,
    "status": 1,
    "category": "自定义"
  }'
```

### 5. 调用Agent

```bash
curl -X POST http://localhost:8080/agent/execute \
  -H "Content-Type: application/json" \
  -d '{
    "agentCode": "my_agent",
    "input": {
      "param1": "value1"
    }
  }'
```

## API接口文档

### Agent管理接口

#### 1. 分页查询Agent

```
GET /agent/page?pageNum=1&pageSize=10&keyword=&category=
```

#### 2. 获取Agent详情

```
GET /agent/{id}
GET /agent/code/{agentCode}
```

#### 3. 创建Agent

```
POST /agent
Content-Type: application/json

{
  "agentName": "Agent名称",
  "agentCode": "agent_code",
  "agentType": "PYTHON",
  "description": "描述",
  "endpointUrl": "http://localhost:5000/execute",
  "inputSchema": "{}",
  "timeout": 30,
  "status": 1
}
```

#### 4. 更新Agent

```
PUT /agent/{id}
Content-Type: application/json
```

#### 5. 删除Agent

```
DELETE /agent/{id}
```

#### 6. 执行Agent

```
POST /agent/execute
Content-Type: application/json

{
  "agentCode": "agent_code",
  "input": {
    "param1": "value1"
  },
  "sessionId": "optional_session_id",
  "userId": 1
}
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "executionId": 1,
    "agentCode": "agent_code",
    "status": "SUCCESS",
    "output": {
      "result": "..."
    },
    "executionTime": 123
  }
}
```

#### 7. 获取可用Agent列表

```
GET /agent/available
```

#### 8. 获取执行历史

```
GET /agent/history?sessionId=xxx&agentCode=xxx
```

## Python Agent SDK

### BaseAgent 基类

#### 核心方法

- `execute(input_data)`: 执行Agent逻辑（必须实现）
- `get_input_schema()`: 定义输入参数Schema
- `get_output_schema()`: 定义输出结果Schema
- `get_capabilities()`: 定义Agent能力列表
- `validate_input(input_data)`: 验证输入数据

#### 生命周期钩子

- `before_execute(input_data)`: 执行前钩子
- `after_execute(output_data)`: 执行后钩子
- `on_error(error)`: 错误处理钩子

### AgentServer 服务器

提供以下HTTP接口：

- `GET /health`: 健康检查
- `GET /info`: 获取Agent信息
- `POST /execute`: 执行Agent
- `POST /config`: 设置配置

## 示例Agent

### 1. 天气查询Agent

```bash
cd python-agents
python examples/weather_agent.py
```

测试：
```bash
curl -X POST http://localhost:5001/execute \
  -H "Content-Type: application/json" \
  -d '{"input": {"city": "北京"}}'
```

### 2. 数据分析Agent

```bash
python examples/data_analysis_agent.py
```

测试：
```bash
curl -X POST http://localhost:5002/execute \
  -H "Content-Type: application/json" \
  -d '{"input": {"data": [1,2,3,4,5], "operation": "avg"}}'
```

## 最佳实践

### 1. 输入验证

始终在 `validate_input` 方法中验证输入参数：

```python
def validate_input(self, input_data: Dict[str, Any]) -> bool:
    if 'required_param' not in input_data:
        raise ValueError("缺少必需参数: required_param")
    return True
```

### 2. 错误处理

在 `on_error` 方法中处理异常：

```python
def on_error(self, error: Exception) -> Dict[str, Any]:
    logger.error(f"执行失败: {str(error)}")
    return {
        "error": str(error),
        "error_type": type(error).__name__
    }
```

### 3. 日志记录

使用logging模块记录关键操作：

```python
import logging
logger = logging.getLogger(__name__)

def execute(self, input_data):
    logger.info(f"开始执行: {input_data}")
    # ...
    logger.info(f"执行完成")
```

### 4. 超时设置

根据实际情况设置合理的超时时间：

```python
def get_timeout(self) -> int:
    return 60  # 60秒
```

### 5. Schema定义

详细定义输入输出Schema，便于AI理解和调用：

```python
def get_input_schema(self) -> Dict[str, Any]:
    return {
        "type": "object",
        "properties": {
            "param1": {
                "type": "string",
                "description": "详细的参数说明"
            }
        },
        "required": ["param1"]
    }
```

## 故障排查

### Agent无法连接

1. 检查Agent服务是否启动
2. 检查端口是否被占用
3. 检查防火墙设置

### 执行超时

1. 增加timeout配置
2. 优化Agent执行逻辑
3. 检查网络连接

### 参数验证失败

1. 检查输入参数格式
2. 查看Agent的input_schema定义
3. 检查validate_input方法

## 安全建议

1. **认证**: 使用API Key或Bearer Token进行认证
2. **加密**: 使用HTTPS传输敏感数据
3. **限流**: 设置合理的并发限制
4. **日志**: 记录所有调用日志，便于审计
5. **隔离**: 不同Agent使用不同的端口和进程

## 性能优化

1. **缓存**: 对频繁访问的数据进行缓存
2. **异步**: 使用异步处理提高并发能力
3. **连接池**: 复用HTTP连接
4. **资源限制**: 设置合理的内存和CPU限制

## 常见问题

### Q: 如何处理大数据量？

A: 可以使用流式处理或分批处理，避免一次性加载所有数据。

### Q: 如何实现Agent的版本管理？

A: 在Agent注册时指定version字段，可以同时运行多个版本的Agent。

### Q: 如何监控Agent的运行状态？

A: 使用 `/health` 接口进行健康检查，查看执行历史表了解调用情况。

### Q: 如何实现Agent的热更新？

A: 更新Agent代码后重启服务，平台会自动路由到新的Agent实例。

