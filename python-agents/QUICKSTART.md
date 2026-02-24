# Agent接入快速启动指南

## 5分钟快速体验

### 步骤1: 初始化数据库

```bash
# 在项目根目录执行
mysql -u root -p ai_knowledge_platform < sql/create_agent_tables.sql
```

### 步骤2: 安装Python依赖

```bash
cd python-agents
pip install -r requirements.txt
```

### 步骤3: 启动示例Agent

打开一个新终端，启动天气查询Agent：

```bash
cd python-agents
python examples/weather_agent.py
```

你应该看到类似的输出：

```
============================================================
天气查询Agent已启动
Agent编码: weather_agent
Agent名称: 天气查询Agent
监听端口: 5001
============================================================

测试命令:
curl -X POST http://localhost:5001/execute \
  -H "Content-Type: application/json" \
  -d '{"input": {"city": "北京"}}'

============================================================

 * Running on all addresses (0.0.0.0)
 * Running on http://127.0.0.1:5001
```

### 步骤4: 测试Agent

打开另一个终端，测试Agent是否正常工作：

```bash
# 健康检查
curl http://localhost:5001/health

# 获取Agent信息
curl http://localhost:5001/info

# 执行Agent
curl -X POST http://localhost:5001/execute \
  -H "Content-Type: application/json" \
  -d '{"input": {"city": "北京"}}'
```

你应该看到类似的响应：

```json
{
  "status": "success",
  "agent_code": "weather_agent",
  "execution_time": 5,
  "city": "北京",
  "temperature": "25°C",
  "weather": "晴",
  "humidity": "65%",
  "wind_speed": "8m/s",
  "update_time": "2024-01-01 12:00:00"
}
```

### 步骤5: 启动后端服务

确保以下服务已启动：

1. MySQL数据库
2. AI Gateway (端口8080)
3. AI QA Service (端口8086)

### 步骤6: 注册Agent到平台

```bash
curl -X POST http://localhost:8080/agent \
  -H "Content-Type: application/json" \
  -d '{
    "agentName": "天气查询Agent",
    "agentCode": "weather_agent",
    "agentType": "PYTHON",
    "description": "查询指定城市的天气信息",
    "endpointUrl": "http://localhost:5001/execute",
    "endpointType": "HTTP",
    "authType": "NONE",
    "inputSchema": "{\"type\":\"object\",\"properties\":{\"city\":{\"type\":\"string\",\"description\":\"城市名称\"}},\"required\":[\"city\"]}",
    "timeout": 30,
    "status": 1,
    "category": "天气服务"
  }'
```

### 步骤7: 通过平台调用Agent

```bash
curl -X POST http://localhost:8080/agent/execute \
  -H "Content-Type: application/json" \
  -d '{
    "agentCode": "weather_agent",
    "input": {
      "city": "上海"
    }
  }'
```

### 步骤8: 查看执行历史

```bash
curl http://localhost:8080/agent/history?agentCode=weather_agent
```

## 自动化测试

我们提供了自动化测试脚本，可以一键测试所有功能：

### Python版本（推荐）

```bash
cd python-agents
python test_agent.py
```

### Bash版本

```bash
cd python-agents
chmod +x test_agent.sh
./test_agent.sh
```

## 开发你自己的Agent

### 1. 创建Agent文件

创建 `my_agent.py`：

```python
from agent_sdk import BaseAgent, AgentServer
from typing import Dict, Any

class MyAgent(BaseAgent):
    def __init__(self):
        super().__init__(
            agent_code="my_agent",
            agent_name="我的Agent",
            description="这是我开发的第一个Agent"
        )
    
    def execute(self, input_data: Dict[str, Any]) -> Dict[str, Any]:
        # 你的业务逻辑
        name = input_data.get('name', 'World')
        return {
            "message": f"Hello, {name}!",
            "timestamp": "2024-01-01 12:00:00"
        }
    
    def get_input_schema(self) -> Dict[str, Any]:
        return {
            "type": "object",
            "properties": {
                "name": {
                    "type": "string",
                    "description": "名字"
                }
            }
        }

if __name__ == "__main__":
    agent = MyAgent()
    server = AgentServer(agent, port=5003)
    server.run(debug=True)
```

### 2. 启动Agent

```bash
python my_agent.py
```

### 3. 测试Agent

```bash
curl -X POST http://localhost:5003/execute \
  -H "Content-Type: application/json" \
  -d '{"input": {"name": "张三"}}'
```

### 4. 注册到平台

```bash
curl -X POST http://localhost:8080/agent \
  -H "Content-Type: application/json" \
  -d '{
    "agentName": "我的Agent",
    "agentCode": "my_agent",
    "agentType": "PYTHON",
    "description": "这是我开发的第一个Agent",
    "endpointUrl": "http://localhost:5003/execute",
    "inputSchema": "{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"}}}",
    "timeout": 30,
    "status": 1
  }'
```

## 常见问题

### Q: Agent启动失败，提示端口被占用

A: 修改端口号，例如：

```python
server = AgentServer(agent, port=5010)  # 使用其他端口
```

### Q: 平台无法连接到Agent

A: 检查以下几点：
1. Agent服务是否正常启动
2. 防火墙是否允许该端口
3. endpointUrl是否正确（注意使用实际IP而不是localhost）

### Q: 如何查看Agent日志

A: Agent SDK使用Python的logging模块，日志会输出到控制台。

### Q: 如何部署到生产环境

A: 建议使用以下方式：
1. 使用gunicorn或uwsgi运行Agent
2. 使用supervisor或systemd管理进程
3. 使用nginx做反向代理
4. 配置HTTPS和认证

## 下一步

- 查看 [完整文档](../docs/AGENT_INTEGRATION.md)
- 查看 [SDK API文档](README.md)
- 查看更多示例：
  - [天气查询Agent](examples/weather_agent.py)
  - [数据分析Agent](examples/data_analysis_agent.py)

## 获取帮助

如有问题，请查看：
1. [完整文档](../docs/AGENT_INTEGRATION.md)
2. [API文档](README.md)
3. 示例代码

