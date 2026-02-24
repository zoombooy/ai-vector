# AI Manager Python Agent SDK

这是一个用于开发自定义Python Agent的SDK，可以轻松地将你的Python功能集成到AI Manager平台中。

## 目录结构

```
python-agents/
├── agent_sdk/              # Agent SDK核心代码
│   ├── __init__.py
│   ├── base_agent.py      # Agent基类
│   └── agent_server.py    # Agent HTTP服务器
├── examples/               # 示例Agent
│   ├── weather_agent.py   # 天气查询Agent示例
│   └── data_analysis_agent.py  # 数据分析Agent示例
├── requirements.txt        # Python依赖
└── README.md              # 本文件
```

## 快速开始

### 1. 安装依赖

```bash
cd python-agents
pip install -r requirements.txt
```

### 2. 运行示例Agent

#### 天气查询Agent

```bash
python examples/weather_agent.py
```

Agent将在 `http://localhost:5001` 启动

测试命令：
```bash
curl -X POST http://localhost:5001/execute \
  -H "Content-Type: application/json" \
  -d '{"input": {"city": "北京"}}'
```

#### 数据分析Agent

```bash
python examples/data_analysis_agent.py
```

Agent将在 `http://localhost:5002` 启动

测试命令：
```bash
curl -X POST http://localhost:5002/execute \
  -H "Content-Type: application/json" \
  -d '{"input": {"data": [1, 2, 3, 4, 5], "operation": "avg"}}'
```

### 3. 开发自己的Agent

创建一个新的Python文件，继承 `BaseAgent` 类：

```python
from agent_sdk import BaseAgent, AgentServer
from typing import Dict, Any, List

class MyAgent(BaseAgent):
    """我的自定义Agent"""
    
    def __init__(self):
        super().__init__(
            agent_code="my_agent",
            agent_name="我的Agent",
            description="这是我的自定义Agent"
        )
    
    def execute(self, input_data: Dict[str, Any]) -> Dict[str, Any]:
        """执行Agent逻辑"""
        # 在这里实现你的业务逻辑
        result = input_data.get('value', 0) * 2
        
        return {
            "result": result,
            "message": "处理成功"
        }
    
    def get_input_schema(self) -> Dict[str, Any]:
        """定义输入参数"""
        return {
            "type": "object",
            "properties": {
                "value": {
                    "type": "number",
                    "description": "输入值"
                }
            },
            "required": ["value"]
        }
    
    def get_capabilities(self) -> List[str]:
        """定义Agent能力"""
        return ["数据处理", "计算"]

if __name__ == "__main__":
    agent = MyAgent()
    server = AgentServer(agent, host="0.0.0.0", port=5003)
    server.run(debug=True)
```

### 4. 注册Agent到平台

启动Agent后，使用以下API将Agent注册到AI Manager平台：

```bash
curl -X POST http://localhost:8080/agent \
  -H "Content-Type: application/json" \
  -d '{
    "agentName": "我的Agent",
    "agentCode": "my_agent",
    "agentType": "PYTHON",
    "description": "这是我的自定义Agent",
    "endpointUrl": "http://localhost:5003/execute",
    "endpointType": "HTTP",
    "inputSchema": "{\"type\":\"object\",\"properties\":{\"value\":{\"type\":\"number\"}}}",
    "timeout": 30,
    "status": 1,
    "category": "自定义"
  }'
```

### 5. 调用Agent

注册成功后，可以通过平台API调用Agent：

```bash
curl -X POST http://localhost:8080/agent/execute \
  -H "Content-Type: application/json" \
  -d '{
    "agentCode": "my_agent",
    "input": {
      "value": 10
    }
  }'
```

## Agent SDK API

### BaseAgent 基类

所有自定义Agent都应该继承此类。

#### 必须实现的方法

- `execute(input_data: Dict[str, Any]) -> Dict[str, Any]`: 执行Agent逻辑

#### 可选重写的方法

- `get_input_schema() -> Dict[str, Any]`: 定义输入参数Schema
- `get_output_schema() -> Dict[str, Any]`: 定义输出结果Schema
- `get_capabilities() -> List[str]`: 定义Agent能力列表
- `get_category() -> str`: 定义Agent分类
- `get_tags() -> List[str]`: 定义Agent标签
- `get_timeout() -> int`: 定义超时时间（秒）
- `validate_input(input_data: Dict[str, Any]) -> bool`: 验证输入数据
- `before_execute(input_data: Dict[str, Any])`: 执行前的钩子方法
- `after_execute(output_data: Dict[str, Any])`: 执行后的钩子方法
- `on_error(error: Exception) -> Dict[str, Any]`: 错误处理钩子方法

### AgentServer 服务器

提供HTTP接口供Java后端调用。

#### 接口列表

- `GET /health`: 健康检查
- `GET /info`: 获取Agent信息
- `POST /execute`: 执行Agent
- `POST /config`: 设置配置

## 最佳实践

1. **输入验证**: 在 `validate_input` 方法中验证输入参数
2. **错误处理**: 在 `on_error` 方法中处理异常
3. **日志记录**: 使用 `logging` 模块记录关键操作
4. **超时设置**: 根据实际情况设置合理的超时时间
5. **Schema定义**: 详细定义输入输出Schema，便于AI理解和调用

## 常见问题

### Q: 如何处理敏感配置？

A: 使用 `set_config` 方法设置配置，不要在代码中硬编码敏感信息。

### Q: 如何实现异步执行？

A: 可以在 `execute` 方法中使用异步库（如 `asyncio`），或者返回任务ID后台执行。

### Q: 如何调试Agent？

A: 启动Agent时设置 `debug=True`，查看详细日志。

## 许可证

MIT License

