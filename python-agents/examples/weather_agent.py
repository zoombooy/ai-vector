"""
天气查询Agent示例
演示如何使用Agent SDK开发一个简单的天气查询Agent
"""

import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))

from agent_sdk import BaseAgent, AgentServer
from typing import Dict, Any, List
import random


class WeatherAgent(BaseAgent):
    """天气查询Agent"""
    
    def __init__(self):
        super().__init__(
            agent_code="weather_agent",
            agent_name="天气查询Agent",
            description="查询指定城市的天气信息，包括温度、湿度、天气状况等。当用户询问天气相关问题时可以调用此Agent。"
        )
    
    def execute(self, input_data: Dict[str, Any]) -> Dict[str, Any]:
        """
        执行天气查询
        
        Args:
            input_data: 包含 city 字段的字典
            
        Returns:
            包含天气信息的字典
        """
        city = input_data.get('city', '北京')
        
        # 这里是模拟数据，实际应该调用真实的天气API
        # 例如：和风天气、OpenWeatherMap等
        weather_data = self._get_mock_weather(city)
        
        return {
            "city": city,
            "temperature": weather_data["temperature"],
            "weather": weather_data["weather"],
            "humidity": weather_data["humidity"],
            "wind_speed": weather_data["wind_speed"],
            "update_time": weather_data["update_time"]
        }
    
    def _get_mock_weather(self, city: str) -> Dict[str, Any]:
        """获取模拟天气数据"""
        import datetime
        
        # 模拟不同城市的天气
        weather_conditions = ["晴", "多云", "阴", "小雨", "中雨", "雷阵雨"]
        
        return {
            "temperature": f"{random.randint(15, 35)}°C",
            "weather": random.choice(weather_conditions),
            "humidity": f"{random.randint(40, 90)}%",
            "wind_speed": f"{random.randint(1, 15)}m/s",
            "update_time": datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        }
    
    def get_input_schema(self) -> Dict[str, Any]:
        """定义输入参数Schema"""
        return {
            "type": "object",
            "properties": {
                "city": {
                    "type": "string",
                    "description": "城市名称，例如：北京、上海、深圳"
                }
            },
            "required": ["city"]
        }
    
    def get_output_schema(self) -> Dict[str, Any]:
        """定义输出结果Schema"""
        return {
            "type": "object",
            "properties": {
                "city": {"type": "string", "description": "城市名称"},
                "temperature": {"type": "string", "description": "温度"},
                "weather": {"type": "string", "description": "天气状况"},
                "humidity": {"type": "string", "description": "湿度"},
                "wind_speed": {"type": "string", "description": "风速"},
                "update_time": {"type": "string", "description": "更新时间"}
            }
        }
    
    def get_capabilities(self) -> List[str]:
        """定义Agent能力"""
        return ["天气查询", "实时天气", "天气预报"]
    
    def get_category(self) -> str:
        """定义Agent分类"""
        return "天气服务"
    
    def get_tags(self) -> List[str]:
        """定义Agent标签"""
        return ["天气", "查询", "实时"]
    
    def validate_input(self, input_data: Dict[str, Any]) -> bool:
        """验证输入数据"""
        if 'city' not in input_data:
            raise ValueError("缺少必需参数: city")
        
        city = input_data['city']
        if not isinstance(city, str) or len(city.strip()) == 0:
            raise ValueError("city参数必须是非空字符串")
        
        return True


if __name__ == "__main__":
    # 创建Agent实例
    agent = WeatherAgent()
    
    # 创建并启动服务器
    server = AgentServer(agent, host="0.0.0.0", port=5001)
    
    print("=" * 60)
    print(f"天气查询Agent已启动")
    print(f"Agent编码: {agent.agent_code}")
    print(f"Agent名称: {agent.agent_name}")
    print(f"监听端口: 5001")
    print("=" * 60)
    print("\n测试命令:")
    print('curl -X POST http://localhost:5001/execute \\')
    print('  -H "Content-Type: application/json" \\')
    print('  -d \'{"input": {"city": "北京"}}\'')
    print("\n" + "=" * 60 + "\n")
    
    server.run(debug=True)

