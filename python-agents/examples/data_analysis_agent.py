"""
数据分析Agent示例
演示如何使用Agent SDK开发一个数据分析Agent
"""

import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))

from agent_sdk import BaseAgent, AgentServer
from typing import Dict, Any, List
import statistics


class DataAnalysisAgent(BaseAgent):
    """数据分析Agent"""
    
    def __init__(self):
        super().__init__(
            agent_code="data_analysis_agent",
            agent_name="数据分析Agent",
            description="对数据进行统计分析，支持求和、平均值、最大值、最小值、中位数、标准差等统计功能。"
        )
    
    def execute(self, input_data: Dict[str, Any]) -> Dict[str, Any]:
        """
        执行数据分析
        
        Args:
            input_data: 包含 data 和 operation 字段的字典
            
        Returns:
            包含分析结果的字典
        """
        data = input_data.get('data', [])
        operation = input_data.get('operation', 'sum')
        
        # 执行相应的统计操作
        if operation == 'sum':
            result = sum(data)
        elif operation == 'avg':
            result = statistics.mean(data)
        elif operation == 'max':
            result = max(data)
        elif operation == 'min':
            result = min(data)
        elif operation == 'median':
            result = statistics.median(data)
        elif operation == 'stdev':
            result = statistics.stdev(data) if len(data) > 1 else 0
        elif operation == 'variance':
            result = statistics.variance(data) if len(data) > 1 else 0
        else:
            raise ValueError(f"不支持的操作: {operation}")
        
        return {
            "operation": operation,
            "result": result,
            "data_count": len(data),
            "data_range": {
                "min": min(data) if data else None,
                "max": max(data) if data else None
            }
        }
    
    def get_input_schema(self) -> Dict[str, Any]:
        """定义输入参数Schema"""
        return {
            "type": "object",
            "properties": {
                "data": {
                    "type": "array",
                    "items": {"type": "number"},
                    "description": "数字数组，要分析的数据"
                },
                "operation": {
                    "type": "string",
                    "enum": ["sum", "avg", "max", "min", "median", "stdev", "variance"],
                    "description": "操作类型：sum(求和)、avg(平均值)、max(最大值)、min(最小值)、median(中位数)、stdev(标准差)、variance(方差)"
                }
            },
            "required": ["data", "operation"]
        }
    
    def get_output_schema(self) -> Dict[str, Any]:
        """定义输出结果Schema"""
        return {
            "type": "object",
            "properties": {
                "operation": {"type": "string", "description": "执行的操作"},
                "result": {"type": "number", "description": "计算结果"},
                "data_count": {"type": "integer", "description": "数据数量"},
                "data_range": {
                    "type": "object",
                    "properties": {
                        "min": {"type": "number", "description": "最小值"},
                        "max": {"type": "number", "description": "最大值"}
                    }
                }
            }
        }
    
    def get_capabilities(self) -> List[str]:
        """定义Agent能力"""
        return ["数据统计", "数据分析", "数学计算", "统计分析"]
    
    def get_category(self) -> str:
        """定义Agent分类"""
        return "数据分析"
    
    def get_tags(self) -> List[str]:
        """定义Agent标签"""
        return ["数据", "分析", "统计", "数学"]
    
    def get_timeout(self) -> int:
        """定义超时时间"""
        return 30
    
    def validate_input(self, input_data: Dict[str, Any]) -> bool:
        """验证输入数据"""
        if 'data' not in input_data:
            raise ValueError("缺少必需参数: data")
        
        if 'operation' not in input_data:
            raise ValueError("缺少必需参数: operation")
        
        data = input_data['data']
        if not isinstance(data, list):
            raise ValueError("data参数必须是数组")
        
        if len(data) == 0:
            raise ValueError("data数组不能为空")
        
        if not all(isinstance(x, (int, float)) for x in data):
            raise ValueError("data数组中的所有元素必须是数字")
        
        operation = input_data['operation']
        valid_operations = ['sum', 'avg', 'max', 'min', 'median', 'stdev', 'variance']
        if operation not in valid_operations:
            raise ValueError(f"operation必须是以下值之一: {', '.join(valid_operations)}")
        
        return True


if __name__ == "__main__":
    # 创建Agent实例
    agent = DataAnalysisAgent()
    
    # 创建并启动服务器
    server = AgentServer(agent, host="0.0.0.0", port=5002)
    
    print("=" * 60)
    print(f"数据分析Agent已启动")
    print(f"Agent编码: {agent.agent_code}")
    print(f"Agent名称: {agent.agent_name}")
    print(f"监听端口: 5002")
    print("=" * 60)
    print("\n测试命令:")
    print('curl -X POST http://localhost:5002/execute \\')
    print('  -H "Content-Type: application/json" \\')
    print('  -d \'{"input": {"data": [1, 2, 3, 4, 5], "operation": "avg"}}\'')
    print("\n" + "=" * 60 + "\n")
    
    server.run(debug=True)

